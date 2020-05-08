 package com.tipumc;
 
 import java.util.Vector;
 
 public final class Map {
 
 
     public Map(Vector<String> map)
     {
         startMap = map;
         height = map.size();
         width = 0;
         for (int i = 0; i < height; i++) {
             if (map.get(i).length() > width)
             {
                 width = map.get(i).length();
             }
         }
        
         mapMatrix = new char[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                 char c = startMap.get(i).charAt(j);
                 if (c == '#' | c == '.') {
                     mapMatrix[i][j] = startMap.get(i).charAt(j);
                 }
                 else if (c == '+' | c == '*') {
                     mapMatrix[i][j] = '.';
                 }
                 else
                 {
                     mapMatrix[i][j] = ' ';
                 }
             }
         }
         
     }
     public int getHeight()
     {
         return this.height;
     }
     public int getWidth()
     {
         return this.width;
     }
 
     public boolean isEmpty(int x, int y)
     {
         char c = startMap.get(y).charAt(x);
         return (c == ' ' | c == '@');
         
         
     }
 
     public boolean isWall(int x, int y)
     {
         char c = startMap.get(y).charAt(x);
         return (c == '#');
         
         
     }
 
     public boolean isGoal(int x, int y)
     {
         char c = startMap.get(y).charAt(x);
         return (c == '.');
     }
     
     public boolean isGoalOnBox(int x, int y)
     {
         char c = startMap.get(y).charAt(x);
         return (c == '*'); 
     }
 
     /**
      *
      * @return String representing the map
      */
     public String toString()
     {
         String out = new String("");
         for (int i = 0; i < startMap.size(); i++) {
             if (i==0) {
                 out += startMap.get(i);
             } else {
                 out += System.getProperty("line.separator")+startMap.get(i);
             }
         }
         return out;
     }
     
     public char[][] mapMatrix;
     private Vector<String> startMap;
     private int height;
     private int width;
 }
