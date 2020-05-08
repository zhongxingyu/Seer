 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.scrumble.server.services;
 
 import com.scrumble.server.entities.Member1;
 import com.scrumble.server.entities.Project;
 import com.scrumble.server.sessionbeans.ProjectFacadeLocal;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 
 /**
  * REST Web Service
  *
  * @author ArNo
  */
 @Path("projects")
 @Stateless
 public class ProjectsResource {
 
     @Context
     private UriInfo context;
 
     @EJB
     private ProjectFacadeLocal projectBean;
     /**
      * Creates a new instance of ProjectsResource
      */
     public ProjectsResource() {
     }
 
     /**
      * Retrieves representation of list of com.scrumble.server.entities.Project object
      * @return a JSON representation of the list of all userstories.
      */
     @GET
     @Path("all")
     @Produces("application/json")
     public List<Project> findAll() {
         return projectBean.findAll();
     }
     
     
     /**
      * Retrieves representation of a single com.scrumble.server.entities.Project object
      * @param id the id of the Project object to retrieve
      * @return a JSON representation of the related Project object.
      */
     @GET
     @Path("search/{pattern}")
     @Produces("application/json")
     public List<Project> searchProjectsQuick(@PathParam("pattern") String pattern) {
         return projectBean.quickSearch(pattern);
     }
     
     
     /**
      * Retrieves representation of a single com.scrumble.server.entities.Project object
      * @param id the id of the Project object to retrieve
      * @return a JSON representation of the related Project object.
      */
     @GET
     @Path("{id}")
     @Produces("application/json")
     public Project getProject(@PathParam("id") String id) {
         return projectBean.find(Integer.parseInt(id));
     }
     
     
     /**
      * POST method for creating an instance of Project object
      * @param project JSON representation for the Project object
      * @return an HTTP response with content of the created resource.
      */
     @POST
     @Path("add")
     @Consumes("application/json")
     @Produces("application/json")
     public Response addProject(Project project) {
         projectBean.create(project);
         
         Response reponse=Response.status(200).build();
         return reponse;
     }
     
     
     /**
      * PUT method for updating an instance of Project object
      * @param project JSON representation for the Project object
      * @return an HTTP response with content of the created resource.
      */
     @PUT
     @Consumes("application/json")
     @Produces("application/json")
     public void updateProject(Project project) {
         projectBean.edit(project);
     }
     
     
     /**
      * Removes a single com.scrumble.server.entities.Project object
      * @param id the id of the Project object to remove
      * @return nothing.
      */
     @DELETE
     @Path("{id}")
     @Produces("application/json")
     public Response removeProject(@PathParam("id") String id) {
         if(projectBean.find(Integer.parseInt(id))!=null)
             projectBean.remove(projectBean.find(Integer.parseInt(id)));
         
         Response reponse=Response.status(200).build();
         return reponse;
     }
     
     
     /**
      * Retrieves the list of a com.scrumble.server.entities.Members linked with the Project object
      * @param id the id of the Project object to retrieve
      * @return a JSON representation of the related Project object.
      */
     @GET
     @Path("{idProject}/members")
     @Produces("application/json")
     public List<Member1> findAllProjectMembers(@PathParam("idProject") String idProject) {
         return projectBean.findAllProjectMembers(Integer.parseInt(idProject));
     }
     
     /**
      * Retrieves the list of a com.scrumble.server.entities.Members linked with the Project object
      * @param id the id of the Project object to retrieve
      * @return a JSON representation of the related Project object.
      */
     @GET
     @Path("{idProject}/members/no")
     @Produces("application/json")
     public List<Member1> findAllNotProjectMembers(@PathParam("idProject") String idProject) {
         return projectBean.findAllNotProjectMembers(Integer.parseInt(idProject));
     }
     
     /**
      * Retrieves the list of a com.scrumble.server.entities.Members linked with the Project object
      * @param id the id of the Project object to retrieve
      * @return a JSON representation of the related Project object.
      */
     @POST
     @Path("{idProject}/members/{idMember}")
     @Consumes("application/json")
     @Produces("application/json")
     public void addMemberToProject(@PathParam("idProject") String idProject, 
                                             @PathParam("idMember") String idMember) {
         projectBean.addMemberToProject(Integer.parseInt(idProject), Integer.parseInt(idMember));
     }
     
     /**
      * Retrieves the list of a com.scrumble.server.entities.Members linked with the Project object
      * @param id the id of the Project object to retrieve
      * @return a JSON representation of the related Project object.
      */
     @DELETE
     @Path("{idProject}/members/{idMember}")
     @Consumes("application/json")
     @Produces("application/json")
     public void removeMemberFromProject(@PathParam("idProject") String idProject, 
                                             @PathParam("idMember") String idMember) {
         projectBean.removeMemberFromProject(Integer.parseInt(idProject), Integer.parseInt(idMember));
     }
     
     /**
      * Retrieves the list of project allowed to a member
      * @param id the id of the member to retrieve
      * @return a JSON representation of the project list
      */
     @GET
    @Path("{login}/project")
     @Consumes("application/json")
     @Produces("application/json")
    public List<Project> findProjectByUser (@PathParam("login") String login) {
        return projectBean.findProjectByUser(login);
     }
     
}
