 package com.github.enr.xite.util;
 
 import java.io.File;
 import java.io.IOException;
 
 public class FileTraverser {
     public final void traverse(final File f) throws IOException {
         if (f.isDirectory()) {
             onDirectory(f);
             final File[] childs = f.listFiles();
             for (File child : childs) {
                traverse(child);
             }
             return;
         }
         onFile(f);
     }
 
     public void onDirectory(final File d) {
     }
 
     public void onFile(final File f) {
     }
 }
