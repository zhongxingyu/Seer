 package br.com.engenhodesoftware.sigme.core.controller;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.event.AjaxBehaviorEvent;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.primefaces.event.SelectEvent;
 
 import br.com.engenhodesoftware.sigme.core.application.ManageInstitutionsService;
 import br.com.engenhodesoftware.sigme.core.domain.Institution;
 import br.com.engenhodesoftware.sigme.core.domain.InstitutionType;
 import br.com.engenhodesoftware.sigme.core.domain.Regional;
 import br.com.engenhodesoftware.sigme.core.persistence.RegionalDAO;
 import br.com.engenhodesoftware.util.ejb3.application.CrudServiceLocal;
 import br.com.engenhodesoftware.util.ejb3.application.filters.Criterion;
 import br.com.engenhodesoftware.util.ejb3.application.filters.CriterionType;
 import br.com.engenhodesoftware.util.ejb3.application.filters.LikeFilter;
 import br.com.engenhodesoftware.util.ejb3.application.filters.MultipleChoiceFilter;
 import br.com.engenhodesoftware.util.ejb3.application.filters.ReverseMultipleChoiceFilter;
 import br.com.engenhodesoftware.util.ejb3.controller.CrudAction;
 import br.com.engenhodesoftware.util.people.domain.Address;
 import br.com.engenhodesoftware.util.people.domain.City;
