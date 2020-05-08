 package org.bh.data;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.IValue;
 import org.bh.platform.PlatformEvent;
 import org.bh.platform.Services;
 
 /**
  * General Data Transfer Object
  * 
  * @author Marcus Katzor
  * @author Robert Vollmer
  * @author Michael Löckelt
  * 
  * @param <ChildT>
  *            Type of the children.
  */
 @SuppressWarnings("unchecked")
 public abstract class DTO<ChildT extends IDTO> implements IDTO<ChildT> {
 	private static final long serialVersionUID = -9118358642929015184L;
 	private static final Logger log = Logger.getLogger(DTO.class);
 
 	@Retention(RetentionPolicy.RUNTIME)
 	@Target(ElementType.FIELD)
 	protected @interface Method {
 		String value() default "";
 	}
 
 	@Retention(RetentionPolicy.RUNTIME)
 	@Target(ElementType.FIELD)
 	public @interface Stochastic {
 	}
 
 	/**
 	 * Possible keys with which the user can access this DTO.
 	 */
 	protected final List<String> availableKeys;
 
 	private static Map<String, List<String>> KEYS_CACHE = new HashMap<String, List<String>>();
 
 	/**
 	 * Prefix of the methods. Method name: methodPrefix + key
 	 */
 	private static final String METHOD_PREFIX = "get";
 
 	/**
 	 * Possible methods with which the user can call simple methods for
 	 * calculation.
 	 */
 	protected Map<String, String> availableMethods;
 
 	private static Map<String, Map<String, String>> METHODS_CACHE = new HashMap<String, Map<String, String>>();
 
 	/**
 	 * All children assigned to this DTO.
 	 */
 	protected LinkedList<ChildT> children = new LinkedList<ChildT>();
 
 	/**
 	 * In the sandbox mode a valid copy of the DTO is made and can be used as
 	 * fallback data if a validation check returns false.
 	 */
 	protected boolean sandBoxMode = false;
 
 	/**
 	 * This flag shows if the dto was filled valid. It used for the tree and for
 	 * the exception list
 	 */
 	private boolean valid = true;
 
 	/**
 	 * The actual map which contains all values.
 	 */
 	protected Map<String, IValue> values = new HashMap<String, IValue>();
 
 	/**
 	 * Fallback data in order to provide a kind of undo functionality.
 	 */
 	protected Map<String, IValue> fallBackValues = new HashMap<String, IValue>();
 
 	public DTO(Enum[] enumeration) {
 		String className = getClass().getName();
 		if (KEYS_CACHE.containsKey(className)
 				&& METHODS_CACHE.containsKey(className)) {
 			availableKeys = KEYS_CACHE.get(className);
 			availableMethods = METHODS_CACHE.get(className);
 
 		} else {
 			availableKeys = new ArrayList<String>();
 			availableMethods = new HashMap<String, String>();
 
 			for (Enum element : enumeration) {
 				// Norman, 08.01.2010, 17:36 --> Keys in properties file for translation are not lowercase
 				//String key = element.toString().toLowerCase();
 				String key = element.toString();
 				availableKeys.add(key);
 				try {
 					Field field = element.getClass().getDeclaredField(
 							element.name());
 					Method method = field.getAnnotation(Method.class);
 					if (method != null) {
 						String methodName = (method.value().isEmpty()) ? (METHOD_PREFIX + element
 								.name())
 								: (method.value());
 						availableMethods.put(key, methodName);
 					}
 				} catch (Throwable e) {
 					continue;
 				}
 			}
 
 			KEYS_CACHE.put(className, availableKeys);
 			METHODS_CACHE.put(className, availableMethods);
 		}
 	}
 
 	@Override
 	public IValue get(Object key1) throws DTOAccessException {
 		// Norman, 08.01.2010, 17:36 --> Keys in properties file for translation are not lowercase
 		//String key = key1.toString().toLowerCase();
 		String key = key1.toString();
 		// Check whether the key is part of the DTO
 		if (!availableKeys.contains(key))
 			throw new DTOAccessException("This DTO does not have a key '" + key
 					+ "'");
 
 		// If the key is an actual key then return the corresponding value
 		IValue result = values.get(key);
 		if (result != null)
 			return result;
 		// If no value is assigned to this key, but a method exists
 		else if (availableMethods.containsKey(key))
 			try {
 				return invokeMethod(availableMethods.get(key));
 			} catch (NoSuchMethodError e) {
 				throw new DTOAccessException("Method does not exist", e);
 			}
 		else
 			throw new DTOAccessException(
 					"There is no value assigned to the key: '" + key + "'");
 	}
 
 	@Override
 	public Calculable getCalculable(Object key) throws DTOAccessException {
 		return (Calculable) get(key);
 	}
 
 	@Override
 	public void put(Object key1, IValue value) throws DTOAccessException {
 		// Norman, 08.01.2010, 17:36 --> Keys in properties file for translation are not lowercase
 		//String key = key1.toString().toLowerCase();
 		String key = key1.toString();
 		if (availableKeys.contains(key)) {
 			values.put(key, value);
 			Services.firePlatformEvent(new PlatformEvent(this,
 					PlatformEvent.Type.DATA_CHANGED));
 		}
 
 		else
 			throw new DTOAccessException("The key '" + key
 					+ "' is not part of this DTO!");
 	}
 
 	/**
 	 * Get a reference to a method with the specified name.
 	 * 
 	 * @param methodName
 	 * @return
 	 * @throws NoSuchMethodError
 	 */
 	private java.lang.reflect.Method getMethod(String methodName)
 			throws NoSuchMethodException {
 		// Get all methods
 		java.lang.reflect.Method[] methods = getClass().getDeclaredMethods();
 		// Go through each method and check if the wanted method
 		// is available
 		for (java.lang.reflect.Method method : methods) {
 			if (method.getName().equalsIgnoreCase(methodName)) {
 				return method;
 			}
 		}
 		throw new NoSuchMethodError(methodName);
 	}
 
 	/**
 	 * Invokes a method via reflection of this object.
 	 * 
 	 * @param method
 	 * @return
 	 * @throws DTOAccessException
 	 */
 	private IValue invokeMethod(String methodName) throws DTOAccessException {
 		IValue result = null;
 		try {
 			// Invoke method and store result
 			result = (IValue) getMethod(methodName).invoke(this);
 		} catch (NoSuchMethodException e) {
 			throw new DTOAccessException("The specified method '" + methodName
 					+ "' could not be found.");
 		} catch (InvocationTargetException e) {
 			throw new DTOAccessException(e.getTargetException());
 		} catch (Exception e) {
 			throw new DTOAccessException("The specified method '" + methodName
 					+ "' could not be invoked.");
 		}
 
 		if (result == null)
 			throw new DTOAccessException("The method '" + methodName
 					+ "' returned no proper result.");
 
 		// Return result
 		return result;
 	}
 
 	@Override
 	public String toString() {
 		return values.toString();
 	}
 
 	@Override
 	public ChildT addChild(ChildT child) throws DTOAccessException {
 		return addChild(child, true);
 	}
 
 	@Override
 	public ChildT addChild(ChildT child, boolean addLast)
 			throws DTOAccessException {
 		if (!children.contains(child)) {
 			if (addLast) {
 				children.addLast(child);
 			} else {
 				children.addFirst(child);
 			}
 			Services.firePlatformEvent(new PlatformEvent(this,
 					PlatformEvent.Type.DATA_CHANGED));
 			return child;
 		}
 		throw new DTOAccessException(
 				"The child is already assigned to this DTO!");
 	}
 
 	@Override
 	public ChildT getChild(int index) throws DTOAccessException {
 		ChildT result = null;
 		try {
 			result = children.get(index);
 		} catch (IndexOutOfBoundsException e) {
 			throw new DTOAccessException(
 					"There is no child at the given position: " + index);
 		}
 		return result;
 	}
 
 	@Override
 	public List<ChildT> getChildren() {
 		return Collections.unmodifiableList(children);
 	}
 
 	@Override
 	public ChildT getLastChild() {
 		return children.getLast();
 	}
 
 	@Override
 	public ChildT removeChild(int index) throws DTOAccessException {
 		try {
 			ChildT removedChild = children.remove(index);
 			Services.firePlatformEvent(new PlatformEvent(this,
 					PlatformEvent.Type.DATA_CHANGED));
 			return removedChild;
 		} catch (IndexOutOfBoundsException e) {
 			throw new DTOAccessException(
 					"There is no child at the given position: " + index);
 		}
 	}
 
 	@Override
 	public void removeChild(ChildT child) throws DTOAccessException {
 		try {
 			children.remove(child);
 			Services.firePlatformEvent(new PlatformEvent(this,
 					PlatformEvent.Type.DATA_CHANGED));
 		} catch (IndexOutOfBoundsException e) {
 			throw new DTOAccessException(
 					"This object is not a child in DTOs childlist!");
 		}
 	}
 
 	@Override
 	public void removeAllChildren() {
 		children.clear();
 		Services.firePlatformEvent(new PlatformEvent(this,
 				PlatformEvent.Type.DATA_CHANGED));
 	}
 
 	@Override
 	public int getChildrenSize() {
 		return children.size();
 	}
 	
 	@Override
 	public int getNoOfValues() {
 		return values.size();
 	}
 
 	@Override
 	public boolean getSandBoxMode() {
 		return sandBoxMode;
 	}
 
 	@Override
 	public void setSandBoxMode(boolean mode) {
 		if (mode) {
 			fallBackValues.clear();
 			values.putAll(((DTO<ChildT>) clone()).values);
 		}
 		sandBoxMode = mode;
 		log.debug("Sandboxmode changed to " + mode);
 	}
 
 	@Override
 	public DTO<ChildT> clone() throws DTOAccessException {
 		DTO<ChildT> result = null;
 		try {
 			// Try to instantiate a new instance of the class of this DTO
 			result = this.getClass().newInstance();
 			// Go through each value, copy it and put it into the new instance
 			for (Map.Entry<String, IValue> entry : values.entrySet()) {
 				result.put(entry.getKey(), entry.getValue().clone());
 				// Copy and add children to the new instance
 			}
 			if (children != null) {
 				for (ChildT child : children) {
 					// TODO check mit Robert
 					result.addChild((ChildT) child.clone());
 				}
 			}
 
 		} catch (Exception e) {
 			throw new DTOAccessException(
 					"An error occured during the cloning of a DTO. Class: "
 							+ getClass().getName(), e);
 		}
 
 		return result;
 	}
 
 	@Override
 	public List<String> getKeys() {
 		return availableKeys;
 	}
 
 	/**
 	 * return a iterable object containing children
 	 */
 	@Override
 	public Iterator<Map.Entry<String, IValue>> iterator() {
 		return values.entrySet().iterator();
 	}
 
 	@Override
 	public boolean isValid(boolean recursive) {
 		if (recursive)
 			return this.valid && getChildValidity();
 		return this.valid;
 	}
 
 	/**
 	 * get the validity of all children
 	 * 
 	 * @return
 	 */
 	private boolean getChildValidity() {
 		Iterator<ChildT> childIterator = getChildren().iterator();
 
 		while (childIterator.hasNext()) {
 			DTO<ChildT> childT = (DTO<ChildT>) childIterator.next();
 			if (!childT.isValid(true)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void setValid(boolean valid) {
		if (this.valid == valid)
			return;
 		this.valid = valid;
 	}
 }
