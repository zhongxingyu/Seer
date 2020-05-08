 /**
  * IT-Grundschutz-Katalog to XML-Parser
  * Copyright (C) 2012, Lars Meyer
  */
 
 package de.ipm.gsk;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Entities.EscapeMode;
 import org.jsoup.select.Elements;
 
 public class WebParser {
 
 	private static String MODULE_ID = "_content/baust/";
 	private static String THREAT_ID = "_content/g/";
 	private static String MEASURE_ID = "_content/m/";
 
 	public static void parse(Document xmlDoc) {
 		try {
 			Document doc = Jsoup
 					.connect(
 							"https://www.bsi.bund.de/DE/Themen/ITGrundschutz/ITGrundschutzKataloge/itgrundschutzkataloge_node.html")
 					.get();
 			xmlDoc.createElement("<?xml version='1.0' encoding='UTF-8'?>");
 			Element root = xmlDoc.appendElement("gsk");
 			root.attr("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.attr("xsi:noNamespaceSchemaLocation","gsk.xsd");
 			xmlDoc.appendChild(root);
 			Element modules = root.appendElement("modulesCatalogue");
 			Elements modulesPages = doc.select("a[href*=" + MODULE_ID + "]");
 			for (Element e : modulesPages) {
 				parseModules(modules, e);
 			}
 			Element threats = root.appendElement("threatsCatalogue");
 			Elements threatsPages = doc.select("a[href*=" + THREAT_ID + "]");
 			for (Element e : threatsPages) {
 				parseThreats(threats, e);
 			}
 
 			Element measures = root.appendElement("measuresCatalogue");
 			Elements measuresPages = doc.select("a[href*="+MEASURE_ID+"]");
 			 for( Element e : measuresPages) {
 				 parseMeasures(measures, e);
 			 }
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void parseModules(Element parent,
 			Element module) {
 		try {
 			Document doc = Jsoup.connect(module.absUrl("href")).timeout(0)
 					.get();
 			Element modulesCatalogue = parent.appendElement("modules");
 			modulesCatalogue.attr("title", doc
 					.select("h1[class=isFirstInSlot]").first().text());
 			System.out.println(doc.select("h1[class=isFirstInSlot]").first()
 					.text());
 			Elements modules = doc.select("p a[href*=" + MODULE_ID + "]");
 			for (Element m : modules) {
 				parseModule(modulesCatalogue, m);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void parseModule(Element parent, Element module)
 			throws IOException {
 		Document doc = Jsoup.connect(module.absUrl("href")).timeout(0).get();
 		Elements children = doc.getElementById("content").children();
 
 		Document xmlDoc = parent.ownerDocument();
 		Element xmlModule = xmlDoc.createElement("module");
 		Elements version = doc.getElementsContainingOwnText("Stand: ");
 		if (version.size() > 0) {
 			String[] splitted = version.last().text().split(":");
 			xmlModule.attr("version", splitted[splitted.length-1]);
 		}
 
 		Element currentSubModule = null;
 		Boolean moduleBool = false;
 		Element moduleDescription = xmlDoc.createElement("moduleDescription");
 		Element moduleLinks = xmlDoc.createElement("moduleLinks");
 		Boolean threatBool = false;
 		Element threatDescription = xmlDoc.createElement("threatDescription");
 		Element threatLinks = xmlDoc.createElement("threatLinks");
 		Element threatLinksAdd = xmlDoc.createElement("threatLinksAdd");
 		Boolean measureBool = false;
 		Element measureDescription = xmlDoc.createElement("measureDescription");
 		Element measureLinks = xmlDoc.createElement("measureLinks");
 		Element measureLinksAdd = xmlDoc.createElement("measureLinksAdd");
 		for (Element c : children) {
 			if ((currentSubModule != null) && c.tagName().equals("p")) {
 				parseForLinks(c, "B [1-5].\\d{1,3}", "a[href*=" + MODULE_ID
 						+ "]", "moduleLink", moduleLinks);
 				parseForLinks(c, "G [1-5].\\d{1,3}", "a[href*=" + THREAT_ID
 						+ "]", "threatLink", threatLinksAdd);
 				parseForLinks(c, "M [1-6].\\d{1,3}", "a[href*=" + MEASURE_ID
 						+ "]", "measureLink", measureLinksAdd);
 			}
 			if (c.attr("class").equals("isFirstInSlot")) {
 				System.out.println(c.text());
 				xmlModule.attr("title", c.text());
 			} else if (c.tagName().equals("h2")
 					&& c.text().equals("Beschreibung")) {
 				currentSubModule = moduleDescription;
 				moduleBool = true;
 			} else if (c.tagName().equals("h2")
 					&& c.text().equals("Gefährdungslage")) {
 				currentSubModule = threatDescription;
 				moduleBool = false;
 				threatBool = true;
 			} else if (c.tagName().equals("h2")
 					&& c.text().equals("Maßnahmenempfehlungen")) {
 				currentSubModule = measureDescription;
 				threatBool = false;
 				measureBool = true;
 			} else if ((moduleBool || threatBool || measureBool)
 					&& c.tagName() == "p") {
 				appendParsedElement(currentSubModule, c, "paragraph");
 			} else if ((moduleBool || threatBool || measureBool)
 					&& c.tagName() == "h3") {
 				appendParsedElement(currentSubModule, c, "headline");
 			} else if ((moduleBool || threatBool || measureBool)
 					&& c.tagName() == "ul") {
 				Element listElement = currentSubModule.appendElement("list");
 				for (Element listItem : c.select("li")) {
 					appendParsedElement(listElement, listItem, "listitem");
 				}
 				currentSubModule.appendChild(listElement);
 			} else if (c.tagName() == "table") {
 				Element last = currentSubModule.children().last();
 				if (last.tagName().equals("headline")) {
 					last.remove();
 				}
 				moduleBool = false;
 				threatBool = false;
 				measureBool = false;
 				parseForLinks(c, "G [1-5].\\d{1,3}", "a[href*=" + THREAT_ID
 						+ "]", "threatLink", threatLinks);
 				parseForMeasureLinks(c, "M [1-6].\\d{1,3}", "a[href*=" + MEASURE_ID
 						+ "]", "measureLink", measureLinks);
 			}
 		}
 
 		removeClones(threatLinks, threatLinksAdd);
 		removeClones(measureLinks, measureLinksAdd);
 
 		xmlModule.appendChild(moduleDescription);
 		if (moduleLinks.children().size() > 0) {
 			xmlModule.appendChild(moduleLinks);
 		}
 		xmlModule.appendChild(threatDescription);
 		xmlModule.appendChild(threatLinks);
 		if (threatLinksAdd.children().size() > 0) {
 			xmlModule.appendChild(threatLinksAdd);
 		}
 		xmlModule.appendChild(measureDescription);
 		xmlModule.appendChild(measureLinks);
 		if (measureLinksAdd.children().size() > 0) {
 			xmlModule.appendChild(measureLinksAdd);
 		}
 		parent.appendChild(xmlModule);
 	}
 
 	public static void parseThreats(Element parent, Element threatCatalogue) {
 		try {
 			Document doc = Jsoup.connect(threatCatalogue.absUrl("href"))
 					.timeout(0).get();
 			Element threatsElement = parent.appendElement("threats");
 			threatsElement.attr("title", doc
 					.select("h1[class=isFirstInSlot]").first().text());
 			System.out.println(doc.select("h1[class=isFirstInSlot]").first()
 					.text());
 			Elements threats = doc.select("p a[href*=" + THREAT_ID + "]");
 			for (Element t : threats) {
 				parseThreat(threatsElement, t);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private static void parseThreat(Element parent, Element threat)
 			throws IOException {
 		Document doc = Jsoup.connect(threat.absUrl("href")).timeout(0).get();
 		Elements children = doc.getElementById("content").children();
 		Element xmlModule = parent.appendElement("threat");
 
 		Element currentSubModule = null;
 		Element moduleLinks = xmlModule.ownerDocument().createElement(
 				"moduleLinks");
 		Element threatLinks = xmlModule.ownerDocument().createElement(
 				"threatLinks");
 		Element measureLinks = xmlModule.ownerDocument().createElement(
 				"measureLinks");
 		for (Element c : children) {
 			if ((currentSubModule != null) && c.tagName().equals("p")) {
 				parseForLinks(c, "B [1-5].\\d{1,3}", "a[href*=" + MODULE_ID
 						+ "]", "moduleLink", moduleLinks);
 				parseForLinks(c, "G [1-5].\\d{1,3}", "a[href*=" + THREAT_ID
 						+ "]", "threatLink", threatLinks);
 				parseForLinks(c, "M [1-6].\\d{1,3}", "a[href*=" + MEASURE_ID
 						+ "]", "measureLink", measureLinks);
 			}
 			if (c.attr("class").equals("isFirstInSlot")) {
 				System.out.println(c.text());
 				xmlModule.attr("title", c.text());
 				currentSubModule = xmlModule;
 			} else if ((currentSubModule != null) && c.tagName() == "p") {
 				if(c.text().contains("Stand:")){
 					String[] splitted = c.text().split(":");
 					xmlModule.attr("version", splitted[splitted.length-1]);
 				} else {
 					appendParsedElement(currentSubModule, c, "paragraph");
 				}				
 			} else if ((currentSubModule != null) && c.tagName() == "h3") {
 				appendParsedElement(currentSubModule, c, "headline");
 			} else if ((currentSubModule != null) && c.tagName() == "ul") {
 				Element listElement = currentSubModule.appendElement("list");
 				for (Element listItem : c.select("li")) {
 					appendParsedElement(listElement, listItem, "listitem");
 				}
 				currentSubModule.appendChild(listElement);
 			}
 		}
 		if (moduleLinks.children().size() > 0) {
 			xmlModule.appendChild(moduleLinks);
 		}
 		if (threatLinks.children().size() > 0) {
 			xmlModule.appendChild(threatLinks);
 		}
 		if (measureLinks.children().size() > 0) {
 			xmlModule.appendChild(measureLinks);
 		}
 	}
 
 	public static void parseMeasures(Element parent, Element measureCatalogue) {
 		try {
 			Document doc = Jsoup.connect(measureCatalogue.absUrl("href"))
 					.timeout(0).get();
 			Element measureElement = parent.appendElement("measures");
 			measureElement.attr("title", doc
 					.select("h1[class=isFirstInSlot]").first().text());
 			System.out.println(doc.select("h1[class=isFirstInSlot]").first()
 					.text());
 			Elements measures = doc.select("p a[href*=" + MEASURE_ID + "]");
 			for (Element m : measures) {
 				parseMeasure(measureElement, m);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static void parseMeasure(Element parent, Element measure)
 			throws IOException {
 		Document doc = Jsoup.connect(measure.absUrl("href")).timeout(0).get();
 		Elements children = doc.getElementById("content").children();
 		Element xmlModule = parent.appendElement("measure");
 
 		Element currentSubModule = null;
 		Element moduleLinks = xmlModule.ownerDocument().createElement(
 				"moduleLinks");
 		Element threatLinks = xmlModule.ownerDocument().createElement(
 				"threatLinks");
 		Element measureLinks = xmlModule.ownerDocument().createElement(
 				"measureLinks");
 		for (Element c : children) {
 			if ((currentSubModule != null) && c.tagName().equals("p")) {
 				parseForLinks(c, "B [1-5].\\d{1,3}", "a[href*=" + MODULE_ID
 						+ "]", "moduleLink", moduleLinks);
 				parseForLinks(c, "G [1-5].\\d{1,3}", "a[href*=" + THREAT_ID
 						+ "]", "threatLink", threatLinks);
 				parseForLinks(c, "M [1-6].\\d{1,3}", "a[href*=" + MEASURE_ID
 						+ "]", "measureLink", measureLinks);
 			}
 			if (c.attr("class").equals("isFirstInSlot")) {
 				System.out.println(c.text());
 				xmlModule.attr("title", c.text());
 				currentSubModule = xmlModule;
 			} else if ((currentSubModule != null) && c.tagName() == "p") {
 				if(c.text().contains("Stand:")){
 					String[] splitted = c.text().split(":");
 					xmlModule.attr("version", splitted[splitted.length-1]);
 				} else if(c.text().contains("Verantwortlich für Initiierung:")){
 					String[] splitted = c.text().split(":");
 					xmlModule.attr("initiator", splitted[splitted.length-1]);
 				} else if(c.text().contains("Verantwortlich für Umsetzung:")){
 					String[] splitted = c.text().split(":");
 					xmlModule.attr("implementation", splitted[splitted.length-1]);
 				} else {
 					appendParsedElement(currentSubModule, c, "paragraph");					
 				}
 			} else if ((currentSubModule != null) && c.tagName() == "h3") {
 				appendParsedElement(currentSubModule, c, "headline");
 			} else if ((currentSubModule != null) && c.tagName() == "ul") {
 				Element listElement = currentSubModule.appendElement("list");
 				for (Element listItem : c.select("li")) {
 					appendParsedElement(listElement, listItem, "listitem");
 				}
 				currentSubModule.appendChild(listElement);
 			}
 		}
 		if (moduleLinks.children().size() > 0) {
 			xmlModule.appendChild(moduleLinks);
 		}
 		if (threatLinks.children().size() > 0) {
 			xmlModule.appendChild(threatLinks);
 		}
 		if (measureLinks.children().size() > 0) {
 			xmlModule.appendChild(measureLinks);
 		}
 	}
 
 	public static void main(String[] args) {
 		Document xmlDoc = new Document("gsk");
 		xmlDoc.outputSettings().escapeMode(EscapeMode.xhtml);
 		parse(xmlDoc);
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter("gsk.xml"));
 			out.write(xmlDoc.html());
 			out.close();
 			System.out.println("-- Fertig --");
 		} catch (IOException e) {
 		}
 	}
 	private static void parseForMeasureLinks(Element searchElement, String regexp,
 			String linkId, String linkTag, Element linkList) {
 		Elements parsedLinks = searchElement
 				.getElementsByAttributeValueMatching("title", regexp).select(
 						linkId);
 		for (Element e : parsedLinks) {
 			Element link = searchElement.ownerDocument().createElement(linkTag);
 			link.attr("title", e.attr("title"));
 			link.attr("category", e.parent().nextElementSibling().text());
 			linkList.appendChild(link);
 		}
 	}
 	private static void parseForLinks(Element searchElement, String regexp,
 			String linkId, String linkTag, Element linkList) {
 		Elements parsedLinks = searchElement
 				.getElementsByAttributeValueMatching("title", regexp).select(
 						linkId);
 		for (Element e : parsedLinks) {
 			Element link = searchElement.ownerDocument().createElement(linkTag);
 			link.attr("title", e.attr("title"));
 			linkList.appendChild(link);
 		}
 	}
 
 	private static void removeClones(Element tableList, Element textParseList) {
 		for (Element l : tableList.children()) {
 			for (Element add : textParseList.children()) {
 				if (l.attr("title").equals(add.attr("title"))) {
 					add.remove();
 				}
 			}
 		}
 	}
 
 	private static void appendParsedElement(Element currentXMLElement,
 			Element parseElement, String tag) {
 		Element listElement = currentXMLElement.appendElement(tag);
 		listElement.append(parseElement.text().replace("<", "").replace(">", ""));
 	}
 }
