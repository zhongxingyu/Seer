 /*******************************************************************************
  * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.tests;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Random;
 
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IDiagnostics;
 import org.eclipse.tcf.services.IRunControl;
 import org.eclipse.tcf.services.IRunControl.RunControlContext;
 
 class TestAttachTerminate implements ITCFTest, IRunControl.RunControlListener {
 
     private final TCFTestSuite test_suite;
     private final RunControl test_rc;
     private final IDiagnostics diag;
     private final IRunControl rc;
     private final Random rnd = new Random();
 
     private int cnt = 0;
     private int timer = 0;
 
     private final HashSet<String> test_ctx_ids = new HashSet<String>();
 
     TestAttachTerminate(TCFTestSuite test_suite, RunControl test_rc, IChannel channel) {
         this.test_suite = test_suite;
         this.test_rc = test_rc;
         diag = channel.getRemoteService(IDiagnostics.class);
         rc = channel.getRemoteService(IRunControl.class);
     }
 
     public void start() {
         if (diag == null || rc == null) {
             test_suite.done(this, null);
         }
         else {
             rc.addListener(this);
             diag.getTestList(new IDiagnostics.DoneGetTestList() {
                 public void doneGetTestList(IToken token, Throwable error, String[] list) {
                     if (!test_suite.isActive(TestAttachTerminate.this)) return;
                     if (error != null) {
                         exit(error);
                     }
                     else if (list.length > 0) {
                         startTestContext(list[rnd.nextInt(list.length)]);
                         Protocol.invokeLater(100, new Runnable() {
                             public void run() {
                                 if (!test_suite.isActive(TestAttachTerminate.this)) return;
                                 timer++;
                                 if (test_suite.cancel) {
                                     exit(null);
                                 }
                                else if (timer < 600 * TCFTestSuite.NUM_CHANNELS) {
                                     Protocol.invokeLater(100, this);
                                 }
                                 else if (test_ctx_ids.isEmpty()) {
                                     exit(new Error("Timeout waiting for 'contextAdded' event"));
                                 }
                                 else {
                                     exit(new Error("Timeout waiting for 'contextRemoved' event. Context: " + test_ctx_ids));
                                 }
                             }
                         });
                         return;
                     }
                     exit(null);
                 }
             });
         }
     }
 
     public boolean canResume(String id) {
         return true;
     }
 
     private void startTestContext(String test_name) {
         for (int i = 0; i < 4; i++) {
             diag.runTest(test_name, new IDiagnostics.DoneRunTest() {
                 public void doneRunTest(IToken token, Throwable error, final String id) {
                     cnt--;
                     if (error != null) {
                         exit(error);
                     }
                     else if (id == null) {
                         exit(new Error("Invalid ID in Diagnostics.runTest responce"));
                     }
                     else if (test_rc.getContext(id) == null) {
                         exit(new Error("Missing 'contextAdded' event for context " + id));
                     }
                     else {
                         test_ctx_ids.add(id);
                         test_rc.cancel(id);
                     }
                 }
             });
             cnt++;
         }
     }
 
     private void exit(Throwable x) {
         if (!test_suite.isActive(this)) return;
         rc.removeListener(this);
         test_suite.done(this, x);
     }
 
     public void containerResumed(String[] context_ids) {
     }
 
     public void containerSuspended(String main_context, String pc,
             String reason, Map<String, Object> params,
             String[] suspended_ids) {
     }
 
     public void contextAdded(RunControlContext[] contexts) {
     }
 
     public void contextChanged(RunControlContext[] contexts) {
     }
 
     public void contextException(String context, String msg) {
     }
 
     public void contextRemoved(String[] context_ids) {
         for (String id : context_ids) {
             if (test_ctx_ids.remove(id)) timer = 0;
         }
         if (cnt == 0 && test_ctx_ids.isEmpty()) exit(null);
     }
 
     public void contextResumed(String context) {
     }
 
     public void contextSuspended(String context, String pc, String reason, Map<String,Object> params) {
     }
 }
