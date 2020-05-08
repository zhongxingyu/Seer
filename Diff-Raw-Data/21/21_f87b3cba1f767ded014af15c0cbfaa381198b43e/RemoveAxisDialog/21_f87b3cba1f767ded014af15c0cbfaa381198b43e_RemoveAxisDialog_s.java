 package org.dawb.workbench.plotting.system.dialog;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dawb.common.ui.plot.IPlottingSystem;
 import org.dawb.common.ui.plot.axis.AxisUtils;
 import org.dawb.common.ui.plot.axis.IAxis;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 public class RemoveAxisDialog extends Dialog {
 
 	private IAxis           axis;
 	private IPlottingSystem system;
 
 	public RemoveAxisDialog(Shell parentShell, IPlottingSystem system) {
 		super(parentShell);
 		this.system = system;
 		setShellStyle(getShellStyle() | SWT.RESIZE);
 	}
 	@Override
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		newShell.setText("Remove Axis");
 	}
 	
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		final Composite content = new Composite(parent, SWT.NONE);
 		content.setLayoutData(new GridData(GridData.FILL_BOTH));
 		content.setLayout(new GridLayout(2, false));
 		
 		Label label = new Label(content, SWT.NONE);
 		label.setText("Title");
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
 		
 		final CCombo title = new CCombo(content, SWT.READ_ONLY|SWT.BORDER);
 		title.setItems(getAxisTitles());
 		title.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				final List<IAxis> axes = AxisUtils.getUserAxes(system);
 				axis = axes.get(title.getSelectionIndex());
 			}
 		});
 		title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		
 		return content;
 	}
 	
 	private String[] getAxisTitles() {
 		final List<IAxis>  axes   = AxisUtils.getUserAxes(system);
 		final List<String> titles = new ArrayList<String>(axes.size());
 		for (IAxis axis : axes) titles.add(axis.getTitle());
 		return titles.toArray(new String[titles.size()]);
 	}
 	
 	protected void createButtonsForButtonBar(Composite parent) {
 		// create OK and Cancel buttons by default
 		createButton(parent, IDialogConstants.OK_ID, "Delete", true);
 		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
 	}
 	public IAxis getAxis() {
 		return axis;
 	}
 	public void setAxis(IAxis axis) {
 		this.axis = axis;
 	}
 
 }
