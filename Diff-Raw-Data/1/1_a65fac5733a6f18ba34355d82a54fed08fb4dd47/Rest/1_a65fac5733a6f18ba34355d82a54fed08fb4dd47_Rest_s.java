 package org.powertac.tourney.services;
 
 import org.apache.log4j.Logger;
 import org.powertac.tourney.beans.Broker;
 import org.powertac.tourney.beans.Game;
 import org.powertac.tourney.beans.Tournament;
 import org.powertac.tourney.constants.Constants;
 import org.springframework.stereotype.Service;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.*;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 @Service("rest")
 public class Rest
 {
   private static Logger log = Logger.getLogger("TMLogger");
 
   public String parseBrokerLogin (Map<String, String[]> params)
   {
     String responseType = params.get(Constants.Rest.REQ_PARAM_TYPE)[0];
     String brokerAuthToken = params.get(Constants.Rest.REQ_PARAM_AUTH_TOKEN)[0];
     String tournamentName = params.get(Constants.Rest.REQ_PARAM_JOIN)[0];
     log.info(String.format("Broker %s login request : %s",
         brokerAuthToken, tournamentName));
 
     String retryResponse = "{\n \"retry\":%d\n}";
     String loginResponse = "{\n \"login\":%d\n \"jmsUrl\":%s\n \"queueName\":%s\n \"serverQueue\":%s\n}";
     String doneResponse = "{\n \"done\":\"true\"\n}";
     if (responseType.equalsIgnoreCase("xml")) {
       String head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message>";
       String tail = "</message>";
       retryResponse = head + "<retry>%d</retry>" + tail;
       loginResponse = head + "<login><jmsUrl>%s</jmsUrl><queueName>%s</queueName><serverQueue>%s</serverQueue></login>" + tail;
       doneResponse = head + "<done></done>" + tail;
     }
 
     if (tournamentName == null) {
       log.info("Tournament name is empty, sending done response");
       return doneResponse;
     }
 
     // TODO Get only GameList for this tournament.
     List<Game> allGames = Game.getGamesInTourney(tournamentName);
     if (!allGames.isEmpty()) {
       Database db = new Database();
       try {
         db.startTrans();
 
         // TODO Should this be a config item?
         // TODO Move this to sql?
         long readyDeadline = 2*60*1000;
         long nowStamp = new Date().getTime();
 
         // Find games matching the competition name and have brokers registered
         for (Game g: allGames) {
           // Only consider games that are more than X minutes ready, to allow Viz Login
           long readyStamp = g.getReadyTime().getTime();
           if (nowStamp < (readyStamp + readyDeadline)) {
             continue;
           }
 
           Broker broker = g.getBrokerRegistration(brokerAuthToken);
           if (broker != null && !broker.getBrokerInGame()) {
             broker.setBrokerInGame(true);
             db.updateBrokerInGame(g.getGameId(), broker);
            db.commitTrans();
             log.info(String.format("Sending login to broker %s : %s, %s, %s",
                 broker.getBrokerName(), g.getJmsUrl(),
                 broker.getQueueName(), g.getServerQueue()));
             return String.format(loginResponse, g.getJmsUrl(),
                                  broker.getQueueName(), g.getServerQueue());
           }
         }
       }
       catch (Exception e) {
         log.error(e.getMessage());
         log.error("Error, sending done response");
         return doneResponse;
       }
       finally {
         db.abortTrans();
       }
     }
 
     // TODO Do sql check
     // JEC - this could be REALLY long lists after a while.
     for (Tournament t: Tournament.getTournamentList()) {
       if (tournamentName.equals(t.getTournamentName())) {
         log.debug(String.format("Broker: %s attempted to log into existing "
             + "tournament: %s --sending retry", brokerAuthToken, tournamentName));
         return String.format(retryResponse, 60);
       }
     }
 
     log.debug(String.format("Broker: %s attempted to log into non-existing "
         + "tournament: %s --sending done", brokerAuthToken, tournamentName));
     return doneResponse;
   }
 
   /**
    * Handles a login GET request from a visualizer of the form<br/>
    * &nbsp;../visualizerLogin.jsp?machineName<br/>
    * Response is either retry(n) to tell the viz to wait n seconds and try again,
    * or queueName(qn) to tell the visualizer to connect to its machine and
    * listen on the queue named qn.
    */
   public String parseVisualizerLogin (HttpServletRequest request,
                                       Map<String, String[]> params)
   {
     log.info("Visualizer login request");
     String machineName = params.get("machineName")[0];
     String head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message>";
     String tail = "</message>";
     String retryResponse = head + "<retry>%d</retry>" + tail;
     String loginResponse = head + "<login><queueName>%s</queueName><serverQueue>%s</serverQueue></login>" + tail;
     String errorResponse = head + "<error>%s</error>" + tail; 
 
     // Validate source of request
     if (!validateVizRequest(request)) {
       return String.format(errorResponse, "invalid login request");
     }
 
     // If there is a game in game_ready state on the machine for this viz,
     // then return a login with the correct queue name; otherwise, return
     // a retry.
     List<Game> readyGames;
     Database db = new Database();
     try {
       db.startTrans();
       // Only allow to log in to 'ready' games
       readyGames = db.findGamesByStatusAndMachine(Game.STATE.game_ready,
                                                       machineName);
       //readyGames.addAll(db.findGamesByStatusAndMachine(Game.STATE.game_pending,
       //                                                 machineName));
       if (readyGames.isEmpty()) {
         db.commitTrans();
         log.debug("No games available, retry visualizer");
         return String.format(retryResponse, 60);
       }
       else {
         // We'll use the first Game in the list
         Game candidate = readyGames.get(0);
         String queue = candidate.getVisualizerQueue();
         String svrQueue = candidate.getServerQueue();
         db.commitTrans();
         log.info("Game available, login visualizer, " + queue +", "+ svrQueue);
         return String.format(loginResponse, queue, svrQueue);
       }
     }
     catch (SQLException e) {
       db.abortTrans();
       log.error(e.toString());
       e.printStackTrace();
       return String.format(errorResponse, "database error");
     }
   }
   
   private boolean validateVizRequest (HttpServletRequest request)
   {
     // TODO
     String host = request.getRemoteHost();
     log.info("Viz request from " + host);
     return true;
   }
 
   public String parseServerInterface (Map<String, String[]> params, String clientAddress)
   {
     if (!Utils.checkClientAllowed(clientAddress)) {
       return "error";
     }
 
     try {
       String actionString = params.get(Constants.Rest.REQ_PARAM_ACTION)[0];
       if (actionString.equalsIgnoreCase("status")) {
         String statusString = params.get(Constants.Rest.REQ_PARAM_STATUS)[0];
         int gameId = Integer.parseInt(
             params.get(Constants.Rest.REQ_PARAM_GAME_ID)[0]);
         return Game.handleStatus(statusString, gameId);
       }
       else if (actionString.equalsIgnoreCase("boot")) {
         String gameId = params.get(Constants.Rest.REQ_PARAM_GAME_ID)[0];
         return serveBoot(gameId);
       }
     }
     catch (Exception ignored) {}
     return "error";
   }
 
   /***
    * Returns a properties file string
    *
    * @param params
    * @return String representing a properties file
    */
   public String parseProperties (Map<String, String[]> params)
   {
     String gameId = "0";
     try {
       gameId = params.get(Constants.Rest.REQ_PARAM_GAME_ID)[0];
     }
     catch (Exception ignored) {}
 
     List<String> props;
     props = CreateProperties.getPropertiesForGameId(Integer.parseInt(gameId));
 
     Game g;
     Database db = new Database();
     try {
     	db.startTrans();
     	g = db.getGame(Integer.parseInt(gameId));
     	db.commitTrans();
     }
     catch(Exception e) {
     	db.abortTrans();
     	e.printStackTrace();
       return "";
     }
 
     String weatherLocation = "server.weatherService.weatherLocation = ";
     String startTime = "common.competition.simulationBaseTime = ";
     String jms = "server.jmsManagementService.jmsBrokerUrl = ";
     String remote = "server.visualizerProxyService.remoteVisualizer = true";
     String vizQ = "server.visualizerProxyService.visualizerQueue = ";
     String minTimeslot = "common.competition.minimumTimeslotCount = 1380";
     String expectedTimeslot = "common.competition.expectedTimeslotCount = 1440";
     String serverFirstTimeout =
       "server.competitionControlService.firstLoginTimeout = 600000";
     String serverTimeout =
       "server.competitionControlService.loginTimeout = 120000";
 
     // Test settings
     if (g.getGameName().toLowerCase().contains("test")) {
     	minTimeslot = "common.competition.minimumTimeslotCount = 200";
     	expectedTimeslot = "common.competition.expectedTimeslotCount = 220";
     }
 
     String result = "";
     // JEC - replaced a HORRIBLE magic number.
     if (props.size() > 0) {
       result += weatherLocation + props.get(0) + "\n";
     }
     if (props.size() > 1) {
       result += startTime + props.get(1) + "\n";
     }
     if (props.size() > 2) {
       if (props.get(2).isEmpty()) {
     	  result += jms + "tcp://localhost:61616" + "\n";
       }
       else {
     	  result += jms + props.get(2) + "\n";
       }
     }
     result += serverFirstTimeout + "\n";
     result += serverTimeout + "\n";
     result += remote + "\n";
     result += vizQ + g.getVisualizerQueue() + "\n";
     result += minTimeslot + "\n";
     result += expectedTimeslot + "\n";
 
     return result;
   }
 
   /***
    * Returns a pom file string
    *
    * @param params
    * @return String representing a pom file
    */
   public String parsePom (Map<String, String[]> params)
   {
     try {
       String pomId = params.get(Constants.Rest.REQ_PARAM_POM_ID)[0];
       return servePom(pomId);
     }
     catch (Exception e) {
       log.error(e.getMessage());
       return "error";
     }
   }
 
   public String servePom(String pomId)
   {
     String result = "";
     try {
       // Determine pom-file location
       TournamentProperties properties = TournamentProperties.getProperties();
       String pomLocation = properties.getProperty("pomLocation") +
           "pom."+ pomId +".xml";
 
       // Read the file
       FileInputStream fstream = new FileInputStream(pomLocation);
       DataInputStream in = new DataInputStream(fstream);
       BufferedReader br = new BufferedReader(new InputStreamReader(in));
       String strLine;
       while ((strLine = br.readLine()) != null) {
         result += strLine + "\n";
       }
 
       // Close the input stream
       fstream.close();
       in.close();
       br.close();
     }
     catch (Exception e) {
       log.error(e.getMessage());
       result = "error";
     }
 
     return result;
   }
 
   public String serveBoot(String gameId)
   {
     String result = "";
 
     try {
       // Determine boot-file location
       TournamentProperties properties = TournamentProperties.getProperties();
       String bootLocation = properties.getProperty("bootLocation") +
                             "game-" + gameId + "-boot.xml";
 
       // Read the file
       FileInputStream fstream = new FileInputStream(bootLocation);
       DataInputStream in = new DataInputStream(fstream);
       BufferedReader br = new BufferedReader(new InputStreamReader(in));
       String strLine;
       while ((strLine = br.readLine()) != null) {
         result += strLine + "\n";
       }
 
       // Close the input stream
       fstream.close();
       in.close();
       br.close();
     }
     catch (Exception e) {
       log.error(e.getMessage());
       result = "error";
     }
 
     return result;
   }
 
   /***
    * Handle 'PUT' to serverInterface.jsp, either boot.xml or (Boot|Sim) log
    */
   public String handleServerInterfacePUT (Map<String, String[]> params, HttpServletRequest request)
   {
     if (!Utils.checkClientAllowed(request.getRemoteAddr())) {
       return "error";
     }
 
     try {
       String fileName = params.get(Constants.Rest.REQ_PARAM_FILENAME)[0];
       TournamentProperties properties = TournamentProperties.getProperties();
 
       String path;
       if (fileName.endsWith("boot.xml")) {
         path = properties.getProperty("bootLocation") + fileName;
       } else {
         path = properties.getProperty("logLocation") + fileName;
       }
 
       // Write to file
       InputStream is = request.getInputStream();
       FileOutputStream fos = new FileOutputStream(path);
       byte buf[] = new byte[1024];
       int letti;
       while ((letti = is.read(buf)) > 0) {
         fos.write(buf, 0, letti);
       }
       fos.close();
     } catch (Exception e) {
       return "error";
     }
     return "success";
   }
 }
