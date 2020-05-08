 /******************************************************************************
  * Copyright (c) David Orme and others
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    David Orme - initial API and implementation
  ******************************************************************************/
 package com.coconut_palm_software.possible.internal;
 
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 
 import com.coconut_palm_software.possible.Nulls;
 import com.coconut_palm_software.possible.Possible;
 
 
 /**
 * An Possible instance that contains a value.
  * 
 * @param <T> The type that this Possible<T> is encapsulating.
  */
 public final class Some<T> extends Possible<T> {
     private final T value;
     private IStatus status = null;
  
     public Some(T value) {
         this.value = value;
     }
  
     public Some(T value, IStatus status) {
         this.value = value;
         this.status = status;
     }
  
     /* (non-Javadoc)
      * @see org.eclipse.e4.core.functionalprog.optionmonad.Option#get()
      */
     public T get() {
         return value;
     }
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.e4.core.functionalprog.optionmonad.Option#getOrSubstitute(java.lang.Object)
 	 */
 	public T getOrSubstitute(T defaultValue) {
 		return value;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.e4.core.functionalprog.optionmonad.Option#getOrThrow(java.lang.Throwable)
 	 */
 	public <E extends Throwable> T getOrThrow(E exception) {
 		return value;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.e4.core.functionalprog.optionmonad.Option#hasValue()
 	 */
 	public boolean hasValue() {
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.e4.core.functionalprog.optionmonad.Option#getStatus()
 	 */
 	public IStatus getStatus() {
 		return Nulls.valueOrSubstitute(status, Status.OK_STATUS);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.coconut_palm_software.possible.Possible#contains(java.lang.Object)
 	 */
 	@Override
 	public boolean contains(Object o) {
 		return value.equals(o);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.coconut_palm_software.possible.Possible#isEmpty()
 	 */
 	@Override
 	public boolean isEmpty() {
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.coconut_palm_software.possible.Possible#size()
 	 */
 	@Override
 	public int size() {
 		return 1;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.coconut_palm_software.possible.Possible#toArray()
 	 */
 	@Override
 	public Object[] toArray() {
 		return new Object[] {value};
 	}
 
 	/* (non-Javadoc)
 	 * @see com.coconut_palm_software.possible.Possible#toArray(A[])
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public <A> A[] toArray(A[] a) {
 		if (a.length < 1) {
 			a = (A[])java.lang.reflect.Array.newInstance(
 					a.getClass().getComponentType(), 1);
 		}
 		a[0] = (A) value;
 		if (a.length > 1) {
 			a[1] = null;
 		}
 		return a;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Iterable#iterator()
 	 */
 	@Override
 	public Iterator<T> iterator() {
 		return new Iterator<T>() {
 			boolean returnedValue = false;
 
 			@Override
 			public boolean hasNext() {
 				return !returnedValue;
 			}
 
 			@Override
 			public T next() {
 				if (hasNext()) {
 					returnedValue = true;
 					return value;
 				}
 				throw new IndexOutOfBoundsException("Past end of collection.");
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}};
 	}
 }
  
