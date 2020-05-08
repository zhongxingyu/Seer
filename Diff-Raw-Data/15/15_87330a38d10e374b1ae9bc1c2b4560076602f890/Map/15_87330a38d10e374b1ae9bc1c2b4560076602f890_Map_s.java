 package com.willbat.MotherlAndroid;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 /**
  * This class contains the map of tiles that is the rendered game world
  * TODO: This will also contain the code used to generate the world and distribute minerals
  * TODO: add conditional constructors for loading maps
  * TODO: Maps could be stored as JSON objects, but it might make more sense just to store arrays of numbers and use the tiletype enum
  */
 public class Map
 {
     Tile[][][] tiles;
 
     public Map(int width, int height)
     {
        tiles = new Tile[height][width][3];
         generateMap();
     }
 
     public Map(String filename)
     {
 
     }
 
     private void generateMap()
     {
         //int layerNumber = 0;
         for (Tile[][] columnRow : tiles)
         {
             int rowNumber = 0;
             for (Tile[] row : columnRow)
             {
                 int columnNumber = 0;
                 for (Tile tile : row)
                 {
                     if (rowNumber < 2)
                     {
                         row[columnNumber] = new Tile(Tiletype.AIR, columnNumber, rowNumber);
                     }
                     else if (rowNumber > 2 && rowNumber < 5)
                     {
                         row[columnNumber] = new Tile(Tiletype.DIRT, columnNumber, rowNumber);
                     }
                     else
                     {
                         row[columnNumber] = new Tile(Tiletype.COAL, columnNumber, rowNumber);
                     }
                     columnNumber++;
                 }
                 rowNumber++;
             }
             //layerNumber++;
         }
     }
 
     public void draw(SpriteBatch batch)
     {
         int layerNumber = 0;
         for (Tile[][] columnRow : tiles)
         {
             int columnNumber = 0;
             for (Tile[] row : columnRow)
             {
                 int rowNumber = 0;
                 for (Tile tile : row)
                 {
                     if (layerNumber == 0)
                     {
                         if (tile != null && !tiles[layerNumber+2][columnNumber][rowNumber].visible && !tiles[layerNumber+1][columnNumber][rowNumber].visible && tile.visible)
                         {
                             tile.draw(batch);
                         }
                     }
                     else if (layerNumber == 1)
                     {
                         if (tile != null && !tiles[layerNumber+1][columnNumber][rowNumber].visible && tile.visible)
                         {
                             tile.draw(batch);
                         }
                     }
                     else if (layerNumber == 2)
                     {
                         if (tile != null && tile.visible)
                         {
                             tile.draw(batch);
                         }
                     }
                     rowNumber++;
                 }
                 columnNumber++;
             }
             layerNumber++;
         }
     }
 }
