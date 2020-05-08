 package org.geekgoblin.neditlesmaki.game.systems;
 
 import com.artemis.Aspect;
import com.artemis.Entity;
import com.artemis.systems.EntityProcessingSystem;
 
 import org.geekygoblin.nedetlesmaki.game.components.Movable;
import org.geekygoblin.nedetlesmaki.game.components.Position;
import org.geekygoblin.nedetlesmaki.game.components.Direction;
 
 /*
  *
  * @author natir
  */
 public class MoveSystem extends EntityProcessingSystem {
     
     public MoveSystem(){
 	super(Aspect.getAspectForAll(Movable.class, Position.class, Direction.class));
     }
 
     @Override
     protected void process(Entity e) {
 	if(e.isEnabled()) {
	    Direction dir = e.getComponent(Direction.class);
	    Position pos = e.getComponent(Position.class);
	    int nb_case = e.getComponent(Movable.class).getNbCase();
	    switch(dir.getDirection()) {
 	    case "Up" :
 		pos.setY(noOutGrid(pos.getY(), +1*nb_case));
		dir.setDirection("None");
 		break;
 	    case "Down" :
 		pos.setY(noOutGrid(pos.getY(), -1*nb_case));
		dir.setDirection("None");
 		break;
 	    case "Left" :
 		pos.setX(noOutGrid(pos.getX(), +1*nb_case));
		dir.setDirection("None");
 		break;
 	    case "Right" :
		pos.setX(noOutGrid(pos.getX(), +1*nb_case));
		dir.setDirection("None");
 		break;
 	    case "None" :
 		/*No move*/
 		break;
 	    }
 	}
     }
 
     private int noOutGrid(int pos, int delta){
 	if(pos + delta < 0) {
 	    return 0;
 	}
 	else {
 	    return pos+delta;
 	}
     }
 }
