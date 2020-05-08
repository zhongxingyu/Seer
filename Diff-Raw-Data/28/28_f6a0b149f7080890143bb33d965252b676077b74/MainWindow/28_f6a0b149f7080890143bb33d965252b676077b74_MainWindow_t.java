 package gui.admin;
 
 import java.awt.BorderLayout;
import java.awt.Dimension;
 import java.awt.Frame;
import java.awt.Toolkit;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 
 
 
 public class MainWindow extends JFrame {
 	private static final long serialVersionUID = 1L;
 
 
 	public MainWindow(List<JPanel> adminPanels) {
 		Frame[] frames = getFrames();
 		LoginDialog loginDlg = new LoginDialog(frames[0]);
 		loginDlg.setVisible(true);
		setSize(600, 600);
		setTitle("Administrasjon for bildeviser");
		setLayout(new BorderLayout());
		centerFrame();
 
 		JTabbedPane tabbedPane = new JTabbedPane();
 
 		for (JPanel p : adminPanels) {
 			tabbedPane.add(p);
 		}
 
 		this.add(tabbedPane, BorderLayout.CENTER);
 	}

	/*
	 * Centers the frame
	 */
	public void centerFrame() {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2,
						 dim.height/2-this.getSize().height/2);
	}
 }
