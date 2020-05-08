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
 package org.jboss.tools.jst.jsp.editor;
 
 import org.eclipse.ui.IReusableEditor;
 import org.eclipse.ui.texteditor.ITextEditor;
 
public interface IVisualEditor extends ITextEditor, IReusableEditor {
 	public static final int VISUALSOURCE_MODE = 0;
 	//public static final int VISUAL_MODE = 1;
 	public static final int SOURCE_MODE = 1;
 	
 	/** PREVIEW_MODE */
 	public static final int PREVIEW_MODE = 2;
 
 	public void setVisualMode(int mode);
 	public IVisualController getController();
 	public Object getPreviewWebBrowser();
 	public void createPreviewBrowser();
 	public Object getVisualEditor();
 	public void createVisualEditor();
 	public void maximizeSource();
 	public void maximizeVisual();
 
 }
