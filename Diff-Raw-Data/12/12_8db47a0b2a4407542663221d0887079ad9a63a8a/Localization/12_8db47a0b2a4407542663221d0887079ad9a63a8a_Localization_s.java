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
 import java.util.HashMap;
 
 import com.minestar.MineStarWarp.Main;
 
 public class Localization {
 
     private static Localization instance;
     // key = node , value = text
     private HashMap<String, String> texts = new HashMap<String, String>();
 
     private Localization(String language) {
         loadtexts(language);
     }
 
     private void loadtexts(String language) {
 
         File temp = new File("plugins/MineStarWarp/localization/" + language
                 + ".txt");
 
         if (!temp.exists()) {
             Main.log.printWarning("No localization found! Please insert one into the localized folder!");
             return;
         }
 
         String line = "";
         try {

            BufferedReader bReader = new BufferedReader(new FileReader(temp));
             String node = "";
             String[] split;
             while ((line = bReader.readLine()) != null) {
                 if (line.trim().isEmpty())
                     continue;
                 if (line.startsWith("#")) {
                     node = line.replace("#", "").concat(".");
                     continue;
                 }
                 line = new String(line.getBytes(), "UTF-8");
                 split = line.split(" = ", 2);
 
                 texts.put(node.concat(split[0]), split[1]);
             }
             bReader.close();
         }
         catch (Exception e) {
             Main.log.printError("Error while loading the localized texts", e);
             Main.log.printWarning(line);
         }
 
         Main.log.printInfo("Loaded " + texts.size() + " localized texts");
     }
 
     public static Localization getInstance(String language) {
 
         if (instance == null)
             instance = new Localization(language);
         return instance;
     }
 
     public String get(String node) {
         String text = texts.get(node);
         if (text == null) {
             Main.log.printWarning("ERROR! Cannot find a text for the node "
                     + node);
             return "Fehler beim laden eines Textes! Bitte an einen Admin wenden!";
         }
         return text;
     }
 
     public String get(String node, String... args) {
         String text = texts.get(node);
         if (text == null) {
             Main.log.printWarning("ERROR! Cannot find a text for the node "
                     + node);
             return "Fehler beim laden eines Textes! Bitte an einen Admin wenden!";
         }
         return String.format(texts.get(node), (Object[]) args);
     }
 }
