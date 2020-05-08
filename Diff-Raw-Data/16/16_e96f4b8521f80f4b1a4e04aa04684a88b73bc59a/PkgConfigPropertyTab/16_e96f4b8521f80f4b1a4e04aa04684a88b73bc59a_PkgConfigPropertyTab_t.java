 /*******************************************************************************
  * Copyright (c) 2011 Petri Tuononen and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Petri Tuononen - Initial implementation
  *******************************************************************************/
 package org.eclipse.cdt.managedbuilder.pkgconfig.properties;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.cdt.core.settings.model.ICResourceDescription;
 import org.eclipse.cdt.managedbuilder.pkgconfig.util.Parser;
 import org.eclipse.cdt.managedbuilder.pkgconfig.util.PathToToolOption;
 import org.eclipse.cdt.managedbuilder.pkgconfig.util.PkgConfigUtil;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 
 /**
  * Property tab to select packages and add pkg-config output
  * of checked packages to compiler and linker.
  * 
  * TODO: Save checked packages to .cproject
  * TODO: Initialize checked packages from .cproject
  */
 public class PkgConfigPropertyTab extends AbstractCPropertyTab {
 
 	private CheckboxTableViewer pkgCfgViewer;
 	private Object newChecked;
 	private Object removedItem;
 	private boolean newItemToggle;
 	private Set<Object> checkedSet = new HashSet<Object>();
 	private static final int BUTTON_SELECT = 0;
 	private static final int BUTTON_DESELECT = 1;
 	
 	protected SashForm sashForm;
 	protected Composite comp;
 	
 	private static final String[] BUTTONS = new String[] {
 		"Select",
 		"Deselect"
 	};
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	public void createControls(Composite parent) {
 		super.createControls(parent);
 		usercomp.setLayout(new GridLayout(1, false));
 
 		sashForm = new SashForm(usercomp, SWT.NONE);
 		sashForm.setBackground(sashForm.getDisplay().getSystemColor(SWT.COLOR_GRAY));
 		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		GridLayout layout = new GridLayout(1, false);
 		layout.marginHeight = 5;
 		sashForm.setLayout(layout);
 
 		Composite c1 = new Composite(sashForm, SWT.NONE);
		GridLayout layout2 = new GridLayout(2, false);
		layout2.marginHeight = 15;
		c1.setLayout(layout2);
 		
 		pkgCfgViewer = CheckboxTableViewer.newCheckList(c1, SWT.MULTI | SWT.H_SCROLL
 				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
 		final Table tbl = pkgCfgViewer.getTable();
 		tbl.setHeaderVisible(true);
 		tbl.setLinesVisible(true);
 
 		createColumns(c1, pkgCfgViewer);
 		pkgCfgViewer.setContentProvider(new ArrayContentProvider());
 		pkgCfgViewer.setInput(DataModelProvider.INSTANCE.getEntries());
 		
 //		tbl.addSelectionListener(new SelectionAdapter() {
 //			@Override
 //			public void widgetSelected(SelectionEvent e) {
 //				handleSelectionChanged();
 ////				updateButtons();
 //			}});
 		
 		pkgCfgViewer.addCheckStateListener(new PkgListener());
 //		
 		pkgCfgViewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				TableItem itm = tbl.getSelection()[0];
 				if (itm.getChecked()) {
 					itm.setChecked(false);
 				} else {
 					itm.setChecked(true);
 				}
 				handleCheckStateChange();
 			}
 		});
 		
 		//buttons
 		Composite compositeButtons = new Composite(c1, SWT.NONE);
 		initButtons(compositeButtons, BUTTONS);
 		
