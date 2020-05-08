 import java.awt.Canvas;
 import java.awt.Dimension;
 import javax.swing.JFrame;
 import java.awt.BorderLayout;
 import java.awt.image.*;
 import java.awt.Graphics;
 import java.awt.Color;
 import java.io.IOException;
 
 public class Game extends Canvas implements Runnable{
 
     private static final long serialVersionUID = 1L;
     private static final int WIDTH = 160;
     private static final int HEIGHT = WIDTH / 12 * 9;
     private static final int SCALE = 3;
     private static final String NAME = "Betray V" + serialVersionUID + " Alpha";
 
     private boolean running = false;
     public int tickCount = 0;
     private JFrame frame;
 
     private Screen screen;
     public InputHandler input;
     public Level level;
     public Player player;
 
     private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
     private int[] pixels = ( (DataBufferInt) image.getRaster().getDataBuffer() ).getData();
     private int[] colors = new int[6*6*6];  // RGB
 
     public Game(){
         frame = new JFrame(NAME);
 
         frame.setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
         frame.setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
         frame.setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setLayout(new BorderLayout());
         frame.add(this, BorderLayout.CENTER);
         frame.pack();
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
         this.requestFocus();    // No clicking in to move anymore!
     }
 
     public void init(){
         int i = 0;
         for(int r = 0; r < 6; r++){
             for(int g = 0; g < 6; g++){
                 for(int b = 0; b < 6; b++){
                     int rr = (r * 255 / 5);
                     int gg = (g * 255 / 5);
                     int bb = (b * 255 / 5);
 
                     colors[i++] = rr << 16 | gg << 8 | bb;
                 }
             }
         }
 
         screen = new Screen(WIDTH, HEIGHT, new SpriteSheet("res/spriteSheet.png"));
         input = new InputHandler(this);
         level = new Level("res/levels/waterTestLevel.png");
         player = new Player(level, 15, 15, input);
         level.addEntity(player);
     }
 
     public void run(){
         long lastTime = System.nanoTime();
         double nsPerTick = 1000000000D/60D;
 
         int ticks = 0;
         int frames = 0;
 
         long lastTimer = System.currentTimeMillis();
         double delta = 0;
 
         init();
 
         while(running){
             long now = System.nanoTime();
             delta += (now - lastTime) / nsPerTick;
             lastTime = now;
             boolean shouldRender = true;
 
             while(delta >= 1){
                 ticks++;
                 tick();
                 delta -= 1;
                 shouldRender = true;
             }
 
             try{
                 Thread.sleep(2);
             }
             catch(InterruptedException e){
                 e.printStackTrace();
             }
             
             if(shouldRender){
                 frames++;
                 render();
             }
 
             if( (System.currentTimeMillis() - lastTimer) > 1000){
                 lastTimer += 1000;
                 System.out.println("Frames: " + frames + ", Ticks: " + ticks);
                 frames = 0;
                 ticks = 0;
             }
         }
     }
 
 
     public void tick(){
         level.tick();
         tickCount++;
         if(player.isTouchingDoor){
             level.imagePath = level.getTile(player.x >> 3, player.y >> 3).imgPath;
             level.loadLevelFromFile();
             if(player.spawnAtOldPosition){
                 level.removeEntity(player);
                player = new Player(level, level.lastx, level.lasty+8, input);  // NOT good
                 level.addEntity(player);
                 level.isInside = false;
                 player.spawnAtOldPosition = false;
                 player.isTouchingDoor = false;
                 return;
             }
 
             level.removeEntity(player);
            player = new Player(level, 15, 15, input);  // NOT good
             level.addEntity(player);
             level.isInside = true;
 
 
         }
     }
 
     public void render(){
         BufferStrategy bs = getBufferStrategy();
         if(bs == null){
             createBufferStrategy(3);
             return;
         }
 
         int xOffset = player.x - (screen.width / 2);
         int yOffset = player.y - (screen.height / 2);
 
         level.renderTiles(screen, xOffset, yOffset);
 
         level.renderEntities(screen);
         
         for(int y = 0; y < screen.height; y++){
             for(int x = 0; x < screen.width; x++){
                 int colorCode = screen.pixels[x + y * screen.width];
                 if(colorCode < 255){
                     pixels[x + y * WIDTH] = colors[colorCode];
                 }
             }
         }        
         Graphics g = bs.getDrawGraphics();
         g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
 
         g.dispose();
         bs.show();
     }
 
     public synchronized void start(){
         running = true;
         new Thread(this).start();
     }
 
     public synchronized void stop(){
         running = false;
     }
 
     public static void main(String args[]){
         new Game().start();
     }
 }
