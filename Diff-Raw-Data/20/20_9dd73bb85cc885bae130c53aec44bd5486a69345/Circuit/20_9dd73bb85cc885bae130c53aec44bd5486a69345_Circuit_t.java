 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.circuit;
 
 import com.mahn42.framework.BuildingDetector;
 import com.mahn42.framework.Framework;
 import com.mahn42.framework.WorldDBList;
 import java.util.HashMap;
 import java.util.List;
 import org.bukkit.World;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author andre
  */
 public class Circuit extends JavaPlugin {
 
     public Framework framework;
 
     public WorldDBList<CircuitBuildingDB> DBs;
     
     public CircuitTask circuitTask;
     public HashMap<String, CircuitHandler> circuitHandlers = new HashMap<String, CircuitHandler>();
 
     public static void main(String[] args) {
     }
 
     @Override
     public void onEnable() {
         framework = Framework.plugin;
         DBs = new WorldDBList<CircuitBuildingDB>(CircuitBuildingDB.class, this);
         
         framework.registerSaver(DBs);
         
         circuitTask = new CircuitTask(this);
         getServer().getScheduler().scheduleAsyncRepeatingTask(this, circuitTask, 20, 1);
         
         CircuitBuildingHandler lHandler = new CircuitBuildingHandler(this);
 
         registerCircuitHandler(new CircuitHandler42M00());
         registerCircuitHandler(new CircuitHandler42M01());
         registerCircuitHandler(new CircuitHandler42M02());
         CircuitDescription lDesc;
         
         BuildingDetector lDetector = framework.getBuildingDetector();
         
         lDesc = new CircuitDescription();
         lDesc.name = "Circuit.DIP4"; // IC with 4 Pins
         lDesc.typeName = "DIP IC 4 Pins";
         lDesc.circuitTypeName = "DIP4";
         lDesc.type = CircuitDescription.Type.DIP;
         lDesc.pinCount = 4;
         lDesc.handler = lHandler;
         lDetector.addDescription(lDesc);
         lDesc.createAndActivateXZ();
        /*
         lDesc = new CircuitDescription();
         lDesc.name = "Circuit.DIP6"; // IC with 6 Pins
         lDesc.typeName = "DIP IC 6 Pins";
         lDesc.circuitTypeName = "DIP6";
         lDesc.type = CircuitDescription.Type.DIP;
         lDesc.pinCount = 6;
         lDesc.handler = lHandler;
         lDetector.addDescription(lDesc);
         lDesc.createAndActivateXZ();
 
         lDesc = new CircuitDescription();
         lDesc.name = "Circuit.DIP8"; // IC with 8 Pins
         lDesc.typeName = "DIP IC 8 Pins";
         lDesc.circuitTypeName = "DIP8";
         lDesc.type = CircuitDescription.Type.DIP;
         lDesc.pinCount = 8;
         lDesc.handler = lHandler;
         lDetector.addDescription(lDesc);
         lDesc.createAndActivateXZ();
        */
         List<World> lWorlds = getServer().getWorlds();
         for(World lWorld : lWorlds) {
             DBs.getDB(lWorld);
         }
     }
     
     public void registerCircuitHandler(CircuitHandler aHandler) {
         circuitHandlers.put(aHandler.name, aHandler);
     }
     
     @Override
     public void onDisable() {
         getServer().getScheduler().cancelTasks(this);
     }
 }
