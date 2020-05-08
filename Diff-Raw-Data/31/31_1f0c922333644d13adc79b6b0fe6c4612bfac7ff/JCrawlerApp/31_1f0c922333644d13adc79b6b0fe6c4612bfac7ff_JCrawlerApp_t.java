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
 import scottdjohnson.jcrawler.JCrawler;
 
 /**
  * Provides the main function for accessing JCrawler
 *
 * @see JCrawler
 *
 * @author Scott Johnson
 **/
 public class JCrawlerApp 
 {	
 	/**
 	 * Main function for launching JCrawler
 	 * @param args a URL to crawl and store in the database
 	 */
 	public static void main(String[] args)
 	{
 		BinaryTree bt = new BinaryTree();
 		URLNode un = new URLNode(args[0]);
 		 
 		Validate.isTrue(args.length == 1, "usage: supply url to fetch");
 		String url 	= args[0];
 		
 		bt.insertNode(un);
 		un.save();
 		
 		JCrawler.getLinksFromURL(bt, url, un.getKey());
 	}
 }
