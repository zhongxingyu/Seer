 package com.jpizarro.th.client.model.util.xml;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.http.HttpEntity;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.jpizarro.th.client.model.service.to.response.GenericGameResponseTO;
 import com.jpizarro.th.entity.Game;
 import com.jpizarro.th.entity.User;
 
 import es.sonxurxo.gpsgame.client.util.exception.ServerException;
 
 public class XMLToBussinessConversor {
 	private static String TAG = "HttpHelper";
 	
 	private static Document parseHttpEntity(HttpEntity entity) throws Exception {
 		InputStream in = null;
 		try {
 			in = entity.getContent();
 		} catch (Exception e) {
 			throw e;
 		}
 		
 		Document document = null;
 		try {
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			document = db.parse(in);
 		} catch (IOException ioe) {
 			throw ioe;
 		} catch (ParserConfigurationException pce) {
 			throw new Exception(pce);
 		} catch (SAXException se) {
 			throw new Exception(se);
 		}
 		return document;
 	}
 	
 	public static User toUser(HttpEntity entity) throws Exception{
 		//		throw new Exception("Not Impl");
 		User user = new User();
 		Document document = parseHttpEntity(entity);
 		if (document.getElementsByTagName("user").getLength() == 1) {
 			
 			Element element = (Element)document.getElementsByTagName(
 			"username").item(0);
 			String username = element.getChildNodes().item(0).getNodeValue();
 			user.setUserName(username.replace("+", " "));
 			
 			
 		}else {
 			Element codeElement = (Element)document.getElementsByTagName(
 			"code").item(0);
 			int code = Integer.parseInt(codeElement.getChildNodes().item(0).
 					getNodeValue());
 			Element messageElement = (Element)document.getElementsByTagName(
 			"message").item(0);
 			String message = messageElement.getChildNodes().item(0).getNodeValue();
 
 			throw new ServerException(code, message);
 		}
 		
 		return user;
 	}
 	public static Game toGame(HttpEntity entity) throws Exception{
 		throw new ServerException(ServerException.NOT_IMPL, "Not Impl: "+TAG+" toGame");
 	}
 	private static Game toGame(Element gameDocument) throws Exception {
 		throw new ServerException(ServerException.NOT_IMPL, "Not Impl: "+TAG+" toGame");
 	}
 	public static List<String> toCityList(HttpEntity entity) throws Exception {
 		Document citiesDocument = parseHttpEntity(entity);
 		List<String> cityList = new ArrayList<String>();
 		
 		if (citiesDocument.getElementsByTagName("cities").getLength() == 1) {
 			NodeList cities = citiesDocument.getElementsByTagName("city");
 			for (int i=0;i<cities.getLength();i++) {
 				cityList.add(cities.item(i).getChildNodes().item(0).getNodeValue());
 			}
 		}
		else{
			throw new ServerException(ServerException.NOT_IMPL, "Not Impl: "+TAG+" toCityList empty");
		}
 		return cityList;
 	}
 	public static boolean toBooleanOrExceptionRegister(HttpEntity entity) 
 	throws Exception {
 
 		Document gameDocument = parseHttpEntity(entity);
 		
 		if (gameDocument.getElementsByTagName("registered").getLength() == 1) {
 		
 			return true;
 		}
 		else {
 			Element codeElement = (Element)gameDocument.getElementsByTagName(
 					"code").item(0);
 			int code = Integer.parseInt(codeElement.getChildNodes().item(0).
 					getNodeValue());
 			Element messageElement = (Element)gameDocument.getElementsByTagName(
 					"message").item(0);
 			String message = messageElement.getChildNodes().item(0).getNodeValue();
 			
 			throw new ServerException(code, message);
 		}
 	}
 	public static boolean toBooleanOrExceptionUpdate(HttpEntity entity) 
 	throws Exception {
 
 		Document gameDocument = parseHttpEntity(entity);
 		
 		if (gameDocument.getElementsByTagName("updated").getLength() == 1) {
 		
 			return true;
 		}
 		else {
 			Element codeElement = (Element)gameDocument.getElementsByTagName(
 					"code").item(0);
 			int code = Integer.parseInt(codeElement.getChildNodes().item(0).
 					getNodeValue());
 			Element messageElement = (Element)gameDocument.getElementsByTagName(
 					"message").item(0);
 			String message = messageElement.getChildNodes().item(0).getNodeValue();
 			
 			throw new ServerException(code, message);
 		}
 	}
 	
 	public static boolean toBooleanOrExceptionChangePassword(HttpEntity entity) 
 	throws Exception {
 
 		Document gameDocument = parseHttpEntity(entity);
 		
 		if (gameDocument.getElementsByTagName("passwordChanged").getLength() == 1) {
 		
 			return true;
 		}
 		else {
 			Element codeElement = (Element)gameDocument.getElementsByTagName(
 					"code").item(0);
 			int code = Integer.parseInt(codeElement.getChildNodes().item(0).
 					getNodeValue());
 			Element messageElement = (Element)gameDocument.getElementsByTagName(
 					"message").item(0);
 			String message = messageElement.getChildNodes().item(0).getNodeValue();
 			
 			throw new ServerException(code, message);
 		}
 	}
 	
 	public static boolean toBooleanOrExceptionJoin(HttpEntity entity) 
 	throws Exception {
 
 		Document gameDocument = parseHttpEntity(entity);
 		
 		if (gameDocument.getElementsByTagName("joined").getLength() == 1) {
 		
 			return true;
 		}
 		else {
 			Element codeElement = (Element)gameDocument.getElementsByTagName(
 					"code").item(0);
 			int code = Integer.parseInt(codeElement.getChildNodes().item(0).
 					getNodeValue());
 			Element messageElement = (Element)gameDocument.getElementsByTagName(
 					"message").item(0);
 			String message = messageElement.getChildNodes().item(0).getNodeValue();
 			
 			throw new ServerException(code, message);
 		}
 	}
 	public static boolean toBooleanOrExceptionRemove(HttpEntity entity) 
 	throws Exception {
 
 		Document gameDocument = parseHttpEntity(entity);
 		
 		if (gameDocument.getElementsByTagName("abandoned").getLength() == 1) {
 		
 			return true;
 		}
 		else {
 			Element codeElement = (Element)gameDocument.getElementsByTagName(
 					"code").item(0);
 			int code = Integer.parseInt(codeElement.getChildNodes().item(0).
 					getNodeValue());
 			Element messageElement = (Element)gameDocument.getElementsByTagName(
 					"message").item(0);
 			String message = messageElement.getChildNodes().item(0).getNodeValue();
 			
 			throw new ServerException(code, message);
 		}
 	}
 
 	public static boolean toBooleanOrExceptionSend(HttpEntity entity) 
 	throws Exception {
 
 		Document gameDocument = parseHttpEntity(entity);
 		
 		if (gameDocument.getElementsByTagName("messageSent").getLength() == 1) {
 		
 			return true;
 		}
 		else {
 			Element codeElement = (Element)gameDocument.getElementsByTagName(
 					"code").item(0);
 			int code = Integer.parseInt(codeElement.getChildNodes().item(0).
 					getNodeValue());
 			Element messageElement = (Element)gameDocument.getElementsByTagName(
 					"message").item(0);
 			String message = messageElement.getChildNodes().item(0).getNodeValue();
 			
 			throw new ServerException(code, message);
 		}
 	}	
 }
 
