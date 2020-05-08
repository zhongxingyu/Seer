 package ui;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 
 import transactions.ReportCheckedOut;
 import transactions.ReportCheckedOutHelper;
 import transactions.ReportMostPopular;
 
 public class InsertYearAndNumberPanel extends JPanel{
 	private List<JTextField> textFields = new ArrayList<JTextField>();	
 	public InsertYearAndNumberPanel(){
 
 		super(new GridLayout(5,1)); 
 		init();
 
 	}
 //
 	private void init(){
 
 		// Label
 		JLabel label = new JLabel("Insert Year then Number");
 		label.setHorizontalAlignment(SwingConstants.CENTER);
 		
 		// Search Book button
 		JButton addBookButton = new JButton("Submit");		
 		addBookButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				//Execute when button is pressed
 				Integer year = Integer.parseInt(textFields.get(0).getText().trim());
 				Integer number = Integer.parseInt(textFields.get(1).getText().trim());
 				
 				System.out.println(year + " " + number);
 				new ReportMostPopular(year, number);
 				System.out.println("Possibly insert subject");
 
 			}
 		});
 		
 		JButton backButton = new JButton("Back");		
 		backButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				new UserFrame();
 				//lf.dispose();
 			}
 		});
 		
 		JTextField tf = new JTextField();
 		int tf_y = HEIGHT/11;
 		int tf_h = HEIGHT/15;
 		tf.setBounds(WIDTH/2, tf_y + (HEIGHT/11 - tf_h)/2 , WIDTH/3 , tf_h);
 		textFields.add(tf);
 		
 		JTextField tf2 = new JTextField();
 		tf.setBounds(WIDTH/2, tf_y + (HEIGHT/11 - tf_h)/2 , WIDTH/3 , tf_h);
 		textFields.add(tf2);
 			
 		add(label);
 		add(tf);
 		add(tf2);
 		add(addBookButton);	
 		add(backButton);
 	}
 
 }
