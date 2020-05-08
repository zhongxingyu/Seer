 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.util.Arrays;
 import java.util.zip.Adler32;
 import java.util.zip.CheckedOutputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.imageio.ImageIO;
 
 /**
  * Edge detection process, which detects edges on a original image and output the detected result to
  * a specific position.
  * 
  * @author Zeyuan Li 
  * */ 
 public class EdgeProcess implements MigratableProcess {
   private static final long serialVersionUID = 2L;
   private TransactionalFileInputStream inFile;
   private TransactionalFileOutputStream outFile;
   private String id;
   private String pathPrefix;
   private boolean readDone;
   private boolean writeDone;
   private int height;
   private int width;
   private int picsize;
   private byte[] picbuf;
   private int idxbuf;
   private volatile boolean suspending;
 
   public EdgeProcess(String args[]) throws Exception {
     if (args.length != 3) {
       System.out.println("usage: EdgeDetectionProcess <inputImage> <outputImage>");
       throw new Exception("Invalid Arguments");
     }
 
     // TODO: pathPrefix is a afs prefix for input/output file
     pathPrefix = "";
     String[] tmp = args[1].split("/");
     id = tmp[tmp.length - 1] + "_EdgeProcess";
     inFile = new TransactionalFileInputStream(args[1]);
     outFile = new TransactionalFileOutputStream(args[2]);
 
     BufferedImage image = ImageIO.read(new File(args[1]));
     height = image.getHeight();
     width = image.getWidth();
    picsize = height * width;
     picbuf = new byte[picsize];
     idxbuf = 0;
   }
 
   // default constructor for transfer processes around nodes and resume process
   public EdgeProcess() {
   }
 
   /**
    * Note: some zip code is adapted from Chapter I/O in book "Think in Java"
    * */
   public void run() {
 
     try {
       while (!suspending) {
         // 1st: finised read img to byte[]. 2nd: edge detection. 3rd: write detected img (in byte[])
         // to disk
         if (!readDone) {
           int c = inFile.read();
           if (c == -1) {
             InputStream in = new ByteArrayInputStream(picbuf);
             BufferedImage imgori = ImageIO.read(in);
             in.close();
 
             // Note: following 11 lines are from
             // http://www.tomgibara.com/computer-vision/canny-edge-detector and
             // http://www.mkyong.com/java/how-to-convert-byte-to-bufferedimage-in-java/
             // detect edge
             CannyEdgeDetector detector = new CannyEdgeDetector(); 
             // adjust its parameters as desired
            detector.setLowThreshold(0.5f);
             detector.setHighThreshold(1f);
             // apply it to an image
             detector.setSourceImage(imgori);
             detector.process();
             BufferedImage imgedge = detector.getEdgesImage();
 
             // convert BufferedImage to byte array
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ImageIO.write(imgedge, "jpg", baos);
             baos.flush();
             picbuf = baos.toByteArray();
             baos.close();
             idxbuf = 0;
 
             readDone = true;
             continue;
           }
           picbuf[idxbuf++] = (byte) c;
         } else if (readDone && !writeDone) {
           // TODO: how to write streaming img
           if (idxbuf == picbuf.length) {
             System.out.println("[EdgeDectectionProcess]: Detection finished!");
             break;
           }
           outFile.write(picbuf[idxbuf++]);
         }
 
         // Make process take longer
         //Thread.sleep(1);
       }
     } catch (EOFException e) {
       // End of File
       System.err.println("[EdgeDetectionProcess]: Error: " + e);
       e.printStackTrace();
     } catch (IOException e) {
       System.err.println("[EdgeDetectionProcess]: Error: " + e);
       e.printStackTrace();
     } 
 
     // wake up suspend() so that we can call suspend() next time.
     suspending = false;
   }
 
   public void suspend() {
     suspending = true;
     while (suspending)
       ;
 
   }
 
   @Override
   public String toString() {
     return id;
   }
 
   public static void main(String[] args) throws Exception {
    String[] s = { "EdgeProcess", "data/img.jpg", "data/edgeimg.jpg" };
     EdgeProcess ep = new EdgeProcess(s);
     Thread t = new Thread(ep);
     t.start();
     Thread.sleep(1000);
 
     Serializer se = new Serializer();
     String fpath = se.serialize(ep);
     ep = (EdgeProcess) se.deserialize(fpath);
     t = new Thread(ep);
     t.start();
   }
 
 }
