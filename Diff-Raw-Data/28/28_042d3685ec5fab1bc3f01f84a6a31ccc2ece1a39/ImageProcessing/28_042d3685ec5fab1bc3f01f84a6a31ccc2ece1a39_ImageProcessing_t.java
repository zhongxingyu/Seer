 /*
  * cilla - Blog Management System
  *
  * Copyright (C) 2012 Richard "Shred" Körber
  *   http://cilla.shredzone.org
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.shredzone.cilla.ws;
 
 import java.io.Serializable;
 
 /**
  * Contains parameters for image processing.
  *
  * @author Richard "Shred" Körber
  */
 public class ImageProcessing implements Serializable {
     private static final long serialVersionUID = 4111433007737582101L;
 
     private int width;
     private int height;
     private ImageType type;
 
    public ImageProcessing() {
        // Default constructor
    }

    public ImageProcessing(int width, int height, ImageType type) {
        this.width = width;
        this.height = height;
        this.type = type;
    }

     /**
      * Maximum image width. The image's aspect ratio is kept. 0 means original size.
      */
     public int getWidth()                       { return width; }
     public void setWidth(int width)             { this.width = width; }
 
     /**
      * Maximum image height. The image's aspect ratio is kept. 0 means original size.
      */
     public int getHeight()                      { return height; }
     public void setHeight(int height)           { this.height = height; }
 
     /**
      * Type of the processed image.
      */
     public ImageType getType()                  { return type; }
     public void setType(ImageType type)         { this.type = type; }
 
     /**
      * Sets the maximum image width and height for scaling. The image's aspect ratio is
      * kept.
      *
      * @param width
      *            Image width
      * @param height
      *            Image height
      */
     public void setDimension(int width, int height) {
         this.width = width;
         this.height = height;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append(width).append('x').append(height);
         if (type != null) {
             sb.append('-').append(type.name());
         }
         return sb.toString();
     }
 
 }
