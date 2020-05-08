 
 
 package xandrew.game;
 
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.media.opengl.GL;
 import javax.swing.JOptionPane;
 import scene.GLRenderable;
 import scene.Node;
 import scene.RenderableNode;
 import scene.shapes.GLSquare;
 import xandrew.game.light.LightBeam;
 
 /**
  *
  * @author Andrew
  */
 public class GameLevel extends Node
 {
 
     /**
      * levels/levelX_data.png
      *      This is the data file - defines the bounds
      * levels/levelX_bg.png
      *      This is the display what is shown on the screen
      *
      */
      private BufferedImage levelData;//, levelDisplay;
 
      /** The level id */
      private int levelID;
 
      /** The player's position */
      private Node player;
 
 
     /**
      * Creates a new level with an id
      * @param levelID
      */
     public GameLevel(int levelID)
     {
         super("Level" + levelID);
 
         this.levelID = levelID;
 
         player = new Node("Player");
         addChild(player);
 
         
 
     }
 
 
     /**
      * Interact with the level at the current position
      */
     public void interact()
     {
         /*
 
         float pointX = 200;
         float pointY = 412;
 
         if(getCurrentPlayerX() > pointX - 20 && getCurrentPlayerX() < pointX + 20)
         {
             if(getCurrentPlayerY() > pointY - 20 && getCurrentPlayerY() < pointY + 20)
             {
             }
         }
          */
     }
 
 
 
     public Node getPlayer()
     {
         return player;
     }
 
   
 
     @Override
     public void init(GL gl)
     {
 
         //load light beams
 
        RenderableNode lightNode = new RenderableNode("Light beam", new LightBeam(0, 0));
        addChild(lightNode);
         
 
         //load images
         try
         {
             //load image
             BufferedImage tmp = ImageIO.read(new File("images/level" + levelID + "_data.png"));
             //levelDisplay = ImageIO.read(new File("images/level" + levelID + "_bg.png"));
 
             //have to flip image for data process
             levelData = new BufferedImage(tmp.getWidth(), tmp.getHeight(),tmp.getType());
 
             for(int h=0; h<tmp.getHeight(); h++)
             {
                 for(int w=0; w<tmp.getWidth(); w++)
                 {
                     levelData.setRGB(w, tmp.getHeight() - h - 1, tmp.getRGB(w, h));
                 }
             }
         }
         catch (IOException ex)
         {
             Logger.getLogger(GameLevel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
 
         //create a visual display
         GLSquare square = new GLSquare();
         square.setFileName("images/level" + levelID + "_bg.png");
         RenderableNode rn = new RenderableNode("LevelDisplay", square);
         rn.setScale(levelData.getWidth(), levelData.getHeight(), 1);
 
 
         //create player visual
         RenderableNode playerPic = new RenderableNode("Player Pic");
         GLSquare pp = new GLSquare();
         pp.setFileName("images/player.png");
         playerPic.setRenderTarget(pp);
         playerPic.setScale(5.0f);
 
         player.addChild(playerPic);
 
         moveToPostion(50,50);
 
         addChild(rn);
 
 
         super.init(gl);
 
     }
 
 
     public int getWidth()
     {
         return levelData.getWidth();
     }
 
 
     public int getHeight()
     {
         return levelData.getHeight();
     }
 
 
     /**
      *
      * @param pX
      * @param pY
      */
     public void moveToPostion(float pX, float pY)
     {
         //pX = getWidth()/2;
         //pY += getHeight()/2;
         System.out.println("px: " + pX + ", py: " + pY);
 
         Color c = new Color(levelData.getRGB((int)pX, (int)pY));
         int red = c.getRed();
         int green = c.getGreen();
         int blue = c.getBlue();
 
         if( ! (red < 250 && green < 250 && blue < 250) )
         {
             player.setTranslation((-getWidth()/2) + pX, (-getHeight()/2) + pY);
         }
 
         //check to ensure this isn't black
     }
 
 
 
     private static final int P_MOVE = 1;
 
 
     private float getCurrentPlayerX()
     {
         return player.getTranslation()[0] + getWidth()/2;
     }
 
     private float getCurrentPlayerY()
     {
         return player.getTranslation()[1] + getHeight()/2;
     }
 
     @Override
     public void update()
     {
         CameraController key = CameraController.DEFAULT_CONTROLLER;
 
 
         if(key.poll(KeyEvent.VK_W) || key.poll(KeyEvent.VK_UP))
         {
             moveToPostion(getCurrentPlayerX(), getCurrentPlayerY() + P_MOVE);
         }
         if(key.poll(KeyEvent.VK_S) || key.poll(KeyEvent.VK_DOWN))
         {
             moveToPostion(getCurrentPlayerX(), getCurrentPlayerY() - P_MOVE);
         }
         if(key.poll(KeyEvent.VK_A) || key.poll(KeyEvent.VK_LEFT))
         {
             moveToPostion(getCurrentPlayerX() - P_MOVE, getCurrentPlayerY());
         }
         if(key.poll(KeyEvent.VK_D) || key.poll(KeyEvent.VK_RIGHT))
         {
             moveToPostion(getCurrentPlayerX() + P_MOVE, getCurrentPlayerY());
         }
 
         if(key.poll(KeyEvent.VK_SPACE))
         {
             interact();
         }
 
         super.update();
     }
 
 
     @Override
     public void draw(GL gl)
     {
 
         //gl.glTranslatef(-(getWidth() / 2 / SCALE_DOWN) + (playerX /SCALE_DOWN) , -(getHeight() / 2 / SCALE_DOWN) + (playerY /SCALE_DOWN), 0);
 
         /*
         gl.glTranslatef((getWidth() / SCALE_DOWN) - playerX / SCALE_DOWN, (getHeight() / SCALE_DOWN) - playerY / SCALE_DOWN, 0);
 
         gl.glBegin(GL.GL_POINT);
             gl.glColor3f(0.0f, 0.0f, 1.0f);//, levelID, playerX)
             gl.glVertex2f(playerX / SCALE_DOWN, playerY / SCALE_DOWN);
         gl.glEnd();
 
          */
 
         //move to the bottom left corner to make things easier
         //gl.glTranslatef((float)getWidth()/2, (float)getHeight()/2, 0);
 
         
 
         gl.glTranslatef(-player.getTranslation()[0], -player.getTranslation()[1], 0);
 
         super.draw(gl);
 
 
         
 
 
         
     }
 }
