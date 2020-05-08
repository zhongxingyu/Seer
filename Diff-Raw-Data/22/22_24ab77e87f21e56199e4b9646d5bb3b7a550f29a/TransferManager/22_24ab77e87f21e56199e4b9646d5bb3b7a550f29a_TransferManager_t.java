 package xtremweb.api.transman;
 
 /**
  * TransferManager is the engine responsible for processing, monitoring 
  * and launching transfers.
  * 
  * Created: Sun Feb 18 17:54:41 2007
  *
  * @author <a href="mailto:fedak@lri.fr">Gilles Fedak</a>
  * @version 1.0
  */
 
 import xtremweb.core.iface.*;
 import xtremweb.core.obj.dt.Transfer;
 
 import xtremweb.core.db.*;
 import xtremweb.core.obj.dc.*;
 import xtremweb.core.obj.dr.*;
 import xtremweb.serv.dt.*;
 import xtremweb.core.uid.*;
 import xtremweb.core.conf.*;
 import xtremweb.core.util.*;
 
 import xtremweb.core.log.Logger;
 import xtremweb.core.log.LoggerFactory;
 import xtremweb.core.perf.PerfMonitor;
 import xtremweb.core.perf.PerfMonitorFactory;
 
 import java.util.*;
 import java.io.File;
 import java.rmi.RemoteException;
 import java.util.Collection;
 import java.util.Iterator;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Extent;
 import javax.jdo.Query;
 import javax.jdo.Transaction;
 import java.util.Properties;
 
 
 
 /**
  * <code>TransferManager</code>.
  *
  * @author <a href="mailto:fedak@xtremciel.local">Gilles Fedak</a>
  * @version 1.0
  */
 public class TransferManager {
 
     private InterfaceRMIdt dt = null;
     private InterfaceRMIdr dr = null; //UNUSED FIXME
 
     /** time between two periodic activities (in milli seconds) */
     private int timeout = 1000; 
 
     private Timer timer;
 
     private int concurrentDowload=1;
 
     private final static PerfMonitor perf = PerfMonitorFactory.createPerfMonitor("TransferManager", "hits per second", 3000);
 
     /*
      * <code>oobTransfers</code> is a Hashtable associating an
      * OOBTransfer to each transfer.
      * It is used to cache OOBTransfer and to avoid creating OOBTransfer for
      * each Transfer scanned in the Database
      */
     private SortedVector oobTransfers;
 
     private Logger log = LoggerFactory.getLogger("Transfer Manager (transman)");
 
     /**
      * Creates a new <code>TransferManager</code> instance.
      *
      * @param ldt an <code>InterfaceRMIdt</code> value
      * @param ldr an <code>InterfaceRMIdr</code> value
      */
     public TransferManager(InterfaceRMIdr ldr, 
 			   InterfaceRMIdt ldt) {
 	dr = ldr;
 	dt = ldt;
 	init();
     }
 
     /**
      * Creates a new <code>TransferManager</code> instance.
      *
      */
     public TransferManager() {
 	init();
     }
 
 
     /**
      * Creates a new <code>TransferManager</code> instance.
      *
      * @param comms a <code>Vector</code> value
      */
     public TransferManager(Vector comms) {
 
 	for (Object o : comms) {
 	    if (o instanceof InterfaceRMIdr) dr = (InterfaceRMIdr) o;
 	    if (o instanceof InterfaceRMIdt) dt = (InterfaceRMIdt) o;
 	}
 	init();
     }
 
     private void init() {
 	oobTransfers = new SortedVector(new OOBTransferOrder());
     }
 
     /**
      * <code>registerTransfer</code> adds a tranfer to the TransferManager
      * The transfer is persisted in the database. It will be later read 
      * by the main loop
      * @param tuid a <code>String</code> value
      * @param oobt an <code>OOBTransfer</code> value
      */
     public void registerTransfer(String tuid, OOBTransfer oobt) {
 	//	OOBTransferFactory.persistOOBTransfer(oobt);
 	oobt.persist();
     }
 
 
     public Transfer createTransfer() throws TransferManagerException {
 	try {
 	    Transfer trans = new Transfer();
 	    DBInterfaceFactory.getDBInterface().makePersistent(trans);
 	    dt.putTransfer(trans);  
 	    return trans;
 
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	}
 	throw new TransferManagerException();
     }
 
     /**
      * <code>start</code> launches periodic TM engine
      */
     public void start(boolean isDaemon) { 
 	log.debug("Starting TM Engine");
 	if (timer==null) timer=new Timer("TransferManagerTimer", isDaemon); 
 	timer.schedule(new TimerTask() { 
 		public void run() { 
 		    checkTransfer();
 		} 
 	    } , 0, timeout ); 
     }
 
     /**
      * <code>start</code> launches periodic Active Data engine
      */
     public void start() { 
 	// by default, do not start as a daemon
 	start(false);
     }
 
     /**
      * <code>stop</code> stops periodic TM Engine
      */
     public void stop() {
 	log.debug("Stopping TM Engine");
 	timer.cancel();
	timer.purge();
	timer = null;
     }
 
  
 
     public void removeOOBTransfer(Transfer trans) throws OOBException {
 	oobTransfers.removeElement(trans.getuid());
     }
 
     public OOBTransfer getOOBTransfer(Transfer trans) throws OOBException {
 	OOBTransfer oob = null;
 	int idx =  oobTransfers.search(trans.getuid());
 	if (idx != -1)
 	    oob = (OOBTransfer) oobTransfers.elementAt(idx);
 	else {
 	    oob = OOBTransferFactory.createOOBTransfer(trans);
 	    oobTransfers.addElement(oob);
 	    log.debug("TransferManager new transfer " + trans.getuid() + " : " + oob.toString());
 	}
 	/*
 	OOBTransfer oob = (OOBTransfer) oobTransfers.get(trans.getuid());
 	if (oob==null) {
 	    oob = OOBTransferFactory.createOOBTransfer(trans);
 	    oobTransfers.put(trans.getuid(), oob);
 	    log.debug("RESUMING TRANSFER " + trans.getuid() + " : " + oob.toString());
 	}
 	*/
 	return oob;
     }
 
     /**
      * <code>checkTransfer</code> scans the database and take decision
      *     according to the status of the trabsfer 
      * <ol>
      * <li> prepare transfert (check for data availability)
      * <li> check for finished transfer
      * <li> cleanup transfer
      * </ol>
      */
     private void checkTransfer() {
 	long start=System.currentTimeMillis();
 	
 	PersistenceManagerFactory pmf = DBInterfaceFactory.getPersistenceManagerFactory();
 	PersistenceManager pm = pmf.getPersistenceManager();
 	Transaction tx=pm.currentTransaction();
 	
 	try {
 	    tx.begin();
 	    /* begin nouveau */
 	    Query query = pm.newQuery(Transfer.class, 
 				      "status != " + TransferStatus.TODELETE  ); 	    
 	    Collection results = (Collection)query.execute();
 	    if (results==null) {
 		log.debug("nothing to check");
 		return;
 	    }
 	    Iterator iter = results.iterator();
 
 	    OOBTransfer oob;
             while (iter.hasNext()) {
 		Transfer trans = (Transfer) iter.next();
 		log.debug("Checking Transfer : " + trans.getuid() + ":" + TransferType.toString(trans.gettype()));
 		switch (trans.getstatus()) {
 		    //Register the transfer remotely if it
 		    //succeed, set the local transfer to  READY
 		    //if not set the transfert to INVALID
 		    //set the remote transfer to READY, if it
 		    //fails set the local transfer to INVALID
 
 		case TransferStatus.PENDING :
 		    log.debug("PENDING");
 		    try {
 			oob = getOOBTransfer(trans);
 			if ( TransferType.isLocal(trans.gettype()) ) {
 			    log.debug("Registring remote" + oob);
 			    Transfer tcpy =  (Transfer) pm.detachCopy(trans);
 			    log.debug ("transfer " + tcpy + " | data " +  oob.getData() + " | remote protocol " +  oob.getRemoteProtocol() + " | remote locator " +  oob.getRemoteLocator());
 			    dt.registerTransfer( tcpy, 
                                                  oob.getData(), 
 						 oob.getRemoteProtocol(),
 						 oob.getRemoteLocator());
 			    log.debug("Transfer registred");
 			    dt.setTransferStatus(trans.getuid(), 
 			    			 TransferStatus.READY);
 			}
 			
 		    } catch (Exception re) {
 			log.debug("cannot register transfer " + re);
 			re.printStackTrace();
 			trans.setstatus(TransferStatus.INVALID);
 			break;
 		    }
 		    trans.setstatus(TransferStatus.READY);
 		    break;
 		    
 		case TransferStatus.READY :
 		    log.debug("READY");			
 		    try {
 			log.debug("start tranfer : " + trans.getuid());
 			//correct transfer creation
 			if (TransferType.isLocal(trans.gettype()))
 			    dt.startTransfer(trans.getuid());
 			//going to start the transfer
 			oob = getOOBTransfer(trans);
 			if (TransferType.isLocal(trans.gettype())) {
 			    log.debug("oob connect");
 			    oob.connect();
 			}
 			if (trans.gettype() == TransferType.UNICAST_SEND_SENDER_SIDE ) {
 			    log.debug("oob sendSenderSide");
 			    oob.sendSenderSide();
 			}
 			if (trans.gettype() == TransferType.UNICAST_SEND_RECEIVER_SIDE ) {
 			    log.debug("oob sendReceiverSide");
 			    oob.sendReceiverSide();
 			}
 			if (trans.gettype() == TransferType.UNICAST_RECEIVE_RECEIVER_SIDE ) {
 			    log.debug("oob receiveReceiverSide");
 			    oob.receiveReceiverSide();
 			}
 			if (trans.gettype() == TransferType.UNICAST_RECEIVE_SENDER_SIDE ) {
 			    log.debug("oob receiveSenderSide");
 			    oob.receiveSenderSide();
 			}
 			if (TransferType.isLocal(trans.gettype())) {
 			    dt.setTransferStatus(trans.getuid(), 
 						 TransferStatus.TRANSFERING);
 			}
 		    } catch (RemoteException re) {
 			trans.setstatus(TransferStatus.INVALID);
 			break;
 		    } catch (OOBException oobe) {
 			trans.setstatus(TransferStatus.INVALID);
 			break;
 		    }
 		    trans.setstatus(TransferStatus.TRANSFERING);
 		    break;
 
 		case TransferStatus.INVALID :
 		    log.debug("INVALID");
 		    try {
 			if ( TransferType.isLocal(trans.gettype()) )
 			    dt.setTransferStatus(trans.getuid(), 
 						 TransferStatus.INVALID);
 		    } catch (RemoteException re) {
 			trans.setstatus(TransferStatus.INVALID);
 			break;
 		    }
 		    trans.setstatus(TransferStatus.TODELETE);
 		    break;
 
 		case TransferStatus.TRANSFERING :
 		    //check the status
 		    log.debug("TRANSFERING");
 		    boolean complete = false;
 		    //check if transfer is complete
 		    try {
 			oob = getOOBTransfer(trans);
 			//FIXME gros bordel dans le sens du transfer
 			//TODO changer le if(!) en if()
 			log.debug("transfer type " + TransferType.toString(trans.gettype()));
 			if ( trans.gettype() ==  TransferType.UNICAST_SEND_SENDER_SIDE )
 			    complete = dt.poolTransfer(trans.getuid());
 			if ( trans.gettype() ==  TransferType.UNICAST_SEND_RECEIVER_SIDE )
 			    complete = oob.poolTransfer();
 			if ( trans.gettype() ==  TransferType.UNICAST_RECEIVE_RECEIVER_SIDE )
 			    complete = oob.poolTransfer();
 		    
 			//Transfer is finished
 			if (complete) {
 			    trans.setstatus(TransferStatus.COMPLETE);
 			}
 			//FIXME check for errors
 			if(oob.error()) {
 			    trans.setstatus(TransferStatus.INVALID);
 			}
 		    } catch (RemoteException re) {
 			//bof rien a faire			
 			break;
 		    } catch (OOBException oobe) {
 			//go in the state INVALID (should be ABORT ?)
 			
 			break;
 		    }
 
 		    break;
 
 		case TransferStatus.COMPLETE :
 		    //check the status
 		    //we stay in the complete status up to when we have been checked as complete.
 		    log.debug("COMPLETE");
 		    //The transfer ends when the sender is aware that the
 		    try {
 			oob = getOOBTransfer(trans);
 			if ( TransferType.isLocal(trans.gettype()) ) {
 			    dt.endTransfer(trans.getuid());
 			    trans.setstatus(TransferStatus.TODELETE);
 			    dt.setTransferStatus(trans.getuid(), 
 						 TransferStatus.TODELETE);
 			}
 		    } catch (Exception re) {
 			break;
 		    }
 		    break;
 		    
 		case TransferStatus.STALLED : 
 		    log.debug("STALLED");
 		    break;
  		case TransferStatus.TODELETE :
 		    //check the status
 		    log.debug("TODELETE");
 		    try {
 			//TODO DELETE TRANSFER FROM THE DATABASE
 			removeOOBTransfer(trans);
 		    } catch (OOBException oobe) {
 			break;
 		    }
 		    break;
 
 		default :
 		    log.debug("ERROR");
 		
 		}
 	    }
 	    tx.commit();
 	} finally {
 	    if (tx.isActive())
 		tx.rollback();
 	    pm.close();
 	}
 	long end=System.currentTimeMillis();	
 	perf.addSample(end - start);
     }
 
 
 
     //FIXME c pas top; mettre une limite au premier resultat retourne
     public boolean downloadComplete() {
 	PersistenceManager pm = DBInterfaceFactory.getPersistenceManagerFactory().getPersistenceManager();
 	Transaction tx=pm.currentTransaction();	
 	try {
 	    tx.begin();
 	    Query query = pm.newQuery(xtremweb.core.obj.dt.Transfer.class); 
 	    //				      "status != " + TransferStatus.TODELETE );
 	    //	    query.setUnique(true);
 	    Collection results = (Collection)query.execute();
 	    if (results==null) {
 		return true;
 	    } else {
 		Iterator iter = results.iterator();
 		while (iter.hasNext()) {
 		    Transfer trans = (Transfer) iter.next();
 		    log.debug("scanning transfer " + trans.getuid() + " " + trans.getdatauid() + trans.getstatus());
 		    if (trans.getstatus() != TransferStatus.TODELETE)
 			return false;
 		}
 	    }
 	    tx.commit();
 	} finally {
             if (tx.isActive())
                 tx.rollback();
             pm.close();
 	}
 	/*
 	for ( Object o:  oobTransfers.values()) {
 	    OOBTransfer trans = (OOBTransfer) o;
 	    if (data.equals(trans.getData().getuid()))
 
 	}
 	*/
 
 	return true;
     }
 
     /**
      *  <code>waitForAllData</code> waits for all transfers to complete.
      *
      */
     public void waitForAllData() {
 	while (!downloadComplete()) {
 	    try {
 		Thread.sleep(timeout);		
 	    } catch (Exception e) {};	 
 	    log.debug("Still in the barrier");
 	}	
     }
 
     /**
      *  <code>isTransferComplete</code> check if a transfer of a given data is complete.
      *
      * @param data a <code>Data</code> value
      * @return a <code>boolean</code> value
      */
     public boolean isTransferComplete(Data data) {
 	PersistenceManager pm = DBInterfaceFactory.getPersistenceManagerFactory().getPersistenceManager();
 	Transaction tx=pm.currentTransaction();	
 	boolean isComplete = true;	
 	try {
 	    tx.begin();
 	    Query query = pm.newQuery(xtremweb.core.obj.dt.Transfer.class, 
 				      "datauid == \"" + data.getuid() +"\"" ); 
 	    //				      "status != " + TransferStatus.TODELETE );
 	    //	    query.setUnique(true);
 	    Collection results = (Collection)query.execute();
 	    if (results==null) {
 		log.debug("pas de resultat");
 		return true;
 	    } else {
 		Iterator iter = results.iterator();
 		//	    isComplete = (query.execute() == null);
 		while (iter.hasNext()) {
 		    Transfer trans = (Transfer) iter.next();
 		    log.debug("scanning transfer " + trans.getuid() + " " + trans.getdatauid() + trans.getstatus());
 		    if (trans.getstatus() != TransferStatus.TODELETE)
 			return false;
 		}
 	    }
 	    tx.commit();
 	} finally {
             if (tx.isActive())
                 tx.rollback();
             pm.close();
 	}
 
 	return isComplete;
     }
 
     /**
      *  <code>waitFor</code> waits for a specific data to be transfered.
      *
      * @param data a <code>Data</code> value
      */
     public void waitFor(Data data) throws TransferManagerException {
 	while (!isTransferComplete(data)) {
 	    try {
 		Thread.sleep(timeout);		
 	    } catch (Exception e) {
 		throw new TransferManagerException();
 	    };	 
 	    log.debug("waitFor data " + data.getuid() );
 	}		
     }
 
     public void waitFor(DataCollection datacollection, String oob) throws TransferManagerException {
 	log.debug("begin waitFor!");
 	try {
 	    
 	    Data data=null;
 	    PersistenceManager pm = DBInterfaceFactory.getPersistenceManagerFactory().getPersistenceManager();
 
 	    Transaction tx=pm.currentTransaction();
 	    try {
 		tx.begin();
 
 		Extent e=pm.getExtent(DataChunk.class,true);
 		Iterator iter=e.iterator();
 	    
 		while (iter.hasNext()) {
 		    DataChunk datachunk = (DataChunk) iter.next();
 		    if (datachunk.getcollectionuid().equals(datacollection.getuid())){
 			Query query = pm.newQuery(xtremweb.core.obj.dc.Data.class,  "uid == \"" + datachunk.getdatauid() + "\"");
 			query.setUnique(true);
 			Data dataStored = (Data) query.execute();
 			data = (Data) pm.detachCopy(dataStored);
 			data.setoob(oob);
 			waitFor(data);
 			log.debug("Oh ha Transfer waitfor data uid="+data.getuid());
 		    }
 		}
            
 		tx.commit();
 	    } finally {
 		if (tx.isActive())
 		    tx.rollback();
 		pm.close();
 	    }	
 
 	} catch (Exception e) {
 	    throw new TransferManagerException();
 	}
 
     }
 
     //This a comparator for the test
     class OOBTransferOrder implements Comparator {
 	
 	public int compare(Object p1, Object p2) {
 	    String s1;
 	    String s2;
 	    if (p1 instanceof String)
 		s1 = (String) p1;
 	    else
 		s1=((OOBTransfer) p1).getTransfer().getuid();
 	    if (p2 instanceof String) 
 		s2 = (String) p2;
 	    else
 		s2=((OOBTransfer) p2).getTransfer().getuid();
 	    return  s1.compareTo(s2);
 	}
     }
 
 }
