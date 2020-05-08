 package CS247;
 
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.*;
 
 import java.util.*;
 import java.io.*;
 import java.net.URLEncoder;
 
 /* 
 	A job to parse RSS feeds.
 	It requies one param from the server: the url.
 	And it returns a result with all the titles, links and descriptions in that order.
 */
 class RSSJob extends XMLJob {
 
 	String url;
 	RSSHandler handler;
 	
 	RSSJob(Job j){
 		super(j);
 		
 		try {
 			// the url of the rss feed should be the first parameter.
 			url = params.get(0);
 		} catch(Throwable e){
 			e.printStackTrace();
 			ret = new Result(Result.INVALID);
 		}
 		// create the RSSHandler, as shown below.
 		handler = new RSSHandler(type == Job.TEST ? true : false);
 	}
 	
 	Result execute(){
 		// if ret is not null then there has been an error, return the invalid result.
 		if(ret != null) return ret;
 		try {
 			// start parsing the XML returned.
			parser.parse(url, handler);
 			// get the result after parsing.
 			ret = handler.result;
 		} catch(Throwable e){
 			// in case of an error, return an invalid result.
 			ret = new Result(Result.INVALID);
 			e.printStackTrace();
 		}
 		return ret;
 	}
 	
 	// static method for testing.
 	public static void main(String[] args){
 		RSSJob r = new RSSJob(new Job(Job.TEST, "http://www.reddit.com/r/worldnews/.xml"));
 		r.execute();
 	}
 }
 
 class RSSHandler extends DefaultHandler {
 	// result that is filled after parsing.
 	Result result;
 	// the current xml element name.
 	private String current_element;
 	private RSSInfo item;
 	private ArrayList<RSSInfo> rss_items;
 	private int item_number;
 	private boolean done;
 	// bool to print debug info, will be true if the static test method is used.
 	private boolean debug;
 	
 	RSSHandler(boolean debug){
 		this.debug = debug;
 		item_number = -1;
 		rss_items = new ArrayList<RSSInfo>();
 		current_element = "none";
 		done = false;
 	}
 	// this method is called for every xml start tag i.e. <item>
 	public void startElement(String namespaceURI, String localName, 
 						String qName, Attributes atts) throws SAXException {
 		if(localName.equals("item")){
 			item_number++;
 			item = new RSSInfo();
 		}
 		if(item_number < 0) return;
 		current_element = localName;
 	}
 	
 	// this method is called with the characters between an xml start / end tag.
 	public void characters(char[] ch, int start, int length) throws SAXException {
 		// add titles, links and descriptions to our list of rss_items.
 		if(current_element.equals("title")){
 			item.title += (new String(ch, start, length)).replaceAll("(\\r|\\n)", "");
 		}
 		if(current_element.equals("link")){
 			item.link += (new String(ch, start, length)).replaceAll("(\\r|\\n)", "");
 		}
 		if(current_element.equals("description")){
 			item.desc += (new String(ch, start, length)).replaceAll("(\\r|\\n)", "");
 		}
 	}
 	// this method is called for every xml end tag i.e. </item>
 	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
 		if(localName.equals("item")){
 			// if any of the three pieces of info are missing, add some sane values.
 			if(item.title.equals("")) item.title = "untitled";
 			if(item.link.equals("")) item.link = "unavailable";
 			if(item.desc.equals("")) item.desc = "unavailable";
 			
 			rss_items.add(item);
 		}
 		// if we get a channel end tag, the rss feed is over, so create the result.
 		if(localName.equals("channel") && !done){
 			done = true;
 			result = new Result(Result.RSS);
 			for(RSSInfo i : rss_items){
 				if(debug) System.out.printf("T: %s\nL: %s\nD: %s\n", i.title, i.link, i.desc);
 				result.addParam(i.title);
 				try {
 					i.link = URLEncoder.encode(i.link, "UTF-8");
 				} catch(Exception e){
 					i.link = "unavailable";
 				}
 				result.addParam(i.link);
 				result.addParam(i.desc);
 			}
 		}
 		current_element = "none";
 	}
 }
 // class to hold the strings we are interested in.
 class RSSInfo {
 	String title;
 	String link;
 	String desc;
 	RSSInfo(){
 		title = link = desc = "";
 	}
 }
