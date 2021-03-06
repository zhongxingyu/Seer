 /*  GASdotto 0.1
  *  Copyright (C) 2008 Roberto -MadBob- Guido <madbob@users.barberaware.org>
  *
  *  This is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.barberaware.client;
 
 import java.util.*;
 import java.lang.*;
 import com.google.gwt.user.client.ui.*;
 
 public class PercentageBox extends TextBox {
 	private boolean		valid;
 
 	public PercentageBox () {
 		valid = true;
 		setText ( "0" );
 
 		addFocusListener (
 			new FocusListener () {
 				public void onFocus ( Widget sender ) {
 				}
 
 				public void onLostFocus ( Widget sender ) {
 					String text;
 
 					text = getText ();
 					valid = true;
 
 					if ( text.equals ( "" ) )
 						setText ( "0" );
 
 					else {
 						int len;
 
 						len = text.length ();
 
 						for ( int i = 0; i < len; i++ )
 							if ( text.charAt ( i ) == '%' ) {
 								if ( i != len - 1 ) {
 									Utils.showNotification ( "Valore non valido" );
 									valid = false;
 								}
 
 								break;
 							}
 					}
 				}
 			}
 		);
 
 		addKeyboardListener (
 			new KeyboardListenerAdapter () {
 				public void onKeyPress ( Widget sender, char keyCode, int modifiers ) {
 					if ( ( !Character.isDigit ( keyCode ) ) &&
 							( keyCode != KeyboardListener.KEY_TAB ) &&
 							( keyCode != KeyboardListener.KEY_BACKSPACE ) &&
 							( keyCode != KeyboardListener.KEY_LEFT ) &&
 							( keyCode != KeyboardListener.KEY_UP ) &&
 							( keyCode != KeyboardListener.KEY_RIGHT ) &&
 							( keyCode != KeyboardListener.KEY_DOWN ) &&
							( keyCode != '.' ) &&
 							( keyCode != '%' ) ) {
 
 						cancelKey ();
 					}
 				}
 			}
 		);
 
 		setVisibleLength ( 6 );
 	}
 
 	public void setValue ( String value ) {
 		setText ( value );
 	}
 
 	public String getValue () {
 		if ( valid == true )
 			return getText ();
 		else
 			return "0";
 	}
 }
