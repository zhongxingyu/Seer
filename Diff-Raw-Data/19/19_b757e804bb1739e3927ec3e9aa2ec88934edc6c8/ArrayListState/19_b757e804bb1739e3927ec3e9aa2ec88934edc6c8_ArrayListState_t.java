 package ecologylab.xml;
 
 import java.util.ArrayList;
 
 /**
  * An ElementState XML tree node that supports an ArrayList of
  * children (as well as whatever else you add to it).
  * 
  * @author andruid
  */
 public class ArrayListState extends ElementState
 {
 	public ArrayList set	=	new ArrayList();
 		
 	public ArrayListState()
 	{
 		super();
 	}
 
    /**
     * Add an element to our Collection.
     */
    	public void addNestedElement(ElementState elementState)
    throws XmlTranslationException
    {
    		set.add(elementState);
    	}
    	/**
    	 * Remove all elements from our Collection.
    	 *
    	 */
    	public void clear()
    	{
    		set.clear();
    	}
    	
    	/**
    	 * Get the number of elements in the set.
    	 * @return
    	 */
    	public int size()
    	{
    		return set.size();
    	}
 }
