 /*
  * Copyright (c) 2003-2006 jMonkeyEngine
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * * Redistributions of source code must retain the above copyright
  *   notice, this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright
  *   notice, this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  *
  * * Neither the name of 'jMonkeyEngine' nor the names of its contributors 
  *   may be used to endorse or promote products derived from this software 
  *   without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.jmex.awt.swingui;
 
 import java.awt.AWTEvent;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.KeyboardFocusManager;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.FocusEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
import java.awt.event.ContainerListener;
import java.awt.event.ContainerEvent;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Map;
import java.beans.PropertyVetoException;
 import javax.swing.JComponent;
 import javax.swing.JDesktopPane;
 import javax.swing.JInternalFrame;
 import javax.swing.JPanel;
 import javax.swing.JRootPane;
 import javax.swing.JTabbedPane;
 import javax.swing.Popup;
 import javax.swing.PopupFactory;
 import javax.swing.RepaintManager;
 import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.plaf.basic.BasicInternalFrameUI;
 
 import com.jme.bounding.OrientedBoundingBox;
 import com.jme.image.Texture;
 import com.jme.input.InputHandler;
 import com.jme.input.KeyInput;
 import com.jme.input.MouseInput;
 import com.jme.input.action.InputAction;
 import com.jme.input.action.InputActionEvent;
 import com.jme.math.Ray;
 import com.jme.math.Vector2f;
 import com.jme.math.Vector3f;
 import com.jme.renderer.Renderer;
 import com.jme.scene.shape.Quad;
 import com.jme.scene.state.AlphaState;
 import com.jme.scene.state.TextureState;
import com.jme.scene.Node;
 import com.jme.system.DisplaySystem;
 import com.jme.util.LoggingSystem;
 import com.jmex.awt.input.AWTKeyInput;
 import com.jmex.awt.input.AWTMouseInput;
 
 /**
  * A quad that displays a {@link JDesktopPane} as texture. It also converts jME mouse and keyboard events to Swing
  * events. The latter does work for ortho mode only. There are some issues with using multiple of this desktops.
  *
  * @see ImageGraphics
  */
 public class JMEDesktop extends Quad {
     private static final long serialVersionUID = 1L;
     private ImageGraphics graphics;
     private JDesktopPane desktop;
     private Texture texture;
     private boolean initialized;
     private int width;
     private int height;
 
     private boolean showingJFrame = false;
     private final Frame awtWindow;
     private int desktopWidth;
     private int desktopHeight;
     private static final int DOUBLE_CLICK_TIME = 300;
     private final InputHandler inputHandler;
     private JMEDesktop.XUpdateAction xUpdateAction;
     private JMEDesktop.YUpdateAction yUpdateAction;
     private WheelUpdateAction wheelUpdateAction;
     private JMEDesktop.ButtonAction allButtonsUpdateAction;
     private InputAction keyUpdateAction;
 
     /**
      * @see #setShowingJFrame
      */
     public boolean isShowingJFrame() {
         return showingJFrame;
     }
 
     /**
      * @param showingJFrame true to display the desktop in a JFrame instead on this quad.
      * @deprecated for debuggin only
      */
     public void setShowingJFrame( boolean showingJFrame ) {
         this.showingJFrame = showingJFrame;
         awtWindow.setVisible( showingJFrame );
         awtWindow.repaint();
     }
 
     /**
      * Allows to disable input for the whole desktop and to add custom input actions.
      *
      * @return this desktops input hander for input bindings
      * @see #getXUpdateAction()
      * @see #getYUpdateAction()
      * @see #getWheelUpdateAction()
      * @see #getButtonUpdateAction(int)
      * @see #getKeyUpdateAction()
      */
     public InputHandler getInputHandler() {
         return inputHandler;
     }
 
     /**
      * Create a quad with a Swing-Texture. Creates the quad and the JFrame but do not setup the rest.
      * Call {@link #setup(int, int, boolean, InputHandler)} to finish setup.
      *
      * @param name name of this desktop
      */
     public JMEDesktop( String name ) {
         super( name );
 
         inputHandler = new InputHandler();
 
         awtWindow = new Frame() {
             public boolean isShowing() {
                 return true;
             }
 
             public boolean isVisible() {
 //                if ( new Throwable().getStackTrace()[1].getMethodName().startsWith( "requestFocus" ) ) {
 //                    System.out.println( "requestFocus" );
 //                }
 
                 if ( awtWindow.isFocusableWindow()
                         && new Throwable().getStackTrace()[1].getMethodName().startsWith( "requestFocus" ) ) {
                     return false;
                 }
                 return initialized || super.isVisible();
             }
 
             public Graphics getGraphics() {
                 if ( !showingJFrame ) {
                     return graphics == null ? super.getGraphics() : graphics.create();
                 }
                 else {
                     return super.getGraphics();
                 }
             }
 
             public boolean isFocused() {
                 return true;
             }
         };
         awtWindow.setFocusableWindowState( false );
         Container contentPane = awtWindow;
         awtWindow.setUndecorated( true );
         dontDrawBackground( contentPane );
 //            ( (JComponent) contentPane ).setOpaque( false );
 
         desktop = new JDesktopPane() {
             public void paint( Graphics g ) {
                 if ( !isShowingJFrame() ) {
                     g.clearRect( 0, 0, getWidth(), getHeight() );
                 }
                 super.paint( g );
             }
 
             public boolean isOptimizedDrawingEnabled() {
                 return false;
             }
         };
 
         new ScrollPaneRepaintFixListener().addTo( desktop );
 
 
         final Color transparent = new Color( 0, 0, 0, 0 );
         desktop.setBackground( transparent );
         desktop.setFocusable( true );
         desktop.addMouseListener( new MouseAdapter() {
             public void mousePressed( MouseEvent e ) {
                 desktop.requestFocusInWindow();
             }
         } );
 
         // this internal frame is a workaround for key binding problems in JDK1.5
         // todo: this workaround does not seem to work on mac
         if ( System.getProperty( "os.name" ).toLowerCase().indexOf( "mac" ) < 0 ) {
             final JInternalFrame internalFrame = new JInternalFrame();
             internalFrame.setUI( new BasicInternalFrameUI( internalFrame ) {
                 protected void installComponents() {
                 }
             } );
             internalFrame.setOpaque( false );
             internalFrame.setBackground( null );
             internalFrame.getContentPane().setLayout( new BorderLayout() );
             internalFrame.getContentPane().add( desktop, BorderLayout.CENTER );
             internalFrame.setVisible( true );
             internalFrame.setBorder( null );
             contentPane.add( internalFrame );
         }
         else {
             // this would have suited for JDK1.4:
             contentPane.add( desktop, BorderLayout.CENTER );
         }
 
         awtWindow.pack();
 
         RepaintManager.currentManager( null ).setDoubleBufferingEnabled( false );
     }
 
     /**
      * Create a quad with a Swing-Texture.
      * Note that for the texture a width and height that is a power of 2 is used if the graphics card does
      * not support the specified size for textures. E.g. this results in a 1024x512
      * texture for a 640x480 desktop (consider using a 512x480 desktop in that case).
      *
      * @param name               name of the spatial
      * @param width              desktop width
      * @param height             desktop height
      * @param inputHandlerParent InputHandler where the InputHandler of this desktop should be added as subhandler,
      *                           may be null to provide custom input handling or later adding of InputHandler(s)
      * @see #getInputHandler()
      */
     public JMEDesktop( String name, final int width, final int height, InputHandler inputHandlerParent ) {
         this( name, width, height, false, inputHandlerParent );
     }
 
     /**
      * Create a quad with a Swing-Texture.
      * Note that for the texture a width and height that is a power of 2 is used if the graphics card does
      * not support the specified size for textures or mipMapping is true. E.g. this results in a 1024x512
      * texture for a 640x480 desktop (consider using a 512x480 desktop in that case).
      *
      * @param name               name of the spatial
      * @param width              desktop width
      * @param height             desktop hieght
      * @param mipMapping         true to compute mipmaps for the desktop (not recommended), false for creating
      *                           a single image texture
      * @param inputHandlerParent InputHandler where the InputHandler of this desktop should be added as subhandler,
      *                           may be null to provide custom input handling or later adding of InputHandler(s)
      * @see #getInputHandler()
      */
     public JMEDesktop( String name, final int width, final int height, boolean mipMapping, InputHandler inputHandlerParent ) {
         this( name );
 
         setup( width, height, mipMapping, inputHandlerParent );
     }
 
     /**
      * Set up the desktop quad - may be called only once.
      * Note that for the texture a width and height that is a power of 2 is used if the graphics card does
      * not support the specified size for textures or mipMapping is true. E.g. this results in a 1024x512
      * texture for a 640x480 desktop (consider using a 512x480 desktop in that case).
      *
      * @param width              desktop width
      * @param height             desktop hieght
      * @param mipMapping         true to compute mipmaps for the desktop (not recommended), false for creating
      *                           a single image texture
      * @param inputHandlerParent InputHandler where the InputHandler of this desktop should be added as subhandler,
      *                           may be null to provide custom input handling or later adding of InputHandler(s)
      * @see #getInputHandler()
      */
     public void setup( int width, int height, boolean mipMapping, InputHandler inputHandlerParent ) {
         reconstruct( null, null, null, null );
         if ( inputHandlerParent != null ) {
             inputHandlerParent.addToAttachedHandlers( inputHandler );
         }
 
         if ( initialized ) {
             throw new IllegalStateException( "may be called only once" );
         }
         initialize( powerOf2SizeIfNeeded( width, mipMapping ), powerOf2SizeIfNeeded( height, mipMapping ) );
 
         this.width = powerOf2SizeIfNeeded( width, mipMapping );
         this.height = powerOf2SizeIfNeeded( height, mipMapping );
         setModelBound( new OrientedBoundingBox() );
         updateModelBound();
 
         desktop.setPreferredSize( new Dimension( width, height ) );
         desktopWidth = width;
         desktopHeight = height;
         awtWindow.pack();
 
         TextureState ts = DisplaySystem.getDisplaySystem().getRenderer().createTextureState();
         texture = new Texture();
         texture.setCorrection( Texture.CM_PERSPECTIVE );
         texture.setFilter( Texture.FM_LINEAR );
         texture.setMipmapState( mipMapping ? Texture.MM_LINEAR_LINEAR : Texture.MM_LINEAR );
         texture.setWrap( Texture.WM_WRAP_S_WRAP_T );
 
         graphics = ImageGraphics.createInstance( this.width, this.height, mipMapping ? 2 : 0 );
         enableAntiAlias( graphics );
         graphics.translate( ( this.width - width ) * 0.5f, ( this.height - height ) * 0.5f );
         texture.setImage( graphics.getImage() );
 
         texture.setScale( new Vector3f( 1, -1, 1 ) );
         ts.setTexture( texture );
         ts.apply();
         this.setRenderState( ts );
 
         AlphaState alpha = DisplaySystem.getDisplaySystem().getRenderer().createAlphaState();
         alpha.setEnabled( true );
         alpha.setBlendEnabled( true );
         alpha.setSrcFunction( AlphaState.SB_SRC_ALPHA );
         alpha.setDstFunction( AlphaState.DB_ONE_MINUS_SRC_ALPHA );
         alpha.setTestEnabled( true );
         alpha.setTestFunction( AlphaState.TF_GREATER );
         this.setRenderState( alpha );
 
 //        Toolkit.getDefaultToolkit().addAWTEventListener( new AWTEventListener() {
 //            public void eventDispatched( AWTEvent event ) {
 //                if ( isShowingJFrame() ) {
 //                    System.out.println( event );
 //                }
 //            }
 //        }, 0xFFFFFFFFFFFFFFFFl );
 
 
         xUpdateAction = new XUpdateAction();
         yUpdateAction = new YUpdateAction();
         wheelUpdateAction = new WheelUpdateAction();
         wheelUpdateAction.setSpeed( AWTMouseInput.WHEEL_AMP );
         allButtonsUpdateAction = new ButtonAction( InputHandler.BUTTON_ALL );
         keyUpdateAction = new KeyUpdateAction();
 
         setupDefaultInputBindings();
 
         if ( desktopsUsed == 0 ) {
             PopupFactory.setSharedInstance( new MyPopupFactory() );
             desktopsUsed--;
         }
 
         initialized = true;
 
         setSynchronizingThreadsOnUpdate( true );
     }
 
     private static int desktopsUsed = 0;
 
     protected void setupDefaultInputBindings() {
         getInputHandler().addAction( getButtonUpdateAction( InputHandler.BUTTON_ALL ), InputHandler.DEVICE_MOUSE, InputHandler.BUTTON_ALL,
                 InputHandler.AXIS_NONE, false );
         getInputHandler().addAction( getXUpdateAction(), InputHandler.DEVICE_MOUSE, InputHandler.BUTTON_NONE, 0, false );
         getInputHandler().addAction( getYUpdateAction(), InputHandler.DEVICE_MOUSE, InputHandler.BUTTON_NONE, 1, false );
         getInputHandler().addAction( getWheelUpdateAction(), InputHandler.DEVICE_MOUSE, InputHandler.BUTTON_NONE, 2, false );
 
         getInputHandler().addAction( getKeyUpdateAction(), InputHandler.DEVICE_KEYBOARD, InputHandler.BUTTON_ALL, InputHandler.AXIS_NONE, false );
     }
 
     //todo: reuse the runnables
     //todo: possibly reuse events, too?
 
     public void onKey( final char character, final int keyCode, final boolean pressed ) {
         try {
             SwingUtilities.invokeAndWait( new Runnable() {
                 public void run() {
                     sendAWTKeyEvent( keyCode, pressed, character );
                 }
             } );
         } catch ( InterruptedException e ) {
             e.printStackTrace();
         } catch ( InvocationTargetException e ) {
             e.printStackTrace();
         }
     }
 
     public void onButton( final int button, final boolean pressed, final int x, final int y ) {
         convert( x, y, location );
         final int awtX = (int) location.x;
         final int awtY = (int) location.y;
         try {
             SwingUtilities.invokeAndWait( new Runnable() {
                 public void run() {
                     sendAWTMouseEvent( awtX, awtY, pressed, button );
                 }
             } );
         } catch ( InterruptedException e ) {
             e.printStackTrace();
         } catch ( InvocationTargetException e ) {
             e.printStackTrace();
         }
     }
 
     public void onWheel( final int wheelDelta, final int x, final int y ) {
         convert( x, y, location );
         final int awtX = (int) location.x;
         final int awtY = (int) location.y;
         try {
             SwingUtilities.invokeAndWait( new Runnable() {
                 public void run() {
                     sendAWTWheelEvent( wheelDelta, awtX, awtY );
                 }
             } );
         } catch ( InterruptedException e ) {
             e.printStackTrace();
         } catch ( InvocationTargetException e ) {
             e.printStackTrace();
         }
     }
 
     public void onMove( int xDelta, int yDelta, final int newX, final int newY ) {
         convert( newX, newY, location );
         final int awtX = (int) location.x;
         final int awtY = (int) location.y;
         try {
             SwingUtilities.invokeAndWait( new Runnable() {
                 public void run() {
                     sendAWTMouseEvent( awtX, awtY, false, -1 );
                 }
             } );
         } catch ( InterruptedException e ) {
             e.printStackTrace();
         } catch ( InvocationTargetException e ) {
             e.printStackTrace();
         }
     }
 
     private boolean synchronizingThreadsOnUpdate;
 
     /**
      * @return true if update and swing thread should be synchronized (avoids flickering, eats some performance)
      */
     public boolean isSynchronizingThreadsOnUpdate() {
         return synchronizingThreadsOnUpdate;
     }
 
     /**
      * Choose if update and swing thread should be synchronized (avoids flickering, eats some performance)
      *
      * @param synchronizingThreadsOnUpdate true to synchronize
      */
     public void setSynchronizingThreadsOnUpdate( boolean synchronizingThreadsOnUpdate ) {
         if ( this.synchronizingThreadsOnUpdate != synchronizingThreadsOnUpdate ) {
             this.synchronizingThreadsOnUpdate = synchronizingThreadsOnUpdate;
         }
     }
 
     private void enableAntiAlias( Graphics2D graphics ) {
         RenderingHints hints = graphics.getRenderingHints();
         if ( hints == null ) {
             hints = new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
         }
         else {
             hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
         }
         graphics.setRenderingHints( hints );
     }
 
     /**
      * @return an action that should be invoked to generate an awt event when the mouse x-coordinate is changed
      */
     public XUpdateAction getXUpdateAction() {
         return xUpdateAction;
     }
 
     /**
      * @return an action that should be invoked to generate an awt event when the mouse y-coordinate is changed
      */
     public YUpdateAction getYUpdateAction() {
         return yUpdateAction;
     }
 
     /**
      * @return an action that should be invoked to generate an awt event when the mouse wheel position is changed
      */
     public WheelUpdateAction getWheelUpdateAction() {
         return wheelUpdateAction;
     }
 
     /**
      * @param swingButtonIndex button index sent in generated swing event, InputHandler.BUTTON_ALL for using trigger index
      * @return an action that should be invoked to generate an awt event for a pressed/released mouse button
      */
     public ButtonAction getButtonUpdateAction( int swingButtonIndex ) {
         if ( swingButtonIndex == InputHandler.BUTTON_ALL ) {
             return allButtonsUpdateAction;
         }
         else {
             return new ButtonAction( swingButtonIndex );
         }
     }
 
     /**
      * @return an action that should be invoked to generate an awt event for a pressed/released key
      */
     public InputAction getKeyUpdateAction() {
         return keyUpdateAction;
     }
 
     private static class LightWeightPopup extends Popup {
         private static final Integer INTEGER_MAX_VALUE = new Integer( Integer.MAX_VALUE );
 
         public LightWeightPopup( JComponent desktop ) {
             this.desktop = desktop;
             new ScrollPaneRepaintFixListener().addTo( panel );
         }
 
         private final JComponent desktop;
 
         JPanel panel = new JPanel( new BorderLayout() );
 
         public void adjust( Component owner, Component contents, int x, int y ) {
             panel.setVisible( false );
             desktop.add( panel, INTEGER_MAX_VALUE );
             panel.removeAll();
             panel.add( contents, BorderLayout.CENTER );
             if ( contents instanceof JComponent ) {
                 JComponent jComponent = (JComponent) contents;
                 jComponent.setDoubleBuffered( false );
             }
             panel.setSize( panel.getPreferredSize() );
             y = Math.min( y, desktop.getHeight() - panel.getHeight() );
             x = Math.min( x, desktop.getWidth() - panel.getWidth() );
             panel.setLocation( x, y );
             contents.invalidate();
             panel.validate();
         }
 
         public void show() {
             panel.setVisible( true );
         }
 
         public void hide() {
             Rectangle bounds = panel.getBounds();
             desktop.remove( panel );
             desktop.repaint( bounds );
         }
     }
 
     private void sendAWTKeyEvent( int keyCode, boolean pressed, char character ) {
         keyCode = AWTKeyInput.toAWTCode( keyCode );
         if ( keyCode != 0 ) {
             Component focusOwner = getFocusOwner();
             if ( focusOwner == null ) {
                 focusOwner = desktop;
             }
             if ( focusOwner != null ) {
                 if ( pressed ) {
                     KeyEvent event = new KeyEvent( focusOwner, KeyEvent.KEY_PRESSED,
                             System.currentTimeMillis(), getCurrentModifiers( -1 ),
                             keyCode, character );
                     dispatchEvent( focusOwner, event );
                     anInt.value = keyCode;
                     Char c = (Char) characters.get( anInt );
                     if ( c == null ) {
                         characters.put( new Int( keyCode ), new Char( character ) );
                     }
                     else {
                         c.value = character;
                     }
                 }
                 if ( !pressed ) {
                     anInt.value = keyCode;
                     Char c = (Char) characters.get( anInt );
                     if ( c != null ) {
                         character = c.value;
                         //TODO: repeat input
                         if ( character != '\0' ) {
                             dispatchEvent( focusOwner, new KeyEvent( focusOwner, KeyEvent.KEY_TYPED,
                                     System.currentTimeMillis(), getCurrentModifiers( -1 ),
                                     0, character ) );
                         }
                     }
                     dispatchEvent( focusOwner, new KeyEvent( focusOwner, KeyEvent.KEY_RELEASED,
                             System.currentTimeMillis(), getCurrentModifiers( -1 ),
                             keyCode, character ) );
                 }
             }
         }
     }
 
     private void dispatchEvent( final Component receiver, final AWTEvent event ) {
         if ( getModalComponent() == null || SwingUtilities.isDescendingFrom( receiver, getModalComponent() ) ) {
             if ( !SwingUtilities.isEventDispatchThread() ) {
                 throw new IllegalStateException( "not in swing thread!" );
             }
             receiver.dispatchEvent( event );
         }
     }
 
     private static Int anInt = new Int( 0 );
 
     private static class Int {
         public Int( int value ) {
             this.value = value;
         }
 
         public boolean equals( Object obj ) {
             if ( obj instanceof Int ) {
                 return ( (Int) obj ).value == value;
             }
             else {
                 return false;
             }
         }
 
         public int hashCode() {
             return value;
         }
 
         int value;
     }
 
     private static class Char {
         public Char( char value ) {
             this.value = value;
         }
 
         char value;
     }
 
     /**
      * From keyCode (Int) to character (Char)
      */
     private Map characters = new HashMap();
 
     private static void dontDrawBackground( Container container ) {
         if ( container != null ) {
             container.setBackground( null );
             if ( container instanceof JComponent ) {
                 final JComponent component = ( (JComponent) container );
                 component.setOpaque( false );
             }
             dontDrawBackground( container.getParent() );
         }
     }
 
     private static int powerOf2SizeIfNeeded( int size, boolean generateMipMaps ) {
         if ( generateMipMaps || !TextureState.isSupportingNonPowerOfTwoTextureSize() ) {
             int powerOf2Size = 1;
             while ( powerOf2Size < size ) {
                 powerOf2Size <<= 1;
             }
             return powerOf2Size;
         }
         else {
             return size;
         }
     }
 
     private Component lastComponent;
     private Component grabbedMouse;
     private int grabbedMouseButton;
     private int downX = 0;
     private int downY = 0;
     private long lastClickTime = 0;
     private int clickCount = 0;
     private static final int MAX_CLICKED_OFFSET = 4;
 
     private Vector2f location = new Vector2f();
 
     private void sendAWTWheelEvent( int wheelDelta, int x, int y ) {
         Component comp = lastComponent != null ? lastComponent : componentAt( x, y, desktop, false );
         if ( comp == null ) {
             comp = desktop;
         }
         final Point pos = convertPoint( desktop, x, y, comp );
         final MouseWheelEvent event = new MouseWheelEvent( comp,
                 MouseEvent.MOUSE_WHEEL,
                 System.currentTimeMillis(), getCurrentModifiers( -1 ), pos.x, pos.y, 1, false,
                 MouseWheelEvent.WHEEL_UNIT_SCROLL,
                 Math.abs( wheelDelta ), wheelDelta > 0 ? -1 : 1 );
         dispatchEvent( comp, event );
     }
 
     private boolean useConvertPoint = true;
 
     private Point convertPoint( Component parent, int x, int y, Component comp ) {
         if ( useConvertPoint ) {
             try {
                 return SwingUtilities.convertPoint( parent, x, y, comp );
             } catch ( InternalError e ) {
                 useConvertPoint = false;
             }
         }
         if ( comp != null ) {
             while ( comp != parent ) {
                 x -= comp.getX();
                 y -= comp.getY();
                 if ( comp.getParent() == null ) {
                     break;
                 }
                 comp = comp.getParent();
             }
         }
         return new Point( x, y );
     }
 
     private void sendAWTMouseEvent( int x, int y, boolean pressed, int button ) {
         Component comp = componentAt( x, y, desktop, false );
 
         final int eventType;
         if ( button >= 0 ) {
             eventType = pressed ? MouseEvent.MOUSE_PRESSED : MouseEvent.MOUSE_RELEASED;
         }
         else {
             eventType = getButtonMask( -1 ) == 0 ? MouseEvent.MOUSE_MOVED : MouseEvent.MOUSE_DRAGGED;
         }
 
         final long time = System.currentTimeMillis();
         if ( lastComponent != comp ) {
             //enter/leave events
             while ( lastComponent != null && ( comp == null || !SwingUtilities.isDescendingFrom( comp, lastComponent ) ) )
             {
                 final Point pos = convertPoint( desktop, x, y, lastComponent );
                 sendExitedEvent( lastComponent, getCurrentModifiers( button ), pos );
                 lastComponent = lastComponent.getParent();
             }
             final Point pos = convertPoint( desktop, x, y, lastComponent );
             if ( lastComponent == null ) {
                 lastComponent = desktop;
             }
             sendEnteredEvent( comp, lastComponent, getCurrentModifiers( button ), pos );
             lastComponent = comp;
             downX = Integer.MIN_VALUE;
             downY = Integer.MIN_VALUE;
             lastClickTime = 0;
         }
 
         boolean clicked = false;
         if ( comp != null ) {
             if ( button >= 0 ) {
                 if ( pressed ) {
                     grabbedMouse = comp;
                     grabbedMouseButton = button;
                     downX = x;
                     downY = y;
                     setFocusOwner( componentAt( x, y, desktop, true ) );
                 }
                 else if ( grabbedMouseButton == button && grabbedMouse != null ) {
                     comp = grabbedMouse;
                     grabbedMouse = null;
                     if ( Math.abs( downX - x ) <= MAX_CLICKED_OFFSET && Math.abs( downY - y ) < MAX_CLICKED_OFFSET ) {
                         if ( lastClickTime + DOUBLE_CLICK_TIME > time ) {
                             clickCount++;
                         }
                         else {
                             clickCount = 1;
                         }
                         clicked = true;
                         lastClickTime = time;
                     }
                     downX = Integer.MIN_VALUE;
                     downY = Integer.MIN_VALUE;
                 }
             }
             else if ( grabbedMouse != null ) {
                 comp = grabbedMouse;
             }
 
             final Point pos = convertPoint( desktop, x, y, comp );
             final MouseEvent event = new MouseEvent( comp,
                     eventType,
                     time, getCurrentModifiers( button ), pos.x, pos.y, clickCount,
                     button == 1 && pressed, button >= 0 ? button : 0 );
             dispatchEvent( comp, event );
             if ( clicked ) {
                 // CLICKED seems to need special glass pane handling o_O
                 comp = componentAt( x, y, desktop, true );
                 final Point clickedPos = convertPoint( desktop, x, y, comp );
 
                 final MouseEvent clickedEvent = new MouseEvent( comp,
                         MouseEvent.MOUSE_CLICKED,
                         time, getCurrentModifiers( button ), clickedPos.x, clickedPos.y, clickCount,
                         false, button );
                 dispatchEvent( comp, clickedEvent );
             }
         }
         else if ( pressed ) {
             // clicked no component at all
             setFocusOwner( null );
         }
     }
 
     private boolean focusCleared = false;
 
     public void setFocusOwner( Component comp ) {
         if ( comp == null || comp.isFocusable() ) {
             for ( Component p = comp; p != null; p = p.getParent() ) {
                 if ( p instanceof JInternalFrame ) {
                     try {
                         ( (JInternalFrame) p ).setSelected( true );
                     } catch ( PropertyVetoException e ) {
                         e.printStackTrace();
                     }
                 }
             }
             awtWindow.setFocusableWindowState( true );
             Component oldFocusOwner = getFocusOwner();
             if ( comp == desktop ) {
                 comp = null;
             }
             if ( oldFocusOwner != comp ) {
                 if ( oldFocusOwner != null ) {
                     dispatchEvent( oldFocusOwner, new FocusEvent( oldFocusOwner,
                             FocusEvent.FOCUS_LOST, false, comp ) );
                 }
                 KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
                 if ( comp != null ) {
                     dispatchEvent( comp, new FocusEvent( comp,
                             FocusEvent.FOCUS_GAINED, false, oldFocusOwner ) );
                 }
             }
             awtWindow.setFocusableWindowState( false );
         }
         focusCleared = comp == null;
     }
 
     private int getCurrentModifiers( int button ) {
         int modifiers = 0;
         if ( isKeyDown( KeyInput.KEY_LMENU ) ) {
             modifiers |= KeyEvent.ALT_DOWN_MASK;
             modifiers |= KeyEvent.ALT_MASK;
         }
         if ( isKeyDown( KeyInput.KEY_RMENU ) ) {
             modifiers |= KeyEvent.ALT_GRAPH_DOWN_MASK;
             modifiers |= KeyEvent.ALT_GRAPH_MASK;
         }
         if ( isKeyDown( KeyInput.KEY_LCONTROL ) || isKeyDown( KeyInput.KEY_RCONTROL ) ) {
             modifiers |= KeyEvent.CTRL_DOWN_MASK;
             modifiers |= KeyEvent.CTRL_MASK;
         }
         if ( isKeyDown( KeyInput.KEY_LSHIFT ) || isKeyDown( KeyInput.KEY_RSHIFT ) ) {
             modifiers |= KeyEvent.SHIFT_DOWN_MASK;
             modifiers |= KeyEvent.SHIFT_MASK;
         }
         return modifiers | getButtonMask( button );
     }
 
     private boolean isKeyDown( int key ) {
         return KeyInput.get().isKeyDown( key );
     }
 
     private int getButtonMask( int button ) {
         int buttonMask = 0;
         if ( MouseInput.get().isButtonDown( 0 ) || button == 0 ) {
             buttonMask |= MouseEvent.BUTTON1_MASK;
             buttonMask |= MouseEvent.BUTTON1_DOWN_MASK;
         }
         if ( MouseInput.get().isButtonDown( 1 ) || button == 1 ) {
             buttonMask |= MouseEvent.BUTTON2_MASK;
             buttonMask |= MouseEvent.BUTTON2_DOWN_MASK;
         }
         if ( MouseInput.get().isButtonDown( 2 ) || button == 2 ) {
             buttonMask |= MouseEvent.BUTTON3_MASK;
             buttonMask |= MouseEvent.BUTTON3_DOWN_MASK;
         }
         return buttonMask;
     }
 
     private int lastXin = -1;
     private int lastXout = -1;
     private int lastYin = -1;
     private int lastYout = -1;
 
     private Ray pickRay = new Ray();
     private Vector3f bottomLeft = new Vector3f();
     private Vector3f topLeft = new Vector3f();
     private Vector3f topRight = new Vector3f();
     private Vector3f bottomRight = new Vector3f();
     private Vector3f tuv = new Vector3f();
 
     private void convert( int x, int y, Vector2f store ) {
         if ( lastXin == x && lastYin == y ) {
             store.x = lastXout;
             store.y = lastYout;
         }
         else {
             lastXin = x;
             lastYin = y;
             if ( getRenderQueueMode() == Renderer.QUEUE_ORTHO ) {
                 //TODO: occlusion by other quads (JMEFrames)
                 x = (int) ( x - getWorldTranslation().x + desktopWidth / 2 );
                y = (int) ( DisplaySystem.getDisplaySystem().getHeight() - y - getWorldTranslation().y + desktopHeight / 2 );
             }
             else {
                 store.set( x, y );
                 DisplaySystem.getDisplaySystem().getWorldCoordinates( store, 0, pickRay.origin );
                 DisplaySystem.getDisplaySystem().getWorldCoordinates( store, 0.3f, pickRay.direction ).subtractLocal( pickRay.origin ).normalizeLocal();
 
                 applyWorld( bottomLeft.set( -width * 0.5f, -height * 0.5f, 0 ) );
                 applyWorld( topLeft.set( -width * 0.5f, height * 0.5f, 0 ) );
                 applyWorld( topRight.set( width * 0.5f, height * 0.5f, 0 ) );
                 applyWorld( bottomRight.set( width * 0.5f, -height * 0.5f, 0 ) );
 
                 if ( pickRay.intersectWherePlanarQuad( topLeft, topRight, bottomLeft, tuv ) ) {
                     x = (int) ( ( tuv.y - 0.5f ) * width ) + desktopWidth / 2;
                     y = (int) ( ( tuv.z - 0.5f ) * height ) + desktopHeight / 2;
                 }
                 else {
                     x = -1;
                     y = -1;
                 }
             }
             lastYout = y;
             lastXout = x;
 
             store.set( x, y );
         }
     }
 
     private void applyWorld( Vector3f point ) {
         getWorldRotation().multLocal( point.multLocal( getWorldScale() ) ).addLocal( getWorldTranslation() );
     }
 
     private Component componentAt( int x, int y, Component parent, boolean scanRootPanes ) {
         if ( scanRootPanes && parent instanceof JRootPane ) {
             JRootPane rootPane = (JRootPane) parent;
             parent = rootPane.getContentPane();
         }
 
         Component child = parent;
         if ( !parent.contains( x, y ) ) {
             child = null;
         }
         else {
             synchronized ( parent.getTreeLock() ) {
                 if ( parent instanceof Container ) {
                     Container container = (Container) parent;
                     int ncomponents = container.getComponentCount();
                     for ( int i = 0; i < ncomponents; i++ ) {
                         Component comp = container.getComponent( i );
                         if ( comp != null
                                 && comp.isVisible()
                                 && comp.contains( x - comp.getX(), y - comp.getY() ) ) {
                             child = comp;
                             break;
                         }
                     }
                 }
             }
         }
 
         if ( child != null ) {
             if ( parent instanceof JTabbedPane && child != parent ) {
                 child = ( (JTabbedPane) parent ).getSelectedComponent();
             }
             x -= child.getX();
             y -= child.getY();
         }
         return child != parent && child != null ? componentAt( x, y, child, scanRootPanes ) : child;
     }
 
     private void sendEnteredEvent( Component comp, Component lastComponent, int buttonMask, Point pos ) {
         if ( comp != null && comp != lastComponent ) {
             sendEnteredEvent( comp.getParent(), lastComponent, buttonMask, pos );
 
             pos = convertPoint( lastComponent, pos.x, pos.y, comp );
             final MouseEvent event = new MouseEvent( comp,
                     MouseEvent.MOUSE_ENTERED,
                     System.currentTimeMillis(), buttonMask, pos.x, pos.y, 0, false, 0 );
             dispatchEvent( comp, event );
         }
 
     }
 
     private void sendExitedEvent( Component lastComponent, int buttonMask, Point pos ) {
         final MouseEvent event = new MouseEvent( lastComponent,
                 MouseEvent.MOUSE_EXITED,
                 System.currentTimeMillis(), buttonMask, pos.x, pos.y, 1, false, 0 );
         dispatchEvent( lastComponent, event );
     }
 
     private final LockRunnable paintLockRunnable = new LockRunnable();
 
     public void draw( Renderer r ) {
         if ( graphics.isDirty() ) {
             final boolean synchronizingThreadsOnUpdate = this.synchronizingThreadsOnUpdate;
             if ( synchronizingThreadsOnUpdate ) {
                 synchronized ( paintLockRunnable ) {
                     try {
                         paintLockRunnable.wait = true;
                         SwingUtilities.invokeLater( paintLockRunnable );
                         paintLockRunnable.wait( 100 );
                     } catch ( InterruptedException e ) {
                         e.printStackTrace();
                     }
                 }
             }
             try {
                 if ( graphics != null ) {
                     graphics.update( texture );
                 }
             } finally {
 
                 if ( synchronizingThreadsOnUpdate ) {
                     synchronized ( paintLockRunnable ) {
                         paintLockRunnable.notifyAll();
                     }
                 }
             }
         }
         super.draw( r );
     }
 
     public JDesktopPane getJDesktop() {
         return desktop;
     }
 
     public Component getFocusOwner() {
         if ( !focusCleared ) {
             return this.awtWindow.getFocusOwner();
         }
         else {
             return null;
         }
     }
 
     private class LockRunnable implements Runnable {
         private boolean wait = false;
 
         public void run() {
             synchronized ( paintLockRunnable ) {
                 notifyAll();
                 if ( wait ) {
                     try {
                         //wait for repaint to finish
                         wait = false;
                         paintLockRunnable.wait( 200 );
                     } catch ( InterruptedException e ) {
                         e.printStackTrace();
                     }
                 }
             }
         }
     }
 
     private static class MyPopupFactory extends PopupFactory {
         private final PopupFactory defaultPopupFactory = new PopupFactory();
 
         public Popup getPopup( Component owner, Component contents, int x, int y ) throws IllegalArgumentException {
             while ( !( owner instanceof JDesktopPane ) ) {
                 owner = owner.getParent();
                 if ( owner == null ) {
                     LoggingSystem.getLogger().warning( "jME Popup creation failed, default popup created - desktop not found in component hierarchy of " + owner );
                     return defaultPopupFactory.getPopup( owner, contents, x, y );
                 }
             }
             JMEDesktop.LightWeightPopup popup = new JMEDesktop.LightWeightPopup( (JComponent) owner );
             popup.adjust( owner, contents, x, y );
             return popup;
         }
     }
 
     private class ButtonAction extends InputAction {
         private final int swingButtonIndex;
 
         /**
          * @param swingButtonIndex button index sent in generated swing event, InputHandler.BUTTON_ALL for using trigger index
          */
         public ButtonAction( int swingButtonIndex ) {
             this.swingButtonIndex = swingButtonIndex;
         }
 
         public void performAction( InputActionEvent evt ) {
             onButton( swingButtonIndex != InputHandler.BUTTON_ALL ? swingButtonIndex : evt.getTriggerIndex(), evt.getTriggerPressed(),
                     lastXin, lastYin );
         }
 
     }
 
     private class XUpdateAction extends InputAction {
         public XUpdateAction() {
             setSpeed( 1 );
         }
 
         public void performAction( InputActionEvent evt ) {
             int screenWidth = DisplaySystem.getDisplaySystem().getWidth();
             onMove( (int) ( screenWidth * evt.getTriggerDelta() * getSpeed() ), 0,
                     (int) ( screenWidth * evt.getTriggerPosition() * getSpeed() ), lastYin );
         }
     }
 
     private class YUpdateAction extends InputAction {
         public YUpdateAction() {
             setSpeed( 1 );
         }
 
         public void performAction( InputActionEvent evt ) {
             int screenHeight = DisplaySystem.getDisplaySystem().getHeight();
             onMove( 0, (int) ( screenHeight * evt.getTriggerDelta() * getSpeed() ), lastXin,
                     (int) ( screenHeight * evt.getTriggerPosition() * getSpeed() ) );
         }
     }
 
     private class WheelUpdateAction extends InputAction {
         public WheelUpdateAction() {
             setSpeed( 1 );
         }
 
         public void performAction( InputActionEvent evt ) {
             onWheel( (int) ( evt.getTriggerDelta() * getSpeed() ), lastXin, lastYin );
         }
     }
 
     private class KeyUpdateAction extends InputAction {
         public void performAction( InputActionEvent evt ) {
             onKey( evt.getTriggerCharacter(), evt.getTriggerIndex(), evt.getTriggerPressed() );
         }
     }
 
 
     /**
      * @return current modal component
      * @see #setModalComponent(java.awt.Component)
      */
     public Component getModalComponent() {
         return this.modalComponent;
     }
 
     /**
      * @see #setModalComponent(java.awt.Component)
      */
     private Component modalComponent;
 
     /**
      * Filter the swing event to allow events to the specified component and its children only.
      * Note: this does not prevent shortcuts and mnemonics to work for the other components!
      *
      * @param value component that can be exclusively accessed (including children)
      */
     public void setModalComponent( final Component value ) {
         this.modalComponent = value;
     }
 
     protected void setParent( Node parent ) {
         if ( desktop != null ) {
             super.setParent( parent );
         } else {
             throw new IllegalStateException("already disposed");
         }
     }
 
     /**
      * Call this method of the desktop is no longer needed. Removes this from the scenegraph, later use is not
      * possible any more.
      */
     public void dispose() {
         if ( desktop != null ) {
             if ( getParent() != null ) {
                 getParent().detachChild( this );
             }
             desktop.removeAll();
             awtWindow.dispose();
             inputHandler.removeAllActions();
             if ( inputHandler.getParent() != null ) {
                 inputHandler.getParent().removeFromAttachedHandlers( inputHandler );
             }
             desktop = null;
             desktopsUsed--;
             if ( desktopsUsed == 0 ) {
                 PopupFactory.setSharedInstance( new PopupFactory() );
             }
         }
     }
 
     private static class ScrollPaneRepaintFixListener implements ContainerListener {
         public void componentAdded( ContainerEvent e ) {
             Component child = e.getChild();
             componentAdded( child );
         }
 
         private void componentAdded( Component child ) {
             if ( child instanceof Container ) {
                 Container container = (Container) child;
                 addTo( container );
                 container.addContainerListener( this );
             }
             if ( child instanceof JScrollPane ) {
                 final JScrollPane scrollPane = (JScrollPane) child;
                 // note: the listener added here is only a fix for repaint problems with scrolling
                 subscribeRepaintListener( scrollPane.getViewport() );
             }
         }
 
         private void addTo( Container container ) {
             container.addContainerListener( this );
             for ( int i = 0; i < container.getComponentCount(); i++ ) {
                 componentAdded( container.getComponent( i ) );
             }
         }
 
         private void removeFrom( Container container ) {
             container.removeContainerListener( this );
             for ( int i = 0; i < container.getComponentCount(); i++ ) {
                 componentRemoved( container.getComponent( i ) );
             }
         }
 
         private void subscribeRepaintListener( JViewport viewport ) {
             for ( int i = 0; i < viewport.getChangeListeners().length; i++ ) {
                 ChangeListener listener = viewport.getChangeListeners()[i];
                 if ( listener instanceof ScrollPaneRepaintChangeListener ) {
                     // listener already subscribed
                     return;
                 }
             }
             viewport.addChangeListener( new ScrollPaneRepaintChangeListener( viewport ) );
         }
 
         public void componentRemoved( ContainerEvent e ) {
             Component child = e.getChild();
             componentRemoved( child );
         }
 
         private void componentRemoved( Component child ) {
             if ( child instanceof Container ) {
                 Container container = (Container) child;
                 removeFrom( container );
             }
         }
 
         private static class ScrollPaneRepaintChangeListener implements ChangeListener {
             private final Component component;
 
             public ScrollPaneRepaintChangeListener( Component component ) {
                 this.component = component;
             }
 
             public void stateChanged( ChangeEvent e ) {
                 component.repaint();
             }
         }
     }
 }
