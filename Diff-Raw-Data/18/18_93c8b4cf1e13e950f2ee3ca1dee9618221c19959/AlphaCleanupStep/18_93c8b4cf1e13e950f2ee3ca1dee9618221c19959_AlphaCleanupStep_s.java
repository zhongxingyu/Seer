 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 
 @SuppressWarnings("serial")
 public class AlphaCleanupStep extends Step implements KeyListener {
     BufferedImage originalImage;
     BufferedImage processedImage;
     BufferedImage displayImage;
     double contrast = 0.05;
 
     public AlphaCleanupStep(Listener listener) {
         super(listener);
 
         setFocusable(true);
         addKeyListener(this);
     }
 
     @Override
     public void begin(Object input) {
         originalImage = (BufferedImage) input;
         setPreferredSize(new Dimension(originalImage.getWidth(), originalImage.getHeight()));
         processedImage = Utility.contrastAlpha(originalImage, contrast);
        displayImage = Utility.showAlpha(processedImage);
         repaint();
         requestFocus();
     }
 
     @Override
     public void paint(Graphics g) {
         if (displayImage != null) {
             g.drawImage(displayImage, 0, 0, displayImage.getWidth(), displayImage.getHeight(), null);
         }
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
         switch (e.getKeyCode()) {
         case KeyEvent.VK_UP:
             contrast += 0.01;
             processedImage = Utility.contrastAlpha(originalImage, contrast);
            displayImage = Utility.showAlpha(processedImage);
             repaint();
             System.out.printf("Contrast: %.2f\n", contrast);
             break;
         case KeyEvent.VK_DOWN:
             contrast -= 0.01;
             processedImage = Utility.contrastAlpha(originalImage, contrast);
            displayImage = Utility.showAlpha(processedImage);
             repaint();
             System.out.printf("Contrast: %.2f\n", contrast);
             break;
         case KeyEvent.VK_U:
             listener.update(this, processedImage);
             break;
         }
     }
 
     @Override
     public void keyReleased(KeyEvent arg0) {
     }
 
     @Override
     public void keyTyped(KeyEvent arg0) {
     }
 }
