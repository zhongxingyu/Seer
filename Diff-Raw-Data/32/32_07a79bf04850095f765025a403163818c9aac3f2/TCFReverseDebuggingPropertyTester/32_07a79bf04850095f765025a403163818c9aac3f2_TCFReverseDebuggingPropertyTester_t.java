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
 
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.tcf.internal.cdt.ui.Activator;
 import org.eclipse.tcf.internal.debug.ui.model.TCFNode;
 import org.eclipse.tcf.util.TCFTask;
 
 
 /**
  * Tester for property "org.eclipse.cdt.debug.ui.isReverseDebuggingEnabled"
  * to enable reverse run control actions.
  */
 public class TCFReverseDebuggingPropertyTester extends PropertyTester {
 
     private static final String ENABLED = "isReverseDebuggingEnabled"; //$NON-NLS-1$
 
     public boolean test(Object context, String property, Object[] args, Object expectedValue) {
         if (!ENABLED.equals(property)) return false;

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
