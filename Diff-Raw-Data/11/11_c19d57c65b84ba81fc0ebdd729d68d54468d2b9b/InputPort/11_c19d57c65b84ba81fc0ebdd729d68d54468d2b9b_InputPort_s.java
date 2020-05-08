 
 package yarp.os;
 
 import java.io.*;
 
 
 /**
  * A network-visible object for receiving information.  
  * The port is assigned a name
  * using the register method.  Then it is associated (using the
  * creator method) with a ContentCreator object that will be delegated
  * the work of decoding and in constructing input objects as they
  * arrive.
  */
 public class InputPort implements Port {
 
     /**
      * Assign a unique global name to this port.
      * @param name The name to assign.
      */
     public void register(String name) {
 	BasicPort basic = new BasicPort(name);
 	basic.setHandler(handler);
 	if (pool==null) {
 	    Logger.get().error("should call creator before register");
 	    System.exit(1);
 	}
 	port = basic;
 	basic.start();
     }
 
 
     /**
      * Wait for input to arrive.  Input can be extracted using the
      * content method.  Must have called register and creator
      * once.
      * @param wait true if should wait, false if should poll.
      * @return true is input is available.
      */
     public boolean read(boolean wait) {
 	boolean worthwhile = false;
 	log.println("InputPort getting ready to read");
 	try {
 	    synchronized(stateMutex) {
 
		if (content!=null) {
 		    pool.unget(content);
 		    content = null;
 		}
 
 		clientReading = false;
 		if (pending>0) {
 		    worthwhile = true;
 		    pending--;
 		}
 	    }
 	    if (!worthwhile && wait) {
 		log.println("InputPort getting ready to wait " + worthwhile + " " + wait + " " + pending);
 		synchronized(readSomething) {
 		    readSomething.wait();
 		}
 		synchronized(stateMutex) {
 		    if (pending>0) {
 			worthwhile = true;
 			pending--;
 		    }
 		}
 	    }
 	    log.println("InputPort got something? " + worthwhile);
 	    return worthwhile;
 	} catch (InterruptedException e) {
 	    return false;
 	}
     }
 
 
     /**
      * wait and read
      */
     public boolean read() {
 	return read(true);
     }
 
 
     /**
      * Shut the port down.
      */
     public void close() {
 	port.close();
     }
 
 
     /**
      * Access input that has arrived.  Must call read method first.
      * @return the input that has arrived.
      */
     public Object content() {
 	synchronized(stateMutex) {
 	    if (content!=null) {
 		clientReading = true;
 		return content.object();
 	    }
 	}
 	return null;
     }
 
 
     /**
      * Assign the object delegated to create input objects.
      * @param creator the object delegated to create input objects.
      */
     public void creator(ContentCreator creator) {
 	pool = new ContentPool(creator);
     }
 
 
     private class InputPortHandler implements ProtocolHandler  {
 
 	public void read(Protocol proto) {
 	    Logger.get().println("Could read now!");
 	    Content worker = pool.get();
 	    if (worker!=null) {
 		try {
 		    worker.read(proto.getReader());
 		    //log.info("Simulating a slow read");
 		    //Time.delay(2);
 		    //log.info("Simulated a slow read");
 		} catch (IOException e) {
 		    Logger.get().error("Problem reading");
 		}
 	    }
 	    synchronized(stateMutex) {
 		if (content==null) {
 		    content = worker;
 		    pending = 1;
 		} else {
 		    pool.unget(worker);
 		    log.info("Discarding input, slow client");
 		}
 	    }
 	    onRead();
 	    synchronized(readSomething) {
 		readSomething.notify();
 	    }
 	}
 	
 	public void write(Protocol proto) {
 	    // cannot do this from input port
 	}
     }
 
     public String name() {
 	return port.name();
     }
 
     public void onRead() {
 	// can override this to do something upon receipt of content
     }
 
     private ContentCreator creator;
     private BasicPort port;
     private Object readSomething = new Object();
     private Object stateMutex = new Object();
     private boolean clientReading = false;
     private Content content;
     private ProtocolHandler handler = new InputPortHandler();
     private int pending = 0;
     private ContentPool pool = null;
     static private Logger log = Logger.get();
 }
 