import br.com.engenhodesoftware.util.people.domain.ContactType;
 import br.com.engenhodesoftware.util.people.domain.Telephone;
 import br.com.engenhodesoftware.util.people.persistence.CityDAO;
 import br.com.engenhodesoftware.util.people.persistence.exceptions.MultiplePersistentObjectsFoundException;
 import br.com.engenhodesoftware.util.people.persistence.exceptions.PersistentObjectNotFoundException;
 
 /**
  * Controller class responsible for mediating the communication between user interface and application service for the
  * use case "Manage Institutions".
  * 
  * This use case is a CRUD and, thus, the controller also uses the mini CRUD framework for EJB3..
  * 
  * @author Vitor E. Silva Souza (vitorsouza@gmail.com)
  */
 @Named
 @SessionScoped
 public class ManageInstitutionsAction extends CrudAction<Institution> {
 	/** Serialization id. */
 	private static final long serialVersionUID = 1L;
 
 	/** The logger. */
 	private static final Logger logger = Logger.getLogger(ManageInstitutionsAction.class.getCanonicalName());
 
 	/** The "Manage Institutions" service. */
 	@EJB
 	private ManageInstitutionsService manageInstitutionsService;
 
 	/** The DAO for City objects. */
 	@EJB
 	private CityDAO cityDAO;
 
 	/** The DAO for Regional objects. */
 	@EJB
 	private RegionalDAO regionalDAO;
 
 	/** The controller class that holds references to constant lists, converters, etc. */
 	@Inject
 	private CoreController coreController;
 
 	/** Output: the list of telephone numbers. */
 	private List<Telephone> telephones;
 
 	/** Input: a telephone being added or edited. */
 	private Telephone telephone;
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#getCrudService() */
 	@Override
 	protected CrudServiceLocal<Institution> getCrudService() {
 		// Checks if the current user has the authorization to use this functionality.
 		manageInstitutionsService.authorize();
 
 		return manageInstitutionsService;
 	}
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#createNewEntity() */
 	@Override
 	protected Institution createNewEntity() {
 		logger.log(Level.FINER, "Initializing an empty institution...");
 
 		// Create an empty entity.
 		Institution newEntity = new Institution();
 		newEntity.setAddress(new Address());
 
 		// Create an empty telephone list.
 		telephones = new ArrayList<Telephone>();
 
 		return newEntity;
 	}
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#checkSelectedEntity() */
 	@Override
 	protected void checkSelectedEntity() {
 		logger.log(Level.FINER, "Checking selected institution ({0})...", selectedEntity);
 
 		// The address must not be null.
 		if (selectedEntity.getAddress() == null)
 			selectedEntity.setAddress(new Address());
 
 		// Create the list of telephones with the already existing telephones. Also check for null.
 		if (selectedEntity.getTelephones() == null)
 			selectedEntity.setTelephones(new TreeSet<Telephone>());
 		telephones = new ArrayList<Telephone>(selectedEntity.getTelephones());
 	}
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#initFilters() */
 	@Override
 	protected void initFilters() {
 		logger.log(Level.FINER, "Initializing filter types...");
 
 		// One can filter institutions by name, acronym, city or Regional.
 		addFilter(new LikeFilter("manageInstitutions.filter.byName", "name", getI18nMessage("msgsCore", "manageInstitutions.text.filter.byName")));
 		addFilter(new LikeFilter("manageInstitutions.filter.byAcronym", "acronym", getI18nMessage("msgsCore", "manageInstitutions.text.filter.byAcronym")));
 		addFilter(new LikeFilter("manageInstitutions.filter.byCity", "address.city.name", getI18nMessage("msgsCore", "manageInstitutions.text.filter.byCity")));
 
 		// Regional filter is multiple choice. The options and labels for the options must be supplied.
 		List<Regional> regionals = regionalDAO.retrieveAll();
 		Map<String, String> labels = new LinkedHashMap<String, String>();
 		for (Regional regional : regionals)
 			labels.put(regional.getId().toString(), regional.getName());
 		addFilter(new ReverseMultipleChoiceFilter<Regional>("manageInstitutions.filter.byRegional", "address.city", getI18nMessage("msgsCore", "manageInstitutions.text.filter.byRegional"), regionals, labels, "cities", new Criterion("type.partOfRegional", CriterionType.EQUALS, Boolean.TRUE)));
 
 		// Type filter is multiple choice. The options and labels for the options must be supplied.
 		List<InstitutionType> institutionTypes = coreController.getInstitutionTypes();
 		labels = new LinkedHashMap<String, String>();
 		for (InstitutionType type : institutionTypes)
 			labels.put(type.getId().toString(), type.getType());
 		addFilter(new MultipleChoiceFilter<InstitutionType>("manageInstitutions.filter.byType", "type", getI18nMessage("msgsCore", "manageInstitutions.text.filter.byType"), institutionTypes, labels));
 	}
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#prepEntity() */
 	@Override
 	protected void prepEntity() {
 		logger.log(Level.FINER, "Preparing institution for storage ({0})...", selectedEntity);
 
 		// Inserts the telephone list in the entity.
 		selectedEntity.setTelephones(new TreeSet<Telephone>(telephones));
 
 		// Set the regional again, just in case the type of institution is set or changed after the city.
 		setRegional();
 	}
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#summarizeSelectedEntity() */
 	@Override
 	protected String summarizeSelectedEntity() {
 		return (selectedEntity == null) ? "" : selectedEntity.getAcronym();
 	}
 
 	/** @see br.com.engenhodesoftware.util.ejb3.controller.CrudAction#listTrash() */
 	@Override
 	protected String listTrash() {
 		// List the acronyms of the deleted institutions.
 		StringBuilder acronyms = new StringBuilder();
 		for (Institution entity : trashCan)
 			acronyms.append(entity.getAcronym()).append(", ");
 
 		// Removes the final comma and returns the string.
 		int length = acronyms.length();
 		if (length > 0) acronyms.delete(length - 2, length);
 
 		logger.log(Level.FINE, "List of institutions in the trash can: {0}", acronyms.toString());
 		return acronyms.toString();
 	}
 
 	/**
 	 * Analyzes the name that was given for the institution and, if the acronym field is still empty, suggests a value for
 	 * it based on the given name. 
 	 * 
 	 * This method is intended to be used with AJAX.
 	 */
 	public void suggestAcronym() {
 		// If the name was filled and the acronym is still empty, generate one.
 		String name = selectedEntity.getName();
 		String acronym = selectedEntity.getAcronym();
 		if ((name != null) && ((acronym == null) || (acronym.length() == 0))) {
 			// Generate the acronym joining together all upper-case letters of the name.
 			StringBuilder acronymBuilder = new StringBuilder();
 			char[] chars = name.toCharArray();
 			for (char ch : chars)
 				if (Character.isUpperCase(ch))
 					acronymBuilder.append(ch);
 			selectedEntity.setAcronym(acronymBuilder.toString());
 
 			logger.log(Level.FINE, "Suggested \"{0}\" as acronym for \"{1}\"", new Object[] { selectedEntity.getAcronym(), name });
 		}
 		else logger.log(Level.FINEST, "Acronym not suggested: empty name or acronym already filled (name is \"{0}\", acronym is \"{1}\"", new Object[] { name, acronym });
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
 			logger.log(Level.FINE, "Suggestion for cities beginning with \"{0}\" returned {1} results", new Object[] { query, cities.size() });
 			return cities;
 		}
 		return null;
 	}
 
 	/**
 	 * Handles the event of selection in the auto-complete city field. 
 	 * 
 	 * This method is intended to be used with AJAX.
 	 * 
 	 * @param event
 	 *          The selection event supplied by PrimeFaces' auto-complete component.
 	 */
 	public void handleCitySelection(SelectEvent event) {
		Object obj = event.getObject();
		logger.log(Level.FINER, "Handling select event for city ({0})...", obj);
		
		// If the city has been properly passed by the event, set it in the object so the regional is properly handled.
		if (obj instanceof City) {
			selectedEntity.getAddress().setCity((City)obj);
			logger.log(Level.FINE, "City of institution \"{0}\" has been set to \"{1}\"", new Object[] { selectedEntity, obj });
		}
		
 		setRegional();
 	}
 
 	/**
 	 * Handles the event of selection in the institution type menu. 
 	 * 
 	 * This method is intended to be used with AJAX.
 	 * 
 	 * @param event
 	 *          The change event supplied by JSF's select-one-menu component.
 	 */
 	public void handleInstitutionTypeChange(AjaxBehaviorEvent event) {
 		logger.log(Level.FINER, "Handling change event for institution type ({0})...", event);
 		setRegional();
 	}
 
 	/**
 	 * Sets the regional for the selected institution according to its type and city.
 	 */
 	private void setRegional() {
 		logger.log(Level.FINER, "Setting the regional for institution \"{0}\"...", selectedEntity);
 		// Check the selected entity for null.
 		if (selectedEntity != null) {
 			// Checks if the institution type indicates that the institution is part of a regional.
 			if ((selectedEntity.getType() != null) && (selectedEntity.getType().isPartOfRegional())) {
 				// Check if the city has been set and, if so, retrieve the regional of the city.
 				if ((selectedEntity != null) && (selectedEntity.getAddress() != null) && (selectedEntity.getAddress().getCity() != null)) {
 					try {
 						Regional regional = regionalDAO.retrieveByCity(selectedEntity.getAddress().getCity());
 						selectedEntity.setRegional(regional);
 						logger.log(Level.FINE, "Regional \"{0}\" set for institution \"{1}\"", new Object[] { regional, selectedEntity });
 					}
 					catch (PersistentObjectNotFoundException e) {
 						logger.log(Level.FINE, "Regional not found for city \"{0}\" of institution \"{1}\". Regional set to null.", new Object[] { e.getParams()[0], selectedEntity });
 						selectedEntity.setRegional(null);
 					}
 					catch (MultiplePersistentObjectsFoundException e) {
 						logger.log(Level.WARNING, "Multiple regionals found for city: \"{0}\" of institution \"{1}\". Setting regional to null.", new Object[] { e.getParams()[0], selectedEntity });
 						selectedEntity.setRegional(null);
 					}
 				}
 
 				// The city has not been set.
 				else logger.log(Level.FINEST, "City has not been set for institution \"{0}\" and so the regional can't be retrieved.", selectedEntity);
 			}
 
 			// It's not part of a regional, so set it to null.
 			else {
 				logger.log(Level.FINEST, "Institution \"{0}\" is from a type of institutions that is not part of regionals. Setting regional to null.", selectedEntity);
 				selectedEntity.setRegional(null);
 			}
 		}
 	}
 
 	/** Getter for telephones. */
 	public List<Telephone> getTelephones() {
 		return telephones;
 	}
 
 	/** Setter for telephones. */
 	public void setTelephones(List<Telephone> telephones) {
 		this.telephones = telephones;
 	}
 
 	/** Getter for telephone. */
 	public Telephone getTelephone() {
 		return telephone;
 	}
 
 	/** Setter for telephone. */
 	public void setTelephone(Telephone telephone) {
 		this.telephone = telephone;
 		logger.log(Level.FINEST, "Telephone \"{0}\" has been selected", telephone);
 	}
	
	/** */
	public ContactType getTelephoneType() {
		return (telephone == null) ? null : telephone.getType();
	}
	
	/** */
	public void setTelephoneType(ContactType type) {
		if (telephone != null) telephone.setType(type);
	}
 
 	/**
 	 * Creates a new and empty telephone so the telephone fields can be filled. 
 	 * 
 	 * This method is intended to be used with AJAX, through the PrimeFaces Collector component.
 	 */
 	public void newTelephone() {
 		telephone = new Telephone();
 		logger.log(Level.FINEST, "Empty telephone created as selected telephone");
 	}
 	
	/**
	 * TODO: document this method.
	 */
 	public void resetTelephone() {
 		telephone = null;
 		logger.log(Level.FINEST, "Telephone has been reset -- no telephone is selected");
 	}
 }
