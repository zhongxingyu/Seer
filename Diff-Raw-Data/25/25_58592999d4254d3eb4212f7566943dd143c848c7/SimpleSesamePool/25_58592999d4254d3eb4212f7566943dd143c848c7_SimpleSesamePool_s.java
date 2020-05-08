 package net.metadata.auselit.lorestore.triplestore;
 
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 
 import org.apache.log4j.Logger;
 import org.ontoware.rdf2go.model.ModelSet;
 
 /**
  * File based sesame repositories can only have one connection open to them at a
  * time. This class creates a pool of size one for connections.
  * 
  * Uses a java BlockingQueue, and based upon:
  * http://stackoverflow.com/questions/1137118/does-this-basic-java-object-pool-work/1139830#1139830
  * 
  * @author uqdayers
  * 
  */
 public final class SimpleSesamePool implements TripleStoreConnectorFactory {
 	private static final Logger LOG = Logger.getLogger(SimpleSesamePool.class);
 
 	private final BlockingQueue<ModelSet> connections;
 	private final TripleStoreConnectorFactory cf;
 	
 	public SimpleSesamePool(TripleStoreConnectorFactory cf) throws InterruptedException {
 		this.cf = cf;
 		ModelSet theConnection = cf.retrieveConnection();
		this.connections = new ArrayBlockingQueue<ModelSet>(1, false);
 		connections.add(theConnection);
 	}
 
 	public ModelSet retrieveConnection() throws InterruptedException {
 		return this.connections.take();
 	}
 	
 	public void release(ModelSet connection) throws InterruptedException {
 		this.connections.put(connection);
 	}
 	
 	public void destroy() {
 		LOG.info("Closing repository connection");
 		try {
 			ModelSet con = retrieveConnection();
 			con.close();
 		} catch (InterruptedException e) {
 			LOG.error("Unable to close connection", e);
 		}
 		cf.destroy();
 	}
 }
