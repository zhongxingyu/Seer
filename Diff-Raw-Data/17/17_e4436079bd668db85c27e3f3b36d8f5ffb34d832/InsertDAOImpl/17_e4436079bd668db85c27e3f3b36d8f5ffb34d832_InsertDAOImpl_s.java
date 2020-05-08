 package org.eweb4j.orm.dao.insert;
 
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.eweb4j.orm.dao.DAOException;
 import org.eweb4j.orm.jdbc.JdbcUtil;
 import org.eweb4j.orm.sql.Sql;
 import org.eweb4j.orm.sql.SqlFactory;
 
 public class InsertDAOImpl implements InsertDAO {
 	private DataSource ds;
 	private String dbType;
 
 	public InsertDAOImpl(DataSource ds, String dbType) {
 		this.ds = ds;
 		this.dbType = dbType;
 	}
 
 	public <T> Number[] batchInsert(T[] ts, String... fields) throws DAOException {
 		Number[] ids = null;
 		Connection con = null;
 		if (ts == null || ts.length == 0)
 			return ids;
 
 		try {
 			con = ds.getConnection();
			Sql[] sqls = SqlFactory.getInsertSql(ts).createByFields(fields);
 			List<Object[]> argList = new ArrayList<Object[]>(ts.length);
 			for (Sql sql : sqls){
 				argList.add(sql.args.toArray());
 			}
 			Object[][] args = new Object[argList.size()][];
 			for (int i = 0; i < argList.size(); i++){
 				args[i] = argList.get(i);
 			}
 			
 			ids = JdbcUtil.batchUpdateWithArgs(con, sqls[0].sql, args);
 		} catch (Exception e) {
 			throw new DAOException("", e);
 		}
 
 		return ids;
 	}
 
 	public <T> Number insert(T t) throws DAOException {
 		Number[] rs = batchInsert(new Object[] { t });
 		return rs == null ? 0 : rs[0];
 	}
 
 	public <T> Number insertBySql(Class<T> clazz, String sql, Object... args)
 			throws DAOException {
 		Number id = 0;
 		if (sql == null)
 			return -1;
 
 		Connection con = null;
 		try {
 			con = ds.getConnection();
 			id = JdbcUtil.updateWithArgs(con, sql, args);
 //			if (rs > 0) {
 //				id = DAOUtil.selectMaxId(clazz, ds.getConnection(), dbType);
 //			}
 		} catch (Exception e) {
 			throw new DAOException("", e);
 		}
 
 		return id;
 	}
 
 	public <T> Number[] insertByCondition(T[] ts, String condition)
 			throws DAOException {
 		Number[] ids = null;
 		Connection con = null;
 		if (ts == null || ts.length == 0)
 			return ids;
 
 		ids = new Number[ts.length];
 		try {
 			Sql[] sqls = SqlFactory.getInsertSql(ts).create(condition);
 			for (int i = 0; i < ts.length; i++) {
 				con = ds.getConnection();
 				ids[i] = JdbcUtil.updateWithArgs(con, sqls[i].sql, sqls[i].args.toArray());
 			}
 		} catch (Exception e) {
 			throw new DAOException("", e);
 		}
 
 		return ids;
 	}
 
 	public <T> Number[] insertByFields(T[] ts, String[] fields) throws DAOException {
 		Number[] ids = null;
 		Connection con = null;
 		if (ts == null || ts.length == 0)
 			return ids;
 
 		ids = new Number[ts.length];
 		try {
 			Sql[] sqls = SqlFactory.getInsertSql(ts).createByFields(fields);
 			for (int i = 0; i < ts.length; i++) {
 				con = ds.getConnection();
 				ids[i] = JdbcUtil.updateWithArgs(con, sqls[i].sql, sqls[i].args.toArray());
 			}
 		} catch (Exception e) {
 			throw new DAOException("", e);
 		}
 
 		return ids;
 	}
 
 	public <T> Number insertByField(T t, String... field) throws DAOException {
 		Number[] rs = this.insertByFields(new Object[] { t }, field);
 		return rs == null ? 0 : rs[0];
 	}
 
 	public DataSource getDs() {
 		return ds;
 	}
 
 	public void setDs(DataSource ds) {
 		this.ds = ds;
 	}
 
 	public String getDbType() {
 		return dbType;
 	}
 
 	public void setDbType(String dbType) {
 		this.dbType = dbType;
 	}
 
 }
