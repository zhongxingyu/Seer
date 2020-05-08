 package net.coobird.paint.io;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.imageio.ImageIO;
 
 import net.coobird.paint.image.Canvas;
 import net.coobird.paint.image.ImageLayer;
 
 /**
  * 
  * @author coobird
  *
  */
 public final class DefaultImageOutput extends ImageOutput
 {
 	/**
 	 * TODO Description.
 	 * @param c		Canvas
 	 * @param f		File.
 	 */
 	@Override
 	public void write(Canvas c, File f)
 	{
 		if (c == null || f == null)
 		{
 			throw new NullPointerException();
 		}
 		
 		try
 		{
 			FileOutputStream fos = new FileOutputStream(f);
 			ZipOutputStream zos = new ZipOutputStream(fos);
 			
 			int index = 0;
 			
 			for (ImageLayer l : c.getLayers())
 			{
 				zos.putNextEntry(new ZipEntry("" + index + ".png"));
 				ImageIO.write(l.getImage(), "png", zos);
 				zos.closeEntry();
 				index++;
 			}
 			
 			zos.finish();
 			zos.close();
 			fos.close();
 		}
 		catch (FileNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
