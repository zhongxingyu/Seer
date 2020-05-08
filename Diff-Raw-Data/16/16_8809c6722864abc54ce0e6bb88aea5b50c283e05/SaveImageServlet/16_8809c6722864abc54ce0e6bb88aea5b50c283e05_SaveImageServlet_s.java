 package it.polito.ixem.g4dom.servlet;
 
 import it.polito.ixem.g4dom.SerialManager;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import jssc.SerialPortException;
 
 public class SaveImageServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
 		try {
 			byte nodeID = Byte.parseByte(request.getParameter("nodeID"));
 			
 			SerialManager.getInstance().saveImage(nodeID);
 			
 			request.getRequestDispatcher("body/image.jsp").forward(request, response);
 		} catch (NullPointerException e) {
			
 		} catch (NumberFormatException e) {
			
 		} catch (SerialPortException e) {
			
 		}
 	}
 }
