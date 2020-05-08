 package com.rcosnita.experiments.rdbmsreduce.examples;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.jdbc.Work;
 
 import com.rcosnita.experiments.rdbmsreduce.reductor.Reductor;
 import com.rcosnita.experiments.rdbmsreduce.reductor.SupportedEngines;
 import com.rcosnita.experiments.rdbmsreduce.sessions.JPABuilder;
 
 /**
  * This is the example for how to obtain top ten domains in alphabetical order
  * from an account with thousands of domains.
  * 
  * @author Radu Viorel Cosnita
  * @version 1.0
  * @since 12.07.2012
  */
 public class TopDomains {
 	private final static Logger logger = Logger.getLogger(TopDomains.class);
 	
 	private Reductor reductor;
 	private String sql;
 	private int topDomains;
 	private List<Integer> provIds;
 	
 	public TopDomains(Reductor reductor, String sql, int topDomains, List<Integer> provIds) {
 		this.reductor = reductor;
 		this.sql = sql;
 		this.topDomains = topDomains;
 		this.provIds = provIds;
 	}
 	
 	/**
 	 * Method used to display the top domains.
 	 * 
 	 * @param results
 	 */
 	public void displayTopDomains(List<Map<String, Object>> results) {
 		for(Map<String, Object> domain : results) {
 			logger.info(String.format("Domain ----> %s", domain.get("name")));
 		}
 	}
 	
 	/**
 	 * Method used to obtain the top domains based on the current object attributes.
 	 * 
 	 * @return
 	 */
 	public List<Map<String, Object>> getTopDomains() {
 		EntityManager em = null;
 		final List<Map<String, Object>> results = new ArrayList<Map<String,Object>>();
 		
 		try {
			em = JPABuilder.getEntityManager(JPABuilder.PROVISIONING);
 			
 			Session session = em.unwrap(Session.class);
 			
 			session.doWork(new Work() {
 				@Override
 				public void execute(Connection conn) throws SQLException {
 					try {
 						results.addAll(
 								reductor.reduce(conn, sql, provIds, new HashMap<String, Object>(), 
 									"name", true, topDomains));
 					}
 					catch(Exception ex) {
 						throw new SQLException(ex);
 					}
 				}
 			});
 		}
 		finally {
 			if(em != null) {
 				em.close();
 			}
 		}
 		
 		return results;
 	}
 	
 	public static void main(String[] args) {
 		int accountId = 100;
 		int maxDomains = 10;
 		
 		long startTime = Calendar.getInstance().getTimeInMillis();
 		
 		List<Integer> provIds = JPABuilder.getProvisioningIds(accountId);
 		
 		String sql = "SELECT * FROM domains WHERE prov_id IN (%(prov_ids)) ORDER BY name ASC";
 		
 		Reductor reductor = new Reductor(1, SupportedEngines.MySQL);
 		
 		TopDomains topDomains = new TopDomains(reductor, sql, maxDomains, provIds);
 		List<Map<String, Object>> domains = topDomains.getTopDomains();
 		
 		long endTime = Calendar.getInstance().getTimeInMillis();
 		
 		logger.info(String.format("Top domains use case took %s milliseconds.", (endTime - startTime)));
 		
 		topDomains.displayTopDomains(domains);
 	}
 }
