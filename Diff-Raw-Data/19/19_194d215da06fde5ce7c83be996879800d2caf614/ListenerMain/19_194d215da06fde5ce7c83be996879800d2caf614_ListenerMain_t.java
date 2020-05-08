 package ai.ilikeplaces.logic.Listeners;
 
 import ai.ilikeplaces.entities.Location;
 import ai.ilikeplaces.entities.etc.HumanId;
 import ai.ilikeplaces.entities.etc.RefreshSpec;
 import ai.ilikeplaces.logic.Listeners.widgets.SignInOn;
 import ai.ilikeplaces.logic.Listeners.widgets.SignInOnCriteria;
 import ai.ilikeplaces.logic.Listeners.widgets.schema.thing.*;
 import ai.ilikeplaces.logic.crud.DB;
 import ai.ilikeplaces.logic.modules.Modules;
 import ai.ilikeplaces.rbs.RBGet;
 import ai.ilikeplaces.servlets.Controller;
 import ai.ilikeplaces.util.*;
 import ai.ilikeplaces.ygp.impl.Client;
 import ai.ilikeplaces.ygp.impl.ClientFactory;
 import ai.reaver.Return;
 import ai.reaver.ReturnImpl;
 import ai.scribble.License;
 import ai.scribble._todo;
 import org.itsnat.core.ItsNatDocument;
 import org.itsnat.core.ItsNatServletRequest;
 import org.itsnat.core.ItsNatServletResponse;
 import org.itsnat.core.event.ItsNatServletRequestListener;
 import org.itsnat.core.html.ItsNatHTMLDocument;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Element;
 import org.w3c.dom.html.HTMLDocument;
 import twitter4j.*;
 import twitter4j.conf.ConfigurationBuilder;
 import where.yahooapis.com.v1.Place;
 
 import java.text.MessageFormat;
 import java.util.*;
 
 import static ai.ilikeplaces.servlets.Controller.Page.*;
 import static ai.ilikeplaces.util.MarkupTag.*;
 
 import where.yahooapis.com.v1.Place;
 
 /**
  * @author Ravindranath Akila
  */
 
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 @_todo(task = "RENAME TO LISTENERLOCATION. DO A STRING SEARCH ON LISTENERMAIN TO FIND USAGE FIRST. CURRENT SEARCH SHOWS NO ISSUES. REFAC DELAYED TILL NEXT CHECK")
 public class ListenerMain implements ItsNatServletRequestListener {
 // ------------------------------ FIELDS ------------------------------
 
     public static final String NUMBER_OF_PHOTOS_FOR = "Number of photos for ";
 
     public static final String COLON = ":";
 
 
     final static protected String LocationId = RBGet.globalConfig.getString("LOCATIONID");
 
     private static final String RETURNING_LOCATION = "Returning location ";
 
     private static final String TO_USER = " to user";
 
     private static final String WRONG_WOEID_FORMAT = "SORRY! WRONG WOEID FORMAT";
 
     private static final String HTTP_SESSION_ATTR_LOCATION = "HttpSessionAttr.location";
 
     private static final String AI_ILIKEPLACES_LOGIC_LISTENERS_LISTENER_MAIN_0004 = "ai.ilikeplaces.logic.Listeners.ListenerMain.0004";
 
     private static final String AI_ILIKEPLACES_LOGIC_LISTENERS_LISTENER_MAIN_0005 = "ai.ilikeplaces.logic.Listeners.ListenerMain.0005";
 
     private static final String HTTP_TRAVEL_ILIKEPLACES_COM_INDEX_JSP_CID_317285_PAGE_NAME_HOT_SEARCH_SUBMITTED_TRUE_VALIDATE_CITY_TRUE_CITY = "http://travel.ilikeplaces.com/index.jsp?c" + "id" + "=317285&pageName=hotSearch&submitted=true&validateCity=true&city=";
 
     private static final String QUERIES_FOR_LOCATION = " queries for location ";
 
     private static final String OF = " of ";
 
     private static final String AI_ILIKEPLACES_RBS_GUI = "ai.ilikeplaces.rbs.GUI";
 
     private static final String PROFILE_PHOTOS = "PROFILE_PHOTOS";
 
     private static final String THIS_IS = "This is ";
 
     private static final String BACK_TO = "Back to";
 
     private static final String SPACE = " ";
 
     private static final String PLACE_LIST = "place_list";
 
     private static final String TRAVEL_TO = "Travel to ";
 
     private static final String PAGE = "/page/";
 
     private static final String _OF_ = "_of_";
 
     private static final String CLICK_TO_EXPLORE = "Click to explore ";
 
     private static final String VTIP = "vtip";
 
     private static final String WIDTH = "w" + "id" + "th";
 
     private static final String PX = "110px;";
 
     private static final String UNLOADING_BODY_TIME_SPENT = "Unloading body. Time spent:";
 
     private static final String COMMA = ",";
 
     private static final ClientFactory YAHOO_GEO_PLANET_FACTORY = Modules.getModules().getYahooGeoPlanetFactory();
 
     private static final com.disqus.api.impl.ClientFactory DISQUS_API_FACTORY = Modules.getModules().getDisqusAPIFactory();
 
     private static final String HTTP_DISQUS_COM_API_3_0_THREADS = "http://disqus.com/api/3.0/threads/";
 
     private static final String HTTP_DISQUS_COM_API_3_0_POSTS = "http://disqus.com/api/3.0/posts/";
 
     private static final String IDENT_WOEID = "id" + "ent:WOEID=";
 
     private static final String THREAD = "thread";
 
     private static final String LIST = "list";
 
     private static final String RESPONSE = "response";
 
     private static final String ID = "id";
 
     private static final String NUMBER_OF_ITEMS_AT_DISQUS_IN_THREAD = "Number of Items At Disqus in Thread:";
 
     private static final String RAW__MESSAGE = "raw_message";
 
     private static final String ERROR_IN_UC_DISQUS = "Error in UC DISQUS";
 
     private static final String ERROR_IN_UC_SET_LOGIN_WIDGET = "Error in UC setLoginW" + "id" + "get";
 
     private static final String ERROR_IN_UC_SEO = "Error in UC SEO";
 
     private static final String ERROR_IN_UC_SIGN_ON_DISPLAY_LINK = "Error in UC signOnDisplayLink";
 
     private static final String ERROR_IN_UC_SET_PROFILE_PHOTO_LINK = "Error in UC setProfilePhotoLink";
 
     private static final String ERROR_IN_UC_SET_LOCATION_ID_FOR_JSREFERENCE = "Error in UC setLocationIdForJSReference";
 
     private static final String ERROR_IN_UC_SHOW_UPLOAD_FILE_LINK = "Error in UC showUploadFileLink";
 
     private static final String ERROR_IN_UC_SET_LOCATION_NAME_FOR_JSREFERENCE = "Error in UC setLocationNameForJSReference";
 
     private static final String ERROR_IN_UC_SET_LOCATION_AS_PAGE_TOPIC = "Error in UC setLocationAsPageTopic";
 
     private static final String ERROR_IN_UC_NO_SUPPORT_FOR_NEW_LOCATIONS = "Error in UC noSupportForNewLocations";
 
     private static final RefreshSpec REFRESH_SPEC = new RefreshSpec("longMsgs");
 
     private static final String CREATED_AT = "createdAt";
 
     private static final String ERROR_IN_UC_SET_PROFILE_LINK = "Error in UC setProfileLink";
 
     private static final String PIPE = "|";
 
     final Twitter TWITTER = new TwitterFactory(new ConfigurationBuilder()
             .setOAuthConsumerKey("7h8258DVTL7DJBstoyjoXw")
             .setOAuthConsumerSecret("KjKfC124nB5CAmgvCV9AN64yNymJIIYRdafdWO3mAQ")
             .setOAuthAccessToken("170591510-3kqDPKxqfTHNQHlT5DgLyGOH8bJzJ18N5Ozonr98")
             .setOAuthAccessTokenSecret("sJpOU5bXO9dIPZIXE0vIQuZZTejb9Of5PxJRaMunsk")
             .build()).getInstance();
 
     private static final String AT_SIGN = "@";
 
     private static final String EMPTY = "";
 
 // ------------------------ INTERFACE METHODS ------------------------
 
 
 // --------------------- Interface ItsNatServletRequestListener ---------------------
 
     /**
      * @param request__
      * @param response__
      */
     @Override
     public void processRequest(final ItsNatServletRequest request__, final ItsNatServletResponse response__) {
         new AbstractListener(request__, response__) {
             protected String location;
 
             protected String superLocation;
 
             protected Long WOEID;
 
             /**
              * Initialize your document here by appending fragments
              */
             @Override
             @SuppressWarnings("unchecked")
             @_todo(task = "If location is not available, it should be added through a w" + "id" + "get(or fragment maybe?)")
             protected final void init(final ItsNatHTMLDocument itsNatHTMLDocument__, final HTMLDocument hTMLDocument__, final ItsNatDocument itsNatDocument__, final Object... initArgs) {
                 final SmartLogger sl = SmartLogger.start(
                         Loggers.LEVEL.DEBUG,
                         "Location Page.",
                         60000,
                         null,
                         true
                 );
 
 
                 //this.location = (String) request_.getServletRequest().getAttribute(RBGet.config.getString("HttpSessionAttr.location"));
                 getLocationSuperLocation:
                 {
                     final String[] attr = ((String) request__.getServletRequest().getAttribute(RBGet.globalConfig.getString(HTTP_SESSION_ATTR_LOCATION))).split("_");
                     location = attr[0];
                     if (attr.length == 3) {
                         superLocation = attr[2];
                     } else {
                         superLocation = EMPTY;
                     }
                     tryLocationId:
                     {
                         try {
                             final String w = request__.getServletRequest().getParameter(Location.WOEID);
                             if (w != null) {
                                 WOEID = Long.parseLong(request__.getServletRequest().getParameter(Location.WOEID));
                             }
                         } catch (final NumberFormatException e) {
                             Loggers.USER_EXCEPTION.error(WRONG_WOEID_FORMAT, e);
                         }
                     }
                 }
 
                 sl.appendToLogMSG(RETURNING_LOCATION + location + TO_USER);
 
                 final ResourceBundle gUI = ResourceBundle.getBundle(AI_ILIKEPLACES_RBS_GUI);
 
 
                 layoutNeededForAllPages:
                 {
                     setLoginWidget:
                     {
                         try {
                             new SignInOn(request__, $(Main_login_widget),
                                     new SignInOnCriteria()
                                             .setHumanId(new HumanId(getUsername()))
                                             .setSignInOnDisplayComponent(SignInOnCriteria.SignInOnDisplayComponent.MOMENTS)) {
                             };
                         } catch (final Throwable t) {
                             sl.l(ERROR_IN_UC_SET_LOGIN_WIDGET, t);
                         }
                     }
 
                     SEO:
                     {
                         try {
                             setMainTitle:
                             {
                                 $(mainTitle).setTextContent(MessageFormat.format(gUI.getString("woeidpage.title"), location));
                             }
                             setMetaDescription:
                             {
                                 $(mainMetaDesc).setAttribute(MarkupTag.META.content(), MessageFormat.format(gUI.getString("woeidpage.desc"), location));
                             }
                         } catch (final Throwable t) {
                             sl.l(ERROR_IN_UC_SEO, t);
                         }
                     }
                     signOnDisplayLink:
                     {
                         try {
                             if (getUsername() != null) {
                                 $(Main_othersidebar_identity).setTextContent(gUI.getString(AI_ILIKEPLACES_LOGIC_LISTENERS_LISTENER_MAIN_0004) + getUsernameAsValid());
                             } else {
                                 $(Main_othersidebar_identity).setTextContent(gUI.getString(AI_ILIKEPLACES_LOGIC_LISTENERS_LISTENER_MAIN_0005) + location);
                             }
                         } catch (final Throwable t) {
                             sl.l(ERROR_IN_UC_SIGN_ON_DISPLAY_LINK, t);
                         }
                     }
                     setProfileLink:
                     {
                         try {
                             if (getUsername() != null) {
                                 $(Main_othersidebar_profile_link).setAttribute(MarkupTag.A.href(), Controller.Page.Profile.getURL());
                             } else {
                                 $(Main_othersidebar_profile_link).setAttribute(MarkupTag.A.href(), Controller.Page.signup.getURL());
                             }
                         } catch (final Throwable t) {
                             sl.l(ERROR_IN_UC_SET_PROFILE_LINK, t);
                         }
                     }
                     setProfilePhotoLink:
                     {
                         try {
                             if (getUsername() != null) {
                                 /**
                                  * TODO check for db failure
                                  */
                                 String url = DB.getHumanCRUDHumanLocal(true).doDirtyRHumansProfilePhoto(new HumanId(getUsernameAsValid())).returnValueBadly();
                                 url = url == null ? null : RBGet.globalConfig.getString(PROFILE_PHOTOS) + url;
                                 if (url != null) {
                                     $(Main_profile_photo).setAttribute(MarkupTag.IMG.src(), url);
                                     //displayBlock($(Main_profile_photo));
                                 } else {
                                     //displayNone($(Main_profile_photo));
                                 }
                             } else {
                                 //displayNone($(Main_profile_photo));
                             }
                         } catch (final Throwable t) {
                             sl.l(ERROR_IN_UC_SET_PROFILE_PHOTO_LINK, t);
                         }
                     }
                 }
 
                 final Return<Location> r;
                 if (WOEID != null) {
                     r = DB.getHumanCRUDLocationLocal(true).doRLocation(WOEID, REFRESH_SPEC);
                 } else {
                     r = new ReturnImpl<Location>(ExceptionCache.UNSUPPORTED_OPERATION_EXCEPTION, "Search Unavailable", false);
                     //r = DB.getHumanCRUDLocationLocal(true).dirtyRLocation(location, superLocation);
                 }
 
 
                 if (r.returnStatus() == 0 && r.returnValue() != null) {
                     final Location existingLocation_ = r.returnValue();
 
                     GEO:
                     {
                         if (existingLocation_.getLocationGeo1() == null || existingLocation_.getLocationGeo2() == null) {
                             final Client ygpClient = YAHOO_GEO_PLANET_FACTORY.getInstance(RBGet.globalConfig.getString("where.yahooapis.com.v1.place"));
                             final Place place = ygpClient.getPlace(existingLocation_.getLocationId().toString());
                             existingLocation_.setLocationGeo1(Double.toString(place.getCoordinates().getY()));
                             existingLocation_.setLocationGeo2(Double.toString(place.getCoordinates().getX()));
 
                             new Thread(new Runnable() {
 
                                 @Override
                                 public void run() {
                                     DB.getHumanCRUDLocationLocal(true).doULocationLatLng(
                                             new Obj<Long>(existingLocation_.getLocationId()),
                                             new Obj<Double>(place.getCoordinates().getY()),
                                             new Obj<Double>(place.getCoordinates().getX()));
                                 }
                             }).run();
                         }
                     }
 
 
                     final Location locationSuperSet = existingLocation_.getLocationSuperSet();
                     GEO_WIDGET:
                     {
                         new ai.ilikeplaces.logic.Listeners.widgets.schema.thing.Comment(request__, new CommentCriteria(), $(Controller.Page.Main_right_column)) {
                             @Override
                             protected void init(CommentCriteria commentCriteria) {
                                 final ai.ilikeplaces.logic.Listeners.widgets.schema.thing.Place place = new ai.ilikeplaces.logic.Listeners.widgets.schema.thing.Place(request__,
                                         new PlaceCriteria()
                                                 //This Place
                                                 .setPlaceNamePre("Exciting events in ")
                                                 .setPlaceName(existingLocation_.getLocationName())
                                                 .setPlaceLat(existingLocation_.getLocationGeo1())
                                                 .setPlaceLng(existingLocation_.getLocationGeo2())
                                         //Parent Place
                                         //.setPlaceSuperName(locationSuperSet.getLocationName())
                                         //.setPlaceSuperLat(locationSuperSet.getLocationGeo1())
                                         //.setPlaceSuperLng(locationSuperSet.getLocationGeo2())
                                         //Parent WOEID
                                         //.setPlaceSuperWOEID(locationSuperSet.getWOEID().toString())
                                         ,
                                         $$(CommentIds.commentPerson));
                                 place.$$displayNone(place.$$(ai.ilikeplaces.logic.Listeners.widgets.schema.thing.Place.PlaceIds.placeWidget));
                             }
                         };
                     }
 
                     final List<String> titleManifest = new ArrayList<String>();
 
                     EVENTS_WIDGETS:
                     {
                         try {
                            /* final JSONObject jsonObject = Modules.getModules().getYahooUplcomingFactory()
                                     .getInstance("http://upcoming.yahooapis.com/services/rest/")
                                     .get("",
                                             new HashMap<String, String>() {
                                                 {//Don't worry, this is a static initializer of this map :)
                                                     put("method", "event.search");
                                                     put("woeid", WOEID.toString());
                                                     put("format", "json");
                                                 }
                                             }
 
                                     );
                             final JSONArray events = jsonObject.getJSONObject("rsp").getJSONArray("event");*/
 
                             final JSONObject jsonObject = Modules.getModules().getEventulFactory()
                                     .getInstance("http://api.eventful.com/json/events/search/")
                                     .get("",
                                             new HashMap<String, String>() {
                                                 {//Don't worry, this is a static initializer of this map :)
                                                     put("location", "" + existingLocation_.getLocationGeo1() + "," + existingLocation_.getLocationGeo2());
                                                     put("within", "" + 100);
                                                 }
                                             }
 
                                     );
 
                             Loggers.debug("Eventful Reply:" + jsonObject.toString());
 

//                            final String eventName = eventJSONObject.getString("title");
//                            final String eventUrl = eventJSONObject.getString("url");
//                            final String eventDate = eventJSONObject.getString("start_time");
//                            final String eventVenue = eventJSONObject.getString("venue_name");

                             final JSONArray events = jsonObject.getJSONObject("events").getJSONArray("event");
 
 
                             for (int i = 0; i < events.length(); i++) {
                                 final JSONObject eventJSONObject = new JSONObject(events.get(i).toString());
 
                                titleManifest.add(eventJSONObject.get("title").toString());
 
                                 new Event(request__, new EventCriteria()
                                        .setEventName(eventJSONObject.get("title").toString())
                                        .setEventStartDate(eventJSONObject.get("start_time").toString())
                                        .setEventPhoto(eventJSONObject.getJSONObject("image").get("url").toString())
                                         .setPlaceCriteria(
                                                 new PlaceCriteria()
                                                         .setPlaceNamePre("This event is taking place in ")
                                                         .setPlaceName(existingLocation_.getLocationName())
                                                         .setPlaceLat(eventJSONObject.get("latitude").toString())
                                                         .setPlaceLng(eventJSONObject.get("longitude").toString())
                                                                 //Parent Place
                                                         .setPlaceSuperNamePre(existingLocation_.getLocationName() + " is located in ")
                                                         .setPlaceSuperName(locationSuperSet.getLocationName())
                                                         .setPlaceSuperLat(locationSuperSet.getLocationGeo1())
                                                         .setPlaceSuperLng(locationSuperSet.getLocationGeo2())
                                                                 //Parent WOEID
                                                         .setPlaceSuperWOEID(locationSuperSet.getWOEID().toString())
 
                                         )
                                         , $(Controller.Page.Main_right_column));
                             }
                         } catch (final JSONException e) {
                             sl.l("Error fetching data from Yahoo Upcoming: " + e.getMessage());
                         }
                     }
 
 
                     TWITTER_WIDGETS:
                     {
                         try {
                             final Query _query = new Query("fun OR happening OR enjoy OR nightclub OR restaurant OR party OR travel :)");
                             _query.geoCode(new GeoLocation(Double.parseDouble(existingLocation_.getLocationGeo1()), Double.parseDouble(existingLocation_.getLocationGeo2())), 40, Query.MILES);
                             _query.setResultType(Query.MIXED);
                             final QueryResult result = TWITTER.search(_query);
 
                             //final QueryResult result = TWITTER.search(new Query("Happy").geoCode(new GeoLocation(Double.parseDouble(existingLocation_.getLocationGeo1()), Double.parseDouble(existingLocation_.getLocationGeo2())), 160, Query.MILES));
                             for (Status tweet : result.getTweets()) {
                                 new ai.ilikeplaces.logic.Listeners.widgets.schema.thing.Person(
                                         request__,
                                         new PersonCriteria()
                                                 .setPersonName(tweet.getUser().getName())
                                                 .setPersonPhoto(tweet.getUser().getProfileImageURL())
                                                 .setPersonData(tweet.getText()),
                                         $(Main_right_column)
                                 );
                                 titleManifest.add(tweet.getText());
                             }
                             if (result.getTweets().size() == 0) {
                                 sl.l("No twitter results found");
                             }
                         } catch (final Throwable t) {
                             sl.l("An error occurred during twitter fetch:" + t.getMessage());
                         }
                     }
 
 
                     SEO:
                     {
                         setMetaGEOData:
                         {
                             $(Main_ICBM).setAttribute(MarkupTag.META.content(), existingLocation_.getLocationGeo1() + COMMA + existingLocation_.getLocationGeo2());
                             $(Main_geoposition).setAttribute(MarkupTag.META.content(), existingLocation_.getLocationGeo1() + COMMA + existingLocation_.getLocationGeo2());
                             $(Main_geoplacename).setAttribute(MarkupTag.META.content(), existingLocation_.getLocationName());
                             $(Main_georegion).setAttribute(MarkupTag.META.content(), locationSuperSet.getLocationName());
                         }
                     }
 
                     setLocationIdForJSReference:
                     {
                         try {
                             final Element hiddenLocationIdInputTag = $(INPUT);
                             hiddenLocationIdInputTag.setAttribute(INPUT.type(), INPUT.typeValueHidden());
                             hiddenLocationIdInputTag.setAttribute(INPUT.id(), JSCodeToSend.LocationId);
                             hiddenLocationIdInputTag.setAttribute(INPUT.value(), existingLocation_.getLocationId().toString());
                             hTMLDocument__.getBody().appendChild(hiddenLocationIdInputTag);
 
                             $(Main_location_name).setAttribute(INPUT.value(), existingLocation_.getLocationName() + "");
                             $(Main_super_location_name).setAttribute(INPUT.value(), locationSuperSet.getLocationName() + "");
                         } catch (final Throwable t) {
                             sl.l(ERROR_IN_UC_SET_LOCATION_ID_FOR_JSREFERENCE, t);
                         }
                     }
 
                     setLocationNameForJSReference:
                     {
                         try {
                             final Element hiddenLocationIdInputTag = $(INPUT);
                             hiddenLocationIdInputTag.setAttribute(INPUT.type(), INPUT.typeValueHidden());
                             hiddenLocationIdInputTag.setAttribute(INPUT.id(), JSCodeToSend.LocationName);
                             hiddenLocationIdInputTag.setAttribute(INPUT.value(), existingLocation_.getLocationName() + OF + locationSuperSet.getLocationName());
                             hTMLDocument__.getBody().appendChild(hiddenLocationIdInputTag);
                         } catch (final Throwable t) {
                             sl.l(ERROR_IN_UC_SET_LOCATION_NAME_FOR_JSREFERENCE, t);
                         }
                     }
 
 
                     setLocationAsPageTopic:
                     {
                         try {
 
                             final StringBuilder title = new StringBuilder();
 
                             for (final String titleGuest : titleManifest) {
                                 title.append(titleGuest);
                             }
 
                             final String finalTitle = title.toString();
 
                             $(Main_center_main_location_title).setTextContent(finalTitle.isEmpty() ? (THIS_IS + existingLocation_.getLocationName() + OF + locationSuperSet) : finalTitle);
 
                             for (final Element element : generateLocationLinks(DB.getHumanCRUDLocationLocal(true).doDirtyRLocationsBySuperLocation(existingLocation_))) {
                                 $(Main_location_list).appendChild(element);
                                 displayBlock($(Main_notice_sh));
                             }
                         } catch (final Throwable t) {
                             sl.l(ERROR_IN_UC_SET_LOCATION_AS_PAGE_TOPIC, t);
                         }
                     }
                 } else {
                     noSupportForNewLocations:
                     {
                         try {
 //                            $(Main_notice).appendChild(($(P).appendChild(
 //                                    hTMLDocument__.createTextNode(RBGet.logMsgs.getString("CANT_FIND_LOCATION")
 //                                            + " Were you looking for "
 //                                    ))));
 //                            for (final Element element : generateLocationLinks(DB.getHumanCRUDLocationLocal(true).dirtyRLikeLocations(location))) {
 //                                $(Main_notice).appendChild(element);
 //                                displayBlock($(Main_notice_sh));
 //                            }
                             NotSupportingLikeSearchTooForNow:
                             {
                                 $(Main_notice).appendChild(($(P).appendChild(hTMLDocument__.createTextNode(RBGet.logMsgs.getString("CANT_FIND_LOCATION")))));
                             }
                         } catch (final Throwable t) {
                             sl.l(ERROR_IN_UC_NO_SUPPORT_FOR_NEW_LOCATIONS, t);
                         }
                     }
                 }
                 sl.complete(Loggers.LEVEL.SERVER_STATUS, Loggers.DONE);
             }
 
             /**
              * Use ItsNatHTMLDocument variable stored in the AbstractListener class
              */
             @Override
             protected void registerEventListeners(
                     final ItsNatHTMLDocument itsNatHTMLDocument__,
                     final HTMLDocument hTMLDocument__,
                     final ItsNatDocument itsNatDocument__) {
             }
 
             private List<Element> generateLocationLinks(final List<Location> locationList) {
                 final ElementComposer UList = ElementComposer.compose($(UL)).$ElementSetAttribute(MarkupTag.UL.id(), PLACE_LIST);
 
                 for (Location location : locationList) {
                     final Element link = $(A);
 
                     link.setTextContent(TRAVEL_TO + location.getLocationName() + OF + location.getLocationSuperSet().getLocationName());
 
                     link.setAttribute(A.href(),
                             PAGE
                                     + location.getLocationName()
                                     + _OF_
                                     + location.getLocationSuperSet().getLocationName()
                                     + Parameter.get(Location.WOEID, location.getWOEID().toString(), true));
 
                     link.setAttribute(A.alt(),
                             PAGE + location.getLocationName() + _OF_ + location.getLocationSuperSet().getLocationName());
 
                     link.setAttribute(A.title(),
                             CLICK_TO_EXPLORE + location.getLocationName() + OF + location.getLocationSuperSet().getLocationName());
 
                     link.setAttribute(A.classs(), VTIP);
 
 
                     final Element linkDiv = $(DIV);
 
 
                     linkDiv.appendChild(link);
 
                     UList.wrapThis(
                             ElementComposer.compose($(LI)).wrapThis(linkDiv).get()
                     );
                 }
 
                 final List<Element> elements = new ArrayList<Element>();
                 elements.add(UList.get());
                 return elements;
             }
 
             private Element generateLocationLink(final Location location) {
                 final Element link = $(A);
                 link.setTextContent(TRAVEL_TO + location.getLocationName() + OF + location.getLocationSuperSet().getLocationName());
                 link.setAttribute(A.href(),
                         PAGE
                                 + location.getLocationName()
                                 + _OF_
                                 + location.getLocationSuperSet().getLocationName()
                                 + Parameter.get(Location.WOEID, location.getWOEID().toString(), true));
 
                 link.setAttribute(A.alt(),
                         PAGE + location.getLocationName() + _OF_ + location.getLocationSuperSet().getLocationName());
                 return link;
             }
 
             private Element generateSimpleLocationLink(final Location location) {
                 final Element link = $(A);
                 link.setTextContent(location.getLocationName());
                 link.setAttribute(A.href(),
                         PAGE
                                 + location.getLocationName()
                                 + Parameter.get(Location.WOEID, location.getWOEID().toString(), true));
 
                 link.setAttribute(A.alt(),
                         PAGE + location.getLocationName());
                 return link;
             }
         };//Listener
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     private JSONObject getDisqusPosts(final long WOEID) throws JSONException {
         final com.disqus.api.impl.Client threads = DISQUS_API_FACTORY.getInstance(HTTP_DISQUS_COM_API_3_0_THREADS);
         final com.disqus.api.impl.Client posts = DISQUS_API_FACTORY.getInstance(HTTP_DISQUS_COM_API_3_0_POSTS);
 
         final Map<String, String> threadParams = new HashMap<String, String>();
         threadParams.put(THREAD, IDENT_WOEID + WOEID);
         final JSONObject threadJsonObject = threads.get(LIST, threadParams);
 
         final Map<String, String> postParams = new HashMap<String, String>();
         postParams.put(THREAD, threadJsonObject.getJSONArray(RESPONSE).getJSONObject(0).get(ID).toString());
         final JSONObject postJsonObject = posts.get(LIST, postParams);
         return postJsonObject;
     }
 }
 
