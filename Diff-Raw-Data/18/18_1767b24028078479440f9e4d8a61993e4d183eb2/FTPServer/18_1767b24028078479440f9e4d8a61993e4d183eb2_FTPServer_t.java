 package xtremweb.role.examples;
 
 import jargs.gnu.CmdLineParser;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.util.Properties;
 import java.util.Vector;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPFile;
 
 import xtremweb.api.bitdew.BitDew;
 import xtremweb.api.bitdew.BitDewException;
 import xtremweb.core.com.idl.ComWorld;
 import xtremweb.core.com.idl.ModuleLoaderException;
 import xtremweb.core.iface.InterfaceRMIdr;
 import xtremweb.core.log.Logger;
 import xtremweb.core.log.LoggerFactory;
 import xtremweb.core.obj.dc.Data;
 import xtremweb.core.obj.dc.Locator;
 import xtremweb.core.obj.dr.Protocol;
 import xtremweb.role.cmdline.CommandLineTool;
 
 /**
  * This class makes available a remote FTP server folder in a bitdew powered
  * desktop grid.
  * 
  * @author jsaray
  *
  * @version 1.0
  */
 public class FTPServer {
 
     /*! \example FTPServer.java
      * 
     * This example starts bitdew services (dr, dc, dt, ds) and makes existing files
      * on a remote ftp folder available in bitdew. Then you can use the
      * "CommandLineTool get" option to download the files served by
      * the FTP server.
      * 
      * <ol> <li> Download and locate the file <a>xtremweb.properties</a>  in the same 
      * directory than the jar distribution.</li>
      *
      * <li> Uncomment the commented lines concerning the ftp configuration, by default it 
      * will connects to ftp.lip6.fr, but you can change the
      * configuration with your own ftp configuration data 
      * (hostname,port,directory to be made available, username and
      * password). If you leave the passwd field unfilled, FTPServer 
      * will try to perform an anonymous connection </li>
      * @code #xtremweb.serv.dr.protocols=dummy http ftp
      * #xtremweb.serv.dr.ftp.server=ftp.lip6.fr
      * #xtremweb.serv.dr.ftp.port=21 xtremweb.serv.dr.ftp.login=anonymous
      * #xtremweb.serv.dr.ftp.path=/pub/linux/distributions/slackware/slackware-current
      * #xtremweb.serv.dr.ftp.passwd=
      * @endcode 
      *
      * <li> The file CHECKSUMS.md5 must exist in the remote ftp folder.
      * FTPServer will use the checksum information provided in this file in order
      * to successfully download the file.  CHECKSUMS.md5  should have
      * the following format <md5_checksum> './'<filename>, for example</li>
      * @code 928143c5d1a7825bc5f3c1d18fde1e31 ./ANNOUNCE.13_1
      * c08333e6569a6fd66fd59999bfa94eb5 ./BOOTING.TXT
      * 19494cd00c011661a882c23772eaa7f0 ./CHANGES_AND_HINTS.TXT
      * 18810669f13b87348459e611d31ab760 ./COPYING
      * @endcode
      * 
      * <li> To run the example, execute the following command : </li>
      * @code java -cp bitdew-stand-alone.jar xtremweb.role.examples.FTPServer -f xtremweb.properties
      * @endcode
      * 
      * <li> This command will : 
      *<ol> <li>launch Bitdew services needed for this
      * experiment (data catalog,data transfer management,data repository) </li>
      * <li>connect to the configured FTP server ("xtremweb.serv.dr.ftp.server" property)</li>
      * <li>Create the bitdew objects (Data,Locators) needed to make files on 
      * the configured folder (property xtremweb.serv.dr.ftp.path) visible </li> </ol>
      * </li> <li>At the end of the command execution you should get something
      * like this :</li>
      * 
      * @code File ANNOUNCE.13_1 successfully available, uid=6390d3a0-1cbc-31e0-ab7e-c0c64755d3e6 
      * File BOOTING.TXT successfully available, uid=639edd60-1cbc-31e0-ab7e-c0c64755d3e6 
      * File CHANGES_AND_HINTS.TXT successfully available, uid=63a45ba0-1cbc-31e0-ab7e-c0c64755d3e6 
      * To retrieve any of these files in your system inside a file <file_name> please use xtremweb.role.cmdline.CommandLineTool get
      * <uid> <file_name>
      * 
      * @endcode <li> You can execute CommandLineTool get command in order to retrieve a
      * file from the ftp server, for example :</li>
      * 
      * @code java -cp xtremweb.role.cmdline.CommandLineTool get 639edd60-1cbc-31e0-ab7e-c0c64755d3e6 LOCAL_BOOTING.TXT
      * 
      * @endcode
      * 
      * <li> This command will download the file of uid
      * 639edd60-1cbc-31e0-ab7e-c0c64755d3e6 (remotely BOOTING.TXT) to the local
      * file LOCAL_BOOTING.TXT, you should get the following message:</li>
      * 
      * @code [ INFO] data has been successfully copied to LOCAL_BOOTING.TXT
      * 
      * @endcode Application code source : </ol>
      */
     /**
      * Apache FTPClient object
      */
     private FTPClient cl;
 
     /**
      * BitDew API to make available the FTP folder
      */
     private BitDew bd;
 
     /**
      * Data repository RMI interface
      */
     
     private InterfaceRMIdr dr;
     private Protocol prot;
     /**
      * Log4J loggger
      */
     private Logger log = LoggerFactory.getLogger("FTPServer");
 
 
     /**
      * Creates a new <code>FTPServer</code> instance.
      */
     public FTPServer() {
 
 	//First, we start the services dc dr dt on this node
	String[] serverargs = { "serv", "dc", "dr", "dt", "ds" };
 	new CommandLineTool(serverargs);
 
 	//Second, we create local connections to this services in
 	//order to create retreive information and create the data
 	Vector comms;
 	try {
 	    //comms is a vector containing the interfaces to each services 
	    comms = ComWorld.getMultipleComms("localhost", "rmi", 4325, "dc","dr", "dt", "ds");
 	    //the bitdew API is created and initialized. It will be
 	    //used later to create data
 	    bd = new BitDew(comms);
 	    //we need to retreive some information about the FTP
 	    //protocol. This information are available through the
 	    //Data repository interface.
 	    dr = (InterfaceRMIdr) comms.get(1);
 	    prot = dr.getProtocolByName("ftp");
 	    cl = new FTPClient();
 	} catch (ModuleLoaderException e) {
 	    e.printStackTrace();
 	} catch (RemoteException e) {
 	    e.printStackTrace();
 	}
 
     }
     
     /**
      * This method prints the syntax of FTPServer command
      */
     private static void printUsage() {
 	System.err
 		.println("Usage : FTPServer [file.properties]"
 			+ "Options:\n"
 			+ "file.properties : name of a .properties file");
 			
     }
 
     /**
      * This method creates the locators needed so bitdew can recognize the
      * data in a remote ftp folder on the data grid.
      * @return a <code>Vector</code> value
      */
     public Vector makeAvailable() {
 	Properties md5 = null;
 	Vector v = new Vector();
 	try {
 	    cl.enterLocalPassiveMode();
 	    FTPFile[] files = cl.listFiles();
 	    log.info("calculating md5 signatures from CHECKSUMS.md5");
 	    md5 = getSignatures();
 	    for (int i = 0; i < files.length; i++) {
 		String name = files[i].getName();
 		//if the file is a checksum file, we do not want to make it available:
 		if (name.equals("CHECKSUMS.md5")
 			|| name.equals("CHECKSUMS.md5.asc")
 			|| files[i].isDirectory())
 		    continue;
 		Data data = bd.createData(name,"FTP",files[i].getSize(),md5.getProperty(name));
 		Locator remote_locator = bd.createRemoteLocator(data,"ftp");
 		bd.associateDataLocator(data, remote_locator);
 		log.info("File " + name + " , uid="+ data.getuid());
 		v.add(data.getuid());
 	    }
 	    log.info("To retrieve any of these files in your system on a file <file_name> please use CmdLineTool get <uid> <file_name>");
 	} catch (IOException e) {
 	    e.printStackTrace();
 	} catch (BitDewException e) {
 	    e.printStackTrace();
 	} catch (Exception e) {
 	    log.debug("weird exception");
 	    e.printStackTrace();
 	}
 	return v;
     }
 
     /**
      * Disconnects this class instance from the FTP server
      */
     public void disconnect() {
 	try {
 	    cl.disconnect();
 	} catch (IOException e) {
 	    e.printStackTrace();
 	}
     }
 
     /**
      * Assuming there is a SIGNATURES.md5 file on pathname, this method parses
      * that file and builds a properties structure to provide an easy access to
      * the md5 file signatures.
      * 
      * @return a hash structure <name of file,md5 signature>
      * @exception Exception if an error occurs
      * @exception FileNotFoundException
      *             if a CHECKSUMS.MD5 file could not be found
      */
     public Properties getSignatures() throws Exception {
 	Properties p = new Properties();
 	boolean exist = false;
 	FileOutputStream fos;
 	try {
 	    fos = new FileOutputStream(new File("sigs.md5"));
 	    boolean result; 
 	    cl.enterLocalPassiveMode();
 	    result = cl.retrieveFile("CHECKSUMS.md5", fos);
 	    fos.close();
 	    if (!result)
 		throw new FileNotFoundException(
 			"There is no CHECKSUMS.md5 file on that directory");
 	    BufferedReader br = new BufferedReader(new FileReader("sigs.md5"));
 	    String str = br.readLine();
 	    while (str != null) {
 		if (str.split("/").length == 2) {
 		    exist = true;
 		    log.debug(str + " is a file");
 		    String[] tokens = str.split("  ./");
 		    if (tokens.length != 2)
 			throw new Exception(
 				"The md5 file is not propertly parsed I'm waiting <md5> ./<filename>");
 		    p.put(tokens[1], tokens[0]);
 		}
 		str = br.readLine();
 	    }
 	    if (!exist)
 		throw new Exception(
 			"The md5 file is not propertly parsed I'm waiting <md5> ./<filename>");
 	} catch (FileNotFoundException e) {
 	    e.printStackTrace();
 	} catch (IOException e) {
 	    e.printStackTrace();
 	}
 	return p;
     }
 
     /**
      * Changes directory to the one signaled by pathname
      * 
      * @return a <code>String</code> value
      * @exception Exception if an error occurs
      */
     public String changeDirectory() throws Exception {
 	cl.pwd();
 	    if (!cl.changeWorkingDirectory(prot.getpath()))
 		throw new Exception("Unknown directory " + prot.getpath());
 	return cl.printWorkingDirectory();
     }
 
     /**
      * This method try to logs in the FTP server signaled by the user
      * @exception Exception if a problem is found
      */
     public void login() throws Exception {
 	String user=prot.getlogin();
 	String passwd=prot.getpassword();
 	System.out.println("login " + user + " passwd " + passwd);
 	try {
 	    if (user.equals("anonymous")) {
 		if (!cl.login(user, ""))
 		    throw new Exception(
 			    "Is not possible to connect as anonymous");
 	    }
 	    else {
 		if (!cl.login(user, passwd))
 		    throw new Exception("Cannot be connected");
 	    }
 	} catch (IOException e) {
 	    e.printStackTrace();
 	}
     }
     /**
      * This method connnects an instance of this class to the FTP server
      * @exception Exception if an error occurs
      * @host the host we want to connect
      */
     public void connect() throws Exception {
 	String host=null;
 	try {
 	    host = prot.getserver();
 	    cl.connect(prot.getserver());
 	    log.info("connect server answer " + cl.getReplyString());
 	    log.info("connected to " + host);
 	} catch (UnknownHostException e) {
 	    throw new Exception("Unknown host " + host);
 	} catch (SocketException e) {
 	    e.printStackTrace();
 	} catch (IOException e) {
 	    e.printStackTrace();
 	}
     }
 
 
     /**
      * Program main method
      * 
      * @param args
      */
 
     public static void main(String[] args) {
 	try {
 	    //if a property file is specified, then use it
 	    if (args.length ==1) {
 		File fp = new File(args[1]);
 		if (fp.exists())
 		    System.setProperty("PROPERTIES_FILE", args[1]);
 	    }
 
 	    FTPServer ftpa = new FTPServer();
 	    ftpa.connect();
 	    ftpa.login();
 	    ftpa.changeDirectory();
 	    ftpa.makeAvailable();
 	    ftpa.disconnect();
 	} catch (Exception e) {
 	    System.err.println(e.getMessage());
 	    printUsage();
 	    System.exit(2);
 	}
     }
 
 
 }
