 /**
  * Copyright 2011 The PlayN Authors
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package memdial.core;
 
 import playn.core.*;
 import playn.core.util.Callback;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static playn.core.PlayN.*;
 
 public class Dial {
     public static final String IMAGE = "images/dial-retro-cut.png";
     private ImageLayer imageLayer;
     private ImageLayer textLayer;
     private float angle;
    public static final double MAX_ANGLE = 2 * Math.PI;
     private boolean clockwise = false;
 
     private static List<Point> coords = new ArrayList<Point>();
     static {
         coords.add(new Point(412, 451));
         coords.add(new Point(492, 307));
         coords.add(new Point(468, 187));
         coords.add(new Point(379, 101));
         coords.add(new Point(260, 82));
         coords.add(new Point(149, 140));
         coords.add(new Point(93, 244));
         coords.add(new Point(99, 366));
         coords.add(new Point(172, 451));
         coords.add(new Point(290, 492));
     }
 
     public boolean isClockwise() {
         return clockwise;
     }
 
     public void setClockwise(boolean clockwise) {
         this.clockwise = clockwise;
     }
 
     private static class Point {
         Point(int x, int y) {
             this.x = x;
             this.y = y;
         }
         int x;
         int y;
     }
 
     public Dial(final GroupLayer parentLayer, final float x, final float y) {
         Image image = assets().getImage(IMAGE);
         imageLayer = graphics().createImageLayer(image);
 
         // Add a callback for when the image loads.
         // This is necessary because we can't use the width/height (to center the
         // image) until after the image has been loaded
         image.addCallback(new Callback<Image>() {
             @Override
             public void onSuccess(Image result) {
                 imageLayer.setOrigin(result.width() / 2f, result.height() / 2f).setTranslation(x, y).setDepth(0);
                 parentLayer.add(imageLayer);
             }
 
             @Override
             public void onFailure(Throwable cause) {
                 log().error("Error loading image!", cause);
             }
         });
 
         textLayer = createLayerWithText("Dial\n666");
         textLayer.setOrigin(textLayer.width() / 2, textLayer.height() / 2).setTranslation(x, y).setDepth(1);
         parentLayer.add(textLayer);
 
         for (int ixNum = 0; ixNum < 10; ixNum++) {
             ImageLayer numLayer = createLayerWithText(Integer.toString(ixNum));
             numLayer.setOrigin(Memdial.SCREEN_WIDTH - coords.get(ixNum).x, Memdial.SCREEN_HEIGHT - coords.get(ixNum).y).
                     setTranslation(Memdial.SCREEN_WIDTH + 100, Memdial.SCREEN_HEIGHT - 12).
                     setDepth(-1);
             parentLayer.add(numLayer);
         }
     }
 
     public void update(int delta) {
         log().info("update(delta=" + delta + ")");
         log().info("    angle=" + angle);
        if (Math.abs(angle) < Math.abs(MAX_ANGLE)) {
             if (isClockwise()) {
                 double correctionFactor = 1;
                 angle += correctionFactor * 2 * Math.PI * 0.2 / delta;
             } else {
                double correctionFactor = 1 - Math.abs(angle) / Math.abs(MAX_ANGLE);
                 angle -= correctionFactor * 2 * Math.PI * 0.2 / delta;
             }
         }
         imageLayer.setRotation(angle);
         textLayer.setRotation(angle);
     }
 
     private ImageLayer createLayerWithText(String text) {
         Font font = graphics().createFont("king668", Font.Style.PLAIN, 48f);
         TextFormat format = new TextFormat().withFont(font);
         TextLayout layout = graphics().layoutText(text, format);
         return createTextLayer(layout, 0xFF000000);
     }
 
     protected ImageLayer createTextLayer(TextLayout layout, int color) {
         CanvasImage image = graphics().createImage((int)Math.ceil(layout.width()),
                 (int)Math.ceil(layout.height()));
         image.canvas().setFillColor(color);
         image.canvas().fillText(layout, 0, 0);
         return graphics().createImageLayer(image);
     }
 }
