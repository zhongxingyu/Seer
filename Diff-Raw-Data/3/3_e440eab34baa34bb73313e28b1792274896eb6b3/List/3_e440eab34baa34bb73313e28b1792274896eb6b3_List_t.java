 package nl.giantit.minecraft.GiantShop.API.GSW.Commands.Chat.Pickup;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import nl.giantit.minecraft.GiantShop.API.GSW.GSWAPI;
 import nl.giantit.minecraft.GiantShop.API.GSW.PickupQueue;
 import nl.giantit.minecraft.GiantShop.API.GSW.Queued;
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.Misc.Messages;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.perms.Permission;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Giant
  */
 public class List {
 	
 	public static void exec(Player player, String[] args) {
 		Messages mH = GiantShop.getPlugin().getMsgHandler();
 		Permission perms = GiantShop.getPlugin().getPermHandler().getEngine();
 		if(perms.has(player, "giantshop.api.web.pickup.list")) {
 			PickupQueue pQ = GSWAPI.getInstance().getPickupQueue();
 			if(pQ.inQueue(player.getName())) {
 				ArrayList<Queued> qList = pQ.getAll(player.getName());
 
 				if(qList.size() > 0) {
 					int perPage = config.Obtain().getInt("GiantShop.global.perPage");
 					int curPag = 0;
 
 					if(args.length >= 1) {
 						try{
 							curPag = Integer.parseInt(args[0]);
 						}catch(Exception e) {
 							curPag = 1;
 						}
 					}else
 						curPag = 1;
 
 					curPag = (curPag > 0) ? curPag : 1;
 					int pages = ((int)Math.ceil((double)qList.size() / (double)perPage) < 1) ? 1 : (int)Math.ceil((double)qList.size() / (double)perPage);
 					int start = (curPag * perPage) - perPage;
 
 					if(curPag > pages) {
 						HashMap<String, String> d = new HashMap<String, String>();
 						d.put("list", "Delivery list");
 						d.put("pages", String.valueOf(pages));
 						Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "pageOverMax", d));
 					}else{
 						HashMap<String, String> d = new HashMap<String, String>();
 						d.put("page", String.valueOf(curPag));
 						d.put("maxPages", String.valueOf(pages));
 						Heraut.say(player, mH.getMsg(Messages.msgType.MAIN, "PickupListPageHead", d));
						for(int i = start; i < (((start + perPage) > qList.size()) ? qList.size() : (start + perPage)); i++) {
							Queued q = qList.get(i);
 							d = new HashMap<String, String>();
 							d.put("transactionID", q.getTransactionID());
 							d.put("itemID", String.valueOf(q.getItemID()));
 							d.put("itemType", String.valueOf(q.getItemType()));
 							d.put("itemName", q.getItemName());
 							d.put("amount", String.valueOf(q.getAmount()));
 						
 							Heraut.say(player, mH.getMsg(Messages.msgType.MAIN, "PickupListEntry", d));
 						}
 					}
 				}else{
 					Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "emptyQueue"));
 				}
 			}else{
 				Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "emptyQueue"));
 			}
 		}else{
 			HashMap<String, String> data = new HashMap<String, String>();
 			data.put("command", "gsw pickup list");
 
 			Heraut.say(player, mH.getMsg(Messages.msgType.ERROR, "noPermissions", data));
 		}
 	}
 }
