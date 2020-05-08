 /*
  * Created on Apr 3, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.spacebar.escape.j2se;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.imageio.ImageIO;
 
 import org.spacebar.escape.common.Continuation;
 import org.spacebar.escape.common.Level;
 import org.spacebar.escape.common.Solution;
 
 /**
  * @author adam
  * 
  * TODO To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Style - Code Templates
  */
 public class EscapeFrame extends Frame {
     public final static int STARTW = 800;
 
     public final static int STARTH = 600;
 
     PlayCanvas pc;
 
     public EscapeFrame() {
         super("Escape");
         initFrame();
 
         setVisible(true);
     }
 
     /**
      *  
      */
     private void initFrame() {
         setBackground(Color.BLACK);
 
         // icon
         InputStream in = ResourceUtil.getLocalResourceAsStream("icon.png");
         try {
             setIconImage(ImageIO.read(in));
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         setSize(STARTW, STARTH);
     }
 
     public EscapeFrame(Level l) {
         super("Escape");
         initFrame();
 
         setLevel(l);
         setVisible(true);
     }
     
     public EscapeFrame(Level l, Solution s) {
         super("Escape");
         initFrame();
 
         setLevel(l);
         setSolution(s);
         setVisible(true);
     }
 
     private void setSolution(Solution s) {
         pc.setSolution(s);
     }
 
     private WindowAdapter escapeSimulator;
 
     /**
      * @param l
      * @param c
      */
     public void setLevel(Level l) {
         // used to get out of the canvas
         Continuation c = new Continuation() {
             public void invoke() {
                 System.exit(0);
             }
         };
 
         setTitle("\"" + l.getTitle() + "\" by " + l.getAuthor() + " ("
                 + l.getWidth() + "x" + l.getHeight() + ") - Escape");
 
         if (pc != null) {
             remove(pc);
         }
         if (escapeSimulator != null) {
             removeWindowListener(escapeSimulator);
         }
 
         pc = new PlayCanvas(l, c);
         escapeSimulator = new WindowAdapter() {
             @Override
 			public void windowClosing(WindowEvent e) {
                 // simulate press of ESCAPE
                 KeyEvent ke = new KeyEvent(EscapeFrame.this,
                         KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0,
                         KeyEvent.VK_ESCAPE, KeyEvent.CHAR_UNDEFINED);
                 pc.dispatchEvent(ke);
             }
         };
         addWindowListener(escapeSimulator);
 
         add(pc);
         pc.setPreferredSize(new Dimension(STARTW, STARTH));
         pack();
     }
 }
