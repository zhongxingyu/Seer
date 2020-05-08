 package jp.gr.java_conf.neko_daisuki.fsyscall.slave;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.RandomAccessFile;
 import java.util.Arrays;
 
 import jp.gr.java_conf.neko_daisuki.fsyscall.Command;
 import jp.gr.java_conf.neko_daisuki.fsyscall.CommandDispatcher;
 import jp.gr.java_conf.neko_daisuki.fsyscall.Encoder;
 import jp.gr.java_conf.neko_daisuki.fsyscall.Errno;
 import jp.gr.java_conf.neko_daisuki.fsyscall.L;
 import jp.gr.java_conf.neko_daisuki.fsyscall.PayloadSize;
 import jp.gr.java_conf.neko_daisuki.fsyscall.Pid;
 import jp.gr.java_conf.neko_daisuki.fsyscall.ProtocolError;
 import jp.gr.java_conf.neko_daisuki.fsyscall.SyscallResult;
 import jp.gr.java_conf.neko_daisuki.fsyscall.Unix;
 import jp.gr.java_conf.neko_daisuki.fsyscall.io.SyscallInputStream;
 import jp.gr.java_conf.neko_daisuki.fsyscall.io.SyscallOutputStream;
 
 public class Slave extends Worker {
 
     private static class Logger {
 
         public static void info(String message) {
             L.info(buildMessage(message));
         }
 
         private static String buildMessage(String message) {
             return String.format("Slave: %s", message);
         }
     }
 
     private static class UnixException extends Exception {
 
         private Errno mErrno;
 
         public UnixException(Errno errno, Throwable e) {
             super(e);
             initialize(errno);
         }
 
         public UnixException(Errno errno) {
             initialize(errno);
         }
 
         public Errno getErrno() {
             return mErrno;
         }
 
         private void initialize(Errno errno) {
             mErrno = errno;
         }
     }
 
     private interface UnixFile {
 
         public int read(byte[] buffer) throws UnixException;
         public void close() throws UnixException;
         public long lseek(long offset, int whence) throws UnixException;
     }
 
     private abstract static class UnixRandomAccessFile implements UnixFile {
 
         protected RandomAccessFile mFile;
 
         protected UnixRandomAccessFile(String path, String mode) throws UnixException {
             try {
                 mFile = new RandomAccessFile(path, mode);
             }
             catch (FileNotFoundException e) {
                 throw new UnixException(Errno.ENOENT, e);
             }
             catch (SecurityException e) {
                 throw new UnixException(Errno.EPERM, e);
             }
         }
 
         public abstract int read(byte[] buffer) throws UnixException;
 
         public void close() throws UnixException {
             try {
                 mFile.close();
             }
             catch (IOException e) {
                 throw new UnixException(Errno.EBADF, e);
             }
         }
 
         public long lseek(long offset, int whence) throws UnixException {
             long pos;
             switch (whence) {
             case Unix.Constants.SEEK_SET:
                 pos = offset;
                 break;
             case Unix.Constants.SEEK_CUR:
                 try {
                     pos = mFile.getFilePointer() + offset;
                 }
                 catch (IOException e) {
                     throw new UnixException(Errno.EIO);
                 }
                 break;
             case Unix.Constants.SEEK_END:
                 try {
                     pos = mFile.length() + offset;
                 }
                 catch (IOException e) {
                     throw new UnixException(Errno.EIO);
                 }
                 break;
             case Unix.Constants.SEEK_DATA:
             case Unix.Constants.SEEK_HOLE:
             default:
                 throw new UnixException(Errno.EINVAL);
             }
 
             try {
                 mFile.seek(pos);
             }
             catch (IOException e) {
                 throw new UnixException(Errno.EIO, e);
             }
 
             return pos;
         }
     }
 
     private static class UnixInputFile extends UnixRandomAccessFile {
 
         public UnixInputFile(String path) throws UnixException {
             super(path, "r");
         }
 
         public int read(byte[] buffer) throws UnixException {
             int nBytes;
             try {
                 nBytes = mFile.read(buffer);
             }
             catch (IOException e) {
                 throw new UnixException(Errno.EIO, e);
             }
             return nBytes != -1 ? nBytes : 0;
         }
     }
 
     private static class UnixOutputFile extends UnixRandomAccessFile {
 
         public UnixOutputFile(String path) throws UnixException {
             super(path, "rw");
         }
 
         public int read(byte[] buffer) throws UnixException {
             throw new UnixException(Errno.EBADF);
         }
     }
 
     private abstract static class UnixStream implements UnixFile {
 
         public abstract int read(byte[] buffer) throws UnixException;
         public abstract void close() throws UnixException;
 
         public long lseek(long offset, int whence) throws UnixException {
             throw new UnixException(Errno.ESPIPE);
         }
     }
 
     private static class UnixInputStream extends UnixStream {
 
         private InputStream mIn;
 
         public UnixInputStream(InputStream in) {
             mIn = in;
         }
 
         public int read(byte[] buffer) throws UnixException {
             int nBytes;
             try {
                 nBytes = mIn.read(buffer);
             }
             catch (IOException e) {
                 throw new UnixException(Errno.EIO, e);
             }
             return nBytes != -1 ? nBytes : 0;
         }
 
         public void close() throws UnixException {
             try {
                 mIn.close();
             }
             catch (IOException e) {
                 throw new UnixException(Errno.EBADF, e);
             }
         }
     }
 
     private static class UnixOutputStream extends UnixStream {
 
         private OutputStream mOut;
 
         public UnixOutputStream(OutputStream out) {
             mOut = out;
         }
 
         public int read(byte[] buffer) throws UnixException {
             throw new UnixException(Errno.EBADF);
         }
 
         public void close() throws UnixException {
             try {
                 mOut.close();
             }
             catch (IOException e) {
                 throw new UnixException(Errno.EBADF, e);
             }
         }
     }
 
     private static final int UNIX_FILE_NUM = 256;
 
     private Application mApplication;
     private SyscallInputStream mIn;
     private SyscallOutputStream mOut;
 
     private UnixFile[] mFiles;
 
     private SlaveHelper mHelper;
 
     public Slave(Application application, InputStream in, OutputStream out) throws IOException {
         Logger.info("a slave is starting.");
 
         mApplication = application;
         mIn = new SyscallInputStream(in);
         mOut = new SyscallOutputStream(out);
         mHelper = new SlaveHelper(this, mIn, mOut);
 
         mFiles = new UnixFile[UNIX_FILE_NUM];
         mFiles[0] = new UnixInputStream(System.in);
         mFiles[1] = new UnixOutputStream(System.out);
         mFiles[2] = new UnixOutputStream(System.err);
 
         writeOpenedFileDescriptors();
         Logger.info("file descripters were transfered from the slave.");
     }
 
     public boolean isReady() throws IOException {
         return mIn.isReady();
     }
 
     public void work() throws IOException {
         Logger.info("performing the work.");
         mHelper.runSlave();
         Logger.info("finished the work.");
     }
 
     public SyscallResult.Generic32 doOpen(String path, int flags, int mode) throws IOException {
         SyscallResult.Generic32 result = new SyscallResult.Generic32();
 
         int fd = findFreeSlotOfFile();
         if (fd < 0) {
             result.retval = -1;
             result.errno = Errno.ENFILE;
             return result;
         }
 
         UnixFile file;
         try {
             switch (flags & Unix.Constants.O_ACCMODE) {
             case Unix.Constants.O_RDONLY:
                 file = new UnixInputFile(path);
                 break;
             case Unix.Constants.O_WRONLY:
                 // XXX: Here ignores O_CREAT.
                 file = new UnixOutputFile(path);
                 break;
             default:
                 result.retval = -1;
                 result.errno = Errno.EINVAL;
                 return result;
             }
         }
         catch (UnixException e) {
             result.retval = -1;
             result.errno = e.getErrno();
             return result;
         }
 
         mFiles[fd] = file;
 
         result.retval = fd;
         return result;
     }
 
     public SyscallResult.Read doRead(int fd, long nbytes) throws IOException {
         SyscallResult.Read result = new SyscallResult.Read();
 
        UnixFile file;
        try {
            file = mFiles[fd];
        }
        catch (IndexOutOfBoundsException e) {
            result.retval = -1;
            result.errno = Errno.EBADF;
            return result;
        }
         if (file == null) {
             result.retval = -1;
             result.errno = Errno.EBADF;
             return result;
         }
 
         /*
          * This implementation cannot handle the nbytes parameter which is
          * greater than maximum value of int (2^30 - 1).
          */
         byte[] buffer = new byte[(int)nbytes];
         try {
             result.retval = file.read(buffer);
         }
         catch (UnixException e) {
             result.retval = -1;
             result.errno = e.getErrno();
             return result;
         }
 
         result.buf = Arrays.copyOf(buffer, (int)result.retval);
         return result;
     }
 
     public SyscallResult.Generic64 doLseek(int fd, long offset, int whence) throws IOException {
         SyscallResult.Generic64 result = new SyscallResult.Generic64();
 
        UnixFile file = mFiles[fd];
         if (file == null) {
             result.retval = -1;
             result.errno = Errno.EBADF;
             return result;
         }
 
         long pos;
         try {
             pos = file.lseek(offset, whence);
         }
         catch (UnixException e) {
             result.retval =-1;
             result.errno = e.getErrno();
             return result;
         }
 
         result.retval = pos;
         return result;
     }
 
     public SyscallResult.Generic64 doMmap(char[] addr, long len, int prot, int flags, int fd, long pos) throws IOException {
         return null;
     }
 
     public SyscallResult.Pread doPread(int fd, long iovcnt, long offset) throws IOException {
         return null;
     }
 
     public SyscallResult.Generic32 doIssetugid() throws IOException {
         return null;
     }
 
     public SyscallResult.Lstat doLstat(String path) throws IOException {
         return null;
     }
 
     public SyscallResult.Fstat doFstat(int fd) throws IOException {
         return null;
     }
 
     public SyscallResult.Stat doStat(String path) throws IOException {
         return null;
     }
 
     public SyscallResult.Generic32 doWritev(int fd, char[] iovp, long iovcnt) throws IOException {
         return null;
     }
 
     public SyscallResult.Generic32 doSelect(int nfds, Unix.FdSet in, Unix.FdSet ou, Unix.FdSet ex, Unix.TimeVal tv) throws IOException {
         return null;
     }
 
     public SyscallResult.Readlink doReadlink(String path, long count) throws IOException {
         return null;
     }
 
     public SyscallResult.Generic32 doIoctl(int fd, long com, char[] data) throws IOException {
         return null;
     }
 
     public SyscallResult.Generic32 doAccess(String path, int flags) throws IOException {
         return null;
     }
 
     public SyscallResult.Generic32 doLink(String path1, String path2) throws IOException {
         return null;
     }
 
     public SyscallResult.Generic32 doClose(int fd) throws IOException {
         SyscallResult.Generic32 result = new SyscallResult.Generic32();
 
        UnixFile file = mFiles[fd];
         if (file == null) {
             result.retval = -1;
             result.errno = Errno.EBADF;
             return result;
         }
 
         try {
             file.close();
         }
         catch (UnixException e) {
             result.retval = -1;
             result.errno = e.getErrno();
             return result;
         }
 
         mFiles[fd] = null;
 
         result.retval = 0;
         return result;
     }
 
     public SyscallResult.Generic64 doWrite(int fd, byte[] buf, long nbytes) throws IOException {
         return null;
     }
 
     public void doExit(int rval) throws IOException {
         mIn.close();
         mOut.close();
         mApplication.removeSlave(this);
         mApplication.setExitStatus(rval);
     }
 
     private void writeOpenedFileDescriptors() throws IOException {
         int fds[] = { 0, 1, 2 };
         byte[][] buffers = new byte[fds.length][];
         for (int i = 0; i < fds.length; i++) {
             buffers[i] = Encoder.encodeInteger(fds[i]);
         }
         int len = 0;
         for (int i = 0; i < fds.length; i++) {
             len += buffers[i].length;
         }
 
         mOut.write(len);
         for (int i = 0; i < fds.length; i++) {
             mOut.write(buffers[i]);
         }
     }
 
     private int findFreeSlotOfFile() {
         int len = mFiles.length;
         int i;
         for (i = 0; (i < len) && (mFiles[i] != null); i++) {
         }
         return i < len ? i : -1;
     }
 }
 
 /**
  * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4 filetype=java
  */
