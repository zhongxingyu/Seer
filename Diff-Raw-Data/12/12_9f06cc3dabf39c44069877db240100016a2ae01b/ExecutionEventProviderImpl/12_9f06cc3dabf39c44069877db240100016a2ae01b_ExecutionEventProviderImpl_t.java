 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Tanja Mayerhofer - initial API and implementation
  */
 package org.modelexecution.fumldebug.core.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.modelexecution.fumldebug.core.ExecutionEventListener;
 import org.modelexecution.fumldebug.core.ExecutionEventProvider;
 import org.modelexecution.fumldebug.core.event.Event;
 
 public class ExecutionEventProviderImpl implements ExecutionEventProvider {
 
 	private List<ExecutionEventListener> listeners = new ArrayList<ExecutionEventListener>();

 	public void addEventListener(ExecutionEventListener listener) {
		listeners.add(listener);
 	}
 
 	public void removeEventListener(ExecutionEventListener listener) {
 		listeners.remove(listener);
 	}
 
 	public void notifyEventListener(Event event) {
		for (ExecutionEventListener l : new ArrayList<ExecutionEventListener>(
				listeners)) {
 			l.notify(event);
 		}
 	}

 }
