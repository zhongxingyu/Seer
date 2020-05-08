 package de.dhbw.mannheim.cloudraid.net.connector;
 
 import java.io.InputStream;
 
 /**
 * Defines the methods to be implemented by classes that are used to connect do
  * cloud services.
  * 
  * @author Florian Bausch
  * 
  */
 public interface IStorageConnector {
 
 	/**
 	 * Connects to a cloud service
 	 * 
 	 * @return true, if the connection could be established; false, if not.
 	 */
 	public boolean connect(String service);
 
 	/**
 	 * Changes a file on a cloud service.
 	 * 
 	 * @return true, if the file could be changed; false, if not.
 	 */
 	public boolean put(String resource);
 
 	/**
 	 * Gets a file from a cloud service.
 	 * 
 	 * @return An InputStream to the regarding file.
 	 */
 	public InputStream get(String resource);
 
 	/**
 	 * Deletes a file on a cloud service.
 	 * 
 	 * @return true, if the file could be deleted; false, if not.
 	 */
 	public boolean delete(String resource);
 
 	/**
 	 * Sends a file to a cloud service.
 	 * 
 	 * @return The link to the new file on the cloud service.
 	 */
 	public String post(String resource, String parent);
 
 	/**
 	 * Returns the options available for a resource.
 	 * 
 	 * @return The options.
 	 */
 	public String[] options(String resource);
 
 	/**
 	 * Returns meta data for a resource.
 	 * 
 	 * @return The meta data.
 	 */
 	public String head(String resource);
 
 }
