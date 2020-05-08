 package plotter.pdf;
 
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import org.apache.commons.io.IOUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import plotter.util.Configuration;
 
 import com.lowagie.text.pdf.PdfReader;
 
 public class PrintJob extends File {
 
 	private static final long serialVersionUID = 1578101753406396888L;
 
 	/*
 	 * Define max. thumbnail dimensions
 	 */
 	private final int MAX_HEIGHT = 120;
 	private final int MAX_WIDTH = 120;
 
 	private String filename;
 	private int numberOfPages;
 	private String printSize;
 	private int copies;
 
 	List<File> thumbnails = new ArrayList<File>();
 
 	public PrintJob(String filename, String originalFileName) throws IOException {
 		super(filename);
 
 		this.filename = originalFileName;
 
 		this.setNumberOfPages(getPageCount());
 	}
 
 	/**
 	 * Use iText to retrieve the number of pages of the PDF
 	 * 
 	 * @return the number of pages
 	 * @throws IOException
 	 */
 	private int getPageCount() throws IOException {
 		int pageCount = 0;
 
 		ByteArrayInputStream bais = null;
 		PdfReader reader = null;
 
 		try {
 			reader = new PdfReader(new FileInputStream(this));
 
 			pageCount = reader.getNumberOfPages();
 		} catch (Exception e) {
 			throw new IOException("Could not retrieve page count.", e);
 		} finally {
 			if (reader != null)
 				reader.close();
 			IOUtils.closeQuietly(bais);
 		}
 
 		return pageCount;
 	}
 
 	/**
 	 * Convert PDF to images
 	 * 
 	 * @param resolution
 	 *            the resolution in dpo
 	 * @return a list of converted images
 	 * @throws IOException
 	 */
 	private List<Image> convertToImages(int resolution) throws IOException {
 		// Get temporary file names for images
 		File tmp = File.createTempFile("plotter_%d_", ".png");
 
 		ArrayList<String> command = new ArrayList<String>();
 
 		// Build command
 		command.add(Configuration.getProperty("plotter.ghostscript.executable"));
 		command.add("-dQUIET");
 		command.add("-dNOPAUSE");
 		command.add("-dBATCH");
 		command.add("-dSAFER");
 		command.add("-r" + resolution);
 		command.add("-sDEVICE=png16m");
		command.add("-dTextAlphaBits=4");
 		command.add("-dGraphicsAlphaBits=4");
 		command.add("-sOutputFile=" + tmp.getAbsolutePath());
 		command.add(this.getAbsolutePath());
 
 		ProcessBuilder processBuilder = new ProcessBuilder(command);
 		Process proc = processBuilder.start();
 
 		int exitCode;
 		try {
 			exitCode = proc.waitFor();
 		} catch (InterruptedException e) {
 			throw new IOException("PDF to image conversion interrupted.", e);
 		}
 
 		if (exitCode != 0) {
 			String errorMessage = IOUtils.toString(proc.getErrorStream())
 					+ " - " + IOUtils.toString(proc.getInputStream());
 
 			throw new IOException(
 					"PDF could not be converted to images, message: "
 							+ errorMessage);
 		}
 
 		// Retrieve images
 		List<Image> images = new ArrayList<Image>();
 		for (int i = 1; i <= this.getNumberOfPages(); i++) {
 			String imageFilename = tmp.getAbsolutePath().replace("%d",
 					Integer.toString(i));
 
 			images.add(ImageIO.read(new File(imageFilename)));
 		}
 
 		tmp.delete();
 
 		return images;
 	}
 
 	/**
 	 * Generate 72dpi thumbnails
 	 * 
 	 * @throws IOException
 	 */
 	public void generateThumbnails() throws IOException {
 		List<Image> images = convertToImages(72);
 
 		for (int i = 0; i < images.size(); i++) {
 			// Retrieve a temporary file
 			File tmp = File.createTempFile(
 					"plotter-thumbnail" + i + "_" + this.getName(), ".png");
 			tmp.deleteOnExit();
 
 			RenderedImage image = (RenderedImage) images.get(i);
 
 			// compute thumbnail dimensions
 			int width = MAX_WIDTH;
 			int height = MAX_HEIGHT;
 			float scale_width = (float) MAX_WIDTH / (float) image.getWidth();
 			float scale_height = (float) MAX_HEIGHT / (float) image.getHeight();
 			if (scale_width < scale_height)
 				height = Math.round(image.getHeight() * scale_width);
 			else
 				width = Math.round(image.getWidth() * scale_height);
 
 			// scale image to thumbnail
 			Image scaledImage = ((Image) image).getScaledInstance(width,
 					height, BufferedImage.SCALE_SMOOTH);
 			BufferedImage scaledBufferedImage = new BufferedImage(width,
 					height, BufferedImage.TYPE_INT_RGB);
 			scaledBufferedImage.getGraphics()
 					.drawImage(scaledImage, 0, 0, null);
 
 			// Write image
 			ImageIO.write(scaledBufferedImage, "png", tmp);
 			thumbnails.add(tmp);
 		}
 	}
 
 	public String getFilename() {
 		return filename;
 	}
 
 	public List<File> getThumbnails() {
 		return thumbnails;
 	}
 
 	public int getNumberOfPages() {
 		return numberOfPages;
 	}
 
 	public void setNumberOfPages(int numberOfPages) {
 		this.numberOfPages = numberOfPages;
 	}
 
 	public String getPrintSize() {
 		return printSize;
 	}
 
 	public void setPrintSize(String printSize) {
 		this.printSize = printSize;
 	}
 
 	public int getCopies() {
 		return copies;
 	}
 
 	public void setCopies(int copies) {
 		this.copies = copies;
 	}
 
 	public JSONObject toJSON() throws JSONException {
 		return new JSONObject().put("name", this.getFilename()).put("pages",
 				this.getNumberOfPages());
 	}
 }
