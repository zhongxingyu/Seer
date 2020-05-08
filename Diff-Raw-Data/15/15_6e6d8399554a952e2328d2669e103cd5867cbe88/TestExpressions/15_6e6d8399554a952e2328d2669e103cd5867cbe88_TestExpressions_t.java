 /*******************************************************************************
  * Copyright (c) 2008, 2012 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.tests;
 
 import java.math.BigInteger;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IErrorReport;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.protocol.JSON;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IBreakpoints;
 import org.eclipse.tcf.services.IDiagnostics;
 import org.eclipse.tcf.services.IExpressions;
 import org.eclipse.tcf.services.IRunControl;
 import org.eclipse.tcf.services.IStackTrace;
 import org.eclipse.tcf.services.ISymbols;
 
 class TestExpressions implements ITCFTest,
     IRunControl.RunControlListener, IExpressions.ExpressionsListener, IBreakpoints.BreakpointsListener {
 
     private final TCFTestSuite test_suite;
     private final RunControl test_rc;
     private final IDiagnostics diag;
     private final IExpressions expr;
     private final ISymbols syms;
     private final IStackTrace stk;
     private final IRunControl rc;
     private final IBreakpoints bp;
     private final Random rnd = new Random();
 
     private String test_id;
     private String bp_id;
     private boolean bp_ok;
     private IDiagnostics.ISymbol sym_func3;
     private String test_ctx_id;
     private String process_id;
     private String thread_id;
     private boolean run_to_bp_done;
     private boolean loc_info_done;
     private boolean no_cpp;
     private boolean test_done;
     private boolean cancel_test_sent;
     private IRunControl.RunControlContext test_ctx;
     private IRunControl.RunControlContext thread_ctx;
     private String suspended_pc;
     private boolean waiting_suspend;
     private String[] stack_trace;
     private IStackTrace.StackTraceContext[] stack_frames;
     private String[] local_var_expr_ids;
     private final Map<String,String> global_var_ids = new HashMap<String,String>();
     private final Map<String,String> local_var_ids = new HashMap<String,String>();
     private final Map<String,SymbolLocation> global_var_location = new HashMap<String,SymbolLocation>();
     private final Map<String,SymbolLocation> local_var_location = new HashMap<String,SymbolLocation>();
     private final Map<String,IExpressions.Expression> expr_ctx = new HashMap<String,IExpressions.Expression>();
     private final Map<String,IExpressions.Value> expr_val = new HashMap<String,IExpressions.Value>();
     private final Map<String,ISymbols.Symbol> expr_sym = new HashMap<String,ISymbols.Symbol>();
     private final Map<String,String[]> expr_chld = new HashMap<String,String[]>();
     private final Set<String> expr_to_dispose = new HashSet<String>();
     private int timer = 0;
 
     private static String[] global_var_names = {
         "tcf_test_char",
         "tcf_test_short",
         "tcf_test_long",
         "tcf_cpp_test_bool",
     };
 
     private static String[] test_expressions = {
         "func2_local1",
         "func2_local2",
         "func2_local3",
         "func2_local1 == func2_local1",
         "func2_local1 != func2_local2",
         "1.34 == 1.34",
         "1.34 != 1.35",
         "1 ? 1 : 0",
         "!func2_local1 ? 0 : 1",
         "(0 || 0) == 0",
         "(0 || func2_local1) == 1",
         "(func2_local1 || 0) == 1",
         "(func2_local1 || func2_local1) == 1",
         "(0 && 0) == 0",
         "(0 && func2_local1) == 0",
         "(func2_local1 && 0) == 0",
         "(func2_local1 && func2_local1) == 1",
         "(func2_local1 | func2_local2) == 3",
         "(func2_local1 & func2_local2) == 0",
         "(func2_local1 ^ func2_local2) == 3",
         "(func2_local1 < func2_local2)",
         "(func2_local1 <= func2_local2)",
         "!(func2_local1 > func2_local2)",
         "!(func2_local1 >= func2_local2)",
         "(func2_local1 < 1.1)",
         "(func2_local1 <= 1.1)",
         "!(func2_local1 > 1.1)",
         "!(func2_local1 >= 1.1)",
         "(func2_local2 << 2) == 8",
         "(func2_local2 >> 1) == 1",
         "+func2_local2 == 2",
         "-func2_local2 == -2",
         "(short)(int)(long)((char *)func2_local2 + 1) == 3",
         "((func2_local1 + func2_local2) * 2 - 2) / 2 == 2",
         "func2_local3.f_struct->f_struct->f_struct == &func2_local3",
         "(char *)func2_local3.f_struct",
         "(char[4])func2_local3.f_struct",
         "&((test_struct *)0)->f_float",
         "&((struct test_struct *)0)->f_float",
         "tcf_test_func3",
         "&tcf_test_func3",
         "tcf_test_array + 10",
         "*(tcf_test_array + 10) | 1",
         "&*(char *)(int *)0 == 0",
         "(bool)0 == false",
         "(bool)1 == true",
         "sizeof(bool) == sizeof true",
         "tcf_cpp_test_class::s_int == 1",
         "sizeof tcf_cpp_test_class::s_int == sizeof signed",
         "tcf_cpp_test_class::tcf_cpp_test_class_nested::s_int == 2",
         "tcf_cpp_test_class_extension::tcf_cpp_test_class_nested::s_int == 2",
         "enum_val1 == 1 && enum_val2 == 2 && enum_val3 == 3",
     };
 
     @SuppressWarnings("unused")
     private static class SymbolLocation {
         Exception error;
         Map<String,Object> props;
     }
 
     TestExpressions(TCFTestSuite test_suite, RunControl test_rc, IChannel channel) {
         this.test_suite = test_suite;
         this.test_rc = test_rc;
         diag = channel.getRemoteService(IDiagnostics.class);
         expr = channel.getRemoteService(IExpressions.class);
         syms = channel.getRemoteService(ISymbols.class);
         stk = channel.getRemoteService(IStackTrace.class);
         rc = channel.getRemoteService(IRunControl.class);
         bp = channel.getRemoteService(IBreakpoints.class);
     }
 
     public void start() {
         if (diag == null || expr == null || stk == null || rc == null || bp == null) {
             test_suite.done(this, null);
         }
         else {
             expr.addListener(this);
             rc.addListener(this);
             bp.addListener(this);
             diag.getTestList(new IDiagnostics.DoneGetTestList() {
                 public void doneGetTestList(IToken token, Throwable error, String[] list) {
                     if (!test_suite.isActive(TestExpressions.this)) return;
                     if (error != null) {
                         exit(error);
                     }
                     else {
                         if (list.length > 0) {
                             test_id = list[rnd.nextInt(list.length)];
                             runTest();
                             Protocol.invokeLater(100, new Runnable() {
                                 public void run() {
                                     if (!test_suite.isActive(TestExpressions.this)) return;
                                     timer++;
                                     if (test_suite.cancel) {
                                         exit(null);
                                     }
                                     else if (timer < 600) {
                                         if (test_done && !cancel_test_sent) {
                                             test_rc.cancel(test_ctx_id);
                                             cancel_test_sent = true;
                                         }
                                         Protocol.invokeLater(100, this);
                                     }
                                     else if (test_ctx_id == null) {
                                         exit(new Error("Timeout waiting for reply of Diagnostics.runTest command"));
                                     }
                                     else {
                                         exit(new Error("Missing 'contextRemoved' event for " + test_ctx_id));
                                     }
                                 }
                             });
                             return;
                         }
                         exit(null);
                     }
                 }
             });
         }
     }
 
     public boolean canResume(String id) {
         if (test_ctx_id != null && thread_ctx == null) return false;
         if (thread_ctx != null && !test_done) {
             assert thread_ctx.getID().equals(thread_id);
             IRunControl.RunControlContext ctx = test_rc.getContext(id);
             if (ctx == null) return false;
             String grp = ctx.getRCGroup();
             if (id.equals(thread_id) || grp != null && grp.equals(thread_ctx.getRCGroup())) {
                 if (run_to_bp_done) return false;
                 if (sym_func3 == null) return false;
                 if (suspended_pc == null) return false;
                 BigInteger pc0 = JSON.toBigInteger(sym_func3.getValue());
                 BigInteger pc1 = new BigInteger(suspended_pc);
                 if (pc0.equals(pc1)) return false;
             }
         }
         return true;
     }
 
     @SuppressWarnings("unchecked")
     private void runTest() {
         timer = 0;
         if (bp_id == null) {
             bp.set(null, new IBreakpoints.DoneCommand() {
                 public void doneCommand(IToken token, Exception error) {
                     if (error != null) {
                         exit(error);
                     }
                     else {
                         bp_id = "TestExpressionsBP";
                         runTest();
                     }
                 }
             });
             return;
         }
         if (!bp_ok) {
             Map<String,Object> m = new HashMap<String,Object>();
             m.put(IBreakpoints.PROP_ID, bp_id);
             m.put(IBreakpoints.PROP_ENABLED, Boolean.TRUE);
             m.put(IBreakpoints.PROP_LOCATION, "tcf_test_func3");
             bp.set(new Map[]{ m }, new IBreakpoints.DoneCommand() {
                 public void doneCommand(IToken token, Exception error) {
                     if (error != null) {
                         exit(error);
                     }
                     else {
                         bp_ok = true;
                         runTest();
                     }
                 }
             });
             return;
         }
         if (test_ctx_id == null) {
             diag.runTest(test_id, new IDiagnostics.DoneRunTest() {
                 public void doneRunTest(IToken token, Throwable error, String id) {
                     if (error != null) {
                         exit(error);
                     }
                     else if (id == null) {
                         exit(new Exception("Test context ID must not be null"));
                     }
                     else if (test_rc.getContext(id) == null) {
                         exit(new Exception("Missing context added event"));
                     }
                     else {
                         test_ctx_id = id;
                         runTest();
                     }
                 }
             });
             return;
         }
         if (test_ctx == null) {
             rc.getContext(test_ctx_id, new IRunControl.DoneGetContext() {
                 public void doneGetContext(IToken token, Exception error, IRunControl.RunControlContext ctx) {
                     if (error != null) {
                         exit(error);
                     }
                     else if (ctx == null) {
                         exit(new Exception("Invalid test execution context"));
                     }
                     else {
                         test_ctx = ctx;
                         process_id = test_ctx.getProcessID();
                         if (test_ctx.hasState()) thread_id = test_ctx_id;
                         runTest();
                     }
                 }
             });
             return;
         }
         if (thread_id == null) {
             rc.getChildren(process_id, new IRunControl.DoneGetChildren() {
                 public void doneGetChildren(IToken token, Exception error, String[] ids) {
                     if (error != null) {
                         exit(error);
                     }
                     else if (ids == null || ids.length == 0) {
                         exit(new Exception("Test process has no threads"));
                     }
                     else if (ids.length != 1) {
                         exit(new Exception("Test process has too many threads"));
                     }
                     else {
                         thread_id = ids[0];
                         runTest();
                     }
                 }
             });
             return;
         }
         if (thread_ctx == null) {
             rc.getContext(thread_id, new IRunControl.DoneGetContext() {
                 public void doneGetContext(IToken token, Exception error, IRunControl.RunControlContext ctx) {
                     if (error != null) {
                         exit(error);
                     }
                     else if (ctx == null || !ctx.hasState()) {
                         exit(new Exception("Invalid thread context"));
                     }
                     else {
                         thread_ctx = ctx;
                         runTest();
                     }
                 }
             });
             return;
         }
         if (suspended_pc == null) {
             thread_ctx.getState(new IRunControl.DoneGetState() {
                 public void doneGetState(IToken token, Exception error,
                         boolean suspended, String pc, String reason,
                         Map<String,Object> params) {
                     if (error != null) {
                         exit(new Exception("Cannot get context state", error));
                     }
                     else if (!suspended) {
                         waiting_suspend = true;
                     }
                     else if (pc == null || pc.length() == 0 || pc.equals("0")) {
                         exit(new Exception("Invalid context PC"));
                     }
                     else {
                         suspended_pc = pc;
                         runTest();
                     }
                 }
             });
             return;
         }
         if (sym_func3 == null) {
             diag.getSymbol(process_id, "tcf_test_func3", new IDiagnostics.DoneGetSymbol() {
                 public void doneGetSymbol(IToken token, Throwable error, IDiagnostics.ISymbol symbol) {
                     if (error != null) {
                         exit(error);
                     }
                     else if (symbol == null) {
                         exit(new Exception("Symbol must not be null: tcf_test_func3"));
                     }
                     else {
                         sym_func3 = symbol;
                         runTest();
                     }
                 }
             });
             return;
         }
         if (!run_to_bp_done) {
             BigInteger pc0 = JSON.toBigInteger(sym_func3.getValue());
             BigInteger pc1 = new BigInteger(suspended_pc);
             if (!pc0.equals(pc1)) {
                 waiting_suspend = true;
                 return;
             }
             run_to_bp_done = true;
         }
         assert test_done || !canResume(thread_id);
         if (stack_trace == null) {
             stk.getChildren(thread_id, new IStackTrace.DoneGetChildren() {
                 public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                     if (error != null) {
                         exit(error);
                     }
                     else if (context_ids == null || context_ids.length < 2) {
                         exit(new Exception("Invalid stack trace"));
                     }
                     else {
                         stack_trace = context_ids;
                         runTest();
                     }
                 }
             });
             return;
         }
         if (stack_frames == null) {
             stk.getContext(stack_trace, new IStackTrace.DoneGetContext() {
                 public void doneGetContext(IToken token, Exception error, IStackTrace.StackTraceContext[] frames) {
                     if (error != null) {
                         exit(error);
                     }
                     else if (frames == null || frames.length != stack_trace.length) {
                         exit(new Exception("Invalid stack trace"));
                     }
                     else {
                         stack_frames = frames;
                         runTest();
                     }
                 }
             });
             return;
         }
         if (local_var_expr_ids == null) {
             expr.getChildren(stack_trace[stack_trace.length - 2], new IExpressions.DoneGetChildren() {
                 public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                     if (error != null || context_ids == null) {
                         // Need to continue tests even if local variables info is not available.
                         // TODO: need to distinguish absence of debug info from other errors.
                         local_var_expr_ids = new String[0];
                         runTest();
                     }
                     else {
                         local_var_expr_ids = context_ids;
                         runTest();
                     }
                 }
             });
             return;
         }
         for (final String id : local_var_expr_ids) {
             if (expr_ctx.get(id) == null) {
                 expr.getContext(id, new IExpressions.DoneGetContext() {
                     public void doneGetContext(IToken token, Exception error, IExpressions.Expression ctx) {
                         if (error != null) {
                             exit(error);
                         }
                         else {
                             expr_ctx.put(id, ctx);
                             local_var_ids.put(id, ctx.getSymbolID());
                             runTest();
                         }
                     }
                 });
                 return;
             }
         }
         if (syms != null && local_var_expr_ids.length > 0) {
             for (final String nm : global_var_names) {
                 if (!global_var_ids.containsKey(nm)) {
                     syms.find(process_id, new BigInteger(suspended_pc), nm, new ISymbols.DoneFind() {
                         public void doneFind(IToken token, Exception error, String symbol_id) {
                             if (error != null) {
                                 if (nm.startsWith("tcf_cpp_") && error instanceof IErrorReport &&
                                         ((IErrorReport)error).getErrorCode() == IErrorReport.TCF_ERROR_SYM_NOT_FOUND) {
 
                                     global_var_ids.put(nm, null);
                                     no_cpp = true;
                                     runTest();
                                     return;
                                 }
                                 exit(error);
                             }
                             else if (symbol_id == null) {
                                 exit(new Exception("Invalid symbol ID"));
                             }
                             else {
                                 global_var_ids.put(nm, symbol_id);
                                 runTest();
                             }
                         }
                     });
                     return;
                 }
             }
         }
         if (syms != null && !loc_info_done) {
             for (final String id : global_var_ids.values()) {
                 if (id != null && global_var_location.get(id) == null) {
                     syms.getLocationInfo(id, new ISymbols.DoneGetLocationInfo() {
                         public void doneGetLocationInfo(IToken token, Exception error, Map<String, Object> props) {
                             SymbolLocation l = new SymbolLocation();
                             l.error = error;
                             l.props = props;
                             global_var_location.put(id, l);
                             if (error != null) {
                                 if (error instanceof IErrorReport &&
                                         ((IErrorReport)error).getErrorCode() == IErrorReport.TCF_ERROR_INV_COMMAND) {
                                     runTest();
                                     return;
                                 }
                                 exit(error);
                             }
                            else if (props == null) {
                                exit(new Exception("Invalid symbol location info: props = null"));
                             }
                             else {
                                List<Object> cmds = (List<Object>)props.get(ISymbols.LOC_VALUE_CMDS);
                                if (cmds == null || cmds.size() == 0) {
                                    exit(new Exception("Invalid symbol location info: ValueCmds = null"));
                                }
                                else {
                                    runTest();
                                }
                             }
                         }
                     });
                     return;
                 }
             }
             for (final String id : local_var_ids.values()) {
                 if (id != null && local_var_location.get(id) == null) {
                     syms.getLocationInfo(id, new ISymbols.DoneGetLocationInfo() {
                         public void doneGetLocationInfo(IToken token, Exception error, Map<String, Object> props) {
                             SymbolLocation l = new SymbolLocation();
                             l.error = error;
                             l.props = props;
                             local_var_location.put(id, l);
                             List<Object> cmds = (List<Object>)props.get(ISymbols.LOC_VALUE_CMDS);
                             if (error != null) {
                                 if (error instanceof IErrorReport &&
                                         ((IErrorReport)error).getErrorCode() == IErrorReport.TCF_ERROR_INV_COMMAND) {
                                     runTest();
                                     return;
                                 }
                                 exit(error);
                             }
                             else if (cmds == null || cmds.size() == 0) {
                                 exit(new Exception("Invalid symbol location info"));
                             }
                             else {
                                 runTest();
                             }
                         }
                     });
                     return;
                 }
             }
             loc_info_done = true;
         }
         for (final String txt : test_expressions) {
             if (local_var_expr_ids.length == 0) {
                 // Debug info not available
                 if (txt.indexOf("func2_local") >= 0) continue;
                 if (txt.indexOf("test_struct") >= 0) continue;
                 if (txt.indexOf("tcf_test_array") >= 0) continue;
                 if (txt.indexOf("(char *)") >= 0) continue;
                 if (txt.indexOf("enum_val") >= 0) continue;
             }
             if (local_var_expr_ids.length == 0 || no_cpp) {
                 // Agent is not build with C++ compiler
                 if (txt.indexOf("tcf_cpp_test") >= 0) continue;
                 if (txt.indexOf("(bool)") >= 0) continue;
             }
             if (expr_ctx.get(txt) == null) {
                 expr.create(stack_trace[stack_trace.length - 2], null, txt, new IExpressions.DoneCreate() {
                     public void doneCreate(IToken token, Exception error, IExpressions.Expression ctx) {
                         if (error != null) {
                             exit(error);
                         }
                         else {
                             expr_to_dispose.add(ctx.getID());
                             expr_ctx.put(txt, ctx);
                             runTest();
                         }
                     }
                 });
                 return;
             }
         }
         for (final String id : local_var_expr_ids) {
             if (expr_val.get(id) == null) {
                 expr.evaluate(id, new IExpressions.DoneEvaluate() {
                     public void doneEvaluate(IToken token, Exception error, IExpressions.Value ctx) {
                         if (error != null) {
                             exit(error);
                         }
                         else {
                             expr_val.put(id, ctx);
                             runTest();
                         }
                     }
                 });
                 return;
             }
         }
         for (final String id : expr_ctx.keySet()) {
             if (expr_val.get(id) == null) {
                 expr.evaluate(expr_ctx.get(id).getID(), new IExpressions.DoneEvaluate() {
                     public void doneEvaluate(IToken token, Exception error, IExpressions.Value ctx) {
                         if (error != null) {
                             exit(error);
                         }
                         else {
                             expr_val.put(id, ctx);
                             byte[] arr = ctx.getValue();
                             boolean b = false;
                             for (byte x : arr) {
                                 if (x != 0) b = true;
                             }
                             if (!b) exit(new Exception("Invalid value of expression \"" + id + "\""));
                             runTest();
                         }
                     }
                 });
                 return;
             }
         }
         if (syms != null) {
             for (final String id : expr_val.keySet()) {
                 if (expr_sym.get(id) == null) {
                     IExpressions.Value v = expr_val.get(id);
                     String type_id = v.getTypeID();
                     if (type_id != null) {
                         syms.getContext(type_id, new ISymbols.DoneGetContext() {
                             public void doneGetContext(IToken token, Exception error, ISymbols.Symbol ctx) {
                                 if (error != null) {
                                     exit(error);
                                 }
                                 else if (ctx == null) {
                                     exit(new Exception("Symbol.getContext returned null"));
                                 }
                                 else {
                                     expr_sym.put(id, ctx);
                                     runTest();
                                 }
                             }
                         });
                         return;
                     }
                 }
             }
             for (final String id : expr_sym.keySet()) {
                 if (expr_chld.get(id) == null) {
                     ISymbols.Symbol sym = expr_sym.get(id);
                     syms.getChildren(sym.getID(), new ISymbols.DoneGetChildren() {
                         public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                             if (error != null) {
                                 exit(error);
                             }
                             else {
                                 if (context_ids == null) context_ids = new String[0];
                                 expr_chld.put(id, context_ids);
                                 runTest();
                             }
                         }
                     });
                     return;
                 }
             }
         }
         for (final String id : expr_to_dispose) {
             expr.dispose(id, new IExpressions.DoneDispose() {
                 public void doneDispose(IToken token, Exception error) {
                     if (error != null) {
                         exit(error);
                     }
                     else {
                         expr_to_dispose.remove(id);
                         runTest();
                     }
                 }
             });
             return;
         }
         test_done = true;
     }
 
     private void exit(Throwable x) {
         if (!test_suite.isActive(this)) return;
         expr.removeListener(this);
         bp.removeListener(this);
         rc.removeListener(this);
         test_suite.done(this, x);
     }
 
     //--------------------------- Run Control listener ---------------------------//
 
     public void containerResumed(String[] context_ids) {
         for (String id : context_ids) contextResumed(id);
     }
 
     public void containerSuspended(String context, String pc, String reason,
             Map<String,Object> params, String[] suspended_ids) {
         for (String id : suspended_ids) {
             contextSuspended(id, null, null, null);
         }
     }
 
     public void contextAdded(IRunControl.RunControlContext[] contexts) {
     }
 
     public void contextChanged(IRunControl.RunControlContext[] contexts) {
     }
 
     public void contextException(String context, String msg) {
         if (test_done) return;
         IRunControl.RunControlContext ctx = test_rc.getContext(context);
         if (ctx != null) {
             String p = ctx.getParentID();
             String c = ctx.getCreatorID();
             if (!test_ctx_id.equals(c) && !test_ctx_id.equals(p)) return;
         }
         exit(new Exception("Context exception: " + msg));
     }
 
     public void contextRemoved(String[] context_ids) {
         for (String id : context_ids) {
             if (id.equals(test_ctx_id)) {
                 if (test_done) {
                     bp.set(null, new IBreakpoints.DoneCommand() {
                         public void doneCommand(IToken token, Exception error) {
                             exit(error);
                         }
                     });
                 }
                 else {
                     exit(new Exception("Test process exited too soon"));
                 }
                 return;
             }
         }
     }
 
     public void contextResumed(String id) {
         if (id.equals(thread_id)) {
             if (run_to_bp_done && !test_done) {
                 assert thread_ctx != null;
                 assert !canResume(thread_id);
                 exit(new Exception("Unexpected contextResumed event: " + id));
             }
             suspended_pc = null;
         }
     }
 
     public void contextSuspended(String id, String pc, String reason, Map<String,Object> params) {
         assert id != null;
         if (id.equals(thread_id) && waiting_suspend) {
             suspended_pc = pc;
             waiting_suspend = false;
             runTest();
         }
     }
 
     //--------------------------- Expressions listener ---------------------------//
 
     public void valueChanged(String id) {
     }
 
     //--------------------------- Breakpoints listener ---------------------------//
 
     @SuppressWarnings("unchecked")
     public void breakpointStatusChanged(String id, Map<String,Object> status) {
         if (id.equals(bp_id) && process_id != null && !test_done) {
             String s = (String)status.get(IBreakpoints.STATUS_ERROR);
             if (s != null) exit(new Exception("Invalid BP status: " + s));
             Collection<Map<String,Object>> list = (Collection<Map<String,Object>>)status.get(IBreakpoints.STATUS_INSTANCES);
             if (list == null) return;
             String err = null;
             for (Map<String,Object> map : list) {
                 String ctx = (String)map.get(IBreakpoints.INSTANCE_CONTEXT);
                 if (process_id.equals(ctx) && map.get(IBreakpoints.INSTANCE_ERROR) != null)
                     err = (String)map.get(IBreakpoints.INSTANCE_ERROR);
             }
             if (err != null) exit(new Exception("Invalid BP status: " + err));
         }
     }
 
     public void contextAdded(Map<String,Object>[] bps) {
     }
 
     public void contextChanged(Map<String,Object>[] bps) {
     }
 }
