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
 
 public class ProductsGUI
 {
 	static JFrame frame;
 	static JMenuBar menuBar;
 	static JButton fetchCustHistButton, newComplButton;
     
     static JTextArea readOnlyTextArea, discussTextArea;
     static JTextField custIdTxt, fNameTxt, sNameTxt, houseNumTxt, streetNameTxt, cityTxt, countyTxt, postCodeTxt, phoneNumTxt, emailTxt;
     
     static NavigationListener navigationListener;
     static ComplaintFocusListener complaintFocusListener;
     static FetchCustListener fetchCustListener;  
     static AddDiscussionListener addDiscussionListener; 
     static ProductDescListener productDescListener;
 	
 	View v;
 	ComplaintGUI cgui;
 	CustDetailsGUI cdgui;
 	JoiningGUI jgui;
 	ProductDescGUI p;
 	CustValidationGUI cvgui;
 
 	
 	public ProductsGUI()
     { 		  	   	
     	navigationListener = new NavigationListener();
     	complaintFocusListener = new ComplaintFocusListener();
     	fetchCustListener = new FetchCustListener(); 
     	addDiscussionListener = new AddDiscussionListener();
     	productDescListener = new ProductDescListener();
     }
     
     public static void addComponentsToPane(Container pane)
     {      	  	  			
 		// Get the default toolkit
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 		
 		// Get the current screen size
 		Dimension scrnsize = toolkit.getScreenSize();
 		double width = (scrnsize.getWidth() / 2) - 400;
 		double height = (scrnsize.getHeight() / 2) - 400;
 		
 		frame.setLocation((int)width,(int)height);
 		
 		
 		//scroll pane
 		JScrollPane discussScrollPane;
 		JScrollPane readOnlyScrollPane;
 		
 		//menus
 		JMenu fileMenu = new JMenu("File");
 		JMenu pageNav = new JMenu("Navigation");
		JMenu fileHelpMenu = new JMenu("Help");
 		
 		//menu items
 		JMenuItem fileClose = new JMenuItem("Close");
 		
 		JMenuItem navHome = new JMenuItem("Home");
 		JMenuItem navCustVal = new JMenuItem("Customer Validation");
 		JMenuItem navComplaints = new JMenuItem("Complaints");
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
 		pageNav.add(navCustVal);
 		pageNav.add(navComplaints);
 		pageNav.add(navJoining);
 		pageNav.add(navCustDetails);
 		
 		fileClose.addActionListener(navigationListener);
 		navHome.addActionListener(navigationListener);
 		navCustVal.addActionListener(navigationListener);
 		navComplaints.addActionListener(navigationListener);
 		navJoining.addActionListener(navigationListener);
 		navCustDetails.addActionListener(navigationListener);
		
		//help menu
		fileHelpMenu.add(helpFAQ);
		fileHelpMenu.add(helpGuide);
		fileHelpMenu.add(helpSearch); 
		
 		
 		//add menu
 		c.ipady = 15;
 		c.weightx = 0.5;
 		c.gridy = 0;
 		c.gridx = 0;
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
 		c.gridy = 1;
 		c.gridx = 0;
 		c.gridwidth = 6;
 		c.gridheight = 1;
 		//c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,0,10,0);
 		pane.add(homeComplButton, c);   
 		homeComplButton.addActionListener(navigationListener); 	
 		
 		
 		JLabel custIdLbl = new JLabel("Customer ID:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridy = 2;
 		c.gridx = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		//c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,20,0,0);
 		pane.add(custIdLbl, c);
 		
 		
 		custIdTxt = new JTextField(10);
 		custIdTxt.setFont(new Font("Serif", Font.ITALIC, 16));
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 2;
 		c.gridx = 1;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
 		//c.fill = GridBagConstraints.HORIZONTAL;
 		pane.add(custIdTxt, c);
 		
 		
 		
 		fetchCustHistButton = new JButton("Fetch History");
 		//c.ipady = 20;
 		c.weightx = 0.0;
 		c.gridy = 2;
 		c.gridx = 2;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		//c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,0,0,0);
 		pane.add(fetchCustHistButton, c);
 		fetchCustHistButton.addActionListener(fetchCustListener);
 		
 		
 		//horizontal separator
 		JSeparator topSeparator = new JSeparator(JSeparator.HORIZONTAL);
 		topSeparator.setPreferredSize(new Dimension(1,6));
 		c.gridy = 3;
 		c.gridx = 0;
 		c.gridwidth = 0;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(5,0,-15,0);
 		c.weightx = 1;
 		pane.add(topSeparator, c);
 		
 		
 		
 		//First Name
 		JLabel fNameLbl = new JLabel("First Name:");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 4;
 		c.gridx = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,20,0,0);
 		pane.add(fNameLbl, c);
 			
 		
 		fNameTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;	
 		c.gridx = 1;
 		c.gridy = 4;		
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,-30,0,60);
 		pane.add(fNameTxt, c);
 		fNameTxt.setEnabled(false);
 		
 		
 		
 		//Surname
 		JLabel sNameLbl = new JLabel("Surname:");
 		c.ipady = 20;
 		c.weightx = 0.1;	
 		c.gridy = 5;
 		c.gridx = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,20,0,30);
 		pane.add(sNameLbl, c);
 		
 		sNameTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 5;
 		c.gridx = 1;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,-30,0,60);
 		pane.add(sNameTxt, c);
 		sNameTxt.setEnabled(false);
 		
 		
 		
 		//House Number
 		JLabel houseNumLbl = new JLabel("House Number:");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 4;
 		c.gridx = 2;		
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,-30,0,60);
 		pane.add(houseNumLbl, c);
 		
 		houseNumTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 4;
 		c.gridx = 3;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,-30,0,-60);
 		pane.add(houseNumTxt, c);
 		houseNumTxt.setEnabled(false);
 		
 		
 		//Street Name
 		JLabel streetNameLbl = new JLabel("Street Name:");
 		c.ipady = 20;
 		c.weightx = 0.1;		
 		c.gridy = 5;
 		c.gridx = 2;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,-30,0,60);
 		pane.add(streetNameLbl, c);
 		
 		streetNameTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;	
 		c.gridy = 5;
 		c.gridx = 3;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,-30,0,-60);
 		pane.add(streetNameTxt, c);
 		streetNameTxt.setEnabled(false);
 		
 		
 		//City
 		JLabel cityLbl = new JLabel("City:");
 		c.ipady = 20;
 		c.weightx = 1.0;		
 		c.gridy = 4;
 		c.gridx = 4;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,100,0,0);
 		pane.add(cityLbl, c);
 		
 		cityTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 4;
 		c.gridx = 5;	
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,30,0,0);
 		pane.add(cityTxt, c);
 		cityTxt.setEnabled(false);
 		
 		//County
 		JLabel countyLbl = new JLabel("County:");
 		c.ipady = 20;
 		c.weightx = 0.1;	
 		c.gridy = 6;
 		c.gridx = 4;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,100,0,0);
 		pane.add(countyLbl, c);
 		
 		countyTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 6;
 		c.gridx = 5;	
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,30,0,0);
 		pane.add(countyTxt, c);
 		countyTxt.setEnabled(false);
 		
 		
 		//Post Code
 		JLabel postCodeLbl = new JLabel("Post Code:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridy = 5;
 		c.gridx = 4;	
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,100,0,0);
 		pane.add(postCodeLbl, c);
 		
 		postCodeTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 5;
 		c.gridx = 5;	
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,30,0,0);
 		pane.add(postCodeTxt, c);
 		postCodeTxt.setEnabled(false);
 		
 		
 		//Telephone No.
 		JLabel phoneNumLbl = new JLabel("Telephone Number: ");
 		c.ipady = 20;
 		c.weightx = 0.1;	
 		c.gridy = 6;
 		c.gridx = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,20,0,0);
 		pane.add(phoneNumLbl, c);
 		
 		phoneNumTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 6;
 		c.gridx = 1;	
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,-30,0,60);
 		pane.add(phoneNumTxt, c);
 		phoneNumTxt.setEnabled(false);
 		
 		//Email Address
 		JLabel emailLbl = new JLabel("Email Address:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridy = 6;
 		c.gridx = 2;	
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,-30,0,60);
 		pane.add(emailLbl, c);
 		
 		emailTxt = new JTextField("");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 6;
 		c.gridx = 3;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,-30,0,-60);
 		pane.add(emailTxt, c);
 		emailTxt.setEnabled(false);
 		
 		//horizontal separator
 		JSeparator lowerSeparator = new JSeparator(JSeparator.HORIZONTAL);
 		lowerSeparator.setPreferredSize(new Dimension(1,6));
 		c.gridy = 7;
 		c.gridx = 0;
 		c.gridwidth = 0;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(5,0,-15,0);
 		c.weightx = 1;
 		pane.add(lowerSeparator, c);
 		
 		
 		JLabel discussionLbl = new JLabel("Previous Discussion History");
 		//c.ipady = 20;
 		//c.weightx = 1;
 		c.gridy = 8;
 		c.gridx = 0;		
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,0,0,0);
 		pane.add(discussionLbl, c);
 		
 		
 		
 		//add text area
 		readOnlyTextArea = new JTextArea(10, 40);
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
 		c.gridy = 9;
 		c.gridx = 0;	
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
 		pane.add(readOnlyScrollPane, c);
 		//textArea.getDocument().addDocumentListener(this);
 			
 		
 		//add text area
 		discussTextArea = new JTextArea("Add discussion...", 10, 40);
 		discussTextArea.setFont(new Font("Serif", Font.ITALIC, 16));
 		discussTextArea.setLineWrap(true);
 		discussTextArea.setWrapStyleWord(true);
 		discussTextArea.addFocusListener(complaintFocusListener);
 		discussTextArea.setEnabled(false);
 		
 		discussScrollPane = new JScrollPane(discussTextArea);
 		discussScrollPane.setVerticalScrollBarPolicy(
 		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		//complaintScrollPane.setPreferredSize(new Dimension(400, 200));
 		//c.fill = GridBagConstraints.VERTICAL;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.0;
 		//c.ipady = 200;
 		//c.ipadx = 200;	
 		c.gridy = 9;
 		c.gridx = 3;
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
 		pane.add(discussScrollPane, c);
 		//textArea.getDocument().addDocumentListener(this);
 		
 		
 		newComplButton = new JButton("Add Discussion to History");
 		//c.ipady = 20;
 		//c.weightx = 1;
 		c.gridy = 10;
 		c.gridx = 5;		
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,0,0,0);
 		pane.add(newComplButton, c);
 		newComplButton.addActionListener(addDiscussionListener);
 		newComplButton.setEnabled(false);
 		
 		
 		JButton prodDescButton = new JButton("Product Descriptions");
 		//c.ipady = 20;
 		//c.weightx = 1;
 		c.gridy = 11;
 		c.gridx = 0;		
 		c.gridwidth = 0;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(10,0,0,0);
 		pane.add(prodDescButton, c);
 		prodDescButton.addActionListener(productDescListener);
     }
       
     public static void createAndShowGUI()
     {
         //Create and set up the window. Set instantiation parameters.
         frame = new JFrame("Products");
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
     class FetchCustListener implements ActionListener
     {
 		public void actionPerformed(ActionEvent e)
 		{
 			String custId;
 			Pattern pattern = Pattern.compile("^[0-9]");
 			Matcher matcher = pattern.matcher(custIdTxt.getText());
 			if(!matcher.matches())
 			{
 				JOptionPane.showMessageDialog(null,"Incorrect customer ID format, must be number(s).","Validation Warning",JOptionPane.WARNING_MESSAGE);
 				custIdTxt.setText("");
 			}
 			else
 			{
 				custId = custIdTxt.getText();
 				
 				
 				Connection conn = Call_Centre_Training.getConnection();
 				PreparedStatement stmt = null;
 				PreparedStatement stmt2 = null;
 				PreparedStatement stmt3 = null;
 				ResultSet rs = null;
 				ResultSet rs2 = null;
 				ResultSet rs3 = null;
 				String status = "";
 				
 				
 				
 				String previousHistory = "";
 				if(("".equals(custId)) )
 				{
 					JOptionPane.showMessageDialog(null,"Please enter a customer ID","Validation Warning",JOptionPane.WARNING_MESSAGE);
 				}
 				else
 				{
 					
 					try
 					{
 						stmt = conn.prepareStatement("SELECT status FROM customer WHERE cust_id =?");
 						stmt.setString(1, custId);
 						rs3 = stmt.executeQuery();
 						rs3.first();
 						status = rs3.getString("status");
 						if (status.equals("1"))
 						{
 							JOptionPane.showMessageDialog(null,"You are viewing an account that has been closed!");
 							rs3.next();
 						}
 						stmt2 = conn.prepareStatement("SELECT cust_id,fName,sName,houseNo,streetName,city,county,postCode,telNo,email FROM customer WHERE cust_id =?");
 						stmt2.setString(1, custId);
 						rs = stmt2.executeQuery();					
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
 								cityTxt.setText(rs.getString("City"));
 								countyTxt.setText(rs.getString("county"));
 								postCodeTxt.setText(rs.getString("postCode"));
 								phoneNumTxt.setText(rs.getString("telNo"));
 								emailTxt.setText(rs.getString("email"));	
 								custIdTxt.setEnabled(false);		
 								newComplButton.setEnabled(true);	
 								discussTextArea.setEnabled(true);
 							}
 							
 						}
						conn.close();
 					}
 					catch(SQLException ex)
 					{		
 						JOptionPane.showMessageDialog(null,"Cannot find customer with that ID","Validation Warning",JOptionPane.WARNING_MESSAGE);
 					}
 				}
 				try
 				{
 					stmt3 = conn.prepareStatement("SELECT DATE_FORMAT(dateTime, '%W %D %M %Y %H:%i') AS dateTime,discussion, dateTime as dt2 FROM discussion WHERE cust_id =? ORDER BY dt2 DESC");
 					stmt3.setString(1, custId);
 					rs2 = stmt3.executeQuery();
 											
 					boolean found2 = rs2.next();				
 					if (!found2)
 					{
 						readOnlyTextArea.setText("No Previous history");
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
 							previousHistory += rs2.getString(1) + "\n" + rs2.getString(2) + "\n\n";
 							rs2.next();
 						}
 						readOnlyTextArea.setText(previousHistory);
 						readOnlyTextArea.setCaretPosition(0);
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
 			if (e.getActionCommand().equals("Joining")) 
 			{
 			    jgui = new JoiningGUI();
 				jgui.createAndShowGUI();
 			    frame.dispose();
 			}
 			if (e.getActionCommand().equals("Customer Details")) 
 			{
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
     	
     class AddDiscussionListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent ev)
 		{
 			String discussionIn = discussTextArea.getText();
 			String previousDiscussion = readOnlyTextArea.getText();
 			String custId = custIdTxt.getText();
 	    	String previousHistory = "";
 	    	//Database insert
 	    	Connection conn = Call_Centre_Training.getConnection();
 			PreparedStatement stmt = null;
 			ResultSet rs = null;
 			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
 			Date date = new Date();
 			String dateNow = dateFormat.format(date);
 			try
 			{
 				stmt = conn.prepareStatement("INSERT INTO discussion (cust_id, dateTime, discussion) VALUES (?,?,?)");
 				stmt.setString(1,custId);
 				stmt.setString(2,dateNow);
 				stmt.setString(3,discussionIn);
 				stmt.executeUpdate();
 				JOptionPane.showMessageDialog(null,"Discussion added!");
				conn.close();
 			}
 			catch(SQLException ex)
 			{
 				readOnlyTextArea.setText("Please Enter Customer ID.");
 				ex.printStackTrace();
 			}
 			PreparedStatement stmt2 = null;
 			ResultSet rs2 = null;
 			try
 			{
 				stmt2 = conn.prepareStatement("SELECT DATE_FORMAT(dateTime, '%W %D %M %Y %H:%i') AS dateTime,discussion, dateTime as dt2 FROM discussion WHERE cust_id =? ORDER BY dt2 DESC");
 				stmt2.setString(1, custId);
 				rs2 = stmt2.executeQuery();
 										
 				boolean found2 = rs2.next();				
 				if (!found2)
 				{
 					readOnlyTextArea.setText("No Previous history");
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
 						previousHistory += rs2.getString(1) + "\n" + rs2.getString(2) + "\n\n";
 						rs2.next();
 					}
 					readOnlyTextArea.setText(previousHistory);					
 				}
 				conn.close();
 			}
 			catch(SQLException ex)
 			{			
 				ex.printStackTrace();
 			}
 			readOnlyTextArea.setText(previousHistory);
 			readOnlyTextArea.setCaretPosition(0);
 			
 		}    		
 	}
 	    	
 	class ProductDescListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent e)
 		{
 			p = new ProductDescGUI();
 			p.createAndShowGUI();
 		}	
 	}     	
     	
     class ComplaintFocusListener implements FocusListener
 	{
      	public void focusGained(FocusEvent e)
      	{
 			discussTextArea.setText("");
      	}
         public void focusLost(FocusEvent e) 
         {  		 
     	}		     	
 	}
 }
