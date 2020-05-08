 package com.dbpractice.realestate.dao;
 
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import com.dbpractice.realestate.domain.Application;
 import com.dbpractice.realestate.domain.District;
 
 @Repository
 public class ApplicationDAOImpl implements ApplicationDAO {
 
 	private final String CLOSED_STATUS = "CLOSED";
 	
 	@Autowired
 	private SessionFactory sessionFactory;
 	
 	@Override
	public Integer createApplication(Application application) {
		return (Integer)sessionFactory.getCurrentSession().save(application);
 	}
 
 	@Override
 	public void clearClosed() {
 		sessionFactory.getCurrentSession()
 			.createSQLQuery("delete from applications where status = ?")
 			.addEntity(Application.class).setString(0, CLOSED_STATUS).executeUpdate();
 	}
 
 	@Override
 	public int getRequestsCount(District district) {
 		String queryString = "select count(ap.*) from applications ap, appartments at, " +
 				"streets st, districts ds where ap.appartment_id = at.appartment_id " +
 				"and at.street_id = st.street_id and st.district_id = ?";
 		Integer result = (Integer)sessionFactory.getCurrentSession()
 			.createSQLQuery(queryString)
 			.setInteger(0, district.getDistrictId())
 			.uniqueResult();
 		return result.intValue();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Application> getRequest(String status) {
 		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Application.class);
 		return (List<Application>)criteria.add(Restrictions.eq("status", status)).list();
 	}
 
 	@Override
 	public void updateApplication(Application application) {
 		sessionFactory.getCurrentSession().update(application);
 	}
 
 	@Override
 	public void deleteApplication(Application application) {
 		sessionFactory.getCurrentSession().delete(application);
 	}
 
 	@Override
 	public Application getApplicationById(int applicationId) {
 		return (Application)sessionFactory.getCurrentSession()
 				.createSQLQuery("select * from applications where ?")
 				.setInteger(0, applicationId).uniqueResult();
 	}
 	
 }
