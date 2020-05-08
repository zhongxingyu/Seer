 package telnet;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.LineBorder;
 
 import telnet.ScrollingTextPane.TextPane;
 
 public class Telnet {
 
 	private TextPane inputArea;
 	private TextPane outputArea;
 	private Socket s;
 	private BufferedReader in;
 	private PrintWriter out;
 	private GPanel g = new GPanel();
 	private JFrame gui = new JFrame("Telnet");
 
 	public Telnet(String host, int port) {
 		try {
 			s = new Socket(InetAddress.getByName(host), port);
 			s.setKeepAlive(true);
 			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
 			out = new PrintWriter(s.getOutputStream(), true);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		new Thread() {
 			public void run() {
 				try {
 					while (true) {
 						String line = in.readLine();
 						outputArea.append(line);
 					}
 				} catch (IOException e) {
 					System.out.println("Connection Closed.");
 				}
 			}
 		}.start();
 
 		gui.setSize(600, 500);
 		gui.add(g);
 
 		gui.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				out.close();
 				try {
 					in.close();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				try {
 					s.close();
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				System.exit(0);
 			}
 		});
 
 		gui.setVisible(true);
 
 		inputArea.requestFocusInWindow();
 	}
 
 	@SuppressWarnings("serial")
 	private class GPanel extends JPanel {
 		private int tabCount = 0;
 
 		GPanel() {
 			super(new GridBagLayout());
 
 			ScrollingTextPane inputScrollPane = new ScrollingTextPane();
 			ScrollingTextPane outputScrollPane = new ScrollingTextPane();
 
 			inputArea = inputScrollPane.getTextPane();
 			outputArea = outputScrollPane.getTextPane();
 
 			inputArea.addKeyListener(new KeyAdapter() {
 
 				public void keyPressed(KeyEvent e) {
 					switch (e.getKeyCode()) {
 					// up key
 					case 38:
 						// if shift is on
 						if (e.getModifiersEx() == 64) {
 							inputArea.setText(CommandRecall.getInstance().prev());
 						}
 						break;
 
 					// down key
 					case 40:
 						// if shift is on
 						if (e.getModifiersEx() == 64) {
 							CommandRecall.getInstance().add(inputArea.getText());
 							inputArea.setText(CommandRecall.getInstance().next());
 						}
 						break;
 					// l key
 					case 76:
 						// if control is on
 						if (e.getModifiersEx() == 128) {
 							outputArea.setText("");
 						}
 						break;
 					// e key
 					case 69:
 						// if control is on
 						e.consume();
 						String text = inputArea.getSelectedText();
 						if (e.getModifiersEx() == 128 && text != null) {
 							outputArea.append(text);
 							out.println(text.replaceAll("\\n", ""));
 						}
 						break;
 					}
 
 				}
 
 				public void keyTyped(KeyEvent e) {
 					switch ((int) e.getKeyChar()) {
 					// tab key
 					case 9:
 						tabCount++;
 						e.consume();// Does not work
 						inputArea.setText(inputArea.getText().trim());
 						if (tabCount == 1) {
 							String autoCompleted = Tab.getInstance()
 									.autoComplete(inputArea.getText(),
 											inputArea.getCaretPosition());
 							if (!autoCompleted.equals(inputArea.getText())) {
 								inputArea.setText(autoCompleted);
 
 							}
 						} else if (tabCount == 2) {
 							String results = Tab.getInstance().suggestions(
 									inputArea.getText(),
 									inputArea.getCaretPosition());
 							if (results.startsWith("doc ", 1)){
 								outputArea.append(results);
 								out.println(results);
 							}
 							else{
 							outputArea.append(results);
 							}
 						}
 						break;
 					// enter key
 					case 10:
 						// if shift is on
 						if (e.getModifiersEx() == 64) {
 							String text = inputArea.getText().replaceAll(
 									"\\s+$", "");
 							outputArea.append(text);
 							CommandRecall.getInstance().add(text);
 							out.println(text.replaceAll("\\n", ""));
 							inputArea.setText("");
 						}
 						tabCount = 0;
 						break;
 					default:
 						tabCount = 0;
 						break;
 					}
 				}
 
 			});
 
 			inputArea.setFocusTraversalKeysEnabled(false);
 
 			outputArea.setKeymap(null);
 
 			// Add Components to this panel.
 			GridBagConstraints c = new GridBagConstraints();
 
 			c.gridwidth = getWidth();
 			c.weightx = 1;
 
 			c.fill = GridBagConstraints.BOTH;
 			c.gridx = 0;
 			c.gridy = 1;
 			c.weighty = 1;
 			c.anchor = GridBagConstraints.PAGE_END;
 			this.add(inputScrollPane, c);
 
 			c.fill = GridBagConstraints.BOTH;
 			c.gridx = 0;
 			c.gridy = 0;
 			c.weighty = 4;
 			this.add(outputScrollPane, c);
 
 			setBackground(Color.BLACK);
 
 			outputArea.setBorder(new LineBorder(Color.BLACK));
 			inputArea.setBorder(new LineBorder(Color.RED));
 
 		}
 	}
 
 }
