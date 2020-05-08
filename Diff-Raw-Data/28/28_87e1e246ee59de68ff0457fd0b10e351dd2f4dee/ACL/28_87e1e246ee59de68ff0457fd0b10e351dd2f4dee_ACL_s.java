 package security;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import security.acl.ObjectEntity;
 import world.Stadt;
 
 public class ACL extends SecurityCallback {
 
 	private ArrayList<ObjectEntity> instanceList;
	private ArrayList<>
 
 	private static ACL acl;
 
 	private ACL() {
 		super();
 		instanceList = new ArrayList<ObjectEntity>();
 	}
 
 	public static ACL getInstance() {
 		if (acl == null) {
 			acl = new ACL();
 		}
 		return acl;
 	}
 
 	public void parseACLFile() throws ParserConfigurationException,
 			SAXException, IOException {
 		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
 				.newInstance();
 		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 		Document doc = docBuilder.parse(new File("resource/acl.xml"));
 
 		doc.getDocumentElement().normalize();
 
 		Element instances = getFirstNamedElem(doc.getDocumentElement()
 				.getChildNodes(), "instances");
 
 		for (int i = 0; i < instances.getChildNodes().getLength(); i++) {
 			if (instances.getChildNodes().item(i).getNodeType() != Document.ELEMENT_NODE)
 				continue;
 			Element instance = (Element) instances.getChildNodes().item(i);
 			if ("object".equals(instance.getNodeName())) {
 				String clazz = instance.getAttributes().getNamedItem("class")
 						.getTextContent();
 				String id = getFirstNamedElem(instance.getChildNodes(), "id")
 						.getTextContent();
 				ObjectEntity item = new ObjectEntity(clazz, id);
 				for (int j = 0; j < instance.getChildNodes().getLength(); j++) {
 					if (instance.getChildNodes().item(j).getNodeType() == Document.ELEMENT_NODE) {
 						Element _attr = (Element) instance.getChildNodes()
 								.item(j);
 						if ("attr".equals(_attr.getNodeName())) {
 							String attr_name = _attr.getAttributes()
 									.getNamedItem("name").getTextContent();
 							String attr_val = _attr.getTextContent();
 							item.addAttribute(attr_name, attr_val);
 						}
 					}
 				}
 				instanceList.add(item);
 			}
 		}
 	}
 
 	private Element getFirstNamedElem(NodeList lst, String name) {
 		for (int i = 0; i < lst.getLength(); i++) {
 			if (name.equals(lst.item(i).getNodeName())
 					&& lst.item(i).getNodeType() == Document.ELEMENT_NODE) {
 				return (Element) lst.item(i);
 			}
 		}
 		return null;
 	}
 
 	private Map<Object, Permission> map = new HashMap<Object, Permission>();
 
 	public boolean checkPermission(Method m, Object callee, Object caller) {
 		return checkPermission(m.getName(), callee, caller);
 	}
 
 	public boolean checkPermission(String method, Object callee, Object caller) {
 		if (caller instanceof Stadt)
 			return true; // Work-a-round bis ACL infrastruktur steht
 		if (map.get(callee) != null
				&& map.get(callee).getMethods().contains(method))
 			return true;
 		return false;
 	}
 
 	public void addPermission(Object a, Object b, String... methods) {
 		map.put(a, new Permission(b, Arrays.asList(methods)));
 	}
 
 	public void addPermission(SecureInterface a, SecureInterface b,
 			String... methods) {
 		addPermission(a.getObject(this, createTan()),
 				b.getObject(this, createTan()), methods);
 	}
 
 	private class Permission {
 		private Object caller;
 		private List<String> methods;
 
 		private Permission(Object caller, List<String> methods) {
 			this.setCaller(caller);
 			this.methods = methods;
 		}
 
 		public List<String> getMethods() {
 			return methods;
 		}
 
 		public void setMethods(List<String> methods) {
 			this.methods = methods;
 		}
 
 		public Object getCaller() {
 			return caller;
 		}
 
 		public void setCaller(Object caller) {
 			this.caller = caller;
 		}
 	}
 }
