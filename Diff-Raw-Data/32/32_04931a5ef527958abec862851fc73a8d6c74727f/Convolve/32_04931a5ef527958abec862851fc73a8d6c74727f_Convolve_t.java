 // I, Yotam Gingold, the author of this file, release it to the public domain.
 
 /*
 javac Convolve.java
 java -classpath . Convolve input.png output.png kernel00 kernel01 kernel02 kernel10 kernel11 kernel12 kernel20 kernel21 kernel22
 */
 
 public class Convolve
 {
     public static void main( String [] args )
     throws java.io.IOException
     {
         ImageAsArray.ImageAsArrayHolder data = ImageAsArray.LoadImageAsByteArrayARGB( args[0] );
         /*
        data.width = 3;
        data.height = 3;
        data.pixels_ARGB = new int[ 3*3*4 ];
        for( int i = 0; i < data.pixels_ARGB.length; ++i )
        {
            data.pixels_ARGB[ i ] = 255;
        }
        */
        
        /*
         // Convert to greyscale.
         for( int i = 0; i < data.width*data.height; ++i )
         {
             int avg = 0;
             avg += data.pixels_ARGB[ 4*i + 1 ];
             avg += data.pixels_ARGB[ 4*i + 2 ];
             avg += data.pixels_ARGB[ 4*i + 3 ];
             avg /= 3;
             data.pixels_ARGB[ 4*i + 1 ] = avg;
             data.pixels_ARGB[ 4*i + 2 ] = avg;
             data.pixels_ARGB[ 4*i + 3 ] = avg;
         }
         */
         
         int [] kernel = new int[ 9 ];
         for( int i = 0; i < 9; ++i )
         {
             kernel[ i ] = Integer.parseInt( args[ i+2 ] );
            System.out.println( "kernel[ " + i + " ]: " + kernel[i] );
         }
         
         ImageAsArray.ImageAsArrayHolder output = new ImageAsArray.ImageAsArrayHolder( data );
        for( int i = 0; i < output.pixels_ARGB.length; ++i )
        {
            output.pixels_ARGB[ i ] = 255;
        }
         
        for( int col = 1; col < data.width-1; ++col )
        for( int row = 1; row < data.height-1; ++row )
         {
             for( int channel = 1; channel < 4; ++channel )
             {
                 output.set( row, col, channel, ConvolveAt( kernel, data, col, row, channel ) );
             }
         }
         
         ImageAsArray.SaveImageFromByteArrayARGB( output, args[1] );
     }
     
     public static int ConvolveAt( int[] kernel, ImageAsArray.ImageAsArrayHolder data, int col, int row, int channel )
     {
         // Ignore pixels outside the image boundary.  This isn't the best option,
         // especially if we're not renormalizing the kernel, but it's simple.
         int col0 = Math.max( col - 1, 0 ) - col;
         int col1 = Math.min( col + 1, data.width - 1 ) - col;
         
         int row0 = Math.max( row - 1, 0 ) - row;
         int row1 = Math.min( row + 1, data.height - 1 ) - row;
         
         assert col0 <= 0;
         assert col1 >= 0;
         
         assert row0 <= 0;
         assert row1 >= 0;
         
         int sum = 0;
        for( int i = col0; i <= col1; ++i )
        for( int j = row0; j <= row1; ++j )
         {
            // System.out.println( "sum += " + data.get( row+j, col+i, channel ) + " * " + kernel[ 1+i + (1+j)*3 ] );
             sum += data.get( row+j, col+i, channel ) * kernel[ 1+i + (1+j)*3 ];
         }
         
        // System.out.println( "sum: " + sum );
        
         // return sum;
         return Math.min( Math.max( sum, 0 ), 255 );
         // return 255 - Math.min( Math.max( sum, 0 ), 255 );
         // return Math.min( Math.max( Math.abs( sum ), 0 ), 255 );
     }
 }
