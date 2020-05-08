 package com.bh.game.graphics;
 
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class Image {
 	public BufferedImage image;
 	public int[] pixels;
 	
 	public int w;
 	public int h;
 	
 	public Image(String path) {
 		try {
 			image = ImageIO.read(Image.class.getResourceAsStream(path));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		this.w = image.getWidth();
 		this.h = image.getHeight();
 		
 		pixels = image.getRGB(0, 0, w, h, null, 0, w);
 		
 		for (int i = 0; i < pixels.length; i++) {
 			pixels[i] += 0x1000000;
 		}
 	}
 	
 	public Image(BufferedImage b) {
 		image = b;
 		this.w = image.getWidth();
 		this.h = image.getHeight();
 		pixels = image.getRGB(0, 0, w, h, null, 0, w);
 		for (int i = 0; i < pixels.length; i++) {
 			pixels[i] += 0x1000000;
 		}
 	}
 	
	public static double getAngle(int x, int y, int x1, int y1) {
	    double dx = x1 - x;
	    double dy = y1 - y;
 	    double inRads = Math.atan2(dy,dx);
	    return inRads;
 	}
 	public static BufferedImage Rotate(BufferedImage i, double rotation) {
 		rotation = Math.toRadians(rotation);
 		AffineTransform tx = AffineTransform.getRotateInstance(rotation,
 				i.getWidth() / 2, i.getHeight() / 2);
 		AffineTransformOp op = new AffineTransformOp(tx,
 				AffineTransformOp.TYPE_BILINEAR);
 		i = op.filter(i, null);
 		return i;
 	}
 
 }
