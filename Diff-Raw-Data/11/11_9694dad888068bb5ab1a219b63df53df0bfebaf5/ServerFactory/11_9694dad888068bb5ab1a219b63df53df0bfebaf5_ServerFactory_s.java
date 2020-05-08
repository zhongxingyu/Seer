 package hu.xea.nova.ws.rest.bamboo;
 
 import hu.xea.nova.ws.rest.bamboo.api.BambooServer;
 
 /**
  * Allows clients to instantiate {@link BambooServer} instances without knowing
  * the implementation details. 
  * 
 * @author sandor.pecsi
  *
  */
 public abstract class ServerFactory {
 
 	/**
 	 * Retrieves an instance of the Bamboo Server REST API proxy.
 	 * 
 	 * @return a {@link BambooServer} object
 	 */
 	public abstract BambooServer getServer();
 }
