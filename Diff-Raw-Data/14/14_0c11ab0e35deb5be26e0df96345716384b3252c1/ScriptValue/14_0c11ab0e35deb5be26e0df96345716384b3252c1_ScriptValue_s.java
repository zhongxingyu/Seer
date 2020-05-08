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
 import org.eclipse.debug.core.model.IVariable;
 import org.eclipse.dltk.dbgp.IDbgpProperty;
 import org.eclipse.dltk.debug.core.ScriptDebugManager;
 import org.eclipse.dltk.debug.core.eval.IScriptEvaluationCommand;
 import org.eclipse.dltk.debug.core.eval.IScriptEvaluationEngine;
 import org.eclipse.dltk.debug.core.model.AtomicScriptType;
 import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.debug.core.model.IScriptType;
 import org.eclipse.dltk.debug.core.model.IScriptTypeFactory;
 import org.eclipse.dltk.debug.core.model.IScriptValue;
 import org.eclipse.dltk.internal.debug.core.eval.ScriptEvaluationCommand;
 
 public class ScriptValue extends ScriptDebugElement implements IScriptValue {
 	final private IDbgpProperty property;
 	final private IScriptType type;
 	final protected IVariable[] variables;
 	private IScriptStackFrame frame;
 
 	public static IScriptValue createValue(IScriptStackFrame frame, IDbgpProperty property) {
 		IScriptType type = createType(frame.getDebugTarget(), property);
 		if (type.isCollection()) {			
 			return new ScriptArrayValue(frame, property, type);
 		}
 		return new ScriptValue(frame, property, type);
 	}
 	
 	ScriptValue(IScriptStackFrame frame, IDbgpProperty property, IScriptType type) {
 		this.frame = frame;
 		this.property = property;
 		this.type = type;
 		this.variables = createChildVariables(property);
 	}
 
 	private ScriptVariable[] createChildVariables(IDbgpProperty property) {
 		IDbgpProperty[] properties = property.getAvailableChildren();
 
 		ScriptVariable[] variables = new ScriptVariable[properties.length];
 		for (int i = 0; i < properties.length; ++i) {
 			variables[i] = new ScriptVariable(frame, properties[i]);
 		}
 
 		return variables;
 	}
 	
 	private static IScriptType createType(IDebugTarget target, IDbgpProperty property) {
 		IScriptType type = null;
 		final String rawType = property.getType();
 		
 		final IScriptTypeFactory factory = ScriptDebugManager.getInstance()
 				.getTypeFactoryByDebugModel(target.getModelIdentifier());
 		if (factory != null) {
 			type = factory.buildType(rawType);
 		} else {
 			type = new AtomicScriptType(rawType);
 		}
 		return type;
 	}
 
 	public String getReferenceTypeName() throws DebugException {
 		return getType().getName();
 	}
 
 	public String getValueString() throws DebugException {
 		String value = property.getValue();
 
 		if (value == null || value.length() == 0) {
 			IScriptType type = getType();
 			if (!type.isAtomic()) {
 				StringBuffer sb = new StringBuffer();
 				sb.append(type.getName());
 				String id = getInstanceId();
 				if (id != null) {
 					sb.append(" (id = " + id + ")"); // TODO add constant
 				}
 				return sb.toString();
 			}
 		}
 
 		return value;
 	}
 
 	public IVariable[] getVariables() throws DebugException {
 		return (IVariable[]) variables.clone();
 	}
 
 	public boolean hasVariables() throws DebugException {
 		return variables.length > 0;
 	}
 
 	public boolean isAllocated() throws DebugException {
 		return true;
 	}
 
 	public String toString() {
 		return property.getValue();
 	}
 
 	public IDebugTarget getDebugTarget() {
 		return frame.getDebugTarget();
 	}
 
 	public String getInstanceId() {
 		return property.getKey();
 	}
 
 	public IScriptType getType() {
 		return type;
 	}
 
 	public IScriptEvaluationCommand createEvaluationCommand(String messageTemplate,
 			IScriptThread thread) {
 		IScriptEvaluationEngine engine = thread.getEvaluationEngine();
 
		final String snippet = messageTemplate.replace("(%variable%)", getEvalName());
 
 		return new ScriptEvaluationCommand(engine, snippet, frame);
 	}
 	
 	public String getEvalName() {
 		return property.getEvalName();
 	}
 }
