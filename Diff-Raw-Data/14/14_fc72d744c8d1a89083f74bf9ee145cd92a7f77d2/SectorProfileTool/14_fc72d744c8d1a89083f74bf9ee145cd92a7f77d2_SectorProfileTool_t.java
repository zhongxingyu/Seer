 package org.dawb.workbench.plotting.tools;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.dawb.common.ui.menu.CheckableActionGroup;
 import org.dawb.common.ui.menu.MenuAction;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.plot.region.IRegion;
 import org.dawb.common.ui.plot.region.IRegion.RegionType;
 import org.dawb.common.ui.plot.region.IRegionListener;
 import org.dawb.common.ui.plot.region.ROIEvent;
 import org.dawb.common.ui.plot.region.RegionEvent;
 import org.dawb.common.ui.plot.trace.IImageTrace;
 import org.dawb.common.ui.plot.trace.ILineTrace;
 import org.dawb.common.ui.plot.trace.ITrace;
 import org.dawb.workbench.plotting.Activator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Display;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 
 public abstract class SectorProfileTool extends ProfileTool {
 
 
 	private MenuAction      center;
 	private IRegionListener sectorRegionListener;
 
 	@Override
 	protected void configurePlottingSystem(AbstractPlottingSystem plotter) {
 
 		// We will add an action here for centering the sector.
 		this.center = new MenuAction("Center selection");
 		center.setImageDescriptor(Activator.getImageDescriptor("icons/sector-center-menu.png"));
 		
 		getSite().getActionBars().getToolBarManager().add(center);
		getSite().getActionBars().getMenuManager().add(center);
 		updateSectors();
 		
 		this.sectorRegionListener  = new IRegionListener.Stub() {
 			
 			@Override
 			public void regionsRemoved(RegionEvent evt) {
 				updateSectors();
 			}
 			
 			@Override
 			public void regionRemoved(RegionEvent evt) {
 				updateSectors();
 			}
 			@Override
 			public void regionAdded(RegionEvent evt) {
 				updateSectors();
 				if (evt.getRegion()!=null && evt.getRegion().getRegionType()==RegionType.SECTOR) {
 					SectorROI sroi = (SectorROI)evt.getRegion().getROI().copy();
 					sroi.setSymmetry(preferredSymmetry);
 					sroi.setCombineSymmetry(preferredCombine);
 					evt.getRegion().setROI(sroi);
 				}
 			}
 		};
 		
 		final MenuAction symmetry = new MenuAction("Symmetry setting");
 		symmetry.setImageDescriptor(Activator.getImageDescriptor("icons/sector-symmetry-menu.png"));
 		getSite().getActionBars().getToolBarManager().add(symmetry);
		getSite().getActionBars().getMenuManager().add(symmetry);
 		
 		addSymetryActions(symmetry);
 		
 	}
 	
 	private int lastSymmetry = 0;
 	@Override
 	public void roiChanged(ROIEvent evt) {
 		
 		if (evt.getROI()!=null && evt.getROI() instanceof SectorROI) {
 			SectorROI sroi = (SectorROI)evt.getROI();
 			if (sroi.getSymmetry()!=lastSymmetry) { // New plots required
 				profilePlottingSystem.clear();
 			}
 			lastSymmetry = sroi.getSymmetry();
 		}
 
         super.roiChanged(evt);
 	}
 	
 	private int     preferredSymmetry = SectorROI.NONE;
 	private boolean preferredCombine  = false;
 	
 	private void addSymetryActions(final MenuAction symmetry) {
 		
 		final CheckableActionGroup group = new CheckableActionGroup();
 		for (int isymetry = 0; isymetry < 7; isymetry++) {
 
 			final int finalSym = isymetry;
 			
 			final Action action = new Action(SectorROI.getSymmetryText(isymetry), IAction.AS_CHECK_BOX) {
 				@Override
 				public void run() {
 					
 					preferredSymmetry = finalSym;
 					final Collection<IRegion> regions = getPlottingSystem().getRegions(RegionType.SECTOR);
 					
 					Collection<IRegion> notPossible = new ArrayList<IRegion>(3);
 					if (regions!=null) for (IRegion iRegion : regions) {
 						SectorROI roi = (SectorROI)iRegion.getROI();
 						
 						//if (roi.checkSymmetry(finalSym)) {
 							roi = roi.copy();
 							roi.setSymmetry(finalSym);
 							iRegion.setROI(roi);
 						//} else {						
 						//	notPossible.add(iRegion);
 						//}
 					}
 					
 					update(null, null, false);
 
 					if (notPossible.size()>0) {
 						MessageDialog.openError(Display.getDefault().getActiveShell(), "Cannot use symmetry",
 								                "The region"+(notPossible.size()==1?": '":"s: '")+notPossible.toString().substring(1,notPossible.toString().length()-2)+"'\n"+
 								                "cannot have "+(notPossible.size()==1?"its":"their")+" symmetry set to "+SectorROI.getSymmetryText(finalSym)+
 								                "\n\nPlease try a different symmetry for "+(notPossible.size()==1?"it.":"them."));
 					}
 				}
 			};	
             group.add(action);
 			symmetry.add(action);
 		}
 	
 		symmetry.getAction(0).setChecked(true);
 		
 		final Action combine = new Action("Combine symmetry", IAction.AS_CHECK_BOX) {
 			@Override
 			public void run() {
 				preferredCombine = isChecked();
 				
 				final Collection<IRegion> regions = getPlottingSystem().getRegions(RegionType.SECTOR);
 				if (regions!=null) for (IRegion iRegion : regions) {
 					SectorROI roi = (SectorROI)iRegion.getROI();
 					roi = roi.copy();
 					roi.setCombineSymmetry(isChecked());
 					iRegion.setROI(roi);
 				}
 				
 				update(null, null, false);
 			    
 			}
 		};
 		combine.setImageDescriptor(Activator.getImageDescriptor("icons/sector-symmetry-combine.png"));
 		getSite().getActionBars().getToolBarManager().add(combine);
		getSite().getActionBars().getMenuManager().add(combine);
 	}
 
 	public void activate() {
 		super.activate();
 		
 		if (getPlottingSystem()!=null && sectorRegionListener!=null) {
 			getPlottingSystem().addRegionListener(sectorRegionListener);
 		}
 	}
 	
 	public void deactivate() {
 		super.deactivate();
 		if (getPlottingSystem()!=null) {
 			getPlottingSystem().removeRegionListener(sectorRegionListener);
 		}
 	}
 	
 	private void updateSectors() {
 		
 		if (getPlottingSystem()==null) return;
 		
 		center.clear();
 		
 		final Collection<IRegion> regions = getPlottingSystem().getRegions();
 		if (regions!=null) for (final IRegion region : regions) {
 			if (isRegionTypeSupported(region.getRegionType())) {
 				
 				final Action centerRegion = new Action("Center sector '"+region.getName()+"'") {
 					public void run() {
 						center.setSelectedAction(this);
 						final double[] cen = getBeamCenter();
 						if (cen!=null) {
 							final SectorROI sroi = (SectorROI)region.getROI();
 							sroi.setPoint(cen);
 							region.setROI(sroi);
 							
 							center.setSelectedAction(this);
 						}
 					}
 				};
 				centerRegion.setImageDescriptor(Activator.getImageDescriptor("icons/sector-center-action.png"));
 			
 				center.add(centerRegion);
 			}
 		}
 		
 		if (center.size()>0) center.setSelectedAction(0);
 			
 		// TODO likely to cause flicker
 		getSite().getActionBars().getToolBarManager().update(true);
		getSite().getActionBars().getMenuManager().update(true);
 	}
 
 	protected double[] getBeamCenter() {
 		
         IMetaData meta = getMetaData();
         if (meta==null || !(meta instanceof IDiffractionMetadata)) {
         	return getImageCenter();
         }
          	
         IDiffractionMetadata dm = (IDiffractionMetadata)meta;
         
         if (dm.getDetector2DProperties()==null) return getImageCenter();
         
         return dm.getDetector2DProperties().getBeamLocation();
  	}
 
 	private double[] getImageCenter() {
     	final AbstractDataset image = getImageTrace().getData();
     	return new double[]{image.getShape()[1]/2d, image.getShape()[0]/2d};
 	}
 
 	protected abstract AbstractDataset[] getXAxis(final SectorROI sroi, final AbstractDataset[] integrals);
 	
 	/**
 	 * Please name the integral the same as the name you would like to plot.
 	 * 
 	 * @param data
 	 * @param mask
 	 * @param sroi
 	 * @param region
 	 * @param isDrag
 	 * @return either array size 1 or 2. If 2, 2 plots are created on the profile
 	 */
 	protected abstract AbstractDataset[] getIntegral( final AbstractDataset data,
 										              final AbstractDataset mask, 
 										              final SectorROI       sroi, 
 						                              final IRegion         region,
 										              final boolean         isDrag);
 
 
 	@Override
 	protected void createProfile(IImageTrace  image, 
 			                     IRegion      region, 
 			                     ROIBase      rbs, 
 			                     boolean      tryUpdate,
 			                     boolean      isDrag,
 			                     IProgressMonitor monitor) {
         
 		if (monitor.isCanceled()) return;
 		if (image==null) return;
 		
 		if (!isRegionTypeSupported(region.getRegionType())) return;
 
 		final SectorROI sroi = (SectorROI) (rbs==null ? region.getROI() : rbs);
 		if (sroi==null) return;
 		if (!region.isVisible()) return;
 
 		if (monitor.isCanceled()) return;
 		
 		final AbstractDataset data = isDrag ? image.getDownsampled()     : image.getData();
 		final AbstractDataset mask = isDrag ? image.getDownsampledMask() : image.getMask();
 		
 		SectorROI downsroi = null;
 		if (isDrag) {
 			downsroi = sroi.copy();
 			downsroi.downsample(image.getDownsampleBin());
 		}
 			
 		final AbstractDataset[] integrals = getIntegral(data, mask, isDrag ? downsroi : sroi, region, isDrag);	
         if (integrals==null) return;
 				
 		final AbstractDataset[] xis = getXAxis(sroi, integrals);
 
 		for (int i = 0; i < integrals.length; i++) {
 			final AbstractDataset integral = integrals[i];
 			final AbstractDataset xi       = xis[i];
 			
 			final ILineTrace x_trace = (ILineTrace)profilePlottingSystem.getTrace(integral.getName());
 			if (tryUpdate && x_trace!=null) {
 				getControl().getDisplay().syncExec(new Runnable() {
 					public void run() {
 						x_trace.setData(xi, integral);
 					}
 				});
 
 			} else {
 
 				Collection<ITrace> plotted = profilePlottingSystem.createPlot1D(xi, Arrays.asList(new AbstractDataset[]{integral}), monitor);
 				registerTraces(region, plotted);			
 			}
 		}
 
 	}
 	
 	@Override
 	protected boolean isRegionTypeSupported(RegionType type) {
 		return type==RegionType.SECTOR || type==RegionType.RING;
 	}
 
 	@Override
 	protected RegionType getCreateRegionType() {
 		return RegionType.SECTOR;
 	}
 
 }
