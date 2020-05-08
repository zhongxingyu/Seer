 package org.makumba.parade.controller;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Vector;
 
 import org.makumba.parade.tools.HtmlUtils;
 import org.makumba.parade.tools.ParadeJNotifyListener;
 import org.makumba.parade.model.managers.CVSManager;
 import org.makumba.parade.model.managers.FileManager;
 import org.makumba.parade.tools.Execute;
 
 public class CvsController {
     
     public static Object[] onCheck(String context, String[] params) {
         String absolutePath = params[0];
         java.io.File f = new java.io.File(absolutePath);
         if(!f.exists() || !f.canRead()) {
             Object[] obj = {"Internal ParaDe error: path is not accessible", new Boolean(false)};
             return obj;
         }
         
         Vector cmd = new Vector();
         
         cmd.add("cvs");
         cmd.add("-n");
         cmd.add("-l");
         cmd.add("update");
         cmd.add("-P");
         cmd.add("-d");
//        
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
         
         Execute.exec(cmd, f, getPrintWriterCVS(out));
         CVSManager.updateCvsCache(context, absolutePath, true);
         
         Object[] res = {result.toString(), new Boolean(true)};
         
         return res;
     }
     
     public static Object[] onUpdate(String context, String[] params) {
         String absolutePath = params[0];
         java.io.File f = new java.io.File(absolutePath);
         if(!f.exists() || !f.canRead()) {
             Object[] obj = {"Internal ParaDe error: path is not accessible", new Boolean(false)};
             return obj;
         }
         
         Vector cmd = new Vector();
         cmd.add("cvs");
         cmd.add("update");
         cmd.add("-P");
         cmd.add("-d");
         cmd.add("-l");
         
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
         
         ParadeJNotifyListener.createDirectoryLock(f.getAbsolutePath());
         Execute.exec(cmd, f, getPrintWriterCVS(out));
         ParadeJNotifyListener.removeDirectoryLock(f.getAbsolutePath());
         
         // cvs update modifies state of file and of cvs data, locally
         FileManager.updateDirectoryCache(context, absolutePath, true);
         CVSManager.updateCvsCache(context, absolutePath, true);
         
         
         Object[] res = {result.toString(), new Boolean(true)};
         
         return res;
     }
 
     public static Object[] onRUpdate(String context, String[] params) {
         String absolutePath = params[0];
         java.io.File f = new java.io.File(absolutePath);
         if(!f.exists() || !f.canRead()) {
             Object[] obj = {"Internal ParaDe error: path is not accessible", new Boolean(false)};
             return obj;
         }
         Vector cmd = new Vector();
         cmd.add("cvs");
         cmd.add("update");
         cmd.add("-P");
         cmd.add("-d");
         
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
         
         ParadeJNotifyListener.createDirectoryLock(f.getAbsolutePath());
         Execute.exec(cmd, f, getPrintWriterCVS(out));
         ParadeJNotifyListener.removeDirectoryLock(f.getAbsolutePath());
         
         // cvs recursive update modifies state of file and of cvs data, recursively
         FileManager.updateDirectoryCache(context, absolutePath, false);
         CVSManager.updateCvsCache(context, absolutePath, false);
         
         Object[] res = {result.toString(), new Boolean(true)};
         
         return res;
     }
     
     public static Object[] onCommit(String context, String[] params) {
         String absolutePath = params[0];
         String absoluteFilePath = params[1];
         String message = params[2];
         java.io.File f = new java.io.File(absoluteFilePath);
         java.io.File p = new java.io.File(absolutePath);
         
         Vector cmd = new Vector();
         cmd.add("cvs");
         cmd.add("commit");
         cmd.add("-m");
         cmd.add("\""+message+"\"");
         cmd.add(f.getName());
         
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
         
         Execute.exec(cmd, p, getPrintWriterCVS(out));
         
         CVSManager.updateSimpleCvsCache(context, absoluteFilePath);
         
         // in case of a cvs commit that leads to the deletion of a file of the repository
         // we need to make sure that the file gets removed from the cache or it will appear as "zombie"
         
         FileManager.checkShouldCache(context, absolutePath, absoluteFilePath);   
         Object[] res = {result.toString(), new Boolean(true)};
         
         return res;
     }
     
     public static Object[] onDiff(String context, String absolutePath, String file) {
         java.io.File f = new java.io.File(file);
         java.io.File p = new java.io.File(absolutePath);
         if(!f.exists() || !f.canRead() || !p.exists() || !p.canRead()) {
             Object[] obj = {"Internal ParaDe error: file is not accessible", new Boolean(false)};
             return obj;
         }
         Vector cmd = new Vector();
         cmd.add("cvs");
         cmd.add("diff");
         cmd.add(f.getName());
         
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
         
         Execute.exec(cmd, p, getPrintWriterCVS(out));
         
         Object[] res = {result.toString(), new Boolean(true)};
         
         return res;
     }
     
     public static Object[] onAdd(String context, String absolutePath, String file) {
         java.io.File f = new java.io.File(file);
         java.io.File p = new java.io.File(absolutePath);
         if(!f.exists() || !f.canRead() || !p.exists() || !p.canRead()) {
             Object[] obj = {"Internal ParaDe error: file is not accessible", new Boolean(false)};
             return obj;
         }
         Vector cmd = new Vector();
         cmd.add("cvs");
         cmd.add("add");
         cmd.add(f.getName());
         
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
         
         ParadeJNotifyListener.createFileLock(f.getAbsolutePath());
         Execute.exec(cmd, p, getPrintWriterCVS(out));
         CVSManager.updateSimpleCvsCache(context, f.getAbsolutePath());
         ParadeJNotifyListener.removeFileLock(f.getAbsolutePath());
         
         Object[] res = {result.toString(), new Boolean(true)};
         
         return res;
     }
     
     public static Object[] onAddBinary(String context, String absolutePath, String file) {
         java.io.File f = new java.io.File(file);
         java.io.File p = new java.io.File(absolutePath);
         if(!f.exists() || !f.canRead() || !p.exists() || !p.canRead()) {
             Object[] obj = {"Internal ParaDe error: file is not accessible", new Boolean(false)};
             return obj;
         }
         Vector cmd = new Vector();
         cmd.add("cvs");
         cmd.add("add");
         cmd.add("-kb");
         cmd.add(f.getName());
         
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
         
         ParadeJNotifyListener.createFileLock(f.getAbsolutePath());
         Execute.exec(cmd, p, getPrintWriterCVS(out));
         CVSManager.updateSimpleCvsCache(context, f.getAbsolutePath());
         ParadeJNotifyListener.removeFileLock(f.getAbsolutePath());
         
         Object[] res = {result.toString(), new Boolean(true)};
         
         return res;
     }
     
     public static Object[] onUpdateFile(String context, String absolutePath, String absoluteFilePath) {
         java.io.File f = new java.io.File(absoluteFilePath);
         java.io.File p = new java.io.File(absolutePath);
         
         Vector cmd = new Vector();
         cmd.add("cvs");
         cmd.add("update");
         cmd.add(f.getName());
         
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
         
         ParadeJNotifyListener.createFileLock(absoluteFilePath);
         Execute.exec(cmd, p, getPrintWriterCVS(out));
         FileManager.updateSimpleFileCache(context, p.getAbsolutePath(), f.getName());
         CVSManager.updateSimpleCvsCache(context, absoluteFilePath);
         ParadeJNotifyListener.removeFileLock(absoluteFilePath);
         
         Object[] res = {result.toString(), new Boolean(true)};
         
         return res;
     }
     
     public static Object[] onDeleteFile(String context, String absolutePath, String absoluteFilePath) {
         java.io.File f = new java.io.File(absoluteFilePath);
         java.io.File p = new java.io.File(absolutePath);
         
         Vector cmd = new Vector();
         cmd.add("cvs");
         cmd.add("delete");
         cmd.add(f.getName());
         
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
         
         ParadeJNotifyListener.createFileLock(absoluteFilePath);
         Execute.exec(cmd, p, getPrintWriterCVS(out));
         FileManager.updateSimpleFileCache(context, absolutePath, f.getName());
         CVSManager.updateSimpleCvsCache(context, absoluteFilePath);
         ParadeJNotifyListener.removeFileLock(absoluteFilePath);
         
         Object[] res = {result.toString(), new Boolean(true)};
         
         return res;
     }
     
     /* displays output with colors */
     public static PrintWriter getPrintWriterCVS(PrintWriter out) {
             final PrintWriter o = out;
             return new PrintWriter(new ByteArrayOutputStream()) {
                 public void print(String s) {
                     try {
                         o.print(s);
                     } catch (Throwable t) {
                         t.printStackTrace();
                     }
                 }
     
                 public void println(String s) {
                     try {
                         String style = "color:#444444"; // default
                         if (s.startsWith("M "))
                             style = "color:blue"; // modified
                         if (s.startsWith("A "))
                             style = "color:purple"; // added
                         if (s.startsWith("R "))
                             style = "color:purple"; // removed
                         if (s.startsWith("U "))
                             style = "color:green"; // updated
                         if (s.startsWith("P "))
                             style = "color:green"; // patched
                         if (s.startsWith("C "))
                             style = "color:red; font:bold"; // conflict
                         if (s.startsWith("? "))
                             style = "color:purple"; // unknown
                         if (s.startsWith("< "))
                             style = "background:#ffdddd"; // content removed
                         if (s.startsWith("> "))
                             style = "background:lightblue"; // content added
                         if (s.startsWith("exec: "))
                             style = "color:black";
                         if (s.endsWith("was lost"))
                             style = "color: brown; background:pink";
                         if (s.endsWith(" -- ignored"))
                             style = "color: green";
                         o.println("<span style=\"" + style + "\">"
                                 + HtmlUtils.string2html(s) + "</span><br>");
                         o.flush();
                     } catch (Throwable t) {
                         t.printStackTrace();
                     }
                 }
             };
         }
 
 }
