 package net.java.dev.cejug.classifieds.server.welcome;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.ejb.EJB;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 
 import net.java.dev.cejug.classifieds.server.ejb3.bean.interfaces.ClassifiedsAdminLocal;
 import net.java.dev.cejug_classifieds.metadata.admin.MonitorQuery;
 import net.java.dev.cejug_classifieds.metadata.admin.MonitorResponse;
 
 /**
  * Servlet implementation class Teste
  */
 public class Welcome extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	@EJB
	private transient ClassifiedsAdminLocal admin;
 
 	@Override
 	public void service(ServletRequest request, ServletResponse response)
 			throws ServletException, IOException {
 		MonitorQuery query = new MonitorQuery();
 		query.setAverageResponseLength(44);
 		MonitorResponse monResponse = admin.checkMonitorOperation(query);
 
 		PrintWriter out = response.getWriter();
 		out.println(monResponse.getServiceName() + " is online since "
 				+ monResponse.getOnlineSince());
 	}
 }
