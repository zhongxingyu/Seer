 package net.intensicode.me;
 
 import net.intensicode.core.*;
 import net.intensicode.util.*;
 
 import javax.microedition.lcdui.Graphics;
 import javax.microedition.lcdui.game.GameCanvas;
 
 
 public final class MicroGameView extends GameCanvas implements DirectScreen
     {
     public MicroCanvasGraphics graphics;
 
     //#if TOUCH_SUPPORTED
     public MicroTouchHandler touch;
     //#endif
 
     public MicroGameSystem system;
 
     public MicroKeysHandler keys;
 
     public SystemContext context;
 
 
     public MicroGameView()
         {
         super( false );
         setFullScreenMode( true );
         }
 
     // From DirectScreen
 
     public final int width()
         {
         if ( myTargetSize.width == 0 ) return getWidth();
         return myTargetSize.width;
         }
 
     public final int height()
         {
         if ( myTargetSize.width == 0 ) return getHeight();
         return myTargetSize.height;
         }
 
     public final int getTargetWidth()
         {
         return myTargetSize.width;
         }
 
     public final int getTargetHeight()
         {
         return myTargetSize.height;
         }
 
     public void setTargetSize( final int aWidth, final int aHeight )
         {
         myTargetSize.setTo( aWidth, aHeight );
         //#if DEBUG
         Log.debug( "Target screen size: {}x{}", width(), height() );
         Log.debug( "Device screen size: {}x{}", getWidth(), getHeight() );
         //#endif
         }
 
     public Position toTarget( final int aNativeX, final int aNativeY )
         {
         throw new RuntimeException( "nyi" );
         }
 
     public final void beginFrame()
         {
         updateGraphicsSize();
         graphics.gc = myBufferGC = createNewGraphics();
         }
 
     public final void endFrame()
         {
         if ( isShown() ) flushGraphics();
         graphics.gc = myBufferGC = null;
         }
 
     public final void initialize()
         {
         }
 
     public final void cleanup()
         {
         }
 
     // From Canvas
 
     protected final void hideNotify()
         {
         //#if DEBUG
         Log.debug( "MicroGameView#hideNotify" );
         //#endif
         system.stop();
         super.hideNotify();
         }
 
     protected final void showNotify()
         {
         //#if DEBUG
         Log.debug( "MicroGameView#showNotify" );
         //#endif
         super.showNotify();
         system.start();
         }
 
     protected void sizeChanged( final int aWidth, final int aHeight )
         {
         //#if DEBUG
         Log.debug( "MicroGameView#sizeChanged {} {}", aWidth, aHeight );
         //#endif
         super.sizeChanged( aWidth, aHeight );
         updateGraphicsSize();
         beginFrame();
         endFrame();
         }
 
     protected final void keyPressed( final int aCode )
         {
         final int gameAction = getGameAction( aCode );
         keys.keyPressed( aCode, gameAction );
         }
 
     //#if NO_KEY_REPEAT
 
     protected final void keyRepeated( final int i )
         {
         }
 
     //#endif
 
     protected final void keyReleased( final int aCode )
         {
         final int gameAction = getGameAction( aCode );
         keys.keyReleased( aCode, gameAction );
         }
 
     //#if TOUCH_SUPPORTED
 
     protected final void pointerPressed( final int aX, final int aY )
         {
         touch.pointerPressed( aX, aY );
         }
 
     protected final void pointerReleased( final int aX, final int aY )
         {
         touch.pointerReleased( aX, aY );
         }
 
     protected final void pointerDragged( final int aX, final int aY )
         {
         touch.pointerDragged( aX, aY );
         }
 
     //#endif
 
     // Implementation
 
     private void updateGraphicsSize()
         {
         graphics.width = width();
         graphics.height = height();
         }
 
     private Graphics createNewGraphics()
         {
         //#if DEBUG
         Assert.isNull( "old buffer graphics should be disposed", myBufferGC );
         //#endif
 
         final int realWidth = getWidth();
         final int realHeight = getHeight();
         final int width = width();
         final int height = height();
 
         myBufferGC = getGraphics();
         clearGC( myBufferGC, realWidth, realHeight );
 
         final int xOffset = ( realWidth - width ) / 2; // align center
         final int yOffset = ( realHeight - height ) / 2; // align bottom
         resetGC( myBufferGC, xOffset, yOffset, width, height );
 
         return myBufferGC;
         }
 
     private static void resetGC( final Graphics aGC, final int aOffsetX, final int aOffsetY, final int aWidth, final int aHeight )
         {
         aGC.translate( -aGC.getTranslateX(), -aGC.getTranslateY() );
         aGC.translate( aOffsetX, aOffsetY );
         aGC.setClip( 0, 0, aWidth, aHeight );
         }
 
     private static void clearGC( final Graphics aGC, final int aWidth, final int aHeight )
         {
         aGC.translate( -aGC.getTranslateX(), -aGC.getTranslateY() );
         aGC.setColor( 0 );
         aGC.fillRect( 0, 0, aWidth, aHeight );
         }
 
 
     private Graphics myBufferGC;
 
     private final Size myTargetSize = new Size();
     }
