 package uk.co.brotherlogic.jarpur;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import uk.co.brotherlogic.jarpur.replacers.SimpleReplacer;
 import uk.co.brotherlogic.mdb.Connect;
 
 /**
  * Front of house deals with request and routes them to the relevant controller
  * 
  * @author sat
  * 
  */
 public class FrontOfHouse extends HttpServlet {
 	List<Route> routingTable;
 	private static String baseAddress = "";
 	public static ServletContext context;
 
 	Route resourceRoute = new Route("resource/", null);
 
 	public FrontOfHouse() {
 		Connect.setForProduction();
 	}
 
 	public static void main(String[] args) {
 		FrontOfHouse foh = new FrontOfHouse();
 	}
 
 	private void buildRoutingTable() {
 		routingTable = new LinkedList<Route>();
 		Properties props = new Properties();
 
 		try {
 			props.load(new FileInputStream(new File(getServletContext()
 					.getRealPath("WEB-INF")
 					+ "/routing.properties")));
 			Properties lProps = new Properties();
 			lProps.load(new FileInputStream(new File(getServletContext()
 					.getRealPath("WEB-INF")
 					+ "/links.properties")));
 			SimpleReplacer.setLinkTable(new LinkTable(lProps));
 			context = getServletContext();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		for (Object matcher : props.keySet()) {
 			routingTable.add(new Route(matcher.toString(), props.get(matcher)
 					.toString()));
 		}
 	}
 
 	@Override
 	public void doGet(HttpServletRequest req, HttpServletResponse res)
 			throws IOException, ServletException {
 
 		System.err.println("BASE ADDRESS");
 		LinkTable.add = req.getContextPath();
 
 		if (baseAddress.length() == 0) {
 			// Build up the base address
 			String base = req.getContextPath();
 			baseAddress = req.getRequestURL().substring(0,
 					req.getRequestURL().indexOf(base) + base.length())
 					+ "/";
 			System.err.println("BASE = " + baseAddress);
 		}
 
 		String params = req.getRequestURL().toString();
 
 		System.err.println("PARAMS = " + req.getRequestURL().toString()
 				+ " and " + params);
		System.err.println("SUBSTRING: " + baseAddress + " and " + params);
		LinkTable.add = baseAddress.substring(baseAddress.indexOf("/", 7));
 		String request = params
 				.substring(baseAddress.length(), params.length());
 
 		if (resourceRoute.matches(request)) {
 			// We're after a resource rather than a page - just serve it
 			OutputStream os = res.getOutputStream();
 			byte[] buffer = new byte[256];
 			InputStream is = new FileInputStream(new File(getServletContext()
 					.getRealPath("WEB-INF")
 					+ "/resources/" + resourceRoute.getRemaining(request)));
 			int read = is.read(buffer, 0, buffer.length);
 			while (read > 0) {
 				os.write(buffer, 0, read);
 				read = is.read(buffer, 0, buffer.length);
 			}
 			is.close();
 			os.close();
 		} else {
 			// Build the routing table if we need to
 			if (routingTable == null)
 				buildRoutingTable();
 
 			res.setContentType("text/html");
 			PrintWriter out = res.getWriter();
 
 			Route matcher = null;
 			for (Route route : routingTable) {
 				if (route.matches(request)) {
 					matcher = route;
 					break;
 				}
 			}
 
 			if (matcher != null) {
 				Page handler = matcher.getHandler();
 				String remainder = matcher.getRemaining(request);
 				String[] elems = remainder.split("/");
 				Map<String, String> parameters = new TreeMap<String, String>();
 				System.err.println("REMAIN = " + remainder);
 				if (elems.length >= 2)
 					for (int i = 0; i < elems.length; i += 2)
 						parameters.put(elems[i], elems[i + 1]);
 
 				long sTime = System.currentTimeMillis();
 				out.println(handler.buildPage(parameters));
 				System.err.println("Build time = "
 						+ (System.currentTimeMillis() - sTime) + "ms");
 
 				out.close();
 			}
 		}
 	}
 }
