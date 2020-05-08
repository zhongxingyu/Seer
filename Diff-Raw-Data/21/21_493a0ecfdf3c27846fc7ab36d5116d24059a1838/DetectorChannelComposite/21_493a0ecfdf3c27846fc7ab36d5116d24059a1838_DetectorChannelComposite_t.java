 package de.ptb.epics.eve.editor.views.scanmoduleview;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.PlatformUI;
 
 import de.ptb.epics.eve.data.measuringstation.AbstractDevice;
 import de.ptb.epics.eve.data.measuringstation.Detector;
 import de.ptb.epics.eve.data.measuringstation.DetectorChannel;
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
import de.ptb.epics.eve.data.measuringstation.filter.ExcludeDevicesOfScanModuleFilterManualUpdate;
 import de.ptb.epics.eve.data.scandescription.Channel;
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.editor.views.detectorchannelview.DetectorChannelView;
 
 /**
  * <code>DetectorChannelComposite</code>. is part of the
  * {@link de.ptb.epics.eve.editor.views.scanmoduleview.ScanModuleView}.
  * 
  * @author ?
  * @author Marcus Michalsky
  * @author Hartmut Scherr
  */
 public class DetectorChannelComposite extends Composite {
 
 	private TableViewer tableViewer;
 	private ScanModule scanModule;
 	private MenuManager menuManager;
 	private final IMeasuringStation measuringStation;
 	
 	/**
 	 * Constructs a <code>DetectorChannelComposite</code>.
 	 * 
 	 * @param parent the parent composite
 	 * @param style the style
 	 * @param measuringStation the measuring station (containing available 
 	 * 		  detector channels)
 	 */
 	public DetectorChannelComposite(final Composite parent, final int style, 
 									final IMeasuringStation measuringStation) {
 		super(parent, style);
 		this.measuringStation = measuringStation;
 
 		final GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 2;
 		
 		this.setLayout(gridLayout);
 		
 		GridData gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalSpan = 2;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		
 		this.tableViewer = new TableViewer(this, SWT.NONE);
 		this.tableViewer.getControl().setLayoutData(gridData);
 		
 		TableColumn column = 
 			new TableColumn(this.tableViewer.getTable(), SWT.LEFT, 0);
 	    column.setText("Detector Channel");
 	    column.setWidth(250);
 
 	    column = new TableColumn(this.tableViewer.getTable(), SWT.LEFT, 1);
 	    column.setText("Average");
 	    column.setWidth(80);
 
 	    this.tableViewer.getTable().setHeaderVisible(true);
 	    this.tableViewer.getTable().setLinesVisible(true);
 	    
 	    this.tableViewer.setContentProvider(new DetectorChannelInputWrapper());
 	    this.tableViewer.setLabelProvider(new DetectorChannelLabelProvider());
 	    
 	    final CellEditor[] editors = new CellEditor[2];
 	    
 	    editors[0] = new TextCellEditor(this.tableViewer.getTable());
 	    editors[1] = new TextCellEditor(this.tableViewer.getTable());
 	    
 	    this.tableViewer.setCellEditors(editors);
 	    
 	    final String[] props = {"device", "value"};
 	    
 	    this.tableViewer.setColumnProperties(props);
 
 		this.tableViewer.getTable().addSelectionListener(
 				new TableViewerSelectionListener());
 	    
 		menuManager = new MenuManager("#PopupMenu");
 		
 		menuManager.setRemoveAllWhenShown(true);
 		menuManager.addMenuListener(new MenuManagerMenuListener());
 				    
 		final Menu contextMenu = 
 			menuManager.createContextMenu(this.tableViewer.getTable());
 		this.tableViewer.getControl().setMenu(contextMenu);
 	}
 
 	/**
 	 * Sets the {@link de.ptb.epics.eve.data.scandescription.Channel} of the 
 	 * {@link de.ptb.epics.eve.editor.views.detectorchannelview.DetectorChannelView}.
 	 *
 	 * @param ch the {@link de.ptb.epics.eve.data.scandescription.Channel} 
 	 * 		  that should be set
 	 */
 	public void setDetectorChannelView(Channel ch) {
 
 		// try to find the detector channel view
 		IViewPart detectorChannelView = PlatformUI.getWorkbench().
 		  									 getActiveWorkbenchWindow().
 		  									 getActivePage().
 		  									 findView(DetectorChannelView.ID);
 		if(detectorChannelView != null) {
 			((DetectorChannelView)detectorChannelView).setChannel(ch);
 		}
 	}
 	
 	/**
 	 * Returns the currently set 
 	 * {@link de.ptb.epics.eve.data.scandescription.ScanModule}.
 	 * 
 	 * @return the currently set 
 	 * 		   {@link de.ptb.epics.eve.data.scandescription.ScanModule}
 	 */
 	public ScanModule getScanModule() {
 		return this.scanModule;
 	}
 	
 	/**
 	 * Sets the {@link de.ptb.epics.eve.data.scandescription.ScanModule}.
 	 * 
 	 * @param scanModule the 
 	 * 		  {@link de.ptb.epics.eve.data.scandescription.ScanModule} that 
 	 * 		  should be set
 	 */
 	public void setScanModule(final ScanModule scanModule) {
 
 		if(scanModule != null) {
 			this.tableViewer.getTable().setEnabled(true);
 		} else {
 			this.tableViewer.getTable().setEnabled(false);
 		}
 		this.scanModule = scanModule;
 		this.tableViewer.setInput(scanModule);
 		
 		// if there are motor axis present... 
 		if(tableViewer.getTable().getItems().length > 0)
 		{
 			// ... and none is selected ...
 			if(tableViewer.getTable().getSelectionCount() == 0)
 			{
 				// ... select the first one and set the motor axis view
 				tableViewer.getTable().select(0);
 				setDetectorChannelView((Channel)tableViewer.getTable().getItem(0).getData());
 			} else {
 				// .. set the motor axis view
 				setDetectorChannelView((Channel)tableViewer.getTable().getSelection()[0].getData());
 			}
 		} else {
 			setDetectorChannelView(null);
 		}
 		
 		if(scanModule == null || tableViewer.getTable().getSelectionCount() == 0)
 			setDetectorChannelView(null);
 	}
 	
 	// ************************************************************************
 	// **************************** Listeners *********************************
 	// ************************************************************************
 	
 	/**
 	 * 
 	 */
 	class TableViewerSelectionListener implements SelectionListener {
 		
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
 			final String channelName = 
 				tableViewer.getTable().getSelection()[0].getText(0);
 			Channel[] channels = scanModule.getChannels();
 			for(int i = 0; i < channels.length; ++i) {
 				if(channels[i].getDetectorChannel().
 						getFullIdentifyer().equals(channelName)) {
 					setDetectorChannelView(channels[i]);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	class MenuManagerMenuListener implements IMenuListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void menuAboutToShow(IMenuManager manager) {
 			
 			final ImageDescriptor classImage = ImageDescriptor.createFromImage(
 					de.ptb.epics.eve.viewer.Activator.getDefault().
 					getImageRegistry().get("CLASS"));
 			final ImageDescriptor detectorImage = ImageDescriptor.createFromImage(
 					de.ptb.epics.eve.viewer.Activator.getDefault().
 					getImageRegistry().get("DETECTOR"));
 			final ImageDescriptor channelImage = ImageDescriptor.createFromImage(
 					de.ptb.epics.eve.viewer.Activator.getDefault().
 					getImageRegistry().get("CHANNEL"));

			((ExcludeDevicesOfScanModuleFilterManualUpdate)measuringStation).update();

 			for(final String className : measuringStation.getClassNameList()) {
 				
 				boolean containsAtLeastOne = false;
 				final MenuManager currentClassMenu = new MenuManager(
 						className, classImage, className);
 				
 				for(final AbstractDevice device : 
 					measuringStation.getDeviceList(className)) {
 				
 					if(device instanceof Detector) {
 						containsAtLeastOne = true;
 						final Detector detector = (Detector)device;
 						final MenuManager currentDetectorMenu = 
 							new MenuManager(detector.getName(), detectorImage, detector.getName());
 						currentClassMenu.add(currentDetectorMenu);
 
 						// iterate for each channel of the detector
 						for(final DetectorChannel channel : detector.getChannels()) {
 							if (channel.getClassName().isEmpty()) {
 								// add only channels which have no className
 								final Action setChannelAction = new SetChannelAction(channel);
 								setChannelAction.setImageDescriptor(channelImage);
 								containsAtLeastOne = true;
 								currentDetectorMenu.add(setChannelAction);
 							}
 						}
 					} else if(device instanceof DetectorChannel) {
 						containsAtLeastOne = true;	
 						final Action setChannelAction = new SetChannelAction((DetectorChannel)device);
 						setChannelAction.setImageDescriptor(channelImage);
 						containsAtLeastOne = true;
 						currentClassMenu.add(setChannelAction);
 					}
 					if(containsAtLeastOne) {
 						manager.add(currentClassMenu);
 					} 
 				}
 			}
 				
 			for(final Detector detector : measuringStation.getDetectors()) {
 				if("".equals(detector.getClassName()) || detector.getClassName() == null) {
 					boolean containsAtLeastOne = false;
 					final MenuManager currentDetectorMenu = 
 							new MenuManager(detector.getName(), detectorImage, detector.getName());
 					for(final DetectorChannel channel : 
 						detector.getChannels()) {
 							if("".equals(channel.getClassName()) || 
 							   channel.getClassName() == null) {
 							final Action setChannelAction = 
 								new SetChannelAction(channel);
 							setChannelAction.setImageDescriptor(channelImage);
 							containsAtLeastOne = true;
 							currentDetectorMenu.add( setChannelAction );
 						}
 					}
 					if(containsAtLeastOne) {
 						manager.add(currentDetectorMenu);
 					} 
 				}
 			}
 
 			if (scanModule.getChannels().length > 0) {
 				Action deleteAction = new DeleteAction();
 				deleteAction.setEnabled(true);
 				deleteAction.setText("Delete Channel");
 				deleteAction.setToolTipText("Deletes Channel");
 				deleteAction.setImageDescriptor(PlatformUI.getWorkbench().
 				   								getSharedImages().
 				   								getImageDescriptor(
 				   								ISharedImages.IMG_TOOL_DELETE));
 				manager.add(deleteAction);	
 			}
 			
 		}
 	}
 	
 	// ****************************** Actions *********************************
 	
 	/**
 	 * 
 	 */
 	class SetChannelAction extends Action {
 		
 		final DetectorChannel ch;
 		
 		SetChannelAction(DetectorChannel ch)
 		{
 			this.ch = ch;
 			this.setText("".equals(ch.getName())
 						 ? ch.getID()
 						 : ch.getName());
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void run() {
 			super.run();
 			for(final Channel c : scanModule.getChannels()) {
 				if(c.getAbstractDevice() == ch) {
 					return;
 				}
 			}
 			Channel c = new Channel(scanModule);
 			c.setDetectorChannel(ch);
 			scanModule.add(c);
 			setDetectorChannelView(c);
 			tableViewer.refresh();
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	class DeleteAction extends Action {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void run() {
     		
     		scanModule.remove((Channel)((IStructuredSelection)
     				tableViewer.getSelection()).getFirstElement());
 
     		tableViewer.refresh();
     	}
 	}
 }
