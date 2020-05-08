 package br.uff.midiacom.xml.datatype.elementList;
 
 import br.uff.midiacom.xml.XMLElement;
 import br.uff.midiacom.xml.XMLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 
 /**
  * This class represents a list of xml elements. This element list does not
  * stores null elements or repeated elements.
  *
  * @param <T>
  *          the type of element stored in the list.
  * @param <P>
  *          type of the stored element parent.
  */
 public class ElementList<T extends XMLElement, P extends XMLElement> implements Iterable<T> {
 
     protected List<T> elements;
 
 
     /**
      * Element list constructor.
      */
     public ElementList() {
         elements = new ArrayList<T>();
     }
 
 
     /**
      * Returns the size of the list.
      *
      * @return
      *          integer representing the list size.
      */
     public int size() {
         return elements.size();
     }
 
 
     /**
      * Verifies if the list is empty.
      *
      * @return
      *          boolean indicating if the list is empty.
      */
     public boolean isEmpty() {
         return elements.isEmpty();
     }
     
 
     /**
      * Verifies if the list contains one specific element.
      *
      * @param element
      *          element to be verified.
      * @return
      *          true if the element exists in the list.
      * @throws XMLException
      *          if the element is null.
      */
     public boolean contains(T element) throws XMLException {
         if(element == null)
             throw new XMLException("Null element.");
 
         return elements.contains(element);
     }
 
 
     /**
      * Returns the list iterator.
      *
      * @return
      *          iterator of the list elements.
      */
     public Iterator<T> iterator() {
         return elements.iterator();
     }
     
 
     /**
      * Adds an element to the list.
      *
      * @param element
      *          element to be added.
      * @param parent
      *          element new parent.
      * @return
      *          true if the element was added to the list.
      * @throws XMLException
      *          if the element is null.
      */
     public boolean add(T element, P parent) throws XMLException {
         if(element == null)
             throw new XMLException("Null element.");
 
         for(T el : elements){
             if(el.compare(element)){
                 elements.remove(el);
                 break;
             }
         }
 
         if(elements.add(element)){
             element.setParent(parent);
             return true;
         }
         return false;
     }
     
 
     /**
      * Removes an element of the list.
      *
      * @param element
      *          element to be removed from the list.
      * @return
      *          true if the element was removed.
      * @throws XMLException
      *          if the element is null.
      */
     public boolean remove(T element) throws XMLException {
         if(element == null)
             throw new XMLException("Null element.");
 
         if(elements.remove(element)){
             element.setParent(null);
             return true;
         }
         return false;
     }
 
 
     /**
      * Adds a collection of elements in the list.
      *
      * @param clctn
      *          collection of elements to be added.
      * @return
      *          true if the elements were added.
      */
     public boolean addAll(Collection<? extends T> clctn) {
         return elements.addAll(clctn);
     }
 
 
     /**
      * Removes a collection of elements of the list.
      *
      * @param clctn
      *          collection of elements to be removed.
      * @return
      *          true if the elements were removed.
      */
     public boolean removeAll(Collection<?> clctn) {
         return elements.removeAll(clctn);
     }
 
 
     /**
      * Removes all the elements from the list.
      */
     public void clear() {
         elements.clear();
     }
 
 
     /**
      * Returns the element at an specific position.
      *
      * @param i
      *          position of the element to get.
      * @return
      *          the element at position i in the list.
      * @throws XMLException
      *          if the position is out of bounds.
      */
     public T get(int i) throws XMLException {
         if(i < 0 || i >= elements.size())
             throw new XMLException("Index out of bounds.");
 
         return elements.get(i);
     }
    
    
    /**
     * Returns the position of an element.
     *
     * @param element
     *          element to get the position.
     * @return
     *          the element position in the list.
     */
    public int getPosition(T element) {
        return elements.indexOf(element);
    }
 }
