 /*
  * The HRT Project.
  * Aavailable under a Creative Commons 2.0 License.
  */
 package org.hrva.capture;
 
 import com.fourspaces.couchdb.Database;
 import com.fourspaces.couchdb.Document;
 import com.fourspaces.couchdb.Session;
 import java.io.*;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import org.apache.commons.logging.Log; 
 import org.apache.commons.logging.LogFactory; 
 
 /**
  * Push HRT Feeds or Mappings to the CouchDB.
  * 
  * <p>This is both a main program with a command-line interface, 
  * as well as object that can  be used to push.
  * </p>
  * 
  * <p>Typical use case</p>
  * <code><pre>
  *     CouchPush cp = new CouchPush();
  *     cp.open( "http://localhost:5984/database" );
  *     cp.push_feed( "some_feed_file.csv" );
  *     System.out.print( "Created "+cp.id );
  * </pre></code>
  * 
  * <p>When run as a main program, this uses a properties
  * file and command-line arguments to determine the 
  * server, database, documents and attachments.
  * </p>
  * 
  * <p>At the command line, it might look like this.</p>
  * <code><pre>
  * java -cp LogCapture/dist/LogCapture.jar org.hrva.capture.CouchPush -m route -e 2012-03-12 route.csv
  * java -cp LogCapture/dist/LogCapture.jar org.hrva.capture.CouchPush -m stop -e 2012-03-12 stop.csv
  * java -cp LogCapture/dist/LogCapture.jar org.hrva.capture.CouchPush -m vehicle -e 2012-03-12 vehicle.csv
  * </pre></code>
  * 
  * <p>Depends on a <tt>hrtail.properties</tt> properties file.</p>
  * <dl>
  * <dt>couchpush.db_url</dt><dd>The database URL to use</dd>
  * </dl>
  * 
  * <p>Uses <a href="https://github.com/mbreese/couchdb4j">CouchDB4J</a></p>
  *
  * <p>Uses <a href="http://args4j.kohsuke.org/">Args4J</a></p>
  *
  * <p>Also relevant are these projects</p>
  *
  * <p>org.apache.commons.httpclient from <a
  * href="http://hc.apache.org/index.html">hc.apache.org</a></p>
  *
  * <p>org.json.simple from <a
  * href="http://code.google.com/p/json-simple/">code.google.com</a></p>
  *
  * @author slott
  */
 public class CouchPush {
     
     /** Properties for this application. */
     Properties global= new Properties();               
 
     // Use GMT for timestamps to avoid EST/EDT problems.
     private TimeZone zulu = TimeZone.getTimeZone("GMT");
 
     // Couchdb Session and Database
     private Session s;
     private Database db;
     
     // Used to format timestamps in GMT.
     private DateFormat fmt_date_time;
     
     /** For feeds, simply provide -f option. */
     @Option(name = "-f", usage = "Feed")
     boolean feed= false;
     
     /** For mappings, provide -m <i>type</i>. */
     @Option(name = "-m", usage = "Mapping Type (one of vehicle, route, stop)")
     String mapping = null;
     
     /** For mappings, provide -e <i>yyyy-mm-dd</i> effective date. */
     @Option(name = "-e", usage = "Effective Date yyyy-mm-dd")
     String effective = null;
     
     /** Verbose debugging. */
     @Option(name = "-v", usage = "Vebose logging")
     boolean verbose= false;
 
     /** Command-line Arguments. */
     @Argument
     List<String> arguments = new ArrayList<String>();
 
     /** Used to parse dates. */
     DateFormat fmt_date = new SimpleDateFormat("yyyy-MM-dd");
 
     /** Logger. */
     final Log  logger = LogFactory.getLog(CouchPush.class);
 
     /**
      * Command-line program to push a file to the HRT couch DB.
      * <p>All this does is read properties and invoke run_main</p>
      * @param args arguments
      */
     public static void main(String[] args) {
         Log log = LogFactory.getLog(CouchPush.class);
         File prop_file = new File("hrtail.properties");
         Properties config = new Properties();
         try {
             config.load(new FileInputStream(prop_file));
         } catch (IOException ex) {
             log.warn( "Can't find "+prop_file.getName(), ex );
             try {
                 log.debug(prop_file.getCanonicalPath());
             } catch (IOException ex1) {
             }
         }
         CouchPush cp = new CouchPush( config);
         try {
             cp.run_main(args);
         } catch (CmdLineException ex1) {
             log.fatal("Invalid Options", ex1);
         } catch (MalformedURLException ex2) {
             log.fatal("Invalid CouchDB URL", ex2);
         } catch (IOException ex3) {
             log.fatal(ex3);
         }
     }
     
     /**
      * Build the CouchPush instance.  
      * 
      * @param global The hrtail.properties file
      */
     public CouchPush( Properties global ) {
         super();
         this.global= global;
         // Force the timzone code to be simply "Z".  This only works
         // because the format really IS forced to GMT.
         fmt_date_time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         fmt_date_time.setTimeZone( zulu );
     }
 
     /**
      * Main program to parse arguments and push a feed or mapping file.
      * 
      * <p>Parses command-line arguments.  Builds document, posts
      * document and puts attachment.
      * </p>
      * 
      * <p>Accepts the following options</p>
      * <dl>
      * <dt><tt>-f</tt></dt><dd>This is a feed.</dd>
      * <dt><tt>-m <i>type</i></tt></dt><dd>This is a mapping of type vehicle, route, stop.</dd>
      * <dt><tt>-e <i>yyyy-mm-d</i></tt></dt><dd>The effective date of this mapping.</dd>
      * <dt><tt>-v</tt></dt><dd>Verbose logging.</dd>
      * </dl>
      * <p>This gets properties from the <tt>hrtail.properties</tt> file.</p>
      * @param args
      * @throws CmdLineException
      * @throws MalformedURLException
      * @throws IOException  
      */
     public void run_main(String[] args) throws CmdLineException, MalformedURLException, IOException {
         CmdLineParser parser = new CmdLineParser(this);
         parser.parseArgument(args);
                 
         if (feed) {
             if (mapping != null || effective != null) {
                 throw new CmdLineException("Cannot use both -f and -m/-e options.");
             }
         } else {
             if (mapping == null || effective == null) {
                 throw new CmdLineException("Mapping must have -m type and -e yyyy-mm-dd");
             }
             try {
                 Date eff_dt = fmt_date.parse(effective);
             } catch (ParseException ex) {
                 throw new CmdLineException("Invalid effective date format");
             }
         }
 
         if (arguments.isEmpty()) {
             throw new CmdLineException("Missing file name");
         }
 
         open();
 
         for (String filename : arguments) {
             Object[] details = {
                 mapping, effective, filename, s.getHost(), db.getName()
             };
             File attachment= new File(filename);
             Document doc;
             if (feed) {
                 String msg = MessageFormat.format( 
                         "Push Feed {2} to {3}/{4}", details );
                 logger.info( msg );
                 doc= push_feed(attachment);
             } else {
                 String msg = MessageFormat.format(
                         "Push {0} Mapping {2} to {3}/{4}", details );
                 logger.info(msg);
                 doc= push_mapping(mapping, effective, attachment);
             }
             if( doc != null ) {
                 logger.info("Created " + doc.getId());
             }
         }
     }
 
     /**
      * Opens the default CouchDB session and database.
      * 
      * <p>This uses the <tt>couchpush.db_url</tt> property.  By
      * default this is <tt>http://localhost:5984/couchdbkit_test</tt>
      * 
      * @throws MalformedURLException
      * @throws IOException  
      */
     public void open() throws MalformedURLException, IOException {
        String default_url= global.getProperty("couchpush.db_url","http://localhost:5984/couchdbkit_test");
        open( default_url );
     }
 
     /**
      * Opens the given CouchDB session and databsae.
      * 
      * @param url_override
      * @throws MalformedURLException
      * @throws IOException  
      */
     public void open(String url_override) throws MalformedURLException, IOException {
         URL details= new URL( url_override );
         s = new Session(details.getHost(), details.getPort());
         db = s.getDatabase(details.getPath());
     }
 
     /**
      * Pushes a single feed file.  Creates a feed document with
      * timestamp, status and doc_type of "Feed".
      * 
      * <code>
      * {"timestamp":"2012-03-03T04:03:12Z",
      *   "status":"new",
      *   "doc_type":"Feed"
      * }
      * </code>
      * 
      * @param attachment the File to push
      * @return Document object that was created.
      * @throws FileNotFoundException  
      */
     public Document push_feed(File attachment) throws FileNotFoundException {
         Date modified = new Date(attachment.lastModified());
         Document document = new Document();
         document.put("timestamp", fmt_date_time.format(modified));
         document.put("status", "new");
         document.put("doc_type", "Feed");
 
         boolean ok= push(document, "feed", new FileReader(attachment) );
         if( ok ) return document;
         return null;
     }
 
     /**
      * Pushes a single mapping file.  Creates a feed document
      * with timestamp, mapping type, effective date and doc_type of "Mapping".
      * 
      * <code>
      * {"timestamp":"2012-03-03T04:03:12Z",
      * "mapping_type":"vehicle",
      * "effective_date":"2012-03-12",
      * "doc_type":"Mapping"
      * }
      * </code>
      * 
      * @param mapping type of mapping; generally vehicle, route or stop. 
      * @param effective effective date string in the form yyyy-mm-dd.
      * @param attachment the File to push
      * @return Document object that was created.
      * @throws FileNotFoundException  
      */
     public Document push_mapping(String mapping, String effective, File attachment) throws FileNotFoundException {
         Date modified = new Date(attachment.lastModified());
         Document document = new Document();
         document.put("timestamp", fmt_date_time.format(modified));
         document.put("mapping_type", mapping);
         document.put("effective_date", effective);
         document.put("doc_type", "Mapping");
 
         boolean ok=push(document, "content", new FileReader(attachment) );
         if( ok ) return document;
         return null;
     }
 
     /**
      * Generic POST of a document following by a PUSH of an attachment.
      * 
      * @param document the CouchDB Document instance to push; 
      *  this is updated it id and rev.
      * @param name the attachment name (generally feed or content)
      * @param attachment a Reader for a File to attach
      * @return  True if the push was successful.
      */
     public boolean push(Document document, String name, Reader attachment) {
         try {
             BufferedReader rdr = new BufferedReader(attachment);
 
             db.saveDocument(document);
             String id = (String) document.getId();
             String rev1 = (String) document.getRev();
             if( verbose ) {
                 logger.debug( document );
             }
 
             StringBuilder content = new StringBuilder();
             String line = rdr.readLine();
             while (line != null) {
                content.append(line);
                 line = rdr.readLine();
             }
             Document resp= db.putAttachment(id, rev1, name, "text/csv", content.toString());
             if( verbose ) {
                 logger.debug( resp );
             }
             return resp.getBoolean("ok");
             
         } catch (java.io.FileNotFoundException ex1) {
             logger.error(ex1);
         } catch (java.io.IOException ex2) {
             logger.error(ex2);
         }
         return false;
 
     }
 }
