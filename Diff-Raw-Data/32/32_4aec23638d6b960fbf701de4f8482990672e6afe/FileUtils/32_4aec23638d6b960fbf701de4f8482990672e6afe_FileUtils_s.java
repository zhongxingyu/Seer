 package org.jboss.jreadlineshell.util;
 
 import org.jboss.jreadline.util.Parser;
 import org.jboss.jreadlineshell.file.Prompt;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author <a href="mailto:stale.pedersen@jboss.org">Ståle W. Pedersen</a>
  */
 public class FileUtils {
 
     public static List<String> listMatchingDirectories(String possibleDir, Prompt prompt) {
                //System.out.println("looking for: " + possibleDir);
        //TODO: do not work if there is a filename/other directory
         // that starts with possibleDir
        List<String> returnFiles;
         if (possibleDir.trim().isEmpty()) {
             List<String> allFiles = listDirectory(prompt.getCwd());
            returnFiles = new ArrayList<String>();
             for (String file : allFiles)
                 if (file.startsWith(possibleDir))
                     returnFiles.add(file.substring(possibleDir.length()));
 
             return returnFiles;
         }
         else if (!possibleDir.startsWith("/") &&
                 new File(prompt.getCwd().getAbsolutePath() + "/" + possibleDir).isDirectory()) {
            //System.out.println("possibleDir is a dir, return those");
             if(!possibleDir.endsWith("/")) {
               returnFiles = new ArrayList<String>();
                 returnFiles.add("/");
                 return returnFiles;
             }
             else
                 return listDirectory(new File(prompt.getCwd().getAbsolutePath() + "/" + possibleDir));
         }
        else  if (new File(prompt.getCwd().getAbsolutePath() + "/" + possibleDir).isFile()) {
            returnFiles = new ArrayList<String>();
             returnFiles.add(" ");
             return returnFiles;
         }
        //should check if possibleDir contain /
        else { //if(possibleDir.contains("/")) {
             returnFiles = new ArrayList<String>();
             if(new File(possibleDir).isDirectory() && !possibleDir.endsWith("/")) {
                 returnFiles.add("/");
                 return returnFiles;
             }
 
             //1.list possibleDir.substring(pos
             String lastDir = null;
             String rest = null;
             if(possibleDir.contains("/")) {
                 lastDir = possibleDir.substring(0,possibleDir.lastIndexOf("/"));
                 rest = possibleDir.substring(possibleDir.lastIndexOf("/")+1);
             }
             else {
                 if(new File(prompt.getCwd()+"/"+possibleDir).exists())
                     lastDir = possibleDir;
                 else {
                     rest = possibleDir;
                 }
             }
             //System.out.println("rest:"+rest);
             //System.out.println("lastDir:"+lastDir);
 
             List<String> allFiles;
             if(possibleDir.startsWith("/"))
                 allFiles =  listDirectory(new File("/"+lastDir));
             else if(lastDir != null)
                 allFiles =  listDirectory(new File(prompt.getCwd()+"/"+lastDir));
             else
                 allFiles =  listDirectory(prompt.getCwd());
 
             //TODO: optimize
             //1. remove those that do not start with rest, if its more than one
             if(rest != null && !rest.isEmpty())
                 for (String file : allFiles)
                     if (file.startsWith(rest))
                         //returnFiles.add(file);
                         returnFiles.add(file.substring(rest.length()));
 
             if(returnFiles.size() > 1) {
                 String startsWith = Parser.findStartsWith(returnFiles);
                 if(startsWith != null && startsWith.length() > 0) {
                     returnFiles.clear();
                     returnFiles.add(startsWith);
                 }
                 //need to list complete filenames
                 else {
                     returnFiles.clear();
                     for (String file : allFiles)
                         if (file.startsWith(rest))
                             returnFiles.add(file);
                 }
             }
 
             return returnFiles;
 
         }



     }
 
     public static List<String> listDirectory(File path) {
         List<String> fileNames = new ArrayList<String>();
         if(path != null && path.isDirectory())
             for(File file : path.listFiles())
                 fileNames.add(file.getName());
 
         return fileNames;
     }
 
     public static String getDirectoryName(File path, File home) {
         if(path.getAbsolutePath().startsWith(home.getAbsolutePath()))
             return path.getAbsolutePath().substring(home.getAbsolutePath().length());
         else
             return path.getAbsolutePath();
     }
 }
