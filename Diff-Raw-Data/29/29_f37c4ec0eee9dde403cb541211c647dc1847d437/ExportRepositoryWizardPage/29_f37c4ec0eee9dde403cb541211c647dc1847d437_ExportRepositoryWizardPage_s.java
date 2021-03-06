 /**
  * Copyright (C) 2012 BonitaSoft S.A.
  * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2.0 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bonitasoft.studio.common.repository.ui.wizard;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.bonitasoft.studio.common.jface.databinding.validator.EmptyInputValidator;
 import org.bonitasoft.studio.common.log.BonitaStudioLog;
 import org.bonitasoft.studio.common.repository.Messages;
 import org.bonitasoft.studio.common.repository.RepositoryManager;
 import org.bonitasoft.studio.common.repository.model.IRepositoryFileStore;
 import org.bonitasoft.studio.common.repository.model.IRepositoryStore;
 import org.bonitasoft.studio.common.repository.operation.ExportBosArchiveOperation;
 import org.bonitasoft.studio.common.repository.ui.viewer.CheckboxRepositoryTreeViewer;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.PojoObservables;
 import org.eclipse.core.databinding.beans.PojoProperties;
 import org.eclipse.core.databinding.observable.set.IObservableSet;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.databinding.validation.MultiValidator;
 import org.eclipse.core.databinding.validation.ValidationStatus;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.databinding.wizard.WizardPageSupport;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.events.IExpansionListener;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
 import org.eclipse.ui.part.FileEditorInput;
 
 /**
  * @author Romain Bioteau
  *
  */
 public class ExportRepositoryWizardPage extends WizardPage {
 
 
     public static final String FILE_EXTENSION = "bos";
 
     private static final String STORE_DESTINATION_NAMES_ID = "ExportRepositoryWizardPage.STORE_DESTINATION_NAMES_ID";
 
     private static final int COMBO_HISTORY_LENGTH = 5;
 
     private String detinationPath ;
 
     private CheckboxRepositoryTreeViewer treeViewer;
     private final Object input;
     private final boolean isZip;
 
     private DataBindingContext dbc;
     private Set<Object> selectedFiles = new HashSet<Object>();
     private Button destinationBrowseButton;
     private Combo destinationCombo;
 
     private WizardPageSupport pageSupport;
 
     private final String defaultFileName;
 
     /**
      * @param input
      * @param isZip
      * @param showImages TODO
      * @param selectAllByDefault TODO
      * @param pageName
      */
     public ExportRepositoryWizardPage(Object input, boolean isZip, final String defaultFileName ,final String wizardTitle) {
         super(ExportRepositoryWizardPage.class.getName());
         this.isZip = isZip;
         this.defaultFileName = defaultFileName ;
         setTitle(wizardTitle);
         if(isZip){
             setDescription(Messages.exportArtifactsWizard_desc);
         }else{
             setDescription(Messages.exportArtifactsWizard_desc_toFile);
         }
         this.input = input;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
      */
     @Override
     public void createControl(Composite parent) {
         dbc = new DataBindingContext() ;
 
         final  Composite composite = new Composite(parent, SWT.NONE);
         composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
         composite.setLayout(new GridLayout(3,false));
 
 
         final Section browseRepoSection = new Section(composite, Section.NO_TITLE_FOCUS_BOX | Section.TWISTIE);
         browseRepoSection.setLayoutData(GridDataFactory.fillDefaults().grab(true,false).span(3, 1).hint(SWT.DEFAULT,300).create()) ;
         browseRepoSection.setText(Messages.browseRepository) ;
         browseRepoSection.addExpansionListener(new IExpansionListener() {
 
             @Override
             public void expansionStateChanging(ExpansionEvent event) {}
 
             @Override
             public void expansionStateChanged(ExpansionEvent event) {
                 Point defaultSize = getShell().getSize() ;
                 Point size = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true) ;
                 getShell().setSize(defaultSize.x, size.y) ;
                 getShell().layout(true, true) ;
 
             }
         }) ;
 
         browseRepoSection.setClient(createViewer(browseRepoSection)) ;
         browseRepoSection.setExpanded(true) ;
         createDestination(composite) ;
 
         pageSupport = WizardPageSupport.create(this, dbc) ;
         setControl(composite);
     }
 
     protected Control createViewer(final Composite composite) {
         treeViewer = new CheckboxRepositoryTreeViewer(composite, SWT.BORDER | SWT.V_SCROLL);
         treeViewer.setInput(input) ;
         treeViewer.getControl().setLayoutData(GridDataFactory.swtDefaults().grab(true, false).span(3, 1).hint(SWT.DEFAULT, 150).create());
 
         final IObservableSet checkedElementsObservable =  ViewersObservables.observeCheckedElements(treeViewer,Object.class) ;
         final MultiValidator notEmptyValidator = new MultiValidator() {
             @Override
             protected IStatus validate() {
                 if (checkedElementsObservable.isEmpty()) {
                     return ValidationStatus.error(Messages.selectAtLeastOneArtifact);
                 }
                 return ValidationStatus.ok();
             }
         }  ;
 
         treeViewer.collapseAll() ;
 
         dbc.addValidationStatusProvider(notEmptyValidator);
         dbc.bindSet(checkedElementsObservable, PojoObservables.observeSet(this, "selectedFiles")) ;
 
         final Set<IRepositoryFileStore> selectedChild = getArtifacts() ;
         for(IRepositoryStore store : RepositoryManager.getInstance().getCurrentRepository().getAllExportableStores()){
             List<IRepositoryFileStore> children =  store.getChildren() ;
 
             boolean containsAllChildren = !children.isEmpty() ;
             int cpt = children.size();
             int unexportable = 0;
             for(IRepositoryFileStore file : children){
                 if(!file.canBeExported()){
                     unexportable++;
                 }
                 if(!contains(selectedChild,file) && (file != null && file.canBeExported())){
                     cpt--;
                     containsAllChildren= false;
                 }
             }
 
             if(containsAllChildren){
                 treeViewer.setChecked(store, true) ;
             }else if(cpt != unexportable && cpt < children.size() && cpt > 0){
                 treeViewer.setGrayChecked(store, true) ;
             }
         }
 
         return treeViewer.getTree() ;
     }
 
     private boolean contains(Set<IRepositoryFileStore> selectedChild, IRepositoryFileStore file) {
         for(IRepositoryFileStore f : selectedChild){
             if(f.equals(file)){
                 return true ;
             }
         }
         return false;
     }
 
     @Override
     public void dispose() {
         super.dispose();
         if(pageSupport != null){
             pageSupport.dispose() ;
         }
         if(dbc != null){
             dbc.dispose() ;
         }
     }
 
 
     public Set<IRepositoryFileStore> getArtifacts() {
         Set<IRepositoryFileStore> checkedArtifacts = new HashSet<IRepositoryFileStore>();
         for (Object element : treeViewer.getCheckedElements()) {
             if(element instanceof IRepositoryFileStore){
                 checkedArtifacts.add((IRepositoryFileStore) element);
             }
         }
         return checkedArtifacts ;
     }
 
 
 
 
     public boolean finish() {
         Set<IResource> resourcesToExport = new HashSet<IResource>() ;
         saveWidgetValues() ;
 
         if(isZip){
             final ExportBosArchiveOperation operation = new ExportBosArchiveOperation() ;
             operation.setDestinationPath(getDetinationPath()) ;
             for(IRepositoryFileStore file : getArtifacts()){
                 if(file.getResource() != null && file.getResource().exists()){
                     resourcesToExport.add(file.getResource()) ;
                 }
                 if(!file.getRelatedResources().isEmpty()){
                     resourcesToExport.addAll(file.getRelatedResources()) ;
                 }
             }
             try{
                 Set<IResource> toOpen = new HashSet<IResource>();
                 for(IEditorReference ref : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences()){
                     if(ref.getEditorInput() instanceof FileEditorInput)  {
                         if(resourcesToExport.contains(((FileEditorInput)ref.getEditorInput()).getFile())){
                             toOpen.add(((FileEditorInput)ref.getEditorInput()).getFile());
                         }
                     }
                 }
                 operation.setResourcesToOpen(toOpen);
             }catch (Exception e) {
                 BonitaStudioLog.error(e);
             }
 
             operation.setResources(resourcesToExport) ;
 
             try {
                 getContainer().run(true, true, new IRunnableWithProgress() {
 
                     @Override
                     public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                         operation.run(monitor) ;
                     }
                 });
             } catch (InterruptedException e) {
                 return false;
             } catch (InvocationTargetException e) {
                 BonitaStudioLog.error(e) ;
                 return false;
             }
 
             IStatus status = operation.getStatus();
             if (!status.isOK()) {
                 ErrorDialog.openError(Display.getDefault().getActiveShell(),
                         DataTransferMessages.DataTransfer_exportProblems,
                         null, // no special message
                         status);
                 return false;
             }else{
                 MessageDialog.openInformation(getContainer().getShell(),Messages.exportLabel, Messages.exportFinishMessage) ;
             }
 
             return status.getSeverity() == IStatus.OK ;
         }else{
             try {
                 getContainer().run(false, false, new IRunnableWithProgress() {
 
                     @Override
                     public void run(IProgressMonitor monitor) throws InvocationTargetException,
                     InterruptedException {
                         monitor.beginTask(Messages.exporting, getArtifacts().size()) ;
                         File dest = new File(getDetinationPath()) ;
                         if(!dest.exists()){
                             dest.mkdirs() ;
                         }
                         for(IRepositoryFileStore file : getArtifacts()){
                             if(file.getResource() != null && file.getResource().exists()){
                                 file.export(dest.getAbsolutePath()) ;
                                 monitor.worked(1) ;
                             }
                         }
 
                     }
                 }) ;
             } catch (Exception e){
                 BonitaStudioLog.error(e) ;
             }
 
             return true ;
         }
     }
 
     protected void createDestination(final Composite group) {
         final Label destPath = new Label(group, SWT.NONE) ;
         destPath.setText(Messages.destinationPath +" *") ;
         destPath.setLayoutData(GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).create());
 
         // destination name entry field
         destinationCombo = new Combo(group, SWT.SINGLE | SWT.BORDER);
         destinationCombo.setLayoutData(GridDataFactory.fillDefaults().grab(true,false).align(SWT.FILL, SWT.CENTER).create());
 
         restoreWidgetValues() ;
         UpdateValueStrategy pathStrategy = new UpdateValueStrategy() ;
         pathStrategy.setAfterGetValidator(new EmptyInputValidator(Messages.destinationPath)) ;
         if(isZip){
             pathStrategy.setBeforeSetValidator(new IValidator() {
 
                 @Override
                 public IStatus validate(Object input) {
                     if(!input.toString().endsWith(".bos") ){
                         return ValidationStatus.error(Messages.invalidFileFormat) ;
                     }
                     if(new File(input.toString()).isDirectory()){
                         return ValidationStatus.error(Messages.invalidFileFormat) ;
                     }
                     return ValidationStatus.ok();
                 }
             }) ;
         }else{
             pathStrategy.setBeforeSetValidator(new IValidator() {
 
                 @Override
                 public IStatus validate(Object input) {
                     if(!new File(input.toString()).isDirectory()){
                         return ValidationStatus.error(Messages.destinationPathMustBeADirectory) ;
                     }
                     return ValidationStatus.ok();
                 }
             }) ;
         }
 
 
         dbc.bindValue(SWTObservables.observeText(destinationCombo), PojoProperties.value(ExportRepositoryWizardPage.class, "detinationPath").observe(this),pathStrategy,null) ;
 
 
         // destination browse button
         destinationBrowseButton= new Button(group, SWT.PUSH);
         destinationBrowseButton.setText(Messages.browse);
         destinationBrowseButton.setLayoutData(GridDataFactory.fillDefaults().hint(85,SWT.DEFAULT).create());
 
         destinationBrowseButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 handleDestinationBrowseButtonPressed();
             }
         });
     }
 
 
     /**
      *  Open an appropriate destination browser so that the user can specify a source
      *  to import from
      */
     protected void handleDestinationBrowseButtonPressed() {
         if(isZip){
             FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE | SWT.SHEET);
             // dialog.setFilterExtensions(new String[] { "*.bar" }); //$NON-NLS-1$
             dialog.setText(Messages.selectDestinationTitle);
             String currentSourceString = getDetinationPath();
             int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
             if (lastSeparatorIndex != -1) {
                 dialog.setFilterPath(currentSourceString.substring(0,
                         lastSeparatorIndex));
                 File f = new File(currentSourceString) ;
                 if(!f.isDirectory()){
                     dialog.setFileName(f.getName()) ;
                 }
 
                 dialog.setFilterExtensions(new String[]{"*."+FILE_EXTENSION}) ;
             }
             String selectedFileName = dialog.open();
 
             if (selectedFileName != null) {
                 destinationCombo.setText(selectedFileName);
             }
         }else{
             DirectoryDialog dialog = new DirectoryDialog(getContainer().getShell(), SWT.SAVE | SWT.SHEET);
             // dialog.setFilterExtensions(new String[] { "*.bar" }); //$NON-NLS-1$
             dialog.setText(Messages.selectDestinationTitle);
             String currentSourceString = getDetinationPath();
             int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
             if (lastSeparatorIndex != -1) {
                 dialog.setFilterPath(currentSourceString.substring(0,
                         lastSeparatorIndex));
             }
             String selectedFileName = dialog.open();
 
             if (selectedFileName != null) {
                 destinationCombo.setText(selectedFileName);
             }
         }
 
     }
 
 
     /**
      *  Hook method for restoring widget values to the values that they held
      *  last time this wizard was used to completion.
      */
     protected void restoreWidgetValues() {
         IDialogSettings settings = getDialogSettings();
         if (settings != null) {
             String[] directoryNames = settings
                     .getArray(STORE_DESTINATION_NAMES_ID);
             if (directoryNames == null || directoryNames.length == 0) {
                 String path = System.getProperty("user.home") ;
                 if(defaultFileName != null && isZip){
                     path =  path + File.separator + defaultFileName ;
                 }
                 setDetinationPath(path);
                 return; // ie.- no settings stored
             }
 
             // destination
 
             String oldPath = directoryNames[0] ;
             if(defaultFileName != null && isZip){
                 File f =  new File(oldPath) ;
                 if(f.isFile()){
                     oldPath =  f.getParentFile().getAbsolutePath() + File.separator + defaultFileName ;
                 }else{
                     oldPath =  oldPath + File.separator + defaultFileName ;
                 }
 
             }else if(!isZip){
                 File f =  new File(oldPath) ;
                 if(f.isFile()){
                     oldPath =  f.getParentFile().getAbsolutePath()  ;
                 }
             }
             setDetinationPath(oldPath);
             for (int i = 0; i < directoryNames.length; i++) {
                 addDestinationItem(directoryNames[i]);
             }
         }
     }
 
     /**
      *  Hook method for saving widget values for restoration by the next instance
      *  of this class.
      */
     protected void saveWidgetValues() {
         // update directory names history
         IDialogSettings settings = getDialogSettings();
         if (settings != null) {
             String[] directoryNames = settings
                     .getArray(STORE_DESTINATION_NAMES_ID);
             if (directoryNames == null) {
                 directoryNames = new String[0];
             }
 
             String dest = getDetinationPath();
             if(dest.endsWith(".bos")){
                 dest = new File(dest).getParentFile().getAbsolutePath();
             }
             directoryNames = addToHistory(directoryNames,dest);
             settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);
         }
     }
 
     /**
      * Adds an entry to a history, while taking care of duplicate history items
      * and excessively long histories.  The assumption is made that all histories
      * should be of length <code>WizardDataTransferPage.COMBO_HISTORY_LENGTH</code>.
      *
      * @param history the current history
      * @param newEntry the entry to add to the history
      */
     protected String[] addToHistory(String[] history, String newEntry) {
         java.util.ArrayList l = new java.util.ArrayList(Arrays.asList(history));
         addToHistory(l, newEntry);
         String[] r = new String[l.size()];
         l.toArray(r);
         return r;
     }
 
     /**
      * Adds an entry to a history, while taking care of duplicate history items
      * and excessively long histories.  The assumption is made that all histories
      * should be of length <code>WizardDataTransferPage.COMBO_HISTORY_LENGTH</code>.
      *
      * @param history the current history
      * @param newEntry the entry to add to the history
      */
     protected void addToHistory(List history, String newEntry) {
         history.remove(newEntry);
         history.add(0, newEntry);
 
         // since only one new item was added, we can be over the limit
         // by at most one item
         if (history.size() > COMBO_HISTORY_LENGTH) {
             history.remove(COMBO_HISTORY_LENGTH);
         }
     }
 
 
     /**
      *  Add the passed value to self's destination widget's history
      *
      *  @param value java.lang.String
      */
     protected void addDestinationItem(String value) {
         destinationCombo.add(value);
     }
 
     public String getDetinationPath() {
         return detinationPath;
     }
 
     public void setDetinationPath(String detinationPath) {
         this.detinationPath = detinationPath;
     }
 
     public Set<Object> getSelectedFiles() {
         return selectedFiles;
     }
 
     public void setSelectedFiles(Set<Object> selectedFiles) {
         this.selectedFiles = selectedFiles;
     }
 
 }
