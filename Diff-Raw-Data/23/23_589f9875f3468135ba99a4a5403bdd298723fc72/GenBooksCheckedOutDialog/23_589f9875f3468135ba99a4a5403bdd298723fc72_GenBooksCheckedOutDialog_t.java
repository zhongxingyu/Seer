 package UI;
 
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.Label;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import Objects.BookCopy;
 import Objects.Borrowing;
 import Transactions.Transactions;
 
 public class GenBooksCheckedOutDialog extends JFrame implements ActionListener{
 
 	JTextField subject = new JTextField();
 	
 	static String returnToUserDialogString = "Return to User Dialog";
 	static String genReport = "Generate Report";
 	
 	public static final int VALIDATIONERROR = 2;
 	
 	public GenBooksCheckedOutDialog(String name)
 	{
 		super (name);		
 	}
 	
 	private void addComponentsToPane(final Container pane)
 	{
 		JPanel panel = new JPanel();
 		panel.setLayout(new GridLayout(3, 2));
 		
 		panel.add(new Label("Provide Subject (optional)"));
 		panel.add(subject);
 		
 		panel.add(new Label(""));
 		panel.add(new Label(""));
 //		panel.add(new Label("Call"));
 //		panel.add(year);
 //		
 //		panel.add(new Label("Number"));
 //		panel.add(number);
 //			
 		JButton returnToUserDialog = new JButton(returnToUserDialogString);
 		returnToUserDialog.setActionCommand(returnToUserDialogString);
 		returnToUserDialog.addActionListener(this);
 		
 		JButton generateReport = new JButton(genReport);
 		generateReport.setActionCommand(genReport);
 		generateReport.addActionListener(this);
 //		JButton genReportButton = new JButton(genReport);
 //		genReportButton.setActionCommand(genReport);
 //		genReportButton.addActionListener(this);
 //		
 		panel.add(returnToUserDialog);
 		panel.add(generateReport);
 //		panel.add(genReportButton);
 		
 		pane.add(panel);
 	}
     public static void createAndShowGUI() {
         //Create and set up the window.
         GenBooksCheckedOutDialog frame = new GenBooksCheckedOutDialog("Search Dialog");
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //Set up the content pane.
         frame.addComponentsToPane(frame.getContentPane());
         //Display the window.
         frame.pack();
         frame.setVisible(true);
     }
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		
 		if (returnToUserDialogString.equals(arg0.getActionCommand()))
 		{
 			this.dispose();
 			
 		}
 		else if (genReport.equals(arg0.getActionCommand()))
 		{ 
 			Transactions t = new Transactions();
 			List<Borrowing> outBooks = t.showCheckedOutBorrowing();
 			
 			String subjectStr;
 			if (subject.getText().trim().length() > 0)
 			{
 				subjectStr = subject.getText().trim();
				//List<Borrowing> tempList = outBooks;
				List<Borrowing> toRemove = new ArrayList<Borrowing>();
 				List<BookCopy> bcs = t.showBookSearch("", "", subjectStr);
				
				for (int i = 0; i < outBooks.size(); i++)
 				{
					if (!keepBorrowing(outBooks.get(i), bcs))
 					{
						toRemove.add(outBooks.get(i));
 					}
 				}
				for (Borrowing b : toRemove)
				{
					outBooks.remove(b);
				}
				
 				LibrarianDisplayCheckedOutBooks.createAndShowGUI(outBooks);
 			}else
 			{
 				LibrarianDisplayCheckedOutBooks.createAndShowGUI(outBooks);
 			}	
 		}	
 	}
 	
 	private List<Borrowing> orderByCallNumber(List<Borrowing> b)
 	{
 		return null;
 	}
 	
 	private boolean keepBorrowing(Borrowing b, List<BookCopy> bcs)
 	{
 		for (BookCopy c : bcs)
 		{
 			if (b.callNumber.equals(c.callNumber))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	
 }
