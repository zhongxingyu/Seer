 /**
 * @(#)Call_Centre_Training.java
 *
 * Call Centre Training Application
 *
 * @authors: Robbie Aftab, Ash Ellis, Steve Glasspool, Matt Kennedy
 */
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.sql.SQLException;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.sql.PreparedStatement;
 import java.sql.Blob;
 import java.io.IOException;
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 
 public class CallLogView
 {
 	static JFrame frame;
 	static long start, now;
 	static Date startDate;
 	static String startDateString;
 	static String endDateString;
 	static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
 	static JButton startButton, endButton, logCallButton;
 	static JTextField callTakerTextField;
 	static JTextArea commentsTextArea;
 	static JScrollPane commentsScrollPane;
 	static JLabel callTakerLbl;
 	static CommentsFocusListener commentsFocusListener;
 	static CallLogListener callLogListener;
 	static StartCallListener startCallListener;
 	static EndCallListener endCallListener;
 	View v;	
 	
 	public CallLogView()
 	{		
 		callLogListener = new CallLogListener();
 		startCallListener = new StartCallListener();
 		endCallListener = new EndCallListener();
 		commentsFocusListener = new CommentsFocusListener();
 	}
 	
 	
 	class CallLogListener implements ActionListener
     {
 	    public void actionPerformed(ActionEvent e)
 	    {	     
 		
 			String commentsIn = commentsTextArea.getText();
 			String callTakerIn = callTakerTextField.getText();
 			String elapsedTime = "TIME_ERROR";
 			try
 			{
 				elapsedTime = String.valueOf(elapsedTime());
 			}catch(NumberFormatException ex)
 			{
 				ex.printStackTrace();
 			}
 			
 			
 			
 	    	//Database insert
 	    	Connection conn = Call_Centre_Training.getConnection();
 			PreparedStatement stmt = null;
 			ResultSet rs = null;
 			
 			try
 			{
 			
 				stmt = conn.prepareStatement("INSERT INTO call_log (start_time, end_time, call_length, comments, call_taker) VALUES (?,?,?,?,?)");
 				stmt.setString(1, startDateString);
 				stmt.setString(2, endDateString);
 				stmt.setString(3, elapsedTime);
 				stmt.setString(4, commentsIn);
 				stmt.setString(5, callTakerIn);
 				stmt.executeUpdate();
 				conn.close();
 				
 				JOptionPane.showMessageDialog(null,"Call Logged!"); 
 			}
 			catch(SQLException ex)
 			{
 				ex.printStackTrace();
 			}
 			startButton.setEnabled(true);
 			logCallButton.setEnabled(false);
 			
 			commentsTextArea.setText("Enter comments here");
 			callTakerTextField.setText("");
     	}                   
 	}
 
 	 
 	public static void addComponentsToPane(Container pane)
     {  
     	frame.setLocation(50,100);
     	    	  	  	
 	    pane.setLayout(new GridBagLayout());
 	    GridBagConstraints c = new GridBagConstraints();
 	    
     	startButton = new JButton("Start call");
     	c.ipady = 20;
 		c.weightx = 0.5;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		pane.add(startButton, c);
 		startButton.addActionListener(startCallListener);
 		
 		endButton = new JButton("End call");
 		c.ipady = 20;
 		c.weightx = 0.5;
 		c.gridx = 1;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		endButton.setEnabled(false);
 		pane.add(endButton, c);
 		endButton.addActionListener(endCallListener);
 				
 		commentsTextArea = new JTextArea("Enter comments here", 10, 40);
 		commentsTextArea.setLineWrap(true);
 		commentsTextArea.setWrapStyleWord(true);
 
 		commentsScrollPane = new JScrollPane(commentsTextArea);
 		commentsScrollPane.setVerticalScrollBarPolicy(
         JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		//complaintScrollPane.setPreferredSize(new Dimension(400, 200));
 		//c.fill = GridBagConstraints.VERTICAL;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.0;
 		//c.ipady = 200;
 		//c.ipadx = 200;
 		c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(20,0,10,0);
 		pane.add(commentsScrollPane, c);
 		commentsTextArea.addFocusListener(commentsFocusListener);
 		
 		callTakerLbl = new JLabel("Name: ");
 		c.ipady = 20;
 		c.weightx = 1;
 		c.gridx = 0;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,10,0,0);
 		pane.add(callTakerLbl, c);
 		
 		callTakerTextField = new JTextField(10);
 		c.ipady = 20;
 		c.weightx = 1;
 		c.gridx = 1;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,-130,0,0);
 		callTakerTextField.setEnabled(false);
 		pane.add(callTakerTextField, c);
 		
 		logCallButton = new JButton("Log Call");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 0;
 		c.gridy = 4;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,0,0,0);
 		pane.add(logCallButton, c);
 		logCallButton.setEnabled(false);
 		logCallButton.addActionListener(callLogListener);
     }
     
     
     public static void createAndShowGUI()
     {
         //Create and set up the window. Set instantiation parameters.
         frame = new JFrame("Call Logging");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(300,500);
         //frame.setLocation(50,50);
         	
     	frame.setResizable(false);
 		frame.setLocationRelativeTo(null);
 		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);			
  
         //Set up the content pane.
         addComponentsToPane(frame.getContentPane());
  
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
     
     
     	     
      
      
      class StartCallListener implements ActionListener
      {
 	     public void actionPerformed(ActionEvent e)
 	     {
 	     	v = new View();
 	     	v.createAndShowGUI();
 			startTimer();
 			startDate = new Date();
 			startDateString = dateFormat.format(startDate);
 	     	
 	     	endButton.setEnabled(true);
 	     	startButton.setEnabled(false);
 	     }
      }
      
      class EndCallListener implements ActionListener
      {
 	     public void actionPerformed(ActionEvent e)
 	     {
 	     	Date endDate = new Date();
 			endDateString = dateFormat.format(endDate);
 			
 			JOptionPane.showMessageDialog(null,"Call Duration: " + elapsedTime() + " seconds.");
 	     	
 	     	callTakerTextField.setEnabled(true);
 	     	commentsTextArea.setEnabled(true);
 	     	logCallButton.setEnabled(true);
 	     	endButton.setEnabled(false);
	     	v.frame.dispose();
	     	
 	     }
      }
      
     public void startTimer()
     {   
    		start = System.currentTimeMillis();
     }
     
     public long elapsedTime()
     {
        now = System.currentTimeMillis();
        return (now - start) / 1000;
     }
     
     class CommentsFocusListener implements FocusListener
     {
      	public void focusGained(FocusEvent e)
      	{
 			commentsTextArea.setText("");
      	}
         public void focusLost(FocusEvent e) 
         {
        		 
     	}
     }
 }
