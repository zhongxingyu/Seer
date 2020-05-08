 package scrumurai.ws.resources;
 
 import java.net.URI;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 import scrumurai.data.EMF;
 import scrumurai.data.entities.Sprint;
 
 import scrumurai.data.entities.User;
 import scrumurai.data.entities.UserStory;
 import scrumurai.data.mapping.DataMapper;
 
 @Path("/userstories")
 public class UserStoryResource implements Resource<UserStory> {
 
     @Context
     UriInfo uriInfo;
     private DataMapper dm = new DataMapper(UserStory.class);
 
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     public Response create(UserStory obj) {
         System.err.println(obj);
         int id = dm.create(obj);
         EntityManager em = EMF.get().createEntityManager();
         em.getTransaction().begin();
         Sprint s = em.find(Sprint.class, obj.getSprint().getId());
         s.setTotal_effort(s.getTotal_effort() + obj.getEffort());
         em.getTransaction().commit();
         em.close();
         if (id > -1) {
             URI uri = uriInfo.getAbsolutePathBuilder().path(Integer.toString(id)).build();
             return Response.created(uri).build();
         } else {
             return Response.status(400).build();
         }
     }
 
     @GET
     @Path("/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response read(@PathParam("id") int id) {
         UserStory u = (UserStory) dm.read(id);
         if (u != null) {
             return Response.ok(dm.read(id)).build();
         } else {
             return Response.status(404).build();
         }
     }
 
     @PUT
     @Path("/{id}")
     @Consumes(MediaType.APPLICATION_JSON)
     public Response update(@PathParam("id") int id, UserStory obj) {
         obj.setId(id);
         if (dm.update(obj)) {
             return Response.status(204).build();
         } else {
             return Response.status(404).build();
         }
     }
     
     @PUT
     @Path("/{id}/state")
     @Consumes(MediaType.APPLICATION_JSON)
     public Response updateState(@PathParam("id") int id, UserStory obj) {
         EntityManager em = EMF.get().createEntityManager();
         UserStory us;
         if ((us = em.find(UserStory.class, id)) != null) {
             em.getTransaction().begin();
             if (obj.getState().equals("completed") && !us.getState().equals("completed")) {
                 Sprint s = em.find(Sprint.class, us.getSprint().getId());
                 s.setProgress(s.getProgress() + us.getEffort());
                 us.setEnd_date(new Date());
             }
             if (!obj.getState().equals("completed") && us.getState().equals("completed")) {
                 Sprint s = em.find(Sprint.class, us.getSprint().getId());
                 s.setProgress(s.getProgress() - us.getEffort());
                 us.setEnd_date(null);
             }
             us.setState(obj.getState());
             em.getTransaction().commit();
             em.close();
             return Response.status(204).build();
         } else {
             return Response.status(404).build();
         }
     }
     
     @PUT
     @Path("/{id}/details")
     @Consumes(MediaType.APPLICATION_JSON)
     public Response updateDetail(@PathParam("id") int id, UserStory obj) {
         EntityManager em = EMF.get().createEntityManager();
         UserStory us;
         if ((us = em.find(UserStory.class, id)) != null) {
             em.getTransaction().begin();
             us.setName(obj.getName());
             us.setDescription(obj.getDescription());
             us.setEffort(obj.getEffort());
             us.setBusiness_value(obj.getBusiness_value());
             Sprint s = em.find(Sprint.class, obj.getSprint().getId());
             us.setSprint(s);
             em.getTransaction().commit();
             em.close();
             return Response.status(204).build();
         } else {
             return Response.status(404).build();
         }
     }
     
     @PUT
     @Path("/{id}/assignee")
     @Consumes(MediaType.APPLICATION_JSON)
     public Response updateAssignee(@PathParam("id") int id, UserStory obj) {
         EntityManager em = EMF.get().createEntityManager();
         UserStory us;
         if ((us = em.find(UserStory.class, id)) != null) {
             em.getTransaction().begin();
             User u = em.find(User.class, obj.getAssignee().getId());
             us.setAssignee(u);
             if(us.getState().equals("to do")) {
                 us.setState("assigned");
             }
             em.getTransaction().commit();
             em.close();
             return Response.status(204).build();
         } else {
             return Response.status(404).build();
         }
     }
 
     @DELETE
     @Path("/{id}")
     public Response delete(@PathParam("id") int id) {
         EntityManager em = EMF.get().createEntityManager();
         em.getTransaction().begin();
         UserStory us = em.find(UserStory.class, id);
         Sprint s = em.find(Sprint.class, us.getSprint().getId());
         s.setTotal_effort(s.getTotal_effort() - us.getEffort());
         if(us.getState().equals("completed")) {
             s.setProgress(s.getProgress() - us.getEffort());
         }
         em.getTransaction().commit();
         em.close();
         if (dm.delete(id)) {
             return Response.status(204).build();
         } else {
             return Response.status(404).build();
         }
     }
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     public Response list() {
         return Response.ok(dm.list()).build();
     }
     
     @GET
     @Path("/release/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response list(@PathParam("id") int id) {
         System.out.println("LIST USER STORY");
         EntityManager em = EMF.get().createEntityManager();
         TypedQuery<UserStory> query = em.createQuery("SELECT e FROM " + UserStory.class.getSimpleName() + " e WHERE e.sprint.id IN (SELECT s.id FROM Sprint s WHERE s.release.id = :release_id) ORDER BY e.end_date", UserStory.class);
         query.setParameter("release_id", id);
         List<UserStory> rs = query.getResultList();
         em.close();
         if (rs.size() > 0) {
             return Response.ok(rs).build();
         } else {
             return Response.status(404).build();
         }
     }
     
     @GET
     @Path("/project/{id}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response listByProject(@PathParam("id") int id) {
         EntityManager em = EMF.get().createEntityManager();
         TypedQuery<UserStory> query = em.createQuery("SELECT x FROM UserStory x WHERE x.project.id = :project_id", UserStory.class);
         query.setParameter("project_id", id);
         List<UserStory> rs = query.getResultList();
         em.close();
         return Response.ok(rs).build();
     }
     
     @GET
    @Path("/todo/{id}")
     @Produces(MediaType.APPLICATION_JSON)
    public Response listToDo(@PathParam("id") int project_id) {
         EntityManager em = EMF.get().createEntityManager();
        TypedQuery<UserStory> query = em.createQuery("SELECT x FROM UserStory x WHERE x.project.id = :project_id AND x.end_date IS null ORDER BY x.sprint.start_date DESC", UserStory.class);
         query.setParameter("project_id", project_id);
         List<UserStory> rs = query.getResultList();
         em.close();
         return Response.ok(rs).build();
     }
 }
