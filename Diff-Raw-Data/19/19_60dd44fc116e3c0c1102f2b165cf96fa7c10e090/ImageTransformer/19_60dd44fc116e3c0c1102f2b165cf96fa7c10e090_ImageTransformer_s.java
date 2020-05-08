 /*******************************************************************************
  * eAdventure (formerly <e-Adventure> and <e-Game>) is a research project of the e-UCM
  *          research group.
  *   
  *    Copyright 2005-2012 e-UCM research group.
  *  
  *     e-UCM is a research group of the Department of Software Engineering
  *          and Artificial Intelligence at the Complutense University of Madrid
  *          (School of Computer Science).
  *  
  *          C Profesor Jose Garcia Santesmases sn,
  *          28040 Madrid (Madrid), Spain.
  *  
  *          For more info please visit:  <http://e-adventure.e-ucm.es> or
  *          <http://www.e-ucm.es>
  *  
  *  ****************************************************************************
  * This file is part of eAdventure, version 1.5.
  * 
  *   You can access a list of all the contributors to eAdventure at:
  *          http://e-adventure.e-ucm.es/contributors
  *  
  *  ****************************************************************************
  *       eAdventure is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *  
  *      eAdventure is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with Adventure.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.common.auxiliar;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Transparency;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 
 /**
  * Class with all the common functions for transform
  * images.
  * 
  * Implemented as a Singleton 
 * @author Ángel S.
  *
  */
 public class ImageTransformer {
 
     private static ImageTransformer imageTransformer;
     
     private ImageTransformer(){
         
     }
     
     public static ImageTransformer getInstance(){
         if ( imageTransformer == null ){
             imageTransformer = new ImageTransformer();
         }
         return imageTransformer;
     }
     
     /**
      * Return an image scaled with specified parameters
      * (Method copied from MultimediaManager)
      * @param image Image to scale
      * @param x Scale for axis x
      * @param y Scale for axis y
      * @return An image scaled.
      */
     public Image getScaledImage( Image image, float x, float y ) {
 
         Image newImage = null;
 
         if( image != null ) {
 
             // set up the transform
             AffineTransform transform = new AffineTransform( );
             transform.scale( x, y );
             transform.translate( ( x - 1 ) * image.getWidth( null ) / 2, ( y - 1 ) * image.getHeight( null ) / 2 );
 
             // create a transparent (not translucent) image
             newImage = new BufferedImage( image.getWidth( null ), image.getHeight( null ), Transparency.BITMASK );
 
             // draw the transformed image
             Graphics2D g = (Graphics2D) newImage.getGraphics( );
 
             g.drawImage( image, transform, null );
             g.dispose( );
         }
 
         return newImage;
     }
 }
