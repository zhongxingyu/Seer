 /*
  * See the COPYRIGHT.txt file distributed with this work for information
  * regarding copyright ownership.
  *
  * This software is made available by Red Hat, Inc. under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution and is
  * available at http://www.eclipse.org/legal/epl-v10.html.
  *
  * See the AUTHORS.txt file in the distribution for a full listing of
  * individual contributors.
  */
 package org.jboss.tools.modeshape.rest.wizards;
 
 import static org.jboss.tools.modeshape.rest.IUiConstants.HelpContexts.PUBLISH_DIALOG_HELP_CONTEXT;
 import static org.jboss.tools.modeshape.rest.IUiConstants.Preferences.ENABLE_RESOURCE_VERSIONING;
 import static org.jboss.tools.modeshape.rest.IUiConstants.Preferences.IGNORED_RESOURCES_PREFERENCE;
 import static org.jboss.tools.modeshape.rest.IUiConstants.Preferences.PUBLISHING_PREFERENCE_PAGE_ID;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.wizard.IWizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.eclipse.ui.help.IWorkbenchHelpSystem;
 import org.jboss.tools.modeshape.rest.Activator;
 import org.jboss.tools.modeshape.rest.IServerRegistryListener;
 import org.jboss.tools.modeshape.rest.RestClientI18n;
 import org.jboss.tools.modeshape.rest.ServerManager;
 import org.jboss.tools.modeshape.rest.ServerRegistryEvent;
 import org.jboss.tools.modeshape.rest.Utils;
 import org.jboss.tools.modeshape.rest.actions.AddPublishAreaAction;
 import org.jboss.tools.modeshape.rest.actions.NewServerAction;
 import org.jboss.tools.modeshape.rest.domain.ModeShapeRepository;
 import org.jboss.tools.modeshape.rest.domain.ModeShapeServer;
 import org.jboss.tools.modeshape.rest.domain.ModeShapeWorkspace;
 import org.jboss.tools.modeshape.rest.domain.WorkspaceArea;
 import org.jboss.tools.modeshape.rest.jobs.PublishJob.Type;
 import org.jboss.tools.modeshape.rest.preferences.IgnoredResourcesModel;
 import org.jboss.tools.modeshape.rest.preferences.PublishingFileFilter;
 import org.modeshape.common.util.CheckArg;
 import org.modeshape.common.util.StringUtil;
 import org.modeshape.web.jcr.rest.client.Status;
 import org.modeshape.web.jcr.rest.client.Status.Severity;
 
 /**
  * The <code>PublishPage</code> is a UI for publishing or unpublishing one or more files to a repository.
  */
 public final class PublishPage extends WizardPage implements IServerRegistryListener, ModifyListener {
 
     /**
      * The key in the wizard <code>IDialogSettings</code> for the recurse flag.
      */
     private static final String RECURSE_KEY = "recurse"; //$NON-NLS-1$
 
     /**
      * Indicates if the file filter should be used.
      */
     private static boolean filterFiles = true;
 
     /**
      * @param container the project or folder whose files are being requested
      * @param recurse the flag indicating if child containers should be traversed
      * @param filter the file filter or <code>null</code> if not used
      * @return the list of files contained in the specified container (never <code>null</code>)
      * @throws CoreException if there is a problem finding the files
      */
     private static List<IFile> findFiles( IContainer container,
                                           boolean recurse,
                                           PublishingFileFilter filter ) throws CoreException {
         List<IFile> result = new ArrayList<IFile>();
 
         if (((container instanceof IProject) && !((IProject)container).isOpen())
             || ((filter != null) && !filter.accept(container))) {
             return result;
         }
 
         // process container members
         for (IResource member : container.members()) {
             if (recurse && (member instanceof IContainer)) {
                 // don't select closed projects
                 if ((member instanceof IProject) && !((IProject)member).isOpen()) {
                     continue;
                 }
 
                 result.addAll(findFiles((IContainer)member, recurse, filter));
             } else if ((member instanceof IFile) && ((IFile)member).getLocation().toFile().exists()) {
                 if ((filter == null) || filter.accept(member)) {
                     result.add((IFile)member);
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Processes the specified list of files and for (1) each file found adds it to the result and (2) for each project or folder
      * adds all contained files. For projects and folders processing will be recursive based on saved wizard settings.
      * 
      * @param resources the resources being processed (never <code>null</code>)
      * @param recurse the flag indicating if child containers should be traversed
      * @param filter the file filter or <code>null</code> if not used
      * @return the files being published or unpublished (never <code>null</code> )
      * @throws CoreException if there is a problem processing the resources
      */
     private static List<IFile> processResources( List<IResource> resources,
                                                  boolean recurse,
                                                  PublishingFileFilter filter ) throws CoreException {
         assert (resources != null);
         List<IFile> result = new ArrayList<IFile>();
 
         // Project Map - the outer map. Its keys are IProjects and its values
         // are a Parent Map
         // Parent Map - the inner map. Its keys are IContainers (IProject,
         // IFolder) and its values are a list of files
         Map<IProject, Map<IContainer, List<IFile>>> projectMap = new HashMap<IProject, Map<IContainer, List<IFile>>>();
 
         // Step 1: Process resources
         // - For each file make sure there is a project entry and parent entry
         // then add the file to the Parent Map.
         // - For each folder make sure there is a project entry then add folder
         // entry.
         // - For each project make sure there is a project entry.
         //
         // Step 2: Process maps
         // - In the Project Map, when the recurse flag is set, entries for
         // projects that have a null value (parent map) will be
         // traversed finding all child files and them to results.
         // - In the internal parent map, when the recurse flag is set, entries
         // for parents that have a null value (child
         // collection) will be traversed finding all child files and add them to
         // results.
         //
         // Step 3: Add files from Step 1 to results
 
         // Step 1 (see above for processing description)
         for (IResource resource : resources) {
             IFile file = null;
             IProject project = null;
             List<IFile> files = null;
             Map<IContainer, List<IFile>> parentMap = null;
 
             // see if resource is filtered
             if ((filter != null) && !filter.accept(resource)) {
                 continue;
             }
 
             if (resource instanceof IFile) {
                 IContainer parent = null; // project or folder
                 file = (IFile)resource;
                 parent = file.getParent();
                 project = file.getProject();
 
                 // make sure there is a project entry
                 if (!projectMap.containsKey(project)) {
                     projectMap.put(project, null);
                 }
 
                 parentMap = projectMap.get(project);
 
                 // make sure there is a parent entry
                 if (parentMap == null) {
                     parentMap = new HashMap<IContainer, List<IFile>>();
                     projectMap.put(project, parentMap);
                 }
 
                 files = parentMap.get(parent);
 
                 // make sure there is a files collection
                 if (files == null) {
                     files = new ArrayList<IFile>();
                     parentMap.put(parent, files);
                 }
 
                 // add file
                 files.add(file);
             } else if (resource instanceof IFolder) {
                 IFolder folder = (IFolder)resource;
                 project = folder.getProject();
 
                 // make sure there is a project entry
                 if (!projectMap.containsKey(project)) {
                     projectMap.put(project, null);
                 }
 
                 parentMap = projectMap.get(project);
 
                 // make sure there is a folder entry
                 if (parentMap == null) {
                     parentMap = new HashMap<IContainer, List<IFile>>();
                     projectMap.put(project, parentMap);
                 }
 
                 // add folder only if not already there
                 if (!parentMap.containsKey(folder)) {
                     parentMap.put(folder, null);
                 }
             } else if (resource instanceof IProject) {
                 project = (IProject)resource;
 
                 // if map does not have entry create one
                 if (!projectMap.containsKey(project)) {
                     projectMap.put(project, null);
                 }
             }
         }
 
         // Step 2 (see above for processing description)
         // Process projects that have nothing under them selected
         for (IProject project : projectMap.keySet()) {
             Map<IContainer, List<IFile>> parentMap = projectMap.get(project);
 
             if (parentMap == null) {
                 result.addAll(findFiles(project, recurse, filter));
             } else {
                 // process folders with no folder entries
                 for (IContainer folder : parentMap.keySet()) {
                     List<IFile> files = parentMap.get(folder);
 
                     if (files == null) {
                         result.addAll(findFiles(folder, recurse, filter));
                     }
                 }
             }
         }
 
         // Step 3 (see above for processing description)
         for (IProject project : projectMap.keySet()) {
             Map<IContainer, List<IFile>> parentMap = projectMap.get(project);
 
             if (parentMap != null) {
                 for (Entry<IContainer, List<IFile>> entry : parentMap.entrySet()) {
                     if (entry.getValue() != null) {
                         result.addAll(entry.getValue());
                     }
                 }
             }
         }
 
         return result;
     }
 
     /**
      * The button that allows user to create a new publish area.
      */
     private Button btnNewArea;
 
     /**
      * The repository chooser control.
      */
     private Combo cbxRepository;
 
     /**
      * The server chooser control.
      */
     private Combo cbxServer;
 
     /**
      * The workspace chooser control.
      */
     private Combo cbxWorkspace;
 
     /**
      * The workspace area chooser control.
      */
     private Combo cbxWorkspaceAreas;
 
     /**
      * The control indicating if the user wants to version resources (will be <code>null</code> when unpublishing).
      */
     private Button chkVersioning;
 
     /**
      * The files being published or unpublished (never <code>null</code>).
      */
     private List<IFile> files;
 
     /**
      * The filter used to determine if a file should be included in publishing operations (may be <code>null</code>).
      */
     private PublishingFileFilter filter;
 
     /**
      * A hyperlink to the preference page (will be <code>null</code> when unpublishing).
      */
     private Link linkPrefs;
 
     /**
      * The control containing all the files being published or unpublished.
      */
     private org.eclipse.swt.widgets.List lstResources;
 
     /**
      * Indicates if resources should be found recursively.
      */
     private boolean recurse = true;
 
     /**
      * A collection of repositories for the selected server (never <code>null</code>).
      */
     private List<ModeShapeRepository> repositories;
 
     /**
      * The repository where the workspace is located.
      */
     private ModeShapeRepository repository;
 
     /**
      * <code>true</code> if the selected repository supports versioning
      */
     private boolean repositorySupportsVersioning;
 
     /**
      * The collection of resources selected by the user to be published or unpublished.
      */
     private final List<IResource> resources;
 
     /**
      * The server where the repository is located.
      */
     private ModeShapeServer server;
 
     /**
      * A collection of servers from the server registry (never <code>null</code> ).
      */
     private List<ModeShapeServer> servers;
 
     /**
      * The current validation status.
      */
     private Status status;
 
     /**
      * Indicates if publishing or unpublishing is being done.
      */
     private final Type type;
 
     /**
      * Indicates if versioning of published resources should be done.
      */
     private boolean versioning = true;
 
     /**
      * The workspace where the resources are being published/unpublished (may be <code>null</code>).
      */
     private ModeShapeWorkspace workspace;
 
     /**
      * The path segment prepended to the resource project path.
      */
     private String workspaceArea;
 
     /**
      * A collection of workspaces for the selected server repository (never <code>null</code>).
      */
     private List<ModeShapeWorkspace> workspaces;
 
     /**
      * @param type indicates if publishing or unpublishing is being done
      * @param resources the resources being published or unpublished (never <code>null</code>)
      * @throws CoreException if there is a problem processing the input resources
      */
     public PublishPage( Type type,
                         List<IResource> resources ) throws CoreException {
         super(PublishPage.class.getSimpleName());
         CheckArg.isNotNull(resources, "resources"); //$NON-NLS-1$
         setTitle((type == Type.PUBLISH) ? RestClientI18n.publishPagePublishTitle : RestClientI18n.publishPageUnpublishTitle);
         setPageComplete(false);
 
         this.type = type;
         this.resources = resources;
 
         // load filter with current preference value
         IgnoredResourcesModel model = new IgnoredResourcesModel();
         model.load(Activator.getDefault().getPreferenceStore().getString(IGNORED_RESOURCES_PREFERENCE));
         this.filter = (filterFiles ? new PublishingFileFilter(model) : null);
     }
 
     private void constructLocationPanel( Composite parent ) {
         Group pnl = new Group(parent, SWT.NONE);
         pnl.setText(RestClientI18n.publishPageLocationGroupTitle);
         pnl.setLayout(new GridLayout(2, false));
         pnl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 
         // row 1: label combobox button
         // row 2: label combobox
         // row 3: label combobox
         // row 4: label combobox
 
         { // row 1: server row
             Composite pnlServer = new Composite(pnl, SWT.NONE);
             GridLayout layout = new GridLayout(3, false);
             layout.marginHeight = 0;
             layout.marginWidth = 0;
             pnlServer.setLayout(layout);
             GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
             gd.horizontalSpan = 2;
             pnlServer.setLayoutData(gd);
 
             Label lblServer = new Label(pnlServer, SWT.LEFT);
             lblServer.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
             lblServer.setText(RestClientI18n.publishPageServerLabel);
             lblServer.setToolTipText(RestClientI18n.publishPageServerToolTip);
 
             this.cbxServer = new Combo(pnlServer, SWT.DROP_DOWN | SWT.READ_ONLY);
             this.cbxServer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
             this.cbxServer.setToolTipText(RestClientI18n.publishPageServerToolTip);
 
             final IAction action = new NewServerAction(this.getShell(), getServerManager());
             final Button btnNewServer = new Button(pnlServer, SWT.PUSH);
             btnNewServer.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
             btnNewServer.setText(RestClientI18n.publishPageNewServerButton);
             btnNewServer.setToolTipText(action.getToolTipText());
             btnNewServer.addSelectionListener(new SelectionAdapter() {
                 /**
                  * {@inheritDoc}
                  * 
                  * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                  */
                 @Override
                 public void widgetSelected( SelectionEvent e ) {
                     action.run();
                 }
             });
 
             // update page message first time selected to get rid of initial message by forcing validation
             btnNewServer.addSelectionListener(new SelectionAdapter() {
                 /**
                  * {@inheritDoc}
                  * 
                  * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                  */
                 @Override
                 public void widgetSelected( SelectionEvent e ) {
                     updateInitialMessage();
                     btnNewServer.removeSelectionListener(this);
                 }
             });
         }
 
         { // row 2: repository row
             Label lblRepository = new Label(pnl, SWT.LEFT);
             lblRepository.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
             lblRepository.setText(RestClientI18n.publishPageRepositoryLabel);
             lblRepository.setToolTipText(RestClientI18n.publishPageRepositoryToolTip);
 
             this.cbxRepository = new Combo(pnl, SWT.DROP_DOWN | SWT.READ_ONLY);
             this.cbxRepository.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
             this.cbxRepository.setToolTipText(RestClientI18n.publishPageRepositoryToolTip);
         }
 
         { // row 3: workspace row
             Label lblWorkspace = new Label(pnl, SWT.LEFT);
             lblWorkspace.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
             lblWorkspace.setText(RestClientI18n.publishPageWorkspaceLabel);
 
             this.cbxWorkspace = new Combo(pnl, SWT.DROP_DOWN | SWT.READ_ONLY);
             this.cbxWorkspace.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 
             if (type == Type.PUBLISH) {
                 this.cbxWorkspace.setToolTipText(RestClientI18n.publishPageWorkspacePublishToolTip);
             } else {
                 this.cbxWorkspace.setToolTipText(RestClientI18n.publishPageWorkspaceUnpublishToolTip);
             }
 
             this.cbxWorkspace.setToolTipText(this.cbxWorkspace.getToolTipText());
         }
 
         { // row 4: workspace area
             Composite pnlArea = new Composite(parent, SWT.NONE);
             GridLayout layout = new GridLayout(3, false);
             layout.marginHeight = 0;
             layout.marginWidth = 0;
             pnlArea.setLayout(layout);
             pnlArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
             ((GridData)pnl.getLayoutData()).horizontalSpan = 2;
 
             Label lblWorkspaceArea = new Label(pnlArea, SWT.LEFT);
             lblWorkspaceArea.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
             lblWorkspaceArea.setText(RestClientI18n.publishPageWorkspaceAreaLabel);
             lblWorkspaceArea.setToolTipText(RestClientI18n.publishPageWorkspaceAreaToolTip);
 
             this.cbxWorkspaceAreas = new Combo(pnlArea, SWT.DROP_DOWN | SWT.READ_ONLY);
             this.cbxWorkspaceAreas.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
             this.cbxWorkspaceAreas.setToolTipText(RestClientI18n.publishPageWorkspaceAreaToolTip);
             this.cbxWorkspaceAreas.setEnabled(false);
 
             this.btnNewArea = new Button(pnlArea, SWT.PUSH);
             this.btnNewArea.setImage(Activator.getDefault().getSharedImage(ISharedImages.IMG_OBJ_ADD));
             this.btnNewArea.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
             this.btnNewArea.setToolTipText(RestClientI18n.addPublishAreaActionToolTip);
             this.btnNewArea.setEnabled(false);
             this.btnNewArea.addSelectionListener(new SelectionAdapter() {
 
                 /**
                  * {@inheritDoc}
                  * 
                  * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                  */
                 @Override
                 public void widgetSelected( SelectionEvent e ) {
                     handleNewWorkspaceArea();
                 }
             });
         }
     }
 
     private void constructResourcesPanel( Composite parent ) {
         Composite pnl = new Composite(parent, SWT.NONE);
         pnl.setLayout(new GridLayout());
         pnl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
         // pnl layout:
         // row 1: lbl
         // row 2: lstResources
         // row 3: recurse chkbox
         // row 4: versioning chkbox and link (only when publishing)
 
         { // row 1
             Label lbl = new Label(pnl, SWT.LEFT);
             lbl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
 
             if (type == Type.PUBLISH) {
                 lbl.setText(RestClientI18n.publishPagePublishResourcesLabel);
             } else {
                 lbl.setText(RestClientI18n.publishPageUnpublishResourcesLabel);
             }
         }
 
         { // row 2
             this.lstResources = new org.eclipse.swt.widgets.List(pnl, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
             GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
             gd.horizontalSpan = 2;
             gd.minimumHeight = this.lstResources.getItemHeight() * 2; // set min height
             gd.heightHint = this.lstResources.getItemHeight() * 10; // set preferred height
             this.lstResources.setLayoutData(gd);
             final org.eclipse.swt.widgets.List finalLst = this.lstResources;
 
             // update page message first time selected to get rid of initial message by forcing validation
             this.lstResources.addSelectionListener(new SelectionAdapter() {
                 /**
                  * {@inheritDoc}
                  * 
                  * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                  */
                 @Override
                 public void widgetSelected( SelectionEvent e ) {
                     // do the very first time to get rid of initial message then
                     // remove listener
                     updateInitialMessage();
                     finalLst.removeSelectionListener(this);
                 }
             });
 
             // load list with initial files
             loadFiles();
         }
 
         { // row 3 recurse chkbox
             Button chkRecurse = new Button(pnl, SWT.CHECK);
             chkRecurse.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
             chkRecurse.setText(RestClientI18n.publishPageRecurseCheckBox);
             chkRecurse.setToolTipText(RestClientI18n.publishPageRecurseCheckBoxToolTip);
 
             // set the recurse flag based on dialog settings
             if (getDialogSettings().get(RECURSE_KEY) != null) {
                 this.recurse = getDialogSettings().getBoolean(RECURSE_KEY);
             }
 
             chkRecurse.setSelection(this.recurse);
             chkRecurse.addSelectionListener(new SelectionAdapter() {
                 /**
                  * {@inheritDoc}
                  * 
                  * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                  */
                 @Override
                 public void widgetSelected( SelectionEvent e ) {
                     handleRecurseChanged(((Button)e.widget).getSelection());
                 }
             });
 
             // update page message first time selected to get rid of initial message by forcing validation
             chkRecurse.addSelectionListener(new SelectionAdapter() {
                 /**
                  * {@inheritDoc}
                  * 
                  * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                  */
                 @Override
                 public void widgetSelected( SelectionEvent e ) {
                     updateInitialMessage();
                     ((Button)e.widget).removeSelectionListener(this);
                 }
             });
         }
 
         if (this.type == Type.PUBLISH) {
             // row 4 versioning chkbox and link to open preference page
             Composite pnlVersioning = new Composite(pnl, SWT.NONE);
             pnlVersioning.setLayout(new GridLayout(2, false));
             ((GridLayout)pnlVersioning.getLayout()).marginWidth = 0;
             pnlVersioning.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
             ((GridData)pnlVersioning.getLayoutData()).minimumHeight = 30;
 
             this.chkVersioning = new Button(pnlVersioning, SWT.CHECK);
             this.chkVersioning.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
             this.chkVersioning.setText(RestClientI18n.publishPageVersionCheckBox);
             this.chkVersioning.setToolTipText(RestClientI18n.publishPageVersionCheckBoxToolTip);
 
             // set the version flag based on preference
             this.versioning = Activator.getDefault().getPreferenceStore().getBoolean(ENABLE_RESOURCE_VERSIONING);
 
             this.chkVersioning.setSelection(this.versioning);
             this.chkVersioning.setEnabled(false);
             this.chkVersioning.addSelectionListener(new SelectionAdapter() {
                 /**
                  * {@inheritDoc}
                  * 
                  * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                  */
                 @Override
                 public void widgetSelected( SelectionEvent e ) {
                     handleVersioningChanged(((Button)e.widget).getSelection());
                 }
             });
 
             this.linkPrefs = new Link(pnlVersioning, SWT.WRAP);
             this.linkPrefs.setText(RestClientI18n.publishPageOpenPreferencePageLink);
             this.linkPrefs.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
             this.linkPrefs.setEnabled(false);
             this.linkPrefs.addSelectionListener(new SelectionAdapter() {
                 /**
                  * {@inheritDoc}
                  * 
                  * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                  */
                 @Override
                 public void widgetSelected( SelectionEvent e ) {
                     handleOpenPreferencePage();
                 }
             });
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
      */
     @Override
     public void createControl( Composite parent ) {
         Composite pnlMain = new Composite(parent, SWT.NONE);
         pnlMain.setLayout(new GridLayout());
         pnlMain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
         constructLocationPanel(pnlMain);
         constructResourcesPanel(pnlMain);
         setControl(pnlMain);
 
         // add combobox listeners
         this.cbxRepository.addModifyListener(this);
         this.cbxServer.addModifyListener(this);
         this.cbxWorkspace.addModifyListener(this);
         this.cbxWorkspaceAreas.addModifyListener(this);
 
         // register with the help system
         IWorkbenchHelpSystem helpSystem = Activator.getDefault().getWorkbench().getHelpSystem();
         helpSystem.setHelp(pnlMain, PUBLISH_DIALOG_HELP_CONTEXT);
 
         // register to receive server registry events (this will populate the UI)
         getServerManager().addRegistryListener(this);
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.eclipse.jface.dialogs.DialogPage#dispose()
      */
     @Override
     public void dispose() {
         getServerManager().removeRegistryListener(this);
         super.dispose();
     }
 
     /**
      * @return the files to publish or unpublish (never <code>null</code>)
      */
     List<IFile> getFiles() {
         return this.files;
     }
 
     /**
      * @return the server manager obtained from the wizard
      */
     private ServerManager getServerManager() {
         return ((PublishWizard)getWizard()).getServerManager();
     }
 
     /**
      * @return the workspace to use when publishing or unpublishing (page must be complete)
      */
     ModeShapeWorkspace getWorkspace() {
         assert isPageComplete();
         return this.workspace;
     }
 
     /**
      * @return the path segment prepended to the resource project path (never <code>null</code> but can be empty)
      */
     String getWorkspaceArea() {
         return this.workspaceArea;
     }
 
     /**
      * Opens dialog that will create a new workspace area.
      */
     void handleNewWorkspaceArea() {
         final AddPublishAreaAction action = new AddPublishAreaAction(getShell(), getServerManager(), this.workspace,
                                                                      Utils.getServerViewer());
         action.run();
 
         if (action.success()) {
             String newPath = null;
             final String[] before = this.cbxWorkspaceAreas.getItems();
             final WorkspaceArea[] workspaceAreas = workspaceAreas();
             final List<String> items = new ArrayList<String>();
 
             // add in those workspace areas identified by server
             if ((workspaceAreas != null) && (workspaceAreas.length != 0)) {
                 for (final WorkspaceArea area : workspaceAreas) {
                     boolean found = false;
                     final String path = area.getPath();
                     items.add(path);
 
                     // keep track of the new path added
                     if (StringUtil.isBlank(newPath)) {
                         for (final String beforeArea : before) {
                             if (beforeArea.equals(path)) {
                                 found = true;
                                 break;
                             }
                         }
 
                         if (!found) {
                             newPath = path;
                         }
                     }
                 }
 
                 Collections.sort(items);
             }
 
             // populate combo
             this.cbxWorkspaceAreas.setItems(items.toArray(new String[items.size()]));
 
             // should always have a new path unless server went down
             if (!StringUtil.isBlank(newPath)) {
                 this.cbxWorkspaceAreas.setText(newPath);
             }
         }
     }
 
     /**
      * Opens the preference page.
      */
     void handleOpenPreferencePage() {
         // open preference page and only allow the pref page where the version setting is
         PreferencesUtil.createPreferenceDialogOn(getShell(),
                                                  PUBLISHING_PREFERENCE_PAGE_ID,
                                                  new String[] {PUBLISHING_PREFERENCE_PAGE_ID},
                                                  null).open();
     }
 
     /**
      * Saves the recurse setting and reloads the files to be published or unpublished.
      * 
      * @param selected the flag indicating the new recurse setting
      */
     void handleRecurseChanged( boolean selected ) {
         this.recurse = selected;
 
         try {
             this.files = processResources(this.resources, isRecursing(), filter);
             loadFiles();
         } catch (CoreException e) {
             Activator.getDefault().log(new Status(Severity.ERROR, RestClientI18n.publishPageRecurseProcessingErrorMsg, e));
 
             if (getControl().isVisible()) {
                 MessageDialog.openError(getShell(),
                                         RestClientI18n.errorDialogTitle,
                                         RestClientI18n.publishPageRecurseProcessingErrorMsg);
             }
         }
     }
 
     /**
      * Handler for when the repository control value is modified
      */
     void handleRepositoryModified() {
         int index = this.cbxRepository.getSelectionIndex();
 
         // make sure there is a selection
         if (index != -1) {
             // since repositories are sorted need to find right one
             final String repoName = this.cbxRepository.getText();
 
             for (ModeShapeRepository repo : this.repositories) {
                 if (repoName.equals(repo.getName())) {
                     this.repository = repo;
                     break;
                 }
             }
         }
 
         // repository capabilities could affect the UI
         updateRepositoryCapabilities();
 
         // clear loaded workspaces
         refreshWorkspaces();
 
         // update page state
         updateState();
     }
 
     /**
      * Handler for when the server control value is modified
      */
     void handleServerModified() {
         int index = this.cbxServer.getSelectionIndex();
 
         // make sure there is a selection
         if (index != -1) {
             this.server = this.servers.get(index);
         }
 
         // need to reload repositories since server changed
         refreshRepositories();
 
         // update page state
         updateState();
     }
 
     /**
      * Saves the versioning setting.
      * 
      * @param selected the flag indicating the new versioning setting
      */
     void handleVersioningChanged( boolean selected ) {
         this.versioning = selected;
     }
 
     /**
      * Handler for when the workspace area value is modified.
      */
     void handleWorkspaceAreaModified() {
         int index = this.cbxWorkspaceAreas.getSelectionIndex();
 
         if (index == -1) {
             this.workspaceArea = this.cbxWorkspaceAreas.getText();
         } else {
             this.workspaceArea = this.cbxWorkspaceAreas.getItems()[index];
         }
     }
 
     WorkspaceArea[] workspaceAreas() {
         try {
             final WorkspaceArea[] workspaceAreas = getServerManager().getWorkspaceAreas(this.workspace);
             return workspaceAreas;
         } catch (Exception e) {
             Activator.getDefault().log(new Status(
                                                   Severity.ERROR,
                                                   NLS.bind(RestClientI18n.publishPageUnableToObtainWorkspaceAreas, this.workspace),
                                                   e));
         }
 
         return new WorkspaceArea[0];
     }
 
     /**
      * Handler for when the workspace control value is modified.
      */
     void handleWorkspaceModified() {
         int index = this.cbxWorkspace.getSelectionIndex();
 
         // make sure there is a selection
         if (index != -1) {
             final String workspaceName = this.cbxWorkspace.getText();
  
             // since workspaces are sorted need to find the right one
             for (ModeShapeWorkspace testWorkspace : this.workspaces) {
                 if (workspaceName.equals(testWorkspace.getName())) {
                     this.workspace = testWorkspace;
                     break;
                 }
             }
 
             // update workspace areas from server
             final WorkspaceArea[] workspaceAreas = workspaceAreas();
             final List<String> items = new ArrayList<String>();
 
             // add in those workspace areas identified by server
             if ((workspaceAreas != null) && (workspaceAreas.length != 0)) {
                 for (final WorkspaceArea area : workspaceAreas) {
                     final String path = area.getPath();
                     items.add(path);
                 }
 
                 Collections.sort(items);
             }
 
             // populate combo
             this.cbxWorkspaceAreas.setItems(items.toArray(new String[items.size()]));
 
             if (!this.cbxWorkspaceAreas.isEnabled()) {
                 this.cbxWorkspaceAreas.setEnabled(true);
                 this.btnNewArea.setEnabled(true);
             }
         } else {
             this.workspaceArea = null;
             this.cbxWorkspaceAreas.removeAll();
 
             if (this.cbxWorkspaceAreas.isEnabled()) {
                 this.cbxWorkspaceAreas.setEnabled(false);
                 this.btnNewArea.setEnabled(false);
             }
         }
 
         if (this.cbxWorkspaceAreas.getItemCount() == 0) {
             this.workspaceArea = null;
         } else {
             this.cbxWorkspaceAreas.select(0);
         }
 
         updateState();
     }
 
     /**
      * @return <code>true</code> if resources found recursively under projects and folders should also be published or unpublished
      */
     boolean isRecursing() {
         return this.recurse;
     }
 
     /**
      * @return <code>true</code> if versioning of published resources should be done
      */
     boolean isVersioning() {
         return (this.repositorySupportsVersioning && this.versioning);
     }
 
     /**
      * Populates the list of files to be published based on the recurse flag and the list of workspace selected resources.
      * Pre-condition is that {@link #processResources(List, boolean, PublishingFileFilter)} has been called.
      */
     private void loadFiles() {
         this.lstResources.removeAll();
 
         for (IResource resource : this.files) {
             this.lstResources.add(resource.getFullPath().toString());
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
      */
     @Override
     public void modifyText( ModifyEvent e ) {
         if (e.widget == this.cbxServer) {
             handleServerModified();
         } else if (e.widget == this.cbxRepository) {
             handleRepositoryModified();
         } else if (e.widget == this.cbxWorkspace) {
             handleWorkspaceModified();
         } else if (e.widget == this.cbxWorkspaceAreas) {
             handleWorkspaceAreaModified();
         } else {
             assert false; // should not happen
         }
     }
 
     /**
      * Refreshes the repository-related fields and controls based on the server registry. This in turn causes the workspaces to
      * also to be refreshed.
      */
     private void refreshRepositories() {
         this.repository = null;
 
         if (this.server == null) {
             this.repositories = Collections.emptyList();
         } else {
             try {
                 this.repositories = new ArrayList<ModeShapeRepository>(getServerManager().getRepositories(this.server));
             } catch (Exception e) {
                 this.repositories = Collections.emptyList();
                 String msg = NLS.bind(RestClientI18n.serverManagerGetRepositoriesExceptionMsg, this.server.getShortDescription());
                 Activator.getDefault().log(new Status(Severity.ERROR, msg, e));
             }
         }
 
         // clear items
         this.cbxRepository.removeAll();
 
         // reload
         if (this.repositories.isEmpty()) {
             // disable control if necessary
             if (this.cbxRepository.getEnabled()) {
                 this.cbxRepository.setEnabled(false);
             }
         } else if (this.repositories.size() == 1) {
             this.repository = this.repositories.get(0);
             this.cbxRepository.add(this.repository.getName());
             this.cbxRepository.select(0);
 
             // enable control if necessary
             if (!this.cbxRepository.getEnabled()) {
                 this.cbxRepository.setEnabled(true);
             }
         } else {
             // add an item for each repository
             final List<String> repoNames = new ArrayList<String>(this.repositories.size());
 
             for (ModeShapeRepository repository : this.repositories) {
                 repoNames.add(repository.getName());
             }
 
             Collections.sort(repoNames);
             this.cbxRepository.setItems(repoNames.toArray(new String[repoNames.size()]));
 
             // enable control if necessary
             if (!this.cbxRepository.getEnabled()) {
                 this.cbxRepository.setEnabled(true);
             }
         }
 
         // repository capabilities could affect the UI
         updateRepositoryCapabilities();
 
         // must reload workspaces
         refreshWorkspaces();
     }
 
     /**
      * Refreshes the server-related fields and controls based on the server registry. This in turn causes the repositories and
      * workspaces to also to be refreshed.
      */
     void refreshServers() {
         this.server = null;
         this.servers = new ArrayList<ModeShapeServer>(getServerManager().getServers());
 
         // clear server combo
         this.cbxServer.removeAll();
 
         if (this.servers.size() == 0) {
             // disable control if necessary
             if (this.cbxServer.getEnabled()) {
                 this.cbxServer.setEnabled(false);
             }
         } else if (this.servers.size() == 1) {
             this.server = this.servers.get(0);
             this.cbxServer.add(this.server.getName());
             this.cbxServer.select(0);
 
             // enable control if necessary
             if (!this.cbxServer.getEnabled()) {
                 this.cbxServer.setEnabled(true);
             }
         } else {
             // add an item for each server
             for (ModeShapeServer server : this.servers) {
                 this.cbxServer.add(server.getName());
             }
 
             // enable control if necessary
             if (!this.cbxServer.getEnabled()) {
                 this.cbxServer.setEnabled(true);
             }
         }
 
         // must reload repositories
         refreshRepositories();
     }
 
     /**
      * Refreshes the workspace-related fields and controls based on the server registry.
      */
     private void refreshWorkspaces() {
         this.workspace = null;
 
         if (this.repository == null) {
             this.workspaces = Collections.emptyList();
         } else {
             try {
                 this.workspaces = new ArrayList<ModeShapeWorkspace>(getServerManager().getWorkspaces(this.repository));
             } catch (Exception e) {
                 this.workspaces = Collections.emptyList();
                 String msg = NLS.bind(RestClientI18n.serverManagerGetWorkspacesExceptionMsg, this.repository);
                 Activator.getDefault().log(new Status(Severity.ERROR, msg, e));
             }
         }
 
         // clear items
         this.cbxWorkspace.removeAll();
         this.cbxWorkspaceAreas.removeAll();
 
         // reload
         if (this.workspaces.isEmpty()) {
             // disable controls if necessary
             if (this.cbxWorkspace.getEnabled()) {
                 this.cbxWorkspace.setEnabled(false);
             }
 
             if (this.cbxWorkspaceAreas.isEnabled()) {
                 this.cbxWorkspaceAreas.setEnabled(false);
                 this.btnNewArea.setEnabled(false);
             }
         } else if (this.workspaces.size() == 1) {
             ModeShapeWorkspace temp = this.workspaces.get(0);
             this.cbxWorkspace.add(temp.getName());
             this.cbxWorkspace.select(0);
 
             // enable controls if necessary
             if (!this.cbxWorkspace.getEnabled()) {
                 this.cbxWorkspace.setEnabled(true);
             }
 
            final boolean enable = (this.cbxWorkspaceAreas.getItemCount() != 0);

            if (this.cbxWorkspaceAreas.getEnabled() != enable) {
                this.cbxWorkspaceAreas.setEnabled(enable);
                this.btnNewArea.setEnabled(enable);
             }
         } else {
             // add an item for each workspace
             final List<String> workspaceNames = new ArrayList<String>(this.workspaces.size());
 
             for (ModeShapeWorkspace workspace : this.workspaces) {
                 workspaceNames.add(workspace.getName());
             }
 
             Collections.sort(workspaceNames);
             this.cbxWorkspace.setItems(workspaceNames.toArray(new String[workspaceNames.size()]));
 
             // enable controls if necessary
             if (!this.cbxWorkspace.getEnabled()) {
                 this.cbxWorkspace.setEnabled(true);
             }
 
             if (this.cbxWorkspaceAreas.isEnabled()) {
                 this.cbxWorkspaceAreas.setEnabled(false);
                 this.btnNewArea.setEnabled(false);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.jboss.tools.modeshape.rest.IServerRegistryListener#serverRegistryChanged(org.jboss.tools.modeshape.rest.ServerRegistryEvent)
      */
     @Override
     public Exception[] serverRegistryChanged( ServerRegistryEvent event ) {
         // should only be a new server event
         if (event.isNew()) {
             refreshServers();
             updateState();
         }
 
         return null;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
      */
     @Override
     public void setVisible( boolean visible ) {
         super.setVisible(visible);
 
         if (visible) {
             // set initial status
             validate();
 
             // update OK/Finish button enablement
             setPageComplete(!this.status.isError());
 
             // set initial message
             if (this.status.isOk()) {
                 String msg = ((this.type == Type.PUBLISH) ? RestClientI18n.publishPagePublishOkStatusMsg : RestClientI18n.publishPageUnpublishOkStatusMsg);
                 setMessage(msg, IMessageProvider.NONE);
             } else {
                 setMessage(this.status.getMessage(), IMessageProvider.ERROR);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.eclipse.jface.wizard.WizardPage#setWizard(org.eclipse.jface.wizard.IWizard)
      */
     @Override
     public void setWizard( IWizard newWizard ) {
         super.setWizard(newWizard);
 
         try {
             this.files = processResources(this.resources, isRecursing(), this.filter);
         } catch (CoreException e) {
             Activator.getDefault().log(new Status(Severity.ERROR, RestClientI18n.publishPageRecurseProcessingErrorMsg, e));
         }
     }
 
     /**
      * Updates the initial page message.
      */
     void updateInitialMessage() {
         String msg = ((this.type == Type.PUBLISH) ? RestClientI18n.publishPagePublishOkStatusMsg : RestClientI18n.publishPageUnpublishOkStatusMsg);
 
         if (msg.equals(getMessage())) {
             updateState();
         }
     }
 
     /**
      * Some capabilities (like versioning) will not be supported by all repositories. This could affect the UI.
      */
     private void updateRepositoryCapabilities() {
         // versioning
         this.repositorySupportsVersioning = true;
 
         if (this.repository == null) {
             this.repositorySupportsVersioning = false;
         } else {
             Object supportsVersioning = this.repository.getProperties().get(javax.jcr.Repository.OPTION_VERSIONING_SUPPORTED);
 
             if (supportsVersioning == null) {
                 this.repositorySupportsVersioning = false;
             } else {
                 this.repositorySupportsVersioning = Boolean.parseBoolean(supportsVersioning.toString());
             }
         }
 
         // update enabled state of versioning controls
         if ((this.chkVersioning != null) && (this.chkVersioning.getEnabled() != this.repositorySupportsVersioning)) {
             this.chkVersioning.setEnabled(this.repositorySupportsVersioning);
             this.linkPrefs.setEnabled(this.repositorySupportsVersioning);
         }
     }
 
     /**
      * Updates message, message icon, and OK button enablement based on validation results
      */
     void updateState() {
         // get the current state
         validate();
 
         // update OK/Finish button enablement
         setPageComplete(!this.status.isError());
 
         // update page message
         if (this.status.isError()) {
             setMessage(this.status.getMessage(), IMessageProvider.ERROR);
         } else {
             if (this.status.isWarning()) {
                 setMessage(this.status.getMessage(), IMessageProvider.WARNING);
             } else if (this.status.isInfo()) {
                 setMessage(this.status.getMessage(), IMessageProvider.INFORMATION);
             } else {
                 setMessage(this.status.getMessage(), IMessageProvider.NONE);
             }
         }
     }
 
     /**
      * Validates all inputs and sets the validation status.
      */
     private void validate() {
         String msg = null;
         Severity severity = Severity.ERROR;
 
         if ((this.resources == null) || this.resources.isEmpty() || this.files.isEmpty()) {
             msg = ((type == Type.PUBLISH) ? RestClientI18n.publishPageNoResourcesToPublishStatusMsg : RestClientI18n.publishPageNoResourcesToUnpublishStatusMsg);
         } else if (this.server == null) {
             int count = this.cbxServer.getItemCount();
             msg = ((count == 0) ? RestClientI18n.publishPageNoAvailableServersStatusMsg : RestClientI18n.publishPageMissingServerStatusMsg);
         } else if (this.repository == null) {
             int count = this.cbxRepository.getItemCount();
             msg = ((count == 0) ? RestClientI18n.publishPageNoAvailableRepositoriesStatusMsg : RestClientI18n.publishPageMissingRepositoryStatusMsg);
         } else if (this.workspace == null) {
             int count = this.cbxWorkspace.getItemCount();
             msg = ((count == 0) ? RestClientI18n.publishPageNoAvailableWorkspacesStatusMsg : RestClientI18n.publishPageMissingWorkspaceStatusMsg);
         } else if (this.workspaceArea == null) {
             msg = RestClientI18n.publishPageMissingPublishAreaStatusMsg;
         } else {
             severity = Severity.OK;
             msg = ((type == Type.PUBLISH) ? RestClientI18n.publishPagePublishOkStatusMsg : RestClientI18n.publishPageUnpublishOkStatusMsg);
         }
 
         this.status = new Status(severity, msg, null);
     }
 
     /**
      * Processing done after wizard is finished. Wizard was not canceled.
      */
     void wizardFinished() {
         // update dialog settings
         getDialogSettings().put(RECURSE_KEY, this.recurse);
     }
 
 }
