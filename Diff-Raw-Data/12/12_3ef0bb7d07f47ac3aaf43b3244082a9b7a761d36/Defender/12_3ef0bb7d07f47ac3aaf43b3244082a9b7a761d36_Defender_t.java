 /*
  * This file is part of the Turtle project
  *
  * (c) 2011 Julien Brochet <julien.brochet@etu.univ-lyon1.fr>
  *
  * For the full copyright and license information, please view the LICENSE
  * file that was distributed with this source code.
  */
 
 package turtle.behavior.turtle;
 
 import java.awt.geom.Point2D;
 import turtle.entity.field.Ball;
 import turtle.entity.field.Field;
import turtle.entity.field.Goal;
 import turtle.util.Random;
 import turtle.util.geom.Circle2D;
 import turtle.util.geom.Vector2D;
 
 /**
  * Comportement d'un défenseur
  *
  * @author Julien Brochet <julien.brochet@etu.univ-lyon1.fr>
  * @since 1.0
  */
 public class Defender extends AbstractTurtleBehavior
 {
     public Defender(Field field)
     {
         super(field);
     }
 
     @Override
     public void apply(Vector2D vector, long elapsedTime)
     {
         Ball ball = mField.getBall();
         Point2D initialPosition = mTurtle.getInitialPosition();
         Point2D turtlePosition  = mTurtle.getPosition();
         Circle2D circle = new Circle2D(initialPosition.getX(), initialPosition.getY(), mTurtle.getDiameter() * 20);
 
         if (circle.contains(ball.getPosition())) {
             // Le ballon est proche du joueur
             if (mTurtle.isAround(ball)) {
                 Vector2D speed = new Vector2D();
                Goal goal = mField.getOtherGoal(mTurtle.getTeam().getGoal());
 
                speed.set(goal.getRectangle().getCenterX() - turtlePosition.getX(), 0);
                speed.setLength(Random.between(0.05, 0.15));
                speed.rotate(Random.degreesToRadians(-30, +30));
 
                 ball.shoot(mTurtle, speed);
             } else {
                 // Le ballon est proche mais pas encore assez donc
                 // Le joueur avance !
                 Point2D ballPosition = ball.getPosition();
 
                 vector.set(ballPosition.getX() - turtlePosition.getX(), ballPosition.getY() - turtlePosition.getY());
                 vector.setLength(Random.between(0.04, 0.05));
             }
         } else {
             if (turtlePosition.distance(initialPosition) > 0.1) {
                 // Le goal n'est pas à sa place de départ
                 vector.set(initialPosition.getX() - turtlePosition.getX(), initialPosition.getY() - turtlePosition.getY());
                vector.setLength(Random.between(0.03, 0.04));
             }
         }
     }
 }
