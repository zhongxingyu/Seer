 package org.liquibase.doc.check;
 
 import liquibase.util.file.FilenameUtils;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class DocChecker {
     public static void main(String[] args) throws Exception {
         Set<File> files = new HashSet<File>();
         findFiles(new File("_site"), files);
 
         Set<String> seenFiles = new HashSet<String>();
         for (File file : files) {
             checkLinks(file, seenFiles);
             checkContent(file);
         }
         for (File file : files) {
             if (!file.getAbsolutePath().contains("javadoc") && !seenFiles.contains(file.getCanonicalPath())) {
                 System.out.println("Found no references to "+file.getAbsolutePath());
             }
         }
     }
 
     private static void checkContent(File file) throws IOException {
         BufferedReader reader = new BufferedReader(new FileReader(file));
         String line;
         String contents = "";
         while ((line = reader.readLine()) != null) {
             contents += line;
         }
 
        if (contents.contains("Unknown nav")) {
             System.out.println("Problem with navigation in "+file.getAbsolutePath());
         }
 
 
     }
 
     private static void checkLinks(File file, Set<String> seenFiles) throws Exception {
         BufferedReader reader = new BufferedReader(new FileReader(file));
         String line;
         String contents = "";
         while ((line = reader.readLine()) != null) {
             contents += line;
         }
        Matcher matcher = Pattern.compile("href=[\"']?([\\w:/\\.\\-]+)[\"']?").matcher(contents);
        while (matcher.find()) {
            String url = matcher.group(1);
             File dir = file.getParentFile();
             if (url.startsWith("http")) {
                 continue;
             }
             if (url.startsWith("mailto")) {
                 continue;
             }
             if (url.endsWith(".md")) {
                 System.out.println("Reference to markdown "+url+" in "+file.getAbsolutePath());
             }
             File referencedFile = new File(dir, url);
             try {
                 seenFiles.add(referencedFile.getCanonicalPath());
             } catch (IOException e) {
                 System.out.println("Bad file "+referencedFile);
                 e.printStackTrace();
                 continue;
             }
             if (referencedFile.isDirectory()) {
                 System.out.println("Reference to directory "+url+" in "+file.getAbsolutePath());
             }
             if (!referencedFile.exists()) {
                 System.out.println("Reference to missing "+url+" in "+file.getAbsolutePath());
             }
         }
     }
 
     private static void findFiles(File dir, Set<File> files) {
         files.addAll(Arrays.asList(dir.listFiles(new FilenameFilter() {
             @Override
             public boolean accept(File dir, String name) {
                 return name.endsWith(".html");
             }
         })));
 
         for (File subdir : dir.listFiles(new FileFilter() {
             @Override
             public boolean accept(File pathname) {
                 return pathname.isDirectory();
             }
         })) {
             findFiles(subdir, files);
         }
     }
 }
