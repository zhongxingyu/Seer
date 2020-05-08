 package org.silix.the9ull.microbit.model;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import java.util.zip.CRC32;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 
 /*
  * Singleton. This class is a wrapper of Bitcoin,
  * moreover it implements persistence transactions.
  */
 public class Transactions {
 
 	static Transactions instance = null;
 	
 	private static final String MINIMUMFEE = "0.005"; // Maximum minimum fee
 	
 	Bitcoin bc;
 	
 	static private BigDecimal fee = null;
 	
 	private Transactions() throws IOException {
 		
 		Bitcoin.readRpcCredentials();
 		bc = new Bitcoin(false);
 		
 	}
 	
 	static public Transactions getInstance() throws IOException {
 		if(instance==null)
 			instance = new Transactions();
 		return instance;
 	}
 	
 	private boolean sendtouser(UserP from, UserP to, BigDecimal amount, Session session) {
 		boolean ok=false;
 		BigDecimal fee;
 		String fee_s = PersistenceUtility.dictGet("internalfee", session);
 		
 		if(fee_s==null)
 			fee = new BigDecimal(0);
 		else
 			fee = new BigDecimal(fee_s);
 		
 		if(amount.add(fee).compareTo(from.getFund()) <= 0){
 			from.setFund(from.getFund().subtract(amount).subtract(fee));
 			to.setFund(to.getFund().add(amount));
 			
 			HistoryP history = new HistoryP();
 			history.setFrom(from);
 			history.setTo(to);
 			history.setHowmuch(amount);
 			history.setWhen(new Date());
 			if(fee.signum()!=0) //fee!=0
 				history.setFee(fee);
 			
 			session.update(from);
 			session.update(to);
 			session.save(history);
 			ok=true;
 		}
 		return ok;
 	}
 
 	boolean sendtouser(UserP from, UserP to, BigDecimal amount) {
 		boolean ok=false;
 		
 		SessionFactory sessionFactory = SingletonSessionFactory.getSessionFactory();
 		Session session = sessionFactory.openSession(); 
 		Transaction htx = session.beginTransaction();
 		
 		ok = sendtouser(from,to,amount,session);
 		
 		htx.commit();
 		session.close();
 		
 		return ok;
 	}
 	boolean sendtouser(int fromid, int toid, BigDecimal amount) {
 		boolean ok=false;
 		
 		UserP from, to;
 		
 		SessionFactory sessionFactory = SingletonSessionFactory.getSessionFactory();
 		Session session = sessionFactory.openSession(); 
 		Transaction htx = session.beginTransaction();
 		
 		Query q = session.createQuery("from UserP user where user.id="+fromid);
 		from = (UserP)q.list().get(0);
 		q = session.createQuery("from UserP user where user.id="+toid);
 		to = (UserP)q.list().get(0);
 		
 		ok = sendtouser(from,to,amount,session);
 		
 		htx.commit();
 		session.close();
 		
 		return ok;
 	}
 	
 
 	public Tx sendtoaddress(UserP from, String address, BigDecimal amount) {
 		String txid = null;
 		Tx tx;
 		
 		if(fee==null) {
 			SessionFactory sessionFactory = SingletonSessionFactory.getSessionFactory();
 			Session session = sessionFactory.openSession(); 
 			Transaction htx = session.beginTransaction();
 			String sfee = (String) PersistenceUtility.dictGet("minimumfee", session);
 			htx.commit();
 			session.close();
 			if(sfee==null)
 				sfee = MINIMUMFEE;
 			fee = new BigDecimal(sfee);
 			System.out.println("Fee: "+fee);
 		}
 			
 		if(bc.validateaddress(address)
 				&& bc.getbalanceall().compareTo(amount.add(fee)) >= 0
 				&& from.getFund().compareTo(amount.add(fee)) >= 0
 				) {
 			
 			//
 			txid = bc.sendtoaddress(address, amount);
 			tx = new Tx();
 			tx.setTxid(txid);
 			
 			Collection<Map<String,Object>> txs = bc.listtransactions(); //recent transactions
 			
 
 			SessionFactory sessionFactory = SingletonSessionFactory.getSessionFactory();
 			Session session = sessionFactory.openSession(); 
 			Transaction htx = session.beginTransaction();
 		
 			
 			for(Map<String,Object> t : txs){
 				if(txid.equals((String)t.get("txid")) && 
 						address.equals((String)t.get("address")) && 
 						"send".equals((String)t.get("category"))) {
 					
 					tx.setTxid((String)t.get("txid"));
 					tx.setAddress(address);
 					tx.setAmount(new BigDecimal((Double)t.get("amount")).setScale(8,BigDecimal.ROUND_HALF_DOWN));
 					// Get fee and make it positive
 					tx.setFee(new BigDecimal((Double)t.get("fee")).setScale(8,BigDecimal.ROUND_HALF_DOWN));
 					tx.setCategory("send");
 					//time: ms
 					Date time = new Date(1000 * (Long)t.get("time"));
 					tx.setWhen(time);
 					
 
 					
 					//update fee
 					//System.out.println(""+fee+" "+tx.getFee());
 					if(!fee.equals(tx.getFee()) && tx.getFee().signum()!=0){
 						fee = tx.getFee();
 						PersistenceUtility.dictSet("minimumfee", ""+fee, session);
 					}
 
 					// Insert History
 					HistoryP history = new HistoryP();
 					history.setFrom(from);
 					history.setTo(from);
 					
 					history.setWhen(time);
 					history.setHowmuch(tx.getAmount());
 					history.setFee(fee); //equals to tx.getFee()
 					
 					CRC32 hash = new CRC32();
 					hash.update(((String)tx.getTxid()).getBytes());
 					history.setTxidcrc(hash.getValue());
 					
 					//update fund
 					from.setFund(from.getFund().add(fee).add(tx.getAmount()));
 					
 					session.save(history);
 					session.update(from);
 					
 					break;
 				}
 			}
 			
 			htx.commit();
 			session.close();
 			
 			return tx;
 		}
 		
 		System.out.println("Valid address: "+bc.validateaddress(address));
 		System.out.println("Server Balance: "+bc.getbalanceall());
 		System.out.println("Amount+fee: "+amount.add(fee));
 		
 		return null;
 	}
 	
 
 	public Map<String,BigDecimal> updatefunds(boolean all) {
 		
 		Map<String,BigDecimal> funds = new HashMap<String,BigDecimal>(); // address, new fund
 		String lastblock = null;
 		
 		SessionFactory sessionFactory = SingletonSessionFactory.getSessionFactory();
 		Session session = sessionFactory.openSession();
 		Transaction htx = session.beginTransaction();
 		Query q;
 		
 		if(!all)
 			lastblock = PersistenceUtility.dictGet("lastblock",session);
 		//System.out.println(lastblock);
 		
 		//
 		//session.update(dict);
 		
 		
 		Map<String,Object> listsince = bc.listsinceblock(lastblock); // transactions, lastblock
 		Collection<Map<String,Object>> transactions = (Collection<Map<String,Object>>)listsince.get("transactions");
 		lastblock = (String)listsince.get("lastblock");
 		
 		for(Map<String,Object> tx : transactions) {
 			if("receive".equals((String)tx.get("category"))) {
 				HistoryP history = new HistoryP();
 				BigDecimal howmuch = new BigDecimal((Double)tx.get("amount")).setScale(8,BigDecimal.ROUND_HALF_DOWN); 
 				history.setHowmuch(howmuch);
 				CRC32 hash = new CRC32();
 				hash.update(((String)tx.get("txid")).getBytes());
 				history.setTxidcrc(hash.getValue());
 				
 				//Object x = tx.get("timereceived");
 				//System.out.println(x);
 				Date timereceived = new Date(1000 * (Long)tx.get("timereceived"));
 				history.setWhen(timereceived);
 				
 				String address = (String)tx.get("address");
 				q = session.createQuery("from UserP user where user.deposit_address='"+address+"'");
 				if(q.list().size()==0) {
 					System.out.println("deposit_address sconosciuto: "+address);
 					// Save new funds only in funds variable
 					funds.put(address, new BigDecimal((Double)tx.get("amount")).setScale(8,BigDecimal.ROUND_HALF_DOWN));
 					continue;
 				}
 				UserP user = (UserP)q.list().get(0);
 				System.out.println("User id: "+user.getId());
 				
 				history.setFrom(user);
 				history.setTo(user);
 				
 				// Is history object already persistance?
 				q = session.createQuery("from HistoryP h where h.txidcrc="+history.getTxidcrc());
 				if(q.list().size()>0){
 					System.out.println("Oh, crc trovato!");
 					boolean exists = false;
 					for(Object o : q.list()) {
 						HistoryP h = (HistoryP)o;
 						System.out.println("for interno :o "+h);
 						System.out.println(""+history);
 						
 						if( h.getFrom().equals(history.getFrom()) &&
 								h.getTo().equals(history.getTo()) &&
 								h.getHowmuch().equals(history.getHowmuch()) &&
 								h.getWhen().compareTo(history.getWhen())==0
 								) {
 							exists = true;
 							continue;
 						}
 						
 					}
 					if(exists)
 						continue;
 				}
 				// update howmuch and save history and user. Only if object is not into db
 				user.setFund(user.getFund().add(howmuch));
 					
 				session.save(history);
 				session.update(user);	
 				
 			}
 			
 		}
 		
 		PersistenceUtility.dictSet("lastblock", lastblock, session);
 		PersistenceUtility.dictSet("updatefundstime", "" + new Date().getTime(), session);
 		PersistenceUtility.dictSet("updatefundshumantime", "" + new Date(), session);
 		
 		htx.commit();
 		session.close();
 		
 		return funds;
 	}
 	
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		 
 		Transactions bt = Transactions.getInstance();
 		
 		System.out.println("Qui ci sono");
 		//System.out.println( bc.validateaddress("n2NAjt6oZBPZY5bhTGEBHkENTDtrpYc7fS") );
 		//System.out.println( bc.validateaddress("14UWwaX1BdCHEfXQF9KqivMmK3EbWjHDoV") );
 		//System.out.println(bc.listaccounts());
 		//System.out.println(bc.getb`ckcount());
 		//System.out.println(bc.listtransactions());
 		//System.out.println(bc.getnewaddress(null));
 		
 
 		//System.out.println(bc.getbalanceall());
 		
 		Random r = new Random();
 		//Tx tx = bc.sendtoaddress("mrZQpbfo4RqEoBeJw4u5E9u6CEJsycjEM5", new BigDecimal(r.nextFloat()/1000.0));
 		
 		
 		//System.out.println(""+tx.getAmount().multiply(new BigDecimal(-1))+" + "+
 		//		tx.getFee().multiply(new BigDecimal(-1))+" = "+
 		//		tx.getAmount().add(tx.getFee()).multiply(new BigDecimal(-1)));
 		//System.out.println(bc.getbalanceall());
 		
 		bt.updatefunds(false);
 		
 		/*
 		SessionFactory sessionFactory = SingletonSessionFactory.getSessionFactory();
 		Session session = sessionFactory.openSession(); 
 		Transaction htx = session.beginTransaction();
 		
 		Query q = session.createQuery("from UserP user where user.id=87");
 		UserP user = (UserP)q.list().get(0);
 		
 		htx.commit();
 		session.close();
 		
 		//bc.updatefunds(false);
 
 		System.out.println(bt.sendtoaddress(user,"mrZQpbfo4RqEoBeJw4u5E9u6CEJsycjEM5", new BigDecimal("0.2")));
 		*/
 		for(int i=0 ; i<2 ; i++){
 			System.out.println(bt.sendtouser(103, 105, new BigDecimal("1")));
 			System.out.println(bt.sendtouser(105, 88, new BigDecimal("0.4")));
 			System.out.println(bt.sendtouser(105, 88, new BigDecimal("0.6")));
 			System.out.println(bt.sendtouser(88, 104, new BigDecimal("1")));
 			System.out.println(bt.sendtouser(104, 103, new BigDecimal("1")));
 		}
 		/*while(bt.sendtouser(87, 105, new BigDecimal("0.01"))){
 			
 		}*/
 		
 		//System.out.println(Bitcoin.jsonToJava("{\"minconf\":\"2\"}"));
 		
 		  
 	}
 
 }
