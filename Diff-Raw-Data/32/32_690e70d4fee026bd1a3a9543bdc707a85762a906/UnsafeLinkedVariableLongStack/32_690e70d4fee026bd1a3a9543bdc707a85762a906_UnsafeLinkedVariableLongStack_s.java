 package largeStack;
 
 import sun.misc.Unsafe;
 
 import java.lang.reflect.Field;
 
 /**
  * An implementation with unsafe with stack extends implemented using a linked list.
  * No more array copies !
  * Stack extends have an increasing same, similarly to ArrayList.
  */
 public class UnsafeLinkedVariableLongStack implements LongStack {
 
 	public static final int DEFAULT_CHUNK_SIZE = 10000;
 
 	private IndexCell curChunk;
 
 	private int nextTop;
 
     private Unsafe unsafe;
 
     private int chunkSize;
 
     private IndexCell head;
 
     private IndexCell tail;
 
     private long capacity;
 
     public static final int SIZEOF_LONG = 8;
 
     public UnsafeLinkedVariableLongStack() throws Exception {
 		this(DEFAULT_CHUNK_SIZE);
 	}
 
 	public UnsafeLinkedVariableLongStack(int size) throws Exception {
             Field field = Unsafe.class.getDeclaredField("theUnsafe");
             field.setAccessible(true);
             unsafe = (Unsafe)field.get(null);
             this.chunkSize = size * SIZEOF_LONG;
             nextTop = 0;
             head = new IndexCell(unsafe.allocateMemory(this.chunkSize), chunkSize);
             curChunk = head;
             tail = head;
             capacity = chunkSize;
 	}
 
 	public void push(long v) {
 		if (nextTop == curChunk.size) {
             if (curChunk.next == null) {
                 increase();
             }
 		}
        unsafe.putLong(tail.value + nextTop, v);
         nextTop += SIZEOF_LONG;
 	}
 
     private void increase() {
        long cellCapa = capacity / 2;
         IndexCell cell = new IndexCell(unsafe.allocateMemory(cellCapa), cellCapa, tail, tail.next);
        capacity *= (3 / 2);
         tail = cell;
         curChunk = cell;
         nextTop=0;
     }
 
 	public long pop() {

         if (nextTop > 0) {
             nextTop -=SIZEOF_LONG;
         } else {
             curChunk = curChunk.prev;
             nextTop = (int)(curChunk.size - SIZEOF_LONG);
         }
         return unsafe.getInt(curChunk.value + nextTop);
 	}
 
     @Override
     public void free() {
         IndexCell c = head;
         while (c != null) {
             unsafe.freeMemory(c.value);
             c = c.next;
         }
     }
 
     @Override
     public long peek() {
         return unsafe.getInt(curChunk.value + nextTop - SIZEOF_LONG);
     }
 
     static class IndexCell {
 
         long value;
 
         long size;
 
         IndexCell next;
 
         IndexCell prev;
 
         public IndexCell(long v, long s, IndexCell p, IndexCell n) {
             this.size = s;
             this.value = v;
             this.next = n;
             this.prev = p;
         }
 
         public IndexCell(long v, long s) {
             this.size = s;
             value = v;
             next = null;
             prev = null;
         }
     }
 }
