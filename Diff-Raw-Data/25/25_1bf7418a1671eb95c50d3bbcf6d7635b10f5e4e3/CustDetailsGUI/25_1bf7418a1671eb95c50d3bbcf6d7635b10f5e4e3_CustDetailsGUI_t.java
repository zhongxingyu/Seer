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
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class CustDetailsGUI
 {		
 	static JFrame frame;
 	static JMenuBar menuBar;
 	
 	final static boolean shouldFill = true;
     final static boolean shouldWeightX = true;
     final static boolean RIGHT_TO_LEFT = false;
     
     static NavigationListener navigationListener;
     static CloseCustomerListener closeCustomerListener;
     static OpenCustomerListener openCustomerListener;
     static UpdateCustomerListener updateCustomerListener;
     static FetchCustListener fetchCustListener;
     static JTextField custIdTxt, fNameTxt, sNameTxt, houseNumTxt, streetNameTxt, cityTxt, countyTxt, postCodeTxt, phoneNumTxt, emailTxt, secAnswerTxt;
     static JComboBox secQuestionCombo;
     static JButton fetchCustDetailsButton, closeAccountButton, openAccountButton, updateDetailsButton;
     
     static String[] secQuestionString = {"First pets name?", "Mothers maiden name?", "Favourite actor?"};
 	
 	View v;
 	ComplaintGUI cgui;
 	ProductsGUI pgui;
 	JoiningGUI jgui;
 	CustValidationGUI cvgui;
 	
 	public CustDetailsGUI()
     { 	   	
     	navigationListener = new NavigationListener();
     	closeCustomerListener = new CloseCustomerListener();
     	openCustomerListener = new OpenCustomerListener();
     	updateCustomerListener = new UpdateCustomerListener();
     	fetchCustListener = new FetchCustListener();
     }
     
     
     public static void addComponentsToPane(Container pane)
     {
     	// Get the default toolkit
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 
 		// Get the current screen size
 		Dimension scrnsize = toolkit.getScreenSize();
 		double width = (scrnsize.getWidth() / 2) - 150;
 		double height = (scrnsize.getHeight() / 2) - 280;
     	
     	frame.setLocation((int)width,(int)height);
     	     	  	  	
     	if (RIGHT_TO_LEFT)
     	{
             pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
         }
 
 		//menus
 		JMenu fileMenu = new JMenu("File");
 		JMenu pageNav = new JMenu("Navigation");

 		
 		//menu items
 		JMenuItem fileClose = new JMenuItem("Close");
 		
 		JMenuItem navHome = new JMenuItem("Home");
 		JMenuItem navProducts = new JMenuItem("Products");
 		JMenuItem navComplaints = new JMenuItem("Complaints");
 		JMenuItem navJoining = new JMenuItem("Joining");
 		JMenuItem navCustVal = new JMenuItem("Customer Validation");
 		
 
 	    pane.setLayout(new GridBagLayout());
 	    GridBagConstraints c = new GridBagConstraints();
 	    
 	    if (shouldFill)
 	    {	//maximum height, maximum width
 	    	c.fill = GridBagConstraints.VERTICAL;		    
 		    c.fill = GridBagConstraints.HORIZONTAL;
 	    }
 	    	    
 	    
 		//menu bar
 		menuBar = new JMenuBar();
 		pane.add(menuBar);
 		//pane.setJMenuBar(menuBar);
     	menuBar.add(fileMenu);
     	menuBar.add(pageNav);
 		
 		//file menu
     	fileMenu.add(fileClose);
     	
     	//nav menu
     	pageNav.add(navHome);
     	pageNav.add(navProducts);
     	pageNav.add(navJoining);
     	pageNav.add(navCustVal);
     	pageNav.add(navComplaints);
     	pageNav.add(navCustVal);
     	
     	fileClose.addActionListener(navigationListener);
     	navHome.addActionListener(navigationListener);
     	navProducts.addActionListener(navigationListener);
     	navJoining.addActionListener(navigationListener);
     	navCustVal.addActionListener(navigationListener);
     	navComplaints.addActionListener(navigationListener);
     	
     	
     	
     	//add menu
     	c.ipady = 15;
 	    c.weightx = 0.5;
     	c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 0;
 		c.gridheight = 1;
     	pane.add(menuBar, c);
     	//c.insets = new Insets(0,0,0,0);
     	//menuBar.addActionListener(navigationListener); 
     	
 	 	//add buttons
     	JButton homeComplButton = new JButton("Home");
 	    c.ipady = 20;
 	    c.weightx = 0.0;
     	c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,10,0);
     	pane.add(homeComplButton, c);   
     	homeComplButton.addActionListener(navigationListener);
     	
     	
     	JLabel custIdLbl = new JLabel("Customer ID:");
 		//c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 2;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,15,0);
     	pane.add(custIdLbl, c);
 		
 		
 		custIdTxt = new JTextField("");
 		//c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 1;
 		c.gridy = 2;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,15,40);
     	pane.add(custIdTxt, c);
 				
 		fetchCustDetailsButton = new JButton("Fetch Customer Details");
 		//c.ipady = 20;
 		c.weightx = 0.0;
 		c.gridx = 2;
 		c.gridy = 2;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,-35,15,0);
     	pane.add(fetchCustDetailsButton, c);
 		fetchCustDetailsButton.addActionListener(fetchCustListener);
 		
 		
 		///////////////customer details fields/////////////////
 		//First Name
 		JLabel fNameLbl = new JLabel("First Name:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(fNameLbl, c);
 		
 		fNameTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 1;
 		c.gridy = 3;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(fNameTxt, c);
 		
 		
 		//Surname
 		JLabel sNameLbl = new JLabel("Surname:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 4;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(sNameLbl, c);
 		
 		sNameTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 1;
 		c.gridy = 4;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(sNameTxt, c);
 		
 		
 		//House Number
 		JLabel houseNumLbl = new JLabel("House Number:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 5;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(houseNumLbl, c);
 		
 		houseNumTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 1;
 		c.gridy = 5;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(houseNumTxt, c);
 		
 		
 		//Street Name
 		JLabel streetNameLbl = new JLabel("Street Name:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 6;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(streetNameLbl, c);
 		
 		streetNameTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 1;
 		c.gridy = 6;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(streetNameTxt, c);
 		
 		
 		//City
 		JLabel cityLbl = new JLabel("City:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 7;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(cityLbl, c);
 		
 		cityTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 1;
 		c.gridy = 7;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(cityTxt, c);
 		
 		
 		//County
 		JLabel countyLbl = new JLabel("County:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 8;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(countyLbl, c);
 		
 		countyTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 1;
 		c.gridy = 8;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(countyTxt, c);
 		
 		
 		//Post Code
 		JLabel postCodeLbl = new JLabel("Post Code:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 9;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(postCodeLbl, c);
 		
 		postCodeTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 1;
 		c.gridy = 9;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(postCodeTxt, c);
 		
 		
 		//Telephone No.
 		JLabel phoneNumLbl = new JLabel("Telephone Number: ");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 10;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(phoneNumLbl, c);
 		
 		phoneNumTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 1;
 		c.gridy = 10;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(phoneNumTxt, c);
 		
 		
 		//Email Address
 		JLabel emailLbl = new JLabel("Email Address:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 11;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(emailLbl, c);
 		
 		emailTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 1;
 		c.gridy = 11;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(emailTxt, c);
 		
 		
 		//Security Question
 		JLabel secQuestionLbl = new JLabel("Security Question:");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 0;
 		c.gridy = 12;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(secQuestionLbl, c);
 		
 		secQuestionCombo = new JComboBox(secQuestionString);
 		//c.ipadx = 100;
 		c.weightx = 0.0;
 		c.gridx = 1;
 		c.gridy = 12;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,100,0,0);
     	pane.add(secQuestionCombo, c);
 		
 		
 		//Security Answer
 		JLabel secAnswerLbl = new JLabel("Security Answer:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 13;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(secAnswerLbl, c);
 		
 		secAnswerTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 1;
 		c.gridy = 13;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(secAnswerTxt, c);
     	
     	//button
     	updateDetailsButton = new JButton("Update");
 	    c.ipady = 20;
 	    c.weightx = 0.0;
     	c.gridx = 0;
 		c.gridy = 14;
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.insets = new Insets(10,0,0,0);
     	pane.add(updateDetailsButton, c);   
     	updateDetailsButton.addActionListener(updateCustomerListener);
     	updateDetailsButton.setEnabled(false);
     	
     	//button
     	closeAccountButton = new JButton("Close Account");
 	    c.ipady = 20;
 	    c.weightx = 0.0;
     	c.gridx = 0;
 		c.gridy = 15;
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.insets = new Insets(10,0,0,0);
     	pane.add(closeAccountButton, c);   
     	closeAccountButton.addActionListener(closeCustomerListener);
     	closeAccountButton.setVisible(true);
     	closeAccountButton.setEnabled(false);
     	
     	    	//button
     	openAccountButton = new JButton("Re-open Account");
 	    c.ipady = 20;
 	    c.weightx = 0.0;
     	c.gridx = 0;
 		c.gridy = 15;
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.insets = new Insets(10,0,0,0);
     	pane.add(openAccountButton, c);   
     	openAccountButton.addActionListener(openCustomerListener);
     	openAccountButton.setVisible(false);
   	 	openAccountButton.setEnabled(false);
     }
     
     
     public static void createAndShowGUI()
     {
         //Create and set up the window. Set instantiation parameters.
         frame = new JFrame("Customer Details");
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
     class CloseCustomerListener implements ActionListener
     {
 		public void actionPerformed(ActionEvent e)
 		{
 			int result = JOptionPane.showConfirmDialog(null,"Are you sure you want to close this customer account?","Close Account",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
 			if (result == JOptionPane.YES_OPTION)
 			{
 				String custId = custIdTxt.getText();	
 				Connection conn = Call_Centre_Training.getConnection();
 				PreparedStatement stmt = null;
 				PreparedStatement stmt2 = null;
 				ResultSet rs = null;
 				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
 				Date date = new Date();
 				String dateNow = dateFormat.format(date);	
 				try
 				{
 					String reason = JOptionPane.showInputDialog(null, "Please enter a reason for account closure: ", "Closing Account", 1);
 					if(reason != null){
 						reason = "No reason entered.";
 						stmt = conn.prepareStatement("UPDATE customer SET status=1 WHERE cust_id=?");
 						stmt.setString(1, custId);
 						stmt.executeUpdate();
 						custIdTxt.setEnabled(true);	
 						fetchCustDetailsButton.setEnabled(true);
 						custIdTxt.setText("");
 						fNameTxt.setText("");
 				    	sNameTxt.setText("");
 				    	houseNumTxt.setText("");
 				    	streetNameTxt.setText("");
 				    	cityTxt.setText("");
 				    	countyTxt.setText("");
 				    	postCodeTxt.setText("");
 				    	phoneNumTxt.setText("");
 				    	emailTxt.setText("");
 				    	secAnswerTxt.setText("");
 				    	closeAccountButton.setEnabled(false);
 						openAccountButton.setEnabled(false);
 						updateDetailsButton.setEnabled(false);
 					 	stmt2 = conn.prepareStatement("INSERT INTO closed (customer_id, date, reason ) VALUES (?,?,?)");
 					    stmt2.setString(1, custId);
 					    stmt2.setString(2, dateNow);
 					    stmt2.setString(3, reason);
 						stmt2.executeUpdate();
 						JOptionPane.showMessageDialog(null,"Customer Closed!");
 						conn.close();
 					}else
 					{
 						JOptionPane.showMessageDialog(null, "You pressed the cancel button, therefore not closing account.", "Closing Account", 1);
 					}						
 				}
 				catch(SQLException ex)
 				{
 					ex.printStackTrace();
 				}	      
 	       	}
 	      	else if (result == JOptionPane.NO_OPTION)
 	      	{
       	   		JOptionPane.showMessageDialog(null, "You pressed the cancel button, therefore not closing account.", "Closing Account", 1);
     		}       
     	}	
     }
     
     class OpenCustomerListener implements ActionListener
     {
     	public void actionPerformed(ActionEvent e)
     	{
     		int result = JOptionPane.showConfirmDialog(null,"Are you sure you want to re-open this customer account?","Open Account",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
     		if (result == JOptionPane.YES_OPTION)
     		{
 				String custId = custIdTxt.getText();	
 				Connection conn = Call_Centre_Training.getConnection();
 				PreparedStatement stmt = null;
 				ResultSet rs = null;
 								
 				try
 				{
 					stmt = conn.prepareStatement("UPDATE customer SET status=0 WHERE cust_id=?");
 					stmt.setString(1, custId);
 					stmt.executeUpdate();
 					
 					JOptionPane.showMessageDialog(null,"Customer Re-opened!");
 					custIdTxt.setEnabled(true);	
 					fetchCustDetailsButton.setEnabled(true);
 					custIdTxt.setText("");
 					fNameTxt.setText("");
 			    	sNameTxt.setText("");
 			    	houseNumTxt.setText("");
 			    	streetNameTxt.setText("");
 			    	cityTxt.setText("");
 			    	countyTxt.setText("");
 			    	postCodeTxt.setText("");
 			    	phoneNumTxt.setText("");
 			    	emailTxt.setText("");
 			    	secAnswerTxt.setText("");
 			    	closeAccountButton.setEnabled(false);
 					openAccountButton.setEnabled(false);
 					updateDetailsButton.setEnabled(false);
 					conn.close();
 		
 				}
 				catch(SQLException ex)
 				{
 					ex.printStackTrace();
 				}    	      
     	    }
     	    else if (result == JOptionPane.NO_OPTION)
     	    {
           		System.out.println("Re-opening account cancelled");
         	}  	       
     	} 	
     }
     
     class UpdateCustomerListener implements ActionListener
     {
     	public void actionPerformed(ActionEvent e)
     	{
     		Pattern telephonePattern = Pattern.compile("\\Q+\\E[0-9]{0,14}$|[0-9]{0,14}$");
     		Matcher phoneMatcher = telephonePattern.matcher(phoneNumTxt.getText());
     		
     		
     	    int result = JOptionPane.showConfirmDialog(null,"Are you sure you want to update customer details?","Account update",JOptionPane.YES_NO_OPTION);
     	    if (result == JOptionPane.YES_OPTION) 
     	   	{
 				String custId = custIdTxt.getText();	
 				String fNameIn = fNameTxt.getText();
 				String sNameIn = sNameTxt.getText();
 				String houseNumIn = houseNumTxt.getText();
 				String streetNameIn = streetNameTxt.getText();
 				String cityIn = cityTxt.getText();
 				String countyIn = countyTxt.getText();
 				String postCodeIn = postCodeTxt.getText();
 					if(!phoneMatcher.matches())
 			    	{
 			    		JOptionPane.showMessageDialog(null,"Phone number can contain only numbers.");
 			    	}else
 				    	{
 						String phoneIn = phoneNumTxt.getText();
 						String emailIn = emailTxt.getText();
 						String secQuestionIn = (String)secQuestionCombo.getSelectedItem();
 						String secAnswerIn = secAnswerTxt.getText();
 						
 						Connection conn = Call_Centre_Training.getConnection();
 						PreparedStatement stmt = null;
 						ResultSet rs = null;
 						try
 						{
 							stmt = conn.prepareStatement("UPDATE customer SET fName=?, sName=?, houseNo=?, streetName=?, city=?, county=?, postCode=?, telNo=?, email=?, secQues=?, SecAns=?  WHERE cust_id=?");
 							stmt.setString(1, fNameIn);
 							stmt.setString(2, sNameIn);
 							stmt.setString(3, houseNumIn);
 							stmt.setString(4, streetNameIn);
 							stmt.setString(5, cityIn);
 							stmt.setString(6, countyIn);
 							stmt.setString(7, postCodeIn);
 							stmt.setString(8, phoneIn);
 							stmt.setString(9, emailIn);
 							stmt.setString(10, secQuestionIn);
 							stmt.setString(11, secAnswerIn);
 							stmt.setString(12, custId);
 							stmt.executeUpdate();
 							JOptionPane.showMessageDialog(null,"Customer details updated!");
 							conn.close();
 						}
 						catch(SQLException ex)
 						{
 						ex.printStackTrace();
 						}			  	
 		    	    }
     	   		}
 	     	    else if (result == JOptionPane.NO_OPTION)
 	     	    {
 	          		System.out.println("Update cancelled.");
 	       		}   	     
 	    	}
     	 	
     }
     
      class FetchCustListener implements ActionListener
      {
 		public void actionPerformed(ActionEvent ev)
 		{			
 			String custId = custIdTxt.getText();
 			String secQues = ""; 
 			Connection conn = Call_Centre_Training.getConnection();
 			PreparedStatement stmt = null;
 			ResultSet rs = null;
			String q1 = "Mothers Maiden Name?";
			String q2 = "Favourite actor?";
 			String status = "";
 			if(("".equals(custId)))
     		{
     			JOptionPane.showMessageDialog(null,"Please enter a customer ID","Validation Warning",JOptionPane.WARNING_MESSAGE);
     		}
     		else
     		{		
 				try
 				{
 					stmt = conn.prepareStatement("SELECT fName,sName,houseNo,streetName,city,county,postCode,telNo,email,secQues,secAns,status FROM customer WHERE cust_id =?");
 					stmt.setString(1, custId);
 					rs = stmt.executeQuery();
 					
 					boolean found = rs.next();				
 					if (!found)
 					{
 						JOptionPane.showMessageDialog(null,"No customer found!");
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
 							fNameTxt.setText(rs.getString("fName"));
 							sNameTxt.setText(rs.getString("sName"));
 							houseNumTxt.setText(rs.getString("houseNo"));
 							streetNameTxt.setText(rs.getString("streetName"));
 							cityTxt.setText(rs.getString("city"));
 							countyTxt.setText(rs.getString("county"));
 							postCodeTxt.setText(rs.getString("postCode"));
 							phoneNumTxt.setText(rs.getString("telNo"));
 							emailTxt.setText(rs.getString("email"));
 							secQues = rs.getString("secQues");
							if (q1.equals(secQues))
 							{
 								secQuestionCombo.setSelectedIndex(1);
 							}
							else if (q2.equals(secQues))
 							{
 								secQuestionCombo.setSelectedIndex(2);
 							}
 							else
 							{
 								secQuestionCombo.setSelectedIndex(0);	
 							}		
 							secAnswerTxt.setText(rs.getString("secAns"));
 							status = rs.getString("status");
 							closeAccountButton.setEnabled(true);
 							openAccountButton.setEnabled(true);
 							updateDetailsButton.setEnabled(true);
 							if (status.equals("1"))
 							{
 								closeAccountButton.setVisible(false);
 								openAccountButton.setVisible(true);
 								JOptionPane.showMessageDialog(null,"You are viewing an account that has been closed!");
 							}
 							else
 							{
 								closeAccountButton.setVisible(true);
 							}
 							rs.next();
 						}
 						custIdTxt.setEnabled(false);	
 						fetchCustDetailsButton.setEnabled(false);
 					}		
 					conn.close();		
 				}
 				catch(SQLException ex)
 				{				
 					ex.printStackTrace();
 				}
 			}
 		}
     }
     
     
     class NavigationListener implements ActionListener
     {
 		public void actionPerformed(ActionEvent e)
 		{
 			if (e.getActionCommand().equals("Home")) 
 			{
 			 	v = new View();	
 			    v.createAndShowGUI();		    	
 			   	frame.dispose();
 			}
 			if (e.getActionCommand().equals("Complaints")) 
 			{
 			 	cgui = new ComplaintGUI();
 			    cgui.createAndShowGUI();	
 			    frame.dispose();
 			}
 			if (e.getActionCommand().equals("Products")) 
 			{
 			    pgui = new ProductsGUI();
 				pgui.createAndShowGUI();
 			    frame.dispose();
 			}
 			if (e.getActionCommand().equals("Joining")) 
 			{
 			    jgui = new JoiningGUI();
 				jgui.createAndShowGUI();
 			    frame.dispose();
 			}
 			if (e.getActionCommand().equals("Customer Validation")) 
 			{
 			    cvgui = new CustValidationGUI();
 				cvgui.createAndShowGUI();
 			    frame.dispose();
 			}
 			if (e.getActionCommand().equals("Close")){
 			    System.exit(0);
 			}
 		}
     }
 }
