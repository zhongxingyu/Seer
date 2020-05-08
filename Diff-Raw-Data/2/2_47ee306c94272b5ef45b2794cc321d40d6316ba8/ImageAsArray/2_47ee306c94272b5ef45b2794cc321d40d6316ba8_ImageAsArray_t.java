 // I, Yotam Gingold, the author of this file, release it to the public domain.
 
 /*
 javac ImageAsArray.java
 java -classpath . ImageAsArray input.png output.png
 */
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import javax.imageio.ImageIO;
 
 public class ImageAsArray
 {
     public static void main( String [] args )
     throws java.io.IOException
     {
         ImageAsArrayHolder data = LoadImageAsByteArrayARGB( args[0] );
         
         // Let's examine the image data.
         int [] totals_ARGB = { 0,0,0,0 };
         for( int i = 0; i < data.width*data.height; ++i )
         {
             totals_ARGB[0] += data.pixels_ARGB[ 4*i + 0 ];
             totals_ARGB[1] += data.pixels_ARGB[ 4*i + 1 ];
             totals_ARGB[2] += data.pixels_ARGB[ 4*i + 2 ];
             totals_ARGB[3] += data.pixels_ARGB[ 4*i + 3 ];
         }
         System.out.println( "image( 0,0 ).a, r, b, g: " + data.pixels_ARGB[0] + ", " + data.pixels_ARGB[1] + ", " + data.pixels_ARGB[2] + ", " + data.pixels_ARGB[3] );
         System.out.println( "total number of pixels: " + data.width*data.height );
         System.out.println( "totals_ARGB[0] aka A: " + totals_ARGB[0] );
         System.out.println( "totals_ARGB[1] aka R: " + totals_ARGB[1] );
         System.out.println( "totals_ARGB[2] aka G: " + totals_ARGB[2] );
         System.out.println( "totals_ARGB[3] aka B: " + totals_ARGB[3] );
         
         SaveImageFromByteArrayARGB( data, args[1] );
     }
     
     public static class ImageAsArrayHolder
     {
         public int width;
         public int height;
         
         // 'pixels_ARGB' is an array with length width*height*4.
         // Pixels are stored contiguously, first row 0, then row 1, etc.
         // For each pixel, there is an int ranging from 0 to 255
         // for alpha, red, green, blue, in that order.
         // In other words, the pixel at row i and column j can be accessed:
        // pixels_ARGB[ ( i*width + j )*4 + C ],
         // where C is 0 for alpha, 1 for red, 2 for blue, and 3 for green.
         //
         // NOTE: Values above 255 cannot be represented in 8-bits-per-channel images.
         //       Calling SaveImageFromByteArrayARGB() with such values will simply
         //       ignore bits above the 8-th bit.  You have been warned.
         //
         // NOTE: This would be more naturally implemented using unsigned bytes,
         //       but Java doesn't support them.
         public int [] pixels_ARGB;
         
         public ImageAsArrayHolder()
         {
             width = -1;
             height = -1;
         }
     }
     public static ImageAsArrayHolder LoadImageAsByteArrayARGB( String path )
     throws java.io.IOException
     {
         System.err.println( "[Loading image from \"" + path + "\"]" );
         
         ImageAsArrayHolder result = new ImageAsArrayHolder();
         
         // Load the image file.
         BufferedImage image = ImageIO.read( new File( path ) );
         int int_packed_pixels_ARGB [] = image.getRGB( 0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth() );
         assert int_packed_pixels_ARGB.length == image.getWidth() * image.getHeight();
         
         result.width = image.getWidth();
         result.height = image.getHeight();
         result.pixels_ARGB = new int[ result.width * result.height * 4 ];
         for( int i = 0; i < int_packed_pixels_ARGB.length; ++i )
         {
             // First bit shift and then mask.
             // Doing it the other way preserves the negativity of the high-order bit,
             // making an alpha value of 255 into -1.
             result.pixels_ARGB[ 4*i + 0 ] = ( int_packed_pixels_ARGB[ i ] >> 24 ) & 0x000000FF;
             result.pixels_ARGB[ 4*i + 1 ] = ( int_packed_pixels_ARGB[ i ] >> 16 ) & 0x000000FF;
             result.pixels_ARGB[ 4*i + 2 ] = ( int_packed_pixels_ARGB[ i ] >> 8 ) & 0x000000FF;
             result.pixels_ARGB[ 4*i + 3 ] = ( int_packed_pixels_ARGB[ i ] ) & 0x000000FF;
         }
         
         return result;
     }
     
     public static void SaveImageFromByteArrayARGB( ImageAsArrayHolder image_as_array, String path )
     throws java.io.IOException
     {
         SaveImageFromByteArrayARGB( image_as_array.width, image_as_array.height, image_as_array.pixels_ARGB, path );
     }
     public static void SaveImageFromByteArrayARGB( int width, int height, int[] pixels_ARGB, String path )
     throws java.io.IOException
     {
         assert pixels_ARGB.length == width*height*4;
         
         final String kImageType = "png";
         
         // If it's a directory, tack on 'output'.
         // NOTE: The extension (.png) will be added by the next test.
         while( new File( path ).isDirectory() )
         {
             path = path + System.getProperty("file.separator") + "output";
         }
         
         // If it doesn't end with the right extension, add it on.
         if( !path.toLowerCase().endsWith( "." + kImageType.toLowerCase() ) )
         {
             path = path + "." + kImageType;
         }
         
         // Make sure the path is unique.
         path = UniquePath( path );
         
         // Copy the data into the image.
         BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
         
         int int_packed_pixels_ARGB [] = new int[ width*height ];
         for( int i = 0; i < int_packed_pixels_ARGB.length; ++i )
         {
             int_packed_pixels_ARGB[ i ] =
                 (( pixels_ARGB[ 4*i + 0 ] & 0x000000FF ) << 24 )
                 |
                 (( pixels_ARGB[ 4*i + 1 ] & 0x000000FF ) << 16 )
                 |
                 (( pixels_ARGB[ 4*i + 2 ] & 0x000000FF ) << 8 )
                 |
                 (( pixels_ARGB[ 4*i + 3 ] & 0x000000FF ) )
                 ;
         }
         image.setRGB( 0, 0, width, height, int_packed_pixels_ARGB, 0, width );
         
         System.err.println( "[Saving image to \"" + path + "\"]" );
         ImageIO.write( image, kImageType, new File( path ) );
     }
     
     // Behaves like the python os.path.splitext() function.
     public static String [] SplitExt( String path )
     {
         String [] result = new String[2];
         
         int split_dot = path.lastIndexOf( "." );
         int split_slash = path.lastIndexOf( System.getProperty("file.separator") );
         if( -1 != split_dot && (-1 == split_slash || split_slash < split_dot) )
         {
             result[0] = path.substring( 0, split_dot );
             result[1] = path.substring( split_dot );
         }
         else
         {
             result[0] = path;
             result[1] = "";
         }
         
         return result;
     }
     
     /*
     Given an input String representing a filesystem path 'path',
     returns a new path based off of 'path' such that nothing
     exists at the return 'path'.  This is done by appending
     a number to the end of the filename.
     
     NOTE: This works for files or directories.
     */
     public static String UniquePath( String path )
     {
         // Removing trailing slashes, in case this is a path to a directory, not a file.
         path = path.replaceAll( System.getProperty("file.separator") + "*$", "" );
         
         int count = 1;
         String [] original_path_splitext = SplitExt( path );
         
         while( new File( path ).exists() )
         {
             path = original_path_splitext[0] + " " + count + original_path_splitext[1];
             count += 1;
         }
         
         return path;
     }
 }
