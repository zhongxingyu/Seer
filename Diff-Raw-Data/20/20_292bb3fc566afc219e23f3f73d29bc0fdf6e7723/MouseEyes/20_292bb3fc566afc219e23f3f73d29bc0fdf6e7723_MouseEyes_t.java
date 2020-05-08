 package com.ptank.brain.world.simpleworld.mental.mouse;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import com.ptank.brain.world.simpleworld.physical.Bread;
 import com.ptank.brain.world.simpleworld.physical.Cat;
 import com.ptank.brain.world.simpleworld.physical.Mouse;
 import com.ptank.util.gridworld.Tile;
 import com.ptank.util.gridworld.World.Direction;
 
 public class MouseEyes implements NeuralInput {
 
 	private Mouse mouse;
 	private Tile currentTile;
 	
 	private static final Class<?>[] knownObjects = {Mouse.class,Cat.class,Bread.class};
 	
 	public MouseEyes(Mouse mouse) {
 		this.mouse = mouse;
 	}
 
 	@Override
 	public List<Double> getNextInput() {
 		List<Double> result = new ArrayList<Double>(size());
 		for(int i = 0; i < size(); i++) {
 			result.add(0.0);
 		}
 		TileIterator tileIterator = new TileIterator(mouse.getFacingDirection(),currentTile);
 		int tileIndex = 0;
 		while(tileIterator.hasNext()) {
 			Tile tile = tileIterator.next();
			if(tile.isOccupied()) {
				for(int i = 0; i < knownObjects.length; i++) {
					Class<?> obj = knownObjects[i];
					if(tile.getUnitOnTile() != null && obj.isAssignableFrom(tile.getUnitOnTile().getClass())) {
						set(result,i,tileIndex);
						break;
					}
 				}
			} else if(!tile.isPassable()) {
				//In this case we're seeing a wall
				set(result,knownObjects.length,tileIndex);
 			}
 			tileIndex++;
 		}
 		return result;
 	}
 	
 	private void set(List<Double> list, int objectIndex, int squareIndex) {
 		list.set(objectIndex*8+squareIndex,1.0);
 	}
 	
 	public int size() {
		//Each of the objects (plus a wall) could be seen in one of 8 squares.
		return (knownObjects.length+1)*8;
 	}
 	
 	/**     3
 	 *     24
 	 *    X15     --> N
 	 *     86
 	 *      7
 	 */
 	private static class TileIterator implements Iterator<Tile> {
 
 		private Direction baseDirection;
 		private Tile currentTile;
 		private static final Direction [] directions = {Direction.North,Direction.West,Direction.NorthWest,
 				                                        Direction.East,Direction.East,Direction.East,
 				                                        Direction.East,Direction.SouthWest};
 		private int currentIndex = 0;
 		
 		public TileIterator(Direction baseDirection, Tile startingTile) {
 			this.baseDirection = baseDirection;
 			this.currentTile = startingTile;
 		}
 		
 		@Override
 		public boolean hasNext() {
 			return currentIndex < directions.length;
 		}
 
 		@Override
 		public Tile next() {
 			if(!hasNext()) {
 				throw new NoSuchElementException();
 			}
 			Direction toMove = baseDirection.combine(directions[currentIndex]);
 			currentTile = currentTile.getNeighbor(toMove);
 			currentIndex++;
 			return currentTile;
 		}
 
 		@Override
 		public void remove() {
 			throw new RuntimeException("Operation not implemented");
 		}
 
 	}
 	
 }
