 /*
  * Created on Aug 19, 2006
  */
 package zz.utils.list;
 
 import java.util.NoSuchElementException;
 
 /**
  * A re-implementation of a linked list, with the entry structure public.
  * @author gpothier
  */
 public class NakedLinkedList<E>
 {
 	private int itsSize = 0;
 	private Entry<E> itsRoot;
 	
 	
 	public NakedLinkedList()
 	{
 		itsRoot = new Entry<E>(null);
 		itsRoot.setPrev(itsRoot);
 		itsRoot.setNext(itsRoot);
 	}
 	
 	/**
 	 * Creates a new entry. Subclasses can override this method to create
 	 * custom entries.
 	 */
	protected Entry<E> createEntry(E aElement)
 	{
 		return new Entry<E>(aElement);
 	}
 	
 	public int size()
 	{
 		return itsSize;
 	}
 	
 	public void addAfter(Entry<E> aBase, Entry<E> aEntry)
 	{
 		assert aEntry.getNext() == null;
 		assert aEntry.getPrev() == null;
 		
 		aEntry.setPrev(aBase);
 		aEntry.setNext(aBase.getNext());
 		aBase.getNext().setPrev(aEntry);
 		aBase.setNext(aEntry);
 		
 		itsSize++;
 	}
 
 	public E getLast()
 	{
 		return getLastEntry().getValue();
 	}
 	
 	public Entry<E> getLastEntry()
 	{
 		Entry<E> theLast = itsRoot.getPrev();
 		if (theLast == itsRoot) throw new NoSuchElementException();
 		return theLast;
 	}
 	
 	public void addLast(E aElement)
 	{
 		addLast(createEntry(aElement));
 	}
 	
 	public void addLast(Entry<E> aEntry)
 	{
 		addAfter(itsRoot.getPrev(), aEntry);
 	}
 	
 	public E getFirst()
 	{
 		return getFirstEntry().getValue();
 	}
 	
 	public Entry<E> getFirstEntry()
 	{
 		Entry<E> theFirst = itsRoot.getNext();
 		if (theFirst == itsRoot) throw new NoSuchElementException();
 		return theFirst;
 	}
 	
 	public void addFirst(E aElement)
 	{
 		addFirst(createEntry(aElement));
 	}
 	
 	public void addFirst(Entry<E> aEntry)
 	{
 		addAfter(itsRoot, aEntry);
 	}
 	
 	public void remove(Entry<E> aEntry)
 	{
 		aEntry.getPrev().setNext(aEntry.getNext());
 		aEntry.getNext().setPrev(aEntry.getPrev());
 		aEntry.setPrev(null);
 		aEntry.setNext(null);
 		itsSize--;
 	}
 	
 	public static class Entry<E>
 	{
 		private Entry<E> itsNext;
 		private Entry<E> itsPrev;
 		private E itsValue;
 		
 		public Entry(E aValue)
 		{
 			itsValue = aValue;
 		}
 
 		Entry<E> getNext()
 		{
 			return itsNext;
 		}
 		
 		void setNext(Entry<E> aNext)
 		{
 			itsNext = aNext;
 		}
 		
 		Entry<E> getPrev()
 		{
 			return itsPrev;
 		}
 		
 		void setPrev(Entry<E> aPrev)
 		{
 			itsPrev = aPrev;
 		}
 		
 		public E getValue()
 		{
 			return itsValue;
 		}
 		
 		public void setValue(E aValue)
 		{
 			itsValue = aValue;
 		}
 		
 		public boolean isAttached()
 		{
 			assert (getNext() == null) == (getPrev() == null);
 			return getNext() != null;
 		}
 	}
 }
