 package jipdbs.web;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.utils.SystemProperty;
 
 @SuppressWarnings("serial")
 public class InfoServlet extends HttpServlet {
 
 	public static class App {
 
 		final String version;
 
 		public App(String version) {
 			this.version = version;
 		}
 
 		public String getVersion() {
 			return version;
 		}
 		
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
		App app = new App(SystemProperty.version.get());
 		req.setAttribute("app", app);
 		
 	}
 }
