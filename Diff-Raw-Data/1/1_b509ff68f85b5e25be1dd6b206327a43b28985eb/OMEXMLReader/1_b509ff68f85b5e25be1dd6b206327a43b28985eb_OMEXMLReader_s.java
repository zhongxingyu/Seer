 //
 // OMEXMLReader.java
 //
 
 /*
 LOCI Bio-Formats package for reading and converting biological file formats.
 Copyright (C) 2005-@year@ Melissa Linkert, Curtis Rueden, Chris Allan
 and Eric Kjellman.
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU Library General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Library General Public License for more details.
 
 You should have received a copy of the GNU Library General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
 package loci.formats.in;
 
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.zip.*;
 import loci.formats.*;
 import org.openmicroscopy.xml.OMENode;
 
 /**
  * OMEXMLReader is the file format reader for OME-XML files.
  *
  * @author Melissa Linkert linkert at cs.wisc.edu
  */
 public class OMEXMLReader extends FormatReader {
 
   // -- Fields --
 
   /** Current file. */
   protected RandomAccessStream in;
 
   /** Flag indicating whether current file is little endian. */
   protected boolean[] littleEndian;
 
   /** Number of image planes in the file. */
   protected int[] numImages;
 
   /** Number of bits per pixel. */
   protected int[] bpp;
 
   /** Offset to each plane's data. */
   protected Vector[] offsets;
 
   /** String indicating the compression type. */
   protected String[] compression;
 
   /** Image width. */
   private int[] width;
 
   /** Image height. */
   private int[] height;
 
   /** Number of channels. */
   private int[] numChannels;
 
   /** Number of timepoints. */
   private int[] numT;
 
   /** Number of Z slices. */
   private int[] numZ;
 
   /** Dimension order. */
   private String[] order;
 
   /** Internal OME-XML store, for convenient access to metadata fields. */
   private OMEXMLMetadataStore omexml = new OMEXMLMetadataStore();
 
   // -- Constructor --
 
   /** Constructs a new OME-XML reader. */
   public OMEXMLReader() { super("OME-XML", "ome"); }
 
   // -- FormatReader API methods --
 
   /** Checks if the given block is a valid header for an OME-XML file. */
   public boolean isThisType(byte[] block) {
     return new String(block, 0, 5).equals("<?xml");
   }
 
   /** Return the number of series in this file. */
   public int getSeriesCount(String id) throws FormatException, IOException {
     if (!id.equals(currentId)) initFile(id);
     return width.length;
   }
 
   /** Determines the number of images in the given OME-XML file. */
   public int getImageCount(String id) throws FormatException, IOException {
     if (!id.equals(currentId)) initFile(id);
     return numImages[series];
   }
 
   /** Checks if the images in the file are RGB. */
   public boolean isRGB(String id) throws FormatException, IOException {
     return false;
   }
 
   /** Return true if the data is in little-endian format. */
   public boolean isLittleEndian(String id) throws FormatException, IOException {
     if (!id.equals(currentId)) initFile(id);
     return littleEndian[series];
   }
 
   /** Returns whether or not the channels are interleaved. */
   public boolean isInterleaved(String id) throws FormatException, IOException {
     return false;
   }
 
   /** Obtains the specified image from the given file as a byte array. */
   public byte[] openBytes(String id, int no)
     throws FormatException, IOException
   {
     if (!id.equals(currentId)) initFile(id);
 
     if (no < 0 || no >= numImages[series]) {
       throw new FormatException("Invalid image number: " + no);
     }
 
     int w = width[series];
     int h = width[series];
 
     in.seek(((Integer) offsets[series].get(no)).intValue());
 
     byte[] buf;
     if (no < getImageCount(id) - 1) {
       buf = new byte[((Integer) offsets[series].get(no + 1)).intValue() -
         ((Integer) offsets[series].get(no)).intValue()];
     }
     else {
       buf = new byte[(int) (in.length() -
         ((Integer) offsets[series].get(no)).intValue())];
     }
     in.read(buf);
     String data = new String(buf);
 
     // retrieve the compressed pixel data
 
     int dataStart = data.indexOf(">") + 1;
     String pix = data.substring(dataStart);
     if (pix.indexOf("<") > 0) {
       pix = pix.substring(0, pix.indexOf("<"));
     }
 
     byte[] pixels = Compression.base64Decode(pix);
 
     if (compression[series].equals("bzip2")) {
       byte[] tempPixels = pixels;
       pixels = new byte[tempPixels.length - 2];
       System.arraycopy(tempPixels, 2, pixels, 0, pixels.length);
 
       ByteArrayInputStream bais = new ByteArrayInputStream(pixels);
       CBZip2InputStream bzip = new CBZip2InputStream(bais);
       pixels = new byte[w * h * bpp[series]];
       for (int i=0; i<pixels.length; i++) {
         pixels[i] = (byte) bzip.read();
       }
     }
     else if (compression[series].equals("zlib")) {
       try {
         Inflater decompressor = new Inflater();
         decompressor.setInput(pixels, 0, pixels.length);
         pixels = new byte[w * h * bpp[series]];
         decompressor.inflate(pixels);
         decompressor.end();
       }
       catch (DataFormatException dfe) {
         throw new FormatException("Error uncompressing zlib data.");
       }
     }
     return pixels;
   }
 
   /** Obtains the specified image from the given OME-XML file. */
   public BufferedImage openImage(String id, int no)
     throws FormatException, IOException
   {
     return ImageTools.makeImage(openBytes(id, no), width[series],
       height[series], 1, false, bpp[series], littleEndian[series]);
   }
 
   /** Closes any open files. */
   public void close() throws FormatException, IOException {
     if (in != null) in.close();
     in = null;
     currentId = null;
   }
 
   /** Initializes the given OME-XML file. */
   protected void initFile(String id) throws FormatException, IOException {
     close();
     currentId = id;
     metadata = new Hashtable();
     in = new RandomAccessStream(getMappedId(id));
 
     in.skipBytes(200);
 
     int numDatasets = 0;
     Vector endianness = new Vector();
     Vector bigEndianPos = new Vector();
 
     byte[] buf = new byte[1];
 
     while (in.getFilePointer() < in.length()) {
       // read a block of 8192 characters, looking for the "BigEndian" pattern
       buf = new byte[8192];
       boolean found = false;
       while (!found) {
         if (in.getFilePointer() < in.length()) {
           in.read(buf, 9, 8183);
           String test = new String(buf);
 
           int ndx = test.indexOf("BigEndian");
           if (ndx != -1) {
             found = true;
             String endian = test.substring(ndx + 11);
             endianness.add(new Boolean(!endian.toLowerCase().startsWith("t")));
             bigEndianPos.add(new Integer(in.getFilePointer() - (8192 - ndx)));
             numDatasets++;
           }
         }
         else if (numDatasets == 0) {
           throw new FormatException("Pixel data not found.");
         }
         else found = true;
       }
     }
 
     littleEndian = new boolean[numDatasets];
     offsets = new Vector[numDatasets];
 
     for (int i=0; i<littleEndian.length; i++) {
       littleEndian[i] = ((Boolean) endianness.get(i)).booleanValue();
       offsets[i] = new Vector();
     }
 
     // look for the first BinData element in each series
 
     for (int i=0; i<numDatasets; i++) {
       in.seek(((Integer) bigEndianPos.get(i)).intValue());
       boolean found = false;
       buf = new byte[8192];
       in.read(buf, 0, 14);
 
       while (!found) {
         if (in.getFilePointer() < in.length()) {
           int numRead = in.read(buf, 14, 8192-14);
 
           String test = new String(buf);
 
           int ndx = test.indexOf("<Bin");
           if (ndx == -1) {
             byte[] b = buf;
             System.arraycopy(b, 8192 - 15, buf, 0, 14);
           }
           else {
             while (!((ndx != -1) && (ndx != test.indexOf("<Bin:External")) &&
               (ndx != test.indexOf("<Bin:BinaryFile"))))
             {
               ndx = test.indexOf("<Bin", ndx+1);
             }
             found = true;
             numRead += 14;
             offsets[i].add(new Integer(
               (int) in.getFilePointer() - (numRead - ndx)));
           }
         }
         else {
           throw new FormatException("Pixel data not found");
         }
       }
     }
 
     in.seek(0);
 
     for (int i=0; i<numDatasets; i++) {
       if (i == 0) {
         buf = new byte[((Integer) offsets[i].get(0)).intValue()];
       }
       else {
         // look for the next Image element
 
         boolean found = false;
         buf = new byte[8192];
         in.read(buf, 0, 14);
         while (!found) {
           if (in.getFilePointer() < in.length()) {
             int numRead = in.read(buf, 14, 8192-14);
 
             String test = new String(buf);
 
             int ndx = test.indexOf("<Image ");
             if (ndx == -1) {
               byte[] b = buf;
               System.arraycopy(b, 8192 - 15, buf, 0, 14);
             }
             else {
               found = true;
               in.seek(in.getFilePointer() - (8192 - ndx));
             }
           }
           else {
             throw new FormatException("Pixel data not found");
           }
         }
 
         int bufSize = ((Integer) offsets[i].get(0)).intValue() -
           in.getFilePointer();
         buf = new byte[bufSize];
       }
       in.read(buf);
     }
 
     OMENode ome = null;
     try {
       ome = new OMENode(new File(getMappedId(id)));
     }
     catch (Exception exc) { throw new FormatException(exc); }
     MetadataStore store = getMetadataStore(id);
     store.setRoot(ome);
 
     width = new int[numDatasets];
     height = new int[numDatasets];
     numImages = new int[numDatasets];
     bpp = new int[numDatasets];
    littleEndian = new boolean[numDatasets];
     compression = new String[numDatasets];
     numChannels = new int[numDatasets];
     numT = new int[numDatasets];
     numZ = new int[numDatasets];
     order = new String[numDatasets];
     pixelType = new int[numDatasets];
 
     int oldSeries = getSeries(currentId);
 
     omexml.setRoot(ome);
     for (int i=0; i<numDatasets; i++) {
       setSeries(currentId, i);
       Integer ndx = new Integer(i);
       width[i] = omexml.getSizeX(ndx).intValue();
       height[i] = omexml.getSizeY(ndx).intValue();
       numT[i] = omexml.getSizeT(ndx).intValue();
       numZ[i] = omexml.getSizeZ(ndx).intValue();
       numChannels[i] = omexml.getSizeC(ndx).intValue();
 
       String type = omexml.getPixelType(ndx).toLowerCase();
       if (type.endsWith("16")) {
         bpp[i] = 2;
         pixelType[i] = type.indexOf("u") == -1 ? FormatReader.INT16 :
           FormatReader.UINT16;
       }
       else if (type.endsWith("32")) {
         bpp[i] = 4;
         pixelType[i] = type.indexOf("u") == -1 ? FormatReader.INT32 :
           FormatReader.UINT32;
       }
       else if (type.equals("float")) {
         bpp[i] = 4;
         pixelType[i] = FormatReader.FLOAT;
       }
       else {
         bpp[i] = 1;
         pixelType[i] = type.indexOf("u") == -1 ? FormatReader.INT8 :
           FormatReader.UINT8;
       }
 
       order[i] = omexml.getDimensionOrder(ndx);
 
       // calculate the number of raw bytes of pixel data that we are expecting
       int expected = width[i] * height[i] * bpp[i];
 
       // find the compression type and adjust 'expected' accordingly
       in.seek(((Integer) offsets[i].get(0)).intValue());
       buf = new byte[256];
       in.read(buf);
       String data = new String(buf);
 
       int compressionStart = data.indexOf("Compression") + 13;
       int compressionEnd = data.indexOf("\"", compressionStart);
       if (compressionStart != -1 && compressionEnd != -1) {
         compression[i] = data.substring(compressionStart, compressionEnd);
       }
       else compression[i] = "none";
 
       expected /= 2;
 
       in.seek(((Integer) offsets[i].get(0)).intValue());
 
       searchForData(expected, numZ[i] * numT[i] * numChannels[i]);
       numImages[i] = offsets[i].size();
       if (numImages[i] < (numZ[i] * numT[i] * numChannels[i])) {
         // hope this doesn't happen too often
         in.seek(((Integer) offsets[i].get(0)).intValue());
         searchForData(0, numZ[i] * numT[i] * numChannels[i]);
         numImages[i] = offsets[i].size();
       }
     }
     setSeries(currentId, oldSeries);
     sizeX = width;
     sizeY = height;
     sizeZ = numZ;
     sizeC = numChannels;
     sizeT = numT;
     currentOrder = order;
   }
 
   // -- Helper methods --
 
   /** Searches for BinData elements, skipping 'safe' bytes in between. */
   private void searchForData(int safe, int numPlanes) throws IOException {
     int iteration = 0;
     boolean found = false;
     if (offsets[series].size() > 1) {
       Object zeroth = offsets[series].get(0);
       offsets[series].clear();
       offsets[series].add(zeroth);
     }
 
     in.skipBytes(1);
     while (((in.getFilePointer() + safe) < in.length()) &&
       (offsets[series].size() < numPlanes))
     {
       in.skipBytes(safe);
 
       // look for next BinData element
       found = false;
       byte[] buf = new byte[8192];
       while (!found) {
         if (in.getFilePointer() < in.length()) {
           int numRead = in.read(buf, 20, buf.length - 20);
           String test = new String(buf);
 
           // datasets with small planes could have multiple sets of pixel data
           // in this block
           int ndx = test.indexOf("<Bin");
           while (ndx != -1) {
             found = true;
             if (numRead == buf.length - 20) numRead = buf.length;
             offsets[series].add(new Integer(
               (int) in.getFilePointer() - (numRead - ndx)));
             ndx = test.indexOf("<Bin", ndx+1);
           }
         }
         else {
           found = true;
         }
       }
 
       iteration++;
     }
   }
 
   // -- Main method --
 
   public static void main(String[] args) throws FormatException, IOException {
     new OMEXMLReader().testRead(args);
   }
 
 }
