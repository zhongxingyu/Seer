 package ch.hsr.bieridee.resourcehandler;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReader;
 import javax.imageio.stream.ImageInputStream;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.neo4j.server.rest.web.NodeNotFoundException;
 import org.restlet.data.MediaType;
 import org.restlet.representation.FileRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ServerResource;
 
 import ch.hsr.bieridee.config.Res;
 import ch.hsr.bieridee.exceptions.WrongNodeTypeException;
 import ch.hsr.bieridee.resourcehandler.interfaces.IDocumentResource;
 
 /**
 * Image resource.
  */
 public class ImageResource extends ServerResource implements IDocumentResource {
 
 	private long imageId;
 
 	@Override
 	public void doInit() {
 		this.imageId = Long.parseLong((String) (getRequestAttributes().get(Res.IMAGE_REQ_ATTR)));
 	}
 
 	@Override
 	public Representation retrieve() throws WrongNodeTypeException, NodeNotFoundException {
 		final FileRepresentation file = new FileRepresentation(new File("images/1.gif"), MediaType.IMAGE_GIF);
 		// InputStream is = null;
 		// try {
 		// is = new FileInputStream(file.getFile());
 		// } catch (FileNotFoundException e) {
 		// e.printStackTrace();
 		// }
 		// final String format = getFormatName(is);
 		// System.out.println("File format: " + format);
 		return file;
 	}
 
 	@Override
 	public void store(Representation rep) {
 		throw new NotImplementedException(); // TODO
 	}
 
 	@Override
 	public void remove(Representation rep) {
 		throw new NotImplementedException(); // TODO
 	}
 
 	// private static String getFormatName(Object o) {
 	// try {
 	// ImageInputStream iis = ImageIO.createImageInputStream(o);
 	// Iterator iter = ImageIO.getImageReaders(iis);
 	// if (!iter.hasNext()) {
 	// return null;
 	// }
 	//
 	// ImageReader reader = (ImageReader) iter.next();
 	// iis.close();
 	// return reader.getFormatName();
 	// } catch (IOException e) {
 	// }
 	// return null;
 	// }
 
 }
