 package edu.segal.bradbox;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class ContactModule extends JPanel {
 	/**
 	 * Required for JPanel
 	 */
 	private static final long serialVersionUID = 1L;
 	SuperFrame superframe;
 	JLabel nameLabel = new JLabel();
 	JLabel numberLabel = new JLabel();
 	JButton callButton = new JButton("Call");
 	JButton smsButton = new JButton("Text");
 	JButton editButton = new JButton("Edit");
 	boolean fav;
 	int rank;
 	String name;
 	String number;
 
 	ContactModule(SuperFrame sf, String nm, String no, boolean f, int r) {
 		superframe = sf;
 		setName(nm);
 		setNumber(no);
 		fav = f;
 		rank = r;
 		
 		Font buttonFont = new Font("SansSerif", Font.PLAIN, 18);
 		Font labelFont = new Font("SansSerif", Font.PLAIN, 12);
 		callButton.setFont(buttonFont);
		callButton.setBackground(Constants.BRAD_BLUE);
 		smsButton.setFont(buttonFont);
 		editButton.setFont(buttonFont);
 		
 		callButton.setPreferredSize(new Dimension(140, 50));
 		editButton.setPreferredSize(new Dimension(80, 50));
 		
 		// May need to initialize look and feel here
 		
 		JPanel namePane = new JPanel();
 		namePane.setPreferredSize(new Dimension(110, 50));
 		namePane.setLayout(new BoxLayout(namePane, BoxLayout.Y_AXIS));
 		JPanel buttonPane = new JPanel();
 
 		nameLabel.setFont(labelFont);
 		numberLabel.setFont(labelFont);
 		namePane.add(nameLabel);
 		namePane.add(numberLabel);
 		
 		this.add(namePane);
 		this.add(buttonPane);
 			buttonPane.add(callButton);
 			//buttonPane.add(smsButton);
 			buttonPane.add(editButton);
 		
 		callButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				superframe.getKeypad().initiateCall(numberLabel.getText());
 			}
 		});
 		
 		editButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				superframe.openEditContact(nameLabel.getText(), numberLabel.getText(), fav, rank);
 			}
 		});
 	}
 	
 	public void setName(String nm) {
 		nameLabel.setText(nm);
 	}
 
 	public void setNumber(String no) {
 		numberLabel.setText(no);
 	}
 	
 	public void setFav(boolean f) {
 		fav = f;
 	}
 }
