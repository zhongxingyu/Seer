 package com.id.ui.app;
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JLayeredPane;
 import javax.swing.border.EmptyBorder;
 
 import com.id.events.KeyStroke;
 import com.id.events.KeyStrokeHandler;
 import com.id.ui.Constants;
 import com.id.ui.editor.TextPanel;
 
 @SuppressWarnings("serial")
 public class AppPanel extends JLayeredPane implements KeyListener {
  private final Component spotlightView;
  private final Component fileListView;
  private final Component stackView;
   private final KeyStrokeHandler handler;
  private final FinderPanel fuzzyFinderPanel;
  private final AppLayout appLayout = new AppLayout();
 
   public AppPanel(Component fileListView, Component spotlightView,
       Component stackView, KeyStrokeHandler handler, FinderPanel fuzzyFinderPanel,
       TextPanel minibufferView) {
    this.spotlightView = spotlightView;
    this.fileListView = fileListView;
    this.stackView = stackView;
     this.handler = handler;
    this.fuzzyFinderPanel = fuzzyFinderPanel;
     setBorder(new EmptyBorder(22, 22, 22, 22));
     setLayout(appLayout);
     setFocusTraversalKeysEnabled(false);
     add(fileListView, "filelist");
     add(spotlightView, "spotlight");
     add(stackView, "stack");
     add(minibufferView, "minibuffer");
     add(fuzzyFinderPanel, "fuzzyfinder");
     setLayer(fuzzyFinderPanel, JLayeredPane.POPUP_LAYER);
     fuzzyFinderPanel.setVisible(false);
   }
 
   @Override
   public void paintComponent(Graphics g) {
     super.paintComponent(g);
     g.setColor(Constants.BG_COLOR);
     g.fillRect(0, 0, getWidth(), getHeight());
   }
 
   // KeyListener.
   @Override
   public void keyTyped(KeyEvent e) {
     // Do nothing.
   }
 
   @Override
   public void keyPressed(KeyEvent e) {
     // The editor doesn't care about shift being pressed.
     if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
       return;
     }
     KeyStroke keyStroke = KeyStroke.fromKeyEvent(e);
     logEventTranslationInfo(e, keyStroke);
     // NOTE KeyEvent.getKeyCode() is only defined in keyPressed when control
     // is down.
     if (keyStroke.getKeyChar() == 'q' && keyStroke.isControlDown()) {
       System.exit(0);
     }
 
     // TODO(koz): Don't repaint on every keystroke.
     this.handler.handleKeyStroke(keyStroke);
     repaint();
   }
 
   @Override
   public void keyReleased(KeyEvent e) {
     // Do nothing.
   }
 
   private void logEventTranslationInfo(KeyEvent event, KeyStroke keyStroke) {
 //    System.out.println("event: " + event);
 //    System.out.println("keystroke: " + keyStroke);
   }
 }
