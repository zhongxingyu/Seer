 package com.deuxhuithuit.ImageColorer.Console;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.awt.image.IndexColorModel;
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 
 import javax.imageio.ImageIO;
 
 import com.deuxhuithuit.ImageColorer.Core.GifImage;
 
 public final class Main {
 
 	//private static final String HEX_COLOR_FORMAT_32 = "%s%x2%x2%x2.%s"; // color is 16 bits
 	private static final String HEX_COLOR_FORMAT_16 = "%s%x%x%x.%s"; // color is 8 bits
 	//private static final String RGB_TEXT_COLOR_FORMAT = "%srgb(%d,%d,%d).%s"; // color is always 16 bits
 	//private static final String RGB_FIXED_COLOR_FORMAT = "%s%d3%d3%d3.%s"; // 16 bits here too
 
 	private static final int COLOR_FORMAT = 16; // 16 (X10) | 256 (X100)
 	//Private Const COLOR_DEPTH As Byte = 16 ' 8, 16, 24 beware! 1111 1111 / 1111 1111 / 1111 1111
 
 	private static String outputFolder = "../../output/";
 	private static String file = "../../test.gif";
 	private static Color victim;
 	private static String colorFormat = HEX_COLOR_FORMAT_16;
 	private static int stepper = 256 / COLOR_FORMAT;
 	
 	// added for travis
 	private static boolean DEBUG = false;
 
 	public static void main(String[] args) throws InterruptedException, IOException {
 		int exit = 0;
 		
 		parseArgs(args);
 
 		System.out.println("Welcome in Deux Huit Huit's ImageColorer");
 		System.out.println();
 		System.out.println("File: " + file);
 		System.out.println("Output: " + outputFolder);
 		System.out.println("Filename format " + colorFormat);
 		System.out.println();
 		System.out.println("Color format: "+ COLOR_FORMAT + " bits");
 		System.out.println("Victim " + victim);
 		
 		if (victim == null) {
 			System.err.println("No victim color found. Can not continue.");
 			System.exit(1);
 		}
 		
 		System.out.println();
 		Thread.sleep(1000);
 		System.out.print(" -> 3 -> ");
 		Thread.sleep(1000);
 		System.out.print("2 -> ");
 		Thread.sleep(1000);
 		System.out.print("1 -> ");
 		Thread.sleep(1000);
 		System.out.println(" GO!");
 		System.out.println();
 
 		Date start = new Date();
 
 		File fileInfo = new File(file);
 
 		// better file check
 		if (fileInfo != null && fileInfo.exists() && fileInfo.canRead() && fileInfo.isFile()) {
 			ProcessFile(fileInfo);
 		} else {
 			System.out.println("ERROR: File '"+ fileInfo.getAbsolutePath() +"' does not exists. Can not continue.");
 		}
 		
 		System.out.println();
 		System.out.println(String.format("Took %.3f minutes to create %d images", (new Date().getTime() - start.getTime()) / 6000.00, (int) Math.pow(COLOR_FORMAT, 3)));
 		System.out.println();
 		
 		if (DEBUG) {
 			System.out.println("Hit <Enter> to exit...");
 			exit = System.in.read();
 		}
 		
 		System.exit(exit);
 	}
 
 	private static void parseArgs(String[] args) {
 		for (String s : args){
 			if (s.equals("-v")) {
 
 			} else if (s.equals("-b")) {
 				DEBUG = true;
 				
 			} else {
 				if (s.startsWith("-f:")){
 					file = s.substring(3);
 					
 				} else if (s.startsWith("-o:")) {
 					outputFolder = s.substring(3);
 					
 				} else if (s.startsWith("-c:")) {
 					victim = GifImage.ParseColor(s.substring(3));
 				
 				} else {
 					System.out.println(String.format("Argument '%s' not valid.", s));
 				}
 			}
 		}
 	}
 
 	private static void ProcessFile(File fileInfo) throws IOException {
 		BufferedImage img = ImageIO.read(fileInfo);
 
 		for (int r = 0; r <= 128; r += stepper) {
 			for (int g = 0; g <= 32; g += stepper) {
 				for (int b = 0; b <= 32; b += stepper) {
 					tangible.RefObject<BufferedImage> tempRef_img = new tangible.RefObject<BufferedImage>(img);
 					CreateNewImage(tempRef_img, r, g, b);
 				}
 			}
 		}
 
 		//img.dispose();
 		img = null;
 	}
 
 
 	private static void CreateNewImage(tangible.RefObject<BufferedImage> refImage, int r, int g, int b) throws IOException {
 		// clone image
 		
 		// Convert to gif with new color
 		GifImage.ConverToGifImageWithNewColor(refImage, (IndexColorModel) refImage.argvalue.getColorModel(), victim, new Color(r, g, b, 255));
 
 		// Sage this gif imagerefImage
 		//tangible.RefObject<BufferedImage> tempRef_newImage = new tangible.RefObject<BufferedImage>(newImage.argvalue);
 		SaveGifImage(refImage.argvalue, r, g, b);
 		//newImage = tempRef_newImage;
 
 		// Free up resources
 		//newImage.dispose();
 		//newImage = null;
 	}
 
 	private static int sd(int n) {
 		if (n == 0) {
 			return 0;
 		}
 		return n / stepper;
 	}
 
 	private static void SaveGifImage(BufferedImage newImage, int r, int g, int b) throws IOException {
 		File directory = new File(outputFolder);
 		
 		if (!directory.exists()) {
 			directory.mkdir();
 		}
 		
 		File fileInfo = new File(String.format(colorFormat, outputFolder, sd(r), sd(g), sd(b), "gif"));
 
 		if (fileInfo.exists()) {
 			fileInfo.delete();
 		}
 
 		ImageIO.write(newImage, "gif", fileInfo);
 
 		System.out.println(String.format(" - File %s as been created!", fileInfo.getName()));
 	}
 }
