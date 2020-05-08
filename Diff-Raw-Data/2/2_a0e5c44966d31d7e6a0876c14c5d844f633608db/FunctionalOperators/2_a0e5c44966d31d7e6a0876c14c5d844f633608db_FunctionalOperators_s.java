 //Copyright 2012-2013 Joshua Scoggins. All rights reserved.
 //
 //Redistribution and use in source and binary forms, with or without modification, are
 //permitted provided that the following conditions are met:
 //
 //   1. Redistributions of source code must retain the above copyright notice, this list of
 //      conditions and the following disclaimer.
 //
 //   2. Redistributions in binary form must reproduce the above copyright notice, this list
 //      of conditions and the following disclaimer in the documentation and/or other materials
 //      provided with the distribution.
 //
 //THIS SOFTWARE IS PROVIDED BY Joshua Scoggins ``AS IS'' AND ANY EXPRESS OR IMPLIED
 //WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 //FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Joshua Scoggins OR
 //CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 //CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 //SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 //ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 //NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 //ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 //
 //The views and conclusions contained in the software and documentation are those of the
 //authors and should not be interpreted as representing official policies, either expressed
 //or implied, of Joshua Scoggins. 
 package functions;
 import java.util.*;
 import java.lang.*;
 public class FunctionalOperations {
 	private FunctionalOperations() { }
 
 	public static <T> T as(Object value) {
 		return (T) value; //REALLY UNSAFE
 	}
 
 	public static <T> List<T> cdr(List<T> input) {
 		return input.size() <= 1 ? new Vector<T>(0) : input.subList(1, input.size());
 	}
 
 	public static <T> T car(List<T> input) {
 		return input.isEmpty() ? null : input.get(0);
 	}
 
 	public static void let(DynamicDelegate function, Object[]... dynamicVariables) {
 		//final NonLocalClosedVariable<T> cV0 = new NonLocalClosedVariable<T>(v0);
 		for (int i = 0; i < dynamicVariables.length; i++) {
 			function.registerDynamicVariable(getDynamicVariableName(dynamicVariables[i]),
 					getDynamicVariableValue(dynamicVariables[i]),
 					getDynamicVariableReadonlyStatus(dynamicVariables[i]));
 		}
 	}
 
 	public static <T> T ldarg(DynamicDelegate vars, String name) {
 		return ((NonLocalClosedVariable<T>) vars.getDynamicVariable(name)).getActualValue();
 	}
 
 	public static int ldarg_i4(DynamicDelegate var, String name) {
 		Integer val = ldarg(var, name);
 		return val.intValue();
 	}
 
 	public static double ldarg_f8(DynamicDelegate var, String name) {
 		Double val = ldarg(var, name);
 		return val.doubleValue();
 	}
 
 	public static long ldarg_i8(DynamicDelegate var, String name) {
 		Long val = ldarg(var, name);
 		return val.longValue();
 	}
 
 	public static float ldarg_f4(DynamicDelegate var, String name) {
 		Float val = ldarg(var, name);
 		return val.floatValue();
 	}
 
 	public static short ldarg_i2(DynamicDelegate var, String name) {
 		Short val = ldarg(var, name);
 		return val.shortValue();
 	}
 
 	public static byte ldarg_i1(DynamicDelegate var, String name) {
 		Byte val = ldarg(var, name);
 		return val.byteValue();
 	}
 
 	public static String ldarg_str(DynamicDelegate var, String name) {
 		return ldarg(var, name);
 	}
 
 	public static boolean ldarg_bool(DynamicDelegate var, String name) {
 		Boolean val = ldarg(var, name);
 		return val.booleanValue();
 	}
 
 	public static char ldarg_char(DynamicDelegate var, String name) {
 		Character val = ldarg(var, name);
 		return val.charValue();
 	}
 
 	public static Object empty_args() {
 		return new Object[0];
 	}
 
 	public static Object[][] variables(String ... elements) {
 		Object[][] output = new Object[elements.length][];
 		for (int i = 0; i < elements.length; i++) {
 			output[i] = dynamicVariable(elements[i]);
 		}
 		return output;
 	}
 
 	public static Delegate defun(Object[][] args, DelegateBody body) {
 		final NonLocalClosedVariable<Object[][]> arguments = closeOverNonLocal(args);
 		final NonLocalClosedVariable<DelegateBody> fn = closeOverNonLocal(body);
 		return new DynamicDelegateBase() {
 
 			Hashtable<Integer, String> reference;
 
 			@Override
 			protected void initVariables() {
 				reference = new Hashtable<Integer, String>();
 				Object[][] a = arguments.getActualValue();
 				for (int i = 0; i < a.length; i++) {
 					reference.put(i, getDynamicVariableName(a[i]));
 				}
 				let(this, arguments.getActualValue());
 		   }
 			@Override
 			public DynamicDelegate copy() {
 			   return (DynamicDelegate)clone();
 			}
 			@Override
 				public Object clone() {
 					DynamicDelegateBase bn = (DynamicDelegateBase) this;
 					final ClosedVariable<DynamicDelegateBase> _fn = closeOver(bn);
 					return new DynamicDelegateBase(this) {
 
 						@Override
 							protected void initVariables() {
 								//nothing special to init as that is handled by the copy method
 							}
 
 						@Override
 							public DynamicDelegate copy() {
 								return _fn.getValue().copy(); //elegant hack
 								//we have the outer scope copy instance called from the inner
 								//scope to prevent infinite loops. This may seem like an infinite
 								//loop would occur but the reality is that copy will only be called once
 								//but the same function will be provided, this has the side effect of preventing
 								//recursive class calls as well.
 							}
 						@Override
 						public Object clone() {
 							return _fn.getValue().clone();
 						}
 
 						public Object invoke(DynamicDelegate localVariables, Object[] input) {
 							return _fn.getValue().invoke(localVariables, input);
 						}
 
 						public Object invoke(DynamicDelegate localVariables) {
 							return _fn.getValue().invoke(localVariables);
 						}
 
 						public Object invoke() {
 							return invoke(this);
 						}
 
 						public Object invoke(Object[] input) {
 							return invoke(this, input);
 						}
 
 						public void run() {
 							_fn.getValue().run();
 						}
 
 						public void run(DynamicDelegate localVariables) {
							_fn.getValue(this);
 						}
 					};
 				}
 
 			public Object invoke(DynamicDelegate localVariables, Object[] input)
 			{
 				try {
 					DynamicDelegate body = copy();
 					for (int i = 0; i < input.length; i++) {
 						body.setDynamicVariable(reference.get(i), input[i]);
 					}
 					return body.invoke(body);
 				} catch (DynamicVariableReadonlyException d) {
 					return null;
 				}
 			}
 
 			public Object invoke(DynamicDelegate localVariables) {
 				return fn.getActualValue().invoke(localVariables);
 			}
 
 			public Object invoke() {
 				return invoke(this);
 			}
 
 			public Object invoke(Object[] input) {
 				return invoke(this, input);
 			}
 
 			public void run() {
 				run(this);
 			}
 
 			public void run(DynamicDelegate localVariables) {
 				fn.getActualValue().run(localVariables);
 			}
 		};
 	}
 
 	public static String getDynamicVariableName(Object[] var) {
 		return (String) var[0];
 	}
 
 	public static Object getDynamicVariableValue(Object[] var) {
 		return var[1];
 	}
 
 	public static boolean getDynamicVariableReadonlyStatus(Object[] var) {
 		return ((Boolean) var[2]).booleanValue();
 	}
 
 	public static Object[] dynamicVariable(String name, Object value, boolean readonly) {
 		return new Object[] { name, value, readonly };
 	}
 
 	public static Object[] dynamicVariable(String name, Object value) {
 		return dynamicVariable(name, value, false);
 	}
 
 	public static Object[] dynamicVariable(String name) {
 		return dynamicVariable(name, null);
 	}
 
 	public static <T> ClosedVariable<T> closeOver(T value) {
 		return new ClosedVariable<T>(value);
 	}
 
 	public static <T> NonLocalClosedVariable<T> closeOverNonLocal(ClosedVariable<T> value) {
 		return new NonLocalClosedVariable<T>(value);
 	}
 
 	public static <T> NonLocalClosedVariable<T> closeOverNonLocal(T value) {
 		return new NonLocalClosedVariable<T>(value);
 	}
 
 	public static <T> void setClosedVariable(ClosedVariable<T> var, T newValue) {
 		var.setValue(newValue);
 	}
 
 	public static <T> void setNonLocalVariable(ClosedVariable<ClosedVariable<T>> target, T value) {
 		ClosedVariable<T> inner = target.getValue();
 		setClosedVariable(inner, value);
 	}
 }
