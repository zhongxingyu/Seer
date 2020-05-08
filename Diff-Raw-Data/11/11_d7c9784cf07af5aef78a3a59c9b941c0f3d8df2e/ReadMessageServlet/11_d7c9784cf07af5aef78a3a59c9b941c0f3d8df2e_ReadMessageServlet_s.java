 package se.ltu.M7017E.lab3;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class ReadMessageServlet extends HttpServlet {
 
 	private static final long serialVersionUID = -7183074843178240815L;
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// Audio player
 		response.setContentType("audio/ogg");
 		response.setStatus(HttpServletResponse.SC_OK);
 		// get the file to read
		File file = new File("/tmp/sip-voicemail/"
 				+ request.getParameter("username") + "/"
 				+ request.getParameter("name"));
 		FileInputStream message = new FileInputStream(file);
 		// read in browser
 		response.setCharacterEncoding(null);
 		response.setContentLength((int) file.length());
 
 		int readbyte;
 		while ((readbyte = message.read()) != -1) {
 			response.getOutputStream().write(readbyte);
 		}
 		message.close();
 	}
 }
