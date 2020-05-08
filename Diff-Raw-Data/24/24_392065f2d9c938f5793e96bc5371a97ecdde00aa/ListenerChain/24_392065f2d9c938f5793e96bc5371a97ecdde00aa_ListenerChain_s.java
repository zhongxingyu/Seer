 
 package utils.event;
 
 import java.util.EventListener;
 
 /**
  * Класс для хранения получателей событий. В отличии от
  * <code>javax.swing.event.EventListenerList</code>
  * {@link #add(Class, EventListener) добавление} одного и того же получателя
  * возможно только один раз.
  * <p>
  * 
  * 
  * <pre>
  * // Пример использования ListenerChain для обслуживания UndoableEditEvent.
  * public class Example {
  *     protected ListenerChain listeners = new ListenerChain();
  * 
  *     // Добавление получателя сообщений.
  *     public void addUndoableEditListener(UndoableEditListener listener) {
  *         listeners.add(UndoableEditListener.class, listener);
  *     }
  * 
  *     // Удаление получателя сообщений.
  *     public void removeUndoableEditListener(UndoableEditListener listener) {
  *         listeners.remove(UndoableEditListener.class, listener);
  *     }
  * 
  *     // Выпуск сообщения.
  *     protected void fireUndoEvent(UndoableEditEvent event) {
  *         Object[] listenerArray = listeners.getListenerList();
  * 
  *         for (int i = 0; i &lt; listenerArray.length; i += 2) {
  *             if (listenerArray[i] == UndoableEditListener.class)
  *                 ((UndoableEditListener) listenerArray[i + 1])
  *                                 .undoableEditHappened(event);
  *         }
  *     }
  * }
  * </pre>
  */
 public class ListenerChain {
     /**
      * Шаг изменения размера массива {@link #items}. ВАЖНО! Шаг должен быть
      * кратен двум.
      */
     protected static int ARRAY_STEP_SIZE = 4;
     /** Число занятых ячеек массива {@link #items}. */
     private int          actualSize;
     /** Массив для хранения пар <code>класс:получатель</code>. */
     private Object[]     items;
 
     /**
      *
      */
     public ListenerChain() {
         actualSize = 0;
         items = new Object[ARRAY_STEP_SIZE];
     }
 
     /**
      * При необходимости изменяет размер массива {@link #items}. Переменная
      * {@link #actualSize} принимает значение <code>size</size>.
      * 
      * @param size Требуемый размер массива.
      */
    protected void _allocate(int size) {
         int sz;
 
         if (size == actualSize) return;
 
         sz = ((size + ARRAY_STEP_SIZE - 1) / ARRAY_STEP_SIZE) * ARRAY_STEP_SIZE;
 
         actualSize = size;
 
         if (items.length == sz) return;
 
         Object[] temp = new Object[sz];
         System.arraycopy(items, 0, temp, 0, sz < items.length ? sz
                         : items.length);
         items = temp;
     }
 
     /**
      * Добавляет получателя сообщений. Никаких проверок не производится.
      * 
      * @param <С> Тип получателя сообщений.
      * @param lClass Класс получателя сообщений.
      * @param listener Добавляемый получатель сообщений.
      */
    protected <С extends EventListener> void _add(Class<С> lClass, С listener) {
        _allocate(actualSize + 2);
         items[actualSize - 2] = lClass;
         items[actualSize - 1] = listener;
     }
 
     /**
      * Удаляет зарегистрированного получателя сообщений. Удаление происходит при
      * совпадении <code>lClass</code> и <code>listener</code>. Других проверок
      * не производится.
      * 
      * @param <С> Тип получателя сообщений.
      * @param lClass Класс получателя сообщений.
      * @param listener Удаляемый получатель сообщений.
      */
    protected <С extends EventListener> void _remove(Class<С> lClass, С listener) {
         for (int i = 0; i < actualSize; i += 2) {
             if (items[i] == lClass && items[i + 1] == listener) {
                 System.arraycopy(items, i + 2, items, i, actualSize - i - 2);
                _allocate(actualSize - 2);
                 break;
             }
         }
     }
 
     /**
      * Возвращает массив с парами <code>класс:получатель</code>. Чётные элементы
      * массива содержат класс получателя; нечётные - самого получателя
      * сообщений.
      * 
      * Подробнее в {@linkplain ListenerChain общем описание}
      */
     public Object[] getListenerList() {
         return items;
     }
 
     /**
      * Добавляет получателя сообщений. Если пара <code>класс:получатель</code>
      * уже зарегистрирована, то она сначала удаляется, а затем добавляется в
      * конец массива получателей. Таким образом, в массиве возможно только одно
      * сочетание <code>класс:получатель</code>.
      * 
      * @param <С> Тип получателя сообщений.
      * @param lClass Класс получателя сообщений.
      * @param listener Добавляемый получатель сообщений.
      */
     public synchronized <С extends EventListener> void add(Class<С> lClass,
                     С listener) {
         if (listener == null) return;
 
        _remove(lClass, listener);
        _add(lClass, listener);
     }
 
     /**
      * Удаляет зарегистрированного получателя сообщений. Удаление происходит при
      * совпадении <code>lClass</code> и <code>listener</code>.
      * 
      * @param <С> Тип получателя сообщений.
      * @param lClass Класс получателя сообщений.
      * @param listener Удаляемый получатель сообщений.
      */
     public synchronized <С extends EventListener> void remove(Class<С> lClass,
                     С listener) {
         if (listener == null) return;
 
        _remove(lClass, listener);
     }
 }
