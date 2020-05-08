 package rekkyn.javagame;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class EntityMenu {
     
     public boolean isOpen;
     public List<MenuOption> options;
     public List output = new ArrayList();
     int maxLeft, maxRight, maxHeight;
     public float x1, y1, x2, y2;
     public float[] origin = new float[2];
     
     private Entity e;
     
     public EntityMenu(Entity e) {
         this.e = e;
     }
     
     public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
         final int SCALE = 3;
         origin[0] = e.x + e.width / 2;
         origin[1] = e.y - 15;
         x1 = origin[0] - maxLeft;
         x2 = origin[0] + maxRight;
         y1 = origin[1] + maxHeight;
         y2 = origin[1];
         int direction = 0; // 0 = up, 1 = down
         if (x1 <= 0) {
             origin[0] -= x1;
         } else if (x2 > Game.width) {
             origin[0] -= x2 - Game.width;
         }
         if (y1 < 0) {
             origin[1] = e.y + e.height + 15 - maxHeight;
             direction = 1;
         }
         x1 = origin[0] - maxLeft;
         x2 = origin[0] + maxRight;
         y1 = origin[1] + maxHeight;
         y2 = origin[1];
         
         g.pushTransform();
         g.translate(origin[0], origin[1]);
         Image image = Game.scaleImage(Menu.menu, SCALE);
         
         maxLeft = 27;
         maxRight = 27;
         maxHeight = -42;
         for (int i = 0; i < options.size(); i++) {
             if (options.get(i).getLeftWidth() + 9 > maxLeft) maxLeft = options.get(i).getLeftWidth() + 9;
             if (options.get(i).getRigthWidth() + 9 > maxRight) maxRight = options.get(i).getRigthWidth() + 9;
         }
         if (-20 - (options.size() - 1) * 20 - 10 < maxHeight) maxHeight = -20 - (options.size() - 1) * 20 - 10;
         
         while (maxLeft % SCALE != 0) {
             maxLeft++;
         }
         while (maxRight % SCALE != 0) {
             maxRight++;
         }
         while (maxHeight % SCALE != 0) {
             maxHeight--;
         }
         
         g.drawImage(image,                  //
                 -maxLeft + 7 * SCALE,       //
                 -7 * SCALE,                 //
                 maxRight - 7 * SCALE,       //
                 SCALE,                      //
                 7 * SCALE, 11 * SCALE, 8 * SCALE, 19 * SCALE); // bottom
         
         g.setColor(Colour.altbackground);
         g.fillRect(-maxLeft + 7 * SCALE, maxHeight, maxRight - 8 * SCALE + SCALE - (-maxLeft + 7 * SCALE), 7 * SCALE); // top
         
         g.setColor(Colour.altbackground);
         g.fillRect(-maxLeft, maxHeight + 7 * SCALE, maxLeft + maxRight, -maxHeight - 14 * SCALE);// middle
         
         g.setColor(Colour.altdarker);
         g.fillRect(maxRight, maxHeight + 7 * SCALE, SCALE, -maxHeight - 14 * SCALE);// shadow
         
         g.drawImage(image,                      //
                 -maxLeft,                       //
                 -7 * SCALE,                     //
                 -maxLeft + 7 * SCALE,           //
                 SCALE,                          //
                 0, 11 * SCALE, 7 * SCALE, 19 * SCALE); // bottom left corner
         g.drawImage(image,                      //
                 maxRight - 8 * SCALE + SCALE,   //
                 -7 * SCALE,                     //
                 maxRight + SCALE,               //
                 SCALE,                          //
                 24 * SCALE, 11 * SCALE, 32 * SCALE, 19 * SCALE); // bottom right
                                                                  // corner
         g.drawImage(image,                      //
                 -maxLeft,                       //
                 maxHeight,                      //
                 -maxLeft + 7 * SCALE,           //
                 maxHeight + 7 * SCALE,          //
                 0, 0, 7 * SCALE, 7 * SCALE); // top left corner
         g.drawImage(image,                      //
                 maxRight - 8 * SCALE + SCALE,   //
                 maxHeight,                      //
                 maxRight + SCALE,               //
                 maxHeight + 7 * SCALE,          //
                 24 * SCALE, 0, 32 * SCALE, 7 * SCALE); // top right corner
         
         g.setColor(Colour.altmain);
         for (int i = 0; i < options.size(); i++) {
             options.get(i).render(-20 - i * 20, g);
         }
         
         g.popTransform();
         
         if (direction == 0) {
             g.drawImage(image, e.x + e.width / 2 - 2.5F * SCALE, e.y - 24, e.x + e.width / 2 + 3.5F * SCALE, e.y - 15 + 4 * SCALE, // pointer
                     13 * SCALE, 15 * SCALE, 19 * SCALE, 22 * SCALE);
         } else {
             g.drawImage(image, e.x + e.width / 2 - 2.5F * SCALE, e.y + e.height + 2 * SCALE, e.x + e.width / 2 + 3.5F * SCALE, e.y
                     + e.height + 8 * SCALE, // pointer
                     33 * SCALE, 0, 38 * SCALE, 6 * SCALE);
         }
         
     }
     
     public void update(GameContainer container, StateBasedGame game) {
         for (int i = 0; i < options.size(); i++) {
             options.get(i).update();
         }
         
         output.clear();
         for (int i = 0; i < options.size(); i++) {
             output.add(options.get(i).output());
         }
     }
     
     public class MenuOption {
         
         String title;
         
         void render(int yPos, Graphics g) throws SlickException {
             Font.draw(":", 0, yPos, 2, g);
             Font.draw(title, -Font.getWidth(title, 2), yPos, 2, g);
         }
         
         int getLeftWidth() {
             return Font.getWidth(title, 2);
         }
         
         int getRigthWidth() {
             return 2;
         }
         
         Object output() {
             return title;
         }
         
         void update() {}
     }
     
     public class Radio extends MenuOption implements Clickable {
         
         boolean activated;
         
         public Radio(String title, boolean activated) {
             this.title = title;
             this.activated = activated;
         }
         
         @Override
         void render(int yPos, Graphics g) throws SlickException {
             super.render(yPos, g);
             g.fillRect(10, yPos, 10, 10);
             if (activated) {
                 g.setColor(Colour.main);
                 g.fillRect(12, yPos + 2, 6, 6);
             }
         }
         
         @Override
         int getLeftWidth() {
             return Font.getWidth(title, 2);
         }
         
         @Override
         int getRigthWidth() {
             return 20;
         }
         
         @Override
         Object output() {
             return activated;
         }
         
         @Override
         public void onClick(float mouseX, float mouseY) {
             if (mouseX >= 10 && mouseX <= 20 && mouseY >= 0 && mouseY <= 10) {
                 activated = !activated;
             }
         }
     }
     
     public class Text extends MenuOption implements Clickable {
         
         public boolean editing;
         
         public Text(String title) {
             this.title = title;
         }
         
         @Override
         void render(int yPos, Graphics g) throws SlickException {
             g.setColor(Colour.main);
             if (editing && System.currentTimeMillis() % 1000 < 500) {
                 Font.centerText(">" + title + "<", 0, yPos, 2, g);
             } else {
                 Font.centerText(title, 0, yPos, 2, g);
             }
         }
         
         @Override
         int getLeftWidth() {
             return Font.getWidth(">" + title + "<", 2) / 2;
         }
         
         @Override
         int getRigthWidth() {
             return Font.getWidth(">" + title + "<", 2) / 2 - 3;
         }
         
         @Override
         public void onClick(float mouseX, float mouseY) {
             editing = !editing;
             Font.resetInput(Game.appgc);
         }
         
         @Override
         void update() {
             if (editing) {
                 e.world.takeInput(false);
                 title = Font.editString(title, Game.appgc);
                 if (!isOpen) editing = false;
                if (Game.appgc.getInput().isKeyPressed(Input.KEY_ENTER)) editing = false;
             } else {
                 e.world.takeInput(true);
             }
         }
     }
     
     public interface Clickable {
         public void onClick(float mouseX, float mouseY);
     }
     
     public void setOptions(List<MenuOption> options) {
         this.options = options;
     }
     
     public List getOutput() {
         return output;
     }
     
     public void clicked() {
         float mouseX = Mouse.getX() - origin[0];
         float mouseY = Game.height - Mouse.getY() - origin[1];
         for (int i = 0; i < options.size(); i++) {
             if (mouseX >= -options.get(i).getLeftWidth() && mouseX <= options.get(i).getRigthWidth() && mouseY >= -20 - i * 20
                     && mouseY <= -10 - i * 20 && options.get(i) instanceof Clickable) {
                 ((Clickable) options.get(i)).onClick(mouseX, mouseY - i * 20 + 20);
             }
         }
     }
     
 }
