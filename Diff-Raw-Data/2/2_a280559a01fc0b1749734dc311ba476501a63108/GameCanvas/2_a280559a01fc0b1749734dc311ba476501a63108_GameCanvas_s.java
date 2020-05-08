 package javax.microedition.lcdui.game;
 
 import javax.microedition.lcdui.*;
 
 public class GameCanvas extends Canvas
     {
     public GameCanvas( final boolean aSuppressKeyEvents )
         {
         mySuppressKeyEvents = aSuppressKeyEvents;
         }
 
     public final Graphics getGraphics()
         {
         if ( myBuffer == null || myGraphics == null )
             {
             myBuffer = Image.createImage( super.getWidth(), super.getHeight() );
             myGraphics = myBuffer.getGraphics();
             }
         return myGraphics;
         }
 
     public final void flushGraphics()
         {
         final Graphics graphics = displayBuffer.beginFrame();
         paint( graphics );
         displayBuffer.endFrame();
         }
 
     // From Canvas
 
     protected final void paint( final Graphics aGraphics )
         {
        aGraphics.drawImage( myBuffer, 0, 0, ALIGN_TOP_LEFT );
         }
 
 
     private Image myBuffer;
 
     private Graphics myGraphics;
 
     private boolean mySuppressKeyEvents = false;
 
     private static final int ALIGN_TOP_LEFT = Graphics.TOP | Graphics.LEFT;
     }
