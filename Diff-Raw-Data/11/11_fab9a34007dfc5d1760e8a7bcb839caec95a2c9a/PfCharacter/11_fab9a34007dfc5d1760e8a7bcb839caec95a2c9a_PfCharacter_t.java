 package net.mindsoup.pathfindercharactersheet.pf;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.mindsoup.pathfindercharactersheet.R;
 import net.mindsoup.pathfindercharactersheet.pf.classes.PfClass;
 import net.mindsoup.pathfindercharactersheet.pf.feats.FeatFactory;
 import net.mindsoup.pathfindercharactersheet.pf.feats.PfFeats;
 import net.mindsoup.pathfindercharactersheet.pf.items.Item;
 import net.mindsoup.pathfindercharactersheet.pf.items.ItemEffects;
 import net.mindsoup.pathfindercharactersheet.pf.items.ItemSlots;
 import net.mindsoup.pathfindercharactersheet.pf.items.ItemType;
 import net.mindsoup.pathfindercharactersheet.pf.items.Weapon;
 import net.mindsoup.pathfindercharactersheet.pf.items.Wearable;
 import net.mindsoup.pathfindercharactersheet.pf.races.PfChooseBonusAttributeRace;
 import net.mindsoup.pathfindercharactersheet.pf.races.PfRace;
 import net.mindsoup.pathfindercharactersheet.pf.skills.PfSkill;
 import net.mindsoup.pathfindercharactersheet.pf.skills.PfSkills;
 import net.mindsoup.pathfindercharactersheet.pf.skills.SkillFactory;
 import net.mindsoup.pathfindercharactersheet.pf.util.Calculation;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class PfCharacter implements Parcelable {
 	private PfRace myRace;
 	private PfClass myClass;
 	private String name;
 	private long id;
 	
 	private Weapon ActiveMainhandWeapon = null;
 	private Weapon ActiveOffhandWeapon= null;
 	
 	// stats
 	private int constitution = 0;
 	private int strength = 0;
 	private int dexterity = 0;
 	private int intelligence = 0;
 	private int wisdom = 0;
 	private int charisma = 0;
 	
 	private int tempCon = 0;
 	private int tempStr = 0;
 	private int tempDex = 0;
 	private int tempInt = 0;
 	private int tempWis = 0;
 	private int tempCha = 0;
 	
 	private int xp = 0;
 	private int coin = 0;
 	private PfPace pace = PfPace.FAST;
 	private int availableSkillRanks = 0;
 	private int availableFeats = 0;
 	
 	private int newLevels = 0;
 	private int hitpoints = 0;
 	
 	private Map<PfSkills, PfSkill> trainedSkills = new HashMap<PfSkills, PfSkill>();
 	private Set<PfFeats> feats = new HashSet<PfFeats>();
 	private List<Item> inventory = new LinkedList<Item>();
 	
 	@SuppressWarnings("rawtypes")
 	public static final Parcelable.Creator CREATOR =
 	    new Parcelable.Creator() {
 	        public PfCharacter createFromParcel(Parcel in) {
 	            return new PfCharacter(in);
 	        }
 
 	        public PfCharacter[] newArray(int size) {
 	            return new PfCharacter[size];
 	        }
 	    };
 	    
 	public PfCharacter(Parcel in) {
 		readFromParcel(in);
 	}
 	
 	public PfCharacter(PfRace argRace, PfClass argClass, String name) {
 		if(argRace == null) {
 			throw new IllegalArgumentException("Character constructor does not accept null values for race.");
 		}
 		
 		if(argClass == null) {
 			throw new IllegalArgumentException("Character constructor does not accept null values for class.");
 		}
 		
 		this.myRace = argRace;
 		this.myClass = argClass;
 		this.name = name;
 	}
 	
 	public int getNewLevels() {
 		return this.newLevels;
 	}
 	
 	public void setNewLevels(int levels) {
 		this.newLevels = levels;
 	}
 	
 	public void setId(long id) {
 		this.id = id;
 	}
 	
 	public long getId() {
 		return this.id;
 	}
 	
 	public PfRace getRace() {
 		return myRace;
 	}
 	
 	public PfClass getPfClass() {
 		return myClass;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setPace(PfPace argPace) {
 		this.pace = argPace;
 	}
 	
 	public PfPace getPace() {
 		return this.pace;
 	}
 	
 	public int getAvailableSkillRanks() {
 		return availableSkillRanks;
 	}
 	
 	public int getMoney() {
 		return coin;
 	}
 	
 	public void setMoney(int copperPieces) {
 		coin = copperPieces;
 	}
 	
 	/**
 	 * This function should not be called to add skill ranks when
 	 * leveling up - only to initialise skill ranks at a certain 
 	 * amount. 
 	 * 
 	 * For leveling up, add XP (either addXp() or setXp())  and if 
 	 * a new level is reached levelUp() is automatically called, 
 	 * which adds the correct amount of skill ranks.
 	 */
 	public void setAvailableSkillRanks(int ranks) {
 		this.availableSkillRanks = ranks;
 	}
 	
 	public void setBaseConstitution(int argCon) {
 		this.constitution = argCon;
 	}
 	
 	public int getBaseConsistution() {
 		return this.constitution;
 	}
 	
 	public Calculation getConstitution() {
 		Calculation c = new Calculation();
 		c.add("Constitution",  this.constitution);
 		c.add("Racial modifier", myRace.getConModifier());
 		c.add("Temporary bonus", getTempConBoost());
 		
 		return c;
 	}
 	
 	public void setBaseStrength(int argStr) {
 		this.strength = argStr;
 	}
 	
 	public int getBaseStrength() {
 		return this.strength;
 	}
 	
 	public Calculation getStrength() {
 		Calculation c = new Calculation();
 		c.add("Strength",  this.strength);
 		c.add("Racial modifier", myRace.getStrModifier());
 		c.add("Temporary bonus", getTempStrBoost());
 		
 		return c;
 	}
 	
 	public void setBaseDexterity(int argDex) {
 		this.dexterity = argDex;
 	}
 	
 	public int getBaseDexterity() {
 		return this.dexterity;
 	}
 	
 	public Calculation getDexterity() {
 		Calculation c = new Calculation();
 		c.add("Dexterity",  this.dexterity);
 		c.add("Racial modifier", myRace.getDexModifier());
 		c.add("Temporary bonus", getTempDexBoost());
 		
 		return c;
 	}
 	
 	public int getBaseIntelligence() {
 		return this.intelligence;
 	}
 	
 	public void setBaseIntelligence(int argInt) {
 		this.intelligence = argInt;
 	}
 	
 	public Calculation getIntelligence() {
 		Calculation c = new Calculation();
 		c.add("Intelligence",  this.intelligence);
 		c.add("Racial modifier", myRace.getIntModifier());
 		c.add("Temporary bonus", getTempIntBoost());
 		
 		return c;
 	}
 	
 	public void setBaseWisdom(int argWis) {
 		this.wisdom = argWis;
 	}
 	
 	public int getBaseWisdom() {
 		return this.wisdom;
 	}
 	
 	public Calculation getWisdom() {
 		Calculation c = new Calculation();
 		c.add("Wisdom",  this.wisdom);
 		c.add("Racial modifier", myRace.getWisModifier());
 		c.add("Temporary bonus", getTempWisBoost());
 		
 		return c;
 	}
 	
 	public int getBaseAttributeValue(PfAttributes attr) {
 		switch(attr) {
 		case CHA:
 			return getBaseCharisma();
 		case CON:
 			return getBaseConsistution();
 		case DEX:
 			return getBaseDexterity();
 		case INT:
 			return getBaseIntelligence();
 		case STR:
 			return getBaseStrength();
 		default:
 			return getBaseWisdom();
 		}
 	}
 	
 	public void setBaseCharisma(int argCha) {
 		this.charisma = argCha;
 	}
 	
 	public int getBaseCharisma() {
 		return this.charisma;
 	}
 	
 	public void setBaseStats(int cha, int con, int dex, int intel, int str, int wis) {
 		this.charisma = cha;
 		this.constitution = con;
 		this.dexterity = dex;
 		this.intelligence = intel;
 		this.strength = str;
 		this.wisdom = wis;
 	}
 	
 	public Calculation getCharisma() {
 		Calculation c = new Calculation();
 		c.add("Charisma",  this.charisma);
 		c.add("Racial modifier", myRace.getChaModifier());
 		c.add("Temporary bonus", getTempChaBoost());
 		
 		return c;
 	}
 	
 	public void setTempConBoost(int boost) {
 		this.tempCon = boost;
 	}
 	
 	public int getTempConBoost() {
 		return this.tempCon;
 	}
 	
 	public void setTempChaBoost(int boost) {
 		this.tempCha = boost;
 	}
 	
 	public int getTempChaBoost() {
 		return this.tempCha;
 	}
 	
 	public void setTempDexBoost(int boost) {
 		this.tempDex = boost;
 	}
 	
 	public int getTempDexBoost() {
 		return this.tempDex;
 	}
 	
 	public void setTempIntBoost(int boost) {
 		this.tempInt = boost;
 	}
 	
 	public int getTempIntBoost() {
 		return this.tempInt;
 	}
 	
 	public void setTempStrBoost(int boost) {
 		this.tempStr = boost;
 	}
 	
 	public int getTempStrBoost() {
 		return this.tempStr;
 	}
 	
 	public void setTempWisBoost(int boost) {
 		this.tempWis = boost;
 	}
 	
 	public int getTempWisBoost() {
 		return this.tempWis;
 	}
 	
 	public int getTempBoostFor(PfAttributes attribute) {
 		switch(attribute) {
 		case CHA:
 			return this.tempCha;
 		case CON:
 			return this.tempCon;
 		case DEX:
 			return this.tempDex;
 		case INT:
 			return this.tempInt;
 		case STR:
 			return this.tempStr;
 		case WIS:
 			return this.tempWis;
 		default:
 			throw new RuntimeException("Invalid attribute. This should never happen!");
 		}
 	}
 	
 	public Calculation getAttributeValue(PfAttributes attribute) {
 		switch(attribute) {
 		case CHA:
 			return getCharisma();
 		case CON:
 			return getConstitution();
 		case DEX:
 			return getDexterity();
 		case INT:
 			return getIntelligence();
 		case STR:
 			return getStrength();
 		case WIS:
 			return getWisdom();
 		default:
 			throw new RuntimeException("Invalid attribute. This should never happen!");
 		}
 	}
 	
 	/**
 	 * This should only be called once, after character creation.
 	 */
 	public void initialiseSecondaryStatsForNewCharacter() {
 		// hitpoint con bonus
 		this.setHitpoints(this.getMaxHitpoints().sum() + this.getAttributeBonus(this.getConstitution()));
 		
 		// skill ranks (+1 skill rank for human)
 		int skillRanks = myClass.getBaseSkillRanksPerLevel() + this.getAttributeBonus(this.getIntelligence());
 		int feats = 1;
 		
 		if(this.getRace().getRace() == PfRaces.HUMAN) {
 			skillRanks++;
 			feats++;
 		}
 			
 		this.setAvailableSkillRanks(skillRanks);
 			
 		// feat (2 feats for humans)
 		this.setAvailableFeats(feats);
 		
 	}
 	
 	public int getLevel(int xp) {
 		// TODO: make this complete/better
 		switch(this.pace) {
 		case SLOW:
 			if(xp < 3000)
 				return 1;
 			else if (xp < 7500)
 				return 2;
 			else if (xp < 14000)
 				return 3;
 			else if (xp < 23000)
 				return 4;
 			else if (xp < 35000)
 				return 5;
 			else
 				return 6;
 		case FAST:
 			if(xp < 1300)
 				return 1;
 			else if (xp < 3300)
 				return 2;
 			else if (xp < 6000)
 				return 3;
 			else if (xp < 10000)
 				return 4;
 			else if (xp < 15000)
 				return 5;
 			else
 				return 6;
 		default:	// medium
 			if(xp < 2000)
 				return 1;
 			else if (xp < 5000)
 				return 2;
 			else if (xp < 9000)
 				return 3;
 			else if (xp < 15000)
 				return 4;
 			else if (xp < 23000)
 				return 5;
 			else
 				return 6;
 		} 
 	}
 	
 	public int getLevel() {
 		return getLevel(this.xp);
 	}
 	
 	public int getAttributeBonus(int attribute) {
 		// see core rulebook page 17
 		return (int)Math.floor( (attribute / 2.0) - 5);
 	}
 	
 	public int getAttributeBonus(Calculation attribute) {
 		return getAttributeBonus(attribute.sum());
 	}
 	
 	/**
 	 * Get the Base Attack Bonus. Since some characters can do multiple attacks per round (see getNumAttacksPerRound()),
 	 * make sure to include if this is attack 0, 1, 2, etc.
 	 * <br><br>
 	 * Note that this attack index is zero based, so the first attack has index 0, second attack index 1, etc.
 	 *  
 	 * @param attack Attack index within the round.
 	 * @return Base Attack Bonus
 	 */
 	public int getBaseAttackBonus(int attack) {
 		int bab = this.getPfClass().getAttackBonus(this.getLevel()) - (attack * this.getPfClass().getAttackBonus(this.myClass.getExtraAttackPerNumLevels() - 1));
 		return bab;
 
 	}
 	
 	public int getNumAttacksPerRound() {
 		return (int)Math.ceil((double)this.getLevel() / this.myClass.getExtraAttackPerNumLevels());
 	}
 	
 	/**
 	 * Change this character's active mainhand weapon. When changing weapons, always change mainhand first.
 	 * 
 	 * @param weapon
 	 * @return true on success
 	 */
 	public boolean setMainhandWeapon(Weapon weapon) {
 		this.ActiveMainhandWeapon = weapon;
 		
 		// if the weapon is a two handed weapon, we don't carry another weapon in our offhand
 		if(this.ActiveMainhandWeapon.getHandedness() == PfHandedness.TWOHAND) 
 			this.ActiveOffhandWeapon = null;
 		
 		return true;
 	}
 	
 	/**
 	 * Change this character's active offhand weapon. When changing weapons, always change mainhand first.
 	 * 
 	 * @param weapon
 	 * @return true on success
 	 */
 	public boolean setOffhandWeapon(Weapon weapon) {
 		// set an offhand weapon only if we're not already carrying a two handed weapon
 		if(this.ActiveMainhandWeapon.getHandedness() != PfHandedness.TWOHAND) {
 			this.ActiveOffhandWeapon = weapon;
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public Weapon getMainhandWeapon() {
 		return this.ActiveMainhandWeapon;
 	}
 	
 	public Weapon getOffhanddWeapon() {
 		return this.ActiveOffhandWeapon;
 	}
 	
 
 	
 
 	
 	public void levelUp(int oldLevel) {
 		
 		int newLevels = this.getLevel() - oldLevel;
 		
 		for(int i = 0; i < newLevels; i++) {
 			// add skill ranks
 			this.availableSkillRanks += myClass.getBaseSkillRanksPerLevel() + this.getAttributeBonus(this.getIntelligence());
 		
 			
 			// if this is an uneven level
 			// gain a feat
 			if( (oldLevel + i + 1) % 2 == 1)
 				this.availableFeats++;
 		}
 			
 	}
 	
 	public int getXp() {
 		return xp;
 	}
 	
 	public void setXp(int amount) {
 		this.xp = amount;
 	}
 	
 	public void addXp(int amount) {
 		this.setXp(this.getXp() + amount);
 	}
 	
 	public Calculation getMaxHitpoints() {
 		Calculation hp = new Calculation();
 		
 		hp.add("Hitpoints", this.hitpoints);			
 				
 		return hp;
 	}
 	
 	public void setHitpoints(int hitpoints) {
 		this.hitpoints = hitpoints;
 	}
 	
 	public Calculation getAC() {
 		Calculation ac = new Calculation();
 		// core rulebook page 179
 		// TODO: add armor/shield
 		ac.add("Base", 10);
 		
 		// bonus or penalty for tiny/large/etc characters
 		int sizeBonus = this.getSizeCMBModifier() * -1;
 		ac.add("Size", sizeBonus);
 		ac.add("Dexterity bonus", Math.min(this.getAttributeBonus(this.getDexterity()), getMaxDexBonus()) );
 		
 		for(Item i : inventory) {
 			int acbonus = 0;
 			if(i.isEquiped()) {
 				if(i.getEffects().get(ItemEffects.AC_BONUS) != null)
 					acbonus += i.getEffects().get(ItemEffects.AC_BONUS);
 				if(i.getType() == ItemType.WEARABLE)
 					acbonus += ((Wearable)i).getArmorClass();
 				
 				ac.add(i.getName(), acbonus);
 			}
 		}
 		
 		
 		return ac;
 	}
 	
 	public Calculation getReflexSave() {
 		Calculation ref = new Calculation();
 		ref.add("DEX bonus",  this.getAttributeBonus(this.getDexterity()));
 		ref.add("Reflex bonus", this.getPfClass().getReflexSaveModifier(this.getLevel()), true);
 		
 		for(Item i : inventory) {
 			int savebonus = 0;
 			if(i.isEquiped()) {
 				if(i.getEffects().get(ItemEffects.RESISTANCE) != null)
 					savebonus += i.getEffects().get(ItemEffects.RESISTANCE);
 				
 				ref.add(i.getName(), savebonus);
 			}
 		}
 		
 		return ref;
 	}
 	
 	public Calculation getWillSave() {
 		Calculation ref = new Calculation();
 		ref.add("WIS bonus",  this.getAttributeBonus(this.getWisdom()));
 		ref.add("Will bonus", this.getPfClass().getWillSaveModifier(this.getLevel()), true);
 		
 		for(Item i : inventory) {
 			int savebonus = 0;
 			if(i.isEquiped()) {
 				if(i.getEffects().get(ItemEffects.RESISTANCE) != null)
 					savebonus += i.getEffects().get(ItemEffects.RESISTANCE);
 				
 				ref.add(i.getName(), savebonus);
 			}
 		}
 		
 		return ref;
 	}
 	
 	public Calculation getFortitudeSave() {
 		Calculation ref = new Calculation();
 		ref.add("CON bonus",  this.getAttributeBonus(this.getConstitution()));
 		ref.add("Fortitude bonus", this.getPfClass().getFortSaveModifier(this.getLevel()), true);
 		
 		for(Item i : inventory) {
 			int savebonus = 0;
 			if(i.isEquiped()) {
 				if(i.getEffects().get(ItemEffects.RESISTANCE) != null)
 					savebonus += i.getEffects().get(ItemEffects.RESISTANCE);
 				
 				ref.add(i.getName(), savebonus);
 			}
 		}
 		
 		return ref;
 	}
 	
 	public int getMaxDexBonus() {
 		int result = 99;
 	
 		for(Item i : inventory) {
 			if(i.getType() == ItemType.WEARABLE)
 				result = Math.min(result, ((Wearable)i).getMaxDexBonus());
 		}
 		
 		return result;
 	}
 	
 	public Calculation getMeleeAttackBonus(int attack) {
 		return getAttackBonus(attack, true);
 	}
 	
 	private Calculation getAttackBonus(int attack, boolean isMelee) {
 		// core rulebook page 178
 		Calculation ab = new Calculation();
 		ab.add("Base attack bonus", this.getBaseAttackBonus(attack));
 		if(isMelee) {
 			ab.add("Strength modifier", this.getAttributeBonus(this.getStrength()));
 		} else {
 			ab.add("Dexterity modifier", this.getAttributeBonus(this.getDexterity()));
 		}
 		ab.add("Size modifier", this.getSizeAttackBonusModifier());
 		
 		
 		// TODO: add code to deal with non-proficient armor
 		for(Item i : inventory) {
 			if(i.getEffects().size() > 0 && i.isEquiped()) {
 				if(i.getEffects().get(ItemEffects.ATTACK_BONUS) != null)
 					ab.add(i.getName(), i.getEffects().get(ItemEffects.ATTACK_BONUS));
 			}
 				
 		}
 		
 		return ab; 
 	}
 	
 	public Calculation getRangedAttackBonus(int attack) {
 		return getAttackBonus(attack, false);
 	}
 	
 	private int getSizeAttackBonusModifier() {
 		switch(this.myRace.getSize()) {
 			case SMALL:
 				return 1;
 			case MEDIUM:
 				return 0;
 			case LARGE:
 				return -1;
 			default:
 				return 0;
 		}
 	}
 	
 	private int getSizeCMBModifier() {
 		switch(this.myRace.getSize()) {
 			case DIMINUTIVE:
 				return -4;
 			case TINY:
 				return -2;
 			case SMALL:
 				return -1;
 			case MEDIUM:
 				return 0;
 			case LARGE:
 				return 1;
 			case HUGE:
 				return 2;
 			case GARGANTUAN:
 				return 4;
 			case COLOSSAL:
 				return 8;
 			default:
 				throw new RuntimeException("Invalid character size!");
 		}
 	}
 	
 	public int getDamageModifier() {
 		double multiplier = 1.0;
 		
 		for(Item i : inventory) {
 			if(i.getType() == ItemType.WEAPON && i.isEquiped()) {
 				if( ((Weapon)i).getHandedness() == PfHandedness.TWOHAND )
 					multiplier = 1.5;
 			}
 		}
 			
 		return (int)Math.floor(this.getAttributeBonus(this.getStrength()) * multiplier);
 	}
 	
 	public int getAvailableFeats() {
 		return this.availableFeats;
 	}
 	
 	public void setAvailableFeats(int feats) {
 		this.availableFeats = feats;
 	}
 	
 	/**
 	 * Add a feat to this character
 	 * @param feat the feat to add
 	 * @return true if added successfully, false if not
 	 */
 	public boolean gainFeat(PfFeats feat) {
 		// if we have available feats AND we don't already have this feat
 		if(this.getAvailableFeats() > 0 && !feats.contains(feat)) {
 			if(FeatFactory.getPrerequisite(feat).satisfiesPrerequisite(this)) {
 				feats.add(feat);
 				this.setAvailableFeats(this.getAvailableFeats() - 1);
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean hasFeat(PfFeats feat) {
 		return feats.contains(feat);
 	}
 	
 	public Set<PfFeats> getFeats() {
 		Set<PfFeats> returnSet = new HashSet<PfFeats>(feats);
 		
 		returnSet.addAll(Arrays.asList(this.getPfClass().getClassFeats()));
 		
 		return returnSet;
 	}
 	
 	public void removeFeat(PfFeats feat) {
 		if(feats.remove(feat)) {
 			this.setAvailableFeats(this.getAvailableFeats() + 1);
 		}
 		
 	}
 	
 	/**
 	 * NOTE THAT THIS SHOULD ONLY BE USED DURING CHARACTER LOADING
 	 * 
 	 * For normal character skill training use spendSkillRankOnSkill
 	 * 
 	 * @param type
 	 * @param ranks
 	 */
 	public void putSkillRanksInSkill(PfSkills type, int ranks) {
 		PfSkill skill = trainedSkills.get(type);
 		
 		// if we did not train this skill already
 		if(skill == null) {
 			// get the skill and add it to our trained skills
 			skill = SkillFactory.getSkill(type);
 			trainedSkills.put(type, skill);
 		}
 		
 		skill.setRank(ranks);
 	}
 	
 	/**
 	 * Makes best effort to train a skill. Game rules may prohibit adding
 	 * more ranks to a skill, however. Use the return value to check how
 	 * many skill ranks were actually applied.
 	 * 
 	 * @param type the skill type to train
 	 * @param ranks how many ranks to put in the skill
 	 * @return the number of skill ranks actually applied to the skill
 	 */
 	public int trainSkill(PfSkills type, int ranks) {
 		// TODO: write unit test for this
 		
 		int maxRanks = 0;
 		
 		if(this.getAvailableSkillRanks() > 0) {
 			PfSkill skill = trainedSkills.get(type);
 			
 			// if we did not train this skill already
 			if(skill == null) {
 				// get the skill and add it to our trained skills
 				skill = SkillFactory.getSkill(type);
 				trainedSkills.put(type, skill);
 			}
 			
 			// calculate the maximum amount of skill ranks that can be applied:
 			// total skill rank must remain smaller than or equal to character level
 			// skill ranks must be available
 			maxRanks = Math.min(Math.min(this.getLevel() - skill.getRank(), ranks), this.getAvailableSkillRanks());
 			
 			// apply the skill ranks, making sure not to exceed our character level
 			skill.setRank( skill.getRank() + maxRanks);
 		}
 		// return 
 		return maxRanks;
 	}
 	
 	/**
 	 * Makes best effort to train a skill. Game rules may prohibit adding
 	 * more ranks to a skill, however. Use the return value to check how
 	 * many skill ranks were actually applied. This method automatically
 	 * subtracts the used skill ranks from the pool of available skill ranks
 	 * 
 	 * @param type the skill type to train
 	 * @param ranks how many ranks to put in the skill
 	 * @return the number of skill ranks actually applied to the skill
 	 */
 	public int spendSkillRankOnSkill(PfSkills type, int ranks) {
 		// TODO: write unit test for this
 		
 		int usedRanks = trainSkill(type, ranks);
 		
 		this.setAvailableSkillRanks(this.getAvailableSkillRanks() - usedRanks);
 		
 		return usedRanks;
 	}
 	
 	/**
 	 * Removes a certain amount of ranks from a skill
 	 * 
 	 * @param type the skill to untrain
 	 * @param ranks the amount of ranks to untrain
 	 * @return the actual amount of ranks that were untrained
 	 */
 	public int untrainSkill(PfSkills type, int ranks) {
 		
 		// TODO: write unit test for this
 		
 		int untrainedRanks = 0;
 		PfSkill skill = trainedSkills.get(type);
 		
 		if(skill != null) {
 			int currentRank = skill.getRank();
 			untrainedRanks = Math.min(currentRank, ranks);
 			
 			// we are completely unlearning the skill
 			if(untrainedRanks == currentRank) {
 				skill.setRank(0);
 				//trainedSkills.remove(type);
 			} else { // we aren't completely unlearning the skill
 				skill.setRank(currentRank - untrainedRanks);
 			}
 			
 			this.setAvailableSkillRanks(this.getAvailableSkillRanks() + untrainedRanks);
 		}
 		
 		return untrainedRanks;
 	}
 	
 	public boolean canUseSkill(PfSkills type) {
 		if(SkillFactory.getSkill(type).canUseUntrained())
 			return true;
 		
 		PfSkill skill = trainedSkills.get(type);
 		
 		// potentially some of the skills in our trained skill collection
 		// are actually rank 0, and therefore not usable
 		if(skill != null) {
 			if(skill.getRank() > 0)
 				return true;
 		}
 		
 		return false;
 	}
 	
 	public int getSkillRank(PfSkills type) {
 		if(trainedSkills.containsKey(type)) {
 			return trainedSkills.get(type).getRank();
 		}
 		
 		return 0;
 	}
 	
 	public Calculation getSkillBonus(PfSkills type) {
 		PfSkill skill = trainedSkills.get(type);
 		
 		Calculation trainedBonus = new Calculation();
 					
 		// if we have not trained the skill get an untrained skill 
 		// note that here we don't check if this skill can be used untrained
 		// use canUseSkill() to determine that
 		if(skill == null) 
 			skill = SkillFactory.getSkill(type);
 		
 		// we've trained the skill, so get the skill ranks
 		trainedBonus.add("Skill rank", skill.getRank());
 		
 		// is this a class skill? if so we get a bonus!
 		if(skill.isClassSkill(this.myClass) && skill.getRank() > 0)
 			trainedBonus.add("Class skill", 3);
 		
 		// our ability modifier for this skill
 		int abilityModifier = this.getAttributeBonus(this.getAttributeValue(skill.getAttribute()));
 		
 		trainedBonus.add("Ability modifier", abilityModifier);
 		trainedBonus.add("Racial bonus", myRace.getSkillBonus(type));
 
 		// add armor check penalty for skills where skill.hasArmorCheckPenalty() is true
 		if(skill.hasArmorCheckPenalty()) {
 			for(Item i : inventory) {
 				if(i.isEquiped()) {
 					if(i.getType() == ItemType.WEARABLE)
 						trainedBonus.add(i.getName(), ((Wearable)i).getArmorPenalty());
 				}
 			}
 		}
 		
 		
 		// feats
 		// Acrobatic feat
 		if(feats.contains(PfFeats.ACROBATIC) && (type == PfSkills.ACROBATICS || type == PfSkills.FLY))
 			trainedBonus.add("Acrobatic", 2);
 		
 		// Alertness feat
 		if(feats.contains(PfFeats.ALERTNESS) && (type == PfSkills.PERCEPTION || type == PfSkills.SENSE_MOTIVE))
 			trainedBonus.add("Alertness", 2);
 		
 		// Animal Affinity feat
 		if(feats.contains(PfFeats.ANIMAL_AFFINITY) && (type == PfSkills.HANDLE_ANIMAL || type == PfSkills.RIDE))
 			trainedBonus.add("Animal Affinity", 2);
 		
 		// Athletic feat
 		if(feats.contains(PfFeats.ATHLETIC) && (type == PfSkills.CLIMB || type == PfSkills.SWIM))
 			trainedBonus.add("Athletic", 2);
 		
 		// Intimidating prowess
 		if(skill.getType() == PfSkills.INTIMIDATE && this.hasFeat(PfFeats.INTIMIDATING_PROWESS))
 			trainedBonus.add("Intimidating Prowess", this.getAttributeBonus(this.getAttributeValue(PfAttributes.STR)));
 		
 		// Persuasive feat
 		if(feats.contains(PfFeats.PERSUASIVE) && (type == PfSkills.DIPLOMACY || type == PfSkills.INTIMIDATE))
 			trainedBonus.add("Persuasive", 2);
 		
 		return trainedBonus;
 	}
 	
 	public Map<PfSkills,PfSkill> getTrainedSkills() {
 		return trainedSkills;
 	}
 	
 	public int getPortraitRes() {
 		return R.drawable.ic_action_user;
 	}
 
 	@Override
 	public int describeContents() {
 		return 0;
 	}
 	
 	public boolean canChooseBonusStat() {
 		if(this.getRace().getRace() == PfRaces.HALFELF || this.getRace().getRace() == PfRaces.HALFORC || this.getRace().getRace() == PfRaces.HUMAN) 
 			return true;
 		
 		return false;
 	}
 	
 	public Calculation getCombatManeuverBonus(int attack) {
 		Calculation c = new Calculation();
 		
 		c.add("Base attack bonus", this.getBaseAttackBonus(attack));
 		
 		if(myRace.getSize() == PfSizes.DIMINUTIVE || myRace.getSize() == PfSizes.TINY || feats.contains(PfFeats.AGILE_MANEUVERS))
 			c.add("Dexterity modifier", this.getAttributeBonus(this.getAttributeValue(PfAttributes.DEX)));
 		else
 			c.add("Strength modifier", this.getAttributeBonus(this.getAttributeValue(PfAttributes.STR)));
 		
 		c.add("Size modifier", this.getSizeCMBModifier());
 		return c;
 	}
 	
 	public Calculation getCombatManeuverDefense(int attack) {
 		Calculation c = new Calculation();
 		
 		c.add("Base", 10);
 		c.add("Base attack bonus", this.getBaseAttackBonus(attack));
 		c.add("Strength modifier", this.getAttributeBonus(this.getAttributeValue(PfAttributes.STR)));
 		c.add("Dexterity modifier", this.getAttributeBonus(this.getAttributeValue(PfAttributes.DEX)));
 		c.add("Size modifier", this.getSizeCMBModifier());
 		
 		return c;
 	}
 	
 	//**********************************
 	// Inventory code
 	//**********************************
 	
 	public int addItem(Item item) {
 		int stackSize = item.getStackSize();
 		// if we already have one of these, just add it to the stack
 		if(inventory.contains(item)) {
 			Item i = inventory.get(inventory.indexOf(item));
 			stackSize = i.getStackSize() + item.getStackSize();
 			i.setStackSize(stackSize);
 		} else {
 			inventory.add(item);
 		}
 		
 		Collections.sort(inventory);
 		
 		return stackSize;
 			
 	}
 	
 	public boolean removeItem(Item item, int amount) {
 		if(inventory.contains(item)) {
 			Item i = inventory.get(inventory.indexOf(item));
 			
 			if(amount > i.getStackSize())
 				return false;
 			
 			if(amount < i.getStackSize()) {
 				i.setStackSize(i.getStackSize() - amount);
 				return true;
 			}
 			
 			// we're removing all of this item
 			inventory.remove(item);
 			return true;
 		}
 		
 		return false;
 			
 	}
 	
 	public float getTotalCarryingWeight() {
 		float weight = 0;
 		
 		for(Item i : inventory)
 			weight += i.getStackWeight();
 		
 		return weight;
 	}
 	
 	public List<Item> getInventoryItems() {
 		return inventory;
 	}
 	
 	@Override
 	public void writeToParcel(Parcel out, int flags) {
 		out.writeInt(myClass.getPfClass().ordinal());
 		out.writeInt(myRace.getRace().ordinal());
 		out.writeString(name);
 		out.writeInt(xp);
 		out.writeInt(coin);
 		out.writeLong(id);
 		out.writeInt(pace.ordinal());
 		out.writeInt(availableSkillRanks);
 		out.writeInt(newLevels);
 		out.writeInt(hitpoints);
 		
 		Bundle sb = new Bundle();
 		for(PfSkills s : trainedSkills.keySet())
 			sb.putParcelable(s.toString(), trainedSkills.get(s));
 			
 		out.writeBundle(sb);
 		
 		out.writeInt(charisma);
 		out.writeInt(constitution);
 		out.writeInt(dexterity);
 		out.writeInt(intelligence);
 		out.writeInt(strength);
 		out.writeInt(wisdom);
 		
 		if(this.canChooseBonusStat())
 			out.writeInt( ((PfChooseBonusAttributeRace)this.getRace()).getBonusAttribute().ordinal() );
 		
 		out.writeInt(availableFeats);
 		
 		int[] feat_ints = new int[feats.size()];
 		int i = 0;
 		for(PfFeats f : feats) {
 			feat_ints[i] = f.ordinal();
 			i++;
 		}
 		
 		out.writeIntArray(feat_ints);
 		
 		Bundle items = new Bundle();
 		for(Item item : inventory)
 			items.putParcelable(item.getName(), item);
 		
 		out.writeBundle(items);
 	}
 	
 	public void readFromParcel(Parcel in) {
 		myClass = PfClasses.getPfClass(PfClasses.getPfClass(in.readInt()));
 		myRace = PfRaces.getRace(PfRaces.getRace(in.readInt()));
 		name = in.readString();
 		xp = in.readInt();
 		coin = in.readInt();
 		id = in.readLong();
 		pace = PfPace.getPace(in.readInt());
 		availableSkillRanks = in.readInt();
 		newLevels = in.readInt();
 		hitpoints = in.readInt();
 		
 		Bundle b = in.readBundle();
 		b.setClassLoader(PfSkill.class.getClassLoader());
 		for(String s : b.keySet()) {
 			Parcelable p = b.getParcelable(s);
 			PfSkill skill = (PfSkill)p;
 			trainedSkills.put(skill.getType(), skill);
 		}
 		
 		charisma = in.readInt();
 		constitution = in.readInt();
 		dexterity = in.readInt();
 		intelligence = in.readInt();
 		strength = in.readInt();
 		wisdom = in.readInt();
 		
 		if(this.canChooseBonusStat()) 
 			((PfChooseBonusAttributeRace)this.getRace()).setBonusAttribute(PfAttributes.getAttribute(in.readInt()));
 		
 		availableFeats = in.readInt();
 		
 		int[] feat_ints = in.createIntArray();
 		for(int i : feat_ints)
 			this.feats.add(PfFeats.getFeat(i));
 		
 		Bundle items = in.readBundle();
 		items.setClassLoader(Item.class.getClassLoader());
 		
 		for(String s : items.keySet()) {
 			Parcelable p = items.getParcelable(s);
 			Item newItem = (Item)p;
 			this.addItem(newItem);
 		}
 	}
 
 	public void equipItem(Item item) {
 		unequipItem(item.getSlot());
 		
 		if(item.isEquiped()) {
 			item.unequip();
 		} else {			
 			item.equip();
 		}
 		
 	}
 	
 	public void unequipItem(ItemSlots slot) {
 		for(Item i : inventory) {
 			if(i.isEquiped() && i.getSlot() == slot)
 				i.unequip();
 		}
 	}
 
 	public void setBaseAttributeValue(int value, PfAttributes attribute) {
 		switch(attribute) {
 			case CHA:
 				setBaseCharisma(value);
				break;
 			case CON:
 				setBaseConstitution(value);
				break;
 			case DEX:
 				setBaseDexterity(value);
				break;
 			case INT:
 				setBaseIntelligence(value);
				break;
 			case STR:
 				setBaseStrength(value);
				break;
 			default:
 				setBaseWisdom(value);
				break;
 		}
 	}
 }
