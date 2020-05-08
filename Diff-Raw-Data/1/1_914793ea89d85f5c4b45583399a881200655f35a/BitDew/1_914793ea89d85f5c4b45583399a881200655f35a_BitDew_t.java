 package xtremweb.api.bitdew;
 
 import xtremweb.serv.dc.*;
 import xtremweb.core.iface.*;
 import xtremweb.core.log.*;
 import xtremweb.core.db.*;
 import xtremweb.core.com.com.*;
 import xtremweb.core.com.idl.*;
 import xtremweb.core.obj.dc.Data;
 import xtremweb.core.obj.dc.DataCollection;
 import xtremweb.core.obj.dc.DataChunk;
 import xtremweb.core.obj.dr.Protocol;
 import xtremweb.core.obj.dc.Locator;
 import xtremweb.core.obj.dt.Transfer;
 import xtremweb.core.obj.ds.Attribute;
 import xtremweb.core.util.filesplit.*;
 import xtremweb.serv.dt.*;
 import xtremweb.serv.dt.ftp.*;
 import xtremweb.serv.dc.ddc.*;
 import xtremweb.serv.dc.*;
 import xtremweb.serv.ds.*;
 
 import xtremweb.api.transman.*;
 
 import java.io.*;
 import java.rmi.RemoteException;
 import java.util.Vector;
 
 import xtremweb.core.util.filesplit.*;
 import xtremweb.core.util.uri.*;
 
 /**
  *  <code>BitDew</code> programming interface.
  *
  * @author <a href="mailto:Gilles.Fedak@inria.fr">Gilles Fedak</a>
  * @version 1.0
  */
 public class BitDew {
 
     private static Logger log = LoggerFactory.getLogger(BitDew.class);
     
     private InterfaceRMIdc idc;
     private InterfaceRMIdr idr;
     private InterfaceRMIdt idt;
     private InterfaceRMIds ids;
 
     private DistributedDataCatalog ddc = null;
     private String myHost="test_that_dude";
 
     private long splittime;
     private long creattime;
 
     /**
      * Creates a new <code>BitDew</code> instance.
      *
      * @param comms a <code>Vector</code> value
      */
     public BitDew(Vector comms) {
 
 	for (Object o : comms) {
 	    if (o instanceof InterfaceRMIdc) idc = (InterfaceRMIdc) o;
 	    if (o instanceof InterfaceRMIdr) idr = (InterfaceRMIdr) o;
 	    if (o instanceof InterfaceRMIdt) idt = (InterfaceRMIdt) o;
 	    if (o instanceof InterfaceRMIds) ids = (InterfaceRMIds) o;
 	}
 	init();
     }
 
     /**
      * Creates a new <code>BitDew</code> instance.
      *
      * @param cdc an <code>InterfaceRMIdc</code> value
      * @param cdr an <code>InterfaceRMIdr</code> value
      * @param cdt an <code>InterfaceRMIdt</code> value
      * @param cds an <code>InterfaceRMIds</code> value
      */
     public BitDew(InterfaceRMIdc cdc, InterfaceRMIdr cdr, InterfaceRMIdt cdt, InterfaceRMIds cds) {
 	idc = cdc;
 	idr = cdr;
 	idt = cdt;
 	ids = cds;
 
 	init();
     } // BitDew constructor
 
 
     private void init() {
 
 	try {
 	    ddc = DistributedDataCatalogFactory.getDistributedDataCatalog();
 	    String entryPoint = idc.getDDCEntryPoint();
 	    if (entryPoint != null) {
 		ddc.join(entryPoint);
 		log.info("Started DHT service for distributed data catalog [entryPoint:" + entryPoint + "]");
 	    }
 	} catch (Exception ddce) {
 	    log.warn("unable to start a Distributed Data Catalog service");
 	    ddc = null;		
 	}
 	//	TransferManagerFactory.init(idr, idt);
 	
     }
 
     /**
      * <code>createData</code> creates Data.
      *
      * @return a <code>Data</code> value
      * @exception BitDewException if an error occurs
      */
     public Data createData() throws BitDewException {
 	try {
 	    Data data = new Data();
 	    idc.putData(data);  
 	    return data;
 
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	}
 	throw new BitDewException();
     }
 
     /**
      * <code>createData</code> creates Data with its name set as specified .
      *
      * @param name a <code>String</code> value
      * @return a <code>Data</code> value
      * @exception BitDewException if an error occurs
      */
     public Data createData(String name) throws BitDewException {
 	try {
 	    Data data = new Data();
 	    data.setname(name);
 	    //DBInterfaceFactory.getDBInterface().makePersistent(data);
 	    idc.putData(data);  
 	    return data;
 
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	}
 	throw new BitDewException();
     }
     
     public Data createData(String name,String protocol,long size, String checksum)throws BitDewException
     {
 	try {
 	    Data data = new Data();
 	    data.setname(name);
 	    data.setoob(protocol);
 	    data.setsize(size);
 	    data.setchecksum(checksum);
 	    DBInterfaceFactory.getDBInterface().makePersistent(data);
 	    idc.putData(data);  
 	    return data;
 
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	}
 	throw new BitDewException();
     }
     /**
      * <code>createData</code> creates Data.
      *
      * @param name a <code>String</code> value
      * @param protocol a <code>String</code> value
      * @param size an <code>int</code> value
      * @return a <code>Data</code> value
      * @exception BitDewException if an error occurs
      */
     //TODO size is originally declared as long and here is int
     public Data createData(String name, String protocol, int size)  throws BitDewException {
 	try {
 	    Data data = new Data();
 	    data.setname(name);
 	    data.setoob(protocol);
 	    data.setsize(size);
 	    DBInterfaceFactory.getDBInterface().makePersistent(data);
 	    idc.putData(data);  
 	    return data;
 
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	}
 	throw new BitDewException();
     } 
 
 
     /**
      * <code>createData</code> creates Data from file.
      *
      * @param file a <code>File</code> value
      * @return a <code>Data</code> value
      * @exception BitDewException if an error occurs
      */
     public Data createData(File file) throws BitDewException {
 	Data data = DataUtil.fileToData(file);
 
 	try {
 	    DBInterfaceFactory.getDBInterface().makePersistent(data);
 	    idc.putData(data);
 	    log.debug ("uid = " + DataUtil.toString(data));
 	    return data;
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	} catch (Exception e) {
 	    log.debug("Error creating data " + e);
 	}
 	throw new BitDewException();
     }
 
     /**
      * <code>associateDataLocator</code> inserts a data in the catalog and then associates a locator to a data. Finally
      * it returns the associated OOBTransfer
      * @param d the data
      * @param lo the locator
      * @return OOBTransfer the transfer to get this data
      * @throws BitDewException if a problem occurs
      */
     public OOBTransfer associateDataLocator(Data d,Locator lo) throws BitDewException
     {
 	DBInterfaceFactory.getDBInterface().makePersistent(d);
 	try {
 	    idc.putData(d);
 	    return put(d,lo);
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	    throw new BitDewException();
 	}
     }
     /**
      *  <code>updateData</code> updates the data fields (name, size, checksum) 
      * with the file characteristics and put the file in the data slot.
      *
      * @param data a <code>Data</code> value
      * @param file a <code>File</code> value
      * @exception BitDewException if an error occurs
      */
     public void updateData(Data data, File file) throws BitDewException {
 	//set the new file value
 	Data tmp =  DataUtil.fileToData(file);
 	data.setchecksum(tmp.getchecksum());
 	data.setsize(tmp.getsize());
 	data.setname(tmp.getname());
 	DBInterfaceFactory.getDBInterface().makePersistent(data);
 	putData(data);
     }
     
     /**
      * This method saves a data locally and in catalog service.
      * @param data data to save
      * @throws BitDewException if a problem occurs
      */
     public void putData(Data data) throws BitDewException {
 	DBInterfaceFactory.getDBInterface().makePersistent(data);
 	try {
 	    idc.putData(data);  
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	    throw new BitDewException();
 	}
     }
 
     /**
      *  <code>deleteData</code> deletes data
      *
      * @param data a <code>Data</code> value
      * @exception BitDewException if an error occurs
      */
     public void deleteData(Data data) throws BitDewException {
 	//set the new status to TODELETE value
 	data.setstatus(DataStatus.TODELETE);
 	DBInterfaceFactory.getDBInterface().makePersistent(data);
 	putData(data);
     }
 
     /**
      * <code>createLocator</code> creates  a new locator.
      *
      * @param ref a <code>String</code> value
      * @return a <code>Locator</code> value
      * @exception BitDewException if an error occurs
      */
     public Locator createLocator(String ref)  throws BitDewException {
 	try {
 	    Locator locator = new Locator();
 	    locator.setref(ref);
  	    DBInterfaceFactory.getDBInterface().makePersistent(locator);
 	    putLocator(locator);
 	    return locator;
 	} catch (Exception re) {
 	    log.debug("Cannot createLocator " + re);
 	}
 	throw new BitDewException();
     } 
     
     /**
      * Creates the associated remote locator for a given data
      * @param data: the data we want to associate a Locator
      * @return Locator a remote locator sharing protocol,reference and uid  with data 
      */
     public Locator createRemoteLocator(Data data,String protocol) {
 	File f = new File("local");
 	Protocol prot;
 	Locator remote_locator = null;
 	try {
 	    prot = idr.getProtocolByName(protocol);
 	    log.debug(" The protocol extracted has the following data :  Path : " + prot.getpath() + " Login : " + prot.getlogin() + " Passwd : " + prot.getpassword());
 	    remote_locator = new Locator();
 	    prot.setpath(f.getPath());
 	    remote_locator.setdatauid(data.getuid());
 	    remote_locator.setprotocoluid(prot.getuid());
 	    remote_locator.setdrname(((CommRMITemplate) idr).getHostName());
 	    remote_locator.setref(data.getname());
 	    remote_locator.setpublish(true);
 	} catch (RemoteException e) {
 	    e.printStackTrace();
 	}
 	return remote_locator;
     }
 
     /**
      *  <code>putLocator</code> registers locator
      *
      * @param loc a <code>Locator</code> value
      * @exception BitDewException if an error occurs
      */
     public void putLocator(Locator loc) throws BitDewException {
 	try {
 	    idc.putLocator(loc);
 	    log.debug (" created locator " + loc.getuid());
 	    return;
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	} catch (Exception e) {
 	    log.debug("Error creating data " + e);
 	}
 	throw new BitDewException();
     }
     
     /**
      * Returns a protocol object from his string name
      * @param protocol the protocol name
      * @throws BitDew Exception if there was a problem
      * @return a protocol object that has the name in protocol parameter
      */
     public Protocol getProtocolByName(String protocol)throws BitDewException
     {	try {
 	    return idr.getProtocolByName(protocol);
 	} catch (RemoteException e) {
 	    throw new BitDewException("Problem getting the protocol " + protocol);
 	}
     }
 
     /**
      * <code>put</code> convenience method to register a data already
      * present in a data repository without having to copy the data repository.
      *
      * @param data a <code>Data</code> value
      * @param remote_locator a <code>Locator</code> value
      * @exception BitDewException if an error occurs
      */
     public OOBTransfer put(Data data, Locator remote_locator) throws BitDewException {
 	Protocol remote_proto;
 	File file = new File(data.getname());
 	Locator local_locator = new Locator();
 	Protocol local_proto = new Protocol();
 	local_proto.setname("local");
 	try {
 	
 	local_locator.setdatauid(data.getuid());
 		//	local_locator.setdrname("localhost");
 		//	local_locator.setprotocoluid(local_proto.getuid());
 	local_locator.setref(file.getAbsolutePath());
 		
 	log.debug("Local Locator : " + file.getAbsolutePath());
 	    
 	    
 	    if (remote_locator.getuid() == null)
 		DBInterfaceFactory.getDBInterface().makePersistent(remote_locator);
 	    remote_proto = idr.getProtocolByName(data.getoob());
 	    log.debug("Remote_proto fetched : " + remote_proto.getuid() + " : " +remote_proto.getname() +"://" + remote_proto.getlogin() + ":" +  remote_proto.getpassword() +  "@" + ((CommRMITemplate) idr).getHostName() + ":" +  remote_proto.getport() +"/" + remote_proto.getpath() );
 	} catch (RemoteException re) {
 	    log.debug("Cannot find a oob protocol " + data.getoob() + " " + re);
 	    throw new BitDewException();
 	}
 	remote_locator.setdatauid(data.getuid());
 	remote_locator.setdrname(((CommRMITemplate) idr).getHostName());
 	remote_locator.setprotocoluid(remote_proto.getuid());	
 	try {
 	    idc.putLocator(remote_locator);
 	    log.debug("registred new locator");
 	} catch (RemoteException re) {
 	    log.debug("Cannot register locator " + re);
 	    throw new BitDewException();
 	}
 	Transfer t = new Transfer();
 	t.setlocatorremote(remote_locator.getuid());
 	t.settype(TransferType.UNICAST_SEND_SENDER_SIDE);
 	OOBTransfer oobt=null;
 	try {
 	    oobt = OOBTransferFactory.createOOBTransfer(data, t, remote_locator, local_locator, remote_proto, local_proto);
 	} catch (OOBException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	return oobt;
     }
 
 
     /**
      * <code>put</code> file to a data
      *
      * @param file a <code>File</code> value, file to put
      * @param data a <code>Data</code> value, data into which to put the file
      * @param oob a <code>String</code> value, protocol to use to transfer the data
      * @exception BitDewException if an error occurs
      */
     public OOBTransfer put(File file, Data data, String oob) throws BitDewException {
 	data.setoob(oob);
 	return put(file, data);
     }
 
     /**
      * <code>put</code> file to a data
      *
      * @param file a <code>File</code> value
      * @param data a <code>Data</code> value
      * @exception BitDewException if an error occurs
      */
     public OOBTransfer put(File file, Data data) throws BitDewException {
 	
 	// No local protocol
 	Protocol local_proto = new Protocol();
 	local_proto.setname("local");
 
 	Locator local_locator = new Locator();
 	local_locator.setdatauid(data.getuid());
 	//	local_locator.setdrname("localhost");
 	//	local_locator.setprotocoluid(local_proto.getuid());
 	local_locator.setref(file.getAbsolutePath());
 	
 	log.debug("Local Locator : " + file.getAbsolutePath());
 	Protocol remote_proto;
 
 	// set the default protocol to FTP if there is no
 	if (data.getoob() == null)
 	    data.setoob("dummy");
 
 	try {
 	    remote_proto = idr.getProtocolByName(data.getoob());
 
 	    log.debug("Remote_proto fetched : " + remote_proto.getuid() + " : " +remote_proto.getname() +"://" + remote_proto.getlogin() + ":" +  remote_proto.getpassword() +  "@" + ((CommRMITemplate) idr).getHostName() + ":" +  remote_proto.getport() +"/" + remote_proto.getpath() );
 	} catch (RemoteException re) {
 	    log.debug("Cannot find a oob protocol " + data.getoob() + " " + re);
 	    throw new BitDewException();
 	}
 
 	Locator remote_locator = new Locator();
 	remote_locator.setdatauid(data.getuid());
 	remote_locator.setdrname(((CommRMITemplate) idr).getHostName());
 	remote_locator.setprotocoluid(remote_proto.getuid());
 
 	try {
 	    remote_locator.setref( idr.getRef("" +data.getuid()) );
 	    log.debug("Remote_reference fetched : " + remote_locator.getref());
 	} catch (RemoteException re) {
 	    log.debug("Cannot find a protocol ftp " + re);
 	    throw new BitDewException();
 	}
 
 	//prepare
 	Transfer t = new Transfer();
     	t.setlocatorremote(remote_locator.getuid());
 	t.settype(TransferType.UNICAST_SEND_SENDER_SIDE);
     //t.setlocatorlocal(local_locator.getuid());
 	//	Data data = DataUtil.fileToData(file);
 	OOBTransfer oobTransfer;
 	try {
 	    oobTransfer = OOBTransferFactory.createOOBTransfer(data, t, remote_locator, local_locator, remote_proto, local_proto);
 	   
 	} catch(OOBException oobe) {
 	   log.debug("Error when creating OOBTransfer " + oobe);
 	   throw new BitDewException("Error when transfering data to ftp server : " + remote_proto.getname() +"://" + remote_proto.getlogin() + ":" +  remote_proto.getpassword() +  "@" + ((CommRMITemplate) idr).getHostName() + ":" +  remote_proto.getport() +"/" + remote_proto.getpath() + "/" + remote_locator.getref() );
 	}
 	//FIXME cannot assume that the data has been fully copied now.
 	// should put a status to the locator ????
 	//no, data has now status LOCK and UNLOCK
 	try {
 	    remote_locator.setpublish(true);
 	    idc.putLocator(remote_locator);
 	    log.debug("registred new locator");
 	} catch (RemoteException re) {
 	    log.debug("Cannot register locator " + re);
 	    throw new BitDewException();
 	}
 
 	log.debug("Succesfully created data [" + data.getuid()+ "] with remote storage [" + remote_locator.getref()  + "] " + remote_proto.getname() +"://[" + remote_proto.getlogin() + ":" +  remote_proto.getpassword() +  "]@" + ((CommRMITemplate) idr).getHostName() + ":" +  remote_proto.getport() +"/" + remote_proto.getpath() + "/" + remote_locator.getref() );
 	return oobTransfer;
     }
 
     /**
      * <code>get</code> data into file.
      *
      * @param data a <code>Data</code> value
      * @param file a <code>File</code> value
      * @exception BitDewException if an error occurs
      */
     public OOBTransfer get(Data data, File file) throws BitDewException {
 	OOBTransfer oobTransfer=null;
 	// No local protocol
 	Protocol local_proto = new Protocol();
 	local_proto.setname("local");
 
 	Locator local_locator = new Locator();
 	local_locator.setdatauid(data.getuid());
 	//local_locator.setdrname("localhost");
 	//	local_locator.setprotocoluid(local_proto.getuid());
 	local_locator.setref(file.getAbsolutePath());
 	
 	log.debug("Local Locator : " + file.getAbsolutePath());
 
 	// get an FTP remote protocol
 	Locator remote_locator = null;
 
 	// set the default protocol to FTP if there is no
 	if (data.getoob() == null)
 	    data.setoob("FTP");
 
 	try {
 	    remote_locator = ( idc.getLocatorByDataUID( data.getuid() ));
 	    if (remote_locator==null) throw new BitDewException("Cannot retreive locator for data uid: " + data.getuid());
 	    log.debug("Remote_reference fetched : " + remote_locator.getref() + " and protocol " + remote_locator.getprotocoluid() + "@" + remote_locator.getdrname() );
 	} catch (RemoteException re) {
 	    log.debug("Cannot find a locator associated with data " + data.getuid() + " " + re);
 	    throw new BitDewException();
 	}
 
 	Protocol remote_proto;
 
 	try {
 	     remote_proto = idr.getProtocolByUID(remote_locator.getprotocoluid());
 	     log.debug("Remote_proto fetched : " + remote_proto.getuid() + " : " +remote_proto.getname() +"://" + remote_proto.getlogin() + ":" +  remote_proto.getpassword() +  "@" + ((CommRMITemplate) idr).getHostName() + ":" +  remote_proto.getport() +"/" + remote_proto.getpath() );
 	} catch (RemoteException re) {
 	    log.debug("Cannot find a protocol oob " + re);
 	    throw new BitDewException();
 	}
 
 	//prepar
 	Transfer t = new Transfer();
     	t.setlocatorremote(remote_locator.getuid());
 	t.settype(TransferType.UNICAST_RECEIVE_RECEIVER_SIDE);
 	//      t.setlocatorlocal(local_locator.getuid());
 	//	Data data = DataUtil.fileToData(file);
 	
 	try {
 	    oobTransfer = OOBTransferFactory.createOOBTransfer(data, t, remote_locator, local_locator, remote_proto, local_proto);
 	    /*	    oobTransfer.connect();
 	    oobTransfer.receiveReceiverSide();
 	    oobTransfer.waitFor();
 	    oobTransfer.disconnect();
 	    */
 	} catch(OOBException oobe) {
 	   log.debug("Was not able to transfer " + oobe);
 	   throw new BitDewException("Error when transfering data from : " + remote_proto.getname() +"://" + remote_proto.getlogin() + ":" +  remote_proto.getpassword() +  "@" + ((CommRMITemplate) idr).getHostName() + ":" +  remote_proto.getport() +"/" + remote_proto.getpath() + "/" + remote_locator.getref() );
 	}
 	
 	
 	return oobTransfer;
     }
 
 
     /**
      *  <code>searchDataByUid</code> searches data in the central data catalog.
      *
      * @param dataUid a <code>String</code> value
      * @return a <code>Data</code> value
      * @exception BitDewException if an error occurs
      */
     public Data searchDataByUid(String dataUid) throws BitDewException {
 	try {
 	    return idc.getData(dataUid);
 	} catch (RemoteException re ) {
 	    log.debug("cannot find data : " + dataUid + " in DC\n" + re);
 	}
 	throw new BitDewException();
     }
 
 
     /**
      *  <code>getDataUidByName</code> returns Data Uid accordings to the name
      *
      * @param name a <code>String</code> value
      * @return a <code>String</code> value
      * @exception BitDewException if an error occurs
      */
     public String getDataUidByName(String name) throws BitDewException {
 	try {
 	    return idc.getDataUidByName(name);
 	} catch (RemoteException re) {
 	    log.debug("cannot find data: "  + name + " in DC\n" + re);
 	}
 	throw new BitDewException();
 
     }
 
     /**
      *  <code>getDataByName</code> returns the Data according to the name
      *
      * @param name a <code>String</code> value
      * @return a <code>Data</code> value
      * @exception BitDewException if an error occurs
      */
     public Data getDataByName(String name) throws BitDewException {
 	String uid = getDataUidByName(name);
 	return searchDataByUid(uid);
     }
 
     /**
      *  <code>getAttributeByName</code> returns the Attribute according to the name
      *
      * @param name a <code>String</code> value
      * @return an <code>Attribute</code> value
      * @exception BitDewException if an error occurs
      */
     public Attribute getAttributeByName(String name) throws BitDewException {
 	try {
 	    return ids.getAttributeByName(name);
 	} catch (RemoteException re) {
 	    log.debug("cannot find attr: "  + name + " in DC\n" + re);
 	}
 	throw new BitDewException();
 
     }
 
     /**
      *  <code>getAttributeByUid</code> returns the Attribute according to its Uid
      *
      * @param uid a <code>String</code> value
      * @return an <code>Attribute</code> value
      * @exception BitDewException if an error occurs
      */
     public Attribute getAttributeByUid(String uid) throws BitDewException {
 	try {
 	    return ids.getAttributeByUid(uid);
 	} catch (RemoteException re) {
 	    log.debug("cannot find attr: "  + uid + " in DC\n" + re);
 	}
 	throw new BitDewException();
 
     }
 
 
     /**
      *  <code>ddcSearch</code> searches data in the distributed data catalog.
      *
      * @param data a <code>Data</code> value
      * @return a <code>String</code> value
      * @exception BitDewException if an error occurs
      */
     public String ddcSearch( Data data) throws BitDewException {
 	try {
 	    if (ddc !=null ) 
 		return ddc.search(data.getuid());
 	} catch (DDCException ddce ) {
 	    log.debug("cannot ddc find data : " + data + "\n" + ddce);
 	}
 	throw new BitDewException();
     }
 
     /**
      * <code>ddcPublish</code> publishes data and host in the
      * distributed data catalog.
      *
      * @param data a <code>Data</code> value
      * @param hostid a <code>String</code> value
      * @exception BitDewException if an error occurs
      */
     public void ddcPublish( Data data, String hostid) throws BitDewException {
 	try {
 	    if (ddc !=null ) 
 		ddc.publish(data.getuid(), hostid);
 	    return;
 	} catch (DDCException ddce) {
 	    log.debug("cannot ddc publish [data|hostid] : [" + data.getuid() + "|" + hostid + "]"  + "\n" + ddce);
 	}
 	throw new BitDewException();
     }
 
     /**
      *  <code>ddcPublish</code> publish arbitrary pair key value in
      *  the distributed data catalog
      *
      * @param key a <code>String</code> value
      * @param value a <code>String</code> value
      * @exception BitDewException if an error occurs
      */
     public void ddcPublish( String key, String value) throws BitDewException {
 	try {
 	    if (ddc !=null ) 
 		ddc.publish(key, value);
 	    return;
 	} catch (DDCException ddce) {
 	    log.debug("cannot ddc publish [data|hostid] : [" + key + "|" + value + "]"  + "\n" + ddce);
 	}
 	throw new BitDewException();
     }
 
     
 
     /**
      * <code>createDataCollection</code>create a DataCollection object, all the files in this directory 
      * are put into this DataCollection object 
      *  directory should end with "/" or "\\"
      * @param directory a <code>String</code> value
      * @return a <code>DataCollection</code> value
      * @exception BitDewException if an error occurs
      */
     public DataCollection createDataCollection(String directory) throws BitDewException {
 
 	long t2 = System.currentTimeMillis();
 	ReadFileList rfl = new ReadFileList();
 	rfl.getFileList(directory);
 	int FileNum = rfl.FileNum; 
 	for (int i=0;i<FileNum;i++)
 	    log.debug(rfl.separatedFilesAndSize[i][0]+"+"+rfl.separatedFilesAndSize[i][1]);
 	long totalsize = rfl.totalsize;
 
 	DataCollection datacollection = new DataCollection();
 	datacollection.setname(directory);
 	datacollection.setchecksum(DataUtil.checksum(new File(directory+rfl.separatedFiles[FileNum-1])));
 	datacollection.setsize(totalsize);
 	datacollection.setchunks(FileNum);
 
 	try {
 	    
 	    DBInterfaceFactory.getDBInterface().makePersistent(datacollection);
 	    idc.putDataCollection(datacollection);
 	    log.debug("datacollection uid = " + datacollection.getuid());
 	    
 	    for (int i=0;i<FileNum;i++){
 		
 		Data data = new Data();
 		data.setname(rfl.separatedFiles[i]);
 		data.setchecksum(DataUtil.checksum(new File(directory+rfl.separatedFiles[i])));
 		data.setsize(Long.parseLong(rfl.separatedFilesAndSize[i][1]));
 		data.settype(0);
 
 		DBInterfaceFactory.getDBInterface().makePersistent(data);
 		idc.putData(data);
 		log.debug("uid = " + DataUtil.toString(data));
 		log.debug("data uid = "+ DataUtil.toString(data));
 		
 		DataChunk datachunk = new DataChunk();
 		datachunk.setdatauid(data.getuid());
 		datachunk.setcollectionuid(datacollection.getuid());
 		datachunk.setindex(i);
 		datachunk.setoffset(i);
 		
 		DBInterfaceFactory.getDBInterface().makePersistent(datachunk);
 		idc.putDataChunk(datachunk);
 		log.debug("datachunk uid = " + datachunk.getuid());
 	    }
 	    long t3 = System.currentTimeMillis();
 	    setCreatTime(t3-t2);
 
 	    return datacollection;
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	} catch (Exception e) {
 	    log.debug("Error creating datacollection " + e);
 	}
 	throw new BitDewException();
 	
     }
 
     /**
      * <code>createDataCollection</code> create a DataCollection object given the full path of the file and the chunck size. 
      * Also split this file in chuncks.
      * @param fullNameAndPath a <code>String</code> value
      * @param blocksize a <code>long</code> value
      * @return a <code>DataCollection</code> value
      * @exception BitDewException if an error occurs
      */
     public DataCollection createDataCollection(String fullNameAndPath, long blocksize) throws BitDewException {
 	long t1 = System.currentTimeMillis();
 	SeparatorChannel separator = new SeparatorChannel();
 	try{
 	    separator.SepFile(fullNameAndPath, blocksize);
 	}catch (IOException ioe){
 	    ioe.printStackTrace();
 	}
 	long t2 = System.currentTimeMillis();
 	setSplitTime(t2-t1);
 
 	String directory = null;
 	int fn = fullNameAndPath.lastIndexOf("/");
 	if (fn!=-1)
 	    directory = fullNameAndPath.substring(0,fn+1);
 
 	log.debug("Oh ha, compute: Directory="+directory);
 	ReadFileList rfl = new ReadFileList();
 	rfl.getFileListFromSep(directory, (int)separator.BlockNum);
 	int FileNum = rfl.FileNum; 
 	for (int i=0;i<FileNum;i++)
 	    log.debug(rfl.separatedFilesAndSize[i][0]+"+"+rfl.separatedFilesAndSize[i][1]);
 	long totalsize = rfl.totalsize;
 
 	DataCollection datacollection = new DataCollection();
 	datacollection.setname(fullNameAndPath);
 	//	long tang = System.currentTimeMillis();
 	datacollection.setchecksum(DataUtil.checksum(new File(fullNameAndPath)));
 	//datacollection.setchecksum("0");
 	//	long bing = System.currentTimeMillis();
 	//	log.debug("md5sum for gss.tar.gz is:"+((bing-tang)/1000));
 	datacollection.setsize(totalsize);
 	datacollection.setchunks(FileNum);
 
 	try {
 	    
 	    DBInterfaceFactory.getDBInterface().makePersistent(datacollection);
 	    idc.putDataCollection(datacollection);
 	    log.debug("datacollection uid = " + datacollection.getuid());
 	    
 	    for (int i=0;i<FileNum;i++){
 		
 		Data data = new Data();
 		data.setname(rfl.separatedFiles[i]);
 		data.setchecksum(DataUtil.checksum(new File(directory+rfl.separatedFiles[i])));
 		data.setsize(Long.parseLong(rfl.separatedFilesAndSize[i][1]));
 		data.settype(0);
 
 		DBInterfaceFactory.getDBInterface().makePersistent(data);
 		idc.putData(data);
 		log.debug("uid = " + DataUtil.toString(data));
 		log.debug("data uid = "+ DataUtil.toString(data));
 		
 		DataChunk datachunk = new DataChunk();
 		datachunk.setdatauid(data.getuid());
 		datachunk.setcollectionuid(datacollection.getuid());
 		datachunk.setindex(i);
 		datachunk.setoffset(i);
 		
 		DBInterfaceFactory.getDBInterface().makePersistent(datachunk);
 		idc.putDataChunk(datachunk);
 		log.debug("datachunk uid = " + datachunk.getuid());
 	    }
 	    long t3 = System.currentTimeMillis();
 	    setCreatTime(t3-t2);
 
 	    return datacollection;
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	} catch (Exception e) {
 	    log.debug("Error creating datacollection " + e);
 	}
 	throw new BitDewException();	
     }
     
 
     /**
      * <code>put</code> each file of a directory into a DataCollection. 
      * Directory path should end with "/" or "\\"
      * @param directory a <code>String</code> value
      * @param datacollection a <code>DataCollection</code> value
      * @return a <code>Vector</code> value
      * @exception BitDewException if an error occurs
      */
     public Vector put(String directory, DataCollection datacollection) throws BitDewException {
     	boolean b = false;
 	Vector uidList = new Vector();
 
 	if (directory.endsWith("\\")||directory.endsWith("/"))
 	    b = true;
 	else
 	    log.debug("path error!");
 
 	Vector v =null;
 
 	int FileNum = datacollection.getchunks();
 
         try{
 	    v = idc.getAllDataInCollection(datacollection.getuid());
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	} 
 	    
 	if (v.size()!=FileNum)
 	    b = false;
 	else{
 	    log.debug("Size is OK! and Size= "+v.size());
 	}
 	
 	if (b){
 	    for (int i=0; i<FileNum; i++){
 	        Data data = (Data) v.elementAt(i);
 	        String name = directory+data.getname();
 	        File file = new File(name);
 	        put(file, data);
 
 		log.debug("Put one data finished! data name= "+name);
 	        log.debug("Data------");
 	        log.debug("data uid= "+data.getuid());
 	        log.debug("data checksum= "+data.getchecksum());
 	        log.debug("data size= "+data.getsize());
 	        log.debug("data type= "+data.gettype());
 	        log.debug("data oob= "+data.getoob());
 		uidList.addElement(data.getuid());
 	    }
 	}
 	return uidList;
     }
 
 
     /**
      * <code>put</code> directory should end with "/" or "\\"
      * set oob for each Data
      * @param directory a <code>String</code> value
      * @param datacollection a <code>DataCollection</code> value
      * @param oob a <code>String</code> value
      * @return a <code>Vector</code> value
      * @exception BitDewException if an error occurs
      */
     public Vector put(String directory, DataCollection datacollection, String oob) throws BitDewException {
 	boolean b = false;
 	Vector uidList = new Vector();
 
 	if (directory.endsWith("\\")||directory.endsWith("/"))
 	    b = true;
 	else
 	    log.debug("path error!");
 
 	Vector v =null;
 
 	int FileNum = datacollection.getchunks();
 
         try{
 	    v = idc.getAllDataInCollection(datacollection.getuid());
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	} 
 	    
 	if (v.size()!=FileNum)
 	    b = false;
 	else{
 	    log.debug("Size is OK! and Size= "+v.size());
 	}
 	
 	if (b){
 	    for (int i=0; i<FileNum; i++){
 	        Data data = (Data) v.elementAt(i);
 	        String name = directory+data.getname();
 	        File file = new File(name);
 		data.setoob(oob);
 	        put(file, data);
 
 		log.debug("Put one data finished! data name= "+name);
 	        log.debug("Data------");
 	        log.debug("data uid= "+data.getuid());
 	        log.debug("data checksum= "+data.getchecksum());
 	        log.debug("data size= "+data.getsize());
 	        log.debug("data type= "+data.gettype());
 	        log.debug("data oob= "+data.getoob());
 		uidList.addElement(data.getuid());
 	    }
 	}
 	return uidList;
     }
     
     /**
      *  <code>searchDataCollectionByUid</code> returns a DataCollection according to its Uid
      *
      * @param datacollectionUid a <code>String</code> value
      * @return a <code>DataCollection</code> value
      * @exception BitDewException if an error occurs
      */
     public DataCollection searchDataCollectionByUid(String datacollectionUid) throws BitDewException {
 	try {
 	    return idc.getDataCollection(datacollectionUid);
 	} catch (RemoteException re ) {
 	    log.debug("cannot find datacollection : " + datacollectionUid + " in DC\n" + re);
 	}
 	throw new BitDewException();
     }
 
     /**
      *  <code>searchDataCollectionByName</code> returns a DataCollection according to its name
      *
      * @param datacollectionname a <code>String</code> value
      * @return a <code>DataCollection</code> value
      * @exception BitDewException if an error occurs
      */
     public DataCollection searchDataCollectionByName(String datacollectionname) throws BitDewException {
 	try {
 	    return idc.getDataCollectionByName(datacollectionname);
 	} catch (RemoteException re ) {
 	    log.debug("cannot find datacollection : " + datacollectionname + " in DC\n" + re);
 	}
 	throw new BitDewException();
     }    
 
     
     /**
      * <code> get</code> all Data in this datacollection, save them in the directory.
      * directory should end with "/" or "\\"
      * @param datacollection a <code>DataCollection</code> value
      * @param directory a <code>String</code> value
      * @param oob a <code>String</code> value
      * @return a <code>Vector</code> value
      * @exception BitDewException if an error occurs
      */
     public Vector get(DataCollection datacollection, String directory, String oob) throws BitDewException {
 	boolean b = false;
 	Vector uidList = new Vector();
 
 	if (directory.endsWith("\\")||directory.endsWith("/"))
 	    b = true;
 	else
 	    log.debug("path error!");
 
 	Vector v =null;
 
 	int FileNum = datacollection.getchunks();
 
         try{
 	    v = idc.getAllDataInCollection(datacollection.getuid());
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	} 
 	    
 	if (v.size()!=FileNum)
 	    b = false;
 	else{
 	    log.debug("Size is OK! and Size= "+v.size());
 	}
 	
 	if (b){
 	    for (int i=0; i<FileNum; i++){
 	        Data data = (Data) v.elementAt(i);
 	        String name = directory+data.getname();
 	        File file = new File(name);
 		data.setoob(oob);
 		get(data, file);
 
 		log.debug("get one data finished! data name= "+name);
 	        log.debug("Data------");
 	        log.debug("data uid= "+data.getuid());
 	        log.debug("data checksum= "+data.getchecksum());
 	        log.debug("data size= "+data.getsize());
 	        log.debug("data type= "+data.gettype());
 	        log.debug("data oob= "+data.getoob());
 		uidList.addElement(data.getuid());
 	    }
 	}
 	return uidList;
     }
 
 
     /**
      * <code> get</code> all Data in this datacollection, save them in the directory.
      * directory should end with "/" or "\\"
      * get a datacollection
      *
      * @param datacollection a <code>DataCollection</code> value
      * @param directory a <code>String</code> value
      * @return a <code>Vector</code> value
      * @exception BitDewException if an error occurs
      */
     public Vector get(DataCollection datacollection, String directory) throws BitDewException {
 	boolean b = false;
 	Vector uidList = new Vector();
 
 	if (directory.endsWith("\\")||directory.endsWith("/"))
 	    b = true;
 	else
 	    log.debug("path error!");
 
 	Vector v =null;
 
 	int FileNum = datacollection.getchunks();
 
         try{
 	    v = idc.getAllDataInCollection(datacollection.getuid());
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	} 
 	    
 	if (v.size()!=FileNum)
 	    b = false;
 	else{
 	    log.debug("Size is OK! and Size= "+v.size());
 	}
 	
 	if (b){
 	    for (int i=0; i<FileNum; i++){
 	        Data data = (Data) v.elementAt(i);
 	        String name = directory+data.getname();
 	        File file = new File(name);
 		get(data, file);
 
 		log.debug("get one data finished! data name= "+name);
 	        log.debug("Data------");
 	        log.debug("data uid= "+data.getuid());
 	        log.debug("data checksum= "+data.getchecksum());
 	        log.debug("data size= "+data.getsize());
 	        log.debug("data type= "+data.gettype());
 	        log.debug("data oob= "+data.getoob());
 		uidList.addElement(data.getuid());
 	    }
 	}
 	return uidList;
     }
 
 
     /**
      * <code>combine</code> all Data exist, then combine to a big file, and check MD5
      *
      * @param datacollection a <code>DataCollection</code> value
      * @param directory a <code>String</code> value
      * @exception BitDewException if an error occurs
      */
     public void combine(DataCollection datacollection, String directory) throws BitDewException {
 	boolean b = false;
 	
 	if (directory.endsWith("\\")||directory.endsWith("/"))
 	    b = true;
 	else
 	    log.debug("path error!");
 
 	Vector v =null;
 
 	int FileNum = datacollection.getchunks();
 
         try{
 	    v = idc.getAllDataInCollection(datacollection.getuid());
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	} 
 	    
 	if (v.size()!=FileNum)
 	    b = false;
 	else{
 	    log.debug("Size is OK! and Size= "+v.size());
 	}
 	
 	if (b){
 	    
 	    Data data0 = (Data) v.elementAt(0);
             int len = data0.getname().length();
 	    if (data0.getname().substring(len-9, len-4).equals(".part")){
 	        log.debug("begin Combine datacollection");
 	        log.debug("directory="+directory);
 	        CombinatorChannel combinator = new CombinatorChannel();
 	        combinator.setDirectory(directory);
 		try{
 		    combinator.CombFile();
 		}catch(IOException ioe){
 		    ioe.printStackTrace();
 		}
 	        
 	        String combinedfile = directory + combinator.getRealName(data0.getname());
 	        String newMD5 = DataUtil.checksum(new File(combinedfile));
 	        String oldMD5 = datacollection.getchecksum();
 	        if (newMD5.equals(oldMD5))
 		    log.debug("Big File MD5 prefect!");
 	    }
 	}
     }
 
 
     /**
      * <code>combine</code> assembles data chunks already downloaded into a single file
      * @param directory a <code>String</code> value
      * @exception BitDewException if an error occurs
      */
     public void combine(String directory) throws BitDewException {
         log.debug("begin Combine datacollection");
         CombinatorChannel combinator = new CombinatorChannel();
         combinator.setDirectory(directory);
 	try{
 	    boolean a = combinator.CombFile();
 	    if (a)
 		log.debug("combine completed!");
 	}catch(IOException ioe){
 	    ioe.printStackTrace();
 	}
     }
 
     /**
      * <code>get</code> get  parts of a DataCollection. The chunks interval is specified by the begeining and end index.
      *
      * @param datacollection a <code>DataCollection</code> value
      * @param directory a <code>String</code> value
      * @param oob a <code>String</code> value
      * @param indexbegin an <code>int</code> value
      * @param indexend an <code>int</code> value
      * @return a <code>Vector</code> value
      * @exception BitDewException if an error occurs
      */
     public Vector get(DataCollection datacollection, String directory, String oob,  int indexbegin, int indexend) throws BitDewException {
 	
 	Vector uidList = new Vector();
 	if ((indexbegin>=0)&&(indexend>=0))
 	    if (indexend < indexbegin)
 		return null;
 	
 	boolean b = false;
 	
 	if (directory.endsWith("\\")||directory.endsWith("/"))
 	    b = true;
 	else
 	    log.debug("path error!");
 
 	Vector v =null;
 
 	int FileNum = indexend-indexbegin+1;
 
         try{
 	    v = idc.getDataInCollection(datacollection.getuid(), indexbegin, indexend);
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	} 
 	    
 	if (v.size()!=FileNum)
 	    b = false;
 	else{
 	    log.debug("Size is OK! and Size= "+v.size());
 	}
 	
 	if (b){
 	    for (int i=0; i<FileNum; i++){
 	        Data data = (Data) v.elementAt(i);
 	        String name = directory+data.getname();
 	        File file = new File(name);
 		data.setoob(oob);
 		log.debug("get one data finished! data name= "+name);
 	        log.debug("Data------");
 	        log.debug("data uid= "+data.getuid());
 	        log.debug("data checksum= "+data.getchecksum());
 	        log.debug("data size= "+data.getsize());
 	        log.debug("data type= "+data.gettype());
 	        log.debug("data oob= "+data.getoob());
 		get(data, file);
 		uidList.addElement(data.getuid());
 	    }
 	}
 	return uidList;
     }
 
     /**
      * <code>get</code> get  parts of a DataCollection. The chunks interval is specified by the begeining and end index.
      *
      * @param datacollectionuid a <code>String</code> value
      * @param directory a <code>String</code> value
      * @param oob a <code>String</code> value
      * @param indexbegin an <code>int</code> value
      * @param indexend an <code>int</code> value
      * @return a <code>Vector</code> value
      * @exception BitDewException if an error occurs
      */
     public Vector get(String datacollectionuid, String directory, String oob, int indexbegin, int indexend) throws BitDewException {
 	Vector uidList = new Vector();
 
 	if ((indexbegin>=0)&&(indexend>=0))
 	    if (indexend < indexbegin)
 		return null;
 	
 	boolean b = false;
 	
 	if (directory.endsWith("\\")||directory.endsWith("/"))
 	    b = true;
 	else
 	    log.debug("path error!");
 
 	Vector v =null;
 
 	int FileNum = indexend-indexbegin+1;
 
         try{
 	    v = idc.getDataInCollection(datacollectionuid, indexbegin, indexend);
 	} catch (RemoteException re) {
 	    log.debug("Cannot find service " + re);
 	} 
 	    
 	if (v.size()!=FileNum)
 	    b = false;
 	else{
 	    log.debug("Size is OK! and Size= "+v.size());
 	}
 	
 	if (b){
 	    for (int i=0; i<FileNum; i++){
 	        Data data = (Data) v.elementAt(i);
 	        String name = directory+data.getname();
 	        File file = new File(name);
 		data.setoob(oob);
 		log.debug("get one data finished! data name= "+name);
 	        log.debug("Data------");
 	        log.debug("data uid= "+data.getuid());
 	        log.debug("data checksum= "+data.getchecksum());
 	        log.debug("data size= "+data.getsize());
 	        log.debug("data type= "+data.gettype());
 	        log.debug("data oob= "+data.getoob());
 		get(data, file);
 		uidList.addElement(data.getuid());
 	    }
 	}
 	return uidList;
     }
 
     public void get(String uri, File file) throws BitDewException{
 	BitDewURI bduri = new BitDewURI(uri);
 	String dataUid = bduri.getUid();
 	Data data = searchDataByUid(dataUid);
 	get(data, file);
     }
 
     public void get(BitDewURI uri, File file) throws BitDewException{
 	String dataUid = uri.getUid();
 	Data data = searchDataByUid(dataUid);
 	get(data, file);
     }
 
     public long getSplitTime(){
 	return this.splittime;
     }
     
     public void setSplitTime(long t){
 	this.splittime = t;
     }
     
     public long getCreatTime(){
 	return this.creattime;
     }
     
     public void setCreatTime(long t){
 	this.creattime = t;
     }
     
 }
     // BitDew
