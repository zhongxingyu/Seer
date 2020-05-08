 package org.powertac.tourney.constants;
 
 import org.powertac.tourney.beans.Game;
 import org.powertac.tourney.beans.Tournament;
 
 public class Constants
 {
   public class Props
   {
    public static final String weatherServerURL =
        "server.weatherService.serverUrl = %s\n";
     public static final String weatherLocation =
         "server.weatherService.weatherLocation = %s\n";
     public static final String startTime =
         "common.competition.simulationBaseTime = %s\n";
     public static final String jms =
         "server.jmsManagementService.jmsBrokerUrl = %s\n";
     public static final String serverFirstTimeout =
         "server.competitionControlService.firstLoginTimeout = %s\n";
     public static final String serverTimeout =
         "server.competitionControlService.loginTimeout = %s\n";
     public static final String remote =
         "server.visualizerProxyService.remoteVisualizer = %s\n";
     public static final String vizQ =
         "server.visualizerProxyService.visualizerQueueName = %s\n";
     public static final String minTimeslot =
         "common.competition.minimumTimeslotCount = %s\n";
     public static final String expectedTimeslot =
         "common.competition.expectedTimeslotCount = %s\n";
   }
 
   public class Rest
   {
     // Possible Rest Parameters for Broker Login
     public static final String REQ_PARAM_AUTH_TOKEN = "authToken";
     public static final String REQ_PARAM_JOIN = "requestJoin";
     public static final String REQ_PARAM_TYPE = "type";
 
     // Possible Rest Paramenters for Server Interface
     public static final String REQ_PARAM_STATUS = "status";
     public static final String REQ_PARAM_GAMEID = "gameId";
     public static final String REQ_PARAM_ACTION = "action";
     public static final String REQ_PARAM_FILENAME = "fileName";
     public static final String REQ_PARAM_MESSAGE = "message";
     public static final String REQ_PARAM_BOOT = "boot";
     public static final String REQ_PARAM_HEARTBEAT = "heartbeat";
     public static final String REQ_PARAM_GAMERESULTS = "gameresults";
     public static final String REQ_PARAM_GAMELENGTH = "gameLength";
     public static final String REQ_PARAM_STANDINGS = "standings";
 
     // Possible Rest Parameters for pom service
     public static final String REQ_PARAM_POM_ID = "pomId";
   }
 
   public static class HQL
   {
     public static final String GET_USERS =
         "FROM User AS user ";
 
     public static final String GET_USER_BY_NAME =
         "FROM User AS user "
             + "LEFT JOIN FETCH user.brokerMap AS brokerMap "
             + "LEFT JOIN FETCH brokerMap.tournamentMap AS tournamentMap "
             + "WHERE user.userName =:userName ";
 
     public static final String GET_POMS =
         "FROM Pom AS pom "
           + " LEFT JOIN FETCH pom.user ";
 
     public static final String GET_LOCATIONS =
         "FROM Location AS location ";
 
     public static final String GET_MACHINE_BY_MACHINENAME =
         "FROM Machine AS machine "
             + " WHERE machine.machineName =:machineName ";
 
     public static final String GET_MACHINES =
         "FROM Machine AS machine ";
 
     public static final String GET_TOURNAMENT_BY_ID =
         "FROM Tournament AS tournament "
             + "LEFT JOIN FETCH tournament.gameMap AS gameMap "
             + "LEFT JOIN FETCH tournament.brokerMap AS brokerMap "
             + "LEFT JOIN FETCH brokerMap.user "
             + "WHERE tournament.tournamentId =:tournamentId";
 
     public static final String GET_TOURNAMENT_BY_NAME =
         "FROM Tournament AS tournament "
             + "LEFT JOIN FETCH tournament.gameMap AS gameMap "
             + "LEFT JOIN FETCH gameMap.agentMap AS agentMap "
             + "LEFT JOIN FETCH agentMap.broker "
             + "LEFT JOIN FETCH agentMap.game "
             + "LEFT JOIN FETCH tournament.brokerMap AS brokerMap "
             + "LEFT JOIN FETCH brokerMap.user "
             + "WHERE tournament.tournamentName =:tournamentName";
 
     public static final String GET_TOURNAMENTS_NOT_COMPLETE =
         "FROM Tournament AS tournament "
             + "LEFT JOIN FETCH tournament.gameMap as gameMap "
             + "LEFT JOIN FETCH gameMap.agentMap AS agentMap "
             + "LEFT JOIN FETCH gameMap.machine "
             + "LEFT JOIN FETCH agentMap.broker "
             + "LEFT JOIN FETCH agentMap.game "
             + "LEFT JOIN FETCH tournament.brokerMap AS brokerMap "
             + "LEFT JOIN FETCH brokerMap.user "
             + "WHERE NOT tournament.status='" + Tournament.STATE.complete + "'";
 
     public static final String GET_GAME_BY_ID =
         "FROM Game AS game "
             + "LEFT JOIN FETCH game.tournament "
             + "LEFT JOIN FETCH game.machine "
             + "LEFT JOIN FETCH game.agentMap as agentMap "
             + "LEFT JOIN FETCH agentMap.broker as broker "
             + "LEFT JOIN FETCH broker.user "
             + "WHERE game.gameId =:gameId";
 
     public static final String GET_GAMES_SINGLE_BOOT_PENDING =
         "FROM Game AS game "
             + "LEFT JOIN FETCH game.tournament AS tournament "
             + "LEFT JOIN FETCH game.machine "
             + "LEFT JOIN FETCH game.agentMap "
             + "WHERE game.status='"+ Game.STATE.boot_pending.toString() +"' "
             + "AND tournament.type='" + Tournament.TYPE.SINGLE_GAME + "'";
 
     public static final String GET_GAMES_MULTI_BOOT_PENDING =
         "FROM Game AS game "
             + "LEFT JOIN FETCH game.tournament AS tournament "
             + "LEFT JOIN FETCH game.machine "
             + "LEFT JOIN FETCH game.agentMap "
             + "WHERE game.status='"+ Game.STATE.boot_pending.toString() +"' "
             + "AND tournament.tournamentId =:tournamentId";
 
     public static final String GET_GAMES_SINGLE_BOOT_COMPLETE =
         "FROM Game AS game "
             + "LEFT JOIN FETCH game.tournament AS tournament "
             + "LEFT JOIN FETCH game.machine "
             + "LEFT JOIN FETCH game.agentMap AS agentMap "
             + "LEFT JOIN FETCH agentMap.broker AS broker "
             + "LEFT JOIN FETCH broker.user "
             + "LEFT JOIN FETCH broker.agentMap "
 
             + "WHERE game.status='"+ Game.STATE.boot_complete.toString() +"' "
             + "AND game.startTime < :startTime "
             + "AND tournament.type='" + Tournament.TYPE.SINGLE_GAME + "'";
 
     public static final String GET_GAMES_MULTI_BOOT_COMPLETE =
         "FROM Game AS game "
             + "LEFT JOIN FETCH game.tournament AS tournament "
             + "LEFT JOIN FETCH game.machine "
             + "LEFT JOIN FETCH game.agentMap AS agentMap "
             + "LEFT JOIN FETCH agentMap.broker AS broker "
             + "LEFT JOIN FETCH broker.user "
             + "LEFT JOIN FETCH broker.agentMap "
 
             + "WHERE game.status='"+ Game.STATE.boot_complete.toString() + "' "
             + "AND game.startTime < :startTime "
             + "AND tournament.tournamentId =:tournamentId";
 
     public static final String GET_GAMES_NOT_COMPLETE =
         "FROM Game AS game "
             + "LEFT JOIN FETCH game.tournament "
             + "LEFT JOIN FETCH game.machine "
             + "LEFT JOIN FETCH game.agentMap agentMap "
             + "LEFT JOIN FETCH agentMap.broker as broker "
             + "LEFT JOIN FETCH broker.user "
             + "WHERE NOT game.status='"
             + Game.STATE.game_complete.toString() + "' ";
 
     public static final String GET_GAMES_COMPLETE =
         "FROM Game AS game "
             + "LEFT JOIN FETCH game.tournament "
             + "LEFT JOIN FETCH game.machine "
             + "LEFT JOIN FETCH game.agentMap agentMap "
             + "LEFT JOIN FETCH agentMap.broker as broker "
             + "LEFT JOIN FETCH broker.user "
             + "WHERE game.status='"+ Game.STATE.game_complete.toString() + "' ";
 
     public static final String GET_BROKERS =
         "FROM Broker AS broker "
             + "LEFT JOIN FETCH broker.tournamentMap "
             + "LEFT JOIN FETCH broker.user "
             + "LEFT JOIN FETCH broker.agentMap as agentMap ";
 
     public static final String GET_BROKER_BY_ID =
         "FROM Broker AS broker "
             + "LEFT JOIN FETCH broker.user "
             + "LEFT JOIN FETCH broker.tournamentMap "
             + "LEFT JOIN FETCH broker.agentMap as agentMap "
             + "LEFT JOIN FETCH agentMap.game "
             + "LEFT JOIN FETCH agentMap.broker as broker2 "
             + "LEFT JOIN FETCH broker2.user "
             + "WHERE broker.brokerId =:brokerId ";
 
     public static final String GET_BROKER_BY_NAME =
         "FROM Broker AS broker "
             + "LEFT JOIN FETCH broker.user "
             + "WHERE brokerName =:brokerName ";
 
     public static final String GET_BROKER_BY_BROKERAUTH =
         "FROM Broker AS broker "
             + "LEFT JOIN FETCH broker.user "
             + "LEFT JOIN FETCH broker.tournamentMap "
             + "LEFT JOIN FETCH broker.agentMap as agentMap "
             + "LEFT JOIN FETCH agentMap.game as game "
             + "LEFT JOIN FETCH game.tournament "
             + "LEFT JOIN FETCH game.machine "
             + "LEFT JOIN FETCH agentMap.broker as broker2 "
             + "LEFT JOIN FETCH broker2.user "
             + "WHERE broker.brokerAuth =:brokerAuth ";
   }
 }
