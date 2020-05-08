 package introsde.crud.rest.resources;
 
 import introsde.crud.rest.dao.PersonDao;
 import introsde.crud.rest.model.Person;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Request;
 import javax.ws.rs.core.UriInfo;
 
 
 //Will map the resource to the URL /person
 @Path("/person")
 public class PeopleResource {
 
 	// Allows to insert contextual objects into the class,
 	// e.g. ServletContext, Request, Response, UriInfo
 	@Context
 	UriInfo uriInfo;
 	@Context
 	Request request;
 
 	// Return the list of people to the user in the browser
 	@GET
 	@Produces(MediaType.TEXT_XML)
 	public List<Person> getPersonsBrowser() {
 		List<Person> people = new ArrayList<Person>();
 		people.addAll(PersonDao.instance.getModel().values());
 		return people;
 	}
 
 	// Return the list of people for applications
 	@GET
 	@Produces({ MediaType.APPLICATION_XML})
 	public List<Person> getPersonListXML() {
 		List<Person> people = new ArrayList<Person>();
 		people.addAll(PersonDao.instance.getModel().values());
 		return people;
 	}
 	
 
 	// Return the list of people for applications
 	@GET
 	@Produces({ MediaType.APPLICATION_JSON })
 	public List<Person> getPersonListJson() {
 		List<Person> people = new ArrayList<Person>();
 		people.addAll(PersonDao.instance.getModel().values());
 		return people;
 	}
 
 	// retuns the number of people
 	// to get the total number of records
 	@GET
 	@Path("count")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String getCount() {
 		System.out.println("Getting count...");
 		int count = PersonDao.instance.getModel().size();
 		return String.valueOf(count);
 	}
 
 	@POST
 	@Produces(MediaType.APPLICATION_XML)
 	@Consumes(MediaType.APPLICATION_XML)
 	public Person newPerson(Person person) throws IOException {
 		System.out.println("Creating new person...");
 		int count = PersonDao.instance.getModel().size();
 		String newId = count+1+"";
 		person.setId(newId);
 		PersonDao.instance.getModel().put(newId, person);
 		return person;
 	}
 	
 	// Defines that the next path parameter after the base url is
 	// treated as a parameter and passed to the PersonResources
	// Allows to type http://localhost:5900/base_url/1
	// 1 will be treated as parameter todo and passed to PersonResource
 	@Path("{personId}")
 	public PersonResource getPerson(@PathParam("personId") String id) {
 		return new PersonResource(uriInfo, request, id);
 	}
 
 }
