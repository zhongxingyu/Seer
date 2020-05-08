 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2012 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the Software), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.image;
 
 import ij.ImagePlus;
 
 import java.awt.image.BufferedImage;
 
 
 /**
  * Represents a multidimensional image with some pixel data and associated metadata.
  *<p>
  * This interface extends the Collection<ImageCoordinate> interface, which allows for foreach-style iteration through
  * all the coordinates in the Image, without having users need to explicitly worry about things like number of dimensions or
  * bounds, etc.  This foreach-style iteration is the preferred method for operating on the pixel values of the Image.  No guarantee is made
  * about which dimension will be iterated over in which order, though this will remain constant for a given Image, and coordinates will always
  * be supplied in increasing order for a given dimension (until the maximum of that dimension is reached and it loops back to zero).
  *<p>
  * This class also has the capability of boxing (a single) region of interest in the Image and restricting the foreach-style iteration
  * solely to that box, if only a portion of the Image needs to be processed, viewed, etc.
  *<p>
  * Image requires methods only for reading of images; use {@link WritableImage} for read/write access to images.  Splitting these
  * allows methods using images to specify whether the images might be modified by the internals.
  *<p>
  * To create new Images, use {@link ImageFactory}.
  * 
  * @author Colin J. Fuller
  *
  */
 public interface Image extends java.util.Collection<ImageCoordinate>, java.io.Serializable {
 	
 	/**
 	 * Sets the region of interest in the Image.
 	 * <p>
 	 * The region of interest must be a (possibly high-dimensional) rectangle and is represented by two points, the lower bound of the coordinates
 	 * and the upper bound of the coordinates (conceptually like the upper-left and lower-right corners for the 2D case).  The box will include the
 	 * lower bound but exclude the upper bound.
 	 *<p>
 	 * The region of interest will control what area of the Image will be iterated over using foreach-style iteration, or
 	 * any of the methods of an {@link ImageIterator}.  This will be the sole region iterated over until {@link #clearBoxOfInterest()} is
 	 * called, or this method is called again with a new region of interest.  A new region of interest specified will replace any existing
 	 * region; it is not possible to construct complex regions by successively building with several rectangles.
 	 * <p>
 	 * As per the specification in {@link ImageCoordinate}, users are still responsible for recycling the ImageCoordinate parameters; they are
 	 * not retained and recycled.
 	 *
 	 * @param boxMin    The (inclusive) lower coordinate bound of the boxed region of interest.
 	 * @param boxMax    The (exclusive) upper coordinate bound of the boxed region of interest.
 	 */
 	public void setBoxOfInterest(ImageCoordinate boxMin, ImageCoordinate boxMax);

	public void setBoxOfInterest(ImageCoordinate boxMin, ImageCoordinate boxMax);
 	
 	/**
 	 * Clears any existing region of interest that has been set on this Image.
 	 *
 	 * This will cause any foreach-style iteration or ImageIterator-controlled iteration to iterate over the entire image.
 	 *
 	 */
 	public void clearBoxOfInterest();
 	
 	/**
 	 *  Gets the (exclusive) upper bound of any region of interest currently set on this Image, or null if no region is currently
 	 * set.
 	 * <p>
 	 * This is a reference to the actual internal ImageCoordinate and should not be modified or used after the region of interest has been cleared.
 	 * As per the specification in {@link ImageCoordinate}, users should <i>not</i> recycle the ImageCoordinate returned.
 	 *
 	 * @return  The ImageCoordinate whose components are the upper bound on the region of interest, or null if there is no region of interest.
 	 */
 	public ImageCoordinate getBoxMax();
 	
 	/**
 	 *  Gets the (inclusive) lower bound of any region of interest currently set on this Image, or null if no region is currently
 	 * set.
 	 * <p>
 	 * This is a reference to the actual internal ImageCoordinate and should not be modified or used after the region of interest has been cleared.
 	 * As per the specification in {@link ImageCoordinate}, users should <i>not</i> recycle the ImageCoordinate returned.
 	 *
 	 * @return  The ImageCoordinate whose components are the lower bound on the region of interest, or null if there is no region of interest.
 	 */
 	public ImageCoordinate getBoxMin();
 	
 	/**
 	 * Queries whether the Image is currently boxed with a region of interest.
 	 * @return  true if there is currently a region of interest set, false otherwise.
 	 */
 	public boolean getIsBoxed();
 	
 	/**
 	 * Gets the metadata associated with this Image.  (The object returned is an {@link loci.formats.meta.IMetadata} to facilitate
 	 * use with the LOCI bio-formats library.
 	 * @return  The metadata object associated with the Image.
 	 */
 	public loci.formats.meta.IMetadata getMetadata();
 	
 	/**
 	 * Creates a new Image that is a sub-image of the Image.
 	 * <p>
 	 * For a read-only image, the pixel data may or may not be copied.
 	 * <p>
 	 * The new Image will be created with bare-bones metadata and its pixel values set to the pixel values of this Image in the specifed region.
 	 * The new Image will have the dimension sizes specified (as if the Image constructor were called with this size parameter), and start at the specified
 	 * coordinate in the current Image (inclusive).  If the start point + new dimension sizes goes outside the current Image, then the new Image will be filled
 	 * with zeros at any coordinate outside the current Image.
 	 * <p>
 	 * As per the specification in {@link ImageCoordinate}, users are responsible for recycling the ImageCoordinate parameters; they are not retained
 	 * or recycled by this class.
 	 *
 	 *
 	 * @param newDimensions     An ImageCoordinate containing the size of the new sub-image in each dimension.
 	 * @param startPoint        The (inclusive) point in the current Image where the sub-image will start (this will become coordinate (0,0,0,...) in the new Image).
 	 * @return                  A new Image with pixel values set to those of the specified sub region of the current Image.
 	 */
 	public Image subImage(ImageCoordinate newDimensions, ImageCoordinate startPoint);
 	
 	/**
 	 * Writes the Image to a file.
 	 * <p>
 	 * The output format of the Image will be guessed from the extension.  Valid formats are those that the LOCI bio-formats library can write.
 	 * (The recommended option is the ome-tiff format, which should have the extension .ome.tif).
 	 *
 	 * @param filename  The full absolute path to the file to which the Image is to be written.  The parent directory must exist.
 	 */
 	public void writeToFile(String filename);
 	
 	/**
 	 * Gets the value of the Image at the coordinate specified.  No bounds checking is performed.
 	 * @param coord     An ImageCoordinate specifying the location of the value to retrieve.
 	 * @return          The value of the Image at the specified location as a float.
 	 */
 	public float getValue(ImageCoordinate coord);
 	
 	/**
 	 * Checks to see whether a specified ImageCoordinate represents a location in the Image.
 	 * @param c     The ImageCoordinate to check
 	 * @return      true, if the ImageCoordinate lies within this Image in every dimension, false otherwise.
 	 */
 	public boolean inBounds(ImageCoordinate c);
 	
 	/**
 	 * Returns an ImageCoordinate that contains the size of each dimension of the Image.
 	 * <p>
 	 * This ImageCoordinate should not be modified by users, nor should it be recycled by users.
 	 * 
 	 * @return      An ImageCoordinate containing the size of each dimension of the Image.
 	 */
 	public ImageCoordinate getDimensionSizes();
 	
 	/**
 	 * Method for converting a single Image with non-singleton specified dimension into a List of Images with
 	 * singleton specified dimension, each containing the Image for a single point along that dimension.
 	 * <p>
 	 * Images will be returned in the List in the order of their dimension index in the original Image.
 	 * @return      A List of Images, each with one point from the original Image.
 	 */
 	public java.util.List<Image> split(int dimension);
 	
 	/**
 	 * Convenience method for converting a single Image with non-singleton color dimension into a List of Images with
 	 * singleton color dimension, each containing the Image for a single color channel.
 	 * <p>
 	 * Images will be returned in the List in the order of their color dimension index in the original Image.
 	 * @return      A List of Images, each with one color channel from the original Image.
 	 */
 	public java.util.List<Image> splitChannels();
 	
 	/**
 	 * Converts the Image to a {@link BufferedImage} for use with other java imaging clases.
 	 *<p>
 	 * Because the Buffered Image can only represent single-plane images (possibly with down-sampled color channels), this
 	 * conversion may not convert all the Image data.
 	 *<p>
 	 * For multi-plane images, this will convert the first x-y plane only (that is, with c=0, t=0, z=0).  If you want a different
 	 * plane, create a single plane sub-image using {@link #subImage(ImageCoordinate, ImageCoordinate)}, and then convert that Image.
 	 * <p>
 	 * The output format is a 16-bit greyscale BufferedImage.
 	 *
 	 * @return  A BufferedImage containing the pixel values from the first plane of the Image converted to 16-bit greyscale format.
 	 */
 	public java.awt.image.BufferedImage toBufferedImage();
 	
 	/**
 	 * Converts the Image to an ImageJ ImagePlus.
 	 * 
 	 * @return	The converted Image.
 	 */
 	public ImagePlus toImagePlus();
 	
 	/**
 	 * Gets the number of planes in this Image (that is the number of distinct (z, c, t) coordinates).
 	 *
 	 * @return  The number of planes.
 	 */
 	public int getPlaneCount();
 	
 	/**
 	 * Selects a plane as active for iteration.  This has the same effect as calling the setBoxOfInterest method
 	 * with coordinates that would select only the given plane.  This should not be used in conjunction with the setBoxOfInterest method,
 	 * as internally this method uses the same boxing mechanism.  To clear the selected plane, call clearBoxOfInterest.
 	 * 
 	 * @param i     The plane index to set as active; no guarantee is made as to which plane this is, except that this index
 	 *              will always refer to the same plane for a given Image, and iterating from i = 0 to getPlaneCount() - 1 will
 	 *              visit all the planes in the Image.
 	 */
 	public void selectPlane(int i);
 	
 	/**
 	* Queries whether this Image's implementation can support writing.
 	* @return 	a boolean specifying whether this image can be written to.
 	*/
 	public boolean isWritable();
 	
 	/**
 	* Gets a writable version of this Image.  If the Image on which this method is
 	* called is already writable, then it is returned unchanged.  Otherwise, 
 	* a new object will be returned that contains a writable copy of the image data.
 	*/
 	public WritableImage getWritableInstance();
 	
 	/**
 	* Gets a PixelData instance that holds the image data.
 	*/
 	public PixelData getPixelData();
 	
 }
 
