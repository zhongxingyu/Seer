 package net.dtl.citizenstrader;
 
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.inventory.ItemStack;
 
 import net.citizensnpcs.api.exception.NPCLoadException;
 import net.citizensnpcs.api.npc.NPC;
 import net.citizensnpcs.api.npc.character.Character;
 import net.citizensnpcs.api.util.DataKey;
 import net.dtl.citizenstrader.TraderStatus.Status;
 import net.dtl.citizenstrader.traits.InventoryTrait;
 import net.dtl.citizenstrader.traits.StockItem;
 import net.milkbowl.vault.economy.Economy;
 
 
 public class TraderNpc extends Character implements Listener {
 	private Economy econ;
 	
 	private HashMap<String,TraderStatus> state = new HashMap<String,TraderStatus>();
 	
 	public TraderNpc() {
 	}
 	
 	public void setEcon(Economy e) {
 		econ = e;
 	}
 	
 	public TraderStatus getStatus(String name) {
 		if ( state.containsKey(name) )
 			return state.get(name);
 		return null;
 	}
 
 	@Override
 	public void load(DataKey arg0) throws NPCLoadException {		
 	}
 
 	@Override
 	public void save(DataKey arg0) {
 	} 
 	
 	@Override
 	public void onRightClick(NPC npc, Player p) {
 		
 		if ( p.getItemInHand().getTypeId() != 280 ) {
 			if ( !state.containsKey(p.getName()) )
 				state.put(p.getName(),new TraderStatus(npc));
 
 			state.get(p.getName()).setInventory(npc.getTrait(InventoryTrait.class).inventoryView(54,npc.getName()));
 			if ( state.get(p.getName()).getStatus().equals(Status.PLAYER_MANAGE_SELL) )
 				npc.getTrait(InventoryTrait.class).inventoryView(state.get(p.getName()).getInventory(), Status.PLAYER_MANAGE_SELL);
 			p.openInventory(state.get(p.getName()).getInventory());
 			
 		} else {
 			if ( state.containsKey(p.getName()) && state.get(p.getName()).getTrader().getId() == npc.getId() ) {
 				if ( !state.get(p.getName()).getStatus().equals(Status.PLAYER_MANAGE_SELL) ) {
 					state.get(p.getName()).setStatus(Status.PLAYER_MANAGE_SELL);
 					p.sendMessage(ChatColor.RED + "Trader manager enabled");
 				} else if ( state.get(p.getName()).getStatus().equals(Status.PLAYER_MANAGE_SELL) ) { 
 					state.get(p.getName()).setStatus(Status.PLAYER_SELL);
 					p.sendMessage(ChatColor.RED + "Trader manager disabled");
 				}
 			} else {
 				state.put(p.getName(),new TraderStatus(npc,Status.PLAYER_MANAGE_SELL));
 				p.sendMessage(ChatColor.RED + "Trader manager enabled");
 			}
 		}
 			
 		
 	}
 	
 	@Override
     public void onSet(NPC npc) {
         if( !npc.hasTrait(InventoryTrait.class) ){
             npc.addTrait( new InventoryTrait() );
         }
     }
 
 	@EventHandler
 	public void inventoryClick(InventoryClickEvent event) {
 		if ( event.getRawSlot() < 0 )
 			return;
 		if ( event.getWhoClicked() instanceof Player ) {
 			Player p = (Player) event.getWhoClicked();
 			if ( state.containsKey(p.getName()) ) {
 				TraderStatus trader = state.get(p.getName());
 				InventoryTrait sr = trader.getTrader().getTrait(InventoryTrait.class);
 				boolean top = event.getView().convertSlot(event.getRawSlot()) == event.getRawSlot();
 				
 				if ( (!trader.getStatus().equals(Status.PLAYER_MANAGE_SELL) && 
 					  !trader.getStatus().equals(Status.PLAYER_MANAGE_SELL_AMOUT) ) && 
 					  !trader.getStatus().equals(Status.PLAYER_MANAGE_PRICE ) && top ) {
 					StockItem si = null;
 					
 					if ( trader.getStatus().equals(Status.PLAYER_SELL) || trader.getStatus().equals(Status.PLAYER_SELL_AMOUT) ) {
 						if ( trader.getStatus().equals(Status.PLAYER_SELL_AMOUT) )
 							si = trader.getStockItem();
 						else
 							si = sr.itemForSell(event.getSlot());
 						if ( si != null ) {
 							if ( event.isShiftClick() ) {
 								if ( si.hasMultipleAmouts() && trader.getStatus().equals(Status.PLAYER_SELL_AMOUT) ) {
 									if ( econ.has(p.getName(), si.getPrice(event.getSlot())) ) {
 										if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)14)) &&
 											 !event.getCurrentItem().getType().equals(Material.AIR)) {
 											econ.withdrawPlayer(p.getName(), si.getPrice(event.getSlot()));
 											p.getInventory().addItem(event.getCurrentItem());
 											p.sendMessage(ChatColor.GOLD + "You bought " + event.getCurrentItem().getAmount() + " for " + si.getPrice(event.getSlot()) + ".");
 										}
 									} else {
 										p.sendMessage(ChatColor.GOLD + "You don't have enough money.");
 									}
 								} else {
 									if ( econ.has(p.getName(), si.getPrice()) ) {
 										econ.withdrawPlayer(p.getName(), si.getPrice());
 										p.getInventory().addItem(si.getItemStack());
 										p.sendMessage(ChatColor.GOLD + "You bought " + si.getItemStack().getAmount() + " for " + si.getPrice() + ".");
 									} else {
 										p.sendMessage(ChatColor.GOLD + "You don't have enough money.");
 									}
 								}
 							} else {
 								if ( trader.getStatus().equals(Status.PLAYER_SELL_AMOUT) ) {
 									if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)14)) && ( event.getSlot() == trader.getInventory().getSize() - 1 ) ) {
 										trader.getInventory().clear();
 										sr.inventoryView(trader.getInventory(),Status.PLAYER_SELL);
 										trader.setStatus(Status.PLAYER_SELL);
 										trader.setStockItem(null);
 									} else {
 										if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)14)) &&
 											 !event.getCurrentItem().getType().equals(Material.AIR) ) 
 											p.sendMessage(ChatColor.GOLD + "This item costs " + si.getPrice(event.getSlot()) + ".");
 									}
 								} else if ( trader.getStatus().equals(Status.PLAYER_SELL) ) {
 									if ( si.hasMultipleAmouts() ) {
 										if ( trader.getStatus().equals(Status.PLAYER_SELL) ) {
 											trader.getInventory().clear();
 											InventoryTrait.setInventoryWith(trader.getInventory(), si);
 											trader.setStatus(Status.PLAYER_SELL_AMOUT);
 											trader.setStockItem(si);
 										}
 									} else {
 										
 											p.sendMessage(ChatColor.GOLD + "This item costs " + si.getPrice() + ".");
 									}
 								}
 							} 
 						} else if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)5)) && ( event.getSlot() == trader.getInventory().getSize() - 1 ) ) {
 							trader.getInventory().clear();
 							sr.inventoryView(trader.getInventory(),Status.PLAYER_BUY);
 							trader.setStatus(Status.PLAYER_BUY);
 							trader.setStockItem(null);
 						} 
 					} else if ( trader.getStatus().equals(Status.PLAYER_BUY) ) {
 						si = sr.wantItemBuy(event.getSlot());
 						if ( si != null ) {
 							if ( si.getItemStack().getType().equals(event.getCursor().getType()) ) {
 								econ.depositPlayer(p.getName(), si.getPrice()*event.getCursor().getAmount());
								p.sendMessage(ChatColor.GOLD + "You sold " + event.getCursor().getAmount() + " for " + si.getPrice(event.getSlot()) + ".");
 								event.setCursor(new ItemStack(Material.AIR));
 							} else {
 								if ( !event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)3)) &&
 									 !event.getCurrentItem().getType().equals(Material.AIR) ) 
 								p.sendMessage(ChatColor.GOLD + "You can get for this item " + si.getPrice(event.getSlot()) + " money.");
 							}
 						} else {
 							if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)3)) && ( event.getSlot() == trader.getInventory().getSize() - 1 ) ) {
 								trader.getInventory().clear();
 								sr.inventoryView(trader.getInventory(),Status.PLAYER_SELL);
 								trader.setStatus(Status.PLAYER_SELL);
 								trader.setStockItem(null);
 							}
 						}
 					}					
 					event.setCancelled(true);
 				} else {
 					StockItem si = null;
 					if ( top ) {
 						if ( event.isShiftClick() ) {
 							if ( trader.getStatus().equals(Status.PLAYER_MANAGE_SELL) ) {
 								si = sr.itemForSell(event.getSlot());
 								if ( si != null ) {
 									trader.getInventory().clear();
 									InventoryTrait.setInventoryWith(trader.getInventory(), si);
 									trader.setStatus(Status.PLAYER_MANAGE_SELL_AMOUT);
 									trader.setStockItem(si);
 								} else {
 									if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1)) && 
 										 ( event.getSlot() == trader.getInventory().getSize() - 2 ) ) {
 										trader.setStatus(Status.PLAYER_MANAGE_PRICE);
 										trader.getInventory().clear();
 										sr.inventoryView(trader.getInventory(),Status.PLAYER_MANAGE_PRICE);
 										
 									}
 								}
 							} else if ( trader.getStatus().equals(Status.PLAYER_MANAGE_SELL_AMOUT) ) {
 								sr.saveNewAmouts(trader.getInventory(), trader.getStockItem());
 								trader.getInventory().clear();
 								sr.inventoryView(trader.getInventory(),Status.PLAYER_MANAGE_SELL);
 								trader.setStatus(Status.PLAYER_MANAGE_SELL);
 								trader.setStockItem(null);
 							} else if ( trader.getStatus().equals(Status.PLAYER_MANAGE_PRICE) ) {
 								 if ( event.getCurrentItem().equals(new ItemStack(Material.WOOL,1,(short)0,(byte)15)) && 
 								      ( event.getSlot() == trader.getInventory().getSize() - 2 ) ) {
 									trader.setStatus(Status.PLAYER_MANAGE_SELL);
 									trader.getInventory().clear();
 									sr.inventoryView(trader.getInventory(),Status.PLAYER_MANAGE_SELL);
 								}
 							}
 
 							event.setCancelled(true);
 						} else {
 							if ( trader.getStatus().equals(Status.PLAYER_MANAGE_SELL) ) {
 								if ( trader.getStockItem() == null ) {
 									trader.setStockItem( sr.itemForSell(event.getSlot()) );
 								} else {
 									if ( trader.getStockItem().getSlot() < 0 ) {
 										trader.getStockItem().getAmouts().clear();
 										trader.getStockItem().addAmout(event.getCursor().getAmount());
 										sr.addItem(true, trader.getStockItem());
 									}
 									StockItem item = trader.getStockItem();
 									
 									if ( !event.getCurrentItem().getType().equals(Material.AIR) )
 										trader.setStockItem(sr.itemForSell(event.getSlot()));
 									else
 										trader.setStockItem(null);
 									item.setSlot(event.getSlot());
 								}
 							} else if ( trader.getStatus().equals(Status.PLAYER_MANAGE_SELL_AMOUT) ) {
 								if ( !event.getCursor().getType().equals(Material.AIR) &&
 									 !( event.getCursor().getType().equals(trader.getStockItem().getItemStack().getType()) &&
 									    event.getCursor().getData().equals(trader.getStockItem().getItemStack().getData()) ) ||
 									 ( !event.getCurrentItem().getType().equals(trader.getStockItem().getItemStack().getType()) &&
 									   !event.getCurrentItem().getType().equals(Material.AIR) ) ) {
 									p.sendMessage(ChatColor.GOLD + "Wrong item!");
 									event.setCancelled(true);
 								}
 							} else if ( trader.getStatus().equals(Status.PLAYER_MANAGE_BUY) ) {
 								//Future Implementation
 							} else if ( trader.getStatus().equals(Status.PLAYER_MANAGE_PRICE) ) {
 								si = sr.itemForSell(event.getSlot());
 								if ( si == null )
 									si = sr.wantItemBuy(event.getSlot());
 								if ( si != null ) {
 									if ( event.isLeftClick() )
 										si.increasePrice(this.getManagePriceAmout(event.getCursor()));
 									else if ( event.isRightClick() ) 
 										si.lowerPrice(this.getManagePriceAmout(event.getCursor()));
 									p.sendMessage(ChatColor.GOLD + "New price: " + si.getPrice()/si.getAmouts().get(0));
 									event.setCancelled(true);
 								} else 
 									p.sendMessage(ChatColor.GOLD + "Wrong Item!");
 							}
 						}
 						trader.setLastInv(true);
 					} else {
 						if ( trader.getStatus().equals(Status.PLAYER_MANAGE_SELL) || trader.getStatus().equals(Status.PLAYER_MANAGE_BUY) ) {
 							if ( trader.getLastInv() && trader.getStockItem() != null ) {
 								sr.removeItem(true, trader.getStockItem().getSlot());
 								trader.setStockItem(null);
 							} else {
 								ItemStack is = event.getCurrentItem();
 								trader.setStockItem(new StockItem(is.getTypeId()+" a:"+is.getAmount()));
 							}
 						} else {
 						}
 						trader.setLastInv(false);
 					}
 				}
 			} 
 		}
 	}
 	
 
 
 
 	@EventHandler
 	public void inventoryClose(InventoryCloseEvent event){
 	    if(state.containsKey(event.getPlayer().getName())){
 			if ( state.get(event.getPlayer().getName()).getStatus().equals(Status.PLAYER_SELL_AMOUT) ||
 				 state.get(event.getPlayer().getName()).getStatus().equals(Status.PLAYER_SELL) ||
 				 state.get(event.getPlayer().getName()).getStatus().equals(Status.PLAYER_BUY) ) 
 				state.remove(event.getPlayer().getName());
 			else {
 				TraderStatus trader = state.get(event.getPlayer().getName());
 				InventoryTrait sr = trader.getTrader().getTrait(InventoryTrait.class);
 				if ( trader.getStatus().equals(Status.PLAYER_MANAGE_SELL_AMOUT) ){
 					sr.saveNewAmouts(trader.getInventory(), trader.getStockItem());
 					trader.getInventory().clear();
 					sr.inventoryView(trader.getInventory(),Status.PLAYER_SELL);
 				}
 				trader.setStatus(Status.PLAYER_MANAGE_SELL);
 				trader.setStockItem(null);
 			}
 	    }
 	}
 	
 	public int getManagePriceAmout(ItemStack is) {
 		if ( is.getType().equals(Material.DIRT) )
 			return is.getAmount()*10;		
 		else if ( is.getType().equals(Material.COBBLESTONE) )
 			return is.getAmount()*100;
 		return is.getAmount();
 	}
 	
 }
 	
 
