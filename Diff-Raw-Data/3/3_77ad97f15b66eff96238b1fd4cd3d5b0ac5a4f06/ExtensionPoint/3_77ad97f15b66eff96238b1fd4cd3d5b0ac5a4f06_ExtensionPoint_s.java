 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.common.extensionpoint;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.emfstore.common.Activator;
 
 /**
  * This class is a convenience wrapper for eclipse extension points. It can be configured to return null if a value
  * can't be found, but also to throw Exceptions. The latter normally requires a catch block but you don't have to null
  * check.
  * 
  * @author wesendon
  */
 
 public class ExtensionPoint {
 	private List<ExtensionElement> elements;
 	private final String id;
 	private boolean exceptionInsteadOfNull;
 	private Comparator<ExtensionElement> comparator;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param id extension point id
 	 */
 	public ExtensionPoint(String id) {
 		this(id, false);
 	}
 
 	/**
 	 * Constructor with option of set the throw exception option.
 	 * 
 	 * @param id extension point id
 	 * @param throwException if true, an {@link ExtensionPointException} is thrown instead of returning null
 	 */
 	public ExtensionPoint(String id, boolean throwException) {
 		this.id = id;
 		exceptionInsteadOfNull = throwException;
 		this.comparator = getDefaultComparator();
 		reload();
 	}
 
 	/**
 	 * Reloads extensions from the registry.
 	 */
 	public void reload() {
 		this.elements = new ArrayList<ExtensionElement>();
 		for (IConfigurationElement element : Platform.getExtensionRegistry().getConfigurationElementsFor(this.id)) {
 			elements.add(new ExtensionElement(element, exceptionInsteadOfNull));
 		}
 		Collections.sort(this.elements, this.comparator);
 	}
 
 	/**
 	 * Returns the default comparator, it doesn't sort but uses the natural order. This method is intended for
 	 * overriding if other default is preferred.
 	 * 
 	 * @return comparator
 	 */
 	protected Comparator<ExtensionElement> getDefaultComparator() {
 		return new Comparator<ExtensionElement>() {
 			public int compare(ExtensionElement o1, ExtensionElement o2) {
 				return 0;
 			}
 		};
 	}
 
 	/**
 	 * Gets a class from the element with highest priority ({@link #getElementWithHighestPriority()}, default
 	 * {@link #getFirst()}). Or rather the registered instance of that class.
 	 * 
 	 * @param classAttributeName class attribute name
 	 * @param returnType Class of expected return value
 	 * @param <T> the type of the class
 	 * @return the result or either null, or an runtime exception is thrown in the case of
 	 *         {@link #setThrowException(boolean)} is true.
 	 */
 	@SuppressWarnings("unchecked")
 	public <T> T getClass(String classAttributeName, Class<T> returnType) {
 		ExtensionElement first = getElementWithHighestPriority();
 		if (first != null) {
 			return first.getClass(classAttributeName, returnType);
 		}
 		return (T) handleErrorOrNull(exceptionInsteadOfNull, null);
 	}
 
 	/**
 	 * Returns the value of the boolean attribute, if existing, or given false otherwise, from the element with
 	 * highest priority ({@link #getElementWithHighestPriority()}, default {@link #getFirst()}).
 	 * 
 	 * @param name attribute id
 	 * @return the result or either null, or an runtime exception is thrown in the case of
 	 *         {@link #setThrowException(boolean)} is true.
 	 */
 	public Boolean getBoolean(String name) {
 		return getBoolean(name, false);
 	}
 
 	/**
 	 * Returns the value of the boolean attribute, if existing, or given defaultValue otherwise, from the element with
 	 * highest priority ({@link #getElementWithHighestPriority()}, default {@link #getFirst()}).
 	 * 
 	 * @param name attribute id
 	 * @param defaultValue the default value if attribute does not exist
 	 * @return the result or either null, or an runtime exception is thrown in the case of
 	 *         {@link #setThrowException(boolean)} is true.
 	 */
 	public Boolean getBoolean(String name, boolean defaultValue) {
 		ExtensionElement element = getElementWithHighestPriority();
 		if (element != null) {
 			return element.getBoolean(name, defaultValue);
 		}
		return (Boolean) handleErrorOrNull(exceptionInsteadOfNull, null);
 	}
 
 	/**
 	 * Gets a Integer from the element with highest priority ({@link #getElementWithHighestPriority()}, default
 	 * {@link #getFirst()}).
 	 * 
 	 * @param name attribute id
 	 * @return the result or either null, or an runtime exception is thrown in the case of
 	 *         {@link #setThrowException(boolean)} is true.
 	 */
 	public Integer getInteger(String name) {
 		ExtensionElement element = getElementWithHighestPriority();
 		if (element != null) {
 			return element.getInteger(name);
 		}
 		return (Integer) handleErrorOrNull(exceptionInsteadOfNull, null);
 	}
 
 	/**
 	 * Gets an attribute in form of a string from the element with highest priority (
 	 * {@link #getElementWithHighestPriority()}, default {@link #getFirst()}).
 	 * 
 	 * @param name attribute id
 	 * @return the result or either null, or an runtime exception is thrown in the case of
 	 *         {@link #setThrowException(boolean)} is true.
 	 */
 	public String getAttribute(String name) {
 		ExtensionElement element = getElementWithHighestPriority();
 		if (element != null) {
 			return element.getAttribute(name);
 		}
 		return (String) handleErrorOrNull(exceptionInsteadOfNull, null);
 	}
 
 	/**
 	 * Returns the element with highest priority, by default {@link #getFirst()} is used. This method is intended to be
 	 * overriden in order to modify default behavior.
 	 * 
 	 * @return {@link ExtensionElement}
 	 */
 	public ExtensionElement getElementWithHighestPriority() {
 		return getFirst();
 	}
 
 	/**
 	 * Set a custom comparator which defines the order of the {@link ExtensionElement}.
 	 * 
 	 * @param comparator the comparator
 	 */
 	public void setComparator(Comparator<ExtensionElement> comparator) {
 		this.comparator = comparator;
 	}
 
 	/**
 	 * Returns the first {@link ExtensionElement} in the list.
 	 * 
 	 * @return {@link ExtensionElement}, null or a {@link ExtensionPointException} is thrown, depending on your config (
 	 *         {@link #setThrowException(boolean)}
 	 */
 	public ExtensionElement getFirst() {
 		if (elements.size() > 0) {
 			return elements.get(0);
 		}
 		return (ExtensionElement) handleErrorOrNull(exceptionInsteadOfNull, null);
 	}
 
 	/**
 	 * Returns the wrapped extension elements.
 	 * 
 	 * @return list of {@link ExtensionElement}
 	 */
 	public List<ExtensionElement> getExtensionElements() {
 		return Collections.unmodifiableList(elements);
 	}
 
 	/**
 	 * Set whether null should be returned or exception should be thrown by this class.
 	 * 
 	 * @param b true to throw exceptions
 	 * @return returns this, in order to allow chaining method calls
 	 */
 	public ExtensionPoint setThrowException(boolean b) {
 		this.exceptionInsteadOfNull = b;
 		return this;
 	}
 
 	// public void batch(ForEach expt) {
 	// for (ExtensionElement element : elements) {
 	// boolean throwException = element.getThrowException();
 	// element.setThrowException(true);
 	// try {
 	// expt.execute(element);
 	// } catch (ExtensionPointException e) {
 	// // do nothing
 	// }
 	// element.setThrowException(throwException);
 	// }
 	// }
 
 	/**
 	 * This method handles on basis of {@link #setThrowException(boolean)} whether null is returned or an exception is
 	 * thrown.
 	 * 
 	 * @param useException chosen option
 	 * @param expOrNull exception which will be wrapped, or null, for which an exception can be genereated
 	 * @return null, or a {@link ExtensionPointException} is thrown
 	 */
 	protected static Object handleErrorOrNull(boolean useException, Exception expOrNull) {
 		if (useException) {
 			if (expOrNull == null) {
 				throw new ExtensionPointException("Value not found.");
 			}
 			logException(expOrNull);
 			throw new ExtensionPointException(expOrNull);
 		}
 		return null;
 	}
 
 	/**
 	 * Convenience method for logging.
 	 * 
 	 * @param e exception
 	 */
 	protected static void logException(Exception e) {
 		Activator.getDefault().logException("An exception occurred while using an ExtensionPoint", e);
 	}
 
 	/**
 	 * Returns the number of {@link ExtensionElement}.
 	 * 
 	 * @return size
 	 */
 	public int size() {
 		return this.elements.size();
 	}
 }
