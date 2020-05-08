 /**
 Marvin Project <2007-2009>
 
 Initial version by:
 
 Danilo Rosetto Munoz
 Fabio Andrijauskas
 Gabriel Ambrosio Archanjo
 
 site: http://marvinproject.sourceforge.net
 
 GPL
 Copyright (C) <2007>  
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License along
 with this program; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
 
 package net.sourceforge.marvinproject.difference.differenceGray;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import marvin.gui.MarvinFilterWindow;
 import marvin.image.MarvinImage;
 import marvin.image.MarvinImageMask;
 import marvin.plugin.MarvinAbstractPluginImage;
 import marvin.util.MarvinAttributes;
 import marvin.util.MarvinErrorHandler;
 import marvin.util.MarvinFileChooser;
 
 /**
  * Absolute difference between two images considering the gray scale.
  * @author Danilo Rosetto Muoz
  * @version 1.0 03/28/2008
  */
 
 public class DifferenceGray extends MarvinAbstractPluginImage{
 
 	public void load(){}
 
 	public void show(){
 		MarvinFilterWindow l_filterWindow = new MarvinFilterWindow("Difference", 400,350, getImagePanel(), this);
 		l_filterWindow.setVisible(true);
 	}
 
 	public void process
 	(
 		MarvinImage a_imageIn, 
 		MarvinImage a_imageOut,
 		MarvinAttributes a_attributesOut,
 		MarvinImageMask a_mask, 
 		boolean a_previewMode
 	)
 	{
 		//Selects the other image to apply th difference
 		String file = null;
 		BufferedImage buffImage2=null;
 		
 		try {
 			//Open the file browser dialog
 			file = MarvinFileChooser.select(null, true, MarvinFileChooser.OPEN_DIALOG);
 		} catch (Exception e) {
 			MarvinErrorHandler.handle(MarvinErrorHandler.TYPE.ERROR_FILE_CHOOSE, e);
 			return;
 		}
 		
 		if(file == null) return;
 
 		//Loads the image to the memory and creates an MarvinImage
 		try{
 			buffImage2 =  ImageIO.read(new File(file));
 		}catch (IOException ioe) {
 			MarvinErrorHandler.handle(MarvinErrorHandler.TYPE.ERROR_FILE_OPEN, ioe);
 			return;
 		}
 
 		MarvinImage image2 = new MarvinImage(buffImage2);		
 		
 		//Gets the minimum width and height
 		int minX = Math.min(a_imageIn.getWidth(), image2.getWidth());
 		int minY = Math.min(a_imageIn.getHeight(), image2.getHeight());
 		
 		for (int x = 0; x < minX; x++) {
 			for (int y = 0; y < minY; y++) {
 				//Calculate the difference
 				
 				//Gets the gray scale value
 				int gray = (int)((a_imageIn.getRed(x, y)*0.3) + (a_imageIn.getGreen(x, y)*0.11) + (a_imageIn.getBlue(x, y)*0.59));
 				int gray1 = (int)((image2.getRed(x, y)*0.3) + (image2.getGreen(x, y)*0.11) + (image2.getBlue(x, y)*0.59));
 				
 				//Makes the absolute difference
 				int diff = Math.abs(gray - gray1);
 	            int v = (diff / 2);
 				
 	            //Sets the value to the new image
				a_imageOut.setRGB(x, y, v, v, v);
 			}
 		}
 	}
 }
