 package mx.meido.simpleshorturl.servlet;
 
 import java.io.IOException;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import mx.meido.simpleshorturl.listener.SimpleShortUrlContextListener;
 import mx.meido.simpleshorturl.util.ShortUrlGen;
 import mx.meido.simpleshorturl.util.db.MongoDbDAOFactory;
 
 /**
  * Servlet implementation class UrlRedirectServlet
  */
 public final class UrlRedirectServlet extends HttpServlet {
 	private static final long serialVersionUID = 3671843701429536982L;
 	
 	public static ShortUrlGen shortUrlGen;
 	
 	/**
      * @see HttpServlet#HttpServlet()
      */
     public UrlRedirectServlet() {
         super();
     }
     
     @Override
     public void init(ServletConfig config) throws ServletException {
     	super.init(config);
     	log("UrlServlet Initialized!");
     	UrlRedirectServlet.shortUrlGen = new ShortUrlGen(MongoDbDAOFactory.getInstance());
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String path = request.getRequestURI();
 		
 		if(path.indexOf(request.getContextPath())==0){
 			path = apartShortUrl(path);
 		}else{
			path = path.substring(2);
 		}
 		
 		log("s: "+path);
 		String fullUrl = MongoDbDAOFactory.getInstance().getFullUrl(path);
 		log("f: "+fullUrl);
 		
 		if(fullUrl != null){
 			response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
 			response.setHeader("Location", fullUrl);
 		}else{
 			response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
 			response.setHeader("Location", SimpleShortUrlContextListener.getProps().get("admin-url"));
 		}
 	}
 	
 	/**
 	 * important: uri must with context
 	 * @param uri
 	 * @return
 	 */
 	private String apartShortUrl(String uri){
 		return uri.substring(uri.indexOf("/s", 1)+3);
 	}
 
 }
