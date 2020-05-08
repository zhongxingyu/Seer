import java.util.NoSuchElementException;

 public class LinkedList<E>
 {
     private class Node<E>
     {
         public E element;
         public Node<E> next;
         public Node<E> previous;
 
         public Node(E element)
         {
             this.element = element;
         }
 
         public void left(Node<E> node)
         {
             link(previous, node);
             link(node, this);
         }
 
         public void left(Node<E> first, Node<E> last)
         {
             link(previous, first);
             link(last, this);
         }
 
         private void link(Node<E> left, Node<E> right)
         {
             if (left != null)
             {
                 left.next = right;
             }
             if (right != null)
             {
                 right.previous = left;
             }
         }
 
         public void remove()
         {
             link(previous, next);
         }
 
         public void right(Node<E> node)
         {
             link(node, next);
             link(this, node);
         }
 
         public void right(Node<E> first, Node<E> last)
         {
             link(last, next);
             link(this, first);
         }
     }
 
     private Node<E> head;
     private Node<E> tail;
     private int size;
 
     public void addAfter(int index, E element)
     {
         if (!validIndex(index))
         {
             throw new IndexOutOfBoundsException("index");
         }
         node(index).right(new Node<E>(element));
         ++size;
     }
 
     public void addAfter(int index, LinkedList<E> linkedList)
     {
         if (!validIndex(index))
         {
             throw new IndexOutOfBoundsException("index");
         }
         if (linkedList.head == null)
         {
             return;
         }
         node(index).right(linkedList.head, linkedList.tail);
         size += linkedList.size;
         linkedList.head = linkedList.tail = null;
         linkedList.size = 0;
     }
 
     public void addBefore(int index, E element)
     {
         if (!validIndex(index))
         {
             throw new IndexOutOfBoundsException("index");
         }
         node(index).left(new Node<E>(element));
         ++size;
     }
 
     public void addBefore(int index, LinkedList<E> linkedList)
     {
         if (!validIndex(index))
         {
             throw new IndexOutOfBoundsException("index");
         }
         if (linkedList.head == null)
         {
             return;
         }
         node(index).left(linkedList.head, linkedList.tail);
         size += linkedList.size;
         linkedList.head = linkedList.tail = null;
         linkedList.size = 0;
     }
 
     public void addFirst(E element)
     {
         if (head == null)
         {
             head = tail = new Node<E>(element);
         }
         else
         {
             Node<E> node = new Node<E>(element);
             head.left(node);
             head = node;
         }
         ++size;
     }
 
     public void addFirst(LinkedList<E> linkedList)
     {
         if (head == null)
         {
             head = linkedList.head;
             tail = linkedList.tail;
         }
         else
         {
             head.left(linkedList.head, linkedList.tail);
             head = linkedList.head;
         }
         size += linkedList.size;
         linkedList.head = linkedList.tail = null;
         linkedList.size = 0;
     }
 
     public void addLast(E element)
     {
         if (head == null)
         {
             head = tail = new Node<E>(element);
         }
         else
         {
             Node<E> node = new Node<E>(element);
             tail.right(node);
             tail = node;
         }
         ++size;
     }
 
     public void addLast(LinkedList<E> linkedList)
     {
         if (head == null)
         {
             head = linkedList.head;
             tail = linkedList.tail;
         }
         else
         {
             tail.right(linkedList.head, linkedList.tail);
             tail = linkedList.tail;
         }
         size += linkedList.size;
         linkedList.head = linkedList.tail = null;
         linkedList.size = 0;
     }
 
     public void clear()
     {
         head = tail = null;
         size = 0;
     }
 
     public boolean contains(E element)
     {
         return index(element) != -1;
     }
 
     public boolean empty()
     {
         return head == null;
     }
 
     public boolean equals(Object object)
     {
         if (object == this)
         {
             return true;
         }
         if (object == null || !(object instanceof LinkedList<?>))
         {
             return false;
         }
         LinkedList<?> linkedList = (LinkedList<?>)object;
         if (size != linkedList.size())
         {
             return false;
         }
         Node ourNode = head;
         Node theirNode = linkedList.head;
         while (ourNode != null && theirNode != null)
         {
             if (!ourNode.element.equals(theirNode.element))
             {
                 return false;
             }
             ourNode = ourNode.next;
             theirNode = theirNode.next;
         }
         return true;
     }
 
     public E get(int index)
     {
         if (!validIndex(index))
         {
             throw new IndexOutOfBoundsException("index");
         }
         return node(index).element;
     }
 
     public E getFirst()
     {
         if (head == null)
         {
             throw new NoSuchElementException();
         }
         return head.element;
     }
 
     public E getLast()
     {
         if (head == null)
         {
             throw new NoSuchElementException();
         }
         return tail.element;
     }
 
     public int hashCode()
     {
         int hashCode = super.hashCode();
         for (int i = 0; i < size; ++i)
         {
             hashCode = hashCode * 31 + get(i).hashCode();
         }
         return hashCode;
     }
 
     public int index(E element)
     {
         if (head == null)
         {
             return -1;
         }
         int index = 0;
         for (Node<E> node = head; node != null; node = node.next)
         {
            if (node.element == null || element == null)
             {
				if (node.element == element)
				{
                	return index;
				}
             }
			else if (node.element.equals(element))
             {
                 return index;
             }
			++index;
         }
         return -1;
     }
 
     private Node<E> node(int index)
     {
         if (index < size / 2)
         {
             Node<E> node = head;
             int i = 0;
             while (i < index)
             {
                 node = node.next;
                 ++i;
             }
             return node;
         }
         else
         {
             Node<E> node = tail;
             int i = size - 1;
             while (i > index)
             {
                 node = node.previous;
                 --i;
             }
             return node;
         }
     }
 
     public E remove(int index)
     {
         if (!validIndex(index))
         {
             throw new IndexOutOfBoundsException("index");
         }
         if (index == 0)
         {
             return removeFirst();
         }
         if (index == size - 1)
         {
             return removeLast();
         }
         Node<E> node = node(index);
         node.remove();
         --size;
         return node.element;
     }
 
     public E removeFirst()
     {
         if (head == null)
         {
             throw new NoSuchElementException();
         }
         head.remove();
         E element = head.element;
         if (head == tail)
         {
             head = tail = null;
         }
         else
         {
             head = head.next;
         }
         --size;
         return element;
     }
 
     public E removeLast()
     {
         if (head == null)
         {
             throw new NoSuchElementException();
         }
         tail.remove();
         E element = tail.element;
         if (head == tail)
         {
             head = tail = null;
         }
         else
         {
             tail = tail.previous;
         }
         --size;
         return element;
     }
 
     public void set(int index, E element)
     {
         if (!validIndex(index))
         {
             throw new IndexOutOfBoundsException("index");
         }
         node(index).element = element;
     }
 
     public void setFirst(E element)
     {
         if (head == null)
         {
             throw new NoSuchElementException();
         }
         head.element = element;
     }
 
     public void setLast(E element)
     {
         if (head == null)
         {
             throw new NoSuchElementException();
         }
         tail.element = element;
     }
 
     public int size()
     {
         return size;
     }
 
     public String toString()
     {
         StringBuilder stringBuilder = new StringBuilder();
         stringBuilder.append("[");
         Node<E> node = head;
         while (node != null)
         {
             if (node != head)
             {
                 stringBuilder.append(", ");
             }
             stringBuilder.append(node.element.toString());
             node = node.next;
         }
         stringBuilder.append("]");
         return stringBuilder.toString();
     }
 
     private boolean validIndex(int index)
     {
         return index >= 0 && index + 1 <= size && size > 0;
     }
 }
