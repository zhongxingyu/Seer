 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.connect.studio;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 import org.nuxeo.ide.common.BundleImageProvider;
 import org.nuxeo.ide.common.UI;
 import org.nuxeo.ide.connect.ConnectPlugin;
 import org.nuxeo.ide.connect.Connector;
 import org.nuxeo.ide.connect.IStudioListener;
 import org.nuxeo.ide.connect.StudioProvider;
 import org.nuxeo.ide.connect.studio.tree.FeatureNode;
 import org.nuxeo.ide.connect.studio.tree.Node;
 import org.nuxeo.ide.connect.studio.tree.StudioProjectProvider;
 import org.nuxeo.ide.connect.studio.tree.TypeNode;
 import org.nuxeo.ide.connect.ui.MultiExportOperationsWizard;
 import org.nuxeo.ide.sdk.SDKPlugin;
 
 /**
  * You must call create() method in order to initialize the panel.
  * 
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class StudioPanel extends Composite implements IStudioListener {
 
     protected TreeViewer tv;
 
     protected ScrolledForm form;
 
     protected FormToolkit toolkit;
 
     protected BundleImageProvider imgProvider;
 
     public StudioPanel(Composite parent) {
         super(parent, SWT.NONE);
         setLayout(new FillLayout());
         createContent();
         try {
             ConnectPlugin.getStudioProvider().addStudioListener(this);
         } catch (Exception e) {
             UI.showError("Failed to register studio update listener", e);
         }
     }
 
     public TreeViewer getTreeViewer() {
         return tv;
     }
 
     public void setInput(StudioProvider provider) {
         getTreeViewer().setInput(provider);
         form.setText(getFormTitle());
     }
 
     public void setInput(StudioProject project) {
         form.setText(getFormTitle());
         getTreeViewer().setInput(project);
     }
 
     public void dispose() {
         super.dispose();
         try {
             ConnectPlugin.getStudioProvider().removeStudioListener(this);
         } catch (Exception e) {
             // do nothing
         }
         tv = null;
         form = null;
         if (toolkit != null) {
             toolkit.dispose();
             toolkit = null;
         }
         if (imgProvider != null) {
             imgProvider.dispose();
             imgProvider = null;
         }
     }
 
     protected String getFormTitle() {
         return "Studio Projects";
     }
 
     protected void createContent() {
         imgProvider = new BundleImageProvider(
                 ConnectPlugin.getDefault().getBundle());
         toolkit = new FormToolkit(getShell().getDisplay());
 
         form = toolkit.createScrolledForm(this);
         form.getBody().setLayout(new GridLayout());
         form.setImage(imgProvider.getImage("icons/studio_project.gif"));
         createToolbar(form);
 
         Section section = toolkit.createSection(form.getBody(),
                 Section.EXPANDED | Section.TITLE_BAR | Section.DESCRIPTION);
         section.setText("Project Features");
         section.setDescription("You can browse features available in the Nuxeo Studio project. Double click a feature entry to open it in the browser.");
 
         GridData gd = new GridData();
         gd.horizontalAlignment = SWT.FILL;
         gd.grabExcessHorizontalSpace = true;
         gd.verticalAlignment = SWT.FILL;
         gd.grabExcessVerticalSpace = true;
         section.setLayoutData(gd);
 
         Tree tree = toolkit.createTree(section, SWT.H_SCROLL | SWT.V_SCROLL
                 | SWT.BORDER);
 
         section.setClient(tree);
 
         tv = new TreeViewer(tree);
 
         tv.addDoubleClickListener(new IDoubleClickListener() {
             @Override
             public void doubleClick(DoubleClickEvent event) {
                 ISelection sel = event.getSelection();
                 if (sel.isEmpty()) {
                     return;
                 }
                 Object obj = ((IStructuredSelection) sel).getFirstElement();
                 if (obj instanceof Node) {
                     Node<?> node = (Node<?>) obj;
                     if (node.isTerminal()) {
                         if (node instanceof FeatureNode) {
                             openFeature(node);
                         } else if (node instanceof TypeNode) {
                             openGlobalFeature(node);
                         }
                     } else {
                         if (tv.getExpandedState(obj)) {
                             tv.collapseToLevel(obj, 1);
                         } else {
                             tv.expandToLevel(obj, 1);
                         }
                     }
                 } else {
                     if (tv.getExpandedState(obj)) {
                         tv.collapseToLevel(obj, 1);
                     } else {
                         tv.expandToLevel(obj, 1);
                     }
                 }
             }
         });
 
         StudioProjectProvider provider = new StudioProjectProvider(imgProvider);
         tv.setContentProvider(provider);
         tv.setLabelProvider(provider);
         tv.setSorter(new ViewerSorter());
     }
 
     protected void openFeature(Node<?> node) {
         StudioFeature feature = ((FeatureNode) node).getData();
         String url = node.getProject().getUrl() + "#@feature:"
                 + feature.getId() + '.' + feature.getType();
         Program.launch(url);
     }
 
     protected void openGlobalFeature(Node<?> node) {
         String type = ((TypeNode) node).getId();
         String url = node.getProject().getUrl() + "#@feature:global." + type;
         Program.launch(url);
     }
 
     protected IAction createRefreshAction() {
         Action action = new Action() {
             public void run() {
                 final Job job = new Job("Refresh Studio Projects") {
                     @Override
                     protected IStatus run(IProgressMonitor monitor) {
                         monitor.beginTask("Refresh Studio Projects", 10);
                         try {
                             StudioProvider provider = ConnectPlugin.getStudioProvider();
                             monitor.worked(1);
                             provider.updateProjects(Connector.getDefault().getProjects());
                             monitor.worked(9);
                             return Status.OK_STATUS;
                         } catch (Exception e) {
                             return new Status(IStatus.ERROR,
                                     SDKPlugin.PLUGIN_ID,
                                     "Failed to refresh studio project", e);
                         } finally {
                             monitor.done();
                         }
                     }
                 };
                 job.setUser(true);
                 job.schedule();
             }
         };
         action.setId("refresh");
         action.setText("Refresh");
         action.setImageDescriptor(imgProvider.getImageDescriptor("icons/refresh.gif"));
         return action;
     }
 
     protected IAction createExportOperationsAction() {
         Action action = new Action() {
             public void run() {
                 MultiExportOperationsWizard wizard = new MultiExportOperationsWizard();
                 wizard.init(PlatformUI.getWorkbench(),
                         StructuredSelection.EMPTY);
                 WizardDialog dialog = new WizardDialog(getShell(), wizard);
                 dialog.create();
                 dialog.open();
             }
         };
         action.setId("export");
         action.setText("Export Operations");
         action.setImageDescriptor(imgProvider.getImageDescriptor("icons/export.gif"));
         return action;
     }
 
     protected void createToolbar(ScrolledForm form) {
         form.getToolBarManager().add(createRefreshAction());
         form.getToolBarManager().add(createExportOperationsAction());
         form.getToolBarManager().update(true);
     }
 
     @Override
     public void handleProjectsUpdate(final StudioProvider provider) {
         Display d = Display.getCurrent();
         if (d == null) {
             Display.getDefault().asyncExec(new Runnable() {
                 @Override
                 public void run() {
                     doRefresh(provider);
                 }
             });
         } else {
             doRefresh(provider);
         }
     }
 
     protected void doRefresh(StudioProvider provider) {
         setInput(provider);
     }
 
 }
