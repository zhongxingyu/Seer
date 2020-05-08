 package judo;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.PlainDocument;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Scanner;
 import java.util.Collections;
 import com.petebevin.markdown.*;
 
 
 public class MainEditorView extends JFrame implements Observer {
 
 	/**
 	 * Need this as JFrame is serializable...?
 	 */
 	private static final long serialVersionUID = 1L;
 	private static ImageIcon fileIcon = new ImageIcon("resources/icons/markdownBtns/new.png");
 	private JTabbedPane tabbedPane;
 	private ArrayList<EditableDocument> docs; //these are our models
 	private JudoEditorController controller;
 	//private ArrayList<JTextArea> textareas = new ArrayList<JTextArea>();
 	private final MarkdownProcessor mp = new MarkdownProcessor();
 	private EditableDocument currentDocument;
 	
 	public JTabbedPane getTabbedPane()
 	{
 		return this.tabbedPane;
 	}
 	
 	/* MAINEDITORVIEW 
 	 * Second constructor takes in a model
 	 * *********************************** */
 	public MainEditorView( ArrayList<EditableDocument> docs, JudoEditorController controlr)
 	{
 		this();
 		this.setDocs(docs);
 		this.controller = controlr;
 		//textareas.add(0, docs.getDocumentByIndex(0)); maybe my model is just a text area?
 				
 	}
 		
 	/**
 	 * Main Editor View Constructor
 	 * */
 	public MainEditorView()
 	{
 		this.setSize(900, 800);
 		this.setTitle("Judo Editor");
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		final Container contentPane = this.getContentPane();
 		contentPane.setLayout(new BorderLayout(5, 3));
 		
 		JPanel topPanel = new JPanel(new FlowLayout());
 		JPanel fileActionsPanel = createFileOpsPanel();
 		JPanel markdownBtnsPanel = createMrkdwnBtns(); //will go inside menuPanel
 		JPanel tabsPanel = new JPanel(); //will go inside menuPanel below markdownPanel
 		
 		 
 		JMenuBar menuBar = new JMenuBar();
 		/* MENUBAR  */
         JMenu fileMenu = new JMenu("File");
         /*
         JMenu editMenu = new JMenu("Edit");
         JMenu viewMenu = new JMenu("View");
         JMenu helpMenu = new JMenu("Help");
         */       
         
         
         /*    FILEMENU ITEMS  */
         JMenuItem newItem = new JMenuItem("New", 'N');
         fileMenu.add(newItem);
         //add action listener for NEW
         newItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 try {
                     controller.CreateNewDocument();
                     // JOptionPane.showMessageDialog(contentPane, "Save Complete!");
                 } catch (Exception e) {
                     //JOptionPane.showMessageDialog(contentPane, "Save Error!");
                 }
             }
         });
         
         JMenuItem openItem = new JMenuItem("Open...", 'O');
         fileMenu.add(openItem);
         openItem.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) 
             {
             	CallOpenFunction();
             }
         });
 
         
         fileMenu.addSeparator(); /*---------------------*/
         
         JMenuItem saveItem = new JMenuItem("Save", 'S');
         fileMenu.add(saveItem);
         saveItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 try {
                 	CallSaveFunction();
                     JOptionPane.showMessageDialog(contentPane, "Save Complete!");
                 } catch (Exception e) 
                 {
                     JOptionPane.showMessageDialog(contentPane, "There was an Error saving!" + e.getMessage());
                 }
             }
         });
         
         
 //        JMenuItem saveAsItem = new JMenuItem("Save As...");
 //        fileMenu.add(saveAsItem);
         
         fileMenu.addSeparator(); /*---------------------*/
         
         final JMenuItem previewItem = new JMenuItem("Preview", 'V');
         fileMenu.add(previewItem);
         previewItem.addActionListener(new ActionListener(){
         	
         	@Override
         	public void actionPerformed(ActionEvent arg0) {
         		if(currentDocument != null)
         		ProcessPreview();
         	}
         });
     
 //        JMenuItem printItem = new JMenuItem("Print", 'P');
 //        fileMenu.add(printItem);
         
         fileMenu.addSeparator(); /*---------------------*/
         final JMenuItem quitItem = new JMenuItem("Quit", 'Q');
         fileMenu.add(quitItem);
         quitItem.addActionListener(new ActionListener(){
         	
         	@Override
         	public void actionPerformed(ActionEvent arg0) {
         		CloseEditor();
         	}
         });
         
         
         
         /* Adding Menus to bar*/
         menuBar.add(fileMenu);
         //menuBar.add(editMenu);
         //menuBar.add(viewMenu);
         //menuBar.add(helpMenu);
         
         /*Buttons*/
         
         /* default new file tab*/
 //        final JTextArea ta1 = new JTextArea();
 //        textareas.add(ta1);
 //        final JScrollPane panelta1 = new JScrollPane(ta1);
 //        panelta1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 //        panelta1.setPreferredSize(new Dimension(800	, 600));
         
         
         /* CENTER tabbed pane*/
         tabbedPane = new JTabbedPane();
         tabbedPane.setPreferredSize(new Dimension(800, 600));
         tabbedPane.addChangeListener(new ChangeListener(){
 
 			@Override
 			public void stateChanged(ChangeEvent arg0) {
 				if(arg0 != null)
 				{
 					int selIndex = getTabbedPane().getSelectedIndex();
 					JScrollPane scp = (JScrollPane)tabbedPane.getComponentAt(selIndex);
 					for(EditableDocument doc : docs)
 					{
 						if(doc.getScrollpane() == scp)
 							setCurrentDocument(doc);
 					}
 				}				
 			}
         	
         });
         //this.tabbedPane.addTab("new", fileIcon, panelta1);
         //this.tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
         
         tabsPanel.add(tabbedPane);
         //tabsPanel.set
         contentPane.add("Center", tabsPanel);
         
         /* setting menu/tool bar to top of pane */
         setJMenuBar(menuBar);
         /*setting other Jpanels*/
         contentPane.add("West", fileActionsPanel);
         topPanel.add(markdownBtnsPanel);
         contentPane.add("North", topPanel);
         
         
         /* actionListeners */
         ActionListener listener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent event) {
 //            	if (event.getSource() == openBtn){
 //            		
 //            	}
             }
         };
         
         
 	} // end of default constructor
 
 	private JPanel createFileOpsPanel() {
 		
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 
 		
 		final JButton newBtn = new JButton(new ImageIcon("resources/icons/fileops/new.png"));
 		newBtn.setToolTipText("New File");
 		
 		final JButton openBtn = new JButton(new ImageIcon("resources/icons/fileops/folder.png"));
 		openBtn.setToolTipText("Open File");
 		
 		final JButton saveBtn = new JButton(new ImageIcon("resources/icons/fileops/save.png"));
 		saveBtn.setToolTipText("Save Current Doc");
 		
 //		JButton savecopyBtn = new JButton(new ImageIcon("resources/icons/fileops/savecopy.png"));
 //		saveBtn.setToolTipText("Save Current Doc");
 		
 		final JButton closeBtn = new JButton(new ImageIcon("resources/icons/fileops/close.png"));
 		closeBtn.setToolTipText("Close Current File");
 		
 		ActionListener fileActionsListener = new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent event) {
             	
             		try
             		{
             			if (event.getSource() == openBtn){
             				CallOpenFunction();
             			}
             			else if (event.getSource() == newBtn)
             			{
             				CallNewFunction();
             			}
             			else if (event.getSource() == closeBtn)
             			{
             				closeCurrentDoc();
             			}
             			else if (event.getSource() == saveBtn)
             			{
             				CallSaveFunction();
             			}
 	                }
 	            	catch (Exception e)
 	                {
 	                    //JOptionPane.showMessageDialog("There was an Error saving!");
 	            		e.printStackTrace();
 	                }
             	
             }
         };
         // attach listeners to buttons
 		openBtn.addActionListener(fileActionsListener);
 		newBtn.addActionListener(fileActionsListener);
 		saveBtn.addActionListener(fileActionsListener);
 		closeBtn.addActionListener(fileActionsListener);
 		//addActionListener(fileActionsListener);
 		
 		
 		buttonPanel.add(newBtn);
 		buttonPanel.add(openBtn);
 		buttonPanel.add(saveBtn);
 		// buttonPanel.add(savecopyBtn);
 		buttonPanel.add(closeBtn);
 		//buttonPanel.add();
 		return buttonPanel;
 	} // END OF CreateFileOpsPanel
 	
 	
 	/**
 	 * CallSaveFunction
 	 * */
 	public void CallSaveFunction()
 	{
 		String filename = "";
 		if (this.getCurrentDocument().getFilepath().equals(""))
 		{
 			filename = JOptionPane.showInputDialog("Please specify a file path and name.");
 			getCurrentDocument().setFilepath(filename);
 			int index = tabbedPane.getSelectedIndex();
 			tabbedPane.setTitleAt(index, this.getCurrentDocument().getFilepath());
 		
 		}
		else
		{

 			String fp = JOptionPane.showInputDialog("Please specify where you want to save this file.");
 			this.getCurrentDocument().setFilepath(fp);
 		}
 		this.controller.saveDocument(this.getCurrentDocument());
 	}
 	
 	/**
 	 * CallSaveFunction
 	 * */
 	public void CallNewFunction()
 	{
 		
 		this.controller.CreateNewDocument();
 	}
 	
 	/*
 	 * Create and wires up the button to their respective action listeners 
 	 * */
 	private JPanel createMrkdwnBtns() {
 
 		ActionListener ltnr;
 		JPanel panel = new JPanel(new FlowLayout());
 
 		final JButton boldBtn = new JButton(new ImageIcon("resources/icons/markdownBtns/bold.png"));
 		boldBtn.setToolTipText("Bold");
 				
 		final JButton italicBtn = new JButton(new ImageIcon("resources/icons/markdownBtns/italic.png"));
 		italicBtn.setToolTipText("Italics");
 		
 		final JButton h1Btn = new JButton(new ImageIcon("resources/icons/markdownBtns/h1.png"));
 		h1Btn.setToolTipText("<H1>");
 		
 		final JButton h2Btn = new JButton(new ImageIcon("resources/icons/markdownBtns/h2.png"));
 		h2Btn.setToolTipText("<H2>");
 		
 		final JButton h3Btn = new JButton(new ImageIcon("resources/icons/markdownBtns/h3.png"));
 		h3Btn.setToolTipText("<H3>");
 		
 		final JButton h4Btn = new JButton(new ImageIcon("resources/icons/markdownBtns/h4.png"));
 		h4Btn.setToolTipText("<H4>");
 		
 		final JButton h5Btn = new JButton(new ImageIcon("resources/icons/markdownBtns/h5.png"));
 		h5Btn.setToolTipText("<H5>");
 		
 		final JButton h6Btn = new JButton(new ImageIcon("resources/icons/markdownBtns/h6.png"));
 		h6Btn.setToolTipText("<H6>");
 		
 		final JButton bulletBtn = new JButton(new ImageIcon("resources/icons/markdownBtns/bullet.png"));
 		bulletBtn.setToolTipText("<ul> - bullet list");
 		
 		final JButton listBtn = new JButton(new ImageIcon("resources/icons/markdownBtns/list.png"));
 		listBtn.setToolTipText("<ol> - Numbered List");
 		final JButton linkBtn = new JButton(new ImageIcon("resources/icons/markdownBtns/link.png"));
 		linkBtn.setToolTipText("Insert hyperlink");
 		final JButton imageBtn = new JButton(new ImageIcon("resources/icons/markdownBtns/image.png"));
 		imageBtn.setToolTipText("Insert image");
 		
 		final JButton quoteBtn = new JButton(new ImageIcon("resources/icons/markdownBtns/quoteblock.png"));
 		quoteBtn.setToolTipText("Insert Quote Block");
 		
 		final JButton toHtmlBtn = new JButton(new ImageIcon("resources/icons/markdownBtns/html!.png"));
 		toHtmlBtn.setToolTipText("Convert To HTML!");
 		
 		//add actionListener for all other btns
 		ltnr = new ActionListener() {
 		            @Override
 		            public void actionPerformed(ActionEvent event)
 		            {
 		            	if (event.getSource() == boldBtn)
 		            		MakeBoldAtCurrentCaret();
 		            	else if (event.getSource() == italicBtn)
 		            		MakeItalicAtCurrentCaret();
 		            	else if (event.getSource() == h1Btn)
 		            		MakeH1AtCurrentCaret();
 		            	else if (event.getSource() == h2Btn)
 		            		MakeH2AtCurrentCaret();
 		            	else if (event.getSource() == h3Btn)
 		            		MakeH3AtCurrentCaret();
 		            	else if (event.getSource() == h4Btn)
 		            		MakeH4AtCurrentCaret();
 		            	else if (event.getSource() == h5Btn)
 		            		MakeH5AtCurrentCaret();
 		            	else if (event.getSource() == h6Btn)
 		            		MakeH6AtCurrentCaret();
 		            	else if (event.getSource() == bulletBtn)
 		                	MakeBulletAtCurrentCaret();
 		                else if (event.getSource() == listBtn)
 		                	MakeOListAtCurrentCaret();
 		                else if (event.getSource() == imageBtn)
 		                	MakeImageAtCurrentCaret();
 		                else if (event.getSource() == linkBtn)
 		                	MakeLinkAtCurrentCaret();
 		                else if (event.getSource() == quoteBtn)
 		                	MakeBlockQuoteAtCaret();
 		            	           
 		            }          
 		        };
 		        boldBtn.addActionListener(ltnr);
 				italicBtn.addActionListener(ltnr);
 				h1Btn.addActionListener(ltnr);
 				h2Btn.addActionListener(ltnr);
 				h3Btn.addActionListener(ltnr);
 				h4Btn.addActionListener(ltnr);
 				h5Btn.addActionListener(ltnr);
 				h6Btn.addActionListener(ltnr);
 				bulletBtn.addActionListener(ltnr);
 				listBtn.addActionListener(ltnr);
 				linkBtn.addActionListener(ltnr);
 				imageBtn.addActionListener(ltnr);
 				quoteBtn.addActionListener(ltnr);
 				
 		//final MarkdownProcessor m = new MarkdownProcessor();
 		toHtmlBtn.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent event) {
             	
             	ProcessPreview();
             }          
         });
 	
 		
 		panel.add(boldBtn);
 		panel.add(italicBtn);
 		panel.add(h1Btn);
 		panel.add(h2Btn);
 		panel.add(h3Btn);
 		panel.add(h4Btn);
 		panel.add(h5Btn);
 		panel.add(h6Btn);
 		panel.add(bulletBtn);
 		panel.add(listBtn);
 		panel.add(linkBtn);
 		panel.add(imageBtn);
 		panel.add(quoteBtn);
 		panel.add(toHtmlBtn);
 		
 		return panel;
 		
 	} //end of createMarkdownBtnns
 	
 	protected void MakeBlockQuoteAtCaret() {
 		try {
 			int caret = getCurrentDocument().getTextarea().getCaretPosition();
 			if (caret != -1)
 			getCurrentDocument().getDocument().insertString(caret, "> ", null);
 		} catch (BadLocationException e) {
 			
 			e.printStackTrace();
 		}
 	}
 
 	protected void MakeLinkAtCurrentCaret() {
 		try
 		{
 			int caret = getCurrentDocument().getTextarea().getCaretPosition();
 			getCurrentDocument().getDocument().insertString(caret, "[text](url)", null);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	protected void MakeImageAtCurrentCaret() {
 		// TODO Auto-generated method stub
 		try {
 			int caret = getCurrentDocument().getTextarea().getCaretPosition();
 			getCurrentDocument().getDocument().insertString(caret, "![alttext](imgurl)", null);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	protected void MakeOListAtCurrentCaret() {
 		// TODO Auto-generated method stub
 		try {
 			int caret = getCurrentDocument().getTextarea().getCaretPosition();
 			getCurrentDocument().getDocument().insertString(caret, "- ", null);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	protected void MakeBulletAtCurrentCaret() {
 		// TODO Auto-generated method stub
 		try {
 			int caret = getCurrentDocument().getTextarea().getCaretPosition();
 			getCurrentDocument().getDocument().insertString(caret, "* ", null);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	protected void MakeH6AtCurrentCaret() {
 		// TODO Auto-generated method stub
 		try {
 			int caret = getCurrentDocument().getTextarea().getCaretPosition();
 			getCurrentDocument().getDocument().insertString(caret, "###### ", null);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	protected void MakeH5AtCurrentCaret() {
 		// TODO Auto-generated method stub
 		try {
 			int caret = getCurrentDocument().getTextarea().getCaretPosition();
 			getCurrentDocument().getDocument().insertString(caret, "##### ", null);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	protected void MakeH4AtCurrentCaret() {
 		// TODO Auto-generated method stub
 		try {
 			int caret = getCurrentDocument().getTextarea().getCaretPosition();
 			getCurrentDocument().getDocument().insertString(caret, "#### ", null);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	protected void MakeH3AtCurrentCaret() {
 		// TODO Auto-generated method stub
 		try {
 			int caret = getCurrentDocument().getTextarea().getCaretPosition();
 			getCurrentDocument().getDocument().insertString(caret, "### ", null);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	protected void MakeH2AtCurrentCaret() {
 		// TODO Auto-generated method stub
 		try {
 			int caret = getCurrentDocument().getTextarea().getCaretPosition();
 			getCurrentDocument().getDocument().insertString(caret, "## ", null);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	protected void MakeH1AtCurrentCaret() {
 		// TODO Auto-generated method stub
 		try {
 			int caret = getCurrentDocument().getTextarea().getCaretPosition();
 			getCurrentDocument().getDocument().insertString(caret, "# ", null);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	protected void MakeItalicAtCurrentCaret() {
 		// TODO Auto-generated method stub
 		try {
 			int caret = getCurrentDocument().getTextarea().getCaretPosition();
 			getCurrentDocument().getDocument().insertString(caret, "_", null);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	protected void MakeBoldAtCurrentCaret() {
 		// TODO Auto-generated method stub
 		try {
 			int caret = getCurrentDocument().getTextarea().getCaretPosition();
 			if (caret != -1)
 			getCurrentDocument().getDocument().insertString(caret, "**", null);
 		} catch (BadLocationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * Process the markdown text in the current text Area and show a preview of HTML in default browser
 	 * */
 	private void ProcessPreview() {
 		EditableDocument currdoc = this.getCurrentDocument();
 		String str = "";
 		try {
 			str = currdoc.getDocument().getText(0, currdoc.getDocument().getLength());
 		} catch (/*BadLocationException*/ Exception e1) {
 			
 			JOptionPane.showMessageDialog(this,e1.getMessage());
 		}
 		String html = mp.markdown(str);
 		BufferedWriter outfile;
 		try
         {
             outfile = new BufferedWriter(new FileWriter("preview.html"));
             outfile.write(html);
 
             outfile.close();
         }
         catch (IOException e)
         {
             System.out.println("Something went bad saving your current input/settings");
         }
 		
 		File file = new File("preview.html");
 		try 
 		{
 			Desktop.getDesktop().open(file); // opens the preview HTML file in a default browser
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		// TODO set this to controller
 		
 	}
 
 	public ArrayList<EditableDocument> getDocs() {
 		return docs;
 	}
 
 	public void setDocs(ArrayList<EditableDocument> docs) {
 		this.docs = docs;
 	}
 	
 	
 	public void CloseEditor()
 	{
 		if(JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?") == 0)//this,"Are you sure you want to exit (unsaved data will be lost)" );
 		{
 			WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
 			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
         }
 	}
 
 	public EditableDocument getCurrentDocument() {
 		return currentDocument;
 	}
 
 	public void setCurrentDocument(EditableDocument currentDocument) {
 		this.currentDocument = currentDocument;
 	}
 	
 	public void CallOpenFunction()
 	{
 		
 		JFileChooser fileChooser = new JFileChooser();
 		JTextArea textArea = new JTextArea();
         int status = fileChooser.showOpenDialog(this);
         if (status == JFileChooser.APPROVE_OPTION)
         {
             File file = fileChooser.getSelectedFile();
             Scanner fileScanner = null;
             boolean noError = true;
             try {
                 fileScanner = new Scanner(file);
             } catch (FileNotFoundException e) {
                 noError = false;
                 JOptionPane.showMessageDialog(this, "File not found!");
             }
             String textfromfile = "";
             while (noError && fileScanner.hasNextLine()) {
             	textfromfile += fileScanner.nextLine()+"\n";
             }
             
             Document pd = (Document)new PlainDocument();
             try
             {
 				pd.insertString(pd.getLength(), textfromfile, null);
 			}
             catch (BadLocationException e1)
             {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
             
             pd.addDocumentListener(new DocumentListener(){
 
 				@Override
 				public void changedUpdate(DocumentEvent e) {
 					// TODO Auto-generated method stub
 					SetFileToUnsaved();
 					
 				}
 
 				@Override
 				public void insertUpdate(DocumentEvent e) {
 					SetFileToUnsaved();
 					
 				}
 
 				@Override
 				public void removeUpdate(DocumentEvent e) {
 					// TODO Auto-generated method stub
 					SetFileToUnsaved();
 					
 				}
             	
             });
             
             // add new scroll pane, textArea, and doc to model:
             EditableDocument edoc = new EditableDocument(new JScrollPane(), new JTextArea(), pd, file.getPath());
             this.docs.add(edoc);
             //add it to View tabs
             
             edoc.getScrollpane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
             edoc.getScrollpane().setPreferredSize(new Dimension(800	, 600));
         	this.tabbedPane.add(edoc.getFilepath(),edoc.getScrollpane());
         }
 	}
 
 	protected void SetFileToUnsaved() {
 		// TODO Auto-generated method stub
 		this.getCurrentDocument().setIsunsaved(true);
 		
 	}
 
 	protected void closeCurrentDoc()
 	{
 		if(getCurrentDocument().isIsunsaved())
 		{
 			if(JOptionPane.showConfirmDialog(this.getContentPane(), getCurrentDocument().getFilepath() + " has unsaved changes. Are you sure you want to close it?") == 0){
 				CloseCurrentTab();
 				return;
 			}
 		}
 		else { 
 			CloseCurrentTab();
 		}
 	}
 
 	private void CloseCurrentTab() {
 		// TODO Auto-generated method stub
 		//omponent panetoclose =	tabbedPane.get
 		int sel = tabbedPane.getSelectedIndex();
 		tabbedPane.remove(sel);
 	}
 	
 } // end of MainEditorView class
