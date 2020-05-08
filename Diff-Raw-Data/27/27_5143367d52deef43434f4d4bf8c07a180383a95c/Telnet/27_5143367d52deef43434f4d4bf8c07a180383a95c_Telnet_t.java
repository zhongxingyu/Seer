 package telnet;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 
 import telnet.ScrollingTextPane.TextPane;
 
 public class Telnet {
 
 	private TextPane outputArea;
 	private Socket s;
 	private BufferedReader socketOutput;
 	private PrintWriter socketInput;
 	private JFrame gui = new JFrame("Telnet");
 	private ManagedFile file = new ManagedFile();
 
 	public Telnet(String host, int port) {
 		try {
 			gui.setIconImage(javax.imageio.ImageIO.read(new File("icon.png")));
 			s = new Socket(InetAddress.getByName(host), port);
 			s.setKeepAlive(true);
 			socketOutput = new BufferedReader(new InputStreamReader(
 					s.getInputStream()));
 			socketInput = new PrintWriter(s.getOutputStream(), true);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		try {
 			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 		} 
 		catch (Exception e) {
 		}
 		
 		gui.setSize(600, 500);
 		gui.setBackground(Color.BLACK);
 
 		gui.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				socketInput.close();
 				try {
 					socketOutput.close();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				try {
 					s.close();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				file.promptSaveOnQuit();
 				System.exit(0);
 			}
 		});
 
 		gui.setLayout(new GridBagLayout());
 		
 		GPanel gpanel = new GPanel();
 		
 		ScrollingTextPane rightInputScrollPane = new ScrollingTextPane();
 			
 		rightInputScrollPane.addKeyListener(new RightInputAreaListener(
 				rightInputScrollPane, outputArea, socketInput, file));
 		
 
 		GridBagConstraints c = new GridBagConstraints();		
 		c.fill = GridBagConstraints.BOTH;
 		c.gridy = 0;
 		c.weighty = 1;
 		c.weightx = 1;
 		
 		c.gridx = 1;		
 		gui.add(rightInputScrollPane, c);
 		
 		c.gridx = 0;
 		gui.add(gpanel, c);
 
 		gui.setVisible(true);
 
 	}
 
 	@SuppressWarnings("serial")
 	private class GPanel extends JPanel {
 
 		GPanel() {
 			super(new GridBagLayout());
 
 			ScrollingTextPane outputScrollPane = new ScrollingTextPane();
 			outputArea = outputScrollPane.getTextPane();
 
 			ScrollingTextPane leftInputScrollPane = new ScrollingTextPane();
 
 			leftInputScrollPane.addKeyListener(new LeftInputAreaListener(
 					leftInputScrollPane, outputArea, socketInput));
 			
 			outputArea.setKeymap(null);
 
 			GridBagConstraints c = new GridBagConstraints();
 			c.fill = GridBagConstraints.BOTH;
 			c.gridx = 0;
 			c.weightx = 1;
 					
 			c.gridy = 1;
 			c.weighty = 1;
 			this.add(leftInputScrollPane, c);
 
 			c.gridy = 0;
 			c.weighty = 4;			
 			this.add(outputScrollPane, c);
			
			new Thread() {
				public void run() {
					try {
						while (true) {
							String line = socketOutput.readLine();
							outputArea.append(line);
						}
					} catch (IOException e) {
						System.out.println("Connection Closed.");
					}
				}
			}.start();
 
 			
 
 			setBackground(Color.BLACK);
 
 		}
 	}
 
 }
