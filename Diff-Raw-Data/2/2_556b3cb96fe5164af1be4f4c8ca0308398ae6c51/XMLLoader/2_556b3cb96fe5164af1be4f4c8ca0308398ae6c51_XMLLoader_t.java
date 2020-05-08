 package utilities;
 
 import org.jdom2.*;
 import org.jdom2.input.SAXBuilder;
 
 import productInfo.Customer;
 import productInfo.Order;
 import productInfo.Product;
 import productInfo.ProductNotFoundException;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Deze class leest de data uit het XML bestand en zet deze om in een order object
  * 
  * @param path
  * @author Jorn
  */
 public class XMLLoader {
 	
 	/**
 	 * Functie leest de data uit het XML bestand en zet deze om in een order object
 	 * 
 	 * @param path Pad naar het bestand
 	 * @return order
 	 */
 	public static Order readOrder(String path) throws ProductNotFoundException{
 		
 		// Maakt een order aan en maakt hem leeg, deze wordt later gevuld met informatie
 		Order order = null;
 		
 		try {
 			// Bouwt het document
 			Document document = buildDocument(path);
 			
 			// Begin node aanwijzen
 			Element rootNode = document.getRootElement();
 			
 			// Haalt de datum uit het XML bestand
 			Date date = getDate(rootNode);
 	
 			// Haalt de totale prijs uit het XML bestand
 			float totalPrice = Float.parseFloat(rootNode.getChildText("totalprice"));
 	   
 			// Haalt de klant informatie uit het XML bestand
 			Customer customer = getCustomerInfo(rootNode);
 
 			//Vul de order met informatie
 			order = new Order(date, totalPrice, customer);
 			
 			// Haalt de producten uit het XML bestand doormiddel van een for-loop
 			List<Element> list = rootNode.getChildren("product");
 			for (int i = 0; i < list.size(); i++) {
 				// Haalt een product uit het xml bestand
 				getProductFromXML(order, list, i);
 			}
 		}
 		
 		// Vangt de eventuele foutmeldingen op
 		catch (IOException io) {
 			io.printStackTrace();
 		}
 		
 		catch (JDOMException jdomex) {
 			jdomex.printStackTrace();
 		} 
 		
 		catch (ParseException e) {
 			e.printStackTrace();
 		}
 		
 		// Returnt de gevulde order
 		return order;
 	}
 	
 	/**
 	 * Haalt een product uit het XML bestand en zet hem in de order
 	 * 
 	 * @param order 
 	 * @param list Lijst met producten
 	 * @param i Teller van de for loop
 	 */
 	private static void getProductFromXML(Order order, List<Element> list, int i)
 			throws ProductNotFoundException {
 		Element node = (Element) list.get(i);
 		int productId = Integer.parseInt(node.getChildText("productnumber"));
 		String description = node.getChildText("description");
 		float price = Float.parseFloat(node.getChildText("price"));
 		
 		//Haal het aangegeven aantal producten op
 		for(int n = 0 ; n < Integer.parseInt(node.getChildText("amount")); n++) {
 			Product product = new Product(productId, description, price);
 			order.addProduct(product);
 		}
 	}
 
 	/**
 	 * Haalt de klant informatie uit het XML bestand
 	 * 
 	 * @param rootNode De standaard node
 	 * @return customer
 	 */
 	private static Customer getCustomerInfo(Element rootNode) {
 		int customerId = Integer.parseInt(rootNode.getChildText("customernumber"));
 		String customername = rootNode.getChildText("customername");
 		Customer customer = new Customer(customerId, customername);
 		return customer;
 	}
 
 	/**
 	 * Haalt de datum uit het XML bestand
 	 * 
 	 * @param rootNode De standaard node
 	 * @return date
 	 */
 	private static Date getDate(Element rootNode) throws ParseException {
 		String dateXML = rootNode.getChildText("date");
 		Date date = new SimpleDateFormat("dd-MM-yyyy").parse(dateXML);
 		return date;
 	}
 	
 	/**
 	 * Bouwt een JDOM document van het XML bestand met behulp van een SAX parser
 	 * 
 	 * @param path Pad naar het bestand
 	 * @return document
 	 */
 	private static Document buildDocument(String path) throws JDOMException,
 			IOException {
 		SAXBuilder builder = new SAXBuilder();
 		File xmlFile = new File(path);
 		Document document = (Document) builder.build(xmlFile);
 		return document;
 	}
 }
