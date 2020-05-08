 package co.speechat.rest;
 
 import co.speechat.data.ChatHouse;
 import co.speechat.data.ChatHouseImpl;
 import co.speechat.data.ChatRoom;
 import com.sun.jersey.api.NotFoundException;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 /**
  * Author: Iurii Lytvynenko
  */
 public class AdminResource {
     private ChatHouse chatHouse;
 
     public AdminResource() {
         chatHouse = ChatHouseImpl.get();
     }
 
     @POST
     @Path("/{room}")
     public void createRoom(@PathParam("room") String room) {
         if (room == null || room.isEmpty())
             throw new NotFoundException();
         chatHouse.createRoom(room);
     }
 
     @GET
     @Path("/{room}/users")
     @Produces(MediaType.TEXT_PLAIN)
    public String getUserCount(@PathParam("room") String room) {
         ChatRoom chatRoom = chatHouse.getRoom(room);
        return "" + (chatRoom != null ? chatRoom.getUsersCount() : 0);
     }
 }
