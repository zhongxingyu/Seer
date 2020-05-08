 package de.ptb.epics.eve.editor.views.scanview;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.ExpandBar;
 import org.eclipse.swt.widgets.ExpandItem;
 import org.eclipse.swt.widgets.FileDialog;
 
 import de.ptb.epics.eve.data.PluginTypes;
 import de.ptb.epics.eve.data.measuringstation.PlugIn;
 import de.ptb.epics.eve.data.measuringstation.PluginParameter;
 import de.ptb.epics.eve.data.scandescription.Chain;
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.data.scandescription.errors.ChainError;
 import de.ptb.epics.eve.data.scandescription.errors.ChainErrorTypes;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ControlEventTypes;
 import de.ptb.epics.eve.editor.Activator;
 import de.ptb.epics.eve.editor.dialogs.PluginControllerDialog;
 import de.ptb.epics.eve.editor.graphical.editparts.ChainEditPart;
 import de.ptb.epics.eve.editor.graphical.editparts.ScanDescriptionEditPart;
 import de.ptb.epics.eve.editor.graphical.editparts.ScanModuleEditPart;
 import de.ptb.epics.eve.editor.views.EventComposite;
 
 /**
  * <code>ScanView</code> is the graphical representation of a 
  * {@link de.ptb.epics.eve.data.scandescription.Chain}. It shows the elements 
  * of its underlying model and allows editing them.
  * <p>
  * To define which chain should be presented use {@link #setCurrentChain(Chain)}. 
  * 
  * @author ?
  * @author Marcus Michalsky
  * @author Hartmut Scherr
  */
 public class ScanView extends ViewPart implements ISelectionListener {
 
 	/**
 	 * the unique identifier of the view
 	 */
 	public static final String ID = "de.ptb.epics.eve.editor.views.ScanView";
 
 	// logging
 	private static Logger logger = Logger.getLogger(ScanView.class.getName());
 	
 	// *******************************************************************
 	// ********************** underlying model ***************************
 	// *******************************************************************
 	
 	// the chain that should be presented
 	private Chain currentChain;
 	
 	// *******************************************************************
 	// ******************* end of: underlying model **********************
 	// *******************************************************************
 	
 	// the utmost composite (which contains all elements)
 	private Composite top = null;
 	
 	// the expand bar
 	private ExpandBar bar = null;
 	
 	// (1st) Save Options Expand Item & contained composite
 	private ExpandItem saveOptionsExpandItem;
 	private Composite saveOptionsComposite;
 	
 	// (2nd) Comment Expand Item & contained composite
 	private ExpandItem commentExpandItem;
 	private Composite commentComposite;
 	
 	// (3rd) Events Expand Item & contained Composite
 	private ExpandItem eventsExpandItem;
 	private Composite eventsComposite;
 	
 	// ******** Save Options Widgets *************
 	private Label fileFormatLabel;
 	
 	private Combo fileFormatCombo;
 	private ControlDecoration fileFormatComboControlDecoration;
 	private FileFormatComboSelectionListener fileFormatComboSelectionListener;
 	
 	private Button fileFormatOptionsButton;
 	private FileFormatOptionsButtonSelectionListener 
 			fileFormatOptionsButtonSelectionListener;
 	
 	private Label filenameLabel;
 	
 	private Text filenameInput;
 	private ControlDecoration fileNameInputControlDecoration;
 	private FileNameInputModifiedListener fileNameInputModifiedListener;
 	
 	private Button filenameBrowseButton;
 	
 	private Button saveScanDescriptionCheckBox;
 	private SaveScanDescriptionCheckBoxSelectionListener 
 			saveScanDescriptionCheckBoxSelectionListener;
 	
 	private Button confirmSaveCheckBox;
 	private ConfirmSaveCheckBoxSelectionListener 
 			confirmSaveCheckBoxSelectionListener;
 	
 	private Button autoIncrementCheckBox;
 	private AutoIncrementCheckBoxSelectionListener 
 			autoIncrementCheckBoxSelectionListener;
 	
 	private Label repeatCountLabel;
 	private Text repeatCountText;
 	private RepeatCountTextModifiedListener repeatCountTextModifiedListener;
 	// ***** end of: Widgets of Save Options ********
 	
 	
 	// ***** Widgets of Comment *******	
 	private Text commentInput;
 	private CommentInputModifiedListener commentInputModifiedListener;
 	// *** end of: Widgets of Comment *****
 	
 	
 	// ***** Widgets of Events *******	
 	private CTabFolder eventsTabFolder;
 
 	private CTabItem pauseTabItem;
 	private EventComposite pauseEventComposite;
 	
 	private CTabItem redoTabItem;
 	private EventComposite redoEventComposite;
 	
 	private CTabItem breakTabItem;
 	private EventComposite breakEventComposite;
 	
 	private CTabItem stopTabItem;
 	private EventComposite stopEventComposite;
 	// ***** end of: Widgets of Events *******
 	
 	private Image warnImage;
 	private Image errorImage;
 	private Image eventErrorImage;
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createPartControl(final Composite parent) {
 		logger.debug("createPartControl");
 		
 		parent.setLayout(new FillLayout());
 		
 		// if no measuring station is loaded -> show error and do nothing
 		if(Activator.getDefault().getMeasuringStation() == null) {
 			final Label errorLabel = new Label(parent, SWT.NONE);
 			errorLabel.setText("No Measuring Station has been loaded. " +
 								"Please check Preferences!");
 			return;
 		}
 		
 		this.warnImage = FieldDecorationRegistry.getDefault().getFieldDecoration(
 				FieldDecorationRegistry.DEC_WARNING).getImage();
 		this.errorImage = FieldDecorationRegistry.getDefault().getFieldDecoration(
 				FieldDecorationRegistry.DEC_ERROR).getImage();
 		this.eventErrorImage = PlatformUI.getWorkbench().
 				getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
 		
 		// top composite
 		this.top = new Composite(parent, SWT.NONE);
 		this.top.setLayout(new FillLayout());
 		
 		// expand bar for Save Options, Comment & Events
 		this.bar = new ExpandBar(this.top, SWT.V_SCROLL);
 		
 		// **************************************
 		// ************ Save Expander ***********
 		// **************************************
 		
 		// save composite gets a 3 column grid
 		this.saveOptionsComposite = new Composite(this.bar, SWT.NONE);
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		gridLayout.horizontalSpacing = 12;
 		this.saveOptionsComposite.setLayout(gridLayout);
 		
 		// GUI: "File Format: <Combo Box>"
 		this.fileFormatLabel = new Label(this.saveOptionsComposite, SWT.NONE);
 		this.fileFormatLabel.setText("File format:");
 		
 		this.fileFormatCombo = new Combo(this.saveOptionsComposite, SWT.READ_ONLY);
 		GridData gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.fileFormatCombo.setLayoutData(gridData);
 		// insert all available SAVE plug ins in the combo box
 		List<String> pluginNames = new ArrayList<String>();
 		PlugIn[] plugins = Activator.getDefault().getMeasuringStation().
 									 getPlugins().toArray(new PlugIn[0]);
 		for(int i = 0; i < plugins.length; ++i) {
 			if(plugins[i].getType() == PluginTypes.SAVE) {
 				pluginNames.add(plugins[i].getName());
 			}
 		}
 		this.fileFormatCombo.setItems(pluginNames.toArray(new String[0]));
 		this.fileFormatComboControlDecoration = new ControlDecoration(
 				fileFormatCombo, SWT.RIGHT);
 		this.fileFormatComboControlDecoration.setImage(errorImage);
 		this.fileFormatComboControlDecoration.setDescriptionText(
 				"The File Format is mandatory!");
 		this.fileFormatComboControlDecoration.hide();
 		fileFormatComboSelectionListener = new FileFormatComboSelectionListener();
 		this.fileFormatCombo.addSelectionListener(fileFormatComboSelectionListener);
 		
 		// Save Plug in Options button
 		this.fileFormatOptionsButton = 
 				new Button(this.saveOptionsComposite, SWT.NONE);
 		this.fileFormatOptionsButton.setText("Options");
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.CENTER;
 		this.fileFormatOptionsButton.setLayoutData(gridData);
 		fileFormatOptionsButtonSelectionListener = 
 				new FileFormatOptionsButtonSelectionListener();
 		this.fileFormatOptionsButton.addSelectionListener(
 				fileFormatOptionsButtonSelectionListener);
 		// end of: GUI: "File Format: <Combo Box>"
 		
 		// GUI: "Filename: <Textfield>"
 		this.filenameLabel = new Label(this.saveOptionsComposite, SWT.NONE);
 		this.filenameLabel.setText("Filename:");
 		this.filenameInput = new Text(this.saveOptionsComposite, SWT.BORDER);
 		String tooltip = "The file name where the data should be saved.\n " +
 						"The following macros can be used:\n" + 
 						"${WEEK} : calendar week\n" + 
 						"${DATE} : date as yyyyMMdd (e.g., 20111231)\n" + 
 						"${DATE-} : date as yyyy-MM-dd (e.g., 2011-12-31)\n" + 
 						"${TIME} : time as HHmmss\n" +
 						"${TIME-} : time as HH-mm-ss\n" +
 						"${PV:<pvname>} : replace with value of pvname";
 		this.filenameInput.setToolTipText(tooltip);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.verticalAlignment = GridData.CENTER;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.filenameInput.setLayoutData(gridData);
 		this.fileNameInputControlDecoration = new ControlDecoration(
 				filenameInput, SWT.RIGHT);
 		this.fileNameInputControlDecoration.setImage(errorImage);
 		this.fileNameInputControlDecoration.hide();
 		fileNameInputModifiedListener = new FileNameInputModifiedListener();
 		this.filenameInput.addModifyListener(fileNameInputModifiedListener);
 		
 		// Browse Button
 		this.filenameBrowseButton = new Button(this.saveOptionsComposite, SWT.NONE);
 		this.filenameBrowseButton.setText("Browse...");
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.END;
 		gridData.verticalAlignment = GridData.CENTER;
 		this.filenameBrowseButton.setLayoutData(gridData);
 		this.filenameBrowseButton.addMouseListener(new SearchButtonMouseListener());
 		
 		// Save Scan Description check box
 		this.saveScanDescriptionCheckBox = 
 				new Button(this.saveOptionsComposite, SWT.CHECK);
 		this.saveScanDescriptionCheckBox.setText("Save Scan-Description");
 		this.saveScanDescriptionCheckBox.setToolTipText(
 				"Check to save the scan description into the datafile.");
 		gridData = new GridData();
 		gridData.horizontalSpan = 4;
 		this.saveScanDescriptionCheckBox.setLayoutData(gridData);
 		saveScanDescriptionCheckBoxSelectionListener = 
 				new SaveScanDescriptionCheckBoxSelectionListener();
 		this.saveScanDescriptionCheckBox.addSelectionListener(
 				saveScanDescriptionCheckBoxSelectionListener);
 		
 		// Confirm Save check box
 		this.confirmSaveCheckBox = new Button(this.saveOptionsComposite, SWT.CHECK);
 		this.confirmSaveCheckBox.setText("Confirm Save");
 		this.confirmSaveCheckBox.setToolTipText(
 				"Check if saving the datafile should be confirmed");
 		gridData = new GridData();
 		gridData.horizontalSpan = 4;
 		this.confirmSaveCheckBox.setLayoutData(gridData);
 		confirmSaveCheckBoxSelectionListener = 
 				new ConfirmSaveCheckBoxSelectionListener();
 		this.confirmSaveCheckBox.addSelectionListener(
 				confirmSaveCheckBoxSelectionListener);
 		
 		// Add auto incrementing Number to Filename check box
 		this.autoIncrementCheckBox = new Button(this.saveOptionsComposite, SWT.CHECK);
 		this.autoIncrementCheckBox.setText(
 				"Add Autoincrementing Number to Filename");
 		gridData = new GridData();
 		gridData.horizontalSpan = 4;
 		this.autoIncrementCheckBox.setLayoutData(gridData);
 		autoIncrementCheckBoxSelectionListener = 
 				new AutoIncrementCheckBoxSelectionListener();
 		this.autoIncrementCheckBox.addSelectionListener(
 				autoIncrementCheckBoxSelectionListener);
 		
 		// repeat count text field
 		this.repeatCountLabel = new Label(this.saveOptionsComposite, SWT.NONE);
 		this.repeatCountLabel.setText("repeat count:");
 		this.repeatCountText = new Text(this.saveOptionsComposite, SWT.BORDER);
 		this.repeatCountText.setToolTipText(
 				"the number of times the scan will be repeated");
 		gridData = new GridData();
 		gridData.horizontalSpan = 3;
 		this.repeatCountText.setLayoutData(gridData);
 		repeatCountTextModifiedListener = new RepeatCountTextModifiedListener();
 		this.repeatCountText.addModifyListener(repeatCountTextModifiedListener);
 		
 		// add expand item to the expander
 		this.saveOptionsExpandItem = new ExpandItem(this.bar, SWT.NONE, 0);
 		saveOptionsExpandItem.setText("Save Options");
 		saveOptionsExpandItem.setHeight(
 				this.saveOptionsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
 		saveOptionsExpandItem.setControl(this.saveOptionsComposite);
 		
 		this.saveOptionsExpandItem.setExpanded(true);
 		
 		// **************************************
 		// ******** end of Save Expander ********
 		// **************************************
 		
 		// **************************************
 		// ********** Comment Expander **********
 		// **************************************
 		
 		this.commentComposite = new Composite(this.bar, SWT.NONE);
 		FillLayout fillLayout = new FillLayout();
 		fillLayout.marginWidth = 5;
 		this.commentComposite.setLayout(fillLayout);
 		
 		// Comment Input
 		this.commentInput = new Text(this.commentComposite, 
 				SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
 		commentInputModifiedListener = new CommentInputModifiedListener();
 		this.commentInput.addModifyListener(commentInputModifiedListener);
 		
 		// add comment expander to the expand bar
 		this.commentExpandItem = new ExpandItem (this.bar, SWT.NONE, 0);
 		commentExpandItem.setText("Comment");
 		commentExpandItem.setHeight(100);
 		commentExpandItem.setControl(this.commentComposite);
 		this.commentExpandItem.setExpanded(true);
 		
 		// **************************************
 		// ****** end of Comment Expander *******
 		// **************************************
 		
 		// **************************************
 		// ********** Events Expander ***********
 		// **************************************
 		
 		this.eventsComposite = new Composite(this.bar, SWT.NONE);
 		this.eventsComposite.setLayout(new FillLayout());
 		
 		// events tab folder contains the tabs pause, redo, break & stop
 		eventsTabFolder = new CTabFolder(this.eventsComposite, SWT.FLAT);
 		eventsTabFolder.addSelectionListener(
 				new EventsTabFolderSelectionListener());
 								
 		pauseEventComposite = new EventComposite(eventsTabFolder, SWT.NONE, 
 				ControlEventTypes.PAUSE_EVENT);
 		redoEventComposite = new EventComposite(eventsTabFolder, SWT.NONE, 
 				ControlEventTypes.CONTROL_EVENT);
 		breakEventComposite = new EventComposite(eventsTabFolder, SWT.NONE, 
 				ControlEventTypes.CONTROL_EVENT);
 		stopEventComposite = new EventComposite(eventsTabFolder, SWT.NONE, 
 				ControlEventTypes.CONTROL_EVENT);
 		
 		this.pauseTabItem = new CTabItem(eventsTabFolder, SWT.FLAT);
 		this.pauseTabItem.setText("Pause");
 		this.pauseTabItem.setControl(pauseEventComposite);
 		this.pauseTabItem.setToolTipText("Event to pause an resume this scan");
 		this.redoTabItem = new CTabItem(eventsTabFolder, SWT.FLAT);
 		this.redoTabItem.setText("Redo");
 		this.redoTabItem.setControl(redoEventComposite);
 		this.redoTabItem.setToolTipText(
 				"Repeat the current scan point, if redo event occurs");
 		this.breakTabItem = new CTabItem(eventsTabFolder, SWT.FLAT);
 		this.breakTabItem.setText("Break");
 		this.breakTabItem.setControl(breakEventComposite);
 		this.breakTabItem.setToolTipText(
 				"Finish the current scan module and continue with next");
 		this.stopTabItem = new CTabItem(eventsTabFolder, SWT.FLAT);
 		this.stopTabItem.setText("Stop");
 		this.stopTabItem.setControl(stopEventComposite);
 		this.stopTabItem.setToolTipText("Stop this scan");
 		
 		// add Events expander to the expand bar
 		this.eventsExpandItem = new ExpandItem(this.bar, SWT.NONE, 0);
 		this.eventsExpandItem.setText("Events");
 		this.eventsExpandItem.setHeight(
 				this.eventsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
 		this.eventsExpandItem.setControl(this.eventsComposite);
 		
 		// **************************************
 		// ******* end of Events Expander *******
 		// **************************************
 		
 		top.setVisible(false);
 		
 		// listen to selection changes (if a chain (or one of its scan modules) 
 		// is selected, its attributes are made available for editing)
 		getSite().getWorkbenchWindow().getSelectionService().
 				addSelectionListener(this);
 	}
 	
 	// ************************************************************************
 	// *********************** End of createPartControl ***********************
 	// ************************************************************************
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setFocus() {
 		logger.debug("Focus gained -> forward to top composite");
 		this.top.setFocus();
 	}
 	
 	/**
 	 * Sets the current {@link de.ptb.epics.eve.data.scandescription.Chain} 
 	 * (the underlying model whose contents is presented by this view).
 	 * 
 	 * @param currentChain the 
 	 * 		  {@link de.ptb.epics.eve.data.scandescription.Chain} that should 
 	 * 		  be set current. Use <code>null</code> to present an empty view.
 	 */
 	public void setCurrentChain(final Chain currentChain) {
 		// set the new chain as current chain
 		this.currentChain = currentChain;
 		
 		removeListeners();
 		
 		if(this.currentChain != null) {
 			this.top.setVisible(true);
 			this.setPartName("Chain: " + this.currentChain.getId());
 			
 			if(this.currentChain.getSavePluginController().getPlugin() != null) {
 				this.fileFormatCombo.setText(this.currentChain.
 						getSavePluginController().getPlugin().getName());
 			}
 			
 			if(this.currentChain.getSaveFilename() != null) {
 				this.filenameInput.setText(this.currentChain.getSaveFilename());
 				this.filenameInput.setSelection(
 						this.filenameInput.getText().length());
 			}
 			
 			this.saveScanDescriptionCheckBox.setSelection(
 					this.currentChain.isSaveScanDescription());
 			this.confirmSaveCheckBox.setSelection(
 					this.currentChain.isConfirmSave());
 			this.autoIncrementCheckBox.setSelection(
 					this.currentChain.isAutoNumber());
 			
 			this.repeatCountText.setText(Integer.toString(
 					this.currentChain.getScanDescription().getRepeatCount()));
 			
 			this.commentInput.setText(this.currentChain.getComment());
 			this.commentInput.setSelection(
 					this.currentChain.getComment().length());
 			
 			if (this.pauseEventComposite.getControlEventManager() != 
 				this.currentChain.getPauseControlEventManager()) {
 					this.pauseEventComposite.setControlEventManager(
 						this.currentChain.getPauseControlEventManager());
 			}
 			if (this.redoEventComposite.getControlEventManager() != 
 				this.currentChain.getRedoControlEventManager()) {
 					this.redoEventComposite.setControlEventManager(
 						this.currentChain.getRedoControlEventManager());
 			}
 			if (this.breakEventComposite.getControlEventManager() != 
 				this.currentChain.getRedoControlEventManager()) {
 					this.breakEventComposite.setControlEventManager(
 						this.currentChain.getBreakControlEventManager());
 			}
 			if (this.stopEventComposite.getControlEventManager() != 
 				this.currentChain.getStopControlEventManager()) {
 					this.stopEventComposite.setControlEventManager(
 						this.currentChain.getStopControlEventManager());
 			}
 			checkForErrors();
 		} else { // currentChain == null
 			this.fileFormatCombo.deselectAll();
 			this.filenameInput.setText("");
 			this.saveScanDescriptionCheckBox.setSelection(false);
 			this.confirmSaveCheckBox.setSelection(false);
 			this.autoIncrementCheckBox.setSelection(false);
 			this.repeatCountText.setText("");
 			this.commentInput.setText("");
 			
 			this.pauseEventComposite.setControlEventManager(null);
 			this.redoEventComposite.setControlEventManager(null);
 			this.breakEventComposite.setControlEventManager(null);
 			this.stopEventComposite.setControlEventManager(null);
 			
 			this.setPartName("No Chain selected");
 			this.top.setVisible(false);
 		}
 		addListeners();
 	}
 	
 	/*
 	 * called by setCurrentChain() to check for errors in user input and show 
 	 * error decorators.
 	 */
 	private void checkForErrors() {
 		// reset all
 		
 		this.fileFormatComboControlDecoration.hide();
 		this.fileNameInputControlDecoration.hide();
 		
 		this.pauseTabItem.setImage(null);
 		this.breakTabItem.setImage(null);
 		this.redoTabItem.setImage(null);
 		this.stopTabItem.setImage(null);
 		
 		
 		if (this.currentChain.getSavePluginController().
 							 getModelErrors().size() > 0) {
 			this.fileFormatComboControlDecoration.show();
 			this.fileFormatCombo.deselectAll();
 		}
 		
		File file = new File(this.currentChain.getSaveFilename());
		if(file.isFile() && file.exists()) {
			this.fileNameInputControlDecoration.setImage(warnImage);
			this.fileNameInputControlDecoration.setDescriptionText(
					"File already exists!");
			this.fileNameInputControlDecoration.show();
		}
		file = null;
		
 		for(IModelError error : this.currentChain.getModelErrors()) {
 			if (error instanceof ChainError) {
 				final ChainError chainError = (ChainError)error;
				this.fileNameInputControlDecoration.setImage(errorImage);
 				this.fileNameInputControlDecoration.show();
 				if(chainError.getErrorType() == ChainErrorTypes.FILENAME_EMPTY) {
 					this.fileNameInputControlDecoration.setDescriptionText(
 							"Filename must not be empty!");
 				} else if (chainError.getErrorType() == 
 							ChainErrorTypes.FILENAME_ILLEGAL_CHARACTER) {
 					this.fileNameInputControlDecoration.setDescriptionText(
 							"Filename contains illegal characters!");
 				}
 			}
 		}
 		
 		if(this.currentChain.getPauseControlEventManager().
 							 getModelErrors().size() > 0) {
 			this.pauseTabItem.setImage(eventErrorImage);
 		}
 		if(this.currentChain.getBreakControlEventManager().
 				getModelErrors().size() > 0) {
 			this.breakTabItem.setImage(eventErrorImage);
 		}
 		if(this.currentChain.getRedoControlEventManager().
 							 getModelErrors().size() > 0) {
 			this.redoTabItem.setImage(eventErrorImage);
 		}
 		if(this.currentChain.getStopControlEventManager().
 							 getModelErrors().size() > 0) {
 			this.stopTabItem.setImage(eventErrorImage);
 		}
 	}
 	
 	/*
 	 * used by setCurrentChain() to re-enable listeners
 	 */
 	private void addListeners() {
 		this.fileFormatCombo.addSelectionListener(fileFormatComboSelectionListener);
 		this.filenameInput.addModifyListener(fileNameInputModifiedListener);
 		this.repeatCountText.addModifyListener(repeatCountTextModifiedListener);
 		this.commentInput.addModifyListener(commentInputModifiedListener);
 		
 		this.saveScanDescriptionCheckBox.addSelectionListener(
 				saveScanDescriptionCheckBoxSelectionListener);
 		this.confirmSaveCheckBox.addSelectionListener(
 				confirmSaveCheckBoxSelectionListener);
 		this.autoIncrementCheckBox.addSelectionListener(
 				autoIncrementCheckBoxSelectionListener);
 	}
 	
 	/*
 	 * used by setCurrentChain() to temporarily disable listeners
 	 */
 	private void removeListeners() {
 		this.fileFormatCombo.removeSelectionListener(
 				fileFormatComboSelectionListener);
 		this.filenameInput.removeModifyListener(fileNameInputModifiedListener);
 		this.repeatCountText.removeModifyListener(
 				repeatCountTextModifiedListener);
 		this.commentInput.removeModifyListener(commentInputModifiedListener);
 		
 		this.saveScanDescriptionCheckBox.removeSelectionListener(
 				saveScanDescriptionCheckBoxSelectionListener);
 		this.confirmSaveCheckBox.removeSelectionListener(
 				confirmSaveCheckBoxSelectionListener);
 		this.autoIncrementCheckBox.removeSelectionListener(
 				autoIncrementCheckBoxSelectionListener);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		logger.debug("selection changed");
 		
 		if(selection instanceof IStructuredSelection) {
 			if(((IStructuredSelection) selection).size() == 0) {
 				return;
 			}
 		}
 
 	// since at any given time this view can only display options of 
 	// one device we take the first element of the selection
 	Object o = ((IStructuredSelection) selection).toList().get(0);
 		if (o instanceof ChainEditPart) {
 			// set new Chain
 			if(logger.isDebugEnabled()) {
 				logger.debug("Chain: " + ((Chain)((ChainEditPart)o).
 						getModel()).getId() + " selected."); 
 			}
 			setCurrentChain((Chain)((ChainEditPart)o).getModel());
 
 		} else if (o instanceof ScanModuleEditPart) {
 			// a scan module belongs to a chain -> show chain
 			if(logger.isDebugEnabled()) {
 				logger.debug("ScanModule " + (ScanModule)
 						((ScanModuleEditPart)o).getModel() + " selected.");
 				setCurrentChain(((ScanModule)((ScanModuleEditPart)o).
 						getModel()).getChain());
 			}
 		} else if (o instanceof ScanDescriptionEditPart) {
 				logger.debug("selection is ScanDescriptionEditPart: " + o);
 				setCurrentChain(null);
 		} else {
 			logger.debug("selection other than Chain -> ignore: " + o);
 		}
 	}
 	
 	// ************************************************************************
 	// *************************** Listener ***********************************
 	// ************************************************************************
 	
 	/**
 	 * {@link org.eclipse.swt.events.ModifyListener} of
 	 * <code>savePluginCombo</code>.
 	 */
 	class FileFormatComboSelectionListener implements SelectionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 				currentChain.getSavePluginController().setPlugin(
 						Activator.getDefault().getMeasuringStation().
 								  getPluginByName(fileFormatCombo.getText()));
 				checkForErrors();
 		}
 	}
 
 	/**
 	 * {@link org.eclipse.swt.events.SelectionListener} of 
 	 * <code>savePluginOptionsButton</code>.
 	 */
 	class FileFormatOptionsButtonSelectionListener implements SelectionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			PluginControllerDialog dialog = new PluginControllerDialog( 
 								null, currentChain.getSavePluginController());
 			dialog.setBlockOnOpen(true);
 			dialog.open();
 		}
 	}
 	
 	/**
 	 * {@link org.eclipse.swt.events.ModifyListener} of
 	 * <code>fileNameInput</code>.
 	 */
 	class FileNameInputModifiedListener implements ModifyListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText(ModifyEvent e) {
 			currentChain.setSaveFilename(filenameInput.getText().trim());
 			checkForErrors();
 		}
 	}
 
 	/**
 	 * {@link org.eclipse.swt.events.MouseListener} of 
 	 * <code>searchButton</code>.
 	 */
 	class SearchButtonMouseListener implements MouseListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mouseDoubleClick(MouseEvent e) {
 		}
 		
 		/**
 		 * {@inheritDoc}<br><br>
 		 * Opens a file dialog to choose the destination of the data file.
 		 */
 		@Override
 		public void mouseDown(MouseEvent e) {
 			
 			int lastSeperatorIndex;
 			final String filePath;
 			
 			if (currentChain.getSaveFilename() != null && 
 				!currentChain.getSaveFilename().isEmpty()) 
 			{ // there already is a filename (and path) -> show the path
 				lastSeperatorIndex = currentChain.getSaveFilename().
 											lastIndexOf(File.separatorChar);
 				filePath = currentChain.getSaveFilename().
 									substring(0, lastSeperatorIndex + 1);
 			} else {
 				// no filename -> set path to <rootDir>/daten/ or <rootDir>
 				String rootdir = Activator.getDefault().getRootDirectory();
 				File file = new File(rootdir + "data/");
 				if(file.exists()) {
 					filePath = rootdir + "data/";
 				} else {
 					filePath = rootdir;
 				}
 				file = null;
 			}
 			
 			logger.debug("FileDialog path: " + filePath);
 			
 			FileDialog fileWindow = new FileDialog(getSite().getShell(), SWT.SAVE);
 			fileWindow.setFilterPath(filePath);
 			String name = fileWindow.open();
 			
 			if(name != null) {
 				// try to get suffix parameter of the selected plug in
 				final Iterator<PluginParameter> it = 
 						currentChain.getSavePluginController().getPlugin().
 									 getParameters().iterator();
 				
 				PluginParameter pluginParameter = null;
 				while(it.hasNext()) {
 					pluginParameter = it.next();
 					if(pluginParameter.getName().equals("suffix")) {
 						break;
 					}
 					pluginParameter = null;
 				}
 				
 				if(pluginParameter != null) {
 					// suffix found -> replace
 					String suffix = currentChain.getSavePluginController().
 												 get("suffix").toString();
 					
 					// remove old suffix
 					final int lastPoint = name.lastIndexOf(".");
 					final int lastSep = name.lastIndexOf("/");
 
 					if ((lastPoint > 0) && (lastPoint > lastSep)) { 
 						filenameInput.setText(name.substring(0, lastPoint) + "." + suffix);
 					} else {
 						filenameInput.setText(name + "." + suffix);
 					}
 				}
 				else {
 					filenameInput.setText(name);
 				}
 				filenameInput.setSelection(filenameInput.getText().length());
 			}
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mouseUp(MouseEvent e) {
 		}
 	}
 	
 	/**
 	 * {@link org.eclipse.swt.events.SelectionListener} of 
 	 * <code>saveScanDescriptionCheckBox</code>.
 	 */
 	class SaveScanDescriptionCheckBoxSelectionListener 
 												implements SelectionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			currentChain.setSaveScanDescription(
 					saveScanDescriptionCheckBox.getSelection());
 		}
 	}
 
 	// ******************* Selection Listener *******************
 	
 	/**
 	 * {@link org.eclipse.swt.events.SelectionListener} of 
 	 * <code>manualSaveCheckBox</code>.
 	 */
 	class ConfirmSaveCheckBoxSelectionListener implements SelectionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			currentChain.setConfirmSave(confirmSaveCheckBox.getSelection());
 		}
 	}
 
 	// ******************* Selection Listener *******************
 	
 	/**
 	 * {@link org.eclipse.swt.events.SelectionListener} of 
 	 * <code>autoNumberCheckBox</code>.<br><br>
 	 */
 	class AutoIncrementCheckBoxSelectionListener implements SelectionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			currentChain.setAutoNumber(autoIncrementCheckBox.getSelection());
 		}
 	}
 	
 	/**
 	 * {@link org.eclipse.swt.events.ModifyListener} of
 	 * <code>repeatCountText</code>.
 	 */
 	class RepeatCountTextModifiedListener implements ModifyListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText(ModifyEvent e) {
 			try {
 				currentChain.getScanDescription().setRepeatCount(
 						Integer.parseInt(repeatCountText.getText()));
 			} catch(final NumberFormatException ex) {
 				repeatCountText.setText("");
 			}
 		}
 	}
 	
 	/**
 	 * {@link org.eclipse.swt.events.ModifyListener} of
 	 * <code>commentInput</code>.
 	 */
 	class CommentInputModifiedListener implements ModifyListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText(ModifyEvent e) {
 			currentChain.setComment(commentInput.getText());
 		}
 	}
 	
 	/**
 	 * {@link org.eclipse.swt.events.SelectionListener} of 
 	 * <code>eventsTabFolder</code>.
 	 */
 	class EventsTabFolderSelectionListener implements SelectionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			// update entries in selection list
 			((EventComposite)eventsTabFolder.
 					getSelection().getControl()).setEventChoice();
 		}
 	}
 }
