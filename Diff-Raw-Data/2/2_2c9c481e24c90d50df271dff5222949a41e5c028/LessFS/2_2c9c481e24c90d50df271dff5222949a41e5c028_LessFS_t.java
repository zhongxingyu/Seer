 package com.polopoly.javarebel.fs;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.zeroturnaround.javarebel.LoggerFactory;
 
 import com.asual.lesscss.LessEngine;
 import com.asual.lesscss.LessException;
 
 public class LessFS implements FS {
 
     File base;
     static LessEngine engine = new LessEngine();
 
     public LessFS(File base)
     {
         this.base = base;
     }
 
     public Object[] getFileInfo(String path) throws IOException
     {
         if (path == null) {
             return null;
         }
         if (!path.endsWith(".css")) {
             return null;
         }
         String less = path.substring(0, path.length() - 3) + "less";
         File file = new File(base.getAbsolutePath() + "/" + less);
         if (!file.exists() || !file.isFile()) {
             return null;
         }
         int index = path.lastIndexOf('/');
         String dir = index == -1 ? "/" : path.substring(0, index);
         String name = path.substring(index+1);
         boolean isDirectory = file.isDirectory();
         long lastModified = file.lastModified();
         long size;
         try {
             size = compile(file).length();
             return new Object[] { dir, name, isDirectory, lastModified, size } ;
         } catch (LessException e) {
             LoggerFactory.getInstance().error(e);
             throw new IOException(e);
         }
     }
 
     public boolean exportFile(String path, OutputStream out) throws IOException
     {
         if (path == null) {
             return false;
         }
         if (!path.endsWith(".css")) {
             return false;
         }
         String less = path.substring(0, path.length() - 3) + "less";
         File file = new File(base.getAbsolutePath() + "/" + less);
         if (!file.exists() || !file.isFile()) {
             return false;
         }
         try {
             out.write(compile(file).getBytes("UTF-8"));
             return true;
         } catch (LessException e) {
             LoggerFactory.getInstance().error(e);
             throw new IOException(e);
         }
     }
 
     private String compile(File file) throws LessException, IOException
     {
        return engine.compile(file).replaceAll("\\\\n", "\n");
     }
 }
