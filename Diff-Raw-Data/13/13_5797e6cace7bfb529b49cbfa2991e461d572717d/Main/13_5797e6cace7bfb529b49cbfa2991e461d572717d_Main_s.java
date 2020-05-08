 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 import java.awt.*;
 import java.io.*;
 
 public class Main extends JFrame {
     private int h = 1000;
    private int p = 49;
 
     private double a = 0.1;
    private double b = 0.2;
 
    private double alpha = 0.002;
    private double beta  = 0.0002;
 
     private ArrayList<DataPair> data = new ArrayList<DataPair>();
 
     private Grapher graph           = new Grapher(data);
     private PhaseGrapher phaseGraph = new PhaseGrapher(data);
 
     JTextField hField;
     JTextField pField;
     JTextField aField;
     JTextField bField;
     JTextField alphaField;
     JTextField betaField;
 
     JButton runButton;
 
     public static void main(String[] args) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 new Main();
             }
         });
     }
 
     public Main() {
         setTitle("Lotka-Volterra");
 
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setLayout(new BorderLayout());
 
         JLabel hLabel     = new JLabel("h");
         JLabel pLabel     = new JLabel("p");
         JLabel aLabel     = new JLabel("a");
         JLabel bLabel     = new JLabel("b");
         JLabel alphaLabel = new JLabel("alpha");
         JLabel betaLabel  = new JLabel("beta");
 
         hField = new JTextField();
         hField.setText("" + h);
 
         pField = new JTextField();
         pField.setText("" + p);
 
         aField = new JTextField();
         aField.setText("" + a);
 
         bField = new JTextField();
         bField.setText("" + b);
 
         alphaField = new JTextField();
         alphaField.setText("" + alpha);
 
         betaField = new JTextField();
         betaField.setText("" + beta);
 
         runButton = new JButton("Run");
         runButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 runButtonClicked();
             }
         });
 
         graph.setPreferredSize(new Dimension(800, 400));
         phaseGraph.setPreferredSize(new Dimension(800, 400));
 
         JPanel panel = new JPanel();
         panel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
         panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
 
         panel.add(hLabel);
         panel.add(hField);
         panel.add(Box.createRigidArea(new Dimension(10, 0)));
         panel.add(pLabel);
         panel.add(pField);
         panel.add(Box.createRigidArea(new Dimension(10, 0)));
         panel.add(aLabel);
         panel.add(aField);
         panel.add(Box.createRigidArea(new Dimension(10, 0)));
         panel.add(bLabel);
         panel.add(bField);
         panel.add(Box.createRigidArea(new Dimension(10, 0)));
         panel.add(alphaLabel);
         panel.add(alphaField);
         panel.add(Box.createRigidArea(new Dimension(10, 0)));
         panel.add(betaLabel);
         panel.add(betaField);
         panel.add(Box.createRigidArea(new Dimension(10, 0)));
         panel.add(runButton);
 
         add(panel, BorderLayout.NORTH);
         add(graph, BorderLayout.CENTER);
         add(phaseGraph, BorderLayout.SOUTH);
 
         pack();
         setVisible(true);
     }
 
     private void step() {
         double dH = a * h - alpha * h * p;
         double dP = beta * h * p - b * p;
 
         h += dH;
         p += dP;
     }
 
     private void runButtonClicked() {
         new Thread() {
             public void run() {
                 runHelper();
             }
         }.start();
     }
 
     private void runHelper() {
         h     = Integer.parseInt(hField.getText());
         p     = Integer.parseInt(pField.getText());
         a     = Double.parseDouble(aField.getText());
         b     = Double.parseDouble(bField.getText());
         alpha = Double.parseDouble(alphaField.getText());
         beta  = Double.parseDouble(betaField.getText());
 
         data = new ArrayList<DataPair>();
         data.add(new DataPair(h, p));
 
        while (h > 0 && p > 0 && data.size() < 1000000) {
             step();
             data.add(new DataPair(h, p));
         }
 
         graph.setData(data);
         graph.repaint();
 
         phaseGraph.setData(data);
         phaseGraph.repaint();
     }
 }
