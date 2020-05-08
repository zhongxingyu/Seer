 package univ_angers.virtuose.utils;
 
 import java.io.*;
 
 import org.apache.log4j.Logger;
 import org.jdom.*;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Iterator;
 import java.util.Queue;
 import java.util.Stack;
 
 public class XmlToHtml {
 	private static Logger log = Logger.getLogger(Writer.class);
 
 	static Document document;
 	static Element racine;
 	static String htmlPath = "/tmp/page.html";
 	static public String result = "";
 	static public String title = "";
 	static int depth = 0;
 
 	public static void main(String[] args) {
 		start("<node TEXT=\"AAA\" class=\"\"></node>");
 		log.info("title: "+title);
 	}
 
 	public static void start(String xmlInput) {
 		SAXBuilder sxb = new SAXBuilder();
 		try {
 			document = sxb.build(new StringReader(xmlInput));
 			racine = document.getRootElement();
 			
 			// On ignore la racine car Extract rend une racine au contenu vide pour le moment.
 			if(racine.getChildren().size() == 1) {
				racine = (Element)racine.getChildren().get(0);
 			}
 			
 			convert2(document, racine);
 			log.debug("convert2" + result);
 
 			document = sxb.build(new StringReader(result));
 			racine = document.getRootElement();
 			convert3(document, racine);
 			log.debug("convert3" + result);
 		} catch (Exception e) {
 			log.error(e.getMessage());
 		}
 	}
 	
 	static void convert2(Document doc, Element node) {
 		List<Element> lNode = new ArrayList<Element>();
 		lNode.add(node);
 		
 		elemToHtml(node);
 		Queue<Element> stack = new ArrayDeque<Element>();
 
 		Iterator<Element> i = node.getChildren().iterator();
 		while (i.hasNext()) {
 			stack.add(i.next());
 		}
 
 		while (stack.peek() != null) {
 			Element e = stack.poll();
 			
 			elemToHtml(e);
 			Iterator<Element> e_i = e.getChildren().iterator();
 			while (e_i.hasNext()) {
 				Element eChild = (Element) e_i.next();
 				if (!lNode.contains(eChild)) {
 					lNode.add(eChild);
 					stack.add(eChild);
 				}
 			}
 		}
 
		result = new XMLOutputter().outputString(doc);
 	}
 
 	static void convert3(Document doc, Element node) {
 		List<Element> lNode = new ArrayList<Element>();
 		lNode.add(node);
 		elemToHtml2(node);
 
 		Queue<Element> stack = new ArrayDeque<Element>();
 
 		Iterator<Element> i = node.getChildren().iterator();
 		while (i.hasNext()) {
 			stack.add(i.next());
 		}
 
 		while (stack.peek() != null) {
 			Element e = stack.poll();
 			elemToHtml2(e);
 			Iterator<Element> e_i = e.getChildren().iterator();
 			while (e_i.hasNext()) {
 				Element eChild = (Element) e_i.next();
 				if (!lNode.contains(eChild)) {
 					lNode.add(eChild);
 					stack.add(eChild);
 				}
 			}
 		}
 		
		result = new XMLOutputter().outputString(doc);
 	}
 
 	static void elemToHtml(Element e) {
 		if (e.getName().equals("node")) {			
 			Element parent = e.getParentElement();
 			if (parent != null && !parent.getName().equals("ul")) {
 				List<Element> content = parent.removeContent();
 				Element ul = new Element("ul");
 				parent.addContent(ul);
 				ul.addContent(content);
 			}
 
 			//String text = e.getAttributeValue("TEXT");
 			//log.debug("elemToHtml: " + text);
 			e.setName("li");
 		}
 	}
 
 	static void elemToHtml2(Element e) {
 		if (e.getName().equals("li")) {
 			String text = e.getAttributeValue("TEXT");
 			//log.debug("elemToHtml: " + text);
 			Element a = new Element("a");
 			if (text.length() < 2) {
 				text = "default";
 			}
 			
 			if (e.getAttributeValue("class") != null) {
 				title = e.getAttributeValue("TEXT");
 				log.debug("-----------------------------------------"+title);
 				a.setAttribute("class", "result");
 				e.removeAttribute("class");
 			}
 			
 			a.setText(text);
 			e.addContent(0, a);
 			e.removeAttribute("TEXT");
 			e.removeAttribute("PROFONDEUR");
 			e.removeAttribute("ID");
 			e.removeAttribute("ID_PARENT");
 			e.setName("li");
 		}
 	}
 
 	static void save_xml(Document doc, String filePath) {
 		try {
 			XMLOutputter sortie = new XMLOutputter(Format.getPrettyFormat());
 			sortie.output(doc, new FileOutputStream(filePath));
 			log.info("html page saved at: " + filePath);
 		} catch (java.io.IOException e) {
 			log.error(e.getMessage());
 		}
 	}
 }
