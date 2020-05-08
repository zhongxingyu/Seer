 package info.liberitas.wss;
 
 import info.liberitas.wss.api.WebSocketServiceWrapper;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Alexander <estliberitas> Makarenko
  * Date: 29.04.12
  * Time: 23:29
  */
 public class WebSocketServlet extends HttpServlet {
 
	private WebSocketService service;
 	private WebSocketServiceWrapper serviceWrapper;
 
 	@Override
 	public String getInitParameter(String name) {
 		String initParam = super.getInitParameter(name);
 		if (initParam != null && !initParam.isEmpty()) {
 			return initParam;
 		}
 
 		if (getServletConfig() == null || getServletContext() == null) {
 			return null;
 		}
 
 		String contextParam = getServletContext().getInitParameter(name);
 		if (contextParam != null && !contextParam.isEmpty()) {
 			return contextParam;
 		}
 
 		return null;
 	}
 
 	@Override
 	public void init() throws ServletException {
		service = new WebSocketService(this);
 		serviceWrapper = WrapperUtils.getServiceWrapper(service);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		serviceWrapper.doGet(req, resp);
 	}
 }
