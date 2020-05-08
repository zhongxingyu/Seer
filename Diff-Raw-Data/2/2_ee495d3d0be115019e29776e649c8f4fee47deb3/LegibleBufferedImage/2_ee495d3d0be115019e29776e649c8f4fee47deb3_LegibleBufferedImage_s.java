 
 package com.elusivehawk.engine.render;
 
 import java.awt.image.BufferedImage;
 
 /**
  * 
  * 
  * 
  * @author Elusivehawk
  */
 public class LegibleBufferedImage implements ILegibleImage
 {
 	protected final BufferedImage img;
 	protected final EnumColorFormat format;
 	
 	public LegibleBufferedImage(BufferedImage image)
 	{
 		img = image;
 		
 		EnumColorFormat f;
 		
 		switch (image.getType())
 		{
 			case BufferedImage.TYPE_3BYTE_BGR: f = EnumColorFormat.BGRA;
 			case BufferedImage.TYPE_4BYTE_ABGR: f = EnumColorFormat.ABGR;
 			case BufferedImage.TYPE_4BYTE_ABGR_PRE: f = EnumColorFormat.ABGR;
 			case BufferedImage.TYPE_INT_ARGB: f = EnumColorFormat.ARGB;
 			case BufferedImage.TYPE_INT_ARGB_PRE: f = EnumColorFormat.ARGB;
 			case BufferedImage.TYPE_INT_BGR: f = EnumColorFormat.BGRA;
 			case BufferedImage.TYPE_INT_RGB: f = EnumColorFormat.RGBA;
 			default: f = EnumColorFormat.RGBA;
 		}
 		
 		format = f;
 		
 	}
 	
 	@Override
 	public int getPixel(int x, int y)
 	{
 		return this.img.getRGB(x, y);
 	}
 	
 	@Override
 	public EnumColorFormat getFormat()
 	{
		return null;
 	}
 	
 	@Override
 	public int getHeight()
 	{
 		return this.img.getHeight();
 	}
 	
 	@Override
 	public int getWidth()
 	{
 		return this.img.getWidth();
 	}
 	
 }
