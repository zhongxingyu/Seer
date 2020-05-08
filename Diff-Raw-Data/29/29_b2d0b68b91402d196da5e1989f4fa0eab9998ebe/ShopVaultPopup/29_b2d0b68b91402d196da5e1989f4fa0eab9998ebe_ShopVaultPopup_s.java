 
 package org.ss.gui;
 
 import net.milkbowl.vault.economy.EconomyResponse;
 
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.gui.Button;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 import org.ss.SpoutShopPermissions;
 import org.ss.shop.Shop;
 
 public class ShopVaultPopup
 		extends ShopPopup {
 
 	GenericTextField txt_setfunds;
 	GenericButton btn_setfunds_update;
 	GenericTextField txt_withdraw = new GenericTextField();
 	GenericButton btn_withdraw = new GenericButton( "Withdraw" );
 	GenericTextField txt_deposit = new GenericTextField();
 	GenericButton btn_deposit = new GenericButton( "Deposit" );
 
 	public ShopVaultPopup( SpoutPlayer player, Shop shop ) {
 		super( "Shop: Vault", player, shop );
 
 		if ( !shop.isManager( player ) ) {
 			close();
 			return;
 		}
 
 		if ( SpoutShopPermissions.ADMIN.hasNode( player ) ) {
 			GenericLabel lbl_setfunds = new GenericLabel( "Set\nFunds" );
 			lbl_setfunds.setAnchor( WidgetAnchor.TOP_LEFT );
 			lbl_setfunds.setX( 70 );
 			lbl_setfunds.setY( 40 );
 			lbl_setfunds.setWidth( 20 );
 			lbl_setfunds.setHeight( 20 );
 
 			txt_setfunds = new GenericTextField();
 			txt_setfunds.setAnchor( WidgetAnchor.TOP_LEFT );
 			txt_setfunds.setX( 140 );
 			txt_setfunds.setY( 40 );
 			txt_setfunds.setWidth( 100 );
 			txt_setfunds.setHeight( 20 );
 			txt_setfunds.setPlaceholder( Double.toString( shop.shop_vault ) );
 
 			btn_setfunds_update = new GenericButton( "Update" );
 			btn_setfunds_update.setAnchor( WidgetAnchor.TOP_LEFT );
 			btn_setfunds_update.setX( SCREEN_WIDTH - 180 );
 			btn_setfunds_update.setY( 40 );
 			btn_setfunds_update.setWidth( 80 );
 			btn_setfunds_update.setHeight( 20 );
 
 			attachWidgets( lbl_setfunds, txt_setfunds, btn_setfunds_update );
 		}
 
 		GenericLabel lbl_withdraw = new GenericLabel( "Withdraw" );
 		lbl_withdraw.setAnchor( WidgetAnchor.TOP_LEFT );
 		lbl_withdraw.setX( 70 );
 		lbl_withdraw.setY( 70 );
 		lbl_withdraw.setWidth( lbl_withdraw.getWidth() * 5 );
 		lbl_withdraw.setHeight( 10 );
 
 		txt_withdraw.setAnchor( WidgetAnchor.TOP_LEFT );
 		txt_withdraw.setX( 140 );
 		txt_withdraw.setY( 70 );
 		txt_withdraw.setWidth( 100 );
 		txt_withdraw.setHeight( 20 );
 		txt_withdraw.setPlaceholder( "0.00" );
 
 		btn_withdraw.setAnchor( WidgetAnchor.TOP_LEFT );
 		btn_withdraw.setX( SCREEN_WIDTH - 180 );
 		btn_withdraw.setY( 70 );
 		btn_withdraw.setWidth( 80 );
 		btn_withdraw.setHeight( 20 );
 
 		GenericLabel lbl_deposit = new GenericLabel( "Deposit" );
 		lbl_deposit.setAnchor( WidgetAnchor.TOP_LEFT );
 		lbl_deposit.setX( 70 );
 		lbl_deposit.setY( 100 );
 		lbl_deposit.setWidth( lbl_deposit.getWidth() * 5 );
 		lbl_deposit.setHeight( 10 );
 
 		txt_deposit.setAnchor( WidgetAnchor.TOP_LEFT );
 		txt_deposit.setX( 140 );
 		txt_deposit.setY( 100 );
 		txt_deposit.setWidth( 100 );
 		txt_deposit.setHeight( 20 );
 		txt_deposit.setPlaceholder( "0.00" );
 
 		btn_deposit.setAnchor( WidgetAnchor.TOP_LEFT );
 		btn_deposit.setX( SCREEN_WIDTH - 180 );
 		btn_deposit.setY( 100 );
 		btn_deposit.setWidth( 80 );
 		btn_deposit.setHeight( 20 );
 
 		attachWidgets( lbl_withdraw, txt_withdraw, btn_withdraw, lbl_deposit, txt_deposit, btn_deposit );
 	}
 
 	public void onButtonClick( ButtonClickEvent bce ) {
 		Button button = bce.getButton();
 
 		if ( !shop.isManager( player ) ) {
 			close();
 			return;
 		}
 
 		if ( button.equals( btn_setfunds_update ) ) {
 			try {
				Double amount = Double.parseDouble( format.format( txt_setfunds.getText() ) );
 
 				if ( amount < 0 ) {
 					shop.shop_vault = -1;
 					setStatus( color_green, "Infinite funds activated." );
 					return;
 				}
 
 				shop.shop_vault = amount;
 				setStatus( color_green, "Shop funds set to $" + amount );
 
 				updateFunds();
 			} catch ( NumberFormatException nfe ) {
 				setError( "Please enter only a number." );
 			}
 		}
 
 		if ( button.equals( btn_withdraw ) ) {
 			try {
				Double amount = Double.parseDouble( format.format( txt_withdraw.getText() ) );
 
 				if ( amount < 0 ) {
 					setError( "Please use deposit instead." );
 					return;
 				}
 
 				if ( shop.hasInfiniteWealth() ) {
 					setError( "The shop needs to not be in infinite wealth mode to do this." );
 					return;
 				}
 
 				if ( shop.shop_vault < amount ) {
 					setError( "The shop does not have enough funds to support this transaction." );
 					return;
 				}
 
 				EconomyResponse response = economy.depositPlayer( player.getName(), amount );
 
 				if ( response.transactionSuccess() ) {
 					shop.shop_vault -= amount;
 					setStatus( color_green, "You have withdrawn $" + amount + " from the shop." );
 					updateFunds();
 				} else {
 					setError( response.errorMessage );
 				}
 
 			} catch ( NumberFormatException nfe ) {
 				setError( "Please enter only a number." );
 			}
 		}
 
 		if ( button.equals( btn_deposit ) ) {
 			try {

				Double amount = Double.parseDouble( format.format( txt_deposit.getText() ) );
 
 				if ( amount < 0 ) {
 					setError( "Please use withdraw instead." );
 					return;
 				}
 
 				if ( shop.hasInfiniteWealth() ) {
 					setError( "The shop needs to not be in infinite wealth mode to do this." );
 					return;
 				}
 
 				EconomyResponse response = economy.withdrawPlayer( player.getName(), amount );
 
 				if ( response.transactionSuccess() ) {
 					shop.shop_vault += amount;
 					setStatus( color_green, "You have deposited $" + amount + " into the shop." );
 					updateFunds();
 				} else {
 					setError( response.errorMessage );
 				}
 			} catch ( NumberFormatException nfe ) {
 				setError( "Please enter only a number." );
 			}
 		}
 
 		super.onButtonClick( bce );
 	}
 }
