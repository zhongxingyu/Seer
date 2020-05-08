 /**
  *
  * @author arjend
  */
 package memory;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.ArrayList;
 
 
 public class Start extends JFrame
     implements ActionListener {
 
     private JButton btn_add_person, btn_start_test;
     private CreatePersonGui createperson;
     private StartTest starttest;
     private ArrayList<String[]> persons = new ArrayList<String[]>();
 
     public static void main(String[] args) {
         Start frame = new Start();
         frame.setSize(400, 300);
         frame.createGUI();
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
     }
 
     private void createGUI() {
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         Container window = getContentPane();
         window.setLayout(new FlowLayout());
 
         btn_start_test = new JButton("Start Test");
         window.add(btn_start_test);
         btn_start_test.addActionListener(this);
 
         btn_add_person= new JButton("Add Person");
         window.add(btn_add_person);
         btn_add_person.addActionListener(this);
 
     }
 
     @Override
     public void actionPerformed(ActionEvent event) {
         final String command = event.getActionCommand();
         if (command.equals("Add Person")){
             this.createperson  = new CreatePersonGui(this, "Add Person");
             this.createperson.setVisible(true);
         }
         else if (command.equals("Start Test")){
             this.starttest = new StartTest(this, "Start Test");
             this.starttest.setVisible(true);
         }
     }
 }
