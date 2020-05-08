 package gr.ntua.image.mediachecker;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Enumeration;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import org.apache.tika.Tika;
 
 import org.im4java.core.Info;
 import org.im4java.core.InfoException;
 import org.im4java.core.IMOperation;
 import org.im4java.core.ConvertCmd;
 import org.im4java.core.IM4JavaException;
 import org.im4java.process.ArrayListOutputConsumer;
 
 import com.xuggle.xuggler.Configuration;
 import com.xuggle.xuggler.Global;
 import com.xuggle.xuggler.ICodec;
 import com.xuggle.xuggler.IContainer;
 import com.xuggle.xuggler.IStream;
 import com.xuggle.xuggler.IStreamCoder;
 
 import com.itextpdf.text.pdf.PdfReader;
 import com.itextpdf.text.pdf.parser.PdfTextExtractor;
 
 public class MediaChecker {
 
	private static final String version = "1.0 RC";
 	private static final int PALETTE_SIZE = 6;
 
 	/**
 	 * Static function to get basic information about an image file
 	 * @param  filename              filename of the query image
	 * @param  colormap              filename of the colormap to be used for creating the color palette
 	 * @return                       an ImageInfo object with the results
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 * @throws InfoException
 	 */
 	public static ImageInfo getImageInfo(String filename, String colormap) throws IOException, FileNotFoundException, InfoException, InterruptedException, IM4JavaException {
 		Info ii           = new Info(filename, true);
 		int width         = ii.getImageWidth();
 		int height        = ii.getImageHeight();
 		String format     = ii.getImageFormat();
 		String mimeType   = MediaChecker.getMimeType(filename);
 
 		ii                = new Info(filename);
 		String colorSpace = ii.getProperty("Colorspace");
 
 		// Get 6 dominant colors from the image
 		ConvertCmd cmd                 = new ConvertCmd();
 		IMOperation op                 = new IMOperation();
 		ArrayListOutputConsumer output = new ArrayListOutputConsumer();
 		op.addImage(filename);
 		op.dither("Riemersma");
 		op.remap(colormap);
 		op.format("%c");
 		op.addImage("histogram:info:-");
 		cmd.setOutputConsumer(output);
 		cmd.run(op);
 
 		// Parse output
 		ArrayList<String> lines = output.getOutput();
 		Collections.sort(lines);
 		Collections.reverse(lines);
 		Pattern p = Pattern.compile("#(?:[0-9a-fA-F]{6}){1,2}");
 		ArrayList<String> colors = new ArrayList<String>();
 
  		for (String line : lines) {
  			Matcher m = p.matcher(line);
 			if (m.find()) {
 				String clr = m.group(0).substring(1);
 				colors.add(m.group(0));
 
 				if (colors.size() >= PALETTE_SIZE)
 					break;
 			}
 		}
 
 		String[] palette = new String[colors.size()];
 		colors.toArray(palette);
 
 		return new ImageInfo(width, height, mimeType, format, colorSpace, palette);
 	}
 
 	/**
 	 * Static function to get basic information about a video file
 	 * @param  filename                 filename of the video file
 	 * @return                          a VideoInfo object with the results
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 * @throws IllegalArgumentException
 	 */
 	public static VideoInfo getVideoInfo(String filename) throws IOException, FileNotFoundException, IllegalArgumentException {
 		IContainer container = IContainer.make();
 		if (container.open(filename, IContainer.Type.READ, null) < 0)
 			throw new IllegalArgumentException("Could not open file: " + filename);
 
 		String mimeType  = MediaChecker.getMimeType(filename);
 		int numStreams   = container.getNumStreams();
 		long duration    = container.getDuration() == Global.NO_PTS ? -1 : container.getDuration() / 1000;
 		int width        = 0;
 		int height       = 0;
 		double framerate = 0.0;
 		int bitRate      = 0;
 		String codec     = "";
 
 		for (int i=0; i<numStreams; i++) {
 			IStream stream     = container.getStream(i);
 			IStreamCoder coder = stream.getStreamCoder();
 			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
 				width     = coder.getWidth();
 				height    = coder.getHeight();
 				framerate = coder.getFrameRate().getDouble();
 				codec     = coder.getCodecID().toString();
 				bitRate   = coder.getBitRate();
 				break;
 			}
 		}
 
 		return new VideoInfo(width, height, mimeType, codec.substring(9).toLowerCase(), duration, framerate, bitRate);
 	}
 
 	/**
 	 * Static function to get basic information about an audio file
 	 * @param  filename                 filename of the audio file
 	 * @return                          an AudioInfo object with the results
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 * @throws IllegalArgumentException
 	 */
 	public static AudioInfo getAudioInfo(String filename) throws IOException, FileNotFoundException, IllegalArgumentException {
 		IContainer container = IContainer.make();
 		if (container.open(filename, IContainer.Type.READ, null) < 0)
 			throw new IllegalArgumentException("Could not open file: " + filename);
 
 		String mimeType = MediaChecker.getMimeType(filename);
 		int numStreams  = container.getNumStreams();
 		long duration   = container.getDuration() == Global.NO_PTS ? -1 : container.getDuration() / 1000;
 		int channels    = 0;
 		int sampleRate  = 0;
 		int bitDepth    = 0;
 		int bitRate     = 0;
 		String codec    = "";
 
 		for (int i=0; i<numStreams; i++) {
 			IStream stream     = container.getStream(i);
 			IStreamCoder coder = stream.getStreamCoder();
 			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
 				channels   = coder.getChannels();
 				sampleRate = coder.getSampleRate();
 				bitDepth   = Integer.parseInt(coder.getSampleFormat().toString().substring(5));
 				bitRate    = coder.getBitRate();
 				codec      = coder.getCodecID().toString();
 				break;
 			}
 		}
 
 		return new AudioInfo(mimeType, codec.substring(9).toLowerCase(), duration, sampleRate, bitDepth, bitRate, channels);
 	}
 
 	/**
 	 * Function to determine if a text file is searchable.
 	 * XML, RTF, ePub and text files are by definition searchable. A PDF file is considered searchable if it contains
 	 * ANY text on any page.
 	 * @param  filename              file to be checked
 	 * @return
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 */
  	protected boolean isSearchable(String filename) throws IOException, FileNotFoundException {
 		String mimeType = MediaChecker.getMimeType(filename);
 
 		switch (mimeType) {
 			case "application/xml":
 			case "application/rtf":
 			case "application/epub":
 			case "text/plain":
 				return true;
 			case "application/pdf":
 				PdfReader reader   = new PdfReader(filename);
 				boolean searchable = false;
 
 				String page;
 				for (int i=0; i<reader.getNumberOfPages(); i++) {
 					page = PdfTextExtractor.getTextFromPage(reader, i);
 					if (page != null && !page.isEmpty()) {
 						searchable = true;
 						break;
 					}
 				}
 				reader.close();
 
 				return searchable;
 			default:
 				return false;
 		}
 	}
 
 	/**
 	 * Function to get the mime-type of a file.
 	 * @param  filename              name of the file to be checked
 	 * @return                       the mime-type
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 */
 	public static String getMimeType(String filename) throws IOException, FileNotFoundException {
 		File file         = new File(filename);
 		InputStream is    = new FileInputStream(file);
 		String mimeType   = new Tika().detect(file);
 		is.close();
 
 		return mimeType;
 	}
 
 	/**
 	 * Function to get the size of a file in bytes
 	 * @param  filename              name of the file to be checked
 	 * @return
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 */
 	public static long getFileSize(String filename) throws IOException, FileNotFoundException {
 		File file = new File(filename);
 		return file.length();
 	}
 
 	/**
 	 * Function to get the version of MediaChecker
 	 * @return
 	 */
 	public static String getVersion() {
 		return MediaChecker.version;
 	}
 }
