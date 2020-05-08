 package cz.cvut.fit.mirun.lemavm.structures.classes;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import cz.cvut.fit.mirun.lemavm.exceptions.VMAmbiguousMethodDeclaration;
 import cz.cvut.fit.mirun.lemavm.exceptions.VMParsingException;
 import cz.cvut.fit.mirun.lemavm.structures.VMObject;
 import cz.cvut.fit.mirun.lemavm.structures.builtin.VMNull;
 import cz.cvut.fit.mirun.lemavm.utils.VMUtils;
 
 /**
  * Meta class representing classes defined in the VM.
  * 
  * @author kidney
  * 
  */
 public final class VMClass {
 
 	private static final Map<String, VMClass> classes = new HashMap<>();
 
 	private final String name;
 	private final VMClass superClass;
 	private final Map<String, VMField> fields;
 	private final List<VMMethod> constructors;
 	// Contains all defined methods (static and instance)
 	private final Map<String, List<VMMethod>> methods;
 	// Contains only static methods
 	private final Map<String, List<VMMethod>> staticMethods;
 
 	// Contains static field values
 	private final VMEnvironment classEnv;
 
 	private VMClass(String name, VMClass superClass) {
 		this.name = name;
 		this.superClass = superClass;
 		this.fields = new HashMap<>();
 		this.constructors = new ArrayList<>();
 		this.methods = new HashMap<>();
 		this.staticMethods = new HashMap<>();
 		if (superClass != null) {
 			this.classEnv = new VMEnvironment(superClass.classEnv);
 		} else {
 			this.classEnv = new VMEnvironment();
 		}
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public VMClass getSuperClass() {
 		return superClass;
 	}
 
 	public Map<String, VMField> getFields() {
 		return fields;
 	}
 
 	/**
 	 * Add new field to this class.
 	 * 
 	 * @param newField
 	 *            The field to add
 	 * @throws VMParsingException
 	 *             If this class already contains a field of that name
 	 */
 	public void addField(VMField field) {
 		if (field == null) {
 			throw new NullPointerException();
 		}
 		if (fields.containsKey(field.getName())) {
 			throw new VMParsingException("Field with name " + field.getName()
 					+ " already exists in class " + name);
 		}
 		// fields.put(field.getName(), field);
 		if (field.isStatic()) {
 			addStaticFieldValue(field);
 		} else {
 			fields.put(field.getName(), field);
 		}
 	}
 
 	/**
 	 * Add a value binding for the specified static field.
 	 * 
 	 * @param field
 	 */
 	private void addStaticFieldValue(VMField field) {
 		final String name = field.getName();
 		final Object val = field.getVal();
 		final String type = field.getType();
 		if (val == null) {
 			if (VMUtils.isTypePrimitive(type)) {
 				classEnv.addPrimitiveBinding(name,
 						VMUtils.getTypeDefaultValue(type), type);
 			} else {
 				classEnv.addBinding(name, VMNull.getInstance(), type);
 			}
 		} else {
 			if (VMUtils.isTypePrimitive(type)) {
 				classEnv.addPrimitiveBinding(name, val, type);
 			} else {
 				classEnv.addBinding(name, (VMObject) val, type);
 			}
 		}
 	}
 
 	public Map<String, List<VMMethod>> getMethods() {
 		return methods;
 	}
 
 	/**
 	 * Get a list of methods with the specified name. </p>
 	 * 
 	 * If there are no method with this name, an empty list is returned.
 	 * 
 	 * @param methodName
 	 *            Name of the method
 	 * @return List of methods or an empty list
 	 */
 	public List<VMMethod> getMethodsForName(String methodName) {
 		if (methodName == null) {
 			throw new NullPointerException();
 		}
 		List<VMMethod> res = methods.get(methodName);
 		if (res == null) {
 			return Collections.emptyList();
 		}
 		return res;
 	}
 
 	/**
 	 * Get a list of static methods with the specified name. </p>
 	 * 
 	 * If there are none such, an empty list is returned.
 	 * 
 	 * @param methodName
 	 *            Name of the method
 	 * @return List of methods or an empty list
 	 */
 	public List<VMMethod> getStaticMethodsForName(String methodName) {
 		if (methodName == null) {
 			throw new NullPointerException();
 		}
 		List<VMMethod> res = staticMethods.get(methodName);
 		if (res == null) {
 			res = Collections.emptyList();
 		}
 		return res;
 	}
 
 	public List<VMMethod> getDeclaredConstructors() {
 		return constructors;
 	}
 
 	public VMEnvironment getClassEnvironment() {
 		return classEnv;
 	}
 
 	/**
 	 * Add a new method to this class. </p>
 	 * 
 	 * Method ambiguity check is performed.
 	 * 
 	 * @param newMethod
 	 *            The method to add
 	 * @throws VMAmbiguousMethodDeclaration
 	 */
 	public void addMethod(VMMethod newMethod) {
 		if (newMethod == null) {
 			throw new NullPointerException();
 		}
 		List<VMMethod> ms = methods.get(newMethod.getName());
 		if (ms != null) {
 			VMMethod.checkForMethodAmbiguity(methods.get(newMethod.getName()),
 					newMethod);
 			ms.add(newMethod);
 		} else {
 			ms = new ArrayList<>();
 			ms.add(newMethod);
 			methods.put(newMethod.getName(), ms);
 		}
 		if (newMethod.isMethodStatic()) {
 			List<VMMethod> statics = staticMethods.get(newMethod.getName());
 			if (statics == null) {
 				statics = new ArrayList<>();
 				staticMethods.put(newMethod.getName(), statics);
 			}
 			statics.add(newMethod);
 		}
 		newMethod.setOwner(this);
 	}
 
 	/**
 	 * Add a new constructor to this class. </p>
 	 * 
 	 * Method ambiguity check is performed.
 	 * 
 	 * @param newConstructor
 	 *            The constructor to add
 	 * @throws VMAmbiguousMethodDeclaration
 	 */
 	public void addConstructor(VMMethod newConstructor) {
 		if (newConstructor == null) {
 			throw new NullPointerException();
 		}
 		VMMethod.checkForMethodAmbiguity(getDeclaredConstructors(),
 				newConstructor);
 		constructors.add(newConstructor);
 		newConstructor.setOwner(this);
 	}
 
 	/**
 	 * Create new instance of this class. </p>
 	 * 
 	 * @return New instance of this class
 	 */
 	public VMClassInstance createInstance() {
 		if (superClass != null) {
 			return new VMClassInstance(this, superClass.createInstance());
 		} else {
 			return new VMClassInstance(this, null);
 		}
 	}
 
 	/**
 	 * Check if this class instance is the same or is a superclass of the
 	 * specified other class.
 	 * 
 	 * @param other
 	 *            The class to check
 	 * @return True if this class is assignable from the other
 	 */
 	public boolean isAssignableFrom(VMClass other) {
 		if (other.getName().equals(name)) {
 			return true;
 		}
 		VMClass sup = other;
 		while (sup != null) {
 			if (sup.getName().equals(name)) {
 				return true;
 			}
 			sup = sup.getSuperClass();
 		}
 		return false;
 	}
 
 	@Override
 	public boolean equals(Object other) {
 		if (other instanceof VMClass) {
 			final VMClass c = (VMClass) other;
 			return name.equals(c.getName());
 		}
 		return false;
 	}
 
 	@Override
 	public int hashCode() {
 		int hash = 31 * name.hashCode();
 		return hash;
 	}
 
 	@Override
 	public String toString() {
 		return name;
 	}
 
 	/**
 	 * Create class with the specified parameters. </p>
 	 * 
 	 * This static method is the only way to create a {@code VMClass} so that
 	 * there is always only one object of the specified class name.
 	 * 
 	 * @param name
 	 *            Name of the new class
 	 * @param superClass
 	 *            Super class of the new class. Can be null
 	 * @return The new meta class
 	 */
 	public static VMClass createClass(String name, String superClass) {
 		if (name == null || name.isEmpty()) {
 			throw new VMParsingException(
 					"Invalid VMClass constructor parameters: " + name);
 		}
 		if (classes.containsKey(name)) {
 			// return classes.get(name);
 			throw new VMParsingException("Definition of class with name "
 					+ name + " already exists.");
 		}
		final VMClass parent = classes.get(superClass);
		if (parent == null) {
			throw new VMParsingException("Superclass " + superClass
					+ " not found.");
 		}
 		final VMClass newClass = new VMClass(name, parent);
 		classes.put(name, newClass);
 		VMEnvironment.addType(name);
 		VMEnvironment.addType(name + "[]");
 		return newClass;
 	}
 
 	/**
 	 * Get a map of all existing classes.
 	 * 
 	 * @return Existing classes
 	 */
 	public static Map<String, VMClass> getClasses() {
 		return classes;
 	}
 
 	/**
 	 * Reset VMClass when reseting Virtual machine
 	 */
 	public static void resetPartVM() {
 		classes.clear();
 	}
 }
