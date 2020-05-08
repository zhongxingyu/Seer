 /*
  * This file is part of the Turtle project
  *
  * (c) 2011 Julien Brochet <julien.brochet@etu.univ-lyon1.fr>
  *
  * For the full copyright and license information, please view the LICENSE
  * file that was distributed with this source code.
  */
 
 package turtle.entity.field;
 
 import java.awt.Dimension;
 import java.awt.geom.Point2D;
 
 /**
  * Représentation d'un terrain de foot
  *
  * @author Julien Brochet <julien.brochet@etu.univ-lyon1.fr>
  * @since 1.0
  */
 public class Field
 {
     /**
      * Les dimensions du terrain
      */
     protected Dimension mDimension;
 
     /**
      * Le premier but
      */
     protected Goal mGoalA;
 
     /**
      * Le deuxième but
      */
     protected Goal mGoalB;
 
     /**
      * Le ballon sur le terrain de foot
      */
     protected Ball mBall;
 
     /**
      * Construction du terrain de foot
      */
     public Field(int width, int height)
     {
         mDimension = new Dimension(width, height);
 
         // The ball
        mBall = new Ball(new Point2D.Float((float) mDimension.getWidth() / 2, (float) mDimension.getHeight() / 2));
 
         // Goal A
         float goal = (float) (0.35 * height);
 
         float x = (float) (0.05 * width);
         float y = (height - goal) / 2;
 
         mGoalA = new Goal(new Point2D.Float(x, y), new Point2D.Float(x, y + goal));
 
         // Goal B
         x = width - x;
 
         mGoalB = new Goal(new Point2D.Float(x, y), new Point2D.Float(x, y + goal));
     }
 
     /**
      * Retourne les dimensions du terrain
      */
     public Dimension getDimension()
     {
         return mDimension;
     }
 
     /**
      * Retourne le but de l'équipe A
      */
     public Goal getGoalA()
     {
         return mGoalA;
     }
 
     /**
      * Retourne le but de l'équipe B
      */
     public Goal getGoalB()
     {
         return mGoalB;
     }
 
     /**
      * Retourne le ballon du terrain de foot
      */
     public Ball getBall()
     {
         return mBall;
     }
 }
