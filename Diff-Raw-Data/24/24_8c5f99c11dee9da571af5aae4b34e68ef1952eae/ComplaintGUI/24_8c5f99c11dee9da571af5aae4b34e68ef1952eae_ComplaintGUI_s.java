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
 
 public class ComplaintGUI
 {
 	//page objects
 	View v;
 	ProductsGUI pgui;
 	JoiningGUI jgui;
 	CustDetailsGUI cdgui;
 	CustValidationGUI cvgui;
 	
 	static NavigationListener navigationListener;	
 	static FetchCustHistListener fetchCustHistListener;
 	static AddComplaintListener addComplaintListener;
 	static ComplaintFocusListener complaintFocusListener;
 	static JFrame frame;
 	static JMenuBar menuBar;
 	static JTextArea readOnlyTextArea, complaintTextArea;
 	static JTextField custIdTxt;
 	static JComboBox problemTypeCombo;
 	static JButton fetchCustHistButton, newComplButton;
 	static JScrollPane readOnlyScrollPane;
 	
 	static String[] problemTypeString = {"Product faulty", "Engineer missed call out", "Poor service", "Wants to speak to a manager", "Other"};
 	
     public ComplaintGUI()
     { 	   	
 		//navigation inner class listener
     	navigationListener = new NavigationListener();  
     	fetchCustHistListener = new FetchCustHistListener();
     	addComplaintListener = new AddComplaintListener();	
     	complaintFocusListener = new ComplaintFocusListener();
     }
     
     
     public static void addComponentsToPane(Container pane)   	
     {      	  	  	
     	// Get the default toolkit
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 
 		// Get the current screen size
 		Dimension scrnsize = toolkit.getScreenSize();
 		double width = (scrnsize.getWidth() / 2) - 150;
 		double height = (scrnsize.getHeight() / 2) - 350;
     	
     	frame.setLocation((int)width,(int)height);
     	
 		//scroll pane
 		JScrollPane complaintScrollPane;
 		
 		
 		//menus
 		JMenu fileMenu = new JMenu("File");
 		JMenu pageNav = new JMenu("Navigation");
		JMenu fileHelpMenu = new JMenu("Help");
 		
 		//menu items
 		JMenuItem fileClose = new JMenuItem("Close");
 		
 		JMenuItem navHome = new JMenuItem("Home");
 		JMenuItem navValidation = new JMenuItem("Customer Validation");
 		JMenuItem navProducts = new JMenuItem("Products");
 		JMenuItem navJoining = new JMenuItem("Joining");
 		JMenuItem navCustDetails = new JMenuItem("Customer Details");
 		
		JMenuItem helpFAQ = new JMenuItem("FAQ");
		JMenuItem helpGuide = new JMenuItem("System guide");
		JMenuItem helpSearch = new JMenuItem("Search");
 
 	    pane.setLayout(new GridBagLayout());
 	    GridBagConstraints c = new GridBagConstraints();
 	     
 	    
 		//menu bar
 		menuBar = new JMenuBar();
 		pane.add(menuBar);
 		//pane.setJMenuBar(menuBar);
     	menuBar.add(fileMenu);
     	menuBar.add(pageNav);
    	menuBar.add(fileHelpMenu);
 		
 		//file menu
     	fileMenu.add(fileClose);
     	
     	//nav menu
     	pageNav.add(navHome);
     	pageNav.add(navValidation);
     	pageNav.add(navProducts);
     	pageNav.add(navJoining);
     	pageNav.add(navCustDetails);
     	
     	fileClose.addActionListener(navigationListener);
     	navHome.addActionListener(navigationListener);
     	navValidation.addActionListener(navigationListener);
     	navProducts.addActionListener(navigationListener);
     	navJoining.addActionListener(navigationListener);
     	navCustDetails.addActionListener(navigationListener);
     	
    	//help menu
    	fileHelpMenu.add(helpFAQ);
    	fileHelpMenu.add(helpGuide);
    	fileHelpMenu.add(helpSearch); 
     	
     	
     	//add menu
     	c.ipady = 15;
 	    c.weightx = 0.5;
     	c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 0;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
     	pane.add(menuBar, c);
     	//c.insets = new Insets(0,0,0,0);
     	//menuBar.addActionListener(navigationListener); 
     	
 	 	//add buttons
     	JButton homeComplButton = new JButton("Home");
 	    c.ipady = 20;
 	    c.weightx = 0.0;
     	c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 0;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		//c.insets = new Insets(0,0,0,0);
     	pane.add(homeComplButton, c);   
     	homeComplButton.addActionListener(navigationListener); 	
     	
 
 		JLabel custIdLbl = new JLabel("Customer ID:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 2;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		//c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(custIdLbl, c);
 		//newComplButton.addActionListener(navigationListener);
 		
 		custIdTxt = new JTextField(10);
 		custIdTxt.setFont(new Font("Serif", Font.ITALIC, 16));
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 1;
 		c.gridy = 2;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
 		//c.fill = GridBagConstraints.HORIZONTAL;
     	pane.add(custIdTxt, c);
 		//newComplButton.addActionListener(navigationListener);
 
 		
 		fetchCustHistButton = new JButton("Fetch Complaint History");
 		//c.ipady = 20;
 		c.weightx = 0.0;
 		c.gridx = 2;
 		c.gridy = 2;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		//c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(fetchCustHistButton, c);
 		fetchCustHistButton.addActionListener(fetchCustHistListener);
 		
 		
 	    
 	    //add text area
 	    readOnlyTextArea = new JTextArea("Previous Complaints History", 10, 40);
 	    readOnlyTextArea.setEditable(false);
 		readOnlyTextArea.setFont(new Font("Serif", Font.ITALIC, 16));
 		readOnlyTextArea.setLineWrap(true);
 		readOnlyTextArea.setWrapStyleWord(true);
 		
 		readOnlyScrollPane = new JScrollPane(readOnlyTextArea);
 		readOnlyScrollPane.setVerticalScrollBarPolicy(
         JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		//complaintScrollPane.setPreferredSize(new Dimension(400, 200));
 		//c.fill = GridBagConstraints.VERTICAL;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.0;
 		//c.ipady = 200;
 		//c.ipadx = 200;
 		c.gridx = 0;
 		c.gridy = 3;
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,10,0);
 		pane.add(readOnlyScrollPane, c);
 		//textArea.getDocument().addDocumentListener(this);
 	    
 	    
 	    
 	    
 	    //add text area
 	    complaintTextArea = new JTextArea("Add comments here.", 10, 40);
 		complaintTextArea.setFont(new Font("Serif", Font.ITALIC, 16));
 		complaintTextArea.setLineWrap(true);
 		complaintTextArea.setWrapStyleWord(true);
 		complaintTextArea.addFocusListener(complaintFocusListener);
 		
 		complaintScrollPane = new JScrollPane(complaintTextArea);
 		complaintScrollPane.setVerticalScrollBarPolicy(
         JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		//complaintScrollPane.setPreferredSize(new Dimension(400, 200));
 		//c.fill = GridBagConstraints.VERTICAL;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.0;
 		//c.ipady = 200;
 		//c.ipadx = 200;
 		c.gridx = 0;
 		c.gridy = 4;
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
 		pane.add(complaintScrollPane, c);
 		//textArea.getDocument().addDocumentListener(this);		
 		
 		JLabel problemLbl = new JLabel("Select problem type:");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 0;
 		c.gridy = 5;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(problemLbl, c);		
 				
 		problemTypeCombo = new JComboBox(problemTypeString);
 		c.weightx = 0.0;
 		c.gridx = 1;
 		c.gridy = 5;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,35,0,0);
     	pane.add(problemTypeCombo, c);		
 		
 		newComplButton = new JButton("Add Complaint");
 		//c.ipady = 20;
 		//c.weightx = 1;
 		c.gridx = 0;
 		c.gridy = 6;
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(newComplButton, c);
 		newComplButton.addActionListener(addComplaintListener);
 		newComplButton.setEnabled(false);
 		
     }
     
     
     public static void createAndShowGUI()
     {
         //Create and set up the window. Set instantiation parameters.
         frame = new JFrame("Complaints");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         	
     	frame.setResizable(false);
 		frame.setLocationRelativeTo(null);
 		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);			
  
         //Set up the content pane.
         addComponentsToPane(frame.getContentPane());
  
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
     
     
     //event listeners
     class NavigationListener implements ActionListener
     {
 		public void actionPerformed(ActionEvent e)
 		{
 			if (e.getActionCommand().equals("Home")) {                     
                 v = new View();	
 		    	v.createAndShowGUI();		    	
                 frame.dispose();
 			}
             if (e.getActionCommand().equals("Products")) {
                 pgui = new ProductsGUI();
 		    	pgui.createAndShowGUI();			    	
                 frame.dispose();
             }
             if (e.getActionCommand().equals("Joining")) {
                 jgui = new JoiningGUI();
 		    	jgui.createAndShowGUI();		    	
                 frame.dispose();
             }
             if (e.getActionCommand().equals("Customer Details")) {
                 cdgui = new CustDetailsGUI();
 		    	cdgui.createAndShowGUI();
                 frame.dispose();
             }
             if (e.getActionCommand().equals("Customer Validation")) 
         	{
             	cvgui = new CustValidationGUI();
 				cvgui.createAndShowGUI();
             	frame.dispose();
         	}
             if (e.getActionCommand().equals("Close")) {
                 System.exit(0);
             }
 		}
    }
     	
    
 	public class FetchCustHistListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent ev)
 		{		
 			String custId = custIdTxt.getText();
 			Connection conn = Call_Centre_Training.getConnection();
 			PreparedStatement stmt = null;
 			PreparedStatement stmt2 = null;
 			ResultSet rs = null;
 			ResultSet rs2 = null;
 			String status = "";

 			
 			String previousHistory = "";
 			if(("".equals(custId)))
     		{
     			JOptionPane.showMessageDialog(null,"Please enter a customer ID","Validation Warning",JOptionPane.WARNING_MESSAGE);
     		}
     		else
 			try
 			{
 				stmt = conn.prepareStatement("SELECT status FROM customer WHERE cust_id =?");
 				stmt.setString(1, custId);			
 				rs2 = stmt.executeQuery();
 				rs2.first();
 				status = rs2.getString("status");
 				if (status.equals("1"))
 				{
 					JOptionPane.showMessageDialog(null,"You are viewing an account that has been closed!");
 					rs2.next();
 				}
 				stmt2 = conn.prepareStatement("SELECT DATE_FORMAT( DATETIME,  '%W %D %M %Y %H:%i' ) AS DATETIME, complaint, problemType, dateTime as dt2 FROM complaints WHERE cust_id =? ORDER BY dt2 DESC");
 				stmt2.setString(1, custId);
 				rs = stmt2.executeQuery();
 
 				boolean found = rs.next();
 				
 				if (!found)
 				{
 					readOnlyTextArea.setText("No Existing Complaint");
 				}
 				else
 				{
 					int recordCount = 0;
 					while(rs.next())
 					{
 						recordCount++;
 					}
 					rs.first();
 					for(int i = 0;i < (recordCount +1); i++)
 					{
 						previousHistory += rs.getString(1) + "\n" + rs.getString(3) + "\n" + rs.getString(2) + "\n\n";
 						rs.next();
 					}
 					readOnlyTextArea.setText(previousHistory);
 					custIdTxt.setEnabled(false);	
 					fetchCustHistButton.setEnabled(false);
 					newComplButton.setEnabled(true);
 				}
 				readOnlyTextArea.setCaretPosition(0);
 				conn.close();
 			}
 			catch(SQLException ex)
 			{
 				JOptionPane.showMessageDialog(null,"Cannot find customer with that ID","Validation Warning",JOptionPane.WARNING_MESSAGE);
 			}
 		
 		}
 	}
 
 	class AddComplaintListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent ev)
 		{
 			String complaintIn = complaintTextArea.getText();
 			String problemTypeIn = (String)problemTypeCombo.getSelectedItem();
 			String previousComplaint = readOnlyTextArea.getText();
 			String custId = custIdTxt.getText();
 	    		
 		    	//Database insert
 		    	Connection conn = Call_Centre_Training.getConnection();
 				PreparedStatement stmt = null;
 				ResultSet rs = null;
 				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
 				Date date = new Date();
 				String dateNow = dateFormat.format(date);
 				try
 				{
 					stmt = conn.prepareStatement("INSERT INTO complaints (cust_id, dateTime, complaint, problemType) VALUES (?,?,?,?)");
 					stmt.setString(1, custId);
 					stmt.setString(2, dateNow);
 					stmt.setString(3, complaintIn);
 					stmt.setString(4, problemTypeIn);
 					stmt.executeUpdate();
 					JOptionPane.showMessageDialog(null,"Complaint added!");
					conn.close();
 				}
 				catch(SQLException ex)
 				{
 					readOnlyTextArea.setText("Please Enter Customer ID.");
 					ex.printStackTrace();
 				}
 				
 			
 			
 			PreparedStatement stmt2 = null;
 			ResultSet rs2 = null;
 			
 			String previousHistory2 = "";
 			try
 			{
 				stmt2 = conn.prepareStatement("SELECT DATE_FORMAT( DATETIME,  '%W %D %M %Y %H:%i' ) AS DATETIME, complaint, problemType, dateTime as dt2 FROM complaints WHERE cust_id =? ORDER BY dt2 DESC");
 				stmt2.setString(1, custId);
 				rs2 = stmt2.executeQuery();
 
 				boolean found2 = rs2.next();
 				
 				if (!found2)
 				{
 					readOnlyTextArea.setText("No Existing Complaint");
 				}
 				else
 				{
 					int recordCount2 = 0;
 					while(rs2.next())
 					{
 						recordCount2++;
 					}
 					rs2.first();
 					for(int i = 0;i < (recordCount2 +1); i++)
 					{
 						previousHistory2 += rs2.getString(1) + "\n" + rs2.getString(3) + "\n" + rs2.getString(2) + "\n\n";
 						rs2.next();
 					}
 					readOnlyTextArea.setText(previousHistory2);				
 				}
 				readOnlyTextArea.setCaretPosition(0);
 				conn.close();
 			}
 			catch(SQLException ex)
 			{
 				readOnlyTextArea.setText("Please Enter Customer ID.");
 				ex.printStackTrace();
 			}			
 			complaintTextArea.setText("");
 		}
 	}
 		
 
 	class ComplaintFocusListener implements FocusListener
     {
 		public void focusGained(FocusEvent e)
 		{
 			complaintTextArea.setText("");
 		}
 		public void focusLost(FocusEvent e) 
 		{			 
 		}	
     }				
 }
 	     
      
