 package com.rhcloud.mongo.db;
 
 import java.io.Serializable;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 
 import com.mongodb.ReadPreference;
 import com.rhcloud.mongo.adapter.XmlStringTypeAdapter;
 
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 public class DatastoreConfig implements Serializable {
 
 	/**
 	 * 
 	 */
 	
 	private static final long serialVersionUID = 447030075733870658L;
 	
 	/**
 	 * 
 	 */
 	
 	@XmlElement(name="mongodb-db-host")
 	@XmlJavaTypeAdapter(XmlStringTypeAdapter.class)
 	private String host;
 	
 	/**
 	 * 
 	 */
 	
 	@XmlElement(name="mongodb-db-name")
 	@XmlJavaTypeAdapter(XmlStringTypeAdapter.class)
 	private String database;
 	
 	/**
 	 * 
 	 */
 	
 	@XmlElement(name="mongodb-db-port")
 	private int port;
 	
 	/**
 	 * 
 	 */
 	
 	@XmlElement(name="mongodb-db-username")
 	@XmlJavaTypeAdapter(XmlStringTypeAdapter.class)
 	private String username;
 	
 	/**
 	 * 
 	 */
 	
 	@XmlElement(name="mongodb-db-password")
 	@XmlJavaTypeAdapter(XmlStringTypeAdapter.class)
 	private String password;
 	
 	/**
 	 * 
 	 */
 	
 	@XmlElement(name="readPreference")
 	private ReadPreference readPreference;
 	
 	
 	/**
 	 * constructor
 	 */
 	
 	public DatastoreConfig() {
 		
 	}
 	
 	/**
 	 * getHostname
 	 * 
 	 * @return hostname
 	 */
 	
 	public String getHost() {
 		return host;
 	}
 	
 	/**
 	 * setHostname
 	 * 
 	 * @param hostname
 	 */
 	
 	public void setHost(String host) {
 		this.host = host;
 	}
 	
 	/**
 	 * getDatabase
 	 * 
 	 * @return database
 	 */
 	
 	public String getDatabase() {
 		return database;
 	}
 	
 	/**
 	 * setDatabase
 	 * 
 	 * @param database
 	 */
 	
 	public void setDatabase(String database) {
 		this.database = database;
 	}
 	
 	/**
 	 * getPort
 	 * @return port
 	 */
 	
 	public int getPort() {
 		return port;
 	}
 	
 	/**
 	 * setPort
 	 * 
 	 * @param port
 	 */
 	
 	public void setPort(int port) {
 		this.port = port;
 	}
 	
 	/**
 	 * getUsername
 	 * 
 	 * @return username
 	 */
 	
 	public String getUsername() {
 		return username;
 	}
 	
 	/**
 	 * setUsername
 	 * 
 	 * @param username
 	 */
 	
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	
 	/**
 	 * getPassword
 	 * 
 	 * @return password
 	 */
 	
 	public String getPassword() {
 		return password;
 	}
 	
 	/**
 	 * setPassword
 	 * 
 	 * @param password
 	 */
 	
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	
 	/**
 	 * getReadPreference
 	 * 
 	 * @return ReadPreference
 	 */
 	
 	public ReadPreference getReadPreference() {
 		return readPreference;
 	}
 	
 	/**
 	 * setReadPreference
 	 * 
 	 * @param readPreference
 	 */
 	
 	public void setReadPreference(ReadPreference readPreference) {
 		this.readPreference = readPreference;
 	}
 }
