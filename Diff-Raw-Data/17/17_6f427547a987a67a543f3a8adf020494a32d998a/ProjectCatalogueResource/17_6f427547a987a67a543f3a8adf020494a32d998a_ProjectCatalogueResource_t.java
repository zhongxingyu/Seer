 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.adde.webbapp.frontend.rest;
 
 import com.adde.webbapp.model.dao.DAOFactory;
 import com.adde.webbapp.model.dao.PersonCatalogue;
 import com.adde.webbapp.model.dao.ProjectCatalogue;
 import com.adde.webbapp.model.entity.Person;
 import com.adde.webbapp.model.entity.Project;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.QueryParam;
 
 /**
  *
  * @author Joakim
  */
 @Path("projects")
 public class ProjectCatalogueResource {
 
     private final ProjectCatalogue projectCatalogue = DAOFactory.getDAOFactory().getProjectCatalogue();
     @Context
     private UriInfo uriInfo;
     @Context
     HttpServletRequest request;
 
     @GET
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response get() {
         List<Project> projects = projectCatalogue.getAll();
         return Response.ok(toProjectProxy(projects)).build();
     }
 
     @GET
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     @Path("{id}")
     public Response find(@PathParam("id") long id) {
         Project project = projectCatalogue.find(id);
         GenericEntity<ProjectProxy> ge = new GenericEntity<ProjectProxy>(new ProjectProxy(project)) {
         };
 
         if (project != null) {
             return Response.ok(ge).build();
         } else {
             return Response.noContent().build();
         }
     }
 
     @POST
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     public Response add(@FormParam("name") String name) {
        //HttpSession session = request.getSession(true);
        //Person person = (Person) session.getAttribute("person");
        // OBS ENDAST PERSON FÖR TEST
        Person person = new Person("apan", "apansson", "banana");
        PersonCatalogue.newInstance().add(person);
         Project p = new Project(name, person);
         try {
             projectCatalogue.add(p);
 
             URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(p.getId())).build(p);
             return Response.created(uri).build();
         } catch (IllegalArgumentException e) {
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 
     @DELETE
     @Path("{id}")
     public Response remove(@PathParam("id") long id) {
         try {
             projectCatalogue.remove(id);
             return Response.ok().build();
         } catch (IllegalArgumentException e) {
             return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
         }
     }
 
     @PUT
     @Path("{id}")
     @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response update(@PathParam("id") Long id, @FormParam("name") String name) {
         Project oldProject = projectCatalogue.find(id);
         if (oldProject != null) {
             oldProject.setName(name);
             projectCatalogue.update(oldProject);
             return Response.ok(new ProjectProxy(oldProject)).build();
         }
         return Response.noContent().build();
     }
 
     @GET
     @Path("count")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response getCount() {
         return Response.ok(new PrimitiveJSONWrapper(projectCatalogue.getCount())).build();
     }
 
     @GET
     @Path("range")
     @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
     public Response getRange(@QueryParam("first") int first,
             @QueryParam("nItems") int nItems) {
         List<Project> projects = projectCatalogue.getRange(first, nItems);
         return Response.ok(toProjectProxy(projects)).build();
     }
 
     private GenericEntity<List<ProjectProxy>> toProjectProxy(List<Project> projects) {
         List<ProjectProxy> projectProxies = new ArrayList<>();
         for (Project p : projects) {
             projectProxies.add(new ProjectProxy(p));
         }
         GenericEntity<List<ProjectProxy>> ge = new GenericEntity<List<ProjectProxy>>(projectProxies) {
         };
         return ge;
     }
 }
