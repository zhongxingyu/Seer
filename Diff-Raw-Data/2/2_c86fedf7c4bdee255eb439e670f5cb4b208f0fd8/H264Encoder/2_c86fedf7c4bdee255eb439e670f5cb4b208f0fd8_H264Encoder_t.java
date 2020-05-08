 package org.test.streaming.encoding;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.xuggle.xuggler.ICodec;
 import com.xuggle.xuggler.IContainer;
 import com.xuggle.xuggler.IStream;
 import com.xuggle.xuggler.IStreamCoder;
 
 /**
  * 
  * @author meidis
  * 
  */
 public class H264Encoder implements Encoder {
 
 	protected static final Log log = LogFactory.getLog(H264Encoder.class);
 
 	private File originDir;
 	private File tempDir;
 	private File targetDir;
 	private String targetExtension = "mp4";
 	private String tempExtension = "temp";
 	private File buffer;
 	private int fixedVideoBitRate = 875;
 	private String audioCodec = "libfaac";
 	private String fileName;
 	private int videoBitRate;
 	private int videoHeight;
 	private int audioBitRate;
 
 	public H264Encoder(String fileName, File originDir, File targetDir) {
 		this.fileName = fileName;
 		this.originDir = originDir;
 		this.targetDir = targetDir;
 		this.tempDir = new File(originDir + "/_TEMP_ENCODING_" + fileName);
 		this.buffer = new File(tempDir, "_SAFE_TO_DELETE_ANYTIME.garbage");
 
 		File origin = new File(originDir, fileName);
 		if (!origin.exists()) {
 			throw new IllegalStateException("origin file does not exist: "
 					+ originDir + fileName);
 		}
 		File tmp = tempDir;
 		this.removeDirectory(tmp);
 		tmp.mkdirs();
 		targetDir.mkdirs();
 	}
 
 	@Override
 	public File encode() {
 
 		String inFile = new File(originDir, fileName).getAbsolutePath();
 		String[] splittedFileName = fileName.split("\\.");
 		String extension = splittedFileName[splittedFileName.length - 1];
 		String tempFile = new File(tempDir, fileName.replace(extension, tempExtension)
 				+ "." + targetExtension).getAbsolutePath();
 		String outFile = new File(targetDir, fileName.replace(extension, targetExtension)).getAbsolutePath(); 
 		
 		scanFile(inFile);
////
 		log.info("About to start encoding for inFile: " + inFile+" - buffer: " + buffer+" - outFile: " + outFile+" - videoBitRate: " + videoBitRate+" - videoHeight: " + videoHeight+" - audioCodec: " + audioCodec+" - audioBitRate: " + audioBitRate);
 
 		String firstPass = firstStep(inFile);
 		String secondPass = secondStep(inFile, tempFile);
 		String thirdPass = thirdStep(tempFile, outFile);
 				
 		File encodedFile = new File(outFile);
 		try {
 			tempDir.mkdirs();
 			launchEncoding(firstPass);
 			log.debug("About to delete buffer: " + buffer);
 			buffer.delete();
 			launchEncoding(secondPass);
 			launchEncoding(thirdPass);
 
 			this.removeDirectory(tempDir);
 
 		} catch (IOException e) {
 			log.error("Unable to encode video: " + inFile, e);
 		}
 		return encodedFile;
 	}
 
 	private String firstStep(String inFile) {
 		String step = "ffmpeg -i " + inFile
 				+ " -vcodec libx264 -vprofile high -preset slow -b:v "
 				+ videoBitRate + "k -maxrate " + videoBitRate + "k -bufsize "
 				+ videoBitRate * 2 + "k -vf scale=-1:" + videoHeight
 				+ " -threads 0 -pass 1 -an -f mp4 " + buffer;
 		log.info(step);
 		return step;
 	}
 
 	private String secondStep(String inFile, String tempFile) {
 		String step =  "ffmpeg -i " + inFile
 				+ " -vcodec libx264 -vprofile high -preset slow -b:v "
 				+ videoBitRate + "k -maxrate " + videoBitRate + "k -bufsize "
 				+ videoBitRate * 2 + "k -vf scale=-1:" + videoHeight
 				+ " -threads 0 -pass 2 -acodec " + audioCodec + " -b:a "
 				+ audioBitRate + "k -f mp4 " + tempFile;
 		log.info(step);
 		return step;
 	}
 
 	private String thirdStep(String tempFile, String outFile) {
 		String step = "qt-faststart " + tempFile + " " + outFile; 
 		log.info(step);
 		return step;
 	}
 
 	private void scanFile(String inFile) {
 
 		// Create a Xuggler container object
 		IContainer container = IContainer.make();
 		// Open up the container
 		if (container.open(inFile, IContainer.Type.READ, null) < 0)
 			throw new IllegalArgumentException("could not open file: " + inFile);
 
 		// query how many streams the call to open found
 		int numStreams = container.getNumStreams();
 		videoBitRate = fixedVideoBitRate(container.getBitRate() / 1000);
 
 		// and iterate through the streams to print their meta data
 		for (int i = 0; i < numStreams; i++) {
 			// Find the stream object
 			IStream stream = container.getStream(i);
 			// Get the pre-configured decoder that can decode this stream;
 			IStreamCoder coder = stream.getStreamCoder();
 			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
 				// "libf"+coder.getCodec().getName();
 				audioBitRate = coder.getBitRate() / 1024;
 			} else if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
 				videoHeight = coder.getHeight();
 			}
 		}
 		container.close();
 	}
 
 	private int fixedVideoBitRate(int i) {
 		return i >= fixedVideoBitRate ? fixedVideoBitRate : i;
 	}
 
 	public void launchEncoding(String proc) throws IOException {
 
 		log.debug("About to launch " + proc);
 		Process process = Runtime.getRuntime().exec(proc, new String[0],
 				tempDir);
 		InputHandler errorHandler = new InputHandler(process.getErrorStream(),
 				"Error Stream");
 		errorHandler.start();
 		InputHandler inputHandler = new InputHandler(process.getInputStream(),
 				"Output Stream");
 		inputHandler.start();
 		try {
 			process.waitFor();
 		} catch (InterruptedException e) {
 			log.error("encoding process interrupted", e);
 			throw new IOException("process interrupted");
 		}
 		log.info("exit code: " + process.exitValue());
 	}
 
 	private boolean removeDirectory(File directory) {
 
 		if (directory == null)
 			return false;
 		if (!directory.exists())
 			return true;
 		if (!directory.isDirectory())
 			return false;
 
 		String[] list = directory.list();
 
 		// Some JVMs return null for File.list() when the
 		// directory is empty.
 		if (list != null) {
 			for (int i = 0; i < list.length; i++) {
 				File entry = new File(directory, list[i]);
 
 				if (entry.isDirectory()) {
 					if (!removeDirectory(entry))
 						return false;
 				} else {
 					if (!entry.delete())
 						return false;
 				}
 			}
 		}
 		return directory.delete();
 	}
 
 }
