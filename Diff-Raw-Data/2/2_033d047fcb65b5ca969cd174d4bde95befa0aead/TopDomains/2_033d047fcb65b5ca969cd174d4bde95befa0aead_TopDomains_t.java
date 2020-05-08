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
 	 * Method used to obtain the top domains based on the current object attributes.
 	 * 
 	 * @return
 	 */
 	public List<Map<String, Object>> getTopDomains() {
 		EntityManager em = null;
 		final List<Map<String, Object>> results = new ArrayList<Map<String,Object>>();
 		
 		try {
 			em = JPABuilder.getEntityManager(JPABuilder.STORE);
 			
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
 		if(args.length != 2) {
 			throw new RuntimeException("Invalid usage... Ex: java com.rcosnita.experiments.rdbmsreduce.examples.TopDomains <account_id> <number_of_domains>");
 		}
 		
 		int accountId = Integer.parseInt(args[0]);
 		int maxDomains = Integer.parseInt(args[1]);
 		
 		long startTime = Calendar.getInstance().getTimeInMillis();
 		
 		List<Integer> provIds = JPABuilder.getProvisioningIds(accountId);
 		
 		String sql = "SELECT * FROM domains WHERE prov_id IN (%(prov_ids)) ORDER BY name ASC";
 		
 		Reductor reductor = new Reductor(1, SupportedEngines.MySQL);
 		
 		TopDomains topDomains = new TopDomains(reductor, sql, maxDomains, provIds);
 		
 		long startTime2 = Calendar.getInstance().getTimeInMillis(); 
 		List<Map<String, Object>> domains = topDomains.getTopDomains();
 		long endTime2 = Calendar.getInstance().getTimeInMillis();
 		
 		logger.info(String.format("Get top domains operation took %s milliseconds.", (endTime2 - startTime2)));		
 		
 		long endTime = Calendar.getInstance().getTimeInMillis();
 		
 		logger.info(String.format("Top domains use case took %s milliseconds.", (endTime - startTime)));
 		
		DomainsUtils.displayDomains(domains);
 		
 		System.exit(0);
 	}
 }
