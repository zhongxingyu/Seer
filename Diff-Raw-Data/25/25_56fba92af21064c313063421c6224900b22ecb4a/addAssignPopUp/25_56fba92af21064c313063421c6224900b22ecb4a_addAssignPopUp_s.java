 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.event.*;
 import java.util.*;
 
 public class addAssignPopUp extends JFrame
 {
     private JTextField name, due;
     private JLabel assignName, dueDate;
     private JButton submit, cancel;
     private JPanel box, inside;
     private String aName, dDate;
    private boolean clicked;
     
     public addAssignPopUp()
     {
         box = new JPanel();
         inside = new JPanel();
         
         name = new JTextField(24);
         name.addActionListener(new SubmitListener());
         due = new JTextField(24);
         due.addActionListener(new SubmitListener());
         
         assignName = new JLabel("Name of Assignment");
         dueDate = new JLabel("Due Date");
         
         submit = new JButton("Submit");
         submit.addActionListener(new SubmitListener());
         
         cancel = new JButton("cancel");
         cancel.addActionListener(new CancelListener());
         
         box.setLayout(new GridLayout(1,2));
         box.add(submit);
         box.add(cancel);
         
         inside.setLayout(new GridLayout(5,1));
         inside.add(assignName);
         inside.add(name);
         inside.add(dueDate);
         inside.add(due);
         inside.add(box);
         this.add(inside);
         setSize(300,300);
         box.setVisible(true);
     }
     
 public String getName()
 {
     return aName;
 }
 
 public String getDate()
 {
     return dDate;
 }
 
 public boolean submitClicked()
 {
    return clicked;
 }
 
     
 private class SubmitListener implements ActionListener
     {
         public void actionPerformed(ActionEvent e)
         {
             aName = name.getText();
             dDate = due.getText();
            clicked = true;
             setVisible(false);
         }
     }
     
 private class CancelListener implements ActionListener
     {
         public void actionPerformed(ActionEvent e)
         {
             setVisible(false);
         }
     }
     
     
 }
     
 
