 package bck;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /* thread que vai enviar chunks para fazer backup no receiver*/
 public class Sender extends Thread {
 
     MulticastSocket socket = null;
     InetAddress address = null;
     int MC;
     int MD;
     String sha = "";
     int replication_degree;
     boolean delete;
     
     public Sender(InetAddress ad, int m_c, int m_d, String sh, int rd, boolean dlt) throws IOException {
 
         address = ad;
         MC = m_c;
         MD = m_d;
         sha = sh;
         replication_degree = rd;
         socket = new MulticastSocket(MD);
         socket.joinGroup(address);
         delete = dlt;
         
     }
 
     @Override
     @SuppressWarnings("SleepWhileInLoop")
     public void run() {
         FileOutputStream file = null;
 
         System.out.println("Sending...");
         int n = 0;
         int retransmit = 0;
         int time_interval_double = 1;
         
         //Adiciona o ficheiro que está a enviar, aos array de ficheiros enviados
         Backup.getSendedFiles().add(sha);
         while(retransmit < 5){
             HashMap<String, byte[]> file_to_send_chunks = Backup.getMapChunkFiles().get(sha);
             n = 0;
             
             while (file_to_send_chunks.get(String.valueOf(n)) != null) {
                System.out.println("enviando chunk no "+n);
                 String msg = "PUTCHUNK " + Backup.getVersion() + " " + this.sha + " " + n
                         + " " + replication_degree + "\n\n";
                 byte[] msg_byte = msg.getBytes();
                 byte[] final_msg = new byte[msg_byte.length + file_to_send_chunks.get(String.valueOf(n)).length];
                 System.arraycopy(msg_byte, 0, final_msg, 0, msg_byte.length);
                 System.arraycopy(file_to_send_chunks.get(String.valueOf(n)), 0, final_msg, msg_byte.length, file_to_send_chunks.get(String.valueOf(n)).length);
 
                 DatagramPacket chunk = new DatagramPacket(final_msg, final_msg.length, this.address, this.MD);
 
                 try {
                     Thread.sleep(100);
                     socket.send(chunk);
                     Backup.getMissingChunks(sha).put(n, replication_degree);
                 } catch (Exception ex) {
                     Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 n++;
             }
             
             try {
                 Thread.sleep(500 * time_interval_double);
             } catch (InterruptedException ex) {
                 Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
             }
 
             if (!Backup.getMissingChunks(sha).isEmpty()){
                 retransmit++;
                 time_interval_double *= 2;
                 System.out.println("Trying to send missing chunks... try#"+retransmit);
                 
                 if(retransmit == 5)
                     Utils.flag_sending = 2;
             }
             else{
                 Utils.flag_sending = 0;
                 break;
             }
         }
         if(delete)
             Backup.getMapChunkFiles().remove(sha);
     }
 }
