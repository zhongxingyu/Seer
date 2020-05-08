 package es.eucm.eadventure.editor.control.controllers.imageedition.filter;
 
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.awt.image.WritableRaster;
 import java.util.Stack;
 
 public class TransparentColorFilter implements ImageFilter {
 
     /**
      * Threshold for transparency
      */
     private int threshold = 15;
 
     private boolean contiguous = true;
     
     private int alphaValue = 0;
    
    private boolean active = false;
 
     public TransparentColorFilter( boolean contiguous, int threshold ) {
 
         this.contiguous = contiguous;
         this.threshold = threshold;
     }
    
    public void setActive( boolean active ){
        this.active = active;
    }
 
     public void transform( BufferedImage image, int x, int y ) {
 
        if( active && image.getColorModel( ).getNumComponents( ) == 4 ) {
             WritableRaster raster = image.getRaster( );
 
             int transparentColor[] = new int[ 4 ];
             raster.getPixel( x, y, transparentColor );
 
             int imagePixels[] = new int[ image.getWidth( ) * image.getHeight( ) * 4 ];
             raster.getPixels( 0, 0, image.getWidth( ), image.getHeight( ), imagePixels );
 
             int rT = transparentColor[0];
             int gT = transparentColor[1];
             int bT = transparentColor[2];
             int aT = transparentColor[3];
 
             if( contiguous ) {
                 initAlgorithm( image, imagePixels, rT, gT, bT, aT );
                 applyAlgorithm( x, y );
             }
             else {
                 for( int i = 0; i < imagePixels.length; i += 4 ) {
                     int rI = imagePixels[i];
                     int gI = imagePixels[i + 1];
                     int bI = imagePixels[i + 2];
                     if( rI >= rT - threshold && rI <= rT + threshold && gI >= gT - threshold && gI <= gT + threshold && bI >= bT - threshold && bI <= bT + threshold ) {
                         imagePixels[i + 3] = alphaValue;
                     }
                 }
 
             }
             raster.setPixels( 0, 0, image.getWidth( ), image.getHeight( ), imagePixels );
         }
     }
 
     private int width, height;
 
     private int[] imagePixels;
 
     private int rT, gT, bT, aT;
 
     private boolean[] pixelsDone;
 
     private Stack<Point> stack;
 
     private void initAlgorithm( BufferedImage image, int imagePixels[], int rT, int gT, int bT, int aT ) {
 
         width = image.getWidth( );
         height = image.getHeight( );
         this.imagePixels = imagePixels;
         this.rT = rT;
         this.gT = gT;
         this.bT = bT;
         this.aT = aT;
         pixelsDone = new boolean[ width * height ];
         stack = new Stack<Point>( );
         for( int i = 0; i < pixelsDone.length; i++ ) {
             int k = i * 4;
             pixelsDone[i] = !( imagePixels[k] < rT + threshold && imagePixels[k] > rT - threshold && imagePixels[k + 1] < gT + threshold && imagePixels[k + 1] > gT - threshold && imagePixels[k + 2] < bT + threshold && imagePixels[k + 2] > bT - threshold && imagePixels[k + 3] < aT + threshold && imagePixels[k + 3] > aT - threshold );
         }
 
     }
 
     private void applyAlgorithm( int initialX, int initialY ) {
 
         stack.push( new Point( initialX, initialY ) );
 
         while( !stack.isEmpty( ) ) {
             Point p = stack.pop( );
             int x = p.x;
             int y = p.y;
             if( x >= 0 && x < width && y >= 0 && y < height ) {
                 int pixelIndex = ( y * width ) + x;
 
                 if( pixelIndex >= 0 && pixelIndex < pixelsDone.length && !pixelsDone[pixelIndex] ) {
                     pixelsDone[pixelIndex] = true;
                     int k = pixelIndex * 4;
                     if( imagePixels[k] < rT + threshold && imagePixels[k] > rT - threshold && imagePixels[k + 1] < gT + threshold && imagePixels[k + 1] > gT - threshold && imagePixels[k + 2] < bT + threshold && imagePixels[k + 2] > bT - threshold && imagePixels[k + 3] < aT + threshold && imagePixels[k + 3] > aT - threshold ) {
                         imagePixels[k + 3] = alphaValue;
 
                         if( x - 1 >= 0 && !pixelsDone[pixelIndex - 1] )
                             stack.push( new Point( x - 1, y ) );
 
                         if( x + 1 < width && !pixelsDone[pixelIndex + 1] )
                             stack.push( new Point( x + 1, y ) );
 
                         if( y - 1 >= 0 && !pixelsDone[pixelIndex - width] )
                             stack.push( new Point( x, y - 1 ) );
 
                         if( y + 1 < height && !pixelsDone[pixelIndex + width] )
                             stack.push( new Point( x, y + 1 ) );
                     }
                 }
             }
 
         }
 
     }
 
     public void setThreshold( int threshold ) {
         if (threshold >= 1 && threshold <= 255)
             this.threshold = threshold;
     }
 
     public int getThreshold( ) {
 
         return threshold;
     }
 
     public void setContiguous( boolean contiguous ) {
 
         this.contiguous = contiguous;
     }
 
     public boolean isContiguous( ) {
 
         return this.contiguous;
     }
     
     public void setAlphaValue( int alphaValue ){
         this.alphaValue = alphaValue;
     }
 
 }
