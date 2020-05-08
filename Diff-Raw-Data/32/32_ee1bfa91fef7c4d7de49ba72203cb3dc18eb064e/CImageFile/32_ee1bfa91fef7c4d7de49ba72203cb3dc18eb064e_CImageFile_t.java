 /*
  * Part of Fresco software under GPL licence
  * http://www.gnu.org/licenses/gpl-3.0.txt
  */
 package file;
 
 import com.sun.media.jai.codec.FileSeekableStream;
 import com.sun.media.jai.codec.ImageCodec;
 import com.sun.media.jai.codec.ImageDecoder;
 import file.filter.CFileFilter;
 import file.filter.CMultiFilter;
 import fresco.CData;
 import fresco.CImageContainer;
 import java.awt.color.ColorSpace;
 import java.awt.color.ICC_ColorSpace;
 import java.awt.color.ICC_Profile;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorConvertOp;
 import java.awt.image.IndexColorModel;
 import java.awt.image.RenderedImage;
 import java.awt.image.WritableRaster;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.media.jai.JAI;
 import javax.media.jai.LookupTableJAI;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 
 /**
  *
  * @author gimli
  */
 public class CImageFile {
 
 	private static Logger logger = Logger.getLogger(CImageFile.class.getName());
 
 	public static void openPicture() {
 		File working_path = CData.getWorkingPath();
 		File[] pictures;
 		JFileChooser fc;
 		ArrayList<CImageContainer> newImages;
 
 		fc = new JFileChooser();
 
 		fc.addChoosableFileFilter(new CMultiFilter(ImageIO.getReaderFileSuffixes()));
 		fc.setMultiSelectionEnabled(true);
 		if (working_path != null) {
 			fc.setCurrentDirectory(working_path);
 		} else {
 			fc.setCurrentDirectory(new File("."));
 		}
 		if (fc.showOpenDialog(null) != JFileChooser.CANCEL_OPTION) {
 			pictures = fc.getSelectedFiles();
 			CData.setWorkingPath(fc.getCurrentDirectory());
 
 			CImageLoader imageLoader = new CImageLoader(pictures);
 			imageLoader.addPropertyChangeListener(CData.getProgressBar());
 			imageLoader.execute();
 		} else {
 			logger.info("Wrong file format. Or no file selected.");
 		}
 	}
 
 	/**
 	 * This method do Save As ...
 	 * @param image data to store
	 * @return new name of image
 	 */
	public static String saveAsPicture(CImageContainer image) {
 		JFileChooser fc;
 		String extension;
 
 		fc = new JFileChooser();
 		String[] supportedFormats = ImageCodec.getEncoderNames(image.getImage(), null);
 
 		for (int i = 0; i < supportedFormats.length; i++) {
 			fc.addChoosableFileFilter(new CFileFilter(supportedFormats[i], ImageCodec.getCodec(supportedFormats[i]).getFormatName()));
 		}
 
 		if (CData.getWorkingPath() != null) {
 			fc.setCurrentDirectory(CData.getWorkingPath());
 		}
 
 		if (fc.showSaveDialog(null) != JFileChooser.CANCEL_OPTION) {
 			String userString = fc.getSelectedFile().getName();
 			int sepPosition = userString.lastIndexOf(".");
 
 			if (sepPosition == -1) { // no
 				extension = fc.getFileFilter().getDescription();
 			} else {
 				extension = userString.toString().substring(sepPosition + 1);
 
 				if (extension.equals("")
 						|| !fc.getFileFilter().accept(fc.getSelectedFile())) { // file extension is not of selected type
 					extension = fc.getFileFilter().getDescription();
 				} else { // valid string
 					userString = userString.substring(0, sepPosition);
 				}
 			}
 
 			CData.setWorkingPath(fc.getCurrentDirectory());
 
 			try {
 				String path = fc.getSelectedFile().getParent() + "/" + userString + "." + extension.toLowerCase();
 				JAI.create("filestore", image.getTransformedImage(CData.view), path, extension);
 			} catch (IllegalArgumentException iae) {
 				logger.info("wrong name");
 				extension = fc.getFileFilter().getDescription();
 				//JAI.create("filestore", image.getImage(), path.toString()+"."+extension, extension);
 			}
 
			logger.log(Level.INFO, "{0} has been saved.", fc.getSelectedFile());
			return userString;
 		}
 		logger.info("Image saving have been cancelled.");
		return null;
 	}
 
 	/**
 	 * Returns an ImageIcon, or null if the path was invalid.
 	 */
 	public static ImageIcon createImageIcon(String path, String description) {
 		java.net.URL imgURL = CImageFile.class.getResource(path);
 		if (imgURL != null) {
 			return new ImageIcon(imgURL, description);
 		} else {
 			logger.warning("Couldn't find file: " + path);
 			return null;
 		}
 	}
 
 	public static BufferedImage loadResource(String path) {
 		java.net.URL imgURL = CImageFile.class.getResource(path);
 		if (imgURL == null) {
 			logger.warning("Couldn't find file: " + path);
 			return null;
 		}
 		try {
 			return ImageIO.read(imgURL);
 		} catch (IOException ioe) {
 			logger.warning("Couldn't find file: " + path);
 			return null;
 		}
 	}
 
 	/**
 	 * This method uses Java Advanced Imaging package
 	 * @param path defines input file path
 	 * @return loaded image or null if format unknown
 	 * @throws IOException typicaly any problem with file loading
 	 */
 	public static BufferedImage loadImage(String path) throws IOException {
 		FileSeekableStream stream = new FileSeekableStream(new File(path));
 		String[] names = ImageCodec.getDecoderNames(stream);
 
 		if (names == null || names.length == 0) {
 			throw new IOException("Unknown file format. No data found.");
 		} else {
 			logger.info("File name: " + names[0]);
 		}
 
 		ImageDecoder dec = ImageCodec.createImageDecoder(names[0], stream, null);
 		RenderedImage src = dec.decodeAsRenderedImage();
 
 		logger.info("Image loaded.");
 
 		// create output buffered image
 		BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
 		WritableRaster writableRaster = dest.getRaster();
 
 		// we have to manage colors different way
 		if (src.getColorModel() instanceof IndexColorModel) {
 			IndexColorModel icm = (IndexColorModel) src.getColorModel();
 			byte[][] data = new byte[3][icm.getMapSize()];
 
 			icm.getReds(data[0]);
 			icm.getGreens(data[1]);
 			icm.getBlues(data[2]);
 
 			LookupTableJAI lut = new LookupTableJAI(data);
 
 			writableRaster.setRect(JAI.create("lookup", src, lut).getData());
 		} else { // images with classic bands
 			ColorSpace cs = new ICC_ColorSpace(ICC_Profile.getInstance(ICC_ColorSpace.CS_sRGB)),
 					inputSpace = src.getColorModel().getColorSpace();
 			ColorConvertOp op;
 
 			if (inputSpace.getType() == ColorSpace.TYPE_RGB && // alpha images
 					src.getColorModel().hasAlpha()) {
 
 				src.copyData(null);
 				BufferedImage imgSrc = new BufferedImage(src.getColorModel(), src.copyData(null), false, null);
 
 				op = new ColorConvertOp(cs, null);
 				op.filter(imgSrc, dest);
 
 			} else {
 
 				logger.info("Raster bands: " + src.getData().getNumBands() + " space bands: " + inputSpace.getNumComponents());
 				op = new ColorConvertOp(inputSpace, cs, null);
 				op.filter(src.getData(), writableRaster);
 			}
 		}
 
 		return dest;
 	}
 }
