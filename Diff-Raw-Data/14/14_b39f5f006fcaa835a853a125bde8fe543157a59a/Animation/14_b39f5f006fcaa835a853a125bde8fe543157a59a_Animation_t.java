 /*
  *  BlinzCore - core library of audio, video, and other essential classes.
 *  Copyright (C) 2009-2010 BlinzProject <gtalent2@gmail.com>
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
 package org.blinz.graphics;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * A container class for Images that automatically flips after a set interval
  * of time.
  *
  * @author Blinz Project
  */
 public class Animation {
 
     private long milliSecondsPerImage = 10;
     private long lastUpdateTime = 0;
     private int slide = 0;
     private final LinkedList<Image> toAdd = new LinkedList<Image>();
     private final LinkedList<Image> toRemove = new LinkedList<Image>();
     private final ArrayList<Image> images = new ArrayList<Image>();
    
    /**
     * Constructor
     */
     public Animation() {
     }
 
     /**
      * Adds the image associated with the path provided.
      * @param imagePath
      * @throws FileNotFoundException
      */
     public final void addImage(String imagePath) throws IOException {
         Image image = ImageLoader.loadImage(imagePath);
         toAdd.add(image);
     }
 
     /**
      * Adds the given Image object to this Animation.
      * @param image
      * @throws FileNotFoundException
      */
     public final void addImage(Image image) {
         toAdd.add(image);
     }
 
     /**
      * Removes the image associated with the path provided.
      * @param imagePath
      */
     public final void removeImage(String imagePath) {
         for (int i = 0; i < images.size(); i++) {
             if (images.get(i).getImageStub().getPath().equals(imagePath)) {
                 images.remove(i);
             }
         }
     }
 
     /**
      * Removes the specified Image object from this Animation.
      * @param image
      */
     public final void removeImage(Image image) {
         toRemove.add(image);
     }
 
     /**
      * Sets the number milli-seconds that will pass before the animation flips
      * to the next image.
      * Note: This is based on System time.
      * @param number
      */
     public final void milliSecondsPerImage(int number) {
         this.milliSecondsPerImage = number;
     }
 
     /**
      * Returns the current Image in this Animation.
      * Note: Returns null if this Animation is empty.
     * @return the current Image in this Animation
      */
     final Image getImage() {
         manageImages();
         if (images.isEmpty()) {
             return null;
         }
         return images.get(getSlide());
     }
 
     /**
      * Adds and removes Images according to what is in the toAdd and toRemove lists
      * respectively.
      */
     final void manageImages() {
         while (!toAdd.isEmpty()) {
             images.add(toAdd.get(0));
             toAdd.remove(0);
         }
 
         while (!toRemove.isEmpty()) {
             if (!images.remove(toRemove.get(0))) {
                 toAdd.remove(toRemove.get(0));
             }
             toRemove.remove(0);
         }
     }
 
     @Override
     protected void finalize() {
         try {
             super.finalize();
         } catch (Throwable ex) {
             Logger.getLogger(Animation.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Returns the current slide index, updating it if necessary.
     * @return the current slide index.
      */
     private final synchronized int getSlide() {
         if (System.currentTimeMillis() - lastUpdateTime >= milliSecondsPerImage) {
             final int slides = images.size();
             slide += (System.currentTimeMillis() - lastUpdateTime) / milliSecondsPerImage;
             slide -= (slide / slides) * slides;
             lastUpdateTime = System.currentTimeMillis();
         }
         return slide;
     }
 }
