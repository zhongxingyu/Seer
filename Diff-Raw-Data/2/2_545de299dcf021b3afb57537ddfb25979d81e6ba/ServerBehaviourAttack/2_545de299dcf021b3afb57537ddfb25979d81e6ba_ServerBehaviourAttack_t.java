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
 
 import de._13ducks.cor.game.GameObject;
 import de._13ducks.cor.game.Moveable;
 import de._13ducks.cor.game.Unit;
 import de._13ducks.cor.game.server.Server;
 import de._13ducks.cor.game.server.ServerCore;
 import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Das Server-Angriffsbehaviour
  * Jede Einheit hat ihr eigenes.
  * Läuft normalerweise immer und koordiniert alles, was mit Angriff zu tun hat.
  * Also sowohl das Suchen und Verfolgen, als auch das reine Kämpfen.
  * Dieses Behaviour muss über alle Änderungen des Bewegungsmodus informiert werden, damit es sich entsprechend verhalten kann.
  * Das Behaviour hat Grundlegend 2 Modi:
  * - Normal. Die Einheit kämpft gerade, läuft zu einem Kampf oder steht herum und hält nach feinden Ausschau.
  * - F-Mode. (Flucht/Focus). Die Einheit läuft ohne sich zu wehren auf ihr Ziel zu. Dieser Modus wird automatisch verlassen, 
  *              wenn das Ziel erreicht/besiegt ist. Vorher nicht.
  * Intern gibt es noch mehr Modi, die aber an dieser Stelle nicht genauer erläutert werden.
  * Default ist Normal-Searching
  */
 public class ServerBehaviourAttack extends ServerBehaviour {
 
     /**
      * Der default-Zustand.
      * Die Einheit sucht nach Zielen in ihrer Umgebung.
      * Die Einheit tut dies normalerweise immer, auch beim normalen laufen.
      */
     private static final int SEARCHENEMY = 1;
     /**
      * Die Einheit kämpft gerade.
      * Konkret ist das Ziel in Reichweite, und die Einheit wartet darauf, erneut Schaden zufügen zu können.
      * In diesem Zustand steht die Einheit.
      * Ist das Ziel außer Reichweite, wird ein neues in Reichweite gesucht, wenn keins da ist, wird auf MODE_GOTO umgeschaltet und das Ziel verfolgt.
      */
     private static final int FIGHTING = 2;
     /**
      * Die Einheit kämpft gerade, läuft konkret gerade auf ihr Ziel zu.
      * Wechselt auf MODE_FIGHTING, wenn angekommen.
      * Die Einheit sucht in diesem Modus permanent alternative Ziele, die ohne weiteres Laufen verfügbar sind.
      * Sollte so eines gefunden werden, wird das Ziel gewechselt und auf MODE_FIGHTING umgeschaltet
      */
     private static final int GOTO = 3;
     /**
      * In diesem Modus setzt die Einheit alles daran, dieses Ziel anzugreiffen.
      * Das bedeutet: Sie verfolg diese Einheit, auch wenn andere in Reichweite wären.
      * Sie wehrt sich nicht, auch wenn die verfolgte Einheit weit weg ist.
      * Die Einheit wird dort hinlaufen und angreiffen, bis sie abgezogen wird.
      * Dieser Modus wechselt automatisch zurück zu MODE_SEARCHENEMY, wenn das Ziel besiegt ist.
      */
     private static final int FOCUS = 4;
     /**
      * Dies ist der Fluchtmodus. Die Einheit versucht, das Ziel um jeden Preis zu erreichen.
      * Die Einheit läuft nur direkt auf das Ziel zu und sucht auf dem Weg keine Ziele.
      * Die Einheit wird sich nicht verteidigen, wenn sie angegriffen wird.
      * Sobald das Ziel erreicht ist, schält die Einheit wieder auf MODE_SEARCHENEMY um.
      */
     private static final int FLEE = 5;
     /**
      * Dies ist "Stellung halten".
      * Die Einheit wird sich niemals vom Fleck bewegen, auch wenn sie von einem Ziel außer eigener Reichweite angegriffen wird.
      * Die Einheit wird sich nur wehren, wenn sie sich dafür nicht bewegen muss.
      * Dieser Modus wird nicht automatisch verlassen.
      */
     private static final int STAY_FIGHTING = 6;
     /**
      * In diesem Modus wird die Einheit sich weder bewegen, noch sich gegen angreifende Feinde zur Wehr setzen.
      * Dieser Modus wird nicht automatisch verlassen.
      */
     private static final int STAY_STILL = 7;
     /**
      * Minimale Wartedauer zwischen zwei regulären Suchläufen.
      * Hinweis: Wird gelegentlich auch sofort getriggert.
      */
     private static final int SEARCH_INTERVAL = 500;
     public static final int MOVEMODE_GOTO = 1;
     public static final int MOVEMODE_RUNTO = 2;
     /**
      * Der derzeitige Modus
      */
     private int mode = SEARCHENEMY;
     /**
      * Das derzeitige Ziel
      */
     private GameObject target;
     /**
      * Die Einheit selbst
      */
     private Unit caster2;
     /**
      * Wann die Einheit frühstens das nächste mal "feuern" kann
      */
     private long nextHit;
     /**
      * Wann die Einheit das nächste mal nach Feinden suchen kann.
      */
     private long nextSearch;
     /**
      * Die MovementMap
      */
     private MovementMap movemap;
 
     public ServerBehaviourAttack(Unit caster, ServerCore.InnerServer inner, MovementMap movemap) {
         super(inner, caster, 2, 5, true);
         caster2 = caster;
         this.movemap = movemap;
     }
 
     @Override
     public synchronized void execute() {
         switch (mode) {
             case FLEE:
                 // Ende, wenn Ziel erreicht
                 if (!caster2.getLowLevelManager().isMoving()) {
                     mode = SEARCHENEMY;
                     trigger();
                 }
                 break;
             case STAY_STILL:
                 // Überhaupt gar nichts. Die Einheit wehrt sich nicht.
                 break;
             case STAY_FIGHTING:
                 // Angriff nur auf Einheiten in Reichweite
                 if (target != null) {
                     if (!inRange(target)) {
                         // Dieses Ziel ist weg, wir dürfen es nicht verfolgen, also fallen lassen.
                         target = null;
                     } else {
                         if (alive(target)) {
                             shootIfReady();
                         } else {
                             // Weitersuchen
                             target = null;
                             trigger();
                         }
                     }
                 } else {
                     // Einheit in Reichweite suchen
                     if (searchInRangeScheduled()) {
                         // Neues gefunden? - Dann gleich nochmal rechnen
                         trigger();
                     }
                 }
                 break;
             case FOCUS:
                 // Keine alternativen Ziele suchen
                 // Ende des Modus, wenn Ziel zerstört
                 if (target == null || !alive(target)) {
                     mode = SEARCHENEMY;
                     trigger();
                 } else {
                     // In Range?
                     if (inRange(target)) {
                         // Bewegung stoppen, falls läuft
                         stopForFight();
                         shootIfReady();
                     } else {
                         // Verfolgen?
                         if (!caster2.getLowLevelManager().isMoving()) {
                             if (target instanceof Moveable) {
                                 caster2.getFollowManager().setTarget((Moveable) target);
                             }
                         } // else: Weiterlaufen
                     }
                 }
                 break;
             case GOTO:
                 // Eigentliches Ziel erreicht und lebt noch?
                 if (inRange(target) && alive(target)) {
                     // Lebts noch?
                     // Stehenbleiben
                     stopForFight();
                     // Auf Kämpfen umschalten
                     mode = FIGHTING;
                     trigger();
                 } else {
                     // Besseres Ziel verfügbar?
                     if (searchInRangeScheduled()) {
                         mode = FIGHTING;
                         trigger();
                     } else {
                         // Das alte Ziel behalten?
                         if (alive(target)) {
                             // Kein besseres Ziel. Weiter bzw. loslaufen:
                             if (!caster2.getLowLevelManager().isMoving()) {
                                 if (target instanceof Moveable) {
                                     caster2.getFollowManager().setTarget((Moveable) target);
                                 }
                             } // else: Weiterlaufen
                         } else {
                             stopForFight();
                             target = null;
                             mode = SEARCHENEMY;
                         }
                     }
                 }
                 break;
             case FIGHTING:
                 // Das derzeitige Ziel noch in Reichweite?
                 if (inRange(target)) {
                     if (alive(target)) {
                         // Dann ist ja alles klar
                         shootIfReady();
                     } else {
                         // Neues Ziel suchen
                         target = null;
                         mode = SEARCHENEMY;
                         trigger();
                     }
                 } else {
                     // Besseres Ziel in Reichweite? (Zwangs-Sofortsuche)
                     if (searchInRangeImmediately()) {
                         shootIfReady();
                     } else {
                         // Nein, wir verfolgen also besser das alte Ziel.
                         mode = GOTO;
                         trigger();
                     }
                 }
                 break;
             default: // (case SEARCHENEMY:)
                 // Die Einheit hat Kampftechnisch nichts zu tun, steht/läuft aber.
                 if (searchInRangeScheduled()) {
                     // Stehenbleiben
                     stopForFight();
                     mode = FIGHTING;
                     trigger(); // Sorort loskloppen
                 }
                 break;
         }
     }
 
     /**
      * Prüft, ob das gegebene GO in Reichweite ist
      * @param object
      * @return 
      */
     private boolean inRange(GameObject object) {
         return caster2.getPrecisePosition().getDistance(object.getCentralPosition()) <= caster2.getRange();
     }
 
     /**
      * Sucht nach Einheiten in Reichweite.
      * Um die Serverlast zu reduzieren, wird nur gelegentlich gesucht.
      */
     private boolean searchInRangeScheduled() {
         if (System.currentTimeMillis() >= nextSearch) {
             nextSearch = System.currentTimeMillis() + SEARCH_INTERVAL;
             return searchInRangeImmediately();
         }
         return false;
     }
 
     private boolean searchInRangeImmediately() {
         List<Moveable> moversAround = movemap.moversAround(caster2, caster2.getRange());
         // Liste nach möglichen Zielen filtern (und das mit der geringsten Distanz herausfinden)
         Iterator<Moveable> it = moversAround.iterator();
         double minDist = caster2.getRange();
         Moveable minDistMoveable = null;
         while (it.hasNext()) {
             Moveable move = it.next();
             if (move.isAttackableBy(caster2.getPlayerId())) {
                 double dist = caster2.getPrecisePosition().getDistance(move.getPrecisePosition());
                 if (dist <= minDist) {
                     minDist = dist;
                     minDistMoveable = move;
                 }
             } else {
                 it.remove();
             }
         }
 
         if (minDistMoveable != null) {
             target = minDistMoveable.getAttackable();
             return true;
         }
         return false;
     }
 
     /**
      * "Feuert", falls der Cooldown ok ist.
      */
     private void shootIfReady() {
         if (System.currentTimeMillis() >= nextHit) {
             // FEUER!
             // Flugzeit berechnen:
             int atkdelay = 0;
             int damage = caster2.getDamage() * caster2.getDamageFactors()[target.getArmorType()] / 100;
             if (caster2.getBulletspeed() > 0) {
                 atkdelay = (int) (caster2.getPrecisePosition().getDistance(target.getCentralPosition()) * 1000 / caster2.getBulletspeed());
             }
             if (atkdelay == 0) {
                 // Direkt schädigen:
                 target.dealDamageS(damage);
             } else {
                 Server.getInnerServer().atkMan.delayDamageTo(target, damage, atkdelay);
             }
             rgi.netctrl.broadcastDATA(rgi.packetFactory((byte) 39, caster2.netID, target.netID, damage, atkdelay));
            nextHit = System.currentTimeMillis() + caster2.getAtkdelay();
         }
     }
 
     @Override
     public void activate() {
         // Ignore, dieses Behaviour wird nicht angehalten.
     }
 
     @Override
     public void deactivate() {
         // Ignore, dieses Behaviour wird nicht angehalten.
     }
 
     @Override
     public void gotSignal(byte[] packet) {
     }
 
     @Override
     public void pause() {
     }
 
     @Override
     public void unpause() {
     }
 
     synchronized void newMoveMode(int i) {
         switch (i) {
             case MOVEMODE_RUNTO:
                 // Angriffsziel fallen lassen:
                 target = null;
                 mode = FLEE;
                 break;
             default: // MOVEMODE_GOTO
                 // Angriffsziel fallen lassen:
                 target = null;
                 mode = SEARCHENEMY;
         }
     }
 
     private boolean alive(GameObject target) {
         return target.getLifeStatus() != GameObject.LIFESTATUS_DEAD;
     }
 
     private void stopForFight() {
         if (caster2.getLowLevelManager().isMoving()) {
             caster2.getTopLevelManager().stopForFight(caster2);
         }
     }
 
     /**
      * Setzt ein neues Angriffsziel.
      * Überschreibt alte Befehle.
      * @param target Das neue Ziel
      * @param focusfire true, wenn Focus gewünscht
      */
     synchronized void setAttack(GameObject target, boolean focusfire) {
         if (focusfire) {
             mode = FIGHTING;
         } else {
             mode = FOCUS;
         }
         this.target = target;
         trigger();
     }
 
     /**
      * Das derzeitige Angriffsziel.
      * Kann sich jederzeit ändern, darf nicht aus dem movementsystem rausgegeben werden.
      * @return 
      */
     GameObject getCurrentTarget() {
         return target;
     }
 }
