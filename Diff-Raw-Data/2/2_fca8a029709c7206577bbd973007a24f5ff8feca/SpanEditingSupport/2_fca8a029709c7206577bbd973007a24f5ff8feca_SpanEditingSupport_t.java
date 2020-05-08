 package org.dawnsci.slicing.component;
 
 import org.dawnsci.common.widgets.celleditor.SpinnerCellEditor;
 import org.dawnsci.slicing.api.system.DimsData;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.LazyDataset;
 
 public class SpanEditingSupport extends EditingSupport {
 	
 	private static final Logger logger = LoggerFactory.getLogger(SpanEditingSupport.class);
 
 	private SliceSystemImpl system;
 
 	public SpanEditingSupport(final SliceSystemImpl system, ColumnViewer viewer) {
 		super(viewer);
 		this.system = system;
 	}
 
 	@Override
 	protected CellEditor getCellEditor(final Object element) {
 		try {
 			final  SpinnerCellEditor ret = new SpinnerCellEditor(((TableViewer)getViewer()).getTable(), SWT.BORDER);
 			final DimsData data = (DimsData)element;
 			int dimension = data.getDimension();
 			ret.setMinimum(1);
			final int max = (int)Math.round(LazyDataset.getMaxSliceLength(system.getData().getLazySet(), dimension)/4d);
 			ret.setMaximum(max);
 			ret.getSpinner().setToolTipText("The maximum value of a slice of dimension '"+(dimension+1)+"' is '"+max+"',\nbased on available memory.");
 			
 			ret.getSpinner().addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 			        data.setSliceSpan(ret.getSpinner().getSelection());
 			        TableViewer tviewer = (TableViewer)getViewer();
 			       
 			        tviewer.update(data, new String[]{"Slice"});
 			        if (system.synchronizeSliceData(data)) system.slice(false);
 				}				
 			});
 
 		    return ret;
 		} catch (Exception ne) {
 			logger.error("Cannot set bounds of spinner, invalid data!", ne);
 			return null;
 		}
 	}
 
 	@Override
 	protected boolean canEdit(Object element) {
 		final DimsData data = (DimsData)element;
 		return system.isAdvanced() && data.getPlotAxis().isAdvanced();
 	}
 
 	@Override
 	protected Object getValue(Object element) {
 		final DimsData data = (DimsData)element;
 		return data.getSliceSpan();
 	}
 
 	@Override
 	protected void setValue(Object element, Object value) {
 		final DimsData data = (DimsData)element;
         data.setSliceSpan((Integer)value);
         system.update(data, false);
 	}
 
 }
