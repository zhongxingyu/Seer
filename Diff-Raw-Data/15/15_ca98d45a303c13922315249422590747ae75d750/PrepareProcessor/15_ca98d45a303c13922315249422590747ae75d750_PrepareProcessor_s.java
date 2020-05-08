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
 package com.treasure_data.bulk_import.prepare_parts;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Logger;
 
 public class PrepareProcessor {
 
     public static class Task {
         private static final String TAG = "__PREPARE_FINISH__";
         static final Task FINISH_TASK = new Task(TAG);
 
         String fileName;
 
         boolean isTest = false;
         String testText = null;
 
         public Task(String fileName) {
             this.fileName = fileName;
         }
 
         protected InputStream createInputStream(PrepareConfig.CompressionType compressionType)
                 throws IOException {
             if (!isTest) {
                 return compressionType.createInputStream(new FileInputStream(fileName));
             } else {
                 return new ByteArrayInputStream(testText.getBytes());
             }
         }
 
         @Override
         public boolean equals(Object obj) {
             if (! (obj instanceof Task)) {
                 return false;
             }
 
             Task t = (Task) obj;
             return t.fileName.equals(fileName);
         }
 
         static boolean endTask(Task t) {
             return t.equals(FINISH_TASK);
         }
     }
 
     public static class ErrorInfo {
        Task task;
        Throwable error = null;
 
        long redRows = 0;
        long writtenRows = 0;

        public ErrorInfo() {
        }
     }
 
     private static final Logger LOG = Logger.getLogger(
             PrepareProcessor.class.getName());
 
     protected PrepareConfig conf;
 
     public PrepareProcessor(PrepareConfig conf) {
         this.conf = conf;
     }
 
     public ErrorInfo execute(final Task task) {
         LOG.info(String.format("Convert file '%s'", task.fileName));
 
         // TODO #MN need type paramters
         ErrorInfo err = new ErrorInfo();
         err.task = task;
 
         FileParser p = null;
         MsgpackGZIPFileWriter writer = null;
         try {
             p = FileParser.newFileParser(conf);
             p.configure(task.fileName);
             p.sample(task.createInputStream(conf.getCompressionType()));
         } catch (Throwable t) {
             err.error = t;
         }
 
        if (conf.dryRun()) {
             if (p != null) {
                 p.closeSilently();
             }
 
             // if this processing is dry-run mode, thread of control
             // returns back
             return err;
         }
 
         try {
             writer = new MsgpackGZIPFileWriter(conf);
             writer.configure(task.fileName);
 
             p.setFileWriter(writer);
             p.parse(task.createInputStream(conf.getCompressionType()));
         } catch (Throwable t) {
             err.error = t;
         }
 
         if (p != null) {
             p.closeSilently();
         }
         if (writer != null) {
             writer.closeSilently();
         }
 
         err.redRows = p.getRowNum();
         err.writtenRows = writer.getRowNum();
 
         LOG.info(String.format("Converted file '%s', %d entries",
                 task.fileName, err.writtenRows));
 
         return err;
     }
 
 }
