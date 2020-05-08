 /*
  *  This file is part of CandideRL.
  *
  *  CandideRL is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  CandideRL is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with CandideRL.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package game.ui.swing;
 
 import game.ui.GameUI;
 import game.utility.ColoredChar;
 
 import javax.swing.JFrame;
 import javax.swing.WindowConstants;
 
 import java.awt.Toolkit;
 import java.awt.Dimension;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /*
     TODO
         Implement menu to configure game options. Another way would be to
         read all currently hardcoded constants from some file
         Second way is preferable.
  */
 
 public class SwingGameUI implements GameUI {
 
     private JFrame mainWindow;
     private TextWindow mapWindow;
     private final int DEFAULT_MAP_WIDTH = 80;
     private final int DEFAULT_MAP_HEIGHT = 25;
 
     private Character key = ' ';
 
     private final Log log = LogFactory.getLog(SwingGameUI.class);
 
     public static GameUI getUI() {
         return new SwingGameUI();
     }
 
     private SwingGameUI() {
         long initTime = 0;
         if (log.isTraceEnabled()) {
             log.trace("SwingGameUI creation start");
             initTime = System.currentTimeMillis();
         }
 
         mainWindow = new JFrame("CandideRL");
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         mapWindow = TextWindow.getTextWindow(DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT, screenSize);
 
         if (log.isTraceEnabled()) {
             log.trace(String.format("Created console :: %dms", System.currentTimeMillis() - initTime));
             log.trace(String.format("mapWindow preferred = %s%n mapWindow minimum = %s%n mapWindow maximum = %s",
                     mapWindow.getPreferredSize(), mapWindow.getMinimumSize(), mapWindow.getMaximumSize()));
         }
 
         mainWindow.getContentPane().add(mapWindow);
         mainWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         KeyListener kl = new KeyListener() {
             @Override
             public void keyTyped(KeyEvent e) { }
 
             @Override
             public void keyPressed(KeyEvent e) {
                synchronized (key) {
                    key.notify();
                 }
                key = e.getKeyChar();
             }
 
             @Override
             public void keyReleased(KeyEvent e) { }
         };
         mainWindow.setPreferredSize(mapWindow.getPreferredSize());
         mainWindow.setResizable(false);
         mainWindow.addKeyListener(kl);
         mainWindow.pack();
         mainWindow.setVisible(true);
     }
 
     @Override
     public void drawMap(ColoredChar[][] charMap) {
         if (log.isTraceEnabled()) {
             log.trace("drawMap begin");
         }
         ColoredChar c;
         for (int i = 0; i < charMap[0].length; i++) {
             for (int j = 0; j < charMap.length; j++) {
                 c = charMap[j][i];
                 mapWindow.write(c.getChar(), c.getForeground(), c.getBackground());
             }
         }
         mapWindow.repaint();
         if (log.isTraceEnabled()) {
             log.trace("drawMap end");
         }
     }
 
     @Override
     public char getInputChar() {
        synchronized (key) {
             try {
                key.wait();
             } catch (InterruptedException exc) {
                 throw new RuntimeException(exc);
             }
         }
        return key;
     }
 
     @Override
     public void showAnnouncement(String msg) {
     }
 
     @Override
     public void showMessage(String msg) {
     }
 
     @Override
     public void showStats(String stats) {
     }
 
     @Override
     public void close() {
         if (log.isTraceEnabled()) {
             log.trace("SwingGameUI close begin");
         }
         mainWindow.setVisible(false);
         mainWindow.dispose();
         if (log.isTraceEnabled()) {
             log.trace("SwingGameUI close end");
         }
     }
 
     @Override
     public int getScreenWidth() {
         return DEFAULT_MAP_WIDTH;
     }
 
     @Override
     public int getScreenHeight() {
         return DEFAULT_MAP_HEIGHT;
     }
 
     @Override
     public int getMapWidth() {
         return DEFAULT_MAP_WIDTH;
     }
 
     @Override
     public int getMapHeight() {
         return DEFAULT_MAP_HEIGHT;
     }
 }
