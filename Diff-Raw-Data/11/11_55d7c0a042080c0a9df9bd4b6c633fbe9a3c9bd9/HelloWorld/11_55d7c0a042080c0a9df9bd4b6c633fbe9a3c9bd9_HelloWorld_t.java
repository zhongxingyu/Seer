 package xtremweb.role.examples;
 
 import java.util.Vector;
 import xtremweb.core.log.*;
 import xtremweb.api.bitdew.BitDew;
 import xtremweb.api.activedata.ActiveData;
 import xtremweb.api.activedata.ActiveDataCallback;
 import xtremweb.core.com.idl.ComWorld;
 import xtremweb.core.obj.dc.Data;
 import xtremweb.core.obj.ds.Attribute;
 
 /**
  * <code>HelloWorld</code>.
  *
  * @author <a href="mailto:fedak@lri.fr">Gilles Fedak</a>
  * @version 1.0
  */
 
 public class HelloWorld {
 
     /*! @mainpage BitDew: An Open Source Middleware for Large Scale Data Management
      *
      * This is a short introduction to run the exemple in the BitDew
      * source package 
      *
      * @section download Downloading BitDew
      *
      * Please Download BitDew from the BitDew web site
      * hppt://www.BitDew.net
      * 
      * You can get a complete documentation on the web site.
      *
      * @section run_sec Running the examples
      *
      * To install the BitDew software follow the steps :
      * <ol>
      *  <li> deflate the BitDew archive</li>
      *  <li> move in the BitDew directory</li>
      * </ol>
      *
      * If you modify the source and if you need to compile again
      * BitDew, run the following command :
      @code
      ./build.sh
      @endcode
      * 
      * Examples have similar structure : the example code is usually a
      * client. You will need to execte the different services to be
      * able to start the exemples
      * <ol>
      * <li>start a server. Open a terminal window and start the
      * following command</li>
 
      @code
      java -jar bitdew-stand-alone.jar serv dc dt dr ds
      @endcode
 
      * <li> start the example
 
      @code
      java -cp bitdew-stand-alone.jar xtremweb.role.examples.HelloWorld localhost
      @endcode
      * 
      *<li> Please, consult the documentation specific for each  example !
      </ol>
      */
 
     /*! \example HelloWorld.java
      *
      *
      * The first example shows how to create Data, set an Attribute to the 
      * Data and schedule the Data.
      * You should definitely have a look in the source code, HelloWorld.java.
 
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
      java -cp bitdew-stand-alone.jar xtremweb.role.examples.HelloWorld localhost
      @endcode
 
      * <li> start for the second time the example in a new shell so that the data created can be scheduled somewhere. Wait for a few seconds.
 
      @code
      java -cp bitdew-stand-alone.jar xtremweb.role.examples.HelloWorld localhost
      @endcode
 
      * <li> On each shell you should see a message similar to :
 
      @code
      Received data: Hello World, whose uid is 8a356580-445f-31dd-887a-39748b0f20e7 and with Attribute helloWorldAttr
      @endcode
 
      * <li> This show that each HelloWorld program have exchanged data. As you can see, the two uids are different, which means that two data were created.
 
     *<li> Source code of the HelloWorld class:
      */
 
     Logger log = LoggerFactory.getLogger("HelloWorld");
     BitDew bitdew;
     ActiveData activeData;
 
     /**
      * Creates a new <code>HelloWorld</code> instance.
      *
      * @param host a <code>String</code> value
      * @param port an <code>int</code> value
      */
     public HelloWorld(String host, int port) throws Exception {
 
 	//intialize the communication vectors which will be used by
 	//the API
 	Vector comms = ComWorld.getMultipleComms(host, "rmi", port, "dc", "dr",  "ds");
 
 	//now intialize the APIs
 	bitdew = new BitDew(comms);
 	activeData = new ActiveData(comms);
 
 	//register the HelloWorldCallback to handle Data creation 
 	activeData.registerActiveDataCallback(new HelloWorldCallback());	
 	activeData.start();
 
 	//now creates a Data
 	Data data = bitdew.createData("Hello World");
 	
 	//creates an Attribute, which has the property to create a
 	//data replica for every host
 	Attribute attr = activeData.createAttribute("attr helloWorldAttr = {replicat = -1 }");
 
 	//associates the Attribute with the Data
 	activeData.schedule(data, attr);
	log.info(" data HelloWorld [" + data.getuid() + "] is successfully created and scheduled with attribute helloWorldAttr = {replicat = -1 } " );
     }
 
     /**
      *  <code>HelloWorldCallback</code> handles scheduling of Data.
      *
      */
     public class HelloWorldCallback implements ActiveDataCallback {
 
 	/**
 	 * Describe <code>onDataScheduled</code> method here.
 	 *
 	 * @param data a <code>Data</code> value
 	 * @param attr an <code>Attribute</code> value
 	 */
 	public void onDataScheduled(Data data, Attribute attr) {
 	    System.out.println("Received data: " + data.getname() + ", whose uid is " + data.getuid() 
 			       + ", and with Attribute " + attr.getname());
 	}
 
 	public void onDataDeleted(Data data, Attribute attr) {}
     }
 
     //usage is localhost if your services (dc, ds) run on the same host
     //otherwise
     public static void main(String[] args) throws Exception {
  	String hostName = "localhost";
 	if (args.length>0) hostName = args[0];
 	int port = 4325;
	if (args.length==2) port=Integer.parseInt(args[1]);
 	HelloWorld hw = new HelloWorld(hostName, port);
     }
 
 }
