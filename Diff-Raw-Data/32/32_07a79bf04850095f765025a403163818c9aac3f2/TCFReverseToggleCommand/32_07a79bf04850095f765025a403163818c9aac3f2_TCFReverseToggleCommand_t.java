 /*******************************************************************************
 * Copyright (c) 2010, 2012 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.cdt.ui.commands;
 
 import java.util.concurrent.ExecutionException;
 
 import org.eclipse.cdt.debug.core.model.IReverseToggleHandler;
 import org.eclipse.debug.core.commands.IDebugCommandRequest;
 import org.eclipse.debug.core.commands.IEnabledStateRequest;
 import org.eclipse.tcf.internal.cdt.ui.Activator;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.util.TCFTask;
 
 /**
  * Toggles enablement for reverse run control support.
  */
 public class TCFReverseToggleCommand implements IReverseToggleHandler {
 
     public void canExecute(IEnabledStateRequest request) {
         request.setEnabled(true);
         request.done();
     }
 
     public boolean execute(final IDebugCommandRequest request) {
         if (request.getElements().length != 0 && request.getElements()[0] instanceof TCFNode) {
            final TCFNode node = (TCFNode)request.getElements()[0];
             Protocol.invokeLater(new Runnable() {
                 public void run() {
                    boolean enabled = node.getModel().isReverseDebugEnabled();
                    node.getModel().setReverseDebugEnabled(!enabled);
                     request.done();
                 };
             });
         } else {
             request.done();
         }
         return true;
     }
 
     public boolean toggleNeedsUpdating() {
         return true;
     }
 
     public boolean isReverseToggled(Object context) {
         if (context instanceof TCFNode) {
            final TCFNode node = (TCFNode)context;
             try {
                 return new TCFTask<Boolean>() {
                     public void run() {
                        done(node.getModel().isReverseDebugEnabled());
                     };
                 }.get();
             }
             catch (InterruptedException e) {
                 Activator.log(e);
                 return false;
             }
             catch (ExecutionException e) {
                 Activator.log(e);
                 return false;
             }
         }
         return false;
     }
 
 }
