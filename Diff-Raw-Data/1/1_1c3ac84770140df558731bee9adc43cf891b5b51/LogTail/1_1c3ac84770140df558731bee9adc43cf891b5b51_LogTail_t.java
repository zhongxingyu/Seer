 /*
  * The HRT Project.
  * This work is licensed under the 
  * Creative Commons Attribution-NonCommercial 3.0 Unported License. 
  * To view a copy of this license, 
  * visit http://creativecommons.org/licenses/by-nc/3.0/ 
  * or send a letter to 
  * Creative Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
  */
 package org.hrva.capture;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 
 /**
  * Tail a rapidly growing log file and push an HRT Feeds to the CouchDB.
  *
  * <p>This is both a main program with a command-line interface, as well as
  * object that can be used to tail a log file. </p>
  *
  * <p>Typical use case</p>
  * <code><pre>
  *     LogTail lt = new LogTail();
  *     lt.tail( "/path/to/some.log", "extract.txt" );
  *     CouchPush cp= new CouchPush()
  *     cp.open();
  *     cp.push_feed( "extract.txt" );
  *     System.out.print( "Created "+cp.id );
  * </pre></code>
  *
  * <p>At the command line, it might look like this.</p> 
  * <code><pre>
  * java -cp LogTail/dist/LogTail.jar org.hrva.capture.LogTail -o extract.txt /path/to/some.log
  * java -cp LogTail/dist/LogTail.jar org.hrva.capture.CouchPush -f extract.txt 
  * </pre></code>
  * 
  * @author slott
  */
 public class LogTail {
 
     /** Properties for this application. */
     Properties global = new Properties();
     
     /** Output file name. */
     @Option(name="-o", usage="Output file name.")
     String extract_filename="hrtrtf.txt";
     
     /** Immediate Push option. */
     @Option(name="-f", usage="Do an immediate feed push.")
     boolean immediate= false;
     
     /** Verbose debugging. */
     @Option(name = "-v", usage = "Vebose logging")
     boolean verbose= false;
 
     /** Command-line Arguments. */
     @Argument
     List<String> arguments = new ArrayList<String>();
 
     /** Logger. */
     final Log  logger = LogFactory.getLog(LogTail.class);
 
     /**
      * Command-line program to tail a log and then push file to the HRT couch
      * DB.
      * <p>All this does is read properties and invoke run_main</p>
      *
      * @param args arguments
      */
     public static void main(String[] args) {
         Log log = LogFactory.getLog(LogTail.class);
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
         LogTail lt = new LogTail(config);
         try {
             lt.run_main(args);
         } catch (CmdLineException ex1) {
             log.fatal("Invalid Options", ex1);
         } catch (MalformedURLException ex2) {
             log.fatal("Invalid CouchDB URL", ex2);
         } catch (IOException ex3) {
             log.fatal(ex3);
         }
     }
 
     /**
      * Build the LogTail instance. 
      * 
      * @param global The hrtail.properties file
      */
     public LogTail( Properties global) {
         super();
         this.global= global;
     }
 
     /**
      * Tails the log and (optionally) pushes a feed file.
      *
      * <ol> <li>Get cached status info.</li> <li>Tail Log</li>
      * <li>Update cached status info.</li>
      * <li>(optionally) Send to
      * couchdb.</li>  </ol>
      *
      * @param args the command line arguments
      * @throws CmdLineException
      * @throws FileNotFoundException
      * @throws IOException
      */
     public void run_main(String[] args) throws CmdLineException, FileNotFoundException, IOException {
         CmdLineParser parser = new CmdLineParser(this);
         parser.parseArgument(args);
 
         if (arguments.size() != 1) {
             throw new CmdLineException("Only one log file can be tailed");
         }
         for (String source : arguments) {
             String temp = tail(source, extract_filename);
             if (temp != null && immediate) {
                 push_feed(temp);
             }
         }
     }
 
     /**
      * Tail the given file if the size has changed and return a temp filename.
      *
      * <p>This returns a temp filename if the log being tailed has changed.
      * </p>
      * 
      * <p>The supplied target filename is -- actually -- a format string.
      * The available value, <<tt>{0}</tt> is the sequence number
      * that's saved in the history cache.</p>
      *
      * @param source The log filename to tail
      * @param target A temporary filename into which to save the tail piece.
      * @return temp filename, if the file size changed; otherwise null
      * @throws FileNotFoundException
      * @throws IOException
      */
     public String tail(String source, String target) throws FileNotFoundException, IOException {
         // The resulting file name (or null if the log did not grow).
         String temp_name = null;
 
         // Open our last-time-we-looked file.
         String cache_file_name = global.getProperty("logtail.tail_status_filename",
                 "logtail.history");
         String limit_str = global.getProperty("logtail.file_size_limit",
                 "1m"); // 1 * 1024 * 1024;
         int limit;
         if( limit_str.endsWith("m") || limit_str.endsWith("M") ) {
             limit= 1024*1024*Integer.parseInt(limit_str.substring(0,limit_str.length()-1));
         }
         else if( limit_str.endsWith("k") || limit_str.endsWith("K") ) {
             limit= 1024*Integer.parseInt(limit_str.substring(0,limit_str.length()-1));
         }
         else{
             limit = Integer.parseInt(limit_str);
         }
 
         Properties state = get_state(cache_file_name);
 
         // Find the previous size and sequence number
         String prev_size_str = state.getProperty("size." + source, "0");
         long prev_size = Long.parseLong(prev_size_str);
         String seq_str = state.getProperty("seq." + source, "0");
         long sequence = Long.parseLong(seq_str);
 
         Object[] details = {
             source, target, seq_str, prev_size_str 
         };
         logger.info(MessageFormat.format("Tailing {0} to {1}", details));
         logger.info(MessageFormat.format("Count {2}, Bytes {3}", details));
         sequence += 1;
 
         // Attempt to seek to the previous position
         long position = 0;
         File log_to_tail = new File(source);
         RandomAccessFile rdr = new RandomAccessFile(log_to_tail, "r");
         try {
             long current_size = rdr.length();
             if (current_size == prev_size) {
                 // Same size.  Nothing more to do here.
                 position = current_size;
             } else {
                 // Changed size.  Either grew or was truncated.
                 if (rdr.length() < prev_size) {
                     // Got truncated.  Read from beginning.
                     sequence = 0;
                    prev_size= 0;
                 } else {
                     // Got bigger.  Read from where we left off.
                     rdr.seek(prev_size);
                 }
                 // Read to EOF or the limit.  
                 // No reason to get greedy.
                 int read_size;
                 if (current_size - prev_size > limit) {
                     read_size = limit;
                     rdr.seek( current_size-limit );
                 } else {
                     read_size = (int) (current_size - prev_size);
                 }
                 byte[] buffer = new byte[read_size];
                 rdr.read(buffer);
                 position = rdr.getFilePointer();
 
                 // Write temp file
                 Object[] args = { sequence };
                 temp_name = MessageFormat.format(target, args);
 
                 File extract = new File(temp_name);
                 OutputStream wtr = new FileOutputStream(extract);
                 wtr.write(buffer);
             }
         } finally {
             rdr.close();
         }
 
         // Update our private last-time-we-looked file.
         state.setProperty("size." + source, String.valueOf(position));
         state.setProperty("seq." + source, String.valueOf(sequence));
         save_state(cache_file_name, state);
 
         Object[] details2 = {
             source, target, seq_str, prev_size_str, 
             String.valueOf(sequence), String.valueOf(position)
         };
         logger.info(MessageFormat.format("Count {4}, Bytes {5}", details2));
 
         return temp_name;
     }
 
     /**
      * Push the given file to the database server. This essentially runs the
      * CouchPush application.
      *
      * @param filename
      * @throws MalformedURLException
      * @throws IOException
      */
     public void push_feed(String filename) throws MalformedURLException, IOException {
         File attachment= new File(filename);
         CouchPush cp = new CouchPush(global);
         cp.open();
         cp.push_feed(attachment);
     }
 
     /**
      * Get the saved file size state.
      *
      * @param name Properties file into which the file sizes were saved.
      * @return Properties object with saved file sizes.
      */
     public Properties get_state(String name) {
         Properties state = new Properties();
         File cache_file = new File(name);
         if (cache_file.exists()) {
             InputStream istr;
             try {
                 istr = new FileInputStream(cache_file);
                 state.load(istr);
             } catch (FileNotFoundException ex) {
                 logger.warn("No history "+name, ex);
             } catch (java.io.IOException ex) {
                 logger.warn("Problems with history "+name, ex);
             }
         }
         return state;
     }
 
     /**
      * Save the file size for next time we're executed.
      *
      * @param name Properties file into which the file sizes are saved.
      * @param state Properties object to persist.
      * @throws FileNotFoundException
      * @throws IOException
      */
     public void save_state(String name, Properties state) throws FileNotFoundException, IOException {
         OutputStream ostr = new FileOutputStream(name);
         state.store(ostr, "LogTail Cache");
     }
 }
