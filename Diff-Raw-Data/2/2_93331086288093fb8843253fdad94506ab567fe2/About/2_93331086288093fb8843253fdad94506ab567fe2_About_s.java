 package org.plugins.xperia2011bootloaderlockstatus;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 import org.lang.Language;
 import org.system.OS;
 
 public class About extends JDialog {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private final JPanel contentPanel = new JPanel();
 	public static final String version = "1.0";
 	public static final String msg1 = "Bootloader Lock Status Checker (for Xperia 2011 devices)";
	public static final String msg2 = "By DooMLoRD";
 
 
 	/**
 	 * Create the dialog.
 	 */
 	public About() {
 		setTitle("About");
 		setModalityType(ModalityType.APPLICATION_MODAL);
 		setModal(true);
 		setResizable(false);
 		setAlwaysOnTop(true);
 		setBounds(100, 100, 374, 217);
 		getContentPane().setLayout(new BorderLayout());
 		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
 		getContentPane().add(contentPanel, BorderLayout.CENTER);
 		contentPanel.setLayout(null);
 		{
 			JLabel lblName = new JLabel(msg1);
 			lblName.setHorizontalAlignment(SwingConstants.CENTER);
 			lblName.setBounds(10, 11, 348, 14);
 			contentPanel.add(lblName);
 		}
 		{
 			JLabel lblVersion = new JLabel("Version "+version);
 			lblVersion.setHorizontalAlignment(SwingConstants.CENTER);
 			lblVersion.setBounds(10, 36, 348, 14);
 			contentPanel.add(lblVersion);
 		}
 		{
 			
 			JLabel lblJavaVersion = new JLabel("Java Version " + System.getProperty("java.version"));
 			lblJavaVersion.setHorizontalAlignment(SwingConstants.CENTER);
 			lblJavaVersion.setBounds(10, 61, 348, 14);
 			contentPanel.add(lblJavaVersion);
 		}
 		{
 			JLabel lblFrom = new JLabel(msg2);
 			lblFrom.setHorizontalAlignment(SwingConstants.CENTER);
 			lblFrom.setBounds(10, 111, 348, 14);
 			contentPanel.add(lblFrom);
 		}
 		JLabel lblOs = new JLabel("OS Version "+OS.getVersion());
 		lblOs.setHorizontalAlignment(SwingConstants.CENTER);
 		lblOs.setBounds(10, 86, 348, 14);
 		contentPanel.add(lblOs);
 		{
 			JPanel buttonPane = new JPanel();
 			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
 			getContentPane().add(buttonPane, BorderLayout.SOUTH);
 			{
 				JButton okButton = new JButton("OK");
 				okButton.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent arg0) {
 						dispose();
 					}
 				});
 				okButton.setActionCommand("OK");
 				buttonPane.add(okButton);
 				getRootPane().setDefaultButton(okButton);
 			}
 		}
 		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		setLanguage();
 	}
 	
 	public void setLanguage() {
 		Language.translate(this);
 	}
 
 }
