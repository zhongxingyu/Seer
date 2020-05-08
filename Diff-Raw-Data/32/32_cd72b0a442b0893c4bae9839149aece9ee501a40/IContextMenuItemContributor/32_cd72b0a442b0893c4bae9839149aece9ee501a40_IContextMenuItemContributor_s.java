 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.extensionpoint;
 
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 
 /**
  * @author mengbo
  */
 public interface IContextMenuItemContributor {
 	void setURI(String uri);
 
 	String getURI();
 
 	void fillContextMenu(IMenuManager manager, ISelection selection,
 			IStructuredModel model, Control parentUI);
 }
