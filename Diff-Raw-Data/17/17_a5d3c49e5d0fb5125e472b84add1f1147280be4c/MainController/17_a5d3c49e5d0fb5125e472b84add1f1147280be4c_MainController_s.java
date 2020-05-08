 package gui.controller;
 
 import misc.Editor;
 import misc.ExecutionHandler;
 import misc.MessageSystem;
 import misc.Settings;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.graphics.Image;
 
 import gui.AboutFrame;
 import gui.EvaluationFrame;
 import gui.FileFrame;
 import gui.HelpFrame;
 import gui.MainFrame;
 import gui.SettingsFrame;
 import gui.RandomTestFrame;
 
 public class MainController implements SelectionListener, MouseListener {
 	private ExecutionHandler executionHandler;
 	
 	private MainFrame mainframe;
 	private MiscController miscController;
 	private SettingsController settingsController;
 	private EditorController editorController;
 	private TreeViewController treeController;
 	
 	public MainController() {
 		MessageSystem messagesystem = new MessageSystem();
 		this.executionHandler = new ExecutionHandler(messagesystem);
 		Editor editor = new Editor();
 		this.mainframe = new MainFrame(this, editor, messagesystem);
 		this.editorController = new EditorController(editor, this.mainframe.getEditor());
 		this.miscController = new MiscController(null);
 		this.settingsController = new SettingsController(Settings.getInstance());
 		//TODO bergabe vom ExecutionHandler
 		this.treeController = new TreeViewController(this.mainframe.getBreakpointView(),
 				this.mainframe.getVarView());		
 		
 		//Statement breakpoints setzen
 		this.editorController.getEditorView().getLineNumbers().addMouseListener(this);
 		
 		
 		//Has to be the last call in the constructor
 		initMainFrame();
 	}
 	
 	private void initMainFrame() {
 		/* Very important to call this in a separated method because SWT uses an infinite 
 		 * loop for its window management and we need the instance of MainFrame.
 		 */
 		this.mainframe.openWindow();
 	}
 	
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		//frame events
 		if(e.getSource() == this.mainframe.getMenuBar().getMenuBarItemExit()) {
 			System.exit(0);
 		} else if(e.getSource() == mainframe.getMenuBar().getMenuBarItemLoad()) {
 			new FileFrame(this.mainframe.getShell(), SWT.OPEN);
 		} else if(e.getSource() == mainframe.getMenuBar().getMenuBarItemSave()) {
 			new FileFrame(this.mainframe.getShell(), SWT.SAVE);
 		} else if(e.getSource() == mainframe.getMenuBar().getMenuBarItemSettings()) {
 			new SettingsFrame(this.mainframe.getShell(), this.settingsController);
 		} else if(e.getSource() == mainframe.getMenuBar().getMenuBarItemEvaluation()) {
 			new EvaluationFrame(this.mainframe.getShell());
 		} else if(e.getSource() == mainframe.getMenuBar().getMenuBarItemRandomTest()) {
 			new RandomTestFrame(this.mainframe.getShell(), miscController);
 		} else if(e.getSource() == mainframe.getMenuBar().getMenurBarItemAbout()) {
 			new AboutFrame(this.mainframe.getShell());
 		} else if(e.getSource() == mainframe.getMenuBar().getMenuBarItemHelp()) {
 			new HelpFrame(this.mainframe.getShell());
 		} 
 		//button events
 		else if(e.getSource() == mainframe.getRunButton()) {
 			//Images
 			Image image = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/run2.png"));
 			Image image2 = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/pause1.png"));
 			this.mainframe.switchIcon(image, image2);
 			this.mainframe.getPauseButton().setEnabled(true);
 			this.mainframe.getStopButton().setEnabled(true);
 			//Functions
 			assert editorController != null;
 			this.executionHandler.parse(this.editorController.getEditor().getSource());
 			this.treeController.addExecution(this.executionHandler.getProgramExecution());
 		} else if(e.getSource() == mainframe.getStepButton()) {
 			//Images
 			Image image = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/run1.png"));
 			Image image2 = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/pause2.png"));
 			this.mainframe.switchIcon(image, image2);
 			this.mainframe.getPauseButton().setEnabled(false);
 			this.mainframe.getStopButton().setEnabled(true);
 			//Functions
 			this.treeController.addExecution(this.executionHandler.getProgramExecution());
 			this.treeController.updateVarView();   
 		} else if(e.getSource() == mainframe.getPauseButton()) {
 			Image image = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/run1.png"));
 			Image image2 = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/pause2.png"));
 			this.mainframe.switchIcon(image, image2);
 			//Functions
 			this.treeController.updateVarView();      
 		} else if(e.getSource() == mainframe.getStopButton()) {
 			//Images
 			Image image = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/run1.png"));
 			Image image2 = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/pause1.png"));
 			this.mainframe.switchIcon(image, image2);
 			this.mainframe.getStopButton().setEnabled(false);
 			this.mainframe.getPauseButton().setEnabled(false);
 			//Functions
 			this.treeController.updateVarView();    
 			this.treeController.removeExecution();
 		} else if(e.getSource() == mainframe.getValidateButton()) {
 			Image image = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/run1.png"));
 			Image image2 = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/pause1.png"));
 			this.mainframe.switchIcon(image, image2);
 		}
 	}
 	
 	@Override
 	public void mouseDoubleClick(MouseEvent e) {
 		if (e.getSource() == this.editorController.getEditorView().getLineNumbers()) {
 			int offset = this.editorController.getEditorView().getLineNumbers().getCaretOffset();
 			int lineCount = this.editorController.getEditorView().getTextField().getLineCount();
 			if (offset > lineCount - 1 || this.editorController.getEditorView().getTextField().getLine(offset) == null 
					|| this.editorController.getEditorView().getTextField().getLine(offset).length() == 0) {
 				return;
 			} 
 			this.treeController.addStatementBreakpoint(offset + 1);
 		}		
 	}
 	
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 		// TODO Auto-generated method stub
 	}
 	@Override
 	public void mouseDown(MouseEvent arg0) {
 		// TODO Auto-generated method stub		
 	}
 	@Override
 	public void mouseUp(MouseEvent arg0) {
 		// TODO Auto-generated method stub		
 	}
 }
