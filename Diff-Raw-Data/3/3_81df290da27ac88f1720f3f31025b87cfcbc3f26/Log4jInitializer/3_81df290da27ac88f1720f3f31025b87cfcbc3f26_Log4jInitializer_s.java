 import java.io.IOException;
 import java.net.URL;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.helpers.Loader;
 
 
 /**
  * Initializes log4j configuration based on environment properties
  * 
  * @author Varun Achar
  */
 public class Log4jInitializer extends HttpServlet
 {
 	private static final long	serialVersionUID	= 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public Log4jInitializer()
 	{
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see Servlet#init(ServletConfig)
 	 */
 	@Override
 	public void init(ServletConfig config) throws ServletException
 	{
 		String env = System.getenv("MY_ENVIRONMENT_VARIABLE");
 		URL log4jConfig = Loader.getResource("log4j-" + env + ".properties");
		PropertyConfigurator.configureAndWatch(log4jConfig.getFile().substring(1), 60 * 1000);
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
 	{
 		// Dummy Servlet. This doesn't execute because we're not mapping this servlet to any url
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
 	{
 		// Dummy Servlet. This doesn't execute because we're not mapping this servlet to any url
 	}
 
 }
