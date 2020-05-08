 /*******************************************************************************
  * Copyright (c) 2011 Manumitting Technologies, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Brian de Alwis (MT) - initial API and implementation
  *******************************************************************************/
 package org.eclipse.e4.tools.css.spy;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolItem;
 import org.w3c.dom.NodeList;
 
 public class WidgetTreeProvider implements ITreeContentProvider {
     private static final Object[] EMPTY_ARRAY = new Object[0];
 
     public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
     }
 
     public Object[] getElements(Object inputElement) {
         if (inputElement instanceof Object[]) {
             return (Object[]) inputElement;
         } else if (inputElement instanceof Collection< ? >) {
             return ((Collection< ? >) inputElement).toArray();
         }
         return getChildren(inputElement);
     }
 
 
     public Object[] getChildren(Object parentElement) {
         CSSStylableElement element = CssSpyDialog.getCSSElement(parentElement);
         if (element == null) {
 			return EMPTY_ARRAY;
         }
         NodeList kids = element.getChildNodes();
         ArrayList<Object> children = new ArrayList<Object>(kids.getLength());
         for (int i = 0; i < kids.getLength(); i++) {
 			children.add(((CSSStylableElement) kids.item(i)).getNativeWidget());
         }
         return children.toArray();
     }
 
     public Object getParent(Object element) {
 		if (element instanceof Control) {
 			return ((Control) element).getParent();
		} else if (element instanceof org.eclipse.swt.custom.CTabItem) {
			return ((org.eclipse.swt.custom.CTabItem) element).getParent();
		} else if (element instanceof org.eclipse.e4.ui.widgets.CTabItem) {
			return ((org.eclipse.e4.ui.widgets.CTabItem) element).getParent();
		} else if (element instanceof ToolItem) {
			return ((ToolItem) element).getParent();
 		}
 		return null;
     }
 
     public boolean hasChildren(Object element) {
         return getChildren(element).length > 0;
     }
 
     public void dispose() {
     }
 
 }
