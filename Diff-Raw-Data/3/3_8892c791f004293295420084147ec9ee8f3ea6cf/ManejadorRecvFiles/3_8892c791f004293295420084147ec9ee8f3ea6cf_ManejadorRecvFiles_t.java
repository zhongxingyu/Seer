 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Threads.ClaseAlumno;
 
 import Threads.ClaseMaestro.*;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author zerg
  */
 public class ManejadorRecvFiles implements Runnable {
 
     private ServerSocket serverSocket;
     private File pathLocal;
     private static int bufferSize = 1000000; //1M
     private ObjectInputStream ois;
     private String fileName;
 
     public ManejadorRecvFiles(ServerSocket elServerSocket, File pathLocal) {
         serverSocket = elServerSocket;
         this.pathLocal = pathLocal;
     }
 
     @Override
     public void run() {
         try {
             int count;
             Socket socket = serverSocket.accept();
             ois = new ObjectInputStream(socket.getInputStream());
             byte[] buffer = new byte[bufferSize];
             //Obtengo el archivo remoto
             File pathRemoto = (File) ois.readObject();
             //Extraigo el nombre
             fileName = pathRemoto.getName();
             //El archivo se escribe en "pathLocal/fileName"
             String file = pathLocal.getPath() + "/" + fileName;
             //Abro el archivo en disco
             FileOutputStream fos = new FileOutputStream(file);
             //Recibo el archivo y escribo en disco
             while (!socket.isClosed() && (count = ois.read(buffer)) > 0) {
                 fos.write(buffer, 0, count);
             }
             ois.close();
             fos.close();
             if (!socket.isClosed()) {
                 socket.close();
             }
         } catch (ClassNotFoundException ex) {
             Logger.getLogger(ManejadorRecvFiles.class.getName()).log(Level.SEVERE, null, ex);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(ManejadorSendFiles.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(ManejadorSendFiles.class.getName()).log(Level.SEVERE, null, ex);
             JOptionPane.showMessageDialog(new JFrame(), "Fallo al recibir " + fileName);
         }
        JOptionPane.showMessageDialog(new JFrame(), "Finalizó recepción de " + fileName);
     }
 }
