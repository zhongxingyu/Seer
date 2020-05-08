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
 package de._13ducks.cor.game.networks.behaviour.impl;
 
 import de._13ducks.cor.game.FloatingPointPosition;
 import de._13ducks.cor.networks.server.behaviour.ServerBehaviour;
 import de._13ducks.cor.game.server.ServerCore;
 import de._13ducks.cor.game.Unit;
 import de._13ducks.cor.game.server.movement.Vector;
 
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
  */
 public class ServerBehaviourMove extends ServerBehaviour {
 
     private Unit caster2;
     private FloatingPointPosition target;
     private double speed;
     private boolean stopUnit = false;
     private long lastTick;
     private Vector lastVec;
     
     public ServerBehaviourMove(ServerCore.InnerServer newinner, Unit caster) {
         super(newinner, caster, 1, 20, true);
         caster2 = caster;
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
     public void execute() {
         // Auto-Ende:
         if (target == null || speed <= 0) {
             deactivate();
             return;
         }
         // Wir laufen also.
         // Aktuelle Position berechnen:
         Vector vec = target.subtract(caster2.getPrecisePosition()).toVector();
         vec.normalize();
         if (!vec.equals(lastVec)) {
             // An Client senden
             rgi.netctrl.broadcastMoveVec(caster2.netID, target, speed);
         }
         long ticktime = System.currentTimeMillis();
         vec.multiply((ticktime - lastTick) / 1000.0 * speed);
         FloatingPointPosition newpos = vec.toFloatingPointPosition();
         
         // Ziel schon erreicht?
         Vector nextVec = target.subtract(newpos).toVector();
         if (vec.isOpposite(nextVec)) {
             // ZIEL!
             // Wir sind warscheinlich drüber - egal einfach auf dem Ziel halten.
             caster2.setMainPosition(target);
             target = null;
             stopUnit = false; // Es ist wohl besser auf dem Ziel zu stoppen als kurz dahinter!
             deactivate();
         } else {
             // Sofort stoppen?
             if (stopUnit) {
                 caster2.setMainPosition(newpos);
                 target = null;
                 stopUnit = false;
                 deactivate();
             } else {
                 // Weiterlaufen
                 caster2.setMainPosition(newpos);
                 lastTick = System.currentTimeMillis();
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
     public void setTargetVector(FloatingPointPosition pos) {
         if (pos == null) {
             throw new IllegalArgumentException("Cannot send " + caster2 + " to null");
         }
         target = pos;
         lastTick = System.currentTimeMillis();
         lastVec = Vector.NULL;
         activate();
     }
 
     /**
      * Setzt den Zielvektor und die Geschwindigkeit und startet die Bewegung sofort.
      * @param pos die Zielposition
      * @param speed die Geschwindigkeit
      */
     public void setTargetVector(FloatingPointPosition pos, double speed) {
         changeSpeed(speed);
         setTargetVector(pos);
     }
 
     /**
      * Ändert die Geschwindigkeit während des Laufens.
      * Speed wird verkleinert, wenn der Wert über dem Einheiten-Maximum liegen würde
      * @param speed Die Einheitengeschwindigkeit
      */
     public void changeSpeed(double speed) {
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
     public void stopImmediately() {
         stopUnit = true;
         trigger();
     }
 }
