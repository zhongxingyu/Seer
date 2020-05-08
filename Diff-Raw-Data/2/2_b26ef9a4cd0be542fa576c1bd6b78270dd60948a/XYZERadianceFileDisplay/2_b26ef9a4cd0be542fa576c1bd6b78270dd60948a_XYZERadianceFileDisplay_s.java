 /**
  * 
  */
 package ca.eandb.jmist.framework.display;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 
 import ca.eandb.jdcp.JdcpUtil;
 import ca.eandb.jdcp.job.HostService;
 import ca.eandb.jmist.framework.Display;
 import ca.eandb.jmist.framework.Raster;
 import ca.eandb.jmist.framework.color.CIEXYZ;
 import ca.eandb.jmist.framework.color.Color;
 import ca.eandb.jmist.framework.color.ColorModel;
 import ca.eandb.jmist.framework.loader.radiance.RadiancePicture;
 import ca.eandb.jmist.framework.loader.radiance.RadiancePicture.Format;
 import ca.eandb.util.UnexpectedException;
 
 /**
  * A <code>Display</code> that writes a radiance HDR image file using the XYZE
  * pixel format.
  * @author Brad Kimmel
  */
 public final class XYZERadianceFileDisplay implements Display, Serializable {
 
 	/** Serialization version ID. */
 	private static final long serialVersionUID = 7431366666341133926L;
 	
 	/** Default filename. */
 	private static String DEFAULT_FILENAME = "output.hdr";
 
 	/** The name of the file to write. */
 	private final String fileName;
 	
 	/** The <code>RadiancePicture</code> image to write. */
 	private transient RadiancePicture picture;
 	
 	/**
	 * Creates a new <code>RGBERadianceFileDisplay</code>.
 	 * @param fileName The name of the file to write.
 	 */
 	public XYZERadianceFileDisplay(String fileName) {
 		this.fileName = fileName;
 	}
 	
 	/**
 	 * Creates a new <code>XYZERadianceFileDisplay</code>.
 	 */
 	public XYZERadianceFileDisplay() {
 		this(DEFAULT_FILENAME);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.Display#initialize(int, int, ca.eandb.jmist.framework.color.ColorModel)
 	 */
 	@Override
 	public void initialize(int w, int h, ColorModel colorModel) {
 		picture = new RadiancePicture(w, h, Format.XYZE);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.Display#fill(int, int, int, int, ca.eandb.jmist.framework.color.Color)
 	 */
 	@Override
 	public void fill(int x, int y, int w, int h, Color color) {
 		CIEXYZ xyz = color.toXYZ();
 		for (int j = y; j < y + h; j++) {
 			for (int i = x; i < x + w; i++) {
 				picture.setPixelXYZ(i, j, xyz);
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.Display#setPixel(int, int, ca.eandb.jmist.framework.color.Color)
 	 */
 	@Override
 	public void setPixel(int x, int y, Color pixel) {
 		picture.setPixelXYZ(x, y, pixel.toXYZ());
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.Display#setPixels(int, int, ca.eandb.jmist.framework.Raster)
 	 */
 	@Override
 	public void setPixels(int x, int y, Raster pixels) {
 		for (int j = 0, h = pixels.getHeight(); j < h; j++) {
 			for (int i = 0, w = pixels.getWidth(); i < w; i++) {
 				picture.setPixelXYZ(x + i, y + j, pixels.getPixel(i, j).toXYZ());
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.Display#finish()
 	 */
 	@Override
 	public void finish() {
 		HostService service = JdcpUtil.getHostService();
 		try {
 			FileOutputStream os = (service != null) ? service
 					.createFileOutputStream(fileName) : new FileOutputStream(
 					fileName);
 					
 			picture.write(os);
 		} catch (IOException e) {
 			throw new UnexpectedException(e);
 		}
 	}
 
 }
