 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  * research group.
  * 
  * Copyright 2005-2010 <e-UCM> research group.
  * 
  * You can access a list of all the contributors to <e-Adventure> at:
  * http://e-adventure.e-ucm.es/contributors
  * 
  * <e-UCM> is a research group of the Department of Software Engineering and
  * Artificial Intelligence at the Complutense University of Madrid (School of
  * Computer Science).
  * 
  * C Profesor Jose Garcia Santesmases sn, 28040 Madrid (Madrid), Spain.
  * 
  * For more info please visit: <http://e-adventure.e-ucm.es> or
  * <http://www.e-ucm.es>
  * 
  * ****************************************************************************
  * 
  * This file is part of <e-Adventure>, version 1.2.
  * 
  * <e-Adventure> is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with <e-Adventure>. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package es.eucm.eadventure.editor.gui.displaydialogs;
 
 import java.awt.GridBagConstraints;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.AssetsController;
 import es.eucm.eadventure.editor.control.controllers.animation.FrameDataControl;
 import es.eucm.eadventure.editor.control.controllers.general.ResourcesDataControl;
 import es.eucm.eadventure.editor.control.controllers.imageedition.EditImageController;
 import es.eucm.eadventure.editor.control.controllers.imageedition.ImageToolBar;
 import es.eucm.eadventure.editor.control.controllers.imageedition.filter.TransparentColorFilter;
 
 /**
  * Dialog holding image edition
  * 
  */
 public class EditImageDialog extends GraphicDialog {
 
     private static final long serialVersionUID = 3449739085089862729L;
 
     /**
      * Image
      */
     private BufferedImage image;
 
     private String path;
 
     private EditImageController controller;
 
     private ResourcesDataControl resourcesDataControl;
     
     private int assetIndex;
     
     private FrameDataControl frameDataControl;
     
     public EditImageDialog( ResourcesDataControl resourcesDataControl2, int assetIndex2 ) {
         this.path = resourcesDataControl2.getAssetPath( assetIndex2 );
         this.resourcesDataControl = resourcesDataControl2;
         this.assetIndex = assetIndex2;
 
         fillAndInitilize();
     }
 
 
     public EditImageDialog( FrameDataControl frameDataControl ) {
         this.frameDataControl = frameDataControl;
         this.path = frameDataControl.getImageURI( );
 
         fillAndInitilize();
     }
 
     
     private void fillAndInitilize() {
 
         // Load the image
         BufferedImage tempImage = (BufferedImage) AssetsController.getImage( path );
         image = new BufferedImage(tempImage.getWidth( ), tempImage.getHeight( ), BufferedImage.TYPE_4BYTE_ABGR);
         image.getGraphics( ).drawImage( tempImage, 0, 0, null );
        setSize( image.getWidth( ) > 450 ? image.getWidth( ) : 450, image.getHeight( ) > 600 ? 600 : image.getHeight( ) );
 
 
         // Set the dialog and show it
         setTitle( TC.get( "ImageDialog.Title", AssetsController.getFilename( path ) ) );
 
         controller = new EditImageController( image, this );
 
         TransparentColorFilter filter = new TransparentColorFilter( false, 15 );
         controller.setImageFilter( filter );
 
         GridBagConstraints c = new GridBagConstraints( );
         c.gridx = 0;
         c.gridy = 0;
         final boolean transparencyAllowed = image.getColorModel( ).getComponentSize( ).length >= 4;
 
         c.gridy++;
         this.add( new ImageToolBar( transparencyAllowed, filter, controller, imagePanel ), c );
 
         imagePanel.addMouseListener( new MouseAdapter( ) {
 
             @Override
             public void mouseClicked( MouseEvent e ) {
 
                 int xImage = e.getX( ) - imageX;
                 int yImage = e.getY( ) - imageY;
                 float widthRatio = (float) image.getWidth( ) / (float) imageWidth;
                 float heightRatio = (float) image.getHeight( ) / (float) imageHeight;
                 xImage = Math.round( xImage * widthRatio );
                 yImage = Math.round( yImage * heightRatio );
                 controller.transform( xImage, yImage );
 
             }
         } );       
 
         JPanel bottomPanel = new JPanel( );
 
         JButton okButton = new JButton( TC.get( "GeneralText.OK" ) );
         okButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 if( transparencyAllowed && controller.isChanged( ) ) {
                     String temp[] = path.split( "\\." );
                     String ext = temp[temp.length - 1];
                     String newPath = path;
                     File f;
                     if (ext.equals( "png" ))
                         f = new File( Controller.getInstance( ).getProjectFolder( ), path );
                     else {
                         newPath = "";
                         for (int i = 0; i < temp.length -1; i++)
                             newPath = newPath + temp[i] + ".";
                         newPath = newPath + "png";
                         f = new File( Controller.getInstance( ).getProjectFolder( ), newPath );
                     }
                     try {
                         ImageIO.write( image, "png", ImageIO.createImageOutputStream( f ) );
                         if (!newPath.equals( path ) && resourcesDataControl != null)
                             resourcesDataControl.setAssetPath( newPath, assetIndex );
                         if (!newPath.equals( path ) && frameDataControl != null)
                             frameDataControl.setImageURI( newPath );
                     }
                     catch( IOException e1 ) {
                         e1.printStackTrace( );
                     }
                 }
 
                 setVisible( false );
 
             }
         } );
         bottomPanel.add( okButton );
 
         JButton cancelButton = new JButton( TC.get( "GeneralText.Cancel" ) );
         cancelButton.addActionListener( new ActionListener( ) {
 
             public void actionPerformed( ActionEvent e ) {
 
                 setVisible( false );
 
             }
 
         } );
 
         bottomPanel.add( cancelButton );
 
         c.gridy++;
         this.add( bottomPanel, c );
         
         this.setVisible( true );
 
     }
 
     @Override
     protected void deleteImages( ) {
 
         if( image != null )
             image.flush( );
 
     }
 
     @Override
     protected Image getCurrentImage( ) {
 
         return image;
     }
 
 }
