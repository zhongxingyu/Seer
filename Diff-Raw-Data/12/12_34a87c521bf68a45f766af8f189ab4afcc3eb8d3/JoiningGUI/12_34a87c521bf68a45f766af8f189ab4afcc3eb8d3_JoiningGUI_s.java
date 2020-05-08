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
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class JoiningGUI
 {
 	View v;
 	ComplaintGUI cgui;
 	ProductsGUI pgui;
 	CustDetailsGUI cdgui;
 	CustValidationGUI cvgui;
 	
 	static NavigationListener navigationListener;
 	static AddCustomerListener addCustomerListener;
 	static ResetListener resetListener;
 	
 	static JFrame frame;
 	static JMenuBar menuBar;
 	
 	public static JTextField custIdTxt, fNameTxt, sNameTxt, houseNumTxt, streetNameTxt, cityTxt, countyTxt, postCodeTxt, phoneNumTxt, emailTxt, secAnswerTxt;
     static JComboBox secQuestionCombo;
     
     static String[] secQuestionString = {"First pets name?", "Mothers maiden name?", "Favourite actor?"};
 	
 	public JoiningGUI()
     {
     	navigationListener = new NavigationListener();   
     	addCustomerListener = new AddCustomerListener();
     	resetListener = new ResetListener();
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
 		JScrollPane readOnlyScrollPane;
 		
 		//menus
 		JMenu fileMenu = new JMenu("File");
 		JMenu pageNav = new JMenu("Navigation");
		JMenu fileHelpMenu = new JMenu("Help");
 		
 		//menu items
 		JMenuItem fileClose = new JMenuItem("Close");
 		
 		JMenuItem navHome = new JMenuItem("Home");
 		JMenuItem navCustVal = new JMenuItem("Customer Validation");
 		JMenuItem navProducts = new JMenuItem("Products");
 		JMenuItem navComplaints = new JMenuItem("Complaints");
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
     	pageNav.add(navProducts);
     	pageNav.add(navComplaints);
     	pageNav.add(navCustDetails);
     	
     	fileClose.addActionListener(navigationListener);
     	navHome.addActionListener(navigationListener);
     	navCustVal.addActionListener(navigationListener);
     	navProducts.addActionListener(navigationListener);
     	navComplaints.addActionListener(navigationListener);
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
     	
 
 		///////////////customer details fields/////////////////
 		//First Name
 		JLabel fNameLbl = new JLabel("First Name:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,10,0,10);
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
 		c.insets = new Insets(0,10,0,0);
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
 		c.insets = new Insets(0,10,0,0);
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
 		c.insets = new Insets(0,10,0,0);
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
 		c.insets = new Insets(0,10,0,0);
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
 		c.insets = new Insets(0,10,0,0);
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
 		c.insets = new Insets(0,10,0,0);
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
 		c.insets = new Insets(0,10,0,10);
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
 		c.insets = new Insets(0,10,0,0);
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
 		c.insets = new Insets(0,10,0,0);
     	pane.add(secQuestionLbl, c);
 		
 		secQuestionCombo = new JComboBox(secQuestionString);
 		//c.ipadx = 100;
 		c.weightx = 0.0;
 		c.gridx = 1;
 		c.gridy = 12;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(secQuestionCombo, c);
 		
 		
 		//Security Answer
 		JLabel secAnswerLbl = new JLabel("Security Answer:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 13;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,10,0,0);
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
     		
     	JButton addCustButton = new JButton("Add Customer");
 	    c.ipady = 20;
 	    c.weightx = 0.0;
     	c.gridx = 0;
 		c.gridy = 14;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(addCustButton, c);   
     	addCustButton.addActionListener(addCustomerListener); 	
     		
     	JButton resetButton = new JButton("Reset");
 	    c.ipady = 20;
 	    c.weightx = 0.0;
     	c.gridx = 1;
 		c.gridy = 14;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(resetButton, c);   
     	resetButton.addActionListener(resetListener); 	
     }
      
     public static void createAndShowGUI()
     {
         //Create and set up the window. Set instantiation parameters.
         frame = new JFrame("Joining");
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
             if (e.getActionCommand().equals("Complaints")) {
                 cgui = new ComplaintGUI();
 		    	cgui.createAndShowGUI();                
                 frame.dispose();
             }
             if (e.getActionCommand().equals("Products")) {
                 pgui = new ProductsGUI();
 		    	pgui.createAndShowGUI();
                 frame.dispose();
             }
             if (e.getActionCommand().equals("Customer Validation")) 
         	{
             	cvgui = new CustValidationGUI();
 				cvgui.createAndShowGUI();
             	frame.dispose();
         	}
             if (e.getActionCommand().equals("Customer Details")) {
                 cdgui = new CustDetailsGUI();
 		    	cdgui.createAndShowGUI();
                 frame.dispose();
             }
             if (e.getActionCommand().equals("Close")) {
             System.exit(0);
         	}
 		}
 	}
 	
 	static public boolean numericVal(String str)
 	{    
         if (str == null || str.length() == 0)
             return false;
                   
         for (int i = 0; i < str.length(); i++)
         {
             //If we find a non-digit character we return false.
             if (!Character.isDigit(str.charAt(i)))
                 return false;
         }    
         return true;
     }
     
     class AddCustomerListener implements ActionListener
     {
     	public void actionPerformed(ActionEvent ev)
     	{
     		Pattern telephonePattern = Pattern.compile("\\Q+\\E[0-9]{0,14}$|[0-9]{0,14}$");
     		Matcher phoneMatcher = telephonePattern.matcher(phoneNumTxt.getText());
     		
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
 		    	
 		    	boolean testFNameIn = JoiningGUI.numericVal(fNameIn);
 		    	boolean testSNameIn = JoiningGUI.numericVal(sNameIn);
 		    	
 		    	if(("".equals(fNameIn)) || ("".equals(sNameIn))||("".equals(houseNumIn))||("".equals(streetNameIn))||("".equals(cityIn))
 		    		||("".equals(countyIn))||("".equals(postCodeIn))||("".equals(emailIn))||("".equals(secQuestionIn))||("".equals(secAnswerIn)))		    			
 		    	{
 		    		JOptionPane.showMessageDialog(null,"No blank fields allowed!");
 		    	}
 		    	else if(!(testFNameIn == false) || !(testSNameIn == false))
 		    	{
 		    		JOptionPane.showMessageDialog(null,"First and last name can contain only letters.");
 		    	}
 		    	else
 		    	{
 			    	//Database insert
 			    	Connection conn = Call_Centre_Training.getConnection();
 					PreparedStatement stmt = null;
 					ResultSet rs = null;
 					
 					try
 					{
 						stmt = conn.prepareStatement("INSERT INTO customer (fName,sName,houseNo,streetName,city,county,postCode,telNo,email,secQues,secAns) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
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
 	
 						stmt.executeUpdate();
 						JOptionPane.showMessageDialog(null,"Customer added!");
 						conn.close();
 					}
 					catch(SQLException ex)
 					{
 						ex.printStackTrace();
 					}
 			        
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
 		    	}
 	    	}
     	}
     }
     
     	
    	class ResetListener implements ActionListener
     {
     	public void actionPerformed(ActionEvent ev)
     	{
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
     	}
     }	
 }
