 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.image.io;
 
 import java.io.File;
 
 import ome.xml.model.enums.EnumerationException;
 import edu.stanford.cfuller.imageanalysistools.frontend.LoggingUtilities;
 import edu.stanford.cfuller.imageanalysistools.image.Image;
 import edu.stanford.cfuller.imageanalysistools.image.PixelData;
 
 /**
  * This class writes an Image to disk as one of several possible formats.
  * 
  * @author Colin J. Fuller
  * 
  */
 
 public class ImageWriter {
 	
 	Image toWrite;
 
 	protected ImageWriter(){}
 
     /**
      * Constructs an ImageWriter for a given Image.
      * <p>
      * The writer retains a reference to the Image, so it can be used multiple times for writing the same Image to disk.  Only the Image's metadata will be used;
      * pixel data is passed in separately, allowing several Images that share the same metadata to be written with the same writer.
      * @param im    The Image whose metadata will be used to write Images to disk.
      */
 	public ImageWriter(Image im) {
 		toWrite = im;
 	}
 
     /**
      * Writes the specified pixels to disk with the ImageWriter's Image's metadata.
      * <p>
      * The output format will be specified by the filename's extenstion and must be a valid writable extension for the LOCI bioformats library.
      * The recommended format is ome-tiff, with extension ".ome.tif".
      *
      * @param filename  The absolute path of the file to which the image will be written.  The parent directory should exist.
      * @param imagePixels   The PixelData containing the values to write to disk.
      * @throws java.io.IOException  if there is an exception while opening or writing the file.
      */
 	public void write(String filename, PixelData imagePixels) throws java.io.IOException {
		
 		loci.formats.IFormatWriter writer = new loci.formats.ImageWriter();

 		try {
 			toWrite.getMetadata().setPixelsDimensionOrder(ome.xml.model.enums.DimensionOrder.fromString(imagePixels.getDimensionOrder().toUpperCase()), 0);
 			toWrite.getMetadata().setPixelsType(ome.xml.model.enums.PixelType.fromString(loci.formats.FormatTools.getPixelTypeString(imagePixels.getDataType())), 0);
 
 		} catch (EnumerationException e) {
 			LoggingUtilities.getLogger().severe("Exception while trying to update dimension order of pixel data: " + e.getMessage());
 		}
 	
 		writer.setMetadataRetrieve(toWrite.getMetadata());
 		//the next line is a hack... when writing the same image multiple times, there seems to be some sort of issue with 
 		//the pixels data being deleted, but not having a dummy object put in to read the endianness off of.
 		toWrite.getMetadata().setPixelsBinDataBigEndian(imagePixels.getByteOrder() == java.nio.ByteOrder.BIG_ENDIAN, 0, 0);
 		
 		
 		try {
 			
 			File f = new File(filename);
 			
 			if (f.exists()) { // there seems to be some bug with adding to the end of an existing image rather than overwriting if it's there already; delete any existing file to avoid this.
 				f.delete();
 			}
 			
 			writer.setId(filename);
 						
 			for (int i = 0; i < imagePixels.getNumPlanes(); i++) {
 				
 				byte[] bytes = imagePixels.getPlane(i);
 								
 				writer.savePlane(i, bytes);
 				
 			}
 			
 		} catch (loci.formats.FormatException e) {
 			LoggingUtilities.getLogger().severe("Encountered exception " + e.toString() + " while writing " + filename + " skipping write and proceeding.");
 			e.printStackTrace();
 			return;
 		} finally {
 		
 		    writer.close();
 
         }
 		
 	}
 	
 }
