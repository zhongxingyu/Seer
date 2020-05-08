 package org.jnizon;
 
 import java.util.HashMap;
 
 public class Context extends HashMap<Integer, HeapItem> {
 	private static final long serialVersionUID = 4161192730488414955L;
 
 	private int context_id = 0;
 	private Heap heap;
 	private Context parent;
 
 	public Context(int id, Heap heap) {
 		this.context_id = id;
 		this.heap = heap;
 	}
 
 	public void setParent(Context parent) {
 		this.parent = parent;
 	}
 
 	public boolean containsKey(Identifier id) {
 		return containsKey(Integer.valueOf(id.getName().hashCode()));
 	}
 
 	public Context getParent() {
 		return parent;
 	}
 
 	public Expression get(Identifier id) {
 		HeapItem item = get(id.getName());
 		if (item == null) {
 			if(parent == null) return id;
 			return parent.get(id);
 		}
 			
 		return item.getValue();
 	}
 
 	public HeapItem get(String key) {
 		return get(key.hashCode());
 	}
 
 	public void put(int key, HeapItem value) {
 		put(Integer.valueOf(key), value);
 	}
 
 	public void put(String key, HeapItem value) {
 		put(key.hashCode(), value);
 	}
 
 	public void put(HeapItem value) {
 		put(value.getLabel(), value); // TODO: Change hashCode to MD4
 	}
	
 	public void put(Identifier id, Expression expr) {
 		HeapItem prev = get(id.getName());
 		if(prev == null && parent != null) {
 			parent.put(id, expr);
 		} else {
 			HeapItem item = new HeapItem(id.getName(), expr);
 			put(item);
 		}
 	}
 	
	public void putLocal(Identifier id, Expression expr) {
		HeapItem item = new HeapItem(id.getName(), expr);
		put(item);
	}
	
 	public void remove(Identifier id) {
 		remove(Integer.valueOf(id.getName().hashCode()));
 	}
 	
 	public int getContextId() {
 		return context_id;
 	}
 
 	public Context derivate() {
 		int childId = heap.newContext();
 		Context child = heap.getContext(childId);
 		child.setParent(this);
 		return child;
 	}
 }
