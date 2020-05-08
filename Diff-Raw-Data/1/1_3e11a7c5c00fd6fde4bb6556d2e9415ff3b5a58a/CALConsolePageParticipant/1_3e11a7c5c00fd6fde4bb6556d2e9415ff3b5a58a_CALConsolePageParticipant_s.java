 /*******************************************************************************
  * Copyright (c) 2007 Business Objects Software Limited and others.
  * All rights reserved. 
  * This file is made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Business Objects Software Limited - initial API and implementation
  *******************************************************************************/
 
 /*
  * CALConsolePageParticipant.java
  * Created: Jul 24, 2007
  * By: Edward Lam
  */
 
 package org.openquark.cal.eclipse.ui.console;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleConstants;
 import org.eclipse.ui.console.IConsolePageParticipant;
 import org.eclipse.ui.console.actions.CloseConsoleAction;
 import org.eclipse.ui.contexts.IContextActivation;
 import org.eclipse.ui.contexts.IContextService;
 import org.eclipse.ui.handlers.IHandlerActivation;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.part.IPageBookViewPage;
 
 
 /**
  * Page participant to activate console actions
  * @author Edward Lam
  */
 public class CALConsolePageParticipant implements IConsolePageParticipant {
 
     private IHandlerActivation fHandlerActivation;
     private IContextActivation fContextActivation;
 
     private CloseConsoleAction fCloseAction;
     private TerminateConsoleExecutionAction terminateConsoleExecutionAction;
 
     /**
      * {@inheritDoc}
      */
     public void init(IPageBookViewPage page, IConsole console) {
         fCloseAction = new CloseConsoleAction(console);
 
         IToolBarManager manager = page.getSite().getActionBars().getToolBarManager();
         manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, fCloseAction);
 
         terminateConsoleExecutionAction = new TerminateConsoleExecutionAction((CALConsole)console);
         manager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, terminateConsoleExecutionAction);
     }
 
     /**
      * {@inheritDoc}
      */
     public void dispose() {
         deactivated();
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public Object getAdapter(Class adapter) {
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public void activated() {
         // add EOF submissions
         IWorkbench workbench = PlatformUI.getWorkbench();
         IHandlerService handlerService = (IHandlerService) workbench.getAdapter(IHandlerService.class);
         
         IHandler terminateExecutionHandler = new AbstractHandler() {
            @Override
             public Object execute(ExecutionEvent event) throws ExecutionException {
                 terminateConsoleExecutionAction.run(null);
                 return null;
             }
         };
         
         fHandlerActivation = handlerService.activateHandler("org.openquark.cal.eclipse.ui.console.terminateexecution", terminateExecutionHandler); //$NON-NLS-1$
                 
         IContextService contextService = (IContextService) workbench.getAdapter(IContextService.class);
         fContextActivation = contextService.activateContext("org.openquark.cal.eclipse.ui.calEditorScope"); //$NON-NLS-1$
     }
 
     /**
      * {@inheritDoc}
      */
     public void deactivated() {
         // remove EOF submissions
         IWorkbench workbench = PlatformUI.getWorkbench();
         IHandlerService handlerService = (IHandlerService)workbench.getAdapter(IHandlerService.class);
         handlerService.deactivateHandler(fHandlerActivation);
         IContextService contextService = (IContextService)workbench.getAdapter(IContextService.class);
         contextService.deactivateContext(fContextActivation);
     }
 
 }
