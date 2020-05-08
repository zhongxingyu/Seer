 package com.angelini.fly;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class LayoutTemplater extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 	private static final String LAYOUT = "/layout/layout.html";
 	
 	private static final String COMPONENT = "<script id=\"{{id}}\" type=\"text/template\">" +
 											"	\n{{html}}\n" +
 											"</script>";
 	
 	private String folder;
 	private String layout;
 	private String compString;
 	private Authentication auth;
 	
 	public LayoutTemplater(String folder, Map<String, String> components, Authentication auth) throws IOException {
 		this.folder = folder;
 		this.layout = Utils.readFile(LAYOUT);
 		this.compString = "";
 		this.auth = auth;
 		
 		for (Map.Entry<String, String> entry : components.entrySet()) {
 			String comp = COMPONENT.replace("{{id}}", entry.getKey().split("\\.")[0]);
 			compString += comp.replace("{{html}}", entry.getValue());
 		}
 		
 		layout = layout.replace("{{components}}", compString);
 	}
 	
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (auth != null && auth.verifySignature(req) != null) {
 			resp.sendRedirect(AuthServlet.LOGIN_URL);
 			return;
 		}
 		
 		try {
 			String path = (req.getPathInfo() == "/") ? "/index.html" : req.getPathInfo();
 			String html = this.layout.replace("{{body}}", Utils.readFile(folder + path));
 			
 			resp.setStatus(200);
 			resp.getWriter().print(html);
 			
 		} catch (Exception e) {
 			resp.setStatus(404);
 		}
 	}
 	
 }
