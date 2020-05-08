 /* Entry for sortable.com coding challenge
  * by Kellen Steffen
  */
 
 import java.util.Stack;
 import java.util.Iterator;
 import java.io.FileReader;
 import java.io.FileWriter;
 import org.json.*;
 
 public class Match
 {
   public static final String PRODUCT_NAME = "product_name";
   public static final String PRODUCT_MANUFACTURER = "manufacturer";
   public static final String PRODUCT_MODEL = "model";
   public static final String PRODUCT_DATE = "announced-date";
 
   public static final String LISTING_TITLE = "title";
   public static final String LISTING_MANUFACTURER = "manufacturer";
   public static final String LISTING_CURRENCY = "currency";
   public static final String LISTING_PRICE = "price";
 
   public static final String PRODUCT_FILE = "products.txt";
   public static final String LISTING_FILE = "listings.txt";
   public static final String RESULT_FILE = "results.txt";
 
   public static void main( String[] args )
   {
     FileReader productReader;
     FileReader listingReader;
     FileWriter resultWriter;
    JSONWriter jsonWriter;
     JSONTokener toke;
     Stack<JSONObject> productStack = new Stack<JSONObject>();
     Stack<JSONObject> listingStack = new Stack<JSONObject>();
 
     try {
       // open up the product, listing, and result files
      productReader = new FileReader( PRODUCT_FILE );
      listingReader = new FileReader( LISTING_FILE );
      resultWriter = new FileWriter( RESULT_FILE );
      jsonWriter = new JSONWriter( resultWriter );
 
       // read the product file
       toke = new JSONTokener( productReader );
       while ( toke.more() ) {
         // get the next line
         String str = toke.nextTo( '\n' );
         // discard the newline
         toke.next();
 
         // convert the string to JSON and put it on the product stack
         productStack.push( new JSONObject( str ) );
       }
       productReader.close();
 
       // read the listing file
       toke = new JSONTokener( listingReader );
       while ( toke.more() ) {
         // get the next line
         String str = toke.nextTo( '\n' );
         toke.next();
 
         // convert the string to JSON and put it on the listing stack
         listingStack.push( new JSONObject( str ) );
       }
       listingReader.close();
 
       // compare products and listings
       
       // write the results to disk
       // ...
      System.out.println( "Results written to results.txt." );
      jsonWriter = null;
       resultWriter.close();
 
     } catch ( Exception e ) {
       System.err.println( "that was exceptional!" );
       System.err.println( e );
       System.exit( 1 );
     }
   }
 }
