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
 package thirteenducks.cor.game.server;
 
 import thirteenducks.cor.game.client.ClientIALGroup;
 import thirteenducks.cor.game.Building;
 import thirteenducks.cor.game.GameObject;
 import java.util.ArrayList;
 import java.util.Timer;
 import thirteenducks.cor.game.Position;
 import thirteenducks.cor.networks.server.behaviour.ServerBehaviour;
 import thirteenducks.cor.game.Unit;
 
 /**
  * Diese Klasse ist das (neue) zentrale Bewegungs-Managementsystem.
  * Alle Einheitenbewegungen werden über dieses Modul gestartet, gestoppt und geändert.
  * Das ServerBehaviourMove berechnet weiterhin alle Bewegungen, stellt es fest,
  * dass die Route geändert werden muss, wird dieses Modul benachrichtigt.
  * Dieses Modul hat die folgenden Aufgaben:
  * - Initialisierung aller Individualbewegungen
  * - Initialisierung aller Gruppenbewegungen via IAL
  * - Strikte Eine-Einheit-pro-Feld-Überwachung
  * - Verarbeiten der Client-Angriff-Request (sowohl human als auch autoselect)
  *
  *
  */
 public class ServerMoveManager {
 
     ServerCore.InnerServer inner;
     Timer timer;
 
     public ServerMoveManager(ServerCore.InnerServer newinner) {
         inner = newinner;
         timer = new Timer();
     }
 
     /**
      * Das automatische Zielsuch-Behaviour des Clients hat sich ein Ziel ausgesucht.
      * An das Angriffsbehaviour weiterleiten
      * @param atk
      * @param victim
      */
     public void idleAttack(Unit atk, GameObject victim) {
         // Allgemeine Absicherungen:
         if (atk == null || victim == null) {
             throw new NullPointerException();
         }
         if (!(victim instanceof Unit || victim instanceof Building)) {
             throw new IllegalArgumentException("You can attack RogBuilding or RogUnit. Nothing else.");
         }
 
         if (victim instanceof Unit) {
             atk.attackManager.attackUnit((Unit) victim);
         } else {
             atk.attackManager.attackBuilding((Building) victim);
         }
     }
 
     /**
      * Ein Spieler hat eine Einheit selektiert und möchte damit angreiffen.
      * Das wars, alles andere macht der MoveManager automatisch.
      * @param atk
      * @param victim
      */
     public void humanSingleAttack(Unit atk, GameObject victim) {
         System.out.println("AddMe: HUMAN_SINGLE_ATTACK");
    /*     // Allgemeine Absicherungen:
         if (atk == null || victim == null) {
             throw new NullPointerException();
         }
         if (!(victim instanceof Unit || victim instanceof Building)) {
             throw new IllegalArgumentException("You can attack RogBuilding or RogUnit. Nothing else.");
         }
         // Gut, die Argumente sollten ok sein, auf gehts.
         ArrayList<Unit> attackers = new ArrayList<Unit>();
         attackers.add(atk);
 
         if (victim instanceof Unit) {
             // Eine Einheit ist das Ziel
             intelligentAttack((Unit) victim, (ArrayList<Unit>) attackers.clone());
         } else {
             // Angriff auf Gebäude
             intelligentAttack((Building) victim, (ArrayList<Unit>) attackers.clone());
         }
         // Jetzt sind alle zugeteilt
         // Angriffziel setzen
         if (victim instanceof Unit) {
             Unit unitVic = (Unit) victim;
             for (Unit unit : attackers) {
                 unit.attackManager.ialUnit(unitVic);
             }
         } else {
             Building bVic = (Building) victim;
             for (Unit unit : attackers) {
                 unit.attackManager.ialBuilding(bVic);
             }
         }
 */
     }
 
     /**
      * Ein Spieler hat einige Einheiten selektiert und möchte damit ein Ziel angreifen.
      * Das wars, alles andere macht der MoveManager automatisch.
      * Eine optimale Angriffsposition wird gesucht, die Einheiten dort hingeschickt und das Angriffsverhalten aktiviert.
      * @param attackers
      * @param victim
      */
     public void humanGroupAttack(ArrayList<Unit> attackers, GameObject victim) {
         System.out.println("AddMe: HUMAN_GROUP_ATTACK");
      /*   // Allgemeine Absicherungen:
         if (attackers == null || victim == null) {
             throw new NullPointerException();
         }
         if (attackers.isEmpty()) {
             throw new java.lang.IllegalArgumentException("Can't attack without attackers! (attackers.isEmpty() == true)!");
         }
         if (!(victim instanceof Unit || victim instanceof Building)) {
             throw new IllegalArgumentException("You can attack RogBuilding or RogUnit. Nothing else.");
         }
 
         // Gut, die Argumente sollten ok sein, auf gehts.
         if (victim instanceof Unit) {
             // Eine Einheit ist das Ziel
             intelligentAttack((Unit) victim, (ArrayList<Unit>) attackers.clone());
         } else {
             // Angriff auf Gebäude
             intelligentAttack((Building) victim, (ArrayList<Unit>) attackers.clone());
         }
         // Jetzt sind alle zugeteilt
         // Angriffziel setzen
         if (victim instanceof Unit) {
             Unit unitVic = (Unit) victim;
             for (Unit unit : attackers) {
                 unit.attackManager.ialUnit(unitVic);
             }
         } else {
             Building bVic = (Building) victim;
             for (Unit unit : attackers) {
                 unit.attackManager.ialBuilding(bVic);
             }
         }
         // Fertig. */
     }
 
     /**
      * Bewegungsbefehl für eine einzelne Einheit vom Spieler
      * @param unit
      * @param target
      */
     public synchronized void humanSingleMove(Unit unit, Position target, boolean allowDifferentTarget) {
         //Bewegung initialisieren:
 
         // Weg suchen
         ArrayList<Position> newpath = null;
 
         newpath = inner.pathfinder.findPath(unit, target, allowDifferentTarget);
         /*
         if (unit.path.indexOf(unit.position) + 1 != unit.path.size()) {
         newpath = inner.pathfinder.findPath(unit.path.get(unit.lastwaypoint + 1), target, unit.getPlayerId(), allowDifferentTarget);
         } else {
         System.out.println("FixMe: ERROR switching Path! 1");
         } */
         System.out.println("Check for Switchpath!");
 
         if (newpath == null || newpath.size() == 1) {
             // Es gibt keinen Weg, wir können da also nicht hingehen
             // Kollision wieder setzen - falls wir stehen
             return;
         }
         // Wenn wir uns nicht bewegen, dann einfach Pfad nehmen und losrennen
         this.directMoveUnit(newpath, unit, false);
 
         /*  } else {
         try {
         // Bewegt sich gerade, neuen Pfad an nächsten Wegpunkt anhängen.
         // Wir löschen alle Wegpunkte nach dem Nächsten:
         newpath = saveSubList(unit.path, unit.lastwaypoint, newpath);
 
         // Altes Ziel freigeben
         inner.netmap.deleteFieldReservation(unit.movingtarget);
 
         this.attachMoveUnit(newpath, unit, false, unit.attacktarget != null);
 
         } catch (Exception ex) {
         // Relativ Wichtig.
         System.out.println("FixMe: ERROR switching Path! 2");
         inner.logger(ex);
         }
 
         } */
     }
 
     /**
      * Versucht, die angegebene Einheit so schnell wie möglich zu stoppen
      * @param unit
      */
     public void stopMovement(Unit unit) {
         System.out.println("AddMe: aroundMe() required!");
     }
 
     /**
      * Bewegungsbefehl vom Spieler für mehrere Einheiten
      * @param target - das angeklickte Bewegungsziel
      * @param movers - alle Einheiten, die dort hin sollen
      */
     public void humanGroupMove(Position target, final ArrayList<Unit> movers) {
         Position vector = movers.get(0).getMainPosition().subtract(target).transformToVector();
         // Jetzt alle Einheiten laufen lassen:
         for (int i = 0; i < movers.size(); i++) {
            Position individualTarget = target.aroundMePlus(vector, movers.get(0), true, 0, Position.AROUNDME_CIRCMODE_HALF_CIRCLE, Position.AROUNDME_COLMODE_GROUNDTARGET, true, inner);
             this.humanSingleMove(movers.get(i), individualTarget, true);
         }
     }
 
     public void cancelledReservationFor(Unit unit) {
         System.out.println("AddMe: Cancel reservation!");
     }
 
     /**
      * Sucht dem Angriffsbehaviour eine neue Position zum Angreiffen für das bisherige Ziel.
      * Wenn es keine gibt, wird ein neues Ziel gesucht, das angegriffen werden kann.
      * Wird auch nach dem Töten aufgerufen, sucht dann ein neues Ziel.
      * Eine der wichtigsten Methoden den Bewegungs/Angriffsystems, denn diese
      * Methode ermöglicht erstmals das automatische Weiterkämpfen ohne
      * Kollisionsprobleme!
      *
      * Diese Methode sucht nach folgendem Schema nach neuen Positionen/Angriffszielen:
      * 1. Wenn die Einheit so wie sie dasteht eine andere Angreiffen kann, wird das gemacht, ansonsten:
      * 2. Wenn noch eine Position um das ursprüngliche Ziel frei ist, wird die Einheit dort hingeschickt, ansonsten:
      * 3. Wird ein anderes Ziel mit einer freien Position gesucht und die Einheit dort hingeschickt.
      *
      * Wenn alles nichts hilft, wird die Einheit stehen gelassen, und das Angriffsziel gelöscht.
      * Dann wartet die Einheit eine bestimmte Zeit (z.B. 3Sekunden) und sucht dann neu
      *
      * Einheiten suchen nicht nach Zielen, wenn sie einen Baubefehl haben, dieser geht vor
      * @param unit
      */
  /*   public void searchNewAtkPosForMeele(final Unit unit) {
         // Es darf nur ein neues Ziel gesucht werden, wenn die Einheit gerade nicht mit wichtigerem Beschäftigt ist
         // Nicht während dem Bauen:
         ServerBehaviour constructing = unit.getServerBehaviour(5);
         if (constructing != null && constructing.isActive()) {
             // Angriffsverhalten abschalten
             unit.attackManager.setIdle(false, false);
             unit.attackManager.deactivate();
             return;
         }
         // Diverse Variablen sichern:
         Position realPos = unit.getMainPosition();
         GameObject victim = null;
         if (unit.attackManager.atkTarget != null && unit.attackManager.atkTarget.getLifeStatus() == GameObject.LIFESTATUS_ALIVE) {
             victim = unit.attackManager.atkTarget;
         }
         // Zuerst schauen, ob die Einheit stehen bleiben kann:
         Unit potTarget = searchDirectEnemys(realPos, unit.getPlayerId());
         if (potTarget != null) {
             // Gefunden, die Angreifen
             unit.attackManager.attackUnit(potTarget);
             return;
         }
         // War leider nix, Schritt 2: Position bei dem alten Ziel
         // Dieser Schritt geht nur bei Unit als Angriffsziel, und nur, wenn das Ziel noch lebt
         if (victim != null && victim instanceof Unit) {
             Position pos = searchAtkPosForTarget((Unit) victim);
             if (pos != null) {
                 ialSendTo(unit, pos);
                 return;
             }
         }
         // Schritt 3: Anderes Ziel (Einheit) suchen
         int searchRange = 5;
         // Die 1.41 ist das umrechnen von Kreisen in Abstände
         if (unit.getRange() / 1.41 > 5) {
             searchRange = (int) (unit.getRange() / 1.41);
         }
         Position newtar = unit.meeleAttackableEnemyAroundMe(searchRange, inner);
         if (newtar != null) {
             Unit newatk = searchDirectEnemys(newtar, unit.getPlayerId());
             if (newatk != null) {
                 // Die nehmen und angreiffen!
                 ialSendTo(unit, newtar);
                 unit.attackManager.ialUnit(newatk);
                 return;
             }
         }
 
         // Nichts hat geklappt, warten und dann Client neu suchen lassen:
         unit.attackManager.setIdle(false, false);
         unit.attackManager.deactivate();
         timer.schedule(new TimerTask() {
 
             @Override
             public void run() {
                 if (!unit.attackManager.isActive()) {
                     unit.attackManager.setIdle(true, true);
                 }
             }
         }, 3000);
     } */
 
     /**
      * Sucht dem Angriffsbehaviour eine neue Position zum Angreiffen für das bisherige Ziel.
      * Wenn es keine gibt, wird ein neues Ziel gesucht, das angegriffen werden kann.
      * Wird auch nach dem Töten aufgerufen, sucht dann ein neues Ziel.
      * Eine der wichtigsten Methoden den Bewegungs/Angriffsystems, denn diese
      * Methode ermöglicht erstmals das automatische Weiterkämpfen ohne
      * Kollisionsprobleme!
      *
      * Diese Methode sucht nach folgendem Schema nach neuen Positionen/Angriffszielen:
      * 1. Wenn die Einheit so wie sie dasteht eine andere Angreiffen kann, wird das gemacht....
      * 
      * ... ansonsten wird das Client-Idle wieder aktiviert, soll der sich drum kümmern.
      * @param unit
      */
   /*  public void searchNewAtkPosForRange(final Unit unit) {
         // Es darf nur ein neues Ziel gesucht werden, wenn die Einheit gerade nicht mit wichtigerem Beschäftigt ist
         // Nicht während dem Bauen:
         ServerBehaviour constructing = unit.getServerBehaviour(5);
         if (constructing != null && constructing.isActive()) {
             // Angriffsverhalten abschalten
             unit.attackManager.setIdle(false, false);
             unit.attackManager.deactivate();
             return;
         }
         // Zuerst schauen, ob die Einheit stehen bleiben kann:
         Unit potTarget = unit.enemyInRangeAroundMe(inner);
         if (potTarget != null) {
             // Gefunden, die Angreifen
             unit.attackManager.attackUnit(potTarget);
             return;
         }
         // Nichts hat geklappt, den Client neu suchen lassen:
         unit.attackManager.setIdle(true, true);
     } */
 
     /**
      * Sucht um die gegebene Einheit nach freien Nahkampf-Angriffspositionen
      * @param target
      * @return
      */
  /*   private Position searchAtkPosForTarget(Unit target) {
         return target.getMainPosition().aroundMe(1, inner, 8);
 
     } */
 
     /**
      * Sucht in der direkten Nahkampf-Umgebung nach möglichen Zielen, die
      * ohne Bewegung erreichbar sind.
      * @param pos
      * @param playerId
      * @return
      */
  /*   private Unit searchDirectEnemys(Position pos, int playerId) {
         // 8 Felder absuchen
         int px = pos.getX();
         int py = pos.getY();
         for (int i = 0; i < 8; i++) {
             int x = px;
             int y = py;
             switch (i) {
                 case 0:
                     y -= 2;
                     break;
                 case 1:
                     y--;
                     x--;
                     break;
                 case 2:
                     x -= 2;
                     break;
                 case 3:
                     x--;
                     y++;
                     break;
                 case 4:
                     y += 2;
                     break;
                 case 5:
                     x++;
                     y++;
                     break;
                 case 6:
                     x += 2;
                     break;
                 case 7:
                     x++;
                     y--;
                     break;
             }
             Unit unit = inner.netmap.getEnemyUnitRef(x, y, playerId);
             if (unit != null) {
                 return unit;
             }
         }
         return null;
     } */
 
     /**
      * Reserviert das Ziel für die Anstehende Bewegung. Überschreibt vorhandene
      * ohne Check, benachrichtigt aber das "Opfer"
      * @param mover Die Einheit, für die Reserviert wird
      * @param target Das zu reservierende Ziel-Feld
      * @param time Der Zeitpunkt, zu dem die Einheit ankommen wird (und daher die Reservierung abläuft)
      */
     private void reserveTarget(Unit mover, long time, Position target) {
         inner.netmap.setFieldReserved(target, time + 200, mover); // 200 müssen dazugerechnet werden, da erst dann das Behaviour die Collision setzen kann
     }
 
     /**
      * Sendet eine Einheit zu einer bestimmten Position.
      * Darf nur vom IAL-System verwendet werden, es muss garantiert sein, dass:
      * die Position frei ist und die Reservierung in Ordnung geht.
      * @param unit
      * @param to
      */
  /*   private synchronized void ialSendTo(Unit unit, Position to) {
 
         // aktuelle Position freimachen:
         inner.netmap.setCollision(unit.getMainPosition(), collision.free);
         if (unit.equals(inner.netmap.getUnitRef(unit.getMainPosition(), unit.getPlayerId()))) {
             inner.netmap.setUnitRef(unit.getMainPosition(), null, unit.getPlayerId());
         }
         //Bewegung initialisieren:
 
         // Weg suchen
         ArrayList<Position> newpath = null;
         newpath = inner.pathfinder.findPath(unit, to, true);
         System.out.println("AddMe: Check for SWITCH");
 
         /*} else {
         if (unit.path.indexOf(unit.getMainPosition()) + 1 != unit.path.size()) {
         newpath = inner.pathfinder.findPath(unit.path.get(unit.lastwaypoint + 1), to, unit.getPlayerId(), true);
         } else {
         System.out.println("FixMe: ialERROR switching Path! 1");
         }
         } */
 
    /*     if (newpath == null) {
             System.out.println("FixMoveMan: IAL-SendTo, no path!");
             return;
         }
         // Wenn wir uns nicht bewegen, dann einfach Pfad nehmen und losrennen
         // IAL-Bewegungen sind fliehen, damit keine anderen Ziele gesucht werden
         this.directMoveUnit(newpath, unit, false);
 
         /*  } else {
         try {
         // Bewegt sich gerade, neuen Pfad an nächsten Wegpunkt anhängen.
         // Wir löschen alle Wegpunkte nach dem Nächsten:
         newpath = saveSubList(unit.path, unit.lastwaypoint, newpath);
 
         // Altes Ziel freigeben
         inner.netmap.deleteFieldReservation(unit.movingtarget);
 
         // IAL ist fliehen
         this.attachMoveUnit(newpath, unit, false, true);
 
         } catch (Exception ex) {
         // Relativ Wichtig.
         System.out.println("FixMe: ialERROR switching Path! 2");
         inner.logger(ex);
         }
 
         } 
     } */
 
     /**
      * Bewegung initialisieren & an Clients schicken.
      * Ziel wird reserviert, und einfach überschrieben
      * Einheit darf sich nicht bewegen
      * @param path
      * @param unit
      */
     private synchronized void directMoveUnit(ArrayList<Position> path, Unit unit, boolean compressed) {
         // Bauen abbrechen:
         ServerBehaviour cnstrct = unit.getServerBehaviour(5);
         if (cnstrct != null && cnstrct.isActive()) {
             cnstrct.deactivate();
         }
         // Erst broadcasten, dann selber verarbeiten, denn senden kann lang dauern
 
         inner.netctrl.broadcastMove(unit.netID, path, false);
         // Komprimierten Pfad auspacken
         if (compressed) {
             inner.extractPath(path);
         }
         // Kollision umstellen
         inner.netmap.releasePosition(unit);
         unit.applyNewPath(inner, path);
 
       /*  unit.movingtarget = path.get(path.size() - 1);
         unit.position = path.get(0);
         unit.path = path;
         unit.order = orders.move;
         unit.lastwaypoint = 0;
         unit.calcWayLength();
         unit.nextWayPointDist = unit.pathOrder.get(1);
         unit.startTime = System.currentTimeMillis();
 
         this.reserveTarget(unit, (long) (1000.0 * unit.length / unit.speed), unit.movingtarget);
 
         // Ist diese Bewegung ein Fliehen?
         if (flee) {
             unit.moveManager.fleeing = true;
         } else {
             unit.moveManager.fleeing = false;
         }
 
         // Angriffsverhalten umstellen?
         if (flee) {
             unit.attackManager.setIdle(false, false);
             unit.attacktarget = null;
             unit.attackManager.deactivate();
             // Dem Client für Debug mitteilen
             if (inner.isInDebugMode()) {
                 inner.netctrl.broadcastDATA(inner.packetFactory((byte) 13, unit.netID, 0, 0, 0));
             }
         } else {
             unit.attackManager.setIdle(true, false);
         }*/
 
     }
 
  /*   private void attachMoveUnit(ArrayList<Position> completePath, Unit unit, boolean compressed, boolean flee) {
         if (completePath == null) {
             return;
         }
         // Bauen abbrechen:
         ServerBehaviour cnstrct = unit.getbehaviourS(5);
         if (cnstrct != null && cnstrct.isActive()) {
             cnstrct.deactivate();
         }
         // Erst broadcasten, dann selber verarbeiten, denn senden kann lang dauern
 
         synchronized (unit.pathSync) {
 
             inner.netctrl.broadcastMove(unit.netID, completePath, true);
             // Komprimierten Pfad auspacken
             if (compressed) {
                 inner.extractPath(completePath);
             }
             unit.movingtarget = completePath.get(completePath.size() - 1);
             unit.path = completePath;
             unit.calcWayLength();
 
             this.reserveTarget(unit, (long) (1000.0 * unit.length / unit.speed), unit.movingtarget);
 
             // Ist diese Bewegung ein Fliehen?
             if (flee) {
                 unit.moveManager.fleeing = true;
             } else {
                 unit.moveManager.fleeing = false;
             }
 
             // Angriffsystem umschalten?
             if (flee) {
                 unit.attackManager.setIdle(false, false);
                 unit.attacktarget = null;
                 unit.attackManager.deactivate();
                 // Dem Client für Debug mitteilen
                 if (inner.isInDebugMode()) {
                     inner.netctrl.broadcastDATA(inner.packetFactory((byte) 13, unit.netID, 0, 0, 0));
                 }
             } else {
                 unit.attackManager.setIdle(true, false);
             }
 
             unit.moveManager.activate();
         }
     } */
 
     public static ArrayList<Position> saveSubList(ArrayList<Position> oldpath, int lastwaypoint, ArrayList<Position> newpath) {
         if (oldpath.size() > lastwaypoint + 1) {
             oldpath = new ArrayList<Position>(oldpath.subList(0, lastwaypoint + 1));
         }
         oldpath.addAll(newpath);
         return oldpath;
     }
 
     /**
      * Intelligente Feldzuteilung beim Angriff auf Einheiten.
      * Sendet alle Einheiten in der selected-ArrayList auf eine möglichst intelligente Angriffs-Position
      * Dieses Zuteilungssystem ist weit besser als ein position.aroundMe(), weil:
      * - Berücksichtigung der Range
      * - Bessere Ausrichtung
      * - Umrunden der Einheit bei vielen Angreifern
      * - Zuordnung nicht nur auf Ecken, sondern auch auf Seiten
      * - Automatische Zuordnung zur richtigen Ecke/Seite
      *
      * @param victim Die Einheit, das Angegriffen werden soll
      */
    /* private void intelligentAttack(Unit victim, ArrayList<Unit> selected) {
         // Wir brauchen einen Liste mit Units:
         ArrayList<Unit> ial = new ArrayList<Unit>(selected.size());
         for (GameObject obj : selected) {
             Unit unit = (Unit) obj;
             ial.add(unit);
         }
         // Liste mit Feldern, die wir im Rahmen dieser IAL schon besetzt haben, erstellen
         ArrayList<Position> usedFields = new ArrayList<Position>();
         // Die sind fürs Kämpfen zu viel, die müssen warten
         ArrayList<Unit> store = new ArrayList<Unit>();
         // Fertig, Liste ist da.
         float bx = victim.getMainPosition().getX();
         float by = victim.getMainPosition().getY();
         // Gruppen anlegen
         // Leider erlaubt Java keine "generic array creation" a la ArrayList<RogUnit>[]
         ClientIALGroup[] groups = new ClientIALGroup[8];
         for (int q = 0; q < 8; q++) {
             groups[q] = new ClientIALGroup();
         }
         // Für jede Einheit Winkel berechnen und in Gruppen zuteilen
         for (Unit unit : ial) {
             float mx = unit.getMainPosition().getY();
             float my = unit.getMainPosition().getX();
             // Winkel berechnen:
             float deg = (float) Math.atan((mx - bx) / (by - my));
             // Bogenmaß in Grad umrechnen (Bogenmaß ist böse (!))
             deg = (float) (deg / Math.PI * 180);
             // In 360Grad System umrechnen (falls negativ)
             if (deg < 0) {
                 deg = 360 + deg;
             }
             // Winkel sind kleinstmöglich, wir brauchen aber einen vollen 360°-Umlauf
             if (mx > bx && my > by) {
                 deg -= 180;
             } else if (mx < bx && my > by) {
                 deg += 180;
             }
             if (deg == 0 || deg == -0) {
                 if (my > by) {
                     deg = 180;
                 }
             }
             if (deg < 22.5) {
                 // Ecke, nach unten
                 groups[0].add(unit);
             } else if (deg < 67.5) {
                 // Gerade, nach links unten
                 groups[1].add(unit);
             } else if (deg < 115.5) {
                 // Ecke, nach links
                 groups[2].add(unit);
             } else if (deg < 160.5) {
                 // Gerade, nach links oben
                 groups[3].add(unit);
             } else if (deg < 205.5) {
                 // Ecke, nach oben
                 groups[4].add(unit);
             } else if (deg < 250.5) {
                 // Gerade, nach rechts oben
                 groups[5].add(unit);
             } else if (deg < 295.5) {
                 // Ecke, nach rechts
                 groups[6].add(unit);
             } else if (deg < 340.5) {
                 // Gerade, nach rechts unten
                 groups[7].add(unit);
             } else {
                 // Nochmal Ecke nach unten
                 groups[0].add(unit);
             }
         }
         int run = 0;
         // Jetzt jede Gruppe einzeln Abhandeln
         for (int i = 0; i < 8; i++) {
             boolean again = false;
             if (i == 7) {
                 run++;
                 // Danach nochmal?
                 if (run < 4) {
                     again = true;
                 }
             }
             ClientIALGroup<Unit> units = (ClientIALGroup<Unit>) groups[i];
             // Zuteilung suchen (Ecke/Gerade(und welche?)
             Position vecD = null; // Normaler Schritt
             Position vecR = null; // Abweichung rechts
             Position vecL = null; // Abweichung links
             Position posG = victim.getMainPosition(); // Bezugsposition
             if (i == 0) {
                 // Ecke, nach unten
                 vecD = new Position(0, 2);
                 vecR = new Position(-1, 1);
                 vecL = new Position(1, 1);
             } else if (i == 1) {
                 // Gerade, nach links unten
                 vecD = new Position(-1, 1);
                 vecR = new Position(-1, -1);
                 vecL = new Position(1, 1);
             } else if (i == 2) {
                 // Ecke, nach links
                 vecD = new Position(-2, 0);
                 vecR = new Position(-1, -1);
                 vecL = new Position(-1, 1);
             } else if (i == 3) {
                 // Gerade, nach links oben
                 vecD = new Position(-1, -1);
                 vecR = new Position(1, -1);
                 vecL = new Position(-1, 1);
             } else if (i == 4) {
                 // Ecke, nach oben
                 vecD = new Position(0, -2);
                 vecR = new Position(1, -1);
                 vecL = new Position(-1, -1);
             } else if (i == 5) {
                 // Gerade, nach rechts oben
                 vecD = new Position(1, -1);
                 vecR = new Position(1, 1);
                 vecL = new Position(-1, -1);
             } else if (i == 6) {
                 // Ecke, nach rechts
                 vecD = new Position(2, 0);
                 vecR = new Position(1, 1);
                 vecL = new Position(1, -1);
             } else if (i == 7) {
                 // Gerade, nach rechts unten
                 vecD = new Position(1, 1);
                 vecR = new Position(-1, 1);
                 vecL = new Position(1, -1);
             }
             // Alle Units löschen, die schon in Reichweite stehen
             for (int p = 0; p < units.size(); p++) {
                 Unit unit = units.get(p);
                 Position tpos = unit.getMainPosition();
                 if (tpos.getDistance(posG) <= unit.getRange()) {
                     units.remove(p);
                     p--;
                 }
             }
             // Überhaupt noch welche da
             if (units.isEmpty()) {
                 continue;
             }
             // Ausgansfeld finden (Zielfeld der ersten Einheit)
             // Einheiten ihrer Reichweite nach sortieren: (nah zuerst)
             Collections.sort(units, new Comparator<Unit>() {
 
                 @Override
                 public int compare(Unit o1, Unit o2) {
                     if (o1.getRange() > o2.getRange()) {
                         return 1;
                     } else if (o1.getRange() < o2.getRange()) {
                         return -1;
                     } else {
                         return 0;
                     }
                 }
             });
             // Range holen
             double range = units.get(0).getRange();
             Position opVecD = new Position(vecD.getX() * -1, vecD.getY() * -1);
             Position posO = null;
             try {
                 posO = posG.clone();
             } catch (CloneNotSupportedException ex) {
                 System.out.println("Critical (clone) Error, aborting");
                 return;
             }
             int count = 0;
             while (count++ < 30) {
                 // Feld für Feld hoch marschieren, abbrechen, sobald außer Range
                 posO = posO.add(opVecD);
                 if (posG.getDistance(posO) > range) {
                     posO = posO.subtract(opVecD);
                     break;
                 }
             }
 
             // Jetzt mit der Verteilung beginnen
             // Alle Positionen freimachen
             for (int a = 0; a < units.size(); a++) {
                 inner.netmap.setCollision(units.get(a).getMainPosition(), collision.free);
             }
 
             // Erste Einheit kommt auf die Startposition
             double lastrange = units.get(0).getRange();
             if (!inner.netmap.isGroundColliding(posO) && !usedFields.contains(posO)) {
                 ialSendTo(units.get(0), posO);
                 usedFields.add(posO);
                 units.remove(0);
             }
             // Jetzt immer abwechseln links und rechts springen
             Position jumpPos = null;
             try {
                 jumpPos = posO.clone();
             } catch (CloneNotSupportedException ex) {
                 System.out.println("Critical (clone) Error, aborting");
                 return;
             }
             int counter = 0;
             while (!units.isEmpty()) {
                 counter++;
                 Unit unit = units.get(0);
                 // Wenn sich die Range ändert eventuell neu losrennen
                 if (unit.getRange() != lastrange) {
                     lastrange = unit.getRange();
                     range = lastrange;
                     try {
                         posO = posG.clone();
                     } catch (CloneNotSupportedException ex) {
                         System.out.println("Critical (clone) Error, aborting");
                         return;
                     }
                     int recount = 0;
                     while (recount++ < 30) {
                         // Feld für Feld hoch marschieren, abbrechen, sobald außer Range
                         posO = posO.add(opVecD);
                         if (posG.getDistance(posO) > range) {
                             posO = posO.subtract(opVecD);
                             break;
                         }
                     }
                     // Gefunden, diese Einheit da hin
                     if (!inner.netmap.isGroundColliding(posO) && !usedFields.contains(posO)) {
                         ialSendTo(units.get(0), posO);
                         usedFields.add(posO);
                         units.remove(0);
                     }
                     // Zurücksetzen
                     counter = 0;
                     try {
                         jumpPos = posO.clone();
                     } catch (CloneNotSupportedException ex) {
                         System.out.println("Critical (clone) Error, aborting");
                         return;
                     }
                 } else {
                     // Rechts oder links?
                     if (counter % 2 == 1) {
                         // Ungerade = Rechts
                         // Wie oft?
                         int jumps = counter - ((counter - 1) / 2);
                         // Jetzt so oft jumpen
                         Position potTarget = jumpPos.add(new Position(vecR.getX() * jumps, vecR.getY() * jumps));
                         // Position ok?
                         if (posG.getDistance(potTarget) > range) {
                             // In den nächsten Kreis jumpen - falls es den gibt
                             jumpPos = jumpPos.add(vecD);
                             counter = 0;
                             if (jumpPos.equals(posG)) {
                                 // Ende dieses Systems erreicht.
                                 store.addAll(wearLevelUnits(units, groups, i));
                                 units.clear();
                                 break;
                             }
                             if (!inner.netmap.isGroundColliding(jumpPos) && !usedFields.contains(jumpPos)) {
                                 ialSendTo(unit, jumpPos);
                                 usedFields.add(jumpPos);
                                 units.remove(0);
                             }
                         } else {
                             if (!inner.netmap.isGroundColliding(potTarget) && !usedFields.contains(potTarget)) {
                                 ialSendTo(unit, potTarget);
                                 usedFields.add(potTarget);
                                 units.remove(0);
                             }
                         }
                     } else {
                         // Gerade = Links
                         // Wie oft?
                         int jumps = (counter - 1) - ((counter - 2) / 2);
                         // Jetzt so oft jumpen
                         Position potTarget = jumpPos.add(new Position(vecL.getX() * jumps, vecL.getY() * jumps));
                         // Position ok?
                         if (posG.getDistance(potTarget) > range) {
                             // In den nächsten Kreis jumpen - falls es den gibt
                             jumpPos = jumpPos.add(vecD);
                             counter = 1;
                             if (jumpPos.equals(posG)) {
                                 // Ende dieses Systems erreicht.
                                 store.addAll(wearLevelUnits(units, groups, i));
                                 units.clear();
                                 break;
                             }
                             if (!inner.netmap.isGroundColliding(jumpPos) && !usedFields.contains(jumpPos)) {
                                 ialSendTo(unit, jumpPos);
                                 usedFields.add(jumpPos);
                                 units.remove(0);
                             }
                         } else {
                             if (!inner.netmap.isGroundColliding(potTarget) && !usedFields.contains(potTarget)) {
                                 ialSendTo(unit, potTarget);
                                 usedFields.add(potTarget);
                                 units.remove(0);
                             }
                         }
                     }
                 }
             }
             if (i == 7) {
                 // Nochmal?
                 if (again) {
                     i = -1;
                 }
             }
         }
         // Jetzt die Einheiten, die im Store-Array sind hinten hinstellen. Die Kämpfen erstmal nicht mit, sollen aber in der Nähe sein.
         // Also jetzt versuchen, die möglichst gut zu verteilen
         int grp = -1;
         while (!store.isEmpty()) {
             // Oben anfangen (Pfusch)
             grp++;
             if (grp == 8) {
                 grp = 0;
             }
             ClientIALGroup group = groups[grp];
             group.addStore(store.remove(0));
         }
         // Jetzt IAL-Mäßig verteilen
         for (int i = 0; i < 8; i++) {
             int kreis = 0;
             ArrayList<Unit> units = groups[i].getStored();
             // Zuteilung suchen (Ecke/Gerade(und welche?)
             Position vecD = null; // Normaler Schritt
             Position vecR = null; // Abweichung rechts
             Position vecL = null; // Abweichung links
             Position posG = victim.getMainPosition(); // Bezugsposition des Gebäudes
             if (i == 0) {
                 // Ecke, nach unten
                 vecD = new Position(0, 2);
                 vecR = new Position(-1, 1);
                 vecL = new Position(1, 1);
             } else if (i == 1) {
                 // Gerade, nach links unten
                 vecD = new Position(-1, 1);
                 vecR = new Position(-1, -1);
                 vecL = new Position(1, 1);
             } else if (i == 2) {
                 // Ecke, nach links
                 vecD = new Position(-2, 0);
                 vecR = new Position(-1, -1);
                 vecL = new Position(-1, 1);
             } else if (i == 3) {
                 // Gerade, nach links oben
                 vecD = new Position(-1, -1);
                 vecR = new Position(1, -1);
                 vecL = new Position(-1, 1);
             } else if (i == 4) {
                 // Ecke, nach oben
                 vecD = new Position(0, -2);
                 vecR = new Position(1, -1);
                 vecL = new Position(-1, -1);
             } else if (i == 5) {
                 // Gerade, nach rechts oben
                 vecD = new Position(1, -1);
                 vecR = new Position(1, 1);
                 vecL = new Position(-1, -1);
             } else if (i == 6) {
                 // Ecke, nach rechts
                 vecD = new Position(2, 0);
                 vecR = new Position(1, 1);
                 vecL = new Position(1, -1);
             } else if (i == 7) {
                 // Gerade, nach rechts unten
                 vecD = new Position(1, 1);
                 vecR = new Position(-1, 1);
                 vecL = new Position(1, -1);
             }
             // Überhaupt welche da?
             if (units.isEmpty()) {
                 continue;
             }
             // Range holen
             double range = units.get(0).getRange();
             Position opVecD = new Position(vecD.getX() * -1, vecD.getY() * -1);
             Position posO = null;
             try {
                 posO = posG.clone();
             } catch (CloneNotSupportedException ex) {
                 System.out.println("Critical (clone) Error, aborting");
                 return;
             }
             int count = 0;
             while (count++ < 30) {
                 // Feld für Feld hoch marschieren, abbrechen, sobald außer Range
                 posO = posO.add(opVecD);
                 if (posG.getDistance(posO) > range) {
                     break;
                 }
             }
             // Erste Einheit kommt auf die Startposition
             if (!inner.netmap.isGroundColliding(posO) && !usedFields.contains(posO)) {
                 ialSendTo(units.get(0), posO);
                 usedFields.add(posO);
                 units.remove(0);
             }
             // Schätzwert für Kreissprunggrenze ermitteln
             kreis = (int) (1 + posG.getDistance(posO));
             // Jetzt immer abwechseln links und rechts springen
             Position jumpPos = null;
             try {
                 jumpPos = posO.clone();
             } catch (CloneNotSupportedException ex) {
                 System.out.println("Critical (clone) Error, aborting");
                 return;
             }
             int counter = 0;
             while (!units.isEmpty()) {
                 counter++;
                 Unit unit = units.get(0);
                 // Rechts oder links?
                 if (counter % 2 == 1) {
                     // Ungerade = Rechts
                     // Wie oft?
                     int jumps = counter - ((counter - 1) / 2);
                     // Zu viele Jumps?
                     if (jumps > kreis) {
                         // In den nächsten Kreis jumpen - falls es den gibt
                         jumpPos = jumpPos.add(opVecD);
                         counter = 0;
                         kreis++;
                         if (!inner.netmap.isGroundColliding(jumpPos) && !usedFields.contains(jumpPos)) {
                             ialSendTo(unit, jumpPos);
                             usedFields.add(jumpPos);
                             units.remove(0);
                         }
                     } else {
                         // Jetzt so oft jumpen
                         Position potTarget = jumpPos.add(new Position(vecR.getX() * jumps, vecR.getY() * jumps));
                         if (!inner.netmap.isGroundColliding(potTarget) && !usedFields.contains(potTarget)) {
                             ialSendTo(unit, potTarget);
                             usedFields.add(potTarget);
                             units.remove(0);
                         }
                     }
                 } else {
                     // Gerade = Links
                     // Wie oft?
                     int jumps = (counter - 1) - ((counter - 2) / 2);
                     // Zu viele Jumps?
                     if (jumps > kreis) {
                         // In den nächsten Kreis jumpen - falls es den gibt
                         jumpPos = jumpPos.add(vecD);
                         counter = 1;
                         kreis++;
                         if (!inner.netmap.isGroundColliding(jumpPos) && !usedFields.contains(jumpPos)) {
                             ialSendTo(unit, jumpPos);
                             usedFields.add(jumpPos);
                             units.remove(0);
                         }
                     } else {
                         // Jetzt so oft jumpen
                         Position potTarget = jumpPos.add(new Position(vecL.getX() * jumps, vecL.getY() * jumps));
                         if (!inner.netmap.isGroundColliding(potTarget) && !usedFields.contains(potTarget)) {
                             ialSendTo(unit, potTarget);
                             usedFields.add(potTarget);
                             units.remove(0);
                         }
                     }
                 }
             }
 
         }
     } */
 
     /**
      * Intelligente Feldzuteilung beim Angriff auf Gebäude.
      * Sendet alle Einheiten in der selected-ArrayList auf eine möglichst intelligente Angriffs-Position
      * Dieses Zuteilungssystem ist weit besser als ein getBestEdge().aroundMe(), weil:
      * - Berücksichtigung der Range
      * - Bessere Ausrichtung
      * - Umrunden des Gebäudes bei vielen Angreifern
      * - Zuordnung nicht nur auf Ecken, sondern auch auf Seiten
      * - Automatische Zuordnung zur richtigen Ecke/Seite
      *
      * @param victim Das Gebäude, das Angegriffen werden soll
      */
   /*  private void intelligentAttack(Building victim, ArrayList<Unit> selected) {
         // Wir brauchen einen Liste mit Units:
         ArrayList<Unit> ial = new ArrayList<Unit>(selected.size());
         for (GameObject obj : selected) {
             Unit unit = (Unit) obj;
             ial.add(unit);
         }
         // Liste mit Feldern, die wir im Rahmen dieser IAL schon besetzt haben, erstellen
         ArrayList<Position> usedFields = new ArrayList<Position>();
         // Die sind fürs Kämpfen zu viel, die müssen warten
         ArrayList<Unit> store = new ArrayList<Unit>();
         // Fertig, Liste ist da.
         //Gebäude-Mitte finden:
         float bx = 0;
         float by = 0;
         //Z1
         //Einfach die Hälfte als Mitte nehmen
         bx = victim.getMainPosition().getX() + ((victim.getZ1() - 1) * 1.0f / 2);
         by = victim.getMainPosition().getY() - ((victim.getZ1() - 1) * 1.0f / 2);
         //Z2
         // Einfach die Hälfte als Mitte nehmen
         bx += ((victim.getZ2() - 1) * 1.0f / 2);
         by += ((victim.getZ2() - 1) * 1.0f / 2);
         // Gebäude-Mitte gefunden
         // Gruppen anlegen
         // Leider erlaubt Java keine "generic array creation" a la ArrayList<RogUnit>[]
         ClientIALGroup[] groups = new ClientIALGroup[8];
         for (int q = 0; q < 8; q++) {
             groups[q] = new ClientIALGroup();
         }
         // Für jede Einheit Winkel berechnen und in Gruppen zuteilen
         for (Unit unit : ial) {
             float mx = unit.getMainPosition().getX();
             float my = unit.getMainPosition().getY();
             // Winkel berechnen:
             float deg = (float) Math.atan((mx - bx) / (by - my));
             // Bogenmaß in Grad umrechnen (Bogenmaß ist böse (!))
             deg = (float) (deg / Math.PI * 180);
             // In 360Grad System umrechnen (falls negativ)
             if (deg < 0) {
                 deg = 360 + deg;
             }
             // Winkel sind kleinstmöglich, wir brauchen aber einen vollen 360°-Umlauf
             if (mx > bx && my > by) {
                 deg -= 180;
             } else if (mx < bx && my > by) {
                 deg += 180;
             }
             if (deg == 0 || deg == -0) {
                 if (my > by) {
                     deg = 180;
                 }
             }
             if (deg < 22.5) {
                 // Ecke, nach unten
                 groups[0].add(unit);
             } else if (deg < 67.5) {
                 // Gerade, nach links unten
                 groups[1].add(unit);
             } else if (deg < 115.5) {
                 // Ecke, nach links
                 groups[2].add(unit);
             } else if (deg < 160.5) {
                 // Gerade, nach links oben
                 groups[3].add(unit);
             } else if (deg < 205.5) {
                 // Ecke, nach oben
                 groups[4].add(unit);
             } else if (deg < 250.5) {
                 // Gerade, nach rechts oben
                 groups[5].add(unit);
             } else if (deg < 295.5) {
                 // Ecke, nach rechts
                 groups[6].add(unit);
             } else if (deg < 340.5) {
                 // Gerade, nach rechts unten
                 groups[7].add(unit);
             } else {
                 // Nochmal Ecke nach unten
                 groups[0].add(unit);
             }
         }
         // Einheiten zugeteilt
         int run = 0;
         // Jetzt jede Gruppe einzeln Abhandeln
         for (int i = 0; i < 8; i++) {
             boolean again = false;
             if (i == 7) {
                 run++;
                 // Danach nochmal?
                 if (run < 2) {
                     again = true;
                 }
             }
             ClientIALGroup<Unit> units = (ClientIALGroup<Unit>) groups[i];
             // Zuteilung suchen (Ecke/Gerade(und welche?)
             Position vecD = null; // Normaler Schritt
             Position vecR = null; // Abweichung rechts
             Position vecL = null; // Abweichung links
             Position posG = null; // Bezugsposition des Gebäudes
             if (i == 0) {
                 // Ecke, nach unten
                 vecD = new Position(0, 2);
                 vecR = new Position(-1, 1);
                 vecL = new Position(1, 1);
                 posG = new Position(victim.getMainPosition().getX() + (victim.getZ1() - 1), victim.getMainPosition().getY() - (victim.getZ1() - 1));
             } else if (i == 1) {
                 // Gerade, nach links unten
                 vecD = new Position(-1, 1);
                 vecR = new Position(-1, -1);
                 vecL = new Position(1, 1);
                 posG = new Position(victim.getMainPosition().getX() + (victim.getZ1() - 1) + ((victim.getZ2() - 1) / 2), victim.getMainPosition().getY() - (victim.getZ1() - 1) + ((victim.getZ2() - 1) / 2));
             } else if (i == 2) {
                 // Ecke, nach links
                 vecD = new Position(-2, 0);
                 vecR = new Position(-1, -1);
                 vecL = new Position(-1, 1);
                 posG = new Position(victim.getMainPosition().getX() + (victim.getZ1() - 1) + (victim.getZ2() - 1), victim.getMainPosition().getY() - (victim.getZ1() - 1) + (victim.getZ2() - 1));
             } else if (i == 3) {
                 // Gerade, nach links oben
                 vecD = new Position(-1, -1);
                 vecR = new Position(1, -1);
                 vecL = new Position(-1, 1);
                 posG = new Position(victim.getMainPosition().getX() + ((victim.getZ1() - 1) / 2) + (victim.getZ2() - 1), victim.getMainPosition().getY() - ((victim.getZ1() - 1) / 2) + (victim.getZ2() - 1));
             } else if (i == 4) {
                 // Ecke, nach oben
                 vecD = new Position(0, -2);
                 vecR = new Position(1, -1);
                 vecL = new Position(-1, -1);
                 posG = new Position(victim.getMainPosition().getX() + (victim.getZ2() - 1), victim.getMainPosition().getY() + (victim.getZ2() - 1));
             } else if (i == 5) {
                 // Gerade, nach rechts oben
                 vecD = new Position(1, -1);
                 vecR = new Position(1, 1);
                 vecL = new Position(-1, -1);
                 posG = new Position(victim.getMainPosition().getX() + ((victim.getZ2() - 1) / 2), victim.getMainPosition().getY() + ((victim.getZ2() - 1) / 2));
             } else if (i == 6) {
                 // Ecke, nach rechts
                 vecD = new Position(2, 0);
                 vecR = new Position(1, 1);
                 vecL = new Position(1, -1);
                 posG = victim.getMainPosition();
             } else if (i == 7) {
                 // Gerade, nach rechts unten
                 vecD = new Position(1, 1);
                 vecR = new Position(-1, 1);
                 vecL = new Position(1, -1);
                 posG = new Position(victim.getMainPosition().getX() + ((victim.getZ1() - 1) / 2), victim.getMainPosition().getY() - ((victim.getZ1() - 1) / 2));
             }
             // Alle Units löschen, die schon in Reichweite stehen
             for (int p = 0; p < units.size(); p++) {
                 Unit unit = units.get(p);
                 Position tpos = unit.getMainPosition();
                 if (tpos.getDistance(posG) <= unit.getRange()) {
                     units.remove(p);
                     p--;
                 }
             }
             // Überhaupt noch welche da
             if (units.isEmpty()) {
                 continue;
             }
             // Ausgansfeld finden (Zielfeld der ersten Einheit)
             // Einheiten ihrer Reichweite nach sortieren: (nah zuerst)
             Collections.sort(units, new Comparator<Unit>() {
 
                 @Override
                 public int compare(Unit o1, Unit o2) {
                     if (o1.getRange() > o2.getRange()) {
                         return 1;
                     } else if (o1.getRange() < o2.getRange()) {
                         return -1;
                     } else {
                         return 0;
                     }
                 }
             });
             // Range holen
             double range = units.get(0).getRange();
             Position opVecD = new Position(vecD.getX() * -1, vecD.getY() * -1);
             Position posO = null;
             try {
                 posO = posG.clone();
             } catch (CloneNotSupportedException ex) {
                 System.out.println("Critical (clone) Error, aborting");
                 return;
             }
             int count = 0;
             while (count++ < 30) {
                 // Feld für Feld hoch marschieren, abbrechen, sobald außer Range
                 posO = posO.add(opVecD);
                 if (posG.getDistance(posO) > range) {
                     posO = posO.subtract(opVecD);
                     break;
                 }
             }
 
             // Jetzt mit der Verteilung beginnen
             // Alle Positionen freimachen
             for (int a = 0; a < units.size(); a++) {
                 inner.netmap.setCollision(units.get(a).getMainPosition(), collision.free);
             }
 
             double lastrange = units.get(0).getRange();
             // Erste Einheit kommt auf die Startposition
             if (!inner.netmap.isGroundColliding(posO) && !usedFields.contains(posO)) {
                 ialSendTo(units.get(0), posO);
                 usedFields.add(posO);
                 units.remove(0);
             }
             // Jetzt immer abwechseln links und rechts springen
             Position jumpPos = null;
             try {
                 jumpPos = posO.clone();
             } catch (CloneNotSupportedException ex) {
                 System.out.println("Critical (clone) Error, aborting");
                 return;
             }
             int counter = 0;
             while (!units.isEmpty()) {
                 counter++;
                 Unit unit = units.get(0);
                 // Wenn sich die Range ändert eventuell neu losrennen
                 if (unit.getRange() != lastrange) {
                     lastrange = unit.getRange();
                     range = lastrange;
                     try {
                         posO = posG.clone();
                     } catch (CloneNotSupportedException ex) {
                         System.out.println("Critical (clone) Error, aborting");
                         return;
                     }
                     int recount = 0;
                     while (recount++ < 30) {
                         // Feld für Feld hoch marschieren, abbrechen, sobald außer Range
                         posO = posO.add(opVecD);
                         if (posG.getDistance(posO) > range) {
                             posO = posO.subtract(opVecD);
                             break;
                         }
                     }
                     // Gefunden, diese Einheit da hin
                     if (!inner.netmap.isGroundColliding(posO) && !usedFields.contains(posO)) {
                         ialSendTo(units.get(0), posO);
                         usedFields.add(posO);
                         // Die ist damit abgehandelt
                         units.remove(0);
                     }
                     // Zurücksetzen
                     counter = 0;
                     try {
                         jumpPos = posO.clone();
                     } catch (CloneNotSupportedException ex) {
                         System.out.println("Critical (clone) Error, aborting");
                         return;
                     }
                 } else {
                     // Rechts oder links?
                     if (counter % 2 == 1) {
                         // Ungerade = Rechts
                         // Wie oft?
                         int jumps = counter - ((counter - 1) / 2);
                         // Jetzt so oft jumpen
                         Position potTarget = jumpPos.add(new Position(vecR.getX() * jumps, vecR.getY() * jumps));
                         // Position ok?
                         if (posG.getDistance(potTarget) > range) {
                             // In den nächsten Kreis jumpen - falls es den gibt
                             jumpPos = jumpPos.add(vecD);
                             counter = 0;
                             if (jumpPos.equals(posG)) {
                                 // Ende dieses Systems erreicht.
                                 store.addAll(wearLevelUnits(units, groups, i));
                                 units.clear();
                                 break;
                             }
                             if (!inner.netmap.isGroundColliding(jumpPos) && !usedFields.contains(jumpPos)) {
                                 ialSendTo(unit, jumpPos);
                                 usedFields.add(jumpPos);
                                 units.remove(0);
                             }
                         } else {
                             if (!inner.netmap.isGroundColliding(potTarget) && !usedFields.contains(potTarget)) {
                                 ialSendTo(unit, potTarget);
                                 units.remove(0);
                             }
                         }
                     } else {
                         // Gerade = Links
                         // Wie oft?
                         int jumps = (counter - 1) - ((counter - 2) / 2);
                         // Jetzt so oft jumpen
                         Position potTarget = jumpPos.add(new Position(vecL.getX() * jumps, vecL.getY() * jumps));
                         // Position ok?
                         if (posG.getDistance(potTarget) > range) {
                             // In den nächsten Kreis jumpen - falls es den gibt
                             jumpPos = jumpPos.add(vecD);
                             counter = 1;
                             if (jumpPos.equals(posG)) {
                                 // Ende dieses Systems erreicht.
                                 store.addAll(wearLevelUnits(units, groups, i));
                                 units.clear();
                                 break;
                             }
                             if (!inner.netmap.isGroundColliding(jumpPos) && !usedFields.contains(jumpPos)) {
                                 ialSendTo(unit, jumpPos);
                                 usedFields.add(jumpPos);
                                 units.remove(0);
                             }
                         } else {
                             if (!inner.netmap.isGroundColliding(potTarget) && !usedFields.contains(potTarget)) {
                                 ialSendTo(unit, potTarget);
                                 usedFields.add(potTarget);
                                 units.remove(0);
                             }
                         }
                     }
                 }
             }
             if (i == 7) {
                 // Nochmal?
                 if (again) {
                     i = -1;
                 }
             }
         }
         // Jetzt die Einheiten, die im Store-Array sind hinten hinstellen. Die Kämpfen erstmal nicht mit, sollen aber in der Nähe sein.
         // Also jetzt versuchen, die möglichst gut zu verteilen
         int grp = -1;
         while (!store.isEmpty()) {
             // Oben anfangen (Pfusch)
             grp++;
             if (grp == 8) {
                 grp = 0;
             }
             ClientIALGroup group = groups[grp];
             group.addStore(store.remove(0));
         }
         // Jetzt IAL-Mäßig verteilen
         for (int i = 0; i < 8; i++) {
             int kreis = 0;
             ArrayList<Unit> units = groups[i].getStored();
             // Zuteilung suchen (Ecke/Gerade(und welche?)
             Position vecD = null; // Normaler Schritt
             Position vecR = null; // Abweichung rechts
             Position vecL = null; // Abweichung links
             Position posG = null; // Bezugsposition des Gebäudes
             if (i == 0) {
                 // Ecke, nach unten
                 vecD = new Position(0, 2);
                 vecR = new Position(-1, 1);
                 vecL = new Position(1, 1);
                 posG = new Position(victim.getMainPosition().getX() + (victim.getZ1() - 1), victim.getMainPosition().getY() - (victim.getZ1() - 1));
             } else if (i == 1) {
                 // Gerade, nach links unten
                 vecD = new Position(-1, 1);
                 vecR = new Position(-1, -1);
                 vecL = new Position(1, 1);
                 posG = new Position(victim.getMainPosition().getX() + (victim.getZ1() - 1) + ((victim.getZ2() - 1) / 2), victim.getMainPosition().getY() - (victim.getZ1() - 1) + ((victim.getZ2() - 1) / 2));
             } else if (i == 2) {
                 // Ecke, nach links
                 vecD = new Position(-2, 0);
                 vecR = new Position(-1, -1);
                 vecL = new Position(-1, 1);
                 posG = new Position(victim.getMainPosition().getX() + (victim.getZ1() - 1) + (victim.getZ2() - 1), victim.getMainPosition().getY() - (victim.getZ1() - 1) + (victim.getZ2() - 1));
             } else if (i == 3) {
                 // Gerade, nach links oben
                 vecD = new Position(-1, -1);
                 vecR = new Position(1, -1);
                 vecL = new Position(-1, 1);
                 posG = new Position(victim.getMainPosition().getX() + ((victim.getZ1() - 1) / 2) + (victim.getZ2() - 1), victim.getMainPosition().getY() - ((victim.getZ1() - 1) / 2) + (victim.getZ2() - 1));
             } else if (i == 4) {
                 // Ecke, nach oben
                 vecD = new Position(0, -2);
                 vecR = new Position(1, -1);
                 vecL = new Position(-1, -1);
                 posG = new Position(victim.getMainPosition().getX() + (victim.getZ2() - 1), victim.getMainPosition().getY() + (victim.getZ2() - 1));
             } else if (i == 5) {
                 // Gerade, nach rechts oben
                 vecD = new Position(1, -1);
                 vecR = new Position(1, 1);
                 vecL = new Position(-1, -1);
                 posG = new Position(victim.getMainPosition().getX() + ((victim.getZ2() - 1) / 2), victim.getMainPosition().getY() + ((victim.getZ2() - 1) / 2));
             } else if (i == 6) {
                 // Ecke, nach rechts
                 vecD = new Position(2, 0);
                 vecR = new Position(1, 1);
                 vecL = new Position(1, -1);
                 posG = victim.getMainPosition();
             } else if (i == 7) {
                 // Gerade, nach rechts unten
                 vecD = new Position(1, 1);
                 vecR = new Position(-1, 1);
                 vecL = new Position(1, -1);
                 posG = new Position(victim.getMainPosition().getX() + ((victim.getZ1() - 1) / 2), victim.getMainPosition().getY() - ((victim.getZ1() - 1) / 2));
             }
             // Überhaupt welche da?
             if (units.isEmpty()) {
                 continue;
             }
             // Range holen
             double range = units.get(0).getRange();
             Position opVecD = new Position(vecD.getX() * -1, vecD.getY() * -1);
             Position posO = null;
             try {
                 posO = posG.clone();
             } catch (CloneNotSupportedException ex) {
                 System.out.println("Critical (clone) Error, aborting");
                 return;
             }
             int count = 0;
             while (count++ < 30) {
                 // Feld für Feld hoch marschieren, abbrechen, sobald außer Range
                 posO = posO.add(opVecD);
                 if (posG.getDistance(posO) > range) {
                     break;
                 }
             }
             // Erste Einheit kommt auf die Startposition
             if (!inner.netmap.isGroundColliding(posO) && !usedFields.contains(posO)) {
                 ialSendTo(units.get(0), posO);
                 usedFields.add(posO);
                 units.remove(0);
             }
             // Schätzwert für Kreissprunggrenze ermitteln
             kreis = (int) (1 + posG.getDistance(posO));
             // Jetzt immer abwechseln links und rechts springen
             Position jumpPos = null;
             try {
                 jumpPos = posO.clone();
             } catch (CloneNotSupportedException ex) {
                 System.out.println("Critical (clone) Error, aborting");
                 return;
             }
             int counter = 0;
             while (!units.isEmpty()) {
                 counter++;
                 Unit unit = units.get(0);
                 // Rechts oder links?
                 if (counter % 2 == 1) {
                     // Ungerade = Rechts
                     // Wie oft?
                     int jumps = counter - ((counter - 1) / 2);
                     // Zu viele Jumps?
                     if (jumps > kreis) {
                         // In den nächsten Kreis jumpen - falls es den gibt
                         jumpPos = jumpPos.add(opVecD);
                         counter = 0;
                         kreis++;
                         if (!inner.netmap.isGroundColliding(jumpPos) && !usedFields.contains(jumpPos)) {
                             ialSendTo(unit, jumpPos);
                             usedFields.add(jumpPos);
                             units.remove(0);
                         }
                     } else {
                         // Jetzt so oft jumpen
                         Position potTarget = jumpPos.add(new Position(vecR.getX() * jumps, vecR.getY() * jumps));
                         if (!inner.netmap.isGroundColliding(potTarget) && !usedFields.contains(potTarget)) {
                             ialSendTo(unit, potTarget);
                             usedFields.add(potTarget);
                             units.remove(0);
                         }
                     }
                 } else {
                     // Gerade = Links
                     // Wie oft?
                     int jumps = (counter - 1) - ((counter - 2) / 2);
                     // Zu viele Jumps?
                     if (jumps > kreis) {
                         // In den nächsten Kreis jumpen - falls es den gibt
                         jumpPos = jumpPos.add(vecD);
                         counter = 1;
                         kreis++;
                         if (!inner.netmap.isGroundColliding(jumpPos) && !usedFields.contains(jumpPos)) {
                             ialSendTo(unit, jumpPos);
                             usedFields.add(jumpPos);
                             units.remove(0);
                         }
                     } else {
                         // Jetzt so oft jumpen
                         Position potTarget = jumpPos.add(new Position(vecL.getX() * jumps, vecL.getY() * jumps));
                         if (!inner.netmap.isGroundColliding(potTarget) && !usedFields.contains(potTarget)) {
                             ialSendTo(unit, potTarget);
                             usedFields.add(potTarget);
                             units.remove(0);
                         }
                     }
                 }
             }
 
         }
     } */
 
     /**
      * Verteilt überschüssige Einheiten auf andere Gruppen, soweit möglich
      * Dies geschieht nach folgenden Regeln:
      * Wenn andere Gruppen noch platz haben, dann gleichmäßig dort rein - möglichst Nah.
      * @param units
      * @param groups
      * @param current
      */
     private ArrayList<Unit> wearLevelUnits(ClientIALGroup<Unit> units, ClientIALGroup[] groups, int currentGroup) {
         // Sind wir schon beim Storen, oder versuchen wirs noch normal
         if (units.getMode() != ClientIALGroup.MODE_STORE) {
             // Auf OUTSOURCE stellen
             if (units.getMode() == ClientIALGroup.MODE_NORMAL) {
                 units.modeUp();
             }
             // Soviele wie wir hier hatten sollten etwa auch in die anderen reinpassen
             int max = units.getCounter();
             // Alle anderen Gruppen durchprobieren
             // Zuerst merken, wo wie viele reinpassen würden
             int[] moveMax = new int[8];
             for (int i = 0; i < 8; i++) {
                 if (i == currentGroup) {
                     // Da ist voll
                     moveMax[i] = 0;
                 } else {
                     if (groups[i].getMode() == ClientIALGroup.MODE_NORMAL) {
                         // Da geht noch was
                         moveMax[i] = max - groups[i].getCounter();
                         if (moveMax[i] < 0) {
                             moveMax[i] = 0;
                         }
                     } else {
                         moveMax[i] = 0;
                     }
                 }
             }
             // Jetzt wissen wir, wie viele wo reinpassen.
             // Also jetzt versuchen, die möglichst gut zu verteilen
             for (int tG = 1; tG < 5; tG++) {
                 int grp = currentGroup;
                 // Abwechselnd links und rechts
                 if (tG < 4) {
                     // Rechts anfangen
                     grp += tG;
                     if (grp > 7) {
                         grp -= 8;
                     }
                     ClientIALGroup<Unit> rechts = groups[grp];
                     int rechtsP = moveMax[grp];
                     // Links weitermachen
                     grp = currentGroup - tG;
                     if (grp < 0) {
                         grp += 8;
                     }
                     ClientIALGroup<Unit> links = groups[grp];
                     int linksP = moveMax[grp];
                     // Jetzt verteilen
                     // Wenn links und rechts zusammen nicht reichen, einfach komplett vollstopfen
                     if (rechtsP + linksP < units.size()) {
                         moveUnits(units, rechts, rechtsP);
                         moveUnits(units, links, linksP);
                         // Dann gehts in der nächsten Runde weiter
                     } else {
                         // Wenn links und rechts reicht, möglichst gleichmäßig verteilen
                         // Dazu einfach abwechseln, solange es geht
                         int stopp = 0;
                         while (!units.isEmpty() && stopp < 2) {
                             stopp = 0;
                             if (rechtsP-- > 0) {
                                 moveUnits(units, rechts, 1);
                             } else {
                                 stopp++;
                             }
                             if (linksP-- > 0 && !units.isEmpty()) {
                                 moveUnits(units, links, 1);
                             } else {
                                 stopp++;
                             }
                         }
                         break;
                     }
 
                 } else {
                     // Das Links/Rechts-Verfahren funktioniert natürlich beim letzten Feld nichtmehr, da ist ja nurnoch eines.
                     grp += tG;
                     if (grp > 7) {
                         grp -= 8;
                     }
                     ClientIALGroup<Unit> last = groups[grp];
                     if (moveMax[grp] < units.size()) {
                         moveUnits(units, last, moveMax[grp]);
                     } else {
                         moveUnits(units, last, units.size());
                     }
                 }
 
             }
         }
 
         // Jetzt wurden aller verteilt, die gut verteilbar waren
         return units;
     }
 
     /**
      * Interne Hilfsmethode für wearLevel (IAL)
      * Verschiebt Einheiten in eine andere Gruppe
      * @param from
      * @param to
      * @param number
      */
     private void moveUnits(ClientIALGroup from, ClientIALGroup to, int number) {
         for (; number > 0; number--) {
             to.add(from.outsource(0));
         }
     }
 }
