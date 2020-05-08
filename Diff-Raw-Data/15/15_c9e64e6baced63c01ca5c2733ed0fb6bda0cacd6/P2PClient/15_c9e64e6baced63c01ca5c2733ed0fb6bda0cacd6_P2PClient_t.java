 package xtremweb.role.examples;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.Properties;
 
 import xtremweb.api.bitdew.BitDew;
 import xtremweb.api.bitdew.BitDewException;
 import xtremweb.api.transman.TransferManager;
 import xtremweb.api.transman.TransferManagerException;
 import xtremweb.core.com.idl.ComWorld;
 import xtremweb.core.com.idl.ModuleLoaderException;
 import xtremweb.core.conf.ConfigurationException;
 import xtremweb.core.conf.ConfigurationProperties;
 import xtremweb.core.iface.InterfaceRMIdc;
 import xtremweb.core.iface.InterfaceRMIdr;
 import xtremweb.core.iface.InterfaceRMIds;
 import xtremweb.core.iface.InterfaceRMIdt;
 import xtremweb.core.log.Logger;
 import xtremweb.core.log.LoggerFactory;
 import xtremweb.core.obj.dc.Data;
 import xtremweb.serv.dt.OOBTransfer;
 
 //
 // This class request over the P2P network a specific term.
 // 
 //@author josefrancisco
 // 
 //
 public class P2PClient {
 
     //
     // Bitdew API
     //
     private BitDew bitdew;
 
     //
     // Transfer manager API
     private TransferManager tm;
 
     //
     // Data Catalog service
     //
     private InterfaceRMIdc dc;
 
     //
     // Data Transfer service
     private InterfaceRMIdt dt;
 
     //
     // Data repository service
     //
     private InterfaceRMIdr dr;
 
     //
     // Data scheduler service
     //
     private InterfaceRMIds ds;
 
     //
     // Local IP address constant
     //
     private String LOCAL_ADDRESS;
     
     //
     //	Log file
     //
     private Logger log = LoggerFactory.getLogger("P2PClient");
     
     //
     //	DLPT bootstrap node
     //
     private String BOOTSTRAP;
 
     //
     // Class constructor, initialize services and API 
     //
     public P2PClient() {
 
 	try {
 	    Properties props = ConfigurationProperties.getProperties();
 	    // we read the bootstrap node value from the properties file
 	    String bootstrap = props.getProperty("xtremweb.core.http.bootstrapNode");
 	    // if there is no value on the file, we assing localhost by default
 	    BOOTSTRAP = bootstrap != null ? bootstrap : InetAddress.getLocalHost().getHostAddress();
 	    //local address is the machine IP local address
 	    LOCAL_ADDRESS = InetAddress.getLocalHost().getHostAddress();
 	    // we build bitdew services dc dt and ds, as we need to perform requests on data contained on the DHT, 
 	    // dc will reference the DHT
 	    dc = (InterfaceRMIdc) ComWorld.getComm(BOOTSTRAP, "rmi", 4325, "dc");
 	    dt = (InterfaceRMIdt) ComWorld.getComm(LOCAL_ADDRESS, "rmi", 4325,"dt");
 	    ds = (InterfaceRMIds) ComWorld.getComm(LOCAL_ADDRESS, "rmi", 4325,"ds");
 	    // starting bitdew, transfer manager API and starting tm.
 	    tm = new TransferManager(dt);
 	    bitdew = new BitDew(dc, dr, dt, ds);
 	    tm.start();
 	} catch (ModuleLoaderException e) {
 	    log.warn("All bitdew services could not be loaded, if you want to use BitDew API make sure you launch them before " + e.getMessage());
 	} catch (UnknownHostException e) {
 	    e.printStackTrace();
 	} catch (ConfigurationException e) {
 	    e.printStackTrace();
 	}
     }
 
     
 
     //
     // Download a song from the P2P network, it builds the neccessary build infrastructure to achieve this.
     // @param songname the song name
     // @param md5 the song signature 
     //
    public void download(String songname, String md5,String ip) throws BitDewException, TransferManagerException, ModuleLoaderException{
 	try {
 	    // first retrieve the ip list of machines having signatures md5
 	    List ips = bitdew.ddcSearch(md5);
 	   
 	    // Once we have an ip of a machine having that md5sum, we are able to begin the download,
 	    // but first we need to contact that  machine's catalog and repository
 	    if (ips != null && ips.size() != 0 && ip.equals("none")) {
 		dr = (InterfaceRMIdr) ComWorld.getComm((String) ips.get(0),
 			"rmi", 4325, "dr");
 		dc = (InterfaceRMIdc) ComWorld.getComm((String) ips.get(0),
 			"rmi", 4325, "dc");
 	    } else if (!ip.equals("none")){
 		dr = (InterfaceRMIdr) ComWorld.getComm((String) ip,
 			"rmi", 4325, "dr");
 		dc = (InterfaceRMIdc) ComWorld.getComm((String) ip,
 			"rmi", 4325, "dc");
 	    }
 	    else
 		throw new BitDewException("There is not ip for that md5 ! ");
 	    // Then we create a new bitdew API from these newly created services
 	    bitdew = new BitDew(dc, dr, dt, ds);
 	    File file = new File(songname);
 	    //the getDataFromMd5 method help us to retrieve a bitdew data having a given MD5sum 
 	    Data d = bitdew.getDataFromMd5(md5);
 	    //we will use http protocol
 	    d.setoob("http");
 	    OOBTransfer oob;
 	    //download begins
 	    oob = bitdew.get(d, file);
 	    tm.registerTransfer(oob);
 	    tm.waitFor(d);
 	    log.info("File : " + songname + " was successfully downloaded ");
 	} catch (BitDewException e) {
 	    e.printStackTrace();
	    throw new BitDewException(e.getMessage());
 	} catch (TransferManagerException e) {
 	    e.printStackTrace();
	    throw new TransferManagerException(e.getMessage());
 	} catch (ModuleLoaderException e) {
 	    e.printStackTrace();
	    throw new ModuleLoaderException(e.getMessage());
 	}
     }
     
     //
     // This method includes you as the owner of a file once you have downloaded it
     // @param song file name
     // @param md5 the md5 cheksum
     //
    public void republish(String song,String md5) throws ModuleLoaderException,BitDewException{
 	try {
 	    //We are going to publish the new downloaded song in the DHT, so we need to 
 	    //build a reference to the distributed data catalog on the bootstrap node.
 	    dc = (InterfaceRMIdc) ComWorld.getComm(BOOTSTRAP, "rmi", 4325, "dc");
 	    dt = (InterfaceRMIdt) ComWorld.getComm(LOCAL_ADDRESS, "rmi", 4325,"dt");
 	    ds = (InterfaceRMIds) ComWorld.getComm(LOCAL_ADDRESS, "rmi", 4325,"ds");
 	    dr = (InterfaceRMIdr) ComWorld.getComm(LOCAL_ADDRESS, "rmi", 4325,"dr");
 	    //build a new bitdew instance from these services.
 	    bitdew = new BitDew(dc, dr, dt, ds);
 	    String[] toks = song.split("[\\s\\._-]");
 	    //We split the file name to extract each word composing it, and we index the song name
 	    //with each one of this words, then we index the MD5 too together with the IP address
 	    for (int i = 0; i < toks.length; i++) {
 		Data sb = new Data();
 		sb.setname(song);
 		sb.setchecksum(md5);
 		bitdew.ddcPublish(md5, LOCAL_ADDRESS);
 	    }
 	} catch (ModuleLoaderException e) {
 	    e.printStackTrace();
	    throw new ModuleLoaderException(e.getMessage());
 	} catch (BitDewException e) {
 	    e.printStackTrace();
	    throw new BitDewException(e.getMessage());
 	}
     }
 }
