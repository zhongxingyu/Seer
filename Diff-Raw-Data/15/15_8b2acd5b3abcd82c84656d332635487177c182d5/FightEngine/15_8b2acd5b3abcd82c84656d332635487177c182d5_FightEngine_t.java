 package org.infinite.engines.fight;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.infinite.db.dao.DaoManager;
 import org.infinite.db.dto.Item;
 import org.infinite.db.dto.PlayerKnowSpell;
 import org.infinite.db.dto.Spell;
 import org.infinite.db.dto.SpellAffectPlayer;
 import org.infinite.engines.items.ItemsEngine;
 import org.infinite.engines.magic.MagicEngine;
 import org.infinite.util.GenericUtil;
 import org.infinite.util.InfiniteCst;
 
 
 public class FightEngine {
 
 	/* ***************** START SPRING BEANS ***************** */
 	
 	private static DaoManager daoManager;
 	private static MagicEngine magicEngine;
 	private static ItemsEngine itemsEngine;
 	
 	public void setDaoManager(DaoManager daoManager) {
 		FightEngine.daoManager = daoManager;
 	}
 
 	public DaoManager getDaoManager() {
 		return daoManager;
 	}
 	
 	public void setMagicEngine(MagicEngine magicEngine) {
 		FightEngine.magicEngine = magicEngine;
 	}
 
 	public MagicEngine getMagicEngine() {
 		return magicEngine;
 	}
 	
 	public ItemsEngine getItemsEngine() {
 		return itemsEngine;
 	}
 
 	public void setItemsEngine(ItemsEngine itemsEngine) {
 		FightEngine.itemsEngine = itemsEngine;
 	}
 	/* ***************** END SPRING BEANS ***************** */
 	
 	
 
 	private static final Log LOG = LogFactory.getLog(FightEngine.class);
 
 	
 	
 
 	public ArrayList<FightRound> runFight(ArrayList<PlayerInterface> firstSide, ArrayList<PlayerInterface> secondSide) throws Exception{
 
 		ArrayList<FightRound> report = new ArrayList<FightRound>();
 		
 		ArrayList<PlayerInterface> fightOrder = new ArrayList<PlayerInterface>();
 		int[] targetOrder = null;
 
 		//prepare for fight
 		prepareForFight(firstSide,secondSide);
 		
 		int iRound = 0;
 		do {
 
 			//evaluate init & choose targets
 			targetOrder = evaluateInit(iRound,firstSide, secondSide, fightOrder);
 
 			evaluateAffectingSpell(fightOrder,iRound,report);
 			
 			
 			//loop and attack
 			for (int i = 0; i < fightOrder.size(); i++) {
 				
 				FightRound r = new FightRound(iRound);
 
 				//get attacker
 				PlayerInterface attacker = fightOrder.get( i );
 
 				//attacker has already been killed, skip!
 				if(! attacker.isAlive() ){
 					continue;
 				}
 
 				//get defender
 				PlayerInterface defender = fightOrder.get( targetOrder[i] );
 
 				//defender has already been killed, skip!
 				if(! defender.isAlive() ){
 					continue;
 				}
 
 				//fight!				
 				int atkType = attacker.getAttackType(defender);
 
 				r.setAttacker( attacker.getName() );
 				r.setDefender( defender.getName() );
 				r.setFirstSide( firstSide.contains(attacker) );
 
 
 				switch (atkType) {
 				case InfiniteCst.ATTACK_TYPE_WEAPON:					
 					meleeFight(iRound, attacker, defender,r );
 					break;
 				case InfiniteCst.ATTACK_TYPE_MAGIC:
 					magicFight(iRound, attacker, defender,r );
 					break;
 				default: //InfiniteCst.ATTACK_TYPE_IDLE
 					idleFight(attacker, r );
 					
 				break;
 				}
 
 				//after attack is resolved check if someone died
 
 				//if defender dies
 				if( ! defender.isAlive() ){
 
 					r.setEndRound(true);
 					loot(attacker,defender,r);
 										
 					if( firstSide.remove(defender) ){
 						LOG.debug(defender.getName()+" removed from firstSide "+firstSide.size()+" remains");
 					}
 					if( secondSide.remove(defender) ){
 						LOG.debug("------> "+defender.getName()+" removed from secondSide "+secondSide.size()+" remains");
 					}
 				}
 				
 				report.add(r);
 			}//for - fightorder
 			
 			iRound++;
 			
 		} while ( firstSide.size()> 0 && secondSide.size()>0);
 
 		
 		//TODO add recap XML
 
 		return report;
 	}
 
 	private void evaluateAffectingSpell(ArrayList<PlayerInterface> fightOrder, int round,	ArrayList<FightRound> report) {
 		// TODO evaluate spells effects
 		
 		for (int i = 0; i < fightOrder.size(); i++) {
 			
 			PlayerInterface p = fightOrder.get(i);
 			
 			//fist check if some spell elapsed
 			checkForElapsedSpells(p);
 			
 			for (int j = 0; j < p.getSpellsAffecting().size(); j++) 
 			{
 				
 				Spell s = p.getSpellsAffecting().get(j).getSpell();
 				
 				FightRound r = new FightRound(round);
 				r.setSpellEffect(true);
 				r.setAttacker( s.getName() );
 				r.setAttackImg( s.getImage() );
 				r.setDefender( p.getName() );
 				r.setAttackType(s.getSpelltype());
 				r.setRoundType(InfiniteCst.ATTACK_TYPE_MAGIC);
 				
 				switch (s.getSpelltype()) {
 				case InfiniteCst.MAGIC_ATTACK:
 					int dmg=0;
 						try {
 							dmg = GenericUtil.rollDice( s.getDamage() );
 						} catch (Exception e) {
 							LOG.error("evaluateAffectingSpell - DIce:"+s.getDamage(),e);
 						}
 					p.inflictDamage(dmg);
 					r.setAttackDmg(dmg);
 					
 					break;
 				case InfiniteCst.MAGIC_HEAL:
 
 					int heal=0;
 					try {
 						heal = GenericUtil.rollDice( s.getDamage() );
 					} catch (Exception e) {
 						LOG.error("evaluateAffectingSpell - DIce:"+s.getDamage(),e);
 					}
 					p.healDamage(heal);
 					r.setAttackDmg(heal);
 					
 					break;
 				case InfiniteCst.MAGIC_DEFEND:
 					//this should not have any active effect, only stats modification
 					break;
 				}
 				
 				report.add(r);
 				
 			}
 			
 			
 		}
 		
 		
 	}
 
 	private void loot(PlayerInterface attacker, PlayerInterface defender, FightRound r) {	
 		
 		attacker.addGold( defender.getRewardGold() );
 		r.setGold(defender.getRewardGold());
 		
 		attacker.addExperience( defender.getRewardPX() );
 		r.setPx(defender.getRewardPX());
 		
 		ArrayList<Item> items = defender.getRewardItems();
 		attacker.lootItems( items,getItemsEngine() );
 		r.addItems(items);
 	}
 
 	
 	private void idleFight(PlayerInterface attacker, FightRound r) {
 		int points = attacker.restRound(1);
 		r.setAttacker(attacker.getName());
 		r.setAttackDmg(points);
 		r.setRoundType(InfiniteCst.ATTACK_TYPE_IDLE);
 		
 	}
 
 	
 	private void magicFight(int round, PlayerInterface attacker, PlayerInterface defender,FightRound r ) {		
 		
 
 		String[] szAtkData = attacker.getAttackName(round);
 		
 		r.setAttackName(szAtkData[0]);
 		r.setAttackImg(szAtkData[1]);
 		r.setRoundType(InfiniteCst.ATTACK_TYPE_MAGIC);
 
		Spell spell = ((PlayerKnowSpell)attacker.getCurrentAttack(round)).getSpell();		
 		
 		spell = attacker.castSpell( spell , getMagicEngine());
 		
 		if(spell==null){
 			r.setAttackType(InfiniteCst.MAGIC_UNCAST);
			r.setHit(false);
 			return;
		}else{
			r.setHit(true);
 		}
 		
 		int type = spell.getSpelltype();
 		r.setAttackType(type);
 
 		switch (type) 
 		{
 		case InfiniteCst.MAGIC_ATTACK:
 
 			int dmg=0;
 			try {
 				dmg = GenericUtil.rollDice( spell.getDamage() );
 			} catch (Exception e) {
 				LOG.error("magicFight DICE:"+spell.getDamage(),e);
 			}
 
 			if( (spell.getSavingthrow() >= 0 ) && getMagicEngine().rollSavingThrow(spell, attacker, defender) ){
 				dmg = Math.round(dmg/2);
 			}
 
 			defender.inflictDamage(dmg);
 			r.setAttackDmg(dmg);
 			break;
 			
 		case InfiniteCst.MAGIC_HEAL:
 
 			try {
 					int heal = GenericUtil.rollDice( spell.getDamage() );
 					attacker.healDamage( heal );
 					//out.setTextContent("Healed for "+heal+"HP");
 					r.setAttackDmg(heal);
 				} catch (Exception e) {
 					LOG.error("DICE:"+spell.getDamage(),e);
 				}
 
 			break;
 
 		default:
 			break;
 		}
 
 		if(spell.getDuration()>0){
 			//attack spells last on target, protection & heal last on caster
 			if( spell.getSpelltype()== InfiniteCst.MAGIC_ATTACK ){
 				defender.addToAffectingSpells(spell,getMagicEngine());
 			}
 			else{
 				attacker.addToAffectingSpells(spell,getMagicEngine());
 			}
 		}
 		
 	}
 
 	
 	private void meleeFight(int round, PlayerInterface attacker, PlayerInterface defender, FightRound r ) {
 
 		//int atkRoll = attacker.getRollToAttack();
 		int defCA = defender.getTotalCA();
 
 		//String[] szAtkData = attacker.getAttackName(round);
 		String[] szAtkData = attacker.getMeleeAttacks(round);
 		
 		String attacksName = "";
 		String attacksImg = "";
 		int[] attackRolls = new int[szAtkData.length];
 		int dmg = 0;
 		
 		for (int i = 0; i < szAtkData.length; i++) {
 			
 			//System.out.println(szAtkData[i]);
 			
 			String[] szTmp = szAtkData[i].split(InfiniteCst.ATTACK_INNERSEPARATOR);
 			attacksName += "," + szTmp[0];
 			attacksImg  += "," + (( szTmp.length == 3 )?szTmp[1]:szTmp[0]);
 			String roll = (( szTmp.length == 3 )?szTmp[2]:szTmp[1]);
 			attackRolls[i] = attacker.getRollToAttack();
 			
 			if( attackRolls[i] >= defCA ){
 				try {
 					dmg += GenericUtil.rollDice(roll);
 				} catch (Exception e) {
 					GenericUtil.err("meleeFight - rolldice:"+roll, e);
 					e.printStackTrace();
 				}
 			}
 			
 		}
 		
 		r.setRoundType(InfiniteCst.ATTACK_TYPE_WEAPON);
 		r.setAttackName(attacksName.substring(1) ); //r.setAttackName(szAtkData[0]);
 		r.setAttackImg(attacksImg.substring(1) ); //r.setAttackImg(szAtkData[1]);
 		r.setAttackRoll(attackRolls); //r.setAttackRoll(atkRoll);
 		r.setDefenderCA(defCA);
 
 		if( dmg>0 ){
 			int remain = defender.inflictDamage(dmg);
 			r.setHit(true);
 			r.setAttackDmg(dmg);
 			r.setRemainHp(remain);
 		}
 		else{
 			r.setHit(false);
 		}
 		
 		/*
 		if(atkRoll>= defCA){
 			int inflict = attacker.getAttackDamage(round);
 			int remain = defender.inflictDamage(inflict);
 			r.setHit(true);
 			r.setAttackDmg(inflict);
 			r.setRemainHp(remain);
 		}
 		else{
 			r.setHit(false);
 		}
 		*/
 
 	}
 	
 	
 	private void prepareForFight(ArrayList<PlayerInterface> firstSide, ArrayList<PlayerInterface> secondSide) {
 		
 		for (int i = 0; i < firstSide.size(); i++) {
 			firstSide.get(i).prepareForFight(this);
 		}
 		
 		for (int i = 0; i < secondSide.size(); i++) {
 			secondSide.get(i).prepareForFight(this);
 		}
 
 	}
 
 	
 	private int[] evaluateInit(int round, ArrayList<PlayerInterface> firstSide, ArrayList<PlayerInterface> secondSide, ArrayList<PlayerInterface> fightOrder) {
 
 		//populate target arrays & temporary whole array
 		fightOrder.clear();
 		ArrayList<PlayerInterface> tmpOrder = new ArrayList<PlayerInterface>();
 		tmpOrder.addAll(firstSide);
 		tmpOrder.addAll(secondSide);
 
 		//get all init values
 		int[] initValues = new int[tmpOrder.size()];
 		int[] initOrder = new int[initValues.length];
 		int[] targetOrder = new int[initValues.length];
 		for (int i = 0; i < initValues.length; i++) {
 			initValues[i] = tmpOrder.get(i).getInitiative( round );			
 		}
 
 		//sort init array and get vector order
 		initOrder = GenericUtil.quickSort(initValues);
 		for (int i = 0; i < initValues.length; i++) {
 			fightOrder.add(i, tmpOrder.get(initOrder[i]));
 		}
 
 		/*
 		System.out.println("\n----- Fight Order -----");
 		for (int i = 0; i < fightOrder.size(); i++) {
 			System.out.println(i+":"+fightOrder.elementAt(i).getName());
 		}
 		 */
 
 		//every fighter of first choose a target of second side
 		for (int i = 0; i < firstSide.size(); i++) {
 
 			//choose randomly a target from the other side
 			int iRandomChoose = (int) Math.floor(  Math.random()  * (secondSide.size()-1) );
 
 			//which position has the defender ?
 			PlayerInterface defender = secondSide.get(iRandomChoose);
 			int iDefenderIndex = fightOrder.indexOf(defender);
 
 			//which position has the attacker ?
 			PlayerInterface attacker = firstSide.get(i);
 			int iAttackerIndex = fightOrder.indexOf(attacker);
 
 			targetOrder[iAttackerIndex] = iDefenderIndex;
 		}
 
 
 		//do the same for other side
 		for (int i = 0; i < secondSide.size(); i++) {
 
 			//choose randomly a target from the other side
 			int iRandomChoose = (int) Math.floor(  Math.random()  * (firstSide.size()-1) );
 
 			//which position has the defender ?
 			PlayerInterface defender = firstSide.get(iRandomChoose);
 			int iDefenderIndex = fightOrder.indexOf(defender);
 
 			//which position has the attacker ?
 			PlayerInterface attacker = secondSide.get(i);
 			int iAttackerIndex = fightOrder.indexOf(attacker);
 
 			targetOrder[iAttackerIndex] = iDefenderIndex;
 		}
 
 		return targetOrder;
 	}
 
 	
 	
 	public static int getAvailableAttackSlot(PlayerInterface p) {
 		return p.getLevel() / InfiniteCst.CFG_LV_TO_BATTLE_PLAN_SLOTS +1;
 	}
 
 	
 	public void checkForElapsedSpells(PlayerInterface p){
 
 		long now = (new Date()).getTime();
 		for (int i = 0; i < p.getSpellsAffecting().size(); i++) {
 
 			SpellAffectPlayer sap = p.getSpellsAffecting().get(i);
 			if(sap.getElapsing() <= now ){
 				p.removeSpellsAffecting( p.getSpellsAffecting().get(i).getId() );
 				getDaoManager().delete( sap );
 			}		
 			
 		}		
 	}
 	
 	public Item[] parseUnarmedAttack( String attack) {
 
 		String[] szNames = attack.split("/");
 		Item[] items = new Item[szNames.length];
 		
 		for (int i = 0; i < items.length; i++) {
 			szNames = attack.split(",");
 			Item it = new Item(szNames[0],"",szNames[0], 1,0,0,0,0,0,0,0,0,0,0,1,1,szNames[1],1,-1,InfiniteCst.EQUIP_ISWEAPON);
 			items[i] = it;
 		}	
 		
 		return items;
 	}
 	
 }
