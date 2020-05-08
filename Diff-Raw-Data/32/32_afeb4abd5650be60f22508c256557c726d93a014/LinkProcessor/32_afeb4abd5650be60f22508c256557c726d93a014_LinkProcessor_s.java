 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ConcurrentHashMap;
 
 import java.sql.DriverManager;
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import java.net.URI;
 import java.net.URL;
 
 public class LinkProcessor extends Thread
 {
     private ArrayList<String> linkKeys = new ArrayList<String>();
     private ConcurrentLinkedQueue<String> linkKeyQueue =
         new ConcurrentLinkedQueue<String>();
     private ConcurrentHashMap<String,String> allUrls = null;
     private ConcurrentHashMap<String,String> linkDomains = null;
 
     private Connection          dbh = null;
     private ResultsModel resultsModel = null;
 
     private boolean urlsLoaded = false;
     private int lastCompleted = -1;
     private int completedCount = 0;
 
     private LinkDataFetcher shareCountFetcher = null;
     private LinkDataFetcher titleFetcher = null;
 
     private boolean interrupted = false;
 
     private String dbFile = null;
 
     public LinkProcessor( ResultsModel resultsModel, String dbFile )
     {
         this.resultsModel = resultsModel;
         this.dbFile     = dbFile;
 
         allUrls     = new ConcurrentHashMap<String,String>();
         linkDomains = new ConcurrentHashMap<String,String>();
 
         dbh = dbConnect();
         dbCreateTables();
     }
 
     public void finalize()
     {
         dbClose();
     }
     
     public boolean addLinkKey( String linkKey, String url )
     {
         if( linkKey != null && url != null && allUrls.put( linkKey, url ) == null )
         {
             linkKeys.add( linkKey );
             return linkKeyQueue.add( linkKey );
         }
         return false;
     }
 
     public void setUrlsLoadedState( boolean loaded )
     {
         urlsLoaded = loaded;
     }
 
     public void run()
     {
         while( ! interrupted )
         {
             // POPULATE GUI TABLE WITH DATA FROM DATABASE
             // Fetch from db: title, rating, share_count, tags
             // populate jtable resultsTable
             while( ! interrupted && ! ( urlsLoaded && linkKeyQueue.isEmpty() ) )
             {
                 String linkKey = linkKeyQueue.poll();
                 if( linkKey == null )
                 {
                     sleep( 1000 );
                     continue;
                 }
 
                 Stome.buttonsSetEnabled( false );
 
                 String url    = allUrls.get( linkKey );
 
                 Rating rating = dbGetRating( linkKey );
                 String domain = getLinkDomain( linkKey, url );
                 String newTag = "";
                 Tags   tags   = dbGetLinkTags( linkKey );
                 String title  = dbGetTitle( linkKey );
 
                 int shareCount = rating.getShareCount();
 
                 Hyperlink link = null;
                 try { link = new Hyperlink( linkKey, title, new URL( url ) ); }
                 catch( java.net.MalformedURLException ex ) {}
 
                 // Sorts links as it adds them
                 int rowIndex = 0;
                 for( ; rowIndex < resultsModel.getRowCount(); rowIndex++ )
                 {
                     int sc = ( (Rating) 
                         resultsModel.getValueAt( rowIndex,
                             ResultsModel.SHARES_COL ) ).getShareCount();
                     if( shareCount > sc )
                         break;
                 }
 
                 Object[] obj = new Object[ resultsModel.getColumnCount() ];
                 obj[ ResultsModel.SHARES_COL ]  = rating;
                 obj[ ResultsModel.NEW_TAG_COL ] = newTag;
                 obj[ ResultsModel.TAGS_COL ]    = tags;
                 obj[ ResultsModel.DOMAIN_COL ]  = domain;
                 obj[ ResultsModel.LINK_COL ]    = link;
                 
                 resultsModel.insertRow( rowIndex, obj );
 //                System.out.println( "db data: " + url );
             }
 
             // UPDATE GUI TABLE (AND DATABASE) WITH DATA FROM INTERNET
             // For each piece of data these LinkDataFetchers fetch,
             // they call LinkProcessor.processLinkData() which updates the
             // GUI table and the database
 
             if( linkKeys.size() > 0 )
             {
                 ArrayList<String> linkKeysWOShares = linkKeysWithoutShares();
                 completedCount += ( linkKeys.size() - linkKeysWOShares.size() );
 
                 shareCountFetcher =
                     new LinkDataFetcher( Stome.SHARE_COUNT, linkKeysWOShares, 
                                          allUrls, this );
                 shareCountFetcher.start();
 
                 int lastShareCountCompleted = 0;
                 int shareCountCompleted = 0;
 
                 // Wait until share count fetching is complete
                 while( ! interrupted && ! shareCountFetcher.completed() )
                 {
                     sleep( 1000 );
                     shareCountCompleted = shareCountFetcher.completedCount();
                     if( ! interrupted && 
                         shareCountCompleted > lastShareCountCompleted )
                     {
                         titleFetcher =
                             new LinkDataFetcher(
                                 Stome.TITLE, linkKeysWithoutTitles( 0 ), 
                                 allUrls, this );
                         titleFetcher.start();
                         
                         // Wait until title count fetching is complete
                         while( ! titleFetcher.completed() )
                         {
                             sleep( 500 );
                         }
                     }
                 }
 
                 if( interrupted )
                     break;
 
                 completedCount += linkKeysWOShares.size();
                 if( linkKeysWOShares.size() == 0 )
                     setFetchStatus( 0, 0 );
 
 //                linkKeys.removeAllElements();
                 linkKeys.clear();
 
                 // Find the links in table which are missing shareCount or title
                 // Try to fetch these values again
 
                 ArrayList<String> shareCountLinkKeys = new ArrayList<String>();
                 ArrayList<String> titleLinkKeys = new ArrayList<String>();
 
                 // Get linkKeys from GUI table
                 for( int i = 0; i < resultsModel.getRowCount(); i++ )
                 {
                     Rating rr = (Rating) resultsModel.getValueAt(
                         i, ResultsModel.SHARES_COL );
                     if( rr.getShareCount() == -1 )
                         shareCountLinkKeys.add( rr.getLinkKey() );
 
                     Hyperlink rh = (Hyperlink) resultsModel.getValueAt(
                         i, ResultsModel.LINK_COL );
                     if( rh.getTitle() == null )
                         titleLinkKeys.add( rh.getLinkKey() );
                 }
 
                 shareCountFetcher =
                     new LinkDataFetcher( Stome.SHARE_COUNT, shareCountLinkKeys,
                                          allUrls, this );
                 shareCountFetcher.start();
 
                 titleFetcher =
                     new LinkDataFetcher( Stome.TITLE, titleLinkKeys,
                                          allUrls, this );
                 titleFetcher.start();
 
                 // Wait until share count fetching is complete
                 while( ! shareCountFetcher.completed() &&
                        ! titleFetcher.completed() )
                     sleep( 1000 );
 
                 // Remove links with no associated tags from the "links" table
                 String query = "DELETE FROM links WHERE id NOT IN " + 
                     "( SELECT DISTINCT( link_id ) FROM link_tags );";
                 dbUpdate( query );
 
                 // Delete expired links in all_links (1 day or older)
                 query = "DELETE FROM all_links " +
                     "WHERE DATE( 'now' ) > DATE( added_on, '+24 hours' )";
                 dbUpdate( query );
 
                 // Update completed count
                 lastCompleted = -1;
 
                 Stome.buttonsSetEnabled( true );
             }
 
             sleep( 1000 );
         }
     }
 
     // Gets linkKeys from GUI table
     private ArrayList<String> linkKeysWithoutTitles( int shareCountGreaterThan )
     {
         ArrayList<String> titleLinkKeys = new ArrayList<String>();
         for( int i = 0; i < resultsModel.getRowCount(); i++ )
         {
             Rating rr = 
                 (Rating) resultsModel.getValueAt( i, ResultsModel.SHARES_COL );
             if( rr.getShareCount() > shareCountGreaterThan )
             {
                 Hyperlink rh = 
                     (Hyperlink) resultsModel.getValueAt( i, ResultsModel.LINK_COL );
                 if( rh.getTitle() == null )
                     titleLinkKeys.add( rh.getLinkKey() );
             }
         }
         return titleLinkKeys;
     }
 
     private ArrayList<String> linkKeysWithoutShares()
     {
         ArrayList<String> shareLinkKeys = new ArrayList<String>();
         for( int i = 0; i < resultsModel.getRowCount(); i++ )
         {
             Rating rr = 
                 (Rating) resultsModel.getValueAt( i, ResultsModel.SHARES_COL );
             if( rr.getShareCount() == -1 )
             {
                 String linkKey = rr.getLinkKey();
                 if( linkKeys.contains( linkKey ) )
                     shareLinkKeys.add( rr.getLinkKey() );
             }
         }
         return shareLinkKeys;
     }
 
     public synchronized void setFetchStatus( int completed, int total )
     {
         if( completed > lastCompleted )
         {
             lastCompleted = completed;
             resultsModel.setFetchStatus( ( completed + completedCount ) + " / " + 
                                        ( total + completedCount ) );
         }
     }
 
     public synchronized void processLinkData( 
         int type, String linkKey, String value )
     {
         if( value == null || value.equals( "" ) )
             return;
 
         int rowIndex = 0;
         for( ; rowIndex < resultsModel.getRowCount(); rowIndex++ )
         {
             if( type == Stome.SHARE_COUNT )
             {
                 Rating rr = (Rating) resultsModel.getValueAt(
                     rowIndex, ResultsModel.SHARES_COL );
                 String lk = rr.getLinkKey();
                 if( linkKey == lk )
                 {
 
                     int shareCount = Integer.parseInt( value );
                     dbAddLink( linkKey, shareCount );
 
                     // Update shareCount value in GUI Table
                     rr.setShareCount( shareCount );
                     resultsModel.setValueAt( rr, rowIndex, ResultsModel.SHARES_COL );
 
                     // Perform sort on GUI Table
                     for( int j = rowIndex - 1; j >= 0; j-- )
                     {
                         Rating rr2 = (Rating) resultsModel.getValueAt(
                             j, ResultsModel.SHARES_COL );
                         if( shareCount > rr2.getShareCount() )
                         {
                             resultsModel.moveRow( rowIndex, rowIndex, j );
                             rowIndex = j;
                         }
                     }
 
                     break;
                 }
             }
             else if( type == Stome.TITLE )
             {
                 Hyperlink rh = (Hyperlink) resultsModel.getValueAt(
                     rowIndex, ResultsModel.LINK_COL );
                 String lk = rh.getLinkKey();
                 if( linkKey == lk )
                 {
                     rh.setTitle( value );
                     resultsModel.setValueAt( rh, rowIndex, ResultsModel.LINK_COL );
                     break;
                 }
             }
         }
     }
 
     // Adds link to all_links table
 
     private void dbAddLink( String linkKey, int shareCount )
     {
         if( shareCount < 0 )
             return;
 
         String url = allUrls.get( linkKey );
         String domain = getLinkDomain( linkKey, url );
 
         // Get domainId of domain
         String query = "SELECT id FROM domains WHERE domain = '" + domain + "'";
         ArrayList<String[]> results = dbSelect( query, 1 );
         String domainId = "null";
         if( results.size() > 0 && results.get( 0 ).length > 0 )
             domainId = "'" + results.get( 0 )[ 0 ] + "'";
 
         // Domain doesn't exist, so add it to domains table
         if( domainId.equals( "null" ) )
         {
             query = "INSERT INTO domains( domain ) VALUES( '" + domain + "' )";
             dbUpdate( query );
 
             query = "SELECT id FROM domains WHERE domain = '" + domain + "'";
             results = dbSelect( query, 1 );
             if( results.size() > 0 && results.get( 0 ).length > 0 )
                 domainId = "'" + results.get( 0 )[ 0 ] + "'";
         }
 
         // Set domain_id in all_links table
         query = "INSERT OR REPLACE INTO all_links" + 
             "( link_key, domain_id, share_count ) " + 
             "VALUES( '" + linkKey + "', " + domainId + ", " + shareCount + " )";
         dbUpdate( query );
     }
 
     // Adds link to links table
 
     public void dbAddLink( String linkKey, String title )
     {
         dbUpdateTitle( linkKey, title );
     }
 
     // Can't add link tag if link wasn't already added via dbAddLink
 
     public void dbAddLinkTag( String linkKey, String newTag )
     {
         Tags tags = new Tags();
         ArrayList<String[]> results = dbSelect(
             "SELECT id FROM links WHERE link_key = '" + linkKey + "'", 1 );
         if( results.size() > 0 && results.get( 0 ).length > 0 )
         {
             String linkId = results.get( 0 )[ 0 ];
 
             results = dbSelect(
                 "SELECT id FROM tags WHERE tag = '" + newTag + "'", 1 );
 
             // Tag doesn't exist, create it
             if( results.size() == 0 )
             {
                 dbUpdate( "INSERT INTO tags( tag ) " + 
                     "VALUES( '" + newTag + "' )" );
                 results = dbSelect(
                     "SELECT id FROM tags WHERE tag = '" + newTag + "'", 1 );
             }
 
             String tagId = results.get( 0 )[ 0 ];
 
             dbUpdate( "INSERT INTO link_tags( link_id, tag_id ) " +
                 "VALUES( '" + linkId + "', '" + tagId + "' )" );
         }
     }
 
     public void dbDeleteTag( String tagName )
     {
         String tagId  = dbGetTagId( tagName );
         String sql = "DELETE FROM tags WHERE id = " + tagId;
         dbUpdate( sql );
     }
 
     public void dbDeleteLinkTag( String linkKey, String tagName )
     {
         String tagId  = dbGetTagId( tagName );
         String linkId = dbGetLinkId( linkKey );
         String sql = "DELETE FROM link_tags WHERE tag_id = " + tagId +
             " AND link_id = " + linkId;
         dbUpdate( sql );
 
         // Check to see if any links are associated with tag, if not delete tag
         ArrayList<String[]> results = dbSelect(
             "SELECT COUNT(*) FROM link_tags WHERE tag_id = " + tagId, 1 );
         if( results.size() > 0 && results.get( 0 ).length > 0 )
         {
             int count = Integer.parseInt( (String) results.get( 0 )[ 0 ] );
             if( count == 0 )
                 dbDeleteTag( tagName );
         }
     }
 
     private String getLinkDomain( String linkKey, String url )
     {
         String domain = linkDomains.get( linkKey );
         if( linkDomains.get( linkKey ) == null )
         {
             // Extract domain from url
             try
             {
                 URI uri = new URI( url );
                 domain = uri.getHost();
                 if( domain.startsWith( "www." ) )
                     domain = domain.substring( 4 );
             }
             catch( java.net.URISyntaxException ex ) {}
             if( domain == null )
                 domain = "";
         }
 
         return domain;
     }
 
     private String dbGetTitle( String linkKey )
     {
         ArrayList<String[]> results = dbSelect(
             "SELECT title FROM links WHERE link_key = '" + linkKey + "'", 1 );
         if( results.size() > 0 && results.get( 0 ).length > 0 )
             return results.get( 0 )[ 0 ];
 
         return null;
     }
 
     public Tags dbGetAllTags()
     {
         Tags tags = new Tags();
         ArrayList<String[]> results =
             dbSelect( "SELECT tag FROM tags ORDER BY tag", 1 );
         if( results.size() > 0 )
         {
             for( int i = 0; i < results.size(); i++ )
                 tags.add( results.get( i )[ 0 ] );
         }
         return tags;
     }
 
     public Integer dbGetTagCount( String tagName, Tags selectedTags )
     {
         String whereClause = "tag_id = " + dbGetTagId( tagName );
         if( selectedTags.size() > 0 )
         {
             whereClause += " OR ";
 
             for( int i = 0; i < selectedTags.size(); i++ )
             {
                 String tn = selectedTags.get( i );
                 whereClause += "tag_id = " + dbGetTagId( tn );
                 if( i < selectedTags.size() - 1 )
                     whereClause += " OR ";
             }
         }
 
         int tagCount = selectedTags.size() + 1;
 
         String sql = "SELECT COUNT(*) FROM ( " +
             "SELECT link_id, COUNT(*) c FROM link_tags WHERE ( " +
             whereClause + " ) GROUP BY link_id ) WHERE c = " + tagCount;
 
         ArrayList<String[]> results = dbSelect( sql, 1 );
         if( results.size() > 0 && results.get( 0 ).length > 0 )
             return new Integer( Integer.parseInt( results.get( 0 )[ 0 ] ) );
         return new Integer( 0 );
     }
 
     public ArrayList<String> dbGetTagLinks( Tags tags )
     {
         HashMap<String,Integer> linkFreq = new HashMap<String,Integer>();
 
         for( int i = 0; i < tags.size(); i++ )
         {
             String tagName = tags.get( i );
             String tagId = dbGetTagId( tagName );
             String sql = "SELECT url FROM links, link_tags WHERE tag_id = " + tagId +
                 " AND id = link_id";
             ArrayList<String[]> results = dbSelect( sql, 1 );
             for( int j = 0; j < results.size(); j++ )
             {
                 String tagLink = results.get( j )[ 0 ];
                 if( ! linkFreq.containsKey( tagLink ) )
                     linkFreq.put( tagLink, new Integer( 1 ) );
                 else
                     linkFreq.put( tagLink,
                         new Integer( linkFreq.get( tagLink ).intValue() + 1 ) );
             }
         }
 
         ArrayList<String> tagLinks = new ArrayList<String>();
 
         Iterator linkItr = linkFreq.entrySet().iterator();
         while( linkItr.hasNext() )
         {
             Map.Entry pairs = (Map.Entry) linkItr.next();
             String link   = (String)  pairs.getKey();
             Integer count = (Integer) pairs.getValue();
             linkItr.remove();
 
             if( count.intValue() == tags.size() )
                 tagLinks.add( link );
         }
         
         return tagLinks;
     }
 
     private String dbGetTagId( String tag )
     {
         String sql = "SELECT id FROM tags WHERE tag = '" + tag + "'";
         ArrayList<String[]> results = dbSelect( sql, 1 );
         if( results.size() > 0 && results.get( 0 ).length > 0 )
             return results.get( 0 )[ 0 ];
         return null;
     }
 
     private String dbGetLinkId( String linkKey )
     {
         String sql = "SELECT id FROM links WHERE link_key = '" + linkKey + "'";
         ArrayList<String[]> results = dbSelect( sql, 1 );
         if( results.size() > 0 && results.get( 0 ).length > 0 )
             return results.get( 0 )[ 0 ];
         return null;
     }
 
     private Tags dbGetLinkTags( String linkKey )
     {
         String url = allUrls.get( linkKey );
         Tags tags = new Tags();
         String linkId = dbGetLinkId( linkKey );
         if( linkId != null )
         {
             ArrayList<String[]> results = dbSelect(
                 "SELECT tag_id FROM link_tags WHERE link_id = " + linkId, 1 );
             if( results.size() > 0 )
             {
                 for( int i = 0; i < results.size(); i++ )
                 {
                     String tagId = results.get( i )[ 0 ];
                     ArrayList<String[]> results2 = dbSelect(
                         "SELECT tag FROM tags WHERE id = " + tagId, 1 );
                     if( results2.size() > 0 && results.get( 0 ).length > 0 )
                     {
                         tags.add( results2.get( 0 )[ 0 ] );
                     }
                 }
             }
         }
         return tags;
     }
 
     private Rating dbGetRating( String linkKey )
     {
         ArrayList<String[]> results = null;
         int rating     = -1;
         int shareCount = -1;
 
         results = dbSelect(
             "SELECT rating FROM links WHERE link_key = '" + linkKey + "'", 1 );
         if( results.size() > 0 && results.get( 0 ).length > 0 )
             if( results.get( 0 )[ 0 ] != null )
                 rating = Integer.parseInt( results.get( 0 )[ 0 ] );
 
         results = dbSelect(
             "SELECT share_count FROM all_links WHERE link_key = '" + 
             linkKey + "'", 1 );
         if( results.size() > 0 && results.get( 0 ).length > 0 )
             shareCount = Integer.parseInt( results.get( 0 )[ 0 ] );
 
         return new Rating( linkKey, rating, shareCount );
     }
 
     private void dbCreateTables()
     {
         String sql = null;
 
         sql =
             "CREATE TABLE IF NOT EXISTS links(" +
             "    id           INTEGER    UNIQUE PRIMARY KEY AUTOINCREMENT," +
             "    link_key     BLOB       UNIQUE NOT NULL," +
             "    url          TEXT       NOT NULL," +
             "    title        TEXT," +
             "    rating       INTEGER," +
             "    last_opened  DATETIME," +
             "    hidden       INTEGER " +
             ");";
         dbUpdate( sql );
 
         // Used to store the ratings for all links without the overhead
         // of storing the full urls
 
         sql =
             "CREATE TABLE IF NOT EXISTS all_links(" +
             "    link_key     BLOB     UNIQUE NOT NULL," +
             "    domain_id    INTEGER  NOT NULL," +
             "    share_count  INTEGER," +
             "    added_on     DATETIME DEFAULT CURRENT_TIMESTAMP" +
             ");";
         dbUpdate( sql );
 
         sql = 
             "CREATE TABLE IF NOT EXISTS domains(" +
             "    id      INTEGER  UNIQUE PRIMARY KEY AUTOINCREMENT," +
             "    domain  TEXT     NOT NULL " +
             ");";
         dbUpdate( sql );
 
         sql =
             "CREATE TABLE IF NOT EXISTS tags(" +
             "    id   INTEGER  UNIQUE PRIMARY KEY AUTOINCREMENT," +
             "    tag  TEXT     NOT NULL " +
             ");";
         dbUpdate( sql );
 
         sql =
             "CREATE TABLE IF NOT EXISTS link_tags(" +
             "    link_id  INTEGER," +
             "    tag_id   INTEGER," +
             "    PRIMARY KEY( link_id, tag_id ) " +
             ");";
         dbUpdate( sql );
     }
 
     private Connection dbConnect() 
     {
         Connection dbh = null;
         try
         {
             Class.forName( "org.sqlite.JDBC" );
             dbh = DriverManager.getConnection( "jdbc:sqlite:" + dbFile );
         }
         catch( Exception ex )
         {
             System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
            System.exit( 0 );
         }
         return dbh;
     }
 
     private void dbUpdate( String sql )
     {
         Statement stmt = null;
         try
         {
             stmt = dbh.createStatement();
             stmt.executeUpdate( sql );
         }
         catch( Exception ex )
         {
             if( ! ex.getMessage().matches( "^table \\S+ already exists$" ) )
            {
                 System.err.println( ex.getClass().getName() + 
                                     ": " + ex.getMessage() );
            }
         }
         finally
         {
            try { if( stmt != null ) { stmt.close(); } }
             catch( Exception ex ) { ex.printStackTrace(); }
         }
     }
 
     public void dbUpdateTitle( String linkKey, String title )
     {
         PreparedStatement stmt = null;
 
         String linkId = dbGetLinkId( linkKey );
 
         try
         {
             if( linkId == null )
             {
                 stmt = dbh.prepareStatement(
                     "INSERT INTO links( link_key, url, title ) " + 
                     "VALUES( ?, ?, ? )" );
                 stmt.setString( 1, linkKey );
                 stmt.setString( 2, allUrls.get( linkKey ) );
                 stmt.setString( 3, title );
             }
             else
             {
                 stmt = dbh.prepareStatement(
                     "UPDATE links SET url = ?, title = ? WHERE link_key = ?" );
                 stmt.setString( 1, allUrls.get( linkKey ) );
                 stmt.setString( 2, title );
                 stmt.setString( 3, linkKey );
             }
             stmt.executeUpdate();
         }
        catch( SQLException ex )
        {
//            if( ! ex.getMessage().matches( ".*column link_key is not unique.*" ) )
                ex.printStackTrace();
        }
         finally
         {
            try { if( stmt != null ) { stmt.close(); } }
             catch( Exception ex ) { ex.printStackTrace(); }
         }
     }
 
     private ArrayList<String[]> dbSelect( String query, int columns )
     {
         Statement stmt = null;
         ArrayList<String[]> results = new ArrayList<String[]>();
         try
         {
             stmt = dbh.createStatement();
             ResultSet rs = stmt.executeQuery( query );
 
             while( rs.next() )
             {
                 String[] row = new String[ columns ];
                 for( int j = 0; j < columns; j++ )
                     row[ j ] = rs.getString( j + 1 );
                 results.add( row );
             }
         }
         catch( Exception ex )
         {
             ex.printStackTrace();
         }
 
         return results;
     }
 
     private void dbClose()
     {
         try
         {
             dbh.close();
         }
         catch( Exception ex )
         {
             System.err.println( ex.getClass().getName() + ": " + ex.getMessage() );
         }
     }
 
     private void sleep( int milliseconds )
     {
         try { Thread.sleep( milliseconds ); }
         catch( InterruptedException e )
         {
             interrupted = true;
 
             if( shareCountFetcher != null )
             {
                 shareCountFetcher.interrupt();
                 while(   shareCountFetcher.isAlive() &&
                        ! shareCountFetcher.isInterrupted() )
                 {
                     try { Thread.sleep( 200 ); }
                     catch( InterruptedException ex ) {}
                 }
             }
 
             if( titleFetcher != null )
             {
                 titleFetcher.interrupt();
                 while(   titleFetcher.isAlive() &&
                        ! titleFetcher.isInterrupted() )
                 {
                     try { Thread.sleep( 200 ); }
                     catch( InterruptedException ex ) {}
                 }
             }
         }
     }
 }
