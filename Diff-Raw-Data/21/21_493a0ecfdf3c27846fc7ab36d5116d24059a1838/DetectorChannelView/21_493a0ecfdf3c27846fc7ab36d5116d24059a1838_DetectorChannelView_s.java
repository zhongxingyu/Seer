 package de.ptb.epics.eve.editor.views.detectorchannelview;
 
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.ExpandBar;
 import org.eclipse.swt.widgets.ExpandItem;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.layout.GridData;
 
 import de.ptb.epics.eve.data.measuringstation.Event;
 import de.ptb.epics.eve.data.scandescription.Channel;
 import de.ptb.epics.eve.data.scandescription.errors.ChannelError;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 import de.ptb.epics.eve.editor.Activator;
 import de.ptb.epics.eve.editor.views.EventComposite;
 
 /**
  * <code>DetectorChannelView</code> is a composite to input the parameters
  * of a detector channel from a scanModul.
  * 
  * @author Hartmut Scherr
  * @author Marcus Michalsky
  */
 public class DetectorChannelView extends ViewPart implements IModelUpdateListener {
 
 	/**
 	 * the unique identifier of the view.
 	 */
 	public static final String ID = 
 		"de.ptb.epics.eve.editor.views.DetectorChannelView";
 	
 	// logging
 	private static Logger logger = 
 			Logger.getLogger(DetectorChannelView.class.getName());
 	
 	
 	// *******************************************************************
 	// ********************** underlying model ***************************
 	// ******************************************************************* 
 	
 	// the detector channel containing the information that is shown and 
 	// allowed for editing.
 	private Channel currentChannel;
 	
 	// *******************************************************************
 	// ****************** end of: underlying model ***********************
 	// *******************************************************************
 	
 	private boolean modelUpdateListenerSuspended;
 
 	private Composite top = null;
 	private ScrolledComposite sc = null;
 
 	private Label averageLabel;
 	private Text averageText;
 	private Label averageErrorLabel;
 	private TextNumberVerifyListener averageTextVerifyListener;
 	private AverageTextModifyListener averageTextModifyListener;
 	
 	private Label maxDeviationLabel;
 	private Text maxDeviationText;
 	private Label maxDeviationErrorLabel;
 	private TextDoubleVerifyListener maxDeviationTextVerifyListener;
 	private MaxDeviationTextModifyListener maxDeviationTextModifyListener;
 	
 	private Label minimumLabel;
 	private Text minimumText;
 	private Label minimumErrorLabel;
 	private TextDoubleVerifyListener minimumTextVerifyListener;
 	private MinimumTextModifyListener minimumTextModifyListener;
 	
 	private Label maxAttemptsLabel;
 	private Text maxAttemptsText;
 	private Label maxAttemptsErrorLabel;	
 	private TextNumberVerifyListener maxAttemptsTextVerifyListener;
 	private MaxAttemptsTextModifyListener maxAttemptsTextModifyListener;
 	
 	private Button confirmTriggerManualCheckBox;
 	private ConfirmTriggerManualCheckBoxSelectionListener
 			confirmTriggerManualCheckBoxSelectionListener;
 	
 	private ExpandBar bar = null;
 	private BarControlListener barControlListener;
 	private ExpandItem itemEventOptions;
 	private CTabFolder eventsTabFolder = null;
 	private EventsTabFolderSelectionListener eventsTabFolderSelectionListener;
 	private EventComposite redoEventComposite = null;
 	private EventCompositeControlListener eventCompositeControlListener;
 	private Composite eventComposite = null;
 
 	private CTabItem redoEventTabItem;
 	
 	private Button detectorReadyEventCheckBox;
 	private DetectorReadyEventCheckBoxSelectionListener 
 			detectorReadyEventCheckBoxSelectionListener;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createPartControl(final Composite parent) {
 		parent.setLayout(new FillLayout());
 		
 		if(Activator.getDefault().getMeasuringStation() == null) {
 			final Label errorLabel = new Label(parent, SWT.NONE);
 			errorLabel.setText("No Measuring Station has been loaded. " +
 							   "Please check Preferences!");
 			return;
 		}
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		GridData gridData;
 		
 		this.sc = new ScrolledComposite(parent, SWT.H_SCROLL | 
 										SWT.V_SCROLL | SWT.BORDER);
 
 		this.top = new Composite(sc, SWT.NONE);
 		this.top.setLayout(gridLayout);
 
 		sc.setContent(this.top);
 		sc.setExpandHorizontal(true);
 		sc.setExpandVertical(true);
 		
 		// GUI: Average: <TextBox> x
 		this.averageLabel = new Label(this.top, SWT.NONE);
 		this.averageLabel.setText("Average:");
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.averageLabel.setLayoutData(gridData);
 		
 		this.averageText = new Text(this.top, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		this.averageText.setLayoutData(gridData);
 		this.averageTextVerifyListener = new TextNumberVerifyListener();
 		this.averageText.addVerifyListener(averageTextVerifyListener);
 		this.averageTextModifyListener = new AverageTextModifyListener();
 		this.averageText.addModifyListener(averageTextModifyListener); 
 		this.averageErrorLabel = new Label(this.top, SWT.NONE);
 		
 		// GUI: Max. Deviation (%): <TextBox> x
 		this.maxDeviationLabel = new Label(this.top, SWT.NONE);
 		this.maxDeviationLabel.setText("Max. Deviation (%):");
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.maxDeviationLabel.setLayoutData(gridData);
 		
 		this.maxDeviationText = new Text(this.top, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		this.maxDeviationText.setLayoutData(gridData);
 		this.maxDeviationTextVerifyListener = new TextDoubleVerifyListener();
 		this.maxDeviationText.addVerifyListener(maxDeviationTextVerifyListener);
 		this.maxDeviationTextModifyListener = new MaxDeviationTextModifyListener();
 		this.maxDeviationText.addModifyListener(maxDeviationTextModifyListener);
 		this.maxDeviationErrorLabel = new Label(this.top, SWT.NONE);
 		
 		// GUI: Minimum: <TextBox> x
 		this.minimumLabel = new Label(this.top, SWT.NONE);
 		this.minimumLabel.setText("Minimum:");
 		this.minimumLabel.setToolTipText(
 				"for values < minimum no deviation check");
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.minimumLabel.setLayoutData(gridData);
 		
 		this.minimumText = new Text(this.top, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		this.minimumText.setLayoutData(gridData);
 		this.minimumTextVerifyListener = new TextDoubleVerifyListener();
 		this.minimumText.addVerifyListener(minimumTextVerifyListener);
 		this.minimumTextModifyListener = new MinimumTextModifyListener();
 		this.minimumText.addModifyListener(minimumTextModifyListener);
 		this.minimumErrorLabel = new Label(this.top, SWT.NONE);
 		
 		// GUI: Max. Attempts: <TextBox> x
 		this.maxAttemptsLabel = new Label(this.top, SWT.NONE);
 		this.maxAttemptsLabel.setText("Max. Attempts:");
 		this.maxAttemptsLabel.setToolTipText(
 				"Maximum attemps to calculate deviation:" );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.maxAttemptsLabel.setLayoutData(gridData);
 		
 		this.maxAttemptsText = new Text(this.top, SWT.BORDER);
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		this.maxAttemptsText.setLayoutData(gridData);
 		this.maxAttemptsTextVerifyListener = new TextNumberVerifyListener();
 		this.maxAttemptsText.addVerifyListener(maxAttemptsTextVerifyListener);
 		this.maxAttemptsTextModifyListener = new MaxAttemptsTextModifyListener();
 		this.maxAttemptsText.addModifyListener(maxAttemptsTextModifyListener);
 		this.maxAttemptsErrorLabel = new Label(this.top, SWT.NONE);
 		
 		// GUI: [] Confirm Trigger Manual
 		this.confirmTriggerManualCheckBox = new Button(this.top, SWT.CHECK);
 		this.confirmTriggerManualCheckBox.setText("Confirm Trigger manual");
 		this.confirmTriggerManualCheckBox.setToolTipText(
 				"Mark to ask before trigger this channel");
 		gridData = new GridData();
 		gridData.horizontalSpan = 2;
 		this.confirmTriggerManualCheckBox.setLayoutData(gridData);
 		this.confirmTriggerManualCheckBoxSelectionListener = 
 				new ConfirmTriggerManualCheckBoxSelectionListener();
 		this.confirmTriggerManualCheckBox.addSelectionListener( 
 				confirmTriggerManualCheckBoxSelectionListener);
 
 		top.layout();
 		// Expand Bar
 		this.bar = new ExpandBar(this.top, SWT.V_SCROLL);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 // TODO: Warum wird bei GridData.FILL die Breite des Fensters zu groß angelegt?
 // 		Woher wird bestimmt wie breit FILL gesetzt wird? (Hartmut 23.6.11)
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 3;
 		this.bar.setLayoutData(gridData);
 		this.barControlListener = new BarControlListener();
 		this.bar.addControlListener(barControlListener);
 		
 		// Event Section
 		gridLayout = new GridLayout();
 		this.eventComposite = new Composite(this.bar, SWT.NONE);
 		this.eventComposite.setLayout(gridLayout);
 		this.eventCompositeControlListener = new EventCompositeControlListener();
 		this.eventComposite.addControlListener(eventCompositeControlListener);
 
 		// first expand item (Events)
 		this.itemEventOptions = new ExpandItem (this.bar, SWT.NONE, 0);
 		this.itemEventOptions.setText("Event options");
 		this.itemEventOptions.setHeight(
 				this.eventComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
 		this.itemEventOptions.setControl(this.eventComposite);
 
 		this.detectorReadyEventCheckBox = 
 				new Button(this.eventComposite, SWT.CHECK);
 		this.detectorReadyEventCheckBox.setText("Send Detector Ready Event");
 		this.detectorReadyEventCheckBox.setToolTipText(
 				"Mark to send detector ready event if channel is ready");
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.detectorReadyEventCheckBox.setLayoutData(gridData);
 		this.detectorReadyEventCheckBoxSelectionListener = 
 				new DetectorReadyEventCheckBoxSelectionListener();
 		this.detectorReadyEventCheckBox.addSelectionListener(
 				detectorReadyEventCheckBoxSelectionListener);
 
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalAlignment = GridData.FILL;
 		
 		// Event Options Tab
 		eventsTabFolder = new CTabFolder(this.eventComposite, SWT.FLAT );
 		eventsTabFolder.setLayoutData(gridData);
 		eventsTabFolderSelectionListener = new EventsTabFolderSelectionListener();
 		eventsTabFolder.addSelectionListener(eventsTabFolderSelectionListener);
 		
 		redoEventComposite = new EventComposite(eventsTabFolder, SWT.NONE);
 		 
 		this.redoEventTabItem = new CTabItem(eventsTabFolder, SWT.FLAT);
 		this.redoEventTabItem.setText("Redo");
 		this.redoEventTabItem.setToolTipText("Repeat the current reading " +
 				"of the channel, if redo event occurs");
 		this.redoEventTabItem.setControl(redoEventComposite);
 		
 		this.averageText.setEnabled(false);
 		this.maxDeviationText.setEnabled(false);
 		this.minimumText.setEnabled(false);
 		this.maxAttemptsText.setEnabled(false);
 		this.confirmTriggerManualCheckBox.setEnabled(false);
 		this.detectorReadyEventCheckBox.setEnabled(false);
 		this.eventsTabFolder.setEnabled(false);
 
 		top.setVisible(false);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setFocus() {
 	}
 
 	/**
 	 * 
 	 * @param channel
 	 */
 	public void setChannel(final Channel channel) {
 		
 		if(channel != null)
 			logger.debug("set channel (" + 
 						 channel.getDetectorChannel().getID() + ")");
 		else
 			logger.debug("set channel (null)");
 		
 		if(this.currentChannel != null) {
 			this.currentChannel.removeModelUpdateListener(this);
 		}
 		this.currentChannel = channel;
 		
 		if(this.currentChannel != null) {
 			this.currentChannel.addModelUpdateListener(this);
 		}
 		
 		updateEvent(null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void updateEvent(ModelUpdateEvent modelUpdateEvent) {
 		
 		if(modelUpdateEvent != null) 
 			logger.debug(modelUpdateEvent.getSender().getClass().getName());
 
 		if(modelUpdateListenerSuspended) return;
 
 		removeListeners();
 		
 		if(this.currentChannel != null) {
 			// current channel set -> update widgets
 			
 			// set the view title
 			this.setPartName(
 					currentChannel.getAbstractDevice().getFullIdentifyer());
 			
 			// set average text
 			this.averageText.setText("" + this.currentChannel.getAverageCount());
			
 			// set max deviation
 			if(this.currentChannel.getMaxDeviation() != Double.NEGATIVE_INFINITY) {
 				this.maxDeviationText.setText(
 						"" + this.currentChannel.getMaxDeviation());
 			} else {
 				this.maxDeviationText.setText("");
 			}
 			
 			// set minimum
 			if(this.currentChannel.getMinumum() != Double.NEGATIVE_INFINITY) {
 				this.minimumText.setText("" + this.currentChannel.getMinumum());
 			} else {
 				this.minimumText.setText("");
 			}
 			
 			// set max attempts
 			if(this.currentChannel.getMaxAttempts() != Integer.MIN_VALUE) {
 				this.maxAttemptsText.setText(
 						"" + this.currentChannel.getMaxAttempts());
 			} else {
 				this.maxAttemptsText.setText("");
 			}
 			
 			// set confirm trigger check box
 			this.confirmTriggerManualCheckBox.setSelection(
 					this.currentChannel.isConfirmTrigger());
 			
 			// set detector ready event check box
 			this.detectorReadyEventCheckBox.setSelection(
 					this.currentChannel.getDetectorReadyEvent() != null);
 			
 			this.averageText.setEnabled(true);
 			this.maxDeviationText.setEnabled(true);
 			this.minimumText.setEnabled(true);
 			this.maxAttemptsText.setEnabled(true);
 			this.confirmTriggerManualCheckBox.setEnabled(true);
 			this.detectorReadyEventCheckBox.setEnabled(true);
 			this.eventsTabFolder.setEnabled(true);
 			
 			this.redoEventComposite.setControlEventManager(
 					this.currentChannel.getRedoControlEventManager());
 			
 			if (sc.getMinHeight() == 0) {
 				// Wenn das erste Mal die DetectorChannelView für einen Channel 
 				// aufgerufen wird, gibt es noch keine Mindesthöhe für die 
 				// Scrollbar. Die wird hier dann gesetzt.
 
 				int height = bar.getBounds().y + 
 							 itemEventOptions.getHeight() + 
 							 itemEventOptions.getHeaderHeight() + 5;
 // TODO: Höhe und Breite muß noch besser berechnet werden (Hartmut 22.6.11)
 //				int width = bar.getBounds().x + bar.getBounds().width - 25;
 				int width = bar.getBounds().x + bar.getBounds().width -25;
 				sc.setMinSize(this.top.computeSize(width, height));
 			}
 		
 			checkForErrors();			
 			top.setVisible(true);
 			
 		} else {
 			// currentChannel is null
 			this.averageText.setText("");
 			this.maxDeviationText.setText("");
 			this.minimumText.setText("");
 			this.maxAttemptsText.setText("");
 			this.confirmTriggerManualCheckBox.setSelection(false);
 			this.detectorReadyEventCheckBox.setSelection(false);
 			this.eventsTabFolder.setEnabled(false);
 			
 			this.averageText.setEnabled(false);
 			this.maxDeviationText.setEnabled(false);
 			this.minimumText.setEnabled(false);
 			this.maxAttemptsText.setEnabled(false);
 			this.confirmTriggerManualCheckBox.setEnabled(false);
 			this.detectorReadyEventCheckBox.setEnabled(false);
 			this.setPartName("No Detector Channel selected");
 			
 			this.averageErrorLabel.setImage(null);
 			this.maxDeviationErrorLabel.setImage(null);
 			this.minimumErrorLabel.setImage(null);
 			this.maxAttemptsErrorLabel.setImage(null);
 			
 			this.redoEventComposite.setControlEventManager(null);
 			
 			top.setVisible(false);
 		}
 		
 		addListeners();
 	}
 
 	/*
 	 * 
 	 */
 	private void checkForErrors()
 	{
     	// reset errors
     	this.averageErrorLabel.setImage(null);
 		this.averageErrorLabel.setToolTipText("");
 		this.maxDeviationErrorLabel.setImage(null);
 		this.maxDeviationErrorLabel.setToolTipText("");
 		this.minimumErrorLabel.setImage(null);
 		this.minimumErrorLabel.setToolTipText("");	
 		this.maxAttemptsErrorLabel.setImage(null);
 		this.maxAttemptsErrorLabel.setToolTipText("");	
 
 		final Iterator<IModelError> it = 
 				this.currentChannel.getModelErrors().iterator();
 
 		while(it.hasNext()) {
 			final IModelError modelError = it.next();
 			if(modelError instanceof ChannelError) {
 				final ChannelError channelError = (ChannelError) modelError;
 				
 				switch(channelError.getErrorType()) {
 					case MAX_DEVIATION_NOT_POSSIBLE:
 						this.maxDeviationErrorLabel.setImage(
 								PlatformUI.getWorkbench().getSharedImages().
 								getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 						this.maxDeviationErrorLabel.setToolTipText("Max Deviation value not possible");
 						// update and resize View with getParent().layout()
 						this.maxDeviationErrorLabel.getParent().layout();
 						break;
 					case MINIMUM_NOT_POSSIBLE:
 						this.minimumErrorLabel.setImage(
 								PlatformUI.getWorkbench().getSharedImages().
 								getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 						this.minimumErrorLabel.setToolTipText("Minimum value not possible");
 						this.minimumErrorLabel.getParent().layout();
 						break;
					case MAX_ATTEMPTS_NOT_POSSIBLE:
						this.maxAttemptsErrorLabel.setImage(
								PlatformUI.getWorkbench().getSharedImages().
								getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
						this.maxAttemptsErrorLabel.setToolTipText("Max Attempts value not possible");
						this.maxAttemptsErrorLabel.getParent().layout();
						break;
 				}
 				
 			}
 		}
 		if(this.currentChannel.getRedoControlEventManager().
 							   getModelErrors().size() > 0) {
 			this.redoEventTabItem.setImage(
 					PlatformUI.getWorkbench().getSharedImages().
 					getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
 		} else {
 			this.redoEventTabItem.setImage(null);
 		}
 	}
 	
 	protected void suspendModelUpdateListener()
 	{
 		currentChannel.removeModelUpdateListener(this);
 		modelUpdateListenerSuspended = true;
 	}
 	
 	protected void resumeModelUpdateListener()
 	{
 		currentChannel.addModelUpdateListener(this);
 		modelUpdateListenerSuspended = false;
 	}
 
 	/*
 	 * 
 	 */
 	private void addListeners()
 	{
 		averageText.addModifyListener(averageTextModifyListener);
 		averageText.addVerifyListener(averageTextVerifyListener);
 		maxDeviationText.addModifyListener(maxDeviationTextModifyListener);
 		maxDeviationText.addVerifyListener(maxDeviationTextVerifyListener);
 		minimumText.addModifyListener(minimumTextModifyListener);
 		minimumText.addVerifyListener(minimumTextVerifyListener);
 		maxAttemptsText.addModifyListener(maxAttemptsTextModifyListener);
 		maxAttemptsText.addVerifyListener(maxAttemptsTextVerifyListener);
 		
 		confirmTriggerManualCheckBox.addSelectionListener(
 				confirmTriggerManualCheckBoxSelectionListener);
 		eventsTabFolder.addSelectionListener(eventsTabFolderSelectionListener);
 		detectorReadyEventCheckBox.addSelectionListener(
 				detectorReadyEventCheckBoxSelectionListener);
 		
 		bar.addControlListener(barControlListener);
 		eventComposite.addControlListener(eventCompositeControlListener);
 	}
 	
 	/*
 	 * 
 	 */
 	private void removeListeners()
 	{
 		averageText.removeModifyListener(averageTextModifyListener);
 		averageText.removeVerifyListener(averageTextVerifyListener);
 		maxDeviationText.removeModifyListener(maxDeviationTextModifyListener);
 		maxDeviationText.removeVerifyListener(maxDeviationTextVerifyListener);
 		minimumText.removeModifyListener(minimumTextModifyListener);
 		minimumText.removeVerifyListener(minimumTextVerifyListener);
 		maxAttemptsText.removeModifyListener(maxAttemptsTextModifyListener);
 		maxAttemptsText.removeVerifyListener(maxAttemptsTextVerifyListener);
 		
 		confirmTriggerManualCheckBox.removeSelectionListener(
 				confirmTriggerManualCheckBoxSelectionListener);
 		eventsTabFolder.removeSelectionListener(eventsTabFolderSelectionListener);
 		detectorReadyEventCheckBox.removeSelectionListener(
 				detectorReadyEventCheckBoxSelectionListener);
 		
 		bar.removeControlListener(barControlListener);
 		eventComposite.removeControlListener(eventCompositeControlListener);
 	}
 	
 	///////////////////////////////////////////////////////////
 	// Hier kommen jetzt die verschiedenen Listener Klassen
 	///////////////////////////////////////////////////////////
 
 	/**
 	 * <code>ModifyListener</code> of <code>AverageText</code>.
 	 */
 	class AverageTextModifyListener implements ModifyListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText(final ModifyEvent e) {
 			logger.debug("average text modified");
 // sollen logger Infos auch in das MotorAxisStart... geschrieben werden?
 			
 			suspendModelUpdateListener();
 
 			if(currentChannel != null) {
 				try {
 					currentChannel.setAverageCount(Integer.parseInt(averageText.getText()));
 					logger.debug("set average text to: " + Integer.parseInt(averageText.getText()));
 				} catch(final NumberFormatException ex) {
 					// set default value (1)
 					currentChannel.setAverageCount(1);
 				}
 			}
 			checkForErrors();
 			resumeModelUpdateListener();
 		}
 	}
 
 	/**
 	 * <code>ModifyListener</code> of <code>MaxDeviationText</code>.
 	 */
 	class MaxDeviationTextModifyListener implements ModifyListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText(final ModifyEvent e) {
 
 			suspendModelUpdateListener();
 			
 			if(currentChannel != null) {
 				if(maxDeviationText.getText().equals("")) {
 					currentChannel.setMaxDeviation(Double.NEGATIVE_INFINITY);
 				} else {
 					try {
 						currentChannel.setMaxDeviation(Double.parseDouble(maxDeviationText.getText()));
 					} catch(final NumberFormatException ex) {
 						currentChannel.setMaxDeviation(Double.NaN);
 					}
 				}
 			}
 			checkForErrors();
 			resumeModelUpdateListener();
 		}
 	}
 
 	/**
 	 * <code>ModifyListener</code> of <code>MinimumText</code>.
 	 */
 	class MinimumTextModifyListener implements ModifyListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText(final ModifyEvent e) {
 
 			suspendModelUpdateListener();
 
 			if(currentChannel != null) {
 				if(minimumText.getText().equals("")) {
 					currentChannel.setMinumum(Double.NEGATIVE_INFINITY);
 				} else {
 					try {
 						currentChannel.setMinumum(Double.parseDouble( minimumText.getText()));
 					} catch(final NumberFormatException ex) {
 						currentChannel.setMinumum(Double.NaN);
 					}
 				}
 			}
 			checkForErrors();
 			resumeModelUpdateListener();
 		}
 	}
 
 	/**
 	 * <code>ModifyListener</code> of <code>MaxAttemptsText</code>.
 	 */
 	class MaxAttemptsTextModifyListener implements ModifyListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText(final ModifyEvent e) {
 
 			suspendModelUpdateListener();
 
 			if(currentChannel != null) {
 				if(maxAttemptsText.getText().equals("")) {
 					currentChannel.setMaxAttempts(Integer.MIN_VALUE);
 				} else {
 					try {
 						currentChannel.setMaxAttempts(Integer.parseInt(maxAttemptsText.getText()));
 					} catch(final NumberFormatException ex) {
 						currentChannel.setMaxAttempts(-1);
 					}
 				}
 			}
 			checkForErrors();
 			resumeModelUpdateListener();
 		}
 	}
 
 	/**
 	 * <code>SelectionListener</code> of 
 	 * <code>confirmTriggerManualCheckBox</code>.
 	 */
 	class ConfirmTriggerManualCheckBoxSelectionListener implements SelectionListener {
 
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
 			if(currentChannel != null) {
 				currentChannel.setConfirmTrigger(
 						confirmTriggerManualCheckBox.getSelection());
 			}
 		}
 	}
 
 	/**
 	 * <code>SelectionListener</code> of 
 	 * <code>detectorReadyEventCheckBox</code>.
 	 */
 	class DetectorReadyEventCheckBoxSelectionListener implements SelectionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(final SelectionEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 			// we create an event and add it to the list if selected 
 			// or remove the event with same id from the list if deselected
 			Event detReadyEvent = new Event(
 					currentChannel.getAbstractDevice().getID(), 
 					currentChannel.getAbstractDevice().getParent().getName(), 
 					currentChannel.getAbstractDevice().getName(), 
 					currentChannel.getParentScanModul().getChain().getId(), 
 					currentChannel.getParentScanModul().getId());
 
 			if(detectorReadyEventCheckBox.getSelection()) {
 				currentChannel.getParentScanModul().getChain().
 							   getScanDescription().add(detReadyEvent);
 				currentChannel.setDetectorReadyEvent(detReadyEvent);
 			} else {
 				currentChannel.getParentScanModul().getChain().
 							   getScanDescription().removeEventById(
 									   detReadyEvent.getID());
 				currentChannel.setDetectorReadyEvent(null);
 			}
 		}
 	}
 	
 	/**
 	 * <code>ControlListener</code> of <code>bar</code>.
 	 */
 	class BarControlListener implements ControlListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void controlMoved(final ControlEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void controlResized(final ControlEvent e) {
 			int height = bar.getSize().y - itemEventOptions.getHeaderHeight() - 20;
 			itemEventOptions.setHeight(height < 200 ? 200 : height);
 		}
 	}
 
 	/**
 	 * <code>SelectionListener</code> of <code>eventsTabFolder</code>.
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
 			// Einträge in der Auswahlliste werden aktualisiert
 			CTabItem wahlItem = eventsTabFolder.getSelection();
 			EventComposite wahlComposite = (EventComposite)wahlItem.getControl();
 			wahlComposite.setEventChoice();
 		}
 	}
 
 	/**
 	 * <code>ControlListener</code> of <code>eventComposite</code>.
 	 */
 	class EventCompositeControlListener implements ControlListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void controlMoved(ControlEvent e) {
 			// Erst werden die Children ausgelesen um nachzusehen, welches
 			// Child ein CTabFolder ist, dieses bekommt dann den Focus!
 			Control[] childArray = eventComposite.getChildren();
 			for( int i = 0; i < childArray.length; ++i ) {
 				if (childArray[i].toString().equals("CTabFolder {}")) {
 					childArray[i].setFocus();
 				}
 			}
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void controlResized(ControlEvent e) {
 		}	
 	}
 
 	/**
 	 * <code>VerifyListener</code> of Text Widget from
 	 * <code>DetectorChannelView</code>
 	 */
 	class TextNumberVerifyListener implements VerifyListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void verifyText(VerifyEvent e) {
 
 			switch (e.keyCode) {  
             	case SWT.BS:           // Backspace  
             	case SWT.DEL:          // Delete  
 			    case SWT.HOME:         // Home  
 			    case SWT.END:          // End  
 			    case SWT.ARROW_LEFT:   // Left arrow  
 			    case SWT.ARROW_RIGHT:  // Right arrow  
 			    	return;  
 			}  
 
 			if (!Character.isDigit(e.character)) {  
 					e.doit = false;  // disallow the action  
 		    }  			
 		}
 
 	}
 
 	/**
 	 * <code>VerifyListener</code> of Text Widget from
 	 * <code>MotorAxisStartStopStepwidthComposite</code>
 	 */
 	class TextDoubleVerifyListener implements VerifyListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void verifyText(VerifyEvent e) {
 
 			switch (e.keyCode) {  
             	case SWT.BS:           // Backspace  
             	case SWT.DEL:          // Delete  
 			    case SWT.HOME:         // Home  
 			    case SWT.END:          // End  
 			    case SWT.ARROW_LEFT:   // Left arrow  
 			    case SWT.ARROW_RIGHT:  // Right arrow  
 			    	return;  
 			}  
 
 			String oldText = ((Text)(e.widget)).getText();
 
 			if (!Character.isDigit(e.character)) {  
 				if (e.character == '.') {
 					// character . is a valid character, if he is not in the old string
 					if (oldText.contains("."))
 						e.doit = false;
 				} 
 				else if (e.character == '-') {
 					// character - is a valid character as first sign and after an e
 					if (oldText.isEmpty()) {
 						// oldText is emtpy, - is valid
 					}
 					else {
 						// wenn das letzte Zeichen von oldText ein e ist, ist das minus auch erlaubt
 						int index = oldText.length();
 						if (oldText.substring(index-1).equals("e")) {
 							// letzte Zeichen ist ein e und damit erlaubt
 						}
 						else
 							e.doit = false;
 					}
 				} 
 				else if (e.character == 'e') {
 					// character e is a valid character, if he is not in the old string
 					if (oldText.contains("e"))
 						e.doit = false;
 				} 
 				else {
 					e.doit = false;  // disallow the action  
 		        }
 		    }  			
 		}
 	}
 
 }
