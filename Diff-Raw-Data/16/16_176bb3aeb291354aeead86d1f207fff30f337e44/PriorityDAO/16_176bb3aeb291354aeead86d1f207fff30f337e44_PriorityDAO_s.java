 package com.ocmc.ws.dao;
 
 import java.util.List;
 
 import com.ocmc.ws.beans.PriorityBean;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
import com.ocmc.utils.HibernateUtils;
 
 public class PriorityDAO implements IDao<PriorityBean> {
	//SessionFactory sessionFactory = HibernateUtils.getSessionFactory();
	SessionFactory sessionFactory;
 	@Autowired
 	public void setSessionFactory(SessionFactory sessionFactory) {
 	     this.sessionFactory = sessionFactory;
 	 }
 	
 	@Override
 	public Long save(PriorityBean p) {
 		return (Long)sessionFactory.getCurrentSession().save(p);
 	}
 
 	@Override
 	public void update(PriorityBean p) {
 		sessionFactory.getCurrentSession().update(p);
 		
 	}
 
 	@Override
 	public void delete(PriorityBean p) {
 		sessionFactory.getCurrentSession().delete(p);
 		
 	}
 
 	@Override
 	public PriorityBean get(Long id) {
 		return (PriorityBean)sessionFactory.getCurrentSession().get(PriorityBean.class, id);
 	}
 
 	@Override
 	public List<PriorityBean> getAll() {
		return (List<PriorityBean>)sessionFactory.getCurrentSession().createQuery("FROM PriorityBean");
 	}
 
 }
