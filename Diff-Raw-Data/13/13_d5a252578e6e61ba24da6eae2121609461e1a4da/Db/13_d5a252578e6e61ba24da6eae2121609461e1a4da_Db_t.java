 package org.eweb4j.orm;
 
 import org.eweb4j.orm.dao.DAOFactory;
 import org.eweb4j.orm.jdbc.transaction.Trans;
 import org.eweb4j.orm.jdbc.transaction.Transaction;
 import org.eweb4j.orm.sql.Model2Table;
 
 /**
  * Db operator for Active Record Model Helper
  * @author weiwei l.weiwei@163.com
  * @date 2013-1-22 下午03:51:22
  */
 public class Db {
 	
 	public static void createTableIfNotExist(final Class<?> entity){
 		String script = Model2Table.generateOne(entity, false, false);
 		final String[] sqls = script.split(";");
 		
 		Transaction.execute(new Trans() {
 			public void run(Object... args) throws Exception {
 				for (String sql : sqls){
 					if (sql == null || sql.trim().length() == 0)
 						continue;
 					
 					Db.ar(entity).dao().sql(sql).execute();
 				}
 			}
 		});
 	}
 	
 	public static void forceCreateTable(final Class<?> entity){
 		String script = Model2Table.generateOne(entity, true, false);
 		final String[] sqls = script.split(";");
 		
 		Transaction.execute(new Trans() {
 			public void run(Object... args) throws Exception {
 				for (String sql : sqls){
 					if (sql == null || sql.trim().length() == 0)
 						continue;
 					
 					Db.ar(entity).dao().sql(sql).execute();
 				}
 			}
 		});
 	}
 	
 	public static <T> Number[] batchDelete(T[] ts){
 		if (ts == null || ts.length == 0)
 			return null;
 		
 		return DAOFactory.getDeleteDAO().batchDelete(ts);
 	}
 	
 	public static <T> Number[] batchDelete(String dsName, T[] ts){
 		if (ts == null || ts.length == 0)
 			return null;
 		
 		return DAOFactory.getDeleteDAO(dsName).batchDelete(ts);
 	}
 	
 	public static <T> Number[] batchUpdate(T[] ts){
 		if (ts == null || ts.length == 0)
 			return null;
 		
 		return batchUpdate(null, ts);
 	}
 	
 	public static <T> Number[] batchUpdate(T[] ts, String... fields){
 		if (ts == null || ts.length == 0)
 			return null;
 		
 		return DAOFactory.getUpdateDAO().batchUpdate(ts, fields);
 	}
 	
 	public static <T> Number[] batchUpdate(T[] ts, String[] fields, Object[] values){
 		if (ts == null || ts.length == 0)
 			return null;
 		
 		return DAOFactory.getUpdateDAO().batchUpdate(ts, fields, values);
 	}
 	
 	public static <T> Number[] batchUpdate(String dsName, T[] ts, String... fields){
 		if (ts == null || ts.length == 0)
 			return null;
 		
 		return DAOFactory.getUpdateDAO(dsName).batchUpdate(ts, fields);
 	}
 	
 	public static <T> Number[] batchUpdate(String dsName, T[] ts, String[] fields, Object[] values){
 		if (ts == null || ts.length == 0)
 			return null;
 		
 		return DAOFactory.getUpdateDAO(dsName).batchUpdate(ts, fields, values);
 	}
 	
 	public static <T> Number[] batchInsert(T[] ts){
 		if (ts == null || ts.length == 0)
 			return null;
 		
 		return batchInsert(null, ts);
 	}
 	
 	public static <T> Number[] batchInsert(T[] ts, String... fields){
 		if (ts == null || ts.length == 0)
 			return null;
 		
 		Number[] ids = DAOFactory.getInsertDAO().batchInsert(ts, fields);
 		if (ids != null && ids.length > 0){
 			for (int i = 0; i < ts.length; i++){
 				ModelHelper<T> helper = new ModelHelper<T>(ts[i]);
 				helper._setId(ids[i].longValue());
 			}
 		}
 		return ids;
 	}
 	
 	public static <T> Number[] batchInsert(T[] ts, String[] fields, Object[] values){
 		if (ts == null || ts.length == 0)
 			return null;
 		
 		Number[] ids = DAOFactory.getInsertDAO().batchInsert(ts, fields, values);
 		if (ids != null && ids.length > 0){
 			for (int i = 0; i < ts.length; i++){
 				ModelHelper<T> helper = new ModelHelper<T>(ts[i]);
 				helper._setId(ids[i].longValue());
 			}
 		}
 		return ids;
 	}
 	
 	public static <T> Number[] batchInsert(String dsName, T[] ts, String... fields){
 		if (ts == null || ts.length == 0)
 			return null;
 		
 		Number[] ids = DAOFactory.getInsertDAO(dsName).batchInsert(ts, fields);
 		if (ids != null && ids.length > 0){
 			for (int i = 0; i < ts.length; i++){
 				ModelHelper<T> helper = new ModelHelper<T>(ts[i]);
 				helper._setId(ids[i].longValue());
 			}
 		}
 		
 		return ids;
 	}
 	
 	public static <T> ModelHelper<T> ar(T t){
 		return new ModelHelper<T>(t);
 	}
 	
 	public static <T> ModelHelper<T> ar(T t, String dsName){
 		ModelHelper<T> inst = new ModelHelper<T>(t);
 		inst.setDsName(dsName);
 		return inst;
 	}
 	
 	public static <T> ModelHelper<T> ar(Class<T> cls){
 		return Models.inst(cls);
 	}
 	
 	public static <T> ModelHelper<T> ar(Class<T> cls, String dsName){
 		ModelHelper<T> inst = Models.inst(cls);
 		inst.setDsName(dsName);
 		return inst;
 	}
	
	public static void main(String[] args){
		Object cls = Db.class;
		System.out.println(Class.class.isInstance(cls));
	}
 }
