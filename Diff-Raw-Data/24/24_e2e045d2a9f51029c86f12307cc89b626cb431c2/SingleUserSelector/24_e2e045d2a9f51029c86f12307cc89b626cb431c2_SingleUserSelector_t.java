 /**
  * Pleiades
  * Copyright (C) 2011 - 2012
  * Computational Intelligence Research Group (CIRG@UP)
  * Department of Computer Science
  * University of Pretoria
  * South Africa
  */
 package net.pleiades.simulations.selection;
 
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
         String[] keySet = simulationsDB.loadAllKeys().toArray(new String[0]);
         
         for (String key : keySet) {
             String u = key.substring(0, key.indexOf("_"));
             if (u.equalsIgnoreCase(user)) {
                return key;
             }
         }
        
        return "";
     }
 }
