 package reflect.android.api;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class API {
 	private Map<String, Class> m_classes = new HashMap<String, Class>();
 	private static final String API_VERSIONS = "platform-tools" + File.separator + "api" + File.separator + "api-versions.xml";
 
 	public boolean read() throws RuntimeException {
 		final String androidSDK = System.getenv("ANDROID_SDK");
 		if (androidSDK == null)
 		{
 			throw new RuntimeException("Environment variable ANDROID_SDK is missing.");
 		}
 		return read(new File(androidSDK));
 	}
 
 	public boolean read(File android_sdk) {
 		final File api = new File(android_sdk, API_VERSIONS);
 
 		Document doc = null;
 		try {
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			doc = dBuilder.parse(api);
 			doc.getDocumentElement().normalize();
 		} catch(Exception e) {
 			return false;
 		}
 
 		NodeList nodes = doc.getElementsByTagName("class");
 		int len = nodes.getLength();
 		for (int i = 0; i < len; ++i)
 		{
 			final Element node = (Element)nodes.item(i);
 			if (!read(node))
 				return false;
 		}
 		return true;
 	}
 
 	private boolean read(Element clazz) {
 		final String name = clazz.getAttribute("name");
 		final String since = clazz.getAttribute("since");
 		final int iSince;
 		if (since == null) iSince = 1;
 		else iSince = Integer.valueOf(since);
 
 		Class _class = new Class(iSince, name.replace("/", "."));
 
 		NodeList nodes = clazz.getElementsByTagName("method");
 		int len = nodes.getLength();
 		for (int i = 0; i < len; ++i)
 		{
 			final Element node = (Element)nodes.item(i);
 			if (!read(_class, node))
 				return false;
 		}
 
 		add(_class);
 		return true;
 	}
 
 	private boolean read(Class _class, Element method) {
 		final String name = method.getAttribute("name");
 		final String since = method.getAttribute("since");
 		
 		if (name == null) return false;
 		final String[] names = name.split("\\(", 2);
 		if (names == null || names.length != 2)
 			return false;
 
 		final int iSince;
 		if (since == null || since.isEmpty()) iSince = 1;
 		else iSince = Integer.valueOf(since);
 		
 		_class.add(new Method(iSince, names[0], "(" + names[1].replace("/", ".")));
		return false;
 	}
 
 	void add(Class clazz) {
 		m_classes.put(clazz.getName(), clazz);
 	}
 
 	public Class find(String clazz) {
 		if (!m_classes.containsKey(clazz))
 			return null;
 		return m_classes.get(clazz);
 	}
 
 	public Class find(String clazz, int targetAPI) {
 		final Class result = find(clazz);
 		if (result == null) return null;
 		if (result.availableSince() > targetAPI)
 			return null;
 		return result;
 	}
 
 	public String[] getClasses(int targetAPI)
 	{
 		Vector<String> classes = new Vector<String>();
 		for (Map.Entry<String, Class> e: m_classes.entrySet())
 		{
 			if (e.getValue().availableSince() > targetAPI)
 				continue;
 			classes.add(e.getKey());
 		}
 		String [] items = new String[classes.size()];
 		return classes.toArray(items);
 	}
 
 	public Class get(String clazz, int targetAPI) {
 		if (!m_classes.containsKey(clazz))
 			return null;
 		Class klazz = m_classes.get(clazz);
 		if (klazz.availableSince() > targetAPI)
 			return null;
 		return klazz;
 	}
 }
