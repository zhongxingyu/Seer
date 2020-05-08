 import java.util.NoSuchElementException;
 import java.util.Iterator;
 
 @AuthorClass(author="Julian Schrittwieser")
 public class MyMap implements Iterable {
   private class Elem {
     private final String key;
     private final Object value;
 
     private Elem prev = null;
     private Elem next = null;
 
     private Elem(String key, Object value) {
       this.value = value;
       this.key = key;
     }
 
   }
 
   private class MyIterator implements Iterator {
     private Elem cur;
     private int curIdx = -1;
 
     private MyIterator(Elem start) {
       Elem temp = new Elem(null, null);
       temp.next = start;
       this.cur = temp;
     }
 
     public boolean hasNext() {
       return cur.next != null;
     }
 
     private boolean hasPrevious() {
       return cur.prev != null;
     }
 
     public String next() {
       if(!hasNext())
         throw new NoSuchElementException();
 
       cur = cur.next;
       curIdx += 1;
       return cur.key;
     }
 
     private Object value() {
       return cur.value;
     }
 
     private int nextIndex() {
       return curIdx + 1;
     }
 
     public void remove() {
       if(curIdx < 0)
         throw new IllegalStateException();
 
       if(curIdx == 0) {
         if(hasNext())
           first = cur.next;
         else
           first = null;
       }
 
       if(hasNext()) {
         cur.next.prev = cur.prev;
       } else {
         if(cur.prev == null)
           last = null;
         else
           last = cur.prev;
       }
 
       if(cur.prev != null)
         cur.prev.next = cur.next;
 
       size -= 1;
     }
 
     private void set(Object value) {
       Elem n = new Elem(cur.key, value);
       n.next = cur.next;
       n.prev = cur.prev;
 
       if(hasNext())
         cur.next.prev = n;
 
       if(hasPrevious())
         cur.prev.next = n;
     }
   }
 
   private Elem first = null;
   private Elem last = null;
   private int size = 0;
 
   public int size() {
     return size;
   }
 
   public Iterator iterator() {
     return new MyIterator(first);
   }
 
   public boolean contains(String key) {
     MyIterator iter = new MyIterator(first);
     while(iter.hasNext())
      if(iter.next() == key)
         return true;
     return false;
   }
 
   public void put(String key, Object value) {
     MyIterator iter = new MyIterator(first);
     while(iter.hasNext()) {
      if(iter.next() == key) {
         /* already have the key, change the value */
         iter.set(value);
         return;
       }
     }
 
     /* key not in map, add it to the end */
     Elem e = new Elem(key, value);
 
     if(first == null) {
       first = e;
       last = first;
     } else {
       last.next = e;
       e.prev = last;
       last = e;
     }
 
     size += 1;
   }
 
  public Object get(String key) {
     MyIterator iter = new MyIterator(first);
     while(iter.hasNext())
      if(iter.next() == key)
         return iter.value();
     return null;
   }
 
  public void remove(String key) {
     MyIterator iter = new MyIterator(first);
     while(iter.hasNext())
      if(iter.next() == key)
         iter.remove();
   }
 
 }
 
