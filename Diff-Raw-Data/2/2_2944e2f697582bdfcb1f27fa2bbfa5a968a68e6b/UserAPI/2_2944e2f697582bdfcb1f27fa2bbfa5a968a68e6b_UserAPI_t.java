 package ru.redcraft.pinterest4j.core.api;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.ws.rs.core.MediaType;
 
 import org.apache.log4j.Logger;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import ru.redcraft.pinterest4j.Activity;
 import ru.redcraft.pinterest4j.Activity.ActivityType;
 import ru.redcraft.pinterest4j.Board;
 import ru.redcraft.pinterest4j.Followable;
 import ru.redcraft.pinterest4j.NewUserSettings;
 import ru.redcraft.pinterest4j.Pin;
 import ru.redcraft.pinterest4j.User;
 import ru.redcraft.pinterest4j.core.activities.CommentActivity;
 import ru.redcraft.pinterest4j.core.activities.CreateBoardActivity;
 import ru.redcraft.pinterest4j.core.activities.FollowBoardActivity;
 import ru.redcraft.pinterest4j.core.activities.FollowUserActivity;
 import ru.redcraft.pinterest4j.core.activities.PinActivity;
 import ru.redcraft.pinterest4j.core.api.AdditionalUserSettings.Gender;
 import ru.redcraft.pinterest4j.core.api.FollowCollection.FollowContainer;
 import ru.redcraft.pinterest4j.core.api.FollowCollection.FollowType;
 import ru.redcraft.pinterest4j.exceptions.PinterestRuntimeException;
 import ru.redcraft.pinterest4j.exceptions.PinterestUserNotFoundException;
 
 import com.sun.jersey.api.client.ClientResponse.Status;
 import com.sun.jersey.multipart.FormDataBodyPart;
 import com.sun.jersey.multipart.FormDataMultiPart;
 
 public class UserAPI extends CoreAPI {
 
 	private static final String USER_FOLLOWING_PROP_NAME = "pinterestapp:following";
 	private static final String USER_FOLLOWERS_PROP_NAME = "pinterestapp:followers";
 	private static final String USER_BOARDS_PROP_NAME = "pinterestapp:boards";
 	private static final String USER_PINS_PROP_NAME = "pinterestapp:pins";
 	private static final String USER_IMAGE_PROP_NAME = "og:image";
 	
 	private static final Logger LOG = Logger.getLogger(UserAPI.class);
 	
 	private static final String USER_API_ERROR = "USER API ERROR: ";
 	
 	UserAPI(PinterestAccessToken accessToken, InternalAPIManager apiManager) {
 		super(accessToken, apiManager);
 	}
 	
 	private Document getUserInfoPage(String userName) {
 		return new APIRequestBuilder(userName + "/")
 			.addExceptionMapping(Status.NOT_FOUND, new PinterestUserNotFoundException(userName))
 			.setErrorMessage(USER_API_ERROR)
 			.build().getDocument();
 	}
 	
 	public UserBuilder getCompleteUser(String userName) {
 		LOG.debug("Getting all info for username " + userName);
 		UserBuilder builder = new UserBuilder();
 		builder.setUserName(userName);
 		
 		Document doc = getUserInfoPage(userName);
 		
 		Map<String, String> metaMap = new HashMap<String, String>();
 		for(Element meta : doc.select("meta")) {
 			metaMap.put(meta.attr("property"), meta.attr("content"));
 		}
 		builder.setFollowingCount(Integer.valueOf(metaMap.get(USER_FOLLOWING_PROP_NAME)));
 		builder.setFollowersCount(Integer.valueOf(metaMap.get(USER_FOLLOWERS_PROP_NAME)));
 		builder.setBoardsCount(Integer.valueOf(metaMap.get(USER_BOARDS_PROP_NAME)));
 		builder.setPinsCount(Integer.valueOf(metaMap.get(USER_PINS_PROP_NAME)));
 		builder.setImageURL(metaMap.get(USER_IMAGE_PROP_NAME));
 		
 		Element userInfo = doc.select("div.content").first();
 		builder.setFullName(userInfo.getElementsByTag("h1").first().text());
 		
 		Element description = userInfo.getElementsByTag("p").first();
 		builder.setDescription(description != null ? description.text() : null);
 		Element twitter = userInfo.select("a.twitter").first();
 		builder.setTwitterURL(twitter != null ? twitter.attr(HREF_TAG_ATTR) : null);
 		Element facebook = userInfo.select("a.facebook").first();
 		builder.setFacebookURL(facebook != null ? facebook.attr(HREF_TAG_ATTR) : null);
 		Element website = userInfo.select("a.website").first();
 		builder.setSiteURL(website != null ? website.attr(HREF_TAG_ATTR) : null);
 		Element location = userInfo.select("li#ProfileLocation").first();
 		builder.setLocation(location != null ? location.text() : null);
 		
 		builder.setLikesCount(Integer.valueOf(doc.select("div#ContextBar").first().getElementsByTag("li").get(2).getElementsByTag("strong").first().text()));
 		
 		return builder;
 	}
 	
 	public User getUserForName(String userName) {
 		return new LazyUser(getCompleteUser(userName), getApiManager());
 	}
 
 	private AdditionalUserSettings getUserAdtSettings() {
 		AdditionalUserSettings adtSettings = new AdditionalUserSettings();
 		Document doc = new APIRequestBuilder("settings/")
 			.setProtocol(Protocol.HTTPS)
 			.setAjaxUsage(false)
 			.setErrorMessage(USER_API_ERROR)
 			.build().getDocument();
 		adtSettings.setEmail(doc.getElementById("id_email").attr(VALUE_TAG_ATTR));
 		adtSettings.setFirstName(doc.getElementById("id_first_name").attr(VALUE_TAG_ATTR));
 		adtSettings.setLastName(doc.getElementById("id_last_name").attr(VALUE_TAG_ATTR));
 		adtSettings.setUserName(doc.getElementById("id_username").attr(VALUE_TAG_ATTR));
 		adtSettings.setWebsite(doc.getElementById("id_website").attr(VALUE_TAG_ATTR));
 		adtSettings.setLocation(doc.getElementById("id_location").attr(VALUE_TAG_ATTR));
 		if(doc.getElementById("id_gender_0").hasAttr(UserAPI.CHECKED_TAG_ATTR)) {
 			adtSettings.setGender(Gender.MALE);
 		}
 		if(doc.getElementById("id_gender_1").hasAttr(UserAPI.CHECKED_TAG_ATTR)) {
 			adtSettings.setGender(Gender.FEMALE);
 		}
 		if(doc.getElementById("id_gender_2").hasAttr(UserAPI.CHECKED_TAG_ATTR)) {
 			adtSettings.setGender(Gender.UNSPECIFIED);
 		}
 		return adtSettings;
 	}
 	
 	private FormDataMultiPart createUserForm(NewUserSettings settings) {
 		FormDataMultiPart multipartForm = new FormDataMultiPart();
 		AdditionalUserSettings adtSettings = getUserAdtSettings();
 		multipartForm.bodyPart(new FormDataBodyPart("csrfmiddlewaretoken", getAccessToken().getCsrfToken().getValue())); 
 		multipartForm.bodyPart(new FormDataBodyPart("email", adtSettings.getEmail()));
 		multipartForm.bodyPart(new FormDataBodyPart("gender", adtSettings.getGender().name().toLowerCase(PINTEREST_LOCALE)));
 		multipartForm.bodyPart(new FormDataBodyPart("username", adtSettings.getUserName()));
 		
 		multipartForm.bodyPart(new FormDataBodyPart("first_name", 
 				settings.getFirstName() != null ? settings.getFirstName() : adtSettings.getFirstName()));
 		multipartForm.bodyPart(new FormDataBodyPart("last_name", 
 				settings.getLastName() != null ? settings.getLastName() : adtSettings.getLastName()));
 		if(settings.getWebsite() != null || adtSettings.getWebsite() != null) {
 			multipartForm.bodyPart(new FormDataBodyPart("website", 
 					settings.getWebsite() != null ? settings.getWebsite() : adtSettings.getWebsite()));
 		}
 		if(settings.getLocation() != null || adtSettings.getLocation() != null) {
 			multipartForm.bodyPart(new FormDataBodyPart("location", 
 					settings.getLocation() != null ? settings.getLocation() : adtSettings.getLocation()));
 		}
 		if(settings.getAbout() != null) {
 			multipartForm.bodyPart(new FormDataBodyPart("about", settings.getAbout()));
 		}
 		if(settings.getImage() != null) {
 			multipartForm.bodyPart(createImageBodyPart(settings.getImage()));
 		}
 		return multipartForm;
 	}
 	
 	public User updateUser(NewUserSettings settings) {
 		LOG.debug(String.format("Updating user=%s with settings=%s", getAccessToken().getLogin(), settings));
 		new APIRequestBuilder("settings/")
 			.setProtocol(Protocol.HTTPS)
 			.setAjaxUsage(false)
 			.setMethod(Method.POST, createUserForm(settings))
 			.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE)
 			.setHttpSuccessStatus(Status.FOUND)
 			.setErrorMessage(USER_API_ERROR)
 			.build();
 		User newUser = getUserForName(getAccessToken().getLogin());
 		LOG.debug("User updated. New user info: " + newUser);
 		return newUser;
 	}
 
 	public void followUser(User user, boolean follow) {
 		LOG.debug(String.format("Setting follow on user = %s to = %s", user, follow));
 		new APIRequestBuilder(user.getUserName() + "/follow/")
 			.setMethod(Method.POST, getSwitchForm("unfollow", follow))
 			.setErrorMessage(USER_API_ERROR)
 			.build().getResponse();
 		LOG.debug("Board follow mark set to " + follow);
 	}
 
 	public boolean isFollowing(User user) {
 		LOG.debug("Checking following status for user=" + user);
 		boolean followed = false;
 		if(getUserInfoPage(user.getUserName()).select("a.unfollowuserbutton").size() == 1) {
 			followed = true;
 		}
 		LOG.debug("Following state is " + followed);
 		return followed;
 	}
 
 	public FollowContainer getFollow(Followable followable, FollowType followType, int page, long marker) {
 		LOG.debug(String.format("Getting follows of type=%s for followable=%s on page=%d with marker=%d", followType.name(), followable, page, marker));
 		String path = null;
 		if(page != 1) {
 			path = String.format("%s/?page=%d&marker=%d", followType.name().toLowerCase(PINTEREST_LOCALE), page, marker);
 		}
 		else {
 			path = followType.name().toLowerCase(PINTEREST_LOCALE) + "/?page=1";
 		}
 		String entity = new APIRequestBuilder(followable.getURL() + path)
 			.setErrorMessage(USER_API_ERROR)
 			.build().getResponse().getEntity(String.class);
 		long newMarker = 0;
 		
 		Pattern pattern = Pattern.compile("\"marker\": (-?[0-9]+)");
 		Matcher m = pattern.matcher(entity);
 		if (m.find()) {
 			newMarker = Long.valueOf(m.group(1));
 		}
 		else {
 			pattern = Pattern.compile("settings.marker = (-?[0-9]+)");
 			m = pattern.matcher(entity);
 			if (m.find()) {
 				newMarker = Long.valueOf(m.group(1));
 			}
 			else {
 				throw new PinterestRuntimeException(USER_API_ERROR + "follow marker parsing error");
 			}
 		}
 		
 		List<User> users = new ArrayList<User>();
 		Document doc = Jsoup.parse(entity);
 		for(Element userElement : doc.select("div.PersonInfo")) {
 			User user = new LazyUser(userElement.getElementsByTag("a").first().attr(HREF_TAG_ATTR).replace("/", ""), getApiManager());
 			users.add(user);
 		}
 		
 		FollowContainer container = new FollowContainer(newMarker, users);
 		LOG.debug("Container with follow created: " + container);
 		return container;
 	}
 
 	public List<Activity> getActivity(User user) {
 		LOG.debug("Getting activity for user = " + user);
 		List<Activity> activities = new ArrayList<Activity>();
 		Document doc = new APIRequestBuilder(user.getURL() + "activity")
 			.setErrorMessage(USER_API_ERROR)
 			.build().getDocument();
 		for(Element activity : doc.select("div.activity")) {
 			Set<String> types = activity.classNames();
 			if(types.contains("activity-1")) {
 				activities.add(new PinActivity(ActivityType.PIN, new LazyPin(Long.valueOf(activity.attr(DATA_ID_TAG_ATTR)), getApiManager())));
 			}
 			else if(types.contains("activity-5")) {
 				activities.add(new PinActivity(ActivityType.REPIN, new LazyPin(Long.valueOf(activity.attr(DATA_ID_TAG_ATTR)), getApiManager())));
 			}
 			else if(types.contains("activity-6")) {
 				activities.add(new PinActivity(ActivityType.LIKE, new LazyPin(Long.valueOf(activity.attr(DATA_ID_TAG_ATTR)), getApiManager())));
 			}
 			else if(types.contains("activity-7")) {
 				
				Pattern pattern = Pattern.compile("“(.*?).”");
 				String info = activity.select("div.info").first().text();
 				Matcher m = pattern.matcher(info);
 				if (m.find()) {
 					String commentMsg = m.group(1);
 					Pin pin = new LazyPin(Long.valueOf(activity.attr(DATA_ID_TAG_ATTR)), getApiManager());
 					activities.add(new CommentActivity(pin, commentMsg));
 				}
 				
 			}
 			else if(types.contains("activity-45")) {
 				activities.add(new FollowUserActivity(new LazyUser((activity.getElementsByTag("a").attr(HREF_TAG_ATTR).replace("/", "")), getApiManager())));
 			}
 			else if(types.contains("activity-26")) {
 				Board board = new LazyBoard(activity.getElementsByTag("a").attr(HREF_TAG_ATTR), getApiManager());
 				Pattern pattern = Pattern.compile("Followed");
 				String info = activity.select("div.info").first().text();
 				Matcher m = pattern.matcher(info);
 				if (m.find()) {
 					activities.add(new FollowBoardActivity(board));
 				} 
 				else {
 					activities.add(new CreateBoardActivity(board));
 				}
 			}
 		}
 		LOG.debug("User activity is: " + activities);
 		return activities;
 	}
 	
 }
