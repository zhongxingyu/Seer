 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.sql.*;
 import java.util.*;
 import oracle.sql.*;
 import oracle.jdbc.*;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import javax.imageio.ImageIO;
 import util.User;
 import util.Db;
 
 /**
  *
  *
 */
 
 import org.apache.commons.fileupload.DiskFileUpload;
 import org.apache.commons.fileupload.FileItem;
 
 public class uploadImage extends HttpServlet {
 	private Db database;
 	private HttpSession session;
 	private String owner;
 	private String location;
 	private String date;
 	private String subject;
 	private String description;
 	private int security;
 	private int image_id;
 	public String response_message;
 	
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 		 throws ServletException, IOException {
 		
 		try {
 			/*
 			 * Parse the HTTP request to get the image stream
 			 */
 			DiskFileUpload fu = new DiskFileUpload();
 			List FileItems = fu.parseRequest(request);
 			/*
 			 * Process the uploaded items, assuming 1 image file uploaded
 			 */
 			Iterator i = FileItems.iterator();
 			FileItem item = (FileItem) i.next();
 			while (i.hasNext() && item.isFormField()) {
 				item = (FileItem) i.next();
 			}
 			
 			/*
 			 * Get Image Stream
 			 */
 			InputStream instream = item.getInputStream();
 			
 			BufferedImage img = ImageIO.read(instream);
 			BufferedImage thumbNail = shrink(img, 10);
 			
 			/*
 			 *Connect to the database and create a statement
 			 */
 			database = new Db();
 			database.connect_db();
 			
 			/*
 			 * Check if user is logged in, if not redirect
 			 */		
 			response.setContentType("text/html");
 			session = request.getSession(true);
 			owner = (String) session.getAttribute("username");
 			if(owner == null) {
 				response.sendRedirect("login.jsp");
 			}
 			
 			/* 
 			 * Get inputs from owner
 			 */
 			location = request.getParameter("location");
 			date = request.getParameter("date");
 			if (date == null) {
 				date = "sysdate";
 			}
 			subject = request.getParameter("subject");
			security = request.getParameter("security").value;
 			image_id = database.execute_stmt("image_id_sequence.nextval");
 			
 	
                         /*
                          * Insert a empty blob into the table
                          */
 //			database.addEmptyImage(image_id,  owner, security, subject, location, 
 //					date, desc);
 
 			/*
 			 * Get BLOB from database
 			 */
 
 			BLOB myImage = database.getImageById(image_id);
 			BLOB myThumb = database.getThumbnailById(image_id);	
 
 
 			/*
 			 * Write thumbnail image into a BLOB object
 			 */ 
 			OutputStream outstream = myThumb.getBinaryOutputStream();
 			ImageIO.write(thumbNail, "jpg", outstream);
 			
 			/*
 			 * Write image into a BLOB object
 			 */	
 			int size = myImage.getBufferSize();
 	    		byte[] buffer = new byte[size];
 	    		int length = -1;
 	    		while ((length = instream.read(buffer)) != -1)
 				outstream.write(buffer, 0, length);			
 
 			instream.close();
 			outstream.close();
 			response_message = " File has been Uploaded!    ";
 			database.close_db();
 
 		} catch (Exception e) {
 			response_message = e.getMessage();
 		}	
 		
 		//Output response to the client
 		response.setContentType("text/html");
 		PrintWriter out = response.getWriter();
 		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
 		    	    "Transitional//EN\">\n" +
 		    	    "<HTML>\n" +
 		    	    "<HEAD><TITLE>Upload Message</TITLE></HEAD>\n" +
 		    	    "<BODY>\n" +
 		    	    "<H1>" +
 		            response_message +
 		    	    "</H1>\n" +
 		    	    "</BODY></HTML>");
 
 	}
 
 	/*
 	 * Strink function
 	 * http://www.java-tips.org/java-se-tips/java.awt.image/shrinking-an-image-by-skipping-pixels.html
 	*/
    	 public static BufferedImage shrink(BufferedImage image, int n) {
 
         	int w = image.getWidth() / n;
         	int h = image.getHeight() / n;
 
         	BufferedImage shrunkImage = new BufferedImage(w, h, image.getType());
 
         	for (int y=0; y < h; ++y)
             	for (int x=0; x < w; ++x)
                 	shrunkImage.setRGB(x, y, image.getRGB(x*n, y*n));
 
         	return shrunkImage;
     }
 }
