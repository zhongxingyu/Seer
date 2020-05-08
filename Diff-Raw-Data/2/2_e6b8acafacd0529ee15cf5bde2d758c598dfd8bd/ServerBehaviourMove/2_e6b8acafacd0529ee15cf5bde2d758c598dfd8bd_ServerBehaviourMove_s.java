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
 import de._13ducks.cor.game.server.Server;
 import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
 import de._13ducks.cor.game.server.ServerCore;
 import de._13ducks.cor.map.fastfindgrid.Traceable;
 import java.util.List;
 import org.newdawn.slick.geom.Circle;
 import org.newdawn.slick.geom.Polygon;
 
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
     /**
      * Mittelpunkt für arc-Bewegungen
      */
     private SimplePosition around;
     /**
      * Aktuelle Bewegung eine Kurve?
      */
     private boolean arc;
     /**
      * Richtung der Kurve (true = Plus)
      */
     private boolean arcDirection;
     /**
      * Wie weit (im Bogenmaß) wir auf dem Kreis laufen müssen
      */
     private double tethaDist;
     /**
      * Wie weit wir (im Bogenmaß) auf dem Kreis schon gelaufen sind
      */
     private double movedTetha;
     private double speed;
     private boolean stopUnit = false;
     private long lastTick;
     private SimplePosition clientTarget;
     private MovementMap moveMap;
     private GroupMember pathManager;
     /**
      * Die Systemzeit zu dem Zeitpunkt, an dem mit dem Warten begonnen wurde
      */
     private long waitStartTime;
     /**
      * Gibt an, ob gerade gewartet wird
      * (z.B. wenn etwas im Weg steht und man wartet bis es den Weg freimacht)
      */
     private boolean wait;
     /**
      * Die Zeit, die gewartet wird (in Nanosekunden) (eine milliarde ist eine sekunde)
      */
     private static final long waitTime = 3000000000l;
     /**
      * Eine minimale Distanz, die Einheiten beim Aufstellen wegen einer Kollision berücksichtigen. 
      * Damit wird verhindert, dass aufgrund von Rundungsfehlern Kolision auf ursprünlich als frei
      * eingestuften Flächen berechnet wird.
      */
     public static final double MIN_DISTANCE = 0.1;
     /**
      * Ein simples Flag, das nach dem Kollisionstest gesetzt ist.
      */
     private boolean colliding = false;
     /**
      * Zeigt nach dem Kollisionstest auf das letzte (kollidierende) Objekt.
      */
     private Moveable lastObstacle;
 
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
         // Erstmal default-Berechnung für gerades laufen
         FloatingPointPosition oldPos = caster2.getPrecisePosition();
         long ticktime = System.nanoTime();
         Vector vec = target.toFPP().subtract(oldPos).toVector();
         vec.normalizeMe();
         vec.multiplyMe((ticktime - lastTick) / 1000000000.0 * speed);
         FloatingPointPosition newpos = vec.toFPP().add(oldPos);
         if (arc) {
             // Kreisbewegung berechnen:
             double rad = Math.sqrt((oldPos.x() - around.x()) * (oldPos.x() - around.x()) + (oldPos.y() - around.y()) * (oldPos.y() - around.y()));
             double tetha = Math.atan2(oldPos.y() - around.y(), oldPos.x() - around.x());
             double delta = vec.length(); // Wie weit wir auf diesem Kreis laufen
             if (!arcDirection) { // Falls Richtung negativ delta invertieren
                 delta *= -1;
             }
             double newTetha = ((tetha * rad) + delta) / rad; // Strahlensatz, u = 2*PI*r
             movedTetha += Math.abs(newTetha - tetha);
             // Über-/Unterläufe behandeln:
             if (newTetha > Math.PI) {
                 newTetha = -2 * Math.PI + newTetha;
             } else if (newTetha < -Math.PI) {
                 newTetha = 2 * Math.PI + newTetha;
             }
             Vector newPvec = new Vector(Math.cos(newTetha), Math.sin(newTetha));
             newPvec = newPvec.multiply(rad);
             newpos = around.toVector().add(newPvec).toFPP();
         }
 
         // Ob die Kollision später noch geprüft werden muss. Kann durch ein Weiterlaufen nach warten überschrieben werden.
         boolean checkCollision = true;
 
         // Wir sind im Warten-Modus. Jetzt also testen, ob wir zur nächsten Position können
         if (wait) {
             // Testen, ob wir schon weiterlaufen können:
             // Echtzeitkollision:
             newpos = checkAndMaxMove(oldPos, newpos);
             if (colliding) {
                 // Immer noch Kollision
                 if (System.nanoTime() - waitStartTime < waitTime) {
                     // Das ist ok, einfach weiter warten
                     lastTick = System.nanoTime();
                     return;
                 } else {
                     // Wartezeit abgelaufen
                     wait = false;
                     // Wir stehen schon, der Client auch --> nichts weiter zu tun.
                     target = null;
                     deactivate();
                     System.out.println("STOP waiting: " + caster2);
                     return;
                 }
             } else {
                 // Nichtmehr weiter warten - Bewegung wieder starten
                 System.out.println("GO! Weiter mit " + caster2 + " " + newpos + " nach " + target);
                 wait = false;
                 checkCollision = false;
             }
         }
 
         if (!target.equals(clientTarget) && !stopUnit) {
             // An Client senden
             rgi.netctrl.broadcastMoveVec(caster2.getNetID(), target.toFPP(), speed);
             clientTarget = target.toFPP();
         }
 
         if (checkCollision) {
             // Zu laufenden Weg auf Kollision prüfen
             newpos = checkAndMaxMove(oldPos, newpos);
 
             if (!stopUnit && colliding) {
                 // Kollision. Gruppenmanager muss entscheiden, ob wir warten oder ne Alternativroute suchen.
                 wait = this.caster2.getMidLevelManager().collisionDetected(this.caster2, lastObstacle, target);
 
                 if (wait) {
                     waitStartTime = System.nanoTime();
                     // Spezielle Stopfunktion: (hält den Client in einem Pseudozustand)
                     // Der Client muss das auch mitbekommen
                     rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 24, caster2.getNetID(), 0, Float.floatToIntBits((float) newpos.getfX()), Float.floatToIntBits((float) newpos.getfY())));
                     setMoveable(oldPos, newpos);
                     clientTarget = null;
                     System.out.println("WAIT-COLLISION " + caster2 + " with " + lastObstacle + " stop at " + newpos);
                     return; // Nicht weiter ausführen!
                 } else {
                     // Nochmal laufen, wir haben ein neues Ziel!
                     trigger();
                     return;
                 }
             }
         }
         // Ziel schon erreicht?
         Vector nextVec = target.toFPP().subtract(newpos).toVector();
         boolean arcDone = false;
         if (arc) {
             arcDone = movedTetha >= tethaDist;
         }
         if (((!arc && vec.isOpposite(nextVec)) || arcDone || newpos.equals(target)) && !stopUnit) {
             // Zielvektor erreicht
             // Wir sind warscheinlich drüber - egal einfach auf dem Ziel halten.
             setMoveable(oldPos, target.toFPP());
             // Neuen Wegpunkt anfordern:
             if (!pathManager.reachedTarget(caster2)) {
                 // Wenn das false gibt, gibts keine weiteren, dann hier halten.
                 target = null;
                 stopUnit = false; // Es ist wohl besser auf dem Ziel zu stoppen als kurz dahinter!
                 deactivate();
             }
 
         } else {
             // Sofort stoppen?
             if (stopUnit) {
                 // Der Client muss das auch mitbekommen
                 rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 24, caster2.getNetID(), 0, Float.floatToIntBits((float) newpos.getfX()), Float.floatToIntBits((float) newpos.getfY())));
                 System.out.println("MANUSTOP: " + caster2 + " at " + newpos);
                 setMoveable(oldPos, newpos);
                 target = null;
                 stopUnit = false;
                 deactivate();
             } else {
                 // Weiterlaufen
                 setMoveable(oldPos, newpos);
                 lastTick = System.nanoTime();
             }
         }
 
     }
 
     private void setMoveable(FloatingPointPosition from, FloatingPointPosition to) {
         caster2.setMainPosition(to);
         // Neuer Sektor?
         while (pathManager.nextSectorBorder() != null && pathManager.nextSectorBorder().sidesDiffer(from, to)) {
             // Ja, alten löschen und neuen setzen!
             SectorChangingEdge edge = pathManager.borderCrossed();
             caster2.setMyPoly(edge.getNext(caster2.getMyPoly()));
         }
         // Schnellsuchraster aktualisieren:
         caster3.setCell(Server.getInnerServer().netmap.getFastFindGrid().getNewCell(caster3));
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
      * Falls arc true ist, werden arcDirection und arcCenter ausgewertet, sonst nicht.
      * @param pos die Zielposition
      * @param arc ob das Ziel auf einer Kurve angesteuert werden soll (true)
      * @param arcDirection Richtung der Kurve (true - Bogenmaß-Plusrichtung)
      * @param arcCenter um welchen Punkt gedreht werden soll.
      */
     public synchronized void setTargetVector(SimplePosition pos, boolean arc, boolean arcDirection, SimplePosition arcCenter) {
         if (pos == null) {
             throw new IllegalArgumentException("Cannot send " + caster2 + " to null (" + arc + ")");
         }
         if (!pos.toVector().isValid()) {
             throw new IllegalArgumentException("Cannot send " + caster2 + " to invalid position (" + arc + ")");
         }
         target = pos;
         lastTick = System.nanoTime();
         clientTarget = Vector.ZERO;
         this.arc = arc;
         this.arcDirection = arcDirection;
         this.around = arcCenter;
         this.movedTetha = 0;
         if (arc) {
             // Länge des Kreissegments berechnen
             double startTetha = Math.atan2(caster2.getPrecisePosition().y() - around.y(), caster2.getPrecisePosition().x() - around.x());
             double targetTetha = Math.atan2(target.y() - around.y(), target.x() - around.x());
             if (arcDirection) {
                 tethaDist = targetTetha - startTetha;
             } else {
                 tethaDist = startTetha - targetTetha;
             }
             if (tethaDist < 0) {
                 tethaDist = 2 * Math.PI + tethaDist;
             }
         }
 
         activate();
     }
 
     /**
      * Setzt den Zielvektor und die Geschwindigkeit und startet die Bewegung sofort.
      * @param pos die Zielposition
      * @param speed die Geschwindigkeit
      */
     public synchronized void setTargetVector(SimplePosition pos, double speed, boolean arc, boolean arcDirection, SimplePosition arcCenter) {
         changeSpeed(speed);
         setTargetVector(pos, arc, arcDirection, arcCenter);
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
      * Untersucht die gegebene Route auf Echtzeitkollision.
      * Sollte alles frei sein, wird to zurückgegeben.
      * Ansonsten gibts eine neue Position, bis zu der problemlos gelaufen werden kann.
      * Im schlimmsten Fall ist dies die from-Position, die frei sein MUSS!
      * @param from von wo wir uns losbewegen
      * @param to wohin wir laufen
      * @return FloatingPointPosition bis wohin man laufen kann.
      */
     private FloatingPointPosition checkAndMaxMove(FloatingPointPosition from, FloatingPointPosition to) {
         // Zuallererst: Wir dürfen nicht über das Ziel hinaus laufen:
         if (!arc) {
             Vector oldtargetVec = new Vector(target.x() - from.x(), target.y() - from.y());
             Vector newtargetVec = new Vector(target.x() - to.x(), target.y() - to.y());
             if (oldtargetVec.isOpposite(newtargetVec)) {
                 // Achtung, zu weit!
                 to = target.toFPP();
             }
         } else {
             // Arc-Bewegung begrenzen:
             if (movedTetha >= tethaDist) {
                 to = target.toFPP();
             }
         }
         // Zurücksetzen
         lastObstacle = null;
         colliding = false;
         List<Moveable> possibleCollisions = moveMap.moversAroundPoint(caster2.getPrecisePosition(), caster2.getRadius() + 10, caster2.getMyPoly());
         // Uns selber ignorieren
         possibleCollisions.remove(caster2);
         // Freies Gebiet markieren:
         Vector ortho = new Vector(to.getfY() - from.getfY(), from.getfX() - to.getfX()); // 90 Grad verdreht (y, -x)
         ortho.normalizeMe();
         ortho.multiplyMe(caster2.getRadius());
         Vector fromv = from.toVector();
         Vector tov = to.toVector();
         Polygon poly = new Polygon();
         poly.addPoint((float) fromv.add(ortho).x(), (float) fromv.add(ortho).y());
         poly.addPoint((float) fromv.add(ortho.getInverted()).x(), (float) fromv.add(ortho.getInverted()).y());
         poly.addPoint((float) tov.add(ortho.getInverted()).x(), (float) tov.add(ortho.getInverted()).y());
         poly.addPoint((float) tov.add(ortho).x(), (float) tov.add(ortho).y());
         poly.addPoint((float) fromv.add(ortho).x(), (float) fromv.add(ortho).y());
 
         for (Moveable t : possibleCollisions) {
             float radius = (float) (t.getRadius() + MIN_DISTANCE / 2);
             Circle c = new Circle((float) t.getPrecisePosition().x(), (float) t.getPrecisePosition().y(), radius); //Das getUnit ist ugly!
             boolean arcCol = collidesOnArc(t, around, radius, from, to);
             // Die Kollisionsbedingungen: Schnitt mit Begrenzungslinien, liegt innerhalb des Testpolygons, liegt zu nah am Ziel
             // Die ersten beiden Bedingungen gelten nur für nicht-arc-Bewegungen!
             // Die letzte gilt dafür nur für arc-Bewegungen
             if (!arc && (poly.intersects(c)) || (!arc && poly.includes(c.getCenterX(), c.getCenterY())) || to.getDistance(t.getPrecisePosition()) < caster2.getRadius() + radius || (arc && arcCol)) {
                 System.out.println("COL! with: " + t + " at " + t.getPrecisePosition() + " (dist: " + to.getDistance(t.getPrecisePosition()) + ") on route to " + target + " critical point is " + to);
                 // Kollision!
                 if (!arc) {
                     // Jetzt muss poly verkleinert werden.
                     // Dazu muss die Zielposition to auf der Strecke von from nach to so weit wie notwendig nach hinten verschoben werden.
                     // Notwendiger Abstand zur gefundenen Kollision t
                     float distanceToObstacle = (float) (this.caster2.getRadius() + radius + MIN_DISTANCE / 2);
                     // Vector, der vom start zum Ziel der Bewegung zeigt.
                     Vector dirVec = new Vector(to.getfX() - from.getfX(), to.getfY() - from.getfY());
                     // 90 Grad dazu
                     Vector origin = new Vector(-dirVec.getY(), dirVec.getX());
                     // Strecke vom Hinderniss in 90-Grad-Richtung
                     Edge edge = new Edge(t.getPrecisePosition().toNode(), t.getPrecisePosition().add(origin.toFPP()).toNode());
                     // Strecke zwischen start und ziel
                     Edge edge2 = new Edge(caster2.getPrecisePosition().toNode(), caster2.getPrecisePosition().add(dirVec.toFPP()).toNode());
                     // Schnittpunkt
                     SimplePosition p = edge.endlessIntersection(edge2);
                     if (p == null) {
                         System.out.println("ERROR: " + caster2 + " " + edge + " " + edge2 + " " + t.getPrecisePosition() + " " + origin + " " + dirVec + " " + distanceToObstacle);
                     }
                     // Abstand vom Hinderniss zur Strecke edge2
                     double distance = t.getPrecisePosition().getDistance(p.toFPP());
                     // Abstand vom Punkt auf edge2 zu freigegebenem Punkt
                     double b = Math.sqrt((distanceToObstacle * distanceToObstacle) - (distance * distance));
                     // Auf der Strecke weit genug zurück gehen:
                     FloatingPointPosition nextnewpos = p.toVector().add(dirVec.getInverted().normalize().multiply(b)).toFPP();
 
                     // Zurückgegangenes Stück analysieren
                     if (new Vector(nextnewpos.getfX() - fromv.x(), nextnewpos.getfY() - fromv.y()).isOpposite(dirVec)) {
                         // Ganz zurück setzen. Dann muss man nicht weiter suchen, die ganze Route ist hoffnungslos blockiert
                         colliding = true;
                         lastObstacle = t;
                         return from;
                     }
 
                     // Hier gibt es keine Kollision mehr.
                     // poly neu bauen:
                     to = nextnewpos;
                     tov = nextnewpos.toVector();
                     poly = new Polygon();
                     poly.addPoint((float) fromv.add(ortho).x(), (float) fromv.add(ortho).y());
                     poly.addPoint((float) fromv.add(ortho.getInverted()).x(), (float) fromv.add(ortho.getInverted()).y());
                     poly.addPoint((float) tov.add(ortho.getInverted()).x(), (float) tov.add(ortho.getInverted()).y());
                     poly.addPoint((float) tov.add(ortho).x(), (float) tov.add(ortho).y());
                     poly.addPoint((float) fromv.add(ortho).x(), (float) fromv.add(ortho).y());
 
 
                 } else {
                     // to auf den Laufkreis nach hinten verschieben.
                     Vector obst = t.getPrecisePosition().toVector();
                     // Als erstes die Schnittpunkte der beiden Kreise bestimmmen
                     // Zuerst Mittelpunkt der Linie durch beide Schnittpunkte berechnen:
                     Vector direct = new Vector(around.x() - obst.x(), around.y() - obst.y());
                     double moveRad = from.toFPP().getDistance(around.toFPP());
                     Vector z1 = direct.normalize().multiply(caster2.getRadius() + radius);
                    Vector z2 = direct.normalize().multiply(moveRad);
                     Vector mid = direct.normalize().multiply((z1.length() + z2.length()) / 2.0);
                     // Senkrechten Vektor und seine Länge berechnen:
                     Vector ortho2 = new Vector(direct.y(), -direct.x());
                     ortho2 = ortho2.normalize().multiply(Math.sqrt(((caster2.getRadius() + radius) * (caster2.getRadius() + radius)) - (mid.length() * mid.length())));
                     // Schnittpunkte ausrechnen:
                     Vector posMid = new Vector(obst.x() + mid.x(), obst.y() + mid.y()); // Positionsvektor des Mittelpunkts
                     Vector s1 = posMid.add(ortho2);
                     Vector s2 = posMid.add(ortho2.getInverted());
                     // Ausbrüten, ob s1 oder s2 der richtige ist:
                     Vector newPosVec = null;
                     double fromTetha = Math.atan2(around.y() - from.y(), around.x() - from.x());
                     if (fromTetha < 0) {
                         fromTetha += 2 * Math.PI;
                     }
                     double s1tetha = Math.atan2(around.y() - s1.y(), around.x() - s1.x());
                     if (s1tetha < 0) {
                         s1tetha += 2 * Math.PI;
                     }
                     double s2tetha = Math.atan2(around.y() - s2.y(), around.x() - s2.x());
                     if (s2tetha < 0) {
                         s2tetha += 2 * Math.PI;
                     }
                     if (s1tetha < fromTetha) {
                         s1tetha += 2 * Math.PI;
                     }
                     if (s2tetha < fromTetha) {
                         s2tetha += 2 * Math.PI;
                     }
                     if (s1tetha < s2tetha) {
                         if (arcDirection) {
                             newPosVec = s1;
                         } else {
                             newPosVec = s2;
                         }
                     } else {
                         if (arcDirection) {
                             newPosVec = s2;
                         } else {
                             newPosVec = s1;
                         }
                     }
 
                     to = around.toVector().add(newPosVec).toFPP();
                     tov = to.toVector();
                     
                 
                 }
                 colliding = true;
                 lastObstacle = t;
 
                 // Falls wir so weit zurück mussten, dass es gar netmehr weiter geht:
                 if (from.equals(to)) {
                     return from;
                 }
             }
         }
 
         // Jetzt wurde alles oft genug verschoben um definitiv keine Kollision mehr zu haben.
         // Bis hierhin die Route also freigeben:
         return to;
     }
 
     /**
      * Hiermit lässt sich herausfinden, ob dieser mover gerade wartet, weil jemand im Weg steht.
      * @return 
      */
     boolean isWaiting() {
         return wait;
     }
 
     /**
      * @return the pathManager
      */
     GroupMember getPathManager() {
         return pathManager;
     }
 
     /**
      * @param pathManager the pathManager to set
      */
     void setPathManager(GroupMember pathManager) {
         this.pathManager = pathManager;
     }
 
     /**
      * Findet heraus, ob das gegebenen Moveable auf dem gegebenen zu laufenden Abschnitt liegt,
      * also eine Kollision darstellt.
      * @param t
      * @return true, wenn Kollision
      */
     private boolean collidesOnArc(Moveable t, SimplePosition around, double colRadius, SimplePosition from, SimplePosition to) {
         if (!arc) {
             return false;
         }
         // Muss in +arc-Richtung erfolgen, notfalls start und Ziel tauschen
         if (!arcDirection) {
             SimplePosition back = from;
             from = to;
             to = back;
         }
 
         // Zuerst auf Nähe des gesamten Kreissegments testen
         double dist = t.getPrecisePosition().getDistance(around.toFPP());
         double moveRad = from.toFPP().getDistance(around.toFPP());
         double minCol = moveRad - colRadius - t.getRadius();
         double maxCol = moveRad + colRadius + t.getRadius();
         if (dist >= minCol && dist <= maxCol) {
             // Mögliche Kollision!
             // Winkeltest
             double fromTetha = Math.atan2(around.y() - from.y(), around.x() - from.x());
             if (fromTetha < 0) {
                 fromTetha += 2 * Math.PI;
             }
             double toTetha = Math.atan2(around.y() - to.y(), around.x() - to.x());
             if (toTetha < 0) {
                 toTetha += 2 * Math.PI;
             }
             double colTetha = Math.atan2(around.y() - t.getPrecisePosition().y(), around.x() - t.getPrecisePosition().x());
             if (colTetha < 0) {
                 colTetha += 2 * Math.PI;
             }
             // Zusätzlichen Umlauf beachten
             if (toTetha < fromTetha) {
                 if (colTetha < toTetha) {
                     colTetha += 2 * Math.PI;
                 }
                 toTetha += 2 * Math.PI;
             }
             if (colTetha >= fromTetha && colTetha <= toTetha) {
                 // Dann auf jeden Fall
                 return true;
             }
 
             // Sonst weitertesten: Der 6-Punkte-Test
             Circle c = new Circle((float) t.getPrecisePosition().x(), (float) t.getPrecisePosition().y(), (float) t.getRadius());
             Vector fromOrtho = new Vector(from.x() - around.x(), from.y() - around.y());
             fromOrtho = fromOrtho.normalize().multiply(colRadius);
             Vector toOrtho = new Vector(to.x() - around.x(), to.y() - around.y());
             toOrtho = toOrtho.normalize().multiply(colRadius);
 
             SimplePosition t1 = from.toVector().add(fromOrtho);
             SimplePosition t2 = from.toVector();
             SimplePosition t3 = from.toVector().add(fromOrtho.getInverted());
             SimplePosition t4 = to.toVector().add(toOrtho);
             SimplePosition t5 = to.toVector();
             SimplePosition t6 = to.toVector().add(toOrtho.normalize());
 
             if (c.contains((float) t1.x(), (float) t1.y())
                     || c.contains((float) t2.x(), (float) t2.y())
                     || c.contains((float) t3.x(), (float) t3.y())
                     || c.contains((float) t4.x(), (float) t4.y())
                     || c.contains((float) t5.x(), (float) t5.y())
                     || c.contains((float) t6.x(), (float) t6.y())) {
                 return true;
             }
         }
 
         return false;
     }
 }
