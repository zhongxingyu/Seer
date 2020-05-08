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
 package org.jboss.tools.jst.web.tiles.ui.editor;
 
 import org.eclipse.gef.ui.actions.ActionRegistry;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.jboss.tools.common.editor.AbstractSelectionProvider;
 import org.jboss.tools.common.editor.ObjectMultiPageEditor;
 import org.jboss.tools.common.editor.ObjectTextEditor;
 import org.jboss.tools.common.gef.outline.xpl.DiagramContentOutlinePage;
 import org.jboss.tools.common.model.ui.texteditors.XMLTextEditorComponent;
 import org.jboss.tools.jst.web.messages.xpl.WebUIMessages;
 import org.jboss.tools.jst.web.tiles.model.TilesConfigFilteredTreeConstraint;
 import org.jboss.tools.jst.web.tiles.ui.ITilesHelpContextIds;
 import org.jboss.tools.jst.web.tiles.ui.TilesUIPlugin;
 
 public class TilesCompoundEditor extends ObjectMultiPageEditor {
 	protected TilesGuiEditor guiEditor;
 	protected TilesConfigFilteredTreeConstraint constraint = new TilesConfigFilteredTreeConstraint();
 	
 	public TilesCompoundEditor() {
 		outline.addFilter(constraint);
 	}
 	
 	protected Composite createPageContainer(Composite parent) {
 		Composite composite = super.createPageContainer(parent);
 		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ITilesHelpContextIds.TILES_EDITOR);
 		return composite;
 	}
 	
 	protected void doCreatePages() {
 		if (isAppropriateNature()) {
 			treeFormPage = createTreeFormPage();
 			treeFormPage.setTitle(WebUIMessages.TILES_EDITOR);
 			treeFormPage.addFilter(constraint);
 			treeFormPage.initialize(object);
 			addFormPage(treeFormPage);
 			createGuiPage();
 		}
 		createTextPage();
 		initEditors();
 	}
 
 	protected boolean isWrongEntity(String entity) {
 		return !entity.equals("FileTiles"); //$NON-NLS-1$
 	}
 	public TilesGuiEditor getGuiEditor(){
 		return this.guiEditor;
 	}
 	
 	protected void createGuiPage() {
 		guiEditor = new TilesGuiEditor();
 		try {
			int index = addPage(guiEditor, guiEditor.getEditorInput());
 			setPageText(index, WebUIMessages.DIAGRAM);
 			guiEditor.setInput(input);
 			selectionProvider.setHost(guiEditor.getSelectionProvider());
 			guiEditor.addErrorSelectionListener(createErrorSelectionListener());
 			selectionProvider.addHost(
 					"guiEditor", guiEditor.getSelectionProvider()); //$NON-NLS-1$
 		} catch (PartInitException e) {
 			TilesUIPlugin.getPluginLog().logError(e);
 		}
 	}
 	
 	protected ObjectTextEditor createTextEditor() {
 		return new XMLTextEditorComponent();	
 	}
 
 	public void dispose() {
 		if(input != null) {
 			selectionProvider.setHost(null);
 			getSite().setSelectionProvider(null); 
 		}
 		super.dispose();
 		if(guiEditor != null) {
 			guiEditor.dispose();
 			guiEditor = null;
 		}
 	}
 	
 	protected void setNormalMode() {
 		if ((guiEditor != null) && isAppropriateNature()) {
 			guiEditor.setObject(getModelObject(), isErrorMode());
 		}
 		if (treeFormPage!=null) {
 			treeFormPage.initialize(getModelObject());
 			treeFormPage.setErrorMode(isErrorMode());
 		}
 		updateSelectionProvider();
 	}
 
 	protected void setErrorMode() {
 		setNormalMode();
 	}
 
 	protected int getGuiPageIndex() {
 		return 1; 
 	}
 	
 	public boolean isGuiEditorActive() {
 		return getActivePage() == getGuiPageIndex();
 	}
 	
 	protected void updateSelectionProvider() {
 		if(guiEditor != null) {
 			selectionProvider.addHost("guiEditor", guiEditor.getSelectionProvider()); //$NON-NLS-1$
 		}
 		if(textEditor != null) {
 			selectionProvider.addHost("textEditor", getTextSelectionProvider()); //$NON-NLS-1$
 		}
 		int index = getActivePage();
 		if(index == getSourcePageIndex()) {
 			if(textEditor != null) {
 				selectionProvider.setHost(getTextSelectionProvider());
 			}
 			return;
 		}
 		if(index != getGuiPageIndex() || guiEditor == null || guiEditor.getSelectionProvider() == null) {
 			if (treeEditor != null) {
 				selectionProvider.setHost(treeEditor.getSelectionProvider());
 				treeEditor.fireEditorSelected();
 			}
 			if (treeFormPage != null) {
 				selectionProvider.addHost("treeEditor", treeFormPage.getSelectionProvider(), true); //$NON-NLS-1$
 			}
 		} else {
 			ISelectionProvider p = guiEditor.getSelectionProvider();
 			selectionProvider.setHost(p);
 			if(p instanceof AbstractSelectionProvider) {
 				((AbstractSelectionProvider)p).fireSelectionChanged();
 			}		
 		}
 	}
 
 	public Object getAdapter(Class adapter) {
 		if(adapter == IContentOutlinePage.class){
 			if(guiEditor == null || guiEditor.getGUI() == null) {
 				return super.getAdapter(adapter);
 			}
 			Object o = guiEditor.getGUI().getAdapter(adapter);
 			if(o instanceof DiagramContentOutlinePage) {
 				DiagramContentOutlinePage g = (DiagramContentOutlinePage)o;
 				g.setTreeOutline(outline);
 			}
 			return o;  
 		}
 		if(adapter == ActionRegistry.class || adapter == org.eclipse.gef.editparts.ZoomManager.class){
 			 if(guiEditor != null)
 			 	if(guiEditor.getGUI() != null)
 			 		return guiEditor.getGUI().getAdapter(adapter);
 		}
 
 		return super.getAdapter(adapter);
 	}
 
 }
