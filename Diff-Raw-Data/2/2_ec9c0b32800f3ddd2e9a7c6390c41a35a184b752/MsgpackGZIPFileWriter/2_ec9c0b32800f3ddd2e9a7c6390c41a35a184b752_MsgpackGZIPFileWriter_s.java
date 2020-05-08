 //
 // Treasure Data Bulk-Import Tool in Java
 //
 // Copyright (C) 2012 - 2013 Muga Nishizawa
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package com.treasure_data.bulk_import.prepare_parts;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FilterOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.logging.Logger;
 import java.util.zip.GZIPOutputStream;
 
 import org.msgpack.MessagePack;
 import org.msgpack.packer.Packer;
 
 public class MsgpackGZIPFileWriter extends FileWriter {
     static class DataSizeChecker extends FilterOutputStream {
 
         private int size = 0;
 
         public DataSizeChecker(OutputStream out) {
             super(out);
         }
 
         @Override
         public void write(byte[] b, int off, int len) throws IOException {
             size += len;
             super.write(b, off, len);
         }
 
         @Override
         public void close() throws IOException {
             size = 0; // Refresh
             super.close();
         }
 
         public int size() {
             return size;
         }
     }
 
     private static final Logger LOG = Logger
             .getLogger(MsgpackGZIPFileWriter.class.getName());
 
     protected MessagePack msgpack;
     protected Packer packer;
     protected GZIPOutputStream gzout;
 
     private int splitSize;
     protected DataSizeChecker dout;
     private int outputFileIndex = 0;
     private String outputDirName;
     private String outputFilePrefix;
     private File outputFile;
 
     public MsgpackGZIPFileWriter(PrepareConfig conf) {
         super(conf);
     }
 
     @Override
     public void configure(String infileName) throws PreparePartsException {
         msgpack = new MessagePack();
 
         // outputFilePrefix
        int lastSepIndex = infileName.lastIndexOf(File.pathSeparator);
         outputFilePrefix = infileName.substring(lastSepIndex + 1,
                 infileName.length()).replace('.', '_');
 
         // outputDir
         outputDirName = conf.getOutputDirName();
         File outputDir = new File(outputDirName);
         if (!outputDir.exists()) {
             outputDir.mkdirs();
         }
 
         // splitSize
         splitSize = conf.getSplitSize() * 1024;
 
         reopenOutputFile();
     }
 
     protected void reopenOutputFile() throws PreparePartsException {
         // close stream
         if (outputFileIndex != 0) {
             try {
                 close();
             } catch (IOException e) {
                 throw new PreparePartsException(e);
             }
         }
 
         // create msgpack packer
         try {
             String outputFileName = outputFilePrefix + "_" + outputFileIndex
                     + ".msgpack.gz";
             outputFileIndex++;
             outputFile = new File(outputDirName, outputFileName);
             dout = new DataSizeChecker(new BufferedOutputStream(
                     new FileOutputStream(outputFile)));
             gzout = new GZIPOutputStream(dout);
             packer = msgpack.createPacker(new BufferedOutputStream(gzout));
 
             LOG.fine("Created output file: " + outputFileName);
         } catch (IOException e) {
             throw new PreparePartsException(e);
         }
     }
 
     @Override
     public void writeBeginRow(int size) throws PreparePartsException {
         try {
             packer.writeMapBegin(size);
         } catch (IOException e) {
             throw new PreparePartsException(e);
         }
     }
 
     @Override
     public void write(Object o) throws PreparePartsException {
         try {
             packer.write(o);
         } catch (IOException e) {
             throw new PreparePartsException(e);
         }
     }
 
     @Override
     public void writeString(String v) throws PreparePartsException {
         try {
             packer.write(v);
         } catch (IOException e) {
             throw new PreparePartsException(e);
         }
     }
 
     @Override
     public void writeInt(int v) throws PreparePartsException {
         try {
             packer.write(v);
         } catch (IOException e) {
             throw new PreparePartsException(e);
         }
     }
 
     @Override
     public void writeLong(long v) throws PreparePartsException {
         try {
             packer.write(v);
         } catch (IOException e) {
             throw new PreparePartsException(e);
         }
     }
 
     @Override
     public void writeDouble(double v) throws PreparePartsException {
         try {
             packer.write(v);
         } catch (IOException e) {
             throw new PreparePartsException(e);
         }
     }
 
     @Override
     public void writeNil() throws PreparePartsException {
         try {
             packer.writeNil();
         } catch (IOException e) {
             throw new PreparePartsException(e);
         }
     }
 
     @Override
     public void writeEndRow() throws PreparePartsException {
         try {
             packer.writeMapEnd();
             if (dout.size() > splitSize) {
                 reopenOutputFile();
             }
         } catch (IOException e) {
             throw new PreparePartsException(e);
         }
     }
 
     @Override
     public void close() throws IOException {
         if (packer != null) {
             packer.flush();
             packer.close();
             packer = null;
         }
         if (gzout != null) {
             gzout.close();
             gzout = null;
             dout = null;
         }
     }
 
 }
