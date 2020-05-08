 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class ChestSyncListener extends PluginListener {
 	private ChestSync cs;
 	private CSData csd;
 	private HashMap<Player, Boolean> addingLink = new HashMap<Player, Boolean>();
 	private HashMap<Player, String> LinkNames = new HashMap<Player, String>();
 	private ArrayList<Player> checkingInfo = new ArrayList<Player>();
 	private final String pre = "9[ChestSync] ";
 	private final String r = "c";
 	private final String a = "b";
 	private final PluginLoader PL = etc.getLoader();
 	
 	public ChestSyncListener(ChestSync cs, CSData csd){
 		this.cs = cs;
 		this.csd = csd;
 	}
 	
 	public boolean onOpenInventory(HookParametersOpenInventory openinventory) {
 		Inventory inv = openinventory.getInventory();
 		
 		if(inv instanceof Chest){
 			
 			if(csd.isChestSyncInv(inv)){
 				
 				Player player = openinventory.getPlayer();
 				
 				if(!csd.isOwner(player, inv)){
 					player.sendMessage(pre+r+"You do not own this link and cannot access it!");
 					return true;
 				}
 					
 				if(csd.isOpen(inv)){
 					player.sendMessage(pre+r+"This inventory is being accessed already!");
 					return true;
 				}
 				
 				if(csd.isOtherOpen(inv)){
 					player.sendMessage(pre+r+"The linked inventory is open!");
 					return true;
 				}
 				
 				csd.addOpenInv(inv);
 			}
 		}
 	    return false;
 	}
 	
 	public void onCloseInventory(HookParametersCloseInventory closeinventory){
 		Inventory inv = closeinventory.getInventory();
 		if(inv instanceof Chest){
 			csd.SyncChests(inv);
 			csd.removeOpenInv(inv);
 		}
 		
 	}
 	
 	public boolean onCommand(Player player, String[] args){
 		if((args[0].equalsIgnoreCase("/cs") || args[0].equalsIgnoreCase("/chestsync")) && (player.canUseCommand("/chestsync") || player.canUseCommand("/csadmin"))){
 			if(args.length > 1){
 				if(args.length == 3){
 					if(args[2].equalsIgnoreCase("create")){
 						if(!csd.LinkInUse(args[1]) && !isPendingLink(args[1])){
 							addingLink.put(player, false);
 							LinkNames.put(player, args[1]);
 							player.sendMessage(pre+a+"Left Click chest to add to link.");
 							return true;
 						}
 						else{
 							player.sendMessage(pre+r+"Link: '"+args[1]+"' is already taken. Please choose a different Link Name.");
 							player.sendMessage(pre+a+"or Use '/cs <linkname> add ' to complete the link.");
 							return true;
 						}
 					}
 					else if(args[2].equalsIgnoreCase("add")){
 						if(csd.LinkInUse(args[1])){
 							if(csd.isOwner(player, args[1])){
 								if(!csd.LinkComplete(args[1])){
 									addingLink.put(player, true);
 									LinkNames.put(player, args[1]);
 									player.sendMessage(pre+a+"Left Click chest to add to link.");
 									return true;
 								}
 								else{
 									player.sendMessage(pre+r+"Link: '"+args[1]+"' is already completed!");
 									return true;
 								}
 							}
 							else{
 								player.sendMessage(pre+r+"You do not have rights to Link: '"+args[1]+"'");
 								return true;
 							}
 						}
 						else{
 							player.sendMessage(pre+r+"Link: '"+args[1]+"' is not a vaild Link!");
 							player.sendMessage(pre+a+"Use '/cs <linkname> create ' to start the link.");
 							return true;
 						}
 					}
 				}
 				else if(args[1].equalsIgnoreCase("info")){
 					if(!checkingInfo.contains(player)){
 						checkingInfo.add(player);
 					}
 					player.sendMessage(pre+a+"Left click chest for INFO");
 					return true;
 				}
 				else{
 					player.sendMessage(pre+a+"Usage:");
 					player.sendMessage(pre+a+"/cs <linkname> create - Starts a link");
 					player.sendMessage(pre+a+"/cs <linkname> add - Adds to a link");
 					player.sendMessage(pre+a+"/cs info - Views Chest Info if Linked");
 				}
 			}
 			else{
 				player.sendMessage("7--------9 ChestSync by aDarkDiplomat 7--------");
 				player.sendMessage("7-------------- 6"+cs.version+"9 Installed 7--------------");
 				if(!cs.isLatest() && player.isAdmin()){
 					player.sendMessage("7-----c An update is availible! Latest = 2"+cs.CurrVer+" 7-----");
 				}
 				player.sendMessage("9Usage:");
 				player.sendMessage("9/cs <linkname> create b- Starts a link");
 				player.sendMessage("9/cs <linkname> add b- Adds to a link");
 				player.sendMessage("9/cs info b- Views Chest Info if Linked");
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean onBlockDestroy(Player player, Block block){
 		if(block.getType() == 54){
 			if(addingLink.containsKey(player)){
 				if(!isProtected(player, block)){
 					Inventory inv = (Inventory)player.getWorld().getOnlyComplexBlock(block);
 					if((inv != null) && (inv instanceof Chest)){
 						String LinkName = LinkNames.get(player);
 						if(!csd.isChestSyncInv(inv) && !addingLink.get(player)){
 							csd.createLink(player, inv, LinkName);
 							player.sendMessage(pre+a+"Use '/cs <linkname> add' to complete link.");
 							LinkNames.remove(player);
 							addingLink.remove(player);
 							return true;
 						}
 						else{
 							csd.addInv(player, inv, LinkName);
 							player.sendMessage(pre+a+"Link Completed!");
 							LinkNames.remove(player);
 							addingLink.remove(player);
 							return true;
 						}
 					}
 				}
 			}
 			else if(checkingInfo.contains(player)){
 				Inventory inv = (Inventory)player.getWorld().getOnlyComplexBlock(block);
 				if((inv != null) && (inv instanceof Chest)){
 					if(csd.isChestSyncInv(inv)){
 						if(csd.isOwner(player, inv)){
 							player.sendMessage(pre+a+"LinkName= 6"+csd.getLinkName(inv));
 							csd.getInfo(player, inv);
 						}
 						else{
 							player.sendMessage(pre+r+"You do not own this link and cannot view it's info!");
 						}
 					}
 					else{
 						player.sendMessage(pre+r+"Not a ChestSync Chest!");
 					}
 					checkingInfo.remove(player);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public boolean onBlockBreak(Player player, Block block) {
 		if(block.getType() == 54){
 			Inventory inv = (Inventory)player.getWorld().getOnlyComplexBlock(block);
 			if((inv != null) && (inv instanceof Chest)){
 				if(csd.isChestSyncInv(inv)){
 					if(!csd.isOwner(player, inv)){
 						return true;
 					}
 					else if(!isProtected(player, block)){
 						csd.clearInv(player, inv);
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
	public boolean onBlockPlace(Player player, Block block){
		if(block.getType() == 54){
			Inventory inv = (Inventory)player.getWorld().getOnlyComplexBlock(block);
			if((inv != null) && (inv instanceof Chest)){
				if(csd.isChestSyncInv(inv)){
					if(DChestCheck(player, block)){
 						player.sendMessage(pre+r+"Synced Chests cannot become double chests!");
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	private boolean isPendingLink(String LinkName){
 		for(Player key : LinkNames.keySet()){
 			String LN = LinkNames.get(key);
 			if(LN.equals(LinkName)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private boolean isProtected(Player player, Block block){
 		boolean protect = false, isSet = false;
 		if(PL.getPlugin("LWC") != null && PL.getPlugin("LWC").isEnabled()){
 			try{
 				protect = !(Boolean) PL.callCustomHook("LWC-AccessCheck", new Object[]{player, block});
 				isSet = true;
 			}catch(Exception E){ //API Failed/Non-Existent
 				protect = true;
 				isSet = false;
 			}
 		}
 		else if(PL.getPlugin("ChastityChest") != null && PL.getPlugin("ChastityChest").isEnabled()){
 			try{
 				protect = !(Boolean)PL.callCustomHook("ChastityChest-Check", new Object[]{player});
 				isSet = true;
 			}catch(Exception E){ //API Failed/Non-Existent
 				protect = false;
 				isSet = false;
 			}
 		}
 		else if(PL.getPlugin("ChestLock") != null && PL.getPlugin("ChestLock").isEnabled()){
 			try{
 				protect = !(Boolean)etc.getLoader().callCustomHook("ChestLock-API", new Object[] {player, block, "IS_ALLOWED"});
 			}catch(Exception E){ //API Failed/Non-Existent
 				protect = false;
 				isSet = false;
 			}
 		}
 		
 		if(PL.getPlugin("Realms") != null && PL.getPlugin("Realms").isEnabled() && !isSet){
 			try{
 				protect = !(Boolean)PL.callCustomHook("Realms-PermissionCheck", new Object[]{"INTERACT", player, block});
 				isSet = true;
 			}catch(Exception E){ //API Failed/Non-Existent
 				protect = false;
 			}
 		}
 		else if(PL.getPlugin("Cuboids2") != null && PL.getPlugin("Cuboids2").isEnabled() && !isSet){
 			try{
 				protect = !(Boolean)PL.callCustomHook("CuboidsAPI", new Object[]{"PLAYER_ALLOWED", player, block});
 			}catch(Exception E){ //API Failed/Non-Existent
 				protect = false;
 			}
 		}
 		return protect;
 	}
 	
	private boolean DChestCheck(Player player, Block block){
 		int bx = block.getX(), bz = block.getZ();
 		Block block2 = player.getWorld().getBlockAt(bx-1, block.getY(), bz);
 		if(!(block2.getType() == 54)){
 			block2 = player.getWorld().getBlockAt(bx+1, block.getY(), bz);
 		}
 		if(!(block2.getType() == 54)){
 			block2 = player.getWorld().getBlockAt(bx, block.getY(), bz-1);
 		}
 		if(!(block2.getType() == 54)){
 			block2 = player.getWorld().getBlockAt(bx, block.getY(), bz+1);
 		}
 		if(block2.getType() == 54){
			return true;
 		}
 			
		return false;
 	}
 }
