 package tetis;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class DrawingTetris implements DrawingInterface {
 
     TetrisPanel owner;
 
     Timer tim;
 
     boolean started = false, paused = false;
     int th = 20, tw = 10;
     Tetris t = new Tetris(th, tw);
     Font drFont = new Font("Arial", Font.PLAIN, 30);
 
     public DrawingTetris(TetrisPanel ow) {
         owner = ow;
     }
 
     public void draw(Graphics g) {
         Graphics2D g2 = (Graphics2D) g;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
         g.setColor(new Color(238, 238, 238));
         ///g.fillRect(0, 0, tw * 21, th * 21); //do not remove!
         g.fillRect(0, 0, 800, 500);
         g.setColor(Color.BLACK);
         int nx = drawLeft(0, g);
         nx += drawCenter(nx, g);
         drawRight(nx, g);
     }
     
     private int drawLeft(int sx, Graphics g) {
         Graphics2D g2 = (Graphics2D) g;
         drawPreview(sx, 41, g, t.hold);
         if(t.holdf)
             g.setColor(Color.RED);
         else
             g.setColor(Color.GREEN);
         g.fillOval(10, 90, 20, 20);
         g.setColor(Color.BLACK);
         g.drawOval(10, 90, 20, 20);
         g.setFont(drFont);
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_OFF);
         g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                 RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                 RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
         g.drawString("Hold:", sx + 10, 30);
         return 90;
     }
     
     private int drawCenter(int sx, Graphics g) {
         Graphics2D g2 = (Graphics2D) g;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
         ColoredTable table = t.table();
         for(int i = 0; i < th; i++)
             for(int j = 0; j < tw; j++)
                 if (table.code[i][j] == 1) {
                     g.setColor(table.c[i][j]);
                     g.fillRoundRect(j * 20 + j + 1 + sx, i * 20 + i + 1,
                             20, 20, 5, 5);
                 } else if (table.code[i][j] == 2) {
                     g.setColor(Color.DARK_GRAY);
                     g.fillRoundRect(j * 20 + j + 1 + sx, i * 20 + i + 1,
                             20, 20, 5, 5);
                     Color light = opacityColor(table.c[i][j], 90);
                     g.setColor(light);
                     g.fillRoundRect(j * 20 + j + 1 + sx, i * 20 + i + 1,
                             20, 20, 5, 5);
                 } else {
                     g.setColor(Color.DARK_GRAY);
                     g.fillRoundRect(j * 20 + j + 1 + sx, i * 20 + i + 1,
                             20, 20, 5, 5);
                 }
         return tw * 21;
     }
     
     private void drawRight(int sx, Graphics g) {
         Graphics2D g2 = (Graphics2D) g;
 //        Block nxtblck = t.next;
 //        for(int i = 0; i < 4; i++)
 //            for(int j = 0; j < 4; j++)
 //                if (i < nxtblck.block.length &&
 //                        j < nxtblck.block[i].length &&
 //                        nxtblck.block[i][j]) {
 //                    g.setColor(nxtblck.color);
 //                    g.fillRoundRect(j * 10 + j + 10 + sx, i * 10 + i + 41,
 //                            10, 10, 5, 5);
 //                } else {
 //                    g.setColor(Color.DARK_GRAY);
 //                    g.fillRoundRect(j * 10 + j + 10 + sx, i * 10 + i + 41,
 //                            10, 10, 5, 5);
 //                }
         drawPreview(sx, 41, g, t.next);
         g.setColor(Color.BLACK);
         g.setFont(drFont);
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_OFF);
         g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                 RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                 RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
         g.drawString("Next:", sx + 10, 30);
         g.drawString("Score: " + t.ptc.getPoints(), sx + 10, 120);
         g.drawString("Lines: " + t.ptc.getLines(), sx + 10, 150);
         if(paused) {
             g.setColor(new Color(60, 60, 60, 200));
             g.fillRect(0, 0, 800, 500);
             g.setColor(Color.RED);
             g.drawString("Paused", sx + 50, 90);
         }
         if(t.gameover()) {
             //stopRepeater = true;
             tim.cancel();
             started = false;
             owner.menu.setStarted(false);
             g.setColor(Color.RED);
             g.drawString("Game over", sx + 10, 180);
         }
     }
     
     private void drawPreview(int x, int y, Graphics g, Block dblck) {
         for(int i = 0; i < 4; i++)
             for(int j = 0; j < 4; j++)
                 if (dblck != null &&
                         i < dblck.block.length &&
                         j < dblck.block[i].length &&
                         dblck.block[i][j]) {
                     g.setColor(dblck.color);
                     g.fillRoundRect(j * 10 + j + 10 + x, i * 10 + i + y,
                             10, 10, 5, 5);
                 } else {
                     g.setColor(Color.DARK_GRAY);
                     g.fillRoundRect(j * 10 + j + 10 + x, i * 10 + i + y,
                             10, 10, 5, 5);
                 }
     }
 
     public void keyPressed(KeyEvent e) {
         int code = e.getKeyCode();
         if(started && !paused){
             if(code == KeyEvent.VK_LEFT) {
                     t.moveLeft();
                     repaint();
             }
             else if(code == KeyEvent.VK_RIGHT) {
                     t.moveRight();
                     repaint();
             }
             else if(code == KeyEvent.VK_UP) {
                     t.rotate();
                     repaint();
             }
             else if(code == KeyEvent.VK_DOWN) {
                     //if(t.checkmove())
                         moveTetris();
                     repaint();
             }
             else if(code == KeyEvent.VK_ENTER ||
                     code == KeyEvent.VK_SPACE) {
                     while(moveTetris()){}
                     //moveTetris();
                     //while(t.checkmove())
                         //moveTetris();
                     repaint();
             }
             else if(code == KeyEvent.VK_SHIFT) {
                     t.swap();
                     repaint();
             }
         }
         if(code == KeyEvent.VK_PAUSE) {
             if(!t.gameover()) {
                 paused = !paused;
                 repaint();
             }
         }
         else if(code == KeyEvent.VK_ESCAPE) {
             paused = true;
             owner.openMenu();
         }
     }
 
     public void start() {
         t = new Tetris(th, tw);
         TimerTask task = new TimerTask() {
             public void run(){
                 started = true;
                 if(!paused)
                     moveTetris();
                 repaint();
             }
         };
         repaint();
         tim = new Timer();
         tim.schedule(task, 1000, 1000);
     }
 
     public boolean moveTetris() {
         if(started)
             return t.move();
         return false;
     }
 
     public void stopTetris() {
         if(started) {
             started = false;
             tim.cancel();
         }
     }
 
     public void mouseMoved(MouseEvent e) {
     }
 
     public void mousePressed(MouseEvent e) {
     }
 
     public void repaint() {
         owner.repaint();
     }
     
     public Color opacityColor(Color c, int alpha) {
         int r = c.getRed(),
                 g = c.getGreen(),
                 b = c.getBlue();
         return new Color(r, g, b, alpha);
     }
 
 }
