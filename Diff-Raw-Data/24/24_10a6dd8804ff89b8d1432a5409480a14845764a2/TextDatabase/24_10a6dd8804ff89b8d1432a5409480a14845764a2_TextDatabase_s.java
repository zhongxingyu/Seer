 package mediastore;
 
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.color.ColorSpace;
 import java.awt.image.BufferedImage;
 import java.awt.image.BufferedImageOp;
 import java.awt.image.ColorConvertOp;
 import java.awt.image.LookupOp;
 import java.awt.image.ShortLookupTable;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.LineNumberReader;
 import java.io.OutputStreamWriter;
 import java.util.LinkedList;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 
 /**
  * A class that interacts with a text-based database.
  *
  * @author Milton John, Ryan Smith and Cole Arnold
  * @version 1.0 Feb 7, 2013
  *
  */
 public class TextDatabase extends Database {
 
     private String rootDir;
 
     /**
      * Parses a text database from a given root directory.
      *
      * @param rootDir Location in the file system of the database
      */
     public TextDatabase( String rootDir ) throws java.lang.Exception {
         this.rootDir = rootDir;
         maxMediaID = 1;
         maxCustomerID = 1;
         maxPurchaseID = 1;
 
         // check that the root dir exists
         File rootDirFile = new File( rootDir );
         if ( !rootDirFile.exists() ) {
             System.out.println( "WARNING: \"" + rootDirFile.getCanonicalPath() + "\" is missing, intializing an empty master database at this location..." );
             rootDirFile.mkdir();
         }
 
         // <editor-fold defaultstate="collapsed" desc="parse customer database">
 
         // check that the customer folder exists
         File customerFolder = new File( rootDir.concat( "Customers" ) );
         if ( !customerFolder.exists() ) {
             System.out.println( "WARNING: \"" + customerFolder.getCanonicalPath() + "\" is missing, initalizing an empty customer database at this location..." );
             customerFolder.mkdir();
         }
 
         // get number of customers
         File[] filesInCustomerFolder = customerFolder.listFiles();
         customerCount = filesInCustomerFolder.length;
 
 
         // initalize customer list
         customers = new LinkedList();
 
 
         for ( File f : filesInCustomerFolder ) {
 
             // strip the '.txt' from the filename to get the customer id
             int id = Integer.parseInt( getFilename( f ) );
 
             checkCustomerID( id );
 
             if ( id == 0 ) {
                 throw new java.util.InputMismatchException( "Invalid customer ID 0 present." );
             }
 
             // parse the customer's information
             LineNumberReader in = new LineNumberReader( new FileReader( f ) );
             String name = in.readLine();
             String address = in.readLine();
             double credit = Double.parseDouble( in.readLine() );
 
             // parse the customer's purchase history
             LinkedList purchaseHistory = new LinkedList();
             while ( in.ready() ) {
                 int purchaseID = Integer.parseInt( in.readLine() );
                 double purchasePrice = Double.parseDouble( in.readLine() );
                 long purchaseDate = Long.parseLong( in.readLine() );
 
                 purchaseHistory.add( new Purchase( purchaseID, purchasePrice, purchaseDate ) );
 
                 checkPurchaseID( purchaseID );
             }
 
             customers.add( new Customer( id, name, address, credit, purchaseHistory, this ) );
 
             in.close();
 
         }
         // </editor-fold>
 
         // <editor-fold defaultstate="collapsed" desc="parse media databases">
 
         // a filter that only lets directories through
         class DirectoryChecker implements FileFilter {
 
             public boolean accept( File f ) {
                 return f.isDirectory();
             }
         }
 
         DirectoryChecker filter = new DirectoryChecker();
 
         media = new LinkedList();
 
         // <editor-fold defaultstate="collapsed" desc="parse movie database">
         File movieFolder = new File( rootDir.concat( "Movies" ) );
         if ( !movieFolder.exists() ) {
             System.out.println( "WARNING: \"" + movieFolder.getCanonicalPath() + "\" is missing, initalizing an empty movie database at this location..." );
             movieFolder.mkdir();
         }
 
         // get number of movies
         File[] filesInMovieFolder = movieFolder.listFiles( filter ); // determine the number of movies
         movieCount = filesInMovieFolder.length;
 
 
 
         // parse movie database
         for ( File fDir : filesInMovieFolder ) {
             // strip the '.txt' from the filename to get the id
             int id = Integer.parseInt( fDir.getName() );
 
             checkMediaID( id );
 
             File f = null;
 
             // parse metadata
             f = new File( fDir.getCanonicalPath().concat( File.separator + "metadata.txt" ) );
             if ( !f.exists() ) {
                 System.out.println( "Error parsing database: Movie id " + id + " is missing metadata.txt" );
                 throw new java.io.FileNotFoundException();
             }
             LineNumberReader in = new LineNumberReader( new FileReader( f ) );
             String author = in.readLine();
             String title = in.readLine();
             int duration = Integer.parseInt( in.readLine() );
             String genre = in.readLine();
             double rating = Double.parseDouble( in.readLine() );
             int totalReviews = Integer.parseInt( in.readLine() );
             double price = Double.parseDouble( in.readLine() );
             int numSold = Integer.parseInt( in.readLine() );
             int releaseYear = Integer.parseInt( in.readLine() );
 
             media.add( new Movie( id, author, title, duration, genre, rating, totalReviews, price, numSold, releaseYear ) );
 
             // check for presence of cover, background, and trailer
             // warn if missing
             f = new File( fDir.getCanonicalPath().concat( File.separator + "cover.png" ) );
             if ( !f.exists() ) {
                 System.out.println( "WARNING: Movie id " + id + " is missing cover.png" );
             }
 
             f = new File( fDir.getCanonicalPath().concat( File.separator + "background.png" ) );
             if ( !f.exists() ) {
                 System.out.println( "WARNING: Movie id " + id + " is missing background.png" );
             }
 
             f = new File( fDir.getCanonicalPath().concat( File.separator + "trailer.mp4" ) );
             if ( !f.exists() ) {
                 System.out.println( "WARNING: Movie id " + id + " is missing trailer.mp4" );
             }
             in.close();
 
         }
 
 
 
 
         // </editor-fold>
 
         // <editor-fold defaultstate="collapsed" desc="parse music album database">
 
         // just so there's no confusion, in comments and output strings these are refered to as "music albums"
         // in code they're refered to as "album" or "albums"
         // the folder name is "Music"
 
         File albumFolder = new File( rootDir.concat( "Music" ) );
         if ( !albumFolder.exists() ) {
             System.out.println( "WARNING: \"" + albumFolder.getCanonicalPath() + "\" is missing, initalizing an empty music album database at this location..." );
             albumFolder.mkdir();
         }
 
         // get number of music albums
         File[] filesInAlbumFolder = albumFolder.listFiles( filter ); // determine the number of music albums
         albumCount = filesInAlbumFolder.length;
 
 
 
         // parse music albums database
         for ( File fDir : filesInAlbumFolder ) {
             // strip the '.txt' from the filename to get the id
             int id = Integer.parseInt( fDir.getName() );
 
             checkMediaID( id );
 
             File f = null;
 
             // parse metadata
             f = new File( fDir.getCanonicalPath().concat( File.separator + "metadata.txt" ) );
             if ( !f.exists() ) {
                 System.out.println( "Error parsing database: Album id " + id + " is missing metadata.txt" );
                 throw new java.io.FileNotFoundException();
             }
             LineNumberReader in = new LineNumberReader( new FileReader( f ) );
             String author = in.readLine();
             String title = in.readLine();
             int duration = Integer.parseInt( in.readLine() );
             String genre = in.readLine();
             double rating = Double.parseDouble( in.readLine() );
             int totalReviews = Integer.parseInt( in.readLine() );
             double price = Double.parseDouble( in.readLine() );
             int numSold = Integer.parseInt( in.readLine() );
 
             media.add( new Album( id, author, title, duration, genre, rating, totalReviews, price, numSold ) );
 
             // check for presence of cover, background, and preview
             // warn if missing
             f = new File( fDir.getCanonicalPath().concat( File.separator + "cover.png" ) );
             if ( !f.exists() ) {
                 System.out.println( "WARNING: Album id " + id + " is missing cover.png" );
             }
 
             f = new File( fDir.getCanonicalPath().concat( File.separator + "background.png" ) );
             if ( !f.exists() ) {
                 System.out.println( "WARNING: Album id " + id + " is missing background.png" );
             }
 
             f = new File( fDir.getCanonicalPath().concat( File.separator + "preview.mp3" ) );
             if ( !f.exists() ) {
                 System.out.println( "WARNING: Album id " + id + " is missing preview.mp3" );
             }
             in.close();
 
         }
 
         // </editor-fold>
 
         // <editor-fold defaultstate="collapsed" desc="parse audiobooks database">
 
         // the 'b' in audiobook is always lowercase
 
         File audiobookFolder = new File( rootDir.concat( "Audiobooks" ) );
         if ( !audiobookFolder.exists() ) {
             System.out.println( "WARNING: \"" + audiobookFolder.getCanonicalPath() + "\" is missing, initalizing an empty audiobook database at this location..." );
             audiobookFolder.mkdir();
         }
 
         // get number of audiobooks
         File[] filesInAudiobookFolder = audiobookFolder.listFiles( filter ); // determine the number of audiobooks
         audiobookCount = filesInAudiobookFolder.length;
 
 
 
         // parse audiobooks database
         for ( File fDir : filesInAudiobookFolder ) {
             // strip the '.txt' from the filename to get the id
             int id = Integer.parseInt( fDir.getName() );
 
             checkMediaID( id );
 
             File f = null;
 
             // parse metadata
             f = new File( fDir.getCanonicalPath().concat( File.separator + "metadata.txt" ) );
             if ( !f.exists() ) {
                 System.out.println( "Error parsing database: Audiobook id " + id + " is missing metadata.txt" );
                 throw new java.io.FileNotFoundException();
             }
             LineNumberReader in = new LineNumberReader( new FileReader( f ) );
             String author = in.readLine();
             String title = in.readLine();
             int duration = Integer.parseInt( in.readLine() );
             String genre = in.readLine();
             double rating = Double.parseDouble( in.readLine() );
             int totalReviews = Integer.parseInt( in.readLine() );
             double price = Double.parseDouble( in.readLine() );
             int numSold = Integer.parseInt( in.readLine() );
 
             media.add( new Audiobook( id, author, title, duration, genre, rating, totalReviews, price, numSold ) );
 
             // check for presence of cover, background, and preview
             // warn if missing
             f = new File( fDir.getCanonicalPath().concat( File.separator + "cover.png" ) );
             if ( !f.exists() ) {
                 System.out.println( "WARNING: Audiobook id " + id + " is missing cover.png" );
             }
 
             f = new File( fDir.getCanonicalPath().concat( File.separator + "background.png" ) );
             if ( !f.exists() ) {
                 System.out.println( "WARNING: Audiobook id " + id + " is missing background.png" );
             }
 
             f = new File( fDir.getCanonicalPath().concat( File.separator + "preview.mp3" ) );
             if ( !f.exists() ) {
                 System.out.println( "WARNING: Audiobook id " + id + " is missing preview.mp3" );
             }
             in.close();
 
         }
 
         // </editor-fold>
 
         // </editor-fold>
 
         // <editor-fold defaultstate="collapsed" desc="parse manager configuration">
 
         // check that the manager config file exists
         File managerFile = new File( rootDir.concat( "Manager.txt" ) );
         if ( !managerFile.exists() ) {
             System.out.println( "WARNING: \"" + managerFile.getCanonicalPath() + "\" is missing, intializing with the default manager password \"asdf\"..." );
             FileWriter fw = new FileWriter( managerFile );
             BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( managerFile ), "UTF-8" ) );
             out.write( "asdf" );
             out.close();
         }
 
         // parse manager config file
         LineNumberReader in = new LineNumberReader( new FileReader( managerFile ) );
         String password = in.readLine();
         manager = new Manager( password, this );
         in.close();
 
         // </editor-fold>
 
     }
 
     /**
      * Appends a customer Purchase to the database entry for the given Customer.
      *
      * @param customer
      * @param purchase
      * @throws java.io.IOException
      */
     @Override
     public void writeCustomerPurchase( Customer customer, Purchase purchase ) throws java.io.IOException {
         purchase.setID( ++maxPurchaseID );
         File customerToModify = new File( rootDir.concat( "Customers" ) + File.separator + customer.getID() + ".txt" );
         FileWriter fw = new FileWriter( customerToModify, true );
         //BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( customerToModify ), "UTF-8" ) );
         fw.write( purchase.toTextDB() );
         fw.close();
     }
 
     /**
      * Writes a Customer to the database.
      *
      * @param customer The customer to commit to the database
      * @throws java.io.IOException
      */
     @Override
     public void writeNewCustomer( Customer customer ) throws java.io.IOException {
         customers.add( customer );
         File customerToModify = new File( rootDir.concat( "Customers" ) + File.separator + customer.getID() + ".txt" );
         FileWriter fw = new FileWriter( customerToModify );
         fw.write( customer.toTextDB() );
         for ( Purchase p : customer.getPurchaseHistory() ) {
             fw.write( p.toTextDB() );
         }
         fw.close();
     }
 
     /**
      * Writes a new Media item to the database, generating a new id in the
      * process.
      *
      * @param m The Media item to commit to the database
      * @throws java.io.IOException
      */
     @Override
     public void writeNewMediaItem( Media m ) throws java.io.IOException {
         File newFile;
         File newDir;
 
         // increment media count
         mediaCount++;
 
         // generate a new ID
         m.id = maxMediaID++;
 
         if ( m instanceof Movie ) {
             // increment movie count
             movieCount++;
 
             // create a new .txt entry, fill it with data
             newDir = new File( rootDir.concat( "Movies" + File.separator + Integer.toString( maxMediaID ) + File.separator ) );
             newDir.mkdir();
             newFile = new File( newDir.getCanonicalPath() + File.separator + "metadata.txt" );
             newFile.createNewFile();
             BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( newFile ), "UTF-8" ) );
             out.write( ( (Movie) m ).toTextDB() );
             out.close();
 
             return;
         }
 
         if ( m instanceof Album ) {
             // increment album count
             albumCount++;
 
             // create a new .txt entry, fill it with data
             newDir = new File( rootDir.concat( "Music" + File.separator + Integer.toString( maxMediaID ) + File.separator ) );
             newDir.mkdir();
             newFile = new File( newDir.getCanonicalPath() + File.separator + "metadata.txt" );
             newFile.createNewFile();
             BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( newFile ), "UTF-8" ) );
             out.write( ( (Album) m ).toTextDB() );
             out.close();
 
             return;
         }
 
         if ( m instanceof Audiobook ) {
             // increment audiobook count
             audiobookCount++;
 
             // create a new .txt entry, fill it with data
             newDir = new File( rootDir.concat( "Audiobooks" + File.separator + Integer.toString( maxMediaID ) + File.separator ) );
             newDir.mkdir();
             newFile = new File( newDir.getCanonicalPath() + File.separator + "metadata.txt" );
             newFile.createNewFile();
             BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( newFile ), "UTF-8" ) );
             out.write( ( (Audiobook) m ).toTextDB() );
             out.close();
         }
     }
 
     /**
      * Writes a new Media item to the database.
      *
      * @param m The Media item to commit to the database
      * @param id The id of this new Media item
      * @throws java.io.IOException
      */
     @Override
     public void writeNewMediaItem( Media m, int id ) throws java.io.IOException {
 
         File newFile;
         File newDir;
 
         // increment media count
         mediaCount++;
 
         if ( m instanceof Movie ) {
             // increment movie count
             movieCount++;
 
             // create a new .txt entry, fill it with data
             newDir = new File( rootDir.concat( "Movies" + File.separator + Integer.toString( id ) + File.separator ) );
             newDir.mkdir();
             newFile = new File( newDir.getCanonicalPath() + File.separator + "metadata.txt" );
             newFile.createNewFile();
             BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( newFile ), "UTF-8" ) );
             out.write( ( (Movie) m ).toTextDB() );
             out.close();
 
             return;
         }
 
         if ( m instanceof Album ) {
             // increment album count
             albumCount++;
 
             // create a new .txt entry, fill it with data
             newDir = new File( rootDir.concat( "Music" + File.separator + Integer.toString( id ) + File.separator ) );
             newDir.mkdir();
             newFile = new File( newDir.getCanonicalPath() + File.separator + "metadata.txt" );
             newFile.createNewFile();
             BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( newFile ), "UTF-8" ) );
             out.write( ( (Album) m ).toTextDB() );
             out.close();
 
             return;
         }
 
         if ( m instanceof Audiobook ) {
             // increment audiobook count
             audiobookCount++;
 
             // create a new .txt entry, fill it with data
             newDir = new File( rootDir.concat( "Audiobooks" + File.separator + Integer.toString( id ) + File.separator ) );
             newDir.mkdir();
             newFile = new File( newDir.getCanonicalPath() + File.separator + "metadata.txt" );
             newFile.createNewFile();
             BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( newFile ), "UTF-8" ) );
             out.write( ( (Audiobook) m ).toTextDB() );
             out.close();
         }
     }
 
     /**
      * Overwrites an existing Media entry in the database with a modified copy
      * of it.
      *
      * @param m The Media item to commit to the database
      * @throws java.io.IOException
      */
     @Override
     public void writeModifiedMediaItem( Media m ) throws java.io.IOException {
         File newFile;
 
         if ( m instanceof Movie ) {
 
             // create a new .txt entry, fill it with data
             newFile = new File( rootDir.concat( "Movies" + File.separator + Integer.toString( m.id ) + File.separator + "metadata.txt" ) );
             newFile.delete();
             newFile.createNewFile();
             FileWriter fw = new FileWriter( newFile );
             BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( newFile ), "UTF-8" ) );
             out.write( ( (Movie) m ).toTextDB() );
             out.close();
 
             return;
         }
 
         if ( m instanceof Album ) {
 
             // create a new .txt entry, fill it with data
             newFile = new File( rootDir.concat( "Music" + File.separator + Integer.toString( m.id ) + File.separator + "metadata.txt" ) );
             newFile.delete();
             newFile.createNewFile();
             FileWriter fw = new FileWriter( newFile );
             BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( newFile ), "UTF-8" ) );
             out.write( ( (Album) m ).toTextDB() );
             out.close();
 
             return;
         }
 
         if ( m instanceof Audiobook ) {
 
             // create a new .txt entry, fill it with data
             newFile = new File( rootDir.concat( "Audiobooks" + File.separator + Integer.toString( m.id ) + File.separator + "metadata.txt" ) );
             newFile.delete();
             newFile.createNewFile();
             FileWriter fw = new FileWriter( newFile );
             BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( newFile ), "UTF-8" ) );
             out.write( ( (Audiobook) m ).toTextDB() );
             out.close();
         }
     }
 
     /**
      * Removes a Media item from the database.
      *
      * @param m The Media item to remove from the database
      * @throws java.io.IOException
      */
     @Override
     public void deleteMediaItem( Media m ) throws java.io.IOException {
         int id = m.id;
         File victim = new File( rootDir + File.separator + getFolderString( m ) + File.separator + id + File.separator );
         File[] victims = victim.listFiles();
         for ( File f : victims ) {
             f.delete();
         }
         victim.delete();
     }
 
     /**
      * Get the database folder name a given Media object should be stored in.
      *
      * @param m The Media object to check
      * @return The folder name
      */
     private String getFolderString( Media m ) {
         if ( m instanceof Movie ) {
             return "Movies";
         }
         if ( m instanceof Album ) {
             return "Music";
         }
         if ( m instanceof Audiobook ) {
             return "Audiobooks";
         }
         return null;
     }
 
     /**
      * Returns the file name, minus the extension.
      *
      * @param f The File to process
      * @return The file name, minus the extension
      */
     private String getFilename( File f ) {
         return f.getName().substring( 0, f.getName().lastIndexOf( '.' ) );
     }
 
     /**
      * Generates an ASCII art representation of a Media object's cover image.
      *
      * @param m The Media object to generate ASCII art from
      * @param width The width of the ASCII art, in characters
      * @param height The height of the ASCII art, in characters
      * @return An ASCII art representation of the cover art
      * @throws java.io.IOException
      */
     public String generateCoverASCII( Media m, int width, int height ) throws java.io.IOException {
         // thanks to https://github.com/Thelleo/Image2ASCII
         // this code is from my personal fork, https://github.com/o0whiplash0o/Image2ASCII
         BufferedImage inputImage;
         File image = new File( rootDir + getFolderString( m ) + File.separator + m.getID() + File.separator + "cover.png" );
         if ( !image.exists() ) {
             String unknown = "images/unknown.png";
             inputImage = ImageIO.read( getClass().getResource( unknown ) );
         } else {
             inputImage = ImageIO.read( image );
         }
         ColorSpace greyscale = ColorSpace.getInstance( ColorSpace.CS_GRAY );
         ColorConvertOp cco = new ColorConvertOp( greyscale, null );
         // invert the image for black-on-white terminals (like the one in NetBeans)
         // thanks to http://jonathangiles.net/blog/?p=702
         short[] invertTable;
         invertTable = new short[ 256 ];
         for ( int i = 0; i < 256; i++ ) {
             invertTable[i] = (short) ( 255 - i );
         }
         BufferedImageOp invertOp = new LookupOp( new ShortLookupTable( 0, invertTable ), null );
         BufferedImage greyscaleImage = cco.filter( inputImage, null );
         invertOp.filter( greyscaleImage, greyscaleImage );
         // end http://jonathangiles.net/blog/?p=702
         boolean border = true;
         // preserve width and aspect ratio, shrink height as necessary
         height *= (double) greyscaleImage.getHeight()
                 / (double) greyscaleImage.getWidth();
 
         if ( border ) {
             width -= 2;
             height -= 2;
         }
 
         if ( width < 0 ) {
             width = 0;
         }
         if ( height < 0 ) {
             height = 0;
         }
 
         String outputString = "";
 
         // rescale image
         BufferedImage temp = new BufferedImage( width, height,
                 BufferedImage.TYPE_INT_RGB );
         Graphics2D g = temp.createGraphics();
         g.setRenderingHint( RenderingHints.KEY_INTERPOLATION,
                 RenderingHints.VALUE_INTERPOLATION_BILINEAR );
         g.drawImage( greyscaleImage, 0, 0, width, height, 0, 0, greyscaleImage
                 .getWidth(), greyscaleImage.getHeight(), null );
         g.dispose();
         greyscaleImage = temp;
 
         if ( border ) {
             if ( ( 80 - width ) > 0 ) {
                 for ( int j = 0; j < ( ( 80 - width ) / 2 ); j++ ) {
                     outputString += ' ';
                 }
             }
             outputString += " ";
             for ( int j = 1; j <= width; j++ ) {
                 outputString += "_";
             }
             outputString += " \n";
 
         }
 
         for ( int i = 0; i < height; i++ ) {
             if ( ( 80 - width ) > 0 ) {
                 for ( int j = 0; j < ( ( 80 - width ) / 2 ); j++ ) {
                     outputString += ' ';
                 }
             }
             if ( border ) {
                 outputString += '|';
             }
             for ( int j = 0; j < width; j++ ) {
 
                 char character = ' ';
                 int pixel = greyscaleImage.getRGB( j, i );
                 int grey = pixel & 0xFF;
 
                 if ( grey > 0 ) {
                     character = ' ';
                 }
                 if ( grey >= 8 ) {
                     character = '\'';
                 }
                 if ( grey >= 16 ) {
                     character = '.';
                 }
                 if ( grey >= 32 ) {
                     character = '-';
                 }
                 if ( grey >= 48 ) {
                     character = ':';
                 }
                 if ( grey >= 64 ) {
                     character = '+';
                 }
                 if ( grey >= 80 ) {
                     character = ( Math.random() > 0.5 ) ? '/' : '\\';
                 }
                 if ( grey >= 96 ) {
                     character = 'o';
                 }
                 if ( grey >= 112 ) {
                     character = 's';
                 }
                 if ( grey >= 128 ) {
                     character = '*';
                 }
                 if ( grey >= 144 ) {
                     character = 'y';
                 }
                 if ( grey >= 160 ) {
                     character = 'h';
                 }
                 if ( grey >= 176 ) {
                     character = 'd';
                 }
                 if ( grey >= 192 ) {
                     character = 'm';
                 }
                 if ( grey >= 208 ) {
                     character = 'V';
                 }
                 if ( grey >= 224 ) {
                     character = 'Q';
                 }
                 if ( grey >= 240 ) {
                     character = 'M';
                 }
 
                 outputString += character;
 
             }
 
             if ( border ) {
                 outputString += "|\n";
             } else {
                 outputString += "\n";
             }
 
         }
 
         if ( border ) {
             if ( ( 80 - width ) > 0 ) {
                 for ( int j = 0; j < ( ( 80 - width ) / 2 ); j++ ) {
                     outputString += ' ';
                 }
             }
             outputString += '|';
             for ( int j = 1; j <= width; j++ ) {
                 outputString += "_";
             }
             outputString += "|";
 
         }
         return outputString;
     }
 
     /**
      * Preview a Media item.
      *
      * @param m The item to preview
      * @return m is returned if the operation was successful, null otherwise
      * @throws java.io.IOException
      */
     public Media preview( Media m ) throws java.io.IOException {
         File previewFile = new File( rootDir + getFolderString( m ) + File.separator + m.getID() + File.separator + ( ( m instanceof Movie ) ? "trailer.mp4" : "preview.mp3" ) );
         if ( !previewFile.exists() ) {
             return null;
         }
         String previewFilePath = previewFile.getCanonicalPath();
         String OS = System.getProperty( "os.name" );
         String cmd = "";
         Runtime runtime = Runtime.getRuntime();
         if ( OS.charAt( 0 ) == 'W' ) { // windows
             cmd = "cmd.exe /c \"" + previewFilePath + "\"";
             runtime.exec( cmd );
             return m;
         } else {
             return null;
         }
     }
    
    public ImageIcon viewCoverImage ( Media m ) throws java.io.IOException{
        ImageIcon image = new ImageIcon( rootDir + getFolderString( m ) + File.separator + m.getID() + File.separator + "cover.png" );
         return image;
     }
        public String bgFileLocation ( Media m ) throws java.io.IOException{
            return (rootDir + getFolderString( m ) + File.separator + m.getID() + File.separator + "background.png" );
        
        
     }
 }
