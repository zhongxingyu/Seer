 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *         research group.
  *  
  *   Copyright 2005-2010 <e-UCM> research group.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *         http://e-adventure.e-ucm.es/contributors
  * 
  *   <e-UCM> is a research group of the Department of Software Engineering
  *         and Artificial Intelligence at the Complutense University of Madrid
  *         (School of Computer Science).
  * 
  *         C Profesor Jose Garcia Santesmases sn,
  *         28040 Madrid (Madrid), Spain.
  * 
  *         For more info please visit:  <http://e-adventure.e-ucm.es> or
  *         <http://www.e-ucm.es>
  * 
  * ****************************************************************************
  * 
  * This file is part of <e-Adventure>, version 1.2.
  * 
  *     <e-Adventure> is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU Lesser General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     <e-Adventure> is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU Lesser General Public License for more details.
  * 
  *     You should have received a copy of the GNU Lesser General Public License
  *     along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.engine.core.gui.hud.traditionalhud;
 
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalElement;
 
 /**
  * This class renders all the items of the inventory
  */
 public class Inventory {
 
     /**
      * Index of the first item displayed in the inventory
      */
     private int indexFirstItemDisplayed;
 
     /**
      * Empty constructor
      */
     public Inventory( ) {
 
         indexFirstItemDisplayed = 0;
     }
 
     /**
      * Scrolls the inventory up
      */
     private void scrollInventoryUp( ) {
 
         if( indexFirstItemDisplayed > 0 ) {
             indexFirstItemDisplayed -= TraditionalHUD.INVENTORY_ITEMS_PER_LINE;
         }
     }
 
     /**
      * Scrolls the inventory down
      */
     private void scrollInventoryDown( ) {
 
         if( Game.getInstance( ).getInventory( ).getItemCount( ) > indexFirstItemDisplayed + TraditionalHUD.INVENTORY_ITEMS_PER_LINE * TraditionalHUD.INVENTORY_LINES ) {
             indexFirstItemDisplayed += TraditionalHUD.INVENTORY_ITEMS_PER_LINE;
         }
     }
 
     /**
      * Draws the inventory
      * 
      * @param g
      *            Graphics to draw the inventory
      */
     public void draw( Graphics2D g ) {
 
         int indexLastItemDisplayed;
 
         if( indexFirstItemDisplayed + TraditionalHUD.INVENTORY_LINES * TraditionalHUD.INVENTORY_ITEMS_PER_LINE > Game.getInstance( ).getInventory( ).getItemCount( ) )
             indexLastItemDisplayed = Game.getInstance( ).getInventory( ).getItemCount( );
         else
             indexLastItemDisplayed = indexFirstItemDisplayed + TraditionalHUD.INVENTORY_LINES * TraditionalHUD.INVENTORY_ITEMS_PER_LINE;
 
         int x = TraditionalHUD.FIRST_ITEM_X;
         int y = TraditionalHUD.FIRST_ITEM_Y;
         for( int i = indexFirstItemDisplayed; i < indexLastItemDisplayed; i++ ) {
             g.drawImage( Game.getInstance( ).getInventory( ).getItem( i ).getIconImage( ), x, y, null );
             if( x == TraditionalHUD.FIRST_ITEM_X + TraditionalHUD.ITEM_SPACING_X * ( TraditionalHUD.INVENTORY_ITEMS_PER_LINE - 1 ) ) {
                 x = TraditionalHUD.FIRST_ITEM_X;
                 y += TraditionalHUD.ITEM_SPACING_Y;
             }
             else
                 x += TraditionalHUD.ITEM_SPACING_X;
         }
     }
 
     /**
      * Tells the inventory that a click has been performed
      * 
      * @param x
      *            X position of the click
      * @param y
      *            Y position of the click
      */
     public void mouseClicked( MouseEvent e ) {
 
         if( e.getX( ) > TraditionalHUD.ITEMS_PANEL_X && e.getX( ) < TraditionalHUD.ITEMS_PANEL_X + TraditionalHUD.ITEMS_PANEL_WIDTH && e.getY( ) > TraditionalHUD.ITEMS_PANEL_Y && e.getY( ) < TraditionalHUD.ITEMS_PANEL_Y + TraditionalHUD.ITEMS_PANEL_HEIGHT ) {
             int indexLastItemDisplayed;
             FunctionalElement element = null;
 
             if( indexFirstItemDisplayed + TraditionalHUD.INVENTORY_LINES * TraditionalHUD.INVENTORY_ITEMS_PER_LINE > Game.getInstance( ).getInventory( ).getItemCount( ) )
                 indexLastItemDisplayed = Game.getInstance( ).getInventory( ).getItemCount( );
             else
                 indexLastItemDisplayed = indexFirstItemDisplayed + TraditionalHUD.INVENTORY_LINES * TraditionalHUD.INVENTORY_ITEMS_PER_LINE;
 
             int indexX = TraditionalHUD.FIRST_ITEM_X;
             int indexY = TraditionalHUD.FIRST_ITEM_Y;
             for( int i = indexFirstItemDisplayed; i < indexLastItemDisplayed; i++ ) {
 
                 if( indexX < e.getX( ) && e.getX( ) < indexX + TraditionalHUD.ITEM_WIDTH && indexY < e.getY( ) && e.getY( ) < indexY + TraditionalHUD.ITEM_HEIGHT )
                     element = Game.getInstance( ).getInventory( ).getItem( i );
 
                 if( indexX == TraditionalHUD.FIRST_ITEM_X + TraditionalHUD.ITEM_SPACING_X * ( TraditionalHUD.INVENTORY_ITEMS_PER_LINE - 1 ) ) {
                     indexX = TraditionalHUD.FIRST_ITEM_X;
                     indexY += TraditionalHUD.ITEM_SPACING_Y;
                 }
                 else
                     indexX += TraditionalHUD.ITEM_SPACING_X;
             }
 
             if( element != null )
                 Game.getInstance( ).getFunctionalPlayer( ).performActionInElement( element );
         }
         else {
             if( e.getY( ) > TraditionalHUD.SCROLL_DOWN_Y && e.getY( ) < TraditionalHUD.SCROLL_DOWN_Y + TraditionalHUD.SCROLL_HEIGHT && e.getX( ) > TraditionalHUD.SCROLL_DOWN_X && e.getX( ) < TraditionalHUD.SCROLL_DOWN_X + TraditionalHUD.SCROLL_WIDTH )
                 scrollInventoryDown( );
             else if( e.getY( ) > TraditionalHUD.SCROLL_UP_Y && e.getY( ) < TraditionalHUD.SCROLL_UP_Y + TraditionalHUD.SCROLL_HEIGHT && e.getX( ) > TraditionalHUD.SCROLL_UP_X && e.getX( ) < TraditionalHUD.SCROLL_UP_X + TraditionalHUD.SCROLL_WIDTH )
                 scrollInventoryUp( );
         }
 
     }
 
     /**
      * Tells the mouse has been moved over the inventory
      * 
      * @param x
      *            X position of the click
      * @param y
      *            Y position of the click
      */
     public void mouseMoved( MouseEvent e ) {
 
         int indexLastItemDisplayed;
         FunctionalElement element = null;
 
         if( indexFirstItemDisplayed + TraditionalHUD.INVENTORY_LINES * TraditionalHUD.INVENTORY_ITEMS_PER_LINE > Game.getInstance( ).getInventory( ).getItemCount( ) )
             indexLastItemDisplayed = Game.getInstance( ).getInventory( ).getItemCount( );
         else
             indexLastItemDisplayed = indexFirstItemDisplayed + TraditionalHUD.INVENTORY_LINES * TraditionalHUD.INVENTORY_ITEMS_PER_LINE;
 
         int indexX = TraditionalHUD.FIRST_ITEM_X;
         int indexY = TraditionalHUD.FIRST_ITEM_Y;
         for( int i = indexFirstItemDisplayed; i < indexLastItemDisplayed; i++ ) {
 
             if( indexX < e.getX( ) && e.getX( ) < indexX + TraditionalHUD.ITEM_WIDTH && indexY < e.getY( ) && e.getY( ) < indexY + TraditionalHUD.ITEM_HEIGHT )
                 element = Game.getInstance( ).getInventory( ).getItem( i );
 
             if( indexX == TraditionalHUD.FIRST_ITEM_X + TraditionalHUD.ITEM_SPACING_X * ( TraditionalHUD.INVENTORY_ITEMS_PER_LINE - 1 ) ) {
                 indexX = TraditionalHUD.FIRST_ITEM_X;
                 indexY += TraditionalHUD.ITEM_SPACING_Y;
             }
             else
                 indexX += TraditionalHUD.ITEM_SPACING_X;
         }
 
        if( element != null )
            Game.getInstance( ).getActionManager( ).setElementOver( element );
        else
            Game.getInstance( ).getActionManager( ).setElementOver( null );
     }
 }
