 package org.dawnsci.plotting.tools.diffraction;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.measure.quantity.Quantity;
 import javax.swing.tree.TreeNode;
 
 import org.dawb.common.services.ILoaderService;
 import org.dawb.common.ui.menu.CheckableActionGroup;
 import org.dawb.common.ui.menu.MenuAction;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.region.IROIListener;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.ROIEvent;
 import org.dawb.common.ui.plot.region.RegionEvent;
 import org.dawb.common.ui.plot.region.RegionUtils;
 import org.dawb.common.ui.plot.tool.AbstractToolPage;
 import org.dawb.common.ui.plot.tool.IToolPage;
 import org.dawb.common.ui.plot.tool.IToolPageSystem;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.IPaletteListener;
 import org.dawb.common.ui.plot.trace.ITraceListener;
 import org.dawb.common.ui.plot.trace.PaletteEvent;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawb.common.ui.viewers.TreeNodeContentProvider;
 import org.dawnsci.common.widgets.tree.ClearableFilteredTree;
 import org.dawnsci.common.widgets.tree.DelegatingProviderWithTooltip;
 import org.dawnsci.common.widgets.tree.IResettableExpansion;
 import org.dawnsci.common.widgets.tree.NodeFilter;
 import org.dawnsci.common.widgets.tree.NodeLabelProvider;
 import org.dawnsci.common.widgets.tree.NumericNode;
 import org.dawnsci.common.widgets.tree.UnitEditingSupport;
 import org.dawnsci.common.widgets.tree.ValueEditingSupport;
 import org.dawnsci.plotting.Activator;
 import org.dawnsci.plotting.preference.diffraction.DiffractionPreferencePage;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.JFacePreferences;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.resource.ColorRegistry;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.TreeViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectedListener;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectionEvent;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.utils.BeamCenterRefinement;
 import uk.ac.diamond.scisoft.analysis.roi.CircularFitROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 
 
 public class DiffractionTool extends AbstractToolPage implements CalibrantSelectedListener, IResettableExpansion {
 
 	private static final Logger logger = LoggerFactory.getLogger(DiffractionTool.class);
 	
 	private ClearableFilteredTree filteredTree;
 	private TreeViewer      viewer;
 	private Composite       control;
 	private DiffractionTreeModel model;
 	private ILoaderService  service;
 	
 	private static DiffractionTool      activeDiffractionTool=null;
 	
 	//Region and region listener added for 1-click beam centring
 	private IRegion               tmpRegion;
 	private IRegionListener       regionListener;
 	private IPaletteListener.Stub paletteListener;
 	private ITraceListener.Stub   traceListener;
 	private IROIListener roiListener;
 	
 	protected DiffractionImageAugmenter augmenter;
 	
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 	
 	
 	public DiffractionTool() {
 		super();
 		
         this.paletteListener = new IPaletteListener.Stub() {
         	protected void updateEvent(PaletteEvent evt) {
         		updateIntensity();
         	}
         };
 
 		this.traceListener = new ITraceListener.Stub() {
 			protected void update(TraceEvent evt) {
 				if (getImageTrace()!=null) getImageTrace().addPaletteListener(paletteListener);
 				if (getImageTrace()!=null) createDiffractionModel(true);
 				updateIntensity();
 			}
 		};
 		
 		this.service = (ILoaderService)PlatformUI.getWorkbench().getService(ILoaderService.class);
       
 	}
 
 	protected void updateIntensity() {
 		try {
 			if (model==null) return;
 			model.setIntensityValues(getImageTrace());
 			if (!(viewer.getControl().isDisposed())) viewer.refresh();
 		} catch (Exception e) {
 			logger.error("Updating intensity values!", e);
 		}
 	}
 
 	@Override
 	public void createControl(final Composite parent) {
 		
 		this.control = new Composite(parent, SWT.NONE);
 		control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 		control.setLayout(new GridLayout(1, false));
 		GridUtils.removeMargins(control);
 	
 		this.filteredTree = new ClearableFilteredTree(control, SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER, new NodeFilter(this), true);		
 		viewer = filteredTree.getViewer();
 		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		createColumns(viewer);
 		viewer.setContentProvider(new TreeNodeContentProvider()); // Swing tree nodes
 		viewer.getTree().setLinesVisible(true);
 		viewer.getTree().setHeaderVisible(true);
 		
 		final Label label = new Label(control, SWT.NONE);
 		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
 		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
 		label.setForeground(new Color(label.getDisplay(), colorRegistry.getRGB(JFacePreferences.QUALIFIER_COLOR)));
 		label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 		label.setText("* Click to change value  ");
 		
 		createDiffractionModel(false);
 		createActions();
 		createListeners();
 
 	}
 	
 	public void activate() {
 		super.activate();
 		createDiffractionModel(true);
 		
 		IPlottingSystem plotting = getPlottingSystem();
 		if (plotting != null) {
 			if (regionListener != null)
 				plotting.addRegionListener(regionListener);
 			if (traceListener != null)
 				plotting.addTraceListener(traceListener);
 			if (tmpRegion != null)
 				plotting.addRegion(tmpRegion);
 		}
 
 		if (augmenter!=null) augmenter.activate();
 		CalibrationFactory.addCalibrantSelectionListener(this);
 		activeDiffractionTool = this;
 			
 		final IDiffractionMetadata dmd = getDiffractionMetaData();
 		if (viewer!=null && viewer.getInput()!=null && model!=null && dmd!=null && dmd.getDetector2DProperties()!=null && dmd.getOriginalDiffractionCrystalEnvironment()!=null) {
 			try {
 			    viewer.refresh();
 			} catch (Throwable ne) {
 				// Sometimes model could not be resolved at this point.
 			}
 		}
 	}
 	
 	public void deactivate() {
 		
 		if (!isActive()) {return;}
 		
 		super.deactivate();
 		IPlottingSystem plotting = getPlottingSystem();
 		if (plotting != null) {
 			plotting.removeRegionListener(regionListener);
 			if (traceListener != null)
 				plotting.removeTraceListener(traceListener);
 			if (tmpRegion != null)
 				plotting.removeRegion(tmpRegion);
 		}
 
 		CalibrationFactory.removeCalibrantSelectionListener(this);
 		if (augmenter!=null) augmenter.deactivate();
 		if (activeDiffractionTool==this) activeDiffractionTool = null;
 		if (model!=null) model.deactivate();
 	}
 	
 	public void dispose() {
 		super.dispose();
 		if (model!=null) model.dispose();
 		if (augmenter != null) augmenter.dispose();
 	}
 
 	private void createDiffractionModel(boolean force) {
 		
 		if (!force && model!=null)  return;
 		if (force && model!=null) {
 			model.dispose();
 			model= null;
 		}
 		if (viewer==null)           return;
 		
 		IDiffractionMetadata data=null;
 		try {
 			data  = getDiffractionMetaData();
 			if (data==null || data.getOriginalDetector2DProperties()==null || data.getDiffractionCrystalEnvironment()==null) {
 				return;
 			}
 			model = new DiffractionTreeModel(data);
 			model.setViewer(viewer);
 			model.activate();
 			if (augmenter != null) {
 				augmenter.setDiffractionMetadata(data);
 			}
 		} catch (Exception e) {
 			logger.error("Cannot create model!", e);
 			return;
 		}
 			
 		viewer.setInput(model.getRoot());
 		model.activate();
 		
         resetExpansion();
 		getSite().setSelectionProvider(viewer);
 
 	}
 	
 	public void resetExpansion() {
 		try {
 			if (model == null) return;
 			final List<?> top = model.getRoot().getChildren();
 			for (Object element : top) {
 			   filteredTree.expand(element);
 			}
 		} catch (Throwable ne) {
 			// intentionally silent
 		}
 	}
 	
 	private boolean diffractionMetadataAreEqual(IDiffractionMetadata meta1,IDiffractionMetadata meta2) {
 		
 		if (meta1.getDetector2DProperties().equals(meta2.getDetector2DProperties()) &&
 				meta1.getDiffractionCrystalEnvironment().equals(meta2.getDiffractionCrystalEnvironment())) {
 			return true;
 		}
 		
 		return false;
 		
 	}
 	
 	private IDiffractionMetadata getDiffractionMetaData() {
 		// Now always returns IDiffractionMetadata to prevent creation of a new
 		// metadata object after listeners have been added to the old metadata
 		
 		IDiffractionMetadata lockedMeta = service.getLockedDiffractionMetaData();
 		
 		if (lockedMeta != null) {
 			
    		    IImageTrace imageTrace = getImageTrace();
 			if (imageTrace==null) return lockedMeta;
 			
 			IMetaData mdImage = imageTrace.getData().getMetadata();
 			
 			if (mdImage == null) {
 				imageTrace.getData().setMetadata(lockedMeta.clone());
 			} else if (!(mdImage instanceof IDiffractionMetadata)) {
 				IDiffractionMetadata idm = DiffractionDefaultMetadata.getDiffractionMetadata(imageTrace.getData().getShape(),mdImage);
 				DiffractionDefaultMetadata.copyNewOverOld(lockedMeta, idm);
 				imageTrace.getData().setMetadata(idm);
 			} else if (mdImage instanceof IDiffractionMetadata) {
 				if (!diffractionMetadataAreEqual((IDiffractionMetadata)mdImage,lockedMeta)) {
 					DiffractionDefaultMetadata.copyNewOverOld(lockedMeta, (IDiffractionMetadata)mdImage);
 					imageTrace.getData().setMetadata(mdImage);
 				}
 			}
 			return lockedMeta;
 			
 		}
 		
 		
 		//If not see if the trace has diffraction meta data
 		IImageTrace imageTrace = getImageTrace();
 		if (imageTrace==null) return null;
 		IMetaData mdImage = imageTrace.getData().getMetadata();
 		
 		int[] imageShape = imageTrace.getData().getShape();
 		
 		if (mdImage !=null && mdImage  instanceof IDiffractionMetadata) return (IDiffractionMetadata)mdImage;
 		
 		// if it is null try and get it from the loader service
 		if (mdImage == null) {
 			
 			IMetaData md = null;
 			if (getPart() instanceof IEditorPart) {
 				try {
 					md = service.getMetaData(EclipseUtils.getFilePath(((IEditorPart)getPart()).getEditorInput()), null);
 					
 				} catch (Exception e) {
 					logger.error("Cannot read meta data from "+getPart().getTitle(), e);
 				}
 			}
 			
 			// If it is there and diffraction data return it
 			if (md!=null && md instanceof IDiffractionMetadata) return (IDiffractionMetadata)md;
 			
 			if (md != null)
 				mdImage = md;
 		}
 		
 		//if the file contains IMetaData and is hdf5 try and pull some meta data from it.
 		if (mdImage!=null) {
 			if (getPart() instanceof IEditorPart) {
 				IDiffractionMetadata difMet = NexusDiffractionMetaCreator.diffractionMetadataFromNexus(EclipseUtils.getFilePath(((IEditorPart)getPart()).getEditorInput()),
 						mdImage,imageShape);
 				if (difMet !=null) {
 					imageTrace.getData().setMetadata(difMet);
 					return difMet;
 				}
 			}
 		}
 		
 		//if the file contains IMetaData but not IDiffraction meta data, wrap the old meta in a 
 		// new IDiffractionMetadata object and put it back in the dataset
 		if (mdImage!=null) {
 			mdImage = DiffractionDefaultMetadata.getDiffractionMetadata(imageTrace.getData().getShape(),mdImage);
 			imageTrace.getData().setMetadata(mdImage);
 			return (IDiffractionMetadata)mdImage;
 		}
 		
 		// if there is no meta create default IDiff and put it in the dataset
 		mdImage = DiffractionDefaultMetadata.getDiffractionMetadata(imageTrace.getData().getShape());
 		imageTrace.getData().setMetadata(mdImage);
 //		}
 		
 		return (IDiffractionMetadata)mdImage;
 	}
 
 	private TreeViewerColumn defaultColumn;
 	
 	private void createColumns(TreeViewer viewer) {
 				
 		viewer.setColumnProperties(new String[] { "Name", "Original", "Value", "Unit" });
 		ColumnViewerToolTipSupport.enableFor(viewer);
 
 		TreeViewerColumn var = new TreeViewerColumn(viewer, SWT.LEFT, 0);
 		var.getColumn().setText("Name"); // Selected
 		var.getColumn().setWidth(260);
 		var.setLabelProvider(new NodeLabelProvider(0));
 		
 		var = new TreeViewerColumn(viewer, SWT.LEFT, 1);
 		var.getColumn().setText("Original"); // Selected
 		var.getColumn().setWidth(0);
 		var.getColumn().setResizable(false);
 		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(1)));
 		defaultColumn = var;
 		
 		var = new TreeViewerColumn(viewer, SWT.LEFT, 2);
 		var.getColumn().setText("Value"); // Selected
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(2)));
 		var.setEditingSupport(new ValueEditingSupport(viewer));
 
 		var = new TreeViewerColumn(viewer, SWT.LEFT, 3);
 		var.getColumn().setText("Unit"); // Selected
 		var.getColumn().setWidth(90);
 		var.setLabelProvider(new DelegatingProviderWithTooltip(new NodeLabelProvider(3)));
 		var.setEditingSupport(new UnitEditingSupport(viewer));
 	}
 	
 	private TreeNode   copiedNode;
 	private MenuAction calibrantActions;
 	private Action     calPref;
 	private static Action lock;
 	
 	private void createActions() {
 		
 		final IToolBarManager toolMan = getSite().getActionBars().getToolBarManager();
 		final MenuManager     menuMan = new MenuManager();
 		
 		final Action showDefault = new Action("Show the original/default value column", Activator.getImageDescriptor("icons/plot-tool-diffraction-default.gif")) {
 			public void run() {
 				defaultColumn.getColumn().setWidth(isChecked()?80:0);
 				defaultColumn.getColumn().setResizable(!isChecked());
 			}
 		};
 		showDefault.setChecked(false);
 		
 		final Action reset = new Action("Reset selected field", Activator.getImageDescriptor("icons/reset.gif")) {
 			@Override
 			public void run() {
 				final TreeNode node = (TreeNode)((StructuredSelection)viewer.getSelection()).getFirstElement();
 				if (node instanceof NumericNode) {
 					((NumericNode<?>)node).reset();
 					viewer.refresh(node);
 				}
 			}
 		};
 		final Action resetAll = new Action("Reset all fields", Activator.getImageDescriptor("icons/reset_red.png")) {
 			@Override
 			public void run() {
 				
 				boolean ok = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Confirm Reset All", "Are you sure that you would like to reset all values?");
 				if (!ok) return;
 				filteredTree.clearText();
 				if (service.getLockedDiffractionMetaData()!=null) {
 					model.reset();
 					viewer.refresh();
 			        resetExpansion();
 				} else {
 					createDiffractionModel(true);
 					model.reset();
 				}
 			}
 		};
 		
 		final Action copy = new Action("Copy value", Activator.getImageDescriptor("icons/copy.gif")) {
 			@Override
 			public void run() {
 				copiedNode = (TreeNode)((StructuredSelection)viewer.getSelection()).getFirstElement();
 			}
 		};
 
 		final Action paste = new Action("Paste value", Activator.getImageDescriptor("icons/paste.gif")) {
 			@SuppressWarnings("unchecked")
 			@Override
 			public void run() {
 				if (copiedNode!=null) {
 					Object object = ((StructuredSelection)viewer.getSelection()).getFirstElement();
 					if (object instanceof NumericNode) {
 						NumericNode<Quantity> nn = (NumericNode<Quantity>)object;
 						if (!nn.isEditable()) {
 							MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Cannot paste", "The item '"+nn.getLabel()+"' is not writable.\n\nPlease choose a different value to paste to.");
 							return;
 						}
 						
 						try {
 						    nn.mergeValue(copiedNode);
 						} catch (Throwable e) {
 							try {
 								if (EclipseUtils.getActivePage().findView("org.eclipse.pde.runtime.LogView")==null) {
 								    EclipseUtils.getActivePage().showView("org.eclipse.pde.runtime.LogView");
 								}
 							} catch (PartInitException pe) {
 								// Ignored.
 							}
 							IStatus status = new Status(IStatus.INFO, Activator.PLUGIN_ID, "Cannot past into '"+nn.getLabel()+"'", e);
 							Activator.getDefault().getLog().log(status);
 						}
 						viewer.refresh(object);
 					}
 				}
 			}
 		};
 		
 		final Action centre = new Action("One-click beam centre", IAction.AS_PUSH_BUTTON) {
 			@Override
 			public void run() {
 				logger.debug("1-click clicked");
 				
 				try {
 					if (tmpRegion != null) {
 						getPlottingSystem().removeRegion(tmpRegion);
 					}
 					tmpRegion = getPlottingSystem().createRegion(RegionUtils.getUniqueName("BeamCentrePicker", getPlottingSystem()), IRegion.RegionType.POINT);
 					tmpRegion.setUserRegion(false);
 					tmpRegion.setVisible(false);
 					
 				} catch (Exception e) {
 					logger.error("Cannot add beam center", e);
 				}
 
 			}
 		};
 		centre.setImageDescriptor(Activator.getImageDescriptor("icons/centre.png"));
 		
 		final Action cCentre = new Action("Circle-based beam centre", IAction.AS_PUSH_BUTTON) {
 			@Override
 			public void run() {
 				logger.debug("Circling centre clicked");
 				
 				try {
 					if (tmpRegion != null) {
 						getPlottingSystem().removeRegion(tmpRegion);
 					}
 					tmpRegion = getPlottingSystem().createRegion(RegionUtils.getUniqueName("BeamCentrePicker", getPlottingSystem()), IRegion.RegionType.CIRCLEFIT);
 					tmpRegion.setUserRegion(false);
 					tmpRegion.addROIListener(roiListener);
 				} catch (Exception e) {
 					logger.error("Cannot add beam center", e);
 				}
 
 			}
 		};
 		cCentre.setImageDescriptor(Activator.getImageDescriptor("icons/centre.png"));
 
 		final Action refine = new Action("Refine beam center", IAction.AS_PUSH_BUTTON) {
 			
 			class Compare implements Comparator<IPeak> {
 
 				@Override
 				public int compare(IPeak o1, IPeak o2) {
 					if (o1.getPosition() > o2.getPosition()) {
 						return 1;
 					}
 					if (o1.getPosition() < o2.getPosition()) {
 						return -1;
 					}
 					return 0;
 				}
 
 			}
 			
 			
 			@SuppressWarnings("unchecked")
 			private List<IPeak> loadPeaks() {
 				IToolPage radialTool = getToolSystem().getToolPage(
 						"org.dawb.workbench.plotting.tools.radialProfileTool");
 				IToolPage fittingTool = ((IToolPageSystem)radialTool.getToolPlottingSystem()).getToolPage(
 						"org.dawb.workbench.plotting.tools.fittingTool");
 				if (fittingTool != null) {
 					List<IPeak> fittedPeaks = (List<IPeak>) fittingTool.getAdapter(IPeak.class);
 
 					if (fittedPeaks != null) {
 						Collections.sort(fittedPeaks, new Compare());
 
 						ArrayList<IPeak> peaks = new ArrayList<IPeak>(fittedPeaks.size());
 						if (peaks != null && peaks.size() > 0)
 							peaks.clear();
 						for (IPeak peak : fittedPeaks) {
 							peaks.add(peak);
 						}
 						return peaks;
 					}
 				}
 				
 				return null;
 			}
 
 			@Override
 			public void run() {
 				try {
 					AbstractDataset dataset = getImageTrace().getData();
 					AbstractDataset mask = getImageTrace().getMask();
 					SectorROI sroi = (SectorROI) getPlottingSystem().getRegions(RegionType.SECTOR).iterator().next().getROI();
 					final BeamCenterRefinement beamOffset = new BeamCenterRefinement(dataset, mask, sroi);
 					List<IPeak> peaks = loadPeaks();
 					if (peaks==null) throw new Exception("Cannot find peaks!");
 					beamOffset.setInitPeaks(peaks);
 					
 					beamOffset.optimize(sroi.getPoint());
 				} catch (Throwable ne) {
 					
 					/**
 					 * Long discussion with Iralki on this. The algorithm must be set up in a particular way to 
 					 * run at the moment. 
 					 */
 					ConfigurableMessageDialog dialog = new ConfigurableMessageDialog(Display.getDefault().getActiveShell(),
 							"Experimental Refinement Algorithm Uncomplete",
 							null,
 							"Could not read peak positons to start refinement.\nThere is a process to set up the refinement because it is in an experimental form at the moment:\n\n"+
 							"1. Open the 'Diffraction' tool in a dedicated view (action on the right of the toolbar).\n"+
 							"2. Open the 'Radial Profile' tool (from the plot containing the image).\n" +
 							"3. Select a sector which bisects the rings wanted.\n"+
 							"4. In this 'Radial Profile' tool select peak fitting.\n"+
 							"5. Set up a peak fit on all the rings which the redial profile found.\n"+
 							"6. Now run the refine action in the diffraction tool again.\n\n"+
 							"Please note that the algorithm may not converge. A job is run for the refinement which may be stopped.\n"+
 							"Please contact your support representative for more training/help with refinement.\n\n"+
 							"(NOTE This dialog can be kept open as a guide while doing the proceedure.)",
 							MessageDialog.INFORMATION,
 							new String[]{IDialogConstants.OK_LABEL},
 							0);
 					dialog.setShellStyle(SWT.SHELL_TRIM|SWT.MODELESS);
 					dialog.open();
 				}
 			}
 		};
 		refine.setImageDescriptor(Activator.getImageDescriptor("icons/refine.png"));
 		
 		if (lock==null) lock = new Action("Lock the diffraction data and apply it to newly opened files.",IAction.AS_CHECK_BOX) {
 		    @Override
 			public void run() {
 		    	if (isChecked()) {
 		    		IDiffractionMetadata data = activeDiffractionTool.getDiffractionMetaData().clone();
 		    		service.setLockedDiffractionMetaData(data);
 		    	} else {
 		    		service.setLockedDiffractionMetaData(null);
 		    	}
 		    	activeDiffractionTool.createDiffractionModel(true);
 			}
 		};
 		lock.setImageDescriptor(Activator.getImageDescriptor("icons/lock.png"));
 
 	
 		this.calPref = new Action("Configure Calibrants...") {
 			@Override
 			public void run() {
 				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), DiffractionPreferencePage.ID, null, null);
 				if (pref != null) pref.open();
 			}
 		};
 
 		this.calibrantActions = new MenuAction("Calibrants");
 		calibrantActions.setImageDescriptor(Activator.getImageDescriptor("/icons/calibrant_rings.png"));
 		updateCalibrationActions(CalibrationFactory.getCalibrationStandards());		
 		
 
 		MenuAction dropdown = new MenuAction("Resolution rings");
 	    dropdown.setImageDescriptor(Activator.getImageDescriptor("/icons/resolution_rings.png"));
 
 		augmenter = new DiffractionImageAugmenter((AbstractPlottingSystem)getPlottingSystem());
 	    augmenter.addActions(dropdown);
 		
 	    toolMan.add(lock);
 		toolMan.add(new Separator());
 	    toolMan.add(dropdown);
 	    toolMan.add(calibrantActions);
 		toolMan.add(new Separator());
 		toolMan.add(centre);
 		toolMan.add(cCentre);
 		toolMan.add(refine);
 		toolMan.add(new Separator());
 		toolMan.add(reset);
 		toolMan.add(resetAll);
 		toolMan.add(new Separator());
 		toolMan.add(showDefault);
 		toolMan.add(new Separator());
 		
 		menuMan.add(dropdown);
 	    menuMan.add(centre);
 	    menuMan.add(cCentre);
 	    menuMan.add(refine);
 		menuMan.add(new Separator());
 		menuMan.add(reset);
 		menuMan.add(resetAll);
 		menuMan.add(new Separator());
 		menuMan.add(copy);
 		menuMan.add(paste);
 		menuMan.add(new Separator());
 		menuMan.add(showDefault);
 		menuMan.add(new Separator());
 		menuMan.add(calPref);
 		
 		final Menu menu = menuMan.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 
 		getSite().getActionBars().getMenuManager().add(new Separator());
 		getSite().getActionBars().getMenuManager().add(calPref);
 		getSite().getActionBars().getMenuManager().add(new Separator());
 	}
 	
 	private void updateCalibrationActions(final CalibrationStandards standards) {
 		this.calibrantActions.clear();
 		final String selected = standards.getSelectedCalibrant();
 		final CheckableActionGroup grp = new CheckableActionGroup();
 		Action selectedAction=null;
 		for (final String calibrant : standards.getCalibrantList()) {
 			final Action calibrantAction = new Action(calibrant, IAction.AS_CHECK_BOX) {
 				public void run() {
 					standards.setSelectedCalibrant(calibrant, true);
 				}
 			};
 			grp.add(calibrantAction);
 			if (selected!=null&&selected.equals(calibrant)) selectedAction = calibrantAction;
 			calibrantActions.add(calibrantAction);
 		}
 		calibrantActions.addSeparator();
 		calibrantActions.add(calPref);
 		if (selected!=null) selectedAction.setChecked(true);
 	}
 
 	private void createListeners() {
 		
 		this.regionListener = new IRegionListener.Stub() {
 			@Override
 			public void regionAdded(RegionEvent evt) {
 				//test if our region
 				if (evt.getRegion() == tmpRegion) {
 					logger.debug("1-Click region added (type: {})", tmpRegion.getRegionType());
 					double[] point = tmpRegion.getROI().getPointRef();
 					logger.debug("Clicked here X: {} Y : {}", point[0], point[1]);
 
 					if (tmpRegion.getRegionType() == RegionType.POINT)
 						getPlottingSystem().removeRegion(tmpRegion);
 					IDiffractionMetadata data = getDiffractionMetaData();
 					DetectorProperties detprop = data.getDetector2DProperties();
 					detprop.setBeamCentreCoords(point);
 					if (!augmenter.isShowingBeamCenter()) {
 						augmenter.drawBeamCentre(true);
 					}
 				}
 			}
 		};
 
 		roiListener = new IROIListener.Stub() {
 			@Override
 			public void update(ROIEvent evt) {
 				ROIBase r = evt.getROI();
 				if (r instanceof CircularFitROI) {
 					double[] point = r.getPointRef();
 					logger.debug("ROI moved here X: {} Y : {}", point[0], point[1]);
 					IDiffractionMetadata data = getDiffractionMetaData();
 					DetectorProperties detprop = data.getDetector2DProperties();
 					detprop.setBeamCentreCoords(point);
 					if (!augmenter.isShowingBeamCenter()) {
 						augmenter.drawBeamCentre(true);
 					}
 				}
 			}
 		};
 	}
 
 
 	@Override
 	public Control getControl() {
 		return control;
 	}
 
 	@Override
 	public void setFocus() {
		if (viewer!=null && !viewer.getControl().isDisposed()) viewer.getControl().setFocus();
 	}
 
 	@Override
 	public void calibrantSelectionChanged(CalibrantSelectionEvent evt) {
 		updateCalibrationActions((CalibrationStandards)evt.getSource());
 	}
 
 }
