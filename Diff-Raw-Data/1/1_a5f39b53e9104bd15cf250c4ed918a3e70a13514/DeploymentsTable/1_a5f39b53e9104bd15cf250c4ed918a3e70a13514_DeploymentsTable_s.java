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
 package org.nuxeo.ide.sdk.deploy;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.jface.viewers.BaseLabelProvider;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.ToolItem;
 import org.nuxeo.ide.common.BundleImageProvider;
 import org.nuxeo.ide.common.UI;
 import org.nuxeo.ide.common.forms.WidgetName;
 import org.nuxeo.ide.sdk.SDKPlugin;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 @WidgetName("deployments")
 public class DeploymentsTable extends Composite {
 
     protected DeploymentPreferences prefs;
 
     protected BundleImageProvider imgs;
 
     protected CheckboxTableViewer tv;
 
     /**
      * a cache of deployment names for validation
      */
     protected Set<String> names;
 
     protected DeploymentDialog dialog;
 
     public DeploymentsTable(Composite parent) {
         super(parent, SWT.BORDER);
         imgs = new BundleImageProvider(SDKPlugin.getDefault().getBundle());
         createContent();
     }
 
     public DeploymentPreferences getPrefs() {
         return prefs;
     }
 
     public CheckboxTableViewer getTableViewer() {
         return tv;
     }
 
     private final Set<String> getNamesCache() {
         if (names == null) {
             names = new HashSet<String>();
             for (Deployment d : prefs.getDeployments()) {
                 names.add(d.getName());
             }
         }
         return names;
     }
 
     private final void invalidateNamesCache() {
         names = null;
     }
 
     public boolean validateName(String name) {
         return !getNamesCache().contains(name);
     }
 
     public void refresh() {
         try {
             setPrefs(DeploymentPreferences.load());
         } catch (Exception e) {
             UI.showError("Failed to load persisted configuration", e);
         }
     }
 
     public void setPrefs(DeploymentPreferences prefs) {
         invalidateNamesCache();
         this.prefs = prefs;
         tv.setInput(prefs);
         Deployment def = prefs.getDefault();
         if (def != null) {
             tv.setSelection(new StructuredSelection(def));
             for (TableItem item : tv.getTable().getItems()) {
                 item.setChecked(def == item.getData());
             }
         }
     }
 
     public void addDeployment(Deployment deployment) {
         invalidateNamesCache();
         prefs.addDeployment(deployment);
         tv.add(deployment);
         tv.setCheckedElements(new Object[] { deployment });
         tv.setSelection(new StructuredSelection(deployment));
     }
 
     public void removeDeployment(Deployment deployment) {
         invalidateNamesCache();
         int i = prefs.indexOf(deployment);
         prefs.removeDeployment(deployment);
         tv.remove(deployment);
         if (i == -1 || prefs.isEmpty()) {
             return;
         }
         ISelection selection = null;
         if (i == 0) {
             selection = new StructuredSelection(prefs.getDeployment(0));
         } else {
             selection = new StructuredSelection(prefs.getDeployment(i - 1));
         }
         tv.setSelection(selection);
     }
 
     public Deployment getSelection() {
         ISelection sel = tv.getSelection();
         if (!sel.isEmpty()) {
             return (Deployment) ((IStructuredSelection) sel).getFirstElement();
         }
         return null;
     }
 
     public void updateDeployment(Deployment deployment) {
         tv.update(deployment, null);
     }
 
     public Deployment getDefaultDeployment() {
         for (TableItem item : tv.getTable().getItems()) {
             if (item.getChecked()) {
                 return (Deployment) item.getData();
             }
         }
         return null;
     }
 
     protected String generateName() {
         Set<String> set = getNamesCache();
         String name = "Unnamed";
         int index = 0;
         while (set.contains(name)) {
             name = "Unnamed-" + (++index);
         }
         return name;
     }
 
     protected void createContent() {
         GridLayout layout = new GridLayout();
         setLayout(layout);
 
         ToolBar tbar = new ToolBar(this, SWT.FLAT);
         ToolItem add = new ToolItem(tbar, SWT.NONE);
         add.setText("Add");
         add.setToolTipText("Create a new configuration");
         add.setImage(imgs.getImage("icons/add.gif"));
 
         final ToolItem delete = new ToolItem(tbar, SWT.NONE);
         delete.setText("Delete");
         delete.setToolTipText("Remove the selected configuration");
         delete.setImage(imgs.getImage("icons/delete.gif"));
 
         tv = CheckboxTableViewer.newCheckList(this, SWT.BORDER | SWT.H_SCROLL
                 | SWT.V_SCROLL);
 
         tv.setLabelProvider(new MyLabelProvider());
         tv.setContentProvider(new MyContentProvider());
 
         tv.getTable().addListener(SWT.Selection, new Listener() {
             @Override
             public void handleEvent(Event event) {
                 itemChecked(event);
                 dialog.refreshDeploymentPanel();
                 delete.setEnabled(!tv.getSelection().isEmpty());
             }
         });
 
         GridData gd = new GridData();
         gd.horizontalAlignment = SWT.FILL;
         gd.grabExcessHorizontalSpace = true;
         gd.verticalAlignment = SWT.FILL;
         gd.grabExcessVerticalSpace = true;
         tv.getTable().setLayoutData(gd);
 
         tv.getTable().setFocus();
 
         delete.setEnabled(false);
 
         add.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 Deployment d = new Deployment(generateName());
                 addDeployment(d);
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
 
         delete.addSelectionListener(new SelectionListener() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 Deployment d = getSelection();
                 if (d != null) {
                     removeDeployment(d);
                 }
             }
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 widgetSelected(e);
             }
         });
     }
 
     public void dispose() {
         imgs.dispose();
         imgs = null;
         names = null;
     }
 
     protected void itemChecked(Event event) {
         TableItem selectedItem = (TableItem) event.item;
         selectedItem.setChecked(true);
         tv.getTable().setSelection(selectedItem);
         for (TableItem item : tv.getTable().getItems()) {
             if (item != selectedItem && item.getChecked()) {
                 item.setChecked(false);
             }
         }
     }
 
     static class MyLabelProvider extends BaseLabelProvider implements
             ILabelProvider {
 
         @Override
         public String getText(Object element) {
             return ((Deployment) element).getName();
         }
 
         @Override
         public Image getImage(Object element) {
             return null;
         }
     }
 
     static class MyContentProvider implements IStructuredContentProvider {
         @Override
         public Object[] getElements(Object inputElement) {
             return ((DeploymentPreferences) inputElement).getDeployments();
         }
 
         @Override
         public void dispose() {
         }
 
         @Override
         public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
         }
     }
 
     public void setDialog(DeploymentDialog dialog) {
         this.dialog = dialog;
     }
 
 }
