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
 import java.util.*;
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
 import javax.imageio.ImageIO;
 
 
 public class ProductDescGUI
 {
 	static JFrame frame;
 	static JButton testButton;
 	
 	static JLabel prodTypeLbl, imageLbl, prodPicLbl;
 	
 	static JButton closeButton;
 	
 	static JTextArea prodTextArea;
 	static JScrollPane prodScrollPane;
 	
 	static JComboBox prodTypeCombo;
     static String[] prodTypeString = {"Product Type", "Tariffs", "Boilers", "Insurance"};
     static String[] prodTypeNextString = {"Products"};
     static JComboBox prodTypeNextCombo;
     
        
     static CloseListener closeListener;
     static ComboPicListener comboPicListener;
     static ComboListener comboListener;
     static NextComboListener nextComboListener;
     
     static String fileLocation = "test.jpg";
     static String currentFileLocation = "";
     static ImageIcon img = new ImageIcon(ProductDescGUI.class.getResource(fileLocation));
 	
 	public ProductDescGUI()
 	{		
 		closeListener = new CloseListener();
 		comboPicListener = new ComboPicListener();
 		comboListener = new ComboListener();
 		nextComboListener = new NextComboListener();
 	}
 
 	 
 	public static void addComponentsToPane(Container pane)
     {  
     	frame.setLocation(50,100);
     	
     	pane.setLayout(new GridBagLayout());
 	    GridBagConstraints c = new GridBagConstraints();
 	    
 	    /*prodTypeLbl = new JLabel("");
 		c.ipady = 20;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(prodTypeLbl, c);*/
     	
     	prodTypeCombo = new JComboBox(prodTypeString);
 		//c.ipadx = 100;
 		c.weightx = 0.0;
 		c.gridx = 1;
 		c.gridy = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,100,0,0);
     	pane.add(prodTypeCombo, c);
     	prodTypeCombo.addItemListener(comboListener);
     		    	
     	prodTypeNextCombo = new JComboBox(prodTypeNextString);
 		//c.ipadx = 100;
 		c.weightx = 0.0;
 		c.gridx = 1;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,100,0,0);
     	pane.add(prodTypeNextCombo, c);
     	prodTypeNextCombo.setEnabled(false);
     	prodTypeNextCombo.addItemListener(nextComboListener);
     	   	
     	prodPicLbl = new JLabel("",img,JLabel.CENTER);
     	c.ipady = 0;
     	c.ipadx = -45;
 		c.weightx = 0.1;
 		c.gridx = 2;
 		c.gridy = 2;
 		c.gridwidth = 1;
 		c.gridheight = 2;
 		c.insets = new Insets(0,0,0,0);
     	pane.add(prodPicLbl, c);
     
     	prodTextArea = new JTextArea("Product Information", 10, 40);
 	    prodTextArea.setEditable(false);
 		prodTextArea.setFont(new Font("Serif", Font.ITALIC, 16));
 		prodTextArea.setLineWrap(true);
 		prodTextArea.setWrapStyleWord(true);
 		
 		prodScrollPane = new JScrollPane(prodTextArea);
 		prodScrollPane.setVerticalScrollBarPolicy(
         JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.0;
 		c.gridx = 0;
 		c.gridy = 3;
 		c.gridwidth = 2;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,10,0);
 		pane.add(prodScrollPane, c);
 		//textArea.getDocument().addDocumentListener(this);
     	
 		closeButton = new JButton("Close");
 		c.ipady = 20;
 		c.weightx = 1.0;
 		c.gridx = 0;
 		c.gridy = 4;
 		c.gridwidth = 0;
 		c.gridheight = 1;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(0,0,0,0);
 		pane.add(closeButton, c);
 		closeButton.addActionListener(closeListener); 
     }
     
     
     public static void createAndShowGUI()
     {
         //Create and set up the window. Set instantiation parameters.
         frame = new JFrame("Product Descriptions");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(300,800);
         //frame.setLocation(50,50);
         	
     	frame.setResizable(true);
 		frame.setLocationRelativeTo(null);
 		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);			
  
         //Set up the content pane.
         addComponentsToPane(frame.getContentPane());
  
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
     
     class CloseListener implements ActionListener
     {
     	public void actionPerformed(ActionEvent e)
     	{
 			if (e.getActionCommand().equals("Close")) {
                     frame.dispose();
                 }
     	}
    	}
    	
    	//using ItemListener this allows us to see when a new item has been selected in the combo box, 
    	//this means we don't need a submit button
    	class ComboListener implements ItemListener
     {
     	public void itemStateChanged(ItemEvent evt)
     	{
     		prodTypeNextCombo.removeAllItems();
     		
     		prodTypeNextCombo.addItem(prodTypeNextString[0]);
 			prodTypeCombo = (JComboBox)evt.getSource();
 			if(evt.getStateChange() == ItemEvent.SELECTED)
 			{
 				String fileName = null;
 				//Item that has just been selected
 				String prodType = (String)prodTypeCombo.getSelectedItem();
 				if(prodType == "Product Type")
 				{
 					prodType = "";
 					prodTypeNextCombo.setEnabled(false);
 				}
 				else 
 				{
 					Connection conn = Call_Centre_Training.getConnection();
 					try
 					{					
 						PreparedStatement prodTypeStmt = conn.prepareStatement("SELECT product_name FROM products WHERE product_type =?");
 						prodTypeStmt.setString(1, prodType);
 						ResultSet prodTypeRsltSet = prodTypeStmt.executeQuery();
 						
 						ArrayList<String> productName = new ArrayList<String>();
 						while(prodTypeRsltSet.next())
 						{
 							productName.add(prodTypeRsltSet.getString("product_name"));
 						}
 								
 						String[] prodTypeNextString = new String[productName.size()];
 						prodTypeNextString = productName.toArray(prodTypeNextString);
 		
 						for(int i = 0 ; i < prodTypeNextString.length; i++)
 						{
 							prodTypeNextCombo.addItem(prodTypeNextString[i]);
 						}
 						
 						prodTypeNextCombo.setEnabled(true);
 			
 				   		conn.close();	   		
 					}
 					catch(SQLException ex)
 					{
 						ex.printStackTrace();
 					}
 				}
 			}
 			else if(evt.getStateChange() == ItemEvent.DESELECTED)
 			{
 				//The item which has just been deselected.
 			}		
     	}
    	}
    	
    	class NextComboListener implements ItemListener
    	{
     	public void itemStateChanged(ItemEvent evt)
     	{
     		//prodTypeNextCombo.removeAllItems();
 			prodTypeNextCombo = (JComboBox)evt.getSource();
 			
 			if(evt.getStateChange() == ItemEvent.SELECTED)
 			{
 				String fileLocation = null;
 				//Item that has just been selected
 				String prodName = (String)prodTypeNextCombo.getSelectedItem();
 				Connection conn = Call_Centre_Training.getConnection();
 					
 				try
 				{						
 					PreparedStatement prodNameStmt = conn.prepareStatement("SELECT * FROM products WHERE product_name =?");
 					prodNameStmt.setString(1, prodName);
 					ResultSet prodNameRsltSet = prodNameStmt.executeQuery();
 											
 					while (prodNameRsltSet.next()) 
 					{
 					  currentFileLocation = prodNameRsltSet.getString("file_location");
 				      File image = new File(currentFileLocation);
 				      FileOutputStream fos = new FileOutputStream(image);
 				      byte[] buffer = new byte[1];
 				      
 				      InputStream is = prodNameRsltSet.getBinaryStream(2);
 			
 				      while (is.read(buffer) > 0) 
 				      {
 				        fos.write(buffer);
 				      }
 				      fos.close();
 				      
 				      prodTextArea.setText("Product description: " + prodNameRsltSet.getString(4) + "\n\nProduct Price: " + prodNameRsltSet.getString(5));
 				    }						
 					
 					ImageIcon image = new ImageIcon(currentFileLocation);
 					prodPicLbl.setIcon(image);
 					conn.close();		   		
 				}
 				catch(SQLException ex)
 				{
 					ex.printStackTrace();
 				}
 				catch(FileNotFoundException ex)
 				{
 					ex.printStackTrace();
 				}
 				catch(IOException ex)
 				{
 					ex.printStackTrace();
 				}
 			}				
 			else if(evt.getStateChange() == ItemEvent.DESELECTED)
 			{
 				//The item which has just been deselected.
 			}			
     	}
    	}
    	
    	class ComboPicListener implements ActionListener
     {
     	public void actionPerformed(ActionEvent ev)
     	{
     	}
    	}
 }
