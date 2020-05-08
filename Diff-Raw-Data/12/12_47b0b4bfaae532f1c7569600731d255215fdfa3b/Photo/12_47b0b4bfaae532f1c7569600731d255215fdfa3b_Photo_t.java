 package name.xumingjun.rest;
 
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 
 
 import name.xumingjun.rest.bean.AbstractJsonBean;
 import name.xumingjun.util.CodeBeautifier;
 import name.xumingjun.util.ConfigConstants;
 
 /**
  * handle RESTful service for Showing Photos
  * @author mingjun
  *
  */
 @Path("/photo")
 public class Photo {
 
 	public final static String LOCAL_ROOT = ConfigConstants.SHARED_DIRECTORY;
 	public final static String WWW_ROOT = ConfigConstants.SHARED_DIRECTORY.replace(ConfigConstants.LOCAL_ROOT, "/");
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public String getPhotoList(@QueryParam("path") String path) {
 		List<String> names = queryPhotoList(path);
 		List<PhotoInfo> photos = new ArrayList<PhotoInfo>(names.size());
 		for(String name: names) {
 			PhotoInfo p = new PhotoInfo();
 			p.url = name.replace(LOCAL_ROOT, WWW_ROOT);
 			p.name = name.substring(name.lastIndexOf("/")+1);
 			photos.add(p);
 		}
 		return AbstractJsonBean.gson.toJson(photos);
 	}
 
 	List<String> queryPhotoList(String path) {
 		List<String> photoNames = new LinkedList<String>();
 		InputStream in = null;
 		try {
 			ProcessBuilder pb = new ProcessBuilder("find", LOCAL_ROOT+path);
 			in = pb.start().getInputStream();
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String line = null;
 			while(null != (line= br.readLine())) {
 				if(line.matches(".+\\.(jpe?g|JPE?G|png|PNG)$"))
 					photoNames.add(line);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			CodeBeautifier.close(in);
 		}
 		return photoNames;
 	}
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
	@Path("/details")
 	public String getPhotoWithDetails(@QueryParam("photoURLs") String photoURLs) {
		List<PhotoInfo> raw =  AbstractJsonBean.arrayFromJson(photoURLs, PhotoInfo.class);
		List<PhotoInfo> photos = new ArrayList<PhotoInfo>(raw.size());
		for(PhotoInfo r: raw) {
			photos.add(generatePhotoInfo(r.url));
 		}
 		return AbstractJsonBean.gson.toJson(photos);
 	}
 
 	PhotoInfo generatePhotoInfo(String url) {
 		PhotoInfo info = new PhotoInfo();
 		info.url = url;
 		info.name = url.substring(url.lastIndexOf("/")+1);
 		String localPath = info.url.replace(WWW_ROOT, LOCAL_ROOT);
 		try {
 			BufferedImage image = ImageIO.read(new File(localPath));
 			info.width = image.getWidth();
 			info.height = image.getHeight();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return info;
 	}
 }
 class PhotoInfo {
 	String url;
 	String name;
 	Integer width;
 	Integer height;
 }
 
