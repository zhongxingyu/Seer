 /*
  *  Copyright 2008, 2009, 2010, 2011:
  *   Tobias Fleig (tfg[AT]online[DOT]de),
  *   Michael Haas (mekhar[AT]gmx[DOT]de),
  *   Johannes Kattinger (johanneskattinger[AT]gmx[DOT]de)
  *
  *  - All rights reserved -
  *
  *
  *  This file is part of Centuries of Rage.
  *
  *  Centuries of Rage is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Centuries of Rage is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Centuries of Rage.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package de._13ducks.cor.game.server;
 
 import de._13ducks.cor.game.Building;
 import de._13ducks.cor.game.GameObject;
 import de._13ducks.cor.game.Unit;
 import java.io.*;
 import java.util.*;
 import de._13ducks.cor.game.ability.ServerAbilityUpgrade.upgradeaffects;
 import java.security.*;
 import jonelo.jacksum.*;
 import jonelo.jacksum.algorithm.*;
 import de._13ducks.cor.game.BehaviourProcessor;
 import de._13ducks.cor.game.DescParamsBuilding;
 import de._13ducks.cor.game.DescParamsUnit;
 import de._13ducks.cor.networks.client.behaviour.DeltaUpgradeParameter;
 import de._13ducks.cor.game.NetPlayer;
 import de._13ducks.cor.game.PlayersBuilding;
 import de._13ducks.cor.map.CoRMap;
 import de._13ducks.cor.game.Unit2x2;
 import de._13ducks.cor.game.Unit3x3;
 import de._13ducks.cor.game.ability.ServerAbilityUpgrade;
 import de._13ducks.cor.game.server.movement.MovementMap;
 import de._13ducks.cor.map.MapIO;
 
 /**
  * Das MapModul auf der Server-Seite
  * Liest .map - Dateien, bereitet sie auf und verschickt sie an alle Clients
  * Verwaltet die 3 großen ArrayLists (Einheiten, Gebäude, Ressourcen)
  * Verwaltet den netID - Vector
  *
  * @author tfg
  */
 public class ServerMapModule {
 
     ServerCore.InnerServer rgi;
     CoRMap theMap;
     HashMap<Integer, Unit> descTypeUnit;
     HashMap<Integer, Building> descTypeBuilding;
     HashMap<Integer, ServerAbilityUpgrade> descTypeAbilities;
     boolean syntaxWarned = false; // Ob schon eine Warnung wegen Syntax-Fehlern angezeigt wurde.
     HashMap<Integer, GameObject> netIDList;
     public List<Unit> unitList;
     public List<Building> buildingList;
     private int nextNetID = 1;
     int mapHash;
     public byte[] abBuffer;
     public byte[] descBuffer;
     public byte[] mapBuffer;
     private MovementMap moveMap;
 
     ServerMapModule(ServerCore.InnerServer in) {
         rgi = in;
         netIDList = new HashMap<Integer, GameObject>();
     }
 
     void initModule() {
         // Dateiliste erstellen
 
         ArrayList<File> olist = new ArrayList<File>();
         ArrayList<File> flist = new ArrayList<File>();
         ArrayList<File> dlist = new ArrayList<File>();
         ArrayList<File> alist = new ArrayList<File>();
 
         File OrdnerSuchen = new File("game/epoch/"); //Unterordner in "game" suchen
         File[] Ordner = OrdnerSuchen.listFiles();
         for (File ord : Ordner) {
             if (ord.isDirectory() && !ord.getName().startsWith(".") && !ord.getName().startsWith(".")) {
                 olist.add(ord);
             }
         }
 
         for (int i = 0; i < olist.size(); i++) { //Dateien in den Unterordnern suchen
             String Unterordner = olist.get(i).toString();
             File gameOrdner = new File(Unterordner);
             File[] files = gameOrdner.listFiles();
             flist.addAll(Arrays.asList(files));
         }
 
         for (int i = 0; i < flist.size(); i++) {
             File file = flist.get(i);
             if (file.getName().endsWith("~") || file.getName().startsWith(".")) { // Sicherungsdateien und unsichtbares Aussortieren
                 flist.remove(i);
                 i--;
             } else {
                 if (file.getName().startsWith("a") || file.getName().startsWith("A")) {
                     alist.add(file);
                 } else if (file.getName().startsWith("d") || file.getName().startsWith("D")) {
                     dlist.add(file);
                 }
             }
         }
         // Abilitys einmal reinladen für die Clients
         rgi.logger("[MapModule]: Loading Abilities (clients)...");
         FileInputStream stream = null;
         // Puffer-Größe berechnen
         int buffSize = 0;
         for (File abFile : alist) {
             buffSize += abFile.length();
             buffSize++; // Zeilensprung
         }
         abBuffer = new byte[buffSize];
         int runIndex = 0;
         for (File abFile : alist) {
             try {
                 stream = new FileInputStream(abFile);
                 while (stream.available() > 0) {
                     abBuffer[runIndex] = (byte) stream.read();
                     runIndex++;
                 }
                 // Zeilensprung
                 abBuffer[runIndex] = '\n';
                 runIndex++;
             } catch (FileNotFoundException ex) {
                 rgi.logger(ex);
                 rgi.logger("[ServerMapModule][ERROR]: Can't load abilit-list " + abFile.getAbsolutePath());
             } catch (IOException ex) {
                 rgi.logger(ex);
                 rgi.logger("[MapModule][ERROR]: Can't read abilit-list " + abFile.getAbsolutePath());
             } finally {
                 try {
                     stream.close();
                 } catch (IOException ex) {
                 }
             }
         }
         // Einglesen, normal weitermachen.
 
         rgi.logger("[MapModule]: Loading Abilities (self)...");
         descTypeAbilities = new HashMap<Integer, ServerAbilityUpgrade>();
 
         for (File descFile : alist) {
             // Read abilitys
             FileReader abReader = null;
             try {
                 abReader = new FileReader(descFile);
                 BufferedReader bdescReader = new BufferedReader(abReader);
                 String zeile = null;
                 // Einlesen
                 boolean inDesc = false;
                 ServerAbilityUpgrade au = null;
                 byte type = 0;
                 int id = 0;
                 int line = 0;
                 while ((zeile = bdescReader.readLine()) != null) {
                     line++;
                     // Zeile interpretieren
                     if (!zeile.isEmpty()) {
                         char first = zeile.charAt(0);
                         if (first == '#') {
                             // Kommentar, ignorieren
                             continue;
                         }
 
                         // Sind wir grad in der Klammer ?
                         if (inDesc) {
                             // Beim = trennen
                             int indexgleich = zeile.indexOf('='); // Istgleich suchen
                             if (indexgleich != -1) {
                                 String v1 = zeile.substring(0, indexgleich);
                                 String v2 = zeile.substring(indexgleich + 1);
                                 int indexraute = v2.indexOf('#'); //Kommentar am Ende der Zeile?
                                 if (indexraute != -1) {
                                     v2 = v2.substring(0, indexraute - 1);
                                 }
 
                                 if (type == 0) {
                                     // Typ suchen
                                     if (v1.equals("type")) {
                                         if ("upgrade".equals(v2)) {
                                             // Type RogGameObjectAbilityUrgrade
                                             au = new ServerAbilityUpgrade(id);
                                             au.rgi = rgi;
                                             type = 3;
                                         }
                                     }
                                 } else if (type == 3) {
                                     if ("affects".equals(v1)) {
                                         if (v2.contains("all")) {
                                             au.affects = upgradeaffects.all;
                                         } else if (v2.contains("fresh")) {
                                             au.affects = upgradeaffects.fresh;
                                         } else if (v2.contains("old")) {
                                             au.affects = upgradeaffects.old;
                                         } else if (v2.contains("self")) {
                                             au.affects = upgradeaffects.self;
                                         }
                                     } else if ("uptype".equals(v1)) {
                                         if (v2.equals("epoche")) {
                                             au.epocheUpgrade = true;
                                         }
                                     } else if ("reference".equals(v1)) {
                                         if (v2.startsWith("U")) {
                                             au.descTypeIdU = saveStrtoInt(v2.substring(1), zeile, line);
                                         } else if (v2.startsWith("B")) {
                                             au.descTypeIdB = saveStrtoInt(v2.substring(1), zeile, line);
                                         }
                                     } else if ("newTex".equals(v1)) {
                                         au.newTex = v2;
                                     } else if ("newarmortype".equals(v1)) {
                                         au.newarmortype = v2;
                                     } else if ("hitpointsup".equals(v1)) {
                                         au.hitpointsup = saveStrtoInt(v2, zeile, line);
                                     } else if ("maxhitpointsup".equals(v1)) {
                                         au.maxhitpointsup = saveStrtoInt(v2, zeile, line);
                                     } else if ("antiheavyinfup".equals(v1)) {
                                         au.antiheavyinfup = saveStrtoInt(v2, zeile, line);
                                     } else if ("antilightinfup".equals(v1)) {
                                         au.antilightinfup = saveStrtoInt(v2, zeile, line);
                                     } else if ("antiairup".equals(v1)) {
                                         au.antiairup = saveStrtoInt(v2, zeile, line);
                                     } else if ("antibuildingup".equals(v1)) {
                                         au.antibuildingup = saveStrtoInt(v2, zeile, line);
                                     } else if ("antikavup".equals(v1)) {
                                         au.antikavup = saveStrtoInt(v2, zeile, line);
                                     } else if ("antitankup".equals(v1)) {
                                         au.antitankup = saveStrtoInt(v2, zeile, line);
                                     } else if ("antivehicleup".equals(v1)) {
                                         au.antivehicleup = saveStrtoInt(v2, zeile, line);
                                     } else if ("damageup".equals(v1)) {
                                         au.damageup = saveStrtoInt(v2, zeile, line);
                                     } else if ("toAnimDesc".equals(v1)) {
                                         au.toAnimDesc = saveStrtoInt(v2, zeile, line);
                                     } else if ("maxIntraup".equals(v1)) {
                                         au.maxIntraup = saveStrtoInt(v2, zeile, line);
                                     } else if ("damageup".equals(v1)) {
                                         au.damageup = saveStrtoInt(v2, zeile, line);
                                     } else if ("bulletspeedup".equals(v1)) {
                                         au.bulletspeedup = saveStrtoInt(v2, zeile, line);
                                     } else if ("rangeup".equals(v1)) {
                                         au.rangeup = saveStrtoInt(v2, zeile, line);
                                     } else if ("speedup".equals(v1)) {
                                         au.speedup = saveStrtoDouble(v2, zeile, line);
                                     } else if ("harv".equals(v1)) {
                                         if (v2.contains("true")) {
                                             au.harv = true;
                                         }
                                     } else if ("toEpoche".equals(v1)) {
                                         au.toEpoche = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.startsWith("B")) {
                                         int desc = saveStrtoInt(v1.substring(1, v1.indexOf(".")), zeile, line);
                                         if (desc != 0) {
                                             // Wenn dazu noch kein Eintrag da ist, dann einen Anlegen
                                             if (!au.edelta.containsKey("B" + desc)) {
                                                 DeltaUpgradeParameter p = new DeltaUpgradeParameter();
                                                 p.moddesc = desc;
                                                 p.modunit = false;
                                                 au.edelta.put("B" + desc, p);
                                             }
                                             // Eintragen
                                             String v1s = v1.substring(v1.indexOf(".") + 1);
                                             DeltaUpgradeParameter dup = au.edelta.get("B" + desc);
                                             if ("newTex".equals(v1s)) {
                                                 dup.newTex = v2;
                                             } else if ("newarmortype".equals(v1s)) {
                                                 dup.newarmortype = v2;
                                             } else if ("hitpointsup".equals(v1s)) {
                                                 dup.hitpointsup = saveStrtoInt(v2, zeile, line);
                                             } else if ("maxhitpointsup".equals(v1s)) {
                                                 dup.maxhitpointsup = saveStrtoInt(v2, zeile, line);
                                             } else if ("antiheavyinfup".equals(v1s)) {
                                                 dup.antiheavyinfup = saveStrtoInt(v2, zeile, line);
                                             } else if ("antilightinfup".equals(v1s)) {
                                                 dup.antilightinfup = saveStrtoInt(v2, zeile, line);
                                             } else if ("antiairup".equals(v1s)) {
                                                 dup.antiairup = saveStrtoInt(v2, zeile, line);
                                             } else if ("antibuildingup".equals(v1s)) {
                                                 dup.antibuildingup = saveStrtoInt(v2, zeile, line);
                                             } else if ("antikavup".equals(v1s)) {
                                                 dup.antikavup = saveStrtoInt(v2, zeile, line);
                                             } else if ("antitankup".equals(v1s)) {
                                                 dup.antitankup = saveStrtoInt(v2, zeile, line);
                                             } else if ("antivehicleup".equals(v1s)) {
                                                 dup.antivehicleup = saveStrtoInt(v2, zeile, line);
                                             } else if ("damageup".equals(v1s)) {
                                                 dup.damageup = saveStrtoInt(v2, zeile, line);
                                             } else if ("toAnimDesc".equals(v1s)) {
                                                 dup.toAnimDesc = saveStrtoInt(v2, zeile, line);
                                             } else if ("maxIntraup".equals(v1s)) {
                                                 dup.maxIntraup = saveStrtoInt(v2, zeile, line);
                                             } else if ("healup".equals(v1s)) {
                                                 dup.healup = saveStrtoInt(v2, zeile, line);
                                             }
                                         }
                                     } else if (v1.startsWith("U")) {
                                         int desc = saveStrtoInt(v1.substring(1, v1.indexOf(".")), zeile, line);
                                         if (desc != 0) {
                                             // Wenn dazu noch kein Eintrag da ist, dann einen Anlegen
                                             if (!au.edelta.containsKey("U" + desc)) {
                                                 DeltaUpgradeParameter p = new DeltaUpgradeParameter();
                                                 p.moddesc = desc;
                                                 p.modunit = true;
                                                 au.edelta.put("U" + desc, p);
                                             }
                                             // Eintragen
                                             String v1s = v1.substring(v1.indexOf(".") + 1);
                                             DeltaUpgradeParameter dup = au.edelta.get("U" + desc);
                                             if ("newTex".equals(v1s)) {
                                                 dup.newTex = v2;
                                             } else if ("newarmortype".equals(v1s)) {
                                                 dup.newarmortype = v2;
                                             } else if ("hitpointsup".equals(v1s)) {
                                                 dup.hitpointsup = saveStrtoInt(v2, zeile, line);
                                             } else if ("maxhitpointsup".equals(v1s)) {
                                                 dup.maxhitpointsup = saveStrtoInt(v2, zeile, line);
                                             } else if ("antiheavyinfup".equals(v1s)) {
                                                 dup.antiheavyinfup = saveStrtoInt(v2, zeile, line);
                                             } else if ("antilightinfup".equals(v1s)) {
                                                 dup.antilightinfup = saveStrtoInt(v2, zeile, line);
                                             } else if ("antiairup".equals(v1s)) {
                                                 dup.antiairup = saveStrtoInt(v2, zeile, line);
                                             } else if ("antibuildingup".equals(v1s)) {
                                                 dup.antibuildingup = saveStrtoInt(v2, zeile, line);
                                             } else if ("antikavup".equals(v1s)) {
                                                 dup.antikavup = saveStrtoInt(v2, zeile, line);
                                             } else if ("antitankup".equals(v1s)) {
                                                 dup.antitankup = saveStrtoInt(v2, zeile, line);
                                             } else if ("antivehicleup".equals(v1s)) {
                                                 dup.antivehicleup = saveStrtoInt(v2, zeile, line);
                                             } else if ("damageup".equals(v1s)) {
                                                 dup.damageup = saveStrtoInt(v2, zeile, line);
                                             } else if ("bulletspeedup".equals(v1)) {
                                                 dup.bulletspeedup = saveStrtoInt(v2, zeile, line);
                                             } else if ("toAnimDesc".equals(v1s)) {
                                                 dup.toAnimDesc = saveStrtoInt(v2, zeile, line);
                                             } else if ("damageup".equals(v1s)) {
                                                 dup.damageup = saveStrtoInt(v2, zeile, line);
                                             } else if ("rangeup".equals(v1s)) {
                                                 dup.rangeup = saveStrtoInt(v2, zeile, line);
                                             } else if ("speedup".equals(v1s)) {
                                                 dup.speedup = saveStrtoDouble(v2, zeile, line);
                                             } else if ("harv".equals(v1s)) {
                                                 if (v2.contains("true")) {
                                                     dup.harv = true;
                                                 }
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
 
                     if (zeile.contains("}")) {
                         // Fertig - einfügen
                         if (au != null) {
                             descTypeAbilities.put(id, au);
                         }
                         au = null;
                         inDesc = false;
                     }
 
                     if (zeile.contains("{")) {
                         // Neuer Eintrag
                         inDesc = true;
                         int indexL2 = zeile.lastIndexOf(" ");
                         String v3 = zeile.substring(0, indexL2);
                         id = Integer.parseInt(v3);
                         type = 0;
                     }
                 }
 
             } catch (FileNotFoundException ex) {
                 rgi.logger(ex);
                 rgi.logger("[ServerMapModule][ERROR]: Can't load abilit-list " + descFile.getAbsolutePath());
             } catch (IOException ex) {
                 rgi.logger(ex);
                 rgi.logger("[MapModule][ERROR]: Can't read abilit-list " + descFile.getAbsolutePath());
             } finally {
                 try {
                     abReader.close();
                 } catch (IOException ex) {
                 }
             }
         }
 
         // DESC einmal reinladen für die Clients
         rgi.logger("[MapModule]: Loading DESC (clients)...");
         FileInputStream stream2 = null;
         // Puffer-Größe berechnen
         int buffSize2 = 0;
         for (File deFile : dlist) {
             buffSize2 += deFile.length();
             buffSize2 += 1; // Einen Zeilensprung einfügen
         }
         descBuffer = new byte[buffSize2];
         int runIndex2 = 0;
         for (File deFile : dlist) {
             try {
                 stream = new FileInputStream(deFile);
                 while (stream.available() > 0) {
                     descBuffer[runIndex2] = (byte) stream.read();
                     runIndex2++;
                 }
                 // Zeilensprung
                 descBuffer[runIndex2] = '\n';
                 runIndex2++;
             } catch (FileNotFoundException ex) {
                 rgi.logger(ex);
                 rgi.logger("[ServerMapModule][ERROR]: Can't load desc-list " + deFile.getAbsolutePath());
             } catch (IOException ex) {
                 rgi.logger(ex);
                 rgi.logger("[MapModule][ERROR]: Can't read desc-list " + deFile.getAbsolutePath());
             } finally {
                 try {
                     stream.close();
                 } catch (IOException ex) {
                 }
             }
         }
         // Einglesen, normal weitermachen.
 
         rgi.logger("[MapModule]: Loading Unit-/Building-Types (self)...");
         descTypeUnit = new HashMap<Integer, Unit>();
         descTypeBuilding = new HashMap<Integer, Building>();
 
         for (File descFile : dlist) {
             // Read game/descTypes
             FileReader descReader = null;
             try {
                 descReader = new FileReader(descFile);
                 BufferedReader bdescReader = new BufferedReader(descReader);
                 String zeile = null;
                 // Einlesen
                 boolean inDesc = false;
                 String mode = null;
                 DescParamsBuilding rB = null;
                 DescParamsUnit rU = null;
                 int id = 0;
                 int line = 0;
                 while ((zeile = bdescReader.readLine()) != null) {
                     line++;
                     // Zeile interpretieren
                     if (!zeile.isEmpty()) {
                         char first = zeile.charAt(0);
                         if (first == '#') {
                             // Kommentar, ignorieren
                             continue;
                         }
 
                         // Sind wir grad in der Klammer ?
                         if (inDesc) {
                             // Beim = trennen
                             int indexgleich = zeile.indexOf('='); // Istgleich suchen
                             if (indexgleich != -1) {
                                 String v1 = zeile.substring(0, indexgleich);
                                 String v2 = zeile.substring(indexgleich + 1);
                                 int indexraute = v2.indexOf('#'); //Kommentar am Ende der Zeile?
                                 if (indexraute != -1) {
                                     v2 = v2.substring(0, indexraute - 1);
                                 }
                                 if (mode.equals("B")) {
                                     // Gebäude
                                     if (v1.equals("name")) {
                                         rB.setDescName(v2);
                                     } else if (v1.equals("hitpoints")) {
                                         rB.setHitpoints(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("maxhitpoints")) {
                                         rB.setMaxhitpoints(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("Gdesc")) {
                                         rB.setDescDescription(v2);
                                     } else if (v1.equals("z1")) {
                                         rB.setZ1(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("z2")) {
                                         rB.setZ2(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("maxIntra")) {
                                         rB.setMaxIntra(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("harvests")) {
                                         rB.setHarvests(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("harvRate")) {
                                         rB.setHarvRate(saveStrtoDouble(v2, zeile, line));
                                     } else if (v1.equals("range")) {
                                         rB.setRange(saveStrtoDouble(v2, zeile, line));
                                     } else if (v1.equals("damage")) {
                                         rB.setDamage(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("cooldownmax")) {
                                         rB.setFireDelay(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("bulletspeed")) {
                                         rB.setBulletspeed(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("bullettexture")) {
                                         rB.setBullettexture(v2);
                                     } else if (v1.equals("atkdelay")) {
                                         rB.setAtkdelay(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("antiair")) {
                                         int[] damageFactors = rB.getDamageFactors();
                                         damageFactors[GameObject.ARMORTYPE_AIR] = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.equals("antibuilding")) {
                                         int[] damageFactors = rB.getDamageFactors();
                                         damageFactors[GameObject.ARMORTYPE_BUILDING] = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.equals("antiheavyinf")) {
                                         int[] damageFactors = rB.getDamageFactors();
                                         damageFactors[GameObject.ARMORTYPE_HEAVYINF] = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.equals("antikav")) {
                                         int[] damageFactors = rB.getDamageFactors();
                                         damageFactors[GameObject.ARMORTYPE_KAV] = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.equals("antilightinf")) {
                                         int[] damageFactors = rB.getDamageFactors();
                                         damageFactors[GameObject.ARMORTYPE_LIGHTINF] = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.equals("antitank")) {
                                         int[] damageFactors = rB.getDamageFactors();
                                         damageFactors[GameObject.ARMORTYPE_TANK] = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.equals("antivehicle")) {
                                         int[] damageFactors = rB.getDamageFactors();
                                         damageFactors[GameObject.ARMORTYPE_VEHICLE] = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.equals("accepts")) {
                                         if ("all".equals(v2)) {
                                             rB.setAccepts(Building.ACCEPTS_ALL);
                                         }
                                     } else if (v1.equals("visrange")) {
                                         rB.setVisrange(saveStrtoInt(v2, zeile, line));
                                     }
                                 } else if (mode.equals("U")) {
                                     // Einheiten
                                     if (v1.equals("name")) {
                                         rU.setDescName(v2);
                                     } else if (v1.equals("hitpoints")) {
                                         rU.setHitpoints(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("maxhitpoints")) {
                                         rU.setMaxhitpoints(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("Gdesc")) {
                                         rU.setDescDescription(v2);
                                     } else if (v1.equals("Gpro")) {
                                         rU.setDescPro(v2);
                                     } else if (v1.equals("size")) {
                                         if ("3x3".equals(v2)) {
                                             rU.setSize(3);
                                         }
                                     } else if (v1.equals("Gcon")) {
                                         rU.setDescCon(v2);
                                     } else if (v1.equals("speed")) {
                                         rU.setSpeed(saveStrtoDouble(v2, zeile, line));
                                     } else if (v1.equals("range")) {
                                         rU.setRange(saveStrtoDouble(v2, zeile, line));
                                     } else if (v1.equals("damage")) {
                                         rU.setDamage(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("cooldownmax")) {
                                         rU.setFireDelay(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("bulletspeed")) {
                                         rU.setBulletspeed(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("bullettexture")) {
                                         rU.setBullettexture(v2);
                                     } else if (v1.equals("atkdelay")) {
                                         rU.setAtkdelay(saveStrtoInt(v2, zeile, line));
                                     } else if (v1.equals("armortype")) {
                                         if ("lightinf".equals(v2)) {
                                             rU.setArmorType(GameObject.ARMORTYPE_LIGHTINF);
                                         } else if ("heavyinf".equals(v2)) {
                                             rU.setArmorType(GameObject.ARMORTYPE_HEAVYINF);
                                         } else if ("air".equals(v2)) {
                                             rU.setArmorType(GameObject.ARMORTYPE_AIR);
                                         } else if ("kav".equals(v2)) {
                                             rU.setArmorType(GameObject.ARMORTYPE_KAV);
                                         } else if ("tank".equals(v2)) {
                                             rU.setArmorType(GameObject.ARMORTYPE_TANK);
                                         } else if ("vehicle".equals(v2)) {
                                             rU.setArmorType(GameObject.ARMORTYPE_VEHICLE);
                                         }
                                     } else if (v1.equals("antiair")) {
                                         int[] damageFactors = rU.getDamageFactors();
                                         damageFactors[GameObject.ARMORTYPE_AIR] = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.equals("antibuilding")) {
                                         int[] damageFactors = rU.getDamageFactors();
                                         damageFactors[GameObject.ARMORTYPE_BUILDING] = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.equals("antiheavyinf")) {
                                         int[] damageFactors = rU.getDamageFactors();
                                         damageFactors[GameObject.ARMORTYPE_HEAVYINF] = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.equals("antikav")) {
                                         int[] damageFactors = rU.getDamageFactors();
                                         damageFactors[GameObject.ARMORTYPE_KAV] = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.equals("antilightinf")) {
                                         int[] damageFactors = rU.getDamageFactors();
                                         damageFactors[GameObject.ARMORTYPE_LIGHTINF] = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.equals("antitank")) {
                                         int[] damageFactors = rU.getDamageFactors();
                                         damageFactors[GameObject.ARMORTYPE_TANK] = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.equals("antivehicle")) {
                                         int[] damageFactors = rU.getDamageFactors();
                                         damageFactors[GameObject.ARMORTYPE_VEHICLE] = saveStrtoInt(v2, zeile, line);
                                     } else if (v1.equals("visrange")) {
                                         rU.setVisrange(saveStrtoInt(v2, zeile, line));
                                     }
                                 }
                             }
                         }
 
                         if (first == 'U') {
                             // Neue Einheit
                             rU = new DescParamsUnit();
                             inDesc = true;
                             int indexL1 = zeile.indexOf(" ");
                             int indexL2 = zeile.lastIndexOf(" ");
                             String v3 = zeile.substring(indexL1 + 1, indexL2);
                             id = Integer.parseInt(v3);
                             mode = "U";
                         } else if (first == 'B') {
                             // Neues Gebäude
                             rB = new DescParamsBuilding();
                             inDesc = true;
                             int indexL1 = zeile.indexOf(" ");
                             int indexL2 = zeile.lastIndexOf(" ");
                             String v3 = zeile.substring(indexL1 + 1, indexL2);
                             id = Integer.parseInt(v3);
                             mode = "B";
                         } else if (first == '}') {
                             // Fertig, in HashMap speichern
                             if (mode.equals("U")) {
                                 rU.setDescTypeId(id);
                                 descTypeUnit.put(id, rU.getSize() == 3 ? new Unit3x3(rU) : new Unit2x2(rU));
                                 inDesc = false;
                             } else if (mode.equals("B")) {
                                 rB.setDescTypeId(id);
                                 descTypeBuilding.put(id, new PlayersBuilding(rB));
                                 inDesc = false;
                             }
                         }
                     }
                 }
                 rgi.logger("[ServerMapModule]: ServerMapModule is ready to rock! (init completed)");
             } catch (FileNotFoundException ex) {
                 rgi.logger(ex);
                 rgi.logger("[ServerMapModule][ERROR]: Can't open descTypes " + descFile.getAbsolutePath());
             } catch (IOException ex) {
                 rgi.logger(ex);
                 rgi.logger("[ServerMapModule][ERROR]: Can't read descTypes " + descFile.getAbsolutePath());
             } finally {
                 try {
                     descReader.close();
                 } catch (IOException ex) {
                 }
             }
         }
     }
 
     public void loadMap(String mapName) {
         // Läd die angegebene Map-Datei
         rgi.logger("[MapModul] Loading map...");
         File tempMap = new File(mapName);
         if (!tempMap.exists()) {
             // Datei nicht gefunden - Fehler loggen
             System.out.println("[MapModul][ERROR]: Map not found! Map: <" + mapName + ">");
             rgi.logger("[MapModul][ERROR]: Map not found! Map: <" + mapName + ">");
             rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 11, 1, 0, 0, 0));
         } else {
             try {
                 // Checksumme gleich berechnen:
                 AbstractChecksum checksum = JacksumAPI.getChecksumInstance("adler32");
                 try {
                     checksum.readFile(mapName);
                 } catch (IOException ex) {
                     System.out.println("ICFixMe: Checksum-Error??");
                     rgi.logger("ICFixMe: Checksum-Error??");
                     return;
                 }
                 mapHash = (int) checksum.getValue();
             } catch (NoSuchAlgorithmException ex) {
                 System.out.println("CriticalFixMe: Can't calc checksum - Adler32 is not available...");
                 rgi.logger("CriticalFixMe: Can't calc checksum - Adler32 is not available...");
                 return;
             }
 
             FileInputStream stream;
             mapBuffer = new byte[(int) tempMap.length()];
             try {
                 stream = new FileInputStream(tempMap);
                 int runIndex = 0;
                 while (stream.available() > 0) {
                     mapBuffer[runIndex] = (byte) stream.read();
                     runIndex++;
                 }
             } catch (FileNotFoundException ex) {
             } catch (IOException ex2) {
                 rgi.logger(ex2);
             }
             // Jetzt Map öffnen
             theMap = MapIO.readMap(tempMap.getPath(), MapIO.MODE_SERVER, null, rgi);
             unitList = Collections.synchronizedList((ArrayList<Unit>) theMap.getMapPoperty("UNIT_LIST"));
             buildingList = Collections.synchronizedList((ArrayList<Building>) theMap.getMapPoperty("BUILDING_LIST"));
             refreshUnits();
             refreshBuildings();
             nextNetID = (Integer) theMap.getMapPoperty("NEXTNETID");
             createIDList();
             createAllLists();
             rgi.game.registerUnitList(unitList);
             moveMap = MovementMap.createMovementMap(theMap, buildingList);
             rgi.moveMan.initMoveManager(moveMap);
             rgi.logger("[MapModul] Map \"" + mapName + "\" loaded");
             // Maphash bekannt, jetzt Name + Hash an andere Clients übertragen
             System.out.println("Targethash: " + mapHash);
             rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 1, mapHash, 0, 0, 0));
             rgi.netctrl.broadcastString(mapName, (byte) 2);
 
         }
     }
 
     /**
      * Erstellt alle intern notwendigen Listen
      */
     private void createAllLists() {
         List<BehaviourProcessor> bpList = new ArrayList<BehaviourProcessor>();
         for (Unit unit : unitList) {
             bpList.add(unit);
         }
         for (Building building : buildingList) {
             bpList.add(building);
         }
         rgi.game.registerAllList(bpList);
     }
 
     public Unit getDescUnit(int playerId, int descId) {
         if (rgi.game.getPlayer(playerId).descUnit.containsKey(descId)) {
             return (Unit) rgi.game.getPlayer(playerId).descUnit.get(descId).getCopy(getNewNetID());
         } else {
             System.out.println("GetDESCError: Get unit " + descId);
             return null;
         }
     }
 
     public Building getDescBuilding(int playerId, int descId) {
         if (rgi.game.getPlayer(playerId).descBuilding.containsKey(descId)) {
             return (Building) rgi.game.getPlayer(playerId).descBuilding.get(descId).getCopy(getNewNetID());
         } else {
             System.out.println("GetDESCError: Get building " + descId);
             return null;
         }
     }
 
     private int getNewNetID() {
         nextNetID++;
         return (nextNetID - 1);
     }
 
     public int getMapSizeX() {
         return theMap.getMapSizeX();
     }
 
     public int getMapSizeY() {
         return theMap.getMapSizeY();
     }
 
     private void createIDList() {
         // Verlässt sich darauf, dass der MapEditor/RandomMapGenerator korrekte netIDs vergeben hat...
         netIDList.clear();
         for (Unit unit : unitList) {
             netIDList.put(unit.netID, unit);
         }
         for (Building building : buildingList) {
             netIDList.put(building.netID, building);
         }
     }
 
     private int saveStrtoInt(String transform, String ganzeZeile, int line) {
         // Übersetzt den String in ein int und fängt exceptions ab.
         int i;
         try {
             i = Integer.parseInt(transform);
             return i;
         } catch (java.lang.NumberFormatException ex) {
             // Ging nicht, Syntax-Fehler ins Logfile schreiben
             rgi.logger("[RogMapModule][ERROR]: Syntax-Error in line " + line + ": \"" + transform + "\" is not a valid number - Defaulting to 1");
             if (!syntaxWarned) {
                 // Anzeigen
                 //rgi.rogGraphics.displayWarning("Einer oder mehrere Syntax-Fehler in \"game/descType\" \n eventuell sind Einheiten oder Gebäude falsch! \n Mehr Informationen im Logfile.");
                 syntaxWarned = true;
             }
             return 1;
         }
     }
 
     private double saveStrtoDouble(String transform, String ganzeZeile, int line) {
         // Übersetzt den String in ein int und fängt exceptions ab.
         double i;
         try {
             i = Double.parseDouble(transform);
             return i;
         } catch (java.lang.NumberFormatException ex) {
             // Ging nicht, Syntax-Fehler ins Logfile schreiben
             rgi.logger("[RogMapModule][ERROR]: Syntax-Error in line " + line + ": \"" + transform + "\" is not a valid number - Defaulting to 1.0");
             if (!syntaxWarned) {
                 // Anzeigen
                 //rgi.rogGraphics.displayWarning("Einer oder mehrere Syntax-Fehler in \"game/descType\" \n eventuell sind Einheiten oder Gebäude falsch! \n Mehr Informationen im Logfile.");
                 syntaxWarned = true;
             }
             return 1;
         }
     }
 
     private void refreshUnits() {
         // Im Speicherformat werden einige Dinge weggelassen, jetzt wird der Rest aufgefüllt, um wieder vollständige RogUnits zu haben
         for (Unit unit : unitList) {
             unit.copyPropertiesFrom(descTypeUnit.get(unit.getDescTypeId()));
         }
     }
 
     private void refreshBuildings() {
         // Im Speicherformat werden einige Dinge weggelassen, jetzt wird der Rest aufgefüllt, um wieder vollständige RogBuildings zu haben
         for (Building building : buildingList) {
             building.copyPropertiesFrom(descTypeBuilding.get(building.getDescTypeId()));
         }
     }
 
     public boolean isValidUnitDesc(int playerId, int desc) {
         return (rgi.game.getPlayer(playerId).descUnit.get(desc) != null);
     }
 
     public boolean isValidBuildingDesc(int playerId, int desc) {
         return (rgi.game.getPlayer(playerId).descBuilding.get(desc) != null);
     }
 
     public List<Unit> getUnitList() {
         // Liefert die Einheitenliste zurück.
         return unitList;
     }
 
     public List<Building> getBuildingList() {
         // Gibt die Gebäudeliste zurück
         return buildingList;
     }
 
     /**
      * Sucht eine Einheit anhand ihrer netID.
      *
      * @param netID - Die netID der Einheit.
      * @return RogUnit, falls sie gefunden wurde, sonst null.
      */
     public Unit getUnitviaID(int netID) {
         try {
             return (Unit) netIDList.get(netID);
         } catch (Exception ex) {
             // Gibts net, falscher Typ etc...
             return null;
         }
     }
 
     /**
      * Sucht ein Gebäude anhand seiner netID.
      *
      * @param netID - Die netID des Gebäudes.
      * @return RogBiulding, falls gefunden, sonst null.
      */
     public Building getBuildingviaID(int netID) {
         try {
             return (Building) netIDList.get(netID);
         } catch (Exception ex) {
             // Gibts net, falscher Typ etc...
             return null;
         }
     }
 
     /**
      * Sucht ein GameObject anhand seiner netID.
      *
      * @param netID - Die netID des GameObjects.
      * @return RogGameObjekt, falls sie gefunden wurde, sonst null.
      */
     public GameObject getGameObjectviaID(int netID) {
         try {
             return netIDList.get(netID);
         } catch (Exception ex) {
             // Gibts net, falscher Typ etc...
             return null;
         }
     }
 
     /**
      * Fügt ein Gebäude zum Spiel hinzu.
      * Darf nur vom NetController aufgerufen werden!
      * @param b Das neue Gebäude
      */
     public void addBuildingAsSite(Building b) {
         this.buildingList.add(b);
 
         this.netIDList.put(b.netID, b);
 
         rgi.game.addGO(b);
 
         // In Abhängigkeitsliste einfügen
         if (b.getLifeStatus() == GameObject.LIFESTATUS_ALIVE) {
             if (!rgi.game.playerList.get(b.getPlayerId()).bList.contains(b.getDescTypeId())) {
                 rgi.game.playerList.get(b.getPlayerId()).bList.add(b.getDescTypeId());
             }
         }
 
         // Broadcasten
 
         rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 18, b.netID, b.getDescTypeId(), b.getMainPosition().getX(), b.getMainPosition().getY()));
         rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 19, b.netID, b.getPlayerId(), b.getHitpoints(), 0));
     }
 
     /**
      * Fügt eine Einheit zum Spiel hinzu
      * Sendet die Infos auch an die Clients
      * @param u Die neue Einheit
      */
     public void addUnit(Unit u) {
         // Diese Einheit für das Bewegungssystem fit machen
         u.initServerMovementManagers(rgi, moveMap);
 
         this.unitList.add(u);
 
         this.netIDList.put(u.netID, u);
         rgi.game.addGO(u);
 
         // Abhängigkeiten
         if (!rgi.game.playerList.get(u.getPlayerId()).uList.contains(u.getDescTypeId())) {
             rgi.game.playerList.get(u.getPlayerId()).uList.add(u.getDescTypeId());
         }
        
        registerUnitMovements(u);
 
         // Broadcasten
 
         rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 21, u.netID, u.getDescTypeId(), u.getMainPosition().getX(), u.getMainPosition().getY()));
         rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 19, u.netID, u.getPlayerId(), u.getHitpoints(), 0));
     }
 
     /**
      *  Entfernt die Einheit aus dem Spiel und spielt vorher noch eine Todesanimation ab.
      * @param u
      */
     public void killUnit(Unit u) {
         if (u != null && u.getLifeStatus() == GameObject.LIFESTATUS_ALIVE) {
             // Allen mitteilen
             rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 28, u.netID, 0, 0, 0));
 
             // Unit löschen
             this.unitList.remove(u);
             this.netIDList.remove(u.netID);
             rgi.game.removeGO(u);
            
             moveMap.removeMoveable(u);
         }
     }
 
     public void killUnit(Unit u, int killer) {
         if (u != null && u.getLifeStatus() == GameObject.LIFESTATUS_ALIVE) {
             rgi.serverstats.trackUnitkill(killer, u.getPlayerId()); //Kill in Statistik eintragen
             killUnit(u);
         }
     }
 
     /**
      *  Entfernt die Einheit aus dem Spiel und spielt vorher noch eine Todesanimation ab.
      * @param u
      */
     public void killBuilding(Building u) {
         if (u != null && u.getLifeStatus() == GameObject.LIFESTATUS_ALIVE) {
             // Behaviours sofort stoppen
             // Alle enthaltenen Einheiten rauslassen
             u.removeAll(rgi);
 
             // Allen mitteilen
             rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 30, u.netID, 0, 0, 0));
 
             // Unit löschen
             this.buildingList.remove(u);
             this.netIDList.remove(u.netID);
             rgi.game.removeGO(u);
 
             // Sieg/Niederlage testen
             checkFinished(u.getPlayerId());
         }
     }
 
     public void killBuilding(Building b, int killer) {
         if (b != null && b.getLifeStatus() == GameObject.LIFESTATUS_ALIVE) {
             rgi.serverstats.trackBuildingkill(killer, b.getPlayerId());
             killBuilding(b);
         }
     }
    
     /**
      * Initialisiert diese Einheit für das neue Server-Bewegunssystem
      * @param unit 
      */
     public void registerUnitMovements(Unit unit) {
         moveMap.registerMoveable(unit);
     }
 
     /**
      * Überprüft, ob ein Spieler keine Gebäude mehr hat (und daher verliert)
      * Wenn nurnoch ein Spieler übrig ist, hat dieser gewonnen
      *
      */
     private void checkFinished(int playerId) {
         boolean bleft = false;
         for (int i = 0; i < buildingList.size(); i++) {
             Building b = buildingList.get(i);
             if (b.getPlayerId() == playerId && b.getLifeStatus() == GameObject.LIFESTATUS_ALIVE) {
                 bleft = true;
                 break;
             }
         }
         if (!bleft) {
             // Dieser Spieler ist fertig
             rgi.game.getPlayer(playerId).setFinished(true);
             // Alle Einheiten dieses Spielers entfernen
             for (int i = 0; i < rgi.netmap.unitList.size(); i++) {
                 Unit b = unitList.get(i);
                 if (b.getPlayerId() == playerId) {
                     this.killUnit(b);
                     rgi.serverstats.trackUnitdelete(playerId);
                     i--;
                 }
             }
             // Wie viele spielen noch?
             int playerLeft = 0;
             int lastPlayerId = 0;
             for (int p = 0; p < rgi.game.playerList.size(); p++) {
                 NetPlayer player = rgi.game.getPlayer(p);
                 if (!player.isFinished()) {
                     playerLeft++;
                     lastPlayerId = player.playerId;
                 }
             }
             if (playerLeft > 1) {
                 // Den Clients mitteilen
                 rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 12, playerId, 1, 0, 0));
                 // Dieser Spieler hat verloren, aber das Spiel ist noch nicht aus, er soll also specen
             } else if (playerLeft == 1) {
                 // Ein anderer hat gewonnen (daraus schließen die andern Clients, dass sie verloren haben)
                 rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 12, lastPlayerId, 2, 0, 0));
 
                 // Server-Statistiken an alle Clients senden
                 for (int player = 1; player < rgi.game.playerList.size(); player++) { // Für jeden Spieler
                     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 46, 6, rgi.serverstats.unitsrecruited[player], player, 0));
                     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 46, 7, rgi.serverstats.unitskilled[player], player, 0));
                     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 46, 8, rgi.serverstats.unitslost[player], player, 0));
                     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 46, 9, rgi.serverstats.buildingsbuilt[player], player, 0));
                     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 46, 10, rgi.serverstats.buildingskilled[player], player, 0));
                     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 46, 11, rgi.serverstats.buildingslost[player], player, 0));
                 }
             }
         }
     }
 
     /**
      * Löscht alle Einheiten & Gebäude eines Spielers
      * @param playerId
      */
     public void playerLeft(int playerId) {
         // Alle Einheiten dieses Spielers entfernen - fall das Spiel noch läuft
         for (int i = 0; i < rgi.netmap.unitList.size(); i++) {
             Unit b = unitList.get(i);
             if (b.getPlayerId() == playerId) {
                 this.killUnit(b);
                 rgi.serverstats.trackUnitdelete(playerId);
                 i--;
             }
         }
         for (int i = 0; i < rgi.netmap.buildingList.size(); i++) {
             Building b = buildingList.get(i);
             if (b.getPlayerId() == playerId) {
                 this.killBuilding(b);
                 i--;
             }
         }
     }
    
     /**
      * Verwaltet komplexere toDESC-Upgrades
      *
      * @param mode old=2, new=3, all=4
      * @param caster Der dieses Upgrade ausführt
      * @param fromDesc Von welcher Desc...
      * @param toDesc ... auf welche upgraden?
      */
     public void manageComplexUpgrades(int mode, GameObject caster, int fromDesc, int toDesc) {
         boolean units = false;
         // Änderungen suchen:
         if (fromDesc < 0) {
             // Negative Werte sind Einheiten
             fromDesc *= -1;
             units = true;
         }
 
         // Wer denn alles?
         if (mode == 2) {
             // Vorhandene
             if (units) {
                 for (Unit unit : unitList) {
                     if (unit.getDescTypeId() == fromDesc) {
                         unit.performUpgrade(rgi, toDesc);
                     }
                 }
             } else {
                 for (Building building : buildingList) {
                     if (building.getDescTypeId() == fromDesc) {
                         building.performUpgrade(rgi, toDesc);
                     }
                 }
             }
         } else if (mode == 3) {
             // Zukünftige
             if (units) {
                 rgi.game.getPlayer(caster.getPlayerId()).descUnit.put(fromDesc, rgi.game.getPlayer(caster.getPlayerId()).descUnit.get(toDesc));
             } else {
                 rgi.game.getPlayer(caster.getPlayerId()).descBuilding.put(fromDesc, rgi.game.getPlayer(caster.getPlayerId()).descBuilding.get(toDesc));
             }
         } else if (mode == 4) {
             // Alle
             if (units) {
                 rgi.game.getPlayer(caster.getPlayerId()).descUnit.put(fromDesc, rgi.game.getPlayer(caster.getPlayerId()).descUnit.get(toDesc));
             } else {
                 rgi.game.getPlayer(caster.getPlayerId()).descBuilding.put(fromDesc, rgi.game.getPlayer(caster.getPlayerId()).descBuilding.get(toDesc));
             }
             if (units) {
                 for (Unit unit : unitList) {
                     if (unit.getDescTypeId() == fromDesc) {
                         unit.performUpgrade(rgi, toDesc);
                     }
                 }
             } else {
                 for (Building building : buildingList) {
                     if (building.getDescTypeId() == fromDesc) {
                         building.performUpgrade(rgi, toDesc);
                     }
 
                 }
             }
         }
     }
    
     /**
      * Getter für die MoveMap
      */
     public MovementMap getMoveMap() {
         return moveMap;
     }
 }
