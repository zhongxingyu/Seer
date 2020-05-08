 package controllers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import logic.Crawler;
 import logic.Parser;
 import models.DistributionWish;
 import models.Item;
 import models.OpenOrder;
 
 import org.apache.commons.io.IOUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import play.Logger;
 import play.mvc.Controller;
 import play.templates.Template;
 import play.templates.TemplateLoader;
 import play.test.Fixtures;
 import flexjson.JSONDeserializer;
 import flexjson.JSONSerializer;
 
 public class Application extends Controller {
 
 	public static void index() {
 		render();
 	}
 
 	public static void doku() {
 		index();
 	}
 	
 	public static void test(){
		List<Item> items = Item.find("byItemId", "P1").fetch();
		renderJSON(items);
 	}
 	
 	public static void testLogin() {
 		setHeader();
 		renderJSON(true);
 	}
 	
 	public static void postDistributionWish() {
 		setHeader();
 		String body = getBodyAsString();
 		ArrayList<DistributionWish> wishs =  new JSONDeserializer<ArrayList<DistributionWish>>().use("values", DistributionWish.class).deserialize(body);
 		renderText(wishs);
 	}
 	
 	public static void getDistributenWishs() {
 		setHeader();
 		List<DistributionWish> wishs = DistributionWish.findAll();
 		if (wishs == null || wishs.size() == 0) {
 			error("Keine Distributionswünsche vorhanden");
 		}
 		renderJSON(new JSONSerializer().include("item.itemId", "period", "period1", "period2", "period3").exclude("*").serialize(wishs));
 	}
 
 	/**
 	 * Testmethod to develop xml parser with local xml file doesnt load
 	 * everytime the xml from the scsim website
 	 */
 	public static void parseXML() {
 		OpenOrder.deleteAll();
 		Template template = TemplateLoader.load("229_9_7result.xml");
 		String xmlFile = template.render();
 
 		InputStream in = IOUtils.toInputStream(xmlFile);
 		Parser p = new Parser(in);
 		p.parseDoc();
 		List<Item> items = Item.findAll();
 		renderText(items.size() + " : " + items);
 	}
 
 	/**
 	 * upload latest result xml file manualy
 	 * 
 	 * @param file
 	 */
 	public static void uploadXML(File file) {
 		InputStream in = null;
 		try {
 			in = new FileInputStream(file);
 			Parser p = new Parser(in);
 			p.parseDoc();
 		} catch (FileNotFoundException | NullPointerException e) {
 			e.printStackTrace();
 			error("No file received.");
 		}
 		ok();
 	}
 
 	/**
 	 * check if login data is correct
 	 */
 	public static void login() {
 		setHeader();
 		JSONObject json = getBodyAsJSON();
 		String username = null, password = null;
 		try {
 			username = json.getString("username");
 			password = json.getString("password");
 		} catch (JSONException e) {
 			error("Error on login check");
 		}
 		Crawler cr = new Crawler(username, password);
 		renderJSON(cr.checkLogin());
 	}
 
 	/**
 	 * crawling scsim.de to download the latest result xml file
 	 * 
 	 * @param username
 	 * @param password
 	 */
 	public static void loadXmlFromSite(String username, String password) {
 		Crawler cr = new Crawler(username, password);
 		String file = cr.importFileFromWeb();
 		Logger.info("file:\n%s", file);
 		renderText(file);
 	}
 
 	/**
 	 * set response header for CORS Ajax requests
 	 */
 	private static void setHeader() {
 		response.setHeader("Access-Control-Allow-Origin", "*");
 	}
 
 	/**
 	 * get request body as JSONObject
 	 * @return
 	 */
 	private static JSONObject getBodyAsJSON() {
 		String body = params.get("body");
 		JSONObject json = null;
 		try {
 			json = new JSONObject(body);
 		} catch (JSONException e) {
 			Logger.error("couldnt parse body to JSONObject: %s", body);
 		}
 		return json;
 	}
 	
 	private static String getBodyAsString() {
 		return params.get("body");
 	}
 
 }
