 package org.dejava.component.ejb.service;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.inject.Inject;
 
 import org.dejava.component.ejb.dao.AbstractGenericDAO;
 import org.dejava.component.exception.localized.checked.BusinessRuleException;
 
 /**
  * Implements the default behavior of an JPA entity EJB service.
  * 
  * @param <Entity>
  *            Any entity.
  */
 public abstract class AbstractGenericService<Entity> {
 
 	/**
 	 * Entity DAO.
 	 */
 	@Inject
 	private AbstractGenericDAO<Entity> entityDAO;
 
 	/**
 	 * Gets the entityDAO.
 	 * 
 	 * @return The entityDAO.
 	 */
 	public AbstractGenericDAO<Entity> getEntityDAO() {
 		return entityDAO;
 	}
 
 	/**
 	 * Adds or update the persistent entity.
 	 * 
 	 * @param entity
 	 *            The entity to be persisted.
 	 * @return The persisted (and updated) entity.
 	 */
 	public Entity addOrUpdate(final Entity entity) {
		System.out.println("teste 1");
 		// Merges the entity.
 		return getEntityDAO().merge(entity);
 	}
 
 	/**
 	 * Adds or update all the persistent entities.
 	 * 
 	 * @param entities
 	 *            The entities to be persisted.
 	 * @return The persisted (and updated) entity.
 	 */
 	public Collection<Entity> addOrUpdateAll(final Collection<Entity> entities)  {
 		// Creates a new list of entities.
 		final Collection<Entity> mergedEntities = new ArrayList<>();
 		// If the given collection is not empty.
 		if (entities != null) {
 			// For each given entity.
 			for (final Entity currentEntity : entities) {
 				// Tries to add or update the current entity.
 				mergedEntities.add(addOrUpdate(currentEntity));
 			}
 		}
 		// Returns the list of merged entities.
 		return mergedEntities;
 	}
 
 	/**
 	 * Removes a persistent entity.
 	 * 
 	 * @param entity
 	 *            Entity to be removed.
 	 */
 	public void remove(final Entity entity) {
 		// Tries to remove the entity.
 		getEntityDAO().remove(entity);
 	}
 
 	/**
 	 * Removes persistent entities.
 	 * 
 	 * @param entities
 	 *            The entities to be removed.
 	 */
 	public void removeAll(final Collection<Entity> entities) {
 		// If the given collection is not empty.
 		if (entities != null) {
 			// For each given entity.
 			for (final Entity currentEntity : entities) {
 				// Tries to remove the current entity.
 				remove(currentEntity);
 			}
 		}
 	}
 
 	/**
 	 * Gets an entity by its id.
 	 * 
 	 * @param identifier
 	 *            The id of the entity.
 	 * @return An entity by its id.
 	 */
 	public Entity getEntityById(final Object identifier) {
 		// Tries to return the entity.
 		return getEntityDAO().getEntityById(identifier);
 	}
 
 	/**
 	 * Gets all entities with the given attribute value.
 	 * 
 	 * @param attributeName
 	 *            The attribute name.
 	 * @param attributeValue
 	 *            The attribute value.
 	 * @param firstResult
 	 *            The first result that should be considered by the query.
 	 * @param maxResults
 	 *            The maximum numbers of results to be considered by the query.
 	 * @return All entities with the given attribute value.
 	 */
 	public Collection<Entity> getEntitiesByAttribute(final String attributeName, final Object attributeValue,
 			final Integer firstResult, final Integer maxResults) {
 		// Tries to get the entities.
 		return getEntityDAO().getEntitiesByAttribute(attributeName, attributeValue, firstResult, maxResults);
 	}
 
 	/**
 	 * Gets all entities of the kind.
 	 * 
 	 * @param firstResult
 	 *            The first result that should be considered by the query.
 	 * @param maxResults
 	 *            The maximum numbers of results to be considered by the query.
 	 * 
 	 * @return All entities of the kind.
 	 */
 	public Collection<Entity> getAllEntities(final Integer firstResult, final Integer maxResults) {
 		// Tries to get the entities.
 		return getEntityDAO().getAllEntities(firstResult, maxResults);
 	}
 }
