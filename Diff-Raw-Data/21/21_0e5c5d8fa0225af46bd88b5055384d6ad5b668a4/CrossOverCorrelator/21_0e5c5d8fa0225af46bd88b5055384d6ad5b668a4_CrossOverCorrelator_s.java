 package com.bluesmoke.farm.correlator;
 
 import com.bluesmoke.farm.correlator.builder.CorrelatorBuilderManager;
 import com.bluesmoke.farm.service.feed.FeedService;
 import com.bluesmoke.farm.util.ListElement;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.TreeMap;
 
 public class CrossOverCorrelator extends GenericCorrelator{
 
     private ListElement<TreeMap<String, Object>> currentAggressiveListElement;
     private ListElement<TreeMap<String, Object>> currentPassiveListElement;
 
     public CrossOverCorrelator(String id, CorrelatorBuilderManager correlatorBuilderManager, CorrelatorPool pool, FeedService feed, GenericCorrelator aggressiveParent, GenericCorrelator passiveParent, HashMap<String, Object> config)
     {
         super("CrossOver_" + id, correlatorBuilderManager, pool, feed, aggressiveParent, passiveParent, config);
     }
 
     @Override
     public void createMutant() {
 
         HashMap<String, Object> config = new HashMap<String, Object>(this.config);
         Random rand = new Random();
         int timeSpan = rand.nextInt(4);
         timeSpan = (int) Math.pow(10, timeSpan);
         while(timeSpan == (Integer) this.config.get("timeSpan"))
         {
             timeSpan = rand.nextInt(4);
             timeSpan = (int) Math.pow(10, timeSpan);
         }
         config.put("timeSpan" , timeSpan);
 
         new CrossOverCorrelator(pool.getNextID(), correlatorBuilderManager, pool, feed, aggressiveParent, passiveParent, config);
     }
 
     @Override
     public String createState() {
 
         String state = null;
         try{
             int timeSpan = (Integer) config.get("timeSpan");
            if(!config.containsKey("aggressiveUnderlying") || !aggressiveParent.currentUnderlyingComponents.containsKey(config.get("aggressiveUnderlying")))
             {
                 killLineage();
                 return null;
             }
            double currentA = (Double) aggressiveParent.currentUnderlyingComponents.get(config.get("aggressiveUnderlying"));
             Object underlyingComponents = null;
             if(currentAggressiveListElement == null)
             {
                 currentAggressiveListElement = aggressiveParent.stackUnderlyingComponents.getHead();
 
                 for(int i = 0; i < timeSpan + 1; i++)
                 {
                     if (currentAggressiveListElement.getPrevious() != null)
                     {
                         currentAggressiveListElement = currentAggressiveListElement.getPrevious();
                         underlyingComponents = currentAggressiveListElement.getData();
                     }
                     else {
                         currentAggressiveListElement = null;
                         return null;
                     }
                 }
             }
             else {
                 currentAggressiveListElement = currentAggressiveListElement.getPrevious();
                 underlyingComponents = currentAggressiveListElement.getData();
             }
             if(currentAggressiveListElement != null && currentAggressiveListElement == currentAggressiveListElement.getTail())
             {
                 currentAggressiveListElement = null;
             }
             if(underlyingComponents == null
                     || !((TreeMap<String, Object>) underlyingComponents).containsKey(config.get("aggressiveUnderlying"))
                     || !((TreeMap<String, Object>) underlyingComponents).containsKey(config.get("passiveUnderlying")))
             {
                 return null;
             }
             double pastA = (Double) ((TreeMap<String, Object>) underlyingComponents).get(config.get("aggressiveUnderlying"));
            if(!config.containsKey("passiveUnderlying") || !passiveParent.currentUnderlyingComponents.containsKey(config.get("passiveUnderlying")))
             {
                 killLineage();
                 return null;
             }
            double currentP = (Double) passiveParent.currentUnderlyingComponents.get(config.get("passiveUnderlying"));
             underlyingComponents = null;
             if(currentPassiveListElement == null)
             {
                 currentPassiveListElement = passiveParent.stackUnderlyingComponents.getHead();
 
                 for(int i = 0; i < timeSpan + 1; i++)
                 {
                     if (currentPassiveListElement.getPrevious() != null)
                     {
                         currentPassiveListElement = currentPassiveListElement.getPrevious();
                         underlyingComponents = currentPassiveListElement.getData();
                     }
                     else {
                         currentPassiveListElement = null;
                         return null;
                     }
                 }
             }
             else {
                 currentPassiveListElement = currentPassiveListElement.getPrevious();
                 underlyingComponents = currentPassiveListElement.getData();
             }
             if(currentPassiveListElement != null && currentPassiveListElement == currentPassiveListElement.getTail())
             {
                 currentPassiveListElement = null;
             }
 
             if(underlyingComponents == null|| !((TreeMap<String, Object>) underlyingComponents).containsKey(config.get("passiveUnderlying")))
             {
                 return null;
             }
             double pastP = (Double) ((TreeMap<String, Object>) underlyingComponents).get(config.get("passiveUnderlying"));
 
             double cross = ((currentP - currentA)/(pastA - currentA + currentP - pastP)) * timeSpan;
 
             currentUnderlyingComponents.put("crossover", cross);
 
             if(cross == Double.NEGATIVE_INFINITY || cross < -5)
             {
                 cross = -5;
             }
             if(cross == Double.POSITIVE_INFINITY || cross > 5)
             {
                 cross = 5;
             }
             if(cross == 0)
             {
                 return "" + 0;
             }
 
             int direction = 0;
             if(currentA > currentP)
             {
                 direction = 1;
             }
 
             currentUnderlyingComponents.put("direction", direction);
 
             state = "" + (int)(Math.signum(cross) * Math.log10(Math.abs(cross + 1))) + "," + direction;
         }
         catch (Exception e)
         {
             e.printStackTrace();
             //feed.pause();
             killLineage();
             return null;
         }
 
         return state;
     }
 }
