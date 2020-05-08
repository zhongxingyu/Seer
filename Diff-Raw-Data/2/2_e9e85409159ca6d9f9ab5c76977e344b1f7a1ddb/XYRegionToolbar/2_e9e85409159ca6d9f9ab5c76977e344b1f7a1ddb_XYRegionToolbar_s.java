 package org.dawb.workbench.plotting.system.swtxy;
 
 
 import java.util.Collection;
 import java.util.List;
 
 import org.csstudio.swt.xygraph.toolbar.CheckableActionGroup;
 import org.csstudio.swt.xygraph.toolbar.GrayableButton;
 import org.csstudio.swt.xygraph.toolbar.XYGraphConfigDialog;
 import org.csstudio.swt.xygraph.toolbar.XYGraphToolbar;
 import org.dawb.common.ui.menu.MenuAction;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.workbench.plotting.Activator;
 import org.dawb.workbench.plotting.printing.PlotPrintPreviewDialog;
 import org.dawb.workbench.plotting.printing.PrintSettings;
 import org.dawb.workbench.plotting.system.dialog.AddRegionDialog;
 import org.dawb.workbench.plotting.system.dialog.RemoveRegionCommand;
 import org.dawb.workbench.plotting.system.dialog.RemoveRegionDialog;
 import org.eclipse.draw2d.ActionEvent;
 import org.eclipse.draw2d.ActionListener;
 import org.eclipse.draw2d.Button;
 import org.eclipse.draw2d.ButtonModel;
 import org.eclipse.draw2d.ChangeEvent;
 import org.eclipse.draw2d.ChangeListener;
 import org.eclipse.draw2d.Clickable;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.ImageFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.ToggleButton;
 import org.eclipse.draw2d.ToggleModel;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IContributionManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.widgets.Display;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class XYRegionToolbar extends XYGraphToolbar {
 
 
 	private static Logger logger = LoggerFactory.getLogger(XYRegionToolbar.class);
 	
 	private XYRegionGraph regionGraph;
 
 
 	public XYRegionToolbar(XYRegionGraph xyGraph) {
 		super(xyGraph);
 		this.regionGraph = xyGraph;
 	}
 
 	public XYRegionToolbar(XYRegionGraph xyGraph, int flags) {
 		super(xyGraph, flags);
 		this.regionGraph = xyGraph;
 	}
 	
 	public void createGraphActions(final IContributionManager tool, final IContributionManager men) {
 
         final CheckableActionGroup zoomG = new CheckableActionGroup();
         
         MenuAction zoomMenuTmp =null;
         if (System.getProperty("org.dawb.workbench.plotting.system.swtxy.zoomDropDown") !=null) {
         	zoomMenuTmp = new MenuAction("Zoom tool");
         	zoomMenuTmp.setToolTipText("Zoom tool");
         	zoomMenuTmp.setId("org.dawb.workbench.ui.editors.plotting.swtxy.zoomTools");
         }
         
         final MenuAction zoomDropDown = zoomMenuTmp;
         
         for (Object child : getChildren()) {
 			
         	if (!(child instanceof Figure)) continue;
         	final Figure c = (Figure)child;
         	if (c instanceof Clickable) {
         		
         		final Clickable button = (Clickable)c;
         		final int flag = button instanceof ToggleButton
         		               ? IAction.AS_CHECK_BOX
         		               : IAction.AS_PUSH_BUTTON;
         		
         		final String text  = ((Label)button.getToolTip()).getText();
         		
         		final Object cont  = button.getChildren().get(0);
         		final Image  image = cont instanceof ImageFigure
         		                   ? ((ImageFigure)cont).getImage()
         		                   : ((Label)cont).getIcon();
         		                   
         		final Action action = new Action(text, flag) {
         			public void run() {
         				if (button.getModel() instanceof ToggleModel) {
         					((ToggleModel)button.getModel()).fireActionPerformed();
         				} else {
         				    button.doClick();
         				}  
         				if (zoomDropDown!=null && zoomGroup.getElements().contains(button.getModel())) {
         					zoomDropDown.setSelectedAction(this);
         				}
         			}
 				};
 				 
 				if (flag == IAction.AS_CHECK_BOX) {
 					final boolean isSel = button.isSelected();
 					action.setChecked(isSel);
 				}
 
 				if (button instanceof GrayableButton) {
 					final GrayableButton gb = (GrayableButton)button;
 					gb.addChangeListener(new ChangeListener() {	
 						@Override
 						public void handleStateChanged(ChangeEvent event) {
 							if (event.getPropertyName().equals(ButtonModel.ENABLED_PROPERTY)) {
                                 action.setEnabled(gb.isEnabled());
 							}
 						};
 					});
 
 				};
         				
 				action.setImageDescriptor(new ImageDescriptor() {			
 					@Override
 					public ImageData getImageData() {
 						return image.getImageData();
 					}
 				});
 				
         	    final List models = zoomGroup.getElements();
         	    if (models.contains(button.getModel())) {
         	    	if (zoomDropDown!=null && tool.find(zoomDropDown.getId())==null) {
         				tool.add(zoomDropDown);
         				men.add(zoomDropDown);
         	    	}
         	    	if (zoomDropDown!=null) {
         	    		zoomDropDown.add(action);
         	    	} else {
         	    		tool.add(action);
         	    		men.add(action);
         	    	}
         	    	zoomG.add(action);
         	    } else {
     				tool.add(action);
     				men.add(action);
         	    }
         	    
         	} else if (c instanceof ToolbarSeparator) {
         		
            		tool.add(new Separator(((ToolbarSeparator)c).getId()));
            		men.add(new Separator(((ToolbarSeparator)c).getId()));
         	}
         }
         
         if (zoomDropDown!=null) zoomDropDown.setSelectedAction(0);
         if (zoomDropDown!=null) zoomDropDown.getAction(0).setChecked(true);
        
         final MenuAction regionDropDown = new MenuAction("Add a selection region");
         regionDropDown.setId("org.dawb.workbench.ui.editors.plotting.swtxy.addRegions");
  
 		final Action addLine = new Action("Add Line Selection...", Activator.getImageDescriptor("icons/ProfileLine.png")) {
 			public void run() {				
 				try {
 					createRegion(regionDropDown, this, RegionType.LINE);
 				} catch (Exception e) {
 					logger.error("Cannot create region!", e);
 				}
 			}
 		};
 		regionDropDown.add(addLine);
 		
 		final Action addBox = new Action("Add Box Selection...", Activator.getImageDescriptor("icons/ProfileBox.png")) {
 			public void run() {				
 				try {
 					createRegion(regionDropDown, this, RegionType.BOX);
 				} catch (Exception e) {
 					logger.error("Cannot create region!", e);
 				}
 			}
 		};
 		regionDropDown.add(addBox);
 		
 		
 		final Action addXAxis = new Action("Add X-Axis Selection...", Activator.getImageDescriptor("icons/Cursor-horiz.png")) {
 			public void run() {				
 				try {
 					createRegion(regionDropDown, this, RegionType.XAXIS);
 				} catch (Exception e) {
 					logger.error("Cannot create region!", e);
 				}
 			}
 		};
 		regionDropDown.add(addXAxis);
 		
 		final Action addYAxis = new Action("Add Y-Axis Selection...", Activator.getImageDescriptor("icons/Cursor-vert.png")) {
 			public void run() {				
 				try {
 					createRegion(regionDropDown, this, RegionType.YAXIS);
 				} catch (Exception e) {
 					logger.error("Cannot create region!", e);
 				}
 			}
 		};
 		regionDropDown.add(addYAxis);
 
 		
 		regionDropDown.setSelectedAction(addLine);
 		
 		tool.insertBefore("org.csstudio.swt.xygraph.toolbar.extra", regionDropDown);
 		men.insertBefore("org.csstudio.swt.xygraph.toolbar.extra", regionDropDown);
 			
         final MenuAction removeRegionDropDown = new MenuAction("Delete selection region(s)");
         regionDropDown.setId("org.dawb.workbench.ui.editors.plotting.swtxy.removeRegions");
 
         final Action removeRegion = new Action("Remove Region...", Activator.getImageDescriptor("icons/RegionDelete.png")) {
 			public void run() {
 				RemoveRegionDialog dialog = new RemoveRegionDialog(Display.getCurrent().getActiveShell(), (XYRegionGraph)xyGraph);
 				if(dialog.open() == Window.OK && dialog.getRegion() != null){
 					((XYRegionGraph)xyGraph).removeRegion(dialog.getRegion());
 					xyGraph.getOperationsManager().addCommand(
 							new RemoveRegionCommand((XYRegionGraph)xyGraph, dialog.getRegion()));					
 				}
 			}
 		};
 		
 		removeRegionDropDown.add(removeRegion);
 		removeRegionDropDown.setSelectedAction(removeRegion);
 		
         final Action removeAllRegions = new Action("Remove all regions...", Activator.getImageDescriptor("icons/RegionDeleteAll.png")) {
 			public void run() {
 				
 				final boolean yes = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), 
 						                  "Please Confirm Delete All",
 						                  "Are you sure you would like to delete all selection regions?");
 				
 				if (yes){
 					xyGraph.getOperationsManager().addCommand(
 							new RemoveRegionCommand((XYRegionGraph)xyGraph, ((XYRegionGraph)xyGraph).getRegions()));					
 					((XYRegionGraph)xyGraph).clearRegions();
 				}
 			}
 		};
 		
 		removeRegionDropDown.add(removeAllRegions);
 
 		
 		tool.insertAfter("org.dawb.workbench.ui.editors.plotting.swtxy.addRegions", removeRegionDropDown);
 		men.insertAfter("org.dawb.workbench.ui.editors.plotting.swtxy.addRegions", removeRegionDropDown);
 	}
 
 	protected void createRegion(MenuAction regionDropDown, Action action, RegionType type) throws Exception {
 		
 		if (xyGraph.getXAxisList().size()==1 && xyGraph.getYAxisList().size()==1) {
 			regionGraph.createRegion(getUniqueName(type.getName()), xyGraph.primaryXAxis, xyGraph.primaryYAxis, type, true);
 		} else {
 			AddRegionDialog dialog = new AddRegionDialog(Display.getCurrent().getActiveShell(), (XYRegionGraph)xyGraph, type);
 			if (dialog.open() != Window.OK){
 				return;
 			}
 		}
 		regionDropDown.setSelectedAction(action);	
 		regionDropDown.setChecked(true);
 	}
 
 	protected String getUniqueName(String base) {
 		int val = 1;
 		final Collection<String> regions = ((RegionArea)xyGraph.getPlotArea()).getRegionNames();
 		if (regions==null) return base+" "+val;
 		while(regions.contains(base+" "+val)) val++;
 		return base+" "+val;
 	}
 
 	protected void openConfigurationDialog() {
 		XYGraphConfigDialog dialog = new XYRegionConfigDialog(Display.getCurrent().getActiveShell(), xyGraph);
 		dialog.open();
 	}
 	
 	private PrintSettings settings;
 
 	@Override
 	public void addSnapshotButton() {
 		super.addSnapshotButton(); // TODO Remove old one later by not calling this
 		
		Button printButton = new Button(createImage("icons/printer.png"));
 		printButton.setToolTip(new Label("Print the plotting"));
 		addButton(printButton);
 		printButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent event) {
 				if (settings==null) settings = new PrintSettings();
 				PlotPrintPreviewDialog dialog = new PlotPrintPreviewDialog(xyGraph, Display.getCurrent(), settings);
 				settings=dialog.open();
 			}
 		});
 	}
 }
