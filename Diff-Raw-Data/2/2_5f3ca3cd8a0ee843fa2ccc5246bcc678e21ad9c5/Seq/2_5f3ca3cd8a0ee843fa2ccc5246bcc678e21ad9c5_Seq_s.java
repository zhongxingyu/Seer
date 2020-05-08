 package collection;
 
 /**
  * A generic persistent list interface. Every mutation operation
  * creates new modified list instance, the previous version is left
 * untouched.
  *
  * @param <T> Element type.
  */
 public interface Seq<T> {
   /**
    * Get the first element. The first element is the most
    * recently {@link #cons(Object) consed} one.
    *
    * @return The most recently consed element.
    * @throws RangeException If list is empty.
    */
   T head() throws RangeException;
 
   /**
    * Get list without the first element. The first element
    * is the most recently {@link #cons(Object) consed} one.
    *
    * @return The list without the most recently consed element.
    * @throws RangeException If list is empty.
    */
   Seq<T> tail() throws RangeException;
 
   /**
    * Prepend element to the head of this list. The prepended
    * element index is zero.
    *
    * @param v An element to prepend.
    * @return Updated list.
    */
   Seq<T> cons(T v);
 
   /**
    * Append element to the tail of this list. The appended
    * element index is {@link #size() size()} - 1.
    *
    * @param v An element to append.
    * @return Updated list.
    */
   Seq<T> snoc(T v);
 
   /**
    * Concatenate two lists.
    *
    * @param that The list whose elements to append to this one.
    * @return Updated list.
    */
   Seq<T> concat(Seq<T> that);
 
   /**
    * Get list size.
    *
    * @return Number of elements in this list.
    */
   int size();
 
   /**
    * Get element at the specified position.
    *
    * @param index Element index, where 0 is the index
    *              of the most recently consed element, 1 is the index
    *              of the next element and so on.
    * @return The element with the specified index.
    * @throws RangeException If element index is out of range.
    */
   T nth(int index) throws RangeException;
 
   class RangeException extends IllegalStateException {
     public RangeException() {}
   }
 }
