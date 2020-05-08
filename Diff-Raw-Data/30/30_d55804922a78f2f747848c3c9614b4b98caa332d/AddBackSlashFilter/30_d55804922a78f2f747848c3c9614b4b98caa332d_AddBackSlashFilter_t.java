 package ro.zg.open_groups;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ro.zg.open_groups.gui.constants.UriFragments;
 
 public class AddBackSlashFilter implements Filter {
     private static ServletContext servletContext;
 
     @Override
     public void destroy() {
 	// TODO Auto-generated method stub
 
     }
 
     @Override
     public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException,
 	    ServletException {
 	HttpServletRequest req = (HttpServletRequest) arg0;
 	HttpServletResponse res = (HttpServletResponse) arg1;
 
 	String contextPath = req.getContextPath();
 	String requestUri = req.getRequestURI();
 //	System.out.println("context path=" + contextPath);
 	System.out.println("req uri=" + requestUri);
 //	System.out.println("req url=" + req.getRequestURL());
 	String uri = requestUri.substring(contextPath.length()).trim();
 	if (uri.equals("") || uri.equals("/")) {
 //	    res.sendRedirect(contextPath + "/");
	    RequestDispatcher dispatcher = req.getRequestDispatcher("/site/");
 	    dispatcher.forward(req, res);
 	    return;
 	}
 //	else if (uri.startsWith("/img")) {
 //	    returnImage(uri, req, res);
 //	    return;
 //	}
 //	
 //	else if(uri.startsWith("/res/")) {
 //	    RequestDispatcher dispatcher = req.getRequestDispatcher(uri);
 //	    dispatcher.forward(req, res);
 //	    return;
 //	}
 	else if(uri.startsWith("/"+UriFragments.SHOW_ENTITY)) {
 	    res.sendRedirect(contextPath+"/#"+uri.substring(1));
 //	    System.out.println("show");
 //	    RequestDispatcher dispatcher = req.getRequestDispatcher("/#"+uri.substring(1));
 //	    dispatcher.forward(req, res);
 	    return;
 	}
 	
 	arg2.doFilter(arg0, arg1);
     }
 
     private boolean isVaadinStuff(String uri) {
 	return uri.contains("VAADIN") || uri.contains("UIDL");
     }
 
     private void returnImage(String uri, HttpServletRequest req, HttpServletResponse resp) throws IOException {
 //	System.out.println("returning image " + uri);
 
 	String filePath = "VAADIN/themes/open-groupstheme" + uri;
 	String filename = servletContext.getRealPath(filePath);
 	File file = new File(filename);
 
 	if (!file.exists()) {
 	    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
 	}
 
 	// Get the MIME type of the image
 	String mimeType = servletContext.getMimeType(filename);
 	if (mimeType == null) {
 	    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
 	    return;
 	}
 
 	// Set content type
 	resp.setContentType(mimeType);
 
 	// Set content size
 
 	resp.setContentLength((int) file.length());
 
 	// Open the file and output streams
 	FileInputStream in = new FileInputStream(file);
 	OutputStream out = resp.getOutputStream();
 
 	// Copy the contents of the file to the output stream
 	byte[] buf = new byte[1024];
 	int count = 0;
 	while ((count = in.read(buf)) >= 0) {
 	    out.write(buf, 0, count);
 	}
 	in.close();
 	out.flush();
 	out.close();
     }
 
     @Override
     public void init(FilterConfig arg0) throws ServletException {
 	// TODO Auto-generated method stub
 	servletContext = arg0.getServletContext();
     }
 
 }
