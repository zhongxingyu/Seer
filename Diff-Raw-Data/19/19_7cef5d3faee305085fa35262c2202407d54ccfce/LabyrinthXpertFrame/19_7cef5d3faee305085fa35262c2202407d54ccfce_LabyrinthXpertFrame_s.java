 package gui;
 
 import java.awt.Color;
 import java.awt.event.WindowListener;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.BorderLayout;
 import java.awt.Font;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import java.net.URL;
 import java.net.MalformedURLException;
 
 import java.io.ByteArrayInputStream;
 import org.xml.sax.InputSource;
 
 import javax.swing.JTextArea;
 import javax.swing.JScrollPane;
 
 import controller.LabyrinthXpertMode;
 import controller.LabyrinthField;
 
 public class LabyrinthXpertFrame extends JPanel implements WindowListener {
 
     static JFrame frame_;
     private JButton submit_button_;
     private JTextArea ta_;
     private ControlSimulation control_simulation_;
 
     LabyrinthXpertFrame(String labyrinth, ControlSimulation control_simulation) {
         control_simulation_ = control_simulation;
         ta_ = new JTextArea();
         Font f = new Font("Monospaced", Font.PLAIN, 12);
         ta_.setFont(f);
 
         submit_button_ = new JButton();
         submit_button_.setBounds(5, 5, 89, 40);
         submit_button_.setBackground(Color.ORANGE);
         submit_button_.setForeground(new Color(0, 0, 0));
         submit_button_.setText("Labyrinth übernehmen");
         submit_button_.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 String labyrinth = ta_.getText();
                 control_simulation_.xpertLabyrinth(labyrinth);
             }
         });
 
         frame_ = new JFrame("Marvin-10 Experten-Modus");
         frame_.getContentPane().setLayout(new BorderLayout());
         frame_.getContentPane().add(submit_button_, BorderLayout.NORTH);
         // frame_.getContentPane().add(ta_, BorderLayout.CENTER);
 
         ta_.setText(labyrinth);
 
         JScrollPane scrollpane_ = new JScrollPane(ta_);
         frame_.getContentPane().add(scrollpane_, BorderLayout.CENTER);
         LineNumberBorder lnb = new LineNumberBorder(java.awt.Color.red);// check
                                                                         // this
         ta_.setBorder(lnb);
 
         frame_.getContentPane().add(scrollpane_, BorderLayout.CENTER);
         frame_.addWindowListener(this);
         frame_.setSize(600, 800);
         frame_.setVisible(true);
         this.repaint();
     }
 
     public void windowClosing(WindowEvent e) {
         control_simulation_.xpertLabyrinthClosed();
     }
 
     public void windowClosed(WindowEvent e) {
     }
 
     public void windowOpened(WindowEvent e) {
     }
 
     public void windowIconified(WindowEvent e) {
     }
 
     public void windowDeiconified(WindowEvent e) {
     }
 
     public void windowActivated(WindowEvent e) {
     }
 
     public void windowDeactivated(WindowEvent e) {
     }
 
 }
