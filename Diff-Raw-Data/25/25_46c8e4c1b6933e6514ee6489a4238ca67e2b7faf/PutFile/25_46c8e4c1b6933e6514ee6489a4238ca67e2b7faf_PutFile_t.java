 /*
  * #%L
  * Bitrepository Command Line
  * %%
  * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.commandline;
 
 import java.io.File;
 import java.net.URL;
 
 import org.apache.commons.cli.Option;
 import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
 import org.bitrepository.bitrepositoryelements.ChecksumType;
 import org.bitrepository.client.eventhandler.OperationEvent;
 import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
 import org.bitrepository.commandline.utils.CommandLineArgumentsHandler;
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
 public class PutFile {
     /**
      * @param args The arguments for performing the PutFile operation.
      */
     public static void main(String[] args) {
         PutFile putfile = new PutFile(args);
         putfile.performOperation();
     }
     
     /** The component id. */
     private final static String COMPONENT_ID = "PutFileClient";
     
     /** The settings for the put file client.*/
     private final Settings settings;
     /** The security manager.*/
     private final SecurityManager securityManager;
     /** The client for performing the PutFile operation.*/
     private final PutFileClient client;
     /** The handler for the command line arguments.*/
     private final CommandLineArgumentsHandler cmdHandler;
     
     /**
      * 
      * @param args
      */
     private PutFile(String ... args) {
         System.out.println("Initialising arguments");
         cmdHandler = new CommandLineArgumentsHandler();
         try {
             createOptionsForCmdArgumentHandler();
             cmdHandler.parseArguments(args);
             
             settings = cmdHandler.loadSettings(COMPONENT_ID);
             securityManager = cmdHandler.loadSecurityManager(settings);
             
             System.out.println("Instantiating the PutFileClient");
             client = ModifyComponentFactory.getInstance().retrievePutClient(settings, securityManager, COMPONENT_ID);
         } catch (Exception e) {
             System.err.println(cmdHandler.listArguments());
             throw new IllegalArgumentException(e);
         }
     }
     
     /**
      * Perform the PutFile operation.
      */
     public void performOperation() {
         System.out.println("Performing the PutFile operation.");
         OperationEvent e = putTheFile();
         if(e.getType() == OperationEventType.COMPLETE) {
             System.out.println("PutFile operation successfull for the file '" 
                     + cmdHandler.getOptionValue(Constants.FILE_ARG) + "': " + e);
             System.exit(0);
         } else {
             System.err.println("PutFile failed for the file '" + cmdHandler.getOptionValue(Constants.FILE_ARG) + "':" 
                     + e);
             System.exit(-1);
         }
     }
     
     /**
      * Creates the options for the command line argument handler.
      */
     private void createOptionsForCmdArgumentHandler() {
         cmdHandler.createDefaultOptions();
         
         Option fileOption = new Option(Constants.FILE_ARG, true, "The path to the file, which is wanted to "
                 + "be put");
         fileOption.setRequired(Constants.ARGUMENT_IS_REQUIRED);
         cmdHandler.addOption(fileOption);
         
         Option checksumOption = new Option(Constants.CHECKSUM_ARG, true, 
                 "[OPTIONAL] The checksum of the file to be put.");
         checksumOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
         cmdHandler.addOption(checksumOption);
         
         Option checksumTypeOption = new Option(Constants.REQUEST_CHECKSUM_TYPE_ARG, true, 
                 "[OPTIONAL] The algorithm of checksum to request in the response from the pillars.");
         checksumTypeOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
         cmdHandler.addOption(checksumTypeOption);
         Option checksumSaltOption = new Option(Constants.REQUEST_CHECKSUM_SALT_ARG, true, 
                 "[OPTIONAL] The salt of checksum to request in the response. Requires the ChecksumType argument.");
         checksumSaltOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
         cmdHandler.addOption(checksumSaltOption);
     }
     
     /**
      * Initiates the operation and waits for the results.
      * @return The final event for the results of the operation. Either 'FAILURE' or 'COMPLETE'.
      */
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
         if(!cmdHandler.hasOption(Constants.CHECKSUM_ARG)) {
             return null;            
         }
         
         ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
         res.setCalculationTimestamp(CalendarUtils.getNow());
         res.setChecksumSpec(ChecksumUtils.getDefault(settings));
         res.setChecksumValue(Base16Utils.encodeBase16(cmdHandler.getOptionValue(Constants.CHECKSUM_ARG)));
         
         return res;
     }
     
     /**
      * @return The requested checksum spec, or null if the arguments does not exist.
      */
     private ChecksumSpecTYPE getRequestChecksumSpec() {
         if(!cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_TYPE_ARG)) {
             return null;
         }
         
         ChecksumSpecTYPE res = new ChecksumSpecTYPE();
         res.setChecksumType(ChecksumType.fromValue(cmdHandler.getOptionValue(Constants.REQUEST_CHECKSUM_TYPE_ARG)));
         
         if(cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_SALT_ARG)) {
             res.setChecksumSalt(Base16Utils.encodeBase16(cmdHandler.getOptionValue(Constants.REQUEST_CHECKSUM_TYPE_ARG)));
         }
         return res;
     }
     
     /**
      * Finds the file from the arguments.
      * @return 
      */
     private File findTheFile() {
         String filePath = cmdHandler.getOptionValue(Constants.FILE_ARG);
         
         File file = new File(filePath);
         if(!file.isFile()) {
             throw new IllegalArgumentException("The file '" + filePath + "' is invalid. It does not exists or it "
                     + "is a directory.");
         }
         
         return file;
     }
 }
