 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 
 import org.eclipse.riena.core.util.ListenerList;
 import org.eclipse.riena.ui.ridgets.IRidget;
 
 /**
  * This class notifies a collection of listeners of the type T when a widget is
  * selected.
  */
 abstract class AbstractObserver<T> extends SelectionAdapter {
 
 	private final IRidget source;
 
 	private ListenerList<T> listeners;
 
 	/**
 	 * Create a new instance.
 	 * 
 	 * @param source
 	 *            the ridget where the selection occured; never null
 	 */
 	public AbstractObserver(IRidget source) {
 		Assert.isNotNull(source);
 		this.source = source;
 	}
 
 	@Override
 	public final void widgetSelected(SelectionEvent evt) {
 		fireAction(evt);
 	}
 
 	/**
 	 * Adds a listener to the collection of listeners which are notified. Adding
 	 * the same listener twice has no effect.
 	 * 
 	 * @param listener
 	 *            a listener instance of type T (non-null)
 	 * @throws RuntimeException
 	 *             if IActionListener is null
 	 */
 	public final void addListener(T listener) {
 		Assert.isNotNull(listener, "listener is null"); //$NON-NLS-1$
 		if (listeners == null) {
			listeners = new ListenerList<T>(listener.getClass());
 		}
 		listeners.add(listener);
 	}
 
 	/**
 	 * Removes a listener from the collection of listeners which are notified.
 	 * 
 	 * @param listener
 	 *            a listener instance of type T
 	 */
 	public final void removeListener(T listener) {
 		if (listeners != null) {
 			listeners.remove(listener);
 		}
 	}
 
 	/**
 	 * This method forwards the given SelectionEvent to the collection of
 	 * listeners.
 	 * <p>
 	 * Must be implemented by subclasses. Subclasses are free to create an
 	 * entirely new event and forward that instead of the original one, if
 	 * necessary.
 	 */
 	protected abstract void fireAction(SelectionEvent evt);
 
 	/**
 	 * Get the ridget where the selection occurred.
 	 * 
 	 * @return an IRidget instance; never null
 	 */
 	protected final IRidget getSource() {
 		return source;
 	}
 
 	/**
 	 * Get the collection of listeners.
 	 * 
 	 * @return A ListenerList or <b>null</b>, if no listeners have been added.
 	 *         May be empty.
 	 */
 	protected final ListenerList<T> getListeners() {
 		return listeners;
 	}
 }
