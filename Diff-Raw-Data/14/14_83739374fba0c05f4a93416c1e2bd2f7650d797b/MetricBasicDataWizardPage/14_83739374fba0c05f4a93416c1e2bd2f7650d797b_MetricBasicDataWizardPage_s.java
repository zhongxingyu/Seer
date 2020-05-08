 package org.eclipse.emf.refactor.metrics.generator.ui;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.refactor.metrics.generator.interfaces.INewMetricWizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 
 public class MetricBasicDataWizardPage extends WizardPage implements Listener {
 
 	private static final String PAGE_NAME = "org.eclipse.emf.refactor.metrics.MetricBasicDataWizardPage";
 	private static final String PAGE_TITLE = "New Metric: Basic Data";
 	private static final String PAGE_DESCRIPTION = "Please specify basic metric data. " +
 												"Required fields are denoted by \"(*)\".";
 	private Text nameTextField, idTextField, descriptionTextField;
 	private Combo projectCombo, metamodelCombo, contextCombo;
 	private String jar = "";
 	private String importPackage, metaModel, contextType;
 	
 	@Override
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		final GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		container.setLayout(layout);
 		createTextFields(container);
 		initProjectsAndMetamodels();
 		System.out.println("vorher");
 		if (metaModel != null && ! metaModel.isEmpty() 
 				&& contextType != null && ! contextType.isEmpty()) {
 			System.out.println("drin");
 			setFixedMetamodel(metaModel);
 			setFixedContext(contextType);
 		}
 		System.out.println("nachher");
 		setControl(container);
 		this.setPageComplete(false);
 	}	
 
 	@Override
 	public boolean canFlipToNextPage() {
 		if (((INewMetricWizard) getWizard()).getPageNumbers() > 1) {
 			if (this.isPageComplete()) {
 				((INewMetricWizard) getWizard()).updateSecondPage();
 				return true;
 			}
 		} 
 		return false;
 	}
 	
 	public WizardPage getNextPage() {
 		if (((INewMetricWizard) getWizard()).getPageNumbers() > 1) {
 			return ((INewMetricWizard) getWizard()).getSecondPage();
 		} else {
 			return null;
 		}
 	}
 	
 	@Override
 	public void handleEvent(Event event) {
 		if (event.widget == projectCombo) {
 			((INewMetricWizard) getWizard()).setTargetProject(projectCombo.getText());
 		}
 		if (event.widget == metamodelCombo) {
 			String nsURI = metamodelCombo.getText();
 			if (nsURI != null && ! nsURI.isEmpty()) {
 				EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(nsURI);
 				if (ePackage != null) {
					importPackage = ePackage.getClass().getPackage().getName();
 					if (importPackage.endsWith(".impl")) {
 						int length = importPackage.length();
 						importPackage = importPackage.substring(0, length-5);
 					}
 					System.out.println("importPackage: " + importPackage);
 					File jarFile;
 					try {
 						jarFile = new File
 								(ePackage.getClass().getProtectionDomain()
 								.getCodeSource().getLocation().toURI());
 						String jarName = jarFile.getName();
 						int index = jarName.indexOf("_");
 						jar = jarName.substring(0, index);
 						System.out.println("Jar5: " + jar);
 					} catch (URISyntaxException e) {
 						e.printStackTrace();
 					} finally {	
 						contextCombo.removeAll();
 						Object[] eObjectNames = new Object[ePackage.eContents().size()];
 						for (int i = 0; i < eObjectNames.length; i++) {
 							EObject eObject = ePackage.eContents().get(i);
 							if (eObject instanceof ENamedElement) {
 								eObjectNames[i] = ((ENamedElement) ePackage.eContents().get(i)).getName();
 							}
 						}					
 				        Arrays.sort(eObjectNames);
 						for(Object object : eObjectNames){
 							contextCombo.add((String)object);
 						}
 					}
 				}
 			} else {
 				jar = "";
 				contextCombo.removeAll();
 			}
 		}
 			updatePageComplete();
 			getWizard().getContainer().updateButtons();	
 	}
 
 	public MetricBasicDataWizardPage() {
 		super(PAGE_NAME);
 		setTitle(PAGE_TITLE);
 		setDescription(PAGE_DESCRIPTION);
 	}
 	
 	private void createTextFields(Composite container) {
 		Label label;
 		GridData gridData;
 		Group group;
 		GridLayout layout;
 		// - Project -
 		group = new Group(container, SWT.NONE);
 		group.setText("Project Data");
 	    layout = new GridLayout();
 	    layout.numColumns = 2;
 	    group.setLayout(layout);
 	    gridData = new GridData(GridData.FILL_HORIZONTAL);
 	    group.setLayoutData(gridData);
 		// col:1
 		label = new Label(group, SWT.NONE);
 		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
 		label.setText("Target project (*):");
 		// col:2
 		projectCombo = new Combo(group, SWT.READ_ONLY);
 		final GridData projectComboData = new GridData(GridData.FILL_HORIZONTAL);
 		projectCombo.setLayoutData(projectComboData);
 		//projectCombo.setText("Select Project");
 		projectCombo.addListener(SWT.Selection, this);
 		// - Metric Data -
 		group = new Group(container, SWT.NONE);
 		group.setText("Metric Data");
 	    layout = new GridLayout();
 	    layout.numColumns = 2;
 	    group.setLayout(layout);
 	    gridData = new GridData(GridData.FILL_HORIZONTAL);
 	    group.setLayoutData(gridData);
 		// - Name -
 		// col:1
 		label = new Label(group, SWT.NONE);
 		label.setText("Name (*):");
 		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
 		// col:2
 		nameTextField = new Text(group, SWT.BORDER);
 		nameTextField.addListener(SWT.Modify, this);
 		nameTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		// - Id -
 		// col:1
 		label = new Label(group, SWT.NONE);
 		label.setText("Metric ID (*):");
 		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
 		// col:2
 		idTextField = new Text(group, SWT.BORDER);
 		idTextField.addListener(SWT.Modify, this);
 		idTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		// - Description -
 		// col:1
 		label = new Label(group, SWT.NONE);
 		label.setText("Description:");
 		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
 		// col:2
 		descriptionTextField = new Text(group, SWT.BORDER);
 		descriptionTextField.addListener(SWT.Modify, this);
 		descriptionTextField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		// - Source -
 		group = new Group(container, SWT.NONE);
 		group.setText("Meta Model and Context Type");
 	    layout = new GridLayout();
 	    layout.numColumns = 2;
 	    group.setLayout(layout);
 	    gridData = new GridData(GridData.FILL_HORIZONTAL);
 	    group.setLayoutData(gridData);
 	    // - Metamodel -
 	 	// col:1
 	 	label = new Label(group, SWT.NONE);
 	 	label.setText("Meta model (*):");
 	 	label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
 	 	// col:2
 	 	metamodelCombo = new Combo(group, SWT.BORDER);
 	 	metamodelCombo.addListener(SWT.Selection, this);
 	    // - Context -
 	 	// col:1
 	 	label = new Label(group, SWT.NONE);
 	 	label.setText("Context type (*):");
 	 	label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
 	 	// col:2
 	 	contextCombo = new Combo(group, SWT.BORDER);
 	 	contextCombo.addListener(SWT.Selection, this);
 	}
 	
 	private void initProjectsAndMetamodels(){
 		for (IProject project : ((INewMetricWizard) getWizard()).getProjects()) {
 			projectCombo.add(project.getName());
 		}
 		Object [] metamodelObjects = 
 				EPackage.Registry.INSTANCE.keySet().toArray(new Object[EPackage.Registry.INSTANCE.size()]);
         Arrays.sort(metamodelObjects);
 		for(Object object : metamodelObjects){
 			metamodelCombo.add(object.toString());
 		}
 	}
 	
 	private void setFixedMetamodel(String metaModel) {
 		for (String mm : metamodelCombo.getItems()) {
 			if (mm.equals(metaModel)) {
 				metamodelCombo.removeAll();
 				metamodelCombo.setItems(new String[] {metaModel});
 				metamodelCombo.select(0);
 				metamodelCombo.setEnabled(false);
 				EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(metaModel);
 				importPackage = ePackage.getClass().getPackage().getName();
 				if (importPackage.endsWith(".impl")) {
 					int length = importPackage.length();
 					importPackage = importPackage.substring(0, length-5);
 				}
 				System.out.println("importPackage: " + importPackage);
 				File jarFile;
 				try {
 					jarFile = new File
 							(ePackage.getClass().getProtectionDomain()
 							.getCodeSource().getLocation().toURI());
 					String jarName = jarFile.getName();
 					int index = jarName.indexOf("_");
 					jar = jarName.substring(0, index);
 					System.out.println("Jar5: " + jar);
 				} catch (URISyntaxException e) {
 					e.printStackTrace();
 				}
 				break;
 			}
 		}
 	}
 	
 	private void setFixedContext(String context) {
 		contextCombo.removeAll();
 		contextCombo.setItems(new String[] {context});
 		contextCombo.select(0);
 		contextCombo.setEnabled(false);
 	}
 
 	/**
 	 * Wird jedes mal ausgefhr wenn sich der Inhalt eines Textfeldes im Wizard
 	 * verndert. berprft die inhalte der Textfelder und erzeugt
 	 * entschprechende Meldungen im Wizardfenster.
 	 */
 	private void updatePageComplete() {
 		if(nameTextField.getText().isEmpty()){
 			this.setMessage("Metric name is not specified.", ERROR);
 			this.setPageComplete(false);
 		}else
 		if(idTextField.getText().isEmpty()){
 			this.setMessage("Metric ID is not specified.", ERROR);
 			this.setPageComplete(false);
 		}else
 		if(metamodelCombo.getText().isEmpty()){
 			this.setMessage("Metric metamodel is not specified.", ERROR);
 			this.setPageComplete(false);
 		}else
 		if( contextCombo.getText().isEmpty()){
 			this.setMessage("Metric context is not specified.", ERROR);
 			this.setPageComplete(false);
 		}else
 		if(!checkProject()){
 			this.setMessage("Target project for the Metric is not specified.", ERROR);
 			this.setPageComplete(false);
 		}else{
 			this.setMessage("");
 			this.setPageComplete(true);
 			saveTextFieldValues();
 		}
 	}
 	
 	private boolean checkProject(){
 		if(!projectCombo.getText().isEmpty())
 			return true;
 		return false;
 	}
 	
 	private void saveTextFieldValues(){
 		((INewMetricWizard) getWizard()).setName(this.nameTextField.getText());
 		((INewMetricWizard) getWizard()).setId(this.idTextField.getText());
 		((INewMetricWizard) getWizard()).setDescription(this.descriptionTextField.getText());
 		((INewMetricWizard) getWizard()).setMetamodel(this.metamodelCombo.getText());
 		((INewMetricWizard) getWizard()).setContext(this.contextCombo.getText());
 		((INewMetricWizard) getWizard()).setJar(jar);
 		((INewMetricWizard) getWizard()).setImportPackage(importPackage);
 	}
 
 	public void setMetamodel(String metaModel) {
 		this.metaModel = metaModel;
 	}
 
 	public void setContextType(String contextType) {
 		this.contextType = contextType;
 	}
 
 }
 	
 
