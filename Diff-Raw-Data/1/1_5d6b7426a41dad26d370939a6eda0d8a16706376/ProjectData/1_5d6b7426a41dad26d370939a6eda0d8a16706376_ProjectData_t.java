 package org.nauxiancc.projects;
 
 import org.nauxiancc.configuration.Global.Paths;
 
 import java.io.File;
 import java.util.LinkedList;
 
 /**
  * Loads locally found runners for the updater and for the loader. This will
  * retrieve all runners found in the respective categories.
  *
  * @author Naux
  * @since 1.0
  */
 
 public class ProjectData {
 
     public static final LinkedList<Project> DATA = new LinkedList<>();
 
     private ProjectData() {
     }
 
     /**
      * This does a sweep of the source folder to get all 5 categories and
      * respective Runners inside of them. This is should only be called to check
      * for currently loaded Runners, then to add all the updated versions of
      * Runners, if any. The key for the HashMap is the category, with the value
      * being a list of runners.
      *
      * @since 1.0
      */
 
     public static void loadCurrent() {
        DATA.clear();
         final File root = new File(Paths.SOURCE);
         if (!root.exists()) {
             return;
         }
         final String[] list = root.list();
         for (final String name : list) {
             if (name != null) {
                 final File file = new File(root, name);
                 final int idx = name.indexOf("Runner.class");
                 if (idx == -1) {
                     continue;
                 }
                 try {
                     DATA.add(new Project(name.substring(0, idx), file));
                 } catch (final Exception e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
 }
