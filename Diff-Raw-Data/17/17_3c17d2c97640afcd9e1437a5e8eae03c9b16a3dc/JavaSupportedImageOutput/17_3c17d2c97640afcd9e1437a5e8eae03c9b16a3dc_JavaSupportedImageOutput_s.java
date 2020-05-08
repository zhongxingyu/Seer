 package net.coobird.paint.io;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import net.coobird.paint.application.ApplicationUtils;
 import net.coobird.paint.image.Canvas;
 import net.coobird.paint.image.ImageRendererFactory;
 
 /**
  * 
  * @author coobird
  *
  */
 public final class JavaSupportedImageOutput extends ImageOutput
 {
 	static
 	{
 		if (ImageIO.getImageWritersByFormatName("png").hasNext())
 		{
 			addFilter(new ImageFileFilter(
 					"Portable Network Graphics (png)",
 					new String[]{"png"}
 					)
 			);
 		}
 		
 		if (ImageIO.getImageWritersByFormatName("jpeg").hasNext())
 		{
 			addFilter(new ImageFileFilter(
 					"JPEG Image (jpeg, jpg)",
 					new String[]{"jpg", "jpeg"}
 					)
 			);
 		}
 		
 		if (ImageIO.getImageWritersByFormatName("bmp").hasNext())
 		{
 			addFilter(new ImageFileFilter(
 					"Bitmap Image (bmp)",
 					new String[]{"bmp"}
 					)
 			);
 		}
 	}
 	
 	public JavaSupportedImageOutput()
 	{
 		this("JavaSupportedImageOutput Filter");
 	}
 	
 	public JavaSupportedImageOutput(String name)
 	{
 		super(name);
 	}
 	
 	@Override
 	public void write(Canvas c, File f)
 	{
 		/*
 		 * Determine format string from file extension.
 		 */
 		write(c, f, ImageFileFilter.getExtension(f));
 	}
 	
 	@Override
 	public void write(Canvas c, File f, String format)
 	{
 		/*
 		 * Note: saving as JPEG causes image to look over saturated.
 		 * TODO investigate cause and find mitigation measures.
 		 */
 		try
 		{
 			BufferedImage img = ImageRendererFactory.getInstance().render(
 					c,
 					false
 			);
 			
 			if (
 					format.equals("jpeg") ||
 					format.equals("jpg") ||
 					format.equals("bmp")
 			)
 			{
 				BufferedImage reducedImg = new BufferedImage(
 						img.getWidth(),
 						img.getHeight(),
 						BufferedImage.TYPE_INT_RGB
 				);
 				
 				Graphics2D g = reducedImg.createGraphics();
 				g.setColor(Color.white);
 				g.fillRect(0, 0, img.getWidth(), img.getHeight());
 				g.drawImage(img, 0, 0, null);
 				g.dispose();
 				
 				img = reducedImg;
 			}
 			
 			ImageIO.write(img, format, f);
 		}
 		catch (IOException e)
 		{
 			ApplicationUtils.showExceptionMessage(e);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.coobird.paint.io.ImageOutput#supportsFile(java.io.File)
 	 */
 	@Override
 	public boolean supportsFile(File f)
 	{
 		//FIXME getWriterFileSuffixes is from Java 1.6
 		String[] suffixes = ImageIO.getWriterFileSuffixes();
 
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
