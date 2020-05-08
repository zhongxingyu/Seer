 package xtremweb.api.activedata;
 
 /**
  * Describe class ActiveData here.
  *
  *
  * Created: Fri Aug 17 14:27:42 2007
  *
  * @author <a href="mailto:fedak@lri7-234.lri.fr">Gilles Fedak</a>
  * @version 1.0
  */
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
 import xtremweb.core.obj.dr.Protocol;
 import xtremweb.core.obj.dc.Locator;
 import xtremweb.core.obj.dt.Transfer;
 
 import xtremweb.core.obj.ds.Attribute;
 import xtremweb.core.obj.ds.Host;
 
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
 
 
 public class ActiveData {
 
     private InterfaceRMIdc cdc;
     private InterfaceRMIds cds;
     private Vector cache;
 
     private Host host = ComWorld.getHost();
 
     private Vector<ActiveDataCallback> callbacks;
     //FIXME
     //hash attributes from attr uid to attribute 
     private HashMap<String,Attribute> attributes;
 
     /** time between two periodic activities (in milli seconds) */
     protected int timeout = 10000; 
 
     private Timer timer;
 
     private boolean isActive=false;
 
     DBInterface dbi = DBInterfaceFactory.getDBInterface();
 
     protected Logger log = LoggerFactory.getLogger("Active Data");
     /**
      * Creates a new <code>ActiveData</code> instance.
      *
      */
     public ActiveData(InterfaceRMIdc dc , InterfaceRMIds ds) {
 	cdc = dc;
 	cds = ds;
 	init();
     }
 
     public ActiveData(Vector comms) {
 
 	for (Object o : comms) {
 	    if (o instanceof InterfaceRMIdc) cdc = (InterfaceRMIdc) o;
 	    if (o instanceof InterfaceRMIds) cds = (InterfaceRMIds) o;
 	}
 	init();
     }
 
     public void init() {
 	cache = new Vector();	
 	callbacks = new Vector<ActiveDataCallback>();
 	attributes = new HashMap<String,Attribute>();
     }
 
     public boolean isActive() {
 	return isActive;
     }
 
     /**
      * <code>stop</code> stops periodic Active Data Engine
      */
     public void stop() {
 	log.debug("Stopping AD Engine");
 	timer.cancel();
 	isActive=false;
     }
 
    /**
     * <code>start</code> launches periodic Active Data engine
     */
    public void start() { 
 	log.debug("Starting AD Engine");
	if (timer==null) timer=new Timer(true); 
 	timer.schedule(new TimerTask() { 
 		public void run() { 
 		    checkData();
 		} 
	    } ,0, timeout ); 
 	isActive=true;
     }
 
     protected void checkData() {
 
 	Vector datasync = new Vector();
 	PersistenceManagerFactory pmf = DBInterfaceFactory.getPersistenceManagerFactory();
 	PersistenceManager pm = pmf.getPersistenceManager();
 	Transaction tx=pm.currentTransaction();
 
 	try {
 	    tx.begin();
             Extent e=pm.getExtent(Data.class,true);
             Iterator iter=e.iterator();
 
             while (iter.hasNext()) {
 		Data data = (Data) iter.next();
 		//		log.debug("checking data " + data.getuid() + " : " + DataStatus.toString(data.getstatus()));
 		datasync.add(data.getuid());
 	    }
 
 	    Vector newdatauid = cds.sync(host, datasync);
 	    String datauids = "";
 
 	    //check for data to delete
 	    for (int i=0; i<newdatauid.size(); i++)
 		datauids += "uid != \"" + ((String) newdatauid.elementAt(i)) + "\" && "; 
 	    //	    if (newdatauid.size()>0) 
 	    //	datauids = datauids.substring(0, datauids.length()-4);
 	    //	    log.debug("going to check for deletion : " + datauids);
 
 
 	    Query query = pm.newQuery(xtremweb.core.obj.dc.Data.class, datauids + "  status != " + DataStatus.TODELETE );		
 	    Collection result = (Collection) query.execute();	       
 	    iter=result.iterator();
 	    String toDelete = "";
 
             while (iter.hasNext()) {
 		Data data = (Data) iter.next();
 		toDelete+=data.getuid() + " ";
 		data.setstatus(DataStatus.TODELETE);
 
 		//add attribute to attruid
 		Attribute attr = attributes.get(data.getattruid());
 
 		//FIXME do we really need attributes here ?
 		if ((attr == null) && (data.getattruid() != null)) {
 		    attr = cds.getAttributeByUid(data.getattruid());
 		    if (attr == null)
 			log.warn("BIZARRE");
 		    else
 			attributes.put(attr.getuid(),attr);
 		}
 
 		for(ActiveDataCallback callback : callbacks) {
 		    log.debug ("calling callback Schedule on data data  [d " + data.getuid() + ":a " + attr.getuid()  + "]"  );
 		    callback.onDataDeleted(data,attr);
 		}
 
 	    }
 	    if (!toDelete.equals(""))
 		log.debug("uids deleted " + toDelete);
 	    //FIXME get better performances
 	    //check for data to download
 	    for (int i=0; i<newdatauid.size(); i++) {
 		String uid =  ((String) newdatauid.elementAt(i));
 		query = pm.newQuery(xtremweb.core.obj.dc.Data.class,  "uid == \"" + uid + "\" && status != " + DataStatus.TODELETE  );
 		query.setUnique(true);
 		Data d = (Data) query.execute();
 		if (d==null) {
 		    log.debug ("getting a new data  " + uid);
 		    //contact dc to get complete information
 		    d = cdc.getData(uid);
 
 		    pm.makePersistent(d);
 		    //add attribute to attruid
 		    Attribute attr = attributes.get(d.getattruid());
 		    if (attr == null) {
 			attr = cds.getAttributeByUid(d.getattruid());
 			attributes.put(attr.getuid(),attr);
 		    }
 		    for(ActiveDataCallback callback : callbacks) {
 			log.debug ("calling callback Schedule on data data  [d " + d.getuid() + ":a " + attr.getuid()  + "]"  );
 			callback.onDataScheduled(d,attr);
 		    }
 		} else {
 		    log.debug (" d " + d.getuid() + " is present ");
 		}
 	    }
 	    tx.commit();
 	} catch (Exception e) {
 	    log.debug("exception occured when running active data " + e);
 	} finally {
 	    if (tx.isActive())
 		tx.rollback();
 	    pm.close();
 	}
     }
 
     public void registerActiveDataCallback(ActiveDataCallback callback) {
 	callbacks.add(callback);
     }
 
     public void registerActiveDataCallback(ActiveDataCallback callback, Attribute attr) {
 
     }
 
     private void fixoob( Data data, Attribute attr)  throws ActiveDataException {
 	try {
 	    if (attr.getoob()==null) return;
 	    if (!attr.getoob().equals(data.getoob())) {
 		data.setoob(attr.getoob());
 		cdc.putData(data);
 	    }
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	    throw new ActiveDataException();
 	}
     }
 
     public void schedule( Data data, Attribute attr)  throws ActiveDataException {
 	try {
 	    fixoob(data, attr);
 	    
 	    data.setattruid(attr.getuid());
 	    cdc.putData(data);
 	    cds.associateDataAttribute(data, attr);
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	    throw new ActiveDataException();
 	}
     }
 
     public void scheduleAndPin(Data data,  Attribute attr, Host host)  throws ActiveDataException {
 	try {
 	    fixoob(data, attr);
 	    data.setattruid(attr.getuid());
 	    cdc.putData(data);
 	    cds.associateDataAttributeHost(data, attr, host);
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	    throw new ActiveDataException();
 	}
     }
 
 
     public void pin(Data data,  Host host)  throws ActiveDataException  {
 	try {
 	    cds.associateDataHost(data, host);
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	    throw new ActiveDataException();
 	}
     }
 
 
     public Attribute createAttribute(String def)   throws ActiveDataException {
 	Attribute attr = AttributeUtil.parseAttribute(def);
 	DBInterfaceFactory.getDBInterface().makePersistent(attr);
 	return registerAttribute(attr);
     }
 
     public Attribute registerAttribute(Attribute attr)  throws ActiveDataException {
 	try {
 	    Attribute _attr = cds.registerAttribute(attr);
 	    return _attr;
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	}
 	throw new ActiveDataException();
     }
 
     public void settimeout(int ms) {
 	timeout = ms;
     }
 
 }
