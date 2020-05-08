 package uk.frequencymobile.flow.server.service;
 
 import java.lang.reflect.ParameterizedType;
 import java.net.URI;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import uk.frequencymobile.flow.server.data.GenericDAO;
 import uk.frequencymobile.flow.server.model.GenericEntity;
 
 
 /**
  * @author Victor Basso
  * TODO inject the DAO from spring
  */
 @SuppressWarnings("unchecked")
 public class GenericService<T extends GenericEntity> {
 
 	Class<T> entityClass;
 	GenericDAO<T> dao;
 	
 	@Context UriInfo uriInfo;
 	@Context Request request;
 	
 	public GenericService(GenericDAO<T> dao) {
         this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
         this.dao = dao;
      }
 	
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<T> findAll(){
		/*TRACE*/System.out.println("findAll()");
 		List<T> list = dao.findAll();
 		return list;
 	}
 	
 	@GET
 	@Path("/{id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	public T findById(@PathParam("id") long id) {
 		T user = dao.findById(id);
 		return user;
 	}
 	
 	@PUT
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.TEXT_PLAIN)
 	public Response create(T entity){
 		dao.makePersistent(entity);
 		URI uri = uriInfo.getBaseUriBuilder().path(""+entity.getId()).build(); //TODO missing the enitity type in path
 		return Response.created(uri).build();
 	}
 	
 }
