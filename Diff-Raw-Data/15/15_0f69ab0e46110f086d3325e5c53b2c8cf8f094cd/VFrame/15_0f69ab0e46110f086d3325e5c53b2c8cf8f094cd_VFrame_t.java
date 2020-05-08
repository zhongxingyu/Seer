 package org.hettoo.vui;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferStrategy;
 import java.awt.Canvas;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.RenderingHints;
 import java.awt.event.KeyListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import java.util.List;
 import java.util.ArrayList;
 
 public class VFrame {
     private JFrame jFrame;
     private VFrameCanvas canvas;
 
     public VFrame(Size size) {
         jFrame = new JFrame();
         jFrame.setUndecorated(true);
         jFrame.setResizable(false);
         setTitle("VUI application");
         JPanel panel = (JPanel)jFrame.getContentPane();
         panel.setPreferredSize(new java.awt.Dimension(size.getWidth(),
                     size.getHeight()));
         panel.setLayout(null);
 
         jFrame.pack();
 
         canvas = new VFrameCanvas(new VTheme(), panel);
         jFrame.addKeyListener(canvas);
         jFrame.addWindowListener(new WindowAdapter() {
             @Override
             public void windowActivated(WindowEvent event) {
                 draw();
             }
         });
     }
 
     public void show() {
         jFrame.setLocationRelativeTo(null);
         jFrame.setVisible(true);
     }
 
     private class VFrameCanvas extends VCanvas implements KeyListener {
         private VTheme theme;
         private VComponent component;
 
         private JPanel panel;
         private Canvas canvas;
 
         private BufferStrategy strategy;
         private Graphics graphics;
 
         private List<Key> modifierKeys;
 
         public VFrameCanvas(VTheme theme, JPanel panel) {
             super();
             this.theme = theme;
             this.panel = panel;
             canvas = new Canvas();
             canvas.addKeyListener(this);
             setSize(new Size(panel.getWidth(), panel.getHeight()));
             panel.add(canvas);
 
             canvas.setIgnoreRepaint(true);
             canvas.createBufferStrategy(2);
             strategy = canvas.getBufferStrategy();
            getGraphics();

            modifierKeys = new ArrayList<Key>();
        }
 
        private void getGraphics() {
            graphics = strategy.getDrawGraphics();
             ((Graphics2D)graphics).setRenderingHint(
                 RenderingHints.KEY_TEXT_ANTIALIASING,
                     RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         }
 
         @Override
         public void setSize(Size size) {
             super.setSize(size);
             panel.setPreferredSize(new java.awt.Dimension(size.getWidth(),
                         size.getHeight()));
             canvas.setBounds(0, 0, size.getWidth(), size.getHeight());
         }
 
         public void setComponent(VComponent component) {
             this.component = component;
             component.setParent(this);
             component.activate();
         }
 
         @Override
         public VTheme getTheme() {
             return theme;
         }
 
         private void setColor(Color color) {
             graphics.setColor(new java.awt.Color(color.getRed(),
                         color.getGreen(), color.getBlue(), color.getAlpha()));
         }
 
         @Override
         public int getTextWidth(String text, FontStyle fontStyle) {
             return graphics.getFontMetrics(
                     createFont(fontStyle)).stringWidth(text);
         }
 
         @Override
         public int getTextHeight(FontStyle fontStyle) {
             return graphics.getFontMetrics(createFont(fontStyle)).getHeight();
         }
 
         @Override
         public void drawRectangle(VRectangle rectangle) {
             setColor(rectangle.getColor());
             Rectangle rect = rectangle.getRectangle();
             Size offset = rect.getOffset();
             Size size = rect.getSize();
             graphics.fillRect(offset.getWidth(), offset.getHeight(),
                     size.getWidth(), size.getHeight());
         }
 
         private int convertFontStyle(FontType style) {
             switch (style) {
                 case BOLD:
                     return Font.BOLD;
             }
             return Font.PLAIN;
         }
 
         private Font createFont(FontStyle style) {
             return new Font("Monospaced", convertFontStyle(style.getType()),
                     style.getSize());
         }
 
         @Override
         public void drawText(String text, Size position, FontStyle fontStyle) {
             setColor(fontStyle.getColor());
             graphics.setFont(createFont(fontStyle));
             graphics.drawString(text, position.getWidth(),
                     position.getHeight());
         }
 
         @Override
         public void draw() {
             drawRectangle(new VRectangle(new Rectangle(new Size(0, 0),
                             new Size(canvas.getWidth(), canvas.getHeight())),
                         theme.getRootColor()));
             if (component != null)
                 component.draw();
             show();
         }
 
         @Override
         public void show() {
            graphics.dispose();
             strategy.show();
            getGraphics();
         }
 
         public void keyPressed(KeyEvent event) {
             Key key = Key.get(event.getKeyCode());
             if (key.isModifier() && !modifierKeys.contains(key))
                 modifierKeys.add(key);
             else if (component != null)
                 component.keyPressed(new KeyPress(key, modifierKeys));
         }
 
         public void keyReleased(KeyEvent event) {
             Key key = Key.get(event.getKeyCode());
             if (key.isModifier())
                 modifierKeys.remove(key);
         }
 
         public void keyTyped(KeyEvent event) {
             switch (event.getKeyChar()) {
                 case KeyEvent.VK_ESCAPE:
                     destroy();
                     return;
             }
         }
     }
 
     public void setComponent(VComponent component) {
         canvas.setComponent(component);
     }
 
     public void draw() {
         canvas.draw();
     }
 
     public void setTitle(String title) {
         jFrame.setTitle(title);
     }
 
     public void destroy() {
         jFrame.dispose();
     }
 }
