 package xtremweb.role.examples;
 
 import xtremweb.core.log.*;
 import xtremweb.api.bitdew.BitDew;
 import xtremweb.serv.dc.DataUtil;
 import xtremweb.api.transman.TransferManager;
 import xtremweb.core.com.idl.ComWorld;
 import xtremweb.core.obj.dc.Data;
 
 import java.util.Vector;
 import java.io.File;
 
 /**
  *  <code>PutGet</code>.
  *
  * @author <a href="mailto:fedak@lri.fr">Gilles Fedak</a>
  * @version 1.0
  */
 public class PutGet {
 
 
     /*! \example PutGet.java
      *
      *
      * This example shows how:
      * <ol>
      * <li>to create a Data from a file and to copy the
      * file to the Data Space </li>
      * <li>to retreive the Data in a new file</li>
      *</ol>
 
      * 
      * Examples have similar structure : the example code is usually a
      * client. You will need to execute the different services to be
      * able to start the exemple
 
      * <ol>
      * <li>start a server. Open a terminal window and start the
      * following command</li>
 
      @code
      java -jar bitdew-stand-alone.jar serv dc dt dr ds
      @endcode
 
      * <li> open a new terminal window and start the example
 
      @code
      java -cp bitdew-stand-alone.jar xtremweb.role.examples.PutGet put myfile
      @endcode
 
      * <li> This command will create a new Data and copy the content of the file 
      * in the Data Space using the http protocol.
 
      * <li>At the end of the execution, you should see something like :
      @code
 [ INFO]  succesfully created data myfile [20f35880-4e72-31dd-be09-42554967791c] = { md5=5dd2e8d0dc257e5c756e11d3ddfff2eb size=10267 }
 [ INFO] data successfully transfered.
 [ INFO] To get the data, invoke the same program with the following arguments:
 [ INFO] get  myfile_copy 20f35880-4e72-31dd-be09-42554967791c
 
      @endcode
      * <li> You can now use the following command to retreive copy the data in a new file 
 
      @code
      java -cp bitdew-stand-alone.jar xtremweb.role.examples.PutGet get  myfile_copy 20f35880-4e72-31dd-be09-42554967791c
      @endcode
 
      *<li> You should now obtain the following message :
 
      @code
 [INFO] data has been successfully copied to myfile_copy
      @endcode
 
      *<li> Source code of the PutGet example: 
      */
 
 
     private BitDew bitdew;
     private TransferManager transferManager;
 
     Logger log = LoggerFactory.getLogger("PutGet");
 
     /**
      * Creates a new <code>PutGet</code> instance.
      *
      */
     public PutGet(String host, int port) throws Exception {
 
 	//intialize the communication vectors which will be used by
 	//the API
 	Vector comms = ComWorld.getMultipleComms(host, "rmi", port, "dc", "dr", "dt", "ds");
 
 	//now intialize the APIs
 	bitdew = new BitDew(comms);
 	transferManager = new TransferManager(comms);
 	transferManager.start();
     }
 
     public void put(String fileName) throws Exception {
 	File file = new File(fileName);
 	if (!file.exists()) {
 	    log.fatal("file  " + fileName + "does not exist");
 	    System.exit(1);
 	}
 	//create a Data from a file
 	Data data = bitdew.createData(file);
 
	log.info(" successfully created " + DataUtil.toString(data));
 
 	//
 	bitdew.put( file, data, "http");
 	transferManager.waitFor(data);	
 	transferManager.stop();
 	log.info("data successfully transfered.");
 	log.info("To get the data, invoke the same program with the following arguments:");
 	log.info("get  " + data.getname() + "_copy "+ data.getuid());
     }
     
 
     public void get (String fileName, String dataUid) throws Exception {
 	File file = new File(fileName);
 	Data data = bitdew.searchDataByUid(dataUid);	    
 	bitdew.get(data, file);
 	
 	transferManager.start();
 	transferManager.waitFor(data);	
 	transferManager.stop();
 	log.info("data has been successfully copied to " + fileName);
     }
 
     public static void usage() {
 	System.out.println("usage : [host] [port] put filename");
 	System.out.println("   or : [host] [port] get filename datauid");
 	System.exit(1);
     }
 
     public static void main(String[] args) throws Exception {
 
 	String hostName = "localhost";
 	int port = 4325;
 	boolean put = true;
 	String filename = "";
 	String datauid = "";
 
 	if (args.length<2) 
 	    usage();
 	
 	for(int i=0; i<args.length;i++) {
 	    if (args[i].equals("put") || args[i].equals("get") ) {
 		if (i==(args.length-1)) {
 		    usage();
 		}
 		//host name and port
 		if (i==2) {
 		    hostName = args[0];
 		    Integer.parseInt(args[1]);
 		}
 		if (i==1) {
 		    hostName = args[0];
 		}
 		//put or get
 		put = args[i].equals("put");
 		//filename
 		filename = args[i+1];
 		//datauid
 		if (i+2==(args.length-1)) {
 		    datauid = args[i+2];
 		}		
 		break;
 	    }
 	}
 	PutGet pg = new PutGet(hostName, port);
 	if (put)
 	    pg.put(filename);
 	else
 	    pg.get(filename,datauid);
     }
 
 }
