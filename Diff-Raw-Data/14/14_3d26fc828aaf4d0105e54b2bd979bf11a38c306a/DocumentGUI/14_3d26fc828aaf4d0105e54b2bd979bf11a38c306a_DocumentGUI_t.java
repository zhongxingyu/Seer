 package GUI;
 
 import javax.swing.*;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import Commands.CutCommand;
 import Commands.IndentCommand;
 import Commands.InsertCommand;
 import Commands.PasteCommand;
 import Commands.SaveAsCommand;
 import Commands.SaveCommand;
 import Commands.ToggleIndentCommand;
 import Commands.ToggleWrapCommand;
 import Entities.DocumentMenu;
 import Entities.TheDocument;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 
 /**
  * GUI for each document
  * @author Chris Timmons
  * @author Ben Kantor (bdk3079@rit.edu)
  */
 @SuppressWarnings("serial")
 public class DocumentGUI extends JPanel{
 	
 	private TheDocument thisDoc;
 	private DocumentMenu docMenu;
 	private LinkView linkView;
 	private TreeView treeView;
 	
 	private JPanel menuPanel,insertPanel,docMenuPanel,settingsPanel; 
 	private JPanel indentPanel,wordWrapPanel;
 	private JButton linkViewBtn, treeViewBtn;
 	private InsertButton insertBoldBtn,insertItalicsBtn,insertHeaderBtn,insertListBtn,insertTableBtn,insertTextBtn;
 	private InsertButton insertHRefBtn, insertImgTag, insertSrcTag;
 	private JRadioButton wordWrapOn,wordWrapOff,indentOn,indentOff;
 	private JLabel wordWrapLabel = new JLabel("Word-Wrap:      ");
 	private JLabel indentLabel = new JLabel("Auto-Indent:      ");
 	private final JTextArea textArea;
 	
 	private JMenu documentMenu;
 	private JMenuItem save,saveAs,cut,paste,undo;
 	
 	private int indentSize = 5;
 	private JSpinner indentChanger;
 	private boolean linkViewOn = false;
 	
 	/**
 	 * Constructor for the document GUI
 	 * @param docParam TheDocument that is being visually represented
 	 */
 	public DocumentGUI(TheDocument docParam) {		
 		thisDoc = docParam;
 		docMenu = createDocumentMenu(thisDoc);
 		
 		DocumentMenuListener docMenuListener = new DocumentMenuListener();
 		InsertListener insertListener = new InsertListener();
 		this.setLayout(new BorderLayout());
 		
 		//Tab-Size Changer
 		JPanel tabSizePanel = new JPanel();
 		JLabel tabSizeLabel = new JLabel("Indent-Size");
 		indentChanger = new JSpinner();
 		indentChanger.setValue(indentSize);
 		tabSizePanel.add(tabSizeLabel,BorderLayout.WEST);
 		tabSizePanel.add(indentChanger,BorderLayout.CENTER);
 		
 		
 		//JMenu
 		documentMenu = new JMenu("Document");
 		save = new JMenuItem("Save");
 		saveAs = new JMenuItem("Save As");
 		cut = new JMenuItem("Cut");
 		paste = new JMenuItem("Paste");
 		undo = new JMenuItem("Undo");
 		documentMenu.add(documentMenu);
 		documentMenu.add(save);
 		documentMenu.add(saveAs);
 		documentMenu.add(cut);
 		documentMenu.add(paste);
 		documentMenu.add(undo);
 		
 		//Create Panels
 		menuPanel = new JPanel();
 		//treePanel = new JPanel();
 		insertPanel = new JPanel(new BorderLayout());
 		indentPanel = new JPanel(new GridLayout(1,3));
 		wordWrapPanel = new JPanel(new GridLayout(1, 3));
 		docMenuPanel = new JPanel(new BorderLayout());
 		settingsPanel = new JPanel(new GridLayout(3,1));
 		
 		
 		//Create Buttons
 		linkViewBtn = new JButton("Toggle Link View");
 		treeViewBtn = new JButton ("Toggle Outline Mode");
 		
 		wordWrapOn = new JRadioButton("On ", true);
 		wordWrapOff = new JRadioButton("Off ");
 		indentOn = new JRadioButton("On", true);
 		indentOff = new JRadioButton("Off");
 		
 		insertBoldBtn = new InsertButton("<b> Bold </b>","BOLD");
 		insertItalicsBtn = new InsertButton("<i> Italic </i>","ITALIC");
 		insertHeaderBtn = new InsertButton("<h1> Header </h1>","HEADER");
 		insertListBtn = new InsertButton("Insert List","LIST");
 		insertTableBtn = new InsertButton("Insert Table","TABLE");
 		insertTextBtn = new InsertButton("Text","Tag");
 		insertHRefBtn = new InsertButton("Link", "HREF");
 		insertImgTag = new InsertButton("IMG","IMG");
 		insertSrcTag = new InsertButton("SRC","SRC");
 		
 		//Add Listeners
 		save.addActionListener(docMenuListener);
 		saveAs.addActionListener(docMenuListener);
 		cut.addActionListener(docMenuListener);
 		paste.addActionListener(docMenuListener);
 		indentOn.addActionListener(docMenuListener);
 		indentOff.addActionListener(docMenuListener);
 		wordWrapOn.addActionListener(docMenuListener);
 		wordWrapOff.addActionListener(docMenuListener);
 		
 		insertBoldBtn.addActionListener(insertListener);
 		insertItalicsBtn.addActionListener(insertListener);
 		insertHeaderBtn.addActionListener(insertListener);
 		insertListBtn.addActionListener(insertListener);
 		insertTableBtn.addActionListener(insertListener);
 		insertTextBtn.addActionListener(insertListener);
 		
 		linkViewBtn.addActionListener(new ActionListener() {
 			/**
 			 * Checks if the LinkView for the Current Doc has been created yet. If not, the LinkView is created
 			 */
 			public void actionPerformed(ActionEvent e) {
 				if ((linkView == null) || (!linkView.isShowing())) {
 					linkView = new LinkView(DocumentGUI.this, "Link View: " + thisDoc.getName());
 					add(linkView, BorderLayout.EAST);
 					revalidate();
 					repaint();
 				} else{
 					remove(linkView);
 					revalidate();
 					repaint();
 				}
 			}
 		});
 		
 		treeViewBtn.addActionListener(new ActionListener(){
 		    
 		    public void actionPerformed(ActionEvent e){
 		        
     		    if ((treeView == null) || (!treeView.isShowing())) {
     		        DefaultMutableTreeNode tree = null;
                     
                         try {
                             tree = thisDoc.buildTree(textArea.getText());
                         } catch (Exception ex){
                             System.err.print("Parsing failed");
                         }
                    
                     if(thisDoc.isWellFormed() == true){
                         treeView = new TreeView(DocumentGUI.this, "Tree View: " + thisDoc.getName(), tree );
                         add(treeView,BorderLayout.WEST);
                         revalidate();
                         repaint();
                     }
                     else{
                          
                         JFrame frame = new JFrame("Cannot Create Tree.");
                         JOptionPane.showMessageDialog(frame, "Document is not well-formed. Cannot create Tree.","Inane error",JOptionPane.ERROR_MESSAGE);
                     }
                     
                 } else{
                 	remove(treeView);
                 	revalidate();
                 	repaint();
                 }
 		    }
 		});
 
 		//Define WordWrapPanel		
 		wordWrapPanel.add(wordWrapLabel);
 		wordWrapPanel.add(wordWrapOn);
 		wordWrapPanel.add(wordWrapOff);
 		ButtonGroup wordWrapGroup = new ButtonGroup();
 		wordWrapGroup.add(wordWrapOn);
 		wordWrapGroup.add(wordWrapOff);
 		
 		//Define IndentWrapPanel
 		
 		indentPanel.add(indentLabel);
 		indentPanel.add(indentOn);
 		indentPanel.add(indentOff);
 		ButtonGroup indentGroup = new ButtonGroup();
 		indentGroup.add(indentOn);
 		indentGroup.add(indentOff);
 		
 		//Define MenuPanel
 		menuPanel.setLayout(new GridLayout(1,2));
 		settingsPanel.add(indentPanel);
 		settingsPanel.add(wordWrapPanel);
 		settingsPanel.add(tabSizePanel);
 		menuPanel.add(settingsPanel);
 		
 		//Define Insert Panel
 		JLabel insertLabel = new JLabel("Insert");
 		JPanel insertButtons = new JPanel(new GridLayout(3,6));
 		insertButtons.add(insertTextBtn);
 		insertButtons.add(insertBoldBtn);
 		insertButtons.add(insertBoldBtn);
 		insertButtons.add(insertItalicsBtn);
 		insertButtons.add(insertHeaderBtn);
 		insertButtons.add(insertListBtn);
 		insertButtons.add(insertTableBtn);
 		insertButtons.add(insertImgTag);
 		insertButtons.add(insertSrcTag);
 		insertButtons.add(linkViewBtn);
 		insertButtons.add(treeViewBtn);
 		JScrollPane buttonScrollPane = new JScrollPane(insertButtons);
 		buttonScrollPane.setPreferredSize(new Dimension(50,100));
 		insertPanel.add(insertLabel,BorderLayout.NORTH);
 		insertPanel.add(buttonScrollPane,BorderLayout.SOUTH);
 		insertPanel.setPreferredSize(new Dimension(600,100));
 		
 		//Define docMenuPanel
 		docMenuPanel.add(insertPanel,BorderLayout.CENTER);
 		docMenuPanel.add(menuPanel,BorderLayout.WEST);
 		
 		
 		//Setup Main Panel
 		textArea = new JTextArea();
 		textArea.setEditable(true);
 		textArea.setText(thisDoc.getXml());
		textArea.registerKeyboardAction(new AutoIndentAction(true), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
 		JScrollPane textPane = new JScrollPane(textArea);
 		add(docMenuPanel,BorderLayout.NORTH);
 		add(textPane,BorderLayout.CENTER);	
 		
 	//	textArea.registerKeyboardAction(new AutoIndentAction(true), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
 		
 	}
 	
 	public JMenu getDocMenu(){
 		return documentMenu;
 	}
 	
 	/**
 	 * creates a doc menu for the document
 	 * @param theDocument
 	 * @return
 	 */
 	private DocumentMenu createDocumentMenu(TheDocument theDocument){
 		CutCommand cut = new CutCommand(theDocument);
 		PasteCommand paste = new PasteCommand(theDocument);
 		InsertCommand insert = new InsertCommand(theDocument);
 		IndentCommand indent = new IndentCommand(theDocument);
 		SaveCommand save = new SaveCommand(theDocument);
 		SaveAsCommand saveAs = new SaveAsCommand(theDocument);
 		ToggleWrapCommand toggleWrap = new ToggleWrapCommand(theDocument);
 		ToggleIndentCommand toggleIndent = new ToggleIndentCommand(theDocument);
 		DocumentMenu docMenu = new DocumentMenu(cut,paste,insert,indent,save,saveAs,toggleWrap,toggleIndent);
 		return docMenu;
 	}
 	
 
 	/**
 	 * Gets the text in the JTextArea
 	 * @return the text
 	 */
 	public String getText() {
 		return textArea.getText();
 	}
 	
 	/**
 	 * listener for each of the document buttons
 	 * @author Chris Timmons
 	 *
 	 */
 	private class DocumentMenuListener implements ActionListener{
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			String action = e.getActionCommand();
 			if(action == "Save"){
 			    try {
                     thisDoc.getReader().quickParse(textArea.getText());
                 } catch (Exception ex){
                     thisDoc.setWellFormed(false);
                 }
 			    
 			    if(thisDoc.isWellFormed() == true){
 			        thisDoc.save(textArea.getText());
 			    }
 			    else{
 			        int dialogButton = JOptionPane.YES_NO_OPTION;
 	                int result = JOptionPane.showConfirmDialog (null, "This document is not well formed. To save anyway, click YES. For auto-fix, click NO.","Warning",dialogButton);
 
 			        if (result == JOptionPane.YES_OPTION){
 			            thisDoc.save(textArea.getText());
 			        }
 			        else{
 			            thisDoc.setXml(textArea.getText(),"2");
 			            textArea.setText(thisDoc.getXml());
 			            thisDoc.setWellFormed(true);
 			        }
 			    }
 			} else if(action == "SaveAs"){
 			    try {
                     thisDoc.getReader().quickParse(textArea.getText());
                 } catch (Exception ex){
                     thisDoc.setWellFormed(false);
                 }
                 if(thisDoc.isWellFormed() == true){
                     thisDoc.save(textArea.getText());
                 }
                 else{
                     int dialogButton = JOptionPane.YES_NO_OPTION;
                     int result = JOptionPane.showConfirmDialog (null, "This document is not well formed. To save anyway, click YES. For auto-fix, click NO.","Warning",dialogButton);
 
                     if (result == JOptionPane.YES_OPTION){
                         thisDoc.save(textArea.getText());
                     }
                     else{
                         thisDoc.setXml(textArea.getText(),"2");
                         textArea.setText(thisDoc.getXml());
                         thisDoc.setWellFormed(true);
                     }
                 }
 			} else if(action == "Copy"){
 			} else if(action == "Cut"){
 				docMenu.cut();
 			} else if(action == "Paste"){
 				docMenu.paste();
 			} else if (action == "On "){
 				docMenu.toggleWrap();
 			} else if (action == "Off "){
 				docMenu.toggleWrap();
 			} else if (action == "On"){
 			    textArea.registerKeyboardAction(new AutoIndentAction(true), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
 			} else if (action == "Off"){
 			    textArea.registerKeyboardAction(new AutoIndentAction(false), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);
 			}
 		}		
 	}
 	
 	/**
 	 * listener for each of the inserts
 	 * @author Chris Timmons
 	 *
 	 */
 	private class InsertListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			InsertButton inB = (InsertButton)e.getSource();
 			//String insertTag = inB.getHtmlTag();
 			String action = e.getActionCommand();
 			int pos = textArea.getCaretPosition();
 			if ((action.equals("<b> Bold </b>")) || (action.equals("<i> Italic </i>")) || (action.equals("<h1> Header </h1>"))) {
 				docMenu.insert();
 				textArea.insert(inB.getName() + "\n", pos);
 			}
 			else if (action.equals("Insert Table")) {
 				textArea.insert("\n\t</table>\n", pos);
 				for (int i=0;i<3;i++) {
 					textArea.insert("\n\t\t</tr>", pos);
 					for (int j=0;j<3;j++) {
 						textArea.insert("\n\t\t\t<td></td>", pos);
 					}
 					textArea.insert("\n\t\t<tr>", pos);
 				}
 				textArea.insert("\n\t<table>", pos);
 			}
 			else if (action.equals("Insert List")) {
 				textArea.insert("\n\t</ul>\n\n", pos);
 				for (int i=0;i<5;i++) {
 					textArea.insert("\n\t\t<li></li>", pos);
 				}
 				textArea.insert("\n\t<ul>", pos);
 			}
 		}
 		
 	}
 	
 	/**
 	 * extends JButtons to have names and html tags
 	 * @author Chris Timmons
 	 *
 	 */
     private class InsertButton extends JButton{
 		private String htmlTag;
 		private String name;
 		
 		/**
 		 * creates the button based on the parameters
 		 * @param nameParam
 		 * @param tag
 		 */
 		public InsertButton(String nameParam,String tag){
 			super(nameParam);
 			name = nameParam;
 			htmlTag = tag;
 		}
 		
 		/**
 		 * 
 		 * @return htmlTag
 		 */
 		@SuppressWarnings("unused")
         public String getHtmlTag(){
 			return htmlTag;
 		}
 		
 		/**
 		 * @return name
 		 */
 		@Override
         public String getName() {
 			return name;
 		}
 	}
 
 	
 }
