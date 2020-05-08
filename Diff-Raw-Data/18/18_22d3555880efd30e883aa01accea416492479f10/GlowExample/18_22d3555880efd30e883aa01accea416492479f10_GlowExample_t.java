 /*
  *   casmi examples
  *   http://casmi.github.io/
  *   Copyright (C) 2011, Xcoo, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package casmi.graphics.shader;
 
 import casmi.Applet;
 import casmi.AppletRunner;
 import casmi.KeyEvent;
 import casmi.MouseButton;
 import casmi.MouseEvent;
 import casmi.graphics.color.ColorSet;
 import casmi.graphics.element.Circle;
 import casmi.graphics.element.Element;
 import casmi.graphics.element.MouseClickCallback;
 import casmi.graphics.element.Rect;
 import casmi.graphics.element.Text;
 import casmi.graphics.element.Texture;
 
 /**
  * GlowShading example.
  *
  * @author Y. Ban
  *
  * @see casmi.Applet#enableBlurShader()
  * @see casmi.Applet#disableBlurShader()
  * @see casmi.graphics.element.Element#enableBlur()
  * @see casmi.graphics.element.Element#setBlurMode(BlurMode)
  */
 public class GlowExample extends Applet {
 
     static final String IMAGE_PATH = Applet.class.getResource("logo.png").getPath();
 
     Rect r1, r2, r3;
     Circle c;
     Text t;
     Texture tex;
 
     double radian = 0.0;
     double rot = 0.0;
 
     @Override
     public void setup() {
         setSize(800, 600);
 
         r1 = new Rect(100, 100);
         r1.setPosition(100, 100);
         r1.setFillColor(ColorSet.DARK_GREEN);
 
         r2 = new Rect(100, 100);
         r2.setPosition(150, 150);
         r2.setFillColor(ColorSet.ORANGE);
         r2.setStroke(false);
         r2.enableBlur();
        r2.setBlurMode(BlurMode.GLOW);
 
         r3 = new Rect(100, 100);
         r3.setPosition(200, 200, 100);
         r3.setFillColor(ColorSet.BLUE);
         r3.setStroke(false);
         r3.enableBlur();
 
         t = new Text("c");
         t.getFont().setSize(50);
         t.setPosition(70, 70);
         t.setStrokeColor(ColorSet.LIGHT_BLUE);
         t.enableBlur();
        t.setBlurMode(BlurMode.GLOW);
 
         c = new Circle(30);
         c.setPosition(500 + 100 * Math.cos((radian / 180.0) * Math.PI),
             300 + 100 * Math.sin((radian / 180.0) * Math.PI));
         c.setFillColor(ColorSet.RED);
         c.setStrokeColor(ColorSet.RED);
         c.enableBlur();
        c.setBlurMode(BlurMode.MOTION_BLUR);
 
         MouseClickCallback mouseclick = new MouseClickCallback() {
 
             @Override
             public void run(MouseClickTypes eventtype, Element element) {
                 if (eventtype == MouseClickTypes.CLICKED) {
                     BlurMode blur = element.getBlurMode();
                     if (element.isBlur()) {
                        if (blur == BlurMode.BLUR)
                            element.setBlurMode(BlurMode.GLOW);
                        else if (blur == BlurMode.GLOW)
                             element.disableBlur();
                     } else {
                         element.enableBlur();
                        element.setBlurMode(BlurMode.BLUR);
                     }
                 }
             }
         };
         r3.addMouseEventCallback(mouseclick);
 
         tex = new Texture(IMAGE_PATH);
         tex.setPosition(500, 100);
         tex.setWidth(tex.getWidth() / 1.2);
         tex.setRotation(rot, 0.0, 1.0, 0.0);
         tex.enableBlur();
         tex.addMouseEventCallback(mouseclick);
 
         enableBlurShader();
 
         addObject(r1);
         addObject(t);
         addObject(r3);
         addObject(r2);
         addObject(c);
         addObject(tex);
     }
 
     @Override
     public void update() {
         rot += 2.0;
         tex.setRotation(rot, 0.0, 1.0, 0.0);
         radian += 2;
         if (radian > 360)
             radian = 0;
         c.setPosition(500 + 100 * Math.cos((radian / 180.0) * Math.PI),
             300 + 100 * Math.sin((radian / 180.0) * Math.PI));
     }
 
     @Override
     public void mouseEvent(MouseEvent e, MouseButton b) {}
 
     @Override
     public void keyEvent(KeyEvent e) {
         if (!isBlur())
             enableBlurShader();
         else
             disableBlurShader();
     }
 
     public static void main(String[] args) {
         AppletRunner.run("casmi.graphics.shader.GlowExample", "Glow Example");
     }
 }
