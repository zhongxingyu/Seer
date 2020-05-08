 package org.jdogma.concurrency.contract;
 
 import org.jdogma.concurrency.ThreadAccessController;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 
 /**
  * @author Kristian Rosenvold
  */
 public class DiagnosticHashSet<T> extends HashSet<T>
 {
     private final ThreadAccessController threadAccessController = new ThreadAccessController();
 
     @SuppressWarnings({"UnusedDeclaration"})
     public DiagnosticHashSet()
     {
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public DiagnosticHashSet( Collection<? extends T> c )
     {
         super( c );
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public DiagnosticHashSet( int initialCapacity, float loadFactor )
     {
         super( initialCapacity, loadFactor );
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public DiagnosticHashSet( int initialCapacity )
     {
         super( initialCapacity );
     }
 
 
     @Override
     public Iterator<T> iterator()
     {
         threadAccessController.checkThreadAccess();
         return super.iterator();
     }
 
     @Override
     public int size()
     {
         threadAccessController.checkThreadAccess();
         return super.size();
     }
 
     @Override
     public boolean isEmpty()
     {
         threadAccessController.checkThreadAccess();
         return super.isEmpty();
     }
 
     @Override
     public boolean contains( Object o )
     {
         threadAccessController.checkThreadAccess();
         return super.contains( o );
     }
 
     @Override
     public boolean add( T t )
     {
        if (threadAccessController != null) threadAccessController.checkThreadAccess();
         return super.add( t );
     }
 
     @Override
     public boolean remove( Object o )
     {
         threadAccessController.checkThreadAccess();
         return super.remove( o );
     }
 
     @Override
     public void clear()
     {
         threadAccessController.checkThreadAccess();
         super.clear();
     }
 
     @Override
     public Object clone()
     {
         threadAccessController.checkThreadAccess();
         return super.clone();
     }
 
     @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
     @Override
     public boolean equals( Object o )
     {
         threadAccessController.checkThreadAccess();
         return super.equals( o );
     }
 
     @Override
     public int hashCode()
     {
         threadAccessController.checkThreadAccess();
         return super.hashCode();
     }
 
     @Override
     public boolean removeAll( Collection<?> c )
     {
         threadAccessController.checkThreadAccess();
         return super.removeAll( c );
     }
 
     @Override
     public Object[] toArray()
     {
         threadAccessController.checkThreadAccess();
         return super.toArray();
     }
 
     @SuppressWarnings({"SuspiciousToArrayCall"})
     @Override
     public <T> T[] toArray( T[] a )
     {
         threadAccessController.checkThreadAccess();
         return super.toArray( a );
     }
 
     @Override
     public boolean containsAll( Collection<?> c )
     {
         threadAccessController.checkThreadAccess();
         return super.containsAll( c );
     }
 
     @Override
     public boolean addAll( Collection<? extends T> c )
     {
        if (threadAccessController != null) threadAccessController.checkThreadAccess();
         return super.addAll( c );
     }
 
     @Override
     public boolean retainAll( Collection<?> c )
     {
         threadAccessController.checkThreadAccess();
         return super.retainAll( c );
     }
 
     @Override
     public String toString()
     {
         threadAccessController.checkThreadAccess();
         return super.toString();
     }
 }
