 package org.eclipse.jst.jsf.ui.internal.classpath;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.ArchiveFile;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibrary;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibraryRegistryFactory;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFVersion;
 import org.eclipse.jst.jsf.ui.internal.JSFUiPlugin;
 import org.eclipse.jst.jsf.ui.internal.Messages;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Common control for adding JSF library instances
  */
 public class JSFLibraryEditControl extends Composite implements ModifyListener, SelectionListener {
 	private Text txtName;
 	private Label lblName;
 	private CCombo cboVersions;
 	private Label lblVersions;
 	private Button chkDeploy;
 	private Button chkImpl;
 	private Composite btnBar;
 	private Button btnAdd;
 	private Button btnRemove;
 	private TableViewer jars;
 	
 	private boolean initing = false;
 
 	private JSFLibrary workingCopyLibrary;
 	
 	private String validationMsg;
 	private Set _listeners;
 	private int _isNew = -1;//use isNew() method.. not this variable directly
 
 	/**
 	 * @param workingCopyLibrary  working copy of the JSF library
 	 * @param parent parent SWT control
 	 */
 	public JSFLibraryEditControl(JSFLibrary workingCopyLibrary, Composite parent){
 		super(parent, SWT.NONE);
 		this.workingCopyLibrary = workingCopyLibrary;
 		_listeners = new HashSet(1);
 		createControl(parent);
 	}
 	
 	/**
 	 * @param listener
 	 */
 	public void addValidationListener(JSFLibraryValidationListener listener){
 		removeValidationListener(listener);
 		_listeners.add(listener);
 	}
 	
 	/**
 	 * @param listener
 	 */
 	public void removeValidationListener(JSFLibraryValidationListener listener){
 		_listeners.remove(listener);
 	}
 	
 	/**
 	 * @param parent
 	 */
 	public void createControl(Composite parent) {
 		initing = true;
 
 //		this = new Composite(parent, SWT.NONE);
 		this.setLayout(new GridLayout(2, false));
 		this.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		lblName = new Label(this, SWT.NONE);
 		lblName.setText(Messages.JSFLibraryWizard_LibraryName);
 		lblName.setLayoutData(new GridData(GridData.BEGINNING));
 
 		txtName = new Text(this, SWT.BORDER);
 		txtName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		txtName.addModifyListener(this);
 
 		lblVersions = new Label(this, SWT.NONE);
 		lblVersions.setText(Messages.JSFLibraryWizard_VersionSupported);
 
 		cboVersions = new CCombo(this, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
 		cboVersions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		cboVersions.addModifyListener(this);
 
 		Group jarsComp = new Group(this, SWT.NONE);
 		jarsComp.setText(Messages.JSFLibraryWizard_LibraryJars);
 		GridLayout gl1 = new GridLayout(2, false);
 		jarsComp.setLayout(gl1);
 		GridData gd2 = new GridData(GridData.FILL_HORIZONTAL
 				| GridData.FILL_VERTICAL);
 		gd2.horizontalSpan = 2;
 		jarsComp.setLayoutData(gd2);
 
 		createJarsViewer(jarsComp);
 
 		createButtons(jarsComp);
 
 		chkImpl = new Button(this, SWT.CHECK);
 		chkImpl.setText(Messages.JSFLibraryWizard_IsJSFImplementation);
 		GridData gd3 = new GridData();
 		gd3.horizontalSpan = 2;
 		chkImpl.setLayoutData(gd3);
 		chkImpl.addSelectionListener(this);
 
 		chkDeploy = new Button(this, SWT.CHECK);
 		chkDeploy.setText(Messages.JSFLibraryWizard_DeployJars);
 		GridData gd4 = new GridData();
 		gd4.horizontalSpan = 2;
 		chkDeploy.setLayoutData(gd4);
 //		chkDeploy.addSelectionListener(this);
 		chkDeploy.setVisible(false);
 
 		loadVersions();
 
 		if (!isNew()) {
 			txtName.setText(workingCopyLibrary.getName());
			if (workingCopyLibrary.getJSFVersion().getName().equals(JSFVersion.UNKNOWN_LITERAL.getName())) {
				cboVersions.setText(Messages.JSFLibraryEditControl_ImplVersion_UNKNOWN);
			} else {
				cboVersions.setText(workingCopyLibrary.getJSFVersion().getName());
			}
 			chkDeploy.setSelection(workingCopyLibrary.isDeployed());
 			chkImpl.setSelection(workingCopyLibrary.isImplementation());
 		}
 		jars.setInput(workingCopyLibrary);
 
 		initing = false;
 
 		txtName.setFocus();
 	}
 
 	private void createJarsViewer(Group jarsComp) {
 		jars = new TableViewer(jarsComp, SWT.BORDER | SWT.MULTI);
 		jars.setContentProvider(new IStructuredContentProvider() {
 			public Object[] getElements(Object inputElement) {
 				if (inputElement instanceof JSFLibrary)
 					return ((JSFLibrary) inputElement).getArchiveFiles()
 							.toArray();
 				return new Object[0];
 			}
 
 			public void dispose() {
                 // do nothing
 			}
 
 			public void inputChanged(Viewer viewer, Object oldInput,
 					Object newInput) {
 				// won't happen
 			}
 		});
 		jars.setLabelProvider(new ILabelProvider() {
 			private Image jarImg = null;
 			public Image getImage(Object element) {
 				if (jarImg == null){
 					ImageDescriptor desc = JSFUiPlugin.getImageDescriptor("obj16/jar_obj.gif"); //$NON-NLS-1$
 					jarImg = desc.createImage();
 				}
 				return jarImg;
 			}
 
 			public String getText(Object element) {
 				StringBuffer labelBuf = new StringBuffer();
 				if (element instanceof ArchiveFile) {
 					ArchiveFile archive = (ArchiveFile) element;
 					labelBuf.append(archive.getName());	
 					if (!archive.exists())
 						labelBuf.append("[missing]"); //$NON-NLS-1$
 					labelBuf.append(" - ").append(archive.getPath()); //$NON-NLS-1$
 				}
 				return labelBuf.toString();
 			}
 
 			public void addListener(ILabelProviderListener listener) {
                 // no listeners supported
 			}
 
 			public void dispose() {
 				if (jarImg != null)
 					jarImg.dispose();
 			}
 
 			public boolean isLabelProperty(Object element, String property) {
 				return false;
 			}
 
 			public void removeListener(ILabelProviderListener listener) {
                 // no listeners supported
 			}
 		});
 
 		jars.addSelectionChangedListener(new ISelectionChangedListener(){
 			public void selectionChanged(SelectionChangedEvent event){
 				updateButtons();
 			}
 		});
 		
 		GridData gd = new GridData(GridData.FILL_BOTH);
 //		gd.widthHint = convertWidthInCharsToPixels(30);
 //		gd.heightHint = convertHeightInCharsToPixels(10);
 		jars.getControl().setLayoutData(gd);
 	}
 
 	private void updateButtons() {
 		btnRemove.setEnabled(!((StructuredSelection)jars.getSelection()).isEmpty());
 		// getButton(IDialogConstants.OK_ID).setEnabled(modified);
 	}
 
 	private void createButtons(Composite c) {
 		btnBar = new Composite(c, SWT.NONE);
 		GridLayout gl = new GridLayout(1, false);
 		gl.marginHeight = 0;
 		gl.marginTop = 0;
 		gl.marginWidth = 0;
 		btnBar.setLayout(gl);
 		btnBar.setLayoutData(new GridData(GridData.END));
 
 		btnAdd = new Button(btnBar, SWT.NONE);
 		btnAdd.setText(Messages.JSFLibraryWizard_Add);
 		btnAdd.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
 				| GridData.VERTICAL_ALIGN_BEGINNING));
 		btnAdd.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				String cur = null;
 				String[] chosenJars = openExtJarFileDialog(cur);
 				if (chosenJars != null) {
 					for (int i = 0; i < chosenJars.length; i++) {
 						String jar = chosenJars[i];
 						if (!workingCopyLibrary.containsArchiveFile(jar)) {
 							ArchiveFile archive = JSFLibraryRegistryFactory.eINSTANCE
 								.createArchiveFile();
 							archive.setSourceLocation(jar);
 							archive.setRelativeDestLocation("WEB-INF/lib"); //$NON-NLS-1$
 							workingCopyLibrary.getArchiveFiles().add(archive);
 						}
 					}
 					jars.refresh();
 					validate();
 				}
 			}
 		});
 
 		btnRemove = new Button(btnBar, SWT.NONE);
 		btnRemove.setEnabled(false);
 		btnRemove.setText(Messages.JSFLibraryWizard_Remove);
 		btnRemove.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
 				| GridData.VERTICAL_ALIGN_BEGINNING));
 		btnRemove.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (jars.getSelection() instanceof StructuredSelection){
 					StructuredSelection objs = (StructuredSelection)jars.getSelection();
 					if (objs != null){
 						Iterator it = objs.iterator();
 						 while (it.hasNext()){
 							 Object obj = it.next();
 							 ArchiveFile jar = (ArchiveFile)obj;
 							 workingCopyLibrary.getArchiveFiles().remove(jar);							 
 						 }
 					}
 					jars.refresh();
 					validate();
 				}
 			}
 		});
 	}
 
 	private void loadVersions() {
 		cboVersions.removeAll();
 		Iterator it = JSFVersion.VALUES.iterator();
 		while (it.hasNext()) {
 			JSFVersion ver = (JSFVersion) it.next();
			if (ver.getName().equals(JSFVersion.UNKNOWN_LITERAL.getName())) {
				cboVersions.add(Messages.JSFLibraryEditControl_ImplVersion_UNKNOWN);
			} else {
				cboVersions.add(ver.getName());
			}
 		}
 	}
 
 	private String[] openExtJarFileDialog(String existing) {
 		String title = Messages.JSFLibraryWizard_ExtJarFileDialogTitle;
 
 		FileDialog dialog = new FileDialog(getShell(),
 				existing == null ? SWT.MULTI : SWT.SINGLE);
 		dialog.setText(title);
 		dialog.setFilterExtensions(new String[] { "*.jar;*.zip" }); //$NON-NLS-1$
 		// FIXME: remember and use last path chosen??
 		String filterPath = ResourcesPlugin.getWorkspace().getRoot()
 				.getFullPath().toString();
 		dialog.setFilterPath(filterPath);
 		// if (existing != null) {
 		// dialog.setFileName(existing.getPath().lastSegment());
 		// }
 
 		String res = dialog.open();
 		if (res == null) {
 			return null;
 		}
 		String[] fileNames = dialog.getFileNames();
 		String[] elems = new String[fileNames.length];
 		IPath file = new Path(res);
 		IPath apath = file.removeLastSegments(1);
 		for (int i = 0; i < fileNames.length; i++) {
 			elems[i] = apath.append(fileNames[i]).toString();
 		}
 		return elems;
 	}	
 
 	public void modifyText(ModifyEvent e) {
 		validate();
 		updateButtons();	
 	}
 
 	private void fireValidateEvent(final JSFLibraryValidationEvent jSFLibraryValidationEvent) {
 		new Runnable(){
 			public void run() {
 				for (Iterator it=_listeners.iterator();it.hasNext();){
 					JSFLibraryValidationListener listener = (JSFLibraryValidationListener)it.next();
 					listener.notifyValidation(jSFLibraryValidationEvent);
 				}
 			}
 		}.run();
 	}
 
 
 	public void widgetSelected(SelectionEvent e) {
 		validate();
 		updateButtons();
 	}
 
 	public void widgetDefaultSelected(SelectionEvent e) {
         // no handling for default selection
 	}
 
 	private void validate() {
 		if (initing)
 			return;
 		validationMsg = null;
 //		setPageComplete(true);
 		if (!validateName() || !validateJars() || !validateVersion()) {
 //			setPageComplete(false);
 		}
 		fireValidateEvent(new JSFLibraryValidationEvent(validationMsg));
 	}
 	
 	private boolean validateJars() {
 		if (workingCopyLibrary.getArchiveFiles().isEmpty()) {
 			validationMsg = Messages.JSFLibraryWizard_ValidateNoJars;
 			return false;
 		}
 		return true;
 	}
 	
 	private boolean validateVersion() {
 		//FIXME: why isn't selection indesx correct???
 	//	if (cboVersions.getSelectionIndex() < 0) {
 	//		setErrorMessage("Choose the maximum JSF version supported if known.");
 	//		return false;
 	//	}
 		return true;
 	}
 	
 	private boolean validateName() {
 		if (txtName.getText() == null
 				|| txtName.getText().trim().equals("")) { //$NON-NLS-1$
 			validationMsg = Messages.JSFLibraryWizard_ValidateNoLibraryName;
 			return false;
 		}
 		String aName = txtName.getText().trim();
 		if (isNew() || (!isNew() && !getCurrentLibraryName().equals(aName))) {
 			if (isNameInRegistry(JSFCorePlugin.getDefault()
 					.getJSFLibraryRegistry().getAllJSFLibraries(), aName)) {
 				validationMsg = Messages.JSFLibraryWizard_ValidateExistingLibraryName;
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	private boolean isNew() {
 		if (_isNew == -1){
 			_isNew = workingCopyLibrary.getName() == null ? 1 : 0;
 		}
 		return _isNew == 1;
 	}
 
 	private String getCurrentLibraryName() {
 		return workingCopyLibrary.getName();		
 	}
 
 	private boolean isNameInRegistry(Collection c, String name) {
 		Iterator it = c.iterator();
 		while (it.hasNext()) {
 			JSFLibrary lib = (JSFLibrary) it.next();
 			if (lib.getName().equals(name)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @return the trimmed name of the user input for jsf library name
 	 */
 	public String getJSFLibraryName() {		
 		return txtName.getText().trim();
 	}
 
 
 	/**
 	 * @return the value of the user input for the isDeployed checkbox
 	 */
 	public boolean getIsDeployed() {		
 		return chkDeploy.getSelection();
 	}
 
 
 	/**
 	 * @return the value of the user input for the isJSFImplementation checkbox
 	 */
 	public boolean getIsImplementation() {
 		return chkImpl.getSelection();
 	}
 
 	/**
 	 * @return the jsf version selected in the version dropping
 	 */
 	public JSFVersion getJSFVersion() {
 		if (cboVersions.getSelectionIndex() >= 0) {
 			JSFVersion ver = (JSFVersion) JSFVersion.VALUES.get(cboVersions
 					.getSelectionIndex());
 			return ver;
 		}
 		return JSFVersion.UNKNOWN_LITERAL;
 	}
 
 	/**
 	 * @param implsOnly
 	 */
 	public void setImplOnly(boolean implsOnly) {
 		if (implsOnly){
 			chkImpl.setSelection(true);
 			chkImpl.setEnabled(false);
 		}
 	}
 	
 	/**
 	 * @param nonImplsOnly
 	 */
 	public void setNonImplOnly(boolean nonImplsOnly) {
 		if (nonImplsOnly){
 			chkImpl.setSelection(false);
 			chkImpl.setEnabled(false);
 		}
 	}
 }
