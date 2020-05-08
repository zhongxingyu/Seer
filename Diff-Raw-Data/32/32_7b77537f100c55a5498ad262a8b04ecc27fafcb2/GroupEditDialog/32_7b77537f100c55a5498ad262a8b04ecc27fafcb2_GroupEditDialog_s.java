 package crisis.ui.dialogs;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import crisis.api.model.CrisisGroup;
 import crisis.application.CrisisManager;
 
 public class GroupEditDialog extends CrisisDialog {
 
 	Map<Integer, CrisisGroup> groupMap = new HashMap<>();
 	CrisisGroup selectedGroup = null;
 	private Combo parentGroupCombo = null;
 	private Text newGroupNameText = null;
 	private Text description = null;
 
 	public String getGroupName() {
 
 		return "";
 	}
 
 	public GroupEditDialog(Shell parentShell) {
 
 		super(parentShell);
 	}
 
 	public void setGroup(CrisisGroup group) {
 
 		selectedGroup = group;
 	}
 
 	public CrisisGroup getParentGroup() {
 
 		return selectedGroup;
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 
 		getShell().setText("Edit Group");
 		getShell().setToolTipText("Modify the selected group");
 		
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout(1, false);
 		composite.setLayout(layout);
 
 		// name
 		new Label(composite, SWT.LEFT).setText("&Name:");
 		newGroupNameText = new Text(composite, SWT.BORDER);
 		newGroupNameText.setText(selectedGroup.getGroupName());
 		new Label(composite, SWT.NONE);
 
 		// group
 		new Label(composite, SWT.NONE).setText("&Parent Group:");
 		parentGroupCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
 		int i = 0;
 		for (CrisisGroup group : CrisisManager.getManager().getAllGroups()) {
 			if(isValidParentGroup(group, selectedGroup)) {
 				parentGroupCombo.add(group.getGroupName(), i);
 				groupMap.put(i, group);
 				if (group.getGroupId().equals(selectedGroup.getParentGroupId())) {
 					parentGroupCombo.select(i);
 				}
 				i++;
 			}
 		}
 		new Label(composite, SWT.NONE);
 		
 		// comment
 		new Label(composite, SWT.LEFT).setText("&Description:");
 		description = new Text(composite, SWT.NONE);
 		description.setText(selectedGroup.getGroupDescription());
 		new Label(composite, SWT.NONE);
 		
 		return composite;
 	}
 
 	
 	private boolean isValidParentGroup(CrisisGroup group, CrisisGroup parentGroup){
 		
 		// check if parent same or child of group
 		for(CrisisGroup tempGroup = group; tempGroup != null ; tempGroup = CrisisManager.getManager().getGroupById(tempGroup.getParentGroupId())) {
 			if(parentGroup == tempGroup){
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	
 	@Override
 	protected void okPressed() {
 
 		// Validate
 		String newName = newGroupNameText.getText();
 		if(newName.isEmpty()) {
 			MessageDialog.openWarning(getShell(), "Warning", "Group name cannot be empty.");
 			return;
 		}
 		String newDescription = description.getText();
 		CrisisGroup newParentGroup = groupMap.get(parentGroupCombo.getSelectionIndex());
		
 		// Modify
 		CrisisManager.getManager().modifyGroup(selectedGroup.getGroupId(), newName, newParentGroup, newDescription);
 				
 		super.okPressed();
 	}
 }
