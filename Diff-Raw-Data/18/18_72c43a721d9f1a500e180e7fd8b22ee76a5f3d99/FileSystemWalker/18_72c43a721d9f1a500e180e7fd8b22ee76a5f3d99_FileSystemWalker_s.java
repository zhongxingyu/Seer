 package ru.spbau.mayorov.task2;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Arseny Mayorov.
  *         Date: 24.02.13
  */
 public class FileSystemWalker {
 
     /** Path for scanning root. */
     private String rootPath = null;
     /** Filename filter (must implement FilenameFilter interface). */
     private PatternFilter filter = null;
     /** Array used for storing result tree. */
     private ArrayList<String> output = new ArrayList<String>();
 
     /** Constructs FileSystemWalker for given root path and filename filter.
      *
      * @param path path to start from.
      * @param flt filename filter object.
      */
     public FileSystemWalker(final String path, final PatternFilter flt) {
          this.rootPath = path;
          this.filter = flt;
     }
 
     /** Returns strings representing formatted tree of filesystem structure.
      * May take a lot of time for large hierarchy.
      *
      * @return list of strings representing tree of filesystem structure.
      */
     public final List<String> listFiles() {
         File f = new File(rootPath);
         processFile(f, 0);
         ArrayList<String> result = output;
         output = null;
         return result;
     }
 
     /** Internal method used to scan directories recursively.
      *
      * @param f file descriptor.
      * @param level nesting level indicator.
      */
     private void processFile(final File f, final int level) {
 
         String name = new String();
         for (int i = 0; i < level; i++) {
             name += "|----";
         }
 
        name += f.getName();
 
        if (!f.canRead()) {
             name = f.getName() + " (access denied)";
         }
        output.add(name);
 
         File[] children = f.listFiles(filter);
 
         if (children == null) {
             return;
         }
 
         for (File child: children) {
             if (f.isDirectory()) {
                 processFile(child, level + 1);
             }
         }
 
     }
 }
