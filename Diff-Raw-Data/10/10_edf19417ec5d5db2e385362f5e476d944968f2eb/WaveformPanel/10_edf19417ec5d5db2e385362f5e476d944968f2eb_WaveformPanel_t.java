 package tazam;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import javax.swing.JPanel;
 
 /**
  * Draws the waveform of the selected audio file.
  *
  */
 public class WaveformPanel extends JPanel {
 
     /**
      * The default horizontal scale factor in the display
      */
     public static double HSCALE = 0.1;
     //public static int WIDTH = 2000;
     /**
      * The height of the waveform panel
      */
     public static int HEIGHT = 150;
     /**
      * The data
      */
 //    private Signal signal;
     /**
      * Contains the samples of data of audio
      */
     private double[] samples;
     /**
      * The scale of the waveform horizontally
      */
     private double hscale = 0.1;
     /**
      * The scale of the waveform vertically
      */
     private double vscale;
     /**
      * The number of total samples in the audio clip
      */
     private long numberOfSamples;
 
     /**
      * Constructs a waveform panel using a Signal object.
      *
      * @param signal The signal whose data will be drawn.
      */
     public WaveformPanel(Signal signal) {
         super();
 //        this.signal = signal;
         samples = signal.getSamples();
         numberOfSamples = samples.length;
         double width = numberOfSamples * hscale;
         setPreferredSize(new Dimension((int) width, HEIGHT));
         //setPreferredSize(new Dimension(WIDTH, HEIGHT));
 
         setBackground(Color.WHITE);
         setLayout(new FlowLayout());
         revalidate();
         repaint();
     }
 
     /**
      * Paints the audio file's waveform.
      *
      * @param g The graphics context for painting the component
      */
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         double width = numberOfSamples * hscale;//local width that will be changed as user zooms in or out
         setPreferredSize(new Dimension((int) width, HEIGHT));
         g.setColor(Color.BLACK);
         int oldY = getHeight() / 2;
         int oldX = 0;
         g.drawLine(oldX, oldY, getWidth(), oldY);//draw line of origin
         getVscale();
 
         g.setColor(Color.BLUE);
         for (int t = 0; t < samples.length; t++) {
             double scaledSample = samples[t] * vscale;
             double scaledX = (double) (((double) t / (double) samples.length) * width);
             //double scaledX = (double)(((double)t/(double)samples.length)*WIDTH);
 
             int x = (int) scaledX;
             int y = (int) ((getHeight() / 2 - scaledSample));
             g.drawLine(oldX, oldY, x, y);
 
             oldX = x;
             oldY = y;
         }
     }
 
     /**
      * Zooms in to the waveform.
      */
     public void zoomIn() {
         hscale *= 1.5;
         //WIDTH *= 1.5;
         revalidate();
         repaint();
     }
 
     /**
      * Zooms out of the waveform.
      */
     public void zoomOut() {
        hscale /= 1.5;
         //WIDTH /= 1.5;
         revalidate();
         repaint();
     }
 
     /**
      * Resets the view of the waveform display
      */
     public void resetZoom() {
         hscale = HSCALE;
         revalidate();
         repaint();
     }
 
     /**
      * Gets the original range of values in the data.
      *
      * @return The range of the values.
      */
     private double getDataHeight() {
         double min = Double.MAX_VALUE;
         double max = Double.MIN_VALUE;
         for (int i = 0; i < samples.length; i++) {
             double sample = samples[i];
             if (sample < min) {
                 min = sample;
             }
         }
         for (int i = 0; i < samples.length; i++) {
             double sample = samples[i];
             if (sample > max) {
                 max = sample;
             }
         }
         return (max - min);
     }
 
     /**
      * Sets the vertical pixel scale based on the data.
      */
     private void getVscale() {
         double dataHeight = getDataHeight();
         vscale = getHeight() / dataHeight;
     }
 }
