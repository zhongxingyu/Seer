 
 package net.messze.valahol;
 
 import com.wordnik.swagger.annotations.*;
 import net.messze.valahol.data.*;
 import net.messze.valahol.service.MongodbPersistence;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.activation.MimetypesFileTypeMap;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.lang.Error;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Example resource class hosted at the URI path "/myresource"
  */
 @Path("/puzzle")
 @Api(value = "/puzzle", description = "Operations about puzzles.", basePath = "http://localhost:9999/rest")
 @Produces({"application/json"})
 public class PuzzleApi {
 
     public static final Logger LOG = LoggerFactory.getLogger(PuzzleApi.class);
 
     @Inject
     @Named("googleBrowserApiKey")
     private String apiKey;
 
     @Inject
     MongodbPersistence persistenceService;
 
     /**
      * Method processing HTTP GET requests, producing "text/plain" MIME media type.
      *
      * @return String that will be send back as a response of type "text/plain".
      */
     @GET
     @Path("/{id}")
     @ApiOperation(value = "Find puzzle by ID", responseClass = "net.messze.valahol.data.Puzzle", notes = "If the user session exists, solution statistics also will be delivered")
     @ApiErrors(value = {@ApiError(code = 404, reason = "No puzzle for the given id")})
     public Response get(@ApiParam(value = "The id of the puzzle to get", required = true) @PathParam("id") String id) {
         Puzzle response = persistenceService.<Puzzle>find(Puzzle.class, id);
         if (response == null) {
             return Response.status(404).build();
         } else {
             return Response.ok(response).build();
         }
     }
 
     @POST
     @ApiOperation(value = "Create a new puzzle")
     @Consumes("application/json")
     public String create(@ApiParam(value = "Puzzle object to create", required = true) Puzzle puzzle) {
         if (puzzle.getId() != null) {
             persistenceService.update(Puzzle.class, puzzle.getId(), puzzle);
             return puzzle.getId();
         } else {
             return persistenceService.<Puzzle>create(puzzle);
         }
     }
 
     @DELETE
     @Path("/{id}")
     @ApiOperation(value = "Delete puzzle by id")
     public void delete(@ApiParam(value = "The id of the puzzle to get", required = true) @PathParam("id") String id) {
         persistenceService.<Puzzle>delete(Puzzle.class, id);
     }
 
     @GET
     @ApiOperation(value = "Get all the existing puzzles", responseClass = "net.messze.valahol.data.PuzzleDetails", multiValueResponse = true)
     @Path("/all")
     @ApiErrors(value = {@ApiError(code = 401, reason = "Own puzzles are requested but no user session")})
     public Response list(@ApiParam(value = "set to true if you need the puzzles of the current user") @DefaultValue("false") @QueryParam("own") boolean own, @Context javax.servlet.http.HttpServletRequest request) {
         if (own) {
             String currentUserId = (String) request.getSession().getAttribute(AuthApi.USER_ID);
             if (currentUserId == null) {
                 Response.status(401).entity(new Error("No valid user session")).build();
             }
             return Response.ok(persistenceService.findAll(Puzzle.class, currentUserId)).build();
         } else {
             return Response.ok(persistenceService.findAll(Puzzle.class, null)).build();
         }
     }
 
     @GET
     @ApiOperation(value = "Get hall of fames")
     @Path("/highscores")
     public List<Highscore> highscores() {
         return persistenceService.highscore();
     }
 
     @GET
     @ApiOperation(value = "Get the thumbnail of a specific puzzle")
     @Path("/{id}/thumbnail")
     @Produces("image/*")
     public Response thumbnail(@ApiParam(value = "The id of the puzzle", required = true)
                               @PathParam("id") String id) {
         Puzzle p = persistenceService.find(Puzzle.class, id);

        File cacheFile = new File("/tmp/r/id.jpg");
         if (!cacheFile.exists()) {
            if (!cacheFile.getParentFile().exists()) {
                cacheFile.getParentFile().mkdirs();
            }
             try {
                 URL url = new URL("http://maps.googleapis.com/maps/api/streetview?size=300x100&location=" + p.getLat() +
                         "," + p.getLng() + "&heading=" + p.getPitch() + "&pitch=" + p.getPitch() + "&sensor=false&key=" + apiKey);
                 LOG.debug("Caching thumbnail file from: " + url);
                 ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                 FileOutputStream fos = new FileOutputStream(cacheFile);
                 fos.getChannel().transferFrom(rbc, 0, 1 << 24);
             } catch (Exception ex) {
                 LOG.error("Can't cache the thumbnail file", ex);
             }
 
         }
         String mt = new MimetypesFileTypeMap().getContentType(cacheFile);
         return Response.ok(cacheFile, mt).build();
     }
 
 
     @POST
     @ApiOperation(value = "Send a new solution")
     @Path("/{id}/solution")
     @Consumes("application/json")
     public Response solve(@ApiParam(value = "The id of the puzzle to solve", required = true)
                           @PathParam("id") String id, @ApiParam(value = "Solution object to update", required = true) Solution solution) {
         Puzzle puzzle = persistenceService.find(Puzzle.class, id);
         solution.setDate(new Date());
         if (puzzle == null) {
             return Response.status(404).build();
         }
         //todo user the current one from the session.
         solution.setPuzzleName(puzzle.getLabel());
         SolutionResponse response = new SolutionResponse();
         if (puzzle.getAnswer().equals(solution.getAnswer())) {
             solution.setType(1);
             response.setGood(true);
             response.setResponse("IGEEEN.");
             User currentUser = persistenceService.find(User.class, solution.getUserId());
             puzzle.addSolver(new Solver(solution.getUserId(), currentUser.getUserName(), solution.getScore(), solution.getDate()));
             persistenceService.update(Puzzle.class, puzzle.getId(), puzzle);
             persistenceService.create(solution);
         } else {
             response.setGood(false);
             solution.setType(0);
             response.setResponse("Nem, nem.");
             Guess g = new Guess();
             g.setDate(solution.getDate());
             g.setUserId(solution.getUserId());
             g.setPuzzleId(solution.getUserId());
             g.setPuzzleName(solution.getPuzzleName());
             g.setAnswer(solution.getAnswer());
             persistenceService.create(g);
         }
 
 
         return Response.ok(response).build();
     }
 
 
     @GET
     @Path("/{id}/comments")
     @ApiOperation(value = "Return with the comments of a specific puzzle", responseClass = "net.messze.valahol.data.Comment", multiValueResponse = true)
     public List<Comment> comments(@ApiParam(value = "The id of the puzzle.", required = true) @PathParam("id") String id) {
         return persistenceService.findCommentsForPost(id);
     }
 
     @POST
     @ApiOperation(value = "Create a new comment for a specific puzzle")
     @Consumes("application/json")
     @Path("/{id}/comment")
     public void addComment(@ApiParam(value = "The id of the puzzle.", required = true) @PathParam("id") String id,
                            @ApiParam(value = "Comment object to create.", required = true) Comment comment) {
         comment.setPuzzleId(id);
         persistenceService.create(comment);
     }
 
 }
