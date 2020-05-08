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
 
 import org.eclipse.swt.events.SelectionEvent;
 
 import org.eclipse.riena.core.util.ListenerList;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IRidget;
 
 /**
  * This class notifies a collection of action {@link IActionListener}s when a
  * widget is selected.
  */
 public class ActionObserver extends AbstractObserver<IActionListener> {
 
 	public ActionObserver(IRidget source) {
 		super(source);
 	}
 
 	protected void fireAction(SelectionEvent evt) {
 		ListenerList<IActionListener> listeners = getListeners();
 		if (listeners != null) {
 			for (IActionListener listener : listeners.getListeners()) {
 				listener.callback();
 			}
 		}
 	}
 
 }
