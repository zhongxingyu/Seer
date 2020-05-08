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
 package com.treasure_data.commands;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Logger;
 
 
 public class MultithreadsCommand<REQ extends CommandRequest, RET extends CommandResult>
         extends Command<REQ, RET> {
     private static final Logger LOG = Logger
             .getLogger(MultithreadsCommand.class.getName());
 
     protected Command<REQ, RET> command;
 
    private BlockingQueue<Worker.Task> taskQueue;
     private List<Worker<REQ, RET>> workers;
 
     public MultithreadsCommand(Command<REQ, RET> command) {
         this.command = command;
     }
 
     @Override
     public void execute(REQ request, RET result, File file)
             throws CommandException {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void preExecute(REQ request, RET result) throws CommandException {
     }
 
     @Override
     public void postExecute(REQ request, RET result) throws CommandException {
     }
 
     @Override
     public void execute(REQ request, RET result) throws CommandException {
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
         LOG.fine("recognized CPU processors: " + numOfProcs);
 
         // create worker threads
         workers = new ArrayList<Worker<REQ, RET>>(numOfProcs);
         for (int i = 0; i < numOfProcs; i++) {
            Worker<REQ, RET> w = new Worker<REQ, RET>(
                    this, command, request, result);
             LOG.fine("created worker thread: " + w.getName());
             workers.add(w);
         }
         // start workers
         for (Worker<REQ, RET> w : workers) {
             w.start();
         }
 
         // join
         while (!workers.isEmpty()) {
             Worker<REQ, RET> lastWorker = workers.get(workers.size() - 1);
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
 
         // TODO #MN should consider the design of {pre,post}Execute()
         command.postExecute(request, result);
     }
 
     static class Worker<REQ extends CommandRequest, RET extends CommandResult>
             extends Thread {
         static class Task {
             File file;
             Task(File file) {
                 this.file = file;
             }
         }
 
        MultithreadsCommand<REQ, RET> parent;
         Command<REQ, RET> command;
         REQ request;
         RET result;
 
        public Worker(MultithreadsCommand<REQ, RET> parent,
                Command<REQ, RET> command, REQ request, RET result) {
            this.parent = parent;
             this.command = command;
             this.request = request;
             this.result = result;
         }
 
         @Override
         public void run() {
             while (true) {
                Task t = parent.taskQueue.poll();
                 if (t == null) {
                     break;
                 } else {
                     try {
                         RET cloned = (RET) result.clone();
                         command.execute(request, cloned, t.file);
                        //command.execute(request, result, t.file);
                     } catch (CommandException e) {
                         LOG.severe(String.format("failed command by %s: %s",
                                 getName(), e.getMessage()));
                         e.printStackTrace();
                     }
                 }
             }
         }
     }
 }
