 package casmi.graphics.group;
 
 
 
 
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
 
 
 import casmi.Applet;
 import casmi.AppletRunner;
 import casmi.KeyEvent;
 import casmi.MouseButton;
 import casmi.MouseEvent;
 import casmi.graphics.color.GrayColor;
 import casmi.graphics.element.Line;
 
 /**
  * Color of Group example.
  * 
  * @author Y. Ban
  */
 
 class GroupX extends Group {
 
     Line l1 = new Line(200, 200, 400, 400);
     Line l2 = new Line(200, 400, 400, 200);
     
     public GroupX() {
     	this.add(l1);
     	this.add(l2);
     }
 	@Override
 	public void update() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
 
 public class GroupColorExample extends Applet {
 
 	GroupX  group;
 	double gray;
 	GrayColor color;
 
     @Override
     public void setup() {
         setSize(600, 600);
         
         group = new GroupX();
         gray = 1.0;
         color = new GrayColor(gray);
         group.setStrokeColor(color);
         group.setStrokeWidth(25);
         addObject(group);
     }
 
     @Override
 	public void update() {
     	if(gray<0.0)
     		gray = 1.0;
     	gray-=0.01;
     	color.setGray(gray);
     }
     
 	@Override
 	public void mouseEvent(MouseEvent e, MouseButton b) {}
 
 	@Override
 	public void keyEvent(KeyEvent e) {}
     
     public static void main(String[] args) {
         AppletRunner.run("casmi.graphics.group.GroupColorExample", "Object Example");
     }
 
 }
