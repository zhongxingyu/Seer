 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mahn42.anhalter42.building;
 
 import com.mahn42.framework.BlockPosition;
 import com.mahn42.framework.Building;
 import java.util.ArrayList;
 
 /**
  *
  * @author andre
  */
 public class SendReceiveBuilding extends Building {
     public enum Mode {
         send,
         receive
     }
     
     public Mode mode = Mode.send;
     public String frequencyName = "";
     public BlockPosition antenaEdge1 = new BlockPosition();
     public BlockPosition antenaEdge2 = new BlockPosition();
     
     @Override
     protected void toCSVInternal(ArrayList aCols) {
         super.toCSVInternal(aCols);
         aCols.add(mode);
         aCols.add(antenaEdge1.toCSV(","));
         aCols.add(antenaEdge2.toCSV(","));
        aCols.add(frequencyName);
     }
 
     @Override
     protected void fromCSVInternal(DBRecordCSVArray aCols) {
         super.fromCSVInternal(aCols);
         mode = Enum.valueOf(Mode.class, aCols.pop());
         antenaEdge1.fromCSV(aCols.pop(), "\\,");
         antenaEdge2.fromCSV(aCols.pop(), "\\,");
        frequencyName = aCols.pop();
     }
 }
