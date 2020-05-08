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
 package org.jboss.tools.jst.web.ui.internal.preferences;
 
 import java.util.ResourceBundle;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.*;
 
 import org.jboss.tools.jst.web.project.helpers.LibrarySet;
 import org.jboss.tools.jst.web.project.helpers.LibrarySets;
 
 public class LibrarySetsPreferencePage extends PreferencePage implements
 		IWorkbenchPreferencePage {
	public static final String ID = "org.jboss.tools.common.xstudio.libsets";
 	public static final String BUNDLE_NAME = "preferences"; 
 	public static final ResourceBundle BUNDLE = ResourceBundle.getBundle(LibrarySetsPreferencePage.class.getPackage().getName() + "." + BUNDLE_NAME); 
 	String[] librarySets;
 	LibrarySets helper;
 	LibrarySet library;
 	String[] jars;
 	Button addButtonLibJar,removeButtonLibJar,removeButtonLib;
 	List listLibJar;
 	public void init(IWorkbench workbench) {
 		helper = LibrarySets.getInstance();
 		librarySets = helper.getLibrarySetList();
 	}
 
 	protected Control createContents(Composite parent) {
 		noDefaultAndApplyButton();
 
 		Composite entryLib = new Composite(parent, SWT.NULL);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 		data.grabExcessHorizontalSpace = true;
 		entryLib.setLayoutData(data);
 
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		entryLib.setLayout(layout);
 		int heightHint =convertVerticalDLUsToPixels(14/*IDialogConstants.BUTTON_HEIGHT*/);
 		int widthHint =convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
 
 		// listLib
 		final List listLib = new List(entryLib, SWT.SINGLE | SWT.BORDER
 				| SWT.V_SCROLL);
 		listLib.setItems(librarySets);
 		GridData gridDataList = new GridData(GridData.FILL_BOTH);
 		gridDataList.widthHint = 270;//Minimum width for the column.
 		gridDataList.horizontalSpan = 1;
 		gridDataList.verticalSpan = 2;
 
 		int listHeight = listLib.getItemHeight() * 7;
 		Rectangle trim = listLib.computeTrim(0, 0, listHeight * 35, listHeight);
 		gridDataList.heightHint = trim.height;
 		listLib.setLayoutData(gridDataList);
 		
 		Button addButtonLib = new Button(entryLib, SWT.PUSH);
 		addButtonLib.setText(BUNDLE.getString("LibrarySetsPreferencePage.0"));
 		//addButtonLib.setText("Add");
 		GridData gridDataAddButton = new GridData(GridData.BEGINNING);
 		gridDataAddButton.widthHint = widthHint;
 		gridDataAddButton.heightHint=heightHint;
 		addButtonLib.setLayoutData(gridDataAddButton);
 
 	    removeButtonLib = new Button(entryLib, SWT.PUSH);
 		removeButtonLib.setText(BUNDLE.getString("LibrarySetsPreferencePage.1"));
 		GridData gridDataRemoveButton = new GridData(GridData.BEGINNING);
 		gridDataRemoveButton.widthHint = widthHint;
 		gridDataRemoveButton.heightHint = heightHint;
 		gridDataRemoveButton.verticalAlignment = GridData.BEGINNING;
 		removeButtonLib.setLayoutData(gridDataRemoveButton);
 		removeButtonLib.setEnabled(false);
 		
 		Label jarsIncludedLabel = new Label(entryLib, SWT.NONE);
 		jarsIncludedLabel.setText(BUNDLE.getString("LibrarySetsPreferencePage.jarsIncluded"));
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		data.horizontalSpan = 2;
 		jarsIncludedLabel.setLayoutData(data);
 
 	
 		listLibJar = new List(entryLib, SWT.SINGLE | SWT.BORDER| SWT.V_SCROLL);
 		GridData gridDatalistLibJar = new GridData(GridData.FILL_BOTH);
 		gridDatalistLibJar.widthHint = 270;//Minimum width for the column.
 		gridDatalistLibJar.horizontalSpan = 1;
 		gridDatalistLibJar.verticalSpan = 2;
 		listHeight = listLibJar.getItemHeight() * 7;
 		trim = listLibJar.computeTrim(0, 0, listHeight * 35, listHeight);
 		gridDatalistLibJar.heightHint = trim.height;
 		listLibJar.setLayoutData(gridDatalistLibJar);
 
 		addButtonLibJar = new Button(entryLib, SWT.PUSH);
 		addButtonLibJar.setText(BUNDLE.getString("LibrarySetsPreferencePage.0"));
 		GridData gridDataAddButtonLibJar = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gridDataAddButtonLibJar.widthHint = widthHint;
 		gridDataAddButtonLibJar.heightHint = heightHint;
 		addButtonLibJar.setLayoutData(gridDataAddButtonLibJar);		
 		addButtonLibJar.setEnabled(false);
 		
 		removeButtonLibJar = new Button(entryLib, SWT.PUSH);
 		removeButtonLibJar.setText(BUNDLE.getString("LibrarySetsPreferencePage.1"));
 		GridData gridDataRemoveButtonLibJar = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gridDataRemoveButtonLibJar.widthHint = widthHint;
 		gridDataRemoveButtonLibJar.heightHint = heightHint;
 		gridDataRemoveButtonLibJar.verticalAlignment = GridData.BEGINNING;
 		removeButtonLibJar.setLayoutData(gridDataRemoveButtonLibJar);
 		removeButtonLibJar.setEnabled(false);
 	
 		// Listeners
 		listLibJar.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {				
 				if(!addButtonLibJar.getEnabled())
 				 addButtonLibJar.setEnabled(true);
 				if(!removeButtonLibJar.getEnabled())
 					removeButtonLibJar.setEnabled(true);
 			}
 
 		});		
 		addButtonLib.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				boolean cancel=false;
 				String nameNewItem = helper.addLibrarySet();
 				librarySets = helper.getLibrarySetList();
 				listLib.setItems(librarySets);
 				if(nameNewItem != null) {
 					int ind=listLib.indexOf(nameNewItem);
 					listLib.setSelection(ind);
 					onLibrarySelect(nameNewItem);
 					cancel = false;
 				} else {
 					cancel = true;
 					addButtonLibJar.setEnabled(false);
 					removeButtonLib.setEnabled(false);
 				}
 				listLibJar.setItems(new String []{});
 				if(!cancel && (!addButtonLibJar.getEnabled())) {
 					addButtonLibJar.setEnabled(true);
 					removeButtonLib.setEnabled(true);
 				}
 			}
 		});
 		
 		removeButtonLib.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				int indexDel = listLib.getSelectionIndex();
 				if (indexDel < 0) return;
 				String nameLib = listLib.getItem(indexDel);					
 				boolean b = helper.removeLibrarySet(nameLib);
 				if(!b) return;
 				librarySets = helper.getLibrarySetList();
 				listLib.setItems(librarySets);
 				while(librarySets.length <= indexDel) indexDel--;
 				if(indexDel >= 0) {
 					listLib.setSelection(indexDel);
 					String n = listLib.getItem(indexDel);
 					if(n != null) onLibrarySelect(n);
 				} else {
 					listLibJar.setItems(new String []{});
 					removeButtonLib.setEnabled(false);
 					addButtonLibJar.setEnabled(false);
 					removeButtonLibJar.setEnabled(false);
 				}
 			}
 		});
 
 		listLib.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				int indexDel = listLib.getSelectionIndex();
 				String nameLib = listLib.getItem(indexDel);
 				onLibrarySelect(nameLib);
 			}
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 
 		});		
 
 		addButtonLibJar.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				String[] ls = openJarFiles();
 				if(ls == null || ls.length == 0) return;
 				String nameJar = null;
 				for (int i = 0; i < ls.length; i++) {
 					String n = library.addJar(ls[i]);
 					if(nameJar == null & n != null) nameJar = n;
 				}
 				jars = library.getJarList();
 				listLibJar.setItems(jars);
 				if(nameJar != null) {
 					int ind = listLibJar.indexOf(nameJar);
 					listLibJar.setSelection(ind);
 					removeButtonLibJar.setEnabled(true);				} else {
 					removeButtonLibJar.setEnabled(false);
 				}
 			}
 		});
 		
 		
 		removeButtonLibJar.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent event) {
 				int indexDel = listLibJar.getSelectionIndex();
 				if (indexDel < 0) return;
 				String nameLibJar = listLibJar.getItem(indexDel);
 				if(nameLibJar == null) return;
 				boolean b = library.removeJar(nameLibJar);
 				if(!b) return;
 				jars = library.getJarList();
 				listLibJar.setItems(jars);
 				while(jars.length <= indexDel) indexDel--;
 				addButtonLibJar.setEnabled(true);
 				if(indexDel >= 0) {
 					listLibJar.setSelection(indexDel);
 					removeButtonLibJar.setEnabled(true);
 				} else {
 					removeButtonLibJar.setEnabled(false);
 				}
 			}
 		});
 		if(librarySets != null && librarySets.length > 0) {
 			listLib.setSelection(0);
 			onLibrarySelect(librarySets[0]);
 		}			
 		return entryLib;
 
 	}
 	
 	void onLibrarySelect(String name) {
 		library = helper.getLibrarySet(name);				
 		jars = library.getJarList();
 		if(listLibJar != null && !listLibJar.isDisposed()) {
 			listLibJar.setItems(jars);
 		}
 		addButtonLibJar.setEnabled(true);
 		removeButtonLibJar.setEnabled(false);
 		removeButtonLib.setEnabled(true);
 	}
 	
 	private String[] openJarFiles() {
 		if(addButtonLibJar == null || addButtonLibJar.isDisposed()) return new String[0];
 		FileDialog dialog = new FileDialog(addButtonLibJar.getShell(), SWT.OPEN | SWT.MULTI);
 		String[] extensions = new String[]{"*.jar"};
 //		dialog.setFilterPath(p.getAbsolutePath());
 //		dialog.setFileName(f.getName());
 		dialog.setFilterExtensions(extensions);
 		String result = dialog.open();
 		if(result == null) return new String[0];
 		String[] fns = dialog.getFileNames();
 		String filterPath = dialog.getFilterPath();
 		String[] rs = new String[fns.length];
 		for (int i = 0; i < rs.length; i++) {
 			rs[i] = (filterPath + "/" + fns[i]).replace('\\', '/');
 		}
 		return rs;
 	}
 
 }
 
