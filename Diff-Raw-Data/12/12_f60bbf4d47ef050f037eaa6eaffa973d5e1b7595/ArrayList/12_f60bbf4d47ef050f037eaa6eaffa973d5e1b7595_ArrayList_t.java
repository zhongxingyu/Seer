 package com.willfaught;
 
 import java.util.Iterator;
 
 public class ArrayList<E> implements List<E>, Queue<E>, Stack<E>
 {
     private int size;
     private Object[] elements;
 
     public ArrayList()
     {
         this(16);
     }
 
     public ArrayList(int capacity)
     {
         if (capacity < 0)
         {
             throw new IllegalArgumentException();
         }
         elements = new Object[capacity];
     }
 
     @Override
     public void add(E element)
     {
         if (size == elements.length)
         {
             grow();
         }
         elements[size++] = element;
     }
 
     @Override
     public void add(int index, E element)
     {
         if (index < 0 || index > size)
         {
             throw new IndexOutOfBoundsException();
         }
         if (size == elements.length)
         {
             grow();
         }
         for (int i = size; i > index; --i)
         {
             elements[i] = elements[i - 1];
         }
         elements[index] = element;
         ++size;
     }
 
     @Override
     public void clear()
     {
         for (int i = 0; i < size; ++i)
         {
             elements[i] = null;
         }
         size = 0;
     }
 
     @Override
     public boolean contains(E element)
     {
         return index(element) >= 0;
     }
 
     @Override
     public ArrayList<E> copy()
     {
         ArrayList<E> copy = new ArrayList<E>(size);
         if (size == 0)
         {
             return copy;
         }
         for (int i = 0; i < size; ++i)
         {
             if (elements[i] instanceof Copyable<?>)
             {
                 Copyable<?> copyable = (Copyable<?>) elements[i];
                 copy.elements[i] = copyable.copy();
             }
             else
             {
                 copy.elements[i] = elements[i];
             }
         }
         return copy;
     }
 
     @Override
     public E dequeue()
     {
         if (size == 0)
         {
             throw new IllegalStateException();
         }
         return remove(0);
     }
 
     @SuppressWarnings("unchecked")
     private E element(int index)
     {
         return (E) elements[index];
     }
 
     @Override
     public boolean empty()
     {
         return size == 0;
     }
 
     @Override
     public void enqueue(E element)
     {
         add(element);
     }
 
     @Override
     public boolean equals(Object object)
     {
         if (object == this)
         {
             return true;
         }
         if (!(object instanceof ArrayList<?>))
         {
             return false;
         }
         ArrayList<?> arrayList = (ArrayList<?>) object;
         if (size != arrayList.size)
         {
             return false;
         }
         for (int i = 0; i < size; ++i)
         {
             if (!elements[i].equals(arrayList.elements[i]))
             {
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public E get(int index)
     {
         if (!valid(index))
         {
             throw new IndexOutOfBoundsException();
         }
         return element(index);
     }
 
     private void grow()
     {
         Object[] copy = new Object[elements.length * 2];
         for (int i = 0; i < elements.length; ++i)
         {
             copy[i] = elements[i];
         }
         elements = copy;
     }
 
     @Override
     public int hashCode()
     {
         int hash = 0;
         for (int i = 0; i < size; ++i)
         {
             hash = hash * 31 + elements[i].hashCode();
         }
         return hash;
     }
 
     @Override
     public E head()
     {
         if (size == 0)
         {
             throw new IllegalStateException();
         }
         return element(0);
     }
 
     @Override
     public int index(E element)
     {
         for (int i = 0; i < size; ++i)
         {
             if (elements[i].equals(element))
             {
                 return i;
             }
         }
         return -1;
     }
 
     @Override
     public Iterator<E> iterator()
     {
         return new Iterator<E>()
         {
            private int index = 0;
 
             @Override
             public boolean hasNext()
             {
                return index < size;
             }
 
             @Override
             public E next()
             {
                 if (!hasNext())
                 {
                     throw new IllegalStateException();
                 }
                return element(index++);
             }
 
             @Override
             public void remove()
             {
                 throw new UnsupportedOperationException();
             }
         };
     }
 
     @Override
     public E pop()
     {
         if (size == 0)
         {
             throw new IllegalStateException();
         }
         return remove(size - 1);
     }
 
     @Override
     public void push(E element)
     {
         add(element);
     }
 
     @Override
     public void remove(E element)
     {
         if (element == null)
         {
             throw new IllegalArgumentException();
         }
         for (int i = 0; i < size; ++i)
         {
             if (element.equals(elements[i]))
             {
                 remove(i);
                 return;
             }
         }
     }
 
     @Override
     public E remove(int index)
     {
         if (!valid(index))
         {
             throw new IndexOutOfBoundsException();
         }
         E element = element(index);
         for (int i = index; i < size - 1; ++i)
         {
             elements[i] = elements[i + 1];
         }
         elements[size - 1] = null;
         --size;
         return element;
     }
 
     @Override
     public E set(int index, E element)
     {
         if (!valid(index))
         {
             throw new IndexOutOfBoundsException();
         }
         E old = element(index);
         elements[index] = element;
         return old;
     }
 
     @Override
     public int size()
     {
         return size;
     }
 
     @Override
     public E top()
     {
         if (size == 0)
         {
             throw new IllegalStateException();
         }
         return element(size - 1);
     }
 
     @Override
     public String toString()
     {
         CharArray charArray = new CharArray();
         charArray.append("[");
         for (int i = 0; i < size; ++i)
         {
             if (i != 0)
             {
                 charArray.append(", ");
             }
             charArray.append(elements[i].toString());
         }
         charArray.append("]");
         return charArray.toString();
     }
 
     private boolean valid(int index)
     {
         return size > 0 && index >= 0 && index < size;
     }
 }
