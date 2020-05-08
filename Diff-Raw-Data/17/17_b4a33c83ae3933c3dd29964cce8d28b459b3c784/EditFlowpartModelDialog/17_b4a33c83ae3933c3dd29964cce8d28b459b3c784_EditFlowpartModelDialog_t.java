 package jp.hishidama.xtext.dmdl_editor.ui.wizard.page.flowpart;
 
import java.util.List;

 import jp.hishidama.eclipse_plugin.dialog.EditDialog;
 import jp.hishidama.eclipse_plugin.util.StringUtil;
 import jp.hishidama.xtext.dmdl_editor.dmdl.ModelDefinition;
 import jp.hishidama.xtext.dmdl_editor.dmdl.ModelUiUtil;
 import jp.hishidama.xtext.dmdl_editor.dmdl.ModelUtil;
 import jp.hishidama.xtext.dmdl_editor.ui.dialog.DmdlModelSelectionDialog;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public class EditFlowpartModelDialog extends EditDialog {
 
 	private IProject project;
 	private FlowpartModelRow row;
 
	private Button inButton;
 	private Text nameText;
 	private String modelClassName;
 	private Text modelNameText;
 	private Text modelDescText;
 
 	public EditFlowpartModelDialog(Shell parentShell, IProject project, FlowpartModelRow row) {
 		super(parentShell, "In/Out編集", 3);
 		this.project = project;
 		this.row = row;
 	}
 
 	@Override
 	protected void createFields(Composite composite) {
		List<Button> buttons = createRadioField(composite, "in/out", "in", "out");
		inButton = buttons.get(0);
		inButton.setSelection(row.in);
		buttons.get(1).setSelection(!row.in);
 		nameText = createTextField(composite, "name");
 		nameText.setText(nonNull(row.name));
 		modelClassName = row.modelClassName;
 		TextButtonPair pair = createTextButtonField(composite, "model name", "select");
 		modelNameText = pair.text;
 		modelNameText.setText(nonNull(row.modelName));
 		modelNameText.setEditable(false);
 		pair.button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				selectDataModel();
 			}
 		});
 		modelDescText = createTextField(composite, "model description");
 		modelDescText.setText(nonNull(row.modelDescription));
 		modelDescText.setEditable(false);
 	}
 
 	@Override
 	protected void refresh() {
 		refreshOkButton();
 	}
 
 	protected void selectDataModel() {
 		DmdlModelSelectionDialog dialog = new DmdlModelSelectionDialog(getShell(), project);
 		dialog.setInitialModel(modelNameText.getText());
 		if (dialog.open() != Window.OK) {
 			return;
 		}
 
 		ModelDefinition model = dialog.getSelectedDataModel();
 		String modelName = nonNull(model.getName());
 		modelNameText.setText(modelName);
 		modelDescText.setText(ModelUtil.getDecodedDescriptionText(model));
 		this.modelClassName = ModelUiUtil.getModelClassName(project, modelName);
 		if (nameText.getText().trim().isEmpty()) {
 			nameText.setText(StringUtil.toLowerCamelCase(modelName));
 		}
 	}
 
 	@Override
 	protected boolean validate() {
 		if (StringUtil.isEmpty(nameText.getText().trim())) {
 			return false;
 		}
 		if (StringUtil.isEmpty(modelNameText.getText().trim())) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	protected void okPressed() {
		row.in = inButton.getSelection();
 		row.name = nameText.getText().trim();
 		row.modelClassName = modelClassName;
 		row.modelName = modelNameText.getText();
 		row.modelDescription = modelDescText.getText();
 
 		super.okPressed();
 	}
 }
