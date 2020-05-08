 package net.coobird.paint.io;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import net.coobird.paint.application.ApplicationUtils;
 import net.coobird.paint.image.Canvas;
 import net.coobird.paint.image.ImageLayer;
 
 /**
  * An image import filter for formats supported by the Java Image IO library.
  * @author coobird
  *
  */
 public final class JavaSupportedImageInput extends ImageInput
 {
 	static
 	{
 		if (ImageIO.getImageReadersByFormatName("png").hasNext())
 		{
 			addFilter(new ImageFileFilter(
 					"Portable Network Graphics (png)",
 					new String[]{"png"}
 					)
 			);
 		}
 		
 		if (ImageIO.getImageReadersByFormatName("jpeg").hasNext())
 		{
 			addFilter(new ImageFileFilter(
 					"JPEG Image (jpeg, jpg)",
 					new String[]{"jpg", "jpeg"}
 					)
 			);
 		}	
 	}
 	
 	public JavaSupportedImageInput()
 	{
 		this("JavaSupportedImageInput Filter");
 	}
 	
 	public JavaSupportedImageInput(String name)
 	{
 		super(name);
 	}
 	
 	/**
 	 * Reads a file form a Java supported format.
 	 * @param f				The {@code File} object to read from.
 	 */
 	@Override
 	public final Canvas read(File f)
 	{
 		BufferedImage img = null;
 		Canvas c = null;
 		
 		try
 		{
 			img = ImageIO.read(f);
 			
 			ImageLayer layer = new ImageLayer(img);
 
 			c = new Canvas(img.getWidth(), img.getHeight());
 			c.addLayer(layer);
 		}
 		catch (IOException e)
 		{
 			ApplicationUtils.showExceptionMessage(e);
 		}
 		
 		return c;
 	}
 
 	/**
 	 * Determines whether the given {@link File} object is a supported image
 	 * format by the {@code JavaSupportedImageInput} class.
 	 * @param f				The file to check whether the format is supported.
 	 */
 	@Override
 	public boolean supportsFile(File f)
 	{
 		//FIXME getReaderFileSuffixes is from Java 1.6
 		String[] suffixes = ImageIO.getReaderFileSuffixes();
 
 		String fileExtension = ImageFileFilter.getExtension(f);
 		
 		//TODO check if this next line will fail if 
 		
 		for (String suffix : suffixes)
 		{
 			if (suffix.equals(fileExtension.toLowerCase()))
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 }
