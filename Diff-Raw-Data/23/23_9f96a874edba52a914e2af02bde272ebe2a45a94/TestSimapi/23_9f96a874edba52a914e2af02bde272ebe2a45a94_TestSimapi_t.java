 /*
  * File   : $Source: /alkacon/cvs/AlkaconSimapi/src/com/alkacon/simapi/test/Attic/TestSimapi.java,v $
 * Date   : $Date: 2005/11/15 15:42:57 $
 * Version: $Revision: 1.3 $
  *
  * This library is part of OpenCms -
  * the Open Source Content Mananagement System
  *
  * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * For further information about Alkacon Software, please see the
  * company website: http://www.alkacon.com
  *
  * For further information about OpenCms, please see the
  * project website: http://www.opencms.org
  * 
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package com.alkacon.simapi.test;
 
 import com.alkacon.simapi.RenderSettings;
 import com.alkacon.simapi.Simapi;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.Iterator;
 
 import javax.imageio.ImageIO;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 //import com.drew.imaging.jpeg.JpegMetadataReader;
 //import com.drew.metadata.Directory;
 //import com.drew.metadata.Metadata;
 //import com.drew.metadata.Tag;
 
 /**
  * Test class for the imaging operations.<p>
  */
 public class TestSimapi extends VisualTestCase {
 
     /**
      * Test suite for this test class.<p>
      * 
      * @return the test suite
      */
     public static Test suite() {
 
         TestSuite suite = new TestSuite();
         suite.setName(TestSimapi.class.getName());
 
         suite.addTest(new TestSimapi("testScaleTransparent"));
         suite.addTest(new TestSimapi("testResizeScale"));
         suite.addTest(new TestSimapi("testResizeScaleFill"));
         suite.addTest(new TestSimapi("testSlowScalingIssue"));
         suite.addTest(new TestSimapi("testWriteJpegQuality"));
         suite.addTest(new TestSimapi("testResizeScaleFillSmall"));
         suite.addTest(new TestSimapi("testCrop"));
         suite.addTest(new TestSimapi("testResizeCrop"));
         suite.addTest(new TestSimapi("testWriteGif"));
         suite.addTest(new TestSimapi("testWriteJpegAndPng"));
         suite.addTest(new TestSimapi("testRead"));
 
         return suite;
     }
 
     /**
      * Default JUnit constructor.<p>
      * 
      * @param params JUnit parameters
      */
     public TestSimapi(String params) {
 
         super(params);
     }
 
     /**
      * Tests "slow scaling" issue encountered with some JPEG's.<p>
      * 
      * @throws Exception if the test fails
      */
     public void testSlowScalingIssue() throws Exception {
 
         Simapi simapi = new Simapi();
 
         long startTime = System.currentTimeMillis();
         
         BufferedImage img1 = Simapi.read(getClass().getResource("slow_scale01.jpg"));
         BufferedImage img2 = Simapi.read(getClass().getResource("slow_scale02.jpg"));
         BufferedImage img3 = Simapi.read(getClass().getResource("slow_scale03.jpg"));
         
         long loadTime = System.currentTimeMillis() - startTime;
         startTime = System.currentTimeMillis();
 
        img1 = simapi.resize(img1, 100, 100, Color.RED, Simapi.POS_CENTER);
        img2 = simapi.resize(img2, 100, 100, Color.RED, Simapi.POS_CENTER);
        img3 = simapi.resize(img3, 100, 100, Color.RED, Simapi.POS_CENTER);
         
         long scaleTime = System.currentTimeMillis() - startTime;
 
         checkImage(new BufferedImage[] {img1, img2, img3}, "Load time (msec): " + loadTime + " / Scale time (msec): " + scaleTime + ". Is this ok?");
         
         File baseDir = new File(getClass().getResource("slow_scale01.jpg").getPath()).getParentFile();
         
         simapi.write(img1, new File(baseDir, "w_slow_scale01.jpg"), Simapi.TYPE_JPEG);
         simapi.write(img2, new File(baseDir, "w_slow_scale02.jpg"), Simapi.TYPE_JPEG);
         simapi.write(img3, new File(baseDir, "w_slow_scale03.jpg"), Simapi.TYPE_JPEG);
         
         img1 = Simapi.read(getClass().getResource("w_slow_scale01.jpg"));
         img2 = Simapi.read(getClass().getResource("w_slow_scale02.jpg"));
         img3 = Simapi.read(getClass().getResource("w_slow_scale03.jpg"));
         
         checkImage(new BufferedImage[] {img1, img2, img3}, "Is default low quality OK for thumbnails?");
     }
     
     /**
      * Tests resizing a transparent image.<p>
      * 
      * @throws Exception if the test fails
      */
     public void testScaleTransparent() throws Exception {
 
         Simapi simapi = new Simapi();
         BufferedImage result;
         
         BufferedImage imgBull = Simapi.read(getClass().getResource("bull.jpg"));
         result = simapi.resize(imgBull, 500, 500, Simapi.COLOR_TRANSPARENT, Simapi.POS_CENTER, true);
         checkImage(new BufferedImage[] {imgBull, result}, "Has it been scaled (reduced) to 500x500 pixel with WHITE background fill?");
         assertEquals(500, result.getWidth());
         assertEquals(500, result.getHeight());
         
         File input = new File(getClass().getResource("logo_alkacon_150_t.gif").getPath());
         byte[] imgBytes = readFile(input);        
         BufferedImage img1 = Simapi.read(imgBytes);
         
         result = simapi.resize(img1, 75, 75, true);
         File destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved1.gif");
         simapi.write(result, destination, Simapi.TYPE_GIF);
         BufferedImage read = Simapi.read(destination);        
         checkImage(new BufferedImage[] {img1, read}, "Has it been scaled to 75x27 pixel and saved as GIF with transparent background ok?");
         assertEquals(75, read.getWidth());
         assertEquals(27, read.getHeight()); // aspect ratio kept intact
         
         result = simapi.resize(img1, 75, 75, false);
         destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved2.gif");
         simapi.write(result, destination, Simapi.TYPE_GIF);
         read = Simapi.read(destination);        
         checkImage(new BufferedImage[] {img1, read}, "Has it been scaled to 75x75 pixel and saved as GIF with transparent background ok?");
         assertEquals(75, read.getWidth());
         assertEquals(75, read.getHeight());
         
         result = simapi.resize(img1, 300, 300, true);
         destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved3.gif");
         simapi.write(result, destination, Simapi.TYPE_GIF);
         read = Simapi.read(destination);        
         checkImage(new BufferedImage[] {img1, read}, "Has it been scaled to 300x108 pixel and saved as GIF with transparent background ok?");
         assertEquals(300, read.getWidth());
         assertEquals(108, read.getHeight()); // aspect ratio kept intact
         
         result = simapi.resize(img1, 300, 300, Color.RED, Simapi.POS_CENTER);
         destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved4.gif");
         simapi.write(result, destination, Simapi.TYPE_GIF);
         read = Simapi.read(destination);        
         checkImage(new BufferedImage[] {img1, read}, "Has it been scaled to 300x300 pixel and saved as GIF with RED fill color?");
         assertEquals(300, read.getWidth());
         assertEquals(300, read.getHeight());
         
         result = simapi.resize(img1, 300, 300, Simapi.COLOR_TRANSPARENT, Simapi.POS_CENTER);
         destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved5.gif");
         simapi.write(result, destination, Simapi.TYPE_GIF);
         read = Simapi.read(destination);        
         checkImage(new BufferedImage[] {img1, read}, "Has it been scaled to 300x300 pixel and saved as GIF with transparent background ok (NO fill color)?");
         assertEquals(300, read.getWidth());
         assertEquals(300, read.getHeight());
         
         destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved5.png");
         simapi.write(result, destination, Simapi.TYPE_PNG);
         read = Simapi.read(destination);        
         checkImage(new BufferedImage[] {img1, read}, "Has it been scaled to 300x300 pixel and saved as PNG with transparent background ok (NO fill color)?");
         assertEquals(300, read.getWidth());
         assertEquals(300, read.getHeight());
         
         destination = new File(input.getParentFile(), "logo_alkacon_150_t_saved5.jpg");
         simapi.write(result, destination, Simapi.TYPE_JPEG);
         read = Simapi.read(destination);        
         checkImage(new BufferedImage[] {img1, read}, "Has it been scaled to 300x300 pixel and saved as JPEG with transparent background replaced?");
         assertEquals(300, read.getWidth());
         assertEquals(300, read.getHeight());
     }
     
     /**
      * Tests writing an image as JPEG with different quality settings.<p>
      * 
      * @throws Exception if the test fails
      */
     public void testWriteJpegQuality() throws Exception {
         
         BufferedImage read;
         File input;
         File destination;
                 
         input = new File(getClass().getResource("DSCN0754.JPG").getPath());
 
 //        // Java standard metadata extraction - no Exif info is read from file
 //        ImageInputStream stream = ImageIO.createImageInputStream(input);
 //        Iterator iter = ImageIO.getImageReaders(stream);
 //        ImageReader reader = (ImageReader)iter.next();                
 //        ImageReadParam param = reader.getDefaultReadParam();
 //        reader.setInput(stream, true, true);        
 //        IIOMetadata meta = reader.getImageMetadata(0);
 //        if (meta != null) {
 //            String formats[] = meta.getMetadataFormatNames();
 //            System.out.println("format count: " + formats.length);
 //            for (int i=0; i<formats.length; i++) {
 //                System.out.println("format " + i + ": " + formats[i]);
 //                Node metaData = meta.getAsTree(formats[i]);
 //                TransformerFactory tFactory = TransformerFactory.newInstance();
 //                Transformer transformer = tFactory.newTransformer();
 //                DOMSource source = new DOMSource(metaData);
 //                StreamResult sr = new StreamResult(System.out);
 //                transformer.transform(source, sr);
 //            }            
 //        } else {
 //            System.out.println("meta is null");
 //        }        
 //        BufferedImage img0 = reader.read(0, param);
 //        stream.close();
 //        reader.dispose();
 //      
         
 //        Metadata metadata = JpegMetadataReader.readMetadata(input);
 //        // iterate through metadata directories
 //        Iterator directories = metadata.getDirectoryIterator();
 //        while (directories.hasNext()) {
 //            Directory directory = (Directory)directories.next();
 //            // iterate through tags and print to System.out
 //            Iterator tags = directory.getTagIterator();
 //            while (tags.hasNext()) {
 //                Tag tag = (Tag)tags.next();
 //                // use Tag.toString()
 //                System.out.println(tag);
 //            }
 //        }
         
         /* TODO: Writing of Exif information
          * 
          * Libary for Exif extraction AND writing (but only with Java 1.5):
          * http://mediachest.sourceforge.net/mediautil/
          */
 
         RenderSettings rs = new RenderSettings(Simapi.RENDER_QUALITY);
         Simapi simapi = new Simapi(rs);
 
         read = Simapi.read(input);
         read = simapi.resize(read, 800, 600, false);
         checkImage(new BufferedImage[] {read}, "Has it been read?");
         
         input = new File(getClass().getResource("screen1.png").getPath());
         BufferedImage img1 = Simapi.read(input);
         
         rs.setCompressionQuality(0.3f);
         destination = new File(input.getParentFile(), "saved_q_low.jpg");
         simapi.write(img1, destination, Simapi.TYPE_JPEG);
         read = Simapi.read(destination);
         checkImage(new BufferedImage[] {img1, read}, "Has it been written to disk as JPEG in a _low_ quality version?");
         
         rs.setCompressionQuality(0.95f);
         destination = new File(input.getParentFile(), "saved_q_high.jpg");
         simapi.write(img1, destination, Simapi.TYPE_JPEG);
         read = Simapi.read(destination);
         checkImage(new BufferedImage[] {img1, read}, "Has it been written to disk as JPEG in a _high_ quality version?");
     }
     
     /**
      * Tests resizing/scaling and filling a small image.<p>
      * 
      * @throws Exception if the test fails
      */
     public void testResizeScaleFillSmall() throws Exception {
 
         Simapi simapi = new Simapi();
 
         File input;
         BufferedImage img1;
         BufferedImage result1;
         BufferedImage result2;
         BufferedImage result3;
 
         input = new File(getClass().getResource("opencms.gif").getPath());
         img1 = Simapi.read(input);
 
         result1 = simapi.resize(img1, 300, 300, Color.MAGENTA, Simapi.POS_STRAIGHT_DOWN);
         result2 = simapi.resize(img1, 300, 300, Color.YELLOW, Simapi.POS_STRAIGHT_RIGHT);
         result3 = simapi.resize(img1, 300, 300, Color.GREEN, Simapi.POS_STRAIGHT_UP);
         checkImage(
             new BufferedImage[] {result1, result2, result3},
             "Images should be scaled and placed down, right, up");
 
         input = new File(getClass().getResource("opencms.gif").getPath());
         img1 = Simapi.read(input);
 
         result1 = simapi.resize(img1, 300, 300, Color.MAGENTA, Simapi.POS_CENTER);
         result2 = simapi.resize(img1, 300, 300, Color.YELLOW, Simapi.POS_DOWN_LEFT);
         result3 = simapi.resize(img1, 300, 300, Color.GREEN, Simapi.POS_UP_RIGHT);
         checkImage(
             new BufferedImage[] {result1, result2, result3},
             "Images should be scaled and placed center, down left, up right");
     }
 
     /**
      * Tests resizing/scaling and filling an image.<p>
      * 
      * @throws Exception if the test fails
      */
     public void testResizeScaleFill() throws Exception {
 
         Simapi simapi = new Simapi();
 
         File input;
         BufferedImage img1;
         BufferedImage result1;
         BufferedImage result2;
         BufferedImage result3;
 
         input = new File(getClass().getResource("screen1.png").getPath());
         img1 = Simapi.read(input);
 
         result1 = simapi.resize(img1, 250, 250, Color.MAGENTA, Simapi.POS_STRAIGHT_DOWN);
         result2 = simapi.resize(img1, 350, 250, Color.YELLOW, Simapi.POS_STRAIGHT_RIGHT);
         result3 = simapi.resize(img1, 250, 250, Color.GREEN, Simapi.POS_STRAIGHT_UP);
         checkImage(
             new BufferedImage[] {result1, result2, result3},
             "Images should be scaled and placed down, right, up");
 
         input = new File(getClass().getResource("opencms.gif").getPath());
         img1 = Simapi.read(input);
 
         result1 = simapi.resize(img1, 150, 150, Color.MAGENTA, Simapi.POS_STRAIGHT_DOWN);
         result2 = simapi.resize(img1, 250, 50, Color.YELLOW, Simapi.POS_STRAIGHT_RIGHT);
         result3 = simapi.resize(img1, 150, 150, Color.GREEN, Simapi.POS_STRAIGHT_UP);
         checkImage(
             new BufferedImage[] {result1, result2, result3},
             "Images should be scaled and placed down, right, up");
     }
 
     /**
      * Tests cropping and resizing an image.<p>
      * 
      * @throws Exception if the test fails
      */
     public void testResizeCrop() throws Exception {
 
         Simapi simapi = new Simapi();
 
         File input = new File(getClass().getResource("opencms.gif").getPath());
         BufferedImage img1 = Simapi.read(input);
         BufferedImage result1 = simapi.resize(img1, 200, 100, Simapi.POS_CENTER);
         BufferedImage result2 = simapi.resize(img1, 200, 100, Simapi.POS_STRAIGHT_LEFT);
         BufferedImage result3 = simapi.resize(img1, 200, 100, Simapi.POS_STRAIGHT_RIGHT);
         checkImage(
             new BufferedImage[] {result1, result2, result3},
             "Images should be resized and cropped at center, left, right");
 
         input = new File(getClass().getResource("screen1.png").getPath());
         img1 = Simapi.read(input);
         result1 = simapi.resize(img1, 400, 200, Simapi.POS_CENTER);
         result2 = simapi.resize(img1, 400, 200, Simapi.POS_STRAIGHT_UP);
         result3 = simapi.resize(img1, 400, 200, Simapi.POS_STRAIGHT_DOWN);
         checkImage(
             new BufferedImage[] {result1, result2, result3},
             "Images should be resized and cropped at center, up, down");
     }
 
     /**
      * Tests cropping an image.<p>
      * 
      * @throws Exception if the test fails
      */
     public void testCrop() throws Exception {
 
         Simapi simapi = new Simapi();
 
         File input;
         BufferedImage img1;
         BufferedImage result1;
         BufferedImage result2;
         BufferedImage result3;
 
         input = new File(getClass().getResource("screen1.png").getPath());
         img1 = Simapi.read(input);
 
         result1 = simapi.crop(img1, 250, 250, Simapi.POS_STRAIGHT_LEFT);
         result2 = simapi.crop(img1, 250, 250, Simapi.POS_CENTER);
         result3 = simapi.crop(img1, 250, 250, Simapi.POS_STRAIGHT_RIGHT);
         checkImage(
             new BufferedImage[] {result1, result2, result3},
             "Images should be cropped MIDDLE at left, center right");
 
         result1 = simapi.crop(img1, 250, 250, Simapi.POS_UP_LEFT);
         result2 = simapi.crop(img1, 250, 250, Simapi.POS_STRAIGHT_UP);
         result3 = simapi.crop(img1, 250, 250, Simapi.POS_UP_RIGHT);
         checkImage(
             new BufferedImage[] {result1, result2, result3},
             "Images should be cropped UP at left, center, right");
 
         result1 = simapi.crop(img1, 250, 250, Simapi.POS_DOWN_LEFT);
         result2 = simapi.crop(img1, 250, 250, Simapi.POS_STRAIGHT_DOWN);
         result3 = simapi.crop(img1, 250, 250, Simapi.POS_DOWN_RIGHT);
         checkImage(
             new BufferedImage[] {result1, result2, result3},
             "Images should be cropped DOWN at left, center, right");
     }
 
     /**
      * Tests writing an image as GIF.<p>
      * 
      * @throws Exception if the test fails
      */
     public void testWriteGif() throws Exception {
 
         Simapi simapi = new Simapi();
 
         ImageIO.scanForPlugins();
         Iterator gifWriters = ImageIO.getImageWritersByFormatName(Simapi.TYPE_GIF);
         if (!gifWriters.hasNext()) {
             fail("No Java ImageIO writers for the GIF format are available.");
         }
 
         BufferedImage result;
         BufferedImage read;
         File input;
         File destination;
 
         input = new File(getClass().getResource("opencms.gif").getPath());
         BufferedImage img1 = Simapi.read(input);
         result = simapi.resize(img1, 300, 150, true, true);
 
         destination = new File(input.getParentFile(), "saved3.gif");
         System.out.println(destination.getAbsolutePath());
         simapi.write(result, destination, Simapi.TYPE_GIF);
         read = Simapi.read(destination);
         checkImage(new BufferedImage[] {img1, read}, "Has it been written to disk as GIF in a scaled version?");
     }
 
     /**
      * Tests writing an image as JPEG and PNG.<p>
      * 
      * @throws Exception if the test fails
      */
     public void testWriteJpegAndPng() throws Exception {
 
         Simapi simapi = new Simapi();
 
         BufferedImage result;
         BufferedImage read;
         File input;
         File destination;
 
         input = new File(getClass().getResource("opencms.gif").getPath());
         BufferedImage img1 = Simapi.read(input);
         result = simapi.resize(img1, 300, 150, true, true);
         destination = new File(input.getParentFile(), "saved1.jpg");
         System.out.println(destination.getAbsolutePath());
         simapi.write(result, destination, Simapi.TYPE_JPEG);
         read = Simapi.read(destination);
         checkImage(new BufferedImage[] {img1, read}, "Has it been written to disk as JPEG in a scaled version?");
         
         input = new File(getClass().getResource("screen1.png").getPath());
         BufferedImage img2 = Simapi.read(input);
         result = simapi.scale(img2, 0.8f);
         destination = new File(input.getParentFile(), "saved2.png");
         System.out.println(destination.getAbsolutePath());
         simapi.write(result, destination, Simapi.TYPE_PNG);
         read = Simapi.read(destination);
         checkImage(new BufferedImage[] {img2, read}, "Has it been written to disk as PNG in a scaled version?");
     }
     
     /**
      * Tests resizing and scaling an image.<p>
      * 
      * @throws Exception if the test fails
      */
     public void testResizeScale() throws Exception {
 
         Simapi simapi = new Simapi();
 
         BufferedImage result;
 
         BufferedImage imgBull = Simapi.read(getClass().getResource("bull.jpg"));
         result = simapi.resize(imgBull, 150, 80, Color.RED, Simapi.POS_CENTER, true);
         checkImage(new BufferedImage[] {imgBull, result}, "Has it been scaled (reduced) to 150x80 pixel with RED background fill?");
         assertEquals(150, result.getWidth());
         assertEquals(80, result.getHeight());
         
         result = simapi.resize(imgBull, 450, 450, Color.RED, Simapi.POS_CENTER, true);
         checkImage(new BufferedImage[] {imgBull, result}, "Has it been scaled (enlarged) to 450x450 pixel WITH blowup and red background fill?");
         assertEquals(450, result.getWidth());
         assertEquals(450, result.getHeight());
         
         result = simapi.resize(imgBull, 450, 450, Color.RED, Simapi.POS_CENTER, false);
         checkImage(new BufferedImage[] {imgBull, result}, "Has it been scaled (enlarged) to 450x450 pixel WITHOUT blowup and red background fill?");
         assertEquals(450, result.getWidth());
         assertEquals(450, result.getHeight());
         
         result = simapi.resize(imgBull, 250, 250, Color.RED, Simapi.POS_CENTER, false);
         checkImage(new BufferedImage[] {imgBull, result}, "Has it been scaled (reduced) to 250x250 pixel WITHOUT blowup and red background fill?");
         assertEquals(250, result.getWidth());
         assertEquals(250, result.getHeight());
         
         result = simapi.resize(imgBull, 350, 450, false);
         checkImage(new BufferedImage[] {imgBull, result}, "Has it been scaled to 350x450 pixel without keeping PROPORTIONS?");
         assertEquals(350, result.getWidth());
         assertEquals(450, result.getHeight());
         
         result = simapi.resize(imgBull, 350, 450, Simapi.POS_UP_RIGHT);
         checkImage(new BufferedImage[] {imgBull, result}, "Has it been scaled (enlarged) to 350x450 pixel with CROP up right?");
         assertEquals(350, result.getWidth());
         assertEquals(450, result.getHeight());
         
         result = simapi.resize(imgBull, 200, 250, Simapi.POS_UP_RIGHT);
         checkImage(new BufferedImage[] {imgBull, result}, "Has it been scaled (reduced) to 200x250 pixel with CROP up right?");
         assertEquals(200, result.getWidth());
         assertEquals(250, result.getHeight());
         
         BufferedImage img1 = Simapi.read(getClass().getResource("screen1.png"));
         result = simapi.scale(img1, 0.75f);
         checkImage(new BufferedImage[] {img1, result}, "Has it been scaled to 75%?");
         assertEquals((int)(img1.getWidth() * 0.75f), result.getWidth());
         assertEquals((int)(img1.getHeight() * 0.75f), result.getHeight());
         
         BufferedImage img2 = Simapi.read(getClass().getResource("opencms_text.jpg"));
         result = simapi.resize(img2, 100, 50);
         
         checkImage(new BufferedImage[] {img2, result}, "Has it been resized to 100x50 pixel?");
         assertEquals(100, result.getWidth());
         assertEquals(50, result.getHeight());
         
         BufferedImage img3 = Simapi.read(getClass().getResource("alkacon.png"));
         result = simapi.resize(img3, 400, 300, true);
 
         checkImage(new BufferedImage[] {img3, result}, "Has it been resized to 400x300 pixel with aspect intact?");
         assertEquals(400, result.getWidth());
         assertEquals(254, result.getHeight()); // aspect ratio height is 254
     }
 
     /**
      * Tests reading an image.<p>
      * 
      * @throws Exception if the test fails
      */
     public void testRead() throws Exception {
 
         BufferedImage img1 = Simapi.read(getClass().getResource("alkacon.png"));
         BufferedImage img2 = Simapi.read(getClass().getResource("opencms_text.jpg"));
         BufferedImage img3 = Simapi.read(getClass().getResource("opencms.gif"));
 
         checkImage(new BufferedImage[] {img1, img2, img3}, "Do you see 3 images?");
     }
 }
