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
 /**
  * Repräsentiert ein Feld im 2-Dimensionalen Raum.
  * Arbeitet mit dem bekannten Rauten-nur jedes zweite Feld-System.
  *
  **/
 package de._13ducks.cor.game;
 
 //import elementcorp.rog.RogMapElement.collision;
 import java.io.*;
 
 public class Position implements Comparable<Position>, Serializable, Cloneable {
 
     public static final int AROUNDME_CIRCMODE_FULL_CIRCLE = 1;
     public static final int AROUNDME_CIRCMODE_HALF_CIRCLE = 2;
     public static final int AROUNDME_COLMODE_GROUNDTARGET = 10;
     public static final int AROUNDME_COLMODE_GROUNDPATH = 11;
     public static final int AROUNDME_COLMODE_GROUNDPATHPLANNING = 12;
     protected int X;                                    //X-Koodrinate
     protected int Y;
     private int cost;
     private int heuristic;
     private int valF;
     private Position parent;                          //Das Feld von dem man kommt
 
     //Konstruktor
     public Position(int x, int y) {
         X = x;
         Y = y;
     }
 
     /**
      * Überprüft, ob eine Position erlaubt ist, also legal Koordinaten hat.
      * Checkt nur ob die Position überhaupt legal ist, Fragen bezüglich der Mapgrenzen beantwortet das Mapmodul
      * @return
      */
     public boolean valid() {
         return (X % 2 == Y % 2);
     }
 
     /**
      * Zieht die mitgegebene Position von der Position ab, auf der es aufgerufen wird
      */
     public Position subtract(Position pos) {
         return new Position(this.X - pos.X, this.Y - pos.Y);
     }
 
     /**
      * Addiert eine Position zu dieser hier
      */
     public Position add(Position pos) {
         return new Position(this.X + pos.X, this.Y + pos.Y);
     }
 
    public boolean equals(Position p2) {
        if (p2 == null) {
            return false;
        }
        if (X != p2.X) {
            return false;
        }
        if (Y != p2.Y) {
            return false;
        }
        return true;
    }

     @Override
     public boolean equals(Object p2) {
         if (p2.getClass().equals(this.getClass())) {
             Position pos = (Position) p2;
             if (X != pos.X) {
                 return false;
             }
             if (Y != pos.Y) {
                 return false;
             }
             return true;
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 89 * hash + this.X;
         hash = 89 * hash + this.Y;
         return hash;
     }
 
     @Override
     public int compareTo(Position f) {
         if (f.getValF() > this.getValF()) {
             return -1;
         } else if (f.getValF() < getValF()) {
             return 1;
         } else {
             return 0;
         }
     }
 
     public double getDistance(Position pos) {
         // Gibt die Entfernung zwischen dem übergebenen Punkt und diesem hier zurück
         return Math.sqrt(Math.pow(pos.X - this.X, 2) + Math.pow(pos.Y - this.Y, 2));
     }
 
     public Position transformToVector() {
         // Liefert den RogPosition zurück, in Vectordarstellung, maximale Werte 1 bis -1
         Position newvec = new Position(0, 0);
         if (this.X > 0) {
             newvec.X = 1;
         } else if (this.X < 0) {
             newvec.X = -1;
         } else {
             newvec.X = 0;
         }
         if (this.Y > 0) {
             newvec.Y = 1;
         } else if (this.Y < 0) {
             newvec.Y = -1;
         } else {
             newvec.Y = 0;
         }
         return newvec;
     }
 
     /**
      * Wandelt diese RogPosition in einen int-Vector um
      * Int-Vektoren beginnen oben mit 1 und laufen im Uhrzeigersinn herum
      * @return int Der Vector
      */
     public int transformToIntVector() {
         if (this.X > 0) {
             if (this.Y > 0) {
                 return 4;
             } else if (this.Y < 0) {
                 return 2;
             }
             return 3;
         } else if (this.X < 0) {
             if (this.Y > 0) {
                 return 6;
             } else if (this.Y < 0) {
                 return 8;
             }
             return 7;
         }
         if (this.Y > 0) {
             return 5;
         } else if (this.Y < 0) {
             return 1;
         }
         return 0;
     }
 
     /**
      * Dreht den Vector um, lääst ihn also in die Gegenrichtung zeigen
      * @param oldVec der Vektor der umgedreht werden soll
      * @return der umgedrehte Vector
      */
     public static int flipIntVector(int oldVec) {
         oldVec += 4;
         if (oldVec > 8) {
             oldVec -= 8;
         }
         return oldVec;
     }
 
     @Override
     public String toString() {
         return this.X + "|" + this.Y;
     }
 
     @Override
     public Position clone() throws CloneNotSupportedException {
         return (Position) super.clone();
     }
 
     /**
      * @return the X
      */
     public int getX() {
         return X;
     }
 
     /**
      * @param X the X to set
      */
     public void setX(int X) {
         this.X = X;
     }
 
     /**
      * @return the Y
      */
     public int getY() {
         return Y;
     }
 
     /**
      * @param Y the Y to set
      */
     public void setY(int Y) {
         this.Y = Y;
     }
 
     /**
      * @return the cost
      */
     public int getCost() {
         return cost;
     }
 
     /**
      * @param cost the cost to set
      */
     public void setCost(int cost) {
         this.cost = cost;
     }
 
     /**
      * @return the heuristic
      */
     public int getHeuristic() {
         return heuristic;
     }
 
     /**
      * @param heuristic the heuristic to set
      */
     public void setHeuristic(int heuristic) {
         this.heuristic = heuristic;
     }
 
     /**
      * @return the valF
      */
     public int getValF() {
         return valF;
     }
 
     /**
      * @param valF the valF to set
      */
     public void setValF(int valF) {
         this.valF = valF;
     }
 
     /**
      * @return the parent
      */
     public Position getParent() {
         return parent;
     }
 
     /**
      * @param parent the parent to set
      */
     public void setParent(Position parent) {
         this.parent = parent;
     }
 }
