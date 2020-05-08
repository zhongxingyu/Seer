 package org.h2.h2o.remote;
 
 import java.net.InetSocketAddress;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.h2.engine.Constants;
 import org.h2.engine.Database;
 import org.h2.h2o.comms.remote.DatabaseInstanceRemote;
 import org.h2.h2o.manager.ISchemaManager;
 import org.h2.h2o.manager.MovedException;
 import org.h2.h2o.manager.SchemaManagerReference;
 import org.h2.h2o.util.DatabaseURL;
 import org.h2.h2o.util.SchemaManagerReplication;
 import org.h2.test.h2o.ChordTests;
 
 import uk.ac.standrews.cs.nds.p2p.exceptions.P2PNodeException;
 import uk.ac.standrews.cs.nds.p2p.interfaces.IKey;
 import uk.ac.standrews.cs.nds.p2p.util.SHA1KeyFactory;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.stachordRMI.impl.ChordNodeImpl;
 import uk.ac.standrews.cs.stachordRMI.interfaces.IChordNode;
 import uk.ac.standrews.cs.stachordRMI.interfaces.IChordRemoteReference;
 
 /**
  * Interface between the database system and Chord, providing various methods which use chord
  * functionality to locate databases and schema managers on the ring.
  * 
  * @author Angus Macdonald (angus@cs.st-andrews.ac.uk)
  */
 public class ChordInterface implements Observer {
 
 	/**
 	 * The node that this ChordManager instance encapsulates.
 	 */
 	private IChordNode chordNode;
 
 
 
 	/**
 	 * The port on which the local Chord node is running its RMI server. 
 	 */
 	private int rmiPort;
 
 	/**
 	 * Used to cache the location of the schema manager by asking the known node where it is on startup. This is only
 	 * ever really used for this initial lookup. The rest of the schema manager funcitonality is hidden behind the 
 	 * SchemaManagerReference object.
 	 */
 	private DatabaseURL actualSchemaManagerLocation = null;
 
 	private SchemaManagerReference schemaManagerRef;
 
 
 	private Database db;
 
 	/**
 	 * <p>Set of nodes in the system sorted by key order.
 	 * 
 	 * <p>This set is only maintained if {@link org.h2.engine.Constants#IS_TEST} is true, and won't
 	 * work in anything other than a test environment where each node is in the same address space.
 	 * @param schemaManagerRef 
 	 */
 	//public static SortedSet<IChordNode> allNodes = new TreeSet<IChordNode>(new NodeComparator());
 
 	public ChordInterface (Database db, SchemaManagerReference schemaManagerRef){
 		this.db = db;
 		this.schemaManagerRef = schemaManagerRef;
 	}
 
 	/**
 	 * Start a new Chord ring at the specified location.
 	 * @param hostname	The hostname on which the Chord ring will be started. This must be a local address to the machine
 	 * 	on which this process is running.
 	 * @param port	The port on which the Chord node will listen. This is port on which the RMI registry will be created.
 	 * @param databaseName	The name of the database instance starting this Chord ring. This information is used purely
 	 * 	for diagnostic output, so can be left null.
 	 * @return	True if the chord ring was started successfully; otherwise false.
 	 */
 	public boolean startChordRing(String hostname, int port, DatabaseURL databaseURL) {
 
 		this.rmiPort = port;
 
 		InetSocketAddress localChordAddress = new InetSocketAddress(hostname, port);
 		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Deploying new Chord ring on " + hostname + ":" + port);
 
 		/*
 		 * Join the existing Chord Ring.
 		 */
 		try {
 			chordNode  = ChordNodeImpl.deployNode(localChordAddress, null);
 
 			if (Constants.IS_TEST){ 
 				//allNodes.add(chordNode);
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 			return false;
 		} catch (P2PNodeException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		if (chordNode == null){
 			ErrorHandling.hardError("Failed to create Chord Node.");
 		}
 
 		this.schemaManagerRef.setLookupLocation(chordNode.getProxy());
 
 		this.actualSchemaManagerLocation = databaseURL;
 
 		this.schemaManagerRef.setInKeyRange(true);
 
 		((ChordNodeImpl)chordNode).addObserver(this);
 
 		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Started local Chord node on : " + databaseURL.getDbLocationWithoutIllegalCharacters() + " : " + hostname + ":" + port + 
 				" : initialized with key :" + chordNode.getKey().toString(10) + " : " + chordNode.getKey() + " : schema manager at " + this.schemaManagerRef.getLookupLocation() + " : ");
 		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Schema manager key: : : : :" + SchemaManagerReference.schemaManagerKey.toString(10) + " : " + SchemaManagerReference.schemaManagerKey);
 
 		return true;
 	}
 
 	/**
 	 * Join an existing chord ring.
 	 * 	
 	 * @param localHostname 	The hostname on which this node will start. This must be a local address to the machine
 	 * 	on which this process is running. 
 	 * @param localPort			The port on which this node will listen. The RMI server will run on this port.
 	 * @param remoteHostname	The hostname of a known host in the existing Chord ring.
 	 * @param remotePort		The port on which a known host is listening.
 	 * @param databaseName		The name of the database instance starting this Chord ring. This information is used purely
 	 * 	for diagnostic output, so can be left null.	
 	 * @return true if a node was successfully created and joined an existing Chord ring; otherwise false.
 	 */
 	public boolean joinChordRing(String localHostname, int localPort, String remoteHostname, int remotePort, String databaseName) {
 
 		this.rmiPort = localPort;
 
 		InetSocketAddress localChordAddress = new InetSocketAddress(localHostname, localPort);
 		InetSocketAddress knownHostAddress = new InetSocketAddress(remoteHostname, remotePort);
 
 		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Connecting to existing Chord ring on " + remoteHostname + ":" + remotePort);
 
 		try {
 			chordNode = ChordNodeImpl.deployNode(localChordAddress, knownHostAddress);
 			if (Constants.IS_TEST){ 
 				//allNodes.add(chordNode);
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 			return false;
 		} catch (P2PNodeException e) {
 			ErrorHandling.errorNoEvent("Failed to create new chord node on + " + localHostname + ":" + localPort + " known host: " + remoteHostname + ":" + remotePort);
 			return false;
 		}	
 
 		if (chordNode == null){
 			ErrorHandling.hardError("Failed to create Chord Node.");
 			return false;
 		}
 
 		//((ChordNodeImpl)chordNode).addObserver(this); //now done by the calling method.
 
 		this.schemaManagerRef.setInKeyRange(false);
 
 		actualSchemaManagerLocation = getSchemaManagerURL(remoteHostname, remotePort);
 
 		/*
 		 *	Ensure the ring is stable before continuing with any tests. 
 		 */
 
 		if (Constants.IS_TEST){
 
 
 			//RingStabilizer.waitForStableNetwork(allNodes);
 
 			//	for (IChordNode node: allNodes){
 			///		System.out.println("CHECK. Suc: " + node.getSuccessor());
 			//	}
//			System.err.println("Schema manager key: " + SchemaManagerReference.schemaManagerKey);
 		}
 
 		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Started local Chord node on : " + 
 				databaseName + " : " + localHostname + " : " + localPort + " : initialized with key :" + chordNode.getKey().toString(10) + 
 				" : " + chordNode.getKey() + " : schema manager at " + this.schemaManagerRef.getLookupLocation() + " : " + chordNode.getSuccessor().getKey());
 
 
 		return true;
 	}
 
 	/**
 	 * Called by various chord functions in {@link ChordNodeImpl} which are being observed. Of particular interest
 	 * to this class is the case where the predecessor of a node changes. This is used to assess whether the schema managers
 	 * location has changed.
 	 * 
 	 * <p>If changing this method please note that it is called synchronously by the Observable class, ChordNodeImpl. This means
 	 * that if you try and do something such as chordNode.stabilize() you will possibly introduce some form of deadlock into Chord. This is
 	 * difficult to debug, but is the most likely cause of a ring failing to close properly (i.e. not stablizing even after an extended period).
 	 * 
 	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
 	 */
 	@Override
 	public void update(Observable o, Object arg) {
 		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Start of update() : " + arg);
 
 		/*
 		 * If the predecessor of this node has changed.
 		 */
 		if (arg.equals(ChordNodeImpl.PREDECESSOR_CHANGE_EVENT)){
 			if (this.schemaManagerRef.getLookupLocation() == null) {
 				//This is a new chord node. If it is responsible for the schema manager its successor will say so.
 				Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "End of update() : " + arg);
 				return;
 			} 
 
 			IChordRemoteReference oldSchemaManagerLocation = this.schemaManagerRef.getLookupLocation();
 
 			IChordRemoteReference newSchemaManagerLocation = null;
 			try {
 				//			chordNode.stabilize();
 				newSchemaManagerLocation = lookupSchemaManagerNodeLocation();
 			} catch (RemoteException e1) {
 				ErrorHandling.errorNoEvent("Current schema manager lookup does not resolve to active host.");
 			}
 			if (!oldSchemaManagerLocation.equals(newSchemaManagerLocation)){
 				//We have a new schema manager location
 
 				Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "\tThe schema manager location has changed.");
 				Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "\tOld location: " + oldSchemaManagerLocation);
 				//Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "\tNew location: " + newSchemaManagerLocation);
 
 				/*
 				 * In the case of a new node: this node must check that it still has the schema manager
 				 * in its keyspace. If it does nothing else needs to happen. If not, then the new node (the predecessor) is now
 				 * in control of the schema manager.
 				 * 
 				 * In the case of a node failure: this node must check whether it has taken over responsibility for the schema manager.
 				 * If it was already responsible for the schema manager then nothing will have changed.
 				 */
 
 
 				/*
 				 * TODO check whether the schema manager was ever in this nodes key space.
 				 */
 
 
 				//					RingStabilizer.waitForStableNetwork(allNodes);
 
 
 				try {
 					boolean inKeyRange = chordNode.inLocalKeyRange(SchemaManagerReference.schemaManagerKey);
 
 					schemaManagerRef.setInKeyRange(inKeyRange);
 
 					if (!this.schemaManagerRef.isInKeyRange() && inKeyRange){ //The schema manager has only just become in the key range of this node.
 						Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "\tThe schema manager is now in the key range of : " + chordNode);
 
 						//	schemaManagerNowInKeyRange();
 
 					} else if (this.schemaManagerRef.isInKeyRange() && inKeyRange){ //Nothing has changed.
 						Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "\tThe schema manager location has not changed. It is still in the key range of " + chordNode);
 					} else if (this.schemaManagerRef.isInKeyRange() && !inKeyRange){
 						Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "\tThe schema manager is no longer in the key range of : " + chordNode);
 					}
 
 					this.schemaManagerRef.setInKeyRange(inKeyRange);
 
 				} catch (uk.ac.standrews.cs.nds.p2p.exceptions.P2PNodeException  e) {
 					e.printStackTrace();
 
 				}
 
 
 			}
 		} else if (arg.equals(ChordNodeImpl.SUCCESSOR_CHANGE_EVENT)){
 			/*
 			 * The successor has changed. Make sure the schema manager is replicated to the new successor if this instance is controlling the schema
 			 * manager.
 			 */
 			if (this.schemaManagerRef.isSchemaManagerLocal()){
 				//The schema manager is running locally. Replicate it's state to the new successor.
 				IChordRemoteReference successor = chordNode.getSuccessor();
 
 				DatabaseInstanceRemote dbInstance = null;
 
 				try {
 					String hostname = successor.getRemote().getAddress().getHostName();
 					int port = successor.getRemote().getAddress().getPort();
 
 					dbInstance = getRemoteReferenceToDatabaseInstance(hostname, port);
 
 					if (dbInstance == null){
 						/*
 						 * The remote chord node hasn't been fully instantiated yet. Wait a while then try again.
 						 */
 
 						if (!Constants.IS_NON_SM_TEST){
 							//Don't bother trying to replicate the schema manager if this is a test which doesn't require it.
 							SchemaManagerReplication newThread = new SchemaManagerReplication(hostname, port, this.db, this);
 							newThread.start();
 						}
 
 					} else {
 
 						this.db.getSchemaManager().addSchemaManagerDataLocation(dbInstance);
 
 						//dbInstance.createNewSchemaManagerBackup(db.getSchemaManager());
 						//dbInstance.executeUpdate("CREATE REPLICA SCHEMA H2O");
 						
 						if (Constants.IS_TEST){
 							ChordTests.setReplicated(true);
 						}
 
 					}
 				} catch (RemoteException e) {
 					e.printStackTrace();
 				} catch (MovedException e) {
 					schemaManagerRef.handleMovedException(e);
 				}
 
 
 
 			}
 
 		}
 		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "End of update() : " + arg);
 
 	}
 
 	/**
 	 * Called when the schema manager has recently moved into the key range of this node.
 	 * 
 	 * At this point the node can decide to do nothing (leave the schema manager where it is and maintain a reference to it), or 
 	 * transfer the schema manager to this machine. This method currently does the latter to test its functionality.
 	 */
 	private void schemaManagerNowInKeyRange() {
 		System.err.println("##################################################################################");
 
 		schemaManagerRef.migrateSchemaManagerToLocalInstance(false);
 	}
 
 	/**
 	 * Returns the port on which this chord nodes RMI server is running.
 	 * @return RMI registry port for this chord node.
 	 */
 	public int getRmiPort() {
 		return rmiPort;
 	}
 
 	/**
 	 * Returns a reference to this chord nodes RMI registry.
 	 * @return	The RMI registry of this chord node.
 	 */
 	public Registry getLocalRegistry() {
 		try {
 			return LocateRegistry.getRegistry(rmiPort);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Get the actual location of the schema manager by first looking up the location where the 'schemamanager'
 	 * lookup resoloves to, then querying the database instance at this location for the location of the schema manager.
 	 * @return
 	 */
 	public DatabaseURL getActualSchemaManagerLocation() {
 		if (actualSchemaManagerLocation != null){ return actualSchemaManagerLocation; }
 
 		IChordRemoteReference oldSchemaManagerNodeLocation = this.schemaManagerRef.getLookupLocation();
 
 		IChordRemoteReference sml = null;
 		try {
 			sml = lookupSchemaManagerNodeLocation();
 			schemaManagerRef.setLookupLocation(sml);
 		} catch (RemoteException e1) {
 			e1.printStackTrace();
 
 			return null;
 		}
 
 		if (!sml.equals(oldSchemaManagerNodeLocation) || actualSchemaManagerLocation == null){ //look for a new schema manager location
 			try {
 				actualSchemaManagerLocation  = getSchemaManagerURL(sml.getRemote().getAddress().getHostName(), sml.getRemote().getAddress().getPort());
 			} catch (RemoteException e) {
 				ErrorHandling.exceptionErrorNoEvent(e, "Occurred when trying to find schema manager.");
 			}
 		} // else - the schema manager location hasn't changed. You can use the old one. 
 
 		return actualSchemaManagerLocation;
 	}
 
 	/**
 	 * Get the actual location of the schema manager (not the location which a chord lookup
 	 * resolves to) from a known host.
 	 * @param knownHost	The URL of a known host. This must include the correct hostname and RMI port.
 	 * @return A {@link DatabaseURL} object representing the schema managers current location.
 	 */
 	public DatabaseURL getSchemaManagerURL(DatabaseURL knownHost) {
 		return getSchemaManagerURL(knownHost.getHostname(), knownHost.getRMIPort());
 	}
 
 	/**
 	 * Get the actual location of the schema manager (not the location which a chord lookup
 	 * resolves to) from a known host.
 	 * @param hostname The hostname of the known host.
 	 * @param port		The port on which the known host runs its RMI server.
 	 * @return A {@link DatabaseURL} object representing the schema managers current location.
 	 */
 	private DatabaseURL getSchemaManagerURL(String hostname, int port) {
 		try {
 			return getRemoteReferenceToDatabaseInstance(hostname, port).getSchemaManagerLocation();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/**
 	 * Obtain a remote reference to a database instance, where the instance has a chord node running
 	 * on the specified hostname and port.
 	 * 
 	 * <p>This information is used to locate the chord node's RMI registry which provides a reference to
 	 * the database instance itself.
 	 * @return	Remote reference to the database instance.
 	 */
 	public DatabaseInstanceRemote getRemoteReferenceToDatabaseInstance(String hostname, int port) {
 		Registry remoteRegistry;
 		try {
 			remoteRegistry = LocateRegistry.getRegistry(hostname, port);
 
 			DatabaseInstanceRemote dbInstance = (DatabaseInstanceRemote) remoteRegistry.lookup(ChordDatabaseRemote.LOCAL_DATABASE_INSTANCE);
 
 			return dbInstance;
 
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (NotBoundException e) {
 			Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Database Instance wasn't bound on hostname : " + hostname + ", Port : " + port);
 
 			//			try {
 			//				
 			//				remoteRegistry = LocateRegistry.getRegistry(hostname, port);
 			//				for (String s: remoteRegistry.list()){
 			//					System.out.println(s);
 			//				}
 			//			} catch (AccessException e1) {
 			//				// TODO Auto-generated catch block
 			//				e1.printStackTrace();
 			//			} catch (RemoteException e1) {
 			//				// TODO Auto-generated catch block
 			//				e1.printStackTrace();
 			//			}
 
 		}
 		return null;
 	}
 
 	/**
 	 * Get a reference to the Chord node which is responsible for managing the database's schema manager lookup,
 	 * BUT NOT NECESSARILY THE SCHEMA MANAGER ITSELF.
 	 * @return	Remote reference to the chord node managing the schema manager.
 	 * @throws RemoteException 
 	 */
 	private IChordRemoteReference lookupSchemaManagerNodeLocation() throws RemoteException{
 		IChordRemoteReference newSMLocation = null;
 
 		if (chordNode != null){
 			newSMLocation = chordNode.lookup(SchemaManagerReference.schemaManagerKey);
 		}
 
 
 		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Found schema manager at: " + newSMLocation);
 
 		this.schemaManagerRef.setLookupLocation(newSMLocation);
 
 		return newSMLocation;
 	}
 
 	/**
 	 * @param schemaManager
 	 */
 	public void bindSchemaManager(SchemaManagerReference schemaManagerRef) {
 		ISchemaManager stub = null;
 
 		try {
 			stub = (ISchemaManager) UnicastRemoteObject.exportObject(schemaManagerRef.getSchemaManager(), 0);
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 
 		try {
 			getLocalRegistry().bind("SCHEMA_MANAGER", stub);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * 
 	 */
 	public void shutdownNode() {
 		Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Shutting down node: " + chordNode);
 
 		if (!Constants.IS_NON_SM_TEST){
 			//allNodes.remove(chordNode);
 			chordNode.destroy();
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	public ChordNodeImpl getChordNode() {
 		return (ChordNodeImpl) chordNode;
 	}
 
 	/**
 	 * @return the currentSMLocation
 	 */
 	public IChordRemoteReference getCurrentSMLocation() {
 		return this.schemaManagerRef.getLookupLocation();
 	}
 
 	//	public IChordRemoteReference lookupInstanceLocation(DatabaseURL databaseURL){
 	//
 	//		InetSocketAddress address = new InetSocketAddress(databaseURL.getHostname(), databaseURL.getRMIPort());
 	//
 	//		IKey node_key = keyFactory.generateKey(address);
 	//
 	//		IChordRemoteReference remoteNode = null;
 	//
 	//		try {
 	//			remoteNode = chordNode.lookup(node_key);
 	//		} catch (RemoteException e) {
 	//			e.printStackTrace();
 	//
 	//		}
 	//
 	//		return remoteNode;
 	//	}
 }
