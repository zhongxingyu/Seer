 package view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.io.PrintWriter;
 
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 import org.scilab.forge.jlatexmath.TeXIcon;
 
 import server.Regex;
 
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
 	
 	private JTextPane textArea;
 	private JScrollPane scrollText;
 	
 	private JLabel messageLabel;
 	private JButton latexButton;
 	private JButton closeLatexButton;
 	private LatexPanel latexDisplay;
 	
 	private PrintWriter out;
 	private String docName;
 	private String userName;
 	private String docContent;
 	
 	private StyledDocument textDocument;
 	
 	private int version;
 	
 	/**
 	 * Constructor of the DocEdit GUI element
 	 * @param outputStream PrintWriter on which client publishes requests to the server
 	 * @param documentName Name of the document which is currently being edited
 	 * @param userName Name of the user currently making the edit on the document
 	 * @param content Initial content of the document, when the document is loaded from the server
 	 * @param collaboratorNames The initial list of collaborators of the document at the time the document is loaded from the server
 	 */
 	public DocEdit(PrintWriter outputStream, String documentName, String user, String content, String collaboratorNames, int versionID, String colors){
 		super(documentName);
 		
 		this.version = versionID;
 		this.out = outputStream; 
 		this.docName = documentName;
 		this.userName = user;
 		this.docContent = content;
 
 		welcomeLabel = new JLabel("Welcome " + userName + "!");
 		exitButton = new JButton("Exit Doc");
 		
 		collabLabel = new JLabel("Collaborators: ");
 		collaborators = new JLabel(collaboratorNames);
 		updateCollaborators(collaboratorNames, colors);
 		
 		messageLabel = new JLabel("Messages will appear here.");
 		latexButton = new JButton("Latex View");
 		latexDisplay = new LatexPanel();
 		closeLatexButton = new JButton("<");
 		closeLatexButton.setVisible(false);
 		latexDisplay.setVisible(false);
 		
 		textArea = new JTextPane();
 		scrollText = new JScrollPane(textArea);
 		scrollText.setMinimumSize(new Dimension(700, 700));
 		textArea.setText(docContent);
 		textDocument = textArea.getStyledDocument();
 		
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
 		
 		
 		//latex button will both open the latex display then change into a render button
 		latexButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e){
 				if (latexDisplay.isVisible())
 					renderLatex();
 				else
 					showLatexDisplay();
 			}
 		});
 		
 		textArea.addKeyListener(new KeyListener() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				int position = textArea.getCaretPosition();
 				if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {					
 					if (position > 0) {
 						position --;
 						int length = 1;
 						
 						out.println("CHANGE&type=deletion&userName=" + Regex.escape(userName) + "&docName=" + Regex.escape(docName) + "&" +
 								"position=" + position + "&length=" + length + "&version=" + version + "&");
 					}					
 				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 					int length = 1;
 					String change = "\t";	
 					out.println("CHANGE&type=insertion&userName=" + Regex.escape(userName) + "&docName=" + Regex.escape(docName) + "&" +
 							"position=" + position +  "&length=" + length + "&version=" + version + "&change=" + Regex.escape(change) + "&");
 				}
 			}
 
 			@Override
 			public void keyReleased(KeyEvent IGNORE) {}
 
 			@Override
 			public void keyTyped(KeyEvent e) {
 				int position = textArea.getCaretPosition();
 				String change = String.valueOf(e.getKeyChar());
 				if (! (change.equals("\b") || change.equals("\n"))) {
 					javax.swing.text.Style style = textArea.addStyle("BlackForecolor", null);
 			        StyleConstants.setForeground(style, Color.black);
 			        change = change.equals("\n") ? "\t" : change;
 					int length = change.length();
 			        textDocument.setCharacterAttributes(position - length, length, textArea.getStyle("BlackForecolor"), false); 
 					out.println("CHANGE&type=insertion&userName=" + Regex.escape(userName) + "&docName=" + Regex.escape(docName) + "&" +
 							"position=" + position + "&length=" + length + "&version=" + version + "&change=" + Regex.escape(change) + "&");
 				} 
 			}
 			
 		});
 		
 		//make the latex disappear
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
 		
 		packFrame();
 	}
 	
 	/**
 	 * This renders the latex in the display
 	 */
 	public void renderLatex(){
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
 	
 	/**
 	 * This shows the latex pane and allows people to render
 	 */
 	public void showLatexDisplay(){
 		latexDisplay.setVisible(true);
 		int height = scrollText.getHeight();
 		int width = scrollText.getWidth();
 		latexDisplay.setMinimumSize(new Dimension(width/2, height));
 		scrollText.setMinimumSize(new Dimension(width/2, height));
 		latexButton.setText("Render");
 		closeLatexButton.setVisible(true);
 		packFrame();
 	}
 	
 	/**
 	 * Inserts new content at the given position in the document
 	 * @param change New content added at the position	
 	 * @param position Position at which insertion must be made
 	 * @param versionNo New version number of the document
 	 */
 	public void insertContent(String change, int position, int versionNo, Color color) {
 		this.version = versionNo;
 		
 		int length = change.length();
 		int cursorPosition = textArea.getCaretPosition();
 		//update the cursor position if the change comes before the cursor
 		cursorPosition = cursorPosition > position ? cursorPosition + length : cursorPosition;
 		synchronized (textDocument) {
 			try {
 				Style style = textArea.addStyle("foreGround", null);
 		        StyleConstants.setForeground(style, color);
 		        position = position >= textDocument.getLength() ? textDocument.getLength() : position;
 				textDocument.insertString(position, change , style);
 			} catch (BadLocationException e) {
 				out.println("CORRECTERROR&userName=" + Regex.escape(userName) + "&docName=" + Regex.escape(docName) + "&");
 				e.printStackTrace();
 				messageLabel.setText("You have an error syncing. Please exit and reopen the doc");
 			}
 			textArea.setCaretPosition(cursorPosition);
 		}
 	}
 	
 	/**
 	 * Deletes all content from the given version for the given length of characters
 	 * @param position Position of start of deletion
 	 * @param length Length of deletion
 	 * @param versionNo New version number of the document
 	 */
 	public void deleteContent(int position, int length, int versionNo) {
 		this.version = versionNo;
 		int cursorPosition = textArea.getCaretPosition();
 		cursorPosition = cursorPosition >= position ? cursorPosition - length : cursorPosition;
 		synchronized(textDocument) {
 			try {
				if(cursorPosition >= 0)
					textArea.setCaretPosition(cursorPosition);
 				textDocument.remove(position, length);
 			} catch (BadLocationException e) {
 				System.out.println("Position: " + String.valueOf(position));
 				System.out.println("Length: " + String.valueOf(length));
 				System.out.println(String.valueOf(textArea.getText().length()));
 				//In case client is out of sync, send request to the server to reupdate the client
 				out.println("CORRECTERROR&userName=" + userName + "&docName=" + docName + "&"); 
 				messageLabel.setText("You have an error syncing. Please exit and reopen the doc");
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * Reset the text contained in the document 
 	 * @param newContent New content of the document
 	 */
 	public synchronized void resetText(String newContent) {
 		textArea.setText(newContent);
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
 		out.println("EXITDOC&userName=" + Regex.escape(userName) + "&docName=" + Regex.escape(docName) + "&");	
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
 	 * @param collors list of hex colors that corresponds to each collaborator
 	 */
 	public void updateCollaborators(String collaboratorNames, String colors) {
 		if (collaboratorNames == null)
 			return;
 		String[] users = collaboratorNames.split(" ");
 		//colors is a list of hex values where the hex values are integers
 		String[] userColorList = colors.split(" ");
 		if (users.length != userColorList.length){
 			collaborators.setText(collaboratorNames);
 			return;
 		}
 		//Code to change to color of the legend of collaborators
 		String text = "<html>";
 		for (int i = 0; i < users.length; i++){
 			text += "<font color=rgb(" + userColorList[i] + ")>" + users[i].replace(",", " ") 
 					+ "</font>";
 		}
 		text += "</html>";
 		collaborators.setText(text);
 	}
 	
 	/**
 	 * This packs the frame after resizing. It makes sure that everything on the frame
 	 * fits within the JFrame
 	 */
 	public void packFrame() {
 		this.pack();
 	}
 
 	/**
 	 * Sets up a new login DocEdit element. For testing purposes alone
 	 * @param args Unused
 	 */
 	public static void main(String[] args){
 		DocEdit main = new DocEdit(new PrintWriter(System.out), "Document name", "victor", "", "collab", 0, "");
 		main.setVisible(true);
 	}
 }
