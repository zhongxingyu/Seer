 package models;
 
 import javax.persistence.Entity;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 
import net.sf.oval.constraint.MaxSize;
 
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class Template.
  */
 
 @Entity
 public class Template extends Model{
 	
 	/** The body. */
 	@Lob
 	@Required
	@MaxSize(10000)
 	public String html;
 	
 	/** The description. */
 	@ManyToOne
 	public Platform platform;
 	
 	/** The country. */
 	@ManyToOne
 	public Country country;
 
 	/**
 	 * Instantiates a new template.
 	 *
 	 * @param html the html
 	 * @param platform the platform
 	 * @param country the country
 	 */
 	public Template(String html, Platform platform, Country country) {
 		this.html = html;
 		this.platform = platform;
 		this.country = country;
 	}
 	
 	
 
 }
