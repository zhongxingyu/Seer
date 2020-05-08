 package edu.ualberta.med.biobank.barcodegenerator.forms;
 
 import java.util.Map;
 
 import javax.xml.bind.JAXBException;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.ISourceProviderListener;
 import org.eclipse.ui.PlatformUI;
 
 import edu.ualberta.med.biobank.SessionManager;
 import edu.ualberta.med.biobank.barcodegenerator.dialogs.ComboInputDialog;
 import edu.ualberta.med.biobank.barcodegenerator.dialogs.ComboInputDialog.InvalidOptionsException;
 import edu.ualberta.med.biobank.barcodegenerator.dialogs.StringInputDialog;
 import edu.ualberta.med.biobank.barcodegenerator.template.Template;
 import edu.ualberta.med.biobank.barcodegenerator.template.TemplateStore;
 import edu.ualberta.med.biobank.barcodegenerator.template.presets.cbsr.CBSRLabelMaker;
 import edu.ualberta.med.biobank.barcodegenerator.trees.ConfigurationTree;
 import edu.ualberta.med.biobank.barcodegenerator.trees.TreeException;
 import edu.ualberta.med.biobank.common.wrappers.JasperTemplateWrapper;
 import edu.ualberta.med.biobank.gui.common.BgcPlugin;
 import edu.ualberta.med.biobank.gui.common.BgcSessionState;
 import edu.ualberta.med.biobank.gui.common.forms.Actions;
 import edu.ualberta.med.biobank.gui.common.forms.BgcEntryForm;
 import edu.ualberta.med.biobank.gui.common.forms.BgcEntryFormActions;
 import edu.ualberta.med.biobank.gui.common.widgets.BgcBaseText;
 import gov.nih.nci.system.applicationservice.ApplicationException;
 
 /**
  * 
  * View for making templates. Consists of a configuration tree editor to edit
  * jasper maker specific configuration settings.
  * 
  * @author Thomas Polasek 2011
  * 
  */
 
 public class LabelTemplateEntryForm extends BgcEntryForm implements
     SelectionListener {
 
     public static final String ID = "edu.ualberta.med.biobank.barcodegenerator.forms.TemplateEntryForm";
 
     private Button deleteButton = null;
     private Button copyButton = null;
     private Button newButton = null;
 
     private BgcBaseText templateNameText = null;
     private BgcBaseText printerNameText = null;
     private BgcBaseText jasperConfigText = null;
 
     private List templateNamesList = null;
     private ConfigurationTree configTree = null;
     private String prevTemplateName = null;
 
     private Shell shell = null;
 
     private TemplateStore templateStore = null;
 
     private boolean loggedIn = false;
 
     @Override
     protected void init() throws Exception {
         setPartName("Label Templates");
     }
 
     @Override
     protected void performDoubleClick(DoubleClickEvent event) {
         // do nothing
     }
 
     @Override
     protected void addToolbarButtons() {
         formActions = new BgcEntryFormActions(this);
         formActions.addConfirmAction(Actions.GUI_COMMON_CONFIRM);
         form.updateToolBar();
     }
 
     @Override
     protected void createFormContent() throws Exception {
         super.createFormContent();
         form.setText("Label Templates");
         form.setMessage(getOkMessage(), IMessageProvider.NONE);
         page.setLayout(new GridLayout(1, false));
 
         BgcSessionState sessionSourceProvider = BgcPlugin
             .getSessionStateSourceProvider();
 
         loggedIn = sessionSourceProvider.getCurrentState()
             .get(BgcSessionState.SESSION_STATE_SOURCE_NAME)
             .equals(BgcSessionState.LOGGED_IN);
 
         shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 
         createMasterDetail();
 
         sessionSourceProvider
             .addSourceProviderListener(new ISourceProviderListener() {
                 @Override
                 public void sourceChanged(int sourcePriority,
                     String sourceName, Object sourceValue) {
                     if (sourceValue != null) {
                         loggedIn = sourceValue
                             .equals(BgcSessionState.LOGGED_IN);
                         updateForm();
                     }
                 }
 
                 @Override
                 public void sourceChanged(int sourcePriority,
                     @SuppressWarnings("rawtypes") Map sourceValuesByName) {
                 }
             });
         updateForm();
     }
 
     protected String getOkMessage() {
         return "Used to create a new template for a printer label";
     }
 
     @Override
     public void setFocus() {
     }
 
     private void createMasterDetail() throws ApplicationException {
         page.setLayout(new GridLayout(2, false));
         page.setLayoutData(new GridData(GridData.FILL, GridData.FILL | SWT.TOP,
             true, true));
 
         // master section
         Composite masterComp = toolkit.createComposite(page);
         masterComp.setLayout(new GridLayout(1, false));
         masterComp.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
             true, true));
 
         Composite client = createSectionWithClient("Label Templates",
             masterComp);
         client.setLayout(new GridLayout());
         client.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
             true));
 
         templateNamesList = new List(client, SWT.BORDER | SWT.V_SCROLL);
         templateNamesList.addSelectionListener(listListener);
         GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
         gd.heightHint = 300;
         templateNamesList.setLayoutData(gd);
 
         Composite composite4 = new Composite(client, SWT.NONE);
         composite4.setLayout(new GridLayout(3, true));
         composite4.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
             true, true));
 
         newButton = toolkit.createButton(composite4, "New", SWT.PUSH);
         newButton.addSelectionListener(this);
         newButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
             true, true));
 
         copyButton = toolkit.createButton(composite4, "Copy", SWT.PUSH);
         copyButton.addSelectionListener(this);
         copyButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
             true, true));
 
         deleteButton = toolkit.createButton(composite4, "Delete", SWT.PUSH);
         deleteButton.addSelectionListener(this);
         deleteButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
             true, true));
 
         createComposite3();
     }
 
     private void createComposite3() {
         Composite detailsComp = toolkit.createComposite(page);
         detailsComp.setLayout(new GridLayout(1, false));
         detailsComp.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
             true, true));
 
         Composite client = createSectionWithClient("Templates Details",
             detailsComp);
         client.setLayout(new GridLayout(2, false));
         client.setLayoutData(new GridData(GridData.FILL,
             GridData.VERTICAL_ALIGN_BEGINNING, true, true));
 
         templateNameText = (BgcBaseText) createLabelledWidget(client,
             BgcBaseText.class, SWT.NONE, "Template Name");
         templateNameText.setLayoutData(new GridData(GridData.FILL,
             GridData.FILL, true, true));
         templateNameText.setEditable(false);
         templateNameText.setEnabled(true);
 
         jasperConfigText = (BgcBaseText) createLabelledWidget(client,
             BgcBaseText.class, SWT.NONE, "Jasper Configuration");
         jasperConfigText.setLayoutData(new GridData(GridData.FILL,
             GridData.FILL, true, true));
         jasperConfigText.setEditable(false);
         jasperConfigText.setEnabled(true);
 
         printerNameText = (BgcBaseText) createLabelledWidget(client,
             BgcBaseText.class, SWT.NONE, "Intended Printer");
         printerNameText.setEditable(true);
         printerNameText.setLayoutData(new GridData(GridData.FILL,
             GridData.FILL, true, true));
         printerNameText.addModifyListener(printerNameModifyListener);
 
         Label l = toolkit.createLabel(client, "Configuration:");
         GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true);
         gd.horizontalSpan = 2;
         l.setLayoutData(gd);
 
         configTree = new ConfigurationTree(client, SWT.NONE);
     }
 
     private void setEnable(boolean enable) {
         configTree.setEnabled(enable);
         deleteButton.setEnabled(enable);
         copyButton.setEnabled(enable);
         newButton.setEnabled(enable);
         printerNameText.setEnabled(enable);
     }
 
     /**
      * 
      * Updates the template name list and jasper file combo.
      * 
      */
     private void updateForm() {
         try {
             if (loggedIn) {
                 if (templateStore == null) {
                     templateStore = new TemplateStore();
                 }
 
                 setEnable(true);
 
                 for (String s : templateStore.getTemplateNames())
                     templateNamesList.add(s);
 
                 templateNamesList.redraw();
 
             } else {
                 setEnable(false);
                 templateNamesList.removeAll();
                 templateNamesList.redraw();
 
             }
         } catch (ApplicationException e) {
             BgcPlugin.openAsyncError("Database Error",
                 "Error while updating form", e);
         }
     }
 
     private SelectionListener listListener = new SelectionListener() {
         @Override
         public void widgetSelected(SelectionEvent e) {
 
             String[] selectedItems = templateNamesList.getSelection();
             if (selectedItems.length == 1) {
 
                 try {
                     confirm(true);
                 } catch (Exception e1) {
                     BgcPlugin.openAsyncError("Template Saving",
                         "Failed to save template: " + e1.getMessage());
                     return;
                 }
 
                 if (selectedItems[0] != null) {
                     Template selectedTemplate;
                     try {
                         selectedTemplate = templateStore
                             .getTemplate(selectedItems[0]);
                     } catch (Exception e1) {
                         BgcPlugin.openAsyncError("Template Selection",
                             "Failed to load the selected template");
                         return;
                     }
 
                     if (!selectedTemplate.getName().equals(selectedItems[0])) {
                         BgcPlugin
                             .openAsyncError("Template Name Error",
                                 "Severe Error: Internal template names do not match.");
                         return;
                     }
 
                     // load selected template into gui elements
                     try {
                         jasperConfigText.setText(selectedTemplate
                             .getJasperTemplateName());
                     } catch (Exception e1) {
                         jasperConfigText.setText("");
                         BgcPlugin.openAsyncError("Template Selection",
                             "Failed to find the jasper configuration name.");
                         return;
                     }
 
                     try {
                         configTree.populateTree(selectedTemplate
                             .getConfiguration());
                     } catch (Exception ee) {
                         jasperConfigText.setText("");
 
                         try {
                             configTree.populateTree(null);
                         } catch (TreeException e1) {
                            BgcPlugin
                                .openAsyncError(
                                    "Error: Could not set Template Configuration Tree",
                                    ee.getMessage());
                            return;
                         }
                     }
 
                     templateNameText.setText(selectedTemplate.getName());
                     prevTemplateName = selectedTemplate.getName();
                     printerNameText.setText(selectedTemplate.getPrinterName());
                     printerNameText.setEnabled(true);
                     setDirty(selectedTemplate.isNew());
 
                 } else {
                     prevTemplateName = null;
                     templateNameText.setText("Please select a template");
                     printerNameText.setText("");
                     printerNameText.setEnabled(false);
                     jasperConfigText.setText("");
                     try {
                         configTree.populateTree(null);
                     } catch (TreeException e1) {
                         BgcPlugin
                             .openAsyncError(
                                 "Error: Could not clear the Template Configuration Tree",
                                 e1.getError());
                     }
                     setDirty(false);
                 }
 
             } else {
                 BgcPlugin.openAsyncError("Selection Listener Error",
                     "invalid selected items length: " + selectedItems.length);
                 return;
             }
 
         }
 
         @Override
         public void widgetDefaultSelected(SelectionEvent e) {
             widgetSelected(e);
 
         }
     };
 
     @Override
     public void confirm() {
         confirm(false);
     }
 
     public void confirm(boolean askUserToSave) {
         try {
             try {
                 configTree.resetEditor();
             } catch (TreeException e1) {
                 BgcPlugin.openAsyncError("Tree Editor",
                     "Failed to reset tree editor");
             }
 
             if (prevTemplateName != null) {
                 if (isDirty() || configTree.isDirty()) {
 
                     Template selectedTemplate = templateStore
                         .getTemplate(prevTemplateName);
 
                     if (!askUserToSave
                         || BgcPlugin
                             .openConfirm("Template Saving",
                                 "Template has been modified, do you want to save your changes?")) {
 
                         String printerName = printerNameText.getText();
                         if ((printerName == null)
                             || (printerName.length() == 0)) {
                             selectedTemplate.setPrinterName("default");
                         } else {
                             if (!printerName.equals(selectedTemplate
                                 .getPrinterName())) {
                                 selectedTemplate.setPrinterName(printerName);
                             }
                         }
 
                         if (configTree.isDirty()) {
                             selectedTemplate.setConfiguration(configTree
                                 .getConfiguration());
                         }
                         selectedTemplate.persist();
                         setDirty(false);
                         configTree.unDirty();
                     }
 
                 }
 
             }
         } catch (Exception e1) {
             BgcPlugin.openAsyncError("Template Save Error",
                 "Could not save the template to the database", e1);
         }
     }
 
     @Override
     public void widgetSelected(SelectionEvent e) {
         if (e.getSource() == newButton) {
             newButtonSelected(e);
         } else if (e.getSource() == copyButton) {
             copyButtonSelected(e);
         } else if (e.getSource() == deleteButton) {
             deleteButtonSelected(e);
         } else {
             BgcPlugin.openAsyncError("Invalid selection event",
                 "invalid selection source");
         }
     }
 
     @Override
     public void widgetDefaultSelected(SelectionEvent e) {
         widgetSelected(e);
     }
 
     private void templatesNamesSelectLast() {
         if ((templateNamesList != null)
             && ((templateNamesList.getItemCount() - 1) >= 0)) {
             templateNamesList.select(templateNamesList.getItemCount() - 1);
             templateNamesList.notifyListeners(SWT.Selection, new Event());
         }
     }
 
     private void newButtonSelected(SelectionEvent e) {
         try {
             StringInputDialog dialog = new StringInputDialog("New Template",
                 "Please enter the name for the new template", "Name", shell);
             if (dialog.open() == Dialog.OK) {
 
                 String newTemplateName = dialog.getValue();
 
                 if (!templateStore.getTemplateNames().contains(newTemplateName)) {
 
                     // jasper config combo selection
                     ComboInputDialog jasperComboDialog = new ComboInputDialog(
                         "Jasper Configuration Selection",
                         "Please select a Jasper Configuration that you would like to base this template on.",
                         JasperTemplateWrapper.getTemplateNames(SessionManager
                             .getAppService()), null, shell);
                     jasperComboDialog.open();
 
                     String selectedJasperConfig = jasperComboDialog.getValue();
                     if ((selectedJasperConfig == null)
                         || (selectedJasperConfig.length() == 0))
                         return;
 
                     Template ct = new Template();
 
                     ct.setName(newTemplateName);
                     ct.setPrinterName("default");
                     ct.setJasperTemplate(JasperTemplateWrapper
                         .getTemplateByName(SessionManager.getAppService(),
                             selectedJasperConfig));
 
                     if (selectedJasperConfig.equals("CBSR")) {
                         ct.setConfiguration(CBSRLabelMaker
                             .getDefaultConfiguration());
 
                     } else {
                         ct.setConfiguration(CBSRLabelMaker
                             .getDefaultConfiguration());
                     }
 
                     try {
                         templateStore.addTemplate(ct);
                     } catch (Exception e1) {
                         BgcPlugin.openAsyncError("Cannot Add Template",
                             "Failed to add template to the template store.");
                         return;
                     }
 
                     templateNamesList.add(ct.getName());
                     templatesNamesSelectLast();
                     templateNamesList.redraw();
 
                 } else {
                     BgcPlugin.openAsyncError("Template Exists",
                         "Your new template must have a unique name.");
                     return;
                 }
             }
         } catch (ApplicationException e1) {
             BgcPlugin.openAsyncError("Template Creation Error",
                 "Application Exception: could not create template", e1);
         } catch (InvalidOptionsException e1) {
             BgcPlugin
                 .openAsyncError(
                     "Template Creation Error",
                     "No Jasper Configurations Exist.\n\nPlease create at least one Jasper Configuration first.",
                     e1);
         } catch (JAXBException e1) {
             BgcPlugin.openAsyncError("Template Creation Error",
                 "Failed to set configuration data.", e1);
         }
 
     }
 
     private void copyButtonSelected(SelectionEvent e) {
         try {
             if (prevTemplateName == null)
                 return;
 
             StringInputDialog dialog = new StringInputDialog(
                 "Cloned Template Name",
                 "What is the name of the cloned template?", "Name", shell);
             dialog.setValue(prevTemplateName + " copy");
 
             if (dialog.open() == Dialog.OK) {
 
                 Template selectedTemplate = templateStore
                     .getTemplate(prevTemplateName);
 
                 String newTemplateName = dialog.getValue();
 
                 if (!templateStore.getTemplateNames().contains(newTemplateName)) {
 
                     Template cloned = selectedTemplate.clone();
 
                     if (cloned != null) {
 
                         cloned.setName(newTemplateName);
                         cloned.persist();
 
                         templateStore.addTemplate(cloned);
 
                         templateNamesList.add(newTemplateName);
                         templatesNamesSelectLast();
                         templateNamesList.redraw();
                     } else {
                         BgcPlugin.openAsyncError("Copy Template Error",
                             "Could not copy template. An error occured.");
                         return;
                     }
                 } else {
                     BgcPlugin.openAsyncError("Template Exists",
                         "Your new template must have a unique name.");
                     return;
                 }
             }
         } catch (Exception e1) {
             BgcPlugin.openAsyncError("Template Create Error",
                 "Could not create template", e1);
         }
     }
 
     private void deleteButtonSelected(SelectionEvent e) {
         try {
 
             if (prevTemplateName != null) {
                 MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION
                     | SWT.YES | SWT.NO);
                 messageBox.setMessage("Are you sure you want to delete "
                     + prevTemplateName + "?");
                 messageBox.setText("Deleting Template");
 
                 int response = messageBox.open();
                 if (response == SWT.YES) {
 
                     Template selectedTemplate = templateStore
                         .getTemplate(prevTemplateName);
 
                     templateStore.deleteTemplate(prevTemplateName);
                     templateNamesList.remove(selectedTemplate.getName());
 
                     if (!selectedTemplate.isNew()) {
                         selectedTemplate.delete();
                     }
 
                     templateNameText.setText("Please select a template");
 
                     templateNamesList.deselectAll();
                     templatesNamesSelectLast();
                     templateNamesList.redraw();
 
                     prevTemplateName = null;
                     printerNameText.setText("");
                     printerNameText.setEnabled(false);
 
                     jasperConfigText.setText("");
 
                     configTree.populateTree(null);
 
                 }
             }
         } catch (Exception e1) {
             BgcPlugin.openAsyncError("Template Delete Error",
                 "Could not delete template", e1);
         }
     }
 
     private ModifyListener printerNameModifyListener = new ModifyListener() {
 
         @Override
         public void modifyText(ModifyEvent e) {
             setDirty(true);
         }
     };
 
 }
