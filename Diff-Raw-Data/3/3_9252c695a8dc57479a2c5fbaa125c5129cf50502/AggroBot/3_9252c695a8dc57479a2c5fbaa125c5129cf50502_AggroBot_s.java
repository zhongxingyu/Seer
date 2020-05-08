 package server.model;
 
 import java.util.List;
 
 public class AggroBot extends Bot {
 
 	private static final float MOVE_DIRECTION_MEMORY = 15;
 	private float lastMoveX;
 	private float lastMoveY;
 
 	public AggroBot(int id, float x, float y, String name) {
 		super(id, x, y, name);
 		this.lastMoveX = Float.MAX_VALUE;
 		this.lastMoveY = Float.MAX_VALUE;
 	}
 
 	@Override
 	protected List<Entity> move(Game game, float dX, float dY) {
 		float beforeX = this.getX();
 		float beforeY = this.getY();
 
 		List<Entity> result;
		if (euclideanLength(lastMoveX, lastMoveY) < this.speed / 2)
 			if (abs(lastMoveX) > abs(lastMoveY))
 				result = super.move(game, dX + memoryX(), 0 + memoryY());
 			else
 				result = super.move(game, 0 + memoryX(), dY + memoryY());
 		else
 			result = super.move(game, dX + memoryX(), dY + memoryY());
 
 		float afterX = this.getX();
 		float afterY = this.getY();
 		this.lastMoveX = afterX - beforeX;
 		this.lastMoveY = afterY - beforeY;
 
 		return result;
 	}
 
 	private float memoryX() {
 		return lastMoveX * MOVE_DIRECTION_MEMORY;
 	}
 
 	private float memoryY() {
 		return lastMoveY * MOVE_DIRECTION_MEMORY;
 	}
 
 	private float abs(float f) {
 		return f < 0 ? -f : f;
 	}
 }
