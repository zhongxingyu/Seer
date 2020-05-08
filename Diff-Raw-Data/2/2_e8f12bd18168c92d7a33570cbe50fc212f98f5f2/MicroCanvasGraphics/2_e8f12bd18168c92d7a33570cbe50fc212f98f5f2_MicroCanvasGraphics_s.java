 package net.intensicode.me;
 
 import net.intensicode.core.*;
 import net.intensicode.util.*;
 
 import javax.microedition.lcdui.*;
 
 public final class MicroCanvasGraphics extends DirectGraphics
     {
     Graphics gc;
 
     int width;
 
     int height;
 
     public MicroCanvasGraphics()
         {
         }
 
     public MicroCanvasGraphics( final Image aImage )
         {
         gc = aImage.getGraphics();
         width = aImage.getWidth();
         height = aImage.getHeight();
         }
 
     // From DirectGraphics
 
     public int getColorRGB24()
         {
         return gc.getColor() & 0x00FFFFFF;
         }
 
     public int getColorARGB32()
         {
         return gc.getColor();
         }
 
     public final void setColorRGB24( final int aRGB24 )
         {
         gc.setColor( 0xFF000000 | aRGB24 );
         }
 
     public final void setColorARGB32( final int aARGB32 )
         {
         gc.setColor( aARGB32 );
         }
 
     public final void setFont( final FontResource aFont )
         {
         //#if DEBUG
         Assert.isTrue( "only MicroFontResource supported for now", aFont instanceof MicroFontResource );
         //#endif
         final MicroFontResource fontResource = (MicroFontResource) aFont;
         gc.setFont( fontResource.font );
         }
 
     public final void clearRGB24( final int aRGB24 )
         {
         setColorRGB24( aRGB24 );
         gc.fillRect( 0, 0, width, height );
         }
 
     public final void drawLine( final int aX1, final int aY1, final int aX2, final int aY2 )
         {
         gc.drawLine( aX1, aY1, aX2, aY2 );
         }
 
     public final void drawRect( final int aX, final int aY, final int aWidth, final int aHeight )
         {
         gc.drawRect( aX, aY, aWidth, aHeight );
         }
 
     public final void drawRGB( final int[] aARGB32, final int aOffsetX, final int aScanlineSize, final int aX, final int aY, final int aWidth, final int aHeight, final boolean aUseAlpha )
         {
         gc.drawRGB( aARGB32, aOffsetX, aScanlineSize, aX, aY, aWidth, aHeight, aUseAlpha );
         }
 
     public final void fillRect( final int aX, final int aY, final int aWidth, final int aHeight )
         {
         gc.fillRect( aX, aY, aWidth, aHeight );
         }
 
     public final void fillTriangle( final int aX1, final int aY1, final int aX2, final int aY2, final int aX3, final int aY3 )
         {
         gc.fillTriangle( aX1, aY1, aX2, aY2, aX3, aY3 );
         }
 
     public final void blendImage( final ImageResource aImage, final int aX, final int aY, final int aAlpha256 )
         {
         //#if DEBUG
         Assert.isTrue( "only MicroImageResource supported for now", aImage instanceof MicroImageResource );
         Assert.between( "alpha value 256", 0, 255, aAlpha256 );
         //#endif
         if ( aAlpha256 == 0 )
             {
             // Nothing to do..
             }
         else if ( aAlpha256 == FULLY_OPAQUE )
             {
             drawImage( aImage, aX, aY );
             }
         else
             {
             final MicroImageResource imageResource = (MicroImageResource) aImage;
             myImageBlender.blend( imageResource, aAlpha256 );
            gc.drawRGB( myImageBlender.buffer, 0, myImageBlender.width, 0, 0, myImageBlender.width, myImageBlender.height, true );
             }
         }
 
     public final void blendImage( final ImageResource aImage, final Rectangle aSourceRect, final int aX, final int aY, final int aAlpha256 )
         {
         //#if DEBUG
         Assert.isTrue( "only MicroImageResource supported for now", aImage instanceof MicroImageResource );
         Assert.between( "alpha value 256", 0, 255, aAlpha256 );
         //#endif
         if ( aAlpha256 == 0 )
             {
             // Nothing to do..
             }
         else if ( aAlpha256 == FULLY_OPAQUE )
             {
             drawImage( aImage, aSourceRect, aX, aY );
             }
         else
             {
             final MicroImageResource imageResource = (MicroImageResource) aImage;
             myImageBlender.blend( imageResource, aSourceRect, aAlpha256 );
             gc.drawRGB( myImageBlender.buffer, 0, myImageBlender.width, aX, aY, myImageBlender.width, myImageBlender.height, true );
             }
         }
 
     public final void drawImage( final ImageResource aImage, final int aX, final int aY )
         {
         //#if DEBUG
         Assert.isTrue( "only MicroImageResource supported for now", aImage instanceof MicroImageResource );
         //#endif
         final MicroImageResource imageResource = (MicroImageResource) aImage;
         gc.drawImage( imageResource.image, aX, aY, ALIGN_TOP_LEFT );
         }
 
     public final void drawImage( final ImageResource aImage, final int aX, final int aY, final int aAlignment )
         {
         //#if DEBUG
         Assert.isTrue( "only MicroImageResource supported for now", aImage instanceof MicroImageResource );
         //#endif
         final MicroImageResource imageResource = (MicroImageResource) aImage;
         final Position aligned = getAlignedPosition( aX, aY, aImage.getWidth(), aImage.getHeight(), aAlignment );
         gc.drawImage( imageResource.image, aligned.x, aligned.y, ALIGN_TOP_LEFT );
         }
 
     public final void drawImage( final ImageResource aImage, final Rectangle aSourceRect, final int aTargetX, final int aTargetY )
         {
         //#if DEBUG
         Assert.isTrue( "only MicroImageResource supported for now", aImage instanceof MicroImageResource );
         //#endif
         final MicroImageResource imageResource = (MicroImageResource) aImage;
         storeCurrentClipRect();
         gc.clipRect( aTargetX, aTargetY, aSourceRect.width, aSourceRect.height );
         gc.drawImage( imageResource.image, aTargetX - aSourceRect.x, aTargetY - aSourceRect.y, ALIGN_TOP_LEFT );
         restorePreviousClipRect();
         }
 
     public final void drawSubstring( final String aText, final int aStart, final int aEnd, final int aX, final int aY )
         {
         gc.drawSubstring( aText, aStart, aEnd - aStart, aX, aY, ALIGN_TOP_LEFT );
         }
 
     public final void drawChar( final char aCharCode, final int aX, final int aY )
         {
         gc.drawChar( aCharCode, aX, aY, ALIGN_TOP_LEFT );
         }
 
     // Implementation
 
     private void storeCurrentClipRect()
         {
         myStoredClip.x = gc.getClipX();
         myStoredClip.y = gc.getClipY();
         myStoredClip.width = gc.getClipWidth();
         myStoredClip.height = gc.getClipHeight();
         }
 
     private void restorePreviousClipRect()
         {
         gc.setClip( myStoredClip.x, myStoredClip.y, myStoredClip.width, myStoredClip.height );
         }
 
 
     private final Rectangle myStoredClip = new Rectangle();
 
     private final ImageBlender myImageBlender = new ImageBlender();
 
     private static final int ALIGN_TOP_LEFT = Graphics.TOP | Graphics.LEFT;
     }
