 package com.project4.FilterPlayGround;
 
 import com.anotherbrick.inthewall.Config.MyColorEnum;
 import com.anotherbrick.inthewall.TouchEnabled;
 import com.anotherbrick.inthewall.VizButton;
 import com.anotherbrick.inthewall.VizPanel;
 
 public class Keyboard extends VizPanel implements TouchEnabled {
 
   public Keyboard(float x0, float y0, float width, float height, VizPanel parent) {
     super(x0, y0, width, height, parent);
   }
 
   String[] row1 = {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p"};
   String[] row2 = {"a", "s", "d", "f", "g", "h", "j", "k", "l"};
   String[] row3 = {"z", "x", "c", "v", "b", "n", "m"};
   String[] row4 = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
   VizButton[] buttons = new VizButton[38];
 
   public void setup() {
     int count = 0;
     for (String val : row1) {
       buttons[count] = new VizButton(27 + count * (20), 5, 15, 15, this);
       buttons[count].setText(val);
       buttons[count].name = "keyboard|" + val;
       buttons[count].setStyle(MyColorEnum.DARK_GRAY, MyColorEnum.WHITE, MyColorEnum.DARK_GRAY, 255,
           255, 11);
       addTouchSubscriber(buttons[count]);
       count++;
     }
     int count1 = 0;
     for (String val : row2) {
       buttons[count] = new VizButton(37 + count1 * (20), 25, 15, 15, this);
       buttons[count].setText(val);
       buttons[count].name = "keyboard|" + val;
       buttons[count].setStyle(MyColorEnum.DARK_GRAY, MyColorEnum.WHITE, MyColorEnum.DARK_GRAY, 255,
           255, 11);
       addTouchSubscriber(buttons[count]);
       count++;
       count1++;
     }
     count1 = 0;
     for (String val : row3) {
       buttons[count] = new VizButton(57 + count1 * (20), 45, 15, 15, this);
       buttons[count].setText(val);
       buttons[count].name = "keyboard|" + val;
       buttons[count].setStyle(MyColorEnum.DARK_GRAY, MyColorEnum.WHITE, MyColorEnum.DARK_GRAY, 255,
           255, 11);
       addTouchSubscriber(buttons[count]);
       count++;
       count1++;
     }
     buttons[count] = new VizButton(27, 45, 25, 15, this);
     buttons[count].setText("_");
     buttons[count].name = "keyboard|" + "_";
     buttons[count].setStyle(MyColorEnum.DARK_GRAY, MyColorEnum.WHITE, MyColorEnum.DARK_GRAY, 255,
         255, 11);
    addTouchSubscriber(buttons[count]);
     count++;
     buttons[count] = new VizButton(57 + count1 * (20), 45, 25, 15, this);
     buttons[count].setText("DEL");
     buttons[count].name = "keyboard|" + "DEL";
     buttons[count].setStyle(MyColorEnum.DARK_GRAY, MyColorEnum.WHITE, MyColorEnum.DARK_GRAY, 255,
         255, 11);
     addTouchSubscriber(buttons[count]);
     count1 = 0;
     count++;
     for (String val : row4) {
       buttons[count] = new VizButton(27 + count1 * (20), 65, 15, 15, this);
       buttons[count].setText(val);
       buttons[count].name = "keyboard|" + val;
       buttons[count].setStyle(MyColorEnum.DARK_GRAY, MyColorEnum.WHITE, MyColorEnum.DARK_GRAY, 255,
           255, 11);
       addTouchSubscriber(buttons[count]);
       count++;
       count1++;
     }
   }
 
   @Override
   public boolean draw() {
     if (!isVisible()) return false;
     noStroke();
     background(MyColorEnum.MEDIUM_GRAY);
     for (VizButton but : buttons) {
       but.draw();
       but.drawTextCentered();
     }
     return false;
   }
 
   @Override
   public boolean touch(float x, float y, boolean down, TouchTypeEnum touchType) {
     propagateTouch(x, y, down, touchType);
     return false;
   }
 }
