 
 package org.ss.gui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map;
 
 import org.bukkit.enchantments.Enchantment;
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.gui.Button;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericItemWidget;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.player.SpoutPlayer;
 import org.ss.other.SSEnchantment;
 import org.ss.shop.Shop;
 import org.ss.shop.ShopEntry;
 
 public class ShopLookupItemPopup
 		extends ShopPopup {
 
 	private int range_start = -1;
 	private int range_end = -1;
 	private boolean forward_scan;
 	private int scan_index;
 	private String search;
 
 	private GenericButton btn_prev;
 	private GenericButton btn_next;
 	private GenericTextField txt_search = new GenericTextField();
 	private GenericButton btn_search = new GenericButton( "Search" );
 
 	private ArrayList< ShopEntry > shop_entries = new ArrayList< ShopEntry >();
 	private ArrayList< GenericButton > modify_entry_buttons = new ArrayList< GenericButton >();
 
 	public ShopLookupItemPopup( SpoutPlayer player, Shop shop ) {
 		this( player, shop, 0 );
 	}
 
 	public ShopLookupItemPopup( SpoutPlayer player, Shop shop, int scan_index_param ) {
 		this( player, shop, scan_index_param, true );
 	}
 
 	public ShopLookupItemPopup( SpoutPlayer player, Shop shop, int p_scan_index, boolean p_forward_scan ) {
 		this( player, shop, p_scan_index, p_forward_scan, null );
 	}
 
 	public ShopLookupItemPopup( SpoutPlayer p, Shop s, int p_scan_index, boolean p_forward_scan, String p_search ) {
 		super( "Shop: Lookup", p, s );
 
 		if ( !shop.isManager( player ) ) {
 			close();
 			return;
 		}
 
 		this.forward_scan = p_forward_scan;
 		this.scan_index = p_scan_index;
 		this.search = p_search;
 
 		txt_search.setAnchor( WidgetAnchor.TOP_LEFT );
 		txt_search.setX( 80 );
 		txt_search.setY( 10 );
 		txt_search.setWidth( 140 );
 		txt_search.setHeight( 20 );
 		txt_search.setPlaceholder( "Quick Search" );
 		if ( p_search != null )
 			txt_search.setPlaceholder( p_search );
 
 		btn_search.setAnchor( WidgetAnchor.TOP_LEFT );
 		btn_search.setX( SCREEN_WIDTH - 180 );
 		btn_search.setY( 10 );
 		btn_search.setWidth( 80 );
 		btn_search.setHeight( 20 );
 		attachWidgets( txt_search, btn_search );
 
 		btn_prev = new GenericButton( "Previous" );
 		btn_prev.setAnchor( WidgetAnchor.TOP_LEFT );
 		btn_prev.setX( SCREEN_WIDTH - 90 );
 		btn_prev.setY( 100 );
 		btn_prev.setWidth( 80 );
 		btn_prev.setHeight( 20 );
 		btn_prev.setEnabled( false );
 
 		btn_next = new GenericButton( "Next" );
 		btn_next.setAnchor( WidgetAnchor.TOP_LEFT );
 		btn_next.setX( SCREEN_WIDTH - 90 );
 		btn_next.setY( 130 );
 		btn_next.setWidth( 80 );
 		btn_next.setHeight( 20 );
 		btn_next.setEnabled( false );
 
 		attachWidgets( btn_prev, btn_next );
 
 		int mod = forward_scan ? 1 : -1;
 		int found_entries = 0;
 
 		while ( found_entries < 5 ) {
 			if ( p_forward_scan ) {
 				if ( scan_index == s.shop_entries.size() )
 					break;
 			} else {
 				if ( scan_index < 0 )
 					break;
 			}
 
 			ShopEntry entry = s.shop_entries.get( scan_index );
 
 			if ( p_search != null && !entry.matchesString( p_search ) ) {
 				scan_index += mod;
 
 				continue;
 			}
 
 			if ( range_start == -1 )
 				range_start = scan_index;
 			else
 				range_end = scan_index;
 
 			found_entries++;
 			shop_entries.add( entry );
 
 			scan_index += mod;
 		}
 
 		if ( !p_forward_scan )
 			Collections.reverse( shop_entries );
 
 		int y_start = 10;
 		for ( ShopEntry entry : shop_entries ) {
 			SpoutItemStack sis = entry.createItemStack();
 
 			GenericButton btn_modify = new GenericButton( "Modify" );
 			btn_modify.setAnchor( WidgetAnchor.TOP_LEFT );
 			btn_modify.setX( SCREEN_WIDTH - 180 );
 			btn_modify.setY( y_start += 30 );
 			btn_modify.setWidth( 80 );
 			btn_modify.setHeight( 20 );
 			modify_entry_buttons.add( btn_modify );
 
 			StringBuilder builder = new StringBuilder( sis.getMaterial().getNotchianName() );
 
 			Map< Enchantment, Integer > enchantments = sis.getEnchantments();
 			if ( enchantments.size() != 0 ) {
 				for ( Enchantment enc : enchantments.keySet() ) {
 					builder.append( "\n" + SSEnchantment.lookup( enc.getId() ) + ": Level " + enchantments.get( enc ) );
 				}
 			}
 
 			GenericItemWidget display = new GenericItemWidget( sis );
			display.setAnchor( WidgetAnchor.TOP_LEFT );
			display.setFixed( true );
 			display.setX( 80 );
 			display.setY( y_start );
 			display.setWidth( 10 );
 			display.setHeight( 10 );
 			display.setTooltip( builder.toString() );
 
 			GenericLabel lbl_stock = new GenericLabel();
			lbl_stock.setAnchor( WidgetAnchor.TOP_LEFT );
 			if ( entry.hasInfiniteStock() )
 				lbl_stock.setText( "Infinite Stock" );
 			else {
 				if ( entry.units_in_stock < 10000 )
 					lbl_stock.setText( Integer.toString( entry.units_in_stock ) + " Units" );
 				else
 					lbl_stock.setText( ">9999 Units" );
 			}
 			lbl_stock.setX( 110 );
 			lbl_stock.setY( y_start );
 			lbl_stock.setWidth( lbl_stock.getText().length() * 5 );
 			lbl_stock.setHeight( 10 );
 
 			attachWidgets( btn_modify, display, lbl_stock );
 		}
 
 		if ( forward_scan ) {
 			if ( p_scan_index != 0 && scan_index != 0 )
 				btn_prev.setEnabled( true );
 		} else {
 			if ( range_end != 0 )
 				btn_prev.setEnabled( true );
 		}
 
 		if ( found_entries == 5 ) {
 			int end = p_forward_scan ? range_end : range_start;
 
 			if ( end < s.shop_entries.size() - 1 )
 				btn_next.setEnabled( true );
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
 				new ShopLookupItemPopup( player, shop, 0, true, txt_search.getText() ).show();
 			else
 				setError( "Please enter a search term first." );
 
 			return;
 		}
 
 		if ( button.equals( btn_prev ) ) {
 			new ShopLookupItemPopup( player, shop, ( forward_scan ? range_start : range_end ) - 1, false, search )
 					.show();
 			return;
 		}
 
 		if ( button.equals( btn_next ) ) {
 			new ShopLookupItemPopup( player, shop, ( forward_scan ? range_end : range_start ) + 1, true, search )
 					.show();
 			return;
 		}
 
 		if ( modify_entry_buttons != null )
 			for ( int index = 0; index < modify_entry_buttons.size(); index++ ) {
 				if ( !button.equals( modify_entry_buttons.get( index ) ) )
 					continue;
 
 				new ShopModifyItemPopup( player, shop, shop_entries.get( index ) ).show();
 				return;
 			}
 
 		super.onButtonClick( bce );
 	}
 }
