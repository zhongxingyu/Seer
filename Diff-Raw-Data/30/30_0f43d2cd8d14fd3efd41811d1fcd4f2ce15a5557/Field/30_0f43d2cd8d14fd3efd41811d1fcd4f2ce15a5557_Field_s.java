 package com.foton.robot_controller;
 
 import android.util.Log;
 import android.util.Pair;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Created by foton on 29.10.13.
  */
 public class Field {
     private double cellSize;
     public int chunkSize;
     private HashMap<Pair<Integer, Integer>, FieldChunk> fieldChunks = new HashMap<Pair<Integer, Integer>, FieldChunk>();
 
     public Field() {
         cellSize = 0;
         chunkSize = 0;
     }
 
     public Field(double cellSize_, int chunkSize_) {
         cellSize = cellSize_;
         chunkSize = chunkSize_;
     }
 
     private FieldChunk getFieldChunkByChunkCoords(Pair<Integer, Integer> chunkCoords) {
         FieldChunk fieldChunk = fieldChunks.get(chunkCoords);
         if (fieldChunk == null) {
             fieldChunks.put(chunkCoords, new FieldChunk(chunkSize, chunkCoords.first, chunkCoords.second));
             fieldChunk = fieldChunks.get(chunkCoords);
         }
         return fieldChunk;
     }
 
     private Pair<Integer, Integer> getFieldChunkCoordsByCellCoords(Pair<Integer, Integer> cellCoords) {
        return new Pair<Integer, Integer>(cellCoords.first >= 0 ? (int)(cellCoords.first / cellSize) : -(int)((-cellCoords.first - 1) / cellSize) - 1,
                                          cellCoords.second >= 0 ? (int)(cellCoords.second / cellSize) : -(int)((-cellCoords.second - 1) / cellSize) - 1);
     }
     
     private Pair<Integer, Integer> getCellCoordsByCoords(Pair<Double, Double> coords) {
        return new Pair<Integer, Integer>(coords.first >= 0 ? (int)(coords.first / cellSize) : (int)(coords.first / cellSize) - 1,
                                          coords.first >= 0 ? (int)(coords.second / cellSize) : (int)(coords.second / cellSize) - 1);
     }
 
     private FieldChunk getFieldChunkByCellCoords(Pair<Integer, Integer> cellCoords) {
         return getFieldChunkByChunkCoords(getFieldChunkCoordsByCellCoords(cellCoords));
     }
 
     public Cell getCellByCoords(Pair<Double, Double> coords) {
         Pair<Integer, Integer> cellCoords = getCellCoordsByCoords(coords);
         return getFieldChunkByCellCoords(cellCoords).getLocalCell(cellCoords);
     }
 
     public ArrayList<FieldChunk> getFieldChunks() {
         ArrayList<FieldChunk> res = new ArrayList<FieldChunk>();
         for (FieldChunk fieldChunk: fieldChunks.values())
             res.add(fieldChunk);
         return res;
     }
 }
