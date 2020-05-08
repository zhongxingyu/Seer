 package com.mktneutral;
 
 import org.openqa.selenium.firefox.FirefoxDriver;
 import au.com.bytecode.opencsv.CSVReader;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.io.FileOutputStream;
 import javax.imageio.ImageIO;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.regex.Pattern;
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.ResultSet;
 import java.net.URL;
 import java.awt.image.BufferedImage;
 import java.net.MalformedURLException;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.WebDriverBackedSelenium;
 import com.thoughtworks.selenium.SeleniumException;
 import org.browsermob.proxy.ProxyServer;
 import org.openqa.selenium.Proxy;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.CapabilityType;
 import org.browsermob.core.har.Har;
 import org.browsermob.core.har.HarLog;
 import org.browsermob.core.har.HarEntry;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.JSONException;
 import java.security.NoSuchAlgorithmException;
 
 public class AdMonitor { 
     private static ProxyServer server;
     private static Proxy proxy;
     private static WebDriverBackedSelenium selena;
     private static String harFileName;
     private static ArrayList<String> adKeys;
     private static Connection hsqldb;
     private static Statement stmt;
     private static String winName;
     private static ArrayList<String> adUrls;
     private static ArrayList<String> contentTypes;    
     private static ArrayList<String> fileNames;
     private static List<String[]> adRows;
     private static List<String[]> listRows;
 
     public static void main( String[] args ) {
       try {
          String dateString  = (new java.sql.Date( (new java.util.Date()).getTime() )).toString();
         harFileName = "/tmp/NetworkTraffic" + dateString + ".har";        
          readLists(); 
 
          adKeys = new ArrayList<String>(); 
 	 for ( String[] row : adRows ) {
 	    adKeys.add( row[0].trim() );
 	 }
 
          startServer();
          getPageTraffic( listRows.get(1)[0].trim() );
          getDBConnection();         
          parseJSONTraffic();
          downloadFiles();
          
          getFileTypes();
          getBadImages();
          
          writeAdUrls();                 
          // printGoodContentTypeFiles();
          getLandingPages( listRows.get(1)[0].trim() ); //here
 
          showResults();
          closeDBConnection();
 
          stopServer();        
          killFirefoxProcess();        
 	 killJavaProcesses();              
 
          System.out.println( "End of main." );    
       } catch ( Exception e ) { e.printStackTrace(); }
     }
 
     public static void readLists() {
       try {
         CSVReader listReader = new CSVReader( new FileReader( "./inputfile_sample.txt" ),'\t' );
         CSVReader adListReader = new CSVReader( new FileReader( "./adfilter_sample.txt" ),'\t' );
         adRows = (List<String[]>) adListReader.readAll();
         listRows = (List<String[]>) listReader.readAll();
         listReader.close();
         adListReader.close();
 	// System.out.println( listRows.get(1)[0].trim() );
       } catch ( FileNotFoundException fnfe ) { fnfe.printStackTrace(); }
       catch ( IOException ioe ) { ioe.printStackTrace(); }
     }
 
     public static void startServer() {    
       try {
        System.out.println( "Starting Proxy server." );
        server = new ProxyServer( 4444 );
        server.start();
        proxy = server.seleniumProxy();
       } catch ( Exception e ) { e.printStackTrace(); }
     }
 
     public static void getPageTraffic( String _url ) {
       try {
        System.out.println( "Starting FirefoxDriver and getting page traffic." );
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setCapability(CapabilityType.PROXY, proxy);
        FirefoxDriver driver = new FirefoxDriver(capabilities);
        server.newHar( "univision.com" );
        selena = new WebDriverBackedSelenium( driver, "univision.com" );
        selena.getWrappedDriver().get( _url );
        server.getHar().writeTo( new FileOutputStream( harFileName ) );      
       } catch ( SeleniumException se ) { se.printStackTrace(); }
       catch ( IOException ioe ) { ioe.printStackTrace(); }
     }
 
    public static void parseJSONTraffic() {
     System.out.println( "Parsing JSON traffic data." );
     try {
      BufferedReader r = new BufferedReader( new FileReader( harFileName ) );
      String s;
      String jsonString = "";
      while ((s=r.readLine())!=null) {
 	jsonString += s.trim();
      }
      r.close();
     
      try {
        stmt.executeUpdate("DROP TABLE IF EXISTS url_list");
        stmt.executeUpdate("CREATE TABLE url_list ( url_string VARCHAR(2048) )");
      } catch ( SQLException sqle ) { sqle.printStackTrace(); }
 
      JSONObject logObj =  (new JSONObject( jsonString )).getJSONObject( "log" );    
      JSONArray entriesArray = logObj.getJSONArray("entries");
      for ( int i=0; i<entriesArray.length(); i++ ) {
        try {
 	JSONObject obj = entriesArray.getJSONObject( i );
         JSONObject resp = obj.getJSONObject( "response" );
         JSONObject content = resp.getJSONObject( "content" );
 
         if ( content.getString( "mimeType" ).startsWith("image/png") || content.getString( "mimeType" ).startsWith("image/jpeg") || content.getString( "mimeType" ).startsWith("image/gif") || content.getString( "mimeType" ).startsWith("application/x-shockwave-flash")  ) {
             //System.out.println( "MimeType = " + content.getString( "mimeType" ) );
 	    JSONObject req = obj.getJSONObject("request");
             String urlString = req.getString("url").trim();
             for ( String adKey : adKeys ) {
 	       if ( urlString.toLowerCase().contains(adKey) ) { 	 
 		   try {
 		      stmt.executeUpdate( "INSERT INTO url_list VALUES ( '" + urlString + "' )" );
                       // System.out.println( "INSERT INTO url_list VALUES ( '" + urlString + "' )" );
                       break;
                    } catch ( SQLException sqle ) { sqle.printStackTrace(); }   
 	       }
 	    }    
         }   
        } catch ( JSONException jsone ) { jsone.printStackTrace(); }
      }
 
      try {
       stmt.executeUpdate( "DROP TABLE IF EXISTS unique_urls" );
       stmt.executeUpdate( "CREATE TABLE unique_urls ( url_string VARCHAR(2048) )" );
       stmt.executeUpdate( "INSERT INTO unique_urls SELECT DISTINCT url_string FROM url_list WHERE (LOCATE('.js',url_string)=0  AND LOCATE('1x1',url_string)=0 AND LOCATE('1X1',url_string)=0)" );
      }  catch ( SQLException sqle ) { sqle.printStackTrace(); }     
 
     } catch ( IOException ioe ) { ioe.printStackTrace(); }
     catch ( JSONException jsone ) { jsone.printStackTrace(); }
    }
 
     public static void getDBConnection() {
       try {
          System.out.println( "Opening DB Connection." );
 	 Class.forName("org.hsqldb.jdbcDriver");
          hsqldb = DriverManager.getConnection("jdbc:hsqldb:file:admonitor", "SA", "" );
          stmt = hsqldb.createStatement();                   
       } catch ( ClassNotFoundException cnfe ) { cnfe.printStackTrace(); }
       catch ( SQLException sqle ) { sqle.printStackTrace(); }     
     }
 
     public static void closeDBConnection() {
       try {
         System.out.println( "Closing DB Connection." );
 	hsqldb.close();
       } catch ( SQLException sqle ) { sqle.printStackTrace(); }  
     }
 
     public static void downloadFiles() {
       System.out.println( "Downloading ad media files..." );
       try {
        ResultSet urls = stmt.executeQuery( "SELECT DISTINCT url_string FROM unique_urls" );
        ArrayList<String> adUrlList = new ArrayList<String>();
        while ( urls.next() ) {
 	  adUrlList.add( urls.getString(1).trim() );
        }
        urls.close();
       
        try {
 	 stmt.executeUpdate( "DROP TABLE IF EXISTS urls_files" );
          stmt.executeUpdate( "CREATE TABLE urls_files ( url_string VARCHAR(2048), file_name VARCHAR(64) )" );
        } catch ( SQLException sqle ) { sqle.printStackTrace(); }
 
        try {
         Process p = Runtime.getRuntime().exec( "/bin/rm -f /root/Desktop/AdMonitor/DL/file*" );
         p.waitFor(); 
         BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
         String s;
         while ((s=r.readLine())!=null) {
 	    // System.out.println( s );
         }
         r.close();
        } catch ( IOException ioe ) { ioe.printStackTrace(); }
        catch ( InterruptedException ie ) { ie.printStackTrace(); }
            
        int fileCounter = 0;
        for ( String adUrl : adUrlList ) {
          try {
 	   String fileName = SimpleSHA1.SHA1("file" + Integer.toString( fileCounter ));
 	   String cmdString = "/usr/bin/wget -v --output-document=/root/Desktop/AdMonitor/DL/" + fileName + " --tries=3 " + adUrl;
            System.out.println( cmdString );
 	   Process  p = Runtime.getRuntime().exec( cmdString );
            p.waitFor();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String s;
            while ((s = r.readLine())!=null) {
 	       System.out.println( s );
            }
            r.close();
            stmt.executeUpdate( "INSERT INTO urls_files VALUES ('" + adUrl + "', '" + fileName + "')" );
          } catch ( IOException ioe ) { ioe.printStackTrace(); }
          catch ( InterruptedException ie ) { ie.printStackTrace(); }
          catch ( SQLException sqle ) { sqle.printStackTrace(); }
          catch ( NoSuchAlgorithmException nsae ) { nsae.printStackTrace(); }
          fileCounter++;
        }
       } catch ( SQLException e ) { e.printStackTrace(); }    
     }
 
     public static void getFileTypes() {
      try {
       ResultSet fileNames = stmt.executeQuery( "SELECT url_string, file_name FROM urls_files" );
       stmt.executeUpdate( "DROP TABLE IF EXISTS urls_files_types" );
       stmt.executeUpdate( "CREATE TABLE urls_files_types ( url_string VARCHAR(2048), file_name VARCHAR(64), content_type VARCHAR(256) )" );
 
       while ( fileNames.next() ) {
 	  // System.out.println( fileNames.getString(2).trim() );
         String fileName = "/root/Desktop/AdMonitor/DL/" + fileNames.getString(2).trim();
         String cmdString = "/usr/bin/file -bi " + fileName;
         Process p = Runtime.getRuntime().exec( cmdString );
         p.waitFor();
         BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
         String s;
         while ((s = r.readLine())!=null) {
 	  try {
 	     System.out.println( "INSERT INTO urls_files_types VALUES ( '" + fileNames.getString(1).trim() + "', '" +  fileNames.getString(2).trim() + "', '" + s.trim() + "' )" );
 	    stmt.executeUpdate( "INSERT INTO urls_files_types VALUES ( '" + fileNames.getString(1).trim() + "', '" +  fileNames.getString(2).trim() + "', '" + s.trim() + "' )" );           
           } catch ( SQLException sqle ) { sqle.printStackTrace(); }
         }
         r.close();     
       }
  
       try {
 	fileNames.close();
 	stmt.executeUpdate( "DROP TABLE IF EXISTS good_content_types" );
         stmt.executeUpdate( "CREATE TABLE good_content_types ( url_string VARCHAR(2048), file_name VARCHAR(64), content_type VARCHAR(256) )" );
         stmt.executeUpdate( "INSERT INTO good_content_types SELECT * FROM urls_files_types WHERE ( LOCATE('image/jpeg',content_type)!=0 OR LOCATE('application/x-shockwave-flash',content_type)!=0 OR LOCATE('image/gif',content_type)!=0 OR LOCATE('image/png',content_type)!=0 )" );  
       } catch ( SQLException sqle ) { sqle.printStackTrace(); }
      } catch ( Exception ioe ) { ioe.printStackTrace(); }
     }
     
     public static void printGoodContentTypeFiles() {
       try {
          ResultSet rs =  stmt.executeQuery( "SELECT * FROM good_content_types" );
          while ( rs.next() ){
 	     // System.out.println( "good-content: " + rs.getString(1).trim() + ", " + rs.getString(2).trim() );
          }
          rs.close();
       } catch ( SQLException sqle ) { sqle.printStackTrace(); }
     }
 
     public static void getBadImages() {
       try {
 	stmt.executeUpdate( "DROP TABLE IF EXISTS bad_images" );
 	stmt.executeUpdate( "CREATE TABLE bad_images ( file_name VARCHAR(64) )" );        
         ResultSet rs =  stmt.executeQuery( "SELECT * FROM good_content_types WHERE (LOCATE('image/jpeg',content_type)!=0 OR LOCATE('image/gif',content_type)!=0 OR LOCATE('image/png',content_type)!=0) ORDER BY file_name ASC" );        
         while ( rs.next() ) {
 	  URL imgUrl = new URL( "file:///root/Desktop/AdMonitor/DL/" + rs.getString(2) );
           // System.out.println( "imgs " + rs.getString(2) );
           BufferedImage img = ImageIO.read( imgUrl );
           if ( img.getWidth() == 1 || img.getHeight() == 1 || img.getWidth() == 2 || img.getHeight() == 2 || img.getWidth() == 3 || img.getHeight() == 3 || img.getWidth() == 4 || img.getHeight() == 4 ) {
              try {
 		 // System.out.println( "INSERT INTO bad_images VALUES ( '" + rs.getString(2) + "' )" );
 	       stmt.executeUpdate( "INSERT INTO bad_images VALUES ( '" + rs.getString(2) + "' )" );
              } catch ( SQLException sqle ) { sqle.printStackTrace(); } 
           }
           img = null;
         }
         rs.close();
       } catch ( SQLException sqle ) { sqle.printStackTrace(); }
       catch ( MalformedURLException murle ) { murle.printStackTrace(); }
       catch ( IOException ioe ) { ioe.printStackTrace(); }
     }
 
     public static void writeAdUrls() {
       try {
 	 stmt.executeUpdate( "DROP TABLE IF EXISTS ad_urls" );
 	 stmt.executeUpdate( "CREATE TABLE ad_urls ( url_string VARCHAR(2048), file_name VARCHAR(64), content_type VARCHAR(256) )" );
 	 ResultSet rs = stmt.executeQuery( "SELECT * FROM good_content_types WHERE file_name NOT IN ( SELECT * FROM bad_images )" );
           
          // System.out.println( "Writing Ad URLs" );               
 	 while ( rs.next() ) {
 	  try {
               // System.out.println( "INSERT INTO ad_urls VALUES ( '" + rs.getString(1) + "', '" + rs.getString(2) +  "', '" + rs.getString(3) + "' )" ); 
               stmt.executeUpdate( "INSERT INTO ad_urls VALUES ( '" + rs.getString(1) + "', '" + rs.getString(2) +  "', '" + rs.getString(3) + "' )" );
 	      } catch ( SQLException sqle ) { sqle.printStackTrace(); }
          }
          rs.close();
       } catch ( SQLException sqle ) { sqle.printStackTrace(); }   
     }
 
     public static void getLandingPages( String _url ) {
       System.out.println( "Getting landing page URLs..." );
       try {
           adUrls = new ArrayList<String>();
           contentTypes = new ArrayList<String>();
           fileNames = new ArrayList<String>();
 
           stmt.executeUpdate("DROP TABLE IF EXISTS landing_page_urls");
           stmt.executeUpdate("CREATE TABLE landing_page_urls ( ad_url VARCHAR(2048), file_name VARCHAR(64), content_type VARCHAR(256), landing_page_url VARCHAR(2048) )");
 
 	  ResultSet rs = stmt.executeQuery( "SELECT * FROM ad_urls" );
           while ( rs.next() ) {
 	      adUrls.add( rs.getString(1).trim() );
 	      fileNames.add( rs.getString(2).trim() );
 	      contentTypes.add( rs.getString(3).trim() );
           }
 
           /*
           ArrayList<String> frameSrcs = new ArrayList<String>();
           try {
 	      //selena.getEval( "var frameList = ;" );
              // System.out.println( "iframeLength = " + selena.getEval("frameList.length;") );
              System.out.println( "here we are 1" );
              for ( int i=0; i<iFrameCount; i++ ) {
 		 frameSrcs.add( selena.getEval( "window.document.getElementsByTagName('iframe').item("+Integer.toString(i)+").getAttribute('src');" ) );
                  System.out.println( selena.getEval( "window.document.getElementsByTagName('iframe').item("+Integer.toString(i)+").getAttribute('src');" ) );
 	     }
 	     System.exit(0);
 	     } catch ( SeleniumException se ) { se.printStackTrace(); } */
 
 	  /*  for ( int i=0; i<iFrameCount; i++ ) {
 	      selena.getEval( "window.document.getElementsByTagName('iframe').item("+Integer.toString(i)+").setAttribute('name','frame"+Integer.toString(i)+"');");
 	      } */
           
 
           try {
 	    Iterator iter = null;
 	    try {
                iter = selena.getWrappedDriver().getWindowHandles().iterator();
 	    } catch ( SeleniumException se ) { se.printStackTrace(); }
             winName = "";
             while (iter.hasNext()) {
 	      winName = (String)iter.next();
             }          
             //System.out.println( "winName = " + winName );          
           } catch ( SeleniumException se ) { se.printStackTrace(); }
 
           int imgCount = 0;
           try { 
             imgCount = Integer.parseInt( selena.getEval( "var imgList = window.document.getElementsByTagName('img'); for ( var i=0; i<imgList.length; i++ ) { imgList.item(i).setAttribute('id','img'+i); } imgList.length;" ).trim() );
           } catch ( SeleniumException se ) { se.printStackTrace(); }
           //System.out.println ( "image count = " + imgCount );
           
           int embedCount = 0;
           try {
             embedCount = Integer.parseInt( selena.getEval( "var embedList = window.document.getElementsByTagName('embed'); for ( var i=0; i<embedList.length; i++ ) { embedList.item(i).setAttribute('id','embed'+i); } embedList.length;").trim() );
           } catch ( SeleniumException se ) { se.printStackTrace(); }
 	  // System.out.println( "embed count = " + embedCount );
 
           int iframeCount = 0;
           try { 
              iframeCount = Integer.parseInt( selena.getEval( "var iframeList = window.document.getElementsByTagName('iframe'); for ( var i=0; i<iframeList.length; i++ ) { iframeList.item(i).setAttribute('id','iframe'+i); } iframeList.length;" ).trim() );
           } catch ( SeleniumException se ) { se.printStackTrace(); }
           // System.out.println( "iframe count = " + iframeCount );
           
           //Process images, jpeg, png, gif
           for ( int i=0; i<imgCount; i++ ) {
 	     String imgId = "img"+Integer.toString(i);
              String imgSrc =  "";
              try {
                 imgSrc = selena.getEval("window.document.getElementById('"+imgId+"').getAttribute('src');");
              } catch ( SeleniumException se ) { se.printStackTrace(); }
 
              String[] imgPieces = imgSrc.trim().split("/");
              String imgLastPiece = imgPieces[imgPieces.length-1];
              for ( int j=0; j<contentTypes.size(); j++ ) {
 		 try {
                 if ( contentTypes.get(j).startsWith("image/png") || contentTypes.get(j).startsWith("image/jpeg") || contentTypes.get(j).startsWith("image/gif") ) {
                     String[] pieces = adUrls.get(j).trim().split("/");
                     if ( pieces[pieces.length-1].equals( imgLastPiece ) ) {
 			// System.out.println( pieces[pieces.length-1] + ", " +  imgLastPiece );
 
                         try {
                        	   selena.getEval("window.document.getElementById('"+imgId+"').parentNode.setAttribute('target','blank'); window.document.getElementById('"+imgId+"').parentNode.parentNode.setAttribute('target','blank'); window.document.getElementById('"+imgId+"').parentNode.parentNode.parentNode.setAttribute('target','blank'); window.document.getElementById('"+imgId+"').click();");
                         } catch ( SeleniumException se ) { se.printStackTrace(); }
 
                        try {
                          Thread.sleep( 3000 );
                        } catch ( InterruptedException ie ) { ie.printStackTrace(); }       
 
                        	Iterator iter2 = null;
                         try {
                           iter2 = selena.getWrappedDriver().getWindowHandles().iterator();
                         } catch ( SeleniumException se ) { se.printStackTrace(); }
 
                         while ( iter2.hasNext()) {
                             String windowName = (String)iter2.next();
 			    if ( !windowName.equals( winName ) ) {
 			      try {
 				selena.selectWindow( windowName );
                               } catch ( SeleniumException se ) { se.printStackTrace(); }
 
                               try {
 				  String landingPageUrl = selena.getLocation().trim();
                                   System.out.println("INSERT INTO landing_page_urls ( ad_url, file_name, content_type, landing_page_url ) VALUES ( '" + ((String)adUrls.get(j)).trim() + "', '" + ((String)fileNames.get(j)).trim() + "', '" + ((String)contentTypes.get(j)).trim() + "', '" + landingPageUrl + "' )");
                                   stmt.executeUpdate("INSERT INTO landing_page_urls ( ad_url, file_name, content_type, landing_page_url ) VALUES ( '" + ((String)adUrls.get(j)).trim() + "', '" + ((String)fileNames.get(j)).trim() + "', '" + ((String)contentTypes.get(j)).trim() + "', '" + landingPageUrl + "' )");
                               } catch ( SeleniumException se ) { se.printStackTrace(); }
 
 			      try {
                                 selena.close();
                               } catch ( SeleniumException se ) { se.printStackTrace(); }
                               try {
                                 selena.selectWindow( winName );
                               } catch ( SeleniumException se ) { se.printStackTrace(); }
                             }
                         }
                         break;
                     }
                 }
                } catch ( SeleniumException se ) { se.printStackTrace(); }
              }
 	    } 
           
           // Process embeds
           ArrayList<String> clickTagUrls = new ArrayList<String>();
           ArrayList<Integer> clickTagEmbedIdx = new ArrayList<Integer>();
  
           for ( int i=0; i<embedCount; i++ ) {
             String embedId = "embed"+Integer.toString(i);
             String embedSrc = "";
             try {
               embedSrc =  selena.getEval("window.document.getElementById('"+embedId+"').getAttribute('src');").trim();
             } catch ( SeleniumException se ) { se.printStackTrace(); }
 
             String[] embedPieces = embedSrc.trim().split("/");
             String embedLastPiece = embedPieces[embedPieces.length-1];
             for ( int j=0; j<contentTypes.size(); j++ ) {
 	      if ( contentTypes.get(j).startsWith("application/x-shockwave-flash") ) {
 	       try {
 	          String[] pieces = adUrls.get(j).trim().split("/");
                   if ( pieces[pieces.length-1].equals( embedLastPiece ) ) {
 		      // System.out.println( pieces[pieces.length-1] + ", " + embedLastPiece );
                       String flashVarsAttribute = "";
                       try {
                          flashVarsAttribute = selena.getEval("window.document.getElementById('"+embedId+"').getAttribute('flashvars');").trim();     
                       } catch ( SeleniumException se ) { se.printStackTrace(); }
                      
                       String vars = embedSrc + ";" + flashVarsAttribute;
                       vars = vars.replaceAll("clickTag","clicktag");
                       vars = vars.replaceAll("CLICKTAG","clicktag");
                       vars = vars.replaceAll("ClickTag","clicktag");
                       vars = vars.replaceAll("clickTAG","clicktag");
                       vars = vars.replaceAll("ClickTAG","clicktag");
                       vars = vars.replaceAll("Clicktag","clicktag");
                       String[] varsArray = vars.split(Pattern.quote("clicktag"));
 
                       if ( varsArray.length > 1 ) {
                        for ( int k=1; k<varsArray.length; k++ ) {
 			  String[] varPieces = varsArray[k].split(Pattern.quote("="));
                           String clickTagUrl = varPieces[1].trim();
                           clickTagUrl = clickTagUrl.replace("http%3a//", "http://");
                           clickTagUrl = clickTagUrl.replace("http%3A//", "http://");
                           clickTagUrl = clickTagUrl.replace("HTTP%3A//", "http://");
                           clickTagUrl = clickTagUrl.replace("HTTP%3a//", "http://");
                           clickTagUrl = clickTagUrl.replace("http%3A%2F%2F", "http://");
                           clickTagUrl = clickTagUrl.replace("http%3a%2a%2f", "http://");
                           clickTagUrl = rtrim(clickTagUrl);
                           clickTagUrls.add( clickTagUrl );
                           clickTagEmbedIdx.add(new Integer(j));
 
                           //http splits                          
                           clickTagUrl = clickTagUrl.replaceAll("http%3a//","http://");
                           clickTagUrl = clickTagUrl.replaceAll("http%3A//","http://");
                           clickTagUrl = clickTagUrl.replaceAll("HTTP%3A//","http://");
                           clickTagUrl = clickTagUrl.replaceAll("HTTP%3a//","http://");
 
                           String[] httpPieces = clickTagUrl.split("http://");
                           // System.out.println( "Last Piece = " + httpPieces[httpPieces.length-1] );
                           
                           //System.out.println( "clickTagUrl = " + clickTagUrl );
                           break;
                        }
                       }
                       break;                
                   }
                } catch ( SeleniumException se ) { se.printStackTrace(); }
 	      }
             }
           }
 
           for ( int i=0; i<clickTagUrls.size(); i++ ) {
 	      // System.out.println( "opening landing page, " + clickTagUrls.get(i) );
               int idx = clickTagEmbedIdx.get(i).intValue();
               try {
 	        selena.getEval( "var link = window.document.createElement('a'); link.setAttribute('href','"+clickTagUrls.get(i)+"'); link.setAttribute('target','blank'); var bodyItems = window.document.getElementsByTagName('body'); var body = bodyItems.item(0); body.appendChild(link); link.click();" );
               } catch ( SeleniumException se ) { se.printStackTrace(); }
 
               try {
                  Thread.sleep( 5000 );
               } catch ( InterruptedException ie ) { ie.printStackTrace(); }       
    
               Iterator iter2 = null;
               try {
                  iter2 = selena.getWrappedDriver().getWindowHandles().iterator();
               } catch ( SeleniumException se ) { se.printStackTrace(); }
 
                  while ( iter2.hasNext()) {
                       String windowName = (String)iter2.next();
 		      if ( !windowName.equals( winName ) ) {
 			 try {
 			   selena.selectWindow( windowName );
                          } catch ( SeleniumException se ) { se.printStackTrace(); }
                        
                          try {
                               String landingPageUrl = selena.getLocation().trim();
                               System.out.println("INSERT INTO landing_page_urls ( ad_url, file_name, content_type, landing_page_url ) VALUES ( '" + ((String)adUrls.get(i)).trim() + "', '" + ((String)fileNames.get(i)).trim() + "', '" + ((String)contentTypes.get(i)).trim() + "', '" + landingPageUrl + "' )");
                              
                               stmt.executeUpdate("INSERT INTO landing_page_urls ( ad_url, file_name, content_type, landing_page_url ) VALUES ( '" + ((String)adUrls.get(idx)).trim() + "', '" + ((String)fileNames.get(idx)).trim() + "', '" + ((String)contentTypes.get(idx)).trim() + "', '" + landingPageUrl + "' )");                             
                          } catch ( SeleniumException se ) { se.printStackTrace(); }
 
                          try {
                            selena.close();
                          } catch ( SeleniumException se ) { se.printStackTrace(); }
                            break;
                       }
 		 }
 
                  try {
                    selena.selectWindow( winName );
                  } catch ( SeleniumException se ) { se.printStackTrace(); }
 	  }
 
           //Process iframes.
           ArrayList<String> iframeSrcList = new ArrayList<String>();
           for ( int i=0; i<iframeCount; i++ ) {
             try {
 	      iframeSrcList.add( selena.getEval( "window.document.getElementById('iframe"+Integer.toString(i)+"').getAttribute('src');" ).trim() );
             } catch ( SeleniumException se ) { se.printStackTrace(); }
           }
          
           //here with select frames
           int iFrameCount = 0;
           try {
 	      iFrameCount =  Integer.parseInt( selena.getEval("window.frames.length") );
               System.out.println( "IFRAME COUNT = " + iFrameCount );
           } catch ( SeleniumException se ) { se.printStackTrace(); }
 
           for ( int i=0; i<iFrameCount; i++ ) {
               selena.selectFrame( "relative=top" );  
               selena.getWrappedDriver().switchTo().frame( i );
               //System.out.println( "Got frame"+Integer.toString(i) + " = " + selena.getLocation() );
               processFrameII( i, winName );
           }
           
           System.out.println( "Processed all of the frames" );
 
          try {
             selena.close();
          } catch ( SeleniumException se ) { se.printStackTrace(); }
       } catch ( SQLException sqle ) { sqle.printStackTrace(); }
     }
 
     public static void processFrameII( int frameIdx, String referringWindow ) {
         //check for blank iframe.
         if ( selena.getLocation().trim().equals("") || selena.getLocation().trim().equals("about:blank")  ) {
 	    return;
         }
 
        System.out.println( "Processing the frame" );
 
         //Do clicks on images and embeds within the frame.
         int frameImgCount = 0;
         try { 
           frameImgCount = Integer.parseInt( selena.getEval( "var imgList = window.document.getElementsByTagName('img'); for ( var i=0; i<imgList.length; i++ ) { imgList.item(i).setAttribute('id','img'+i); } imgList.length;" ).trim() );
         } catch ( SeleniumException se ) { se.printStackTrace(); }
         //System.out.println ( "frame image count = " + frameImgCount );
 
         int frameEmbedCount = 0;
         try {
            frameEmbedCount = Integer.parseInt( selena.getEval( "var embedList = window.document.getElementsByTagName('embed'); for ( var i=0; i<embedList.length; i++ ) { embedList.item(i).setAttribute('id','embed'+i); } embedList.length;").trim() );
         } catch ( SeleniumException se ) { se.printStackTrace(); }
         //System.out.println( "frame embed count = " + frameEmbedCount );
        
 	int frameIframeCount = 0;
         try { 
            frameIframeCount = Integer.parseInt( selena.getEval( "var iframeList = window.document.getElementsByTagName('iframe'); for ( var i=0; i<iframeList.length; i++ ) { iframeList.item(i).setAttribute('id','iframe'+i); } iframeList.length;" ).trim() );
         } catch ( SeleniumException se ) { se.printStackTrace(); }
         //System.out.println( "frame iframe count = " + frameIframeCount );
 
         //Process Images in the frame.
         // System.out.println( "Processing frame images." );
         for ( int i=0; i<frameImgCount; i++ ) {
 	    String imgId = "img"+Integer.toString(i);
             String imgSrc =  "";
 	    try { 
                 imgSrc = selena.getEval("window.document.getElementById('"+imgId+"').getAttribute('src');");
             } catch ( SeleniumException se ) { se.printStackTrace(); }
 
             String[] imgPieces = imgSrc.trim().split("/");
             String imgLastPiece = imgPieces[imgPieces.length-1];
             for ( int j=0; j<contentTypes.size(); j++ ) {
 	       try {
                    if ( contentTypes.get(j).startsWith("image/png") || contentTypes.get(j).startsWith("image/jpeg") || contentTypes.get(j).startsWith("image/gif") ) {
                     String[] pieces = adUrls.get(j).trim().split("/");
                     if ( pieces[pieces.length-1].equals( imgLastPiece ) ) {
 			// System.out.println( pieces[pieces.length-1] + ", " +  imgLastPiece );
                         try {
 			    selena.getEval("window.document.getElementById('"+imgId+"').parentNode.setAttribute('target','blank'); window.document.getElementById('"+imgId+"').parentNode.parentNode.setAttribute('target','blank'); window.document.getElementById('"+imgId+"').click();");
                         } catch ( SeleniumException se ) { se.printStackTrace(); }
                         try {
                             Thread.sleep( 3000 );
                        } catch ( InterruptedException ie ) { ie.printStackTrace(); }       
                                
                         Iterator iter2 = null;
                         try { 
                            iter2 = selena.getWrappedDriver().getWindowHandles().iterator();
                         } catch ( SeleniumException se ) { se.printStackTrace(); }
 
                         while ( iter2.hasNext()) {
                             String windowName = (String)iter2.next();
 			    if ( !windowName.equals( winName ) ) {
                                 try {
 				  selena.selectWindow( windowName );
                                 } catch ( SeleniumException se ) { se.printStackTrace(); }
                                 
                                 try {
                                    String landingPageUrl = selena.getLocation().trim();
                                    System.out.println("INSERT INTO landing_page_urls ( ad_url, file_name, content_type, landing_page_url ) VALUES ( '" + ((String)adUrls.get(j)).trim() + "', '" + ((String)fileNames.get(j)).trim() + "', '" + ((String)contentTypes.get(j)).trim() + "', '" + landingPageUrl + "' )");
 
                                    stmt.executeUpdate("INSERT INTO landing_page_urls ( ad_url, file_name, content_type, landing_page_url ) VALUES ( '" + ((String)adUrls.get(j)).trim() + "', '" + ((String)fileNames.get(j)).trim() + "', '" + ((String)contentTypes.get(j)).trim() + "', '" + landingPageUrl + "' )");
                                 } catch ( SeleniumException se ) { se.printStackTrace(); }
                                 catch ( SQLException sqle ) { sqle.printStackTrace(); }
                                 
                                 try {
                                   Thread.sleep( 3000 );
                                 } catch ( InterruptedException ie ) { ie.printStackTrace(); }       
                                 try {
                                   selena.goBack();
                                 } catch ( SeleniumException se )  { se.printStackTrace(); }
                                 try {
                                   selena.selectWindow( referringWindow );
                                   selena.getWrappedDriver().switchTo().frame( frameIdx );
                                 } catch ( SeleniumException se ) { se.printStackTrace(); }
                                 break;
                             }
                         }
                         break;
                     }
 		   }
 		} catch ( SeleniumException se ) { se.printStackTrace(); }
 	   }
 	}
 
         //Process embeds to get and resolve clicktag urls.
         System.out.println( "Processing frame embeds." );
         ArrayList<String> clickTagUrls = new ArrayList<String>();
         ArrayList<Integer> clickTagEmbedIdx = new ArrayList<Integer>();
 
         for ( int i=0; i<frameEmbedCount; i++ ) {
                String embedId = "embed"+Integer.toString(i);
                String embedSrc = "";
 	       try { 
                  embedSrc =  selena.getEval("window.document.getElementById('"+embedId+"').getAttribute('src');");
 	       } catch ( SeleniumException se ) { se.printStackTrace(); }
 
                String[] embedPieces = embedSrc.trim().split("/");
                String embedLastPiece = embedPieces[embedPieces.length-1];
 	       // System.out.println( "embed last piece = " + embedLastPiece );
                for ( int j=0; j<contentTypes.size(); j++ ) {
 	         if ( contentTypes.get(j).startsWith("application/x-shockwave-flash") ) {
 	           try {
                       String[] pieces = adUrls.get(j).trim().split("/");
                       if ( pieces[pieces.length-1].equals( embedLastPiece ) ) {
 		        System.out.println( "frame embed found: " + pieces[pieces.length-1] + ", " + embedLastPiece );
                         String flashVarsAttribute = "";
                         try {
                             flashVarsAttribute =  selena.getEval("window.document.getElementById('"+embedId+"').getAttribute('flashvars');").trim();
 		        }  catch ( SeleniumException se ) { se.printStackTrace(); }
                         
                         String vars = embedSrc + ";" + flashVarsAttribute;
                         vars = vars.replaceAll("clickTag","clicktag");
                         vars = vars.replaceAll("CLICKTAG","clicktag");
                         vars = vars.replaceAll("ClickTag","clicktag");
                         vars = vars.replaceAll("clickTAG","clicktag");
                         vars = vars.replaceAll("ClickTAG","clicktag");
                         vars = vars.replaceAll("Clicktag","clicktag");
                         String[] varsArray = vars.split(Pattern.quote("clicktag"));
                      
                         if ( varsArray.length > 1 ) {
                          for ( int k=1; k<varsArray.length; k++ ) {
 			   String[] varPieces = varsArray[k].split(Pattern.quote("="));
                            String clickTagUrl = varPieces[1].trim();
 			   clickTagUrl = clickTagUrl.replace("http%3a//","http://");
                            clickTagUrl = clickTagUrl.replace("http%3A//","http://");
                            clickTagUrl = clickTagUrl.replace("HTTP%3A//","http://");
                            clickTagUrl = clickTagUrl.replace("HTTP%3a//","http://");
                            clickTagUrl = rtrim(clickTagUrl);
 			   clickTagUrls.add( clickTagUrl );                           
                            clickTagEmbedIdx.add( new Integer(j) );
                            clickTagUrl = clickTagUrl.replaceAll("http%3a//","http://");
                            clickTagUrl = clickTagUrl.replaceAll("http%3A//","http://");
                            clickTagUrl = clickTagUrl.replaceAll("HTTP%3A//","http://");
                            clickTagUrl = clickTagUrl.replaceAll("HTTP%3a//","http://");
 
                            System.out.println( "clickTagUrl = " + clickTagUrl );
                            String[] httpPieces = clickTagUrl.split("http://");
                            break;
 			 }
 			}
                         break;
                       }
       	           } catch ( SeleniumException se ) { se.printStackTrace(); } 
                  }
                }
 	 }
 
           for ( int i=0; i<clickTagUrls.size(); i++ ) {
              // System.out.println( "opening frame landing page, " + clickTagUrls.get(i) );
              int idx = clickTagEmbedIdx.get(i).intValue();
 
              try {
 	       selena.getEval( "var link = window.document.createElement('a'); link.setAttribute('href','"+clickTagUrls.get(i)+"'); link.setAttribute('target','blank'); var bodyItems = window.document.getElementsByTagName('body'); var body = bodyItems.item(0); body.appendChild(link); link.click();" );
 	     }  catch ( SeleniumException se ) { se.printStackTrace(); }
 
               try {
                    Thread.sleep( 4000 );
               } catch ( InterruptedException ie ) { ie.printStackTrace(); }                                 
 	         Iterator iter2 = null;
                  try {
                    iter2 = selena.getWrappedDriver().getWindowHandles().iterator();
                  } catch ( SeleniumException se ) { se.printStackTrace(); }
 
                  while ( iter2.hasNext()) {
                       String windowName = (String)iter2.next();
 		      if ( !windowName.equals( winName ) ) {
 			  try {
 			    selena.selectWindow( windowName );
 			  } catch ( SeleniumException se ) { se.printStackTrace(); }
       
                           try {
                            String landingPageUrl = selena.getLocation().trim();
                            System.out.println("INSERT INTO landing_page_urls ( ad_url, file_name, content_type, landing_page_url ) VALUES ( '" + ((String)adUrls.get(idx)).trim() + "', '" + ((String)fileNames.get(idx)).trim() + "', '" + ((String)contentTypes.get(idx)).trim() + "', '" + landingPageUrl + "' )");
                            stmt.executeUpdate("INSERT INTO landing_page_urls ( ad_url, file_name, content_type, landing_page_url ) VALUES ( '" + ((String)adUrls.get(idx)).trim() + "', '" + ((String)fileNames.get(idx)).trim() + "', '" + ((String)contentTypes.get(idx)).trim() + "', '" + landingPageUrl + "' )");
                          } catch ( SeleniumException se ) { se.printStackTrace(); }
 			  catch ( SQLException sqle ) { sqle.printStackTrace(); }
 			  try {
                            selena.goBack();
                           } catch ( SeleniumException se ) { se.printStackTrace(); }
                           try {
 			      selena.selectWindow( referringWindow );
                               selena.getWrappedDriver().switchTo().frame( frameIdx );
                           } catch ( SeleniumException se ) { se.printStackTrace(); }
                           break;
                       }
 		 }
                  break; 
 	  }
 
           //here with select frames
           int iFrameCount = 0;
           try {
 	      iFrameCount =  Integer.parseInt( selena.getEval("window.frames.length") );
               System.out.println( "FRAME IFRAME COUNT = " + iFrameCount );
           } catch ( SeleniumException se ) { se.printStackTrace(); }
 
           try {
                selena.selectWindow( referringWindow );
 	  } catch ( SeleniumException se ) { se.printStackTrace(); }
          
           for ( int i=0; i<iFrameCount; i++ ) {
 	    try {
 		selena.getWrappedDriver().switchTo().frame(frameIdx).switchTo().frame( i );
             } catch ( SeleniumException se ) { se.printStackTrace(); }
             try {
               System.out.println( "Got frame"+Integer.toString(i) + " = " + selena.getLocation() );
             } catch ( SeleniumException se ) { se.printStackTrace(); }
             processFrameII( i, referringWindow );
             try {
               selena.selectFrame( "relative=top" );
             } catch ( SeleniumException se ) { se.printStackTrace(); }  
           }
 
         System.out.println( "----------------------------------------" );
     }
 
     public static void processFrame( String _url, String referringWindow ) {
         try {
           selena.selectWindow( referringWindow );
         } catch ( SeleniumException se ) { se.printStackTrace(); }
 
         //check for blank iframe.
         if ( _url.trim().equals("") || _url.trim().equals("about:blank")  ) {
 	    return;
         }
 
         //Open the frame by adding a link to the body with target=blank and clicking it.
         try {
 	    // System.out.println( "opening the frame" );
             selena.getEval( "var link = window.document.createElement('a'); link.setAttribute('href','"+_url+"'); link.setAttribute('target','blank'); var bodyItems = window.document.getElementsByTagName('body'); var body = bodyItems.item(0); body.appendChild(link); link.click();" );
         } catch ( SeleniumException se ) { se.printStackTrace(); }
 
         try {
            Thread.sleep( 3000 );
         } catch ( InterruptedException ie ) { ie.printStackTrace(); }       
                        
         Iterator iter1 = null;
         try {
           iter1 = selena.getWrappedDriver().getWindowHandles().iterator();
         } catch ( SeleniumException se ) { se.printStackTrace(); }       
 
         while ( iter1.hasNext()) {
          String windowName1 = (String)iter1.next();
 	 if ( !windowName1.equals( winName ) ) {
 	    try {
 	      selena.selectWindow( windowName1 );
             } catch ( SeleniumException se ) { se.printStackTrace(); }       
 
 	    //Do clicks on images and embeds within the frame.
             int frameImgCount = 0;
             try { 
                frameImgCount = Integer.parseInt( selena.getEval( "var imgList = window.document.getElementsByTagName('img'); for ( var i=0; i<imgList.length; i++ ) { imgList.item(i).setAttribute('id','img'+i); } imgList.length;" ).trim() );
             } catch ( SeleniumException se ) { se.printStackTrace(); }
             // System.out.println ( "frame image count = " + frameImgCount );
           
 	    int frameEmbedCount = 0;
             try {
                frameEmbedCount = Integer.parseInt( selena.getEval( "var embedList = window.document.getElementsByTagName('embed'); for ( var i=0; i<embedList.length; i++ ) { embedList.item(i).setAttribute('id','embed'+i); } embedList.length;").trim() );
             } catch ( SeleniumException se ) { se.printStackTrace(); }
             // System.out.println( "frame embed count = " + frameEmbedCount );
        
 	    int frameIframeCount = 0;
             try { 
                frameIframeCount = Integer.parseInt( selena.getEval( "var iframeList = window.document.getElementsByTagName('iframe'); for ( var i=0; i<iframeList.length; i++ ) { iframeList.item(i).setAttribute('id','iframe'+i); } iframeList.length;" ).trim() );
             } catch ( SeleniumException se ) { se.printStackTrace(); }
             // System.out.println( "frame iframe count = " + frameIframeCount );
               
              //Process Images in the frame.
              // System.out.println( "Processing frame images." );
              for ( int i=0; i<frameImgCount; i++ ) {
 	      String imgId = "img"+Integer.toString(i);
               String imgSrc =  "";
 	      try { 
                 imgSrc = selena.getEval("window.document.getElementById('"+imgId+"').getAttribute('src');");
               } catch ( SeleniumException se ) { se.printStackTrace(); }
 
               String[] imgPieces = imgSrc.trim().split("/");
               String imgLastPiece = imgPieces[imgPieces.length-1];
               for ( int j=0; j<contentTypes.size(); j++ ) {
 		try {
                    if ( contentTypes.get(j).startsWith("image/png") || contentTypes.get(j).startsWith("image/jpeg") || contentTypes.get(j).startsWith("image/gif") ) {
                     String[] pieces = adUrls.get(j).trim().split("/");
                     if ( pieces[pieces.length-1].equals( imgLastPiece ) ) {
 			// System.out.println( pieces[pieces.length-1] + ", " +  imgLastPiece );
                         try {
 			    selena.getEval("window.document.getElementById('"+imgId+"').parentNode.setAttribute('target','blank'); window.document.getElementById('"+imgId+"').parentNode.parentNode.setAttribute('target','blank'); window.document.getElementById('"+imgId+"').click();");
                         } catch ( SeleniumException se ) { se.printStackTrace(); }
                         try {
                             Thread.sleep( 3000 );
                         } catch ( InterruptedException ie ) { ie.printStackTrace(); }       
                                
                         Iterator iter2 = null;
                         try { 
                            iter2 = selena.getWrappedDriver().getWindowHandles().iterator();
                         } catch ( SeleniumException se ) { se.printStackTrace(); }
 
                         while ( iter2.hasNext()) {
                             String windowName = (String)iter2.next();
 			    if ( !windowName.equals( winName ) ) {
                                 try {
 				  selena.selectWindow( windowName );
                                 } catch ( SeleniumException se ) { se.printStackTrace(); }
                                 
                                 try {
                                    String landingPageUrl = selena.getLocation().trim();
                                    System.out.println("INSERT INTO landing_page_urls ( ad_url, file_name, content_type, landing_page_url ) VALUES ( '" + ((String)adUrls.get(j)).trim() + "', '" + ((String)fileNames.get(j)).trim() + "', '" + ((String)contentTypes.get(j)).trim() + "', '" + landingPageUrl + "' )");
 
                                    stmt.executeUpdate("INSERT INTO landing_page_urls ( ad_url, file_name, content_type, landing_page_url ) VALUES ( '" + ((String)adUrls.get(j)).trim() + "', '" + ((String)fileNames.get(j)).trim() + "', '" + ((String)contentTypes.get(j)).trim() + "', '" + landingPageUrl + "' )");
                                 } catch ( SeleniumException se ) { se.printStackTrace(); }
                                 catch ( SQLException sqle ) { sqle.printStackTrace(); }
                                 
                                 try {
                                   Thread.sleep( 3000 );
                                 } catch ( InterruptedException ie ) { ie.printStackTrace(); }       
                                 try {
                                   selena.goBack();
                                 } catch ( SeleniumException se )  { se.printStackTrace(); }
                                 try {
                                   selena.selectWindow( windowName1 );
                                 } catch ( SeleniumException se ) { se.printStackTrace(); }
                                 break;
                             }
                         }
                         break;
                     }
 		   }
 		} catch ( SeleniumException se ) { se.printStackTrace(); }
 	      }
 	     }
 
              try {
                selena.selectWindow( windowName1 );
              } catch ( SeleniumException se ) { se.printStackTrace(); }
 
              //Process embeds to get and resolve clicktag urls.
              System.out.println( "Processing frame embeds." );
              ArrayList<String> clickTagUrls = new ArrayList<String>();
              ArrayList<Integer> clickTagEmbedIdx = new ArrayList<Integer>();
 
              for ( int i=0; i<frameEmbedCount; i++ ) {
                String embedId = "embed"+Integer.toString(i);
 
                String embedSrc = "";
 	       try { 
                  embedSrc =  selena.getEval("window.document.getElementById('"+embedId+"').getAttribute('src');");
 	       } catch ( SeleniumException se ) { se.printStackTrace(); }
 
                String[] embedPieces = embedSrc.trim().split("/");
                String embedLastPiece = embedPieces[embedPieces.length-1];
 	       // System.out.println( "embed last piece = " + embedLastPiece );
                for ( int j=0; j<contentTypes.size(); j++ ) {
 	         if ( contentTypes.get(j).startsWith("application/x-shockwave-flash") ) {
 	           try {
                       String[] pieces = adUrls.get(j).trim().split("/");
                       if ( pieces[pieces.length-1].equals( embedLastPiece ) ) {
 		        System.out.println( "frame embed found: " + pieces[pieces.length-1] + ", " + embedLastPiece );
                         String flashVarsAttribute = "";
                         try {
                             flashVarsAttribute =  selena.getEval("window.document.getElementById('"+embedId+"').getAttribute('flashvars');").trim();
 		        }  catch ( SeleniumException se ) { se.printStackTrace(); }
                         
                         String vars = embedSrc + ";" + flashVarsAttribute;
                         vars = vars.replaceAll("clickTag","clicktag");
                         vars = vars.replaceAll("CLICKTAG","clicktag");
                         vars = vars.replaceAll("ClickTag","clicktag");
                         vars = vars.replaceAll("clickTAG","clicktag");
                         vars = vars.replaceAll("ClickTAG","clicktag");
                         vars = vars.replaceAll("Clicktag","clicktag");
                         String[] varsArray = vars.split(Pattern.quote("clicktag"));
                      
                         if ( varsArray.length > 1 ) {
                          for ( int k=1; k<varsArray.length; k++ ) {
 			   String[] varPieces = varsArray[k].split(Pattern.quote("="));
                            String clickTagUrl = varPieces[1].trim();
 			   clickTagUrl = clickTagUrl.replace("http%3a//","http://");
                            clickTagUrl = clickTagUrl.replace("http%3A//","http://");
                            clickTagUrl = clickTagUrl.replace("HTTP%3A//","http://");
                            clickTagUrl = clickTagUrl.replace("HTTP%3a//","http://");
                            clickTagUrl = rtrim(clickTagUrl);
 			   clickTagUrls.add( clickTagUrl );                           
                            clickTagEmbedIdx.add( new Integer(j) );
 
                            clickTagUrl = clickTagUrl.replaceAll("http%3a//","http://");
                            clickTagUrl = clickTagUrl.replaceAll("http%3A//","http://");
                            clickTagUrl = clickTagUrl.replaceAll("HTTP%3A//","http://");
                            clickTagUrl = clickTagUrl.replaceAll("HTTP%3a//","http://");
 
                            String[] httpPieces = clickTagUrl.split("http://");
                            // System.out.println( "Last Piece = " + httpPieces[httpPieces.length-1] );
                          
                            // System.out.println( "frame clickTagUrl = " + clickTagUrl );
                            break;
 			 }
 			}
                         break;
                       }
       	           } catch ( SeleniumException se ) { se.printStackTrace(); } 
                  }
                }
 	     }
              
              for ( int i=0; i<clickTagUrls.size(); i++ ) {
          	 // System.out.println( "opening frame landing page, " + clickTagUrls.get(i) );
               int idx = clickTagEmbedIdx.get(i).intValue();
 
              try {
 	       selena.getEval( "var link = window.document.createElement('a'); link.setAttribute('href','"+clickTagUrls.get(i)+"'); link.setAttribute('target','blank'); var bodyItems = window.document.getElementsByTagName('body'); var body = bodyItems.item(0); body.appendChild(link); link.click();" );
 	     }  catch ( SeleniumException se ) { se.printStackTrace(); }
 
               try {
                    Thread.sleep( 5000 );
               } catch ( InterruptedException ie ) { ie.printStackTrace(); }                                 
 	         Iterator iter2 = null;
                  try {
                    iter2 = selena.getWrappedDriver().getWindowHandles().iterator();
                  } catch ( SeleniumException se ) { se.printStackTrace(); }
 
                  while ( iter2.hasNext()) {
                       String windowName = (String)iter2.next();
 		      if ( !windowName.equals( winName ) ) {
 			  try {
 			    selena.selectWindow( windowName );
 			  } catch ( SeleniumException se ) { se.printStackTrace(); }
       
                           try {
                            String landingPageUrl = selena.getLocation().trim();
                            System.out.println("INSERT INTO landing_page_urls ( ad_url, file_name, content_type, landing_page_url ) VALUES ( '" + ((String)adUrls.get(idx)).trim() + "', '" + ((String)fileNames.get(idx)).trim() + "', '" + ((String)contentTypes.get(idx)).trim() + "', '" + landingPageUrl + "' )");
                            stmt.executeUpdate("INSERT INTO landing_page_urls ( ad_url, file_name, content_type, landing_page_url ) VALUES ( '" + ((String)adUrls.get(idx)).trim() + "', '" + ((String)fileNames.get(idx)).trim() + "', '" + ((String)contentTypes.get(idx)).trim() + "', '" + landingPageUrl + "' )");
                          } catch ( SeleniumException se ) { se.printStackTrace(); }
 			  catch ( SQLException sqle ) { sqle.printStackTrace(); }
 
 			  try {
                            selena.goBack();
                           } catch ( SeleniumException se ) { se.printStackTrace(); }
                           try {
                            selena.selectWindow( windowName1 ); 
                           } catch ( SeleniumException se ) { se.printStackTrace(); }
                           break;
                       }
 		 }
                  break; 
 	     }
             
 	     // System.out.println( "Processing frame iframes." );
              ArrayList<String> iframeSrcList = new ArrayList<String>();
              for ( int i=0; i<frameIframeCount; i++ ) {
 	       try {
 	          iframeSrcList.add( selena.getEval( "window.document.getElementById('iframe"+Integer.toString(i)+"').getAttribute('src');" ).trim() );
 	       }  catch ( SeleniumException se ) { se.printStackTrace(); }
              } 
 
              int frameCount = 0;
              for ( String iframeSrc : iframeSrcList ) {
                System.out.println( "Getting frame iframe = " + iframeSrc + ", frameNumber = " + frameCount );
                processFrame( iframeSrc, windowName1 );
                frameCount++;
 	     }
 
              try {
                selena.goBack();
              } catch ( SeleniumException se ) { se.printStackTrace(); }
              // System.out.println( "closing frame." );
              try {
                selena.close();
              } catch ( SeleniumException se ) { se.printStackTrace(); }
              try {
                selena.selectWindow( referringWindow );
              } catch ( SeleniumException se ) { se.printStackTrace(); }
              // System.out.println( "Returning to referring window." );
          }
         } 
     }
 
     public static void killFirefoxProcess() {
       try {
        selena.close();
       }  catch ( SeleniumException se ) { se.printStackTrace(); } 
       try {
        System.out.println( "Killing Firefox." );
        Process p = Runtime.getRuntime().exec( "/usr/bin/killall firefox-bin" );
        p.waitFor();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s;
        while ((s=r.readLine())!=null) {}
        r.close();
       } catch ( IOException ioe ) { ioe.printStackTrace(); }
       catch ( NullPointerException npe ) { npe.printStackTrace(); }
       catch ( InterruptedException ie ) { ie.printStackTrace(); }
     }
 
     public static void stopServer() {
       try {
          System.out.println( "Stopping proxy server." );
          server.stop();
       } catch ( Exception e ) { e.printStackTrace(); }
     }
 
     public static String rtrim(String str) {
      int len = str.length();
      while(str.charAt(len-1) == '&') {
        str = str.substring(0,len-1);
        len--;
      }
      return str;    
     }
 
     public static void showResults() {
      BufferedWriter csvWriter = null;
      try {
 	 csvWriter = new BufferedWriter( new FileWriter("./AdMonitorOutput.csv") );
          csvWriter.write( "\"Ad Url\",\"File Name\",\"Content Type\",\"Landing Page URL\"\n" );
      } catch ( IOException ioe ) { ioe.printStackTrace(); }
      try {
       ResultSet localResultSet1 = stmt.executeQuery("SELECT COUNT(*) FROM ad_urls AS t1 LEFT OUTER JOIN landing_page_urls AS t2 ON t2.ad_url=t1.url_string");
       localResultSet1.next();
       System.out.println("count = " + localResultSet1.getString(1));
       localResultSet1.close();
       ResultSet localResultSet2 = stmt.executeQuery("SELECT DISTINCT t1.*, t2.landing_page_url FROM ad_urls AS t1 LEFT OUTER JOIN landing_page_urls AS t2 ON ( t2.ad_url=t1.url_string ) ORDER BY t1.file_name ASC");
       while (localResultSet2.next())
         try {
           System.out.println(localResultSet2.getString(1).trim() + ", " + localResultSet2.getString(2) + ", " + localResultSet2.getString(3) + ", " + localResultSet2.getString(4));
           csvWriter.write( "\"" + localResultSet2.getString(1).trim() + "\",\"" + localResultSet2.getString(2) + "\",\"" + localResultSet2.getString(3) + "\",\"" + localResultSet2.getString(4) + "\"\n" );
         }
         catch (NullPointerException localNullPointerException) {
           localNullPointerException.printStackTrace();
         }
         catch ( IOException ioe ) { ioe.printStackTrace(); }
        localResultSet2.close();
     }
     catch (SQLException localSQLException) {
       localSQLException.printStackTrace();
     }
     try {
 	csvWriter.close();
     } catch ( IOException ioe ) { ioe.printStackTrace(); }
   }
 
   public static void killJavaProcesses() {
     try {
        System.out.println( "Killing Java processes." );
        Process p = Runtime.getRuntime().exec( "/usr/bin/killall java" );
        p.waitFor();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String s;
        while ((s=r.readLine())!=null) {}
        r.close();
      } catch ( IOException ioe ) { ioe.printStackTrace(); }
      catch ( NullPointerException npe ) { npe.printStackTrace(); }
      catch ( InterruptedException ie ) { ie.printStackTrace(); }
   } 
 } 
