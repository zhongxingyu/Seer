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
 
 public class CustValidationGUI
 {
 	View v;
 	ComplaintGUI cgui;
 	ProductsGUI pgui;
 	JoiningGUI jgui;
 	CustDetailsGUI cdgui;
 	
 	static JFrame frame;
 	static JMenuBar menuBar;
 	static JTextField sNameTxt, houseNoTxt, postCodeTxt, secQuestionTxt, secAnsTxt;
 	static JButton validateBtn, submitBtn;
 	
 	static NavigationListener navigationListener;
 	static ValidateListener validateListener;
 	static SubmitListener submitListener;
 	
 	public CustValidationGUI()
     { 		 	
     	navigationListener = new NavigationListener(); 
     	validateListener = new ValidateListener();
     	submitListener = new SubmitListener();
     }
     
     
     public static void addComponentsToPane(Container pane)
     {
     	// Get the default toolkit
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 
 		// Get the current screen size
 		Dimension scrnsize = toolkit.getScreenSize();
 		double width = (scrnsize.getWidth() / 2) - 150;
 		double height = (scrnsize.getHeight() / 2) - 150;
     	
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
 		JMenuItem navComplaints = new JMenuItem("Complaints");
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
     	pageNav.add(navComplaints);
     	pageNav.add(navProducts);
     	pageNav.add(navJoining);
     	pageNav.add(navCustDetails);
     	
     	fileClose.addActionListener(navigationListener);
     	navHome.addActionListener(navigationListener);
     	navComplaints.addActionListener(navigationListener);
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
     	JButton homeValButton = new JButton("Home");
 	    c.ipady = 20;
 	    c.weightx = 0.0;
     	c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(homeValButton, c);   
     	homeValButton.addActionListener(navigationListener); 	
     		
     	JLabel sNameLbl = new JLabel("Surname:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridy = 2;
 		c.gridx = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(10,20,0,5);
     	pane.add(sNameLbl, c);
 	
 		
 		sNameTxt = new JTextField(20);
 		sNameTxt.setFont(new Font("Serif", Font.ITALIC, 16));
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 2;
 		c.gridx = 1;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(10,0,0,20);
 		c.fill = GridBagConstraints.HORIZONTAL;
     	pane.add(sNameTxt, c);	
     		
     	JLabel houseNoLbl = new JLabel("House Number:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridy = 3;
 		c.gridx = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(10,20,0,5);
     	pane.add(houseNoLbl, c);
 		
 		
 		houseNoTxt = new JTextField(20);
 		houseNoTxt.setFont(new Font("Serif", Font.ITALIC, 16));
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 3;
 		c.gridx = 1;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(10,0,0,20);
 		c.fill = GridBagConstraints.HORIZONTAL;
     	pane.add(houseNoTxt, c);	
     		
     	JLabel postCodeLbl = new JLabel("Post Code:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridy = 4;
 		c.gridx = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(10,20,0,5);
     	pane.add(postCodeLbl, c);
 	
 		
 		postCodeTxt = new JTextField(20);
 		postCodeTxt.setFont(new Font("Serif", Font.ITALIC, 16));
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 4;
 		c.gridx = 1;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(10,0,0,20);
 		c.fill = GridBagConstraints.HORIZONTAL;
     	pane.add(postCodeTxt, c);	
     		
     	submitBtn = new JButton("Submit");
 	    c.ipady = 20;
 	    c.weightx = 0.0;
     	c.gridx = 0;
 		c.gridy = 5;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(10,0,0,0);
     	pane.add(submitBtn, c);   
     	submitBtn.addActionListener(submitListener);
     	
     	JLabel secQuestionLbl = new JLabel("Security Question:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridy = 6;
 		c.gridx = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(10,20,0,5);
     	pane.add(secQuestionLbl, c);
     	
     	secQuestionTxt = new JTextField(20);
     	secQuestionTxt.setEditable(false);
 		secQuestionTxt.setFont(new Font("Serif", Font.ITALIC, 16));
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 6;
 		c.gridx = 1;
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.insets = new Insets(10,0,0,20);
 		c.fill = GridBagConstraints.HORIZONTAL;
     	pane.add(secQuestionTxt, c);
     	
     	JLabel secAnsLbl = new JLabel("Security Answer:");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridy = 7;
 		c.gridx = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(10,20,0,5);
     	pane.add(secAnsLbl, c);
     	
     	secAnsTxt = new JTextField(20);
 		secAnsTxt.setFont(new Font("Serif", Font.ITALIC, 16));
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridy = 7;
 		c.gridx = 1;
 		c.gridwidth = 3;
 		c.gridheight = 1;
 		c.insets = new Insets(10,0,0,20);
 		c.fill = GridBagConstraints.HORIZONTAL;
     	pane.add(secAnsTxt, c);		
     	secAnsTxt.setEnabled(false);
     		
     	validateBtn = new JButton("Validate");
 	    c.ipady = 20;
 	    c.weightx = 0.0;
     	c.gridx = 0;
 		c.gridy = 8;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(10,0,0,0);
     	pane.add(validateBtn, c);   
     	validateBtn.setEnabled(false);
     	validateBtn.addActionListener(validateListener);
     }
     
     
     public static void createAndShowGUI() {
         //Create and set up the window. Set instantiation parameters.
         frame = new JFrame("Customer Validation");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         	
     	frame.setResizable(false);
 		frame.setLocationRelativeTo(null);
 		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);			
  
         //Set up the content pane.
         addComponentsToPane(frame.getContentPane());
  
         //Display the window.
         frame.pack();
         frame.setVisible(true);
         
         //fetchCustHistButton.addActionListener(fetchCustHistListener);	
     }
     
     class SubmitListener implements ActionListener
     {
     	public void actionPerformed(ActionEvent e)
     	{		
 
     		if(("".equals(sNameTxt.getText())) || ("".equals(houseNoTxt.getText()))||("".equals(postCodeTxt.getText())))
     		{   			
     			JOptionPane.showMessageDialog(null,"No blank fields allowed!","Validation Warning",JOptionPane.WARNING_MESSAGE);
     		}
     		else
     		{	
     			String regexp="^([A-PR-UWYZ](([0-9](([0-9]|[A-HJKSTUW])?)?)|([A-HK-Y][0-9]([0-9]|[ABEHMNPRVWXY])?)) [0-9][ABD-HJLNP-UW-Z]{2})|GIR 0AA$";
 				Pattern pattern = Pattern.compile(regexp);
 				Matcher matcher = pattern.matcher(postCodeTxt.getText().toUpperCase());
 				
 				if(matcher.matches())
 				{
 	    			Connection conn = Call_Centre_Training.getConnection();
 					PreparedStatement stmt = null;
 					ResultSet rs = null;
 					
 					try
 					{
 						stmt = conn.prepareStatement("SELECT secQues FROM customer WHERE sName=? AND houseNo=? AND postCode=?");
 						stmt.setString(1, sNameTxt.getText());
 						stmt.setString(2, houseNoTxt.getText());
 						stmt.setString(3, postCodeTxt.getText());
 						rs = stmt.executeQuery();
 		
 						boolean found = rs.next();					
 						if (!found)
 						{
 						  	JOptionPane.showMessageDialog(null,"No existing Customer!","Validation Warning",JOptionPane.WARNING_MESSAGE);
 						  	sNameTxt.setText("");
 						  	houseNoTxt.setText("");
 						  	postCodeTxt.setText("");
 						  	secQuestionTxt.setText("");
 						  	secAnsTxt.setText("");	
 						}
 						else
 						{
 							secQuestionTxt.setText(rs.getString(1));
 							houseNoTxt.setEnabled(false);
 							sNameTxt.setEnabled(false);	
 							postCodeTxt.setEnabled(false);
 							submitBtn.setEnabled(false);
 							secAnsTxt.setEnabled(true);
 							validateBtn.setEnabled(true);							
 						}
 						conn.close();
 					}
 					catch(SQLException ex)
 					{
 						ex.printStackTrace();
 					}
 	    		}
     			else
 	    		{
 	    			JOptionPane.showMessageDialog(null,"Incorrect Post Code Format!","Validation Warning",JOptionPane.WARNING_MESSAGE);
 	    		}
     		}
     	}
     }
     
     class ValidateListener implements ActionListener
     {
     	public void actionPerformed(ActionEvent e)
     	{
     			Connection conn = Call_Centre_Training.getConnection();
 				PreparedStatement stmt = null;
 				ResultSet rs = null;
 				ResultSet rs2 = null;
 				
 				try
 				{
 					stmt = conn.prepareStatement("SELECT * FROM customer WHERE sName=? AND houseNo=? AND postCode=? AND secAns=?");
 					stmt.setString(1, sNameTxt.getText());
 					stmt.setString(2, houseNoTxt.getText());
 					stmt.setString(3, postCodeTxt.getText());
 					stmt.setString(4, secAnsTxt.getText());
 					rs = stmt.executeQuery();
 	
 					boolean found = rs.next();					
 					if (!found)
 					{
 					  	JOptionPane.showMessageDialog(null,"Incorrect security answer, please try again.","Validation Warning",JOptionPane.WARNING_MESSAGE);
 					  	secAnsTxt.setText("");
 					}
 					else
 					{					
 						String cID = rs.getString("cust_id");
 					
 						JOptionPane.showMessageDialog(null,"Customer validated, please continue with call. Customer ID: " + cID);	
 						sNameTxt.setText("");
 					  	houseNoTxt.setText("");
 					  	postCodeTxt.setText("");
 					  	secQuestionTxt.setText("");
 					  	secAnsTxt.setText("");
 					  	
 						houseNoTxt.setEnabled(true);
 						sNameTxt.setEnabled(true);	
 						postCodeTxt.setEnabled(true);
 						submitBtn.setEnabled(true);
 						secAnsTxt.setEnabled(false);
 						validateBtn.setEnabled(false);			
 					}
 					conn.close();
 				}
 				catch(SQLException ex)
 				{
 					ex.printStackTrace();
 				}
     	}
     }
     
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
             if (e.getActionCommand().equals("Joining")) {
                 jgui = new JoiningGUI();
 		    	jgui.createAndShowGUI();
                 frame.dispose();
             }
             if (e.getActionCommand().equals("Products")) {
                 pgui = new ProductsGUI();
 		    	pgui.createAndShowGUI();
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
 }
