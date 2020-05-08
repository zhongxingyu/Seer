 package grokswell.hypermerchant;
 
 //import static java.lang.System.out;
 
 import java.util.ArrayList;
 
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import regalowl.hyperconomy.HyperAPI;
 import regalowl.hyperconomy.HyperConomy;
 import regalowl.hyperconomy.HyperObject;
 import regalowl.hyperconomy.HyperObjectAPI;
 import regalowl.hyperconomy.HyperPlayer;
 import regalowl.hyperconomy.LanguageFile;
 import regalowl.hyperconomy.TransactionResponse;
 
 public class ShopTransactions {
 	ArrayList<ArrayList<String>> pages = new ArrayList<ArrayList<String>>();
 	int items_count;
 	String shopname;
 	private Player player;
 	private HyperConomy hc;
 	private LanguageFile hc_lang;
 	//private EconomyManager ecoMan;
     //private HyperMerchantPlugin plugin;
     private ShopMenu shopmenu;
     HyperPlayer hp;
     //HyperEconomy hEcon;
 	HyperObjectAPI hoAPI = new HyperObjectAPI();
 	HyperAPI hyperAPI = new HyperAPI();
 	
 	public ShopTransactions(Player plyr, String sname, HyperMerchantPlugin plgn, ShopMenu sm) {
 		player=plyr;
 		//plugin=plgn;
 		shopname=sname;
 		shopmenu = sm;
 		hc = HyperConomy.hc;
 		//ecoMan = hc.getEconomyManager();
 		hp = hoAPI.getHyperPlayer(player.getName());
 	    //hEcon = hp.getHyperEconomy();
 		hc_lang = hc.getLanguageFile();
 
 	}
	//PLAYER SELLS ENCHANT TO SHOP
 	public boolean Sell(String enchant) {
 			if (player.getGameMode() == GameMode.CREATIVE) {
 				player.sendMessage(hc_lang.get("CANT_SELL_CREATIVE"));
 				return false;
 			}
 
 		HyperObject ho = hoAPI.getHyperObject(enchant,hyperAPI.getShopEconomy(shopname));
		if ((hyperAPI.getShop(shopname).isTradeable(ho))) {
 			TransactionResponse response = hoAPI.sell(player, ho, 1, hyperAPI.getShop(shopname));
 			response.sendMessages();
 			return true;
 		}
 		return false;
 	}
	//PLAYER SELLS ITEM TO SHOP
 	public boolean Sell(ItemStack item_stack) {
 			if (player.getGameMode() == GameMode.CREATIVE) {
 				player.sendMessage(hc_lang.get("CANT_SELL_CREATIVE"));
 				return false;
 			}
 		int item_amount = item_stack.getAmount();
 		HyperObject ho = hoAPI.getHyperObject(item_stack, hyperAPI.getShopEconomy(shopname), hyperAPI.getShop(shopname));
 		String item_name = ho.getDisplayName().toLowerCase();
 		//out.println(item_name);
 		ho = hoAPI.getHyperObject(item_name, hyperAPI.getShopEconomy(shopname), hyperAPI.getShop(shopname));
 		
		if (shopmenu.shopstock.items_in_stock.contains(item_name) && (hyperAPI.getShop(shopname).isTradeable(ho))) {
 			player.getInventory().addItem(item_stack);
 			TransactionResponse response = hoAPI.sell(player, ho, item_amount, hyperAPI.getShop(shopname));
 			response.sendMessages();
 			return true;
 			
 		}
 		return false;
 	}
 	
	//PLAYER BUYS ITEM FROM SHOP
 	public void Buy(String item, int qty) {
 		HyperObject ho = hoAPI.getHyperObject(item.replaceAll(" ", "_"), hyperAPI.getShopEconomy(shopname), hyperAPI.getShop(shopname));
 		if (!hp.hasBuyPermission(hyperAPI.getShop(shopname))) {
 			player.sendMessage("You cannot buy from this shop.");
 			return;
 		}
 		TransactionResponse response = hoAPI.buy(player, ho, qty, hyperAPI.getShop(shopname));
 		response.sendMessages();
 	}
 
 }
