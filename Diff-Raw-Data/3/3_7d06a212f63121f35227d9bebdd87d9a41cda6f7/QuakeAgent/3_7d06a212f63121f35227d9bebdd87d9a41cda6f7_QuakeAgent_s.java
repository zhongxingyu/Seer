 package quakeagent;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Vector;
 import java.util.Random;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import soc.qase.bot.ObserverBot;
 import soc.qase.file.bsp.BSPParser;
 import soc.qase.state.Player;
 import soc.qase.state.PlayerMove;
 import soc.qase.state.World;
 import soc.qase.tools.vecmath.Vector3f;
 
 import soc.qase.state.*;
 
 import java.lang.Math;
 
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.SocketException;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Iterator;
 import jess.*;
 import soc.qase.ai.waypoint.WaypointMap;
 import soc.qase.ai.waypoint.WaypointMapGenerator;
 
 import java.util.Random;
 
 public class QuakeAgent {
     public static final int N_BOTS = 1;
     static SimpleBot[] botArray = new SimpleBot[N_BOTS];
     
     public static void main(String[] args) throws IOException {
         // TODO code application logic here
         Init();	
     }
     
     
     public static void Init() throws IOException {
 			
         Configuration.init();
         /*
         testViking();
         return;
         */
         // Set path to quake2 dir. This is necesary in order to get information
         // about the maps.
         String quake2_path=Configuration.getProperty( "quake2_path" );
         System.setProperty("QUAKE2", quake2_path);
         //WaypointMap map = WaypointMapGenerator.generate(Configuration.getProperty( "map_information_path"), (float)0.4);
         WaypointMap map = WaypointMap.loadMap( Configuration.getProperty( "map_waypoints_path"));
         //map.saveMap("/home/garoe/gitUniversidad/aia_practicas/maps_information/q2dm1w04.waypoint");
         
         //Give the share data a copy to the map, for internal calculations
         ShareData.setMap(map);
         
         for(int i = 0; i < N_BOTS; i++){
 	        // Bot creation (more than one can be created).
         	botArray[i] = new SimpleBot("KillBot" + Integer.toString(i) ,"female/athena");
 	        //Generate all the waypoints to move around the map
         	botArray[i].setMap(map); 
 	        // Connect to the server (localhost).
         	botArray[i].connect(getIpAddress(), 27910);
         }
          
         
         //When closing the application disconect from the server
         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
             @Override
             public void run() {
             	for(int i = 0; i < N_BOTS; i++){
             		botArray[i].disconnect();
             	}
             }
         }));
          
     }
     
     // Get the ip of this machine.
     public static String getIpAddress(){
         String res = "127.0.0.1";
 
         try{
 
             for(Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();ifaces.hasMoreElements();){
                //NetworkInterface iface = (NetworkInterface)ifaces.nextElement();

                 Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                 for (Iterator<NetworkInterface> it = Collections.list(nets).iterator(); it.hasNext();) {
                     NetworkInterface netint = (NetworkInterface) it.next();
                     Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                     for (Iterator<InetAddress> it2 = Collections.list(inetAddresses).iterator(); it2.hasNext();) {
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
     
     public static void testCLP() throws IOException
     {
         Rete engine = new Rete();
         try {
             System.out.println( "1" );
             Configuration.init();
             System.out.println( "2" );
             System.out.println( Configuration.getProperty( "clp_path" ) + "general.clp" );
             engine.batch( Configuration.getProperty( "clp_path" ) + "general.clp" );
             System.out.println( "3" );
             //engine.eval("(reset)");
             //engine.assertString("(color rojo)");
             //engine.assertString("(health 150)");
             
             int[] health_values = { 15, 30, 50, 100 };
             int[] armor_values = { 15, 30, 50, 100 };
             
             int[][] test_values =
             {
                 { 15, 30, 0, 0 },
                 { 30, 15, 0, 0 },
                 { 15, 60, 15, 0 },
                 { 15, 60, 60, 30 },
                 { 15, 60, 60, 60 },
                 { 60, 15, 60, 30 },
                 { 60, 15, 60, 60 },
                 { 15, 150, 60, 30 },
                 { 15, 150, 60, 60 },
                 { 150, 15, 60, 30 },
                 { 150, 15, 60, 60 }
             };
             
             String[] result_values =
             {
                 "health",
                 "armor",
                 "ammo",
                 "weapon"
             };
             
             for( int i=0; i<4; i++ ){
                 //engine.clear();
                 engine.reset();
                 
                 Fact f = new Fact("bot-state", engine );
                 f.setSlotValue("health", new Value( test_values[i][0], RU.INTEGER));
                 f.setSlotValue("armor", new Value( test_values[i][1], RU.INTEGER));
                 f.setSlotValue("ammo", new Value( test_values[i][2], RU.INTEGER));
                 f.setSlotValue("fire-power", new Value( test_values[i][3], RU.INTEGER));
                 engine.assertFact(f);
 
                 engine.run();
                 Value v = engine.eval("?*preferred-object*");
                 System.out.println(v.stringValue(engine.getGlobalContext()));
                 
                 // Iterator it = new FilteringIterator(engine.listFacts(), new Filter.ByModule("RESULTS"));
             }
             
             
 
             //Value v = engine.eval("?*preferred-object*");
             //System.out.println(v.stringValue(engine.getGlobalContext()));
         } catch (JessException je) {
             System.out.println("initBot: Error in line " + je.getLineNumber());
             System.out.println("Code:\n" + je.getProgramText());
             System.out.println("Message:\n" + je.getMessage());
             System.out.println("Aborted");
             System.exit(1);
         }
     }
           
     
     public static void testViking() throws FileNotFoundException, IOException{
         Viking viking = new Viking();
         
         //viking.loadFromFile( Configuration.getProperty( "battle_experience_path" ) );
         
         int [] diff = { -30, -50, 55, 30 };
                 
         viking.printBattleExperience();
         
         viking.attackEnemy( diff );
         //viking.addBattleExperience( diff, BattleResult.UNFINISHED );
         
         
     }
 }
