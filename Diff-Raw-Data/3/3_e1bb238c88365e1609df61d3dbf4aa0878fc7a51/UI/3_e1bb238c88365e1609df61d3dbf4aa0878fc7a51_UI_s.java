package uk.ac.gla.dcs.tp3.w.ui.TestUI;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.*;
 import javax.swing.*;
 
 /**
  * @author david
  *
  */
 public class UI extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 
 	public UI() {
         initUI();
     }
 	
 	public final void initUI() {
 		 //full screen panel
 		 JPanel screenPanel = new JPanel();
 		 getContentPane().add(screenPanel);
 		 
 		 //the table panel
 	     JPanel tablePanel = new JPanel();
 
 	     //the radio buttons panel
 	     JPanel radioPanel = new JPanel();
 	     
 	    //set borders
 	     radioPanel.setBorder(BorderFactory.createEmptyBorder(40,0,0,0));
 	     screenPanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,10));
 
 	     //set layouts and add the panels to the screen panel
 	     screenPanel.setLayout(new BorderLayout());
 	     radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
 	     tablePanel.setLayout(new BorderLayout());
 	     screenPanel.add(radioPanel, BorderLayout.PAGE_START);
 	     screenPanel.add(tablePanel, BorderLayout.CENTER);
 	     
 	     
 	     //table data 
 	     String[] columnNames = {"First Name",
                  "Last Name",
                  "Sport",
                  "# of Years",
                  "Vegetarian"};
 
 	     Object[][] data = {
 	    		 {"Kathy", "Smith",
 	    			 "Snowboarding", new Integer(5), new Boolean(false)},
 	    			 {"John", "Doe",
 	    				 "Rowing", new Integer(3), new Boolean(true)},
 	    				 {"Sue", "Black",
 	    					 "Knitting", new Integer(2), new Boolean(false)},
 	    					 {"Jane", "White",
 	    						 "Speed reading", new Integer(20), new Boolean(true)},
 	    						 {"Joe", "Brown",
 	    							 "Pool", new Integer(10), new Boolean(false)}
 	     };
 
 	     final JTable table = new JTable(data, columnNames);
 	     //table.setPreferredScrollableViewportSize(new Dimension(500, 400));
 	     table.setFillsViewportHeight(true);
 	     
 	     //Create the scroll pane and add the table to it.
 	     JScrollPane scrollPane = new JScrollPane(table);
 	     //Add the scroll pane to this panel.
 	     tablePanel.add(scrollPane, BorderLayout.PAGE_START);
 	     
 	     
 	     
 	     //Create the radio buttons.
 	     JRadioButton rButton1 = new JRadioButton("First option");
 	     rButton1.setSelected(true);
 	     radioPanel.add(rButton1);
 	     radioPanel.add(Box.createRigidArea(new Dimension(20,0)));
 	     
 	     JRadioButton rButton2 = new JRadioButton("Second option");
 	     radioPanel.add(rButton2);
 	     radioPanel.add(Box.createRigidArea(new Dimension(200,0)));
 	     
 	     JRadioButton rButton3 = new JRadioButton("Third option");
 	     radioPanel.add(rButton3);
 	     radioPanel.add(Box.createRigidArea(new Dimension(20,0)));
 	     
 	     JRadioButton rButton4 = new JRadioButton("Fourth option");
 	     rButton4.setSelected(true);
 	     radioPanel.add(rButton4);
 	    
 	     //set up the quit button and other radio buttons
 	     JButton quitButton = new JButton("Quit");
 	     quitButton.setToolTipText("Click here to exit");
 	     quitButton.addActionListener(new ActionListener() {
 	         public void actionPerformed(ActionEvent event) {
 	             System.exit(0);
 	        }
 	     });
 	     radioPanel.add(Box.createRigidArea(new Dimension(50,0)));
 	     radioPanel.add(quitButton);
 	     
 
 	     //set general frame stuff
 	     setTitle("Team W UI early iteration");
 	     setSize(800, 600);
 	     setLocationRelativeTo(null);
 	     setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
 
 }
