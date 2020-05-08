 
 
 package is.hax.spring.cache;
 
 import javax.cache.Cache;
 import java.util.Collection;
 import java.util.Set;
 
 
 /**
  * @author Peter Backlund, Vidar Svansson
  * Borrowed from http://code.google.com/p/feeling-lucky-pictures
  */
 public class CacheHolder <E> {
 	Cache cache;
 
 	public CacheHolder(Cache cache) {
 		this.cache = cache;
 	}
 
     @SuppressWarnings("unchecked")
     public E get(String key) {
         return (E) cache.get(key);
     }
 
     @SuppressWarnings("unchecked")
     public E get(Long key) {
         return (E) cache.get(key);
     }
 
 
     @SuppressWarnings("unchecked")
     public void put(Long key, E element){
         cache.put(key, element);
     }
 
     @SuppressWarnings("unchecked")
     public void put(String key, E element){
         cache.put(key, element);
     }
 
     public void clear() {
         cache.clear();
     }
 
     public void remove(Long id) {
         cache.remove(id);
     }
 
     @SuppressWarnings("unchecked")
    public Collection<E> getAll(Set<Long> keys) {
         return cache.getAll(keys).values();
     }
 
     @SuppressWarnings("unchecked")
     public Collection<E> getAll() {
         return cache.values();
     }
 }
