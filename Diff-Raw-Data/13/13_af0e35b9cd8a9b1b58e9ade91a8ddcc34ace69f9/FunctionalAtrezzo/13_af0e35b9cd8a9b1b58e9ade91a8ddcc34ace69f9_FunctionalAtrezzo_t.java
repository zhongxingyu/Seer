 /**
  * <e-Adventure> is an <e-UCM> research project. <e-UCM>, Department of Software
  * Engineering and Artificial Intelligence. Faculty of Informatics, Complutense
  * University of Madrid (Spain).
  * 
  * @author Del Blanco, A., Marchiori, E., Torrente, F.J. (alphabetical order) *
  * @author Lpez Maas, E., Prez Padilla, F., Sollet, E., Torijano, B. (former
  *         developers by alphabetical order)
  * @author Moreno-Ger, P. & Fernndez-Manjn, B. (directors)
  * @year 2009 Web-site: http://e-adventure.e-ucm.es
  */
 
 /*
  * Copyright (C) 2004-2009 <e-UCM> research group
  * 
  * This file is part of <e-Adventure> project, an educational game & game-like
  * simulation authoring tool, available at http://e-adventure.e-ucm.es.
  * 
  * <e-Adventure> is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * <e-Adventure>; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
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
 import es.eucm.eadventure.common.data.chapter.elements.Atrezzo;
 import es.eucm.eadventure.common.data.chapter.elements.Element;
 import es.eucm.eadventure.common.data.chapter.elements.Item;
 import es.eucm.eadventure.common.data.chapter.resources.Asset;
 import es.eucm.eadventure.common.data.chapter.resources.Resources;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.gui.GUI;
 import es.eucm.eadventure.engine.multimedia.MultimediaManager;
 import es.eucm.eadventure.engine.resourcehandler.ResourceHandler;
 
 /**
  * An atrezzo object in the game
  */
 public class FunctionalAtrezzo extends FunctionalElement {
 
     /**
      * Resources being used in the atrezzo item
      */
     protected Resources resources;
 
     /**
      * Image of the atrezo item, to display in the scene
      */
     protected Image image;
 
     /**
      * Image of the atrezzo item, to display on the inventory
      */
     // private Image icon;
     private Image oldImage = null;
     
     private int x1, y1, x2, y2;
     
     private int width, height;
 
     private float oldScale = -1;
 
     private Image oldOriginalImage = null;
 
     /**
      * Atrezzo item containing the data
      */
     protected Atrezzo atrezzo;
 
     private ElementReference reference;
 
     public FunctionalAtrezzo( Atrezzo atrezzo, ElementReference reference ) {
 
         this( atrezzo, reference.getX( ), reference.getY( ) );
         this.reference = reference;
         this.layer = reference.getLayer( );
         this.scale = reference.getScale( );
     }
 
     /**
      * Creates a new FunctionalItem
      * 
      * @param atrezzo
      *            the atrezzo's data
      * @param x
      *            the atrezzo's horizontal position
      * @param y
      *            the atrezzo's vertical position
      */
     public FunctionalAtrezzo( Atrezzo atrezzo, int x, int y ) {
 
         super( x, y );
         this.atrezzo = atrezzo;
         this.scale = 1;
         Image tempimage = null;
         //icon = null;
 
         resources = createResourcesBlock( );
 
         // Load the resources
         MultimediaManager multimediaManager = MultimediaManager.getInstance( );
         if( resources.existAsset( Item.RESOURCE_TYPE_IMAGE ) ) {
             tempimage = multimediaManager.loadImageFromZip( resources.getAssetPath( Item.RESOURCE_TYPE_IMAGE ), MultimediaManager.IMAGE_SCENE );
             removeTransparentParts(tempimage);
         }
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
                 if (alpha > 128) {
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
         image = GUI.getInstance( ).getGraphicsConfiguration( ).createCompatibleImage( x2 - x1, y2 - y1, Transparency.BITMASK );
 
         // draw the transformed image
         Graphics2D g = (Graphics2D) image.getGraphics( );
 
         g.drawImage( tempimage, 0, 0, x2-x1, y2-y1, x1, y1, x2, y2, null);
 //        g.drawImage( image, transform, null );
         g.dispose( );
         
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
             }
             // if( resources.existAsset( Item.RESOURCE_TYPE_ICON ) )
             //   icon = multimediaManager.loadImageFromZip( resources.getAssetPath( Item.RESOURCE_TYPE_ICON ), MultimediaManager.IMAGE_SCENE );
         }
     }
 
     /**
      * Returns this atrezzo's data
      * 
      * @return this atrezzo's data
      */
     public Atrezzo getAtrezzo( ) {
 
         return atrezzo;
     }
 
     /**
      * Returns this atrezzo's icon image
      * 
      * @return this atrezzo's icon image
      */
     // public Image getIconImage( ) {
     //   return icon;
     //}
     @Override
     public Element getElement( ) {
 
         return atrezzo;
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
 
         int x_image = Math.round( x - ( getWidth( ) * scale / 2 ) ) - Game.getInstance( ).getFunctionalScene( ).getOffsetX( );
         int y_image = Math.round( y - getHeight( ) * scale );
        //x_image+=x1;
        //y_image+=y1;
         if( scale != 1 ) {
             Image temp;
             if( scale == oldScale && image == oldOriginalImage ) {
                 temp = oldImage;
             }
             else {
                 //temp = image.getScaledInstance( Math.round( image.getWidth( null ) * scale ), Math.round( image.getHeight( null ) * scale ), Image.SCALE_SMOOTH );
                 temp = GUI.getInstance( ).getGraphicsConfiguration( ).createCompatibleImage( Math.round( image.getWidth( null ) * scale ),  Math.round( image.getHeight( null ) * scale ), Transparency.BITMASK );
                 ((Graphics2D) temp.getGraphics( )).drawImage( image, AffineTransform.getScaleInstance( scale, scale ), null );
                 oldImage = temp;
                 oldOriginalImage = image;
                 oldScale = scale;
             }
             if( layer == -1 ){
                 System.out.println( "[AT] "+this.getAtrezzo( ).getId( )+ ": ("+x_image+" , "+y_image+") "+temp.getWidth( null )+"x"+temp.getHeight( null ) );
                 GUI.getInstance( ).addElementToDraw( temp, x_image, y_image, Math.round( y ), Math.round( y ), null, null );
             }else{
                 System.out.println( "[AT] "+this.getAtrezzo( ).getId( )+ ": ("+x_image+" , "+y_image+") "+temp.getWidth( null )+"x"+temp.getHeight( null ) );
                 GUI.getInstance( ).addElementToDraw( temp, x_image, y_image, layer, Math.round( y ), null, null  );
             }
 
         }
         else if( layer == -1 ){
             System.out.println( "[AT] "+this.getAtrezzo( ).getId( )+ ": ("+x_image+" , "+y_image+") "+image.getWidth( null )+"x"+image.getHeight( null ) );
             GUI.getInstance( ).addElementToDraw( image, x_image, y_image, Math.round( y ), Math.round( y ), null, null  );
         }else{
             System.out.println( "[AT] "+this.getAtrezzo( ).getId( )+ ": ("+x_image+" , "+y_image+") "+image.getWidth( null )+"x"+image.getHeight( null ) );
             GUI.getInstance( ).addElementToDraw( image, x_image, y_image, layer, Math.round( y ), null, null  );
         }
     }
 
     @Override
     public boolean isPointInside( float x, float y ) {
 
         boolean isInside = false;
 
         int mousex = (int) ( x - ( this.x - getWidth( ) * scale / 2 ) );
         int mousey = (int) ( y - ( this.y - getHeight( ) * scale ) );
 
         if (mousex < x1 || mousey < y1 || mousex >= x2 || mousey >= y2)
             return false;
         mousex = mousex - x1;
         mousey = mousey - y1;
         if( ( mousex >= 0 ) && ( mousex < getWidth( ) * scale ) && ( mousey >= 0 ) && ( mousey < getHeight( ) * scale ) ) {
             BufferedImage bufferedImage = (BufferedImage) image;
             int alpha = bufferedImage.getRGB( (int) ( mousex / scale ), (int) ( mousey / scale ) ) >>> 24;
             isInside = alpha > 128;
         }
 
         return isInside;
     }
 
     //TODO creo k hay que quitarlo
     @Override
     public boolean isInInventory( ) {
 
         return false;//Game.getInstance( ).getItemSummary( ).isItemGrabbed( atrezzo.getId( ) );
     }
 
     @Override
     public boolean examine( ) {
 
         return false;
     }
 
     @Override
     public boolean canBeUsedAlone( ) {
 
         return false;
     }
 
     /* Own methods */
 
     /**
      * Creates the current resource block to be used
      */
     public Resources createResourcesBlock( ) {
 
         // Get the active resources block
         Resources newResources = null;
         for( int i = 0; i < atrezzo.getResources( ).size( ) && newResources == null; i++ )
             if( new FunctionalConditions( atrezzo.getResources( ).get( i ).getConditions( ) ).allConditionsOk( ) )
                 newResources = atrezzo.getResources( ).get( i );
 
         // If no resource block is available, create a default one 
         if( newResources == null ) {
             newResources = new Resources( );
             //  newResources.addAsset( new Asset( Item.RESOURCE_TYPE_ICON, ResourceHandler.DEFAULT_ICON ) );
             newResources.addAsset( new Asset( Item.RESOURCE_TYPE_IMAGE, ResourceHandler.DEFAULT_IMAGE ) );
         }
         return newResources;
     }
 
     @Override
     public boolean canPerform( int action ) {
 
         return false;
     }
 
     @Override
     public Action getFirstValidAction( int actionType ) {
 
         return null;
     }
 
     @Override
     public CustomAction getFirstValidCustomAction( String actionName ) {
 
         return null;
     }
 
     @Override
     public CustomAction getFirstValidCustomInteraction( String actionName ) {
 
         return null;
     }
 
     @Override
     public InfluenceArea getInfluenceArea( ) {
 
         return null;
     }
 
     public ElementReference getReference( ) {
 
         return reference;
     }
 }
