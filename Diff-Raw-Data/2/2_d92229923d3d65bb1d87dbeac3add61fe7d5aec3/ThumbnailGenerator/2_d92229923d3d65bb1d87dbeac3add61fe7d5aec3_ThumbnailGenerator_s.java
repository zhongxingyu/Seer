 /* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
 
 package org.infoglue.cms.util.graphics;
 
 import java.awt.*;
 import java.awt.image.*;
 import java.io.*;
 import org.infoglue.cms.util.CmsLogger;
 import org.infoglue.cms.util.CmsPropertyHandler;
 
 public class ThumbnailGenerator
 {
     public ThumbnailGenerator()
     {
     }
    
     private void execCmd(String command) throws Exception
     {
 		CmsLogger.logSevere(command);
 		String line;
 		Process p = Runtime.getRuntime().exec(command);
 		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
 		
 		while ((line = input.readLine()) != null)
 		{
 		    CmsLogger.logSevere(line);
 		}
 		input.close();
     }
    
     public void transform(String originalFile, String thumbnailFile, int thumbWidth, int thumbHeight, int quality) throws Exception
     {
 		Image image = javax.imageio.ImageIO.read(new File(originalFile));
 		
 		double thumbRatio = (double)thumbWidth / (double)thumbHeight;
 		int imageWidth    = image.getWidth(null);
 		int imageHeight   = image.getHeight(null);
 		double imageRatio = (double)imageWidth / (double)imageHeight;
 		if (thumbRatio < imageRatio)
 		{
 		    thumbHeight = (int)(thumbWidth / imageRatio);
 		}
 		else
 		{
 		    thumbWidth = (int)(thumbHeight * imageRatio);
 		}
 		
 		if(imageWidth < thumbWidth && imageHeight < thumbHeight)
 		{
 		    thumbWidth = imageWidth;
 		    thumbHeight = imageHeight;
 		}
 		else if(imageWidth < thumbWidth)
 		    thumbWidth = imageWidth;
 		else if(imageHeight < thumbHeight)
 		    thumbHeight = imageHeight;
 		
 		if(thumbWidth < 1)
 		    thumbWidth = 1;
 		if(thumbHeight < 1)
 		    thumbHeight = 1;
 		    
		if(CmsPropertyHandler.getProperty("externalThumbnailGeneration") != null)
 		{
 		    String[] args = new String[5];
 		    
 		    args[0] = CmsPropertyHandler.getProperty("externalThumbnailGeneration");
 		    args[1] = "-resize";
 		    args[2] = String.valueOf(thumbWidth) + "x" + String.valueOf(thumbHeight);
 		    args[3] = originalFile;
 		    args[4] = thumbnailFile;
 		    
 		    try
 		    {
 		        Process p = Runtime.getRuntime().exec(args);
 		        p.waitFor();
 		    }
 		    catch(InterruptedException e)
 		    {
 		        new Exception("Error resizing image for thumbnail", e); 
 		    }		    
 		}
 		else
 		{
 		    BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
 		    Graphics2D graphics2D = thumbImage.createGraphics();
 		    graphics2D.setBackground(Color.WHITE);
 		    graphics2D.setPaint(Color.WHITE);
 		    graphics2D.fillRect(0, 0, thumbWidth, thumbHeight);
 		    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 		    graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
 		    
 		    javax.imageio.ImageIO.write(thumbImage, "JPG", new File(thumbnailFile));
 		}
 	}
 }
