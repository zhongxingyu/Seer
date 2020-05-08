 package telnet;
 
 import java.awt.Color;
 import java.awt.Event;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.InputEvent;
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
 
 import javax.swing.Action;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.KeyStroke;
 import javax.swing.border.LineBorder;
 import javax.swing.text.DefaultEditorKit;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.TextAction;
 
 public class Telnet {
 
 	private JTextPane inputArea;
 	private JTextPane outputArea;
 	private Socket s;
 	private BufferedReader in;
 	private PrintWriter out;
 	private GPanel g = new GPanel();
 	private JFrame gui = new JFrame("Telnet");
 	private CommandList cmds = new CommandList();
 
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
 						synchronized (outputArea) {
 							outputArea.setText(outputArea.getText() + line
 									+ "\n");
 							outputArea.setCaretPosition(outputArea
 									.getDocument().getLength());
 						}
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
 			inputArea = new JTextPane();
 			inputArea.addKeyListener(new KeyAdapter() {
 
 				public void keyPressed(KeyEvent e) {
 					switch (e.getKeyCode()) {
 					// up key
 					case 38:
 						// if shift is on
 						if (e.getModifiersEx() == 64) {
 							inputArea.setText(cmds.prev());
 							inputArea.setCaretPosition(inputArea.getDocument()
 									.getLength());
 						}
 						break;
 
 					// down key
 					case 40:
 						// if shift is on
 						if (e.getModifiersEx() == 64) {
 							cmds.add(inputArea.getText());
 							inputArea.setText(cmds.next());
 						}
 						break;
 					// l key
 					case 76:
 						// if control is on
 						if (e.getModifiersEx() == 128) {
 							outputArea.setText("");
 						}
 						break;
 					//e key
 					case 69:
 						// if control is on
 						e.consume();
						String text = inputArea.getSelectedText();
						if (e.getModifiersEx() == 128 && text != null) {
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
 							outputArea.setText(outputArea.getText()
 									+ Tab.getInstance().suggestions(
 											inputArea.getText(),
 											inputArea.getCaretPosition())
 									+ "\n");
 						}
 						break;
 					// enter key
 					case 10:
 						// if shift is on
 						if (e.getModifiersEx() == 64) {
 							String text = inputArea.getText().replaceAll(
 									"\\s+$", "");
 							synchronized (outputArea) {
 								outputArea.setText(outputArea.getText() + text
 										+ "\n");
 							}
 							cmds.add(text);
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
 
 			outputArea = new JTextPane();
 
 			inputArea.setFocusTraversalKeysEnabled(false);
 			
 			outputArea.setKeymap(null);
 			
 			JScrollPane outputScrollPane = new JScrollPane(outputArea);
 			JScrollPane inputScrollPane = new JScrollPane(inputArea);
 
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
 
 			outputScrollPane.setBorder(new LineBorder(Color.BLACK));
 			inputScrollPane.setBorder(new LineBorder(Color.BLACK));
 			outputArea.setBorder(new LineBorder(Color.BLACK));
 			inputArea.setBorder(new LineBorder(Color.RED));
 			inputArea.setBackground(Color.BLACK);
 			outputArea.setBackground(Color.BLACK);
 			inputArea.setForeground(Color.GREEN);
 			outputArea.setForeground(Color.GREEN);
 			inputArea.setCaretColor(Color.RED);
 
 		}
 	}
 
 }
