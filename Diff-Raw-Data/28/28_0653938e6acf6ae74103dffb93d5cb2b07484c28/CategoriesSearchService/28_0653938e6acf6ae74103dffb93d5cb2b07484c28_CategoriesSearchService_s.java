 package com.grupo3.productConsult.services;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import android.app.IntentService;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.ResultReceiver;
 
 import com.grupo3.productConsult.Category;
 import com.grupo3.productConsult.Product;
import com.grupo3.productConsult.utilities.PhoneUtils;
 import com.grupo3.productConsult.utilities.ServerURLGenerator;
 import com.grupo3.productConsult.utilities.XMLParser;
 
 public class CategoriesSearchService extends IntentService {
 
 	public static final String LOAD_CATEGORIES = "1";
 	public static final String LOAD_SUBCATEGORIES = "2";
 	public static final String LOAD_PRODUCTS_BY_SUBCATEGORY = "3";
	public static final String LOAD_PRODUCT = "3";
 
 	public static final int STATUS_ERROR = 0;
 	public static final int STATUS_SUCCESS = 1;
 
 	private static ServerURLGenerator catalogServer = new ServerURLGenerator(
 			"Catalog");
 
 	public CategoriesSearchService() {
 		super("CategoriesSearchService");
 	}
 
 	public static List<Category> fetchCategories()
 			throws ClientProtocolException, IOException {
 		catalogServer.clearParameters();
 		catalogServer.addParameter("method", "GetCategoryList");
 		catalogServer.addParameter("language_id", PhoneUtils.getLanguageId());
 		HttpResponse response = catalogServer.getServerResponse();
 		try {
 			List<Category> categories = new LinkedList<Category>();
 			XMLParser parser = new XMLParser(response);
 			NodeList catNodes = parser.getElements("category");
 			for (int i = 0; i < catNodes.getLength(); i++) {
 				categories.add(parseCategory(parser, catNodes.item(i)));
 			}
 			return categories;
 		} catch (ParseException e) {
 		} catch (ParserConfigurationException e) {
 		} catch (SAXException e) {
 		}
 		return null;
 	}
 
 	private static Category parseCategory(XMLParser parser, Node node) {
 		int id = Integer.parseInt(node.getAttributes().getNamedItem("id")
 				.getNodeValue());
 		String name = parser.getStringFromSingleElement("name", (Element) node);
 		return new Category(name, id);
 	}
 
 	public static List<Category> fetchSubCategories(int categoryId)
 			throws ClientProtocolException, IOException {
 		catalogServer.clearParameters();
 		catalogServer.addParameter("method", "GetSubcategoryList");
 		catalogServer.addParameter("language_id", PhoneUtils.getLanguageId());
 		catalogServer.addParameter("category_id", categoryId + "");
 		HttpResponse response = catalogServer.getServerResponse();
 		try {
 			List<Category> categories = new LinkedList<Category>();
 			XMLParser parser = new XMLParser(response);
 			NodeList subCatNodes = parser.getElements("subcategory");
 			for (int i = 0; i < subCatNodes.getLength(); i++) {
 				categories.add(parseCategory(parser, subCatNodes.item(i)));
 			}
 			return categories;
 		} catch (ParseException e) {
 		} catch (ParserConfigurationException e) {
 		} catch (SAXException e) {
 		}
 		return null;
 	}
 
 	public static List<Product> fetchProductsBySubcategory(int categoryId,
 			int subCategoryId) throws IOException {
 		catalogServer.clearParameters();
 		catalogServer.addParameter("method", "GetProductListBySubcategory");
 		catalogServer.addParameter("language_id", PhoneUtils.getLanguageId());
 		catalogServer.addParameter("category_id", categoryId + "");
 		catalogServer.addParameter("subcategory_id", subCategoryId + "");
 		HttpResponse response = catalogServer.getServerResponse();
 		try {
 			List<Product> products = new LinkedList<Product>();
 			XMLParser parser = new XMLParser(response);
 			NodeList productNodes = parser.getElements("product");
 			for (int i = 0; i < productNodes.getLength(); i++) {
 				products.add(parseProduct(parser, productNodes.item(i)));
 			}
 			return products;
 		} catch (ParseException e) {
 		} catch (ParserConfigurationException e) {
 		} catch (SAXException e) {
 		}
 		return null;
 	}
 
 	private static Product parseProduct(XMLParser parser, Node node) {
 		int id = Integer.parseInt(node.getAttributes().getNamedItem("id")
 				.getNodeValue());
 		String name = parser.getStringFromSingleElement("name", (Element) node);
 		double price = Double.parseDouble(parser.getStringFromSingleElement(
 				"price", (Element) node));
 		return new Product(id, name, price);
 	}
 
 	public static Product fetchProduct(int productId)
 			throws ClientProtocolException, IOException {
 		catalogServer.clearParameters();
 		catalogServer.addParameter("method", "GetProduct");
 		catalogServer.addParameter("product_id", productId + "");
 		HttpResponse response = catalogServer.getServerResponse();
 		try {
 			XMLParser parser = new XMLParser(response);
 			Node productNode = parser.getElements("product").item(0);
 			return parseProductComplete(parser, productNode);
 		} catch (ParseException e) {
 		} catch (ParserConfigurationException e) {
 		} catch (SAXException e) {
 		}
 
 		return null;
 	}
 
 	private static Product parseProductComplete(XMLParser parser, Node node) {
 		Product p = parseProduct(parser, node);
 		int categoryId = Integer.parseInt(parser.getStringFromSingleElement(
 				"category_id", (Element) node));
 		p.setCategoryId(categoryId);
 		int rank = Integer.parseInt(parser.getStringFromSingleElement(
 				"sales_rank", (Element) node));
 		p.setSaleRank(rank);
 		String imgSrc = parser.getStringFromSingleElement("image_url",
 				(Element) node);
 		p.setImgSrc(imgSrc);
 
 		if (categoryId == 1) {
 			p.setInformation("actors", parser.getStringFromSingleElement(
 					"actors", (Element) node));
 			p.setInformation("format", parser.getStringFromSingleElement(
 					"format", (Element) node));
 			p.setInformation("language", parser.getStringFromSingleElement(
 					"language", (Element) node));
 			p.setInformation("subtitles", parser.getStringFromSingleElement(
 					"subtitles", (Element) node));
 			p.setInformation("region", parser.getStringFromSingleElement(
 					"region", (Element) node));
 			p.setInformation("aspect_ratio", parser.getStringFromSingleElement(
 					"aspect_ratio", (Element) node));
 			p.setInformation("number_discs", parser.getStringFromSingleElement(
 					"number_discs", (Element) node));
 			p.setInformation("release_date", parser.getStringFromSingleElement(
 					"release_date", (Element) node));
 			p.setInformation("run_time", parser.getStringFromSingleElement(
 					"run_time", (Element) node));
 			p.setInformation("ASIN", parser.getStringFromSingleElement("ASIN",
 					(Element) node));
 		} else {
 			p.setInformation("authors", parser.getStringFromSingleElement(
 					"authors", (Element) node));
 			p.setInformation("publisher", parser.getStringFromSingleElement(
 					"publisher", (Element) node));
 			p.setInformation("published_date", parser
 					.getStringFromSingleElement("published_date",
 							(Element) node));
 			p.setInformation("ISBN_10", parser.getStringFromSingleElement(
 					"ISBN_10", (Element) node));
 			p.setInformation("ISBN_13", parser.getStringFromSingleElement(
 					"ISBN_13", (Element) node));
 			p.setInformation("language", parser.getStringFromSingleElement(
 					"language", (Element) node));
 		}
 		return p;
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		List<Category> categories;
 		ResultReceiver receiver = intent.getParcelableExtra("receiver");
 		String command = intent.getStringExtra("command");
 		Bundle bundle = new Bundle();
 		try {
 			if (LOAD_CATEGORIES.equals(command)) {
 				categories = fetchCategories();
 				bundle.putSerializable("categories", (Serializable) categories);
 			} else if (LOAD_SUBCATEGORIES.equals(command)) {
 				int id = Integer.parseInt(intent
 						.getStringExtra("subCategoryId"));
 				categories = fetchSubCategories(id);
 				bundle.putSerializable("subCategories",
 						(Serializable) categories);
 			} else if (LOAD_PRODUCTS_BY_SUBCATEGORY.equals(command)) {
 				int catId = Integer.parseInt(intent
 						.getStringExtra("categoryId"));
 				int subCatId = Integer.parseInt(intent
 						.getStringExtra("subCategoryId"));
 				List<Product> products = fetchProductsBySubcategory(catId,
 						subCatId);
 				bundle.putSerializable("products", (Serializable) products);
 			} else if (LOAD_PRODUCT.equals(command)) {
 				int prodId = Integer.parseInt(intent
 						.getStringExtra("productId"));
 				Product p = fetchProduct(prodId);
 				bundle.putSerializable("product", p);
 			}
 			receiver.send(STATUS_SUCCESS, bundle);
 		} catch (Exception e) {
 			receiver.send(STATUS_ERROR, bundle);
 		}
 	}
 }
