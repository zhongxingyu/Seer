 
 package yarp.os;
 
 import java.lang.*;
 import java.io.*;
 
 public class OutputPort {
 
     private ContentCreator creator;
     private Content content;
     private BasicPort port;
 
     public void creator(ContentCreator creator) {
 	this.creator = creator;
     }
 
     public void register(String name) {
 	Address server = NameClient.getNameClient().register(name);
 	Logger.get().info("Registered output port " + name + " as " + 
 			  server.toString());
 	port = new BasicPort(server,name);
 	port.start();
     }
 
     public void connect(String name) {
 	if (port==null) {
 	    Logger.get().error("Please call register() before connect()");
 	    System.exit(1);
 	}
 	if (creator==null) {
 	    Logger.get().error("Please call creator() before connect()");
 	    System.exit(1);
 	}
 
 	NameClient nc = NameClient.getNameClient();
 
 	String base = NameClient.getNamePart(name);
 	String carrier = NameClient.getProtocolPart(name);
 	
 	
 	Address add = nc.query(base);
 	
 	if (add==null) {
 	    Logger.get().error("Cannot find port " + base);
 	    return;
 	}
 	add = new Address(add.getName(),add.getPort(),carrier);
 	Logger.get().println("connecting to address " + add.toString());
 	
 	/*
 	 * Let's be polite and make sure the carrier is supported
 	 */
	if (NameClient.canConnect(port.getName(),base,carrier)) {
 	    Connection c = null;
 	    try {
 		c = new Connection(add,port.getPortName(),base);
 		port.addConnection(c);
 	    } catch (IOException e) {
 		Logger.get().error("Could not make connection");
 	    }
 	}
     }
 
     public Object content() {
 	if (content==null) {
 	    content = creator.create();
 	}
 	return content.object();
     }
 
     public void write() {
 	content();
 	port.send(content);
 	content = null;
     }
 
     public void write(Content content) {
 	port.send(content);
     }
 
     public void close() {
 	port.close();
     }
 
 }
