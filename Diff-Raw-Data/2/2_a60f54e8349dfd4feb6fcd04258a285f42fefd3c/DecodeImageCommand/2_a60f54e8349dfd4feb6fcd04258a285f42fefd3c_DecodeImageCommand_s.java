 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.celements.photo.plugin.cmd;
 
 import java.awt.color.CMMException;
 import java.awt.color.ColorSpace;
 import java.awt.color.ICC_ColorSpace;
 import java.awt.color.ICC_Profile;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorConvertOp;
 import java.awt.image.Raster;
 import java.awt.image.WritableRaster;
 import java.awt.image.renderable.ParameterBlock;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.imageio.IIOException;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReader;
 import javax.imageio.stream.ImageInputStream;
 import javax.media.jai.JAI;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.sanselan.ImageReadException;
 import org.apache.sanselan.Sanselan;
 import org.apache.sanselan.common.byteSources.ByteSource;
 import org.apache.sanselan.common.byteSources.ByteSourceInputStream;
 import org.apache.sanselan.formats.jpeg.JpegImageParser;
 import org.apache.sanselan.formats.jpeg.segments.UnknownSegment;
 
 import com.sun.media.jai.codec.FileCacheSeekableStream;
 import com.sun.media.jai.codec.SeekableStream;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiAttachment;
 
 public class DecodeImageCommand {
   private static final Log LOGGER = LogFactory.getFactory().getInstance(
       DecodeImageCommand.class);
   
   public static final int COLOR_TYPE_RGB = 1;
   public static final int COLOR_TYPE_CMYK = 2;
   public static final int COLOR_TYPE_YCCK = 3;
 
   private int colorType = COLOR_TYPE_RGB;
   private String cmykProfile = "ECI_Offset_2009/ISOcoated_v2_300_eci.icc";
   
   public BufferedImage readImage(XWikiAttachment att, XWikiContext context
       ) throws ImageReadException, XWikiException {
     return readImage(att.getContentInputStream(context), att.getFilename(), 
         att.getMimeType(context));
   }
   
   public BufferedImage readImage(InputStream imageStream, String filename, 
       String mimeType) throws ImageReadException, XWikiException {
     BufferedImage image = null;
     ByteArrayOutputStream createMarkStreamHelper = null;
     try {
       if(imageStream.markSupported()) {
         imageStream.mark(Integer.MAX_VALUE);
       } else {
         createMarkStreamHelper = new ByteArrayOutputStream();
         byte[] buffer = new byte[1024];
         int len;
         while ((len = imageStream.read(buffer)) > -1 ) {
           createMarkStreamHelper.write(buffer, 0, len);
         }
         createMarkStreamHelper.flush();
         //NOTICE: ByteArrayInputStream supports mark and marks by default on position 0
         imageStream = new ByteArrayInputStream(createMarkStreamHelper.toByteArray()); 
       }
       colorType = COLOR_TYPE_RGB;
       boolean hasAdobeMarker = false;
       ImageInputStream stream = ImageIO.createImageInputStream(imageStream);
       Iterator<ImageReader> iter = ImageIO.getImageReaders(stream);
       while (iter.hasNext()) {
         ImageReader reader = iter.next();
         reader.setInput(stream);
         ICC_Profile profile = null;
         try {
           try {
             image = reader.read(0);
           } catch(CMMException cmmExcp) {
             imageStream.reset();
             image = readUsingJAI(imageStream, mimeType);
           }
         } catch(IIOException iioExcp) {
           colorType = COLOR_TYPE_CMYK;
           imageStream.reset();
           hasAdobeMarker = hasAdobeMarker(imageStream, filename);
           imageStream.reset();
           profile = Sanselan.getICCProfile(imageStream, filename);
           WritableRaster raster = (WritableRaster) reader.readRaster(0, null);
           if (colorType == COLOR_TYPE_YCCK) {
             convertYcckToCmyk(raster);
           }
           if(hasAdobeMarker) {
             convertInvertedColors(raster);
           }
           image = convertCmykToRgb(raster, profile);
         }
         if(image != null) {
           break;
         }
       }
     } catch(IOException ioe) {
       
     }finally {
       if(imageStream != null) {
         try {
           imageStream.close();
         } catch (IOException ioe) {
           LOGGER.error("Exception cloasing in stream.", ioe);
         }
       }
       if(createMarkStreamHelper != null) {
         try {
           createMarkStreamHelper.close();
         } catch (IOException ioe) {
           LOGGER.error("Exception cloasing out stream.", ioe);
         }
       }
     }
    return null;
   }
 
   // Requires Java Advanced Imaging - used as fallback for certain jpeg files containing 
   // conflicting information on where the actual image data begins (JFIF != EXIF)
   BufferedImage readUsingJAI(InputStream inputStream, String mimeType)
       throws IOException, XWikiException {
     BufferedImage image;
     SeekableStream seekableStream = new FileCacheSeekableStream(inputStream);
     ParameterBlock paramBlock = new ParameterBlock();
     paramBlock.add(seekableStream);
     image = JAI.create(mimeType.replaceAll("image/", ""), paramBlock
         ).getAsBufferedImage();
     return image;
   }
   
   boolean hasAdobeMarker(InputStream imgIn, String filename
       ) throws IOException, ImageReadException {
     boolean hasAdobeMarker = true;
     JpegImageParser parser = new JpegImageParser();
     ByteSource byteSource = new ByteSourceInputStream(imgIn, filename);//new ByteSourceFile(file);
     @SuppressWarnings("rawtypes")
     ArrayList segments = parser.readSegments(byteSource, new int[] { 0xffee }, true);
     if (segments != null && segments.size() >= 1) {
       UnknownSegment app14Segment = (UnknownSegment) segments.get(0);
       byte[] data = app14Segment.bytes;
       if (data.length >= 12 && data[0] == 'A' && data[1] == 'd' && data[2] == 'o' 
           && data[3] == 'b' && data[4] == 'e'){
         hasAdobeMarker = true;
         int transform = app14Segment.bytes[11] & 0xff;
         if (transform == 2) {
           colorType = COLOR_TYPE_YCCK;
         }
       }
     }
     return hasAdobeMarker;
   }
   
   void convertYcckToCmyk(WritableRaster raster) {
     int height = raster.getHeight();
     int width = raster.getWidth();
     int stride = width * 4;
     int[] pixelRow = new int[stride];
     for (int h = 0; h < height; h++) {
       raster.getPixels(0, h, width, 1, pixelRow);
       for (int x = 0; x < stride; x += 4) {
         int y = pixelRow[x];
         int cb = pixelRow[x + 1];
         int cr = pixelRow[x + 2];
         int c = (int) (y + 1.402 * cr - 178.956);
         int m = (int) (y - 0.34414 * cb - 0.71414 * cr + 135.95984);
         y = (int) (y + 1.772 * cb - 226.316);
         if (c < 0) c = 0; else if (c > 255) c = 255;
         if (m < 0) m = 0; else if (m > 255) m = 255;
         if (y < 0) y = 0; else if (y > 255) y = 255;
         pixelRow[x] = 255 - c;
         pixelRow[x + 1] = 255 - m;
         pixelRow[x + 2] = 255 - y;
       }
       raster.setPixels(0, h, width, 1, pixelRow);
     }
   }
 
   void convertInvertedColors(WritableRaster raster) {
     int height = raster.getHeight();
     int width = raster.getWidth();
     int stride = width * 4;
     int[] pixelRow = new int[stride];
     for (int h = 0; h < height; h++) {
       raster.getPixels(0, h, width, 1, pixelRow);
       for (int x = 0; x < stride; x++) {
         pixelRow[x] = 255 - pixelRow[x];
       }
       raster.setPixels(0, h, width, 1, pixelRow);
     }
   }
 
   BufferedImage convertCmykToRgb(Raster cmykRaster, ICC_Profile cmykProfile
       ) throws IOException {
     cmykProfile = getICCProfile(cmykProfile);
     ICC_ColorSpace cmykCS = new ICC_ColorSpace(cmykProfile);
     BufferedImage rgbImage = new BufferedImage(cmykRaster.getWidth(), 
         cmykRaster.getHeight(), BufferedImage.TYPE_INT_RGB);
     WritableRaster rgbRaster = rgbImage.getRaster();
     ColorSpace rgbCS = rgbImage.getColorModel().getColorSpace();
     ColorConvertOp cmykToRgb = new ColorConvertOp(cmykCS, rgbCS, null);
     cmykToRgb.filter(cmykRaster, rgbRaster);
     return rgbImage;
   }
 
   ICC_Profile getICCProfile(ICC_Profile cmykProfile) throws IOException {
     if (cmykProfile == null) {
       cmykProfile = ICC_Profile.getInstance(getClass(
           ).getClassLoader().getResourceAsStream(getCMYKProfile()));
     }
     if (cmykProfile.getProfileClass() != ICC_Profile.CLASS_DISPLAY) {
       // Need to clone entire profile, due to a JDK 7 bug
       byte[] profileData = cmykProfile.getData();
       if (profileData[ICC_Profile.icHdrRenderingIntent] == ICC_Profile.icPerceptual) {
         intToBigEndian(ICC_Profile.icSigDisplayClass, profileData, 
             ICC_Profile.icHdrDeviceClass); // Header is first
         cmykProfile = ICC_Profile.getInstance(profileData);
       }
     }
     return cmykProfile;
   }
   
   void intToBigEndian(int value, byte[] array, int index) {
     array[index]   = (byte) (value >> 24);
     array[index+1] = (byte) (value >> 16);
     array[index+2] = (byte) (value >>  8);
     array[index+3] = (byte) (value);
   }
 
   public String getCMYKProfile() {
     return cmykProfile;
   }
   
   public void setCMYKProfile(String cmykProfile) {
     this.cmykProfile = cmykProfile;
   }
 }
