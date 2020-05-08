 package com.bluesmoke.farm.correlator;
 
 import com.bluesmoke.farm.correlator.builder.CorrelatorBuilderManager;
 import com.bluesmoke.farm.service.feed.FeedService;
 import com.bluesmoke.farm.util.ListElement;
 
 import java.util.*;
 
 public class DifferentialCorrelator extends GenericCorrelator{
 
     private boolean resolutionSet = false;
     private ListElement<TreeMap<String, Object>> currentAggressiveListElement;
     private ListElement<TreeMap<String, Object>> currentPassiveListElement;
 
     public DifferentialCorrelator(String id, CorrelatorBuilderManager correlatorBuilderManager, CorrelatorPool pool, FeedService feed, GenericCorrelator aggressiveParent, GenericCorrelator passiveParent, HashMap<String, Object> config)
     {
         super("Differential_" + id, correlatorBuilderManager, pool, feed, aggressiveParent, passiveParent, config);
         numTicksObserved = 0;
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
 
         new DifferentialCorrelator(pool.getNextID(), correlatorBuilderManager, pool, feed, aggressiveParent, passiveParent, config);
     }
 
     @Override
     public String createState() {
 
         String state = null;
 
         try{
             String type = (String) config.get("type");
             int timeSpan = (Integer) config.get("timeSpan");
            if(!config.containsKey("aggressiveUnderlying"))
             {
                 killLineage();
                 return null;
             }
            Object o = aggressiveParent.getUnderlyingComponent(config.get("aggressiveUnderlying"));
            if(o == null)
            {
                return null;
            }
            double current = (Double) o;
 
             if(type.equals("Single"))
             {
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
                 if(underlyingComponents == null || !((TreeMap<String, Object>) underlyingComponents).containsKey(config.get("aggressiveUnderlying")))
                 {
                     return null;
                 }
                 double past = (Double) ((TreeMap<String, Object>) underlyingComponents).get(config.get("aggressiveUnderlying"));
 
                 double differential = current - past;
                 currentUnderlyingComponents.put("differential", differential);
 
                 if(!resolutionSet)
                 {
                     int exp = (int) Math.log10(resolution);
                     resolution = Math.pow(10, exp)/3;
                     resolutionSet = true;
                 }
 
                 state = "" + (int)(differential/resolution);
             }
             else {
                 Object underlyingComponents = null;
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
                 if(underlyingComponents == null || !((TreeMap<String, Object>) underlyingComponents).containsKey(config.get("passiveUnderlying")))
                 {
                     return null;
                 }
                 double past = (Double) ((TreeMap<String, Object>) underlyingComponents).get(config.get("passiveUnderlying"));
 
                 double differential = current - past;
                 currentUnderlyingComponents.put("differential", differential);
 
                 if(!resolutionSet)
                 {
                     int exp = (int) Math.log10(resolution);
                     resolution = Math.pow(10, exp)/3;
                     resolutionSet = true;
                 }
 
                 state = "" + (int)(differential/resolution);
             }
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
