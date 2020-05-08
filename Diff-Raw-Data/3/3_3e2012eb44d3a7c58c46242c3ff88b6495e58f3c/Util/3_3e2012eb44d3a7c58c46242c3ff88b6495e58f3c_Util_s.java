 /*
  *  The OpenDiamond Platform for Interactive Search
  *  Version 5
  *
  *  Copyright (c) 2007, 2009-2010 Carnegie Mellon University
  *  All rights reserved.
  *
  *  This software is distributed under the terms of the Eclipse Public
  *  License, Version 1.0 which can be found in the file named LICENSE.
  *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
  *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
  */
 
 package edu.cmu.cs.diamond.opendiamond;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.CompletionService;
 import java.util.concurrent.ExecutionException;
 
 import javax.imageio.ImageIO;
 import javax.swing.Spring;
 import javax.swing.SpringLayout;
 
 /**
  * A class containing some static utility methods.
  */
 public class Util {
     private Util() {
     }
 
     // XXX endian specific
     /**
      * Extracts a little-endian <code>int</code> from a <code>byte[]</code>.
      * 
      * @param value
      *            the <code>byte</code> array
      * @return the <code>int</code>
      */
     public static int extractInt(byte[] value) {
         return (value[3] & 0xFF) << 24 | (value[2] & 0xFF) << 16
                 | (value[1] & 0xFF) << 8 | (value[0] & 0xFF);
     }
 
     /**
      * Extracts a little-endian <code>long</code> from a <code>byte[]</code>.
      * 
      * @param value
      *            the <code>byte</code> array
      * @return the <code>long</code>
      */
     public static long extractLong(byte[] value) {
         return ((long) (value[7] & 0xFF) << 56)
                 | ((long) (value[6] & 0xFF) << 48)
                 | ((long) (value[5] & 0xFF) << 40)
                 | ((long) (value[4] & 0xFF) << 32)
                 | ((long) (value[3] & 0xFF) << 24)
                 | ((long) (value[2] & 0xFF) << 16)
                 | ((long) (value[1] & 0xFF) << 8) | (value[0] & 0xFF);
     }
 
     /**
      * Extracts a little-endian <code>double</code> from a <code>byte[]</code>.
      * 
      * @param value
      *            the <code>byte</code> array
      * @return the <code>double</code>
      */
     public static double extractDouble(byte[] value) {
         return Double.longBitsToDouble(extractLong(value));
     }
 
     /**
      * Gets a scale value for resizing images.
      * 
      * @param w
      *            the existing width
      * @param h
      *            the existing height
      * @param maxW
      *            the desired maximum width
      * @param maxH
      *            the desired maximum height
      * @return a scale value
      */
     public static double getScaleForResize(int w, int h, int maxW, int maxH) {
         double scale = 1.0;
 
         double imgAspect = (double) w / h;
         double targetAspect = (double) maxW / maxH;
 
         if (imgAspect > targetAspect) {
             // more wide
             if (w > maxW) {
                 scale = (double) maxW / w;
             }
         } else {
             // more tall
             if (h > maxH) {
                 scale = (double) maxH / h;
             }
         }
 
         return scale;
     }
 
     /**
      * Scales an image.
      * 
      * @param img
      *            existing image
      * @param scale
      *            scale factor
      * @return a new scaled image
      */
     public static BufferedImage scaleImage(BufferedImage img, double scale) {
         BufferedImage dest = createCompatibleImage(img,
                 (int) (img.getWidth() * scale), (int) (img.getHeight() * scale));
 
         return scaleImage(img, dest);
     }
 
     /**
      * Scales an image into an existing <code>BufferedImage</code>
      * 
      * @param img
      *            existing image
      * @param dest
      *            destination image
      * @return destination image
      */
     private static BufferedImage scaleImage(BufferedImage img,
             BufferedImage dest) {
 
         Graphics2D g = dest.createGraphics();
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                 RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         g.drawImage(img, 0, 0, dest.getWidth(), dest.getHeight(), null);
         g.dispose();
 
         return dest;
     }
 
     private static BufferedImage createCompatibleImage(BufferedImage image,
             int width, int height) {
         if (GraphicsEnvironment.isHeadless()) {
             return new BufferedImage(width, height, image.getType());
         } else {
             return GraphicsEnvironment.getLocalGraphicsEnvironment()
                     .getDefaultScreenDevice().getDefaultConfiguration()
                     .createCompatibleImage(width, height,
                             image.getTransparency());
         }
     }
 
     /**
      * Scales an image using a fast, low quality algorithm.
      * 
      * @param img
      *            existing image
      * @param scale
      *            scale factor
      * @return a new scaled image
      */
     public static BufferedImage scaleImageFast(BufferedImage img, double scale) {
         BufferedImage dest = createCompatibleImage(img,
                 (int) (img.getWidth() * scale), (int) (img.getHeight() * scale));
         Graphics2D g = dest.createGraphics();
         g.drawImage(img, 0, 0, dest.getWidth(), dest.getHeight(), null);
         g.dispose();
 
         return dest;
     }
 
     // http://java.sun.com/docs/books/tutorial/uiswing/examples/layout/SpringGridProject/src/layout/SpringUtilities.java
     /* Used by makeCompactGrid. */
     private static SpringLayout.Constraints getConstraintsForCell(int row,
             int col, Container parent, int cols) {
         SpringLayout layout = (SpringLayout) parent.getLayout();
         Component c = parent.getComponent(row * cols + col);
         return layout.getConstraints(c);
     }
 
     /**
      * Aligns the first <code>rows</code> * <code>cols</code> components of
      * <code>parent</code> in a grid. Each component in a column is as wide as
      * the maximum preferred width of the components in that column; height is
      * similarly determined for each row. The parent is made just big enough to
      * fit them all.
      * 
      * @param parent
      *            container to put grid in
      * @param rows
      *            number of rows
      * @param cols
      *            number of columns
      * @param initialX
      *            x location to start the grid at
      * @param initialY
      *            y location to start the grid at
      * @param xPad
      *            x padding between cells
      * @param yPad
      *            y padding between cells
      */
     public static void makeCompactGrid(Container parent, int rows, int cols,
             int initialX, int initialY, int xPad, int yPad) {
         SpringLayout layout;
         try {
             layout = (SpringLayout) parent.getLayout();
         } catch (ClassCastException exc) {
             System.err
                     .println("The first argument to makeCompactGrid must use SpringLayout.");
             return;
         }
 
         // Align all cells in each column and make them the same width.
         Spring x = Spring.constant(initialX);
         for (int c = 0; c < cols; c++) {
             Spring width = Spring.constant(0);
             for (int r = 0; r < rows; r++) {
                 width = Spring.max(width, getConstraintsForCell(r, c, parent,
                         cols).getWidth());
             }
             for (int r = 0; r < rows; r++) {
                 SpringLayout.Constraints constraints = getConstraintsForCell(r,
                         c, parent, cols);
                 constraints.setX(x);
                 constraints.setWidth(width);
             }
             x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
         }
 
         // Align all cells in each row and make them the same height.
         Spring y = Spring.constant(initialY);
         for (int r = 0; r < rows; r++) {
             Spring height = Spring.constant(0);
             for (int c = 0; c < cols; c++) {
                 height = Spring.max(height, getConstraintsForCell(r, c, parent,
                         cols).getHeight());
             }
             for (int c = 0; c < cols; c++) {
                 SpringLayout.Constraints constraints = getConstraintsForCell(r,
                         c, parent, cols);
                 constraints.setY(y);
                 constraints.setHeight(height);
             }
             y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
         }
 
         // Set the parent's size.
         SpringLayout.Constraints pCons = layout.getConstraints(parent);
         pCons.setConstraint(SpringLayout.SOUTH, y);
         pCons.setConstraint(SpringLayout.EAST, x);
     }
 
     /**
      * Extracts a <code>String</code> from a <code>byte[]</code>.
      * 
      * @param value
      *            the <code>byte[]</code>
      * @return a string
      */
     public static String extractString(byte[] value) {
         try {
             return new String(value, 0, value.length - 1, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
             return "";
         }
     }
 
     /**
      * Reads an <code>InputStream</code> until EOF.
      * 
      * @param in
      *            the <code>InputStream</code> to read
      * @return the read data
      * @throws IOException
      *             if an IO error occurs
      */
     public static byte[] readFully(InputStream in) throws IOException {
         ByteArrayOutputStream out = new ByteArrayOutputStream();
 
         byte bb[] = new byte[4096];
 
         int amount;
         while ((amount = in.read(bb)) != -1) {
             out.write(bb, 0, amount);
         }
 
         return out.toByteArray();
     }
 
     /**
      * Streams one entry into a quick tar archive.
      * 
      * @param out
      *            the quick tar output stream
      * @param in
      *            the input
      * @param length
      *            number of bytes to read from <code>in</code>
      * @param name
      *            the name of this entry
      * @throws IOException
      *             if an IO error occurs
      */
     public static void quickTar1(DataOutputStream out, InputStream in,
             int length, String name) throws IOException {
 
         // write name length (+1 for zero termination)
         byte nameBytes[] = name.getBytes("UTF-8");
         out.writeInt(nameBytes.length + 1);
 
         // write size
         out.writeInt(length);
 
         // write name (zero-terminated)
         out.write(nameBytes);
         out.write(0);
 
         // write data
         for (int i = 0; i < length; i++) {
             out.write(in.read());
         }
     }
 
     /**
      * Creates a quick tar archive from an array of files.
      * 
      * @param files
      *            the input
      * @return the quick tar data
      * @throws IOException
      *             if an IO error occurs
      */
     public static byte[] quickTar(File files[]) throws IOException {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream(bos);
 
         for (File f : files) {
             if (f.isFile()) {
                 String name = f.getName();
                 long length = f.length();
                 BufferedInputStream in = new BufferedInputStream(
                         new FileInputStream(f));
                 try {
                     quickTar1(out, in, (int) length, name);
                 } finally {
                     in.close();
                 }
             }
         }
 
         return bos.toByteArray();
     }
 
     /**
      * Creates a quick tar archive from the files in a directory
      * (non-recursive).
      * 
      * @param directory
      *            the directory to create an archive from
      * @return the archive
      * @throws IllegalArgumentException
      *             if <code>directory</code> is not a directory
      * @throws IOException
      *             if an IO error occurs
      */
     public static byte[] quickTar(File directory) throws IOException {
         if (!directory.isDirectory()) {
             throw new IllegalArgumentException(directory + " must be directory");
         }
 
         return quickTar(directory.listFiles());
     }
 
     /**
      * Streams one entry into a quick tar archive.
      * 
      * @param out
      *            the quick tar output stream
      * @param buf
      *            the input
      * @param name
      *            the name of this entry
      * @throws IOException
      *             if an IO error occurs
      */
     public static void quickTar1(DataOutputStream out, byte[] buf, String name)
             throws IOException {
         ByteArrayInputStream in = new ByteArrayInputStream(buf);
         quickTar1(out, in, buf.length, name);
     }
 
     static void checkResultsForIOException(int size,
             CompletionService<?> connectionCreator) throws IOException,
             InterruptedException {
         for (int i = 0; i < size; i++) {
             try {
                 connectionCreator.take().get();
             } catch (ExecutionException e1) {
                 Throwable cause = e1.getCause();
                 if (cause instanceof IOException) {
                     throw (IOException) cause;
                 }
                 e1.printStackTrace();
             }
         }
     }
 
     /**
      * Extracts a BufferedImage from a Result.
      * 
      * @param r
      *            the result to extract an image from
      * @return an image, or <code>null</code> if no image can be decoded
      */
     public static BufferedImage extractImageFromResult(Result r) {
         // first, try rgbimage
         byte[] rgbimage = r.getValue("_rgb_image.rgbimage");
         if (rgbimage != null) {
             return decodeRGBImage(rgbimage);
         }
 
         // then, try ImageIO
         byte[] data = r.getData();
         if (data != null) {
             InputStream in = new ByteArrayInputStream(data);
             try {
                 return ImageIO.read(in);
             } catch (IOException e) {
                 e.printStackTrace();
                 return null;
             }
         }
 
         return null;
     }
 
     private static BufferedImage decodeRGBImage(byte[] rgbimage) {
         ByteBuffer buf = ByteBuffer.wrap(rgbimage);
         buf.order(ByteOrder.LITTLE_ENDIAN);
 
         // skip header
         buf.position(8);
 
         // sizes
         int h = buf.getInt();
         int w = buf.getInt();
 
         // do it
         BufferedImage result = new BufferedImage(w, h,
                 BufferedImage.TYPE_INT_RGB);
         int data[] = ((DataBufferInt) result.getRaster().getDataBuffer())
                 .getData();
         for (int i = 0; i < data.length; i++) {
             byte r = buf.get();
             byte g = buf.get();
             byte b = buf.get();
             buf.get();
 
             data[i] = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
         }
 
         return result;
     }
 
     /**
      * Retrieves an object from a server and extracts a BufferedImage from it.
      * 
      * @param objectIdentifier
      *            the identifier for the object to extract an image from
      * @param factory
      *            the factory to use to retrieve the object
      * @return an image, or <code>null</code> if no image can be decoded
      * @throws IOException
      *             if an IO error occurs while retrieving the object
      */
     public static BufferedImage extractImageFromResultIdentifier(
             ObjectIdentifier objectIdentifier, SearchFactory factory)
             throws IOException {
         Set<String> desiredAttributes = new HashSet<String>();
 
         // try to get the decoded image first, then the undecoded image
         desiredAttributes.add("_rgb_image.rgbimage");
         Result r2 = factory.generateResult(objectIdentifier, desiredAttributes);
 
         if (r2.getValue("_rgb_image.rgbimage") == null) {
             desiredAttributes.add("");
             r2 = factory.generateResult(objectIdentifier, desiredAttributes);
         }
 
         // decode
         return Util.extractImageFromResult(r2);
     }
     
     /**
     * Takes to (non-absolute) paths and joins them together appropriately depending on OS.
      * 
      * @param path1
      * 			the presumed beginning of the combined path
      * @param path2
      * 			the presumed ending of the combined path
      * @return a string representation of the combined path
      */
     static String joinPaths(String path1, String path2) {
     	    File file1 = new File(path1);
     	    File file2 = new File(file1, path2);
     	    return file2.getPath();
     }
     
     /**
      * 
      * This function returns a stacktrace as a <code>String</code>
      * given an input <code>Throwable</code> object.
      * 
      * @param aThrowable the exception being thrown
      * @return stacktrace of the exception as a string
      */
     static String getStackTrace(Throwable aThrowable) {
         final Writer result = new StringWriter();
         final PrintWriter printWriter = new PrintWriter(result);
         aThrowable.printStackTrace(printWriter);
         return result.toString();
       }
 }
