 package org.geekgoblin.neditlesmaki.game.systems;
 
import com.artemis.EntitySystem;
 import com.artemis.Aspect;

 
 import org.geekygoblin.nedetlesmaki.game.components.Movable;
 
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
	    Direction dir = e.getComponents(Direction.class);
	    Position pos = e.getComponents(Position.class);
	    int nb_case = e.getComponents(Movable.class).getNbCase();
	    switch(dir.getDir()) {
 	    case "Up" :
 		pos.setY(noOutGrid(pos.getY(), +1*nb_case));
		dir.setDir("None");
 		break;
 	    case "Down" :
 		pos.setY(noOutGrid(pos.getY(), -1*nb_case));
		dir.setDir("None");
 		break;
 	    case "Left" :
 		pos.setX(noOutGrid(pos.getX(), +1*nb_case));
		dir.setDir("None");
 		break;
 	    case "Right" :
		pos.setX(noOutGrid(pos.getX()+1*nb_case));
		dir.setDir("None");
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
