 /* Copyright (c) 2007 Bug Labs, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.buglabs.osgi.concierge.ui.launch;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.ComboBoxCellEditor;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 
 import com.buglabs.osgi.concierge.core.utils.ProjectUtils;
 import com.buglabs.osgi.concierge.launch.ConciergeLaunchConfiguration;
 import com.buglabs.osgi.concierge.natures.ConciergeProjectNature;
 import com.buglabs.osgi.concierge.runtime.ConciergeRuntime;
 
 /**
  * 
  * @author Ken Gilmer - ken@buglabs.net
  * 
  */
 public class BundleTab extends AbstractLaunchConfigurationTab {
 
 	private List launchBundles;
 
 	private List allBundles;
 
 	private CheckboxTableViewer viewer;
 
 	private Button selectAll;
 
 	private Button selectNone;
 
 	private Button initializeButton = null;
 
 	private Button continueButton;
 
 	private Group bundleGroup;
 
 	private List installList;
 
 	private Group cgBundleGroup;
 
 	private CheckboxTableViewer cgViewer;
 
 	private Button cgSelectAll;
 
 	private Button cgSelectNone;
 
 	private List coreBundles;
 
 	private List cgLaunchBundles;
 
 	private List cgInstallList;
 
 	private Map startLevelMap;
 
 	public static final String NAME = "Name";
 
 	public static final String INITIAL_STATE = "Initial State";
 
 	public static final String START_LEVEL = "Start Level";
 
 	public static final String[] COLUMN_PROPERTIES = { NAME, INITIAL_STATE, START_LEVEL };
 
 	public static final Integer INITIAL_STATE_INSTALL = new Integer(0);
 
 	public static final Integer INITIAL_STATE_START = new Integer(1);
 
 	public static final String INSTALL = "Install";
 
 	public static final String START = "Start";
 
 	private String frameworkStartLevel;
 
 	private Spinner frameworkStartLevelSpinner;
 
 	public BundleTab() {
 	}
 
 	public void createControl(Composite parent) {
 		Composite main = new Composite(parent, SWT.None);
 		main.setLayout(new GridLayout());
 		main.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Composite topComp = new Composite(main, SWT.None);
 		topComp.setLayout(new GridLayout(2, false));
 		topComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		Composite topSideComp = new Composite(topComp, SWT.None);
 		topSideComp.setLayout(new GridLayout());
 		topSideComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		continueButton = new Button(topSideComp, SWT.RADIO);
 		continueButton.setText("Continue with pre-existing state");
 
 		initializeButton = new Button(topSideComp, SWT.RADIO);
 		initializeButton.setText("Initialize Runtime (Clear any previous state)");
 
 		Composite frameworkStartLevelComp = new Composite(topComp, SWT.None);
 		frameworkStartLevelComp.setLayout(new GridLayout(2, false));
 		frameworkStartLevelComp.setLayoutData(new GridData(GridData.FILL_BOTH));
 		Label lblFrameworkStartLevel = new Label(frameworkStartLevelComp, SWT.NONE);
 		lblFrameworkStartLevel.setText("Framework Start Level: ");
 
 		frameworkStartLevelSpinner = new Spinner(frameworkStartLevelComp, SWT.BORDER);
 		frameworkStartLevelSpinner.setValues(1, 1, 10, 0, 1, 1);
 		frameworkStartLevelSpinner.addModifyListener(new ModifyListener() {
 
 			public void modifyText(ModifyEvent e) {
 				frameworkStartLevel = "" + ((Spinner) e.widget).getSelection();
 				refreshDialog();
 			}
 		});
 
 		Composite spaceComp = new Composite(main, SWT.None);
 		spaceComp.setLayout(new GridLayout());
 		spaceComp.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		cgBundleGroup = new Group(spaceComp, SWT.None);
 		cgBundleGroup.setText("Concierge Bundles");
 		cgBundleGroup.setLayout(new GridLayout());
 		cgBundleGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		Composite cgViewerComp = new Composite(cgBundleGroup, SWT.None);
 		cgViewerComp.setLayout(StripGridLayoutMargins(new GridLayout(2, false)));
 		cgViewerComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		Composite cgViewerBoxComp = new Composite(cgViewerComp, SWT.NONE);
 		cgViewerBoxComp.setLayout(StripGridLayoutMargins(new GridLayout()));
 		GridData gdata = new GridData(GridData.FILL_HORIZONTAL);
 		gdata.heightHint = 120;
 		cgViewerBoxComp.setLayoutData(gdata);
 
 		cgViewer = createBundleViewer(cgViewerBoxComp, true);
 		cgViewer.addCheckStateListener(new ICheckStateListener() {
 			public void checkStateChanged(CheckStateChangedEvent event) {
 				cgLaunchBundles = Arrays.asList(cgViewer.getCheckedElements());
 				refreshDialog();
 			}
 		});
 
 		new Label(cgViewerBoxComp, SWT.NONE).setText("These bundles are configurable in the Concierge preference page.");
 
 		Composite cgButtonComp = new Composite(cgViewerComp, SWT.None);
 		cgButtonComp.setLayout(StripGridLayoutMargins(new GridLayout()));
 		cgButtonComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
 
 		cgSelectAll = new Button(cgButtonComp, SWT.None);
 		cgSelectAll.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
 		cgSelectAll.setText("Select All");
 		cgSelectAll.addSelectionListener(new SelectionListener() {
 
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 			}
 
 			public void widgetSelected(SelectionEvent arg0) {
 				cgViewer.setAllChecked(true);
 				cgLaunchBundles = coreBundles;
 				refreshDialog();
 			}
 
 		});
 
 		cgSelectNone = new Button(cgButtonComp, SWT.None);
 		cgSelectNone.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
 		cgSelectNone.setText("Deselect All");
 		cgSelectNone.addSelectionListener(new SelectionListener() {
 
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 			}
 
 			public void widgetSelected(SelectionEvent arg0) {
 				cgViewer.setAllChecked(false);
 				cgLaunchBundles = new ArrayList(0);
 				refreshDialog();
 			}
 
 		});
 
 		// ----------------------------
 
 		bundleGroup = new Group(spaceComp, SWT.None);
 		bundleGroup.setText("Workspace Bundles");
 		bundleGroup.setLayout(new GridLayout());
 		bundleGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Composite viewerComp = new Composite(bundleGroup, SWT.None);
 		viewerComp.setLayout(StripGridLayoutMargins(new GridLayout(2, false)));
 		viewerComp.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Composite viewerBoxComp = new Composite(viewerComp, SWT.NONE);
 		viewerBoxComp.setLayout(StripGridLayoutMargins(new GridLayout()));
 		viewerBoxComp.setLayoutData(new GridData(GridData.FILL_BOTH));
 		viewer = createBundleViewer(viewerBoxComp, false);
 		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
 		viewer.addCheckStateListener(new ICheckStateListener() {
 			public void checkStateChanged(CheckStateChangedEvent event) {
 				launchBundles = Arrays.asList(viewer.getCheckedElements());
 				refreshDialog();
 			}
 		});
 
 		Composite buttonComp = new Composite(viewerComp, SWT.None);
 		buttonComp.setLayout(StripGridLayoutMargins(new GridLayout()));
 		buttonComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
 
 		selectAll = new Button(buttonComp, SWT.None);
 		selectAll.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
 		selectAll.setText("Select All");
 		selectAll.addSelectionListener(new SelectionListener() {
 
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 			}
 
 			public void widgetSelected(SelectionEvent arg0) {
 				viewer.setAllChecked(true);
 				launchBundles = allBundles;
 				refreshDialog();
 			}
 
 		});
 
 		selectNone = new Button(buttonComp, SWT.None);
 		selectNone.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
 		selectNone.setText("Deselect All");
 		selectNone.addSelectionListener(new SelectionListener() {
 
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 			}
 
 			public void widgetSelected(SelectionEvent arg0) {
 				viewer.setAllChecked(false);
 				launchBundles = new ArrayList(0);
 				refreshDialog();
 			}
 
 		});
 
 		initializeButton.addSelectionListener(new SelectionListener() {
 
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 			}
 
 			public void widgetSelected(SelectionEvent arg0) {
 				setViewerEnabled(initializeButton.getSelection());
 				refreshDialog();
 			}
 		});
 
 		setControl(main);
 	}
 
 	private CheckboxTableViewer createBundleViewer(Composite cgViewerBoxComp, boolean isCore) {
 		CheckboxTableViewer tviewer = CheckboxTableViewer.newCheckList(cgViewerBoxComp, SWT.FULL_SELECTION | SWT.BORDER);
 
 		Table table = tviewer.getTable();
 		table.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		TableColumn tc = new TableColumn(table, SWT.NONE);
 		tc.setText(NAME);
 		tc.setWidth(150);
 		tc = new TableColumn(table, SWT.NONE);
 		tc.setText(INITIAL_STATE);
 		tc.setWidth(90);
 		tc = new TableColumn(table, SWT.NONE);
 		tc.setText(START_LEVEL);
 		tc.setWidth(90);
 
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		// Add Cell Editors
 		CellEditor[] cellEditors = new CellEditor[] { new TextCellEditor(table, SWT.READ_ONLY),
 				new ComboBoxCellEditor(table, new String[] { "Install", "Start" }, SWT.READ_ONLY),
 				new ComboBoxCellEditor(table, new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }, SWT.READ_ONLY) };
 
 		tviewer.setColumnProperties(COLUMN_PROPERTIES);
 
 		if (isCore) {
 			tviewer.setCellModifier(new CoreBundleCellModifier(this));
			tviewer.setContentProvider(new ConciergeBundleProvider());
 			tviewer.setLabelProvider(new ConciergeCoreBundleLabelProvider());
 		} else {
 			tviewer.setCellModifier(new BundleCellModifier(this));
			tviewer.setContentProvider(new ConciergeBundleProvider());
 			tviewer.setLabelProvider(new ConciergeProjectLabelProvider());
 		}
 
 		tviewer.setCellEditors(cellEditors);
 		tviewer.setSorter(new ViewerSorter());
 
 		return tviewer;
 	}
 
 	protected void refreshDialog() {
 		setDirty(true);
 		updateLaunchConfigurationDialog();
 	}
 
 	private void setViewerEnabled(boolean state) {
 		selectAll.setEnabled(state);
 		selectNone.setEnabled(state);
 		viewer.getControl().setEnabled(state);
 		viewer.setAllGrayed(!state);
 		bundleGroup.setEnabled(state);
 
 		cgSelectAll.setEnabled(state);
 		cgSelectNone.setEnabled(state);
 		cgViewer.getControl().setEnabled(state);
 		cgViewer.setAllGrayed(!state);
 		cgBundleGroup.setEnabled(state);
 
 	}
 
 	public String getName() {
 		return "Bundles";
 	}
 
 	public void initializeFrom(ILaunchConfiguration configuration) {
 		try {
 			allBundles = getWSCGProjectNames();
 		} catch (CoreException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			allBundles = new Vector();
 		}
 
 		viewer.setInput(allBundles);
 
 		try {
 
 			launchBundles = configuration.getAttribute(ConciergeLaunchConfiguration.LAUNCH_BUNDLE_LIST_CONFIG, new ArrayList());
 			installList = configuration.getAttribute(ConciergeLaunchConfiguration.INSTALL_MAP, new ArrayList());
 			startLevelMap = configuration.getAttribute(ConciergeLaunchConfiguration.START_LEVEL_MAP, new HashMap());
 			viewer.setCheckedElements(launchBundles.toArray());
 			frameworkStartLevel = configuration.getAttribute(ConciergeLaunchConfiguration.FRAMEWORK_START_LEVEL, "1");
 			frameworkStartLevelSpinner.setSelection(Integer.parseInt(frameworkStartLevel));
 
 			if (configuration.getAttribute(ConciergeLaunchConfiguration.INITIALIZE_RUNTIME, true)) {
 				initializeButton.setSelection(true);
 			} else {
 				continueButton.setSelection(true);
 			}
 
 			setViewerEnabled(initializeButton.getSelection());
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		viewer.refresh();
 
 		List cgFiles = ConciergeRuntime.getDefault().getConciergeJars();
 
 		File frameworkJar = null;
 		for (Iterator i = cgFiles.iterator(); i.hasNext();) {
 			File f = (File) i.next();
 			if (f.getName().equals("cg_framework.jar")) {
 				frameworkJar = f;
 			}
 		}
 
 		cgFiles.remove(frameworkJar);
 
 		coreBundles = filesToStrings(cgFiles);
 		cgViewer.setInput(coreBundles);
 		try {
 			cgLaunchBundles = configuration.getAttribute(ConciergeLaunchConfiguration.LAUNCH_CORE_BUNDLE_LIST_CONFIG, new ArrayList());
 			cgInstallList = configuration.getAttribute(ConciergeLaunchConfiguration.CORE_INSTALL_MAP, new ArrayList());
 			cgViewer.setCheckedElements(cgLaunchBundles.toArray());
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
 		configuration.setAttribute(ConciergeLaunchConfiguration.LAUNCH_BUNDLE_LIST_CONFIG, launchBundles);
 		configuration.setAttribute(ConciergeLaunchConfiguration.INSTALL_MAP, installList);
 
 		configuration.setAttribute(ConciergeLaunchConfiguration.LAUNCH_CORE_BUNDLE_LIST_CONFIG, cgLaunchBundles);
 		configuration.setAttribute(ConciergeLaunchConfiguration.CORE_INSTALL_MAP, cgInstallList);
 
 		configuration.setAttribute(ConciergeLaunchConfiguration.INITIALIZE_RUNTIME, initializeButton.getSelection());
 		configuration.setAttribute(ConciergeLaunchConfiguration.START_LEVEL_MAP, startLevelMap);
 		configuration.setAttribute(ConciergeLaunchConfiguration.FRAMEWORK_START_LEVEL, frameworkStartLevel);
 	}
 
 	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
 		configuration.setAttribute(ConciergeLaunchConfiguration.LAUNCH_BUNDLE_LIST_CONFIG, new ArrayList(0));
 		configuration.setAttribute(ConciergeLaunchConfiguration.INSTALL_MAP, new ArrayList());
 		configuration.setAttribute(ConciergeLaunchConfiguration.INITIALIZE_RUNTIME, true);
 
 		configuration.setAttribute(ConciergeLaunchConfiguration.LAUNCH_CORE_BUNDLE_LIST_CONFIG, new ArrayList(0));
 		configuration.setAttribute(ConciergeLaunchConfiguration.CORE_INSTALL_MAP, new ArrayList());
 		configuration.setAttribute(ConciergeLaunchConfiguration.START_LEVEL_MAP, new HashMap());
 		configuration.setAttribute(ConciergeLaunchConfiguration.FRAMEWORK_START_LEVEL, "1");
 	}
 
	private class ConciergeBundleProvider implements IStructuredContentProvider {
 
 		public Object[] getElements(Object inputElement) {
 			return ((List) inputElement).toArray(new String[((List) inputElement).size()]);
 		}
 
 		public void dispose() {
 
 		}
 
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 
 		}
 	}
 
 	private static List filesToStrings(List files) {
 		List fileStrings = new ArrayList();
 
 		for (Iterator i = files.iterator(); i.hasNext();) {
 			fileStrings.add(((File) i.next()).getAbsolutePath());
 		}
 
 		return fileStrings;
 	}
 
 	/**
 	 * Builds a list of Concierge Projects
 	 * 
 	 * @return A list of projects in the workspace containing the Concierge
 	 *         Project Nature.
 	 * @throws CoreException
 	 */
 	public static List getWSCGProjectNames() throws CoreException {
 
 		final List projects = new Vector();
 
 		IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
 		wsroot.accept(new IResourceVisitor() {
 
 			public boolean visit(IResource resource) throws CoreException {
 
 				if (resource.getType() == IResource.ROOT) {
 					return true;
 				} else if (resource.getType() == IResource.PROJECT) {
 					IProject project = (IProject) resource;
 					if (project.isOpen() && project.hasNature(ConciergeProjectNature.ID)) {
						projects.add(project.getName());
 					}
 				}
 
 				return false;
 			}
 		});
 
 		return projects;
 	}
 
 	/**
 	 * Removes margins from a GridLayout
 	 * 
 	 * @param layout
 	 * @return
 	 */
 	private GridLayout StripGridLayoutMargins(GridLayout layout) {
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 
 		return layout;
 	}
 
 	private class CoreBundleCellModifier extends AbstractBundleCellModifier {
 
 		public CoreBundleCellModifier(BundleTab tab) {
 			super(tab);
 		}
 
 		protected List getInstallList() {
 			return cgInstallList;
 		}
 
 		protected Map getStartMap() {
 			return startLevelMap;
 		}
 
 		protected void setInstallList(List list) {
 			cgInstallList = list;
 		}
 
 		protected void setStartMap(Map map) {
 			startLevelMap = map;
 		}
 
 		protected Viewer getViewer() {
 			return cgViewer;
 		}
 
 	}
 
 	private class BundleCellModifier extends AbstractBundleCellModifier {
 
 		public BundleCellModifier(BundleTab tab) {
 			super(tab);
 		}
 
 		protected List getInstallList() {
 			return installList;
 		}
 
 		protected Map getStartMap() {
 			return startLevelMap;
 		}
 
 		protected void setInstallList(List list) {
 			installList = list;
 		}
 
 		protected void setStartMap(Map map) {
 			startLevelMap = map;
 		}
 
 		protected Viewer getViewer() {
 			return viewer;
 		}
 	}
 
 	private class ConciergeCoreBundleLabelProvider extends AbstractBundleLabelProvider {
 
 		public String getColumnText(Object element, int columnIndex) {
 			File f = new File((String) element);
 			switch (columnIndex) {
 			case 0:
 				try {
 					return getBundleName(f);
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				return f.getName();
 			case 1:
 				if (getInstallList() == null || !getInstallList().contains(element)) {
 					return "Start";
 				} else {
 					return "Install";
 				}
 			case 2:
 				// Start Level
 				if (getStartLevelMap() != null && getStartLevelMap().containsKey(element)) {
 					int value = Integer.parseInt(getStartLevelMap().get(element).toString());
 					return new Integer(value).toString();
 				} else {
 					// Default start level for concierge
 					return "1";
 				}
 			}
 			return "";
 		}
 
 		protected String getBundleName(File f) throws IOException {
 			Manifest m = (new JarFile(f)).getManifest();
 			String name = null;
 
 			if (m != null) {
 				name = m.getMainAttributes().getValue("Bundle-SymbolicName");
 
 				if (name == null) {
 					name = m.getMainAttributes().getValue("Bundle-Name");
 
 					if (name == null) {
 						name = f.getName();
 					}
 				}
 			}
 
 			return name;
 		}
 
 		protected List getInstallList() {
 			return cgInstallList;
 		}
 
 		protected Map getStartLevelMap() {
 			return startLevelMap;
 		}
 	}
 
 	private class ConciergeProjectLabelProvider extends AbstractBundleLabelProvider {
 		public String getColumnText(Object element, int columnIndex) {
			IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject((String) element);
 			switch (columnIndex) {
 			case 0:
 				try {
 					return getBundleName(p);
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (CoreException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				return p.getName();
 			case 1:
 				if (getInstallList() == null || !getInstallList().contains(element)) {
 					return "Start";
 				} else {
 					return "Install";
 				}
 			case 2:
 				// Start Level
 				if (getStartLevelMap() != null && getStartLevelMap().containsKey(element)) {
 					int value = Integer.parseInt(getStartLevelMap().get(element).toString());
 					return new Integer(value).toString();
 				} else {
 					// Default start level for concierge
 					return "1";
 				}
 			}
 			return "";
 		}
 
 		protected String getBundleName(IProject p) throws IOException, CoreException {
 				String name = p.getName();
 				Manifest m = new Manifest(ProjectUtils.getManifestFile(p).getContents());
 
 				name = m.getMainAttributes().getValue("Bundle-SymbolicName");
 
 				if (name == null || name.trim().length() < 1) {
 					name = m.getMainAttributes().getValue("Bundle-Name");
 
 					if (name == null) {
 						name = p.getName();
 					}
 				}
 
 			
 
 			return name;
 		}
 
 		protected List getInstallList() {
 			return installList;
 		}
 
 		protected Map getStartLevelMap() {
 			return startLevelMap;
 		}
 	}
 }
