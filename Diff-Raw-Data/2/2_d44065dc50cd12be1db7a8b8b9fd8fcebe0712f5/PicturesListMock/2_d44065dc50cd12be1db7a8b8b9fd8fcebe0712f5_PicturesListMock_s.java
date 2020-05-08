 package mock;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import models.Picture;
 
 import org.apache.commons.io.IOUtils;
 
 import play.Logger;
 
 public class PicturesListMock {
 
 	public static Map<String, Picture> pictures = new HashMap<String, Picture>();
 
 	static {
 		// load an image
 		InputStream picture1AsStream = PicturesListMock.class
 				.getResourceAsStream("/mock/fnac.jpg");
 
 		if (picture1AsStream != null) {
 			try {
 				Logger.debug("Loading mock image");
 				byte[] bytes = IOUtils.toByteArray(picture1AsStream);
 
				addPicture("67890", bytes, "image/jpg");
 
 			} catch (IOException e) {
 				Logger.error("Error loading an image", e);
 			}
 		} else {
 			Logger.error("Image not found");
 		}
 
 		InputStream picture2AsStream = PicturesListMock.class
 				.getResourceAsStream("/mock/caissiere.jpg");
 
 		if (picture2AsStream != null) {
 			try {
 				Logger.debug("Loading mock image");
 				byte[] bytes = IOUtils.toByteArray(picture2AsStream);
 
 				addPicture("13579", bytes, "image/jpg");
 
 			} catch (IOException e) {
 				Logger.error("Error loading an image", e);
 			}
 		} else {
 			Logger.error("Image not found");
 		}
 
 	}
 
 	public static Picture addPicture(String id, byte[] bytes, String contentType) {
 		Picture picture = new Picture(id, bytes, contentType);
 
 		pictures.put(id, picture);
 
 		return picture;
 	}
 
 	public static Picture findPicture(String id) {
 
 		return pictures.get(id);
 	}
 
 }
