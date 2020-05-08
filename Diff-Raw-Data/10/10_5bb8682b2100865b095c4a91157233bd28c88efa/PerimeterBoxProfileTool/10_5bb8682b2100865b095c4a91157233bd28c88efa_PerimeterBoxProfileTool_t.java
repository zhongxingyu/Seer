 package org.dawnsci.plotting.tools.profile;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.widgets.ROIWidget;
 import org.dawnsci.plotting.Activator;
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.region.IROIListener;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.dawnsci.plotting.api.region.IRegionListener;
 import org.dawnsci.plotting.api.region.ROIEvent;
 import org.dawnsci.plotting.api.region.RegionEvent;
 import org.dawnsci.plotting.api.region.RegionUtils;
 import org.dawnsci.plotting.api.tool.AbstractToolPage;
 import org.dawnsci.plotting.api.tool.IProfileToolPage;
 import org.dawnsci.plotting.api.tool.IToolPageSystem;
 import org.dawnsci.plotting.api.tool.ToolPageFactory;
 import org.dawnsci.plotting.api.trace.ITrace;
 import org.dawnsci.plotting.tools.RegionSumTool;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.part.IPageSite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 
 /**
  * Perimeter Box tool that creates a tool page with 3 plotting systems and a composite:<br>
  * - a zoom profile<br>
  * - an XY plot that shows the line profiles of the vertical edges of the box<br>
  * - an XY plot that show the line profiles of the horizontal edges of the box<br>
  * - a composite with table viewers used to display and edit the box X/Y points and width/height,<br>
  *    as well as a canvas where the sum of the ROI is shown.<br>
  * 
  * @author wqk87977
  *
  */
 public class PerimeterBoxProfileTool extends AbstractToolPage  implements IROIListener {
 
 	private final static Logger logger = LoggerFactory.getLogger(PerimeterBoxProfileTool.class);
 	
 	private AbstractPlottingSystem zoomProfilePlottingSystem;
 	private AbstractPlottingSystem verticalProfilePlottingSystem;
 	private AbstractPlottingSystem horizontalProfilePlottingSystem;
 	private IRegionListener        regionListener;
 	private Map<String,Collection<ITrace>> registeredTraces;
 
 	private ROIWidget myROIWidget;
 	private ROIWidget verticalProfileROIWidget;
 	private ROIWidget horizontalProfileROIWidget;
 
 	private Composite profileContentComposite;
 	private IRegion region;
 
 	private IProfileToolPage sideProfile1;
 	private IProfileToolPage sideProfile2;
 	private ProfileTool zoomProfile;
 	private RegionSumTool roiSumProfile;
 
 	public PerimeterBoxProfileTool() {
 		
 		this.registeredTraces = new HashMap<String,Collection<ITrace>>(7);
 		try {
 			this.regionListener = new IRegionListener.Stub() {
 				@Override
 				public void regionRemoved(RegionEvent evt) {
 					if (evt.getRegion()!=null) {
 						evt.getRegion().removeROIListener(PerimeterBoxProfileTool.this);
 					}
 				}
 				@Override
 				public void regionsRemoved(RegionEvent evt) {
 					//clears traces if all regions removed
 					final Collection<IRegion> regions = getPlottingSystem().getRegions();
 					if (regions == null || regions.isEmpty()) {
 						registeredTraces.clear();
 						zoomProfilePlottingSystem.clear();
 						verticalProfilePlottingSystem.clear();
 						horizontalProfilePlottingSystem.clear();
 					}
 				}
 
 				@Override
 				public void regionAdded(RegionEvent evt) {}
 
 				@Override
 				public void regionCreated(RegionEvent evt) {
 					if (evt.getRegion()!=null) {
 						evt.getRegion().addROIListener(PerimeterBoxProfileTool.this);
 					}
 				}
 				protected void update(RegionEvent evt) {}
 			};
 		} catch (Exception e) {
 			logger.error("Cannot get plotting system!", e);
 		}
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		// Create extra toolbar buttons
 		createActions(getSite());
 
 		profileContentComposite = new Composite(parent, SWT.NONE);
 		profileContentComposite.setLayout(new GridLayout(1, true));
 		SashForm sashForm = new SashForm(profileContentComposite, SWT.HORIZONTAL);
 		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		sashForm.setBackground(new Color(parent.getDisplay(), 192, 192, 192));
 
 		SashForm sashForm2 = new SashForm(sashForm, SWT.VERTICAL);
 		sashForm2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		sashForm2.setBackground(new Color(parent.getDisplay(), 192, 192, 192));
 		SashForm sashForm3 = new SashForm(sashForm, SWT.VERTICAL);
 		sashForm3.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		sashForm3.setBackground(new Color(parent.getDisplay(), 192, 192, 192));
 
 		try {
 			// we create the zool plot part
 			zoomProfile = (ProfileTool)ToolPageFactory.getToolPage("org.dawb.workbench.plotting.tools.zoomTool");
 			zoomProfile.setToolSystem((AbstractPlottingSystem)getPlottingSystem());
 			zoomProfile.setPlottingSystem((AbstractPlottingSystem)getPlottingSystem());
 			zoomProfile.setTitle("Zoom Profile");
 			zoomProfile.setToolId(String.valueOf(zoomProfile.hashCode()));
 			zoomProfile.createControl(sashForm2);
 			zoomProfile.activate();
 
 			//horizontal profiles
 			sideProfile1 = (IProfileToolPage)ToolPageFactory.getToolPage("org.dawb.workbench.plotting.tools.boxLineProfileTool");
 			sideProfile1.setLineType(SWT.HORIZONTAL);
 			sideProfile1.setPlotEdgeProfile(true);
 			sideProfile1.setPlotAverageProfile(false);
 			sideProfile1.setToolSystem((AbstractPlottingSystem)getPlottingSystem());
 			sideProfile1.setPlottingSystem((AbstractPlottingSystem)getPlottingSystem());
 			sideProfile1.setTitle("Horizontal Perimeter Profile");
 			sideProfile1.setToolId(String.valueOf(sideProfile1.hashCode()));
 			sideProfile1.createControl(sashForm2);
 			sideProfile1.activate();
 			//vertical profiles
 			sideProfile2 = (IProfileToolPage)ToolPageFactory.getToolPage("org.dawb.workbench.plotting.tools.boxLineProfileTool");
 			sideProfile2.setLineType(SWT.VERTICAL);
 			sideProfile2.setPlotEdgeProfile(true);
 			sideProfile2.setPlotAverageProfile(false);
 			sideProfile2.setToolSystem((AbstractPlottingSystem)getPlottingSystem());
 			sideProfile2.setPlottingSystem((AbstractPlottingSystem)getPlottingSystem());
 			sideProfile2.setTitle("Vertical Perimeter Profile");
 			sideProfile2.setToolId(String.valueOf(sideProfile2.hashCode()));
 			sideProfile2.createControl(sashForm3);
 			sideProfile2.activate();
 
 			//profiles plotting systems
 			zoomProfilePlottingSystem = (AbstractPlottingSystem)zoomProfile.getToolPlottingSystem();
 			zoomProfilePlottingSystem.setRescale(true);
 			zoomProfilePlottingSystem.setKeepAspect(false);
 			verticalProfilePlottingSystem = (AbstractPlottingSystem)sideProfile2.getToolPlottingSystem();
 			verticalProfilePlottingSystem.setShowLegend(false);
 			horizontalProfilePlottingSystem = (AbstractPlottingSystem)sideProfile1.getToolPlottingSystem();
 			horizontalProfilePlottingSystem.setShowLegend(false);
 
 			//start: we create the ROI information composite
 			final ScrolledComposite scrollComposite = new ScrolledComposite(sashForm3, SWT.H_SCROLL | SWT.V_SCROLL);
 			final Composite contentComposite = new Composite(scrollComposite, SWT.FILL);
 			contentComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true, 1, 1));
 			contentComposite.setLayout(new GridLayout(1, false));
 
 			ExpansionAdapter expansionAdapter = new ExpansionAdapter() {
 				@Override
 				public void expansionStateChanged(ExpansionEvent e) {
 					logger.trace("regionsExpander");
 					Rectangle r = scrollComposite.getClientArea();
 					scrollComposite.setMinSize(contentComposite.computeSize(r.width, SWT.DEFAULT));
 					contentComposite.layout();
 				}
 			};
 
 			Label metadataLabel = new Label(contentComposite, SWT.NONE);
 			metadataLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
 			metadataLabel.setAlignment(SWT.CENTER);
 			metadataLabel.setText("Region Of Interest Information");
 
 			//main roi
 			ExpandableComposite mainRegionInfoExpander = new ExpandableComposite(contentComposite, SWT.NONE);
 			mainRegionInfoExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 			mainRegionInfoExpander.setLayout(new GridLayout(1, false));
 			mainRegionInfoExpander.setText("Colour Box ROI");
 
 			Composite mainRegionComposite = new Composite(mainRegionInfoExpander, SWT.NONE);
 			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 			mainRegionComposite.setLayout(new GridLayout(1, false));
 			mainRegionComposite.setLayoutData(gridData);
 
 			myROIWidget = new ROIWidget(mainRegionComposite, (AbstractPlottingSystem) getPlottingSystem(), "Perimeter Box region editor");
 			myROIWidget.createWidget();
 			myROIWidget.addSelectionChangedListener(new ISelectionChangedListener() {
 				@Override
 				public void selectionChanged(SelectionChangedEvent event) {
 					IROI newRoi = myROIWidget.getROI();
 					String regionName = myROIWidget.getRegionName();
 					IRegion region = getPlottingSystem().getRegion(regionName);
 					if(region != null){
 						region.setROI(newRoi);
 					}
 				}
 			});
 
 			Group regionSumGroup = new Group(mainRegionComposite, SWT.NONE);
 			regionSumGroup.setText("Sum");
 			gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 			regionSumGroup.setLayout(new GridLayout(1, false));
 			regionSumGroup.setLayoutData(gridData);
 			roiSumProfile = (RegionSumTool)ToolPageFactory.getToolPage("org.dawb.workbench.plotting.tools.regionSumTool");
 			roiSumProfile.setToolSystem((AbstractPlottingSystem) getPlottingSystem());
 			roiSumProfile.setPlottingSystem((AbstractPlottingSystem) getPlottingSystem());
 			roiSumProfile.setTitle("Region_Sum");
 			roiSumProfile.setToolId(String.valueOf(roiSumProfile.hashCode()));
 			roiSumProfile.createControl(regionSumGroup);
 			roiSumProfile.activate();
 
 			mainRegionInfoExpander.setClient(mainRegionComposite);
 			mainRegionInfoExpander.addExpansionListener(expansionAdapter);
 			mainRegionInfoExpander.setExpanded(true);
 
 			//vertical xaxis roi
 			ExpandableComposite verticalRegionInfoExpander = new ExpandableComposite(contentComposite, SWT.NONE);
 			verticalRegionInfoExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 			verticalRegionInfoExpander.setLayout(new GridLayout(1, false));
 			verticalRegionInfoExpander.setText("Vertical Profile ROI");
 
 			Composite verticalProfileComposite = new Composite(verticalRegionInfoExpander, SWT.NONE);
 			verticalProfileComposite.setLayout(new GridLayout(1, false));
 			verticalProfileComposite.setLayoutData(gridData);
 
 			verticalProfileROIWidget = new ROIWidget(verticalProfileComposite, verticalProfilePlottingSystem, "Left/Right region editor");
 			verticalProfileROIWidget.setIsProfile(true);
 			verticalProfileROIWidget.createWidget();
 			verticalProfileROIWidget.addSelectionChangedListener(new ISelectionChangedListener() {
 				@Override
 				public void selectionChanged(SelectionChangedEvent event) {
 					IROI newRoi = verticalProfileROIWidget.getROI();
 					String regionName = verticalProfileROIWidget.getRegionName();
 					IRegion region = verticalProfilePlottingSystem.getRegion(regionName);
 					if(region != null){
 						region.setROI(newRoi);
 					}
 				}
 			});
 			verticalRegionInfoExpander.setClient(verticalProfileComposite);
 			verticalRegionInfoExpander.addExpansionListener(expansionAdapter);
 			verticalRegionInfoExpander.setExpanded(false);
 			
 			//horizontal xaxis roi
 			ExpandableComposite horizontalRegionInfoExpander = new ExpandableComposite(contentComposite, SWT.NONE);
 			horizontalRegionInfoExpander.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 			horizontalRegionInfoExpander.setLayout(new GridLayout(1, false));
 			horizontalRegionInfoExpander.setText("Horizontal Profile ROI");
 
 			Composite horizontalProfileComposite = new Composite(horizontalRegionInfoExpander, SWT.NONE);
 			horizontalProfileComposite.setLayout(new GridLayout(1, false));
 			horizontalProfileComposite.setLayoutData(gridData);
 
 			horizontalProfileROIWidget = new ROIWidget(horizontalProfileComposite, horizontalProfilePlottingSystem, "Bottom/Up region editor");
 			horizontalProfileROIWidget.setIsProfile(true);
 			horizontalProfileROIWidget.createWidget();
 			horizontalProfileROIWidget.addSelectionChangedListener(new ISelectionChangedListener() {
 				@Override
 				public void selectionChanged(SelectionChangedEvent event) {
 					IROI newRoi = horizontalProfileROIWidget.getROI();
 					String regionName = horizontalProfileROIWidget.getRegionName();
 					IRegion region = horizontalProfilePlottingSystem.getRegion(regionName);
 					if(region != null){
 						region.setROI(newRoi);
 					}
 				}
 			});
 			horizontalRegionInfoExpander.setClient(horizontalProfileComposite);
 			horizontalRegionInfoExpander.addExpansionListener(expansionAdapter);
 			horizontalRegionInfoExpander.setExpanded(false);
 
 			scrollComposite.setContent(contentComposite);
 			scrollComposite.setExpandHorizontal(true);
 			scrollComposite.setExpandVertical(true);
 			scrollComposite.addControlListener(new ControlAdapter() {
 				@Override
 				public void controlResized(ControlEvent e) {
 					Rectangle r = scrollComposite.getClientArea();
 					scrollComposite.setMinSize(contentComposite.computeSize(r.width, SWT.DEFAULT));
 				}
 			});
 			//end Roi information composite
 
 			sashForm.setWeights(new int[]{1, 1});
 
 			//activate();
 		} catch (Exception e) {
 			logger.error("Cannot locate any Abstract plotting System!", e);
 		}
 	}
 
 	private void createActions(IPageSite site) {
 
 		final Action plotAverage = new Action("Plot Average Box Profiles", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				if(isChecked()){
 					sideProfile1.setPlotAverageProfile(true);
 					sideProfile2.setPlotAverageProfile(true);
 				}
 				else{
 					sideProfile1.setPlotAverageProfile(false);
 					sideProfile2.setPlotAverageProfile(false);
 				}
 				sideProfile1.update(region);
 				sideProfile2.update(region);
 			}
 		};
 		plotAverage.setToolTipText("Toggle On/Off Average Profiles");
 		plotAverage.setText("Average");
 		plotAverage.setImageDescriptor(Activator.getImageDescriptor("icons/average.png"));
 
 		final Action plotEdge = new Action("Plot Edge Box Profiles", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				if(isChecked()){
 					sideProfile1.setPlotEdgeProfile(true);
 					sideProfile2.setPlotEdgeProfile(true);
 				}
 				else{
 					sideProfile1.setPlotEdgeProfile(false);
 					sideProfile2.setPlotEdgeProfile(false);
 				}
 				sideProfile1.update(region);
 				sideProfile2.update(region);
 			}
 		};
 		plotEdge.setToolTipText("Toggle On/Off Perimeter Profiles");
 		plotEdge.setText("Edge");
 		plotEdge.setChecked(true);
 		plotEdge.setImageDescriptor(Activator.getImageDescriptor("icons/edge-color-box.png"));
 
 		// if site is null, the tool has been called programmatically
 		if(site != null){
 			IToolBarManager toolMan = getSite().getActionBars().getToolBarManager();
 			MenuManager menuMan = new MenuManager();
 			toolMan.add(plotEdge);
 			menuMan.add(plotEdge);
 			toolMan.add(plotAverage);
 			menuMan.add(plotAverage);
 		} 
 	}
 
 	@Override
 	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
 		if (clazz == IToolPageSystem.class) {
 			return profileContentComposite;
 		} else {
 			return super.getAdapter(clazz);
 		}
 	}
 
 	@Override
 	public final ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 
 	@Override
 	public void setFocus() {
 		if (getControl()!=null && !getControl().isDisposed()) {
 			getControl().setFocus();
 		}
 	}
 
 	@Override
 	public void activate() {
 		super.activate();
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().addRegionListener(regionListener);
 		}
 		setRegionsActive(true);
 
 		createNewRegion();
 
 		if(myROIWidget != null)
 			myROIWidget.addRegionListener((AbstractPlottingSystem)getPlottingSystem());
 		if(verticalProfileROIWidget != null)
 			verticalProfileROIWidget.addRegionListener(verticalProfilePlottingSystem);
 		if(horizontalProfileROIWidget != null)
 			horizontalProfileROIWidget.addRegionListener(horizontalProfilePlottingSystem);
 		if(zoomProfile != null && sideProfile1 != null 
 				&& sideProfile2 != null && roiSumProfile != null){
 			zoomProfile.activate();
 			sideProfile1.activate();
 			sideProfile2.activate();
 			roiSumProfile.activate();
 		}
 	}
 	
 	private final void createNewRegion() {
 		// Start with a selection of the right type
 		try {
 			IRegion region = getPlottingSystem().createRegion(RegionUtils.getUniqueName(getRegionName(), getPlottingSystem()), getCreateRegionType());
 			region.setUserObject(getMarker());
 		} catch (Exception e) {
 			logger.error("Cannot create region for profile tool!");
 		}
 	}
 	
 	/**
 	 * The object used to mark this profile as being part of this tool.
 	 * By default just uses package string.
 	 * @return
 	 */
 	private Object getMarker() {
 		return getToolPageRole().getClass().getName().intern();
 	}
 
 	public boolean isRegionTypeSupported(RegionType type) {
 		return (type==RegionType.BOX)||(type==RegionType.PERIMETERBOX)||(type==RegionType.XAXIS)||(type==RegionType.YAXIS);
 	}
 
 	private RegionType getCreateRegionType() {
 		return RegionType.PERIMETERBOX;
 	}
 
 	@Override
 	public void deactivate() {
 		super.deactivate();
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().removeRegionListener(regionListener);
 		}
 		setRegionsActive(false);
 		if(zoomProfile != null && sideProfile1 != null
 				&& sideProfile2 != null && roiSumProfile != null){
 			zoomProfile.deactivate();
 			sideProfile1.deactivate();
 			sideProfile2.deactivate();
 			roiSumProfile.deactivate();
 		}
		if(zoomProfilePlottingSystem != null
				&& verticalProfilePlottingSystem != null
				&& horizontalProfilePlottingSystem != null){
			zoomProfilePlottingSystem.clear();
			verticalProfilePlottingSystem.clear();
			horizontalProfilePlottingSystem.clear();
		}
 		if(myROIWidget != null)
 			myROIWidget.dispose();
 		if(verticalProfileROIWidget != null)
 			verticalProfileROIWidget.dispose();
 		if(horizontalProfileROIWidget != null)
 			horizontalProfileROIWidget.dispose();
 	}
 	
 	private void setRegionsActive(boolean active) {
 		if (getPlottingSystem()!=null) {
 			final Collection<IRegion> regions = getPlottingSystem().getRegions();
 			if (regions!=null) for (IRegion iRegion : regions) {
 				if (active) {
 					iRegion.addROIListener(this);
 				} else {
 					iRegion.removeROIListener(this);
 				}
 				if (iRegion.getUserObject()==getMarker()) {
 					if (active) {
 						iRegion.setVisible(active);
 					} else {
 						// If the plotting system has changed dimensionality
 						// to something not compatible with us, remove the region.
 						// TODO Change to having getRank() == rank 
 						if (getToolPageRole().is2D() && !getPlottingSystem().is2D()) {
 							iRegion.setVisible(active);
 						} else if (getPlottingSystem().is2D() && !getToolPageRole().is2D()) {
 							iRegion.setVisible(active);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public Control getControl() {
 		return profileContentComposite;
 	}
 
 	@Override
 	public void dispose() {
 		deactivate();
 		zoomProfile.dispose();
 		sideProfile1.dispose();
 		sideProfile2.dispose();
 		roiSumProfile.dispose();
 		registeredTraces.clear();
 		if (zoomProfilePlottingSystem!=null) zoomProfilePlottingSystem.dispose();
 		zoomProfilePlottingSystem = null;
 		if (verticalProfilePlottingSystem!=null) verticalProfilePlottingSystem.dispose();
 		verticalProfilePlottingSystem = null;
 		if (horizontalProfilePlottingSystem!=null) horizontalProfilePlottingSystem.dispose();
 		horizontalProfilePlottingSystem = null;
 		super.dispose();
 	}
 
 	@Override
 	public void roiDragged(ROIEvent evt) {
 		region = (IRegion)evt.getSource();
 		myROIWidget.setEditingRegion(region);
 	}
 
 	@Override
 	public void roiChanged(ROIEvent evt) {
 		region = (IRegion)evt.getSource();
 		myROIWidget.setEditingRegion(region);
 	}
 
 	private String getRegionName() {
 		return "Profile";
 	}
 
 	@Override
 	public IPlottingSystem getToolPlottingSystem() {
 		return zoomProfilePlottingSystem;
 	}
 
 	@Override
 	public void roiSelected(ROIEvent evt) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean isStaticTool() {
 		return true;
 	}
 }
