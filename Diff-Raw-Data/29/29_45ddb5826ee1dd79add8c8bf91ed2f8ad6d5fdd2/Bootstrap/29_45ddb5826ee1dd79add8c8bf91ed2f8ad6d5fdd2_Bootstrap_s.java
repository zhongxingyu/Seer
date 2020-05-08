 /**
  * @author Boris
  */
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import models.Component;
 import models.Item;
 
 import org.apache.commons.io.IOUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import play.Logger;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import play.templates.Template;
 import play.templates.TemplateLoader;
 import play.test.Fixtures;
 
 @OnApplicationStart
 public class Bootstrap extends Job {
 	public void doJob() {
 		Logger.info("Bootstrap");
 		Fixtures.deleteAllModels();
		Fixtures.loadModels("initial-items.yml", "initial-distributions.yml", "initial-workplaces.yml", "ItemTime.yml", "initial-dispositionOrder.yml", "initial-productionPlan.yml");
 		createComponents();
 	}
 
 	public static void createComponents() {
 		Template template = TemplateLoader.load("initial-components.xml");
 		String xmlFile = template.render();
 		InputStream in = IOUtils.toInputStream(xmlFile);
 		DocumentBuilder dBuilder = null;
 		Document document = null;
 		try {
 			dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			document = dBuilder.parse(in);
 		} catch (ParserConfigurationException | SAXException | IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		document.normalize();
 
 		NodeList component = document.getElementsByTagName("component");
 		for (int i = 0, length = component.getLength(); i < length; i++) {
 			Node node = component.item(i);
 
 			if (node.getNodeType() == Node.ELEMENT_NODE) {
 
 				Element ele = (Element) node;
 
 				Component comp = new Component();
 				String id = ele.getElementsByTagName("id").item(0).getTextContent();
 				String amount = ele.getElementsByTagName("amount").item(0).getTextContent();
 				String item = ele.getElementsByTagName("item").item(0).getTextContent();
 
 				comp.amount = Integer.parseInt(amount);
 				List<Item> items = Item.find("byItemId", item).fetch();
 				comp.item = items.get(0);
 				comp.save();
 
 				comp.amount = Integer.parseInt(amount);
 				List<Item> items2 = Item.find("byItemId", id).fetch();
 				Item item2 = items2.get(0);
 				item2.addComp(comp);
 				item2.save();
 
 			}
 		}
 	}
 }
