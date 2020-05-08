 package com.polopoly.javarebel.fs;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.NativeArray;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.tools.shell.Global;
 
 public class JSFS implements FS {
 
     File pack;
     JSConfig config;
     private final String targetImplicitBase;
     
     public JSFS(File pack, String targetImplicitBase)
     {
         this.config = new JSConfig(pack);
         this.targetImplicitBase = targetImplicitBase;
         this.pack = pack;
     }
 
     public Object[] getFileInfo(String path) throws IOException
     {
         if (path == null) {
             return null;
         }
         if (targetImplicitBase != null) {
             if (!path.startsWith(targetImplicitBase)) {
                 return null;
             }
             path = path.substring(targetImplicitBase.length());
         }
         int index = path.lastIndexOf('/');
         String dir = index == -1 ? "/" : path.substring(0, index);
         String name = path.substring(index+1);
        if (getFiles(name) == null) {
             return null;
         }
         boolean isDirectory = false;
         long lastModified = getLastModified(path);
         long size = getFileContent(path).length;
         return new Object[] { dir, name, isDirectory, lastModified, size } ;
     }
 
     public boolean exportFile(String path, OutputStream out) throws IOException
     {
         if (targetImplicitBase != null) {
             if (!path.startsWith(targetImplicitBase)) {
                 return false;
             }
             path = path.substring(targetImplicitBase.length());
         }
         byte[] file = getFileContent(path);
         if (file == null) {
             return false;
         }
         out.write(file);
         return true;
     }
     
     private long getLastModified(String path) throws IOException
     {
         long lastModified = pack.lastModified();
         for (File file : getFiles(path)) {
             long thisModified = file.lastModified();
             if (thisModified > lastModified) {
                 lastModified = thisModified;
             }
         }
         return lastModified;
     }
     
     private byte[] getFileContent(String path) throws IOException
     {
         File[] files = getFiles(path);
         if (files == null) {
             return null;
         }
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         boolean first = true;
         for (File file : files) {
             if (first) {
                 first = false;
             } else {
                 baos.write((int) '\n');
             }
             FSUtil.pipe(new FileInputStream(file), baos);
         }
         return baos.toByteArray();
     }
 
     public File[] getFiles(String path) throws IOException 
     {
         return config.getFiles(path);
     }
 
     private static class JSConfig {
         private Scriptable scope;
         private File pack;
         private Map<String, File[]> cache = new HashMap<String, File[]>();
         private long lastAccess = -1;
 
         public JSConfig(File pack) {
             this.pack = pack;
             Context cx = Context.enter();
             cx.setOptimizationLevel(9);
             Global global = new Global();
             global.init(cx);
             scope = cx.initStandardObjects(global);
             Context.exit();
         }
 
         public File[] getFiles(String uri) throws IOException
         {
             String absolute = pack.getParentFile().getAbsolutePath();
             int start = 0;
             if (uri.startsWith("/")) { 
                 start = 1;
             }
             int end = uri.length();
             if (uri.endsWith(".js")) {
                 end = end - 3;
             }
             String name = uri.substring(start, end);
             return files(absolute, name);
         }
 
         private synchronized File[] files(String absolute, String name) throws IOException
         {
             if (pack.lastModified() <= lastAccess) {
                 return cache.get(name);
             }
             cache.clear();
             lastAccess = pack.lastModified();
             Context cx = Context.enter();
             cx.evaluateReader(scope, new FileReader(pack), pack.getName(), 0, null);
             NativeArray names = (NativeArray) cx.evaluateString(scope, "var names = [] ; for (name in deps) { names.push(name); } ; names", "getNames(" + name + ")", 1, null);
             for (int i = 0 ; i < names.getLength() ; i++) {
                 if (names.get(i, scope) instanceof String) {
                     String packName = (String) names.get(i, scope);
                     NativeArray deps = (NativeArray) cx.evaluateString(scope, "deps['"+packName+"']", "getFiles(" + packName + ")", 1, null);
                     List<File> files = new ArrayList<File>();
                     for (int j = 0 ; j < deps.getLength() ; j++) {
                         files.add(new File(absolute + "/" + (String) deps.get(j, scope) + ".js"));
                     }
                     cache.put(packName, files.toArray(new File[0]));
                 }
             }
             Context.exit();
             return cache.get(name);
         }
     }
 }
