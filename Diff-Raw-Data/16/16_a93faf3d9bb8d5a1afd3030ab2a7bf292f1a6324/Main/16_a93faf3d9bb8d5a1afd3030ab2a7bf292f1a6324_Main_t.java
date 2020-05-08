 ﻿package com.deuxhuithuit.ImageColorer.Console;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.IndexColorModel;
 import java.awt.image.WritableRaster;
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 
 import javax.imageio.ImageIO;
 
 import tangible.RefObject;
 
public final class Main {
 
 	private static final String HEX_COLOR_FORMAT_32 = "{0}{1:X2}{2:X2}{3:X2}.{4}"; // color is 16 bits
 	private static final String HEX_COLOR_FORMAT_16 = "{0}{1:X1}{2:X1}{3:X1}.{4}"; // color is 8 bits
 	private static final String RGB_TEXT_COLOR_FORMAT = "{0}rgb({1},{2},{3}).{4}"; // color is always 16 bits
 	private static final String RGB_FIXED_COLOR_FORMAT = "{0}{1:000}{2:000}{3:000}).{4}"; // 16 bits here too
 
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
 
 		if (fileInfo != null && fileInfo.exists() && fileInfo.canRead() && fileInfo.isFile()) {
 			ProcessFile(fileInfo);
 		} else {
 			System.out.println("ERROR: File '"+ fileInfo.getAbsolutePath() +"' does not exists. Can not continue.");
 		}
		
 		System.out.println();
 		System.out.println("Took "+ String.format("%.3f", (new java.util.Date().getTime() - start.getTime()) / 6000) + " minutes to create " + ((Math.pow(COLOR_FORMAT, 3))) + " images");
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
					victim = com.deuxhuithuit.ImageColorer.Core.GifImage.ParseColor(s.substring(3));
 				
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
 					img = tempRef_img.argvalue;
 				}
 			}
 		}
 
 		//img.dispose();
 		img = null;
 	}
 
 
 	private static void CreateNewImage(tangible.RefObject<BufferedImage> refImage, int r, int g, int b) throws IOException {
 		// http://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
 		ColorModel cm = refImage.argvalue.getColorModel();
 		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
 		WritableRaster raster = refImage.argvalue.copyData(null);
 		RefObject<BufferedImage> newImage = new tangible.RefObject<BufferedImage>( new BufferedImage(cm, raster, isAlphaPremultiplied, null) );
 
 		// Convert to gif with new color
 		com.deuxhuithuit.ImageColorer.Core.GifImage.ConverToGifImageWithNewColor(newImage, (IndexColorModel) refImage.argvalue.getColorModel(), victim, new Color(r, g, b, 255));
 
 		// Sage this gif image
 		tangible.RefObject<BufferedImage> tempRef_newImage = new tangible.RefObject<BufferedImage>(newImage.argvalue);
 		SaveGifImage(tempRef_newImage, r, g, b);
 		newImage = tempRef_newImage;
 
 		// Free up resources
 		//newImage.dispose();
 		newImage = null;
 	}
 
 	private static int sd(int n) {
 		if (n == 0) {
 			return 0;
 		}
 		return n / stepper;
 	}
 
 	private static void SaveGifImage(tangible.RefObject<BufferedImage> newImage, int r, int g, int b) throws IOException {
 		File directory = new java.io.File(outputFolder);
 		
 		if (! (directory.exists())) {
 			directory.mkdir();
 		}
 		
 		File fileInfo = new java.io.File(String.format(colorFormat, outputFolder, sd(r), sd(g), sd(b), "gif"));
 
 		if (fileInfo.exists()) {
 			fileInfo.delete();
 		}
 
 		ImageIO.write(newImage.argvalue, "gif", fileInfo);
 
 		System.out.println(String.format(" - File %s as been created!", fileInfo.getName()));
 	}
 }
