 package br.com.engenhodesoftware.sigme.core.application;
 
 import java.io.Serializable;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.ejb.EJB;
 import javax.ejb.Singleton;
 import javax.inject.Named;
 
 import br.com.engenhodesoftware.sigme.core.domain.InstitutionType;
 import br.com.engenhodesoftware.sigme.core.domain.Regional;
 import br.com.engenhodesoftware.sigme.core.persistence.InstitutionTypeDAO;
 import br.com.engenhodesoftware.sigme.core.persistence.RegionalDAO;
 import br.com.engenhodesoftware.util.people.domain.ContactType;
 import br.com.engenhodesoftware.util.people.persistence.ContactTypeDAO;
 
 /**
  * Singleton bean that stores in memory information that is useful for the entire application, i.e., read-only
  * information shared by all users. This bean stores information for the core package.
  * 
  * @author Vitor Souza (vitorsouza@gmail.com)
  */
 @Singleton
 @Named("coreInfo")
 public class CoreInformation implements Serializable {
 	/** Serialization id. */
 	private static final long serialVersionUID = 1L;
 
 	/** The logger. */
 	private static final Logger logger = Logger.getLogger(CoreInformation.class.getCanonicalName());
 
 	/** The DAO for InstitutionType objects. */
 	@EJB
 	private InstitutionTypeDAO institutionTypeDAO;
 
 	/** The DAO for ContactType objects. */
 	@EJB
 	private ContactTypeDAO contactTypeDAO;
 
 	/** The DAO for Regional objects. */
 	@EJB
 	private RegionalDAO regionalDAO;
 
 	/** Indicates if the system is properly installed. */
 	private Boolean systemInstalled;
 
 	/** Indicates the decorator being used in the administration area. */
	private String decorator = "criterion";
 
 	/** The list of institution types (cache of objects that don't change very often). */
 	private SortedSet<InstitutionType> institutionTypes;
 
 	/** The list of telephone types (cache of objects that don't change very often). */
 	private SortedSet<ContactType> contactTypes;
 
 	/** The list of regionals (cache of objects that don't change very often). */
 	private SortedSet<Regional> regionals;
 
 	static {
 		// FIXME: Check if we still need this after migrating from Seam to Java EE 6.
 		// See http://www.jboss.com/index.html?module=bb&op=viewtopic&p=4025747#4025747
 		// TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
 	}
 
 	/** Getter for systemInstalled. */
 	public Boolean getSystemInstalled() {
 		return isSystemInstalled();
 	}
 
 	/** Getter for systemInstalled. */
 	public Boolean isSystemInstalled() {
 		// If not done before, checks if the system has been installed.
 		if (systemInstalled == null) {
 			logger.log(Level.FINER, "Checking if the system is properly installed...");
 
 			// The system is propertly installed if the basic information on cities, states, regions, institution types, etc.
 			// have already been
 			// included in the database. As a convention, we check for institution types.
 			long count = institutionTypeDAO.retrieveCount();
 			systemInstalled = (count > 0);
 			logger.log(Level.FINER, "systemInstalled = {0}", systemInstalled);
 		}
 		return systemInstalled;
 	}
 
 	/** Setter for systemInstalled. */
 	public void setSystemInstalled(Boolean systemInstalled) {
 		this.systemInstalled = systemInstalled;
 	}
 
 	/** Getter for decorator. */
 	public String getDecorator() {
 		// FIXME: allow for change of decorator later.
 		return decorator;
 	}
 
 	/** Getter for institutionTypes. */
 	public SortedSet<InstitutionType> getInstitutionTypes() {
 		// If the institution types haven't yet been loaded, load them.
 		if (institutionTypes == null) {
 			institutionTypes = new TreeSet<InstitutionType>();
 			institutionTypes.addAll(institutionTypeDAO.retrieveAll());
 			logger.log(Level.FINE, "Loaded {0} institution types.", institutionTypes.size());
 		}
 		return institutionTypes;
 	}
 
 	/**
 	 * Invalidates the list of institution types so it can be reloaded.
 	 */
 	public void invalidateInstitutionTypes() {
 		institutionTypes = null;
 	}
 
 	/** Getter for contactTypes. */
 	public SortedSet<ContactType> getContactTypes() {
 		// If the contact types haven't yet been loaded, load them.
 		if (contactTypes == null) {
 			contactTypes = new TreeSet<ContactType>();
 			contactTypes.addAll(contactTypeDAO.retrieveAll());
 			logger.log(Level.FINE, "Loaded {0} contact types.", contactTypes.size());
 		}
 		return contactTypes;
 	}
 
 	/** Getter for regionals. */
 	public SortedSet<Regional> getRegionals() {
 		// If the regionals haven't yet been loaded, load them.
 		if (regionals == null) {
 			regionals = new TreeSet<Regional>();
 			regionals.addAll(regionalDAO.retrieveAll());
 			logger.log(Level.FINE, "Loaded {0} regionals.", regionals.size());
 		}
 		return regionals;
 	}
 }
