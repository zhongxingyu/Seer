 package de.ptb.epics.eve.editor.views;
 
 import java.util.Iterator;
 
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
 import de.ptb.epics.eve.data.scandescription.errors.ChannelErrorTypes;
 import de.ptb.epics.eve.data.scandescription.errors.IModelError;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 import de.ptb.epics.eve.editor.Activator;
 
 /**
  * <code>DetectorChannelView</code> is a composite to input the parameters
  * of a detector channel from a scanModul.
  * @author Hartmut Scherr
  *
  */
 public class DetectorChannelView extends ViewPart implements IModelUpdateListener {
 
 	public static final String ID = "de.ptb.epics.eve.editor.views.DetectorChannelView";
 
 	private Composite top = null;
 	private ScrolledComposite sc = null;
 
 	private Label averageLabel;
 	private Text averageText;
 	private Label averageErrorLabel;
 	
 	private Label maxDeviationLabel;
 	private Text maxDeviationText;
 	private Label maxDeviationErrorLabel;
 	
 	private Label minimumLabel;
 	private Text minimumText;
 	private Label minimumErrorLabel;
 	
 	private Label maxAttemptsLabel;
 	private Text maxAttemptsText;
 	private Label maxAttemptsErrorLabel;
 	
 	private Button confirmTriggerManualCheckBox;
 
 	private ExpandBar bar = null;
 	private ExpandItem item0;
 	private CTabFolder eventsTabFolder = null;
 	private EventComposite redoEventComposite = null;
 	private Composite eventComposite = null;
 
 	private CTabItem redoEventTabItem;
 	
 	private Button detectorReadyEventCheckBox;
 
 	private Channel currentChannel;
 
 	@Override
 	public void createPartControl( final Composite parent ) {
 		parent.setLayout(new FillLayout());
 		
 		if( Activator.getDefault().getMeasuringStation() == null ) {
 			final Label errorLabel = new Label( parent, SWT.NONE );
 			errorLabel.setText( "No Measuring Station has been loaded. Please check Preferences!" );
 			return;
 		}
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		GridData gridData;
 		
		this.sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
 
 		this.top = new Composite( sc, SWT.NONE );
 		this.top.setLayout( gridLayout );
 
 		sc.setContent(this.top);
         sc.setExpandHorizontal(true);
         sc.setExpandVertical(true);
 		
 		this.averageLabel = new Label( this.top, SWT.NONE );
 		this.averageLabel.setText( "Average:" );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.averageLabel.setLayoutData( gridData );
 		
 		this.averageText = new Text( this.top, SWT.BORDER );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		this.averageText.setLayoutData( gridData );
 		this.averageText.addModifyListener( new AverageTextModifyListener()); 
 
 		this.averageErrorLabel = new Label( this.top, SWT.NONE );
 		this.averageErrorLabel.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_WARN_TSK ) );
 		
 		this.maxDeviationLabel = new Label( this.top, SWT.NONE );
 		this.maxDeviationLabel.setText( "Max. Deviation (%):" );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.maxDeviationLabel.setLayoutData( gridData );
 		
 		this.maxDeviationText = new Text( this.top, SWT.BORDER );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		this.maxDeviationText.setLayoutData( gridData );
 		this.maxDeviationText.addModifyListener( new MaxDeviationTextModifyListener());
 		
 		this.maxDeviationErrorLabel = new Label( this.top, SWT.NONE );
 		this.maxDeviationErrorLabel.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_WARN_TSK ) );
 		
 		this.minimumLabel = new Label( this.top, SWT.NONE );
 		this.minimumLabel.setText( "Minumum:" );
 		this.minimumLabel.setToolTipText("for values < minimum no deviation check");
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.minimumLabel.setLayoutData( gridData );
 		
 		this.minimumText = new Text( this.top, SWT.BORDER );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		this.minimumText.setLayoutData( gridData );
 		this.minimumText.addModifyListener( new MinimumTextModifyListener());
 		
 		this.minimumErrorLabel = new Label( this.top, SWT.NONE );
 		this.minimumErrorLabel.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_WARN_TSK ) );
 		
 		this.maxAttemptsLabel = new Label( this.top, SWT.NONE );
 		this.maxAttemptsLabel.setText( "Max. Attempts:" );
 		this.maxAttemptsLabel.setToolTipText("Maximum attemps to calculate deviation:" );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.maxAttemptsLabel.setLayoutData( gridData );
 		
 		this.maxAttemptsText = new Text( this.top, SWT.BORDER );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		this.maxAttemptsText.setLayoutData( gridData );
 		this.maxAttemptsText.addModifyListener( new MaxAttemptsTextModifyListener());
 		
 		this.maxAttemptsErrorLabel = new Label( this.top, SWT.NONE );
 		this.maxAttemptsErrorLabel.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_WARN_TSK ) );
 		
 		this.confirmTriggerManualCheckBox = new Button( this.top, SWT.CHECK );
 		this.confirmTriggerManualCheckBox.setText( "Confirm Trigger manual" );
 		this.confirmTriggerManualCheckBox.setToolTipText("Mark to ask before trigger this channel");
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 3;
 		this.confirmTriggerManualCheckBox.setLayoutData( gridData );
 		this.confirmTriggerManualCheckBox.addSelectionListener( new ConfirmTriggerManualCheckBoxSelectionListener());
 
 		this.bar = new ExpandBar( this.top, SWT.V_SCROLL);
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 3;
 		this.bar.setLayoutData( gridData );
 		this.bar.addControlListener( new BarControlListener());
 		
 		// Event Section
 		gridLayout = new GridLayout();
 		this.eventComposite = new Composite( this.bar, SWT.NONE );
 		this.eventComposite.setLayout( gridLayout );
 		this.eventComposite.addControlListener(new EventCompositeControlListener());
 
 		// first expand item (Events)
 		this.item0 = new ExpandItem ( this.bar, SWT.NONE, 0);
 		this.item0.setText("Event options");
 		this.item0.setHeight( this.eventComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
 		this.item0.setControl( this.eventComposite );
 
 		
 		this.detectorReadyEventCheckBox = new Button( this.eventComposite, SWT.CHECK );
 		this.detectorReadyEventCheckBox.setText( "Send Detector Ready Event" );
 		this.detectorReadyEventCheckBox.setToolTipText( "Mark to send detector ready event if channel is ready" );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.detectorReadyEventCheckBox.setLayoutData( gridData );
 		this.detectorReadyEventCheckBox.addSelectionListener( new DetectorReadyEventCheckBoxSelectionListener());
 
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalAlignment = GridData.FILL;
 
 		eventsTabFolder = new CTabFolder(this.eventComposite, SWT.FLAT );
 		eventsTabFolder.setLayoutData( gridData );
 		eventsTabFolder.addSelectionListener(new EventsTabFolderSelectionListener());
 
 		
 		redoEventComposite = new EventComposite( eventsTabFolder, SWT.NONE);
 		 
 		this.redoEventTabItem = new CTabItem(eventsTabFolder, SWT.FLAT);
 		this.redoEventTabItem.setText( "Redo" );
 		this.redoEventTabItem.setToolTipText("Repeat the current reading of the channel, if redo event occurs");
 		this.redoEventTabItem.setControl(redoEventComposite);
 		
 		this.averageText.setEnabled( false );
 		this.maxDeviationText.setEnabled( false );
 		this.minimumText.setEnabled( false );
 		this.maxAttemptsText.setEnabled( false );
 		this.confirmTriggerManualCheckBox.setEnabled( false );
 		this.detectorReadyEventCheckBox.setEnabled( false );
 		this.eventsTabFolder.setEnabled(false);
 
 	}
 
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setChannel( final Channel channel ) {
 		if( this.currentChannel != null ) {
 			this.currentChannel.removeModelUpdateListener( this );
 		}
 		this.currentChannel = channel;
 
 		if( this.currentChannel != null ) {
 
 			this.averageText.setText( "" + this.currentChannel.getAverageCount() );
 			
 			if( this.currentChannel.getMaxDeviation() != Double.NEGATIVE_INFINITY ) {
 				this.maxDeviationText.setText( "" + this.currentChannel.getMaxDeviation() );
 			} else {
 				this.maxDeviationText.setText( "" );
 			}
 			
 			if( this.currentChannel.getMinumum() != Double.NEGATIVE_INFINITY ) {
 				this.minimumText.setText( "" + this.currentChannel.getMinumum() );
 			} else {
 				this.minimumText.setText( "" );
 			}
 
 			if( this.currentChannel.getMaxAttempts() != Integer.MIN_VALUE ) {
 				this.maxAttemptsText.setText( "" + this.currentChannel.getMaxAttempts() );
 			} else {
 				this.maxAttemptsText.setText( "" );
 			}
 			
 			this.confirmTriggerManualCheckBox.setSelection( this.currentChannel.isConfirmTrigger() );
 			this.detectorReadyEventCheckBox.setSelection( this.currentChannel.getDetectorReadyEvent() != null );
 			this.setPartName( channel.getAbstractDevice().getFullIdentifyer() );
 
 			this.averageText.setEnabled( true );
 			this.maxDeviationText.setEnabled( true );
 			this.minimumText.setEnabled( true );
 			this.maxAttemptsText.setEnabled( true );
 			this.confirmTriggerManualCheckBox.setEnabled( true );
 			this.detectorReadyEventCheckBox.setEnabled( true );
 			this.eventsTabFolder.setEnabled(true);
 			
 			this.redoEventComposite.setControlEventManager( this.currentChannel.getRedoControlEventManager() );
 			
 			// TODO von Hartmut: da maxDeviation und minimum keine 
 			// ChannelErrors liefern, muß hier auch keien Abfrage erfolgen!
 			this.maxDeviationErrorLabel.setImage( null );
 			this.minimumErrorLabel.setImage( null );
 		
 			final Iterator< IModelError > it = this.currentChannel.getModelErrors().iterator();
 			while( it.hasNext() ) {
 				final IModelError modelError = it.next();
 				if( modelError instanceof ChannelError ) {
 					final ChannelError channelError = (ChannelError) modelError;
 					if( channelError.getErrorType() == ChannelErrorTypes.MAX_DEVIATION_NOT_POSSIBLE ) {
 						this.maxDeviationErrorLabel.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK ) );
 					} else if( channelError.getErrorType() == ChannelErrorTypes.MINIMUM_NOT_POSSIBLE ) {
 						this.minimumErrorLabel.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK ) );
 					}
 				}
 			}
 			if( this.currentChannel.getRedoControlEventManager().getModelErrors().size() > 0 ) {
 				this.redoEventTabItem.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK ) );
 			} else {
 				this.redoEventTabItem.setImage( null );
 			}
 			this.currentChannel.addModelUpdateListener( this );
 			
 			
 			if (sc.getMinHeight() == 0) {
 				// Wenn das erste Mal die DetectorChannelView für einen Channel aufgerufen wird, gibt es noch
 				// keine Mindesthöhe für die Scrollbar. Die wird hier dann gesetzt.

 				int height = bar.getBounds().y + item0.getHeight() + item0.getHeaderHeight() + 5;
 				int width = bar.getBounds().x + bar.getBounds().width + 5;
 				sc.setMinSize(this.top.computeSize(width, height));
 			}
 			
 		} else {
 			this.averageText.setText( "" );
 			this.maxDeviationText.setText( "" );
 			this.minimumText.setText( "" );
 			this.maxAttemptsText.setText( "" );
 			this.confirmTriggerManualCheckBox.setSelection( false );
 			this.detectorReadyEventCheckBox.setSelection( false );
 			this.eventsTabFolder.setEnabled(false);
 			
 			this.averageText.setEnabled( false );
 			this.maxDeviationText.setEnabled( false );
 			this.minimumText.setEnabled( false );
 			this.maxAttemptsText.setEnabled( false );
 			this.confirmTriggerManualCheckBox.setEnabled( false );
 			this.detectorReadyEventCheckBox.setEnabled( false );
 			this.setPartName( "Detector Channel View" );
 			
 			this.averageErrorLabel.setImage( null );
 			this.maxDeviationErrorLabel.setImage( null );
 			this.minimumErrorLabel.setImage( null );
 			this.maxAttemptsErrorLabel.setImage(null);
 		}
 
 	}
 
 	@Override
 	public void updateEvent(ModelUpdateEvent modelUpdateEvent) {
 		if( this.currentChannel.getRedoControlEventManager().getModelErrors().size() > 0 ) {
 			this.redoEventTabItem.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK ) );
 		} else {
 			this.redoEventTabItem.setImage( null );
 		}
 		
 	}
 
 	///////////////////////////////////////////////////////////
 	// Hier kommen jetzt die verschiedenen Listener Klassen
 	///////////////////////////////////////////////////////////
 
 	/**
 	 * <code>ModifyListener</code> of Average Text from
 	 * <code>DetectorChannelView</code>
 	 */
 	class AverageTextModifyListener implements ModifyListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText( final ModifyEvent e ) {
 			if( currentChannel != null ) {
 				if( averageText.getText().equals( "" ) ) {
 					// average soll immer gefüllt sein, obwohl es im xsd-File
 					// bisher kein Pflichtfeld ist (Hartmut 6.5.10)
 					averageErrorLabel.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK ) );
 					averageErrorLabel.setToolTipText( "The average must not be empty." );
 				} else {
 					try {
 						currentChannel.setAverageCount( Integer.parseInt( averageText.getText() ) );
 						averageErrorLabel.setImage( null );
 						averageErrorLabel.setToolTipText( null );
 					} catch( final NumberFormatException ex ) {
 						averageErrorLabel.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK ) );
 						averageErrorLabel.setToolTipText( "The average must be a integer number." );
 					}
 					catch( final IllegalArgumentException ex ) {
 						averageErrorLabel.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK ) );
 						averageErrorLabel.setToolTipText( ex.getMessage() );
 					}
 				}
 			}
 		}
 	};
 
 	/**
 	 * <code>ModifyListener</code> of MaxDeviation Text from
 	 * <code>DetectorChannelView</code>
 	 */
 	class MaxDeviationTextModifyListener implements ModifyListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText( final ModifyEvent e ) {
 			if( currentChannel != null ) {
 				if( maxDeviationText.getText().equals( "" ) ) {
 					currentChannel.setMaxDeviation( Double.NEGATIVE_INFINITY );
 					maxDeviationErrorLabel.setImage( null );
 					maxDeviationErrorLabel.setToolTipText( null );
 				} else {
 					try {
 						currentChannel.setMaxDeviation( Double.parseDouble( maxDeviationText.getText() ) );
 						maxDeviationErrorLabel.setImage( null );
 						maxDeviationErrorLabel.setToolTipText( null );
 					} catch( final NumberFormatException ex ) {
 						maxDeviationErrorLabel.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK ) );
 						maxDeviationErrorLabel.setToolTipText( "The max deviation must be a floating point number." );
 					}
 				}
 			}
 		}
 	};
 
 	/**
 	 * <code>ModifyListener</code> of Minimum Text from
 	 * <code>DetectorChannelView</code>
 	 */
 	class MinimumTextModifyListener implements ModifyListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText( final ModifyEvent e ) {
 			if( currentChannel != null ) {
 				if( minimumText.getText().equals( "" ) ) {
 					currentChannel.setMinumum( Double.NEGATIVE_INFINITY );
 					minimumErrorLabel.setImage( null );
 					minimumErrorLabel.setToolTipText( null );
 				} else {
 					try {
 						currentChannel.setMinumum( Double.parseDouble( minimumText.getText() ) );
 						minimumErrorLabel.setImage( null );
 						minimumErrorLabel.setToolTipText( null );
 					} catch( final NumberFormatException ex ) {
 						minimumErrorLabel.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK ) );
 						minimumErrorLabel.setToolTipText( "The minimum must be a floating point number." );
 					}
 				}
 			}
 		}
 	};
 
 	/**
 	 * <code>ModifyListener</code> of MaxAttempts Text from
 	 * <code>DetectorChannelView</code>
 	 */
 	class MaxAttemptsTextModifyListener implements ModifyListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText( final ModifyEvent e ) {
 			if( currentChannel != null ) {
 				if( maxAttemptsText.getText().equals( "" ) ) {
 					currentChannel.setMaxAttempts( Integer.MIN_VALUE );
 					maxAttemptsErrorLabel.setImage( null );
 					maxAttemptsErrorLabel.setToolTipText( null );
 				} else {
 					try {
 						currentChannel.setMaxAttempts( Integer.parseInt( maxAttemptsText.getText() ) );
 						maxAttemptsErrorLabel.setImage( null );
 						maxAttemptsErrorLabel.setToolTipText( null );
 					} catch( final NumberFormatException ex ) {
 						maxAttemptsErrorLabel.setImage( PlatformUI.getWorkbench().getSharedImages().getImage( ISharedImages.IMG_OBJS_ERROR_TSK ) );
 						maxAttemptsErrorLabel.setToolTipText( "The max attempts must be a non negativ integer number." );
 					}
 				}
 			}
 		}
 	};
 
 	/**
 	 * <code>SelectionListener</code> of ConfirmTriggerManual CheckBox from
 	 * <code>DetectorChannelView</code>
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
 			if( currentChannel != null ) {
 				currentChannel.setConfirmTrigger( confirmTriggerManualCheckBox.getSelection() );
 			}
 		}
 	};
 
 	/**
 	 * <code>SelectionListener</code> of DetectorReadyEvent CheckBox from
 	 * <code>DetectorChannelView</code>
 	 */
 	class DetectorReadyEventCheckBoxSelectionListener implements SelectionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected( final SelectionEvent e ) {
 			
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected( final SelectionEvent e ) {
 			// we create an event and add it to the list if selected 
 			// or remove the event with same id from the list if deselected
 			Event detReadyEvent = new Event(currentChannel.getAbstractDevice().getID(), currentChannel.getAbstractDevice().getParent().getName(), currentChannel.getAbstractDevice().getName(), currentChannel.getParentScanModul().getChain().getId(), currentChannel.getParentScanModul().getId());
 
 			if( detectorReadyEventCheckBox.getSelection() ) {
 				currentChannel.getParentScanModul().getChain().getScanDescription().add( detReadyEvent );
 				currentChannel.setDetectorReadyEvent(detReadyEvent);
 			} else {
 				currentChannel.getParentScanModul().getChain().getScanDescription().removeEventById( detReadyEvent.getID() );
 				currentChannel.setDetectorReadyEvent(null);
 			}
 		}
 	};
 	
 	/**
 	 * <code>ControlListener</code> of Event Options Bar from
 	 * <code>DetectorChannelView</code>
 	 */
 	class BarControlListener implements ControlListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void controlMoved( final ControlEvent e ) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void controlResized( final ControlEvent e ) {
 			int height = bar.getSize().y - item0.getHeaderHeight() - 20;
 			item0.setHeight( height<200?200:height );
 		}
 	};
 
 	/**
 	 * <code>SelectionListener</code> of EventsTabFolder from
 	 * <code>DetectorChannelView</code>
 	 */
 	class EventsTabFolderSelectionListener implements SelectionListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 			// TODO Auto-generated method stub
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(SelectionEvent e) {
			// TODO Auto-generated method stub
 			// Einträge in der Auswahlliste werden aktualisiert
 			CTabItem wahlItem = eventsTabFolder.getSelection();
 			EventComposite wahlComposite = (EventComposite)wahlItem.getControl();
 			wahlComposite.setEventChoice();
 		}
 	};
 
 	/**
 	 * <code>ControlListener</code> of EventsComposite from
 	 * <code>DetectorChannelView</code>
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
 		
 	};
 	
 }
