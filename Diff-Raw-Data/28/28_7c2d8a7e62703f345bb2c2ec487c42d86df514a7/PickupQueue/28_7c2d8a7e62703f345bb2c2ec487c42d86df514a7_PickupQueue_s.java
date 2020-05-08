 package nl.giantit.minecraft.GiantShop.API.GSW;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import nl.giantit.minecraft.GiantShop.API.conf;
 import nl.giantit.minecraft.GiantShop.GiantShop;
 import nl.giantit.minecraft.GiantShop.Misc.Heraut;
 import nl.giantit.minecraft.GiantShop.Misc.Messages;
 import nl.giantit.minecraft.GiantShop.core.Database.drivers.iDriver;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 /**
  *
  * @author Giant
  */
 public class PickupQueue {
 	
 	private GiantShop p;
 	private Messages mH;
 	
 	private HashMap<String, ArrayList<Queued>> queue;
 	
 	private void loadQueue() {
 		iDriver db = p.getDB().getEngine();
 		this.queue = new HashMap<String, ArrayList<Queued>>();
 		
 		HashMap<String, String> order = new HashMap<String, String>();
 		order.put("player", "ASC");
 		order.put("transactionID", "ASC");
 		
 		db.select("player", "transactionID", "itemID", "itemType", "amount").from("#__api_gsw_pickups").orderBy(order);
 		ArrayList<HashMap<String, String>> resSet = db.execQuery();
 		String lP = "";
 		ArrayList<Queued> qList = new ArrayList<Queued>();
 		for(HashMap<String, String> res : resSet) {
 			if(!res.get("player").equals(lP)) {
				this.queue.put(lP, qList);
 				lP = res.get("player");
 				qList = new ArrayList<Queued>();
 			}
 			
 			int id;
 			int type;
 			int amount;
 			try {
 				id = Integer.parseInt(res.get("itemid"));
 				type = Integer.parseInt(res.get("itemtype"));
 				amount = Integer.parseInt(res.get("amount"));
				qList.add(new Queued(id, type, amount, res.get("transactionid")));
 			}catch(NumberFormatException e) {
 				p.getLogger().warning("[GSWAPI][PickupQueue] Transaction " + res.get("transactionid") + " for player " + lP + " is corrupt!");
 				continue;
 			}
 		}
 		
 	}
 	
 	public PickupQueue(GiantShop p) {
 		this.p = p;
 		this.mH = p.getMsgHandler();
 		
 		this.loadQueue();
 	}
 	
 	public void addToQueue(String transactionID, String player, int amount, int id, int type) {
 		// Add purchase to database and take money
 		iDriver db = p.getDB().getEngine();
 		ArrayList<String> fields = new ArrayList<String>() {
 			{
 				add("transactionID");
 				add("player");
 				add("amount");
 				add("itemID");
 				add("itemType");
 			}
 		};
 		
 		HashMap<Integer, HashMap<String, String>> values = new HashMap<Integer, HashMap<String, String>>();
 		int i = 0;
 		
 		for(String field : fields) {
 			HashMap<String, String> temp = new HashMap<String, String>();
 			if(field.equals("transactionID")) {
 				temp.put("data", transactionID);
 				values.put(i, temp);
 			}else if(field.equals("player")) {
 				temp.put("data", player);
 				values.put(i, temp);
 			}else if(field.equals("amount")) {
 				temp.put("kind", "INT");
 				temp.put("data", "" + amount);
 				values.put(i, temp);
 			}else if(field.equals("itemID")) {
 				temp.put("kind", "INT");
 				temp.put("data", "" + id);
 				values.put(i, temp);
 			}else if(field.equals("itemType")) {
 				temp.put("kind", "INT");
 				temp.put("data", "" + type);
 				values.put(i, temp);
 			}
 			
 			i++;
 		}
 		
 		// Insert transaction into database as ready pickup!
 		db.insert("#__api_gsw_pickups", fields, values).updateQuery();
 		
 		ArrayList<Queued> q;
 		if(this.queue.containsKey(player)) {
 			q = this.queue.remove(player);
 		}else{
 			q = new ArrayList<Queued>();
 		}
 		
 		Queued Q = new Queued(id, type, amount, transactionID);
 		q.add(Q);
 		this.queue.put(player, q);
 		
 		this.stalkUser(player);
 	}
 	
 	// Seperate method because delivery should not call remove from Queue.
 	public void removeFromDB(String player, String transactionID) {
 		iDriver db = p.getDB().getEngine();
 		HashMap<String, String> where = new HashMap<String, String>();
 		where.put("player", player);
 		where.put("transactionID", transactionID);
 		db.delete("#__api_gsw_pickups").where(where).updateQuery();
 	}
 	
 	public void removeFromQueue(String player, String transactionID) {
 		if(this.inQueue(player)) {
 			ArrayList<Queued> qList = this.queue.get(player);
 			Iterator<Queued> qItr = qList.iterator();
 			while(qItr.hasNext()) {
 				Queued q = qItr.next();
 				if(q.getTransactionID().equals(transactionID)) {
 					this.removeFromDB(player, transactionID);
 					qItr.remove();
 					break;
 				}
 			}
 		}
 	}
 	
 	public boolean inQueue(String player) {
 		return this.queue.containsKey(player);
 	}
 	
 	public void stalkUser(String player) {
 		Player pl = p.getSrvr().getPlayerExact(player);
 		if(null != pl) {
 			// Player is online! Bug him about his new purchase now!
 			conf c = GSWAPI.getInstance().getConfig();
 			if(c.getBoolean("GiantShopWeb.Items.PickupsRequireCommand")) {
 				Heraut.say(pl, mH.getMsg(Messages.msgType.MAIN, "itemWaitingForPickup"));
 				Heraut.say(pl, mH.getMsg(Messages.msgType.MAIN, "itemPickupHelp"));
 			}else{
 				this.deliver(pl);
 			}
 		}
 	}
 	
 	public void deliver(Player player) {
 		ArrayList<Queued> qList = this.queue.remove(player.getName());
 		for(Queued q : qList) {
 			this.removeFromDB(player.getName(), q.getTransactionID());
 			this.deliver(player, q);
 		}
 		
 		Heraut.say(player, mH.getMsg(Messages.msgType.MAIN, "itemsDelivered"));
 	}
 	
 	public void deliver(Player pl, Queued q) {
 		Inventory inv = pl.getInventory();
 		HashMap<String, String> data = new HashMap<String, String>();
 		data.put("transactionID", q.getTransactionID());
 		data.put("itemID", String.valueOf(q.getItemID()));
 		data.put("itemType", String.valueOf(q.getItemType()));
 		data.put("itemName", q.getItemName());
 		data.put("amount", String.valueOf(q.getAmount()));
 
 		Heraut.say(pl, mH.getMsg(Messages.msgType.MAIN, "itemDelivery", data));
 
 		ItemStack iStack;
 		if(q.getItemType() != -1 && q.getItemType() != 0) {
 			if(q.getItemID() != 373)
 				iStack = new MaterialData(q.getItemID(), (byte) ((int) q.getItemType())).toItemStack(q.getAmount());
 			else
 				iStack = new ItemStack(q.getItemID(), q.getAmount(), (short) ((int) q.getItemType()));
 		}else{
 			iStack = new ItemStack(q.getItemID(), q.getAmount());
 		}
 
 		HashMap<Integer, ItemStack> left = inv.addItem(iStack);
 		if(!left.isEmpty()) {
 			Heraut.say(pl, mH.getMsg(Messages.msgType.ERROR, "infFull"));
 			for(Map.Entry<Integer, ItemStack> stack : left.entrySet()) {
 				pl.getWorld().dropItem(pl.getLocation(), stack.getValue());
 			}
 		}
 	}
 }
