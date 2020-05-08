 package com.atlassian.labs.telamon.script;
 
 import com.atlassian.labs.telamon.script.ScriptSource;
 import com.atlassian.labs.telamon.script.ScriptSourceLoader;
 import com.atlassian.labs.telamon.script.FileScriptSource;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.io.File;
 
 /**
  *
  */
 public class DirectoryScriptSourceLoader implements ScriptSourceLoader {
     private final File baseDir;
     private final boolean reloadable;
 
     public DirectoryScriptSourceLoader(File baseDir) {
             this(baseDir, false);
     }
 
     public DirectoryScriptSourceLoader(File baseDir, boolean reloadable) {
         this.baseDir = baseDir;
         this.reloadable = reloadable;
     }
 
     public List<ScriptSource> getSources() {
         List<ScriptSource> sources = new ArrayList<ScriptSource>();
         if (baseDir == null || !baseDir.exists())
         {
             throw new IllegalArgumentException("The baseDir is null or doesn't exist");
         }
         for (File jsfile : baseDir.listFiles())
         {
             if (jsfile.getName().endsWith(".js"))
             {
                 sources.add(new FileScriptSource(jsfile));
             }
         }
 
         Collections.sort(sources, new Comparator<ScriptSource>()
         {
             public int compare(ScriptSource first, ScriptSource second) {
                return second.getPath().compareTo(first.getPath());
             }
         });
 
         return sources;
     }
 
     public ScriptSource getSource(String name) {
         File file = new File(baseDir, name + ".js");
         if (file.exists())
         {
             return new FileScriptSource(file);
         }
         return null;
     }
 
     public boolean getSupportsReload() {
         return reloadable;
     }
 }
