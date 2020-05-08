 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package engine;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.event.KeyEvent;
 import java.io.*;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class TcpServer {
 
     private String pathPrincipal;
 
     public void setPathPrincipal(String pathPrincipal) {
         this.pathPrincipal = pathPrincipal;
     }
 
     public void start() {
         String mensagem = null;
 
         try {
             ServerSocket ss = new ServerSocket(7000);
             while (true) {
                 Socket socket = ss.accept();
                 System.out.println(socket.getInetAddress());
 
                 DataInputStream in = new DataInputStream(socket.getInputStream());
                 mensagem = in.readUTF();
                 System.out.println(mensagem);//mensagem recebida do cliente
 
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
 
 
                 if (mensagem.equals("lista")) {
                     //byte[] b = toBytes(walk("d:\\series\\", false));
                     byte[] b = toBytes(walk(pathPrincipal));
                     out.write(b);
                 }
                 
                 if (mensagem.equals("setFullScreenOn")) {
                     setFullScreen();
                 }
 
                 if (mensagem.equals("..Voltar")) {
                     System.out.println(tempLocal);
                     String[] et = tempLocal.split("\\\\");
                     String result = "";
 
                     for (int i = 0; i < et.length - 1; i++) {
                         result += et[i] + "\\";
                     }
 
                     tempLocal = result;
 
                     byte[] b = toBytes(walk(tempLocal));
                     out.write(b);
                 }
 
                 if (listaDir.contains(mensagem)) {
                     byte[] b = toBytes(walk(tempLocal + mensagem + "\\"));
                     out.write(b);
                 } else {
                     if (!mensagem.equals("..Voltar")) {
                         executeFile(tempLocal + mensagem);
                     }
 
                 }
 
                 in.close();
                 out.close();
                 socket.close();
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
     }
     private List<String> listaDir;
     private String tempLocal = "";
 
     public List<String> walk(String path) {
         tempLocal = path;
         listaDir = new ArrayList<String>();
         List<String> lista = new ArrayList<String>();
         File root = new File(path);
         File[] list = root.listFiles();
         System.out.println(path);
 
        if (!path.equals(pathPrincipal)) {
             lista.add("..Voltar");
         }
 
         for (File f : list) {
             if (f.isDirectory()) {
                 lista.add(f.getName());
                 listaDir.add(f.getName());
             } else {
                 lista.add(f.getName());
             }
         }
         return lista;
     }
 
     private void executeFile(String path) {
         File root = new File(path);
         try {
             Runtime.getRuntime().exec("cmd.exe /c \"" + root.getAbsolutePath() + "\"");
         } catch (IOException ex) {
             Logger.getLogger(TcpServer.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void setFullScreen() {
         Robot robot;
         try {
             robot = new Robot();
             robot.keyPress(KeyEvent.VK_ALT);
             robot.keyPress(KeyEvent.VK_ENTER);
             robot.keyRelease(KeyEvent.VK_ENTER);
             robot.keyRelease(KeyEvent.VK_ALT);
         } catch (AWTException ex) {
             Logger.getLogger(TcpServer.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private byte[] toBytes(List<String> lista) {
         byte[] yourBytes = null;
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutput out = null;
         try {
             out = new ObjectOutputStream(bos);
             out.writeObject(lista);
             yourBytes = bos.toByteArray();
 
             out.close();
             bos.close();
 
         } catch (Exception ex) {
             Logger.getLogger(TcpServer.class.getName()).log(Level.SEVERE, null, ex);
         }
         return yourBytes;
     }
 }
