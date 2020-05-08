 package org.dawnsci.slicing.component;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
import org.dawnsci.slicing.Activator; // On purpose! Gets preference from expected place.
 import org.dawb.common.ui.components.cell.ScaleCellEditor;
 import org.dawnsci.common.widgets.celleditor.SpinnerCellEditorWithPlayButton;
 import org.dawnsci.slicing.api.system.AxisType;
 import org.dawnsci.slicing.api.system.DimsData;
 import org.dawnsci.slicing.api.util.SliceUtils;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Scale;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 
 
 class SliceEditingSupport extends EditingSupport {
 
 	/**
 	 * Format used to show value in nexus axes
 	 */
 	private NumberFormat format;
 
 	private ScaleCellEditor                 scaleEditor;
 	private SpinnerCellEditorWithPlayButton spinnerEditor;
 	private TextCellEditor                  rangeEditor;
 	
 	private SliceSystemImpl                 system;
 
 	public SliceEditingSupport(final SliceSystemImpl system, ColumnViewer viewer) {
 		
 		super(viewer);
 		this.system = system;
 		this.format = DecimalFormat.getNumberInstance();
 		
 		scaleEditor = new ScaleCellEditor((Composite)viewer.getControl(), SWT.NO_FOCUS);
 		final Scale scale = (Scale)scaleEditor.getControl();
 		scale.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 		scaleEditor.setMinimum(0);
 		scale.setIncrement(1);
 		scale.addMouseListener(new MouseAdapter() {			
 			@Override
 			public void mouseUp(MouseEvent e) {
 				if (!system.is3D()) return;
 				updateSlice(true);
 			}
 		});
 		scaleEditor.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				updateSlice(!system.is3D());
 			}
 		});
 		
 		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.workbench.ui");
 		spinnerEditor = new SpinnerCellEditorWithPlayButton((TableViewer)viewer, "Play through slices", store.getInt("data.format.slice.play.speed"));
 		spinnerEditor.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
 		spinnerEditor.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
                 final DimsData data  = (DimsData)((IStructuredSelection)((TableViewer)getViewer()).getSelection()).getFirstElement();
                 final int value = ((Spinner)e.getSource()).getSelection();
                 data.setSlice(value);
                 data.setSliceRange(null);
          		if (system.synchronizeSliceData(data)) system.slice(false);
 			}
 			
 		});
 
 		rangeEditor = new TextCellEditor((Composite)viewer.getControl(), SWT.NONE);
 		((Text)rangeEditor.getControl()).addModifyListener(new ModifyListener() {			
 			@Override
 			public void modifyText(ModifyEvent e) {
                 final DimsData data  = (DimsData)((IStructuredSelection)((TableViewer)getViewer()).getSelection()).getFirstElement();
 				final Text text = (Text)e.getSource();
 				final String range = text.getText();
 				
 				final Matcher matcher = Pattern.compile("(\\d+)\\:(\\d+)").matcher(range);
 				if ("all".equals(range)) {
 					text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
 				} else if (matcher.matches()) {
 					final int[] shape = system.getLazyDataset().getShape();
 					int start = Integer.parseInt(matcher.group(1));
 					int end   = Integer.parseInt(matcher.group(2));
 					if (start>-1&&end>-1&&start<shape[data.getDimension()]&&end<shape[data.getDimension()]) {
 					    text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
 					} else {
 						text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
 					}
 				} else {
 					text.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
 				}			
 			}
 		});
 		((Text)rangeEditor.getControl()).setToolTipText("Please enter \"all\" or a range of the form <int>:<int>.");
 
 	}
 	
 	protected void updateSlice(boolean doSlice) {
 		final DimsData data  = (DimsData)((IStructuredSelection)getViewer().getSelection()).getFirstElement();
 		final Scale scale = (Scale)scaleEditor.getControl();
 		final int value = scale.getSelection();
 		data.setSlice(value);
 		data.setSliceRange(null);
 		scale.setToolTipText(getScaleTooltip(data, scale.getMinimum(), scale.getMaximum()));		
 		if (doSlice&&system.synchronizeSliceData(data)) system.slice(false);
 	}
 	
 	protected String getScaleTooltip(DimsData data, int minimum, int maximum) {
 		
 		int value = data.getSlice();
         final StringBuffer buf = new StringBuffer();
         
         IDataset axis = null;
         if (system.isAxesVisible()) try {
             axis = SliceUtils.getAxis(system.getCurrentSlice(), system.getData().getVariableManager(), data, null); 
         } catch (Throwable ne) {
         	axis = null;
         }
         
         String min = String.valueOf(minimum);
         String max = String.valueOf(maximum);
         String val = String.valueOf(value);
         
         int ispan = value+data.getSliceSpan();
         if (ispan>=maximum) ispan = maximum;
         String span= String.valueOf(value+data.getSliceSpan());
         try {
 	        if (axis!=null) {
 				min = format.format(axis.getDouble(minimum));
 				max = format.format(axis.getDouble(maximum));
 				val = format.format(axis.getDouble(value));
 				if (data.getPlotAxis().isAdvanced()) {
 					span = format.format(axis.getDouble(ispan));
 				}
 	        } 
         } catch (Throwable ignored) {
         	// Use indices
         }
     
         if (data.getPlotAxis().isAdvanced()) {
         	buf.append(data.getPlotAxis().getName());
         	buf.append("(");
         	buf.append(val);
         	buf.append(":");
         	buf.append(span);
         	buf.append(")");
         } else {
             buf.append(min);
             buf.append(" <= ");
             buf.append(val);
             buf.append(" <= ");
             buf.append(max);
         }
         return buf.toString();
 	}
 
 
 	@Override
 	protected CellEditor getCellEditor(Object element) {
 		
 		int[] dataShape = system.getLazyDataset().getShape();
 		final DimsData data = (DimsData)element;
 		if (data.isTextRange()) return rangeEditor;
 		if (Activator.getDefault().getPreferenceStore().getInt(SliceConstants.SLICE_EDITOR)==1) {
             spinnerEditor.setMaximum(dataShape[data.getDimension()]-1);
 		    return spinnerEditor;
 		} else {
 			final Scale scale = (Scale)scaleEditor.getControl();
 			scale.setMaximum(dataShape[data.getDimension()]-1);
 			scale.setPageIncrement(scale.getMaximum()/10);
 
 			scale.setToolTipText(getScaleTooltip(data, scale.getMinimum(), scale.getMaximum()));
 			return scaleEditor;
 		}
 	}
 
 	@Override
 	protected boolean canEdit(Object element) {
 		final DimsData data = (DimsData)element;
 		final int[] dataShape = system.getLazyDataset().getShape();
 		if (dataShape[data.getDimension()]<2) return false;
 		if (data.isTextRange()) return true;
 		return data.getPlotAxis()==AxisType.SLICE || data.getPlotAxis().isAdvanced();
 	}
 
 	@Override
 	protected Object getValue(Object element) {
 		final DimsData data = (DimsData)element;
 		if (data.isTextRange()) return data.getSliceRange() != null ? data.getSliceRange() : "all";
 		return data.getSlice();
 	}
 
 	@Override
 	protected void setValue(Object element, Object value) {
 		final DimsData data = (DimsData)element;
 		if (value instanceof Integer) {
 			data.setSlice((Integer)value);
 		} else {
 			data.setSliceRange((String)value);
 		}
 		system.update(data, true);
 	}
 
 	void setPlayButtonVisible(boolean vis) {
 		spinnerEditor.setPlayButtonVisible(vis);
 	}
 }
