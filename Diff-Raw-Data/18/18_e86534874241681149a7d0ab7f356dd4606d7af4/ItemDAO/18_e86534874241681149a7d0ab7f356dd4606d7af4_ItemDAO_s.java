 package com.tda.persistence;
 
 import java.util.List;
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Restrictions;
 
 import com.tda.model.Item;
 
 public class ItemDAO extends GenericDAOImpl<Item> implements GenericDAO<Item> {
 	@Override
     protected Class<Item> getDomainClass() {
         return Item.class;
     }
 	
 	@SuppressWarnings("unchecked")
 	public List<Item> findByName(String name) {
 		DetachedCriteria criteria = DetachedCriteria.forClass(Item.class);
		criteria.add(Restrictions.like("name", name));
 		return getHibernateTemplate().findByCriteria(criteria);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Item> findByDescription(String description) {
 		DetachedCriteria criteria = DetachedCriteria.forClass(Item.class);
		criteria.add(Restrictions.like("description", description));
 		return getHibernateTemplate().findByCriteria(criteria);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Item> findByRange(int minQ, int maxQ) {
 		DetachedCriteria criteria = DetachedCriteria.forClass(Item.class);
		criteria.add(Restrictions.ge("quantity", minQ));
		criteria.add(Restrictions.le("quantity", maxQ));
 		return getHibernateTemplate().findByCriteria(criteria);
 	}
 }
