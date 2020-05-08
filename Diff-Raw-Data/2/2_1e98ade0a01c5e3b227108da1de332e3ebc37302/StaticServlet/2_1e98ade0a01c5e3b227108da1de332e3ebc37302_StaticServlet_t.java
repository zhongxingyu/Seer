 /**
  * 
  */
 package org.jboss.test.faces.staging;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * @author asmirnov
  *
  */
 @SuppressWarnings("serial")
 public class StaticServlet extends HttpServlet {
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
		InputStream inputStream = getServletContext().getResourceAsStream(req.getPathInfo());
 		if(null != inputStream){
 			String fileName = req.getServletPath();
 			String mimeType = getServletContext().getMimeType(fileName);
 			if(null == mimeType){
 				mimeType = "text/plain";
 			}
 			resp.setContentType(mimeType);
 			ServletOutputStream outputStream = resp.getOutputStream();
 			int c;
 			while((c = inputStream.read())>0){
 				outputStream.write(c);
 			}
 			inputStream.close();
 			outputStream.close();
 		} else {
 			resp.sendError(404, "not found");
 		}
 	}
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		doGet(req, resp);
 	}
 }
