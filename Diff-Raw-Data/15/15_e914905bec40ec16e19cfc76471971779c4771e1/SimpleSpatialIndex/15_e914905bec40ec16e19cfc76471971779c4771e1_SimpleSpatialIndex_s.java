 package platformer.core.model.systems.impl.culling;
 
 import platformer.core.model.GameObject;
 import platformer.core.model.SpatialIndex;
 import platformer.core.model.systems.Positionable;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.IntArray;
 
 public class SimpleSpatialIndex<T extends Positionable> implements SpatialIndex<T> {
 	private int cellSize;
 	private SpatialIndexMap<T> cellMap;
 	
 	/**
 	 * The constructor requires both the cellSize (usually enough to fit a
 	 * regular object bounding box) and width required for projecting a spatial
 	 * index into 1D. Usually the width is expected to be provided by the Level
 	 * object.
 	 * 
 	 * @param cellSize
 	 * @param initialWidth
 	 */
 	public SimpleSpatialIndex(int cellSize) {
 		this.cellSize = cellSize;
 		cellMap = new SpatialIndexMap<T>();
 	}
 
 	/**
 	 * The function takes a bounded object as a parameter and computes what grid
 	 * cells it hashes to.
 	 * 
 	 * @param obj
 	 * @return void
 	 */
 	@Override
 	public void addObject(T object) {
 		if (object instanceof Positionable) {
 			Positionable obj = (Positionable) object;
 			Vector3 objPos = obj.getPosition();
 			Rectangle objBounds = obj.getBounds();
 			Rectangle boundingBox = new Rectangle(objPos.x, objPos.y, objBounds.width, objBounds.height);
 
 			Array<IntArray> affectedCells = getAffectedCells(boundingBox);
 
 			for (int i = 0; i < affectedCells.size; i++) {
 				cellMap.add(affectedCells.get(i), object);
 			}
 		} else {
 			Gdx.app.error("SimpleSpatialIndex", "Object not added; GameObject is expected to be instance of Positionable");
 		}
 	}
 
 	private Vector2 getBucketId(Vector2 point) {
 		Vector2 gridCell = new Vector2();
 
 		gridCell.x = (point.x - (point.x % cellSize)) / cellSize;
 		gridCell.y = (point.y - (point.y % cellSize)) / cellSize;
 
 		return gridCell;
 	}
 
 	/**
 	 * Returns an array of cells affected by the AABB query
 	 * 
 	 * Make sure to dispose of the result once done
 	 * 
 	 * @param aabb
 	 * @return Array<Vector2>
 	 */
 	private Array<IntArray> getAffectedCells(Rectangle aabb) {
 		Array<IntArray> affectedCells = new Array<IntArray>(2);
 
 		Vector2 frustrumMinPoint = getBucketId(new Vector2(aabb.x - 100, aabb.y - 100));
 		Vector2 frustrumMaxPoint = getBucketId(new Vector2(aabb.x + aabb.width + 100, aabb.y + aabb.height + 100));
 
 		for (int x = (int) frustrumMinPoint.x; x <= frustrumMaxPoint.x; x++) {
 			for (int y = (int) frustrumMinPoint.y; y <= frustrumMaxPoint.y; y++) {
 				affectedCells.add(new IntArray(new int[]{x, y}));
 			}
 		}
 
 		return affectedCells;
 	}
 
 	@Override
 	public Array<T> getObjects(Rectangle aabb) {
 		Array<IntArray> affectedCells = getAffectedCells(aabb);
 		Array<T> objects = new Array<T>();
 
 		for (int i = 0; i < affectedCells.size; i++) {
 			Array<T> currentCell = cellMap.get(affectedCells.get(i));
 
 			for (T gameObject : currentCell) {
 				if (!objects.contains(gameObject, true)) {
 					objects.add(gameObject);
 				}
 			}
 		}
 
 		return objects;
 	}
 
 	public void updateObjects(Rectangle aabb) {
 		Array<IntArray> affectedCells = getAffectedCells(aabb);
 		Array<T> objects = new Array<T>(getObjects(aabb));

 		for (int i = 0; i < affectedCells.size; i++) {
 			cellMap.remove(affectedCells.get(i));
 		}
 
 		for (T gameObject : objects) {
 			addObject(gameObject);
 		}
 	}
 }
