 package eu.dm2e.ws.api;
 
import java.util.List;

 /** Interface for classes that support validation */
 public interface IValidatable {
 
	public abstract List<ValidationMessage> validate() throws Exception;
 
 }
