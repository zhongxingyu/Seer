 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package serverghome;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
     /**
     *
     * @author anisbenyoub
     */
     public class ServerGHome extends Thread{
         
         protected 
            int nbClick;
             boolean serverOn;
             Map<InetAddress,ComClient> myClientMap;
             
         public ServerGHome()
         {
            nbClick=0;
             myClientMap=new HashMap<InetAddress,ComClient>();
             serverOn=true;
         }
     @Override
         public  void run() 
         {
                 ServerSocket socketserver = null  ;
 		BufferedReader in;
 		PrintWriter out;
 		
 		try {
 		
 			socketserver = new ServerSocket(8880);
 			System.out.println("Le serveur est à l'écoute du port "+socketserver.getLocalPort());
                         while(serverOn)
                         {
                             System.out.println("remise en attente");
                             Socket client ;
                             client = socketserver.accept();
                             System.out.println("trucrecu");
                             if(myClientMap.get(client.getInetAddress())!=null)        
                             {
                                  out = new PrintWriter(client.getOutputStream());
                                  out.println("C0");
                                  out.flush();
                                  System.out.println("Deja repertorie");
                                 
                             }
                             else
                             {
                                  System.out.println("Nouveau");
                                  ComClient aNewComClient =  new ComClient(socketserver,client,this);
                                  myClientMap.put(client.getInetAddress(), aNewComClient);
                                  aNewComClient.start();
                             }
 
 
                         }
 		        
 		}catch (IOException e) {
 			
 			e.printStackTrace();
 		}
         try {
             socketserver.close();
         } catch (IOException ex) {
             Logger.getLogger(ServerGHome.class.getName()).log(Level.SEVERE, null, ex);
         }
         }
     public synchronized void removeClient(InetAddress address)
     {
         System.out.println("Client supprime");
         System.out.println(myClientMap.size());
         myClientMap.remove(address);
         System.out.println(myClientMap.size());
 
     }
         public synchronized void sendClick()
     {
        nbClick++;
         System.out.println("Click recu");
 
 
     }
        public synchronized int getNbClick()
        {
            return nbClick;
        }
     }
