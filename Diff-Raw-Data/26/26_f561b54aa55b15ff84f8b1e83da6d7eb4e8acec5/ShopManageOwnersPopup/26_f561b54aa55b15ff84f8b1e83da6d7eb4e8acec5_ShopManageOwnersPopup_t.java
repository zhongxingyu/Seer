 
 package org.ss.gui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.gui.Button;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 import org.ss.shop.Shop;
 
 public class ShopManageOwnersPopup
 		extends ShopPopup {
 	private int range_start = -1;
 	private int range_end = -1;
 	private boolean forward_scan;
 	private int scan_index;
 	private String search;
 
 	private ArrayList< String > managers = new ArrayList< String >();
 	private ArrayList< GenericButton > remove_buttons = new ArrayList< GenericButton >();
 
 	private GenericButton btn_prev = new GenericButton( "Previous" );
 	private GenericButton btn_next = new GenericButton( "Next" );
 	private GenericTextField txt_search = new GenericTextField();
 	private GenericButton btn_search = new GenericButton( "Search" );
 
 	private GenericTextField txt_add = new GenericTextField();
 	private GenericButton btn_add = new GenericButton( "Add" );
 
 	public ShopManageOwnersPopup( SpoutPlayer p, Shop s ) {
 		this( p, s, 0, true, null );
 	}
 
 	public ShopManageOwnersPopup( SpoutPlayer p, Shop s, int p_scan_index, boolean p_forward_scan, String p_search ) {
 		super( "Owners", p, s );
 
 		if ( !shop.isManager( player ) ) {
 			close();
 			return;
 		}
 
 		this.scan_index = p_scan_index;
 		this.forward_scan = p_forward_scan;
 		this.search = p_search;
 
 		btn_prev.setAnchor( WidgetAnchor.TOP_LEFT );
 		btn_prev.setX( SCREEN_WIDTH - 90 );
 		btn_prev.setY( 100 );
 		btn_prev.setWidth( 80 );
 		btn_prev.setHeight( 20 );
 		btn_prev.setEnabled( false );
 
 		btn_next.setAnchor( WidgetAnchor.TOP_LEFT );
 		btn_next.setX( SCREEN_WIDTH - 90 );
 		btn_next.setY( 130 );
 		btn_next.setWidth( 80 );
 		btn_next.setHeight( 20 );
 		btn_next.setEnabled( false );
 
 		txt_search.setAnchor( WidgetAnchor.TOP_LEFT );
 		txt_search.setX( 80 );
 		txt_search.setY( SCREEN_HEIGHT - 35 );
 		txt_search.setWidth( 160 );
 		txt_search.setHeight( 20 );
 		if ( p_search != null )
 			txt_search.setText( p_search );
 		else
 			txt_search.setPlaceholder( "Quick Search" );
 
 		btn_search.setAnchor( WidgetAnchor.TOP_LEFT );
 		btn_search.setX( SCREEN_WIDTH - 180 );
 		btn_search.setY( SCREEN_HEIGHT - 35 );
 		btn_search.setWidth( 80 );
 		btn_search.setHeight( 20 );
 
 		txt_add.setAnchor( WidgetAnchor.TOP_LEFT );
 		txt_add.setX( 80 );
 		txt_add.setY( SCREEN_HEIGHT - 65 );
 		txt_add.setWidth( 160 );
 		txt_add.setHeight( 20 );
 		txt_add.setPlaceholder( "Add Owner" );
 
 		btn_add.setAnchor( WidgetAnchor.TOP_LEFT );
 		btn_add.setX( SCREEN_WIDTH - 180 );
 		btn_add.setY( SCREEN_HEIGHT - 65 );
 		btn_add.setWidth( 80 );
 		btn_add.setHeight( 20 );
 
 		attachWidgets( btn_prev, btn_next, txt_search, btn_search, txt_add, btn_add );
 
 		int mod = forward_scan ? 1 : -1;
 		int found_entries = 0;
 		while ( found_entries < 4 ) {
 			if ( p_forward_scan ) {
 				if ( scan_index == s.owners.size() )
 					break;
 			} else {
 				if ( scan_index < 0 )
 					break;
 			}
 
 			if ( range_start == -1 )
 				range_start = scan_index;
 			else
 				range_end = scan_index;
 
 			String manager = shop.owners.get( scan_index );
 
 			if ( search != null && !manager.contains( search.toLowerCase() ) ) {
 				scan_index += mod;
 				continue;
 			}
 
 			scan_index += mod;
 			found_entries++;
 			managers.add( manager );
 		}
 
 		if ( !forward_scan )
 			Collections.reverse( managers );
 
 		int y_start = 10;
 		for ( String manager : managers ) {
 			GenericLabel lbl_manager = new GenericLabel( manager );
			lbl_manager.setAnchor( WidgetAnchor.TOP_LEFT );
 			lbl_manager.setX( 80 );
 			lbl_manager.setY( y_start += 30 );
 			lbl_manager.setWidth( lbl_manager.getText().length() * 5 );
 			lbl_manager.setHeight( 10 );
 
 			GenericButton btn_remove = new GenericButton( "Remove" );
 			btn_remove.setAnchor( WidgetAnchor.TOP_LEFT );
 			btn_remove.setX( SCREEN_WIDTH - 180 );
 			btn_remove.setY( y_start );
 			btn_remove.setWidth( 80 );
 			btn_remove.setHeight( 20 );
 
 			if ( manager.equalsIgnoreCase( player.getName() ) ) {
 				btn_remove.setEnabled( false );
 				btn_remove.setText( "Self" );
 			}
 
 			remove_buttons.add( btn_remove );
 
 			attachWidgets( lbl_manager, btn_remove );
 		}
 
 		if ( forward_scan ) {
 			if ( p_scan_index != 0 && scan_index != 0 )
 				btn_prev.setEnabled( true );
 		} else {
 			if ( range_end != 0 )
 				btn_prev.setEnabled( true );
 		}
 	}
 
 	public void onButtonClick( ButtonClickEvent bce ) {
 		Button button = bce.getButton();
 
 		if ( !shop.isManager( player ) ) {
 			close();
 			return;
 		}
 
 		if ( button.equals( btn_search ) ) {
 			if ( txt_search.getText() != null )
 				new ShopManageOwnersPopup( player, shop, 0, true, txt_search.getText() ).show();
 			else
 				setError( "Please enter a search term first." );
 
 			return;
 		}
 
 		if ( button.equals( btn_add ) ) {
 			String name = txt_add.getText();
 
 			if ( name == null || name.length() == 0 ) {
 				setError( "Please enter a name first." );
 				return;
 			}
 
 			if ( shop.owners.contains( name.toLowerCase() ) ) {
 				setError( "They are already an owner." );
 				return;
 			} else {
 				shop.owners.add( name.toLowerCase() );
 				setStatus( color_green, "They are now an owner." );
 				return;
 			}
 		}
 
 		if ( button.equals( btn_prev ) ) {
 			new ShopManageOwnersPopup( player, shop, ( forward_scan ? range_start : range_end ) - 1, false, search )
 					.show();
 			return;
 		}
 
 		if ( button.equals( btn_next ) ) {
 			new ShopManageOwnersPopup( player, shop, ( forward_scan ? range_end : range_start ) + 1, true, search )
 					.show();
 			return;
 		}
 
 		for ( int index = 0; index < remove_buttons.size(); index++ ) {
 			if ( button.equals( remove_buttons.get( index ) ) ) {
 				shop.owners.remove( managers.get( index ) );
 
 				button.setEnabled( false );
 				button.setText( "Removed" );
 
 				return;
 			}
 		}
 
 		super.onButtonClick( bce );
 	}
 }
