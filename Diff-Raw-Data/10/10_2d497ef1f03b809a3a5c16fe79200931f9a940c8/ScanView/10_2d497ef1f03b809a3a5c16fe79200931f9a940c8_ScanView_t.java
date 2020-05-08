 package de.ptb.epics.eve.editor.views.scanview;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.ExpandBar;
 import org.eclipse.swt.widgets.ExpandItem;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 
 import de.ptb.epics.eve.data.PluginTypes;
 import de.ptb.epics.eve.data.measuringstation.PlugIn;
 import de.ptb.epics.eve.data.measuringstation.PluginParameter;
 import de.ptb.epics.eve.data.scandescription.Chain;
 import de.ptb.epics.eve.data.scandescription.errors.ChainError;
 import de.ptb.epics.eve.data.scandescription.errors.ChainErrorTypes;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 import de.ptb.epics.eve.editor.Activator;
 import de.ptb.epics.eve.editor.Helper;
 import de.ptb.epics.eve.editor.dialogs.PluginControllerDialog;
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
  */
 public class ScanView extends ViewPart implements IModelUpdateListener {
 
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
 	private Composite saveOptionsComposite = null;
 	
 	// (2nd) Comment Expand Item & contained composite
 	private ExpandItem commentExpandItem;
 	private Composite commentComposite = null;
 	
 	// (3rd) Events Expand Item & contained Composite
 	private ExpandItem eventsExpandItem;
 	private Composite eventsComposite = null;
 	
 	// ******** Widgets of Save Options *************
 	private Label fileFormatLabel = null;
 	private Combo fileFormatCombo = null;
 	private Label fileFormatComboErrorLabel = null;
 	private Button fileFormatOptionsButton = null;
 	
 	private FileFormatComboSelectionListener fileFormatComboSelectionListener;
 	private FileFormatOptionsButtonSelectionListener 
 			fileFormatOptionsButtonSelectionListener;
 	
 	private Label filenameLabel = null;
 	private Text filenameInput = null;
 	private Label filenameErrorLabel = null;
 	private Button filenameBrowseButton = null;
 
 	private FileNameInputModifiedListener fileNameInputModifiedListener;
 	
 	private Button saveScanDescriptionCheckBox = null;
 	private SaveScanDescriptionCheckBoxSelectionListener 
 			saveScanDescriptionCheckBoxSelectionListener;
 	
 	private Button confirmSaveCheckBox = null;
 	private ConfirmSaveCheckBoxSelectionListener 
 			confirmSaveCheckBoxSelectionListener;
 	
 	private Button autoIncrementCheckBox = null;
 	private AutoIncrementCheckBoxSelectionListener 
 			autoIncrementCheckBoxSelectionListener;
 	
 	private Label repeatCountLabel = null;
 	private Text repeatCountText = null;
 	
 	private RepeatCountTextModifiedListener repeatCountTextModifiedListener;
 	// ***** end of: Widgets of Save Options ********
 	
 	
 	// ***** Widgets of Comment *******	
 	private Text commentInput = null;
 
 	private CommentInputModifiedListener commentInputModifiedListener;
 	// *** end of: Widgets of Comment *****
 	
 	
 	// ***** Widgets of Events *******	
 	private CTabFolder eventsTabFolder = null;
 
 	private CTabItem pauseTabItem;
 	private EventComposite pauseEventComposite = null;
 	
 	private CTabItem redoTabItem;
 	private EventComposite redoEventComposite = null;
 	
 	private CTabItem breakTabItem;
 	private EventComposite breakEventComposite = null;
 	
 	private CTabItem stopTabItem;
 	private EventComposite stopEventComposite = null;	
 	// ***** end of: Widgets of Events *******
 	
 	
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
 
 		// the top composite
 		this.top = new Composite(parent, SWT.NONE);
 		this.top.setLayout(new GridLayout());
 		
 		// expand bar for Save Options, Comment & Events
 		this.bar = new ExpandBar(this.top, SWT.V_SCROLL);
 		GridData gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		this.bar.setLayoutData(gridData);
 		
 		// **************************************
 		// ************ Save Expander ***********
 		// **************************************
 
 		// save composite gets a 4 column grid
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 4;
 		this.saveOptionsComposite = new Composite(this.bar, SWT.NONE);
 		this.saveOptionsComposite.setLayout(gridLayout);
 
 		// GUI: "File Format: <Combo Box>"
 		this.fileFormatLabel = new Label(this.saveOptionsComposite, SWT.NONE);
 		this.fileFormatLabel.setText("File format:");
 		this.fileFormatCombo = new Combo(this.saveOptionsComposite, SWT.READ_ONLY);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.CENTER;
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
 		
 		// initialize & register the modify listener to the combo box
 		fileFormatComboSelectionListener = new FileFormatComboSelectionListener();
 		this.fileFormatCombo.addSelectionListener(fileFormatComboSelectionListener);
 		
 		// Save Plug in Error Label
 		this.fileFormatComboErrorLabel = 
 				new Label(this.saveOptionsComposite, SWT.NONE);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.CENTER;
 		gridData.horizontalSpan = 1;
 		this.fileFormatComboErrorLabel.setLayoutData(gridData);
 		
 		// Save Plug in Options button
 		this.fileFormatOptionsButton = 
 				new Button(this.saveOptionsComposite, SWT.NONE);
 		this.fileFormatOptionsButton.setText("Options");
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.CENTER;
 		gridData.horizontalSpan = 1;
 		this.fileFormatOptionsButton.setLayoutData(gridData);
 
 		// initialize & register the selection listener to the button
 		fileFormatOptionsButtonSelectionListener = 
 				new FileFormatOptionsButtonSelectionListener();
 		this.fileFormatOptionsButton.addSelectionListener(
 				fileFormatOptionsButtonSelectionListener);
 		
 		// GUI: "Filename: <Textfield>"
 		this.filenameLabel = new Label(this.saveOptionsComposite, SWT.NONE);
 		this.filenameLabel.setText("Filename:");
 		this.filenameInput = new Text(this.saveOptionsComposite, SWT.BORDER);
		String tooltip = "The file name where the data should be saved.\n " +
						"Use wild cards as followed:\n" + 
						"${WEEK} : calendar week\n" + 
						"${DATE} : date as YYYYMMDD (e.g., 20111231)\n" + 
						"${DATE-} : date as YYYY-MM-DD (e.g., 2011-12-31)\n" + 
						"${TIME} : time as hhmmss\n" +
						"${TIME-} : time as hh-mm-ss";
		this.filenameInput.setToolTipText(tooltip);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.verticalAlignment = GridData.CENTER;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.filenameInput.setLayoutData(gridData);
 		
 		// initialize & register the modify listener to the input field
 		fileNameInputModifiedListener = new FileNameInputModifiedListener();
 		this.filenameInput.addModifyListener(fileNameInputModifiedListener);
 		
 		// File name error label
 		this.filenameErrorLabel = new Label(this.saveOptionsComposite, SWT.NONE);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.CENTER;
 		this.filenameErrorLabel.setLayoutData(gridData);
 		this.filenameErrorLabel.setImage(PlatformUI.getWorkbench().
 				getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK));
 		
 		// Browse Button
 		GridData gridData11 = new GridData();
 		gridData11.horizontalAlignment = GridData.END;
 		gridData11.verticalAlignment = GridData.CENTER;
 		this.filenameBrowseButton = new Button(this.saveOptionsComposite, SWT.NONE);
 		this.filenameBrowseButton.setText("Browse...");
 		this.filenameBrowseButton.setLayoutData(gridData11);
 		this.filenameBrowseButton.setToolTipText("Browse for a file");
 		// (click)Listener for the button
 		this.filenameBrowseButton.addMouseListener(new SearchButtonMouseListener()); 
 				
 		// Save Scan Description check box
 		this.saveScanDescriptionCheckBox = 
 				new Button(this.saveOptionsComposite, SWT.CHECK);
 		this.saveScanDescriptionCheckBox.setText("Save Scan-Description");
 		this.saveScanDescriptionCheckBox.setToolTipText(
 				"Check to save the scan description into the datafile.");
 		gridData = new GridData();
 		gridData.horizontalSpan = 3;
 		this.saveScanDescriptionCheckBox.setLayoutData(gridData);
 		
 		// initialize & register the selection listener to the check box
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
 		gridData.horizontalSpan = 3;
 		this.confirmSaveCheckBox.setLayoutData(gridData);
 
 		// initialize & register the selection listener to the check box
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
 		
 		// initialize & register selection listener to the check box
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
 
 		// initialize & register the modify listener to the text field
 		repeatCountTextModifiedListener = new RepeatCountTextModifiedListener();
 		this.repeatCountText.addModifyListener(repeatCountTextModifiedListener);
 				
 		// add expand item to the expander
 		this.saveOptionsExpandItem = new ExpandItem(this.bar, SWT.NONE, 0);
 		saveOptionsExpandItem.setText("Save Options");
 		saveOptionsExpandItem.setHeight(
 				this.saveOptionsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
 		saveOptionsExpandItem.setControl(this.saveOptionsComposite);
 		
 		this.saveOptionsComposite.addControlListener(
 				new SavingCompositeControlListener());
 		
 		// **************************************
 		// ******** end of Save Expander ********
 		// **************************************
 		
 		// **************************************
 		// ********** Comment Expander **********
 		// **************************************
 		
 		gridLayout = new GridLayout();
 		gridLayout.numColumns = 1;
 		this.commentComposite = new Composite(this.bar, SWT.NONE);
 		this.commentComposite.setLayout(gridLayout);
 		
 		// Comment Input
 		this.commentInput = new Text(this.commentComposite, 
 									 SWT.BORDER | SWT.V_SCROLL);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		this.commentInput.setLayoutData(gridData);
 		
 		// initialize & register modify listener to comment input
 		commentInputModifiedListener = new CommentInputModifiedListener();
 		this.commentInput.addModifyListener(commentInputModifiedListener);
 		
 		// add comment expander to the expand bar
 		this.commentExpandItem = new ExpandItem (this.bar, SWT.NONE, 0);
 		commentExpandItem.setText("Comment");
 		commentExpandItem.setHeight(100);
 		commentExpandItem.setControl(this.commentComposite);
 
 		// **************************************
 		// ****** end of Comment Expander *******
 		// **************************************
 		
 		// **************************************
 		// ********** Events Expander ***********
 		// **************************************
 		
 		gridLayout = new GridLayout();
 		this.eventsComposite = new Composite(this.bar, SWT.NONE);
 		this.eventsComposite.setLayout(gridLayout);
 
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalAlignment = GridData.FILL;
 		this.eventsComposite.setLayoutData(gridData);
 		
 		// events tab folder contains the tabs pause, redo, break & stop
 		eventsTabFolder = new CTabFolder(this.eventsComposite, SWT.FLAT);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		this.eventsTabFolder.setLayoutData(gridData);
 		eventsTabFolder.addSelectionListener(
 				new EventsTabFolderSelectionListener());
 								
 		pauseEventComposite = new EventComposite(eventsTabFolder, SWT.NONE);
 		redoEventComposite = new EventComposite(eventsTabFolder, SWT.NONE);
 		breakEventComposite = new EventComposite(eventsTabFolder, SWT.NONE);
 		stopEventComposite = new EventComposite(eventsTabFolder, SWT.NONE);
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
 		
 		this.eventsComposite.addControlListener(
 				new EventsCompositeControlListener());
 		
 		// **************************************
 		// ******* end of Events Expander *******
 		// **************************************
 		
 		this.bar.addControlListener(new BarControlListener());
 		
 		top.setVisible(false);
 				
 		this.setEnabledForAll(false);
 	}
 	
 	// ************************************************************************
 	// *********************** End of createPartControl ***********************
 	// ************************************************************************
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setFocus() {
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
 		
 		if(currentChain != null)
 			logger.debug("chain set to: " + currentChain.getId());
 		else
 			logger.debug("chain set to: null");
 		
 		// if a current chain is set, stop listening to it
 		if(this.currentChain != null) {
 			this.currentChain.removeModelUpdateListener(this);
 		}
 		// set the new chain as current chain
 		this.currentChain = currentChain;
 		
 		if(this.currentChain != null) {
 			// current chain not null -> listen to updates on it
 			this.currentChain.addModelUpdateListener(this);		
 		} 
 		
 		// update elements (calls the inherited method from the UpdateListener 
 		// with argument null indicating an internal call)
 		updateEvent(null);
 	}	
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void updateEvent(final ModelUpdateEvent modelUpdateEvent) {
 		
 		if(modelUpdateEvent == null) 
 			logger.debug("update event (null)");
 		else 
 			logger.debug("update event (" + 
 				modelUpdateEvent.getSender().getClass().getName() + ")");
 		
 		// temporarily remove listeners to prevent event loops
 		removeListeners();
 		
 		if(this.currentChain != null) {
 			// current chain is not null -> get elements and fill fields
 			
 			this.fileFormatCombo.setText(
 			  (this.currentChain.getSavePluginController().getPlugin() != null)
 			  ? this.currentChain.getSavePluginController().getPlugin().getName()
 			  : "");
 			
 			if(this.currentChain.getSavePluginController().getPlugin() == null) {
 				this.fileFormatCombo.deselectAll();
 			}
 			
 			this.filenameInput.setText(
 					(this.currentChain.getSaveFilename() != null)
 					? this.currentChain.getSaveFilename()
 					: "");
 			
 			this.filenameErrorLabel.setImage(null);
 			this.filenameErrorLabel.setToolTipText("");
 			
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
 			
 			// updating event composites -> set their control event managers
 			
 			if(this.pauseEventComposite.getControlEventManager() != 
 			   this.currentChain.getPauseControlEventManager()) 
 			{
 				this.pauseEventComposite.setControlEventManager(
 						this.currentChain.getPauseControlEventManager());
 			}
 			if(this.redoEventComposite.getControlEventManager() != 
 			   this.currentChain.getRedoControlEventManager()) 
 			{
 				this.redoEventComposite.setControlEventManager(
 						this.currentChain.getRedoControlEventManager());
 			}
 			if(this.breakEventComposite.getControlEventManager() != 
 			   this.currentChain.getRedoControlEventManager()) 
 			{
 				this.breakEventComposite.setControlEventManager(
 						this.currentChain.getBreakControlEventManager());
 			}
 			if(this.stopEventComposite.getControlEventManager() != 
 			   this.currentChain.getStopControlEventManager()) 
 			{
 				this.stopEventComposite.setControlEventManager(
 						this.currentChain.getStopControlEventManager());
 			}		
 			
 			// enable items and set title
 			this.setEnabledForAll(true);
 			this.setPartName("Chain: " + this.currentChain.getId());
 			saveOptionsExpandItem.setExpanded(true);
 			
 			// check if there are errors in the model
 			// and notify the user of them
 			checkForErrors();
 			
 			top.setVisible(true);
 			top.layout();
 			
 		} else {
 			// current chain is null -> clear (no model is represented)
 			//this.fileFormatCombo.setText("");
 			this.fileFormatCombo.deselectAll();
 			this.fileFormatComboErrorLabel.setImage(null);		
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
 			
 			// disable items & reset title
 			this.setEnabledForAll(false);
 			this.setPartName("No Chain selected");
 			saveOptionsExpandItem.setExpanded(false);
 			top.setVisible(false);
 		}	
 		
 		// re-enable listeners
 		addListeners();
 	}
 	
 	/*
 	 * enables/disables all editable widget elements
 	 */
 	private void setEnabledForAll(final boolean enabled) {
 		this.fileFormatCombo.setEnabled(enabled);
 		this.filenameInput.setEnabled(enabled);
 		this.filenameBrowseButton.setEnabled(enabled);
 		this.saveScanDescriptionCheckBox.setEnabled(enabled);
 		this.confirmSaveCheckBox.setEnabled(enabled);
 		this.autoIncrementCheckBox.setEnabled(enabled);
 		this.repeatCountText.setEnabled(enabled);
 		
 		this.commentInput.setEnabled(enabled);
 		this.eventsTabFolder.setEnabled(enabled);
 		
 		if(enabled) {
 			if(this.currentChain.getSavePluginController().getPlugin() != null && 
 			   this.currentChain.getSavePluginController().getPlugin().
 			                     getParameters().size() > 0) {
 				this.fileFormatOptionsButton.setEnabled(true);
 			} else {
 				this.fileFormatOptionsButton.setEnabled(false);
 			}
 		} else {
 			this.fileFormatOptionsButton.setEnabled(false);
 		}	
 	}
 	
 	/*
 	 * called by setCurrentChain() and updateEvent() to check for errors in 
 	 * user input and show error labels
 	 */
 	private void checkForErrors()
 	{
 		if(this.currentChain.getSavePluginController().
 							 getModelErrors().size() > 0) 
 		{
 			this.fileFormatComboErrorLabel.setImage(PlatformUI.getWorkbench().
 				getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 			this.fileFormatComboErrorLabel.setToolTipText(
 				"There is at least one error in the plug in configuration!");
 			this.fileFormatCombo.deselectAll();
 		} else {
 			this.fileFormatComboErrorLabel.setImage(null);
 			this.fileFormatComboErrorLabel.setToolTipText("");	
 		}		
 		
 		final List<IModelError> errorList = this.currentChain.getModelErrors();
 		final Iterator<IModelError> it = errorList.iterator();
 	
 		while(it.hasNext()) {
 			final IModelError modelError = it.next();
 			if(modelError instanceof ChainError) {
 				final ChainError chainError = (ChainError)modelError;
 				this.filenameErrorLabel.setImage(PlatformUI.getWorkbench().
 					getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 				if(chainError.getErrorType() == ChainErrorTypes.FILENAME_EMPTY) {
 					this.filenameErrorLabel.setToolTipText(
 							"Filename must not be empty!");
 				} else if(chainError.getErrorType() == 
 						  ChainErrorTypes.FILENAME_ILLEGAL_CHARACTER) {
 					this.filenameErrorLabel.setToolTipText(
 							"Filename contains illegal characters!");
 				}
 			}
 		}			
 
 		if(this.currentChain.getPauseControlEventManager().
 							 getModelErrors().size() > 0) 
 		{
 			this.pauseTabItem.setImage(PlatformUI.getWorkbench().
 				getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		} else {
 			this.pauseTabItem.setImage(null);
 		}
 		if(this.currentChain.getBreakControlEventManager().
 				getModelErrors().size() > 0) {
 			this.breakTabItem.setImage(PlatformUI.getWorkbench().
 				getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		} else {
 			this.breakTabItem.setImage(null);
 		}
 		if(this.currentChain.getRedoControlEventManager().
 							 getModelErrors().size() > 0) {
 			this.redoTabItem.setImage(PlatformUI.getWorkbench().
 				getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		} else {
 			this.redoTabItem.setImage(null);
 		}
 		if(this.currentChain.getStopControlEventManager().
 							 getModelErrors().size() > 0) 
 		{
 			this.stopTabItem.setImage(PlatformUI.getWorkbench().
 				getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		} else {
 			this.stopTabItem.setImage(null);
 		}
 	}
 	
 	/*
 	 * used by updateEvent() to re-enable listeners
 	 */
 	private void addListeners()
 	{
 		this.filenameInput.addModifyListener(fileNameInputModifiedListener);
 		this.repeatCountText.addModifyListener(repeatCountTextModifiedListener);
 		this.fileFormatCombo.addSelectionListener(fileFormatComboSelectionListener);
 		this.commentInput.addModifyListener(commentInputModifiedListener);
 		
 		this.confirmSaveCheckBox.addSelectionListener(
 				confirmSaveCheckBoxSelectionListener);
 		this.autoIncrementCheckBox.addSelectionListener(
 				autoIncrementCheckBoxSelectionListener);
 		this.saveScanDescriptionCheckBox.addSelectionListener(
 				saveScanDescriptionCheckBoxSelectionListener);
 		this.fileFormatOptionsButton.addSelectionListener(
 				fileFormatOptionsButtonSelectionListener);
 	}
 	
 	/*
 	 * used by updateEvent() to temporarily disable listeners (preventing 
 	 * event loops)
 	 */
 	private void removeListeners()
 	{
 		this.filenameInput.removeModifyListener(fileNameInputModifiedListener);
 		this.repeatCountText.removeModifyListener(
 				repeatCountTextModifiedListener);
 		this.fileFormatCombo.removeSelectionListener(
 				fileFormatComboSelectionListener);
 		this.commentInput.removeModifyListener(commentInputModifiedListener);
 		
 		this.confirmSaveCheckBox.removeSelectionListener(
 				confirmSaveCheckBoxSelectionListener);
 		this.autoIncrementCheckBox.removeSelectionListener(
 				autoIncrementCheckBoxSelectionListener);
 		this.saveScanDescriptionCheckBox.removeSelectionListener(
 				saveScanDescriptionCheckBoxSelectionListener);
 		this.fileFormatOptionsButton.removeSelectionListener(
 				fileFormatOptionsButtonSelectionListener);
 	}
 	
 	/*
 	 * used by several listeners to adjust the height
 	 */
 	private void setHeight()
 	{
 		int height = bar.getSize().y - saveOptionsExpandItem.getHeaderHeight() - 
 					 (saveOptionsExpandItem.getExpanded() 
 					  ? saveOptionsExpandItem.getHeight() 
 					  : 0) - 
 					 commentExpandItem.getHeaderHeight() - 
 					 (commentExpandItem.getExpanded() 
 					  ? commentExpandItem.getHeight() 
 					  : 0) - 
 					 eventsExpandItem.getHeaderHeight() - 20;
 		eventsExpandItem.setHeight(height < 150 ? 150 : height);
 	}
 	
 	// ************************************************************************
 	// *************************** Listener ***********************************
 	// ************************************************************************
 	
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
 				currentChain.getSaveFilename() != "") 
 			{ // there already is a filename (and path) -> show the path
 				lastSeperatorIndex = currentChain.getSaveFilename().
 											lastIndexOf(File.separatorChar);
 				filePath = currentChain.getSaveFilename().
 									substring(0, lastSeperatorIndex + 1);
 			}
 			else {
 				// no filename -> set path to <rootDir>/daten/ or <rootDir>
 				String rootdir = Activator.getDefault().getRootDirectory();
 				File file = new File(rootdir + "daten/");
 				if(file.exists()) {
 					filePath = rootdir + "daten/";
 				} else {
 					filePath = rootdir;
 				}
 				file = null;
 			}
 			
 			logger.debug(filePath);
 			
 			Shell shell = getSite().getShell();
 
 			FileDialog fileWindow = new FileDialog(shell, SWT.SAVE);
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
 
 					if ((lastPoint > 0) && (lastPoint > lastSep))
 						filenameInput.setText(
 								name.substring(0, lastPoint) + "." + suffix);
 					else
 						filenameInput.setText(name + "." + suffix);
 				}
 				else
 					filenameInput.setText(name);
 			}
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mouseUp(MouseEvent e) {
 		}
 	}	
 	
 	// ******************* Control Listener *******************
 	
 	/**
 	 * {@link org.eclipse.swt.events.ControlListener} of 
 	 * <code>savingsComposite</code>.
 	 */
 	class SavingCompositeControlListener implements ControlListener {
 		
 		/**
 		 * {@inheritDoc}<br><br>
 		 * Ensures a minimum height of 150px.
 		 */
 		@Override
 		public void controlMoved(final ControlEvent e) {
 			setHeight();
 		}
 
 		/**
 		 * {@inheritDoc}<br><br>
 		 * Ensures a minimum height of 150px.
 		 */
 		@Override
 		public void controlResized(final ControlEvent e) {
 			setHeight();
 		}
 	}
 	
 	/**
 	 * {@link org.eclipse.swt.events.ControlListener} of 
 	 * <code>eventsComposite</code>.
 	 */
 	class EventsCompositeControlListener implements ControlListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void controlMoved(ControlEvent e) {
 			eventsComposite.setFocus();
 			setHeight();
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void controlResized(ControlEvent e) {
 			setHeight();
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
 	
 	/**
 	 * {@link org.eclipse.swt.events.ControlListener} of 
 	 * (expand-)<code>bar</code>.
 	 */
 	class BarControlListener implements ControlListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void controlMoved(ControlEvent e) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void controlResized(ControlEvent e) {
 			setHeight();			
 		}
 	}
 	
 	// ******************* Modify Listener *******************
 	
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
 			currentChain.setSaveFilename(filenameInput.getText());
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
 			repeatCountText.removeModifyListener(repeatCountTextModifiedListener);
 			try {
 				currentChain.getScanDescription().setRepeatCount(
 						Integer.parseInt(repeatCountText.getText()));
 			} catch(final NumberFormatException ex) {
 				repeatCountText.setText("");
 			}
 			repeatCountText.addModifyListener(repeatCountTextModifiedListener);
 		}
 	}
 	
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
 			if( fileFormatCombo.getText().equals("") || 
 				Helper.contains(fileFormatCombo.getItems(), 
 								fileFormatCombo.getText())) 
 			{
 				currentChain.getSavePluginController().setPlugin(
 						Activator.getDefault().getMeasuringStation().
 								  getPluginByName(fileFormatCombo.getText()));
 				fileFormatComboErrorLabel.setImage(null);	
 				fileFormatComboErrorLabel.setToolTipText("");
 			} else {
 				currentChain.getSavePluginController().setPlugin(null);
 				fileFormatComboErrorLabel.setImage(PlatformUI.getWorkbench().
 											getSharedImages().getImage(
 											ISharedImages.IMG_OBJS_ERROR_TSK));	
 				fileFormatComboErrorLabel.setToolTipText(
 						"The Plug-In cannot be found");
 			}
 			fileFormatOptionsButton.setEnabled(
 					currentChain.getSavePluginController().getPlugin() != null && 
 					currentChain.getSavePluginController().getPlugin().
 								 getParameters().size() > 0 );
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
 }
