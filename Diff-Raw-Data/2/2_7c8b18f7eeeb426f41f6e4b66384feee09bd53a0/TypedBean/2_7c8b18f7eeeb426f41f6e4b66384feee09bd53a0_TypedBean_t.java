 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.beans.common;
 
 import org.eclipse.core.runtime.Assert;
 
 /**
  * Typed bean provides a typed value for simple adapter UI-Binding. The type of
  * the value has to be passed as a type parameter.<br>
  * The UI binding will retrieve the type value from the property
  * {@code TypedBean.PROP_VALUE} + "Type" (convention). Additionally, the
  * {@code TypedBean} will try to retrieve the type from the value.
  */
 public class TypedBean<T> extends AbstractBean {
 
 	/**
 	 * Key for the value property (PROP_VALUE = "value").
 	 */
 	public static final String PROP_VALUE = "value"; //$NON-NLS-1$
 
 	private T value;
 	private Class<T> type;
 
 	/**
 	 * Creates an empty {@code TypedBean} bean with the given type
 	 * 
 	 * @param type
 	 * 
 	 * @since 4.0
 	 */
 	public TypedBean(final Class<T> type) {
 		this.type = type;
 	}
 
 	/**
 	 * Creates a {@code TypedBean} bean with the given value.
 	 * <p>
 	 * <b>Note: </b>If there is no initial value please us the
 	 * {@code TypedBean(final Class<T> type)} constructor for providing a type
 	 * hint!
 	 * 
 	 * @param value
 	 */
 	public TypedBean(final T value) {
		Assert.isNotNull(value, "For a <null> value please use the TypedBean(final Class<T> type) constructor"); //$NON-NLS-1$
 		this.value = value;
 		guessType(value);
 	}
 
 	/**
 	 * Sets the value of this bean
 	 * 
 	 * @param value
 	 *            to set
 	 */
 	public void setValue(final T value) {
 		guessType(value);
 		final Object old = this.value;
 		this.value = value;
 		firePropertyChanged(PROP_VALUE, old, this.value);
 	}
 
 	private void guessType(final T value) {
 		if (value == null || type != null) {
 			return;
 		}
 		type = (Class<T>) value.getClass();
 	}
 
 	/**
 	 * Returns the value of this bean
 	 * 
 	 * @return value
 	 */
 	public T getValue() {
 		return value;
 	}
 
 	/**
 	 * Returns the value type of this bean
 	 * 
 	 * @return value
 	 * @since 4.0
 	 */
 	public Class<T> getValueType() {
 		return type;
 	}
 
 }
