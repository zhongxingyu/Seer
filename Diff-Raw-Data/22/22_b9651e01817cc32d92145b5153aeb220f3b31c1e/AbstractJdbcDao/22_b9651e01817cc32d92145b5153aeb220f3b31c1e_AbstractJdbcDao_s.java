 package net.kamhon.ieagle.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import net.kamhon.ieagle.datagrid.DatagridModel;
 import net.kamhon.ieagle.jdbc.SingleRowMapper;
 import net.kamhon.ieagle.util.CollectionUtil;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.support.JdbcDaoSupport;
 
 /**
  * <p>
  * This is a wrapper class for {@link JdbcDaoSupport} in Spring JDBC. There are a variable {@link JdbcDao} is extends
  * {@link JdbcDaoSupport}. So, indirectly this class will manipulate database with {@link JdbcTemplate}.
  * </p>
  * <p>
  * Basically, the function is mimic the function in {@link JdbcTemplate}, like
  * {@link #queryForInt(String, int[], Object...)}, this mimic from
  * {@link JdbcTemplate#queryForInt(String, Object[], int[])}, but the parameters re-arrange for conveniency
  * </p>
  * 
  * @author kamhon
  * 
  */
 public class AbstractJdbcDao {
     public static final String FILTER_PARAMS = BasicDao.FILTER_PARAMS;
 
     private JdbcDao jdbcDao;
     private String fromSchema;
     private String toSchema;
 
     public String replace(String sql) {
         if (StringUtils.isNotBlank(fromSchema) && StringUtils.isNotBlank(toSchema)) {
             sql = StringUtils.replace(sql, fromSchema, toSchema);
         }
         return sql;
     }
 
     public String performDateOperation(String fieldName, String operator, Date... dates) {
         return jdbcDao.performDateOperation(fieldName, operator, dates);
     }
 
     @Required
     @Autowired
     public void setJdbcDao(JdbcDao jdbcDao) {
         this.jdbcDao = jdbcDao;
     }
 
     /**
      * Enhance the {@link #query(String, RowMapper, Object...)} which this return 1 object only.
      * 
      * @param sql
      * @param rowMapper
      * @param params
      * @return
      * @see #query(String, RowMapper, Object...)
      */
     public <T> T queryForSingleRow(String sql, SingleRowMapper<T> rowMapper, Object... params) {
         List<T> result = new ArrayList<T>();
 
         result = (List<T>) jdbcDao.query(sql, rowMapper, params);
 
         if (CollectionUtil.isNotEmpty(result))
             return result.get(0);
         else
             return null;
     }
 
     /**
      * Enhance the {@link #query(String, RowMapper)} which this return 1 object only.
      * 
      * @param sql
      * @param rowMapper
      * @return
      * @see #query(String, RowMapper)
      */
     public <T> T queryForSingleRow(String sql, SingleRowMapper<T> rowMapper) {
         List<T> result = new ArrayList<T>();
 
         result = (List<T>) jdbcDao.query(sql, rowMapper);
 
         if (CollectionUtil.isNotEmpty(result))
             return result.get(0);
         else
             return null;
     }
 
     public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
         return jdbcDao.query(replace(sql), rowMapper, params);
     }
 
     /**
      * 
      * @param <T>
      * @param datagridModel
      * @param sql
      * @param rowMapper
      * @param params
      */
     protected <T> void queryForDatagrid(DatagridModel<T> datagridModel, String sql, RowMapper<T> rowMapper,
             Object... params) {
         jdbcDao.queryForDatagrid(datagridModel, sql, rowMapper, params);
     }
 
     public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
         return jdbcDao.query(replace(sql), rowMapper);
     }
 
     public int queryForInt(String sql) {
         return jdbcDao.queryForInt(replace(sql));
     }
 
     public int queryForInt(String sql, Object... params) {
         return jdbcDao.queryForInt(replace(sql), params);
     }
 
     public int queryForInt(String sql, int[] argTypes, Object... params) {
         return jdbcDao.queryForInt(replace(sql), argTypes, params);
     }
 
     public List<?> queryForList(String sql) {
         return jdbcDao.queryForList(replace(sql));
     }
 
     public List<?> queryForList(String sql, Object... params) {
         return jdbcDao.queryForList(replace(sql), params);
     }
 
     public List<?> queryForList(String sql, int[] argTypes, Object... params) {
         return jdbcDao.queryForList(replace(sql), argTypes, params);
     }
 
     public long queryForLong(String sql) {
         return jdbcDao.queryForLong(replace(sql));
     }
 
     public long queryForLong(String sql, Object... params) {
         return jdbcDao.queryForLong(replace(sql), params);
     }
 
     public long queryForLong(String sql, int[] argTypes, Object... params) {
         return jdbcDao.queryForLong(replace(sql), argTypes, params);
     }
 
     public String queryForString(String sql) {
         try {
            return jdbcDao.queryForString(replace(sql));
         } catch (EmptyResultDataAccessException ex) {
             return null;
         }
     }
 
     public String queryForString(String sql, Object... params) {
         try {
            return jdbcDao.queryForString(replace(sql), params);
         } catch (EmptyResultDataAccessException ex) {
             return null;
         }
     }
 
     public String queryForString(String sql, int[] argTypes, Object... params) {
         try {
             return jdbcDao.queryForString(replace(sql), argTypes, params);
         } catch (EmptyResultDataAccessException ex) {
             return null;
         }
     }
 
     public Object queryForObject(String sql, Class<?> requiredType) {
         try {
             return jdbcDao.queryForObject(replace(sql), requiredType);
         } catch (EmptyResultDataAccessException ex) {
             return null;
         }
     }
 
     public Object queryForObject(String sql, Class<?> requiredType, Object... params) {
         try {
             return jdbcDao.queryForObject(replace(sql), requiredType, params);
         } catch (EmptyResultDataAccessException ex) {
             return null;
         }
     }
 
     /**
      * Issue a single SQL update operation (such as an insert, update or delete statement) via a prepared statement,
      * binding the given arguments.
      * 
      * @param sql
      * @return
      */
     public int update(String sql) {
         return jdbcDao.update(replace(sql));
     }
 
     /**
      * Issue a single SQL update operation (such as an insert, update or delete statement) via a prepared statement,
      * binding the given arguments.
      * 
      * @param sql
      * @param params
      * @return
      */
     public int update(String sql, Object... params) {
         return jdbcDao.update(replace(sql), params);
     }
 
     /**
      * Issue a single SQL update operation (such as an insert, update or delete statement) via a prepared statement,
      * binding the given arguments.
      * 
      * @param sql
      * @param argTypes
      * @param params
      * @return
      */
     public int update(String sql, int[] argTypes, Object... params) {
         return jdbcDao.update(replace(sql), argTypes, params);
     }
 
     public String getFromSchema() {
         return fromSchema;
     }
 
     public void setFromSchema(String fromSchema) {
         this.fromSchema = fromSchema;
     }
 
     public String getToSchema() {
         return toSchema;
     }
 
     public void setToSchema(String toSchema) {
         this.toSchema = toSchema;
     }
 
     /*
      * public JdbcTemplate getJdbcTemplate() { return jdbcDao.getJdbcTemplate();
      * }
      */
 }
