 package de.runinho.maneger;
 
 import java.awt.BorderLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import java.awt.Font;
 import javax.swing.JTextArea;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 public class errorGui extends JDialog {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private final JPanel contentPanel = new JPanel();
 	private JTextArea textArea;
 
 	public void addMassage(String massage){
 		textArea.append(massage);
 	}
 	public void addMassageln(String massage){
 		textArea.append(massage);
		textArea.append(System.lineSeparator());
 	}
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		try {
 			errorGui dialog = new errorGui("test");
 			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 			dialog.setVisible(true);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Create the dialog.
 	 */
 	public errorGui(String message) {
 		setBounds(100, 100, 525, 427);
 		getContentPane().setLayout(new BorderLayout());
 		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
 		getContentPane().add(contentPanel, BorderLayout.CENTER);
 		contentPanel.setLayout(new BorderLayout(0, 0));
 		{
 			textArea = new JTextArea();
 			textArea.setEditable(false);
 			textArea.setFont(new Font("Monospaced", Font.PLAIN, 28));
 			textArea.setText(message);
 			textArea.append(String.valueOf(textArea.getWidth()));
 			contentPanel.add(textArea);
 		}
 		{
 			JPanel buttonPane = new JPanel();
 			getContentPane().add(buttonPane, BorderLayout.SOUTH);
 			buttonPane.setLayout(new BorderLayout(0, 0));
 			{
 				JButton okButton = new JButton("OK");
 				okButton.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent arg0) {
 						dispose();
 					}
 				});
 				okButton.setFont(new Font("Tahoma", Font.PLAIN, 40));
 				okButton.setActionCommand("OK");
 				buttonPane.add(okButton);
 				getRootPane().setDefaultButton(okButton);
 			}
 		}
 	}
 
 }
