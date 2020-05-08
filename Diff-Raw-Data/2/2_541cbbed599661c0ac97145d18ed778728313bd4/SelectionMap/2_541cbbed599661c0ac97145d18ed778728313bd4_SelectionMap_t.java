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
 package de._13ducks.cor.graphics.input;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Die SelektionsMap des Clients.
  * Im Prinzip ein großes Array mit beliebig vielen Einträgen pro Feld.
  * @author tfg
  */
 public class SelectionMap {
 
     /**
      * Die eigentliche Map.
      */
     private LinkedList<InteractableGameElement>[][] map;
 
     /**
      * Erstellt eine neue SelectionMap
      */
     public SelectionMap(int dimX, int dimY) {
         map = new LinkedList[dimX][dimY];
     }
 
     /**
      * Trägt ein IGE an der gegebenen Position in die Map ein.
      * Sorgt NICHT (!) dafür, dass das IGE nicht mehrfach eingetragen wird.
      * @param x die X-Koordinate
      * @param y die Y-Koordinate
      * @param ige das IGE, das eingetragen werden soll
      */
     public synchronized void addIGE(int x, int y, InteractableGameElement ige) {
         if (x < 0 || x > map.length) {
             System.out.println("WARN: Setting IGE for invalid Field! (X=" + x + ")");
             return;
         }
         if (y < 0 || y > map[0].length) {
             System.out.println("WARN: Setting IGE for invalid Field! (Y=" + y + ")");
             return;
         }
         LinkedList<InteractableGameElement> list = map[x][y];
         if (list == null) {
             list = new LinkedList<InteractableGameElement>();
             map[x][y] = list;
         }
         list.add(ige);
     }
 
     /**
      * Entfernt einen IGE-Eintrag des gegebenen Elements an der der gegebenen Position aus der Map.
      * Achtung! Entfernt nur EINEN Eintrag. Muss mehrfach aufgerufen werden, sollten mehrere da sein.
      * Tut nichts, wenn gar kein Eintrag vorhanden ist.
      * @param x die X-Koordinate
      * @param y die Y-Koordinate
      * @param ige das IGE, das entfernt werden soll.
      */
     public synchronized void removeIGE(int x, int y, InteractableGameElement ige) {
         if (x < 0 || x > map.length) {
             System.out.println("WARN: Removing IGE for invalid Field! (X=" + x + ")");
             return;
         }
         if (y < 0 || y > map[0].length) {
             System.out.println("WARN: Removing IGE for invalid Field! (Y=" + y + ")");
             return;
         }
         LinkedList<InteractableGameElement> list = map[x][y];
         if (list != null) {
             list.remove(ige);
         }
     }
 
     /**
      * Liefert alle IGE's an dieser Stelle, die vom angegebenen Team sind.
      * @param cx die X-Koordinate
      * @param cy die Y-Koordinate
      * @param playerId die PlayerId der gesuchten Einheiten
      * @return alle IGE's an der angegebenen Stelle, die vom angegebenen Team sind.
      */
     public synchronized List<InteractableGameElement> getIGEsWithTeamAt(int cx, int cy, int playerId) {
         if (cx < 0 || cx > map.length) {
             System.out.println("WARN: Getting all IGEs (team) for invalid Field! (X=" + cx + ")");
             return null;
         }
         if (cy < 0 || cy > map[0].length) {
             System.out.println("WARN: Getting all IGEs (team) for invalid Field! (Y=" + cy + ")");
             return null;
         }
         // Alle holen
         LinkedList<InteractableGameElement> found = new LinkedList<InteractableGameElement>();
         LinkedList<InteractableGameElement> list = map[cx][cy];
         if (list != null && !list.isEmpty()) {
             for (InteractableGameElement elem : list) {
                 if (elem.isSelectableByPlayer(playerId)) {
                     found.add(elem);
                 }
             }
         }
         return found;
     }
 
     /**
      * Liefert alle IGE's an dieser Stelle
      * @param cx Die X-Koordinate
      * @param cy Die Y-Koordinate
      * @return Eine Liste mit allen Elementen aller teams, die an dieser Stelle eingetragen sind.
      */
     public synchronized List<InteractableGameElement> getIGEsAt(int cx, int cy) {
         if (cx < 0 || cx > map.length) {
             System.out.println("WARN: Getting IGEs for invalid Field! (X=" + cx + ")");
             return null;
         }
         if (cy < 0 || cy > map[0].length) {
             System.out.println("WARN: Getting IGEs for invalid Field! (Y=" + cy + ")");
             return null;
         }
         // Alle holen
         LinkedList<InteractableGameElement> list = map[cx][cy];
         if (list != null) {
            return (List<InteractableGameElement>) list.clone();
         } else {
             return new LinkedList<InteractableGameElement>();
         }
     }
 
     /**
      * Löscht die komplette Selektionsmap
      * Stellt den Ausgangszustand nach dem Erzeugen durch den Konstruktor wieder her.
      */
     public synchronized void clear() {
         map = new LinkedList[map.length][map[0].length];
     }
 }
