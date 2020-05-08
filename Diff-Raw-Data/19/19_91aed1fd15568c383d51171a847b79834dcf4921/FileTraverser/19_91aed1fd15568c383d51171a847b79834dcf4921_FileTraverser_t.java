 package com.github.enr.xite.util;
 
 import java.io.File;
 import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
 
 public class FileTraverser {
     public final void traverse(final File f) throws IOException {
        List<File> files = Lists.newArrayList();
        List<File> directories = Lists.newArrayList();
         if (f.isDirectory()) {
             onDirectory(f);
             final File[] childs = f.listFiles();
             for (File child : childs) {
                if (child.isDirectory()) {
                    directories.add(child);
                } else {
                    files.add(child);
                }
            }
            for (File dir : directories) {
                traverse(dir);
            }
            for (File file : files) {
                traverse(file);
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
