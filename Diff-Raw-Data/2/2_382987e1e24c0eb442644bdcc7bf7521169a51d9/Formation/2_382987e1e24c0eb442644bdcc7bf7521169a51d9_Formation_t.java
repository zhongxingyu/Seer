 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de._13ducks.cor.game.server.movement;
 
 import de._13ducks.cor.game.FloatingPointPosition;
 import de._13ducks.cor.game.server.Server;
 
 /**
  * Diese Klasse bietet Funktionen für Einheitenformationen
  *
  * @author michael
  */
 public class Formation {
 
     /**
      * erstellt eine Quadratformation am Ziel, die in die Richtung des Vektors zeigt
      * @param unitCount - die Zahl der Einheiten
      * @param target    - Die Mitte der Formation
      * @param vector    - Die Richtung in die die Formation zeigen soll
      * @param distance  - Der Abstand zwischen den Einheiten
      */
     public static FloatingPointPosition[] createSquareFormation(int unitCount, FloatingPointPosition target, FloatingPointPosition vector, double distance) {
         // Rückgabe-Array initialisieren:
         FloatingPointPosition[] formation = new FloatingPointPosition[unitCount];
         for (int i = 0; i < formation.length; i++) {
             formation[i] = new FloatingPointPosition(0, 0);
         }
 
         // Die Position, die gerde bearbeitet wird
         FloatingPointPosition checkPosition = new FloatingPointPosition(0, 0);
 
         // Wieviele Positionen wurden schon gefudnden?
         int foundPositions = 0;
 
         // Richtung (1=E, 2=N, 3=W, 4=S)
         int direction = 1;
 
         // Wie viele sSchritte gecheckt werden
         int steps = 1;
 
         // wenn true wird steps erhöt
         boolean increaseStepFlag = false;
 
         // Die Position, auf die geklickt wurde:
         if (Server.getInnerServer().netmap.getMoveMap().isPositionWalkable(new FloatingPointPosition(0, 0))) {
             formation[foundPositions] = new FloatingPointPosition(0, 0);
             foundPositions++;
             if (foundPositions == unitCount) {
                 return formation;
             }
         }
 
 
         // endlosschleife, wenn genug positionen gefunden wurden wird sie abgebrochen
         while (true) {
             // X- und Y-Veränderung
             double dx = 0, dy = 0;
 
             // in welche Richtung wird gerade gesucht?
             switch (direction) {
                 case 1:
                     dx = distance;
                     dy = 0;
                     break;
                 case 2:
                     dx = 0;
                     dy = distance;
                     break;
                 case 3:
                     dx = -distance;
                     dy = 0;
                     break;
                 case 4:
                     dx = 0;
                     dy = -distance;
                     break;
             }
 
             // (steps) Schritte in die aktuelle Richtung gehen und bei jedem Schritt die Position überprüfen:
             for (int i = 0; i < steps; i++) {
 
                 // CheckPosition verschieben:
                 checkPosition.setfX(checkPosition.getfX() + dx);
                 checkPosition.setfY(checkPosition.getfY() + dy);
 
                 // CheckPosition rotieren (und damit finalPos berechnen):
                 double lenght = Math.sqrt((vector.getX() * vector.getX()) + (vector.getY() * vector.getY()));
                 double skalar = vector.getY();
                 double cosrot = skalar / lenght;
                 if(Double.isNaN(cosrot)){cosrot = 0.001;}
                 double rotation = Math.acos(cosrot);
                 double x = checkPosition.getfX();
                 double y = checkPosition.getfY();
                 double dist = Math.sqrt((x * x) + (y * y));
                 double newRot = rotation + Math.atan2(checkPosition.getfX(), checkPosition.getfY());
                 x = (Math.cos(newRot) * dist);
                 y = (Math.sin(newRot) * dist);
                 FloatingPointPosition finalPos = new FloatingPointPosition(x, y);
 
                 // Wenn finalPos gültig ist zur Liste hinzufügen:
 
 
                 // Wenn die Position gültig ist (also kein NaN oder infinite enthält), wird überprüft ob Einheiten dort stehen können:
                if (finalPos.add(target).toVector().isValid()) {
                     if (Server.getInnerServer().netmap.getMoveMap().isPositionWalkable(finalPos.add(target))) {
                         formation[foundPositions] = new FloatingPointPosition(finalPos.getfX(), finalPos.getfY());
                         foundPositions++;
                         if (foundPositions == unitCount) {
                             return formation;
                         }
                     }
                 }
             }
 
             // Richtung ändern:
             direction++;
             if (direction > 4) {
                 direction = 1;
             }
 
             // Wenn die Flag schon gesetzt ist inkrementieren, sonst flag setzten
             //(dadurch wird steps bei  jedem 2. Richtungswechsel inkrementiert, was in einer kreisbewegung resultiert)
             if (increaseStepFlag == true) {
                 increaseStepFlag = false;
                 steps++;
             } else {
                 increaseStepFlag = true;
             }
         }
     }
 }
