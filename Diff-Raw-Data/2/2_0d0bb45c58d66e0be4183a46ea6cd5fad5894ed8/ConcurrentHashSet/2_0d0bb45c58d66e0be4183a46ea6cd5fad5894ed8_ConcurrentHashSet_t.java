 /**
  * 
  */
 package ecologylab.collections;
 
 import java.util.AbstractSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Low locking ConcurrentHashSet uses ConcurrentHashMap to lock only when add actually adds.
  * 
  * @author andruid
  */
 public class ConcurrentHashSet<E extends Object> extends AbstractSet<E>
 {
 	private ConcurrentHashMap<E, E>	map;
 	
 	public ConcurrentHashSet()
 	{
 		map = new ConcurrentHashMap<E, E>();
 	}
 
 	public ConcurrentHashSet(int capacity)
 	{
 		map = new ConcurrentHashMap<E, E>(capacity);
 	}
 
 	public ConcurrentHashSet(Set<E> set)
 	{
 		for (E element: set)
 			map.put(element, element);
 	}
 	/**
 	 * Add element to set if it wasn't there already.
 	 * 
	 * @param	element	Element to test for membership and add if it was absent.
 	 * 
 	 * @return	true if the set changed, that is, if the element was new.
 	 */
 	@Override
 	public boolean add(E element)
 	{
 		E fromCollection = map.putIfAbsent(element, element);
 		return fromCollection == null;	// previous value is null means that the collection changed.
 	}
 
 	@Override
 	public void clear()
 	{
 		map.clear();
 	}
 
 	@Override
 	public boolean contains(Object obj)
 	{
 		return map.containsKey(obj);
 	}
 
 	@Override
 	public Iterator<E> iterator()
 	{
 		return map.keySet().iterator();
 	}
 
 	@Override
 	public boolean remove(Object obj)
 	{
 		return map.remove(obj) != null;
 	}
 
 	@Override
 	public int size()
 	{
 		return map.size();
 	}
 }
