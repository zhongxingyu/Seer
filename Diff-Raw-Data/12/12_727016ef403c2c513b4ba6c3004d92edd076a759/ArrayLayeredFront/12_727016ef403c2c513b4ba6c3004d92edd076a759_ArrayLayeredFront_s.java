 package trellises;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 public class ArrayLayeredFront<T extends PathTracker> extends AbstractFront<T> implements Front<T> {
 	public static int LAYERS_MAX = 512;
	protected Map<Long, T> layers[] = new HashMap[LAYERS_MAX];
 	/**
 	 * Если фронт содержит хотя бы один не пустой ярус, {@code minLayer} равен индексу первого яруса, содержащего вершины, иначе <code>minLayer > maxLayer</code>.   
 	 */
 	protected int minLayer = Integer.MAX_VALUE;
 	/**
 	 * Если фронт содержит хотя бы один не пустой ярус, {@code maxLayer} равен индексу последнего яруса, содержащего вершины, иначе <code>maxLayer < minLayer</code>.   
 	 */
 	protected int maxLayer = Integer.MIN_VALUE;
 	
 	public ArrayLayeredFront() {
		
 	}
 	
 	@Override
 	public int size() {
 		return size;
 	}
 
 	@Override
 	public Iterator<T> iterator() {
 		return new Iterator<T> () {
 			int layerIndex = minLayer - 1;
 			Iterator<T> iter = null;
 			@Override
 			public boolean hasNext() {
 				return (iter != null && iter.hasNext()) || layerIndex < maxLayer;
 			}
 
 			@Override
 			public T next() {
 				if (iter == null || !iter.hasNext()) {
 					if (layerIndex >= maxLayer) {
 						throw new NoSuchElementException();
 					}
 					do {
 						++layerIndex;
 					} while (layers[layerIndex] == null);
 					
 					iter = layers[layerIndex].values().iterator();
 				}
 				return iter.next();
 			}
 
 			@Override
 			public void remove() {
 				iter.remove();
 				--size;
 				if (layers[layerIndex].isEmpty()) {
 					removeLayer(layerIndex);
 				}
 			}
 		};
 	}
 
 	@Override
 	public boolean add(T t) {
 		Map<Long, T> layerMap = layers[t.layer()];
 		if (layerMap == null) {
 			layerMap = new HashMap<Long, T>();
 			layers[t.layer()] = layerMap;
 			
 			minLayer = Math.min(minLayer, t.layer());
			maxLayer = Math.max(maxLayer, minLayer);
 		}
 		
 		if (layerMap.containsKey(t.vertexIndex())) {
 			return false;
 		}
 		layerMap.put(t.vertexIndex(), t);
 		++size;
 		return true;
 	}
 
 	@Override
 	public T get(int layer, long vertexIndex) {
 		Map<Long, T> layerMap = layers[layer];
 		if (layerMap == null) {
 			return null;
 		}
 		return layerMap.get(vertexIndex);
 	}
 
 	@Override
 	public boolean remove(int layer, long vertexIndex) {
 		Map<Long, T> layerMap = layers[layer];
 		if (layerMap == null) {
 			return false;
 		}		
 		T removed = layerMap.remove(vertexIndex);
 		if (removed != null) {
 			--size;
 			if (layerMap.isEmpty()) {
 				removeLayer(layer);
 			}
 		}
 		return true;
 	}
 
 	private void removeLayer(int layer) {
 		layers[layer] = null;
 		if (size == 0) {
 			minLayer = Integer.MAX_VALUE;
 			maxLayer = Integer.MIN_VALUE;
 		} else {
 			if (layer == maxLayer) {
 				do {
 					--maxLayer;
 				} while (layers[maxLayer] == null);
 			} else if (layer == minLayer) {
 				do {
 					++minLayer;
 				} while (layers[minLayer] == null);
 			}
 		}
 	}
 	
 	@Override
 	public boolean contains(int layer, long vertexIndex) {
 		Map<Long, T> layerMap = layers[layer];
 		if (layerMap == null) {
 			return false;
 		}
 		return layerMap.containsKey(vertexIndex);
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return size == 0;
 	}
 
 	@Override
 	public void clear() {
 		for (int layer = minLayer; layer <= maxLayer; ++layer) {
 			if (layers[layer] != null) {
 				layers[layer].clear();
 				layers[layer] = null;
 			}
 		}
 		minLayer = 0;
 		maxLayer = -1;
 	}
 }
