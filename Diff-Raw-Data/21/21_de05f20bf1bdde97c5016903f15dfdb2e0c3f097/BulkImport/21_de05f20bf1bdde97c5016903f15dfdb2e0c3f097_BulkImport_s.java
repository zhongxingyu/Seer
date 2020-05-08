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
 package com.treasure_data.td_import;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import com.treasure_data.client.TreasureDataClient;
 import com.treasure_data.client.bulkimport.BulkImportClient;
 import com.treasure_data.model.bulkimport.SessionSummary;
 import com.treasure_data.td_import.TaskResult;
 import com.treasure_data.td_import.prepare.MultiThreadPrepareProcessor;
 import com.treasure_data.td_import.prepare.PrepareConfiguration;
 import com.treasure_data.td_import.upload.MultiThreadUploadProcessor;
 import com.treasure_data.td_import.upload.UploadConfiguration;
 import com.treasure_data.td_import.upload.UploadProcessor;
 
 public class BulkImport extends Import {
     private static final Logger LOG = Logger.getLogger(BulkImport.class.getName());
 
     public BulkImport(Properties props) {
         super(props);
     }
 
     public List<com.treasure_data.td_import.TaskResult<?>> doPrepare(final String[] args)
             throws Exception {
         // create prepare configuration
         PrepareConfiguration conf = createPrepareConf(args);
 
         // extract and get file names from command-line arguments
         String[] fileNames = getFileNames(conf, 1);
 
         MultiThreadPrepareProcessor proc =
                 createAndStartPrepareProcessor(conf);
 
         // create prepare tasks
         com.treasure_data.td_import.prepare.Task[] tasks =
                 createPrepareTasks(conf, fileNames);
 
         // start prepare tasks
         startPrepareTasks(conf, tasks);
 
         return stopPrepareProcessor(proc);
     }
 
     public List<com.treasure_data.td_import.TaskResult<?>> doUpload(final String[] args)
             throws Exception {
         // create configuration for 'upload' processing
         UploadConfiguration uploadConf = createUploadConf(args);
 
         // create TreasureDataClient and BulkImportClient objects
         TreasureDataClient tdClient = new TreasureDataClient(uploadConf.getProperties());
         BulkImportClient biClient = new BulkImportClient(tdClient);
 
         // configure session name
         TaskResult<?> r = null;
         String sessionName;
         int filePos;
         if (uploadConf.autoCreate()) { // 'auto-create-session'
             // create session automatically
             sessionName = createBulkImportSessionName(uploadConf, tdClient, biClient);
 
             filePos = 1;
         } else {
             // get session name from command-line arguments
             sessionName = getBulkImportSessionName(uploadConf);
 
             // validate that the session is live or not
             r = UploadProcessor.checkSession(biClient, uploadConf, sessionName);
             if (r.error != null) {
                 throw new IllegalArgumentException(r.error);
             }
 
             filePos = 2;
         }
 
         // if session is already freezed, exception is thrown.
         SessionSummary sess = UploadProcessor.showSession(biClient, uploadConf, sessionName);
         if (sess.uploadFrozen()) {
             throw new IllegalArgumentException(String.format(
                     "Bulk import session %s is already freezed. Please check it with 'td import:show %s'",
                     sessionName, sessionName));
         }
 
         // get and extract uploaded files from command-line arguments
         String[] fileNames = getFileNames(uploadConf, filePos);
 
         MultiThreadUploadProcessor uploadProc =
                 createAndStartUploadProcessor(uploadConf);
 
         List<com.treasure_data.td_import.TaskResult<?>> results =
                 new ArrayList<com.treasure_data.td_import.TaskResult<?>>();
 
         if (!uploadConf.hasPrepareOptions()) {
             // create upload tasks
             com.treasure_data.td_import.upload.UploadTask[] tasks =
                     createUploadTasks(sessionName, fileNames);
 
             // start upload tasks
             startUploadTasks(uploadConf, tasks);
         } else {
             // create configuration for 'prepare' processing
             PrepareConfiguration prepareConf = createPrepareConf(args, true);
 
             MultiThreadPrepareProcessor prepareProc =
                     createAndStartPrepareProcessor(prepareConf);
 
             // create sequential upload (prepare) tasks
             com.treasure_data.td_import.prepare.Task[] tasks = createSequentialUploadTasks(
                     sessionName, fileNames);
 
             // start sequential upload (prepare) tasks
             startPrepareTasks(prepareConf, tasks);
 
             results.addAll(stopPrepareProcessor(prepareProc));
             if (!hasNoPrepareError(results)) {
                 return results;
             }
 
             // end of file list
             try {
                 MultiThreadUploadProcessor.addFinishTask(uploadConf);
             } catch (Throwable t) {
                 LOG.severe("Error occurred During 'addFinishTask' method call");
                 LOG.throwing("Main", "addFinishTask", t);
             }
         }
 
         results.addAll(stopUploadProcessor(uploadProc));
         if (!hasNoUploadError(results)) {
             return results;
         }
 
         // 'auto-perform' and 'auto-commit'
        UploadProcessor.processAfterUploading(biClient, uploadConf, sessionName);
 
        if (uploadConf.autoDelete()) {
             // 'auto-delete-session'
            UploadProcessor.deleteSession(biClient, uploadConf, sessionName);
         }
 
         return results;
     }
 
     protected com.treasure_data.td_import.prepare.Task[] createPrepareTasks(
             final PrepareConfiguration conf,
             final String[] fileNames) {
         com.treasure_data.td_import.prepare.Task[] tasks =
                 new com.treasure_data.td_import.prepare.Task[fileNames.length];
         for (int i = 0; i < fileNames.length; i++) {
             tasks[i] = new com.treasure_data.td_import.prepare.Task(fileNames[i]);
         }
         return tasks;
     }
 
     protected com.treasure_data.td_import.prepare.Task[] createSequentialUploadTasks(
             final String sessionName,
             final String[] fileNames) {
         com.treasure_data.td_import.prepare.Task[] tasks =
                 new com.treasure_data.td_import.prepare.Task[fileNames.length];
         for (int i = 0; i < fileNames.length; i++) {
             tasks[i] = new com.treasure_data.td_import.prepare.SequentialUploadTask(
                     sessionName, fileNames[i]);
         }
         return tasks;
     }
 
     protected com.treasure_data.td_import.upload.UploadTask[] createUploadTasks(
             final String sessionName,
             final String[] fileNames) {
         com.treasure_data.td_import.upload.UploadTask[] tasks =
                 new com.treasure_data.td_import.upload.UploadTask[fileNames.length];
         for (int i = 0; i < fileNames.length; i++) {
             long size = new File(fileNames[i]).length();
             tasks[i] = new com.treasure_data.td_import.upload.UploadTask(
                     sessionName, fileNames[i], size);
         }
         return tasks;
     }
 
     protected void startUploadTasks(
             final UploadConfiguration conf,
             final com.treasure_data.td_import.upload.UploadTask[] tasks) {
         new Thread(new Runnable() {
             public void run() {
                 for (int i = 0; i < tasks.length; i++) {
                     try {
                         MultiThreadUploadProcessor.addTask(tasks[i]);
                     } catch (Throwable t) {
                         LOG.severe("Error occurred During 'addTask' method call");
                         LOG.throwing("Main", "addTask", t);
                     }
                 }
 
                 // end of file list
                 try {
                     MultiThreadUploadProcessor.addFinishTask(conf);
                 } catch (Throwable t) {
                     LOG.severe("Error occurred During 'addFinishTask' method call");
                     LOG.throwing("Main", "addFinishTask", t);
                 }
             }
         }).start();
     }
 
     protected String createBulkImportSessionName(UploadConfiguration conf,
             TreasureDataClient tdClient, BulkImportClient biClient) throws Exception {
         String databaseName = conf.enableMake()[0];
         String tableName = conf.enableMake()[1];
         Date d = new Date();
         String format = "yyyy_MM_dd";
         String timestamp = new SimpleDateFormat(format).format(d);
         String sessionName = String.format("%s_%s_%s_%d", databaseName, tableName,
                 timestamp, (d.getTime() / 1000));
 
         TaskResult<?> e = null;
         // validate that database is live or not
         e = UploadProcessor.checkDatabase(tdClient, conf,
                 sessionName, databaseName);
         if (e.error != null) {
             throw new IllegalArgumentException(e.error);
         }
 
         // validate that table is live or not
         e = UploadProcessor.checkTable(tdClient, conf,
                 sessionName, databaseName, tableName);
         if (e.error != null) {
             // TODO FIXME #MN should create table automatically if
             // it is not found.
             throw new IllegalArgumentException(e.error);
         }
 
         // create bulk import session
         e = UploadProcessor.createSession(biClient, conf, sessionName, databaseName, tableName);
         if (e.error != null) {
             throw new IllegalArgumentException(e.error);
         }
 
         return sessionName;
     }
 
     protected String getBulkImportSessionName(UploadConfiguration conf) {
         List<String> argList = conf.getNonOptionArguments();
         if (argList.size() < 1) {
             throw new IllegalArgumentException("Session name not specified");
         }
         return argList.get(1);
     }
 
     protected PrepareConfiguration createPrepareConf(String[] args) {
         return createPrepareConf(args, false);
     }
 
     protected PrepareConfiguration createPrepareConf(String[] args, boolean isUploaded) {
         PrepareConfiguration.Factory fact = new PrepareConfiguration.Factory(props, isUploaded);
         PrepareConfiguration conf = fact.newPrepareConfiguration(args);
 
         if (!isUploaded) {
             showHelp(Configuration.Command.PREPARE, conf, args);
         }
 
         conf.configure(props, fact.getBulkImportOptions());
         return conf;
     }
 
     protected UploadConfiguration createUploadConf(String[] args) {
         UploadConfiguration.Factory fact = new UploadConfiguration.Factory(props);
         UploadConfiguration conf = fact.newUploadConfiguration(args);
 
         showHelp(Configuration.Command.UPLOAD, conf, args);
 
         conf.configure(props, fact.getBulkImportOptions());
         return conf;
     }
 }
