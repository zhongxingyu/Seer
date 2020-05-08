 /******************************************************************************* 
  * Copyright (c) 2010 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 
 package org.jboss.tools.jst.jsp.selection.bar;
 
 import java.util.Map;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.HandlerEvent;
 import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.ISources;
 import org.eclipse.ui.commands.IElementUpdater;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.menus.UIElement;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.jspeditor.JSPMultiPageEditor;
 import org.jboss.tools.jst.jsp.preferences.IVpePreferencesPage;
 
 /**
  * Selection bar handler
  */
 public class SelectionBarHandler extends AbstractHandler implements IElementUpdater {
 	
 	public static final String COMMAND_ID = "org.jboss.tools.jst.jsp.commands.showSelectionBar"; //$NON-NLS-1$
 
 	/**
 	 * The constructor.
 	 */
 	public SelectionBarHandler() {
 		/*
 		 * Init the item state
 		 */
 		setBaseEnabled(JspEditorPlugin.getDefault().getPreferenceStore().
 			getBoolean(IVpePreferencesPage.SHOW_SELECTION_TAG_BAR));
 	}
 	
 	@Override
 	public void setEnabled(Object evaluationContext) {
 		boolean enabled = false;
 		
 		if (evaluationContext instanceof IEvaluationContext) {
 			IEvaluationContext context = (IEvaluationContext) evaluationContext;
 			Object activeEditor = context.getVariable(ISources.ACTIVE_EDITOR_NAME);
 			if(activeEditor instanceof JSPMultiPageEditor){
 				JSPMultiPageEditor jspEditor = (JSPMultiPageEditor) activeEditor;
 				if(jspEditor.getSelectedPageIndex() != jspEditor.getPreviewIndex()){
 					enabled = true;
 				}
 			}
 		}
 		
 		if (enabled != isEnabled()) {
 			setBaseEnabled(enabled);
 		}
 	}
 	
 	/**
 	 * the command has been executed, so extract extract the needed information
 	 * from the application context.
 	 */
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		/*
 		 * Change command state and save it to PreferenceStore
 		 */
		boolean visible = !HandlerUtil.toggleCommandState(event.getCommand()); 
 		JspEditorPlugin.getDefault().getPreferenceStore().setValue(
 				IVpePreferencesPage.SHOW_SELECTION_TAG_BAR,
				visible);
		/*
		 * https://issues.jboss.org/browse/JBIDE-12330
		 * Explicitly change the selection bar visibility,
		 * because listeners do not catch this event in eclipse 4.2. 
		 */
		IEditorPart activeEditor = HandlerUtil.getActiveEditorChecked(event);
		if(activeEditor instanceof JSPMultiPageEditor) {
			((JSPMultiPageEditor) activeEditor).getSelectionBar().setVisible(visible);
		}
 		return null;
 	}
 	
 	public void updateElement(UIElement element, Map parameters) {
 		fireHandlerChanged(new HandlerEvent(this, true, false));
 	}
 	
 }
