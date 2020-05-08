 package com.github.detro.rps;
 
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 /**
  *
  * TODO Map error cases to different Exceptions: having all "RuntimeException" is very limiting in terms of error handling
  */
 public class GameCenter {
     private static final Logger LOG = Logger.getLogger(GameCenter.class);
 
     private final Map<String, Match> matches = new HashMap<String, Match>();
 
     public GameCenter() {
         // nothing to do for now
     }
 
    public synchronized void addMatch(Match newMatch) {
         if (!containsMatch(newMatch.getId())) {
             matches.put(newMatch.getId(), newMatch);
         } else {
             String errMsg = String.format("Match '%s' already in GameCenter", newMatch.getId());
             LOG.error(errMsg);
             throw new RuntimeException(errMsg);
         }
     }
 
     public List<Match> getMatches() {
         return new ArrayList<Match>(matches.values());
     }
 
     public List<Match> getMatches(String playerId) {
         ArrayList<Match> result = new ArrayList<Match>();
 
         for (Match match : matches.values()) {
             if (match.containsPlayer(playerId)) {
                 result.add(match);
             }
         }
 
         return result;
     }
 
     public boolean containsMatch(String matchId) {
         return matches.containsKey(matchId);
     }
 
     public Match getMatch(String matchId) {
         if (!containsMatch(matchId)) {
             String errMsg = String.format("Match '%s' not found", matchId);
             LOG.error(errMsg);
             throw new RuntimeException(errMsg);
         }
 
         return matches.get(matchId);
     }
 }
