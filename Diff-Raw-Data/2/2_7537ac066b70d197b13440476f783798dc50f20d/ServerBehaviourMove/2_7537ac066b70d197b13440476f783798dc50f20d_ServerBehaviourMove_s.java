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
 package de._13ducks.cor.game.server.movement;
 
 import de._13ducks.cor.game.FloatingPointPosition;
 import de._13ducks.cor.game.GameObject;
 import de._13ducks.cor.game.Moveable;
 import de._13ducks.cor.game.SimplePosition;
 import de._13ducks.cor.game.Unit;
 import de._13ducks.cor.game.server.Server;
 import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
 import de._13ducks.cor.game.server.ServerCore;
 import de._13ducks.cor.map.fastfindgrid.Traceable;
 import java.awt.geom.Ellipse2D;
 import java.util.List;
 
 /**
  * Lowlevel-Movemanagement
  * 
  * Verwaltet die reine Bewegung einer einzelnen Einheit.
  * Kümmert sich nicht weiter um Formationen/Kampf oder ähnliches.
  * Erhält vom übergeordneten MidLevelManager einen Richtungsvektor und eine Zielkoordinate.
  * Läuft dann dort hin. Tut sonst nichts.
  * Hat exklusive Kontrolle über die Einheitenposition.
  * Weigert sich, sich schneller als die maximale Einheitengeschwindigkeit zu bewegen.
  * Dadurch werden Sprünge verhindert.
  *
  * Wenn eine Kollision festgestellt wird, wird der überliegende GroupManager gefragt was zu tun ist.
  * Der GroupManager entscheidet dann über die Ausweichroute oder lässt uns warten.
  */
 public class ServerBehaviourMove extends ServerBehaviour {
 
     private Moveable caster2;
     private Traceable caster3;
     private SimplePosition target;
     private double speed;
     private boolean stopUnit = false;
     private long lastTick;
     private SimplePosition clientTarget;
     private MovementMap moveMap;
     /**
      * Die Systemzeit zu dem Zeitpunkt, an dem mit dem Warten begonnen wurde
      */
     private long waitStartTime;
     /**
      * Gibt an, ob gerade gewartet wird
      * (z.B. wenn etwas im WEg steht und man wartet bis es den WEg freimacht)
      */
     private boolean wait;
     /**
      * Die Zeit, die gewartet wird (in Nanosekunden) (eine milliarde ist eine sekunde)
      */
     private static final long waitTime = 3000000000l;
     
     /**
      * Wird für die Abstandssuche benötigt. Falls jemals eine Einheit größer ist, MUSS dieser Wert auch erhöht werden.
      */
     private static final double maxRadius = 4;
     /**
      * Eine minimale Distanz, die Einheiten beim Aufstellen wegen einer Kollision berücksichtigen. 
      * Damit wird verhindert, dass aufgrund von Rundungsfehlern Kolision auf ursprünlich als frei
      * eingestuften Flächen berechnet wird.
      */
     public static final double MIN_DISTANCE = 0.1;
 
     public ServerBehaviourMove(ServerCore.InnerServer newinner, GameObject caster1, Moveable caster2, Traceable caster3, MovementMap moveMap) {
         super(newinner, caster1, 1, 20, true);
         this.caster2 = caster2;
         this.caster3 = caster3;
         this.moveMap = moveMap;
 
     }
 
     @Override
     public void activate() {
         active = true;
         trigger();
     }
 
     @Override
     public void deactivate() {
         active = false;
     }
 
     @Override
     public synchronized void execute() {
         // Auto-Ende:
         if (target == null || speed <= 0) {
             deactivate();
             return;
         }
 
         // Wir laufen also.
         // Aktuelle Position berechnen:
         FloatingPointPosition oldPos = caster2.getPrecisePosition();
         Vector vec = target.toFPP().subtract(oldPos).toVector();
         vec.normalizeMe();
         long ticktime = System.nanoTime();
         vec.multiplyMe((ticktime - lastTick) / 1000000000.0 * speed);
         FloatingPointPosition newpos = vec.toFPP().add(oldPos);
 
         // Wir sind im Warten-Modus. Jetzt also testen, ob wir zur nächsten Position können
         if (wait) {
             // Testen, ob wir schon weiterlaufen können:
             // Echtzeitkollision:
             boolean stillColliding = false;
             for (Traceable t : this.caster3.getCell().getTraceablesAroundMe()) {
                 Unit m = t.getUnit();
                if (m.getPrecisePosition().getDistance(newpos) < (m.getRadius() + this.caster2.getRadius())) {
                     stillColliding = true;
                     break;
                 }
             }
             if (stillColliding) {
                 // Immer noch Kollision
                 if (System.nanoTime() - waitStartTime < waitTime) {
                     // Das ist ok, einfach weiter warten
                     lastTick = System.nanoTime();
                     return;
                 } else {
                     wait = false;
                     // Wir stehen schon, der Client auch --> nichts weiter zu tun.
                     target = null;
                     deactivate();
                     return;
                 }
             } else {
                 // Nichtmehr weiter warten - Bewegung wieder starten
                 wait = false;
                 // Ticktime manipulieren.
                 lastTick = System.nanoTime();
                 trigger();
                 return;
             }
         }
 
         if (!target.equals(clientTarget) && !stopUnit) {
             // An Client senden
             rgi.netctrl.broadcastMoveVec(caster2.getNetID(), target.toFPP(), speed);
             clientTarget = target.toFPP();
         }
 
         if (!stopUnit) {
             // Echtzeitkollision:
             for (Traceable t : this.caster3.getCell().getTraceablesAroundMe()) {
                 Unit m = t.getUnit();
                 if (m.getPrecisePosition().getDistance(newpos) < (m.getRadius() + this.caster2.getRadius()) && m != this.caster) {
                     wait = this.caster2.getMidLevelManager().collisionDetected(this.caster2, m);
 
                     double distanceToObstacle = (float) this.caster2.getRadius() + (float) m.getRadius() + (float) MIN_DISTANCE;
 
 
                     Vector origin = new Vector(-vec.getY(), vec.getX());
 
                     Edge edge = new Edge(m.getPrecisePosition().toNode(), m.getPrecisePosition().add(origin.toFPP()).toNode());
 
                     Edge edge2 = new Edge(caster2.getPrecisePosition().toNode(), caster2.getPrecisePosition().add(vec.toFPP()).toNode());
 
                     SimplePosition p = edge.endlessIntersection(edge2);
 
                     double distance = m.getPrecisePosition().getDistance(p.toFPP());
 
                     double b = Math.sqrt((distanceToObstacle * distanceToObstacle) - (distance * distance));
 
                     FloatingPointPosition nextnewpos = p.toVector().add(vec.getInverted().normalize().multiply(b)).toFPP();
 
 
 
                     if (nextnewpos.toVector().isValid() && checkPosition(nextnewpos)) {
                         newpos = nextnewpos;
                     } else {
                         System.out.println("WARNING: Ugly back-stop!");
                         newpos = oldPos.toFPP();
                     }
                     if (wait) {
                         waitStartTime = System.nanoTime();
                         // Spezielle Stopfunktion: (hält den Client in einem Pseudozustand)
                         // Der Client muss das auch mitbekommen
                         rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 24, caster2.getNetID(), 0, Float.floatToIntBits((float) newpos.getfX()), Float.floatToIntBits((float) newpos.getfY())));
                         caster2.setMainPosition(newpos);
                         clientTarget = null;
                         System.out.println("WAIT-COLLISION " + caster2 + " with " + m + " stop at " + newpos);
                         return; // Nicht weiter ausführen!
                     } else {
                         // Bricht die Bewegung vollständig ab.
                         System.out.println("STOP-COLLISION " + caster2 + " with " + m);
                         stopUnit = true;
                     }
                     break;
                 }
             }
         }
         // Ziel schon erreicht?
         Vector nextVec = target.toFPP().subtract(newpos).toVector();
         if (vec.isOpposite(nextVec) && !stopUnit) {
             // Zielvektor erreicht
             // Wir sind warscheinlich drüber - egal einfach auf dem Ziel halten.
             caster2.setMainPosition(target.toFPP());
             caster3.setCell(Server.getInnerServer().netmap.getFastFindGrid().getNewCell(caster3));
             SimplePosition oldTar = target;
             // Neuen Wegpunkt anfordern:
             if (!stopUnit && !caster2.getMidLevelManager().reachedTarget(caster2)) {
                 // Wenn das false gibt, gibts keine weiteren, dann hier halten.
                 target = null;
                 stopUnit = false; // Es ist wohl besser auf dem Ziel zu stoppen als kurz dahinter!
                 deactivate();
             } else {
                 // Herausfinden, ob der Sektor gewechselt wurde (Movemap)
 
                 SimplePosition newTar = target;
                 if (newTar instanceof Node && oldTar instanceof Node) {
                     // Nur in diesem Fall kommt ein Sektorwechsel in Frage
                     FreePolygon sector = commonSector((Node) newTar, (Node) oldTar);
                     // Sektor geändert?
                     if (!sector.equals(caster2.getMyPoly())) {
                         caster2.setMyPoly(sector);
                     }
                 }
 
                 // Herausfinden, ob der Sektor gewechselt wurde (FastFindGrid)
 
             }
 
         } else {
             // Sofort stoppen?
             if (stopUnit) {
                 // Der Client muss das auch mitbekommen
                 rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 24, caster2.getNetID(), 0, Float.floatToIntBits((float) newpos.getfX()), Float.floatToIntBits((float) newpos.getfY())));
                 caster2.setMainPosition(newpos);
                 target = null;
                 stopUnit = false;
                 deactivate();
             } else {
                 // Weiterlaufen
                 caster2.setMainPosition(newpos);
                 lastTick = System.nanoTime();
             }
         }
 
     }
 
     @Override
     public void gotSignal(byte[] packet) {
     }
 
     @Override
     public void pause() {
         caster2.pause();
     }
 
     @Override
     public void unpause() {
         caster2.unpause();
     }
 
     /**
      * Setzt den Zielvektor für diese Einheit.
      * Es wird nicht untersucht, ob das Ziel in irgendeiner Weise ok ist, die Einheit beginnt sofort loszulaufen.
      * In der Regel sollte noch eine Geschwindigkeit angegeben werden.
      * Wehrt sich gegen nicht existente Ziele.
      * @param pos die Zielposition, wird auf direktem Weg angesteuert.
      */
     public synchronized void setTargetVector(SimplePosition pos) {
         if (pos == null) {
             throw new IllegalArgumentException("Cannot send " + caster2 + " to null");
         }
         if (!pos.toVector().isValid()) {
             throw new IllegalArgumentException("Cannot send " + caster2 + " to invalid position");
         }
         target = pos;
         lastTick = System.nanoTime();
         clientTarget = Vector.ZERO;
         activate();
     }
 
     /**
      * Setzt den Zielvektor und die Geschwindigkeit und startet die Bewegung sofort.
      * @param pos die Zielposition
      * @param speed die Geschwindigkeit
      */
     public synchronized void setTargetVector(SimplePosition pos, double speed) {
         changeSpeed(speed);
         setTargetVector(pos);
     }
 
     /**
      * Ändert die Geschwindigkeit während des Laufens.
      * Speed wird verkleinert, wenn der Wert über dem Einheiten-Maximum liegen würde
      * @param speed Die Einheitengeschwindigkeit
      */
     public synchronized void changeSpeed(double speed) {
         if (speed > 0 && speed <= caster2.getSpeed()) {
             this.speed = speed;
         }
         trigger();
     }
 
     public boolean isMoving() {
         return target != null;
     }
 
     /**
      * Stoppt die Einheit innerhalb eines Ticks.
      */
     public synchronized void stopImmediately() {
         stopUnit = true;
         trigger();
     }
 
     /**
      * Findet einen Sektor, den beide Knoten gemeinsam haben
      * @param n1 Knoten 1
      * @param n2 Knoten 2
      */
     private FreePolygon commonSector(Node n1, Node n2) {
         for (FreePolygon poly : n1.getPolygons()) {
             if (n2.getPolygons().contains(poly)) {
                 return poly;
             }
         }
         return null;
     }
 
     /**
      * Berechnet den Winkel zwischen zwei Vektoren
      * @param vector_1
      * @param vector_2
      * @return
      */
     public double getAngle(FloatingPointPosition vector_1, FloatingPointPosition vector_2) {
         double scalar = ((vector_1.getfX() * vector_2.getfX()) + (vector_1.getfY() * vector_2.getfY()));
 
         double vector_1_lenght = Math.sqrt((vector_1.getfX() * vector_1.getfX()) + vector_2.getfY() * vector_1.getfY());
         double vector_2_lenght = Math.sqrt((vector_2.getfX() * vector_2.getfX()) + vector_2.getfY() * vector_2.getfY());
 
         double lenght = vector_1_lenght * vector_2_lenght;
 
         double angle = Math.acos((scalar / lenght));
 
         return angle;
     }
 
     /**
      * Überprüft auf RTC mit Zielposition
      * @param pos die zu testende Position
      * @return true, wenn frei
      */
     private boolean checkPosition(FloatingPointPosition pos) {
         List<Moveable> movers = moveMap.moversAroundPoint(pos, caster2.getRadius() + maxRadius);
         movers.remove(caster2);
         for (Moveable m : movers) {
             if (m.getPrecisePosition().getDistance(pos) < (m.getRadius() + caster2.getRadius())) {
                 return false;
             }
         }
         return true;
     }
 }
