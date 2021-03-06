 //
 // FormatReader.java
 //
 
 /*
 LOCI Bio-Formats package for reading and converting biological file formats.
 Copyright (C) 2005-2006 Melissa Linkert, Curtis Rueden, Chris Allan
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
 
 package loci.formats;
 
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Hashtable;
 import javax.swing.filechooser.FileFilter;
 import loci.util.FilePattern;
 
 /** Abstract superclass of all biological file format readers. */
 public abstract class FormatReader extends FormatHandler {
 
   // -- Constants --
 
   /** Debugging flag. */
   protected static final boolean DEBUG = false;
 
   /** Debugging level. 1=basic, 2=extended, 3=everything. */
   protected static final int DEBUG_LEVEL = 1;
 
   // -- Fields --
 
   /** Hashtable containing metadata key/value pairs. */
   protected Hashtable metadata;
 
   /** Flag set to true if multi-channel planes are to be separated. */
   protected boolean separated = false;
 
   /**
    * Current metadata store. Should <b>never</b> be accessed directly as the
    * semantics of {@link #getMetadataStore()} prevent "null" access.
    */
   private MetadataStore store = new DummyMetadataStore();
 
   // -- Constructors --
 
   /** Constructs a format reader with the given name and default suffix. */
   public FormatReader(String format, String suffix) { super(format, suffix); }
 
   /** Constructs a format reader with the given name and default suffixes. */
   public FormatReader(String format, String[] suffixes) {
     super(format, suffixes);
   }
 
   // -- Abstract FormatReader API methods --
 
   /** Checks if the given block is a valid header for this file format. */
   public abstract boolean isThisType(byte[] block);
 
   /** Determines the number of images in the given file. */
   public abstract int getImageCount(String id)
     throws FormatException, IOException;
 
   /** Checks if the images in the file are RGB. */
   public abstract boolean isRGB(String id)
     throws FormatException, IOException;
 
   /** Get the size of the X dimension. */
   public abstract int getSizeX(String id) throws FormatException, IOException;
 
   /** Get the size of the Y dimension. */
   public abstract int getSizeY(String id) throws FormatException, IOException;
 
   /** Get the size of the Z dimension. */
   public abstract int getSizeZ(String id) throws FormatException, IOException;
 
   /** Get the size of the C dimension. */
   public abstract int getSizeC(String id) throws FormatException, IOException;
 
   /** Get the size of the T dimension. */
   public abstract int getSizeT(String id) throws FormatException, IOException;
 
   /** Return true if the data is in little-endian format. */
   public abstract boolean isLittleEndian(String id)
     throws FormatException, IOException;
 
   /**
    * Return a five-character string representing the dimension order
    * within the file.
    */
   public abstract String getDimensionOrder(String id)
     throws FormatException, IOException;
 
   /** Obtains the specified image from the given file. */
   public abstract BufferedImage openImage(String id, int no)
     throws FormatException, IOException;
 
   /**
    * Obtains the specified image from the given file as a byte array.
    */
   public abstract byte[] openBytes(String id, int no)
     throws FormatException, IOException;
 
   /** Closes the currently open file. */
   public abstract void close() throws FormatException, IOException;
 
   // -- Internal FormatReader API methods --
 
   /**
    * Initializes the given file (parsing header information, etc.).
    * Most subclasses should override this method to perform
    * initialization operations such as parsing metadata.
    */
   protected void initFile(String id) throws FormatException, IOException {
     close();
     currentId = id;
     metadata = new Hashtable();
     // reinitialize the MetadataStore
     getMetadataStore(id).createRoot();
   }
 
   /**
    * Opens the given file, reads in the first few KB and calls
    * isThisType(byte[]) to check whether it matches this format.
    */
   protected boolean checkBytes(String name, int maxLen) {
     long len = new File(name).length();
     byte[] buf = new byte[len < maxLen ? (int) len : maxLen];
     try {
       FileInputStream fin = new FileInputStream(name);
       int r = 0;
       while (r < buf.length) r += fin.read(buf, r, buf.length - r);
       fin.close();
       return isThisType(buf);
     }
     catch (IOException e) { return false; }
   }
 
   // -- FormatReader API methods --
 
   /**
    * Opens an existing file from the given filename.
    *
    * @return Java Images containing pixel data
    */
   public BufferedImage[] openImage(String id)
     throws FormatException, IOException
   {
     int nImages = getImageCount(id);
     BufferedImage[] images = new BufferedImage[nImages];
     for (int i=0; i<nImages; i++) images[i] = openImage(id, i);
     close();
     return images;
   }
 
   /**
    * Allows the client to specify whether or not to separate channels.
    * By default, channels are left unseparated; thus if we encounter an RGB
    * image plane, it will be left as RGB and not split into 3 separate planes.
    */
   public void setSeparated(boolean separate) { separated = separate; }
 
   /** Gets whether channels are being separated. */
   public boolean isSeparated() { return separated; }
 
   /**
    * Obtains the specified metadata field's value for the given file.
    *
    * @param field the name associated with the metadata field
    * @return the value, or null if the field doesn't exist
    */
   public Object getMetadataValue(String id, String field)
     throws FormatException, IOException
   {
     if (!id.equals(currentId)) initFile(id);
     return metadata.get(field);
   }
 
   /**
    * Obtains the hashtable containing the metadata field/value pairs from
    * the given file.
    *
    * @param id the filename
    * @return the hashtable containing all metadata from the file
    */
   public Hashtable getMetadata(String id) throws FormatException, IOException {
     if (!id.equals(currentId)) initFile(id);
     return metadata;
   }
 
   /**
    * Sets the default metadata store for this reader.
    *
    * @param store a metadata store implementation.
    */
   public void setMetadataStore(MetadataStore store) {
     this.store = store;
   }
 
   /**
    * Retrieves the current metadata store for this reader. You can be
    * assured that this method will <b>never</b> return a <code>null</code>
    * metadata store.
    * @return a metadata store implementation.
    */
   public MetadataStore getMetadataStore(String id)
     throws FormatException, IOException
   {
     if (!id.equals(currentId)) initFile(id);
     return store;
   }
 
   /**
    * Retrieves the current metadata store's root object. It is guaranteed that
    * all file parsing has been performed by the reader prior to retrieval.
    * Requests for a full populated root object should be made using this method.
    * @param id a fully qualified path to the file.
    * @return current metadata store's root object fully populated.
    * @throws IOException if there is an IO error when reading the file specified
    * by <code>path</code>.
    * @throws FormatException if the file specified by <code>path</code> is of an
    * unsupported type.
    */
   public Object getMetadataStoreRoot(String id)
     throws FormatException, IOException {
     if (!id.equals(currentId)) initFile(id);
     return getMetadataStore(id).getRoot();
   }
 
   /**
    * A utility method for test reading a file from the command line,
    * and displaying the results in a simple display.
    */
   public boolean testRead(String[] args) throws FormatException, IOException {
     String id = null;
     boolean pixels = true;
     boolean stitch = false;
     boolean separate = false;
     boolean omexml = false;
     if (args != null) {
       for (int i=0; i<args.length; i++) {
         if (args[i].startsWith("-")) {
           if (args[i].equals("-nopix")) pixels = false;
           else if (args[i].equals("-stitch")) stitch = true;
           else if (args[i].equals("-separate")) separate = true;
           else if (args[i].equals("-omexml")) omexml = true;
           else System.out.println("Ignoring unknown command flag: " + args[i]);
         }
         else {
           if (id == null) id = args[i];
           else System.out.println("Ignoring unknown argument: " + args[i]);
         }
       }
     }
     if (id == null) {
       String className = getClass().getName();
       System.out.println("To test read a file in " + format + " format, run:");
       System.out.println("  java " + className + " [-nopix]");
       System.out.println("    [-stitch] [-separate] [-omexml] in_file");
       return false;
     }
     if (omexml) {
       try {
         Class c = Class.forName("loci.formats.OMEXMLMetadataStore");
         MetadataStore ms = (MetadataStore) c.newInstance();
         setMetadataStore(ms);
       }
       catch (Exception exc) { }
     }
 
     // check type
     System.out.print("Checking " + format + " format ");
     System.out.println(isThisType(id) ? "[yes]" : "[no]");
 
 
     ChannelMerger cm = new ChannelMerger(stitch ?
       new FileStitcher(this) : this);
 
     // read pixels
     if (pixels) {
       System.out.print("Reading " + (stitch ?
         FilePattern.findPattern(new File(id)) : id) + " pixel data ");
       long s1 = System.currentTimeMillis();
       cm.setSeparated(separate);
       int num = cm.getImageCount(id);
 
       System.out.print("(" + num + ") ");
       long e1 = System.currentTimeMillis();
       BufferedImage[] images = new BufferedImage[num];
       long s2 = System.currentTimeMillis();
       for (int i=0; i<num; i++) {
         images[i] = cm.openImage(id, i);
         System.out.print(".");
       }
       long e2 = System.currentTimeMillis();
       System.out.println(" [done]");
 
       // output timing results
       float sec = (e2 - s1) / 1000f;
       float avg = (float) (e2 - s2) / num;
       long initial = e1 - s1;
       System.out.println(sec + "s elapsed (" +
         avg + "ms per image, " + initial + "ms overhead)");
 
       // display pixels in image viewer
       ImageViewer viewer = new ImageViewer();
       viewer.setImages(id, format, images);
       viewer.show();
     }
 
     // read metadata
     System.out.println();
     System.out.print("Reading " + id + " metadata ");
     int imageCount = cm.getImageCount(id);
     boolean rgb = cm.isRGB(id);
     int sizeX = cm.getSizeX(id);
     int sizeY = cm.getSizeY(id);
     int sizeZ = cm.getSizeZ(id);
     int sizeC = cm.getSizeC(id);
     int sizeT = cm.getSizeT(id);
     boolean little = cm.isLittleEndian(id);
     String dimOrder = cm.getDimensionOrder(id);
     Hashtable meta = getMetadata(id);
     System.out.println("[done]");
 
     // output basic metadata
     System.out.println("-----");
     System.out.println("Image count = " + imageCount);
     System.out.print("RGB = " + rgb);
     if (rgb && separate) System.out.print(" (separated)");
     else if (!rgb && !separate) System.out.print(" (merged)");
     System.out.println();
     System.out.println("Width = " + sizeX);
     System.out.println("Height = " + sizeY);
     System.out.println("SizeZ = " + sizeZ);
     System.out.println("SizeC = " + sizeC);
     System.out.println("SizeT = " + sizeT);
     if (imageCount != sizeZ * sizeC * sizeT /
       ((rgb && !separate) ? sizeC : 1))
     {
       System.out.println("************ Warning: ZCT mismatch ************");
     }
     System.out.println("Endianness = " +
       (little ? "intel (little)" : "motorola (big)"));
     System.out.println("Dimension order = " + dimOrder);
     System.out.println("-----");
 
     // output metadata table
     String[] keys = (String[]) meta.keySet().toArray(new String[0]);
     Arrays.sort(keys);
     for (int i=0; i<keys.length; i++) {
       System.out.print(keys[i] + ": ");
       System.out.println(getMetadataValue(id, keys[i]));
     }
     System.out.println();
 
     // output OME-XML
    MetadataStore ms = cm.getMetadataStore(id);
    try {
      Method m = ms.getClass().getMethod("dumpXML", null);
      System.out.println("OME-XML:");
      System.out.println(m.invoke(ms, null));
      System.out.println();
    }
    catch (Exception exc) {
      System.err.println("OME-XML functionality not available:");
      exc.printStackTrace();
     }
 
     return true;
   }
 
   // -- FormatHandler API methods --
 
   /** Creates JFileChooser file filters for this file format. */
   protected void createFilters() {
     filters = new FileFilter[] { new FormatFileFilter(this) };
   }
 
 }
