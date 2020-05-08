 package database;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: kevingoy
  * Date: 10.01.13
  * Time: 16:41
  * To change this template use File | Settings | File Templates.
  */
 public class EditBookPopUp extends JFrame {
 
 
 	public EditBookPopUp(CheckURL db){
 		setTitle("Buch aendern.");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setVisible(true);
 		setSize(400, 400);
 
 		JLabel titleLabel = new JLabel("Titel");
 		final JTextField titleTextField = new JTextField();
 		JLabel priceLabel = new JLabel("Preis");
 		final JTextField priceTextField = new JTextField();
 		JButton okButton = new JButton("OK");
 		okButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent actionEvent) {
 				String title = titleTextField.getText();
 				Float price;
 				if (priceTextField.getText().equals(null)) {
 					price = Float.parseFloat(priceTextField.getText());
 				} else {
 					price = null;
 				}
 				if(title == "" && price == null){
 						setVisible(false);
 				}else{
 					if(title != ""){
 						//alter title
 					}
 					if(price == null){
 						//alter price
 					}
 					setVisible(false);
 				}
 
 			}
 		});
 		JButton cancelButton= new JButton("Cancel");
 		cancelButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent actionEvent) {
 				setVisible(false);
 			}
 		});
 
 		setLayout(new GridLayout(0,2));
 		add(titleLabel);
 		add(titleTextField);
 
 		add(priceLabel);
 		add(priceTextField);
 
 		add(cancelButton);
 		add(okButton);
 	}
 
 
 }
 
