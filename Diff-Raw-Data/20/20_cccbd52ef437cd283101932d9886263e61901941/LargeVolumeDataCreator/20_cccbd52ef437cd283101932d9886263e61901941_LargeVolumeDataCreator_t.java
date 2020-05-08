 package com.rcosnita.experiments.rdbmsreduce;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityTransaction;
 
 import org.apache.log4j.Logger;
 
 import com.rcosnita.experiments.rdbmsreduce.entities.Domain;
 import com.rcosnita.experiments.rdbmsreduce.entities.ProvisioningItem;
 import com.rcosnita.experiments.rdbmsreduce.sessions.JPABuilder;
 
 /**
  * Class used to generate a large amount of data required for our 
  * distributed proof of concept.
  * 
  * @author Radu Viorel Cosnita
  * @version 1.0
  * @since 10.07.2012
  */
 public class LargeVolumeDataCreator {
 	private final static Logger logger = Logger.getLogger(LargeVolumeDataCreator.class);
 	
 	private final int MAX_TRANSACTION_ITEMS = 5000;
 	
 	/**
 	 * Interface used to provide the items generator interface api. This is useful
 	 * to reduce the boiler plate code of JPA transactions.
 	 */
 	private interface ItemsGenerator {
 		/**
 		 * Method used to insert items into a specific entity manager.
 		 * 
 		 * @param maxItems The total number of items desired.
 		 * @param accountIds The list of account ids we want to use.
 		 * @param em The entity manager we want to use.
 		 */
 		public void insertItems(int maxItems, List<Integer> accountIds, EntityManager em);
 	}
 
 	/**
 	 * Method used to generate items.
 	 * 
 	 * @param maxItems The number of maximum items we want to generate.
 	 * @param accountIds The list of account ids we want to generate items for.
 	 * @param pu The persistence unit we want to use.
 	 * @param generator The generator that provides the actual logic of generating items.
 	 */
 	public void generateItems(int maxItems, List<Integer> accountIds, String pu, 
 			ItemsGenerator generator) {
 		logger.info(String.format("Generating total %s provisioning items for %s accounts.", 
 				maxItems, accountIds.size()));
 		
 		EntityManager em = null;
 		
 		try {
 			em = JPABuilder.getEntityManager(pu);
 			
 			generator.insertItems(maxItems, accountIds, em);
 		}
 		finally {
 			if(em != null) {
 				em.close();
 			}			
 		}		
 	}
 	
 	/**
 	 * Method used to generate a large number of provisioning ids.
 	 */
 	public void generateProvisioningIds(int maxProvIds, List<Integer> accountIds) {
 		generateItems(maxProvIds, accountIds, JPABuilder.PROVISIONING, 
 				new ItemsGenerator() {
 			@Override
 			public void insertItems(int maxItems, List<Integer> accountIds, EntityManager em) {				
 				insertProvisioningItems(maxItems, accountIds, em);
 			}
 		});
 	}
 	
 	/**
 	 * Method used to generate and insert provisioning items into database..
 	 *
 	 * @param maxProvIds The maximum number of provisioning items we want to insert.
 	 * @param accountIds The account identifiers under which we insert the provisioning items.
 	 * @param em This is the entity manager we want to use for persistence.
 	 */
 	private void insertProvisioningItems(int maxProvIds, List<Integer> accountIds, EntityManager em) {				
 		int idsPerAccount = maxProvIds / accountIds.size(); 
 		
 		logger.info(String.format("Inserting %s provisioning ids per account.", idsPerAccount));		
 		
 		int inserted = 0;
 
 		EntityTransaction tran = em.getTransaction();
 		tran.begin();
 		
 		for(Integer accountId : accountIds) {
			System.gc(); // give a change to garbage collector to clean resources.
			
			try {
				Thread.sleep(50);
			}
			catch(InterruptedException iex) {}
			
 			for(int i = 0; i < idsPerAccount; i++) {
 				ProvisioningItem item = new ProvisioningItem();
 				
 				int provId = Integer.valueOf("" + i + accountId); 
 				
 				item.setAccountId(accountId);
 				item.setProvType(1);
 				item.setProvId(provId);
 				
 				em.persist(item);
 				
 				inserted++;
 				
 				if(inserted == MAX_TRANSACTION_ITEMS) {
 					try {
 						logger.info("Commiting chunk of prov items");
 						
 						tran.commit();
 	
 						tran = em.getTransaction();
 						tran.begin();
 					}
 					catch(Exception ex) {
 						if(tran != null && tran.isActive()) {
 							tran.rollback();
 						}
 					}
 					finally {
						inserted = 0;
 					}
 				}
 			}
 		}
 		
 		if(inserted > 0) {
 			try {
 				tran.commit();
 			}
 			finally {
 				if(tran != null && tran.isActive()) {
 					tran.rollback();
 				}
 			}			
 		}
 	}
 
 	/**
 	 * Method used to generate a large number of domains.
 	 */
 	public void generateDomains(int maxProvIds, List<Integer> accountIds) {
 		generateItems(maxProvIds, accountIds, JPABuilder.STORE, 
 				new ItemsGenerator() {
 			@Override
 			public void insertItems(int maxItems, List<Integer> accountIds, EntityManager em) {
 				insertDomainItems(maxItems, accountIds, em);
 			}
 		});
 	}	
 	
 	/**
 	 * Method used to generate a random number of domains for the specified account ids.
 	 * 
 	 * @param maxProvIds
 	 * @param accountIds
 	 * @param em
 	 */
 	private void insertDomainItems(int maxProvIds, List<Integer> accountIds, 
 			EntityManager em) {
 		int idsPerAccount = maxProvIds / accountIds.size();
 		
 		String[] baseDoms = new String[]{
 			"google.%s.com", "yahoo.%s.com", "msn.%s.com",
 			"msn.%s.de", "gmx.%s.de", "amazon-%s.de",
 			"amazon.%s.co.uk", "%s.1and1.co.uk", 
 			"%s.1and1.com"
 		};
 		
 		logger.info(String.format("Inserting %s domains per account.", (idsPerAccount * accountIds.size())));
 		
 		EntityTransaction tran = em.getTransaction();
 		tran.begin();
 		
 		int inserted = 0;
 		
 		for(Integer accountId : accountIds) {
			System.gc(); // give a change to garbage collector to clean resources.
			
			try {
				Thread.sleep(50);
			}
			catch(InterruptedException iex) {}			
			
 			for(int i = 0; i < idsPerAccount; i++) {
 				int provId = Integer.valueOf("" + i + accountId);
 				
 				Domain dom = new Domain();
 				dom.setProvId(provId);
 				
 				String domName = baseDoms[(int)(Math.random() * maxProvIds % baseDoms.length)];
 				dom.setName(String.format(domName, provId));
 				
 				em.persist(dom);
 				
 				inserted ++;
 				
 				if(inserted == MAX_TRANSACTION_ITEMS) {
 					try {
 						logger.info("Commiting chunk of domains.");
 						tran.commit();
 						
 						tran = em.getTransaction();
 						tran.begin();
 					}
 					catch(Exception ex) {
 						if(tran != null && tran.isActive()) {
 							tran.rollback();
 						}						
 					}
 					finally {
 						inserted = 0;
 					}
 				}
 			}
 		}
 		
 		if(inserted > 0) {
 			try {
 				tran.commit();
 			}
 			finally {
 				if(tran != null && tran.isActive()) {
 					tran.rollback();
 				}
 			}			
 		}
 	}
 	
 	public static void main(String[] args) {
 		if(args.length != 3) {
 			throw new RuntimeException("Invalid usage ... Command must be executed as follows: java com.rcosnita.experiments.rdbmsreduce.LargeVolumeDataCreator <max_provids> <max_accounts> <start_account>");
 		}
 		
 		LargeVolumeDataCreator generator = new LargeVolumeDataCreator();
 		
 		int maxProvIds = Integer.parseInt(args[0]);
 		int maxAccounts = Integer.parseInt(args[1]);
 		int startAccount = Integer.parseInt(args[2]);
 		
 		List<Integer> accountIds = new ArrayList<Integer>();
 		
 		for(int i = startAccount; i < startAccount + maxAccounts; i++) {
 			accountIds.add(i);
 		}
 		
 		generator.generateProvisioningIds(maxProvIds, accountIds);
 		generator.generateDomains(maxProvIds, accountIds);
 	}
 }
