 /*
  * JBoss, Home of Professional Open Source
  * Copyright (c) 2010, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.beach.util.collection;      
 
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * List of elements that represent Child objects.
  *
  * Note that the list does not truly contain Child objects, but rather
  * objects which are adapted to become Child objects via a ChildClassAdapter.
  * 
  * @author <a href="cdewolf@redhat.com">Carlo de Wolf</a>
  */
 public class ChildrenList<E, P> extends AbstractList<E>
 {
    private static ChildClassAdapter<?, ?> defaultChildClassAdapter = new ChildClassAdapter<Child,Parent>() {
       @Override
       public Parent getParent(Child child)
       {
          return child.getParent();
       }
 
       @Override
       public void setParent(Child child, Parent parent)
       {
          child.setParent(parent);
       }
    };
    
    private ChildClassAdapter<E, P> childClassAdapter;
    
    private List<E> delegate;
 
    private P parent;
 
    public ChildrenList(P parent)
    {
       this(parent, (ChildClassAdapter<E, P>) defaultChildClassAdapter);
    }
 
   public ChildrenList(P parent, List<E> delegate)
   {
      this(parent, delegate, (ChildClassAdapter<E, P>) defaultChildClassAdapter);
   }

    public ChildrenList(P parent, ChildClassAdapter<E, P> childClassAdapter)
    {
      this(parent, new ArrayList<E>(), childClassAdapter);
   }

   public ChildrenList(P parent, List<E> delegate, ChildClassAdapter<E, P> childClassAdapter)
   {
       this.childClassAdapter = childClassAdapter;
      this.delegate = delegate;
       this.parent = parent;
    }
 
    @Override
    public boolean add(E e)
    {
       /*
        * Precondition checks
        */
       
       // Ensure we've got a valid child
       if(e==null)
       {
          throw new NullPointerException("Element to be added must be specified");
       }
       
       // Check to see the child doesn't already have a parent
       final P parent = childClassAdapter.getParent(e);
       if(parent!=null)
       {
          throw new RuntimeException("Cannot add as child element " + e + " which already has parent " + parent);
       }
       
       boolean success = delegate.add(e);
       if(success)
       {
         try
         {
            childClassAdapter.setParent(e, this.parent);
         }
         catch(RuntimeException ex)
         {
            // oh oh
            delegate.remove(e);
            throw ex;
         }
       }
       return success;
    }
 
    @Override
    public E get(int index)
    {
       return delegate.get(index);
    }
 
    @Override
    public int size()
    {
       return delegate.size();
    }
 }
