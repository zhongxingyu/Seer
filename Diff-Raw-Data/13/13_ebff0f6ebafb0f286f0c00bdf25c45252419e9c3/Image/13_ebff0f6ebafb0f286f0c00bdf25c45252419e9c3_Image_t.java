 /*
  *  BlinzCore - core library of audio, video, and other essential classes.
  *  Copyright (C) 2009  BlinzProject <gtalent2@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License version 3 as
  *  published by the Free Software Foundation.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.blinz.graphics;
 
 import net.blinz.util.Size;
 import com.sun.opengl.util.texture.Texture;
 import com.sun.opengl.util.texture.TextureIO;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 
 /**
  * Image class, instances represent images stored in file. To create an Image use
  * ImageLoader.
  * @author Blinz Project
  */
 public final class Image {
 
     /**
      * stub is a shared object among any Image object that point to the same file.
      * It contains all the actual information about the image.
      */
     private ImageStub stub;
 
     /**
      * Constructor for instances of Image.
     * @param stub takes the ImageStub and represents it.
      */
     Image (ImageStub stub) {
         this.stub = stub;
     }
 
     /**
      * Returns a new Size object representing the size of the image in the file
      * this was loaded from.
      * @return Size
      */
     public Size getSize() {
         return new Size(stub.getWidth(), stub.getHeight());
     }
 
     /**
      * Returns the width of the image loaded.
      * @return int
      */
     public int getWidth() {
         return stub.getWidth();
     }
 
     /**
      * Returns the height of the image loaded.
      * @return int
      */
     public int getHeight() {
         return stub.getHeight();
     }
 
     /**
     * Gets the path to this Image in storage.
      * @return path to this Images location in storage.
      */
     public String getPath() {
         return stub.getPath();
     }
 
     @Override
     protected void finalize() throws Throwable {
         if (stub != null) {
             stub.dependentCount--;
         }
         super.finalize();
     }
 
     /**
     * Gets the ImageStub that contains necessary objects to draw the image.
      * @return the ImageStub that this Image represents.
      */
     final ImageStub getImageStub() {
         return stub;
     }
 }
 
/**
 * Contains the objects used to implement image support.
 * @author Blinz Project
 */
 final class ImageStub {
 
     int dependentCount = 1;
     private String path;
     private Texture texture;
     private int width, height;
     private BufferedImage bufferedImage;
 
     ImageStub(String path) throws IOException {
         this.path = path;
         load();
     }
 
     /**
      *
      * @return the path to this image.
      */
     final String getPath() {
         return path;
     }
 
     /**
      *
      * @return the width of the image this ImageStub represents.
      */
     final int getWidth() {
         return width;
     }
 
     /**
      *
      * @return the height of the image this ImageStub represents.
      */
     final int getHeight() {
         return height;
     }
 
     /**
      *
      * @return the JOGL Texture representation of the image this ImageStub represents.
      */
     final Texture getTexture() {
         if (texture == null) {
             texture = (Texture) TextureIO.newTexture(bufferedImage, true);
             bufferedImage = null;
         }
         return texture;
     }
 
     /**
      * Loads the image from storage.
      * @throws IOException
      */
     private final void load() throws IOException {
         bufferedImage = ImageIO.read(new File(path));
         width = bufferedImage.getWidth();
         height = bufferedImage.getHeight();
     }
 }
