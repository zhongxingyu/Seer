 package rest;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 
 import javax.imageio.ImageIO;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.FileUtils;
 
 import com.sun.jersey.spi.resource.Singleton;
 
 import fr.thumbnailsdb.DuplicateGroup;
 import fr.thumbnailsdb.DuplicateMediaFinder;
 import fr.thumbnailsdb.SimilarImageFinder;
 import fr.thumbnailsdb.Test;
 import fr.thumbnailsdb.ThumbStore;
 
 @Path("/hello")
 @Singleton
 public class RestTest {
 
 	protected ThumbStore tb ; 
 	
 	public RestTest() {
 		System.out.println("RestTest.RestTest()");
 		tb = new ThumbStore();
 	}
 	
 
 	@GET
 	@Path("/{param}")
 	public Response getMsg(@PathParam("param") String msg) {
 		String output = "Jersey say : " + msg;
 		return Response.status(200).entity(output).build();
 
 	}
 	
 	@GET
 	@Path("/db/{param}")
 	public Response getSize(@PathParam("param") String info) {
 		if ("size".equals(info)) {
 			return Response.status(200).entity(tb.size()+ "").build();
 		}
         if ("path".equals(info)) {
         	return Response.status(200).entity(tb.getPath()+ "").build();
 		}
 //		System.out.println("RestTest.getSize() thumbstore is " + tb);
         return Response.status(404).build();
 	}
 	
 	
 	@GET
 	@Path("/identical")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public Response getDuplicate(@QueryParam("max") String max) {
 
 		SimilarImageFinder si = new SimilarImageFinder(tb);
 		DuplicateMediaFinder df = new DuplicateMediaFinder(tb);
 		// String source = args[1];
 	//	String r = df.prettyHTMLDuplicate(df.findDuplicateMedia(),Integer.parseInt(max));
 		Collection dc = (Collection) df.computeDuplicateSets(df.findDuplicateMedia()).toCollection();
 		DuplicateGroup dl =  df.computeDuplicateSets(df.findDuplicateMedia()).getFirst();
 //System.out.println("RestTest.getDuplicate() " + dl);
 		Test t = new Test();
 		return Response.status(200).entity(dc).build();
 	}
 
	
	
	
	

 	@GET
 	@Path("getImage/{imageId}")
 	@Produces("image/jpg")
 	public Response getImage(@PathParam(value = "imageId") String imageId) {
 		Image image = null;
 		System.out.println("RestTest.getImage() " + imageId);
 		try {
 			image = ImageIO.read(new File("/Users/fhuet/Documents/Perso/Photos/Wow Photos/IMG_3803.JPG"));
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		if (image != null) {
 			final ByteArrayOutputStream out = new ByteArrayOutputStream();
 			try {
 				ImageIO.write((BufferedImage) image, "jpg", out);
 				final byte[] imgData = out.toByteArray();
 				final InputStream bigInputStream = new ByteArrayInputStream(imgData);
 				return Response.ok(bigInputStream).build();
 			} catch (final IOException e) {
 				return Response.noContent().build();
 			}
 		}
 		return Response.noContent().build();
 	}
 
 	@POST
 	@Path("post/")
 	public Response getPostImage(String imageId) {
 	//	System.out.println("RestTest.getImage() " + imageId);
 		String[] list = new String[] { "/user/fhuet/desktop/home/NOSAVE/img-073020qbs9f.jpg",
 				"/user/fhuet/desktop/home/Perso/photos/DSC00006.JPG" };
 		String result = null;
 		String img = null;
 		for (int i = 0; i < list.length; i++) {
 			try {
 				img = "<img src=\"data:image/jpg;base64,"
 						+ Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(list[i]))) + "\"/>";
 				;
 				System.out.println("RestTest.getPostImage() " + list[i]);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			if (result == null) {
 				result = img;
 			} else {
 				result += img;
 			}
 		}
 
 		return Response.status(200).entity(result).build();
 	}

 }
