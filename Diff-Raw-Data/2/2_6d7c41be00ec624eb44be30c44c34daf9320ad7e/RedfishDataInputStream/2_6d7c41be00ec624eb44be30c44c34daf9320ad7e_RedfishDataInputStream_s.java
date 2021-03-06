 /*
  * Copyright 2011-2012 the RedFish authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hadoop.fs.redfish;
 
 import java.io.*;
 import java.net.*;
 
 import org.apache.commons.logging.Log;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FSDataInputStream;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.FileUtil;
 import org.apache.hadoop.fs.Path;
 
class RedfishDataInputStream extends FSInputStream {
   private long m_ofe;
 
   static {
     try {
       System.loadLibrary("hadoopfishc");
     }
     catch (UnsatisfiedLinkError e) {
       e.printStackTrace();
       System.err.println("Unable to load hadoopfishc: " +
                 System.getProperty("java.library.path"));
       System.exit(1);
     }
   }
 
   private RedfishDataInputStream(long ofe) {
       m_ofe = ofe;
   }
 
   /* This finalizer is intended to free the (small!) amount of memory used by
    * the redfish_file data structure of a closed file.  It also destroys the
    * pthread mutex used to make the rest of the functions thread-safe.
    *
    * Please call close() explicitly to close files.  This it NOT intended as a
    * replacement for that function.
    * */
   protected void finalize() throws Throwable {} {
     this.redfishFree();
   }
 
   public boolean markSupported() {
     return false;
   }
 
   public int read() throws IOException {
     /* The oh-so-useful "read a single byte" method */
     byte[] buf = new byte[1];
     return redfishRead(buf, 0, 1);
   }
 
   public int read(byte[] buf) throws IOException {
     return redfishRead(buf, 0, buf.length());
   }
 
   public int read(byte[] buf, int off, int len) throws IOException {
     return redfishRead(buf, off, len);
   }
 
   public int read(long pos, byte[] buf, int off, int len)
               throws IOException {
     return redfishPread(pos, buf, off, len);
   }
 
   public boolean seekToNewSource(long targetPos) throws IOException {
     /* Redfish handles failover between chunk replicas internally, so this
      * method should be unneeded? */
     return false;
   }
 
   public native
     int available();
       throws IOException;
 
   private native
     int redfishRead(byte[] buf, int boff, int blen);
       throws IOException;
 
   private native
     int redfishPread(long off, byte[] buf, int boff, int blen);
       throws IOException;
 
   public native
     void flush();
       throws IOException;
 
   public native
     void close();
       throws IOException;
 
   private native
     void free();
 
   public native
     void seek(long off);
       throws IOException;
 
   public native
     long getPos();
       throws IOException;
 }
