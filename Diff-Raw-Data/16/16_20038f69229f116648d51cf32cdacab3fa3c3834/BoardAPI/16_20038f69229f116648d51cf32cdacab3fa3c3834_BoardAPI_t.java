 package ru.redcraft.pinterest4j.core.api;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import ru.redcraft.pinterest4j.Board;
 import ru.redcraft.pinterest4j.BoardCategory;
 import ru.redcraft.pinterest4j.NewBoard;
 import ru.redcraft.pinterest4j.User;
 import ru.redcraft.pinterest4j.exceptions.PinterestBoardExistException;
 import ru.redcraft.pinterest4j.exceptions.PinterestBoardNotFoundException;
 
 import com.sun.jersey.api.client.ClientResponse.Status;
 import com.sun.jersey.api.representation.Form;
 
 public final class BoardAPI extends CoreAPI {
 
 	private static final String BOARD_EXIST_RESPONSE_MESSAGE = "<ul class=\"errorlist\"><li>You have a board with this name.</li></ul>";
 	private static final String BOARD_DESCRIPTION_PROP_NAME = "og:description";
 	private static final String BOARD_TITLE_PROP_NAME = "og:title";
 	private static final String BOARD_CATEGORY_PROP_NAME = "pinterestapp:category";
 	private static final String BOARD_PINS_PROP_NAME = "pinterestapp:pins";
 	private static final String BOARD_FOLLOWERS_PROP_NAME = "pinterestapp:followers";
 	private static final String BOARD_USER_PROP_NAME = "pinterestapp:pinner";
 	
 	private static final Logger LOG = Logger.getLogger(BoardAPI.class);
 	private static final String BOARD_API_ERROR = "PINBOARD API ERROR: ";
 	
 	BoardAPI(PinterestAccessToken accessToken, InternalAPIManager apiManager) {
 		super(accessToken, apiManager);
 	}
 	
 	public List<Board> getBoards(User user) {
 		LOG.debug("Collecting board list for user = " + user);
 		List<Board> boardList = new ArrayList<Board>();
 		Document doc = new APIRequestBuilder(user.getUserName() + "/")
 			.setErrorMessage(BOARD_API_ERROR)
 			.build().getDocument();
 		Elements htmlBoards = doc.select(".pinBoard");
 		for(Element htmlBoard : htmlBoards) {
 			if(htmlBoard.hasAttr("id")) {
 				String stringID = htmlBoard.attr("id").replace("board", "");
 				long id = Long.valueOf(stringID);
 				String url = htmlBoard.select("a.link").first().attr("href");
 				String name = htmlBoard.select("h3.serif").first().select("a").text();
 				LazyBoard board = new LazyBoard(id, url, name, getApiManager());
 				boardList.add(board);
 			}
 		}
 		LOG.debug("Board count = " + boardList.size());
 		return boardList;
 	}
 	
 	public static String encodeTitle(String title) {
 		return title.replace('_', ' ').replaceAll("[^a-zA-Z0-9]+", "-").toLowerCase(PINTEREST_LOCALE);
 	}
 	
 	public static String createLink(String title, String login) {
 		return String.format("/%s/%s/", login, encodeTitle(title));
 	}
 
 	public Board createBoard(NewBoard newBoard) {
 		LOG.debug("Creating board for user = " + getApiManager().getUserAPI().getCurrentUser().getUserName() + "using info: " + newBoard);
 		Map<String, String> responseMap = new APIRequestBuilder("board/create/")
 			.setMethod(Method.POST, createNewBoardForm(newBoard))
 			.setErrorMessage(BOARD_API_ERROR)
 			.build().parseResponse(BOARD_EXIST_RESPONSE_MESSAGE, new PinterestBoardExistException(newBoard.getTitle()));
 		LazyBoard createdBoard = new LazyBoard(
 				Long.valueOf(responseMap.get("id")), responseMap.get("url"), responseMap.get("name"), newBoard.getCategory(), getApiManager());
 		LOG.debug("Board created " + createdBoard);
 		return createdBoard;
 	}
 	
 	private Form createNewBoardForm(NewBoard newBoard) {
 		Form form = new Form();
 		form.add("name", newBoard.getTitle());
 		form.add("category", newBoard.getCategory().getId());
 		form.add("collaborator", "me");
 		return form;
 	}
 	
 	private Document getBoardInfoPage(String boardURL) {
 		return new APIRequestBuilder(boardURL)
 			.addExceptionMapping(Status.NOT_FOUND, new PinterestBoardNotFoundException(boardURL))
 			.setErrorMessage(BOARD_API_ERROR)
 			.build().getDocument();	
 	}
 	
 	public BoardBuilder getCompleteBoard(String boardURL) {
 		LOG.debug("Getting all info for board url " + boardURL);
 		
 		BoardBuilder builder = new BoardBuilder();
 		builder.setURL(boardURL);
 		
 		Document doc = getBoardInfoPage(boardURL);
 		
 		Map<String, String> metaMap = new HashMap<String, String>();
 		for(Element meta : doc.select("meta")) {
 			metaMap.put(meta.attr("property"), meta.attr("content"));
 		}
 		builder.setDescription(metaMap.get(BOARD_DESCRIPTION_PROP_NAME));
 		builder.setCategory(BoardCategory.getInstance(metaMap.get(BOARD_CATEGORY_PROP_NAME).toUpperCase(PINTEREST_LOCALE)));
 		builder.setPinsCount(Integer.valueOf(metaMap.get(BOARD_PINS_PROP_NAME)));
 		builder.setFollowersCount(Integer.valueOf(metaMap.get(BOARD_FOLLOWERS_PROP_NAME)));
 		builder.setTitle(metaMap.get(BOARD_TITLE_PROP_NAME));
		builder.setUser(new LazyUser(metaMap.get(BOARD_USER_PROP_NAME).replace("http://pinterest.com/", "").replace("/", ""), getApiManager()));
 		
 		for(Element meta : doc.select("div.BoardList").first().select("li")) {
 			if(meta.child(0).text().equals(builder.getTitle())) {
 				builder.setId(Long.valueOf(meta.attr("data")));
 				break;
 			}
 		}
 		builder.setPageCount(Integer.valueOf(doc.select("a.MoreGrid").first().attr("href").replace("?page=", "")) - 1);
 			
 		return builder;
 	}
 
 	public void deleteBoard(Board board) {
 		LOG.debug("Deleting board " + board);
 		new APIRequestBuilder(board.getURL() + "settings/")
 			.setMethod(Method.DELETE)
 			.setErrorMessage(BOARD_API_ERROR)
 			.build();	
 		LOG.debug("Board deleted");
 	}
 
 	private Form createUpdateBoardForm(String title, String description, BoardCategory category) {
 		Form form = new Form();
 		form.add("name", title);
 		form.add("description", description);
 		form.add("change_BoardCollaborators", "me");
 		form.add("csrfmiddlewaretoken", getAccessToken().getCsrfToken().getValue());
 		form.add("collaborator_name", "Enter a name");
 		form.add("collaborator_username", null);
 		form.add("category", category.getId());
 		return form;
 	}
 	
 	public Board updateBoard(Board board, String title, String description, BoardCategory category) {
 		LOG.debug(String.format("Updating board with uri = %s with title=%s, desc=%s, cat=%s",
 				board.getURL(), title, description, category));
 		new APIRequestBuilder(board.getURL() + "settings/")
 			.setMethod(Method.POST, createUpdateBoardForm(title, description, category))
 			.setErrorMessage(BOARD_API_ERROR)
 			.build();
 		Board updatedBoard = new LazyBoard(
 				board.getId(), createLink(title, getApiManager().getUserAPI().getCurrentUser().getUserName()), title, description, category, getApiManager());
 		LOG.debug("Board updated");
 		return updatedBoard;
 	}
 
 	public Board getBoardByURL(String url) {
 		return new LazyBoard(getCompleteBoard(url), getApiManager());
 	}
 
 	public void followBoard(Board board, boolean follow) {
 		LOG.debug(String.format("Setting follow on board = %s to = %s", board, follow));
 		new APIRequestBuilder(board.getURL() + "follow/")
 			.setMethod(Method.POST, getSwitchForm("unfollow", follow))
 			.setErrorMessage(BOARD_API_ERROR)
 			.build().parseResponse();
 		LOG.debug("Board follow mark set to " + follow);
 	}
 
 	public boolean isFollowing(Board board) {
 		LOG.debug(String.format("Checking board %s for following", board));
 		boolean followed = false;
 		if(getBoardInfoPage(board.getURL()).select("a.unfollowbutton").size() == 1) {
 			followed = true;
 		}
 		LOG.debug("Following status is " + followed);
 		return followed;
 	}
 	
 }
