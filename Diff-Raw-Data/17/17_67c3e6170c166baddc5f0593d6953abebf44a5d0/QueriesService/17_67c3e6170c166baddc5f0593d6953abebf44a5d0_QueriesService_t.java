 package au.org.paperminer.service;
 
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import au.org.paperminer.dao.ImplDAO;
 import au.org.paperminer.dao.PmQueriesDAO;
 import au.org.paperminer.dao.PmQueriesDAOImpl;
 import au.org.paperminer.model.PmQueries;
 import au.org.paperminer.model.PmUsers;
 
 //@Service("queriesService")
 //@Transactional
 @Scope("session")
 @Component("queriesService")
 public class QueriesService {
 	
 	static final Logger log = Logger.getLogger(QueriesService.class);
 	
 	//@Autowired
 	//PmQueriesDAO dao;
 	
 	@Autowired(required=true)
 	private SessionFactory sessionFactory;
 	
 	@Transactional
 	public void save()
 	{
 		PmQueries query = new PmQueries();
 		PmUsers user = new PmUsers();
 		user.setId(1);
 		query.setPmUsers(user);
 		query.setDateCreated(new Date());
 		query.setDateLastRun(new Date());
		query.setDescr("test12");
		query.setQuery("test12");
 		query.setQueryType("s");
		query.setTotalLastRun(111111);
 		PmQueriesDAOImpl pmQueriesDAO = new PmQueriesDAOImpl();
 		pmQueriesDAO.setSession(sessionFactory.openSession());
 		pmQueriesDAO.save(query);
 		
 		//dao.save(query);
 		//Session session = sessionFactory.openSession();
 		//Transaction tx = session.beginTransaction();
 		//session.merge(user);
 		//session.saveOrUpdate(query);
 		log.info("test");
 		//tx.commit();
 		
 		//session.flush();
 		//session.close();
 		System.out.println("save successfull");
 		
 		//dao.flush();
 		log.info("run successfully");
 		
 		/*System.out.println(user.getFirstName());
 		System.out.println(user.getLastName());
 		SessionFactory sf = hibernateUtil.getSessionFactory();
 		Session session = sf.openSession();
 		Transaction tx = session.beginTransaction();
 		//session.merge(user);
 		session.saveOrUpdate(user);
 		log.info("test");
 		tx.commit();
 		
 		session.flush();
 		session.close();
 		System.out.println("save successfull");*/
 	}
 }
