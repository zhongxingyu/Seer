 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.javavfs.impl.sftp;
 
 import ch.ethz.ssh2.SFTPv3Client;
 import ch.ethz.ssh2.SFTPv3DirectoryEntry;
 import ch.ethz.ssh2.SFTPv3FileHandle;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  *
  * @author krog
  */
 public class SftpInputStream extends InputStream {
 
     public SftpInputStream(SFTPv3Client sftpc, SFTPv3DirectoryEntry entry,  SFTPv3FileHandle fileHandle) {
         this.sftpc=sftpc;
         this.fileHandle=fileHandle;
         this.entry=entry;
     }
 
     private SFTPv3Client sftpc;
     private SFTPv3DirectoryEntry entry;
     private SFTPv3FileHandle fileHandle;
     private long offset=0;
 
     @Override
     public int available() throws IOException {
         long avail = entry.attributes.size-offset;
         if(avail>32768)
             avail=32768;
         return (int)avail;
     }
 
 
     public int read() throws IOException {
         byte[] bytes = new byte[0];
         sftpc.read(fileHandle, offset, bytes, 0, 1);
         offset++;
         return bytes[0];
     }
 
     @Override
     public void close() throws IOException {
         sftpc.closeFile(fileHandle);
         super.close();
     }
 
     @Override
     public int read(byte[] b, int off, int len) throws IOException {
         int bytesRead = sftpc.read(fileHandle, offset, b, off, len);
        offset+=bytesRead;
         return bytesRead;
     }
 
     @Override
     public int read(byte[] b) throws IOException {
         return super.read(b);
     }
 
 
 
 
 }
