 package xtremweb.serv.dc;
 
 /**
  * Callbackdc.java
  *
  *
  * Created: Thu Feb 23 14:04:25 2006
  *
  * @author <a href="mailto:">Gilles Fedak</a>
  * @version 1.0
  */
 import java.rmi.*;
 import xtremweb.core.com.idl.*;
 import xtremweb.core.conf.ConfigurationException;
 import xtremweb.core.conf.ConfigurationProperties;
 import xtremweb.core.iface.*;
 import xtremweb.core.obj.dc.*;
 import xtremweb.core.log.*;
 import xtremweb.dao.DaoFactory;
 import xtremweb.dao.data.DaoData;
 import xtremweb.dao.datachunck.DaoDataChunck;
 import xtremweb.dao.datacollection.DaoDataCollection;
 import xtremweb.dao.locator.DaoLocator;
 import xtremweb.serv.dc.ddc.*;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Vector;
 
 /**
  * This class implements the Interfacedc, is the catalog used to handle
  * MetaData in BitDew, this class store objects of type Data, Locator, 
  * DataChunk and DataCollection
  * @author Gilles Fedak
  */
 public class Callbackdc extends CallbackTemplate implements Interfacedc {
 
     /**
      * Logging factory
      */
     protected Logger log = LoggerFactory.getLogger("DC Service");
 
     /**
      * Distributed data catalog to handle replica management
      */
     protected DistributedDataCatalog ddc = null;
     private DaoData dao;
     /**
      * Callbackdc constructor
      */
     public Callbackdc() {
 	try {
 	    dao = (DaoData)DaoFactory.getInstance("xtremweb.dao.data.DaoData");
 	    Properties props = ConfigurationProperties.getProperties();
 	    boolean onddc = Boolean.parseBoolean(props.getProperty("xtremweb.serv.dc.ddc"));
 	    if(onddc)
 	    {	ddc = DistributedDataCatalogFactory.getDistributedDataCatalog();
 	    	ddc.start();
 	    }
 	} catch (DDCException ddce) {
 	    log.warn("unable to start a Distributed Data Catalog service");
 	    ddc = null;
 	} catch (ConfigurationException e) {
 	    e.printStackTrace();
 	}	
     } // Callbackobj constructor
 
     /**
      * Create and put a new Data in BitDew and return it, data encapsulates file
      * metainformation in a java class, and is the usual class manipulated in
      * BitDew.
      * 
      * @param name
      *            file name
      * @param checksum
      *            file checksum (if available)
      * @param size
      *            file size
      * @param type
      *            file type (binary, text)
      * @return the Data representing that file on BitDew.
      * @throws RemoteException
      */
     public Data createData(String name, String checksum, long size, int type)
 	    throws RemoteException {
 	Data data = new Data();
 	data.setname(name);
 	data.setchecksum(checksum);
 	data.setsize(size);
 	data.settype(type);
 	putData(data);
 	return data;
     }
 
     /**
      * Create and put an empty Data in bitdew.
      * 
      * @return The newly-empty data
      * @throws RemoteException
      *             if the RMI service throws an exception
      */
     public Data createData() throws RemoteException {
 	Data data = new Data();
 	putData(data);
 	return data;
     }
     
     /**
      * Given a md5 hashing, find the locally stored data having this md5
      * @param md5
      * @return a data whose hash is md5
      * @throws RemoteException if a problem occurs in the method invocation
      */
    public Data getDataFromMd5(String md5)
     {	try{
     		dao.beginTransaction();
     		Data d = dao.getDataFromMd5(md5);
     		dao.commitTransaction();
     		return d;
     	}finally{
     		if(dao.transactionIsActive())
     			dao.transactionRollback();	
     	} 	
     }
 
     /**
      * Put a data in the DBMS
      * 
      * @data the data to put
      * @return data uid
      */
     public String putData(Data data) throws RemoteException {
 	DaoData dao = (DaoData) DaoFactory
 		.getInstance("xtremweb.dao.data.DaoData");
 	dao.makePersistent(data, true);
 	return data.getuid();
     }
 
     /**
      * Get distributed data catalog entry point
      * 
      * @return distributed hash table entry point
      */
     public String getDDCEntryPoint() throws RemoteException {
 	if (ddc == null)
 	    return "null";
 	else {
 	    try {
 		return ddc.entryPoint();
 	    } catch (DDCException ddce) {
 		log.debug("" + ddce);
 	    }
 	}
 	return "null";
     }
 
     /**
      * Get a data from its uid
      * 
      * @uid data uid
      * @return the data whose uid is the parameter
      */
     public Data getData(String uid) throws RemoteException {
 
     	Data dataStored ;
     	DaoData mydao = new DaoData();
 	try {
 	    mydao.beginTransaction();
 	  
 	    dataStored = (Data) mydao.getByUid(Data.class, uid);
 	 
 		
 	    mydao.commitTransaction();
 	} finally {
 	    if (dao.transactionIsActive())
 		dao.transactionRollback();
 	}
 
 	return dataStored;
     }
 
     /**
      * This method is not being used in the API and according to my research is
      * not actually being performed as all is being marked as TO_DELETE
      */
     public void deleteData(Data data) throws RemoteException {
 	/*PersistenceManager pm = DBInterfaceFactory
 		.getPersistenceManagerFactory().getPersistenceManager();
 
 	Transaction tx = pm.currentTransaction();
 	try {
 	    tx.begin();
 	    // Object id = pm.getObjectId(data);
 	    // Data obj = pm.getObjectById(id);
 	    pm.makePersistent(data);
 	    pm.deletePersistent(data);
 
 	    // Data todelete = pm.getObjectById(uid); // Retrieves the object to
 	    // delete
 	    // pm.deletePersistent(todelete);
 	    tx.commit();
 	} finally {
 	    if (tx.isActive())
 		tx.rollback();
 	    pm.close();
 	}*/
     }
 
     /**
      * Deletes do not seem to be performed
      * 
      * @param uid
      * @throws RemoteException
      */
     public void deleteData(String uid) throws RemoteException {
 	// deleteData(data.getuid());
     }
 
     /**
      * Useful method to explore the data that has been introduced
      */
     public void browse() {
 	try {
 	    dao.beginTransaction();
 	    Collection e = dao.getAll(Data.class);
 	    Iterator iter = e.iterator();
 	    while (iter.hasNext()) {
 		Data data = (Data) iter.next();
 		System.out.println(DataUtil.toString(data));
 	    }
 	    dao.commitTransaction();
 	} finally {
 	    if (dao.transactionIsActive())
 		dao.transactionRollback();
 	
 	}
     }
 
     /**
      * Put a locator on the DBMS
      * 
      * @param locator
      *            the locator to put
      */
     public void putLocator(Locator locator) throws RemoteException {
 	DaoLocator dao = (DaoLocator) DaoFactory
 		.getInstance("xtremweb.dao.locator.DaoLocator");
 	dao.makePersistent(locator, true);
     }
 
     /**
      * Not yet implemented (maybe to delete ?)
      */
     public void setDataStatus(String uid, int status) throws RemoteException {
     }
 
     /**
      * Gets the locator associated with a data
      * 
      * @param uid
      *            , the data uid
      * @return the locator whose id is the parameter
      */
     public Locator getLocatorByDataUID(String uid) throws RemoteException {
 	Locator ret = null;
 	DaoData mydao = new DaoData();
 	try {
 	    mydao.beginTransaction();
 
 	    Collection e = mydao.getAll(Locator.class);
 	    // Query q=pm.newQuery(e, "datauid == " + uid);
 	    Iterator iter = e.iterator();
 	    while (iter.hasNext()) {
 		ret = (Locator) iter.next();
 		if (ret.getdatauid().equals(uid)) {
 		    log.debug("getLocatorByDataUID found one locator : "
 			    + ret.getuid() + ":" + ret.getpublish());
 		    if (ret.getpublish())
 		    	return ret;
 		}
 	    }
 	    mydao.commitTransaction();
 	} finally {
 	    if (mydao.transactionIsActive())
 		mydao.transactionRollback();
 	}
 
 	return null;
 
     }
 
     /**
      * Put a data collection in the DBMS
      * 
      * @param datacollection
      *            the collection to insert
      * @return the id generated for the newly created data collection
      */
     public String putDataCollection(DataCollection datacollection)
 	    throws RemoteException {
 	DaoDataCollection dao = (DaoDataCollection) DaoFactory
 		.getInstance("xtremweb.dao.datacollection.DaoDataCollection");
 	dao.makePersistent(datacollection, true);
 	return datacollection.getuid();
     }
 
     /**
      * This method seems to be useless
      */
     public Vector getDataInCollection(String datacollectionuid, int indexbegin,
 	    int indexend) throws RemoteException {
 	return new Vector();
     }
 
     /**
      * This method seems to be useless
      */
     public DataCollection getDataCollectionByName(String name)
 	    throws RemoteException {
 	return new DataCollection();
     }
 
     /**
      * This method is not being called from the API, maybe we have to erase it
      */
     public DataCollection getDataCollection(String uid) throws RemoteException {
 	DataCollection dataStored = null;
 	
 	try {
 	    dao.beginTransaction();
 	   dataStored = (DataCollection) dao.getByUid(
 		    DataCollection.class, uid);
 	    dao.commitTransaction();
 	} finally {
 	    if (dao.transactionIsActive())
 		dao.transactionRollback();
 	  
 	}
 	return dataStored;
     }
 
     /**
      * Idem. the method have not been called from the API
      */
     public void deleteDataCollection(DataCollection datacollection)
 	    throws RemoteException {
 	/*PersistenceManager pm = DBInterfaceFactory
 		.getPersistenceManagerFactory().getPersistenceManager();
 
 	Transaction tx = pm.currentTransaction();
 	try {
 	    tx.begin();
 
 	    pm.makePersistent(datacollection);
 	    pm.deletePersistent(datacollection);
 
 	    tx.commit();
 	} finally {
 	    if (tx.isActive())
 		tx.rollback();
 	    pm.close();
 	}*/
     }
 
     /**
      * Put a DataChunk on the DBMS
      * 
      * @param datachunk
      *            the datachunk to insert
      * @return the id of the newly created datachunk
      */
     public String putDataChunk(DataChunk datachunk) throws RemoteException {
 	DaoDataChunck dao = (DaoDataChunck) DaoFactory
 		.getInstance("xtremweb.dao.datachunck.DaoDataChunck");
 	dao.makePersistent(datachunk, true);
 	return datachunk.getuid();
     }
 
     /**
      * get a data chunk according to uid
      * 
      * @param the
      *            datachunk uid
      */
     public DataChunk getDataChunk(String uid) throws RemoteException {
 	System.out.println("the uid is " + uid);
 	DataChunk dataStored = null;
 	
 	try {
 	    dao.beginTransaction();
 	    dataStored = (DataChunk) dao.getByUid(
 		    xtremweb.core.obj.dc.DataChunk.class, uid);
 	    System.out.println("dataStored is " + dataStored);
 	    dao.commitTransaction();
 	} finally {
 	    if (dao.transactionIsActive())
 		dao.transactionRollback();
 	}
 
 	return dataStored;
     }
 
     /**
      * It seems we are not deleting things in BD
      */
     public void deleteDataChunk(DataChunk datachunk) throws RemoteException {
 	/*PersistenceManager pm = DBInterfaceFactory
 		.getPersistenceManagerFactory().getPersistenceManager();
 
 	Transaction tx = pm.currentTransaction();
 	try {
 	    tx.begin();
 
 	    pm.makePersistent(datachunk);
 	    pm.deletePersistent(datachunk);
 
 	    tx.commit();
 	} finally {
 	    if (tx.isActive())
 		tx.rollback();
 	    pm.close();
 	}*/
     }
 
     /**
      * Get all data related to a collection uid
      * 
      * @param datacollectionuid
      *            the data collection uid
      * @return a vector containing all the collection data
      */
     public Vector getAllDataInCollection(String datacollectionuid)
 	    throws RemoteException {
 	Vector v = new Vector();	
 	try {
 	    dao.beginTransaction();
 	    Collection e = dao.getAll(DataChunk.class);
 	    Iterator iter = e.iterator();
 
 	    while (iter.hasNext()) {
 		DataChunk datachunk = (DataChunk) iter.next();
 		if (datachunk.getcollectionuid().equals(datacollectionuid)) {
 		    Data dataStored = (Data) dao.getByUid(Data.class,
 			    datachunk.getdatauid());
 		    
 		    v.addElement(dataStored);
 		}
 	    }
 	    dao.commitTransaction();
 	} finally {
 	    if (dao.transactionIsActive())
 		dao.transactionRollback();	  
 	}
 	return v;
     }
 
     /**
      * This method is never called from the API layer
      * 
      */
     public String getDataUidByName(String name) {
 	String uid = "";
 	Data dataStored = null;
 	try {
 	    dao.beginTransaction();
 	    dataStored = (Data) dao.getByName(Data.class, name);
 	    uid = dataStored.getuid();
 	    dao.commitTransaction();
 	} finally {
 	    if (dao.transactionIsActive())
 		dao.transactionRollback();
 	}
 	return uid;
     }
 
     // //////////////////////////////////////////////////////////////
 
     public static void main(String[] args) {
 	try {
 	    Callbackdc dc = new Callbackdc();
 	    Data data;
 	    String uid = null;
 
 	    // data creation
 	    File file = new File("Makefile");
 	    uid = (dc.createData(file.getName(), "CHCKSUM", file.length(), 0))
 		    .getuid();
 	    System.out.println("Data created : " + uid);
 
 	    // data creation from a file
 	    data = DataUtil.fileToData(file);
 	    System.out.println("Data created : " + DataUtil.toString(data));
 	    dc.putData(data);
 
 	    // browse data
 	    dc.browse();
 
 	    // getting data
 	    Data data_read = dc.getData(data.getuid());
 	    System.out.println("readind data UID=" + data.getuid() + " || "
 		    + DataUtil.toString(data_read));
 
 	    // deleting data
 	    Data data_delete = data;
 	    System.out.println("deleting data UID=" + data_delete.getuid());
 	    dc.deleteData(data);
 	    dc.browse();
 
 	} catch (RemoteException re) {
 	    ;
 	}
     }
 
 } // Callbackobj
