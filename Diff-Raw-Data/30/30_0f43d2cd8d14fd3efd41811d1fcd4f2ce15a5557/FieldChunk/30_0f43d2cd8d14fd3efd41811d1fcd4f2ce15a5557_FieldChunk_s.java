 package com.foton.robot_controller;
 
 import android.util.Pair;
 
 /**
  * Created by foton on 29.10.13.
  */
 public class FieldChunk {
     public int size;
     public int chunkX;
     public int chunkY;
     public int leftCellX;
     public int bottomCellY;
     Cell[][] cells;
 
     FieldChunk(int size_, int chunkX_, int chunkY_) {
         size = size_;
         chunkX = chunkX_;
         chunkY = chunkY_;
         leftCellX = chunkX * size;
         bottomCellY = chunkY * size;
         cells = new Cell[size][size];
         for (int i = 0; i < size; ++i)
             for (int j = 0; j < size; ++j)
                 cells[i][j] = new Cell();
     }
 
     public Pair<Integer, Integer> getLocalCellCoords(Pair<Integer, Integer> globalCell) {
         return new Pair<Integer, Integer>(globalCell.first - leftCellX, globalCell.second - bottomCellY);
     }
 
     public Cell getLocalCell(Pair<Integer, Integer> globalCell) {
         Pair<Integer, Integer> cellCoords = getLocalCellCoords(globalCell);
         return cells[cellCoords.first][cellCoords.second];
     }
 }
