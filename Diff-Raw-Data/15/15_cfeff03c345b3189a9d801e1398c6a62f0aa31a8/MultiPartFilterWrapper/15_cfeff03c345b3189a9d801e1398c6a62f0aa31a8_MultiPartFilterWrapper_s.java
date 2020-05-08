 package ee.ut.cs.veebirakendus2013.kurivaim.jettytest.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 import org.eclipse.jetty.servlets.MultiPartFilter;
 
 public class MultiPartFilterWrapper extends MultiPartFilter {
 	
 	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
 		try {
 			super.doFilter(request, response, chain);
 		}
 		catch(IllegalStateException e) {
 			request.setAttribute("multiPartError", new Integer(1));
 		}
 		catch (IOException e) {
			request.setAttribute("multiPartError", new Integer(2));
			
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		catch (ServletException e) {
			request.setAttribute("multiPartError", new Integer(3));
			
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
}
