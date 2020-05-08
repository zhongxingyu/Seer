 package com.personalityextractor.data.source;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import com.personalityextractor.entity.WikipediaEntity;
 
 import tathya.db.YahooBOSS;
 import cs224n.util.Counter;
 import cs224n.util.CounterMap;
 import cs224n.util.PriorityQueue;
 
 public class Wikiminer {
 
 	static HashMap<String, String> cache = new HashMap<String, String>();
 	static HashMap<String, HashMap<String, WikipediaEntity>> cacheRelativeBestSenses = new HashMap<String, HashMap<String, WikipediaEntity>>();
 
 	// caution ahead: mix up the order of the args at your own peril
 	public static double calculateJaccard(List<String> links,
 			List<String> contextPhrases) {
 		StringBuffer arr1String = new StringBuffer();
 		HashSet<String> union = new HashSet<String>();
 		HashSet<String> cphrases = new HashSet<String>();
 
 		for (String s : contextPhrases) {
 			cphrases.add(s);
 		}
 
 		double overlap = 0.0;
 		for (String s : links) {
 			arr1String.append(s + " ");
 			union.add(s);
 		}
 		// System.out.println(arr1String);
 		String arr1Concat = arr1String.toString().trim();
 		for (String s : cphrases) {
 			if (arr1Concat.contains(s)) {
 				overlap++;
 			}
 			union.add(s);
 		}
 
 		return (overlap / union.size());
 	}
 
 	public static double compareArticlesWithJaccard(String id1, String id2) {
 		double overlap = 0.0;
 		ArrayList<String> links1 = getLinks(id1);
 		ArrayList<String> links2 = getLinks(id2);
 		HashSet<String> union = new HashSet<String>();
 		for (String s : links1) {
 			union.add(s);
 			if (links2.contains(s)) {
 				overlap++;
 			}
 		}
 		for (String s : links2) {
 			union.add(s);
 		}
 		return (overlap / union.size());
 	}
 
 	public static ArrayList<String> getLinks(String wikiminer_id) {
 
 		String xml = getXML(wikiminer_id, true);
 		ArrayList<String> links = new ArrayList<String>();
 		DocumentBuilder db = null;
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			db = dbf.newDocumentBuilder();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		InputSource is = new InputSource();
 		is.setCharacterStream(new StringReader(xml));
 		try {
 			Document dom = db.parse(is);
 			NodeList outNodes = dom.getElementsByTagName("LinkOut");
 			if (outNodes != null && outNodes.getLength() != 0) {
 				for (int i = 0; i < outNodes.getLength(); i++) {
 					Node link = outNodes.item(i);
 					if (link != null) {
 						NamedNodeMap attrs = link.getAttributes();
 						Node commonness = attrs.getNamedItem("relatedness");
 						double relevance = Double.parseDouble(commonness
 								.getTextContent());
 						if (relevance >= 0.1) {
 							links
 									.add(attrs.getNamedItem("id")
 											.getTextContent());
 						}
 					}
 				}
 			}
 
 			NodeList inNodes = dom.getElementsByTagName("LinkIn");
 			if (inNodes != null && inNodes.getLength() != 0) {
 				for (int i = 0; i < inNodes.getLength(); i++) {
 					Node link = inNodes.item(i);
 					if (link != null) {
 						NamedNodeMap attrs = link.getAttributes();
 						Node commonness = attrs.getNamedItem("relatedness");
 						double relevance = Double.parseDouble(commonness
 								.getTextContent());
 						if (relevance >= 0.1) {
 							links
 									.add(attrs.getNamedItem("id")
 											.getTextContent());
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return links;
 	}
 
 	public static String getexploreArticleXML(String wikiminer_id) {
 		try {
 			// String urlStr =
 			// "http://wdm.cs.waikato.ac.nz:8080/service?task=search&xml";
 			String urlStr = "http://50.19.209.97:8080/wikipediaminer/services/exploreArticle?parentCategories=true";
 			urlStr += "&id=" + wikiminer_id;
 
 			// return from cache
 			if (cache.containsKey(urlStr)) {
 				return cache.get(urlStr);
 			}
 
 			URL url = new URL(urlStr);
 			URLConnection yc = url.openConnection();
 			BufferedReader in = new BufferedReader(new InputStreamReader(yc
 					.getInputStream()));
 			String inputLine;
 
 			StringBuffer buf = new StringBuffer();
 			while ((inputLine = in.readLine()) != null)
 				buf.append(inputLine);
 			in.close();
 
 			String xml = buf.toString();
 
 			if (!xml.contains("ParentCategory")) {
 				return null;
 			}
 
 			cache.put(urlStr, xml);
 			return xml;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return null;
 
 	}
 	
 	public static String getexploreCategoryXML(String wikiminer_id) {
 		try {
 			// String urlStr =
 			// "http://wdm.cs.waikato.ac.nz:8080/service?task=search&xml";
 			String urlStr = "http://50.19.209.97:8080/wikipediaminer/services/exploreCategory?parentCategories=true";
 			urlStr += "&id=" + wikiminer_id;
 
 			// return from cache
 			if (cache.containsKey(urlStr)) {
 				return cache.get(urlStr);
 			}
 
 			URL url = new URL(urlStr);
 			URLConnection yc = url.openConnection();
 			BufferedReader in = new BufferedReader(new InputStreamReader(yc
 					.getInputStream()));
 			String inputLine;
 
 			StringBuffer buf = new StringBuffer();
 			while ((inputLine = in.readLine()) != null)
 				buf.append(inputLine);
 			in.close();
 
 			String xml = buf.toString();
 
 			if (!xml.contains("ParentCategory")) {
 				return null;
 			}
 
 			cache.put(urlStr, xml);
 			return xml;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return null;
 
 	}
 
 	public static ArrayList<WikipediaEntity> getCategories(String wikiminer_id) {
 		String xml = getexploreArticleXML(wikiminer_id);
 		ArrayList<WikipediaEntity> categories = new ArrayList<WikipediaEntity>();
 		if(xml == null) {
 			return categories;
 		}
 		DocumentBuilder db = null;
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			db = dbf.newDocumentBuilder();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		InputSource is = new InputSource();
 		is.setCharacterStream(new StringReader(xml));
 		try {
 			Document dom = db.parse(is);
 			NodeList catNodes = dom.getElementsByTagName("ParentCategory");
 			if (catNodes != null && catNodes.getLength() != 0) {
 				for (int i = 0; i < catNodes.getLength(); i++) {
 					Node cat = catNodes.item(i);
 					if (cat != null) {
 						NamedNodeMap attrs = cat.getAttributes();
 						String[] values = new String[2];
 						values[0] = attrs.getNamedItem("id").getTextContent();
 						values[1] = attrs.getNamedItem("title").getTextContent();
 						categories.add(new WikipediaEntity(values[1],values[0], 1));
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return categories;
 	}
 	
 	public static ArrayList<WikipediaEntity> getParentCategories(String wikiminer_id) {
 		String xml = getexploreCategoryXML(wikiminer_id);
 		ArrayList<WikipediaEntity> categories = new ArrayList<WikipediaEntity>();
 		if(xml == null) {
 			return categories;
 		}
 		DocumentBuilder db = null;
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			db = dbf.newDocumentBuilder();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		InputSource is = new InputSource();
 		is.setCharacterStream(new StringReader(xml));
 		try {
 			Document dom = db.parse(is);
 			NodeList catNodes = dom.getElementsByTagName("ParentCategory");
 			if (catNodes != null && catNodes.getLength() != 0) {
 				for (int i = 0; i < catNodes.getLength(); i++) {
 					Node cat = catNodes.item(i);
 					if (cat != null) {
 						NamedNodeMap attrs = cat.getAttributes();
 						String[] values = new String[2];
 						values[0] = attrs.getNamedItem("id").getTextContent();
 						values[1] = attrs.getNamedItem("title").getTextContent();
 						categories.add(new WikipediaEntity(values[1],values[0], 1));
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return categories;
 	}
 
 	public static double getJaccard(String xml, List<String> contextPhrases) {
 
 		ArrayList<String> links = new ArrayList<String>();
 		DocumentBuilder db = null;
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			db = dbf.newDocumentBuilder();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		InputSource is = new InputSource();
 		is.setCharacterStream(new StringReader(xml));
 		try {
 			Document dom = db.parse(is);
 			NodeList outNodes = dom.getElementsByTagName("LinkOut");
 			if (outNodes != null && outNodes.getLength() != 0) {
 				for (int i = 0; i < outNodes.getLength(); i++) {
 					Node link = outNodes.item(i);
 					if (link != null) {
 						NamedNodeMap attrs = link.getAttributes();
 						Node commonness = attrs.getNamedItem("relatedness");
 						double relevance = Double.parseDouble(commonness
 								.getTextContent());
 						if (relevance >= 0.1) {
 							links.add(attrs.getNamedItem("title")
 									.getTextContent());
 						}
 					}
 				}
 			}
 
 			NodeList inNodes = dom.getElementsByTagName("LinkIn");
 			if (inNodes != null && inNodes.getLength() != 0) {
 				for (int i = 0; i < inNodes.getLength(); i++) {
 					Node link = inNodes.item(i);
 					if (link != null) {
 						NamedNodeMap attrs = link.getAttributes();
 						Node commonness = attrs.getNamedItem("relatedness");
 						double relevance = Double.parseDouble(commonness
 								.getTextContent());
 						if (relevance >= 0.1) {
 							links.add(attrs.getNamedItem("title")
 									.getTextContent());
 						}
 					}
 				}
 			}
 
 			return calculateJaccard(links, contextPhrases);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return 0.0;
 	}
 
 	// get the category nodes inside the xml
 	public static List<String> getRankedTypes(String entity, String xml,
 			List<String> contextPhrases, int numTypes) {
 		int entityCount = YahooBOSS.makeQuery('"' + entity + '"');
 		StringBuffer contextQuery = new StringBuffer();
 		for (String c : contextPhrases) {
 			contextQuery.append("\"" + c + "\"" + " ");
 		}
 
 		List<String> rankedCategories = new ArrayList<String>();
 		PriorityQueue<String> queue = new PriorityQueue<String>();
 
 		DocumentBuilder db = null;
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			db = dbf.newDocumentBuilder();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		InputSource is = new InputSource();
 		is.setCharacterStream(new StringReader(xml));
 		try {
 			Document dom = db.parse(is);
 			NodeList senseNodes = dom.getElementsByTagName("Category");
 			if (senseNodes != null && senseNodes.getLength() != 0) {
 				for (int i = 0; i < senseNodes.getLength(); i++) {
 					Node topSense = senseNodes.item(i);
 					if (topSense != null) {
 						NamedNodeMap attrs = topSense.getAttributes();
 						String type = attrs.getNamedItem("title")
 								.getTextContent();
 						int count = YahooBOSS.makeQuery("\"" + type + "\" \""
 								+ entity + "\" " + contextPhrases.toString());
 						queue
 								.add(type,
 										((double) count / (double) entityCount));
 					}
 				}
 			}
 			while (queue.hasNext() && numTypes > 0) {
 				numTypes--;
 				rankedCategories.add(queue.next());
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return rankedCategories;
 	}
 
 	public static ArrayList<WikipediaEntity> getWikipediaEntities(String xml,
 			boolean getId) {
 		ArrayList<WikipediaEntity> senses = new ArrayList<WikipediaEntity>();
 		DocumentBuilder db = null;
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			db = dbf.newDocumentBuilder();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		InputSource is = new InputSource();
 		is.setCharacterStream(new StringReader(xml));
 		// System.out.println(xml);
 		try {
 			Document dom = db.parse(is);
 			NodeList senseNodes = dom.getElementsByTagName("Sense");
 			if (senseNodes != null && senseNodes.getLength() != 0) {
 				for (int i = 0; i < senseNodes.getLength(); i++) {
 					Node topSense = senseNodes.item(i);
 					if (topSense != null) {
 						NamedNodeMap attrs = topSense.getAttributes();
 						Node commonness = attrs
 								.getNamedItem("priorProbability");
 
 						if (commonness == null
 								&& xml.contains("Disambiguation")
 								&& xml
 										.contains("This is a disambiguation page")) {
 							double commonnessScore = (1.0 / senseNodes
 									.getLength());
 							String[] senseArray = {
 									attrs.getNamedItem("title")
 											.getTextContent(),
 									attrs.getNamedItem("id").getTextContent(),
 									String.valueOf(commonnessScore) };
 							WikipediaEntity we = new WikipediaEntity(
 									senseArray[0], senseArray[1], 0,
 									senseArray[2]);
 							senses.add(we);
 							continue;
 						}
 						double relevance = Double.parseDouble(commonness
 								.getTextContent());
 						if (relevance >= 0.01) {
 							String[] senseArray = {
 									attrs.getNamedItem("title")
 											.getTextContent(),
 									attrs.getNamedItem("id").getTextContent(),
 									String.valueOf(relevance) };
 							WikipediaEntity we = new WikipediaEntity(
 									senseArray[0], senseArray[1], 0,
 									senseArray[2]);
 							senses.add(we);
 						}
 					}
 				}
 			} else {
 				NodeList articleNodes = dom.getElementsByTagName("Article");
 				if (articleNodes != null && articleNodes.item(0) != null) {
 					Node article = articleNodes.item(0);
 					NamedNodeMap attrs = article.getAttributes();
 					String[] senseArray = {
 							attrs.getNamedItem("title").getTextContent(),
 							attrs.getNamedItem("id").getTextContent(), "1.0" };
 					WikipediaEntity we = new WikipediaEntity(senseArray[0],
 							senseArray[1], 0, senseArray[2]);
 					senses.add(we);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return senses;
 
 	}
 
 	public static ArrayList<String[]> getWikipediaSenses(String xml,
 			boolean getId) {
 		ArrayList<String[]> senses = new ArrayList<String[]>();
 		DocumentBuilder db = null;
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			db = dbf.newDocumentBuilder();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		InputSource is = new InputSource();
 		is.setCharacterStream(new StringReader(xml));
 		// System.out.println(xml);
 		try {
 			Document dom = db.parse(is);
 			NodeList senseNodes = dom.getElementsByTagName("Sense");
 			if (senseNodes != null && senseNodes.getLength() != 0) {
 				for (int i = 0; i < senseNodes.getLength(); i++) {
 					Node topSense = senseNodes.item(i);
 					if (topSense != null) {
 						NamedNodeMap attrs = topSense.getAttributes();
 						Node commonness = attrs.getNamedItem("commonness");
 
 						if (commonness == null
 								&& xml.contains("Disambiguation")
 								&& xml
 										.contains("This is a disambiguation page")) {
 							double commonnessScore = (1.0 / senseNodes
 									.getLength());
 							String[] senseArray = {
 									attrs.getNamedItem("title")
 											.getTextContent(),
 									attrs.getNamedItem("id").getTextContent(),
 									String.valueOf(commonnessScore) };
 							senses.add(senseArray);
 							continue;
 						}
 						double relevance = Double.parseDouble(commonness
 								.getTextContent());
 						if (relevance >= 0.01) {
 							String[] senseArray = {
 									attrs.getNamedItem("title")
 											.getTextContent(),
 									attrs.getNamedItem("id").getTextContent(),
 									String.valueOf(relevance) };
 							senses.add(senseArray);
 						}
 					}
 				}
 			} else {
 				NodeList articleNodes = dom.getElementsByTagName("Article");
 				if (articleNodes != null && articleNodes.item(0) != null) {
 					Node article = articleNodes.item(0);
 					NamedNodeMap attrs = article.getAttributes();
 					senses.add(new String[] {
 							attrs.getNamedItem("title").getTextContent(),
 							attrs.getNamedItem("id").getTextContent(), "1.0" });
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return senses;
 
 	}
 
 	public static String correctEncoding(String query) {
 		StringBuffer correctEncoding = new StringBuffer();
 		String[] sensesplit = query.split("\\s+");
 		if (sensesplit.length > 1)
 			for (String s : sensesplit) {
 				correctEncoding.append(s + "%20");
 			}
 		if (correctEncoding.length() != 0) {
 			query = correctEncoding.toString().substring(0,
 					correctEncoding.toString().length() - 3);
 		}
 		return query;
 	}
 
 	/*
 	 * Takes two words and gets the best Wikipedia Entities based on Compare
 	 * score.
 	 */
 	public static HashMap<String, WikipediaEntity> getRelativeBestSenses(
 			String word1, String word2) {
 		if (word1.startsWith("#"))
 			word1 = word1.replace("#", "");
 
 		if (word2.startsWith("#"))
 			word2 = word2.replace("#", "");
 
 		HashMap<String, WikipediaEntity> wes = new HashMap<String, WikipediaEntity>();
 
 		String urlStr = "compare:" + word1 + ":" + word2;
 		try {
 			if (cacheRelativeBestSenses.containsKey(urlStr)) {
 				return cacheRelativeBestSenses.get(urlStr);
 			} else {
 				String xml_word1 = getXML(word1, false);
 				String xml_word2 = getXML(word2, false);
 				if (xml_word1 == null || xml_word2 == null) {
 					return null;
 				}
 
 				List<WikipediaEntity> we_word1 = getWikipediaEntities(
 						xml_word1, false);
 				List<WikipediaEntity> we_word2 = getWikipediaEntities(
 						xml_word2, false);
 
 				WikipediaEntity bestSense_word1 = null;
 				WikipediaEntity bestSense_word2 = null;
 
 				double bestScore = 0.0;
 				for (int i = 0; i < we_word1.size(); i++) {
 					for (int j = 0; j < we_word2.size(); j++) {
 						double compareScore = compareIds(we_word1.get(i)
 								.getWikiminerID(), we_word2.get(j)
 								.getWikiminerID());
 						if (compareScore > bestScore) {
 							bestScore = compareScore;
 							bestSense_word1 = we_word1.get(i);
 							bestSense_word2 = we_word2.get(j);
 						}
 					}
 				}
 				wes.put(word1, bestSense_word1);
 				wes.put(word2, bestSense_word2);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		cacheRelativeBestSenses.put(urlStr, wes);
 		return wes;
 	}
 
 	public static double compareIds(String id1, String id2) {
 		String urlStr = "http://50.19.209.97:8080/wikipediaminer/services/compare?&ids1="
 				+ id1 + "&ids2=" + id2;
 
 		try {
 			if (cache.containsKey(urlStr)) {
 				return Double.parseDouble(cache.get(urlStr));
 			}
 
 			URL url = new URL(urlStr);
 			URLConnection yc = url.openConnection();
 			BufferedReader in = new BufferedReader(new InputStreamReader(yc
 					.getInputStream()));
 			String inputLine;
 
 			StringBuffer buf = new StringBuffer();
 			while ((inputLine = in.readLine()) != null)
 				buf.append(inputLine);
 			in.close();
 			if (buf.toString().contains("unknownTerm")) {
 				cache.put(urlStr, "0");
 				return 0.0;
 			}
 
 			// System.out.println(buf.toString());
 			DocumentBuilder db = null;
 			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 			try {
 				db = dbf.newDocumentBuilder();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			// System.out.println();
 
 			InputSource is = new InputSource();
 			is.setCharacterStream(new StringReader(buf.toString()));
 			Document dom = db.parse(is);
 			NodeList relatednessNodes = dom.getElementsByTagName("Measure");
 			Node relation = relatednessNodes.item(0);
 			if (relation != null) {
 				String str = relation.getTextContent();
 				cache.put(urlStr, str);
 				return Double.parseDouble(str);
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		cache.put(urlStr, "0");
 		return 0.0;
 	}
 
 	public static String getXML(String query, boolean isId) {
 		if (query.startsWith("#"))
 			query = query.replace("#", "");
 		if (query.equalsIgnoreCase("wikipedia entry"))
 			return null;
 		String[] sensesplit = query.split("\\s+");
 
 		for (int i = 0; i < sensesplit.length; i++) {
 			String s = sensesplit[i];
 			if (Character.isLowerCase(s.charAt(0))) {
 				sensesplit[i] = (Character.toUpperCase(s.charAt(0)) + s
 						.substring(1));
 			}
 		}
 
 		StringBuffer correctEncoding = new StringBuffer();
 		if (sensesplit.length > 1)
 			for (String s : sensesplit) {
 				correctEncoding.append(s + "%20");
 			}
 		if (correctEncoding.length() != 0) {
 			query = correctEncoding.toString().substring(0,
 					correctEncoding.toString().length() - 3);
 		}
 
 		try {
 			// String urlStr =
 			// "http://wdm.cs.waikato.ac.nz:8080/service?task=search&xml";
 			String urlStr = "http://50.19.209.97:8080/wikipediaminer/services/search?complex";
 
 			if (isId) {
 				// urlStr += "&id=" + query;
 				return null;
 			} else {
 				urlStr += "&query=" + query;
 			}
 
 			// return from cache
 			if (cache.containsKey(urlStr)) {
 				return cache.get(urlStr);
 			}
 
 			URL url = new URL(urlStr);
 			URLConnection yc = url.openConnection();
 			BufferedReader in = new BufferedReader(new InputStreamReader(yc
 					.getInputStream()));
 			String inputLine;
 
 			StringBuffer buf = new StringBuffer();
 			while ((inputLine = in.readLine()) != null)
 				buf.append(inputLine);
 			in.close();
 
 			String xml = buf.toString();
 
 			if (!xml.contains("Sense")) {
 				return null;
 			}
 
 			cache.put(urlStr, xml);
 			return xml;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public static void main(String args[]) {
 		ArrayList<WikipediaEntity> ents = getCategories("52648");
 		for(WikipediaEntity we : ents){
 			System.out.println(we.getText()+" "+we.getWikiminerID());
 		}
 
 	}
 
 }
