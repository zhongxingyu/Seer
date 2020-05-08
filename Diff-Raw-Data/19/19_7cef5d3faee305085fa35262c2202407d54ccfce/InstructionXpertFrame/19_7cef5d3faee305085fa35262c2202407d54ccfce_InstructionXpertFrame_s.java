 package gui;
 
 import java.awt.Color;
 import java.awt.TextArea;
 import java.awt.event.ActionEvent;
 import java.awt.BorderLayout;
 import java.awt.Font;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import java.net.URL;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JScrollPane;
 import java.awt.event.WindowListener;
 import java.awt.event.WindowEvent;
 
 import controller.Instruction;
 import controller.InstructionList;
 import controller.IntStringMap;
 import controller.InstructionTxtWriter;
 import controller.InstructionTxtParser;
 
 public class InstructionXpertFrame extends JPanel implements WindowListener {
 
     private JFrame frame_;
     private JButton submit_button_;
     private JTextArea ta_;
     private TUGLabyrinth top_;
     private InstructionList instruct_list_;
     private ControllerGuiCommunicator controller_;
     private IntStringMap mark_array_, state_array_;
     private URL url_;
 
     private Color numberColor;
 
     InstructionXpertFrame(URL url, IntStringMap mark_array,
             IntStringMap state_array, InstructionList instruction_list,
             ControllerGuiCommunicator controller) {
         controller_ = controller;
         state_array_ = state_array;
         mark_array_ = mark_array;
         url_ = url;
         ta_ = new JTextArea();
         Font f = new Font("Monospaced", Font.PLAIN, 12);
         ta_.setFont(f);
         instruct_list_ = instruction_list;
 
         submit_button_ = new JButton();
         submit_button_.setBounds(5, 5, 40, 500);
         submit_button_.setBackground(Color.ORANGE);
         submit_button_.setForeground(new Color(0, 0, 0));
         submit_button_.setText("Regelsatz übernehmen");
         submit_button_.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 performAction();
             }
         });
         submit_button_.setBackground(Color.ORANGE);
         frame_ = new JFrame("Marvin-10 Experten-Modus");
         frame_.getContentPane().setLayout(new BorderLayout());
         frame_.getContentPane().add(submit_button_, BorderLayout.NORTH);
 
         InstructionTxtWriter writer_ = new InstructionTxtWriter(url_);
         writer_.setMarkArray(mark_array_);
         writer_.setStateArray(state_array_);
         String text = writer_.writeText(instruction_list);
         ta_.setText(text);
         JScrollPane scrollpane_ = new JScrollPane(ta_);
 
         LineNumberBorder lnb = new LineNumberBorder(java.awt.Color.red);// check
                                                                         // this
         ta_.setBorder(lnb);
         frame_.getContentPane().add(scrollpane_, BorderLayout.CENTER);
         frame_.addWindowListener(this);
         frame_.setSize(300, 600);
         frame_.setVisible(true);
         this.repaint();
     }
 
     private void performAction() {
         InstructionList instructions = new InstructionList();
         String code = ta_.getText();
         InstructionTxtParser parser_ = new InstructionTxtParser(url_);
         parser_.setMarkArray(mark_array_);
         parser_.setStateArray(state_array_);
         instructions = parser_.parseText(code);
         controller_.xpertRulesUpdated(instructions);
     }
 
     public void windowClosing(WindowEvent e) {
         controller_.xpertRulesClosed();
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
