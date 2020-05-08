 
 public class WShopActions {
 	public static PropertiesFile PRICES = new PropertiesFile(WShop.CONFIG + "prices.txt");
 	public static PropertiesFile ITEMS = new PropertiesFile(WShop.CONFIG + "items.txt");
 	
 	public static void sendHelp(Player p){
 		p.sendMessage(WShop.PRE + "=== WShop Help ===");
 		p.sendMessage(WShop.PRE + "");
 		p.sendMessage(WShop.PRE + "/ws sell - Sell held item.");
 		p.sendMessage(WShop.PRE + "/ws buy <amount> <id | name> [damage]");
 		p.sendMessage(WShop.PRE + "");
 		p.sendMessage(WShop.PRE + "=== WShop Help ===");
 	}// end sendHelp
 	
 	/**
 	 * 
 	 * @param p - The player trying to sell.
 	 * @param item - The item to sell, commonly the players help item. 
 	 */
 	public static void sellItem(Player p, Item item){
 		String key = item.getItemId() + ":" + item.getDamage();
 		double price = PRICES.getDouble(key + "_sell", -1);
 		if (price == -1){
 			p.sendMessage(WShop.PRE + "The server does not buy that item at this time.");
 			return;
 		}
 		int amount = ITEMS.getInt(key, 0);
 		p.getInventory().removeItem(item);
 		ITEMS.setInt(key, amount + item.getAmount());
 		payPlayer(p, price);
 		return;
 	}// end sellItem
 	
 	/**
 	 * 
 	 * @param p - The player buying an item.
 	 * @param item - The item the player wants to buy.
 	 */
 	public static void buyItem(Player p, Item item){
 		String key = item.getItemId() + ":" + item.getDamage();
 		int amount = ITEMS.getInt(key, 0);
 		if (amount == -1){
 			p.sendMessage(WShop.PRE + "The server does not sell that item at this time.");
 			return;
 		}
 		if (amount < item.getAmount()){
 			p.sendMessage(WShop.PRE + "The server does not have that much of " + key);
 			return;
 		}//end if
 		double price = PRICES.getDouble(key + "_buy", -1);
		if (price == -1){
			p.sendMessage(WShop.PRE + "The server does not sell that item at this time." + key);
			return;
		}
 		if (canPay(p, price)){
 			payPlayer(p, -price);// Make sure we charge them here, not pay them. Negate the price.
 			p.giveItem(item);
			ITEMS.setInt(key, amount - item.getAmount());
 			p.sendMessage(WShop.PRE + "Bought an item. ID:" + item.getItemId() + ", Damage:" + item.getDamage() + ", Amount:" + item.getAmount() + ", Cost:" + price);
 		} else {
 			p.sendMessage(WShop.PRE + "You do not have enough money to buy " + key);
 			return;
 		}
 	}// end buyItem
 	
 	/**
 	 * 
 	 * @param p - Player we're paying.
 	 * @param price - How much to pay the player. Can be negative.
 	 */
 	public static void payPlayer(Player p, double price){
 		WShop.LOG.info(WShop.SPRE + "Paid player " + p.getName() + price);
 		etc.getLoader().callCustomHook("dCBalance", new Object[]{"Account-Deposit", p.getName(), price});
 	}// end payPlayer
 	
 	/**
 	 * 
 	 * @param p - Player whose money we're checking.
 	 * @param amount - Amount of money the player needs.
 	 * @return - True if the player can afford it.
 	 */
 	public static boolean canPay(Player p, double amount){
 		if (amount == -1){
 			return false;
 		}
 		double currMoney = (Double) etc.getLoader().callCustomHook("dCBalance", new Object[]{"Account-Balance", p.getName()});
 		if (amount <= currMoney){
 			return true;
 		}
 		return false;
 	}// end canPay
 	
 	/**
 	 * Make an item out of a string.
 	 * @param s - The string to get the data for the item.
 	 */
 	public static Item phraseItem(String string){
 		String[] s = string.split(" ");
 		Item item = new Item();
 		int id = -1;
 		try {
 			id = Integer.parseInt(s[1]);
 		} catch (Exception e){
 			try {
 				for (int i = 0; i < Item.Type.values().length; i++){
 					if (Item.Type.fromId(i).name().equalsIgnoreCase(s[1])){
 						id = Item.Type.fromId(i).getId();
 						break;
 					}
 				}
 				//id = Integer.parseInt(data);
 				WShop.LOG.info(WShop.SPRE + "ID2");
 			} catch (Exception e2){
 				id = -1;
 			}// end try
 		}// end try
 
 		int amount = -1;
 		try {
 			amount = Integer.parseInt(s[0]);
 		} catch (Exception e){
 			amount = -1;
 		}// end try
 
 		int damage = -1;
 		try {
 			damage = Integer.parseInt(s[2]);
 		} catch (Exception e){
 			damage = 0; 
 		}
 		item.setItemId(id);
 		item.setAmount(amount);
 		item.setDamage(damage);
 		return item;
 	}// end phraseItem
 	
 	public static void sendInfo(Player p, Item item){
 		String key = item.getItemId() + ":" + item.getDamage();
 		p.sendMessage(WShop.PRE + key);
 		p.sendMessage(WShop.PRE + "Price (sell): " + PRICES.getDouble(key + "_sell", -1));
 		p.sendMessage(WShop.PRE + "Price (buy): " + PRICES.getDouble(key + "_buy", -1));
 		p.sendMessage(WShop.PRE + "Amount: " + ITEMS.getInt(key, 0));
 	}
 }// end WShopActions
