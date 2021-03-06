 package net.dtl.citizens.trader.types;
 
 import java.text.DecimalFormat;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.inventory.ItemStack;
 
 import net.citizensnpcs.api.npc.NPC;
 import net.dtl.citizens.trader.CitizensTrader;
 import net.dtl.citizens.trader.TraderCharacterTrait;
 import net.dtl.citizens.trader.TraderCharacterTrait.EcoNpcType;
 import net.dtl.citizens.trader.events.TraderTransactionEvent;
 import net.dtl.citizens.trader.events.TraderTransactionEvent.TransactionResult;
 import net.dtl.citizens.trader.managers.LocaleManager;
 import net.dtl.citizens.trader.objects.NBTTagEditor;
 import net.dtl.citizens.trader.objects.StockItem;
 import net.dtl.citizens.trader.objects.TransactionPattern;
 import net.dtl.citizens.trader.parts.TraderStockPart;
 
 public class ServerTrader extends Trader {
 
 	private TransactionPattern pattern = getStock().getPattern();
 	private LocaleManager locale = CitizensTrader.getLocaleManager();
 	
 	public ServerTrader(TraderCharacterTrait trait, NPC npc, Player player) {
 		super(trait, npc, player);
 	//	pattern = patterns.getPattern(this.getTraderConfig().getPattern());
 	}
 
 	@Override
 	public void simpleMode(InventoryClickEvent event) 
 	{
 		DecimalFormat f = new DecimalFormat("#.##");
 		int slot = event.getSlot();
 		
 		if ( slot < 0 )
 		{
 			event.setCursor(null);
 			return;
 		}
 		
 		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
 		
 		if ( top ) 
 		{
 
 			if ( event.isShiftClick() )
 			{
 				((Player)event.getWhoClicked()).sendMessage(ChatColor.GOLD + "You can't shift click this, Sorry");
 				event.setCancelled(true);
 				return;
 			}
 			
 			if ( isManagementSlot(slot, 1) ) 
 			{
 				
 				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(7)) ) 
 				{
 					switchInventory(TraderStatus.SELL);		
 				}
 				else 
 				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(0)) )
 				{
 					if ( !permissionsManager.has(player, "dtl.trader.options.sell") )
 					{
 						player.sendMessage( localeManager.getLocaleString("lacks-permissions-xxx","object:tab") );
 					}
 					else
 					{
 						switchInventory(TraderStatus.SELL);	
 						//TODO add debug mode
 					//	p.sendMessage( locale.getLocaleString("xxx-transaction-tab","transaction:sell") );
 					}
 				} 
 				else 
 				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(1)) ) 
 				{
 					if ( !permissionsManager.has(player, "dtl.trader.options.buy") )
 					{
 						player.sendMessage( localeManager.getLocaleString("lacks-permissions-xxx","object:tab") );
 					}
 					else
 					{
 						switchInventory(TraderStatus.BUY);	
 						//TODO add debug mode
 					//	p.sendMessage( locale.getLocaleString("xxx-transaction-tab","transaction:buy") );
 					}	
 				}
 			} 
 			else
 			if ( equalsTraderStatus(TraderStatus.SELL) ) 
 			{
 				
 				if ( selectItem(slot, TraderStatus.SELL).hasSelectedItem() )
 				{
 					
 					if ( getSelectedItem().hasMultipleAmouts() 
 							&& permissionsManager.has(player, "dtl.trader.options.sell-amounts") )
 					{
 						switchInventory(getSelectedItem());
 						setTraderStatus(TraderStatus.SELL_AMOUNT);
 					}
 					else 
 					{
 						double price = getPrice(player, "sell");
 						//checks
 						if ( !checkLimits() )
 						{
 							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_LIMIT));
 							player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:limit"));
 						}
 						else
 						if ( !inventoryHasPlace(0) )
 						{
 							player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:inventory"));
 							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_SPACE));
 						}
 						else
 						if ( !buyTransaction(price) )
 						{
 							player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:money"));
 							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_MONEY));
 						}
 						else
 						{ 
 							//TODO add debug mode
 							player.sendMessage( locale.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:bought").replace("{amount}", "" + getSelectedItem().getAmount() ).replace("{price}", f.format(price) ) );
 
 							addSelectedToInventory(0);
 
 							updateLimits();
 							
 							//call event Denizen Transaction Trigger
 							Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.SUCCESS_SELL));
 							
 							//logging
 							log("buy", 
 								getSelectedItem().getItemStack().getTypeId(),
 								getSelectedItem().getItemStack().getData().getData(), 
 								getSelectedItem().getAmount(), 
 								price );
 							
 						}
 						
 						//TODO remove?
 					//	else
 					//	{
 							//TODO add debug mode
 					//		p.sendMessage( locale.getLocaleString("xxx-item-cost-xxx").replace("{price}", f.format(getPrice(p, "sell")) ) );
 					//		p.sendMessage( locale.getLocaleString("xxx-transaction-continue", "transaction:buy") );
 					//		setClickedSlot(slot);
 					//	}
 					}
 				}
 			} 
 			else
 			if ( equalsTraderStatus(TraderStatus.SELL_AMOUNT) ) 
 			{
 				
 				if ( !event.getCurrentItem().getType().equals(Material.AIR) ) 
 				{
 					double price = getPrice(player, "sell", slot);
 					if ( !checkLimits(slot) )
 					{
 						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_LIMIT));
 						player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:limit"));
 					}
 					else
 					if ( !inventoryHasPlace(slot) )
 					{
 						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_SPACE));
 						player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:inventory"));
 					}
 					else
 					if ( !buyTransaction(price) ) 
 					{
 						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_MONEY));
 						player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:buying", "reason:money"));
 					}
 					else
 					{
 						//send message
 						//TODO add debug mode
 						player.sendMessage( locale.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:bought").replace("{amount}", "" + getSelectedItem().getAmount(slot) ).replace("{price}", f.format(price) ) );
 						
 						
 						addSelectedToInventory(slot);
 						
 						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.SUCCESS_SELL));
 						
 						updateLimits(slot);
 						switchInventory(getSelectedItem());
 						
 						//logging
 						log("buy", 
 							getSelectedItem().getItemStack().getTypeId(),
 							getSelectedItem().getItemStack().getData().getData(), 
 							getSelectedItem().getAmount(slot), 
 							price );
 						
 					}
 					//TODO remove?
 				//	else 
 				//	{
 						
 				//		p.sendMessage( locale.getLocaleString("xxx-item-cost-xxx").replace("{price}", f.format(getPrice(p, "sell", slot)) ) );
 				//		p.sendMessage( locale.getLocaleString("xxx-transaction-continue", "transaction:buy") );
 				//		setClickedSlot(slot);
 				//	}
 				}
 			} 
 			
 			//TODO remove ?
 		/*	else 
 			if ( equalsTraderStatus(TraderStatus.BUY) )
 			{
 				
 				if ( selectItem(slot, TraderStatus.BUY).hasSelectedItem() ) {
 					
 
 				//	p.sendMessage( locale.getLocaleString("xxx-item-price-xxx").replace("{price}", f.format(getPrice(p, "buy")) ) );
 				}
 			}*/
 		//	setInventoryClicked(true);
 		} 
 		else
 		{
 			if ( equalsTraderStatus(TraderStatus.BUY) ) 
 			{
 				
 				if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true).hasSelectedItem() )
 				{
 					
 					
 					//TODO remove ?
 				//	if ( getClickedSlot() == slot && !getInventoryClicked() ) 
 				//	{
 					double price = getPrice(player, "buy");
 					int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount(); 
 					
 					if ( !checkBuyLimits(scale) )
 					{
 						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_LIMIT));
 						player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:limit"));
 					}
 					else
 					if ( !sellTransaction(price*scale, event.getCurrentItem()) )
 					{
 						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), player, this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_MONEY));
 						player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:money"));
 					}
 					else
 					{
 						player.sendMessage( locale.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:sold").replace("{amount}", "" + getSelectedItem().getAmount()*scale ).replace("{price}", f.format(price*scale) ) );
 
 						//TODO
 						updateBuyLimits(scale);
 
 						NBTTagEditor.removeDescription(event.getCurrentItem());
 						removeFromInventory(event.getCurrentItem(), event);
 						
 						Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.SUCCESS_BUY));
 						
 						//logging
 						log("sell", 
 							getSelectedItem().getItemStack().getTypeId(),
 							getSelectedItem().getItemStack().getData().getData(), 
 							getSelectedItem().getAmount()*scale, 
 							price*scale );
 						
 					
 					} 
 				//	else
 				//	{
 					//	p.sendMessage( locale.getLocaleString("xxx-item-price-xxx").replace("{price}", f.format(getPrice(p, "buy")*((int)event.getCurrentItem().getAmount() / getSelectedItem().getAmount())) ) );
 				//		player.sendMessage( locale.getLocaleString("xxx-transaction-continue", "transaction:sell") );
 				//		setClickedSlot(slot);
 				//	}
 				}
 			} 
 			else
 			if ( equalsTraderStatus(TraderStatus.SELL_AMOUNT) )
 			{ 
 				//TODO add descriptions ("Can't sell here");
 				//p.sendMessage( locale.getLocaleString("amount-exception") );
 				event.setCancelled(true);
 				return;
 			} 
 			else
 			if ( selectItem(event.getCurrentItem(),TraderStatus.BUY,true).hasSelectedItem() ) 
 			{				
 				
 			//	if ( getClickedSlot() == slot && !getInventoryClicked() && permissionsManager.has(player, "dtl.trader.options.buy") ) 
 			//	{
 
 				double price = getPrice(player, "buy");
 
 				int scale = event.getCurrentItem().getAmount() / getSelectedItem().getAmount(); 
 				if ( !permissionsManager.has(player, "dtl.trader.options.buy") )
 				{
 					player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:permission") );
 				}
 				else
 				if ( !checkBuyLimits(scale) )
 				{
 					Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_MONEY));
 					player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:limit") );
 				}
 				else
 				if ( !sellTransaction(price*scale, event.getCurrentItem()) )
 				{
 					Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.FAIL_MONEY));
 					player.sendMessage(localeManager.getLocaleString("xxx-transaction-falied-xxx", "transaction:selling", "reason:money") );
 				}
 				else
 				{
 					player.sendMessage( locale.getLocaleString("xxx-transaction-xxx-item", "entity:player", "transaction:sold").replace("{amount}", "" + getSelectedItem().getAmount()*scale ).replace("{price}", f.format(price*scale) ) );
 
 					
 					//limits update
 					updateBuyLimits(scale);
 					
 					Bukkit.getServer().getPluginManager().callEvent(new TraderTransactionEvent(this, this.getNpc(), event.getWhoClicked(), this.getTraderStatus(), this.getSelectedItem(), price, TransactionResult.SUCCESS_BUY));
 					
 					NBTTagEditor.removeDescription(event.getCurrentItem());
 					
 					//inventory cleanup
 					removeFromInventory(event.getCurrentItem(),event);
 					
 					//logging
 					log("sell", 
 						getSelectedItem().getItemStack().getTypeId(),
 						getSelectedItem().getItemStack().getData().getData(), 
 						getSelectedItem().getAmount()*scale, 
 						price*scale );
 
 				}
 		//	}
 			//	else
 			//	{
 			//		if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)3)) &&
 			//			 !event.getCurrentItem().getType().equals(Material.AIR) ) 
 			//		{
 				//		p.sendMessage( locale.getLocaleString("xxx-item-price-xxx").replace("{price}", f.format(getPrice(p, "buy")*((int)event.getCurrentItem().getAmount() / getSelectedItem().getAmount())) ) );
 			//			player.sendMessage( locale.getLocaleString("xxx-transaction-continue", "transaction:sell") );
 			//			
 			//			setClickedSlot(slot);
 			//		}
 			//	}
 			}
 		//	setInventoryClicked(false);
 		}
 		event.setCancelled(true);
 	}
 	
 
 	@Override
 	public void managerMode(InventoryClickEvent event) {
 		
 		//Going to hide this in the future as an CustomEvent, for developers also
 		boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
 	//	Player p = (Player) event.getWhoClicked();
 		int slot = event.getSlot();		
 		
 		if ( slot < 0 )
 		{
 			event.setCancelled(true);
 			switchInventory(getBasicManageModeByWool());
 			return;
 		}
 		
 		DecimalFormat f = new DecimalFormat("#.##");
 		
 		if ( top )
 		{
 			setInventoryClicked(true);
 
 			// Wool checking, also removing a bug that allowed placing items for sell in the wool slots 
 			if ( isManagementSlot(slot, 3) ) {
 				
 				
 				//price managing
 				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(2)) ) 
 				{
 					
 					if ( !permissionsManager.has(player, "dtl.trader.managing.price") )
 					{
 						player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:price") );
 					}
 					else
 					{
 						//switch to price setting mode
 						setTraderStatus(TraderStatus.MANAGE_PRICE);
 						switchInventory(getBasicManageModeByWool(), "price");
 
 						getInventory().setItem(getInventory().getSize() - 2, itemsConfig.getItemManagement(6));
 						getInventory().setItem(getInventory().getSize() - 3, new ItemStack(Material.AIR));
 						
 
 						//send message
 						player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:price") );
 					}
 						
 				} 
 				else 
 				//is any mode used? return to item adding
 				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(6)) ) 
 				{
 					//close any management mode, switch to the default buy/sell management
 					if ( isSellModeByWool() )
 						this.setTraderStatus(TraderStatus.MANAGE_SELL);
 					if ( isBuyModeByWool() )
 						this.setTraderStatus(TraderStatus.MANAGE_BUY);
 
 					switchInventory(getBasicManageModeByWool(), "manage");
 					
 					getInventory().setItem(getInventory().getSize() - 2, itemsConfig.getItemManagement(2) );//new ItemStack(Material.WOOL,1,(short)0,(byte)15));
 					getInventory().setItem(getInventory().getSize() - 3, itemsConfig.getItemManagement(4) );// ( getBasicManageModeByWool().equals(TraderStatus.MANAGE_SELL) ?  : config.getItemManagement(3) ) );//new ItemStack(Material.WOOL,1,(short)0,(byte)( getBasicManageModeByWool().equals(TraderStatus.MANAGE_SELL) ? 11 : 12 ) ));
 					
 					//send message
 					player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:stock") );
 					
 				}
 				else 
 				//global limits management
 				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(4)) )
 				{
 
 					if ( !permissionsManager.has(player, "dtl.trader.managing.global-limits") )
 					{
 						player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:buy-limit") );
 					}
 					else
 					{
 						//status update
 						setTraderStatus(TraderStatus.MANAGE_LIMIT_GLOBAL);
 						switchInventory(getBasicManageModeByWool(), "glimit");
 						
 						//wool update
 						getInventory().setItem(getInventory().getSize()-3, itemsConfig.getItemManagement(6));
 						getInventory().setItem(getInventory().getSize()-2, itemsConfig.getItemManagement(5));
 	
 						//send message
 						player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:global-limit") );
 					}					
 				} 
 				else 
 				//player limits management
 				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(5)) ) 
 				{
 					if ( !permissionsManager.has(player, "dtl.trader.managing.player-limits") )
 					{
 						player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:buy-limit") );
 					}
 					else
 					{
 						//status update
 						setTraderStatus(TraderStatus.MANAGE_LIMIT_PLAYER);
 						switchInventory(getBasicManageModeByWool(), "plimit");
 						
 						//wool update
 						getInventory().setItem(getInventory().getSize()-3, itemsConfig.getItemManagement(6));
 						getInventory().setItem(getInventory().getSize()-2, itemsConfig.getItemManagement(4));
 	
 						//send message
 						player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:player-limit") );
 					}					
 				}
 				else
 				// add a nice support to this system
 				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(1)) )
 				{
 					if ( !permissionsManager.has(player, "dtl.trader.options.buy") )
 					{
 						player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:buy") );
 					}
 					else
 					{
 						switchInventory(TraderStatus.MANAGE_BUY);
 						
 	
 						//send message
 						player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:buy") );
 						
 					}
 				}
 				else
 				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(0)) )
 				{
 					if ( !permissionsManager.has(player, "dtl.trader.options.sell") )
 					{
 						player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:sell") );
 					}
 					else
 					{
 						//switch to sell mode
 						//status switching included in Inventory switch
 						switchInventory(TraderStatus.MANAGE_SELL);
 						
 						//send message
 						player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:sell") );
 						
 					}
 				}
 				else 
 				//leaving the amount managing
 				if ( isWool(event.getCurrentItem(), itemsConfig.getItemManagement(7)) ) {
 
 					//update amounts and status
 					saveManagedAmouts();
 					switchInventory(TraderStatus.MANAGE_SELL);
 					
 
 					player.sendMessage( localeManager.getLocaleString("xxx-managing-toggled", "entity:player", "manage:stock") );
 				}
 				
 				event.setCancelled(true);
 				return;
 			} 
 			else
 			{
 				//is shift clicked?
 				//amount and limit timeout managing
 				if ( event.isShiftClick() )
 				{
 					
 					//Managing global timeout limits for an item
 					if ( equalsTraderStatus(TraderStatus.MANAGE_LIMIT_GLOBAL) )
 					{
 
 						//show the current timeout
 						if ( event.getCursor().getType().equals(Material.AIR) ) 
 						{
 							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
 								player.sendMessage(localeManager.getLocaleString("xxx-value", "manage:global-timeout").replace("{value}", "" + getSelectedItem().getLimitSystem().getGlobalTimeout()) );
 								
 						}
 						
 						//timeout changing
 						else
 						{
 							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
 							{
 								
 								if ( event.isRightClick() ) 
 								{
 									getSelectedItem().getLimitSystem().changeGlobalTimeout(-calculateTimeout(event.getCursor()));
 								}
 								else
 								{
 									getSelectedItem().getLimitSystem().changeGlobalTimeout(calculateTimeout(event.getCursor()));
 								}
 								
 								NBTTagEditor.removeDescription(event.getCurrentItem());
 								TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getLimitLore(getSelectedItem(), getTraderStatus().name(), pattern, player));
 								
 								player.sendMessage(localeManager.getLocaleString("xxx-value-changed", "manage:global-timeout").replace("{value}", "" + getSelectedItem().getLimitSystem().getGlobalTimeout()) );
 							}
 
 						}
 						
 						selectItem(null);
 						event.setCancelled(true);
 						return;
 					}
 					
 					if ( equalsTraderStatus(TraderStatus.MANAGE_LIMIT_PLAYER) ) 
 					{
 
 						//show the current limit
 						if ( event.getCursor().getType().equals(Material.AIR) ) 
 						{
 							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
 								player.sendMessage(localeManager.getLocaleString("xxx-value", "manage:player-timeout").replace("{value}", "" + getSelectedItem().getLimitSystem().getPlayerTimeout()) );
 							
 							
 						}
 						//limit changing
 						else
 						{
 							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
 							{
 								
 								if ( event.isRightClick() ) 
 								{
 									getSelectedItem().getLimitSystem().changePlayerTimeout(-calculateTimeout(event.getCursor()));
 								}
 								else
 								{
 									getSelectedItem().getLimitSystem().changePlayerTimeout(calculateTimeout(event.getCursor()));
 								}
 								
 								NBTTagEditor.removeDescription(event.getCurrentItem());
 								TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getPlayerLimitLore(getSelectedItem(), getTraderStatus().name(), pattern, player));
 								
 								//add to config 
 								player.sendMessage(localeManager.getLocaleString("xxx-value-changed", "manage:player-timeout").replace("{value}", "" + getSelectedItem().getLimitSystem().getPlayerTimeout()) );
 								}
 
 						}
 						
 						//reset the selected item
 						selectItem(null);
 						
 						event.setCancelled(true);
 						return;
 					}
 					
 					
 					//amount managing
 					if ( event.isLeftClick() )
 					{
 						if ( equalsTraderStatus(TraderStatus.MANAGE_SELL) )
 						{ 
 							//we got sell managing?
 							if ( selectItem(slot,TraderStatus.MANAGE_SELL).hasSelectedItem()
 									&& permissionsManager.has(player, "dtl.trader.managing.multiple-amounts") )
 							{
 								//inventory and status update
 								switchInventory(getSelectedItem());
 								setTraderStatus(TraderStatus.MANAGE_SELL_AMOUNT); 
 							} 
 						} 
 					} 
 					//nothing to do with the shift r.click...
 					else
 					{
 						
 					}
 					event.setCancelled(true);
 					
 				} 
 				else 
 				//manager handling
 				{
 					
 					 //items managing
 					 if ( equalsTraderStatus(getBasicManageModeByWool()) ) {
 						 
 						 //r.click = stack price
 						 if ( event.isRightClick() )
 						 {
 							if ( !permissionsManager.has(player, "dtl.trader.managing.stack-price") )
 							{
 								player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "", "manage:stack-price") );
 								selectItem(null);
 								event.setCancelled(true);
 								return;
 							}
 							 
 							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() )
 							{
 								//if it has the stack price change it back to "per-item" price
 								if ( getSelectedItem().hasStackPrice() ) 
 								{
 									getSelectedItem().setStackPrice(false);
 									player.sendMessage( localeManager.getLocaleString("xxx-value", "manage:stack-price").replace("{value}", "disabled") );
 								} 
 								//change the price to a stack-price
 								else
 								{
 									getSelectedItem().setStackPrice(true);
 									player.sendMessage( localeManager.getLocaleString("xxx-value", "manage:stack-price").replace("{value}", "enabled") );
 								}
 								
 								NBTTagEditor.removeDescription(event.getCurrentItem());
 								TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getManageLore(getSelectedItem(), getTraderStatus().name(), pattern, player));
 								
 								getSelectedItem().setAsPatternItem(false);
 							}
 
 							//reset the selection
 							selectItem(null);
 							
 							//cancel the event
 							event.setCancelled(true);
 							return;
 						 }
 						 if ( hasSelectedItem() ) 
 						 {
 							 //if we got an selected item (new or old)
 							 
 							 StockItem item = getSelectedItem();
 							 
 							 //this item is new!
 							 if ( item.getSlot() == -1 ) {
 
 								 //get the real amount
 								 item.resetAmounts(event.getCursor().getAmount());
 								 
 								 //set the item to the stock
 								 if ( this.isBuyModeByWool() )
 									 getStock().addItem("buy", item);
 								 if ( this.isSellModeByWool() )
 									 getStock().addItem("sell", item);
 
 								 player.sendMessage( localeManager.getLocaleString("xxx-item", "action:added") );
 							 }
 							 
 							 //select an item if it exists in the traders inventory
 							 if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() )
 							 {
 								 getSelectedItem().setSlot(-2);
 								 player.sendMessage( localeManager.getLocaleString("xxx-item", "action:selected") );
 								 
 							 }
 							 
 							 //set the managed items slot
 							 item.setSlot(slot);
 							 item.setAsPatternItem(false);
 							 player.sendMessage( localeManager.getLocaleString("xxx-item", "action:updated") );
 							 
 						} 
 						else 
 						{
 
 							 //select an item if it exists in the traders inventory
 							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() )
 							{
 								getSelectedItem().setSlot(-2);
 								player.sendMessage( localeManager.getLocaleString("xxx-item", "action:selected") );
 							}
 							
 						}
 						return;
 					} 
 					else
 					//managing multiple amounts
 					if ( equalsTraderStatus(TraderStatus.MANAGE_SELL_AMOUNT) )
 					{
 						
 						//is item id and data equal?
 						if ( !equalsSelected(event.getCursor(),true,false) 
 								&& !event.getCursor().getType().equals(Material.AIR) ) {
 
 							//invalid item
 
 							player.sendMessage( localeManager.getLocaleString("xxx-item", "action:invalid") );
 							event.setCancelled(true);
 						}
 						if ( !event.getCursor().getType().equals(Material.AIR) )
 							getSelectedItem().setAsPatternItem(false);
 						
 						return;
 					} 
 					else
 					//manage prices
 					if ( equalsTraderStatus(TraderStatus.MANAGE_PRICE) ) 
 					{
 
 						//show prices
 						if ( event.getCursor().getType().equals(Material.AIR) ) 
 						{
 
 							//select item
 							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
 								player.sendMessage( localeManager.getLocaleString("xxx-value", "manage:price").replace("{value}", f.format(getSelectedItem().getRawPrice())) );
 							
 						} 
 						else
 						//change prices
 						{
 
 							//select item to change
 							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
 							{
 								//change price
 								if ( event.isRightClick() ) 
 									getSelectedItem().lowerPrice(calculatePrice(event.getCursor()));
 								else
 									getSelectedItem().increasePrice(calculatePrice(event.getCursor()));
 
 								getSelectedItem().setAsPatternItem(false);
 								getSelectedItem().setPetternListening(false);
 								
 								NBTTagEditor.removeDescription(event.getCurrentItem());
 								TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getPriceLore(getSelectedItem(), 0, getBasicManageModeByWool().toString(), pattern, player));
 								
 								player.sendMessage( localeManager.getLocaleString("xxx-value-changed", "", "manage:price").replace("{value}", f.format(getSelectedItem().getRawPrice())) );
 							}
 							
 						}
 						
 						//reset the selected item
 						selectItem(null);
 						
 						//cancel the event
 						event.setCancelled(true);
 						
 					} 
 					else 
 					//limit managing
 					if ( equalsTraderStatus(TraderStatus.MANAGE_LIMIT_GLOBAL) ) 
 					{
 						
 						//show limits
 						if ( event.getCursor().getType().equals(Material.AIR) )
 						{
 							
 							//select item which limit will be shown up
 							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
 							{
 								player.sendMessage( localeManager.getLocaleString("xxx-value", "manage:global-limit").replace("{value}", "" + getSelectedItem().getLimitSystem().getGlobalLimit()) );
 							}
 							
 							
 						} 
 						//change limits
 						else 
 						{
 							
 							//select the item
 							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
 							{
 								
 								if ( event.isRightClick() ) 
 									getSelectedItem().getLimitSystem().changeGlobalLimit(-calculateLimit(event.getCursor()));
 								else
 									getSelectedItem().getLimitSystem().changeGlobalLimit(calculateLimit(event.getCursor()));
 
 								NBTTagEditor.removeDescription(event.getCurrentItem());
 								TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getLimitLore(getSelectedItem(), getTraderStatus().name(), pattern, player));
 								
 								getSelectedItem().setAsPatternItem(false);
 								player.sendMessage( localeManager.getLocaleString("xxx-value-changed", "manage:global-limit").replace("{value}", "" + getSelectedItem().getLimitSystem().getGlobalLimit()) );
 							
 							}
 
 						}
 						
 						//reset the selected item
 						selectItem(null);
 						
 						//cancel the event
 						event.setCancelled(true);
 						
 					} 
 					else 
 					//player limits
 					if ( equalsTraderStatus(TraderStatus.MANAGE_LIMIT_PLAYER) ) 
 					{
 						//show limits
 						if ( event.getCursor().getType().equals(Material.AIR) )
 						{
 							
 							//select item which limit will be shown up
 							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
 							{
 								player.sendMessage( localeManager.getLocaleString("xxx-value", "manage:player-limit").replace("{value}", "" + getSelectedItem().getLimitSystem().getPlayerLimit()) );
 							}
 							
 							
 						} 
 						//change limits
 						else 
 						{
 							
 							//select the item
 							if ( selectItem(slot, getBasicManageModeByWool()).hasSelectedItem() ) 
 							{
 								
 								if ( event.isRightClick() ) 
 									getSelectedItem().getLimitSystem().changePlayerLimit(-calculateLimit(event.getCursor()));
 								else
 									getSelectedItem().getLimitSystem().changePlayerLimit(calculateLimit(event.getCursor()));
 								
 								NBTTagEditor.removeDescription(event.getCurrentItem());
 								TraderStockPart.setLore(event.getCurrentItem(), TraderStockPart.getPlayerLimitLore(getSelectedItem(), getTraderStatus().name(), pattern, player));
 								
 								getSelectedItem().setAsPatternItem(false);
 								player.sendMessage( localeManager.getLocaleString("xxx-value-changed", "manage:player-limit").replace("{value}", "" + getSelectedItem().getLimitSystem().getPlayerLimit()) );
 							
 							}
 
 						}
 						
 
 						//reset the selected item
 						selectItem(null);
 						
 						
 						event.setCancelled(true);
 					}
 					 
 				} 
 				
 			} 
 			
 		} 
 		//bottom inventory click
 		else
 		{
 			//is item managing
 			if ( equalsTraderStatus(getBasicManageModeByWool()) )
 			{
 				
 				//is an item is selected
 				if ( getInventoryClicked() && hasSelectedItem() ) {
 
 					//remove it from the stock
 					if ( equalsTraderStatus(TraderStatus.MANAGE_SELL) )
 						getStock().removeItem("sell", getSelectedItem().getSlot());
 					if ( equalsTraderStatus(TraderStatus.MANAGE_BUY) )
 						getStock().removeItem("buy", getSelectedItem().getSlot());
 					
 					//reset the item
 					selectItem(null);
 					
 					//send a message
 					player.sendMessage( localeManager.getLocaleString("xxx-item", "action:removed") );
 					
 				} 
 				else
 				//select a new item ready to be a stock item
 				{
 					
 					//we don't want to have air in our stock, dont we?
 					if ( event.getCurrentItem().getTypeId() != 0 ) 
 					{
 						selectItem( toStockItem(event.getCurrentItem()) );
 						//send a message
 						player.sendMessage( localeManager.getLocaleString("xxx-item", "action:selected") );
 					}
 				}
 			} 
 			
 			setInventoryClicked(false);
 		}
 	}
 
 	@Override
 	public boolean onRightClick(Player player, TraderCharacterTrait trait, NPC npc) 
 	{
 		if ( player.getGameMode().equals(GameMode.CREATIVE) 
 				&& !permissionsManager.has(player, "dtl.trader.bypass.creative") )
 		{
 			player.sendMessage( localeManager.getLocaleString("lacks-permissions-creative") );
 			return false;
 		}
 		if ( player.getItemInHand().getTypeId() == itemsConfig.getManageWand().getTypeId() )
 		{
 			
 			if ( !permissionsManager.has(player, "dtl.trader.bypass.managing") 
 				&& !player.isOp() )
 			{
 				if ( !permissionsManager.has(player, "dtl.trader.options.manage") )
 				{
 					player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "entity:trader") );
 					return false;
 				}
 				if ( !trait.getConfig().getOwner().equals(player.getName()) )
 				{
 					player.sendMessage( localeManager.getLocaleString("lacks-permissions-manage-xxx", "manage:{entity}", "entity:trader") );
 					return false;
 				}
 			}
 			
 			if ( getTraderStatus().isManaging() )
 			{
 				switchInventory( getStartStatus(player) );
 				player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " exited the manager mode");
 				return true;
 			}	
 
 			player.sendMessage(ChatColor.AQUA + npc.getFullName() + ChatColor.RED + " entered the manager mode!");
 
 			switchInventory( getManageStartStatus(player) );
 			return true;
 		}
 
 		NBTTagEditor.removeDescriptions(player.getInventory());
 		if ( !getTraderStatus().isManaging() )
			loadDescriptions(player.getInventory());	
 		
 		player.openInventory(getInventory());
 		return true;
 	}
 
 	@Override
 	public EcoNpcType getType() {
 		return EcoNpcType.SERVER_TRADER;
 	}
 	
 }
 
