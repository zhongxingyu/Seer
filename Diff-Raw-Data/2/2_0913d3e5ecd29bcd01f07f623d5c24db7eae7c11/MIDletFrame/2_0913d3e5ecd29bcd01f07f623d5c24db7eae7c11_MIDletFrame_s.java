 package net.intensicode.runme;
 
 import net.intensicode.runme.util.Log;
 
 import javax.microedition.lcdui.DisplayContext;
 import javax.microedition.midlet.MIDlet;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.awt.image.VolatileImage;
 
 
 public final class MIDletFrame extends JFrame implements DisplayContext, KeyListener, FocusListener
     {
     public MIDletFrame( final int aWidth, final int aHeight )
         {
         myDisplay = new MIDletDisplay( this, aWidth, aHeight );
 
         setDefaultCloseOperation( EXIT_ON_CLOSE );
         setFullScreenMode( false );
 
         addKeyListener( this );
         addFocusListener( this );
         }
 
     public final void start( final MIDlet aMIDlet )
         {
         if ( myContainer != null ) throw new IllegalStateException();
 
         myContainer = new MIDletContainer( aMIDlet, myDisplay );
         myContainer.start();
 
         aMIDlet.frame = this;
 
         final StringBuilder builder = new StringBuilder();
         builder.append( aMIDlet.getClass().getSimpleName() );
         builder.append( " (IntensiCode RunME) - www.intensicode.net / www.berlinfactor.com" );
         setTitle( builder.toString() );
         }
 
     public final void pause()
         {
         if ( myPausedFlag ) return;
         myPausedFlag = true;
         }
 
     // From DisplayContext
 
     public final int displayWidth()
         {
         return myDisplay.width;
         }
 
     public final int displayHeight()
         {
         return myDisplay.height;
         }
 
     public final synchronized javax.microedition.lcdui.Graphics displayGraphics()
         {
         if ( myDisplayBuffer == null )
             {
             myDisplayBuffer = createVolatileImage( myDisplay.width, myDisplay.height );
             myMIDletGraphics = new javax.microedition.lcdui.Graphics( myDisplayBuffer );
             }
         return myMIDletGraphics;
         }
 
     public final synchronized void onRepaintDone()
         {
         if ( myDisplayBuffer == null ) return;
         if ( myDisplayBuffer.contentsLost() ) return;
 
         final BufferStrategy strategy = getBufferStrategy();
         if ( strategy == null ) return;
 
         final Graphics graphics = strategy.getDrawGraphics();
         if ( graphics == null ) return;
 
         paint( graphics, myDisplayBuffer );
 
         graphics.dispose();
         strategy.show();
         }
 
     // From FocusListener
 
     public void focusGained( final FocusEvent e )
         {
         myContainer.setVisible( true );
         if ( myPausedFlag )
             {
             Log.create().debug( "MIDletFrame resuming MIDlet after pause" );
             myPausedFlag = false;
             myContainer.start();
             }
         }
 
     public void focusLost( final FocusEvent e )
         {
         myContainer.setVisible( false );
         }
 
     // From KeyListener
 
     public final void keyPressed( final KeyEvent aKeyEvent )
         {
         if ( aKeyEvent.getKeyCode() == KeyEvent.VK_ENTER )
             {
             if ( aKeyEvent.getModifiersEx() == KeyEvent.ALT_DOWN_MASK )
                 {
                 toggleFullScreenMode();
                 return;
                 }
             }
 
         if ( myContainer != null ) myContainer.keyPressed( aKeyEvent );
         }
 
     public final void keyReleased( final KeyEvent aKeyEvent )
         {
         if ( myContainer != null ) myContainer.keyReleased( aKeyEvent );
         }
 
     public final void keyTyped( final KeyEvent aKeyEvent )
         {
         if ( myContainer != null ) myContainer.keyTyped( aKeyEvent );
         }
 
     // Implementation
 
     private final synchronized void toggleFullScreenMode()
         {
         final boolean isInFullScreenMode = getGraphicsDevice().getFullScreenWindow() == this;
         if ( isInFullScreenMode ) setFullScreenMode( false );
         else setFullScreenMode( true );
         }
 
     private final void setFullScreenMode( final boolean aFullScreenFlag )
         {
         setVisible( false );
         dispose();
 
         if ( myMIDletGraphics != null ) myMIDletGraphics.dispose();
         myMIDletGraphics = null;
         myDisplayBuffer = null;
 
         if ( aFullScreenFlag ) activateFullScreenMode();
         else activateWindowedMode();
 
         try
             {
             Thread.sleep( 250 );
             }
         catch ( final InterruptedException e )
             {
             }
 
         javax.microedition.lcdui.Image.target_gc = getGraphicsConfiguration();
         }
 
     private final void activateWindowedMode()
         {
         setUndecorated( false );
 
         getGraphicsDevice().setFullScreenWindow( null );
 
         centerFrame();
         setVisible( true );
         setCursor( Cursor.getDefaultCursor() );
 
         createBufferStrategy( 2 );
         }
 
     private final void activateFullScreenMode()
         {
         setUndecorated( true );
 
         final GraphicsDevice device = getGraphicsDevice();
         device.setFullScreenWindow( this );
 
         final DisplayMode displayMode = device.getDisplayMode();
         setBounds( 0, 0, displayMode.getWidth(), displayMode.getHeight() );
 
         setVisible( true );
         setCursor( getEmptyCursor() );
 
         createBufferStrategy( 2 );
         }
 
     private final void centerFrame()
         {
         final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         final int width = screenSize.width * 2 / 3;
         final int height = screenSize.height * 2 / 3;
         final int x = (screenSize.width - width) / 2;
         final int y = (screenSize.height - height) / 2;
         setBounds( x, y, width, height );
         }
 
     private final void paint( final Graphics aGraphics, final VolatileImage aImage )
         {
         final Insets insets = getInsets();
         aGraphics.translate( insets.left, insets.top );
 
         final int imageWidth = aImage.getWidth();
         final int imageHeight = aImage.getHeight();
         final int screenWidth = getWidth() - insets.left - insets.right;
         final int screenHeight = getHeight() - insets.top - insets.bottom;
         final float xScale = screenWidth * 1.0f / imageWidth;
         final float yScale = screenHeight * 1.0f / imageHeight;
         final float scale = Math.min( xScale, yScale );
         final int width = (int) ( imageWidth * scale );
         final int height = (int) ( imageHeight * scale );
         final int x = ( screenWidth - width ) / 2;
         final int y = ( screenHeight - height ) / 2;
         aGraphics.setColor( Color.DARK_GRAY );
         aGraphics.fillRect( 0, 0, x, screenHeight );
         aGraphics.fillRect( 0, 0, screenWidth, y );
         aGraphics.fillRect( x + width, 0, screenWidth - x - width, screenHeight );
         aGraphics.fillRect( 0, y + height, screenWidth, screenHeight - y - height );
         aGraphics.drawImage( aImage, x, y, width, height, null );
 
         aGraphics.translate( -insets.left, -insets.top );
         }
 
     private final GraphicsDevice getGraphicsDevice()
         {
         return getGraphicsConfiguration().getDevice();
         }
 
     private final Cursor getEmptyCursor()
         {
         if ( myEmptyCursor == null )
             {
            final Point hotSpot = new Point( 1, 1 );
             final BufferedImage emptyImage = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB );
             myEmptyCursor = Toolkit.getDefaultToolkit().createCustomCursor( emptyImage, hotSpot, "INVISIBLE" );
             }
         return myEmptyCursor;
         }
 
 
 
     private boolean myPausedFlag;
 
     private Cursor myEmptyCursor;
 
     private MIDletContainer myContainer;
 
     private VolatileImage myDisplayBuffer;
 
     private final MIDletDisplay myDisplay;
 
     private javax.microedition.lcdui.Graphics myMIDletGraphics;
     }
