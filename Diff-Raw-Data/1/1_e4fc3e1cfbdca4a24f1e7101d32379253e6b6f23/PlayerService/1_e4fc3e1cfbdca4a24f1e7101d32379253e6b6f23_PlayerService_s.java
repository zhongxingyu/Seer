 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cdiddy.utils.application;
 
 
 import cdiddy.objects.ByeWeek;
 import cdiddy.objects.Name;
 import cdiddy.objects.Player;
 import cdiddy.objects.PlayerPic;
 import cdiddy.objects.Position;
 import cdiddy.objects.SeasonStat;
 import cdiddy.objects.Stat;
 import cdiddy.objects.WeeklyStat;
 import cdiddy.objects.dao.*;
 import cdiddy.utils.system.JacksonPojoMapper;
 import cdiddy.utils.system.OAuthConnection;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.scribe.model.Verb;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 /**
  *
  * @author DMDD
  */
 @Repository("playerUtil")
 public class PlayerService 
 {
 
     @Autowired
     OAuthConnection conn;
     
     @Autowired
     private StatsService statsService;
     @Autowired
     private StatDAO statDAOImpl;
     @Autowired
     private SeasonStatsDAO seasonStatsDAOImpl;
     @Autowired
     private WeeklyStatsDAO weeklyStatsDAOImpl;
     @Autowired
     private PlayersDAO playersDAOImpl;
     @Autowired
     private PositionDAO positionDAOImpl;
     @Autowired
     private ByeWeekDAO byeWeekDAOImpl;
     @Autowired
     private PlayerPicDAO playerPicDAOImpl;
     @Autowired
     private NameDAO nameDAOImpl;
     @Autowired
     private YQLQueryUtil yqlUitl ;
     
     //Yahoo grabs
     
     public void loadPlayers()
     {
         ObjectMapper mapper = new ObjectMapper();
         Map<String,Object> userData;
         Map<String,Object> results;
         Map<String,Object> query;
         ArrayList league;
         List<Player> playerObjList;
         List<Player> playerSaveList = new LinkedList<Player>();
         Map<String,Position> posMap = new HashMap<String,Position>();
         boolean morePlayers = true;
         int start = 0; 
         while (morePlayers)  
         {   
             String yql = "select * from fantasysports.players where game_key='nfl' and start="+start;
             String response = yqlUitl.queryYQL(yql);
             
             try 
             {
                 userData = mapper.readValue(response, Map.class);
                 
                 query = (Map<String, Object>)userData.get("query"); // query details
                 results = (Map<String, Object>)query.get("results"); //result details
                 int count = (Integer)query.get("count");
                 List<Map> playersList = (List<Map>) results.get("player");
                 playerObjList = new LinkedList<Player>();
                 for(Map temp : playersList)
                 {
                     Player tempPlayer = mapper.readValue(JacksonPojoMapper.toJson(temp, false) , Player.class);
                     playerObjList.add(tempPlayer);
                     Object elegiblePosObj = ((Map<String,Map<String,Object>>)temp).get("eligible_positions").get("position");
                      List<Position> tempPosList = new LinkedList<Position>();
                     if (elegiblePosObj instanceof String)
                     {
                         if(!posMap.containsKey((String) elegiblePosObj))
                         {
                             Position tempPos = new Position();
                             tempPos.setPosition((String) elegiblePosObj);
                             tempPosList.add(tempPos);
                             posMap.put((String)elegiblePosObj , tempPos);
                         }
                         else
                         {
                             tempPosList.add(posMap.get((String)elegiblePosObj));
                         }
                        
                        
                     }
                     if(elegiblePosObj instanceof List)
                     {
                         for(Object o : (List)elegiblePosObj)
                         {
                             if(!posMap.containsKey((String) o))
                             {
                                 Position tempPos = new Position();
                                 tempPos.setPosition((String) o);
                                 tempPosList.add(tempPos);
                                 posMap.put((String)o , tempPos);
                             }
                             else
                             {
                                 tempPosList.add(posMap.get((String)o));
                             }
                         }
                     
                     }
                     tempPlayer.setEligible_positions(tempPosList);
                 }
                 Map<Integer, List<SeasonStat>> seasonStatmap = statsService.retrieveSeasonStats(playerObjList);
                 playerObjList = connectStatsToPlayer(seasonStatmap, playerObjList);       
                 Map<Integer, List<WeeklyStat>> statmap = statsService.retrieveWeeklyStats(playerObjList, 1);
                 playerObjList = connectWeeklyStatsToPlayer(statmap, playerObjList);
                 playerSaveList.addAll(playerObjList);
                 start+=count;
                 Logger.getLogger(PlayerService.class.getName()).log(Level.INFO, "Start Count : "+ start);
                 if(count < 25)
                 {
                     morePlayers = false;
                 }
  
                     
                     storePlayersToDatabase(playerSaveList);
                     playerSaveList = new LinkedList<Player>(); 
                     Thread.sleep(30000);
 
             } catch (Exception ex) 
             {
                 Logger.getLogger(PlayerService.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
     
     public String getPlayerStats(Player p)
     {
         
         String player_key = p.getPlayer_id();
         String response = conn.requestData( "http://fantasysports.yahooapis.com/fantasy/v2/player/"+player_key+"/stats?format=json", Verb.GET);
     
         return response;
     }
     
         public String getStatsCategories()
     {
         
        // String player_key = "nfl.p." + p.getYahooId();
         String response = conn.requestData( "http://fantasysports.yahooapis.com/fantasy/v2/game/nfl/stat_categories?format=json", Verb.GET);
     
         return response;
     }
       
     // Helper Methods
         
       private List<Player> connectStatsToPlayer(Map<Integer, List<SeasonStat>> statmap, List<Player> playerObjList) 
     {
         List<Player> result = new LinkedList<Player>();
         for(Player p : playerObjList)
         {
                    
             p.setSeasonStats(statmap.get(Integer.parseInt(p.getPlayer_id())));
             result.add(p);
         
         }
         return result;
     }
 
     private List<Player> connectWeeklyStatsToPlayer(Map<Integer, List<WeeklyStat>> statmap, List<Player> playerObjList) 
     {
         
         List<Player> result = new LinkedList<Player>();
         for(Player p : playerObjList)
         {
             if(p.getWeeklyStats() == null)
             {
                 p.setWeeklyStats(new LinkedList<WeeklyStat>());
             }
             List <WeeklyStat> tempListWeeklyStat = p.getWeeklyStats();
             Map <String, WeeklyStat> weekStatMap = new HashMap<String, WeeklyStat>();
             for(WeeklyStat week : tempListWeeklyStat)
             {
                 weekStatMap.put(week.getWeek(), week);
             }
             List<WeeklyStat> playerWeekStat = statmap.get(Integer.parseInt(p.getPlayer_id()));
             if(playerWeekStat == null)
             {
                 playerWeekStat = new LinkedList<WeeklyStat>(); // just incase the player got no stats for this week. o_0
             }
             for(WeeklyStat week : playerWeekStat)
             {
                if(weekStatMap.containsKey(week.getWeek()))
                {
                    weekStatMap.remove(week.getWeek());
                    weekStatMap.put(week.getWeek(), week);
                }
                else
                {
                     weekStatMap.put(week.getWeek(), week);
                }
             }
             p.setWeeklyStats(new LinkedList<WeeklyStat>(weekStatMap.values()));
             result.add(p);
         
         }
         return result;
     }
     
      public void yahooWeeklyStatsLoad(int week)
     {
         List<Player> allPlayers = playersDAOImpl.getAllPlayers();
         List<Player> playersToGetStats = new LinkedList<Player>();
         List<Player> playerstoSave = new LinkedList<Player>();
         int i = 0;
         for(Player p : allPlayers)
         {
             playersToGetStats.add(p);
             i++;
             if(i%25 == 0)
             {
                 Logger.getLogger(PlayerService.class.getName()).log(Level.INFO, "Players: " + i + " out of " + allPlayers.size());
                 Map<Integer, List<WeeklyStat>> weeklyStats = statsService.retrieveWeeklyStats(playersToGetStats,week);
                 playerstoSave.addAll(connectWeeklyStatsToPlayer(weeklyStats, playersToGetStats));
                 playersToGetStats = new LinkedList<Player>();
                 storePlayersToDatabase(playerstoSave);
                 playerstoSave = new LinkedList<Player>();
                 try 
                 {
                     Thread.sleep(15000);
                 } 
                 catch (InterruptedException ex) {
                     Logger.getLogger(PlayerService.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 
             }
                   
         }
         Logger.getLogger(PlayerService.class.getName()).log(Level.INFO, "Players: " + i + " out of " + allPlayers.size());
         Map<Integer, List<WeeklyStat>> weeklyStats = statsService.retrieveWeeklyStats(playersToGetStats,week);
         playerstoSave.addAll(connectWeeklyStatsToPlayer(weeklyStats, playersToGetStats));
         ///storePlayersToDatabase(playersToGetStats);
          //playersToGetStats = new LinkedList<Player>();
         storePlayersToDatabase(playerstoSave);
     }
 
     
     //Database Updates
     
     
     public void storePlayersToDatabase(List<Player> playerList)
     {
         LinkedList<SeasonStat> listSS = new LinkedList<SeasonStat>();
         LinkedList<WeeklyStat> listWS = new LinkedList<WeeklyStat>();
         LinkedList<Stat> listStat = new LinkedList<Stat>();
         List<Name> listNames = new LinkedList<Name>();
         List<ByeWeek> listByeWeek = new LinkedList<ByeWeek>();
         List<PlayerPic> listPlayerPic = new LinkedList<PlayerPic>();
         List<Position> listAvailPositions = new LinkedList<Position>();
         Map<String,Position> distinctPosions = new HashMap<String, Position>();
         List<Position> exsistingPositions = positionDAOImpl.getPositions();
         if(exsistingPositions == null)
         {
             exsistingPositions = new LinkedList();
         }
         for(Position pos : exsistingPositions)
         {
             distinctPosions.put(pos.getPosition(), pos);       
         }
         for(Player p : playerList)
         {
             List<SeasonStat> tempSSList = p.getSeasonStats();
             List<WeeklyStat> tempWSList = p.getWeeklyStats();
             listSS.addAll(tempSSList);
             for(SeasonStat ss : tempSSList)
             {
                listStat.addAll(ss.getStats());
             }
             listWS.addAll(tempWSList);
             for(WeeklyStat ws : tempWSList)
             {
                listStat.addAll(ws.getStats());
             }
 
             List<Position> savePosList = new LinkedList<Position>();
             //positionDAOImpl.getPositions();
             List<Position> attemptedSavePosList =  p.getEligible_positions();
             for(Position pos : attemptedSavePosList)
             {
                 Position savedPos = pos;
                if(distinctPosions.containsKey(pos.getPosition()))
                {   
                    savedPos = distinctPosions.get(pos.getPosition());
                }
                else
                {
                    distinctPosions.put(pos.getPosition(), pos);
                }
                 savePosList.add(savedPos);
                
             }
             p.setEligible_positions(savePosList);
             listNames.add(p.getName());
             listByeWeek.add(p.getBye_weeks());
             //listAvailPositions.addAll(p.getEligible_positions());
             listPlayerPic.add(p.getHeadshot());
         }
         statDAOImpl.saveStats(listStat);
         seasonStatsDAOImpl.saveSeasonStats(listSS);
         weeklyStatsDAOImpl.saveWeeklyStats(listWS);
         positionDAOImpl.savePositions(new LinkedList<Position>(distinctPosions.values()));
         nameDAOImpl.saveNames(listNames);
         playerPicDAOImpl.savePlayerPics(listPlayerPic);
         byeWeekDAOImpl.saveByeWeeks(listByeWeek);
         playersDAOImpl.savePlayers(playerList);
     }
         
     public List<Player> retrivePlayers() 
     {
          return playersDAOImpl.getAllPlayers();
     }
      public Player retrivePlayer(int playerid) 
     {
          return playersDAOImpl.getPlayerbyYahooId(playerid);
     }
     public List<Player> retrivePlayers(int firstResult, int maxResults) 
     {
         return playersDAOImpl.getPlayers(firstResult, maxResults);
     }
     
     public void primePlayersDatabase() 
     {
          playersDAOImpl.clearPlayers();
     }
     
 
 }
