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
 import de._13ducks.cor.game.SimplePosition;
 
 /**
  * Ein (double) Richtungsvektor.
  * Einfacher, aber weniger flexibel als FPP
  */
 public class Vector implements SimplePosition {
 
     public static final Vector NULL = new Vector(0, 0);
     private double x;
     private double y;
 
     /**
      * Erstell einen neuen Vektor mit den angegebenen Koordinaten
      * @param x x
      * @param y y
      */
     public Vector(double x, double y) {
         this.x = x;
         this.y = y;
     }
 
     /**
      * @return the x
      */
     public double getX() {
         return x;
     }
 
     /**
      * @param x the x to set
      */
     public void setX(double x) {
         this.x = x;
     }
 
     /**
      * @return the y
      */
     public double getY() {
         return y;
     }
 
     /**
      * @param y the y to set
      */
     public void setY(double y) {
         this.y = y;
     }
 
     /**
      * Wandelt den Vektor in eine FPP um.
      * @return der Vektor als FPP.
      */
     public FloatingPointPosition toFPP() {
         return new FloatingPointPosition(x, y);
     }
 
     /**
      * Den Vektor mit einem Skalar multiplizieren.
      * Verändert direkt den Vector!
      * @param scalar 
      */
     public void multiplyMe(double scalar) {
         x *= scalar;
         y *= scalar;
     }
 
     /**
      * Liefert eine Skalar multiplizierte Version dieses Vektors hinzu
      * @param scalar
      * @return 
      */
     public Vector multiply(double scalar) {
         return new Vector(x * scalar, y * scalar);
     }
 
     /**
      * Addiert einen Vektor auf diesen hier.
      * @param vector 
      */
     public void addToMe(Vector vector) {
         x += vector.x;
         y += vector.y;
     }
 
     /**
      * Liefert einen neuen Vektor, der das Ergebniss einer Summation dieses mit dem gegebenen Vektor darstellt
      * @param vector der zweite summand
      * @return einen neuen Vektor, der das Ergebniss einer Summation dieses mit dem gegebenen Vektor darstellt
      */
     public Vector add(Vector vector) {
         return new Vector(x + vector.x, y + vector.y);
     }
 
     /**
      * Invertiert direkt diesen Vektor selbst.
      */
     public void invertMe() {
         x *= -1;
         y *= -1;
     }
 
     /**
      * Liefert eine invertierte Kopie dieses Vektors.
      * @return 
      */
     public Vector getInverted() {
         return new Vector(-x, -y);
     }
 
     /**
      * Normiert den Vektor
      * Verändert direkt den Vector!
      */
     public void normalizeMe() {
         double fact = length();
         x *= 1 / fact;
         y *= 1 / fact;
     }
 
     /**
      * Normiert den Vektor
      * Gibt den Normierten zurück
      */
     public Vector normalize() {
         double fact = length();
         return new Vector(this.x / fact, this.y / fact);
     }
 
     /**
      * Berechnet die Länge des Vektors
      * @return die Länge des Vektors
      */
     public double length() {
         return Math.sqrt(x * x + y * y);
     }
 
     /**
      * Findet heraus, ob dieser Vektor zu dem gegeben Parallel ist.
      * Wie bei allen Vektorvergleichen gibt es eine Toleranz gegen Rundungsfehler.
      * Diese Toleranz beträgt ein Promille pro Richtung.
      * Dies ist eine Behandlung für Richtungsvektoren, ein eventuelles Auf-/Ineinanderliegen von Vektoren wird ignoriert.
      * @param vec der andere Vektor
      * @return true, wenn parallel
      */
     public boolean isParallel(Vector vec) {
         vec.normalizeMe();
         Vector meNormal = this.normalize();
         return (vec.equals(meNormal) || vec.equals(meNormal.getInverted()));
     }
 
     /**
      * True, wenn der gleiche Vektor, nur in die andere Richtung zeigend
      * Länge egal.
      */
     public boolean isOpposite(Vector vec) {
         return this.normalize().equals(new Vector(-vec.x, -vec.y).normalize());
     }
 
     /**
      * Berechnet einen Schnittpunkt zwischen diesem und dem gegeben Vector.
      * Liefert null, falls die Vektoren parallel sind (auch, wenn sie aufeinander liegen!!!)
      * @param vec der andere Vektor
      * @return Einen Stützvektor zum Schnittpunkt oder null, wenns keinen gibt.
      */
     public Vector intersectionWith(Vector mySPos, Vector otherS, Vector otherVec) {
         if (isParallel(otherVec)) {
             return null;
         }
         // Es gibt einen Schnittpunkt
         double fact = 0d;
         // Sonderfälle:
         if (this.x == 0) {
             // Implizit: otherVec.x != 0, weil sonst paralell
             fact = (mySPos.x - otherS.x) / otherVec.x;
         } else if (this.y == 0) {
             // Implizit: otherVec.y != 0, weil sonst paralell
             fact = (mySPos.y - otherS.y) / otherVec.y;
         } else if (otherVec.x == 0) {
             fact = (mySPos.y + (otherS.x - mySPos.x) / x * y - otherS.y) / otherVec.y;
         } else if (otherVec.y == 0) {
             fact = (mySPos.x + (otherS.y - mySPos.y) / y * x - otherS.x) / otherVec.x;
         } else {
             // Normalfall:
             // Diese Formel hab ich mir durch umwandeln von (a,b)+x(c,d)=(e,f)+y(g,h) gebildet
             fact = (otherS.y * this.x - mySPos.y * this.x - otherS.x * this.y + mySPos.x * this.y) / (otherVec.x * this.y - otherVec.y * this.x);
         }
         return otherS.add(otherVec.multiply(fact));
     }
 
     @Override
     public boolean equals(Object o) {
         if (o instanceof SimplePosition) {
             SimplePosition vec = (SimplePosition) o;
             return (Math.abs(this.x - vec.x()) < 0.001 && Math.abs(this.y - vec.y()) < 0.001);
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 41 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
         hash = 41 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
         return hash;
     }
 
     @Override
     public String toString() {
         return "v:" + x + "|" + y;
     }
 
     public double x() {
         return x;
     }
 
     public double y() {
         return y;
     }
 
     public Vector toVector() {
         return new Vector(x, y);
     }
 
     public Node toNode() {
         return new Node(x, y);
     }
 }
