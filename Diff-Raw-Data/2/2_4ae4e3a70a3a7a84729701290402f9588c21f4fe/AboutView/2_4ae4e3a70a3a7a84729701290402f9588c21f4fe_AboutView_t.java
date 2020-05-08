 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.UIManager;
 
 /**
  * The about window
  */
 public class AboutView extends JFrame
 {
 	private static final long serialVersionUID = 1L;
 	private JLabel lbLogo;
 	private JLabel lbTitle;
 	private JLabel lbText;
 	private JButton btLicense;
 	private JButton btClose;
 	
 	private JFrame licenseView;
 
 	/**
 	 * Initialise the GUI
 	 */
 	public void initGUI()
 	{
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		
 		setIconImage(new ImageIcon("septopus.png").getImage());
 		setResizable(false);
 		
 		setTitle("About Septopus");
 		setSize(341, 240);
 		
 		setLayout(null);
 
 		lbTitle = new JLabel("Septopus", JLabel.CENTER);
 		lbTitle.setFont(new Font("Dialog", Font.PLAIN, 24));
 		lbTitle.setBounds(58, 6, 194, 24);
 		getContentPane().add(lbTitle);
 
 		lbText = new JLabel("Advanced Vocab Trainer", JLabel.CENTER);
 		lbText.setBounds(47, 36, 227, 37);
 		getContentPane().add(lbText);
 
 		lbLogo = new JLabel(new ImageIcon("septopus.png"));
 		lbLogo.setBounds(105, 72, 114, 109);
 		getContentPane().add(lbLogo);
 		
 		btLicense = new JButton("License");
 		btLicense.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				StringBuffer licenseText = new StringBuffer();
 				
 				try
 				{
					BufferedReader bufRead = new BufferedReader(new FileReader(new File("LICENSE")));
 					String line;
 					while (null != (line = bufRead.readLine()))
 					{
 						licenseText.append(line + "\n");
 					}
 					
 					bufRead.close();
 				}
 				catch (FileNotFoundException e1)
 				{
 					e1.printStackTrace();
 				}
 				catch (IOException e1)
 				{
 					e1.printStackTrace();
 				}
 				
 				licenseView = new JFrame("License");
 				licenseView.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 				licenseView.setSize(500, 300);
 				licenseView.setLayout(new BorderLayout());
 				
 				licenseView.addWindowListener(new WindowListener() {
 					public void windowActivated(WindowEvent e) {
 					}
 					public void windowClosed(WindowEvent e) {
 						// re-enable parent
 						setEnabled(true);
 					}
 					public void windowClosing(WindowEvent e) {
 					}
 					public void windowDeactivated(WindowEvent e) {
 					}
 					public void windowDeiconified(WindowEvent e) {
 					}
 					public void windowIconified(WindowEvent e) {
 					}
 					public void windowOpened(WindowEvent e) {
 						// disable parent
 						setEnabled(false);
 					}
 				});
 				
 				JTextArea textArea = new JTextArea(licenseText.toString());
 				textArea.setFont(new Font(null, 0, 10));
 				licenseView.add(new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
 				
 				JButton btOkay = new JButton("Okay");
 				btOkay.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e)
 					{
 						licenseView.dispose();
 					}
 				});
 				licenseView.add(btOkay, BorderLayout.SOUTH);
 				
 				licenseView.setVisible(true);
 			}
 		});
 		btLicense.setBounds(40, 185, 114, 20);
 		getContentPane().add(btLicense);
 		
 		btClose = new JButton("Close");
 		btClose.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e)
 			{
 				dispose();
 			}
 		});
 		btClose.setBounds(160, 185, 114, 20);
 		getContentPane().add(btClose);
 		
 		setVisible(true);
 	}
 	
 	public static void main(String[] args)
 	{
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		AboutView view = new AboutView();
 		view.initGUI();
 	}
 }
