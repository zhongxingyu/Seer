 /*******************************************************************************
  * Copyright (c) 2009, 2010 Dejan Spasic
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.phpsrc.eclipse.pti.tools.phpmd.model;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 public class ViolationManager {
 	private static ViolationManager manager;
 	private Collection<IViolation> violations;
 
 	private List<IViolationManagerListener> listeners = new ArrayList<IViolationManagerListener>();
 
 	private ViolationManager() {
		violations = new HashSet<IViolation>();
 	}
 
 	public static ViolationManager getManager() {
 		if (null == manager) {
 			manager = new ViolationManager();
 		}
 		return manager;
 	}
 
 	public IViolation[] getViolations() {
 		if (null == violations) {
 			return new IViolation[] {};
 		}
 		return violations.toArray(new IViolation[violations.size()]);
 	}
 
 	private void fireViolationsChanged(IViolation[] violationsAdded, IViolation[] violationsRemoved) {
 		ViolationManagerEvent event = new ViolationManagerEvent(this, violationsAdded, violationsRemoved);
 
 		for (IViolationManagerListener listener : listeners) {
 			listener.violationsChanged(event);
 		}
 	}
 
 	public void addViolation(IViolation newViolation) {
 		if (null == newViolation)
 			return;
 		addViolation(new IViolation[] { newViolation });
 	}
 
 	public void addViolation(IViolation[] newViolations) {
 		if (null == newViolations)
 			return;
 
 		Collection<IViolation> items = new HashSet<IViolation>(newViolations.length);
 
 		for (IViolation currentViolation : newViolations) {
 			if (null != currentViolation && violations.add(currentViolation)) {
 				items.add(currentViolation);
 			}
 		}
 
 		if (0 < items.size()) {
 			IViolation[] added = items.toArray(new IViolation[items.size()]);
 			fireViolationsChanged(added, IViolation.NONE);
 		}
 	}
 
 	public void removeViolation(IViolation oldViolation) {
 		if (null == oldViolation)
 			return;
 		removeViolation(new IViolation[] { oldViolation });
 	}
 
 	public void removeViolation(IViolation[] oldViolations) {
 		if (null == oldViolations)
 			return;
 
 		Collection<IViolation> items = new HashSet<IViolation>(oldViolations.length);
 
 		for (IViolation currentViolation : oldViolations) {
 			if (null != currentViolation && violations.remove(currentViolation)) {
 				items.add(currentViolation);
 			}
 		}
 
 		if (0 < items.size()) {
 			IViolation[] removed = items.toArray(new IViolation[items.size()]);
 			fireViolationsChanged(IViolation.NONE, removed);
 		}
 	}
 
 	public void addViolationManagerListener(IViolationManagerListener listener) {
 		if (!listeners.contains(listener))
 			listeners.add(listener);
 	}
 
 	public void removeViolationManagerListener(IViolationManagerListener listener) {
 		listeners.remove(listener);
 	}
 }
