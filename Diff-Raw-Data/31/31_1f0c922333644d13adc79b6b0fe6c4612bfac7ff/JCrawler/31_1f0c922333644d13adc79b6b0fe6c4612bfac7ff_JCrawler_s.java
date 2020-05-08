 package scottdjohnson.jcrawler;
 
 import org.jsoup.Jsoup;
 import org.jsoup.helper.Validate;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.io.IOException;
 
 import scottdjohnson.binarytree.URLNode;
 import scottdjohnson.binarytree.BinaryTree;
 
 /**
  * JCrawler provides the basic functionlity for crawling a Web URL.
  * 
  * @author Scott Johnson
 */
 public class JCrawler 
 {	
 	/**
 	 * Crawl a URL and store it and its children in a database
 	 * Absolute URLs are assumed to be external to the site, relative
 	 * URLs are considered internal and are also crawled recursively
 	 * The binary tree bt assures that we do not crawl a URL more than once
 	 * otherwise we will likely end up in an ifinite loop
 	 * 
 	 * @param bt A binary tree which stores the URLs that we have crawled so far
 	 * @param url The URL to crawl
 	 * @param parent The parent key in the database for this URL
 	 */
 	public static void getLinksFromURL( BinaryTree bt, String url, long parent)
 	{		
 		try{
 			Document doc = Jsoup.connect(url).get();
 			Elements links = doc.select("a[href]");
 			int size		= links.size();
 		
 			// First we store all the URLs at the current level
 			for (int i = 0; i < size; i++)
 			{
 				Element link = links.get(i);
 				
 				// We are going to assume that an absolute URL points to an external
 				//	external web site. Not an entirely safe assumption but ok for this demo
 				
 				URLNode un = new URLNode( link.attr("abs:href"), parent);
 				
 				if ( !bt.isNodeInTree( un ) )
 				{
 					bt.insertNode( un );
 					un.save();
 					System.out.println("Inserting URL: " + link.attr("href"));
 				}
 			}				
 			
 			// We now go through the links again and look recursively, except for links we have already looked at
 			for (int i = 0; i < size; i++)
 			{
 				Element link 	= links.get(i);
 				URLNode un 	= new URLNode( link.attr("abs:href"), parent);
 				URI u           = new URI(link.attr("href"));
 
 				if ( !bt.isNodeInTree( un ) )
 					if (!u.isAbsolute())
 						getLinksFromURL( bt, un.getUrl(), un.getKey() );
 
 			}
 		}
 		catch(URISyntaxException e)
 		{
 			System.out.println("This is not a valid URL");
 		}
 		catch(IOException e)
 		{
 			System.out.println("Could not connect to this URL");
 
 		}
 
 	}
 
 }
