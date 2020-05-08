 package pomodoro.listener;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JOptionPane;
 import pomodoro.frame.Frame;
 
 /**
  * Button class handles button actions
  * @author Michael
  */
 public class Button implements ActionListener
 {
     private Frame frame;
     
     /**
      * Button constructor
      * @param frame 
      */
     public Button(Frame frame)
     {
         this.frame = frame;
     } //end of Button constructor
     
     /**
      * actionPerformed method
      * @param e 
      */
     @Override
     public void actionPerformed(ActionEvent e)
     {
         System.out.println(e.getActionCommand().toString());
         
         if(e.getActionCommand().toString().equalsIgnoreCase("start"))
         {
             frame.timer = true;
         }
         if(e.getActionCommand().toString().equalsIgnoreCase("stop"))
         {
             frame.timer = false;
         }
         if(e.getActionCommand().toString().equalsIgnoreCase("work"))
         {
             frame.working = true;
             frame.timer = false;
             frame.minutes = 25;
             frame.seconds = 0;
         }
         if(e.getActionCommand().toString().equalsIgnoreCase("rest"))
         {
             frame.working = false;
             frame.timer = false;
             frame.minutes = 5;
             frame.seconds = 0;
         }
         if(e.getActionCommand().toString().equalsIgnoreCase("add"))
         {
             if(!(frame.taskList.size() == 3))
             {
                 if(frame.taskOne.getText().equals(""))
                 {
                     frame.taskList.add(0, frame.taskInput.getText());
                     frame.taskOne.setText(frame.taskList.get(0));
                    System.out.println(frame.taskList.get(0));
                 }
                 else if(frame.taskTwo.getText().equals(""))
                 {
                     frame.taskList.add(1, frame.taskInput.getText());
                     frame.taskTwo.setText(frame.taskList.get(1));
                    System.out.println(frame.taskList.get(1));
                 }
                 else if(frame.taskThree.getText().equals(""))
                 {
                     frame.taskList.add(2, frame.taskInput.getText());
                     frame.taskThree.setText(frame.taskList.get(2));
                    System.out.println(frame.taskList.get(2));
                 }
             }
             else if(frame.taskList.size() == 3)
             {
                 JOptionPane.showMessageDialog(null, "Tasks are full, complete/remove a task!");
             }
         }
         if(e.getActionCommand().toString().equalsIgnoreCase("complete"))
         {
             if(frame.taskList.size() >= 1)
             {
                 if(e.getSource().equals(frame.completeTaskOne) && 
                         !frame.taskOne.getText().equals(""))
                 {
                     frame.taskList.remove(0);
                     frame.taskOne.setText("");
                 }
                 else if(e.getSource().equals(frame.completeTaskTwo) && 
                         !frame.taskTwo.getText().equals(""))
                 {
                    frame.taskList.remove(0);
                     frame.taskTwo.setText("");
                 }
                 else if(e.getSource().equals(frame.completeTaskThree) && 
                         !frame.taskThree.getText().equals(""))
                 {
                    frame.taskList.remove(0);
                     frame.taskThree.setText("");
                 }
                 else
                 {
                    JOptionPane.showMessageDialog(null, "Task empty, add a task!");
                 }
             }
             else if(frame.taskList.size() == 0)
             {
                 frame.taskInfo = "None";
                 JOptionPane.showMessageDialog(null, "Tasks are empty, add a task!");
             }
         }
         if(e.getActionCommand().toString().equalsIgnoreCase("set"))
         {
             if(frame.taskList.size() >= 1)
             {
                 if(e.getSource().equals(frame.setTaskOne) && 
                         !frame.taskOne.getText().equals(""))
                 {
                     frame.taskInfo = frame.taskOne.getText();
                 }
                 else if(e.getSource().equals(frame.setTaskTwo) && 
                         !frame.taskTwo.getText().equals(""))
                 {
                     frame.taskInfo = frame.taskTwo.getText();
                 }
                 else if(e.getSource().equals(frame.setTaskThree) && 
                         !frame.taskThree.getText().equals(""))
                 {
                     frame.taskInfo = frame.taskThree.getText();
                 }
                 else
                 {
                     JOptionPane.showMessageDialog(null, "Task is empty, add a task!");
                 }
             }
             else if(frame.taskList.size() == 0)
             {
                 frame.taskInfo = "None";
                 JOptionPane.showMessageDialog(null, "Tasks are empty, add a task!");
             }
         }
         if(e.getActionCommand().toString().equalsIgnoreCase("remove"))
         {
             if(frame.taskList.size() >= 1)
             {
                 if(e.getSource().equals(frame.removeTaskOne) && 
                         !frame.taskOne.getText().equals(""))
                 {
                     frame.taskList.remove(0);
                     frame.taskOne.setText("");
                 }
                 else if(e.getSource().equals(frame.removeTaskTwo) && 
                         !frame.taskTwo.getText().equals(""))
                 {
                    frame.taskList.remove(0);
                     frame.taskTwo.setText("");
                 }
                 else if(e.getSource().equals(frame.removeTaskThree) && 
                         !frame.taskThree.getText().equals(""))
                 {
                    frame.taskList.remove(0);
                     frame.taskThree.setText("");
                 }
                 else
                 {
                     JOptionPane.showMessageDialog(null, "Task is empty, add a task!");
                 }
             }
             else if(frame.taskList.size() == 0)
             {
                 frame.taskInfo = "None";
                 JOptionPane.showMessageDialog(null, "Tasks are empty, add a task!");
             }
         }   
     } //end of actionPerformed method
     
 } //end of Button class
