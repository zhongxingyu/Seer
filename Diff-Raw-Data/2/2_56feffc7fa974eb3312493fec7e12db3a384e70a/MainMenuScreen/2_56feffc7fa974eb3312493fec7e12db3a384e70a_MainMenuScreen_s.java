 package Screens;
 
 import helpers.Delegate;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 
 import java.awt.*;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import static org.lwjgl.opengl.GL11.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hp
  * Date: 23/04/13
  * Time: 2:43 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MainMenuScreen extends MenuScreen {
 
     TrueTypeFont titleFont;
     Font fnt;
     private int selectedIndex;
     private int lastKeyPressed;
     Texture background;
 
     public MainMenuScreen(Delegate d) {
         super(d);
         fnt = new Font("Courier New", Font.PLAIN, 40);
         titleFont = new TrueTypeFont(fnt, true);
         background = loadTexture();
     }
 
     public  void Initialize(){
         this.MenuOptions = new ArrayList<String>();
         this.MenuOptions.add("Settings");
         this.MenuOptions.add("Single Player");
         this.MenuOptions.add("Multi Player");
 
     }
     public Texture loadTexture()
     {
         try {
             //Todo: Hard coded file Path. Change later
            return TextureLoader.getTexture("jpg", new FileInputStream(new File("C:/Users/hp/Desktop/test.jpg")));
         } catch (FileNotFoundException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return null;
     }
 
     public void Render(){
         glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
         glColor3f(1.0f, 1.0f, 1.0f);
 
         background.bind();
 
         glBegin(GL_QUADS);
 
         glTexCoord2f(0,0);glVertex2f(0, 0);
         glTexCoord2f(0.9f,0);glVertex2f(800, 0);
         glTexCoord2f(0.9f,0.5f);glVertex2f(800,600);
         glTexCoord2f(0,0.5f);glVertex2f(0,600);
 
         glEnd();
         GL11.glEnable(GL11.GL_BLEND);
         Color current;
         titleFont.drawString(99, 79, "Main Menu", Color.orange);
         titleFont.drawString(100, 80, "Main Menu", Color.white);
         for(int i = 0; i < this.MenuOptions.size(); i++)
         {
             if(selectedIndex == i)
                 current = Color.yellow;
             else
                 current = Color.gray;
 
             super.font.drawString(150f, 150f +(i*50), MenuOptions.get(i), current);
         }
         GL11.glDisable(GL11.GL_BLEND);
     }
 
     public void Update(){
         if(Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
             if(lastKeyPressed != Keyboard.KEY_RETURN)
                 delegate.change(selectedIndex +1);
             lastKeyPressed = Keyboard.KEY_RETURN;
         }
         else if(Keyboard.isKeyDown(Keyboard.KEY_UP)) {
             if(lastKeyPressed != Keyboard.KEY_UP)
                 selectedIndex = ((selectedIndex + 3)-1)%3;
             lastKeyPressed = Keyboard.KEY_UP;
         }else if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
             if(lastKeyPressed != Keyboard.KEY_DOWN)
                 selectedIndex = (selectedIndex+1)%3;
             lastKeyPressed = Keyboard.KEY_DOWN;
         } else {
             lastKeyPressed = -1;
         }
     }
 
 }
