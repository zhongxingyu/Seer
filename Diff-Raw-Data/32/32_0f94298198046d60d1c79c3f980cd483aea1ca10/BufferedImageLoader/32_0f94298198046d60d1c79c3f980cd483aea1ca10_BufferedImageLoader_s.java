 /**
  *  Copyright 2013 Phantom Coder (Richard Tester)
  */
 package com.spectregames.concentration.utils;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 /**
  *  Project: Concentration
  *  
  *  Class: BufferedImageLoader
  *  
  *  @author Phantom Coder
  *	
  */
 public class BufferedImageLoader {
 	private BufferedImage image;
 	
 	public BufferedImage loadImage(String imagePath) throws IOException{
		image = ImageIO.read(this.getClass().getResourceAsStream(imagePath));
 		return image;
 	}
 
 }
