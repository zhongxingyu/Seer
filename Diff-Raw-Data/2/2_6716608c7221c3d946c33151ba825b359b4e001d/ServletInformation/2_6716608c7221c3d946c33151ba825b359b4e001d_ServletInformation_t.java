 package foundation;
 
 import javax.servlet.ServletContext;
 
 public class ServletInformation {
 	private ServletContext context;
 	private static ServletInformation singleton = null;
 	
	private ServletInformation()
 	{
 		context = null;
 	}
 	
 	static public ServletInformation getInstance()
 	{
 		if (singleton == null)
 			singleton = new ServletInformation();
 		return singleton; 
 	}
 	
 	public void setServletContext(ServletContext context)
 	{
 		this.context = context;
 	}
 	
 	public ServletContext getServletContext()
 	{
 		return context;
 	}
 	
 	public String resolvePath(String path)
 	{
 		// For tests.
 		if (context == null)
 			return path;
 		return context.getRealPath(path);
 	}
 }
