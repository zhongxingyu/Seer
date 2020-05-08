 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.ui.interpreters;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.dltk.internal.ui.util.SWTUtil;
 import org.eclipse.dltk.internal.ui.util.TableLayoutComposite;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallType;
 import org.eclipse.dltk.launching.InterpreterStandin;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.eclipse.dltk.ui.util.PixelConverter;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 
 /**
  * A composite that displays installed InterpreterEnvironment's in a table. InterpreterEnvironments can be 
  * added, removed, edited, and searched for.
  * <p>
  * This block implements ISelectionProvider - it sends selection change events
  * when the checked InterpreterEnvironment in the table changes, or when the "use default" button
  * check state changes.
  * </p>
  */
 public abstract class InterpretersBlock implements IAddInterpreterDialogRequestor, ISelectionProvider {
 	
 	/**
 	 * This block's control
 	 */
 	private Composite fControl;
 	
 	/**
 	 * Interpreters being displayed
 	 */
 	protected List fInterpreters = new ArrayList(); 
 	
 	/**
 	 * The main list control
 	 */ 
 	protected CheckboxTableViewer fInterpreterList;
 	
 	// Action buttons
 	private Button fAddButton;
 	private Button fRemoveButton;
 	private Button fEditButton;
 	private Button fCopyButton;
 	private Button fSearchButton;	
     
 	// index of column used for sorting
 	private int fSortColumn = 0;
 	
 	/**
 	 * Selection listeners (checked InterpreterEnvironment changes)
 	 */
 	private ListenerList fSelectionListeners = new ListenerList();
 	
 	/**
 	 * Previous selection
 	 */
 	private ISelection fPrevSelection = new StructuredSelection();
 
     private Table fTable;
 			
 		
 	
 	/** 
 	 * Content provider to show a list of InterpreterEnvironments
 	 */ 
 	class InterpretersContentProvider implements IStructuredContentProvider {		
 		public Object[] getElements(Object input) {
 			return fInterpreters.toArray();
 		}
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		}
 		public void dispose() {
 		}
 	}
 	
 	/**
 	 * Label provider for installed InterpreterEnvironments table.
 	 */
 	class InterpreterLabelProvider extends LabelProvider implements ITableLabelProvider {
 
 		/**
 		 * @see ITableLabelProvider#getColumnText(Object, int)
 		 */
 		public String getColumnText(Object element, int columnIndex) {
 			if (element instanceof IInterpreterInstall) {
 				IInterpreterInstall interp = (IInterpreterInstall)element;
 				switch(columnIndex) {
 					case 0:						
 						return interp.getName();
 					case 1:
 						return interp.getInterpreterInstallType().getName();	
 					case 2: 						
 						return interp.getInstallLocation().toString();
 				}
 			}
 			return element.toString();
 		}
 
 		/**
 		 * @see ITableLabelProvider#getColumnImage(Object, int)
 		 */
 		public Image getColumnImage(Object element, int columnIndex) {
 			if (columnIndex == 0) {
 				//TODO: instert interpreter logo here
 			}
 			return null;
 		}
 
 	}	
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		fSelectionListeners.add(listener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
 	 */
 	public ISelection getSelection() {
 		return new StructuredSelection(fInterpreterList.getCheckedElements());
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
 	 */
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 		fSelectionListeners.remove(listener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
 	 */
 	public void setSelection(ISelection selection) {
 		if (selection instanceof IStructuredSelection) {
 			if (!selection.equals(fPrevSelection)) {
 				fPrevSelection = selection;
 				Object interp = ((IStructuredSelection)selection).getFirstElement();
 				if (interp == null) {
 					fInterpreterList.setCheckedElements(new Object[0]);
 				} else {
 					fInterpreterList.setCheckedElements(new Object[]{interp});
 					fInterpreterList.reveal(interp);
 				}
 				fireSelectionChanged();
 			}
 		}
 	}
 
 	/**
 	 * Creates this block's control in the given control.
 	 * 
 	 * @param ancestor containing control
 	 * @param useManageButton whether to present a single 'manage...' button to
 	 *  the user that opens the installed InterpreterEnvironments pref page for InterpreterEnvironment management,
 	 *  or to provide 'add, remove, edit, and search' buttons.
 	 */
 	public void createControl(Composite ancestor) {
 		
 		Composite parent= new Composite(ancestor, SWT.NULL);
 		GridLayout layout= new GridLayout();
 		layout.numColumns= 2;
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		
 		Font font = ancestor.getFont();
 		parent.setFont(font);	
 		parent.setLayout(layout);
 		
 		fControl = parent;	
 		
 		GridData data;
 				
 		Label tableLabel = new Label(parent, SWT.NONE);
 		tableLabel.setText(InterpretersMessages.InstalledInterpretersBlock_15); 
 		data = new GridData();
 		data.horizontalSpan = 2;
 		tableLabel.setLayoutData(data);
 		tableLabel.setFont(font);
 		
 		
 		PixelConverter conv = new PixelConverter(parent);
 		data = new GridData(GridData.FILL_BOTH);
 		data.widthHint = conv.convertWidthInCharsToPixels(50);
 		TableLayoutComposite tblComposite = new TableLayoutComposite(parent, SWT.NONE);
 		tblComposite.setLayoutData(data);
 		fTable = new Table(tblComposite, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
 				
 		
 		
 		data= new GridData(GridData.FILL_BOTH);
         data.widthHint = 450;
 		fTable.setLayoutData(data);
 		fTable.setFont(font);
 				
 		fTable.setHeaderVisible(true);
 		fTable.setLinesVisible(true);		
 
 		TableColumn column1= new TableColumn(fTable, SWT.NULL);
 		column1.setText(InterpretersMessages.InstalledInterpretersBlock_0); 
 		column1.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				sortByName();
 			}
 		});
 	
 		TableColumn column2= new TableColumn(fTable, SWT.NULL);
 		column2.setText(InterpretersMessages.InstalledInterpretersBlock_2); 
 		column2.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {				
 				sortByType();
 			}
 		});
 		
 		TableColumn column3= new TableColumn(fTable, SWT.NULL);
 		column3.setText(InterpretersMessages.InstalledInterpretersBlock_1); 
 		column3.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				sortByLocation();
 			}
 		});
 
 		fInterpreterList= new CheckboxTableViewer(fTable);			
 		fInterpreterList.setLabelProvider(new InterpreterLabelProvider());
 		fInterpreterList.setContentProvider(new InterpretersContentProvider());
 		// by default, sort by name
 		sortByName();
 		
 		fInterpreterList.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent evt) {
 				enableButtons();
 			}
 		});
 		
 		fInterpreterList.addCheckStateListener(new ICheckStateListener() {
 			public void checkStateChanged(CheckStateChangedEvent event) {
 				if (event.getChecked()) {
 					setCheckedInterpreter((IInterpreterInstall)event.getElement());
 				} else {
 					setCheckedInterpreter(null);
 				}
 			}
 		});
 		
 		fInterpreterList.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent e) {
 				if (!fInterpreterList.getSelection().isEmpty()) {
 					editInterpreter();
 				}
 			}
 		});
 		fTable.addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent event) {
 				if (event.character == SWT.DEL && event.stateMask == 0) {
 					if (fRemoveButton.getEnabled())
 						removeInterpreters();
 				}
 			}
 		});			
 		
 		Composite buttons= new Composite(parent, SWT.NULL);
 		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
 		layout= new GridLayout();
 		layout.marginHeight= 0;
 		layout.marginWidth= 0;
 		buttons.setLayout(layout);
 		buttons.setFont(font);
 		
 		fAddButton = createPushButton(buttons, InterpretersMessages.InstalledInterpretersBlock_3); 
 		fAddButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event evt) {
 				addInterpreter();
 			}
 		});
 		
 		fEditButton= createPushButton(buttons, InterpretersMessages.InstalledInterpretersBlock_4); 
 		fEditButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event evt) {
 				editInterpreter();
 			}
 		});
 		
 		fCopyButton = createPushButton(buttons, InterpretersMessages.InstalledInterpretersBlock_16); 
 		fCopyButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event evt) {
 				copyInterpreter();
 			}
 		});
 		
 		fRemoveButton= createPushButton(buttons, InterpretersMessages.InstalledInterpretersBlock_5); 
 		fRemoveButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event evt) {
 				removeInterpreters();
 			}
 		});
 		
 		// copied from ListDialogField.CreateSeparator()
 		Label separator= new Label(buttons, SWT.NONE);
 		separator.setVisible(false);
 		GridData gd= new GridData();
 		gd.horizontalAlignment= GridData.FILL;
 		gd.verticalAlignment= GridData.BEGINNING;
 		gd.heightHint= 4;
 		separator.setLayoutData(gd);
 		
 		fSearchButton = createPushButton(buttons, InterpretersMessages.InstalledInterpretersBlock_6); 
 		fSearchButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event evt) {
 				search();
 			}
 		});		
 		
 		fillWithWorkspaceInterpreters();
 		enableButtons();
 		fAddButton.setEnabled(ScriptRuntime.getInterpreterInstallTypes(getCurrentNature()).length > 0);
 	}
 	
 	
 	
 	/**
 	 * Fire current selection
 	 */
 	private void fireSelectionChanged() {
 		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
 		Object[] listeners = fSelectionListeners.getListeners();
 		for (int i = 0; i < listeners.length; i++) {
 			ISelectionChangedListener listener = (ISelectionChangedListener)listeners[i];
 			listener.selectionChanged(event);
 		}	
 	}
 
 	
 	/**
 	 * Sorts by Interpreter type, and name within type.
 	 */
 	private void sortByType() {
 		fInterpreterList.setSorter(new ViewerSorter() {
 			public int compare(Viewer viewer, Object e1, Object e2) {
 				if ((e1 instanceof IInterpreterInstall) && (e2 instanceof IInterpreterInstall)) {
 					IInterpreterInstall left= (IInterpreterInstall)e1;
 					IInterpreterInstall right= (IInterpreterInstall)e2;
 					String leftType= left.getInterpreterInstallType().getName();
 					String rightType= right.getInterpreterInstallType().getName();
 					int res= leftType.compareToIgnoreCase(rightType);
 					if (res != 0) {
 						return res;
 					}
 					return left.getName().compareToIgnoreCase(right.getName());
 				}
 				return super.compare(viewer, e1, e2);
 			}
 			
 			public boolean isSorterProperty(Object element, String property) {
 				return true;
 			}
 		});	
 		fSortColumn = 3;			
 	}
 	
 	/**
 	 * Sorts by Interpreter name.
 	 */
 	private void sortByName() {
 		fInterpreterList.setSorter(new ViewerSorter() {
 			public int compare(Viewer viewer, Object e1, Object e2) {
 				if ((e1 instanceof IInterpreterInstall) && (e2 instanceof IInterpreterInstall)) {
 					IInterpreterInstall left= (IInterpreterInstall)e1;
 					IInterpreterInstall right= (IInterpreterInstall)e2;
 					return left.getName().compareToIgnoreCase(right.getName());
 				}
 				return super.compare(viewer, e1, e2);
 			}
 			
 			public boolean isSorterProperty(Object element, String property) {
 				return true;
 			}
 		});		
 		fSortColumn = 1;		
 	}
 	
 	/**
 	 * Sorts by Interpreter location.
 	 */
 	private void sortByLocation() {
 		fInterpreterList.setSorter(new ViewerSorter() {
 			public int compare(Viewer viewer, Object e1, Object e2) {
 				if ((e1 instanceof IInterpreterInstall) && (e2 instanceof IInterpreterInstall)) {
 					IInterpreterInstall left= (IInterpreterInstall)e1;
 					IInterpreterInstall right= (IInterpreterInstall)e2;
 					return left.getInstallLocation().toString().compareToIgnoreCase(right.getInstallLocation().toString());
 				}
 				return super.compare(viewer, e1, e2);
 			}
 			
 			public boolean isSorterProperty(Object element, String property) {
 				return true;
 			}
 		});		
 		fSortColumn = 2;		
 	}
 		
 	private void enableButtons() {
 		IStructuredSelection selection = (IStructuredSelection) fInterpreterList.getSelection();
 		int selectionCount= selection.size();
 		fEditButton.setEnabled(selectionCount == 1);
 		fCopyButton.setEnabled(selectionCount > 0);		
 		if (selectionCount > 0) {
 			Iterator iterator = selection.iterator();
 			while (iterator.hasNext()) {
 				IInterpreterInstall install = (IInterpreterInstall)iterator.next();
 				if (isContributed(install)) {
 					fRemoveButton.setEnabled(false);
 					return;
 				}
 			}
 			fRemoveButton.setEnabled(true);
 		} else {
 			fRemoveButton.setEnabled(false);
 		}
 	}	
 	
 	protected Button createPushButton(Composite parent, String label) {
 		return SWTUtil.createPushButton(parent, label, null);
 	}
 	
 	private boolean isContributed(IInterpreterInstall install) {
 		return ScriptRuntime.isContributedInterpreterInstall(install.getId());
 	}
 	
 	/**
 	 * Returns this block's control
 	 * 
 	 * @return control
 	 */
 	public Control getControl() {
 		return fControl;
 	}
 	
 	/**
 	 * Sets the InterpreterEnvironments to be displayed in this block
 	 * 
 	 * @param Interpreters InterpreterEnvironments to be displayed
 	 */
 	protected void setInterpreters(IInterpreterInstall[] Interpreters) {
 		fInterpreters.clear();
 		for (int i = 0; i < Interpreters.length; i++) {
 			fInterpreters.add(Interpreters[i]);
 		}
 		fInterpreterList.setInput(fInterpreters);
 		fInterpreterList.refresh();
 	}
 	
 	/**
 	 * Returns the InterpreterEnvironments currently being displayed in this block
 	 * 
 	 * @return InterpreterEnvironments currently being displayed in this block
 	 */
 	public IInterpreterInstall[] getInterpreters() {
 		return (IInterpreterInstall[])fInterpreters.toArray(new IInterpreterInstall[fInterpreters.size()]);
 	}
 	
 	
 	
 	/**
 	 * @see IAddInterpreterDialogRequestor#isDuplicateName(String)
 	 */
 	public boolean isDuplicateName(String name) {
 		for (int i= 0; i < fInterpreters.size(); i++) {
 			IInterpreterInstall Interpreter = (IInterpreterInstall)fInterpreters.get(i);
 			if (Interpreter.getName().equals(name)) {
 				return true;
 			}
 		}
 		return false;
 	}	
 		
 	private void removeInterpreters() {
 		IStructuredSelection selection= (IStructuredSelection)fInterpreterList.getSelection();
 		IInterpreterInstall[] Interpreters = new IInterpreterInstall[selection.size()];
 		Iterator iter = selection.iterator();
 		int i = 0;
 		while (iter.hasNext()) {
 			Interpreters[i] = (IInterpreterInstall)iter.next();
 			i++;
 		}
 		removeInterpreters(Interpreters);
 	}	
 	
 	/**
 	 * Removes the given Interpreters from the table.
 	 * 
 	 * @param Interpreters
 	 */
 	public void removeInterpreters(IInterpreterInstall[] Interpreters) {
 		IStructuredSelection prev = (IStructuredSelection) getSelection();
 		for (int i = 0; i < Interpreters.length; i++) {
 			fInterpreters.remove(Interpreters[i]);
 		}
 		fInterpreterList.refresh();
 		IStructuredSelection curr = (IStructuredSelection) getSelection();
 		if (!curr.equals(prev)) {
 			IInterpreterInstall[] installs = getInterpreters();
 			if (curr.size() == 0 && installs.length == 1) {
 				// pick a default Interpreter automatically
 				setSelection(new StructuredSelection(installs[0]));
 			} else {
 				fireSelectionChanged();
 			}
 		}
 	}
 	
 	/**
 	 * Search for installed interpreters in the file system
 	 */
 	protected void search() {
 
 		// choose a root directory for the search 
 		/*DirectoryDialog dialog = new DirectoryDialog(getShell());
 		dialog.setMessage(InterpretersMessages.InstalledInterpreterEnvironmentsBlock_9); 
 		dialog.setText(InterpretersMessages.InstalledInterpreterEnvironmentsBlock_10); 
 		String path = dialog.open();
 		if (path == null) {
 			return;
 		}*/
 		
 		// ignore installed locations
 		final Set exstingLocations = new HashSet();
 		Iterator iter = fInterpreters.iterator();
 		while (iter.hasNext()) {
 			exstingLocations.add(((IInterpreterInstall)iter.next()).getInstallLocation().getAbsoluteFile());
 		}
 		
 		// search
 		final List locations = new ArrayList();
 		final List types = new ArrayList();
 
 		IRunnableWithProgress r = new IRunnableWithProgress() {
 			public void run(IProgressMonitor monitor) {
 				monitor.beginTask(InterpretersMessages.InstalledInterpretersBlock_11, IProgressMonitor.UNKNOWN); 
 				searchFast(locations, types, exstingLocations, monitor);
 				monitor.done();
 			}
 		};
 		
 		try {
            ProgressMonitorDialog progress = new ProgressMonitorDialog(getShell()) {

				protected void createCancelButton(Composite parent) {
					super.createCancelButton(parent);
					cancel.setText(InterpretersMessages.InterpretersBlock_0);
				}
            	
            };
             progress.run(true, true, r);
 		} catch (InvocationTargetException e) {
 			DLTKDebugUIPlugin.log(e);
 		} catch (InterruptedException e) {
 			// cancelled
 			return;
 		}
 		
		if (locations.isEmpty()) { 
 			MessageDialog.openInformation(getShell(), InterpretersMessages.InstalledInterpretersBlock_12, InterpretersMessages.InstalledInterpretersBlock_113); //
 		} else {
 			iter = locations.iterator();
 			Iterator iter2 = types.iterator();
 			while (iter.hasNext()) {
 				File location = (File)iter.next();
 				IInterpreterInstallType type = (IInterpreterInstallType)iter2.next();			
 				IInterpreterInstall Interpreter = new InterpreterStandin(type, createUniqueId(type));
 				String name = location.getName();
 				String nameCopy = new String(name);
 				int i = 1;
 				while (isDuplicateName(nameCopy)) {
 					nameCopy = name + '(' + i++ + ')'; 
 				}
 				Interpreter.setName(nameCopy);
 				Interpreter.setInstallLocation(location);
 				interpreterAdded(Interpreter);
 			}
 		}
 	}
 	
 	
 	protected Shell getShell() {
 		return getControl().getShell();
 	}
 	
 	/**
 	 * Searches in PATH 
 	 * @param found
 	 * @param types
 	 * @param ignore
 	 * @param monitor
 	 */
 	protected void searchFast (List found, List types, Set ignore, IProgressMonitor monitor) {
 		if (monitor.isCanceled()) {
 			return;
 		}
 		// get system environment
 		Map systemEnv = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironmentCasePreserved();
 		String pName = null;
 		Set varNames = systemEnv.keySet();
 		for (Iterator iter = varNames.iterator(); iter.hasNext();) {
 			String var = (String) iter.next();
			if (var.compareToIgnoreCase("path") == 0) { //$NON-NLS-1$
 				pName = var;
 				break;
 			}
 		}
 		if (pName == null)
 			return;
 		String path = (String)systemEnv.get(pName);
 		
 		ArrayList folders = new ArrayList ();
 		
 		//split path
 		if (Platform.getOS().equals(Platform.OS_WIN32)) { 
			String[] res = path.split(";"); //$NON-NLS-1$
 			for (int i = 0; i < res.length; i++) {
 				folders.add(Path.fromOSString(res[i]));
 			}
 		} else {
			String[] res = path.split(":"); //$NON-NLS-1$
 			for (int i = 0; i < res.length; i++) {
 				folders.add(Path.fromOSString(res[i]));
 			}
 		}
 		
 		for (Iterator iter = folders.iterator(); iter.hasNext();) {
 			Path folder = (Path) iter.next();
 			if (folder != null) {
 				File f = folder.toFile();
 				if (f !=null && f.isDirectory())
 					search(f, found, types, ignore, monitor, 1);
 			}
 			
 		}
 		
 	}
 	
 	/**
 	 * Searches the specified directory recursively for installed Interpreters, adding each
 	 * detected Interpreter to the <code>found</code> list. Any directories specified in
 	 * the <code>ignore</code> are not traversed.
 	 * 
 	 * @param directory
 	 * @param found
 	 * @param types
 	 * @param ignore
 	 * @param deep deepness of search. -1 if infinite.
 	 */
 	protected void search(File directory, List found, List types, Set ignore, IProgressMonitor monitor, int deep) {
 		if (deep == 0)
 			return;
 		if (monitor.isCanceled()) {
 			return;
 		}
 
 		String[] names = directory.list();
 		if (names == null) {
 			return;
 		}
 		List subDirs = new ArrayList();
 		for (int i = 0; i < names.length; i++) {
 			if (monitor.isCanceled()) {
 				return;
 			}
 			File file = new File(directory, names[i]);
 						
 			try {
 				monitor.subTask(MessageFormat.format(InterpretersMessages.InstalledInterpretersBlock_14, new String[]{Integer.toString(found.size()), file.getCanonicalPath()}));
 				//	check if file is a symlink
 				if (file.isDirectory() && (!file.getCanonicalPath().equals(file.getAbsolutePath())))
 					continue;
 			} catch (IOException e) {
 			}		
 			IInterpreterInstallType[] InterpreterTypes = ScriptRuntime.getInterpreterInstallTypes(getCurrentNature());	
 
 			if (!ignore.contains(file)) {
 				boolean validLocation = false;
 				// Take the first Interpreter install type that claims the location as a
 				// valid Interpreter install.  Interpreter install types should be smart enough to not
 				// claim another type's Interpreter, but just in case...
 				for (int j = 0; j < InterpreterTypes.length; j++) {
 					if (monitor.isCanceled()) {
 						return;
 					}
 					IInterpreterInstallType type = InterpreterTypes[j];					
 					IStatus status = type.validateInstallLocation(file);
 					if (status.isOK()) {
 						found.add(file);
 						types.add(type);
 						validLocation = true;
 						break;
 					}
 				}
 				if (file.isDirectory() && !validLocation) {
 					subDirs.add(file);
 				} 
 			}
 			
 		}
 		while (!subDirs.isEmpty()) {
 			File subDir = (File)subDirs.remove(0);
 			search(subDir, found, types, ignore, monitor, deep - 1);
 			if (monitor.isCanceled()) {
 				return;
 			}
 		}
 		
 	}	
 	
 	/**
 	 * Sets the checked InterpreterEnvironment, possible <code>null</code>
 	 * 
 	 * @param Interpreter InterpreterEnvironment or <code>null</code>
 	 */
 	public void setCheckedInterpreter(IInterpreterInstall Interpreter) {
 		if (Interpreter == null) {
 			setSelection(new StructuredSelection());
 		} else {
 			setSelection(new StructuredSelection(Interpreter));
 		}
 	}
 	
 	/**
 	 * Returns the checked Interpreter or <code>null</code> if none.
 	 * 
 	 * @return the checked Interpreter or <code>null</code> if none
 	 */
 	public IInterpreterInstall getCheckedInterpreter() {
 		Object[] objects = fInterpreterList.getCheckedElements();
 		if (objects.length == 0) {
 			return null;
 		}
 		return (IInterpreterInstall)objects[0];
 	}
 	
 	/**
 	 * Persist table settings into the give dialog store, prefixed
 	 * with the given key.
 	 * 
 	 * @param settings dialog store
 	 * @param qualifier key qualifier
 	 */
 	public void saveColumnSettings(IDialogSettings settings, String qualifier) {
         int columnCount = fTable.getColumnCount();
 		for (int i = 0; i < columnCount; i++) {
 			settings.put(qualifier + ".columnWidth" + i, fTable.getColumn(i).getWidth());	 //$NON-NLS-1$
 		}
 		settings.put(qualifier + ".sortColumn", fSortColumn); //$NON-NLS-1$
 	}
 	
 	/**
 	 * Restore table settings from the given dialog store using the
 	 * given key.
 	 * 
 	 * @param settings dialog settings store
 	 * @param qualifier key to restore settings from
 	 */
 	public void restoreColumnSettings(IDialogSettings settings, String qualifier) {
 		fInterpreterList.getTable().layout(true);
         restoreColumnWidths(settings, qualifier);
 		try {
 			fSortColumn = settings.getInt(qualifier + ".sortColumn"); //$NON-NLS-1$
 		} catch (NumberFormatException e) {
 			fSortColumn = 1;
 		}
 		switch (fSortColumn) {
 			case 1:
 				sortByName();
 				break;
 			case 2:
 				sortByLocation();
 				break;
 			case 3:
 				sortByType();
 				break;
 		}
 	}
 	
 	private void restoreColumnWidths(IDialogSettings settings, String qualifier) {
         int columnCount = fTable.getColumnCount();
         for (int i = 0; i < columnCount; i++) {
             int width = -1;
             
             try {
                 width = settings.getInt(qualifier + ".columnWidth" + i); //$NON-NLS-1$
             } catch (NumberFormatException e) {}
             
             if (width <= 0) {
                 fTable.getColumn(i).pack();
             } else {
                 fTable.getColumn(i).setWidth(width);
             }
         }
 	}
 		
 	/**
 	 * Populates the InterpreterEnvironment table with existing InterpreterEnvironments defined in the workspace.
 	 */
 	protected void fillWithWorkspaceInterpreters() {
 		//		 fill with interpreters
 		List standins = new ArrayList();
 		IInterpreterInstallType[] types = ScriptRuntime.getInterpreterInstallTypes(getCurrentNature());
 		for (int i = 0; i < types.length; i++) {
 			IInterpreterInstallType type = types[i];
 			IInterpreterInstall[] installs = type.getInterpreterInstalls();
 			if (installs != null)
 				for (int j = 0; j < installs.length; j++) {
 					IInterpreterInstall install = installs[j];
 					standins.add(new InterpreterStandin(install));
 				}
 		}
 		setInterpreters((IInterpreterInstall[])standins.toArray(new IInterpreterInstall[standins.size()]));	
 	}
 	public void interpreterAdded(IInterpreterInstall Interpreter) {
 		fInterpreters.add(Interpreter);
 		fInterpreterList.refresh();
 				
 		IInterpreterInstall[] installs = getInterpreters();
 		if (installs.length == 1) {
 			// pick a default Interpreter automatically
 			setSelection(new StructuredSelection(installs[0]));
 		} 
 		fireSelectionChanged();				
 	}
 	
 	//	 Make sure that InterpreterStandin ids are unique if multiple calls to System.currentTimeMillis()
 	// happen very quickly
 	private static String fgLastUsedID;
 	
 	/**
 	 * Find a unique Interpreter id.  Check existing 'real' Interpreters, as well as the last id used for
 	 * a InterpreterStandin.
 	 */
 	protected String createUniqueId(IInterpreterInstallType InterpreterType) {
 		String id= null;
 		do {
 			id= String.valueOf(System.currentTimeMillis());
 		} while (InterpreterType.findInterpreterInstall(id) != null || id.equals(fgLastUsedID));
 		fgLastUsedID = id;
 		return id;
 	}	
 	
 	/**
 	 * Compares the given name against current names and adds the appropriate numerical 
 	 * suffix to ensure that it is unique.
 	 * @param name the name with which to ensure uniqueness 
 	 * @return the unique version of the given name
 	 *
 	 */
 	protected String generateName(String name){
             if (!isDuplicateName(name)) {
                 return name;
             }
             
             if (name.matches(".*\\(\\d*\\)")) { //$NON-NLS-1$
                 int start = name.lastIndexOf('(');
                 int end = name.lastIndexOf(')');
                 String stringInt = name.substring(start+1, end);
                 int numericValue = Integer.parseInt(stringInt);
                 String newName = name.substring(0, start+1) + (numericValue+1) + ")"; //$NON-NLS-1$
                 return generateName(newName);
             } else {
                 return generateName(name + " (1)"); //$NON-NLS-1$
             }
         }
 	
 	abstract protected String getCurrentNature();
 
 	protected abstract AddScriptInterpreterDialog createInterpreterDialog(IInterpreterInstall standin);
 
 	protected void copyInterpreter() {
 	    IStructuredSelection selection = (IStructuredSelection) fInterpreterList.getSelection();
 	    Iterator it = selection.iterator();
 	
 	    ArrayList newEntries = new ArrayList();
 	    while (it.hasNext()) {
 	        IInterpreterInstall selectedInterpreter = (IInterpreterInstall) it.next();
 	
 	        // duplicate & add Interpreter
 	        InterpreterStandin standin = new InterpreterStandin(selectedInterpreter, createUniqueId(selectedInterpreter.getInterpreterInstallType()));
 	        standin.setName(generateName(selectedInterpreter.getName()));            
 	        AddScriptInterpreterDialog dialog = createInterpreterDialog(standin);
 	        dialog.setTitle(InterpretersMessages.InstalledInterpretersBlock_18);
 	        if (dialog.open() != Window.OK) {
 	            return;
 	        }
 	        newEntries.add(standin);
 	        fInterpreters.add(standin);
 	    }
 	    fInterpreterList.refresh();
 	    fInterpreterList.setSelection(new StructuredSelection(newEntries.toArray()));
 	}
 
 	/**
 	 * Bring up a dialog that lets the user create a new Interpreter definition.
 	 */
 	protected void addInterpreter() {
 		AddScriptInterpreterDialog dialog= createInterpreterDialog(null);
 		dialog.setTitle(InterpretersMessages.InstalledInterpretersBlock_7); 
 		if (dialog.open() != Window.OK) {
 			return;
 		}
 		fInterpreterList.refresh();		
 	}
 
 	protected void editInterpreter() {
 		IStructuredSelection selection= (IStructuredSelection)fInterpreterList.getSelection();
 		IInterpreterInstall Interpreter= (IInterpreterInstall)selection.getFirstElement();
 		if (Interpreter == null) {
 			return;
 		}
 	
 		AddScriptInterpreterDialog dialog= createInterpreterDialog(Interpreter);
 		dialog.setTitle(InterpretersMessages.InstalledInterpretersBlock_8); 
 		if (dialog.open() != Window.OK) {
 			return;
 		}
 		fInterpreterList.refresh(Interpreter);
 		
 	}	
 }
