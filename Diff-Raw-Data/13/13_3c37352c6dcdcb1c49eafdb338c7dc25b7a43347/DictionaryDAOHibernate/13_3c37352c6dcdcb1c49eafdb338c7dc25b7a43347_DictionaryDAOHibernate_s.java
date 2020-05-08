 package cn.sunn.forensiclion.dao.impl;
 
 import cn.sunn.forensiclion.dao.IDictionaryDAO;
 import cn.sunn.forensiclion.domain.Dictionary;
 
 public class DictionaryDAOHibernate extends
 		GenericHibernateDao<Dictionary, Long> implements IDictionaryDAO {
 
 	
 	public Dictionary listDictionary(String name) {
 		// TODO Auto-generated method stub

		return (Dictionary)getHibernateTemplate().find(
				"From Dictionary d where d.name = ?", name).get(0);
 	}
 
 }
