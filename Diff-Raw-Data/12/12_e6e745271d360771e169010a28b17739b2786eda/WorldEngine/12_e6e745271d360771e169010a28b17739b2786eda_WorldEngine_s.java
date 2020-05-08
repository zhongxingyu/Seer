 package com.irr310.server;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.irr310.common.engine.FramerateEngine;
 import com.irr310.common.event.game.DefaultGameEventVisitor;
 import com.irr310.common.event.game.GameEvent;
 import com.irr310.common.tools.Log;
 import com.irr310.common.tools.Vec2;
 import com.irr310.common.world.Faction;
 import com.irr310.common.world.Map;
 import com.irr310.common.world.World;
 import com.irr310.common.world.item.BuildingItemFactory;
 import com.irr310.common.world.item.NexusItem;
 import com.irr310.common.world.system.System;
 
 public class WorldEngine extends FramerateEngine<GameEvent> {
 
     private World world;
 
     public WorldEngine(Object object) {
     }
 
     @Override
     protected void processEvent(GameEvent e) {
         e.accept(new WorldEngineEventVisitor());
     }
 
     @Override
     protected void frame() {
     }
 
     @Override
     protected void onInit() {
         initWorld();
     }
 
     @Override
     protected void onStart() {
         pause(false);
     }
     
     @Override
     protected void onEnd() {
         
     }
 
     private final class WorldEngineEventVisitor extends DefaultGameEventVisitor {
 
//        @Override
//        public void visit(QuitGameEvent event) {
//            java.lang.System.out.println("stopping world engine");
//            setRunning(false);
//        }
 //        
 //        @Override
 //        public void visit(StartEngineEvent event) {
 //            pause(false);
 //        }
 //
 //        @Override
 //        public void visit(PauseEngineEvent event) {
 //            pause(true);
 //        }
        
     }
     
     private void initWorld() {
         
         world = new World();
         Random random = new Random();
         
         Map map = getWorld().getMap();
         
         //Init map
         int factionCount = 5;
         int systemCount = 100;
         double mapSize = 1000;
         double mapMinDistance = mapSize/50;
         
         // Init zone positions
         
         int validSystem = 0;
         while(validSystem < systemCount) {
             
             //double distance = (1 - Math.sqrt(random.nextDouble())) * mapSize;
             double distance = (0.5 * (1 - Math.sqrt(random.nextDouble())) + 0.5 * random.nextDouble()) * mapSize;
             double azimut = random.nextDouble() * 2 * Math.PI;
             
             Vec2 location = new Vec2(0, distance).rotate(azimut);
             
 
             if(map.getZones().size() > 0) {
                 
                 System nearestSystem = map.nearestSystemTo(location);
                 
                 if(nearestSystem.getLocation().distanceTo(location) < mapMinDistance) {
                     // Too near to a existing system, retry
                     Log.trace("Too near to a existing system, retry :"+ nearestSystem.getLocation().distanceTo(location));
                     mapMinDistance--;
                     continue;
                 } else {
                     Log.trace("Distance before :"+ nearestSystem.getLocation().distanceTo(location));
                     location = location.add(location.diff(nearestSystem.getLocation()).normalize().multiply(mapMinDistance/2) );
                     Log.trace("Distance after :"+ nearestSystem.getLocation().distanceTo(location));
                 }
             }
             
             System system = new System(GameServer.pickNewId(), location);
             map.addZone(system);
             mapMinDistance++;
             
             validSystem++;
         }
 
         // Find home system
         double baseAzimut = random.nextDouble() * 2 * Math.PI;
         
         List<System> availableHome = new ArrayList<System>();
         
         for(int i = 0; i < factionCount; i++) {
             Vec2 location = new Vec2(0, mapSize/2).rotate(baseAzimut + i * 2 * Math.PI / factionCount);
             availableHome.add(map.nearestSystemTo(location));
         }
         
         
         // Init faction
         for(int i = 0; i < factionCount; i++) {
             // Pick home system
             int homeIndex = random.nextInt(factionCount - i);
             System system = availableHome.get(homeIndex);
             availableHome.remove(homeIndex);
             
             Faction faction = new Faction(GameServer.pickNewId());
             faction.setHomeSystem(system);
             
             getWorld().addFaction(faction);
             
             NexusItem nexus = BuildingItemFactory.createNexus(faction);
             
             
             getWorld().addItem(nexus);
             
             nexus.forceDeploy(system, system.getRandomEmptySpace(nexus.getDeployedRadius()));
         }
         
 
         
         
         map.dump();
     }
     
     public World getWorld() {
         return world;
     }
 
 }
