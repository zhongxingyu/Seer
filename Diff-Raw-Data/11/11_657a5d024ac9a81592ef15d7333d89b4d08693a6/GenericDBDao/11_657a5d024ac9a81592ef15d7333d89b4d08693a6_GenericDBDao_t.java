 package com.huhuo.flowerservice.core.db;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.BeanPropertyRowMapper;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
 
 import com.alibaba.fastjson.JSONArray;
 import com.huhuo.integration.db.mysql.BeanHelper;
 import com.huhuo.integration.db.mysql.Condition;
 import com.huhuo.integration.db.mysql.IDBDao;
 import com.huhuo.integration.db.mysql.IModel;
 import com.huhuo.integration.exception.DaoException;
 import com.huhuo.integration.util.StringUtils;
 
 /**
  * general DBDao support for multiple data source, supply general DB persist operation
  * @author wuyuxuan
  * @param <T>
  */
 public abstract class GenericDBDao<T extends IModel<Long>> implements IDBDao<T>{
 	
 	protected final Logger logger = LoggerFactory.getLogger(getClass());
 	
 	protected DataSource dataSource;
 	
 	/**
 	 * get table's in DB mapping this entity
 	 * @return
 	 */
 	public abstract String getTableName();
 	/**
 	 * Return the JDBC DataSource used by this DAO.
 	 * subclass should specify a JdbcTemplate instance for all DB persist operation
 	 * @param name the data source's bean name
 	 * @return
 	 */
 	public DataSource getDataSource(String name) {
 		if(dataSource == null) {
 			dataSource = AppContextFactory.getContext().getBean(name, DataSource.class);
 		}
 		return dataSource;
 	}
 	/**
 	 * @see #getDataSource(String)
 	 */
 	public DataSource getDataSource() {
 		return getDataSource("c3p0DsFlower");
 	}
 	/**
 	 * Return the JdbcTemplate for this DAO,
 	 * pre-initialized with the DataSource or set explicitly.
 	 */
 	public final JdbcTemplate getJdbcTemplate() {
 		DataSource dataSource = getDataSource();
 		if(dataSource == null) {
 			throw new DaoException("'dataSource' is required");
 		}
 		return new JdbcTemplate(dataSource);
 	}
 	
 	@Override
 	public boolean save(T t) throws DaoException {
 		if(t == null)
 			throw new DaoException("model t can't be null");
 		if(update(t) < 1) {
 			insert(t);
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public Long insert(T t) throws DaoException {
 		// validate the parameter passed in
 		if(t == null) {
 			return null;
 		}
 		SimpleJdbcInsert insert = new SimpleJdbcInsert(getJdbcTemplate());
 		insert.withTableName(getTableName()).usingGeneratedKeyColumns("id");
 		BeanHelper.GetterSetter[] getterSetterArray = BeanHelper.getGetterSetter(t.getClass());
 		Map<String, Object> args = new HashMap<String, Object>();
 		List<String> cols = new ArrayList<String>();
 		for(final BeanHelper.GetterSetter gs : getterSetterArray){
 			// use auto increase strategy for primary key
 			cols.add(gs.propertyName);
 			Object value = null;
 			try{
 				value = gs.getter.invoke(t);
 			}catch(Exception e){
 				logger.warn(null,e);
 			}
 			value = value == null ? gs.getter.getDefaultValue() : value;
 			args.put(gs.propertyName, value);
 		}
 		insert.usingColumns(cols.toArray(new String[getterSetterArray.length]));
 		Number id = insert.executeAndReturnKey(args);
 		if(id instanceof Long)
 			t.setId((Long) id);
 		return id instanceof Long ? (Long) id : null;
 	}
 
 	/**
 	 * add a record using primitive SQL
 	 * @param t
 	 * @return
 	 */
 	protected int add(T t) {
 		BeanHelper.GetterSetter[] getterSetterArray = BeanHelper.getGetterSetter(t.getClass());
 		final StringBuffer sb = new StringBuffer();
 		List<Object> values = new ArrayList<Object>();
 		sb.append("INSERT INTO ").append(getTableName()).append("(");
 		boolean first = true;
 		for(final BeanHelper.GetterSetter gs : getterSetterArray){
 			// use auto increase strategy for primary key
 			if("id".equals(gs.propertyName))
 				continue;
 			if(!first){
 				sb.append(",");
 			}else{
 				first=false;
 			}
 			sb.append(gs.propertyName);
 			Object value = null;
 			try{
 				value = gs.getter.invoke(t);
 			}catch(Exception e){
 				logger.warn(null,e);
 			}
 			values.add(value==null?gs.getter.getDefaultValue():value);
 		}
 		sb.append(") values(");
 		for(int i=0;i<values.size();i++){
 			sb.append("?");
 			if(i<values.size()-1)
 				sb.append(",");
 		}
 		sb.append(")");
 		final Object[] objects = values.toArray(new Object[values.size()]);
 		int update = getJdbcTemplate().update(sb.toString(), objects);
 		logger.debug("SQL --> {}", sb.toString());
 		logger.debug("params --> {}", JSONArray.toJSONString(objects));
 		logger.debug("row affected --> {}", update);
 		return update;
 	}
 
 	@Override
 	public Integer update(T t) throws DaoException {
 		// validate the parameter passed in
 		if (t == null) {
 			return null;
 		}
 		BeanHelper.GetterSetter[] getterSetterArray = BeanHelper.getGetterSetter(t.getClass());
 		final StringBuffer sb = new StringBuffer();
 		List<Object> values = new ArrayList<Object>();
 		sb.append("UPDATE ").append(getTableName()).append(" SET ");
 		boolean first = true;
 		for(final BeanHelper.GetterSetter gs : getterSetterArray){
 			if("id".equals(gs.propertyName)) {
 				continue;
 			}
 			if(!first){
 				sb.append(",");
 			}else{
 				first=false;
 			}
 			sb.append(gs.propertyName+"=?");
 			Object value = null;
 			try{
 				value = gs.getter.invoke(t);
 			}catch(Exception e){
 				logger.warn(null,e);
 			}
 			values.add(value==null?gs.getter.getDefaultValue():value);
 		}
 		sb.append(" WHERE id=?");
 		values.add(t.getId());
 		final Object[] objects = values.toArray(new Object[values.size()]);
 		int update = getJdbcTemplate().update(sb.toString(),objects);
 		logger.debug("SQL --> {}", sb.toString());
		logger.debug("params --> {}", StringUtils.join(objects, ","));
 		logger.debug("row affected --> {}", update);
 		return update;
 	}
 
 	@Override
 	public Integer delete(T t) throws DaoException {
 		if(t!=null)
 			return deleteById(t.getId());
 		else
 			return null;
 	}
 
 	@Override
 	public <PK> Integer deleteById(PK id) throws DaoException {
 		String sql = String.format("DELETE FROM %s WHERE id=?", getTableName());
 		Integer update = getJdbcTemplate().update(sql, id);
 		logger.debug("SQL --> {}", sql);
 		logger.debug("params --> {}", id);
 		logger.debug("row affected --> {}", update);
 		return update;
 	}
 	@Override
 	public List<T> queryForList(String sql, Class<T> clazz, Object... args)
 			throws DaoException {
 		List<T> rs = getJdbcTemplate().query(sql, args, new BeanPropertyRowMapper<T>(clazz));
 		logger.debug("SQL --> {}", sql);
		logger.debug("params --> {}", StringUtils.join(args, ","));
 		logger.debug("result set --> {}", rs);
 		return rs;
 	}
 	
 	@Override
	public T queryForObject(String sql, Class<T> clazz, Object... args)
 			throws DaoException {
 		try {
 			T singleResult = getJdbcTemplate().queryForObject(sql, BeanPropertyRowMapper.newInstance(clazz), args);
 			logger.debug("SQL --> {}", sql);
			logger.debug("params --> {}", StringUtils.join(args, ","));
 			logger.debug("result set --> {}", singleResult);
 			return singleResult;
 		} catch (EmptyResultDataAccessException e) {
 			logger.info("no available result --> ", ExceptionUtils.getStackTrace(e));
 			return null;
 		}
 	}
 	
 	@Override
 	public <PK> T findById(Class<T> clazz, PK id)
 			throws DaoException {
 		String sql = String.format("SELECT * FROM %s WHERE id=?", getTableName());
 		return queryForObject(sql, clazz, id);
 	}
 
 	@Override
 	public List<T> findModels(Class<T> clazz,
 			Integer start, Integer limit) throws DaoException {
 		String sql;
 		if(start!=null && limit!=null) {
 			sql = String.format("SELECT * FROM %s ORDER BY id DESC LIMIT %s, %s", getTableName(), start, limit);
 		} else {
 			sql = String.format("SELECT * FROM %s ORDER BY id DESC", getTableName());
 		}
 		return queryForList(sql, clazz);
 	}
 	
 	@Override
 	public List<T> findByCondition(Condition<T> condition) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 }
