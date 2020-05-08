 package com.jasonwjones.jumbnail;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 import com.beust.jcommander.JCommander;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		ThumbnailApp ta = new ThumbnailApp();
 		
 		JCommander jc = new JCommander(ta);
 		try {
 			jc.parse(args);
 		} catch (Exception e) {
 			jc.usage();
 			System.exit(1);
 		}
 		
 		File inputDirectory = new File(ta.getInputFolder());
 		if (!inputDirectory.isDirectory()) {
 			System.err.println("Input folder is not a directory.");
 			System.exit(1);
 		}
 		
 		File outputDirectory = new File(ta.getOutputFolder());
 		if (!outputDirectory.isDirectory()) {
 			System.err.println("Output folder is not a directory.");
 			System.exit(1);
 		}
 		
 		File[] jpegFiles = inputDirectory.listFiles(new FilenameFilter() {
 			public boolean accept(File directory, String filename) {
 				String lowercase = filename.toLowerCase();
 				return lowercase.endsWith("jpg") || lowercase.endsWith("jpeg");
 			}
 		});
 		
 		ThumbnailHelper th = new ThumbnailHelper();
 		
 		System.out.println("System detected " + jpegFiles.length + " files.");
 		for (File image : jpegFiles) {
 			String inFile = image.getAbsolutePath();
 			String outFile = outputDirectory + File.separator + image.getName();
 			
 			File checkFile = new File(outFile);
 			
			if (ta.isSkipExisting()) {
				if (checkFile.exists()) {
					System.out.print("-");
				}				
			} else {
 				int width = ta.getWidth();
 				int height = ta.getHeight();
 				
 				th.createThumbnail(inFile, outFile, width, height);
 				System.out.print(".");
			}			
 		}

 	}
 
 }
