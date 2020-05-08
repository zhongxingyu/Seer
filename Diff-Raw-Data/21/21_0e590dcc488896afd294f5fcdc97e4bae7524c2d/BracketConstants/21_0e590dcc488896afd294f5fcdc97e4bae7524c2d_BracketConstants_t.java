 package com.porvak.bracket.domain;
 
 import com.google.common.collect.Maps;
 
 import java.util.Map;
 
 public final class BracketConstants {
     
     public static final String POOL_ID = "4f3c8297a0eea26b78d77538";
     public static final String TOURNAMENT_ID = "4f41ce03d17060d0d8dbd4d6";
    public static final String REDIRECT_TO_HOME = "redirect:/";
     
     private static Map<Integer, Integer> gameRoundMap = Maps.newHashMap();
     private static Map<Integer, Integer> gameRoundFinalsMap = Maps.newHashMap();
 
     static {
         // ROUND 1
         gameRoundMap.put(1, 1);
         gameRoundMap.put(2, 1);
         gameRoundMap.put(3, 1);
         gameRoundMap.put(4, 1);
         gameRoundMap.put(5, 1);
         gameRoundMap.put(6, 1);
         gameRoundMap.put(7, 1);
         gameRoundMap.put(8, 1);
 
         // ROUND 2
         gameRoundMap.put(9, 2);
         gameRoundMap.put(10, 2);
         gameRoundMap.put(11, 2);
         gameRoundMap.put(12, 2);
 
         // ROUND 3
         gameRoundMap.put(13, 3);
         gameRoundMap.put(14, 3);
 
         // ROUND 4
         gameRoundMap.put(15, 4);
 
         // FINALS - ROUND 1
         gameRoundFinalsMap.put(1, 1);
         gameRoundFinalsMap.put(2, 1);
 
         // FINALS - ROUND 2
         gameRoundFinalsMap.put(3, 2);
 
         // FINALS - ROUND 3
         gameRoundFinalsMap.put(4, 3);
     }
     
     public static int getRoundIdForGame(int gameId, int regionId){
         if(regionId == 5){
             return gameRoundFinalsMap.get(gameId);
         }
 
         return gameRoundMap.get(gameId);
     }
 
 }
