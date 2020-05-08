 package cmu.ds.mr.io;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import cmu.ds.mr.conf.JobConf;
 import cmu.ds.mr.util.Util;
 
 /**
  * The class to split the input file/directory of files to FileSplit
  * 
  * */
 public class FileSplitter {
   
  private Properties prop = new Properties();
   
   public FileSplitter() throws FileNotFoundException, IOException {
     prop.load(new FileInputStream(Util.CONFIG_PATH));
   }
 
   /**
    * Split input files
    * 
    * @return # of maps
    * @throws IOException
    * */
   public List<FileSplit> getSplits(JobConf jobConf) throws IOException {
     List<FileSplit> splitFiles = new ArrayList<FileSplit>();
 
     long fileLen = 0l, start = 0l;  // # of lines
     long blksize = Long.parseLong((String) prop.get(Util.BLOCK_SIZE));
     int cnt = 1;
     String line;
     BufferedReader br = null;
 
     try {
       File inFile = new File(jobConf.getInpath());
       if (inFile.isDirectory()) {
         // TODO: multiple files
         File[] files = inFile.listFiles();
 
         for (int i = 0; i < files.length; i++) {
           int nline = 0;
           br = new BufferedReader(new FileReader(files[i]));
           while ((line = br.readLine()) != null) {
             fileLen += line.getBytes().length;
             nline++;
             
             if (fileLen >= cnt * blksize) {
               FileSplit fs = new FileSplit(files[i].getAbsolutePath(), start, nline);
               splitFiles.add(fs);
               
               nline = 0;
               start = fileLen;
               cnt++;
             }
           }
           
           // the remaining part of a file
           if(start > fileLen) {
             FileSplit fs = new FileSplit(files[i].getAbsolutePath(), start, nline);
             splitFiles.add(fs);
           }
         }
       } 
       else {
         int nline = 0;
         br = new BufferedReader(new FileReader(inFile));
         while ((line = br.readLine()) != null) {
           fileLen += line.getBytes().length;
 
           if (fileLen >= cnt * blksize) {
             FileSplit fs = new FileSplit(inFile.getAbsolutePath(), start, nline);
             splitFiles.add(fs);
             
             nline = 0;
             start = fileLen;
             cnt++;
           }
         }
         
         // the remaining part of a file
         if(start > fileLen) {
           FileSplit fs = new FileSplit(inFile.getAbsolutePath(), start, nline);
           splitFiles.add(fs);
         }
       }
     } finally {
       br.close();
     }
 
     return splitFiles;
   }
 
   // public int splitInputFiles(JobConf jobConf, String jobRootDir) throws IOException {
   // File dirFile = new File(jobRootDir);
   // if (!dirFile.exists())
   // dirFile.mkdirs();
   //
   // long fileLen = 0l;
   // long blksize = Long.parseLong((String) prop.get(Util.BLOCK_SIZE));
   // int cnt = 0;
   // String part = "part-", line;
   // String outpath = String.format("%s%c%s%05d", jobConf.getOutpath(), File.separatorChar,
   // "part-", cnt);
   // BufferedReader br = null;
   // BufferedWriter bw = new BufferedWriter(new FileWriter(outpath));
   //
   // try {
   // File inFile = new File(jobConf.getInpath());
   // if (inFile.isDirectory()) {
   // // TODO: multiple files
   // File[] files = inFile.listFiles();
   //
   // for (int i = 0; i < files.length; i++) {
   // br = new BufferedReader(new FileReader(files[i]));
   // while((line = br.readLine()) != null) {
   // bw.write(line);
   // fileLen += line.getBytes().length;
   //
   // if (fileLen >= blksize) {
   // fileLen = 0;
   // bw.close();
   // outpath = String.format("%s%c%s%05d", jobConf.getOutpath(), File.separatorChar,
   // "part-", ++cnt);
   // bw = new BufferedWriter(new FileWriter(outpath));
   // }
   // }
   // }
   // }
   // else {
   // br = new BufferedReader(new FileReader(inFile));
   // while ((line = br.readLine()) != null) {
   // bw.write(line);
   // fileLen += line.getBytes().length;
   //
   // if (fileLen >= blksize) {
   // fileLen = 0;
   // bw.close();
   // outpath = String.format("%s%c%s%05d", jobConf.getOutpath(), File.separatorChar,
   // "part-", ++cnt);
   // bw = new BufferedWriter(new FileWriter(outpath));
   // }
   // }
   // }
   // } finally {
   // br.close();
   // bw.close();
   // }
   //
   // return cnt;
   // }
 
 }
