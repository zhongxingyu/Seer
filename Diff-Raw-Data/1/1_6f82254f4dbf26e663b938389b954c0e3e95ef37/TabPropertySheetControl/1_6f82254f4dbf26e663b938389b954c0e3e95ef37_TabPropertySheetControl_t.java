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
 package org.jboss.tools.jst.jsp.outline.cssdialog.tabs;
 
 import java.util.ArrayList;
 import java.util.Set;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.TreeEditor;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeColumn;
 import org.eclipse.swt.widgets.TreeItem;
 import org.jboss.tools.jst.jsp.messages.JstUIMessages;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.CSSConstants;
 import org.jboss.tools.jst.jsp.outline.cssdialog.common.StyleAttributes;
 
 /**
  * Class for creating Property sheet tab
  * 
  * @author Evgeny Zheleznyakov
  */
 public class TabPropertySheetControl extends BaseTabControl {
 
 	final static private String[] COLUMNS = new String[] {
 			JstUIMessages.PROPERTY_NAME_COLUMN,
 			JstUIMessages.PROPERTY_VALUE_COLUMN };
 	final static private int COLUMNS_WIDTH = 200;
 
 	private Tree tree;
 
 	/**
 	 * Constructor for creating controls
 	 * 
 	 * @param composite
 	 *            The parent composite for tab
 	 * @param elementMap
 	 * @param comboMap
 	 * @param styleAttributes
 	 *            the StyleAttributes object
 	 */
 	public TabPropertySheetControl(Composite parent,
 			StyleAttributes styleAttributes, DataBindingContext bindingContext) {
 		super(bindingContext, styleAttributes, parent, SWT.NONE);
 		setLayout(new FillLayout());
 
 		tree = new Tree(this, SWT.FULL_SELECTION | SWT.SINGLE
 				| SWT.HIDE_SELECTION);
 		tree.setHeaderVisible(true);
 		tree.setLinesVisible(true);
 
 		// Create columns
 		for (int i = 0; i < COLUMNS.length; i++) {
 			TreeColumn column = new TreeColumn(tree, SWT.LEFT | SWT.COLOR_BLACK);
 
 			column.setText(COLUMNS[i]);
 			column.setWidth(COLUMNS_WIDTH);
 		}
 
 		Set<String> sections = CSSConstants.CSS_STYLES_MAP.keySet();
 		for (String sectionKey : sections) {
 
 			TreeItem sectionTreeItem = createTreeItem(tree, sectionKey);
 			sectionTreeItem.setExpanded(true);
 			ArrayList<String> attributeKeys = CSSConstants.CSS_STYLES_MAP
 					.get(sectionKey);
 
 			for (String attribute : attributeKeys) {
 				TreeItem item = createBindedTreeItem(sectionTreeItem, attribute);
 				item.setExpanded(true);
 			}
			sectionTreeItem.setExpanded(true);
 		}
 
 		final TreeEditor editor = new TreeEditor(tree);
 		editor.horizontalAlignment = SWT.LEFT;
 		editor.grabHorizontal = true;
 		editor.minimumHeight = 0;
 		editor.minimumWidth = 0;
 
 		tree.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				// Clean up any previous editor control
 				Control oldEditor = editor.getEditor();
 				if (oldEditor != null)
 					oldEditor.dispose();
 
 				// Identify the selected row
 				TreeItem item = (TreeItem) e.item;
 				if (item == null)
 					return;
 
 				// The control that will be the editor must be a child of the
 				// Tree
 				Control newEditor = null;
 				if (!CSSConstants.CSS_STYLES_MAP.containsKey((item
 						.getText(NAME_ATTRIBUTE_COLUMN)))) {
 					newEditor = createControl(tree, item
 							.getText(NAME_ATTRIBUTE_COLUMN));
 				}
 
 				if (newEditor != null) {
 
 					newEditor.setFocus();
 
 					// Compute the width for the editor
 					// Also, compute the column width, so that the dropdown fits
 					Point size = newEditor
 							.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 					editor.minimumWidth = size.x;
 					editor.minimumHeight = size.y;
 
 					if (tree.getColumn(VALUE_ATTRIBUTE_COLUMN).getWidth() < editor.minimumWidth)
 						tree.getColumn(VALUE_ATTRIBUTE_COLUMN).setWidth(
 								editor.minimumWidth);
 
 					editor.setEditor(newEditor, item, VALUE_ATTRIBUTE_COLUMN);
 				}
 			}
 		});
 
 	}
 }
