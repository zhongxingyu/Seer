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
  *  (C) Liam Byrne, 2008.
  */
 
 package sprites;
 
 import gui.GameMap;
 import images.ImageHelper;
 
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class Pacman extends AbstractSprite {
 
    private static final int width = GameMap.PATH_WIDTH * 2 - 10;
    
    private static final String imageName = "pacman_yellow";
    private static final String extension = ".png";
   private static final int numImages = 5;
    private static final List<BufferedImage> images = Collections.unmodifiableList(makeImages());
    
    
    public Pacman(int hp, List<Point> path) {
       super(images, hp, path);
    }
    
    private static ArrayList<BufferedImage> makeImages() {
       ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
       for(int i = 1; i <= numImages; i++) {
          // Adds all the images
          images.add(ImageHelper.makeImage(width, width, "sprites", imageName + i + extension));
       }
       for(int i = images.size() - 2; i > 0; i--) {
          // Adds the images from the second to last to the second again to make a cycle
          images.add(images.get(i));
       }
       return images;
    }
 
 }
