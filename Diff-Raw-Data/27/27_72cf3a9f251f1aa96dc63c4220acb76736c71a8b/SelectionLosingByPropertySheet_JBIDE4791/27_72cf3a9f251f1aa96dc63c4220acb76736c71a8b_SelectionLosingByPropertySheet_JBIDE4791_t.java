 /*******************************************************************************
  * Copyright (c) 2007-2009 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.css.test.jbide;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.TrayItem;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.views.properties.PropertySheet;
 import org.eclipse.ui.views.properties.PropertySheetEntry;
 import org.eclipse.wst.css.core.internal.document.CSSStructuredDocumentRegionContainer;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSModel;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSStyleSheet;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.jboss.tools.jst.css.test.AbstractCSSViewTest;
 import org.jboss.tools.jst.css.view.CSSEditorView;
 import org.jboss.tools.test.util.JobUtils;
 import org.w3c.dom.css.CSSRule;
 
 /**
  * @author Sergey Dzmitrovich
  * 
  *         https://jira.jboss.org/jira/browse/JBIDE-4791
  * 
  */
 public class SelectionLosingByPropertySheet_JBIDE4791 extends
 		AbstractCSSViewTest {
 
 	private static final String FIRST_CSS_PROPERTY_VALUE = "red";
 
 	private static final String FIRST_CSS_PROPERTY_NAME = "color";
 
 	public static final String TEST_PAGE_NAME = "JBIDE/4791/propertyViewTest.css"; //$NON-NLS-1$
 
 	public static final String PROPERTY_SHEET_VIEW_ID = "org.eclipse.ui.views.PropertySheet"; //$NON-NLS-1$
 
 	public static final String SELECTION_FIELD_NAME = "currentSelection"; //$NON-NLS-1$
 
 	public void testSelectionLosingByPropertySheet() throws CoreException,
 			SecurityException, IllegalArgumentException, NoSuchFieldException,
 			IllegalAccessException {
 
 		IFile pageFile = getComponentPath(TEST_PAGE_NAME, getProjectName());
 
 		assertNotNull(pageFile);
 
 		PropertySheet propertySheet = (PropertySheet) openView(PROPERTY_SHEET_VIEW_ID);
 
 		assertNotNull(propertySheet);
 
 		StructuredTextEditor editor = (StructuredTextEditor) openEditor(
 				pageFile, CSS_EDITOR_ID);
 
 		assertNotNull(editor);
 
 		ICSSModel model = (ICSSModel) getStructuredModel(pageFile);
 
 		assertNotNull(model);
 
 		ICSSStyleSheet document = (ICSSStyleSheet) model.getDocument();
 
 		assertNotNull(document);
 
 		CSSRule cssRule = document.getCssRules().item(0);
 		assertNotNull(cssRule);
 
 		int cssRuleOffset = ((CSSStructuredDocumentRegionContainer) cssRule).getStartOffset();
 		setSelection(editor, cssRuleOffset, 0);
 		
 		Tree propertySheetTree = (Tree) propertySheet.getCurrentPage().getControl();
 		String colorValue = getPropertyValue(propertySheetTree.getItems(), FIRST_CSS_PROPERTY_NAME);
 		assertEquals(FIRST_CSS_PROPERTY_VALUE, colorValue);
 		
 		CSSEditorView view = (CSSEditorView) openView(CSS_EDITOR_VIEW);
		int counter = 10;
		do {
			JobUtils.delay(2000);
			propertySheetTree = (Tree) propertySheet.getCurrentPage().getControl();
			colorValue = getPropertyValue(propertySheetTree.getItems(), FIRST_CSS_PROPERTY_NAME);
			counter--;
		} while (colorValue == null && counter > 0);
		
		assertEquals(FIRST_CSS_PROPERTY_VALUE, colorValue);// if after 20s it is still null - fail
 	}
 	
 	private String getPropertyValue(TreeItem[] treeItems, String propertyName) {
 		for (TreeItem treeItem : treeItems) {
 			Object data = treeItem.getData();
 			if (data instanceof PropertySheetEntry) {
 				PropertySheetEntry propertySheetEntry = (PropertySheetEntry) data;
 				if (propertyName.equals(propertySheetEntry.getDisplayName())) {
 					return propertySheetEntry.getValueAsString();
 				}
 			}
 			String descendantsValue = getPropertyValue(treeItem.getItems(), propertyName);
 			if (descendantsValue != null) {
 				return descendantsValue;
 			}
 		}
 		
 		return null;
 	}
 
 }
