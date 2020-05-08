 package de.ptb.epics.eve.viewer.views.devicesview;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 
 import de.ptb.epics.eve.data.measuringstation.AbstractDevice;
 import de.ptb.epics.eve.data.measuringstation.Detector;
 import de.ptb.epics.eve.data.measuringstation.DetectorChannel;
 import de.ptb.epics.eve.data.measuringstation.Device;
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.measuringstation.Motor;
 import de.ptb.epics.eve.data.measuringstation.MotorAxis;
 
 import de.ptb.epics.eve.viewer.Activator;
 import de.ptb.epics.eve.viewer.MessageSource;
 import de.ptb.epics.eve.viewer.messages.MessageTypes;
 import de.ptb.epics.eve.viewer.messages.ViewerMessage;
 import de.ptb.epics.eve.viewer.views.deviceinspectorview.DeviceInspectorView;
 import de.ptb.epics.eve.viewer.views.deviceoptionsview.DeviceOptionsView;
 
 /**
  * <code>DevicesView</code> visualizes available 
  * {@link de.ptb.epics.eve.data.measuringstation.AbstractDevice}s of a 
  * {@link de.ptb.epics.eve.data.measuringstation.MeasuringStation} in a 
  * {@link org.eclipse.jface.viewers.TreeViewer}.
  * 
  * @author ?
  * @author Marcus Michalsky
  */
 public final class DevicesView extends ViewPart {
 
 	/** the unique identifier of this view */
 	public static final String ID = "DevicesView";
 
 	private static Logger logger = Logger.getLogger(DevicesView.class.getName());
 	
 	private IMeasuringStation measuringStation;
 	
 	private TreeViewer treeViewer;
 	private TreeViewerFocusListener treeViewerFocusListener;
 	
 	private DragSource source;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setFocus() {
 	
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createPartControl(final Composite parent) {
 		
 		measuringStation = Activator.getDefault().getMeasuringStation();
 		if(measuringStation == null) {
 			final Label errorLabel = new Label(parent, SWT.NONE);
 			errorLabel.setText("No device description has been loaded. " +
 					"Please check Preferences!");
 			return;
 		}
 				
 		final FillLayout fillLayout = new FillLayout();
 		parent.setLayout(fillLayout);
 		
 		treeViewer = new TreeViewer(parent);
 		treeViewer.setContentProvider(new DevicesViewTreeViewerContentProvider());
 		treeViewer.setLabelProvider(new DevicesViewTreeViewerLabelProvider());
 		treeViewer.getTree().setEnabled(false);
 		
 		// listen to double clicks (inserts the clicked element to the inspector)
 		treeViewer.addDoubleClickListener(new TreeViewerDoubleClickListener());
 		
		//treeViewerFocusListener = new TreeViewerFocusListener();
		//treeViewer.getTree().addFocusListener(treeViewerFocusListener);
 		
 		// listen to selections (triggers the device options view)
 		//treeViewer.getTree().addSelectionListener(
 				//new TreeViewerTreeSelectionListener());
 		
 		// set this tree viewer as a source for drag n drop (drop in inspector)
 		this.source = new DragSource(
 				this.treeViewer.getTree(), DND.DROP_COPY | DND.DROP_MOVE);
 		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
 		source.setTransfer(types);
 		source.addDragListener(new DragSourceDragListener());
 		
 		setMeasuringStation(measuringStation);
 			
 		final MenuManager menuManager = new MenuManager( "#PopupMenu" );
 		menuManager.setRemoveAllWhenShown( true );
 		menuManager.addMenuListener(new TreeViewerTreeMenuManagerMenuListener());
 		final Menu contextMenu = menuManager.createContextMenu(this.treeViewer.getTree());
 		this.treeViewer.getTree().setMenu(contextMenu);
 		
 		getSite().setSelectionProvider(treeViewer);
 	}
 
 	/**
 	 * 
 	 * @param measuringStation
 	 */
 	public void setMeasuringStation(final IMeasuringStation measuringStation) {
 		Activator.getDefault().getMessagesContainer().addMessage(
 				new ViewerMessage(MessageSource.VIEWER, MessageTypes.INFO, 
 						"Got new measuring station description."));
 		this.measuringStation = measuringStation;
 		this.treeViewer.setInput(this.measuringStation);
 		this.treeViewer.getTree().setEnabled(this.measuringStation != null);
 		this.treeViewer.expandAll();
 	}
 	
 	// ***********************************************************************
 	// **************************** Listener *********************************
 	// ***********************************************************************
 	
 	/**
 	 * 
 	 */
 	class TreeViewerDoubleClickListener implements IDoubleClickListener {
 	
 		/**
 		 * {@inheritDoc}
 	 	 */
 		@SuppressWarnings("unchecked")
 		@Override
 		public void doubleClick(DoubleClickEvent event) {
 			
 			if(logger.isDebugEnabled()) {
 				logger.debug("Double Click: " + event.getSelection());
 			}
 			
 			// get all views
 			IViewReference[] ref = getSite().getPage().getViewReferences();
 			
 			DeviceInspectorView deviceInspectorView = null;
 			for(IViewReference ivr : ref) {
 				if(ivr.getId().equals(DeviceInspectorView.ID)) {
 					if(DeviceInspectorView.activeDeviceInspectorView.equals(ivr.getSecondaryId())) {
 						deviceInspectorView = (DeviceInspectorView)ivr.getPart(false);
 					}
 				}
 			}
 			
 			if(deviceInspectorView != null) {
 				Object selection = 
 					treeViewer.getTree().getSelection()[0].getData();
 				
 				if(selection instanceof AbstractDevice) {
 					deviceInspectorView.addAbstractDevice((AbstractDevice)
 							treeViewer.getTree().getSelection()[0].getData());
 				} else if(selection instanceof List<?>) {
 					for(Object o : (List<Object>)selection) {
 						deviceInspectorView.addAbstractDevice((AbstractDevice)o);
 					}
 				} else if(selection instanceof String) {
 					IMeasuringStation measuringstation = 
 						Activator.getDefault().getMeasuringStation();
 					List<AbstractDevice> devices = 
 						measuringstation.getDeviceList((String)selection);
 					for(AbstractDevice d : devices) {
 						deviceInspectorView.addAbstractDevice(d);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 *
 	 */
 	class TreeViewerTreeSelectionListener implements SelectionListener {
 		
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
 			
 			// get all views
 			IViewReference[] ref = getSite().getPage().getViewReferences();
 			
 			DeviceOptionsView deviceOptionsView = null;
 			for(IViewReference ivr : ref) {
 				if(ivr.getId().equals(DeviceOptionsView.ID)) {
 					if(DeviceOptionsView.activeDeviceOptionsView != null && 
 					   DeviceOptionsView.activeDeviceOptionsView.equals(ivr.getSecondaryId())) {
 						deviceOptionsView = (DeviceOptionsView)ivr.getPart(false);
 					}
 				}
 			}
 			
 			if(deviceOptionsView != null) {
 				TreeItem[] items = treeViewer.getTree().getSelection();
 				
 				if(items.length > 0) {
 					if(items[0].getData() instanceof AbstractDevice) {
 						deviceOptionsView.setDevice((AbstractDevice)items[0].getData());
 					}
 				}
 			}
 		}
 	}
 	
 	// ***************************** Menu **********************************
 	
 	/**
 	 * 
 	 */
 	class TreeViewerTreeMenuManagerMenuListener implements IMenuListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void menuAboutToShow(IMenuManager manager) {
 			
 			final IWorkbenchPage page = getSite().getPage();
 			
 			final TreeItem[] selectedItems = treeViewer.getTree().getSelection();
 			if(selectedItems != null && selectedItems.length > 0) {
 				final Action openAction = new Action() {
 					
 					@Override public void run() {
 						super.run();
 						for(final TreeItem item : selectedItems) {
 							if(item.getData() instanceof AbstractDevice) {
 								try {
 									final IViewPart view = page.showView(
 										"DeviceOptionsView", 
 										((AbstractDevice)item.getData()).getName(), 
 										IWorkbenchPage.VIEW_CREATE);
 									((DeviceOptionsView)view).setDevice(
 											(AbstractDevice)item.getData());
 								} catch (PartInitException e) {
 									logger.error(e.getMessage(), e);
 								}
 							}
 							
 						}
 					}
 				};
 				openAction.setText("Open in seperate options window");
 				manager.add(openAction);
 				
 				final MenuManager deviceInspectors = 
 						new MenuManager("Open in Device Inspector");
 				
 				IViewReference[] ref = getSite().getPage().getViewReferences();
 				for(final IViewReference r : ref) {
 					if(r.getId().equals("DeviceInspectorView")) {
 						final Action action = new Action() {
 							
 							@Override public void run() {
 								super.run();
 								for(final TreeItem item : selectedItems) {
 									if(item.getData() instanceof AbstractDevice) {
 										((DeviceInspectorView)r.getPart(true)).
 											addAbstractDevice(
 											(AbstractDevice)item.getData());
 									}
 								}
 							}
 						};
 						action.setText(r.getPartName());
 						deviceInspectors.add(action);
 					}
 				}
 				manager.add(deviceInspectors);
 			}
 		}
 	}
 	
 	// ************************* DnD *****************************************
 	
 	/**
 	 * 
 	 */
 	class DragSourceDragListener implements DragSourceListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void dragStart(final DragSourceEvent event) {
 			
 			event.doit = true;
 			
 			for(TreeItem item : treeViewer.getTree().getSelection()) {
 				if(logger.isDebugEnabled()) {
 					logger.debug(item.getData() + " selected");
 				}
 				
 				// if the selection contains a class (a collection of different
 				// devices) do not allow dragNdrop
 				if(item.getData() instanceof String) {
 					event.doit = false;
 				}
 			}
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@SuppressWarnings("unchecked")
 		@Override
 		public void dragSetData(final DragSourceEvent event) {
 			
 			// provide the data of the requested type
 			
 			if(TextTransfer.getInstance().isSupportedType(event.dataType)) {
 				
 				TreeItem[] items = treeViewer.getTree().getSelection();
 				StringBuffer data = new StringBuffer();
 				int count = 0;
 				
 				// build the string that is transfered to the drop target
 				
 				// add prefixes defining the type of the device
 				for(TreeItem item : items) {
 					if(item.getData() instanceof Motor) {
 						data.append("M" + ((AbstractDevice)item.getData()).
 								getFullIdentifyer());
 					} else if(item.getData() instanceof MotorAxis) {
 						data.append("A" + ((AbstractDevice)item.getData()).
 								getFullIdentifyer());
 					} else if(item.getData() instanceof Detector) {
 						data.append("D" + ((AbstractDevice)item.getData()).
 								getFullIdentifyer());
 					} else if(item.getData() instanceof DetectorChannel) {
 						data.append("C" + ((AbstractDevice)item.getData()).
 								getFullIdentifyer());
 					} else if(item.getData() instanceof Device) {
 						data.append("d" + ((AbstractDevice)item.getData()).
 								getFullIdentifyer());
 					} else if(item.getData() instanceof List<?>) {
 						
 						int countB = 0;
 						
 						for(Object o : (List<Object>)item.getData()) {
 							if(o instanceof Motor) {
 								data.append("M");
 							} else if(o instanceof Detector) {
 								data.append("D");
 							} else if(o instanceof Device) {
 								data.append("d");
 							}
 							data.append(((AbstractDevice)o).
 									getFullIdentifyer());
 							
 							countB++;
 							if(countB != ((List<Object>)item.getData()).size() ||
 									((List<Object>)item.getData()).size() == 1) {
 								data.append(",");
 							}
 						}
 					}
 					
 					count++;
 					if(count != items.length && 
 					   !(item.getData() instanceof List<?>)) {
 						data.append(",");
 					}
 				}
 				
 				if(logger.isDebugEnabled()) {
 					logger.debug("DragSource: " + data.toString());
 				}
 				
 				event.data = data.toString();
 			}
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void dragFinished(DragSourceEvent event) {
 			// if a move operation has been performed -> remove the data 
 			// from the source
 			// ! we only want to copy data -> do nothing
 		}
 	}
 	
 	// ************************************************************************
 	// ************************* Listeners ************************************
 	// ************************************************************************
 	
 	/**
 	 * 
 	 */
 	class TreeViewerFocusListener implements FocusListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusGained(FocusEvent e) {	
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 			treeViewer.getTree().deselectAll();
 		}
 	}
 }
