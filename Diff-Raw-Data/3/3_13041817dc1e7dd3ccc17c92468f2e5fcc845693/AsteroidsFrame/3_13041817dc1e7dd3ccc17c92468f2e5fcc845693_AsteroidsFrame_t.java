 /*
  * DISASTEROIDS
  * AsteroidsFrame.java
  */
 package disasteroids.gui;
 
 import disasteroids.Game;
 import disasteroids.GameLoop;
 import disasteroids.Running;
 import disasteroids.Settings;
 import disasteroids.Ship;
 import disasteroids.networking.Client;
 import disasteroids.networking.Server;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Toolkit;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.geom.AffineTransform;
 
 /**
  * The big momma class up in the sky.
  * 
  * @author Andy Kooiman, Phillip Cohen
  * @since Classic
  */
 public class AsteroidsFrame extends Frame
 {
     /**
      * Dimensions of the window when not in fullscreen mode.
      * @since November 15 2007
      */
     private static final int WINDOW_WIDTH = 900, WINDOW_HEIGHT = 750;
 
     private AsteroidsPanel panel;
 
     public AsteroidsPanel getPanel()
     {
         return panel;
     }
     /**
      * ID of the player that's at this computer.
      */
     public int localId;
 
     private static AsteroidsFrame frame;
 
     public static AsteroidsFrame frame()
     {
         return frame;
     }
 
     /**
      * Constructs the game frame and game elements.
      * 
      * @param localId   id of the player at this computer
      * @since December 14, 2007
      */
     public AsteroidsFrame( int localId )
     {
         frame = this;
         this.localId = localId;
 
         // Reflect the network state.
         if ( Server.is() )
             setTitle( "Disasteroids (server)" );
         else if ( Client.is() )
             setTitle( "Disasteroids (client)" );
         else
             setTitle( "Disasteroids" );
 
         panel = new AsteroidsPanel( this );
 
         add( panel );
         setResizable( true );
 
         // Close when the exit key is pressed.
         AsteroidsFrameAdapter a = new AsteroidsFrameAdapter();
         addWindowListener( a );
         //   addComponentListener( a );
 
         // Set our size - fullscreen or windowed.
         updateFullscreen();
 
         // Receive key events.
         addKeyListener( new KeystrokeManager() );
 
         GameLoop.startLoop();
     }
 
     public void nextLevel()
     {
         panel.background.init();
         ParticleManager.clear();
     }
 
     public void showStartMessage( String message )
     {
         AsteroidsFrame.frame().getPanel().getStarBackground().writeOnBackground( message,
                 (int) AsteroidsFrame.frame().localPlayer().getX(), (int) AsteroidsFrame.frame().localPlayer().getY() - 40, 0, 50,
                 AsteroidsFrame.frame().localPlayer().getColor(), new Font( "Century Gothic", Font.BOLD, 20 ) );
     }
 
     /**
      * Resets the background, high score, and notification messages.
      * 
      * @since December 25, 2007
      */
     public void resetGame()
     {
         panel.background.clearMessages();
         panel.notificationMessages.clear();
         ParticleManager.clear();
 
         // Reset the background.
         panel.background.init();
     }
 
     public static void addNotificationMessage( String message, int life )
     {
         if ( frame() != null )
             frame().panel.addNotificationMessage( message, life );
     }
 
     /**
      * Toggles fullscreen on/off. 
      * 
      * @since December 11, 2007
      */
     public void toggleFullscreen()
     {
         Settings.setUseFullscreen( !Settings.isUseFullscreen() );
         Running.log( "The game will run " + ( Settings.isUseFullscreen() ? "in fullscreen" : "as a window" ) + " after you restart." );
         /* [PC] This is rather problematic.
         updateFullscreen();
          */
     }
 
     @Override
     public void setSize( int width, int height )
     {
         super.setSize( width, height );
 
         // Force the main image to be resized.
         panel.virtualMem = null;
     }
 
     @Override
     public void setSize( Dimension d )
     {
         super.setSize( d );
 
         // Force the main image to be resized.
         panel.virtualMem = null;
     }
 
     /**
      * Sets the fullscreen/window mode based on the setting.
      *
      * @since December 11, 2007
      */
     private void updateFullscreen()
     {
         GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
 
         // Set fullscreen mode if we're not already.
         if ( Settings.isUseFullscreen() && graphicsDevice.getFullScreenWindow() != this )
         {
             dispose();
             setUndecorated( true );
             panel.setSize( graphicsDevice.getDisplayMode().getWidth(), graphicsDevice.getDisplayMode().getHeight() );
             setSize( graphicsDevice.getDisplayMode().getWidth(), graphicsDevice.getDisplayMode().getHeight() );
             pack();
             graphicsDevice.setFullScreenWindow( this );
 
             // Hide the cursor.
             Image cursorImage = Toolkit.getDefaultToolkit().getImage( "xparent.gif" );
             Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor( cursorImage, new Point( 0, 0 ), "" );
             setCursor( blankCursor );
 
             // Re-create the background.
             if ( panel.background != null )
                 panel.background.init();
         }
         // Set windowed mode if we're not already.
         else if ( ( getSize().width != WINDOW_WIDTH ) || ( getSize().height != WINDOW_HEIGHT ) )
         {
             setVisible( false );
             dispose();
             setUndecorated( false );
             setSize( WINDOW_WIDTH, WINDOW_HEIGHT );
 
             if ( Client.is() )
                 setLocation( 0 + 3 + WINDOW_WIDTH, 0 );
             else
                 setLocation( 0, 0 );
 
             graphicsDevice.setFullScreenWindow( null );
 
             // Show the cursor.
             setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
 
             // Re-create the background.
             if ( panel.background != null )
                 panel.background.init();
         }
         setVisible( true );
     }
 
     /**
      * Draws a point at specified coordinates, translated relative to local ship.
      * @param graph The <code>Graphics</code> context in which to draw
      * @param col The <code>Color</code> in which to draw
      * @param x The x coordinate
      * @param y The y coordinate
      * @since December 16, 2007
      */
     public void drawPoint( Graphics graph, Color col, int x, int y )
     {
         x = RelativeGraphics.translateX( x );
         y = RelativeGraphics.translateY( y );
         graph.setColor( col );
         if ( x > -2 && x < Game.getInstance().GAME_WIDTH + 2 && y > -2 && y < Game.getInstance().GAME_HEIGHT + 2 )
             graph.drawRect( x, y, 0, 0 );
     }
 
     /**
      * Draws a circle with center at coordinates translated relative to local ship with given radius in given color
      * @param col The <code>Color</code> in which the circle will be drawn
      * @param x The x coordinate of the center
      * @param y The y coordinate of the center
      * @param radius The radius of the circle
      * @since December 15, 2007
      */
     public void drawCircle( Graphics graph, Color col, int x, int y, int radius )
     {
         x = RelativeGraphics.translateX( x );
         y = RelativeGraphics.translateY( y );
         graph.setColor( col );
         if ( x > -radius * 2 && x < Game.getInstance().GAME_WIDTH + radius * 2 && y > -radius * 2 && y < Game.getInstance().GAME_HEIGHT + radius * 2 )
             graph.drawOval( x - radius, y - radius, radius * 2, radius * 2 );
     }
 
     /**
      * Draws a line from one coordinate to another in a given color.
      * 
      * @param col   the <code>Color</code> in which the circle will be drawn
      * @param x1    the first x coordinate
      * @param y1    the first y coordinate
      * @param x2    the second x coordinate
      * @param y2    the second y coordinate
      * @since December 15, 2007
      */
     public void drawLine( Graphics graph, Color col, int x1, int y1, int x2, int y2 )
     {
         x1 = (int) ( ( x1 - localPlayer().getX() + getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH );
         y1 = (int) ( ( y1 - localPlayer().getY() + getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT );
         x2 = (int) ( ( x2 - localPlayer().getX() + getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH );
         y2 = (int) ( ( y2 - localPlayer().getY() + getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT );
         graph.setColor( col );
         graph.drawLine( x1, y1, x2, y2 );
     }
 
     public void drawLine( Graphics graph, Color col, int x, int y, int length, double angle )
     {
         x = RelativeGraphics.translateX( x );
         y = RelativeGraphics.translateY( y );
         graph.setColor( col );
         if ( x > -length && x < Game.getInstance().GAME_WIDTH + length && y > -length && y < Game.getInstance().GAME_HEIGHT + length )
             graph.drawLine( x, y, (int) ( x + length * Math.cos( angle ) ), (int) ( y - length * Math.sin( angle ) ) );
     }
 
     public void drawLine( Graphics graph, Color col, int x, int y, int length, int offset, double angle )
     {
         x = RelativeGraphics.translateX( x );
         y = RelativeGraphics.translateY( y );
         graph.setColor( col );
         if ( x > -length && x < Game.getInstance().GAME_WIDTH + length && y > -length && y < Game.getInstance().GAME_HEIGHT + length )
             graph.drawLine( (int) ( x + offset * Math.cos( angle ) ), (int) ( y - offset * Math.sin( angle ) ), (int) ( x + length * Math.cos( angle ) ), (int) ( y - length * Math.sin( angle ) ) );
     }
 
     /**
      * Draws a circle with center at coordinates with given radius in given color.
      * 
      * @param col       the <code>Color</code> in which the circle will be drawn
      * @param x         the x coordinate of the center
      * @param y         the y coordinate of the center
      * @param radius    the radius of the circle
      * @since December 15, 2007
      */
     public void fillCircle( Graphics graph, Color col, int x, int y, int radius )
     {
         x = RelativeGraphics.translateX( x );
         y = RelativeGraphics.translateY( y );
         graph.setColor( col );
         if ( x > -2 * radius && x < Game.getInstance().GAME_WIDTH + radius * 2 && y > -radius * 2 && y < Game.getInstance().GAME_HEIGHT + radius * 2 )
             graph.fillOval( x - radius, y - radius, radius * 2, radius * 2 );
     }
 
     public void drawString( Graphics graph, int x, int y, String str, Color col )
     {
         x = RelativeGraphics.translateX( x );
         y = RelativeGraphics.translateY( y );
         graph.setColor( col );
         if ( x > -50 && x < Game.getInstance().GAME_WIDTH && y > -50 && y < Game.getInstance().GAME_HEIGHT )
         {
             // drawString doesn't support linebreaks, so we do that here.
             String[] lines = str.split( "\n" );
             for ( String line : lines )
             {
                 graph.drawString( line, x - (int) graph.getFont().getStringBounds( line, ( (Graphics2D) graph ).getFontRenderContext() ).getWidth() / 2, y );
                 y += (int) graph.getFont().getStringBounds( line, ( (Graphics2D) graph ).getFontRenderContext() ).getHeight();
             }
         }
     }
 
     /**
      * Draws a polygon in one color with a background of another color.
      * 
      * @param p         the <code>Polygon</code> to be drawn
      * @param fill      the <code>Color</code> in which the <code>Polygon</code> will be drawn
      * @param outline   the <code>Color</code> of the outline
      * @since December 15, 2007
      */
     public void drawPolygon( Graphics graph, Color fill, Color outline, Polygon p )
     {
         graph.setColor( fill );
         graph.fillPolygon( p );
         graph.setColor( outline );
         graph.drawPolygon( p );
     }
 
     /**
      * Draws a circle with center at given point with given radius in one color, with an outline of another color.
      * 
      * @param fill      the <code>Color</code> in which the circle will be drawn
      * @param outline   the <code>Color</code> of the outline
      * @param x         the x coordinate of the center
      * @param y         the y coordinate of the center
      * @param radius    the radius
      * @since December 15, 2007
      */
     public void drawOutlinedCircle( Graphics graph, Color fill, Color outline, int x, int y, int radius )
     {
         fillCircle( graph, fill, x, y, radius );
         drawCircle( graph, outline, x, y, radius );
     }
 
     /**
      * Draws an <code>Image</code> correctly rotated, translated, and scaled
      * @param g The <code>Graphics</code> context in which to draw
      * @param img The raw <code>Image</code> to draw
      * @param x The absolute x coordinate of the center of the <code>Image</code>
      * @param y The absolute y coordinate of the center of the <code>Image</code>
      * @param angle The angle to rotate; positive is clockwise
      * @param scale The relative size of the desired image; 1.0 is the same size as the stock image
      * 
      * @since March 30, 2008
      */
     public void drawImage( Graphics g, Image img, int x, int y, double angle, double scale )
     {
         // [PC] Prevent a bug caused by the game drawing asteroids and aliens (anything that scales, really) before they're fully constructed.
         if ( scale < 0 )
             return;
 
         AffineTransform af = new AffineTransform();
         af.translate( RelativeGraphics.translateX( x ), RelativeGraphics.translateY( y ) );
         af.scale( scale, scale );
         af.rotate( angle );
         af.translate( -img.getWidth( null ) / 2, -img.getHeight( null ) / 2 );
         ( (Graphics2D) g ).drawImage( img, af, null );
     }
 
     /**
      * Draws an <code>Image</code> at a specified location.  Equivalent to the call
      * of drawImage(g, img, x, y, 0.0, 1.0), but more efficient.
      * 
      * @param g The <code>Graphics</code> context in which to draw
      * @param img The <code>Image</code> to draw
      * @param x The absolute x coordinate of the center of the <code>Image</code>
      * @param y The absolute y coordinate of the center of the <code>Image</code>
      * 
      * @since March 30,2008
      */
     public void drawImage( Graphics g, Image img, int x, int y )
     {
         g.drawImage( img, RelativeGraphics.translateX( x ) - img.getWidth( null ) / 2,
                 RelativeGraphics.translateY( y ) - img.getHeight( null ) / 2, null );
     }
 
     /**
      * Adds a new message to the on-screen list.
      * These messages should be relevant to the local player.
      * 
      * @param message   the message text
      */
     public static void addNotificationMessage( String message )
     {
         if ( message.equals( "" ) )
             return;
 
         addNotificationMessage( message, 250 );
     }
 
     /**
      * Returns the player at this computer.
      * 
      * @return  the <code>Ship</code> controlled by the player at this computer
      * @since December 19, 2007
      */
     public Ship localPlayer()
     {
         return (Ship) Game.getInstance().getObjectManager().getObject( localId );
     }
 
     /**
      * A simple handler for the frame's window buttons.
      * 
      * @since December 15, 2007
      */
     private static class AsteroidsFrameAdapter extends WindowAdapter implements ComponentListener
     {
         /**
          * Invoked when a window has been closed.
          * 
          * @param e see <code>WindowListener</code>
          */
         @Override
         public void windowClosing( WindowEvent e )
         {
             frame().dispose();
             Running.quit();
         }
 
         @Override
         public void windowGainedFocus( WindowEvent e )
         {
            //[MW] this seemed to be the problem with the focus being lost, so fixed
            //AsteroidsFrame.frame().addKeyListener( KeystrokeManager.getInstance() );
         }
 
         public void componentResized( ComponentEvent e )
         {
             AsteroidsFrame.frame().setSize( e.getComponent().getWidth(), e.getComponent().getHeight() );
         }
 
         public void componentMoved( ComponentEvent e )
         {
         }
 
         public void componentShown( ComponentEvent e )
         {
         }
 
         public void componentHidden( ComponentEvent e )
         {
         }
     }
 
     /**
      * Returns the x offset for objects during rumbling.
      * 
      * @return  the x offset that objects should be drawn at during rumbling
      * @since April 7, 2008
      */
     public int getRumbleX()
     {
         return panel.rumbleX;
     }
 
     public int getRumbleY()
     {
         return panel.rumbleY;
     }
 
     public void rumble( double amount )
     {
         panel.rumble += amount;
     }
 }
