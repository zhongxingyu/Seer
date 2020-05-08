 package view;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 public class ErrorMessage extends JFrame{
 	
 	private JLabel errorLabel;
 	public ErrorMessage(String errorType, String errorMessage) {
 		super(errorType);
 		
 		errorLabel = new JLabel(errorMessage);
 		getContentPane().add(errorLabel);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.pack();
 	}
 }
