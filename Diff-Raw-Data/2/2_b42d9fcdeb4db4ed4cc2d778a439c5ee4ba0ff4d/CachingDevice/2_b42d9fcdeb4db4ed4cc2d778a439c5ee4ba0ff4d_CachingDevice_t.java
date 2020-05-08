 // (c) copyright 2006 by eXXcellent solutions, Ulm. Author: bschmid
 
 package org.wings.io;
 
 import java.io.IOException;
 
 /**
 * This device buffers all input in an internal {@link java.lang.StringBuilder}
  * until {@link Device#flush()} or {@link Device#close()}
  * is called.
  */
 public class CachingDevice  implements Device {
     private final StringBuilderDevice bufferDevice = new StringBuilderDevice(4096);
     private final Device finalDevice ;
 
     public CachingDevice(Device finalDevice) {
         this.finalDevice = finalDevice;
     }
 
     public String toString() {
         return bufferDevice.toString();
     }
 
     public boolean isSizePreserving() {
         return bufferDevice.isSizePreserving();
     }
 
     public void flush() throws IOException {
         bufferDevice.flush();
     }
 
     public void close() throws IOException {
         bufferDevice.flush();
         finalDevice.print(bufferDevice.toString());
         bufferDevice.close();
     }
 
     public void reset() {
         bufferDevice.reset();
     }
 
     public Device print(String s) {
         return bufferDevice.print(s);
     }
 
     public Device print(char c) {
         return bufferDevice.print(c);
     }
 
     public Device print(char[] c) throws IOException {
         return bufferDevice.print(c);
     }
 
     public Device print(char[] c, int start, int len) throws IOException {
         return bufferDevice.print(c, start, len);
     }
 
     public Device print(int i) {
         return bufferDevice.print(i);
     }
 
     public Device print(Object o) {
         return bufferDevice.print(o);
     }
 
     public Device write(int c) throws IOException {
         return bufferDevice.write(c);
     }
 
     public Device write(byte[] b) throws IOException {
         return bufferDevice.write(b);
     }
 
     public Device write(byte[] b, int off, int len) throws IOException {
         return bufferDevice.write(b, off, len);
     }
 }
