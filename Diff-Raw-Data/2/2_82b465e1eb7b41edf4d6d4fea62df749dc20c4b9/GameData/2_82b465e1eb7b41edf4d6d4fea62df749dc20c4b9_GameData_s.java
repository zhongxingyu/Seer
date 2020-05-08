 package rky.portfolio.io;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import rky.portfolio.gambles.Gamble;
 import rky.portfolio.gambles.Luck;
 import rky.util.SetMap;
 
 public class GameData
 {
     // Gamble id to gamble data
     public final Map<Integer, Gamble>   gambles          = new HashMap<Integer, Gamble>();
     // Gamble data to gamble id
     public final Map<Gamble, Integer>   ids              = new HashMap<Gamble, Integer>();
     // Gamble to class
     public final Map<Gamble, Integer>   gambleClasses    = new HashMap<Gamble, Integer>();
     // Gamble links
     public final SetMap<Gamble, Gamble> links            = new SetMap<Gamble, Gamble>();
     // Class favorability as defined per round in the file provided by shasha
     public final ClassFavorabilityMap   classFavorability = new ClassFavorabilityMap();
 
     public static class ClassFavorabilityMap
     {
         private final Map<Integer, Luck> map = new HashMap<Integer, Luck>();
 
         public void put(Integer round, Integer classId, Luck luck)
         {
             map.put(makeKey(round, classId), luck);
         }
 
         public Luck get(Integer round, Integer classId)
         {
             Luck l = map.get(makeKey(round, classId));
             // Based on the data, this is has max iterations of ~20
             while (l == null && round >= 0)
             {
                 round -= 1;
                 l = map.get(makeKey(round, classId));
             }
 
             if (l == null)
             {
                 l = Luck.neutral;
             }
 
             return l;
         }
         
         private Integer makeKey(Integer round, Integer classId)
         {
            return (round.toString() + classId.toString()).hashCode();
         }
     }
     
 }
