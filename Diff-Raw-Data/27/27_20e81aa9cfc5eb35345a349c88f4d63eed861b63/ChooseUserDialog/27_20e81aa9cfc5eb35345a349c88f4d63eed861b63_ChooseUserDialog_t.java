 package UI;
 
 import Transactions.TestData;
 import Transactions.Transactions;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.AbstractButton;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 public class ChooseUserDialog extends JPanel implements ActionListener{
 	
 	static String[] users = {"Clerk", "Borrower", "Librarian"};
 	static String okayCommand = "OkayPressed";
 	
 	private static JFrame mainFrame;
 	private JComboBox userList;
 	
 	
 	public ChooseUserDialog(){
 		userList = new JComboBox(users);
 		userList.setSelectedIndex(0);
 		userList.addActionListener(this);
 		
 		JButton okayButton = new JButton("Okay");
 		okayButton.setVerticalTextPosition(AbstractButton.CENTER);
 		okayButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		okayButton.setActionCommand(okayCommand);
 		
 		okayButton.addActionListener(this);
 		
 		this.add(userList, BorderLayout.PAGE_START);
 		this.add(okayButton, BorderLayout.PAGE_END);
 	
 	}
 	
 	private void createComboBox()
 	{
 		
 		//return userList;
 	}
 	
 	public static void createAndShowGUI()
 	{
 		mainFrame = new JFrame("Choose User!");
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		JComponent newContentPane = new ChooseUserDialog();
 		newContentPane.setOpaque(true);
 		mainFrame.setContentPane(newContentPane);
 		
 		mainFrame.pack();
 		mainFrame.setVisible(true);
 	}
 	public static void main(String[] args) {
         //Schedule a job for the event-dispatching thread:
         //creating and showing this application's GUI.
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 createAndShowGUI();
             }
         });       
         //System.out.println(Constants.DateToString(Constants.stringToDate("01-oct-2012")));
       //System.out.println(Constants.getCurrentDateInStringFormat());
         TestData data = new TestData();
         data.insertBookCopyData();
   
     }
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if (okayCommand.equals(arg0.getActionCommand()))
 		{
 			int selected = this.userList.getSelectedIndex();
 			switch(selected)
 			{
 			case 0:
 				ClerkDialog.createAndShowGUI();
 				break;
 			case 1:
 				EnterBorrowerIDDialog.createAndShowGUI();
 				break;
 			case 2:
 				LibrarianDialog.createAndShowGUI();
 				break;
 			}
 			mainFrame.dispose();
 		}
 		
 	}
 }
