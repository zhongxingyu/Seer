 package vladimir.chugunov.Stack;
 
 import interfaces.stack.IStack;
 
 /**
  * Адаптивный стек. Размер хранилища изменяется автоматически или вручную.
  * <p/>
  * User: Alpen Ditrix Date: 12.11.13 Time: 15:06
  */
 public class ImmortalStack<E> implements IStack<E> {
 
     /** "Хранилище" стека */
     private Object[] elementData;
     /** Индекс вершины */
     private int      elementCount;
     /**
      * Коэффициент расширения массива. Когда текущий массив будет заполнен, в методе tryGrow() будет создан новый массив
      * со старыми данными, но размера, вычисленного по формуле {@code текущийРазмер * (1 + capacityIncrement}
      *
      * @see vladimir.chugunov.Stack.ImmortalStack#tryGrow()
      */
     private float    capacityIncrement;
 
 
     public ImmortalStack(int initialCapacity, float additionalCapacityIncrement) {
         elementData = new Object[initialCapacity];
         elementCount = -1;
         this.capacityIncrement = 1 + additionalCapacityIncrement;
     }
 
     public ImmortalStack(int initialCapacity) throws NegativeArraySizeException {
         this(initialCapacity, 0);
     }
 
     public ImmortalStack() {
         this(10);
     }
 
     /**
      * Проверяет не заполнился ли массив и расширяет его, если надо.
      *
      * @see ImmortalStack#capacityIncrement
      */
     private void tryGrow() {
         if (elementData.length != elementCount + 1) {
             return;
         } else {
             Object[] newArray = new Object[(int) ((elementCount + 1) * (1 + capacityIncrement))];
             System.arraycopy(elementData, 0, newArray, 0, elementCount + 1);
             elementData = newArray;
         }
     }
 
     /**
      * Проверяет не стало ли число элементов массива слишком мало, по сравнению с размером массива и уменьшает
      * "хранилище", если надо. Однако алгоритм никогда не создаст массив с длинной меньше 10
      * <p/>
      * Необходимым "магическим" условием для уменьшения "хранилища" считается "элементы занимают меньше четверти
      * хранилища"
      */
     private void tryShrink() {
         if (elementData.length / 4 <= elementCount) {
             return;
         } else {
             if (elementData.length > 10) {
                 Object[] newArray = new Object[Math.max(elementData.length / 2, 10)];
                 System.arraycopy(elementData, 0, newArray, 0, elementCount + 1);
                 elementData = newArray;
             }
         }
     }
 
     @Override
     public void push(E element) throws ArrayIndexOutOfBoundsException {
         tryGrow();
         elementData[++elementCount] = element;
     }
 
     @Override
     public E pop() throws ArrayIndexOutOfBoundsException {
         E retVal = (E) elementData[elementCount];
         elementData[elementCount--] = null;
         tryShrink();
         return retVal;
     }
 
     @Override
 
     public E peek() throws ArrayStoreException {
         return (E) elementData[elementCount];
     }
 
     @Override
     public void clear() {
         elementData = new Object[10];
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
 
     /**
      * Создает хранилище нового размера и копирует в него все данные из старого. Новое хранилище точно должно вмещать
      * все элементы из старого (чтобы ничего не потерять)
      *
      * @param newCapacity новый размер
      *
      * @return был ли успешно изменен размер
      */
     public boolean checkAndSetNewCapacity(int newCapacity) {
        if (newCapacity < elementCount) {
             return false;
         }
         Object[] newArray = new Object[newCapacity];
         System.arraycopy(elementData, 0, newArray, 0, elementCount + 1);
         elementData = newArray;
         return true;
     }
 
 }
