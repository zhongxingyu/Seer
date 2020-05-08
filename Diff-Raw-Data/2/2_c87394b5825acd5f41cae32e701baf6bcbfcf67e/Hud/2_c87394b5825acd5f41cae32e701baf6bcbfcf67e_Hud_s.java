 
 package OperationHotHammer.Display;
 
 import OperationHotHammer.Display.Text.Text;
 import java.awt.Font;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import org.newdawn.slick.Color;
 
 
 public enum Hud {
         INSTANCE;
         
         private LinkedHashMap<String,String> vars = new LinkedHashMap<>();
         private float fontSize = 20f;
         private Text uifont = new Text("OperationHotHammer/Assets/Fonts/DisposableDroidBB.ttf", Font.PLAIN, fontSize, false);
         
         public void set(String name, String value) {
             vars.put(name, value);
         }
         
         public void draw(int screenWidth, int screenHeight) {
             int line = 0;
             int column = 0;
             
             for (Map.Entry<String, String> entry : vars.entrySet()) { 
                 uifont.draw(10+column, 10+fontSize*line++, entry.getKey() + ": " + entry.getValue(), Color.white);
                 if(10+fontSize*line+fontSize > screenHeight-10) {
                    column += 300;
                     line = 0;
                 }
             }
         }
     
 }
