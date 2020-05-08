 package kanbannow.resources;
 
 import kanbannow.core.Card;
 import com.yammer.metrics.annotation.Timed;
 import kanbannow.jdbi.CardDAO;
 
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import java.io.IOException;
 import java.util.List;
 
 @Path("/cards/board")
 @Produces(MediaType.APPLICATION_JSON)
 public class CardResource {
     private CardDAO cardDAO;
 
 
     public CardResource(CardDAO aCardDAO) {
         this.cardDAO = aCardDAO;
     }
 
     @GET
     @Timed
     @Path("{id}")
     public List<Card> getCards(@PathParam("id") int boardId) throws IOException, ClassNotFoundException {
         List<Card> cardList = cardDAO.getPostponedCardForBoard(boardId);
         return cardList;
     }
 
 }
