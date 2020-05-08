 /**********************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
 *
  * Contributors:
  *    IBM - Initial API and implementation
  **********************************************************************/
 package org.eclipse.jst.server.tomcat.ui.internal.editor;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.TableLayout;
 import org.eclipse.jst.server.j2ee.IWebModule;
 import org.eclipse.jst.server.tomcat.core.ITomcatServerWorkingCopy;
 import org.eclipse.jst.server.tomcat.core.WebModule;
 import org.eclipse.jst.server.tomcat.core.internal.TomcatConfiguration;
 import org.eclipse.jst.server.tomcat.core.internal.TomcatServer;
 import org.eclipse.jst.server.tomcat.core.internal.command.AddWebModuleCommand;
 import org.eclipse.jst.server.tomcat.core.internal.command.ModifyWebModuleCommand;
 import org.eclipse.jst.server.tomcat.core.internal.command.RemoveWebModuleCommand;
 import org.eclipse.jst.server.tomcat.ui.internal.ContextIds;
 import org.eclipse.jst.server.tomcat.ui.internal.TomcatUIPlugin;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.help.WorkbenchHelp;
 
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.ui.ServerUICore;
 import org.eclipse.wst.server.ui.editor.ICommandManager;
 import org.eclipse.wst.server.ui.editor.ServerEditorPart;
 /**
  * Tomcat configuration web module editor page.
  */
 public class ConfigurationWebModuleEditorPart extends ServerEditorPart {
 	protected ITomcatServerWorkingCopy server2;
 	protected TomcatConfiguration configuration;
 
 	protected Table webAppTable;
 	protected int selection = -1;
 	protected Button addProject;
 	protected Button addExtProject;
 	protected Button remove;
 	protected Button edit;
 
 	protected PropertyChangeListener listener;
 
 	/**
 	 * ConfigurationWebModuleEditorPart constructor comment.
 	 */
 	protected ConfigurationWebModuleEditorPart() {
 		super();
 	}
 
 	/**
 	 * 
 	 */
 	protected void addChangeListener() {
 		listener = new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 				if (TomcatConfiguration.MODIFY_WEB_MODULE_PROPERTY.equals(event.getPropertyName())) {
 					initialize();
 				} else if (TomcatConfiguration.ADD_WEB_MODULE_PROPERTY.equals(event.getPropertyName())) {
 					initialize();
 				} else if (TomcatConfiguration.REMOVE_WEB_MODULE_PROPERTY.equals(event.getPropertyName())) {
 					initialize();
 				}
 			}
 		};
 		configuration.addPropertyChangeListener(listener);
 	}
 	
 	protected ICommandManager getCommandManager() {
 		return commandManager;
 	}
 	
 	/**
 	 * Creates the SWT controls for this workbench part.
 	 */
 	public void createPartControl(Composite parent) {
 		FormToolkit toolkit = getFormToolkit(parent.getDisplay());
 
 		ScrolledForm form = toolkit.createScrolledForm(parent);
 		form.setText(TomcatUIPlugin.getResource("%configurationEditorWebModulesPageTitle"));
 		form.getBody().setLayout(new GridLayout());
 	
 		Section section = toolkit.createSection(form.getBody(), ExpandableComposite.TWISTIE|ExpandableComposite.EXPANDED|ExpandableComposite.TITLE_BAR|Section.DESCRIPTION|ExpandableComposite.FOCUS_TITLE);
 		section.setText(TomcatUIPlugin.getResource("%configurationEditorWebModulesSection"));
 		section.setDescription(TomcatUIPlugin.getResource("%configurationEditorWebModulesDescription"));
 		section.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
 
 		Composite composite = toolkit.createComposite(section);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		layout.marginHeight = 5;
 		layout.marginWidth = 10;
 		layout.verticalSpacing = 5;
 		layout.horizontalSpacing = 15;
 		composite.setLayout(layout);
 		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
 		WorkbenchHelp.setHelp(composite, ContextIds.CONFIGURATION_EDITOR_WEBMODULES);
 		toolkit.paintBordersFor(composite);
 		section.setClient(composite);
 		
 		webAppTable = toolkit.createTable(composite, SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
 		webAppTable.setHeaderVisible(true);
 		webAppTable.setLinesVisible(true);
 		WorkbenchHelp.setHelp(webAppTable, ContextIds.CONFIGURATION_EDITOR_WEBMODULES_LIST);
 		//toolkit.paintBordersFor(webAppTable);
 		
 		TableLayout tableLayout = new TableLayout();
 	
 		TableColumn col = new TableColumn(webAppTable, SWT.NONE);
 		col.setText(TomcatUIPlugin.getResource("%configurationEditorPathColumn"));
 		ColumnWeightData colData = new ColumnWeightData(8, 85, true);
 		tableLayout.addColumnData(colData);
 	
 		TableColumn col2 = new TableColumn(webAppTable, SWT.NONE);
 		col2.setText(TomcatUIPlugin.getResource("%configurationEditorDocBaseColumn"));
 		colData = new ColumnWeightData(13, 135, true);
 		tableLayout.addColumnData(colData);
 	
 		TableColumn col3 = new TableColumn(webAppTable, SWT.NONE);
 		col3.setText(TomcatUIPlugin.getResource("%configurationEditorProjectColumn"));
 		colData = new ColumnWeightData(8, 85, true);
 		tableLayout.addColumnData(colData);
 	
 		TableColumn col4 = new TableColumn(webAppTable, SWT.NONE);
 		col4.setText(TomcatUIPlugin.getResource("%configurationEditorReloadColumn"));
 		colData = new ColumnWeightData(7, 75, true);
 		tableLayout.addColumnData(colData);
 		
 		webAppTable.setLayout(tableLayout);
 	
 		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		data.widthHint = 450;
 		data.heightHint = 120;
 		webAppTable.setLayoutData(data);
 		webAppTable.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				selectWebApp();
 			}
 		});
 	
 		Composite rightPanel = toolkit.createComposite(composite);
 		layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		rightPanel.setLayout(layout);
 		data = new GridData();
 		rightPanel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));
 		//toolkit.paintBordersFor(rightPanel);
 	
 		// buttons still to add:
 		// add project, add external module, remove module
 		addProject = toolkit.createButton(rightPanel, TomcatUIPlugin.getResource("%configurationEditorAddProjectModule"), SWT.PUSH);
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		addProject.setLayoutData(data);
 		WorkbenchHelp.setHelp(addProject, ContextIds.CONFIGURATION_EDITOR_WEBMODULES_ADD_PROJECT);
 	
 		// disable the add project module button if there are no
 		// web projects in the workbench
 		if (!canAddWebModule())
 			addProject.setEnabled(false);
 		else {
 			addProject.addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					WebModuleDialog dialog = new WebModuleDialog(getEditorSite().getShell(), getServer(), server2, configuration, true);
 					dialog.open();
 					if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
 						getCommandManager().executeCommand(new AddWebModuleCommand(configuration, dialog.getWebModule()));
 					}
 				}
 			});
 		}
 	
 		addExtProject = toolkit.createButton(rightPanel, TomcatUIPlugin.getResource("%configurationEditorAddExternalModule"), SWT.PUSH);
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		addExtProject.setLayoutData(data);
 		addExtProject.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				WebModuleDialog dialog = new WebModuleDialog(getEditorSite().getShell(), getServer(), server2, configuration, false);
 				dialog.open();
 				if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
 					getCommandManager().executeCommand(new AddWebModuleCommand(configuration, dialog.getWebModule()));
 				}
 			}
 		});
 		WorkbenchHelp.setHelp(addExtProject, ContextIds.CONFIGURATION_EDITOR_WEBMODULES_ADD_EXTERNAL);
 		
 		edit = toolkit.createButton(rightPanel, TomcatUIPlugin.getResource("%editorEdit"), SWT.PUSH);
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		edit.setLayoutData(data);
 		edit.setEnabled(false);
 		edit.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (selection < 0)
 					return;
 				WebModule module = (WebModule) configuration.getWebModules().get(selection);
 				WebModuleDialog dialog = new WebModuleDialog(getEditorSite().getShell(), getServer(), server2, configuration, module);
 				dialog.open();
 				if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
 					getCommandManager().executeCommand(new ModifyWebModuleCommand(configuration, selection, dialog.getWebModule()));
 				}
 			}
 		});
 		WorkbenchHelp.setHelp(edit, ContextIds.CONFIGURATION_EDITOR_WEBMODULES_EDIT);
 	
 		remove = toolkit.createButton(rightPanel, TomcatUIPlugin.getResource("%editorRemove"), SWT.PUSH);
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		remove.setLayoutData(data);
 		remove.setEnabled(false);
 		remove.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				if (selection < 0)
 					return;
 				getCommandManager().executeCommand(new RemoveWebModuleCommand(configuration, selection));
 				remove.setEnabled(false);
 				edit.setEnabled(false);
 				selection = -1;
 			}
 		});
 		WorkbenchHelp.setHelp(remove, ContextIds.CONFIGURATION_EDITOR_WEBMODULES_REMOVE);
 
 		initialize();
 	}
 
 	protected boolean canAddWebModule() {
 		IModule[] modules = ServerUtil.getModules(server.getServerType().getRuntimeType().getModuleTypes());
 		if (modules != null) {
 			int size = modules.length;
 			for (int i = 0; i < size; i++) {
 				IWebModule webModule = (IWebModule) modules[i].getAdapter(IWebModule.class);
 				if (webModule != null) {
 					IStatus status = server.canModifyModules(new IModule[] { modules[i] }, null, null);
 					if (status != null && status.isOK())
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public void dispose() {
 		if (configuration != null)
 			configuration.removePropertyChangeListener(listener);
 	}
 		
 	/* (non-Javadoc)
 	 * Initializes the editor part with a site and input.
 	 */
 	public void init(IEditorSite site, IEditorInput input) {
 		super.init(site, input);
 		
 		TomcatServer ts = (TomcatServer) server.getAdapter(TomcatServer.class);
 		configuration = ts.getTomcatConfiguration();
		if (configuration != null)
			addChangeListener();
 		
 		if (server != null)
 			server2 = (ITomcatServerWorkingCopy) server.getAdapter(ITomcatServerWorkingCopy.class);
 		
 		initialize();
 	}
 
 	/**
 	 * 
 	 */
 	protected void initialize() {
 		if (webAppTable == null)
 			return;
 
 		webAppTable.removeAll();
 	
 		List list = configuration.getWebModules();
 		Iterator iterator = list.iterator();
 		while (iterator.hasNext()) {
 			WebModule module = (WebModule) iterator.next();
 			TableItem item = new TableItem(webAppTable, SWT.NONE);
 	
 			// FIX-ME
 			String memento = module.getMemento();
 			String projectName = "";
 			Image projectImage = null;
 			if (memento != null && memento.length() > 0) {
 				projectName = TomcatUIPlugin.getResource("%configurationEditorProjectMissing", new String[] {memento});
 				projectImage = TomcatUIPlugin.getImage(TomcatUIPlugin.IMG_PROJECT_MISSING);
 				IModule module2 = ServerUtil.getModule(memento);
 				if (module != null) {
 					projectName = ServerUICore.getLabelProvider().getText(module2);
 					projectImage = ServerUICore.getLabelProvider().getImage(module2);
 				}
 			}
 	
 			String reload = module.isReloadable() ? TomcatUIPlugin.getResource("%configurationEditorReloadEnabled") : TomcatUIPlugin.getResource("%configurationEditorReloadDisabled");
 			String[] s = new String[] {module.getPath(), module.getDocumentBase(), projectName, reload};
 			item.setText(s);
 			item.setImage(0, TomcatUIPlugin.getImage(TomcatUIPlugin.IMG_WEB_MODULE));
 			if (projectImage != null)
 				item.setImage(2, projectImage);
 		}
 		
 		if (readOnly) {
 			addProject.setEnabled(false);
 			addExtProject.setEnabled(false);
 			edit.setEnabled(false);
 			remove.setEnabled(false);
 		} else {
 			addProject.setEnabled(canAddWebModule());
 			addExtProject.setEnabled(true);
 		}
 	}
 
 	/**
 	 * 
 	 */
 	protected void selectWebApp() {
 		if (readOnly)
 			return;
 
 		try {
 			selection = webAppTable.getSelectionIndex();
 			remove.setEnabled(true);
 			edit.setEnabled(true);
 		} catch (Exception e) {
 			selection = -1;
 			remove.setEnabled(false);
 			edit.setEnabled(false);
 		}
 	}
 	
 	/**
 	 * @see IWorkbenchPart#setFocus()
 	 */
 	public void setFocus() {
 		if (webAppTable != null)
 			webAppTable.setFocus();
 	}
 }