//		Composite c = new Composite(sashForm, SWT.NONE);
//		c.setLayoutData(new GridData(GridData.END));

 //		comp = new Composite(sashForm, SWT.NULL);
 //		GridData gd = new GridData();
 //
 //		gd.horizontalAlignment = GridData.FILL;
 //		gd.grabExcessHorizontalSpace = true;
 //		gd.grabExcessVerticalSpace = true;
 //		gd.horizontalSpan = 2;
 //		comp.setLayoutData(gd);
 
 //		sashForm.setWeights(new int[] {100, 100});
 	}
 	
 	/**
 	 * Get checked items.
 	 * @return
 	 */
 	private Object[] getCheckedItems() {
 		return pkgCfgViewer.getCheckedElements();
 	}
 	
 	/**
 	 * Add or remove include paths, library paths and libraries of the checked package.
 	 */
 	private void handleAddRemove() {
 		IProject proj = page.getProject();
 		if (newItemToggle) { //add
 			//handle include paths
 			String incPaths = PkgConfigUtil.pkgOutputCflags(newChecked.toString());
 			String[] incPathArray = Parser.parseIncPaths(incPaths);
 			for (String inc : incPathArray) {
 				PathToToolOption.addIncludePath(inc, proj);
 			}
 			//handle library paths
 			String libPaths = PkgConfigUtil.pkgOutputLibPathsOnly(newChecked.toString());
 			String[] libPathArray = Parser.parseLibPaths2(libPaths);
 			for (String libPath : libPathArray) {
 				PathToToolOption.addLibraryPath(libPath, proj);
 			}
 			//handle libraries
 			String libs = PkgConfigUtil.pkgOutputLibFilesOnly(newChecked.toString());
 			String[] libArray = Parser.parseLibs2(libs);
 			for (String lib : libArray) {
 				PathToToolOption.addLib(lib, proj);
 			}
 		} else { //remove
 			//handle include paths
 			String incPaths = PkgConfigUtil.pkgOutputCflags(removedItem.toString());
 			String[] incPathArray = Parser.parseIncPaths(incPaths);
 			for (String inc : incPathArray) {
 				PathToToolOption.removeIncludePath(inc, proj);
 			}
 			//handle library paths
 			String libPaths = PkgConfigUtil.pkgOutputLibPathsOnly(removedItem.toString());
 			String[] libPathArray = Parser.parseLibPaths2(libPaths);
 			for (String libPath : libPathArray) {
 				PathToToolOption.removeLibraryPath(libPath, proj);
 			}
 			//handle libraries
 			String libs = PkgConfigUtil.pkgOutputLibFilesOnly(removedItem.toString());
 			String[] libArray = Parser.parseLibs2(libs);
 			for (String lib : libArray) {
 				PathToToolOption.removeLib(lib, proj);
 			}
 		}
 	}
 	
 	/**
 	 * Add or remove include paths, library paths and libraries of the checked package.
 	 */
 	private void handleAddRemoveAllPackages() {
 		IProject proj = page.getProject();
 		Object[] checkedPkgs = getCheckedItems();
 		for (Object o : checkedPkgs) {
 			if (newItemToggle) { //add
 				//handle include paths
 				String incPaths = PkgConfigUtil.pkgOutputCflags(o.toString());
 				String[] incPathArray = Parser.parseIncPaths(incPaths);
 				for (String inc : incPathArray) {
 					PathToToolOption.addIncludePath(inc, proj);
 				}
 				//handle library paths
 				String libPaths = PkgConfigUtil.pkgOutputLibPathsOnly(o.toString());
 				String[] libPathArray = Parser.parseLibPaths2(libPaths);
 				for (String libPath : libPathArray) {
 					PathToToolOption.addLibraryPath(libPath, proj);
 				}
 				//handle libraries
 				String libs = PkgConfigUtil.pkgOutputLibFilesOnly(o.toString());
 				String[] libArray = Parser.parseLibs2(libs);
 				for (String lib : libArray) {
 					PathToToolOption.addLib(lib, proj);
 				}
 			} else { //remove
 				//handle include paths
 				String incPaths = PkgConfigUtil.pkgOutputCflags(o.toString());
 				String[] incPathArray = Parser.parseIncPaths(incPaths);
 				for (String inc : incPathArray) {
 					PathToToolOption.removeIncludePath(inc, proj);
 				}
 				//handle library paths
 				String libPaths = PkgConfigUtil.pkgOutputLibPathsOnly(o.toString());
 				String[] libPathArray = Parser.parseLibPaths2(libPaths);
 				for (String libPath : libPathArray) {
 					PathToToolOption.removeLibraryPath(libPath, proj);
 				}
 				//handle libraries
 				String libs = PkgConfigUtil.pkgOutputLibFilesOnly(o.toString());
 				String[] libArray = Parser.parseLibs2(libs);
 				for (String lib : libArray) {
 					PathToToolOption.removeLib(lib, proj);
 				}
 			}
 		}
 	}
 	
 //	private void saveChecked() { 
 		//TODO: Find out how to save the state of checked checkboxes
 		//TODO: Save to .cproject
 //	}
 	
 	@Override
 	protected void performApply(ICResourceDescription src,
 			ICResourceDescription dst) {
 	}
 
 	@Override
 	protected void performDefaults() {
 		//uncheck every checkbox
 		Object[] elements = {};
 		pkgCfgViewer.setCheckedElements(elements);
 	}
 
 	@Override
 	protected void updateData(ICResourceDescription cfg) {
 	}
 
 	@Override
 	protected void updateButtons() {
 	}
 	
 	public IProject getProject() {
 		IProject proj = page.getProject();
 		return proj;
 	}
 	
 	public IProject getSelectedProject() {
 		IEditorPart ed = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if(ed  != null) {
 		    IFileEditorInput input = (IFileEditorInput)ed.getEditorInput() ;
 		    IFile file = input.getFile();
 		    IProject activeProject = file.getProject();
 		    return activeProject;
 		}
 		return null;
 	}
 	
 	/**
 	 * Check state listener for the table viwer.
 	 *
 	 */
 	public class PkgListener implements ICheckStateListener {
 
 		@Override
 		public void checkStateChanged(CheckStateChangedEvent e) {
 			handleCheckStateChange();
 		}
 	}
 	
 	protected void handleCheckStateChange() {
 //		saveChecked(); //TODO: uncomment when implemented
 		newItemToggle = false;
 		//get checked items
 		Object[] checkedItems = getCheckedItems();
 
 		//check for added item
 		if (checkedItems.length > checkedSet.size()) {
 			//compare to old set
 			for (Object o : checkedItems) {
 				if (!checkedSet.contains(o)) {
 					newChecked = o;
 					checkedSet.add(o); //populate set with new item
 					newItemToggle = true;
 				}
 			}
 		}
 
 		//check for removed item
 		if (checkedItems.length < checkedSet.size()) {
 			List<Object> list = Arrays.asList(checkedItems);
 			Set<Object> newSet = new HashSet<Object>(list);
 			Object[] oldCheckedItems = checkedSet.toArray();
 			for (Object o : oldCheckedItems) {
 				if(!newSet.contains(o)) {
 					removedItem = o;
 					newItemToggle = false;
 				}
 			}
 		}
 
 		handleAddRemove();
 	}
 	
 	/**
 	 * Creates table columns, headers and sets the size of the columns.
 	 * 
 	 * @param parent
 	 * @param viewer
 	 */
 	private void createColumns(final Composite parent, final TableViewer viewer) {
 		String[] titles = { "Packages", "Description" };
 		int[] bounds = { 200, 500 };
 
 		//first column is for the package
 		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0]);
 		col.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				DataModel dm = (DataModel) element;
 				return dm.getPackage();
 			}
 		});
 
 		//second column is for the description
 		col = createTableViewerColumn(titles[1], bounds[1]);
 		col.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				DataModel dm = (DataModel) element;
 				return dm.getDescription();
 			}
 		});
 	}
 
 	/**
 	 * Creates a column for the table viewer.
 	 * 
 	 * @param title
 	 * @param bound
 	 * @return
 	 */
 	private TableViewerColumn createTableViewerColumn(String title, int bound) {
 
 		final TableViewerColumn viewerColumn = new TableViewerColumn(pkgCfgViewer,
 				SWT.NONE);
 		final TableColumn column = viewerColumn.getColumn();
 
 		column.setText(title);
 		column.setWidth(bound);
 		column.setResizable(true);
 
 		return viewerColumn;
 	}
 	
 	/**
 	 * Get selected item(s).
 	 * Only needed if multiselection with select/deselect button is implemented.
 	 * 
 	 * @return
 	 */
 	private TableItem[] getSelected() {
 		TableItem[] selected = pkgCfgViewer.getTable().getSelection();
 		return selected;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#buttonPressed(int)
 	 */
 	@Override
 	public void buttonPressed (int n) {
 		switch (n) {
 		case BUTTON_SELECT:
 			selectedButtonPressed();
 			break;
 		case BUTTON_DESELECT:
 			deselectedButtonPressed();
 			break;
 		default:
 			break;
 		}
 		updateButtons();
 	}
 	
 	private void selectedButtonPressed() {
 		TableItem[] selected = getSelected();
 		if (selected.length>0) {
 			newItemToggle = true;
 		}
 		for (TableItem itm : selected) {
 			itm.setChecked(true);
 		}
 		handleAddRemoveAllPackages();
 	}
 	
 	private void deselectedButtonPressed() {
 		TableItem[] selected = getSelected();
 		if (selected.length>0) {
 			newItemToggle = false;
 		}
 		for (TableItem itm : selected) {
 			itm.setChecked(false);
 		}
 		handleAddRemoveAllPackages();
 	}
 	
 }
