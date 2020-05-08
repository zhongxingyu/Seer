 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.ui.wizards.project;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.text.MessageFormat;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.jboss.tools.common.model.ui.IValueChangeListener;
 import org.jboss.tools.common.model.ui.IValueProvider;
 import org.jboss.tools.common.model.ui.attribute.IListContentProvider;
 import org.jboss.tools.common.model.ui.attribute.editor.CheckBoxEditor;
 import org.jboss.tools.common.model.ui.attribute.editor.DirectoryChoicerEditor;
 import org.jboss.tools.common.model.ui.attribute.editor.DropDownEditor;
 import org.jboss.tools.common.model.ui.attribute.editor.ExtendedFieldEditor;
 import org.jboss.tools.common.model.ui.attribute.editor.IPropertyEditor;
 import org.jboss.tools.common.model.ui.attribute.editor.PropertyEditor;
 import org.jboss.tools.common.model.ui.attribute.editor.StringEditor;
 import org.eclipse.jface.preference.FieldEditor;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 
 import org.jboss.tools.common.meta.key.WizardKeys;
 import org.jboss.tools.common.model.plugin.ModelPlugin;
 import org.jboss.tools.common.model.ui.ModelUIPlugin;
 import org.jboss.tools.common.model.ui.widgets.DefaultSettings;
 import org.jboss.tools.common.model.ui.widgets.IWidgetSettings;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.jst.web.project.helpers.IWebProjectTemplate;
 import org.jboss.tools.jst.web.project.helpers.NewWebProjectContext;
 import org.jboss.tools.jst.web.project.version.ProjectVersion;
 import org.jboss.tools.jst.web.project.version.ProjectVersions;
 import org.jboss.tools.jst.web.ui.Messages;
 
 public class NewWebProjectWizardPage extends WizardPage {
 
 	protected Label introduction;
 	protected PropertyEditor nameEditor;
 	protected PropertyEditor locationEditor;
 	protected PropertyEditor useDefaultPathEditor;
 	protected PropertyEditor versionEditor;
 	protected PropertyEditor templateEditor;
 	
 	protected LocalValueProvider nameAdapter;
 	protected LocalValueProvider locationAdapter;
 	protected LocalValueProvider useDefaultPathAdapter;
 	protected LocalValueProvider versionAdapter;
 	protected TemplateAdapter templateAdapter;
 	
 	protected InputChangeListener inputChangeListener = new InputChangeListener();
 	
 	private IPath defaultPath;
 
 	private NewWebProjectContext context;
 	protected IWebProjectTemplate template;
 	
 	public NewWebProjectWizardPage(NewWebProjectContext context) {
 		super(Messages.NewWebProjectWizardPage_Page1);		
 		this.context = context;
 		template = context.getTemplate();
 		// init editors & adapters			
		IWidgetSettings settings = DefaultSettings.getDefault();
 			
 		nameEditor = new StringEditor(settings);
 		nameAdapter = new NameAdapter();
 		nameEditor.setInput(nameAdapter);
 		nameEditor.setLabelText(getString(getKey() + "_"+NewWebProjectContext.ATTR_NAME)+"*"); //$NON-NLS-1$ //$NON-NLS-2$
 
 		locationEditor = new DirectoryChoicerEditor(settings);
 		locationAdapter = new LocationAdapter();
 		locationEditor.setInput(locationAdapter);
 		locationEditor.setLabelText(getString(getKey() + "_"+NewWebProjectContext.ATTR_LOCATION)+"*"); //$NON-NLS-1$ //$NON-NLS-2$
 
 		useDefaultPathEditor = new CheckBoxEditor(settings);
 		useDefaultPathAdapter = new UseDefaultPathAdapter();
 		useDefaultPathEditor.setInput(useDefaultPathAdapter);
 		useDefaultPathEditor.setLabelText(getString(getKey() + "_"+NewWebProjectContext.ATTR_USE_DEFAULT_LOCATION)+"*"); //$NON-NLS-1$ //$NON-NLS-2$
 
 		versionEditor = new DropDownEditor(settings);
 		versionAdapter = new StrutsVersionAdapter();
 		versionEditor.setInput(versionAdapter);
 		versionEditor.setLabelText(getString(getKey() + "_"+NewWebProjectContext.ATTR_VERSION)+"*"); //$NON-NLS-1$ //$NON-NLS-2$
 
 		templateEditor = new DropDownEditor(settings);
 		templateAdapter = new TemplateAdapter();
 		templateEditor.setInput(templateAdapter);
 		templateEditor.setLabelText(getString(getKey() + "_"+NewWebProjectContext.ATTR_TEMPLATE)+"*"); //$NON-NLS-1$ //$NON-NLS-2$
 		
 		defaultPath = ModelUIPlugin.getWorkspace().getRoot().getLocation();
 		
 		this.setPageComplete(false);
 	}
 	
 	protected String getKey() {
 		return "newStrutsProjectPage1"; //$NON-NLS-1$
 	}
 	
 	Composite composite = null;
 	
 	public void createControl(Composite parent)	{
 		createControlImpl(parent);
 	}
 
 	private void createControlImpl(Composite parent)	{
 			GridData gd;
 			FieldEditor editor;
 			composite = new Composite(parent, SWT.NONE);
 			GridLayout layout = new GridLayout(2, false);
 			layout.horizontalSpacing = 10;
 			layout.verticalSpacing = 10;
 			layout.marginHeight = 4;
 			layout.marginWidth = 4;
 			composite.setLayout(layout);
 
 			// nameEditor
 			editor = nameEditor.getFieldEditor(composite);
 			editor.fillIntoGrid(composite, 2);
 			
 			// useDefaultPathEditor
 			editor = useDefaultPathEditor.getFieldEditor(composite);
 			editor.fillIntoGrid(composite, 2);
 			
 			// locationEditor
 			editor = locationEditor.getFieldEditor(composite);
 			editor.fillIntoGrid(composite, 2);
 			
 			// versionEditor 
 			editor = versionEditor.getFieldEditor(composite);
 			editor.fillIntoGrid(composite, 2);
 			
 			// templateEditor
 			editor = templateEditor.getFieldEditor(composite);
 			editor.fillIntoGrid(composite, 2);
 			
 			gd = new GridData(GridData.FILL_BOTH);
 			//gd.heightHint = 400;
 			composite.setLayoutData(gd);
 			
 			this.setControl(composite);
 ///Preference.USE_DEFAULT_PROJECT_ROOT.getValue()
 			// init default values
 			nameAdapter.setValue(""); //$NON-NLS-1$
 			useDefaultPathAdapter.setValue((getProjectRootOption() != null) ? "false" : "true"); //$NON-NLS-1$ //$NON-NLS-2$
 //			locationAdapter.setValue(defaultPath.toOSString());
 			if (getProjectRootOption() == null)
 				locationAdapter.setValue(defaultPath.toOSString());
 			else		
 				locationAdapter.setValue(getProjectRootOption());
 			versionAdapter.setValue(template.getDefaultVersion());
 			templateAdapter.setValue(template.getDefaultTemplate(template.getDefaultVersion()));
 //			setPageComplete(false);
 	}
 	
 	protected String getProjectRootOption() {
 		return null;
 	}
 	
 	private String getProjectName()	{
 		String s = getStringValue(nameAdapter);
 		return (s == null) ? null : s.trim();
 	}
 
 	private String getProjectLocation() {
 		return getStringValue(locationAdapter);
 	}
 	
 	protected String getStringValue(LocalValueProvider adapter) {
 		Object o = adapter.getValue();
 		return (o == null) ? null : o.toString();
 	}
 
 	private IProject getProjectHandle() {
 		return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
 	}
 	
 	private boolean validatePage() {
 		IWorkspace workspace = ModelUIPlugin.getWorkspace();
 		String projectName = getProjectName();
 		ProjectVersions versions = template.getProjectVersions();
 		String versionsError = versions.getErrorMessage();
 		if(versionsError != null) {
 			setErrorMessage(versionsError);
 			return false;
 		}
 
 		if ("".equals(projectName)) { //$NON-NLS-1$
 			setErrorMessage(null);
 			setMessage(getString(getKey() + "_specifyProjectName")); //$NON-NLS-1$
 			return false;
 		}
 		
 		IStatus nameStatus = workspace.validateName(projectName, IResource.PROJECT);
 		if (!nameStatus.isOK()) {
 			setErrorMessage(nameStatus.getMessage());
 			return false;
 		}
 
 		String projectLocation = getProjectLocation();	
 		if ("".equals(projectLocation)) { //$NON-NLS-1$
 			setErrorMessage(null);
 			setMessage(getString(getKey() + "_specifyLocation"));  //$NON-NLS-1$
 			return false;
 		}
 
 		if (!(new Path(projectLocation)).isAbsolute()) {
 			setErrorMessage(getString(getKey() + "_locationError"));  //$NON-NLS-1$
 			return false;
 		}
 
 		if (getProjectHandle().exists() || projectExistsIgnoreCase()) {
 			setErrorMessage(getString(getKey() + "_projectExistsMessage")); //$NON-NLS-1$
 			return false;
 		}
 		
 		IProject overlap = overlaps(projectLocation);
 		if(overlap != null) {
 			setErrorMessage(MessageFormat.format(
 					Messages.NewWebProjectWizardPage_OverlappingLocation,
 					overlap.getName())); 
 			return false;
 		}
 		
 		if ("".equals(getStringValue(versionAdapter))) { //$NON-NLS-1$
 			setErrorMessage(versionsError);
 			return false;
 		}
 
 		String versionName = versionAdapter.getStringValue(true);
 		ProjectVersion version = versions.getVersion(versionName);
 		String error = (version == null) ? MessageFormat.format(Messages.NewWebProjectWizardPage_CannotFindVersion, versionName) : version.getErrorMessage();
 		if(error != null) {
 			setErrorMessage(error);
 			return false;
 		}
 		
 		String templateName = getStringValue(templateAdapter);
 		if(templateName == null || templateName.length() == 0) {
 			String[] ts = template.getTemplateList(versionName);
 			error = (ts == null || ts.length == 0) 
 				? MessageFormat.format(Messages.NewWebProjectWizardPage_NoTemplates, versionName)
 				: Messages.NewWebProjectWizardPage_NoTemplateName;
 			setErrorMessage(error);
 			return false;
 		}
 		setErrorMessage(null);
 		setMessage(null);		
 		updateContext();
 		return true; 
 	}
 	
 	private boolean projectExistsIgnoreCase() {
 		String name = getProjectName();
 		return EclipseResourceUtil.projectExistsIgnoreCase(name);
 	}
 	
 	private IProject overlaps(String projectLocation) {
 		projectLocation = projectLocation.replace('\\','/').toLowerCase() + "/"; //$NON-NLS-1$
 		IProject[] ps = ModelPlugin.getWorkspace().getRoot().getProjects();
 		for (int i = 0; i < ps.length; i++) {
 			String location = ps[i].getLocation().toString().replace('\\','/').toLowerCase() + "/"; //$NON-NLS-1$
 			if(location.startsWith(projectLocation)) return ps[i];
 			if(projectLocation.startsWith(location)) return ps[i];
 		}
 		return null;
 	}
 	
 	public void updateContext()	{
 		context.setProject(getProjectHandle());
 		context.setProjectLocation(getProjectLocation());
 		context.setProjectTemplate(getStringValue(templateAdapter));
 		context.setVersion(getStringValue(versionAdapter));
 ///		context.updateApplicationName();
 	}
 
 	class LocalValueProvider implements IValueProvider, IValueChangeListener, IAdaptable {
 		protected Object value = ""; //$NON-NLS-1$
 		protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
 
 		// listeners
 		public void addValueChangeListener(PropertyChangeListener l) {
 			pcs.addPropertyChangeListener(l);
 		}
 		public void removeValueChangeListener(PropertyChangeListener l) {
 			pcs.removePropertyChangeListener(l);
 		}
 		public void fireValueChange(Object oldValue, Object newValue) {
 			pcs.firePropertyChange(IPropertyEditor.VALUE, oldValue, newValue);
 		}
 
 		// value
 		public Object getValue() {
 			return value;
 		}
 
 		public String getStringValue(boolean returnNullAsEmptyString) {
 			Object o = getValue();
 			return (o != null) ? o.toString() : (returnNullAsEmptyString) ? "" : null; //$NON-NLS-1$
 		}
 	
 		public void setValue(Object value) {
 			Object oldValue = this.value;
 			this.value = value;
 			fireValueChange(oldValue, this.value);
 		}
 		
 		// IValueChangeListener
 		public void	valueChange(PropertyChangeEvent event) {
 			this.setValue(event.getNewValue());
 		}
 
 		// IAdaptable
 		public Object getAdapter(Class adapter) {
 			if (adapter == IValueProvider.class) {
 				return this;
 			}
 			if (adapter == IValueChangeListener.class) {
 				return this;
 			}
 			Assert.isTrue(true, "LocalValueProvider instance itself cannot provide adapter for "+adapter.getName()); //$NON-NLS-1$
 			return null;
 		}
 	}
 
 	class NameAdapter extends LocalValueProvider {
 		public void setValue(Object value) {
 			super.setValue(value);
 			if ("true".equals("" + useDefaultPathAdapter.getValue())) { //$NON-NLS-1$ //$NON-NLS-2$
 				String name = (value == null) ? "" : value.toString().trim(); //$NON-NLS-1$
 				locationAdapter.setValue(defaultPath.append(name).toOSString());
 			}
 			setPageComplete(validatePage());
 		}
 	}
 
 	class LocationAdapter extends LocalValueProvider {
 		public void setValue(Object value) {
 			super.setValue(value);
 			setPageComplete(validatePage());
 		}
 	}
 	
 	class UseDefaultPathAdapter extends LocalValueProvider {
 		public void setValue(Object value) {
 			super.setValue(value);
 			if (getControl()!=null) {
 				locationEditor.getFieldEditor(null).setEnabled("false".equals("" + getValue()),(Composite)getControl()); //$NON-NLS-1$ //$NON-NLS-2$
 				String name = nameEditor.getValue().toString();
 				if(name == null) name = ""; else name = name.trim(); //$NON-NLS-1$
 				locationAdapter.setValue(defaultPath.append(name).toOSString());	
 			}
 			setPageComplete(validatePage());
 		}
 	}
 	
 	protected String[] getVersionList() {
 		return template.getVersionList();
 	}
 	
 	class StrutsVersionAdapter extends LocalValueProvider {
 		private ILabelProvider labelProvider;
 		private IListContentProvider listContentProvider;
 		
 		public StrutsVersionAdapter() {
 			super();
 			labelProvider = new LabelProvider(); 
 			listContentProvider = 
 				new IListContentProvider() {
 					public Object[] getElements(Object inputElement) {
 						return getVersionList();
 					}
 					public void dispose() {}
 					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
 				};
 		}
 		public Object getAdapter(Class adapter) {
 			if (adapter == IValueProvider.class) {
 				return this;
 			}
 			if (adapter == IValueChangeListener.class) {
 				return this;
 			}
 			if (adapter == ILabelProvider.class) {
 				return labelProvider;
 			}
 			if (adapter == IListContentProvider.class) {
 				return listContentProvider;
 			}
 			return super.getAdapter(adapter);
 		}
 		public void	valueChange(PropertyChangeEvent event) {
 			super.valueChange(event);
 			validationLock++;
 			try {
 				String v = templateAdapter.getStringValue(true);
 				templateAdapter.fireListChanged();
 				if(!v.equals(templateAdapter.getValue())) {
 					templateAdapter.setValue(template.getDefaultTemplate(versionAdapter.getStringValue(true)));
 				}
 			} finally {
 				validationLock--;
 			}
 			setPageComplete(validatePage());
 		}
 		
 	}
 	
 	protected String[] getTemplateList(String version) {
 		return template.getTemplateList(version);
 	}
 	
 	int validationLock = 0;
 
 	class TemplateAdapter extends LocalValueProvider {
 		private ILabelProvider labelProvider;
 		private IListContentProvider listContentProvider;
 		
 		public TemplateAdapter() {
 			super();
 			labelProvider = new LabelProvider(); 
 			listContentProvider = 
 				new IListContentProvider() {
 					public Object[] getElements(Object inputElement) {
 						return getTemplateList(NewWebProjectWizardPage.this.getStringValue(versionAdapter));
 					}
 					public void dispose() {}
 					public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
 				};
 		}
 		public Object getAdapter(Class adapter) {
 			if (adapter == IValueProvider.class) {
 				return this;
 			}
 			if (adapter == IValueChangeListener.class) {
 				return this;
 			}
 			if (adapter == ILabelProvider.class) {
 				return labelProvider;
 			}
 			if (adapter == IListContentProvider.class) {
 				return listContentProvider;
 			}
 			return super.getAdapter(adapter);
 		}
 		
 		public void fireListChanged() {
 			pcs.firePropertyChange(IPropertyEditor.LIST_CONTENT, null, null);
 		}
 		
 		public void	valueChange(PropertyChangeEvent event) {
 			super.valueChange(event);
 			if(validationLock == 0) {
 				setPageComplete(validatePage());
 			}
 		}
 	}
 
 	private String getString(String key) {
 		return WizardKeys.getString(key.replace(' ','_'));
 	}
 	
 	class InputChangeListener implements java.beans.PropertyChangeListener {
 		public void propertyChange(java.beans.PropertyChangeEvent evt) {
 			setPageComplete(validatePage());
 		}
 	}
 	
     public void setVisible(boolean visible) {
         super.setVisible(visible);
         if(visible) {
         	getControl().getDisplay().asyncExec(new Runnable() {
         		public void run() {
         			if(composite == null || composite.isDisposed()) return;
                 	((ExtendedFieldEditor)nameEditor.getFieldEditor(composite)).setFocus();
         		}
         	});
         }
     }
 
 }
