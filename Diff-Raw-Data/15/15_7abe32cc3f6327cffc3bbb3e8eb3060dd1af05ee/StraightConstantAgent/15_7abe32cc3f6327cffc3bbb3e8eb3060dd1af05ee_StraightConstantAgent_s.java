 package pl.edu.agh.mabics.agents.implementation;
 
 import org.springframework.stereotype.Service;
 import pl.edu.agh.mabics.agents.AbstractAgent;
 import pl.edu.agh.mabics.platform.model.Move;
 import pl.edu.agh.mabics.platform.model.PlatformRequest;
 import pl.edu.agh.mabics.platform.model.PlatformResponse;
 import pl.edu.agh.mabics.platform.model.Vector;
 import pl.edu.agh.mabics.ui.datamodel.util.Coordinates;
 
 import java.util.List;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Mateusz
  * Date: 18.02.13
  * Time: 19:20
  */
 @Service
 public class StraightConstantAgent extends AbstractAgent {
 
     private Random random = new Random();
     private int speed = 0;
 
     @Override
     public PlatformResponse getNextMove(PlatformRequest request) {
         PlatformResponse response = new PlatformResponse();
 
         if (speed == 0) {
            speed = Math.abs(random.nextInt() % 2) + 1; //find random number from set {1,2}
             System.out.println("speed was chosen!: " + speed);
         }
         response.setSpeed(speed);
         Coordinates position = request.getPosition();
         Direction direction = getDirection(request.getDestination());
         for (Move move : request.getAllowedMoves()) {
             if (checkIfMoveInGoodDirection(direction, move, position)) {
                 if (direction.equals(Direction.RIGHT)) move.velocity = new Vector(1, 0);
                 else if (direction.equals(Direction.TOP)) move.velocity = new Vector(0, 1);
                 response.setMove(move);
                 return response;
             }
         }
         if (request.getAllowedMoves().size() == 0) {
             response.setMove(getStayInPlaceMove(position));
             response.setSpeed(0);
         } else response.setMove(request.getAllowedMoves().get(0));
         return response;
     }
 
     @Override
     public void onComplete() {
         speed = 0;
     }
 
     @Override
     public void onCollision() {
     }
 
     @Override
     protected void onNextGame() {
     }
 
     private boolean checkIfMoveInGoodDirection(Direction direction, Move move, Coordinates position) {
         int x = position.getX();
         int y = position.getY();
         switch (direction) {
             case TOP:
                 return (move.getPoint().getX() == x && move.getPoint().getY() <= y);
             case RIGHT:
                 return (move.getPoint().getX() >= x && move.getPoint().getY() == y);
 
         }
         return false;
     }
 
     private Direction getDirection(List<Coordinates> destination) {
         int dx = destination.get(0).getX() - destination.get(1).getX();
         int dy = destination.get(0).getY() - destination.get(1).getY();
         if (dx == 0) {
             return Direction.RIGHT;
         } else if (dy == 0) {
             return Direction.TOP;
         }
         return Direction.NONE;
     }
 
     enum Direction {
         RIGHT,
         TOP,
         NONE;
     }
 }
 
 
 
