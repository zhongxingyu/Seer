 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.fourisland.frigidearth;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Toolkit;
 import java.awt.Transparency;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.util.List;
 import java.util.Stack;
 import java.util.concurrent.CopyOnWriteArrayList;
 import javax.swing.JFrame;
 
 /**
  *
  * @author hatkirby
  */
 public class Main extends Canvas
 {
    public static final int CANVAS_WIDTH = 636;
     public static final int CANVAS_HEIGHT = 480;
     
     private static JFrame mainWindow;
     private static Color[][] grid;
     private static int drawOffsetX = 0;
     private static int drawOffsetY = 0;
     private static Canvas gameCanvas;
     private static List<Renderable> renderables = new CopyOnWriteArrayList<Renderable>();
     private static Stack<Inputable> inputables = new Stack<Inputable>();
     private static GameState gameState;
     
     public static void main(String[] args)
     {
         gameCanvas = new Main();
         
         mainWindow = new JFrame();
         mainWindow.setTitle("Frigid Earth");
         mainWindow.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
         mainWindow.setLocation(GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().x-CANVAS_WIDTH/2, GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint().y-CANVAS_HEIGHT/2);
         mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         mainWindow.addKeyListener(new KeyListener() {
             @Override
             public void keyTyped(KeyEvent ke)
             {
                 // No me importa
             }
 
             @Override
             public void keyPressed(KeyEvent ke)
             {
                 inputables.peek().processInput(ke);
                 render(gameCanvas);
             }
 
             @Override
             public void keyReleased(KeyEvent ke)
             {
                 // No me importa
             }
         });
         mainWindow.add(gameCanvas);
         mainWindow.setVisible(true);
         mainWindow.setSize(CANVAS_WIDTH, CANVAS_HEIGHT+mainWindow.getInsets().top);
         
         gameCanvas.createBufferStrategy(2);
         
         setGameState(new MapViewGameState());
     }
     
     public static void setGameState(GameState m_gameState)
     {
         renderables.clear();
         inputables.clear();
         
         gameState = m_gameState;
         renderables.add(gameState);
         inputables.push(gameState);
         
         render(gameCanvas);
     }
     
     public static void addRenderable(Renderable renderable)
     {
         renderables.add(renderable);
     }
     
     public static void removeRenderable(Renderable renderable)
     {
         renderables.remove(renderable);
     }
     
     public static void addInputable(Inputable inputable)
     {
         inputables.push(inputable);
     }
     
     public static void removeInputable(Inputable inputable)
     {
         if (inputables.peek() == inputable)
         {
             inputables.pop();
         } else {
             throw new IllegalStateException("Inputable to be removed must be at the top of the stack");
         }
     }
     
     private static void render(Canvas gameCanvas)
     {
         BufferStrategy buffer = gameCanvas.getBufferStrategy();
         GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
         GraphicsDevice device = env.getDefaultScreenDevice();
         GraphicsConfiguration config = device.getDefaultConfiguration();
         BufferedImage vImg = config.createCompatibleImage(CANVAS_WIDTH, CANVAS_HEIGHT, Transparency.TRANSLUCENT);
         Graphics2D g = vImg.createGraphics();
         
         for (Renderable renderable : renderables)
         {
             renderable.render(g);
         }
 
         do {
             do {
                 float wt = mainWindow.getWidth() / (float) CANVAS_WIDTH;
                 float ht = (mainWindow.getHeight() - mainWindow.getInsets().top) / (float) CANVAS_HEIGHT;
                 int renderWidth = Math.round(Math.min(wt, ht) * CANVAS_WIDTH);
                 int renderHeight = Math.round(Math.min(wt, ht) * CANVAS_HEIGHT);
                 int renderX = (mainWindow.getWidth()/2)-(renderWidth/2);
                 int renderY = ((mainWindow.getHeight()-mainWindow.getInsets().top)/2)-(renderHeight/2);
                 
                 Graphics g2 = buffer.getDrawGraphics();
                 g2.setColor(Color.BLACK);
                 g2.fillRect(0, 0, mainWindow.getWidth(), mainWindow.getHeight());
                 g2.drawImage(vImg, renderX, renderY, renderWidth, renderHeight, gameCanvas);
                 g2.dispose();
             } while (buffer.contentsRestored());
 
             buffer.show();
         } while (buffer.contentsLost());
 
         
         Toolkit.getDefaultToolkit().sync();
     }
 
     @Override
     public void paint(Graphics grphcs)
     {
         render(this);
     }
     
 }
