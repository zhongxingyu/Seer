 package rky.portfolio.gambles;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import rky.util.SetMap;
 
 public class GambleGenerator
 {
     private static final double AVERAGE_EXPECTED = 2.0;
     private static final double HALF_PROBABILITY_OF_LINK = 0.4;
     private static final int NUM_GAMBLES = (int) (Math.random() * 20) + 190;
     
     public static List<Gamble> generateGambles()
     {
         ArrayList<Double> gambleExpectedReturns = new ArrayList<Double>(NUM_GAMBLES);
 
         double sum = 0;
         for (int i = 0; i < NUM_GAMBLES; i++)
         {
             double randomValue = Math.random();
             gambleExpectedReturns.add(randomValue);
             sum += randomValue;
         }
 
         for (int i = 0; i < NUM_GAMBLES; i++)
         {
            gambleExpectedReturns.set(i, gambleExpectedReturns.get(i) * AVERAGE_EXPECTED / sum);
         }
 
         ArrayList<Gamble> gambles = new ArrayList<Gamble>(NUM_GAMBLES);
 
         for (int i = 0; i < NUM_GAMBLES; i++)
         {
             gambles.add(Gambles.randomGamble(gambleExpectedReturns.get(i)));
         }
 
         return gambles;
     }
     
     public static SetMap<Integer, Integer> generateLinks(List<Gamble> gambles)
     {
         Map<Gamble, Integer> gambleIds = new HashMap<Gamble, Integer>();
 
         for (int i = 0; i < NUM_GAMBLES; i++)
         {
             gambleIds.put(gambles.get(i), i);
         }
 
         SetMap<Integer, Integer> links = new SetMap<Integer, Integer>();
 
         for (Gamble g1 : gambles)
         {
             for (Gamble g2 : gambles)
             {
                 if (g1 == g2) continue;
 
                 if (Math.random() < HALF_PROBABILITY_OF_LINK)
                 {
                     links.put(gambleIds.get(g1), gambleIds.get(g2));
                     links.put(gambleIds.get(g2), gambleIds.get(g1));
                 }
             }
         }
         
         return links;
     }
 }
