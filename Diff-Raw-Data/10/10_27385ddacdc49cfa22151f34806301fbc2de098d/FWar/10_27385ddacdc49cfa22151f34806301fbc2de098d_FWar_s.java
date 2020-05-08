 package com.massivecraft.factions;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 import com.massivecraft.factions.integration.Econ;
 import com.massivecraft.factions.struct.Relation;
 import com.massivecraft.factions.zcore.persist.Entity;
 
 public class FWar extends Entity{	
 	public transient Set<InventoryView> tempInvs= new HashSet<InventoryView>();
 	public transient Set<InventoryView> tempInvsFromTarget = new HashSet<InventoryView>();
 	
 	public Map<String,Integer> items = new HashMap<String,Integer>();
 	public int money;
 	
 	public int moneyFromTarget;
 	public Map<String,Integer> itemsToPay = new HashMap<String,Integer>();
 	
 	private String attackerFactionID, targetFactionID;
 	public Faction getAttackerFaction(){ return Factions.i.get(attackerFactionID);}
 	public Faction getTargetFaction(){ return Factions.i.get(targetFactionID);}
 	
 	public boolean isStarted;
 	public boolean isWar;
 	public long time;
 	public long timeToNextPayForMorePlayersThenTarget;
 	
 	public long timeToDeleteFWar;
 	
 	
 	public FWar(Faction attacker, Faction target){
 		this.attach();
 		
 		this.attackerFactionID=attacker.getId();
 		this.targetFactionID=target.getId();
 		
 		this.isStarted = false; // Obviously the war isn't started yet
 		this.isWar = false;//No War until time passed
 		
 		this.timeToDeleteFWar = System.currentTimeMillis();
 	}
 	
 	public void startWar(){
 		this.isStarted = true;
 		
 		/* Set the relation to enemy */
 		//attackerFaction.setRelationWish(targetFaction, Relation.ENEMY);
 		
 		/* Set time */
 		this.time = System.currentTimeMillis();
 		
 		/* Send the messages to both of the factions */
 		getTargetFaction().sendMessage("Eurer Fraktion wurde eine Forderung gestellt, in hhe von "+getDemandsAsString());
 		getTargetFaction().sendMessage("Die Forderung kam von "+getAttackerFaction().getTag()+" und falls ihr nicht innerhalb "+getTimeToWar()+" bezahlt, werden sie angreifen!");
 		
 		getAttackerFaction().sendMessage("Eure Fraktion hat "+getTargetFaction().getTag()+" Forderungen in hhe von "+getDemandsAsString()+" gestellt");
 		getAttackerFaction().sendMessage("Falls sie nicht innerhalb "+getTimeToWar()+" zahlen, kommt es zum Krieg!");
 		
 		/* Copy items */
 		itemsToPay= new HashMap<String, Integer>(items);
 	}
 	
 	public String getDemandsAsString(){
 		String ausgabe="";
 		
 		if(Conf.econEnabled){
 			if(this.money>0)
 				ausgabe=ausgabe+Econ.moneyString(this.money)+", ";
 		}
 		
 		int i=0;
 		for(String itemString:items.keySet()){
 			MaterialData item=convertStringToMaterialData(itemString);
 			i++;
 			if(i>1 && i < items.size()) ausgabe=ausgabe+", ";
 			
 			if(i==items.size()) ausgabe=ausgabe+" und ";
 			
 			Integer args=items.get(itemString);
 			
 			ausgabe=ausgabe+args+" "+item;
 		}
 		
 		return ausgabe;
 	}
 	
 	public String getTimeToWar(){
 		long timeToWar=(Conf.fwarHoursUntilWarStartsAfterDemand*60*60*1000)-(System.currentTimeMillis()-this.time);
 		
 		
 		
 		return (int)Math.floor(timeToWar/(60*60*1000))+"h "+(int)Math.floor(timeToWar%(60*60*1000)/(60*1000))+"min";
 	}
 	
 	
 	public long getMilliTimeToWar(){
 		long timeToWar=(Conf.fwarHoursUntilWarStartsAfterDemand*60*60*1000)-(System.currentTimeMillis()-this.time);
 		
 		return timeToWar;
 	}
 	
 	
 	public long getMilliTimeToDeleteFWar(){
 		long timeToDeleteFWar=(30*60*1000)-(System.currentTimeMillis()-this.timeToDeleteFWar);
 		
 		return timeToDeleteFWar;
 	}
 	
 	
 	public static void checkForDeleteFWars(){
 		for(FWar war:FWars.i.get()){
 			if(war.isStarted==false){
 				if(war.getMilliTimeToDeleteFWar()<0){
 					war.remove();
 					war.getAttackerFaction().sendMessage(ChatColor.GOLD+"Kriegserklrungen gegen "+ChatColor.GREEN+war.getTargetFaction().getTag()+ChatColor.GOLD+" wurden abgebrochen da ihr lnger als 30 Minuten gebraucht habt diese auszuarbeiten!");
 				}
 			}
 		}
 	}
 	
 	
 	
 	public static void setRelationshipWhenTimeToWarIsOver(){
 		for(FWar war:FWars.i.get()){
 			if(!war.isWar){
 				if(war.getMilliTimeToWar()<=0){
 					war.isWar=true;
 					war.getAttackerFaction().setRelationWish(war.getTargetFaction(), Relation.ENEMY);
 					war.getTargetFaction().setRelationWish(war.getAttackerFaction(), Relation.ENEMY);
 					
 					war.getAttackerFaction().sendMessage(ChatColor.GOLD+"Der Krieg gegen die Fraktion "+ChatColor.GREEN+war.getTargetFaction().getTag()+ChatColor.GOLD+" hat begonnen!");
 					war.getTargetFaction().sendMessage(ChatColor.GOLD+"Der Krieg gegen die Fraktion "+ChatColor.GREEN+war.getAttackerFaction().getTag()+ChatColor.GOLD+" hat begonnen!");
 					
 					war.timeToNextPayForMorePlayersThenTarget = System.currentTimeMillis();
 				}
 			}
 		}
 	}
 	
 	
 	public long getMilliTimeToNextPay(){
 		long timeToPay=(24*60*60*1000)-(System.currentTimeMillis()-this.timeToNextPayForMorePlayersThenTarget);
 		
 		return timeToPay;
 	}
 	
 	
 	public static void payForMorePlayersThenTarget(){
 		if(Conf.econEnabled){
 			for(FWar war:FWars.i.get()){
 				if(war.isWar){
 					if(war.getMilliTimeToNextPay()<0){
 						if(war.getAttackerFaction().getFPlayers().size()>war.getTargetFaction().getFPlayers().size()){
 							int zwischenwert=war.getAttackerFaction().getFPlayers().size()-war.getTargetFaction().getFPlayers().size();
 							if(Econ.modifyMoney(war.getAttackerFaction(), -(zwischenwert*10), "", "for paying the Playerdifference in a War")){
 								
 							}else{
 								war.remove();
 								war.getAttackerFaction().sendMessage("Der Krieg gegen "+war.getTargetFaction().getTag()+" wurde beendet da ihr kein Geld mehr habt um die Spielerdifferenz auszugleichen!");
 								war.getTargetFaction().sendMessage("Der Krieg gegen "+war.getAttackerFaction().getTag()+" wurde beendet da sie kein Geld mehr habt um die Spielerdifferenz auszugleichen!");
 							}
 						}
 						war.timeToNextPayForMorePlayersThenTarget = System.currentTimeMillis();
 					}
 				}
 			}
 		}
 	}
 	
 	
 	public void addTempInventory(InventoryView inv){
 		tempInvs.add(inv);
 	}
 	
 	public void removeTempInventory(InventoryView inv){
 		if(tempInvs.contains(inv)){
 			for(ItemStack istack:inv.getTopInventory().getContents()){
 				if(istack!=null){
 					if(istack.getEnchantments().isEmpty()){ //Forbid enchanted items
 						if(istack.getDurability()==0){ //Forbid damaged items
 							boolean blackContains=false;
 							for(String string:Conf.fwarItemBlackList){
 								MaterialData mat=convertStringToMaterialData(string);
 								
 								if(mat.getItemTypeId()==istack.getTypeId() && mat.getData()==istack.getData().getData()){
 									blackContains=true;
 								}
 							}
 							if(!blackContains){
 								Integer args;
 								if(items.get(convertMaterialDataToString(istack.getData()))==null){
 									args=istack.getAmount();
 									items.put(convertMaterialDataToString(istack.getData()), args);
 								}
 								else{
 									Integer argsOLD = items.get(convertMaterialDataToString(istack.getData()));
 									args=argsOLD+istack.getAmount();
 									items.put(convertMaterialDataToString(istack.getData()), args);
 								}
 							}else{
 								inv.getPlayer().getWorld().dropItem(inv.getPlayer().getLocation(), istack);
 								FPlayers.i.get((Player) inv.getPlayer()).sendMessage(ChatColor.RED+""+istack.getData().getData()+" ist auf der Blacklist. Du kannst es nicht verwenden.");
 							}
 						}else{
 							inv.getPlayer().getWorld().dropItem(inv.getPlayer().getLocation(), istack);
 							FPlayers.i.get((Player) inv.getPlayer()).sendMessage(ChatColor.RED+"Du kannst keine kaputte Items verwenden!");
 						}
 					}else{
 						inv.getPlayer().getWorld().dropItem(inv.getPlayer().getLocation(), istack);
 						FPlayers.i.get((Player) inv.getPlayer()).sendMessage(ChatColor.RED+"Du kannst keine verzauberte Items verwenden!");
 					}
 				}
 			}
 			inv.getTopInventory().clear();
 			tempInvs.remove(inv);
 		}
 	}
 	
 	public void addTempInventoryFromTarget(InventoryView inv){
 		tempInvsFromTarget.add(inv);
 	}
 	
 	public void removeTempInventoryFromTarget(InventoryView inv){
 		tempInvsFromTarget.remove(inv);
 	}
 	
 	public void remove(){
 		FWars.i.detach(this);
 		
 		for(String matString:items.keySet()){
 			MaterialData mat=convertStringToMaterialData(matString);
 			Integer args;
 			if(getAttackerFaction().factionInventory.get(matString)==null){
 				getAttackerFaction().factionInventory.put(convertMaterialDataToString(mat), items.get(matString));
 			}else{
 				args=items.get(matString);
 				Integer argsOLD=getAttackerFaction().factionInventory.get(matString);
 				args=args+ argsOLD;
 				getAttackerFaction().factionInventory.put(convertMaterialDataToString(mat), args);
 				
 			}
 		}
 		
 		
 		
 		if(getAttackerFaction().getRelationTo(getTargetFaction())==Relation.ENEMY){
 			getAttackerFaction().setRelationWish(getTargetFaction(), Relation.NEUTRAL);
 			getTargetFaction().setRelationWish(getAttackerFaction(), Relation.NEUTRAL);
 		}
 		
 		
 	}
 	
 	// Get functions
 	
 	public static FWar get(Faction attackerFaction, Faction targetFaction){
 		for(FWar war:FWars.i.get()){
 			if(war.getAttackerFaction()==attackerFaction){
 				if(war.getTargetFaction()==targetFaction){
 					return war;
 				}
 			}
 		}
 		
 		return null;
 	}
 	
 	public static FWar getAsAttacker(Faction faction){
 		for(FWar war:FWars.i.get()){
 			if(war.getAttackerFaction()==faction){
 				return war;
 			}
 		}
 		return null;
 	}
 	
 	public static FWar getAsTarget(Faction faction){
 		for(FWar war:FWars.i.get()){
 			if(war.getTargetFaction()==faction){
 				return war;
 			}
 		}
 		return null;
 	}
 	
 	// Other static functions
 	
 	public static void removeFactionWars(Faction faction){
 		for(FWar war:FWars.i.get()){
 			if(war.getAttackerFaction()==faction || war.getTargetFaction() == faction){
 				war.remove();
 			}
 		}
 	}
 	
 	public static FWar getWar(Faction attacker, Faction target){
 		for(FWar war:FWars.i.get()){
 			if(war.getAttackerFaction()==attacker && war.getTargetFaction() == target){
 				return war;
 			}
 		}
 		
 		return null;
 	}
 	
 	
 	public static String convertMaterialDataToString(MaterialData mat){
 		String text = ""+mat.getItemTypeId()+":"+mat.getData()+"";
		P.p.log(text);
 		return text;
 	}
 	
 	public static MaterialData convertStringToMaterialData(String text){
 		String[] parts = text.split(":");
		P.p.log("!!"+parts[0]+":"+parts[1]+"!!");
 		MaterialData mat=new MaterialData(Integer.parseInt(parts[0]), (byte)Integer.parseInt(parts[1]));
 		return mat;
 	}
 	
 	
 }
