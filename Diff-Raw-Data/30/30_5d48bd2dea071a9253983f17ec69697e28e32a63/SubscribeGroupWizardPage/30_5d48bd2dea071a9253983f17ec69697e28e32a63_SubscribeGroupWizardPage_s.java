 /*******************************************************************************
  *  Copyright (c) 2010 Weltevree Beheer BV, Remain Software & Industrial-TSI
  *                                                                      
  * All rights reserved. This program and the accompanying materials     
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at             
  * http://www.eclipse.org/legal/epl-v10.html                            
  *                                                                      
  * Contributors:                                                        
  *    Wim Jongman - initial API and implementation
  *******************************************************************************/
 package org.eclipse.ecf.salvo.ui.internal.wizards;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.eclipse.ecf.protocol.nntp.model.INewsgroup;
 import org.eclipse.ecf.protocol.nntp.model.IServer;
 import org.eclipse.ecf.salvo.ui.internal.provider.NewsContentProvider;
 import org.eclipse.ecf.salvo.ui.internal.provider.NewsGroupProvider;
 import org.eclipse.ecf.salvo.ui.internal.provider.NewsLabelProvider;
 import org.eclipse.ecf.salvo.ui.internal.resources.ISalvoResource;
 import org.eclipse.ecf.salvo.ui.internal.resources.SalvoResourceFactory;
 import org.eclipse.ecf.salvo.ui.internal.resources.SalvoUIResource;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.FilteredTree;
 import org.eclipse.ui.dialogs.PatternFilter;
 
 public class SubscribeGroupWizardPage extends WizardPage {
 
 	private TreeViewer checkboxTreeViewer;
 	final private HashSet<Object> checkedItems = new HashSet<Object>();

 	public SubscribeGroupWizardPage(String pageName) {
 		super(pageName);
 		setDescription("Select the groups you want to subscribe to.");
 	}
 
 	public void createControl(Composite parent) {
 
 		checkedItems.clear();
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		composite.setLayout(new GridLayout(1, false));
 
 		setControl(composite);
 
 		PatternFilter filter = new PatternFilter() {
 
			private boolean next;

 			@Override
 			public boolean isElementVisible(Viewer viewer, Object element) {
 
 				if (checkedItems.contains(element)) {
 					return true;
 				}
 
 				return super.isElementVisible(viewer, element);
 			}

 		};
 
		FilteredTree tree = new FilteredTree(composite, SWT.CHECK, filter, true);
		
		checkboxTreeViewer = tree.getViewer();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		checkboxTreeViewer.setContentProvider(new NewsContentProvider());
		checkboxTreeViewer.setLabelProvider(new NewsLabelProvider(checkedItems));
 		// checkboxTreeViewer.setComparator(new ViewerComparator());
 
 		// ITreeViewerListener
 
 		checkboxTreeViewer.getTree().addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseUp(MouseEvent e) {
 
 				if (e.widget instanceof Tree) {
 					TreeItem item = ((Tree) e.widget).getItem(new Point(e.x,
 							e.y));
 					if (item != null) {
 						if (item.getChecked())
 							checkedItems.add(item.getData());
 						else
 							checkedItems.remove(item.getData());
 					}
 				}
 			}
 		});
 	}
 
 	public Collection<INewsgroup> getGroups() {
 		ArrayList<INewsgroup> result = new ArrayList<INewsgroup>();
 		for (Object object : checkedItems)
 			if (object instanceof SalvoUIResource
 					&& ((SalvoUIResource) object).getObject() instanceof INewsgroup)
 				result.add((INewsgroup) ((SalvoUIResource) object).getObject());
 		return result;
 	}
 
 	public void setInput(IServer server) {
 		setTitle(server.getAddress());
		ISalvoResource s2 = SalvoResourceFactory.getResource(server
				.getAddress(), server);
 		s2.setChildProvider(new NewsGroupProvider(s2));
 		checkboxTreeViewer.setInput(s2);
 	}
 }
