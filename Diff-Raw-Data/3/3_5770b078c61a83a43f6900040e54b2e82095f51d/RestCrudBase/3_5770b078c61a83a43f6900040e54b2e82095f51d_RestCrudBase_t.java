 package uk.me.doitto.webapp.ws;
 
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import uk.me.doitto.webapp.dao.AbstractEntity;
 import uk.me.doitto.webapp.dao.Crud;
 
 /**
  * Specializes IRestCrud for Long PKs, specifies an overlay method to selectively allow editing over REST
  * @author super
  *
  * @param <T> the entity type
  */
 @SuppressWarnings("serial")
 public abstract class RestCrudBase<T extends AbstractEntity> implements IRestCrud<T, Long> {
 	
     public static final String COUNT = "count";
     
 	/**
 	 * Copies selected fields from the returned object to a local object
 	 * 
 	 * @param incoming edited entity from client
 	 * @param existing destination object for updated fields
 	 * @return the updated destination object
 	 */
 	protected abstract T overlay (final T incoming, final T existing);
 	
 	protected abstract Crud<T> getService ();
 	
 	@PUT
     @Path("{id}")
     @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     @Override
     public T update (@PathParam("id") final Long id, final T t) {
 		assert id >= 0;
     	assert t != null;
     	return getService().update(overlay(t, getService().find(id)));
     }
     
     @GET
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     @Override
     public List<T> getAll() {
         return getService().findAll();
     }
 
     @GET
     @Path("{first}/{max}")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
 	@Override
 	public List<T> getRange(@PathParam("first") final int first, @PathParam("max") final int max) {
 		assert first >= 0;
 		assert max >= 0;
 		return getService().findAll(first, max);
 	}
 
     @GET
     @Path("{id}")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     @Override
     public T getById (@PathParam("id") final Long id) {
 		assert id >= 0;
 		return getService().find(id);
     }
 
     @DELETE
     @Path("{id}")
     @Override
     public Response delete (@PathParam("id") final Long id) {
 		assert id >= 0;
 		getService().delete(id);
         return Response.ok().build();
     }
 
     @GET
     @Path(COUNT)
     @Produces(MediaType.TEXT_PLAIN)
 	@Override
 	public String count () {
 		return String.valueOf(getService().count());
 	}
 }
