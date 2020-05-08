 package javax.game.sidescroller;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.media.utils.loaders.images.ImageLoader;
 import javax.media.utils.loaders.sound.SoundLoader;
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 /**
  * The main body of a side-scrolling game using Java3D
  * 
  * Notice that this should be overridden with the actual game implementation.
  * This base class only handles:
  * - Sending ticks to managers
  * - Rendering at appropriate intervals
  * - Double buffering
  * - Start/Stop/Pause/Resume
  */
 @SuppressWarnings ( "serial" )
 public abstract class GamePanel extends JPanel implements ActionListener {
 
     /**
      * Our parent frame
      */
     protected GameBase frame;
 
     private Dimension viewportSize;
     protected Rectangle worldArea;
     private Timer timer;
 
     /**
      * Number of ticks per second
      */
     private long tickrate;
 
     /**
      * Time of last tick.
      * Used to determine if we should skip
      * rendering due to system load.
      */
     private long lastTickTime;
     private volatile int skippedFrames = 0;
     private volatile int frames = 0;
 
     protected boolean paused = false;
     
     /**
      * If the time between one render finishes, and
      * the next is called drops below this value in
      * milliseconds, we skip one rendering cycle to
      * let the graphics system catch up.
      */
    protected int timerSlack = 100;
 
     /**
      * Never skip more than this many frames in a row.
      */
     protected int maxFrameSkips = 5;
 
     /**
      * Only execute a game state update tick every
      * this many frames.
      */
     protected int ticksPerUpdate = 5;
 
     protected RibbonsManager ribbons;
     public SpriteManager sprites;
     public ImageLoader images;
     protected SoundLoader sounds;
 
     /**
      * Stores the current rendering of the game state.
      * Used to do off-screen rendering to improve
      * performance.
      */
     private Graphics renderGraphic;
     private Image render = null;
 
     /**
      * The current in-game position.
      * This should indicates the position of the top-left
      * corner of the visible part of the current game's map.
      * All other positions in the java.media.j3d.utils.game.sidescroller
      * package are based on this coordinate system.
      */
     protected Point position;
 
     /**
      * Creates a new GamePanel using the given resources.
      * All overriding constructors *must* call this before doing anything else!
      * 
      * None of the managers need to be passed, but they are usually useful.
      * worldArea may be set to null for no size limit.
      * If the world is limited, any sprite that is about to exit the game area will
      * cause the {@link #spriteEscaped(Sprite)} method to be called.
      * 
      * @param viewportSize The size of the game panel
      * @param worldArea The world area, null for unlimited
      * @param ribbons The ribbon manager to use
      * @param sprites The sprite manager to use
      * @param images The image loader to use
      * @param sounds The sound loader to use
      * @param tickrate Only update game state every this many frames
      */
     public GamePanel ( Dimension viewportSize, Rectangle worldArea, RibbonsManager ribbons, SpriteManager sprites, ImageLoader images, SoundLoader sounds, long tickrate ) {
         this.viewportSize = viewportSize;
         this.worldArea = worldArea;
         this.ribbons = ribbons;
         this.sprites = sprites;
         this.images = images;
         this.sounds = sounds;
         this.tickrate = tickrate;
 
         this.setDoubleBuffered ( false );
         this.setBackground ( Color.black );
         this.setSize ( this.viewportSize );
         this.setPreferredSize ( this.viewportSize );
         this.setMinimumSize ( this.viewportSize );
         this.setMaximumSize ( this.viewportSize );
         this.setLayout ( null );
         this.setBorder ( BorderFactory.createEmptyBorder ( ) );
         this.viewportSize = this.getSize ( );
 
         this.setFocusable ( true );
         this.requestFocus ( );
 
         this.addKeyListener ( this.sprites );
 
         this.position = new Point ( 0, 0 );
         if ( this.ribbons != null )
             this.ribbons.updatePosition ( this.getVisibleMapRectangle ( ) );
 
         if ( this.sprites != null )
             this.sprites.setWorldArea ( this.worldArea );
 
         System.out.format ( "Inter-frame delay: %d ms with tickrate %d\n", (int) Math.round ( 1000.0 / this.tickrate ), this.tickrate );
         this.timer = new Timer ( (int) Math.round ( 1000.0 / this.tickrate ), this );
         /**
          * Initial delay should be straight after we've started the game so we can draw straightaway
          */
         this.timer.setInitialDelay ( 100 );
     }
 
     /**
      * Returns a rectangle representing the visible part of the current game world
      * 
      * @return a rectangle representing the visible part of the current game world
      */
     public Rectangle getVisibleMapRectangle ( ) {
         return new Rectangle ( this.position, this.getSize ( ) );
     }
 
     /**
      * Sets the parent frame of this panel
      * 
      * @param gameBase parent frame of this panel
      */
     public void setParent ( GameBase gameBase ) {
         this.frame = gameBase;
     }
 
     /**
      * Used to receive events from the running Timer
      */
     @Override
     public void actionPerformed ( ActionEvent e ) {
 
         if ( !this.paused && this.frames == 0 ) {
             this.position = this.tick ( );
 
             if ( this.worldArea != null ) {
                 this.position.x = Math.max ( this.worldArea.x, this.position.x );
                 this.position.y = Math.max ( this.worldArea.y, this.position.y );
                 this.position.x = Math.min ( this.position.x, this.worldArea.x + ( this.worldArea.width - this.getSize ( ).width ) );
                 this.position.y = Math.min ( this.position.y, this.worldArea.y + ( this.worldArea.height - this.getSize ( ).height ) );
             }
 
             if ( this.ribbons != null )
                 this.ribbons.updatePosition ( new Rectangle ( this.position, this.getSize ( ) ) );
             if ( this.sprites != null )
                 this.sprites.tick ( );
         }
 
        long timeDiff = System.currentTimeMillis ( ) - this.lastTickTime - this.timer.getDelay();
 
         // If animation is taking too long, we skip render/draw, and just update game state
        if ( this.skippedFrames > this.maxFrameSkips || timeDiff < timerSlack ) {
             this.render ( );
             this.draw ( );
             this.skippedFrames = 0;
         } else {
            System.out.println ( "Skipping frame" );
            System.out.println ( "diff is " + (timeDiff - this.timer.getDelay()) + ", which is more than " + this.timerSlack );
            System.out.println ( "Skipped frames is " + this.skippedFrames + " vs. max of " + this.maxFrameSkips );
             this.skippedFrames++;
         }
 
         this.frames = ( this.frames + 1 ) % this.ticksPerUpdate;
         this.lastTickTime = System.currentTimeMillis ( );
     }
 
     /**
      * Does an off-screen render of the current game state
      */
     private void render ( ) {
         if ( this.render == null ) {
             this.render = this.createImage ( this.viewportSize.width, this.viewportSize.height );
             if ( this.render == null )
                 throw new RuntimeException ( "Failed to render image" );
             this.renderGraphic = this.render.getGraphics ( );
         }
 
         // Empty background
         this.renderGraphic.setColor ( Color.white );
         this.renderGraphic.fillRect ( 0, 0, this.viewportSize.width, this.viewportSize.height );
 
         // Draw elements in order
         if ( this.ribbons != null )
             this.ribbons.display ( this.renderGraphic );
         // Brick manager goes here
         if ( this.sprites != null )
             this.sprites.display ( this.renderGraphic, this.getVisibleMapRectangle ( ) );
 
         this.render ( this.renderGraphic );
     }
 
     /**
      * Called by the off-screen renderer whenever it wants to render.
      * 
      * Should be override to render any elements not handled
      * by the base GamePanel.
      * Default implementation is empty.
      * 
      * @param g The graphics object to draw with
      */
     protected void render ( Graphics g ) {
     }
 
     /**
      * Use active rendering to put the buffered image on-screen
      */
     private void draw ( ) {
         Graphics g;
         try {
             g = this.getGraphics ( );
             if ( ( g != null ) && ( this.render != null ) )
                 g.drawImage ( this.render, 0, 0, null );
             // Sync the display on some systems.
             // (on Linux, this fixes event queue problems)
             Toolkit.getDefaultToolkit ( ).sync ( );
             g.dispose ( );
         } catch ( Exception e ) {
             System.out.println ( "Graphics context error: " + e );
         }
     }
 
     /**
      * Called when one game tick has passed
      * Should return the new position of the map
      * 
      * @return the new position of the map
      */
     public abstract Point tick ( );
 
     /**
      * Called when the game is resumed.
      * Default implementation is empty.
      */
     protected void onResume ( ) {
     }
 
     /**
      * Called to resume the game
      */
     public void resume ( ) {
         this.paused = false;
         this.onResume ( );
     }
 
     /**
      * Called when the game is paused.
      * Default implementation is empty.
      */
     protected void onPause ( ) {
     }
     
     /**
      * Returns true if the game is paused
      * @return true if the game is paused
      */
     public boolean isPaused ( ) {
         return this.paused;
     }
 
     /**
      * Called to pause the game
      */
     public void pause ( ) {
         this.paused = true;
         this.onPause ( );
     }
 
     /**
      * Called when the game is quit.
      * Default implementation is to call System.exit(0);
      */
     protected void onEnd ( ) {
         System.exit ( 0 );
     }
 
     /**
      * Called to quit the game
      */
     public void end ( ) {
         this.timer.stop ( );
         this.onEnd ( );
     }
 
     /**
      * Called when the game is started.
      * Default implementation is empty.
      */
     protected void onStart ( ) {
     }
 
     /**
      * Called to start the game
      */
     public void start ( ) {
         this.timer.start ( );
         this.onStart ( );
     }
 
     /**
      * Returns the rectangle representing the entire game world
      * 
      * @return
      */
     public Rectangle getWorldArea ( ) {
         return this.worldArea;
     }
 }
