 /*
  * This file is part of the Turtle project
  *
  * (c) 2011 Julien Brochet <julien.brochet@etu.univ-lyon1.fr>
  *
  * For the full copyright and license information, please view the LICENSE
  * file that was distributed with this source code.
  */
 
 package turtle.util;
 
 /**
  * Gestion des vecteurs en deux dimensions
  *
  * @author Julien Brochet <julien.brochet@etu.univ-lyon1.fr>
  * @since 1.0
  */
 public class Vector2D implements Cloneable
 {
     protected double mX;
     protected double mY;
 
     public Vector2D()
     {
         this(0, 0);
     }
 
     public Vector2D(double x, double y)
     {
         mX = x;
         mY = y;
     }
 
     public Vector2D(Vector2D vector)
     {
         mX = vector.mX;
         mY = vector.mY;
     }
 
     /**
      * Effectue une rotation du vecteur
      *
     * @param angle L'angle de rotation (en radian)
      */
     public void rotate(double angle)
     {
         double rx = (mX * Math.cos(angle)) - (mY * Math.sin(angle));
         double ry = (mY * Math.cos(angle)) + (mX * Math.sin(angle));
 
         mX = rx;
         mY = ry;
     }
 
     /**
      * Retourne la taille du vecteur
      */
     public double getLength()
     {
         return  Math.sqrt(mX * mX + mY * mY);
     }
 
     /**
      * Change la taille du vecteur
      *
      * @param length La taille du vecteur
      */
     public Vector2D setLength(double length)
     {
         scale(length / getLength());
 
         return this;
     }
 
     @Override
     public boolean equals(Object obj)
     {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Vector2D other = (Vector2D) obj;
 
         if (Double.doubleToLongBits(this.mX) != Double.doubleToLongBits(other.mX)) {
             return false;
         }
         if (Double.doubleToLongBits(this.mY) != Double.doubleToLongBits(other.mY)) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public int hashCode()
     {
         int hash = 7;
 
         hash = 53 * hash + (int) (Double.doubleToLongBits(this.mX) ^ (Double.doubleToLongBits(this.mX) >>> 32));
         hash = 53 * hash + (int) (Double.doubleToLongBits(this.mY) ^ (Double.doubleToLongBits(this.mY) >>> 32));
 
         return hash;
     }
 
     /**
      * Additione un vecteur au vecteur actuel
      *
      * @param vector Le vecteur à additioner
      *
      * @return L'instance courante avec l'opération réalisée
      */
     public Vector2D sum(Vector2D vector)
     {
         mX += vector.mX;
         mY += vector.mY;
 
         return this;
     }
 
     /**
      * Soustrait un vecteur au vecteur actuel
      *
      * @param vector Le vecteur à soustraire
      *
      * @return L'instance courante avec l'opération réalisée
      */
     public Vector2D sub(Vector2D vector)
     {
         mX -= vector.mX;
         mY -= vector.mY;
 
         return this;
     }
 
     /**
      * Multiplie le vecteur avec une constante
      *
      * @param a Le vecteur à multipliser
      *
      * @return L'instance courante avec l'opération réalisée
      */
     public Vector2D scale(double a)
     {
         mX *= a;
         mY *= a;
 
         return this;
     }
 
     public double dot(Vector2D vector)
     {
         return mX * vector.mX + mY * vector.mY;
     }
 
     /**
      * Retourne l'angle entre le vecteur courant et un autre vecteur
      *
      * @param vector L'autre vecteur
      *
      * @return Retourne l'angle en radiant
      */
     public double angle(Vector2D vector)
     {
         Vector2D v = new Vector2D(this);
         Vector2D w = new Vector2D(vector);
 
         v.normalize();
         w.normalize();
 
         return Math.acos(v.dot(w));
     }
 
     public double angle()
     {
         Vector2D vector = new Vector2D(1, 0);
 
         return angle(vector);
     }
 
     public void normalize()
     {
         double length = getLength();
 
         if (length != 0.0) {
             set(mX / length, mY / length);
         }
     }
 
     public void set(double x, double y)
     {
         mX = x;
         mY = y;
     }
 
     public void setNull()
     {
         set(0,0);
     }
 
     /**
      * Inversion le vecteur
      *
      * @return L'instance courante avec l'opération réalisée
      */
     public Vector2D neg()
     {
         mX -= mX;
         mY -= mY;
 
         return this;
     }
 
     /**
      * Effectue une rotation de 90° au vecteur
      *
      * @return L'instance courante avec l'opération réalisée
      */
     public Vector2D rotate90()
     {
         double tmp = mX;
 
         mX = -mY;
         mY = tmp;
 
         return this;
     }
 
     /**
      * Calcule la distance entre deux vecteurs
      *
      * @param vector Le deuxième vecteur
      *
      * @return La distance
      */
     public double dist(Vector2D vector)
     {
         double dx = mX - vector.mX;
         double dy = mY - vector.mY;
 
         return Math.sqrt(dx * dx + dy * dy);
     }
 
     /**
      * Retourne la composante X du vecteur
      */
     public double getX()
     {
         return mX;
     }
 
     /**
      * Retourne lacomposante Y du vecteur
      */
     public double getY()
     {
         return mY;
     }
 
     @Override
     public Vector2D clone()
     {
         return new Vector2D(this);
     }
 
     @Override
     public String toString()
     {
         return "[" + mX + "," + mY + "]";
     }
 }
