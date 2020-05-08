 package com.andrewreisner.tractorDrive;
 import java.awt.EventQueue;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.UIManager;
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.JSplitPane;
 import javax.swing.JButton;
 import javax.swing.JTextPane;
 
 public class MainWindow {
 
     private JFrame frame;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		try {
 			for (LookAndFeelInfo info: UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())){
 					UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (Exception e) {}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainWindow window = new MainWindow();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public MainWindow() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 450, 300);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		JFileChooser fchoose = new JFileChooser();
 		fchoose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		int retval = fchoose.showOpenDialog(frame.getContentPane());
 		
 		JPanel panel = new JPanel();
 		frame.getContentPane().add(panel, BorderLayout.SOUTH);
 		panel.setLayout(new BorderLayout(0, 0));
 		
 		JLabel lblStatus = new JLabel("Ready");
 		panel.add(lblStatus, BorderLayout.WEST);
 		
 		JSplitPane splitPane = new JSplitPane();
 		splitPane.setEnabled(false);
 		panel.add(splitPane, BorderLayout.EAST);
 		
 		JButton btnUpdate = new JButton("update");
 		splitPane.setLeftComponent(btnUpdate);
 		
 		JButton btnCancel = new JButton("cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				System.exit(1);
 			}
 		});
 		splitPane.setRightComponent(btnCancel);
 		
 		JTextPane textPane = new JTextPane();
 		textPane.setContentType("text/html");
 		textPane.setEditable(false);
 		
 		JScrollPane jsp = new JScrollPane(textPane);
 		frame.getContentPane().add(jsp, BorderLayout.CENTER);
 		
 		if (retval == JFileChooser.APPROVE_OPTION) {
 			try {
 					TractorRepo tractorRepo = new TractorRepo(fchoose.getSelectedFile().getAbsolutePath());
 					tractorRepo.fetchUpdates(null);
 					StringBuilder html = new StringBuilder();
 					html.append("<html><body><h1>Current Release: " + tractorRepo.getVersion() + "</h1>");
					String[] updates = tractorRepo.getUpdates();
 					html.append("<h2>Found " +  updates.length + " updates.</h2><hr />");
 					for (int i=updates.length-1;i>=0;i--) {
 						html.append("<pre>");
 						html.append(tractorRepo.getReleaseNotes(updates[i]));
 						html.append("</pre><hr />");
 					}
 					html.append("</body></html>");
 					textPane.setText(html.toString());
 					textPane.setCaretPosition(0);
 			}catch (Exception ex){
 				ex.printStackTrace();
 				JOptionPane.showMessageDialog(frame, "Invalid TractorDrive directory!");
 				System.exit(1);
 			}
 		} else {
 			JOptionPane.showMessageDialog(frame, "You must select the tractorDrive directory to update!");
 			System.exit(1);
 		}
 		
 		
 	}
 
 }
