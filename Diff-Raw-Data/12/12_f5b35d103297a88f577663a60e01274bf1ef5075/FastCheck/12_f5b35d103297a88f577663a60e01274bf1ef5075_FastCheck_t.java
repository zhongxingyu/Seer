 package paybar.rest;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import javax.annotation.Resource;
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.JMSException;
 import javax.jms.MessageProducer;
 import javax.jms.ObjectMessage;
 import javax.jms.Queue;
 import javax.jms.Session;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.transaction.HeuristicMixedException;
 import javax.transaction.HeuristicRollbackException;
 import javax.transaction.NotSupportedException;
 import javax.transaction.RollbackException;
 import javax.transaction.Status;
 import javax.transaction.SystemException;
 import javax.transaction.Transaction;
 import javax.transaction.TransactionManager;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.StatusType;
 
 import org.infinispan.AdvancedCache;
 import org.infinispan.Cache;
 import org.infinispan.manager.CacheContainer;
 import org.infinispan.manager.CacheManager;
 import org.jboss.resteasy.client.ClientRequest;
 import org.jboss.resteasy.client.ClientResponse;
 import org.jboss.resteasy.util.GenericType;
 
 import paybar.junit.CouponAfterRegisterTest;
 
 import at.ac.uibk.paybar.messages.Configuration;
 import at.ac.uibk.paybar.messages.MetadataMessage;
 import at.ac.uibk.paybar.messages.TransactionMessage;
 import at.ac.uibk.paybar.messages.TransactionRequest;
 import at.ac.uibk.paybar.model.FastAccount;
 import at.ac.uibk.paybar.model.FastCoupon;
 import at.ac.uibk.paybar.model.TransferAccount;
 
 /**
  * FastCheck is responsible for authorizing a transaction. This includes account
  * and TAN-code checkup. If both are valid the FastCheck answers the client with
  * success and queues a transaction + updates the distributed caches account
  * status immediately. The TAN-code should be invalidated as soon as possible
  * too.
  * 
  * @author wolfi
  * 
  */
 @Path("/transactions")
 @RequestScoped
 public class FastCheck {
 
 	private static final int BATCH_SIZE = 10000;
 
 	public static final String FASTCHECK_JNDI_NAME = "java:jboss/infinispan/container/fastcheck";
 
 	@Inject
 	private Logger log;
 
 	private static InitialContext INITIAL_CONTEXT = null;
 
 	// @Resource(lookup = "java:jboss/infinispan/fastcheck")
 	// CacheContainer cacheContainer;
 
 	@POST
 	@Path("/account/new")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public String newAccount(TransferAccount transferAccount) {
 		String result = "FAILURE";
 
 		boolean success = false;
 		InitialContext ic = null;
 		Connection jmsConnection = null;
 		boolean openTransaction = false;
 		WebApplicationException exceptionToThrow = null;
 		TransactionManager accountTransactionManager = null;
 
 		// method needs @Form parameter with @POST
 		if (transferAccount != null) {
 
 			long accountId = transferAccount.getId();
 			long amount = transferAccount.getCredit();
 
 			// fetch the account for the request
 			try {
 				ic = getInitialContextOfApp();
 				CacheContainer cacheContainer = (CacheContainer) ic
 						.lookup(FASTCHECK_JNDI_NAME);
 
 				Cache<Long, FastAccount> accountCache = cacheContainer
 						.getCache("accounts");
 				Cache<String, Long> couponCache = cacheContainer
 						.getCache("coupons");
 				AdvancedCache<Long, FastAccount> advancedCache = accountCache
 						.getAdvancedCache();
 				accountTransactionManager = advancedCache
 						.getTransactionManager();
 				accountTransactionManager.begin();
 				openTransaction = true;
 
 				FastAccount fastAccount = advancedCache.get(accountId);
 
 				if (fastAccount == null) {
 					addAccount(couponCache, accountCache, transferAccount);
 					accountTransactionManager.commit();
 					success = true;
 				} else {
 					// existing account found, so this is no valid new account
 					if (ic != null)
 						ic.close();
 					exceptionToThrow = new WebApplicationException(404);
 					accountTransactionManager.rollback();
 				}
 			} catch (NamingException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (NotSupportedException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (SystemException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (SecurityException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (IllegalStateException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (RollbackException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (HeuristicMixedException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (HeuristicRollbackException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} finally {
 				// Be sure to close all resources!
 				try {
 					if (ic != null) {
 						ic.close();
 					}
 					if (jmsConnection != null) {
 						jmsConnection.close();
 					}
 				} catch (NamingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (JMSException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				// check if the transaction is still open
 				// if it is so, rollback
 				try {
 					if (accountTransactionManager != null && openTransaction) {
 						accountTransactionManager.rollback();
 					}
 				} catch (SystemException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 		}
 
 		if (success) {
 			result = "SUCCESS";
 		} else if (exceptionToThrow != null) {
 			log.log(Level.SEVERE, exceptionToThrow.getMessage());
 			throw exceptionToThrow;
 		} else {
 			log.log(Level.SEVERE, "Invalid transaction request");
 			throw new WebApplicationException(500);
 		}
 
 		return result;
 	}
 
 	@POST
 	@Path("/coupons/new")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public String newCoupons(TransferAccount transferAccount) {
 		String result = "FAILURE";
 
 		boolean success = false;
 		InitialContext ic = null;
 		Connection jmsConnection = null;
 		boolean openTransaction = false;
 		WebApplicationException exceptionToThrow = null;
 		TransactionManager accountTransactionManager = null;
 
 		// method needs @Form parameter with @POST
 		if (transferAccount != null) {
 
 			long accountId = transferAccount.getId();
 			long amount = transferAccount.getCredit();
 
 			// fetch the account for the request
 			try {
 				ic = getInitialContextOfApp();
 				CacheContainer cacheContainer = (CacheContainer) ic
 						.lookup(FASTCHECK_JNDI_NAME);
 
 				Cache<Long, FastAccount> accountCache = cacheContainer
 						.getCache("accounts");
 				Cache<String, Long> couponCache = cacheContainer
 						.getCache("coupons");
 				AdvancedCache<Long, FastAccount> advancedCache = accountCache
 						.getAdvancedCache();
 				accountTransactionManager = advancedCache
 						.getTransactionManager();
 				accountTransactionManager.begin();
 				openTransaction = true;
 
 				FastAccount fastAccount = advancedCache.get(accountId);
 
 				if (fastAccount != null) {
 					addNewCoupons(couponCache, transferAccount.getCoupons(),
 							fastAccount);
 					accountTransactionManager.commit();
 					success = true;
 				} else {
 					// existing account found, so this is no valid new account
 					if (ic != null)
 						ic.close();
 					exceptionToThrow = new WebApplicationException(404);
 					accountTransactionManager.rollback();
 				}
 			} catch (NamingException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (NotSupportedException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (SystemException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (SecurityException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (IllegalStateException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (RollbackException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (HeuristicMixedException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (HeuristicRollbackException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} finally {
 				// Be sure to close all resources!
 				try {
 					if (ic != null) {
 						ic.close();
 					}
 					if (jmsConnection != null) {
 						jmsConnection.close();
 					}
 				} catch (NamingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (JMSException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				// check if the transaction is still open
 				// if it is so, rollback
 				try {
 					if (accountTransactionManager != null && openTransaction) {
 						accountTransactionManager.rollback();
 					}
 				} catch (SystemException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 		}
 
 		if (success) {
 			result = "SUCCESS";
 		} else if (exceptionToThrow != null) {
 			log.log(Level.SEVERE, exceptionToThrow.getMessage());
 			throw exceptionToThrow;
 		} else {
 			log.log(Level.SEVERE, "Invalid transaction request");
 			throw new WebApplicationException(500);
 		}
 
 		return result;
 	}
 
 	/**
 	 * This method provides the central transaction authorization method.
 	 * 
 	 * @param posId
 	 *            Point of Sale
 	 * @param tanCode
 	 *            coupon code
 	 * @param amount
 	 *            amount to be deducted, positive long
 	 * @return
 	 * @throws NamingException
 	 */
 	@POST
 	@Path("/tan/{tanCode}")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public String transaction(@PathParam("tanCode") String tanCode,
 			TransactionRequest transactionRequest) {
 		String result = "FAILURE";
 		boolean success = false;
 		InitialContext ic = null;
 		Connection jmsConnection = null;
 		boolean openTransaction = false;
 		WebApplicationException exceptionToThrow = null;
 		TransactionManager accountTransactionManager = null;
 
 		// method needs @Form parameter with @POST
 		if (transactionRequest != null) {
 			String posId = transactionRequest.getPosOrBankId();
 			long amount = transactionRequest.getAmount();
 
 			// fetch the account for the request
 			try {
 				ic = getInitialContextOfApp();
 				CacheContainer cacheContainer = (CacheContainer) ic
 						.lookup(FASTCHECK_JNDI_NAME);
 
 				Cache<String, Long> couponCache = cacheContainer
 						.getCache("coupons");
 
 				Long accountId = couponCache.get(tanCode);
 
 				if (accountId != null) {
 
 					// fetch the account, open a transaction
 					Cache<Long, FastAccount> accountCache = cacheContainer
 							.getCache("accounts");
 					AdvancedCache<Long, FastAccount> advancedCache = accountCache
 							.getAdvancedCache();
 					accountTransactionManager = advancedCache
 							.getTransactionManager();
 					accountTransactionManager.begin();
 					openTransaction = true;
 
 					// try to find the account
 					boolean gotLock = true;
 
 					/*
 					 * advancedCache.lock(accountId); int tries = 10;
 					 * 
 					 * while (!gotLock && tries > 0) { tries--; gotLock =
 					 * advancedCache.lock(accountId); }
 					 */
 
 					// if we still do not have a lock, exit by exception
 					if (!gotLock) {
 						// maybe there is no such key
 						// but this should not be
 						// TODO: check for existence of key
 
 						// bail out with a overloaded exception... the cache is
 						// not responding currently
 						exceptionToThrow = new WebApplicationException(503);
 					}
 
 					FastAccount fastAccount = advancedCache.get(accountId);
 
 					if (fastAccount == null) {
 						// bail out with an exception of 404 because we do not
 						// have such an account
 						exceptionToThrow = new WebApplicationException(404);
 					}
 
 					// see if account has enough credit
 					// TODO: should check for overflows and other validation of
 					// limits
 					long oldCredit = fastAccount.getCredit();
 					long newCredit = oldCredit - amount;
 					success = newCredit > 0;
 
 					if (success) {
 						// TODO: fetch username from cache
 						TransactionMessage transactionMessage = new TransactionMessage(
 								TransactionMessage.TYPE_TRANSACTION, posId,
 								amount, System.currentTimeMillis(), accountId,
 								tanCode);
 
 						/*
 						 * Now transmit everything to the JMS.
 						 */
 
 						// JMS operations
 
 						// Step 2. Look up the XA Connection Factory
 						ConnectionFactory cf = (ConnectionFactory) ic
 								.lookup("java:/JmsXA");
 
 						// Step 3. Look up the Queue
 						Queue queue = (Queue) ic.lookup("queue/test");
 
 						// Step 4. Create a connection, a session and a
 						// message producer for the queue
 						jmsConnection = cf.createConnection();
 						Session session = jmsConnection.createSession(false,
 								Session.AUTO_ACKNOWLEDGE);
 						MessageProducer messageProducer = session
 								.createProducer(queue);
 
 						// Step 5. Create a Text Message
 						ObjectMessage message = session
 								.createObjectMessage(transactionMessage);
 						messageProducer.send(message);
 
 						// Message sent. Create update in cache.
 						fastAccount.setCredit(newCredit);
 						fastAccount.removeCoupon(tanCode);
 
 						// immediately release the lock
 						accountTransactionManager.commit();
 						openTransaction = false;
 
 					} else {
 						// no success!
 						// payment required ;-)
 						exceptionToThrow = new WebApplicationException(402);
 						accountTransactionManager.rollback();
 						openTransaction = false;
 					}
 				} else {
 					// account not found, so this is no valid coupon!
 					if (ic != null)
 						ic.close();
 					exceptionToThrow = new WebApplicationException(404);
 				}
 			} catch (NamingException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (JMSException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (NotSupportedException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (SystemException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (SecurityException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (IllegalStateException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (RollbackException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (HeuristicMixedException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (HeuristicRollbackException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} finally {
 				// Step 12. Be sure to close all resources!
 				try {
 					if (ic != null) {
 						ic.close();
 					}
 					if (jmsConnection != null) {
 						jmsConnection.close();
 					}
 				} catch (NamingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (JMSException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				// check if the transaction is still open
 				// if it is so, rollback
 				try {
 					if (accountTransactionManager != null && openTransaction) {
 						accountTransactionManager.rollback();
 					}
 				} catch (SystemException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 		}
 
 		if (success) {
 			result = "SUCCESS";
 		} else if (exceptionToThrow != null) {
 			log.log(Level.SEVERE, exceptionToThrow.getMessage());
 			throw exceptionToThrow;
 		} else {
 			log.log(Level.SEVERE, "Invalid transaction request");
 			throw new WebApplicationException(500);
 		}
 
 		return result;
 	}
 
 	private InitialContext getInitialContextOfApp() throws NamingException {
 		if (INITIAL_CONTEXT == null)
 			INITIAL_CONTEXT = new InitialContext();
 		return INITIAL_CONTEXT;
 	}
 
 	/**
 	 * This method allows putting money on to a user's account.
 	 * 
 	 * @param posId
 	 *            This time it is the source financial institute
 	 * @param tanCode
 	 *            Used for storing credit card details
 	 * @param amount
 	 *            amount to be charged on to the users account
 	 * @param id
 	 *            the username of the account to be charged
 	 * @return
 	 * @throws NamingException
 	 */
 	@POST
	@Path("/charge/{id}/{creditCardNumber}")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
	public String charge(@PathParam("id") Long id,
 			@PathParam("creditCardNumber") String creditCardNumber,
 			TransactionRequest transactionRequest) {
 		String result = "FAILURE";
 		boolean success = false;
 		InitialContext ic = null;
 		Connection jmsConnection = null;
 		boolean openTransaction = false;
 		WebApplicationException exceptionToThrow = null;
 		TransactionManager accountTransactionManager = null;
 
 		// method needs @Form parameter with @POST
 		if (transactionRequest != null) {
 			String posId = transactionRequest.getPosOrBankId();
 			long amount = transactionRequest.getAmount();
 
 			// fetch the account for the request
 			try {
 				ic = getInitialContextOfApp();
 				CacheContainer cacheContainer = (CacheContainer) ic
 						.lookup(FASTCHECK_JNDI_NAME);
 
 				Long accountId = id;
 
 				if (accountId != null) {
 
 					// fetch the account, open a transaction
 					Cache<Long, FastAccount> accountCache = cacheContainer
 							.getCache("accounts");
 					AdvancedCache<Long, FastAccount> advancedCache = accountCache
 							.getAdvancedCache();
 					accountTransactionManager = advancedCache
 							.getTransactionManager();
 					accountTransactionManager.begin();
 					openTransaction = true;
 
 					// try to find the account
 					boolean gotLock = true;
 
 					/*
 					 * advancedCache.lock(accountId); int tries = 10;
 					 * 
 					 * while (!gotLock && tries > 0) { tries--; gotLock =
 					 * advancedCache.lock(accountId); }
 					 */
 
 					// if we still do not have a lock, exit by exception
 					if (!gotLock) {
 						// maybe there is no such key
 						// but this should not be
 						// TODO: check for existence of key
 
 						// bail out with a overloaded exception... the cache is
 						// not responding currently
 						exceptionToThrow = new WebApplicationException(503);
 					}
 
 					FastAccount fastAccount = advancedCache.get(accountId);
 
 					if (fastAccount == null) {
 						// bail out with an exception of 404 because we do not
 						// have such an account
 						exceptionToThrow = new WebApplicationException(404);
 					}
 
 					// see if credit card is valid
 					// but not in this prototype
 					// TODO: should check for overflows and other validation of
 					// limits
 					long oldCredit = fastAccount.getCredit();
 					long newCredit = oldCredit + amount;
 					success = true;
 
 					if (success) {
 						TransactionMessage transactionMessage = new TransactionMessage(
 								TransactionMessage.TYPE_CHARGE,
 								transactionRequest.getPosOrBankId(), amount,
 								System.currentTimeMillis(), id,
 								creditCardNumber);
 
 						/*
 						 * Now transmit everything to the JMS.
 						 */
 
 						// JMS operations
 
 						// Step 2. Look up the XA Connection Factory
 						ConnectionFactory cf = (ConnectionFactory) ic
 								.lookup("java:/JmsXA");
 
 						// Step 3. Look up the Queue
 						Queue queue = (Queue) ic.lookup("queue/test");
 
 						// Step 4. Create a connection, a session and a
 						// message producer for the queue
 						jmsConnection = cf.createConnection();
 						Session session = jmsConnection.createSession(false,
 								Session.AUTO_ACKNOWLEDGE);
 						MessageProducer messageProducer = session
 								.createProducer(queue);
 
 						// Step 5. Create a Text Message
 						ObjectMessage message = session
 								.createObjectMessage(transactionMessage);
 						messageProducer.send(message);
 
 						// Message sent. Create update in cache.
 						fastAccount.setCredit(newCredit);
 
 						// immediately release the lock
 						accountTransactionManager.commit();
 						openTransaction = false;
 
 					} else {
 						// no success!
 						// payment required ;-)
 						exceptionToThrow = new WebApplicationException(402);
 						accountTransactionManager.rollback();
 						openTransaction = false;
 					}
 				} else {
 					// account not found, so this is no valid coupon!
 					if (ic != null)
 						ic.close();
 					exceptionToThrow = new WebApplicationException(404);
 				}
 			} catch (NamingException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (JMSException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (NotSupportedException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (SystemException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (SecurityException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (IllegalStateException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (RollbackException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (HeuristicMixedException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} catch (HeuristicRollbackException e) {
 				exceptionToThrow = new WebApplicationException(e);
 			} finally {
 				// Step 12. Be sure to close all resources!
 				try {
 					if (ic != null) {
 						ic.close();
 					}
 					if (jmsConnection != null) {
 						jmsConnection.close();
 					}
 				} catch (NamingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (JMSException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 
 				// check if the transaction is still open
 				// if it is so, rollback
 				try {
 					if (accountTransactionManager != null && openTransaction) {
 						accountTransactionManager.rollback();
 					}
 				} catch (SystemException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 
 		}
 
 		if (success) {
 			result = "SUCCESS";
 		} else if (exceptionToThrow != null) {
 			log.log(Level.SEVERE, exceptionToThrow.getMessage());
 			throw exceptionToThrow;
 		} else {
 			log.log(Level.SEVERE, "Invalid transaction request");
 			throw new WebApplicationException(500);
 		}
 
 		return result;
 	}
 
 	@GET
 	public String initWebService() {
 		boolean result = initCaches();
 		return result ? "SUCCESS" : "FAILURE";
 	}
 
 	/**
 	 * Fetch the data for the cache... Do a full initialization. Maybe a
 	 * "kernel" could do this in a more centralized way...
 	 */
 	private boolean initCaches() {
 		boolean result = false;
 
 		Cache<String, Long> couponCache = null;
 		Cache<Long, FastAccount> accountCache = null;
 		boolean openTransaction = false;
 		InitialContext ic = null;
 
 		try {
 			ic = getInitialContextOfApp();
 			CacheContainer cacheContainer = (CacheContainer) ic
 					.lookup(FASTCHECK_JNDI_NAME);
 			couponCache = cacheContainer.getCache("coupons");
 			accountCache = cacheContainer.getCache("accounts");
 
 			// empty the cache
 			couponCache.clear();
 
 			AdvancedCache<Long, FastAccount> advancedCache = accountCache
 					.getAdvancedCache();
 			TransactionManager accountTransactionManager = advancedCache
 					.getTransactionManager();
 			accountTransactionManager.begin();
 			openTransaction = true;
 
 			// empty the cache
 			accountCache.clear();
 			// now we will fill the whole cache
 
 			/*
 			 * Fetch data from the clearing house
 			 */
 
 			// fetch all data from the clearing house...
 			long numberAccountsUnread = 0;
 			ClientRequest clientRequest = new ClientRequest(
 					"http://localhost:8080/clearinghouse/rest/fastcheckInterface/metaData");
 			clientRequest.accept("application/json");
 			/*
 			 * clientRequest .body("application/json", new
 			 * TransactionRequest(amount,
 			 * Configuration.BankPosName.toString()));
 			 */
 
 			ClientResponse<MetadataMessage> fastcheckResponse = clientRequest
 					.get(MetadataMessage.class);
 
 			// check error code of response
 			if (Response.Status.OK
 					.equals(fastcheckResponse.getResponseStatus())) {
 
 				MetadataMessage mm = fastcheckResponse
 						.getEntity(MetadataMessage.class);
 
 				numberAccountsUnread = mm.getNoOfAccounts();
 
 				long currentAccountIndex = 0;
 
 				long numberAccountsRequested = 0;
 
 				if (numberAccountsUnread > BATCH_SIZE) {
 					numberAccountsRequested = BATCH_SIZE;
 				} else {
 					numberAccountsRequested = numberAccountsUnread;
 				}
 
 				while (numberAccountsUnread > 0) {
 					clientRequest = new ClientRequest(
 							"http://localhost:8080/clearinghouse/rest/fastcheckInterface/accountBatch/"
 									+ currentAccountIndex + "/"
 									+ numberAccountsRequested);
 					clientRequest.accept("application/json");
 
 					// ArrayList<TransferAccount> list = new
 					// ArrayList<TransferAccount>();
 					GenericType<List<TransferAccount>> entity = new GenericType<List<TransferAccount>>() {
 					};
 					ClientResponse<List<TransferAccount>> response = clientRequest
 							.get(entity);
 					List<TransferAccount> batch = response.getEntity();
 
 					for (TransferAccount transferAccount : batch) {
 						addAccount(couponCache, accountCache, transferAccount);
 					}
 
 					if (batch.size() < numberAccountsRequested) {
 						// there has been some change in the available accounts
 						// number... stop fetching accounts now.
 						numberAccountsUnread = 0;
 					} else {
 						numberAccountsUnread -= numberAccountsRequested;
 					}
 				}
 
 				/*
 				 * Commit the newly initialized caches.
 				 */
 
 				accountTransactionManager.commit();
 				// couponCache.getAdvancedCache().getTransactionManager().commit();
 				openTransaction = false;
 
 				result = true;
 			} else {
 				accountTransactionManager.commit();
 				openTransaction = false;
 			}
 
 		} catch (NamingException e) {
 			// TODO: logging
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			// close all open transactions
 			if (accountCache != null && openTransaction) {
 				try {
 					// rollback only if the transaction has not been closed
 					// already
 					accountCache.getAdvancedCache().getTransactionManager()
 							.rollback();
 
 					// catch all inner exceptions and log them...
 				} catch (IllegalStateException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (SystemException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 
 		return result;
 	}
 
 	private void addAccount(Cache<String, Long> couponCache,
 			Cache<Long, FastAccount> accountCache,
 			TransferAccount transferAccount) {
 		ArrayList<FastCoupon> fastCoupons = transferAccount.getCoupons();
 		FastAccount fastAccount = new FastAccount();
 		fastAccount.setId(transferAccount.getId());
 		fastAccount.setCredit(transferAccount.getCredit());
 
 		addNewCoupons(couponCache, fastCoupons, fastAccount);
 		accountCache.put(new Long(fastAccount.getId()), fastAccount);
 	}
 
 	private void addNewCoupons(Cache<String, Long> couponCache,
 			ArrayList<FastCoupon> fastCoupons, FastAccount fastAccount) {
 		/*
 		 * Fast index for every coupon that is added to the system... every
 		 * coupon has a unique code that links to its user
 		 */
 		for (FastCoupon fastCoupon : fastCoupons) {
 			couponCache.put(fastCoupon.getCouponCode(),
 					new Long(fastAccount.getId()));
 		}
 
 		fastAccount.addFastCoupons(fastCoupons);
 	}
 
 }
