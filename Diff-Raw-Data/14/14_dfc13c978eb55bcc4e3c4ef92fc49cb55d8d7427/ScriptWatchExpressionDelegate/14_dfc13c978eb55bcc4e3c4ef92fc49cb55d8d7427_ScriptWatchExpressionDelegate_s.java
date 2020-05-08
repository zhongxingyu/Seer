 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.internal.core.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.model.IDebugElement;
 import org.eclipse.debug.core.model.IValue;
 import org.eclipse.debug.core.model.IWatchExpressionDelegate;
 import org.eclipse.debug.core.model.IWatchExpressionListener;
 import org.eclipse.debug.core.model.IWatchExpressionResult;
 import org.eclipse.dltk.dbgp.IDbgpProperty;
 import org.eclipse.dltk.dbgp.IDbgpSession;
 import org.eclipse.dltk.dbgp.commands.IDbgpCoreCommands;
 import org.eclipse.dltk.dbgp.commands.IDbgpExtendedCommands;
 import org.eclipse.dltk.dbgp.exceptions.DbgpException;
 import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.debug.core.model.IScriptValue;
import org.eclipse.dltk.debug.core.model.IScriptVariable;
 
 public class ScriptWatchExpressionDelegate implements IWatchExpressionDelegate, IWatchExpressionResult {
 
 	private IDebugElement context;
 
 	private String expression;
 
 	private IWatchExpressionListener listener;
 
 	private IScriptValue value;
 
 	private List errors;
 
 	protected void addError(String error) {
 		errors.add(error);
 	}
 
 	protected void runEvaluation(IDbgpSession session) {
 		final IDbgpExtendedCommands extended = session.getExtendedCommands();
 		final IDbgpCoreCommands core = session.getCoreCommands();
 
 		Job job = new Job("Evaluate expression") {
 			protected IStatus run(IProgressMonitor monitor) {
 				try {
 					if (context.getDebugTarget().isTerminated())
 						return Status.OK_STATUS;
 					IDbgpProperty property = extended.evaluate(expression);
 
 					ScriptVariable variable = new ScriptVariable(context.getDebugTarget(), 0, property, core);
 					value = new ScriptValue(variable);
 
 					listener.watchEvaluationFinished(ScriptWatchExpressionDelegate.this);
 				} catch (DbgpException e) {
 					addError(e.getMessage());
 				}
 
 				return Status.OK_STATUS;
 			}
 		};
 		job.setSystem(true);
 		job.setUser(false);
 		job.schedule();
 	}
 
 	protected void runEvaluation(IScriptThread thread) {
 		runEvaluation(thread.getDbgpSession());
 	}
 
 	protected void runEvaluation(IScriptStackFrame stackFrame) {
 		runEvaluation((IScriptThread) (stackFrame.getThread()));
 	}
 
 	public ScriptWatchExpressionDelegate() {
 		this.errors = new ArrayList();
 	}
 
 	// IWatchExpressionDelegate
 	public void evaluateExpression(String expression, IDebugElement context, IWatchExpressionListener listener) {
 		this.expression = expression;
 		this.context = context;
 		this.listener = listener;
 
 		if (context instanceof IScriptThread) {
 			runEvaluation((IScriptThread) context);
 		} else if (context instanceof IScriptStackFrame) {
 			runEvaluation((IScriptStackFrame) context);
 		} else {
 			addError("Unknown expression contex");
 		}
 	}
 
 	// IWatchExpressionResult
 	public IValue getValue() {
 		return value;
 	}
 
 	public boolean hasErrors() {
 		return !errors.isEmpty();
 	}
 
 	public String[] getErrorMessages() {
 		return (String[]) errors.toArray(new String[errors.size()]);
 	}
 
 	public String getExpressionText() {
 		return expression;
 	}
 
 	public DebugException getException() {
 		return null;
 	}
 }
