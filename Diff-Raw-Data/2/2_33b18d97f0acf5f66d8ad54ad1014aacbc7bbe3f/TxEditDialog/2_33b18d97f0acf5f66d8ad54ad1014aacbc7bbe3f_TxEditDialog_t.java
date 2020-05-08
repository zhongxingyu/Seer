 package crisis.ui.dialogs;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import crisis.application.CrisisManager;
 import crisis.model.Date;
 import crisis.model.Money;
 import crisis.model.Tx;
 import crisis.model.TxGroup;
 import crisis.model.TxType;
 
 public class TxEditDialog extends CrisisDialog {
 
 	private Map<Integer, TxGroup> groupMap = new ConcurrentHashMap<>();
 	private Combo parentGroupCombo;
 	private DateTime dateTime;
 	private Text value;
 	private Text description;
 	private Text comment;
 	private Tx selectedTx;
 	private Button expense;
 	private Button profit;
 
 	public TxEditDialog(Shell parentShell) {
 
 		super(parentShell);
 	}
 
 	protected Control createDialogArea(Composite parent) {
 
 		getShell().setText("Edit Transaction");
 
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout(1, false);
 		composite.setLayout(layout);
 		new Label(composite, SWT.NONE);
 
 		// description
 		new Label(composite, SWT.LEFT).setText("&Description:");
 		description = new Text(composite, SWT.BORDER | SWT.BORDER);
 		description.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
 		description.setText(selectedTx.getDescription());
 		new Label(composite, SWT.NONE);
 
 		// group
 		new Label(composite, SWT.LEFT).setText("&Group:");
 		parentGroupCombo = new Combo(composite, SWT.READ_ONLY);
 		int i = 0;
 		for (TxGroup group : CrisisManager.getCrisis().getGroups()) {
 			if (group == CrisisManager.getCrisis().getGroupsRoot()) {
 				continue;
 			}
 			groupMap.put(i, group);
 			parentGroupCombo.add(group.getName(), i);
 			if (group == selectedTx.getParentGroup()) {
 				parentGroupCombo.select(i);
 			}
 			
 			i++;
 		}
 		new Label(composite, SWT.NONE);
 
 		// date
 		new Label(composite, SWT.LEFT).setText("&Date:");
 		dateTime = new DateTime(composite, SWT.CALENDAR);
		dateTime.setDate(selectedTx.getDate().getYear(), selectedTx.getDate().getMonth() - 1, selectedTx.getDate().getDay());
 		new Label(composite, SWT.NONE);
 
 		// value
 		new Label(composite, SWT.NONE).setText("&Value:");
 		value = new Text(composite, SWT.NONE);
 		value.setText(String.valueOf(selectedTx.getValue().getValue()));
 		new Label(composite, SWT.NONE);
 
 		// comment
 		new Label(composite, SWT.NONE).setText("&Comment:");
 		comment = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
 		comment.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
 		comment.setText(selectedTx.getComment());
 		new Label(composite, SWT.NONE);
 
 		// type
 		expense = new Button(composite, SWT.RADIO);
 		expense.setText("Expense");
 		profit = new Button(composite, SWT.RADIO);
 		profit.setText("Profit");
 		if(selectedTx.getTxType() == TxType.EXPENSE){
 			expense.setSelection(true);	
 		} else {
 			profit.setSelection(true);
 		}
 		
 		new Label(composite, SWT.NONE);
 
 		return composite;
 	}
 
 	protected void okPressed() {
 
 		// group
 		TxGroup group = groupMap.get(parentGroupCombo.getSelectionIndex());
 
 		// date
 		Date date = new Date(dateTime.getYear(), dateTime.getMonth() + 1, dateTime.getDay());
 
 		// value
 		Money money = null;
 		try {
 			 money = new Money(Double.valueOf(value.getText()));
 		} catch(NumberFormatException nfe) {
 			CrisisManager.warnUser(getShell(), "Value\"value.getText()\" is not valid.");
 			return;
 		}
 		
 		// type
 		TxType txType = expense.getSelection() ? TxType.EXPENSE : TxType.PROFIT;
 
 		// description
 		if(description.getText().isEmpty()){
 			CrisisManager.warnUser(getShell(), "Description cannot be empty.");
 			return;
 		}
 		
 		CrisisManager.modifyTx(selectedTx, group, date, description.getText(), money, comment.getText(), txType);
 
 		super.okPressed();
 	}
 
 	public void setSelectedTx(Tx tx) {
 
 		selectedTx = tx;
 	}
 }
