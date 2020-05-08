 package wicket.contrib.data.model.sandbox;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import wicket.contrib.data.model.UnimplementedList;
 
 /**
  * A List that behaves like a pageable object. It works with a count and a
  * window. The count is used for the total number of elements that are available
  * for this list, and the window is used for the currently loaded elements.
  * <p>
  * This list figures out what elements need to be loaded when asked for
  * elements. The actual loading of the count and elements is delegated to
  * subclasses. Ordering is supported through the add/remove ordering methods.
  * </p>
  * <p>
  * Detachable operation is supported by {@link #getDetachableModel()}, however,
  * for expensive queries (such as search) it may be advisable to set the window
  * several times larger than the paging size and not use detaching so that the
  * search is rerun less often. Also note tha this class is most efficient when
  * it's window size is exactly that of the page containing it, however it's best
  * to overestimate: a window one item smaller than the page will result in two
  * selects for each request.
  * </p>
  * <p>
  * Not all {@link java.util.List}methods are supported. Unsupported methods
  * will throw {@link java.lang.IllegalStateException}s.
  * </p>
  * <p>
  * NOTE: this list is meant for specific situations, like backing up
  * {@link wicket.markup.html.list.PageableListView}s, that use sequential
  * addressing of the list. Random access is inefficient, as a number of rows
  * (the page) is loaded at once for each window. Hence it IS efficent for
  * sequential access.
  * </p>
  * 
  * @author Phil Kulak
  * @author Eelco Hillenius
  */
 public abstract class OrderedPageableList extends UnimplementedList
 {
 	private List window = null;
 
 	private int windowStart = 0;
 
 	private int windowSize = 10;
 
 	private int totalElements = -1;
 
 	private List ordering = new ArrayList(3);
 
 	/**
 	 * Default constructor. A detachable list is created with a default window
 	 * size of 10.
 	 */
 	public OrderedPageableList()
 	{
 	}
 
 	/**
	 * Creates a list with the given window size and detachable status.
 	 * 
 	 * @param windowSize
 	 *            the number of elements loaded at one time
 	 */
 	public OrderedPageableList(int windowSize)
 	{
 		this.windowSize = windowSize;
 	}
 
 	/**
 	 * Returns items starting from the start index, with size no greater then
 	 * max.
 	 * 
 	 * @param start the index to start at
 	 * @param max the maximum elements to return
 	 * @param ordering the list of orderings to use                  
 	 * @return
 	 */
 	protected abstract List getItems(int start, int max, List ordering);
 
 	/**
 	 * Returns the total elements.
 	 */
 	protected abstract int getCount();
 
 	/**
 	 * Adds an ordering to the end of the list. If the field already exists, the
 	 * direction is switched.
 	 */
 	public OrderedPageableList addOrder(String field)
 	{
         // Detach the object since it's state needs to be reloaded.
         onDetach();
         
 		ListOrder order = new ListOrder(field);
 		int i = ordering.indexOf(order);
 
 		if (i != -1)
 			((ListOrder) ordering.get(i)).switchOrder();
 		else
 			ordering.add(order);
 		
        return this;
 	}
 
 	/**
 	 * Removes the ordering on the given field.
 	 */
 	public OrderedPageableList removeOrder(String field)
 	{
 		ordering.remove(new ListOrder(field));
 		return this;
 	}
 
 	/**
 	 * Gets the element at the given position. If the index is outside of the
 	 * currently loaded window, the window that the index should be in is
 	 * loaded. Forward traversal is assumed.
 	 * 
 	 * @see java.util.List#get(int)
 	 */
 	public final Object get(int index)
 	{
 		if (window == null)
 			resetWindow(index);
 
		if (index > size())
 			throw new IndexOutOfBoundsException("Index greater then size - 1");
 		if (index < 0)
 			throw new IndexOutOfBoundsException("Index less than zero.");
 
 		int relativeIndex = index - windowStart;
 
 		if (relativeIndex < 0 || relativeIndex >= windowSize)
 		{
 			resetWindow(index);
 			relativeIndex = 0;
 		}
 		return window.get(relativeIndex);
 	}
 
 	/**
 	 * @see java.util.List#size()
 	 */
 	public final int size()
 	{
 		if (totalElements == -1)
 			totalElements = getCount();
 
 		return totalElements;
 	}
 
 	/**
 	 * A convenience method for obtaining a DetachableModel containing this
 	 * list.
 	 */
 	public DetachableList getDetachableModel()
 	{
 		return new DetachableList(this);
 	}
 
 	public void onDetach()
 	{
 		window = null;
 	}
 
 	private void resetWindow(int start)
 	{
 		windowStart = start;
 		window = getItems(windowStart, windowSize, ordering);
 	}
 }
