 /*
  * MiningContest
  * Copyright (C) 2011 IndiPlex
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.indiplex.miningcontest.map;
 
 import de.indiplex.miningcontest.generator.Base;
 import de.indiplex.miningcontest.generator.Outpost;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import javax.imageio.ImageIO;
 
 /**
  *
  * @author IndiPlex <Cartan12@indiplex.de>
  */
 public class MapParser {
     
     public static final HashMap<Integer, Integer> wools = new HashMap<Integer, Integer>();    
     
     static {
         wools.put(0xFFFFFFFF, 0x0);
         wools.put(0xFF000000, 0xF);
         wools.put(0xFFFF0000, 0xE);
         wools.put(0xFF00FF00, 0x5);
         wools.put(0xFFFFFF00, 0x4);
         wools.put(0xFF0000FF, 0xB);
         wools.put(0xFF00FFFF, 0x3);        
     }
     
    public static Map parseMap(String mapName) {
         try {            
            File mapFile = new File(mapName);
             BufferedImage image = ImageIO.read(mapFile);
             int width = image.getWidth();
             int heigth = image.getHeight();
             
             int[][] data = new int[width][heigth];
             byte[][] wool = new byte[width][heigth];
             for (int x=0;x<width;x++) {
                 for (int y=0;y<heigth;y++) {
                     int c = image.getRGB(x, y);  
                     c = optimizeColor(c);
                     wool[x][y] = closestWoolColor(c);
                     data[x][y] = c;
                 }
             }
             
             MapChunk[][] chunks = new MapChunk[21][21];
             
             for (int x=0;x<21;x++) {
                 for (int y=0;y<21;y++) {
                     int[][] cdata = new int[16][16];
                     for (int cx=0;cx<16;cx++) {
                         for (int cy=0;cy<16;cy++) {
                             cdata[cx][cy] = data[x*16+cx][y*16+cy];
                         }
                     }
                     MapChunk mc = new MapChunk(new Point(x-10, y-10), cdata);
                     if (mc.getType().equals(MapChunk.Type.OUTPOST)) {
                         mc = new Outpost(mc.getPos(), data, mc.getRoom(), MapChunk.Type.OUTPOST);
                     } else if (mc.getType().equals(MapChunk.Type.BASE)) {
                         mc = new Base(mc.getPos(), data, mc.getRoom(), MapChunk.Type.BASE);
                     }
                     chunks[x][y] = mc;
                 }
             }
             return new Map(wool, chunks, image);
         } catch (IOException ex) {
             ex.printStackTrace();
             return null;
         }
     }
     
     private static byte closestWoolColor(int of) {
         int min = Integer.MAX_VALUE;
         int closest = of;
 
         for (int v : wools.keySet()) {
             if (v==of) {
                 closest = v;
             }
         }        
         Integer get = wools.get(closest);
         if (get==null) {
             System.out.println(Integer.toHexString(of));
             return 0;
         }
         return get.byteValue();
     }
     private static int optimizeColor(int of) {
         int min = Integer.MAX_VALUE;
         int closest = of;
 
         for (int v : wools.keySet()) {
             final int diff = Math.abs(v - of);
 
             if (diff < min) {
                 min = diff;
                 closest = v;
             }
         }
 
         return closest;
     }
 }
