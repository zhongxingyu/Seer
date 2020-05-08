 package org.globalgamejam.strat;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import com.badlogic.gdx.backends.jogl.JoglApplication;
 
 @SuppressWarnings("serial")
 public class DesktopLauncher extends JFrame implements ActionListener {
 	// Graphical components
 	private JButton button;
 	private JTextField host, port;
 
 	// Window constructor
 	public DesktopLauncher() {
 		super();
 
 		// Window properties
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setTitle("Strat Client connection");
 		setSize(300, 200);
 		setResizable(false);
 
 		// Create the visual panel
 		JPanel panel = new JPanel(new GridLayout(5, 1));
 		setContentPane(panel);
 
 		// Add controls
 		panel.add(new JLabel("Host :"));
 		host = new JTextField("localhost");
 		panel.add(host);
 
 		panel.add(new JLabel("Port :"));
 		port = new JTextField("50000");
 		panel.add(port);
 
 		button = new JButton("Connect");
 		button.setActionCommand("connect");
 		button.addActionListener(this);
 		panel.add(button);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		String host = this.host.getText();
 		int port = Integer.parseInt(this.port.getText());
 		String Action = e.getActionCommand();
 		if (Action.equals("connect")) {
 			// Hack for openjdk7
 			System.loadLibrary("jawt");
 
 			// Try to connect
 			try {
 				// GameRenderer creation and connection
 				GameStrat gr = new GameStrat(host, port);
 
 				// OK, launch display and hide dialog
				new JoglApplication(gr, "Strat", 800, 480, false);
 				this.dispose();
 			} catch (IOException ex) {
 				// Inform user of the error
 				JOptionPane.showMessageDialog(null, "Connection error to "
 						+ host + ":" + port+ "\n" + ex.getMessage(), "Strat Client connection error",
 						JOptionPane.ERROR_MESSAGE);
 				System.out.println("Connexion error to " + host + ":" + port
 						+ " : " + ex.getMessage());
 			}
 
 		}
 	}
 
 	public static void main(String[] argv) {
 		DesktopLauncher app = new DesktopLauncher();
 		app.setVisible(true);
 	}
 }
