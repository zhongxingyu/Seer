 package org.dawb.workbench.plotting.system;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.ui.image.PaletteFactory;
 import org.dawb.common.ui.menu.CheckableActionGroup;
 import org.dawb.common.ui.menu.MenuAction;
 import org.dawb.common.ui.plot.PlotType;
 import org.dawb.common.ui.plot.PlottingActionBarManager;
 import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.IImageTrace.ImageOrigin;
 import org.dawb.common.ui.plot.trace.TraceEvent;
 import org.dawb.workbench.plotting.Activator;
 import org.dawb.workbench.plotting.preference.PlottingConstants;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.ui.IActionBars;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 class LightWeightActionBarsManager extends PlottingActionBarManager {
 
 
 	private static final Logger logger = LoggerFactory.getLogger(LightWeightActionBarsManager.class);
 	
 	private LightWeightPlottingSystem system;
 	private Action                    plotIndex, plotX;
 	private boolean                   datasetChoosingRequired = true;
 	private List<ActionContainer>     oneDimensionalActions;
 	private List<ActionContainer>     twoDimensionalActions;
 
 	protected LightWeightActionBarsManager(LightWeightPlottingSystem system) {
 		super(system);
 		this.system = system;
 		oneDimensionalActions = new ArrayList<ActionContainer>();
 		twoDimensionalActions = new ArrayList<ActionContainer>();
 	}
 
 	protected void switchActions(final PlotType type) {
 		
 		final IActionBars bars = system.getActionBars();
     	if (bars==null) return;
     	 
     	if (oneDimensionalActions!=null) for (ActionContainer ac : oneDimensionalActions) {
     		if (type.is1D() && ac.getManager().find(ac.getId())==null) {
     			ac.getManager().insertAfter(ac.getId()+".group", ac.getAction());
     		} else if (!type.is1D()) {
     			ac.getManager().remove(ac.getId());
     		}
 		}
     	
     	final boolean is2D = !type.is1D();
     	if (twoDimensionalActions!=null) for (ActionContainer ac : twoDimensionalActions) {
     		if (is2D && ac.getManager().find(ac.getId())==null) {
     			ac.getManager().insertAfter(ac.getId()+".group", ac.getAction());
     		} else if (!is2D) {
     			ac.getManager().remove(ac.getId());
       		}
 		}
     	
     	if (bars.getToolBarManager()!=null)    bars.getToolBarManager().update(true);
     	if (bars.getMenuManager()!=null)       bars.getMenuManager().update(true);
     	if (bars.getStatusLineManager()!=null) bars.getStatusLineManager().update(true);
     	bars.updateActionBars();
 	}
 	
 	
 	protected void createToolDimensionalActions(final ToolPageRole role,
 			                                    final String       viewId) {
 
 		final IActionBars bars = system.getActionBars();
 		if (bars!=null) {
        	
 			try {
 				IAction toolSet = createToolActions(role, viewId);
 				if (toolSet==null) return;
 
 				bars.getToolBarManager().add(new Separator(role.getId()+".group"));
 				bars.getToolBarManager().insertAfter(role.getId()+".group", toolSet);
 				if (role.is1D()&&!role.is2D()) oneDimensionalActions.add(new ActionContainer(toolSet, bars.getToolBarManager()));
 				if (role.is2D()&&!role.is1D()) twoDimensionalActions.add(new ActionContainer(toolSet, bars.getToolBarManager()));
 
 	        	
 			} catch (Exception e) {
 				logger.error("Reading extensions for plotting tools", e);
 			}
        }	
 	}
 	
 	protected void createAspectAction() {
 
 		final Action action = new Action("Keep aspect ratio", IAction.AS_CHECK_BOX) {
 			
 		    public void run() {		    	
 		    	Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.ASPECT, isChecked());
 		    	system.getGraph().setKeepAspect(isChecked());
 		    	system.repaint();
 		    }
 		};
         
 		action.setImageDescriptor(Activator.getImageDescriptor("icons/aspect.png"));
 		action.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.ASPECT));
 		
 		final IActionBars bars = system.getActionBars();
 		if (bars!=null) {
 			bars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.aspect.group"));
 			action.setId("org.dawb.workbench.plotting.aspect");
 			bars.getToolBarManager().insertAfter("org.dawb.workbench.plotting.aspect.group", action);
 	 
 			twoDimensionalActions.add(new ActionContainer(action, bars.getToolBarManager()));
 		}
 	}
 	
 	protected void createPalleteActions() {
 		
     	final Map<String,Integer> names = PaletteFactory.getPaletteNames();
     	
 		int paletteIndex = Activator.getDefault().getPreferenceStore().getInt(PlottingConstants.P_PALETTE);
 
 		final MenuAction lutCombo = new MenuAction("Color");
 		lutCombo.setId(getClass().getName()+lutCombo.getText());
 		
 		lutCombo.setImageDescriptor(Activator.getImageDescriptor("icons/color_wheel.png"));
 		
 		CheckableActionGroup group      = new CheckableActionGroup();
 		for (final String paletteName : names.keySet()) {
 			final Action action = new Action(paletteName, IAction.AS_CHECK_BOX) {
 				public void run() {
 					int paletteIndex = PaletteFactory.PALETTES.get(paletteName);
 					Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.P_PALETTE, paletteIndex);
 					final PaletteData data = PaletteFactory.getPalette(paletteIndex);
 					system.getGraph().setPaletteData(data);
 				}
 			};
 			group.add(action);
 			lutCombo.add(action);
 			action.setChecked(PaletteFactory.PALETTES.get(paletteName)==paletteIndex);
 		}
 		lutCombo.setToolTipText("Histogram");
 
 		final IActionBars bars = system.getActionBars();
 		if (bars!=null) {
 			bars.getMenuManager().add(new Separator(lutCombo.getId()+".group"));
 			bars.getMenuManager().insertAfter(lutCombo.getId()+".group", lutCombo);
 			
 			twoDimensionalActions.add(new ActionContainer(lutCombo, bars.getMenuManager()));
 		}
 	}
 	
 
 	public void createOriginActions() {
 
 		final MenuAction origins = new MenuAction("Image Origin");
 		origins.setId(getClass().getName()+".imageOrigin");
 		
 		origins.setImageDescriptor(Activator.getImageDescriptor("icons/origins.png"));
 		
 		CheckableActionGroup group      = new CheckableActionGroup();
         ImageOrigin imageOrigin = IImageTrace.ImageOrigin.forLabel(Activator.getDefault().getPreferenceStore().getString(PlottingConstants.ORIGIN_PREF));
         IAction selectedAction  = null;
         
         for (final ImageOrigin origin : IImageTrace.ImageOrigin.origins) {
 			
         	final IAction action = new Action(origin.getLabel(), IAction.AS_CHECK_BOX) {
         		public void run() {
         			Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.ORIGIN_PREF, origin.getLabel());
        			    system.getGraph().setImageOrigin(origin);
        			    setChecked(true);
         		}
         	};
         	origins.add(action);
         	group.add(action);
         	
         	if (imageOrigin==origin) selectedAction = action;
 		}
         
         if (selectedAction!=null) selectedAction.setChecked(true);
         
 		final IActionBars bars = system.getActionBars();
 		bars.getMenuManager().add(new Separator(origins.getId()+".group"));
 		bars.getMenuManager().insertAfter(origins.getId()+".group", origins);
 		
 		twoDimensionalActions.add(new ActionContainer(origins, bars.getMenuManager()));
 
 	}
 
 
 	/**
 	 * Also uses 'bars' field to add the actions
 	 * @param rightClick
 	 */
 	protected void createAdditionalActions(final IContributionManager rightClick) {
 		
         // Add additional if required
 		final IActionBars bars = system.getActionBars();
 			
         if (extra1DActions!=null&&!extra1DActions.isEmpty()){
     		bars.getToolBarManager().add(new Separator("org.dawb.workbench.plotting.extra1D.group"));
          	for (IAction action : extra1DActions) {
         		bars.getToolBarManager().add(action);
         		action.setId("org.dawb.workbench.plotting.extra1D");
         		oneDimensionalActions.add(new ActionContainer(action, bars.getToolBarManager()));
         	}
         }
         
         // Add more actions
         // Rescale		
 		final Action rescaleAction = new Action("Rescale axis when plotted data changes", Activator.getImageDescriptor("icons/rescale.png")) {
 		    public void run() {
 				system.setRescale(!system.isRescale());
 		    }
 		};
 		rescaleAction.setChecked(this.system.isRescale());
 		rescaleAction.setId("org.dawb.workbench.plotting.rescale");
 		if (bars!=null) oneDimensionalActions.add(new ActionContainer(rescaleAction, bars.getToolBarManager()));
 
         if (bars!=null) bars.getToolBarManager().add(new Separator(rescaleAction.getId()+".group"));
 		rightClick.add(new Separator(rescaleAction.getId()+".group"));
 
 		if (bars!=null) bars.getToolBarManager().insertAfter(rescaleAction.getId()+".group", rescaleAction);
 		rightClick.insertAfter(rescaleAction.getId()+".group", rescaleAction);
 
 		
 		if (datasetChoosingRequired) {
 			// By index or using x 
 			final CheckableActionGroup group = new CheckableActionGroup();
 			plotIndex = new Action("Plot using indices", IAction.AS_CHECK_BOX) {
 			    public void run() {
 			    	Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.PLOT_X_DATASET, false);
 			    	setChecked(true);
 			    	system.setXfirst(false);
 			    	setXfirst(false);
 			    	system.fireTracesAltered(new TraceEvent(system.getGraph()));
 			    }
 			};
 			plotIndex.setImageDescriptor(Activator.getImageDescriptor("icons/plotindex.png"));
 			plotIndex.setId("org.dawb.workbench.plotting.plotIndex");
 			group.add(plotIndex);
 			
 			plotX = new Action("Plot using first data set selected as x-axis", IAction.AS_CHECK_BOX) {
 			    public void run() {
 			    	Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.PLOT_X_DATASET, true);
 			    	setChecked(true);
 			    	system.setXfirst(true);
 			    	setXfirst(true);
 			    	system.fireTracesAltered(new TraceEvent(system.getGraph()));
 			    }
 			};
 			plotX.setImageDescriptor(Activator.getImageDescriptor("icons/plotxaxis.png"));
 			plotX.setId("org.dawb.workbench.plotting.plotX");
 			group.add(plotX);
 			
 			boolean xfirst = Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.PLOT_X_DATASET);
 			if (xfirst) {
 				plotX.setChecked(true);
 			} else {
 				plotIndex.setChecked(true);
 			}
 			
 			
 			if (bars!=null) {
 			
 				bars.getToolBarManager().add(new Separator(plotIndex.getId()+".group"));
 		        bars.getToolBarManager().add(new Separator(plotX.getId()+".group"));
 				bars.getToolBarManager().add(plotIndex);
 	       		oneDimensionalActions.add(new ActionContainer(plotIndex, bars.getToolBarManager()));
 				bars.getToolBarManager().add(plotX);
 	       		oneDimensionalActions.add(new ActionContainer(plotX, bars.getToolBarManager()));
 		        bars.getToolBarManager().add(new Separator());
 			}
 			
 			rightClick.add(new Separator(plotIndex.getId()+".group"));
 			rightClick.add(new Separator(plotX.getId()+".group"));
 			rightClick.add(plotIndex);
 			rightClick.add(plotX);
 			rightClick.add(new Separator());
 		}
 		
 				
 	}
 
 	public void setXfirst(boolean xfirst) {
 		if (xfirst) {
 			if (plotX!=null) plotX.setChecked(true);
 		} else {
 			if (plotIndex!=null) plotIndex.setChecked(true);
 		}
 	}
 	
 	public void setDatasetChoosingRequired(boolean choosingRequired) {
 		if (plotX!=null)     plotX.setEnabled(choosingRequired);
 		if (plotIndex!=null) plotIndex.setEnabled(choosingRequired);
 		this.datasetChoosingRequired  = choosingRequired;
 	}	
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		
 	    plotIndex = null;
 	    plotX     = null;
 	    
 	    if (oneDimensionalActions!=null) oneDimensionalActions.clear();
 	    oneDimensionalActions = null;
 	       
 	    if (twoDimensionalActions!=null) twoDimensionalActions.clear();
 	    twoDimensionalActions = null;
 
 	}
 	
 	
 	private final class ActionContainer {
         private IAction action;
         private IContributionManager manager;
         
         public ActionContainer(IAction action, IContributionManager manager) {
         	this.action  = action;
         	this.manager = manager;
         }
         
 		public String getId() {
 			return action.getId();
 		}
 
 		public IAction getAction() {
 			return action;
 		}
 		public void setAction(IAction action) {
 			this.action = action;
 		}
 		public IContributionManager getManager() {
 			return manager;
 		}
 		public void setManager(IContributionManager manager) {
 			this.manager = manager;
 		}
 	}
 
 
 }
