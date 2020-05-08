 package eionet.webq.dao;
 
import java.util.Properties;

 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.jdbc.core.BeanPropertyRowMapper;
 
 /**
  * Common logic for DAO classes.
  *
  * @param <T> DTO
  */
 public abstract class AbstractDao<T> {
     /**
      * Sql statements properties.
      */
     @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
     @Autowired
     @Qualifier("sql_statements")
     Properties sqlProperties;
 
     /**
      * Row mapper for web form upload.
      *
     * @return {@link org.springframework.jdbc.core.BeanPropertyRowMapper} for
     *         {@link org.springframework.jdbc.core.BeanPropertyRowMapper}
      */
     BeanPropertyRowMapper<T> rowMapper() {
         return BeanPropertyRowMapper.newInstance(getDtoClass());
     }
 
     /**
      * Return dto class.
      *
      * @return dto class
      */
     abstract Class<T> getDtoClass();
 }
