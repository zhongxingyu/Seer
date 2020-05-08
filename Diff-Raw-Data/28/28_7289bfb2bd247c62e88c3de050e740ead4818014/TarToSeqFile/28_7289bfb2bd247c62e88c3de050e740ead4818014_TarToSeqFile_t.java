 /* TarToSeqFile.java - Convert tar files into Hadoop SequenceFiles.
  *
  * Copyright (C) 2008 Stuart Sierra
  * Modified by Reason Zhang @ 2012
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License. You
  * may obtain a copy of the License at
  * http:www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package me.sheimi.hadoop.seq;
 
 // From ant.jar, http://ant.apache.org/ 
 import org.apache.tools.bzip2.CBZip2InputStream;
 import org.apache.tools.tar.TarEntry;
 import org.apache.tools.tar.TarInputStream;
 
 // From hadoop-*-core.jar, http://hadoop.apache.org/
 // Developed with Hadoop 1.0.3. */
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.io.Text;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.zip.GZIPInputStream;
 
 import me.sheimi.util.SeqImage;
 
 public class TarToSeqFile {
 	
   private File inputFile;
   private File outputFile;
   private LocalSetup setup;
 
   public TarToSeqFile() throws Exception {
     setup = new LocalSetup();
   }
 
   public void setInput(File inputFile) {
     this.inputFile = inputFile;
   }
 
   public void setOutput(File outputFile) {
       this.outputFile = outputFile;
   }
 
   public void execute() throws Exception {
     TarInputStream input = null;
     SequenceFile.Writer output = null;
     try {
       input = openInputFile();
       output = openOutputFile();
       TarEntry entry;
       while ((entry = input.getNextEntry()) != null) {
         if (entry.isDirectory()) { continue; }
         String filename = entry.getName();
         String[] fns = filename.split("/");
         if (fns[fns.length - 1].charAt(0) == '.') {
           continue;
         }
         byte[] data = TarToSeqFile.getBytes(input, entry.getSize());
         System.out.printf("%s %d %d\n", filename, entry.getSize(), data.length);
         
         Text key = new Text(filename);
         BytesWritable value = new BytesWritable(data);
         output.append(key, value);
       }
     } finally {
       if (input != null) {
         input.close();
       }
       if (output != null) {
         output.close();
       }
     }
   }
 
   private TarInputStream openInputFile() throws Exception {
     InputStream fileStream = new FileInputStream(inputFile);
     String name = inputFile.getName();
     InputStream theStream = null;
     if (name.endsWith(".tar.gz") || name.endsWith(".tgz")) {
       theStream = new GZIPInputStream(fileStream);
     } else if (name.endsWith(".tar.bz2") || name.endsWith(".tbz2")) {
       fileStream.skip(2);
       theStream = new CBZip2InputStream(fileStream);
     } else {
       theStream = fileStream;
     }
     return new TarInputStream(theStream);
   }
 
   private SequenceFile.Writer openOutputFile() throws Exception {
     Path outputPath = new Path(outputFile.getAbsolutePath());
     return SequenceFile.createWriter(setup.getLocalFileSystem(), setup.getConf(),
                                      outputPath,
                                      Text.class, BytesWritable.class,
                                      SequenceFile.CompressionType.BLOCK);
   }
 
   private static byte[] getBytes(TarInputStream input, long size) throws Exception {
     if (size > Integer.MAX_VALUE) {
       throw new Exception("A file in the tar archive is too large.");
     }
     
    SeqImage image = new SeqImage(input, (int)size);
     byte[] bytes = image.encode();
     return bytes;
   }
 
   public static void main(String[] args) {
     
     if (args.length != 2) {
       exitWithHelp();
     }
 
     try {
       TarToSeqFile me = new TarToSeqFile();
       me.setInput(new File(args[0]));
       me.setOutput(new File(args[1]));
       me.execute();
     } catch (Exception e) {
       e.printStackTrace();
       exitWithHelp();
     }
   }
 
   public static void exitWithHelp() {
     System.err.println("Usage: java org.altlaw.hadoop.TarToSeqFile <tarfile> <output>\n\n" +
                        "<tarfile> may be GZIP or BZIP2 compressed, must have a\n" +
                        "recognizable extension .tar, .tar.gz, .tgz, .tar.bz2, or .tbz2.");
     System.exit(1);
   }
 }
