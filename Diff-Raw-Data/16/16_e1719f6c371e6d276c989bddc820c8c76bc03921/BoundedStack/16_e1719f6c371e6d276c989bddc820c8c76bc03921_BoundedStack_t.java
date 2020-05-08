 package vladimir.chugunov.Stack;
 
 import interfaces.stack.IStack;
 
 /**
  * Ограниченный стек. Размер хранилища может быть изменен только мануально
  * <p/>
  * User: Alpen Ditrix Date: 12.11.13 Time: 14:10
  */
 public class BoundedStack<E> implements IStack<E> {
 
     /** "Хранилище" стека */
     private Object[] elementData;
     /** Индекс вершины стека */
     private int      elementCount;
 
 
     public BoundedStack(int initialCapacity) throws NegativeArraySizeException {
         elementData = new Object[initialCapacity];
         elementCount = -1;
     }
 
     @Override
     public void push(E element) throws ArrayIndexOutOfBoundsException {
         elementData[++elementCount] = element;
     }
 
     @Override
     public E pop() throws ArrayIndexOutOfBoundsException {
         E retVal = (E) elementData[elementCount];
         elementData[elementCount--] = null;
         return retVal;
     }
 
     @Override
     public E peek() throws ArrayStoreException {
         return (E) elementData[elementCount];
     }
 
     @Override
     public void clear() {
        elementData = new Object[elementData.length];
         elementCount = -1;
     }
 
     @Override
     public boolean isEmpty() {
         return elementCount == -1;
     }
 
     @Override
     public int size() {
         return elementCount + 1;
     }
 
     public int getBound() {
         return elementData.length;
     }
 
     /**
      * Создает хранилище нового размера и копирует в него все данные из старого. Новое хранилище точно должно вмещать
      * все элементы из старого (чтобы ничего не потерять)
      *
      * @param newCapacity новый размер
      *
      * @return был ли успешно изменен размер
      */
     public boolean checkAndSetNewCapacity(int newCapacity) {
        if (newCapacity <= elementCount) {
             return false;
         }
         Object[] newArray = new Object[newCapacity];
         System.arraycopy(elementData, 0, newArray, 0, elementCount + 1);
         elementData = newArray;
         return true;
     }
 }
