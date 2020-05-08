 package nl.hanze.designpatterns.db;
 
 import com.sun.rowset.CachedRowSetImpl;
 
 import javax.sql.RowSetMetaData;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.sql.SQLException;
 import java.util.Iterator;
 
 public class RowIterator<T> implements Iterator<T> {
 
 	private Class<T> returnType;
 	private CachedRowSetImpl resultSet;
 	
 	protected RowIterator(CachedRowSetImpl toIterate, Class<T> returnType){
 		this.resultSet = toIterate;
 		this.returnType = returnType;
 	}
 	
 	@Override
 	public boolean hasNext() {
 		try {
 			return !this.resultSet.isLast();
 		} catch (SQLException e) {
 			System.err.println("RowIterator : hasNext() -> SQLException : " + e.getMessage());
 		}
 		return false;
 	}
 
 	@Override
 	public T next() {
 		try {
 			if(this.resultSet.next()) {
 				Constructor<T> constructor = (Constructor<T>) this.getConstructor();
 				RowSetMetaData metaData = (RowSetMetaData) this.resultSet.getMetaData();
 
				Type[] parameterTypes = constructor.getGenericParameterTypes();
 
 				if (parameterTypes.length == metaData.getColumnCount()) {
 					Object[] parameters = new Object[parameterTypes.length];
					for(int i=0; i<parameterTypes.length; i++){
						//parameters[i] = this.resultSet.getObject(i, parameterTypes[i].getClass());
						parameters[i] = this.resultSet.getObject(i+1);
 					}
 
					T instance = constructor.newInstance(parameters);
					return instance;
 				}
 			}
 		} catch (SQLException e) {
 			System.err.println("RowIterator : next() -> SQLException : " + e.getMessage());
 		} catch (IllegalAccessException e) {
 			System.err.println("RowIterator : next() -> IllegalAccessException : " + e.getMessage());
 		} catch (InstantiationException e) {
 			System.err.println("RowIterator : next() -> InstantiationException : " + e.getMessage());
 		} catch (InvocationTargetException e) {
 			System.err.println("RowIterator : next() -> InvocationTargetException : " + e.getMessage());
 		}
 
 		return null;
 	}
 
 	@Override
 	public void remove() {
 		throw new UnsupportedOperationException();
 	}
 
 	private Constructor<?> getConstructor() throws SQLException{
 		Constructor<?>[] constructors = this.returnType.getDeclaredConstructors();
 		RowSetMetaData metaData = (RowSetMetaData) this.resultSet.getMetaData();
 
 		for (Constructor<?> constructor : constructors){
			if (constructor.getParameterTypes().length == metaData.getColumnCount()){
 				return constructor;
 			}
 		}
 
 		return null;
 	}
 }
