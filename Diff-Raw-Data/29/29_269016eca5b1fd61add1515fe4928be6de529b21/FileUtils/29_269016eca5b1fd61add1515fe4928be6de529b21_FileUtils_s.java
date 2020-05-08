package com.grooveshark.hadoop.util;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.mapred.JobConf;
 
 public class FileUtils
 {
 
     public static List<String> readFile(String filename) throws Exception {
         BufferedReader br = new BufferedReader(new FileReader(filename));
         List<String> lines = new LinkedList<String>();
         String line = "";
         while ((line = br.readLine()) != null) {
             lines.add(line);
         }
         return lines;
     }
 
     public static List<String> readFile(String filename, int numLines) throws Exception {
         BufferedReader br = new BufferedReader(new FileReader(filename));
         List<String> lines = new LinkedList<String>();
         String line = "";
         int count = 1;
         while ((line = br.readLine()) != null && count <= numLines) {
             lines.add(line);
             count++;
         }
         return lines;
     }
 
     public static void writeLine(BufferedWriter writer, String s) throws IOException {
         writer.write(s);
         writer.newLine();
     }
 
     public static void writeLine(String filename, String s, boolean append) throws Exception {
         BufferedWriter writer = new BufferedWriter(new FileWriter(filename, append));
         writer.write(s);
         writer.newLine();
         writer.flush();
         writer.close();
     }
 
     public static boolean isHDFSFileExists(Path path, JobConf conf) throws IOException {
         FileSystem fs = path.getFileSystem(conf);
         if (fs.exists(path)) {
             return true;
         } else {
             return false;
         }
     }
 
     public static boolean deleteHDFSFile(Path path, JobConf conf) throws IOException {
         FileSystem fs = path.getFileSystem(conf);
         if (fs.delete(path, true)) {
             return true;
         } else {
             return false;
         }
     }

    public static void logToStdOut(String threadName, String msg) {
        System.out.println(DateUtils.getNow() + ": [" + threadName + "] " + msg);
    }
 }
