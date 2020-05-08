 /*
  *   casmi examples
  *   http://casmi.github.com/
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
 
 package casmi.graphics;
 
 import casmi.Applet;
 import casmi.AppletRunner;
 import casmi.KeyEvent;
 import casmi.MouseButton;
 import casmi.MouseEvent;
 import casmi.graphics.color.ColorSet;
 import casmi.graphics.element.Line;
 import casmi.graphics.element.Text;
 import casmi.graphics.element.TextAlign;
 import casmi.graphics.font.Font;
import casmi.graphics.object.RootObject;
 
 /**
  * Example of casmi Font
  * 
  * @author Y.Ban
  * 
  */
 public class TextExample2 extends Applet {
 
     Font f = null;
     Line l;
     Text t, t2, t3;
    RootObject group;
 
     public void setup() {
         String s = "casmi is free software: \n you can redistribute it and/or modify it \n" +
             " under the terms of the GNU Lesser General Public License \n" +
             " as published by the Free Software Foundation, \n" +
             " either version 3 of the License, \n" +
             " or (at your option) any later version.";
         String s2 = "This program is distributed in the hope that it will be useful,\n " +
             "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
             " MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\n " +
             "GNU Lesser General Public License for more details.";
 
         setSize(1024, 768);
         f = new Font("San-Serif");
         f.setSize(15);
         t = new Text("This is test for font rendering", f, 200, 300);
         t.setStrokeColor(ColorSet.LIGHT_BLUE);
 
         t2 = new Text(s, f, 500, 600);
         t2.setAlign(TextAlign.CENTER);
         t2.setStrokeColor(ColorSet.INDIAN_RED);
 
         f.setStyle("ITALIC");
         t3 = new Text(s2, f, 450, 250);
         t3.setAlign(TextAlign.LEFT);
         t3.setStrokeColor(ColorSet.GOLD);
         t3.setLeading(t3.getLeading() + 2);
 
         l = new Line(200, 300 - t.getDescent(), 200 + t.getWidth(), 300 - t.getDescent());
         l.setStrokeColor(ColorSet.LIGHT_BLUE);
         // t.setRotation(30);
         // l.setRotation(30);
        group = new RootObject();
         group.add(t);
         group.add(l);
         group.setRotation(30);
         addObject(t2);
         addObject(t3);
         addObject(group);
     }
 
     @Override
     public void update() {}
 
     @Override
     public void mouseEvent(MouseEvent e, MouseButton b) {}
 
     @Override
     public void keyEvent(KeyEvent e) {}
 
     public static void main(String[] args) {
         AppletRunner.run("casmi.graphics.TextExample2", "TextExample2");
     }
 
 }
