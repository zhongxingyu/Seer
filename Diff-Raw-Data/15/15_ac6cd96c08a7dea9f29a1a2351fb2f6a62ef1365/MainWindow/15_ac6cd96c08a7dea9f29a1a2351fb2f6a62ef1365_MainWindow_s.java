 package org.jtrace.examples.swing;
 
 import java.awt.FlowLayout;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import org.jtrace.Tracer;
 import org.jtrace.ViewPlane;
 import org.jtrace.swing.TracerPanel;
 
 public class MainWindow extends JFrame {
 
   private static final long serialVersionUID = 8122517505454630633L;
 
   public MainWindow() {
     setSize(700, 650);
     setTitle("JTrace");
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
     init();
   }
 
   private void init() {
     JPanel mainPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
 
     mainPanel.add(createTracerPanel());
     mainPanel.add(createTracerPanel());
     
     add(mainPanel);
   }
 
   private JPanel createTracerPanel() {
    return new TracerPanel(new Tracer(), App.createScene(), new ViewPlane(500, 500), 500, 500);
   }
   
 }
