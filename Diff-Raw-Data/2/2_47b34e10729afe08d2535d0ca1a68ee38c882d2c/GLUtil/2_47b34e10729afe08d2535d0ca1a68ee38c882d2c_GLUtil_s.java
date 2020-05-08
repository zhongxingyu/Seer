 package com.unknownloner.lonelib.util;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 import java.nio.IntBuffer;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 
 public class GLUtil {
 	
 	public static void exitOnError(String section) {
 		if(printError(section)) {
 			System.exit(0);
 		}
 	}
 	
 	public static boolean printError(String section) {
 		int err = GL11.glGetError();
 		if(err == 0)
 			return false;
 		String errName;
 		switch(err) {
 		case 0x500:
 			errName = "GL_INVALID_ENUM";
 			break;
 		case 0x501:
 			errName = "GL_INVALID_VALUE";
 			break;
 		case 0x502:
 			errName = "GL_INVALID_OPERATION";
 			break;
 		case 0x503:
 			errName = "GL_STACK_OVERFLOW";
 			break;
 		case 0x504:
 			errName = "GL_STACK_UNDERFLOW";
 			break;
 		case 0x505:
 			errName = "GL_OUT_OF_MEMORY";
 			break;
 		case 0x506:
 			errName = "GL_INVALID_FRAMEBUFFER_OPERATION";
 			break;
 		case 0x8031:
 			errName = "GL_TABLE_TOO_LARGE";
 			break;
 		default:
 			errName = "UNKNOWN ERROR";
 		}
 		System.err.println("An OpenGL Error Occured!");
 		System.err.println("Section: ".concat(section));
 		System.err.println("Error: ".concat(errName));
 		return true;
 	}
 	
 	public static void setNativesPath(String path) {
 		System.setProperty("org.lwjgl.librarypath", path);
 	}
 	
 	public static BufferedImage takeScreenshot() {
 		return takeScreenshot(0, 0, Display.getWidth(), Display.getHeight());
 	}
 	
 	/**
	 * Takes a screenshot of the back buffer
 	 * @param x Left x value of capture area
 	 * @param y Lower y value of capture area (0 = bottom)
 	 * @param width Width of capture area
 	 * @param height Height of capture area
 	 * @return
 	 */
 	public static BufferedImage takeScreenshot(int x, int y, int width, int height) {
 		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 		IntBuffer rgb = BufferUtil.createIntBuffer(width * height);
 		GL11.glReadPixels(x, y, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, rgb); /*Format as ARGB instead of RGBA */
 		int[] imgData = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
 		
 		//glReadPixels as 0, 0 as bottom left. BufferedImages have it as top left.
 		//This flips the image
 		for(y = height - 1; y >= 0; y--) {
 			rgb.get(imgData, y * width, width);
 		}
 		return img;
 	}
 
 }
