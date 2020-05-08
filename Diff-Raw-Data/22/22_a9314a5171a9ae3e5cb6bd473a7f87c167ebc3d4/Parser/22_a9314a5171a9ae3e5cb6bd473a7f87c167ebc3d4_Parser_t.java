 package logic;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.List;
 import java.util.Locale;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import models.Capacity;
 import models.DispositionOrder;
 import models.DistributionWish;
 import models.Item;
 import models.OpenOrder;
 import models.ProductionOrder;
 import models.User;
 import models.WaitingList;
 import models.Workplace;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import play.Logger;
 
 public class Parser {
 
 	private Document document = null;
 
 	public Parser(InputStream in) {
 		try {
 			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			document = dBuilder.parse(in);
 			document.normalize();
 		} catch (ParserConfigurationException e) {
 			Logger.error("Couldnt get DocumentBuilder", e);
 		} catch (SAXException | IOException e) {
 			Logger.error("Couldnt parse InputStream to Document", e);
 		}
 	}
 
 	public void parseDoc() {
 		ApplicationLogic.resetData();
 		parsePeriod();
 		parseArticles();
 		parseOpenOrders();
 		parseWaitingLists();
 		parseOrderInWorks();
 //		ApplicationLogic.calcProductionPlan();
 	}
 
 	private void parsePeriod() {
 		Node period = document.getElementsByTagName("results").item(0);
 		NamedNodeMap att = period.getAttributes(); 
 		User user = new User();
 		user.period = getString(att, "period");
 		user.save();
 	}
 
 	private void parseArticles() {
 		NodeList articles = document.getElementsByTagName("article");
 		Logger.info("parseArticles %s", articles.getLength());
 		for (int i = 0, length = articles.getLength(); i < length; i++) {
 			Node node = articles.item(i);
 			NamedNodeMap att = node.getAttributes();
 			Item item = Item.find("byItemNumber", getInteger(att, "id")).first();
 			if (item == null) {
 				item = new Item();
 				item.itemNumber = getInteger(att, "id");
 			}
 			item.amount = getInteger(att, "amount");
 			item.price = getDouble(att, "price");
 			item.save();
 		}
 	}
 
 	private void parseOpenOrders() {
 		NodeList openOrders = document.getElementsByTagName("futureinwardstockmovement");
 		NodeList orders = openOrders.item(0).getChildNodes();
 		Logger.info("parseOpenOrders: %s", orders.getLength());
 		for (int i = 0, length = orders.getLength(); i < length; i++) {
			Node node = orders.item(i);			
 			NamedNodeMap att = node.getAttributes();
 			OpenOrder order = new OpenOrder();
			order.orderPeriod = getInteger(att, "orderperiod");				
 			order.mode = getInteger(att, "mode");
 			order.amount = getInteger(att, "amount");
 			Item item = Item.find("byItemNumber", getInteger(att, "article")).first();
 			order.item = item.itemId;
 			order.save();
 		}
 	}
 
 	private void parseWaitingLists() {
 		NodeList waitinglistworkstations = document.getElementsByTagName("waitinglistworkstations");
 		NodeList workplaces = waitinglistworkstations.item(0).getChildNodes();
 		Logger.info("parseWaitingLists %s", workplaces.getLength());
 		for (int i = 0, lengthWP = workplaces.getLength(); i < lengthWP; i++) {
 			NodeList waitingLists = workplaces.item(i).getChildNodes();
 			Node workplace = workplaces.item(i);
 			NamedNodeMap attWP = workplace.getAttributes();
 			Workplace wO = Workplace.find("byWorkplaceId", getInteger(attWP, "id")).first();
 			for (int j = 0, lengthWL = waitingLists.getLength(); j < lengthWL; j++) {
 				Node waiting = waitingLists.item(j);
 				NamedNodeMap attWL = waiting.getAttributes();
 				WaitingList wList = new WaitingList();
 				wList.period = getInteger(attWL, "period");
 				wList.amount = getInteger(attWL, "amount");
 				wList.orderNumber = getInteger(attWL, "order");
 				wList.timeneed = getInteger(attWL, "timeneed");
 				wList.workplace = wO.workplaceId;
 				Item it = Item.find("byItemNumber", getInteger(attWL, "item")).first();
 				wList.item = it.itemId;
 				wList.save();
 				wO.addWaitingList(wList);
 			}
 			wO.save();
 		}
 	}
 
 	private void parseOrderInWorks() {
 		NodeList ordersinwork = document.getElementsByTagName("ordersinwork");
 		NodeList workplaces = ordersinwork.item(0).getChildNodes();
 		Logger.info("parseOrderInWorks: %s", workplaces.getLength());
 		for (int i = 0, lengthWP = workplaces.getLength(); i < lengthWP; i++) {
 			Node workplace = workplaces.item(i);
 			NamedNodeMap attWP = workplace.getAttributes();
 			Workplace wO = Workplace.find("byWorkplaceId", getInteger(attWP, "id")).first();
 			WaitingList wList = new WaitingList();
 			wList.period = getInteger(attWP, "period");
 			wList.amount = getInteger(attWP, "amount");
 			wList.orderNumber = getInteger(attWP, "order");
 			wList.timeneed = getInteger(attWP, "timeneed");
 			wList.workplace = wO.workplaceId;
 			Item it = Item.find("byItemNumber", getInteger(attWP, "item")).first();
 			wList.item = it.itemId;
 			wList.inWork = true;
 			wList.save();
 			wO.inWork = wList.waitingListId;
 			wO.save();
 		}
 	}
 
 	@SuppressWarnings("all")
 	private Double getDouble(NamedNodeMap node, String attribute) {
 		try {
 			NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
 			return format.parse(node.getNamedItem(attribute).getNodeValue()).doubleValue();
 		} catch (DOMException | ParseException e) {
 			Logger.error("cant convert %s to double", node.getNamedItem(attribute).getNodeValue());
 			return null;
 		}
 	}
 
 	@SuppressWarnings("all")
 	private int getInteger(NamedNodeMap node, String attribute) {
 		return Integer.parseInt(node.getNamedItem(attribute).getNodeValue());
 	}
 
 	@SuppressWarnings("all")
 	private Long getLong(NamedNodeMap node, String attribute) {
 		return Long.parseLong(node.getNamedItem(attribute).getNodeValue());
 	}
 
 	@SuppressWarnings("all")
 	private String getString(NamedNodeMap node, String attribute) {
 		return node.getNamedItem(attribute).getNodeValue();
 	}
 	
 	public static Document parseInputXML(){
 
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder = null;
 		try {
 			docBuilder = docFactory.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {			
 			e.printStackTrace();
 		}
  
 		// root element
 		Document doc = docBuilder.newDocument();
 		Node rootElement = doc.createElement("input");
 		doc.appendChild(rootElement);
 		
 		Element qualitiycontrol = doc.createElement("qualitycontrol");
 		qualitiycontrol.setAttribute("delay", "0");
 		qualitiycontrol.setAttribute("losequantity", "0");
 		qualitiycontrol.setAttribute("type", "no");
 		rootElement.appendChild(qualitiycontrol);
 				
 		//Sellwishes
 		Node sellwishes = doc.createElement("sellwish");
 		rootElement.appendChild(sellwishes);
 		
 		//sellwish
 		List<DistributionWish> dist_wish = DistributionWish.findAll();
 		for (DistributionWish distributionWish : dist_wish) {
 			Element sellwish = doc.createElement("item");
 			
 			sellwish.setAttribute("article", distributionWish.item);
 			sellwish.setAttribute("quantity", String.valueOf(distributionWish.period0));
 						
 			sellwishes.appendChild(sellwish);
 		}
 		
 		//selldirect
 		Node selldirect = doc.createElement("selldirect");
 		rootElement.appendChild(selldirect);
 		
 		Element selldirects1 = doc.createElement("item");
 		selldirects1.setAttribute("article", "1");
 		selldirects1.setAttribute("penalty", "0.0");
 		selldirects1.setAttribute("price", "0.0");
 		selldirects1.setAttribute("quantity", "0");
 		
 		Element selldirects2 = doc.createElement("item");
 		selldirects2.setAttribute("article", "2");
 		selldirects2.setAttribute("penalty", "0.0");
 		selldirects2.setAttribute("price", "0.0");
 		selldirects2.setAttribute("quantity", "0");
 		
 		Element selldirects3 = doc.createElement("item");
 		selldirects3.setAttribute("article", "3");
 		selldirects3.setAttribute("penalty", "0.0");
 		selldirects3.setAttribute("price", "0.0");
 		selldirects3.setAttribute("quantity", "0");
  
 		//orderlist
 		Node orderlist = doc.createElement("orderlist");
 		rootElement.appendChild(orderlist);
  
 		//order
 		List<DispositionOrder> list = DispositionOrder.findAll();
 		for (DispositionOrder dispositionOrder : list) {
 			Element order = doc.createElement("order");
 			
 			order.setAttribute("article", dispositionOrder.item);
 			order.setAttribute("modus", String.valueOf(dispositionOrder.mode));
 			order.setAttribute("quantity", String.valueOf(dispositionOrder.quantity));
 						
 			orderlist.appendChild(order);
 		}
 		
 		//productionlist
 		Node productionlist = doc.createElement("productionlist");
 		rootElement.appendChild(productionlist);
 		
 		//production
 		List<ProductionOrder> prod_list = ProductionOrder.findAll();
 		for (ProductionOrder productionOrder : prod_list) {
 			Element production = doc.createElement("production");
 			
 			production.setAttribute("article", productionOrder.item);
 			production.setAttribute("quantity", String.valueOf(productionOrder.amount));
 						
 			productionlist.appendChild(production);
 		}
 		
 		//workingtimelist
 		Node workingtimelist = doc.createElement("workingtimelist");
 		rootElement.appendChild(workingtimelist);
 		
 		//workingtime
 		List<Capacity> capa_list = Capacity.findAll();
 		for (Capacity work : capa_list) {
 			
 			Element workingtime = doc.createElement("workingtime");
 			
 			workingtime.setAttribute("overtime", String.valueOf(work.overtime));
 			workingtime.setAttribute("shift", String.valueOf(work.shift));
 			workingtime.setAttribute("station", String.valueOf(work.workplace));
 						
 			workingtimelist.appendChild(workingtime);
 		}
 
 		
 		return doc;
 	}
 	
 	//method to convert Document to String
 	private static String getStringFromDocument(Document doc)
 	{
 	    try
 	    {
 	       DOMSource domSource = new DOMSource(doc);
 	       StringWriter writer = new StringWriter();
 	       StreamResult result = new StreamResult(writer);
 	       TransformerFactory tf = TransformerFactory.newInstance();
 	       Transformer transformer = tf.newTransformer();
 	       transformer.transform(domSource, result);
 	       return writer.toString();
 	    }
 	    catch(TransformerException ex)
 	    {
 	       ex.printStackTrace();
 	       return null;
 	    }
 	}
 
 }
