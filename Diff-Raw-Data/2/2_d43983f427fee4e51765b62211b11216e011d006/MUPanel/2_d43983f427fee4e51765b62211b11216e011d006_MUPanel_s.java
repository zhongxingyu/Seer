 /* CSC 370 Project 4
  * Producer-Consumer Project
  * 
  * description available at:
  * http://raider.mountunion.edu/csc/CSC370/spring2013/projects/projectProducerConsumer.html
  * 
  * written by Matt Amabeli
  * 
  * MetroButtons created by Kyle Dreger
  * 
  * ----------------------
  * WORK LOG
  * ===================
  * Feb 27 2013  |  7p.m. - 11p.m. | created the framework for the project, completed the checklist items for Friday
  * Feb 28 2013  |  8p.m. - 10p.m. | tweaked a few classes, cleaned things up, began working on Buffer class
  * March 2 2013 |  9a.m. - 9:30a.m.| added bufferTest class, fixed buffer.toString(), added calls to buffer in MUPanel to display info
  * March 2 2013 |  8p.m. - 9:30p.m. | added content to the consumer class & code to call it in MUPanel
  * March 4 2013 |  7p.m. - 11p.m. | added Threads to the Producer and Consumer classes
  * March 5 2013 |  6p.m. - 7:30p.m. | began work on the run methods
  * March 6 2013 |  8p.m. - 10p.m. | run in consumer now works, still tweaking producer
  * March 19 2013 | 8p.m. - 11p.m. | Fixed a semaphore problem in the run of producer, the program now works
  *                                - no longer has duplicate of thread.start(), now called in constructor rather then button
  *                                - added comments
  * 
  */
 
 /*
  * NOTES:
  *  -When i attempted to run this on a non computer lab comuter it would not work.
  *  -The MetroButtons are a creation of Kyle Dreger, and they seem like a fun addition. They are cited more heavily in the MetroButton class.
  *  -Buttons do not fade to be un-clickable after the buffer is full/empty, but if you click them nothing will happen at that point.
  *      -(except of course for the extra wait/consume thing we were supposed to throw in)
  */
 
 
 
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.concurrent.Semaphore;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 
 public class MUPanel extends JPanel implements ActionListener{
 private String windowTitle = "Deadlock Avoidance Program";
     private JLabel demoLabel;
     private TextArea input, output;
     private TextField command;
     private JButton demoJButton;
     private OpSystem opSystem;
     
     public MUPanel() {
         setLayout(null);
         setPreferredSize(new Dimension(800, 600));
         setName(windowTitle);
 //        addWindowListener(this);
         setBackground(Color.decode("#fafafa"));
 
         // Remove the following lines to clear the JPanel
         JLabel defaultLabel = new JLabel("Enter a command ...");
         defaultLabel.setBounds(45, 25, 325, 50);
         defaultLabel.setFont(new Font("Georgia", Font.PLAIN, 35));
         this.add(defaultLabel);
         
         command = new TextField();
         command.setBounds(50, 80, 300, 30);
         command.addActionListener(this);
         add(command);
         
         input=new TextArea();
         input.setBounds(50, 150, 300, 400);
         input.setText("A B C\n"
                 + "332\n"
                 + "010753\n"
                 + "200322\n"
                 + "302902\n"
                 + "211222\n"
                 + "002433");
         add(input);
         
         output=new TextArea();
         output.setBounds(400, 50, 350, 500);        
         output.setText("There are 5 possible commands:\n\n"
                 + "L -- Load\n"
                 + "S -- Safety Check\n"
                 + "R -- Request Resource\n"
                 + "A -- Add Resource\n"
                 + "D -- Delete Resource\n\n");
         add(output);
         
        
     }
 
     // Using an IF statement to determine the source of the event
     // allows us to have cleaner code when you start adding other
     // elements to the panel. 
     @Override
     public void actionPerformed(ActionEvent event) {
         Object source = event.getSource();
         
         //event listener for the input command text field
         if (source.equals(command)) {
             
             if(command.getText().toUpperCase().equals("L")){
                 opSystem = new OpSystem(output);
                 output.append("L\n\n");
                 command.setText("");
                 opSystem.parseAndStoreData(input.getText());
             }
             else if(command.getText().toUpperCase().equals("S")){
                 output.append("S\n\n\n");
                 command.setText("");
             }
             else if(command.getText().toUpperCase().startsWith("R")){
                 output.append("R\n\n\n");
                 command.setText("");
             }
             else if(command.getText().toUpperCase().startsWith("A")){
                 output.append("A\n\n\n");
                 command.setText("");
             }
             else if(command.getText().toUpperCase().startsWith("D")){
                 output.append("D\n\n\n");
                 command.setText("");
             }
             else{
                output.append("Please enter a valid command...\n\n\n");
                 command.setText("");
             }
         }
     }
     /***********************************************
      * Do NOT change or delete anything below here!
      ***********************************************/
     public void frame()
     {
         JFrame f = new JFrame(getName());
         f.setContentPane(this);
         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.pack();
         f.setVisible(true);
     }
 
     public static void main(String args[]){new MUPanel().frame();}
 
 } // end of class MUPanel
