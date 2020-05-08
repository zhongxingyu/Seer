 package view.dialogs;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import controller.DisposeAction;
 
 public class AboutDialog extends JDialog {
     private static final long serialVersionUID = -5201660923822684104L;
     
     private JButton ok = new JButton("OK");
     private JPanel bottom = new JPanel();
     private JPanel content = new JPanel();
 
     public AboutDialog(JFrame parent) {
 	super(parent, "3Paint - informacje");
 	setLayout(new BorderLayout());
 	setSize(500, 400);
 	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 	setLocation(d.width/4, d.height/4);
 	setResizable(false);
 	setAlwaysOnTop(true);
 	
 	//bottom bar with OK button
 	ok.addActionListener(new DisposeAction(this));
 	bottom.setLayout(new FlowLayout(FlowLayout.RIGHT));
 	bottom.add(ok);
 	add(BorderLayout.SOUTH, bottom);
 	
 	//logo on the left side
 	BufferedImage img = null;
 	try {
 	    URL url = getClass().getResource("/resources/images/about_logo.png");
 	    img = ImageIO.read(url);
 	} catch (IOException e) {
 	    e.printStackTrace();
 	}
 	add(BorderLayout.WEST, new JLabel(new ImageIcon(img)));
 	
 	//main content panel
 	content.setBorder(BorderFactory.createLoweredBevelBorder());
 	content.setBackground(Color.WHITE);
	content.add(new JLabel("<html><font size='7'><b>3Paint</b></font><br /><hr /></html>"));
 	add(content);
 	
 	setVisible(true);
     }
 }
