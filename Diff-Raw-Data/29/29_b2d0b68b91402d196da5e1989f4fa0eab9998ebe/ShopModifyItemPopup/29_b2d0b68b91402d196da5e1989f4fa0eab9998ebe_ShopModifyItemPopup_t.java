 
 package org.ss.gui;
 
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.gui.Button;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericItemWidget;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 import org.ss.SpoutShopPermissions;
 import org.ss.shop.Shop;
 import org.ss.shop.ShopEntry;
 
 public class ShopModifyItemPopup
 		extends ShopPopup {
 
 	public final ShopEntry entry;
 
 	private GenericLabel lbl_amount = new GenericLabel();
 	private GenericTextField txt_setamount;
 	private GenericButton btn_setamount_update;
 	private GenericTextField txt_buying = new GenericTextField();
 	private GenericButton btn_buying_update = new GenericButton( "Update" );
 	private GenericTextField txt_store_price = new GenericTextField();
 	private GenericButton btn_store_price_update = new GenericButton( "Update" );
 	private GenericTextField txt_purchase_price = new GenericTextField();
 	private GenericButton btn_purchase_price_update = new GenericButton( "Update" );
 	private GenericTextField txt_remove = new GenericTextField();
 	private GenericButton btn_remove = new GenericButton( "Remove" );
 	private GenericButton btn_delete = new GenericButton( "Delete" );
 
 	public ShopModifyItemPopup( SpoutPlayer player, Shop shop, ShopEntry entry ) {
 		super( "Shop: Modify Item", player, shop );
 
 		this.entry = entry;
 
 		if ( !shop.isManager( player ) ) {
 			close();
 			return;
 		}
 
 		btn_delete.setAnchor( WidgetAnchor.TOP_LEFT );
 		btn_delete.setX( SCREEN_WIDTH - 90 );
 		btn_delete.setY( 100 );
 		btn_delete.setWidth( 80 );
 		btn_delete.setHeight( 20 );
 
 		GenericItemWidget display = new GenericItemWidget( entry.createItemStack() );
 		display.setAnchor( WidgetAnchor.TOP_LEFT );
 		display.setWidth( 10 );
 		display.setHeight( 10 );
 		display.setX( 100 );
 		display.setY( 10 );
 
 		lbl_amount.setAnchor( WidgetAnchor.TOP_LEFT );
 		lbl_amount.setX( 100 );
 		lbl_amount.setY( 40 );
 
 		if ( SpoutShopPermissions.ADMIN.hasNode( player ) ) {
 			lbl_amount.setText( "Modify\nStock" );
 
 			txt_setamount = new GenericTextField();
 			txt_setamount.setAnchor( WidgetAnchor.TOP_LEFT );
 			txt_setamount.setX( SCREEN_WIDTH - 270 );
 			txt_setamount.setY( 40 );
 			txt_setamount.setWidth( 80 );
 			txt_setamount.setHeight( 20 );
 			txt_setamount.setPlaceholder( Integer.toString( entry.units_in_stock )
 					+ ( entry.hasInfiniteStock() ? " (Infinite)" : "" ) );
 			txt_setamount.setTooltip( "The number of units in stock.\n-1 means infinite stock." );
 
 			btn_setamount_update = new GenericButton( "Update" );
 			btn_setamount_update.setAnchor( WidgetAnchor.TOP_LEFT );
 			btn_setamount_update.setX( SCREEN_WIDTH - 180 );
 			btn_setamount_update.setY( 40 );
 			btn_setamount_update.setWidth( 80 );
 			btn_setamount_update.setHeight( 20 );
 
 			attachWidgets( txt_setamount, btn_setamount_update );
 		} else
 			lbl_amount.setText( "In Stock: " + entry.units_in_stock );
 
 		lbl_amount.setWidth( lbl_amount.getText().length() * 5 );
 		lbl_amount.setHeight( 10 );
 
 		GenericLabel lbl_wanted = new GenericLabel( "Buying" );
 		lbl_wanted.setAnchor( WidgetAnchor.TOP_LEFT );
 		lbl_wanted.setX( 100 );
 		lbl_wanted.setY( 70 );
 		lbl_wanted.setWidth( lbl_wanted.getText().length() * 5 );
 		lbl_wanted.setHeight( 10 );
 		lbl_wanted.setTooltip( "The number of units you are wanting to buy from players.\n-1 is always buying." );
 
 		txt_buying.setAnchor( WidgetAnchor.TOP_LEFT );
 		txt_buying.setX( SCREEN_WIDTH - 270 );
 		txt_buying.setY( 70 );
 		txt_buying.setWidth( 80 );
 		txt_buying.setHeight( 20 );
 		txt_buying.setPlaceholder( Integer.toString( entry.units_wanted )
 				+ ( entry.hasInfiniteDemand() ? " (Infinite)" : "" ) );
 
 		btn_buying_update.setAnchor( WidgetAnchor.TOP_LEFT );
 		btn_buying_update.setX( SCREEN_WIDTH - 180 );
 		btn_buying_update.setY( 70 );
 		btn_buying_update.setWidth( 80 );
 		btn_buying_update.setHeight( 20 );
 
 		GenericLabel lbl_store_price = new GenericLabel( "Store\nPrice" );
 		lbl_store_price.setAnchor( WidgetAnchor.TOP_LEFT );
 		lbl_store_price.setX( 100 );
 		lbl_store_price.setY( 100 );
 		lbl_store_price.setWidth( lbl_wanted.getText().length() * 5 );
 		lbl_store_price.setHeight( 10 );
 		lbl_store_price
 				.setTooltip( "The cost the player must pay per unit of this item.\n<0 will stop selling the item." );
 
 		txt_store_price.setAnchor( WidgetAnchor.TOP_LEFT );
 		txt_store_price.setX( SCREEN_WIDTH - 270 );
 		txt_store_price.setY( 100 );
 		txt_store_price.setWidth( 80 );
 		txt_store_price.setHeight( 20 );
 		if ( entry.cost_to_buy_unit < 0 )
 			txt_store_price.setPlaceholder( "Not For Sale" );
 		else
 			txt_store_price.setPlaceholder( Double.toString( entry.cost_to_buy_unit ) );
 
 		btn_store_price_update.setAnchor( WidgetAnchor.TOP_LEFT );
 		btn_store_price_update.setX( SCREEN_WIDTH - 180 );
 		btn_store_price_update.setY( 100 );
 		btn_store_price_update.setWidth( 80 );
 		btn_store_price_update.setHeight( 20 );
 
 		GenericLabel lbl_purchase = new GenericLabel( "Purchase\nPrice" );
 		lbl_purchase.setAnchor( WidgetAnchor.TOP_LEFT );
 		lbl_purchase.setX( 100 );
 		lbl_purchase.setY( 130 );
 		lbl_purchase.setWidth( lbl_wanted.getText().length() * 5 );
 		lbl_purchase.setHeight( 10 );
 		lbl_purchase.setTooltip( "The cost the store will pay per unit of this item.\n<0 will stop buying the item." );
 
 		txt_purchase_price.setAnchor( WidgetAnchor.TOP_LEFT );
 		txt_purchase_price.setX( SCREEN_WIDTH - 270 );
 		txt_purchase_price.setY( 130 );
 		txt_purchase_price.setWidth( 80 );
 		txt_purchase_price.setHeight( 20 );
 		if ( entry.cost_to_sell_unit < 0 )
 			txt_purchase_price.setPlaceholder( "Not For Sale" );
 		else
 			txt_purchase_price.setPlaceholder( Double.toString( entry.cost_to_sell_unit ) );
 
 		btn_purchase_price_update.setAnchor( WidgetAnchor.TOP_LEFT );
 		btn_purchase_price_update.setX( SCREEN_WIDTH - 180 );
 		btn_purchase_price_update.setY( 130 );
 		btn_purchase_price_update.setWidth( 80 );
 		btn_purchase_price_update.setHeight( 20 );
 
 		GenericLabel lbl_remove = new GenericLabel( "Remove\nItems" );
 		lbl_remove.setAnchor( WidgetAnchor.TOP_LEFT );
 		lbl_remove.setX( 100 );
 		lbl_remove.setY( 160 );
 		lbl_remove.setWidth( lbl_wanted.getText().length() * 5 );
 		lbl_remove.setHeight( 10 );
 
 		txt_remove.setAnchor( WidgetAnchor.TOP_LEFT );
 		txt_remove.setX( SCREEN_WIDTH - 270 );
 		txt_remove.setY( 160 );
 		txt_remove.setWidth( 80 );
 		txt_remove.setHeight( 20 );
 		txt_remove.setPlaceholder( "0" );
 
 		btn_remove.setAnchor( WidgetAnchor.TOP_LEFT );
 		btn_remove.setX( SCREEN_WIDTH - 180 );
 		btn_remove.setY( 160 );
 		btn_remove.setWidth( 80 );
 		btn_remove.setHeight( 20 );
 
 		attachWidgets( btn_delete, display, lbl_amount, lbl_wanted, txt_buying, btn_buying_update, lbl_store_price,
 				txt_store_price, btn_store_price_update, lbl_purchase, txt_purchase_price, btn_purchase_price_update,
 				lbl_remove, txt_remove, btn_remove );
 	}
 
 	public void onButtonClick( ButtonClickEvent bce ) {
 		Button button = bce.getButton();
 
 		if ( !shop.isManager( player ) ) {
 			close();
 			return;
 		}
 
 		if ( button.equals( btn_delete ) ) {
 			if ( entry.units_in_stock > 0 ) {
 				ItemStack stack = entry.createItemStack();
 				stack.setAmount( entry.units_in_stock );
 
 				player.getInventory().addItem( stack );
 			}
 
 			shop.shop_entries.remove( entry );
 
 			ShopLookupItemPopup popup = new ShopLookupItemPopup( player, shop );
 			popup.setStatus( color_green, "Entry deleted." );
 			popup.show();
 			return;
 		}
 
 		if ( button.equals( btn_setamount_update ) ) {
 			try {
 				Integer amount = Integer.parseInt( txt_setamount.getText() );
 
 				if ( amount < 1 ) {
 					setStatus( color_green, "Infinite stock activated." );
 					entry.units_in_stock = -1;
 					return;
 				} else {
 					entry.units_in_stock = amount;
 					setStatus( color_green, "Stock set to " + amount );
 				}
 
 			} catch ( Exception e ) {
 				setError( "You must enter only a number." );
 			}
 
 			return;
 		}
 
 		if ( button.equals( btn_buying_update ) ) {
 			try {
 				Integer amount = Integer.parseInt( txt_buying.getText() );
 
 				if ( amount < 0 ) {
 					setStatus( color_green, "Infinite demand activate." );
 				} else {
 					if ( amount == 0 )
 						setStatus( color_green, "You are no longer buying this item." );
 					else
 						setStatus( color_green, "Now buying " + amount + " units" );
 				}
 			} catch ( Exception e ) {
 				setError( "You must enter only a number." );
 			}
 
 			return;
 		}
 
 		if ( button.equals( btn_store_price_update ) ) {
 			try {
				double amount = Double.parseDouble( txt_store_price.getText() );
				amount = Double.parseDouble( format.format( amount ) );
 
 				if ( amount < 0 ) {
 					entry.cost_to_buy_unit = -1;
 					setStatus( color_error, "No longer selling item." );
 					return;
 				}
 
 				entry.cost_to_buy_unit = amount;
 				setStatus( color_green, "Now selling at $" + amount + "/unit." );
 			} catch ( Exception e ) {
 				setError( "You must enter only a number." );
 			}
 		}
 
 		if ( button.equals( btn_purchase_price_update ) ) {
 			try {
				double amount = Double.parseDouble( txt_purchase_price.getText() );
				amount = Double.parseDouble( format.format( amount ) );
 
 				if ( amount < 0 ) {
 					entry.cost_to_sell_unit = -1;
 					setStatus( color_red, "No longer buying item." );
 					return;
 				}
 
 				entry.cost_to_sell_unit = amount;
 				setStatus( color_green, "Now buying at $" + amount + "/unit." );
 			} catch ( Exception e ) {
 				setError( "You must enter only a number." );
 			}
 		}
 
 		if ( button.equals( btn_remove ) ) {
 			try {
 				Integer amount = Integer.parseInt( txt_remove.getText() );
 
				if ( amount < 1 ) {
					setError( "Please enter only a positive number." );
 					return;
 				}
 
 				if ( entry.hasInfiniteStock() ) {
 					setError( "Please disable infinite stock to use this." );
 					return;
 				}
 
 				if ( entry.units_in_stock < amount ) {
 					setError( "There are not enough units in stock." );
 					return;
 				}
 
 				ItemStack stack = entry.createItemStack();
 				stack.setAmount( amount );
 				entry.units_in_stock -= amount;
 				player.getInventory().addItem( stack );
 
 				if ( entry.units_in_stock == 0 )
 					setStatus( color_green, "All of the units have been removed." );
 				else
 					setStatus( color_green, amount + " units have been removed." );
 
 				if ( !player.hasPermission( "spoutshops.admin" ) )
 					lbl_amount.setText( "In Stock: " + entry.units_in_stock );
 				else
 					txt_setamount.setPlaceholder( Integer.toString( entry.units_in_stock ) );
 
 			} catch ( NumberFormatException nfe ) {
 				setError( "Please enter only a positive number." );
 			}
 		}
 
 		super.onButtonClick( bce );
 	}
 }
