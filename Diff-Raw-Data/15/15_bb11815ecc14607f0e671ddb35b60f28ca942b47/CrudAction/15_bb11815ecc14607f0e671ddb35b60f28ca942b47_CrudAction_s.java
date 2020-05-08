 package br.com.engenhodesoftware.util.ejb3.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.faces.application.FacesMessage;
 
 import org.primefaces.model.LazyDataModel;
 import org.primefaces.model.SortOrder;
 
 import br.com.engenhodesoftware.util.ejb3.application.CrudException;
 import br.com.engenhodesoftware.util.ejb3.application.CrudServiceLocal;
 import br.com.engenhodesoftware.util.ejb3.application.CrudValidationError;
 import br.com.engenhodesoftware.util.ejb3.application.filters.Filter;
 import br.com.engenhodesoftware.util.ejb3.persistence.PersistentObject;
 
 /**
  * Base class for classes that provide controller functionality for CRUD use cases.
  * 
  * This class integrates with the CrudServiceLocal (EJB3) interface and must deal with a subclass of PersistentObject,
  * which is specified as a generic parameter, along with the class of its ID. It provides basic controller functions,
  * mediating the communication between CRUD web pages and CRUD service classes.
  * 
  * <i>This class is part of the Engenho de Software CRUD framework for EJB3 (Java EE 6).</i>
  * 
  * @param <T>
  *          Entity manipulated by the CRUD use case.
  * 
  * @see br.com.engenhodesoftware.util.ejb3.application.CrudServiceLocal
  * @see br.com.engenhodesoftware.util.ejb3.persistence.PersistentObject
  * @author Vitor Souza (vitorsouza@gmail.com)
  * @version 1.1
  */
 public abstract class CrudAction<T extends PersistentObject> extends JSFAction {
 	/** Serialization id. */
 	private static final long serialVersionUID = 1L;
 
 	/** Maximum number of entities to show in each page. */
 	private static final int MAX_ENTITIES_PER_PAGE = 10;
 
 	/** The logger. */
 	private static final Logger logger = Logger.getLogger(CrudAction.class.getCanonicalName());
 
 	/** The view path where the web pages are located. */
 	protected String viewPath;
 
 	/** The name of the message bundle variable to be used in i18n messages. */
 	protected String bundleName;
 
 	/** The resource bundle prefix for messages. */
 	protected String bundlePrefix;
 
 	/** List navigation: the index of the first entity being displayed. */
 	protected int firstEntityIndex = -1;
 
 	/** List navigation: the index of the last entity being displayed. */
 	protected int lastEntityIndex;
 
 	/** List navigation: the number of existing entities in the persistence media. */
 	protected long entityCount;
 
 	/** Output: the list of existing entities. */
 	protected List<T> entities;
 
 	/** Input/Output: the selected entity among the list of existing entities. */
 	protected T selectedEntity;
 
 	/** Primefaces lazy data model for use with a lazy p:dataTable component. */
 	protected LazyDataModel<T> lazyEntities;
 
 	/** Output: if the data is read-only. */
 	protected boolean readOnly = false;
 
 	/** Output: the list of entities to delete. */
 	protected SortedSet<T> trashCan = new TreeSet<T>();
 
 	/** Output: available filters. */
 	protected List<Filter<?>> filters;
 
 	/** Output: filter label, what is shown in the web page (when multiple-choice, avoids displaying the id). */
 	protected String filterLabel;
 
 	/** Input: the selected filter's ID. */
 	protected String filterKey;
 
 	/** Output: the currently selected filter. */
 	protected Filter<?> filter;
 
 	/** Output: a flag indicating if filtering is on or not. */
 	protected boolean filtering = false;
 
 	/** Input: the filter parameter. */
 	protected String filterParam;
 
 	/** Internal control: a map to locate the filter given its field name. */
 	protected Map<String, Filter<?>> filtersMap = new TreeMap<String, Filter<?>>();
 
 	/** Getter for firstEntityIndex. */
 	public int getFirstEntityIndex() {
 		return firstEntityIndex;
 	}
 
 	/** Getter for lastEntityIndex. */
 	public int getLastEntityIndex() {
 		return lastEntityIndex;
 	}
 
 	/** Getter for readOnly. */
 	public boolean isReadOnly() {
 		return readOnly;
 	}
 
 	/** Getter for filters. */
 	public List<Filter<?>> getFilters() {
 		// Lazily initialize the list of filters.
 		if (filters == null) {
 			filters = new ArrayList<Filter<?>>();
 			initFilters();
 		}
 		return filters;
 	}
 
 	/** Getter for filterLabel. */
 	public String getFilterLabel() {
 		return filterLabel;
 	}
 
 	/** Getter for filter. */
 	public Filter<?> getFilter() {
 		return filter;
 	}
 
 	/** Getter for filtering. */
 	public boolean isFiltering() {
 		return filtering;
 	}
 
 	/** Getter for filterKey. */
 	public String getFilterKey() {
 		return filterKey;
 	}
 
 	/** Setter for filterKey. */
 	public void setFilterKey(String filterKey) {
 		this.filterKey = filterKey;
 	}
 
 	/** Getter for filterParam. */
 	public String getFilterParam() {
 		return filterParam;
 	}
 
 	/** Setter for filterParam. */
 	public void setFilterParam(String filterParam) {
 		this.filterParam = filterParam;
 	}
 
 	/** Getter for entityCount. */
 	public long getEntityCount() {
 		return entityCount;
 	}
 
 	/** Getter for entities. */
 	public List<T> getEntities() {
 		if (entities == null)
 			goFirst();
 		return entities;
 	}
 
 	/** Getter for lazyEntities. */
 	public LazyDataModel<T> getLazyEntities() {
 		if (lazyEntities == null) {
 			count();
 			lazyEntities = new LazyDataModel<T>() {
 				private static final long serialVersionUID = -7530065697374570235L;
 				@Override
 				public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
 					firstEntityIndex = first;
 					lastEntityIndex = first + pageSize;
 					retrieveEntities();
 					return entities;
 				}
 			};
 			lazyEntities.setRowCount((int) entityCount);
 		}
 
 		return lazyEntities;
 	}
 
 	/** Getter for trashCan, encapsulated in a List so it can be shown in a dataTable. */
 	public List<T> getTrashCan() {
 		return new ArrayList<T>(trashCan);
 	}
 
 	/** Getter for selectedEntity. */
 	public T getSelectedEntity() {
 		return selectedEntity;
 	}
 
 	/** Setter for selectedEntity. */
 	public void setSelectedEntity(T selectedEntity) {
 		logger.log(Level.INFO, "Entity selected: {0}", selectedEntity);
 		this.selectedEntity = selectedEntity;
 	}
 
 	/**
 	 * Informs to other methods (and web pages) what is the maximum number of entities to be displayed in a listing page.
 	 * This method exists so it can be overridden by subclasses if desired.
 	 * 
 	 * @return The maximum number of entities to be displayed in the page at a time.
 	 */
 	public int getMaxEntitiesPerPage() {
 		return MAX_ENTITIES_PER_PAGE;
 	}
 
 	/**
 	 * Informs to other methods what is the view path where the web pages are to be located. This method may be overridden
 	 * by subclasses if they don't follow the standard naming convention for Crud Actions, which is:
 	 * <code>com.yourdomain.yoursystem.package-name.controller.ManageObjectAction</code> which would lead to a view path
 	 * of <code>/package-name/manageObject/</code>.
 	 * 
 	 * @return The view path string.
 	 */
 	public String getViewPath() {
 		// If this method is not overridden, tries to guess the view path from the class name.
 		if (viewPath == null) {
 			// Starts with the fully-qualified name of the class (package and name).
 			int idx;
 			String pkg = "", service = "";
 			String classFullName = getClass().getCanonicalName();
 
 			// Searches for the name of the package according to the name convention (before ".controller.").
 			idx = classFullName.indexOf(".controller.");
 			if (idx != -1) {
 				pkg = classFullName.substring(0, idx);
 				idx = pkg.lastIndexOf('.');
 				if (idx != -1)
 					pkg = pkg.substring(idx + 1);
 				pkg = "/" + pkg;
 			}
 
 			// Searches for the name of the service according to convention (class name, removing trailing Action).
 			idx = classFullName.lastIndexOf(".");
 			service = (idx == -1) ? classFullName : classFullName.substring(idx + 1);
 			idx = service.indexOf("Action");
 			if (idx != -1)
 				service = service.substring(0, idx);
 			if (service.length() > 1)
 				service = Character.toLowerCase(service.charAt(0)) + service.substring(1);
 
 			// Builds the view path with the name of the package and class.
 			viewPath = pkg + "/" + service + "/";
 			logger.log(Level.INFO, "View path not provided by subclass, thus guessing from naming convention: {0}", viewPath);
 		}
 		return viewPath;
 	}
 
 	/**
 	 * TODO: documentation.
 	 * 
 	 * @return
 	 */
 	public boolean getFacesRedirect() {
 		return false;
 	}
 
 	/**
 	 * Informs to other methods what is the name of the variable that represents the resource bundle with i18n messages.
 	 * This method may be overridden by subclasses if they don't follow the standard naming convention for Crud Actions,
 	 * which is: <code>com.yourdomain.yoursystem.package-name.controller.ManageObjectAction</code> which would lead to a
 	 * bundle variable name of <code>package-name</code>.
 	 * 
 	 * @return The view path string.
 	 */
 	public String getBundleName() {
 		// If this method is not overridden, tries to guess the bundle name from the class name.
 		if (bundleName == null) {
 			// Starts with the fully-qualified name of the class (package and name).
 			int idx;
 			String pkg = "";
 			String classFullName = getClass().getCanonicalName();
 
 			// Searches for the name of the package according to the name convention (before ".controller.").
 			idx = classFullName.indexOf(".controller.");
 			if (idx != -1) {
 				pkg = classFullName.substring(0, idx);
 				idx = pkg.lastIndexOf('.');
 				if (idx != -1)
 					pkg = pkg.substring(idx + 1);
 				pkg = "/" + pkg;
 			}
 
 			// Builds the view path with the name of the package and class.
 			bundleName = pkg;
 			logger.log(Level.INFO, "Bundle name not provided by subclass, thus guessing from naming convention: {0}", bundleName);
 		}
 		return bundleName;
 	}
 
 	/**
 	 * Informs to other methods what is the default prefix for resource bundle messages for this action. This method may
 	 * be overridden by subclasses if they don't follow the standard naming convention for Crud Actions, which is:
 	 * <code>com.yourdomain.yoursystem.package-name.controller.ManageObjectAction</code> which would lead to a prefix of
 	 * <code>manageObject</code>.
 	 * 
 	 * @return The view path string.
 	 */
 	public String getBundlePrefix() {
 		// If the bundle prefix is not specified by the subclass, tries to guess it from the class name.
 		if (bundlePrefix == null) {
 			// Starts with the fully-qualified name of the class (package and name).
 			int idx;
 			String service = "";
 			String classFullName = getClass().getCanonicalName();
 
 			// Searches for the name of the service according to convention (class name, removing trailing Action).
 			idx = classFullName.lastIndexOf(".");
 			service = (idx == -1) ? classFullName : classFullName.substring(idx + 1);
 			idx = service.indexOf("Action");
 			if (idx != -1)
 				service = service.substring(0, idx);
 			if (service.length() > 1)
 				service = Character.toLowerCase(service.charAt(0)) + service.substring(1);
 
 			// The bundle prefix is the name of class adapted as before.
 			bundlePrefix = service;
 			logger.log(Level.INFO, "Bundle prefix not provided by subclass, thus guessing from naming convention: {0}", bundlePrefix);
 		}
 		return bundlePrefix;
 	}
 
 	/**
 	 * Obtains the name of the form field in the HTML page, given the name of the property it represents. It defaults to
 	 * "form:" + propertyName, but it can be overridden in case of need.
 	 * 
 	 * @param propertyName
 	 *          The name of the property.
 	 * @return The name of the field.
 	 */
 	protected String getFieldName(String propertyName) {
 		return "form:" + propertyName;
 	}
 
 	/**
 	 * Provides the CRUD service class to other methods that need it. This method must be overridden by subclasses, each
 	 * one providing its specific CRUD service.
 	 * 
 	 * @return A service class that complies to the CRUD specification.
 	 */
 	protected abstract CrudServiceLocal<T> getCrudService();
 
 	/**
 	 * Method called by the constructor to initialize the entity and any auxiliary properties. Must be overridden by
 	 * subclasses to implement this behavior.
 	 */
 	protected abstract T createNewEntity();
 
 	/**
 	 * Initializes the collection of filters that indicate to the view what kind of filtering can be done with the list of
 	 * entities.
 	 */
 	protected abstract void initFilters();
 
 	/**
 	 * Adds a filter to this controller.
 	 * 
 	 * @param filter
 	 *          The filter object representing a kind of filtering.
 	 */
 	protected void addFilter(Filter<?> filter) {
 		logger.log(Level.INFO, "Adding filter: {0} ({1})", new Object[] { filter.getKey(), filter.getType() });
 		if (this.filter == null)
 			this.filter = filter;
 		filtersMap.put(filter.getKey(), filter);
 		filters.add(filter);
 	}
 
 	/**
 	 * Method called by the retrieve and update scenarios to check if all is OK with the retrieved entity. This method is
 	 * intended to be overridden by subclasses to implement specific behavior, such as management of sub-entities.
 	 */
 	protected void checkSelectedEntity() {
 		logger.log(Level.INFO, "checkSelectedEntity() not overridden by subclass. Doing nothing");
 	}
 
 	/**
 	 * Prepares the entity to be saved. This method is intended to be overridden by subclasses which implement specific
 	 * behavior, such as management of sub-entities.
 	 */
 	protected void prepEntity() {
 		logger.log(Level.INFO, "prepEntity() not overridden by subclass. Doing nothing");
 	}
 
 	/**
 	 * Builds a string with the contents of the trash, so we can inform the user what has been just deleted. The basic
 	 * implementation just returns the size of the trash, therefore it is advised to override it.
 	 * 
 	 * @return A string representation of the contents of the trash.
 	 */
 	protected String listTrash() {
 		logger.log(Level.INFO, "listTrash() not overridden by subclass. Returning trashCan size: {0}", trashCan.size());
 		return "" + trashCan.size();
 	}
 
 	/**
 	 * Retrieves an existing entity from the business layer, given its ID. Sets it as the selected entity. This method is
 	 * intended to be used internally.
 	 */
 	public void retrieveExistingEntity(Long id) {
 		// Checks if we're creating a new entity or updating/visualizing an existing one.
 		if (id != null) {
 			// Retrieve the selected entity again from the business layer. This merges it to the current session and updates
 			// its data, avoiding some problems such as stale data and lazy-loading.
 			logger.log(Level.INFO, "Retrieving from the application layer entity with id {0}", id);
 			selectedEntity = getCrudService().retrieve(id);
 
 			// Check if the entity is sane (no nulls where there shouldn't be).
 			checkSelectedEntity();
 		}
 	}
 
 	/**
 	 * Retrieves the entity count and stores it for future use.
 	 */
 	protected void count() {
 		logger.log(Level.INFO, "Counting entities. Filtering is {0}", (filtering ? "ON" : "OFF"));
 
 		// Checks if there's an active filter.
 		if (filtering)
 			// There is. Count only filtered entities.
 			entityCount = getCrudService().countFiltered(filter, filterParam);
 		else
 		// There's not. Count all entities.
 		entityCount = getCrudService().count();
 
 		// Since the entity count might have changed, force reloading of the lazy entity model.
 		lazyEntities = null;
 
 		// Updates the index of the last entity and checks if it has gone over the limit.
 		lastEntityIndex = firstEntityIndex + MAX_ENTITIES_PER_PAGE;
 		if (lastEntityIndex > entityCount)
 			lastEntityIndex = (int) entityCount;
 	}
 
 	/**
 	 * Retrieves a collection of entities, respecting the selected range. Makes the collection available to the view. This
 	 * method is intended to be used internally.
 	 */
 	protected void retrieveEntities() {
 		// Checks if the last entity index is over the number of entities and correct it.
 		if (lastEntityIndex > entityCount)
 			lastEntityIndex = (int) entityCount;
 
 		// Checks if there's an active filter.
 		if (filtering) {
 			// There is. Retrieve not only within range, but also with filtering.
 			logger.log(Level.INFO, "Retrieving from the application layer {0} of a total of {1} entities: interval [{2}, {3}) using filter \"{4}\" and search param \"{5}\"", new Object[] { (lastEntityIndex - firstEntityIndex), entityCount, firstEntityIndex, lastEntityIndex, filter.getKey(), filterParam });
 			entities = getCrudService().filter(filter, filterParam, firstEntityIndex, lastEntityIndex);
 		}
 		else {
 			// There's not. Retrieve all entities within range.
 			logger.log(Level.INFO, "Retrieving from the application layer {0} of a total of {1} entities: interval [{2}, {3})", new Object[] { (lastEntityIndex - firstEntityIndex), entityCount, firstEntityIndex, lastEntityIndex });
 			entities = getCrudService().list(firstEntityIndex, lastEntityIndex);
 		}
 
 		// Adjusts the last entity index.
 		lastEntityIndex = firstEntityIndex + entities.size();
 	}
 
 	/**
 	 * Sets the indices to the first page of entities and retrieve them.
 	 * 
 	 * This method is intended to be used with AJAX.
 	 */
 	public void goFirst() {
 		// Move the first entity index to zero to show the first page.
 		firstEntityIndex = 0;
 
 		// Always counts the entities in this method, as it can be called via AJAX from the pages. This also sets the last
 		// entity index.
 		count();
 
 		// Retrieve the entities from the application layer.
 		retrieveEntities();
 	}
 
 	/**
 	 * Sets the indices to the previous page of entities and retrieve them.
 	 * 
 	 * This method is intended to be used with AJAX.
 	 */
 	public void goPrevious() {
 		// Only moves to the previous page if there is one.
 		if (firstEntityIndex > 0) {
 			// Shift the first entity index backward by the max number of entities in a page.
 			firstEntityIndex -= MAX_ENTITIES_PER_PAGE;
 
 			// Checks if, by any chance, the above shifting took the first entity index too far and correct it.
 			if (firstEntityIndex < 0)
 				firstEntityIndex = 0;
 
 			// Always counts the entities in this method, as it can be called via AJAX from the pages. This also sets the last
 			// entity index.
 			count();
 
 			// Retrieve the entities from the application layer.
 			retrieveEntities();
 		}
 	}
 
 	/**
 	 * Sets the indices to the next page of entities and retrieve them.
 	 * 
 	 * This method is intended to be used with AJAX.
 	 */
 	public void goNext() {
 		// Always counts the entities in this method, as it can be called via AJAX from the pages.
 		count();
 
 		// Only moves to the next page if there is one.
 		if (lastEntityIndex < entityCount) {
 			// Shift the first entity index forward by the max number of entities in a page.
 			firstEntityIndex += MAX_ENTITIES_PER_PAGE;
 
 			// Set the last entity index to a full page of entities starting from the first index.
 			lastEntityIndex = firstEntityIndex + MAX_ENTITIES_PER_PAGE;
 
 			// Retrieve the entities from the application layer.
 			retrieveEntities();
 		}
 	}
 
 	/**
 	 * Sets the indices to the last page of entities and retrieve them.
 	 * 
 	 * This method is intended to be used with AJAX.
 	 */
 	public void goLast() {
 		// Always counts the entities in this method, as it can be called via AJAX from the pages.
 		count();
 
 		// Checks for the trivial case of no entities.
 		if (entityCount == 0)
 			firstEntityIndex = lastEntityIndex = 0;
 		else {
 			// Calculates how many entities there are in the last page (the remainder of dividing the count by the max
 			// entities in a page).
 			int remainder = ((int) entityCount % MAX_ENTITIES_PER_PAGE);
 
 			// Check if the remainder is zero, in which case the last page is full. Otherwise, the remainder is the number of
 			// entities in
 			// the last page. Sets the first and last index accordingly.
 			firstEntityIndex = (remainder == 0) ? (int) entityCount - MAX_ENTITIES_PER_PAGE : (int) entityCount - remainder;
 			lastEntityIndex = (int) entityCount;
 		}
 
 		// Retrieve the entities from the application layer.
 		retrieveEntities();
 	}
 
 	/** Shortcut to trash(null). */
 	public void trash() {
 		trash(null);
 	}
 
 	/**
 	 * Moves the selected entity to the trash can for possible future deletion.
 	 * 
 	 * This method is intended to be used with AJAX.
 	 */
 	public void trash(Long id) {
 		// Retrieves the existing entity that was selected, if not already done by the JSF component.
 		if (selectedEntity == null)
 			selectedEntity = getCrudService().retrieve(id);
 
 		// Adds the selected entity to the trash can so the user can confirm the deletion.
 		logger.log(Level.INFO, "Adding {0} (id {1}) to the trash can for future deletion.", new Object[] { selectedEntity, id });
 		trashCan.add(selectedEntity);
 	}
 
 	/**
 	 * Cancel deletion and cleans the trash can.
 	 * 
 	 * This method is intended to be used with AJAX.
 	 */
 	public void cancelDeletion() {
 		// Removes all entities from the trash and cancel their deletion.
 		logger.log(Level.INFO, "Deletion has been cancelled. Clearing trash can");
 		trashCan.clear();
 	}
 
 	/**
 	 * Changes the filter so the filter bar can be reloaded properly.
 	 * 
 	 * This method is intended to be used with AJAX.
 	 */
 	public void changeFilter() {
 		// If filtering, cancel it.
 		if (filtering)
 			cancelFilter();
 
 		// Gets the filter from the map and stores in the appropriate property.
 		if ((filterKey != null) && (filterKey.length() > 0)) {
 			logger.log(Level.INFO, "Changing filter to: {0}", filterKey);
 			filterParam = null;
 			filter = filtersMap.get(filterKey);
 			filtering = false;
 		}
 	}
 
 	/**
 	 * Filters the list of entities using a given criteria.
 	 * 
 	 * This method is intended to be used with AJAX.
 	 */
 	public void filter() {
 		// Checks if the necessary parameters are not null or empty.
 		if ((filterKey != null) && (filterKey.length() > 0) && (filterParam != null) && (filterParam.length() > 0)) {
 			// Gets the filter from the map and stores in the appropriate property.
 			logger.log(Level.INFO, "Filtering entities using filter {0} and param \"{1}\"", new Object[] { filterKey, filterParam });
 			filter = filtersMap.get(filterKey);
 			filterLabel = filter.getOptionLabel(filterParam);
 			filtering = true;
 
 			// After performing a search, always go to the first page of entities.
 			goFirst();
 		}
 	}
 
 	/**
 	 * Clears the filtering information.
 	 * 
 	 * This method is intended to be used with AJAX.
 	 */
 	public void cancelFilter() {
 		logger.log(Level.INFO, "Clearing filter information");
 		filtering = false;
 
 		// After canceling a search, always go to the first page of entities.
 		goFirst();
 	}
 
 	/**
 	 * Displays the list of entities, which then provides access to every other CRUD functionality.
 	 * 
 	 * @return The view path for the listing of entities.
 	 */
 	public String list() {
 		logger.log(Level.INFO, "Listing entities...");
 
 		// Gets the entity count.
 		count();
 
 		// Checks if the index of the listing should be changed and reload the page.
 		if (firstEntityIndex < 0)
 			goFirst();
 		else if (lastEntityIndex > entityCount)
 			goLast();
 		else retrieveEntities();
 
 		// Goes to the listing.
 		return getViewPath() + "list.xhtml?faces-redirect=" + getFacesRedirect();
 	}
 
 	/**
 	 * Displays the form for the creation of a new entity.
 	 * 
 	 * @return The view path for the input form.
 	 */
 	public String create() {
 		logger.log(Level.INFO, "Displaying form for entity creation");
 
 		// Sets the data as read-write.
 		readOnly = false;
 
 		// Resets the entity so we can create a new one.
 		selectedEntity = createNewEntity();
 
 		// Goes to the form.
 		return getViewPath() + "form.xhtml?faces-redirect=" + getFacesRedirect();
 	}
 
 	/** Shortcut to retrieve(null). */
 	public String retrieve() {
 		return retrieve(null);
 	}
 
 	/**
 	 * Displays the form with the data of the selected entity. Sets the data as read-only.
 	 * 
 	 * @return The view path for the input form.
 	 */
 	public String retrieve(Long id) {
 		logger.log(Level.INFO, "Displaying form for entity retrieval");
 
 		// Sets the data as read-only.
 		readOnly = true;
 
 		// Retrieves the existing entity that was selected, if not already done by the JSF component.
 		if (selectedEntity == null)
 			retrieveExistingEntity(id);
 		else checkSelectedEntity();
 
 		// Goes to the form.
 		return getViewPath() + "form.xhtml?faces-redirect=" + getFacesRedirect();
 	}
 
 	/** Shortcut to update(null). */
 	public String update() {
 		return update(null);
 	}
 
 	/**
 	 * Displays the form with the data of the selected entity for updating the entity (leaves it read-write).
 	 * 
 	 * @return The view path for the input form.
 	 */
 	public String update(Long id) {
 		logger.log(Level.INFO, "Displaying form for entity update");
 
 		// Sets the data as read-write.
 		readOnly = false;
 
 		// Retrieves the existing entity that was selected, if not already done by the JSF component.
 		if (selectedEntity == null)
 			retrieveExistingEntity(id);
 		else checkSelectedEntity();
 
 		// Goes to the form.
 		return getViewPath() + "form.xhtml?faces-redirect=" + getFacesRedirect();
 	}
 
 	/**
 	 * Saves (create or update) the entity based on the data sent from the form.
 	 * 
 	 * @return The view path of the listing if no problems occurred. Otherwise, return null to go back to the form.
 	 */
 	public String save() {
 		logger.log(Level.INFO, "Saving entity...");
 
 		// Prepare the entity for saving.
 		prepEntity();
 
 		// Checks if we want to create or update the entity. Validates the operation first and stops in case of errors.
 		try {
 			if (selectedEntity.getId() == null) {
 				getCrudService().validateCreate(selectedEntity);
 				getCrudService().create(selectedEntity);
 				addGlobalI18nMessage(getBundleName(), FacesMessage.SEVERITY_INFO, getBundlePrefix() + ".info.createSucceeded");
 			}
 			else {
 				getCrudService().validateUpdate(selectedEntity);
 				getCrudService().update(selectedEntity);
 				addGlobalI18nMessage(getBundleName(), FacesMessage.SEVERITY_INFO, getBundlePrefix() + ".info.updateSucceeded");
 			}
 		}
 		catch (CrudException crudException) {
 			// Adds an error message to each validation error included in the exception.
 			for (CrudValidationError error : crudException) {
 				logger.log(Level.WARNING, "Exception while saving " + selectedEntity, crudException.getMessage());
 
 				// Checks if the field name was specified. If it was, attach the message to the form field.
 				if (error.getFieldName() != null)
 					addFieldI18nMessage(getFieldName(error.getFieldName()), getBundleName(), FacesMessage.SEVERITY_ERROR, error.getMessageKey(), error.getMessageParams());
 				else addGlobalI18nMessage(getBundleName(), FacesMessage.SEVERITY_ERROR, error.getMessageKey(), error.getMessageParams());
 			}
 
 			// Goes back to the same page, i.e., the form.
 			return null;
 		}
 
 		// Goes back to the listing.
 		return list();
 	}
 
 	/**
 	 * Deletes all the entities in the trash can. If there are any problems deleting entities, they will be displayed in
 	 * the listing page afterwards.
 	 * 
 	 * @return The view path of the listing.
 	 */
 	public String delete() {
 		logger.log(Level.INFO, "Deleting entities...");
 		List<Object> notDeleted = new ArrayList<Object>();
 
 		// Deletes the entities that are in the trash can. Validates each exclusion, but don't stop in case of errors.
 		for (T entity : trashCan)
 			try {
 				getCrudService().validateDelete(entity);
 				getCrudService().delete(entity);
 			}
 			catch (CrudException crudException) {
 				// Displays error messages as global. Don't stop the deletion process.
 				logger.log(Level.WARNING, "Exception while deleting " + entity, crudException.getMessage());
 				for (CrudValidationError error : crudException)
 					addGlobalI18nMessage(getBundleName(), FacesMessage.SEVERITY_ERROR, error.getMessageKey(), error.getMessageParams());
 
 				// Marks the entity to be removed from the trash can, so we won't list it in the successfull message.
 				notDeleted.add(entity);
 			}
 
 		// Writes the status message (only if at least one entity was deleted successfully). Empties it afterwards.
 		trashCan.removeAll(notDeleted);
 		if (!trashCan.isEmpty()) {
 			addGlobalI18nMessage(getBundleName(), FacesMessage.SEVERITY_INFO, getBundlePrefix() + ".info.deleteSucceeded", listTrash());
 			trashCan.clear();
 		}
 
 		// Goes back to the listing.
 		return list();
 	}
 }
