 package jDistsim.utils.common;
 
 import jDistsim.utils.logging.Logger;
 import jDistsim.utils.resource.Resources;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 3.3.13
  * Time: 0:41
  */
 public class PandaInjector {
 
     private boolean activate;
     private String password = "panda";
     private int level = 0;
     private JComponent canvas;
 
     public PandaInjector(JComponent canvas, JComponent listened) {
         this.canvas = canvas;
         activate = false;
         listened.addKeyListener(new KeyAdapter() {
             @Override
             public void keyReleased(KeyEvent keyEvent) {
                 String currentChar = password.substring(level, level + 1);
                 if (String.valueOf(keyEvent.getKeyChar()).equals(currentChar)) {
                     level++;
                 } else {
                     level = 0;
                 }
                 if (complete()) {
                     makePanda();
                 }
             }
         });
     }
 
     private void makePanda() {
        Logger.log("Inject panda! :)");
        Graphics2D graphics2D = (Graphics2D) canvas.getGraphics();
        Image pandaImage = Resources.getImage("system/entity/panda.png");
        graphics2D.drawImage(pandaImage, 0, 0, 256, 256, canvas);
        graphics2D.dispose();
        reset();
     }
 
     public void activate() {
         activate = true;
     }
 
     public void deactivate() {
         activate = false;
     }
 
     private boolean complete() {
         return level == password.length();
     }
 
     private void reset() {
         level = 0;
     }
 }
