 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pendulumsimulator;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Line2D;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 
 /**
  *
  * @author praveen
  */
 public class PendulumAnimation 
                 implements Runnable {
     
     private JFrame frame;
     private JFrame setup;
     private PaintPanel paintPanel;
     private ControlPanel controlPanel;
     private InvertedPendulumAI ai;
     
     private double theta, omega;
     
     private static float dt = 0.01f; 
     private static final float bobd = 50f; 
     private static final float g = 90f;
     
     private static float rodlength = 200f;
     private static float k = 400000f; 
     private static float mass;
     private static float I;
     private static float df = 0.1f;
     private static float t = 0.0f;
     private static float time = 0.0f;
     private Timer timer;
     
     private File out;
     private FileWriter outw;
     
     private boolean threadStop = false;
     
     public class UpdateTimeTask extends TimerTask {
         
         @Override
         public void run()
         {
             time += 0.01;
         }
         
     }
     
     public class PaintPanel extends JPanel {               
     
         @Override
         public void paintComponent(Graphics g)
         {
             super.paintComponent(g);
             
             // clear the screen
             g.setColor(Color.white);
             g.fillRect(0, 0, this.getWidth(), this.getHeight());
 
             // draw the circle in its new position
             Graphics2D g2d = (Graphics2D) g;
             g2d.setColor(Color.GRAY);
             g2d.setStroke(new BasicStroke(5));
             double topx = this.getWidth() / 2 + (rodlength * Math.sin(theta));
             double topy = this.getHeight() - (rodlength * Math.cos(theta));
             float shift = -200;
             g2d.draw(new Line2D.Double(this.getWidth() / 2, 
                     this.getHeight() + shift, 
                     topx, topy + shift));
             Ellipse2D.Double circle = new Ellipse2D.Double(topx - bobd/2, topy - bobd/2 + shift, bobd, bobd);
             g2d.setColor(Color.darkGray);
             g2d.fill(circle);
             g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             
             // show time
             g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
             g2d.setColor(Color.BLACK);
             g2d.drawString("Time: " + String.valueOf((double)Math.round(time * 100) / 100) + "s", 20, 30);
         }
     }
     
     public class ControlPanel extends JPanel 
                         implements ChangeListener,
                         ActionListener 
     {
         final PendulumAnimation sim = PendulumAnimation.this; 
         Thread t;
         public ControlPanel()
         {
             super();
             
             JSlider kslider = new JSlider(0, 100);
             JSlider wslider = new JSlider(0, 10);
             JSlider tslider = new JSlider(-80, 80);
             JSlider oslider = new JSlider(-20, 20);
             JSlider tislider = new JSlider(0, 100);
             JSlider dfslider = new JSlider(0, 100);
             JSlider lslider = new JSlider(0, 100);
             
             kslider.addChangeListener(this);
             kslider.setName("kslider");
             kslider.setMajorTickSpacing(10);
             kslider.setMinorTickSpacing(10);
             kslider.setPaintTicks(true);
             kslider.setLabelTable(kslider.createStandardLabels(20));
             kslider.setPaintLabels(true);
             
             wslider.addChangeListener(this);
             wslider.setName("wslider");
             wslider.setMajorTickSpacing(1);
             wslider.setMinorTickSpacing(10);
             wslider.setPaintTicks(true);
             wslider.setLabelTable(wslider.createStandardLabels(1));
             wslider.setPaintLabels(true);
             
             tslider.addChangeListener(this);
             tslider.setName("tslider");
             tslider.setMajorTickSpacing(10);
             tslider.setMinorTickSpacing(10);
             tslider.setPaintTicks(true);
             tslider.addChangeListener(this);
             tslider.setLabelTable(tslider.createStandardLabels(20));
             tslider.setPaintLabels(true);
             
             oslider.addChangeListener(this);
             oslider.setName("oslider");
             oslider.setMajorTickSpacing(2);
             oslider.setMinorTickSpacing(10);
             oslider.setPaintTicks(true);
             oslider.setPaintTrack(true);
             oslider.setLabelTable(oslider.createStandardLabels(5));
             oslider.setPaintLabels(true);
             
             tislider.addChangeListener(this);
             tislider.setName("tislider");
             tislider.setMajorTickSpacing(100);
             tislider.setMinorTickSpacing(10);
             tislider.setPaintTicks(true);
             tislider.setPaintTrack(true);
             tislider.setLabelTable(tislider.createStandardLabels(20));
             tislider.setPaintLabels(true);
             
             dfslider.addChangeListener(this);
             dfslider.setName("dfslider");
             dfslider.setMajorTickSpacing(100);
             dfslider.setMinorTickSpacing(10);
             dfslider.setPaintTicks(true);
             dfslider.setPaintTrack(true);
             dfslider.setLabelTable(dfslider.createStandardLabels(20));
             dfslider.setPaintLabels(true);
             
             lslider.addChangeListener(this);
             lslider.setName("lslider");
             lslider.setMajorTickSpacing(100);
             lslider.setMinorTickSpacing(10);
             lslider.setPaintTicks(true);
             lslider.setPaintTrack(true);
             lslider.setLabelTable(lslider.createStandardLabels(20));
             lslider.setPaintLabels(true);
             
             //tislider.setLabelTable(tislider.createStandardLabels(10));
             //tislider.setPaintLabels(true);
             
             this.add(new JLabel("Adjust current constant (T = k * i)"));
             this.add(kslider);
             this.add(new JLabel("Adjust mass of bob"));
             this.add(wslider);
             this.add(new JLabel("Adjust deviation (degrees)"));
             this.add(tslider);
             this.add(new JLabel("Adjust angular velocity (rad/sec)"));
             this.add(oslider);
             this.add(new JLabel("Adjust time interval of updation"));
             this.add(tislider);
             this.add(new JLabel("Adjust damping factor"));
             this.add(dfslider);
             this.add(new JLabel("Adjust length"));
             this.add(lslider);
             
             JButton startButton = new JButton("Start");
             startButton.setActionCommand("start");
             startButton.addActionListener(this);
             
             JButton stopButton = new JButton("Pause");
             stopButton.setActionCommand("stop");
             stopButton.addActionListener(this);
             
             this.add(startButton);
             this.add(stopButton);
             this.setLayout(new GridLayout(8, 1, 5, 20));
             this.setBorder(new EmptyBorder(20,20,20,20));
             
         }
 
         @Override
         public void stateChanged(ChangeEvent e) 
         {
             JSlider slider = (JSlider) e.getSource();
             double value = slider.getValue();
             
             switch (slider.getName())
             {
                 case "kslider" : 
                     k = (float) (value * 8000);
                     break;
                     
                 case "wslider" :
                     mass = (float) (value) * 10;
                     break;
                     
                 case "tslider" :
                     theta = value * 3.14 / 180;
                     paintPanel.repaint();
                     break;
                     
                 case "oslider" :
                     omega = value;
                     paintPanel.repaint();
                     break;
                     
                 case "tislider" :
                     dt = (float) (value / 10000);
                     break;
                     
                 case "dfslider" :
                     df = (float) (value / 100);
                     break;
                    
                 case "lslider" :
                     rodlength = (float) (value * 3);
                     paintPanel.repaint();
                     break;
             }
         }
 
         @Override
         public void actionPerformed(ActionEvent e) 
         {
             if ("start".equals(e.getActionCommand()))
             {                
                 t = new Thread(sim);
                 t.start();
                 threadStop = false;                
             }
             
             if ("stop".equals(e.getActionCommand()))
             {
                 //threadStop = true;
                 timer.cancel(); 
                 t.stop();                               
             }
         }
         
     }
     
     public PendulumAnimation()
     {
             frame = new JFrame("Inverted Pendulum");
             frame.setVisible(true);
             ai = new InvertedPendulumAI("gauss");
             
             mass = 10f;
             I = mass * (rodlength) * (rodlength);
             
             // init params
             theta = 0;
             omega = 0;
             threadStop = false;
             
             frame.setSize(1080, 500);
             frame.setLocation(200, 200);
             frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
             paintPanel = new PaintPanel();
             controlPanel = new ControlPanel();
             
             frame.setLayout(new GridLayout());
             frame.add(paintPanel);
             frame.add(controlPanel);
             
             frame.validate();                    
         
     }
     
     public void run()
     {
             t = 0.0f;       
             time = 0.0f;
             
             // schedule timer
             timer = new Timer();
             UpdateTimeTask timerTask = new UpdateTimeTask();
             timer.schedule(timerTask, 0, 10);
 
            while (Math.abs(theta) > 1e-4 || Math.abs(omega) > 1e-3)
             {                                               
 
                 // get current
                 double current = ai.getCurrent(theta, omega);    
                 if (Math.abs(theta) > 1)
                 {
                     current /= Math.pow(100, Math.abs(theta) - 1);
                 }
                 double alpha = (k * current + 
                         mass * g * rodlength * Math.sin(theta)) / I; 
 
                 // damping
                 if (omega >= 0) 
                     alpha -= df * omega * omega;
                 else
                     alpha += df * omega * omega;
 
                 // update theta, omega
                 theta += omega * dt + 0.5 * alpha * dt * dt;
                 omega += alpha * dt;
                 t += dt;
 
                 // normalize theta
                 if (theta > 3.14) {
                     theta -= 2 * 3.14; 
                 } else if (theta < -3.14) {
                     theta += 2 * 3.14;
                 }
                 
                 // force the panel to repaint itself
                 paintPanel.repaint();
 
                 // wait 100ms
                 try {  
                     Thread.sleep((long) dt * 100);
                 } catch (Exception ex) {} 
                                                              
 
             }               
     }
     
    
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         
         PendulumAnimation anime = new PendulumAnimation();
         //InvertedPendulumAI ai = new InvertedPendulumAI("gauss");
         //ai.getCurrent(0, 0);
         
     }
     
 }
