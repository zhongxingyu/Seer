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
 
 public class View
 {
 	static NavigationListener navigationListener;
 	
 	static JFrame frame;
 	static JMenuBar menuBar;
 	
 	static String fileLocation = "logo.jpg";
     static ImageIcon img = new ImageIcon(View.class.getResource("logo.jpg"));
 
 	ComplaintGUI cgui;
 	ProductsGUI pgui;
 	JoiningGUI jgui;
 	CustDetailsGUI cdgui;
 	CustValidationGUI cvgui;
 
     public View()
     {  
     	//listener object
      	navigationListener = new NavigationListener();
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
 		
 		//menu items
 		JMenuItem fileClose = new JMenuItem("Close");
 		
 		JMenuItem navcomplaints = new JMenuItem("Complaints");
 		JMenuItem navValidation = new JMenuItem("Customer Validation");
 		JMenuItem navProducts = new JMenuItem("Products");
 		JMenuItem navJoining = new JMenuItem("Joining");
 		JMenuItem navCustDetails = new JMenuItem("Customer Details");
 		
 	
 	    pane.setLayout(new GridBagLayout());
 	    GridBagConstraints c = new GridBagConstraints();
 	    
 		//menu bar
 		menuBar = new JMenuBar();
 		pane.add(menuBar);
 		//pane.setJMenuBar(menuBar);
 		menuBar.add(fileMenu);
 		menuBar.add(pageNav);
 		
 		//file menu
 		fileMenu.add(fileClose);
 		
 		//nav menu
 		pageNav.add(navValidation);
 		pageNav.add(navcomplaints);
 		pageNav.add(navProducts);
 		pageNav.add(navJoining);
 		pageNav.add(navCustDetails);
 		
 		fileClose.addActionListener(navigationListener);
 		navValidation.addActionListener(navigationListener);
 		navcomplaints.addActionListener(navigationListener);
 		navProducts.addActionListener(navigationListener);
 		navJoining.addActionListener(navigationListener);
 		navCustDetails.addActionListener(navigationListener);
 		
 		
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
 		
 		    
 	   	JLabel prodPicLbl = new JLabel("",img,JLabel.CENTER);
     	c.ipady = 0;
     	c.ipadx = 0;
 		c.weightx = 1.0;
 		c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.gridheight = 2;
 		c.insets = new Insets(15,10,10,15);
     	pane.add(prodPicLbl, c);
 		
 	 	//add buttons
 		JButton custValButton = new JButton("Customer Validation");
 	    c.ipadx = 300;
 	    c.ipady = 20;
 	    c.weightx = 0.0;
 		c.gridx = 1;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(15,10,15,10);
 		pane.add(custValButton, c);   
 		custValButton.addActionListener(navigationListener); 
 			
 		JButton complaintButton = new JButton("Complaints");
 	    c.ipady = 20;
 	    c.weightx = 0.0;
 		c.gridx = 1;
 		c.gridy = 2;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,10,200,10);
 		pane.add(complaintButton, c);   
 		complaintButton.addActionListener(navigationListener);
 		
 		JButton prodButton = new JButton("Products");
 	    c.ipady = 20;
 	    c.weightx = 0.0;
 		c.gridx = 1;
 		c.gridy = 3;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(-315,10,15,10);
 		pane.add(prodButton, c);   
 		prodButton.addActionListener(navigationListener);
 		
 		JButton joiningButton = new JButton("Joining");
 	    c.ipady = 20;
 	    c.weightx = 0.0;
 		c.gridx = 1;
 		c.gridy = 4;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(-200,10,15,10);
 		pane.add(joiningButton, c);   
 		joiningButton.addActionListener(navigationListener);
 				
 		JButton custDetailsButton = new JButton("Customer Details");
 	    c.ipady = 20;
 	    c.weightx = 0.0;
 		c.gridx = 1;
 		c.gridy = 6;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(-90,10,15,10);
 		pane.add(custDetailsButton, c);   
 		custDetailsButton.addActionListener(navigationListener); 
 		
 		
 		
 	}
     
     
     public static void createAndShowGUI() {
         //Create and set up the window. Set instantiation parameters.
         frame = new JFrame("Call Centre Training");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
     	frame.setResizable(false);
 		frame.setLocationRelativeTo(null);
 		
 		frame.setSize(400,400);		
  
         //Set up the content pane.
         addComponentsToPane(frame.getContentPane());
  
         //Display the window.
         frame.pack();
         frame.setVisible(true);
         
         //fetchCustHistButton.addActionListener(fetchCustHistListener);	
     }
     
     
     //event listeners
     class NavigationListener implements ActionListener
      {
 	     public void actionPerformed(ActionEvent e)
 	     {
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
 }
