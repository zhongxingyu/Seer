 package com.tda.persistence.dao;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
import java.util.List;
 
 import com.tda.model.applicationuser.OnlineUser;
 
 public class OnlineUserDAO extends GenericDAOImpl<OnlineUser> {
 
 	@Override
 	protected Class<OnlineUser> getDomainClass() {
 		return OnlineUser.class;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void deleteTimeoutUsers(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String now = sdf.format(new Date());
		List<OnlineUser> list = getHibernateTemplate().find("from OnlineUser ou WHERE timeout <= '" + now + "'");
		for (OnlineUser onlineUser : list) {
			this.delete(onlineUser);
		}
 	}
 }
