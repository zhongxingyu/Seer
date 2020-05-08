 package irgame;
 
 import irgame.input.Keyboard;
 import irgame.object.Character;
 import irgame.object.Ground;
 import irgame.physics.Collision;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferStrategy;
 import javax.swing.JFrame;
 
 /**
  * @author Daniel Johansson
  */
 public class Game extends Canvas implements Runnable {   
     private static final long serialVersionUID = 1L;
     
     private static final String GAMETITLE = "Plud";
     public static final int WIDTH = 640;
     public static final int HEIGHT = WIDTH * 9 / 16;
     // public static int scale = 3;
     
     private Thread thread;
     private JFrame frame;
     private Keyboard key;
     private boolean running = false;
     
     //private Screen screen;
     
     //private BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
     //private int[] pixels = ((DataBufferInt)img.getRaster().getDataBuffer()).getData();
     
     public static int gravity = 2;
     
     //public static Ground ground;
     public static Ground[] ground = new Ground[WIDTH / 32 + 1];
     public static Character chaR;
     
     private int jump = 0;
     //private Image ground;
     
     public Game(){
         Dimension winSize = new Dimension(WIDTH/* * scale*/, HEIGHT/* * scale*/);
         setPreferredSize(winSize);
         
         //screen = new Screen(width, height);
         frame = new JFrame();
         
         key = new Keyboard();
         addKeyListener(key);
     }
     
     public synchronized void start() {
         running = true;
         thread = new Thread(this, "Display");
         thread.start();
     }
     
     public synchronized void stop() {
         running = false;
         try {
             thread.join();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
     
     public void run() {
         long lastTime = System.nanoTime();
         long timer = System.currentTimeMillis();
         final double ns = 1000000000.0 / 60.0;
         double delta = 0;
         
         int frames = 0;
         int updates = 0;
         
         //ground = new Ground(0, 0);
         for (int i = 0; i < ground.length; i++){
             /*int r = (int)(Math.random() * 3 + 1);
             System.out.println(r);*/
             if(i == 16){
                 ground[i] = new Ground(0, 0, i, 2);
             }else{
                 ground[i] = new Ground(0, 0, i, 1);
             }
             
         }
         //chaR = new irgame.object.Character((getWidth() / chaR.getSIZE()) / 2 - chaR.getSIZE() , (getHeight() / chaR.getSIZE()) / 2 - chaR.getSIZE() );
         chaR = new irgame.object.Character();
         
         
         while(running) {
             long now = System.nanoTime();
             delta += (now - lastTime) / ns;
             lastTime = now;
             
             while(delta >= 1){
                 update();
                 updates++;
                 delta--;
             }
             
             render();
             frames++;
             
             if (System.currentTimeMillis() - timer > 1000) {
                 timer += 1000;
                 frame.setTitle(GAMETITLE + " | " + updates + " ups, " + frames + " fps");
                 updates = 0;
                 frames = 0;
             }
         }
     }
     
     public void update(){
         
         chaR.yPos += gravity;
         for (int i = 0; i < ground.length; i++){
             if (ground[i].xPos <= -ground[i].SIZE){
                 ground[i].xPos += getWidth() + ground[i].SIZE;
             }
             ground[i].xPos -= chaR.HORIZ_VEL;
         }
         
         Collision.update();
         key.update();
        if (key.up){
             System.out.println(chaR.state);
             if (chaR.state.equals("standing") || chaR.state.equals("jumping")){
                 gravity = 0;
                 if (jump < chaR.JUMP_HEIGHT / chaR.JUMP_FORCE){
                     chaR.yPos -= chaR.JUMP_FORCE;
                     jump++;
                     System.out.println("gravity: " + gravity + ", jump: " + jump);
                 }
                 if (jump == 7){
                     gravity = 2;
                     jump = 0;
                     chaR.state = "falling";
                 }else{
                     chaR.state = "jumping";
                 }
                 System.out.println("gravity: " + gravity);
                 
             }
         }
         //if (key.down){}
         if (key.left){chaR.xPos -= chaR.HORIZ_VEL;}
         if (key.right){chaR.xPos += chaR.HORIZ_VEL;}
     }
     
     public void render(){
         BufferStrategy bs = getBufferStrategy();
         if (bs == null) {
             createBufferStrategy(3);
             return;
         }
         
         Graphics g = bs.getDrawGraphics();
         g.setColor(Color.DARK_GRAY);
         g.fillRect(0, 0, getWidth(), getHeight());
         
         //Ground rendering
         for (int i = 0; i < ground.length; i++){
             g.drawImage(ground[i].sprite, ground[i].xPos, ground[i].yPos, null);
         }
         
         //Character rendering
         g.drawImage(chaR.BODY, chaR.xPos, chaR.yPos + chaR.SIZE, null);
         g.drawImage(chaR.HEAD, chaR.xPos, chaR.yPos, null);
         
         g.dispose();
         bs.show();
     }
     
     public static void main(String[] args) {
         Game game = new Game();
         game.frame.setResizable(false);
         game.frame.setTitle(GAMETITLE);
         game.frame.add(game);
         game.frame.pack();
         game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         game.frame.setLocationRelativeTo(null);
         game.frame.setVisible(true);
         
         game.start();
     }
 }
