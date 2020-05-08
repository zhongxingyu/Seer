 package fr.utbm.vi51.agent;
 
 import java.util.List;
 
 import org.janusproject.kernel.status.Status;
 import org.janusproject.kernel.status.StatusFactory;
 
 import fr.utbm.vi51.configs.Consts;
 import fr.utbm.vi51.environment.Direction;
 import fr.utbm.vi51.environment.DropFood;
 import fr.utbm.vi51.environment.EatFood;
 import fr.utbm.vi51.environment.Food;
 import fr.utbm.vi51.environment.InsectBody;
 import fr.utbm.vi51.environment.KillEnemy;
 import fr.utbm.vi51.environment.Message;
 import fr.utbm.vi51.environment.Move;
 import fr.utbm.vi51.environment.Perception;
 import fr.utbm.vi51.environment.Pheromone;
 import fr.utbm.vi51.environment.Side;
 import fr.utbm.vi51.environment.Square;
 import fr.utbm.vi51.environment.WorldObject;
 import fr.utbm.vi51.util.PathFinder;
 import fr.utbm.vi51.util.Point3D;
 
 enum WarriorBehaviour {
     GO_HOME, PATROL, FIGHT,
 }
 
 /**
  * @author Top-K
  * 
  */
 public class Warrior extends Ant {
     private WarriorBehaviour currentBehaviour;
     private Point3D lastPosition;
     private Point3D relativeStartingPointPosition; // Remembers the position of
     private static final int attackPoints = 1;
 
     public Warrior(Point3D position, int speed, Side side) {
         super(side.getWarriorTexture(), position, speed, side);
         currentBehaviour = WarriorBehaviour.PATROL;
     }
 
     @Override
     public Status activate(Object... params) {
         lastTime = this.getTimeManager().getCurrentDate().getTime();
         return StatusFactory.ok(this);
     }
 
     @Override
     public Status live() {
         super.live();
         InsectBody body = this.getBody();
 
         //If their is no body, the agent is waiting to die
         if (body == null) {
             return null;
         }
 
         // If an action is already planned, wait for it to be resolved
         if (body.getAction() != null) {
             return null;
         }
 
         // Worker will only act every ANTACTIONDELAY milliseconds
         if (this.getTimeManager().getCurrentDate().getTime() - lastTime < Consts.ANTACTIONDELAY
                 && lastTime != 0) {
             return null;
         }
 
         if (relativeStartingPointPosition != null) {
             relativeStartingPointPosition.x -= body.getPosition().x
                     - lastPosition.x;
             relativeStartingPointPosition.y -= body.getPosition().y
                     - lastPosition.y;
         }
         lastPosition = new Point3D(body.getPosition());
 
         Perception currentPerception = this.getBody().getPerception();
 
         /*if (dropPheromoneIfNeeded()) {
             return null;
         }*/
 
         if (body.isHungry()) {
             if (body.getCarriedObject() instanceof Food) {
                 body.setAction(new EatFood(body));
                 lastTime = this.getTimeManager().getCurrentDate().getTime();
                 return null;
             }
             currentBehaviour = WarriorBehaviour.GO_HOME;
         }
 
         switch (currentBehaviour) {
             case GO_HOME:
                 goHome();
                 break;
             case PATROL:
                 patrol();
                 break;
             case FIGHT:
                 fight();
                 break;
             default:
                 break;
         }
 
         if (movementPath != null && !movementPath.isEmpty()) {
             lastTime = this.getTimeManager().getCurrentDate().getTime();
             Move m = new Move(body, movementPath.removeFirst());
             body.setAction(m);
             return null;
         }
 
         /*
          * If after behavior functions, no action has been defined and movement
          * path is empty, the worker doesn't know what to do, so move randomly .
          */
         if (body.getAction() == null
                 && (movementPath == null || movementPath.isEmpty())) {
             Square[][][] perceivedMap = currentPerception.getPerceivedMap();
             // Find a crossable square
             int x;
             int y;
             do {
                 x = (int) Math.floor(Math.random() * perceivedMap.length);
                 y = (int) Math.floor(Math.random() * perceivedMap[0].length);
             } while (!perceivedMap[x][y][0].getLandType().isCrossable());
             movementPath = PathFinder.findPath(currentPerception
                     .getPositionInPerceivedMap(), new Point3D(x, y, 0),
                     perceivedMap);
         }
         return null;
 
     }
 
     private void goHome() {
         Perception currentPerception = this.getBody().getPerception();
         Square[][][] perceivedMap = currentPerception.getPerceivedMap();
         Pheromone currentBestPheromone = null;
         Point3D currentBestPheromonePositionInPerceivedMap = null;
 
         // If the body is carrying food and we are on the queen's square, drop
         // the food
         // If the body is hungry and there is food on the same square, eat it 
         for (WorldObject wo : perceivedMap[currentPerception
                 .getPositionInPerceivedMap().x][currentPerception
                 .getPositionInPerceivedMap().y][0].getObjects()) {
             if (this.getBody().isHungry() && wo instanceof Food) {
                 this.getBody().setAction(new EatFood(this.getBody()));
                 return;
             }
             if (!this.getBody().isHungry()
                     && wo.getTexturePath().equals(
                             this.getBody().getSide().getQueenTexture())) {
                 if (this.getBody().getCarriedObject() != null) {
                     this.getBody().setAction(new DropFood(this.getBody()));
                 }
                 currentBehaviour = WarriorBehaviour.PATROL;
                 relativeStartingPointPosition = new Point3D(0, 0, 0);
                 return;
             }
         }
 
         // Look for the queen, for home pheromones, or if hungry for food
         for (int i = 0; i < perceivedMap.length; ++i) {
             for (int j = 0; j < perceivedMap[0].length; ++j) {
                 List<WorldObject> objects = perceivedMap[i][j][0].getObjects();
                 synchronized (objects) {
                     for (WorldObject wo : objects) {
                         if (wo.getTexturePath().equals(
                                 this.getBody().getSide().getQueenTexture())) {
                             movementPath = PathFinder.findPath(
                                     currentPerception
                                             .getPositionInPerceivedMap(),
                                     new Point3D(i, j, 0), perceivedMap);
                             return;
                         } else if (wo instanceof Pheromone) {
                             Pheromone p = (Pheromone) wo;
                             if (p.getMessage() == Message.HOME
                                     && p.getSide().equals(
                                             this.getBody().getSide())) {
                                 currentBestPheromone = Pheromone
                                         .closestToSubject(p,
                                                 currentBestPheromone);
                                 if (currentBestPheromone == p) {
                                     currentBestPheromonePositionInPerceivedMap = new Point3D(
                                             i, j, 0);
                                 }
                             }
                         } else if (this.getBody().isHungry()
                                 && wo instanceof Food) {
                             movementPath = PathFinder.findPath(
                                     currentPerception
                                             .getPositionInPerceivedMap(),
                                     new Point3D(i, j, 0), perceivedMap);
                             return;
                         }
                     }
                 }
             }
         }
         if (currentBestPheromone != null
                 && currentBestPheromonePositionInPerceivedMap != null) {
             movementPath = PathFinder.findPath(
                     //TODO c'était ici que currentBestPheromoneblbla était null
                     currentPerception.getPositionInPerceivedMap(),
                     currentBestPheromonePositionInPerceivedMap, perceivedMap);
         }
     }
 
     private void patrol() {
         Perception p = this.getBody().getPerception();
         Square[][][] perceivedMap = p.getPerceivedMap();
         Point3D positionInPerceivedMap = p.getPositionInPerceivedMap();
         InsectBody enemyBody = null;
         Point3D enemyPositionInPerceivedMap = null;
         for (int i = 0; i < perceivedMap.length && enemyBody == null; ++i) {
             for (int j = 0; j < perceivedMap[0].length && enemyBody == null; ++j) {
                 List<WorldObject> objectsPerceived = perceivedMap[i][j][0]
                         .getObjects();
                 for (WorldObject wo : objectsPerceived) {
                     if (wo instanceof InsectBody) {
                         InsectBody ib = (InsectBody) wo;
                         if (!ib.getSide().equals(this.getBody().getSide())) {
                             enemyBody = ib;
                             enemyPositionInPerceivedMap = new Point3D(i,j,0);
                             break;
                         }
                     }
                 }
             }
         }
         if(enemyBody != null && movementPath != null) {
             if(movementPath.size() == 0) {
                 this.getBody().setAction(new KillEnemy(this.getBody(),Direction.NONE));
                 lastTime = this.getTimeManager().getCurrentDate().getTime();
                 return;
             } else if(movementPath.size() == 1) {
                 this.getBody().setAction(new KillEnemy(this.getBody(),movementPath.getFirst()));
                 lastTime = this.getTimeManager().getCurrentDate().getTime();
                 return;
             }
         }
         
         if (relativeStartingPointPosition != null
                 && Point3D.euclidianDistance(new Point3D(0, 0, 0),
                         relativeStartingPointPosition) > 20) {
             currentBehaviour = WarriorBehaviour.GO_HOME;
         } else if (enemyBody != null) {
             movementPath = PathFinder.findPath(positionInPerceivedMap, enemyPositionInPerceivedMap, perceivedMap);
         } else if (relativeStartingPointPosition != null
                 && Point3D.euclidianDistance(new Point3D(0, 0, 0),
                         relativeStartingPointPosition) > 10) {
             currentBehaviour = WarriorBehaviour.GO_HOME;
         } else {
             this.getBody().setAction(
                     new Move(this.getBody(), Direction.random()));
             lastTime = this.getTimeManager().getCurrentDate().getTime();
         }
     }
 
     private void fight() {
         Perception currentPerception = this.getBody().getPerception();
         Square[][][] perceivedMap = currentPerception.getPerceivedMap();
         Point3D positionInPerceivedMap = currentPerception
                 .getPositionInPerceivedMap();
 
         //If on the same square as the enemy, then attack him
 
         /*for (WorldObject wo : perceivedMap[positionInPerceivedMap.x][positionInPerceivedMap.y][0].getObjects()) {
         	if (wo.getTexturePath().equals("img/Ants/warrior.png") && ((InsectBody) wo).getSide() != this.getBody().getSide()) {
         		InsectBody enemy = ((InsectBody) wo);
         		enemy.setHealthPoints(enemy.getHealthPoints() - this.attackPoints);
         		//if the enemy has no HP then he dies
         		if (enemy.getHealthPoints() <= 0) {
         			this.getBody().setAction(new KillEnemy(this.getBody()));
         			
         		}
         	}
         }*/
 
         /*for (int i = 0; i < perceivedMap.length; ++i) {
             for (int j = 0; j < perceivedMap[0].length; ++j) {
                 List<WorldObject> objectsPerceived = perceivedMap[i][j][0]
                         .getObjects();
                 synchronized (objectsPerceived) {
                     for (WorldObject wo : objectsPerceived) {
                         if (wo.getTexturePath().equals("img/Ants/warrior.png")
                                 && ((InsectBody) wo).getSide() != this
                                         .getBody().getSide()) {
                             //if the enemy is not on the same square then try to reach it
                             if (Point3D.euclidianDistance(this.getBody()
                                     .getPosition(), wo.getPosition()) != 0) {
                                 System.out.println("enemy side :"
                                         + ((InsectBody) wo).getSide());
                                 System.out.println("side :"
                                         + this.getBody().getSide());
                                 System.out.println("enemy pos : "
                                         + wo.getPosition());
                                 System.out.println("pos : "
                                         + this.getBody().getPosition());
                                 movementPath = PathFinder.findPath(
                                         currentPerception
                                                 .getPositionInPerceivedMap(),
                                         new Point3D(i, j, 0), perceivedMap);
                                 return;
                             } else {
                                 //else fight the enemy
                                 System.out.println("FIGHT");
                                 InsectBody enemy = ((InsectBody) wo);
                                 enemy.setHealthPoints(enemy.getHealthPoints()
                                         - this.attackPoints);
                                 System.out.println("HP :"
                                         + enemy.getHealthPoints());
                                 if (enemy.getHealthPoints() <= 0) {
                                     //enemy dies !
                                     this.getBody().setAction(
                                             new KillEnemy(this.getBody()));
                                 }
 
                             }
                         }
                     }
                 }
             }
         }*/
 
     }
 
 }
