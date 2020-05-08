 package cn.sunn.forensiclion.dao.impl;
 
import java.util.List;

 import cn.sunn.forensiclion.dao.IDictionaryDAO;
 import cn.sunn.forensiclion.domain.Dictionary;
 
 public class DictionaryDAOHibernate extends
 		GenericHibernateDao<Dictionary, Long> implements IDictionaryDAO {
 
 	
 	public Dictionary listDictionary(String name) {
 		// TODO Auto-generated method stub
		@SuppressWarnings("unchecked")
		List<Dictionary> dict = getHibernateTemplate().find(
				"From Dictionary d where d.name = ?", name);
		if (dict.size() == 0){
			return null;
		}else
		return dict.get(0);
 	}
 
 }
