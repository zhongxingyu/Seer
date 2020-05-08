 package UI;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.Label;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.AbstractButton;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class ClerkDialog extends JFrame implements ActionListener{
 	
 	static JFrame mainFrame;
 	static String addBorrowerCommand = "addBorrowerPressed";
 	static String checkOutItemsCommand = "checkOutItemsPressed";
 	static String processReturnCommand = "processReturnPressed";
 	static String checkOverdueCommand = "overduePressed";
 	
 	static String returnToChooseUserDialogCommand = "Return to User Dialog";
 	JTextField returnField = new JTextField();
 	public ClerkDialog(String name)
 	{
 		super (name);
 	}
 	
 	private void addComponentsToPane(final Container pane)
 	{
 		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 3));
 		
 		JButton addBorrowerButton = new JButton("Add Borrower");
 		addBorrowerButton.setVerticalTextPosition(AbstractButton.CENTER);
 		addBorrowerButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		addBorrowerButton.setActionCommand(addBorrowerCommand);
 		addBorrowerButton.addActionListener(this);
 		
 		JButton checkOutItems = new JButton("Check Out Items");
 		checkOutItems.setVerticalTextPosition(AbstractButton.CENTER);
 		checkOutItems.setHorizontalTextPosition(AbstractButton.CENTER);
 		checkOutItems.setActionCommand(checkOutItemsCommand);
 		checkOutItems.addActionListener(this);
 		
 		JButton overdueButton = new JButton ("Check Overdues");
 		overdueButton.setVerticalAlignment(AbstractButton.CENTER);
 		overdueButton.setHorizontalAlignment(AbstractButton.CENTER);
 		overdueButton.setActionCommand(checkOverdueCommand);
 		overdueButton.addActionListener(this);
 		
 		panel.add(addBorrowerButton);
 		panel.add(checkOutItems);
 		panel.add(overdueButton);
 		
 		JButton processReturn = new JButton ("Process Return");
 		processReturn.setVerticalAlignment(AbstractButton.CENTER);
 		processReturn.setHorizontalAlignment(AbstractButton.CENTER);
 		processReturn.setActionCommand(processReturnCommand);
 		processReturn.addActionListener(this);
 		
 		panel.add(new Label("Enter call number to process return"));
 		panel.add(returnField);
 		panel.add(processReturn);
 		
 		JButton backButton = new JButton ("Return to Choose User Dialog");
 		backButton.setVerticalAlignment(AbstractButton.CENTER);
 		backButton.setHorizontalAlignment(AbstractButton.CENTER);
 		backButton.setActionCommand(processReturnCommand);
 		backButton.addActionListener(this);
 		
 		
 		pane.add(panel);
 		
 	}
 	public static void createAndShowGUI()
 	{
 		  //Create and set up the window.
         ClerkDialog frame = new ClerkDialog("Clerk Dialog");
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //Set up the content pane.
         frame.addComponentsToPane(frame.getContentPane());
         //Display the window.
         frame.pack();
         frame.setVisible(true);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if (ClerkDialog.addBorrowerCommand.equals(arg0.getActionCommand()))
 		{
 			//this.setEnabled(false);
 			AddBorrowerDialog.createAndShowGUI();
 		}else if (ClerkDialog.checkOutItemsCommand.equals(arg0.getActionCommand()))
 		{
 			CheckOutItemsDialog.createAndShowGUI();
 		}else if (ClerkDialog.processReturnCommand.equals(arg0.getActionCommand()))
 		{
 			//TODO do something with returnField.getText();
 			//returnField.setText("");
 			
 			ProcessReturnsDialog.createAndShowGUI();
 		}else if (ClerkDialog.checkOverdueCommand.equals(arg0.getActionCommand()))
 		{
 			CheckOverduesDialog.createAndShowGUI();
 		}
 		
 	}
 	
 
 }
