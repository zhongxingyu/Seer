 package com.hadooptraining.lab3;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.LocalFileSystem;
 import org.apache.hadoop.fs.Path;
 
 import java.io.IOException;
 
 /************************************************************************
  *                                LAB 3                                 *
  ************************************************************************/
 
 /**
  * Copies a file from the local file system to the HDFS file system. After compiling the file,
  * you have to invoke it with the Hadoop jar files in the classpath. The best way to invoke this
  * is to use the 'hadoop -jar' command. Typically, the following way works well.
  * hadoop jar <jar_filename> com.hadooptraining.lab3.FileCopy <local file path> <path on HDFS file system>
  */
 public class FileCopy {
     public void copyFile(Path inPath, Path outPath) throws IOException {
         Configuration config = new Configuration();
         // following two lines show how to include a specific configuration file
         //config.addResource(new Path("/etc/hadoop/conf.pseudo.mr1/core-site.xml"));
         //config.addResource(new Path("/etc/hadoop/conf.pseudo.mr1/hdfs-site.xml"));
         FileSystem hdfs = FileSystem.get(config);
         LocalFileSystem local = FileSystem.getLocal(config);
         FSDataInputStream inStream = local.open(inPath);
         FSDataOutputStream outStream = hdfs.create(outPath);
 
         byte[] fromFile = new byte[1000];
         int datalength;
         while ((datalength = inStream.read(fromFile)) > 0) {
             outStream.write(fromFile, 0, datalength);
         }
         inStream.close();
         outStream.close();
     }
 
     /**
      * This is the entry point for the program.
      * @param args Two arguments are needed.
      *             The first one is the local file name with path.
      *             The second one is the path on the HDFS where the file needs to be copied.
      */
     public static void main(String[] args){
         if (args.length == 2) {
             String inputPath = args[0];
             String outputPath = args[1];
             try {
                 FileCopy copier = new FileCopy();
                 copier.copyFile(new Path(inputPath), new Path(outputPath));
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 }
