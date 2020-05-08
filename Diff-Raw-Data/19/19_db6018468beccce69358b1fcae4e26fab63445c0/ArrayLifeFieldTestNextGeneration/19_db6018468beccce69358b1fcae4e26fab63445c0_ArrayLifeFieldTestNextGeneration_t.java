 package com.itransition.life.test;
 
 import com.itransition.life.core.ArrayLifeField;
 import org.junit.Test;
 import java.util.*;
 
 import org.junit.*;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 @RunWith(value = Parameterized.class)
 public class ArrayLifeFieldTestNextGeneration {
     private int width;
     private int height;
     private int[][] initialCells;
     private int epochCount;
     private int[][] finalCells;
 
     public ArrayLifeFieldTestNextGeneration(int width, int height, int[][] initialCells, int epochCount, int[][] finalCells) {
         this.width = width;
         this.height = height;
         this.initialCells = initialCells;
         this.epochCount = epochCount;
         this.finalCells = finalCells;
     }
 
     @Parameters
     public static Collection<Object[]> data() {
         Object[][] data = new Object[][] {
             { 5, 5, new int[][] {{1,1},{1,2},{2,1},{2,2}}, 100, new int[][] {{1,1},{1,2},{2,1},{2,2}} },
             { 5, 5, new int[][] {{0,3},{0,4},{4,3},{4,4}}, 100, new int[][] {{0,3},{0,4},{4,3},{4,4}} },
            { 6, 6, new int[][] {{1,1},{1,2},{2,1},{2,2},{0,3},{0,4},{5,3},{5,4}}, 100, new int[][] {{1,1},{1,2},{2,1},{2,2},{0,3},{0,4},{5,3},{5,4}} },
            { 6, 6, new int[][] {{1,1},{1,2},{2,1},{2,2},{0,3},{0,4},{5,3},{5,4}}, 101, new int[][] {{1,1},{2,1},{2,2},{0,4},{5,3},{5,4}} },
             { 8, 8, new int[][] {{3,7},{3,0},{3,1},{7,4},{0,4},{1,4}}, 50, new int[][] {{3,7},{3,0},{3,1},{7,4},{0,4},{1,4}} },
             { 8, 8, new int[][] {{3,7},{3,0},{3,1},{7,4},{0,4},{1,4}}, 51, new int[][] {{0,3},{0,4},{0,5},{2,0},{3,0},{4,0}} },
             { 5, 6, new int[][] {{1,0},{2,1},{2,2},{1,2},{0,2}}, 4, new int[][] {{2,1},{3,2},{3,3},{2,3},{1,3}} },
            { 120, 120, new int[][] {{1,1},{2,1},{1,2},{2,2},{4,4},{4,3},{3,4},{3,3}}, 100, new int[][] {{1,1},{2,1},{1,2},{2,2},{4,4},{4,3},{3,4},{3,3}} },
            { 120, 120, new int[][] {{1,1},{2,1},{1,2},{2,2},{4,4},{4,3},{3,4},{3,3}}, 101, new int[][] {{1,1},{2,1},{1,2},{4,4},{4,3},{3,4}} },
         };
         return Arrays.asList(data);
     }
 
     @Test
     public void testNextGeneration() throws Exception {
         ArrayLifeField field = new ArrayLifeField(width,height);
         for(int i=0; i<initialCells.length; i++) {
             int x = initialCells[i][0];
             int y = initialCells[i][1];
             field.setAlive(x,y);
         }
         for(int i=0; i<epochCount; i++) {
             field.nextGeneration();
         }
        Assert.assertEquals(finalCells.length,field.getNumberOfAliveCells());
         for(int i=0; i<finalCells.length; i++) {
             int x = finalCells[i][0];
             int y = finalCells[i][1];
             Assert.assertTrue(field.isAlive(x,y));
         }
     }
 }
