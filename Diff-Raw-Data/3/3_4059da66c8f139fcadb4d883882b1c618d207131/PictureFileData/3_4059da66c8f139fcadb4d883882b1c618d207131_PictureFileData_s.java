 //
 // $Id: PictureFileData.java 287 2007-06-17 09:07:04 +0000 (dim., 17 juin 2007)
 // felfert $
 //
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 //
 // Created: 2006-05-09
 // Creator: etienne_sf
 // Last modified: $Date$
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.filedata;
 
 import java.awt.Canvas;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 
 import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReader;
 import javax.imageio.metadata.IIOMetadata;
 import javax.imageio.stream.FileImageInputStream;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 
 import wjhk.jupload2.exception.JUploadException;
 import wjhk.jupload2.exception.JUploadIOException;
 import wjhk.jupload2.filedata.helper.ImageHelper;
 import wjhk.jupload2.filedata.helper.ImageReaderWriterHelper;
 import wjhk.jupload2.policies.PictureUploadPolicy;
 import wjhk.jupload2.policies.UploadPolicy;
 
 /**
  * This class contains all data about files to upload as a picture. It adds the
  * following elements to the {@link wjhk.jupload2.filedata.FileData} class :<BR>
  * <UL>
  * <LI>Ability to define a target format (to convert pictures to JPG before
  * upload, for instance)
  * <LI>Optional definition of a maximal width and/or height.
  * <LI>Ability to rotate a picture, with {@link #addRotation(int)}
  * <LI>Ability to store a picture into a BufferedImage. This is actualy a bad
  * idea within an applet (should run within a java application) : the applet
  * runs very quickly out of memory. With pictures from my Canon EOS20D (3,5M), I
  * can only display two pictures. The third one generates an out of memory
  * error, despite the System.finalize and System.gc I've put everywhere in the
  * code!
  * </UL>
  * 
  * @author etienne_sf
  * @version $Revision$
  */
 public class PictureFileData extends DefaultFileData {
 
     /**
      * Indicate whether the data for this fileData has already been intialized.
      */
     // private boolean initialized = false;
     /**
      * Indicates if this file is a picture or not. This is bases on the return
      * of ImageIO.getImageReadersByFormatName().
      */
     private boolean isPicture = false;
 
     /**
      * If set to true, the PictureFileData will keep the BufferedImage in
      * memory. That is: it won't load it again from the hard drive, and resize
      * and/or rotate it (if necessary) when the user select this picture. When
      * picture are big this is nice. <BR>
      * <BR>
      * <B>Caution:</B> this parameter is currently unused, as the navigator
      * applet runs quickly out of memory (after three or four picture for my
      * Canon EOS 20D, 8,5 Mega pixels).
      * 
      * @see UploadPolicy
      */
     boolean storeBufferedImage = UploadPolicy.DEFAULT_STORE_BUFFERED_IMAGE;
 
     // Will be erased in the constructor.
 
     /*
      * bufferedImage contains a preloaded picture. This buffer is used according
      * to PictureFileDataPolicy.storeBufferedImage.
      * 
      * @see PictureUploadPolicy#storeBufferedImage
      */
     // private BufferedImage bufferedImage = null;
     // Currently commented, as it leads to memory leaks.
     /**
      * This picture is precalculated, and stored to avoid to calculate it each
      * time the user select this picture again, or each time the use switch from
      * an application to another.
      */
     private Image offscreenImage = null;
 
     /**
      * quarterRotation contains the current rotation that will be applied to the
      * picture. Its value should be one of 0, 1, 2, 3. It is controled by the
      * {@link #addRotation(int)} method.
      * <UL>
      * <LI>0 means no rotation.
      * <LI>1 means a rotation of 90� clockwise (word = Ok ??).
      * <LI>2 means a rotation of 180�.
      * <LI>3 means a rotation of 900 counterclockwise (word = Ok ??).
      * </UL>
      */
     int quarterRotation = 0;
 
     /**
      * Width of the original picture. The width is taken from the first image of
      * the file. We expect that all pictures in a file are of the same size (for
      * instance, for animated gif). Calculated in the
      * {@link #PictureFileData(File, File, PictureUploadPolicy)} constructor.
      */
     int originalWidth = -1;
 
     /**
      * Same as {@link #originalWidth}, for the height of the first image in the
      * picture file.
      */
     int originalHeight = -1;
 
     /**
      * transformedPictureFile contains the reference to the temporary file that
      * stored the transformed picture, during upload. It is created by
      * {@link #getInputStream()} and freed by {@link #afterUpload()}.
      */
     private File transformedPictureFile = null;
 
     /**
      * uploadLength contains the uploadLength, which is : <BR>
      * - The size of the original file, if no transformation is needed. <BR>
      * - The size of the transformed file, if a transformation were made. <BR>
      * <BR>
      * It is set to -1 whenever the user ask for a rotation (current only action
      * that need to recalculate the picture).
      */
     private long uploadLength = -1;
 
     /**
      * Contains the reference to a copy of the original picture files.
      * Originally created because a SUN bug would prevent picture to be
      * correctly resized if the original picture filename contains accents (or
      * any non-ASCII characters).
      */
 
     private File workingCopyTempFile = null;
 
     /**
      * Standard constructor: needs a PictureFileDataPolicy.
      * 
      * @param file The files which data are to be handled by this instance.
      * @param root The root directory, to calculate the relative dir (see
      *            {@link #getRelativeDir()}.
      * @param uploadPolicy The current upload policy
      * @throws JUploadIOException Encapsulation of the IOException, if any would
      *             occurs.
      */
     public PictureFileData(File file, File root,
             PictureUploadPolicy uploadPolicy) throws JUploadIOException {
         super(file, root, uploadPolicy);
         // EGR Should be useless
         // this.uploadPolicy = (PictureUploadPolicy) super.uploadPolicy;
         this.storeBufferedImage = uploadPolicy.hasToStoreBufferedImage();
 
         String fileExtension = getFileExtension();
 
         // Is it a picture?
         this.isPicture = isFileAPictrue(file);
 
         // Let's log the test result
         uploadPolicy.displayDebug("isPicture=" + this.isPicture + " ("
                 + file.getName() + "), extension=" + fileExtension, 50);
 
         // If it's a picture, we override the default mime type:
         if (this.isPicture) {
             setMimeTypeByExtension(fileExtension);
         }
     }
 
     /**
      * Free any available memory. This method is called very often here, to be
      * sure that we don't use too much memory. But we still run out of memory in
      * some case.
      * 
      * @param caller Indicate the method or treatment from which this method is
      *            called.
      */
     public void freeMemory(String caller) {
         Runtime rt = Runtime.getRuntime();
 
         // rt.runFinalization();
         rt.gc();
 
         this.uploadPolicy.displayDebug("freeMemory (after " + caller + ") : "
                 + rt.freeMemory(), 50);
     }
 
     /**
      * If this pictures needs transformation, a temporary file is created. This
      * can occurs if the original picture is bigger than the maxWidth or
      * maxHeight, of if it has to be rotated. This temporary file contains the
      * transformed picture. <BR>
      * The call to this method is optional, if the caller calls
      * {@link #getUploadLength()}. This method calls beforeUpload() if the
      * uploadLength is unknown.
      */
     @Override
     public void beforeUpload() throws JUploadException {
         if (this.uploadLength < 0) {
             try {
                 // Get the transformed picture file, if needed.
                 initTransformedPictureFile();
 
             } catch (OutOfMemoryError e) {
                 // Oups ! My EOS 20D has too big pictures to handle more than
                 // two pictures in a navigator applet !!!!!
                 // :-(
                 //
                 // We don't transform it. We clean the file, if it has been
                 // created.
                 deleteTransformedPictureFile();
                 //
                tooBigPicture();
             }
 
             // If the transformed picture is correctly created, we'll upload it.
             // Else we upload the original file.
             if (this.transformedPictureFile != null) {
                 this.uploadLength = this.transformedPictureFile.length();
             } else {
                 this.uploadLength = getFile().length();
             }
         }
 
         // Let's check that everything is Ok
         super.beforeUpload();
     }
 
     /**
      * Returns the number of bytes, for this upload. If needed, that is, if
      * uploadlength is unknown, {@link #beforeUpload()} is called.
      * 
      * @return The length of upload. In this class, this is ... the size of the
      *         original file, or the transformed file!
      */
     @Override
     public long getUploadLength() throws JUploadException {
         if (this.uploadLength < 0) {
             // Hum, beforeUpload should have been called before. Let's correct
             // that.
             beforeUpload();
         }
         return this.uploadLength;
     }
 
     /**
      * This function create an input stream for this file. The caller is
      * responsible for closing this input stream. <BR>
      * This function assumes that the {@link #getUploadLength()} method has
      * already be called : it is responsible for creating the temporary file (if
      * needed). If not called, the original file will be sent.
      * 
      * @return An inputStream
      */
     @Override
     public InputStream getInputStream() throws JUploadException {
         // Do we have to transform the picture ?
         if (this.transformedPictureFile != null) {
             try {
                 return new FileInputStream(this.transformedPictureFile);
             } catch (FileNotFoundException e) {
                 throw new JUploadIOException(e);
             }
         }
         // Otherwise : we read the file, in the standard way.
         return super.getInputStream();
     }
 
     /**
      * Cleaning of the temporary file on the hard drive, if any. <BR>
      * <B>Note:</B> if the debugLevel is 100 (or more) this temporary file is
      * not removed. This allow control of this created file.
      */
     @Override
     public void afterUpload() {
         super.afterUpload();
 
         // Free the temporary file ... if any.
         if (this.transformedPictureFile != null) {
             // for debug : if the debugLevel is enough, we keep the temporary
             // file (for check).
             if (this.uploadPolicy.getDebugLevel() >= 100) {
                 this.uploadPolicy.displayWarn("Temporary file not deleted");
             } else {
                 deleteTransformedPictureFile();
             }
         }
     }
 
     /**
      * This method creates a new Image, from the current picture. The resulting
      * width and height will be less or equal than the given maximum width and
      * height. The scale is maintained. Thus the width or height may be inferior
      * than the given values.
      * 
      * @param canvas The canvas on which the picture will be displayed.
      * @param shadow True if the pictureFileData should store this picture.
      *            False if the pictureFileData instance should not store this
      *            picture. Store this picture avoid calculating the image each
      *            time the user selects it in the file panel.
      * @return The rescaled image.
      * @throws JUploadException Encapsulation of the Exception, if any would
      *             occurs.
      */
     public Image getImage(Canvas canvas, boolean shadow)
             throws JUploadException {
         Image localImage = null;
 
         if (canvas == null) {
             throw new JUploadException(
                     "canvas null in PictureFileData.getImage");
         }
 
         int canvasWidth = canvas.getWidth();
         int canvasHeight = canvas.getHeight();
         if (canvasWidth <= 0 || canvasHeight <= 0) {
             this.uploadPolicy
                     .displayDebug(
                             "canvas width and/or height null in PictureFileData.getImage()",
                             1);
         } else if (shadow && this.offscreenImage != null) {
             // We take and return the previous calculated image for this
             // PictureFileData.
             localImage = this.offscreenImage;
         } else if (this.isPicture) {
             try {
                 // First: load the picture.
                 ImageReaderWriterHelper irwh = new ImageReaderWriterHelper(
                         (PictureUploadPolicy) this.uploadPolicy, this);
                 BufferedImage sourceImage = irwh.readImage(0);
                 irwh.dispose();
                 irwh = null;
                 ImageHelper ih = new ImageHelper(
                         (PictureUploadPolicy) this.uploadPolicy, this,
                         canvasWidth, canvasHeight, this.quarterRotation);
                 localImage = ih.getBufferedImage(
                         ((PictureUploadPolicy) this.uploadPolicy)
                                 .getHighQualityPreview(), sourceImage);
                 // We free memory ASAP.
                 sourceImage.flush();
                 sourceImage = null;
             } catch (OutOfMemoryError e) {
                 // Too bad
                 localImage = null;
                 tooBigPicture();
             }
         } // If isPicture
 
         // We store it, if asked to.
         if (shadow) {
             this.offscreenImage = localImage;
         }
 
         freeMemory("end of " + this.getClass().getName() + ".getImage()");
 
         // The picture is now loaded. We clear the progressBar
         this.uploadPolicy.getApplet().getUploadPanel()
                 .getPreparationProgressBar().setValue(0);
 
         return localImage;
     }// getImage
 
     /**
      * This function is used to rotate the picture. The current rotation state
      * is kept in the quarterRotation private attribute.
      * 
      * @param quarter Number of quarters (90 degrees) the picture should rotate.
      *            1 means rotating of 90 degrees clockwise. Can be negative.
      */
     public void addRotation(int quarter) {
         this.quarterRotation += quarter;
 
         // We'll have to recalculate the upload length, as the resulting file is
         // different.
         this.uploadLength = -1;
 
         // We keep the 'quarter' in the segment [0;4[
         while (this.quarterRotation < 0) {
             this.quarterRotation += 4;
         }
         while (this.quarterRotation >= 4) {
             this.quarterRotation -= 4;
         }
 
         // We need to change the precalculated picture, if any
         if (this.offscreenImage != null) {
             this.offscreenImage.flush();
             this.offscreenImage = null;
         }
     }
 
     /**
      * Indicates if this file is actually a picture or not.
      * 
      * @return the isPicture flag.
      */
     public boolean isPicture() {
         return this.isPicture;
     }
 
     /** @see FileData#getMimeType() */
     @Override
     public String getMimeType() {
         return this.mimeType;
     }
 
     // ///////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////////////// private METHODS
     // ///////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * File.deleteOnExit() is pretty unreliable, especially in applets.
      * Therefore the applet provides a callback which is executed during applet
      * termination. This method performs the actual cleanup.
      */
     public void deleteTransformedPictureFile() {
         if (null != this.transformedPictureFile) {
             this.transformedPictureFile.delete();
             this.transformedPictureFile = null;
             this.uploadLength = -1;
         }
     }
 
     /**
      * Creation of a temporary file, that contains the transformed picture. For
      * instance, it can be resized or rotated. This method doesn't throw
      * exception when there is an IOException within its procedure. If an
      * exception occurs while building the temporary file, the exception is
      * caught, a warning is displayed, the temporary file is deleted (if it was
      * created), and the upload will go on with the original file. <BR>
      * Note: any JUploadException thrown by a method called within
      * getTransformedPictureFile() will be thrown within this method.
      */
     private void initTransformedPictureFile() throws JUploadException {
         int targetMaxWidth;
         int targetMaxHeight;
 
         // If the image is rotated, we compare to realMaxWidth and
         // realMaxHeight, instead of maxWidth and maxHeight. This allows
         // to have a different picture size for rotated and not rotated
         // pictures. See the UploadPolicy javadoc for details ... and a
         // good reason ! ;-)
         if (this.quarterRotation == 0) {
             targetMaxWidth = ((PictureUploadPolicy) this.uploadPolicy)
                     .getMaxWidth();
             targetMaxHeight = ((PictureUploadPolicy) this.uploadPolicy)
                     .getMaxHeight();
         } else {
             targetMaxWidth = ((PictureUploadPolicy) this.uploadPolicy)
                     .getRealMaxWidth();
             targetMaxHeight = ((PictureUploadPolicy) this.uploadPolicy)
                     .getRealMaxHeight();
         }
 
         // Some Helper will .. help us !
         // I like useful comment :-)
         ImageHelper imageHelper = new ImageHelper(
                 (PictureUploadPolicy) this.uploadPolicy, this, targetMaxWidth,
                 targetMaxHeight, this.quarterRotation);
 
         // Should transform the file, and do we already created the transformed
         // file ?
         if (imageHelper.hasToTransformPicture()
                 && this.transformedPictureFile == null) {
 
             // We have to create a resized or rotated picture file, and all
             // needed information.
             // ...let's do it
             try {
                 createTranformedPictureFile(imageHelper);
             } catch (JUploadException e) {
                 // Hum, too bad.
                 // if any file was created, we remove it.
                 if (this.transformedPictureFile != null) {
                     this.transformedPictureFile.delete();
                     this.transformedPictureFile = null;
                 }
                 throw e;
             }
         }
     }// end of initTransformedPictureFile
 
     /**
      * Creates a transformed picture file of the given max width and max height.
      * If the {@link #transformedPictureFile} attribute is not set before
      * calling this method, it will be set. If set before, the existing
      * {@link #transformedPictureFile} is replaced by the newly transformed
      * picture file. It is cleared if an error occured. <BR>
      * 
      * @param imageHelper The {@link ImageHelper} that was initialized with
      *            current parameters.
      */
     void createTranformedPictureFile(ImageHelper imageHelper)
             throws JUploadException {
         IIOMetadata metadata = null;
         IIOImage iioImage = null;
         BufferedImage originalImage = null;
         BufferedImage transformedImage = null;
         ImageReaderWriterHelper imageWriterHelper = new ImageReaderWriterHelper(
                 (PictureUploadPolicy) this.uploadPolicy, this);
         boolean transmitMetadata = ((PictureUploadPolicy) this.uploadPolicy)
                 .getPictureTransmitMetadata();
 
         // Creation of the transformed picture file.
         createTransformedTempFile();
         imageWriterHelper.setOutput(this.transformedPictureFile);
 
         // How many picture should we read from the input file.
         // Default number of pictures is one.
         int nbPictures = 1;
         // For gif file, we put a max to MAX_VALUE, and we check the
         // IndexOutOfBoundsException to identify when we've read all pictures
         if (getExtension(getFile()).equalsIgnoreCase("gif")) {
             nbPictures = Integer.MAX_VALUE;
         }
         this.uploadPolicy.displayDebug(
                 "Reading image with imageWriterHelper.readImage(i)", 50);
         // Now, we have to read each picture from the original file, apply
         // the calculated transformation, and write each transformed picture
         // to the writer.
         // As indicated in javadoc for ImageReader.getNumImages(), we go
         // through pictures, until we get an IndexOutOfBoundsException.
         try {
             for (int i = 0; i < nbPictures; i += 1) {
                 originalImage = imageWriterHelper.readImage(i);
 
                 transformedImage = imageHelper.getBufferedImage(true,
                         originalImage);
 
                 // If necessary, we load the metadata for the current
                 // picture
                 if (transmitMetadata) {
                     metadata = imageWriterHelper.getImageMetadata(i);
                 }
 
                 iioImage = new IIOImage(transformedImage, null, metadata);
                 imageWriterHelper.write(iioImage);
 
                 // Let's clear picture, to force getBufferedImage to read a new
                 // one,
                 // in the next loop.
                 if (originalImage != null) {
                     originalImage.flush();
                     originalImage = null;
                 }
             }// for
         } catch (IndexOutOfBoundsException e) {
             // Was sent by imageWriterHelper.readImage(i)
             // Ok, no more picture to read. We just want to go out of
             // the loop. No error.
             this.uploadPolicy.displayDebug(
                     "IndexOutOfBoundsException catched: end of reading for file "
                             + getFileName(), 10);
         }
 
         if (originalImage != null) {
             originalImage.flush();
             originalImage = null;
         }
 
         // Let's free any used resource.
         imageWriterHelper.dispose();
 
     }
 
     /**
      * This method is called when an OutOfMemoryError occurs. This can easily
      * happen within the navigator, with big pictures: I've put a lot of
      * freeMemory calls within the code, but they don't seem to work very well.
      * When running from eclipse, the memory is freed Ok !
      */
     private void tooBigPicture() {
         String msg = String.format(
                 this.uploadPolicy.getString("tooBigPicture"), getFileName());
         JOptionPane.showMessageDialog(null, msg, "Warning",
                 JOptionPane.WARNING_MESSAGE);
         this.uploadPolicy.displayWarn(msg);
     }
 
     /**
      * This methods set the {@link DefaultFileData#mimeType} to the image mime
      * type, that should be associate with the picture.
      */
     private void setMimeTypeByExtension(String fileExtension) {
         String ext = fileExtension.toLowerCase();
         if (ext.equals("jpg")) {
             ext = "jpeg";
         }
         this.mimeType = "image/" + ext;
     }
 
     /**
      * If {@link #transformedPictureFile} is null, create a new temporary file,
      * and assign it to {@link #transformedPictureFile}. Otherwise, no action.
      * 
      * @throws IOException
      */
     private void createTransformedTempFile() throws JUploadIOException {
         if (this.transformedPictureFile == null) {
             try {
                 this.transformedPictureFile = File.createTempFile("jupload_",
                         ".tmp");
             } catch (IOException e) {
                 throw new JUploadIOException(
                         "PictureFileData.createTransformedTempFile()", e);
             }
             this.uploadPolicy.getApplet().registerUnload(this,
                     "deleteTransformedPictureFile");
             this.uploadPolicy.displayDebug("Using transformed temp file "
                     + this.transformedPictureFile.getAbsolutePath() + " for "
                     + getFileName(), 30);
         }
     }
 
     /**
      * This method loads the picture width and height of the picture. It's
      * called by the current instance when necessary.
      * 
      * @throws JUploadIOException
      * 
      * @see #getOriginalHeight()
      * @see #getOriginalWidth()
      */
     private void initWidthAndHeight() throws JUploadIOException {
         // Is it a picture?
         if (this.isPicture
                 && (this.originalHeight < 0 || this.originalWidth < 0)) {
             // Ok: it's a picture and is original width and height have not been
             // loaded yet.
             // In the windows world, file extension may be in uppercase, which
             // is not compatible with the core Java API.
             Iterator<ImageReader> iter = ImageIO
                     .getImageReadersByFormatName(getFileExtension()
                             .toLowerCase());
             if (iter.hasNext()) {
                 // It's a picture: we store its original width and height, for
                 // further calculation (rescaling and rotation).
                 try {
                     FileImageInputStream fiis = new FileImageInputStream(
                             getFile());
                     ImageReader ir = iter.next();
                     ir.setInput(fiis);
                     this.originalHeight = ir.getHeight(0);
                     this.originalWidth = ir.getWidth(0);
                     ir.dispose();
                     fiis.close();
                 } catch (IOException e) {
                     throw new JUploadIOException("PictureFileData()", e);
                 }
             }
         }
     }
 
     /**
      * If {@link #workingCopyTempFile} is null, create a new temporary file, and
      * assign it to {@link #transformedPictureFile}. Otherwise, no action.
      * 
      * @throws IOException
      */
     private void createWorkingCopyTempFile() throws IOException {
         if (this.workingCopyTempFile == null) {
             // The temporary file must have the correct extension, so that
             // native Java method works on it.
             this.workingCopyTempFile = File.createTempFile("jupload_", ".tmp."
                     + DefaultFileData.getExtension(getFile()));
             this.uploadPolicy.getApplet().registerUnload(this,
                     "deleteWorkingCopyPictureFile");
             this.uploadPolicy.displayDebug("Using working copy temp file "
                     + this.workingCopyTempFile.getAbsolutePath() + " for "
                     + getFileName(), 30);
         }
     }
 
     /**
      * File.deleteOnExit() is pretty unreliable, especially in applets.
      * Therefore the applet provides a callback which is executed during applet
      * termination. This method performs the actual cleanup.
      */
     public void deleteWorkingCopyPictureFile() {
         if (null != this.workingCopyTempFile) {
             this.workingCopyTempFile.delete();
             this.workingCopyTempFile = null;
         }
     }
 
     /**
      * Get the file that contains the original picture. This is used as a
      * workaround for the following JVM bug: once in the navigator, it can't
      * transform picture read from a file whose name contains non-ASCII
      * characters, like French accents.
      * 
      * @return The file that contains the original picture, as the source for
      *         picture transformation
      * @throws JUploadIOException
      */
     public File getWorkingSourceFile() throws JUploadIOException {
 
         if (this.workingCopyTempFile == null) {
             this.uploadPolicy.displayDebug(
                     "[getWorkingSourceFile] Creating a copy of "
                             + getFileName() + " as a source working target.",
                     30);
             FileInputStream is = null;
             FileOutputStream os = null;
             try {
                 createWorkingCopyTempFile();
 
                 is = new FileInputStream(getFile());
                 os = new FileOutputStream(this.workingCopyTempFile);
                 byte b[] = new byte[1024];
                 int l;
                 while ((l = is.read(b)) > 0) {
                     os.write(b, 0, l);
                 }
             } catch (IOException e) {
                 throw new JUploadIOException(
                         "ImageReaderWriterHelper.getWorkingSourceFile()", e);
             } finally {
                 if (is != null) {
                     try {
                         is.close();
                     } catch (IOException e) {
                         this.uploadPolicy
                                 .displayWarn(e.getClass().getName()
                                         + " while trying to close FileInputStream, in PictureUploadPolicy.copyOriginalToWorkingCopyTempFile.");
                     } finally {
                         is = null;
                     }
                 }
                 if (os != null) {
                     try {
                         os.close();
                     } catch (IOException e) {
                         this.uploadPolicy
                                 .displayWarn(e.getClass().getName()
                                         + " while trying to close FileOutputStream, in PictureUploadPolicy.copyOriginalToWorkingCopyTempFile.");
                     } finally {
                         os = null;
                     }
                 }
             }
         }
         return this.workingCopyTempFile;
     }// getWorkingSourceFile()
 
     /**
      * @return the originalWidth of the picture
      * @throws JUploadIOException
      */
     public int getOriginalWidth() throws JUploadIOException {
         initWidthAndHeight();
         return this.originalWidth;
     }
 
     /**
      * @return the originalHeight of the picture
      * @throws JUploadIOException
      */
     public int getOriginalHeight() throws JUploadIOException {
         initWidthAndHeight();
         return this.originalHeight;
     }
 
     // ////////////////////////////////////////////////////////////////////////////////////////////////////
     // /////////////////////// static methods
     // ////////////////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Returns an ImageIcon for the given file, resized according to the given
      * dimensions. If the original file contains a pictures smaller than these
      * width and height, the picture is returned as is (nor resized).
      * 
      * @param pictureFile The file, containing a picture, from which the user
      *            wants to extract a static picture.
      * @param maxWidth The maximum allowed width for the static picture to
      *            generate.
      * @param maxHeight The maximum allowed height for the static picture to
      *            generate.
      * @return The created static picture, or null if the file is null.
      */
     public static ImageIcon getImageIcon(File pictureFile, int maxWidth,
             int maxHeight) {
         ImageIcon thumbnail = null;
 
         if (pictureFile != null) {
             ImageIcon tmpIcon = new ImageIcon(pictureFile.getPath());
             if (tmpIcon != null) {
                 double scaleWidth = ((double) maxWidth)
                         / tmpIcon.getIconWidth();
                 double scaleHeight = ((double) maxHeight)
                         / tmpIcon.getIconHeight();
                 double scale = Math.min(scaleWidth, scaleHeight);
 
                 if (scale < 1) {
                     thumbnail = new ImageIcon(tmpIcon.getImage()
                             .getScaledInstance(
                                     (int) (scale * tmpIcon.getIconWidth()),
                                     (int) (scale * tmpIcon.getIconHeight()),
                                     Image.SCALE_FAST));
                 } else { // no need to miniaturize
                     thumbnail = tmpIcon;
                 }
             }
         }
         return thumbnail;
     }
 
     /**
      * Indicates whether a file is a picture or not. The information is based on
      * the fact the an ImageRead is found, or not, for this file. This test uses
      * the core Java API. As in the windows world, file extension may be in
      * uppercase, the test is based on the lowercase value for the given file
      * extension.
      * 
      * @param file
      * @return true if the file can be opened as a picture, false otherwise.
      */
     public static boolean isFileAPictrue(File file) {
         // In the windows world, file extension may be in uppercase, which is
         // not compatible with the core Java API.
         Iterator<ImageReader> iter = ImageIO
                 .getImageReadersByFormatName(DefaultFileData.getExtension(file)
                         .toLowerCase());
         return iter.hasNext();
     }
 }
