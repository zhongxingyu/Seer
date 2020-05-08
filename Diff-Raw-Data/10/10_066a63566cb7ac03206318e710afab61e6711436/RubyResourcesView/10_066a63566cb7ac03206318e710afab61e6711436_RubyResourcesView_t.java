 /*
  * Author: Markus Barchfeld
  * 
  * Copyright (c) 2004 RubyPeople.
  * 
  * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT
  * is subject to the "Common Public License (CPL) v 1.0". You may not use RDT
  * except in compliance with the License. For further information see
  * org.rubypeople.rdt/rdt.license.
  */
 
 package org.rubypeople.rdt.internal.ui.resourcesview;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.IShowInTarget;
 import org.eclipse.ui.part.ShowInContext;
 import org.eclipse.ui.views.navigator.ResourceNavigator;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.internal.ui.RubyPlugin;
 import org.rubypeople.rdt.internal.ui.RubyViewerFilter;
 import org.rubypeople.rdt.internal.ui.rubyeditor.ExternalRubyFileEditorInput;
 import org.rubypeople.rdt.internal.ui.rubyeditor.IRubyScriptEditorInput;
import org.rubypeople.rdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.rubypeople.rdt.internal.ui.viewsupport.DecoratingRubyLabelProvider;
 import org.rubypeople.rdt.ui.RubyUI;
 
 public class RubyResourcesView extends ResourceNavigator implements IShowInTarget {
 
 	private static String IS_RUBY_FILES_ONLY_MEMENTO_KEY = "isRubyFilesOnlyFilterActivated";
 	private boolean isRubyFilesOnlyFilterActivated = false; // default value if
 														   // there is no
 														   // memento
 
 	public void createPartControl(Composite parent) {
 		super.createPartControl(parent);
 		RubyViewerFilter filter = new RubyViewerFilter(this);
 		this.getViewer().addFilter(filter);
 	}
 
 	protected void makeActions() {
 		setActionGroup(new RubyFilterActionGroup(this));
 	}
 
 	protected void restoreRubyFilesOnlyFilterActivated(IMemento memento) {
 		Integer isRubyFilesOnlyFilterActivatedMemento = memento.getInteger(RubyResourcesView.IS_RUBY_FILES_ONLY_MEMENTO_KEY);
 		if (isRubyFilesOnlyFilterActivatedMemento != null) {
 			this.isRubyFilesOnlyFilterActivated = isRubyFilesOnlyFilterActivatedMemento.intValue() == 1;
 		}
 	}
 
 	public void saveState(IMemento memento) {
 		super.saveState(memento);
 		memento.putInteger(RubyResourcesView.IS_RUBY_FILES_ONLY_MEMENTO_KEY, isRubyFilesOnlyFilterActivated ? 1 : 0);
 	}
 
 	public boolean isRubyFilesOnlyFilterActivated() {
 		return isRubyFilesOnlyFilterActivated;
 	}
 
 	public void setRubyFilesOnlyFilterActivated(boolean isRubyFilesOnlyFilterActivated) {
 		this.isRubyFilesOnlyFilterActivated = isRubyFilesOnlyFilterActivated;
 	}
 
 	public void init(IViewSite site, IMemento memento) throws PartInitException {
 		super.init(site, memento);
 		if (memento != null) {
 			this.restoreRubyFilesOnlyFilterActivated(memento);
 		}
 	}
 	protected void editorActivated(IEditorPart editor) {
 		if (!isLinkingEnabled()) {
 			return;
 		}
 
 		IEditorInput input = editor.getEditorInput();
 		if (input instanceof IFileEditorInput) {
 			IFileEditorInput fileInput = (IFileEditorInput) input;
 			IFile file = fileInput.getFile();
 			// Quick Fix: if Editor is RubyExternal, then file is null
 			// TODO: Check if RubyExternalEditor's input should be IFileEditorInput at all
 			if (file == null) {
 				return ;
 			}			
 		}
 		super.editorActivated(editor) ;
 	}
 
 	public static RubyResourcesView openInActivePerspective() {
 		try {
 			return (RubyResourcesView)RubyPlugin.getActivePage().showView(RubyUI.ID_RUBY_RESOURCE_VIEW);
 		} catch(PartInitException pe) {
 			return null;
 		}
 	}
 	
     public boolean tryToReveal(Object element) {
 		if (revealElementOrParent(element))
             return true;
         return false;
     }
     
     private boolean revealElementOrParent(Object element) {
         if (revealAndVerify(element))
 		    return true;
 		element= getVisibleParent(element);
 		if (element != null) {
 		    if (revealAndVerify(element))
 		        return true;
 		    if (element instanceof IRubyElement) {
 		        IResource resource= ((IRubyElement)element).getResource();
 		        if (resource != null) {
 		            if (revealAndVerify(resource))
 		                return true;
 		        }
 		    }
 		}
         return false;
     }
     
     private boolean revealAndVerify(Object element) {
     	if (element == null)
     		return false;
     	selectReveal(new StructuredSelection(element));
     	return ! getSite().getSelectionProvider().getSelection().isEmpty();
     }
     
     private Object getVisibleParent(Object object) {
     	if (object == null)
     		return null;
     	if (!(object instanceof IRubyElement))
     	    return object;
     	IRubyElement element2= (IRubyElement) object;
     	switch (element2.getElementType()) {
     		case IRubyElement.IMPORT_DECLARATION:
     		case IRubyElement.IMPORT_CONTAINER:
     		case IRubyElement.TYPE:
     		case IRubyElement.METHOD:
     		case IRubyElement.FIELD:
     			// select parent script
     			element2= (IRubyElement)element2.getOpenable();
     			break;
     		case IRubyElement.RUBY_MODEL:
     			element2= null;
     			break;
     	}
     	return element2;
     }
 
 	public boolean show(ShowInContext context) {
 		ISelection selection= context.getSelection();
 		if (selection instanceof IStructuredSelection) {
 			// fix for 64634 Navigate/Show in/Package Explorer doesn't work 
 			IStructuredSelection structuredSelection= ((IStructuredSelection) selection);
 			if (structuredSelection.size() == 1 && tryToReveal(structuredSelection.getFirstElement()))
 				return true;
 		}
 		
 		Object input= context.getInput();
 		if (input instanceof IEditorInput) {
 			Object elementOfInput= getElementOfInput((IEditorInput)context.getInput());
 			return elementOfInput != null && tryToReveal(elementOfInput);
 		}
 
 		return false;
 	}
 	
	@Override
	protected void initLabelProvider(TreeViewer viewer) {
		 viewer.setLabelProvider(new DecoratingRubyLabelProvider(new AppearanceAwareLabelProvider()));
	}
	
 	/**
 	 * Returns the element contained in the EditorInput
 	 */
 	Object getElementOfInput(IEditorInput input) {
 		if (input instanceof IRubyScriptEditorInput)
 			return ((IRubyScriptEditorInput)input).getRubyScript();
 		else if (input instanceof IFileEditorInput)
 			return ((IFileEditorInput)input).getFile();
 		else if (input instanceof ExternalRubyFileEditorInput)
 			return ((ExternalRubyFileEditorInput)input).getStorage();
 		return null;
 	}
 }
