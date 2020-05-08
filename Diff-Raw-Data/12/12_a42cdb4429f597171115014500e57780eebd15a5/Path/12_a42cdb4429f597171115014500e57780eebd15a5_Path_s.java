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
 package de._13ducks.cor.game;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import de._13ducks.cor.game.client.ClientCore;
 import de._13ducks.cor.game.server.ServerCore;
 import de._13ducks.cor.game.server.ServerCore.InnerServer;
 import de._13ducks.cor.graphics.GraphicsContent;
 
 /**
  * Ein Weg ist eine Folge von Feldern, die eine Einheit entlang laufen kann.
  * Enthält Variablen und Mechanismen, um die aktuelle Position der Einheit auf dem Weg zu bestimmen/speichern
  */
 public class Path implements Pauseable, Serializable {
 
     /**
      * Die Position auf der die Bewegung begonnen hat.
      */
     private Position startPos;
     /**
      * Die Position auf der die Bewegung enden soll.
      */
     private Position targetPos;
     /**
      * Der Zeitpunkt, zu dem die Einheit losgelaufen ist.
      */
     private long moveStartTime;
     /**
      * Die Weglänge - Echte Einheiten.
      */
     private double length;
     /**
      * Wurde die Bewegung pausiert? (Pause/Speichermodus)
      */
     private boolean movePaused;
     /**
      * Der Zeitpunkt des Pausierens
      */
     private long movePauseTime;
     /**
      * Wurde die Weglänge bereits berechnet?
      * Wenn true steht auch path zur Verfügung
      */
     private boolean pathComputed;
     /**
      * Die Einheit soll am Ende des Weges in ein Gebäude springen
      */
     private int jumpTo = 0;
     /**
      * Wurde die Sprunganweisung eben erst gesetzt?
      * Die Sprunganweisung überlebt genau einen Bewegungsbefehl, dann wird sie gelöscht.
      */
     private boolean jumpJustSet = false;
     /**
      * Der Index des zuletzt erreichten Wegpunktes
      */
     private int lastWayPoint;
     /**
      * Die Strecke bis zum nächsten Wegpunkt
      */
     private double nextWayPointDist;
     /**
      * Der eigentliche Weg.
      * Muss erst berechnet werden, steht zur Verfügung, wenn pathLengthCalced = true
      */
     private List<PathElement> path;
     /**
      * Der Weg der Grafikengine. Berechnet Feldersprünge immer bei Feldern, nicht bei halben.
      */
     private List<PathElement> gPath;
     /**
      * True, während sich die Einheit bewegt, also das Behaviour laufen soll.
      */
     private boolean moving;
     /**
      * Die Laufgeschwindigkeit in Feldern/Sekunde.
      */
     private double speed;
     /**
      * Grafiksystem - Deltaberechnung.
      */
     private double gNextPointDist;
     /**
      * Grafiksystem - Deltaberechnung.
      */
     private int gLastPointIdx;
 
     /**
      * Erzeugt einen neuen Pfad mit der gegebenen Geschwindigkeit.
      * Sollte sich diese einmal ändern, muss der Path darüber benachrichtigt werden.
      * @param speed die Geschwindigkeite in Feldern/Sekunde
      */
     public Path(double speed) {
         this.speed = speed;
     }
 
     /**
      * Berechnet die Länge des Weges dieser Einheit.
      * Die berechnete Weglänge wird in length gespeichert.
      */
     private synchronized void computePath(List<Position> newPath) {
         pathComputed = false;
         length = 0;
         path = new ArrayList<PathElement>();
         gPath = new ArrayList<PathElement>();
         path.add(new PathElement(newPath.get(0)));
         gPath.add(new PathElement(newPath.get(0)));
         double lasthalf = 0; // Neu: Das neue Feld wird jetzt schon bei der Hälfte eingestellt. Dies speichert die halben.
         for (int i = 1; i < newPath.size(); i++) {
             Position pos = newPath.get(i);
             Position old = newPath.get(i - 1);
             // Richtung berechnen
             int vec = pos.subtract(old).transformToIntVector();
             // Strecke berechnen, mit Pytagoras
             double abschnitt = Math.sqrt(Math.pow(old.getX() - pos.getX(), 2) + Math.pow(old.getY() - pos.getY(), 2)) / 2;
             length = length + abschnitt + lasthalf;
             lasthalf = abschnitt;
             path.add(new PathElement(pos, length, vec));
             gPath.add(new PathElement(pos, length + lasthalf, vec));
         }
         // Letztes halbes noch adden
         length += lasthalf;
         pathComputed = true;
         moving = true;
     }
 
     /**
      * @return the startPos
      */
     public Position getStartPos() {
         return startPos;
     }
 
     /**
      * @return the targetPos
      */
     public Position getTargetPos() {
         return targetPos;
     }
 
     /**
      * Überschreibt den Pfad und lässt die Einheit vom Beginn loslaufen
      * Unit und rgi müssen für Server gesetzt sein, für Client sind die null.
      * @param newPath der neue Weg
      */
     public synchronized void overwritePath(List<Position> newPath, Unit unit, ServerCore.InnerServer rgi) {
         // Eventuell alte Reservierung löschen
         if (isMoving() && rgi != null) {
             rgi.netmap.deleteMoveTargetReservation(unit, targetPos);
         }
         startPos = newPath.get(0);
         targetPos = newPath.get(newPath.size() - 1);
         lastWayPoint = 0;
         gLastPointIdx = 0;
         computePath(newPath);
         this.nextWayPointDist = path.get(1).getDistance();
         this.gNextPointDist = gPath.get(1).getDistance();
         moveStartTime = System.currentTimeMillis();
         if (rgi != null) {
             rgi.netmap.reserveMoveTarget(unit, System.currentTimeMillis() + (long) (1000.0 * length / this.speed), newPath.get(newPath.size() - 1));
         }
     }
 
     /**
      * Versucht, den Weg "seamless" zu ändern.
      * Da diese Änderung während der Bewegung erfolgt, kann es sein, dass die Einheit noch bis zum nächsten Feld weiter läuft
      * @param newPath
      */
     public synchronized void switchPath(ArrayList<Position> newPath) {
         targetPos = newPath.get(newPath.size() - 1);
         computePath(newPath);
         try {
             if (lastWayPoint + 1 > path.size() - 1) {
                 // Automatisch zurückstellen, falls Überschreitung festgestellt.
                 lastWayPoint = path.size() - 2;
                 gLastPointIdx = lastWayPoint;
                 System.out.println("WARNING: Setting unit back on Path-Switch, probably causes Pfusch");
             }
         } catch (Exception ex) {
             System.out.println("ERROR: Problems switching Path:");
             ex.printStackTrace();
         }
         nextWayPointDist = path.get(lastWayPoint + 1).getDistance();
         gNextPointDist = nextWayPointDist;
     }
 
     @Override
     public void pause() {
         movePaused = true;
         movePauseTime = System.currentTimeMillis();
     }
 
     @Override
     public void unpause() {
         movePaused = false;
         moveStartTime = System.currentTimeMillis() - (movePauseTime - moveStartTime);
     }
 
     /**
      * Verwaltet die Bewegung der Unit auf dem Path.
      * Muss regelmäßig aufgerufen werden
      * @param caster2 Die Einheit, deren Pfad verwaltet wird.
      */
     public synchronized void clientManagePath(ClientCore.InnerClient rgi, Unit caster2) { // ugly
         if (isMoving()) {
             long passedTime = 0;
             if (movePaused) {
                 passedTime = movePauseTime - moveStartTime;
             } else {
                 passedTime = System.currentTimeMillis() - moveStartTime;
             }
             double passedWay = passedTime * caster2.speed / 1000;
             // Schon fertig?
             if (passedWay >= length) {
                 // Fertig, Bewegung stoppen
                 caster2.setMainPosition(targetPos);
                 targetPos = null;
                 gPath = null;
                 moving = false;
                 return;
             }
             // Zuletzt erreichten Wegpunkt finden
             if (passedWay >= nextWayPointDist) {
                 // Sind wir einen weiter oder mehrere
                 int weiter = 0;
                 while (passedWay > gPath.get(lastWayPoint + 1 + weiter).getDistance()) {
                     weiter++;
                     if (gPath.size() == lastWayPoint + 1 + weiter) {
                         // Sonderfall - nach dem letzten halben Feld, vor dem Ziel
                         nextWayPointDist = length;
                         caster2.setMainPosition(gPath.get(gPath.size() - 1).getPos());
                         return;
                     }
                 }
                 lastWayPoint += weiter;
                 nextWayPointDist = gPath.get(lastWayPoint + 1).getDistance();
                 caster2.setMainPosition(gPath.get(lastWayPoint).getPos());
             }
         }
     }
 
     /**
      * Verwaltet den Pfad Serverseitig (Server-Behaviour)
      * Tut nichts, wenn sich die Einheit gerade nicht bewegt.
      * @param rgi
      * @param caster2
      */
     public synchronized void serverManagePath(InnerServer rgi, Unit caster2) {
         if (isMoving()) {
             boolean gotError = false;
             int trys = 0;
 
             do {
                 trys++;
                 gotError = false;
                 try {
                     long passedTime = 0;
                     if (movePaused) {
                         passedTime = movePauseTime - moveStartTime;
                     } else {
                         passedTime = System.currentTimeMillis() - moveStartTime;
                     }
                     double passedWay = passedTime * caster2.speed / 1000;
                     // Schon fertig?
                     if (passedWay >= length) {
                         // Fertig, Bewegung stoppen
                         rgi.netmap.setPosition(caster2, targetPos);
                         targetPos = null;
                         path = null;
                         //caster2.attackManager.moveStopped();
                         moving = false;
                         return;
                     }
                     // Zuletzt erreichten Wegpunkt finden
                     if (passedWay >= nextWayPointDist) {
                         // Sind wir einen weiter oder mehrere
                         int weiter = 0;
                         while (passedWay > path.get(lastWayPoint + 1 + weiter).getDistance()) {
                             weiter++;
                             if (path.size() == lastWayPoint + 1 + weiter) {
                                 // Sonderfall - nach dem letzten halben Feld, vor dem Ziel
                                 nextWayPointDist = length;
                                 rgi.netmap.changePosition(caster2, path.get(path.size() - 1).getPos());
                                 return;
                             }
                         }
                         lastWayPoint += weiter;
                         nextWayPointDist = path.get(lastWayPoint + 1).getDistance();
                         rgi.netmap.changePosition(caster2, path.get(lastWayPoint).getPos());
 
                         // Ziel in Reichweite? (für Anhalten)
                         // Neuer Wegpunkt! - Berechnungen durchführen:
 
                         // Ziel nichtmehr frei?
 
                         /*          if (rgi.netmap.isGroundColliding(caster2.movingtarget) || (!reservedTarget && rgi.netmap.checkFieldReservation(caster2.movingtarget))) {
                         if (caster2.jumpTo == 0) { // Nicht bei jumps
                         // Weg zu neuem Ziel berechnen
                         RogPosition pos = caster2.movingtarget.aroundMe(1, rgi);
                         // Reservierung nur löschen, wenn wir die selber schon eingetragen haben
                         if (reservedTarget) {
                         rgi.netmap.deleteFieldReservation(caster2.movingtarget);
                         reservedTarget = false;
                         }
                         boolean ret = false;
                         if (caster2.position.getDistance(pos) < 100) {
                         ret = caster2.moveToPosition(pos, rgi, true);
                         } else {
                         ret = caster2.moveToPosition(pos, rgi, false);
                         }
                         if (!ret) {
                         // Das Ausweichen ging nicht, die Einheit ist ziemlich "gelockt".
                         // Versuche die Einheit da wo sie derzeit ist anzuhalten
                         System.out.println("FixMe: Units target blocked, but can't change path! Trying to stop...");
                         boolean ret2 = caster2.moveToPosition(caster2.position.aroundMe(0, rgi), rgi, true);
                         if (!ret2) {
                         // Da hilft nix mehr...
                         System.out.println("FixMe: Unit can't move anywhere, this may result in 2 units on one field - SRY!");
                         }
                         }
                         }
                         } */
 
                         // Einheit noch nicht am Ziel angekommen - übernächstes Feld frei?
                         // Fürt zu sinnlosem rumgerenne - son mist
                         // (Nächstes braucht man nicht zu prüfen, da rennen wir ja eh schon hin...
 
                         /*if (caster2.lastwaypoint < caster2.path.size() - 3) {
                         // Feld prüfen
                         if (rgi.netmap.isGroundColliding(caster2.path.get(caster2.lastwaypoint + 2))) {
                         // Blockiert, Weg neu suchen
                         //System.out.println(System.currentTimeMillis() + ": Way to " + caster2.movingtarget + " is blocked, re-calcing...");
                         //caster2.moveToPosition(caster2.movingtarget, rgi);
                         }
                         } */
 
                     }
                 } catch (Exception ex) {
                     // Fehler, mit Ziemlicher Sicherheit wurde was von nem anderen Thread geändert, während dieser durchlief.
                     // Darum einfach noch mal versuchen
                     if (trys < 2) {
                         gotError = true;
                         System.out.println("Error in SMoveBehaviour, trying again...");
                         rgi.logger(ex);
                     } else {
                         gotError = false;
                         System.out.println("Critical: 2nd try in SMoveBehaviour didn't help.");
                     }
                 }
             } while (gotError);
         }
     }
 
     /**
      * True, während sich die Einheit bewegt, also das Behaviour laufen soll.
      * @return the moving
      */
     private boolean isMoving() {
         return moving;
     }
 
     /**
      * Berechnet die exakte Position der Einheit und berechnet die Zeichenkoordinaten.
      * Dazu werden x und y - die Zeichenkoordinaten des Zuordnungsfeldes benötigt.
      * Diese Methode liefert die gegebenen Werte zurück, falls gerade keine Bewegung läuft.
      * @param x Koordinate des letzten Zuordnungsfeldes
      * @param y Koordinate des letzten Zuordnungsfeldes
      * @return x und y die korrekten Zeichenkoordinaten.
      */
     public synchronized float[] calcExcactPosition(float x, float y, Unit caster2) {
         if (isMoving()) {
             // Berechnung notwendig:
             Position zPos = gPath.get(lastWayPoint).getPos(); // Wichtig: Behaviour-Variable, nicht die der Grafik!
             // Default-Berechnung:
             try {
                 long passedTime = 0;
                 if (movePaused) {
                     passedTime = movePauseTime - moveStartTime;
                 } else {
                     passedTime = System.currentTimeMillis() - moveStartTime;
                 }
                 double passedWay = passedTime * speed / 1000;
                 // Noch am laufen?
                 if (passedWay <= length) {
                     // Zuletzt erreichten Wegpunkt finden
                     if (passedWay > this.gNextPointDist) {
                         // Sind wir einen weiter oder mehrere
                         int weiter = 1;
                         while (passedWay > gPath.get(gLastPointIdx + 1 + weiter).getDistance()) {
                             weiter++;
                         }
                         gLastPointIdx += weiter;
                         gNextPointDist = gPath.get(gLastPointIdx + 1).getDistance();
                     }
                     // In ganz seltenen Fällen ist hier lastwaypoint zu hoch (vermutlich (tfg) ein multithreading-bug)
                     // Daher erst checken und ggf. reduzieren:
                     if (gLastPointIdx >= gPath.size() - 1) {
                         System.out.println("Client: Lastwaypoint-Error, setting back. May causes jumps!?");
                         gLastPointIdx = gPath.size() - 2;
                     }
                     double diffLength = passedWay - gPath.get(gLastPointIdx).getDistance();
                     // Wir haben jetzt den letzten Punkt der Route, der bereits erreicht wurde, und die Strecke, die danach noch gefahren wurde...
                     // Jetzt noch die Richtung
                     int diffX = gPath.get(gLastPointIdx + 1).getPos().getX() - gPath.get(gLastPointIdx).getPos().getX();
                     int diffY = gPath.get(gLastPointIdx + 1).getPos().getY() - gPath.get(gLastPointIdx).getPos().getY();
                     // Prozentanteil der Stecke, die zurückgelegt wurde
                     double potPathWay = Math.sqrt(Math.pow(Math.abs(diffX), 2) + Math.pow(Math.abs(diffY), 2));
                     double faktor = diffLength / potPathWay * 100;
                     double lDiffX = diffX * faktor / 10; //    / 100 * 10
                     double lDiffY = diffY * faktor / 100 * 7.5;
                     // Eventuell ist die gegebene x und y Zuordungsposition schlecht - prüfen
                     Position pdiff = zPos.subtract(gPath.get(gLastPointIdx).getPos());
                     // Aktuelle Koordinaten reinrechnen:
                     x += lDiffX - (pdiff.getX() * GraphicsContent.FIELD_HALF_X);
                     y += lDiffY - (pdiff.getY() * GraphicsContent.FIELD_HALF_Y);
                 } else {
                     // Sonderfall, wir laufen nichtmehr, aber das Behaviour hat das noch nicht umgestellt.
                     // Unterschied berechnen:
                     Position diff = gPath.get(lastWayPoint).getPos().subtract(targetPos); // Wichtig: Behaviour-Variable, nicht Grafik!
                     x -= (diff.getX() * GraphicsContent.FIELD_HALF_X);
                     y -= (diff.getY() * GraphicsContent.FIELD_HALF_Y);
                 }
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
 
         // Egal ob was reingerechnet wurde, oder nicht:
         return new float[]{x, y};
     }
 
     /**
      * Versucht, die Einheit sofort zu stoppen.
      * Client only!
      */
     public void stopMovement(ClientCore.InnerClient rgi, Unit caster) {
         // Signal an den Server senden, dass muss der machen.
         rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 54, caster.netID, 0, 0, 0));
     }
 
     /**
      * Findet heraus, ob sich die Einheit derzeit Bewegt (also stoppbar ist)
      * @return
      */
     public boolean moveStoppable() {
         return this.isMoving();
     }
 
     /**
      * Liefert die exakte Länge des derzeitigen Weges.
      * Wichtig fürs Reservierungssystem
      * @return die exakte Länge des derzeitigen Weges.
      */
     public double getLength() {
         return length;
     }
 
     /**
      * Biegt den Streckenverlauf so um, dass die Einheit so schnell wie möglich hält.
      * 1. Sofern das Wunschfeld erreichbar ist gibt es zwei Möglichkeiten:
      * 1.1. Die Einheit befindet sich direkt vor einem Feld: Sie läuft grad noch schnell weiter.
      * 1.2. Die Einheit befindet sich direkt nach einem Feld: Sie kehrt um.
      * 2. Wenn das Wunschfeld nicht erreichbar ist, wird ein anderes Feld in der Nähe gesucht und die Einheit dorthin umgeleitet,
      *    falls der Weg dort hin kürzer ist als bis zu dem ursprünglichen Ziel
      * @param rgi
      * @param unit
      */
     public synchronized void stopMovement(InnerServer rgi, Unit unit) {
         System.out.println("THR" + Thread.currentThread().getName());
         if (isMoving()) {
            System.out.println("Stop for " + unit);
             // Optimalen Stop-Punkt berechnen:
             double passedTime = System.currentTimeMillis() - moveStartTime;
             double passedWay = passedTime * unit.getSpeed() / 1000 - path.get(lastWayPoint).getDistance();
             double wayDist = nextWayPointDist - path.get(lastWayPoint).getDistance();
             boolean manualMod = false;
             if (passedWay > wayDist / 2) {
                 // Nach dem Feld: Zurück
                 Position target = path.get(lastWayPoint).getPos();
                 if (Position.checkCol(target.getX(), target.getY(), unit, rgi, Position.AROUNDME_COLMODE_GROUNDTARGET, true)) {
                     // Leider nix, andere suchen
                     rgi.moveMan.humanSingleMove(unit, target.aroundMePlus(null, unit, false, 0, Position.AROUNDME_CIRCMODE_FULL_CIRCLE, Position.AROUNDME_COLMODE_GROUNDTARGET, true, rgi), false);
                 } else {
                     // Frei, also die nehmen!
                     path = path.subList(0, lastWayPoint + 1);
                     PathElement last = path.get(path.size() - 1);
                     // Verbessertes Einfügen, die Distanz stimmt für ein sofortiges umkehren exakt
                     path.add(new PathElement(target, last.getDistance() + (passedWay * 2), Position.flipIntVector(last.getDirection())));
                     manualMod = true;
                     // Client mitteilen
                     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 55, unit.netID, lastWayPoint, target.getX(), target.getY()));
                 }
             } else {
                 // Vor dem Feld, auf dem nächsten halten
                 Position target = path.get(lastWayPoint).getPos();
                 if (Position.checkCol(target.getX(), target.getY(), unit, rgi, Position.AROUNDME_COLMODE_GROUNDTARGET, true)) {
                     // Leider nix, andere suchen
                     rgi.moveMan.humanSingleMove(unit, target.aroundMePlus(null, unit, false, 0, Position.AROUNDME_CIRCMODE_FULL_CIRCLE, Position.AROUNDME_COLMODE_GROUNDTARGET, true, rgi), false);
                 } else {
                    path = path.subList(0, lastWayPoint + 1);
                     manualMod = true;
                     // Client mitteilen
                     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 55, unit.netID, lastWayPoint, target.getX(), target.getY()));
                 }
             }
             if (manualMod) {
                 // Alte Zielreservierung löschen
                 rgi.netmap.deleteMoveTargetReservation(unit, targetPos);
                 // Neues Ziel einstellen
                 length = path.get(path.size() - 1).getDistance();
                 targetPos = path.get(path.size() - 1).getPos();
                 // Neues Ziel reservieren
                 long until = (long) ((1000 * length / speed) + moveStartTime);
                 rgi.netmap.reserveMoveTarget(unit, until, targetPos);
             }
         }
     }
 
     /**
      * Versucht die Position am angegebenen Index sehr schnell zu erreichen.
      * Geht nur, wenn die Einheit gerade stark in der Nähe ist.
      * @param readInt
      * @param readPosition
      */
     public synchronized void quickStop(int readInt, Position readPosition, Unit unit) {
         if (isMoving()) {
             // Die Zielposition ist ein Teil des Pfades und nicht mit dem Server verhandelbar.
             // 2 Mögliche Fälle: Wir sind noch davor, dann einfach Weg umbiegen.
             // Wenn wir schon danach sind zurück gehen
             if (gPath.get(gLastPointIdx).getPos().equals(readPosition)) {
                 // Zurück
                 // Das ist kompliziert. Die letzten zwei Felder werden getauscht, damit die Einheit zurück geht.
                 // Außerdem wird die Wegstrecke manipuliert, damit es keine Sprünge gibt
                 // Aber erstmal alles nach dem nächsten löschen
                 gPath = gPath.subList(0, gLastPointIdx + 2);
                 PathElement last = gPath.remove(gPath.size() - 1);
                 PathElement prelast = gPath.remove(gPath.size() - 1);
                 gPath.add(new PathElement(last.getPos(), prelast.getDistance(), 0)); // Vector egal
                 // Die derzeitige Weglänge kann nicht verändert werden, deshalb muss die Startzeit verschoben werden.
                 long time = System.currentTimeMillis();
                 // Bisherige Strecke berechnen und Startzeit umgekehrt verändern
                 double passedWay = ((time - moveStartTime) * speed / 1000) - prelast.getDistance();
                 gPath.add(new PathElement(prelast.getPos(), prelast.getDistance() + (2 * passedWay), Position.flipIntVector(last.getDirection())));
                 double moveWay = gPath.get(gPath.size() - 1).getDistance() - gPath.get(gPath.size() - 2).getDistance() - passedWay;
                 //moveStartTime = (long) (-1000 * (passedWay + prelast.getDistance()) / speed + time);
                 moveStartTime = (long) (time - ((passedWay + prelast.getDistance()) * 1000 / speed));
                 unit.setMainPosition(gPath.get(gPath.size() - 2).getPos());
                 gNextPointDist = gPath.get(gPath.size() - 1).getDistance();
                 nextWayPointDist = gNextPointDist;
                 targetPos = gPath.get(gPath.size() -1).getPos();
                 length = gPath.get(gPath.size() - 1).getDistance();
             } else if (gPath.get(gLastPointIdx + 1).getPos().equals(readPosition)) {
                 // Weiter zum nächsten.
                 // Hier geschieht nicht viel, es muss nur der Weg nach dem nächsten gelöscht werden.
                 gPath = gPath.subList(0, gLastPointIdx + 2);
                 // Neues Ziel einstellen
                 targetPos = gPath.get(gPath.size() - 1).getPos();
                 length = gPath.get(gPath.size() - 1).getDistance();
             } else {
                 System.out.println("Cannot stop movement smooth, hard-resetting to prevent async");
                 unit.setMainPosition(readPosition);
                 targetPos = null;
                 gPath = null;
                 moving = false;
                 return;
             }
         } else {
             // Die Einheit bewegt sich gerade nicht, soll aber trotzdem angehalten werden...
             // Damit es zu keinen async-Problemen kommt, wird hier die Position der Einheit einfach gesetzt
             System.out.println("WARN: Pfusch in moveSTOP!");
             unit.setMainPosition(readPosition);
             targetPos = null;
             path = null;
             moving = false;
         }
     }
 
     /**
      * Mini-Klasse, um mehrere Werte zu Speichern.
      * Speichert den Wegpunkt, die Strecke bis dort hin und die Richtung bis dort hin.
      */
     class PathElement {
 
         /**
          * Das eigentliche "Feld" dieses Wegelements
          */
         private Position pos;
         /**
          * Die Distanz vom Anfang des Weges bis hier her.
          */
         private double distance;
         /**
          * Die Richtung vom letzten Feld zu diesem.
          * Im 8-ter System der Grafikengine angegeben.
          */
         private int direction;
 
         /**
          * Erstellt eine Wegposition ohne Richtung und Entfernung.
          * Für den Anfang von Wegen
          * @param get
          */
         private PathElement(Position get) {
             pos = get;
         }
 
         /**
          * Erstellt eine reguläre Wegposition mit den angegebenen Parametern.
          * @param pos Position dieses Feldes
          * @param length die Strecke bis zu diesem Feld
          * @param vec die Richtung, aus der man kommt
          */
         private PathElement(Position pos, double length, int vec) {
             this.pos = pos;
             distance = length;
             direction = vec;
         }
 
         /**
          * @return the pos
          */
         public Position getPos() {
             return pos;
         }
 
         /**
          * @return the distance
          */
         public double getDistance() {
             return distance;
         }
 
         /**
          * @return the direction
          */
         public int getDirection() {
             return direction;
         }
     }
 }
