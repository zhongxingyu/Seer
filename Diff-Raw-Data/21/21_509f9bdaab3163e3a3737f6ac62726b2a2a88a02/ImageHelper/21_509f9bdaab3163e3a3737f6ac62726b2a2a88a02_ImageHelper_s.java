 /*
  *  This file is part of Pac Defence.
  *
  *  Pac Defence is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Pac Defence is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Pac Defence.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  (C) Liam Byrne, 2008 - 09.
  */
 
 package images;
 
 import gui.Skin;
 
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.awt.image.BufferedImageOp;
 import java.awt.image.WritableRaster;
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Arrays;
 
 import javax.imageio.ImageIO;
 
 import logic.Helper;
 
 public class ImageHelper {
    
    private static String skin;
    
    public static void setSkin(Skin newSkin) {
       String skinDir = newSkin.getDirectory();
       skin = (skinDir == null) ? null : "skins/" + skinDir + "/";
    }
    
    public static String createPath(boolean useSkin, String... foldersAndFileName) {
       // skin == null means to use the default skin
       String path = useSkin && (skin != null) ? skin : "";
       for (String s : foldersAndFileName) {
          path += s + "/";
       }
       // Removes the extra "/"
       return path.substring(0, path.length() - 1);
    }
    
    public static URL createImageURL(String... foldersAndFileName) {
       // Look in the skin first
       URL withSkin = ImageHelper.class.getResource(createPath(true, foldersAndFileName));
       if(withSkin == null) { // If the image isn't found, load the default image
          return ImageHelper.class.getResource(createPath(false, foldersAndFileName));
       } else {
          return withSkin;
       }
    }
 
    public static BufferedImage makeImage(String... foldersAndFileName) {
       URL imageURL = createImageURL(foldersAndFileName);
       if (imageURL == null) {
          throw new IllegalArgumentException("Image: '" + Arrays.asList(foldersAndFileName) +
                "' not found.");
       }
       try {
          return ImageIO.read(imageURL);
       } catch (IOException e) {
          throw new RuntimeException("Error occured while reading: " + imageURL);
       }
    }
 
    public static BufferedImage makeImage(int width, int height, String... foldersAndFileName) {
       return resize(makeImage(foldersAndFileName), width, height);
    }
 
    /**
     * Makes a new BufferedImage that is a scaled version of that given with
     * specified quality.
     * 
     * @param image
     *        The image to scale.
     * @param width
     *        The width to scale the image to.
     * @param height
     *        The height to scale the image to.
     * @return A scaled version of image.
     */
    public static BufferedImage resize(BufferedImage image, int width, int height) {
       BufferedImage temp = new BufferedImage(width, height, getImageType(image));
       Graphics2D g = temp.createGraphics();
       g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
             RenderingHints.VALUE_INTERPOLATION_BICUBIC);
       g.drawImage(image, 0, 0, width, height, null);
       return temp;
    }
 
    public static BufferedImage rotateImage(BufferedImage image, double angle) {
       BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(),
             getImageType(image));
       AffineTransform at = AffineTransform.getRotateInstance(angle, image.getWidth() / 2,
             image.getHeight() / 2);
       BufferedImageOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
       temp.createGraphics().drawImage(image, op, 0, 0);
       return temp;
    }
 
    public static BufferedImage rotateImage(BufferedImage image, double dx, double dy) {
       return rotateImage(image, Helper.vectorAngle(dx, dy));
    }
    
    public static BufferedImage cloneImage(BufferedImage image) {
       BufferedImage clone = new BufferedImage(image.getWidth(), image.getHeight(),
             getImageType(image));
       clone.getGraphics().drawImage(image, 0, 0, null);
       return clone;
    }
    
    public static void clearImage(BufferedImage image) {
       WritableRaster r = image.getRaster();
       // This object represents a completely transparent pixel
       Object o = image.getColorModel().getDataElements(0, null);
       for(int x = 0; x < image.getWidth(); x++) {
          for(int y = 0; y < image.getHeight(); y++) {
             r.setDataElements(x, y, o);
          }
       }
    }
    
    public static void writePNG(BufferedImage image, String... foldersAndFileName) {
       File f;
       try {
          // The parent directory of this file
          f = new File(createImageURL(Arrays.copyOfRange(foldersAndFileName, 0,
                foldersAndFileName.length - 1)).toURI());
       } catch (URISyntaxException e) {
          throw new RuntimeException(e);
       }
       f = new File(f, foldersAndFileName[foldersAndFileName.length - 1]);
       try {
          ImageIO.write(image, "png", f);
       } catch (IOException e) {
          throw new RuntimeException(e);
       }
    }
    
    private static int getImageType(BufferedImage image) {
      int type = image.getType();
      if(type == BufferedImage.TYPE_CUSTOM) {
         // For a custom type use a generic type that should handle most images
         type = BufferedImage.TYPE_INT_ARGB;
       }
      return type;
    }
 }
