 package com.randombit.uskoci.rest.card;
 
 import com.randombit.uskoci.card.dao.MongoDBCard;
 import com.randombit.uskoci.card.model.Card;
 import org.atmosphere.annotation.Suspend;
 
 import javax.ws.rs.*;
 
 @Path("/card")
 public class CardResource {
 
     /**
      * Suspend the response without writing anything back to the client.
      *
      * @return a white space
      */
     @Suspend(contentType = "application/json")
     @GET
     public String suspend() {
         return "";
     }
 
     /**
      * Get card with requested id ex. /rest/card/1
      * @param id
      * @return Card with requested ID
      */
     @GET
     @Path("{id}")
     @Produces("application/json")
     public Card getCard(@PathParam("id") String id) {
         Card card = MongoDBCard.instance.getModel().get(id);
         if(card==null)
            throw new RuntimeException("Get: Card with " + id +  " not found");
         return card;
     }
 
 }
