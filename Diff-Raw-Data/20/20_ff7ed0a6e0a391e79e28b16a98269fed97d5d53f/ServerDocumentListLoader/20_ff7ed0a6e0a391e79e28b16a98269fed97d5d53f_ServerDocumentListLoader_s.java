 package client;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.DefaultListModel;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingWorker;
 
 /**
  * Opens the file picking window. Only 1 of these is ever made but the main
  * frame's visibility chages. This lets the documentList update in the
  * background. Also has the main thread that handles incoming messages from the
  * server.
  * 
  * @author Marco Salazar
  * 
  */
 public class ServerDocumentListLoader {
 	private JLabel existingDocsLabel = new JLabel();
 	private JButton newDocumentButton = new JButton();
 	private JTextField newDocumentField = new JTextField();
 	private DefaultListModel docsList = new DefaultListModel();
 	private JList docList = new JList(docsList);
 	private JScrollPane scroll = new JScrollPane(docList);
 	protected static JFrame mainFrame = new JFrame();
 	private JPanel mainPanel = new JPanel();
 	private final BufferedReader in;
 	private final PrintWriter out;
 
 	ServerDocumentListLoader(Socket socket) throws IOException {
 		this.in = new BufferedReader(new InputStreamReader(
 				socket.getInputStream()));
 		this.out = new PrintWriter(socket.getOutputStream(), true);
 		start();
 	}
 
 	protected void sendMessage(String docToSend) {
 		out.println(StringAsciiConversion.asciiToString(docToSend));
 	}
 
 	private void start() throws IOException {
 		out.println("CONNECT");
 		makeGUI();
 
 		/**
 		 * This is the main thread that reads ALL incoming messages from the
 		 * server and takes appropriate action. This includes handling messages
 		 * to multiple open documents as well as updating the list of documents
 		 * on the server.
 		 */
 		(new SwingWorker<Void, String>() {
 			private Pattern regex = Pattern.compile("[\\d]+A");
 
 			@Override
 			protected Void doInBackground() throws Exception {
 				while (true) {
 					String line = null;
 					while (line == null) {
 						try {
 							line = in.readLine();
 							publish(line);
 						} catch (IOException e) {
 							JOptionPane.showMessageDialog(null,
 									"Connection Lost", "Error",
 									JOptionPane.ERROR_MESSAGE);
 							System.exit(-1);
 						}
 					}
 				}
 			}
 
 			@Override
 			protected void process(List<String> lines) {
 				for (String line : lines) {
 					System.out.println("Client Received: " + line);
					if (line.charAt(1) == '%') {
 						/* This is an update regarding documents on the server */
 						final String[] tokens = line.split("%");
 						System.out.println(line);
 						assert (tokens.length % 2 == 0);
 						synchronized (docList) {
 							synchronized (docsList) {
 								docsList.clear();
 								for (int i = 0; i + 1 < tokens.length; i += 2) {
 									docsList.addElement(tokens[i + 1]);
 								}
 							}
 						}
 					} else {
 						/* This is a document update */
 						Matcher matcher = regex.matcher(line);
 						if (matcher.matches() && matcher.start() == 0) {
 							String id = line.substring(0, matcher.end() - 1);
 							System.out.println("Parsed ID: " + id);
 							if (ClientLoader.textEditorMap.containsKey(id)) {
 								TextEditor editor = ClientLoader.textEditorMap
 										.get(id);
 								JTextArea document = editor.document;
 	
 								String docAsAsciiCode = line.substring(id.length() + 1);
 								String docInAsciiText = StringAsciiConversion.toAscii(docAsAsciiCode);
 
 								int temp = editor.textAreaListener.caretPos;
 								document.setText(docInAsciiText);
 								document.setCaretPosition(temp);
 							}
 						}
 					}
 				}
 			}
 		}).execute();
 	}
 
 	private void makeGUI() {
 		mainFrame.add(mainPanel);
 
 		MouseListener mouseListener = new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					int id = docList.getSelectedIndex();
 					TextEditor editor = new TextEditor(out, id);
 					ClientLoader.textEditorMap.put("" + id, editor);
 					mainFrame.setVisible(false);
 				}
 			}
 		};
 
 		docList.addMouseListener(mouseListener);
 		existingDocsLabel.setText("Existing Documents");
 		newDocumentButton.setText("New Document");
 		newDocumentButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (newDocumentField.getText() != "") {
 					String fileName = newDocumentField.getText();
 					if (!Pattern.matches("[\\s\\S]*%[\\s\\S]*", fileName)) {
 						out.println("NEW " + fileName);
 						out.println("CONNECT");
 					}else{
 						JOptionPane.showMessageDialog(null,
 								"Please do not use '%' in your file name", "Error",
 								JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 
 		newDocumentField.setText("exampleFile.txt");
 		newDocumentField.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (newDocumentField.getText() != "") {
 					String fileName = newDocumentField.getText();
 					out.println("NEW " + fileName);
 					out.println("CONNECT");
 				}
 			}
 		});
 
 		JPanel newDocumentPanel = new JPanel();
 		GroupLayout newDocumentLayout = new GroupLayout(newDocumentPanel);
 		newDocumentPanel.setLayout(newDocumentLayout);
 		newDocumentLayout.setAutoCreateGaps(true);
 		newDocumentLayout.setAutoCreateContainerGaps(true);
 
 		newDocumentLayout.setHorizontalGroup(newDocumentLayout
 				.createSequentialGroup().addComponent(newDocumentField)
 				.addComponent(newDocumentButton));
 
 		newDocumentLayout.setVerticalGroup(newDocumentLayout
 				.createParallelGroup(GroupLayout.Alignment.CENTER)
 				.addComponent(newDocumentField, 25, 25, 25)
 				.addComponent(newDocumentButton, 25, 25, 25));
 
 		GroupLayout mainLayout = new GroupLayout(mainPanel);
 		mainLayout.setAutoCreateGaps(true);
 		mainLayout.setAutoCreateContainerGaps(true);
 		mainPanel.setLayout(mainLayout);
 
 		mainLayout.setHorizontalGroup(mainLayout
 				.createParallelGroup(GroupLayout.Alignment.CENTER)
 				.addComponent(newDocumentPanel).addComponent(existingDocsLabel)
 				.addComponent(scroll));
 
 		mainLayout.setVerticalGroup(mainLayout.createSequentialGroup()
 				.addComponent(newDocumentPanel).addComponent(existingDocsLabel)
 				.addComponent(scroll));
 
 		mainFrame.pack();
 		mainFrame.setSize(500, 500);
 		mainFrame.setLocationRelativeTo(null);
 		mainFrame.setTitle("File Selection Window");
 		mainFrame.setVisible(true);
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 }
