 package com.ninedemons.leaderboard.remoting;
 
 import com.ninedemons.leaderboard.api.Leaderboard;
 import com.ninedemons.leaderboard.api.Page;
 import org.slf4j.LoggerFactory;
 
 import javax.ws.rs.*;
 import java.util.List;
 
 /**
  * Created by Jon Barber
  */
 @ApplicationPath("/leaderboard/")
 public class LeaderboardExporter {
 
     private final org.slf4j.Logger logger = LoggerFactory.getLogger(LeaderboardExporter.class);
 
 
     private Leaderboard leaderboard;
 
 
     @GET
     @Path("/{leaderboard}/page/{pageNumber}")
     @Produces({"application/xml", "application/json"})
     public PageResponse getPage(@PathParam("leaderboard")String leaderboardName,@PathParam("pageNumber") int pageNumber) {
         logger.debug("Getting page {} from the leaderboard {}",pageNumber,leaderboardName);
 
         Page page = leaderboard.page(leaderboardName,pageNumber);
         PageResponse result = new PageResponse();
         result.setPageNumber(page.getPageNumber());
         result.setEntries(page.getEntries());
         return result;
     }
 
     @GET
     @Path("/{leaderboard}/me/{id}")
     @Produces({"application/xml", "application/json"})
     public EntriesResponse getAroundMe(@PathParam("leaderboard")String leaderboardName,@PathParam("id") String id) {
         logger.debug("Getting contextual leaderboard for {} from the leaderboard {}",id,leaderboardName);
 
         EntriesResponse result =  new EntriesResponse();
         result.setEntries(leaderboard.aroundMe(leaderboardName,id));
         return result;
     }
 
     @GET
    @Path("/{leaderboard}/friends")
     @Produces({"application/xml", "application/json"})
    public EntriesResponse getFriends(@PathParam("leaderboard")String leaderboardName,@QueryParam("id") List<String> idList) {
         logger.debug("Getting a leaderboard for ids {} from the leaderboard {}",idList,leaderboardName);
 
         EntriesResponse result =  new EntriesResponse();
         result.setEntries(leaderboard.friends(leaderboardName, idList));
         return result;
     }
 
 
     public void setLeaderboard(Leaderboard leaderboard) {
         this.leaderboard = leaderboard;
     }
 }
