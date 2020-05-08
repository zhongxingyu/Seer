 package br.com.engenhodesoftware.sigme.core.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Named;
 
 import br.com.engenhodesoftware.sigme.core.application.ManageSpiritistsService;
 import br.com.engenhodesoftware.sigme.core.domain.Attendance;
 import br.com.engenhodesoftware.sigme.core.domain.Institution;
 import br.com.engenhodesoftware.sigme.core.domain.Spiritist;
 import br.com.engenhodesoftware.sigme.core.persistence.InstitutionDAO;
 import br.com.engenhodesoftware.util.ejb3.application.CrudServiceLocal;
 import br.com.engenhodesoftware.util.ejb3.application.filters.Criterion;
 import br.com.engenhodesoftware.util.ejb3.application.filters.CriterionType;
 import br.com.engenhodesoftware.util.ejb3.application.filters.LikeFilter;
 import br.com.engenhodesoftware.util.ejb3.application.filters.ManyToManyFilter;
 import br.com.engenhodesoftware.util.ejb3.controller.CrudAction;
 import br.com.engenhodesoftware.util.people.domain.Address;
 import br.com.engenhodesoftware.util.people.domain.City;
 import br.com.engenhodesoftware.util.people.domain.Telephone;
 import br.com.engenhodesoftware.util.people.persistence.CityDAO;
 
 /**
  * Controller class responsible for mediating the communication between user interface and application service for the
  * use case "Manage Spiritist".
  * 
  * This use case is a CRUD and, thus, the controller also uses the mini CRUD framework for EJB3..
  * 
  * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
  */
 @Named
 @SessionScoped
 public class ManageSpiritistsAction extends CrudAction<Spiritist> {
 	/** Serialization id. */
 	private static final long serialVersionUID = 1L;
 
 	/** The logger. */
 	private static final Logger logger = Logger.getLogger(ManageSpiritistsAction.class.getCanonicalName());
 
 	/** The "Manage Spiritist" service. */
 	@EJB
 	private ManageSpiritistsService manageSpiritistsService;
 
 	/** The DAO for City objects. */
 	@EJB
 	private CityDAO cityDAO;
 
 	/** The DAO for Institution objects. */
 	@EJB
 	private InstitutionDAO institutionDAO;
 
 	/** Output: the list of telephone numbers. */
 	private List<Telephone> telephones;
 
 	/** Input: the telephone being registered. */
 	private Telephone selectedTelephone;
 
 	/** Output: the list of attendances. */
 	private List<Attendance> attendances;
 
 	/** Input: the attendance being edited. */
 	private Attendance selectedAttendance;
 
 	/** Input: the new password to set. */
 	private String newPassword;
 
 	/** Getter for selectedTelephone. */
 	public Telephone getSelectedTelephone() {
 		return selectedTelephone;
 	}
 
 	/** Setter for selectedTelephone. */
 	public void setSelectedTelephone(Telephone selectedTelephone) {
 		this.selectedTelephone = selectedTelephone;
 	}
 
 	/** Getter for selectedAttendance. */
 	public Attendance getSelectedAttendance() {
 		return selectedAttendance;
 	}
 
 	/** Setter for selectedAttendance. */
 	public void setSelectedAttendance(Attendance selectedAttendance) {
 		this.selectedAttendance = selectedAttendance;
 	}
 
 	/** Getter for newPassword. */
 	public String getNewPassword() {
 		return newPassword;
 	}
 
 	/** Setter for newPassword. */
 	public void setNewPassword(String newPassword) {
 		this.newPassword = newPassword;
 	}
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#getCrudService() */
 	@Override
 	protected CrudServiceLocal<Spiritist> getCrudService() {
 		return manageSpiritistsService;
 	}
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#getFacesRedirect() */
 	@Override
 	public boolean getFacesRedirect() {
 		return true;
 	}
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#getBundleName() */
 	@Override
 	public String getBundleName() {
 		return "msgsCore";
 	}
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#createNewEntity() */
 	@Override
 	protected Spiritist createNewEntity() {
 		logger.log(Level.INFO, "Initializing an empty spiritist");
 
 		// Create an empty entity.
 		Spiritist newEntity = new Spiritist();
 		newEntity.setAddress(new Address());
 
 		// Create empty telephone and attendance lists.
 		telephones = new ArrayList<Telephone>();
 		attendances = new ArrayList<Attendance>();
 
 		return newEntity;
 	}
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#checkSelectedEntity() */
 	@Override
 	protected void checkSelectedEntity() {
 		logger.log(Level.INFO, "Checking selected spiritist: {0}", selectedEntity);
 
 		// The address must not be null.
 		if (selectedEntity.getAddress() == null)
 			selectedEntity.setAddress(new Address());
 
 		// Create the list of telephones with the already existing telephones. Also check for null.
 		if (selectedEntity.getTelephones() == null)
 			selectedEntity.setTelephones(new TreeSet<Telephone>());
 		telephones = new ArrayList<Telephone>(selectedEntity.getTelephones());
 
 		// Same for attendances.
 		if (selectedEntity.getAttendances() == null)
 			selectedEntity.setAttendances(new TreeSet<Attendance>());
 		attendances = new ArrayList<Attendance>(selectedEntity.getAttendances());
 	}
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#initFilters() */
 	@Override
 	protected void initFilters() {
 		logger.log(Level.INFO, "Initializing filter types");
 
 		// FIXME: add filter by attendance!
 		// One can filter spiritists by name or e-mail.
 		addFilter(new LikeFilter("manageSpiritists.filter.byName", "name", getI18nMessage("msgsCore", "manageSpiritists.text.filter.byName")));
 		addFilter(new LikeFilter("manageSpiritists.filter.byEmail", "email", getI18nMessage("msgsCore", "manageSpiritists.text.filter.byEmail")));
 		addFilter(new ManyToManyFilter("manageSpiritists.filter.byActiveAttendance", "attendances", getI18nMessage("msgsCore", "manageSpiritists.text.filter.byActiveAttendance"), "institution.name, institution.acronym", true, new Criterion("endDate", CriterionType.IS_NULL)));
 		addFilter(new ManyToManyFilter("manageSpiritists.filter.byInactiveAttendance", "attendances", getI18nMessage("msgsCore", "manageSpiritists.text.filter.byInactiveAttendance"), "institution.name, institution.acronym", true, new Criterion("endDate", CriterionType.IS_NOT_NULL)));
 	}
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#prepEntity() */
 	@Override
 	protected void prepEntity() {
 		logger.log(Level.INFO, "Preparing spiritist for storage: {0}", selectedEntity);
 
 		// Sets the new password.
 		selectedEntity.setPassword(newPassword);
 
 		// Inserts telephone and attendance lists in the entity.
 		selectedEntity.setTelephones(new TreeSet<Telephone>(telephones));
 		selectedEntity.setAttendances(new TreeSet<Attendance>(attendances));
 	}
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#listTrash() */
 	@Override
 	protected String listTrash() {
 		// List the short names of the deleted spiritists.
 		StringBuilder acronyms = new StringBuilder();
 		for (Spiritist entity : trashCan) {
 			acronyms.append(entity.getShortName()).append(", ");
 		}
 
 		// Removes the final comma and returns the string.
 		int length = acronyms.length();
 		if (length > 0)
 			acronyms.delete(length - 2, length);
 
 		logger.log(Level.INFO, "Listing the spiritists in the trash can: {0}", acronyms.toString());
 		return acronyms.toString();
 	}
 
 	/**
 	 * Analyzes the name that was given to the spiritist and, if the short name field is still empty, suggests a value for
 	 * it based on the given name. 
 	 * 
 	 * This method is intended to be used with AJAX.
 	 */
 	public void suggestShortName() {
 		// If the name was filled and the short name is still empty, suggest the first name as short name.
 		String name = selectedEntity.getName();
 		String shortName = selectedEntity.getShortName();
 		if ((name != null) && ((shortName == null) || (shortName.length() == 0))) {
 			int idx = name.indexOf(" ");
 			selectedEntity.setShortName((idx == -1) ? name : name.substring(0, idx).trim());
 
 			logger.log(Level.INFO, "Suggesting {0} as short name for {1}", new Object[] { selectedEntity.getShortName(), name });
 		}
 
 		else logger.log(Level.INFO, "Short name not suggested: name = {0}, shortName = {1}", new Object[] { name, shortName });
 	}
 
 	/**
 	 * Analyzes what has been written so far in the city field and, if not empty, looks for cities that start with the
 	 * given name and returns them in a list, so a dynamic pop-up list can be displayed. This method is intended to be
 	 * used with AJAX.
 	 * 
 	 * @param query What has been written so far in the city field.
 	 * 
 	 * @return The list of City objects whose names match the specified query.
 	 */
 	public List<City> suggestCities(String query) {
 		// Checks if something was indeed typed in the field.
 		if (query.length() > 0) {
 			// Uses the DAO to find the query and returns.
 			List<City> cities = cityDAO.findByName(query);
 			logger.log(Level.INFO, "Searching for cities beginning with \"{0}\" returned {1} results", new Object[] { query, cities.size() });
 			return cities;
 		}
 		return null;
 	}
 	
 	/**
 	 * Adds a new and empty telephone to the list of telephones of the spiritist, so its fields can be filled. This method
 	 * is intended to be used with AJAX.
 	 */
 	public void newTelephone() {
 		logger.log(Level.INFO, "Adding a new telephone to the list");
 		selectedTelephone = new Telephone();
 		telephones.add(selectedTelephone);
 	}
 
 	/**
 	 * Removes one of the telephones of the list of telephones of the spiritist. This method is intended to be used with
 	 * AJAX.
 	 */
 	public void removeTelephone() {
 		logger.log(Level.INFO, "Removing a telephone from the list: {0}", selectedTelephone);
 		telephones.remove(selectedTelephone);
 	}
 
 	/**
 	 * Analyzes what has been written so far in the institution field and, if not empty, looks for institutions that start
 	 * with the given name or acronym and returns them in a list, so a dynamic pop-up list can be displayed. This method
 	 * is intended to be used with AJAX.
 	 * 
 	 * @param event
 	 *          The AJAX event.
 	 * @return The list of institutions to be displayed in the drop-down auto-completion field.
 	 */
 	public List<Institution> suggestInstitutions(Object event) {
 		if (event != null) {
 			String param = event.toString();
 			if (param.length() > 0) {
 				List<Institution> suggestions = institutionDAO.findByNameOrAcronym(param);
 				logger.log(Level.INFO, "Searching for institutions with name or acronym beginning with \"{0}\" returned {1} results", new Object[] { param, suggestions.size() });
 				return suggestions;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Adds a new and empty attendance to the list of attendances of the spiritist, so its fields can be filled. This
 	 * method is intended to be used with AJAX.
 	 */
 	public void newAttendance() {
 		logger.log(Level.INFO, "Adding a new attendance to the list");
 		selectedAttendance = new Attendance();
 		attendances.add(selectedAttendance);
 	}
 
 	/**
 	 * Removes one of the attendances of the list of attendances of the spiritist. This method is intended to be used with
 	 * AJAX.
 	 */
 	public void removeAttendance() {
 		logger.log(Level.INFO, "Removing an attendance from the list: {0}", selectedAttendance);
 		attendances.remove(selectedAttendance);
 	}
 
 	/**
 	 * Unsets the institution of a given attendance so it can be changed. 
 	 * 
 	 * This method is intended to be used with AJAX.
 	 */
 	public void removeInstitutionFromAttendance() {
 		logger.log(Level.INFO, "Unsetting the institution {0} from the attendance {1}", new Object[] { selectedAttendance.getInstitution(), selectedAttendance });
 		selectedAttendance.setInstitution(null);
 	}
 }
