 package gui;
 
 import java.util.ArrayList;
 
 import misc.Message;
 import misc.MessageSystem;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 public class MiscConsole extends Console {
 	public MiscConsole(Composite parent, int style, MessageSystem messagesystem) {
 		super(parent, style, messagesystem);
 		this.messagesystem = messagesystem;
 		this.table = new Table(this, SWT.NONE);
 		
 		//Setting layout
 		GridLayout gLayout = new GridLayout();
 		this.setLayout(gLayout);
 		GridData gData = new GridData(GridData.FILL_BOTH);
 		this.setLayoutData(gData);
 		this.table.setLayoutData(gData);
 		
 		TableColumn column1 = new TableColumn(this.table, SWT.CENTER);
 		column1.setWidth(20);
 		TableColumn column2 = new TableColumn(this.table, SWT.CENTER);
 		column2.setWidth(600);
 		column2.setText("Values");
 		TableColumn column3 = new TableColumn(this.table, SWT.CENTER);
 		column3.setWidth(100);
 		column3.setText("Result");
 		
 		this.table.setLinesVisible(true);
 		this.table.setHeaderVisible(true);
 	}
 	
 	public void printRandomTestResult(String[] result) {
 		TableItem item = new TableItem(this.table, SWT.NONE);
 		item.setText(result[0]);
 		item.setText(1, result[1]);
 		item.setText(2, result[2]);
 	}
 	
	public Table getTable() {
		return this.table;
	}
	
 	@Override
 	public void updateConsole(ArrayList<Message> messages) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
