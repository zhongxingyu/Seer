 package controllers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import logic.ApplicationLogic;
 import logic.Crawler;
 import logic.Parser;
 import models.Capacity;
 import models.DispositionManufacture;
 import models.DispositionOrder;
 import models.DistributionWish;
 import models.Item;
 import models.ItemTime;
 import models.ProductionOrder;
 import models.User;
 import models.Workplace;
 
 import org.apache.commons.io.IOUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 
 import play.Logger;
 import play.mvc.Controller;
 import play.templates.Template;
 import play.templates.TemplateLoader;
 import flexjson.JSONDeserializer;
 import flexjson.JSONSerializer;
 
 public class Application extends Controller {
 
 	public static void index() {
 		render();
 	}
 
 	public static void test() {
 		List<ItemTime> places = ItemTime.findAll();
 		renderText(places);
 	}
 
 	public static void test2() {
 		List<DispositionManufacture> disps = DispositionManufacture.find("byItem", "E26").fetch();
 		renderJSON(disps);
 	}
 	
 	public static void test3() {
 		DistributionWish wish1 = DistributionWish.find("byItem", "P1").first();
 		wish1.period0 = 100;
 		wish1.period1 = 50;
 		wish1.period2 = 90;
 		wish1.period3 = 120;
 		DistributionWish wish2 = DistributionWish.find("byItem", "P2").first();
 		wish2.period0 = 70;
 		wish2.period1 = 20;
 		wish2.period2 = 90;
 		wish2.period3 = 100;
 		DistributionWish wish3 = DistributionWish.find("byItem", "P3").first();
 		wish3.period0 = 10;
 		wish3.period1 = 50;
 		wish3.period2 = 100;
 		wish3.period3 = 150;
 		
 		ApplicationLogic.calculateDisposition();
 		
 		List<DispositionOrder> orders = DispositionOrder.findAll();
 		for (DispositionOrder order : orders) {
 			Logger.info("Order: %s", order.item);
 		}
 		
 		List<DispositionOrder> dispoOrders = DispositionOrder.findAll();
 		for (DispositionOrder dispoOrder : dispoOrders) {
 			Item item = Item.find("byItemId", dispoOrder.item).first();
 			Logger.info("Disposition Order: %s Consumption0: %s Consumption1: %s Consumption2: %s Consumption3: %s Quantity: %s Mode: %s Stock: %s", dispoOrder.item, dispoOrder.consumptionPeriod0, dispoOrder.consumptionPeriod1, dispoOrder.consumptionPeriod2, dispoOrder.consumptionPeriod3, dispoOrder.quantity, dispoOrder.mode, item.amount);
 		}
 	}
 
 	public static void testLogin() {
 		setHeader();
 		renderJSON(true);
 	}
 
 	/**
 	 * save disposition orders
 	 */
 	public static void postDispositionOrders() {
 		setHeader();
 		String body = getBodyAsString();
 		ArrayList<DispositionOrder> orders = new JSONDeserializer<ArrayList<DispositionOrder>>().use("values", DispositionOrder.class).deserialize(body);
 		Logger.info("postDispositionOrders: %s", orders.size());
 		DispositionOrder.merge(orders);
 		ok();
 	}
 
 	/**
 	 * save capacities
 	 */
 	public static void postCapacity() {
 		setHeader();
 		String body = getBodyAsString();
 		// ApplicationLogic.calculateCapacity();
 		ArrayList<Capacity> capacities = new JSONDeserializer<ArrayList<Capacity>>().use("values", Capacity.class).deserialize(body);
 		Logger.info("postCapacity: %s", capacities.size());
 		Capacity.merge(capacities);
 		ok();
 	}
 
 	/**
 	 * save productionOrders
 	 */
 	public static void postProductionOrders() {
 		setHeader();
 		String body = getBodyAsString();
 		ArrayList<ProductionOrder> orders = new JSONDeserializer<ArrayList<ProductionOrder>>().use("values", ProductionOrder.class).deserialize(body);
 		if (orders != null && !orders.isEmpty()) {
 			Logger.info("postProductionOrders: %s %s", ProductionOrder.findAll().size(), orders.size());
 			Workplace.deleteAllProductionPlanLists();
 			ProductionOrder.deleteAll();			
 			ProductionOrder.saveAll(orders);
 		}
 		ApplicationLogic.calculateCapacity();
 		ApplicationLogic.calculateDisposition();
 		ok();
 	}
 
 	/**
 	 * save productionplan
 	 */
 	public static void postProductionPlan() {
 		setHeader();
 		String body = getBodyAsString();
 		ArrayList<DispositionManufacture> plan = new JSONDeserializer<ArrayList<DispositionManufacture>>().use("values", DispositionManufacture.class)
 				.deserialize(body);
 		Logger.info("postProductionPlan: %s", plan.size());
 		DispositionManufacture.merge(plan);
 		ApplicationLogic.planToOrder();
 		ApplicationLogic.calculateCapacity();
 		ApplicationLogic.calculateDisposition();
 		ok();
 	}
 
 	/**
 	 * save and merge posted deistributionWishs
 	 */
 	public static void postDistributionWishs() {
 		setHeader();
 		String body = getBodyAsString();
 		// Logger.info("postDistributionWish: %S", body);
 		ArrayList<DistributionWish> wishs = new JSONDeserializer<ArrayList<DistributionWish>>().use("values", DistributionWish.class).deserialize(body);
 		Logger.info("postDistributionWishs: %s", wishs.size());
 		DistributionWish.merge(wishs);
 		ApplicationLogic.wishToPlan();
 		ApplicationLogic.calcProductionPlan();
 		ApplicationLogic.planToOrder();
 		ApplicationLogic.calculateCapacity();
 		ApplicationLogic.calculateDisposition();
 		ok();
 	}
 
 	/**
 	 * get disposition orders
 	 */
 	public static void getDispositionOrders() {
 		setHeader();
 		Logger.info("getDispositionOrders");
 		List<DispositionOrder> orders = DispositionOrder.findAll();
 		renderJSON(new JSONSerializer().exclude("itemAsObject").serialize(orders));
 	}
 
 	/**
 	 * get calculated capacities of all workplaces
 	 */
 	public static void getCapacity() {
 		setHeader();
 		// ApplicationLogic.calculateCapacity();
 		Logger.info("getCapacity");
 		List<Capacity> capacities = Capacity.findAll();
 		renderJSON(new JSONSerializer().exclude("workplaceAsObject").serialize(capacities));
 	}
 
 	/**
 	 * get all saved productionOrders
 	 */
 	public static void getProductionOrders() {
 		setHeader();
 		Logger.info("getProductionOrders");
 		List<ProductionOrder> orders = ProductionOrder.find("order by orderNumber asc").fetch();
 		// if (orders == null || orders.isEmpty()) {
 		// ApplicationLogic.planToOrder();
 		// orders = ProductionOrder.find("order by orderNumber asc").fetch();
 		// }
 		renderJSON(new JSONSerializer().exclude("itemAsObject").serialize(orders));
 	}
 
 	/**
 	 * get productionplan
 	 */
 	public static void getProductionPlan() {
 		setHeader();
 		Logger.info("getProductionPlan");
 		List<DispositionManufacture> disps = DispositionManufacture.findAll();
 		// Logger.info("childs: %s", disps.get(0));
 		renderJSON(new JSONSerializer().include("itemChilds").exclude("itemAsObject", "ItemChildsAsObject").serialize(disps));
 	}
 
 	/**
 	 * deliver saved distributionWishs, if necessary create them first
 	 */
 	public static void getDistributenWishs() {
 		setHeader();
 		Logger.info("getDistributenWishs");
 		List<DistributionWish> wishs = DistributionWish.findAll();
 		if (wishs == null || wishs.isEmpty()) {
 			wishs = new ArrayList<>();
 			List<Item> pItems = Item.find("byType", "P").fetch();
 			for (Item item : pItems) {
 				DistributionWish wish = new DistributionWish();
 				wish.item = item.itemId;
 				wish.save();
 				wishs.add(wish);
 			}
 		}
 		renderJSON(new JSONSerializer().exclude("itemAsObject").serialize(wishs));
 	}
 	
 	/**
 	 * sets the method for expected delivery calculation
 	 */
 	public static void postUserMethod() {
 		setHeader();
 		String body = getBodyAsString();
 		User user = new JSONDeserializer<User>().deserialize(body);
 		Logger.info("postUserMethod: %s", user.method);
 		ok();
 	}
 	
 	/**
 	 * returns the method for expected delivery calculation
 	 */
 	public static void getUserMethod() {
 		setHeader();
 		Logger.info("getUserMethod");
 		List<User> users = User.findAll();
 		User user = users.get(0);		
 		
 		renderJSON(user.method);
 	}
 	
 	public static void reset() {
 		setHeader();
 		Logger.info("reset");
 		ApplicationLogic.resetData();
 	}
 
 	/**
 	 * Testmethod to develop xml parser with local xml file doesnt load
 	 * everytime the xml from the scsim website
 	 */
 	public static void parseXML() {
 		setHeader();
 		Logger.info("parseXML");
 		Template template = TemplateLoader.load("229_9_7result.xml");
 		String xmlFile = template.render();
 		InputStream in = IOUtils.toInputStream(xmlFile);
 		Parser p = new Parser(in);
 		p.parseDoc();
 		ApplicationLogic.wishToPlan();
 		ApplicationLogic.calcProductionPlan();
 		ApplicationLogic.planToOrder();
 		ApplicationLogic.calculateCapacity();
 		ApplicationLogic.calculateDisposition();
 		
 		List<User> users = User.findAll();
 		int actPeriod = Integer.valueOf(users.get(0).period);
 		
 		renderJSON(actPeriod);
 	}
 	
 	/**
 	 * downloads the input.xml
 	 */
 	public static void downloadXML() {
 		setHeader();
 		response.setContentTypeIfNotSet("application/x-download");  
 		response.setHeader("Content-disposition","attachment; filename=input.xml");
 		
 		Document doc = Parser.parseInputXML();
 		renderXml(doc);
 	}
 
 	/**
 	 * upload latest result xml file manually
 	 * 
 	 * @param file
 	 */
 	public static void uploadXML() {
 		setHeader();
 		
 		String xml = getBodyAsString();
 		Logger.info("uploadXML: %s", xml);
 		
		
		
 		InputStream in = IOUtils.toInputStream(xml);
 		Parser p = new Parser(in);
 		p.parseDoc();
 		
 		ApplicationLogic.wishToPlan();
 		ApplicationLogic.calcProductionPlan();
 		ApplicationLogic.planToOrder();
 		ApplicationLogic.calculateCapacity();
 		ApplicationLogic.calculateDisposition();
//		Template template = TemplateLoader.load("beauty.xml");
//		String xmlFile = template.render();
//		
//		renderText(xmlFile);
		List<User> users = User.findAll(); 
		int actPeriod = Integer.valueOf(users.get(0).period);
 		
		renderJSON(actPeriod);
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
 	 * 
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
 
 	/**
 	 * get request body as string
 	 * 
 	 * @return
 	 */
 	private static String getBodyAsString() {
 		return params.get("body");
 	}
 
 }
