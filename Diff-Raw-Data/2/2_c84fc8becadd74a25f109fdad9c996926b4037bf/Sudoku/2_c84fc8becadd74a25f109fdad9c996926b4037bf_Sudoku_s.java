 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.bainedog.sudoku;
 
 /**
  *
  * @author baine
  */
 public abstract class Sudoku {
 
     protected Sudoku(Integer[][] cells) {
         this.cells = new Integer[cells.length][cells[0].length];
         for (int i = 0; i < cells.length; i++) {
             for (int j = 0; j < cells.length; j++) {
                 this.setCell(i, j, cells[i][j]);
             }
         }
     }
 
     protected Sudoku(int[][] cells) {
         this.cells = new Integer[cells.length][cells[0].length];
         for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells.length; j++) {
                 this.setCell(i, j, (cells[i][j] >= 0 ? cells[i][j] : null));
             }
         }
     }
 
     public final int getOrder() {
         return (int) Math.round(Math.sqrt(cells.length));
     }
 
     public final int getLength() {
         return getOrder() * getOrder();
     }
 
     public final Integer get(int i, int j) {
         return cells[i][j];
     }
 
     protected void setCell(Integer i, Integer j, Integer n) {
         this.cells[i][j] = n;
     }
 
     private final Integer[][] cells;
 }
