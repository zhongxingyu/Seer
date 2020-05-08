 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.scrumble.server.services;
 
 import com.scrumble.server.entities.Member1;
 import com.scrumble.server.sessionbeans.Member1FacadeLocal;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Produces;
 
 /**
  * REST Web Service
  *
  * @author cyril
  */
 @Path("members")
 @Stateless
 public class MembersResource {
 
     @Context
     private UriInfo context;
     
     @EJB
     private Member1FacadeLocal memberBean;  
 
     /**
      * Creates a new instance of MembersResource
      */
     public MembersResource() {
     }
 
     /**
      * Retrieves representation of an instance of com.scrumble.server.services.MembersResource
      * @return an instance of java.lang.String
      */
     @GET
     @Produces("application/json")
     public String getJson() {
         //TODO return proper representation object
         throw new UnsupportedOperationException();
     }
 
     /**
      * PUT method for updating or creating an instance of MembersResource
      * @param content representation for the resource
      * @return an HTTP response with content of the updated or created resource.
      */
     @PUT
     @Consumes("application/json")
     public void putJson(String content) {
     }
     
     
     /**
      * Retrieves representation of list of com.scrumble.server.entities.Member1 object
      * @return a JSON representation of the list of all members.
      */
     @GET
     @Path("all")
     @Produces("application/json")
     public List<Member1> findAll() {
         return memberBean.findAll();
     }
     
     /**
      * Retrieves representation of a single com.scrumble.server.entities.Member1 object
      * @param id the id of the Member1 object to retrieve
      * @return a JSON representation of the related Member1 object.
      */
     @GET
     @Path("{id}")
     @Produces("application/json")
     public Member1 getMember(@PathParam("id") String id) {
         return memberBean.find(Integer.parseInt(id));
     }
     
     
     /**
      * POST method for creating an instance of Member1 object
      * @param member JSON representation for the Member1 object
      * @return an HTTP response with content of the created resource.
      */
     @POST
     @Path("add")
     @Consumes("application/json")
     @Produces("application/json")
     public void addMember(Member1 member) {
         memberBean.create(member);
     }
     
     /**
      * PUT method for updating an instance of Member1 object
      * @param member JSON representation for the Member1 object
      * @return an HTTP response with content of the created resource.
      */
     @PUT
    @Path("{id}")
     @Consumes("application/json")
     @Produces("application/json")
    public void updateMember(@PathParam("id") String id) {
         System.out.println("PUT");
        Member1 member = memberBean.find(Integer.parseInt(id));
        if (member==null)System.out.println("member is NULL !!!!");
         memberBean.edit(member);
     }
     
 }
