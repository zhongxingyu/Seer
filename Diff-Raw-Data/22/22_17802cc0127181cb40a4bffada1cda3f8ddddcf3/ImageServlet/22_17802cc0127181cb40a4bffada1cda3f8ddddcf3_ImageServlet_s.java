 package net.bugsquat.diservlet;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * An image serving servlet. This servlet looks for a URL parameter 'image' and looks up
  * the image based on its value.
  *
  */
 public class ImageServlet extends HttpServlet {
 
	private final String IMAGE_KEY_PARAM = "iid";
 	private ImageStore store;
 	
 	@Override
 	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		store = new DefaultImageStore();
 	}
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		
		String imageKey = request.getParameter(IMAGE_KEY_PARAM);
 	    if (imageKey == null || !store.contains(imageKey)) {
 	    	return;
 	    }
 	    
 	    StoredImage image = store.retrieveImage(imageKey);
 	    response.setContentType(image.getMimeType());
	    response.setContentLength((int)imageKey.length());
 
 	    OutputStream out = response.getOutputStream();
 	    out.write(image.getImageBytes());
 	    out.close();
 	}
 	
 	public void addImageToStore(String key, StoredImage image) {
		store.storeImage(key, image);
 	}
 
 }
