 package org.powertac.tourney.services;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TimeZone;
 
 import javax.faces.context.FacesContext;
 
 import org.powertac.tourney.beans.Game;
 import org.powertac.tourney.beans.Scheduler;
 import org.powertac.tourney.beans.Tournament;
 import org.powertac.tourney.constants.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service("rest")
 public class Rest
 {
 
   private Scheduler scheduler;
  private HashMap<String,Integer> skip = new HashMap<String,Integer>();
 
   public String parseBrokerLogin (Map<?, ?> params)
   {
     String responseType = ((String[]) params.get(Constants.REQ_PARAM_TYPE))[0];
     String brokerAuthToken =
       ((String[]) params.get(Constants.REQ_PARAM_AUTH_TOKEN))[0];
     String competitionName =
       ((String[]) params.get(Constants.REQ_PARAM_JOIN))[0];
 
     SimpleDateFormat dateFormatUTC =
       new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
     dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
 
     String retryResponse;
     String loginResponse;
     String doneResponse;
 
     if (responseType.equalsIgnoreCase("xml")) {
       retryResponse =
         "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message><retry>%d</retry></message>";
       loginResponse =
         "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message><login><jmsUrl>%s</jmsUrl><gameToken>%s</gameToken></login></message>";
       doneResponse =
         "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message><done></done></message>";
     }
     else {
       retryResponse = "{\n \"retry\":%d\n}";
       loginResponse = "{\n \"login\":%d\n \"jmsUrl\":%s\n \"gameToken\":%s\n}";
       doneResponse = "{\n \"done\":\"true\"\n}";
     }
     Database db = new Database();
 
     try {
       // db.openConnection();
       db.startTrans();
       List<Game> allGames = db.getGames();
       List<Tournament> allTournaments = db.getTournaments("pending");
       allTournaments.addAll(db.getTournaments("in-progress"));
       if (competitionName != null && allGames != null) {
 
         // First find all games that match the competition name and have brokers
         // registered
         List<Game> matches = new ArrayList<Game>();
         for (Game g: allGames) {
           // Only consider games that have started and are ready for
           // brokers to join
           Tournament t = db.getTournamentByGameId(g.getGameId());
           // System.out.println("Game: " + g.getGameId() + " Status: " +
           // g.getStatus());
           if (g.getStatus().equalsIgnoreCase("game-in-progress")) {
 
             if (competitionName.equalsIgnoreCase(t.getTournamentName())
                 && g.isBrokerRegistered(brokerAuthToken)) {
 
               if(skip.containsKey(brokerAuthToken) && skip.get(brokerAuthToken) == g.getGameId()){
                 System.out.println("[INFO] Broker " + brokerAuthToken + " already recieved login for game " + g.getGameId());
                 continue;
               }
               System.out.println("[INFO] Sending login to : " + brokerAuthToken
                                  + " jmsUrl : " + g.getJmsUrl());
               skip.put(brokerAuthToken, g.getGameId());
 
               return String.format(loginResponse, g.getJmsUrl(), "1234");
             }
 
           }
 
 
         }
         db.commitTrans();
 
         boolean competitionExists = false;
 
         for (Tournament t: allTournaments) {
           if (competitionName.equals(t.getTournamentName())) {
             competitionExists = true;
             break;
           }
         }
 
         if (competitionExists) {
 
           System.out.println("[INFO] Broker: " + brokerAuthToken
                              + " attempted to log into existing tournament: "
                              + competitionName + " --sending retry");
 
           return String.format(retryResponse, 20);
         }
         else {
           System.out
                   .println("[INFO] Broker: " + brokerAuthToken
                            + " attempted to log into non-existing tournament: "
                            + competitionName + " --sending done");
           return doneResponse;
         }
 
       }
 
     }
     catch (Exception e) {
       db.abortTrans();
       e.printStackTrace();
     }
 
     return doneResponse;
   }
 
   public String parseServerInterface (Map<?, ?> params)
   {
     scheduler = (Scheduler) SpringApplicationContext.getBean("scheduler");
 
     if (params != null) {
       Properties props = new Properties();
       try {
         props.load(Database.class.getClassLoader()
                 .getResourceAsStream("/tournament.properties"));
       }
       catch (IOException e) {
         e.printStackTrace();
       }
 
       String actionString =
         ((String[]) params.get(Constants.REQ_PARAM_ACTION))[0];
 
       if (actionString.equalsIgnoreCase("status")) {
         String statusString =
           ((String[]) params.get(Constants.REQ_PARAM_STATUS))[0];
         String gameIdString =
           ((String[]) params.get(Constants.REQ_PARAM_GAME_ID))[0];
         int gameId = Integer.parseInt(gameIdString);
 
         if (statusString.equalsIgnoreCase("bootstrap-running")) {
           System.out
                   .println("[INFO] Recieved bootstrap running message from game: "
                            + gameId);
           Database db = new Database();
           try {
 
             db.startTrans();
             db.updateGameStatusById(gameId, "boot-in-progress");
             System.out.println("[INFO] Setting game: " + gameId
                                + " to boot-in-progress");
             db.commitTrans();
             return "Success";
           }
           catch (SQLException e) {
             db.abortTrans();
             e.printStackTrace();
           }
 
         }
         else if (statusString.equalsIgnoreCase("bootstrap-done")) {
           System.out
                   .println("[INFO] Recieved bootstrap done message from game: "
                            + gameId);
 
           String hostip = "http://";
 
           try {
             InetAddress thisIp = InetAddress.getLocalHost();
             hostip += thisIp.getHostAddress() + ":8080";
           }
           catch (UnknownHostException e2) {
             e2.printStackTrace();
           }
           Database db = new Database();
           try {
             db.startTrans();
             db.updateGameBootstrapById(gameId,
                                        hostip
                                                + "/TournamentScheduler/faces/poms.jsp?location="
                                                + props.getProperty("fileUploadLocation")
                                                + gameId + "-boot.xml");
             db.updateGameStatusById(gameId, "boot-complete");
             System.out.println("[INFO] Setting game: " + gameId
                                + " to boot-complete");
 
             scheduler.bootrunning = false;
             Game g = db.getGame(gameId);
             db.setMachineStatus(g.getMachineId(), "idle");
             db.commitTrans();
           }
           catch (Exception e) {
             db.abortTrans();
             e.printStackTrace();
           }
           return "Success";
 
         }
         else if (statusString.equalsIgnoreCase("game-ready")) {
           System.out.println("[INFO] Recieved game ready message from game: "
                              + gameId);
           Database db = new Database();
           try {
             db.startTrans();
             db.updateGameStatusById(gameId, "game-in-progress");
             System.out.println("[INFO] Setting game: " + gameId
                                + " to game-in-progress");
             // Tournament t = db.getTournamentByGameId(gameId);
             // db.updateTournamentStatus(t.getTournamentId());
             db.commitTrans();
           }
           catch (SQLException e) {
             db.abortTrans();
             e.printStackTrace();
           }
           return "success";
         }
         else if (statusString.equalsIgnoreCase("game-running")) {
           // TODO Implement a message from the server to the ts
 
         }
         else if (statusString.equalsIgnoreCase("game-done")) {
           System.out.println("[INFO] Recieved game done message from game: "
                              + gameId);
           Database db = new Database();
           try {
             db.startTrans();
             db.updateGameStatusById(gameId, "game-complete");
             System.out.println("[INFO] Setting game: " + gameId
                                + " to game-complete");
             Game g = db.getGame(gameId);
             // Do some cleanup
             db.updateGameFreeBrokers(gameId);
             System.out.println("[INFO] Freeing Brokers for game: " + gameId);
             db.updateGameFreeMachine(gameId);
             System.out.println("[INFO] Freeing Machines for game: " + gameId);
             
             scheduler.resetServer(g.getMachineId());
 
             db.setMachineStatus(g.getMachineId(), "idle");
             db.commitTrans();
           }
           catch (Exception e) {
             db.abortTrans();
             e.printStackTrace();
           }
           return "success";
         }
         else if (statusString.equalsIgnoreCase("game-failed")) {
           System.out.println("[WARN] GAME " + gameId + " FAILED!");
           Database db = new Database();
           try {
             db.startTrans();
             db.updateGameStatusById(gameId, "game-failed");
             Game g = db.getGame(gameId);
 
             db.updateGameFreeBrokers(gameId);
             db.updateGameFreeMachine(gameId);
             
 
             scheduler.resetServer(g.getMachineId());
             db.setMachineStatus(g.getMachineId(), "idle");
 
             db.commitTrans();
           }
           catch (SQLException e) {
             db.abortTrans();
             e.printStackTrace();
           }
           return "success";
         }
         else if (statusString.equalsIgnoreCase("boot-failed")) {
           System.out.println("[WARN] GAME " + gameId + " FAILED!");
           Database db = new Database();
           try {
             db.startTrans();
             db.updateGameStatusById(gameId, "boot-failed");
             Game g = db.getGame(gameId);
             db.setMachineStatus(g.getMachineId(), "idle");
             db.commitTrans();
           }
           catch (SQLException e) {
             db.abortTrans();
             e.printStackTrace();
           }
           return "success";
         }
         else {
           return "ERROR";
         }
 
       }
     }
     return "Not Yet Implementented";
   }
 
   /***
    * Returns a properties file string
    * 
    * @param params
    * @return String representing a properties file
    */
   public String parseProperties (Map<?, ?> params)
   {
     String gameId = "0";
     if (params != null) {
       try {
         gameId = ((String[]) params.get(Constants.REQ_PARAM_GAME_ID))[0];
       }
       catch (Exception e) {
 
       }
     }
 
     List<String> props = new ArrayList<String>();
 
     props = CreateProperties.getPropertiesForGameId(Integer.parseInt(gameId));
 
     String result = "";
 
     // Location of weather data
     String weatherLocation = "server.weatherService.weatherLocation = ";
     // Simulation base time
     String startTime = "common.competition.simulationBaseTime = ";
     // Simulation jmsUrl
     String jms = "server.jmsManagementService.jmsBrokerUrl = ";
 
     // Visualizer Settings
     String remote = "server.visualizerProxyService.remoteVisualizer = ";// true";
 
     String queueName = "server.visualizerProxyService.visualizerQueueName = ";
 
     // Test Settings
     String minTimeslot = "common.competition.minimumTimeslotCount = 220";
     String expectedTimeslot = "common.competition.expectedTimeslotCount = 240";
     String serverFirstTimeout = "server.competitionControlService.firstLoginTimeout = 600000";
 
     // Timeout Settings
     String serverTimeout =
       "server.competitionControlService.loginTimeout = 120000";
 
     if (props.size() == 4) {
       result += weatherLocation + props.get(0) + "\n";
       result += startTime + props.get(1) + "\n";
       result += jms + props.get(2) + "\n";
       result += serverFirstTimeout + "\n";
       result += serverTimeout + "\n";
       if (props.get(2).length() > 2) {
         result += remote + "true\n";
       }
       else {
         result += remote + "\n";
       }
       result += minTimeslot + "\n";
       result += expectedTimeslot + "\n";
       result += queueName + props.get(3) + "\n";
 
     }
 
     return result;
   }
 
   /***
    * Returns a pom file string
    * 
    * @param params
    * @return String representing a pom file
    */
   public String parsePom (Map<?, ?> params)
   {
     String location = "";
     if (params != null) {
       try {
         location = ((String[]) params.get(Constants.REQ_PARAM_POM))[0];
       }
       catch (Exception e) {
 
       }
     }
 
     String result = "";
 
     try {
       // Open the file that is the first
       // command line parameter
       List<String> path = new ArrayList<String>();
       String[] pathArray = (location.split("/"));
       for (String s: pathArray) {
         path.add(s.replace("..", ""));
       }
       Properties props = new Properties();
       try {
         props.load(Database.class.getClassLoader()
                 .getResourceAsStream("/tournament.properties"));
       }
       catch (IOException e) {
         e.printStackTrace();
       }
 
       FileInputStream fstream =
         new FileInputStream(props.getProperty("fileUploadLocation",
                                               "/export/scratch")
                             + path.get(path.size() - 1));
       // Get the object of DataInputStream
       DataInputStream in = new DataInputStream(fstream);
       BufferedReader br = new BufferedReader(new InputStreamReader(in));
       String strLine;
       // Read File Line By Line
       while ((strLine = br.readLine()) != null) {
         // Print the content on the console
         // System.out.println (strLine);
         result += strLine + "\n";
       }
       // Close the input stream
       fstream.close();
       in.close();
       br.close();
     }
     catch (Exception e) {// Catch exception if any
       System.err.println("Error: " + e.getMessage());
     }
 
     return result;
   }
 
 }
