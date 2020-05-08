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
 
 import javax.xml.namespace.QName;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.HashSet;
 
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
                     Set<File> toActivate = new HashSet<File>();
                     Set<File> newer = new HashSet<File>();
                     Set<File> unknown = new HashSet<File>();
                     Set<File> removed = new HashSet<File>(cbps);
 
                     for (File script : scripts) {
                         String scriptRelative = noExt(relativePath(_scriptsDir, script));
 
                         // Can't easily reuse findNextVersion as we also want to know about removed scripts
                         int oldies = 0;
                         int scriptCbps = 0;
                         for (File cbp : cbps) {
                             String cbpRelative = noVerExt(relativePath(_workDir, cbp));
                             if (scriptRelative.equals(cbpRelative)) {
                                 removed.remove(cbp);
                                 scriptCbps++;
                                 if (cbp.lastModified() < script.lastModified()) oldies++;
                             }
                         }
                         if (scriptCbps > 0) {
                             if (oldies == scriptCbps) newer.add(script);
                             else if (firstRun) toActivate.add(script);
                         } else unknown.add(script);
                     }
 
                     // Newer and unknown processes both just need compile (at least for now)
                     newer.addAll(unknown);
                     for (File p : newer) {
                         __log.debug("Recompiling " + p);
                         ProcessModel oprocess = compileProcess(p, cbps);
                         __log.info("Process " + oprocess.getQName().getLocalPart()  + " deployed successfully.\n");
                     }
 
                     for (File p : toActivate) {
                         reloadProcess(p, cbps);
                     }
 
                     for (File p : removed) {
                         Serializer ser = new Serializer(new FileInputStream(p));
                         ProcessModel oprocess = ser.readPModel();
                         QName pid = toPid(oprocess.getQName(), versionFromCbpName(p.getName()));
                         fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.UNDEPLOYED, pid, null));
                         _processes.remove(pid);
                         _descriptors.remove(pid);
                         p.delete();
                     }
 
                     try {
                         Thread.sleep(POLLING_FREQ);
                     } catch (InterruptedException e) {
                         // whatever
                         e.printStackTrace();
                     }
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
 
         private ProcessModel compileProcess(File pfile, List<File> cbps) throws IOException, CompilationException {
             String radical = noExt(relativePath(_scriptsDir, pfile));
             int version = findNextVersion(cbps, radical);
 
             File targetCbp = new File(_workDir, radical + "-" + version + ".cbp");
             targetCbp.getParentFile().mkdirs();
 
             FileOutputStream cbpFos = new FileOutputStream(targetCbp, false);
             Descriptor desc = new Descriptor();
             ProcessModel oprocess = _compiler.compileProcess(pfile, desc);
             Serializer ser = new Serializer();
             ser.writePModel(oprocess, cbpFos);
             cbpFos.close();
 
             QName pid = toPid(oprocess.getQName(), version);
             _processes.put(pid, oprocess);
             _descriptors.put(pid, desc);
 
             fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.DEPLOYED, pid, null));
             fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.ACTIVATED, pid, null));
 
             return oprocess;
         }
 
         private void reloadProcess(File pfile, List<File> cbps) throws IOException, CompilationException {
             String radical = noExt(relativePath(_scriptsDir, pfile));
             int version = findNextVersion(cbps, radical) - 1;
 
             File targetCbp = new File(_workDir, radical + "-" + version + ".cbp");
            // Failed compilation
            if (targetCbp.length() == 0) return;

             Serializer ser = new Serializer(new FileInputStream(targetCbp));
             try {
                 ProcessModel pmodel = ser.readPModel();
                 Descriptor desc = _compiler.rebuildDescriptor(pmodel);
 
                 QName pid = toPid(pmodel.getQName(), version);
 
                 _processes.put(pid, pmodel);
                 _descriptors.put(pid, desc);
 
                 fireEvent(new ProcessStoreEvent(ProcessStoreEvent.Type.ACTIVATED, pid, null));
             } catch (Exception e) {
                 __log.error("Couldn't read compiled process file " + targetCbp.getAbsolutePath()
                         + ", it seems corrupted. As a result the process " + pfile.getAbsolutePath()
                         + " hasn't been reloaded.", e);
                 return;
             }
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
 
         private String noVerExt(String f) {
             return f.substring(0, f.lastIndexOf("-"));
         }
 
         private int findNextVersion(List<File> cbps, String radical) {
             int newVer = 0;
             for (File cbp : cbps) {
                 String cbpName = relativePath(_workDir, cbp);
                 if (cbpName.startsWith(radical)) {
                     int cbpVersion = versionFromCbpName(cbpName);
                     if (cbpVersion > newVer) newVer = cbpVersion;
                 }
             }
             return newVer + 1;
         }
 
         private QName toPid(QName pname, int version) {
             return new QName(pname.getNamespaceURI(), pname.getLocalPart() + "-" + version);
         }
 
         private int versionFromCbpName(String cbpName) {
             int dashIdx = cbpName.lastIndexOf("-");
             String verStr = cbpName.substring(dashIdx + 1, cbpName.lastIndexOf("."));
             try {
                 return Integer.parseInt(verStr);
             } catch (NumberFormatException e) {
                 e.printStackTrace();
                 return -1;
             }
         }
     }
 
 }
