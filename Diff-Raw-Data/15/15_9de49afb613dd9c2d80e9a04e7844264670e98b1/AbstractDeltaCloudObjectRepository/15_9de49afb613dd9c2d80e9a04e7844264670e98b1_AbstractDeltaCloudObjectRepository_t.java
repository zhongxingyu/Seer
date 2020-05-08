 /*******************************************************************************
  * Copyright (c) 2010 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.deltacloud.core;
 
 import java.lang.reflect.Array;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * @author André Dietisheim
  *
  * @param <E> Element to store
  * @param <C> Criteria to match element against
  */
 public abstract class AbstractDeltaCloudObjectRepository<E extends IDeltaCloudElement, C> {
 
 	private List<E> objects = new ArrayList<E>();
 	// TODO switch to readwrite lock to gain performance
 	private Lock lock = new ReentrantLock();
 	private Class<E> typeClass;
 
 	protected AbstractDeltaCloudObjectRepository(Class<E> typeClass) {
 		this.typeClass = typeClass;
 	}
 
 	public E[] add(E object) {
 		try {
 			lock();
 			objects.add(object);
 			return get();
 		} finally {
 			unlock();
 		}
 	}
 
 	public E[] add(Collection<E> objects) {
 		try {
 			lock();
 			for (E object : objects) {
 				objects.add(object);
 			}
 			return get();
 		} finally {
 			unlock();
 		}
 	}
 
 	public E[] clear() {
 		try {
 			lock();
 			objects.clear();
 			return get();
 		} finally {
 			unlock();
 		}
 	}
 
 	public E[] get() {
 		try {
 			lock();
  			@SuppressWarnings("unchecked")
 			E[] objectArray = (E[]) objects.toArray((E[]) Array.newInstance(typeClass, objects.size()));
 			return objectArray;
 		} finally {
 			unlock();
 		}
 	}
 
	public int indexOf(E object) {
		try {
			lock();
			return objects.indexOf(object);
		} finally {
			unlock();
		}
	}
	
 	protected E getById(C criteria) {
 		try {
 			lock();
 			E matchingObject = null;
 			for (E object : objects) {
 				// TODO: use comparator
 				if (matches(criteria, object)) {
 					matchingObject = object;
 					break;
 				}
 			}
 			return matchingObject;
 		} finally {
 			unlock();
 		}
 	}
 
 	protected abstract boolean matches(C criteria, E object);
 
 	public void remove(DeltaCloudInstance instance) {
 		try {
 			lock();
 			objects.remove(instance);
 		} finally {
 			unlock();
 		}
 	}
 
 	protected void lock() {
 		lock.lock();
 	}
 
 	protected void unlock() {
 		lock.unlock();
 	}
 }
