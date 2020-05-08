 package NotSoEasy;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 public class AboutDialog extends JDialog {
 
 	private static final long serialVersionUID = 1L;
 
 	public AboutDialog(Component parent) {
 		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
 
 	    add(Box.createRigidArea(new Dimension(0, 10)));
 
 	    ImageIcon icon = new ImageIcon(ImageManager.getInstance().getImage("Silenzio"));
 	    JLabel label = new JLabel(icon);
 	    label.setMinimumSize(new Dimension(icon.getIconHeight(), icon.getIconWidth()));
 	    label.setAlignmentX(0.5f);
 	    add(label);
 
 	    add(Box.createRigidArea(new Dimension(0, 10)));
 
 	    JLabel name = new JLabel("Bobonne's Challenge, v1.0");
 	    name.setFont(new Font("Serif", Font.BOLD, 13));
 	    name.setAlignmentX(0.5f);
 	    add(name);
 	    
 	    JLabel copyright = new JLabel("\u00a9 2013 - Silenzio bvba");
 	    copyright.setFont(new Font("Serif", Font.BOLD, 13));
 	    copyright.setAlignmentX(0.5f);
 	    add(copyright);
 
 	    add(Box.createRigidArea(new Dimension(0, 50)));
 
 	    JButton close = new JButton("Close");
 	    close.addActionListener(new ActionListener() {
 
 	        public void actionPerformed(ActionEvent event) {
 	            dispose();
 	        }
 	    });
 
 	    close.setAlignmentX(0.5f);
 	    add(close);
 
 	    setResizable(false);
 	    setModalityType(ModalityType.APPLICATION_MODAL);
 	    setTitle("About Bobonne's Challenge");
 	    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 	    
 	    pack();
 	    setLocationRelativeTo(parent);
 	}
 	
 }
