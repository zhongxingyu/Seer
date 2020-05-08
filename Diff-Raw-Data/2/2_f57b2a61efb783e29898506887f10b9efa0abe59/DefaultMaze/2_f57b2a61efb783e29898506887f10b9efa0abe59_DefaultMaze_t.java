 package uk.ac.ox.cs.sokobanexam.model;
 
 import java.util.Set;
 
 import uk.ac.ox.cs.sokobanexam.model.sprites.Floor;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Nothing;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Room;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Sprite;
 import uk.ac.ox.cs.sokobanexam.util.FilterIterator;
 import uk.ac.ox.cs.sokobanexam.util.IterableAdapter;
 import uk.ac.ox.cs.sokobanexam.util.MapIterator;
 import uk.ac.ox.cs.sokobanexam.util.Point;
 import uk.ac.ox.cs.sokobanexam.util.PointRangeSet;
 
 /**
  * The standard Sokoban implementation of the {@link Rules}.
  */
 public class DefaultMaze implements Maze {
 	
 	// Class Invariant:
 	//		mMap.length >= 1
 	//		mMap[y].length >= 1
 	private Room[][] mMap;
 	
 	public DefaultMaze(int width, int height) {
 		if (width <= 0 || height <= 0)
 			throw new IllegalArgumentException("Maze size: "+width+"x"+height);
 		
 		// We don't actually add the "new Floor(new Nothing(point))" nothing rooms
 		// here, as a small optimization, mostly in the case of cloning.
 		mMap = new Room[height][width];
 	}
 	
 	@Override
 	public int getHeight() {
 		return mMap.length;
 	}
 
 	@Override
 	public int getWidth() {
 		return mMap[0].length;
 	}
 	
 	@Override
 	public Set<Point> getPoints() {
 		return new PointRangeSet(getWidth(), getHeight());
 	}
 	
 	@Override
 	public Iterable<Room> getRooms() {
 		return new IterableAdapter<Room>(new MapIterator<Point,Room>(getPoints().iterator()) {
 			@Override public Room applyMap(Point point) {
 				return getRoom(point);
 			}
 		});
 	}
 
 	@Override
 	public Room getRoom(Point point) {
 		if (mMap[point.y][point.x] == null) {
 			mMap[point.y][point.x] = new Floor(new Nothing(point));
 		}
 		return mMap[point.y][point.x];
 	}
 
 	@Override
 	public void putRoom(Room room) {
 		Point point = room.point();
 		mMap[point.y][point.x] = room;
 	}
 
 	@Override
 	public Iterable<Room> getRoomsContaining(final Class<? extends Sprite> type) {
 		return new IterableAdapter<Room>(new FilterIterator<Room>(getRooms().iterator()) {
 			@Override public boolean isGood(Room room) {
 				return type.isInstance(room.inner()); 
 			}
 		});
 	}
 
 	@Override
 	public Iterable<Room> getRoomsOfType(final Class<? extends Sprite> type) {
 		return new IterableAdapter<Room>(new FilterIterator<Room>(getRooms().iterator()) {
 			@Override public boolean isGood(Room room) {
 				return type.isInstance(room); 
 			}
 		});
 	}
 	
 	@Override
 	public DefaultMaze clone() {
 		// I don't like that super.clone() creates objects without calling my constructor.
 		DefaultMaze clone = new DefaultMaze(getWidth(), getHeight());
 		// We can use arraycopy because the content of mMap is all immutable.
 		for (int y = 0; y < getHeight(); y++)
			System.arraycopy(mMap[y], 0, clone.mMap[y], 0, getWidth());
 		return clone;
 	}
 }
