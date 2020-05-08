 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of MineStarWarp.
  * 
  * MineStarWarp is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * MineStarWarp is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with MineStarWarp.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.minestar.MineStarWarp.localization;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.LinkedList;
 
 import com.minestar.MineStarWarp.Main;
 
 public class Localization implements LocalizationConstants {
 
     private static Localization instance;
 
     private String[] texts;
 
     private Localization(String language) {
         loadtexts(language);
     }
 
     private void loadtexts(String language) {
 
        Main.log.printInfo("Start loading texts for language " + language);

         File temp = new File("plugins/MineStarWarp/localization/" + language
                 + ".txt");
 
         if (!temp.exists()) {
             Main.log.printWarning("No localization found! Please insert one into the localized folder!");
             return;
         }
 
         String line = "";
         try {
 
             BufferedReader bReader = new BufferedReader(new FileReader(temp));
             LinkedList<String> list = new LinkedList<String>();
             while ((line = bReader.readLine()) != null) {
                 if (line.startsWith("#") || line.trim().isEmpty())
                     continue;
 
                 list.add(line.split(" = ", 2)[1]);
             }
             texts = new String[list.size()];
             texts = list.toArray(texts);
             bReader.close();
 
         }
         catch (Exception e) {
             Main.log.printError("Error while loading the localized texts", e);
             Main.log.printWarning(line);
         }
 
         Main.log.printInfo("Loaded " + texts.length + " localized texts");
     }
 
     public static Localization getInstance(String language) {
 
         if (instance == null)
             instance = new Localization(language);
         return instance;
     }
     
     public String get(int index) {
         if (index < 0 || index >= texts.length)
             return null;
         return texts[index];
     }
 
     public String get(int index, String... args) {
 
         if (index < 0 || index >= texts.length)
             return null;
         else
             return String.format(texts[index], (Object[])args);
     }
 }
