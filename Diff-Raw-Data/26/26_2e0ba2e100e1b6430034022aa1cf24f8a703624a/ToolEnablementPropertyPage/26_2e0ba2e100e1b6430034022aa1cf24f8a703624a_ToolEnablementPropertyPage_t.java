 /******************************************************************************* 
  * Copyright (c) 2011, 2012 Red Hat, Inc. 
  *  All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  *
  * @author Innar Made
  ******************************************************************************/
 package org.eclipse.bpmn2.modeler.ui.preferences;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.bpmn2.Bpmn2Package;
 import org.eclipse.bpmn2.modeler.core.preferences.Bpmn2Preferences;
 import org.eclipse.bpmn2.modeler.core.preferences.ToolEnablement;
 import org.eclipse.bpmn2.modeler.core.preferences.ToolEnablementPreferences;
 import org.eclipse.bpmn2.modeler.core.runtime.ModelEnablementDescriptor;
 import org.eclipse.bpmn2.modeler.core.runtime.TargetRuntime;
 import org.eclipse.bpmn2.modeler.ui.Activator;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.ICheckStateProvider;
 import org.eclipse.jface.viewers.IElementComparer;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.PropertyPage;
 import org.osgi.service.prefs.BackingStoreException;
 
 public class ToolEnablementPropertyPage extends PropertyPage {
 
 	private ToolEnablementPreferences toolEnablementPreferences;
 	private Bpmn2Preferences bpmn2Preferences;
 	private TargetRuntime currentRuntime;
 	private String currentProfile;
 	private final List<ToolEnablement> bpmnTools = new ArrayList<ToolEnablement>();
 	private final List<ToolEnablement> extensionTools = new ArrayList<ToolEnablement>();
 	private Object[] bpmnToolsEnabled;
 	private Object[] extensionToolsEnabled = new Object[] {};
 	
 	private Button btnOverride;
 	private Combo cboProfile;
 	private CheckboxTreeViewer bpmnTreeViewer;
 	private Tree bpmnTree;
 	private CheckboxTreeViewer extensionTreeViewer;
 	private Tree extensionTree;
 	private ModelEnablementDescriptor modelEnablements;
 
 	/**
 	 * Create the property page.
 	 */
 	public ToolEnablementPropertyPage() {
 		setTitle("Tool Enablement");
 	}
 
 	/**
 	 * Create contents of the property page.
 	 * 
 	 * @param parent
 	 */
 	@Override
 	public Control createContents(Composite parent) {
 		initData();
 
 		Composite container = new Composite(parent, SWT.NULL);
 		container.setLayout(new GridLayout(3, false));
 		container.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 1, 1));
 
 		final Label lblProfile = new Label(container, SWT.NONE);
 		lblProfile.setText("Default Profile:");
 		lblProfile.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 
 		cboProfile = new Combo(container, SWT.READ_ONLY);
 		cboProfile.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
 		currentProfile = bpmn2Preferences.getDefaultModelEnablementProfile();
 		int i = 1;
 		int iSelected = -1;
 		cboProfile.add("");
 		for (ModelEnablementDescriptor me : currentRuntime.getModelEnablements()) {
 			String profile = me.getProfile();
 			String text = profile;
 			if (text==null || text.isEmpty())
 				text = "Unnamed " + (i+1);
 			cboProfile.add(text);
 			cboProfile.setData(Integer.toString(i), me);
 			if (iSelected<0 && (currentProfile!=null && currentProfile.equals(profile)))
 				cboProfile.select(iSelected = i);
 			++i;
 		}
 
 		btnOverride = new Button(container,SWT.CHECK);
 		btnOverride.setText("Override the Default Profile with these settings:");
 		btnOverride.setSelection(bpmn2Preferences.getOverrideModelEnablementProfile());
 		btnOverride.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, 3, 1));
 
 		final Composite treesContainer = new Composite(container, SWT.BORDER);
 		treesContainer.setLayout(new GridLayout(2, true));
 		treesContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
 		
 		// Create Checkbox Tree Viwers for standard BPMN 2.0 elements and any extension elements
 		bpmnTreeViewer = createCheckboxTreeViewer(treesContainer, "Standard BPMN Elements");
 		bpmnTree = bpmnTreeViewer.getTree();
 
 		extensionTreeViewer = createCheckboxTreeViewer(treesContainer, "Extension Elements");
 		extensionTree = extensionTreeViewer.getTree();
 				
 		final Button btnCopy = new Button(container,SWT.FLAT);
 		btnCopy.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 1, 1));
 		btnCopy.setText("Initialize");
 
 		final Label lblCopy = new Label(container, SWT.NONE);
 		lblCopy.setText("these Override Settings from this Profile:");
 		lblCopy.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
 
 		final Combo cboCopy = new Combo(container, SWT.READ_ONLY);
 		cboCopy.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 1, 1));
 		i = 0;
 		iSelected = -1;
 		for (TargetRuntime rt : TargetRuntime.getAllRuntimes()) {
 			if (rt==currentRuntime || rt==TargetRuntime.getDefaultRuntime()) {
 				// can only copy enablements from the current runtime or from
 				// default since other plugins may contribute extension elements
 				// that the current runtime does not recognize.
 				for (ModelEnablementDescriptor me : rt.getModelEnablements()) {
 					String text = rt.getName();
 					if (me.getType()!=null)
 						text += " - " + me.getType();
 					if (me.getProfile()!=null)
 						text += " (" + me.getProfile() + ")";
 					cboCopy.add(text);
 					cboCopy.setData(Integer.toString(i), me);
 					if (rt == currentRuntime && iSelected<0)
 						cboCopy.select(iSelected = i);
 					++i;
 				}
 			}
 		}
 
 		// adjust height of the tree viewers to fill their container when dialog is resized
 		// oddly enough, setting GridData.widthHint still causes the controls to fill available
 		// horizontal space, but setting heightHint just keeps them the same height. Probably
 		// because a GridLayout has a fixed number of columns, but variable number of rows.
 		parent.addControlListener(new ControlAdapter() {
 			@Override
 			public void controlResized(ControlEvent e) {
 				GridData gd = (GridData) bpmnTree.getLayoutData();
 				gd.heightHint = 1000;
 				gd = (GridData) extensionTree.getLayoutData();
 				gd.heightHint = 1000;
 				treesContainer.layout();
 			}
 		});
 
 		Composite importExportButtons = new Composite(container, SWT.NONE);
 		importExportButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 3, 1));
 		importExportButtons.setLayout(new FillLayout());
 
 		Button btnImportProfile = new Button(importExportButtons, SWT.NONE);
 		btnImportProfile.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.NULL);
 				String path = dialog.open();
 				if (path != null) {
 					try {
 						bpmnTools.clear();
 						extensionTools.clear();
 						toolEnablementPreferences.importPreferences(path);
 						reloadPreferences();
 						bpmnTreeViewer.refresh();
 						extensionTreeViewer.refresh();
 						restoreDefaults();
 					} catch (Exception e1) {
 						Activator.showErrorWithLogging(e1);
 					}
 				}
 			}
 		});
 		btnImportProfile.setText("Import Profile ...");
 
 		Button btnExportProfile = new Button(importExportButtons, SWT.NONE);
 		btnExportProfile.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				FileDialog dialog = new FileDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), SWT.SAVE);
 				String path = dialog.open();
 				if (path != null) {
 					try {
 						String runtimeId = null;
 						String type = null;
 						String profile = null;
 						if (modelEnablements!=null) {
 							int i = cboCopy.getSelectionIndex();
 							ModelEnablementDescriptor me = (ModelEnablementDescriptor)cboCopy.getData(""+i);
 							runtimeId = me.getRuntime().getId();
 							profile = me.getProfile();
 							type = me.getType();
 						}
 						toolEnablementPreferences.exportPreferences(runtimeId, type, profile, path);
 					} catch (Exception e1) {
 						Activator.showErrorWithLogging(e1);
 					}
 				}
 			}
 		});
 		btnExportProfile.setText("Export Profile ...");
 		initDataBindings(bpmnTreeViewer, bpmnTools);
 		initDataBindings(extensionTreeViewer, extensionTools);
 
 		boolean enable = btnOverride.getSelection();
 		bpmnTree.setEnabled(enable);
 		extensionTree.setEnabled(enable);
 		btnCopy.setEnabled(enable);
 		lblCopy.setEnabled(enable);
 		cboCopy.setEnabled(enable);
 
 		btnOverride.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				boolean enable = btnOverride.getSelection();
 				bpmnTree.setEnabled(enable);
 				extensionTree.setEnabled(enable);
 				btnCopy.setEnabled(enable);
 				lblCopy.setEnabled(enable);
 				cboCopy.setEnabled(enable);
 			}
 		});
 		
 		btnCopy.addSelectionListener(new SelectionAdapter() {
 			
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				int i = cboCopy.getSelectionIndex();
 				ModelEnablementDescriptor me = (ModelEnablementDescriptor) cboCopy.getData(Integer.toString(i)); 
 				modelEnablements = TargetRuntime.reloadModelEnablements(me);
 				toolEnablementPreferences.setEnablements(modelEnablements);
 				reloadPreferences();
 				bpmnTreeViewer.refresh();
 				extensionTreeViewer.refresh();
 				restoreDefaults();
 			}
 		});
 
 
 		restoreDefaults();
 
 		return container;
 	}
 
 	private CheckboxTreeViewer createCheckboxTreeViewer(Composite parent, String name) {
 		
 		Composite container = new Composite(parent, SWT.NULL);
 		container.setLayout(new GridLayout(1, true));
 		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 
 		final Label label = new Label(container, SWT.NONE);
 		label.setText(name);
 		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 
 		final CheckboxTreeViewer treeViewer = new CheckboxTreeViewer(container, SWT.BORDER);
 		final Tree tree = treeViewer.getTree();
 
 		GridData data = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
 		data.heightHint = 200;
 		data.widthHint = 50;
 		tree.setLayoutData(data);
 		treeViewer.setCheckStateProvider(new ICheckStateProvider() {
 			@Override
 			public boolean isChecked(Object element) {
 				if (element instanceof ToolEnablement) {
 					ToolEnablement toolEnablement = (ToolEnablement)element;
 					if (toolEnablement.getChildren().size()>0) {
 						for (ToolEnablement child : toolEnablement.getChildren()) {
 							if (child.getEnabled())
 								return true;
 						}
 						return false;
 					}
 					return toolEnablement.getEnabled();
 				}
 				return false;
 			}
 
 			@Override
 			public boolean isGrayed(Object element) {
 				if (element instanceof ToolEnablement) {
 					ToolEnablement toolEnablement = (ToolEnablement)element;
 					int countEnabled = 0;
 					for (ToolEnablement child : toolEnablement.getChildren()) {
 						if (child.getEnabled())
 							++countEnabled;
 					}
 					return countEnabled>0 && countEnabled != toolEnablement.getChildren().size();
 				}
 				return false;
 			}
 			
 		});
 		
 		treeViewer.addCheckStateListener(new ICheckStateListener() {
 			@Override
 			public void checkStateChanged(CheckStateChangedEvent event) {
 				boolean checked = event.getChecked();
 				Object element = event.getElement();
 				if (element instanceof ToolEnablement) {
 					ToolEnablement toolEnablement = (ToolEnablement)element;
 					updateDescendents(toolEnablement, checked);
 					updateAncestors(toolEnablement.getParent(), checked);
 				}
 			}
 			
 			void updateDescendents(ToolEnablement toolEnablement, boolean checked) {
 				for (ToolEnablement child : toolEnablement.getChildren()) {
 					updateDescendents(child,checked);
 				}
 				toolEnablement.setSubtreeEnabled(checked);
 				treeViewer.setSubtreeChecked(toolEnablement, checked);
 				
 				treeViewer.setChecked(toolEnablement, checked);
 				treeViewer.setGrayed(toolEnablement, false);
 				for (ToolEnablement friend : toolEnablement.getFriends()) {
 					updateAncestors(friend, checked);
 					if (friend.getParent()!=null)
 						updateAncestors(friend.getParent(), checked);
 				}
 				for (ToolEnablement child : toolEnablement.getChildren()) {
 					for (ToolEnablement friend : child.getFriends()) {
 						if (child.getParent()!=null)
 							updateAncestors(child.getParent(), checked);
 						updateAncestors(friend, checked);
 					}
 				}
 			}
 			
 			void updateAncestors(ToolEnablement parent, boolean checked) {
 				while (parent!=null) {
 					int enabled = parent.getSubtreeEnabledCount();
 					int size = parent.getSubtreeEnabledCount();
 					if (enabled==0) {
 						treeViewer.setChecked(parent, false);
 						parent.setEnabled(false);
 						checked = true;
 					}
 					else if (enabled==size) {
 						treeViewer.setChecked(parent, true);
 						treeViewer.setGrayed(parent, false);
 						parent.setEnabled(true);
 					}
 					else {
 						treeViewer.setGrayChecked(parent, true);
 						parent.setEnabled(true);
 					}
 					
 					for (ToolEnablement friend : parent.getFriends()) {
 						updateAncestors(friend, checked);
 					}
 					bpmnTreeViewer.refresh(parent);
 					extensionTreeViewer.refresh(parent);
 					parent = parent.getParent();
 				}
 			}
 		});
 
 		treeViewer.setComparer(new IElementComparer() {
 
 			@Override
 			public boolean equals(Object a, Object b) {
 				return a == b;
 			}
 
 			@Override
 			public int hashCode(Object element) {
 				return System.identityHashCode(element);
 			}
 		});
 		treeViewer.setUseHashlookup(true);
 		
 		return treeViewer;
 	}
 	
 	private void restoreDefaults() {
 		bpmnTreeViewer.setCheckedElements(bpmnToolsEnabled);
 		extensionTreeViewer.setCheckedElements(extensionToolsEnabled);
 		bpmnTreeViewer.refresh();
 		extensionTreeViewer.refresh();
 	}
 
 	@Override
 	protected void performDefaults() {
 		super.performDefaults();
 		restoreDefaults();
 	}
 
 	private void initData() {
 		toolEnablementPreferences = ToolEnablementPreferences.getPreferences((IProject) getElement().getAdapter(IProject.class));
 		IProject project = (IProject)getElement().getAdapter(IProject.class);
 		bpmn2Preferences = Bpmn2Preferences.getInstance(project);
 
 		modelEnablements = null;
 		String profile = bpmn2Preferences.getDefaultModelEnablementProfile();
 		currentRuntime = bpmn2Preferences.getRuntime();
 		if (profile!=null && !profile.isEmpty()) {
 			for (ModelEnablementDescriptor me : currentRuntime.getModelEnablements()) {
 				if (profile.equals(me.getProfile())) {
 					modelEnablements = TargetRuntime.reloadModelEnablements(me);
 					break;
 				}
 			}
 		}
 		if (modelEnablements == null) {
 			if (currentRuntime.getModelEnablements().size()>0) {
 				ModelEnablementDescriptor me = currentRuntime.getModelEnablements().get(0);
 				modelEnablements = TargetRuntime.reloadModelEnablements(me);
 			}
 		}
 		
 		reloadPreferences();
 	}
 
 	private void reloadPreferences() {
 		bpmnToolsEnabled = reloadPreferences(bpmnTools, null, null);
 		if (modelEnablements!=null)
 			extensionToolsEnabled = reloadPreferences(extensionTools, bpmnTools, modelEnablements);
 	}
 	
 	private Object[] reloadPreferences(List<ToolEnablement> tools, List<ToolEnablement> bpmnTools, ModelEnablementDescriptor me) {
 		tools.clear();
 		if (me!=null)
 			tools.addAll(toolEnablementPreferences.getAllExtensionElements(me, bpmnTools));
 		else
 			tools.addAll(toolEnablementPreferences.getAllElements());
 
 		ArrayList<ToolEnablement> enabled = new ArrayList<ToolEnablement>();
 		for (ToolEnablement tool : tools) {
 			if (tool.getEnabled()) {
 				enabled.add(tool);
 			}
 			ArrayList<ToolEnablement> children = tool.getChildren();
 			for (ToolEnablement t : children) {
 				if (t.getEnabled()) {
 					enabled.add(t);
 				}
 			}
 		}
 		return enabled.toArray();
 	}
 
 	@Override
 	public boolean performOk() {
 		setErrorMessage(null);
 		try {
 			updateToolEnablement(bpmnTools, Arrays.asList(bpmnTreeViewer.getCheckedElements()));
 			for (ToolEnablement t : extensionTools) {
 				updateToolEnablement(t.getChildren(), Arrays.asList(extensionTreeViewer.getCheckedElements()));
 			}
 			
 			bpmn2Preferences.setOverrideModelEnablements(btnOverride.getSelection());
 			bpmn2Preferences.setDefaultModelEnablementProfile(cboProfile.getText());
 
 			bpmn2Preferences.save();
 		} catch (BackingStoreException e) {
 			Activator.showErrorWithLogging(e);
 		}
 		return true;
 	}
 
 	private void updateToolEnablement(List<ToolEnablement> saveables, List<Object> enabled)
 			throws BackingStoreException {
 		for (ToolEnablement t : saveables) {
 			boolean enable = enabled.contains(t);
 			toolEnablementPreferences.setEnabled(t, enable);
 			for (ToolEnablement c : t.getChildren()) {
 				enable = enabled.contains(c);
 				toolEnablementPreferences.setEnabled(c, enable);
 			}
 		}
 		toolEnablementPreferences.flush();
 	}
 
 	protected DataBindingContext initDataBindings(CheckboxTreeViewer treeViewer, List<ToolEnablement> tools) {
 		DataBindingContext bindingContext = new DataBindingContext();
 		//
 		treeViewer.setContentProvider(new ITreeContentProvider() {
 
 			@Override
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			}
 
 			@Override
 			public void dispose() {
 			}
 
 			@Override
 			public boolean hasChildren(Object element) {
 				if (element instanceof ToolEnablement) {
 					return !((ToolEnablement) element).getChildren().isEmpty();
 				}
 				return false;
 			}
 
 			@Override
 			public Object getParent(Object element) {
 				if (element instanceof ToolEnablement) {
 					return ((ToolEnablement) element).getParent();
 				}
 				return null;
 			}
 
 			@Override
 			public Object[] getElements(Object inputElement) {
 				if (inputElement instanceof WritableList) {
 					return ((WritableList) inputElement).toArray();
 				}
 				return null;
 			}
 
 			@Override
 			public Object[] getChildren(Object parentElement) {
 				if (parentElement instanceof ToolEnablement) {
 					return ((ToolEnablement) parentElement).getChildren().toArray();
 				}
 				return null;
 			}
 		});
 
 		treeViewer.setLabelProvider(new ILabelProvider() {
 			@Override
 			public void removeListener(ILabelProviderListener listener) {
 			}
 
 			@Override
 			public boolean isLabelProperty(Object element, String property) {
 				return false;
 			}
 
 			@Override
 			public void dispose() {
 
 			}
 
 			@Override
 			public void addListener(ILabelProviderListener listener) {
 			}
 
 			@Override
 			public Image getImage(Object element) {
 				return null;
 			}
 
 			@Override
 			public String getText(Object element) {
 				if (element instanceof ToolEnablement) {
 					return ((ToolEnablement) element).getName();
 				}
 				return null;
 			}
 		});
 		WritableList writableList = new WritableList(tools, ToolEnablement.class);
 		treeViewer.setInput(writableList);
 		//
 		return bindingContext;
 	}
 
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			if (TargetRuntime.DEFAULT_RUNTIME_ID.equals(bpmn2Preferences.getRuntime().getId())) {
				extensionTree.setVisible(false);
				GridData data = (GridData)extensionTree.getLayoutData();
				data.exclude = true;
			}
			else {
				extensionTree.setVisible(true);
				GridData data = (GridData)extensionTree.getLayoutData();
				data.exclude = false;
			}
		}
		super.setVisible(visible);
	}

 }
