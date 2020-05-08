 import java.util.ArrayList;
 import java.util.List;
 
 import model.Image;
 import model.Link;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class Parser {
 
 	private List<Image> mImageList;
 	private List<Link> mLinkList;
 	private Document doc;
 	private String src;
 	private String alt;
 	private String caption;
 	
 	public Parser() {
 		mImageList = new ArrayList<Image>();
 		mLinkList = new ArrayList<Link>();
 	}
 	
 	public void parse(Document docsrc, String pageUrl)
 	{
 		//get all images
 		doc = docsrc;
 		Elements images = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
 		for (Element image : images) {
 				src = makeAbsolute(pageUrl, image.attr("src"));
 				alt = image.attr("alt");
 				caption = image.attr("caption");
 				mImageList.add(new Image(src,alt,caption));
 		}
 		Elements links = doc.select("a[href]");
 		for (Element link : links) {
 			String href;
 			String text;
 			// get the value from href attribute
 			href = makeAbsolute(pageUrl, link.attr("href"));
 			text = link.text();
 			
 			mLinkList.add(new Link(href, text));
 		}
  
 	}
 	
 	private String makeAbsolute(String url, String link) {
	    if (link.matches("http://.*") || link.matches("https://.*")) {
 	      return link;
 	    }
 	    if (link.matches("/.*") && url.matches(".*$[^/]")) {
 	      return url + "/" + link;
 	    }
 	    if (link.matches("[^/].*") && url.matches(".*[^/]")) {
 	      return url + "/" + link;
 	    }
 	    if (link.matches("/.*") && url.matches(".*[/]")) {
 	      return url + link;
 	    }
 	    if (link.matches("/.*") && url.matches(".*[^/]")) {
 	      return url + link;
 	    }
 	    throw new RuntimeException("Cannot make the link absolute. Url: " + url
 	        + " Link " + link);
 	  }
 	
 	public List<Image> getImages()
 	{
 		return mImageList;
 	}
 	public List<Link> getLinks()
 	{
 		return mLinkList;
 	}
 
 }
