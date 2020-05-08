 package org.qrone.r7.appengine;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import org.mozilla.javascript.Scriptable;
 import org.qrone.database.DatabaseCursor;
 import org.qrone.r7.script.Scriptables;
 import org.qrone.r7.script.browser.Function;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.QueryResultIterable;
 import com.google.appengine.api.datastore.QueryResultIterator;
 import com.google.appengine.api.datastore.Query.SortDirection;
 
 public class AppEngineDatastoreCursor implements DatabaseCursor{
 	private DatastoreService db;
 	private int limit = -1;
 	private int current = 0;
 	private Query query;
 	private QueryResultIterable<Entity> iter;
 	private Map map;
 	
 	public AppEngineDatastoreCursor(DatastoreService db, Query query, Map map ) {
 		this.db = db;
 		this.query = query;
 		this.map = map;
 	}
 	
 	private QueryResultIterator<Entity> iterator(){
 		if(iter == null){
 			PreparedQuery pq = db.prepare(query);
 			iter = pq.asQueryResultIterable();
 		}
 		return iter.iterator();
 	}
 	
 
 	@Override
 	public void forEach(Function func) {
 		for (; iterator().hasNext();) {
 			func.call(iterator().next());
 		}
 	}
 
 	@Override
 	public boolean hasNext() {
 		if(limit < 0){
			return false;
 		}else{
 			if(limit <= 0){
 				return false;
 			}else{
 				return iterator().hasNext();
 			}
 		}
 	}
 
 	@Override
 	public DatabaseCursor limit(Number o) {
 		limit = o.intValue();
 		return this;
 	}
 
 	@Override
 	public Map next() {
 		return AppEngineUtil.fromEntity(nextRaw(), map);
 	}
 
 	public Entity nextRaw() {
 		limit--;
 		if(limit != 0)
 			return iterator().next();
 		else
 			return null;
 	}
 
 	@Override
 	public DatabaseCursor skip(Number o) {
 		current += o.intValue();
 		return this;
 	}
 
 	@Override
 	public DatabaseCursor sort(Scriptable o) {
 		return sort(Scriptables.asMap(o));
 	}
 
 	@Override
 	public DatabaseCursor sort(Map o) {
 		for (Iterator iter = o.keySet().iterator(); iter.hasNext();) {
 			Object key = iter.next();
 			if(key instanceof String){
 				Object obj = o.get(key);
 				if(obj instanceof Boolean && ((Boolean)obj).booleanValue())
 					query.addSort((String)key, SortDirection.ASCENDING);
 				else if(obj instanceof Number && ((Number)obj).intValue() > 0)
 					query.addSort((String)key, SortDirection.ASCENDING);
 				else if(obj == null)
 					query.addSort((String)key, SortDirection.ASCENDING);
 				else
 					query.addSort((String)key, SortDirection.DESCENDING);	
 				return this;
 			}
 		}
 		return this;
 	}
 
 }
