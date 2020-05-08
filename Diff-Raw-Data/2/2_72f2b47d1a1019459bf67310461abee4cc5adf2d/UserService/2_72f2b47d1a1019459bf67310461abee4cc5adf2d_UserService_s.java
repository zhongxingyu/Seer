 /*
  * Created on 2011-08-20
  */
 package com.ligelong.hibernate.service;
 
 import java.sql.Timestamp;
 
 import javax.annotation.Resource;
 
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.ligelong.hibernate.core.BaseDao;
 import com.ligelong.hibernate.core.BaseService;
 import com.ligelong.hibernate.entity.UserEntity;
 import com.ligelong.util.Status;
 
 /**
  * <code>UserService</code>
  *
  * @author David Gong
  */
 @Service
 @Transactional
 public class UserService extends BaseService<UserEntity> {
     /* (non-Javadoc)
      * @see com.ligelong.hibernate.core.BaseService#setBaseDao(com.ligelong.hibernate.core.BaseDao)
      */
     @Override
     @Resource(name="userDao")
     public void setBaseDao(BaseDao<UserEntity> baseDao) {
         this.baseDao = baseDao;
     }
     
     public void addUser(String username, String password, String email, String phone) {
     	UserEntity user = new UserEntity();
     	user.setCreatetime(new Timestamp(System.currentTimeMillis()));
     	user.setEmail(email);
     	user.setPassword(password);
     	user.setPhone(phone);
     	user.setStatus(Status.ON.getValue());
     	user.setUsername(username);
     	this.save(user);
     }
 
 	public UserEntity getUserByPwd(String username, String pwd) {
 		String hql = "from UserEntity where username=? and password=?";
		if(this.getBaseDao().findCountByHql(hql, new Object[]{username, pwd})<1) {
 			return null;
 		}
 		UserEntity user = this.getBaseDao().findListByHql(hql, new Object[]{username, pwd}).get(0);
 		return user;
 	}
 }
