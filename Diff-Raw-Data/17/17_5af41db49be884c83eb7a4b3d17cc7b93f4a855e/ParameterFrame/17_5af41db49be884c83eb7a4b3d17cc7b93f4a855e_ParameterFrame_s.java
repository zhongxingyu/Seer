 package gui;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import ast.FunctionParameter;
 
 public class ParameterFrame extends Frame implements MiscFrame {	
 	private Shell shell;
 	private Text[] values;
 	private Button okButton;
 
 	public ParameterFrame(Shell parentShell) {	
 		shell = new Shell(parentShell);
 		shell.setText("Parameter");
 		
 		GridLayout gLayout = new GridLayout();
 		gLayout.numColumns = 4;
 		shell.setLayout(gLayout);
 		
 		Label label = new Label(this.shell, SWT.NONE);
 		label.setText("Please insert parameter values");	
 		GridData gData = new GridData(GridData.FILL_HORIZONTAL);
 		gData.horizontalSpan = 4;
 		label.setLayoutData(gData);
 		Label label2 = new Label(this.shell, SWT.NONE);
 		label2.setText("for the main function:");	
 		gData = new GridData(GridData.FILL_HORIZONTAL);
 		gData.horizontalSpan = 4;
 		label2.setLayoutData(gData);
 		Label label3 = new Label(this.shell, SWT.NONE);
 		gData = new GridData(GridData.FILL_HORIZONTAL);
 		gData.horizontalSpan = 4;
 		label3.setLayoutData(gData);	
 	}
 	
 	public void createFrame(FunctionParameter[] parameters) {
 		if (parameters == null) {
 			return;
 		}
 		this.values = new Text[parameters.length];
 		this.shell.setSize(200, 150 + 15 * parameters.length);
 		GridData gData;
 		
 		for (int i = 0; i < parameters.length; i++) {
 			new Label(this.shell, SWT.NONE).setText(parameters[i].toString());
 			Text value = new Text(this.shell, SWT.NONE);
 			gData = new GridData(GridData.FILL_HORIZONTAL);
 			gData.horizontalSpan = 3;
 			value.setLayoutData(gData);	
 			this.values[i] = value;
 		}
 		
 		this.okButton = new Button(this.shell, SWT.NONE);
 		this.okButton.setText("OK");
 		gData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
 		gData.horizontalSpan = 4;
 		this.okButton.setLayoutData(gData);
 		
 		if (parameters.length > 0) {
 			this.shell.open();
 		}	
 	}
 
 	public Shell getShell() {
 		return this.shell;
 	}
 	
 	public Text[] getValues() {
 		return this.values;
 	}
 	
 	public Button getOkButton() {
 		return this.okButton;
 	}
 	
 	@Override
 	public Button getSaveButton() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Button getCloseButton() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void close() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
