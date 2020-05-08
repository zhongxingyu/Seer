 package com.crowdstore.persistence.user.store;
 
import com.crowdstore.models.user.User;
import com.crowdstore.persistence.common.DaoSupport;
 import org.mybatis.spring.support.SqlSessionDaoSupport;
 import org.springframework.stereotype.Repository;
 
 /**
  * @author damienriccio
  */
 @Repository
 public class UserDaoImpl extends SqlSessionDaoSupport implements UserDao {
 }
