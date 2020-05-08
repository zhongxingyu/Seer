 package eu.dm2e.ws.api;
 
 /** Interface for classes that support validation */
 public interface IValidatable {
 
	public abstract ValidationReport validate() throws Exception;
 
 }
