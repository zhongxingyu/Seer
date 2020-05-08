 package org.dawnsci.plotting.tools.region;
 
 import org.dawnsci.common.widgets.celleditor.FloatSpinnerCellEditor;
 import org.dawnsci.plotting.api.axis.ICoordinateSystem;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.RegionUtils;
 import org.dawnsci.plotting.tools.region.MeasurementLabelProvider.LabelType;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.CheckboxCellEditor;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Composite;
 
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 /**
  * 
  * @author fcp94556
  *
  */
 public class RegionEditTool extends AbstractRegionTableTool {
 
 	private int precision = 3;
 
 	@Override
 	public ToolPageRole getToolPageRole() {
 		return ToolPageRole.ROLE_2D;
 	}
 
 	protected void createColumns(final TableViewer viewer) {
 		
 		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);
 
 		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
 		var.getColumn().setText("Name");
 		var.getColumn().setWidth(120);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ROINAME));
 		RegionEditingSupport regionEditor = new RegionEditingSupport(viewer, 0);
 		var.setEditingSupport(regionEditor);
 
 		var = new TableViewerColumn(viewer, SWT.CENTER, 1);
 		var.getColumn().setText("Start point x");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.STARTX));
 		regionEditor = new RegionEditingSupport(viewer, 1);
 		var.setEditingSupport(regionEditor);
 
 		var = new TableViewerColumn(viewer, SWT.LEFT, 2);
 		var.getColumn().setText("Start point y");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.STARTY));
 		regionEditor = new RegionEditingSupport(viewer, 2);
 		var.setEditingSupport(regionEditor);
 
 		var = new TableViewerColumn(viewer, SWT.LEFT, 3);
 		var.getColumn().setText("End Point x");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ENDX));
 		regionEditor = new RegionEditingSupport(viewer, 3);
 		var.setEditingSupport(regionEditor);
 
 		var = new TableViewerColumn(viewer, SWT.LEFT, 4);
 		var.getColumn().setText("End point y");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ENDY));
 		regionEditor = new RegionEditingSupport(viewer, 4);
 		var.setEditingSupport(regionEditor);
 
 		var = new TableViewerColumn(viewer, SWT.LEFT, 5);
 		var.getColumn().setText("Max Intensity");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.MAX));
 		regionEditor = new RegionEditingSupport(viewer, 5);
 		var.setEditingSupport(regionEditor);
 
 		var = new TableViewerColumn(viewer, SWT.LEFT, 6);
 		var.getColumn().setText("Sum");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.SUM));
 		regionEditor = new RegionEditingSupport(viewer, 6);
 		var.setEditingSupport(regionEditor);
 
 		var = new TableViewerColumn(viewer, SWT.LEFT, 7);
 		var.getColumn().setText("Active");
 		var.getColumn().setWidth(100);
 		var.setLabelProvider(new MeasurementLabelProvider(this, LabelType.ACTIVE));
 		regionEditor = new RegionEditingSupport(viewer, 7);
 		var.setEditingSupport(regionEditor);
 	}
 	
 	
 	protected void createNewRegion() {
 		try {
 			getPlottingSystem().createRegion(RegionUtils.getUniqueName("Region", getPlottingSystem()), IRegion.RegionType.BOX);
 		} catch (Exception e) {
 			logger.error("Cannot create line region for selecting in measurement tool!", e);
 		}
 	}
 
 	/**
 	 * EditingSupport Class
 	 *
 	 */
 	private class RegionEditingSupport extends EditingSupport {
 
 		private int column;
 
 		public RegionEditingSupport(ColumnViewer viewer, int col) {
 			super(viewer);
 			this.column = col;
 		}
 		@Override
 		protected CellEditor getCellEditor(final Object element) {
 			CellEditor ed = null;
 			
 			if(column > 0 && column < 7){
 				final FloatSpinnerCellEditor fse = new FloatSpinnerCellEditor((Composite)getViewer().getControl(), SWT.RIGHT);
 				fse.setFormat(7, precision);
 				fse.setMaximum(Double.MAX_VALUE);
 				fse.setMinimum(-Double.MAX_VALUE);
 				fse.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent e) {
 						try {
 							setValue(element, fse.getValue(), false);
 						} catch (Exception e1) {
 							logger.debug("Error while setting table value");
 							e1.printStackTrace();
 						}
 						
 					}
 				});
 				return fse;
 			} else if(column == 0){
 				ed = new TextCellEditor(((TableViewer)getViewer()).getTable(), SWT.RIGHT);
 				
 				return ed;
 			} else if(column == 7){
 				ed = new CheckboxCellEditor(((TableViewer)getViewer()).getTable(), SWT.RIGHT);
 				return ed;
 			}
 			return null;
 			
 		}
 
 		@Override
 		protected boolean canEdit(Object element) {
 			if (column == 5 || column == 6) return false;
 			else return true;
 		}
 
 		@Override
 		protected Object getValue(Object element) {
 			final IRegion region = (IRegion)element;
 			IROI roi = region.getROI();
 			ICoordinateSystem coords = region.getCoordinateSystem();
 			double[] startPoint = getAxisPoint(coords, roi.getPoint());
 			double[] endPoint = {0, 0};
 			if(roi instanceof RectangularROI){
 				endPoint = getAxisPoint(coords, ((RectangularROI)roi).getEndPoint());
 			} else if (roi instanceof LinearROI){
 				endPoint = getAxisPoint(coords, ((LinearROI)roi).getEndPoint());
 			}
 			switch (column){
 			case 0:
 				return region.getLabel();
 			case 1:
 				return startPoint[0];
 			case 2:
 				return startPoint[1];
 			case 3:
 				return endPoint[0];
 			case 4:
 				return endPoint[1];
 			case 5: //max intensity
 				return null;
 			case 6: //sum
 				return null;
 			case 7: //isPlot
 				return region.isActive();
 //				return roi.isPlot();
 			default:
 				return null;
 			}
 			
 		}
 
 		@Override
 		protected void setValue(Object element, Object value) {
 			try {
 				this.setValue(element, value, true);
 			} catch (Exception e) {
 				logger.debug("Error while setting table value");
 				e.printStackTrace();
 			}
 		}
 		
 		private void setValue(Object element, Object value, boolean tableRefresh) throws Exception {
 
 			final IRegion region = (IRegion) element;
 			IROI myRoi = region.getROI();
 			ICoordinateSystem coords = region.getCoordinateSystem();
 			double[] roiStartPoint = getAxisPoint(coords, myRoi.getPoint());
 			double[] roiEndPoint = {0, 0};
 			if(myRoi instanceof RectangularROI){
 				roiEndPoint = getAxisPoint(coords, ((RectangularROI)myRoi).getEndPoint());
 			} else if (myRoi instanceof LinearROI){
 				roiEndPoint = getAxisPoint(coords, ((LinearROI)myRoi).getEndPoint());
 			}
 			switch (column){
 			case 0:
 				// takes care of renaming the region (label and key value in hash table)
 				if(!region.getName().equals((String)value))
 					getPlottingSystem().renameRegion(region, (String)value);
 				break;
 			case 1:
 				if(myRoi instanceof LinearROI){
 					LinearROI lroi = (LinearROI)myRoi;
 					double[] endPoint = lroi.getEndPoint();
 					double[] startPoint = getImagePoint(coords, new double[]{(Double)value, roiStartPoint[1]});
 					lroi.setPoint(startPoint);
 					lroi.setPoint((Double)value, lroi.getPointY());
 					lroi.setEndPoint(endPoint);
 					myRoi = lroi;
 				} else if(myRoi instanceof RectangularROI){
 					RectangularROI rroi = (RectangularROI)myRoi;
 					double[] endPoint = rroi.getEndPoint();
 					double[] startPoint = getImagePoint(coords, new double[]{(Double)value, roiStartPoint[1]});
 					rroi.setPoint(startPoint);
 					rroi.setLengths(endPoint[0] - startPoint[0], rroi.getLengths()[1]); //set new endpoint
 					myRoi = rroi;
 				}
 				break;
 			case 2:
 				if(myRoi instanceof LinearROI){
 					LinearROI lroi = (LinearROI)myRoi;
 					double[] endPoint = lroi.getEndPoint();
 					double[] startPoint = getImagePoint(coords, new double[]{roiStartPoint[0], (Double)value});
 					lroi.setPoint(startPoint);
 					lroi.setEndPoint(endPoint);
 					myRoi = lroi;
 				} else if(myRoi instanceof RectangularROI){
 					RectangularROI rroi = (RectangularROI)myRoi;
 					double[] endPoint = rroi.getEndPoint();
 					double[] startPoint = getImagePoint(coords, new double[]{roiStartPoint[0], (Double)value});
 					rroi.setPoint(startPoint);
 					rroi.setLengths(rroi.getLengths()[0], endPoint[1] - startPoint[1]); //set new endpoint
 					myRoi = rroi;
 				}
 				break;
 			case 3:
 				if(myRoi instanceof LinearROI){
 					double[] endPoint = getImagePoint(coords, 
 							new double[]{(Double)value, roiEndPoint[1]});
 					((LinearROI)myRoi).setEndPoint(endPoint);
 				}
 				else if (myRoi instanceof RectangularROI){
 					double[] endPoint = getImagePoint(coords, 
 							new double[]{(Double)value, roiEndPoint[1]});
 					((RectangularROI)myRoi).setEndPoint(endPoint);
 				}
 				break;
 			case 4:
 				if(myRoi instanceof LinearROI){
 					double[] endPoint = getImagePoint(coords, 
 							new double[]{roiEndPoint[0], (Double)value});
 					((LinearROI)myRoi).setEndPoint(endPoint);
 				}
 				else if (myRoi instanceof RectangularROI){
 					double[] endPoint = getImagePoint(coords, 
 							new double[]{roiEndPoint[0], (Double)value});
 					((RectangularROI)myRoi).setEndPoint(endPoint);
 				}
 				break;
 			case 5: //intensity
 				break;
 			case 6: //sum
 				break;
 			case 7: //isPlot
 				myRoi.setPlot((Boolean)value);
 				region.setActive((Boolean)value);
 				break;
 			default:
 				break;
 			}
 
 			if (tableRefresh) {
 				getViewer().refresh();
 			}
 
 			roi = myRoi;
 			IRegion myregion = getPlottingSystem().getRegion(region.getName());
 			if(myregion!= null)
 				myregion.setROI(myRoi);
 		}
 	}
 }
