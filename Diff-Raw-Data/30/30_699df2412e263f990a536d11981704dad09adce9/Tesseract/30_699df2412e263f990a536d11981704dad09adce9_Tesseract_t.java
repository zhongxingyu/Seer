 package org.dstricks;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
 import java.util.UUID;
 
 import org.apache.commons.io.FileUtils;
 
 /**
  * Usage:tesseract imagename outputbase [-l lang] [-psm pagesegmode] [configfile...]
 pagesegmode values are:
 0 = Orientation and script detection (OSD) only.
 1 = Automatic page segmentation with OSD.
 2 = Automatic page segmentation, but no OSD, or OCR
 3 = Fully automatic page segmentation, but no OSD. (Default)
 4 = Assume a single column of text of variable sizes.
 5 = Assume a single uniform block of vertically aligned text.
 6 = Assume a single uniform block of text.
 7 = Treat the image as a single text line.
 8 = Treat the image as a single word.
 9 = Treat the image as a single word in a circle.
 10 = Treat the image as a single character.
 -l lang and/or -psm pagesegmode must occur before anyconfigfile.
  * @author david
  *
  */
 public class Tesseract {
 	public static String process(String imagename) throws IOException, InterruptedException {
 		return process(imagename, null);
 	}
 	
 	public static String process(String imagename, String language) throws IOException, InterruptedException {
 		// TODO: throw an exception or something if tesseract isn't set as a system property properly
 		String tesseractPath = System.getProperty("tesseract");
 		
 		String id = UUID.randomUUID().toString();
 		
		List<String> args = new ArrayList<String>();
		args.add(tesseractPath);
		args.add(imagename);
		args.add(id);

		if(language != null) {
			args.add("-l " + language);
 		}

		ProcessBuilder builder = new ProcessBuilder(args);
		Process p = builder.start();

 		p.waitFor();
 		
 		// TODO: check whether the file exists or not... if not then no text was extracted??
 		File result = new File(id + ".txt");
 		String text = FileUtils.readFileToString(result);
 		result.delete();
 		
 		return text;
 	}
 	
 	public static String process(InputStream image) throws IOException, InterruptedException {
 		File tempFile = File.createTempFile("tesseract_", ".tmp");
 		FileUtils.copyInputStreamToFile(image, tempFile);
 		
 		String text = process(tempFile.getPath());
 		
 		tempFile.delete();

 		return text;
 	}
 }
