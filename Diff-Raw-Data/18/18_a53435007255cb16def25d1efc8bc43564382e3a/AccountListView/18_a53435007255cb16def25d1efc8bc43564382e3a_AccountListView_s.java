 package account.view;
 import javax.swing.*; 
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import java.awt.*; 
 import java.awt.event.*;
 import java.io.IOException;
 
 import account.model.accountItem;
 import account.model.duplicateIDException;
 import account.controller.*;
 import account.model.ModelEvent;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 
 public class AccountListView extends JFrameView {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * IndextoID is used to store the ID's of various accounts
 	 * verus their position in the index. 
 	 */
 	private Hashtable<Integer, Integer> menuIndexToID;
 	private Hashtable<Integer, Integer> IDtoMenuIndex;
 	private Vector menuItems;
 	private JComboBox cb;
 	
 
 	public AccountListView(AccountListController newcontroller) {
 	//super(model, controller); 
 	super();
 	setController(newcontroller);
 	//((AccountModel)(newcontroller.getModel())).addModelListener(this);
 	menuIndexToID = new Hashtable<Integer,Integer>();
 	IDtoMenuIndex = new Hashtable<Integer,Integer>();
 	menuItems = new Vector<String>();
 	
 	//textField.setText("0"); 
 	//this.getContentPane().add(textField, BorderLayout.NORTH); 
 	JPanel buttonPanel = new JPanel();         
 	JPanel FilebuttonPanel = new JPanel();
 	Handler handler = new Handler(); 
 	JButton jButtonDollars = new JButton("DOLLARS"); 
 	jButtonDollars.addActionListener(handler); 
 	JButton jButtonYen = new JButton("YEN"); 
 	jButtonYen.addActionListener(handler);
 	JButton jButtonEuros = new JButton("EUROS"); 
 	jButtonEuros.addActionListener(handler);
 	
 	JButton jButtonSave = new JButton("SAVE"); 
 	jButtonSave.addActionListener(handler);
 	
 	JButton jButtonQuit = new JButton("QUIT"); 
 	jButtonQuit.addActionListener(handler);
 	
 	JButton jButtonWAgent = new JButton("WITHDRAW AGENT");
 	jButtonWAgent.addActionListener(handler);
 	
 	this.getContentPane().add(buttonPanel, BorderLayout.CENTER);
 	this.getContentPane().add(FilebuttonPanel, BorderLayout.SOUTH);
 	buttonPanel.add(jButtonDollars, null);
 	buttonPanel.add(jButtonYen, null);	
 	buttonPanel.add(jButtonEuros, null);
 	
 	FilebuttonPanel.add(jButtonSave, null);
 	FilebuttonPanel.add(jButtonQuit, null);
 	FilebuttonPanel.add(jButtonWAgent, null);
 	
 	
 	
 /**
  * This block is how we populate the menu items.
  */
 	
 	Hashtable<Integer, accountItem> tempList = ((AccountListController)getController()).getMenuItems();
 	Enumeration<Integer> keys = tempList.keys();
 	////j is used to count where something falls (this should be equal to the index
 	/// position in the menu, but no way to be certain (UNSAFE)
 	int j = 0;
 	while( keys.hasMoreElements() ) {
 	 	java.lang.Object i = (keys.nextElement());
 	 	String tempString = new String();
 	 	tempString += "ID: ";
 	 	tempString += (((accountItem) tempList.get(i)).getKey());
 	 	menuIndexToID.put(j, ((accountItem) tempList.get(i)).getKey());
 	 	IDtoMenuIndex.put(((accountItem) tempList.get(i)).getKey(),j);
 	 	tempString += " Name: ";
 	 	tempString += ((accountItem) tempList.get(i)).getName();
 	 	tempString += " Balance: $";
 	 	tempString += ((accountItem) tempList.get(i)).getFunds();
 	 	menuItems.add(j, tempString);
 	 	j++;
 	}
 
 /** Adding
  * A window close listener
  */
 	addWindowListener(new WindowAdapter() {
 		 public void windowClosing(WindowEvent e) {
 		
 			 System.out.println("Closing AccountListView Attempting to Write contents of memory to file");
 			 ((AccountListController) getController()).writeFile();
 		 System.exit(0);
 		        }
 		      });
 	
 	
 	
 	cb = new JComboBox(menuItems);
 	buttonPanel.add(cb);		
 	pack();
 	buttonPanel.setVisible(true);
 	this.setVisible(true);
 	System.out.println("Trying to make visible, Account list appears to be online");
 	}
 	 // Now implement the necessary event handling code 
 
 	/**
 	 * modelChanged needs to update the list
 	 * hopefully we can force an update to the comboBox
 	 * through this.
 	 * 
 	 */
 	
 	public void modelChanged(ModelEvent event) {
 	int tempIndex = getIndexOfID(event.getID());
	
 	///OKAY so we can remove an item at a particular index
 	///and replace it with something else
 	///that means we need to rebuild the string that is typically at that index position
 	accountItem tempItem = ((accountItem)((AccountListController)getController()).getMenuItems().get(event.getID()));
 	String tempString = new String();
  	tempString += "ID: ";
  	tempString += tempItem.getKey();
  	tempString += " Name: ";
  	tempString += tempItem.getName();
  	tempString += " Balance: $";
 	tempString += tempItem.getFunds();
  	menuItems.set(tempIndex, tempString);
  	cb.setSelectedIndex(tempIndex);
  	System.out.println("Should have updated the view");
 	 }
 	
 	public int getSelectedItem(){
 		return menuIndexToID.get(cb.getSelectedItem());
 	}
 	
 	public int getIndexOfID(int ID){
 		return IDtoMenuIndex.get(ID);
 	}
 
 
 	
 	 // Inner classes for Event Handling 
 	class Handler implements ActionListener { 
 		// Event handling is handled locally
 		
 		////Now it's to decide what this thing does
 		public void actionPerformed(ActionEvent e) {
 			System.out.println(e.getActionCommand());			
 			//Object contents = cb.getSelectedItem();
 			
 			//This asks what line was selected, and it gives us
 			//the account ID of the corresponding line. I hope. 
 			Object contents = menuIndexToID.get(cb.getSelectedIndex());
 			System.out.print(((Number)contents).intValue());
 			((AccountListController)getController()).operation(e.getActionCommand(), ((Number)contents).intValue()); 
 			
 	    } 
 	}
 	
 	public void closeAction(){
 		System.out.println("You closed a window!");
 		
 		
 	}
 	
 
 	
 	public static void main(String [] args) throws IOException, BadLineInFile, NumberFormatException, duplicateIDException {
 		//AccountController testAccountController = new AccountController(); 
 		//AccountModel testModel = new AccountModel();
 		//AccountListView testListView = new AccountListView();
 		AccountListController listController = new AccountListController(args[0]);
 	
 		
 	}
 }
