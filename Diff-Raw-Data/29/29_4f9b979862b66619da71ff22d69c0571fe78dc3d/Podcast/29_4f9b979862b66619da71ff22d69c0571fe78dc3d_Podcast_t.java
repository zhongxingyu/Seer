 package com.niavok.podcast;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class Podcast {
 	private List<PodcastTrack> cachedTrackList;
 	private static final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 	private final String podcastName;
 	private final String podcastUrl;
 	private final Pattern podcastInputTitleRegex;
 	private final Pattern podcastInputNumberRegex;
 	private final String youtubeOutputTitleFormat;
 	private final Pattern youtubeNumberRegex;
 	private int podcastInputTitleRegexIndex;
 	private int podcastInputNumberRegexIndex;
 	private int youtubeNumberRegexIndex;
 	private int index;
 	private String descriptionFormat;
 	
 	
 	public Podcast(int index, String podcastName,
 			String podcastUrl,
 			String podcastInputTitleRegex,
 			int podcastInputTitleRegexIndex,
 			String podcastInputNumberRegex,
 			int podcastInputNumberRegexIndex,
 			String youtubeOutputTitleFormat,
 			String youtubeNumberRegex,
 			int youtubeNumberRegexIndex,
 			String descriptionFormat) {
 				this.index = index;
 				this.podcastName = podcastName;
 				this.podcastUrl = podcastUrl;
 				this.descriptionFormat = descriptionFormat;
 				this.podcastInputTitleRegex = Pattern.compile(podcastInputTitleRegex);
 				this.podcastInputTitleRegexIndex = podcastInputTitleRegexIndex;
 				this.podcastInputNumberRegex = Pattern.compile(podcastInputNumberRegex);
 				this.podcastInputNumberRegexIndex = podcastInputNumberRegexIndex;
 				this.youtubeOutputTitleFormat = youtubeOutputTitleFormat;
 				this.youtubeNumberRegex = Pattern.compile(youtubeNumberRegex);
 				this.youtubeNumberRegexIndex = youtubeNumberRegexIndex;
 	}
 
 	public String getPodcastName() {
 		return podcastName;
 	}
 	
 	public String getDescriptionFormat() {
 		return descriptionFormat;
 	}
 
 	public String getYoutubeOutputTitleFormat() {
 		return youtubeOutputTitleFormat;
 	}
 	
 	public Pattern getYoutubeNumberRegex() {
 		return youtubeNumberRegex;
 	}
 	
 	public int getYoutubeNumberRegexIndex() {
 		return youtubeNumberRegexIndex;
 	}
 	
 	public List<PodcastTrack> getTracks() {
 		
 		if(cachedTrackList == null) {
 			generateList();
 		}
 		return cachedTrackList;
 	}
 	
 	private void generateList() {
 
 		cachedTrackList = new ArrayList<PodcastTrack>();
 
 		URL website;
 		try {
 			website = new URL(podcastUrl);
 		} catch (MalformedURLException e) {
 			throw new RessourceLoadingException("Malformed url '" + podcastUrl, e);
 		}
 	    
 	    
 	    try {
             DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 
             Document doc;
             doc = docBuilder.parse(website.openStream());
 
             Element root = doc.getDocumentElement();
 
             if (root.getNodeName().contains("rss")) {
             	System.out.println("rss");
                 parseRss(root);
             } else {
                 throw new RessourceLoadingException("Unknown tag '"+root.getNodeName()+"'for root");
             }
 
         } catch (ParserConfigurationException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (SAXException e) {
             throw new RessourceLoadingException("Failed to parse url '" + podcastUrl, e);
         } catch (IOException e) {
             throw new RessourceLoadingException("Failed to load url '" + podcastUrl, e);
         }
 	    
 	}
 
 
 	private void parseRss(Element element) {
 		NodeList childNodes = element.getChildNodes();
         for (int i = 0; i < childNodes.getLength(); i++) {
             Node node = childNodes.item(i);
             if (node.getNodeType() != Node.ELEMENT_NODE) {
                 // TODO error
                 continue;
             }
             Element subElement = (Element) node;
             if (subElement.getNodeName().equals("channel")) {
                 parseChannel(subElement);
             } else {
                 throw new RessourceLoadingException("Unknown tag '"+subElement.getNodeName()+"'for element '"+element.getNodeName()+"'");
             }
         }
         
 	}
 
 
 	private void parseChannel(Element element) {
 		NodeList childNodes = element.getChildNodes();
         for (int i = 0; i < childNodes.getLength(); i++) {
             Node node = childNodes.item(i);
             if (node.getNodeType() != Node.ELEMENT_NODE) {
                 // TODO error
                 continue;
             }
             Element subElement = (Element) node;
             if (subElement.getNodeName().equals("item")) {
                 parseItem(subElement);
             }
         }
 		
 	}
 
 
 	private void parseItem(Element element) {
 		PodcastTrack track = new PodcastTrack(this);
 		cachedTrackList.add(track);
 		
 		NodeList childNodes = element.getChildNodes();
         for (int i = 0; i < childNodes.getLength(); i++) {
             Node node = childNodes.item(i);
             if (node.getNodeType() != Node.ELEMENT_NODE) {
                 // TODO error
                 continue;
             }
             Element subElement = (Element) node;
             if (subElement.getNodeName().equals("title")) {
                 track.setTitle(subElement.getTextContent());
             }else if (subElement.getNodeName().equals("itunes:summary")) {
                 track.setDescription(subElement.getTextContent());
            }else if (subElement.getNodeName().equals("itunes:image")) {
                track.setImageUrl(subElement.getAttribute("href"));
             }else if (subElement.getNodeName().equals("link")) {
             	track.setPaperUrl(subElement.getTextContent());
             } else if (subElement.getNodeName().equals("media:content")) {
                 track.setAudioUrl(subElement.getAttribute("url"));
             } else if (subElement.getNodeName().equals("image")) {
             	track.setImageUrl(subElement.getElementsByTagName("url").item(0).getTextContent());
             }
             
             
         }
 		
 	}
 
 	public Pattern getInputTitleRegexPattern() {
 		return podcastInputTitleRegex;
 	}
 	
 	
 	public int getInputTitleRegexIndex() {
 		return podcastInputTitleRegexIndex;
 	}
 
 	public Pattern getInputNumberRegexPattern() {
 		return podcastInputNumberRegex;
 	}
 
 	public int getInputNumberRegexIndex() {
 		return podcastInputNumberRegexIndex;
 	}
 
 
 	public int getIndex() {
 		return index;
 	}
 	
 }
