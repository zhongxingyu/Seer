 package net.coobird.paint.io;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 
 import javax.imageio.ImageIO;
 import javax.imageio.ImageWriter;
 
 import net.coobird.paint.image.Canvas;
 import net.coobird.paint.image.ImageRendererFactory;
 
 /**
  * 
  * @author coobird
  *
  */
 public class JavaSupportedImageOutput extends ImageOutput
 {
 
 	@Override
 	public void write(Canvas c, File f)
 	{
 		try
 		{
 			ImageIO.write(
 					ImageRendererFactory.getInstance().render(c),
					getExtension(f),
 					f
 			);
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
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
 		
 		//TODO check if this next line will fail if 
 		
 		for (String suffix : suffixes)
 		{
 			if (suffix.equals(getExtension(f)))
 			{
 				System.out.println(suffix);
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Gets the file extension of the given {@link File} object.
 	 * @param f				The {@code File} object to determine the extension
 	 * 						for.
 	 * @return				The file extension.
 	 */
 	private String getExtension(File f)
 	{
 		int lastIndex = f.getName().lastIndexOf('.');
 		
 		if (lastIndex == -1)
 		{
 			return "";
 		}
 		
 		return f.getName().substring(lastIndex + 1);
 	}
 }
