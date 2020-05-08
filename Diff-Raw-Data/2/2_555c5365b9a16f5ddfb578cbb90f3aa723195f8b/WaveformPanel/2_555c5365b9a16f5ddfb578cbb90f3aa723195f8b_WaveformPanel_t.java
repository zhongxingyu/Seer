 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jcue.ui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.nio.ByteBuffer;
 import java.nio.FloatBuffer;
 import javax.swing.JPanel;
 import jcue.domain.AudioStream;
 
 /**
  *
  * @author oppilas
  */
 public class WaveformPanel extends JPanel {
 
     private int maxWidth, maxHeight;
     private BufferedImage waveImg;
     
     private FloatBuffer streamData;
 
     public WaveformPanel(int maxWidth, int maxHeight) {
         super();
         
         this.setBackground(Color.yellow);
 
         this.maxWidth = maxWidth;
         this.maxHeight = maxHeight;
 
         waveImg = new BufferedImage(maxWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
     }
 
     public void setWaveImg(BufferedImage waveImg) {
         this.waveImg = waveImg;
     }
     
     public void setAudioStream(AudioStream as) {
         this.streamData = as.getStreamData();
     }
     
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         
         int width = this.getWidth();
         int height = this.getHeight();
         int step = (int) Math.ceil(this.streamData.capacity() / width);
         
         g.setColor(new Color(64, 64, 64));
         g.fillRect(0, 0, width, height);
         
         g.setColor(new Color(200, 200, 250));
         for (int i = 0; i < width; i++) {
             float maxValue = 0.0f;
             
            for (int k = (i * step); k < (i * step + step); k++) {
                 if (this.streamData.get(k) > maxValue) {
                     maxValue = this.streamData.get(k);
                 }
             }
             
             g.drawLine(i, height / 2, i, (int) ((height / 2) + (height / 2) * maxValue));
             g.drawLine(i, height / 2, i, (int) ((height / 2) - (height / 2) * maxValue));
         }
         
     }
     
     
 }
