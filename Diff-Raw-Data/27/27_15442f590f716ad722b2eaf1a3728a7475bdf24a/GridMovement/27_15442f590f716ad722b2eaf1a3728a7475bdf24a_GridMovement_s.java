 package propra2012.gruppe33.bomberman.graphics.rendering.scenegraph.grid.movement;
 
 import java.awt.Point;
 import java.util.EnumMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import propra2012.gruppe33.bomberman.GameRoutines;
 import propra2012.gruppe33.bomberman.graphics.rendering.scenegraph.grid.input.Input;
 import propra2012.gruppe33.bomberman.graphics.rendering.scenegraph.grid.items.ItemSpawner;
 
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.Entity;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.GraphicsEntity;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.math.Grid;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.math.MathExt;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.math.Vector2f;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.math.Vector2f.Direction;
 import com.indyforge.twod.engine.graphics.rendering.scenegraph.network.input.InputChange;
 import com.indyforge.twod.engine.util.iteration.FilteredIterator;
 
 /**
  * This class manages the grid movement. If you attach this entity to an entity
  * the entity will react (process movement) using the given input map.
  * 
  * @author Christopher Probst
  * @author Malte Schmidt
  * @author Matthias Hesse
  */
 public final class GridMovement extends Entity {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	/*
 	 * Important state variables. Used to decide when to change the key
 	 * priority.
 	 */
 	private float lv = 0, lh = 0;
 
 	// A local movement var
 	private final Vector2f movement = Vector2f.zero();
 
 	// Stores the moving state of this entity
 	private boolean moving = false;
 
 	// Stores the last direction (useful for sprite animation)
 	private Direction direction = Direction.North;
 
 	// The input map
 	private final Map<Direction, Boolean> inputMap = new EnumMap<Direction, Boolean>(
 			Direction.class);
 
 	private void processMovement(boolean negative, boolean vertical,
 			GraphicsEntity gridEntity, GraphicsEntity node,
 			GraphicsEntity graphicsEntity, Vector2f pos, float maxSpeed,
 			Vector2f dest, float tpf) {
 
 		// Get center vector
 		Vector2f center = pos.round();
 
 		// Get point
 		Point nearest = center.point();
 
 		// Calc dominant site
 		boolean negativeDominant = (vertical ? center.x - pos.x : center.y
 				- pos.y) > 0f;
 
 		// Is the entity already centered ?
 		boolean isCentered = vertical ? MathExt.equals(pos.x, center.x)
 				: MathExt.equals(pos.y, center.y);
 
 		// Get the grid
 		Grid grid = gridEntity.typeProp(Grid.class);
 
 		// Calc direction
 		int dir = negative ? -1 : 1;
 
 		// Calc the max velocity using tpf, maxSpeed
 		float maxMovement = tpf * maxSpeed,
 
 		// Calc the movement based on the grid
 		movement = dir
 				* GameRoutines.lineOfSight(gridEntity,
 						GameRoutines.VELOCITY_NODE_FILTER, pos, maxMovement,
 						vertical ? (negative ? Direction.North
 								: Direction.South) : (negative ? Direction.West
 								: Direction.East));
 
		System.out.println(Math.abs(movement));
 
 		// If movement != 0 -> We can move!
 		boolean canMove = !MathExt.equals(movement, 0);
 
 		/*
 		 * If centered and able to move!
 		 */
 		if (isCentered && canMove) {
 
 			// Simply apply the movement
 			if (vertical) {
 				dest.y += movement;
 			} else {
 				dest.x += movement;
 			}
 		} else {
 
 			// The offset
 			int offset = 0;
 
 			/*
 			 * Very important. If we can not move (into the default direction)
 			 * we have to check the nearest corner. This applies to centered /
 			 * non-centered positions.
 			 */
 			if (!canMove) {
 				// The valid flag!
 				boolean valid = false;
 
 				/*
 				 * This is a very tricky for-loop: We want to loop while i < max
 				 * and valid == false. The max value depends on the isCentered
 				 * flag.
 				 */
 				for (int i = 0, max = isCentered ? 2 : 1; i < max && !valid; i++) {
 
 					// Calc the dominant direction (offest)
 					offset = negativeDominant ? -1 : 1;
 
 					// Calc dom a
 					Point a = new Point(nearest.x + (vertical ? offset : 0),
 							nearest.y + (vertical ? 0 : offset));
 
 					// Calc dom b
 					Point b = new Point(nearest.x + (vertical ? offset : dir),
 							nearest.y + (vertical ? dir : offset));
 
 					// Is the dom path valid ?
 					valid = GameRoutines.VELOCITY_NODE_FILTER.accept(gridEntity
 							.childAt(grid.index(a)))
 							&& GameRoutines.VELOCITY_NODE_FILTER
 									.accept(gridEntity.childAt(grid.index(b)));
 
 					// Swap
 					negativeDominant = !negativeDominant;
 				}
 
 				/*
 				 * If the movement is not valid, exit here!
 				 */
 				if (!valid) {
 					return;
 				}
 			}
 
 			/*
 			 * We have calculated the field of interest successfully.
 			 */
 			Vector2f fieldOfInterest = new Vector2f(nearest.x
 					+ (vertical ? offset : 0), nearest.y
 					+ (vertical ? 0 : offset));
 
 			// Recalc the distance
 			float centerDistance = vertical ? fieldOfInterest.x - pos.x
 					: fieldOfInterest.y - pos.y;
 
 			// Calc the orthogonal movement
 			float orthogonalMovement = (centerDistance >= 0 ? 1f : -1f)
 					* maxMovement;
 
 			// Limit the speed...
 			if (Math.abs(centerDistance) < Math.abs(orthogonalMovement)) {
 				orthogonalMovement = centerDistance;
 			}
 
 			// Move orthogonal
 			if (vertical) {
 				dest.x += orthogonalMovement;
 			} else {
 				dest.y += orthogonalMovement;
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.indyforge.twod.engine.graphics.rendering.scenegraph.Entity#onUpdate
 	 * (float)
 	 */
 	@Override
 	protected void onUpdate(float tpf) {
 
 		/*
 		 * LOOKUP THE CONTROLLED PARENT, THE NODE AND THE GRID ENTITY!
 		 */
 
 		// Get and filter parents...
 		Iterator<Entity> parents = new FilteredIterator<Entity>(
 				GraphicsEntity.TYPE_FILTER, parentIterator(false));
 
 		if (!parents.hasNext()) {
 			return;
 		}
 
 		// Convert parent
 		GraphicsEntity controlledParent = (GraphicsEntity) parents.next();
 
 		if (!parents.hasNext()) {
 			return;
 		}
 
 		// Convert node
 		GraphicsEntity node = (GraphicsEntity) parents.next();
 
 		if (!parents.hasNext()) {
 			return;
 		}
 
 		// Convert grid entity
 		GraphicsEntity gridEntity = (GraphicsEntity) parents.next();
 
 		/*
 		 * PROCESS THE MOVEMENT!
 		 */
 
 		// The node position
 		Vector2f offset = node.position();
 
 		// The position of the controlled parent
 		Vector2f position = controlledParent.position();
 
 		// The absolute position
 		Vector2f absolutePosition = offset.add(position);
 
 		// Look up the speed on the given field
 		Float maxSpeedObj = node.typeProp(Float.class);
 
 		// Check for speed property
 		if (maxSpeedObj == null) {
 			throw new IllegalStateException("The node " + node
 					+ " does not have a speed property.");
 		}
 
 		// Calc the max speed
 		float maxSpeed = maxSpeedObj.floatValue()
 				* controlledParent.typeProp(ItemSpawner.class)
 						.speedPercentage();
 
 		// Init the input flags
 		boolean north = hasInputFor(Direction.North), south = hasInputFor(Direction.South), west = hasInputFor(Direction.West), east = hasInputFor(Direction.East);
 
 		// Set zero
 		movement.setZero();
 
 		// Check both axes
 		boolean vertical = north != south, horizontal = west != east;
 
 		/*
 		 * Never process two axes at the same time.
 		 */
 		if (vertical != horizontal) {
 
 			if (vertical) {
 				// Calc vertical movement
 				processMovement(north, true, gridEntity, node,
 						controlledParent, absolutePosition, maxSpeed, movement,
 						tpf);
 			} else {
 				// Calc horizontal movement
 				processMovement(west, false, gridEntity, node,
 						controlledParent, absolutePosition, maxSpeed, movement,
 						tpf);
 			}
 		} else if (vertical) {
 
 			/*
 			 * Well, both axes at the same time is complicated. To provide a
 			 * nice gameplay we should work with priorities. We do this with
 			 * checking the last direction of our entity. This direction has
 			 * higher priority as long as the entity has not walked one raster
 			 * size. After this the other axes gets higher priority. It is not
 			 * perfect but a beginning.
 			 */
 			switch (direction) {
 			case North:
 			case South:
 
 				if (lv >= 1.25f) {
 
 					// Calc horizontal movement
 					processMovement(west, false, gridEntity, node,
 							controlledParent, absolutePosition, maxSpeed,
 							movement, tpf);
 				} else {
 					// Calc vertical movement
 					processMovement(north, true, gridEntity, node,
 							controlledParent, absolutePosition, maxSpeed,
 							movement, tpf);
 				}
 
 				break;
 
 			case West:
 			case East:
 
 				if (lh >= 1.25f) {
 
 					// Calc vertical movement
 					processMovement(north, true, gridEntity, node,
 							controlledParent, absolutePosition, maxSpeed,
 							movement, tpf);
 
 				} else {
 					// Calc horizontal movement
 					processMovement(west, false, gridEntity, node,
 							controlledParent, absolutePosition, maxSpeed,
 							movement, tpf);
 				}
 
 				break;
 			}
 		}
 
 		// Limit the x velocity
 		movement.x = MathExt.absClamp(movement.x, 0f, maxSpeed * tpf);
 
 		// Limit the y veloctiy
 		movement.y = MathExt.absClamp(movement.y, 0f, maxSpeed * tpf);
 
 		// Calc thresholds
 		boolean xT = MathExt.equals(movement.x, 0), yT = MathExt.equals(
 				movement.y, 0);
 
 		// Look if at least one component is 0
 		if (xT || yT) {
 
 			// Set to true
 			moving = true;
 
 			if (!xT) {
 				if (movement.x > 0) {
 					direction = Direction.East;
 				} else {
 					direction = Direction.West;
 				}
 
 				// Add raster distance
 				lv = 0;
 				lh += Math.abs(movement.x);
 			} else if (!yT) {
 				if (movement.y > 0) {
 					direction = Direction.South;
 				} else {
 					direction = Direction.North;
 				}
 
 				// Add raster distance
 				lh = 0;
 				lv += Math.abs(movement.y);
 			} else {
 				// This entity is not moving anymore
 				moving = false;
 			}
 
 			// Finally add the new movement component
 			controlledParent.position().addLocal(movement);
 
 			/*
 			 * Very important! This method rearranges the node because the
 			 * change of the controller will not be visible yet. Other
 			 * controllers could cause collisions.
 			 */
 			GameRoutines.rearrangeGridNode(node);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.indyforge.twod.engine.graphics.rendering.scenegraph.Entity#onEvent
 	 * (com.indyforge.twod.engine.graphics.rendering.scenegraph.Entity,
 	 * java.lang.Object, java.lang.Object[])
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onEvent(Entity source, Object event, Object... params) {
 		super.onEvent(source, event, params);
 
 		if (event == InputChange.class) {
 
 			Map<Input, Boolean> input = (Map<Input, Boolean>) params[0];
 			inputMap.put(Direction.North, input.get(Input.Up));
 			inputMap.put(Direction.South, input.get(Input.Down));
 			inputMap.put(Direction.West, input.get(Input.Left));
 			inputMap.put(Direction.East, input.get(Input.Right));
 		}
 	}
 
 	/**
 	 * @return the input map of this controller.
 	 */
 	public Map<Direction, Boolean> inputMap() {
 		return inputMap;
 	}
 
 	/**
 	 * @param dir
 	 *            The direction.
 	 * @return true if the input map is set for the given direction, otherwise
 	 *         false.
 	 */
 	public boolean hasInputFor(Direction dir) {
 		Boolean value = inputMap.get(dir);
 		return value != null ? value : false;
 	}
 
 	/**
 	 * @return the last direction of the entity.
 	 */
 	public Direction direction() {
 		return direction;
 	}
 
 	/**
 	 * @return the moving flag.
 	 */
 	public boolean isMoving() {
 		return moving;
 	}
 }
