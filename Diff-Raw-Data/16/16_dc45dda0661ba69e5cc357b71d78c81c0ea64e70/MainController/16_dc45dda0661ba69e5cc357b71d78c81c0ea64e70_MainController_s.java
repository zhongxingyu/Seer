 package gui.controller;
 
 import misc.Editor;
 import misc.ExecutionHandler;
 import misc.Help;
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
 import gui.ParameterFrame;
 import gui.SettingsFrame;
 import gui.RandomTestFrame;
 import gui.controller.EditorController.BreakpointViewController;
 
 public class MainController implements SelectionListener {
 	private ExecutionHandler executionHandler;
 	
 	private MainFrame mainframe;
 	private SettingsController settingsController;
 	private HelpController helpController;
 	private ParameterController parameterContoller;
 	private EditorController editorController;
     private BreakpointViewController breakpointController;
     private VariableViewController varController;
 	
 	public MainController() {
 		MessageSystem messagesystem = new MessageSystem();
 		this.executionHandler = new ExecutionHandler(messagesystem);
 		Editor editor = new Editor();
 		this.mainframe = new MainFrame(this, editor, messagesystem);
 		this.editorController = new EditorController(editor, this.mainframe.getEditor());
 		this.settingsController = new SettingsController(Settings.getInstance());
 		this.helpController = new HelpController(Help.getInstance(), this.mainframe.getHelpBox());
 		this.parameterContoller = new ParameterController(this.executionHandler);
 		this.varController = new VariableViewController(this.mainframe.getVarView(), this.executionHandler);
         this.breakpointController = this.editorController.new BreakpointViewController(this.mainframe.getBreakpointView(),
         		this.executionHandler);
 		
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
 			new RandomTestFrame(this.mainframe.getShell());
 		} else if(e.getSource() == mainframe.getMenuBar().getMenurBarItemAbout()) {
 			new AboutFrame(this.mainframe.getShell());
 		} else if(e.getSource() == mainframe.getMenuBar().getMenuBarItemHelp()) {
 			new HelpFrame(this.mainframe.getShell(), this.helpController);
 		} 
 		
 		//button events
 		else if(e.getSource() == mainframe.getRunButton()) {
 			//Functions
             assert editorController != null;
             int status = 1;
             if (this.executionHandler.getProgramExecution() == null) {
                 status = this.executionHandler.run(this.editorController.getEditor().getSource(), 
                 		this.editorController.getEditor().getStatementBreakpoints(),
                 		this.editorController.getEditor().getGlobalBreakpoints());
                 this.varController.getVarView().getVarTree().removeAll();
                 
                 if (status == 1) {
                 	ParameterFrame frame = new ParameterFrame(this.mainframe.getShell());
                 	this.parameterContoller.addView(frame);
                 }
             }
             if (status == 1) {
             	//(De-)activations
             	this.mainframe.getPauseButton().setEnabled(true);
     			this.mainframe.getStepButton().setEnabled(false);
     			this.mainframe.getStopButton().setEnabled(true);
     			this.mainframe.getCheckSyntaxButton().setEnabled(false);
     			this.varController.activateView();
     			this.varController.getVarView().getVarTree().removeAll();
     			this.editorController.deactivateView();
     			this.breakpointController.deactivateView();
     			
     			//Images
     			Image image = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/run2.png"));
     			Image image2 = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/pause1.png"));
     			this.mainframe.switchIcon(image, image2);
             } 
         } else if(e.getSource() == mainframe.getStepButton()) {
             //Functions
             assert editorController != null;
             int status = 1;
             if (this.executionHandler.getProgramExecution() == null) {
                 status = this.executionHandler.singleStep(this.editorController.getEditor().getSource(), 
                 		this.editorController.getEditor().getStatementBreakpoints(),
                 		this.editorController.getEditor().getGlobalBreakpoints());
                 this.varController.getVarView().getVarTree().removeAll();
             
                 if (status == 1) {
                 	ParameterFrame frame = new ParameterFrame(this.mainframe.getShell());
                 	this.parameterContoller.addView(frame);
                 	this.varController.updateVarView();
                 }           
             } else {
            	 this.varController.updateVarView();
             }                   
             if (status == 1) {
             	//(De-)activations
             	this.mainframe.getPauseButton().setEnabled(false);
             	this.mainframe.getCheckSyntaxButton().setEnabled(false);
                 this.mainframe.getStopButton().setEnabled(true);
                 this.varController.activateView();
                 this.editorController.deactivateView();
                 this.breakpointController.deactivateView();
                 
               //Images
                 Image image = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/run1.png"));
                 Image image2 = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/pause2.png"));
                 this.mainframe.switchIcon(image, image2);
             }
         } else if(e.getSource() == mainframe.getPauseButton()) {            
             //Functions
             this.varController.updateVarView();
             
         	//Images
             Image image = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/run1.png"));
             Image image2 = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/pause2.png"));
             this.mainframe.switchIcon(image, image2);
             
             //(De-)activations
             this.mainframe.getStepButton().setEnabled(true);
             this.mainframe.getPauseButton().setEnabled(false);
             this.editorController.deactivateView();
             this.breakpointController.deactivateView();
         } else if(e.getSource() == mainframe.getStopButton()) {
             //Functions
             this.varController.updateVarView();
             this.executionHandler.destroyProgramExecution();
             
             //Images
             Image image = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/run1.png"));
             Image image2 = new Image(this.mainframe.getDisplay(), MainFrame.class.getResourceAsStream("image/pause1.png"));
             this.mainframe.switchIcon(image, image2);
             
             //(De-)activations
             this.mainframe.getStopButton().setEnabled(false);
             this.mainframe.getPauseButton().setEnabled(false);
             this.mainframe.getStepButton().setEnabled(true);
             this.mainframe.getCheckSyntaxButton().setEnabled(true);
             this.varController.deactivateView();
             this.editorController.activateView();
             this.breakpointController.activateView();
         } else if(e.getSource() == mainframe.getCheckSyntaxButton()) {
         	//Functions
             assert editorController != null;
             this.executionHandler.parse(this.editorController.getEditor().getSource());
         } else if(e.getSource() == mainframe.getValidateButton()) {
         }
     }
 
     @Override
     public void widgetDefaultSelected(SelectionEvent e) {
         // TODO Auto-generated method stub
     }
 }
