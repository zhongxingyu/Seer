 package au.org.paperminer.service;
 
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import au.org.paperminer.dao.ImplDAO;
 import au.org.paperminer.dao.PmQueriesDAO;
 import au.org.paperminer.model.PmQueries;
 import au.org.paperminer.model.PmUsers;
 
 @Service
 @Transactional
 @Scope("session")
@Component
 public class QueriesService {
 	
 	static final Logger log = Logger.getLogger(QueriesService.class);
 	
 	@Autowired
 	PmQueriesDAO dao;
 	
 	public void save()
 	{
 		PmQueries query = new PmQueries();
 		PmUsers user = new PmUsers();
 		user.setId(1);
 		query.setPmUsers(user);
 		query.setDateCreated(new Date());
 		query.setDateLastRun(new Date());
 		query.setDescr("test");
 		query.setQuery("test");
 		query.setQueryType("s");
 		query.setTotalLastRun(11111);
 		dao.save(query);
 		//dao.flush();
 		log.info("run successfully");
 	}
 }
