 /*
  * Created on Jun 26, 2005
  */
 package ecologylab.collections;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * A set of FloatSetElements, that uses ArrayList as the storage medium.
  * 
  * @author andruid
  */
 public class ArrayListFloatSet<T extends FloatSetElement>
 extends ArrayList<T>
 implements BasicFloatSet<T>
 {
 	/**
 	 * 
 	 */
 	public ArrayListFloatSet() 
 	{
 		super();
 	}
 
 	/**
 	 * @param arg0
 	 */
 	public ArrayListFloatSet(int arg0)
 	{
 		super(arg0);
 	}
 
 	/**
 	 * @param arg0
 	 */
 	public ArrayListFloatSet(Collection arg0)
 	{
 		super(arg0);
 	}
 
    /**
 	* Delete an element from the set.
 	* If they are relevant, perhaps recompute internal structures, such as 
 	* incremental sums, or tree/heap balancing, depending on the value of the 
 	* recompute parameter.
 	* 
 	* @param el			The FloatSetElement element to delete.
 	* @param recompute	-1 for absolutely no recompute.
 	* 			 0 for recompute upwards from el.
 	* 			 1 for recompute all.
 	**/
 	public void delete(T el, int recompute)
 	{
 		remove(el);
 	}
 	public void insert(T element)
 	{
 		element.setSet(this);
 		int index	= size();
 		add(element);
 		element.setIndex(index);
 	}
 
    /**
     * Get the ith element in the set.
     * 
     * @param i
     * @return
     */
    public T getElement(int i)
    {
    	  return super.get(i);
    }
    /**
     * Get the last element in the set, or null if the set is empty.
     * 
     * @return
     */
    public T lastElement()
    {
 	  int size	= size();
    	  return (size == 0) ? null : get(size - 1);
    }

	@Override
	public void decrement(T el) 
	{
	}
 }
