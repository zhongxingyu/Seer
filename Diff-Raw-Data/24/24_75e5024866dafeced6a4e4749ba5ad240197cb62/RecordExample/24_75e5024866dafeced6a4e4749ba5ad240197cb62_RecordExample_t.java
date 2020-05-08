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
 package casmi;
 
import casmi.extension.video.Recorder;
 import casmi.graphics.color.ColorSet;
 import casmi.graphics.color.RGBColor;
 import casmi.graphics.element.Circle;
 import casmi.graphics.element.Element;
 import casmi.graphics.element.MouseClickCallback;
 import casmi.graphics.element.MouseOverCallback;
 import casmi.tween.Tween;
 import casmi.tween.TweenElement;
 import casmi.tween.TweenManager;
 import casmi.tween.TweenType;
 import casmi.tween.equations.Sine;
 import casmi.util.SystemUtil;
 
 /**
  * Recording example.
  * <p>
  * Records a window as a MPEG4 movie. 
  * 
  * @author T. Takeuchi
  */
 public class RecordExample extends Applet {
 
     static final String RECORD_FILE = SystemUtil.JAVA_TMP_PATH + "casmi_record.mp4";
     
    Recorder recorder;
    
     Circle circle       = new Circle(320, 240, 15);
     Circle recordCircle = new Circle(610, 450, 15);
     
     TweenManager manager = new TweenManager();
     TweenElement te = new TweenElement(circle);
     
     boolean isRecording = false;
     
     @Override
     public void setup() {
        recorder = new Recorder(this);
        
         setSize(640, 480);
         
         circle.setCenterColor(ColorSet.YELLOW);
         circle.setEdgeColor(new RGBColor(0.0, 0.0));
         addObject(circle);
               
        recordCircle.setFillColor(new RGBColor(ColorSet.RED, 0.5));
         
         recordCircle.addMouseEventCallback(new MouseOverCallback() {
             @Override
             public void run(MouseOverTypes eventtype, Element element) {
                 switch (eventtype) {
                 case ENTERED:
                 case EXISTED:
                     recordCircle.setStroke(true);
                     recordCircle.setStrokeColor(ColorSet.WHITE);
                     break;
                 case EXITED:
                     recordCircle.setStroke(false);
                     break;
                 }
             }
         });
        
         recordCircle.addMouseEventCallback(new MouseClickCallback() {
             @Override
             public void run(MouseClickTypes eventtype, Element element) {
                 if (eventtype == MouseClickTypes.CLICKED) {
                     if (!isRecording) {
                        recorder.record(RECORD_FILE);
                         isRecording = true;
                         recordCircle.setFillColor(new RGBColor(ColorSet.RED));
                         System.out.println("start recording");
                     } else {
                        recorder.stopRecord();
                         isRecording = false;
                         recordCircle.setFillColor(new RGBColor(ColorSet.RED, 0.5));
                         System.out.println("stop recording");
                         System.out.println("file: " + RECORD_FILE);
                     }
                 }
             }
         });
        
         addObject(recordCircle);
     }
 
     @Override
     public void update() {}
 
     @Override
     public void mouseEvent(MouseEvent e, MouseButton b) {
         if (e == MouseEvent.MOVED) {
         	addTween(Tween.to(te, TweenType.POSITION, 500, Sine.OUT).target(getMouseX(), getMouseY()));
         }
     }
     
     @Override
     public void keyEvent(KeyEvent e) {}
 
     public static void main(String[] args) {
         AppletRunner.run("casmi.RecordExample", "Screen Recording Example");
     }
     
 }
