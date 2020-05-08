 package eu.abra.prima;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.sun.jersey.api.JResponse;
 
import eu.abra.prima.beans.CrudObject;

public abstract class CrudController<T extends CrudObject<T>> implements ICrudController<T> {
 
 	protected abstract T newObject();
 	
 	@Override
 	public JResponse<List<T>> list() {
 		List<T> list = new ArrayList<T>();
 		list.add(newObject());
		//sd
 		list.add(newObject());
 		list.add(newObject());
 		return JResponse.ok(list).build();
 	}
 	
 	@Override
 	public T get(String id) {
 		return newObject();
 	}
 
 	@Override
 	public T put(T object) {
 		return object;
 	}
 
 	@Override
 	public void delete(String id) {
 	}
 
 	@Override
 	public T post(String id, T object) {
 		return object;
 	}
 }
