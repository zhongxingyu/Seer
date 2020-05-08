 package org.dawnsci.plotting.tools.region;
 
 import java.util.Collection;
 
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.RegionEvent;
 import org.dawnsci.plotting.api.region.RegionUtils;
 import org.dawnsci.plotting.tools.Activator;
 import org.dawnsci.plotting.tools.region.MeasurementLabelProvider.LabelType;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 
 /**
  * 
  * @author fcp94556
  *
  */
 public class RegionEditTool extends AbstractRegionTableTool {
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 
 	protected void createColumns(final TableViewer viewer) {
 		
 		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
 
 		int count = 0;
 		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, count);
 		var.getColumn().setText("");
 		var.getColumn().setWidth(22);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.VISIBLE));
 		RegionEditingSupport regionEditor = new RegionEditingSupport(this,viewer, count);
 		var.setEditingSupport(regionEditor);
 
 		++count;
 		var = new TableViewerColumn(viewer, SWT.LEFT, count);
 		var.getColumn().setText("Name");
 		var.getColumn().setWidth(120);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ROINAME));
 		regionEditor = new RegionEditingSupport(this,viewer, count);
 		var.setEditingSupport(regionEditor);
 
 		++count;
 		var = new TableViewerColumn(viewer, SWT.CENTER, count);
 		var.getColumn().setText("Start point x");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.STARTX));
 		regionEditor = new RegionEditingSupport(this,viewer, count);
 		var.setEditingSupport(regionEditor);
 
 		++count;
 		var = new TableViewerColumn(viewer, SWT.LEFT, count);
 		var.getColumn().setText("Start point y");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.STARTY));
 		regionEditor = new RegionEditingSupport(this,viewer, count);
 		var.setEditingSupport(regionEditor);
 
 		++count;
 		var = new TableViewerColumn(viewer, SWT.LEFT, count);
 		var.getColumn().setText("End Point x");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ENDX));
 		regionEditor = new RegionEditingSupport(this,viewer,count);
 		var.setEditingSupport(regionEditor);
 
 		++count;
 		var = new TableViewerColumn(viewer, SWT.LEFT, count);
 		var.getColumn().setText("End point y");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ENDY));
 		regionEditor = new RegionEditingSupport(this,viewer, count);
 		var.setEditingSupport(regionEditor);
 
 		++count;
 		var = new TableViewerColumn(viewer, SWT.LEFT, count);
 		var.getColumn().setText("Max Intensity");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.MAX));
 		regionEditor = new RegionEditingSupport(this,viewer,count);
 		var.setEditingSupport(regionEditor);
 
 		++count;
 		var = new TableViewerColumn(viewer, SWT.LEFT, count);
 		var.getColumn().setText("Sum");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.SUM));
 		regionEditor = new RegionEditingSupport(this,viewer, count);
 		var.setEditingSupport(regionEditor);
 
 		++count;
 		var = new TableViewerColumn(viewer, SWT.LEFT, count);
 		var.getColumn().setText("Active");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ACTIVE));
 		regionEditor = new RegionEditingSupport(this,viewer, count);
 		var.setEditingSupport(regionEditor);
 	}
 		
 	@Override
 	protected boolean isRegionOk(IRegion iRegion) {
 		return iRegion.isUserRegion();
 	}
 
 	private String MOBILE_REGION_SETTING = "org.dawnsci.plotting.tools.mobileRegionSetting";
 	
 	protected void createActions() {
 		IToolBarManager man = getSite().getActionBars().getToolBarManager();
 		final Action immobileWhenAdded = new Action("Allow regions to be moved graphically", IAction.AS_CHECK_BOX) {
 			public void run() {
 				Activator.getLocalPreferenceStore().setValue(MOBILE_REGION_SETTING, isChecked());
 				
 				// We also set all regions mobile or immobile
 				final Collection<IRegion> regions = getPlottingSystem().getRegions();
 				for (IRegion iRegion : regions) {
 					if (iRegion.isUserRegion() && iRegion.isVisible()) iRegion.setMobile(isChecked());
 				}
 			}
 		};
 		immobileWhenAdded.setImageDescriptor(Activator.getImageDescriptor("icons/traffic-light-green.png"));
 		immobileWhenAdded.setChecked(Activator.getLocalPreferenceStore().getBoolean(MOBILE_REGION_SETTING));
 		man.add(immobileWhenAdded);
 		man.add(new Separator());
 		man.add(getReselectAction());
 		man.add(new Separator());
 		super.createActions();
 	}

	@Override
	protected String getRegionTypeName() {
		return "region";
	}
 	
 	@Override
 	public void regionAdded(RegionEvent evt) {
 		if (!isActive()) return;
 		super.regionAdded(evt);
 		
 		boolean isMobile = Activator.getLocalPreferenceStore().getBoolean(MOBILE_REGION_SETTING);
 		evt.getRegion().setMobile(isMobile);
 	}
 
 	protected void createNewRegion(boolean force) {
 		try {
 			if (!force) {
 				// We check to see if the region type preferred is already there
 				final Collection<IRegion> regions = getPlottingSystem().getRegions();
 				for (IRegion iRegion : regions) {
 					if (iRegion.isUserRegion() && iRegion.isVisible()) {
 						// We have one already, do not go into create mode :)
 						if (iRegion.getRegionType() == IRegion.RegionType.BOX) return;
 					}
 				}
 			}
 
 			getPlottingSystem().createRegion(RegionUtils.getUniqueName("Region", getPlottingSystem()), IRegion.RegionType.BOX);
 		} catch (Exception e) {
 			logger.error("Cannot create line region for selecting in measurement tool!", e);
 		}
 	}
 
 	/**
 	 * EditingSupport Class
 	 *
 	 */
 }
