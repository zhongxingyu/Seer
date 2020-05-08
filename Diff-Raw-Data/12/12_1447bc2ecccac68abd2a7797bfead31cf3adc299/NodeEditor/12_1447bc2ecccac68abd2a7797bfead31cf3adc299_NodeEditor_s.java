 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2004 Johann Gyger <jgyger@users.sf.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.views;
 
 import java.util.Vector;
 
 import net.sf.jmoney.IBookkeepingPage;
 import net.sf.jmoney.IBookkeepingPageFactory;
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.fields.AccountInfo;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.ExtendablePropertySet;
 import net.sf.jmoney.model2.PageEntry;
 import net.sf.jmoney.model2.PropertySet;
 import net.sf.jmoney.model2.ScalarPropertyAccessor;
 import net.sf.jmoney.model2.SessionChangeAdapter;
 import net.sf.jmoney.model2.SessionChangeListener;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.actions.ActionGroup;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.operations.UndoRedoActionGroup;
 
 /**
  * TODO
  * 
  * @author Johann Gyger
  */
 public class NodeEditor extends FormEditor {
 
     protected Object navigationTreeNode;
    protected Vector pageListeners;
 
     protected SessionChangeListener accountNameChangeListener = null;
     
     /* (non-Javadoc)
      * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
      */
     protected void addPages() {
         NodeEditorInput cInput = (NodeEditorInput)this.getEditorInput();
         IMemento memento = cInput.getMemento();
         
         IBookkeepingPage pages [] = new IBookkeepingPage[pageListeners.size()];
         
     	for (int i = 0; i < pageListeners.size(); i++) {
    		PageEntry entry = (PageEntry)pageListeners.get(i);
     		String pageId = entry.getPageId();
     		IBookkeepingPageFactory pageListener = entry.getPageFactory();
     		pages[i] = pageListener.createFormPage(this, memento==null?null:memento.getChild(pageId));
     	}
     	
     	cInput.pages = pages;
     }
 
     public void dispose() {
     	// When the editor is disposed, let the editor input
     	// know that there is no editor open.  This is necessary
     	// because the input is kept around by Eclipse even when the
     	// editor is closed and kept in a Most Recently Used list.
     	// This list is persisted in the workbench memento, but the
     	// editor input must not try to persist the values of the controls
     	// because the controls no longer exist.
     	// We indicate this situation by clearing out the list
     	// of pages.
         NodeEditorInput cInput = (NodeEditorInput)this.getEditorInput();
     	cInput.pages = null;
 
     	if (accountNameChangeListener != null) {
     		((Account)cInput.getNode()).getObjectKey().getSessionManager().removeChangeListener(accountNameChangeListener);
     	}
 
     	super.dispose();
     }
     
     /* (non-Javadoc)
      * @see org.eclipse.ui.IEditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
      */
     public void init(IEditorSite site, IEditorInput input) throws PartInitException {
         super.init(site, input);
 
         final NodeEditorInput cInput = (NodeEditorInput) input;
 
         navigationTreeNode = cInput.getNode();
     	pageListeners = cInput.getPageListeners();
 
        	setPartName(cInput.getName());
 		setTitleImage(cInput.getImage());
 		
 		/*
 		 * If the node object is an account then the title is the name of the account.
 		 * We must listen for changes in the name and update the title accordingly.
 		 * This ensures the title changes when the account name is edited.
 		 */
 		if (cInput.getNode() instanceof Account) {
 			final Account account = (Account)cInput.getNode();
 			accountNameChangeListener = new SessionChangeAdapter() {
 				@Override
 				public void objectChanged(ExtendableObject changedObject, ScalarPropertyAccessor propertyAccessor, Object oldValue, Object newValue) {
 					if (changedObject == account
 							&& propertyAccessor == AccountInfo.getNameAccessor()) {
 				       	setPartName(account.getName());
 					}
 				}
 			};
 			account.getObjectKey().getSessionManager().addChangeListener(accountNameChangeListener);
 		}
 		
 		ActionGroup ag = new UndoRedoActionGroup(
 //				getSite(), 
 				site, 
 				getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getUndoContext(),
 				true);
 		ag.fillActionBars(site.getActionBars());
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
      */
     public void doSave(IProgressMonitor monitor) {
         // TODO Auto-generated method stub
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.ISaveablePart#doSaveAs()
      */
     public void doSaveAs() {
         throw new RuntimeException("Illegal invocation");
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
      */
     public boolean isSaveAsAllowed() {
         return false;
     }
 
 	/**
 	 * @return
 	 */
 	public Object getSelectedObject() {
 		return navigationTreeNode;
 	}
 
 	/**
 	 * @param extendableObject
 	 */
 	public static void openEditor(IWorkbenchWindow window, ExtendableObject extendableObject) {
 		ExtendablePropertySet<?> propertySet = PropertySet.getPropertySet(extendableObject.getClass());
 		Vector<PageEntry> pages = propertySet.getPageFactories();
 		
 		// Create an editor for this node (or active if an editor
 		// is already open).  However, if no pages are registered for this
 		// node then do nothing.
 		if (!pages.isEmpty()) {
 			try {
 				IEditorInput editorInput = new NodeEditorInput(extendableObject,
 						extendableObject.toString(),
 						propertySet.getIcon(),
 						pages,
 						null);
 				window.getActivePage().openEditor(editorInput,
 				"net.sf.jmoney.genericEditor");
 			} catch (PartInitException e) {
 				JMoneyPlugin.log(e);
 			}
 		}
 	}
 }
