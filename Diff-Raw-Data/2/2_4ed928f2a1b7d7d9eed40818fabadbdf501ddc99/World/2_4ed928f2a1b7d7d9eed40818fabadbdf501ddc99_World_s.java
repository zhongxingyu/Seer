 package graphics;
 
 import java.awt.*; 
 import java.awt.event.*;
 import java.awt.image.*;
 import java.io.*;
 import java.util.Stack;
 import javax.swing.*;
 import javax.swing.event.*;
  
 import entities.*;
 
 public class World extends Canvas implements Runnable, KeyListener, MouseInputListener {
 
     private static final long serialVersionUID = 1L;
     private static final int MAX_FPS = 60;
     private static final String FONT_FILE = "res/font.ttf";
 
     private boolean running;
     private long lastTick;
     private JFrame frame;
     private Font font;
     
     private Ball _ball;
     private Paddle _paddle;
     private Brick[][] _bricks;
 
     public World() {
         running = false;
         lastTick = 0;
         frame = new JFrame();
         font = loadFont();
         
         _ball = new Ball(400,500);
         _paddle = new Paddle(350, 550, 100, 10);
         _bricks = new Brick[10][5];
     }
 
     public void setup() {
        
         setPreferredSize(new Dimension(800,600));
 
         setFocusable(true);
         addKeyListener(this);
         addMouseListener(this);
         addMouseMotionListener(this);
 
         frame.setTitle("Breakout");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.add(this);
         frame.pack();
         frame.setVisible(true);
         	
         for (int y = 0; y < 5; y++){
         	for (int x = 0; x < 10; x++) {
         		_bricks[x][y] = new Brick(x*80 + 5, y*50 + 5, 70, 40);
         		_bricks[x][y].setOn(true);
         	}
         }
         	
     }
 
     private Font loadFont() {
         try {
             InputStream stream = new FileInputStream(FONT_FILE);
             return Font.createFont(Font.TRUETYPE_FONT, stream);
         } catch (FontFormatException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public void run() {
         running = true;
         while (alive()) {
             render();
             tick();
         }
     }
 
     private void render() {
         BufferStrategy strategy = getBufferStrategy();
         if (strategy == null) {
             createBufferStrategy(2);
             return;
         }
         Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics();
         graphics.setColor(Color.BLACK);
         graphics.setFont(font);
         graphics.fillRect(0, 0, getWidth(), getHeight());
         for (int y = 0; y < 5; y++){
         	for (int x = 0; x < 10; x++) {
         		_bricks[x][y].update();
         		_bricks[x][y].draw(graphics);
        		if (_ball.bounce(_bricks[x][y].getRect(), false))
         			_bricks[x][y].setOn(false);
         	}
         }
         _ball.update();
         _ball.bounce(_paddle.getRect(), true);
         _ball.draw(graphics);
         _paddle.update();
         _paddle.draw(graphics);
         graphics.dispose();
         strategy.show();
     }
 
     private void tick() {
         long since = System.currentTimeMillis() - lastTick;
         if (since < 1000 / MAX_FPS) {
             try {
                 Thread.sleep(1000 / MAX_FPS - since);
             } catch (InterruptedException e) {
                 return;
             }
         }
         lastTick = System.currentTimeMillis();
     }
 
     public boolean alive() {
         return running;
     }
 
     public void shutdown() {
         running = false;
     }
 
     public Font getFont() {
         return font;
     }
 
     public int getXOffset(Graphics2D graphics, Font font, String text) {
         double fontWidth = font.getStringBounds(text, graphics.getFontRenderContext()).getWidth();
         return (int) (getWidth() - fontWidth) / 2;
     }
 
     public void hideCursor() {
         BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
         Toolkit kit = Toolkit.getDefaultToolkit();
         Cursor blankCursor = kit.createCustomCursor(img, new Point(0, 0), "hidden");
         setCursor(blankCursor);
     }
 
     public void showCursor() {
         setCursor(Cursor.getDefaultCursor());
     }
 
     /* EVENT HANDLERS */
 
     @Override
     public void keyTyped(KeyEvent event) {
  
     }
 
     @Override
     public void keyPressed(KeyEvent event) {
     	_paddle.update(event);
     	if (event.getKeyCode() == KeyEvent.VK_Q)
     		shutdown();
     }
 
     @Override
     public void keyReleased(KeyEvent event) {
     	_paddle.update(event);
     }
 
     @Override
     public void mouseClicked(MouseEvent event) {
 
     }
 
     @Override
     public void mouseEntered(MouseEvent event) {
  
     }
 
     @Override
     public void mouseExited(MouseEvent event) {
 
     }
 
     @Override
     public void mousePressed(MouseEvent event) {
 
     }
 
     @Override
     public void mouseReleased(MouseEvent event) {
 
     }
 
     @Override
     public void mouseDragged(MouseEvent event) {
 
     }
 
     @Override
     public void mouseMoved(MouseEvent event) {
 
     }
 }
