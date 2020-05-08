 /*******************************************************************************
  * Copyright (c) 2013 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.e4.tools.event.spy.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.e4.tools.event.spy.model.CapturedEvent;
 import org.eclipse.e4.tools.event.spy.model.CapturedEventTreeSelection;
 import org.eclipse.e4.tools.event.spy.model.ItemToFilter;
 import org.eclipse.e4.tools.event.spy.model.Parameter;
 import org.eclipse.e4.tools.event.spy.util.ParameterFormatter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.TreeEvent;
 import org.eclipse.swt.events.TreeListener;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeColumn;
 import org.eclipse.swt.widgets.TreeItem;
 
 
 
 public class CapturedEventTree {
 
 	public interface SelectionListener {
 		void selectionChanged(CapturedEventTreeSelection selection);
 	}
 
	private final Tree tree;
 
 	private SelectionListener selectionListener;
 
	private final List<ItemToFilter> columns = new ArrayList<ItemToFilter>();
 
 
 	/* Layout scheme:
 	 *
 	 *  +-- parent---------------+
 	 *  |                        |
 	 *  |         Tree           |
 	 *  |                        |
 	 *  +------------------------+
 	 *
 	 * */
 
 	public CapturedEventTree(Composite parent) {
 		tree = new Tree(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
 		tree.setHeaderVisible(true);
 		tree.setLinesVisible(true);
 
 		TreeColumn column = new TreeColumn(tree, SWT.LEFT);
 		column.setText(ItemToFilter.Topic.toString());
 		column.setWidth(350);
 		columns.add(ItemToFilter.Topic);
 
 		column = new TreeColumn(tree, SWT.LEFT);
 		column.setText(ItemToFilter.Publisher.toString());
 		column.setWidth(150);
 		columns.add(ItemToFilter.Publisher);
 
 		column = new TreeColumn(tree, SWT.LEFT);
 		column.setText(ItemToFilter.ChangedElement.toString());
 		column.setWidth(150);
 		columns.add(ItemToFilter.ChangedElement);
 
 		addTreeEventListeners();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void addTreeEventListeners() {
 		tree.addTreeListener(new TreeListener() {
 			public void treeExpanded(TreeEvent e) {
 				TreeItem item = (TreeItem) e.item;
 				TreeItem paramItem = item.getItem(0);
				if (paramItem.getText().length() == 0) {
 					for (Parameter param: (List<Parameter>) paramItem.getData()) {
 						if (paramItem == null) {
 							paramItem = new TreeItem(item, SWT.NONE);
 						}
 						paramItem.setText(ParameterFormatter.toString(param));
 						paramItem = null;
 					}
 				}
 
 			}
 			public void treeCollapsed(TreeEvent e) {
 			}
 		});
 
 		tree.addMouseListener(new MouseListener() {
 			public void mouseUp(MouseEvent e) {
 			}
 			public void mouseDoubleClick(MouseEvent e) {
 
 			}
 			public void mouseDown(MouseEvent e) {
 				TreeItem[] items = tree.getSelection();
 				if (items == null || items.length == 0) {
 					return;
 				}
 				int selectedItemIndex = -1;
 				for (int i=0; i<columns.size(); i++) {
 					Rectangle rec = items[0].getBounds(i);
 					if (e.x >= rec.x && e.x <= rec.x + rec.width) {
 						selectedItemIndex = i;
 						break;
 					}
 				}
 				if (selectedItemIndex >= 0 && selectionListener != null) {
 					String selection = items[0].getText(selectedItemIndex);
					if (selection.length() != 0) {
 						selectionListener.selectionChanged(new CapturedEventTreeSelection(selection,items[0].getItemCount() == 0));
 					}
 				}
 			}
 		});
 	}
 
 	public void addEvent(CapturedEvent event) {
 		TreeItem item = new TreeItem(tree, SWT.NONE);
 
 		item.setText(columns.indexOf(ItemToFilter.Topic), event.getTopic());
 		item.setText(columns.indexOf(ItemToFilter.Publisher), event.getPublisherClassName());
 		item.setText(columns.indexOf(ItemToFilter.ChangedElement), event.getChangedElementClassName());
 
 		if (event.hasParameters()) {
 			item = new TreeItem(item, SWT.NONE);
 			item.setData(event.getParameters());
 		}
 	}
 
 	public void setSelectionListener(SelectionListener selectionListener) {
 		this.selectionListener = selectionListener;
 	}
 
 	public Control getControl() {
 		return tree;
 	}
 
 	public void removeAll() {
 		tree.removeAll();
 	}
 }
