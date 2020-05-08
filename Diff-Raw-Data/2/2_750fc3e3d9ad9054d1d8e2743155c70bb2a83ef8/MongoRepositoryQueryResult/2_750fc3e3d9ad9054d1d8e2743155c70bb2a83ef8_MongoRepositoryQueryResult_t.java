 package com.blockmar.persistence.mongo;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import com.blockmar.persistence.RepositoryQueryResult;
 import com.google.code.morphia.query.Query;
 
 public class MongoRepositoryQueryResult<T> implements RepositoryQueryResult<T> {
 	
 	private final Query<? extends MongoRepositoryObject<T>> query;
 	
 	public MongoRepositoryQueryResult(Query<? extends MongoRepositoryObject<T>> query) {
 		this.query = query;
 	}
 	
 	public List<T> all() {
 		List<T> result = new ArrayList<T>();
 		for (MongoRepositoryObject<T> object : query) {
 			result.add(object.get());
 		}
 		return result;
 	}
 
 	public List<T> limit(int count) {
 		List<T> result = new ArrayList<T>();
 		Iterator<? extends MongoRepositoryObject<T>> iterator = query.iterator();
 		
		while (iterator.hasNext() && result.size() < count) {
 			result.add(iterator.next().get());
 		}
 		return result;
 	}
 	
 	@Override
 	public Iterator<T> iterator() {
 		return new RepositoryQueryResultIterator(query.iterator());
 	}
 	
 	private class RepositoryQueryResultIterator implements Iterator<T> {
 
 		private final Iterator<? extends MongoRepositoryObject<T>> iterator;
 		
 		public RepositoryQueryResultIterator(
 				Iterator<? extends MongoRepositoryObject<T>> iterator) {
 			this.iterator = iterator;
 		}
 
 		@Override
 		public boolean hasNext() {
 			return iterator.hasNext();
 		}
 
 		@Override
 		public T next() {
 			MongoRepositoryObject<T> object = iterator.next();
 			return (object == null ? null : object.get());
 		}
 
 		@Override
 		public void remove() {
 			iterator.remove();
 		}
 		
 	}
 }
