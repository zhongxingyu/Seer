 package org.eclipse.jst.servlet.ui.internal.wizard;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jst.j2ee.internal.web.operations.FilterMappingItem;
 import org.eclipse.jst.j2ee.internal.web.operations.IFilterMappingItem;
 import org.eclipse.jst.j2ee.internal.web.operations.INewFilterClassDataModelProperties;
 import org.eclipse.jst.j2ee.internal.wizard.StringArrayTableWizardSection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
 import org.eclipse.wst.common.componentcore.internal.operation.IArtifactEditOperationDataModelProperties;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizardPage;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 
 /**
  * Filter Wizard Setting Page
  */
 public class AddFilterWizardPage extends DataModelWizardPage {
 	final static String[] FILTEREXTENSIONS = {"java"}; //$NON-NLS-1$
 
 	private Text displayNameText;
 
 	FilterMappingsArrayTableWizardSection mappingSection;
 
 	public AddFilterWizardPage(IDataModel model, String pageName) {
 		super(model, pageName);
 		setDescription(IWebWizardConstants.ADD_FILTER_WIZARD_PAGE_DESC);
 		this.setTitle(IWebWizardConstants.ADD_FILTER_WIZARD_PAGE_TITLE);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jem.util.ui.wizard.WTPWizardPage#getValidationPropertyNames()
 	 */
 	protected String[] getValidationPropertyNames() {
 		return new String[]{INewFilterClassDataModelProperties.DISPLAY_NAME, 
 		        INewFilterClassDataModelProperties.INIT_PARAM, 
 		        INewFilterClassDataModelProperties.FILTER_MAPPINGS};
 	}
 
 	protected Composite createTopLevelComposite(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NULL);
 		composite.setLayout(new GridLayout());
 		GridData data = new GridData(GridData.FILL_BOTH);
 		data.widthHint = 300;
 		composite.setLayoutData(data);
 
 		createNameDescription(composite);
 		
 		StringArrayTableWizardSectionCallback callback = new StringArrayTableWizardSectionCallback();
 		StringArrayTableWizardSection initSection = new StringArrayTableWizardSection(composite, IWebWizardConstants.INIT_PARAM_LABEL, IWebWizardConstants.ADD_BUTTON_LABEL, IWebWizardConstants.EDIT_BUTTON_LABEL, 
 				IWebWizardConstants.REMOVE_BUTTON_LABEL, new String[]{IWebWizardConstants.NAME_LABEL, IWebWizardConstants.VALUE_LABEL, IWebWizardConstants.DESCRIPTION_LABEL}, null,// WebPlugin.getDefault().getImage("initializ_parameter"),
 				model, INewFilterClassDataModelProperties.INIT_PARAM);
 		initSection.setCallback(callback);
 		
 		String[] buttons = new String[] {
 		        IWebWizardConstants.ADD_BUTTON_LABEL, 
 		        IWebWizardConstants.EDIT_BUTTON_LABEL, 
 		        IWebWizardConstants.REMOVE_BUTTON_LABEL
 		};
 		String[] columnNames = new String[] {
 		        null,
 		        IWebWizardConstants.URL_SERVLET_LABEL,
 		        IWebWizardConstants.DISPATCHERS_LABEL
 		};
 		mappingSection = new FilterMappingsArrayTableWizardSection(composite, 
 		         model, INewFilterClassDataModelProperties.FILTER_MAPPINGS);
 
 		String text = displayNameText.getText();
 		// Set default URL Pattern
 		List input = new ArrayList();
 		input.add(new FilterMappingItem(IFilterMappingItem.URL_PATTERN, "/" + text)); //$NON-NLS-1$
 		if (mappingSection != null)
 		    mappingSection.setInput(input);
 		displayNameText.setFocus();
 
 		IStatus projectStatus = validateProjectName();
 		if (!projectStatus.isOK()) {
 			setErrorMessage(projectStatus.getMessage());
 			composite.setEnabled(false);
 		}
 	    Dialog.applyDialogFont(parent);
 		return composite;
 	}
 
 	protected IStatus validateProjectName() {
 		// check for empty
 		if (model.getStringProperty(IArtifactEditOperationDataModelProperties.PROJECT_NAME) == null || model.getStringProperty(IArtifactEditOperationDataModelProperties.PROJECT_NAME).trim().length() == 0) {
 			return WTPCommonPlugin.createErrorStatus(IWebWizardConstants.NO_WEB_PROJECTS);
 		}
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	protected void createNameDescription(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NULL);
 		composite.setLayout(new GridLayout(2, false));
 		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 		// display name
 		Label displayNameLabel = new Label(composite, SWT.LEFT);
 		displayNameLabel.setText(IWebWizardConstants.NAME_LABEL);
 		displayNameLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
 		displayNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		displayNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		displayNameText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				String text = displayNameText.getText();
 				// Set default URL Pattern
 				List input = new ArrayList();
 				input.add(new FilterMappingItem(IFilterMappingItem.URL_PATTERN, "/" + text)); //$NON-NLS-1$
 				if (mappingSection != null)
 				    mappingSection.setInput(input);
 			}
 
 		});
 		synchHelper.synchText(displayNameText, INewFilterClassDataModelProperties.DISPLAY_NAME, null);
 
 		// description
 		Label descLabel = new Label(composite, SWT.LEFT);
 		descLabel.setText(IWebWizardConstants.DESCRIPTION_LABEL);
 		descLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
 		Text descText = new Text(composite, SWT.SINGLE | SWT.BORDER);
 		descText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		synchHelper.synchText(descText, INewFilterClassDataModelProperties.DESCRIPTION, null);
 	}
 
 	public String getDisplayName() {
 		return displayNameText.getText();
 	}
 	
 	public boolean canFlipToNextPage() {
 		if (model.getBooleanProperty(INewFilterClassDataModelProperties.USE_EXISTING_CLASS))
 			return false;
 		return super.canFlipToNextPage();
 	}
 }
