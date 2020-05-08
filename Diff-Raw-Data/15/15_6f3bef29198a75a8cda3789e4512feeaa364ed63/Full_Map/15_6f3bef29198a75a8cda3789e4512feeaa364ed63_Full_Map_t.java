 package univ_angers.virtuose.utils;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.StringReader;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Queue;
 
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 /** Met en valeur un chemin dans la carte entière */
 public class Full_Map {
 	private static Logger log = Logger.getLogger(Full_Map.class);
 	private String hightLightedMap;
 	private Document full_map_doc;
 
 	public Full_Map(String full_map, String map) {
 		SAXBuilder sxb = new SAXBuilder();
 		try {
 			Document document_map = sxb.build(new StringReader(map));
 			Document document_full_map = sxb.build(new StringReader(full_map));
 
 			List<String> listId = getListId(document_map);
 			List<String> listId_result = getListIdResult(document_map);
 
 			log.debug("listId: " + listId.toString());
 			log.debug("listId_result: " + listId_result.toString());
 			
 			highlightId(document_full_map, listId, listId_result);
 			full_map_doc = document_full_map;
 			hightLightedMap = docToString(document_full_map);
 
 		} catch (Exception e) {
 			log.error(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	public String getFullMapPath() {
 		String path = "src/main/webapp/tmp/full_map.mm";
 		save_xml(full_map_doc, path);
 		return "/tmp/full_map.mm";
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
 
 	private static String docToString(Document doc) {
 		StringBuffer sresult = new StringBuffer(
 				new XMLOutputter().outputString(doc));
 		int strind = sresult.toString().indexOf('\n');
 		if (strind != -1) {
 			sresult.delete(0, strind);
 		}
 		return sresult.toString();
 	}
 
 	public String getHightLighedMap() {
 		return hightLightedMap;
 	}
 
 	private void highlightId(Document doc, List<String> listId, List<String> listId_result) {
 		Element node = doc.getRootElement();
 
 		List<Element> lNode = new ArrayList<Element>();
 		lNode.add(node);
 
 		hightLight(node, listId, listId_result);
 		Queue<Element> stack = new ArrayDeque<Element>();
 
 		Iterator<Element> i = node.getChildren().iterator();
 		while (i.hasNext()) {
 			stack.add(i.next());
 		}
 
 		while (stack.peek() != null) {
 			Element e = stack.poll();
 
 			hightLight(e, listId, listId_result);
 			Iterator<Element> e_i = e.getChildren().iterator();
 			while (e_i.hasNext()) {
 				Element eChild = (Element) e_i.next();
 				if (!lNode.contains(eChild)) {
 					lNode.add(eChild);
 					stack.add(eChild);
 				}
 			}
 		}
 	}
 
 	private void hightLight(Element node, List<String> listId, List<String> listId_result) {
 		String id = node.getAttributeValue("ID");
 		log.debug("id_node: " + id);
 		if (listId_result.contains(id)) {
 			log.debug("MATCH!!!!!!!!!");
			node.setAttribute("BACKGROUND_COLOR", "#8edcff");	
 		} else if(listId.contains(id)) {
 			node.setAttribute("BACKGROUND_COLOR", "#bada55");
 		}
 
 	}
 
 	public void addId(Element node, List<String> listId) {
 		String id = node.getAttributeValue("ID");
 		listId.add(id);
 	}
 
 	public void addIdResult(Element node, List<String> listId) {
 		String id = node.getAttributeValue("ID");
 		if(node.getAttribute("class") != null) {
 			listId.add(id);		
 		}
 	}
 
 	
 	/** Retourne la liste des Id contenus dans le doc */
 	public List<String> getListId(Document doc) {
 		Element node = doc.getRootElement();
 		List<String> listId = new ArrayList<String>();
 
 		List<Element> lNode = new ArrayList<Element>();
 		lNode.add(node);
 
 		addId(node, listId);
 		Queue<Element> stack = new ArrayDeque<Element>();
 
 		Iterator<Element> i = node.getChildren().iterator();
 		while (i.hasNext()) {
 			stack.add(i.next());
 		}
 
 		while (stack.peek() != null) {
 			Element e = stack.poll();
 
 			addId(e, listId);
 			Iterator<Element> e_i = e.getChildren().iterator();
 			while (e_i.hasNext()) {
 				Element eChild = (Element) e_i.next();
 				if (!lNode.contains(eChild)) {
 					lNode.add(eChild);
 					stack.add(eChild);
 				}
 			}
 		}
 
 		return listId;
 	}
 	
 	
 	/** Retourne la liste des Id contenus dans le doc et considéré comme résultat */
 	public List<String> getListIdResult(Document doc) {
 		Element node = doc.getRootElement();
 		List<String> listId = new ArrayList<String>();
 
 		List<Element> lNode = new ArrayList<Element>();
 		lNode.add(node);
 
 		addIdResult(node, listId);
 		Queue<Element> stack = new ArrayDeque<Element>();
 
 		Iterator<Element> i = node.getChildren().iterator();
 		while (i.hasNext()) {
 			stack.add(i.next());
 		}
 
 		while (stack.peek() != null) {
 			Element e = stack.poll();
 
 			addIdResult(e, listId);
 			Iterator<Element> e_i = e.getChildren().iterator();
 			while (e_i.hasNext()) {
 				Element eChild = (Element) e_i.next();
 				if (!lNode.contains(eChild)) {
 					lNode.add(eChild);
 					stack.add(eChild);
 				}
 			}
 		}
 
 		return listId;
 	}
 
 }
