 package package1;
 
 import javax.swing.*;
 
 import java.awt.GridLayout;
 import java.awt.event.*;
 import java.util.Date;
 public class CampFullStatus extends JDialog implements ActionListener{
 
 	/**Default serial UID */
 	private static final long serialVersionUID = 1L;
 		
 	/** table for data */
 	private JTable outputTable;
 	
 	/** scroll panel for table */
 	private JScrollPane scrollPane;
 	
 	/** label to display date given */
 	private JLabel displayDate;
 	
 	/** table model */
 	private StatusModel tableModel;
 	
 	/** OK Button */
 	private JButton OKButton;
 	
 	public void checkStatus(BetterGregorianCalendar d) {
		tableModel = new StatusModel();
 		tableModel.loadFromText("CampStatus");
 		outputTable = new JTable(tableModel);
 		scrollPane = new JScrollPane(outputTable);
 		displayDate = new JLabel("Camp status on: " + d.toString());
 		OKButton = new JButton("OK");
 		OKButton.addActionListener(this);
 		
 		JPanel panel = new JPanel();
 		panel.setLayout(new GridLayout(3,1));
 		panel.add(displayDate);
 		panel.add(scrollPane);
 		panel.add(OKButton);
 		add(panel);
 		
 		
 		
 		setSize(700,300);
 		setVisible(true);
 		
 	}
 
 	public void actionPerformed(ActionEvent event) {
 		if(event.getSource() == OKButton){
 			dispose();
 		}
 	}
 	
 	
 
 }
