 //
 // Java Extension to CUI for Treasure Data
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
 package com.treasure_data.commands.bulk_import;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Logger;
 
 import com.treasure_data.commands.Command;
 import com.treasure_data.commands.CommandException;
 import com.treasure_data.utils.FileParser;
 import com.treasure_data.utils.FileParserFactory;
 import com.treasure_data.utils.FileWriter;
 
 public class PreparePartsCommand extends
         Command<PreparePartsRequest, PreparePartsResult> {
     private static final Logger LOG = Logger
             .getLogger(PreparePartsCommand.class.getName());
 
     private static BlockingQueue<Worker.Task> taskQueue;
     private List<Worker> workers;
 
     public PreparePartsCommand() {
     }
 
     @Override
     public void execute(PreparePartsRequest request, PreparePartsResult result)
             throws CommandException {
         LOG.info("Execute " + request.getName() + " command");
 
         Properties props = request.getProperties();
         File[] files = request.getFiles();
 
         // create queue and put all tasks to it
         taskQueue = new LinkedBlockingQueue<Worker.Task>(files.length);
         for (int i = 0; i < files.length; i++) {
             try {
                 taskQueue.put(new Worker.Task(files[i]));
             } catch (InterruptedException e) {
                 throw new CommandException(e);
             }
         }
 
         // avaiable cpu processors
         int numOfProcs = Runtime.getRuntime().availableProcessors();
         LOG.info("Recognized CPU Processors: " + numOfProcs);
 
         // create worker threads
         workers = new ArrayList<Worker>(numOfProcs);
         for (int i = 0; i < numOfProcs; i++) {
             Worker w = new Worker(props, request, result);
             LOG.info("Created worker thread: " + w.getName());
             workers.add(w);
         }
         // start workers
         for (Worker w : workers) {
             w.start();
         }
 
         // join
         while (!workers.isEmpty()) {
             Worker lastWorker = workers.get(workers.size() - 1);
             try {
                 lastWorker.join();
             } catch (InterruptedException e) {
                 lastWorker.interrupt();
                 try {
                     lastWorker.join();
                 } catch (InterruptedException e2) {
                 }
             }
             workers.remove(workers.size() - 1);
         }
 
         LOG.info("Finish " + request.getName() + " command");
     }
 
     static class Worker extends Thread {
         static class Task {
             File file;
             Task(File file) {
                 this.file = file;
             }
         }
 
         Properties props;
         PreparePartsRequest request;
         PreparePartsResult result;
         public Worker(Properties props, PreparePartsRequest request,
                 PreparePartsResult result) {
             this.props = props;
             this.request = request;
             this.result = result;
         }
 
         @Override
         public void run() {
             while (true) {
                 Task t = taskQueue.poll();
                 if (t == null) {
                     break;
                 } else {
                     try {
                         execute(props, request, result, t.file);
                     } catch (CommandException e) {
                        LOG.severe(e.getMessage());
                         e.printStackTrace();
                     }
                 }
             }
         }
 
         private void execute(Properties props, PreparePartsRequest request,
                 PreparePartsResult result, final File file) throws CommandException {
             LOG.info("Read file: " + file.getName() + " by " + getName());
 
             FileParser p = FileParserFactory.newInstance(request, file);
             FileWriter w = new FileWriter(request, file);
             while (p.parseRow(w)) {
                 ;
             }
             p.close();
             w.close();
         }
     }
 }
