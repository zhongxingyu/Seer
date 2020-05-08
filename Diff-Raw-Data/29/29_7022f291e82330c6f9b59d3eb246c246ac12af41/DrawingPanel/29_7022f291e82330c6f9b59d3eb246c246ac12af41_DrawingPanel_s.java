 /*
  * Copyright 2009-2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
  
 package griffon.plugins.slideware;
 
 import java.awt.*;
 import javax.swing.*;
 import java.awt.image.BufferedImage;
 
 /**
  * A container that supports custom painting of its background.
  *
  * @author Andres Almiray
  */
 public abstract class DrawingPanel extends JPanel {
    private Rectangle previousBounds;
    private Image cachedImage;
 
     protected void paintComponent(Graphics g) {
         if (previousBounds == null) {
             previousBounds = new Rectangle();
             getBounds(previousBounds);
         }
 
         if (cachedImage == null || !equals(previousBounds, getBounds())){
             getBounds(previousBounds);
             cachedImage = createImage();
         }
         g.drawImage(cachedImage, 0, 0, this);
     }
 
     private boolean equals(Rectangle a, Rectangle b) {
         return a.x == b.x &&
                b.y == b.y &&
                a.width == b.width &&
                a.height == b.height;
     }
 
     private Image createImage() {
         Rectangle bounds = getBounds();
 
         BufferedImage image = GraphicsUtil.createCompatibleImage(bounds.width, bounds.height, true);
         Graphics2D g = image.createGraphics();
         paintImage(g, bounds);
         g.dispose();
 
         return image;
     }
 
     protected abstract void paintImage(Graphics2D g, Rectangle bounds);
 }
