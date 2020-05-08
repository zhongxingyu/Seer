 /**
  * Copyright (c) Stefan Münchow. All rights reserved.
  * 
  * The use and distribution terms for this software are covered by the
  * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
  * which can be found in the file epl-v10.html at the root of this distribution.
  * By using this software in any fashion, you are agreeing to be bound by
  * the terms of this license.
  * You must not remove this notice, or any other, from this software.
  **/
 
 package com.stefanmuenchow.collections;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import com.stefanmuenchow.functors.Predicate;
 import com.stefanmuenchow.functors.UnaryFunction;
 
 /**
 * A SmartList is an ordered collection that can contain
  * duplicate elements. Elements can be accessed by their integer index and
  * can be inserted at a specific position of the list.
  * 
  * @see List
  * @see SmartCollection
  * @see SmartArrayList
  * @see SmartLinkedList
  *
  * @author Stefan Münchow
  */
 public interface SmartList<E> extends List<E>, SmartCollection<E> {
  
     /**
      * Adds an element at the specified index and returns the collection. 
      * Modifies the original list.
      * 
      * @param index          Index to add element at
      * @param element        Element to be added
      * @return               List with element added
      */
     SmartList<E> addReturn(int index, E element);
     
     /**
      * Adds all elements of coll at the specified index and returns the 
      * collection. Modifies the original list.
      * 
      * @param index          Index to add elements at
      * @param c              Collections of elements to be added
      * @return               List with elements added
      */
     SmartList<E> addAllReturn(int index, Collection<E> c);
     
     /**
      * Removes the element at the specified index and returns the collection. 
      * Modifies the original list.
      * 
      * @param index          Index to remove element from
      * @return               List with element removed
      */
     SmartList<E> removeIndexReturn(int index);
     
     /**
      * Replaces the element at the specified index with the specified element. 
      * Modifies the original list.
      * 
      * @param index          Index to replace element at
      * @param elem           Element to be stored at specified position
      * @return               List with element replaced
      */
     SmartList<E> setReturn(int index, E elem);
     
     /**
      * Returns the first element in the List. If the list is empty, an 
      * exception is thrown.
      *
      * @throws NoSuchElementException     If list is empty
      * @return                            First element of the list
      */
     E head() throws NoSuchElementException;
 
     /**
      * Returns a new list without the first element. If the list is empty, an 
      * exception is thrown. The original list remains unchanged.
      *
      * @throws UnsupportedOperationException     If list is empty
      * @return                                   List without first element
      */
     SmartList<E> tail() throws UnsupportedOperationException;
 
     /**
      * Returns the last element of the list. If the list is empty, an exception 
      * is thrown. 
      *
      * @throws NoSuchElementException     If list is empty
      * @return                            Last element of the list
      */
     E last() throws NoSuchElementException;
 
     /**
      * Gets the element at the specified index. If the index is not in the
      * range of valid indices, defaultVal is returned.
      *
      * @param index          Index of element to get
      * @param defaultVal     Value to return if index is not valid
      * @return               Element at index or defaultVal
      */
     E get(int index, E defaultVal);
 
     /**
      * Takes the first n elements of the list and returns the result. If
      * n is greater than the size of the list, the resulting list contains
      * all elements of the original list. A new list is created.
      *
      * @param n       Number of elements to take from beginning of list
      * @return        New list containing the first n elements
      */
     SmartList<E> take(int n);
 
     /**
      * Removes the first n elements of the list and returns the result. If
      * n is greater than the size of the list, the resulting list is empty. 
      * A new list is created.
      *
      * @param n       Number of elements to remove from beginning of list
      * @return        New list without first n elements
      */
     SmartList<E> drop(int n);
 
     /**
      * Takes elements from list until pred evaluates to <code>false</code>
      * the first time. A new list is created.
      *
      * @param pred       Predicate to pick elements
      * @return           Prefix list
      */
     SmartList<E> takeWhile(Predicate<? super E> pred);
 
     /**
      * Removes elements from list until pred evaluates to <code>false</code> 
      * the first time. A new list is created.
      *
      * @param pred       Predicate to pick elements
      * @return           Postfix list
      */
     SmartList<E> dropWhile(Predicate<? super E> pred);
     
     /**
      * Splits the list at the given index and returns a tuple containing both
      * sublists.
      * 
      * @param index      Index to split list at
      * @return           Tuple containg two lists
      */
     Tuple<SmartList<E>, SmartList<E>> splitAt(int index);
     
     /**
      * Partitions a list by the given predicate. The result is a Tuple of two 
      * lists: One list containing all elements that match the predicate and
      * a second list containing all elements that do not match the predicate.
      *  
      * @param pred       Predicate to select elements
      * @return           Tuple containg two lists
      */
     Tuple<SmartList<E>, SmartList<E>> partition(Predicate<? super E> pred);
 
     /**
      * Removes all duplicate values from list. Modifies the original list.
      * 
      * @return           List without duplicate elements
      */
     SmartList<E> removeDuplicates();
 
     /**
      * Inserts the specified elem between each two elements of the list. 
      * Modifies the original list.
      *
      * @param elem       Element to insert
      * @return           List containing elem at all odd indices
      */
     SmartList<E> intersperse(E elem);
 
     /**
      * Creates a {@link SmartList} from two lists. The resulting list contains
      * tuples with one element from the first list and one element from the 
      * second list (at the corresponding index) as its second value. If one 
      * list has less elements than the other one, these elements are discarded.
      *
      * @param anotherList    List to zip with
      * @return               {@link SmartList} build from the two lists
      */
     <T> SmartList<Tuple<E, T>> zipWith(List<T> anotherList);
     
     /**
      * Returns a list of the list indices. The original list remains unchanged.
      *
      * @return               List of indices
      */
     SmartList<Integer> getIndicesList();
 
     /**
      * Creates a {@link SmartMap} using the elements in the list as key and 
      * the number of their occurence in the list as value.
      *
      * @return               {@link SmartMap} containing elements and occurence
      *                       count
      */
     SmartMap<E, Integer> getOccurenceCountMap();
 
     /**
      * Reverses the ordering of the elements in the list and returns it. Changes
      * the original list.
      *
      * @return               Reversed list
      */
     SmartList<E> reverse();
 
     /**
      * Returns the list size without null elements. If there are no null
      * elements, the result is equal to {@link List#size()}.
      *
      * @return               List size without null elements
      */
     int sizeWithoutNulls();
     
     /**
      * Zips this list with anotherList, so that the elements at corresponding
      * positions form a tuple and and performs a map operation on the resulting 
      * list of tuples. 
      * 
      * @see                  SmartList#zipWith(List)
      * @see                  SmartList#map(UnaryFunction)
      * 
      * @param anotherList    List to zip with
      * @param function       Function for map operation
      * @return               Resulting list
      */
     <R, T> SmartList<R> map(List<T> anotherList, UnaryFunction<R, Tuple<E, T>> function);
     
     /** Overridden methods from SmartCollection */
     
     /**
      * @see SmartCollection#addReturn(Object)
      */
     SmartList<E> addReturn(E elem);
 
     /**
      * @see SmartCollection#addAll(Collection)
      */
     SmartList<E> addAllReturn(Collection<E> coll);
     
     /**
      * @see SmartCollection#removeReturn(Object)
      */
     SmartList<E> removeReturn(E elem);
     
     /**
      * @see SmartCollection#removeAllReturn(Collection)
      */
     SmartList<E> removeAllReturn(Collection<E> coll);
     
     /**
      * @see SmartCollection#retainAllReturn(Collection)
      */
     SmartList<E> retainAllReturn(Collection<E> coll);
     
     /**
      * @see SmartCollection#filter(Predicate)
      */
     SmartList<E> filter(Predicate<? super E> predicate);
     
     /**
      * @see SmartCollection#remove(Predicate)
      */
     SmartList<E> remove(Predicate<? super E> predicate);
     
     /**
      * @see SmartCollection#replace(Object, Object)
      */
     SmartList<E> replace(E seek, E replacement);
     
     /**
      * @see SmartCollection#replace(Predicate, Object)
      */
     SmartList<E> replace(Predicate<? super E> predicate, E replacement);
     
     /**
      * @see SmartCollection#replace(Map)
      */
     SmartList<E> replace(Map<E, E> replacements);
     
     /**
      * @see SmartCollection#map(UnaryFunction)
      */
     @Override
     <R> SmartList<R> map(UnaryFunction<R, ? super E> function);
 
     /**
      * @see SmartCollection#castEach(Class)
      */
     @Override
     <T> SmartList<T> castEach(Class<T> clazz);
     
     /**
      * @see SmartCollection#toStandardCollection()
      */
     @Override
     List<E> toStandardCollection();
 }
