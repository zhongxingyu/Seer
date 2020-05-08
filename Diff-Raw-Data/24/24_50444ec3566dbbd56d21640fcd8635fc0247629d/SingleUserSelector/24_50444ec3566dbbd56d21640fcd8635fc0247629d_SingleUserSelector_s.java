 /**
  * Pleiades
  * Copyright (C) 2011 - 2012
  * Computational Intelligence Research Group (CIRG@UP)
  * Department of Computer Science
  * University of Pretoria
  * South Africa
  */
 package net.pleiades.simulations.selection;
 
import com.google.common.collect.LinkedHashMultimap;
import java.util.Random;
 import net.pleiades.database.SimulationsMapStore;
 
 /**
  *
  * @author bennie
  */
 public class SingleUserSelector implements SimulationSelector {
     private String user;
 
     public SingleUserSelector(String user) {
         this.user = user;
     }
 
     @Override
     public String getKey(SimulationsMapStore simulationsDB) {
        Random random = new Random();

         String[] keySet = simulationsDB.loadAllKeys().toArray(new String[0]);
         
        LinkedHashMultimap<String, String> userMap = LinkedHashMultimap.create();
        
         for (String key : keySet) {
             String u = key.substring(0, key.indexOf("_"));
             if (u.equalsIgnoreCase(user)) {
                userMap.put(u, key);
             }
         }
        
        String[] users = userMap.keySet().toArray(new String[0]);
        
        double rand = random.nextDouble();

        int selected = (int)Math.ceil(rand * users.length) - 1;
        
        if (selected < 0) {
            return "";
        }
        
        return userMap.get(users[selected]).toArray(new String[0])[0];
     }
 }
