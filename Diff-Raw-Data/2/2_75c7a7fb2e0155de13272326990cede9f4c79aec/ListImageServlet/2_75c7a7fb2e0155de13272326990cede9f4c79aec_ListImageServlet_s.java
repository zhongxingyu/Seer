 package it.polito.ixem.g4dom.servlet;
 
 import it.polito.ixem.g4dom.PropertyManager;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Comparator;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @WebServlet("/image/list")
 public class ListImageServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	private static final Logger logger = LoggerFactory.getLogger(ListImageServlet.class);
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String photosDirName = PropertyManager.getProperty("photos.dir");
 		if (photosDirName != null) {
 			logger.debug("Property photos.dir exists and is not null...");
 			
 			File photosDir = new File(photosDirName);
 			if (photosDir.isDirectory()) {
 				logger.debug("Photos directory exists and is a directory...");
 				
 				File[] photos = photosDir.listFiles(new FilenameFilter() {
 
 					@Override
 					public boolean accept(File dir, String name) {
 						if (name.endsWith(".jpg") || name.endsWith(".JPG") || name.endsWith(".jpeg") || name.endsWith(".JPEG"))
 							return true;
 						return false;
 					}
 				});
 				logger.debug("Found " + photos.length + " JPEG photos in folder...");
 				
 				Arrays.sort(photos, new Comparator<File>() {
 
 					@Override
 					public int compare(File o1, File o2) {
 						// TODO Auto-generated method stub
						return Long.valueOf(o1.lastModified()).compareTo(o2.lastModified());
 					}
 				});
 				
 				request.setAttribute("photos", photos);
 				request.getRequestDispatcher("/body/photo_list.jsp").include(request, response);
 				return;
 			}
 		}
 		response.sendError(500);
 	}
 }
