 package museumassault;
 
 import java.util.Random;
 import museumassault.monitor.Corridor;
 import museumassault.monitor.Logger;
 import museumassault.monitor.Room;
 import museumassault.monitor.SharedSite;
 
 /**
  * MuseumAssault main class.
  *
  * @author Andre Cruz <andremiguelcruz@ua.pt>
  */
 public class MuseumAssault
 {
     /**
      * Program entry point.
      *
      * @param args the command line arguments
      */
     public static void main(String[] args)
     {
     	// Configurable params
         int nrChiefs = 1;
         int nrTeams = 5;
         int nrThievesPerTeam = 3;
         int nrTotalThieves = 7;
         int nrRooms = 5;
         int maxDistanceBetweenThieves = 1;
         int maxDistanceBetweenRoomAndOutside = 10;
         int maxPowerPerThief = 5;
         int maxCanvasInRoom = 20;
         String logFileName = "log.txt";
 
         Random random = new Random();
         Logger logger = new Logger(logFileName);
 
        // Initializing the necessary entities
         Team[] teams = new Team[nrTeams];
         for (int x = 0; x < nrTeams; x++) {
             teams[x] = new Team(x + 1, nrThievesPerTeam);
         }
 
         Room[] rooms = new Room[nrRooms];
         for (int x = 0; x < nrRooms; x++) {
             rooms[x] = new Room(x + 1, random.nextInt(maxCanvasInRoom - 1) + 1, new Corridor((random.nextInt(maxDistanceBetweenRoomAndOutside - 1) + 1), maxDistanceBetweenThieves, logger), logger);
         }
 
         SharedSite site = new SharedSite(rooms, teams, logger, (nrChiefs > 1));
 
         Thief[] thieves = new Thief[nrTotalThieves];
         for (int x = 0; x < nrTotalThieves; x++) {
             Thief thief = new Thief(x + 1, random.nextInt(maxPowerPerThief - 1) + 1, site);
             thieves[x] = thief;
         }
 
         Chief[] chiefs = new Chief[nrChiefs];
         for (int x = 0; x < nrChiefs; x++) {
             Chief chief = new Chief(x + 1, site);
             chiefs[x] = chief;
         }
 
         // Initialize the logger
         logger.initialize(chiefs, thieves, teams, rooms);
 
         // Start the threads
         for (int x = 0; x < nrTotalThieves; x++) {
             thieves[x].start();
         }
 
         for (int x = 0; x < nrChiefs; x++) {
             chiefs[x].start();
         }
 
         // Wait for the chiefs to join
         for (int x = 0; x < nrChiefs; x++) {
             try {
                 chiefs[x].join();
             } catch (InterruptedException e) {}
         }
 
         logger.terminateLog(site.getNrCollectedCanvas());
 
         System.out.println("Program terminated successfully, please check the log file.");
         System.exit(0);
     }
 }
