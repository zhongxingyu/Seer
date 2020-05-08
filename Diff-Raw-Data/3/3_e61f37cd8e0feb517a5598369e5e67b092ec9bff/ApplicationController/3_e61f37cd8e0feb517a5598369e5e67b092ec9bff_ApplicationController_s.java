 package app.servlets;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.lang.reflect.*;
 
 public class ApplicationController extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		try {
 			runMethod(request, response);
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
 	
 	protected Method runMethod(HttpServletRequest request, HttpServletResponse response) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ServletException, IOException {
 		Method[] methods = this.getClass().getDeclaredMethods();
 		if (request.getPathInfo() != null && !request.getPathInfo().equals("/")) {
 			for (Method m : methods) {
				if (("/" + m.getName()).equals(request.getPathInfo())) {
 					m.invoke(this, request, response);
 					return m;
 				}
 			}
 		} else {
 			index(request, response);
 		}
 		return null;
 	}
 
 	protected void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		render(request, response, "index");
 	}
 	
 	// template = algo => sitio.com/servlet/algo
 	protected void render(HttpServletRequest request, HttpServletResponse response, String template) throws ServletException, IOException {
 		String url = "/WEB-INF/" + getUrlPath() + "/" + template + ".jsp";
 		request.getRequestDispatcher(url).forward(request, response);
 	}
 	
 	protected String getUrlPath() {
 		StringBuilder url = new StringBuilder();
 		url.append(getClass().getName().toLowerCase());
 		url.replace(0, url.lastIndexOf(".") + 1, "");
 		url.replace(url.lastIndexOf("servlet"), url.length(), "");
 		return url.toString();
 	}
 	
 }
