 package org.powertac.tourney.services;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.powertac.tourney.beans.Agent;
 import org.powertac.tourney.beans.Broker;
 import org.powertac.tourney.beans.Game;
 import org.powertac.tourney.beans.Tournament;
 import org.powertac.tourney.constants.Constants;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.*;
 import java.util.Map;
 
 
 public class Rest
 {
   private static Logger log = Logger.getLogger("TMLogger");
 
   private TournamentProperties properties = TournamentProperties.getProperties();
 
   public synchronized String parseBrokerLogin (Map<String, String[]> params)
   {
     String responseType = params.get(Constants.Rest.REQ_PARAM_TYPE)[0];
     String brokerAuth = params.get(Constants.Rest.REQ_PARAM_AUTH_TOKEN)[0];
     String tournamentName = params.get(Constants.Rest.REQ_PARAM_JOIN)[0];
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
 
     log.info(String.format("Broker %s login request : %s",
         brokerAuth, tournamentName));
 
     Session session = HibernateUtil.getSessionFactory().openSession();
     Transaction transaction = session.beginTransaction();
     try {
       Query query = session.createQuery(Constants.HQL.GET_BROKER_BY_BROKERAUTH);
       query.setString("brokerAuth", brokerAuth);
       Broker broker = (Broker) query.uniqueResult();
 
       if (broker == null) {
         log.info("Broker doesn't exists : " + brokerAuth);
         transaction.commit();
         return doneResponse;
       }
       log.debug("Broker id is : " + broker.getBrokerId());
 
       // Check if tournament exists
       query = session.createQuery(Constants.HQL.GET_TOURNAMENT_BY_NAME);
       query.setString("tournamentName", tournamentName);
       Tournament tournament = (Tournament) query.uniqueResult();
       if (tournament == null) {
         log.info("Tournament doesn't exists : " + tournamentName);
         transaction.commit();
         return doneResponse;
       }
 
       // Check if tournament is finished
       if (tournament.stateEquals(Tournament.STATE.complete)) {
         log.info("Tournament is finished, we're done : " + tournamentName);
         transaction.commit();
         return doneResponse;
       }
 
       // Check if broker is registered for this tournament
       if (!broker.getTournamentMap().keySet().contains(tournament.getTournamentId())) {
         log.info(String.format("Broker not registered for tournament " +
             tournament.getTournamentName()));
         transaction.commit();
         return doneResponse;
       }
 
       long readyDeadline = 2*60*1000;
       long nowStamp = Utils.offsetDate().getTime();
 
       // Check if any ready games that are more than X minutes ready (to allow Viz Login)
       for (Game game: tournament.getGameMap().values()) {
         if (!game.stateEquals(Game.STATE.game_ready)) {
           continue;
         }
         Agent agent = game.getAgentMap().get(broker.getBrokerId());
         if (agent == null) {
           continue;
         }
         Agent.STATE state = Agent.STATE.valueOf(agent.getStatus());
         if (state == null || !state.equals(Agent.STATE.pending)) {
           continue;
         }
 
         log.debug("Game " + game.getGameId() + " is ready");
 
         long diff = nowStamp - game.getReadyTime().getTime();
         if (diff < readyDeadline) {
           log.debug("Broker needs to wait for the viz timeout : " +
               (readyDeadline - diff) / 1000);
           continue;
         }
 
         agent.setStatus(Agent.STATE.in_progress.toString());
         session.update(agent);
         log.info(String.format("Sending login to broker %s : %s, %s, %s",
             broker.getBrokerName(), game.getMachine().getJmsUrl(),
             agent.getBrokerQueue(), game.getServerQueue()));
         transaction.commit();
         return String.format(loginResponse, game.getMachine().getJmsUrl(),
             agent.getBrokerQueue(), game.getServerQueue());
       }
 
       log.debug(String.format("No games ready to start for tournament %s",
           tournamentName));
       MemStore.addBrokerLogin(broker.getBrokerId());
       transaction.commit();
       return String.format(retryResponse, 60);
     }
     catch (Exception e) {
       transaction.rollback();
       e.printStackTrace();
       log.error("Error, sending done response");
       return doneResponse;
     }
     finally {
       session.close();
     }
   }
 
   /**
    * Handles a login GET request from a visualizer of the form<br/>
    * &nbsp;../visualizerLogin.jsp?machineName<br/>
    * Response is either retry(n) to tell the viz to wait n seconds and try again,
    * or queueName(qn) to tell the visualizer to connect to its machine and
    * listen on the queue named qn.
    */
   public synchronized String parseVisualizerLogin (HttpServletRequest request,
                                       Map<String, String[]> params)
   {
     String machineName = params.get("machineName")[0];
     String head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message>";
     String tail = "</message>";
     String retryResponse = head + "<retry>%d</retry>" + tail;
     String loginResponse = head + "<login><queueName>%s</queueName><serverQueue>%s</serverQueue></login>" + tail;
     String errorResponse = head + "<error>%s</error>" + tail;
 
     log.info("Visualizer login request : " + machineName);
 
     // Validate source of request
     if (!MemStore.checkVizAllowed(request.getRemoteHost())) {
       return String.format(errorResponse, "invalid login request");
     }
 
     // Wait 30 seconds, game is set ready before it actually starts
     long readyDeadline = 30*1000;
     long nowStamp = Utils.offsetDate().getTime();
     Session session = HibernateUtil.getSessionFactory().openSession();
     Transaction transaction = session.beginTransaction();
     try {
       for (Game game: Game.getNotCompleteGamesList()) {
         if (game.getMachine() == null) {
           continue;
         }
         if (!game.getMachine().getMachineName().equals(machineName)) {
           continue;
         }
         if (!game.stateEquals(Game.STATE.game_ready)) {
           continue;
         }
         if ((nowStamp - game.getReadyTime().getTime()) < readyDeadline) {
           continue;
         }
 
         String queue = game.getVisualizerQueue();
         String svrQueue = game.getServerQueue();
         log.info("Game available, login visualizer, " + queue +", "+ svrQueue);
         transaction.commit();
         return String.format(loginResponse, queue, svrQueue);
       }
 
       log.debug("No games available, retry visualizer");
       MemStore.addVizLogin(machineName);
       transaction.commit();
       return String.format(retryResponse, 60);
     }
     catch (Exception e) {
       transaction.rollback();
       e.printStackTrace();
       log.error(e.toString());
       return String.format(errorResponse, "database error");
     }
     finally {
       session.close();
     }
   }
 
   public synchronized String handleServerInterface (Map<String, String[]> params,
                                        HttpServletRequest request)
   {
     String clientAddress = request.getRemoteAddr();
     try {
       String actionString = params.get(Constants.Rest.REQ_PARAM_ACTION)[0];
       if (actionString.equalsIgnoreCase(Constants.Rest.REQ_PARAM_STATUS)) {
         if (!MemStore.checkMachineAllowed(clientAddress)) {
           return "error";
         }
 
         return handleStatus(params);
       }
       else if (actionString.equalsIgnoreCase(Constants.Rest.REQ_PARAM_BOOT)) {
         String gameId = params.get(Constants.Rest.REQ_PARAM_GAMEID)[0];
         return serveBoot(gameId);
       }
       else if (actionString.equalsIgnoreCase(Constants.Rest.REQ_PARAM_HEARTBEAT)) {
         if (!MemStore.checkMachineAllowed(clientAddress)) {
           return "error";
         }
 
         return handleHeartBeat(params);
       }
     }
 
     catch (Exception ignored) {}
     return "error";
   }
 
   /***
    * Returns a properties file string
    *
    * @param params :
    * @return String representing a properties file
    */
   public String parseProperties (Map<String, String[]> params)
   {
     int gameId;
     try {
       gameId = Integer.parseInt(params.get(Constants.Rest.REQ_PARAM_GAMEID)[0]);
     }
     catch (Exception ignored) {
       return "";
     }
 
     Game game;
     Session session = HibernateUtil.getSessionFactory().openSession();
     Transaction transaction = session.beginTransaction();
     try {
       game = (Game) session.get(Game.class, gameId);
       transaction.commit();
     }
     catch (Exception e) {
       transaction.rollback();
       e.printStackTrace();
       return "";
     }
     finally {
       session.close();
     }
 
     String result = "";
     result += String.format(Constants.Props.weatherLocation, game.getLocation());
     result += String.format(Constants.Props.startTime, game.getSimStartTime());
     if (game.getMachine() != null) {
       result += String.format(Constants.Props.jms, game.getMachine().getJmsUrl());
     } else {
       result += String.format(Constants.Props.jms, "tcp://localhost:61616");
     }
     result += String.format(Constants.Props.serverFirstTimeout, 600000);
     result += String.format(Constants.Props.serverTimeout, 120000);
     result += String.format(Constants.Props.remote, true);
     result += String.format(Constants.Props.vizQ, game.getVisualizerQueue());
 
     if (game.getGameName().toLowerCase().contains("test")) {
       result += String.format(Constants.Props.minTimeslot,
           properties.getProperty("test.minTimeslot", "200"));
       result += String.format(Constants.Props.expectedTimeslot,
           properties.getProperty("test.expectedTimeslot", "220"));
     } else {
       result += String.format(Constants.Props.minTimeslot, 1380);
       result += String.format(Constants.Props.expectedTimeslot, 1440);
     }
 
     return result;
   }
 
   /***
    * Returns a pom file string
    *
    * @param params :
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
 
   private String servePom(String pomId)
   {
     String result = "";
     try {
       // Determine pom-file location
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
 
       // Close the streams
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
 
   private String serveBoot(String gameId)
   {
     String result = "";
 
     try {
       // Determine boot-file location
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
 
       // Close the streams
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
 
   private synchronized String handleStatus (Map<String, String[]> params)
   {
     String statusString = params.get(Constants.Rest.REQ_PARAM_STATUS)[0];
     int gameId = Integer.parseInt(
         params.get(Constants.Rest.REQ_PARAM_GAMEID)[0]);
 
     log.info(String.format("Received %s message from game: %s",
         statusString, gameId));
 
     Session session = HibernateUtil.getSessionFactory().openSession();
     Transaction transaction = session.beginTransaction();
     try {
       Query query = session.createQuery(Constants.HQL.GET_GAME_BY_ID);
       query.setInteger("gameId", gameId);
       Game game = (Game) query.uniqueResult();
 
       if (game == null) {
         log.warn(String.format("Trying to set status %s on non-existing "
             + "game : %s", statusString, gameId));
         return "error";
       }
 
       game.handleStatus(session, statusString);
       transaction.commit();
       return "success";
     }
     catch (Exception e) {
       transaction.rollback();
       e.printStackTrace();
       return "error";
     }
     finally {
       session.close();
 
       String[] gameLengths = params.get(Constants.Rest.REQ_PARAM_GAMELENGTH);
       if (gameLengths!=null && transaction.wasCommitted()) {
         log.info(String.format("Received gamelength %s for game %s",
             gameLengths[0], gameId));
         MemStore.addGameLength(gameId, gameLengths[0]);
       }
     }
   }
 
   public synchronized String handleHeartBeat (Map<String, String[]> params)
   {
     int gameId;
 
     try {
       String message = params.get(Constants.Rest.REQ_PARAM_MESSAGE)[0];
       gameId = Integer.parseInt(params.get(Constants.Rest.REQ_PARAM_GAMEID)[0]);
       if (!(gameId>0)) {
         log.debug("The message didn't have a gameId!");
         return "error";
       }
 
       MemStore.addGameHeartbeat(gameId, message);
     }
     catch (Exception e) {
       e.printStackTrace();
       return "error";
     }
 
     Session session = HibernateUtil.getSessionFactory().openSession();
     Transaction transaction = session.beginTransaction();
     try {
         Game game = (Game) session.get(Game.class, gameId);
         String standings = params.get(Constants.Rest.REQ_PARAM_STANDINGS)[0];
         return game.handleStandings(session, standings, false);
     }
     catch (Exception e) {
       transaction.rollback();
       e.printStackTrace();
       return "error";
     }
     finally {
       session.close();
     }
   }
 
   /***
    * Handle 'PUT' to serverInterface.jsp, either boot.xml or (Boot|Sim) log
    */
   public synchronized String handleServerInterfacePUT (Map<String, String[]> params,
                                           HttpServletRequest request)
   {
     if (!MemStore.checkMachineAllowed(request.getRemoteAddr())) {
       return "error";
     }
 
     try {
       String fileName = params.get(Constants.Rest.REQ_PARAM_FILENAME)[0];
 
       log.info("Received a file " + fileName);
 
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
       is.close();
       fos.close();
 
       if (fileName.contains("sim-logs")) {
         try {
           Runnable r = new LogParser(
               properties.getProperty("logLocation"), fileName);
           new Thread(r).start();
         }
         catch (Exception e) {
           log.error("Error creating LogParser for " + fileName);
         }
       }
     } catch (Exception e) {
       return "error";
     }
     return "success";
   }
 
   /***
    * Handle 'POST' to serverInterface.jsp, this is a end-of-game message
    */
   public synchronized String handleServerInterfacePOST (Map<String, String[]> params,
                                            HttpServletRequest request)
   {
     String remoteAddress = request.getRemoteAddr();
     if (!MemStore.checkMachineAllowed(remoteAddress)) {
       return "error";
     }
 
     try {
       String actionString = params.get(Constants.Rest.REQ_PARAM_ACTION)[0];
       if (!actionString.equalsIgnoreCase(Constants.Rest.REQ_PARAM_GAMERESULTS)){
         log.debug("The message didn't have the right action-string!");
         return "error";
       }
 
       int gameId = Integer.parseInt(
           params.get(Constants.Rest.REQ_PARAM_GAMEID)[0]);
       if (!(gameId>0)) {
         log.debug("The message didn't have a gameId!");
         return "error";
       }
 
       Session session = HibernateUtil.getSessionFactory().openSession();
       Transaction transaction = session.beginTransaction();
       try {
         Game game = (Game) session.get(Game.class, gameId);
         String standings = params.get(Constants.Rest.REQ_PARAM_MESSAGE)[0];
         return game.handleStandings(session, standings, true);
       }
       catch (Exception e) {
         transaction.rollback();
         e.printStackTrace();
         return "error";
       }
       finally {
         session.close();
       }
     }
     catch (Exception e) {
       log.error("Something went wrong with receiving the POST message!");
       log.error(e.getMessage());
       return "error";
     }
   }
 }
