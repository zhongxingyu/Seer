 /**
  *
  * Copyright 2013 the original author or authors.
  * Copyright 2013 Sorcersoft.com S.A.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package sorcer.core.provider;
 
 // Imported classes
 
 import net.jini.config.Configuration;
 import net.jini.core.entry.Entry;
 import net.jini.core.lease.Lease;
 import net.jini.core.lease.LeaseDeniedException;
 import net.jini.core.lease.UnknownLeaseException;
 import net.jini.core.transaction.Transaction;
 import net.jini.core.transaction.TransactionFactory;
 import net.jini.core.transaction.server.TransactionManager;
 import net.jini.lease.LeaseListener;
 import net.jini.lease.LeaseRenewalEvent;
 import net.jini.lease.LeaseRenewalManager;
 import net.jini.space.JavaSpace;
 import net.jini.space.JavaSpace05;
 import sorcer.core.Provider;
 import sorcer.core.exertion.ExertionEnvelop;
 import sorcer.core.loki.exertion.KPEntry;
 import sorcer.core.loki.member.LokiMemberUtil;
 import sorcer.service.ExecState;
 import sorcer.service.Exertion;
 import sorcer.service.ServiceExertion;
 import sorcer.service.Task;
 import sorcer.service.space.SpaceAccessor;
 import sorcer.service.txmgr.TransactionManagerAccessor;
 
 import java.rmi.RemoteException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * This is a class creates a JavaSpace taker that extends the {@link Thread}
  * class and implements the interface {@link LeaseListener}
  *
  * @see Thread
  * @see LeaseListener
  */
 public class SpaceTaker implements Runnable, LeaseListener {
 	static protected final String LOKI_ONLY = "CreatorsPublicKey";
 
 	static Logger logger = Logger.getLogger(SpaceTaker.class.getName());
 
 	protected boolean isTransactional;
 
 	protected static long TRANSACTION_LEASE_TIME = 1000 * 60 * 1; // 1 minute
 
 	protected long transactionLeaseTimeout = TRANSACTION_LEASE_TIME;
 
 	public final static long SPACE_TIMEOUT = 1000 * 30; // 1/2 minute
 
 	protected long spaceTimeout = SPACE_TIMEOUT;
 
 	protected JavaSpace05 space;
 
 	protected SpaceTakerData data;
 
 	protected ExecutorService pool;
 
 	protected static LeaseRenewalManager leaseManager;
 
 	// controls the loop of this space worker
 	protected volatile boolean keepGoing = true;
 
 	public static class SpaceTakerData {
 		public ExertionEnvelop entry;
 		public LokiMemberUtil myMemberUtil;
 		public Provider provider;
 		public String spaceName;
 		public String spaceGroup;
 		public boolean workerTransactional;
 		public boolean noQueue;
 
 		public SpaceTakerData() {
 		}
 
 		public SpaceTakerData(ExertionEnvelop entry, LokiMemberUtil member,
 				Provider provider, String spaceName, String spaceGroup,
 				boolean workerIsTransactional, boolean noQueue) {
 			this.provider = provider;
 			this.entry = entry;
 			this.myMemberUtil = member;
 			this.spaceName = spaceName;
 			this.spaceGroup = spaceGroup;
 			this.workerTransactional = workerIsTransactional;
 			this.noQueue = noQueue;
 		}
 
 		public String toString() {
 			return entry.describe();
 		}
 	}
 
 	/**
 	 * Default constructor. Set the worker thread as a Daemon thread
 	 */
 	public SpaceTaker() {
 	}
 
 	/**
 	 * This is a Constructor. It executes the default constructor plus set the
 	 * provider worker data and executor service pool. The transaction lease
 	 * time is set and space time out time is established.
 	 * 
 	 * @param data
 	 *            SpaceDispatcher data
 	 * @param pool
 	 *            Executor service provides methods to manage termination and
 	 *            tracking progress of one or more asynchronous tasks
 	 */
 	public SpaceTaker(SpaceTakerData data, ExecutorService pool) {
 		this();
 		this.data = data;
 		this.pool = pool;
 		this.transactionLeaseTimeout = getTransactionLeaseTime();
 		this.spaceTimeout = getTimeOut();
 		this.isTransactional = data.workerTransactional;
 	}
 
 	protected long getTransactionLeaseTime() {
 		long lt = TRANSACTION_LEASE_TIME;
 		Configuration config = null;
 		try {
 			config = data.provider.getProviderConfiguration();
 			lt = (Long) config.getEntry(ServiceProvider.PROVIDER,
 					ProviderDelegate.WORKER_TRANSACTION_LEASE_TIME, long.class);
 		} catch (Exception e) {
 			lt = TRANSACTION_LEASE_TIME;
 		}
 		return lt;
 	}
 
 	protected long getTimeOut() {
 		long st = SPACE_TIMEOUT;
 		Configuration config = null;
 		try {
 			config = data.provider.getProviderConfiguration();
 			st = (Long) config.getEntry(ServiceProvider.PROVIDER,
 					ProviderDelegate.SPACE_TIMEOUT, long.class);
 		} catch (Exception e) {
 			st = SPACE_TIMEOUT;
 		}
 		return st;
 	}
 
 	public void run() {
 		logger.finer("** running... isTransactional: " + isTransactional
 				+ ", transactionLeaseTimeout: " + transactionLeaseTimeout
 				+ ", spaceTimeout: " + spaceTimeout);
 		Transaction.Created txnCreated = null;
 		while (keepGoing) {
 			ExertionEnvelop ee = null;
 			try {
 				space = SpaceAccessor.getSpace(data.spaceName,
 						data.spaceGroup);
 				if (space == null) {
 					logger.severe("########### SpaceTaker DID NOT get JavaSpace...");
					Thread.sleep(spaceTimeout / 6);
 					continue;
 				}
 				// logger.log(Level.INFO, "worker space template envelop = "
 				// + data.entry.describe() + "\n service provider = "
 				// + provider);
 				if (data.noQueue && (((ThreadPoolExecutor) pool).getActiveCount() == ((ThreadPoolExecutor) pool)
                             .getCorePoolSize())) {
                     continue;
                 }
 
                 Transaction transaction = null;
                 if (isTransactional) {
                     txnCreated = createTransaction();
                     if (txnCreated == null) {
                         logger.severe("########### SpaceTaker DID NOT get transaction...");
                         Thread.sleep(spaceTimeout / 6);
                         continue;
                     }
                     transaction = txnCreated.transaction;
                 }
                 ee = (ExertionEnvelop) space.take(data.entry,
                         transaction, spaceTimeout);
 
 				// after 'take' timeout abort transaction and sleep for a while
 				// before 'taking' the next exertion
 				if (ee == null) {
 					if (txnCreated != null) {
 						txnCreated.transaction.abort();
 						leaseManager.remove(txnCreated.lease);
 						Thread.sleep(spaceTimeout / 2);
 					}
 					txnCreated = null;
 					continue;
 				}
 				// check is the exertion execution is abandoned (poisoned) by
 				// the requestor
 				// if (isAbandoned(ee.exertion) == true) {
 				// if (txn != null) {
 				// txn.commit();
 				// removeLease(txn);
 				// }
 				// txn = null;
 				// continue;
 				// }
 				// if (((ServiceProvider)
 				// data.provider).isSpaceSecurityEnabled()) {
 				// // if (ee.exertionID.equals(LOKI_ONLY)) {
 				// initDataMember(ee);
 				// }
                 pool.execute(new SpaceWorker(ee, txnCreated));
             } catch (Exception ex) {
				logger.log(Level.INFO, "END LOOP SPACE TAKER EXCEPTION", ex);
			}
 		}
 	}
 
 	synchronized public Transaction.Created createTransaction() {
 		if (leaseManager == null) {
 			leaseManager = new LeaseRenewalManager();
 		}
 		try {
 			TransactionManager tManager = TransactionManagerAccessor
 					.getTransactionManager();
 			if (tManager == null) {
 				return null;
 			}
 			Transaction.Created created = TransactionFactory.create(tManager,
 					transactionLeaseTimeout);
 			leaseManager.renewFor(created.lease,
 					Lease.FOREVER, transactionLeaseTimeout, this);
 			return created;
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (LeaseDeniedException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	protected boolean isAbandoned(Exertion exertion) {
 		if (space != null) {
 			ExertionEnvelop ee = new ExertionEnvelop();
 			ee.parentID = ((ServiceExertion) exertion).getParentId();
 			ee.state = ExecState.POISONED;
 			try {
 				if (space.readIfExists(ee, null, JavaSpace.NO_WAIT) != null) {
 					logger.info("...............dropped poisoned entry...............");
 					return true;
 				}
 			} catch (Exception e) {
 				logger.throwing(this.getClass().getName(), "isAbandoned", e);
 				// continue on
 			}
 		}
 		return false;
 	}
 
 	protected void initDataMember(ExertionEnvelop ee, Transaction txn) {
 		try {
 			KPEntry ckpeRes = (KPEntry) ee.exertion;
 			data.myMemberUtil.setGroupSeqId(ckpeRes.GroupSeqId);
 			data.myMemberUtil.takewriteKPExertion(ckpeRes.publicKey,
 					data.entry.serviceType);
 			data.myMemberUtil.readCCK(data.entry.serviceType);
 			ee = data.myMemberUtil.takeEnEE(data.entry, txn);
 		} catch (Exception e) {
 			logger.throwing(SpaceTaker.class.getName(), "run", e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.jini.lease.LeaseListener#notify(net.jini.lease.LeaseRenewalEvent)
 	 */
 	@Override
 	public void notify(LeaseRenewalEvent e) {
 		logger.severe("########### space transaction lost its lease: "
 				+ e.getLease());
 	}
 
 	class SpaceWorker implements Runnable {
 		private ExertionEnvelop ee;
 		private Transaction.Created txnCreated;
 		
 		SpaceWorker(ExertionEnvelop envelope,
 				Transaction.Created workerTxnCreated)
 				throws UnknownLeaseException {
 			ee = envelope;
 			if (workerTxnCreated != null) {
 				txnCreated = workerTxnCreated;
 //				leaseManager.setExpiration(txnCreated.lease,
 //						System.currentTimeMillis() + transactionLeaseTimeout);
 			}
 		}
 
 		public void run() {
 			try {
 				if (txnCreated != null)
 					logger.info("SpaceWorker >>> transaction: " + txnCreated.transaction);
 				Entry result = doEnvelope(ee, 
 						(txnCreated == null) ? null : txnCreated.transaction);
 				if (result != null) {
 					logger.info("SpaceWorker >>> Putting result to space---"
 							+ ((ExertionEnvelop) result).describe());
 					space.write(result, null, Lease.FOREVER);
 					if (txnCreated != null) {
 						txnCreated.transaction.commit();
 					}
 				} else {
 					if (txnCreated != null) {
 						txnCreated.transaction.abort();
 					}
 				}
 			} catch (Throwable e) {
 				e.printStackTrace();
 			}
 			try {
 				if (txnCreated != null)
 					leaseManager.remove(txnCreated.lease);
 			} catch (UnknownLeaseException e) {
 				// do nothing
 				e.printStackTrace();
 			}
 		}
 
 		public Entry doEnvelope(ExertionEnvelop ee, Transaction transaction) {
 			ServiceExertion se = null, out = null;
 			try {
 				logger.info("\n----SpaceWorker>>execute invoked");
 				ee.exertion.getControlContext().appendTrace(
 						"taken by: " + data.provider.getProviderName() + ":"
 								+ data.provider.getProviderID());
 				se = (ServiceExertion) ee.exertion;
 
 				if (se instanceof Task) {
 					// task for the worker's provider
 					out = ((ProviderDelegate) ((ServiceProvider) data.provider)
 							.getDelegate()).doTask((Task) se, transaction);
 				} else {
 					// delegate it to another collaborating service
 					out = (ServiceExertion) data.provider.service(se,
 							transaction);
 				}
 				if (out != null) {
 					out.setStatus(ExecState.DONE);
 					ee.state = ExecState.DONE;
 					ee.exertion = out;
 				} else {
 					se.setStatus(ExecState.ERROR);
 					ee.state = ExecState.ERROR;
 					ee.exertion = se;
 				}
 			} catch (Throwable th) {	
 				logger.throwing(this.getClass().getName(),
 						"doEnvelope", th);
 				//th.printStackTrace();
 				if (th instanceof Exception) {
 					ee.state = ExecState.FAILED;
 					((ServiceExertion) ee.exertion).setStatus(ExecState.FAILED);
 				} else if (th instanceof Error) {
 						ee.state = ExecState.ERROR;
 						((ServiceExertion) ee.exertion).setStatus(ExecState.ERROR);
 				}
 				((ServiceExertion) ee.exertion).reportException(th);
 			}
 			return ee;
 		}
 	}
 
 	public void setKeepGoing(boolean keepGoing) {
 		this.keepGoing = keepGoing;
 	}
 
 }
