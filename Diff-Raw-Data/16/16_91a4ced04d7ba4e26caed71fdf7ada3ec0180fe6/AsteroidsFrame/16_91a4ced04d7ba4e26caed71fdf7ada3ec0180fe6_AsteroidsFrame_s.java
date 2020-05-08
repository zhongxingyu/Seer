 /*
  * DISASTEROIDS
  * AsteroidsFrame.java
  */
 package disasteroids;
 
 import disasteroids.networking.Client;
 import disasteroids.networking.Server;
 import disasteroids.sound.Sound;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.VolatileImage;
 import java.text.DecimalFormat;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import javax.swing.JOptionPane;
 
 /**
  * The big momma class up in the sky.
  * 
  * @author Andy Kooiman, Phillip Cohen
  * @since Classic
  */
 public class AsteroidsFrame extends Frame implements KeyListener
 {
     /**
      * Dimensions of the window when not in fullscreen mode.
      * @since November 15 2007
      */
     private static final int WINDOW_WIDTH = 500,  WINDOW_HEIGHT = 750;
 
     /**
      * The <code>Image</code> used for double buffering.
      * @since Classic
      */
     private Image virtualMem;
 
     /**
      * The star background.
      * @since Classic
      */
     private Background background;
 
     /**
      * The time stored at the beginning of the last call to paint; Used for FPS.
      * @since December 15, 2007
      */
     private long timeOfLastRepaint;
 
     /**
      * Whether the user is pressing the scoreboard key.
      * @since December 15 2007
      */
     private boolean drawScoreboard;
 
     /**
      * Notification messages always shown in the top-left corner.
      * @since December 19, 2004
      */
     private ConcurrentLinkedQueue<NotificationMessage> notificationMessages = new ConcurrentLinkedQueue<NotificationMessage>();
 
     /**
      * The current amount of FPS. Updated in <code>paint</code>.
      * @since December 18, 2007
      */
     private int lastFPS;
 
     /**
      * Stores whether a high score has been achieved by this player.
      * @since December 20, 2007
      */
     private boolean highScoreAchieved;
 
     /**
      * ID of the player that's at this computer.
      * IDs are used instead of indexes for better reliability.
      * @see <code>players</code>
      * @since December 14 2007
      */
     private int localId;
 
     private static AsteroidsFrame frame;
 
     public static AsteroidsFrame frame()
     {
         return frame;
     }
     /**
      * @since December 29, 2007
      */
     private boolean showWarpDialog;
 
     /**
      * Whether the endgame should be shown on next paint cycle.
      * @since December 30, 2007
      */
     private boolean shouldEnd;
 
     /**
      * The number of times that the paint method has been called, for FPS
      * @since January 10, 2008
      */
     private int paintCount;
 
     /**
      * Constructs the game frame and game elements.
      * 
      * @param localId   id of the player at this computer
      * @since December 14, 2007
      */
     public AsteroidsFrame( int localId )
     {
         paintCount = 0;
         frame = this;
         this.localId = localId;
 
         // Reflect the network state.
         if ( Server.is() )
         {
             setTitle( "Disateroids (server)" );
         }
         else if ( Client.is() )
         {
             setTitle( "Disasteroids (client)" );
         }
         else
         {
             setTitle( "Disasteroids" );
         }
 
         setResizable( false );
 
         // Close when the exit key is pressed.
         addWindowListener( new CloseAdapter() );
 
         // Set our size - fullscreen or windowed.
         updateFullscreen();
 
         // Receive key events.
         this.addKeyListener( this );
     }
 
     /**
      * Resets the background, high score, and notification messages.
      * 
      * @since December 25, 2007
      */
     void resetGame()
     {
         highScoreAchieved = false;
         background.clearMessages();
         notificationMessages.clear();
         shouldEnd = false;
         // Reset the background.
         background.init();
     }
 
     /**
      * Draws game elements
      * @param g     buffered <code>Graphics</code> context to draw on
      * @since December 21, 2007
      */
     private void draw( Graphics g )
     {
         if ( localPlayer() == null )
         {
             return;
         }
 
         // Anti-alias, if the user wants it.
         updateQualityRendering( g, Settings.qualityRendering );
 
         // Calculate FPS.
         if ( ++paintCount % 10 == 0 )
         {
             long timeSinceLast = -timeOfLastRepaint + ( timeOfLastRepaint = System.currentTimeMillis() );
             if ( timeSinceLast > 0 )
             {
                 lastFPS = (int) ( 10000.0 / timeSinceLast );
             }
         }
 
         if ( !highScoreAchieved && localPlayer().getScore() > Settings.highScore )
         {
             Running.log( "New high score of " + insertThousandCommas( localPlayer().getScore() ) + "!", 800 );
             highScoreAchieved = true;
         }
 
         // Draw the star background.
         if ( background == null )
         {
             background = new Background( Game.getInstance().GAME_WIDTH, Game.getInstance().GAME_HEIGHT );
         }
         else
         {
             g.drawImage( background.render(), 0, 0, this );
         }
 
         // Draw stuff in order of importance, from least to most.        
         ParticleManager.draw( g );
 
         Game.getInstance().asteroidManager.draw( g );
 
         for ( GameObject go : Game.getInstance().gameObjects )
         {
             go.draw( g );
         }
 
         // Update the ships.
         for ( Ship s : Game.getInstance().players )
         {
             s.draw( g );
         }
 
         if ( shouldEnd )
         {
             endGame( g );
         }
         // Draw the on-screen HUD.
         drawHud( g );
 
         // Draw the entire scoreboard.
         if ( drawScoreboard )
         {
             drawScoreboard( g );
         }
     }
 
     /**
      * Sets up double buffering.
      * Uses hardware acceleration, if desired.
      * 
      * @since December 25, 2007
      */
     private void initBuffering()
     {
         // Create the buffer.
         if ( Settings.hardwareRendering )
         {
             virtualMem = getGraphicsConfiguration().createCompatibleVolatileImage( getWidth(), getHeight() );
         }
         else
         {
             virtualMem = createImage( getWidth(), getHeight() );
         }
 
         Game.getInstance().startGame();
     }
 
     /**
      * Steps the game and paints all components onto the screen.
      * Uses hardware acceleration, if desired.
      * 
      * @param g the <code>Graphics</code> context of the screen
      * @since Classic
      */
     @Override
     public void paint( Graphics g )
     {
         // Create the image if needed.
         if ( virtualMem == null )
         {
             initBuffering();
         }
 
         if ( showWarpDialog )
         {
             try
             {
                 Game.getInstance().setPaused( false );
                 Game.getInstance().warp( Integer.parseInt( JOptionPane.showInputDialog( null, "Enter the level number to warp to.", Game.getInstance().level ) ) );
             }
             catch ( NumberFormatException e )
             {
                 // Do nothing with incorrect input or cancel.
                 Running.log( "Invalid warp command.", 800 );
             }
             showWarpDialog = false;
         }
         // Render in hardware mode.
         if ( Settings.hardwareRendering )
         {
             do
             {
                 // If the resolution has changed causing an incompatibility, re-create the VolatileImage.
                 if ( ( (VolatileImage) virtualMem ).validate( getGraphicsConfiguration() ) == VolatileImage.IMAGE_INCOMPATIBLE )
                 {
                     initBuffering();
                 }
 
                 // Draw the game's graphics.
                 draw( virtualMem.getGraphics() );
 
             }
             while ( ( (VolatileImage) virtualMem ).contentsLost() );
         } // Render in software mode.
         else
         {
             // Draw the game's graphics.
             draw( virtualMem.getGraphics() );
         }
 
         // Flip the buffer to the screen.
         g.drawImage( virtualMem, 0, 0, this );
 
         repaint();
 
     }
 
     /**
      * Shows a dialog to warps to a particular level.
      * 
      * @since November 15 2007
      */
     private void warpDialog()
     {
         showWarpDialog = true;
     }
 
     void nextLevel()
     {
         background.init();
     }
 
     /**
      * Advances to the next level from a static context.
      * 
      * @deprecated Use <code>AsteroidsFrame.frame()</code> to access frame methods.
      * @since Classic
      */
     @Deprecated
     public static void staticNextLevel()
     {
         Game.getInstance().nextLevel();
     }
 
     /**
      * Draws the on-screen information for the local player.
      * 
      * @param g <code>Graphics</code> to draw on
      * @since December 15, 2007
      */
     private void drawHud( Graphics g )
     {
         Graphics2D g2d = (Graphics2D) g;
         String text = "";
         int x = 0, y = 0;
 
         // Draw the "lives" string.
         g2d.setColor( localPlayer().getColor() );
         g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
         if ( localPlayer().livesLeft() == 1 )
         {
             text = "life";
         }
         else
         {
             text = "lives";
         }
         x = getWidth() - 40;
         y = getHeight() - 15;
         g2d.drawString( text, x, y );
         x -= 10;
 
         // Draw the lives counter.
         g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
         text = "" + localPlayer().livesLeft();
         x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth();
         g2d.drawString( text, x, y );
         x -= 15;
 
         // Draw the level counter.
         g2d.setColor( Color.lightGray );
         g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
         text = "" + Game.getInstance().level;
         x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth();
         g2d.drawString( text, x, y );
 
         // Draw the "level" string.
         g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
         text = "level";
         x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;
         g2d.drawString( text, x, y );
 
         // Draw the score counter.
         g2d.setColor( Color.gray );
         g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
         text = insertThousandCommas( localPlayer().getScore() );
         y = ( isFullscreen() ? 0 : 30 ) + 20; // Offset for the titlebar (yuck)!
         x = getWidth() - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() - 12;
         g2d.drawString( text, x, y );
 
         // Draw the "score" string.
         g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
         text = "score";
         x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;
         g2d.drawString( text, x, y );
 
         // Draw the fps counter.
         g2d.setColor( Color.darkGray );
         g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
         text = "" + lastFPS;
         x = 8;
         y = (int) ( getHeight() - g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() ) - 2;
         g2d.drawString( text, x, y );
 
         // Draw the "fps" string.
         x += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;
         g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
         text = "fps";
         g2d.drawString( text, x, y );
 
         // Draw notification messages.
         x = 8;
         y = ( isFullscreen() ? 0 : 30 ) + 20; // Offset for the titlebar (yuck)!
         g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
         g2d.setColor( Color.lightGray );
         Iterator<NotificationMessage> itr = notificationMessages.iterator();
 
         while ( itr.hasNext() )
         {
             NotificationMessage m = itr.next();
             text = m.message;
             g2d.drawString( text, x, y );
             y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 2;
             if ( m.life-- <= 0 )
             {
                 itr.remove();
             }
         }
 
         // Draw "waiting for server...".
         if ( Client.is() && Client.getInstance().serverTimeout() )
         {
             g2d.setFont( new Font( "Tahoma", Font.BOLD, 18 ) );
             g2d.setColor( Color.GREEN );
 
             text = "Waiting for server...";
             x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
             y = getHeight() / 2 - 50;
 
             g2d.drawString( text, x, y );
         }
     }
 
     /**
      * Draws the full scoreboard of all players.
      * 
      * @param g <code>Graphics</code> context to draw on
      * @since December 15, 2007
      */
     private void drawScoreboard( Graphics g )
     {
         Graphics2D g2d = (Graphics2D) g;
         String text = "";
         int x = 0, y = 0;
 
         // Draw the "scoreboard" string.
         g2d.setColor( Color.white );
         g2d.setFont( new Font( "Tahoma", Font.BOLD, 24 ) );
         text = "Scoreboard";
         x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
         y = getHeight() / 4;
         g2d.drawString( text, x, y );
         y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 10;
 
         // Draw the asteroid count.
         g2d.setColor( Color.lightGray );
         g2d.setFont( new Font( "Tahoma", Font.PLAIN, 16 ) );
         text = insertThousandCommas( Game.getInstance().asteroidManager.size() ) + " asteroid" + ( Game.getInstance().asteroidManager.size() == 1 ? " remains" : "s remain" );
         x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
         g2d.drawString( text, x, y );
         y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 10;
 
         // Create the columns.
         ScoreboardColumn[] columns = new ScoreboardColumn[4];
         columns[0] = new ScoreboardColumn( getWidth() * 2 / 7, "Name" );
         columns[1] = new ScoreboardColumn( getWidth() * 1 / 2, "Score" );
         columns[2] = new ScoreboardColumn( getWidth() * 3 / 5, "Lives" );
         columns[3] = new ScoreboardColumn( getWidth() * 2 / 3, "# Destroyed" );
 
         // Draw the column headers.
         y += 15;
         g2d.setColor( Color.darkGray );
         g2d.setFont( new Font( "Tahoma", Font.PLAIN, 14 ) );
         for ( ScoreboardColumn c : columns )
         {
             g2d.drawString( c.title, c.x, y );
         }
 
         g2d.drawLine( columns[0].x, y + 5, columns[columns.length - 1].x + (int) g2d.getFont().getStringBounds( columns[columns.length - 1].title, g2d.getFontRenderContext() ).getWidth(), y + 5 );
         y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 10;
 
         // Draw the entries.
         for ( Ship s : Game.getInstance().players )
         {
             g2d.setColor( s.getColor() );
             if ( s == localPlayer() )
             {
                 g2d.setFont( g2d.getFont().deriveFont( Font.BOLD ) );
             }
             else
             {
                 g2d.setFont( g2d.getFont().deriveFont( Font.PLAIN ) );
             }
             for ( int i = 0; i < columns.length; i++ )
             {
                 switch ( i )
                 {
                     case 0:
                         text = s.getName();
                         break;
                     case 1:
                         text = insertThousandCommas( s.score() );
                         break;
                     case 2:
                         text = insertThousandCommas( s.livesLeft() );
                         break;
                     case 3:
                         text = insertThousandCommas( s.getNumAsteroidsKilled() );
                         break;
                     default:
                         text = "";
                 }
                 g2d.drawString( text, columns[i].x, y );
             }
             y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 3;
         }
 
         // Draw the high scorer.
         g2d.setFont( new Font( "Tahoma", Font.PLAIN, 12 ) );
         text = "All-time high scorer " + ( highScoreAchieved ? "was " : "is " ) + Settings.highScoreName + " with " + insertThousandCommas( (int) Settings.highScore ) + " points.";
         x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
         y += 40;
         g2d.setColor( Color.white );
         g2d.drawString( text, x, y );
 
         // Boost player egos.
         if ( highScoreAchieved )
         {
             y += (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 3;
             if ( localPlayer().getName().equals( Settings.highScoreName ) )
             {
                 text = "But hey, everyone likes to beat their own score.";
             }
             else
             {
                 text = "But you're much better with your shiny " + insertThousandCommas( localPlayer().getScore() ) + "!";
             }
             x = getWidth() / 2 - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
             g2d.drawString( text, x, y );
         }
     }
 
     /**
      * Starts a new Game.getInstance().
      * 
      * @since Classic
      */
     public static void newGame()
     {
         Game.getInstance().resetEntireGame();
     }
 
     /**
      * Indicates to <code>this</code> that the endgame should be shown on 
      * the next painting cycle.
      * 
      * @since December 30, 2007
      */
     public void endGame()
     {
         shouldEnd = true;
     }
 
     /**
      * Displays the end game messages.
      * @param g the <code>Graphics</code> context of the screen
      * 
      * @since Classic
      */
     private void endGame( Graphics g )
     {
         shouldEnd = false;
         g.setFont( new Font( "Tahoma", Font.BOLD, 32 ) );
         if ( Settings.soundOn )
         {
             Sound.playInternal( Sound.GAME_OVER_SOUND );
         }
         for ( float sat = 0; sat <= 1; sat += .005 )
         {
             g.setColor( Color.getHSBColor( sat, sat, sat ) );
             g.drawString( "Game Over", 250, 400 );
         }
         this.setIgnoreRepaint( true );
 
         // Find the player with the highest score.
         Ship highestScorer = Game.getInstance().players.getFirst();
         for ( Ship s : Game.getInstance().players )
         {
             if ( s.getScore() > highestScorer.getScore() )
             {
                 highestScorer = s;
             }
         }
 
         String victoryMessage = ( Game.getInstance().players.size() > 1 ) ? highestScorer.getName() + " wins" : "You died";
         victoryMessage += " with a score of " + insertThousandCommas( highestScorer.getScore() ) + "!";
 
         // Is this a new high score?
         if ( highestScorer.getScore() > Settings.highScore )
         {
             victoryMessage += "\nThis is a new high score!";
             Settings.highScoreName = highestScorer.getName();
             Settings.highScore = highestScorer.getScore();
         }
 
         // Show the message box declaring victory!
         JOptionPane.showMessageDialog( this, victoryMessage );
 
         // Easter egg!
         //        if ( highScore > 1000000 )
         //        {
         //            try
         //            {
         //                JOptionPane.showMessageDialog( null, "HOLY CRAP!!!! YOUR SCORE IS HIGH!!!\nI NEED HELP TO COMPUTE IT" );
         //                Runtime.getRuntime().exec( "C:/Windows/system32/calc" );
         //            }
         //            catch ( Exception e )
         //            {
         //            }
         //        }
 
         newGame();
         this.setIgnoreRepaint( false );
         repaint();
         shouldEnd = false;
     }
 
     /**
      * Called automatically by key listener when keys are released.
      * The keyCodes are made negative to show this.
      * 
      * @param e the <code>KeyEvent</code> generated by the key listener
      * @since Classic
      */
     public synchronized void keyReleased( KeyEvent e )
     {
         Game.getInstance().actionManager.add( new Action( localPlayer(), 0 - e.getKeyCode(), Game.getInstance().timeStep + 2 ) );
         if ( Client.is() )
         {
             Client.getInstance().keyStroke( 0 - e.getKeyCode() );
         }
     }
 
     /**
      * Dummy method to satisfy <code>KeyListener</code> interface.
      * 
      * @param e the <code>KeyEvent</code> generated by the key listener
      * @since Classic
      */
     public void keyTyped( KeyEvent e )
     {
     }
 
     /**
      * Called automatically by key listener for all keys pressed, and creates an <code>Action</code> to store the relevent data.
      * 
      * @param e the <code>KeyEvent</code> generated by the key listener
      * @since Classic
      */
     public synchronized void keyPressed( KeyEvent e )
     {
         if ( Game.getInstance().isPaused() )
         {
             Game.getInstance().setPaused( false );
             return;
         }
 
         // Is it a local action?
         switch ( e.getKeyCode() )
         {
             case KeyEvent.VK_ESCAPE:
                 Running.quit();
                 break;
             case KeyEvent.VK_M:
                 toggleMusic();
                 break;
             case KeyEvent.VK_S:
                 toggleSound();
                 break;
             case KeyEvent.VK_F4:
                 toggleFullscreen();
                 break;
             case KeyEvent.VK_SCROLL_LOCK:
                 for ( GameObject go : Game.getInstance().gameObjects )
                 {
                     if ( go instanceof Station )
                     {
                         ( (Station) go ).setEasterEgg();
                     }
                 }
                 break;
             case KeyEvent.VK_W:
                 if ( !Client.is() )
                 {
                     AsteroidsFrame.frame().warpDialog();
                 }
                 break;
             case KeyEvent.VK_A:
                 toggleReneringQuality();
                 break;
             case KeyEvent.VK_BACK_SLASH:
                 drawScoreboard = !drawScoreboard;
                 break;
             default:
 
 
                 Game.getInstance().actionManager.add( new Action( localPlayer(), e.getKeyCode(), Game.getInstance().timeStep + 2 ) );
 
                 if ( Client.is() )
                 {
                     Client.getInstance().keyStroke( e.getKeyCode() );
                 }
 
         }
         repaint();
     }
 
     /**
      * Toggles music on/off, and writes the setting to the background.
      * 
      * @since November 15, 2007
      */
     public void toggleMusic()
     {
         Running.log( "Music " + ( Sound.toggleMusic() ? "on." : "off." ) );
     }
 
     /**
      * Toggles sound on/off, and writes the setting to the background.
      * 
      * @since November 15, 2007
      */
     void toggleSound()
     {
         Running.log( "Sound " + ( Sound.toggleSound() ? "on." : "off." ) );
     }
 
     /**
      * Toggles fullscreen on/off. This is rather problematic.
      * 
      * @since December 11, 2007
      */
     private void toggleFullscreen()
     {
         Settings.useFullscreen = !Settings.useFullscreen;
         updateFullscreen();
     }
 
     /**
      * Toggles high-quality rendering on/off, and writes the setting to the background.
      * 
      * @since December 15, 2007
      */
     private void toggleReneringQuality()
     {
         Settings.qualityRendering = !Settings.qualityRendering;
         Running.log( ( Settings.qualityRendering ? "Quality rendering." : "Speed rendering." ) );
     }
 
     /**
      * Enables or disables fancy rendering of the provided <code>Graphics</code>.
      * 
      * @param qualityRendering   whether to use higher-quality rendering
      * @since December 15, 2007
      */
     private void updateQualityRendering( Graphics g, boolean qualityRendering )
     {
         Graphics2D g2d = (Graphics2D) g;
 
         // Adjust the setting.
         if ( qualityRendering )
         {
             g2d.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY );
             g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
             g2d.setRenderingHint( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY );
             g2d.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE );
             g2d.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON );
             g2d.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
             g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
             g2d.setRenderingHint( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE );
             g2d.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
         }
         else
         {
             g2d.setRenderingHint( RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED );
             g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
             g2d.setRenderingHint( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED );
             g2d.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE );
             g2d.setRenderingHint( RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF );
             g2d.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR );
             g2d.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
             g2d.setRenderingHint( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT );
             g2d.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF );
         }
     }
 
     /**
      * Sets the fullscreen/window mode based on the setting.
      *
      * @since December 11, 2007
      */
     private void updateFullscreen()
     {
         GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
 
         // Set fullscreen mode.
         if ( Settings.useFullscreen )
         {
             // Don't change anything if we're already in fullscreen mode.
             if ( graphicsDevice.getFullScreenWindow() != this )
             {
                 dispose();
                 setUndecorated( true );
                 setSize( graphicsDevice.getDisplayMode().getWidth(), graphicsDevice.getDisplayMode().getHeight() );
                 graphicsDevice.setFullScreenWindow( this );
 
                 // Hide the cursor.
                 Image cursorImage = Toolkit.getDefaultToolkit().getImage( "xparent.gif" );
                 Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor( cursorImage, new Point( 0, 0 ), "" );
                 setCursor( blankCursor );
 
                 // Re-create the background.
                 if ( background != null )
                 {
                     background.init();
                 }
             }
         } // Set windowed mode.
         else
         {
             // Don't change anything if we're already in windowed mode.
             if ( ( getSize().width != WINDOW_WIDTH ) || ( getSize().height != WINDOW_HEIGHT ) )
             {
                 setVisible( false );
                 dispose();
                 setUndecorated( false );
                 setSize( WINDOW_WIDTH, WINDOW_HEIGHT );
 
                 if ( Client.is() )
                 {
                     setLocation( 0 + 3 + WINDOW_WIDTH, 0 );
                 }
                 else
                 {
                     setLocation( 0, 0 );
                 }
 
                 graphicsDevice.setFullScreenWindow( null );
 
                 // Show the cursor.
                 setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
 
                 // Re-create the background.
                 if ( background != null )
                 {
                     background.init();
                 }
             }
         }
         setVisible( true );
     }
 
     /**
      * Returns if we're in fullscreen (regardless of the setting).
      * 
      * @return  whether the frame is in fullscreen mode
      * @since December 15, 2007
      */
     public boolean isFullscreen()
     {
         return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getFullScreenWindow() == this;
     }
 
     /**
      * Included to prevent the clearing of the screen between repaints.
      * 
      * @param g the <code>Graphics</code> context of the screen
      * @since Classic
      */
     @Override
     public void update( Graphics g )
     {
         paint( g );
     }
 
     public static void addNotificationMessage( String message, int life )
     {
         if ( message.equals( "" ) )
         {
             return;
         }
 
         if ( frame() != null )
         {
             frame().notificationMessages.add( new NotificationMessage( message, life ) );
         }
     }
 
     /**
      * Writes a message onto the background.
      * 
      * @param message   the message to be written
      * @param x         the x coordinate where the message should be drawn
      * @param y         the y coordinate where the message should be drawn
      * @param col       the <code>Color</code> in which the message should be drawn
      * @since Classic
      */
     public void writeOnBackground( String message, int x, int y, Color col )
     {
         background.writeOnBackground( message, x, y, col );
     }
 
     /**
      * Draws a point at specified coordinates, translated relative to local ship.
      * @param graph The <code>Graphics</code> context in which to draw
      * @param col The <code>Color</code> in which to draw
      * @param x The x coordinate
      * @param y The y coordinate
      * @author Andy Kooiman
      * @since December 16, 2007
      */
     public void drawPoint( Graphics graph, Color col, int x, int y )
     {
         x = ( x - localPlayer().getX() + getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH;
         y = ( y - localPlayer().getY() + getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT;
         graph.setColor( col );
         if ( x > -2 && x < Game.getInstance().GAME_WIDTH + 2 && y > -2 && y < Game.getInstance().GAME_HEIGHT + 2 )
         {
             graph.drawRect( x, y, 0, 0 );
         }
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
         x = ( x - localPlayer().getX() + getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH;
         y = ( y - localPlayer().getY() + getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT;
         graph.setColor( col );
         if ( x > -radius * 2 && x < Game.getInstance().GAME_WIDTH + radius * 2 && y > -radius * 2 && y < Game.getInstance().GAME_HEIGHT + radius * 2 )
         {
             graph.drawOval( x - radius, y - radius, radius * 2, radius * 2 );
         }
     }
 
     /**
      * Draws a line from one coordinate to another in a given color.
      * 
      * @param col   the <code>Color</code> in wihch the circle will be drawn
      * @param x1    the first x coordinate
      * @param y1    the first y coordinate
      * @param x2    the second x coordinate
      * @param y2    the second y coordinate
      * @since December 15, 2007
      */
     public void drawLine( Graphics graph, Color col, int x1, int y1, int x2, int y2 )
     {
         x1 = ( x1 - localPlayer().getX() + getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH;
         y1 = ( y1 - localPlayer().getY() + getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT;
         x2 = ( x2 - localPlayer().getX() + getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH;
         y2 = ( y2 - localPlayer().getY() + getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT;
         graph.setColor( col );
         graph.drawLine( x1, y1, x2, y2 );
     }
 
     public void drawLine( Graphics graph, Color col, int x, int y, int length, double angle )
     {
         x = ( x - localPlayer().getX() + getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH;
         y = ( y - localPlayer().getY() + getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT;
         graph.setColor( col );
         if ( x > -length && x < Game.getInstance().GAME_WIDTH + length && y > -length && y < Game.getInstance().GAME_HEIGHT + length )
         {
             graph.drawLine( x, y, (int) ( x + length * Math.cos( angle ) ), (int) ( y - length * Math.sin( angle ) ) );
         }
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
         x = ( x - localPlayer().getX() + getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH;
         y = ( y - localPlayer().getY() + getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT;
         graph.setColor( col );
         if ( x > -2 * radius && x < Game.getInstance().GAME_WIDTH + radius * 2 && y > -radius * 2 && y < Game.getInstance().GAME_HEIGHT + radius * 2 )
         {
             graph.fillOval( x - radius, y - radius, radius * 2, radius * 2 );
         }
     }
 
     public void drawString( Graphics graph, int x, int y, String str, Color col )
     {
        graph.setFont( new Font( "Tahoma", graph.getFont().getStyle(), graph.getFont().getSize() ) );
         x = ( x - localPlayer().getX() + getWidth() / 2 + 4 * Game.getInstance().GAME_WIDTH ) % Game.getInstance().GAME_WIDTH;
         y = ( y - localPlayer().getY() + getHeight() / 2 + 4 * Game.getInstance().GAME_HEIGHT ) % Game.getInstance().GAME_HEIGHT;
         graph.setColor( col );
         if ( x > -50 && x < Game.getInstance().GAME_WIDTH && y > -50 && y < Game.getInstance().GAME_HEIGHT )
         {
             graph.drawString( str, x, y );
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
      * Adds a new message to the on-screen list.
      * These messages should be relevant to the local player.
      * 
      * @param message   the message text
      */
     public static void addNotificationMessage( String message )
     {
         if ( message.equals( "" ) )
         {
             return;
         }
 
         addNotificationMessage( message, 250 );
     }
 
     /**
      * Returns whether this computer is the first player.
      * This occurs in singleplayer or when hosting.
      * 
      * @return  whether the local computer is player #1
      * @since Classic
      */
     public boolean isPlayerOne()
     {
         return localId == Game.getInstance().players.getFirst().id;
     }
 
     /**
      * Inserts comma seperators at each grouping, up to 10^34.
      * 
      * @param number    The number to be formatted.
      * @return  the formatted string.
      * @since December 15, 2007
      */
     public static String insertThousandCommas( int number )
     {
         return new DecimalFormat( "#,###,###,###,###,###,###,###,###,###,###,###" ).format( number );
     }
 
     /**
      * Returns the player at this computer.
      * 
      * @return  the <code>Ship</code> controlled by the player at this computer
      * @since December 19, 2007
      */
     public Ship localPlayer()
     {
         if ( Game.getInstance().players == null || Game.getInstance().players.size() < 1 )
         {
             return null;
         }
 
         for ( Ship s : Game.getInstance().players )
         {
             if ( s.id == localId )
             {
                 return s;
             }
         }
 
         return null;
     }
 
     /**
      * A simple handler for the frame's window buttons.
      * 
      * @since December 15, 2007
      */
     private static class CloseAdapter extends WindowAdapter
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
     }
 
     /**
      * A small class for the storage of scoreboard colums.
      * 
      * @see <code>drawScoreboard</code>
      * @author Phillip Cohen
      * @since December 15, 2007
      */
     private static class ScoreboardColumn
     {
         /**
          * x coordinate of the column's left edge.
          */
         public int x;
 
         /**
          * Header text.
          */
         public String title;
 
         /**
          * Assigns this column's data.
          * 
          * @param x     the x coordinate of the left edge
          * @param title the text of the column's header
          */
         public ScoreboardColumn( int x, String title )
         {
             this.x = x;
             this.title = title;
         }
     }
 
     /**
      */
     private static class NotificationMessage
     {
         public String message;
 
         public int life;
 
         public NotificationMessage( String message, int life )
         {
             this.message = message;
             this.life = life;
         }
     }
 }
