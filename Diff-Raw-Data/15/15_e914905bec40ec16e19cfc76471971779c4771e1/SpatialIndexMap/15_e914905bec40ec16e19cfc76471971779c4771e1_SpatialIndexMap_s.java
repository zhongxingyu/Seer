 package platformer.core.model.systems.impl.culling;
 
 import platformer.core.model.GameObject;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.IntArray;
 import com.badlogic.gdx.utils.IntMap;
 
 public class SpatialIndexMap<T> extends IntMap<IntMap<Array<T>>> {
 	public SpatialIndexMap() {
 		super();
 
 	}
 
 	public Array<T> get(IntArray coordinates) {
 		int x = coordinates.get(0);
 		int y = coordinates.get(1);
 
 		IntMap<Array<T>> yCell = get(x);
 
 		if (yCell == null) {
 			yCell = new IntMap<Array<T>>();
 			super.put(x, yCell);
 		}
 
 		Array<T> cell = yCell.get(y);
 
 		if (cell == null) {
 			cell = new Array<T>();
 			yCell.put(y, cell);
 		}
 
 		return cell;
 	}
 
 	public void add(IntArray coordinates, T object) {
 		int x = coordinates.get(0);
 		int y = coordinates.get(1);
 		IntMap<Array<T>> yCell = get(x);
 		Array<T> backingArray;
 
 		if (yCell == null) {
 			yCell = new IntMap<Array<T>>();
 			put(x, yCell);
 		}
 
 		backingArray = yCell.get(y);
 		if (backingArray == null) {
 			backingArray = new Array<T>();
 			yCell.put(y, backingArray);
 		}
 
 		if (backingArray.contains(object, true)) {
 			return;
 		}

 		backingArray.add(object);
 	}
 
 	public void remove(IntArray coordinates) {
 		Array<T> bucket = get(coordinates);
 		bucket.clear();
 	}
 
 }
