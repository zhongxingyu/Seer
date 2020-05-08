 /**
  * This class represents a Book containing contacts. Contacts are represented by Contact instances
  * which can be added, updated and deleted
  */
 
 package fr.emn.ose.contact;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 @Path("/contacts")
 public class Book {
 
     private HashMap<Integer, Contact> contacts;
     private int compt = 0;
 
     public Book() {
         this.contacts = new HashMap<Integer, Contact>();
     }
 
     /**
      * Generates a new id.
      *
      * Generate a new id by incrementing the former value of id
      * @return the new id
      */
     private int inc(){
         this.compt++;
         return compt;
     }
 
     @OPTIONS()
     @Path("/getAll")
     public Response getAllOption() {
         return Response.ok().
                 header("Access-Control-Allow-Origin", "*").
                 header("Access-Control-Allow-Methods", "GET, OPTIONS").
                 header("Access-Control-Allow-Headers", "Content-Type").build();
     }
 
     /**
      * Return all the contacts which were added to the book.
      *
      * @return the list of contacts to be displayed
      */
     @GET()
     @Produces({MediaType.APPLICATION_JSON})
     @Path("/getAll")
     public Response getAll(){
         Iterator<Integer> it = contacts.keySet().iterator();
         ArrayList<Contact> aRet = new ArrayList<Contact>();
 
         while(it.hasNext()){
             aRet.add(contacts.get(it.next()));
         }
 
         return Response.ok(aRet).header("Access-Control-Allow-Origin", "*").build();
     }
 
     /**
      * The OPTIONS request for CORS “preflighted” request.
      *
      * A preflighted request first sends the OPTIONS header to the
      * resource on the other domain, to check and see if the actual
      * request is safe to send.
      *
      * @return  OK status for POST requests on "addContact".
      */
     @OPTIONS()
     @Path("/addContact")
     public Response addContactOption() {
         return Response.ok().
                 header("Access-Control-Allow-Origin", "*").
                header("Access-Control-Allow-Methods", "POST", "OPTIONS").
                 header("Access-Control-Allow-Headers", "Content-Type").build();
     }
 
     /**
      * Adds the contact to the list.
      *
      * @param c : the contact to be added
      * @return the contact that was added
      */
     @POST()
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces({MediaType.APPLICATION_JSON})
     @Path("/addContact")
     public Response addContact(Contact c){
         c.setId(this.inc());
         this.contacts.put(c.getId(), c);
 
         return Response.ok(c).header("Access-Control-Allow-Origin", "*").build();
     }
 
     /**
      * The OPTIONS request for CORS “preflighted” request.
      *
      * A preflighted request first sends the OPTIONS header to the
      * resource on the other domain, to check and see if the actual
      * request is safe to send.
      *
      * @return  OK status for PUT / DELETE requests on "editContact".
      */
     @OPTIONS()
     @Path("/editContact/{contact}")
     public Response editContactOption() {
         return Response.ok().
                 header("Access-Control-Allow-Origin", "*").
                 header("Access-Control-Allow-Methods", "PUT, DELETE, OPTIONS").
                 header("Access-Control-Allow-Headers", "Content-Type").build();
     }
 
     /**
      * Edit the contact in the book with the new value.
      *
      * @param id  the id of the contact to be updated
      * @param c   the contact that overrides the former value
      * @return    the new contact that overrides the former value
      */
     @PUT()
     @Path("/editContact/{contact}")
     @Consumes({MediaType.APPLICATION_JSON})
     @Produces({MediaType.APPLICATION_JSON})
     public Response editContact(@PathParam("contact")String id, Contact c){
         contacts.put(Integer.parseInt(id), c);
 
         return Response.ok(c).header("Access-Control-Allow-Origin", "*").build();
     }
 
 
     /**
      * Delete the contact corresponding to the id set in the parameter field
      * @param id : the id corresponding to the contact to delete
      * @return
      */
     @DELETE()
     @Path("/editContact/{contact}")
     public Response deleteContact(@PathParam("contact") String id){
         this.contacts.remove(Integer.parseInt(id));
 
         return Response.ok().header("Access-Control-Allow-Origin", "*").build();
     }
 }
