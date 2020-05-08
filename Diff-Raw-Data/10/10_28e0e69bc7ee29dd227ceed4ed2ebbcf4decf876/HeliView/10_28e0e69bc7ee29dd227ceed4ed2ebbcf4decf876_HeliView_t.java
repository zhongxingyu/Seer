 package iballs;
 
 import java.awt.BorderLayout;
 import java.awt.Image;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 @SuppressWarnings("serial")
 public class HeliView extends JFrame {
 
 	private JPanel contentPane;
 
 	Image heli;
 	public HeliView() {
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(10, 10, 1270, 990);
 		
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 				
 		ImageIcon icon = new ImageIcon("images/heli.jpg");
 		icon.setImage(icon.getImage().getScaledInstance(1260, 980, Image.SCALE_SMOOTH));
 		JLabel jLabel = new JLabel(icon);		
 		contentPane.add(jLabel, BorderLayout.CENTER);
 	}
 }
