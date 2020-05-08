 package jp.gr.java_conf.neko_daisuki.fsyscall.slave;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import jp.gr.java_conf.neko_daisuki.fsyscall.Command;
 import jp.gr.java_conf.neko_daisuki.fsyscall.Pid;
 import jp.gr.java_conf.neko_daisuki.fsyscall.ProtocolError;
 import jp.gr.java_conf.neko_daisuki.fsyscall.io.SyscallInputStream;
 import jp.gr.java_conf.neko_daisuki.fsyscall.io.SyscallOutputStream;
 
 public class SlaveHub extends Worker {
 
     private static class Peer {
 
         private SyscallInputStream mIn;
         private SyscallOutputStream mOut;
 
         public Peer(SyscallInputStream in, SyscallOutputStream out) {
             mIn = in;
             mOut = out;
         }
 
         public SyscallInputStream getInputStream() {
             return mIn;
         }
 
         public SyscallOutputStream getOutputStream() {
             return mOut;
         }
 
         public void close() throws IOException {
             mIn.close();
             mOut.close();
         }
     }
 
     private static class Slave extends Peer {
 
         private Pid mMasterPid;
 
         public Slave(SyscallInputStream in, SyscallOutputStream out, Pid masterPid) {
             super(in, out);
             mMasterPid = masterPid;
         }
 
         public Pid getMasterPid() {
             return mMasterPid;
         }
     }
 
     private Application mApplication;
     private Peer mMhub;
     private Map<Pid, Slave> mSlaves;
 
     public SlaveHub(Application application, InputStream mhubIn, OutputStream mhubOut, InputStream slaveIn, OutputStream slaveOut) throws IOException {
         mApplication = application;
         mMhub = new Peer(
                 new SyscallInputStream(mhubIn),
                 new SyscallOutputStream(mhubOut));
 
         negotiateVersion();
         Pid masterPid = mMhub.getInputStream().readPid();
 
         mSlaves = new HashMap<Pid, Slave>();
         Slave slave = new Slave(
                 new SyscallInputStream(slaveIn),
                 new SyscallOutputStream(slaveOut),
                 masterPid);
         mSlaves.put(masterPid, slave);
 
         transportFileDescriptors(slave);
     }
 
     public boolean isReady() throws IOException {
         if (mMhub.getInputStream().isReady()) {
             return true;
         }
         for (Peer slave: mSlaves.values()) {
             if (slave.getInputStream().isReady()) {
                 return true;
             }
         }
         return false;
     }
 
    /**
     * Closes the connection for the master hub ONLY. When this method is
     * called, all slaves are not alive. So this method ignores connections for
     * slave.
     *
     * When a slave is alive in calling this method, we will know that with an
     * exception.
     */
    public void close() throws IOException {
        mMhub.close();
    }

     public void work() throws IOException {
         if (mMhub.getInputStream().isReady()) {
             processMasterHub();
         }
         for (Slave slave: mSlaves.values()) {
             if (slave.getInputStream().isReady()) {
                 processSlave(slave);
             }
         }
     }
 
     private void processSlave(Slave slave) throws IOException {
         SyscallInputStream in = slave.getInputStream();
         Command command = in.readCommand();
         int payloadSize = in.readPayloadSize();
 
         SyscallOutputStream out = mMhub.getOutputStream();
         out.writeCommand(command);
         out.writePid(slave.getMasterPid());
         out.writePayloadSize(payloadSize);
         out.copyInputStream(in, payloadSize);
     }
 
     private void processMasterHub() throws IOException {
         SyscallInputStream in = mMhub.getInputStream();
         Command command = in.readCommand();
         Pid pid = in.readPid();
         if (command == Command.CALL_EXIT) {
             int unusedStatus = in.readInteger();
             mSlaves.remove(pid).close();
             return;
         }
         int payloadSize = in.readPayloadSize();
 
         SyscallOutputStream out = mSlaves.get(pid).getOutputStream();
         out.writeCommand(command);
         out.writePayloadSize(payloadSize);
         out.copyInputStream(in, payloadSize);
     }
 
     private void negotiateVersion() throws IOException {
         byte version = mMhub.getInputStream().readByte();
         if (version != 0) {
             String fmt = "requested version is not supported: %d";
             throw new ProtocolError(String.format(fmt, version));
         }
         mMhub.getOutputStream().writeByte((byte)0);
     }
 
     private void transportFileDescriptors(Peer slave) throws IOException {
         SyscallInputStream in = slave.getInputStream();
         int len = in.readInteger();
         byte[] data = in.read(len);
 
         SyscallOutputStream out = slave.getOutputStream();
         out.writeInteger(len);
         out.write(data);
     }
 }
 
 /**
  * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
  */
