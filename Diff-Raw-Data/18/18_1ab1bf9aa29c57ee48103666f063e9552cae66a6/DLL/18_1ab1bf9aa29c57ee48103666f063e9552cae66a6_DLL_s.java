 /*
  * Copyright 1996-2002 by Andruid Kerne. All rights reserved.
  * CONFIDENTIAL. Use is subject to license terms.
  */
 package ecologylab.collections;
 import java.awt.*;
 import java.util.Iterator;
 
 import ecologylab.generic.Debug;
 
 /**
  * Doubly linked list of GUI components. Maintains family tree.
  */
 public class DLL<T>
 extends Debug
 implements Iterable<T>
 {	
    private DLL<T>			prevSib;
    private DLL<T>			nextSib;
 
    private T				object;
 
    public DLL(T objectArg)
    {
       object	= objectArg;
    }
 /**
  * Add sibs after ourself.
  */
    public synchronized void addAfter(DLL<T> sibs)
    {
       sibs.setNext(nextSib);
       addToEnd(sibs);
    }
 /**
  * Add sibs to the end of this.
  * 
  * @param sibs	DLL for object added to the end of this.
  */
    public void addToEnd(DLL<T> sibs)
    {
    		if (sibs == this)
    		{
    			debug("ERROR!!!! adding to end of self!!!!");
    			Toolkit.getDefaultToolkit().beep();
    			Thread.dumpStack();
    		}
    		
       nextSib	= sibs;		   // ??? should this be setNext(sibs) -- sync
 //      setNext(sibs);
       sibs.setPrev(this);
    }
 /**
  * Prepend sibs to the start of this.
  * 
  * @param sibs	DLL for object added before this.
  */
    public void addAtBeginning(DLL<T> sibs)
    {
    		if (sibs == this)
    		{
    			debug("ERROR!!!! adding to end of self!!!!");
    			Toolkit.getDefaultToolkit().beep();
    			Thread.dumpStack();
    		}
    		
       prevSib	= sibs;		   // ??? should this be setNext(sibs) -- sync
 //      setNext(sibs);
       sibs.setNext(this);
    }
 /**
  * Remove I and I from the dll of sibs.
  */
    public synchronized void remove()
    {
 	   if (nextSib != null)
 		   nextSib.setPrev(prevSib);
 	   if (prevSib != null)
 		   prevSib.setNext(nextSib);
 	   prevSib	= null;
 	   nextSib	= null;
 	   //FIXME -- uncomment this out!
 //	   object	= null;		   // encourage gc ?? -- why is this commented out??? andruid 2/10/08
    }
 /**
  * Remove the relations of this node, while it is being manipulated in
  * the midst of mergeSort().
  */
    public void clear()
    {
       prevSib	= null;
       nextSib	= null;
 //      remove();
       object	= null;
    }
 /**
  * Remove the relations of this node, while it is being deleted.
  */
    public void clearStronger()
    {
       remove();
       object	= null;
    }
 /**
  * Remove the relations of this node, while it is being rebuilt, and
  * its associations are meaningless.
  */
    public void clearRelations()
    {
       prevSib	= null;
       nextSib	= null;
    }
 /**
  * Not usually for public consumption. Only available for special sorts.
  * With a getKey interface, the sort could be brought here, which will
  * be better oo design.
  */
    public synchronized void setNext(DLL<T> next)
    {
       nextSib	= next;
    }
    public synchronized void setPrev(DLL<T> prev)
    {
       prevSib	= prev;
    }
    public T getThis()
    {
       return object;
    }
    public synchronized T getNext()
    {
       return (nextSib == null) ? null : nextSib.object;
    }
    public synchronized T getPrev()
    {
       return (prevSib == null) ? null : prevSib.object;
    }
    public DLL<T> prev()
    {
       return prevSib;
    }
    public DLL<T> next()
    {
       return nextSib;
    }
    public DLL<T> mergeSortDescending(MergeD<T> mergeD, DLL<T> zSibs)
    {
 	   DLL<T> c			= this;
 	   int n			= 1;
 	   DLL<T> head		= new DLL<T>(null);
 	   head.setNext(c);
 	   DLL<T> a;
 	   do 
 	   {
 		   DLL<T> todo	= head.next();
 		   c			= head;
 		   int toMove	= n - 1;
 //		   System.out.println("");
 //		   visualize.CollageElement.printPieces(c.next(), zSibs);
 //		   System.out.println("start inner loop moving " + toMove);
 		   do
 		   {
 			   // merge lists of size n
 			   DLL<T> t	= todo;
 			   a		= t;
 			   for (int i=1; i<=toMove; i++)
 				   t	= t.next();
 			   DLL<T> b	= t.next();
 			   t.setNext(zSibs);
 			   t		= b;
 			   for (int i=1; i<=toMove; i++)
 				   t	= t.next();
 			   todo	= t.next();
 			   t.setNext(zSibs);
 //			   System.out.println("mergeDescending(a: ");
 //			   visualize.CollageElement.printPieces(a, zSibs);
 //			   System.out.println("mergeDescending(b: ");
 //			   visualize.CollageElement.printPieces(b, zSibs);
 			   c.setNext(mergeD.mergeDescending(a,b));
 //			   System.out.println("returns: ");
 //			   visualize.CollageElement.printPieces(c.next(), zSibs);
 			   int twoN	= n + n;
 			   for (int i=1; i<=twoN; i++)
 				   c	= c.next();
 		   } while (todo != zSibs);
 		   n	       += n;
 	   } while (a != head.next());
 
 	   return head.next();
    }
 
    /**
    * @param object the object to set
    */
    public void setObject(T object)
    {
        this.object = object;
    }
    
    /**
     * Create an {@code Iterator} object for this {@code DLL}. 
     * 
     * @return an {@code Iterator} object for iterating over all elements in this {@code DLL}.
     */
    public Iterator<T> iterator()
    {
 	   return new DLLIterator();
    }
    
    /**
     *  Iterator class for building an iterator object out of the DLL. Uses current 
     *  DLL structure for iteration, starting at the DLL object that instantiates it.
     * 
     */
    private class DLLIterator implements Iterator<T>
    {
 	   DLL<T> currentNode = DLL.this;
 	 
 	   public boolean hasNext()
 	   {
		   return (currentNode != null && currentNode.object != null);
 	   }
 	
 	   public T next()
 	   {
 		   T currentObject 	= currentNode.object;
 		   currentNode 		= currentNode.next();
 		   
 		   return currentObject;
 	   }
 		
 	   public void remove()
 	   {
 		   // optional method that is not needed in our case. 
 	   }
 	   
    }
 }
