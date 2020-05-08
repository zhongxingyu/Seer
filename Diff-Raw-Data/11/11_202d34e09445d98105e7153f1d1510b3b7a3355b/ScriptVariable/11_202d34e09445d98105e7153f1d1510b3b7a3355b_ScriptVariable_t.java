 package org.eclipse.dltk.debug.internal.core.model;
 
 import java.util.List;
 
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.debug.core.model.IValue;
 import org.eclipse.dltk.dbgp.IDbgpProperty;
 import org.eclipse.dltk.dbgp.commands.IDbgpCoreCommands;
 import org.eclipse.dltk.dbgp.exceptions.DbgpException;
 import org.eclipse.dltk.debug.core.model.IScriptVariable;
 
 public class ScriptVariable extends ScriptDebugElement implements
 		IScriptVariable {
 
 	private int stackLevel;
 
 	private IDbgpProperty property;
 
 	private String newValue;
 
 	private IDbgpCoreCommands core;
 
 	protected ScriptVariable(int stackLevel, IDbgpProperty property,
 			IDebugTarget target, IDbgpCoreCommands core) {
 		super(target);
 
 		this.stackLevel = stackLevel;
 		this.property = property;
 
 		this.newValue = null;
 
 		this.core = core;
 	}
 
 	public String getName() throws DebugException {
 		return property.getName();
 	}
 
 	public String getReferenceTypeName() throws DebugException {
 		return property.getType();
 	}
 
 	public IValue getValue() throws DebugException {
 		return new ScriptValue(this);
 	}
 
 	public boolean hasValueChanged() throws DebugException {
 		return newValue != null;
 	}
 
 	public void setValue(String expression) throws DebugException {
 		newValue = expression;
 		try {
 			// TODO: Set current value of IValue !!!
 			core.setPropery(property.getFullName(), stackLevel, expression);
			property.setValue(expression);
			DebugEventHelper.fireChangeEvent(this);
 		} catch (DbgpException e) {
 
 			e.printStackTrace();
 		}
 	}
 
 	public void setValue(IValue value) throws DebugException {
 		setValue(value.getValueString());
 	}
 
 	public boolean supportsValueModification() {
 		return !hasChildren() && !property.isConstant();
 	}
 
 	public boolean verifyValue(String expression) throws DebugException {
 		return expression != null;
 
 	}
 
 	public boolean verifyValue(IValue value) throws DebugException {
 		return verifyValue(value.getValueString());
 	}
 
 	public boolean isConstant() throws DebugException {
 		return property.isConstant();
 	}
 
 	public boolean hasChildren() {
 		return property.hasChildren();
 	}
 
 	public IScriptVariable[] getChildren() {
 		List properties = property.getAvailableChildren();
 		int size = properties.size();
 
 		IScriptVariable[] variables = new IScriptVariable[size];
 		for (int i = 0; i < size; ++i) {
 			variables[i] = new ScriptVariable(stackLevel,
 					(IDbgpProperty) properties.get(i), getDebugTarget(), core);
 		}
 
 		return variables;
 	}
 
 	public String getTypeString() {
 		return property.getType();
 	}
 
 	public String getValueString() {
 		if (newValue != null) {
 			return newValue;
 		} else {
 			return property.getValue();
 		}
 	}
 
 	public String toString() {
 		return property.getName();
 	}
 }
