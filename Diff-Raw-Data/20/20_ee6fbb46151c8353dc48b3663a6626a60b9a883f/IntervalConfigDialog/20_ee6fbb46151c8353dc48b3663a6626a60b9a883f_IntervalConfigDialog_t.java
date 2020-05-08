 package org.eclipse.tcf.te.tcf.processes.ui.internal.dialogs;
 
 import org.eclipse.jface.dialogs.StatusDialog;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public class IntervalConfigDialog extends StatusDialog implements SelectionListener {
 
 	public IntervalConfigDialog(Shell parent) {
 	    super(parent);
     }
 
 	@Override
     protected void configureShell(Shell shell) {
		shell.setText("Configure the refresh interval."); //$NON-NLS-1$
 		super.configureShell(shell);
     }
 
 	private Button button1;
 	private Button button2;
 	private ComboViewer comboViewer;
 	private Text text;
 	
 	@Override
     protected Control createDialogArea(Composite parent) {
 	    Composite composite = (Composite) super.createDialogArea(parent);
 	    Label label = new Label(composite, SWT.NONE);
	    label.setText("Configure the frequency to refresh the process list:"); //$NON-NLS-1$
 	    Composite comp1 = new Composite(composite, SWT.NONE);
 	    GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
 	    comp1.setLayoutData(data);
 	    GridLayout layout = new GridLayout(3, false);
 	    layout.horizontalSpacing = 0;
 	    comp1.setLayout(layout);
 	    button1 = new Button(comp1, SWT.RADIO);
	    button1.setText("Refresh the process list every"); //$NON-NLS-1$
 	    button1.addSelectionListener(this);
 	    text = new Text(comp1, SWT.SINGLE | SWT.BORDER);
 	    text.setTextLimit(Text.LIMIT);
 	    label = new Label(comp1, SWT.NONE);
	    label.setText(" seconds."); //$NON-NLS-1$
 	    Composite comp2 = new Composite(composite, SWT.NONE);
 	    data = new GridData(SWT.FILL, SWT.CENTER, true, false);
 	    comp2.setLayoutData(data);
 	    layout = new GridLayout(3, false);
 	    layout.horizontalSpacing = 0;
 	    comp2.setLayout(layout);
 	    button2 = new Button(comp2, SWT.RADIO);
	    button2.setText("Refresh the process list in a "); //$NON-NLS-1$
 	    button2.addSelectionListener(this);
 	    comboViewer = new ComboViewer(comp2, SWT.READ_ONLY);
 	    comboViewer.setContentProvider(ArrayContentProvider.getInstance());
 	    comboViewer.setLabelProvider(new LabelProvider());
	    comboViewer.setInput(new String[]{"Fast", "Normal", "Slow"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 	    label = new Label(comp2, SWT.NONE);
	    label.setText(" pace."); //$NON-NLS-1$
 	    return composite;
     }
 
 	@Override
     public void widgetSelected(SelectionEvent e) {
 		if(e.getSource() == button1) {
 			text.setEnabled(true);
 			button2.setSelection(false);
 			comboViewer.getCombo().setEnabled(false);
 		} else if(e.getSource() == button2) {
 			comboViewer.getCombo().setEnabled(true);
 			button1.setSelection(false);
 			text.setEnabled(false);
 		}
     }
 
 	@Override
     public void widgetDefaultSelected(SelectionEvent e) {
     }
 }
