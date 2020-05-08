 package largeStack;
 
 import sun.misc.Unsafe;
 
 import java.lang.reflect.Field;
 
 /**
  * An implementation with unsafe with stack extends implemented using a linked list.
  * No more array copies !
  * Stack extends have always the same size, contrary to the principles of ArrayList.
  */
 public class UnsafeLinkedLongStack implements LongStack {
 
 	public static final int DEFAULT_CHUNK_SIZE = 10000;
 
 	private IndexCell curChunk;
 
 	private int nextTop;
 
     private Unsafe unsafe;
 
     private int chunkSize;
 
     private IndexCell head;
 
     private IndexCell tail;
 
     public static final int SIZEOF_LONG = 8;
 
     public UnsafeLinkedLongStack() throws Exception {
 		this(DEFAULT_CHUNK_SIZE);
 	}
 
 	public UnsafeLinkedLongStack(int size) throws Exception {
             Field field = Unsafe.class.getDeclaredField("theUnsafe");
             field.setAccessible(true);
             unsafe = (Unsafe)field.get(null);
             this.chunkSize = size * SIZEOF_LONG;
             nextTop = 0;
             head = new IndexCell(unsafe.allocateMemory(this.chunkSize));
             curChunk = head;
             tail = head;
 	}
 
 	public void push(long v) {
 		if (nextTop == chunkSize) {
             if (curChunk.next == null) {
                 increase();
             }
 		}
        unsafe.putLong(tail.value + nextTop, v);
         nextTop += SIZEOF_LONG;
 	}
 
     private void increase() {
        IndexCell cell = new IndexCell(unsafe.allocateMemory(chunkSize), tail, tail.next);
        tail.next = cell;
        tail = cell;
        curChunk = cell;
         nextTop=0;
     }
 
 	public long pop() {
 
         if (nextTop > 0) {
             nextTop -=SIZEOF_LONG;
         } else {
             nextTop = chunkSize - SIZEOF_LONG;
             curChunk = curChunk.prev;
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
 
         IndexCell next;
 
         IndexCell prev;
 
         public IndexCell(long v, IndexCell p, IndexCell n) {
             this.value = v;
             this.next = n;
             this.prev = p;
         }
 
         public IndexCell(long v) {
             value = v;
             next = null;
             prev = null;
         }
     }
 }
