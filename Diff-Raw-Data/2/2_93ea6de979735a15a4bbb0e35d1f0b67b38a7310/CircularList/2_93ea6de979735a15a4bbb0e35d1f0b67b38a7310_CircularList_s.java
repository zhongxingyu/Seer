import java.util.Iterable;
 
 public interface CircularList<E> extends Iterable<E> {
 
  /**
   * Determines whether a list is empty.
   * @return true if the list is empty, otherwise false
   */
  public boolean isEmpty();
 
  /**
   * Determines the length of a list.
   * @return the number of elements in the list without wrapping
   */
  public int size();
  
  /**
   * Removes all elements from the list.
   */
  public void clear();
 
  /**
   * Adds a new item to the end of the list.
   * @param item the new item to add
   * @return true if the list was modified
   */
  public boolean add(E item);
 
  /**
   * Adds a new item to the list at position index.  
   * Other items are shifted, not overwritten.
   * @param index where to add the new item
   * @param item the new item to add
   * @throws IndexOutOfBoundsException if index is negative
   */
  public void add(int index, E item) throws IndexOutOfBoundsException;
 
  /**
   * Remove and return the item at the given index.
   * @param index the position of the item to remove
   * @return the item that was removed
   * @throws IndexOutOfBoundsException if index is negative
   */
  public E remove(int index) throws IndexOutOfBoundsException;
 
  /**
   * Retrieve the item at the given index without altering the list.
   * @param index the position of the item to retrieve
   * @return the item at position index
   * @throws IndexOutOfBoundsException if index is negative
   */
  public E get(int index) throws IndexOutOfBoundsException;
  
  /**
   * Generate an iterator for the list.  The iterator should 
   * visit the items in a circular pattern.  As long as the
   * list is not empty, it should never stop.
   */
  public Iterator<E> iterator();
 }
