 package org.rubypeople.rdt.testunit.wizards;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogPage;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.ISourceFolderRoot;
 import org.rubypeople.rdt.core.IType;
 import org.rubypeople.rdt.core.RubyConventions;
 import org.rubypeople.rdt.core.RubyModelException;
 import org.rubypeople.rdt.core.search.IRubySearchConstants;
 import org.rubypeople.rdt.core.search.IRubySearchScope;
 import org.rubypeople.rdt.core.search.SearchEngine;
 import org.rubypeople.rdt.internal.corext.codemanipulation.StubUtility;
 import org.rubypeople.rdt.internal.corext.util.Messages;
 import org.rubypeople.rdt.internal.testunit.util.LayoutUtil;
 import org.rubypeople.rdt.internal.testunit.util.TestUnitStatus;
 import org.rubypeople.rdt.internal.testunit.wizards.MethodStubsSelectionButtonGroup;
 import org.rubypeople.rdt.internal.testunit.wizards.WizardMessages;
 import org.rubypeople.rdt.internal.ui.dialogs.TypeSelectionDialog2;
 import org.rubypeople.rdt.ui.wizards.NewTypeWizardPage;
 
 public class RubyNewTestCaseWizardPage extends NewTypeWizardPage {
 
 	private final static String PAGE_NAME= "NewTestCaseCreationWizardPage"; //$NON-NLS-1$
 	
 	/** Field ID of the class under test field. */
 	public final static String CLASS_UNDER_TEST= PAGE_NAME + ".classundertest"; //$NON-NLS-1$
 	
 	private final static String TEST_SUFFIX= "Test"; //$NON-NLS-1$
 	
 	private final static String STORE_SETUP= PAGE_NAME + ".USE_SETUP";	//$NON-NLS-1$
 	private final static String STORE_TEARDOWN= PAGE_NAME + ".USE_TEARDOWN"; //$NON-NLS-1$
 	private final static String STORE_CONSTRUCTOR= PAGE_NAME + ".USE_CONSTRUCTOR"; //$NON-NLS-1$
 	
 	private final static int IDX_SETUP= 0;
 	private final static int IDX_TEARDOWN= 1;
 	private final static int IDX_CONSTRUCTOR= 2;
 	
     private String fClassUnderTestText;
 	private MethodStubsSelectionButtonGroup fMethodStubsButtons;
 
 	private Text fClassUnderTestControl;
 	private Button fClassUnderTestButton;
 
 	private IStatus fClassUnderTestStatus;
 
 	private IType fClassUnderTest;
 
     /**
      * Constructor for RubyNewTestCaseWizardPage.
      * 
      * @param pageName
      */
     public RubyNewTestCaseWizardPage() {
     	super(true, PAGE_NAME);
 
     	setTitle(WizardMessages.NewTestCaseWizardPage_title);
         setDescription(WizardMessages.NewTestCaseWizardPage_description);
 		
         String[] buttonNames= new String[] {
     			/* IDX_SETUP */ WizardMessages.NewTestCaseWizardPage_methodStub_setUp, 
     			/* IDX_TEARDOWN */ WizardMessages.NewTestCaseWizardPage_methodStub_tearDown, 
     			/* IDX_CONSTRUCTOR */ WizardMessages.NewTestCaseWizardPage_methodStub_constructor
     		};
 		
 		fMethodStubsButtons= new MethodStubsSelectionButtonGroup(SWT.CHECK, buttonNames, 2);
 		fMethodStubsButtons.setLabelText(WizardMessages.NewTestCaseWizardPage_method_Stub_label); 
 		 
 		fClassUnderTestStatus= new TestUnitStatus();
 		
 		fClassUnderTestText= ""; //$NON-NLS-1$
     }
 
     /**
      * @see IDialogPage#createControl(Composite)
      */
     public void createControl(Composite parent) {
     	
     	Composite container= new Composite(parent, SWT.NONE);
 		
 		int nColumns= 4;
 		
 		GridLayout layout= new GridLayout();
 		layout.numColumns= nColumns;		
 		container.setLayout(layout);
     	
 		createContainerControls(container, nColumns);
		createPackageControls(container, nColumns);
 		createSeparator(container, nColumns);
 		createTypeNameControls(container, nColumns);                     
 		createSuperClassControls(container, nColumns);
 		createMethodStubSelectionControls(container, nColumns);
         createSeparator(container, nColumns);
         createClassUnderTestControls(container, nColumns);        
 
 		setControl(container);
 		setSuperClass("Test::Unit::TestCase", true);
 		
 		//set default and focus
 		String classUnderTest= getClassUnderTestText();
 		if (classUnderTest.length() > 0) {
 			setTypeName(classUnderTest+TEST_SUFFIX, true);
 		}
 
 		Dialog.applyDialogFont(container);
 		// TODO Uncomment
 //		PlatformUI.getWorkbench().getHelpSystem().setHelp(container, ITestUnitHelpContextIds.NEW_TESTCASE_WIZARD_PAGE);
 		
 		setFocus();
     }
     
     /* (non-Javadoc)
 	 * @see org.eclipse.jdt.ui.wizards.NewTypeWizardPage#createTypeMembers(org.eclipse.jdt.core.IType, org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	protected void createTypeMembers(IType type, IProgressMonitor monitor) throws CoreException {	
 		String lineDelimiter= StubUtility.getLineDelimiterUsed(type.getRubyProject());
 	
 		if (fMethodStubsButtons.isSelected(IDX_CONSTRUCTOR)) {
 			createConstructor(type, lineDelimiter); 	
 		}
 		
 		if (fMethodStubsButtons.isSelected(IDX_SETUP)) {
 			createSetUp(type, lineDelimiter);
 		}
 		
 		if (fMethodStubsButtons.isSelected(IDX_TEARDOWN)) {
 			createTearDown(type, lineDelimiter);
 		}
 
 		if (fClassUnderTest != null) {
 			createTestMethodStubs(type, lineDelimiter);
 		}		
 	}
 
 	@Override
 	protected List<String> addImports() {
 		List<String> imports =  new ArrayList<String>();
 		imports.add("test/unit");
 		// TODO Add import for class under test?
 		return imports;
 	}
 
 	private void createConstructor(IType type, String lineDelimiter) throws CoreException {
     	StringBuffer content = new StringBuffer("def initialize");
     	content.append(lineDelimiter);
     	content.append("  super");
     	content.append(lineDelimiter);
     	content.append("end");
     	content.append(lineDelimiter);
     	type.createMethod(content.toString(), null, true, null);		
 	}
 	
 	private void createSetUp(IType type, String lineDelimiter) throws CoreException {
     	StringBuffer content = new StringBuffer("def setup");
     	content.append(lineDelimiter);
     	content.append("  super");
     	content.append(lineDelimiter);
     	content.append("end");
     	content.append(lineDelimiter);
     	type.createMethod(content.toString(), null, true, null);		
 	}
 	
 	private void createTearDown(IType type, String lineDelimiter) throws CoreException {
     	StringBuffer content = new StringBuffer("def teardown");
     	content.append(lineDelimiter);
     	content.append("  super");
     	content.append(lineDelimiter);
     	content.append("end");
     	content.append(lineDelimiter);
     	type.createMethod(content.toString(), null, true, null);		
 	}
 	
 	private void createTestMethodStubs(IType type, String lineDelimiter) throws CoreException {
 //    	StringBuffer content = new StringBuffer("def setup");
 //    	content.append("  super\n");
 //    	content.append("end\n");
 //    	type.createMethod(content.toString(), null, true, null);		
 	}
 
 	/**
 	 * Creates the controls for the method stub selection buttons. Expects a <code>GridLayout</code> with 
 	 * at least 3 columns.
 	 * 
 	 * @param composite the parent composite
 	 * @param nColumns number of columns to span
 	 */		
 	protected void createMethodStubSelectionControls(Composite composite, int nColumns) {
 		LayoutUtil.setHorizontalSpan(fMethodStubsButtons.getLabelControl(composite), nColumns);
 		LayoutUtil.createEmptySpace(composite, 1);
 		LayoutUtil.setHorizontalSpan(fMethodStubsButtons.getSelectionButtonsGroup(composite), nColumns - 1);
 	}	
     
     /**
 	 * Creates the controls for the 'class under test' field. Expects a <code>GridLayout</code> with 
 	 * at least 3 columns.
 	 * 
 	 * @param composite the parent composite
 	 * @param nColumns number of columns to span
 	 */		
 	protected void createClassUnderTestControls(Composite composite, int nColumns) {
 		Label classUnderTestLabel= new Label(composite, SWT.LEFT | SWT.WRAP);
 		classUnderTestLabel.setFont(composite.getFont());
 		classUnderTestLabel.setText(WizardMessages.NewTestCaseWizardPage_class_to_test_label); 
 		classUnderTestLabel.setLayoutData(new GridData());
 
 		fClassUnderTestControl= new Text(composite, SWT.SINGLE | SWT.BORDER);
 		fClassUnderTestControl.setEnabled(true);
 		fClassUnderTestControl.setFont(composite.getFont());
 		fClassUnderTestControl.setText(fClassUnderTestText);
 		fClassUnderTestControl.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				internalSetClassUnderText(((Text) e.widget).getText());
 			}
 		});
 		GridData gd= new GridData();
 		gd.horizontalAlignment= GridData.FILL;
 		gd.grabExcessHorizontalSpace= true;
 		gd.horizontalSpan= nColumns - 2;
 		fClassUnderTestControl.setLayoutData(gd);
 		
 		fClassUnderTestButton= new Button(composite, SWT.PUSH);
 		fClassUnderTestButton.setText(WizardMessages.NewTestCaseWizardPage_class_to_test_browse); 
 		fClassUnderTestButton.setEnabled(true);
 		fClassUnderTestButton.addSelectionListener(new SelectionListener() {
 			public void widgetDefaultSelected(SelectionEvent e) {
 				classToTestButtonPressed();
 			}
 			public void widgetSelected(SelectionEvent e) {
 				classToTestButtonPressed();
 			}
 		});	
 		gd= new GridData();
 		gd.horizontalAlignment= GridData.FILL;
 		gd.grabExcessHorizontalSpace= false;
 		gd.horizontalSpan= 1;
 		gd.widthHint = LayoutUtil.getButtonWidthHint(fClassUnderTestButton);		
 		fClassUnderTestButton.setLayoutData(gd);
 
 //		ControlContentAssistHelper.createTextContentAssistant(fClassUnderTestControl, fClassToTestCompletionProcessor);
 	}
 	
 	private IType chooseClassToTestType() {	
		ISourceFolderRoot root= getSourceFolderRoot();
 		if (root == null) {
 			return null;
 		}
 		
 		IRubyElement[] elements= new IRubyElement[] { root.getRubyProject() };
 		IRubySearchScope scope= SearchEngine.createRubySearchScope(elements);
 
 		TypeSelectionDialog2 dialog= new TypeSelectionDialog2(getShell(), false,
 			getWizard().getContainer(), scope, IRubySearchConstants.CLASS);
 		dialog.setTitle(WizardMessages.NewTestCaseWizardPage_class_to_test_dialog_title); 
 		dialog.setMessage(WizardMessages.NewTestCaseWizardPage_class_to_test_dialog_message); 
 
 		if (dialog.open() == Window.OK) {
 			return (IType) dialog.getFirstResult();
 		}
 		return null;
 	}
 	
 	private void classToTestButtonPressed() {
 		IType type= chooseClassToTestType();
 		if (type != null) {
 			setClassUnderTest(type.getElementName());
 		}
 	}
 	
 	/**
 	 * Sets the name of the class under test.
 	 * 
 	 * @param name The name to set
 	 */		
 	public void setClassUnderTest(String name) {
 		if (fClassUnderTestControl != null && !fClassUnderTestControl.isDisposed()) {
 			fClassUnderTestControl.setText(name);
 		}
 		internalSetClassUnderText(name);
 	}
 	
 	private void internalSetClassUnderText(String name) {
 		fClassUnderTestText= name;
 		fClassUnderTestStatus= classUnderTestChanged();
 		handleFieldChanged(CLASS_UNDER_TEST);
 	}
 
 	
 	/**
 	 * Hook method that gets called when the class under test has changed. The method class under test
 	 * returns the status of the validation.
 	 * <p>
 	 * Subclasses may extend this method to perform their own validation.
 	 * </p>
 	 * 
 	 * @return the status of the validation
 	 */
 	protected IStatus classUnderTestChanged() {
 		TestUnitStatus status= new TestUnitStatus();
 		
 		
 		fClassUnderTest= null;
 		
		ISourceFolderRoot root = getSourceFolderRoot();
 		if (root == null) {
 			return status;
 		}
 		
 		String classToTestName= getClassUnderTestText();
 		if (classToTestName.length() == 0) {
 			return status;
 		}
 		IStatus val= RubyConventions.validateRubyTypeName(classToTestName);
 		if (val.getSeverity() == IStatus.ERROR) {
 			status.setError(WizardMessages.NewTestCaseWizardPage_error_class_to_test_not_valid); 
 			return status;
 		}
 		
 		try {		
 			IType type= resolveClassNameToType(root.getRubyProject(), classToTestName);
 			if (type == null) {
 				status.setError(WizardMessages.NewTestCaseWizardPage_error_class_to_test_not_exist); 
 				return status;
 			}
 			if (type.isModule()) {
 				status.setWarning(Messages.format(WizardMessages.NewTestCaseWizardPage_warning_class_to_test_is_interface, classToTestName)); 
 			}
 			
 			fClassUnderTest= type;
 //			fPage2.setClassUnderTest(fClassUnderTest);
 		} catch (RubyModelException e) {
 			status.setError(WizardMessages.NewTestCaseWizardPage_error_class_to_test_not_valid); 
 		} 
 		return status;
 	}
 	
 	private IType resolveClassNameToType(IRubyProject rproject, String classToTestName) throws RubyModelException {
 		if (!rproject.exists()) {
 			return null;
 		}
 		
 		IType type= rproject.findType(classToTestName);
 		return type;
 	}
 	
 	/**
 	 * Returns the content of the class to test text field.
 	 * 
 	 * @return the name of the class to test
 	 */
 	public String getClassUnderTestText() {
 		return fClassUnderTestText;
 	}
 	
 		/**
 	 * The wizard owning this page is responsible for calling this method with the
 	 * current selection. The selection is used to initialize the fields of the wizard 
 	 * page.
 	 * 
 	 * @param selection used to initialize the fields
 	 */
 	public void init(IStructuredSelection selection) {
 		IRubyElement jelem= getInitialRubyElement(selection);
 		initContainerPage(jelem);
 		initTypePage(jelem);
 		
 		restoreWidgetValues();
 		
 		doStatusUpdate();
 		
 //		boolean createConstructors= false;
 //		boolean createUnimplemented= true;
 //		IDialogSettings dialogSettings= getDialogSettings();
 //		if (dialogSettings != null) {
 //			IDialogSettings section= dialogSettings.getSection(PAGE_NAME);
 //			if (section != null) {
 //				createConstructors= section.getBoolean(SETTINGS_CREATECONSTR);
 //			}
 //		}
 //		
 //		setMethodStubSelection(createConstructors, true);
 	}
 	
 	/**
 	 * Returns all status to be consider for the validation. Clients can override.
 	 * @return The list of status to consider for the validation.
 	 */
 	protected IStatus[] getStatusList() {
 		return new IStatus[] {
 				fContainerStatus,
 				fTypeNameStatus,
 				fClassUnderTestStatus,
 				fSuperClassStatus
 		};
 	}
 	
 	/**
 	 *	Use the dialog store to restore widget values to the values that they held
 	 *	last time this wizard was used to completion
 	 */
 	private void restoreWidgetValues() {
 		IDialogSettings settings= getDialogSettings();
 		if (settings != null) {
 			fMethodStubsButtons.setSelection(IDX_SETUP, settings.getBoolean(STORE_SETUP));
 			fMethodStubsButtons.setSelection(IDX_TEARDOWN, settings.getBoolean(STORE_TEARDOWN));
 			fMethodStubsButtons.setSelection(IDX_CONSTRUCTOR, settings.getBoolean(STORE_CONSTRUCTOR));
 		} else {
 			fMethodStubsButtons.setSelection(IDX_SETUP, false); //setUp
 			fMethodStubsButtons.setSelection(IDX_TEARDOWN, false); //tearDown
 			fMethodStubsButtons.setSelection(IDX_CONSTRUCTOR, false); //constructor
 		}
 	}	
 }
