 import java.io.*;
 
 public class TransactionalFileOutputStream extends OutputStream implements Serializable {
 
   //private RandomAccessFile raf;
   private File f;
 
   private int curidx;
 
   public TransactionalFileOutputStream(String fpath) {
     f = new File(fpath);
     if(f.exists())
       f.delete();
     
     curidx = 0;
     //RandomAccessFile raf = null;
     try {
       f.createNewFile();
       //raf = new RandomAccessFile(f, "rw");
     } catch (FileNotFoundException e) {
       System.err.println("Output file not found exception.");
       e.printStackTrace();
     } catch (IOException e) {
       System.err.println("Create output file error.");
       e.printStackTrace();
     }
 
   }
   
   
 
   @Override
   public void write(int wbyte) throws IOException {
     RandomAccessFile raf = new RandomAccessFile(f, "rw");
     raf.seek(curidx);
     
     //System.out.println(curidx);
     
     curidx++;
     raf.write(wbyte);
     raf.close();
   }
    
   /*@Override
   public void write(byte[] b, int len, int off) {
     try { 
       RandomAccessFile raf = new RandomAccessFile(f, "rw");
       raf.seek(curidx);
       raf.write(b, len, off);
       curidx += len;
       raf.close();
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
   
   @Override
   public void write(byte[] b) {
     try {
       RandomAccessFile raf = new RandomAccessFile(f, "rw");
       raf.seek(curidx);
       raf.write(b);
       curidx += b.length;
       raf.close();
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
   */
 
 }
