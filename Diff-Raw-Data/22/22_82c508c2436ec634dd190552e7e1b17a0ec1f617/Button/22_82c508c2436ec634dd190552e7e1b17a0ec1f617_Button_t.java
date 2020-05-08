 package circlesvssquares;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import org.jbox2d.common.Vec2;
 
 import processing.core.PConstants;
 
 public class Button {
     private static ArrayList<Button> buttonList = new ArrayList<Button>();
     
     public static void removeButtons() {
         buttonList.clear();
     }
     
     public static boolean updateButtons() {
         for (int i = buttonList.size()-1; i >=0; i--) {
             Button b = buttonList.get(i);
             if (b.update()) return true;
         }
         return false;
     }
     
     public static void displayButtons() {
         for (int i = buttonList.size()-1; i >=0; i--) {
             Button b = buttonList.get(i);
             b.display();
         }
     }
     
     private static void uncheckButtons() {
         for (int i = buttonList.size()-1; i >=0; i--) {
             Button b = buttonList.get(i);
             if (b.isCheckbox) b.isDown = false;
         }
     }
     
     public static Button createButton(Vec2 pos_, float w_, float h_, ButtonCallback c_) {
         Button b = new Button(pos_, w_, h_, c_);
         return b;
     }
     
     public static Button createCheckBox(Vec2 pos_, float w_, float h_, ButtonCallback c_) {
         Button b = new Button(pos_, w_, h_, c_);
         b.isCheckbox = true;
         return b;
     }
     Vec2 pos;
     float w, 
           h;
     String text;
     Color fill,
           fillDown,
           stroke;
     private boolean isDown;
     private boolean isCheckbox;
     
     private ButtonCallback callback;
     
     Button(Vec2 pos_, float w_, float h_, ButtonCallback c_) {
         this.pos = pos_;
         
         this.text = "";
         this.w = w_;
         this.h = h_;
         this.fill = new Color(100, 100, 255);
         this.fillDown = Color.CYAN;
         this.stroke = Color.BLACK;
         this.isDown = false;
         this.isCheckbox = false;
         this.callback = c_;
         
         buttonList.add(this);
     }
 
     public void display() {
         CirclesVsSquares cvs = CirclesVsSquares.instance();
         
         if (this.isDown) cvs.fill(this.fillDown.getRGB());
         else cvs.fill(this.fill.getRGB());
         
         cvs.stroke(this.stroke.getRGB());
         cvs.rectMode(PConstants.CORNER);
         cvs.rect(pos.x, pos.y, w, h);
         
         cvs.fill(0);
         cvs.textSize(15);
        cvs.textAlign(PConstants.CENTER);
         cvs.text(this.text, pos.x + this.w / 2, pos.y + this.h / 2);
     }
 
     public boolean update() {
         if (!isCheckbox) this.isDown = false;
         
         boolean wasClicked = false;
         
         CirclesVsSquares cvs = CirclesVsSquares.instance();
         if (cvs.mouseClick) {
             if(cvs.mouseX>pos.x && cvs.mouseX <pos.x+w && 
                     cvs.mouseY>pos.y && cvs.mouseY <pos.y+h) {
                 if (!this.isDown && this.isCheckbox) Button.uncheckButtons();
                 
                 this.isDown = !this.isDown;
                 wasClicked = true;
                 
                 try {
                     callback.isDown = this.isDown;
                     callback.call();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }
         
         return wasClicked;
     }
     
     public boolean isDown() {
         return isDown;
     }
 
     public void destroy() {
         buttonList.remove(this);
     }
 }
