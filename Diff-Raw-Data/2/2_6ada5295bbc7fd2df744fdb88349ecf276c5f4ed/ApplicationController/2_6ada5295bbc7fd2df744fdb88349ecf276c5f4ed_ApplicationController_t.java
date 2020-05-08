 package app.servlets;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.lang.reflect.*;
 
 public class ApplicationController extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		try {
 			String path = request.getPathInfo();
 			if (path != null && !path.equals("/")) {
 				
 				StringBuilder url = new StringBuilder(path);
 				url.replace(0, 1, ""); // /accion => accion
 				if (url.indexOf("/") != -1) // accion/* => accion
 					url.replace(url.indexOf("/"), url.length(), "");
 				path = url.toString();
 				
 				Method[] methods = this.getClass().getDeclaredMethods();
 				for (Method m : methods)
 					if (m.getName().equals(path))
 						m.invoke(this, request, response);
 			} else {
 				index(request, response);
 			}
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		render(request, response);
 	}
 
 	// stack: [0] getStackTrace [1] render [2] metodo que llamo a render
 	protected void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String accion = Thread.currentThread().getStackTrace()[2].getMethodName();
 		render(request, response, accion);
 	}
 	
 	// parametro            resultado
 	// accion               sitio.com/controlador/accion.jsp
 	// controlador/accion   sitio.com/controlador/accion.jsp
 	// cobb/goes/to/limbo   sitio.com/cobb/goes/to/limbo.jsp
 	protected void render(HttpServletRequest request, HttpServletResponse response, String template) throws ServletException, IOException {
 		String url = "/WEB-INF/";
 		// si es que es una accion del mismo controlador
 		if (template.indexOf("/") == -1)
 			url += getControllerPath() + "/" + template + ".jsp";
 		else // accion de otro controlador
 			url += template + ".jsp";
 		request.getRequestDispatcher(url).forward(request, response);
 	}
 	
 	protected void redirectTo(HttpServletRequest request, HttpServletResponse response, String location) throws IOException {
 		String url = request.getContextPath() + "/";
 		if (location.indexOf("/") == -1) {
 			url += getControllerPath();
 			if (location.equals("index"))
 				response.sendRedirect(url);
 			else
 				response.sendRedirect(url + "/" + location);
 		}
 		else
 			response.sendRedirect(url + location);
 	}
 	
	// app.servlets.LalalaServlet => lalala
 	protected String getControllerPath() {
 		StringBuilder url = new StringBuilder();
 		url.append(getClass().getName().toLowerCase());
 		url.replace(0, url.lastIndexOf(".") + 1, "");
 		url.replace(url.lastIndexOf("servlet"), url.length(), "");
 		return url.toString();
 	}
 	
 }
