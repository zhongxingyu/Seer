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
 package es.eucm.eadventure.engine.core.gui;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.DisplayMode;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Image;
 import java.awt.LayoutManager;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.FocusListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import es.eucm.eadventure.common.data.adventure.DescriptorData;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.engine.core.control.DebugLog;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.gui.hud.contextualhud.ContextualHUD;
 import es.eucm.eadventure.engine.core.gui.hud.traditionalhud.TraditionalHUD;
 import es.eucm.eadventure.engine.multimedia.MultimediaManager;
 
 public class GUIFrame extends GUI implements FocusListener {
 
     /**
      * The frame to keep the screen black behind the game
      */
     private JFrame bkgFrame;
     
     /**
      * The special layout for the bkgFrame. It has two different behaviors, at the programmer's hand:
      * 1) Maximize the size of the component embedded in bkgFrame to fit all the window
      * 2) Maximize the size of the component, but respects a specific width/height ratio. That is essential for displaying videos. 
      */
     private GUILayout guiLayout;
 
     private static DisplayMode originalDisplayMode;
 
     /**
      * Create the singleton instance
      */
     public static void create( ) {
 
         instance = new GUIFrame( );
     }
 
     /**
      * Destroy the singleton instance
      */
     public static void delete( ) {
 
         if( originalDisplayMode != null && instance != null && ( (GUIFrame) instance ).bkgFrame != null ) {
             GraphicsDevice gm;
             GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment( );
             gm = environment.getDefaultScreenDevice( );
             gm.setFullScreenWindow( ( (GUIFrame) instance ).bkgFrame );
             gm.setDisplayMode( originalDisplayMode );
             originalDisplayMode = null;
         }
         if( instance != null && ( (GUIFrame) instance ).bkgFrame != null ) {
             ( (GUIFrame) instance ).bkgFrame.setVisible( false );
             ( (GUIFrame) instance ).bkgFrame.dispose( );
         }
         instance = null;
     }
 
     //private JPanel panel;
 
     /**
      * Private constructor to create the unique instace of the class
      */
     protected GUIFrame( ) {
 
         bkgFrame = new JFrame( "eadventure" ) {
 
             private static final long serialVersionUID = 3648656167576771790L;
 
             @Override
             public void paint( Graphics g ) {
 
                 g.setColor( Color.BLACK );
                 g.fillRect( 0, 0, getSize( ).width, getSize( ).height );
                 if( GUIFrame.this.component != null )
                     GUIFrame.this.component.repaint( );
             }
         };
 
         // Create the list of icons of the window
        List<Image> icons = new ArrayList<Image>( );

        icons.add( new ImageIcon( "gui/Icono-Motor-16x16.png" ).getImage( ) );
        icons.add( new ImageIcon( "gui/Icono-Motor-32x32.png" ).getImage( ) );
        icons.add( new ImageIcon( "gui/Icono-Motor-64x64.png" ).getImage( ) );
        icons.add( new ImageIcon( "gui/Icono-Motor-128x128.png" ).getImage( ) );
        bkgFrame.setIconImages( icons );

         bkgFrame.setUndecorated( true );
 
         Dimension screenSize = Toolkit.getDefaultToolkit( ).getScreenSize( );
        if( graphicConfig == DescriptorData.GRAPHICS_BLACKBKG && !Game.getInstance( ).isDebug( ) ) {
             bkgFrame.setSize( screenSize.width, screenSize.height );
             bkgFrame.setLocation( 0, 0 );
         }
         else {
             if( !Game.getInstance( ).isDebug( ) ) {
                 bkgFrame.setSize( WINDOW_WIDTH, WINDOW_HEIGHT );
                 bkgFrame.setLocation( ( screenSize.width - WINDOW_WIDTH ) / 2, ( screenSize.height - WINDOW_HEIGHT ) / 2 );
             }
             else {
                 bkgFrame.setSize( screenSize.width, screenSize.height );
                 bkgFrame.setLocation( 0, ( System.getProperty( "os.name" ).contains( "Mac" ) ? 15 : 0 ) );
             }
         }
 
         bkgFrame.setBackground( Color.BLACK );
         bkgFrame.setForeground( Color.BLACK );
         
         guiLayout = new GUILayout( );
         
         bkgFrame.setLayout( guiLayout );//new BorderLayout());
 
         gameFrame = new Canvas( );
         background = null;
         elementsToDraw = new ArrayList<ElementImage>( );
         textToDraw = new ArrayList<Text>( );
 
         gameFrame.setIgnoreRepaint( true );
         gameFrame.setFont( new Font( "Dialog", Font.PLAIN, 18 ) );
         gameFrame.setBackground( Color.black );
         gameFrame.setForeground( Color.white );
         gameFrame.setSize( new Dimension( WINDOW_WIDTH, WINDOW_HEIGHT ) );
         if( !Game.getInstance( ).isDebug( ) )
             gameFrame.setLocation( ( screenSize.width - WINDOW_WIDTH ) / 2 - (int) bkgFrame.getLocation( ).getX( ), ( screenSize.height - WINDOW_HEIGHT ) / 2 - (int) bkgFrame.getLocation( ).getY( ) );
         else
             gameFrame.setLocation( (int) bkgFrame.getLocation( ).getX( ), (int) bkgFrame.getLocation( ).getY( ) );
 
         bkgFrame.setVisible( true );
 
         bkgFrame.setIgnoreRepaint( true );
 
         bkgFrame.add( gameFrame );//, BorderLayout.CENTER);
 
         String os = System.getProperty( "os.name" );
         if (os.contains( "Mac" ))
             System.setProperty( "apple.awt.rendering", "speed" );
         
        if( os.contains( "Windows" ) && graphicConfig == DescriptorData.GRAPHICS_FULLSCREEN && !Game.getInstance( ).isDebug( ) ) {
             GraphicsEnvironment environment;
             GraphicsDevice gm = null;
             boolean changed = false;
             try {
                 // Set fullscreen... Runs into compatibility issues in non-Windows systems
                 environment = GraphicsEnvironment.getLocalGraphicsEnvironment( );
                 gm = environment.getDefaultScreenDevice( );
                 originalDisplayMode = gm.getDisplayMode( );
                 DisplayMode dm = new DisplayMode( WINDOW_WIDTH, WINDOW_HEIGHT, 32, DisplayMode.REFRESH_RATE_UNKNOWN );
                 DisplayMode[] dmodes = gm.getDisplayModes( );
                 for( int i = 0; i < dmodes.length && !changed; i++ ) {
                     if( dmodes[i].getBitDepth( ) == dm.getBitDepth( ) && dmodes[i].getHeight( ) == dm.getHeight( ) && dmodes[i].getWidth( ) == dm.getWidth( ) ) {
                         gm.setFullScreenWindow( bkgFrame );
                         gm.setDisplayMode( dm );
                         changed = true;
                     }
                 }
             }
             catch( Exception e ) {
                 if( gm != null && originalDisplayMode != null ) {
                     gm.setDisplayMode( originalDisplayMode );
                     originalDisplayMode = null;
                 }
             }
             if( !changed ) {
                 originalDisplayMode = null;
                 JOptionPane.showMessageDialog( bkgFrame, TC.get( "GUI.NoFullscreenTitle" ), TC.get( "GUI.NoFullscreenContent" ), JOptionPane.WARNING_MESSAGE );
             }
         }
         else if( !os.contains( "Windows" ) && graphicConfig == DescriptorData.GRAPHICS_FULLSCREEN ) {
             JOptionPane.showMessageDialog( bkgFrame, TC.get( "GUI.NoFullscreenTitle" ), TC.get( "GUI.NoFullscreenContent" ), JOptionPane.WARNING_MESSAGE );
         }
 
     }
 
     /**
      * Init the GUI class and also get focus for the mainwindow
      */
     @Override
     public void initGUI( int guiType, boolean customized ) {
 
         // Create the hud, depending on guiType
         if( guiType == DescriptorData.GUI_TRADITIONAL )
             hud = new TraditionalHUD( );
         else if( guiType == DescriptorData.GUI_CONTEXTUAL )
             hud = new ContextualHUD( );
 
         gameFrame.setEnabled( true );
         gameFrame.setVisible( true );
         gameFrame.setFocusable( true );
         // Double buffered painting
         // Set always on top only in full-screen mode
         bkgFrame.setAlwaysOnTop( graphicConfig != DescriptorData.GRAPHICS_WINDOWED );
         gameFrame.createBufferStrategy( 2 );
         gameFrame.validate( );
 
         gameFrame.addFocusListener( this );
         gameFrame.requestFocusInWindow( );
 
         hud.init( );
 
         if( Game.getInstance( ).getGameDescriptor( ).getCursorPath( DEFAULT_CURSOR ) != null ) {
             try {
                 defaultCursor = Toolkit.getDefaultToolkit( ).createCustomCursor( MultimediaManager.getInstance( ).loadImageFromZip( Game.getInstance( ).getGameDescriptor( ).getCursorPath( DEFAULT_CURSOR ), MultimediaManager.IMAGE_MENU ), new Point( 0, 0 ), "defaultCursor" );
             }
             catch( Exception e ) {
                 DebugLog.general( "Cound't find default cursor " );
                 defaultCursor = Toolkit.getDefaultToolkit( ).createCustomCursor( MultimediaManager.getInstance( ).loadImage( "gui/cursors/nocursor.png", MultimediaManager.IMAGE_MENU ), new Point( 0, 0 ), "defaultCursor" );
             }
         }
         else {
             try {
                 defaultCursor = Toolkit.getDefaultToolkit( ).createCustomCursor( MultimediaManager.getInstance( ).loadImage( "gui/cursors/default.png", MultimediaManager.IMAGE_MENU ), new Point( 0, 0 ), "defaultCursor" );
             }
             catch( Exception e ) {
                 DebugLog.general( "Cound't find custom default cursor " );
                 defaultCursor = Toolkit.getDefaultToolkit( ).createCustomCursor( MultimediaManager.getInstance( ).loadImage( "gui/cursors/nocursor.png", MultimediaManager.IMAGE_MENU ), new Point( 0, 0 ), "defaultCursor" );
             }
         }
         gameFrame.setCursor( defaultCursor );
     }
 
     /**
      * Displays a Swing or AWT component in the game window.
      * <p>
      * To remove the component, use RestoreFrame method.
      * 
      * @param component
      * @return
      */
     @Override
     public JFrame showComponent( Component component ) {
 
         gameFrame.setVisible( false );
         bkgFrame.setIgnoreRepaint( true );
         if( this.component != null ) {
             bkgFrame.remove( this.component );
             guiLayout.setFixedSize( 800, 600 );
             guiLayout.setMode( GUILayout.MODE_USE_ALL_WINDOW  );
         }
         this.component = component;
         component.setBackground( Color.BLACK );
         component.setForeground( Color.BLACK );
         bkgFrame.add( component );
         bkgFrame.validate( );
         component.repaint( );
         //System.out.println("IS DISPLAYABLE: " + component.isDisplayable() + "\n");
 
         return bkgFrame;
     }
 
     /**
      * Restores the frame to its original state after displaying a Swing or AWT
      * component.
      * 
      */
     @Override
     public void restoreFrame( ) {
 
         if( component != null ) {
             bkgFrame.remove( component );
             guiLayout.setFixedSize( 800, 600 );
             guiLayout.setMode( GUILayout.MODE_USE_ALL_WINDOW  );
         }
         component = null;
 
         if( Game.getInstance( ).isDebug( ) ) {
             //bkgFrame.setIgnoreRepaint(false);
             //bkgFrame.repaint();
             gameFrame.setVisible( true );
             bkgFrame.validate( );
             Game.getInstance( ).repaintDebug( );
         }
         else {
             bkgFrame.setIgnoreRepaint( true );
             bkgFrame.repaint( );
             gameFrame.setVisible( true );
             bkgFrame.validate( );
         }
     }
 
     @Override
     public Frame getJFrame( ) {
 
         return bkgFrame;
     }
 
     private class GUILayout implements LayoutManager {
 
         /**
          * Constant for behavior 1. The layout will maximize the size of the component embedded in bkgFrame to fit all the window,
          * using all the available space (some exceptions may apply in debugging mode so other panels are also allocated the appropriate space).
          */
         public static final int MODE_USE_ALL_WINDOW = 0;
         
         /**
          * Constant for behavior 2. Maximize the size of the component, but respects a specific width/height ratio. That is essential for displaying videos.
          *  (some exceptions may apply in debugging mode so other panels are also allocated the appropriate space).
          */
         public static final int MODE_RESPECT_WHRATIO = 1;
         
         /**
          * The current mode in use
          * @see {@link #MODE_RESPECT_WHRATIO}, {@link #MODE_USE_ALL_WINDOW}
          */
         private int mode;
         
         
         /**
          * The width/height ratio that should be respected when setting the size of the component contained in bkgFrame.
          * This param. will only be used if mode == MODE_RESPECT_WHRATIO
          */
         private int fixedWidth;
         
         private int fixedHeight;
         
         public void setFixedSize(int w, int h){
             this.fixedHeight = h;
             this.fixedWidth = w;
         }
         
         public GUILayout(){
             // Default mode
             this.mode = MODE_USE_ALL_WINDOW;
         }
         
         /**
          * Sets the current behavior
          * @param mode Accepted values: {@link #MODE_RESPECT_WHRATIO}, {@link #MODE_USE_ALL_WINDOW}
          */
         public void setMode( int mode ){
             if ( mode == MODE_USE_ALL_WINDOW || mode ==MODE_RESPECT_WHRATIO ){
                 this.mode = mode;
             }
         }
         
         /**
          * Returns the current behavior
          * @return {@link #MODE_RESPECT_WHRATIO} or {@link #MODE_USE_ALL_WINDOW}
          */
         public int getMode ( ){
             return mode;
         }
         
         public void addLayoutComponent( String arg0, Component arg1 ) {
 
         }
 
         public void layoutContainer( Container container ) {
 
             Component[] components = container.getComponents( );
             for( int i = 0; i < components.length; i++ ) {
                 Dimension screenSize = Toolkit.getDefaultToolkit( ).getScreenSize( );
                 if( bkgFrame != null ) {
                     int posX = ( screenSize.width - GUI.WINDOW_WIDTH ) / 2 - (int) bkgFrame.getLocation( ).getX( );
                     int posY = ( screenSize.height - GUI.WINDOW_HEIGHT ) / 2 - (int) bkgFrame.getLocation( ).getY( );
                     
                     // New Lines //
                     int w = GUI.WINDOW_WIDTH;
                     int h = GUI.WINDOW_HEIGHT;
                     if ( mode == MODE_RESPECT_WHRATIO ){
                         if (fixedWidth / fixedHeight >= w / h) {
                             w = GUI.WINDOW_WIDTH;
                             h = (int) ((float) fixedHeight / (float) fixedWidth * GUI.WINDOW_WIDTH);
                         } else {
                             h = GUI.WINDOW_HEIGHT;
                             w = (int) ((float) fixedWidth / (float) fixedHeight * GUI.WINDOW_HEIGHT);
                         }
                         
                         posX = ( screenSize.width - w ) / 2 - (int) bkgFrame.getLocation( ).getX( );
                         posY = ( screenSize.height - h ) / 2 - (int) bkgFrame.getLocation( ).getY( );
                     }
                     
                     if( Game.getInstance( ).isDebug( ) ) {
                         posX = ( screenSize.width - w );
                         posY = ( System.getProperty( "os.name" ).contains( "Mac" ) ? 15 : 0 );
                     }
 
                     if( components[i] instanceof DebugLogPanel ) {
                         components[i].setBounds( 0, h + ( System.getProperty( "os.name" ).contains( "Mac" ) ? 15 : 0 ), screenSize.width, screenSize.height - h - ( System.getProperty( "os.name" ).contains( "Mac" ) ? 15 : 0 ) );
                     }
                     else if( components[i] instanceof DebugValuesPanel ) {
                         components[i].setBounds( 0, ( System.getProperty( "os.name" ).contains( "Mac" ) ? 15 : 0 ), screenSize.width - w, h );
                     }
                     else
                         components[i].setBounds( posX, posY, w, h );
                 }
                 else {
                     components[i].setLocation( 0, 0 );
                     components[i].setSize( GUI.WINDOW_WIDTH, GUI.WINDOW_HEIGHT );
                 }
             }
         }
 
         public Dimension minimumLayoutSize( Container arg0 ) {
 
             return arg0.getSize( );
         }
 
         public Dimension preferredLayoutSize( Container arg0 ) {
 
             return arg0.getSize( );
         }
 
         public void removeLayoutComponent( Component arg0 ) {
 
         }
     }
 
     @Override
     public JFrame showComponent( Component component, int w, int h ) {
         if( this.component != null ) {
             bkgFrame.remove( this.component );
             this.component = null;
         }
         guiLayout.setFixedSize( w, h );
         guiLayout.setMode( GUILayout.MODE_RESPECT_WHRATIO  );
         return showComponent (component);
     }
 }
