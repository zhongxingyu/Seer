 package org.padacore.ui.views.extvar;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ComboBoxCellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.padacore.core.gnat.GnatAdaProject;
 import org.padacore.core.gnat.Scenario;
 import org.padacore.core.gnat.ScenarioItem;
 
 public class ValueEditingSupport extends EditingSupport {
 
	private final TableViewer viewer;
 	private final Scenario scenario;
 
 	public ValueEditingSupport(TableViewer viewer, Scenario scenario) {
 		super(viewer);
		this.viewer = viewer;
 		this.scenario = scenario;
 	}
 
 	@Override
 	protected CellEditor getCellEditor(Object element) {
		Composite parent = (Composite) this.viewer.getTable();
 		ScenarioItem var = (ScenarioItem) element;
 		CellEditor res;
 		if (var.isTyped()) {
			res = new ComboBoxCellEditor(parent, this.getValuesAsArray(var), SWT.READ_ONLY
					| SWT.BORDER);
 		} else {
 			res = new TextCellEditor(parent);
 		}
 		return res;
 	}
 
 	@Override
 	protected boolean canEdit(Object element) {
 		return true;
 	}
 
 	@Override
 	protected Object getValue(Object element) {
 		ScenarioItem var = (ScenarioItem) element;
 		Object res;
 		if (var.isTyped()) {
 			res = this.getValues(var).indexOf(var.getValue());
 		} else {
 			res = var.getValue();
 		}
 		return res;
 	}
 
 	@Override
 	protected void setValue(Object element, Object value) {
 		ScenarioItem var = (ScenarioItem) element;
 		String valueToSet;
 
 		if (var.isTyped()) {
 			valueToSet = (String) this.getValues(var).get((int) value);
 		} else {
 			valueToSet = (String) value;
 		}
 		this.scenario.setExternalVariableValueFor(this.getProject(), var.getName(), valueToSet);
		this.viewer.refresh();
 	}
 
 	private GnatAdaProject getProject() {
		return (GnatAdaProject) this.viewer.getInput();
 	}
 
 	private List<String> getValues(ScenarioItem var) {
 		return new ArrayList<>(var.getType().getValues());
 	}
 
 	private String[] getValuesAsArray(ScenarioItem var) {
 		Set<String> values = var.getType().getValues();
 		String[] res = new String[values.size()];
 		values.toArray(res);
 		return res;
 	}
 }
