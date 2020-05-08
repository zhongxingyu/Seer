 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.core.model;
 
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.debug.core.model.IValue;
 import org.eclipse.dltk.dbgp.IDbgpProperty;
 import org.eclipse.dltk.dbgp.IDbgpSession;
 import org.eclipse.dltk.dbgp.commands.IDbgpCoreCommands;
 import org.eclipse.dltk.dbgp.exceptions.DbgpException;
 import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.debug.core.model.IScriptVariable;
 
 public class ScriptVariable extends ScriptDebugElement implements
 		IScriptVariable {
 	private final IDebugTarget target;
 	private final IDbgpSession session;
 	private final IScriptStackFrame frame;
 	private final String name;
 	private IDbgpProperty property;
 
 	private IValue value;
 
 	public ScriptVariable(IScriptStackFrame frame, IDbgpProperty property, String name) {
 		this.target = frame.getDebugTarget();
 		this.session = ((IScriptThread) frame.getThread()).getDbgpSession();
 		this.name = name;
 		this.property = property;
 		this.frame = frame;
 	}
 
 	public IDebugTarget getDebugTarget() {
 		return target;
 	}
 
 	public synchronized IValue getValue() throws DebugException {
 		if (value == null) {
 			value = ScriptValue.createValue(frame, property);
 		}
 		return value;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getReferenceTypeName() throws DebugException {
 		return property.getType();
 	}
 
 	public boolean hasValueChanged() throws DebugException {
 		return false;
 	}
 
 	public synchronized void setValue(String expression) throws DebugException {
 		try {
 	        if (("String".equals(property.getType())) && //$NON-NLS-1$
 	            (!expression.startsWith("'") || !expression.endsWith("'")) && //$NON-NLS-1$ //$NON-NLS-2$
 	            (!expression.startsWith("\"") || !expression.endsWith("\"")))  //$NON-NLS-1$ //$NON-NLS-2$
	            expression = "\"" + expression.replace("\"", "\\\"") + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 			if (session.getCoreCommands().setProperty(property.getEvalName(),
 					frame.getLevel(), expression)) {
 				clearEvaluationManagerCache();
 				update();
 			}
 		} catch (DbgpException e) {
 			// TODO: localize
 			throw wrapDbgpException(Messages.ScriptVariable_cantAssignVariable, e);
 		}
 	}
 
 	private void clearEvaluationManagerCache() {
 		ScriptThread thread = (ScriptThread) frame.getThread();
 		thread.notifyModified();
 		
 	}
 
 	private void update() throws DbgpException {
 		this.value = null;
 
 		IDbgpCoreCommands core = session.getCoreCommands();
 		//String key = property.getKey();
 		String name = property.getEvalName();
 		
 		// TODO: Use key if provided
 		this.property = core.getProperty(name, frame.getLevel());
 
 		DebugEventHelper.fireChangeEvent(this);
 	}
 
 	public void setValue(IValue value) throws DebugException {
 		setValue(value.getValueString());
 	}
 
 	public boolean supportsValueModification() {
 		return !property.isConstant();
 	}
 
 	public boolean verifyValue(String expression) throws DebugException {
 		// TODO: perform more smart verification
 		return expression != null;
 	}
 
 	public boolean verifyValue(IValue value) throws DebugException {
 		return verifyValue(value.getValueString());
 	}
 
 	public boolean isConstant() {
 		return property.isConstant();
 	}
 
 	public String toString() {
 		return getName();
 	}
 
 	public String getId() {
 		return property.getKey();
 	}
 }
