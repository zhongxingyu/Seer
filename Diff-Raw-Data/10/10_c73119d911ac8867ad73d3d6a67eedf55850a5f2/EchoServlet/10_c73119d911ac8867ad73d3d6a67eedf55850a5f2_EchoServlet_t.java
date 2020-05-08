 package gov.usgs.cida.ncetl.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 import java.util.Map;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  *
  * @author jwalker
  */
 //TODO Keep EchoServlet clean and subclass it for changes
 public class EchoServlet extends HttpServlet {
 
 	@Override
 	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		PrintWriter writer = resp.getWriter();
 		writer.write("DELETE called with params: ");
 		writeParams(writer, req);
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		PrintWriter writer = resp.getWriter();
 		String query = req.getQueryString();
 		if (query != null && query.contains("GetCapabilities")) {
 			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
 			writer.write("<ows:Capabilities>\n</ows:Capabilities>");
 		}
 		else if (query != null && query.contains("DumpHeaders")) {
 			Enumeration<String> headerNames = req.getHeaderNames();
 			while (headerNames.hasMoreElements()) {
 				String nextElement = headerNames.nextElement();
 				Enumeration<String> headers = req.getHeaders(nextElement);
 				while (headers.hasMoreElements()) {
 					writer.write(nextElement + ": " + headers.nextElement() + "\n");
 				}
 			}
 		}
 		else {
 			writer.write("GET called with params: ");
 			writeParams(writer, req);
 		}
 	}
 
 	@Override
 	protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		PrintWriter writer = resp.getWriter();
 		writer.write("HEAD called with params: ");
 		writeParams(writer, req);
 	}
 
 	@Override
 	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		PrintWriter writer = resp.getWriter();
 		writer.write("OPTIONS called with params: ");
 		writeParams(writer, req);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		PrintWriter writer = resp.getWriter();
 		writer.write("POST called with params: ");
 		writeParams(writer, req);
 	}
 
 	@Override
 	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		PrintWriter writer = resp.getWriter();
 		writer.write("PUT called with params: ");
 		writeParams(writer, req);
 	}
 
 	@Override
 	protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		PrintWriter writer = resp.getWriter();
 		writer.write("TRACE called with params: ");
 		writeParams(writer, req);
 	}
 
 	private void writeParams(PrintWriter writer, HttpServletRequest req) {
 		Map<String, String[]> parameterMap = req.getParameterMap();
 		String query = req.getQueryString();
 
 		if (query != null && !"".equals(query)) {
 			writer.write(query);
 		}
 		else if (parameterMap.isEmpty()) {
 			writer.write("NONE");
 		}
 		else {
 			for (String ob : parameterMap.keySet()) {
 				String[] get = parameterMap.get(ob);
 				writer.write(ob + "=" + get[0] + ", ");
 			}
 		}
 	}
 }
