 package org.sakaiproject.sdata.services.mra;
 
 import java.util.List;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class MyRecentChangesDbBean implements ServletContextListener {
 
 	public static final MyRecentChangesObserver obs = new MyRecentChangesObserver();
 	private static final Log log = LogFactory
 			.getLog(MyRecentChangesDbBean.class);
 
 	public void init() {
 
		log.info("sData Widget Data Service Initializing ...");
		
 		try {
 			boolean exist = false;
 			boolean exist2 = false;
 			List lsts = null;
 			lsts = org.sakaiproject.db.cover.SqlService.dbRead("SHOW TABLES");
 
 			for (int i = 0; i < lsts.size(); i++) {
 
 				if (lsts.get(i).toString().equals("sdata_lastlogin")) {
 
 					exist = true;
 					//log.error("tables exist");
 				} else if (lsts.get(i).toString().equals("sdata_indexqueue")) {
 					exist2 = true;
 					//log.error("tables exist");
				} 		
 
 			}
 
			if (exist == false) {
 				org.sakaiproject.db.cover.SqlService
 						.dbWrite("create table sdata_lastlogin (userid varchar(255) not null, usereid varchar(255) not null, userdate timestamp not null, primary key(userid));");
 				// log.info("MySakai Login Table Added");
 				// log.info("MySakai Index Queue Table Added");
 
 			}
			if (exist2 == false){
 				org.sakaiproject.db.cover.SqlService
 				.dbWrite("create table sdata_indexqueue (id int not null AUTO_INCREMENT, version timestamp not null, name varchar(255) not null, context varchar(255) not null, tool varchar(255) not null, primary key  (id));");
 		
 				
 			}
 
 		} catch (Exception ex) {
 
 			// log.error("alrdy created");
 
 		}
 
 		// log.info("MySakai Observer added");
 		org.sakaiproject.event.cover.EventTrackingService.addObserver(obs);
 
 	}
 
 	public void contextDestroyed(ServletContextEvent arg0) {
 		// TODO Auto-generated method stub
 		org.sakaiproject.event.cover.EventTrackingService.deleteObserver(obs);
 
 	}
 
 	public void contextInitialized(ServletContextEvent arg0) {
 		// TODO Auto-generated method stub
 		init();
 
 	}
 
 }
