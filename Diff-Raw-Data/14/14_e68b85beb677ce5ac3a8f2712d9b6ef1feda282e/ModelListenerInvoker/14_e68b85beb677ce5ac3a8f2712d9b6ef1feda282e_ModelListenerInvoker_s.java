 /*******************************************************************************
  * Copyright © 2011 Alexey Kaloshin. All Rights Reserved. 
  * 
  *  Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  * 
  *  1. Redistributions of source code must retain the above copyright 
  * notice, this list of conditions and the following disclaimer.
  * 
  *  2. Redistributions in binary form must reproduce the above copyright 
  * notice, this list of conditions and the following disclaimer in the 
  * documentation and/or other materials provided with the distribution.
  * 
  *  3. The name of the author may not be used to endorse or promote 
  * products derived from this software without specific prior written permission. 
  * 
  *  THIS SOFTWARE IS PROVIDED BY Alexey Kaloshin "AS IS" AND ANY EXPRESS 
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
  * AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  ******************************************************************************/
 package cz.nocach.swingmvp.model;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Map;
 
 import com.google.common.base.Function;
 
 import cz.nocach.swingmvp.MvpException;
 import cz.nocach.swingmvp.model.listener.Add;
 import cz.nocach.swingmvp.model.listener.PropertyListenerInvokeInterface;
 import cz.nocach.swingmvp.model.listener.Remove;
 import cz.nocach.swingmvp.util.Annotations;
 import cz.nocach.swingmvp.util.Fields;
 import cz.nocach.swingmvp.util.ObjectsMethodWithAnnotation;
 
 /**
  * INTERNAL
  * 
  * class for invoking events of model listeners. This includes property change events and
  * collection-related change events (add and remove)
  * @author __nocach
  *
  */
 public class ModelListenerInvoker {
 	private final Model model;
 	/**
 	 * 
 	 * @param model not null model
 	 * @throws NullPointerException {@code model} is null
 	 */
 	public ModelListenerInvoker(Model model) {
 		checkNotNull(model);
 		this.model = model;
 	}
 	private Function<Add, Boolean> getFilterAddPropertyEquals(final String propertyName){
 		return new Function<Add, Boolean>() {
 			@Override public Boolean apply(Add input) {
 				return input.propertyName().equals(propertyName);
 			}
 		};
 	}
 	private Function<Remove, Boolean> getFilterRemovePropertyEquals(final String propertyName){
 		return new Function<Remove, Boolean>() {
 			@Override public Boolean apply(Remove input) {
 				return input.propertyName().equals(propertyName);
 			}
 		};
 	}
 	/**
 	 * 
 	 * @param property
 	 * @param <T> type of the annotation that will be passed to this function
 	 * @return
 	 */
 	private <T>Function<ObjectsMethodWithAnnotation<T>, Void> invokeWithPropertyValue(final Object property){
 		return new Function<ObjectsMethodWithAnnotation<T>, Void>() {
 			@Override public Void apply(ObjectsMethodWithAnnotation input) {
 				try {
 					input.method.invoke(input.object, property);
 				} catch (IllegalArgumentException e) {
 					// TODO: handle
 					throw new RuntimeException(e);
 				} catch (IllegalAccessException e) {
 					// TODO: handle
 					throw new RuntimeException(e);
 				} catch (InvocationTargetException e) {
 					// TODO: handle
 					throw new RuntimeException(e);
 				}
 				return null;
 			}
 		};
 	}
 	private void ensureModelHasSetOfCustomListenerInstances() {
 		model.initCustomListeners();
 	}
 	public void firePropertyAdd(final String propertyName, final Object property) {
 		ensureModelHasSetOfCustomListenerInstances();
 		Function<Add, Boolean> applyOnlyForEqualPropertyName = getFilterAddPropertyEquals(propertyName);
 		Function<ObjectsMethodWithAnnotation<Add>, Void> firePropertyAddOnMatch = invokeWithPropertyValue(property);
 		Annotations.applyForObjectsMethodWithAnnotation(model.customListeners, Add.class
 				, applyOnlyForEqualPropertyName, firePropertyAddOnMatch);
 	}
 	public void firePropertyRemoved(final String propertyName, final Object property) {
 		ensureModelHasSetOfCustomListenerInstances();
 		Function<Remove, Boolean> applyOnlyForEqualPropertyName = getFilterRemovePropertyEquals(propertyName);
 		Function<ObjectsMethodWithAnnotation<Remove>, Void> firePropertyAddOnMatch = invokeWithPropertyValue(property);
 		Annotations.applyForObjectsMethodWithAnnotation(model.customListeners, Remove.class
 				, applyOnlyForEqualPropertyName, firePropertyAddOnMatch);
 	}
 	/**
 	 * 
 	 * @param modelListenerImplementer not null object that optionally implements any custom listener
 	 * 		that resides on {@code model}
 	 * @throws NullPointerException if {@code modelListenerImplementer} is null 
 	 */
 	public void invokeCustomListenerEventForEachModelPropertyValue(final Object modelListenerImplementer){
 		checkNotNull(modelListenerImplementer);
 		model.initListenerAndTheirInvokeInterfaces();
 		ensureModelHasSetOfCustomListenerInstances();
 		for (Field eachField : Fields.getDeclaredFieldsFromHierarchy(model.getClass(), Model.class)){
 			enforceFieldPropertyChange(eachField, modelListenerImplementer);
 		}
 		
 		Function<ObjectsMethodWithAnnotation<Add>, Void> callAddElementsListener = 
 				new Function<ObjectsMethodWithAnnotation<Add>, Void>() {
 			@Override public Void apply(ObjectsMethodWithAnnotation<Add> input) {
 				try {
 					if (input.annotation.collection().isEmpty()) return null;
 					if (input.object != modelListenerImplementer) return null;
 					Field collectionField = Fields.getDeclaredFieldFromHierarchy(
 							model.getClass(), input.annotation.collection());
 					collectionField.setAccessible(true);
 					Object collectionFieldValue = collectionField.get(model);
 					if (! (collectionFieldValue instanceof Iterable)){
 						//TODO: handle
 						throw new MvpException(input.annotation.collection() + " must implement Iterable ");
 					}
 					for(Object each : (Iterable)collectionFieldValue){
 						input.method.setAccessible(true);
 						input.method.invoke(input.object, each);
 					}
 				} catch (IllegalArgumentException e) {
 					// TODO: handle
 					throw new MvpException(e);
 				} catch (IllegalAccessException e) {
 					// TODO: handle
 					throw new MvpException(e);
 				} catch (InvocationTargetException e) {
 					// TODO: handle
 					throw new MvpException(e);
 				} catch (SecurityException e) {
 					// TODO: handle
 					throw new MvpException(e);
 				} catch (NoSuchFieldException e) {
 					// TODO: handle
 					throw new MvpException(e);
 				}
 				return null;
 			}
 		};
 		Annotations.applyForObjectsMethodWithAnnotation(model.customListeners,
 				Add.class, null, callAddElementsListener);
 	}
 	@SuppressWarnings("rawtypes")
 	private void enforceFieldPropertyChange(Field fieldName, final Object usingListener) {
 		for(Map.Entry<Object, PropertyListenerInvokeInterface> eachListenerWithInvoker
 				: model.listenersWithInvokeInterfaces.entrySet()){
 			Object listenerInstance = eachListenerWithInvoker.getKey();
 			if (listenerInstance != usingListener) continue;
 			PropertyListenerInvokeInterface listenerInvokeInterface = eachListenerWithInvoker.getValue();
 			if (listenerInvokeInterface.supportsThisProperty(fieldName.getName())){
 				invokeListenerUsingField(fieldName, listenerInstance, listenerInvokeInterface);
 			}
 		}
 	}
 	private void invokeListenerUsingField(Field fieldName,
 			Object listenerInstance,
 			PropertyListenerInvokeInterface listenerInvokeInterface) {
 		try {
 			fieldName.setAccessible(true);
 			Object newValue = fieldName.get(model);
 			Object oldValue = null;
 			listenerInvokeInterface.fire(listenerInstance, model, fieldName.getName(), oldValue, newValue);
 		} catch (IllegalArgumentException e) {
 			// TODO Handle
 			throw new MvpException(e);
 		} catch (IllegalAccessException e) {
 			// TODO Handle
 			throw new MvpException(e);
 		}
 	}
 	
 	/**
 	 * @param beforeSetPropertyValue
 	 * @param afterSetPropertyValue
 	 * @return true if change event must be done, false otherwise
 	 */
 	private boolean doesChangeEventMustBeFired(Object beforeSetPropertyValue, Object afterSetPropertyValue){
 		return beforeSetPropertyValue == null && afterSetPropertyValue != null
 				|| afterSetPropertyValue == null && beforeSetPropertyValue != null
 				|| afterSetPropertyValue != null && beforeSetPropertyValue != null
 												 && !beforeSetPropertyValue.equals(afterSetPropertyValue);
 	}
 	/**
 	 * if {@code beforeSetPropertyValue} does not equal to {@code afterSetPropertyValue} then method will
 	 * call appropriate listeners on the model. It will invoke all listener instances that model
 	 * will have on the moment of the call to this method
 	 * @param propertyName not null propertyName
 	 * @param beforeSetPropertyValue value that was before new property value was set
 	 * @param afterSetPropertyValue value that model has on the moment of call to this method
 	 * @throws NullPointerException {@code propertyName} is null
 	 */
 	public void processPropertyChange(String propertyName,
 			Object beforeSetPropertyValue, Object afterSetPropertyValue) {
 		checkNotNull(propertyName);
 		if (doesChangeEventMustBeFired(beforeSetPropertyValue, afterSetPropertyValue)){
 			model.initListenerAndTheirInvokeInterfaces();
 			for (Map.Entry<Object, PropertyListenerInvokeInterface> entry
 					: model.listenersWithInvokeInterfaces.entrySet()){
				entry.getValue().fire(entry.getKey(), model, propertyName, beforeSetPropertyValue, afterSetPropertyValue);
 			}
 		}
 	}
 }
