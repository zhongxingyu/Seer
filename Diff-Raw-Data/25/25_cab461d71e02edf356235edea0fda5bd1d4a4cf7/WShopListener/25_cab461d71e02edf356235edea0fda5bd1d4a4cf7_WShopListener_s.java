 /**
  * 
  * @author Brian McCarthy
  *
  */
 
 public class WShopListener extends PluginListener{
 	
 	public boolean onCommand(Player p, String[] s){
		if (s[0].equalsIgnoreCase("/ws")){
 			try {
 				if (s[1].equalsIgnoreCase("sell") && p.canUseCommand("/ws_sell")){
 					Item inHand = p.getItemStackInHand();
 					if (inHand == null){
 						p.sendMessage(WShop.PRE + "Please hold an item to sell!");
 						return true;
 					}
 					WShopActions.sellItem(p, inHand);
 					return true;
 				} else if (s[1].equalsIgnoreCase("buy") && p.canUseCommand("/ws_buy")){
 					Item item = WShopActions.phraseItem(etc.combineSplit(2, s, " "));
 					boolean success = true;
 					if (item.getItemId() == -1 || item.getAmount() == -1 || item.getDamage() == -1){
 						success = false;
 					}// end if
 					if (success){
 						WShopActions.buyItem(p, item);
 						return true;
 					} else {
 						p.sendMessage(WShop.PRE + "Could not buy that.");
 						p.sendMessage(WShop.PRE + "Make sure you entered your values correctly.");
 						return true;
 					}// end if (success)
 				} else if (s[1].equalsIgnoreCase("info") && p.canUseCommand("/ws_info")){
 					WShopActions.sendInfo(p, WShopActions.phraseItem(etc.combineSplit(2, s, " ")));
 					return true;
 				}
 			} catch (Exception e){
 				WShopActions.sendHelp(p);
 				return true;
 			}// end try
 		}// end if (/ws)
 		return false;
 	}// end onCommand
 	
 	public boolean onBlockRightClick(Player p, Block b, Item inHand){
 		//63, 68
 		if (b.getType() == 63 || b.getType() == 68){// SignPost and WallSign
 			Sign sign = (Sign) b.getWorld().getComplexBlock(b);
 			if (sign.getText(0).equalsIgnoreCase("[WShop]")){
 				if (sign.getText(1).equalsIgnoreCase("sell")){
 					WShopActions.sellItem(p, inHand);
 				} else if (sign.getText(1).equalsIgnoreCase("buy")){
					Item item = WShopActions.phraseItem(sign.getText(2).replace("_", " ").replace(":", " "));
 					WShopActions.buyItem(p, item);
 				} else if (sign.getText(1).equalsIgnoreCase("info")){
 					Item item;
 					if (sign.getText(2).equalsIgnoreCase("this")){
 						item = inHand;
 					} else {
 						item = WShopActions.phraseItem(sign.getText(2).replace("_", " ").replace(":", " "));
 					}
 					WShopActions.sendInfo(p, item);
 				}
 			}// end if ([WShop])
 		}// end if (63, 68)
 		return false;
 	}
 }// end WShopListener
