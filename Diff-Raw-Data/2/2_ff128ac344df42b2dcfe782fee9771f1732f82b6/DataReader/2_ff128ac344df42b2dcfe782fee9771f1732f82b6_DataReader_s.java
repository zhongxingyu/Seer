 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.github.etsai.kfsxtrackingserver;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * Interfaces with the statistical data, providing the user with read access to the underlying storage scheme
  * @author etsai
  */
 public interface DataReader {
     /**
      * List order for a set of data
      */
     public enum Order {
         /** Sort in ascending order */
         ASC,
         /** Sort in descending order */
         DESC,
         /** Do not sort */
         NONE
     }
     /**
      * Get the difficulty information for each difficulty setting.  Map keys are:
      * <table>
      * <thead>
      *   <tr><th>Key</th><th>Type</th><th>Description</th></tr>
      * </thead>
      * <tbody>
      *   <tr><td>difficulty</td><td>String</td><td>Name of the difficulty</td></tr>
      *   <tr><td>length</td><td>String</td><td>Game length</td></tr>
      *   <tr><td>wins</td><td>Integer</td><td>Number of wins</td></tr>
      *   <tr><td>losses</td><td>Integer</td><td>Number of losses</td></tr>
      *   <tr><td>waveaccum</td><td>Integer</td><td>Accumulated sum of waves each game ended on </td></tr>
      *   <tr><td>time</td><td>Integer</td><td>Accumulated play time for each match played on the difficulty and length in seconds</td></tr>
      * </tbody>
      * </table>
      * @return  List of a map of attributes for each difficulty
      */
     public List<Map<Object, Object>> getDifficulties();
     /**
      * Get the level breakdown for the given difficulty setting.  Map keys are:
      * <table>
      * <thead>
      *   <tr><th>Key</th><th>Type</th><th>Description</th></tr>
      * </thead>
      * <tbody>
      *   <tr><td>level</td><td>String</td><td>Name of the level</td></tr>
      *   <tr><td>wins</td><td>Integer</td><td>Number of wins</td></tr>
      *   <tr><td>losses</td><td>Integer</td><td>Number of losses</td></tr>
      *   <tr><td>waveaccum</td><td>Integer</td><td>Accumulated sum of waves each game ended on </td></tr>
      *   <tr><td>time</td><td>Integer</td><td>Accumulated play time for each match played on the difficulty and length in seconds</td></tr>
      * </tbody>
      * </table>
      * @param   difficulty    Difficulty name
      * @param   length      Game length
      * @return  List of statistics for each map played on the given difficulty setting
      */
    public List<Map<Object, Object>> getDifficultyData(String diffculty, String length);
     /**
      * Get the totals for each played level, across all difficulty settings.  Map keys are:
      * <table>
      * <thead>
      *   <tr><th>Key</th><th>Type</th><th>Description</th></tr>
      * </thead>
      * <tbody>
      *   <tr><td>level</td><td>String</td><td>Name of the level</td></tr>
      *   <tr><td>wins</td><td>Integer</td><td>Number of wins</td></tr>
      *   <tr><td>losses</td><td>Integer</td><td>Number of losses</td></tr>
      *   <tr><td>time</td><td>Integer</td><td>Accumulated play time for each match played on the level in seconds</td></tr>
      * </tbody>
      * </table>
      * @return  List of totals for all levels
      */
     public List<Map<Object, Object>> getLevels();
     /**
      * Get the difficulty breakdown for specific level.  Map keys are:
      * <table>
      * <thead>
      *   <tr><th>Key</th><th>Type</th><th>Description</th></tr>
      * </thead>
      * <tbody>
      *   <tr><td>difficulty</td><td>String</td><td>Name of the difficulty</td></tr>
      *   <tr><td>length</td><td>String</td><td>Game length</td></tr>
      *   <tr><td>wins</td><td>Integer</td><td>Number of wins</td></tr>
      *   <tr><td>losses</td><td>Integer</td><td>Number of losses</td></tr>
      *   <tr><td>waveaccum</td><td>Integer</td><td>Accumulated sum of waves each game ended on </td></tr>
      *   <tr><td>time</td><td>Integer</td><td>Accumulated play time for each match played on the difficulty and length in seconds</td></tr>
      * </tbody>
      * </table>
      * @param   level      Name of the level to lookup
      * @return  List of difficulty breakdowns for all levels played
      */
     public List<Map<Object, Object>> getLevelData(String level);
     /**
      * Get the number of player records in the database
      * @return  Number of players in the database
      */
     public Integer getNumRecords();
     /**
      * Get the record for the given player.  If the steamID64 is invalid, null is returned.  See documentation for getRecords() for map keys.
      * @param   steamID64   SteamID64 of the desired player.
      * @return  Map of attributes for the given player, null if invalid steamID64 is given
      * @see DataReader#getRecords()
      */
     public Map<Object, Object> getRecord(String steamID64);
     /**
      * Get a subset of all the player records, sorted by a specific category in a given order.  See documentation for getRecords() for map keys.
      * @param   group   The group to sort on.  If order is NONE, this parameter is ignored
      * @param   order   Order to sort the group if desired
      * @param   start   The first row to return in the given ordering
      * @param   end     The last row to return in the given ordering
      * @return  Ordered list of player records, limitted by a start and end.
      * @see DataReader#getRecords()
      */
     public List<Map<Object, Object>> getRecords(String group, Order order, int start, int end);
     /**
      * Get all stored player records.  *
      * Get the record for the given player.  If the steamID64 is invalid, null is returned.  Map keys are:
      * <table>
      * <thead>
      *   <tr><th>Key</th><th>Type</th><th>Description</th></tr>
      * </thead>
      * <tbody>
      *   <tr><td>steamid64</td><td>String</td><td>Unique int64 id of the player</td></tr>
      *   <tr><td>name</td><td>String</td><td>Steam community name for the steamID64</td></tr>
      *   <tr><td>avatar</td><td>String</td><td>Steam community profile picture</td></tr>
      *   <tr><td>wins</td><td>Integer</td><td>Number of matches wins</td></tr>
      *   <tr><td>losses</td><td>Integer</td><td>Number of matches losses</td></tr>
      *   <tr><td>disconnects</td><td>Integer</td><td>Number of times the player prematurely left the match</td></tr>
      *   <tr><td>finales_played</td><td>Integer</td><td>Number of boss waves the player participated in</td></tr>
      *   <tr><td>finales_survived</td><td>Integer</td><td>Number of boss waves the player survived</td></tr>
      *   <tr><td>time_connected</td><td>Integer</td><td>Total time spent on the server in seconds</td></tr>
      * </tbody>
      * </table>
      * @return  All player records
      */
     public List<Map<Object, Object>> getRecords();
     /**
      * Get a subset of the match history for the specific player.  The list can be ordered based on a grouping.  See getMatchHistory(String) for the map keys.  
      * @param   steamID64   SteamID64 of the player to lookup
      * @param   group   The group to sort on.  If order is NONE, this parameter is ignored
      * @param   order   Order to sort the group if desired
      * @param   start   The first row to return in the given ordering
      * @param   end     The last row to return in the given ordering
      * @return  Ordered list of the match history for a player.
      * @see DataReader#getMatchHistory(String)
      */
     public List<Map<Object, Object>> getMatchHistory(String steamID64, String group, Order order, int start, int end);
     /**
      * Get all games in a player's match history.  The keys for the map are:result, wave, duration, timestamp, difficulty, length, and level.
      * <table>
      * <thead>
      *   <tr><th>Key</th><th>Type</th><th>Description</th></tr>
      * </thead>
      * <tbody>
      *   <tr><td>result</td><td>String</td><td>Result of the match</td></tr>
      *   <tr><td>wave</td><td>Integer</td><td>Wave reached upon disconnecting or match ending</td></tr>
      *   <tr><td>duration</td><td>Integer</td><td>How long the player was in the match, in seconds</td></tr>
      *   <tr><td>timestamp</td><td>String</td><td>Date and time of when the player left or match ended.  Timezone will be where the tracking server is hosted</td></tr>
      *   <tr><td>difficulty</td><td>String</td><td>Difficulty setting of the match</td></tr>
      *   <tr><td>length</td><td>String</td><td>Game length of the match</td></tr>
      *   <tr><td>level</td><td>String</td><td>Level the match took place on</td></tr>
      * </tbody>
      * </table>
      * @param   steamID64   SteamID64 of the player to lookup
      * @return  All games played by the player
      */
     public List<Map<Object, Object>> getMatchHistory(String steamID64);
     /**
      * Get all stat categories that have an aggregate sum over all players
      * @return  Stat categories for aggregate stats
      */
     public List<String> getAggregateCategories();
     /**
      * Get the aggregate statistics for a specific category.  Map keys are: 
      * <table>
      * <thead>
      *   <tr><th>Key</th><th>Type</th><th>Description</th></tr>
      * </thead>
      * <tbody>
      *   <tr><td>stat</td><td>String</td><td>Name of the statistic</td></tr>
      *   <tr><td>value</td><td>Integer</td><td>Store value corresponding to the statistic</td></tr>
      * </tbody>
      * </table>
      * @param   category    Aggregate category to lookup
      * @return  List of all statistics for a category
      */
     public List<Map<Object, Object>> getAggregateData(String category);
     /**
      * Get the aggregate statistics for a specific category and player.  See getAggregateData(String) for map keys.
      * @param   category    Aggregate category to lookup
      * @param   steamID64   SteamID64 of the player to lookup
      * @return  List of all statistics for a category and player
      * @see DataReader#getAggregateData(String)
      */
     public List<Map<Object, Object>> getAggregateData(String category, String steamID64);
     /**
      * Get the steam community info for a player.  Map keys are: 
      * <table>
      * <thead>
      *   <tr><th>Key</th><th>Type</th><th>Description</th></tr>
      * </thead>
      * <tbody>
      *   <tr><td>name</td><td>String</td><td>Steam community name of the steamID64</td></tr>
      *   <tr><td>avatar</td><td>String</td><td>Picture of the steam community profile</td></tr>
      * </tbody>
      * </table>
      * @param   steamID64   SteamID64 of the player to lookup
      * @return  Steam community info for a player
      */
     public Map<Object, Object> getSteamIDInfo(String steamID64);
     /**
      * Get categories for statistics that support wave by wave analytics
      * @return  List of support statistics that have wave by wave numbers
      */
     public List<String> getWaveDataCategories();
     /**
      * Get detailed wave by wave numbers for a given difficulty setting.  Map keys are: 
      * <table>
      * <thead>
      *   <tr><th>Key</th><th>Type</th><th>Description</th></tr>
      * </thead>
      * <tbody>
      *   <tr><td>wave</td><td>Integer</td><td>Wave the statistic corresponds to</td></tr>
      *   <tr><td>stat</td><td>String</td><td>Name of the statistic</td></tr>
      *   <tr><td>value</td><td>Integer</td><td>Store value corresponding to the statistic</td></tr>
      * </tbody>
      * </table>
      * @param   difficulty      Difficulty name
      * @param   length          Game length
      * @param   category        Category of statistics to retrieve
      * @return  List of wave by wave statistics
      */
     public List<Map<Object, Object>> getWaveData(String difficulty, String length, String category);
     /**
      * Get detailed wave by wave numbers for a given difficulty setting and map.  See getWaveData(String, String, String) for map keys.
      * @param   level           Name of the level
      * @param   difficulty      Difficulty name
      * @param   length          Game length
      * @param   category        Category of statistics to retrieve
      * @return  List of wave by wave statistics
      * @see DataReader#getWaveData(String, String, String)
      */
     public List<Map<Object, Object>> getWaveData(String level, String difficulty, String length, String category);
 }
