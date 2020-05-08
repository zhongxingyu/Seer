 package localServices;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import microFacebook.Validate;
 import distributedServices.Helper;
 
 /**
  * The transaction class manages local transactions.
  */
 public class Transaction implements java.io.Serializable {
 
 	private static final long serialVersionUID = 1L;
 	
 	/*
 	 * The state that a transaction can have.
 	 */
 	private static enum State {
 		Optimistic, // In this state the transaction will acquire 
 					// resources without locking them.
 		Precomit,   // In this state all resources acquired by the 
 					// transaction are locked.
 		Commit, 	// The transaction is commited.
 		Aborted};   // The transaction is aborted.
 	
 	/**
 	 * Class represented a transacted resource.
 	 */
 	private static class Resource  implements java.io.Serializable {
 		private static final long serialVersionUID = 1L;
 		
 		// Resource lock.
 		public boolean lock = false;	
 		// The alias used for the resource in different transaction instances.
 		// Incremented every time is acquired by a transaction instance.
 		public int alias = 0;
 		// The number of times the resource was commited.
 		public int commit = 0;			 
 		
 		/**
 		 * Lock the resource
 		 * @return True if success.
 		 */
 		public boolean lock() {
 			if (lock == true) {
 				return false;
 			}
 			lock = true;
 			return true;
 		}
 		
 		/**
 		 * Unlock the resource.
 		 */
 		public void unlock() {
 			lock = false;
 		}
 		
 		/**
 		 * Get the resource name in this transaction instance.
 		 * @param name - Provides the base name of the resource.
 		 * @return - The alias name for the resource.
 		 */
 		public String getName(String name) {
 			return name + String.valueOf(alias);
 		}
 
 		/**
 		 * This resource checks if the resource has changed from
 		 * another copy of the same resource. A resource is changed 
 		 * when it is commited and the changes become visible.
 		 * @param resource - The resource to check against.
 		 * @return - True if the resource has changed, false otherwise.
 		 */
 		public boolean hasChanged(Resource resource) {
 			Validate.Assert(commit >= resource.commit);
 			Validate.Assert(alias >= resource.alias);
 			return commit != resource.commit;
 		}
 		
 		/**
 		 * Make a copy of a resource, this will become the 
 		 * resource representation in a transaction.
 		 * @return The new resource.
 		 */
 		Resource makeCopy() {
 			Resource resource = new Resource();
 			resource.commit = commit;
 			resource.alias = ++alias;
 			return resource;
 		}
 		
 		/**
 		 * Commit the resource - the commit becomes the alias,
 		 * because alias gives the transaction and the last transaction 
 		 * has locked the resource.
 		 */
 		void commit() {
 			Validate.Assert(commit <= alias);
 			commit = alias;
 		}
 	}
 	
 	/**
 	 * Class representing a transaction instance.
 	 */
 	private static class Instance  implements java.io.Serializable {
 		private static final long serialVersionUID = 1L;
 
 		// The state that the transaction has.
 		public State state;
 		// List of resources acquired by the transaction.
 		public Map<String, Resource> resources = new HashMap<String, Resource>();
 		
 		/**
 		 * Constructor.
 		 * @param locking - If false the transaction start in state Optimistic.
 		 */
 		private Instance(boolean locking) {
 			if (locking) {
 				state = State.Precomit;
 			}
 			state = State.Optimistic;
 		}
 	}
 	
 	/**
 	 * The state of the transaction manager.
 	 */
 	private static class ManagerState implements java.io.Serializable {
 		private static final long serialVersionUID = 1L;
 		// The master list of resources. This is the place transactions acquire resources from.
 		public Map<String, Resource> resources = new HashMap<String, Resource>();
 		// Map of transaction instances and their identifiers.
 		public Map<Integer, Instance> transactions = new HashMap<Integer, Instance>();
 		// identifier of the last transaction.
 		public Integer lastTransaction = 0;
 	}
 	
 	// The file holding the overall transaction state. It includes the
 	// manager and all transaction instances.
 	private final String MASTER_FILE = "transaction";
 	// The manager state contains the list of transactions and of master resources.
 	private ManagerState manager;
 	// The local file system.
 	private FileSystem fileSystem;
 	
 	/**
 	 * Constructor. Load the last saved state.
 	 * @param fileSystem - Provides the local file system.
 	 */
 	public Transaction(FileSystem fileSystem) {
 		this.fileSystem = fileSystem;
 //		try {
 //			String data = fileSystem.get(MASTER_FILE).toString();
 //			manager = Helper.unpack(data);
 //		} catch (StatusException e) {
 			manager = new ManagerState();
 //		}
 	}
 	
 	/**
 	 * Save the manager and all transactions state.
 	 */
 	public void saveTransactionState() {
 		try {
 			if (!fileSystem.fileExists(MASTER_FILE)) {
 				fileSystem.create(MASTER_FILE);
 			}
 			fileSystem.put(MASTER_FILE, Helper.packToString(manager));			
 		} catch (StatusException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Create a new transaction.
 	 * @return - Identifier of the transaction.
 	 */
 	public Integer create() {
 		Instance instance = new Instance(false);
 		Integer transactionId = ++manager.lastTransaction;
 		manager.transactions.put(transactionId, instance);
 		return transactionId;
 	}
 
 	/**
 	 * Acquire a resource from the master list.
 	 * @param name - Name of the resource.
 	 * @param lock - True if the resource should be locked.
 	 * @return - A copy of the resource if it has been acquired.
 	 * @throws StatusException
 	 */
 	private Resource acquire(String name, boolean lock) throws StatusException {
 		if (!manager.resources.containsKey(name)) {
 			manager.resources.put(name, new Resource());
 		}
 
 		if (lock && !manager.resources.get(name).lock()) {
 			throw new StatusException(Status.FileIsLocked);
 		}
 		
 		//
 		// Copy the master resource to a transaction local resource.
 		//
 		
 		Resource resource = manager.resources.get(name).makeCopy();
 		fileSystem.copy(
 				manager.resources.get(name).getName(name),
 				resource.getName(name));
 		return resource;
 	}
 	
 	/**
 	 * Acquire a resource in a transaction context.
 	 * @param transactionId - The transaction instance context.
 	 * @param name - The base name of the resource.
 	 * @return - The alias name of the resource in the transaction context.
 	 * @throws StatusException
 	 */
 	public String acquire(int transactionId, String name) 
 			throws StatusException {
 		
 		if (transactionId == 0) {
			if (!manager.resources.containsKey(name)) {
				manager.resources.put(name, new Resource());
			}
 			return manager.resources.get(name).getName(name);
 		}
 		
 		Instance i = manager.transactions.get(transactionId);
 		Validate.isTrue(i.state == State.Optimistic ||
 				i.state == State.Precomit);
 		
 		Resource resource;
 		if (i.resources.containsKey(name)) {
 			resource = i.resources.get(name);
 		} else {
 			resource = 
 				acquire(name, i.state == State.Precomit);
 			i.resources.put(name, resource);
 			saveTransactionState();
 		}
 		return resource.getName(name);
 	}
 
	/**
	 * Precomit the transaction. If it is an optimistic transaction
	 * lock all resources. If any resource has been changed, since
	 * acquired, the precommit fails.
	 */
 	public void precomit(int transactionId) throws StatusException {
 		Validate.isTrue(transactionId != 0);
 		Instance i = manager.transactions.get(transactionId);
 		Validate.isTrue(i.state == State.Optimistic ||
 				i.state == State.Precomit);
 		
 		if (i.state == State.Precomit) {
 			return;
 		}
 		
 		for(String name : i.resources.keySet()) {
 			
 			//
 			// Check that the resource hasn't been used and lock the 
 			// resource.
 			//
 			
 			Resource localResource = i.resources.get(name);
 			Resource masterResource = manager.resources.get(name);
 			if (masterResource.hasChanged(localResource) ||
 				!masterResource.lock()) {
 				i.state = State.Aborted;
 				throw new StatusException(Status.TransactionAborted);
 			}
 		}
 		i.state = State.Precomit;
 		saveTransactionState();
 	}
 	
	/**
	 * Commit the transaction. Make the master resource name be the
	 * the transaction name of the resource.
	 * @param transactionId - identifier of the transaction.
	 * @throws StatusException
	 */
 	public void commit(int transactionId) throws StatusException {
 		Validate.isTrue(transactionId != 0);
 		Instance i = manager.transactions.get(transactionId);
 		Validate.isTrue(i.state == State.Precomit);
 		
 		Resource masterResource;
 		for(String name : i.resources.keySet()) {
 			masterResource = manager.resources.get(name);
 			masterResource.commit();
 		}
 		i.state = State.Commit;
 		saveTransactionState();
 	}
 	
	/**
	 * Rollback the transaction - unlock all resources.
	 * @param transactionId
	 */
 	public void rollback(int transactionId) {
 		Validate.isTrue(transactionId != 0);
 		Instance i = manager.transactions.get(transactionId);
 		Validate.isTrue(
 				i.state == State.Optimistic ||
 				i.state == State.Precomit);
 
 		//
 		// Unlock all resources.
 		//
 		
 		if (i.state == State.Precomit) {
 			for(String name : i.resources.keySet()) {
 				manager.resources.get(name).unlock();
 			}
 		}
 	
 		i.state = State.Aborted;
 	}
 }
