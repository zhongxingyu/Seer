 /*******************************************************************************
  *  Copyright 2007 Ketan Padegaonkar http://ketan.padegaonkar.name
  *  
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *  
  *      http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  ******************************************************************************/
 package net.sourceforge.jcctray.ui.settings.internal;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import net.sourceforge.jcctray.model.DashBoardProject;
 import net.sourceforge.jcctray.model.Host;
 import net.sourceforge.jcctray.model.JCCTraySettings;
 import net.sourceforge.jcctray.ui.Utils;
 import net.sourceforge.jcctray.ui.settings.AddProjectDialog;
 
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 
 public class BuildProjectsSettingsTab {
 
	private final class ShellRefreshListener extends ShellAdapter{
		public void shellActivated(ShellEvent e) {
			tableViewer.setInput(JCCTraySettings.getInstance().getHosts());				
		}
	}

 	private final class ServerProjectContentProvider implements IStructuredContentProvider {
 		public Object[] getElements(Object elements) {
 			List hosts = (List) elements;
 			List result = new ArrayList();
 			for (Iterator iterator = hosts.iterator(); iterator.hasNext();) {
 				Host host = (Host) iterator.next();
 				Collection projects = host.getProjects();
 				for (Iterator iterator2 = projects.iterator(); iterator2.hasNext();) {
 					DashBoardProject project = (DashBoardProject) iterator2.next();
 					if (project.isEnabled())
 						result.add(project);
 				}
 			}
 			return result.toArray();
 		}
 
 		public void dispose() {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
 			// TODO Auto-generated method stub
 
 		}
 	}
 
 	private final class ServerProjectLabelProvider implements ITableLabelProvider {
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 
 		public String getColumnText(Object element, int columnIndex) {
 			DashBoardProject project = (DashBoardProject) element;
 			switch (columnIndex) {
 			case 0:
 				return project.getHost().getHostString();
 			case 1:
 				return project.getName();
 			}
 			return null;
 		}
 
 		public void addListener(ILabelProviderListener arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void dispose() {
 			// TODO Auto-generated method stub
 
 		}
 
 		public boolean isLabelProperty(Object arg0, String arg1) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public void removeListener(ILabelProviderListener arg0) {
 			// TODO Auto-generated method stub
 
 		}
 	}
 
 	private final class RemoveButtonListener implements SelectionListener {
 
 		public void widgetDefaultSelected(SelectionEvent e) {
 			IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
 			List list = selection.toList();
 			
 			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
 				DashBoardProject project = (DashBoardProject) iterator.next();
 				project.setEnabled(false);
 			}
 			Utils.saveSettings(tableViewer.getTable().getShell());
 			tableViewer.refresh();
 		}
 
 		public void widgetSelected(SelectionEvent e) {
 			widgetDefaultSelected(e);
 		}
 	}
 
 	private final class AddButtonListener implements SelectionListener {
 
 		private final TableViewer	tableViewer;
 
 		public AddButtonListener(TableViewer tableViewer) {
 			this.tableViewer = tableViewer;
 
 		}
 
 		public void widgetDefaultSelected(SelectionEvent e) {
 			new AddProjectDialog(tableViewer.getTable().getShell()).open();
 		}
 
 		public void widgetSelected(SelectionEvent e) {
 			widgetDefaultSelected(e);
 		}
 	}
 
 	private TableViewer	tableViewer;
 	private Button		addButton;
 	private Button		removeButton;
 
 	public BuildProjectsSettingsTab(TabFolder folder) {
 		createBuildProjectsTab(folder);
 		hookEvents();
 		tableViewer.setInput(JCCTraySettings.getInstance().getHosts());
 	}
 
 	private void hookEvents() {
		tableViewer.getTable().getShell().addShellListener(new ShellRefreshListener());
 		addButton.addSelectionListener(new AddButtonListener(tableViewer));
 		removeButton.addSelectionListener(new RemoveButtonListener());
 	}
 
 	private void createBuildProjectsTab(TabFolder tabFolder) {
 		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
 		tabItem.setText("Build Projects");
 		Composite composite = new Composite(tabFolder, SWT.NONE);
 		tabItem.setControl(composite);
 		composite.setLayout(new GridLayout(2, false));
 
 		new Label(composite, SWT.NONE).setText("Use this section to define the projects to monitor.");
 		new Label(composite, SWT.NONE);
 
 		// table
 		Table table = new Table(composite, SWT.MULTI | SWT.FULL_SELECTION | SWT.V_SCROLL);
 		tableViewer = new TableViewer(table);
 		tableViewer.setLabelProvider(new ServerProjectLabelProvider());
 		tableViewer.setContentProvider(new ServerProjectContentProvider());
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		TableColumn tableColumn;
 		tableColumn = new TableColumn(table, SWT.LEFT);
 		tableColumn.setText("Build Server");
 		tableColumn.setWidth(150);
 
 		tableColumn = new TableColumn(table, SWT.LEFT);
 		tableColumn.setText("Project");
 		tableColumn.setWidth(200);
 
 		// add remove buttons
 		composite = new Composite(composite, SWT.NONE);
 		GridLayout fillLayout = new GridLayout(1, false);
 		composite.setLayout(fillLayout);
 
 		addButton = new Button(composite, SWT.NONE);
 		addButton.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, true));
 		addButton.setText("&Add");
 
 		removeButton = new Button(composite, SWT.NONE);
 		removeButton.setText("&Remove");
 		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, true));
 	}
 }
