 package GUI;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileFilter;
 
 import Commands.*;
 import Entities.EditorMenu;
 import Entities.HTML_Editor;
 import Entities.TheDocument;
 
 /**
  * GUI for the text editor. 
  * @author Chris Timmons
  * @author Ben Kantor (bdk3079@rit.edu) *
  */
 @SuppressWarnings("serial")
 public class EditorGUI extends JFrame implements Observer {
 	
 	private HTML_Editor editor;
 	private EditorMenu editorMenu;
 	private ArrayList<TheDocument> openDocs = new ArrayList<TheDocument>();	
 	private JTabbedPane docsPanel;
 	private JFileChooser fileChooser;
 	private JMenuBar menuBar;
 	private JMenu editMenu, documentMenu;
 	private JMenuItem newDoc,openDoc,closeDoc,terminate;
 	
 	
 	/**
 	 * Constructor for the GUI
 	 * @param htmlEditor HTML_Editor that the GUI is representing
 	 */
 	public EditorGUI(HTML_Editor htmlEditor){
 		//Setup Editor Frame
 		editor = htmlEditor;
 		editor.addObserver(this);
 		editorMenu = createEditorMenu();
 		EditorMenuListener editMenuListener = new EditorMenuListener();
 		BorderLayout thisLayout = new BorderLayout();
 		this.setLayout(thisLayout);
 		
 		//TabbedPane
 		docsPanel = new JTabbedPane();
 		DocPaneListener paneListener = new DocPaneListener();
 		docsPanel.addChangeListener(paneListener);
 		
 		//MenuBar & EditorMenu
 		menuBar = new JMenuBar();
 		editMenu = new JMenu("Editor");
 		newDoc = new JMenuItem("New");
 		openDoc = new JMenuItem("Open");
 		closeDoc = new JMenuItem("Close Document");
 		terminate = new JMenuItem("Exit Editor");
 		newDoc.addActionListener(editMenuListener);
 		openDoc.addActionListener(editMenuListener);
 		closeDoc.addActionListener(editMenuListener);
 		terminate.addActionListener(editMenuListener);
 		editMenu.add(newDoc);
 		editMenu.add(openDoc);
 		editMenu.add(closeDoc);
 		editMenu.add(terminate);
 		menuBar.add(editMenu);	
 		
 		//Default DocumentMenu
 		documentMenu = new JMenu("Document");
 		documentMenu.setEnabled(false);
 		menuBar.add(documentMenu);
 		
 		//File Chooser
 		fileChooser = new JFileChooser();
 		HTMLFileFilter htmlFilter = new HTMLFileFilter();
 		fileChooser.setFileFilter(htmlFilter);
 		
 		//Add Components to Frame		
 		add(menuBar, BorderLayout.NORTH);		
 		add(docsPanel, BorderLayout.CENTER);
 		
 		//Window Settings
		setPreferredSize(new Dimension(1000,800));
 		setTitle("HTML Editor");
 		pack();
 		setLocationRelativeTo(null);
 		setVisible(true);
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
 	}
 	
 	/**
 	 * creates a menu of editor commands
 	 * @return editorMenu
 	 */
 	private EditorMenu createEditorMenu(){
 		NewCommand newFile = new NewCommand(editor);
 		OpenCommand open = new OpenCommand(editor);
 		CloseCommand close = new CloseCommand(editor);
 		TerminateCommand terminate = new TerminateCommand(editor);
 		EditorMenu editorMenu = new EditorMenu(newFile,open,close,terminate);
 		return editorMenu;
 	}
 	
 	/**
 	 * class for the editor's menu buttons
 	 * @author Chris Timmons
 	 *
 	 */
 	private class EditorMenuListener implements ActionListener{
 		
 		private JFrame newDocGUI;
 		private JTextField newDocField;
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			String action = e.getActionCommand();
 			if (action == "New"){
 				fileChooser = new JFileChooser();
 				fileChooser.setApproveButtonText("Create New");
 				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 				fileChooser.setFileFilter(new FileFilter(){
 					@Override
 					public boolean accept(File f) {
 						return f.isDirectory();
 					}
 					@Override
 					public String getDescription() {
 						return "HTML File Location";
 					}
 					
 				});
 		        int returnValue = fileChooser.showDialog(null,"Choose New File Location");
 		        if (returnValue == JFileChooser.APPROVE_OPTION) {
 		          File selectedFile = fileChooser.getSelectedFile();
 		          newDocGUI = new JFrame(selectedFile.getPath());
 		          JLabel newdocLabel = new JLabel("Document Name: ");
 		          JButton createNewBtn = new JButton("Accept");
 		          JButton cancelBtn = new JButton("Cancel");
 		          JLabel fileTypeLabel = new JLabel(".html");
 		          newDocField = new JTextField();
 		          newDocGUI.setLayout(new GridLayout(1,5));
 		          newDocGUI.add(newdocLabel);
 		          newDocGUI.add(newDocField);
 		          newDocGUI.add(fileTypeLabel);
 		          newDocGUI.add(createNewBtn);
 		          newDocGUI.add(cancelBtn);
 		          createNewBtn.addActionListener(new NewDocListener());
 		          cancelBtn.addActionListener(new NewDocListener());
 		          newDocGUI.setResizable(false);
 		          newDocGUI.setLocationRelativeTo(null);
		          newDocGUI.pack();
 		          newDocGUI.setVisible(true);
 		        }
 		        
 				
 			} else if(action == "Open"){
 				HTMLFileFilter htmlFilter = new HTMLFileFilter();
 				fileChooser = new JFileChooser();
 				fileChooser.setFileFilter(htmlFilter);
 		        int returnValue = fileChooser.showOpenDialog(null);
 		        if (returnValue == JFileChooser.APPROVE_OPTION) {
 		          File selectedFile = fileChooser.getSelectedFile();
 		          editorMenu.open(selectedFile.getPath());
 		        }
 			} else if(action == "Close Document"){
 				Integer index = docsPanel.getSelectedIndex();
 				if (index != -1){
 					editorMenu.close(index);
 				}
 				
 			} else if(action == "Exit Editor"){
 				editorMenu.terminate();
 				dispose();
 			}
 		}
 		
 		/**
 		 * listener for the newDocGUI
 		 * @author Chris Timmons
 		 *
 		 */
 		private class NewDocListener implements ActionListener{
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String action = e.getActionCommand();
 				if (action == "Accept") {
 					if ((newDocField.getText().matches("[a-zA-Z0-9]+")) && (newDocField.getText().length() > 0)) {
 						String newDocName = newDocGUI.getTitle() + "\\" + newDocField.getText() +".html";
 						editorMenu.newFile(newDocName);
 						newDocGUI.dispose();
 					}
 				}
 				else {
 					newDocGUI.dispose();
 				}
 			}
 		}
 		
 	}
 	
 	/**
 	 * file filter for html files
 	 * @author Chris Timmons
 	 *
 	 */
 	private class HTMLFileFilter extends FileFilter{
 
 		@Override
 		public boolean accept(File f) {
 			return f.isDirectory() || f.getName().toLowerCase().endsWith(".html");
 		}
 
 		@Override
 		public String getDescription() {
 			return ".html";
 		}
 		
 	}
 
 	@Override
 	/**
 	 * Observable update method
 	 */
 	public void update(Observable obs, Object arg1) {
 		updateDocs(editor.getDocs(), (Integer)arg1);		
 	}
 	
 	/**
 	 * updates the gUI to show the current state of the docs
 	 * @param docs
 	 * @param change
 	 */
 	private void updateDocs(ArrayList<TheDocument> docs, Integer change){
 		Integer arraySize = docs.size();
 		if(arraySize > openDocs.size()){
 			TheDocument newDoc = docs.get(change);
 			openDocs.add(newDoc);
 			DocumentGUI newDocGUI = new DocumentGUI(newDoc);
 			docsPanel.add(newDoc.getName(),newDocGUI);
 			validate();
 			repaint();
			pack();
 			setLocationRelativeTo(null);
 		} else if (arraySize < openDocs.size()){
 			openDocs.remove(change);
 			docsPanel.remove(change);
 			validate();
 			repaint();
			pack();
 			setLocationRelativeTo(null);
 		}
 	}
 	
 	private class DocPaneListener implements ChangeListener{
 
 
 		@Override
 		public void stateChanged(ChangeEvent arg0) {	
 			if(docsPanel.getTabCount() != 0){
 			DocumentGUI docGUI = (DocumentGUI)docsPanel.getSelectedComponent();
 			menuBar.remove(documentMenu);
 			documentMenu = docGUI.getDocMenu();
 			documentMenu.setEnabled(true);
 			menuBar.add(documentMenu);
 			validate();
 			repaint();
			pack();
 			setLocationRelativeTo(null);
 			}
 		}
 		
 	}
 }
