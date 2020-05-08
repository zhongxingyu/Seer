 package menu;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.JLabel;
 import javax.swing.JTextArea;
 
 import networking.common.JoinGame;
 import networking.common.Network;
 import networking.common.ServerNotFoundException;
 import networking.common.SocketBusyException;
 
 public class HostMenuPanel extends AbstractMenuPanel {
 
 	public HostMenuPanel(final MainFrame frame) {
 
 		final JLabel error = new JLabel();;
 
 
 		this.addLabel(frame, "PortLabel");
 
 
 
 		final JTextArea port = new JTextArea(1, 30);
 		this.addComponent(port);
 
 		ActionListener listener = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int portNumber = 0;
 				try {
 					portNumber = Integer.parseInt(port.getText());
 				} catch (NumberFormatException e2) {
 					showError("Please enter a valid port number") ;
 					return;
 				}
 				int minPort = 1024;
 				int maxPort = (int)Math.pow(2, 16) - 1;
 				if (portNumber <= minPort){
 					showError("Port number must excede " + minPort) ;
 					return;
 				}
 				if (portNumber > maxPort) {
 					showError("Port number must precede " + maxPort);
 					return;
 				}
 				Network n;
 				try {
 					n = new Network(null , portNumber );
 				} catch (ServerNotFoundException e1) {
 					showError("Server not found");
 					return;
 				} catch (SocketBusyException e1) {
 					showError("Port is being used by another program");
 					return;
 				} catch (IOException e1) {
 					showError("Connection failed, please try again");
 					return;
 				}
 				showError("game joined");
 				frame.addWindowListener(new java.awt.event.WindowAdapter() {
 				    public void windowClosing(java.awt.event.WindowEvent evt) {
 
 				    }
 				});
 				JoinGame jg;
 				try {
 					jg = new JoinGame(n, true, frame.musicThread);
 				} catch (IOException e1) {
 					showError("Connection failed, please try again");
 					return;
 				} catch (ClassNotFoundException e1) {
 					showError("Connection failed, please try again");
 					return;
 				}
 				frame.addMenu(jg.getWindow());
 			}
 
 			private void showError(String string) {
 				error.setText(string);
 			}
 
 		};
 		this.addButton(frame, listener, "CreateButton");
 
 		listener = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.back() ;
 			}
 		};
 		this.addButton(frame, listener, "BackButton");
 
 		this.add(error);
 	}
 
 }
