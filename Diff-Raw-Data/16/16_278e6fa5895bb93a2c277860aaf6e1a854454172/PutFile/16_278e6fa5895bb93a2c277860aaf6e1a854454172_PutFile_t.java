 package org.bitrepository.commandline;
 
 import java.io.File;
 import java.net.URL;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.bitrepository.client.eventhandler.OperationEvent;
 import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
 import org.bitrepository.commandline.utils.CompleteEventAwaiter;
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.common.utils.Base16Utils;
 import org.bitrepository.common.utils.CalendarUtils;
 import org.bitrepository.common.utils.ChecksumUtils;
 import org.bitrepository.modify.ModifyComponentFactory;
 import org.bitrepository.modify.putfile.PutFileClient;
 import org.bitrepository.protocol.FileExchange;
 import org.bitrepository.protocol.ProtocolComponentFactory;
 import org.bitrepository.protocol.security.SecurityManager;
 
 /**
  * Putting a file to the collection.
  */
 public class PutFile extends CommandLineInterface {
     /**
      * @param args The arguments for performing the PutFile operation.
      */
     public static void main(String[] args) {
         new PutFile(args);
     }
     
     /** The component id. */
     private final static String COMPONENT_ID = "PutFileClient";
     
     /** The commandline with the results of the parsed arguments.*/
     private final CommandLine cmd;
     /** The settings for the put file client.*/
     private final Settings settings;
     /** The security manager.*/
     private final SecurityManager securityManager;
     /** The client for performing the PutFile operation.*/
     private final PutFileClient client;
     
     /**
      * 
      * @param args
      */
     private PutFile(String ... args) {
         super();
         try {
            System.out.println("Initialising arguments");
             cmd = parseArguments(args);
             settings = loadSettings(COMPONENT_ID, cmd.getOptionValue(Constants.SETTINGS_ARG));
             securityManager = loadSecurityManager(cmd.getOptionValue(Constants.PRIVATE_KEY_ARG), settings);
             
            System.out.println("Instantiating the PutFileClient");
             client = ModifyComponentFactory.getInstance().retrievePutClient(settings, securityManager, COMPONENT_ID);
         } catch (Exception e) {
             System.err.println(listArguments());
             throw new IllegalArgumentException(e);
         }
         
        System.out.println("Performing the PutFile operation.");
         OperationEvent e = putTheFile();
         if(e.getType() == OperationEventType.COMPLETE) {
             System.out.println("PutFile operation successfull for the file '" 
                     + cmd.getOptionValue(Constants.FILE_PATH_ARG) + "': " + e);
            System.exit(0);
         } else {
             System.err.println("PutFile failed for the file '" + cmd.getOptionValue(Constants.FILE_PATH_ARG) + "':" 
                     + e);
             System.exit(-1);
         }
     }
     
     @Override
     void createSpecificOptions() {
         Option fileOption = new Option(Constants.FILE_PATH_ARG, true, "The path to the file, which is wanted to "
                 + "be put");
         fileOption.setRequired(Constants.ARGUMENT_IS_REQUIRED);
         options.addOption(fileOption);
         
         Option checksumOption = new Option(Constants.CHECKSUM_ARG, true, "The checksum of the file to be put.");
         checksumOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
         options.addOption(checksumOption);
         
         Option checksumTypeOption = new Option(Constants.REQUEST_CHECKSUM_TYPE_ARG, true, 
                 "The algorithm of checksum to request in the response from the pillars.");
         checksumTypeOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
         options.addOption(checksumTypeOption);
         Option checksumSaltOption = new Option(Constants.REQUEST_CHECKSUM_SALT_ARG, true, 
                 "The salt of checksum to request in the response. Requires the ChecksumType argument.");
         checksumSaltOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
         options.addOption(checksumSaltOption);
     }
     
     @SuppressWarnings("deprecation")
     private OperationEvent putTheFile() {
        
         File f = findTheFile();
         FileExchange fileexchange = ProtocolComponentFactory.getInstance().getFileExchange();
         URL url = fileexchange.uploadToServer(f);
         
         ChecksumDataForFileTYPE validationChecksum = getValidationChecksum();
         ChecksumSpecTYPE requestChecksum = getRequestChecksumSpec();
         
         CompleteEventAwaiter eventHandler = new CompleteEventAwaiter(settings);
         client.putFile(url, f.getName(), f.length(), validationChecksum, requestChecksum, eventHandler, 
                 "Putting the file '" + f.getName() + "' from commandLine.");
         
         return eventHandler.getFinish();
     }
     
     /**
      * Creates the data structure for encapsulating the validation checksums for validation of the PutFile operation.
      * @return The ChecksumDataForFileTYPE for the pillars to validate the PutFile operation.
      */
     private ChecksumDataForFileTYPE getValidationChecksum() {
         if(!cmd.hasOption(Constants.CHECKSUM_ARG)) {
             return null;            
         }
         
         ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
         res.setCalculationTimestamp(CalendarUtils.getNow());
         res.setChecksumSpec(ChecksumUtils.getDefault(settings));
         res.setChecksumValue(Base16Utils.encodeBase16(cmd.getOptionValue(Constants.CHECKSUM_ARG)));
         
         return res;
     }
     
     /**
      * @return The requested checksum spec, or null if the arguments does not exist.
      */
     private ChecksumSpecTYPE getRequestChecksumSpec() {
         if(!cmd.hasOption(Constants.REQUEST_CHECKSUM_TYPE_ARG)) {
             return null;
         }
         
         ChecksumSpecTYPE res = new ChecksumSpecTYPE();
         res.setChecksumType(ChecksumType.fromValue(cmd.getOptionValue(Constants.REQUEST_CHECKSUM_TYPE_ARG)));
         
         if(cmd.hasOption(Constants.REQUEST_CHECKSUM_SALT_ARG)) {
             res.setChecksumSalt(Base16Utils.encodeBase16(cmd.getOptionValue(Constants.REQUEST_CHECKSUM_TYPE_ARG)));
         }
         return res;
     }
     
     /**
      * Finds the file from the arguments.
      * @return 
      */
     private File findTheFile() {
         String filePath = cmd.getOptionValue(Constants.FILE_PATH_ARG);
         
         File file = new File(filePath);
         if(!file.isFile()) {
             throw new IllegalArgumentException("The file '" + filePath + "' is invalid. It does not exists or it "
                     + "is a directory.");
         }
         
         return file;
     }
 }
