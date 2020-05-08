 package mainclient;
 
 import gui.MultiDrawFrame;
 import gui.ToolPalette;
 
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Image;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import network.ClientReceiver;
 import network.ClientSender;
 import network.SendBuffer;
 import utils.Controller;
 
 class MultiDrawClient {
 	public static void main(String[] args) {
 		String myName = "";
 
 
 		JTextField nick = new JTextField(25);
 		JTextField address = new JTextField(25);
 		try {
 			address.setText(InetAddress.getLocalHost().getHostAddress());
 		} catch (UnknownHostException e1) {
 			e1.printStackTrace();
 		}
 
 		JPanel myPanel = new JPanel();
 		FlowLayout f = new FlowLayout();
 		f.setAlignment(FlowLayout.TRAILING);
 		myPanel.setLayout(f);
 		myPanel.add(new JLabel("Nickname: "));
 		myPanel.add(nick);
 		myPanel.add(new JLabel("Host: "));
 		myPanel.add(address);
 		myPanel.setPreferredSize(new Dimension(380,60));
 		//myPanel.setBackground(new Color(190,155,200));
 		
 		Image img = null;
 		try {
 			img = ImageIO.read(ToolPalette.class.getResource("/res/icons/paintbrush.png"));
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		
 		ImageIcon imgic = new ImageIcon(img);
 		
 		while(nick.getText().length() <= 0){
 			
 			int result = JOptionPane.showConfirmDialog(null, myPanel,"Connect to an AOJA Mainframe", 
 					JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE,imgic);
 			if (result == JOptionPane.OK_OPTION) {
 				myName = nick.getText();
 			} else {
 				System.exit(0);
 			}
 		}
 
 		Socket s = null;
 
 		try {
			s = new Socket(address.getText(), 30001);
 		} catch (UnknownHostException e) {
 			System.err.println("Did not find a host at the specified address.");
 			System.exit(1);
 		} catch (IOException e) {
 			System.err.println("Connection error.");
 			System.exit(1);
 		}
 
 		SendBuffer buffer = new SendBuffer(10);
 		Controller controller = new Controller();
 		ClientReceiver receiver = new ClientReceiver(s, controller);
 		ClientSender sender = new ClientSender(myName, s, buffer);
 		MultiDrawFrame mainframe = new MultiDrawFrame(buffer);
 
 		controller.setFrame(mainframe);
 		controller.setSender(sender);
 		receiver.start();
 		sender.start();
 	}
 }
