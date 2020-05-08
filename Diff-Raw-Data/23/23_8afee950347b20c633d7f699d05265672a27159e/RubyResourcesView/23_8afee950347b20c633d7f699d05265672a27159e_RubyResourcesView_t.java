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
 import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.views.navigator.ResourceNavigator;
 import org.rubypeople.rdt.internal.ui.RubyViewerFilter;
 
 public class RubyResourcesView extends ResourceNavigator {
 
 	private static String IS_RUBY_FILES_ONLY_MEMENTO_KEY = "isRubyFilesOnlyFilterActivated";
 	private boolean isRubyFilesOnlyFilterActivated = true; // default value if
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
 }
