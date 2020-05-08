 package screencapture;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 
 /**
  * @author paul
  */
 public class ScreenCaptureGui extends JFrame implements ActionListener 
 {
     private String title;
     // Threads
     private MainThread mt;
     // Buttons
     private JButton record;
     
     /**
      * Complete Constructor
      * @param title Takes base title for the gui. 
      */
     public ScreenCaptureGui(String title)
     {
         super(title);
         this.title = title;
         this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         this.setSize(300, 300);
         this.setResizable(false);
         this.record = new JButton("RECORD");
         this.record.addActionListener(this);
         this.add(record);
         this.mt = new MainThread();
     }
     
     /**
      * Checks for what actions are performed in the gui.
      * @param e 
      */
     @Override
     public void actionPerformed(ActionEvent e)
     {
         // If the record button is pressed
         if(e.getSource() == record)
         {
             if(mt == null || !mt.isRunning())
             {
                 mt = new MainThread();
                 this.setTitle(title + " - Recording");
                 record.setText("STOP");
                 mt.start();
                // TODO: DEBIG prints out Starting MainThread
                 System.out.println("Starting the MainThread.");
             }
             else
             {
                 record.setText("WRITING DATA");
                 record.setEnabled(false);
                 // TODO: DEBUG prints out that we are killing the main thread.
                 System.out.println("Killing Main Thread.");
                 // End the MainThread
                 mt.kill();
                 try
                 {
                     // TODO: DEBUG prints out that the main thread has been joined.
                     System.out.println("Main Thread Joined.");
                     mt.join();
                 }
                 catch(Exception ex)
                 { 
                     System.out.println("Main Thread Join Failed.");
                 }
                 // TODO: DEBUG prints out when control from the thread join is handed back to the gui.
                 System.out.println("GUI MT DONE");
                 // Once the join is completed set everything back to normal.
                 record.setEnabled(true);
                 this.setTitle(title);
                 record.setText("RECORD");
             }
         }
     }
 }
