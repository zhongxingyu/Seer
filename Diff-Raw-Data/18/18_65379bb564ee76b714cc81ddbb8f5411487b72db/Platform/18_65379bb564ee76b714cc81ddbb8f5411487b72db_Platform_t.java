 package models;
 
 import javax.persistence.Entity;
 
import play.data.validation.Required;
 import play.db.jpa.Model;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class Platform.
  */
 @Entity
 public class Platform extends Model{
	@Required
 	/** The name. */
	public String name;
 
 	/**
 	 * Instantiates a new platform.
 	 *
 	 * @param name the name
 	 */
 	public Platform(String name) {
 		this.name = name;
 	}
 
 	
 }
