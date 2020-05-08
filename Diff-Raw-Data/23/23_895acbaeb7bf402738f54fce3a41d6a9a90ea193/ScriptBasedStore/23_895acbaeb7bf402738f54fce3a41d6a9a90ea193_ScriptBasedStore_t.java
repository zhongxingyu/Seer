 /*
  * Simplex, lightweight SimPEL server
  * Copyright (C) 2008-2009  Intalio, Inc.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.intalio.simplex.lifecycle;
 
 import com.intalio.simpel.CompilationException;
 import com.intalio.simpel.Descriptor;
 import com.intalio.simplex.embed.EmbeddedStore;
 import org.apache.log4j.Logger;
 import org.apache.ode.bpel.iapi.ProcessStoreEvent;
 import org.apache.ode.bpel.rapi.ProcessModel;
 import org.apache.ode.bpel.rapi.Serializer;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ScriptBasedStore extends EmbeddedStore {
     private static final Logger __log = Logger.getLogger(ScriptBasedStore.class);
 
     public static long POLLING_FREQ = 2000;
 
     private File _scriptsDir;
     private File _workDir;
     private ScriptPoller _poller;
 
     public ScriptBasedStore(File scriptsDir, File workDir) {
         _scriptsDir = scriptsDir;
         _workDir = workDir;
     }
 
     @Override
     protected void start() {
         _poller = new ScriptPoller();
         Thread poller = new Thread(_poller);
         poller.setDaemon(true);
         poller.start();
     }
 
     protected void stop() {
         _poller.stop();
     }
 
     private class ScriptPoller implements Runnable {
         private boolean run = true;
         private boolean firstRun = true;
 
         private final FileFilter _scriptsFilter = new FileFilter() {
             public boolean accept(File path) {
                 return path.getName().endsWith(".simpel") && path.isFile();
             }
         };
         private final FileFilter _cbpFilter = new FileFilter() {
             public boolean accept(File path) {
                 return path.getName().endsWith(".cbp") && path.isFile();
             }
         };
 
         public void run() {
             while (run) {
                 try {
                     List<File> scripts = listFilesRecursively(_scriptsDir, _scriptsFilter);
                     List<File> cbps = listFilesRecursively(_workDir, _cbpFilter);
 
                     // This whole mumbo jumbo is just about populating these lists
                     List<File> newer = new ArrayList<File>();
                     List<File> unknown = new ArrayList<File>();
                     List<File> removed = new ArrayList<File>(cbps);
 
                     for (File script : scripts) {
                         String scriptRelative = noExt(relativePath(_scriptsDir, script));
 
                         boolean found = false;
                         for (File cbp : cbps) {
                             String cbpRelative = noExt(relativePath(_workDir, cbp));
                             if (scriptRelative.equals(cbpRelative)) {
                                 found = true;
                                 removed.remove(cbp);
                                 if (cbp.lastModified() < script.lastModified())
                                     newer.add(script);
                                 else if (firstRun)
                                     unknown.add(script);
                             }
                         }
 
                        if (!found) unknown.add(script);
                     }
 
                    ArrayList<File> toRebuild = new ArrayList<File>(unknown);
                    toRebuild.addAll(newer);
                     for (File p : toRebuild) {
                         __log.debug("Recompiling " + p);
                         ProcessModel oprocess = compileProcess(p);
                         __log.info("Process " + oprocess.getQName().getLocalPart()  + " deployed successfully.\n");
                     }
 
                     // Processes that haven't been activated yet (restart)
                     for (File p : unknown) {
                         __log.debug("Activating " + p);
                         ProcessModel oprocess = compileProcess(p);
                         __log.debug("Process " + oprocess.getQName().getLocalPart()  + " reactivated successfully.\n");
                     }
                     for (File p : removed) {
                         Serializer ser = new Serializer(new FileInputStream(p));
                         ProcessModel oprocess = ser.readPModel();
                         fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.UNDEPLOYED, oprocess.getQName(), null));
                         _processes.remove(oprocess.getQName());
                         p.delete();
                     }
 
                     try {
                         Thread.sleep(POLLING_FREQ);
                     } catch (InterruptedException e) {
                         // whatever
                         e.printStackTrace();
                     }
                    __log.info("Deployment successful.\n");
                 } catch (Throwable t) {
                     if (t instanceof CompilationException)
                         __log.info(t.getMessage() + "Deployment aborted.\n");
                     else
                         __log.error("Unexpected error during compilation.", t);
                 } finally {
                    firstRun = false;
                 }
             }
 
         }
 
         private void stop() {
             run = false;
         }
 
         private ProcessModel compileProcess(File pfile) throws IOException, CompilationException {
             File targetCbp = new File(_workDir, noExt(relativePath(_scriptsDir, pfile)) + ".cbp");
             targetCbp.getParentFile().mkdirs();
 
             FileOutputStream cbpFos = new FileOutputStream(targetCbp, false);
             Descriptor desc = new Descriptor();
             ProcessModel oprocess = _compiler.compileProcess(pfile, desc);
             Serializer ser = new Serializer();
             ser.writePModel(oprocess, cbpFos);
             cbpFos.close();
 
             _processes.put(oprocess.getQName(), oprocess);
             _descriptors.put(oprocess.getQName(), desc);
 
             fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.DEPLOYED, oprocess.getQName(), null));
             fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.ACTIVATED, oprocess.getQName(), null));
 
             return oprocess;
         }
 
         private ArrayList<File> listFilesRecursively(File root, FileFilter filter) {
             ArrayList<File> result = new ArrayList<File>();
             // Filtering the files we're interested in in the current directory
             File[] select = root.listFiles(filter);
             for (File file : select) {
                 result.add(file);
             }
             // Then we can check the directories
             File[] all = root.listFiles();
             for (File file : all) {
                 if (file.isDirectory())
                     result.addAll(listFilesRecursively(file, filter));
             }
             return result;
         }
 
         /**
          * Path of a file relative to a directory. The file has to be (indirectly) contained
          * in that directory.
          */
         private String relativePath(File toDir, File fromFile) {
             if (!fromFile.equals(toDir)) {
                 File parent = fromFile.getParentFile();
                 return relativePath(toDir, parent) + "/" + fromFile.getName();
             } else return "";
         }
 
         private String noExt(String f) {
             return f.substring(0, f.lastIndexOf("."));
         }
     }
 
 }
