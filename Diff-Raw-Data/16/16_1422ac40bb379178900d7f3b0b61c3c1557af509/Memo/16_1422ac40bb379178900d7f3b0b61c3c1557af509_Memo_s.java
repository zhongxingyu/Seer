 /**
  * Name: Christina Black, Kelly Hutchison, Nicole Hart
  * Date: 11/6/2012
  * Section: 002
  * Project: Calendar
  */
 
 //package calendar;
 
 import java.awt.BorderLayout;
 import java.awt.event.*;
 import java.io.IOException;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.*;
 
 /**
  * An event holds the date, time, and location of a user's
  * desired Event
  */
 
 public class Memo extends JPanel implements ActionListener{
     
     
     private JPanel days = new JPanel();
     private JButton okayButton;
     private JFrame frame = new JFrame();
     private JPanel textField = new JPanel();
     private JTextArea message;
     private JLabel memoLabel;
    
     private String[][] monthMemos;
     private int dayIndex;
     private int eventIndex;
         
     //Default Constructor
     /**
      *
      * @param memoEvents
      * @param dayIndex
      * @param eventIndex
      * @throws LineUnavailableException
      * @throws IOException
      */
    public Memo(String[][] monthMemos, int dayIndex, int eventIndex) throws LineUnavailableException, IOException 
     {
         super();
         textField.setBorder(BorderFactory.createEmptyBorder(20,0,0,0));
         days.setBorder(BorderFactory.createEmptyBorder(10,5,10,5));
         BorderLayout layout = new BorderLayout(100, 200);
         setLayout(layout);
         
        this.monthMemos = monthMemos;
        this.dayIndex = dayIndex;
        this.eventIndex = eventIndex;
        
         memoLabel = new JLabel("Memo: ");
         frame.add(memoLabel, BorderLayout.NORTH);
         
         message = new JTextArea();
         message.setSize(100, 300);
         message.setText("Enter your memo here");
         message.selectAll();
         textField.add(message);
         frame.add(textField, BorderLayout.NORTH);
        
         okayButton = new JButton("OKAY");
         okayButton.addActionListener(this);
         frame.add(okayButton);        
         
         frame.setResizable(false);
         frame.setSize(400, 300);
         frame.setLocation(400,250);
         frame.setVisible(true);
     }
 
     //Action listener
     @Override
     public void actionPerformed(ActionEvent e){
         if (e.getSource() == okayButton) {
             if ("".equals(message.getText()))
             {
                 JOptionPane.showMessageDialog(null, "Please write a memo");
             }
             else
             {
             frame.setVisible(false);
             frame.dispose();
             
             JOptionPane.showMessageDialog(null, toString()); 
             
             // Store the alarm
             monthMemos[dayIndex][eventIndex] = toString();
             }
         }
     }
     
     //To string
     @Override
     public String toString() {
       return("MEMO: " + message.getText());
     }
 
    
}
