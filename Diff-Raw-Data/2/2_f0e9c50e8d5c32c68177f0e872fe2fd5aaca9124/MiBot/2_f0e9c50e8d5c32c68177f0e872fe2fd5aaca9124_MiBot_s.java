 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mibot;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Iterator;
 import soc.qase.ai.waypoint.WaypointMapGenerator;
 
 /**
  *
  * @author Cayetano
  */
 public class MiBot {
     
     /**
      * @param args the command line arguments
      */
  
     static MiBotseMueve MiBot,MiBot2;  
     
     public static void main(String[] args) {
         // TODO code application logic here
         Init();	
     }
     
     public static void Init()
 	{		
                 Rutas rutas = new Rutas();
         
 		//Establece la ruta del quake2, necesaria para tener informaciÃ³n sobre los mapas.
 		//Observa la doble barra
 		String quake2_path=rutas.quake2_path; 
 		//System.setProperty("QUAKE2", quake2_path); 
 
 		//CreaciÃ³n del bot (pueden crearse mÃºltiples bots)
 		MiBot = new MiBotseMueve("Venus","female/athena");
                 //MiBot2 = new MiBotseMueve("Marte","male/athena");
 		//MiBot.setMap(WaypointMapGenerator.generate(rutas.Map_path, (float)0.2)); //MiBot2.setMap(WaypointMapGenerator.generate("C:\\Users\\alvarin\\Desktop\\Dropbox\\Quinto\\AIA\\Quake\\quake2\\baseq2\\demos\\level.dm2", (float)0.2));
 		//Conecta con el localhost (el servidor debe estar ya lanzado para que se produzca la conexiÃ³n)
		MiBot.connect(getIpAddress(),27910);//Ejemplo de conexiÃ³n a la mÃ¡quina local
                 System.out.println("Connection State: ");
                 System.out.println(MiBot.isConnected());
                 System.out.println("...");
                 Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                     @Override
                     public void run() {
                         MiBot.disconnect();
                     }
                 }));
                 //MiBot2.connect("127.0.0.1",27910);//Ejemplo de conexiÃ³n a la mÃ¡quina local  
                 System.out.println("...");
 	}
         public static String getIpAddress(){
         String res = "127.0.0.1";
 
         try{
 
             for(Enumeration ifaces = NetworkInterface.getNetworkInterfaces();ifaces.hasMoreElements();){
                 NetworkInterface iface = (NetworkInterface)ifaces.nextElement();
 
                 Enumeration nets = NetworkInterface.getNetworkInterfaces();
                 for (Iterator it = Collections.list(nets).iterator(); it.hasNext();) {
                     NetworkInterface netint = (NetworkInterface) it.next();
                     Enumeration inetAddresses = netint.getInetAddresses();
                     for (Iterator it2 = Collections.list(inetAddresses).iterator(); it2.hasNext();) {
                         InetAddress inetAddress = (InetAddress) it2.next();
                         if (netint.getName().indexOf("eth0") != -1) {
                             res = inetAddress.toString();
                         }
                         if (netint.getName().indexOf("wlan0") != -1) {
                             res = inetAddress.toString();
                         }
                     }
                 }
             }
         }catch (SocketException e){
             System.out.println( "Error reading IP address" );
         }
         //Address is /address
         //so the / must be deleted
         res = res.split("/")[1].trim();
         return res;
     }
 }
 
