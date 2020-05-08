 package dk.statsbiblioteket.medieplatform.bitrepository.ingester;
 
 import dk.statsbiblioteket.medieplatform.bitrepository.ingester.ClientExitCodes.ExitCodes;
 
 import org.bitrepository.protocol.utils.LogbackConfigLoader;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 
 /**
  * The main executable class for ingesting files in a configured bit repository.
  */
 public class Ingester {
     public static final int CONFIG_DIR_ARG_INDEX = 0;
     public static final int FILE_LOCATION_ARG_INDEX = 1;
     public static final int FILEID_ARG_INDEX = 2;
     public static final int CHECKSUM_ARG_INDEX = 3;
     public static final int FILESIZE_ARG_INDEX = 4;
 
     private final static Logger log = LoggerFactory.getLogger(Ingester.class);
     
     private Ingester() {}
     
     public static void main(String[] args) {
     	int exitCode = -1;
         try {
             verifyInputParams(args);
         } catch (ClientFailureException e) {
             System.out.println(e.getMessage());
             System.exit(e.getExitCode().getCode());
         }
         FilePutter putter = null;
        System.err.println("Config dir: " + args[CONFIG_DIR_ARG_INDEX]);
         
         try {
             setupLogging(args[CONFIG_DIR_ARG_INDEX]);
             log.info("Ingest of file requested: " + args);
             log.debug("Starting client");
             putter = new FilePutter(args[CONFIG_DIR_ARG_INDEX], args[FILEID_ARG_INDEX], 
                     args[FILE_LOCATION_ARG_INDEX], args[CHECKSUM_ARG_INDEX], 
                     Long.parseLong(args[FILESIZE_ARG_INDEX]));  
             putter.putFile();
             JSONObject obj = new JSONObject();
             try {
                 obj.put("UrlToFile", putter.getUrl());
                 System.out.println(obj.toString());
                 exitCode = ExitCodes.SUCCESS.getCode();
             } catch (JSONException e) {
             	exitCode = ExitCodes.JSON_ERROR.getCode();
             }
         } catch (ClientFailureException e) {
             log.error("File:" + args[FILEID_ARG_INDEX] + " Failed to ingest file: " + args[FILE_LOCATION_ARG_INDEX], e);
             System.out.println(e.getMessage());
             exitCode = e.getExitCode().getCode();
         } catch (Exception e) {
         	log.error("Caught unexpected exception", e);
         	exitCode = 100;
         } finally {
             if(putter != null) {
             	log.debug("Shutting down messagebus connection");
             	putter.shutdown();
             }
         }
         
         System.exit(exitCode);
     }
     
     /**
      * Method to verify the input parameters
      * Verifies the validity of:
      * - Number of arguments 
      * - The existence and readability of the configuration directory
      * - The ability to parse the file size parameter as a long
      * - The length and content of the checksum parameter  
      * If validation fails error is printed to console and program is exited. 
      */
     private static void verifyInputParams(String[] args) throws ClientFailureException {
         if(args.length != 5) {
             throw new ClientFailureException("Unexpected number of arguments, got " + args.length + " but expected 5" + 
                     "Expecting: ConfigDirPath FileUrl FileID FileChecksum FileSize", 
                     ExitCodes.INPUT_PARAM_COUNT_ERROR);
         }
         
         File configDir = new File(args[CONFIG_DIR_ARG_INDEX]);
         if(!configDir.exists()) {
             throw new ClientFailureException("Config dir (parm " + CONFIG_DIR_ARG_INDEX + ": " +
                     configDir.getAbsolutePath() + ") doesn't exist!",
                     ExitCodes.CONFIG_DIR_ERROR);
         }
         if(!configDir.isDirectory()) {
             throw new ClientFailureException("Config dir (parm " + CONFIG_DIR_ARG_INDEX + ": " + configDir +
                     ") is not a directory!",
                     ExitCodes.CONFIG_DIR_ERROR);
         }
         if(!configDir.canRead()) {
             throw new ClientFailureException("Config dir '" + configDir + "' cannot be read!",
                     ExitCodes.CONFIG_DIR_ERROR);
         }
         
         try {
             Long.parseLong(args[FILESIZE_ARG_INDEX]);
         } catch (Exception e) {
             throw new ClientFailureException("Failed to parse filesize argument " + args[FILESIZE_ARG_INDEX] +
                     " as long.", ExitCodes.FILE_SIZE_ERROR);
         }
         
         String checksum = args[CHECKSUM_ARG_INDEX];
         if((checksum.length() % 2) != 0) {
             throw new ClientFailureException("Checksum argument " + checksum +
                     " does not contain an even number of characters.",
                     ExitCodes.CHECKSUM_ERROR);
         }
         if(!checksum.matches("^\\p{XDigit}*$")) {
             throw new ClientFailureException("Checksum argument " + checksum +
                     " contains non hexadecimal value!", ExitCodes.CHECKSUM_ERROR);
         } 
     }
     
     private static void setupLogging(String configDir) throws ClientFailureException {
         try {
             new LogbackConfigLoader(configDir + "/logback.xml");
         } catch (Exception e) {
             throw new ClientFailureException("Logging setup failed!", ExitCodes.LOGGING_ERROR);
         } 
     }
 }
