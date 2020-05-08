 
 package Version1;
 import java.util.*;
 import java.util.Random;
 import java.awt.Font;
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.*;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.util.ResourceLoader;
 
 public class Main
 {
     float square2x1 = 50;
     float square2y1 = 50;
     float square2x2 = 100;
     float square2y2  = 100;
     boolean shoot = false;
     int score =0;
     TrueTypeFont font;
     private ArrayList<Object> bulletObjs = new ArrayList<Object>();
     private ArrayList<Object> policeObjs = new ArrayList<Object>();
     private Object testObj;
     
     public static void main(String[] args)
     {
         Main main = null;
         try
         {
           main = new Main();
           main.create();
           main.run();
         }
         catch(Exception ex)
         {
             //LOGGER.log(Level.SEVERE,ex.toString(),ex);
         }
     }
     
     
     public void create() throws LWJGLException
     {
         
        initGL(1024, 600);
         
         //wObjs.add(new Object());
         testObj = new Object();
        testObj.init("resource/rexWithTGun2.png", 0.0f, 0.0f, 32.0f, 64.0f, 0.0f, 0.0f, 32.0f, 64.0f);
         
         Font awtFont = new Font("Times New Roman", Font.BOLD, 24);
         font = new TrueTypeFont(awtFont, false);
         
         Mouse.setGrabbed(false);
         Mouse.create();        
     }
     
      
     public void destroy()
     {
         Mouse.destroy();
         Display.destroy();
     }
     
     public void initGL(int width, int height)
     {
         try
         {
             Display.setDisplayMode(new DisplayMode(width,height));
             Display.create();
             Display.setVSyncEnabled(true);
 	}
         catch (LWJGLException e)
         {
             e.printStackTrace();
             System.exit(0);
 	}
  
 	glEnable(GL11.GL_TEXTURE_2D);
         glClearColor(0.0f,0.0f,0.0f,0.0f);
         
         glEnable(GL11.GL_BLEND);
         glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
         
         glViewport(0,0,width,height);
 	glMatrixMode(GL_MODELVIEW);
         
         glMatrixMode(GL_PROJECTION);
         glLoadIdentity();
         glOrtho(0, width, height, 0, 1, -1);
         //gluOrtho2D(0.0f,width,0.0f,height);
 	glMatrixMode(GL_MODELVIEW);
         
         glDisable(GL_DEPTH_TEST);
         glDisable(GL_LIGHTING);
     }
     
     public void render()
     {
         glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
         glLoadIdentity();
         
         testObj.render();
         for(int i = 0; i < bulletObjs.size(); i++)
         {
             bulletObjs.get(i).render();
         }
         for(int j =0; j < policeObjs.size(); j++)
         {
             policeObjs.get(j).render();
         }
         
         displayScore();
     }
     
     public void processMouse()
     {
         float XPos = Mouse.getX();
         float YPos = Mouse.getY();
              
     }
     
     public void processKeyboard()
     {
         if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
         {
             testObj.xPos += 0.5;
         }
         
         if(Keyboard.isKeyDown(Keyboard.KEY_LEFT))
         {
             testObj.xPos -= 0.5;
         }
         
         if(Keyboard.isKeyDown(Keyboard.KEY_UP))
         {
             testObj.yPos += 1;
         }
         if(Keyboard.isKeyDown(Keyboard.KEY_DOWN))
         {
             testObj.yPos -= 1;
         }
         if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
         {
               shoot = true;
               if(bulletObjs.size() > 0)
               {
                 if(bulletObjs.get(bulletObjs.size()-1).xPos > (testObj.xPos + 100))
                 {
                   Object shot = new Object();
                   shot.init("resource/bullet.png",testObj.xPos +25 , testObj.yPos +22, 4, 4, 0, 0, 4, 4);
                   bulletObjs.add(shot);
                 }
               }
               else
               {
                   Object shot = new Object();
                   shot.init("resource/bullet.png",testObj.xPos +25 , testObj.yPos +22, 4, 4, 0, 0, 4, 4);
                   bulletObjs.add(shot);
               }
         }
     }
     
     public void run()            
     {
       while(!Display.isCloseRequested())
       {
           if(Display.isVisible())
           {
               processKeyboard();
               processMouse();
               update();
               render();
           }
           Display.update();
       }
     }
     
     public void update()
     {
         constrainPlayer();
         bulletMove();
         addPoPo();
         bulletCol();
     }
     
     public void constrainPlayer()
     {
         if(testObj.xPos < 5)
         {
             testObj.xPos = 5;
         }
         if(testObj.xPos > 970)
         {
             testObj.xPos = 970;
         }
         if(testObj.yPos < 2)
         {
             testObj.yPos = 2;
         }
         if(testObj.yPos > 150)
         {
             testObj.yPos = 150;
         }
     }
     public void bulletMove()
     {
         for(int i = 0; i < bulletObjs.size(); i++)
         {
             bulletObjs.get(i).xPos += 1;
         }
     }
     
     public void addPoPo()
     {
         Random randomGenerator = new Random();
         int chance = randomGenerator.nextInt(1000);
                 
         if(chance == 501)
         {
             int randX = randomGenerator.nextInt(600);
             int randY = randomGenerator.nextInt(120);
             
             Object police = new Object();
            police.init("resource/police.png", testObj.xPos+50+randX, randY, 32, 64, 0, 0, 32, 64);
             policeObjs.add(police);
         }
     }
     
     public void bulletCol()
     {
         for(int i=0; i<bulletObjs.size(); i++)
         {
             for(int j=0; j<policeObjs.size(); j++)
             {
                 if((bulletObjs.get(i).xPos == policeObjs.get(j).xPos)&&(bulletObjs.get(i).yPos > policeObjs.get(j).yPos) &&(bulletObjs.get(i).yPos < policeObjs.get(j).yPos+64))
                 {
                     policeObjs.get(j).xPos = 2000;
                     score++;
                 }
             }
         }
     }
     
     public void displayScore()
     {
         font.drawString(200, 200, "Score: "+score, Color.green);
     }
     
 }
