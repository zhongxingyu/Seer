 package com.js.interpreter.pascaltypes;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ncsa.tools.common.util.TypeUtils;
 import serp.bytecode.BCClass;
 import serp.bytecode.BCField;
 import serp.bytecode.BCMethod;
 import serp.bytecode.Code;
 import serp.bytecode.Instruction;
 import serp.bytecode.JumpInstruction;
 import serp.bytecode.Project;
 
 import com.js.interpreter.ast.ExpressionContext;
 import com.js.interpreter.ast.VariableDeclaration;
 import com.js.interpreter.ast.instructions.returnsvalue.ReturnsValue;
 import com.js.interpreter.exceptions.ParsingException;
 import com.js.interpreter.runtime.variables.ContainsVariables;
 
 public class CustomType extends DeclaredType {
 	/**
 	 * This class represents a declaration of a new type in pascal.
 	 */
 

 	/**
 	 * This is a list of the defined variables in the custom type.
 	 */
 	public List<VariableDeclaration> variable_types;
 
 	private Class cachedClass = null;
 
 	public CustomType() {
 		variable_types = new ArrayList<VariableDeclaration>();
 	}
 
 	/**
 	 * Adds another sub-variable to this user defined type.
 	 * 
 	 * @param v
 	 *            The name and type of the variable to add.
 	 */
 	public void add_variable_declaration(VariableDeclaration v) {
 		variable_types.add(v);
 	}
 
 	@Override
 	public Object initialize() {
 		try {
 			return toclass().newInstance();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public boolean isarray() {
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		return variable_types.hashCode();
 	}
 
 	@Override
 	public boolean equals(DeclaredType obj) {
 		if (!(obj instanceof CustomType)) {
 			return false;
 		}
 		CustomType other = (CustomType) obj;
 		return variable_types.equals(other.variable_types);
 	}
 
 	@Override
 	public Class toclass() {
 		if (cachedClass != null) {
 			return cachedClass;
 		}
 		Project p = new Project();
		BCClass c = p.loadClass("edu.js.interpreter.custom_types."
				+ Integer.toHexString(hashCode()));
 		c.setDeclaredInterfaces(new Class[] { ContainsVariables.class });
 		for (VariableDeclaration v : variable_types) {
 			c.declareField(v.name, TypeUtils.getTypeForClass(v.type.toclass()));
 		}
 		add_constructor(c);
 		add_get_var(c);
 		add_set_var(c);
 		add_clone(c);
 		cachedClass = bcl.loadThisClass(c.toByteArray());
 		return cachedClass;
 	}
 
 	static ByteClassLoader bcl = new ByteClassLoader();
 
 	public static class ByteClassLoader extends ClassLoader {
 
 		public Class<?> loadThisClass(byte[] bytes) {
 			Class<?> c = defineClass(null, bytes, 0, bytes.length);
 			resolveClass(c);
 			return c;
 		}
 	}
 
 	@Override
 	public ReturnsValue convert(ReturnsValue value, ExpressionContext f)
 			throws ParsingException {
 		RuntimeType other_type = value.get_type(f);
 		if (this.equals(other_type)) {
 			return value;
 		}
 		return null;
 	}
 
 	public DeclaredType getMemberType(String name) {
 		for (VariableDeclaration v : variable_types) {
 			if (v.name.equals(name)) {
 				return v.type;
 			}
 		}
 		System.err.println("Could not find member " + name);
 		return null;
 	}
 
 	@Override
 	public void pushDefaultValue(Code constructor_code) {
 		// TODO Auto-generated method stub
 	}
 
 	private void add_constructor(BCClass b) {
 		BCMethod constructor = b.addDefaultConstructor();
 		constructor.removeCode();
 		Code constructor_code = constructor.getCode(true);
 		constructor_code.aload().setThis();
 		try {
 			constructor_code.invokespecial().setMethod(
 					Object.class.getDeclaredConstructor());
 			for (VariableDeclaration v : variable_types) {
 				constructor_code.aload().setThis();
 				v.type.pushDefaultValue(constructor_code);
 				constructor_code
 						.putfield()
 						.setField(
 								b.getName(),
 								v.get_name(),
 								TypeUtils.isPrimitiveWrapper(v.type.toclass()) ? TypeUtils
 										.getTypeForClass(v.type.toclass())
 										.getCanonicalName() : v.type.toclass()
 										.getCanonicalName());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		constructor_code.vreturn();
 		constructor_code.calculateMaxLocals();
 		constructor_code.calculateMaxStack();
 	}
 
 	/**
 	 * Adds the get_var method to a specified class. This method will conform to
 	 * the ideas of the contains_variables interface, and will allow access to
 	 * all declared fields.
 	 * 
 	 * @param b
 	 *            The class to modify.
 	 */
 	private void add_get_var(BCClass b) {
 		BCMethod get_var = b.declareMethod("get_var", Object.class,
 				new Class[] { String.class });
 		get_var.makePublic();
 		Code get_var_code = get_var.getCode(true);
 		get_var_code.aload().setParam(0);
 		get_var_code.invokevirtual().setMethod(String.class, "intern",
 				String.class, new Class[] {});
 		get_var_code.astore().setParam(0);
 		JumpInstruction previous_if = null;
 		for (BCField f : b.getFields()) {
 			Instruction code_block = get_var_code.constant().setValue(
 					f.getName());
 			if (previous_if != null) {
 				previous_if.setTarget(code_block);
 			}
 			get_var_code.aload().setParam(0);
 			previous_if = get_var_code.ifacmpne();
 			get_var_code.aload().setThis();
 			get_var_code.getfield().setField(f);
 			Class return_type = f.getType();
 			if (return_type == int.class) {
 				get_var_code.invokestatic().setMethod(Integer.class, "valueOf",
 						Integer.class, new Class[] { int.class });
 			} else if (return_type == double.class) {
 				try {
 					get_var_code.invokestatic().setMethod(Double.class,
 							"valueOf", Double.class,
 							new Class[] { double.class });
 
 				} catch (Exception e) {
 				}
 			} else if (return_type == char.class) {
 				get_var_code.invokestatic().setMethod(Character.class,
 						"valueOf", Character.class, new Class[] { char.class });
 			} else if (return_type == boolean.class) {
 				get_var_code.invokestatic().setMethod(Boolean.class, "valueOf",
 						Boolean.class, new Class[] { boolean.class });
 			}
 			get_var_code.areturn();
 		}
 		Instruction i = get_var_code.constant().setNull();
 		if (previous_if != null) {
 			previous_if.setTarget(i);
 		}
 		get_var_code.areturn();
 		get_var_code.calculateMaxLocals();
 		get_var_code.calculateMaxStack();
 	}
 
 	/**
 	 * Adds the set_var method to a specified class. This method will conform to
 	 * the ideas of the contains_variables interface, and will allow access to
 	 * all declared fields.
 	 * 
 	 * @param b
 	 *            The class to modify.
 	 */
 	private void add_set_var(BCClass b) {
 		BCMethod set_var = b.declareMethod("set_var", void.class, new Class[] {
 				String.class, Object.class });
 		set_var.makePublic();
 		Code set_var_code = set_var.getCode(true);
 		set_var_code.aload().setParam(0);
 		set_var_code.invokevirtual().setMethod(String.class, "intern",
 				String.class, new Class[] {});
 		set_var_code.astore().setParam(0);
 		JumpInstruction previous_if = null;
 		for (BCField f : b.getFields()) {
 			Instruction jump_to = set_var_code.constant().setValue(f.getName());
 			if (previous_if != null) {
 				previous_if.setTarget(jump_to);
 			}
 			set_var_code.aload().setParam(0);
 			previous_if = set_var_code.ifacmpne();
 			set_var_code.aload().setThis();
 			set_var_code.aload().setParam(1);
 			Class field_class = f.getType();
 			if (field_class == int.class) {
 				set_var_code.checkcast().setType(Integer.class);
 				set_var_code.invokevirtual().setMethod(Integer.class,
 						"intValue", int.class, new Class[] {});
 			} else if (field_class == double.class) {
 				set_var_code.checkcast().setType(Double.class);
 				set_var_code.invokevirtual().setMethod(Double.class,
 						"doubleValue", double.class, new Class[] {});
 			} else if (field_class == boolean.class) {
 				set_var_code.checkcast().setType(Boolean.class);
 				set_var_code.invokevirtual().setMethod(Boolean.class,
 						"booleanValue", boolean.class, new Class[] {});
 			} else if (field_class == char.class) {
 				set_var_code.checkcast().setType(Character.class);
 				set_var_code.invokevirtual().setMethod(Character.class,
 						"charValue", char.class, new Class[] {});
 			}
 			set_var_code.putfield().setField(f);
 		}
 		Instruction jump_to = set_var_code.vreturn();
 		if (previous_if != null) {
 			previous_if.setTarget(jump_to);
 		}
 		set_var_code.calculateMaxLocals();
 		set_var_code.calculateMaxStack();
 	}
 
 	private void add_clone(BCClass b) {
 		BCMethod clone_method = b.declareMethod("clone",
 				ContainsVariables.class, new Class[0]);
 		clone_method.makePublic();
 		Code clone_code = clone_method.getCode(true);
 		try {
 			clone_code.anew().setType(b);
 			clone_code.astore().setLocal(1);
 			clone_code.aload().setLocal(1);
 			clone_code.invokespecial().setMethod(b.addDefaultConstructor());
 			for (BCField f : b.getFields()) {
 				clone_code.aload().setLocal(1);
 				clone_code.aload().setThis();
 				clone_code.getfield().setField(f);
 				if (!f.getType().isPrimitive() && f.getType() != String.class) {
 					clone_code.invokevirtual().setMethod(
 							f.getType().getMethod("clone", new Class[0]));
 				}
 				clone_code.putfield().setField(f);
 			}
 
 			clone_code.aload().setLocal(1);
 			clone_code.areturn();
 			clone_code.calculateMaxLocals();
 			clone_code.calculateMaxStack();
 		} catch (SecurityException e) {
 			e.printStackTrace();
 
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 
 		}
 	}
 }
