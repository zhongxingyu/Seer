 package org.seventyeight;
 
 import org.apache.log4j.Logger;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 /**
  * @author cwolfgang
  */
 public class TokenList {
 
     private static Logger logger = Logger.getLogger( TokenList.class );
 
     private List<String> list;
     private int number = 0;
     private int counter = -1;
 
     public TokenList( String path ) {
         StringTokenizer tokenizer = new StringTokenizer( path, "/" );
         list = new ArrayList<String>( tokenizer.countTokens() );
         number = tokenizer.countTokens();
         //counter = number;
 
         while( tokenizer.hasMoreTokens() ) {
             String token = "";
             try {
                 token = URLDecoder.decode( tokenizer.nextToken(), "UTF-8" );
             } catch( UnsupportedEncodingException e ) {
                 throw new IllegalStateException( e );
             }
 
             list.add( token );
         }
 
         logger.debug( "Numbers: " + number + ", " + counter );
     }
 
     public String next() {
         counter++;
         return list.get( counter );
     }
 
     public void backup() {
         counter--;
     }
 
     public boolean hasMore() {
         return ( counter + 1 ) < number;
     }
 
     public boolean isEmpty() {
         return ( counter + 1 ) >= number;
     }
 
     public int left() {
         return number - ( counter + 1 );
     }
 }
