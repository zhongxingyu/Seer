 package gui;
 
 import java.awt.Container;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.WindowConstants;
 
 public class About extends JFrame {
 	private JPanel panel;
 
 	public About(Container parentWindow) {
		super("Über");
 		this.setAlwaysOnTop(true);
 		this.setLocationRelativeTo(parentWindow);
 
 		this.panel = new JPanel();
 		this.panel.setLayout(new GridBagLayout());
 
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.insets = new Insets(10, 10, 5, 5);
 
 		JLabel info = new JLabel(
 				"<html>Dieses Programm steht unter GNU GPL v3.<br/>Der Lizenztext steht in der Lizenz.txt Datei.<br/>Autoren:<ul>"
 						+ "<li>Kyra Mooren</li>" + "<li>Philipp Rehs</li>" + "<li>Pakan Sabbaghi</li>"
						+ "<li>Oktay Sarier</li>" + "<li>Maurice Schleußinger</li></ul></html>");
 		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		c.gridx = 0;
 		c.gridy = 0;
 		c.ipadx = 00;
 		c.ipady = 20;
 
 		this.panel.add(info, c);
 		this.setContentPane(this.panel);
 		this.pack();
 		this.setVisible(true);
 	}
 }
