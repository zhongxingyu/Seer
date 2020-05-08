 package me.Josvth.Trade.Listeners;
 
 import me.Josvth.Trade.Trade;
 import me.Josvth.Trade.Exeptions.PlayerNotFoundExeption;
 import me.Josvth.Trade.Handlers.TradeHandler;
 import me.Josvth.Trade.Handlers.LanguageHandler.Message;
 import me.Josvth.Trade.TradingInventories.TradingInventory;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 
 public class InTradeListener implements Listener{
 
 	Trade plugin;
 
 	public InTradeListener(Trade instance){
 		plugin = instance;
 		plugin.getServer().getPluginManager().registerEvents(this,plugin);
 	}
 
 	@EventHandler
 	public void onPlayerAbandon(InventoryCloseEvent event){
 		Player player = (Player)event.getPlayer();
 		TradeHandler tradeHandler = plugin.activeTrades.get(player);
 		if(tradeHandler != null)
 			try {
 				tradeHandler.refuse(player);
 			} catch (PlayerNotFoundExeption e) {
 				e.printStackTrace();
 			}
 	}
 
 	@EventHandler
 	public void onTradeInventoryClick(InventoryClickEvent event){
 
 		Player player = (Player)event.getWhoClicked();
 		TradeHandler tradeHandler = plugin.activeTrades.get(player);
 
 		if(tradeHandler == null) return;
 
 		if(event.isShiftClick()){
 			event.setCancelled(true);
 			event.setResult(Result.DENY);
 		}
 
 		TradingInventory tradingInventory;
 
 		try {
 			tradingInventory = tradeHandler.getTradingInventory(player);
 
 			if(event.getRawSlot() > tradingInventory.getSize() - 1) return;
 
 			if(tradingInventory.isAcceptSlot(event.getRawSlot())){
 				if(tradeHandler.hasAccepted(player)){
 					tradeHandler.denyTrade(player);
				}else{
					tradeHandler.acceptTrade(player);
 				}
 				event.setCancelled(true);
				return;
 			}
 
 			if(tradingInventory.isRefuseSlot(event.getRawSlot())){
 				tradeHandler.refuse(player);
 				event.setCancelled(true);
				return;
 			}
			
 			if(!tradingInventory.canUseSlot(event.getRawSlot())){
 				plugin.languageHandler.sendMessage(player, Message.TRADE_CANNOT_USE_SLOT, "", "", "");
 				event.setCancelled(true);
				return;
 			}
 					
 		} catch (PlayerNotFoundExeption e) {
 			e.printStackTrace();
 			tradeHandler.stopTrading();
 			return;
 		}
 	}
 }
