 package gui;
 
 import interpreter.GlobalBreakpoint;
 
 import java.util.ArrayList;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 public class BreakpointView extends Composite {
 	private Table global;
 	private StyledText addTextField;
 	private Button button;
 	
 	public BreakpointView(Composite parent, int def) {
 		super(parent, def);		
 		
 		//Setting layout
 		GridLayout gLayout = new GridLayout();
 		this.setLayout(gLayout);
 		GridData gData = new GridData(GridData.FILL_BOTH);
 		this.setLayoutData(gData);
 		
 		this.global = new Table (this, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL 
 				| SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
 		
 		this.global.setLinesVisible(true);
 		this.global.setHeaderVisible(true);
 		gData = new GridData(GridData.FILL_BOTH);
 		this.global.setLayoutData(gData);
 				
 		TableColumn column1 = new TableColumn(this.global, SWT.CENTER);
 		column1.setWidth(50);
 		column1.setText("Active");
 		TableColumn column2 = new TableColumn(this.global, SWT.CENTER);
 		column2.setWidth(200);
 		column2.setText("Expression");
 	
 		//Create text field and button for global breakpoint inserts
 		this.addTextField = new StyledText(this, SWT.SINGLE | SWT.BORDER);
 		this.addTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		gData = new GridData(GridData.FILL_HORIZONTAL); 
 		gData.verticalSpan = 5;
 		this.addTextField.setLayoutData(gData);
     
 		this.button = new Button(this, SWT.PUSH);
 		this.button.setText("Add/Remove");
 		gData = new GridData(GridData.FILL_HORIZONTAL);  
 		gData.verticalSpan = 5;
 		this.button.setLayoutData(gData);
 	}
 	
 	public void drawGlobalBreakpointItem(ArrayList<GlobalBreakpoint> gbreakpoints) {		
 		this.global.removeAll();
 		for (int i = 0; i < gbreakpoints.size(); i++) {
 			TableItem item = new TableItem(this.global, SWT.NONE);
 			item.setText(1, gbreakpoints.get(i).getExpression().toString());
			if (gbreakpoints.get(i).isActive()) item.setChecked(true);
 		}
 	}
 	
 	public void removeGlobalBreakpointItem() {
 	}
 	
 	public Table getGlobalBreakpoint() {
 		return this.global;
 	}
 	
 	public StyledText getAddTextField() {
 		return this.addTextField;
 	}
 	
 	public Button getAddButton() {
 		return this.button;
 	}
 }
