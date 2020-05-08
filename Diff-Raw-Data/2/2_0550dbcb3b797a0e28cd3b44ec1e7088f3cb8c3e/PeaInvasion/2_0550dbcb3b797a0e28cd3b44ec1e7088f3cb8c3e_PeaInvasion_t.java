 package org.aksw.verilinks.games.peaInvasion.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import playn.core.PlayN;
 import playn.html.HtmlAssetManager;
 import playn.html.HtmlGame;
 import playn.html.HtmlPlatform;
 import org.aksw.verilinks.games.peaInvasion.client.core.GameComponent;
 import org.aksw.verilinks.games.peaInvasion.client.core.info.Statistics;
 import org.aksw.verilinks.games.peaInvasion.client.oauth.Auth;
 import org.aksw.verilinks.games.peaInvasion.client.oauth.AuthRequest;
 import org.aksw.verilinks.games.peaInvasion.client.oauth.Callback;
 import org.aksw.verilinks.games.peaInvasion.client.panels.HighscorePanel;
 import org.aksw.verilinks.games.peaInvasion.client.panels.LandingPanel;
 import org.aksw.verilinks.games.peaInvasion.client.panels.StartPanel;
 import org.aksw.verilinks.games.peaInvasion.client.panels.StatisticsPanel;
 import org.aksw.verilinks.games.peaInvasion.client.panels.TutorialPanel;
 import org.aksw.verilinks.games.peaInvasion.client.verify.VerifyComponent;
 import org.aksw.verilinks.games.peaInvasion.shared.Bonus;
 import org.aksw.verilinks.games.peaInvasion.shared.Configuration;
 import org.aksw.verilinks.games.peaInvasion.shared.Balancing;
 import org.aksw.verilinks.games.peaInvasion.shared.GameConstants;
 import org.aksw.verilinks.games.peaInvasion.shared.Linkset;
 import org.aksw.verilinks.games.peaInvasion.shared.LoginConstants;
 import org.aksw.verilinks.games.peaInvasion.shared.Message;
 import org.aksw.verilinks.games.peaInvasion.shared.Task;
 import org.aksw.verilinks.games.peaInvasion.shared.Template;
 import org.aksw.verilinks.games.peaInvasion.shared.User;
 import org.aksw.verilinks.games.peaInvasion.shared.Verification;
 import org.aksw.verilinks.games.peaInvasion.shared.VerificationStatistics;
 import org.aksw.verilinks.games.peaInvasion.shared.rdfStatement;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoFacebookFriends;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoFacebookUser;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoGoogleUser;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoInstance;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoLink;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoLinkset;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoLinksetArray;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoProperty;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoHighscoreArray;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoScore;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoTemplate;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoTemplateArray;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoTemplateInstance;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoTemplateProperty;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoUserdata;
 import org.aksw.verilinks.games.peaInvasion.shared.jso.JsoUserstrength;
 import org.aksw.verilinks.games.peaInvasion.shared.msg.Instance;
 import org.aksw.verilinks.games.peaInvasion.shared.msg.Link;
 import org.aksw.verilinks.games.peaInvasion.shared.msg.Property;
 import org.aksw.verilinks.games.peaInvasion.shared.msg.Score;
 import org.aksw.verilinks.games.peaInvasion.shared.msg.Userdata;
 import org.aksw.verilinks.games.peaInvasion.shared.templates.TemplateInstance;
 import org.aksw.verilinks.games.peaInvasion.shared.templates.TemplateLinkset;
 import org.aksw.verilinks.games.peaInvasion.shared.templates.TemplateProperty;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.storage.client.Storage;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
 import com.google.gwt.user.client.rpc.InvocationException;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.jsonp.client.JsonpRequestBuilder;
 import com.google.gwt.maps.client.LoadApi;
 import com.google.gwt.maps.client.LoadApi.LoadLibrary;
 
 /**
  * Entry point class
  */
 public class PeaInvasion extends HtmlGame {
 
 //	public static final String SERVER_URL="http://localhost:8080/verilinks-server/";
	public static final String SERVER_URL="http://[2001:638:902:2010:0:168:35:113]:8080/verilinks-server/";
 	/** Semantic Web nerd, or newbie. */
 	private Configuration config;
 
 	private static final Auth AUTH = Auth.get();
 	private String accessToken;
 
 	public static final int MONEY_FOR_NOT_SURE = 20;
 	public static final int MONEY_FOR_VERIFICATION = 40;
 
 	private static final double ERROR_LIMIT = 0.125;
 	// private static final double ERROR_LIMIT = 0.0;
 
 	/**
 	 * Limit for right answers eval < Threshold -> wrong answer
 	 */
 	private static final double EVAL_THRESHOLD = 0.3;
 
 	/** Username for highscore */
 	private User user;
 	String userName;
 	/** User's choice from which interlinked ontologies the links come */
 	private Linkset linkset;
 
 	// private Statistics statistics;
 	private VerificationStatistics verificationStats;
 
 	// GUI
 	private VerifyComponent verifyComponent;
 	private Button verifyButton;
 
 	/** Lock verification mechanism */
 	private boolean verifyLock;
 
 	private StartPanel tabPanel;
 
 	// Test
 	private Button msgButton;
 	private Button linkMsg;
 	private Button testButton;
 
 	/** Users verification */
 	private Verification verification;
 
 	/** RDF link to verify */
 	private Link link;
 
 	private GameComponent game;
 
 	/** Disable Event Listening for GameComponent */
 	private boolean disableInput;
 
 	/** Start of level */
 	private boolean startOfLevel;
 
 	/** Start of game */
 	private boolean startOfGame;
 
 	/** Should server send already-evaluated link, to check user's credibility */
 	private boolean checkUserCredibility;
 
 	private boolean thisLink = false;
 	private boolean nextLink = false;
 
 	/** First Statement from server? Distinguish for GetLink Callback */
 	private boolean isFirstStatement;
 
 	/** Status of server */
 	private boolean serverRunning;
 
 	/** Saves last pushed key */
 	private int numKeyCache;
 
 	// Highscore
 	private PopupPanel popup;
 	private PopupPanel glass;
 	private PopupPanel menu;
 	private HighscorePanel highscorePanel;
 
 	/*
 	 * Money/Bonus calculation: notSure->10 valid/notValid ->20
 	 */
 	private boolean notSure;
 
 	private String difficulty;
 
 	/** Statistic for how long player plays game */
 	private long startTime;
 
 	/** ArrayList of interlinked ontologies */
 	private ArrayList<Linkset> linksetList;
 
 	// private ArrayList<String> ontologyList;
 
 	/** Template of subject ontology **/
 	private Template subjectTemplate;
 
 	/** Template of object ontology **/
 	private Template objectTemplate;
 
 	private TemplateLinkset template;
 
 	private int FIRST_QUERY_REQUEST = -100;
 
 	/**
 	 * This is the entry point method.
 	 */
 	@Override
 	public void start() {
 		// DOM.setStyleAttribute(RootPanel.getBodyElement(), "margin",
 		// "0px auto");
 		this.startOfGame = true;
 		this.isFirstStatement = true;
 		this.disableInput = true;
 		this.startOfLevel = true;
 		this.verifyLock = true;
 		this.checkUserCredibility = false;
 		this.user = new User();
 		this.verificationStats = new VerificationStatistics();
 
 		this.config = new Configuration();
 		// Check if page called from kongregate -> set in configuration
 		String urlParam = com.google.gwt.user.client.Window.Location
 				.getParameter("kongregate");
 		// Window.alert("urlParam: "+urlParam);
 		if (urlParam != null && urlParam.equals("true")) {
 			// Window.alert("Welcome Kongregate User! :)");
 			config.setKongregate(true);
 			DOM.setStyleAttribute(RootPanel.getBodyElement(), "width", "1030px");
 			// DOM.setStyleAttribute(RootPanel.getBodyElement(),
 			// "-moz-transform", "50%");
 			DOM.setStyleAttribute(RootPanel.getBodyElement(), "margin",
 					"0px auto");
 		} else if (urlParam == null) {
 			config.setSimple(true);
 			DOM.setStyleAttribute(RootPanel.getBodyElement(), "width", "1030px");
 			DOM.setStyleAttribute(RootPanel.getBodyElement(), "margin",
 					"0px auto");
 			// DOM.setStyleAttribute(RootPanel.getBodyElement(), "borderLeft",
 			// "1px solid grey");
 			// DOM.setStyleAttribute(RootPanel.getBodyElement(), "borderRight",
 			// "1px solid grey");
 			config.setKongregate(false);
 		}
 		this.serverRunning = false;
 		this.subjectTemplate = new Template();
 		this.objectTemplate = new Template();
 		loadMapApi();
 		initLanding();
 
 	}
 
 	private void loadMapApi() {
 		echo("LoadMapApi");
 		boolean sensor = true;
 
 		// load all the libs for use
 		ArrayList<LoadLibrary> loadLibraries = new ArrayList<LoadApi.LoadLibrary>();
 		loadLibraries.add(LoadLibrary.ADSENSE);
 		loadLibraries.add(LoadLibrary.DRAWING);
 		loadLibraries.add(LoadLibrary.GEOMETRY);
 		loadLibraries.add(LoadLibrary.PANORAMIO);
 		loadLibraries.add(LoadLibrary.PLACES);
 
 		Runnable onLoad = new Runnable() {
 			public void run() {
 				System.out.println("Map Api loaded");
 			}
 		};
 
 		LoadApi.go(onLoad, loadLibraries, sensor);
 	}
 
 	public void initLanding() {
 		echo("##Client: Initialize landing page");
 
 		final LandingPanel landingPanel = new LandingPanel();
 		// glass panel behind popUp
 		glass = new PopupPanel(false);
 		glass.setStyleName("rx-glass");
 		DOM.setStyleAttribute(glass.getElement(), "width", "100%");
 		DOM.setStyleAttribute(glass.getElement(), "height", "100%");
 		DOM.setStyleAttribute(glass.getElement(), "backgroundColor", "#000");
 		// DOM.setStyleAttribute(glass.getElement(),"opacity", "0.50");
 		// glass.addStyleName("glassPanel");
 		ClickHandler nextBtnClickHandler = new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				echo("Client: Next button clicked");
 				glass.hide();
 				menu.hide();
 				// Check if newbie
 				if (landingPanel.getNewbie())
 					config.setKnowledgeMode(Configuration.KNOWLEDGE_NORMAL);
 				else
 					config.setKnowledgeMode(Configuration.KNOWLEDGE_EXPERT);
 				connect();
 			}
 		};
 		landingPanel.setNextClickHandler(nextBtnClickHandler);
 
 		menu = new PopupPanel(false);
 		menu.add(landingPanel);
 		menu.setStyleName("noBorder");
 
 		// Check logged in
 		final AuthRequest reqFb = new AuthRequest(
 				LoginConstants.FACEBOOK_AUTH_URL,
 				LoginConstants.FACEBOOK_CLIENT_ID).withScopes(
 				LoginConstants.FACEBOOK_FRIENDLIST_SCOPE)
 		// Facebook expects a comma-delimited list of scopes
 				.withScopeDelimiter(",");
 		Storage stor = Storage.getLocalStorageIfSupported();
 		echo("Sotrage length: " + stor.getLength());
 		for (int i = 0; i < stor.getLength(); i++)
 			echo(i + ".Sotrage : " + stor.key(i));
 		echo("storage get : "
 				+ stor.getItem("278603465536665-----email,user_birthday,read_friendlists"));
 		echo("storage get friends: "
 				+ stor.getItem("276474835758653-----read_friendlists"));
 		echo("storage get google: "
 				+ stor.getItem("23285137063.apps.googleusercontent.com-----https://www.googleapis.com/auth/userinfo.profile"));
 
 		// addClearTokens(landingPanel);
 
 		glass.show();
 		menu.center();
 
 		// Focus button
 		landingPanel.focus();
 
 		// Test
 		Button butt = new Button("Logout");
 		butt.addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent arg0) {
 				// TODO Auto-generated method stub
 				logout();
 			}
 		});
 
 		Button asd = new Button("kongregate");
 		asd.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent arg0) {
 				// getToken();
 				echo("kongregate login");
 				kongregateLogin();
 
 			}
 		});
 
 		Button asd2 = new Button("kongregate");
 		asd2.addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent arg0) {
 				echo("rest test");
 				restTest();
 				// Window.Location.replace("http://localhost:8080/verilinks-server/server?service=getUserScore");
 
 			}
 		});
 
 		// hurr test
 		landingPanel.add(asd2);
 		// landingPanel.add(butt);
 
 	}
 
 	private void restTest() {
 		String url = "http://localhost:8080/verilinks-server/server?service=commitVerifications";
 		url = "http://localhost:8080/verilinks-server/server?service=getHighscore&game=peas";
 		url = "http://localhost:8080/verilinks-server/server?service=getLink"
 				+ "&userName=foo&userId=username-login: only name available"
 				+ "&verifiedLinks=55+33+11+1+2" + "&curLink=2"
 				+ "&nextLink=false" + "&verification=1"
 				+ "&linkset=dbpedia-linkedgeodata";
 		url = "http://localhost:8080/verilinks-server/server?service=getLinksets";
 		String data = "{ " + '"' + "verifiTest" + '"' + ":" + '"' + "Test"
 				+ '"' + "}";
 		echo(data);
 
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
 				URL.encode(url));
 		// builder.setHeader("Content-Type",
 		// "application/x-www-form-urlencoded");
 		// builder.setHeader("Access-Control-Allow-Origin", "*");
 		echo("REST: " + url);
 		try {
 			Request request = builder.sendRequest(data, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					// Couldn't connect to server (could be timeout, SOP
 					// violation, etc.)
 					echo("ERROR rest");
 				}
 
 				public void onResponseReceived(Request request,
 						Response response) {
 					// TODO Auto-generated method stub
 					echo("client SUCCESS rest");
 					Window.alert(response.getText());
 					JSONValue jsonValue = JSONParser.parseStrict(response
 							.getText());
 					System.out.println("jsonValue: " + jsonValue.toString());
 					JSONObject jsonObject = jsonValue.isObject(); // assert that
 																	// this is
 																	// an object
 					if (jsonObject == null) {
 						// uh oh, it wasn't an object after
 						// do error handling here
 						System.out
 								.println("JSON payload did not describe an object");
 						throw new RuntimeException(
 								"JSON payload did not describe an object");
 					} else
 						System.out.println("is object");
 					// Cast
 					System.out.println("#######SUBJECT: ");
 					JsoLinksetArray hS = jsonObject.getJavaScriptObject()
 							.cast();
 					System.out.println("casted");
 					linksetList = parseLinksets(hS.getLinkset());
 
 					// System.out.println("#######OBJECT: ");
 					// JsoLink h = jsonObject.getJavaScriptObject().cast();
 					// System.out.println("casted");
 					// parseInstance(h.getObject());
 					// String name = h.getSubject().getOntology().toString();
 					// Window.alert(name);
 					// System.out.println("%%%%%%%%%%%%%%"+h.getSubject().getOntology()+" %%%%%%%% "+h.getSubject().getPropertyNames());
 					// System.out.println("name: "+name);
 
 				}
 			});
 		} catch (RequestException e) {
 			// Couldn't connect to server
 			echo("ERROR!");
 		}
 		// -------------------------------------------------------------------------------
 		// try{
 		// JsonpRequestBuilder builder = new JsonpRequestBuilder();
 		// builder.requestObject(url, new AsyncCallback<JavaScriptObject>(){
 		//
 		// public void onSuccess(JavaScriptObject fbUser) {
 		// //Window.alert("Retrieving identity -> success!");
 		// echo("SUCCESS");
 		// Window.alert(fbUser.toString());
 		//
 		// }
 		//
 		// public void onFailure(Throwable e) {
 		// displayError("ERROR: Couldn't retrieve JSON -> "+e.getMessage());
 		// }});
 		// }catch (Exception e) {
 		// displayError("ERROR: Couldn't retrieve JSON!");
 		// }
 
 	}
 
 	public ArrayList<Linkset> parseLinksets(JsArray<JsoLinkset> linksetArray) {
 		echo("Client: Parse Linksets. Size = " + linksetArray.length());
 		linksetList.clear();
 		JsoLinkset lSet = null;
 		Linkset linkset = null;
 		for (int i = 0; i < linksetArray.length(); i++) {
 			lSet = linksetArray.get(i);
 			linkset = new Linkset();
 			linkset.setSubject(lSet.getSubject());
 			linkset.setObject(lSet.getObject());
 			linkset.setPredicate(lSet.getPredicate());
 			linkset.setDescription(lSet.getDescription());
 			linkset.setDifficulty(lSet.getDifficulty());
 			linksetList.add(linkset);
 		}
 		return linksetList;
 	}
 
 	private Instance parseInstance(JsoInstance inst) {
 		echo("Client: Parse Instance: " + inst.getUri());
 		Instance instance = new Instance();
 		List<Property> properties = new ArrayList<Property>();
 
 		// Get Property names
 		Property prop = null;
 		JsoProperty jOb = null;
 		JsArray propArray = inst.getProperties();
 		echo("Get Properties from JSON. Size: " + propArray.length());
 		String name = null;
 		String value = null;
 		System.out.println("pNames: " + propArray.toString());
 		for (int i = 0; i < propArray.length(); i++) {
 			jOb = propArray.get(i).cast();
 			name = jOb.getProperty();
 			value = jOb.getValue();
 			prop = new Property(name, value);
 			properties.add(prop);
 			// System.out.println(i+"pNames: "+name+ " value: "+value);
 		}
 		instance.setUri(inst.getUri());
 		instance.setProperties(properties);
 		System.out.println("uri: " + instance.getUri());
 		for (int i = 0; i < inst.getProperties().length(); i++) {
 			System.out.println(i + ".: "
 					+ inst.getProperties().get(i).getProperty() + " , v: "
 					+ inst.getProperties().get(i).getValue());
 		}
 		return instance;
 	}
 
 	public ArrayList<Score> parseHighscore(JsArray<JsoScore> highscoreArray) {
 		echo("Client: Parse Highscore. Size = " + highscoreArray.length());
 		ArrayList<Score> highscoreList = new ArrayList<Score>();
 
 		JsoScore jScore = null;
 		Score score = null;
 		for (int i = 0; i < highscoreArray.length(); i++) {
 			jScore = highscoreArray.get(i);
 			score = new Score();
 			score.setId(jScore.getId());
 			score.setName(jScore.getName());
 			score.setScore(jScore.getScore());
 			highscoreList.add(score);
 		}
 		return highscoreList;
 	}
 
 	public Userdata parseUserdata(JsoUserdata userdata) {
 		if (userdata.getId() == null)
 			return null;
 		echo("Client: Parse Userdata. " + userdata.getId());
 
 		Userdata data = new Userdata();
 		data.setId(userdata.getId());
 		data.setName(userdata.getName());
 		data.setHighscore(userdata.getHighscore());
 		data.setStrength(userdata.getStrength());
 
 		return data;
 	}
 
 	public void parseTemplate(JsoTemplateArray tmp) {
 		echo("Parse Template size = " + tmp.getTemplates().length());
 
 		JsoTemplate jTmp = null;
 		echo("linkset: " + linkset.getName());
 		for (int i = 0; i < tmp.getTemplates().length(); i++) {
 			jTmp = tmp.getTemplates().get(i);
 			echo(i + ". " + jTmp.getId());
 			if (this.linkset.getName().equals(jTmp.getId())) {
 				echo("found linkset in tmp");
 				break;
 			}
 		}
 
 		// here
 		JsoTemplateProperty prop = null;
 		JsoTemplateInstance jSub = jTmp.getSubject();
 		echo("jSub: " + jSub.getType());
 
 		List<TemplateProperty> propSubList = new ArrayList<TemplateProperty>();
 		TemplateProperty propSub = null;
 		for (int j = 0; j < jSub.getProperties().length(); j++) {
 			prop = jSub.getProperties().get(j);
 			echo(j + ". " + prop.getProperty());
 			propSub = new TemplateProperty();
 			propSub.setProperty(prop.toString());
 			propSubList.add(propSub);
 		}
 
 		TemplateInstance sub = new TemplateInstance();
 		sub.setType(jSub.getType());
 		sub.setProperties(propSubList);
 		echo("subject instance tempatle done");
 
 		// ob
 		JsoTemplateInstance jOb = jTmp.getObject();
 		echo("jOb: " + jOb.getType());
 
 		List<TemplateProperty> propObList = new ArrayList<TemplateProperty>();
 		TemplateProperty propOb = null;
 		for (int j = 0; j < jOb.getProperties().length(); j++) {
 			prop = jOb.getProperties().get(j);
 			echo(j + ". " + prop.getProperty());
 			propOb = new TemplateProperty();
 			propOb.setProperty(prop.toString());
 			propObList.add(propOb);
 		}
 
 		TemplateInstance ob = new TemplateInstance();
 		ob.setType(jOb.getType());
 		ob.setProperties(propObList);
 		echo("object instance tempatle done");
 
 		this.template = new TemplateLinkset();
 		template.setLinkset(linkset.getName());
 		template.setSubject(sub);
 		template.setObject(ob);
 		template.setPredicate("same as");
 		
 		initGUI();
 		initCallback();
 		initHandler();
 		sendUser();
 
 	}
 
 	private void kongregateLogin() {
 
 		echo("Client: Kongregate Request");
 		setKongregateUser();
 		// String url =
 		// "http://www.kongregate.com/api/user_info.json?username=iq1i&friends=true";
 		// this.callbackKongregateRequest = new AsyncCallback<String>() {
 		// public void onFailure(Throwable caught) {
 		// echo("Client: ERROR Get KongregateLogin! "+caught.getMessage());
 		// Window.alert("ERROR: Get KongregateLogin: "+caught.getMessage());
 		// }
 		// public void onSuccess(String stmt) {
 		// echo("Client: Get KongregateLogin success!");
 		// // Set user
 		// setKongregateUser();
 		// Window.alert("Success Get KongregateLogin: "+stmt);
 		// }
 		// };
 		//
 		// service.kongregateRequest(url, callbackKongregateRequest);
 
 		// this.callbackKongregateLogin = new AsyncCallback<String>() {
 		// public void onFailure(Throwable caught) {
 		// echo("Client: ERROR Get KongregateLogin! "+caught.getMessage());
 		// Window.alert("ERROR Get KongregateLogin: "+caught.getMessage());
 		// }
 		// public void onSuccess(String stmt) {
 		// echo("Client: Get KongregateLogin success!");
 		// Window.alert("Success Get KongregateLogin: "+stmt);
 		// }
 		// };
 		//
 		// service.kongregateLogin("userId", "token", "apiKey",
 		// callbackKongregateLogin);
 
 	}
 
 	public native void setKongregateUser() /*-{
 		// Call instance method instanceFoo() on this
 		//var name = $wnd.kongregate.kongregate.services.getUserId();
 
 		$wnd.checkUserAuthenticated();
 		var s = $wnd.userName;
 		this.@org.aksw.verilinks.games.peaInvasion.client.PeaInvasion::setUserNameKongregate(Ljava/lang/String;)(s);
 	}-*/;
 
 	private void setUserNameKongregate(String name) {
 		if (name != null) {
 			this.user.setName(name);
 			// Window.alert("KongregateUserName: "+user.getName());
 		} else {
 			user.setGuest();
 			Window.alert("Error: Couldn't get kongregate username! Therefore logged in as 'Guest'");
 		}
 
 	}
 
 	private void fbRequestJsonp() {
 		fbRequestIdentityJsonp();
 	}
 
 	private void fbRequestIdentityJsonp() {
 		echo("Client: Identity Request");
 		String url = "https://graph.facebook.com/me?access_token="
 				+ accessToken;
 		try {
 			JsonpRequestBuilder builder = new JsonpRequestBuilder();
 			builder.requestObject(url, new AsyncCallback<JsoFacebookUser>() {
 
 				public void onSuccess(JsoFacebookUser fbUser) {
 					// Window.alert("Retrieving identity -> success!");
 					user.setId(fbUser.getId());
 					user.setName(fbUser.getName());
 					echo("Retrieving identity -> success!");
 					echo("id: " + user.getId());
 					echo("name: " + user.getName());
 
 					fbRequestFriendsJsonp();
 				}
 
 				public void onFailure(Throwable e) {
 					displayError("ERROR: Couldn't retrieve JSON -> "
 							+ e.getMessage());
 				}
 			});
 		} catch (Exception e) {
 			displayError("ERROR: Couldn't retrieve JSON!");
 		}
 	}
 
 	private void fbRequestIdentityOnly() {
 		echo("Client: Identity Request");
 		String url = "https://graph.facebook.com/me?access_token="
 				+ accessToken;
 		try {
 			JsonpRequestBuilder builder = new JsonpRequestBuilder();
 			builder.requestObject(url, new AsyncCallback<JsoFacebookUser>() {
 
 				public void onSuccess(JsoFacebookUser fbUser) {
 					// Window.alert("Retrieving identity -> success!");
 					user.setId(fbUser.getId());
 					user.setName(fbUser.getName());
 					echo("Retrieving identity -> success!");
 					echo("id: " + user.getId());
 					echo("name: " + user.getName());
 					if (!user.getName().isEmpty())
 						// Disable login
 						disableLogin();
 					else
 						echo("Auto-login: User is null!");
 				}
 
 				public void onFailure(Throwable e) {
 					displayError("ERROR: Couldn't retrieve JSON -> "
 							+ e.getMessage());
 				}
 			});
 		} catch (Exception e) {
 			displayError("ERROR: Couldn't retrieve JSON!");
 		}
 	}
 
 	private void fbRequestFriendsJsonp() {
 		echo("Client: Friend Request");
 		String url = "https://graph.facebook.com/me/friends?access_token="
 				+ accessToken;
 		try {
 			JsonpRequestBuilder builder = new JsonpRequestBuilder();
 			builder.requestObject(url, new AsyncCallback<JsoFacebookFriends>() {
 
 				public void onSuccess(JsoFacebookFriends friends) {
 					Window.alert("Login successful! :)");
 					echo("SUCCESS: " + friends.getEntries());
 					echo("friends length: " + friends.getEntries().length());
 					// // Proint friends
 					// for(int i=0;i<friends.getEntries().length();i++){
 					// FacebookFriendsEntry entry = friends.getEntries().get(i);
 					// echo(i+".name: "+entry.getName());
 					// }
 
 					disableLogin();
 				}
 
 				public void onFailure(Throwable e) {
 					displayError("ERROR: Couldn't retrieve JSON -> "
 							+ e.getMessage());
 				}
 			});
 		} catch (Exception e) {
 			displayError("ERROR: Couldn't retrieve JSON!");
 		}
 	}
 
 	private void googleRequestJsonp() {
 		googleRequestIdentityJsonp(true);
 	}
 
 	/**
 	 * Login Google id.
 	 * 
 	 * @param bool
 	 *            show alert when success.
 	 */
 	private void googleRequestIdentityJsonp(final boolean bool) {
 		echo("Client: Identity Request");
 		String url = "https://www.googleapis.com/oauth2/v1/userinfo?access_token="
 				+ accessToken;
 		echo(url);
 		try {
 			JsonpRequestBuilder builder = new JsonpRequestBuilder();
 			builder.requestObject(url, new AsyncCallback<JsoGoogleUser>() {
 
 				public void onSuccess(JsoGoogleUser googleUser) {
 					// Window.alert("Retrieving identity -> success!");
 					user.setId(googleUser.getId());
 					user.setName(googleUser.getName());
 					echo("Retrieving identity -> success!");
 					echo("id: " + user.getId());
 					echo("name: " + user.getName());
 					if (!user.getName().isEmpty()) {
 						// Disable login
 						disableLogin();
 						if (bool)
 							Window.alert("Login successful! :)");
 					} else
 						echo("Auto-login: User is null!");
 
 					// googleRequestFriendsJsonp();
 
 				}
 
 				public void onFailure(Throwable e) {
 					displayError("ERROR: Couldn't retrieve JSON -> "
 							+ e.getMessage());
 				}
 			});
 		} catch (Exception e) {
 			displayError("ERROR: Couldn't retrieve JSON!");
 		}
 	}
 
 	//
 	// private void fbRequest(String url){
 	// echo("URL: "+url);// SOP
 	// // Send request to server and catch any errors.
 	// RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
 	//
 	// try {
 	// Request request = builder.sendRequest(null, new RequestCallback() {
 	// public void onError(Request request, Throwable exception) {
 	// displayError("Couldn't retrieve JSON");
 	// }
 	//
 	// public void onResponseReceived(Request request, Response response) {
 	// if (200 == response.getStatusCode()) {
 	// Window.alert(response.getText());
 	// echo(response.getStatusText());
 	// JSONValue jsonValue = JSONParser.parse(response.getText());
 	// JSONObject jsonObject = jsonValue.isObject();
 	// echo("NAME: "+jsonObject.get("name").toString());
 	//
 	//
 	// } else {
 	// displayError("Couldn't retrieve JSON (" + response.getStatusText()
 	// + ")");
 	// }
 	// }
 	// });
 	// } catch (RequestException e) {
 	// displayError("Couldn't retrieve JSON: "+e.getMessage());
 	// }
 	// }
 
 	private void disableLogin() {
 		tabPanel.disableLogin(user.getName());
 		tabPanel.enableStart();
 	}
 
 	/**
 	 * If can't get JSON, display error message.
 	 * 
 	 * @param error
 	 */
 	private void displayError(String error) {
 		Window.alert(error);
 	}
 
 	// //////////////////////////////////////////////////////////////////////////
 	// CLEARING STORED TOKENS
 	// ///////////////////////////////////////////////////
 	// //////////////////////////////////////////////////////////////////////////
 
 	// Clears all tokens stored in the browser by this library. Subsequent calls
 	// to login() will result in the popup being shown, though it may
 	// immediately
 	// disappear if the token has not expired.
 	private void addClearTokens(LandingPanel panel) {
 		Button button = new Button("Clear stored tokens");
 		button.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				Auth.get().clearAllTokens();
 				Window.alert("All tokens cleared");
 			}
 		});
 		panel.add(button);
 	}
 
 	/**
 	 * Create GUI
 	 */
 	private void initGUI() {
 		echo("Init GUI");
 		this.verifyComponent = new VerifyComponent(template, config);
 		
 		// VerifyComponent Button handler
 		PushButton trueButton = verifyComponent.getTrueButton();
 		trueButton.addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent event) {
 				key1Pressed();
 			}
 		});
 
 		PushButton falseButton = verifyComponent.getFalseButton();
 		falseButton.addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent event) {
 				key2Pressed();
 
 			}
 		});
 
 		PushButton unsureButton = verifyComponent.getUnsureButton();
 		unsureButton.addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent event) {
 				key3Pressed();
 
 			}
 		});
 		echo("eins");
 		this.verifyButton = verifyComponent.getOkButton();
 		// TODO GameMessage
 		msgButton = new Button("Game Messages");
 		msgButton.setStyleName("gameMessage");
 		msgButton.setEnabled(false);
 
 		// Link message
 		linkMsg = new Button("Link Messages");
 		linkMsg.setStyleName("gameMessage");
 		linkMsg.setEnabled(false);
 
 		testButton = new Button("Test2");
 		// testButton.setEnabled(false);
 		// We can add style names to widgets
 		testButton.addStyleName("testButton");
 
 		echo("zwei");
 		RootPanel.get().add(msgButton, 0, 399);
 
 		// Bottom Buttons
 		// Show tutorial
 		Button showTut = new Button("Tutorial");
 		showTut.addStyleName("myButton");
 		showTut.addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent event) {
 				showTutorial();
 			}
 		});
 
 		Button showOverview = new Button("Quick Overview");
 		showOverview.addStyleName("myButton");
 
 		echo("drei");
 		initOverview(showOverview);
 
 		echo("vire");
 		HorizontalPanel bottom = new HorizontalPanel();
 		bottom.add(showTut);
 		bottom.add(showOverview);
 		bottom.setHorizontalAlignment(HasAlignment.ALIGN_RIGHT);
 		RootPanel.get("bottomButtons").add(bottom);
 
 		echo("Client: Init GUI end");
 	}
 
 	/**
 	 * Create callbacks
 	 */
 	private void initCallback() {
 		echo("Client: Init Callback");
 
 		echo("Client: Init Callback Done");
 	}
 
 	/**
 	 * Create EventHandler
 	 */
 	private void initHandler() {
 		echo("Client: Init Handler");
 		echo("MyHandler");
 		// Add EventHandler
 		// MyHandler handler = new MyHandler();
 		// verifyButton.addClickHandler(handler);
 		msgButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				// dialogBox.hide();
 				// msgButton.setEnabled(false);
 				// msgButton.setFocus(true);
 				// service.firstRequest(callback);
 				// highscoreRequest();
 				// System.out.println("kommschonnn");
 				// if(game.isReady())
 				{
 					msgButton.setEnabled(false);
 					firstQueryRequest();
 					game.resume();
 				}
 			}
 		});
 
 		echo("testButton");
 		testButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				// dialogBox.hide();
 				testButton.setEnabled(false);
 				// testButton.setFocus(true);
 				// service.firstRequest(callback);
 				// firstQueryRequest();
 				// Test current dir
 				System.out.println(GWT.getModuleBaseURL());
 				testButton.setText(GWT.getModuleBaseURL());
 				// DBTool db = new DBTool();
 				// testButton.setText(db.bla());
 				templateRequest();
 				// linksetRequest();
 			}
 		});
 		echo("Client: Init Handler Done");
 	}
 
 	/**
 	 * Initialize 2D platform game.
 	 */
 	private void initGame() {
 		echo("Client: Init Game");
 		game = new GameComponent();
 
 		game.setApplication(this);
 		// VerticalPanel panel = new VerticalPanel();
 		HtmlAssetManager assets = HtmlPlatform.register().assetManager();
 		// assets.setPathPrefix("saim/");
 		PlayN.run(game);
 		echo("Client: Init Game Done");
 
 //		Button testB = new Button("test");
 //		testB.addClickHandler(new ClickHandler() {
 //
 //			public void onClick(ClickEvent arg0) {
 //				commitVerifications();
 //
 //			}
 //		});
 //		RootPanel.get("bottomButtons").add(testB);
 
 	}
 
 	private void initLeavePage() {
 		Window.addWindowClosingHandler(new Window.ClosingHandler() {
 			public void onWindowClosing(Window.ClosingEvent closingEvent) {
 				// closingEvent.setMessage("Do you really want to leave the page?");
 				// Disconnect user from server
 				disconnectUser();
 			}
 
 		});
 	}
 
 	private void initStartTime() {
 		this.startTime = System.currentTimeMillis();
 		this.user.setStartTime(startTime);
 		echo("Game Time Start: " + this.startTime);
 	}
 
 	/**
 	 * Initiliaize Tutorial Pop Up
 	 */
 	private void initTutorial() {
 		// Glass panel behind popUp
 		glass = new PopupPanel(false);
 		glass.setStyleName("rx-glass");
 		DOM.setStyleAttribute(glass.getElement(), "width", "100%");
 		DOM.setStyleAttribute(glass.getElement(), "height", "100%");
 		DOM.setStyleAttribute(glass.getElement(), "backgroundColor", "#000");
 		DOM.setStyleAttribute(glass.getElement(), "opacity", "0.30");
 
 		final PopupPanel pop = new PopupPanel(false);
 
 		TutorialPanel tutorialPanel = new TutorialPanel();
 
 		ClickHandler skipHandler = new ClickHandler() {
 
 			public void onClick(ClickEvent arg0) {
 				// Set skip tutorial into local storage
 				setSkipTutorial();
 				firstQueryRequest();
 				glass.hide();
 				pop.hide();
 			}
 		};
 		tutorialPanel.setSkipClickHandler(skipHandler);
 
 		pop.add(tutorialPanel);
 		glass.add(pop);
 
 		pop.center();
 		pop.setStyleName("noBorder");
 		tutorialPanel.setGlassPanel(pop);
 		glass.show();
 
 	}
 
 	private void setSkipTutorial() {
 		echo("Client: Set skip tutorial");
 		Storage store = Storage.getLocalStorageIfSupported();
 		store.setItem("skipTutorial", user.getName());
 		echo("Client: Set skip tutorial Done");
 	}
 
 	private boolean getSkipTutorial() {
 		echo("Client: Get skip tutorial");
 		String key = null;
 		Storage store = Storage.getLocalStorageIfSupported();
 		for (int i = 0; i < store.getLength(); i++) {
 			echo(i + ".Sotrage : " + store.key(i));
 			key = store.key(i);
 			if (key.startsWith("skipTutorial")
 					&& store.getItem(key).equals(user.getName())) {
 				echo("Found token!: " + key + " : " + store.getItem(key));
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void showTutorial() {
 		// Pause game
 		if (!game.world.isPaused())
 			game.pause();
 
 		// Glass panel behind popUp
 		glass = new PopupPanel(false);
 		glass.setStyleName("rx-glass");
 		DOM.setStyleAttribute(glass.getElement(), "width", "100%");
 		DOM.setStyleAttribute(glass.getElement(), "height", "100%");
 		DOM.setStyleAttribute(glass.getElement(), "backgroundColor", "#000");
 		DOM.setStyleAttribute(glass.getElement(), "opacity", "0.30");
 
 		final PopupPanel pop = new PopupPanel(false);
 
 		TutorialPanel tutorialPanel = new TutorialPanel();
 
 		// Change next text
 		tutorialPanel.setNextText();
 
 		ClickHandler skipHandler = new ClickHandler() {
 
 			public void onClick(ClickEvent arg0) {
 				glass.hide();
 				pop.hide();
 				game.resume();
 			}
 		};
 		tutorialPanel.setSkipClickHandler(skipHandler);
 
 		pop.add(tutorialPanel);
 		glass.add(pop);
 
 		glass.show();
 		pop.center();
 		pop.setStyleName("noBorder");
 		tutorialPanel.setGlassPanel(pop);
 	}
 
 	private void initOverview(Button pop) {
 
 		VerticalPanel enemies = new VerticalPanel();
 		DOM.setStyleAttribute(enemies.getElement(), "padding", "20px");
 		HTML head = new HTML("<h2>Enemies</h2>");
 		HTML html = new HTML(
 				""
 						+ "<p><img src='PeaInvasion/images/pea/enemy.png'/> Your everyday invader. Big, round and clumsy. He can roll around all day.</p>"
 						+ "<p><img src='PeaInvasion/images/pea/enemyShooter2.png'/> This guy is on a strict diet. He's become so light that he overcame gravity, don't let him crash into the village.</p>"
 						+ "<p><img src='PeaInvasion/images/pea/enemyCashier.png'/> Well known across the whole universe. The Cashier loves your coins, he will steal them on a regular basis. </p>");
 		Image enemyNormal = new Image("PeaInvasion/images/pea/enemy.png");
 		Image enemyPilot = new Image("PeaInvasion/images/pea/enemyShooter2.png");
 		Image enemyCashier = new Image(
 				"PeaInvasion/images/pea/enemyCashier.png");
 		HTML h1 = new HTML(
 				"Your everyday invader. Big, round and clumsy. His speciality is rolling.");
 		HTML h2 = new HTML(
 				"This guy has the ability to fly. But he's not really good at it. Sometimes he just runs out of gas in midair.");
 		HTML h3 = new HTML(
 				"The Cashier loves coins, especially your coins. He will steal them on a regular basis.");
 
 		HorizontalPanel p1 = new HorizontalPanel();
 		p1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		p1.add(enemyNormal);
 		p1.add(h1);
 		p1.setSpacing(5);
 
 		HorizontalPanel p2 = new HorizontalPanel();
 		p2.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		p2.add(enemyPilot);
 		p2.add(h2);
 		p2.setSpacing(5);
 
 		HorizontalPanel p3 = new HorizontalPanel();
 		p3.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		p3.add(enemyCashier);
 		p3.add(h3);
 		p3.setSpacing(5);
 
 		enemies.setSpacing(20);
 		enemies.add(head);
 		enemies.add(p1);
 		enemies.add(p2);
 		enemies.add(p3);
 		// overview.add(enemyCashier);
 
 		VerticalPanel prize = new VerticalPanel();
 		DOM.setStyleAttribute(prize.getElement(), "padding", "20px");
 		HTML prizeHTML = new HTML(
 				"<h2>Coin Distributions</h2></p>"
 						+ "<i>Reward for answering a question:</i></br><img src='PeaInvasion/images/verification/true.png' />/<img src='PeaInvasion/images/verification/false.png'/>: <font color='green'>  + 40</font> Coins"
 						+ "</br><img src='PeaInvasion/images/verification/unsure.png'/>: <font color='green'>  + 20</font> Coins</p>"
 						+ "<p><i>Bonus:</i></br>Disagreement = 0 Coins</br>Agreement = <font color='green'>  + 10</font> to <font color='green'>  + 1000</font> Coins (Depending on question difficulty)"
 						+ "</br>Penalty = <font color='red'> - 400 </font>Coins</p>");
 		prize.add(prizeHTML);
 
 		final TabPanel tab = new TabPanel();
 		// tab.setAnimationDuration(1000);
 		tab.add(enemies, new HTML("Enemies"));
 		tab.add(prize, new HTML("Coins"));
 		tab.selectTab(0);
 		tab.setSize("600px", "400px");
 		tab.getDeckPanel().setStyleName("no-border-style");
 		DOM.setStyleAttribute(tab.getElement(), "backgroundColor", "black");
 		DOM.setStyleAttribute(tab.getElement(), "color", "white");
 		DOM.setStyleAttribute(tab.getElement(), "fontWeight", "bolder");
 
 		final PopupPanel popUp = new PopupPanel(true);
 		popUp.setAnimationEnabled(true);
 		popUp.add(tab);
 		popUp.setAnimationEnabled(true);
 		pop.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				showOverview(popUp);
 			}
 		});
 
 	}
 
 	private void showOverview(PopupPanel pop) {
 		// Pause game
 		if (!game.world.isPaused())
 			game.pause();
 
 		pop.center();
 		pop.setStyleName("noBorder");
 		pop.show();
 	}
 
 	/**
 	 * Initialize start menu for selecting linkset. Do autologin if needed.
 	 */
 	private void initStartPanel() {
 		// initHighscore();
 		highscoreRequest();
 		tabPanel = new StartPanel(1.5, Unit.EM, config);
 		tabPanel.setLinkset(linksetList);
 		tabPanel.setHighscorePanel(highscorePanel);
 		tabPanel.initGUI();
 
 		// glass panel behind popUp
 		glass = new PopupPanel(false);
 		glass.setStyleName("rx-glass");
 		DOM.setStyleAttribute(glass.getElement(), "width", "100%");
 		DOM.setStyleAttribute(glass.getElement(), "height", "100%");
 		DOM.setStyleAttribute(glass.getElement(), "backgroundColor", "#000");
 
 		menu = new PopupPanel(false);
 		menu.add(tabPanel);
 		DOM.setStyleAttribute(menu.getElement(), "border", "1px black solid");
 		DOM.setStyleAttribute(menu.getElement(), "padding", "0px");
 		glass.show();
 		menu.center();
 
 		checkLoginNeeded();
 
 	}
 
 	private void checkLoginNeeded() {
 		echo("##Client: Check if login is needed");
 		// Check whether user is logged in (but not with kongregate)
 		if (config.getLoginNeeded() == true) {
 			echo("Yeah, it's needed");
 			autoLogin();
 			// Set button ClickHandler for StartPanel
 			setStartPanelClickHandlers();
 		} else {
 			echo("No, it's not needed.");
 			// Kongregate Login
 			kongregateLogin();
 			// Only need to set Start Button handler
 			ClickHandler startBtnClickHandler = new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					echo("Client: Start button clicked");
 					// Check if necessary input entered
 					if (!tabPanel.isEmpty()) {
 
 						if (tabPanel.getLinkset() != null) {// LinksSpec
 							linkset = tabPanel.getLinkset();
 							echo("Selected linkset: " + linkset);
 							templateRequest();
 						} else {
 							Window.setStatus("Invalid Linkset");
 							Window.alert("Please select linkset!");
 						}
 
 					} else {
 						Window.setStatus("Error");
 						Window.alert("No interlinked ontologies in database!");
 					}
 				}
 			};
 			tabPanel.setStartBtnClickHandler(startBtnClickHandler);
 			tabPanel.enableStart();
 		}
 
 	}
 
 	/** Set ClickHandler for StartPanel buttons */
 	private void setStartPanelClickHandlers() {
 		// Start Button
 		ClickHandler startBtnClickHandler = new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				echo("Client: Start button clicked");
 				// Check if necessary input entered
 				if (!tabPanel.isEmpty()) {
 					if (user.getName().length() > 2) {
 						if (tabPanel.getLinkset() != null) {// LinksSpec
 							linkset = tabPanel.getLinkset();
 							echo("Selected linkset: " + linkset);
 							templateRequest();
 						} else {
 							Window.setStatus("Invalid Linkset");
 							Window.alert("Please select linkset!");
 						}
 					} else {
 						Window.setStatus("Invalid UserName");
 						Window.alert("UserName has to be at least 3 characters long!");
 					}
 				} else {
 					Window.setStatus("Error");
 					Window.alert("No interlinked ontologies in database!");
 				}
 			}
 		};
 		tabPanel.setStartBtnClickHandler(startBtnClickHandler);
 
 		// Login Button
 		ClickHandler loginBtnClickHandler = new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				if (tabPanel.loginSelected()) {
 					login();
 					tabPanel.getLoginButton().setEnabled(false);
 				}
 			}
 		};
 		tabPanel.setLoginBtnClickHandler(loginBtnClickHandler);
 
 		// Logout Button
 		ClickHandler logoutBtnClickHandler = new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				if (!user.getName().isEmpty()) {
 					logout();
 				}
 			}
 		};
 		tabPanel.setLogoutBtnClickHandler(logoutBtnClickHandler);
 
 		// Add Links Button
 		ClickHandler addLinksBtnClickHandler = new ClickHandler() {
 			public void onClick(ClickEvent event) {
 
 				glass.hide();
 				menu.hide();
 			}
 		};
 		tabPanel.setAddLinksBtnClickHandler(addLinksBtnClickHandler);
 
 		// Process Task Button
 		ClickHandler processTaskBtnClickHandler = new ClickHandler() {
 			public void onClick(ClickEvent event) {
 
 			}
 		};
 		tabPanel.setProcessTaskBtnClickHandler(processTaskBtnClickHandler);
 
 	}
 
 	/**
 	 * Check whether user is logged in, to disableLogin().
 	 * 
 	 * @return isLoggedin?
 	 */
 	private void autoLogin() {
 		echo("##Client: Autologin");
 		Storage store = Storage.getLocalStorageIfSupported();
 		if (store.getLength() == 0) {
 			echo("User is not logged in!");
 			return;
 		} else {
 			// Search for token
 			echo("Search for accessToken in html5 local storage..");
 			String key;
 
 			// Facebook
 			echo("Facebook token");
 			for (int i = 0; i < store.getLength(); i++) {
 				echo(i + ".Sotrage : " + store.key(i));
 				if (store.key(i).startsWith(LoginConstants.FACEBOOK_CLIENT_ID)) {
 					key = store.key(i);
 					echo("Found token!: " + key);
 					accessToken = store.getItem(key).substring(0,
 							store.getItem(key).indexOf("---"));
 					echo("Token: " + accessToken);
 					fbRequestIdentityOnly();
 					echo("User is already logged in!");
 					return;
 				}
 			}
 
 			// Google
 			echo("Google token");
 			for (int i = 0; i < store.getLength(); i++) {
 				echo(i + ".Sotrage : " + store.key(i));
 				if (store.key(i).startsWith(LoginConstants.GOOGLE_CLIENT_ID)) {
 					key = store.key(i);
 					echo("Found token!: " + key);
 					accessToken = store.getItem(key).substring(0,
 							store.getItem(key).indexOf("---"));
 					echo("Token: " + accessToken);
 					googleRequestIdentityJsonp(false);
 					echo("User is already logged in!");
 					return;
 				}
 			}
 		}
 
 	}
 
 	private void getToken() {
 		final AuthRequest reqFb = new AuthRequest(
 				LoginConstants.FACEBOOK_AUTH_URL,
 				LoginConstants.FACEBOOK_CLIENT_ID).withScopes(
 				LoginConstants.FACEBOOK_FRIENDLIST_SCOPE)
 		// Facebook expects a comma-delimited list of scopes
 				.withScopeDelimiter(",");
 		AUTH.login(reqFb, new Callback<String, Throwable>() {
 			public void onSuccess(String token) {
 				// Access token
 				Window.alert("Got an OAuth token:\n" + token + "\n"
 						+ "Token expires in " + AUTH.expiresIn(reqFb) + " ms\n");
 				accessToken = token;
 
 			}
 
 			public void onFailure(Throwable reason) {
 				// TODO Auto-generated method stub
 				Window.alert("Get Token fail: " + reason.getMessage());
 			}
 		});
 
 	}
 
 	// private void logout(){
 	// echo("Logout:");
 	// String redirect ="http://127.0.0.1:8888/Application/oauthWindow.html";
 	// String tok =
 	// "AAAD7c9Bf7j0BACZAQAAhZCEEZB5ZBSn0R7x6AWV6QXPa9kjM9cUFZB0JFyo2MA7MpTWriRo74DJslcfXHUKISopExmIwA1xoCMc6OHCtlO2nZByO947ktN";
 	// // String url =
 	// "https://graph.facebook.com/me/friends?access_token="+accessToken;
 	// // String
 	// url="https://www.facebook.com/logout.php?next="+redirect+"&access_token="+accessToken;
 	// // String url =
 	// "http://www.hpgloe.com/json/getrec/?lat=37.234&lon=-122.234";
 	// String url
 	// ="http://www.kongregate.com/api/authenticate.json?user_id=765&game_auth_token=AuthToken&api_key=MyApiKey";
 	//
 	//
 	// try{
 	// JsonpRequestBuilder builder = new JsonpRequestBuilder();
 	// builder.setCallbackParam("json");
 	// builder.requestObject(url, new AsyncCallback<JavaScriptObject>(){
 	//
 	// public void onSuccess(JavaScriptObject friends) {
 	// Window.alert("Logout successful");
 	// echo("storage length: "+Storage.getLocalStorageIfSupported().getLength());
 	// }
 	//
 	// public void onFailure(Throwable e) {
 	// displayError("ERROR: Couldn't retrieve JSON -> "+e.getMessage());
 	// }});
 	// }catch (Exception e){
 	// displayError("ERROR: Couldn't retrieve JSON!");
 	// }
 	//
 	// echo("Logout Done!");
 	// }
 
 	private void logout() {
 		echo("Logout");
 		// Clear tokens
 		Auth.get().clearAllTokens();
 		// Clear login info
 		String loggedOutUser = this.user.getName();
 		this.user = new User();
 		this.accessToken = null;
 		tabPanel.enableLogin();
 		echo("'" + loggedOutUser + "' was logged out!");
 		Window.alert("'" + loggedOutUser + "' was logged out!");
 		echo("Logout Done");
 	}
 
 	private void login() {
 
 		echo("Login:");
 		// AuthRequest
 		String selectedLogin = tabPanel.getSelectedLogin();
 		if (selectedLogin.equals(tabPanel.FACEBOOK_LOGIN)) {
 			echo(tabPanel.FACEBOOK_LOGIN);
 			final AuthRequest reqFb = new AuthRequest(
 					LoginConstants.FACEBOOK_AUTH_URL,
 					LoginConstants.FACEBOOK_CLIENT_ID).withScopes(
 					LoginConstants.FACEBOOK_FRIENDLIST_SCOPE)
 			// Facebook expects a comma-delimited list of scopes
 					.withScopeDelimiter(",");
 			AUTH.login(reqFb, new Callback<String, Throwable>() {
 				public void onSuccess(String token) {
 					// Access token
 					// Window.alert("Got an OAuth token:\n" + token + "\n" +
 					// "Token expires in " + AUTH.expiresIn(reqFb) + " ms\n");
 					accessToken = token;
 					// fbRequest
 					fbRequestJsonp();
 
 				}
 
 				public void onFailure(Throwable caught) {
 					Window.alert("Error:\n" + caught.getMessage());
 					// Set Login Button back to enabled, so user can try again
 					tabPanel.getLoginButton().setEnabled(true);
 				}
 			});
 		} else if (selectedLogin.equals(tabPanel.GOOGLE_LOGIN)) {
 			echo(tabPanel.GOOGLE_LOGIN);
 			final AuthRequest reqGoogle = new AuthRequest(
 					LoginConstants.GOOGLE_AUTH_URL,
 					LoginConstants.GOOGLE_CLIENT_ID)
 					.withScopes(LoginConstants.PROFILE_SCOPE);
 			AUTH.login(reqGoogle, new Callback<String, Throwable>() {
 				public void onSuccess(String token) {
 					// Access token
 					// Window.alert("Got an OAuth token:\n" + token + "\n"+
 					// "Token expires in " + AUTH.expiresIn(reqGoogle) +
 					// " ms\n");
 					accessToken = token;
 					// googleRequest
 					googleRequestJsonp();
 				}
 
 				public void onFailure(Throwable caught) {
 					Window.alert("Error:\n" + caught.getMessage());
 				}
 			});
 
 		} else if (selectedLogin.equals(tabPanel.USERNAME_LOGIN)) {
 			echo(tabPanel.USERNAME_LOGIN);
 			usernameLogin();
 
 		}
 
 	}
 
 	private void usernameLogin() {
 		user.setId(Message.USERNAME_LOGIN);
 		user.setName(tabPanel.getEnteredName());
 		Window.alert("Login successful! :)");
 		disableLogin();
 		// if (tabPanel.getEnteredName().length()>2) {
 		// user.setId(Message.USERNAME_LOGIN);
 		// user.setName(tabPanel.getEnteredName());
 		// disableLogin();
 		// }
 		// else {
 		// Window.setStatus("Invalid UserName");
 		// Window.alert("UserName has to be at least 3 characters long!");
 		// }
 	}
 
 	/**
 	 * Init popup for displaying highscore
 	 */
 	private void initHighscore() {
 
 		/*
 		 * final PopupPanel glass = new PopupPanel();
 		 * glass.setStyleName("rx-glass");
 		 * DOM.setStyleAttribute(glass.getElement(), "width", "100%");
 		 * DOM.setStyleAttribute(glass.getElement(), "height", "100%");
 		 * 
 		 * DOM.setStyleAttribute(glass.getElement(), "backgroundColor", "#000");
 		 * DOM.setStyleAttribute(glass.getElement(), "opacity", "0.70");
 		 */
 
 		highscorePanel = new HighscorePanel(startOfGame);
 
 		if (startOfGame == false) {
 
 			// Create the popup dialog box
 			popup = new PopupPanel(false);
 			// popup.setTitle("Game End");
 			popup.setAnimationEnabled(true);
 			popup.setStyleName("highscorepanel");
 			// DOM.setStyleAttribute(popup.getElement(), "z-index", "10");
 			// DOM.setStyleAttribute(popup.getElement(), "width", "100%");
 			// DOM.setStyleAttribute(popup.getElement(), "height", "100%");
 
 			// Add a handler to close the popup
 			ClickHandler closeHandler = new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					echo("Highscore close!");
 					popup.hide();
 					// msgButton.setEnabled(true);
 					// msgButton.setFocus(true);
 					game.worldLoaded = true;
 					verifyButton.setEnabled(true);
 					newLevel();
 				}
 			};
 			// Add a handler to close the popup and send players highscore to
 			// server
 			ClickHandler submitHandler = new ClickHandler() {
 				public void onClick(ClickEvent event) {
 					echo("Highscore submit!");
 					// String score = Integer.toString(game.world.getScore());
 					// String player = nameInput.getText();
 					sendScoreToServer();
 					popup.hide();
 					// msgButton.setEnabled(true);
 					// msgButton.setFocus(true);
 					game.worldLoaded = true;
 					verifyButton.setEnabled(true);
 					newLevel();
 				}
 			};
 
 			echo("Add Highscore Handler");
 			highscorePanel.setSubmitBtnClickHandler(submitHandler);
 			highscorePanel.setCloseBtnClickHandler(closeHandler);
 
 			popup.setWidget(highscorePanel);
 		}
 
 	}
 
 	/** Is InputListening from GameComponent blocked? */
 	public boolean isInputDisabled() {
 		return this.disableInput;
 	}
 
 	/** Is InputListening from GameComponent blocked? */
 	public boolean isStartOfLevel() {
 		return this.startOfLevel;
 	}
 
 	public void setStartOfLevel(boolean b) {
 		this.startOfLevel = b;
 	}
 
 	/**
 	 * Show highscore popup
 	 * 
 	 * @param stmt
 	 */
 	public void showHighscore(ArrayList<Score> stmt) {
 		echo("##Client: show highscore##");
 		highscorePanel.reset();
 		// highscorePanel.setStartOfGame(startOfGame);
 
 		// Update highscoreList
 		highscorePanel.generateHighscoreList(stmt);
 
 		if (startOfGame == false) {
 
 			this.verifyButton.setEnabled(false);
 			// Update score
 			String score = Integer.toString(this.game.world.getInfoText()
 					.getScore());
 			highscorePanel.setScore(score);
 
 			// highscorePanel.setStartOfLevel(false);
 			popup.center();
 			popup.show();
 		}
 		// else {
 		// highscorePanel.setStartOfLevel(true);
 		// }
 		echo("##Client: show highscore END##");
 	}
 
 	/**
 	 * Add money to players budget. Update money from GameWorld and InfoText.
 	 * 
 	 * @param money
 	 */
 	public void addMoney(int money) {
 		game.world.addMoney(money);
 		game.world.getInfoText().updateMoney(game.world.getMoney());
 	}
 
 	/**
 	 * Process bonus. Check bonus and print bonus related message to text field.
 	 * Add verification to VerificationStatistics
 	 * 
 	 * @param bonus
 	 *            money to add
 	 */
 	public void processEval(double eval, double difficulty) {
 		echo("Process Eval = " + eval + ", Diff = " + difficulty);
 
 		int bonus = 0;
 		
 		String msg = null;
 		if (notSure) {// -1
 			bonus = GameConstants.BONUS_UNSURE;
 			setNotSure(GameConstants.BONUS_UNSURE);
 		} else if (eval == GameConstants.EVAL_NEGATIVE) {// sure negative
 			bonus = GameConstants.BONUS_NEGATIVE;
 			setPenalty();
 		} else if (eval == GameConstants.EVAL_POSITIVE) {// sure positive
 			bonus = GameConstants.BONUS_POSITIVE;
 			setAgreement(GameConstants.BONUS_POSITIVE);
 		} else if (eval == GameConstants.EVAL_FIRST) {// sure positive
 			bonus = GameConstants.BONUS_FIRST;
 			setFirstVerification(GameConstants.BONUS_FIRST);
 		} else if (eval > GameConstants.EVAL_THRESHOLD) {// agree
 			bonus = GameConstants.BONUS_AGREE;
 			setAgreement(GameConstants.BONUS_AGREE);
 		} else if (eval <= GameConstants.EVAL_THRESHOLD) {
 			bonus = GameConstants.BONUS_DISAGREE;
 			setDisagreement();
 		}
 		echo("bonus: "+bonus);
 		// documentation of verification
 		verificationStats.addEvaluation(bonus, notSure);
 		
 		int finalBonus = bonus;
 		// here
 //		int finalBonus = Balancing.getBonus(bonus, difficulty);
 		echo("finalBonus: "+finalBonus); 
 		addMoney(finalBonus);
 		notSure = false;
 		echo("Process bonus done!");
 	}
 
 	//
 	//
 	// public void processLinkset(ArrayList<String> list){
 	//
 	// ontologyList = list;
 	// }
 	//
 	/**
 	 * Asynchronous call of server's userVerification method. Send verification
 	 * and linkset information to the server and wait for a response.
 	 */
 	private void sendVerificationToServer(int selection) {
 		echo("\n### Client: Send verification to server ###");
 
 		// disable verify buttons
 		verifyComponent.disableButtons();
 		this.verifyLock = true;
 
 		// Check if users credibility should be checked
 		checkUserCredibility();
 
 		// Sound
 		// game.getSound().playSend();
 
 		// Add temp. money for verification
 		int money = MONEY_FOR_VERIFICATION; // VALID, NOT VALID
 		if (selection == GameConstants.UNSURE) { // NOT SURE
 			money = MONEY_FOR_NOT_SURE;
 			notSure = true;
 		}
 		addMoney(money);
 
 		// Change verifyButton
 		verifyButton.setEnabled(false);
 		verifyButton.setText("Verification send to server!");
 
 		// Get Verification
 		verification = new Verification(link.getId(), selection);
 		verificationStats.addVerification(verification);
 
 		// send veri
 		getLink(selection);
 		echo("CLIENT: thisLink = " + thisLink);
 		echo("CLIENT: nextLink = " + nextLink);
 
 		timeOut();
 		echo("### Client: Send verification to server done. ###");
 	}
 
 	private void checkUserCredibility() {
 		int min = 4;
 		int max = 5;
 		int rnd = min + (int) (Math.random() * ((max - min) + 1));
 		echo("rnd: " + rnd);
 		// int rnd = 7;
 		if (this.verificationStats.getList().size() % rnd == rnd - 1) {
 			this.checkUserCredibility = true;
 			nextLink = Message.EVAL_LINK;
 			echo("debug: TRUE . size: "
 					+ this.verificationStats.getList().size());
 		} else {
 			nextLink = Message.NORMAL_LINK;
 			this.checkUserCredibility = false;
 			echo("debug: FALSE . size: "
 					+ this.verificationStats.getList().size());
 		}
 	}
 
 	private void commitVerifications() {
 		echo("Commit Verification?");
 		// Set level duration
 		user.calcCurrentLevelTime(System.currentTimeMillis());
 		echo("#penalties: " + verificationStats.getCountPenalty());
 		echo("#size: " + verificationStats.getList().size());
 		int agree = verificationStats.getCountAgreed();
 		int disagree = verificationStats.getCountDisagreed();
 		int penalty = verificationStats.getCountPenalty();
 		int unsure = verificationStats.getCountUnsure();
 		echo("agree: " + agree + " disagree: " + disagree + "penalty: "
 				+ penalty + " unsure:" + unsure);
 		echo("Error limit: " + ERROR_LIMIT);
 		echo("CalcErrorRate: " + calcErrorRate());
 		if (calcErrorRate() <= ERROR_LIMIT) {
 			this.user.setCredible(true);
 			echo("Commit Link Verifications!");
 		} else {
 			this.user.setCredible(false);
 			echo("Too many false verifications! Link Verifications won't be commited!");
 		}
 
 		String data = this.verificationStats.getJson();
 		echo("commit: " + data);
 
 		String url = SERVER_URL+"server?service=commitVerifications";
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,
 				URL.encode(url));
 		// StringBuffer postData=new StringBuffer();
 		// postData.append(URL.encode("string")).append("=").append(URL.encode(data));
 		builder.setHeader("Content-type", "application/x-www-form-urlencoded");
 		try {
 			Request request = builder.sendRequest(data, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					echo("ERROR: Commit Verifications! "
 							+ exception.getMessage());
 				}
 
 				public void onResponseReceived(Request request,
 						Response response) {
 					echo(" Commit Verifications success! userStrength: "
 							+ response.toString());
 					JSONValue jsonValue = JSONParser.parseStrict(response
 							.getText());
 					System.out.println("jsonValue: " + jsonValue.toString());
 					JSONObject jsonObject = jsonValue.isObject();
 					JsoUserstrength str = jsonObject.getJavaScriptObject()
 							.cast();
 					user.setStrength(str.getUserstrength());
 				}
 			});
 		} catch (RequestException e) {
 			// Couldn't connect to server
 			echo("ERROR Highscore!");
 		}
 
 		// Submit verificaqtions stats to kongregate
 		if (config.isKongregate() == true)
 			submitVerifyStatsToKongregate(verificationStats.getList().size()
 					+ "", verificationStats.getCountAgreed() + "",
 					verificationStats.getCountPenalty() + "");
 		// Window.alert("java Submit verify stats done!");
 		// Reset List for this level
 		this.verificationStats.reset();
 		echo("Reset verificationStats List for this level");
 
 	}
 
 	private double calcErrorRate() {
 		double errorRate = ((double) verificationStats.getCountPenalty())
 				/ ((double) verificationStats.getList().size());
 		echo("pen: " + verificationStats.getCountPenalty() + ", verified: "
 				+ verificationStats.getList().size() + " , error: " + errorRate);
 
 		return errorRate;
 	}
 
 	public native void submitVerifyStatsToKongregate(String countVerify,
 			String countAgree, String countPenalty) /*-{
 		// Call instance method instanceFoo() on this
 		//var name = $wnd.kongregate.kongregate.services.getUserId();
 
 		$wnd.submitVerifyStats(countVerify, countAgree, countPenalty);
 
 		var s = $wnd.userName;
 		this.@org.aksw.verilinks.games.peaInvasion.client.PeaInvasion::setUserNameKongregate(Ljava/lang/String;)(s);
 	}-*/;
 
 	/**
 	 * Asynchronous call of server's firsRequest method. Send request with
 	 * linkset and wait for responds of new semantic web link to verify and
 	 * bonus.
 	 */
 	public void firstQueryRequest() {
 		echo("Client: First Query Request");
 		getLink(FIRST_QUERY_REQUEST);
 		this.disableInput = false;
 
 	}
 
 	public void linksetRequest() {
 		echo("Linkset Request");
 		initHighscore();
 		if (startOfGame == false)
 			game.worldLoaded = false;
 
 		String url = SERVER_URL+"server?service=getLinksets&game=peaInvasion";
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
 				URL.encode(url));
 		try {
 			Request request = builder.sendRequest(null, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					// Couldn't connect to server (could be timeout, SOP
 					// violation, etc.)
 					echo("ERROR rest");
 				}
 
 				public void onResponseReceived(Request request,
 						Response response) {
 					// TODO Auto-generated method stub
 					echo("Linkset list received");
 					JSONValue jsonValue = JSONParser.parseStrict(response
 							.getText());
 					System.out.println("jsonValue: " + jsonValue.toString());
 					JSONObject jsonObject = jsonValue.isObject(); // assert that
 																	// // an
 																	// object
 					if (jsonObject == null) {
 						System.out
 								.println("JSON payload did not describe an object");
 						throw new RuntimeException(
 								"JSON payload did not describe an object");
 					} else
 						System.out.println("is object");
 					JsoLinksetArray lSet = jsonObject.getJavaScriptObject()
 							.cast();
 					System.out.println("casted");
 					linksetList = parseLinkset(lSet.getLinkset());
 					initStartPanel();
 				}
 			});
 		} catch (RequestException e) {
 			// Couldn't connect to server
 			echo("ERROR Highscore!");
 		}
 		echo("Linkset Request Done");
 	}
 
 	private ArrayList<Linkset> parseLinkset(JsArray<JsoLinkset> lSetArray) {
 		echo("Parse Linkset. Size = " + lSetArray.length());
 		ArrayList<Linkset> linksetList = new ArrayList<Linkset>();
 
 		JsoLinkset jSet = null;
 		Linkset lSet = null;
 		for (int i = 0; i < lSetArray.length(); i++) {
 			jSet = lSetArray.get(i);
 			lSet = new Linkset();
 			lSet.setId(jSet.getId());
 			lSet.setSubject(jSet.getSubject());
 			lSet.setObject(jSet.getObject());
 			lSet.setDescription(jSet.getDescription());
 			lSet.setDifficulty(jSet.getDifficulty());
 
 			linksetList.add(lSet);
 		}
 		return linksetList;
 
 	}
 
 	/**
 	 * Asynchronous call of highscoreRequest method. Requests the highscores
 	 * from the server and wait for responds.
 	 */
 	public void highscoreRequest() {
 		echo("##Client: Highscore Request##");
 		initHighscore();
 		if (startOfGame == false)
 			game.worldLoaded = false;
 
 		String url = SERVER_URL+"server?service=getHighscore&game=peaInvasion";
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
 				URL.encode(url));
 		try {
 			Request request = builder.sendRequest(null, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					// Couldn't connect to server (could be timeout, SOP
 					// violation, etc.)
 					echo("ERROR rest");
 				}
 
 				public void onResponseReceived(Request request,
 						Response response) {
 					// TODO Auto-generated method stub
 					echo("Highscore list received");
 					JSONValue jsonValue = JSONParser.parseStrict(response
 							.getText());
 					System.out.println("jsonValue: " + jsonValue.toString());
 					JSONObject jsonObject = jsonValue.isObject(); // assert that
 																	// // an
 																	// object
 					if (jsonObject == null) {
 						System.out
 								.println("JSON payload did not describe an object");
 						throw new RuntimeException(
 								"JSON payload did not describe an object");
 					} else
 						System.out.println("is object");
 					JsoHighscoreArray hS = jsonObject.getJavaScriptObject()
 							.cast();
 					showHighscore(parseHighscore(hS.getHighscore()));
 				}
 			});
 		} catch (RequestException e) {
 			// Couldn't connect to server
 			echo("ERROR Highscore!");
 		}
 		echo("##Client: Highscore Request Done##");
 	}
 
 	public void sendScoreToServer() {
 		String score = Integer.toString(this.game.world.getInfoText()
 				.getScore());
 		echo("sendScoreToServer: " + score);
 		// TODO wokring??
 		// Send to Kongregate server
 		if (config.isKongregate() == true)
 			sendScoreToKongregate(score);
 
 		String url = SERVER_URL+"server?service=postScore&userId="
 				+ user.getId()
 				+ "&userName="
 				+ user.getName()
 				+ "&game=peaInvasion" + "&score=" + score;
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST,
 				URL.encode(url));
 		try {
 			Request request = builder.sendRequest(null, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					echo("ERROR rest");
 				}
 
 				public void onResponseReceived(Request request,
 						Response response) {
 					echo("Score send");
 					msgButton.setText("Game Message");
 					msgButton.setStyleName("gameMessage");
 				}
 			});
 		} catch (RequestException e) {
 			// Couldn't connect to server
 			echo("ERROR Send score!");
 		}
 	}
 
 	public native void sendScoreToKongregate(String score) /*-{
 		//  	$wnd.alert("java Submit score: "+score);
 		$wnd.submitScore(score);
 	}-*/;
 
 	/**
 	 * Request template from server.
 	 */
 	public void templateRequest() {
 		echo("####Template Request###");
 		String subjectName = linkset.getSubject();
 		final String objectName = linkset.getObject();
 		System.out.println("Names: " + subjectName + "-" + objectName);
 
 		String url = SERVER_URL+"server?service=getTemplate";
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
 				URL.encode(url));
 		try {
 			Request request = builder.sendRequest(null, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					echo("ERROR Tempalte Request");
 				}
 
 				public void onResponseReceived(Request request,
 						Response response) {
 					echo("Template received");
 					JSONValue jsonValue = JSONParser.parseStrict(response
 							.getText());
 					System.out.println("jsonValue: " + jsonValue.toString());
 					JSONObject jsonObject = jsonValue.isObject(); // assert that
 																	// // an
 																	// object
 					if (jsonObject == null) {
 						System.out
 								.println("JSON payload did not describe an object");
 						throw new RuntimeException(
 								"JSON payload did not describe an object");
 					} else
 						System.out.println("is object");
 					JsoTemplateArray hS = jsonObject.getJavaScriptObject()
 							.cast();
 					System.out.println("casted");
 					parseTemplate(hS);
 
 				}
 			});
 		} catch (RequestException e) {
 			// Couldn't connect to server
 			echo("ERROR Temaplte!");
 		}
 
 	}
 
 	// /**
 	// * Request for list of interlinked ontologies.
 	// */
 	// protected void linksetRequest() {
 	// // Receive highscore from server
 	// callbackGetLinkset = new AsyncCallback<ArrayList<Linkset>>() {
 	// public void onFailure(Throwable caught) {
 	// // TODO: Do something with errors.
 	// //verifyButton.setText(caught.getMessage());
 	// }
 	// public void onSuccess(ArrayList<Linkset> stmt) {
 	// //deflatten
 	// System.out.println("Client: Linkset count: "+stmt.size()+"\n");
 	// //verifyButton.setText("LO Success"+stmt.size()+","+stmt.get(0));
 	// //showHighscore(stmt);
 	// linksetList=stmt;
 	// // processLinkset(stmt);
 	// initStartPanel();
 	// }
 	// };
 	// service.linksetRequest(callbackGetLinkset);
 	//
 	// }
 
 	/**
 	 * Update VerifyComponent's tables
 	 * 
 	 * @param stmt
 	 *            new rdfStatement to verify
 	 */
 	private void updateTable(Link stmt, TemplateLinkset template) {
 		verifyComponent.updateStatement(stmt, template);
 	}
 
 	public void key1Pressed() {
 		if (!verifyLock()) {
 			DOM.setStyleAttribute(this.verifyComponent.getTrueButton()
 					.getElement(), "border", "2px solid black");
 			this.sendVerificationToServer(GameConstants.TRUE);
 		}
 	}
 
 	public void key2Pressed() {
 		if (!verifyLock()) {
 			DOM.setStyleAttribute(this.verifyComponent.getFalseButton()
 					.getElement(), "border", "2px solid black");
 			this.sendVerificationToServer(GameConstants.FALSE);
 		}
 	}
 
 	public void key3Pressed() {
 		// if(numKeyCache==3 && !verifyLock())
 		// this.sendVerificationToServer();
 		// else {
 		// this.verifyComponent.setNotSureRadioButton(true);
 		// setNumKeyCache(3);
 		// }
 		if (!verifyLock()) {
 			DOM.setStyleAttribute(this.verifyComponent.getUnsureButton()
 					.getElement(), "border", "2px solid black");
 
 			// this.verifyComponent.setNotSureRadioButton(true);
 			this.sendVerificationToServer(GameConstants.UNSURE);
 		}
 	}
 
 	/**
 	 * Set key cache
 	 * 
 	 * @param key
 	 */
 	public void setNumKeyCache(int key) {
 		game.getSound().playClick();
 		this.numKeyCache = key;
 	}
 
 	private void setFirstVerification(int bonus) {
 		echo("Process Link-Evaluation: First Verification");
 		String msg = "You are the first to verify this statement! " + "<< "
 				+ bonus + " Coins >> Bonus!";
 		game.firstVerification();
 		msgButton.setText(msg);
 		msgButton.setStyleName("gameMessage");
 	}
 
 	private void setAgreement(int bonus) {
 		echo("Process Link-Evaluation: Agreement");
 		String msg = "Agreement! << " + bonus + " Coins >> Bonus!";
 		game.agreement();
 		if (game.isSpecialEvent()) {
 			bonus = 2 * bonus;
 			msg = "Agreement! << " + bonus + " Coins >> Bonus! "
 					+ game.SPECIAL_REACHED
 					+ " agreements in a row! Big Daddy is on his way!";
 
 		}
 		msgButton.setText(msg);
 		msgButton.setStyleName("gameMessage");
 	}
 
 	private void setDisagreement() {
 		echo("Process Link-Evaluation: Disagreement");
 		String msg = "Disagreement! No Bonus!";
 		game.disagreement();
 		msgButton.setText(msg);
 		msgButton.setStyleName("gameMessageDisagreement");
 	}
 
 	private void setPenalty() {
 		echo("Process Link-Evaluation: Penalty");
 		String msg = "False Verification! << " + GameConstants.BONUS_NEGATIVE
 				+ " Coins >> Penalty!";
 		game.penalty();
 		msgButton.setText(msg);
 		msgButton.setStyleName("gameMessagePenalty");
 	}
 
 	private void setNotSure(int bonus) {
 		echo("Process Link-Evaluation: Not sure");
 		String msg = "You weren't sure.";
 		msgButton.setText(msg);
 		msgButton.setStyleName("gameMessageNotSure");
 	}
 
 	/** Connect to server, to get linkset. Check if server is running. */
 	private void connect() {
 		String url = SERVER_URL+"server?service=checkStatus";
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
 				URL.encode(url));
 		try {
 			Request request = builder.sendRequest(null, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					echo("ERROR rest");
 					Window.alert("Server not running!");
 					serverRunning = false;
 				}
 
 				public void onResponseReceived(Request request,
 						Response response) {
 					serverRunning = true;
 					System.out.println(response.getText());
 					linksetRequest();
 				}
 			});
 		} catch (RequestException e) {
 			// Couldn't connect to server
 			echo("ERROR Send score!");
 		}
 	}
 
 	/** Timer for sending verification */
 	private void timeOut() {
 		// Set up Timer
 		Timer t = new Timer() {
 			@Override
 			public void run() {
 				verifyButton.setEnabled(true);
 				verifyButton.setText("VERIFY");
 				verifyLock = false;
 				// disable verify buttons
 				verifyComponent.enableButtons();
 			}
 		};
 		t.schedule(1800);
 	}
 
 	/** Is verification mechanism locked? */
 	private boolean verifyLock() {
 		return verifyLock;
 	}
 
 	private void echo(String msg) {
 		System.out.println("[Client]: " + msg);
 	}
 
 	public void sendUser() {
 		echo("Client: Send user to server!");
 		this.verificationStats.setUser(this.user);
 
 		String url = SERVER_URL+"server?service=getUserdata&userId="
 				+ user.getId() + "&userName=" + user.getName();
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
 				URL.encode(url));
 		try {
 			Request request = builder.sendRequest(null, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					// Couldn't connect to server (could be timeout, SOP
 					// violation, etc.)
 					echo("ERROR userdata");
 				}
 
 				public void onResponseReceived(Request request,
 						Response response) {
 					// TODO Auto-generated method stub
 					echo("Highscore list received");
 					JSONValue jsonValue = JSONParser.parseStrict(response
 							.getText());
 					System.out.println("jsonValue: " + jsonValue.toString());
 					JSONObject jsonObject = jsonValue.isObject(); // assert that
 																	// // an
 																	// object
 					if (jsonObject == null) {
 						System.out
 								.println("JSON payload did not describe an object");
 						throw new RuntimeException(
 								"JSON payload did not describe an object");
 					} else
 						System.out.println("is object");
 					// Cast
 					System.out.println("#######SUBJECT: ");
 					JsoUserdata u = jsonObject.getJavaScriptObject().cast();
 					System.out.println("casted");
 					Userdata userData = parseUserdata(u);
 					if (userData != null) {
 						echo("SendUser Success. Strength: "
 								+ userData.getStrength());
 						user.setStrength(userData.getStrength());
 					} else {
 						echo("new user");
 						user.setStrength("novice");
 					}
 					setDifficulty(linkset.getDifficulty());
 					startOfGame = false;
 					initGame();
 					initStartTime();
 					initLeavePage();
 					glass.hide();
 					menu.hide();
 					// Check if user tutorial should be skipped
 					if (!getSkipTutorial())
 						initTutorial();
 					else {
 						firstQueryRequest();
 					}
 				}
 			});
 		} catch (RequestException e) {
 			// Couldn't connect to server
 			echo("ERROR Highscore!");
 		}
 	}
 
 	/**
 	 * Disconnect user from server. Send session end for statistics.
 	 */
 	public void disconnectUser() {
 		echo("Client: Disconnect user from server!");
 
 		user.calcPlayTime(System.currentTimeMillis());
 
 		String url = SERVER_URL+"server?service=disconnect&userId="
 				+ user.getId()
 				+ "&userName="
 				+ user.getName()
 				+ "&time="
 				+ user.getPlayTime();
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
 				URL.encode(url));
 		try {
 			Request request = builder.sendRequest(null, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					echo("ERROR disconnect");
 				}
 
 				public void onResponseReceived(Request request,
 						Response response) {
 					echo("DisconnectUser Success: " + response.getText());
 				}
 			});
 		} catch (RequestException e) {
 			// Couldn't connect to server
 			echo("ERROR disconnect user");
 		}
 	}
 
 	public void setWin(final boolean end) {
 		this.msgButton.setText("Hell Yeah!");
 		this.msgButton.setStyleName("gameMessage");
 		this.disableInput = true;
 		// Glass panel behind popUp
 		glass.clear();
 		glass = new PopupPanel(false);
 		glass.setStyleName("rx-glass");
 		DOM.setStyleAttribute(glass.getElement(), "width", "100%");
 		DOM.setStyleAttribute(glass.getElement(), "height", "100%");
 		DOM.setStyleAttribute(glass.getElement(), "backgroundColor", "#000");
 		DOM.setStyleAttribute(glass.getElement(), "opacity", "0.30");
 
 		final PopupPanel pop = new PopupPanel(false);
 		pop.setStyleName("statisticsPanel");
 		// Button
 		Button next = new Button("Continue");
 		next.addStyleName("myButton");
 		VerticalPanel btnPanel = new VerticalPanel();
 		btnPanel.setSpacing(5);
 		btnPanel.add(next);
 		next.addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent arg0) {
 				if (end)
 					highscoreRequest();
 				else
 					newLevel();
 				glass.hide();
 				pop.hide();
 			}
 		});
 		// Head
 		HTML head = new HTML("You WIN!");
 		head.setStyleName("win_head");
 		// Image
 		StatisticsPanel stats = new StatisticsPanel(getStatistics());
 
 		// Text
 		HTML textHTML = new HTML(
 				"Wow! You've successfully cleared the level.<br/>"
 						+ "Let's take a look at your stats.");
 		textHTML.setStyleName("win_text");
 		VerticalPanel panel = new VerticalPanel();
 		panel.add(head);
 		panel.add(textHTML);
 		panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
 		panel.add(stats);
 		panel.add(btnPanel);
 
 		pop.add(panel);
 		glass.add(pop);
 
 		glass.show();
 		pop.center();
 
 		// next.setFocus(true);
 		// Send Verification
 		commitVerifications();
 	}
 
 	public void setLose() {
 		this.msgButton.setText("What the ..");
 		this.msgButton.setStyleName("gameMessageDisagreement");
 		this.disableInput = true;
 		// Glass panel behind popUp
 		glass = new PopupPanel(false);
 		glass.setStyleName("rx-glass");
 		DOM.setStyleAttribute(glass.getElement(), "width", "100%");
 		DOM.setStyleAttribute(glass.getElement(), "height", "100%");
 		DOM.setStyleAttribute(glass.getElement(), "backgroundColor", "#000");
 		DOM.setStyleAttribute(glass.getElement(), "opacity", "0.30");
 
 		final PopupPanel pop = new PopupPanel(false);
 		pop.setStyleName("statisticsPanel");
 		// Button
 		Button next = new Button("Continue");
 		next.addStyleName("myButton");
 		VerticalPanel btnPanel = new VerticalPanel();
 		btnPanel.setSpacing(5);
 		btnPanel.add(next);
 		next.addClickHandler(new ClickHandler() {
 
 			public void onClick(ClickEvent arg0) {
 				highscoreRequest();
 				glass.hide();
 				pop.hide();
 			}
 		});
 		// Head
 		HTML head = new HTML("You Lose!");
 		head.setStyleName("lose_head");
 		// Image
 		StatisticsPanel stats = new StatisticsPanel(getStatistics());
 
 		// Text
 		HTML textHTML = new HTML("Too bad. You've lost the game.<br/>"
 				+ "Let's take a look at your stats");
 		textHTML.setStyleName("lose_text");
 
 		VerticalPanel panel = new VerticalPanel();
 		panel.add(head);
 		panel.add(textHTML);
 		panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
 		panel.add(stats);
 		panel.add(btnPanel);
 
 		pop.add(panel);
 		glass.add(pop);
 
 		glass.show();
 		pop.center();
 		// next.setFocus(true);
 
 		// Commit Verification
 		commitVerifications();
 	}
 
 	private void newLevel() {
 		this.msgButton.setText("Let's go!");
 		this.msgButton.setStyleName("gameMessage");
 		game.resetSpecial();
 		game.world.newLevel();
 		this.disableInput = false;
 		this.startOfLevel = true;
 	}
 
 	private Statistics getStatistics() {
 		Statistics statistics = new Statistics();
 		statistics.setUser(user);
 		statistics.setScore(game.world.getScore());
 		statistics.setMoney(game.world.getMoney());
 		statistics.setLevel(game.world.getLevel());
 		statistics.setVerification(verificationStats);
 
 		return statistics;
 	}
 
 	//
 	// public void difficultyRequest(){
 	//
 	//
 	// service.difficultyRequest(linkset.getName(),callbackGetDifficulty);
 	// }
 	//
 
 	private void setDifficulty(String diff) {
 		this.difficulty = diff;
 
 	}
 
 	public String getDifficulty() {
 		// TODO Auto-generated method stub
 		return this.difficulty;
 	}
 
 	public void setUserCurrentLevel(int lvl) {
 		if (user != null)
 			user.setCurrentLevel(lvl);
 
 	}
 
 	public void setCurrentLevelStartTime() {
 		if (this.user != null)
 			this.user.setCurrentLevelStartTime(System.currentTimeMillis());
 
 	}
 
 	private double calcDifficulty(double difficulty) {
 		return Balancing.getLinkDifficulty(difficulty);
 	}
 
 	public User getUser() {
 		// TODO Auto-generated method stub
 		return this.user;
 	}
 
 	private void getLink(final int selection) {
 		// Receive new links, update Table
 		echo("Get Link Request");
 		String url = generateURL(selection);
 		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
 				URL.encode(url));
 		try {
 			Request request = builder.sendRequest(null, new RequestCallback() {
 				public void onError(Request request, Throwable exception) {
 					echo("ERROR disconnect");
 				}
 
 				public void onResponseReceived(Request request,
 						Response response) {
 					echo("GET Link Success: " + response.getText());
 					JSONValue jsonValue = JSONParser.parseStrict(response
 							.getText());
 					JSONObject jsonObject = jsonValue.isObject(); // assert that
 																	// // an
 																	// object
 					JsoLink l = jsonObject.getJavaScriptObject().cast();
 					parseLink(l,selection);
 				}
 			});
 		} catch (RequestException e) {
 			// Couldn't connect to server
 			echo("ERROR get LINK");
 		}
 
 	}
 
 	private void parseLink(JsoLink jLink, int selection) {
 		System.out.println("Parse Link");
 
 		if(selection != this.FIRST_QUERY_REQUEST)
 			processEval(jLink.getEval(),Double.parseDouble(jLink.getDifficulty()));
 
 		setDifficulty(linkset.getDifficulty());
 
 		JsoInstance jSub = jLink.getSubject();
 		JsoProperty jProp = null;
 		Property prop = null;
 		ArrayList<Property> propList = new ArrayList<Property>();
 		for (int i = 0; i < jSub.getProperties().length(); i++) {
 			jProp = jSub.getProperties().get(i);
 			prop = new Property(jProp.getProperty(), jProp.getValue());
 			propList.add(prop);
 		}
 		Instance sub = new Instance();
 		sub.setUri(jSub.getUri());
 		sub.setProperties(propList);
 
 		JsoInstance jOb = jLink.getObject();
 		jProp = null;
 		List<Property> propList2 = new ArrayList<Property>();
 		for (int j = 0; j < jOb.getProperties().length(); j++) {
 			jProp = jOb.getProperties().get(j);
 			prop = new Property(jProp.getProperty(), jProp.getValue());
 			propList2.add(prop);
 		}
 		Instance ob = new Instance();
 		ob.setUri(jOb.getUri());
 		ob.setProperties(propList2);
 
 		link = new Link(jLink.getId(), sub, ob, jLink.getPredicate(), 0, 0);
 		// updae linkMsg
 		linkMsg.setText("Link difficulty: "
 				+ Balancing.getStringLinkDifficulty(Double.parseDouble(jLink
 						.getDifficulty())));
 
 		// Update VerifyComponent
 		updateTable(link, template);
 		DeferredCommand.addCommand(new Command() {
 			public void execute() {
 				verifyButton.setFocus(false);
 			}
 		});
 		// Set thisLink
 		thisLink = nextLink;
 		echo("----------------------------------");
 		echo("Got Statement:");
 		echo("id: " + link.getId());
 		echo("subject uri: " + link.getSubject().getUri());
 
 		echo("object uri: " + link.getObject().getUri());
 
 		echo("predi: " + link.getPredicate());
 		echo("counter: " + link.getCounter());
 		// echo("bonus: " + link.getBonus());
 		echo("Client: This link is Eval_Link?: " + thisLink);
 		echo("----------------------------------");
 	}
 
 	private String generateURL(int selection) {
 		String url = SERVER_URL+"server?service=getLink";
 		url += "&userId=" + user.getId();
 		url += "&userName=" + user.getName();
 		url += "&linkset=" + linkset.getName();
 		url += "&nextLink=" + nextLink;
 		if (link != null)
 			url += "&curLink=" + link.getId();
 		if (this.verificationStats.getVerifiedLinks().length() > 0)
 			url += "&verifiedLinks="
 					+ this.verificationStats.getVerifiedLinks();
 		if (selection != FIRST_QUERY_REQUEST)
 			url += "&verification=" + selection;
 		echo("Generate URL: " + url);
 
 		return url;
 	}
 
 	public void setVerifyLock(boolean b){
 		this.verifyLock=b;
 	}
 }
