 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
  * Copyright 2009 Laszlo Systems, Inc.  All Rights Reserved.                   *
  * Use is subject to license terms.                                            *
  * J_LZ_COPYRIGHT_END *********************************************************/
 package org.openlaszlo.media;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.List;
 import java.util.Vector;
 
 import javax.imageio.ImageIO;
 
 import org.apache.log4j.*;
 
import org.openlaszlo.utils.FileUtils;

 public class ImageMontageMaker {
     protected static Logger mLogger = org.apache.log4j.Logger.getLogger(ImageMontageMaker.class);
 
     public static int writeStrip(List files, String outfile) throws FileNotFoundException, IOException {
         return ImageMontageMaker.writeStrip(files, outfile, true);
     }
 
     public static int writeStrip(List files, String outfile, boolean isHorizontal) throws FileNotFoundException, IOException {
         return ImageMontageMaker.writeStrip(files, outfile, isHorizontal, false);
     }
 
     public static int writeStrip(List files, String outfile, boolean isHorizontal, boolean collapse) throws FileNotFoundException, IOException {
         Toolkit toolkit = Toolkit.getDefaultToolkit();
         int index = 0;
 
         int numfiles = files.size();
 
         // read all files into images array
         Image[] images = new Image[numfiles];
         // maximum size for all images
         int maxwidth = 0, maxheight = 0;
         // cumulative total size for all images
         int totalwidth = 0, totalheight = 0;
         while (index<numfiles) {
             //System.out.println(index + ":" + files.get(index));
             String infile = (String)files.get(index);
             mLogger.debug("Adding to sprite: " + infile);
 
            String extension = FileUtils.getExtension(infile);
            Image img;
            if (extension.toLowerCase().equals("png")) {
                // Have to use this instead of JAI because of bugs in PNG decoder :(
                img = toolkit.createImage(infile);
                int wait = 200;
                while (img.getHeight(null) < 0 && wait-- > 0) {
                    try {
                        Thread.sleep(10);
                    }
                    catch (Exception e) {}
                 }
            } else {
                // use JAI
                img = ImageIO.read(new File(infile));
             }
             
             int width = img.getWidth(null);
             int height = img.getHeight(null);
             totalwidth += width;
             totalheight += height;
             if (width > maxwidth) maxwidth = width;
             if (height > maxheight) maxheight = height;
 
             images[index] = img;
             index++;
 
             mLogger.debug("size: " + width + "," + height + "," + maxwidth + "," + maxheight + "," + totalwidth + "," + totalheight);
         }
 
         //use width/height to write images
         int outputwidth = maxwidth;
         int outputheight = maxheight;
         if (isHorizontal) {
             outputwidth = collapse ? totalwidth : maxwidth * numfiles;
         } else {
             outputheight = collapse ? totalheight : maxheight * numfiles;
         }
 
         mLogger.debug("Output size " + outputwidth + "," + outputheight );
         // combine into one large image
         BufferedImage finalImage = new BufferedImage(outputwidth, outputheight, BufferedImage.TYPE_INT_ARGB);
         Graphics g = finalImage.createGraphics();
 
         index = 0;
         int x = 0, y = 0; 
         while (index<numfiles) {
             Image img = (Image)images[index++];
             //g.drawImage(img, maxwidth * col, maxheight * row, maxwidth, maxheight, null);
            mLogger.debug("Drawing image" + img + " at " + x + ", " + y);
             g.drawImage(img, x, y, null);
             if (isHorizontal) {
                 if (collapse) {
                     x += img.getWidth(null);
                 } else {
                     x += maxwidth;
                 }
             } else {
                 if (collapse) {
                     y += img.getHeight(null);
                 } else {
                     y += maxheight;
                 }
             }
         }
 
         mLogger.debug("Writing css sprite to: " + outfile);
         ImageIO.write(finalImage, "png", new File(outfile));
         // return the size used to compute the offset of the image in master sprites
         return isHorizontal ? outputheight : outputwidth;
     }
 }
