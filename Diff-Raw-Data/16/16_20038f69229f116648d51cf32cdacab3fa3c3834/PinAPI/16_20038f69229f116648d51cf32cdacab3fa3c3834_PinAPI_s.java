 package ru.redcraft.pinterest4j.core.api;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Future;
 
 import javax.ws.rs.core.MediaType;
 
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import ru.redcraft.pinterest4j.Board;
 import ru.redcraft.pinterest4j.Comment;
 import ru.redcraft.pinterest4j.NewPin;
 import ru.redcraft.pinterest4j.Pin;
 import ru.redcraft.pinterest4j.User;
 import ru.redcraft.pinterest4j.exceptions.PinMessageSizeException;
 import ru.redcraft.pinterest4j.exceptions.PinterestPinNotFoundException;
 import ru.redcraft.pinterest4j.exceptions.PinterestRuntimeException;
 
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.ClientResponse.Status;
 import com.sun.jersey.api.client.async.TypeListener;
 import com.sun.jersey.api.representation.Form;
 import com.sun.jersey.multipart.FormDataBodyPart;
 import com.sun.jersey.multipart.FormDataMultiPart;
 
 
 public class PinAPI extends CoreAPI {
 	
 	private static final int MAX_PIN_DESCRIPTION_LENGTH = 500;
 	
 	private static final String PIN_DESCRIPTION_PROP_NAME = "og:description";
 	private static final String PIN_IMAGE_PROP_NAME = "og:image";
 	private static final String PIN_PINBOARD_PROP_NAME = "pinterestapp:pinboard";
 	private static final String PIN_LINK_PROP_NAME = "pinterestapp:source";
 	private static final String PIN_PRICE_PROP_NAME = "pinterestapp:price";
 	private static final String PIN_PINNER_PROP_NAME = "pinterestapp:pinner";
 	private static final String PIN_LIKES_COUNT_PROP_NAME = "pinterestapp:likes";
 	private static final String PIN_REPINS_COUNT_PROP_NAME = "pinterestapp:repins";
 	private static final String PIN_COMMENTS_COUNT_PROP_NAME = "pinterestapp:comments";
 	private static final String PIN_URL = "og:url";
 	private static final String PIN_ID_ATTR = "data-id";
 	
 	private static final String PIN_API_ERROR = "PIN API ERROR: ";
 	private static final String COLLECTED_PINS = "Collected pins: ";
 	
 	private static final Logger LOG = Logger.getLogger(PinAPI.class);
 	
 	static class NoMorePinsException extends PinterestRuntimeException {
 
 		private static final long serialVersionUID = -8944899106991139578L;
 		
 		public NoMorePinsException() {
 			super("No more pins");
 		}
 		
 	}
 	
 	PinAPI(PinterestAccessToken accessToken, InternalAPIManager apiManager) {
 		super(accessToken, apiManager);
 	}
 
 	private void chechPinDescriptionLength(String description) {
 		int descLength = description.length();
 		if(descLength == 0) {
 			throw new PinMessageSizeException(PinMessageSizeException.MSG_ZERO_SIZE);
 		}
 		if(descLength > MAX_PIN_DESCRIPTION_LENGTH) {
 			throw new PinMessageSizeException(PinMessageSizeException.MSG_TOO_LONG);
 		}
 	}
 	
 	private FormDataMultiPart createPinAddForm(long boardID, NewPin newPin) {
 		return createPinAddForm(boardID, newPin.getDescription(), newPin.getPrice(), newPin.getLink(), newPin.getImageURL(), newPin.getImageFile());
 	}
 	
 	private FormDataMultiPart createPinAddForm(long boardID, String description, double price, String link, String imageUrl, File imageFile) {
 		FormDataMultiPart multipartForm = new FormDataMultiPart();
 		multipartForm.bodyPart(new FormDataBodyPart("board", Long.toString(boardID)));
 		multipartForm.bodyPart(new FormDataBodyPart("details", description));
 		multipartForm.bodyPart(new FormDataBodyPart("link", link));
 		multipartForm.bodyPart(new FormDataBodyPart("csrfmiddlewaretoken", getAccessToken().getCsrfToken().getValue()));
 		multipartForm.bodyPart(new FormDataBodyPart("buyable", ""));
 		multipartForm.bodyPart(new FormDataBodyPart("tags", ""));
 		multipartForm.bodyPart(new FormDataBodyPart("replies", ""));
 		if(price != 0) {
 			multipartForm.bodyPart(new FormDataBodyPart("buyable", "$" + Double.toString(price)));
 		}
 		if(imageUrl != null) {
 			multipartForm.bodyPart(new FormDataBodyPart("img_url", imageUrl));
 		}
 		else {
 			multipartForm.bodyPart(createImageBodyPart(imageFile));
 		}
 		return multipartForm;
 	}
 	
 	public Pin addPinToBoard(Board board, NewPin newPin) {
 		LOG.debug("Creating new pin on board " + board + " with new pin: " + newPin);
 		chechPinDescriptionLength(newPin.getDescription());
 		board.getPinsCount(); // Magic. Pinterest boards are lazy initiated on creation
 		Map<String, String> responseMap = new APIRequestBuilder("pin/create/")
 			.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE)
 			.setMethod(Method.POST, createPinAddForm(board.getId(), newPin))
 			.setErrorMessage(PIN_API_ERROR)
 			.build().parseResponse();
 		LazyPin createdPin = new LazyPin(responseMap.get("url"), getApiManager());
 		LOG.debug("Pin created " + createdPin);
 		return createdPin;
 	}
 	
 	private APIRequestBuilder getPinInfoRequestBuilder(long id) {
 		return new APIRequestBuilder("pin/" + Long.toString(id) + "/")
 			.setAjaxUsage(false)
 			.addExceptionMapping(Status.NOT_FOUND, new PinterestPinNotFoundException(id))
 			.setErrorMessage(PIN_API_ERROR);
 	}
 	
 	public PinBuilder getCompletePin(long id) {
 		LOG.debug("Getting all info for pin with id " + id);
 		return parsePinInfo(getPinInfoRequestBuilder(id).build().getDocument());
 	}
 	
 	private PinBuilder parsePinInfo(Document doc) {
 		PinBuilder builder = new PinBuilder();
 		Map<String, String> metaMap = new HashMap<String, String>();
 		for(Element meta : doc.select("meta")) {
 			metaMap.put(meta.attr("property"), meta.attr("content"));
 		}
		builder.setId(Long.valueOf(metaMap.get(PIN_URL).replace("http://pinterest.com/pin/", "").replace("/?timeline=1", "")));
 		builder.setDescription(metaMap.get(PIN_DESCRIPTION_PROP_NAME));
 		builder.setImageURL(metaMap.get(PIN_IMAGE_PROP_NAME));
 		builder.setLink(metaMap.get(PIN_LINK_PROP_NAME));
 		builder.setPrice(metaMap.get(PIN_PRICE_PROP_NAME) != null ? Double.valueOf(metaMap.get(PIN_PRICE_PROP_NAME)) : 0);
 		builder.setLikesCount(Integer.valueOf(metaMap.get(PIN_LIKES_COUNT_PROP_NAME)));
 		builder.setRepinsCount(Integer.valueOf(metaMap.get(PIN_REPINS_COUNT_PROP_NAME)));
 		builder.setCommentsCount(Integer.valueOf(metaMap.get(PIN_COMMENTS_COUNT_PROP_NAME)));
 		builder.setBoard(new LazyBoard(metaMap.get(PIN_PINBOARD_PROP_NAME).replace(PINTEREST_URL, ""), getApiManager()));
 		builder.setPinner(new LazyUser(metaMap.get(PIN_PINNER_PROP_NAME).replace(PINTEREST_URL, "").replace("/", ""), getApiManager()));
 		
 		Elements pinners = doc.select("p#PinnerName").first().getElementsByTag("a");
 		if(pinners.size() > 1) {
 			builder.setOriginalPinner(new LazyUser(pinners.get(1).attr("href").replace("/", ""), getApiManager()));
 			builder.setRepined(true);
 		}
 		else {
 			builder.setOriginalPinner(builder.getPinner());
 			builder.setRepined(false);
 		}
 		return builder;
 	}
 	
 	private List<Pin> parsePinsResponse(Document doc) {
 		List<Pin> pinList = new ArrayList<Pin>();
 		Elements htmlPins = doc.select("div.pin");
 		for(Element htmlPin : htmlPins) {
 			if(htmlPin.hasAttr(PinAPI.PIN_ID_ATTR)) {
 				long pinID = Long.valueOf(htmlPin.attr(PinAPI.PIN_ID_ATTR));
 				pinList.add(new LazyPin(pinID, getApiManager()));
 			}
 		}
 		LOG.debug(COLLECTED_PINS + pinList.size());
 		return pinList;
 	}
 	
 	private List<Pin> processPinsPage(APIRequestBuilder requestBuilder) {
 		List<Pin> response = null;
 		try {
 			Document doc = requestBuilder
 				.addExceptionMapping(Status.NOT_FOUND, new NoMorePinsException())
 				.setErrorMessage(PIN_API_ERROR)
 				.build().getDocument();
 			response = parsePinsResponse(doc);
 		} catch(NoMorePinsException e) {
 			response = new ArrayList<Pin>();
 		}
 		return response;
 	}
 	
 	public List<Pin> getPins(User user, int page, boolean likes) {
 		LOG.debug("Getting pin list for user " + user + " on page " + page + "with filter likes " + likes);
 		String filter = likes ? "&filter=likes" : "";
 		return processPinsPage(new APIRequestBuilder(user.getUserName() + "/pins/?page=" + page + filter));
 	}
 	
 	public List<Pin> getPins(Board board, int page) {
 		LOG.debug("Getting pin list for board " + board + " on page " + page);
 		return processPinsPage(new APIRequestBuilder(board.getURL() + "?page=" + page));
 	}
 	
 	public void deletePin(Pin pin) {
 		LOG.debug("Deleting pin " + pin);
 		new APIRequestBuilder(pin.getURL() + "delete/").setMethod(Method.POST, "{}").setErrorMessage(PIN_API_ERROR)
 			.build();
 		LOG.debug("Pin deleted");
 	}
 
 	public Pin updatePin(Pin pin, String description, double price, String link, Board board) {
 		LOG.debug(String.format("Updating pin=%s with desc=%s, price=%f, link = %s, board=%s",
 				pin, description, price, link, board));
 		chechPinDescriptionLength(description);
 		
 		FormDataMultiPart multipartForm = createPinAddForm(board.getId(), description, price, link, "", null);
 		new APIRequestBuilder(pin.getURL() + "edit/").setMethod(Method.POST, multipartForm)
 			.setErrorMessage(PIN_API_ERROR)
 			.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE)	
 			.build();
 		
 		Pin updatedPin = new LazyPin(pin.getId(), getApiManager());
 		LOG.debug("Pin updated");
 		return updatedPin;
 	}
 
 	public Pin getPinByID(long id) {
 		return new LazyPin(getCompletePin(id), getApiManager());
 	}
 	
 	class PinListener extends TypeListener<ClientResponse> {
 		
 		private final long id;
 		private final AsyncPinPayload payload;
 		
 		public PinListener(long id, AsyncPinPayload payload) {
 			super(ClientResponse.class);
 			this.id = id;
 			this.payload = payload;
 		}
 		
 		@Override
 		public void onComplete(Future<ClientResponse> f)
 				throws InterruptedException {
 			try {
 				ClientResponse response = f.get();
 				if(response.getStatus() == Status.OK.getStatusCode()) {
 					Document doc = Jsoup.parse(response.getEntity(String.class));
 					Pin pin = new LazyPin(parsePinInfo(doc), getApiManager());
 					payload.processPin(pin);
 				}
 				else {
 					LOG.error("Async pin processing error");
 					response.close();
 					f.cancel(true);
 					payload.processException(id, new PinterestRuntimeException(response, "Bad async response"));
 				}
 			} catch (Exception e) {
 				LOG.error("Async pin processing error", e);
 				f.cancel(true);
 				payload.processException(id, e);
 			}
 		}
 	}
 	
 	public void getAsyncPinByID(final long id, final AsyncPinPayload payload) {
 		getPinInfoRequestBuilder(id).buildAsync(new PinListener(id, payload));
 	}
 
 	private Form createRepinForm(Pin pin, Board board, String description) {
 		Form form = new Form();
 		form.add("board", board.getId());
 		form.add("id", pin.getId());
 		form.add("details", description);
 		form.add("csrfmiddlewaretoken", getAccessToken().getCsrfToken().getValue());
 		return form;
 	}
 	
 	public Pin repin(Pin pin, Board board, String description) {
 		String newDescription = (description != null) ? description : pin.getDescription();
 		LOG.debug(String.format("Repining pin = %s on board = %s with descr = %s", pin, board, newDescription));
 		Form repinForm = createRepinForm(pin, board, newDescription);
 		String repinURL = new APIRequestBuilder(pin.getURL() + "repin/")
 			.setMethod(Method.POST, repinForm)
 			.setErrorMessage(PIN_API_ERROR).build().parseResponse().get("repin_url");
 		Pin repinedPin = new LazyPin(repinURL, getApiManager());
 		LOG.debug("Created repined pin=" + repinedPin);
 		return repinedPin;
 	}
 	
 	public void like(Pin pin, boolean like) {
 		LOG.debug(String.format("Setting like of pin = %s to = %s", pin, like));
 		new APIRequestBuilder(pin.getURL() + "like/")
 			.setMethod(Method.POST, getSwitchForm("unlike", like))
 			.setErrorMessage(PIN_API_ERROR).build().parseResponse();
 		LOG.debug("Pin like mark set to " + like);
 	}
 
 	private Form createCommentForm(Pin pin, String comment) {
 		Form form = new Form();
 		form.add("text", comment);
 		form.add("path", "/" + pin.getURL());
 		form.add("replies", "");
 		return form;
 	}
 	
 	public Comment addCommentToPin(Pin pin, String comment, User user) {
 		LOG.debug(String.format("Adding comment to pin = %s with text = '%s' by user = %s", pin, comment, user));
 		Map<String, String> responseMap = new APIRequestBuilder(pin.getURL() + "comment/")
 			.setMethod(Method.POST, createCommentForm(pin, comment))
 			.setErrorMessage(PIN_API_ERROR).build().parseResponse();
 		Comment newComment = new CommentImpl(Long.valueOf(responseMap.get("id")), comment, user, new LazyPin(pin.getId(), getApiManager()));
 		LOG.debug("Comment created: " + newComment);
 		return newComment;
 	}
 	
 	public void deleteComment(Comment comment) {
 		LOG.debug(String.format("Deleting comment = %s", comment));
 		Form commentForm = new Form();
 		commentForm.add("comment", comment.getId());
 		new APIRequestBuilder(comment.getPin().getURL() + "deletecomment/")
 			.setMethod(Method.POST, commentForm)
 			.setErrorMessage(PIN_API_ERROR).build().parseResponse();
 		LOG.debug("Comment deleted");
 	}
 
 	public List<Comment> getComments(Pin pin) {
 		LOG.debug("Getting comments for pin = " + pin);
 		List<Comment> comments = new ArrayList<Comment>();
 		Document doc = null;
 		String axajResponse = null;
 		try{
 			axajResponse = new APIRequestBuilder(pin.getURL())
 			.setErrorMessage(PIN_API_ERROR).build().getResponse().getEntity(String.class);
 			doc = Jsoup.parse(new JSONObject(axajResponse).getString("footer"));
 		} catch(JSONException e) {
 			throw new PinterestRuntimeException(PIN_API_ERROR + axajResponse, e);
 		}
 		for(Element comment : doc.select("div.comment")) {
 			long id = Long.valueOf(comment.getElementsByClass("DeleteComment").first().attr("data"));
 			Element contentMeta = comment.getElementsByClass("CommenterMeta").first();
 			User user = new LazyUser(contentMeta.getElementsByTag("a").first().attr("href").replace("/", ""), getApiManager());
 			contentMeta.getElementsByTag("a").remove();
 			String text = contentMeta.text();
 			comments.add(new CommentImpl(id, text, user, pin));
 		}
 		LOG.debug("Comments extracted: " + comments);
 		return comments;
 	}
 
 	public boolean isLiked(Pin pin) {
 		LOG.debug("Checking liked status for pin=" + pin);
 		boolean liked = false;
 		if(getPinInfoRequestBuilder(pin.getId()).build().getDocument().select("li.unlike-button").size() == 1) {
 			liked = true;
 		}
 		LOG.debug("Liked state is " + liked);
 		return liked;
 	}
 
 	public List<Pin> getPinsFromThread(String url, int page, long marker) {
 		LOG.debug(String.format("Loading pins thread for url = %s on page = %d with marker = %d", url, page, marker));
 		Document doc = new APIRequestBuilder(url + "&page=" + page + "&marker=" + marker)
 			.setErrorMessage(PIN_API_ERROR)
 			.build().getDocument();
 		return parsePinsResponse(doc);
 	}
 
 }
