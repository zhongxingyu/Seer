 package jp.gr.java_conf.neko_daisuki.fsyscall.slave;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.io.PrintStream;
 import java.util.LinkedList;
 import java.util.List;
 
 import jp.gr.java_conf.neko_daisuki.fsyscall.Pid;
 
 public class Application {
 
     private static class Pipe {
 
         private PipedInputStream mIn;
         private PipedOutputStream mOut;
 
         public Pipe() throws IOException {
             mIn = new PipedInputStream();
             mOut = new PipedOutputStream(mIn);
         }
 
         public InputStream getInput() {
             return mIn;
         }
 
         public OutputStream getOutput() {
             return mOut;
         }
     }
 
     private Pid mPid;
     private List<Worker> mWorkers;
 
     public Application() {
         mPid = new Pid(0);
         mWorkers = new LinkedList<Worker>();
     }
 
     public void addWorker(Worker worker) {
         mWorkers.add(worker);
     }
 
     public void run(InputStream in, OutputStream out) throws IOException, InterruptedException {
         Pipe slave2hub = new Pipe();
         Pipe hub2slave = new Pipe();
         Slave slave = new Slave(
                 new Pid(mPid),
                 hub2slave.getInput(), slave2hub.getOutput());
         SlaveHub hub = new SlaveHub(
                 this,
                 in, out,
                 slave2hub.getInput(), hub2slave.getOutput());
         addWorker(hub);
         addWorker(slave);
 
         while (1 < mWorkers.size()) {
             waitReady();
             kickWorkers();
         }
        hub.close();
     }
 
     private void kickWorkerIfReady(Worker worker) throws IOException {
         if (!worker.isReady()) {
             return;
         }
         worker.work();
     }
 
     private void kickWorkers() throws IOException {
         for (Worker worker: mWorkers) {
             kickWorkerIfReady(worker);
         }
     }
 
     private void waitReady() throws IOException, InterruptedException {
         while (!isReady()) {
             Thread.sleep(10 /* msec */);
         }
     }
 
     private boolean isReady() throws IOException {
         boolean ready = false;
         int size = mWorkers.size();
         for (int i = 0; (i < size) && !ready; i++) {
             ready = mWorkers.get(i).isReady();
         }
         return ready;
     }
 
     private static void usage(PrintStream out) {
         out.println("usage: Main rfd wfd");
     }
 
     /**
      * This is the tester method. This class requires two parameters -- the file
      * descriptor to read and another one to write. But, Java DOES NOT HAVE ANY
      * WAYS to make InputStream/OutputStream from file descriptor number. So
      * this method opens <code>/dev/fd/<var>rfd</var></code> and
      * <code>/dev/fd/<var>wfd</var></code> to make streams. This way is
      * available on FreeBSD 9.1 with fdescfs mounted on <code>/dev/fd</code>. I
      * do not know if the same way is usable on other platforms.
      */
     public static void main(String[] args) {
         int rfd, wfd;
         try {
             rfd = Integer.parseInt(args[0]);
             wfd = Integer.parseInt(args[1]);
         }
         catch (ArrayIndexOutOfBoundsException _) {
             Application.usage(System.out);
             return;
         }
         catch (NumberFormatException _) {
             Application.usage(System.out);
             return;
         }
         String fmt = "/dev/fd/%d";
         String inPath = String.format(fmt, new Integer(rfd));
         String outPath = String.format(fmt, new Integer(wfd));
         InputStream in;
         OutputStream out;
         try {
             in = new FileInputStream(inPath);
             out = new FileOutputStream(outPath);
         }
         catch (IOException e) {
             e.printStackTrace();
             return;
         }
 
         try {
             new Application().run(in, out);
         }
         catch (Throwable e) {
             e.printStackTrace();
         }
     }
 }
 
 /**
  * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
  */
