 /*
  * Copyright (C) 2010 France Telecom
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package memoplayer;
 
 import java.io.*;
 import javax.microedition.io.*;
 
 //#ifdef jsr.75
 import javax.microedition.io.file.*;
 //#endif
 
 public class File implements Loadable {
     
     final static int MODE_READ      = 0;
     final static int MODE_SYNC      = 0;
     final static int MODE_ASYNC     = 1;
     final static int MODE_WRITE     = 16;
     final static int MODE_OVERWRITE = MODE_WRITE + 4;
         
     /**
      * Size of chunks used when reading data from HTTP connections
      * @see readAllBytes
      */
     private final static int DOWNLOAD_CHUNK_SIZE = 512;
  
     public File m_next;
     
     private Connection m_c;
     private DataInputStream m_dis;
     private DataOutputStream m_dos;
 
     private String m_url;
     private int m_mode = MODE_READ & MODE_SYNC;
     private int m_state = Loadable.ERROR;
     private int m_len = -1; // the total number of byte to read
     private int m_current = 0; // number of bytes already read (value should be between 0 and m_len)
     private byte[] m_data = null;
    private int m_httpResponseCode = 404;
     private boolean m_isInCache = false;
 
     boolean m_isLocal;
     
     private StringBuffer sb; // for UTF-8 support
     private String m_cacheRecord; // if not null, cache data to this record on load
     private String m_cacheNamespace; // if not empty, name of the Namespace to use
     
     // Read-only constructor
     File (String url) {
         this (url, 0);
     }
 
     File (byte[] buffer) {
         m_dis = new DataInputStream (new ByteArrayInputStream (buffer));
         m_len = buffer.length;
         m_httpResponseCode = 200;
         m_isLocal = true;
         m_state = Loadable.LOADED;
     }
 
     File (final String url, int mode) {
         m_url = url;
         m_mode = mode;
         setState (Loadable.OPENING);
         if (isMode (MODE_ASYNC)) {
             new Thread () {
                 public void run () {
                     open (url);
                 }
             }.start ();
         } else {
             open (url);
         }
     }
   
     // Queued file constructor (see FileQueue.getFile)
     File (String url, File next) {
         m_url = url;
         m_next = next;
         setState(Loadable.QUEUED);
     }
     
     final boolean isMode (final int m) {
         return (m_mode & m) == m;
     }
 
     final public boolean isInCache () {
         return m_isInCache;
     }
 
     public String getErrorMessage () {
         return "";
     }
 
     /**
      * Open queued file, load data to m_data, close file.
      * Used by the FileQueue.
      */
     void openAndLoad() {
         if (getState() == File.QUEUED) {
             open (m_url);
             if (getState() == Loadable.READY) {
                 setState(Loadable.LOADING);
                 m_data = readAllBytes();
                 if (m_data != null) {
                     close(Loadable.LOADED);
                 } else {
                     close(Loadable.ERROR);
                 }
             }
         }
     }
     
     /**
      * This method allows ImageTexture to retrieve data without closing the
      * File (already closed by openAndLoad() method) as with startReadAllBytes(false).
      * @return Return data loaded by the openAndLoad() method.
      */
     byte[] getData() {
         return m_data;
     }
     
     private void open (String url) {
         //Logger.println("File:"+this+": open("+url+") : "+(write?"WRITE":"READ"));
         setState(Loadable.OPENING);
         try {
             m_isLocal = true;
             if (url.startsWith ("http://") || url.startsWith("https://")) {
                 SmartHttpConnection conn;
                 conn = new SmartHttpConnection (url, isMode (MODE_WRITE));
                 m_c = conn;
                 if (!isMode (MODE_WRITE)) {
                     m_len = (int) conn.getLength();
                     m_dis = conn.openDataInputStream();
                     m_httpResponseCode = conn.getResponseCode();
 //#ifdef api.traffic
                     // Count downloaded data if m_len is known, if not rely on m_current
                     if (m_len > 0) Traffic.update (m_len);
 //#endif
                 } else {
                     m_dos = conn.openDataOutputStream();
                 }
                 m_isLocal = false;
 //#ifdef api.ad 
             } else if (url.startsWith("ad://")) {
                 
                 String mail = url.substring(5);
                 String zoneId = null;
                 String purge = null;
                 int t = mail.indexOf(":");
                 if (t != -1) {
                     zoneId = mail.substring(t+1);
                     mail = mail.substring(0, t);
                     t = zoneId.indexOf(":");
                     if (t != -1) {
                         purge = zoneId.substring(t+1);
                         zoneId = zoneId.substring(0,t);
                     }
                 }
                 
                 byte[] data = AdHelper.getAd(mail,zoneId,purge); 
                 
                 if (data != null) {
                     Logger.println("File.open: ad content");
                     m_len = data.length;
                     m_dis = new DataInputStream(new ByteArrayInputStream(data));
                 }      
 //#endif
 //#ifdef MM.pfs
             } else if (url.startsWith("pfs://")) {
                 open (MiniPlayer.getPfsBaseUrl() + url.substring(6));
                 return;
 //#endif
                 
 //#ifdef jsr.75                
             } else if (url.startsWith("file:///")) {
                 FileConnection conn = (FileConnection) Connector.open(url);
                 m_c = conn;
                 if (isMode (MODE_WRITE)) {
                     if (!conn.exists()) {
                         conn.create();
                     } else if (!isMode (MODE_OVERWRITE)) {
                         throw new IOException("File exist and not overwriting !");
                     }
                     m_dos = conn.openDataOutputStream();
                 } else {
                     if (!conn.exists()) {
                         throw new IOException("File not found !");
                     }
                     m_len = (int) conn.fileSize();
                     m_dis = conn.openDataInputStream();
                     m_httpResponseCode = 200;
                 }
 //#endif
             } else if (url.startsWith("cache:")) {
                     // Split url : cache:[namespace]//rmsRecord[.m4m#innerRessource||,sourceUrl[,force]]
 //#ifdef MM.namespace
                     // find the extra namespace if any
                     int index = url.indexOf ("//", 6);
                     String nameSpace = Thread.currentNamespace();
                     if (index > 6) {
                         if (nameSpace.length() == 0) { // prevent non privileged scene to get data from another name space
                             nameSpace = url.substring (6, index);
                         }
                         url = "cache:"+url.substring (index); // build the url to be "as usual"
                     }
 //#endif
                     String rmsRecord = url.substring(8);
                     String sourceUrl = null, innerRessource = null;
                     boolean forceUpdate = false;
                     int t = rmsRecord.indexOf (',');
                     if (t != -1) {
                         sourceUrl = rmsRecord.substring (t+1);
                         rmsRecord = rmsRecord.substring (0, t);
                         if (sourceUrl.endsWith (",force")) {
                             sourceUrl = sourceUrl.substring (0, sourceUrl.length() - 6);
                             forceUpdate = true;
                         }
                         //Logger.println (">> File: cache token: "+rmsRecord+", url:"+sourceUrl);
                     } else if ((t = rmsRecord.indexOf('#')) != -1) {
                         innerRessource = rmsRecord.substring (t+1);
                         rmsRecord = rmsRecord.substring (0, t);
                         if (!rmsRecord.endsWith (".m4m")) {
                             throw new IOException ("Cached ressource extraction (cache://scene.m4m#file) is only supported for M4M files, not for "+rmsRecord); 
                         }
                     }
                     byte[] data = null;
                     if (forceUpdate == false) {
 //#ifdef MM.namespace
                         data = Namespace.getCacheManager(nameSpace).getByteRecord(rmsRecord);
 //#else
                         data = CacheManager.getManager().getByteRecord(rmsRecord);
 //#endif
                     }
                     if (innerRessource != null) {
                         if (data != null) {
                             data = Decoder.extractChunck (data, innerRessource);
                         }
                         if (data == null) {
                             throw new IOException ("Could not extract "+innerRessource+" from cached ressource: "+rmsRecord); 
                         }
                     }
                     //Logger.println (">> File: data: "+data);
                     if (data != null) {
                         m_dis = new DataInputStream(new ByteArrayInputStream(data));
                         m_len = data.length;
                         m_httpResponseCode = 200;
                         m_isInCache = true; 
                     } else if (sourceUrl != null) {
                         //Logger.println ("File.open: File not cached ! Getting from: "+sourceUrl);
                         // Reopen with source stream
                         open (sourceUrl);
                         if (getState() == Loadable.READY) {
 //#ifdef MM.namespace
                             m_cacheNamespace = nameSpace;
 //#endif
                             m_cacheRecord = rmsRecord;
                         } else {
                             throw new IOException("Could not get data from source for cache.");
                         }
                     } else {
                         throw new IOException("Could not find data in cache.");
                     }
                 if (isMode (MODE_WRITE) && m_cacheRecord == null) {
                     throw new IOException("Cannot write in cache for now...");
                 }
             } else { // From JAR ressources
                 boolean explicitelyInJar = false;
                 if (url.startsWith("jar://")) {
                     url = url.substring(6);
                     explicitelyInJar = true;
                 }
                 if (!isMode (MODE_WRITE)) {
                     m_dis = Decoder.getResStream (url); // try to find it in the jar
                     if (m_dis == null && explicitelyInJar == false && url.endsWith ("bml")) {
                         try {
                             m_dis = new DataInputStream (new ByteArrayInputStream (Machine.s_context.decoder.getBml (url)));
                         } catch (Exception e) { 
                             m_dis = null; 
                             Logger.println ("Exception during BML loading "+e);
                         }
                     }
                     if (m_dis != null) {
 //#ifdef platform.android
                         // available() is equal to 1 on Android whatever the original file size
                         m_len = -1; // -1 lets readAllBytes read until the end of the stream 
 //#else
                         m_len = m_dis.available();
 //#endif
                         m_httpResponseCode = 200;
                     } else {
                         throw new IOException("Ressource "+url+" not found in JAR");
                     }
                 } else {
                     throw new IOException("Cannot write in JAR ressource");
                 }
             }
         } catch (Exception e) {
             Logger.println ("File: Exception for " + url + " : " + e);
             // e.printStackTrace();
             close (Loadable.ERROR);
             return;
         }
         setState(Loadable.READY);
     }
 
     void close (int state) {
         
         setState(state);
         if (m_c != null) {
             try { m_c.close(); } catch (Exception e) {e.printStackTrace();}
             m_c = null;
         }
         if (m_dis != null) {
 //#ifdef api.traffic
             // Rely on m_current to count downloaded data when m_len is unknown
             if (!m_isLocal && m_len == -1) {
                 Traffic.update(m_current);
             }
 //#endif
             try { m_dis.close(); } catch (Exception e) {e.printStackTrace();}
             m_dis = null;
         }
         if (m_dos != null) {
 //#ifdef api.traffic
             if (!m_isLocal) {
                 Traffic.update(m_current);
             }
 //#endif
             try { m_dos.close(); } catch (Exception e) {e.printStackTrace();}
             m_dos = null;
         }
         //System.out.println("File close");
     }
 
     boolean isEof () {
         try {
             return m_dis.available () <= 0;
         } catch (Exception e) {
             return true;
         }
     }
 
     /**
      * Copy the full content of the input stream to a byte buffer.
      * If length to read is not known (some HTTP connections send back -1), a dynamic buffer is used.
      * If the input stream is slow (HTTP connections), the copy is be done by chunks of 512 bytes
      * and thread will also yield to notify progress to other threads. 
      * @return the read byte array
      */
     byte[] readAllBytes () {
         if (m_dis != null) {
             try {
                 if (m_len == 0) return null;
                 byte [] buffer = new byte [m_len > 0 ? m_len : DOWNLOAD_CHUNK_SIZE*2];
                 int state = 0, len;
                 while (state != -1) {
                     if (m_isLocal || (buffer.length - m_current) < DOWNLOAD_CHUNK_SIZE) {
                         len = buffer.length - m_current;
                         if (len == 0) break;
                     } else { 
                         len = DOWNLOAD_CHUNK_SIZE;
                     }
                     //System.out.println("Read "+m_current+" "+len+"/"+buffer.length);
                     state = m_dis.read (buffer, m_current, len);
                     m_current += state;
                     if (!m_isLocal) Thread.yield(); //MCP: Notify other threads of progress
                     if (m_len < 0 && m_current == buffer.length) { // reallocate
                         byte [] temp = new byte [buffer.length * 2];
                         System.arraycopy (buffer, 0, temp, 0, buffer.length);
                         buffer = temp;
                     }
                 }
                 if (m_len < 0 && m_current+1 < buffer.length) { // pack
                     byte [] temp = new byte [m_current+1];
                     System.arraycopy (buffer, 0, temp, 0, m_current+1);
                     buffer = temp;
                 }
 //#ifdef mm.gzip
                 if ((buffer[0]&0xFF) == 0x1F &&
                     (buffer[1]&0xFF) == 0x8B &&
                     (buffer[2]&0xFF) == 0x08) { // GZIPed content
                     float l = buffer.length;
                     long t = System.currentTimeMillis ();
                     buffer = com.java4ever.apime.io.GZIP.inflate (buffer);
                     Logger.println ("Gzip: ratio: "+l/buffer.length+" time: "+(System.currentTimeMillis() - t));
                 }
 //#endif
                 if (m_cacheRecord != null) { // Save data to cache
                     CacheManager cm;
 //#ifdef MM.namespace
                     cm = Namespace.getCacheManager (m_cacheNamespace);
 //#else
                     cm = CacheManager.getManager ();
 //#endif
                     cm.setRecord (m_cacheRecord, buffer);
                     m_cacheRecord = null;
                 }
                 return buffer;
             } catch (Exception e) {
                 Logger.println ("File.readAllBytes: got Exception: "+e);
             }
         }
         setState (Loadable.ERROR);
         return null;
     }
     
     String readAll () {
         byte[] data = readAllBytes();
         if (data != null) {
             try { return new String(data,"UTF-8"); }
             catch (UnsupportedEncodingException e) { }
             return new String(data); 
         }
         return "";
     }
 
     //MCP: Switch to read mode for HTTP connections
     private void switchToReadMode () {
         if (!m_isLocal && m_dos != null) {
            SmartHttpConnection hc = (SmartHttpConnection)m_c;
             try {
                 m_dos.close ();
                 m_dos = null;
 //#ifdef api.traffic
                 // Count uploaded data
                 Traffic.update (m_current);
 //#endif
                 m_current = 0;
                m_dis = hc.openDataInputStream ();
                m_len = (int)hc.getLength ();
                m_httpResponseCode = hc.getResponseCode();
                 synchronized (this) { m_mode &= ~MODE_WRITE; } // Jump to read mode
 //#ifdef api.traffic
                 // Count downloaded data if m_len is known, if not rely on m_current
                 if (m_len > 0) Traffic.update (m_len);
 //#endif
                 return;
             } catch (Exception e) {
                 Logger.println ("File.switchToReadMode: Error: "+e);
                if (m_c != null) {
                    m_httpResponseCode = hc.getResponseCode();
                }
             }
         }
         close (Loadable.ERROR);
     }
 
     byte[] startReadAllBytes (boolean async) {
         byte[] data = null;
         if (getState() == Loadable.READY) {
             if (async) {
                 setState (Loadable.LOADING);
                 new Thread () {
                     public void run () {
                         if (isMode (MODE_WRITE)) {
                             switchToReadMode ();
                         }
                         m_data = readAllBytes ();
                         if (m_data != null) {
                             setState (Loadable.LOADED);
                         }
                     }
                 }.start ();
             } else {
                 if (isMode (MODE_WRITE)) {
                     switchToReadMode ();
                 }
                 setState (Loadable.LOADING);
                 data = readAllBytes ();
                 close (Loadable.CLOSED);
             }
         } else if (getState() == Loadable.LOADED) {
             data = m_data;
             m_data = null;
             close (Loadable.CLOSED);
         }
         return data;
     }
     
     String startReadAll (boolean async, String encoding) {
         byte[] data = startReadAllBytes(async);
         if (data != null) {
             try { return new String(data, encoding); } 
             catch (Exception e) { }
             try {
                 return new String(data);
             } catch (Exception e) {}
         }
         return "";
     }
     
     synchronized final int getMode () { return m_mode; }
     synchronized final void setState (int state) { m_state = state; }
     public synchronized final int getState () { return m_state; }
     public final int getDuration () { return m_len; }
     public final int getLen () { return m_len; }
     public final int getCurrent () { return m_current; }
     public final String getName () { return m_url; }
 
     String readString () {
         if (sb == null) sb = new StringBuffer();
         m_current += Decoder.readString(m_dis, sb);
        return sb.toString();
     }
     
     String readLine () {
         if (sb == null) sb = new StringBuffer();
         m_current += Decoder.readLine(m_dis, sb);
         return sb.toString();
     }
 
     int readInt () { 
         m_current += 4;
         return Decoder.readInt (m_dis);
     }
     
     byte [] readBytes (int size) throws IOException {
         m_current += size;
         byte [] buffer = new byte [size];
         m_dis.readFully (buffer, 0, size);
         return buffer;
     }
     
     boolean writeAllBytes(byte[] data) {
         if (m_dos != null) {
             try {
                 m_dos.write(data);
                 m_current += data.length;
                 return true;
             } catch (Exception e) {
                 Logger.println ("File.writeAllBytes: got Exception: "+e);
             }
         }
         setState (Loadable.ERROR);
         return false;
     }
     
     boolean startWriteAllBytes(final byte[] data, boolean async) {
         if (getState () == Loadable.READY && isMode (MODE_WRITE)) {
             if (async) {
                 setState (Loadable.LOADING);
                 new Thread () {
                     public void run () {
                         if (writeAllBytes (data)) {
                             setState (Loadable.READY); // reset ready for more to read or write
                         }
                     }
                 }.start ();
                 return true;
             } else {
                 return writeAllBytes (data);
             }
         }
         return false;
     }
     
     boolean startWriteAll (String s, boolean async, String encoding) {
         byte [] data = null;
         // getBytes() on empty string throws ArrayOutOfBoundsException on Samsung F480
         if (s != null && s.length() != 0) {
             try { data = s.getBytes (encoding); }
             catch (Exception e) { data = s.getBytes (); }
         } else {
             data = new byte[0];
         }
         return startWriteAllBytes (data, async);
     }
 
     int getHttpResponseCode () {
         return m_httpResponseCode;
     }
 }
