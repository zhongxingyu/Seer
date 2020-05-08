 package scottdjohnson.jcrawler;
 
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import java.util.List;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 
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
  * A servlet class for posting new URLs to crawl and getting URL crawl results form the database
 *
 * @see JCrawler
 *
 * @author Scott Johnson
 **/
 public class JCrawlerServlet extends HttpServlet 
 {
 	/**
 	 * Get the results of a previous JCrawler URL crawl from the database
 	 * Delivers HTML which can be inserted into an web page
 	 *
 	 * We optionally allow a urlkey Get variable, which is the key of a previous URL that
 	 * has already been crawled. If this is not delivered, we assume the requester wants
 	 * all results
 	 *
 	 * @param request The request to get from the servlet
 	 * @param response The response back from the servlet
 	 **/
   	public void doGet(HttpServletRequest request, HttpServletResponse response)
     	 throws ServletException, IOException 
 	{
 		String urlkey = request.getParameter("urlkey");
 
 		org.hibernate.Session s = getSession();
                 Query query;
 
 		// If there is a specifc key, get it, otherwise get all of them
 		if (null != urlkey)
 			query = s.createQuery("from URLNode url_list where parent_key = " + urlkey);
 		else
 			query = s.createQuery("from URLNode url_list where parent_key = 0");
 
 		response.setContentType("text/html");
 		
 		// This is necessary for AJAX requests to this function
 		response.setHeader("Access-Control-Allow-Origin","*");
 
 		PrintWriter out = response.getWriter();
 		List list =  query.list();
 		s.close();
 
 		// Loop through all the results from the query
 		for (int i = 0; i < list.size(); i++)
 		{
 			// Count the total number of results that have this URL as a parent
 			int count =( (Integer) getSession().createQuery("select count(*) from URLNode where parent_key="
 				+ ((URLNode)list.get(i)).getKey()).iterate().next() ).intValue();
 
 			// Print out this URL with the number of its children
 			out.println("<a href='' onclick=\"return getAJAX(" + ((URLNode)list.get(i)).getKey() + ");\">" 
                         + ((URLNode)list.get(i)).getUrl() + "</a> (" + count + ")<br />");
 		}
 	}
 
 	/**
 	 * Post the q reuest to crawl a URL, given by the crawl_url parameter
 	 *
 	 * @param request The request to get from the servlet
 	 * @param response The response back from the servlet
 	 **/
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 		throws ServletException, IOException
 	{
 		response.setContentType("text/html");
 		
 		// This is necessary for AJAX requests to this function
 		response.setHeader("Access-Control-Allow-Origin","*");
 
 		String crawl_url = request.getParameter("crawl_url");
 
 		// Store this first URL in the binary tree and save it to the DB
 		BinaryTree bt = new BinaryTree();
 		URLNode un = new URLNode(crawl_url);		 
 		bt.insertNode(un);
 		un.save();
 		
 		// Crawl the URL and its children
 		JCrawler.getLinksFromURL(bt, crawl_url, un.getKey());
 	}
 
 	/**
 	 * Create a new Hibernate session
 	 **/
 	private org.hibernate.Session getSession()
 	{
 		return new Configuration().configure().buildSessionFactory().openSession();
 	}
 }
 
