/**
 * 
 */
 
 package org.eclipselabs.emongo.config;
 
 /**
  * @author bhunt
  * 
  */
 public interface ConfigurationProperties
 {
 	/**
 	 * The factory PID of the client provider component. Use this constant when configuring a client
 	 * provider component with ConfigurationAdmin.
 	 */
 	String CLIENT_PID = "org.eclipselabs.emongo.clientProvider";
 
 	/**
 	 * The factory PID of the id factory component. Use this constant when configuring an id factory
 	 * component with ConfigurationAdmin.
 	 */
 	String ID_FACTORY_PID = "org.eclipselabs.emongo.idFactory";
 
 	/**
 	 * The factory PID of the database provider component. Use this constant when
 	 * configuring a database component with ConfigurationAdmin.
 	 */
 	String DATABASE_PID = "org.eclipselabs.emongo.databaseProvider";
 }
