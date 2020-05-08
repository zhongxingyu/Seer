 package org.powertac.tourney.beans;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TimeZone;
 import java.util.Timer;
 import java.util.TimerTask;
 import javax.annotation.PreDestroy;
 
 import org.powertac.tourney.constants.Constants;
 import org.powertac.tourney.scheduling.AgentLet;
 import org.powertac.tourney.scheduling.DbConnection;
 import org.powertac.tourney.scheduling.MainScheduler;
 import org.powertac.tourney.scheduling.Server;
 import org.powertac.tourney.services.Database;
 import org.powertac.tourney.services.RunBootstrap;
 import org.powertac.tourney.services.RunGame;
 import org.powertac.tourney.services.SpringApplicationContext;
 import org.powertac.tourney.services.TournamentProperties;
 import org.springframework.stereotype.Service;
 
 @Service("scheduler")
 public class Scheduler
 {
 
   private TournamentProperties tournamentProperties;
 
   public static final String key = "scheduler";
   public static boolean running = false;
   public boolean multigame = false;
 
   public boolean bootrunning = false;
 
   HashMap<Server, AgentLet[]> games = new HashMap<Server, AgentLet[]>();
 
   private Timer watchDogTimer = null;
 
   private MainScheduler scheduler;
   private Tournament runningTournament;
 
   SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
 
   private HashMap<Integer, Integer> AgentIdToBrokerId =
     new HashMap<Integer, Integer>();
   private HashMap<Integer, Integer> ServerIdToMachineId =
     new HashMap<Integer, Integer>();
 
   private HashMap<Integer, Timer> bootToBeRun = new HashMap<Integer, Timer>();
   private HashMap<Integer, Timer> simToBeRun = new HashMap<Integer, Timer>();
 
   public static String getKey ()
   {
     return key;
   }
 
   public boolean isRunning ()
   {
     return watchDogTimer != null;
   }
 
   @PreDestroy
   public void cleanUp () throws Exception
   {
     System.out
             .println("[INFO] Spring Container is destroyed! Scheduler clean up");
     if (watchDogTimer != null) {
       watchDogTimer.cancel();
     }
     for (Timer t: bootToBeRun.values()) {
       if (t != null) {
         t.cancel();
       }
     }
     for (Timer t: simToBeRun.values()) {
       if (t != null) {
         t.cancel();
       }
     }
 
   }
 
   public Scheduler ()
   {
 
     dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
     // this.startWatchDog();
     lazyStart();
   }
 
   public boolean isNullTourney ()
   {
     return this.runningTournament == null;
   }
 
   public void reloadTournament ()
   {
     Database db = new Database();
     try {
       db.startTrans();
       Tournament t = db.getTournamentByType("MULTI_GAME");
       this.runningTournament = t;
       List<Machine> machines = db.getMachines();
       List<Database.Server> servers = db.getServers();
       for (int i = 0; i < servers.size(); i++) {
         ServerIdToMachineId.put(servers.get(i).getServerNumber(),
                                 machines.get(i).getMachineId());
       }
 
       // Initially no one is registered so set brokerId's to -1
       List<Database.Agent> agents = db.getAgents();
       for (int i = 0; i < agents.size(); i++) {
         AgentIdToBrokerId.put(agents.get(i).getInternalAgentID(), -1);
       }
 
       int noofagents = t.getMaxBrokers();// maxBrokers;
       int noofcopies = t.getMaxBrokerInstances();// maxBrokerInstances;
       int noofservers = machines.size();
       int[] gtypes = { t.getSize1(), t.getSize2(), t.getSize3() };
       int[] mxs =
         { t.getNumberSize1(), t.getNumberSize2(), t.getNumberSize3() };
 
       try {
         scheduler =
           new MainScheduler(noofagents, noofcopies, noofservers, gtypes, mxs);
         scheduler.initServerPanel(noofservers);
         scheduler.initializeAgentsDB(noofagents, noofcopies);
         scheduler.initGameCube(gtypes, mxs);
         scheduler.resetCube();
         runningTournament = t;
 
       }
       catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
 
       db.commitTrans();
       if (t != null) {
         System.out.println("[INFO] Reloading Tournament: "
                            + t.getTournamentName());
       }
       else {
         System.out.println("[INFO] No tournament to reload");
       }
     }
     catch (Exception e) {
       System.out.println("Error retrieving tourney");
       e.printStackTrace();
     }
   }
 
   public void initTournament (Tournament t, List<Machine> machines)
   {
     Database db3 = new Database();
     try {
       db3.startTrans();
       db3.truncateScheduler();
       db3.commitTrans();
     }
     catch (Exception e) {
       db3.abortTrans();
       e.printStackTrace();
     }
 
     int noofagents = t.getMaxBrokers();// maxBrokers;
     int noofcopies = t.getMaxBrokerInstances();// maxBrokerInstances;
     int noofservers = machines.size();
     int[] gtypes = { t.getSize1(), t.getSize2(), t.getSize3() };
     int[] mxs = { t.getNumberSize1(), t.getNumberSize2(), t.getNumberSize3() };
 
     Database db = new Database();
     try {
       scheduler =
         new MainScheduler(noofagents, noofcopies, noofservers, gtypes, mxs);
       scheduler.initServerPanel(noofservers);
       scheduler.initializeAgentsDB(noofagents, noofcopies);
       scheduler.initGameCube(gtypes, mxs);
       scheduler.resetCube();
       runningTournament = t;
 
       db.startTrans();
       // db.truncateScheduler();
       List<Database.Server> servers = db.getServers();
       System.out.println("[INFO] Size of servers: " + servers.size());
       System.out.println("[INFO] Size of machines: " + machines.size());
       for (int i = 0; i < servers.size(); i++) {
         ServerIdToMachineId.put(servers.get(i).getServerNumber(),
                                 machines.get(i).getMachineId());
       }
 
       // Initially no one is registered so set brokerId's to -1
       List<Database.Agent> agents = db.getAgents();
       for (int i = 0; i < agents.size(); i++) {
         AgentIdToBrokerId.put(agents.get(i).getInternalAgentID(), -1);
       }
 
       db.commitTrans();
 
     }
     catch (Exception e) {
       db.abortTrans();
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
   }
 
   // Resets the internal scheduling tables;
   public synchronized void resetServer (int machineId)
   {
 
     // Find the serverId from a machineId
     int serverNumber = 0;
     for (Integer i: ServerIdToMachineId.keySet()) {
       if (ServerIdToMachineId.get(i) == machineId) {
         serverNumber = i;
         break;
       }
     }
 
     DbConnection db = new DbConnection();
     try {
       db.Setup();
       System.out.println("[INFO] Freeing agents on " + serverNumber);
       String freeAgents = "%s";
      String.format(freeAgents, Constants.FREE_AGENTS_ON_SERVER);
       freeAgents.replace("?", String.valueOf(serverNumber));
       System.out.println("[INFO] Query: " + freeAgents);
       db.SetQuery(freeAgents, "update");
     }
     catch (Exception e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     System.out.println("[INFO] Agents freed");
 
     try {
       scheduler.resetServers(serverNumber);
       System.out.println("[INFO] Servers freed");
     }
     catch (Exception e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
 
   }
 
   public void tickScheduler ()
   {
     if (runningTournament != null) {
       if (runningTournament.getStartTime().before(new Date())) {
         System.out
                 .println("[INFO] Multigame tournament available, ticking scheduler..");
       }
       else {
         System.out.println("[INFO] Too early to start tournament: "
                            + runningTournament.getTournamentName());
         return;
       }
 
       try {
 
         String hostip = "http://";
 
         try {
           InetAddress thisIp = InetAddress.getLocalHost();
           hostip += thisIp.getHostAddress() + ":8080";
         }
         catch (UnknownHostException e2) {
           e2.printStackTrace();
         }
 
         Database db = new Database();
         db.startTrans();
         List<Game> gamesInTourney =
           db.getGamesInTourney(runningTournament.getTournamentId());
         List<Broker> brokersInTourney =
           db.getBrokersInTournament(runningTournament.getTournamentId());
         int i = 0;
         System.out.println("[INFO] Brokers in Tournament: "
                            + brokersInTourney.size() + " TourneyId: "
                            + runningTournament.getTournamentId());
         for (int agentId: AgentIdToBrokerId.keySet()) {
           if (i >= brokersInTourney.size()) {
             break;
           }
           AgentIdToBrokerId.put(agentId, brokersInTourney.get(i++)
                   .getBrokerId());
         }
 
         db.commitTrans();
 
         int tourneySize = gamesInTourney.size();
         List<Game> finalGames = new ArrayList<Game>();
         for (int j = 0; j < tourneySize; j++) {
           Game g = gamesInTourney.get(j);
           if (!g.isHasBootstrp()
               || g.getStatus().equalsIgnoreCase("game-pending")
               || g.getStatus().equalsIgnoreCase("game-in-progress")
               || g.getStatus().equalsIgnoreCase("game-complete")) {
             // gamesInTourney.remove(g);
           }
           else {
             finalGames.add(g);
           }
         }
         gamesInTourney = finalGames;
 
         if (gamesInTourney.size() == 0) {
           System.out
                   .println("[INFO] Tournament is either complete or not enough bootstraps are available");
           return;
         }
         else {
           System.out.println("[INFO] Games with boots available "
                              + gamesInTourney.size());
         }
 
         if (!scheduler.equilibrium()) {
 
           if (games.isEmpty()) {
             System.out.println("[INFO] Acquiring new schedule...");
             games = scheduler.Schedule();
           }
           System.out.println("[INFO] WatchDogTimer reports " + games.size()
                              + " tournament game(s) are ready to start");
 
           List<Server> servers = new ArrayList<Server>(games.keySet());
           for (Server s: servers) {
             if (gamesInTourney.size() == 0) {
               break;
             }
             AgentLet[] agentSet = games.get(s);
 
             System.out.println("[INFO] Server " + s.getServerNumber()
                                + " playing");
 
             for (AgentLet a: agentSet) {
               System.out.println("[INFO] Agent " + a.getAgentType());
             }
 
             String result = "";
             for (Integer key: ServerIdToMachineId.keySet()) {
               result += key + ",";
             }
             System.out
                     .println("[INFO] Key Set in serversToMachines: " + result);
             Integer machineId = ServerIdToMachineId.get(s.getServerNumber());
 
             List<Integer> brokerSet = new ArrayList<Integer>();
             for (AgentLet a: agentSet) {
               brokerSet.add(AgentIdToBrokerId.get(a.getAgentType()));
             }
             System.out.println("[INFO] BrokerSet Size " + brokerSet.size());
 
             System.out.println("[INFO] Games ready");
             Game somegame = gamesInTourney.get(0);
             gamesInTourney.remove(somegame);
 
             System.out.println("[INFO] " + dateFormatUTC.format(new Date())
                                + " : Game: " + somegame.getGameId()
                                + " will be started...");
 
             Database db1 = new Database();
             db1.startTrans();
             Machine m = db1.getMachineById(machineId);
             String brokers = "";
             for (Integer b: brokerSet) {
               Broker tmp = db1.getBroker(b);
               System.out.println("[INFO] Adding broker " + tmp.getBrokerId()
                                  + " to game " + somegame.getGameId());
               db1.addBrokerToGame(somegame.getGameId(), tmp);
               brokers += tmp.getBrokerName() + ",";
             }
             db1.commitTrans();
 
             int lastIndex = brokers.length();
             brokers = brokers.substring(0, lastIndex - 1);
 
             System.out.println("[INFO] Tourney Game " + somegame.getGameId()
                                + " Brokers: " + brokers);
 
             Scheduler.this
                     .runSimTimer(somegame.getGameId(),
                                  new RunGame(
                                              somegame.getGameId(),
                                              hostip + "/TournamentScheduler/",
                                              runningTournament.getPomUrl(),
                                              tournamentProperties
                                                      .getProperty("destination"),
                                              m, brokers), new Date());
 
             games.remove(s);
             // Wait for jenkins
             Thread.sleep(1000);
 
           }
         }
       }
       catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
     }
     else {
       System.out.println("[INFO] No multigame tournament available");
 
     }
   }
 
   public void lazyStart ()
   {
     Timer t = new Timer();
     TimerTask tt = new TimerTask() {
 
       @Override
       public void run ()
       {
         Scheduler.this.tournamentProperties =
           (TournamentProperties) SpringApplicationContext
                   .getBean("tournamentProperties");
         Scheduler.this.startWatchDog();
 
       }
 
     };
     t.schedule(tt, 3000);
   }
 
   public synchronized void startWatchDog ()
   {
     if (!running) {
       running = true;
 
       Timer t = new Timer();
       TimerTask watchDog = new TimerTask() {
         Database db;
 
         @Override
         public void run ()
         {
           // Run watchDog
           db = new Database();
 
           // Run the scheduler
           Scheduler.this.tickScheduler();
           checkForSims();
           checkForBoots();
 
         }
 
         public void checkForSims ()
         {
           System.out.println("[INFO] " + dateFormatUTC.format(new Date())
                              + " : WatchDogTimer Looking for Games To Start..");
           // Check Database for startable games
           try {
             // db.openConnection();
             db.startTrans();
             List<Game> games;
             if (runningTournament == null) {
               games = db.getStartableGames();
             }
             else {
               System.out
                       .println("[INFO] WatchDog CheckForSims ignoring multi-game tournament games");
               games = db.getStartableGames(runningTournament.getTournamentId());
             }
             System.out.println("[INFO] WatchDogTimer reports " + games.size()
                                + " game(s) are ready to start");
 
             String hostip = "http://";
 
             try {
               InetAddress thisIp = InetAddress.getLocalHost();
               hostip += thisIp.getHostAddress() + ":8080";
             }
             catch (UnknownHostException e2) {
               e2.printStackTrace();
             }
 
             for (Game g: games) {
               Tournament t = db.getTournamentByGameId(g.getGameId());
               System.out.println("[INFO] " + dateFormatUTC.format(new Date())
                                  + " : Game: " + g.getGameId()
                                  + " will be started...");
               Scheduler.this
                       .runSimTimer(g.getGameId(),
                                    new RunGame(
                                                g.getGameId(),
                                                hostip + "/TournamentScheduler/",
                                                t.getPomUrl(),
                                                tournamentProperties
                                                        .getProperty("destination")),
                                    new Date());
               try {
                 // Wait for jenkins
                 Thread.sleep(1000);
               }
               catch (InterruptedException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
               }
             }
             db.commitTrans();
           }
           catch (SQLException e) {
             db.abortTrans();
             this.cancel();
             e.printStackTrace();
           }
         }
 
         public void checkForBoots ()
         {
 
           if (!bootrunning) {
 
             System.out
                     .println("[INFO] "
                              + dateFormatUTC.format(new Date())
                              + " : WatchDogTimer Looking for Bootstraps To Start..");
             // Check Database for startable games
             try {
               db.startTrans();
               List<Game> games = db.getBootableGames();
               System.out.println("[INFO] WatchDogTimer reports " + games.size()
                                  + " boots are ready to start");
 
               String hostip = "http://";
 
               try {
                 InetAddress thisIp = InetAddress.getLocalHost();
                 hostip += thisIp.getHostAddress() + ":8080";
               }
               catch (UnknownHostException e2) {
                 e2.printStackTrace();
               }
 
               if (games.size() > 0) {
                 bootrunning = true;
                 Game g = games.get(0);
 
                 Tournament t = db.getTournamentByGameId(g.getGameId());
                 // db.closeConnection();
 
                 System.out.println("[INFO] " + dateFormatUTC.format(new Date())
                                    + " : Boot: " + g.getGameId()
                                    + " will be started...");
 
                 Scheduler.this
                         .runBootTimer(g.getGameId(),
                                       new RunBootstrap(
                                                        g.getGameId(),
                                                        hostip
                                                                + "/TournamentScheduler/",
                                                        t.getPomUrl(),
                                                        tournamentProperties
                                                                .getProperty("destination"),
                                                        tournamentProperties
                                                                .getProperty("bootserverName")),
                                       new Date());
 
               }
               db.commitTrans();
             }
             catch (SQLException e) {
               this.cancel();
               db.abortTrans();
               e.printStackTrace();
             }
 
           }
           else {
             System.out.println("[INFO] " + dateFormatUTC.format(new Date())
                                + " : WatchDogTimer Reports a boot is running");
 
             Database db = new Database();
             List<Game> games = new ArrayList<Game>();
             try {
               db.startTrans();
               games = db.getBootableGames();
               // db.closeConnection();
             }
             catch (SQLException e) {
               db.abortTrans();
               e.printStackTrace();
             }
             System.out.println("[INFO] WatchDogTimer reports " + games.size()
                                + " boot(s) are ready to start");
 
           }
         }
       };
 
       System.out.println("[INFO] " + dateFormatUTC.format(new Date())
                          + " : Starting WatchDog...");
 
       long watchDogInt =
         Integer.parseInt(tournamentProperties
                 .getProperty("scheduler.watchDogInterval", "120000"));
 
       t.schedule(watchDog, new Date(), watchDogInt);
 
       this.watchDogTimer = t;
 
     }
     else {
       System.out.println("[WARN] Watchdog already running");
     }
   }
 
   public void restartWatchDog ()
   {
     this.stopWatchDog();
     this.startWatchDog();
 
   }
 
   public void stopWatchDog ()
   {
     if (watchDogTimer != null) {
       watchDogTimer.cancel();
       running = false;
       System.out.println("[INFO] " + dateFormatUTC.format(new Date())
                          + " : Stopping WatchDog...");
     }
     else {
       System.out.println("[WARN] " + dateFormatUTC.format(new Date())
                          + " : WatchDogTimer Already Stopped");
     }
   }
 
   public void runBootTimer (int gameId, TimerTask t, Date time)
   {
     Timer timer = new Timer();
     timer.schedule(t, time);
     bootToBeRun.put(gameId, timer);
   }
 
   public void runSimTimer (int gameId, TimerTask t, Date time)
   {
     Timer timer = new Timer();
     timer.schedule(t, time);
     simToBeRun.put(gameId, timer);
   }
 
   public void deleteSimTimer (int gameId)
   {
     Timer t = simToBeRun.get(gameId);
     if (t != null) {
       t.cancel();
       simToBeRun.remove(gameId);
     }
     else {
       System.out.println("Timer thread is null for game: " + gameId);
     }
   }
 
   public void deleteBootTimer (int gameId)
   {
     Timer t = bootToBeRun.get(gameId);
     if (t != null) {
       t.cancel();
       bootToBeRun.remove(gameId);
     }
     else {
       System.out.println("Timer thread is null for game: " + gameId);
     }
   }
 
 }
