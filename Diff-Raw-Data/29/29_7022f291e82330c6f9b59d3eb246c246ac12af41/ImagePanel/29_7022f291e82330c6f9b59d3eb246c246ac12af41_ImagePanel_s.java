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
 
 import javax.imageio.ImageIO;
 import java.awt.*;
 import java.io.IOException;
 
 /**
  * A container that draws an image.
  *
  * @author Andres Almiray
  */
 public class ImagePanel extends BackgroundPanel {
     private static final Color TRANSPARENT = new Color(255, 0, 0, 255);
     private float scale = 1.0f;
     private Image image;
     private boolean centerImage = true;
 
     public ImagePanel() {
         setBackground(TRANSPARENT);
     }
 
     public float getScale() {
         return scale;
     }
 
     public void setScale(float scale) {
         this.scale = scale;
         repaint();
     }
 
     public Image getImage() {
         return image;
     }
 
     public void setImage(Image image) {
         this.image = image;
         repaint();
     }
 
     public boolean isCenterImage() {
         return centerImage;
     }
 
     public void setCenterImage(boolean centerImage) {
         this.centerImage = centerImage;
         repaint();
     }
 
     public void setImagePath(String imagePath) {
         try {
             image = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource(imagePath));
             repaint();
         } catch (IOException e) {
             // ignore ??
         }
     }
 
     @Override
     public Dimension getPreferredSize() {
         Image image = getImage();
         if (image == null)
             return new Dimension(0, 0);
         else {
             float width = image.getWidth(null) * getScale();
             float height = image.getHeight(null) * getScale();
             return new Dimension((int) width, (int) height);
         }
     }
 
     protected void paintImage(Graphics2D g, Rectangle bounds) {
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         Image image = getImage();
         if (image != null) {
             Dimension pref = getPreferredSize();
             float width = pref.width;
             float height = pref.height;
             if ((width > bounds.width) || (height > bounds.height)) {
                 float f = Math.min(bounds.width / width, bounds.height / height);
                 width *= f;
                 height *= f;
             }
             int x = 0;
             int y = 0;
             if (centerImage) {
                 x = (int) ((bounds.width - width) / 2);
                 y = (int) ((bounds.height - height) / 2);
             }
             g.drawImage(image, x, y, (int) width, (int) height, null);
         }
     }
 }
