 package jp.gr.java_conf.neko_daisuki.fsyscall.slave;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.RandomAccessFile;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 
 import jp.gr.java_conf.neko_daisuki.fsyscall.Command;
 import jp.gr.java_conf.neko_daisuki.fsyscall.CommandDispatcher;
 import jp.gr.java_conf.neko_daisuki.fsyscall.Encoder;
 import jp.gr.java_conf.neko_daisuki.fsyscall.Errno;
 import jp.gr.java_conf.neko_daisuki.fsyscall.Logging;
 import jp.gr.java_conf.neko_daisuki.fsyscall.PayloadSize;
 import jp.gr.java_conf.neko_daisuki.fsyscall.Pid;
 import jp.gr.java_conf.neko_daisuki.fsyscall.ProtocolError;
 import jp.gr.java_conf.neko_daisuki.fsyscall.SyscallResult;
 import jp.gr.java_conf.neko_daisuki.fsyscall.Unix;
 import jp.gr.java_conf.neko_daisuki.fsyscall.io.SyscallInputStream;
 import jp.gr.java_conf.neko_daisuki.fsyscall.io.SyscallOutputStream;
 
 public class Slave extends Worker {
 
     private abstract static interface SelectPred {
 
         public boolean isReady(UnixFile file) throws UnixException;
     }
 
     private static class WriteSelectPred implements SelectPred {
 
         public boolean isReady(UnixFile file) throws UnixException {
             return file.isReadyToWrite();
         }
     }
 
     private static class ReadSelectPred implements SelectPred {
 
         public boolean isReady(UnixFile file) throws UnixException {
             return file.isReadyToRead();
         }
     }
 
     private static interface TimeoutDetector {
 
         public boolean isTimeout(long usec);
     }
 
     private static class FakeTimeoutDetector implements TimeoutDetector {
 
         public boolean isTimeout(long usec) {
             return false;
         }
     }
 
     private static class TrueTimeoutDetector implements TimeoutDetector {
 
         private Unix.TimeVal mTimeout;
 
         public TrueTimeoutDetector(Unix.TimeVal timeout) {
             mTimeout = timeout;
         }
 
         public boolean isTimeout(long usec) {
             return 1000000 * mTimeout.tv_sec + mTimeout.tv_usec <= usec;
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
 
         public boolean isReadyToRead() throws UnixException;
         public boolean isReadyToWrite() throws UnixException;
         public int read(byte[] buffer) throws UnixException;
         public long pread(byte[] buffer, long offset) throws UnixException;
         public int write(byte[] buffer) throws UnixException;
         public void close() throws UnixException;
         public long lseek(long offset, int whence) throws UnixException;
         public Unix.Stat fstat() throws UnixException;
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
 
         public abstract boolean isReadyToRead() throws UnixException;
         public abstract boolean isReadyToWrite() throws UnixException;
         public abstract int read(byte[] buffer) throws UnixException;
         public abstract long pread(byte[] buffer, long offset) throws UnixException;
         public abstract int write(byte[] buffer) throws UnixException;
 
         public Unix.Stat fstat() throws UnixException {
             Unix.Stat st = new Unix.Stat();
 
             try {
                 st.st_size = mFile.length();
             }
             catch (IOException e) {
                 throw new UnixException(Errno.EIO, e);
             }
 
             return st;
         }
 
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
 
         public boolean isReadyToRead() throws UnixException {
             try {
                 return mFile.getFilePointer() < mFile.length();
             }
             catch (IOException e) {
                 throw new UnixException(Errno.EIO, e);
             }
         }
 
         public boolean isReadyToWrite() throws UnixException {
             return false;
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
 
         public long pread(byte[] buffer, long offset) throws UnixException {
             int nBytes;
             try {
                 long initialPosition = mFile.getFilePointer();
                 mFile.seek(offset);
                 try {
                     nBytes = mFile.read(buffer);
                 }
                 finally {
                     mFile.seek(initialPosition);
                 }
             }
             catch (IOException e) {
                 throw new UnixException(Errno.EIO, e);
             }
             return nBytes == -1 ? 0 : nBytes;
         }
 
         public int write(byte[] buffer) throws UnixException {
             throw new UnixException(Errno.EBADF);
         }
     }
 
     private static class UnixOutputFile extends UnixRandomAccessFile {
 
         public UnixOutputFile(String path) throws UnixException {
             super(path, "rw");
         }
 
         public boolean isReadyToRead() throws UnixException {
             return false;
         }
 
         public boolean isReadyToWrite() throws UnixException {
             return true;
         }
 
         public int read(byte[] buffer) throws UnixException {
             throw new UnixException(Errno.EBADF);
         }
 
         public long pread(byte[] buffer, long offset) throws UnixException {
             throw new UnixException(Errno.EBADF);
         }
 
         public int write(byte[] buffer) throws UnixException {
             try {
                 mFile.write(buffer);
             }
             catch (IOException e) {
                 throw new UnixException(Errno.EIO, e);
             }
             return buffer.length;
         }
     }
 
     private abstract static class UnixStream implements UnixFile {
 
         public abstract boolean isReadyToRead() throws UnixException;
         public abstract boolean isReadyToWrite() throws UnixException;
         public abstract int read(byte[] buffer) throws UnixException;
         public abstract long pread(byte[] buffer, long offset) throws UnixException;
         public abstract int write(byte[] buffer) throws UnixException;
         public abstract void close() throws UnixException;
 
         public long lseek(long offset, int whence) throws UnixException {
             throw new UnixException(Errno.ESPIPE);
         }
 
         public Unix.Stat fstat() throws UnixException {
             throw new UnixException(Errno.ESPIPE);
         }
     }
 
     private static class UnixInputStream extends UnixStream {
 
         private InputStream mIn;
 
         public UnixInputStream(InputStream in) {
             mIn = in;
         }
 
         public boolean isReadyToRead() throws UnixException {
             try {
                 return 0 < mIn.available();
             }
             catch (IOException e) {
                 throw new UnixException(Errno.EIO, e);
             }
         }
 
         public boolean isReadyToWrite() throws UnixException {
             return false;
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
 
         public long pread(byte[] buffer, long offset) throws UnixException {
             throw new UnixException(Errno.ESPIPE);
         }
 
         public int write(byte[] buffer) throws UnixException {
             throw new UnixException(Errno.EBADF);
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
 
         public boolean isReadyToRead() throws UnixException {
             return false;
         }
 
         public boolean isReadyToWrite() throws UnixException {
             return true;
         }
 
         public int read(byte[] buffer) throws UnixException {
             throw new UnixException(Errno.EBADF);
         }
 
         public long pread(byte[] buffer, long offset) throws UnixException {
             throw new UnixException(Errno.ESPIPE);
         }
 
         public int write(byte[] buffer) throws UnixException {
             try {
                 mOut.write(buffer);
             }
             catch (IOException e) {
                 throw new UnixException(Errno.EIO, e);
             }
             return buffer.length;
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
 
     private static Logging.Logger mLogger;
 
     private Application mApplication;
     private SyscallInputStream mIn;
     private SyscallOutputStream mOut;
 
     private UnixFile[] mFiles;
 
     private SlaveHelper mHelper;
 
     public Slave(Application application, InputStream in, OutputStream out, InputStream stdin, OutputStream stdout, OutputStream stderr) throws IOException {
         mLogger.info("a slave is starting.");
 
         mApplication = application;
         mIn = new SyscallInputStream(in);
         mOut = new SyscallOutputStream(out);
         mHelper = new SlaveHelper(this, mIn, mOut);
 
         mFiles = new UnixFile[UNIX_FILE_NUM];
         mFiles[0] = new UnixInputStream(stdin);
         mFiles[1] = new UnixOutputStream(stdout);
         mFiles[2] = new UnixOutputStream(stderr);
 
         writeOpenedFileDescriptors();
         mLogger.verbose("file descripters were transfered from the slave.");
     }
 
     public boolean isReady() throws IOException {
         return mIn.isReady();
     }
 
     public void work() throws IOException {
         mLogger.verbose("performing the work.");
         mHelper.runSlave();
         mLogger.verbose("finished the work.");
     }
 
     public SyscallResult.Generic32 doOpen(String path, int flags, int mode) throws IOException {
         String fmt = "doOpen(path=%s, flags=%d, mode=%d)";
         mLogger.debug(String.format(fmt, path, flags, mode));
 
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
         mLogger.debug(String.format("doRead(fd=%d, nbytes=%d)", fd, nbytes));
 
         SyscallResult.Read result = new SyscallResult.Read();
 
         UnixFile file = getFile(fd);
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
         String fmt = "doLseek(fd=%d, offset=%d, whence=%d)";
         mLogger.debug(String.format(fmt, fd, offset, whence));
 
         SyscallResult.Generic64 result = new SyscallResult.Generic64();
 
         UnixFile file = getFile(fd);
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
 
     public SyscallResult.Pread doPread(int fd, long nbyte, long offset) throws IOException {
         String fmt = "doPread(fd=%d, nbyte=%d, offset=%d)";
         mLogger.debug(String.format(fmt, fd, nbyte, offset));
 
         SyscallResult.Pread result = new SyscallResult.Pread();
 
         UnixFile file = getFile(fd);
         if (file == null) {
             result.retval = -1;
             result.errno = Errno.EBADF;
             return result;
         }
 
         byte[] buffer = new byte[(int)nbyte];
         try {
             result.retval = file.pread(buffer, offset);
         }
         catch (UnixException e) {
             result.retval = -1;
             result.errno = e.getErrno();
             return result;
         }
 
         result.buf = Arrays.copyOf(buffer, (int)result.retval);
         return result;
     }
 
     /**
      * System call handler for issetugid(2). This always returns zero.
      */
     public SyscallResult.Generic32 doIssetugid() throws IOException {
         mLogger.debug("doIssetugid()");
 
         SyscallResult.Generic32 result = new SyscallResult.Generic32();
         result.retval = 0;
         return result;
     }
 
     /**
      * Runs lstat(2). This lstat(2) behaves as same as stat(2) (This
     * implementation does not return the infomation of the link itself).
      * Because Java 1.6 does not handle symbolic links (Java 1.7 can do with the
      * java.nio.files package).
      */
     public SyscallResult.Lstat doLstat(String path) throws IOException {
         mLogger.debug(String.format("doLstat(path=%s)", path));
 
         SyscallResult.Lstat result = new SyscallResult.Lstat();
 
         SyscallResult.Stat statResult = doStat(path);
         result.retval = statResult.retval;
         result.errno = statResult.errno;
         result.ub = statResult.ub;
 
         return result;
     }
 
     public SyscallResult.Fstat doFstat(int fd) throws IOException {
         mLogger.debug(String.format("doFstat(fd=%d)", fd));
 
         SyscallResult.Fstat result = new SyscallResult.Fstat();
 
         UnixFile file = getFile(fd);
         if (file == null) {
             result.retval = -1;
             result.errno = Errno.EBADF;
             return result;
         }
 
         try {
             result.sb = file.fstat();
         }
         catch (UnixException e) {
             result.retval = -1;
             result.errno = e.getErrno();
             return result;
         }
 
         result.retval = 0;
         return result;
     }
 
     public SyscallResult.Stat doStat(String path) throws IOException {
         mLogger.debug(String.format("doStat(path=%s)", path));
 
         SyscallResult.Stat result = new SyscallResult.Stat();
         Unix.Stat stat = new Unix.Stat();
 
         try {
             stat.st_size = new File(path).length();
         }
         catch (SecurityException e) {
             result.retval = -1;
             result.errno = Errno.EPERM;
             return result;
         }
 
         result.retval = 0;
         result.ub = stat;
         return result;
     }
 
     public SyscallResult.Generic32 doWritev(int fd, Unix.IoVec[] iovec) throws IOException {
         mLogger.debug(String.format("doWritev(fd=%d, iovec)", fd));
 
         SyscallResult.Generic32 result = new SyscallResult.Generic32();
 
         UnixFile file = getFile(fd);
         if (file == null) {
             result.retval = -1;
             result.errno = Errno.EBADF;
             return result;
         }
 
         int nBytes = 0;
         for (Unix.IoVec v: iovec) {
             nBytes += v.iov_base.length;
         }
         byte[] buffer = new byte[nBytes];
         int pos = 0;
         for (Unix.IoVec v: iovec) {
             int len = v.iov_base.length;
             System.arraycopy(v.iov_base, 0, buffer, pos, len);
             pos += len;
         }
 
         try {
             result.retval = file.write(buffer);
         }
         catch (UnixException e) {
             result.retval = -1;
             result.errno = e.getErrno();
             return result;
         }
 
         return result;
     }
 
     public SyscallResult.Select doSelect(int nfds, Collection<Integer> in, Collection<Integer> ou, Collection<Integer> ex, Unix.TimeVal timeout) throws IOException {
         String fmt = "doSelect(nfds=%d, in, ou, ex, timeout)";
         mLogger.debug(String.format(fmt, nfds));
 
         SyscallResult.Select result = new SyscallResult.Select();
 
         TimeoutDetector timeoutDetector = timeout != null ? new TrueTimeoutDetector(timeout) : new FakeTimeoutDetector();
 
         long usecInterval = 100 * 1000;
 
         Collection<Integer> inReady = new HashSet<Integer>();
         Collection<Integer> ouReady = new HashSet<Integer>();
         Collection<Integer> exReady = new HashSet<Integer>();
         long usecTime = 0;
         int nReadyFds = 0;
         SelectPred readPred = new ReadSelectPred();
         SelectPred writePred = new WriteSelectPred();
         while (!timeoutDetector.isTimeout(usecTime) && (nReadyFds == 0)) {
             inReady.clear();
             ouReady.clear();
             exReady.clear();
 
             try {
                 selectFds(inReady, in, readPred);
                 selectFds(ouReady, ou, writePred);
                 // TODO: Perform for ex (But how?).
             }
             catch (UnixException e) {
                 result.retval = -1;
                 result.errno = e.getErrno();
                 return result;
             }
 
             try {
                 Thread.sleep(usecInterval / 1000);
             }
             catch (InterruptedException e) {
                 result.retval = -1;
                 result.errno = Errno.EINTR;
                 return result;
             }
             usecTime += usecInterval;
 
             nReadyFds = inReady.size() + ouReady.size() + exReady.size();
         }
 
         result.retval = nReadyFds;
         if (nReadyFds == 0) {
             return result;
         }
 
         result.in = inReady;
         result.ou = ouReady;
         result.ex = exReady;
         return result;
     }
 
     /**
      * readlink(2) implementation. This returns always EINVAL. Because Java 1.6
      * cannot handle symbolic links.
      */
     public SyscallResult.Readlink doReadlink(String path, long count) throws IOException {
         String fmt = "doReadlink(path=%s, count=%d)";
         mLogger.debug(String.format(fmt, path, count));
 
         SyscallResult.Readlink result = new SyscallResult.Readlink();
         result.retval = -1;
         result.errno = Errno.EINVAL;
 
         return result;
     }
 
     /**
      * The dummy implementation of access(2). This always returns zero.
      */
     public SyscallResult.Generic32 doAccess(String path, int flags) throws IOException {
         String fmt = "doAccess(path=%s, flags=0x%02x)";
         mLogger.debug(String.format(fmt, path, flags));
 
         SyscallResult.Generic32 result = new SyscallResult.Generic32();
         result.retval = 0;
         return result;
     }
 
     public SyscallResult.Generic32 doLink(String path1, String path2) throws IOException {
         mLogger.debug(String.format("doLink(path1=%s, path2=%s)", path1, path2));
 
         SyscallResult.Generic32 result = new SyscallResult.Generic32();
         result.retval = -1;
         result.errno = Errno.ENOSYS;
         return result;
     }
 
     public SyscallResult.Generic32 doClose(int fd) throws IOException {
         mLogger.debug(String.format("doClose(fd=%d)", fd));
 
         SyscallResult.Generic32 result = new SyscallResult.Generic32();
 
         UnixFile file = getFile(fd);
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
         mLogger.debug(String.format("doWrite(fd=%d, buf, nbytes=%d)", fd, nbytes));
         SyscallResult.Generic64 result = new SyscallResult.Generic64();
 
         UnixFile file = getFile(fd);
         if (file == null) {
             result.retval = -1;
             result.errno = Errno.EBADF;
             return result;
         }
 
         try {
             result.retval = file.write(buf);
         }
         catch (UnixException e) {
             result.retval = -1;
             result.errno = e.getErrno();
             return result;
         }
 
         return result;
     }
 
     public void doExit(int rval) throws IOException {
         mLogger.debug(String.format("doExit(rval=%d)", rval));
 
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
 
     private UnixFile getFile(int fd) {
         try {
             return mFiles[fd];
         }
         catch (IndexOutOfBoundsException _) {
             return null;
         }
     }
 
     private UnixFile getValidFile(int fd) throws UnixException {
         UnixFile file = getFile(fd);
         if (file == null) {
             throw new UnixException(Errno.EBADF);
         }
         return file;
     }
 
     private void selectFds(Collection<Integer> dest, Collection<Integer> src, SelectPred pred) throws UnixException {
         for (Integer fd: src) {
             UnixFile file = getValidFile(fd.intValue());
             if (pred.isReady(file)) {
                 dest.add(fd);
             }
         }
     }
 
     static {
         mLogger = new Logging.Logger("Slave");
     }
 }
 
 /**
  * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4 filetype=java
  */
