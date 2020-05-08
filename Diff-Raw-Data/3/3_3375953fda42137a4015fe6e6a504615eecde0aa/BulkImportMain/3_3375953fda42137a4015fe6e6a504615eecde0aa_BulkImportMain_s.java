 //
 // Treasure Data Bulk-Import Tool in Java
 //
 // Copyright (C) 2012 - 2013 Muga Nishizawa
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package com.treasure_data.bulk_import;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import com.treasure_data.bulk_import.ErrorInfo;
 import com.treasure_data.bulk_import.prepare_parts.MultiThreadPrepareProcessor;
 import com.treasure_data.bulk_import.prepare_parts.PrepareConfiguration;
 import com.treasure_data.bulk_import.prepare_parts.UploadTask;
 import com.treasure_data.bulk_import.upload_parts.MultiThreadUploadProcessor;
 import com.treasure_data.bulk_import.upload_parts.UploadConfiguration;
 import com.treasure_data.bulk_import.upload_parts.UploadProcessor;
 import com.treasure_data.client.TreasureDataClient;
 import com.treasure_data.client.bulkimport.BulkImportClient;
 
 public class BulkImportMain {
     private static final Logger LOG = Logger.getLogger(BulkImportMain.class.getName());
     private static final String TIME_FORMAT = "yyyy_MM_dd";
 
     public static void prepare(final String[] args, Properties props)
             throws Exception {
         String msg = String.format("Start %s command", Configuration.CMD_PREPARE);
         System.out.println(msg);
         LOG.info(msg);
 
         // create configuration for 'prepare' processing
         final PrepareConfiguration conf = createPrepareConfiguration(props, args);
 
         // extract command-line arguments
         List<String> argList = conf.getNonOptionArguments();
         final String[] fileNames = new String[argList.size() - 1]; // delete 'prepare_parts'
         for (int i = 0; i < fileNames.length; i++) {
             fileNames[i] = argList.get(i + 1);
         }
 
         MultiThreadPrepareProcessor proc = new MultiThreadPrepareProcessor(conf);
         proc.registerWorkers();
         proc.startWorkers();
 
         // scan files that are uploaded
         new Thread(new Runnable() {
             public void run() {
                 for (int i = 0; i < fileNames.length; i++) {
                     try {
                         com.treasure_data.bulk_import.prepare_parts.Task task =
                                 new com.treasure_data.bulk_import.prepare_parts.Task(
                                         fileNames[i]);
                         MultiThreadPrepareProcessor.addTask(task);
                     } catch (Throwable t) {
                         LOG.severe("Error occurred During 'addTask' method call");
                         LOG.throwing("Main", "addTask", t);
                     }
                 }
 
                 // end of file list
                 try {
                     MultiThreadPrepareProcessor.addFinishTask(conf);
                 } catch (Throwable t) {
                     LOG.severe("Error occurred During 'addFinishTask' method call");
                     LOG.throwing("Main", "addFinishTask", t);
                 }
             }
         }).start();
 
         proc.joinWorkers();
         List<ErrorInfo> errs = proc.getErrors();
         outputErrors(errs, Configuration.CMD_PREPARE);
     }
 
     public static void upload(final String[] args, Properties props)
             throws Exception {
         String msg = String.format("Start %s commands", Configuration.CMD_UPLOAD);
         System.out.println(msg);
         LOG.info(msg);
 
         // create configuration for 'upload' processing
         final UploadConfiguration uploadConf = createUploadConfiguration(props, args);
 
         // extract command-line arguments
         List<String> argList = uploadConf.getNonOptionArguments();
 
         // create TreasureDataClient and BulkImportClient objects
         TreasureDataClient tdClient = new TreasureDataClient(uploadConf.getProperties());
         BulkImportClient biClient = new BulkImportClient(tdClient);
 
         // configure session name
         ErrorInfo e;
         final String sessionName;
         int filePos;
         if (uploadConf.autoCreateSession()) { // 'auto-create-session'
             String databaseName = uploadConf.makeSession()[0];
             String tableName = uploadConf.makeSession()[1];
             Date d = new Date();
             String timestamp = new SimpleDateFormat(TIME_FORMAT).format(d);
             sessionName = String.format("%s_%s_%s_%d", databaseName, tableName,
                     timestamp, (d.getTime() / 1000));
 
             // validate that database is live or not
             e = UploadProcessor.checkDatabase(tdClient, uploadConf,
                     sessionName, databaseName);
             if (e.error != null) {
                 throw new IllegalArgumentException(e.error);
             }
 
             // validate that table is live or not
             e = UploadProcessor.checkTable(tdClient, uploadConf,
                     sessionName, databaseName, tableName);
             if (e.error != null) {
                 throw new IllegalArgumentException(e.error);
             }
 
             // create bulk import session
             e = UploadProcessor.createSession(biClient, uploadConf, sessionName, databaseName, tableName);
             if (e.error != null) {
                 throw new IllegalArgumentException(e.error);
             }
 
             filePos = 1;
         } else {
             sessionName = argList.get(1); // get session name from command-line arguments
             // validate that the session is live or not
             e = UploadProcessor.checkSession(biClient, uploadConf, sessionName);
             if (e.error != null) {
                 throw new IllegalArgumentException(e.error);
             }
 
             filePos = 2;
         }
 
         // configure uploaded file list
         final String[] fileNames = new String[argList.size() - filePos]; // delete command
         for (int i = 0; i < fileNames.length; i++) {
             fileNames[i] = argList.get(i + filePos);
         }
 
         MultiThreadUploadProcessor uploadProc = new MultiThreadUploadProcessor(uploadConf);
         uploadProc.registerWorkers();
         uploadProc.startWorkers();
 
         List<ErrorInfo> errs = new ArrayList<ErrorInfo>();
 
         if (!uploadConf.hasPrepareOptions()) {
             // scan files that are uploaded
             new Thread(new Runnable() {
                 public void run() {
                     for (int i = 0; i < fileNames.length; i++) {
                         try {
                             long size = new File(fileNames[i]).length();
                             com.treasure_data.bulk_import.upload_parts.Task task =
                                     new com.treasure_data.bulk_import.upload_parts.Task(
                                     sessionName, fileNames[i], size);
                             MultiThreadUploadProcessor.addTask(task);
                         } catch (Throwable t) {
                             LOG.severe("Error occurred During 'addTask' method call");
                             LOG.throwing("Main", "addTask", t);
                         }
                     }
 
                     // end of file list
                     try {
                         MultiThreadUploadProcessor.addFinishTask(uploadConf);
                     } catch (Throwable t) {
                         LOG.severe("Error occurred During 'addFinishTask' method call");
                         LOG.throwing("Main", "addFinishTask", t);
                     }
                 }
             }).start();
 
             uploadProc.joinWorkers();
         } else {
             // create configuration for 'prepare' processing
             final PrepareConfiguration prepareConf = createPrepareConfiguration(props, args, true);
 
             MultiThreadPrepareProcessor prepareProc = new MultiThreadPrepareProcessor(prepareConf);
             prepareProc.registerWorkers();
             prepareProc.startWorkers();
 
             // scan files that are uploaded
             new Thread(new Runnable() {
                 public void run() {
                     for (int i = 0; i < fileNames.length; i++) {
                         try {
                             UploadTask task = new UploadTask(sessionName, fileNames[i]);
                             MultiThreadPrepareProcessor.addTask(task);
                         } catch (Throwable t) {
                             LOG.severe("Error occurred During 'addTask' method call");
                             LOG.throwing("Main", "addTask", t);
                         }
                     }
 
                     // end of file list
                     try {
                         MultiThreadPrepareProcessor.addFinishTask(prepareConf);
                     } catch (Throwable t) {
                         LOG.severe("Error occurred During 'addFinishTask' method call");
                         LOG.throwing("Main", "addFinishTask", t);
                     }
                 }
             }).start();
 
             prepareProc.joinWorkers();
             errs.addAll(prepareProc.getErrors());
         }
 
         MultiThreadUploadProcessor.addFinishTask(uploadConf);
         uploadProc.joinWorkers();
         errs.addAll(uploadProc.getErrors());
 
         // 'auto-perform' and 'auto-commit'
         ErrorInfo processed = UploadProcessor.processAfterUploading(biClient, uploadConf, sessionName);
         errs.add(processed);
 
         if (uploadConf.autoDeleteSession()) { // 'auto-delete-session'
             ErrorInfo deleted = UploadProcessor.deleteSession(biClient, uploadConf, sessionName);
             errs.add(deleted);
         }
 
         outputErrors(errs, Configuration.CMD_UPLOAD);
     }
 
     private static PrepareConfiguration createPrepareConfiguration(Properties props, String[] args) {
         return createPrepareConfiguration(props, args, false);
     }
 
     private static PrepareConfiguration createPrepareConfiguration(Properties props, String[] args, boolean isUploaded) {
         PrepareConfiguration.Factory fact = new PrepareConfiguration.Factory(props, isUploaded);
         PrepareConfiguration conf = fact.newPrepareConfiguration(args);
 
         if (!isUploaded) {
             showHelp(conf, args);
            System.exit(0);
         }
 
         conf.configure(props, fact.getBulkImportOptions());
         return conf;
     }
 
     private static UploadConfiguration createUploadConfiguration(Properties props, String[] args) {
         UploadConfiguration.Factory fact = new UploadConfiguration.Factory(props);
         UploadConfiguration conf = fact.newUploadConfiguration(args);
 
         showHelp(conf, args);
 
         conf.configure(props, fact.getBulkImportOptions());
         return conf;
     }
 
     private static void showHelp(PrepareConfiguration conf, String[] args) {
         if (conf.hasHelpOption()) {
             try {
                 conf.showHelp();
             } catch (IOException e) {
                 throw new IllegalArgumentException(e);
             }
         }
     }
 
     private static void outputErrors(List<ErrorInfo> errs, String cmd) {
         int errSize = 0;
         for (ErrorInfo e : errs) {
             if (e.error != null) {
                 errSize++;
             }
         }
 
         if (errSize == 0) {
             return;
         }
 
         String msg = String.format("Some errors occurred during %s processing. " +
                 "Please check the following messages.", cmd);
         System.out.println(msg);
         LOG.warning(msg);
 
         for (ErrorInfo e : errs) {
             if (e.error != null) {
                 e.error.printStackTrace();
             }
         }
     }
 
     public static void main(final String[] args) throws Exception {
         if (args.length < 1) {
             throw new IllegalArgumentException("Command not specified");
         }
 
         String commandName = args[0];
         Properties props = System.getProperties();
         if (commandName.equals(Configuration.CMD_PREPARE)) {
             prepare(args, props);
         } else if (commandName.equals(Configuration.CMD_UPLOAD)) {
             upload(args, props);
         } else {
             throw new IllegalArgumentException(
                     String.format("Not support command %s", commandName));
         }
     }
 
 }
