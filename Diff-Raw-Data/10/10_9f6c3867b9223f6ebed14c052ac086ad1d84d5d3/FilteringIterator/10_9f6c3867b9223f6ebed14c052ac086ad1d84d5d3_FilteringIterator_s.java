 package org.aj.testprograms.filteriterator;
 
 import java.util.Iterator;
 
 import org.aj.testprograms.filteriterator.conditionchecker.ObjectTest;
 
 /**
  * @author AJ
  * 
  */
 public class FilteringIterator<E> implements Iterator<E> {
 
 	private final Iterator<E> underlyingIte;
 	private final ObjectTest<E> conditionChecker;
 	private E nextValidObj;
 	private boolean nextPointerValid = false;
 
 	/**
 	 * 
 	 * @param orig
 	 *            - the underlying iterator
 	 * @param objChecker
 	 *            - the condition checker object
 	 */
 	public FilteringIterator(Iterator<E> orig, ObjectTest<E> objChecker) {
 		underlyingIte = orig;
 		conditionChecker = objChecker;
 	}
 
 	@Override
 	public boolean hasNext() {
 		boolean hasNext = false;
		if (nextValidObj == null) {
 			/**
 			 * The next pointer is currently null, so we must advance it using
 			 * the underlying iterator
 			 */
 			boolean notFound = true;
 			while (notFound) {
 				if (underlyingIte.hasNext()) {
 					/**
 					 * the underlying iterator has more elements
 					 */
 					E temp = underlyingIte.next();
 					if (conditionChecker.test(temp)) {
 						/**
 						 * temp meets the requirement, so set the next pointer
 						 * to this
 						 */
 						notFound = false;
 						nextValidObj = temp;
 						hasNext = true;
 					}
 				} else {
 					/**
 					 * the underlying iterator has no more elements, return
 					 * false
 					 */
 					hasNext = false;
 					// set notFound to false to break the loop.
 					notFound = false;
 				}
 			}
 		} else {
 			/**
 			 * the next pointer is pointing to an object that must have been set
 			 * in some previous iteration of calling hasNext - so set the return
 			 * boolean to true
 			 */
 			hasNext = true;
 		}
 		return hasNext;
 	}
 
 	@Override
 	public E next() {
 		E nextObj = null;
		if (nextValidObj == null) {
 			/**
 			 * The next pointer is currently null, so we must advance it using
 			 * the underlying iterator
 			 */
 			boolean notFound = true;
 			while (notFound) {
 				E temp = underlyingIte.next();
 				if (conditionChecker.test(temp)) {
 					/**
 					 * temp meets the requirement, so set the next pointer to
 					 * this. also set the nextPointerValid to true, a call 
 					 * to remove will remove this element
 					 */
 					notFound = false;
 					nextValidObj = temp;
 					nextObj = temp;
 					nextPointerValid = true;
 				}
 			}
 
 		} else {
 			/**
 			 * the next pointer is currently valid read the value into the
 			 * return obj reference and set the next pointer to null
 			 */
 			nextObj = nextValidObj;
 			nextValidObj = null;
			nextPointerValid = true;
 		}
 		return nextObj;
 	}
 
 	@Override
 	public void remove() {
 		if (nextPointerValid) {
 			/**
 			 * the flag will be valid
 			 * only when next returned a
 			 * valid element the last time
 			 * it was called
 			 */
 			underlyingIte.remove();
 			nextPointerValid = false;
 			nextValidObj = null;
 		} else {
 			throw new IllegalStateException("No valid element to remove");
 		}
 	}
 }
