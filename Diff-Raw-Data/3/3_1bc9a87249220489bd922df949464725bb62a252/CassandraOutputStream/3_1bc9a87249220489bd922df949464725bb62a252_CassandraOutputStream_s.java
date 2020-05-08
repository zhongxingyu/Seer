 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.cassandra.hadoop.fs;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import org.apache.cassandra.utils.FBUtilities;
 import org.apache.cassandra.utils.UUIDGen;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.fs.permission.FsPermission;
 import org.apache.hadoop.util.Progressable;
 
 public class CassandraOutputStream extends OutputStream
 {
 
     private Configuration            conf;
 
     private int                      bufferSize;
 
     private CassandraFileSystemStore store;
 
     private Path                     path;
 
     private long                     blockSize;
 
     private ByteArrayOutputStream    backupStream;
 
     private boolean                  closed;
 
     private int                      pos                 = 0;
 
     private long                     filePos             = 0;
 
     private long                     bytesWrittenToBlock = 0;
 
     private byte[]                   outBuf;
 
     private List<Block>              blocks              = new ArrayList<Block>();
 
     private Block                    nextBlock;
     
     private final Progressable       progress;
     
     private FsPermission             perms;
 
     public CassandraOutputStream(Configuration conf, CassandraFileSystemStore store, Path path, FsPermission perms, long blockSize,
             Progressable progress, int buffersize) throws IOException
     {
 
         this.conf = conf;
         this.store = store;
         this.path = path;
         this.blockSize = blockSize;
         this.backupStream = new ByteArrayOutputStream((int) blockSize);
         this.bufferSize = buffersize;
         this.progress = progress;
         this.outBuf = new byte[bufferSize];
         this.perms = perms;
 
     }
 
     public long getPos() throws IOException
     {
         return filePos;
     }
 
     @Override
     public synchronized void write(int b) throws IOException
     {
         if (closed)
         {
             throw new IOException("Stream closed");
         }
 
         if ((bytesWrittenToBlock + pos == blockSize) || (pos >= bufferSize))
         {
             flush();
         }
         outBuf[pos++] = (byte) b;
         filePos++;
     }
 
     @Override
     public synchronized void write(byte b[], int off, int len) throws IOException
     {
         if (closed)
         {
             throw new IOException("Stream closed");
         }
         while (len > 0)
         {
             int remaining = bufferSize - pos;
             int toWrite = Math.min(remaining, len);
             System.arraycopy(b, off, outBuf, pos, toWrite);
             pos += toWrite;
             off += toWrite;
             len -= toWrite;
             filePos += toWrite;
 
             if ((bytesWrittenToBlock + pos >= blockSize) || (pos == bufferSize))
             {
                 flush();
             }
         }
     }
 
     @Override
     public synchronized void flush() throws IOException
     {
         if (closed)
         {
             throw new IOException("Stream closed");
         }
 
         if (bytesWrittenToBlock + pos >= blockSize)
         {
             flushData((int)( blockSize - bytesWrittenToBlock ));
         }
         if (bytesWrittenToBlock == blockSize)
         {
             endBlock();
         }
         flushData(pos);
     }
 
     private synchronized void flushData(int maxPos) throws IOException
     {
         int workingPos = Math.min(pos, maxPos);
 
         if (workingPos > 0)
         {
             //
             // To the local block backup, write just the bytes
             //
             backupStream.write(outBuf, 0, workingPos);
 
             //
             // Track position
             //
             bytesWrittenToBlock += workingPos;
             System.arraycopy(outBuf, workingPos, outBuf, 0, pos - workingPos);
             pos -= workingPos;
         }
     }
 
     private synchronized void endBlock() throws IOException
     {
         // Send it to Cassandra
         if(progress != null)
             progress.progress();
         
         nextBlockOutputStream();
         store.storeBlock(nextBlock, backupStream);
         internalClose();
 
         backupStream.reset();
         bytesWrittenToBlock = 0;
     }
 
     private synchronized void nextBlockOutputStream() throws IOException
     {
        nextBlock = new Block(UUIDGen.makeType1UUIDFromHost(FBUtilities.getLocalAddress()), filePos - bytesWrittenToBlock, bytesWrittenToBlock);
         blocks.add(nextBlock);
         bytesWrittenToBlock = 0;
     }
 
     private synchronized void internalClose() throws IOException
     {
         INode inode = new INode(System.getProperty("user.name","none"), System.getProperty("user.name","none"), perms, INode.FileType.FILE, blocks.toArray(new Block[blocks.size()]));
         store.storeINode(path, inode);
     }
 
     @Override
     public synchronized void close() throws IOException
     {
         if (closed)
         {
             return;
         }
 
         flush();
         if (filePos == 0 || bytesWrittenToBlock != 0)
         {
             endBlock();
         }
 
         backupStream.close();
         super.close();
 
         closed = true;
     }
 
 }
