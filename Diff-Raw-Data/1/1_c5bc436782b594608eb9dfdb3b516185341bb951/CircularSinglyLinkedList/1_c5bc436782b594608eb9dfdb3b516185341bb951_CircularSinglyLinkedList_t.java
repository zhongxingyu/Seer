 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.Random;
 
 public class CircularSinglyLinkedList<Elem> implements Iterable<Elem>
 {
     private int size = 0;  // La taille de la liste
     private Node last;  // Dernier element de la liste
 
     // Un noeud de la liste
     private class Node
     {
         private Elem elem;
         private Node next;
 
         public Node(Elem elem, Node next)
         {
             this.elem = elem;
             this.next = next;
         }
 
         public void setNext(Node next)
         {
             this.next = next;
         }
 
         public Node getNext()
         {
             return next;
         }
 
         public Elem getElem()
         {
             return elem;
         }
     }
 
     // Liste vide?
     public boolean isEmpty()
     {
         return size == 0;
     }
 
     // Taille de la liste
     public int size()
     {
         return size;
     }
 
     //Retourne l'element a l'index "index" s'il existe
     public Elem get(int index)
     {
         if (index >= size || index < 0)
         {
             throw new IndexOutOfBoundsException();
         }
         
         Node temp = last.getNext();
         for(int i = 0; i < index; i++)
         {
             temp = temp.getNext();
         }
         return temp.getElem();
     }
 
     //Creation du premier element de la liste
     private void init(Elem item)
     {
         last = new Node(item, null);
         last.setNext(last);
         size = 1;
     }
 
     //Ajout d'un element a la fin de la liste
     public void append(Elem item)
     {
         //La liste est vide
         if (size == 0)
         {
             init(item);
         }
         else
         {
             Node ajout = new Node(item, last.getNext());
             last.setNext(ajout);
             size++;
             last = ajout;
         }
     }
 
     //Ajoute un element a la position "index" dans la liste
     public void insert(Elem item, int index)
     {
         if (index > size || index < 0)
         {
             throw new IndexOutOfBoundsException();
         }
 
         //La liste est vide
         if (size == 0)
         {
             init(item);
         }
         //On insere a la fin
         else if (index == size)
         {
             append(item);
         }
         //La liste a au moins un element et l'insertion n'est pas a la fin
         else
         {
             Node temp = last;
             for(int i = 0; i < index; i++) // Ends up at index - 1
             {
                 temp = temp.getNext();
             }
             Node ajout = new Node(item, temp.getNext());
             temp.setNext(ajout);
             size++;
         }
     }
 
     //Elimine un element a une position donnee dans la liste
     public void remove(int index)
     {
         if (index >= size || index < 0)
         {
             throw new IndexOutOfBoundsException();
         }
         
         Node temp = last;
         
         for(int i = 0; i < index; i++)
         {
             temp = last.getNext();
         }
         temp.setNext(temp.getNext().getNext());
         size--;
     }
 
     // Methode requise par l'interface Iterable
     public Iterator<Elem> iterator()
     {
         return new CircularSinglyLinkedListIterator();
     }
 
     // L'iterateur retourne par la methode iterator()
     private class CircularSinglyLinkedListIterator implements Iterator<Elem>
     {
         private Node currentNode = last;
         private int i = 0;
 
         public boolean hasNext()
         {
             return i < size;
         }
 
         public void remove()
         {
             throw new UnsupportedOperationException();
         }
 
         public Elem next()
         {
             if (!hasNext())
             {
                 throw new NoSuchElementException();
             }
             currentNode = currentNode.getNext();
             i++;
             return currentNode.getElem();
         }
     }
 
     public Iterator<Elem> josephusIterator()
     {
         return new JosephusIterator();
     }
 
     private class JosephusIterator implements Iterator<Elem>
     {
         // L'interface "Iterator" impose une condition sur l'appel a la
         // fonction remove() de l'iterateur.
         // Voyez: http://download.oracle.com/javase/6/docs/api/java/util/Iterator.html#remove()
         private boolean canRemove = false;
         private Node currentNode = last;
         private int position = 0;
 
         public boolean hasNext()
         {
             canRemove = last.getNext() != last;
             return canRemove;
         }
 
         public void remove()
         {
             if (!canRemove)
             {
                 throw new IllegalStateException();
             }
             // Si vous desirez appeler la fonction remove() de CircularSinglyLinkedList,
             // ecrivez CircularSinglyLinkedList.this.remove(i)
             
             CircularSinglyLinkedList.this.remove(position);
            position--;
         }
 
         public Elem next()
         {
             if (!hasNext())
             {
                 throw new NoSuchElementException();
             }
             
             currentNode = currentNode.getNext();
             if (position >= size - 1)
             {
                 position = 0;
             }
             else
             {
                 position++;
             }
 
 //            if(currentNode.getNext() == currentNode)
 //            {
 //                canRemove = false;
 //            }
             return currentNode.getElem();
         }
     }
 
     /*
      * ******************************
      * Expected output:
      *
      * Forward list: 0 1 2 3 4 5 6 7 8 9      *
      * Reverse list: 9 8 7 6 5 4 3 2 1 0      *
      * Mod3 list: 9 6 7 8 3 4 5 0 1 2      *
      * Forward list after removal: 9      *
      * AU CACHOT: 9 4 8 2 5 6 3 0 7      *
      * LIBERTE POUR LE CONDAMNE: 1 ********************************
      */
     public static void main(String[] args)
     {
         CircularSinglyLinkedList<Integer> circularListForward = new CircularSinglyLinkedList<Integer>();
         CircularSinglyLinkedList<Integer> circularListReverse = new CircularSinglyLinkedList<Integer>();
         CircularSinglyLinkedList<Integer> circularListMod3 = new CircularSinglyLinkedList<Integer>();
 
         for (int i = 0; i < 10; i++)
         {
             circularListForward.append(i);
             circularListReverse.insert(i, 0);
             circularListMod3.insert(i, i % 3);
         }
 
         System.out.println("Forward list: ");
         for (Integer i : circularListForward)
         {
             System.out.print(i + " ");
         }
         System.out.println("\n");
 
         System.out.println("Reverse list: ");
         for (Integer i : circularListReverse)
         {
             System.out.print(i + " ");
         }
         System.out.println("\n");
 
         System.out.println("Mod3 list: ");
         for (Integer i : circularListMod3)
         {
             System.out.print(i + " ");
         }
         System.out.println("\n");
 
         for (int i = 0; i < 9; i++)
         {
             circularListForward.remove(0);
         }
         System.out.println("Forward list after removal: ");
         for (Integer i : circularListForward)
         {
             System.out.print(i + " ");
         }
         System.out.println("\n");
 
         System.out.print("AU CACHOT: ");
         Iterator<Integer> josephusIt = circularListReverse.josephusIterator();
         int i = 0;
         Integer condamne = null;
         while (josephusIt.hasNext())
         {
             condamne = josephusIt.next();
             //Too bad... Au cachot!
             if (i % 5 == 0)
             {
                 System.out.print(condamne + " ");
                 josephusIt.remove();
             }
             i++;
         }
         System.out.println("\n");
         System.out.println("LIBERTE POUR LE CONDAMNE: " + circularListReverse.get(0));
     }
 }
