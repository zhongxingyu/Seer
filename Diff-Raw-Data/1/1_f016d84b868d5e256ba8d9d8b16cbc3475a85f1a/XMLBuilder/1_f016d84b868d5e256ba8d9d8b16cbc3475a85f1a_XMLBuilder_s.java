 package hu.miracle.workers;
 
 import java.awt.Color;
import java.awt.Point;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 public class XMLBuilder {
 
 	// loggolas csak akkor tortenik ha ez a valtozo true -> D as debug
 	private boolean D = false;
 
 	public XMLBuilder() {
 
 	}
 
 	public void logToConsole(String message) {
 		if (D)
 			System.out.println(message);
 	}
 
 	public Scene readXML(String path) throws SAXException, IOException, ParserConfigurationException {
 
 		// AZ XML FÁJL FELDOLGOZÁSA
 		// Letrehozunk egy uj document builder factory-t
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
 		Document doc = db.parse(new InputSource(new FileInputStream(new File(path))));
 
 		// Normalizaljuk a beolvasott fajlt, letrejon egy fa struktura a
 		// memoriaban amiben konnyeden tudunk keresni
 		doc.getDocumentElement().normalize();
 
 		// A tageknek megfelelo nodelistek
 		NodeList creature;
 		NodeList obstacle;
 		NodeList antsinker;
 		NodeList foodstrg;
 		NodeList anthill;
 
 		// anteater tagre keresunk, amennyiben letezik a nodelistbe megtalalhato
 		// lesz az osszes anteater taggel ellatott elem
 		// amennyiben nem talalunk egyetse null lesz az objektum
 		if (doc.getElementsByTagName("anteater") != null) {
 			creature = doc.getElementsByTagName("anteater");
 		} else {
 			creature = null;
 		}
 
 		// obstacle tagre keresunk, amennyiben letezik a nodelistbe megtalalhato
 		// lesz az osszes obstacle taggel ellatott elem
 		// amennyiben nem talalunk egyetse null lesz az objektum
 		if (doc.getElementsByTagName("obstacle") != null) {
 			obstacle = doc.getElementsByTagName("obstacle");
 		} else {
 			obstacle = null;
 		}
 
 		// antsinker tagre keresunk, amennyiben letezik a nodelistbe
 		// megtalalhato
 		// lesz az osszes antsinker taggel ellatott elem
 		// amennyiben nem talalunk egyetse null lesz az objektum
 		if (doc.getElementsByTagName("antsinker") != null) {
 			antsinker = doc.getElementsByTagName("antsinker");
 		} else {
 			antsinker = null;
 		}
 
 		// foodstorage tagre keresunk, amennyiben letezik a nodelistbe
 		// megtalalhato
 		// lesz az osszes foodstorage taggel ellatott elem
 		// amennyiben nem talalunk egyetse null lesz az objektum
 		if (doc.getElementsByTagName("foodstorage") != null) {
 			foodstrg = doc.getElementsByTagName("foodstorage");
 		} else {
 			foodstrg = null;
 		}
 
 		// anthill tagre keresunk, amennyiben letezik a nodelistbe megtalalhato
 		// lesz az osszes anthill taggel ellatott elem
 		// amennyiben nem talalunk egyetse null lesz az objektum
 		if (doc.getElementsByTagName("anthill") != null) {
 			anthill = doc.getElementsByTagName("anthill");
 		} else {
 			anthill = null;
 		}
 
 		// letrehozzuk a szinteret amibe a beolvasott elemeket elhelyezzuk
 		Scene scene = new Scene();
 
 		// creaturek beolvassa
 		if (creature != null) {
 			for (int i = 0; i < creature.getLength(); i++) {
 
 				logToConsole("AntEater found...");
 				Element element = (Element) creature.item(i);
 
 				// csak akkor olvassuk be az objektumot ha minden mezoje
 				// helyesen van kitoltve
 				if (element.getElementsByTagName("position").item(0) != null && element.getElementsByTagName("hunger").item(0) != null
 						&& element.getElementsByTagName("wait").item(0) != null) {
 
 					// koordinatak beolvasasa
 					int x = Integer.parseInt(((Element) element.getElementsByTagName("position").item(0)).getAttribute("x"));
 					logToConsole("x: " + x);
 					int y = Integer.parseInt(((Element) element.getElementsByTagName("position").item(0)).getAttribute("y"));
 					logToConsole("y: " + y);
 
 					Point point = new Point(x, y);
 
 					// ehseg es varakozas mennyisegenek beolvasasa
 					int hunger = Integer.parseInt(((Element) element.getElementsByTagName("hunger").item(0)).getTextContent());
 					logToConsole("hunger: " + hunger);
 
 					int wait = Integer.parseInt(((Element) element.getElementsByTagName("wait").item(0)).getTextContent());
 					logToConsole("wait: " + wait);
 
 					AntEater ae = new AntEater(point, scene, wait, hunger);
 
 					logToConsole("Add AntEater");
 					// hozza adjuk a szinterhez a beolvasott objektumot
 					scene.getCreatures().add(ae);
 
 				} else {
 					// amennyiben hibas a beolvasas a kimenetre hibauzenetet
 					// kuldunk
 					System.out.println("Cannot create object, fields are missing! Check XML format!");
 				}
 			}
 		}
 
 		// akadalyok beolvasasa
 		if (obstacle != null) {
 			for (int i = 0; i < obstacle.getLength(); i++) {
 
 				logToConsole("Obstacle found...");
 				Element element = (Element) obstacle.item(i);
 
 				// csak akkor olvassuk be az objektumot ha minden mezoje
 				// helyesen van kitoltve
 				if (element.getElementsByTagName("position").item(0) != null && element.getElementsByTagName("radius").item(0) != null
 						&& element.getElementsByTagName("color").item(0) != null && element.getElementsByTagName("solid").item(0) != null
 						&& element.getElementsByTagName("movable").item(0) != null) {
 
 					// koordinatak beolvasasa
 					int x = Integer.parseInt(((Element) element.getElementsByTagName("position").item(0)).getAttribute("x"));
 					logToConsole("x: " + x);
 					int y = Integer.parseInt(((Element) element.getElementsByTagName("position").item(0)).getAttribute("y"));
 					logToConsole("y: " + y);
 					Point point = new Point(x, y);
 
 					// szin beolvasasa
 					String color = ((Element) element.getElementsByTagName("color").item(0)).getTextContent();
 					logToConsole("Color: " + color);
 
 					// mivel stringkent olvassuk be ezert vizsgaljuk milyen
 					// szint kell hozzaadnunk
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
 
 					// kiterjedes beolvasasa
 					int radius = Integer.parseInt(((Element) element.getElementsByTagName("radius").item(0)).getTextContent());
 
 					logToConsole("radius: " + radius);
 
 					// athatolhatosag es mozgathatosag beolvasasa
 					String solid = ((Element) element.getElementsByTagName("solid").item(0)).getTextContent();
 					String movable = ((Element) element.getElementsByTagName("movable").item(0)).getTextContent();
 
 					logToConsole("solid: " + solid + ", movable: " + movable);
 
 					boolean sld;
 					boolean mvbl;
 
 					// mivel stingkent van kezelve ezert megvizsgaljuk hogy true
 					// vagy false az ertek
 					// ami szamunkra kell
 					if (solid.equals("true"))
 						sld = true;
 					else
 						sld = false;
 
 					if (movable.equals("true"))
 						mvbl = true;
 					else
 						mvbl = false;
 
 					logToConsole("Add Obstacle");
 
 					// objektum hozzaadasa a szinterhez
 					Obstacle ob = new Obstacle(point, clr, radius, sld, mvbl);
 					scene.getObstacles().add(ob);
 
 				} else {
 					// amennyiben hibas a beolvasas a kimenetre hibauzenetet
 					// kuldunk
 					System.out.println("Cannot create object, fields are missing! Check XML format!");
 				}
 			}
 		}
 
 		// antsinker objektumok beolvasasa
 		if (antsinker != null) {
 			for (int i = 0; i < antsinker.getLength(); i++) {
 
 				logToConsole("AntSinker found...");
 				Element element = (Element) antsinker.item(i);
 
 				// csak akkor olvassuk be az objektumot ha minden mezoje megvan
 				if (element.getElementsByTagName("position").item(0) != null) {
 
 					// koordinatak beolvasasa
 					int x = Integer.parseInt(((Element) element.getElementsByTagName("position").item(0)).getAttribute("x"));
 					logToConsole("x: " + x);
 
 					int y = Integer.parseInt(((Element) element.getElementsByTagName("position").item(0)).getAttribute("y"));
 					logToConsole("y: " + y);
 
 					Point point = new Point(x, y);
 
 					logToConsole("Add AntSinker");
 
 					// beolvasott objektum hozzadasa a szinterhez
 					AntSinker as = new AntSinker(point);
 					scene.getObstacles().add(as);
 
 				} else {
 					// amennyiben hibas a beolvasas a kimenetre hibauzenetet
 					// kuldunk
 					System.out.println("Cannot create object, fields are missing! Check XML format!");
 				}
 
 			}
 		}
 
 		// etelraktarak beolvasasa
 		if (foodstrg != null) {
 			for (int i = 0; i < foodstrg.getLength(); i++) {
 
 				logToConsole("FoodStorage found...");
 				Element element = (Element) foodstrg.item(i);
 
 				// csak akkor olvassuk be az objektumot ha minden mezoje
 				// megfeleloen ki van toltve
 				if (element.getElementsByTagName("position").item(0) != null && element.getElementsByTagName("capacity").item(0) != null
 						&& element.getElementsByTagName("packet").item(0) != null) {
 
 					// koordinatak beolvasasa
 					int x = Integer.parseInt(((Element) element.getElementsByTagName("position").item(0)).getAttribute("x"));
 					logToConsole("x: " + x);
 
 					int y = Integer.parseInt(((Element) element.getElementsByTagName("position").item(0)).getAttribute("y"));
 					logToConsole("y: " + y);
 
 					Point point = new Point(x, y);
 
 					// capacity beolvasasa
 					int capacity = Integer.parseInt(((Element) element.getElementsByTagName("capacity").item(0)).getTextContent());
 					logToConsole("capacity: " + capacity);
 
 					// packet beolvasasa
 					int packet = Integer.parseInt(((Element) element.getElementsByTagName("packet").item(0)).getTextContent());
 					logToConsole("packet: " + packet);
 
 					logToConsole("Add FoodStorage...");
 
 					// a beolvasott objektum hozzaadasa a szinterhez
 					FoodStorage fs = new FoodStorage(point, capacity, packet);
 					scene.getStorages().add(fs);
 
 				} else {
 					// amennyiben nem megfeleloen vannak a mezok kitoltve nem
 					// olvassuk be az objektumot
 					System.out.println("Cannot create object, fields are missing! Check XML format!");
 				}
 
 			}
 		}
 
 		// anthillek beolvasasa
 		if (anthill != null) {
 			for (int i = 0; i < anthill.getLength(); i++) {
 
 				logToConsole("AntHill found...");
 				Element element = (Element) anthill.item(i);
 
 				// csak akkor olvassuk be az objektumot ha megfeleloen ki van
 				// toltve minden mezoje
 				if (element.getElementsByTagName("position").item(0) != null && element.getElementsByTagName("amount").item(0) != null
 						&& element.getElementsByTagName("packet").item(0) != null) {
 
 					// koordinatak beolvasasa
 					int x = Integer.parseInt(((Element) element.getElementsByTagName("position").item(0)).getAttribute("x"));
 					logToConsole("x: " + x);
 
 					int y = Integer.parseInt(((Element) element.getElementsByTagName("position").item(0)).getAttribute("y"));
 					logToConsole("y: " + y);
 
 					Point point = new Point(x, y);
 
 					// amount beolvasasa
 					int amount = Integer.parseInt(((Element) element.getElementsByTagName("amount").item(0)).getTextContent());
 					logToConsole("amount: " + amount);
 
 					// packet beolvasasa
 					int packet = Integer.parseInt(((Element) element.getElementsByTagName("packet").item(0)).getTextContent());
 					logToConsole("packet: " + packet);
 
 					logToConsole("Add AntHill");
 
 					// beolvasott objektum hozza adasa a szinterhez
 					AntHill ah = new AntHill(point, scene, amount, packet);
 					scene.getStorages().add(ah);
 
 				} else {
 					// amennyiben nem megfelelo a formatim nem olvassuk be az
 					// objektumot
 					System.out.println("Cannot create object, fields are missing! Check XML format!");
 				}
 
 			}
 		}
 
 		// vissza adjuk az elkeszult szinteret ami tartalmazza az osszes xml-ben
 		// leirt objektumot
 		return scene;
 	}
 }
