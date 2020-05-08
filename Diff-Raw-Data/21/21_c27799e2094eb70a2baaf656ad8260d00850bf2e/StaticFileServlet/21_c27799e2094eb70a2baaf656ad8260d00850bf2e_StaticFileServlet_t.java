 package co.ohba.autumn;
 
import java.io.File;
import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebInitParam;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpServletResponse;
 
 import net.balusc.webapp.FileServlet;
 
 /*
  * This is essentially our "Default Servlet"
  * that gets hit after all the guice+jersey filters give up
  */
 @WebServlet(urlPatterns={"/*"},initParams= {@WebInitParam(name="basePath",value="public")})
 public class StaticFileServlet extends FileServlet {
 	
 	/*
 	 * I'm extending this FileServlet so that I don't change it's code thus complying with the GNU Lesser GPL license
 	 * 
 	 * FileServlet is from http://balusc.blogspot.com/2009/02/fileservlet-supporting-resume-and.html
 	 * 
 	 * patterns for how to use Jersey as a filter within guice (instead of a servlet) and how to configure jersey
 	 * to forward along the filter chain requests it cant handle were gleaned from this article:
 	 * http://java.net/projects/jersey/lists/users/archive/2010-08/message/196
 	 * 
 	 * you will see some of that implemented in App.java
 	 */
 	
 	@Override
	public void init() throws ServletException{
 		// no code but we MAY need to inject logic here someday
        File path = new File(getServletContext().getRealPath(getInitParameter("basePath")));
        if(!path.exists()){
            throw new ServletException("Please create a 'public' directory in your webapp folder!");
        }
 		super.init();
 	}
 	
 	@Override
 	protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// no code but we MAY need to inject logic here someday
 		super.doHead(request, response);
 	}
 	
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// no code but we MAY need to inject logic here someday
 		if(request.getPathInfo().substring(request.getPathInfo().length() - 1).equals("/")){

 			super.doGet(new HttpServletRequestWrapper(request) {
                public String getPathInfo() {
                    return super.getPathInfo().replaceAll("/$", "/index.html");
                }
            }, response);
 		}else{
 			super.doGet(request, response);
 		}
 		
 	}
 	
 }
