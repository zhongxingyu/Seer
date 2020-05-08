 package liftbot;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JTextPane;
 import javax.swing.JLabel;
 import java.awt.Font;
 import javax.swing.SwingConstants;
 import java.awt.Toolkit;
 
 public class About extends JFrame {
 
 	private JPanel contentPane;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					About frame = new About();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public About() {
 		setIconImage(Toolkit.getDefaultToolkit().getImage(About.class.getResource("/liftbot/res/icon.png")));
 		setResizable(false);
 		setTitle("About LiftBot");
 		setBounds(100, 100, 450, 420);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 		
 		JTextPane txtpnLiftbotV = new JTextPane();
 		txtpnLiftbotV.setEditable(false);
		txtpnLiftbotV.setText("LiftBot v. 1.0.3\r\nWritten by Eric Lujan as an executable JAR file for Java SE 1.7.\r\nPorted from LiftBot for Ruby (also written by Eric Lujan.)\r\n\r\nBased on research by Eric Lujan and Jake Lee conducted at the University of Nevada, Las Vegas.\r\n\r\nPlease send feedback, questions, and comments to lujane@unlv.nevada.edu.\r\n\r\nNote: LiftBot is a work-in-progress, and is based on perfectly ideal conditions. Although it may get pretty close to the actual results, it cannot perfectly predict all of the values. It is not to be relyed completely upon for your balloon launch, but to be used as a reference for further research. Presented as-is without any warranty. \r\n\r\nFor more information about Flying Apple Space Technologies, please see http://team.flyapple.org/.\r\n\r\nFor the latest LiftBot source code and other programs from Flying Apple Space Technologies, please visit\r\nhttp://github.com/ericluwolf/fast/.\r\n\r\nFor legal and license information, please see Help > Legal.");
 		contentPane.add(txtpnLiftbotV, BorderLayout.CENTER);
 	}
 
 }
