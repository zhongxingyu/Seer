 package com.dafrito.rfe.script.values;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.dafrito.rfe.gui.debug.DebugString;
 import com.dafrito.rfe.gui.debug.Debugger;
 import com.dafrito.rfe.inspect.Nodeable;
 import com.dafrito.rfe.script.exceptions.Exception_Nodeable;
 import com.dafrito.rfe.script.operations.ScriptExecutable;
 import com.dafrito.rfe.script.parsing.Referenced;
 import com.dafrito.rfe.script.parsing.ScriptKeywordType;
 
 public class ScriptFunction implements Nodeable, ScriptFunction_Abstract {
 	public static boolean areParametersConvertible(List<ScriptValue> parameters, List<ScriptValue> values) {
 		assert Debugger.openNode("Parameter-Convertibility Tests", "Parameter-Convertibility Test");
 		assert Debugger.addNode("Keys must be convertible to their function-param socket counterpart.");
 		assert Debugger.addSnapNode("Function-Parameter Sockets", parameters);
 		assert Debugger.addSnapNode("Parameter Keys", values);
 		if (values.size() != parameters.size()) {
 			assert Debugger.closeNode("Parameter sizes do not match (" + values.size() + " and " + parameters.size() + ")");
 			return false;
 		}
 		for (int i = 0; i < values.size(); i++) {
 			ScriptValue param = parameters.get(i);
 			if (param == null) {
				throw new NullPointerException("parameter must not be null");
 			}
 			ScriptValue value = values.get(i);
 			if (value != null && !value.isConvertibleTo(param.getType())) {
 				assert Debugger.closeNode("Parameters are not equal (" + param.getType() + " and " + value.getType() + ")");
 				return false;
 			}
 		}
 		assert Debugger.closeNode("Parameters match.");
 		return true;
 	}
 
 	public static boolean areParametersEqual(List<ScriptValue> source, List<ScriptValue> list) {
 		assert Debugger.openNode("Parameter-Equality Tests", "Parameter-Equality Test");
 		assert Debugger.addSnapNode("Function-Parameter Sockets", source);
 		assert Debugger.addSnapNode("Parameter Keys", list);
 		if (list.size() != source.size()) {
 			assert Debugger.closeNode("Parameter sizes do not match (" + list.size() + " and " + source.size() + ")");
 			return false;
 		}
 		for (int i = 0; i < list.size(); i++) {
 			if (!source.get(i).getType().equals(list.get(i).getType())) {
 				assert Debugger.closeNode("Parameters are not equal (" + source.get(i).getType() + " and " + list.get(i).getType() + ")");
 				return false;
 			}
 		}
 		assert Debugger.closeNode("Parameters match.");
 		return true;
 	}
 
 	public static String getDisplayableFunctionName(String name) {
 		if (name == null || name.equals("")) {
 			return "constructor";
 		}
 		return name;
 	}
 
 	private ScriptValueType type;
 	private List<ScriptValue> params;
 	private ScriptKeywordType permission;
 	private ScriptValue returnValue;
 
 	private boolean isAbstract, isStatic;
 
 	private List<ScriptExecutable> expressions = new LinkedList<ScriptExecutable>();
 
 	public ScriptFunction(ScriptValueType returnType, List<ScriptValue> params, ScriptKeywordType permission, boolean isAbstract, boolean isStatic) {
 		this.type = returnType;
 		this.params = params;
 		this.permission = permission;
 		this.isAbstract = isAbstract;
 		this.isStatic = isStatic;
 	}
 
 	@Override
 	public void addExpression(ScriptExecutable exp) throws Exception_Nodeable {
 		assert exp != null;
 		this.expressions.add(exp);
 	}
 
 	@Override
 	public void addExpressions(Collection<ScriptExecutable> list) throws Exception_Nodeable {
 		for (ScriptExecutable exec : list) {
 			this.addExpression(exec);
 		}
 	}
 
 	@Override
 	public boolean areParametersConvertible(List<ScriptValue> list) {
 		return areParametersConvertible(this.getParameters(), list);
 	}
 
 	@Override
 	public boolean areParametersEqual(List<ScriptValue> list) {
 		return areParametersEqual(this.getParameters(), list);
 	}
 
 	@Override
 	public void execute(Referenced ref, List<ScriptValue> valuesGiven) throws Exception_Nodeable {
 		String currNode = "Executing Function Expressions (" + this.expressions.size() + " expressions)";
 		assert Debugger.openNode("Function Expression Executions", currNode);
 		if (valuesGiven != null && valuesGiven.size() > 0) {
 			assert Debugger.openNode("Assigning Initial Parameters (" + valuesGiven.size() + " parameter(s))");
 			assert this.areParametersConvertible(valuesGiven) : "Parameters-convertible test failed in execute";
 			for (int i = 0; i < this.getParameters().size(); i++) {
 				this.getParameters().get(i).setValue(ref, valuesGiven.get(i));
 			}
 			assert Debugger.closeNode();
 		}
 		for (ScriptExecutable exec : this.expressions) {
 			exec.execute();
 			if (exec instanceof Returnable && ((Returnable) exec).shouldReturn()) {
 				this.setReturnValue(exec.getDebugReference(), ((Returnable) exec).getReturnValue());
 				assert Debugger.ensureCurrentNode("Executing Function Expressions (" + this.expressions.size() + " expressions)");
 				assert Debugger.closeNode();
 				return;
 			}
 		}
 		assert Debugger.ensureCurrentNode(currNode);
 		assert Debugger.closeNode();
 	}
 
 	@Override
 	public List<ScriptValue> getParameters() {
 		return this.params;
 	}
 
 	@Override
 	public ScriptKeywordType getPermission() {
 		return this.permission;
 	}
 
 	@Override
 	public ScriptValueType getReturnType() {
 		return this.type;
 	}
 
 	@Override
 	public ScriptValue getReturnValue() {
 		return this.returnValue;
 	}
 
 	@Override
 	public boolean isAbstract() {
 		return this.isAbstract;
 	}
 
 	@Override
 	public boolean isStatic() {
 		return this.isStatic;
 	}
 
 	@Override
 	public void nodificate() {
 		assert Debugger.openNode("Script-Function (Returning " + this.type.getName() + ")");
 		if (this.params != null && this.params.size() > 0) {
 			assert Debugger.addSnapNode("Parameters: " + this.params.size() + " parameter(s)", this.params);
 		}
 		if (this.expressions != null && this.expressions.size() > 0) {
 			assert Debugger.addSnapNode("Expressions: " + this.expressions.size() + " expression(s)", this.expressions);
 		}
 		if (this.permission == null) {
 			Debugger.addNode(DebugString.PERMISSIONNULL);
 		} else {
 			switch (this.permission) {
 			case PRIVATE:
 				assert Debugger.addNode(DebugString.PERMISSIONPRIVATE);
 				break;
 			case PROTECTED:
 				Debugger.addNode(DebugString.PERMISSIONPROTECTED);
 			case PUBLIC:
 				Debugger.addNode(DebugString.PERMISSIONPUBLIC);
 			}
 		}
 		assert Debugger.addNode("Abstract: " + this.isAbstract);
 		assert Debugger.addNode("Static: " + this.isStatic);
 		assert Debugger.addNode("Return Value Reference: " + this.returnValue);
 		assert Debugger.closeNode();
 	}
 
 	@Override
 	public void setReturnValue(Referenced ref, ScriptValue value) throws Exception_Nodeable {
 		if (value == null && this.getReturnType().equals(ScriptKeywordType.VOID)) {
 			return;
 		}
 		assert Debugger.openNode("Setting Return-Value");
 		assert Debugger.addSnapNode("Function", this);
 		assert Debugger.addSnapNode("Value", value);
 		this.returnValue = value.castToType(ref, this.getReturnType());
 		assert Debugger.closeNode();
 	}
 }
