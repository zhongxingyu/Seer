 package edu.virginia.cs4240.webmetrics.modules;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class ElementModule extends Module {
 
 	@Override
 	public String getStatistics() {
 		StringBuilder statBuild = new StringBuilder(256);
 		Document document;
 		try {
 			document = request.getDocument();
 			statBuild.append(ElementTypes(document ));
 			statBuild.append(numLinks(document,request.getUrlHost()));
 			statBuild.append(numImages(document));
 			statBuild.append(resList(document));
 		} catch (IOException e) {
 			statBuild.append(e.getMessage());
 		}
 		return statBuild.toString();
 	}
 	
 	//returns a string of tag names in given doc
 	public static String ElementTypes(Document doc){
 		Elements all = doc.getAllElements();
 		
 		ArrayList<String> tagList = new ArrayList<String>();
 		String retString = "List of tag names: \n";
 		for(Element current: all){
 			String tagName = current.tagName();
 			if(!tagList.contains(tagName)&&!tagName.equals("#root")){
 				retString+=tagName+", ";
 				tagList.add(tagName);
 			}
 		}
 		return retString+"\n";
 	}
 	//returns string with num of links, total, internal, external
 	public static String numLinks(Document doc, String host){
 		//Get all the link elements
 		Elements links = doc.getElementsByTag("a");
 		
 		//Initialize link counts
 		int linkCount = 0;
 		int iLinks = 0;
 		int eLinks = 0;
 		//Iterate through links
 		for (Element link : links) {
 		  String linkHref = link.attr("href");
 		  if(linkHref.contains(host)||linkHref.startsWith("/")){
 			  ++iLinks;
 		  }
 		  else{
 			  ++eLinks;
 		  }
 		  linkCount++;
 		}
 		String ret = "Total links: "+linkCount+"\nInternal links: "+iLinks
 		+"\nExternal links: "+eLinks;
 		return ret+"\n";
 	}
 	//returns number of images in web page
 	public static String numImages(Document doc){
 		Elements images = doc.getElementsByTag("img");
		String retVal = "Number of Images: " +images.size()+"\n";
 		return retVal;
 	}
 
 	//return list of resources
 	public static String resList(Document doc){
 		StringBuilder retString = new StringBuilder(256);
 		retString.append("List of resources:\n");
 		Elements resources = doc.getElementsByTag("link");
 		for(Element e: resources){
 			retString.append(e.toString()+"\n");
 		}
 		return retString.toString();
 	}
 }
