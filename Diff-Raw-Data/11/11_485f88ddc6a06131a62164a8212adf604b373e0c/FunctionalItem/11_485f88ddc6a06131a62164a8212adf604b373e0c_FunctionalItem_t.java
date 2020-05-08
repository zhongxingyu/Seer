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
 package es.eucm.eadventure.engine.core.control.functionaldata;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Transparency;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 
 import es.eucm.eadventure.common.data.chapter.Action;
 import es.eucm.eadventure.common.data.chapter.CustomAction;
 import es.eucm.eadventure.common.data.chapter.ElementReference;
 import es.eucm.eadventure.common.data.chapter.InfluenceArea;
 import es.eucm.eadventure.common.data.chapter.elements.Element;
 import es.eucm.eadventure.common.data.chapter.elements.Item;
 import es.eucm.eadventure.common.data.chapter.resources.Asset;
 import es.eucm.eadventure.common.data.chapter.resources.Resources;
 import es.eucm.eadventure.engine.core.control.ActionManager;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionaleffects.FunctionalEffects;
 import es.eucm.eadventure.engine.core.gui.GUI;
 import es.eucm.eadventure.engine.multimedia.MultimediaManager;
 import es.eucm.eadventure.engine.resourcehandler.ResourceHandler;
 
 /**
  * An object in the game
  */
 public class FunctionalItem extends FunctionalElement {
 
     /**
      * Resources being used in the item
      */
     protected Resources resources;
 
     /**
      * Image of the item, to display in the scene
      */
     protected Image image;
 
     /**
      * Image of the item, to display on the inventory
      */
     private Image icon;
 
     /**
      * Item containing the data
      */
     protected Item item;
 
     protected InfluenceArea influenceArea;
 
     private Image oldOriginalImage = null;
 
     private Image oldImage = null;
     
     private int width, height;
     
     private int x1, y1, x2, y2;
 
     private float oldScale = -1;
 
     private ElementReference reference;
 
     public FunctionalItem( Item item, ElementReference reference ) {
         this( item, reference.getInfluenceArea( ), reference.getX( ), reference.getY( ) );
         this.scale = reference.getScale( );
         this.layer = reference.getLayer( );
         this.reference = reference;
     }
 
     /**
      * Creates a new FunctionalItem
      * 
      * @param item
      *            the item's data
      * @param x
      *            the item's horizontal position
      * @param y
      *            the item's vertical position
      */
     protected FunctionalItem( Item item, InfluenceArea influenceArea, int x, int y ) {
         super( x, y );
         this.item = item;
         this.influenceArea = influenceArea;
         Image tempimage = null;
         
         image = null;
         icon = null;
 
         resources = createResourcesBlock( );
 
         // Load the resources
         MultimediaManager multimediaManager = MultimediaManager.getInstance( );
         if( resources.existAsset( Item.RESOURCE_TYPE_IMAGE ) ) {
             tempimage = multimediaManager.loadImageFromZip( resources.getAssetPath( Item.RESOURCE_TYPE_IMAGE ), MultimediaManager.IMAGE_SCENE );
             removeTransparentParts(tempimage);
             tempimage = null;
             Runtime.getRuntime( ).gc( );
         }
         if( resources.existAsset( Item.RESOURCE_TYPE_ICON ) )
             icon = multimediaManager.loadImageFromZip( resources.getAssetPath( Item.RESOURCE_TYPE_ICON ), MultimediaManager.IMAGE_SCENE );
     }
 
     private void removeTransparentParts(Image tempimage) {
         x1 = tempimage.getWidth( null ); y1 = tempimage.getHeight( null ); x2 = 0; y2 = 0;
         width = x1;
         height = y1;
         for (int i = 0; i < tempimage.getWidth( null ); i++) {
             boolean x_clear = true;
             for (int j = 0; j < tempimage.getHeight( null ); j++) {
                 boolean y_clear = true;
                 BufferedImage bufferedImage = (BufferedImage) tempimage;
                 int alpha = bufferedImage.getRGB( i, j ) >>> 24;
         
                 //OLD VALUE: 128
                 if (alpha > 0) {
                     if (x_clear)
                         x1 = Math.min( x1, i );
                     if (y_clear)
                         y1 = Math.min( y1, j );
                     x_clear = false;
                     y_clear = false;
                     x2 = Math.max( x2, i );
                     y2 = Math.max( y2, j );
                 }
             }
         }
         
        // create a transparent (not translucent) image 
        //NEW! check the parameter is grater than 0, if not, pass the width of the image. This allow introduce completely transparent
        //images
        image = GUI.getInstance( ).getGraphicsConfiguration( ).createCompatibleImage( x2-x1>0?x2-x1:x1, y2-y1>0?y2-y1:y1, Transparency.TRANSLUCENT );
 
         // draw the transformed image
         Graphics2D g = (Graphics2D) image.getGraphics( );
 
        //NEW! check the parameter is grater than 0, if not, pass the width of the image. This allow introduce completely transparent
        //images
        g.drawImage( tempimage, 0, 0, x2-x1>0?x2-x1:x1, y2-y1>0?y2-y1:y1, x1, y1, x2, y2, null);
 //        g.drawImage( image, transform, null );
         g.dispose( );
         
     }
 
     /**
      * Creates a new FunctionalItem at position (0, 0)
      * 
      * @param item
      *            the item's data
      */
     public FunctionalItem( Item item, InfluenceArea influenceArea ) {
 
         this( item, influenceArea, 0, 0 );
     }
 
     /**
      * Updates the resources of the icon (if the current resources and the new
      * one are different)
      */
     public void updateResources( ) {
 
         // Get the new resources
         Resources newResources = createResourcesBlock( );
         
         // If the resources have changed, load the new one
         if( resources != newResources ) {
             resources = newResources;
 
             // Load the resources
             MultimediaManager multimediaManager = MultimediaManager.getInstance( );
             Image tempimage = null;
             if( resources.existAsset( Item.RESOURCE_TYPE_IMAGE ) ) {
                 tempimage = multimediaManager.loadImageFromZip( resources.getAssetPath( Item.RESOURCE_TYPE_IMAGE ), MultimediaManager.IMAGE_SCENE );
                 removeTransparentParts(tempimage);
                 tempimage = null;
                 Runtime.getRuntime( ).gc( );
             }
             if( resources.existAsset( Item.RESOURCE_TYPE_ICON ) )
                 icon = multimediaManager.loadImageFromZip( resources.getAssetPath( Item.RESOURCE_TYPE_ICON ), MultimediaManager.IMAGE_SCENE );
         }
     }
 
     /**
      * Returns this item's data
      * 
      * @return this item's data
      */
     public Item getItem( ) {
 
         return item;
     }
 
     /**
      * Returns this item's icon image
      * 
      * @return this item's icon image
      */
     public Image getIconImage( ) {
 
         return icon;
     }
 
     @Override
     public Element getElement( ) {
 
         return item;
     }
 
     @Override
     public int getWidth( ) {
 
         return width;//image.getWidth( null );
     }
 
     @Override
     public int getHeight( ) {
 
         return height;//image.getHeight( null );
     }
 
     /*
      *  (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.control.functionaldata.Renderable#update(long)
      */
     public void update( long elapsedTime ) {
 
         // Do nothing
     }
 
     /*
      *  (non-Javadoc)
      * @see es.eucm.eadventure.engine.core.control.functionaldata.Renderable#draw(java.awt.Graphics2D)
      */
     public void draw( ) {
         int x_image = Math.round( (x + x1 * scale) - ( getWidth( ) * scale / 2 ) ) - Game.getInstance( ).getFunctionalScene( ).getOffsetX( );
         int y_image = Math.round( (y + y1 * scale) - getHeight( ) * scale );
         if( scale != 1 ) {
             Image temp;
             if( image == oldOriginalImage && scale == oldScale ) {
                 temp = oldImage;
             }
             else {
                 temp = GUI.getInstance( ).getGraphicsConfiguration( ).createCompatibleImage( Math.round( image.getWidth( null ) * scale ),  Math.round( image.getHeight( null ) * scale ), Transparency.TRANSLUCENT );
                 ((Graphics2D) temp.getGraphics( )).drawImage( image, AffineTransform.getScaleInstance( scale, scale ), null );
 
                 oldImage = temp;
                 oldOriginalImage = image;
                 oldScale = scale;
             }
             if( layer == -1 )
                 GUI.getInstance( ).addElementToDraw( temp, x_image, y_image, Math.round( y ), Math.round( y ), highlight, this );
             else
                 GUI.getInstance( ).addElementToDraw( temp, x_image, y_image, layer, Math.round( y ), highlight, this );
         }
         else if( layer == -1 )
             GUI.getInstance( ).addElementToDraw( image, x_image, y_image, Math.round( y ), Math.round( y ), highlight, this  );
         else
             GUI.getInstance( ).addElementToDraw( image, x_image, y_image, layer, Math.round( y ), highlight, this  );
     }
 
     @Override
     public boolean isPointInside( float x, float y ) {
         boolean isInside = false;
 
         int mousex = (int) ( x - ( this.x - getWidth( ) * scale / 2 ) );
         int mousey = (int) ( y - ( this.y - getHeight( ) * scale ) );
         
         if (mousex < x1 * scale || mousey < y1 * scale || mousex >= x2 * scale || mousey >= y2 * scale)
             return false;
         mousex = mousex - (int) (x1 * scale);
         mousey = mousey - (int) (y1 * scale);
 
         if( ( mousex >= 0 ) && ( mousex < (x2 - x1) * scale ) && ( mousey >= 0 ) && ( mousey < (y2 - y1) * scale ) ) {
             BufferedImage bufferedImage = (BufferedImage) image;
             int alpha = bufferedImage.getRGB( (int) ( mousex / scale ), (int) ( mousey / scale ) ) >>> 24;
             isInside = alpha > 128;
         }
 
         return isInside;
     }
 
     @Override
     public boolean canPerform( int action ) {
         boolean canPerform = false;
 
         // The item can't be given to, nor talked to
         switch( action ) {
             case ActionManager.ACTION_LOOK:
             case ActionManager.ACTION_GRAB:
             case ActionManager.ACTION_EXAMINE:
             case ActionManager.ACTION_USE:
             case ActionManager.ACTION_GIVE:
             case ActionManager.ACTION_GOTO:
             case ActionManager.ACTION_USE_WITH:
             case ActionManager.ACTION_CUSTOM:
             case ActionManager.ACTION_CUSTOM_INTERACT:
                 canPerform = true;
                 break;
             case ActionManager.ACTION_GIVE_TO:
             case ActionManager.ACTION_TALK:
                 canPerform = false;
                 break;
         }
 
         return canPerform;
     }
 
     @Override
     public boolean isInInventory( ) {
 
         return Game.getInstance( ).getItemSummary( ).isItemGrabbed( item.getId( ) );
     }
 
     @Override
     public boolean examine( ) {
 
         boolean examined = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < item.getActions( ).size( ) && !examined; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.EXAMINE ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     examined = true;
                 }
             }
         }
 
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( int i = 0; i < item.getActions( ).size( ) && !examined; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.EXAMINE ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     examined = true;
                 }
             }
         }
         return examined;
     }
 
     @Override
     public boolean canBeUsedAlone( ) {
 
         boolean canBeUsedAlone = false;
         Action grabAction = getFirstValidAction( Action.GRAB );
         Action useWithAction = getFirstValidAction( Action.USE_WITH );
         Action giveToAction = getFirstValidAction( Action.GIVE_TO );
         // if the conditions are not met but there are a grabAction, useWith or giveTo defined for this item and its conditions are met, can't be use alone
         boolean grabActionPartial;
         if (grabAction == null)
             grabActionPartial = true;
         else 
             grabActionPartial = !grabAction.isConditionsAreMeet( );
         boolean useWithActionPartial;
         if (useWithAction == null)
             useWithActionPartial = true;
         else 
             useWithActionPartial = !useWithAction.isConditionsAreMeet( );
         boolean giveToActionPartial;
         if (giveToAction == null)
             giveToActionPartial = true;
         else 
             giveToActionPartial = !giveToAction.isConditionsAreMeet( );
         boolean ifUseNotMeetConditionsNeitherOther =  grabActionPartial && useWithActionPartial && giveToActionPartial;
                                                       
         // Check only for one valid use action
         for( int i = 0; i < item.getActions( ).size( ) && !canBeUsedAlone; i++ ) {
             Action action = item.getAction( i );
             
             if( action.getType( ) == Action.USE ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     canBeUsedAlone = true;   
                 } else if (action.isActivatedNotEffects( ) && ifUseNotMeetConditionsNeitherOther)
                     canBeUsedAlone = true;
                 
              }
         }
 
         return canBeUsedAlone;
     }
     
     @Override
     public boolean canBeDragged() {
         boolean canBeDragged = false;
         for (int i = 0; i < item.getActions().size( ) && !canBeDragged; i++) {
             Action action = item.getAction( i );
             if (action.getType( ) == Action.DRAG_TO) {
                 if ( new FunctionalConditions( action.getConditions( )).allConditionsOk( )){
                     canBeDragged = true;
                 } else if (action.isActivatedNotEffects( ))
                     canBeDragged = true;
             }
         }
         return canBeDragged;
     }
 
     
     
     @Override
     public Action getFirstValidAction( int actionType ) {
 
         for( Action action : item.getActions( ) ) {
             if( action.getType( ) == actionType ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     action.setConditionsAreMeet( true );
                     return action;
                 }
             }
         }
 
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( Action action : item.getActions( ) ) {
             if( action.getType( ) == actionType ) {
                 if( action.isActivatedNotEffects( ) ) {
                     action.setConditionsAreMeet( false );
                     return action;
                 }
             }
         }
         return null;
     }
 
     @Override
     public CustomAction getFirstValidCustomAction( String actionName ) {
 
         for( Action action : item.getActions( ) ) {
             if( action.getType( ) == Action.CUSTOM && ( (CustomAction) action ).getName( ).equals( actionName ) ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     return (CustomAction) action;
                 }
             }
         }
 
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( Action action : item.getActions( ) ) {
             if( action.getType( ) == Action.CUSTOM && ( (CustomAction) action ).getName( ).equals( actionName ) ) {
                 if( action.isActivatedNotEffects( ) ) {
                     return (CustomAction) action;
                 }
             }
         }
 
         return null;
     }
 
     @Override
     public CustomAction getFirstValidCustomInteraction( String actionName ) {
 
         for( Action action : item.getActions( ) ) {
             if( action.getType( ) == Action.CUSTOM_INTERACT && ( (CustomAction) action ).getName( ).endsWith( actionName ) ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     return (CustomAction) action;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( Action action : item.getActions( ) ) {
             if( action.getType( ) == Action.CUSTOM_INTERACT && ( (CustomAction) action ).getName( ).endsWith( actionName ) ) {
                 if( action.isActivatedNotEffects( ) ) {
                     return (CustomAction) action;
                 }
             }
         }
         return null;
     }
 
     /**
      * Triggers the grabbing action associated with the item
      * 
      * @return True if the item was grabbed, false otherwise
      */
     public boolean grab( ) {
 
         boolean grabbed = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < item.getActions( ).size( ) && !grabbed; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.GRAB ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // If the it has not a cancel action, grab the item
                     if( !action.getEffects( ).hasCancelAction( ) )
                         Game.getInstance( ).grabItem( item.getId( ) );
 
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     grabbed = true;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( int i = 0; i < item.getActions( ).size( ) && !grabbed; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.GRAB ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // When not effects are active, the item hasn't be grabbed
                     //if( !action.getEffects( ).hasCancelAction( ) )
                       //  Game.getInstance( ).grabItem( item.getId( ) );
 
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     grabbed = true;
                 }
             }
         }
         return grabbed;
     }
 
     /**
      * Triggers the use action associated with the item
      * 
      * @return True if the item was used, false otherwise
      */
     @Override
     public boolean use( ) {
 
         boolean used = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < item.getActions( ).size( ) && !used; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.USE ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     used = true;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( int i = 0; i < item.getActions( ).size( ) && !used; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.USE ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     used = true;
                 }
             }
         }
         return used;
     }
 
     public boolean custom( String actionName ) {
 
         boolean custom = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < item.getActions( ).size( ) && !custom; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.CUSTOM && ( (CustomAction) action ).getName( ).equals( actionName ) ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     custom = true;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( int i = 0; i < item.getActions( ).size( ) && !custom; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.CUSTOM && ( (CustomAction) action ).getName( ).equals( actionName ) ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     custom = true;
                 }
             }
         }
         return custom;
     }
 
     /**
      * Triggers the using with action associated with the item
      * 
      * @param anotherItem
      *            The second item necessary for the use with action
      * @return True if the items were used, false otherwise
      */
     public boolean useWith( FunctionalItem anotherItem ) {
 
         boolean usedWith = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < item.getActions( ).size( ) && !usedWith; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.USE_WITH && action.getTargetId( ).equals( anotherItem.getItem( ).getId( ) ) ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     usedWith = true;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( int i = 0; i < item.getActions( ).size( ) && !usedWith; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.USE_WITH && action.getTargetId( ).equals( anotherItem.getItem( ).getId( ) ) ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     usedWith = true;
                 }
             }
         }
         return usedWith;
     }
 
     /**
      * Triggers the drag to action associated with the item
      * 
      * @param anotherItem
      *            The second item necessary for the use with action
      * @return True if the items were used, false otherwise
      */
     public boolean dragTo( FunctionalItem anotherItem ) {
         boolean dragTo = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < item.getActions( ).size( ) && !dragTo; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.DRAG_TO && action.getTargetId( ).equals( anotherItem.getItem( ).getId( ) ) ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     dragTo = true;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( int i = 0; i < item.getActions( ).size( ) && !dragTo; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.DRAG_TO && action.getTargetId( ).equals( anotherItem.getItem( ).getId( ) ) ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     dragTo = true;
                 }
             }
         }
         return dragTo;
     }
 
     /**
      * Triggers the drag to action associated with the item
      * 
      * @param npc
      *            The second item necessary for the use with action
      * @return True if the items were used, false otherwise
      */
     public boolean dragTo( FunctionalNPC npc ) {
         boolean dragTo = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < item.getActions( ).size( ) && !dragTo; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.DRAG_TO && action.getTargetId( ).equals( npc.getNPC( ).getId( ) ) ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     dragTo = true;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( int i = 0; i < item.getActions( ).size( ) && !dragTo; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.DRAG_TO && action.getTargetId( ).equals( npc.getNPC( ).getId( ) ) ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     dragTo = true;
                 }
             }
         }
         return dragTo;
     }
 
     public boolean customInteract( String actionName, FunctionalItem anotherItem ) {
 
         boolean customInteract = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < item.getActions( ).size( ) && !customInteract; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.CUSTOM_INTERACT && ( (CustomAction) action ).getName( ).equals( actionName ) && action.getTargetId( ) != null && action.getTargetId( ).equals( anotherItem.getItem( ).getId( ) ) ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     customInteract = true;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( int i = 0; i < item.getActions( ).size( ) && !customInteract; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.CUSTOM_INTERACT && ( (CustomAction) action ).getName( ).equals( actionName ) && action.getTargetId( ) != null && action.getTargetId( ).equals( anotherItem.getItem( ).getId( ) ) ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     customInteract = true;
                 }
             }
         }
 
         return customInteract;
     }
 
     public boolean customInteract( String actionName, FunctionalNPC npc ) {
 
         boolean customInteract = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < item.getActions( ).size( ) && !customInteract; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.CUSTOM_INTERACT && action.getTargetId( ).equals( npc.getNPC( ).getId( ) ) && ( (CustomAction) action ).getName( ).equals( actionName ) ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     customInteract = true;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( int i = 0; i < item.getActions( ).size( ) && !customInteract; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.CUSTOM_INTERACT && action.getTargetId( ).equals( npc.getNPC( ).getId( ) ) && ( (CustomAction) action ).getName( ).equals( actionName ) ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     customInteract = true;
                 }
             }
         }
         return customInteract;
     }
 
     /**
      * Triggers the giving action associated with the item
      * 
      * @param npc
      *            The character receiver of the item
      * @return True if the item was given, false otherwise
      */
     public boolean giveTo( FunctionalNPC npc ) {
 
         boolean givenTo = false;
 
         // Only take the FIRST valid action
         for( int i = 0; i < item.getActions( ).size( ) && !givenTo; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.GIVE_TO && action.getTargetId( ).equals( npc.getElement( ).getId( ) ) ) {
                 if( new FunctionalConditions( action.getConditions( ) ).allConditionsOk( ) ) {
                     // If the item has not a cancel action, consume the item
                     if( !action.getEffects( ).hasCancelAction( ) )
                         Game.getInstance( ).consumeItem( item.getId( ) );
 
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getEffects( ) );
                     givenTo = true;
                 }
             }
         }
         // if no actions can be launched (because its conditions are't OK), lunch the first action which has activated not-effects
         for( int i = 0; i < item.getActions( ).size( ) && !givenTo; i++ ) {
             Action action = item.getAction( i );
             if( action.getType( ) == Action.GIVE_TO && action.getTargetId( ).equals( npc.getElement( ).getId( ) ) ) {
                 if( action.isActivatedNotEffects( ) ) {
                     // When not effects are active, the item hasn't be grabbed
                     //if( !action.getEffects( ).hasCancelAction( ) )
                       //  Game.getInstance( ).consumeItem( item.getId( ) );
 
                     // Store the effects
                     FunctionalEffects.storeAllEffects( action.getNotEffects( ) );
                     givenTo = true;
                 }
             }
         }
         return givenTo;
     }
 
     /**
      * Creates the current resource block to be used
      */
     public Resources createResourcesBlock( ) {
 
         // Get the active resources block
         Resources newResources = null;
         for( int i = 0; i < item.getResources( ).size( ) && newResources == null; i++ )
             if( new FunctionalConditions( item.getResources( ).get( i ).getConditions( ) ).allConditionsOk( ) )
                 newResources = item.getResources( ).get( i );
 
         // If no resource block is available, create a default one 
         if( newResources == null ) {
             newResources = new Resources( );
             newResources.addAsset( new Asset( Item.RESOURCE_TYPE_ICON, ResourceHandler.DEFAULT_ICON ) );
             newResources.addAsset( new Asset( Item.RESOURCE_TYPE_IMAGE, ResourceHandler.DEFAULT_IMAGE ) );
         }
         return newResources;
     }
 
     @Override
     public InfluenceArea getInfluenceArea( ) {
 
         return influenceArea;
     }
 
     public ElementReference getReference( ) {
 
         return reference;
     }
 }
