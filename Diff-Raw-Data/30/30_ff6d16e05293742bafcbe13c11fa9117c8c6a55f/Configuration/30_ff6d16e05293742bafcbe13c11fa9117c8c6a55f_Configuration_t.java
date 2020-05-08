 package edu.unh.schwartz.epframework.config;
 
 import com.fasterxml.jackson.core.JsonEncoding;
 import com.fasterxml.jackson.core.JsonFactory;
 import com.fasterxml.jackson.core.JsonGenerator;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.JsonToken;
 import java.io.File;
 import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
 import java.util.Map;
 import java.util.Iterator;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * The settings for the current run of the application. 
  *
  * Talk about the various fields it contains
  */
 public final class Configuration
 {
     /**
      * Key for in file setting.
      */
     public static final String IN_FILE_KEY = "inFile";
 
     /**
      * Key for in file directory setting.
      */
     public static final String IN_FILE_DIR_KEY = "inFileDir";
 
     /**
      * Key for split pattern setting.
      */
     public static final String SPLIT_PATTERN_KEY = "split";
 
     /**
      * Key for out file setting.
      */
     public static final String OUT_FILE_KEY = "outFile";
 
     /**
      * Key for executable location setting.
      */
     public static final String EXEC_LOC_KEY = "execLoc";
 
     /**
      * Key for threading setting.
      */
     public static final String NUM_PROCESSES_KEY = "numProcesses";
 
     /**
      * Key for stats setting.
      */
     public static final String STATS_KEY = "stats";
 
     /**
      * Key for output file header setting.
      */
     public static final String NUM_HEADER_KEY = "numHeaderLines";
 
     /**
      * Key for setting whether or not to save the file.
      */
     public static final String SAVE_KEY = "save";
 
     /**
      * Key for setting the merge method.
      */
     public static final String EXTERNAL_MERGE_KEY = "externalMerge";
 
     /**
      * Key for setting the merge method.
      */
     public static final String CUSTOM_MERGE_KEY = "customMerge";
 
     /**
      * Key for setting the merge method.
      */
     public static final String DEFAULT_MERGE_KEY = "defaultMerge";
 
     /**
      * Key for setting the command line argument.
      */
     public static final String ARGUMENT_KEY = "argument";
 
     /**
      * The Log.
      */
     private static final Log LOG = LogFactory.getLog(Configuration.class);
 
     /**
      * The number of threads used.
      */
     private int numberOfThreads;
     
     /**
      * The name of the input file.
      */
     private String inputFileName;
      
     /**
      * The name of the input directory file.
      */
     private String inputFileDirName;
 
     /**
      * The regex pattern to split the input file on.
      */
     private String splitPattern;
 
     /**
      * The directory where the output should go.
      */
     private String outDirName;
 
     /**
      * The path to the executable.
      */
     private String execPath;
 
     /**
      * Whether or not to make a stats file.
      */
     private boolean makeStats;
 
     /**
      * Number of header lines in an output file from the executable.
      */
     private int numberOfHeaderLines;
 
     /**
      * The method used to merge the results.
      */
     private int mergeMethod = -1;
 
     /**
      * Whether or not to save the config file.
      */
     private boolean save;
 
     /**
      * The arguments for the command line.
      */
     private String argument;
     
     /**
      * Constructs a configuration file from a map.
      * @param map - the map that holds the configuration settings
      */
     public Configuration(final Map<String, Object> map)
     {
         loadConfig(map);
 
         // Check to make sure necessary fields were filled
         boolean crash = false;
         if ((this.inputFileDirName == null && this.inputFileName == null) || 
                 (this.inputFileName != null && this.inputFileName != null))
         {
             LOG.fatal("An input file OR directory must be selected");
             crash = true;
         }
         else if (this.outDirName == null)
         {
             LOG.fatal("An output directory must be selected");
             crash = true;
         }
         else if (this.execPath == null)
         {
             crash = true;
             LOG.fatal("An executable must be selected");
         }
         else if (this.mergeMethod == -1)
         {
             crash = true;
             LOG.fatal("A merge method must be selected");
         }
 
         if (crash)
         {
             System.exit(1);
         }
         else if (this.save)
         {
             saveConfig();
         }
     }
 
     /**
      * Constructs a configuration file from a past configuration file that was
      * saved to the disk.
      *
      * @param fileName - the path to the file on disk
      * @throws IOException - if there is any problem reading the file
      */
     public Configuration(final String fileName) throws IOException
     {
         final JsonFactory jf = new JsonFactory();
         final JsonParser jp = jf.createParser(new File(fileName));
         jp.nextToken();
 
         while (jp.nextToken() != JsonToken.END_OBJECT)
         {
             final String fieldName = jp.getCurrentName();
             jp.nextToken();
             switch(fieldName)
             {
                 case IN_FILE_KEY:
                     this.inputFileName = jp.getText();
                     break;
                 case IN_FILE_DIR_KEY:
                     this.inputFileDirName = jp.getText();
                     break;
                 case SPLIT_PATTERN_KEY:
                     this.splitPattern = jp.getText();
                     break;
                 case OUT_FILE_KEY:
                     this.outDirName = jp.getText();
                     break;
                 case EXEC_LOC_KEY:
                     this.execPath = jp.getText();
                     break;
                 case NUM_PROCESSES_KEY:
                     this.numberOfThreads = jp.getIntValue();
                     break;
                 case STATS_KEY:
                     this.makeStats = jp.getBooleanValue();
                     break;
                 case NUM_HEADER_KEY:
                     this.numberOfHeaderLines = jp.getIntValue();
                     break;
                 case DEFAULT_MERGE_KEY:
                     if (jp.getBooleanValue())
                     {
                         this.mergeMethod = 0;
                     }
                     break;
                 case CUSTOM_MERGE_KEY:
                     if (jp.getBooleanValue())
                     {
                         this.mergeMethod = 1;
                     }
                     break;
                 case EXTERNAL_MERGE_KEY:
                     if (jp.getBooleanValue())
                     {
                         this.mergeMethod = 2;
                     }
                     break;
                  default:
                     assert(false);
             }
         }
         jp.close();
     }
 
     private void loadConfig(final Map<String, Object> map)
     {
          final Iterator<String> it = map.keySet().iterator();
          while (it.hasNext()) 
          {
              final String key = it.next();
              final Object val = map.get(key);
              LOG.debug(key + " -> " + val);
              switch(key)
              {
                  case IN_FILE_KEY:
                      this.inputFileName = (String) val;
                      break;
                  case IN_FILE_DIR_KEY:
                      this.inputFileDirName = (String) val;
                      break;
                  case SPLIT_PATTERN_KEY:
                      this.splitPattern = (String) val;
                      break;
                  case OUT_FILE_KEY:
                      this.outDirName = (String) val;
                      break;
                  case EXEC_LOC_KEY:
                      this.execPath = (String) val;
                      break;
                  case NUM_PROCESSES_KEY:
                      this.numberOfThreads = Integer.valueOf((String) val);
                      break;
                  case STATS_KEY:
                      this.makeStats = (boolean) val;
                      break;
                  case NUM_HEADER_KEY:
                      this.numberOfHeaderLines = Integer.valueOf((String) val);
                      break;
                  case SAVE_KEY:
                      this.save = (boolean) val;
                      break;
                  case DEFAULT_MERGE_KEY:
                      if ((boolean) val)
                      {
                          this.mergeMethod = 0;
                      }
                      break;
                  case CUSTOM_MERGE_KEY:
                      if ((boolean) val)
                      {
                          this.mergeMethod = 1;
                      }
                      break;
                  case EXTERNAL_MERGE_KEY:
                      if ((boolean) val)
                      {
                          this.mergeMethod = 2;
                      }
                      break;
                  default:
                      assert(false);
              }
          }
 
 
     }
 
 
     /**
      * @return the number of threads the program will use
      */
     public int getNumberOfThreads()
     {
         return this.numberOfThreads;
     }
 
     /**
      * @return the path to the input file
      */
     public String getInputFile()
     {
         return this.inputFileName;
     }
 
     /**
      * @return the path to the input directory of files
      */
     public String getInputDirectory()
     {
         return this.inputFileDirName;
     }
 
     /**
      * @return the regex pattern used the split the input file
      */
     public String getSplitPattern()
     {
         return this.splitPattern;
     }
 
     /**
      * @return the directory for the final output
      */
     public String getOutputDirectory()
     {
         return this.outDirName;
     }
 
     /**
      * @return the path to the executable
      */
     public String getExecutable()
     {
         return this.execPath;
     }
 
     /**
      * @return the number of header lines to copy to the final output
      */
     public int getNumHeaderLines()
     {
         return this.numberOfHeaderLines;
     }
 
     /**
      * @return true iff the user wants a statistics file created after the
      * workers have all finished
      */
     public boolean makeStats()
     {
         return this.makeStats;
     }
 
     /**
      * @return the method used to merge the results
      */
     public int getMergeMethod()
     {
         return this.mergeMethod;
     }
 
     /**
      * @return the args for the command line 
      */
     public String getArguments()
     {
         return this.argument;
     }
 
     /**
      * Saves the configuration in a file so it can be loaded if the user wants
      * to use the configuration again.
      */
     public void saveConfig()
     {
         final JsonFactory jsonFactory = new JsonFactory(); 
         try
         {
             LOG.info("Saving configuration in " + this.outDirName);

            final DateFormat dateFmt = new SimpleDateFormat("yyyyMMdd-HHmmss");
            final Date date = new Date();
            final String d = dateFmt.format(date);

            final File file = new File(this.outDirName + "/config" + d + 
                    ".json");
             final JsonGenerator jg = jsonFactory.createGenerator(file, 
                     JsonEncoding.UTF8);
             jg.writeStartObject();
             if (this.inputFileName != null)
             {
                 jg.writeStringField(IN_FILE_KEY, this.inputFileName);
                 jg.writeStringField(SPLIT_PATTERN_KEY, this.splitPattern);
             }
             else
             {
                 jg.writeStringField(IN_FILE_DIR_KEY, this.inputFileDirName);
             }
 
             jg.writeStringField(OUT_FILE_KEY, this.outDirName);
             jg.writeStringField(EXEC_LOC_KEY, this.execPath);
             jg.writeNumberField(NUM_PROCESSES_KEY, this.numberOfThreads);
             jg.writeBooleanField(STATS_KEY, this.makeStats);
             jg.writeNumberField(NUM_HEADER_KEY, this.numberOfHeaderLines);
             jg.writeEndObject();
             jg.close();
             LOG.info("Configuration saved");
         }
         catch (IOException e)
         {
             LOG.error("saveConfig: " + e.getMessage());
         }
     }
 }
