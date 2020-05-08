 package dev.bitcoin.bitcointalk;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonIOException;
 
 import dev.bitcoin.bitcointalk.model.Post;
 import dev.bitcoin.bitcointalk.model.appengine.Board;
 import dev.bitcoin.bitcointalk.model.appengine.Category;
 import dev.bitcoin.bitcointalk.model.appengine.Topic;
 import dev.bitcoin.bitcointalk.model.appengine.TopicPage;
 
 @Path("/v1")
 @SuppressWarnings("serial")
 public class BitcoinTalkAPIServlet extends HttpServlet {
 	
 	Map<String, String> responseCache = new HashMap<String, String>();
 	Database database = new Database();
 	
 	@POST
 	@Path("/pm")
 	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
 	public void pm(MultivaluedMap<String, String> formParams) throws IOException {
 		BitcoinTalkPostMan postMan = new BitcoinTalkPostMan();
 		
 		postMan.sendPrivateMessage(
 				formParams.getFirst("receipient"), 
 				formParams.getFirst("subject"), 
 				formParams.getFirst("message"), 
 				formParams.getFirst("username"), 
 				formParams.getFirst("password"));
 	}
 	
	@GET
 	@Path("/pmlist")
 	@Produces("application/json")
	public void pms(@Context HttpServletResponse servletResponse, 
			@QueryParam("username") String username, 
			@QueryParam("password") String password) throws IOException {
 		BitcoinTalkPostMan postMan = new BitcoinTalkPostMan();
		List<Post> pm = postMan.getPM(
				username, 
				password);
 		new Gson().toJson(pm, servletResponse.getWriter());
 	}
 	
 	@GET
 	@Consumes("application/json")
 	@Produces("application/json")
 	public String getCategories(@Context HttpServletResponse servletResponse) throws MalformedURLException, IOException {
 		setHeaders(servletResponse);
 		final List<Category> categories = database.getCategories();
 		for (Category category : categories) {
 			category.boards = category.getBoards();
 			for (Board board : category.boards) {
 				board.loadChildren();
 				board.boardId = "/v1/boards/" + board.boardId;
 			}
 		}
 		return new Gson().toJson(categories);
 	}
 	
 	@GET
 	@Consumes("text/html")
 	@Produces("text/html")
 	public Response getCategoriesHTML(@Context HttpServletResponse servletResponse) throws MalformedURLException, IOException {
 		setHeaders(servletResponse);
 		final List<Category> categories = database.getCategories();
 		for (Category category : categories) {
 			category.boards = category.getBoards();
 			for (Board board : category.boards) {
 				board.loadChildren();
 				board.boardId = makeLink("/v1/boards/" + board.boardId);
 			}
 		}
 		return Response.ok(asHTML(categories)).build();
 	}
 	
 	@GET
 	@Consumes("application/json")
 	@Produces("application/json")
 	@Path("/boards/{boardId}")
 	public Response getBoard(@Context HttpServletResponse servletResponse, @PathParam("boardId") String boardId) {
 		setHeaders(servletResponse);
 		final Board board = database.getBoard(boardId, true);
 		if(board != null) {
 			ResponseBuilder response = board.isUnripe() ? Response.status(202) : Response.ok();
 			board.loadTopics();
 			board.loadChildren();
 			Collection<Topic> topics = board.getTopics();
 			for (Topic topic : topics) {
 				topic.topicId = "/v1/topics/" + topic.topicId;
 			}
 			return response.entity((new Gson().toJson(board))).build();
 		} else {
 			return Response.status(404).build();
 		}
 	}
 	
 	@GET
 	@Consumes("text/html")
 	@Produces("text/html")
 	@Path("/boards/{boardId}")
 	public Response getBoardHTML(@Context HttpServletResponse servletResponse, @PathParam("boardId") String boardId) {
 		setHeaders(servletResponse);
 		final Board board = database.getBoard(boardId, true);
 		if(board != null) {
 			ResponseBuilder response = board.isUnripe() ? Response.status(202) : Response.ok();
 			board.loadTopics();
 			Collection<Topic> topics = board.getTopics();
 			for (Topic topic : topics) {
 				topic.topicId = makeLink("/v1/topics/" + topic.topicId);
 			}
 			return response.entity(asHTML(board)).build();
 		} else {
 			return Response.status(404).build();
 		}
 	}
 	
 	@GET
 	@Consumes("application/json")
 	@Produces("application/json")
 	@Path("/topics/{topicId}")
 	public Response getTopic(
 			@Context HttpServletResponse servletResponse, 
 			@PathParam("topicId") String topicId,
 			@QueryParam("pageId") String pageId) throws JsonIOException, IOException {
 		setHeaders(servletResponse);
 		final Topic topic = database.getTopic(topicId, true);
 		
 		if(topic != null) {
 			ResponseBuilder response = topic.isUnripe() ? Response.status(202) : Response.ok();
 			topic.loadPages();
 			topic.loadPageCount();
 			Collection<TopicPage> pages = topic.getPages();
 			for (TopicPage topicPage : pages) {
 				topicPage.pageId = "/v1/topics/" + topicId + "/pages/" + topicPage.pageId;
 			}
 			 if (pageId != null) {
 				if(pageId.equals("latest")) 
 					topic.loadLastPage();
 				else
 					tryToLoadPage(pageId, topic);
 			}
 			return response.entity((new Gson().toJson(topic))).build();
 		} else {
 			return Response.status(404).build();
 		}
 	}
 	
 	@GET
 	@Consumes("text/html")
 	@Produces("text/html")
 	@Path("/topics/{topicId}")
 	public Response getTopicHTML(
 			@Context HttpServletResponse servletResponse, 
 			@PathParam("topicId") String topicId,
 			@QueryParam("pageId") String pageId) throws JsonIOException, IOException {
 		setHeaders(servletResponse);
 		final Topic topic = database.getTopic(topicId, true);
 		
 		if(topic != null) {
 				ResponseBuilder response = topic.isUnripe() ? Response.status(202) : Response.ok();
 				topic.loadPages();
 				topic.loadPageCount();
 				Collection<TopicPage> pages = topic.getPages();
 				for (TopicPage topicPage : pages) {
 					topicPage.pageId = makeLink("/v1/topics/" + topicId + "/pages/" + topicPage.pageId);
 				}
 				if (pageId != null) {
 					if(pageId.equals("latest")) 
 						topic.loadLastPage();
 					else
 						tryToLoadPage(pageId, topic);
 				}
 				return response.entity(asHTML(topic)).build();
 			
 		} else {
 			return Response.status(404).build();
 		}
 	}
 
 	private void tryToLoadPage(String pageId, final Topic topic) {
 		try {
 			final int pageIdInt = Integer.parseInt(pageId);
 			topic.loadPageFromIndex(pageIdInt);
 		} catch (Exception e) {
 			// Just don't load any page..
 		}
 	}
 	
 	@GET
 	@Consumes("application/json")
 	@Produces("application/json")
 	@Path("/topics/{topicId}/pages")
 	public Response getPages(
 			@Context HttpServletResponse servletResponse, 
 			@PathParam("topicId") String topicId) throws JsonIOException, IOException {
 		setHeaders(servletResponse);
 		
 		final Topic topic = database.getTopic(topicId, true);
 		if(topic != null) {
 			Collection<TopicPage> pages = topic.getPages();
 			return Response.ok(new Gson().toJson(pages)).build();
 		} else {
 			return Response.status(404).build();
 		}
 	}
 	
 	@GET
 	@Consumes("application/json")
 	@Produces("application/json")
 	@Path("/topics/{topicId}/pages/{pageId}")
 	public Response getPageHTML(
 			@Context HttpServletResponse servletResponse, 
 			@PathParam("topicId") String topicId,
 			@PathParam("pageId") String pageId) throws JsonIOException, IOException {
 		setHeaders(servletResponse);
 		final Topic topic = database.getTopic(topicId, true);
 		if(topic != null) {
 			topic.loadPageFromIndex(Integer.parseInt(pageId));
 			return Response.ok(new Gson().toJson(topic.requestedPage)).build();
 		} else {
 			return Response.status(404).build();
 		}
 	}
 	
 	@GET
 	@Consumes("text/html")
 	@Produces("text/html")
 	@Path("/topics/{topicId}/pages/{pageId}")
 	public Response getPage(
 			@Context HttpServletResponse servletResponse, 
 			@PathParam("topicId") String topicId,
 			@PathParam("pageId") String pageId) throws JsonIOException, IOException {
 		setHeaders(servletResponse);
 		
 		final Topic topic = database.getTopic(topicId, true);
 		if(topic != null) {
 			topic.loadPageFromIndex(Integer.parseInt(pageId));
 			
 			return Response.ok(asHTML(topic.requestedPage)).build();
 		} else {
 			return Response.status(404).build();
 		}
 	}
 	void setHeaders(HttpServletResponse servletResponse) {
 		servletResponse.addHeader("Access-Control-Allow-Origin", "*"); 
 	}
 	
 	String asHTML(Object object) {
 		String json = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(object);
 		return "<pre>" + json + "</pre>";
 	}
 	
 	String makeLink(String href) {
 		return "<a href='" + href + "'>" + href + "</a>";
 	}
 }
