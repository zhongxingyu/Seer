 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.quest;
 
 import com.mahn42.anhalter42.quest.action.Action;
 import com.mahn42.anhalter42.quest.action.ActionList;
 import com.mahn42.framework.BlockAreaList;
 import com.mahn42.framework.BlockPosition;
 import com.mahn42.framework.Framework;
 import com.mahn42.framework.RestrictedRegion;
 import com.mahn42.framework.SyncBlockList;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 /**
  *
  * @author andre
  */
 public class Quest extends QuestObject {
     
     /* Runtime */
     public Random random = new Random();
     public World world = null;
     public ArrayList<String> players = new ArrayList<String>();
     public Scene currentScene = null;
     public SyncBlockList syncList = null;
     public BlockPosition edge1 = new BlockPosition();
     public BlockPosition edge2 = new BlockPosition();
     public BlockAreaList frames = new BlockAreaList();
     public boolean stopped = false;
     public HashMap<String, Object> objects = new HashMap<String, Object>();
     public RestrictedRegion restrictedRegion = new RestrictedRegion();
     public ArrayList<QuestTaskInteraction> interactions;
     
     /* Meta */
     public QuestObjectArray<Scene> scenes = new QuestObjectArray<Scene>(this, Scene.class);
     public HashMap<String, BlockPosition> markers = new HashMap<String, BlockPosition>();
     public QuestObjectHashMap<QuestInventory> inventories = new QuestObjectHashMap<QuestInventory>(this, QuestInventory.class);
     public QuestObjectHashMap<QuestVariable> variables = new QuestObjectHashMap<QuestVariable>(this, QuestVariable.class);
     public QuestObjectArray<PlayerPosition> playerPositions = new QuestObjectArray<PlayerPosition>(this, PlayerPosition.class);
     public int minPlayerCount = 1;
     public int maxPlayerCount = 1;
     public String name;
     public String startScene;
     public BlockPosition startPos = new BlockPosition(0,0,0); // relative from player
     public int width = 1;
     public int height = 1;
     public int depth = 1;
     public int socialPoints = 1;
     public boolean restrictRegion = true;
     public ActionList stopActions = new ActionList(this);
     public ActionList startActions = new ActionList(this);
     
     /* Static */
     public static HashMap<String, Class> actionTypes = new HashMap<String, Class>();
     public static HashMap<String, Class> triggerTypes = new HashMap<String, Class>();
     public static HashMap<String, Class> generatorTypes = new HashMap<String, Class>();
     
     public Quest() {
         quest = this;
     }
     
     public void load(File aFile) {
         YamlConfiguration lConf = new YamlConfiguration(); 
         try {
             lConf.load(aFile);
             fromSectionValue(lConf.get("quest"));
             frames.load(new File(aFile.toString().replaceAll(".yml", ".frm")));
         } catch (Exception ex) {
             Logger.getLogger(Quest.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     public void setAllowedMaterialsFromSectionValue(Object aValue) {
         if (aValue instanceof ArrayList) {
             for(Object lItem : ((ArrayList)aValue)) {
                 Material lMat = Material.getMaterial(lItem.toString().toUpperCase());
                 if (lMat == null) {
                     lMat = Material.getMaterial(Integer.parseInt(lItem.toString()));
                 }
                 quest.log("allowed material:" + lMat.name());
                 restrictedRegion.allowedMaterials.add(lMat);
             }
         }
     }
 
     /*
     public void setScenesFromSectionValue(Object aValue) {
         if (aValue instanceof ArrayList) {
             for(Object lItem : ((ArrayList)aValue)) {
                 Scene lScene = new Scene();
                 lScene.quest = this;
                 lScene.fromSectionValue(lItem);
                 scenes.add(lScene);
             }
         }
     }*/
 
     public void setMarkersFromSectionValue(Object aValue) {
         if (aValue instanceof ArrayList) {
             for(Object lItem : ((ArrayList)aValue)) {
                 HashMap<String, String> lMap = (HashMap)lItem;
                 BlockPosition lPos = new BlockPosition();
                 lPos.fromCSV(lMap.get("pos"), ",");
                 markers.put(lMap.get("name"), lPos);
             }
         }
     }
 /*
     public void setVariablesFromSectionValue(Object aValue) {
         if (aValue instanceof ArrayList) {
             for(Object lItem : ((ArrayList)aValue)) {
                 QuestVariable lVar = new QuestVariable();
                 lVar.quest = this;
                 lVar.fromSectionValue(lItem);
                 variables.put(lVar.name, lVar);
             }
         }
     }
 
     public void setInventoriesFromSectionValue(Object aValue) {
         if (aValue instanceof ArrayList) {
             for(Object lItem : ((ArrayList)aValue)) {
                 QuestInventory lInv = new QuestInventory();
                 lInv.quest = this;
                 lInv.fromSectionValue(lItem);
                 inventories.put(lInv.name, lInv);
             }
         }
     }
 */
     public Scene getScene(String aName) {
         for(Scene lScene : scenes) {
             if (lScene.name.equals(aName)) {
                 return lScene;
             }
         }
         return null;
     }
     
     public QuestVariable getVariable(String aName) {
         QuestVariable lResult = variables.get(aName);
         if (lResult == null) {
             lResult = new QuestVariable();
             lResult.quest = this;
             lResult.name = aName;
             variables.put(lResult.name, lResult);
         }
         return lResult;
     }
     
     public QuestInventory getInventory(String aName) {
         QuestInventory lInv = inventories.get(aName);
         if (lInv == null) {
             lInv = new QuestInventory();
             lInv.quest = this;
             inventories.put(aName, lInv);
         }
         return lInv;
     }
     
     public void initialze() {
         if (edge2.x == 0 && edge2.y == 0 && edge2.z == 0) {
             edge2.cloneFrom(edge1);
             edge2.add(width - 1, height - 1, depth - 1);
         }
         restrictedRegion.lowerEdge = edge1.clone();
         restrictedRegion.upperEdge = edge2.clone();
         if (restrictRegion) {
             Framework.plugin.getRestrictedRegions(world, true).add(restrictedRegion);
         }
         for(Scene lScene : scenes) {
             lScene.initilize();
         }
         for(Object lObject : objects.entrySet()) {
             if (lObject instanceof QuestObject) {
                 ((QuestObject)lObject).quest = this;
             }
         }
         if (startScene != null) {
             currentScene = getScene(startScene);
         }
         for(Action lAction : stopActions) {
             lAction.initialize();
         }
         for(Action lAction : startActions) {
             lAction.initialize();
         }
         for(Action lAction : startActions) {
             quest.log("action " + lAction.type + " executed.");
             lAction.execute();
         }
     }
     
     public void run() {
         if (currentScene != null) {
             for(Object lObject : objects.values()) {
                 if (lObject instanceof QuestObject) {
                     ((QuestObject)lObject).quest = this;
                 }
                 if (lObject instanceof IQuestTick) {
                     ((IQuestTick)lObject).tick();
                 }
             }
             currentScene.run();
         } else {
             stop();
         }
     }
 
     public void stop() {
         if (!stopped) {
             syncList = new SyncBlockList(world);
             for(Action lAction : stopActions) {
                 quest.log("action " + lAction.type + " executed.");
                 lAction.execute();
             }
             syncList.execute();
             if (restrictRegion) {
                 Framework.plugin.getRestrictedRegions(world, true).remove(restrictedRegion);
             }
             for(String lPlayerName:players) {
                Framework.plugin.getPlayerManager().increaseSocialPoint("", "Quest", socialPoints, name, lPlayerName);
                 Player lPlayer = Framework.plugin.getServer().getPlayer(lPlayerName);
                 int y = world.getHighestBlockYAt(lPlayer.getLocation());
                 Location lLoc = lPlayer.getLocation().clone();
                 lLoc.setY(y);
                 lPlayer.teleport(lLoc, PlayerTeleportEvent.TeleportCause.PLUGIN);
             }
             stopped = true;
             QuestPlugin.plugin.stopQuest(this);
             log("Quest " + name + " stopped. (SP:" + socialPoints + ")");
         }
     }
 
     public void finish() {
         stop();
     }
     
     public void log(String aText) {
         if (Framework.plugin.isDebugSet("quest")) {
             QuestPlugin.plugin.getLogger().info("Quest '" + name + "' '" + (currentScene == null ? "null" : currentScene.name) + "':" + aText);
         }
     }
     
     public Player getPlayer(String aName) {
         return QuestPlugin.plugin.getServer().getPlayer(aName);
     }
 
     public Player getPlayer(int aNumber) {
         if (aNumber < players.size()) {
             return getPlayer(players.get(aNumber));
         } else {
             return null;
         }
     }
     
     public void sendPlayerMessage(String aText) {
         for(String lPlayerName: players) {
             Player lPlayer = getPlayer(lPlayerName);
             if (lPlayer != null) {
                 lPlayer.sendMessage(aText);
             }
         }
     }
 }
