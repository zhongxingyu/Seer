 package org.vamdc.validator.gui.mainframe;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.Map;
 
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.JTextComponent;
 
 import org.vamdc.dictionary.HeaderMetrics;
 import org.vamdc.validator.Setting;
 import org.vamdc.validator.ValidatorMain;
 import org.vamdc.validator.gui.search.SearchData;
 import org.vamdc.validator.gui.search.SearchPanel;
 import org.vamdc.validator.gui.settings.SettingsPanel;
 import org.vamdc.validator.interfaces.DocumentError;
 import org.vamdc.validator.interfaces.XSAMSIOModel;
 
 
 public class MainFrameController implements ActionListener {
 
 	public static class XsamsPanelController extends TextPanelController {
 
 
 		public XsamsPanelController(TextPanel model, XSAMSIOModel xsamsdoc){
 			super(model);
 		}
 
 		@Override
 		public void clickedLine(int lineNum) {
 			panel.centerLine(lineNum);
 		}
 	}
 
 	public static class ValidationPanelController extends TextPanelController{
 
 		private XSAMSIOModel xsamsdoc;
 		private TextPanel xsamsPanel;
 
 		public ValidationPanelController(TextPanel valPanel,
 				XSAMSIOModel document, TextPanel xsamsPanel) {
 			super(valPanel);
 			this.xsamsdoc = document;
 			this.xsamsPanel = xsamsPanel;
 		}
 
 		@Override
 		public void clickedLine(int lineNum) {
 			DocumentError clickedError = xsamsdoc.getElementsLocator().getErrors().get((int) lineNum);
 			xsamsPanel.setHighlight(clickedError.getElement(), Color.RED);
 			xsamsPanel.centerLine((int)clickedError.getElement().getFirstLine());
 			centerError(clickedError);
 			
 		}
 
 		private void centerError(DocumentError clickedError) {
 			try {
 				int line = (int)clickedError.getElement().getLastLine()-xsamsPanel.getDocPosition();
 				if (line>xsamsPanel.getWindowRows())
 					return;
 				int ls = xsamsPanel.getTextArea().getLineStartOffset(line);
 				xsamsPanel.getTextArea().setCaretPosition(ls+(int)clickedError.getElement().getLastCol());
 			} catch (BadLocationException e) {
 			}
 		}
 
 	}
 
 	private static class RestrictablesController extends TextPanelController{
 		private JTextComponent query;
 		private RestrictsPanel model;
 
 
 		public RestrictablesController(RestrictsPanel model, JTextComponent query) {
 			super(model);
 			this.query = query;
 			this.model = model;
 		}
 
 		@Override
 		public void clickedLine(int lineNum) {
 			//Add selected restrictable to the end of query string.
 			if (this.query==null || this.query.getText()==null)
 				return;
 			String text = this.query.getText().trim();
 
 			if (text.endsWith(";"))
 				text = text.substring(0, text.length()-1);
 
 			String restr = model.getRestrictable((int)lineNum);
 
 			text+=" "+restr;
 			this.query.setText(text);
 			int len = text.length();
 			query.setCaretPosition(len);
 			query.requestFocusInWindow();
 
 		}
 
 	}
 
 
 
 	private XSAMSIOModel doc;
 	private MainFrame frame;
 	private Thread inputThread; //Thread for xsams input
 
 	public final LocatorPanelController locController; 
 	private JDialog settingsFrame,searchFrame;
 	private SearchData search;
 	private final JFileChooser saveChooser;
 	private final JFileChooser loadChooser;
 
 	public MainFrameController(XSAMSIOModel doc,MainFrame frame){
 		this.doc=doc;
 		this.frame=frame;
 		
 		initSettings(frame);
 
 		initSearch(frame);
 		
 		//Init text panel controllers
 		new XsamsPanelController(frame.xsamsPanel,this.doc);
 		new ValidationPanelController(frame.valPanel,this.doc,frame.xsamsPanel);
 		new RestrictablesController(frame.restrictPanel,frame.getQueryField());
 
 		saveChooser = new JFileChooser();
 		File cdir = new File(Setting.GUIFileSavePath.getValue());
 		saveChooser.setCurrentDirectory(cdir);
 
 		loadChooser = new JFileChooser();
 		File fodir = new File(Setting.GUIFileOpenPath.getValue());
 		loadChooser.setCurrentDirectory(fodir);
 
 		locController = new LocatorPanelController(doc,frame.xsamsPanel);
 	}
 
 	private void initSettings(MainFrame frame) {
 		settingsFrame = new JDialog(frame,"Settings");
 		settingsFrame.setContentPane(new SettingsPanel(this));
 		settingsFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 		settingsFrame.setModal(true);
 	}
 
 	private void initSearch(MainFrame frame) {
 		searchFrame = new SearchPanel(frame,"Search",this);
 		search = ((SearchPanel)searchFrame).getSearch();
 		frame.xsamsPanel.setSearch(search);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		String command = event.getActionCommand();
 		System.out.println(command);
 
 		if (command == MainFrame.DO_QUERY){
 			handleDoQuery(false);
 		}else if (command == MainFrame.PRE_QUERY){
 			handleDoQuery(true);
 		}else if (command == MainFrame.STOP_QUERY){
 			if (inputThread!=null){
 				doc.stopQuery();
 			}
 		}else if (command == MenuBar.CMD_FIND){
 			searchFrame.pack();
 			searchFrame.setVisible(true);
 		}else if (command == MenuBar.CMD_EXIT){
 			if (JOptionPane.showConfirmDialog(frame, "Do you really want to quit?", "Quit", JOptionPane.YES_NO_OPTION)==JOptionPane.OK_OPTION)
 				System.exit(0);
 		}else if (command == MenuBar.CMD_FINDNEXT){
 			search();
 		}else if (command == MenuBar.CMD_CONFIG){
 			settingsFrame.pack();
 			settingsFrame.setVisible(true);
 		}else if (command == MenuBar.CMD_OPEN){
 			handleFileOpen();
 		}else if (command == MenuBar.CMD_SAVE){
 			handleFileSave();
 		}else if (command == MenuBar.CMD_ABOUT){
 			JOptionPane.showMessageDialog(frame, ValidatorMain.ABOUT_MESSAGE);
 		}
 	}
 
 	public void search() {
 		frame.xsamsPanel.centerLine(searchNext(frame.xsamsPanel.getDocCenter()));
 	}
 	
 	/**
 	 * Handle search
 	 */
 	public int searchNext(int startLine){
 		String searchText = search.getSearchText();
 		if (searchText==null || searchText.equals("")) return -1;
 		int foundLine = doc.searchString(searchText, startLine,search.ignoreCase());
 		if (foundLine==-1){
 			switch (JOptionPane.showConfirmDialog(
 					frame,
 					"String "+searchText+" not found, start from the beginning?",
 					"Search",
 					JOptionPane.YES_NO_OPTION))
 					{
 					case JOptionPane.OK_OPTION:
 						foundLine = doc.searchString(searchText,0,search.ignoreCase());
 						break;
 					case JOptionPane.NO_OPTION:
 						return -1;
 					}
 		}
 		if (foundLine==-1){
 			JOptionPane.showMessageDialog(frame, "String "+searchText+" not found.","Search",JOptionPane.INFORMATION_MESSAGE);
 			return -1;
 		}
 
 		return foundLine;
 	}
 
 	/**
 	 * Handle query action
 	 */
 	private void handleDoQuery(final boolean isPreview){
 		//Save query
 		final String query = frame.getQuery();
 		if (inputThread==null){
 			//Create separate thread for query execution
 			inputThread = new Thread( new Runnable(){
 				public void run() {
 					try{
 						frame.progress.setIndeterminate(true);
 						if (isPreview){
 							processHeaders(doc.previewQuery(query));
 							frame.progress.setIndeterminate(false);
 						}
 						else
 							doc.doQuery(query);
 					}catch (Exception e){
 						JOptionPane.showMessageDialog(frame, "Exception during query: "+e.getMessage(),"Query",JOptionPane.ERROR_MESSAGE);
 						frame.progress.setIndeterminate(false);
 						e.printStackTrace();
 					}finally{
 						inputThread = null;
 					}
 				}
 			});
 
 			inputThread.start();
 
 		}
 	}
 	protected void processHeaders(Map<HeaderMetrics, String> previewQuery) {
 		StringBuilder message=new StringBuilder();
 		for (HeaderMetrics metric:previewQuery.keySet()){
 			message.append(metric.name().replace("_", "-")).append(":");
 			message.append(previewQuery.get(metric)).append("\n");
 		}
 		JOptionPane.showMessageDialog(frame,message.toString());
 	}
 
 	/**
 	 * Handle file open action
 	 */
 	private void handleFileOpen(){
 		//Show open dialog
 		if (loadChooser.showOpenDialog(frame)==JFileChooser.APPROVE_OPTION){
 
 			final File filename = loadChooser.getSelectedFile();
 			if (filename.exists() && filename.canRead()&& inputThread==null){
 				//Save new file path
 				Setting.GUIFileOpenPath.setValue(filename.getPath(),true);
 				//Create a thread processing file
 				inputThread = new Thread( new Runnable(){
 					@Override
 					public void run() {
 						try{
 							doc.loadFile(filename);
 						}catch (Exception ex){
 							JOptionPane.showMessageDialog(frame, "Exception during open: "+ex.getMessage(),"Open",JOptionPane.ERROR_MESSAGE);
 						}finally{
 							inputThread=null;
 						}
 					}
 				});
 				inputThread.start();
 			}
 		}
 	}
 	/**
 	 * Handle file save action
 	 */
 	private void handleFileSave() {
 		//Show save dialog
 		if(saveChooser.showSaveDialog(frame)==JFileChooser.APPROVE_OPTION){
 			//If selected file
 			File filename = saveChooser.getSelectedFile();
 			//Check if file exists, ask user to overwrite
 			if (!filename.exists() || (filename.exists() && JOptionPane.showConfirmDialog(
 					frame,
 					"File "+filename.getAbsolutePath()+" already exists! Overwrite?",
 					"Save",
 					JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION)){
 
 				//Save path in preferences for future use
 				Setting.GUIFileSavePath.setValue(filename.getPath(),true);
 				
 				//Tell storage to save file
 				try{
 					doc.saveFile(filename);
 					JOptionPane.showMessageDialog(frame, "File "+filename.getAbsolutePath()+" written successfully.","Save",JOptionPane.INFORMATION_MESSAGE);
 				}catch (Exception ex){
 					JOptionPane.showMessageDialog(frame, "Exception during save: "+ex.getMessage(),"Save",JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Update child components, reload settings.
 	 */
 	public void reloadDocument(){
 		try{
 			doc.reconfigure();
 			settingsFrame.setVisible(false);
 			frame.updateFromModel(true);
 		}catch (Exception e){
 			JOptionPane.showMessageDialog(settingsFrame, "Exception while applying new settings: "+e.getMessage(),"Settings",JOptionPane.ERROR_MESSAGE);
 		}
 
 	}
 
 
 
 
 
 
 
 }
