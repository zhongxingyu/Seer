 package com.jordan.tunnelgame;
 
 import android.util.Log;
 
 import com.jordan.framework.Graphics;
 import com.jordan.framework.Graphics.ImageFormat;
 import com.jordan.framework.Image;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Assets {
 
     public static Image pauseicon, selectedwarp,editorreturn;
     public static Image background , menu;
     public static Button buttonretry, buttonresume, buttonnextlevel, buttonmainmenu;
 
     public static Image iChaser, iOrb, iTile;
     public static Image iBasicTile, iRockTile, iStoneTile, iFireTile, iIceTile, iMetalTile, iSnowTile, iWarpTile, iFinishTile;
 
     //public static Sound click;
     //public static Music theme;
 
     public static ArrayList<Image> tiles = new ArrayList<Image>();
     public static ArrayList<String> charCodes = new ArrayList<String>();
     public static ArrayList<String> backgrounds = new ArrayList<String>();
     public static boolean loaded = false;
 
     public static Graphics g;
 
     public static void loadSplash(Graphics h) {
         g = h;
        menu = g.newImage("splashmenu.png", ImageFormat.RGB565);
     }
 
     public static void load() { if (!loaded) { loaded = true;
 
         backgrounds.add("");
         backgrounds.add("moon");
         backgrounds.add("night");
         backgrounds.add("sunset");
 
         //smallish
         buttonretry = new Button(g.newImage("buttonretry.png", ImageFormat.RGB565),g.newImage("pushedbuttonretry.png", ImageFormat.RGB565));
         buttonmainmenu = new Button(g.newImage("buttonmainmenu.png", ImageFormat.RGB565),g.newImage("pushedbuttonmainmenu.png", ImageFormat.RGB565));
         buttonnextlevel = new Button(g.newImage("buttonnextlevel.png", ImageFormat.RGB565),g.newImage("pushedbuttonnextlevel.png", ImageFormat.RGB565));
         buttonresume = new Button(g.newImage("buttonresume.png", ImageFormat.RGB565),g.newImage("pushedbuttonresume.png", ImageFormat.RGB565));
         pauseicon = g.newImage("pauseicon.png", ImageFormat.RGB565);//small
         selectedwarp = g.newImage("selectedwarp.png", ImageFormat.RGB565);//small
         editorreturn = g.newImage("editorreturn.png", ImageFormat.RGB565);//small
 
         //smallish
         iTile = g.newImage("emptytile.png", ImageFormat.RGB565); tiles.add(iTile); charCodes.add( "  ");
         iChaser = g.newImage("chaser.png", ImageFormat.RGB565); tiles.add(iChaser); charCodes.add("Ca");
         iOrb = g.newImage("orb.png", ImageFormat.RGB565); tiles.add(iOrb); charCodes.add( "Oa");
         iWarpTile = g.newImage("warp.png", ImageFormat.RGB565); tiles.add(iWarpTile); charCodes.add( "w0");
         iFinishTile = g.newImage("finish.png", ImageFormat.RGB565); tiles.add(iFinishTile); charCodes.add( "f!");
         iBasicTile = g.newImage("basictile.png", ImageFormat.RGB565); tiles.add(iBasicTile); charCodes.add( "b+");
         iRockTile = g.newImage("midtile.png", ImageFormat.RGB565); tiles.add(iRockTile); charCodes.add( "b-");
         iStoneTile = g.newImage("stonetile.png", ImageFormat.RGB565); tiles.add(iStoneTile); charCodes.add( "b/");
         iFireTile = g.newImage("firetile.png", ImageFormat.RGB565); tiles.add(iFireTile); charCodes.add( "d*");
         iIceTile = g.newImage("icetile.png", ImageFormat.RGB565); tiles.add(iIceTile); charCodes.add( "s_");
         iMetalTile = g.newImage("metaltile.png", ImageFormat.RGB565); tiles.add(iMetalTile); charCodes.add( "b&");
         iSnowTile = g.newImage("snowtile.png", ImageFormat.RGB565); tiles.add(iSnowTile); charCodes.add( "b^");
 
     }}
 
     public static void backgroundByString(String bg)
     {
         if (bg.equals("del")) {
             background = null;
         }
         background =  g.newImage("background" + bg + ".png", ImageFormat.RGB565);
     }
 
     public static void menuByString(String mn)
     {
         menu = g.newImage(mn + "menu.png", ImageFormat.RGB565);
         /*failmenu = g.newImage("failmenu.png", ImageFormat.RGB565); //big
         finishmenu = g.newImage("finishmenu.png", ImageFormat.RGB565);//big
         packselect = g.newImage("packmenu.png", ImageFormat.RGB565);//big
         levelselect = g.newImage("levelmenu.png", ImageFormat.RGB565);//big
         readymenu = g.newImage("readymenu.png", ImageFormat.RGB565);//big
         pausemenu = g.newImage("pausemenu.png", ImageFormat.RGB565);//big
         editoricons = g.newImage("editormenu.png", ImageFormat.RGB565);//big
         toomanywarps = g.newImage("toomanywarpsmenu.png", ImageFormat.RGB565);//bg
         blockselect = g.newImage("blockmenu.png", ImageFormat.RGB565);//big*/
     }
 
 
 
 
     public static double distance(Coord a, Coord b) {
         return Math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y));
     }
 
     public static boolean inBounds(Coord a, Coord b, Coord c) {
         return a.x>b.x && a.x<c.x && a.y>b.y && a.y < c.y;
     }
 
     public static File fileLocation;
     public static void writeToMemory(String fileName, String text)
     {//needed to instantiate file directory in sample game activity
         Log.e("olderorbgame", fileName);
         try {
             BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(fileLocation,fileName)));
             bufferedWriter.write(text);
             bufferedWriter.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     public static String readFromMemory(String fileName)
     {
         String res = "";
         try {
             BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(fileLocation,fileName)));
             String read;
             StringBuilder builder = new StringBuilder("");
             while((read = bufferedReader.readLine()) != null){
                 builder.append(read);
             }
             res = builder.toString();
             bufferedReader.close();
         } catch (IOException e) {
 
         }
         return res;
     }
 
     public static String getLevelString(int packID, int levelNum) {
         return LevelStorage.getLevelString(packID,levelNum);
     }
 
 }
