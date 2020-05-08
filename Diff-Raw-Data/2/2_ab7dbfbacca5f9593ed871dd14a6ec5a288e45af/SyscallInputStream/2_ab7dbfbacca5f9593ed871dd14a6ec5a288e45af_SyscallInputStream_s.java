 package jp.gr.java_conf.neko_daisuki.fsyscall.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import jp.gr.java_conf.neko_daisuki.fsyscall.Command;
 import jp.gr.java_conf.neko_daisuki.fsyscall.Pid;
 
 public class SyscallInputStream {
 
     private enum Status {
         OPEN,
         CLOSED };
 
     private Status mStatus;
     private InputStream mIn;
 
     public SyscallInputStream(InputStream in) {
         mStatus = Status.OPEN;
         mIn = in;
     }
 
     public boolean isReady() throws IOException {
         return (mStatus == Status.OPEN) && (0 < mIn.available());
     }
 
     public Command readCommand() throws IOException {
         return Command.fromInteger(readInteger());
     }
 
     public void close() throws IOException {
         mStatus = Status.CLOSED;
         mIn.close();
     }
 
     /**
      * Reads signed int (32bits). This method cannot handle unsigned int.
      */
     public int readInteger() throws IOException {
         int n = 0;
         int shift = 0;
         int m;
         while (((m = mIn.read()) & 0x80) != 0) {
             n += ((m & 0x7f) << shift);
             shift += 7;
         }
        return n;
     }
 
     public int readPayloadSize() throws IOException {
         return readInteger();
     }
 
     public byte readByte() throws IOException {
         return (byte)mIn.read();
     }
 
     public Pid readPid() throws IOException {
         return new Pid(readInteger());
     }
 
     public byte[] read(int len) throws IOException {
         byte[] buffer = new byte[len];
         int nBytes = 0;
         while (nBytes < len) {
             nBytes += mIn.read(buffer, nBytes, len - nBytes);
         }
 
         return buffer;
     }
 }
 
 /**
  * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
  */
