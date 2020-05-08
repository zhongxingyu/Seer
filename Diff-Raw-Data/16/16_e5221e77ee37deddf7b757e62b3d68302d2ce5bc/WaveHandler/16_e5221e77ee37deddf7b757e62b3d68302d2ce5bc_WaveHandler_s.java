 import javax.swing.*;
 import java.awt.*;
 
 public class WaveHandler {
     private int currentWave;
     private int messageX;
     private int messageY;
     private int messageFade;
 
     public WaveHandler(){
         //testing purposes
         currentWave = 0;
     }
     public void startWave(){
         currentWave++; //starts at 1
         messageX = 210;
         messageY = Window.getHeight()/2;
         messageFade = 255;
 
         switch(currentWave){
             case(1): //6 enemy1
                 Game.enemies.addEnemy(Window.getWidth()/2, -500, new Enemy1());
                 Game.enemies.addEnemy(-1, -1000, new Enemy1());
                 Game.enemies.addEnemy(-1, -1500, new Enemy1());
                 Game.enemies.addEnemy(-1, -2000, new Enemy1());
                 Game.enemies.addEnemy(-1, -2500, new Enemy1());
                 Game.enemies.addEnemy(-1, -3000, new Enemy1());
             break;
             case(2): //8 enemy2
                 Game.enemies.addEnemy(100,-500, new Enemy2());
                 Game.enemies.addEnemy(380,-750, new Enemy2());
                 Game.enemies.addEnemy(100,-1000, new Enemy2());
                 Game.enemies.addEnemy(380,-1250, new Enemy2());
                 Game.enemies.addEnemy(380,-1500, new Enemy2());
                 Game.enemies.addEnemy(380,-1750, new Enemy2());
             break;
             case(3):
 
             break;
             case(4):
 
             break;
             case(5):
 
             break;
             case(6):
 
             break;
             case(7):
 
             break;
             case(8):
 
             break;
             case(9):
 
             break;
             case(10):
 
             break;
         }
     }
     public void endWave(){
         Window.panel.setPanelState(0);
         Window.panel.editor.setMoney(Game.player.getMoney());
         Game.player.resetFireBullet();
         Game.bullets.removeAll();
     }
     public void draw(Graphics2D g){
         if (messageFade > 0){
            System.out.println(messageFade);
             g.setColor(new Color(255,255,255,messageFade));
             g.drawString("Level "+currentWave,messageX,messageY);
         }
     }
     public void update(){
         if (messageFade-2 > 0) messageFade-=2;
         else messageFade = 0;
         messageY--;
     }
 }
