 package org.dawnsci.slicing.component;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.services.IExpressionObject;
 import org.dawb.common.services.IExpressionObjectService;
 import org.dawb.common.ui.preferences.ViewConstants;
 import org.dawb.hdf5.HierarchicalDataFactory;
 import org.dawb.hdf5.nexus.NexusUtils;
 import org.dawnsci.common.widgets.celleditor.CComboCellEditor;
 import org.dawnsci.slicing.api.Activator;
 import org.dawnsci.slicing.api.system.AxisChoiceEvent;
 import org.dawnsci.slicing.api.system.DimsData;
 import org.dawnsci.slicing.api.system.DimsDataList;
 import org.dawnsci.slicing.api.system.AxisType;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.SliceObject;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 class AxisEditingSupport extends EditingSupport {
 	
 	private static final Logger logger = LoggerFactory.getLogger(AxisEditingSupport.class);
 
 	private CComboCellEditor axisDataEditor;
 	private SliceSystemImpl system;
 	/**
 	 * 1 is first dimension, map of names available for axis, including indices.
 	 */
 	private Map<Integer, List<String>> dimensionNames;
 
 	public AxisEditingSupport(final SliceSystemImpl system, ColumnViewer viewer) {
 		
 		super(viewer);
 		this.system          = system;
 		this.dimensionNames  = new HashMap<Integer,List<String>>(5);
 		
 		this.axisDataEditor = new CComboCellEditor(((TableViewer)viewer).getTable(), new String[]{"indices"}, SWT.READ_ONLY) {
 			protected int getDoubleClickTimeout() {
 				return 0;
 			}		
 			
 			public void activate() {
 				final DimsData     data  = (DimsData)((IStructuredSelection)((TableViewer)getViewer()).getSelection()).getFirstElement();
 				final int idim  = data.getDimension()+1;
 				final List<String> names = dimensionNames.get(idim);
 				final String[] items = names.toArray(new String[names.size()]);
 				
 				if (!Arrays.equals(this.getCombo().getItems(), items)) {
 					this.getCombo().setItems(items);
 				}
 				
 				final int isel = names.indexOf(system.getCurrentSlice().getAxisName(idim));
 				if (isel>-1 && getCombo().getSelectionIndex()!=isel) {
 					this.getCombo().select(isel);
 				}
 				super.activate();
 				
 			}
 		};
 		
 	}
 	@Override
 	protected CellEditor getCellEditor(Object element) {
 		
 		return axisDataEditor;
 	}
 
 	@Override
 	protected boolean canEdit(Object element) {
 		final DimsData data = (DimsData)element;
 	    boolean isSliceIndex = Activator.getDefault().getPreferenceStore().getInt(SliceConstants.SLICE_EDITOR)==1;
 		return isSliceIndex ? data.getPlotAxis()==AxisType.SLICE : true;
 	}
 
 	@Override
 	protected Object getValue(Object element) {
 		final DimsData data = (DimsData)element;
 		final int idim  = data.getDimension()+1;
 		final String dimensionDataName = system.getCurrentSlice().getAxisName(idim);
 		final List<String> names = dimensionNames.get(idim);
 		int selection = names.indexOf(dimensionDataName);
 		return selection>-1 ? selection : 0;
 	}
 
 	@Override
 	protected void setValue(final Object element, final Object value) {
 		
 		final DimsData data = (DimsData)element;
 		final int idim  = data.getDimension()+1;
 
 		String axisName = null;
 		if (value instanceof Integer) {
 			final List<String> names = dimensionNames.get(idim);
 			axisName = names.get(((Integer)value).intValue());
 		} else {
 			axisName = (String)value;
 		}
 		system.getCurrentSlice().setAxisName(idim, axisName);
 		system.update(data, true);
 		system.fireAxisChoiceListeners(new AxisChoiceEvent(system, data.getDimension(), axisName));
 
 	}
 
 	protected void updateAxesChoices() {
 		dimensionNames.clear();
 		final DimsDataList ddl = system.getDimsDataList();
 		for (int idim =1; idim<=ddl.size(); ++idim) {
 			updateAxis(idim);
 		}
 	}
 	/**
 	 * 
 	 * @param idim 1 based index of axis.
 	 */
 	private void updateAxis(int idim) {
 		
 		final SliceObject  sliceObject = system.getCurrentSlice();
 		try {    	
 			final DimsDataList ddl     = system.getDimsDataList();
 				
 			List<String> names = new ArrayList<String>(7);
 			
 			boolean isHDF5 = HierarchicalDataFactory.isHDF5(sliceObject.getPath());
 			if (isHDF5) try {
 				if (sliceObject.getPath()!=null && sliceObject.getName()!=null) {
 				    names.addAll(NexusUtils.getAxisNames(sliceObject.getPath(), sliceObject.getName(), idim));
 				}
 			} catch (Throwable ne) {
 				if (!ddl.isExpression()) throw ne; // Expressions, we don't care that
 				                                            // cannot read nexus
 			}
 			
 			if (!ddl.isExpression()) {				
 				// We add any datasets in the DataHolder which are the right size to be this
 				// axis.
 				names.addAll(getNonNexusDataAxes(idim, names));
 			}
 
 			
 			// Add any expressions 
 	    	final IExpressionObjectService service = (IExpressionObjectService)PlatformUI.getWorkbench().getService(IExpressionObjectService.class);
 	        final List<IExpressionObject>  exprs   = service.getActiveExpressions(sliceObject.getPath());
 
 	        if (exprs!=null) {
 	        	final ILazyDataset lazySet = system.getData().getLazySet();
 				final int size = lazySet.getShape()[idim-1];
 				
 				for (IExpressionObject iExpressionObject : exprs) {
 					final ILazyDataset set = iExpressionObject.getLazyDataSet(iExpressionObject.getExpressionName(), new IMonitor.Stub());
 					if (set.getRank()==1 && set.getSize()==size){
 						final String name = iExpressionObject.getExpressionName()+" [Expression]";
 						names.add(name);
 						final IDataset axisData = iExpressionObject.getDataSet(iExpressionObject.getExpressionName(), new IMonitor.Stub());
 						sliceObject.putExpressionAxis(name, axisData);
 					}
 				}				
 	        }
 
 	        // indices, last but not least.
 			names.add("indices");
 			dimensionNames.put(idim, names);
 			
 			final String dimensionName = sliceObject.getAxisName(idim);
 			if (!names.contains(dimensionName)) {
 				// We get an axis not used elsewhere for the default
 				final Map<Integer,String> others = new HashMap<Integer,String>(sliceObject.getAxisNames());
 				others.keySet().removeAll(Arrays.asList(idim));
 				boolean found = false;
 				Collection<String> values = others.values();
 				for (String n : names) {
 					if (!values.contains(n)) {
 						sliceObject.setAxisName(idim, n);
 						found = true;
 						break;
 					}
 				}
 				if (!found) {
 					sliceObject.setAxisName(idim, "indices");
 					//dimensionNames.put(idim, Arrays.asList("indices"));
 				}
 			}
 			
 
 			
 		} catch (Throwable e) {
 			logger.info("Cannot assign axes!", e);
 			sliceObject.setAxisName(idim, "indices");
 			dimensionNames.put(idim, Arrays.asList("indices"));
 			
 		}
 	}
 	
 	/**
 	 * 
 	 * @param idim
 	 * @return empty list if none of right size or list of all the right size otherwise
 	 */
 	private List<String> getNonNexusDataAxes(int idim, List<String> alreadyFound) {
 		
 		final List<String> ret = new ArrayList<String>(3);
 		if (system.getData().getVariableManager()==null) return ret;
 		
 		final int size = system.getData().getLazySet().getShape()[idim-1];
 
         for (String dataName : system.getData().getVariableManager().getDataNames()) {
         	if (alreadyFound.contains(dataName)) continue;
			final ILazyDataset set = system.getData().getVariableManager().getLazyValue(dataName, new IMonitor.Stub());
 			if (set!=null && set.getRank()==1 && set.getSize()==size) {
 				ret.add(dataName);
 			}
 		}
 		
 		return ret;
 	}
 	
 
 }
