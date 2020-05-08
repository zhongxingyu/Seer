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
 package de._13ducks.cor.networks.client.behaviour.impl;
 
 import de._13ducks.cor.game.FloatingPointPosition;
 import de._13ducks.cor.game.Unit;
 import de._13ducks.cor.game.client.ClientCore;
 import de._13ducks.cor.game.server.movement.Vector;
 import de._13ducks.cor.networks.client.behaviour.ClientBehaviour;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * Der Clientmover bewegt die Einheiten gemäß den Befehlen des Servers auf dem Client.
  * Jede Einheit hat ihren eingenen Clientmover.
  * 
  */
 public class ClientBehaviourMove extends ClientBehaviour {
 
     /**
      * Um wieviele Millisekunden das Clientbehaviour eingehende Bewegungs-
      * befehle absichtlich verzögert, um flakern beim Stoppen zu verhindern.
      */
     private int CLIENT_DELAY = 100;
     /**
      * Das derzeitige Bewegungsziel der Einheit.
      */
     private FloatingPointPosition target;
     /**
      * Die Einheit, die von diesem Behaviour verwaltet wird
      */
     protected Unit caster2;
     /**
      * Die derzeitige Bewegungsgeschwindigkeit
      */
     private double speed;
     /**
      * Stoppen?
      */
     private FloatingPointPosition stopPos = null;
     /**
      * Wann wurde die Bewegung zuletzt berechnet? (in nanosekunden)
      */
     private long lastTick;
     private ConcurrentLinkedQueue<MoveTask> queue;
 
     public ClientBehaviourMove(ClientCore.InnerClient rgi, Unit caster2) {
         super(rgi, caster2, 1, 5, false);
         this.caster2 = caster2;
         queue = new ConcurrentLinkedQueue<MoveTask>();
     }
 
     @Override
     public synchronized void execute() {
         // Eingehende Signale verarbeiten:
         if (!queue.isEmpty()) {
             MoveTask task = queue.peek();
             if (System.currentTimeMillis() >= task.execTime) {
                 queue.remove().perform();
                 if (!queue.isEmpty()) {
                     // Noch mehr da? Dann gleich nochmal:
                     trigger();
                 }
             } else {
                 // Es ist was da, aber wir müssen noch etwas warten.
                 trigger();
             }
        } else {
            // Auto-Ende:
            if (target == null || speed <= 0) {
                deactivate();
                return;
            }
         }
        // Es gibt nichts zu tun...
         if (target == null || speed <= 0) {
             return;
         }
         if (stopPos == null) {
             // Wir laufen also.
             // Aktuelle Position berechnen:
             FloatingPointPosition oldPos = caster2.getPrecisePosition();
             Vector vec = target.subtract(oldPos).toVector();
             vec.normalizeMe();
             long ticktime = System.nanoTime();
             vec.multiplyMe((ticktime - lastTick) / 1000000000.0 * speed);
             FloatingPointPosition newpos = vec.toFPP().add(oldPos);
 
             if (!newpos.toVector().isValid()) {
                 // Das geht so nicht, abbrechen und gleich nochmal!
                 System.out.println("CLIENT-Move: Evil params: " + caster2 + " " + oldPos + " " + target + " " + vec + " " + ticktime + " " + lastTick + "-->" + newpos);
                 trigger();
                 return;
             }
 
             // Ziel schon erreicht?
             Vector nextVec = target.subtract(newpos).toVector();
             if (vec.isOpposite(nextVec)) {
                 // ZIEL!
                 // Wir sind warscheinlich drüber - egal einfach auf dem Ziel halten.
                 caster2.setMainPosition(target);
                 target = null;
                 stopPos = null; // Es ist wohl besser auf dem Ziel zu stoppen als kurz dahinter!
             } else {
                 // Weiterlaufen
                 caster2.setMainPosition(newpos);
                 lastTick = System.nanoTime();
             }
         } else {
             caster2.setMainPosition(stopPos);
             target = null;
             stopPos = null;
         }
     }
 
     @Override
     public synchronized void gotSignal(byte[] packet) {
         if (packet[0] == 24) {
             // Anhalten ist normalerweise kein neuer Task, sondern das Ziel des vorhergehenden wird geändert.
             if (!queue.isEmpty()) {
                 MoveTask task = queue.peek();
                 task.pos = rgi.readFloatingPointPosition(packet, 2);
             } else {
                 // Es läuft schon, live ändern
                 if (target != null) {
                     target = rgi.readFloatingPointPosition(packet, 2);
                 } else {
                     // Wir laufen gar nicht und werden auch in Zukunft nicht loslaufen
                     // Dann als normalen Task adden, damit die Position mit Gewalt
                     // gesetzt wird. Ist zwar hässlich, verhindert aber asyncs.
                     FloatingPointPosition pos = rgi.readFloatingPointPosition(packet, 2);
                     MoveTask task = new MoveTask(0, pos, 0);
                     queue.add(task);
                 }
             }
         } else if (packet[0] == 23) {
             // Move
             float moveSpeed = Float.intBitsToFloat(rgi.readInt(packet, 2));
             FloatingPointPosition pos = new FloatingPointPosition(Float.intBitsToFloat(rgi.readInt(packet, 3)), Float.intBitsToFloat(rgi.readInt(packet, 4)));
             MoveTask task = new MoveTask(1, pos, moveSpeed);
             queue.add(task);
         }
         // Schnell verarbeiten
         activate();
     }
 
     @Override
     public void pause() {
     }
 
     @Override
     public void unpause() {
     }
 
     private synchronized void newMoveVec(double speed, FloatingPointPosition target) {
         this.speed = speed;
         this.target = target;
         lastTick = System.nanoTime();
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
 
     /**
      * Stoppt die Einheit sofort auf der angegeben Position
      * (innerhalb eines Ticks)
      * @param pos 
      */
     private synchronized void stopAt(FloatingPointPosition pos) {
         stopPos = pos;
     }
 
     /**
      * Jedes Signal vom Server wird in ein solches Objekt gesteckt.
      * Der Client verarbeitet diese Aufaben der Reihe nach.
      */
     private class MoveTask {
 
         int mode;
         FloatingPointPosition pos;
         double speed;
         long execTime;
 
         public MoveTask(int mode, FloatingPointPosition pos, double speed) {
             this.mode = mode;
             this.pos = pos;
             this.speed = speed;
             execTime = System.currentTimeMillis() + CLIENT_DELAY;
         }
 
         void perform() {
             System.out.println("QUEUE-TASK: " + caster2 + " " + mode + " " + pos + " " + speed);
             if (mode == 0) {
                 // STOP
                 stopAt(pos);
             } else if (mode == 1) {
                 newMoveVec(speed, pos);
             }
         }
     }
 }
