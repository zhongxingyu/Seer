 package hu.miracle.workers;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringWriter;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 public class XMLBuilder {
 
 	private JAXBContext context;
 
 	public XMLBuilder() {
 
 	}
 
 	public void logToConsole(String message) {
 		System.out.println(message);
 	}
 
 	// public void writeXML(Scene scene) throws JAXBException, IOException {
 	//
 	// //
 	// =============================================================================================================
 	// // Setup JAXB
 	// //
 	// =============================================================================================================
 	//
 	// // Create a JAXB context passing in the class of the object we want to
 	// // marshal/unmarshal
 	//
 	// //
 	// =============================================================================================================
 	// // Marshalling OBJECT to XML
 	// //
 	// =============================================================================================================
 	//
 	// context = JAXBContext.newInstance(Scene.class);
 	//
 	// // Create the marshaller, this is the nifty little thing that will
 	// // actually transform the object into XML
 	// final Marshaller marshaller = context.createMarshaller();
 	// marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
 	//
 	// // Create a stringWriter to hold the XML
 	// final StringWriter stringWriter = new StringWriter();
 	//
 	// // Marshal the javaObject and write the XML to the stringWriter
 	// marshaller.marshal(scene, stringWriter);
 	//
 	// // Print out the contents of the stringWriter
 	// File XML = new File("scene.xml");
 	// BufferedWriter bw = new BufferedWriter(new FileWriter(XML));
 	// bw.write(stringWriter.toString());
 	// System.out.println(stringWriter.toString());
 	// bw.close();
 	//
 	// }
 
 	// public Scene readXML() throws FileNotFoundException, JAXBException {
 	// //
 	// =============================================================================================================
 	// // Unmarshalling XML to OBJECT
 	// //
 	// =============================================================================================================
 	//
 	// context = JAXBContext.newInstance(Scene.class);
 	//
 	// // Create the unmarshaller, this is the nifty little thing that will
 	// // actually transform the XML back into an object
 	// final Unmarshaller unmarshaller = context.createUnmarshaller();
 	//
 	// // Unmarshal the XML in the stringWriter back into an object
 	// final Scene scene = (Scene) unmarshaller.unmarshal(new FileInputStream(
 	// "scene.xml"));
 	//
 	// // Print out the contents of the JavaObject we just unmarshalled from
 	// // the XML
 	// return scene;
 	// }
 
 	public Scene readXML(String path) throws SAXException, IOException,
 			ParserConfigurationException {
 
 		// AZ XML FÁJL FELDOLGOZÁSA
 
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		Document doc = db.parse(new InputSource(new FileInputStream(new File(
 				path))));
 		doc.getDocumentElement().normalize();
 
 		NodeList sceneRootElement = doc.getElementsByTagName("scene");
 
 		// ez itt baromsag soha ne csinaljatok ilyet mert csak egy root element
 		// van nincs tobb xDs
 		// Element creaturesElement = (Element) sceneRootElement.item(0);
 		// Element obstaclesElement = (Element) sceneRootElement.item(1);
 		// Element storagesElement = (Element) sceneRootElement.item(2);
 
 		NodeList creature = doc.getElementsByTagName("anteater");
 		NodeList obstacle = doc.getElementsByTagName("obstacle");
		NodeList antsinker = doc.getElementsByTagName("anthill");
 		NodeList foodstrg = doc.getElementsByTagName("foodstorage");
 		NodeList anthill = doc.getElementsByTagName("anthill");
 
 		Scene scene = new Scene();
 
 		for (int i = 0; i < creature.getLength(); i++) {
 
 			logToConsole("AntEater found...");
 			Element element = (Element) creature.item(i);
 			int x = Integer.parseInt(((Element) element.getElementsByTagName(
 					"position").item(0)).getAttribute("x"));
 			logToConsole("x: " + x);
 			int y = Integer.parseInt(((Element) element.getElementsByTagName(
 					"position").item(0)).getAttribute("y"));
 			logToConsole("y: " + y);
 			Point point = new Point(x, y);
 
 			AntEater ae = new AntEater(point, scene);
 
 			int hunger = Integer.parseInt(((Element) element
 					.getElementsByTagName("hunger").item(0)).getTextContent());
 			logToConsole("hunger: " + hunger);
 
 			int wait = Integer.parseInt(((Element) element
 					.getElementsByTagName("wait").item(0)).getTextContent());
 			logToConsole("wait: " + wait);
 
 			ae.setHunger(hunger);
 			ae.setWait(wait);
 			logToConsole("Add AntEater");
 			scene.getCreatures().add(ae);
 
 		}
 
 		for (int i = 0; i < obstacle.getLength(); i++) {
 
 			logToConsole("Obstacle found...");
 			Element element = (Element) obstacle.item(i);
 			int x = Integer.parseInt(((Element) element.getElementsByTagName(
 					"position").item(0)).getAttribute("x"));
 			logToConsole("x: " + x);
 			int y = Integer.parseInt(((Element) element.getElementsByTagName(
 					"position").item(0)).getAttribute("y"));
 			logToConsole("y: " + y);
 			Point point = new Point(x, y);
 
 			String color = ((Element) element.getElementsByTagName("color")
 					.item(0)).getTextContent();
 			logToConsole("Color: " + color);
 
 			Color clr = Color.white;
 			if (color.equals("red"))
 				clr = Color.red;
 			else if (color.equals("black"))
 				clr = Color.black;
 			else if (color.equals("green"))
 				clr = Color.green;
 			else if (color.equals("blue"))
 				clr = Color.blue;
 			else if (color.equals("yellow"))
 				clr = Color.yellow;
 
 			int radius = Integer.parseInt(((Element) element
 					.getElementsByTagName("radius").item(0)).getTextContent());
 
 			logToConsole("radius: " + radius);
 
 			String solid = ((Element) element.getElementsByTagName("solid")
 					.item(0)).getTextContent();
 			String movable = ((Element) element.getElementsByTagName("movable")
 					.item(0)).getTextContent();
 
 			logToConsole("solid: " + solid + ", movable: " + movable);
 
 			boolean sld;
 			boolean mvbl;
 
 			if (solid.equals("true"))
 				sld = true;
 			else
 				sld = false;
 
 			if (movable.equals("true"))
 				mvbl = true;
 			else
 				mvbl = false;
 
 			logToConsole("Add Obstacle");
 			Obstacle ob = new Obstacle(point, clr, radius, sld, mvbl);
 			scene.getObstacles().add(ob);
 		}
 
 		for (int i = 0; i < antsinker.getLength(); i++) {
 
 			logToConsole("AntSinker found...");
 			Element element = (Element) antsinker.item(i);
 			int x = Integer.parseInt(((Element) element.getElementsByTagName(
 					"position").item(0)).getAttribute("x"));
 			logToConsole("x: " + x);
 
 			int y = Integer.parseInt(((Element) element.getElementsByTagName(
 					"position").item(0)).getAttribute("y"));
 			logToConsole("y: " + y);
 
 			Point point = new Point(x, y);
 
 			logToConsole("Add AntSinker");
 			AntSinker as = new AntSinker(point);
 			scene.getObstacles().add(as);
 
 		}
 
 		for (int i = 0; i < foodstrg.getLength(); i++) {
 
 			logToConsole("FoodStorage found...");
 			Element element = (Element) foodstrg.item(i);
 
 			int x = Integer.parseInt(((Element) element.getElementsByTagName(
 					"position").item(0)).getAttribute("x"));
 			logToConsole("x: " + x);
 
 			int y = Integer.parseInt(((Element) element.getElementsByTagName(
 					"position").item(0)).getAttribute("y"));
 			logToConsole("y: " + y);
 
 			Point point = new Point(x, y);
 
 			int capacity = Integer
 					.parseInt(((Element) element.getElementsByTagName(
 							"capacity").item(0)).getTextContent());
 			logToConsole("capacity: " + capacity);
 
 			int packet = Integer.parseInt(((Element) element
 					.getElementsByTagName("packet").item(0)).getTextContent());
 			logToConsole("packet: " + packet);
 
 			logToConsole("Add FoodStorage...");
 			FoodStorage fs = new FoodStorage(point, capacity, packet);
 			scene.getStorages().add(fs);
 
 		}
 
 		for (int i = 0; i < anthill.getLength(); i++) {
 
 			logToConsole("AntHill found...");
 			Element element = (Element) anthill.item(i);
 			int x = Integer.parseInt(((Element) element.getElementsByTagName(
 					"position").item(0)).getAttribute("x"));
 			logToConsole("x: " + x);
 
 			int y = Integer.parseInt(((Element) element.getElementsByTagName(
 					"position").item(0)).getAttribute("y"));
 			logToConsole("y: " + y);
 
 			Point point = new Point(x, y);
 
 			int amount = Integer.parseInt(((Element) element
 					.getElementsByTagName("amount").item(0)).getTextContent());
 			logToConsole("amount: " + amount);
 
 			int packet = Integer.parseInt(((Element) element
 					.getElementsByTagName("packet").item(0)).getTextContent());
 			logToConsole("packet: " + packet);
 
 			logToConsole("Add AntHill");
 			AntHill ah = new AntHill(point, scene, amount, packet);
 			scene.getStorages().add(ah);
 
 		}
 
 		return scene;
 	}
 
 }
