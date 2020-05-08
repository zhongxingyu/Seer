 package resources;
 
 import model.Person;
 
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.xml.bind.JAXBElement;
 
 import dao.PersonDao;
 
 @Stateless
 @LocalBean
 public class PersonResource {
 	@Context
 	UriInfo uriInfo;
 	@Context
 	Request request;
 	
 	EntityManager entityManager;
 	
 	Long id;
 
 	public PersonResource(UriInfo uriInfo, Request request,Long id, EntityManager em) {
 		this.uriInfo = uriInfo;
 		this.request = request;
 		this.id = id;
 		this.entityManager = em;
 	}
 
 	// Application integration
 	@GET
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public Person getPerson() {
 		Person person = this.getPersonById(id);
 		if (person == null)
 			throw new RuntimeException("Get: Person with " + id + " not found");
 		return person;
 	}
 
 	// for the browser
 	@GET
 	@Produces(MediaType.TEXT_XML)
 	public Person getPersonHTML() {
 		Person person = this.getPersonById(id);
 		System.out.println("Person... " + person.toString());
 		if (person == null)
 			throw new RuntimeException("Get: Person with " + id + " not found");
 
 		System.out.println("Returning person... " + person.toString());
 		return person;
 	}
 
 	@PUT
 	@Consumes(MediaType.APPLICATION_XML)
 	public Response putPerson(JAXBElement<Person> person) {
 		Person c = person.getValue();
 		return putAndGetResponse(c);
 	}
 
 	@DELETE
 	public void deletePerson() {
 		Person c = getPersonById(id);
 		if (c == null)
 			throw new RuntimeException("Delete: Person with " + id
 					+ " not found");
 
 		entityManager.remove(c);
 	}
 
 	private Response putAndGetResponse(Person person) {
 		Response res;
 		
 		Person existing = getPersonById(person.getIdPerson());
 		
 		if (existing == null) {
 			res = Response.noContent().build();
 		} else {
 			res = Response.created(uriInfo.getAbsolutePath()).build();
 		}
 
 		entityManager.merge(person);
 		return res;
 	}
 	
 	public Person getPersonById(Long personId) {
		Person person = entityManager.find(Person.class, personId);
 		return person;
 	}
 }
