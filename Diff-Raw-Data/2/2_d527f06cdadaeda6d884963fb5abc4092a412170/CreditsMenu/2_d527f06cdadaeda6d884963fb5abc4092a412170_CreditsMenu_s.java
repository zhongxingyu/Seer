 package ip.h2n0.main.GFX.menu;
 
 import ip.h2n0.main.GFX.Colours;
 import ip.h2n0.main.GFX.Font;
 import ip.h2n0.main.GFX.Screen;
 
 public class CreditsMenu extends Menu {
 
     private int animTime = 0;
     private int num = 0;
     private int colour = 000;
 
     public CreditsMenu(Menu parent) {
         super();
         this.parent = parent;
         options = new String[] { "C   G", "Elliot Lee-Cerrino", "Jake Bull", "Hayden Lee-Smith" };
     }
 
     @Override
     public void tick() {
         animTime++;
         if (animTime % 360 < 30) {
             colour = 000;
         } else if (animTime % 360 >= 30 && animTime % 360 < 45) {
             colour = 111;
         } else if (animTime % 360 >= 45 && animTime % 360 < 60) {
             colour = 222;
         } else if (animTime % 360 >= 60 && animTime % 360 < 75) {
             colour = 333;
         } else if (animTime % 360 >= 75 && animTime % 360 < 90) {
             colour = 444;
         } else if (animTime % 360 >= 90 && animTime % 360 < 180) {
             colour = 555;
         } else if (animTime % 360 >= 180 && animTime % 360 < 195) {
             colour = 444;
         } else if (animTime % 360 >= 195 && animTime % 360 < 210) {
             colour = 333;
         } else if (animTime % 360 >= 210 && animTime % 360 < 225) {
             colour = 222;
         } else if (animTime % 360 >= 225 && animTime % 360 < 240) {
             colour = 111;
         } else if (animTime % 360 >= 240 && animTime % 360 < 359) {
             colour = 000;
         } else if (animTime % 360 == 359) {
             plusNum();
         }
         if (input.esc.isPressed()) {
             game.setMenu(parent);
         }
     }
 
     public void render(Screen screen) {
         screen.set(0);
         String msg = options[num];
         if (num == 0) {
             Font.renderScale(msg, screen, 123, 95, 4, Colours.get(-1, -1, -1, colour));
         } else if (num == 1) {
             Font.render(msg, screen, 70, 97, Colours.get(-1, -1, -1, colour));
         } else if (num == 2) {
             Font.render(msg, screen, 110, 97, Colours.get(-1, -1, -1, colour));
         } else if (num == 3) {
             Font.render(msg, screen, 77, 97, Colours.get(-1, -1, -1, colour));
         }
     }
 
     private void plusNum() {
        if (num >= 3) {
             num = 0;
             return;
         }
         num++;
     }
 }
