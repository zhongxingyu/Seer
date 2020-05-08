 package jdressel.DerporiaPrime;
 
 import javax.servlet.http.HttpSession;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 public class Utility {
 
 	public static String loginHeaderBanner(Object userObject){
 		String username = "";
 		//Determine user
 		try{
 			User user = (User) userObject;
 			username = user.getUN();
 		} catch (ClassCastException e){
 			//Assume logged out if exception occurs
 			username = "";
 		}
 		if (username==""||username==null){
 			return("<div class=\"username\">\n<form name=\"loginForm\"  action=\"ProcessLogin\" method=\"post\">\nLog In: <input type=\"text\" name=\"username\" placeholder=\"Username\" onkeypress=\"checkEnter(event)\">\n	<input class=\"regular\" type=\"submit\" value=\"Login\" name=\"Log In\"/>\n</form></div>");
 		} 
 
 		return("<div class=\"username\">You are logged in as "+username+" <a href=ProcessLogout> Logout</a></div>");
 	}
 	
 	public static String getUsername(Object userObject){
 		String username = "";
 		//Determine user
 		try{
 			User user = (User) userObject;
 			username = user.getUN();
 		} catch (ClassCastException e){
 			//Assume logged out if exception occurs
 			username = "";
 		}
 		return username;
 	}
 	
 	public static String getUsername(HttpSession session){
 		//Object derp  = request.getSession().getAttribute("username")==null ? "" : request.getSession().getAttribute("username");
 		//out.println(Utility.loginHeaderBanner(derp));
 		Object userAttribute = session.getAttribute("username");
 		String username = "";
 		try{
 			User user = (User) userAttribute;
 			username = user.getUN();
 		} catch (ClassCastException e){
 			username = "";//TODO raise exception if user does not exist
 		} catch (NullPointerException e){
 			username = "";
 		}
 
 		return username;
 	}
 	
 	public static Boolean isLoggedIn(HttpSession session){
 		if(getUsername(session).equals(""))
 			return false;
 		return true;
 	}
 	
 	public void saveUsers(Map<String, User> userMap) throws ParserConfigurationException, TransformerException, SAXException, IOException{
 		
 		File file = new File("users.xml");
 		file.delete();
 		
 		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();;
 
 		Element users = doc.createElement("users");
 		doc.appendChild(users);
 		 
 		// user elements
 		for(Map.Entry<String, User> entry : userMap.entrySet()){
 			Element user = doc.createElement("user");
 			users.appendChild(user);
 			 
 			user.setAttribute("username", entry.getValue().getUN());
 			user.setAttribute("password", "password");
 			//TODO get password user.setAttribute("password", entry.getValue().getPassword());
 			 
 			// votedOn loop until set is done, same for disagree, convinced and unsure
 			for(Assertion a: entry.getValue().getAssertions()){
 				Element votedOn = doc.createElement("votedOn");
 				votedOn.appendChild(doc.createTextNode(a.getId()));
 				user.appendChild(votedOn);
 			}
 			
 			for(Assertion a: entry.getValue().getDisagree()){
 				Element disagree = doc.createElement("disagree");
 				disagree.appendChild(doc.createTextNode(a.getId()));
 				user.appendChild(disagree);
 			}
 			
 			for(Assertion a: entry.getValue().getConvinced()){
 				Element convinced = doc.createElement("convinced");
 				convinced.appendChild(doc.createTextNode(a.getId()));
 				user.appendChild(convinced);
 			}
 			
 			for(Assertion a: entry.getValue().getUnsure()){
 				Element unsure = doc.createElement("unsure");
 				unsure.appendChild(doc.createTextNode(a.getId()));
 				user.appendChild(unsure);
 			}
 		}
 		
 		 
 
 		
 		// write the content into xml file
 		Transformer transformer = TransformerFactory.newInstance().newTransformer();
 		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); //indent is automatically set to 0
 		DOMSource source = new DOMSource(doc);
 		StreamResult result = new StreamResult(new File("users.xml"));
 		
 		transformer.transform(source, result);
 		
 		//new idea?
 		//Transformer transformer = TransformerFactory.newInstance().newTransformer();
 		//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 	
 		//StreamResult result = new StreamResult(new StringWriter());
 		//DOMSource source = new DOMSource(doc);
 		//transformer.transform(source, result);
 
 		//String xmlOutput = result.getWriter().toString();
 		//System.out.println(xmlOutput);
 	}
 	
 	public void saveAssertions(Set<Assertion> assertionSet)throws ParserConfigurationException, TransformerException, SAXException, IOException{
 		
 		File file = new File("assertions.xml");
 		file.delete();
 		
 		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();;
 
 		Element assertions = doc.createElement("assertions");
 		doc.appendChild(assertions);
 		 
 		// assertion elements
 		for(Assertion a: assertionSet){
 			Element assertion = doc.createElement("assertion");
 			assertions.appendChild(assertion);
 			
 			assertion.setAttribute("uuid",	a.getId());
 			
			Element title = doc.createElement("title");
			title.appendChild(doc.createTextNode(a.getName()));
			assertion.appendChild(title);
			
			Element body = doc.createElement("body");
			title.appendChild(doc.createTextNode(a.getBody()));
			assertion.appendChild(body);			
 			
 		}
 		
 		// write the content into xml file
 		Transformer transformer = TransformerFactory.newInstance().newTransformer();
 		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); //indent is automatically set to 0
 		DOMSource source = new DOMSource(doc);
 		StreamResult result = new StreamResult(new File("users.xml"));
 		
 		transformer.transform(source, result);
 	}
 	
 }
