 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.building;
 
 import com.mahn42.framework.Building;
 import com.mahn42.framework.BuildingBlock;
 import com.mahn42.framework.DBSetWorld;
 import java.io.File;
 import java.util.logging.Logger;
 import org.bukkit.World;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.dynmap.markers.MarkerAPI;
 import org.dynmap.markers.MarkerIcon;
 
 /**
  *
  * @author andre
  */
 public class LandmarkDB extends DBSetWorld<Landmark> {
 
     public LandmarkDB() {
         super(Landmark.class);
     }
 
     public LandmarkDB(World aWorld, File aFile) {
         super(Landmark.class, aFile, aWorld);
     }
 
     public void landmarkBuildingInserted(Building aBuilding) {
         BuildingBlock lBlock = aBuilding.getBlock("sign");
         BuildingBlock lPBlock = aBuilding.getBlock("bottom");
         if (lBlock != null && lPBlock != null) {
             BlockState lState = lBlock.position.getBlock(aBuilding.world).getState();
             if (lState instanceof Sign) {
                 String[] lLines = ((Sign)lState).getLines();
                 String lName = lLines[0] + " " + lLines[1];
                 Landmark lMark = getLandmark(lName);
                 if (lMark == null) {
                     lMark = new Landmark();
                     lMark.name = lName;
                     String lMode = lLines[2];
                     MarkerAPI lMarkerAPI = BuildingPlugin.plugin.getDynmapTask().getMarkerAPI();
                     MarkerIcon lIcon = lMarkerAPI.getMarkerIcon(lMode);
                     if (lIcon != null) {
                         lMark.kind = Landmark.Kind.Icon;
                         lMark.iconName = lMode;
                     } else if (lMode.equalsIgnoreCase("area")) {
                         lMark.kind = Landmark.Kind.Area;
                     } else if (lMode.equalsIgnoreCase("line") || lMode.equalsIgnoreCase("path")) {
                         lMark.kind = Landmark.Kind.Line;
                     }
                     String lParam = lLines[3];
                     try {
                         lMark.color = Integer.parseInt(lParam);
                     } catch (Exception ex) {
                         //
                     }
                     addRecord(lMark);
                 }
                 lMark.positions.add(lPBlock.position.clone());
                 aBuilding.name = lMark.name + " #" + lMark.positions.size();
             }
         }
     }
     
     public void landmarkBuildingRemoved(Building aBuilding) {
        String[] lParts = aBuilding.name.split(" #");
         Landmark lMark = getLandmark(lParts[0]);
         if (lMark != null) {
             lMark.positions.remove(aBuilding.edge1);
             lMark.positions.remove(aBuilding.edge2);
             if (lMark.positions.isEmpty()) {
                 remove(lMark);
             }
         }
     }
     
     public Landmark getLandmark(String aName) {
         for(Landmark lMark : this) {
             if (lMark.name.equals(aName)) {
                 return lMark;
             }
         }
         return null;
     }
 }
