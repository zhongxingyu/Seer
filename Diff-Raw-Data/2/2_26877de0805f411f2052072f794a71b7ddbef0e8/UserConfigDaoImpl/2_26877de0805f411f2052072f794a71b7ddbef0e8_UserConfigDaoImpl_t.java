 package com.robonobo.midas.dao;
 
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.springframework.stereotype.Repository;
 
 import com.robonobo.midas.model.MidasUserConfig;
 
 @Repository("userConfigDao")
 public class UserConfigDaoImpl extends MidasDao implements UserConfigDao {
 	@Override
 	public MidasUserConfig getUserConfig(long userId) {
 		return (MidasUserConfig) getSession().get(MidasUserConfig.class, userId);
 	}
 
 	@Override
 	public MidasUserConfig getUserConfig(String key, String value) {
 		String hql = "from MidasUserConfig uc where uc.items[:pKey] = :pVal";
 		Session s = getSession();
 		Query q = s.createQuery(hql);
 		q.setString("pKey", key);
 		q.setString("pVal", value);
 		List list = q.list();
 		if(list.size() == 0)
 			return null;
 		if(list.size() > 1)
 			log.error("Error: Duplicate result for user config key = "+key+", val = "+value);
 		return (MidasUserConfig) list.get(0);
 	}
 	
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<MidasUserConfig> getUserConfigsWithKey(String key) {
 		String hql = "from MidasUserConfig uc where uc.items[:pKey] != null";
 		Session s = getSession();
 		Query q = s.createQuery(hql);
 		q.setString("pKey", key);
 		return q.list();
 	}
 	@Override
 	public void saveUserConfig(MidasUserConfig config) {
 		getSession().saveOrUpdate(config);
 	}
 	
 	@Override
 	public void deleteUserConfig(long userId) {
 		MidasUserConfig muc = getUserConfig(userId);
 		getSession().delete(muc);
 	}
 }
