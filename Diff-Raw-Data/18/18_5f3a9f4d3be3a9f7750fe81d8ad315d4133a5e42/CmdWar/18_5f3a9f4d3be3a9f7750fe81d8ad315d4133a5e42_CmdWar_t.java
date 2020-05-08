 package com.massivecraft.factions.cmd;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryView;
 import org.bukkit.material.MaterialData;
 
 import com.massivecraft.factions.Conf;
 import com.massivecraft.factions.FWar;
 import com.massivecraft.factions.Faction;
 import com.massivecraft.factions.integration.Econ;
 import com.massivecraft.factions.struct.Permission;
 import com.massivecraft.factions.struct.Relation;
 
 public class CmdWar extends FCommand{
 	//Befehl um einen Krieg zu starten mit Forderungen
 	
 	public CmdWar()
 	{
 		super();
 		this.aliases.add("war");
 		
 		this.requiredArgs.add("target");
 		this.optionalArgs.put("cmd", "");
 		this.optionalArgs.put("value", "");
 		
 		this.permission = Permission.MOD.node;
 		this.disableOnLock = true;
 		
 		senderMustBePlayer = true;
 		senderMustBeMember = true;
 		senderMustBeModerator = true;
 		senderMustBeAdmin = false;
 	}
 	
 	@Override
 	public void perform() {
 		if(Conf.fwarEnabled){
 			Faction argFaction=this.argAsFaction(0);
 			String argCmd = this.argAsString(1);
 			int argValue=0;
 			if(this.argAsString(2)!=null){
 				argValue = Integer.parseInt(this.argAsString(2));
 			}
 			
 			
 			if(argFaction!=null){
 				if(argFaction.isNormal()){
 					boolean isAttacker = FWar.get(fme.getFaction(),argFaction)!=null;
 					boolean isTarget = FWar.get(argFaction,fme.getFaction())!=null;
 					
 					if(!isAttacker && !isTarget){ // If the faction doesn't have a war with the other faction
 						if(argCmd==null){ // Check that the cmd parameter is empty
 							if(argFaction.getRelationTo(fme.getFaction())!=Relation.ALLY){
 								if(argFaction.factionsAfterWarProtection.containsKey(fme.getFaction())==false){
 									/* Start the demands */
 									new FWar(fme.getFaction(),argFaction);
 									
 									/* Send help messages */
 									me.sendMessage(ChatColor.GOLD+"Du bist dabei einen Krieg gegen die Fraktion "+ChatColor.GREEN+argFaction.getTag()+ChatColor.GOLD+" zu starten!");
 									
 									me.sendMessage(ChatColor.GOLD+"Folgende Befehle brauchst Du:");
 									me.sendMessage(ChatColor.GREEN+" /f war "+argFaction.getTag()+" additems"+ChatColor.GOLD+" - Fge Items zu den Forderungen hinzu");
 									if(Conf.econEnabled){
 										me.sendMessage(ChatColor.GREEN+" /f war "+argFaction.getTag()+" addmoney  [money]"+ChatColor.GOLD+" - Fge Geld zu den Forderungen hinzu");
 									}
 									me.sendMessage(ChatColor.GREEN+" /f war "+argFaction.getTag()+" cancel"+ChatColor.GOLD+" - Bricht die Forderungen/Krieg ab");
 									me.sendMessage(ChatColor.GREEN+" /f war "+argFaction.getTag()+" confirm"+ChatColor.GOLD+" - Besttigt die Forderungen und informiert die gegnerische Fraktion");
 									
 									me.sendMessage(ChatColor.GOLD+"Alle Items die Du zu den Forderungen hinzufgst werden eurer Fraktion abgezogen. Nach dem Krieg erhaltet ihr diese zurck.");
 								}else{
 									me.sendMessage(ChatColor.RED+"Ihr msst nach einem durch Forderungen beendeten Krieg "+Conf.fwarDaysAfterWarProtection+" Tage warten bis ihr sie wieder angreifen knnt!");
 								}
 							} else {
 								me.sendMessage(ChatColor.RED+"Ihr seid mit "+ChatColor.GOLD+this.argAsString(0)+ChatColor.RED+" verbndet!");
 							}
 						} else {
 							me.sendMessage(ChatColor.RED+"Du hast noch keine Forderungen gegen "+ChatColor.GOLD+this.argAsString(0)+ChatColor.RED+" gestartet!");
 						}
 					}
 					
 					if(isAttacker){ // If the faction is the attacker
 						FWar fwar = FWar.get(fme.getFaction(),argFaction);
 						
 						if(!fwar.isStarted){ //If the war isn't started yet
 							/* Check the commands */
 							if(argCmd!=null){
 								if(argCmd.equalsIgnoreCase("additems")){
 									Inventory inv=Bukkit.createInventory(me, 54);
 									InventoryView view=me.openInventory(inv);
 									fwar.addTempInventory(view);
 								}
 								
 								else if(argCmd.equalsIgnoreCase("addmoney")){
 									if(Conf.econEnabled){
 										if(argValue>0){
 											if(Econ.modifyMoney(fme.getFaction(), -argValue, "", "for add money to a demand")){
 												fwar.money=fwar.money+argValue;
 											}
 										} else {
 											me.sendMessage(ChatColor.RED+"Gebe einen Wert hher als 0 an!");
 										}
 									} else {
 										me.sendMessage(ChatColor.RED+"Economy support ist auf diesem Server nicht aktiviert!");
 									}
 								}
 								
 								else if(argCmd.equalsIgnoreCase("cancel")){
 									me.sendMessage(ChatColor.GREEN+"Du hast die Forderungen erfolgreich abgebrochen. Alle Items wurden eurem Itemkonto hinzugefgt.");
 									if(Conf.econEnabled){
 										Econ.modifyMoney(fme.getFaction(), fwar.money, "for cancelling a war", "");
 									}
 									
 									fwar.remove();
 									
 									// TODO: Fge Items zu dem Itemkonte hinzu
 								} 
 								
 								else if(argCmd.equalsIgnoreCase("confirm")){
 									fwar.startWar();
 								}
 								
 								
 								
 								else{
 									me.sendMessage(ChatColor.RED+"Dieser Befehl existiert nicht!");
 								}
 								
 							} else {
 								me.sendMessage(ChatColor.RED+"Du hast bereits eine Forderung gestartet!");
 								me.sendMessage(ChatColor.RED+"Die Kriegserklrung gegen: "+fwar.getTargetFaction().getTag()+" startet in "+fwar.getTimeToWar());
 							}
 							
 							
 							
 						
 							
 							
 						}
 						
 						if(fwar.isStarted){ //If the war isn't started yet
 							/* Check the commands */
 							if(argCmd!=null){
 								
 								if(argCmd.equalsIgnoreCase("cancelwar")){
 									if(Conf.econEnabled){
 										Econ.modifyMoney(fme.getFaction(), fwar.money, "for cancelling a war", "");
 									}
 									
 									fwar.remove();
 									
 									
 									if(Conf.econEnabled){
 										Econ.modifyMoney(fme.getFaction(), fwar.moneyFromTarget, "for cancelling the pay for a war", "");
 									}
 									
 									for(String matString:fwar.itemsFromTarget.keySet()){
 										MaterialData mat=FWar.convertStringToMaterialData(matString);
 										Integer args;
 										if(fwar.getTargetFaction().factionInventory.get(matString)==null){
 											fwar.getTargetFaction().factionInventory.put(FWar.convertMaterialDataToString(mat), fwar.itemsFromTarget.get(matString));
 										}else{
 											args=fwar.itemsFromTarget.get(matString);
 											Integer argsOLD=fwar.getTargetFaction().factionInventory.get(matString);
 											args=args+ argsOLD;
 											fwar.getTargetFaction().factionInventory.put(FWar.convertMaterialDataToString(mat), args);
 											
 										}
 									}
 									
 									
 								}
 								
							}else{
								me.sendMessage("Zeit zum Start des Krieges: "+fwar.getTimeToWar());
 							}
 						}
 						
 					}
 					
 					
 					if(isTarget){ // If the faction is the attacker
 						FWar fwar = FWar.get(argFaction,fme.getFaction());
 						
 						if(fwar.isStarted){ //If the war isn't started yet
 							/* Check the commands */
 							if(argCmd!=null){
 								if(argCmd.equalsIgnoreCase("addpayitems")){
 									Inventory inv=Bukkit.createInventory(me, 54);
 									InventoryView view=me.openInventory(inv);
 									fwar.addTempInventoryFromTarget(view);
 								}
 								
 								else if(argCmd.equalsIgnoreCase("addpaymoney")){
 									if(Conf.econEnabled){
 										if(argValue>0){
 											if(Econ.modifyMoney(fme.getFaction(), -argValue, "", "for add money to pay a demand")){
 												fwar.moneyFromTarget=fwar.moneyFromTarget+argValue;
 											}
 										} else {
 											me.sendMessage(ChatColor.RED+"Gebe einen Wert hher als 0 an!");
 										}
 									} else {
 										me.sendMessage(ChatColor.RED+"Economy support ist auf diesem Server nicht aktiviert!");
 									}
 								}
 								
 								else if(argCmd.equalsIgnoreCase("cancelpay")){
 									me.sendMessage(ChatColor.GREEN+"Du hast die Zahlung der Forderungen erfolgreich abgebrochen. Alle Items wurden eurem Itemkonto hinzugefgt.");
 									if(Conf.econEnabled){
 										Econ.modifyMoney(fme.getFaction(), fwar.moneyFromTarget, "for cancelling the pay for a war", "");
 									}
 									
 									for(String matString:fwar.itemsFromTarget.keySet()){
 										MaterialData mat=FWar.convertStringToMaterialData(matString);
 										Integer args;
 										if(fwar.getTargetFaction().factionInventory.get(matString)==null){
 											fwar.getTargetFaction().factionInventory.put(FWar.convertMaterialDataToString(mat), fwar.itemsFromTarget.get(matString));
 										}else{
 											args=fwar.itemsFromTarget.get(matString);
 											Integer argsOLD=fwar.getTargetFaction().factionInventory.get(matString);
 											args=args+ argsOLD;
 											fwar.getTargetFaction().factionInventory.put(FWar.convertMaterialDataToString(mat), args);
 											
 										}
 									}
 									
 									// TODO: Fge Items zu dem Itemkonte hinzu
 								} 
 								
 								else if(argCmd.equalsIgnoreCase("showtopay")){
 									me.sendMessage(ChatColor.RED+"Zu Entrichten: "+fwar.getDemandsAsString());
 								}
 								
 								else if(argCmd.equalsIgnoreCase("confirmpay")){
 									boolean passed=true;
 									for(String matString:fwar.items.keySet()){
 										MaterialData mat=FWar.convertStringToMaterialData(matString);
 										boolean found=false;
 										for(String matFromTargetString:fwar.itemsFromTarget.keySet()){
 											MaterialData matFromTarget=FWar.convertStringToMaterialData(matFromTargetString);
 											if((matFromTarget.equals(mat))){
 												found=true;
 												
 												Integer args=fwar.items.get(matString);
 												Integer argsFromTarget=fwar.itemsFromTarget.get(matString);
 												
 												
 												if(argsFromTarget<args){
 													passed=false;
 													me.sendMessage(ChatColor.RED+"Es fehlen noch "+(argsFromTarget-args)+" "+mat.toString()+" um die Forderungen zu erfllen!");
 												}
 												
 											}
 										}
 										if(found==false){
 											passed=false;
 											Integer args=fwar.items.get(matString);
 											me.sendMessage(ChatColor.RED+"Es fehlen noch "+(args)+" "+mat.toString()+" um die Forderungen zu erfllen!");
 										}
 									}
 									
 									if(Conf.econEnabled){
 										if(fwar.money>fwar.moneyFromTarget){
 											me.sendMessage(ChatColor.RED+"Es fehlen noch "+(fwar.money-fwar.moneyFromTarget)+" Heronen um die Forderungen zu erfllen!");
 										}
 									}
 									
 									
 									if(passed==true){
 										me.sendMessage(ChatColor.RED+"Forderungen wurden Erfllt!");
 										
 										for(String matString:fwar.itemsFromTarget.keySet()){
 											MaterialData mat=FWar.convertStringToMaterialData(matString);
 											Integer args;
 											if(fwar.getAttackerFaction().factionInventory.get(matString)==null){
 												fwar.getAttackerFaction().factionInventory.put(FWar.convertMaterialDataToString(mat), fwar.itemsFromTarget.get(matString));
 											}else{
 												args=fwar.itemsFromTarget.get(matString);
 												Integer argsOLD=fwar.getAttackerFaction().factionInventory.get(matString);
 												args=args+ argsOLD;
 												fwar.getAttackerFaction().factionInventory.put(FWar.convertMaterialDataToString(mat), args);
 												
 											}
 										}
 										
 										
 										if(Conf.econEnabled){
 											Econ.modifyMoney(fwar.getAttackerFaction(), fwar.money, "for wining a war on demand", "");
 											Econ.modifyMoney(fwar.getAttackerFaction(), fwar.moneyFromTarget, "for wining a war on demand", "");
 										}
 										
 										
 										fwar.getTargetFaction().factionsAfterWarProtection.put(fwar.getAttackerFaction(), System.currentTimeMillis());
 										
 										fwar.remove();
 										
 										fwar.getAttackerFaction().sendMessage("Der Krieg gegen "+fwar.getTargetFaction()+" wurde Beendet durch das Zahlen der Vorderungen!!");
 										fwar.getTargetFaction().sendMessage("Der Krieg gegen "+fwar.getAttackerFaction()+" wurde Beendet durch das Zahlen der Vorderungen!!");
 									}
 								}
 								
 								else{
 									me.sendMessage(ChatColor.RED+"Dieser Befehl existiert nicht!");
 								}
 								
 							}
 							
 							
 							
 						} else {
 							me.sendMessage(ChatColor.RED+"Die Fraktion "+fwar.getAttackerFaction().getTag()+" ist gerade dabei einen Krieg gegen euch zu starten!");
 						}
 						
 					}
 					
 					
 				} else {
 					me.sendMessage(ChatColor.RED+"Du kannst keinen Krieg gegen "+ChatColor.GOLD+argFaction.getTag()+ChatColor.RED+" starten!");
 				}
 			}
 		}
 	}
 		/*
 		else{
 			//Money
 			if(Conf.econEnabled){
 				if(this.argIsSet(1)){
 					int money=this.argAsInt(1);
 					
 					if(money>=0){
 						int newMoney=money-fme.demandMoney;
 						
 						if(newMoney<=0){
 							Econ.deposit(myFaction.getAccountId(), -newMoney);
 							fme.demandMoney=money;
 							me.sendMessage(ChatColor.GOLD+"Der Forderungsbetrag wurde auf "+ChatColor.GREEN+fme.demandMoney+ChatColor.GOLD+" gesetzt");
 						}
 						else{
 							if(Econ.getBalance(myFaction.getAccountId())<newMoney){
 								me.sendMessage(ChatColor.RED+"Deine Fraktion hat zuwenig Geld um eine solche Forderung stellen zu knnen!");
 							}
 							else{
 								Econ.withdraw(myFaction.getAccountId(),newMoney);
 								fme.demandMoney=money;
 								me.sendMessage(ChatColor.GOLD+"Der Forderungsbetrag wurde auf "+ChatColor.GREEN+fme.demandMoney+ChatColor.GOLD+" gesetzt");
 							}
 						}
 					}
 					else{
 						me.sendMessage(ChatColor.RED+"Der Betrag muss gleich oder mehr als 0 sein!");
 					}
 					return;
 				}
 			}
 			//Beginn War
 			if(this.argAsFaction(0)==fme.demandFaction){
 				new FWar(myFaction, fme.demandFaction, fme.demandItems, fme.demandMoney);
 				fme.demandFaction=null;
 				fme.demandItems.clear();
 				fme.demandMoney=0;
 				fme.isMakingDemand=false;
 			}else{
 				me.sendMessage(ChatColor.RED+"Du bist momentan daran einen Krieg gegen "+ChatColor.GOLD+fme.demandFaction.getTag()+ChatColor.RED+" zu starten, bitte schliess diese Aktion zuerst ab!");
 			}
 			
 		}
 	}
 	*/
 }
