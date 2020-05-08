 package org.robbins.flashcards.webservices;
 
 import java.beans.BeanInfo;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.util.List;
 
 import javax.ws.rs.DELETE;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.robbins.flashcards.service.GenericJpaService;
 import org.robbins.flashcards.webservices.exceptions.GenericWebServiceException;
 import org.springframework.dao.InvalidDataAccessApiUsageException;
 import org.springframework.data.domain.Sort;
 
 public abstract class AbstractGenericResource <T, Serializable> extends AbstractResource {
 	
 	static Logger logger = Logger.getLogger(AbstractGenericResource.class);
 	
 	protected abstract GenericJpaService<T, Long> getService();
 
 	@GET
 	@Produces("application/json")
	public List<T> list(	@QueryParam("sort") String sort,
 							@DefaultValue("asc") @QueryParam("order") String order) {
 		logger.debug("Entering list()");
 		
 		List<T> entities;
 		
 	    try {
 			// should we sort the list?
 	    	if (!StringUtils.isEmpty(sort)) {
 	    		// get a sorted list
 	    		Sort entitySort = getSort(sort, order);
 				entities = getService().findAll(entitySort);
 			} else {
 				// get an unsorted list
 				entities = getService().findAll();
 			}
 		} catch (InvalidDataAccessApiUsageException e) {
 			logger.error(e);
 			throw new GenericWebServiceException(Response.Status.BAD_REQUEST,
 					"Inavlid sort parameter: '" + sort + "'");
 		} catch (Exception e) {
 			logger.error(e);
 			throw new GenericWebServiceException(Response.Status.BAD_REQUEST,
 					"Unable to 'list' entities");
 		}
 
 	    // did we get any results?
 	    if (entities == null) {
 			throw new GenericWebServiceException(Response.Status.NOT_FOUND,
 					"Entities not found");			
 		}
 		
 		return entities;
 	}
 
 	@GET
 	@Path("/count")
 	public Long count() {
 		try {
 			return getService().count();
 		} catch (Exception e) {
 			throw new GenericWebServiceException(
 					Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
 		}
 	}
 	
 	@GET
 	@Path("/{id}")
 	@Produces("application/json")
	public T findOne(	@PathParam("id") Long id) {
 
 		logger.debug("Entering findOne()");
 
 		T entity = getService().findOne(id);
 
 		if (entity == null) {
 			throw new GenericWebServiceException(Response.Status.NOT_FOUND,
 					"Entity not found: " + id);
 		}
 		return entity;
 	}
 	
 	@POST
 	@Produces("application/json")
 	public T post(T entity) {
 		logger.debug("Entering post()");
 		
 		try {
 			T result = getService().save(entity);
 			return result;
 		} catch (Exception e) {
 			logger.error(e);
 			throw new GenericWebServiceException(Response.Status.INTERNAL_SERVER_ERROR,
 					"Unable to 'post' entity");
 		}
 	}
 	
 	@PUT
 	@Path("/{id}")
 	public Response put(@PathParam("id") Long id, T entity) {
 		logger.debug("Entering put()");
 		
 		try {
 			getService().save(entity);
 		} catch (Exception e) {
 			logger.error(e);
 			throw new GenericWebServiceException(Response.Status.INTERNAL_SERVER_ERROR,
 					"Unable to 'put' entity: " + id);
 		}
 		
 		return Response.noContent().build();
 	}
 	
 	@DELETE
 	@Path("/{id}")
 	public Response delete(@PathParam("id") Long id) {
 		logger.debug("Entering delete()");
 		
 		try {
 			getService().delete(id);
 		} catch (Exception e) {
 			logger.error(e);
 			throw new GenericWebServiceException(Response.Status.INTERNAL_SERVER_ERROR,
 					"Unable to 'delete' entity: " + id);
 		}
 		return Response.noContent().build();
 	}
 
 	@POST
 	@Path("/{id}/update")
 	@Produces("application/json")
 	public Response update(@PathParam("id") Long id, T updatedEntity) {
 		try {
 			// get the original entity from the db
 			T originalEntity = getService().findOne(id);
 
 			// merge the original with the updated entity
 			this.merge(updatedEntity, originalEntity);
 
 			// persist the entity back to the db
 			getService().save(originalEntity);
 
 			// return the response
 			return Response.noContent().build();
 
 		} catch (Exception e) {
 			throw new GenericWebServiceException(
 					Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
 		}
 	}
 	
 	// update the target entity with any non-null field in the source entity
 	private void merge(T source, T target) throws Exception {
 		BeanInfo beanInfo = Introspector.getBeanInfo(source.getClass());
 
 		// Iterate over all the attributes
 		for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
 
 			// Only copy writable attributes
 			if (descriptor.getWriteMethod() != null) {
 				Object newValue = descriptor.getReadMethod().invoke(source);
 
 				// Only copy values values where the destination values is not
 				// null
 				if (newValue != null) {
 					// don't bother updating the 'identity' and 'version' fields
 					// which are part of the AbstractPersistentObject
 					if (!descriptor.getName().equals("identity")
 							&& !descriptor.getName().equals("version")) {
 						logger.debug("Updating field '" + descriptor.getName()
 								+ "' to: '" + newValue + "'");
 						descriptor.getWriteMethod().invoke(target, newValue);
 					}
 				}
 			}
 		}
 	}
 }
