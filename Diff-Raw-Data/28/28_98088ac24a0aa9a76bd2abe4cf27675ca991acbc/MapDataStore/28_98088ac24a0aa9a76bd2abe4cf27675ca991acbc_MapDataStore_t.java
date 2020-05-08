 package com.test02.persistence;
 
 import com.google.common.base.Optional;
 import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.test02.exception.ResourceNotFoundException;
 import com.test02.model.Identifiable;
 import com.test02.persistence.datafilter.Query;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 public abstract class MapDataStore<T extends Identifiable> implements DataStore<T> {
 
     private ConcurrentMap<Serializable, T> elements = new ConcurrentHashMap<Serializable, T>();
 
     @Override
     public Optional<T> findById(Serializable id) {
         return Optional.fromNullable(elements.get(id));
     }
 
     @Override
     public T insert(T element) {
         checkNotNull(element);
 
         Serializable id = generateId();
         element.setId(id);
         elements.put(id, element);
 
         return element;
     }
 
     @Override
    public T update(T element) {
         checkNotNull(element);
 
        T replaced = elements.replace(element.getId(), element);
        checkResourceExistent(replaced);

         return element;
     }
 
     @Override
    public void delete(Serializable id) {
         checkNotNull(id);
 
        T removed = elements.remove(id);
        checkResourceExistent(removed);
     }
 
     @Override
     public List<T> getAll() {
        return ImmutableList.copyOf(elements.values());
     }
 
 	@Override
 	public List<T> getFiltered(Query<T> query) {
 		Iterable<T> filtered = Iterables.filter(elements.values(), Predicates.and(query.getPredicates()));
 		return Lists.newArrayList(filtered);
 	}
 
    private void checkResourceExistent(T resource) {
        if (resource == null)
             throw new ResourceNotFoundException();
     }
 
 }
