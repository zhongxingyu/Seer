 package com.grademaster.data.xml;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.grademaster.Globals;
 import com.grademaster.data.IDataObject;
 import com.grademaster.data.objects.ClassDataBase;
 import com.grademaster.data.objects.ClassSection;
 import com.grademaster.data.objects.MyClass;
 import com.grademaster.data.objects.User;
 import com.grademaster.data.objects.UserDataBase;
 import com.grademaster.logging.ErrorLevel;
 
 public class XMLClassDataBaseAdapter implements IXMLAdapter {
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public IDataObject dataToObject(Object o) {
 		Globals.getLogger().log("Converting XML data to ClassDataBase object...");
 
 		ClassDataBase base = new ClassDataBase();
 
 		Document doc = (Document) o;
 
 		NodeList classes = doc.getElementsByTagName("class");
 
 		for (int i=0;i<classes.getLength();i++) {
 			HashMap<String,Object> map = new HashMap<String,Object>();
 
 			Node classNode = classes.item(i);
 			for (int u=0;u<classNode.getChildNodes().getLength();u++) {
 				Node n = classNode.getChildNodes().item(u);
 				if (!n.getNodeName().equals("sections")) {
 					//Used for debugging - Globals.getLogger().log("Processing - " + n.getNodeName()+": " + n.getTextContent());
 					map.put(n.getNodeName(), n.getTextContent());
 				} else {
 					for (int u2=0;u2<n.getChildNodes().getLength();u2++) {
 						HashMap<String,String> map2 = new HashMap<String,String>();
 						Node n2=n.getChildNodes().item(u2);
 						for (int u3 = 0; u3<n2.getChildNodes().getLength();u3++) {
 							map2.put(n2.getChildNodes().item(u3).getNodeName(), n2.getChildNodes().item(u3).getTextContent());
 							//Used for debugging - Globals.getLogger().log(n2.getChildNodes().item(u3).getNodeName()+n2.getChildNodes().item(u3).getTextContent());
 						}
 
 						ArrayList<ClassSection> sections;
 
 						if (map.get("sections")!=null) {
 							sections = (ArrayList<ClassSection>) map.get("sections");
 						} else {
 							sections = new ArrayList<ClassSection>();
 						}
						sections.add(new ClassSection(map2.get("sid"), map2.get("name"), (String) map2.get("cid"), map2.get("value"), map2.get("desc")));
 						map.put("sections", sections);
 					}
 				}
 			}
 			MyClass iClass = new MyClass((String) map.get("cid"),(String) map.get("uid"),(String) map.get("name"),(String) map.get("loc"),(String) map.get("desc"),(ArrayList<ClassSection>) map.get("sections"));
 			base.addObject(iClass);
 		}
 
 		return base;
 	}
 
 	@Override
 	public String objectToData(Object o) throws ParserConfigurationException,
 	Exception {
 		Globals.getLogger().log("Converting ClassDataBase object to String (should be XML)...");
 		Globals.getLogger().log("UserDataBase to data not ready yet!", ErrorLevel.WARNING);
 		return null;
 		/* Might use later
 		//Cast object to config
 		UserDataBase users = (UserDataBase) o;
 
 		//Initialize DOM
 		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
 		Document doc = docBuilder.newDocument();
 		Element rootElement = doc.createElement("users");
 		doc.appendChild(rootElement);
 
 		//Create config element
 		Element xml = rootElement;
 
 		for (Object user1 : users.getObjects()) {	
 			User user= (User) user1;
 
 			Element u = doc.createElement("user");
 			xml.appendChild(u);
 
 			//Start creating data nodes
 			Element uname = doc.createElement("uname");
 			uname.appendChild(doc.createTextNode(user.getUsername()));
 			u.appendChild(uname);
 
 			Element pword = doc.createElement("pword");
 			pword.appendChild(doc.createTextNode(user.getPassword()));
 			u.appendChild(pword);
 
 			Element fname = doc.createElement("fname");
 			fname.appendChild(doc.createTextNode(user.getFname()));
 			u.appendChild(fname);
 
 			Element lname = doc.createElement("lname");
 			lname.appendChild(doc.createTextNode(user.getLname()));
 			u.appendChild(lname);
 
 			Element type = doc.createElement("type");
 			type.appendChild(doc.createTextNode(user.getUserType()));
 			u.appendChild(type);
 
 			Element uid = doc.createElement("uid");
 			uid.appendChild(doc.createTextNode(user.getUid()));
 			u.appendChild(uid);
 
 			Element showWelcome = doc.createElement("showWelcome");
 			showWelcome.appendChild(doc.createTextNode(Boolean.toString(user.isShowWelcomeMessage())));
 			u.appendChild(showWelcome);
 		}
 
 		TransformerFactory transFactory = TransformerFactory.newInstance();
 		Transformer transformer = transFactory.newTransformer();
 		StringWriter buffer = new StringWriter();
 		transformer.transform(new DOMSource(doc),
 		      new StreamResult(buffer));
 		String str = buffer.toString().replace(">", ">\n").replace("</", "\n</");
 
 		return str;*/
 	}
 
 }
