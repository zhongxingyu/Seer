 package com.purplefrog.minecraftExplorer;
 
 import java.awt.*;
 import java.io.*;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: thoth
  * Date: 4/12/13
  * Time: 3:31 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CityScape
 {
 
     public static void main(String[] argv)
         throws IOException
     {
         BlockEditor editor = new BlockEditor(new MinecraftWorld(WorldPicker.pickSaveDir()));
 
         Random rand = new Random();
 
         Streetscape ss = new Streetscape(4, 6);
 
 
         int x0=200;
        int y0=80;
         int z0 =100;
 
         {
             int y9 = y0 - 5;
             editor.fillCube(0, x0, y9, z0, 200, 250- y9, 200);
         }
 
 
         for (int u=0; u+1<ss.nStreets; u++) {
             for (int v=0; v+1<ss.nAvenues; v++) {
                 Rectangle r = ss.meadowSouthEastOf(u,v);
                 r.x += x0;
                 r.y += z0;
 
                 randomBuilding(editor, r, y0+ss.meadow.elevation, rand);
 
                 if (false ) {
                     // lapis rectangle for debugging
                     for (int a=0; a<r.width; a++) {
                         for (int b=0; b<r.height; b++) {
                             editor.setBlock(r.x+a, y0+10, r.y+b, 22);
                         }
                     }
                 }
             }
         }
 
         ss.render(editor, x0, y0, z0);
 
         editor.relight();
 
         editor.save();
     }
 
     public static void randomBuilding(BlockEditor editor, Rectangle emptyLot, int y0, Random rand)
         throws IOException
     {
 
 
         Object [] windows = ClipArt.randomBuildingDecor(rand);
 
         int[] column = (int[]) windows[2];
 
         int nFloors = (rand.nextInt(250-y0-12)+12) / column.length;
 
         SkyScraper1.WindowShape window1 = (SkyScraper1.WindowShape) windows[0];
         SkyScraper1.WindowShape window2 = (SkyScraper1.WindowShape) windows[1];
 
         int uCells = (emptyLot.width - 2 - 1) / window1.cellWidth;
         int vCells = (emptyLot.height - 2 - 1) / window2.cellWidth;
 
         SkyScraper1 scraper = new SkyScraper1(uCells, vCells, nFloors, column, window1, window2);
 
         int dx = (emptyLot.width - scraper.xDimension())/2;
         int dz = (emptyLot.height - scraper.zDimension())/2;
 
         scraper.render(editor, emptyLot.x+dx , y0, emptyLot.y+dz);
     }
 }
