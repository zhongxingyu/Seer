 package xtremweb.serv.ds;
 
 import xtremweb.core.util.SortedVector;
 
 import java.util.*;
 
 import xtremweb.serv.ds.*;
 import xtremweb.serv.dc.*;
 import xtremweb.core.iface.*;
 import xtremweb.core.log.*;
 import xtremweb.core.db.*;
 import xtremweb.core.com.com.*;
 import xtremweb.core.com.idl.*;
 import xtremweb.core.obj.dc.Data;
 import xtremweb.core.obj.ds.Attribute;
 import xtremweb.core.obj.ds.Host;
 import xtremweb.core.obj.dr.Protocol;
 import xtremweb.core.obj.dc.Locator;
 import xtremweb.core.obj.dt.Transfer;
 
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
  * <code>DataScheduler</code> implements the scheduling of data
  *
  * @author <a href="mailto:fedak@lri.fr">Gilles Fedak</a>
  * @version 1.0
  */
 
 public class DataScheduler {
 
     private DataQueue dataCache;
     private Vector toSchedule;
 
     private int numberOfDataToSchedule = 1;
 
     protected Logger log = LoggerFactory.getLogger("Data Scheduler");
 
     /** time between two periodic activities (in milli seconds) */
     protected int timeout = 500; 
 
     private Timer timer;
 
 
     /**
      * Creates a new <code>DataScheduler</code> instance.
      */
     public DataScheduler() {
 	dataCache = new DataQueue();
     }
 
     public void setNumberOfDataToSchedule(int num){
 	this.numberOfDataToSchedule = num;
     }
     
     public int getNumberOfDataToSchedule(){
 	return this.numberOfDataToSchedule;
     }
 
     public void addDataAttribute(Data data, Attribute attr) {
 	dataCache.addElement( new CacheEntry(data, attr) );
     }
 
     public SortedVector getDataCache(){
 	return dataCache;
     }
 
     public void reset() {
 	dataCache.clear();
     }
 
     public void resetOwners() {
 	Iterator iter=dataCache.iterator();	
 	while (iter.hasNext()) {
 	    CacheEntry ce = (CacheEntry) iter.next();
 	    ce.resetOwners();
 	}
     }
 
     /*fill the cache 
     public void checkDataFuck() {
 	PersistenceManagerFactory pmf = DBInterfaceFactory.getPersistenceManagerFactory();
 	PersistenceManager pm = pmf.getPersistenceManager();
 	Transaction tx=pm.currentTransaction();
 
 	try { 
 	    tx.begin();
             Extent e=pm.getExtent(Data.class,true);//DataStatus.TODELETE
             Iterator iter=e.iterator();
 	    
             while (iter.hasNext()) {
 		Data data = (Data) iter.next();
 		//if it's int the 
 		//		log.debug("look for data in the cache " + data.getuid() );
 		if (dataCache.search(data.getuid()) == -1) {
 		    //look for the corresponding attribute
 		    Query query = pm.newQuery(xtremweb.core.obj.ds.Attribute.class,  "uid == \"" + data.getattruid() + "\"");
 		    query.setUnique(true);
 		    Attribute attribute = (Attribute) query.execute();
 		    log.debug("adding data in the cache " + data.getuid() + AttributeUtil.toString(attribute));
 		    CacheEntry ce = new CacheEntry(data, attribute);
 		    dataCache.addElement(ce);
 		}
 	    }	
 	} catch (Exception e) {
 	    log.debug("exception occured when running DataScheduler " + e);
 	} finally {
 	    if (tx.isActive())
 		tx.rollback();
 	    pm.close();
 	}
     }
     */
     public void updateAttribute(Attribute attr) {
 	Iterator iter=dataCache.iterator();	
 	while (iter.hasNext()) {
 	    CacheEntry ce = (CacheEntry) iter.next();
 	    if (attr.getuid().equals(ce.getAttributeUid()))
 		ce.setAttribute(attr);
 	}
     }
 
     public void associateDataAttributeHost(Data data, Attribute attr, Host host) {
 	CacheEntry ce;
 	int idx=dataCache.search(data.getuid());
 	if ( idx == -1) {
 	     ce = new CacheEntry(data, attr);
	     ce.updateOwner(host);
 	    dataCache.addElement(ce);
 	    return;
 	} 
 	ce= (CacheEntry) dataCache.elementAt(idx); 
 	ce.updateOwner(host);
     }
 
     public void associateDataHost(Data data, Host host) {
 	CacheEntry ce;
 	int idx=dataCache.search(data.getuid());
 	if ( idx == -1) 
 	    return;
 	ce= (CacheEntry) dataCache.elementAt(idx); 
 	ce.updateOwner(host);
     }
 
     public void associateDataAttribute(Data data, Attribute attr) {
 	CacheEntry ce;
 	int idx=dataCache.search(data.getuid());
 	if ( idx == -1) {
 	    ce = new CacheEntry(data, attr);
 	    dataCache.addElement(ce);
 	    return;
 	} 
 	ce= (CacheEntry) dataCache.elementAt(idx); 
 	ce.setAttribute(attr);
     }
 
 
     public synchronized Vector removeDataFromCache(Host host, Vector uidslist) {
 	Vector result = new Vector();
 
 	//we scan the uids. If the data should be kept by the node,
 	//then the uids is put in the cache
 	//If the data is not put in the result vector then it will
 	//be destroyed locally by the node
 	for (int i=0; i<uidslist.size(); i++) {
 	    int idx = dataCache.search(uidslist.elementAt(i));
 	    // the data is not present in the cache
 	    if (idx==-1) 
 		continue;
 	    CacheEntry ce = (CacheEntry) dataCache.elementAt(idx);
 	    Data data = ce.getData();
 	    Attribute attr = ce.getAttribute();
 
 	    //perform check according to data status 
 	    if ( data.getstatus() == DataStatus.TODELETE )
 		continue;
 
 	    //the data is in the cache
 	    // we first check for a valid lifetime
 
 	    // if the absolute lifetime exceeds now, the data is deleted
 	    if ( AttributeType.isAttributeTypeSet( attr, AttributeType.LFTABS )
 		 && ( attr.getlftabs() < System.currentTimeMillis())) {
 		data.setstatus(DataStatus.TODELETE);
 		continue;
 	    }
 	    //now check for relative data
 	    if ( AttributeType.isAttributeTypeSet( attr, AttributeType.LFTREL )) {
 		int idxrel = dataCache.search(attr.getlftrel());
 		//if the relative data is not in the cache
 		if (idxrel == -1) {
 		    data.setstatus(DataStatus.TODELETE);
 		    continue;
 		}
 		Data datarel = ((CacheEntry) dataCache.elementAt(idxrel)).getData();
 		//if the relative data  has status TODELETE, the data is deleted
 		if ( datarel.getstatus() == DataStatus.TODELETE ) {
 		    data.setstatus(DataStatus.TODELETE);
 		    continue;
 		}
 	    }
 	    //update time out information for owner
 	    if ( AttributeType.isAttributeTypeSet( attr, AttributeType.FT )) 
 		ce.updateOwner(host);
 	    
 	    result.addElement(ce.getData().getuid());
 	}
 	return result;
     }
 
     public synchronized Vector getNewDataFromCache(Host host, Vector uidslist) {
 	Vector result = new Vector();
 	
 	//check for data which are not scheduled yet
 	for (int i=0; i<dataCache.size(); i++) {
 	    CacheEntry ce = (CacheEntry) dataCache.elementAt(i);
 	    Data data = ce.getData();
 	    Attribute attr = ce.getAttribute();
 
 	    //this data is delete
 	    if (data.getstatus() == DataStatus.TODELETE)
 		continue;
 	    
 	    //this data is already in the worker cache
 	    if (uidslist.contains(data.getuid()))
 		continue;
 
 	    boolean addElement = false;
 
 	    //make some room by cleaning late owners
 	    if ( AttributeType.isAttributeTypeSet( attr, AttributeType.FT )) 
 		ce.updateOwners();
 
 	    //check according to the affinity (here strict affinity)
 	    if  (AttributeType.isAttributeTypeSet( attr, AttributeType.AFFINITY )) {
        	        //if the data in the scheduler list has affinity dependency with one of 
 		//the data in the worker cache, the data is scheduled
 		String obj=attr.getaffinity();
 		int idx=uidslist.lastIndexOf(obj);
 		if (idx!=-1){ //can find it in worker cache
 		    int jdx = dataCache.search(uidslist.elementAt(idx));
 		    //and the data is also present in the scheduler cache, at the same time
 		    if (jdx!=-1){
 			CacheEntry ce1=(CacheEntry)dataCache.elementAt(jdx);
 			Data d1 = ce1.getData();
 			if (d1.getstatus()!=DataStatus.TODELETE)  //not to delete
 			     addElement = true; 
 		    }
 		}
 	    //here affinity is strict , it overides the replicat condition
 	    } 
 	    int replicat = 1;
 	    //check according to the replicat attribute
 	    //the default replicat attribute is 1		
 	    if  (AttributeType.isAttributeTypeSet( attr, AttributeType.REPLICAT ))
 		replicat = attr.getreplicat();
 	    if (( ce.getOwnersNumber() < replicat) || (replicat==-1)) 
 		addElement = true;    
 	    
 	    if (addElement){
 		//check distrib
 		int distrib = -1;  //default value  infinite
 		int count = 0;
 		if  (AttributeType.isAttributeTypeSet( attr, AttributeType.DISTRIB )){	    
 		    //to check how many data share the same attr on this host
 		    //count the numbers of data which share the same attr with Data data
 		    for(int p=0; p<uidslist.size(); p++){
 			int pdx = dataCache.search(uidslist.elementAt(p));
 			if (pdx!=-1){
 			    CacheEntry ce2=(CacheEntry)dataCache.elementAt(pdx);
 			    Attribute attr2 = ce2.getAttribute();
 			    if (attr2.getuid().equals(attr.getuid()))
 				count = count +1;
 			}
 		    }
 		    distrib = attr.getdistrib();
 		}
 		if ((count < distrib) || (distrib==-1)) {
 		    //finnally adds this new data 
 		    result.addElement(data.getuid());
 		    ce.setOwner(host);
 		}
 	    }
 
 	    if (result.size() == numberOfDataToSchedule)     //stop condition     one schedule
 		break;
 	}
 	    
 	result.addAll(uidslist);
 	return result;
     }
 
     //changer le nom qui est mauvais
     public synchronized Vector getData(Host host, Vector uidslist) {
 	Vector result = new Vector();
 	//	String ownerUid
 	result = removeDataFromCache( host, uidslist );
 	result = getNewDataFromCache( host, result );
 	return result;
     }
 
 
     public synchronized void removeData(Data data) {
 	int pdx = dataCache.search(data.getuid());
 	if (pdx!=-1) {
 	    CacheEntry ce = (CacheEntry) dataCache.elementAt(pdx);	
 	    ce.getData().setstatus(DataStatus.TODELETE);
 	}
     }
 
 
     /*
      * <code>start</code> launches periodic Scheduling
      
     public void start() { 
 	log.debug("Starting Data Scheduler");
 	if (timer==null) timer=new Timer(); 
 	timer.schedule(new TimerTask() { 
 		public void run() { 
 		    checkData();
 		} 
 	    } , 0, timeout ); 
     }
     */
     public  void setAliveTimeout(long t) {
 	Owner.setAliveTimeout(t);
     }
 
 }
