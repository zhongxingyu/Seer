 package org.rulemaker.engine.executors;
 
 import java.util.Map;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.rulemaker.engine.EngineContext;
 import org.rulemaker.engine.executors.exception.ExecutionException;
 
 /**
  * Base class for all action executors. It contains
  * base methods to perform custom actions.
  * 
  * @author &Aacute;ngel Garc&iacute;a Bastanchuri
  *
  */
 public abstract class BaseActionExecutor implements ActionExecutor {
 	
 	private Map<String, Object> sharpArgumentsMap;
 	private Map<String, Object> regularArgumentsMap;
 	private EngineContext engineContext;
 
 	/**
 	 * Gets the current map of arguments preceded with '#'.
 	 * 
 	 * @return The current map of sharp arguments.
 	 * 
 	 */
 	protected Map<String, Object> getSharpArgumentsMap() {
 		return sharpArgumentsMap;
 	}
 	
 	/**
 	 * Sets the current map of arguments preceded with '#'.
 	 * 
 	 * @param sharpArgumentsMap The map of sharp arguments to be assigned.
 	 * 
 	 */
 	public void setSharpArgumentsMap(Map<String, Object> sharpArgumentsMap) {
 		this.sharpArgumentsMap = sharpArgumentsMap;
 	}
 	
 	/**
 	 * Gets the current map of regular arguments.
 	 * 
 	 * @return The current map of regular arguments.
 	 * 
 	 */
 	protected Map<String, Object> getRegularArgumentsMap() {
 		return regularArgumentsMap;
 	}
 	
 	/**
 	 * Sets the current map of regular arguments.
 	 * 
 	 * @param regularArgumentsMap the current map of regular arguments.
 	 * 
 	 */
 	public void setRegularArgumentsMap(Map<String, Object> regularArgumentsMap) {
 		this.regularArgumentsMap = regularArgumentsMap;
 	}
 	
 	/**
 	 * Gets the engine context for this action executor.
 	 * 
 	 * @return Current engine context.
 	 * 
 	 */
 	protected EngineContext getEngineContext() {
 		return engineContext;
 	}
 
 	/**
 	 * Sets the current engine context for this executor.
 	 * 
 	 * @param engineContext the current engine context to be assigned.
 	 * 
 	 */
 	public void setEngineContext(EngineContext engineContext) {
 		this.engineContext = engineContext;
 	}
 	
 	/**
 	 * Convenience method to transfer argument values in a map into
 	 * a target object that needs to be updated conforming those values.
 	 * 
 	 * @param targetObject The object to be updated, for each key in map
 	 *                     there should be a setter method in the target
 	 *                     object conforming to the key name to update the value, 
 	 *                     otherwise the value should be ignored.
 	 *                     
 	 * @param argumentsMap The map containing the name of the fields to be updated into
 	 *                     target object and their new values.
 	 *                     
 	 * @throws ExecutionException If there is no target attribute in targetObject matching
 	 * 							  some of the keys given in arguments map or if there is
 	 * 							  a mismatch in argument type.
 	 * 
 	 * @throws NullPointerException If targetObject is null or argumentsMap is null.
 	 *                            
 	 */
 	protected void putArgumentsIntoTargetObject(Object targetObject, 
 			                                    Map<String, Object> argumentsMap) throws ExecutionException {
 		for (String aKey : argumentsMap.keySet()) {
 			if (PropertyUtils.isWriteable(targetObject, aKey)) {
 				try {
 					Class<?> targetArgumentType = PropertyUtils.getPropertyType(targetObject, aKey);
 					Object value = argumentsMap.get(aKey);
 					Class<?> valueType = (value != null) ? value.getClass() : Object.class;
					if (targetArgumentType.isAssignableFrom(valueType)) {
 						PropertyUtils.setProperty(targetObject, aKey, value);
 					} else {
 						throw new ExecutionException("Incompatible types in target object: "
 								+ "expected " + targetArgumentType.getName() + " but was " 
 								+ valueType.getName());
 					}
 				} catch (Exception e) {
 					// This exception might not be thrown.
 					throw new ExecutionException(e);
 				}
 			} else {
 				throw new ExecutionException("There is no property named " + aKey + 
 						" in object with type: " + targetObject.getClass().getName());
 			}
 		}
 	}
 }
