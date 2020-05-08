 package uk.ac.ebi.fgpt.sampletab.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import com.google.common.io.Files;

 public class FileUtils {
     // static logger must have name hand-written
     private static Logger log = LoggerFactory.getLogger("uk.ac.ebi.fgpt.sampletab.utils.FileUtils");
 
     // TODO javadoc
     public static List<File> getParentFiles(File start) {
         List<File> parents = new ArrayList<File>();
         parents.add(new File(start.getName()));
         File totest = start;
         while (totest.getParentFile() != null) {
             totest = totest.getParentFile();
             parents.add(0, new File(totest.getName()));
         }
         return parents;
     }
 
     // TODO javadoc
     public static File joinFileList(List<File> files) {
         File out = files.get(0);
         int i = 1;
         while (i < files.size()) {
             out = new File(out, files.get(i).getName());
             i++;
         }
         return out;
     }
 
     // TODO javadoc
     public static String globToRegex(String glob) {
         StringBuilder sb = new StringBuilder(glob.length());
         for (char currentChar : glob.toCharArray()) {
             switch (currentChar) {
                 case '*':
                     sb.append(".*");
                     break;
                 case '.':
                     sb.append("\\.");
                     break;
                 case '?':
                     sb.append("\\?");
                     break;
                 default:
                     sb.append(currentChar);
                     break;
             }
         }
         return sb.toString();
     }
 
     // TODO javadoc
     public static List<File> getMatchesGlob(String glob) {
         log.debug("glob = " + glob);
         return getMatchesRegex(globToRegex(glob));
     }
     
     public static List<File> getMatchesRegex(String regex) {
         log.debug("regex = " + regex);
         List<File> outfiles = new ArrayList<File>();
         
         File regfile = new File(regex);
         regfile = regfile.getAbsoluteFile();
         List<String> regparts = new ArrayList<String>();
         regparts.add(regfile.getName());
         File totest = regfile;
         while (totest.getParentFile() != null) {
             totest = totest.getParentFile();
             regparts.add(0, totest.getName());
         }
         //trim root
         if (regparts.get(0).equals("")){
         	regparts = regparts.subList(1, regparts.size());
         }
         
         log.debug("regparts= " + regparts);
         
         File root = regfile;
         while (root.getParentFile() != null){
         	root = root.getParentFile();
         }
         File[] subfiles = root.listFiles();
         Arrays.sort(subfiles);
         for (File subfile : subfiles) {
             lookAt(subfile, regparts, outfiles);
         }
         
         return outfiles;
     }
 
     private static void lookAt(File tolook, List<String> regparts, List<File> outfiles){
         log.debug("tolook : "+tolook);
         log.debug("regparts.get(0) : "+regparts.get(0));
         log.debug("matches : "+tolook.getName().matches(regparts.get(0)));
         if (tolook.getName().matches(regparts.get(0))){
             if (regparts.size() == 1){
                 outfiles.add(tolook);
             } else {
                 log.debug("isDirectory() : "+tolook.isDirectory());
                 log.debug("listFiles() : "+tolook.listFiles());
                 if (tolook.isDirectory()) {
                     File[] subfiles = tolook.listFiles();
                     Arrays.sort(subfiles);
                     for (File subfile : subfiles) {
                         lookAt(subfile, regparts.subList(1, regparts.size()), outfiles);
                     }
                 }
             }
         }
     }
     
     public static void copy(File sourceFile, File destFile) throws IOException {
        Files.copy(sourceFile, destFile);
    }
    
    public static void move(File sourceFile, File destFile) throws IOException {
        Files.move(sourceFile, destFile);
     }
 }
