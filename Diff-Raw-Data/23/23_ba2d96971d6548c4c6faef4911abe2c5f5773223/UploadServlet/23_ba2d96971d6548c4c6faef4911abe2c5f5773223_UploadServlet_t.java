 package cyberprime.servlets;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

 
 
 public class UploadServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet{
 	 private static final long serialVersionUID = 529869125345702992L;
 	    public UploadServlet() {
 	        super();
 	    }
 
 	    @Override
 	    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	        System.out.println("SAVING>>>>>>>>>>>>>>>>>>>>");
 	        String type = request.getParameter("type");
 	        String image = request.getParameter("image");
 	        System.out.println("SAVING>>>>>>>>>>>>>>>>>>>>Type=" + type);
 	        System.out.println("SAVING>>>>>>>>>>>>>>>>>>>>data=" + image);
 	        if (type != null) {
 	            return;
 	        }
 
 	        String byteStr = image.split(",")[1];
	        try {
				byte[] bytes = Base64.decode(byteStr);
			} catch (Base64DecodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 	        System.out.println("Servlet working.");
 	//Now you have the image bytes ready to save in File/DB/....etc..
 
 	    }
 }
