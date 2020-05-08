 package eu.monniot.memoArcher.activerecord.query;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import eu.monniot.memoArcher.activerecord.Model;
 import eu.monniot.memoArcher.activerecord.annotation.Table;
 
 public class From implements Sqlable {
 
 	private Class<? extends Model> mTable;
 	private Sqlable mQueryBase;
 	private ArrayList<Object> mArguments;
 	
 	private String mWhere = null;
 	private String mOrderBy = null;
 	private Object mLimit;
 
 	/**
 	 * Initialize a new FROM statement
 	 * @param table
 	 * @param queryBase
 	 */
 	public From(Class<? extends Model> table, Sqlable queryBase) {
 		mTable = table;
 		mQueryBase = queryBase;
 		
 		mArguments = new ArrayList<Object>();
 	}
 	
 	/**
 	 * add a WHERE statement
 	 * Example:  where("id = 1")
 	 * 
 	 * @param where
 	 * @return
 	 */
 	public From where(String where) {
 		mWhere = where;
 		mArguments.clear();
 		return this;
 	}
 	
 	/**
 	 * add a WHERE statement with arguments
 	 * Example: where("age > ? AND age < ?", 18, 24)
 	 * @param where
 	 * @param arguments
 	 * @return
 	 */
 	public From where(String where, Object... arguments) {
 		where(where);
 		mArguments.addAll(Arrays.asList(arguments));
 		
 		return this;
 	}
 	
 	/**
 	 * add an ORDER BY statement
 	 * Example: orderBy("id DESC")
 	 * @param orderBy
 	 * @return
 	 */
 	public From orderBy(String orderBy) {
 		mOrderBy = orderBy;
 		return this;
 	}
 	
 	/**
 	 * add a LIMIT statement
 	 * Example: limit("10")
 	 * @param limit
 	 * @return
 	 */
 	public From limit(String limit) {
 		mLimit = limit;
 		return this;
 	}
 
 	/**
 	 * add a LIMIT statement
 	 * Example: limit(10)
 	 * @param limit
 	 * @return
 	 */
 	public From limit(int limit) {
 		return limit(String.valueOf(limit));
 	}
 	
 	/**
 	 * Execute the query builded so far and return the corresponding objects
 	 * @return
 	 */
 	public <T extends Model> List<T> execute() {
 		// execute the query
 		return null;
 	}
 
 	/**
 	 * Execute the query builded so far and return the first corresponding object
 	 * @return
 	 */
 	public <T extends Model> T executeSingle() {
 		limit(1);
 		// Execute the query
 		return null;
 	}
 
 	/**
 	 * Return a list of all the WHERE arguments of this instance
 	 * @return
 	 */
 	public String[] getArguments() {
		return new String[1];
 	}
 	
 	@Override
 	public String toSql() {
 		
 		StringBuilder sql = new StringBuilder();
 
 		sql.append(mQueryBase.toSql());
 
 		// TODO Move this part of code to its own class (maybe a ReflectionUtil class ?)
 		if(!mTable.isAnnotationPresent(Table.class)) {
 			throw new RuntimeException(mTable.toString()+" must implement the Table annotation");
 		}
 		sql.append("FROM " + mTable.getAnnotation(Table.class).name() + " ");
 		
 		
 		if (mWhere != null) {
 			sql.append("WHERE " + mWhere + " ");
 		}
 		
 		if (mOrderBy  != null) {
 			sql.append("ORDER BY " + mOrderBy + " ");
 		}
 		
 		if (mLimit != null) {
 			sql.append("LIMIT " + mLimit + " ");
 		}
 		
 		return sql.toString().trim();
 	}	
 
 }
