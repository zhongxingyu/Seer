 package net.latroquette.common.database.data;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.latroquette.common.database.IDatabaseConstants;
 import net.latroquette.common.database.data.keyword.DataObject;
 import net.latroquette.common.database.session.DatabaseSession;
 import net.latroquette.common.util.optimizer.CommonValues;
 
 public abstract class AbstractDAO<T extends DataObject> extends DatabaseSession{
 	
 
 	public AbstractDAO(DatabaseSession db){
 		super(db);
 	}
 	
 	public AbstractDAO(){
 		super();
 	}
 	public AbstractDAO(boolean initDbSession){
 		super(initDbSession);
 	}
 	
 	public T getDataObjectById(Integer id, Class<? extends T> clazz){
 		@SuppressWarnings("unchecked")
 		T data = (T)this.session.get(clazz, id);
 		return data ;
 	}
 	
 	public void modify(T data){
		if(data.getDatabaseOperation() == IDatabaseConstants.DEFAULT){
			if(data.getId() == null){
				data.setDatabaseOperation(IDatabaseConstants.INSERT);
			}else{
				data.setDatabaseOperation(IDatabaseConstants.UPDATE);
			}
 		}
 		persist(data);
 	}
 	
 	public Collection<T>  modifyDataObject(Collection<T> data){
 		return modifyDataObject(data, false);
 	}
 	public Collection<T>  modifyDataObject(Collection<T> data, boolean merge){
 		Set<T> dataSet = null;
 		if(data instanceof Set){
 			dataSet = (Set<T>) data;
 		}else{
 			dataSet  = new HashSet<T>(data);
 		}
 		for(T datum : dataSet){
 			if(datum != null){
 				modify(datum);
 			}
 		}
 		if(commit()){
 			return data;
 		}else{
 			return null;
 		}
 	}
 	public T  modifyDataObject(T data){
 		modify(data);
 		if(commit()){
 			return data;
 		}else{
 			return null;
 		}
 	}
 	
 	public boolean deleteDataObject(T data){
 		data.setDatabaseOperation(IDatabaseConstants.DELETE);
 		persist(data);
 		return commit();
 	}
 	
 	public T getDataObject(AbstractDataObject model)
 	{
 		@SuppressWarnings("unchecked")
 		T toReturn = (T)session.get(model.getClass(), model.getId());
 
 		return toReturn;
 	}
 }
