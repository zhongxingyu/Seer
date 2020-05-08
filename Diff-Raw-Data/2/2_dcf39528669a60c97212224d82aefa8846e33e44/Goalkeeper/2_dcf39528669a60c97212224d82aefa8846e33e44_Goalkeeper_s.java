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
 import turtle.util.Vector2D;
 
 /**
  * Comportement d'un gardien de but
  *
  * @author Julien Brochet <julien.brochet@etu.univ-lyon1.fr>
  * @since 1.0
  */
 public class Goalkeeper extends AbstractTurtleBehavior
 {
     public Goalkeeper(Field field)
     {
         super(field);
     }
 
     @Override
     public void apply(Vector2D vector, long elapsedTime)
     {
         double width  = mField.getDimension().getWidth();
         double height = mField.getDimension().getHeight();
         double maxDistance = width * 0.15;
 
         Ball ball = mField.getBall();
         Point2D turtlePosition  = mTurtle.getPosition();
 
         //TODO Il ne faut pas regarder en fonction du joueur mais du goal
         if (mTurtle.isAround(ball, maxDistance)) {
             if (mTurtle.isAround(ball)) {
                 // Le goal est juste a coté du ballon
                 // Il le tire alors à l'autre bout du terrain
                 Vector2D speed = new Vector2D();
                 Goal goal = mField.getOtherGoal(mTurtle.getTeam().getGoal());
 
                speed.set(goal.getRectangle().getCenterX(), Random.between(0, height));
                 speed.setLength(Random.between(0.15, 0.30));
 
                 ball.shoot(mTurtle, speed);
             } else {
                 // Le ballon est proche mais pas encore assez donc
                 // le goal doit avancer vers lui
                 Point2D ballPosition = ball.getPosition();
 
                 vector.set(ballPosition.getX() - turtlePosition.getX(), ballPosition.getY() - turtlePosition.getY());
                 vector.setLength(Random.between(0.01, 0.03));
             }
         } else {
             Point2D initialPosition = mTurtle.getInitialPosition();
 
             if (turtlePosition.distance(initialPosition) > 0.1) {
                 // Le goal n'est pas à sa place de départ
                 vector.set(initialPosition.getX() - turtlePosition.getX(), initialPosition.getY() - turtlePosition.getY());
                 vector.setLength(Random.between(0.01, 0.03));
             }
         }
     }
 }
