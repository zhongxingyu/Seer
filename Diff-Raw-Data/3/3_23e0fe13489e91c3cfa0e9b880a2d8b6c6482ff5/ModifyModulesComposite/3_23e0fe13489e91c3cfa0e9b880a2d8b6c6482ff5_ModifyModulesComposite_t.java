 /*******************************************************************************
  * Copyright (c) 2003, 2011 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.ui.internal.wizard.page;
 
 import java.util.*;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.wst.server.core.*;
 import org.eclipse.wst.server.ui.ServerUICore;
 import org.eclipse.wst.server.ui.internal.*;
 import org.eclipse.wst.server.ui.internal.view.servers.ModuleServer;
 import org.eclipse.wst.server.ui.wizard.IWizardHandle;
 /**
  * A wizard page used to add and remove modules.
  */
 public class ModifyModulesComposite extends Composite {
 	public static final String TASK_REFRESH_MODULES = "refresh-modules";
 
 	private static final String ROOT = "root";
 	protected static Color color;
 	protected static Font font;
 
 	protected IWizardHandle wizard;
 
 	protected IServerAttributes server;
 	protected IRuntime runtime;
 	protected boolean runtimeDirty;
 	protected Object refreshModules;
 	protected boolean showPublishOption;
 	protected boolean publishImmediately;
 
 	protected Map<ChildModuleMapKey, IModule[]> childModuleMap = new HashMap<ChildModuleMapKey, IModule[]>();
 	protected Map<IModule, IModule[]> parentModuleMap = new HashMap<IModule, IModule[]>();
 
 	// original modules on the server
 	protected List<IModule> originalModules = new ArrayList<IModule>();
 
 	// modules available to be added to the server
 	protected List<IModule> modules = new ArrayList<IModule>();
 
 	// current modules on the server
 	protected List<IModule> deployed = new ArrayList<IModule>();
 
 	protected TreeViewer availableTreeViewer;
 	protected TreeViewer deployedTreeViewer;
 
 	protected Button add, addAll;
 	protected Button remove, removeAll;
 
 	protected TaskModel taskModel;
 
 	// a module that must be kept on the server
 	protected IModule requiredModule;
 	protected boolean isComplete = true;
 
 	// the parent modules of the above modules. at least one of these modules
 	// must be kept on the server
 	protected IModule[] requiredModules;
 
 	protected Map<IModule, IStatus> errorMap;
 
 	abstract class TreeContentProvider implements ITreeContentProvider {
 		public void dispose() {
 			// do nothing
 		}
 
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 			// do nothing
 		}
 		
 		public Object[] getChildren(Object parentElement) {
 			ModuleServer ms = (ModuleServer) parentElement;
 			IModule[] parent = ms.module;
 			IModule[] children = childModuleMap.get(new ChildModuleMapKey(parent));
 			
 			List<ModuleServer> list = new ArrayList<ModuleServer>();
 			if (children != null) {
 				int size = children.length;
 				for (int i = 0; i < size; i++) {
 					IModule child = children[i];
 					
 					parentModuleMap.put(child, parent);
 					
 					int size2 = parent.length;
 					IModule[] module2 = new IModule[size2 + 1];
 					System.arraycopy(parent, 0, module2, 0, size2);
 					module2[size2] = child;
 					list.add(new ModuleServer(null, module2));
 				}
 			}
 			return list.toArray();
 		}
 
 		public Object getParent(Object element) {
 			ModuleServer ms = (ModuleServer) element;
 			IModule[] child = ms.module;
 			IModule[] modules2 = parentModuleMap.get(child);
 			if (modules2 == null)
 				return null;
 			return new ModuleServer(null, modules2);
 		}
 
 		public boolean hasChildren(Object element) {
 			ModuleServer ms = (ModuleServer) element;
 			IModule[] parent = ms.module;
 			IModule[] children = childModuleMap.get(new ChildModuleMapKey(parent));
 			return (children != null && children.length > 0);
 		}
 	}
 
 	class AvailableContentProvider extends TreeContentProvider {
 		public Object[] getElements(Object inputElement) {
 			List<ModuleServer> list = new ArrayList<ModuleServer>();
 			Iterator iterator = modules.iterator();
 			while (iterator.hasNext()) {
 				IModule module = (IModule) iterator.next();
 				list.add(new ModuleServer(null, new IModule[] { module }));
 			}
 			return list.toArray();
 		}
 	}
 
 	class DeployedContentProvider extends TreeContentProvider {
 		public Object[] getElements(Object inputElement) {
 			List<ModuleServer> list = new ArrayList<ModuleServer>();
 			Iterator iterator = deployed.iterator();
 			while (iterator.hasNext()) {
 				IModule module = (IModule) iterator.next();
 				list.add(new ModuleServer(null, new IModule[] { module }));
 			}
 			return list.toArray();
 		}
 	}
 
 	/**
 	 * The key element for the child module map
 	 * ChildMapModuleKey
 	 */
 	protected class ChildModuleMapKey {
 		protected IModule[] moduleTree;
 		
 		protected ChildModuleMapKey(IModule curModule) {
 			if (curModule != null) {
 				moduleTree = new IModule[] { curModule };
 			}
 		}
 		
 		protected ChildModuleMapKey(IModule[] curModuleTree) {
 			moduleTree = curModuleTree;
 		}
 		
 		public boolean equals(Object obj) {
 			if (obj == this) // same object
 				return true;
 			
 			if (!(obj instanceof ChildModuleMapKey))
 				return false;
 				
 			IModule[] curCompareModule = ((ChildModuleMapKey) obj).moduleTree;
 			if (curCompareModule == moduleTree) {
 				// the module tree is the same
 				return true;
 			} else if (moduleTree == null || curCompareModule == null || moduleTree.length != curCompareModule.length){
 				return false;
 			} else {
 				// compare each module
 				for (int i = 0; i < curCompareModule.length; i++) {
 					if (!curCompareModule[i].equals(moduleTree[i]))
 						return false;
 				}
 				return true;
 			}
 		}
 
 		public int hashCode() {
 			// force the same hash code on all the instances to makes sure the
 			// equals(Object) method is being used for comparing the objects
 			return 12345;
 		}
 	}
 
 	/**
 	 * Create a new ModifyModulesComposite.
 	 * 
 	 * @param parent a parent composite
 	 * @param wizard a wizard
 	 * @param module the module that is being added
 	 */
 	public ModifyModulesComposite(Composite parent, IWizardHandle wizard, IModule module) {
 		super(parent, SWT.NONE);
 		this.wizard = wizard;
 		requiredModule = module;
 		
 		wizard.setTitle(Messages.wizModuleTitle);
 		wizard.setDescription(Messages.wizModuleDescription);
 		wizard.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_WIZBAN_SELECT_SERVER));
 		
 		createControl();
 	}
 
 	/**
 	 * Create a new ModifyModulesComposite.
 	 * 
 	 * @param parent a parent composite
 	 * @param wizard a wizard
 	 * @param module the module that is being added
 	 * @param showPublishOption show the publishing options
 	 */
 	public ModifyModulesComposite(Composite parent, IWizardHandle wizard, IModule module, boolean showPublishOption) {
 		super(parent, SWT.NONE);
 		this.wizard = wizard;
 		requiredModule = module;
 		this.showPublishOption = showPublishOption;
 		
 		wizard.setTitle(Messages.wizModuleTitle);
 		wizard.setDescription(Messages.wizModuleDescription);
 		wizard.setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_WIZBAN_SELECT_SERVER));
 		
 		createControl();
 	}
 
 	public void setServer(IServerAttributes server) {
 		
 		// see bug 185875, 205869
 		if (refreshModules == taskModel.getObject(TASK_REFRESH_MODULES) && server == this.server) {
 			if (server == null)
 				return;
 			if (runtime == server.getRuntime()) {
 				if (runtime == null)
 					return;
 				if (runtime instanceof IRuntimeWorkingCopy) {
 					IRuntimeWorkingCopy wc = (IRuntimeWorkingCopy) runtime;
 					if (wc.isDirty() == runtimeDirty)
 						return;
 				} else
 					return;
 			}
 		}
 		
 		refreshModules = taskModel.getObject(TASK_REFRESH_MODULES);
 		this.server = server;
 		if (server == null)
 			runtime = null;
 		else
 			runtime = server.getRuntime();
 		runtimeDirty = false;
 		if (runtime != null) {
 			if (runtime instanceof IRuntimeWorkingCopy) {
 				IRuntimeWorkingCopy wc = (IRuntimeWorkingCopy) runtime;
 				if (wc.isDirty())
 					runtimeDirty = true;
 			}
 		}
 		
 		originalModules = new ArrayList<IModule>();
 		deployed = new ArrayList<IModule>();
 		modules = new ArrayList<IModule>();
 		
 		childModuleMap = new HashMap<ChildModuleMapKey, IModule[]>();
 		
 		if (server == null)
 			return;
 		
 		System.setProperty("J2EEcache", "" + Math.random());
 		
 		// get currently deployed modules
 		IModule[] currentModules = server.getModules();
 		if ((currentModules != null) && (currentModules.length > 0)) {
 			int size = currentModules.length;
 			for (int i = 0; i < size; i++) {
 				originalModules.add(currentModules[i]);
 				deployed.add(currentModules[i]);
 			}
 		}
 		
 		// add new module
 		requiredModules = null;
 		errorMap = new HashMap<IModule, IStatus>();
 		if (requiredModule != null) {
 			try {
 				IModule[] parents = server.getRootModules(requiredModule, null);
 				if (parents != null && parents.length > 0)
 					requiredModules = parents;
 				else
 					requiredModules = new IModule[] { requiredModule };
 			} catch (CoreException ce) {
 				if (Trace.INFO) {
 					Trace.trace(Trace.STRING_INFO, "A possible server implementation error", ce);
 				}
 			} catch (Exception e) {
 				if (Trace.WARNING) {
 					Trace.trace(Trace.STRING_WARNING, "Could not find root module", e);
 				}
 			}
 		}
 		if (requiredModules != null && !deployed.contains(requiredModules[0]))
 			deployed.add(requiredModules[0]);
 		
 		// get remaining modules
 		IModule[] modules2 = ServerUtil.getModules(server.getServerType().getRuntimeType().getModuleTypes());
 		if (modules2 != null) {
 			int size = modules2.length;
 			for (int i = 0; i < size; i++) {
 				IModule module = modules2[i];
 				if (!deployed.contains(module)) {
 					try {
 						IModule[] parents = server.getRootModules(module, null);
 						if (parents != null) {
 							int size2 = parents.length;
 							for (int j = 0; j < size2; j++) {
 								if (parents[j].equals(module)) {
 									IStatus status = server.canModifyModules(new IModule[] { module }, null, null);
 									if (status != null && !status.isOK())
 										errorMap.put(module, status);
 									modules.add(module);
 								}
 							}
 						}
 					} catch (CoreException ce) {
 						errorMap.put(module, ce.getStatus());
 						modules.add(module);
 					}
 				}
 			}
 		}
 		
 		// build child map
 		Iterator iterator = deployed.iterator();
 		while (iterator.hasNext()) {
 			IModule module = (IModule) iterator.next();
 			try {
 				IModule[] children = server.getChildModules(new IModule[] { module }, null);
 				if (children != null && children.length > 0)
 					childModuleMap.put(new ChildModuleMapKey(module), children);
 			} catch (Exception e) {
 				// ignore
 			}
 		}
 		
 		iterator = modules.iterator();
 		while (iterator.hasNext()) {
 			IModule module = (IModule) iterator.next();
 			try {
 				IModule[] children = server.getChildModules(new IModule[] { module }, null);
 				if (children != null && children.length > 0)
 					childModuleMap.put(new ChildModuleMapKey(module), children);
 			} catch (Exception e) {
 				// ignore
 			}
 		}
 		
 		// get children recursively one level
 		// put child elements into a different list to avoid concurrent modifications
 		Iterator<ChildModuleMapKey> iterator2 = childModuleMap.keySet().iterator();
 		List<ChildModuleMapKey> list = new ArrayList<ChildModuleMapKey>();
 		while (iterator2.hasNext()) {
 			list.add(iterator2.next());
 		}
 		
 		iterator = list.iterator();
 		while (iterator.hasNext()) {
 			ChildModuleMapKey key = (ChildModuleMapKey) iterator.next();
 			IModule[] children0 = childModuleMap.get(key);
 			if (children0 != null) {
 				int size = children0.length;
 				for (int i = 0; i < size; i++) {
 					int size2 = key.moduleTree.length;
 					IModule[] module2 = new IModule[size2 + 1];
 					System.arraycopy(key.moduleTree, 0, module2, 0, size2);
 					module2[size2] = children0[i];
 					
 					try {
 						IModule[] children = server.getChildModules(module2, null);
 						if (children != null && children.length > 0)
 							childModuleMap.put(new ChildModuleMapKey(module2), children);
 					} catch (Exception e) {
 						// ignore
 					}
 				}
 			}
 		}
 		
 		System.setProperty("J2EEcache", "");
 		
 		updateTaskModel();
 	}
 
 	public void setVisible(boolean b) {
 		if (b) {
 			this.refresh();
 		}
 		super.setVisible(b);
 	}
 
 	/**
 	 * Refresh the composite
 	 */
 	public void refresh() {
 
 		Display.getDefault().syncExec(new Runnable() {
 			public void run() {
 				if (availableTreeViewer == null || availableTreeViewer.getControl().isDisposed())
 					return;
 				try { // update trees if we can
 					availableTreeViewer.refresh();
 					deployedTreeViewer.refresh();
 					setEnablement();
 				} catch (Exception e) {
 					// ignore
 				}
 			}
 		});
 	}
 	
 	public void setTaskModel(TaskModel model) {
 		this.taskModel = model;
 	}
 
 	/**
 	 * Creates the UI of the page.
 	 */
 	protected void createControl() {
 		GridLayout layout = new GridLayout();
 		layout.horizontalSpacing = SWTUtil.convertHorizontalDLUsToPixels(this, 4);
 		layout.verticalSpacing = SWTUtil.convertVerticalDLUsToPixels(this, 4);
 		layout.numColumns = 3;
 		setLayout(layout);
 		setFont(getParent().getFont());
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, ContextIds.MODIFY_MODULES_COMPOSITE);
 		
 		Display display = getDisplay();
 		color = display.getSystemColor(SWT.COLOR_DARK_GRAY);
 		FontData[] fd = getFont().getFontData();
 		int size2 = fd.length;
 		for (int i = 0; i < size2; i++)
 			fd[i].setStyle(SWT.ITALIC);
 		font = new Font(display, fd);
 		addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(DisposeEvent e) {
 				font.dispose();
 			}
 		});
 		
 		Label label = new Label(this, SWT.NONE);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
 		data.horizontalSpan = 3;
 		label.setLayoutData(data);
 		label.setText(Messages.wizModuleMessage);
 		
 		label = new Label(this, SWT.NONE);
 		label.setText(Messages.wizModuleAvailableList);
 		
 		label = new Label(this, SWT.NONE);
 		label.setText("");
 		
 		label = new Label(this, SWT.NONE);
 		label.setText(Messages.wizModuleDeployedList);
 		
 		Tree availableTree = new Tree(this, SWT.BORDER | SWT.MULTI);
 		data = new GridData(GridData.FILL_BOTH);
 		data.heightHint = 200;
 		data.widthHint = 150;
 		availableTree.setLayoutData(data);
 		
 		availableTreeViewer = new TreeViewer(availableTree);
 		ILabelProvider labelProvider = ServerUICore.getLabelProvider();
 		labelProvider.addListener(new ILabelProviderListener() {
 			public void labelProviderChanged(LabelProviderChangedEvent event) {
 				Object[] obj = event.getElements();
 				if (obj == null)
 					availableTreeViewer.refresh(true);
 				else {
 					obj = ServerUIPlugin.adaptLabelChangeObjects(obj);
 					int size = obj.length;
 					for (int i = 0; i < size; i++)
 						availableTreeViewer.refresh(obj[i], true);
 				}
 			}
 		});
 		availableTreeViewer.setLabelProvider(labelProvider);
 		availableTreeViewer.setContentProvider(new AvailableContentProvider());
 		availableTreeViewer.setComparator(new ViewerComparator() {
 			public int compare(Viewer viewer, Object e1, Object e2) {
 				if (e1 instanceof ModuleServer && e2 instanceof ModuleServer) {
 					ModuleServer s1 = (ModuleServer) e1;
 					ModuleServer s2 = (ModuleServer) e2;
 					return (s1.module[s1.module.length - 1].getName().compareToIgnoreCase(s2.module[s2.module.length - 1].getName()));
 				}
 				
 				return super.compare(viewer, e1, e2);
 			}
 		});
 		availableTreeViewer.setInput(ROOT);
 		
 		availableTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				setEnablement();
 			}
 		});
 		availableTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				setEnablement();
 				if (add.isEnabled())
 					add(false);
 			}
 		});
 		
 		// slosh buttons
 		Composite comp = new Composite(this, SWT.NONE);
 		data = new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL);
 		data.widthHint = 120;
 		comp.setLayoutData(data);
 		
 		layout = new GridLayout();
 		layout.marginWidth = 5;
 		layout.marginHeight = 25;
 		layout.verticalSpacing = 20;
 		comp.setLayout(layout);
 		
 		add = new Button(comp, SWT.PUSH);
 		add.setText(Messages.wizModuleAdd);
 		add.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		add.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				add(false);
 			}
 		});
 		
 		remove = new Button(comp, SWT.PUSH);
 		remove.setText(Messages.wizModuleRemove);
 		remove.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		remove.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				remove(false);
 			}
 		});
 		
 		label = new Label(comp, SWT.NONE);
 		label.setText("");
 		
 		addAll = new Button(comp, SWT.PUSH);
 		addAll.setText(Messages.wizModuleAddAll);
 		addAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		addAll.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				add(true);
 			}
 		});
 		
 		removeAll = new Button(comp, SWT.PUSH);
 		removeAll.setText(Messages.wizModuleRemoveAll);
 		removeAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		removeAll.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				remove(true);
 			}
 		});
 		
 		Tree deployedTree = new Tree(this, SWT.BORDER | SWT.MULTI);
 		data = new GridData(GridData.FILL_BOTH);
 		data.widthHint = 150;
 		deployedTree.setLayoutData(data);
 		
 		deployedTreeViewer = new TreeViewer(deployedTree) {
 			public void doUpdateItem(Widget widget, Object element, boolean fullMap) {
 				if (widget instanceof TreeItem && color != null) {
 					TreeItem item = (TreeItem) widget;
 					if (element instanceof ModuleServer) {
 						ModuleServer ms = (ModuleServer) element;
 						IModule m = ms.module[ms.module.length-1];
 						if (m.isExternal())
 							item.setForeground(color);
 						else
 							item.setForeground(null);
 						if (!(server instanceof IServer) || ((IServer)server).getModulePublishState(ms.module) != IServer.PUBLISH_STATE_NONE)
 							item.setFont(font);
 						else
 							item.setFont(null);
 					}
 				}
 				super.doUpdateItem(widget, element, fullMap);
 			}
 		};
 		labelProvider = ServerUICore.getLabelProvider();
 		labelProvider.addListener(new ILabelProviderListener() {
 			public void labelProviderChanged(LabelProviderChangedEvent event) {
 				Object[] obj = event.getElements();
 				if (obj == null)
 					deployedTreeViewer.refresh(true);
 				else {
 					int size = obj.length;
 					for (int i = 0; i < size; i++)
 						deployedTreeViewer.refresh(obj[i], true);
 				}
 			}
 		});
 		deployedTreeViewer.setLabelProvider(labelProvider);
 		deployedTreeViewer.setContentProvider(new DeployedContentProvider());
 		deployedTreeViewer.setComparator(new ViewerComparator() {
 			public int compare(Viewer viewer, Object e1, Object e2) {
 				if (e1 instanceof ModuleServer && e2 instanceof ModuleServer) {
 					ModuleServer s1 = (ModuleServer) e1;
 					ModuleServer s2 = (ModuleServer) e2;
 					return (s1.module[s1.module.length - 1].getName().compareToIgnoreCase(s2.module[s2.module.length - 1].getName()));
 				}
 				
 				return super.compare(viewer, e1, e2);
 			}
 		});
 		deployedTreeViewer.setInput(ROOT);
 		
 		deployedTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				setEnablement();
 			}
 		});
 		deployedTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				setEnablement();
 				if (remove.isEnabled())
 					remove(false);
 			}
 		});
 		
 		if (showPublishOption) {
 			final Button publish = new Button(this, SWT.CHECK);
 			publish.setText(Messages.wizModulePublishImmediately);
 			data = new GridData(GridData.FILL_HORIZONTAL);
 			data.horizontalSpan = 3;
 			publish.setLayoutData(data);
 			
 			publishImmediately = ServerUIPlugin.getPreferences().getPublishOnAddRemoveModule();
 			publish.setSelection(publishImmediately);
 			publish.addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent event) {
 					publishImmediately = publish.getSelection();
 				}
 			});
 		}
 		
 		setEnablement();
 		availableTree.setFocus();
 		
 		Dialog.applyDialogFont(this);
 	}
 
 	protected ModuleServer[] getAvailableSelection() {
 		IStructuredSelection sel = (IStructuredSelection) availableTreeViewer.getSelection();
 		if (sel.isEmpty())
 			return new ModuleServer[0];
 			
 		ModuleServer []  mss = new ModuleServer[sel.size()];
 		System.arraycopy(sel.toArray(), 0, mss, 0, sel.size());
 		return mss;
 	}
 
 	protected ModuleServer[] getDeployedSelection() {
 		IStructuredSelection sel = (IStructuredSelection) deployedTreeViewer.getSelection();
 		if (sel.isEmpty())
 			return new ModuleServer[0];
 		
 		ModuleServer []  mss = new ModuleServer[sel.size()];
 		System.arraycopy(sel.toArray(), 0, mss, 0, sel.size());
 		return mss;
 	}
 
 	protected static IModule getModule(ModuleServer ms) {
 		if (ms == null)
 			return null;
 		IModule[] modules2 = ms.module;
 		return modules2[modules2.length - 1];
 	}
 
 	protected static IModule[] getModules(ModuleServer[] ms) {
 		if (ms == null)
 			return null;
 		IModule[] modules2 = new IModule[ms.length];
 		for (int i = 0; i < ms.length; i++)
 			modules2[i] = getModule(ms[i]);
 		
 		return modules2;
 	}
 
 	protected void setEnablement() {
 		wizard.setMessage(null, IMessageProvider.NONE);
 		
 		int count = 0;
 		if (requiredModules != null) {
 			// count how many of requiredModules are deployed
 			int size = requiredModules.length;
 			for (int i = 0; i < size; i++) {
 				if (deployed.contains(requiredModules[i]))
 					count++;
 			}
 		}
 		
 		// give error if there are more than one possible required modules and none are
 		// added to the server
 		isComplete = true;
 		if (requiredModules != null && requiredModules.length > 1 && count == 0) {
 			String s = "";
 			int size = requiredModules.length;
 			for (int i = 0; i < size; i++) {
 				if (i > 0)
 					s += " | ";
 				s += requiredModules[i].getName();
 			}
 			wizard.setMessage(NLS.bind(Messages.wizModuleRequiredModules, s), IMessageProvider.ERROR);
 			isComplete = false;
 		}
 		
 		// selection specific messages
 		ModuleServer[] ms = getAvailableSelection();
 		if (ms == null ||  ms.length == 0) {
 			add.setEnabled(false);
 		} else {
 			boolean enabled = false;
 			// iterate through selection, if we find at least ONE module that can't be added, exit the loop
 			for (int i = 0; i < ms.length; i++) {
 				IModule module = getModule(ms[i]);
 				if (module != null) {
 					try {
 						IStatus status = errorMap.get(module);
 						if (modules.contains(module)) {
 							if (status == null )
 								enabled = true;
 							else if (status.getSeverity() == IStatus.ERROR) {
 								// this module can't be added because has errors, exit the loop
 								enabled = false;
 								wizard.setMessage(status.getMessage(), IMessageProvider.ERROR);
 								break;
 							} else if (status.getSeverity() == IStatus.WARNING){
 								enabled = true;
 								wizard.setMessage(status.getMessage(), IMessageProvider.WARNING);
 							}
 							else if (status.getSeverity() == IStatus.INFO){
 								enabled = true;
 								wizard.setMessage(status.getMessage(), IMessageProvider.INFORMATION);
 							}
 						}
 						else{
 							// at least ONE module from the selection can't be added, exit the loop   
 							enabled = false;
 							break;
 						}
 					} catch (Exception e) {
 						if (Trace.INFO) {
 							Trace.trace(Trace.STRING_INFO, "Unable to handle error map for module:" + module);
 						} 
 					}
 				}
 			}
 			add.setEnabled(enabled);
 		}
 		addAll.setEnabled(modules.size() > 0);
 		
 		ms = getDeployedSelection();
 		if (ms == null ||  ms.length == 0) {
 			remove.setEnabled(false);
 		} else {
 			boolean enabled = false;
 			// iterate through selection, if we find at least ONE module that can't be added, exit the loop
 			for (int i = 0; i < ms.length; i++) {
 				IModule module = getModule(ms[i]);
 				if (module != null && deployed.contains(module)) {
 					// provide error about removing required single module
 					// required modules can't be removed
 					if (requiredModules != null){
 						if (requiredModules.length == 1 && requiredModules[0].equals(module)) {
 							// this is a required module and can't be removed, exit the loop
 							wizard.setMessage(NLS.bind(Messages.wizModuleRequiredModule, module.getName()), IMessageProvider.ERROR);
 							enabled = false;
 							break;
						} else
							enabled = true;
 					}
 					else 
 						enabled = true;
 				}
 				else{
 					// this module is not found in the 'deployed' array, the module might be a child
 					// at least ONE module from the selection can't be removed, exit the loop
 					enabled = false;
 					break;
 				}
 			}
 			remove.setEnabled(enabled);
 		}
 	
 		if (requiredModules == null)
 			removeAll.setEnabled(deployed.size() > 0);
 		else
 			removeAll.setEnabled(deployed.size() > 1);
 	}
 
 	protected void add(boolean all) {
 		if (all) {
 			IModule[] modules2 = new IModule[modules.size()];
 			modules.toArray(modules2);
 			moveAll(modules2, true);
 		} else
 			moveAll(getModules(getAvailableSelection()), true);
 		updateTaskModel();
 	}
 
 	protected void remove(boolean all) {
 		if (all) {
 			// pick one of the required modules to keep
 			IModule keep = null;
 			if (requiredModules != null) {
 				int size2 = requiredModules.length;
 				for (int i = 0; i < size2; i++) {
 					if (keep == null && deployed.contains(requiredModules[i]))
 						keep = requiredModules[i];
 				}
 			}
 			
 			List<IModule> list = new ArrayList<IModule>();
 			list.addAll(deployed);
 			list.remove(keep);
 			
 			IModule[] modules2 = new IModule[list.size()];
 			list.toArray(modules2);
 			
 			moveAll(modules2, false);
 		} else
 			moveAll(getModules(getDeployedSelection()), false);
 		updateTaskModel();
 	}
 
 	protected void moveAll(IModule[] mods, boolean add2) {
 		int size = mods.length;
 		List<IModule> list = new ArrayList<IModule>();
 		for (int i = 0; i < size; i++) {
 			IStatus status = errorMap.get(mods[i]);
 			
 			if (status == null && !list.contains(mods[i]))
 				list.add(mods[i]);
 		}
 		
 		Iterator iterator = list.iterator();
 		while (iterator.hasNext()) {
 			IModule module = (IModule) iterator.next();
 			ModuleServer ms = new ModuleServer(null, new IModule[] { module });
 			if (add2) {
 				modules.remove(module);
 				deployed.add(module);
 				availableTreeViewer.remove(ms);
 				deployedTreeViewer.add(ROOT, ms);
 			} else {
 				modules.add(module);
 				deployed.remove(module);
 				availableTreeViewer.add(ROOT, ms);
 				deployedTreeViewer.remove(ms);
 			}
 		}
 
 		setEnablement();
 	}
 
 	protected void updateTaskModel() {
 		if (taskModel == null)
 			return;
 
 		taskModel.putObject(TaskModel.TASK_MODULES, getModuleMap());
 		wizard.update();
 	}
 
 	public List<IModule> getModulesToRemove() {
 		List<IModule> list = new ArrayList<IModule>();
 		Iterator iterator = originalModules.iterator();
 		while (iterator.hasNext()) {
 			IModule module = (IModule) iterator.next();
 			if (!deployed.contains(module))
 				list.add(module);
 		}
 		return list;
 	}
 
 	public List<IModule> getModulesToAdd() {
 		List<IModule> list = new ArrayList<IModule>();
 		Iterator iterator = deployed.iterator();
 		while (iterator.hasNext()) {
 			IModule module = (IModule) iterator.next();
 			if (!originalModules.contains(module))
 				list.add(module);
 		}
 		return list;
 	}
 	
 	private void addChildMap(List<IModule[]> map, IModule[] parents, IModule[] children) {
 		if (children == null)
 			return;
 		
 		int size = children.length;
 		for (int i = 0; i < size; i++) {
 			IModule module = children[i];
 			
 			int size2 = parents.length;
 			IModule[] modules2 = new IModule[size2 + 1];
 			System.arraycopy(parents, 0, modules2, 0, size2);
 			modules2[size2] = module;
 			map.add(modules2);
 			
 			IModule[] children2 = childModuleMap.get(new ChildModuleMapKey(module));
 			if (children2 != null)
 				addChildMap(map, modules2, children2);
 		}
 	}
 
 	public List getModuleMap() {
 		final List<IModule[]> map = new ArrayList<IModule[]>();
 	
 		Iterator iterator = deployed.iterator();
 		while (iterator.hasNext()) {
 			IModule module = (IModule) iterator.next();
 			IModule[] moduleTree = new IModule[] { module };
 			map.add(moduleTree);
 			IModule[] children = childModuleMap.get(new ChildModuleMapKey(module));
 			if (children != null)
 				addChildMap(map, moduleTree, children);
 		}
 		
 		return map;
 	}
 
 	public boolean isComplete() {
 		return isComplete;
 	}
 
 	public boolean shouldPublishImmediately() {
 		return publishImmediately;
 	}
 }
