 package com.hts.dao;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Hibernate;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 
 import com.hts.entity.Channel;
 import com.hts.entity.Room;
 import com.hts.entity.SubscriptionPackage;
 import com.hts.exceptions.AppException;
 
 public class SubscriptionPackageDAOHibernateImpl extends DAO implements ISubscriptionPackageDAO {
 	final Logger log = Logger.getLogger(this.getClass());
 
 	@Override
 	public SubscriptionPackage create(SubscriptionPackage subscriptionPackage ) throws AppException {
 		try {
 			begin();
 			getSession().save(subscriptionPackage );
 			commit();
 			return subscriptionPackage ;
 		} catch (HibernateException e) {
 			rollback();
 			log.error(e);
 			throw new AppException(e.getCause().getMessage());
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<SubscriptionPackage> getAll() throws AppException {
 		
 		try {
 			begin();
 			Query q = getSession().createQuery(
 					"from SubscriptionPackage h");
 			List<SubscriptionPackage> subscriptionPackages= q.list();
 			for (SubscriptionPackage s : subscriptionPackages) {
				Hibernate.initialize(s.getChannels());
 			}
 			commit();
 			return subscriptionPackages;
 		} catch (HibernateException e) {
 			log.error(e);
 			throw new AppException(e.getCause().getMessage());
 		}
 	}
 	
 
 	@Override
 	public SubscriptionPackage getById(Integer id) throws AppException {
 		try {
 			begin();
 			Query q = getSession().createQuery(
 					"from SubscriptionPackage h where h.id= :id");
 			q.setLong("id", id);
 			SubscriptionPackage subscriptionPackage = (SubscriptionPackage) q.uniqueResult();
 //			Hibernate.initialize(subscriptionPackage.getChannels());
 			List<Channel> channels = subscriptionPackage.getChannels();
 			//lazy loading
 			channels.size();
 			
 			commit();
 			
 	
 			
 			return subscriptionPackage ;
 		} catch (HibernateException e) {
 			log.error(e);
 			throw new AppException(e.getCause().getMessage());
 		}
 	}
 
 	@Override
 	//update
 	public void update(SubscriptionPackage subscriptionPackage ) throws AppException {
 		try {
 			begin();
 			getSession().update(subscriptionPackage );
 			commit();
 		} catch (HibernateException e) {
 			rollback();
 			log.error(e);
 			throw new AppException(e.getCause().getMessage());
 		}
 
 	}
 
 	@Override
 	public void delete(SubscriptionPackage subscriptionPackage ) throws AppException {
 		try {
 			begin();
 			getSession().delete(subscriptionPackage );
 			commit();
 		} catch (HibernateException e) {
 			rollback();
 			log.error(e);
 			throw new AppException(e.getCause().getMessage());
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<SubscriptionPackage> getByName(String name) throws AppException {
 		try {
 			begin();
 			Query q = getSession().createQuery(
 					"from SubscriptionPackage h where h.subscriptionPackageName= :name");
 			q.setString("name", name);
 			List<SubscriptionPackage> subscriptionPackages = q.list();
 			commit();
 			return subscriptionPackages ;
 		} catch (HibernateException e) {
 			log.error(e);
 			throw new AppException(e.getCause().getMessage());
 		}
 	}
 }
