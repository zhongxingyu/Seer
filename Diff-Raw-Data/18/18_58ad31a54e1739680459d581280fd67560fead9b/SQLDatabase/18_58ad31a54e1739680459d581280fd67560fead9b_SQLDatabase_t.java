 package mediastore;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import javax.swing.ImageIcon;
 import java.sql.*;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * A class that interacts with a SQL-based database.
  *
  * @author Milton John, Ryan Smith and Cole Arnold
  * @version 1.0 April 14, 2013
  */
 public class SQLDatabase extends Database {
 
     Connection dbConn;
     Statement stmt;
     ResultSet results;
     final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
     String sql;
 
     public SQLDatabase( String dbName, String username, String password ) throws IOException, SQLException {
         try {
             Class.forName( driver ).newInstance();
         } catch( Exception ex ) {
             Logger.getLogger( SQLDatabase.class.getName() ).log( Level.SEVERE, null, ex );
         }
         String protocol = "jdbc:derby:";
         //String dbName = "MediaStore";   // the default database name
         dbConn = DriverManager.getConnection( protocol + dbName + ";create=true" );
         stmt = dbConn.createStatement();
 
         DatabaseMetaData dbMeta = dbConn.getMetaData();
         String[] tableTypes = { "app.manager" };
         results = dbMeta.getTables( null, null, null, new String[]{ "TABLE" } );
 
         if( !results.next() ) {
             System.out.println( "Database not found. Creating an empty database..." );
 
             InputStream is = SQLDatabase.class.getResourceAsStream( "initDB.sql" );
             InputStreamReader isr = new InputStreamReader( is );
             BufferedReader br = new BufferedReader( isr );
             StringBuilder sb = new StringBuilder();
             String line;
             line = br.readLine();
             while( line != null ) {
                 sb.append( line );
                 if( line.contains( ";" ) ) {
                     sql = sb.toString();
                     sb = new StringBuilder();
                     // executeQuery cannot process ; so remove it from sql
                     sql = sql.substring( 0, sql.indexOf( ';' ) );
                     // run sql here
                     if( sql.toUpperCase().contains( "SELECT" ) ) {
                         // SELECT statement
                         stmt.executeQuery( sql );
                     } else {
                         // All ohter type of statements
                         stmt.execute( sql );
                     }
                 }
                 line = br.readLine();
             }
         }
 
         // parse customer database
         customers = new LinkedList();
         sql = "select * from app.customer";
         results = stmt.executeQuery( sql );
         while( results.next() ) {
             customers.add( new Customer( results.getInt( "customer_id" ), results.getString( "name" ), results.getString( "address" ), results.getDouble( "balance" ), new LinkedList(), this ) );
             customerCount++;
             maxCustomerID = customerCount;
         }
 
 
         // parse purchase history
         for( Customer c : customers ) {
             sql = "select * from app.customer_purchases where customer_id = " + c.getID();
             results = stmt.executeQuery( sql );
             while( results.next() ) {
                 sql = "select * from app.purchase where purchase_id = " + results.getInt( "purchase_id" );
                 Statement s = dbConn.createStatement();
                 ResultSet r = s.executeQuery( sql );
                 long dt = 0;
                 if( r.next() ) {
                     Purchase p = new Purchase( r.getInt( "media_id" ), r.getDouble( "price" ), dt );
                     c.addPurchase( p );
                 }
             }
         }
 
         // parse media database
         media = new LinkedList();
         sql = "select * from app.media";
         results = stmt.executeQuery( sql );
         while( results.next() ) {
             if( results.getString( "type" ).equals( "mv" ) ) {
                 media.add( new Movie( results.getInt( "media_id" ), results.getString( "author" ), results.getString( "title" ), results.getInt( "duration" ), results.getString( "genre" ), results.getDouble( "rating" ), results.getInt( "total_reviews" ), results.getDouble( "price" ), results.getInt( "numsold" ), results.getInt( "releaseyear" ) ) );
             }
             if( results.getString( "type" ).equals( "al" ) ) {
                 media.add( new Album( results.getInt( "media_id" ), results.getString( "author" ), results.getString( "title" ), results.getInt( "duration" ), results.getString( "genre" ), results.getDouble( "rating" ), results.getInt( "total_reviews" ), results.getDouble( "price" ), results.getInt( "numsold" ) ) );
             }
             if( results.getString( "type" ).equals( "ab" ) ) {
                 media.add( new Audiobook( results.getInt( "media_id" ), results.getString( "author" ), results.getString( "title" ), results.getInt( "duration" ), results.getString( "genre" ), results.getDouble( "rating" ), results.getInt( "total_reviews" ), results.getDouble( "price" ), results.getInt( "numsold" ) ) );
             }
         }
 
         // parse manager database
         sql = "select * from app.manager";
         results = stmt.executeQuery( sql );
         results.next();
         manager = new Manager( results.getString( "password" ), this );
         System.out.println();
 
     }
 
      /**
      * Writes a Customer to the database.
      *
      * @param customer The customer to commit to the database
      * @throws java.io.IOException, java.sql.SQLException
      */
     @Override
     public void writeNewCustomer( Customer customer ) throws IOException, SQLException {
 
 
        sql = "insert into CUSTOMER ( NAME, ADDRESS, BALANCE ) values ( " + customer.getSqlStatement() + " )";
        stmt.execute( sql );
 
     }
     
     /**
      * Writes a Customer to the database.
      *
      * @param customer The customer to commit to the database
      * @throws java.io.IOException, java.sql.SQLException
      */
     @Override
     public void writeModifiedCustomer( Customer customer ) throws IOException, SQLException {
         
         sql = "insert into CUSTOMER ( NAME, ADDRESS, BALANCE ) values ( " + customer.getSqlStatement() + " )";
        stmt.execute( sql );   
     }
 
     @Override
     public void writeCustomerPurchase( Customer customer, Purchase purchase ) throws IOException, SQLException {
         
        sql = "insert into CUSTOMER_PURCHASES ( CUSTOMER_ID, PURCHASE_ID ) values ( " + customer.getID() + "," + purchase.getID() + " )";
        stmt.execute(  sql );
     }
 
      /**
      * Writes a new Media item to the database, generating a new id in the
      * process.
      *
      * @param m The Media item to commit to the database
      * @throws java.io.IOException, java.sql.SQLException
      */
     @Override
     public void writeNewMediaItem( Media m ) throws IOException, SQLException {
         
         String type = "";
         
         if( m instanceof Movie) {
             type = "mv";
         }
         if( m instanceof Album) {
             type = "al";
         }
         if( m instanceof Audiobook) {
             type = "ab";
         }
         
         sql = " insert into MEDIA ( TYPE, AUTHOR, TITLE, DURATION, GENRE, RATING, TOTAL_REVIEWS. PRICE, NUMSOLD, RELEASEYEAR) values ( \'" + type + "\', \'" + m.getAuthor() + "\'  , \'" + m.getTitle() + "\'  , " + m.getDuration() + ", \'" + m.getGenre() + "\', " + m.getRating() + ", " + m.getTotalReviews() + ", " + m.getPrice() + ", " + m.getNumSold() + ", " + m.getReleaseYear() + " )";
        stmt.execute( sql );
     }
 
     @Override
     public void writeNewMediaItem( Media m, int id ) throws IOException, SQLException {
     }
 
     @Override
     public void writeModifiedMediaItem( Media m ) throws IOException, SQLException {
        
        writeNewMediaItem( m, m.getID());
     }
 
     @Override
     public void deleteMediaItem( Media m ) throws IOException, SQLException {
     }
 
     // don't bother implementing these
     @Override
     public Media preview( Media m ) throws IOException, SQLException {
         return null;
     }
 
     @Override
     public ImageIcon viewCoverImage( Media m ) throws IOException, SQLException {
         return null;
     }
 
     @Override
     public String bgFileLocation( Media m ) throws IOException, SQLException {
         return null;
     }
 }
