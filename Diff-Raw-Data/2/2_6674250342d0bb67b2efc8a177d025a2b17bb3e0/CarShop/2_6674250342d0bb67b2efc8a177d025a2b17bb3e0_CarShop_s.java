 package net.stormdev.ucars.shops;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.stormdev.ucars.trade.Lang;
 import net.stormdev.ucars.trade.main;
 import net.stormdev.ucars.utils.Car;
 import net.stormdev.ucars.utils.CarGenerator;
 import net.stormdev.ucars.utils.IconMenu;
 import net.stormdev.ucars.utils.IconMenu.OptionClickEvent;
 import net.stormdev.ucars.utils.IconMenu.OptionClickEventHandler;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class CarShop {
 	private double value;
 	private IconMenu menu = null;
 	private main plugin;
 	public CarShop(main plugin){
 		this.plugin = plugin;
 		value = main.config.getDouble("general.carTrading.averageCarValue")*1.25;
 		int v = (int)value*100;
 		value = (double) v/100;
 		setupMenu(plugin);
 	}
 	
 	public void destroy(){
 		menu.destroy();
 	}
 
 	public IconMenu getShopWindow(){
 		if(menu == null){
 			setupMenu(plugin);
 		}
 		return menu;
 	}
 	
 	public void onClick(OptionClickEvent event){
 		event.setWillClose(false);
 		event.setWillDestroy(false);
 		
 		int slot = event.getPosition();
 		
 		if(slot == 4){
 			//Buy a car
 			event.setWillClose(true);
 			buyCar(event.getPlayer());
 		}
 		return;
 	}
 	
 	public void open(Player player){
 		getShopWindow().open(player);
 		return;
 	}
 	
 	public void buyCar(Player player){
 		if(main.economy == null){
 			main.plugin.setupEconomy();
 			if(main.economy == null){
 				player.sendMessage(main.colors.getError()+"No economy plugin found! Error!");
 				return;
 			}
 		}
 		double bal = main.economy.getBalance(player.getName());
 		double cost = value;
 		if(cost < 1){
 			return;
 		}
 		double rem = bal-cost;
 		if(rem<0){
 			player.sendMessage(main.colors.getError()+Lang.get("general.buy.notEnoughMoney"));
 			return;
 		}
 		main.economy.withdrawPlayer(player.getName(), cost);
 		
 		String currency = main.config.getString("general.carTrading.currencySign");
 		String msg = Lang.get("general.buy.success");
		msg = msg.replaceAll(Pattern.quote("%item%"), " a car");
 		msg = msg.replaceAll(Pattern.quote("%price%"), Matcher.quoteReplacement(currency+cost));
 		msg = msg.replaceAll(Pattern.quote("%balance%"), Matcher.quoteReplacement(currency+rem));
 		player.sendMessage(main.colors.getSuccess()+msg);
 		
 		//Give them the car
 		Car c = CarGenerator.gen();
 		ItemStack i = c.getItem();
 		player.getInventory().addItem(i);
 		
 		return;
 	}
 	
 	public void setupMenu(main plugin){
 		String currency = main.config.getString("general.carTrading.currencySign");
 		
 		this.menu = new IconMenu("Car Shop", 9, new OptionClickEventHandler(){
 
 			public void onOptionClick(OptionClickEvent event) {
 				onClick(event);
 				return;
 			}}, plugin);
 		List<String> info = new ArrayList<String>();
 		info.add(main.colors.getTitle()+"[Price:] "+main.colors.getInfo()+currency+value);
 		this.menu.setOption(4, new ItemStack(Material.MINECART), main.colors.getTitle()+"Buy Random Car", info);
 	}
 	
 }
