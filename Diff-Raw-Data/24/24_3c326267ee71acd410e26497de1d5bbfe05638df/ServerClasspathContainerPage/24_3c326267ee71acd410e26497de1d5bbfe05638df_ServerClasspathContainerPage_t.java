 /*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.ui.internal;
 
 import java.util.HashMap;
import java.util.Iterator;
 import java.util.Map;
import java.util.Set;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.jst.server.core.internal.JavaServerPlugin;
 import org.eclipse.jst.server.core.internal.RuntimeClasspathContainer;
 import org.eclipse.jst.server.core.internal.RuntimeClasspathProviderWrapper;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.ui.internal.viewers.RuntimeTableLabelProvider;
 
 public class ServerClasspathContainerPage extends WizardPage implements IClasspathContainerPage {
 	protected IClasspathEntry selection;
 
 	protected Map<IRuntime, IClasspathEntry> runtimeMap = new HashMap<IRuntime, IClasspathEntry>();
 
 	public ServerClasspathContainerPage() {
 		super("server.container");
 		setImageDescriptor(ImageResource.getImageDescriptor(ImageResource.IMG_WIZ_RUNTIME_TYPE));
 		setTitle(Messages.classpathContainer);
 		setDescription(Messages.classpathContainerDescription);
 		setPageComplete(false);
 		
 		IRuntime[] runtimes = ServerCore.getRuntimes();
 		for (IRuntime runtime : runtimes) {
 			if (runtime.getRuntimeType() != null) {
 				RuntimeClasspathProviderWrapper rcpw = JavaServerPlugin.findRuntimeClasspathProvider(runtime.getRuntimeType());
 				if (rcpw != null) {
 					IPath serverContainerPath = new Path(RuntimeClasspathContainer.SERVER_CONTAINER)
 							.append(rcpw.getId()).append(runtime.getId());
 					runtimeMap.put(runtime, JavaCore.newContainerEntry(serverContainerPath));
 				}
 			}
 		}
 	}
 
 	public boolean finish() {
 		return true;
 	}
 
 	public IClasspathEntry getSelection() {
 		return selection;
 	}
 
 	public void setSelection(IClasspathEntry containerEntry) {
 		selection = containerEntry;
 	}
 
 	public void createControl(Composite parent) {
 		Composite comp = new Composite(parent, SWT.NONE);
 		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
 		
 		GridLayout layout = new GridLayout();
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		comp.setLayout(layout);
 		
 		Label label = new Label(comp, SWT.NONE);
 		label.setText(Messages.classpathContainerRuntimeList);
 		
 		Table table = new Table(comp, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
 		table.setLayoutData(new GridData(GridData.FILL_BOTH));
 		
 		table.setHeaderVisible(false);
 		
 		TableViewer tableViewer = new TableViewer(table);
 		tableViewer.setContentProvider(new IStructuredContentProvider() {
 			public Object[] getElements(Object inputElement) {
 				Object[] obj = runtimeMap.keySet().toArray(); 
 				return obj;
 			}
 
 			public void dispose() {
 				// ignore
 			}
 
 			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 				// ignore
 			}
 		});
 		tableViewer.setLabelProvider(new RuntimeTableLabelProvider());
 		tableViewer.setInput("root");

		if( selection != null ) {
			Set<IRuntime> set = runtimeMap.keySet();
			IRuntime rt;
			for( Iterator<IRuntime> i = set.iterator(); i.hasNext();) {
				rt = i.next();
				if( runtimeMap.get(rt).getPath().equals(selection.getPath())) {
					tableViewer.setSelection(new StructuredSelection(new Object[]{rt}));
					setPageComplete(true);
					break;
				}
			}
		}

 		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				try {
 					IStructuredSelection sel = (IStructuredSelection) event.getSelection();
 					selection = runtimeMap.get(sel.getFirstElement());
 					setPageComplete(true);
 				} catch (Exception e) {
 					selection = null;
 					setPageComplete(false);
 				}
 			}
 		});
 		
 		if (tableViewer.getTable().getItemCount() != 0)
 			tableViewer.getTable().setFocus();
 		
 		setControl(comp);
 	}
 }
