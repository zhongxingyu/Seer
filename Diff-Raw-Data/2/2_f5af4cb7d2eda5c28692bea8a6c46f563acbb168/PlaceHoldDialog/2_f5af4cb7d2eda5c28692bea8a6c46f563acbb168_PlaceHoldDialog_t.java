 package UI;
 
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.Label;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import Transactions.Transactions;
 
 public class PlaceHoldDialog extends JFrame implements ActionListener{
 
 	JTextField callNo = new JTextField();
 //	JTextField author = new JTextField();
 //	JTextField subject = new JTextField();
 	
 	static String returnToUserDialogString = "Return to User Dialog";
 	static String placeHold = "Place hold";
 	static String bid;
 	
 	public static final int VALIDATIONERROR = 2;
 	
 	public PlaceHoldDialog(String name)
 	{
 		super (name);
 
 		
 	}
 	
 	private void addComponentsToPane(final Container pane)
 	{
 		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3, 2));
 		
 		panel.add(new Label("Place Hold"));
 		panel.add(new Label(""));
 		
 		panel.add(new Label("Call number"));
 		panel.add(callNo);
 		
 		// provide option to search ?
 		
 		JButton returnToUserDialog = new JButton(returnToUserDialogString);
 		returnToUserDialog.setActionCommand(returnToUserDialogString);
 		returnToUserDialog.addActionListener(this);
 		
 		JButton placeHoldButton = new JButton(placeHold);
 		placeHoldButton.setActionCommand(placeHold);
 		placeHoldButton.addActionListener(this);
 		
 		panel.add(returnToUserDialog);
 		panel.add(placeHoldButton);
 		
 		pane.add(panel);
 	}
     public static void createAndShowGUI(String borrowerID) {
         //Create and set up the window.
         PlaceHoldDialog frame = new PlaceHoldDialog("Search Dialog");
        // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //Set up the content pane.
         frame.addComponentsToPane(frame.getContentPane());
         //Display the window.
         frame.pack();
         frame.setVisible(true);
         bid = borrowerID;
     }
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		
 		if (returnToUserDialogString.equals(arg0.getActionCommand()))
 		{
 			this.dispose();
 			
 		}
 		else if (placeHold.equals(arg0.getActionCommand()))
 		{ 
 			if (placeHold() != VALIDATIONERROR) {
 				callNo.setText("");
 			}
 			else {
 				JOptionPane.showMessageDialog(this, "Invalid Input", "Error", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 		
 	}
 	
 	
 	private int placeHold() {
 		
 		String callNum = callNo.getText().trim();
 		
 		if (callNum.length() != 0) {
 			
 			DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");
 			Date date = new Date();
 			String currDate = dateFormat.format(date);
 			System.out.println(currDate);
 			
 			Transactions trans = new Transactions();
 			// trans.placeHold(bid, callNum, currDate);
 			return 0;
 		}
 		
 		
 
 		
 		return VALIDATIONERROR;
 	}
 }
