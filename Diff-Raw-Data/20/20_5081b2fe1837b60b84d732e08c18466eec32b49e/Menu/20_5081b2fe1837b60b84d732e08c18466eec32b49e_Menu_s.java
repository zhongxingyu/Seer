 package core.gamestate;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.HashMap;
 import java.util.Map;
 
 import core.keyconfiguration.KeyAnnotation;
 import com.golden.gamedev.object.Background;
 import com.golden.gamedev.object.background.ImageBackground;
 
 import demo.GameEngine2D;
 /**
  * 
  * @author Hui Dong
  *
  */
 public class Menu extends MenuGameObject{
     private int optionID = 0;
     GameEngine2D engine;
     private Background background;
     private BufferedImage arrow;
     private static Map<Integer, Integer> map = new HashMap<Integer, Integer>();
 
     static{
         map.put(0, GameEngine2D.GAMEAI);
         map.put(1, GameEngine2D.GAME_PLAYFIELD);
         map.put(2, GameEngine2D.GAME_HUD);

        
     }
 
     public Menu(GameEngine2D engine) {
         super(engine);
         this.engine = engine;
     }
 
     @Override
     public void initResources() {
         super.initResources();
         background = new ImageBackground(getImage("resources/StarDust.jpg"), 640, 480);
         arrow = getImage("resources/MenuArrow.png");
     }
 
     @Override
     public void render(Graphics2D graphic) {
         background.render(graphic);
         graphic.setColor( Color.WHITE );
         graphic.drawString("DemoAI", 320, 240);
         graphic.drawString("DemoPlayField", 320, 260);     
         graphic.drawString("DemoHUD", 320, 280);   
         graphic.drawString("EXIT", 320, 300);
         graphic.drawImage(arrow, 300, 230 + optionID*20, null);
     }
 
     @Override
     public void update(long arg0) {
         super.update(arg0);
     }
 
     @KeyAnnotation(action = "down")
     public void  down(){
        if(optionID < 2){
             optionID++;
         }
     }
     
     @KeyAnnotation(action = "up")
     public void up(){
         if(optionID > 0){
             optionID--;
         }
     }
     
     @KeyAnnotation(action = "enter")
     public void nextGameObject(){
         if(optionID == 3){
             finish();
             return;
         }
         engine.nextGameID = map.get(optionID);
         engine.setPreviousGameID(map.get(optionID));
         finish();
     }
     
 
 }
