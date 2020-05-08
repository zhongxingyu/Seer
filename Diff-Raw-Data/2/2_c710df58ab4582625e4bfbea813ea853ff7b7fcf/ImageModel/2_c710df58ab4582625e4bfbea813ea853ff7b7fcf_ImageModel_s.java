 /*
  * FIT VUT - 2013 - GJA project 1 - Photo viewer
  * 
  * Ondrej Fibich <xfibic01@stud.fit.vutbr.cz>
  */
 package cz.vutbr.fit.gja.project.model;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.Arrays;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 
 /**
  * Encasement for working with images in the current directory with one of them
  * set up as the current image.
  *
  * @author Ondrej Fibich
  */
 public class ImageModel {
     
     /**
      * Path to the current image file
      */
     private File currentImageFile;
     
     /**
      * Current image
      */
     private ImageIcon currentImage;
     
     /**
      * Is image modified (rotated, ..)
      */
     private boolean modified = false;
     
     /**
      * Filter for images in a directory
      */
     public static final FilenameFilter filter = new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
             final String lname = name.toLowerCase();
             return lname.endsWith(".png") || lname.endsWith(".jpg") ||
                     lname.endsWith(".gif") || lname.endsWith(".jpeg");
         }
     };
 
     /**
      * Creates a new image model
      */
     public ImageModel() {
     }
     
     /**
      * List image files in the given directory
      * 
      * @param dir directory
      * @return listed images files
      */
     private File[] listImagesOfDir(File dir) {
         // not a dir
         if (!dir.isDirectory()) {
             throw new IllegalArgumentException("Not a dir");
         }
         // list
         return dir.listFiles(filter);
     }
     
     /**
      * Sets the image as the current.
      * 
      * @param img 
      * @throws IOException on error in reading
      */
     public void open(File img) throws IOException {
         // wrong image
         if (img == null) {
             throw new NullPointerException("Empty image file given");
         }
         // noe readable
         if (!img.canRead()) {
             throw new IllegalArgumentException("Cannot read from the given image file");
         }
         // store
         currentImageFile = img;
         currentImage = new ImageIcon(img.getAbsolutePath());
         modified = false;
         // not valid
         if (currentImage == null || currentImage.getIconWidth() < 0) {
             currentImage = null;
             currentImageFile = null;
             throw new IOException("cannot get image data");
         }
     }
     
     /**
      * Is an image opened?
      * 
      * @return is it? 
      */
     public boolean isOpened() {
         return currentImageFile != null;
     }
     
     /**
      * Checks if there is a next image from the current position
      * 
      * @return is there?
      */
     public boolean hasNext() {
         if (currentImageFile == null) {
             return false;
         }
         return (indexOfImage() < (countOfImages() - 1));
     }
     
     /**
      * Checks if there is a previous image from the current position
      * 
      * @return is there?
      */
     public boolean hasPrev() {
         if (currentImageFile == null) {
             return false;
         }
         return (indexOfImage() > 0);
     }
     
     /**
      * Gets index of the current image in its directory (starts from zero)
      * 
      * @return index
      */
     public int indexOfImage() {
         File[] images = listImagesOfDir(currentImageFile.getParentFile());
         return Arrays.asList(images).indexOf(currentImageFile);
     }
     
     /**
      * Gets count of images in the current image directory
      * 
      * @return 
      */
     public int countOfImages() {
         return listImagesOfDir(currentImageFile.getParentFile()).length;
     }
     
     /**
      * Seek to a next image in the current image directory.
      * This function does not checks whether the next file exists in the directory.
      * 
      * @see ImageModel#hasNext()
      * @throws IOException on error in reading
      */
     public void next() throws IOException {
         File[] images = listImagesOfDir(currentImageFile.getParentFile());
         open(images[indexOfImage() + 1]);
     }
     
     /**
      * Gets the current image data.
      * 
      * @return image
      */
     public ImageIcon getData() {
         return this.currentImage;
     }
 
     /**
      * Gets the current image file
      * 
      * @return file
      */
     public File getFile() {
         return currentImageFile;
     }
     
     /**
      * Seek to a previous image in the current image directory.
      * This function does not checks whether the previous file exists in the directory.
      * 
      * @see ImageModel#hasPrev() 
      * @throws IOException on error in reading
      */
     public void prev() throws IOException {
         File[] images = listImagesOfDir(currentImageFile.getParentFile());
         open(images[indexOfImage() - 1]);
     }
     
     /**
      * Seek to the first image in the current image directory.
      * 
      * @throws IOException on error in reading
      */
     public void first() throws IOException {
         File[] images = listImagesOfDir(currentImageFile.getParentFile());
         open(images[0]);
     }
     
     /**
      * Seek to the first image in the current image directory.
      * 
      * @throws IOException on error in reading
      */
     public void last() throws IOException {
         File[] images = listImagesOfDir(currentImageFile.getParentFile());
         open(images[images.length - 1]);
     }
 
     /**
      * Gets possible extension names for conversation of this image.
      * 
      * @return array of strings that contains extensions
      */
     public String[] getPossibleExtensionForConvert() {
        final String lname = currentImageFile.getName();
         if (lname.endsWith(".jpg") || lname.endsWith(".jpeg")) {
             return new String[] {"png", "gif"};
         } else if (lname.endsWith(".png")) {
             return new String[] {"jpg", "gif"};
         } else if (lname.endsWith(".gif")) {
             return new String[] {"png", "jpg"};
         } else {
             return new String[] {};
         }
     }
 
     /**
      * Is image modified and not saved?
      * 
      * @return indicator
      */
     public boolean isModified() {
         return modified;
     }
     
     /**
      * Rotate the current image for angle
      * 
      * @param angle angle in radians
      */
     public void rotate(double angle) {
         // opened? or no rotation?
         if (!isOpened() || angle == 0) {
             return;
         }
         // rotate
         int w = currentImage.getIconWidth();
         int h = currentImage.getIconHeight();
         // create buffered image from the image icon
         BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g2d = (Graphics2D) bi.createGraphics();
         currentImage.paintIcon(null, g2d, 0, 0);
         g2d.dispose();
         // make transformation
         AffineTransform tx = new AffineTransform();
         tx.translate(h / 2, w / 2);
         tx.rotate(Math.PI / 2);
         tx.translate(-w / 2, -h / 2);
         // make transformation operation
         AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
         // apply to image
         BufferedImage rbi = new BufferedImage(h, w, bi.getType());
         op.filter(bi, rbi);
         // store result
         currentImage = new ImageIcon(rbi);
         // flag
         modified = true;
     }
     
     /**
      * Scale the current image to the given width and height
      * 
      * @param w width
      * @param h height
      */
     public void scale(int w, int h) {
         // opened? or no rotation?
         if (!isOpened()) {
             return;
         }
         // wrong args
         if (w <= 0 || h <= 0) {
             throw new IllegalArgumentException("Illegal args: " + w + 'x' + h);
         }
         // flag
         modified = true;
         // scale
         Image simg = currentImage.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
         currentImage = new ImageIcon(simg);
     }
 
     /**
      * Saves modified image file
      * 
      * @throws IOException on save error
      */
     public void save() throws IOException {
         // get current extension
         int extStart = currentImageFile.getName().lastIndexOf('.') + 1;
         String ext = currentImageFile.getName().substring(extStart);
         // save
         saveAs(currentImageFile, ext);
     }
     
     /**
      * Saves image to the given file and in given format
      * 
      * @param f file to save to
      * @param ext format (jpg, gif, png)
      * @throws IOException on save error
      */
     public void saveAs(File f, String ext) throws IOException {
         // check args
         if (f == null || ext == null || ext.length() != 3) {
             throw new IllegalArgumentException("Wrong arguments for saveAs");
         }
         // check if modified
         if (!isModified() && f.equals(currentImageFile)) {
             throw new IllegalStateException();
         }
         // args
         int w = currentImage.getIconWidth();
         int h = currentImage.getIconHeight();
         // create buffered image from the image icon
         BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g2d = (Graphics2D) bi.getGraphics();
         currentImage.paintIcon(null, g2d, 0, 0);
         g2d.dispose();
         // save
         ImageIO.write(bi, ext, f);
         // set flag if not saved as
         if (f.equals(currentImageFile))
         {
             modified = false;
         }
     }
     
 }
