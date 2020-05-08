 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.zip.Adler32;
 import java.util.zip.CheckedOutputStream;
 import java.util.zip.GZIPOutputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 /**
  * ZipProcess is a implmentation of MigratableProcess, which can zip a input file to a specific
  * output position.
  * 
  * @author Zeyuan Li
  * */
 public class GzipProcess implements MigratableProcess {
   private static final long serialVersionUID = 3L;
   private TransactionalFileInputStream inFile;
 
   private TransactionalFileOutputStream outFile;
 
 //   private FileInputStream inFile;
 //   private FileOutputStream outFile;
 
   private String id;
 
   private String pathPrefix, infilename;
 
   private volatile boolean suspending;
 
   public GzipProcess(String args[]) throws Exception {
     if (args.length != 3) {
       // System.out.println(args.length + " " + args[0] +" "+ args[1]);
       System.out.println("usage: ZipProcess <inputFile1> <outputFile>");
       throw new Exception(" Invalid Arguments");
     }
 
     // TODO: pathPrefix is a afs prefix for input/output file
     pathPrefix = "";
     String[] tmp = args[1].split("/");
     id = tmp[tmp.length - 1] + "_ZipProcess";
     infilename = args[1];
 //     inFile = new FileInputStream(args[1]);
 //     outFile = new FileOutputStream(args[2]);
     inFile = new TransactionalFileInputStream(args[1]);
     outFile = new TransactionalFileOutputStream(args[2]);
   }
 
   /**
    * default constructor for transfer processes around nodes and resume process
    */
   public GzipProcess() {
   }
 
   /**
    * Note: some zip code is adapted from Chapter I/O in book "Think in Java"
    * */
   public void run() {
     /*String objname = pathPrefix + "data/serialize/" + id + ".dat";
     File objFile = new File(objname);
     
     // if it resumes running, read object in
     if (objFile.exists()) {
       try {
         ObjectInputStream in = new ObjectInputStream(new TransactionalFileInputStream(objname));
         GzipProcess zp = (GzipProcess) in.readObject();
         this.id = zp.id;
         this.inFile = zp.inFile;
         this.outFile = zp.outFile;
         this.pathPrefix = zp.pathPrefix;
         this.suspending = zp.suspending;
         // delete serialized file
         objFile.delete();
       } catch (IOException e) {
         System.err.println("Deserialize IOException " + objname);
         e.printStackTrace();
       } catch (ClassNotFoundException e) {
         System.err.println("Deserialize ClassNotFoundException " + objname);
         e.printStackTrace();
       }
     }*/
     
     try {
       GZIPOutputStream gos = new GZIPOutputStream(outFile);
       
       while (!suspending) {
         // read and write a byte each time
         int c = inFile.read();
         if (c == -1) {
           // Checksum valid only after the file has been closed!
           System.out
                   .println("[GzipProcess]: Write done! ");
           break;
         }
         gos.write(c);
         gos.flush();
 
         // Make ZipProcess take longer
         try {
           Thread.sleep(1000);  
         } catch (InterruptedException e) {
           // ignore it
         }
      }
       // close the stream
       gos.flush();
       gos.close();
     } catch (EOFException e) {
       // End of File
     } catch (IOException e) {
       System.out.println("[ZipProcess]: Error: " + e);
     }
 
     // wake up suspend() so that we can call suspend() next time.
     suspending = false;
     System.out.println("Gzip: run finished.");
     System.out.flush(); 
   }
 
 
   public void suspend() {
     System.out.println("Gzip: suspend() is called.");
     System.out.flush();
     suspending = true;
     while (suspending)
       ;
     System.out.println("Gzip: suspend() finished.");
     System.out.flush();
 
   }
   
   /*public void serialize() {
     // package up
     // TODO: this path need to be on afs so that multiple processes can access
     String objname = pathPrefix + "data/serialize/" + id + ".dat";
     try {
       ObjectOutput s = new ObjectOutputStream(new TransactionalFileOutputStream(objname));
       s.writeObject(this);
       s.flush();
       s.close();
 
     } catch (FileNotFoundException e1) {
       System.err.println("Serialize file not found. id:" + id);
       e1.printStackTrace();
     } catch (IOException e1) {
       System.err.println("Serialize file io exception. id:" + id);
       System.err.println(e1);
       e1.printStackTrace();
     }
   }*/
 
   @Override
   public String toString() { 
     return id;
   }
 
   public static void main(String args[]) throws Exception {
     String[] s = { "GzipProcess", "data/test.txt", "data/res.gz" };
     GzipProcess zp = new GzipProcess(s);
     Thread t = new Thread(zp);
     t.start();
     Thread.sleep(1000);
 
     Serializer se = new Serializer();
     String fpath = se.serialize(zp);
     zp = (GzipProcess) se.deserialize(fpath);
     //Thread.sleep(1000);
     t = new Thread(zp); 
     t.start();
   }
 
 }
