 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.Socket;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.trend.Packet;
 
 
 public class RequestProcessor implements Runnable {
     private static List<Socket> pool = new LinkedList<Socket>();
     private File localFile = null;
     private String filename ="";
     private long filesize = 0;
     private BufferedOutputStream fout = null;
     private MessageDigest mdsum;
     
     public static void processRequest(Socket request) {
         synchronized(pool) {
             pool.add(pool.size(), request);
             pool.notifyAll();
         }
     }
     
     private void writeResponse(boolean success, Packet.Ack.AckType type, BufferedOutputStream conOut) throws IOException {
         Packet.Ack.Builder ackBuilder = Packet.Ack.newBuilder();
         ackBuilder.setType(type);
         ackBuilder.setSuccess(success);
         Packet.Ack response = ackBuilder.build();
         response.writeDelimitedTo(conOut);
         conOut.flush();        
     }
     
     private Packet.Block getFileBlock(BufferedInputStream in ) throws IOException {
         Packet.Block.Builder blockBuilder = Packet.Block.newBuilder();
         blockBuilder.mergeDelimitedFrom(in);
         Packet.Block block = blockBuilder.build();
         System.out.println(String.format("Receive a new block(Seq:%d Size:%d Digest:%s EOF:%s)", 
                 block.getSeqNum(), block.getSize(), block.getDigest(), block.getEof()));
         
         return block;
     }
     
     
     @Override
     public void run() {
         while (true) {
             Socket connection;
             synchronized(pool) {
                 while (pool.isEmpty()) {
                     try {
                         pool.wait();                        
                     }
                     catch (InterruptedException e) {                        
                     }
                 }
                 
                 connection = pool.remove(0);
                 System.out.println("Accept a client from "+connection.getInetAddress().toString());
             }                
             
             try {
                 BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                 BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
                 if (mdsum == null) 
                     mdsum = MessageDigest.getInstance("MD5");
                 else
                     mdsum.reset();
                 
                 if (fout == null) {
                     Packet.Header.Builder headerBuilder = Packet.Header.newBuilder();
                     headerBuilder.mergeDelimitedFrom(in);
                     Packet.Header header = headerBuilder.build();                    
                     fout = new BufferedOutputStream(new FileOutputStream("/tmp/"+header.getFileName()));
                     filesize = header.getFileSize();
                     writeResponse(true, Packet.Ack.AckType.HEADER, out);
                     System.out.println(String.format("Receive a new Header(filename:%s size:%d)",header.getFileName(), header.getFileSize()));                   
                 }
                                 
                 if (fout != null) {
                     while (true) {
                         Packet.Block block = getFileBlock(in);
                         if (block.getEof()) {
                             String digest = String.valueOf(mdsum.digest());
                             if (block.getDigest().equals(digest)) 
                                 writeResponse(true, Packet.Ack.AckType.EOF, out);
                             else
                                 writeResponse(false, Packet.Ack.AckType.EOF, out);
                             break;
                         }
                                               
                         byte[] content = block.getContent().toByteArray();
                         MessageDigest md = MessageDigest.getInstance("MD5");
                         md.update(content);
                        String digest = String.valueOf(md.digest());
                         if (block.getDigest().equals(digest)) {
                             fout.write(content, 0, block.getSize());                           
                             writeResponse(true, Packet.Ack.AckType.BLOCK, out);
                             mdsum.update(content);
                         }
                         else {
                             writeResponse(false, Packet.Ack.AckType.BLOCK, out);
                         }                        
                     }                    
                 }                
             }
             catch (IOException e) {
                 e.printStackTrace();
             } catch (NoSuchAlgorithmException e) {
                 e.printStackTrace();
             }
             finally {
                 try {
                     if (connection != null) connection.close();
                     if (fout != null) {
                         fout.flush();
                         fout.close();
                         fout = null;
                     }
                 }
                 catch (Exception e) {
                     // Todo:
                 }
             }
             
         } // end while
     } 
 
 }
