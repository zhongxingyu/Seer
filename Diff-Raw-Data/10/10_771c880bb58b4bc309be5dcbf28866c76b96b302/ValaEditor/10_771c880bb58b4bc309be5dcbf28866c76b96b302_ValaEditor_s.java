 /* ValaEditor.java
  *
  * Copyright (C) 2007-2008  Johann Prieur <johann.prieur@gmail.com>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  */
 package valable.editors;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.action.Action;
 import org.eclipse.ui.editors.text.TextEditor;
 import org.eclipse.ui.texteditor.ContentAssistAction;
 import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 
 import valable.ValaPlugin;
 import valable.editors.util.ColorManager;
 import valable.outline.ValaOutlinePage;
 
 public class ValaEditor extends TextEditor {
 
 	private ValaOutlinePage valaOutlinePage;
 	private ColorManager    colorManager;
 
 	/**
 	 * Create a new ValaEditor.
 	 */
 	public ValaEditor() {
 		super();
 		
 		colorManager = new ColorManager();
 		setSourceViewerConfiguration(new ValaConfiguration(colorManager));
 		setDocumentProvider(new ValaDocumentProvider());
 		
 		valaOutlinePage = new ValaOutlinePage(this);
 	}
 	
 	
 	public void dispose() {
 		colorManager.dispose();
 		super.dispose();
 	}
 	
 	
 	/**
 	 * Return the required adapter.
 	 * 
 	 * @see org.eclipse.ui.editors.text.TextEditor#getAdapter(java.lang.Class)
 	 * @see http://wiki.eclipse.org/FAQ_How_do_I_create_an_Outline_view_for_my_own_language_editor%3F
 	 */
	@SuppressWarnings("unchecked")
 	@Override
 	public Object getAdapter(Class required) {
 		if (IContentOutlinePage.class.equals(required)) {
 			return valaOutlinePage;
 		}
 		
 		return super.getAdapter(required);
 	}
 	
 	
 	/**
 	 * Additional actions in the Vala editor.
 	 * 
 	 * @see org.eclipse.ui.editors.text.TextEditor#createActions()
 	 */
 	@Override
 	protected void createActions() {
 		super.createActions();
 		
 		Action action = new ContentAssistAction(ValaPlugin.getResourceBundle(), "ContentAssistProposal.", this); 
 		String id = ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS;
 		action.setActionDefinitionId(id);
 		setAction("ContentAssistProposal", action); 
 		markAsStateDependentAction("ContentAssistProposal", true);
 	}
 	
 	
 	/**
 	 * @return the file represented in this editor.
 	 */
 	public IFile getCurrentFile() {
         return (IFile)this.getEditorInput().getAdapter(IFile.class);
 	}
 }
