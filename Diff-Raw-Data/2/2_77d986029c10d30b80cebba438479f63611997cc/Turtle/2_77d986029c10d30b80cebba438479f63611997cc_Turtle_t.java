 /*
  * This file is part of the Turtle project
  *
  * (c) 2011 Julien Brochet <julien.brochet@etu.univ-lyon1.fr>
  *
  * For the full copyright and license information, please view the LICENSE
  * file that was distributed with this source code.
  */
 
 package turtle.entity;
 
 import java.awt.Color;
 import java.awt.geom.Point2D;
 
 import java.awt.geom.Rectangle2D;
 import turtle.behavior.team.TeamBehaviorInterface;
 import turtle.behavior.turtle.TurtleBehaviorInterface;
 import turtle.entity.field.Ball;
 import turtle.util.Log;
 import turtle.util.Vector2D;
 
 /**
  * Représentation d'un joueur de foot
  *
  * @author Julien Brochet <julien.brochet@etu.univ-lyon1.fr>
  * @since 1.0
  */
 public class Turtle
 {
     /**
      * La position du joueur sur le terrain
      */
     protected Point2D mPosition;
 
     /**
      * Le vecteur vitesse
      */
     protected Vector2D mSpeedVector;
 
     /**
      * L'équipe du joueur
      */
     protected Team mTeam;
 
     /**
      * Le comportement du joueur
      */
     protected TurtleBehaviorInterface mBehavior;
 
     /**
      * Création d'un joueur
      *
      * @param team      L'équipe du joueur
      * @param behavior  Le comportement
      * @param position  La position sur le terrain
      */
     public Turtle(Team team, TurtleBehaviorInterface behavior, Point2D position)
     {
         if (behavior == null) {
             throw new IllegalArgumentException("A Turtle needs to have a TurtleBehavior.");
         }
 
         behavior.setTurtle(this);
         team.addTurtle(this);
 
         mSpeedVector = new Vector2D();
         mTeam = team;
         mBehavior = behavior;
         mPosition = position;
 
         Log.i(String.format("Turtle creation (behavior=%s, position=%s)", behavior, position));
     }
 
     /**
      * Met à jour la position d'un joueur
      *
      * @param elapsedTime Le temps écoulé depuis le dernier appel
      */
     public void update(long elapsedTime)
     {
         TeamBehaviorInterface teamBehavior = mTeam.getBehavior();
         Vector2D vector = mBehavior.getNextSpeedVector(elapsedTime);
 
        if (teamBehavior != null && vector != null) {
             teamBehavior.apply(vector, elapsedTime);
         }
 
         mSpeedVector = vector;
     }
 
     /**
      * Met à jour la position du joueur en fonction de sa vitesse
      */
     public void move()
     {
         if (mSpeedVector != null) {
             mPosition.setLocation(mPosition.getX() + mSpeedVector.getX(), mPosition.getY() + mSpeedVector.getY());
         }
     }
 
     /**
      * Regarde si la ball est proche du joueur
      *
      * @param ball La balle du terrain
      *
      * @return Vrai si la balle est proche du joueur, faux sinon
      */
     public boolean isAround(Ball ball)
     {
         int delta = 10;
         Rectangle2D rectangle = new Rectangle2D.Double(mPosition.getX() - delta, mPosition.getY() - delta, 2 * delta, 2 * delta);
 
         return rectangle.contains(ball.getPosition());
     }
 
     /**
      * Retourne la position du joueur sur le terrain
      */
     public Point2D getPosition()
     {
         return mPosition;
     }
 
     /**
      * Change la position du joueur sur le terrain
      *
      * @param position L'objet position
      */
     public void setPosition(Point2D position)
     {
         mPosition = position;
     }
 
     /**
      * Change la position du joueur sur le terrain
      *
      * @param x Le distance sur l'axe des X
      * @param y La distance sur l'axe des Y
      */
     public void setPosition(double x, double y)
     {
         mPosition = new Point2D.Double(x, y);
     }
 
     /**
      * Change le vecteur vitesse du joueur
      *
      * @param vector Le nouveau vecteur de vitesse
      */
     public void setSpeedVector(Vector2D vector)
     {
         mSpeedVector = vector;
     }
 
     /**
      * Retourne le vecteur de vitesse
      */
     public Vector2D getSpeedVector()
     {
         return mSpeedVector;
     }
 
     /**
      * Retourne le comportement du joueur
      *
      * @return Le comportement
      */
     public TurtleBehaviorInterface getBehavior()
     {
         return mBehavior;
     }
 
     /**
      * Retourne l'équipe du joueur
      */
     public Team getTeam()
     {
         return mTeam;
     }
 
     /**
      * Retourne la couleur du joueur
      */
     public Color getColor()
     {
         return mTeam.getColor();
     }
 }
