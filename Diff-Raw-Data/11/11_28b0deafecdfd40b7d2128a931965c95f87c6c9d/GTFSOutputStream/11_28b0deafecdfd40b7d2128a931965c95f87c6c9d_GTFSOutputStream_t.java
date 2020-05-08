 /**
  *
  * Licensed under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  *
  * 
  * Implements the Hadoop FSOutputStream interfaces to allow applications to write to
  * files in Kosmos File System (KFS).
  */
 
 package org.apache.hadoop.fs.gtfs;
 
 import org.apache.hadoop.util.Progressable;
 import org.apache.hadoop.fs.*;
 import org.apache.hadoop.fs.permission.FsPermission;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 
 class GTFSOutputStream extends OutputStream {
 
     private FileSystem dfs;
     private GTFSImpl gtfs_impl;
     private FSDataOutputStream dfs_out;
     private Path dfs_path;
     private FsPermission permission;
     private boolean overwrite;
     private int bufferSize;
     private short replication;
     private long blockSize;
     private boolean append;
     private Progressable progressReporter;
     private byte[] stream_buf;
     private byte[] one_byte;
     private int buf_pos;
     private int pos;
     private int threshold;
     private int fd;
 
     public GTFSOutputStream(int fd,
                             Path dfs_path,
                             FsPermission permission,
                             boolean overwrite,
                             int bufferSize,
                             short replication,
                             long blockSize,
                             int pos,
                             int threshold,
                             FileSystem fs,
                             GTFSImpl gtfs_impl,
                             Progressable progress) throws IOException {
         this.fd = fd;
         this.dfs_path = dfs_path;
         this.permission = permission;
         this.overwrite = overwrite;
         this.bufferSize = bufferSize;
         this.replication = replication;
         this.blockSize = blockSize;
         this.pos = pos;
         this.threshold = threshold;
         this.dfs = fs;
         this.gtfs_impl = gtfs_impl;
         this.progressReporter = progress;
 
         buf_pos = 0;
         stream_buf = new byte[bufferSize];
         one_byte = new byte[1];
 
         if (this.pos > this.threshold) {
             throw new IOException("File should be on DFS");
         }
         this.dfs_out = null;
     }
 
     public long getPos() throws IOException {
         return pos;
     }
 
     public void write(int v) throws IOException {
         one_byte[0] = (byte) (v & 0x77);
         write(one_byte, 0, 1);
     }
 
     public void write(byte b[]) throws IOException {
         write(b, 0, b.length);
     }
 
     public void migrate() throws IOException {
         GTFSImpl.FetchReply.ByReference reply =
                 new GTFSImpl.FetchReply.ByReference();
         byte[] tmpbuf = new byte[threshold];
         gtfs_impl.readall(fd, tmpbuf, reply);
 
         dfs_out = dfs.create(dfs_path, permission, false, bufferSize, replication, blockSize, progressReporter);
         dfs_out.write(tmpbuf, 0, reply.buf_len);
         gtfs_impl.writelink(fd, dfs_path.toString());
     }
 
     public void send_data_to_mds() {
         gtfs_impl.write(fd, stream_buf, buf_pos);
         buf_pos = 0;
     }
 
     public void write(byte b[], int off, int len) throws IOException {
         if (stream_buf == null) {
             throw new IOException("File closed");
         }
         if (dfs_out != null) {
             dfs_out.write(b, off, len);
             return;
         }
         if (pos + len > this.threshold) {
             migrate();
             dfs_out.write(b, off, len);
             return;
         }
 
         int bufToSend = len;
         while (bufToSend > 0) {
             int pack_len = Math.min(bufferSize - buf_pos, bufToSend);
            System.arraycopy(b, off, stream_buf, buf_pos, pack_len);
             pos += pack_len;
             buf_pos += pack_len;
             off += pack_len;
             bufToSend -= pack_len;
 
             if (buf_pos == bufferSize) {
                 send_data_to_mds();
             }
         }
         progressReporter.progress();
     }
 
     public void flush() throws IOException {
         if (dfs_out != null) {
             dfs_out.flush();
         } else {
             send_data_to_mds();
         }
         progressReporter.progress();
     }
 
     public synchronized void close() throws IOException {
         flush();
         stream_buf = null;
         one_byte = null;
         if (dfs_out != null)
           dfs_out.close();
     }
 }
