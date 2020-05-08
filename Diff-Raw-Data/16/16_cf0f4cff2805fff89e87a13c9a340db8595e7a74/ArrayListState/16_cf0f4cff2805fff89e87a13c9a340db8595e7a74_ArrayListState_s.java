 package ecologylab.xml;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
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
 
 	public void add(ElementState elementState)
 	{
 		try {
 			addNestedElement(elementState);
 		}catch (XmlTranslationException e) {
 			debug("ERROR: " + e.getMessage() + " while translating");
 		}		
 	}
     
     public ElementState remove(int i)
     {
         return (ElementState) set.remove(i);
     }
     
     public Iterator iterator()
     {
         return set.iterator();
     }
     
     public void add(int i, ElementState obj)
     {
         set.add(i, obj);
     }
 	
     public ElementState get(int i)
     {
        if (i > set.size())
         {
             return null;
         } else
         {
             return (ElementState) set.get(i);
         }
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
