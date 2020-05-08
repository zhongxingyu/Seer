 /*
  */
 
 package booknaviger.picturehandler;
 
 import booknaviger.MainInterface;
 import booknaviger.exceptioninterface.InfoInterface;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.awt.image.PixelGrabber;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 
 /**
  * @author Inervo
  * This class is used to read and manipulate an image from many source
  */
 public class ImageReader {
     
     BufferedImage image = null;
     Object imageObject = null;
     
     /**
      * Constructor with an image as reference
      * @param image the image to manipulate
      */
     public ImageReader(Image image) {
         Logger.getLogger(ImageReader.class.getName()).entering(ImageReader.class.getName(), "ImageReader", image);
         imageObject = image;
         Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "ImageReader");
     }
 
     /**
      * Constructor with a file as a reference
      * @param imageFile the path to an image
      */
     public ImageReader(File imageFile) {
         Logger.getLogger(ImageReader.class.getName()).entering(ImageReader.class.getName(), "ImageReader", imageFile);
         imageObject = imageFile;
         Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "ImageReader");
     }
 
     /**
      * Constructor with an inputstream as a reference
      * @param imageIS The inputstream to an image
      */
     public ImageReader(InputStream imageIS) {
         Logger.getLogger(ImageReader.class.getName()).entering(ImageReader.class.getName(), "ImageReader", imageIS);
         imageObject = imageIS;
         Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "ImageReader");
     }
     
     /**
      * convert the imageObject to a bufferedImage
      * @return the BufferedImage created from the imageObject read in constructor
      */
     public BufferedImage convertImageToBufferedImage() {
         Logger.getLogger(ImageReader.class.getName()).entering(ImageReader.class.getName(), "convertImageToBufferedImage");
         if (imageObject.getClass().getName().toLowerCase().contains("image")) {
             Image tampon = (Image) imageObject;
             if (image != null) {
                 image.flush();
             }
             if (tampon.getWidth(null) == -1 || tampon.getHeight(null) == -1) {
                 Logger.getLogger(ImageReader.class.getName()).log(Level.SEVERE, "Image can't be read and converted for showing on screen");
                 new InfoInterface(InfoInterface.InfoLevel.ERROR, "image-convert");
                 Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "convertImageToBufferedImage", null);
                 return null;
             }
             image = new BufferedImage(tampon.getWidth(null), tampon.getHeight(null), BufferedImage.TYPE_INT_ARGB);
             Graphics2D g2d = image.createGraphics();
             g2d.drawImage(tampon, 0, 0, tampon.getWidth(null), tampon.getHeight(null), null);
             g2d.dispose();
             Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "convertImageToBufferedImage", image);
             return image;
         }
         Logger.getLogger(ImageReader.class.getName()).log(Level.SEVERE, "Trying to convert something else than an image");
         Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "convertImageToBufferedImage", null);
         return null;
     }
     
     /**
      * Read the image from either the file or the inputstream
      * @return the BufferedImage read
      */
     public BufferedImage readImage() {
         Logger.getLogger(ImageReader.class.getName()).entering(ImageReader.class.getName(), "readImage");
         Logger.getLogger(ImageReader.class.getName()).log(Level.FINE, "Searching imageObject type : {0}", imageObject.getClass().getName());
         if (image != null) {
             image.flush();
         }
         if (imageObject instanceof File) {
             if (((File)imageObject).getName().toLowerCase().endsWith(".jpg") || ((File)imageObject).getName().toLowerCase().endsWith(".jpeg") || ((File)imageObject).getName().toLowerCase().endsWith(".gif") || ((File)imageObject).getName().toLowerCase().endsWith(".png")) {
                 readWithFileToolkit();
             } else {
                 readWithFileImageIO();
             }
             Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "readImage", image);
             return image;
         }
        if (imageObject instanceof java.util.zip.InflaterInputStream) {
             readWithInputStreamImageIO();
             Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "readImage", image);
             return image;
         }
         if (imageObject instanceof java.io.PipedInputStream) {
             readWithInputStreamImageIO();
             Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "readImage", image);
             return image;
         }
         Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "readImage", null);
         return null;
     }
     
     /**
      * Rotate the picture to multiples of 90 degrees
      * @param originalImage the picture to rotate
      * @param rotationDegree the rotationdegree wanted
      * @return the rotated picture
      */
     public static BufferedImage rotatePicture(BufferedImage originalImage, int rotationDegree) {
         Logger.getLogger(ImageReader.class.getName()).entering(ImageReader.class.getName(), "rotatePicture", new Object[] {originalImage, rotationDegree});
         if (originalImage != null) {
             BufferedImage rotatedBufferedImage;
             if(rotationDegree == 180) {
                 rotatedBufferedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
             } else if (rotationDegree == 90 || rotationDegree == 270) {
                 rotatedBufferedImage = new BufferedImage(originalImage.getHeight(), originalImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
             } else if (rotationDegree == 0) {
                 return originalImage;
             } else {
                 Logger.getLogger(ImageReader.class.getName()).log(Level.WARNING, "The rotation wanted have not been found", rotationDegree);
                 Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "rotatePicture", null);
                 return null;
             }
             Graphics2D g2d = rotatedBufferedImage.createGraphics();
             g2d.rotate(Math.toRadians(rotationDegree), 0, 0);
             g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
             g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
             g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
             g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
             if (rotationDegree == 180) {
                 g2d.drawImage(originalImage, -originalImage.getWidth(), -originalImage.getHeight(), originalImage.getWidth(), originalImage.getHeight(), null);
             }
             else if (rotationDegree == 90) {
                 g2d.drawImage(originalImage, 0, -originalImage.getHeight(), originalImage.getWidth(), originalImage.getHeight(), null);
             }
             else if (rotationDegree == 270) {
                 g2d.drawImage(originalImage, -originalImage.getWidth(), 0, originalImage.getWidth(), originalImage.getHeight(), null);
             }
             g2d.dispose();
             originalImage.flush();
             Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "rotatePicture", rotatedBufferedImage);
             return rotatedBufferedImage;
         }
         Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "rotatePicture", null);
         return null;
     }
     
     /**
      * Combine two images as one (mutlipages)
      * @param imageLeft the image which will be on the left
      * @param imageRight the image which will be on the right
      * @return the image which combine the 2
      */
     public static BufferedImage combine2Images(BufferedImage imageLeft, BufferedImage imageRight) {
         Logger.getLogger(ImageReader.class.getName()).entering(ImageReader.class.getName(), "combine2Images", new Object[] {imageLeft, imageRight});
         if (imageLeft == null || imageRight == null) {
             Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "combine2Images", null);
             return null;
         }
         int totalWidth = imageLeft.getWidth() + imageRight.getWidth();
         int totalHeigh = (imageLeft.getHeight() < imageRight.getHeight()) ? imageRight.getHeight() : imageLeft.getHeight();
         BufferedImage imageCombined = new BufferedImage(totalWidth, totalHeigh, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g2d = imageCombined.createGraphics();
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
         g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
         g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
         g2d.setColor(findColor(imageLeft));
         g2d.fillRect(0, 0, totalWidth, totalHeigh);
         g2d.drawImage(imageLeft, 0, 0, imageLeft.getWidth(), imageLeft.getHeight(), null);
         g2d.drawImage(imageRight, imageLeft.getWidth(), 0, imageRight.getWidth(), imageRight.getHeight(), null);
         g2d.dispose();
         imageRight.flush();
         imageLeft.flush();
         Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "combine2Images", imageCombined);
         return imageCombined;
     }
     
     /**
      * Create a thumbnailImage from an orginal one
      * @param originalImage the original picture
      * @return A thumbnail of the original picture
      */
     public static BufferedImage getThumbnailImage(BufferedImage originalImage) {
         Logger.getLogger(ImageReader.class.getName()).entering(ImageReader.class.getName(), "getThumbnailImage", originalImage);
         if (originalImage == null) {
             Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "getThumbnailImage", null);
             return null;
         }
         int srcWidth = originalImage.getWidth(null);
         int srcHeight = originalImage.getHeight(null);
         float ratio = (float)srcHeight / (float)srcWidth;
         int destWidth = 300;
         int destHeight = (int) (300 * ratio);
         BufferedImage thumbnailImage = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g2d = thumbnailImage.createGraphics();
         g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g2d.drawImage(originalImage, 0, 0, destWidth, destHeight, null);
         g2d.dispose();
         Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "getThumbnailImage", thumbnailImage);
         return thumbnailImage;
     }
     
     /**
      * Find the color of the first pixel of an image
      * @param image The picture from which the first pixel color must be found
      * @return The color found
      */
     public static Color findColor(BufferedImage image) {
         Logger.getLogger(ImageReader.class.getName()).entering(ImageReader.class.getName(), "findColor", image);
         int[] tempo = new int[1];
         Color color;
 
         PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, tempo, 0, 1);
         try {
             pg.grabPixels();
         } catch (InterruptedException ex) {
             color = new Color(140, 140, 140);
             Logger.getLogger(ImageReader.class.getName()).log(Level.WARNING, "Color of the first pixel couldn't be found. Using default value");
             Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "findColor", color);
             return color;
         }
         int alpha = (tempo[0] & 0xff000000) >> 24;
         int red = (tempo[0] & 0x00ff0000) >> 16;
         int green = (tempo[0] & 0x0000ff00) >> 8;
         int blue = tempo[0] & 0x000000ff;
         if (alpha != 0) {
             color = new Color(red, green, blue);
         } else {
             color = new Color(140, 140, 140);
         }
         Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "findColor", color);
         return color;
     }
     
     /**
      * Read the imageObject as a file with ImageIO
      */
     private void readWithFileImageIO() {
         Logger.getLogger(ImageReader.class.getName()).entering(ImageReader.class.getName(), "readWithFileImageIO");
         try {
             image = ImageIO.read((File)imageObject);
         } catch (IOException ex) {
             Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
             new InfoInterface(InfoInterface.InfoLevel.ERROR, "file-read", ((File)imageObject).toString());
         }
         Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "readWithFileImageIO");
     }
     
     /**
      * Read the imageObject as a file with the Toolkit
      */
     private void readWithFileToolkit() {
         Logger.getLogger(ImageReader.class.getName()).entering(ImageReader.class.getName(), "readWithFileToolkit");
         Image tampon = Toolkit.getDefaultToolkit().createImage(((File)imageObject).toString());
         MediaTracker mt = new MediaTracker(MainInterface.getPreviewComponent());
         mt.addImage(tampon, 0);
         try {
             mt.waitForID(0);
         } catch (InterruptedException ex) {
             Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "image loading as been interrupted", ex);
         }
         image = new ImageReader(tampon).convertImageToBufferedImage();
         Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "readWithFileToolkit");
     }
 
     /**
      * Read the imageObject as an inputstream with ImageIO
      */
     private void readWithInputStreamImageIO() {
         Logger.getLogger(ImageReader.class.getName()).entering(ImageReader.class.getName(), "readWithInputStreamImageIO");
         try {
             image = ImageIO.read((InputStream)imageObject);
             ((InputStream)imageObject).close();
         } catch (IOException ex) {
             Logger.getLogger(ImageReader.class.getName()).log(Level.SEVERE, null, ex);
             new InfoInterface(InfoInterface.InfoLevel.ERROR, "file-read", imageObject.toString());
         } catch (ArrayIndexOutOfBoundsException ex) {
             Logger.getLogger(ImageReader.class.getName()).log(Level.SEVERE, "Image can't be read and converted for showing on screen");
             new InfoInterface(InfoInterface.InfoLevel.ERROR, "image-convert");
             Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "readWithInputStreamImageIO");
         }
         Logger.getLogger(ImageReader.class.getName()).exiting(ImageReader.class.getName(), "readWithInputStreamImageIO");
     }
 
 }
