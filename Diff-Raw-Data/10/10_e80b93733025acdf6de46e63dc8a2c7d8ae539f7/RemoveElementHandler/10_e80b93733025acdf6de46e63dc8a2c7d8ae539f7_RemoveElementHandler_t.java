 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the context-sensitive command used 
  * to remove the currently selected element form the model
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.commands;
 
 import java.util.Iterator;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.jface.dialogs.MessageDialogWithToggle;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.Activator;
 import org.eclipse.mylyn.reviews.r4e.ui.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.UIUtils;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class RemoveElementHandler extends AbstractHandler {
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Method execute.
 	 * @param event ExecutionEvent
 	 * @return Object 
 	 * @throws ExecutionException
 	 * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
 	 */
 	public Object execute(ExecutionEvent event) {
 
 		final IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
 		if (!selection.isEmpty()) {
 			IR4EUIModelElement element = null;
 			MessageDialogWithToggle dialog = null;
 			for (final Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
 			    element = (IR4EUIModelElement) iterator.next();
 				Activator.Ftracer.traceInfo("Disable element " + element.getName());
 				dialog = MessageDialogWithToggle.openOkCancelConfirm(null,
 						"Disable element",
 						"Do you really want to disable this element?",
 						"Also delete from file (not supported yet)",
                         false,
                         null,
                         null);
 		    	if (dialog.getReturnCode() == Window.OK) {
 		    		try {
		    			//First close element if is it open
		    			if (element.isOpen()) {
		    				element.close();
		    				for (IR4EUIModelElement childElement: element.getChildren()) {
		    					if (null != childElement && childElement.isOpen()) {
		    						childElement.close();
		    						break;
		    					}
		    				}
		    			}
 						element.getParent().removeChildren(element, dialog.getToggleState());
 					} catch (ResourceHandlingException e) {
 						UIUtils.displayResourceErrorDialog(e);
 					} catch (OutOfSyncException e) {
 						UIUtils.displaySyncErrorDialog(e);
 					}
 		    	}
 			}
 		}
 		return null;
 	}
 }
