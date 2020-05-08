 /**
  ******************************************************************************
 *                                CallDialog.java                                *
  ****************************************************************************** 
  * (Overview)
  * 
  *  @author simple-developer
  *  @since 5 Jul 2012
  * 
  * (Description)
  */
 package events;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JLabel;
 
 public class CallDialog extends JDialog {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private final JPanel contentPanel = new JPanel();
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		
 		// Get the native look and feel class name
 		String nativeLF = UIManager.getSystemLookAndFeelClassName();
 
 		// Install the look and feel
 		try {
 		    UIManager.setLookAndFeel(nativeLF);
 		} catch (InstantiationException e) {
 		} catch (ClassNotFoundException e) {
 		} catch (UnsupportedLookAndFeelException e) {
 		} catch (IllegalAccessException e) {
 		}
 		
 		try {
 			CallDialog dialog = new CallDialog();
 			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 			dialog.setVisible(true);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Create the dialog.
 	 */
 	public CallDialog() {
 		setTitle("Call Dialog");
 		setBounds(100, 100, 450, 300);
 		getContentPane().setLayout(new BorderLayout());
 		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
 		getContentPane().add(contentPanel, BorderLayout.CENTER);
 		contentPanel.setLayout(null);
 		
 		JLabel lblNewLabel = new JLabel("New label");
 		lblNewLabel.setBounds(181, 52, 70, 15);
 		contentPanel.add(lblNewLabel);
 		{
 			JPanel buttonPane = new JPanel();
 			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
 			getContentPane().add(buttonPane, BorderLayout.SOUTH);
 			{
 				JButton btnEndCall = new JButton("End Call");
 				btnEndCall.setActionCommand("Cancel");
 				buttonPane.add(btnEndCall);
 			}
 		}
 	}
 }
