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
 package es.eucm.eadventure.editor.gui.displaydialogs;
 
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 
 import es.eucm.eadventure.editor.control.Controller;
 
 /**
  * This holds the common operations for a class that displays images.
  * 
  * @author Bruno Torijano Bueno
  */
 public abstract class GraphicDialog extends JDialog {
 
     /**
      * Required.
      */
     private static final long serialVersionUID = 1L;
     
     /**
      * Panel holding the image
      */
     protected ImagePanel imagePanel;
 
     /**
      * Creates a new graphic dialog.
      */
     public GraphicDialog( ) {
 
         // Call to the JDialog constructor
         super( Controller.getInstance( ).peekWindow( ), Dialog.ModalityType.TOOLKIT_MODAL );
 
         // Set the panel to paint the animation
         setLayout( new GridBagLayout( ) );
         GridBagConstraints c = new GridBagConstraints( );
         c.insets = new Insets( 20, 20, 20, 20 );
         c.fill = GridBagConstraints.BOTH;
         c.weightx = 1;
         c.weighty = 1;
         imagePanel = new ImagePanel( );
         add( imagePanel, c );
 
         // Set the dialog and show it
         addWindowListener( new WindowAdapter( ) {
 
             @Override
             public void windowClosing( WindowEvent e ) {
 
                 setVisible( false );
                 deleteImages( );
                 dispose( );
             }
         } );
         setMinimumSize( new Dimension( 400, 300 ) );
         setSize( 500, 380 );
         Dimension screenSize = Toolkit.getDefaultToolkit( ).getScreenSize( );
         setLocation( ( screenSize.width - getWidth( ) ) / 2, ( screenSize.height - getHeight( ) ) / 2 );
     }
 
     /**
      * Returns the image that must be showed in the dialog.
      * 
      * @return Image to show
      */
     protected abstract Image getCurrentImage( );
 
     /**
      * Returns the ratio of the current image.
      * 
      * @return Ratio of the image being displayed
      */
     protected double getCurrentImageRatio( ) {
 
         if( getCurrentImage( ) != null )
             return (double) getCurrentImage( ).getWidth( null ) / (double) getCurrentImage( ).getHeight( null );
         else
             return Double.MIN_VALUE;
     }
 
     /**
      * Deletes the images when the dialog has been closed.
      */
     protected abstract void deleteImages( );
     
     /**
      * Coordinates for the image
      */
     protected int imageX, imageY, imageWidth, imageHeight;
 
     /**
      * Panel which paints the image.
      */
     public class ImagePanel extends JPanel {
 
         /**
          * Required.
          */
         private static final long serialVersionUID = 1L;
 
         @Override
         public void paint( Graphics g ) {
 
             super.paint( g );
            updateImageValues();
            g.drawImage( getCurrentImage( ), imageX, imageY, imageWidth, imageHeight, null );
        }
 
        /**
         * Updates imageWidth, imageHeight, imageX and imageY according to current dialog size 
         * and image resolution
         */
        public void updateImageValues(){
            // We compare the ratios of the dialog and the image
             double dialogRatio = (double) getWidth( ) / (double) getHeight( );
             double imageRatio = getCurrentImageRatio( );
             if( imageRatio != Double.MIN_VALUE ) {
                 if( dialogRatio <= imageRatio ) {
                     imageWidth = getWidth( );
                     imageHeight = (int) ( getWidth( ) / imageRatio );
                     imageX = 0;
                     imageY = ( ( getHeight( ) - imageHeight ) / 2 );
                 }
 
                 else {
                     imageWidth = (int) ( getHeight( ) * imageRatio );
                     imageHeight = getHeight( );
                     imageX = ( ( getWidth( ) - imageWidth ) / 2 );
                     imageY = 0;
                 }
             }
         }
     }
 }
