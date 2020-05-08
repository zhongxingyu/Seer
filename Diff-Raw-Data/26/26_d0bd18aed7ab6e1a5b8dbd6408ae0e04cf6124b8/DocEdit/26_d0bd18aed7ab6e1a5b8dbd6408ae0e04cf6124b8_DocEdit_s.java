 package view;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.PrintWriter;
 
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 
 import org.scilab.forge.jlatexmath.TeXIcon;
 
 /**
  * Represents the DocEdit GUI element. Allows the user to edit a document.
  * All changes made in the document are updated back to the server.
  * It is possible for the user to return to the document table from the
  * DocEdit GUI element.
  */
 @SuppressWarnings("serial")
 public class DocEdit extends JFrame {
 	
 	private JLabel welcomeLabel;
 	private JButton exitButton;
 	
 	private JLabel collabLabel;
 	private JLabel collaborators;
 	
 	private JTextArea textArea;
 	private JScrollPane scrollText;
 	
 	private JLabel messageLabel;
 	private JButton latexButton;
 	private JButton closeLatexButton;
 	private MyPanel latexDisplay;
 	
 	private PrintWriter out;
 	private String docName;
 	private String userName;
 	private String docContent;
 	@SuppressWarnings("unused")
 	private String collaboratorNames;
 	
 	private Document textDocument;
 	private final DocumentListener documentListener;
 	
 	private int version;
 	
 	/**
 	 * Constructor of the DocEdit GUI element
 	 * @param outputStream PrintWriter on which client publishes requests to the server
 	 * @param documentName Name of the document which is currently being edited
 	 * @param userName Name of the user currently making the edit on the document
 	 * @param content Initial content of the document, when the document is loaded from the server
 	 * @param collaboratorNames The initial list of collaborators of the document at the time the document is loaded from the server
 	 */
 	public DocEdit(PrintWriter outputStream, String documentName, String userName, String content, String collaboratorNames, int versionID){
 		super(documentName);
 		
 		this.version = versionID;
 		out = outputStream;
 		this.docName = documentName;
 		this.userName = userName;
 		this.docContent = content;
 		this.collaboratorNames = collaboratorNames;
 
 		welcomeLabel = new JLabel("Welcome " + userName + "!");
 		exitButton = new JButton("Exit Doc");
 		
 		collabLabel = new JLabel("Collaborators: ");
 		collaborators = new JLabel(collaboratorNames);
 		
 		messageLabel = new JLabel("Messages will appear here.");
 		latexButton = new JButton("Latex View");
 		latexDisplay = new MyPanel();
		latexDisplay.setSize(new Dimension(20, 55));
 		closeLatexButton = new JButton("<");
 		closeLatexButton.setVisible(false);
 		latexDisplay.setVisible(false);
 		
		textArea = new JTextArea(20, 50);
 		scrollText = new JScrollPane(textArea);
 		textArea.setText(docContent);
 		textDocument = textArea.getDocument();
 		
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 
 		GroupLayout layout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 
 		//this sets up the horizontal alignment
 		GroupLayout.ParallelGroup hGroup = layout.createParallelGroup();
 		hGroup.addGroup(
 				layout.createParallelGroup()
 					.addGroup(layout.createSequentialGroup()
 							.addComponent(collabLabel)
 							.addComponent(collaborators)
 							)
 					.addComponent(welcomeLabel)
 					.addComponent(messageLabel)
 					);
 		hGroup.addGroup(
 				layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
 					.addComponent(exitButton)
 					.addGroup(layout.createSequentialGroup()							
 							.addComponent(scrollText)
 							.addComponent(latexDisplay)
 							)
 					.addGroup(layout.createSequentialGroup()
 							.addComponent(closeLatexButton)
 							.addComponent(latexButton)
 							)
 					);
 		layout.setHorizontalGroup(hGroup);
 		
 		//this sets up the vertical alignment
 		GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
 		vGroup.addGroup(
 					layout.createParallelGroup(GroupLayout.Alignment.CENTER)
 						.addComponent(welcomeLabel)
 						.addComponent(exitButton)
 					);
 		vGroup.addGroup(
 					layout.createParallelGroup()
 						.addComponent(collabLabel)
 						.addComponent(collaborators)
 					);
 		vGroup.addGroup(
 					layout.createParallelGroup()
 						.addComponent(scrollText)
 						.addComponent(latexDisplay)
 					);
 		vGroup.addGroup(
 					layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 						.addComponent(messageLabel)
 						.addComponent(closeLatexButton)
 						.addComponent(latexButton)
 					);
 		layout.setVerticalGroup(vGroup);
 		
 		latexButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				if (latexDisplay.isVisible()){
					//TODO render latex
 					String content = textArea.getText();
 					if (Latex.isLatex(content)){
 						TeXIcon icon = Latex.getLatex(content);
 						BufferedImage b = new BufferedImage(icon.getIconWidth(),
 								icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
 						icon.paintIcon(new JLabel(), b.getGraphics(), 0, 0);
 						b.getGraphics().drawImage(b, 0, 0, null);
 						latexDisplay.updateImage(b);
 						latexDisplay.repaint();
 					}
 				}
 				//show latex display and the close button
 				else{
 					latexDisplay.setVisible(true);
 					latexButton.setText("Render");
 					closeLatexButton.setVisible(true);
 				}
 			}
 		});
 		
 		closeLatexButton.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e){				
 				//close latex and make button disappear
 				latexDisplay.setVisible(false);
 				closeLatexButton.setVisible(false);
 				latexButton.setText("Latex View");
 			}	
 			
 		});
 		
 		
 		// Add an action listener to the exit button
 		exitButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				exitDocument();	
 			}
 		});		
 		
 		// Adds a document listener to the document associated with the JTextArea
 		documentListener = new DocumentListener() {
 			@Override
 			public void changedUpdate(DocumentEvent e) {
 				System.out.println("CHANGED!");
 			}
 
 			@Override
 			public void insertUpdate(DocumentEvent e) {
 				try {
 					int position = e.getOffset();
 					int length = e.getLength();
 
 					String change = textDocument.getText(position, length);
 					if (change.equals("\n")) {
 						// Delimit lines with tabs
 						out.println("CHANGE|" + docName + "|" + position + "|" + "\t" + "|" + length + "|" + version);
 					}  else if (! change.equals("")){
 						out.println("CHANGE|" + docName + "|" + position + "|" + change + "|" + length + "|" + version);
 					}
 					
 				} catch (BadLocationException e1) {
 					throw new UnsupportedOperationException();
 				}
 				
 			}
 
 			@Override
 			public void removeUpdate(DocumentEvent e) {
 				int position = e.getOffset();
 				int length = e.getLength();
 				out.println("CHANGE|" + docName + "|" + position + "|" + length + "|" + version);
 				
 			}
 		};
 				
 		this.addListener();
 		
 		this.pack();
 	}
 	
 	/**
 	 * Method to update content in the text area
 	 * @param newContent New content in the text area
 	 */
 	public synchronized void updateContent(String newContent, int position, int length, int versionNo, boolean isInsertion) {
 		this.version = versionNo;
 		
 		int posChange = isInsertion ? position + length : position;
 		
 		posChange = Math.min(posChange, textArea.getText().length());
 		posChange = Math.max(0, posChange);
 		
 		removeListener();
 		textArea.setText(newContent);
 		addListener();
 		//TODO Need to fix the cursor issue; it's pretty annoying
 		textArea.setCaretPosition(posChange);
 	}
 	
 	/**
 	 * Method that returns the content in the text area
 	 * @return Content of the document entered by the user
 	 */
 	public synchronized String getContent() {
 		String content = this.textArea.getText();
 		return content;
 	}
 	
 	/**
 	 * Method for the user to exit the given document
 	 */
 	private synchronized void exitDocument() {
 		out.println("EXITDOC " + userName + " " + docName);	
 	}
 	
 	/**
 	 * Getter for the name of the GUI element
 	 */
 	public String getName() {
 		return docName;
 	}
 	
 	/**
 	 * Method to update the displayed set of collaborators
 	 * @param collaboratorNames The updated list of collaborators
 	 */
 	public void updateCollaborators(String collaboratorNames) {
 		collaborators.setText(collaboratorNames);
 	}
 	
 	/**
 	 * Method that associates document listener to the document associated with the text area
 	 */
 	public void addListener() {
 		textDocument.addDocumentListener(documentListener);
 	}
 	
 	/**
 	 * Method that disassociates the document listener from the document associated with the text area
 	 */
 	public void removeListener() {
 		textDocument.removeDocumentListener(documentListener);
 	}
 
 	/**
 	 * Sets up a new login DocEdit element. For testing purposes alone
 	 * @param args Unused
 	 */
 	public static void main(String[] args){
 		DocEdit main = new DocEdit(new PrintWriter(System.out), "Document name", "victor", "", "collab", 0);
 		main.setVisible(true);
 	}
 	
 }
