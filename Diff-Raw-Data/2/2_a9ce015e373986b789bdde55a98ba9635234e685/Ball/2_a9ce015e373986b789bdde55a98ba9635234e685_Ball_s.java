 /*
  * This file is part of the Turtle project
  *
  * (c) 2011 Julien Brochet <julien.brochet@etu.univ-lyon1.fr>
  *
  * For the full copyright and license information, please view the LICENSE
  * file that was distributed with this source code.
  */
 
 package turtle.entity.field;
 
 import java.awt.geom.Point2D;
 import turtle.util.Vector2D;
 
 /**
  * Représentation d'un ballon de foot
  *
  * @author Julien Brochet <julien.brochet@etu.univ-lyon1.fr>
  * @since 1.0
  */
 public class Ball
 {
     /**
      * La position du ballon
      */
     protected Point2D mPosition;
 
     /**
      * Le vecteur vitesse du ballon
      */
     protected Vector2D mSpeedVector;
 
     /**
      * Le dernier object qui a shooté dans le ballon
      *
      * Il n'est pas question ici de stocker directement un objet
      * de type {@link Turtle} pour isoler correctement ce package
      */
     protected Object mLastShooter;
 
     /**
     * Le Coefficient de frottement du ballon sur le terrain (en m.s-2)
      */
     protected final double mFriction = 3.9e-1;
 
     /**
      * Construction du ballon
      *
      * @param position La position sur le terrain
      */
     public Ball(Point2D position)
     {
         mSpeedVector = new Vector2D();
         mPosition = position;
     }
 
     /**
      * Construction du ballon
      *
      * @param x La position sur l'axe des x
      * @param y La position sur l'axe des y
      */
     public Ball(double x, double y)
     {
         this(new Point2D.Double(x, y));
     }
 
     /**
      * Construction du ballon
      *
      * Le ballon sera positionné à la position (0,0)
      */
     public Ball()
     {
         this(0, 0);
     }
 
     /**
      * Met à jour la position du ballon en fonction de sa vitesse
      */
     public void move(long elapsedTime)
     {
         if (mSpeedVector.getLength() < 1e-2) {
             mSpeedVector.setNull();
             return;
         }
 
         mPosition.setLocation(mPosition.getX() + mSpeedVector.getX() * elapsedTime, mPosition.getY() + mSpeedVector.getY() * elapsedTime);
 
         double friction = 1 - mFriction * ((double) elapsedTime / 1000);
         mSpeedVector.scale(friction);
     }
 
     /**
      * Tir dans le ballon
      *
      * @param shooter L'objet qui a tiré dans le ballon
      * @param speed Le vecteur vitesse du tir
      */
     public void shoot(Object shooter, Vector2D speed)
     {
         mLastShooter = shooter;
         mSpeedVector = speed;
     }
 
     /**
      * Remet à zero le ballon
      */
     public void reset()
     {
         mPosition.setLocation(0, 0);
         mLastShooter = null;
         mSpeedVector.setNull();
     }
 
     /**
      * Change la position du ballon
      *
      * @param position La nouvelle position du ballon
      */
     public void setPosition(Point2D position)
     {
         mPosition = position;
     }
 
     /**
      * Annule le vecteur vitesse du ballon
      */
     public void setNullSpeed()
     {
         mSpeedVector.set(0, 0);
     }
 
     /**
      * Retourne la position du ballon
      */
     public Point2D getPosition()
     {
         return mPosition;
     }
 
     /**
      * Retourne le dernier objet qui a shooté
      * dans le ballon
      */
     public Object getLastShooter()
     {
         return mLastShooter;
     }
 
     /**
      * Change la vitesse du ballon
      *
      * @param vector Le vecteur vitesse
      */
     public void setSpeedVector(Vector2D vector)
     {
         mSpeedVector = vector;
     }
 
     /**
      * Retourne le vecteur vitesse du ballon
      */
     public Vector2D getSpeedVector()
     {
         return mSpeedVector;
     }
 }
