 package org.eclipse.dltk.internal.debug.core.model;
 
 import java.util.Arrays;
 
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.model.IIndexedValue;
 import org.eclipse.debug.core.model.IVariable;
 import org.eclipse.dltk.dbgp.IDbgpProperty;
 import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
 import org.eclipse.dltk.debug.core.model.IScriptType;
 import org.eclipse.dltk.debug.core.model.IScriptValue;
 
 public class ScriptArrayValue extends ScriptValue implements IScriptValue,
 		IIndexedValue {
 
 	ScriptArrayValue(IScriptStackFrame frame, IDbgpProperty property, IScriptType type) {
 		super(frame, property, type);
 	}
 
 	public int getInitialOffset() {
 		return 0;
 	}
 
 	public int getSize() throws DebugException {
 		return variables.length;
 	}
 
 	public IVariable getVariable(int offset) throws DebugException {
 		return variables[offset];
 	}
 
 	public IVariable[] getVariables(int offset, int length)
 			throws DebugException {
		IVariable[] newa = new IVariable[length];
		System.arraycopy(variables, offset, newa, 0, length);
		return newa;
//		return (IVariable[]) Arrays.copyOfRange(variables, offset, offset
//				+ length);
 	}
 
 	public Object getAdapter(Class adapter) {
 		if (adapter == IIndexedValue.class)
 			return this;
 		return super.getAdapter(adapter);
 	}
 
 }
