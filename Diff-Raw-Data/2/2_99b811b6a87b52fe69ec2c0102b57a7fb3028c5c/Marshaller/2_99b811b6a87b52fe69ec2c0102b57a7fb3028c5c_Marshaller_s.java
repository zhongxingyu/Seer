 package org.jcopybook;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 public class Marshaller {
 	private Document copybook;
 	private HashMap<String, Map<String, String>> meta;
 
 	public Marshaller() {
 	}
 
 	public Marshaller(Document layout) {
 		setLayout(layout);
 	}
 
 	public void setLayout(Document copybook) {
 		this.copybook = copybook;
 		meta = new HashMap<String, Map<String, String>>();
 		analyze(copybook, meta);
 	}
 
 	public String process(Document data) {
 		StringBuilder out = new StringBuilder();
 		iterate(data.getElementsByTagName("copybook").item(0), meta, out);
 		return out.toString();
 	}
 
 	private void analyze(Node copybookNode, Map<String, Map<String, String>> meta) {
 		if (copybookNode.getAttributes() != null && copybookNode.getAttributes().getNamedItem("display-length") != null) {
 			Map<String, String> attrs = new HashMap<String, String>();
 			NamedNodeMap attrNodes = copybookNode.getAttributes();
 			for (int i = 0; i < attrNodes.getLength(); i++) {
 				Node attr = attrNodes.item(i);
 				attrs.put(attr.getNodeName(), attr.getNodeValue());
 			}
 			String path = getPathLayout(copybookNode);
 			meta.put(path, attrs);
 		}
 		NodeList childs = copybookNode.getChildNodes();
 		for (int i = 0; i < childs.getLength(); i++) {
 			analyze(childs.item(i), meta);
 		}
 	}
 
 	private void iterate(Node node, Map<String, Map<String, String>> meta, StringBuilder out) {
 		String path = getPath(node);
 		Map<String, String> nodeMeta = meta.get(path);
 		if (nodeMeta != null) {
			if ("true".equals(nodeMeta.containsKey("redefined"))) return;
 			else if (nodeMeta.containsKey("picture")) {
 				String value = node.getFirstChild() == null ? "" : node.getFirstChild().getNodeValue();
 				if (value == null) value = "";
 				value = value.replaceAll("[\\r\\n]+", "");
 				value = handleType(value, nodeMeta);
 				out.append(value);
 				return;
 			}
 		}
 		NodeList childs = node.getChildNodes();
 		for (int i = 0; i < childs.getLength(); i++) {
 			iterate(childs.item(i), meta, out);
 		}
 	}
 
 	public static String handleType(String value, Map<String, String> nodeMeta) {
 		if (nodeMeta != null) {
 			int len = Integer.parseInt(nodeMeta.get("display-length"));
 			if ("true".equals(nodeMeta.get("numeric"))) {
 				if (nodeMeta.containsKey("scale")) {
 					double number = Double.parseDouble(value);
 					int scale = Integer.parseInt(nodeMeta.get("scale"));
 					value = String.format(Locale.ENGLISH, String.format("%%.%df", scale), number);
 					value = value.replaceAll("\\.", "");
 				} else {
 					int number = Integer.parseInt(value);
 					String format = String.format("%%0%dd", len);
 					value = String.format(format, number);
 				}
 				if (value.length() < len) {
 					value = repeat("0", len - value.length()) + value;
 				}
 			} else if (value.length() < len) {
 				value += repeat(" ", len - value.length());
 			}
 			if (value.length() > len) {
 				throw new IllegalStateException(
 						String.format("value '%s' have length more than type: %d instead of %d", nodeMeta.get("name"),
 								value.length(), len));
 			}
 		}
 		return value;
 	}
 
 	public static String repeat(String symb, int num) {
 		StringBuilder buf = new StringBuilder();
 		for (int i = 0; i < num; i++) {
 			buf.append(symb);
 		}
 		return buf.toString();
 	}
 
 	private String getPathLayout(Node node) {
 		if (node.getNodeName().equals("copybook")) return "";
 		StringBuilder path = new StringBuilder();
 		if (node.getParentNode().getNodeName().equals("item"))
 			path.append(node.getParentNode().getAttributes().getNamedItem("name").getNodeValue());
 		path.append("/");
 		path.append(node.getAttributes().getNamedItem("name").getNodeValue());
 		return path.toString();
 	}
 
 	private String getPath(Node node) {
 		if (node.getNodeName().equals("copybook")) return "";
 		StringBuilder path = new StringBuilder();
 		path.append(node.getParentNode().getNodeName());
 		path.append("/");
 		path.append(node.getNodeName());
 		return path.toString();
 	}
 }
